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
name|client
package|;
end_package

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
name|concurrent
operator|.
name|CountDownLatch
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
name|Predicate
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
name|collect
operator|.
name|Iterables
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
name|collect
operator|.
name|Lists
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
name|Delete
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
name|ResultScanner
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
name|HMaster
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
name|BaseMasterObserver
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
name|CoprocessorHost
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
name|MasterCoprocessorEnvironment
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
name|JVMClusterUtil
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
name|Before
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
name|TestEnableTable
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
name|TestEnableTable
operator|.
name|class
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
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
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
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|MasterSyncObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
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
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testEnableTableWithNoRegionServers
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testEnableTableWithNoRegionServers"
argument_list|)
decl_stmt|;
specifier|final
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
specifier|final
name|HMaster
name|m
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
specifier|final
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
specifier|final
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILYNAME
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableDisabled
argument_list|(
name|tableName
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableEnabled
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// disable once more
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilNoRegionsInTransition
argument_list|(
literal|60000
argument_list|)
expr_stmt|;
comment|// now stop region servers
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|rs
init|=
name|cluster
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|rs
operator|.
name|getRegionServer
argument_list|()
operator|.
name|stop
argument_list|(
literal|"stop"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitForRegionServerToStop
argument_list|(
name|rs
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Now enabling table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|admin
operator|.
name|isTableEnabled
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|rs2
init|=
name|cluster
operator|.
name|startRegionServer
argument_list|()
decl_stmt|;
name|cluster
operator|.
name|waitForRegionServerToStart
argument_list|(
name|rs2
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|.
name|getHostname
argument_list|()
argument_list|,
name|rs2
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|,
literal|60000
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableRegions
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|regions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HRegionInfo
name|region
range|:
name|regions
control|)
block|{
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|assign
argument_list|(
name|region
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Waiting for table assigned "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|onlineRegions
init|=
name|admin
operator|.
name|getOnlineRegions
argument_list|(
name|rs2
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|HRegionInfo
argument_list|>
name|tableRegions
init|=
name|filterTableRegions
argument_list|(
name|tableName
argument_list|,
name|onlineRegions
argument_list|)
decl_stmt|;
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
block|}
specifier|private
name|ArrayList
argument_list|<
name|HRegionInfo
argument_list|>
name|filterTableRegions
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|onlineRegions
parameter_list|)
block|{
return|return
name|Lists
operator|.
name|newArrayList
argument_list|(
name|Iterables
operator|.
name|filter
argument_list|(
name|onlineRegions
argument_list|,
operator|new
name|Predicate
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|HRegionInfo
name|input
parameter_list|)
block|{
return|return
name|input
operator|.
name|getTable
argument_list|()
operator|.
name|equals
argument_list|(
name|tableName
argument_list|)
return|;
block|}
block|}
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * We were only clearing rows that had a hregioninfo column in hbase:meta.  Mangled rows that    * were missing the hregioninfo because of error were being left behind messing up any    * subsequent table made with the same name. HBASE-12980    * @throws IOException    * @throws InterruptedException    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testDeleteForSureClearsAllTableRowsFromMeta
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testDeleteForSureClearsAllTableRowsFromMeta"
argument_list|)
decl_stmt|;
specifier|final
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
specifier|final
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILYNAME
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|createTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|desc
argument_list|,
name|HBaseTestingUtility
operator|.
name|KEYS_FOR_HBA_CREATE_TABLE
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Got an exception while creating "
operator|+
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|// Now I have a nice table, mangle it by removing the HConstants.REGIONINFO_QUALIFIER_STR
comment|// content from a few of the rows.
try|try
init|(
name|Table
name|metaTable
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
init|)
block|{
try|try
init|(
name|ResultScanner
name|scanner
init|=
name|metaTable
operator|.
name|getScanner
argument_list|(
name|MetaTableAccessor
operator|.
name|getScanForTableName
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tableName
argument_list|)
argument_list|)
init|)
block|{
for|for
control|(
name|Result
name|result
range|:
name|scanner
control|)
block|{
comment|// Just delete one row.
name|Delete
name|d
init|=
operator|new
name|Delete
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|d
operator|.
name|addColumn
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Mangled: "
operator|+
name|d
argument_list|)
expr_stmt|;
name|metaTable
operator|.
name|delete
argument_list|(
name|d
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableDisabled
argument_list|(
name|tableName
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Rely on the coprocessor based latch to make the operation synchronous.
try|try
block|{
name|deleteTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Got an exception while deleting "
operator|+
name|tableName
argument_list|)
expr_stmt|;
block|}
name|int
name|rowCount
init|=
literal|0
decl_stmt|;
try|try
init|(
name|ResultScanner
name|scanner
init|=
name|metaTable
operator|.
name|getScanner
argument_list|(
name|MetaTableAccessor
operator|.
name|getScanForTableName
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tableName
argument_list|)
argument_list|)
init|)
block|{
for|for
control|(
name|Result
name|result
range|:
name|scanner
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Found when none expected: "
operator|+
name|result
argument_list|)
expr_stmt|;
name|rowCount
operator|++
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rowCount
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|MasterSyncObserver
extends|extends
name|BaseMasterObserver
block|{
specifier|volatile
name|CountDownLatch
name|tableCreationLatch
init|=
literal|null
decl_stmt|;
specifier|volatile
name|CountDownLatch
name|tableDeletionLatch
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|postCompletedCreateTableAction
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|HTableDescriptor
name|desc
parameter_list|,
specifier|final
name|HRegionInfo
index|[]
name|regions
parameter_list|)
throws|throws
name|IOException
block|{
comment|// the AccessController test, some times calls only and directly the
comment|// postCompletedCreateTableAction()
if|if
condition|(
name|tableCreationLatch
operator|!=
literal|null
condition|)
block|{
name|tableCreationLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|postCompletedDeleteTableAction
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
comment|// the AccessController test, some times calls only and directly the postDeleteTableHandler()
if|if
condition|(
name|tableDeletionLatch
operator|!=
literal|null
condition|)
block|{
name|tableDeletionLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|void
name|createTable
parameter_list|(
name|HBaseTestingUtility
name|testUtil
parameter_list|,
name|HTableDescriptor
name|htd
parameter_list|,
name|byte
index|[]
index|[]
name|splitKeys
parameter_list|)
throws|throws
name|Exception
block|{
comment|// NOTE: We need a latch because admin is not sync,
comment|// so the postOp coprocessor method may be called after the admin operation returned.
name|MasterSyncObserver
name|observer
init|=
operator|(
name|MasterSyncObserver
operator|)
name|testUtil
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|MasterSyncObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|observer
operator|.
name|tableCreationLatch
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Admin
name|admin
init|=
name|testUtil
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
if|if
condition|(
name|splitKeys
operator|!=
literal|null
condition|)
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|,
name|splitKeys
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
block|}
name|observer
operator|.
name|tableCreationLatch
operator|.
name|await
argument_list|()
expr_stmt|;
name|observer
operator|.
name|tableCreationLatch
operator|=
literal|null
expr_stmt|;
name|testUtil
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|deleteTable
parameter_list|(
name|HBaseTestingUtility
name|testUtil
parameter_list|,
name|TableName
name|tableName
parameter_list|)
throws|throws
name|Exception
block|{
comment|// NOTE: We need a latch because admin is not sync,
comment|// so the postOp coprocessor method may be called after the admin operation returned.
name|MasterSyncObserver
name|observer
init|=
operator|(
name|MasterSyncObserver
operator|)
name|testUtil
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|MasterSyncObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|observer
operator|.
name|tableDeletionLatch
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Admin
name|admin
init|=
name|testUtil
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
try|try
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table: "
operator|+
name|tableName
operator|+
literal|" already disabled, so just deleting it."
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|observer
operator|.
name|tableDeletionLatch
operator|.
name|await
argument_list|()
expr_stmt|;
name|observer
operator|.
name|tableDeletionLatch
operator|=
literal|null
expr_stmt|;
block|}
block|}
end_class

end_unit

