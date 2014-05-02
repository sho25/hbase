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
name|fail
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Set
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|master
operator|.
name|snapshot
operator|.
name|SnapshotManager
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|SnapshotDescription
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
name|ConstantSizeRegionSplitPolicy
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
name|snapshot
operator|.
name|SnapshotCreationException
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
name|snapshot
operator|.
name|SnapshotTestingUtils
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
name|FSUtils
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
operator|.
name|RegionServerThread
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
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * Test create/using/deleting snapshots from the client  *<p>  * This is an end-to-end test for the snapshot utility  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestSnapshotFromClient
block|{
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
name|TestSnapshotFromClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_RS
init|=
literal|2
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|STRING_TABLE_NAME
init|=
literal|"test"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TEST_FAM
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
specifier|final
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|STRING_TABLE_NAME
argument_list|)
decl_stmt|;
comment|/**    * Setup the config for the cluster    * @throws Exception on failure    */
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|setupConf
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
name|NUM_RS
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|setupConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// disable the ui
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionsever.info.port"
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|// change the flush size to a small amount, regulating number of store files
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hregion.memstore.flush.size"
argument_list|,
literal|25000
argument_list|)
expr_stmt|;
comment|// so make sure we get a compaction when doing a load, but keep around some
comment|// files in the store
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compaction.min"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compactionThreshold"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
comment|// block writes if we get to 12 store files
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.blockingStoreFiles"
argument_list|,
literal|12
argument_list|)
expr_stmt|;
comment|// Enable snapshot
name|conf
operator|.
name|setBoolean
argument_list|(
name|SnapshotManager
operator|.
name|HBASE_SNAPSHOT_ENABLED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_REGION_SPLIT_POLICY_KEY
argument_list|,
name|ConstantSizeRegionSplitPolicy
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|TEST_FAM
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
name|UTIL
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|deleteAllSnapshots
argument_list|(
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|deleteArchiveDirectory
argument_list|(
name|UTIL
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|cleanupTest
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
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
name|warn
argument_list|(
literal|"failure shutting down cluster"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test snapshotting not allowed hbase:meta and -ROOT-    * @throws Exception    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testMetaTablesSnapshot
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseAdmin
name|admin
init|=
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|byte
index|[]
name|snapshotName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"metaSnapshot"
argument_list|)
decl_stmt|;
try|try
block|{
name|admin
operator|.
name|snapshot
argument_list|(
name|snapshotName
argument_list|,
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"taking a snapshot of hbase:meta should not be allowed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// expected
block|}
block|}
comment|/**    * Test HBaseAdmin#deleteSnapshots(String) which deletes snapshots whose names match the parameter    *    * @throws Exception    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testSnapshotDeletionWithRegex
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseAdmin
name|admin
init|=
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
comment|// make sure we don't fail on listing snapshots
name|SnapshotTestingUtils
operator|.
name|assertNoSnapshots
argument_list|(
name|admin
argument_list|)
expr_stmt|;
comment|// put some stuff in the table
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|loadTable
argument_list|(
name|table
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|byte
index|[]
name|snapshot1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"TableSnapshot1"
argument_list|)
decl_stmt|;
name|admin
operator|.
name|snapshot
argument_list|(
name|snapshot1
argument_list|,
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Snapshot1 completed."
argument_list|)
expr_stmt|;
name|byte
index|[]
name|snapshot2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"TableSnapshot2"
argument_list|)
decl_stmt|;
name|admin
operator|.
name|snapshot
argument_list|(
name|snapshot2
argument_list|,
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Snapshot2 completed."
argument_list|)
expr_stmt|;
name|String
name|snapshot3
init|=
literal|"3rdTableSnapshot"
decl_stmt|;
name|admin
operator|.
name|snapshot
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|snapshot3
argument_list|)
argument_list|,
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|snapshot3
operator|+
literal|" completed."
argument_list|)
expr_stmt|;
comment|// delete the first two snapshots
name|admin
operator|.
name|deleteSnapshots
argument_list|(
literal|"TableSnapshot.*"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|SnapshotDescription
argument_list|>
name|snapshots
init|=
name|admin
operator|.
name|listSnapshots
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|snapshots
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|snapshots
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|,
name|snapshot3
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteSnapshot
argument_list|(
name|snapshot3
argument_list|)
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test snapshotting a table that is offline    * @throws Exception    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testOfflineTableSnapshot
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseAdmin
name|admin
init|=
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
comment|// make sure we don't fail on listing snapshots
name|SnapshotTestingUtils
operator|.
name|assertNoSnapshots
argument_list|(
name|admin
argument_list|)
expr_stmt|;
comment|// put some stuff in the table
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|loadTable
argument_list|(
name|table
argument_list|,
name|TEST_FAM
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// get the name of all the regionservers hosting the snapshotted table
name|Set
argument_list|<
name|String
argument_list|>
name|snapshotServers
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RegionServerThread
argument_list|>
name|servers
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getLiveRegionServerThreads
argument_list|()
decl_stmt|;
for|for
control|(
name|RegionServerThread
name|server
range|:
name|servers
control|)
block|{
if|if
condition|(
name|server
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getOnlineRegions
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|snapshotServers
operator|.
name|add
argument_list|(
name|server
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"FS state before disable:"
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
comment|// XXX if this is flakey, might want to consider using the async version and looping as
comment|// disableTable can succeed and still timeout.
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"FS state before snapshot:"
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
comment|// take a snapshot of the disabled table
name|byte
index|[]
name|snapshot
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"offlineTableSnapshot"
argument_list|)
decl_stmt|;
name|admin
operator|.
name|snapshot
argument_list|(
name|snapshot
argument_list|,
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Snapshot completed."
argument_list|)
expr_stmt|;
comment|// make sure we have the snapshot
name|List
argument_list|<
name|SnapshotDescription
argument_list|>
name|snapshots
init|=
name|SnapshotTestingUtils
operator|.
name|assertOneSnapshotThatMatches
argument_list|(
name|admin
argument_list|,
name|snapshot
argument_list|,
name|TABLE_NAME
argument_list|)
decl_stmt|;
comment|// make sure its a valid snapshot
name|FileSystem
name|fs
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|Path
name|rootDir
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getRootDir
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"FS state after snapshot:"
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|confirmSnapshotValid
argument_list|(
name|snapshots
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|TABLE_NAME
argument_list|,
name|TEST_FAM
argument_list|,
name|rootDir
argument_list|,
name|admin
argument_list|,
name|fs
argument_list|,
literal|false
argument_list|,
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
argument_list|,
name|snapshotServers
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteSnapshot
argument_list|(
name|snapshot
argument_list|)
expr_stmt|;
name|snapshots
operator|=
name|admin
operator|.
name|listSnapshots
argument_list|()
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|assertNoSnapshots
argument_list|(
name|admin
argument_list|)
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
name|testSnapshotFailsOnNonExistantTable
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseAdmin
name|admin
init|=
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
comment|// make sure we don't fail on listing snapshots
name|SnapshotTestingUtils
operator|.
name|assertNoSnapshots
argument_list|(
name|admin
argument_list|)
expr_stmt|;
name|String
name|tableName
init|=
literal|"_not_a_table"
decl_stmt|;
comment|// make sure the table doesn't exist
name|boolean
name|fail
init|=
literal|false
decl_stmt|;
do|do
block|{
try|try
block|{
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|fail
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Table:"
operator|+
name|tableName
operator|+
literal|" already exists, checking a new name"
argument_list|)
expr_stmt|;
name|tableName
operator|=
name|tableName
operator|+
literal|"!"
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableNotFoundException
name|e
parameter_list|)
block|{
name|fail
operator|=
literal|false
expr_stmt|;
block|}
block|}
do|while
condition|(
name|fail
condition|)
do|;
comment|// snapshot the non-existant table
try|try
block|{
name|admin
operator|.
name|snapshot
argument_list|(
literal|"fail"
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Snapshot succeeded even though there is not table."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SnapshotCreationException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Correctly failed to snapshot a non-existant table:"
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|testOfflineTableSnapshotWithEmptyRegions
parameter_list|()
throws|throws
name|Exception
block|{
comment|// test with an empty table with one region
name|HBaseAdmin
name|admin
init|=
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
comment|// make sure we don't fail on listing snapshots
name|SnapshotTestingUtils
operator|.
name|assertNoSnapshots
argument_list|(
name|admin
argument_list|)
expr_stmt|;
comment|// get the name of all the regionservers hosting the snapshotted table
name|Set
argument_list|<
name|String
argument_list|>
name|snapshotServers
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RegionServerThread
argument_list|>
name|servers
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getLiveRegionServerThreads
argument_list|()
decl_stmt|;
for|for
control|(
name|RegionServerThread
name|server
range|:
name|servers
control|)
block|{
if|if
condition|(
name|server
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getOnlineRegions
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|snapshotServers
operator|.
name|add
argument_list|(
name|server
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"FS state before disable:"
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"FS state before snapshot:"
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
comment|// take a snapshot of the disabled table
name|byte
index|[]
name|snapshot
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testOfflineTableSnapshotWithEmptyRegions"
argument_list|)
decl_stmt|;
name|admin
operator|.
name|snapshot
argument_list|(
name|snapshot
argument_list|,
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Snapshot completed."
argument_list|)
expr_stmt|;
comment|// make sure we have the snapshot
name|List
argument_list|<
name|SnapshotDescription
argument_list|>
name|snapshots
init|=
name|SnapshotTestingUtils
operator|.
name|assertOneSnapshotThatMatches
argument_list|(
name|admin
argument_list|,
name|snapshot
argument_list|,
name|TABLE_NAME
argument_list|)
decl_stmt|;
comment|// make sure its a valid snapshot
name|FileSystem
name|fs
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|Path
name|rootDir
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getRootDir
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"FS state after snapshot:"
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|emptyCfs
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|TEST_FAM
argument_list|)
decl_stmt|;
comment|// no file in the region
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|nonEmptyCfs
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|SnapshotTestingUtils
operator|.
name|confirmSnapshotValid
argument_list|(
name|snapshots
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|TABLE_NAME
argument_list|,
name|nonEmptyCfs
argument_list|,
name|emptyCfs
argument_list|,
name|rootDir
argument_list|,
name|admin
argument_list|,
name|fs
argument_list|,
literal|false
argument_list|,
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
argument_list|,
name|snapshotServers
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteSnapshot
argument_list|(
name|snapshot
argument_list|)
expr_stmt|;
name|snapshots
operator|=
name|admin
operator|.
name|listSnapshots
argument_list|()
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|assertNoSnapshots
argument_list|(
name|admin
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

