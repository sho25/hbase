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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HConstants
operator|.
name|HBASE_CLIENT_META_OPERATION_TIMEOUT
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
name|master
operator|.
name|LoadBalancer
operator|.
name|TABLES_ON_MASTER
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
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|Random
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
name|ExecutorService
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
name|Executors
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
name|Future
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
name|TimeUnit
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
name|AtomicBoolean
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
name|Collectors
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
name|MemoryCompactionPolicy
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
name|ServerName
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
name|Waiter
operator|.
name|ExplainingPredicate
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
name|io
operator|.
name|ByteBufferPool
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
name|CompactingMemStore
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
name|HRegion
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
name|LargeTests
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
name|RetryCounter
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

begin_comment
comment|/**  * Will split the table, and move region randomly when testing.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|LargeTests
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
name|TestAsyncTableGetMultiThreaded
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
name|TestAsyncTableGetMultiThreaded
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
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|int
name|COUNT
init|=
literal|1000
decl_stmt|;
specifier|private
specifier|static
name|AsyncConnection
name|CONN
decl_stmt|;
specifier|private
specifier|static
name|AsyncTable
argument_list|<
name|?
argument_list|>
name|TABLE
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
index|[]
name|SPLIT_KEYS
decl_stmt|;
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
name|setUp
argument_list|(
name|MemoryCompactionPolicy
operator|.
name|NONE
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|void
name|setUp
parameter_list|(
name|MemoryCompactionPolicy
name|memoryCompaction
parameter_list|)
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|TABLES_ON_MASTER
argument_list|,
literal|"none"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setLong
argument_list|(
name|HBASE_CLIENT_META_OPERATION_TIMEOUT
argument_list|,
literal|60000L
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|ByteBufferPool
operator|.
name|MAX_POOL_SIZE_KEY
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|CompactingMemStore
operator|.
name|COMPACTING_MEMSTORE_TYPE_KEY
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|memoryCompaction
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|SPLIT_KEYS
operator|=
operator|new
name|byte
index|[
literal|8
index|]
index|[]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|111
init|;
name|i
operator|<
literal|999
condition|;
name|i
operator|+=
literal|111
control|)
block|{
name|SPLIT_KEYS
index|[
name|i
operator|/
literal|111
operator|-
literal|1
index|]
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|CONN
operator|=
name|ConnectionFactory
operator|.
name|createAsyncConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|TABLE
operator|=
name|CONN
operator|.
name|getTableBuilder
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|setReadRpcTimeout
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
operator|.
name|setMaxRetries
argument_list|(
literal|1000
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|TABLE
operator|.
name|putAll
argument_list|(
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|COUNT
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|i
lambda|->
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
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
name|run
parameter_list|(
name|AtomicBoolean
name|stop
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
while|while
condition|(
operator|!
name|stop
operator|.
name|get
argument_list|()
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|COUNT
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|i
argument_list|,
name|Bytes
operator|.
name|toInt
argument_list|(
name|TABLE
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
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
name|Exception
block|{
name|int
name|numThreads
init|=
literal|20
decl_stmt|;
name|AtomicBoolean
name|stop
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|ExecutorService
name|executor
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|numThreads
argument_list|,
name|Threads
operator|.
name|newDaemonThreadFactory
argument_list|(
literal|"TestAsyncGet-"
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Future
argument_list|<
name|?
argument_list|>
argument_list|>
name|futures
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|numThreads
argument_list|)
operator|.
name|forEach
argument_list|(
name|i
lambda|->
name|futures
operator|.
name|add
argument_list|(
name|executor
operator|.
name|submit
argument_list|(
parameter_list|()
lambda|->
block|{
name|run
argument_list|(
name|stop
argument_list|)
argument_list|;       return
literal|null
argument_list|;
block|}
block|)
end_class

begin_empty_stmt
unit|))
empty_stmt|;
end_empty_stmt

begin_expr_stmt
name|Collections
operator|.
name|shuffle
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|SPLIT_KEYS
argument_list|)
argument_list|,
operator|new
name|Random
argument_list|(
literal|123
argument_list|)
argument_list|)
expr_stmt|;
end_expr_stmt

begin_decl_stmt
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
end_decl_stmt

begin_for
for|for
control|(
name|byte
index|[]
name|splitPoint
range|:
name|SPLIT_KEYS
control|)
block|{
name|int
name|oldRegionCount
init|=
name|admin
operator|.
name|getRegions
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
name|admin
operator|.
name|split
argument_list|(
name|TABLE_NAME
argument_list|,
name|splitPoint
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
operator|new
name|ExplainingPredicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|size
argument_list|()
operator|>
name|oldRegionCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|explainFailure
parameter_list|()
throws|throws
name|Exception
block|{
return|return
literal|"Split has not finished yet"
return|;
block|}
block|}
argument_list|)
expr_stmt|;
for|for
control|(
name|HRegion
name|region
range|:
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|TABLE_NAME
argument_list|)
control|)
block|{
name|region
operator|.
name|compact
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|//Waiting for compaction to complete and references are cleaned up
name|RetryCounter
name|retrier
init|=
operator|new
name|RetryCounter
argument_list|(
literal|30
argument_list|,
literal|1
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
while|while
condition|(
name|CompactionState
operator|.
name|NONE
operator|!=
name|admin
operator|.
name|getCompactionStateForRegion
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
operator|&&
name|retrier
operator|.
name|shouldRetry
argument_list|()
condition|)
block|{
name|retrier
operator|.
name|sleepUntilNextRetry
argument_list|()
expr_stmt|;
block|}
name|region
operator|.
name|getStores
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|closeAndArchiveCompactedFiles
argument_list|()
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
name|admin
operator|.
name|balance
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
name|ServerName
name|metaServer
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getServerHoldingMeta
argument_list|()
decl_stmt|;
name|ServerName
name|newMetaServer
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|t
lambda|->
name|t
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
operator|.
name|filter
argument_list|(
name|s
lambda|->
operator|!
name|s
operator|.
name|equals
argument_list|(
name|metaServer
argument_list|)
argument_list|)
operator|.
name|findAny
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|admin
operator|.
name|move
argument_list|(
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|newMetaServer
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
block|}
end_for

begin_expr_stmt
name|stop
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|executor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
end_expr_stmt

begin_for
for|for
control|(
name|Future
argument_list|<
name|?
argument_list|>
name|future
range|:
name|futures
control|)
block|{
name|future
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
end_for

unit|} }
end_unit

