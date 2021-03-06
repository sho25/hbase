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
name|assertFalse
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
name|TimeUnit
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
name|regionserver
operator|.
name|HRegionServer
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
name|JVMClusterUtil
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
name|io
operator|.
name|IOUtils
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
name|Before
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
name|Rule
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|TestFlushFromClient
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
name|TestFlushFromClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestFlushFromClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|AsyncConnection
name|asyncConn
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|SPLITS
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"3"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"7"
argument_list|)
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|ROWS
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"4"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"8"
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|public
name|TableName
name|tableName
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|ROWS
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|asyncConn
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
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|IOUtils
operator|.
name|cleanup
argument_list|(
literal|null
argument_list|,
name|asyncConn
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|tableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|t
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|FAMILY
argument_list|,
name|SPLITS
argument_list|)
init|)
block|{
name|List
argument_list|<
name|Put
argument_list|>
name|puts
init|=
name|ROWS
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|r
lambda|->
operator|new
name|Put
argument_list|(
name|r
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
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|!=
literal|20
condition|;
operator|++
name|i
control|)
block|{
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|puts
operator|.
name|forEach
argument_list|(
name|p
lambda|->
name|p
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|value
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|t
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
block|}
name|assertFalse
argument_list|(
name|getRegionInfo
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|getRegionInfo
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|allMatch
argument_list|(
name|r
lambda|->
name|r
operator|.
name|getMemStoreDataSize
argument_list|()
operator|!=
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|TableDescriptor
name|htd
range|:
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|listTableDescriptors
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Tear down, remove table="
operator|+
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFlushTable
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|getRegionInfo
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|anyMatch
argument_list|(
name|r
lambda|->
name|r
operator|.
name|getMemStoreDataSize
argument_list|()
operator|!=
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAsyncFlushTable
parameter_list|()
throws|throws
name|Exception
block|{
name|AsyncAdmin
name|admin
init|=
name|asyncConn
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|getRegionInfo
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|anyMatch
argument_list|(
name|r
lambda|->
name|r
operator|.
name|getMemStoreDataSize
argument_list|()
operator|!=
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFlushRegion
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
init|)
block|{
for|for
control|(
name|HRegion
name|r
range|:
name|getRegionInfo
argument_list|()
control|)
block|{
name|admin
operator|.
name|flushRegion
argument_list|(
name|r
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
name|TimeUnit
operator|.
name|SECONDS
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|r
operator|.
name|getMemStoreDataSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAsyncFlushRegion
parameter_list|()
throws|throws
name|Exception
block|{
name|AsyncAdmin
name|admin
init|=
name|asyncConn
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegion
name|r
range|:
name|getRegionInfo
argument_list|()
control|)
block|{
name|admin
operator|.
name|flushRegion
argument_list|(
name|r
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|TimeUnit
operator|.
name|SECONDS
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|r
operator|.
name|getMemStoreDataSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFlushRegionServer
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
init|)
block|{
for|for
control|(
name|HRegionServer
name|rs
range|:
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getLiveRegionServerThreads
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
operator|::
name|getRegionServer
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
control|)
block|{
name|admin
operator|.
name|flushRegionServer
argument_list|(
name|rs
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|getRegionInfo
argument_list|(
name|rs
argument_list|)
operator|.
name|stream
argument_list|()
operator|.
name|anyMatch
argument_list|(
name|r
lambda|->
name|r
operator|.
name|getMemStoreDataSize
argument_list|()
operator|!=
literal|0
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
name|testAsyncFlushRegionServer
parameter_list|()
throws|throws
name|Exception
block|{
name|AsyncAdmin
name|admin
init|=
name|asyncConn
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionServer
name|rs
range|:
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getLiveRegionServerThreads
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
operator|::
name|getRegionServer
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
control|)
block|{
name|admin
operator|.
name|flushRegionServer
argument_list|(
name|rs
operator|.
name|getServerName
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|getRegionInfo
argument_list|(
name|rs
argument_list|)
operator|.
name|stream
argument_list|()
operator|.
name|anyMatch
argument_list|(
name|r
lambda|->
name|r
operator|.
name|getMemStoreDataSize
argument_list|()
operator|!=
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|List
argument_list|<
name|HRegion
argument_list|>
name|getRegionInfo
parameter_list|()
block|{
return|return
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getLiveRegionServerThreads
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
operator|::
name|getRegionServer
argument_list|)
operator|.
name|flatMap
argument_list|(
name|r
lambda|->
name|r
operator|.
name|getRegions
argument_list|()
operator|.
name|stream
argument_list|()
argument_list|)
operator|.
name|filter
argument_list|(
name|r
lambda|->
name|r
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getTableName
argument_list|()
operator|.
name|equals
argument_list|(
name|tableName
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
return|;
block|}
specifier|private
name|List
argument_list|<
name|HRegion
argument_list|>
name|getRegionInfo
parameter_list|(
name|HRegionServer
name|rs
parameter_list|)
block|{
return|return
name|rs
operator|.
name|getRegions
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|v
lambda|->
name|v
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getTableName
argument_list|()
operator|.
name|equals
argument_list|(
name|tableName
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
return|;
block|}
block|}
end_class

end_unit

