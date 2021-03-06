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
name|regex
operator|.
name|Pattern
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
name|TestSnapshotCloneIndependence
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
name|TestSnapshotCloneIndependence
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|testName
init|=
operator|new
name|TestName
argument_list|()
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
specifier|private
specifier|static
specifier|final
name|int
name|CLEANER_INTERVAL
init|=
literal|100
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|Path
name|rootDir
decl_stmt|;
specifier|private
name|Admin
name|admin
decl_stmt|;
specifier|private
name|TableName
name|originalTableName
decl_stmt|;
specifier|private
name|Table
name|originalTable
decl_stmt|;
specifier|private
name|TableName
name|cloneTableName
decl_stmt|;
specifier|private
name|int
name|countOriginalTable
decl_stmt|;
name|String
name|snapshotNameAsString
decl_stmt|;
name|String
name|snapshotName
decl_stmt|;
comment|/**    * Setup the config for the cluster and start it    */
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
name|fs
operator|=
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
expr_stmt|;
name|rootDir
operator|=
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
expr_stmt|;
name|admin
operator|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|originalTableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test"
operator|+
name|testName
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
name|cloneTableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test-clone-"
operator|+
name|originalTableName
argument_list|)
expr_stmt|;
name|snapshotNameAsString
operator|=
literal|"snapshot_"
operator|+
name|originalTableName
expr_stmt|;
name|snapshotName
operator|=
name|snapshotNameAsString
expr_stmt|;
name|originalTable
operator|=
name|createTable
argument_list|(
name|originalTableName
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
name|loadData
argument_list|(
name|originalTable
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
name|countOriginalTable
operator|=
name|countRows
argument_list|(
name|originalTable
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Original table has: "
operator|+
name|countOriginalTable
operator|+
literal|" rows"
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
name|originalTableName
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|deleteTable
argument_list|(
name|cloneTableName
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|deleteAllSnapshots
argument_list|(
name|UTIL
operator|.
name|getAdmin
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
name|Test
specifier|public
name|void
name|testOnlineSnapshotAppendIndependent
parameter_list|()
throws|throws
name|Exception
block|{
name|createAndCloneSnapshot
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|runTestSnapshotAppendIndependent
argument_list|()
expr_stmt|;
block|}
comment|/**    * Verify that adding data to the cloned table will not affect the original, and vice-versa when    * it is taken as an offline snapshot.    */
annotation|@
name|Test
specifier|public
name|void
name|testOfflineSnapshotAppendIndependent
parameter_list|()
throws|throws
name|Exception
block|{
name|createAndCloneSnapshot
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|runTestSnapshotAppendIndependent
argument_list|()
expr_stmt|;
block|}
comment|/**    * Verify that adding metadata to the cloned table will not affect the original, and vice-versa    * when it is taken as an online snapshot.    */
annotation|@
name|Test
specifier|public
name|void
name|testOnlineSnapshotMetadataChangesIndependent
parameter_list|()
throws|throws
name|Exception
block|{
name|createAndCloneSnapshot
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|runTestSnapshotMetadataChangesIndependent
argument_list|()
expr_stmt|;
block|}
comment|/**    * Verify that adding netadata to the cloned table will not affect the original, and vice-versa    * when is taken as an online snapshot.    */
annotation|@
name|Test
specifier|public
name|void
name|testOfflineSnapshotMetadataChangesIndependent
parameter_list|()
throws|throws
name|Exception
block|{
name|createAndCloneSnapshot
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|runTestSnapshotMetadataChangesIndependent
argument_list|()
expr_stmt|;
block|}
comment|/**    * Verify that region operations, in this case splitting a region, are independent between the    * cloned table and the original.    */
annotation|@
name|Test
specifier|public
name|void
name|testOfflineSnapshotRegionOperationsIndependent
parameter_list|()
throws|throws
name|Exception
block|{
name|createAndCloneSnapshot
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|runTestRegionOperationsIndependent
argument_list|()
expr_stmt|;
block|}
comment|/**    * Verify that region operations, in this case splitting a region, are independent between the    * cloned table and the original.    */
annotation|@
name|Test
specifier|public
name|void
name|testOnlineSnapshotRegionOperationsIndependent
parameter_list|()
throws|throws
name|Exception
block|{
name|createAndCloneSnapshot
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|runTestRegionOperationsIndependent
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOfflineSnapshotDeleteIndependent
parameter_list|()
throws|throws
name|Exception
block|{
name|createAndCloneSnapshot
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|runTestSnapshotDeleteIndependent
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOnlineSnapshotDeleteIndependent
parameter_list|()
throws|throws
name|Exception
block|{
name|createAndCloneSnapshot
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|runTestSnapshotDeleteIndependent
argument_list|()
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
name|Threads
operator|.
name|sleepWithoutInterrupt
argument_list|(
literal|500
argument_list|)
expr_stmt|;
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
comment|/**    * Takes the snapshot of originalTable and clones the snapshot to another tables.    * If {@code online} is false, the original table is disabled during taking snapshot, so also    * enables it again.    * @param online - Whether the table is online or not during the snapshot    */
specifier|private
name|void
name|createAndCloneSnapshot
parameter_list|(
name|boolean
name|online
parameter_list|)
throws|throws
name|Exception
block|{
name|SnapshotTestingUtils
operator|.
name|createSnapshotAndValidate
argument_list|(
name|admin
argument_list|,
name|originalTableName
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
comment|// If offline, enable the table disabled by snapshot testing util.
if|if
condition|(
operator|!
name|online
condition|)
block|{
name|admin
operator|.
name|enableTable
argument_list|(
name|originalTableName
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|originalTableName
argument_list|)
expr_stmt|;
block|}
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
block|}
comment|/**    * Verify that adding data to original table or clone table doesn't affect other table.    */
specifier|private
name|void
name|runTestSnapshotAppendIndependent
parameter_list|()
throws|throws
name|Exception
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
name|countOriginalTable
argument_list|,
name|clonedTableRowCount
argument_list|)
expr_stmt|;
comment|// Attempt to add data to the test
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
literal|"new-row-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
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
name|originalTable
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// Verify that the new row is not in the restored table
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"The row count of the original table was not modified by the put"
argument_list|,
name|countOriginalTable
operator|+
literal|1
argument_list|,
name|countRows
argument_list|(
name|originalTable
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
name|Put
name|p2
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"new-row-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|p2
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
name|p2
argument_list|)
expr_stmt|;
comment|// Verify that the row is not added to the original table.
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"The row count of the original table was modified by the put to the clone"
argument_list|,
name|countOriginalTable
operator|+
literal|1
argument_list|,
name|countRows
argument_list|(
name|originalTable
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
comment|/**    * Do a split, and verify that this only affects one table    */
specifier|private
name|void
name|runTestRegionOperationsIndependent
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Verify that region information is the same pre-split
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|clearRegionLocationCache
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|originalTableHRegions
init|=
name|admin
operator|.
name|getRegions
argument_list|(
name|originalTableName
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
name|getRegions
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
name|splitRegionAsync
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
operator|.
name|get
argument_list|()
expr_stmt|;
name|waitOnSplit
argument_list|(
name|UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|originalTable
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
name|getRegions
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
comment|/**    * Add metadata, and verify that this only affects one table    */
specifier|private
name|void
name|runTestSnapshotMetadataChangesIndependent
parameter_list|()
throws|throws
name|Exception
block|{
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
name|ColumnFamilyDescriptor
name|familyDescriptor
init|=
operator|new
name|ColumnFamilyDescriptorBuilder
operator|.
name|ModifyableColumnFamilyDescriptor
argument_list|(
name|TEST_FAM_2
argument_list|)
decl_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|originalTableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|addColumnFamily
argument_list|(
name|originalTableName
argument_list|,
name|familyDescriptor
argument_list|)
expr_stmt|;
comment|// Verify that it is not in the snapshot
name|admin
operator|.
name|enableTable
argument_list|(
name|originalTableName
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|originalTableName
argument_list|)
expr_stmt|;
comment|// get a description of the cloned table
comment|// get a list of its families
comment|// assert that the family is there
name|HTableDescriptor
name|originalTableDescriptor
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|originalTable
operator|.
name|getDescriptor
argument_list|()
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|clonedTableDescriptor
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|admin
operator|.
name|getDescriptor
argument_list|(
name|cloneTableName
argument_list|)
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
comment|/**    * Verify that deleting the snapshot does not affect either table.    */
specifier|private
name|void
name|runTestSnapshotDeleteIndependent
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Ensure the original table does not reference the HFiles anymore
name|admin
operator|.
name|majorCompact
argument_list|(
name|originalTableName
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
name|Pattern
name|pattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
name|snapshotNameAsString
argument_list|)
decl_stmt|;
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
name|pattern
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
name|originalTableName
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
specifier|public
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
name|originalTable
argument_list|,
name|TEST_FAM
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

