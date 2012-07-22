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
name|backup
operator|.
name|example
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
name|assertNull
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
name|FSDataOutputStream
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
name|master
operator|.
name|cleaner
operator|.
name|HFileCleaner
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
name|cleaner
operator|.
name|TimeToLiveHFileCleaner
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
name|CheckedArchivingHFileCleaner
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
name|regionserver
operator|.
name|Store
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
name|HFileArchiveTestingUtil
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
name|HFileArchiveUtil
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

begin_comment
comment|/**  * Spin up a small cluster and check that the hfiles of region are properly long-term archived as  * specified via the {@link ZKTableArchiveClient}.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestZooKeeperTableArchiveClient
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
name|TestZooKeeperTableArchiveClient
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
specifier|private
specifier|static
specifier|final
name|int
name|numRS
init|=
literal|2
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|maxTries
init|=
literal|5
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|ttl
init|=
literal|1000
decl_stmt|;
specifier|private
specifier|static
name|ZKTableArchiveClient
name|archivingClient
decl_stmt|;
comment|/**    * Setup the config for the cluster    */
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
name|numRS
argument_list|)
expr_stmt|;
name|archivingClient
operator|=
operator|new
name|ZKTableArchiveClient
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|getConnection
argument_list|()
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
comment|// drop the number of attempts for the hbase admin
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.client.retries.number"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// set the ttl on the hfiles
name|conf
operator|.
name|setLong
argument_list|(
name|TimeToLiveHFileCleaner
operator|.
name|TTL_CONF_KEY
argument_list|,
name|ttl
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|HFileCleaner
operator|.
name|MASTER_HFILE_CLEANER_PLUGINS
argument_list|,
name|CheckedArchivingHFileCleaner
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|,
name|TimeToLiveHFileCleaner
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|,
name|LongTermArchivingHFileCleaner
operator|.
name|class
operator|.
name|getCanonicalName
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
comment|// make sure that backups are off for all tables
name|archivingClient
operator|.
name|disableHFileBackup
argument_list|()
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
literal|"problem shutting down cluster"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test turning on/off archiving    */
annotation|@
name|Test
specifier|public
name|void
name|testArchivingEnableDisable
parameter_list|()
throws|throws
name|Exception
block|{
comment|// 1. turn on hfile backups
name|LOG
operator|.
name|debug
argument_list|(
literal|"----Starting archiving"
argument_list|)
expr_stmt|;
name|archivingClient
operator|.
name|enableHFileBackupAsync
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Archving didn't get turned on"
argument_list|,
name|archivingClient
operator|.
name|getArchivingEnabled
argument_list|(
name|TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
comment|// 2. Turn off archiving and make sure its off
name|archivingClient
operator|.
name|disableHFileBackup
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Archving didn't get turned off."
argument_list|,
name|archivingClient
operator|.
name|getArchivingEnabled
argument_list|(
name|TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
comment|// 3. Check enable/disable on a single table
name|archivingClient
operator|.
name|enableHFileBackupAsync
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Archving didn't get turned on"
argument_list|,
name|archivingClient
operator|.
name|getArchivingEnabled
argument_list|(
name|TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
comment|// 4. Turn off archiving and make sure its off
name|archivingClient
operator|.
name|disableHFileBackup
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Archving didn't get turned off for "
operator|+
name|STRING_TABLE_NAME
argument_list|,
name|archivingClient
operator|.
name|getArchivingEnabled
argument_list|(
name|TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testArchivingOnSingleTable
parameter_list|()
throws|throws
name|Exception
block|{
comment|// turn on hfile retention
name|LOG
operator|.
name|debug
argument_list|(
literal|"----Starting archiving"
argument_list|)
expr_stmt|;
name|archivingClient
operator|.
name|enableHFileBackupAsync
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Archving didn't get turned on"
argument_list|,
name|archivingClient
operator|.
name|getArchivingEnabled
argument_list|(
name|TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
comment|// get the RS and region serving our table
name|List
argument_list|<
name|HRegion
argument_list|>
name|servingRegions
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
comment|// make sure we only have 1 region serving this table
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|servingRegions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HRegion
name|region
init|=
name|servingRegions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// get the parent RS and monitor
name|HRegionServer
name|hrs
init|=
name|UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|hrs
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
comment|// put some data on the region
name|LOG
operator|.
name|debug
argument_list|(
literal|"-------Loading table"
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|loadRegion
argument_list|(
name|region
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
name|loadAndCompact
argument_list|(
name|region
argument_list|)
expr_stmt|;
comment|// check that we actually have some store files that were archived
name|Store
name|store
init|=
name|region
operator|.
name|getStore
argument_list|(
name|TEST_FAM
argument_list|)
decl_stmt|;
name|Path
name|storeArchiveDir
init|=
name|HFileArchiveTestingUtil
operator|.
name|getStoreArchivePath
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|region
argument_list|,
name|store
argument_list|)
decl_stmt|;
comment|// check to make sure we archived some files
name|assertTrue
argument_list|(
literal|"Didn't create a store archive directory"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|storeArchiveDir
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"No files in the store archive"
argument_list|,
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|storeArchiveDir
argument_list|,
literal|null
argument_list|)
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
comment|// and then put some non-tables files in the archive
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|Path
name|archiveDir
init|=
name|HFileArchiveUtil
operator|.
name|getArchivePath
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// write a tmp file to the archive dir
name|Path
name|tmpFile
init|=
operator|new
name|Path
argument_list|(
name|archiveDir
argument_list|,
literal|"toDelete"
argument_list|)
decl_stmt|;
name|FSDataOutputStream
name|out
init|=
name|fs
operator|.
name|create
argument_list|(
name|tmpFile
argument_list|)
decl_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|tmpFile
argument_list|)
argument_list|)
expr_stmt|;
comment|// make sure we wait long enough for the file to expire
name|Thread
operator|.
name|sleep
argument_list|(
name|ttl
argument_list|)
expr_stmt|;
comment|// print currrent state for comparison
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|fs
argument_list|,
name|archiveDir
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
comment|// ensure there are no archived files after waiting for a timeout
name|ensureHFileCleanersRun
argument_list|()
expr_stmt|;
comment|// check to make sure the right things get deleted
name|assertTrue
argument_list|(
literal|"Store archive got deleted"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|storeArchiveDir
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Archived HFiles got deleted"
argument_list|,
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|storeArchiveDir
argument_list|,
literal|null
argument_list|)
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Tmp file (non-table archive file) didn't "
operator|+
literal|"get deleted, archive dir: "
operator|+
name|fs
operator|.
name|listStatus
argument_list|(
name|archiveDir
argument_list|)
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|tmpFile
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Turning off hfile backup."
argument_list|)
expr_stmt|;
comment|// stop archiving the table
name|archivingClient
operator|.
name|disableHFileBackup
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Deleting table from archive."
argument_list|)
expr_stmt|;
comment|// now remove the archived table
name|Path
name|primaryTable
init|=
operator|new
name|Path
argument_list|(
name|HFileArchiveUtil
operator|.
name|getArchivePath
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|STRING_TABLE_NAME
argument_list|)
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|primaryTable
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Deleted primary table, waiting for file cleaners to run"
argument_list|)
expr_stmt|;
comment|// and make sure the archive directory is retained after a cleanup
comment|// have to do this manually since delegates aren't run if there isn't any files in the archive
comment|// dir to cleanup
name|Thread
operator|.
name|sleep
argument_list|(
name|ttl
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getHFileCleaner
argument_list|()
operator|.
name|triggerNow
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|ttl
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"File cleaners done, checking results."
argument_list|)
expr_stmt|;
comment|// but we still have the archive directory
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|HFileArchiveUtil
operator|.
name|getArchivePath
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Make sure all the {@link HFileCleaner} run.    *<p>    * Blocking operation up to 3x ttl    * @throws InterruptedException    */
specifier|private
name|void
name|ensureHFileCleanersRun
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|CheckedArchivingHFileCleaner
operator|.
name|resetCheck
argument_list|()
expr_stmt|;
do|do
block|{
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getHFileCleaner
argument_list|()
operator|.
name|triggerNow
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Triggered, sleeping an amount until we can pass the check."
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|ttl
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
operator|!
name|CheckedArchivingHFileCleaner
operator|.
name|getChecked
argument_list|()
condition|)
do|;
block|}
comment|/**    * Test archiving/cleaning across multiple tables, where some are retained, and others aren't    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testMultipleTables
parameter_list|()
throws|throws
name|Exception
block|{
name|archivingClient
operator|.
name|enableHFileBackupAsync
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Archving didn't get turned on"
argument_list|,
name|archivingClient
operator|.
name|getArchivingEnabled
argument_list|(
name|TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
comment|// create the another table that we don't archive
name|String
name|otherTable
init|=
literal|"otherTable"
decl_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|otherTable
argument_list|)
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
comment|// get the parent RS and monitor
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// put data in the filesystem of the first table
name|loadAndCompact
argument_list|(
name|STRING_TABLE_NAME
argument_list|)
expr_stmt|;
comment|// and some data in the other table
name|loadAndCompact
argument_list|(
name|otherTable
argument_list|)
expr_stmt|;
comment|// make sure we wait long enough for the other tables files to expire
name|Thread
operator|.
name|sleep
argument_list|(
name|ttl
argument_list|)
expr_stmt|;
name|ensureHFileCleanersRun
argument_list|()
expr_stmt|;
comment|// check to make sure the right things get deleted
name|Path
name|primaryStoreArchive
init|=
name|HFileArchiveTestingUtil
operator|.
name|getStoreArchivePath
argument_list|(
name|UTIL
argument_list|,
name|STRING_TABLE_NAME
argument_list|,
name|TEST_FAM
argument_list|)
decl_stmt|;
name|Path
name|otherStoreArchive
init|=
name|HFileArchiveTestingUtil
operator|.
name|getStoreArchivePath
argument_list|(
name|UTIL
argument_list|,
name|otherTable
argument_list|,
name|TEST_FAM
argument_list|)
decl_stmt|;
comment|// make sure the primary store doesn't have any files
name|assertTrue
argument_list|(
literal|"Store archive got deleted"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|primaryStoreArchive
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Archived HFiles got deleted"
argument_list|,
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|primaryStoreArchive
argument_list|,
literal|null
argument_list|)
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
literal|"Archived HFiles should have gotten deleted, but didn't"
argument_list|,
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|otherStoreArchive
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|// sleep again to make sure we the other table gets cleaned up
name|Thread
operator|.
name|sleep
argument_list|(
name|ttl
argument_list|)
expr_stmt|;
name|ensureHFileCleanersRun
argument_list|()
expr_stmt|;
comment|// first pass removes the store archive
name|assertFalse
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|otherStoreArchive
argument_list|)
argument_list|)
expr_stmt|;
comment|// second pass removes the region
name|Thread
operator|.
name|sleep
argument_list|(
name|ttl
argument_list|)
expr_stmt|;
name|ensureHFileCleanersRun
argument_list|()
expr_stmt|;
name|Path
name|parent
init|=
name|otherStoreArchive
operator|.
name|getParent
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|parent
argument_list|)
argument_list|)
expr_stmt|;
comment|// thrid pass remove the table
name|Thread
operator|.
name|sleep
argument_list|(
name|ttl
argument_list|)
expr_stmt|;
name|ensureHFileCleanersRun
argument_list|()
expr_stmt|;
name|parent
operator|=
name|otherStoreArchive
operator|.
name|getParent
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|parent
argument_list|)
argument_list|)
expr_stmt|;
comment|// but we still have the archive directory
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|HFileArchiveUtil
operator|.
name|getArchivePath
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|fs
argument_list|,
name|HFileArchiveUtil
operator|.
name|getArchivePath
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
name|UTIL
operator|.
name|deleteTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|otherTable
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|loadAndCompact
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|Exception
block|{
name|byte
index|[]
name|table
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// get the RS and region serving our table
name|List
argument_list|<
name|HRegion
argument_list|>
name|servingRegions
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|table
argument_list|)
decl_stmt|;
comment|// make sure we only have 1 region serving this table
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|servingRegions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HRegion
name|region
init|=
name|servingRegions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// get the parent RS and monitor
name|HRegionServer
name|hrs
init|=
name|UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|hrs
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
comment|// put some data on the region
name|LOG
operator|.
name|debug
argument_list|(
literal|"-------Loading table"
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|loadRegion
argument_list|(
name|region
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
name|loadAndCompact
argument_list|(
name|region
argument_list|)
expr_stmt|;
comment|// check that we actually have some store files that were archived
name|Store
name|store
init|=
name|region
operator|.
name|getStore
argument_list|(
name|TEST_FAM
argument_list|)
decl_stmt|;
name|Path
name|storeArchiveDir
init|=
name|HFileArchiveTestingUtil
operator|.
name|getStoreArchivePath
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|region
argument_list|,
name|store
argument_list|)
decl_stmt|;
comment|// check to make sure we archived some files
name|assertTrue
argument_list|(
literal|"Didn't create a store archive directory"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|storeArchiveDir
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"No files in the store archive"
argument_list|,
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|storeArchiveDir
argument_list|,
literal|null
argument_list|)
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    * Load the given region and then ensure that it compacts some files    */
specifier|private
name|void
name|loadAndCompact
parameter_list|(
name|HRegion
name|region
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|tries
init|=
literal|0
decl_stmt|;
name|Exception
name|last
init|=
literal|null
decl_stmt|;
while|while
condition|(
name|tries
operator|++
operator|<=
name|maxTries
condition|)
block|{
try|try
block|{
comment|// load the region with data
name|UTIL
operator|.
name|loadRegion
argument_list|(
name|region
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
comment|// and then trigger a compaction to be sure we try to archive
name|compactRegion
argument_list|(
name|region
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
return|return;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// keep this around for if we fail later
name|last
operator|=
name|e
expr_stmt|;
block|}
block|}
throw|throw
name|last
throw|;
block|}
comment|/**    * Compact all the store files in a given region.    */
specifier|private
name|void
name|compactRegion
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|byte
index|[]
name|family
parameter_list|)
throws|throws
name|IOException
block|{
name|Store
name|store
init|=
name|region
operator|.
name|getStores
argument_list|()
operator|.
name|get
argument_list|(
name|TEST_FAM
argument_list|)
decl_stmt|;
name|store
operator|.
name|compactRecentForTesting
argument_list|(
name|store
operator|.
name|getStorefiles
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

