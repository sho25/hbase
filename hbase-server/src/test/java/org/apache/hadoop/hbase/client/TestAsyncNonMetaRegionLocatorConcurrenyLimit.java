begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
package|;
end_package

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
operator|.
name|toList
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|AsyncNonMetaRegionLocator
operator|.
name|MAX_CONCURRENT_LOCATE_REQUEST_PER_TABLE
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|ConnectionUtils
operator|.
name|isEmptyStartRow
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|ConnectionUtils
operator|.
name|isEmptyStopRow
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|coprocessor
operator|.
name|CoprocessorHost
operator|.
name|REGION_COPROCESSOR_CONF_KEY
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CompletableFuture
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|IntStream
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|io
operator|.
name|IOUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseClassTestRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseTestingUtility
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HRegionLocation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|RegionLocations
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|TableName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|coprocessor
operator|.
name|ObserverContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|coprocessor
operator|.
name|RegionCoprocessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|coprocessor
operator|.
name|RegionCoprocessorEnvironment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|coprocessor
operator|.
name|RegionObserver
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|InternalScanner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|security
operator|.
name|User
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|testclassification
operator|.
name|ClientTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|testclassification
operator|.
name|MediumTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Bytes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Threads
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestAsyncNonMetaRegionLocatorConcurrenyLimit
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestAsyncNonMetaRegionLocatorConcurrenyLimit
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"async"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|AsyncConnectionImpl
name|CONN
decl_stmt|;
specifier|private
specifier|static
name|AsyncNonMetaRegionLocator
name|LOCATOR
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
index|[]
name|SPLIT_KEYS
decl_stmt|;
specifier|private
specifier|static
name|int
name|MAX_ALLOWED
init|=
literal|2
decl_stmt|;
specifier|private
specifier|static
name|AtomicInteger
name|CONCURRENCY
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|AtomicInteger
name|MAX_CONCURRENCY
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
class|class
name|CountingRegionObserver
implements|implements
name|RegionCoprocessor
implements|,
name|RegionObserver
block|{
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|RegionObserver
argument_list|>
name|getRegionObserver
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|preScannerNext
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|InternalScanner
name|s
parameter_list|,
name|List
argument_list|<
name|Result
argument_list|>
name|result
parameter_list|,
name|int
name|limit
parameter_list|,
name|boolean
name|hasNext
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|c
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|isMetaRegion
argument_list|()
condition|)
block|{
name|int
name|concurrency
init|=
name|CONCURRENCY
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
for|for
control|(
init|;
condition|;
control|)
block|{
name|int
name|max
init|=
name|MAX_CONCURRENCY
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|concurrency
operator|<=
name|max
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|MAX_CONCURRENCY
operator|.
name|compareAndSet
argument_list|(
name|max
argument_list|,
name|concurrency
argument_list|)
condition|)
block|{
break|break;
block|}
block|}
name|Threads
operator|.
name|sleepWithoutInterrupt
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
return|return
name|hasNext
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|postScannerNext
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|InternalScanner
name|s
parameter_list|,
name|List
argument_list|<
name|Result
argument_list|>
name|result
parameter_list|,
name|int
name|limit
parameter_list|,
name|boolean
name|hasNext
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|c
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|isMetaRegion
argument_list|()
condition|)
block|{
name|CONCURRENCY
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
block|}
return|return
name|hasNext
return|;
block|}
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|REGION_COPROCESSOR_CONF_KEY
argument_list|,
name|CountingRegionObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|MAX_CONCURRENT_LOCATE_REQUEST_PER_TABLE
argument_list|,
name|MAX_ALLOWED
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|balancerSwitch
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ConnectionRegistry
name|registry
init|=
name|ConnectionRegistryFactory
operator|.
name|getRegistry
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|CONN
operator|=
operator|new
name|AsyncConnectionImpl
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|registry
argument_list|,
name|registry
operator|.
name|getClusterId
argument_list|()
operator|.
name|get
argument_list|()
argument_list|,
literal|null
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
argument_list|)
expr_stmt|;
name|LOCATOR
operator|=
operator|new
name|AsyncNonMetaRegionLocator
argument_list|(
name|CONN
argument_list|)
expr_stmt|;
name|SPLIT_KEYS
operator|=
name|IntStream
operator|.
name|range
argument_list|(
literal|1
argument_list|,
literal|256
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|i
lambda|->
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%02x"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
operator|.
name|toArray
argument_list|(
name|byte
index|[]
index|[]
operator|::
operator|new
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY
argument_list|,
name|SPLIT_KEYS
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|IOUtils
operator|.
name|closeQuietly
argument_list|(
name|CONN
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|assertLocs
parameter_list|(
name|List
argument_list|<
name|CompletableFuture
argument_list|<
name|RegionLocations
argument_list|>
argument_list|>
name|futures
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|assertEquals
argument_list|(
literal|256
argument_list|,
name|futures
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|futures
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|HRegionLocation
name|loc
init|=
name|futures
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getDefaultRegionLocation
argument_list|()
decl_stmt|;
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|assertTrue
argument_list|(
name|isEmptyStartRow
argument_list|(
name|loc
operator|.
name|getRegion
argument_list|()
operator|.
name|getStartKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%02x"
argument_list|,
name|i
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|loc
operator|.
name|getRegion
argument_list|()
operator|.
name|getStartKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|i
operator|==
name|futures
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|)
block|{
name|assertTrue
argument_list|(
name|isEmptyStopRow
argument_list|(
name|loc
operator|.
name|getRegion
argument_list|()
operator|.
name|getEndKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%02x"
argument_list|,
name|i
operator|+
literal|1
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|loc
operator|.
name|getRegion
argument_list|()
operator|.
name|getEndKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|List
argument_list|<
name|CompletableFuture
argument_list|<
name|RegionLocations
argument_list|>
argument_list|>
name|futures
init|=
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
literal|256
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|i
lambda|->
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%02x"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
operator|.
name|map
argument_list|(
name|r
lambda|->
name|LOCATOR
operator|.
name|getRegionLocations
argument_list|(
name|TABLE_NAME
argument_list|,
name|r
argument_list|,
name|RegionReplicaUtil
operator|.
name|DEFAULT_REPLICA_ID
argument_list|,
name|RegionLocateType
operator|.
name|CURRENT
argument_list|,
literal|false
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|toList
argument_list|()
argument_list|)
decl_stmt|;
name|assertLocs
argument_list|(
name|futures
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"max allowed is "
operator|+
name|MAX_ALLOWED
operator|+
literal|" but actual is "
operator|+
name|MAX_CONCURRENCY
operator|.
name|get
argument_list|()
argument_list|,
name|MAX_CONCURRENCY
operator|.
name|get
argument_list|()
operator|<=
name|MAX_ALLOWED
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

