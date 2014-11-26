begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|assertTrue
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
name|fail
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|HColumnDescriptor
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
name|HConstants
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
name|HTableDescriptor
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
name|MiniHBaseCluster
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
name|PleaseHoldException
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
name|UnknownRegionException
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
name|HTable
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
name|TableState
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
name|Pair
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
name|util
operator|.
name|StringUtils
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Joiner
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
name|TestMaster
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
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestMaster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|TABLENAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestMaster"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILYNAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Admin
name|admin
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeAllTests
parameter_list|()
throws|throws
name|Exception
block|{
comment|// we will retry operations when PleaseHoldException is thrown
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|3
argument_list|)
expr_stmt|;
comment|// Start a cluster of two regionservers.
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|admin
operator|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterAllTests
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
annotation|@
name|Test
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
name|void
name|testMasterOpsWhileSplitting
parameter_list|()
throws|throws
name|Exception
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|HMaster
name|m
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|HTable
name|ht
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLENAME
argument_list|,
name|FAMILYNAME
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|m
operator|.
name|assignmentManager
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|isTableState
argument_list|(
name|TABLENAME
argument_list|,
name|TableState
operator|.
name|State
operator|.
name|ENABLED
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|loadTable
argument_list|(
name|ht
argument_list|,
name|FAMILYNAME
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|ht
operator|.
name|close
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
argument_list|>
name|tableRegions
init|=
name|MetaTableAccessor
operator|.
name|getTableRegionsAndLocations
argument_list|(
name|m
operator|.
name|getConnection
argument_list|()
argument_list|,
name|TABLENAME
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Regions after load: "
operator|+
name|Joiner
operator|.
name|on
argument_list|(
literal|','
argument_list|)
operator|.
name|join
argument_list|(
name|tableRegions
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tableRegions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|tableRegions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFirst
argument_list|()
operator|.
name|getStartKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|,
name|tableRegions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFirst
argument_list|()
operator|.
name|getEndKey
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now trigger a split and stop when the split is in progress
name|LOG
operator|.
name|info
argument_list|(
literal|"Splitting table"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|split
argument_list|(
name|TABLENAME
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for split result to be about to open"
argument_list|)
expr_stmt|;
name|RegionStates
name|regionStates
init|=
name|m
operator|.
name|assignmentManager
operator|.
name|getRegionStates
argument_list|()
decl_stmt|;
while|while
condition|(
name|regionStates
operator|.
name|getRegionsOfTable
argument_list|(
name|TABLENAME
argument_list|)
operator|.
name|size
argument_list|()
operator|<=
literal|1
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Making sure we can call getTableRegions while opening"
argument_list|)
expr_stmt|;
name|tableRegions
operator|=
name|MetaTableAccessor
operator|.
name|getTableRegionsAndLocations
argument_list|(
name|m
operator|.
name|getConnection
argument_list|()
argument_list|,
name|TABLENAME
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Regions: "
operator|+
name|Joiner
operator|.
name|on
argument_list|(
literal|','
argument_list|)
operator|.
name|join
argument_list|(
name|tableRegions
argument_list|)
argument_list|)
expr_stmt|;
comment|// We have three regions because one is split-in-progress
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|tableRegions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Making sure we can call getTableRegionClosest while opening"
argument_list|)
expr_stmt|;
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|pair
init|=
name|m
operator|.
name|getTableRegionForRow
argument_list|(
name|TABLENAME
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cde"
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Result is: "
operator|+
name|pair
argument_list|)
expr_stmt|;
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|tableRegionFromName
init|=
name|MetaTableAccessor
operator|.
name|getRegion
argument_list|(
name|m
operator|.
name|getConnection
argument_list|()
argument_list|,
name|pair
operator|.
name|getFirst
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|tableRegionFromName
operator|.
name|getFirst
argument_list|()
argument_list|,
name|pair
operator|.
name|getFirst
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMoveRegionWhenNotInitialized
parameter_list|()
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|HMaster
name|m
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
try|try
block|{
name|m
operator|.
name|initialized
operator|=
literal|false
expr_stmt|;
comment|// fake it, set back later
name|HRegionInfo
name|meta
init|=
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
decl_stmt|;
name|m
operator|.
name|move
argument_list|(
name|meta
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Region should not be moved since master is not initialized"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|ioe
operator|instanceof
name|PleaseHoldException
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|m
operator|.
name|initialized
operator|=
literal|true
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMoveThrowsUnknownRegionException
parameter_list|()
throws|throws
name|IOException
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testMoveThrowsUnknownRegionException"
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|,
literal|null
argument_list|)
expr_stmt|;
try|try
block|{
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Z"
argument_list|)
argument_list|)
decl_stmt|;
name|admin
operator|.
name|move
argument_list|(
name|hri
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Region should not be moved since it is fake"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|ioe
operator|instanceof
name|UnknownRegionException
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMoveThrowsPleaseHoldException
parameter_list|()
throws|throws
name|IOException
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testMoveThrowsPleaseHoldException"
argument_list|)
decl_stmt|;
name|HMaster
name|master
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|,
literal|null
argument_list|)
expr_stmt|;
try|try
block|{
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|tableRegions
init|=
name|admin
operator|.
name|getTableRegions
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|master
operator|.
name|initialized
operator|=
literal|false
expr_stmt|;
comment|// fake it, set back later
name|admin
operator|.
name|move
argument_list|(
name|tableRegions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Region should not be moved since master is not initialized"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|ioe
argument_list|)
operator|.
name|contains
argument_list|(
literal|"PleaseHoldException"
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|master
operator|.
name|initialized
operator|=
literal|true
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

