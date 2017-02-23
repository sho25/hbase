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
name|HConstants
operator|.
name|EMPTY_END_ROW
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
name|HConstants
operator|.
name|EMPTY_START_ROW
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|instanceOf
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
name|assertArrayEquals
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
name|assertSame
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
name|assertThat
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
name|Arrays
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
name|ThreadLocalRandom
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
name|HRegionInfo
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
name|NotServingRegionException
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
name|TableNotFoundException
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
name|junit
operator|.
name|After
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
name|TestAsyncNonMetaRegionLocator
block|{
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
name|setBalancerRunning
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|AsyncRegistry
name|registry
init|=
name|AsyncRegistryFactory
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
annotation|@
name|After
specifier|public
name|void
name|tearDownAfterTest
parameter_list|()
throws|throws
name|IOException
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|TABLE_NAME
argument_list|)
condition|)
block|{
if|if
condition|(
name|admin
operator|.
name|isTableEnabled
argument_list|(
name|TABLE_NAME
argument_list|)
condition|)
block|{
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
name|LOCATOR
operator|.
name|clearCache
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|createSingleRegionTable
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoTable
parameter_list|()
throws|throws
name|InterruptedException
block|{
for|for
control|(
name|RegionLocateType
name|locateType
range|:
name|RegionLocateType
operator|.
name|values
argument_list|()
control|)
block|{
try|try
block|{
name|LOCATOR
operator|.
name|getRegionLocation
argument_list|(
name|TABLE_NAME
argument_list|,
name|EMPTY_START_ROW
argument_list|,
name|locateType
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|TableNotFoundException
operator|.
name|class
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
name|testDisableTable
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|createSingleRegionTable
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
for|for
control|(
name|RegionLocateType
name|locateType
range|:
name|RegionLocateType
operator|.
name|values
argument_list|()
control|)
block|{
try|try
block|{
name|LOCATOR
operator|.
name|getRegionLocation
argument_list|(
name|TABLE_NAME
argument_list|,
name|EMPTY_START_ROW
argument_list|,
name|locateType
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|TableNotFoundException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|assertLocEquals
parameter_list|(
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|ServerName
name|serverName
parameter_list|,
name|HRegionLocation
name|loc
parameter_list|)
block|{
name|HRegionInfo
name|info
init|=
name|loc
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|TABLE_NAME
argument_list|,
name|info
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|startKey
argument_list|,
name|info
operator|.
name|getStartKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|endKey
argument_list|,
name|info
operator|.
name|getEndKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|serverName
argument_list|,
name|loc
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSingleRegionTable
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|createSingleRegionTable
argument_list|()
expr_stmt|;
name|ServerName
name|serverName
init|=
name|TEST_UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|getServerName
argument_list|()
decl_stmt|;
for|for
control|(
name|RegionLocateType
name|locateType
range|:
name|RegionLocateType
operator|.
name|values
argument_list|()
control|)
block|{
name|assertLocEquals
argument_list|(
name|EMPTY_START_ROW
argument_list|,
name|EMPTY_END_ROW
argument_list|,
name|serverName
argument_list|,
name|LOCATOR
operator|.
name|getRegionLocation
argument_list|(
name|TABLE_NAME
argument_list|,
name|EMPTY_START_ROW
argument_list|,
name|locateType
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|randKey
init|=
operator|new
name|byte
index|[
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextInt
argument_list|(
literal|128
argument_list|)
index|]
decl_stmt|;
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextBytes
argument_list|(
name|randKey
argument_list|)
expr_stmt|;
for|for
control|(
name|RegionLocateType
name|locateType
range|:
name|RegionLocateType
operator|.
name|values
argument_list|()
control|)
block|{
name|assertLocEquals
argument_list|(
name|EMPTY_START_ROW
argument_list|,
name|EMPTY_END_ROW
argument_list|,
name|serverName
argument_list|,
name|LOCATOR
operator|.
name|getRegionLocation
argument_list|(
name|TABLE_NAME
argument_list|,
name|randKey
argument_list|,
name|locateType
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|createMultiRegionTable
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
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
specifier|private
specifier|static
name|byte
index|[]
index|[]
name|getStartKeys
parameter_list|()
block|{
name|byte
index|[]
index|[]
name|startKeys
init|=
operator|new
name|byte
index|[
name|SPLIT_KEYS
operator|.
name|length
operator|+
literal|1
index|]
index|[]
decl_stmt|;
name|startKeys
index|[
literal|0
index|]
operator|=
name|EMPTY_START_ROW
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|SPLIT_KEYS
argument_list|,
literal|0
argument_list|,
name|startKeys
argument_list|,
literal|1
argument_list|,
name|SPLIT_KEYS
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|startKeys
return|;
block|}
specifier|private
specifier|static
name|byte
index|[]
index|[]
name|getEndKeys
parameter_list|()
block|{
name|byte
index|[]
index|[]
name|endKeys
init|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|SPLIT_KEYS
argument_list|,
name|SPLIT_KEYS
operator|.
name|length
operator|+
literal|1
argument_list|)
decl_stmt|;
name|endKeys
index|[
name|endKeys
operator|.
name|length
operator|-
literal|1
index|]
operator|=
name|EMPTY_START_ROW
expr_stmt|;
return|return
name|endKeys
return|;
block|}
specifier|private
name|ServerName
index|[]
name|getLocations
parameter_list|(
name|byte
index|[]
index|[]
name|startKeys
parameter_list|)
block|{
name|ServerName
index|[]
name|serverNames
init|=
operator|new
name|ServerName
index|[
name|startKeys
operator|.
name|length
index|]
decl_stmt|;
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
argument_list|)
operator|.
name|forEach
argument_list|(
name|rs
lambda|->
block|{
name|rs
operator|.
name|getOnlineRegions
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|forEach
argument_list|(
name|r
lambda|->
block|{
name|serverNames
index|[
name|Arrays
operator|.
name|binarySearch
argument_list|(
name|startKeys
argument_list|,
name|r
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|Bytes
operator|::
name|compareTo
argument_list|)
index|]
operator|=
name|rs
operator|.
name|getServerName
argument_list|()
argument_list|;
block|}
block|)
class|;
end_class

begin_empty_stmt
unit|})
empty_stmt|;
end_empty_stmt

begin_return
return|return
name|serverNames
return|;
end_return

begin_function
unit|}    @
name|Test
specifier|public
name|void
name|testMultiRegionTable
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|createMultiRegionTable
argument_list|()
expr_stmt|;
name|byte
index|[]
index|[]
name|startKeys
init|=
name|getStartKeys
argument_list|()
decl_stmt|;
name|ServerName
index|[]
name|serverNames
init|=
name|getLocations
argument_list|(
name|startKeys
argument_list|)
decl_stmt|;
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
operator|.
name|forEach
argument_list|(
name|n
lambda|->
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|startKeys
operator|.
name|length
argument_list|)
operator|.
name|forEach
argument_list|(
name|i
lambda|->
block|{
block|try
block|{
name|assertLocEquals
argument_list|(
name|startKeys
index|[
name|i
index|]
argument_list|,
name|i
operator|==
name|startKeys
operator|.
name|length
operator|-
literal|1
condition|?
name|EMPTY_END_ROW
else|:
name|startKeys
index|[
name|i
operator|+
literal|1
index|]
argument_list|,
name|serverNames
index|[
name|i
index|]
argument_list|,
name|LOCATOR
operator|.
name|getRegionLocation
argument_list|(
name|TABLE_NAME
argument_list|,
name|startKeys
index|[
name|i
index|]
argument_list|,
name|RegionLocateType
operator|.
name|CURRENT
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
decl||
name|ExecutionException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
end_function

begin_empty_stmt
unit|))
empty_stmt|;
end_empty_stmt

begin_expr_stmt
name|LOCATOR
operator|.
name|clearCache
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
operator|.
name|forEach
argument_list|(
name|n
lambda|->
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|startKeys
operator|.
name|length
argument_list|)
operator|.
name|forEach
argument_list|(
name|i
lambda|->
block|{
block|try
block|{
name|assertLocEquals
argument_list|(
name|startKeys
index|[
name|i
index|]
argument_list|,
name|i
operator|==
name|startKeys
operator|.
name|length
operator|-
literal|1
condition|?
name|EMPTY_END_ROW
else|:
name|startKeys
index|[
name|i
operator|+
literal|1
index|]
argument_list|,
name|serverNames
index|[
name|i
index|]
argument_list|,
name|LOCATOR
operator|.
name|getRegionLocation
argument_list|(
name|TABLE_NAME
argument_list|,
name|startKeys
index|[
name|i
index|]
argument_list|,
name|RegionLocateType
operator|.
name|AFTER
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
decl||
name|ExecutionException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
end_expr_stmt

begin_empty_stmt
unit|}))
empty_stmt|;
end_empty_stmt

begin_expr_stmt
name|LOCATOR
operator|.
name|clearCache
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
end_expr_stmt

begin_decl_stmt
name|byte
index|[]
index|[]
name|endKeys
init|=
name|getEndKeys
argument_list|()
decl_stmt|;
end_decl_stmt

begin_expr_stmt
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
operator|.
name|forEach
argument_list|(
name|n
lambda|->
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|endKeys
operator|.
name|length
argument_list|)
operator|.
name|map
argument_list|(
name|i
lambda|->
name|endKeys
operator|.
name|length
operator|-
literal|1
operator|-
name|i
argument_list|)
operator|.
name|forEach
argument_list|(
name|i
lambda|->
block|{
block|try
block|{
name|assertLocEquals
argument_list|(
name|i
operator|==
literal|0
condition|?
name|EMPTY_START_ROW
else|:
name|endKeys
index|[
name|i
operator|-
literal|1
index|]
argument_list|,
name|endKeys
index|[
name|i
index|]
argument_list|,
name|serverNames
index|[
name|i
index|]
argument_list|,
name|LOCATOR
operator|.
name|getRegionLocation
argument_list|(
name|TABLE_NAME
argument_list|,
name|endKeys
index|[
name|i
index|]
argument_list|,
name|RegionLocateType
operator|.
name|BEFORE
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
decl||
name|ExecutionException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
end_expr_stmt

begin_empty_stmt
unit|}))
empty_stmt|;
end_empty_stmt

begin_function
unit|}    @
name|Test
specifier|public
name|void
name|testRegionMove
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|createSingleRegionTable
argument_list|()
expr_stmt|;
name|ServerName
name|serverName
init|=
name|TEST_UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|HRegionLocation
name|loc
init|=
name|LOCATOR
operator|.
name|getRegionLocation
argument_list|(
name|TABLE_NAME
argument_list|,
name|EMPTY_START_ROW
argument_list|,
name|RegionLocateType
operator|.
name|CURRENT
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertLocEquals
argument_list|(
name|EMPTY_START_ROW
argument_list|,
name|EMPTY_END_ROW
argument_list|,
name|serverName
argument_list|,
name|loc
argument_list|)
expr_stmt|;
name|ServerName
name|newServerName
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
name|sn
lambda|->
operator|!
name|sn
operator|.
name|equals
argument_list|(
name|serverName
argument_list|)
argument_list|)
operator|.
name|findAny
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|move
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|newServerName
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|TEST_UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|getServerName
argument_list|()
operator|.
name|equals
argument_list|(
name|newServerName
argument_list|)
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
comment|// Should be same as it is in cache
name|assertSame
argument_list|(
name|loc
argument_list|,
name|LOCATOR
operator|.
name|getRegionLocation
argument_list|(
name|TABLE_NAME
argument_list|,
name|EMPTY_START_ROW
argument_list|,
name|RegionLocateType
operator|.
name|CURRENT
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|LOCATOR
operator|.
name|updateCachedLocation
argument_list|(
name|loc
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// null error will not trigger a cache cleanup
name|assertSame
argument_list|(
name|loc
argument_list|,
name|LOCATOR
operator|.
name|getRegionLocation
argument_list|(
name|TABLE_NAME
argument_list|,
name|EMPTY_START_ROW
argument_list|,
name|RegionLocateType
operator|.
name|CURRENT
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|LOCATOR
operator|.
name|updateCachedLocation
argument_list|(
name|loc
argument_list|,
operator|new
name|NotServingRegionException
argument_list|()
argument_list|)
expr_stmt|;
name|assertLocEquals
argument_list|(
name|EMPTY_START_ROW
argument_list|,
name|EMPTY_END_ROW
argument_list|,
name|newServerName
argument_list|,
name|LOCATOR
operator|.
name|getRegionLocation
argument_list|(
name|TABLE_NAME
argument_list|,
name|EMPTY_START_ROW
argument_list|,
name|RegionLocateType
operator|.
name|CURRENT
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
end_function

begin_comment
comment|// usually locate after will return the same result, so we add a test to make it return different
end_comment

begin_comment
comment|// result.
end_comment

begin_function
annotation|@
name|Test
specifier|public
name|void
name|testLocateAfter
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|splitKey
init|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|row
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|splitKey
block|}
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|HRegionLocation
name|currentLoc
init|=
name|LOCATOR
operator|.
name|getRegionLocation
argument_list|(
name|TABLE_NAME
argument_list|,
name|row
argument_list|,
name|RegionLocateType
operator|.
name|CURRENT
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|ServerName
name|currentServerName
init|=
name|TEST_UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|assertLocEquals
argument_list|(
name|EMPTY_START_ROW
argument_list|,
name|splitKey
argument_list|,
name|currentServerName
argument_list|,
name|currentLoc
argument_list|)
expr_stmt|;
name|HRegionLocation
name|afterLoc
init|=
name|LOCATOR
operator|.
name|getRegionLocation
argument_list|(
name|TABLE_NAME
argument_list|,
name|row
argument_list|,
name|RegionLocateType
operator|.
name|AFTER
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|ServerName
name|afterServerName
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
argument_list|)
operator|.
name|filter
argument_list|(
name|rs
lambda|->
name|rs
operator|.
name|getOnlineRegions
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|stream
argument_list|()
operator|.
name|anyMatch
argument_list|(
name|r
lambda|->
name|Bytes
operator|.
name|equals
argument_list|(
name|splitKey
argument_list|,
name|r
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getStartKey
argument_list|()
argument_list|)
argument_list|)
argument_list|)
operator|.
name|findAny
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|assertLocEquals
argument_list|(
name|splitKey
argument_list|,
name|EMPTY_END_ROW
argument_list|,
name|afterServerName
argument_list|,
name|afterLoc
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|afterLoc
argument_list|,
name|LOCATOR
operator|.
name|getRegionLocation
argument_list|(
name|TABLE_NAME
argument_list|,
name|row
argument_list|,
name|RegionLocateType
operator|.
name|AFTER
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
end_function

begin_comment
comment|// For HBASE-17402
end_comment

begin_function
annotation|@
name|Test
specifier|public
name|void
name|testConcurrentLocate
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|createMultiRegionTable
argument_list|()
expr_stmt|;
name|byte
index|[]
index|[]
name|startKeys
init|=
name|getStartKeys
argument_list|()
decl_stmt|;
name|byte
index|[]
index|[]
name|endKeys
init|=
name|getEndKeys
argument_list|()
decl_stmt|;
name|ServerName
index|[]
name|serverNames
init|=
name|getLocations
argument_list|(
name|startKeys
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|LOCATOR
operator|.
name|clearCache
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|CompletableFuture
argument_list|<
name|HRegionLocation
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
literal|1000
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|n
lambda|->
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|n
argument_list|)
argument_list|)
operator|.
name|map
argument_list|(
name|s
lambda|->
name|Bytes
operator|.
name|toBytes
argument_list|(
name|s
argument_list|)
argument_list|)
operator|.
name|map
argument_list|(
name|r
lambda|->
name|LOCATOR
operator|.
name|getRegionLocation
argument_list|(
name|TABLE_NAME
argument_list|,
name|r
argument_list|,
name|RegionLocateType
operator|.
name|CURRENT
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|toList
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|1000
condition|;
name|j
operator|++
control|)
block|{
name|int
name|index
init|=
name|Math
operator|.
name|min
argument_list|(
literal|8
argument_list|,
name|j
operator|/
literal|111
argument_list|)
decl_stmt|;
name|assertLocEquals
argument_list|(
name|startKeys
index|[
name|index
index|]
argument_list|,
name|endKeys
index|[
name|index
index|]
argument_list|,
name|serverNames
index|[
name|index
index|]
argument_list|,
name|futures
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_function

unit|}
end_unit

