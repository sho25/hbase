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
comment|/**  * Test to verify that the cloned table is independent of the table from which it was cloned  */
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
name|String
name|TEST_FAM_STR
init|=
literal|"fam"
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
name|TEST_FAM_STR
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TABLE_NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|STRING_TABLE_NAME
argument_list|)
decl_stmt|;
comment|/**    * Setup the config for the cluster and start it    * @throws Exception on failure    */
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
comment|// drop the number of attempts for the hbase admin
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
name|setInt
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|250
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|6
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
comment|// and cleanup the archive directory
try|try
block|{
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
operator|.
name|delete
argument_list|(
operator|new
name|Path
argument_list|(
name|UTIL
operator|.
name|getDefaultRootDirPath
argument_list|()
argument_list|,
literal|".archive"
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failure to delete archive directory"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
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
name|runTestSnapshotAppendIndependent
argument_list|(
literal|true
argument_list|)
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
name|runTestSnapshotAppendIndependent
argument_list|(
literal|false
argument_list|)
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
name|runTestSnapshotMetadataChangesIndependent
argument_list|(
literal|true
argument_list|)
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
name|runTestSnapshotMetadataChangesIndependent
argument_list|(
literal|false
argument_list|)
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
name|runTestRegionOperationsIndependent
argument_list|(
literal|false
argument_list|)
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
name|runTestRegionOperationsIndependent
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
specifier|final
name|HTable
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
literal|50
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
if|if
condition|(
name|t
operator|.
name|getRegionLocations
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
name|HBaseAdmin
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
name|String
name|localTableNameAsString
init|=
name|STRING_TABLE_NAME
operator|+
name|startTime
decl_stmt|;
name|HTable
name|original
init|=
name|UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|localTableNameAsString
argument_list|)
argument_list|,
name|TEST_FAM
argument_list|)
decl_stmt|;
try|try
block|{
name|UTIL
operator|.
name|loadTable
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
name|UTIL
operator|.
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
name|localTableNameAsString
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
name|localTableNameAsString
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
name|admin
operator|.
name|enableTable
argument_list|(
name|localTableNameAsString
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|cloneTableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test-clone-"
operator|+
name|localTableNameAsString
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
name|HTable
name|clonedTable
init|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|cloneTableName
argument_list|)
decl_stmt|;
try|try
block|{
specifier|final
name|int
name|clonedTableRowCount
init|=
name|UTIL
operator|.
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
name|add
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
name|original
operator|.
name|flushCommits
argument_list|()
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
name|UTIL
operator|.
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
name|UTIL
operator|.
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
name|add
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
name|clonedTable
operator|.
name|flushCommits
argument_list|()
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
name|UTIL
operator|.
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
name|UTIL
operator|.
name|countRows
argument_list|(
name|clonedTable
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|clonedTable
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|original
operator|.
name|close
argument_list|()
expr_stmt|;
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
name|HBaseAdmin
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
name|String
name|localTableNameAsString
init|=
name|STRING_TABLE_NAME
operator|+
name|startTime
decl_stmt|;
name|HTable
name|original
init|=
name|UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|localTableNameAsString
argument_list|)
argument_list|,
name|TEST_FAM
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|loadTable
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
name|UTIL
operator|.
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
name|localTableNameAsString
decl_stmt|;
comment|// Create a snapshot
name|SnapshotTestingUtils
operator|.
name|createSnapshotAndValidate
argument_list|(
name|admin
argument_list|,
name|localTableNameAsString
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
name|admin
operator|.
name|enableTable
argument_list|(
name|localTableNameAsString
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|cloneTableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test-clone-"
operator|+
name|localTableNameAsString
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
name|original
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
name|Bytes
operator|.
name|toBytes
argument_list|(
name|localTableNameAsString
argument_list|)
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
name|split
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
name|HBaseAdmin
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
name|String
name|localTableNameAsString
init|=
name|STRING_TABLE_NAME
operator|+
name|startTime
decl_stmt|;
name|HTable
name|original
init|=
name|UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|localTableNameAsString
argument_list|)
argument_list|,
name|TEST_FAM
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|loadTable
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
name|localTableNameAsString
decl_stmt|;
comment|// Create a snapshot
name|SnapshotTestingUtils
operator|.
name|createSnapshotAndValidate
argument_list|(
name|admin
argument_list|,
name|localTableNameAsString
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
name|admin
operator|.
name|enableTable
argument_list|(
name|localTableNameAsString
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|cloneTableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test-clone-"
operator|+
name|localTableNameAsString
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
name|admin
operator|.
name|disableTable
argument_list|(
name|localTableNameAsString
argument_list|)
expr_stmt|;
name|admin
operator|.
name|addColumn
argument_list|(
name|localTableNameAsString
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
comment|// Verify that it is not in the snapshot
name|admin
operator|.
name|enableTable
argument_list|(
name|localTableNameAsString
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
block|}
end_class

end_unit

