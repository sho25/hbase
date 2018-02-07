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
name|master
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
name|assertNotNull
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
name|ArrayList
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
name|MetaMockingUtil
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
name|MetaTableAccessor
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
name|client
operator|.
name|Admin
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
name|client
operator|.
name|Connection
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
name|client
operator|.
name|ConnectionFactory
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
name|client
operator|.
name|Get
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
name|client
operator|.
name|RegionInfo
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
name|client
operator|.
name|RegionLocator
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
name|client
operator|.
name|Result
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
name|client
operator|.
name|Table
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
name|master
operator|.
name|assignment
operator|.
name|AssignmentManager
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
name|MasterTests
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
name|PairOfSameType
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
name|MasterTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestCatalogJanitorInMemoryStates
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
name|TestCatalogJanitorInMemoryStates
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
name|TestCatalogJanitorInMemoryStates
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
specifier|final
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|protected
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
name|byte
index|[]
name|ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testRow"
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
literal|"testFamily"
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
literal|"testQualifier"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|VALUE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testValue"
argument_list|)
decl_stmt|;
comment|/**    * @throws java.lang.Exception    */
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
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
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
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test clearing a split parent from memory.    */
annotation|@
name|Test
specifier|public
name|void
name|testInMemoryParentCleanup
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
specifier|final
name|AssignmentManager
name|am
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
specifier|final
name|ServerManager
name|sm
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerManager
argument_list|()
decl_stmt|;
specifier|final
name|CatalogJanitor
name|janitor
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getCatalogJanitor
argument_list|()
decl_stmt|;
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|enableCatalogJanitor
argument_list|(
literal|false
argument_list|)
expr_stmt|;
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
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
argument_list|)
decl_stmt|;
name|int
name|rowCount
init|=
name|TEST_UTIL
operator|.
name|loadTable
argument_list|(
name|t
argument_list|,
name|FAMILY
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|RegionLocator
name|locator
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|allRegionLocations
init|=
name|locator
operator|.
name|getAllRegionLocations
argument_list|()
decl_stmt|;
comment|// We need to create a valid split with daughter regions
name|HRegionLocation
name|parent
init|=
name|allRegionLocations
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|daughters
init|=
name|splitRegion
argument_list|(
name|parent
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Parent region: "
operator|+
name|parent
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Daughter regions: "
operator|+
name|daughters
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"Should have found daughter regions for "
operator|+
name|parent
argument_list|,
name|daughters
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Parent region should exist in RegionStates"
argument_list|,
name|am
operator|.
name|getRegionStates
argument_list|()
operator|.
name|isRegionInRegionStates
argument_list|(
name|parent
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Parent region should exist in ServerManager"
argument_list|,
name|sm
operator|.
name|isRegionInServerManagerStates
argument_list|(
name|parent
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// clean the parent
name|Result
name|r
init|=
name|MetaMockingUtil
operator|.
name|getMetaTableRowResult
argument_list|(
name|parent
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
literal|null
argument_list|,
name|daughters
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|daughters
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
decl_stmt|;
name|janitor
operator|.
name|cleanParent
argument_list|(
name|parent
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|r
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Parent region should have been removed from RegionStates"
argument_list|,
name|am
operator|.
name|getRegionStates
argument_list|()
operator|.
name|isRegionInRegionStates
argument_list|(
name|parent
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Parent region should have been removed from ServerManager"
argument_list|,
name|sm
operator|.
name|isRegionInServerManagerStates
argument_list|(
name|parent
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/*  * Splits a region  * @param t Region to split.  * @return List of region locations  * @throws IOException, InterruptedException  */
specifier|private
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|splitRegion
parameter_list|(
specifier|final
name|RegionInfo
name|r
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|locations
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// Split this table in two.
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|Connection
name|connection
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
decl_stmt|;
name|admin
operator|.
name|splitRegion
argument_list|(
name|r
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
name|PairOfSameType
argument_list|<
name|RegionInfo
argument_list|>
name|regions
init|=
name|waitOnDaughters
argument_list|(
name|r
argument_list|)
decl_stmt|;
if|if
condition|(
name|regions
operator|!=
literal|null
condition|)
block|{
try|try
init|(
name|RegionLocator
name|rl
init|=
name|connection
operator|.
name|getRegionLocator
argument_list|(
name|r
operator|.
name|getTable
argument_list|()
argument_list|)
init|)
block|{
name|locations
operator|.
name|add
argument_list|(
name|rl
operator|.
name|getRegionLocation
argument_list|(
name|regions
operator|.
name|getFirst
argument_list|()
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|locations
operator|.
name|add
argument_list|(
name|rl
operator|.
name|getRegionLocation
argument_list|(
name|regions
operator|.
name|getSecond
argument_list|()
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|locations
return|;
block|}
return|return
name|locations
return|;
block|}
comment|/*    * Wait on region split. May return because we waited long enough on the split    * and it didn't happen.  Caller should check.    * @param r    * @return Daughter regions; caller needs to check table actually split.    */
specifier|private
name|PairOfSameType
argument_list|<
name|RegionInfo
argument_list|>
name|waitOnDaughters
parameter_list|(
specifier|final
name|RegionInfo
name|r
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|PairOfSameType
argument_list|<
name|RegionInfo
argument_list|>
name|pair
init|=
literal|null
decl_stmt|;
try|try
init|(
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
init|;
name|Table
name|metaTable
operator|=
name|conn
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
init|)
block|{
name|Result
name|result
init|=
literal|null
decl_stmt|;
name|RegionInfo
name|region
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|)
operator|<
literal|60000
condition|)
block|{
name|result
operator|=
name|metaTable
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|r
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
break|break;
block|}
name|region
operator|=
name|MetaTableAccessor
operator|.
name|getRegionInfo
argument_list|(
name|result
argument_list|)
expr_stmt|;
if|if
condition|(
name|region
operator|.
name|isSplitParent
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|region
operator|.
name|toString
argument_list|()
operator|+
literal|" IS a parent!"
argument_list|)
expr_stmt|;
name|pair
operator|=
name|MetaTableAccessor
operator|.
name|getDaughterRegions
argument_list|(
name|result
argument_list|)
expr_stmt|;
break|break;
block|}
name|Threads
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|pair
operator|.
name|getFirst
argument_list|()
operator|==
literal|null
operator|||
name|pair
operator|.
name|getSecond
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to get daughters, for parent region: "
operator|+
name|r
argument_list|)
throw|;
block|}
return|return
name|pair
return|;
block|}
block|}
block|}
end_class

end_unit

