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
name|FileStatus
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
name|Stoppable
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
name|ClusterConnection
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
name|Put
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
name|BaseHFileCleanerDelegate
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
name|testclassification
operator|.
name|MiscTests
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
name|HFileArchiveUtil
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
name|StoppableImplementation
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
name|zookeeper
operator|.
name|ZKUtil
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
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

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|invocation
operator|.
name|InvocationOnMock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|Answer
import|;
end_import

begin_comment
comment|/**  * Spin up a small cluster and check that the hfiles of region are properly long-term archived as  * specified via the {@link ZKTableArchiveClient}.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
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
name|HBaseTestingUtility
operator|.
name|createLocalHTU
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
name|ZKTableArchiveClient
name|archivingClient
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Path
argument_list|>
name|toCleanup
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|ClusterConnection
name|CONNECTION
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
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|CONNECTION
operator|=
operator|(
name|ClusterConnection
operator|)
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
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
name|CONNECTION
argument_list|)
expr_stmt|;
comment|// make hfile archiving node so we can archive files
name|ZooKeeperWatcher
name|watcher
init|=
name|UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
decl_stmt|;
name|String
name|archivingZNode
init|=
name|ZKTableArchiveClient
operator|.
name|getArchiveZNode
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|watcher
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|watcher
argument_list|,
name|archivingZNode
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
comment|// only compact with 3 files
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compaction.min"
argument_list|,
literal|3
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
try|try
block|{
name|FileSystem
name|fs
init|=
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
comment|// cleanup each of the files/directories registered
for|for
control|(
name|Path
name|file
range|:
name|toCleanup
control|)
block|{
comment|// remove the table and archive directories
name|FSUtils
operator|.
name|delete
argument_list|(
name|fs
argument_list|,
name|file
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
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
finally|finally
block|{
name|toCleanup
operator|.
name|clear
argument_list|()
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
name|CONNECTION
operator|.
name|close
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|shutdownMiniZKCluster
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
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
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
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testArchivingOnSingleTable
parameter_list|()
throws|throws
name|Exception
block|{
name|createArchiveDirectory
argument_list|()
expr_stmt|;
name|FileSystem
name|fs
init|=
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|Path
name|archiveDir
init|=
name|getArchiveDir
argument_list|()
decl_stmt|;
name|Path
name|tableDir
init|=
name|getTableDir
argument_list|(
name|STRING_TABLE_NAME
argument_list|)
decl_stmt|;
name|toCleanup
operator|.
name|add
argument_list|(
name|archiveDir
argument_list|)
expr_stmt|;
name|toCleanup
operator|.
name|add
argument_list|(
name|tableDir
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// setup the delegate
name|Stoppable
name|stop
init|=
operator|new
name|StoppableImplementation
argument_list|()
decl_stmt|;
name|HFileCleaner
name|cleaner
init|=
name|setupAndCreateCleaner
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|archiveDir
argument_list|,
name|stop
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|BaseHFileCleanerDelegate
argument_list|>
name|cleaners
init|=
name|turnOnArchiving
argument_list|(
name|STRING_TABLE_NAME
argument_list|,
name|cleaner
argument_list|)
decl_stmt|;
specifier|final
name|LongTermArchivingHFileCleaner
name|delegate
init|=
operator|(
name|LongTermArchivingHFileCleaner
operator|)
name|cleaners
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// create the region
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAM
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|UTIL
operator|.
name|createTestRegion
argument_list|(
name|STRING_TABLE_NAME
argument_list|,
name|hcd
argument_list|)
decl_stmt|;
name|loadFlushAndCompact
argument_list|(
name|region
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
comment|// get the current hfiles in the archive directory
name|List
argument_list|<
name|Path
argument_list|>
name|files
init|=
name|getAllFiles
argument_list|(
name|fs
argument_list|,
name|archiveDir
argument_list|)
decl_stmt|;
if|if
condition|(
name|files
operator|==
literal|null
condition|)
block|{
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|fs
argument_list|,
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Didn't archive any files!"
argument_list|)
throw|;
block|}
name|CountDownLatch
name|finished
init|=
name|setupCleanerWatching
argument_list|(
name|delegate
argument_list|,
name|cleaners
argument_list|,
name|files
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|runCleaner
argument_list|(
name|cleaner
argument_list|,
name|finished
argument_list|,
name|stop
argument_list|)
expr_stmt|;
comment|// know the cleaner ran, so now check all the files again to make sure they are still there
name|List
argument_list|<
name|Path
argument_list|>
name|archivedFiles
init|=
name|getAllFiles
argument_list|(
name|fs
argument_list|,
name|archiveDir
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Archived files changed after running archive cleaner."
argument_list|,
name|files
argument_list|,
name|archivedFiles
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
comment|/**    * Test archiving/cleaning across multiple tables, where some are retained, and others aren't    * @throws Exception on failure    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testMultipleTables
parameter_list|()
throws|throws
name|Exception
block|{
name|createArchiveDirectory
argument_list|()
expr_stmt|;
name|String
name|otherTable
init|=
literal|"otherTable"
decl_stmt|;
name|FileSystem
name|fs
init|=
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|Path
name|archiveDir
init|=
name|getArchiveDir
argument_list|()
decl_stmt|;
name|Path
name|tableDir
init|=
name|getTableDir
argument_list|(
name|STRING_TABLE_NAME
argument_list|)
decl_stmt|;
name|Path
name|otherTableDir
init|=
name|getTableDir
argument_list|(
name|otherTable
argument_list|)
decl_stmt|;
comment|// register cleanup for the created directories
name|toCleanup
operator|.
name|add
argument_list|(
name|archiveDir
argument_list|)
expr_stmt|;
name|toCleanup
operator|.
name|add
argument_list|(
name|tableDir
argument_list|)
expr_stmt|;
name|toCleanup
operator|.
name|add
argument_list|(
name|otherTableDir
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// setup the delegate
name|Stoppable
name|stop
init|=
operator|new
name|StoppableImplementation
argument_list|()
decl_stmt|;
name|HFileCleaner
name|cleaner
init|=
name|setupAndCreateCleaner
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|archiveDir
argument_list|,
name|stop
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|BaseHFileCleanerDelegate
argument_list|>
name|cleaners
init|=
name|turnOnArchiving
argument_list|(
name|STRING_TABLE_NAME
argument_list|,
name|cleaner
argument_list|)
decl_stmt|;
specifier|final
name|LongTermArchivingHFileCleaner
name|delegate
init|=
operator|(
name|LongTermArchivingHFileCleaner
operator|)
name|cleaners
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// create the region
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAM
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|UTIL
operator|.
name|createTestRegion
argument_list|(
name|STRING_TABLE_NAME
argument_list|,
name|hcd
argument_list|)
decl_stmt|;
name|loadFlushAndCompact
argument_list|(
name|region
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
comment|// create the another table that we don't archive
name|hcd
operator|=
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAM
argument_list|)
expr_stmt|;
name|HRegion
name|otherRegion
init|=
name|UTIL
operator|.
name|createTestRegion
argument_list|(
name|otherTable
argument_list|,
name|hcd
argument_list|)
decl_stmt|;
name|loadFlushAndCompact
argument_list|(
name|otherRegion
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
comment|// get the current hfiles in the archive directory
name|List
argument_list|<
name|Path
argument_list|>
name|files
init|=
name|getAllFiles
argument_list|(
name|fs
argument_list|,
name|archiveDir
argument_list|)
decl_stmt|;
if|if
condition|(
name|files
operator|==
literal|null
condition|)
block|{
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
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Didn't load archive any files!"
argument_list|)
throw|;
block|}
comment|// make sure we have files from both tables
name|int
name|initialCountForPrimary
init|=
literal|0
decl_stmt|;
name|int
name|initialCountForOtherTable
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Path
name|file
range|:
name|files
control|)
block|{
name|String
name|tableName
init|=
name|file
operator|.
name|getParent
argument_list|()
operator|.
name|getParent
argument_list|()
operator|.
name|getParent
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|// check to which table this file belongs
if|if
condition|(
name|tableName
operator|.
name|equals
argument_list|(
name|otherTable
argument_list|)
condition|)
name|initialCountForOtherTable
operator|++
expr_stmt|;
elseif|else
if|if
condition|(
name|tableName
operator|.
name|equals
argument_list|(
name|STRING_TABLE_NAME
argument_list|)
condition|)
name|initialCountForPrimary
operator|++
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"Didn't archive files for:"
operator|+
name|STRING_TABLE_NAME
argument_list|,
name|initialCountForPrimary
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Didn't archive files for:"
operator|+
name|otherTable
argument_list|,
name|initialCountForOtherTable
operator|>
literal|0
argument_list|)
expr_stmt|;
comment|// run the cleaners, checking for each of the directories + files (both should be deleted and
comment|// need to be checked) in 'otherTable' and the files (which should be retained) in the 'table'
name|CountDownLatch
name|finished
init|=
name|setupCleanerWatching
argument_list|(
name|delegate
argument_list|,
name|cleaners
argument_list|,
name|files
operator|.
name|size
argument_list|()
operator|+
literal|3
argument_list|)
decl_stmt|;
comment|// run the cleaner
name|cleaner
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// wait for the cleaner to check all the files
name|finished
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// stop the cleaner
name|stop
operator|.
name|stop
argument_list|(
literal|""
argument_list|)
expr_stmt|;
comment|// know the cleaner ran, so now check all the files again to make sure they are still there
name|List
argument_list|<
name|Path
argument_list|>
name|archivedFiles
init|=
name|getAllFiles
argument_list|(
name|fs
argument_list|,
name|archiveDir
argument_list|)
decl_stmt|;
name|int
name|archivedForPrimary
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Path
name|file
range|:
name|archivedFiles
control|)
block|{
name|String
name|tableName
init|=
name|file
operator|.
name|getParent
argument_list|()
operator|.
name|getParent
argument_list|()
operator|.
name|getParent
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|// ensure we don't have files from the non-archived table
name|assertFalse
argument_list|(
literal|"Have a file from the non-archived table: "
operator|+
name|file
argument_list|,
name|tableName
operator|.
name|equals
argument_list|(
name|otherTable
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|tableName
operator|.
name|equals
argument_list|(
name|STRING_TABLE_NAME
argument_list|)
condition|)
name|archivedForPrimary
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Not all archived files for the primary table were retained."
argument_list|,
name|initialCountForPrimary
argument_list|,
name|archivedForPrimary
argument_list|)
expr_stmt|;
comment|// but we still have the archive directory
name|assertTrue
argument_list|(
literal|"Archive directory was deleted via archiver"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|archiveDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|createArchiveDirectory
parameter_list|()
throws|throws
name|IOException
block|{
comment|//create the archive and test directory
name|FileSystem
name|fs
init|=
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|Path
name|archiveDir
init|=
name|getArchiveDir
argument_list|()
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|archiveDir
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Path
name|getArchiveDir
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|Path
argument_list|(
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|HConstants
operator|.
name|HFILE_ARCHIVE_DIRECTORY
argument_list|)
return|;
block|}
specifier|private
name|Path
name|getTableDir
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|testDataDir
init|=
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
decl_stmt|;
name|FSUtils
operator|.
name|setRootDir
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|testDataDir
argument_list|)
expr_stmt|;
return|return
operator|new
name|Path
argument_list|(
name|testDataDir
argument_list|,
name|tableName
argument_list|)
return|;
block|}
specifier|private
name|HFileCleaner
name|setupAndCreateCleaner
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|archiveDir
parameter_list|,
name|Stoppable
name|stop
parameter_list|)
block|{
name|conf
operator|.
name|setStrings
argument_list|(
name|HFileCleaner
operator|.
name|MASTER_HFILE_CLEANER_PLUGINS
argument_list|,
name|LongTermArchivingHFileCleaner
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|HFileCleaner
argument_list|(
literal|1000
argument_list|,
name|stop
argument_list|,
name|conf
argument_list|,
name|fs
argument_list|,
name|archiveDir
argument_list|)
return|;
block|}
comment|/**    * Start archiving table for given hfile cleaner    * @param tableName table to archive    * @param cleaner cleaner to check to make sure change propagated    * @return underlying {@link LongTermArchivingHFileCleaner} that is managing archiving    * @throws IOException on failure    * @throws KeeperException on failure    */
specifier|private
name|List
argument_list|<
name|BaseHFileCleanerDelegate
argument_list|>
name|turnOnArchiving
parameter_list|(
name|String
name|tableName
parameter_list|,
name|HFileCleaner
name|cleaner
parameter_list|)
throws|throws
name|IOException
throws|,
name|KeeperException
block|{
comment|// turn on hfile retention
name|LOG
operator|.
name|debug
argument_list|(
literal|"----Starting archiving for table:"
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|archivingClient
operator|.
name|enableHFileBackupAsync
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
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
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
comment|// wait for the archiver to get the notification
name|List
argument_list|<
name|BaseHFileCleanerDelegate
argument_list|>
name|cleaners
init|=
name|cleaner
operator|.
name|getDelegatesForTesting
argument_list|()
decl_stmt|;
name|LongTermArchivingHFileCleaner
name|delegate
init|=
operator|(
name|LongTermArchivingHFileCleaner
operator|)
name|cleaners
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
while|while
condition|(
operator|!
name|delegate
operator|.
name|archiveTracker
operator|.
name|keepHFiles
argument_list|(
name|STRING_TABLE_NAME
argument_list|)
condition|)
block|{
comment|// spin until propagation - should be fast
block|}
return|return
name|cleaners
return|;
block|}
comment|/**    * Spy on the {@link LongTermArchivingHFileCleaner} to ensure we can catch when the cleaner has    * seen all the files    * @return a {@link CountDownLatch} to wait on that releases when the cleaner has been called at    *         least the expected number of times.    */
specifier|private
name|CountDownLatch
name|setupCleanerWatching
parameter_list|(
name|LongTermArchivingHFileCleaner
name|cleaner
parameter_list|,
name|List
argument_list|<
name|BaseHFileCleanerDelegate
argument_list|>
name|cleaners
parameter_list|,
specifier|final
name|int
name|expected
parameter_list|)
block|{
comment|// replace the cleaner with one that we can can check
name|BaseHFileCleanerDelegate
name|delegateSpy
init|=
name|Mockito
operator|.
name|spy
argument_list|(
name|cleaner
argument_list|)
decl_stmt|;
specifier|final
name|int
index|[]
name|counter
init|=
operator|new
name|int
index|[]
block|{
literal|0
block|}
decl_stmt|;
specifier|final
name|CountDownLatch
name|finished
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|counter
index|[
literal|0
index|]
operator|++
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|counter
index|[
literal|0
index|]
operator|+
literal|"/ "
operator|+
name|expected
operator|+
literal|") Wrapping call to getDeletableFiles for files: "
operator|+
name|invocation
operator|.
name|getArguments
argument_list|()
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|ret
init|=
operator|(
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
operator|)
name|invocation
operator|.
name|callRealMethod
argument_list|()
decl_stmt|;
if|if
condition|(
name|counter
index|[
literal|0
index|]
operator|>=
name|expected
condition|)
name|finished
operator|.
name|countDown
argument_list|()
expr_stmt|;
return|return
name|ret
return|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|delegateSpy
argument_list|)
operator|.
name|getDeletableFiles
argument_list|(
name|Mockito
operator|.
name|anyListOf
argument_list|(
name|FileStatus
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|cleaners
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|delegateSpy
argument_list|)
expr_stmt|;
return|return
name|finished
return|;
block|}
comment|/**    * Get all the files (non-directory entries) in the file system under the passed directory    * @param dir directory to investigate    * @return all files under the directory    */
specifier|private
name|List
argument_list|<
name|Path
argument_list|>
name|getAllFiles
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|dir
parameter_list|)
throws|throws
name|IOException
block|{
name|FileStatus
index|[]
name|files
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|dir
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|files
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"No files under:"
operator|+
name|dir
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|Path
argument_list|>
name|allFiles
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
if|if
condition|(
name|file
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|subFiles
init|=
name|getAllFiles
argument_list|(
name|fs
argument_list|,
name|file
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|subFiles
operator|!=
literal|null
condition|)
name|allFiles
operator|.
name|addAll
argument_list|(
name|subFiles
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|allFiles
operator|.
name|add
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|allFiles
return|;
block|}
specifier|private
name|void
name|loadFlushAndCompact
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
comment|// create two hfiles in the region
name|createHFileInRegion
argument_list|(
name|region
argument_list|,
name|family
argument_list|)
expr_stmt|;
name|createHFileInRegion
argument_list|(
name|region
argument_list|,
name|family
argument_list|)
expr_stmt|;
name|Store
name|s
init|=
name|region
operator|.
name|getStore
argument_list|(
name|family
argument_list|)
decl_stmt|;
name|int
name|count
init|=
name|s
operator|.
name|getStorefilesCount
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Don't have the expected store files, wanted>= 2 store files, but was:"
operator|+
name|count
argument_list|,
name|count
operator|>=
literal|2
argument_list|)
expr_stmt|;
comment|// compact the two files into one file to get files in the archive
name|LOG
operator|.
name|debug
argument_list|(
literal|"Compacting stores"
argument_list|)
expr_stmt|;
name|region
operator|.
name|compactStores
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a new hfile in the passed region    * @param region region to operate on    * @param columnFamily family for which to add data    * @throws IOException    */
specifier|private
name|void
name|createHFileInRegion
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|byte
index|[]
name|columnFamily
parameter_list|)
throws|throws
name|IOException
block|{
comment|// put one row in the region
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
literal|"row"
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|columnFamily
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Qual"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"v1"
argument_list|)
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// flush the region to make a store file
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
comment|/**    * @param cleaner    */
specifier|private
name|void
name|runCleaner
parameter_list|(
name|HFileCleaner
name|cleaner
parameter_list|,
name|CountDownLatch
name|finished
parameter_list|,
name|Stoppable
name|stop
parameter_list|)
throws|throws
name|InterruptedException
block|{
comment|// run the cleaner
name|cleaner
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// wait for the cleaner to check all the files
name|finished
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// stop the cleaner
name|stop
operator|.
name|stop
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

