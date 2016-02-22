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
name|Assert
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
name|Ignore
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
name|Ignore
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
comment|/**  * Test to verify that the cloned table is independent of the table from which it was cloned  */
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
name|TestSnapshotCloneIndependence
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
name|TestSnapshotCloneIndependence
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
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
name|String
name|TEST_FAM_STR
init|=
literal|"fam"
decl_stmt|;
specifier|protected
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
name|TEST_FAM_STR
argument_list|)
decl_stmt|;
specifier|protected
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
specifier|private
specifier|static
specifier|final
name|int
name|CLEANER_INTERVAL
init|=
literal|100
decl_stmt|;
comment|/**    * Setup the config for the cluster and start it    * @throws Exception on fOailure    */
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
specifier|static
name|void
name|setupConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// Up the handlers; this test needs more than usual.
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|REGION_SERVER_HIGH_PRIORITY_HANDLER_COUNT
argument_list|,
literal|15
argument_list|)
expr_stmt|;
comment|// enable snapshot support
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
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.info.port"
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
comment|// so make sure we get a compaction when doing a load, but keep around
comment|// some files in the store
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
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.msginterval"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.master.enabletable.roundrobin"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// Avoid potentially aggressive splitting which would cause snapshot to fail
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
comment|// Execute cleaner frequently to induce failures
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.cleaner.interval"
argument_list|,
name|CLEANER_INTERVAL
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.hfilecleaner.plugins.snapshot.period"
argument_list|,
name|CLEANER_INTERVAL
argument_list|)
expr_stmt|;
comment|// Effectively disable TimeToLiveHFileCleaner. Don't want to fully disable it because that
comment|// will even trigger races between creating the directory containing back references and
comment|// the back reference itself.
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.hfilecleaner.ttl"
argument_list|,
name|CLEANER_INTERVAL
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
comment|/**    * Verify that adding data to the cloned table will not affect the original, and vice-versa when    * it is taken as an online snapshot.    */
annotation|@
name|Ignore
argument_list|(
literal|"Flakey. Fix"
argument_list|)
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testOnlineSnapshotAppendIndependent
parameter_list|()
throws|throws
name|Exception
block|{
name|runTestSnapshotAppendIndependent
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify that adding data to the cloned table will not affect the original, and vice-versa when    * it is taken as an offline snapshot.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
annotation|@
name|Ignore
specifier|public
name|void
name|testOfflineSnapshotAppendIndependent
parameter_list|()
throws|throws
name|Exception
block|{
name|runTestSnapshotAppendIndependent
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify that adding metadata to the cloned table will not affect the original, and vice-versa    * when it is taken as an online snapshot.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testOnlineSnapshotMetadataChangesIndependent
parameter_list|()
throws|throws
name|Exception
block|{
name|runTestSnapshotMetadataChangesIndependent
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify that adding netadata to the cloned table will not affect the original, and vice-versa    * when is taken as an online snapshot.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
annotation|@
name|Ignore
specifier|public
name|void
name|testOfflineSnapshotMetadataChangesIndependent
parameter_list|()
throws|throws
name|Exception
block|{
name|runTestSnapshotMetadataChangesIndependent
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify that region operations, in this case splitting a region, are independent between the    * cloned table and the original.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
annotation|@
name|Ignore
specifier|public
name|void
name|testOfflineSnapshotRegionOperationsIndependent
parameter_list|()
throws|throws
name|Exception
block|{
name|runTestRegionOperationsIndependent
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify that region operations, in this case splitting a region, are independent between the    * cloned table and the original.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testOnlineSnapshotRegionOperationsIndependent
parameter_list|()
throws|throws
name|Exception
block|{
name|runTestRegionOperationsIndependent
argument_list|(
literal|true
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
annotation|@
name|Ignore
specifier|public
name|void
name|testOfflineSnapshotDeleteIndependent
parameter_list|()
throws|throws
name|Exception
block|{
name|runTestSnapshotDeleteIndependent
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
argument_list|(
literal|"Flakey test"
argument_list|)
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testOnlineSnapshotDeleteIndependent
parameter_list|()
throws|throws
name|Exception
block|{
name|runTestSnapshotDeleteIndependent
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|waitOnSplit
parameter_list|(
name|Connection
name|c
parameter_list|,
specifier|final
name|Table
name|t
parameter_list|,
name|int
name|originalCount
parameter_list|)
throws|throws
name|Exception
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
literal|200
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|500
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// Restore the interrupted status
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
try|try
init|(
name|RegionLocator
name|locator
init|=
name|c
operator|.
name|getRegionLocator
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|)
init|)
block|{
if|if
condition|(
name|locator
operator|.
name|getAllRegionLocations
argument_list|()
operator|.
name|size
argument_list|()
operator|>
name|originalCount
condition|)
block|{
return|return;
block|}
block|}
block|}
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Split did not increase the number of regions"
argument_list|)
throw|;
block|}
comment|/*    * Take a snapshot of a table, add data, and verify that this only    * affects one table    * @param online - Whether the table is online or not during the snapshot    */
specifier|private
name|void
name|runTestSnapshotAppendIndependent
parameter_list|(
name|boolean
name|online
parameter_list|)
throws|throws
name|Exception
block|{
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
name|Admin
name|admin
init|=
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
specifier|final
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|final
name|TableName
name|localTableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|STRING_TABLE_NAME
operator|+
name|startTime
argument_list|)
decl_stmt|;
try|try
init|(
name|Table
name|original
init|=
name|createTable
argument_list|(
name|localTableName
argument_list|,
name|TEST_FAM
argument_list|)
init|)
block|{
name|loadData
argument_list|(
name|original
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
specifier|final
name|int
name|origTableRowCount
init|=
name|countRows
argument_list|(
name|original
argument_list|)
decl_stmt|;
comment|// Take a snapshot
specifier|final
name|String
name|snapshotNameAsString
init|=
literal|"snapshot_"
operator|+
name|localTableName
decl_stmt|;
name|byte
index|[]
name|snapshotName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|snapshotNameAsString
argument_list|)
decl_stmt|;
name|SnapshotTestingUtils
operator|.
name|createSnapshotAndValidate
argument_list|(
name|admin
argument_list|,
name|localTableName
argument_list|,
name|TEST_FAM_STR
argument_list|,
name|snapshotNameAsString
argument_list|,
name|rootDir
argument_list|,
name|fs
argument_list|,
name|online
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|online
condition|)
block|{
name|tryDisable
argument_list|(
name|admin
argument_list|,
name|localTableName
argument_list|)
expr_stmt|;
block|}
name|TableName
name|cloneTableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test-clone-"
operator|+
name|localTableName
argument_list|)
decl_stmt|;
name|admin
operator|.
name|cloneSnapshot
argument_list|(
name|snapshotName
argument_list|,
name|cloneTableName
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|clonedTable
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|cloneTableName
argument_list|)
init|)
block|{
comment|// Make sure that all the regions are available before starting
name|UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|cloneTableName
argument_list|)
expr_stmt|;
specifier|final
name|int
name|clonedTableRowCount
init|=
name|countRows
argument_list|(
name|clonedTable
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"The line counts of original and cloned tables do not match after clone. "
argument_list|,
name|origTableRowCount
argument_list|,
name|clonedTableRowCount
argument_list|)
expr_stmt|;
comment|// Attempt to add data to the test
specifier|final
name|String
name|rowKey
init|=
literal|"new-row-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rowKey
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|TEST_FAM
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"someQualifier"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"someString"
argument_list|)
argument_list|)
expr_stmt|;
name|original
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// Verify that it is not present in the original table
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"The row count of the original table was not modified by the put"
argument_list|,
name|origTableRowCount
operator|+
literal|1
argument_list|,
name|countRows
argument_list|(
name|original
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"The row count of the cloned table changed as a result of addition to the original"
argument_list|,
name|clonedTableRowCount
argument_list|,
name|countRows
argument_list|(
name|clonedTable
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rowKey
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|TEST_FAM
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"someQualifier"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"someString"
argument_list|)
argument_list|)
expr_stmt|;
name|clonedTable
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// Verify that the new family is not in the restored table's description
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"The row count of the original table was modified by the put to the clone"
argument_list|,
name|origTableRowCount
operator|+
literal|1
argument_list|,
name|countRows
argument_list|(
name|original
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"The row count of the cloned table was not modified by the put"
argument_list|,
name|clonedTableRowCount
operator|+
literal|1
argument_list|,
name|countRows
argument_list|(
name|clonedTable
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/*    * Take a snapshot of a table, do a split, and verify that this only affects one table    * @param online - Whether the table is online or not during the snapshot    */
specifier|private
name|void
name|runTestRegionOperationsIndependent
parameter_list|(
name|boolean
name|online
parameter_list|)
throws|throws
name|Exception
block|{
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
comment|// Create a table
name|Admin
name|admin
init|=
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
specifier|final
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|final
name|TableName
name|localTableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|STRING_TABLE_NAME
operator|+
name|startTime
argument_list|)
decl_stmt|;
name|Table
name|original
init|=
name|createTable
argument_list|(
name|localTableName
argument_list|,
name|TEST_FAM
argument_list|)
decl_stmt|;
name|loadData
argument_list|(
name|original
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
specifier|final
name|int
name|loadedTableCount
init|=
name|countRows
argument_list|(
name|original
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Original table has: "
operator|+
name|loadedTableCount
operator|+
literal|" rows"
argument_list|)
expr_stmt|;
specifier|final
name|String
name|snapshotNameAsString
init|=
literal|"snapshot_"
operator|+
name|localTableName
decl_stmt|;
comment|// Create a snapshot
name|SnapshotTestingUtils
operator|.
name|createSnapshotAndValidate
argument_list|(
name|admin
argument_list|,
name|localTableName
argument_list|,
name|TEST_FAM_STR
argument_list|,
name|snapshotNameAsString
argument_list|,
name|rootDir
argument_list|,
name|fs
argument_list|,
name|online
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|online
condition|)
block|{
name|tryDisable
argument_list|(
name|admin
argument_list|,
name|localTableName
argument_list|)
expr_stmt|;
block|}
name|TableName
name|cloneTableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test-clone-"
operator|+
name|localTableName
argument_list|)
decl_stmt|;
comment|// Clone the snapshot
name|byte
index|[]
name|snapshotName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|snapshotNameAsString
argument_list|)
decl_stmt|;
name|admin
operator|.
name|cloneSnapshot
argument_list|(
name|snapshotName
argument_list|,
name|cloneTableName
argument_list|)
expr_stmt|;
comment|// Verify that region information is the same pre-split
operator|(
operator|(
name|ClusterConnection
operator|)
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|)
operator|.
name|clearRegionCache
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|originalTableHRegions
init|=
name|admin
operator|.
name|getTableRegions
argument_list|(
name|localTableName
argument_list|)
decl_stmt|;
specifier|final
name|int
name|originalRegionCount
init|=
name|originalTableHRegions
operator|.
name|size
argument_list|()
decl_stmt|;
specifier|final
name|int
name|cloneTableRegionCount
init|=
name|admin
operator|.
name|getTableRegions
argument_list|(
name|cloneTableName
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"The number of regions in the cloned table is different than in the original table."
argument_list|,
name|originalRegionCount
argument_list|,
name|cloneTableRegionCount
argument_list|)
expr_stmt|;
comment|// Split a region on the parent table
name|admin
operator|.
name|splitRegion
argument_list|(
name|originalTableHRegions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
name|waitOnSplit
argument_list|(
name|UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|original
argument_list|,
name|originalRegionCount
argument_list|)
expr_stmt|;
comment|// Verify that the cloned table region is not split
specifier|final
name|int
name|cloneTableRegionCount2
init|=
name|admin
operator|.
name|getTableRegions
argument_list|(
name|cloneTableName
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"The number of regions in the cloned table changed though none of its regions were split."
argument_list|,
name|cloneTableRegionCount
argument_list|,
name|cloneTableRegionCount2
argument_list|)
expr_stmt|;
block|}
comment|/*    * Take a snapshot of a table, add metadata, and verify that this only    * affects one table    * @param online - Whether the table is online or not during the snapshot    */
specifier|private
name|void
name|runTestSnapshotMetadataChangesIndependent
parameter_list|(
name|boolean
name|online
parameter_list|)
throws|throws
name|Exception
block|{
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
comment|// Create a table
name|Admin
name|admin
init|=
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
specifier|final
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|final
name|TableName
name|localTableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|STRING_TABLE_NAME
operator|+
name|startTime
argument_list|)
decl_stmt|;
name|Table
name|original
init|=
name|createTable
argument_list|(
name|localTableName
argument_list|,
name|TEST_FAM
argument_list|)
decl_stmt|;
name|loadData
argument_list|(
name|original
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
specifier|final
name|String
name|snapshotNameAsString
init|=
literal|"snapshot_"
operator|+
name|localTableName
decl_stmt|;
comment|// Create a snapshot
name|SnapshotTestingUtils
operator|.
name|createSnapshotAndValidate
argument_list|(
name|admin
argument_list|,
name|localTableName
argument_list|,
name|TEST_FAM_STR
argument_list|,
name|snapshotNameAsString
argument_list|,
name|rootDir
argument_list|,
name|fs
argument_list|,
name|online
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|online
condition|)
block|{
name|tryDisable
argument_list|(
name|admin
argument_list|,
name|localTableName
argument_list|)
expr_stmt|;
block|}
name|TableName
name|cloneTableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test-clone-"
operator|+
name|localTableName
argument_list|)
decl_stmt|;
comment|// Clone the snapshot
name|byte
index|[]
name|snapshotName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|snapshotNameAsString
argument_list|)
decl_stmt|;
name|admin
operator|.
name|cloneSnapshot
argument_list|(
name|snapshotName
argument_list|,
name|cloneTableName
argument_list|)
expr_stmt|;
comment|// Add a new column family to the original table
name|byte
index|[]
name|TEST_FAM_2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam2"
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAM_2
argument_list|)
decl_stmt|;
name|tryDisable
argument_list|(
name|admin
argument_list|,
name|localTableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|addColumnFamily
argument_list|(
name|localTableName
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
comment|// Verify that it is not in the snapshot
name|admin
operator|.
name|enableTable
argument_list|(
name|localTableName
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|localTableName
argument_list|)
expr_stmt|;
comment|// get a description of the cloned table
comment|// get a list of its families
comment|// assert that the family is there
name|HTableDescriptor
name|originalTableDescriptor
init|=
name|original
operator|.
name|getTableDescriptor
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|clonedTableDescriptor
init|=
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|cloneTableName
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"The original family was not found. There is something wrong. "
argument_list|,
name|originalTableDescriptor
operator|.
name|hasFamily
argument_list|(
name|TEST_FAM
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"The original family was not found in the clone. There is something wrong. "
argument_list|,
name|clonedTableDescriptor
operator|.
name|hasFamily
argument_list|(
name|TEST_FAM
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"The new family was not found. "
argument_list|,
name|originalTableDescriptor
operator|.
name|hasFamily
argument_list|(
name|TEST_FAM_2
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"The new family was not found. "
argument_list|,
operator|!
name|clonedTableDescriptor
operator|.
name|hasFamily
argument_list|(
name|TEST_FAM_2
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|tryDisable
parameter_list|(
name|Admin
name|admin
parameter_list|,
name|TableName
name|localTableName
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|offlineRetry
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|offlineRetry
operator|<
literal|5
operator|&&
name|admin
operator|.
name|isTableEnabled
argument_list|(
name|localTableName
argument_list|)
condition|)
block|{
try|try
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|localTableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error disabling the table"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
name|offlineRetry
operator|++
expr_stmt|;
block|}
block|}
comment|/*    * Take a snapshot of a table, add data, and verify that deleting the snapshot does not affect    * either table.    * @param online - Whether the table is online or not during the snapshot    */
specifier|private
name|void
name|runTestSnapshotDeleteIndependent
parameter_list|(
name|boolean
name|online
parameter_list|)
throws|throws
name|Exception
block|{
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
specifier|final
name|Admin
name|admin
init|=
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
specifier|final
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|final
name|TableName
name|localTableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|STRING_TABLE_NAME
operator|+
name|startTime
argument_list|)
decl_stmt|;
try|try
init|(
name|Table
name|original
init|=
name|createTable
argument_list|(
name|localTableName
argument_list|,
name|TEST_FAM
argument_list|)
init|)
block|{
name|loadData
argument_list|(
name|original
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
block|}
comment|// Take a snapshot
specifier|final
name|String
name|snapshotNameAsString
init|=
literal|"snapshot_"
operator|+
name|localTableName
decl_stmt|;
name|byte
index|[]
name|snapshotName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|snapshotNameAsString
argument_list|)
decl_stmt|;
name|SnapshotTestingUtils
operator|.
name|createSnapshotAndValidate
argument_list|(
name|admin
argument_list|,
name|localTableName
argument_list|,
name|TEST_FAM_STR
argument_list|,
name|snapshotNameAsString
argument_list|,
name|rootDir
argument_list|,
name|fs
argument_list|,
name|online
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|online
condition|)
block|{
name|tryDisable
argument_list|(
name|admin
argument_list|,
name|localTableName
argument_list|)
expr_stmt|;
block|}
name|TableName
name|cloneTableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test-clone-"
operator|+
name|localTableName
argument_list|)
decl_stmt|;
name|admin
operator|.
name|cloneSnapshot
argument_list|(
name|snapshotName
argument_list|,
name|cloneTableName
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|cloneTableName
argument_list|)
expr_stmt|;
comment|// Ensure the original table does not reference the HFiles anymore
name|admin
operator|.
name|majorCompact
argument_list|(
name|localTableName
argument_list|)
expr_stmt|;
comment|// Deleting the snapshot used to break the cloned table by deleting in-use HFiles
name|admin
operator|.
name|deleteSnapshot
argument_list|(
name|snapshotName
argument_list|)
expr_stmt|;
comment|// Wait for cleaner run and DFS heartbeats so that anything that is deletable is fully deleted
do|do
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
operator|!
name|admin
operator|.
name|listSnapshots
argument_list|(
name|snapshotNameAsString
argument_list|)
operator|.
name|isEmpty
argument_list|()
condition|)
do|;
try|try
init|(
name|Table
name|original
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|localTableName
argument_list|)
init|)
block|{
try|try
init|(
name|Table
name|clonedTable
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|cloneTableName
argument_list|)
init|)
block|{
comment|// Verify that all regions of both tables are readable
specifier|final
name|int
name|origTableRowCount
init|=
name|countRows
argument_list|(
name|original
argument_list|)
decl_stmt|;
specifier|final
name|int
name|clonedTableRowCount
init|=
name|countRows
argument_list|(
name|clonedTable
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|origTableRowCount
argument_list|,
name|clonedTableRowCount
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|protected
name|Table
name|createTable
parameter_list|(
specifier|final
name|TableName
name|table
parameter_list|,
name|byte
index|[]
name|family
parameter_list|)
throws|throws
name|Exception
block|{
name|Table
name|t
init|=
name|UTIL
operator|.
name|createTable
argument_list|(
name|table
argument_list|,
name|family
argument_list|)
decl_stmt|;
comment|// Wait for everything to be ready with the table
name|UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|table
argument_list|)
expr_stmt|;
comment|// At this point the table should be good to go.
return|return
name|t
return|;
block|}
specifier|protected
name|void
name|loadData
parameter_list|(
specifier|final
name|Table
name|table
parameter_list|,
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|loadTable
argument_list|(
name|table
argument_list|,
name|families
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|int
name|countRows
parameter_list|(
specifier|final
name|Table
name|table
parameter_list|,
specifier|final
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|UTIL
operator|.
name|countRows
argument_list|(
name|table
argument_list|,
name|families
argument_list|)
return|;
block|}
block|}
end_class

end_unit

