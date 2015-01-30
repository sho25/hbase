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
name|Collections
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
name|fs
operator|.
name|PathFilter
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
name|ChoreService
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
name|HRegionFileSystem
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
comment|/**  * Test that the {@link HFileArchiver} correctly removes all the parts of a region when cleaning up  * a region  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
operator|.
name|class
block|,
name|MiscTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestHFileArchiving
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
name|TestHFileArchiving
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
argument_list|()
expr_stmt|;
comment|// We don't want the cleaner to remove files. The tests do that.
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getHFileCleaner
argument_list|()
operator|.
name|cancel
argument_list|(
literal|true
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
comment|// drop the memstore size so we get flushes
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hregion.memstore.flush.size"
argument_list|,
literal|25000
argument_list|)
expr_stmt|;
comment|// disable major compactions
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|MAJOR_COMPACTION_PERIOD
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// prevent aggressive region split
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
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
comment|// cleanup the archive directory
try|try
block|{
name|clearArchiveDirectory
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Failure to delete archive directory:"
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
comment|// NOOP;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRemovesRegionDirOnArchive
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testRemovesRegionDirOnArchive"
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
specifier|final
name|Admin
name|admin
init|=
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
comment|// get the current store files for the region
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
comment|// and load the table
name|UTIL
operator|.
name|loadRegion
argument_list|(
name|region
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
comment|// shutdown the table so we can manipulate the files
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
comment|// now attempt to depose the region
name|Path
name|rootDir
init|=
name|region
operator|.
name|getRegionFileSystem
argument_list|()
operator|.
name|getTableDir
argument_list|()
operator|.
name|getParent
argument_list|()
decl_stmt|;
name|Path
name|regionDir
init|=
name|HRegion
operator|.
name|getRegionDir
argument_list|(
name|rootDir
argument_list|,
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
decl_stmt|;
name|HFileArchiver
operator|.
name|archiveRegion
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
comment|// check for the existence of the archive directory and some files in it
name|Path
name|archiveDir
init|=
name|HFileArchiveTestingUtil
operator|.
name|getRegionArchiveDir
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|region
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|archiveDir
argument_list|)
argument_list|)
expr_stmt|;
comment|// check to make sure the store directory was copied
comment|// check to make sure the store directory was copied
name|FileStatus
index|[]
name|stores
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|archiveDir
argument_list|,
operator|new
name|PathFilter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
if|if
condition|(
name|p
operator|.
name|getName
argument_list|()
operator|.
name|contains
argument_list|(
name|HConstants
operator|.
name|RECOVERED_EDITS_DIR
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|stores
operator|.
name|length
operator|==
literal|1
argument_list|)
expr_stmt|;
comment|// make sure we archived the store files
name|FileStatus
index|[]
name|storeFiles
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|stores
index|[
literal|0
index|]
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|storeFiles
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
comment|// then ensure the region's directory isn't present
name|assertFalse
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|regionDir
argument_list|)
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that the region directory is removed when we archive a region without store files, but    * still has hidden files.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testDeleteRegionWithNoStoreFiles
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testDeleteRegionWithNoStoreFiles"
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
comment|// get the current store files for the region
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
name|FileSystem
name|fs
init|=
name|region
operator|.
name|getRegionFileSystem
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
comment|// make sure there are some files in the regiondir
name|Path
name|rootDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|fs
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|regionDir
init|=
name|HRegion
operator|.
name|getRegionDir
argument_list|(
name|rootDir
argument_list|,
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|regionFiles
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|regionDir
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
literal|"No files in the region directory"
argument_list|,
name|regionFiles
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|files
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
name|regionFiles
control|)
block|{
name|files
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
name|LOG
operator|.
name|debug
argument_list|(
literal|"Current files:"
operator|+
name|files
argument_list|)
expr_stmt|;
block|}
comment|// delete the visible folders so we just have hidden files/folders
specifier|final
name|PathFilter
name|dirFilter
init|=
operator|new
name|FSUtils
operator|.
name|DirFilter
argument_list|(
name|fs
argument_list|)
decl_stmt|;
name|PathFilter
name|nonHidden
init|=
operator|new
name|PathFilter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|file
parameter_list|)
block|{
return|return
name|dirFilter
operator|.
name|accept
argument_list|(
name|file
argument_list|)
operator|&&
operator|!
name|file
operator|.
name|getName
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"."
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|FileStatus
index|[]
name|storeDirs
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|regionDir
argument_list|,
name|nonHidden
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|store
range|:
name|storeDirs
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Deleting store for test"
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|store
operator|.
name|getPath
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// then archive the region
name|HFileArchiver
operator|.
name|archiveRegion
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
comment|// and check to make sure the region directoy got deleted
name|assertFalse
argument_list|(
literal|"Region directory ("
operator|+
name|regionDir
operator|+
literal|"), still exists."
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|regionDir
argument_list|)
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testArchiveOnTableDelete
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testArchiveOnTableDelete"
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
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
comment|// get the hfiles in the region
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
init|=
name|hrs
operator|.
name|getOnlineRegions
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"More that 1 region for test table."
argument_list|,
literal|1
argument_list|,
name|regions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|region
operator|=
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// wait for all the compactions to complete
name|region
operator|.
name|waitForFlushesAndCompactions
argument_list|()
expr_stmt|;
comment|// disable table to prevent new updates
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
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
literal|"Disabled table"
argument_list|)
expr_stmt|;
comment|// remove all the files from the archive to get a fair comparison
name|clearArchiveDirectory
argument_list|()
expr_stmt|;
comment|// then get the current store files
name|List
argument_list|<
name|String
argument_list|>
name|storeFiles
init|=
name|getRegionStoreFiles
argument_list|(
name|region
argument_list|)
decl_stmt|;
comment|// then delete the table so the hfiles get archived
name|UTIL
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Deleted table"
argument_list|)
expr_stmt|;
name|assertArchiveFiles
argument_list|(
name|fs
argument_list|,
name|storeFiles
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertArchiveFiles
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|storeFiles
parameter_list|,
name|long
name|timeout
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|end
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
name|timeout
decl_stmt|;
name|Path
name|archiveDir
init|=
name|HFileArchiveUtil
operator|.
name|getArchivePath
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|archivedFiles
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// We have to ensure that the DeleteTableHandler is finished. HBaseAdmin.deleteXXX() can return before all files
comment|// are archived. We should fix HBASE-5487 and fix synchronous operations from admin.
while|while
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|<
name|end
condition|)
block|{
name|archivedFiles
operator|=
name|getAllFileNames
argument_list|(
name|fs
argument_list|,
name|archiveDir
argument_list|)
expr_stmt|;
if|if
condition|(
name|archivedFiles
operator|.
name|size
argument_list|()
operator|>=
name|storeFiles
operator|.
name|size
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|storeFiles
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|archivedFiles
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Store files:"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|storeFiles
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|i
operator|+
literal|" - "
operator|+
name|storeFiles
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Archive files:"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|archivedFiles
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|i
operator|+
literal|" - "
operator|+
name|archivedFiles
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"Archived files are missing some of the store files!"
argument_list|,
name|archivedFiles
operator|.
name|containsAll
argument_list|(
name|storeFiles
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that the store files are archived when a column family is removed.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testArchiveOnTableFamilyDelete
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testArchiveOnTableFamilyDelete"
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
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
comment|// get the hfiles in the region
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
init|=
name|hrs
operator|.
name|getOnlineRegions
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"More that 1 region for test table."
argument_list|,
literal|1
argument_list|,
name|regions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|region
operator|=
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// wait for all the compactions to complete
name|region
operator|.
name|waitForFlushesAndCompactions
argument_list|()
expr_stmt|;
comment|// disable table to prevent new updates
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
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
literal|"Disabled table"
argument_list|)
expr_stmt|;
comment|// remove all the files from the archive to get a fair comparison
name|clearArchiveDirectory
argument_list|()
expr_stmt|;
comment|// then get the current store files
name|List
argument_list|<
name|String
argument_list|>
name|storeFiles
init|=
name|getRegionStoreFiles
argument_list|(
name|region
argument_list|)
decl_stmt|;
comment|// then delete the table so the hfiles get archived
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|deleteColumn
argument_list|(
name|TABLE_NAME
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
name|assertArchiveFiles
argument_list|(
name|fs
argument_list|,
name|storeFiles
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test HFileArchiver.resolveAndArchive() race condition HBASE-7643    */
annotation|@
name|Test
specifier|public
name|void
name|testCleaningRace
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|long
name|TEST_TIME
init|=
literal|20
operator|*
literal|1000
decl_stmt|;
specifier|final
name|ChoreService
name|choreService
init|=
operator|new
name|ChoreService
argument_list|(
literal|"TEST_SERVER_NAME"
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|Path
name|rootDir
init|=
name|UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|(
literal|"testCleaningRace"
argument_list|)
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
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|HConstants
operator|.
name|HFILE_ARCHIVE_DIRECTORY
argument_list|)
decl_stmt|;
name|Path
name|regionDir
init|=
operator|new
name|Path
argument_list|(
name|FSUtils
operator|.
name|getTableDir
argument_list|(
operator|new
name|Path
argument_list|(
literal|"./"
argument_list|)
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table"
argument_list|)
argument_list|)
argument_list|,
literal|"abcdef"
argument_list|)
decl_stmt|;
name|Path
name|familyDir
init|=
operator|new
name|Path
argument_list|(
name|regionDir
argument_list|,
literal|"cf"
argument_list|)
decl_stmt|;
name|Path
name|sourceRegionDir
init|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|regionDir
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|sourceRegionDir
argument_list|)
expr_stmt|;
name|Stoppable
name|stoppable
init|=
operator|new
name|StoppableImplementation
argument_list|()
decl_stmt|;
comment|// The cleaner should be looping without long pauses to reproduce the race condition.
name|HFileCleaner
name|cleaner
init|=
operator|new
name|HFileCleaner
argument_list|(
literal|1
argument_list|,
name|stoppable
argument_list|,
name|conf
argument_list|,
name|fs
argument_list|,
name|archiveDir
argument_list|)
decl_stmt|;
try|try
block|{
name|choreService
operator|.
name|scheduleChore
argument_list|(
name|cleaner
argument_list|)
expr_stmt|;
comment|// Keep creating/archiving new files while the cleaner is running in the other thread
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
for|for
control|(
name|long
name|fid
init|=
literal|0
init|;
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
operator|<
name|TEST_TIME
condition|;
operator|++
name|fid
control|)
block|{
name|Path
name|file
init|=
operator|new
name|Path
argument_list|(
name|familyDir
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|fid
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|sourceFile
init|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|file
argument_list|)
decl_stmt|;
name|Path
name|archiveFile
init|=
operator|new
name|Path
argument_list|(
name|archiveDir
argument_list|,
name|file
argument_list|)
decl_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
name|sourceFile
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Try to archive the file
name|HFileArchiver
operator|.
name|archiveRegion
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|sourceRegionDir
operator|.
name|getParent
argument_list|()
argument_list|,
name|sourceRegionDir
argument_list|)
expr_stmt|;
comment|// The archiver succeded, the file is no longer in the original location
comment|// but it's in the archive location.
name|LOG
operator|.
name|debug
argument_list|(
literal|"hfile="
operator|+
name|fid
operator|+
literal|" should be in the archive"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|archiveFile
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|sourceFile
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// The archiver is unable to archive the file. Probably HBASE-7643 race condition.
comment|// in this case, the file should not be archived, and we should have the file
comment|// in the original location.
name|LOG
operator|.
name|debug
argument_list|(
literal|"hfile="
operator|+
name|fid
operator|+
literal|" should be in the source location"
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|archiveFile
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|sourceFile
argument_list|)
argument_list|)
expr_stmt|;
comment|// Avoid to have this file in the next run
name|fs
operator|.
name|delete
argument_list|(
name|sourceFile
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|stoppable
operator|.
name|stop
argument_list|(
literal|"test end"
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|choreService
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|rootDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|clearArchiveDirectory
parameter_list|()
throws|throws
name|IOException
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
name|HConstants
operator|.
name|HFILE_ARCHIVE_DIRECTORY
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the names of all the files below the given directory    * @param fs    * @param archiveDir    * @return    * @throws IOException    */
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|getAllFileNames
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
name|Path
name|archiveDir
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
name|archiveDir
argument_list|,
operator|new
name|PathFilter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
if|if
condition|(
name|p
operator|.
name|getName
argument_list|()
operator|.
name|contains
argument_list|(
name|HConstants
operator|.
name|RECOVERED_EDITS_DIR
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
argument_list|)
decl_stmt|;
return|return
name|recurseOnFiles
argument_list|(
name|fs
argument_list|,
name|files
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|)
return|;
block|}
comment|/** Recursively lookup all the file names under the file[] array **/
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|recurseOnFiles
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|FileStatus
index|[]
name|files
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|fileNames
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|files
operator|==
literal|null
operator|||
name|files
operator|.
name|length
operator|==
literal|0
condition|)
return|return
name|fileNames
return|;
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
name|recurseOnFiles
argument_list|(
name|fs
argument_list|,
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|file
operator|.
name|getPath
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|,
name|fileNames
argument_list|)
expr_stmt|;
block|}
else|else
name|fileNames
operator|.
name|add
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|fileNames
return|;
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|getRegionStoreFiles
parameter_list|(
specifier|final
name|HRegion
name|region
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|regionDir
init|=
name|region
operator|.
name|getRegionFileSystem
argument_list|()
operator|.
name|getRegionDir
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|region
operator|.
name|getRegionFileSystem
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|storeFiles
init|=
name|getAllFileNames
argument_list|(
name|fs
argument_list|,
name|regionDir
argument_list|)
decl_stmt|;
comment|// remove all the non-storefile named files for the region
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|storeFiles
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|file
init|=
name|storeFiles
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|file
operator|.
name|contains
argument_list|(
name|HRegionFileSystem
operator|.
name|REGION_INFO_FILE
argument_list|)
operator|||
name|file
operator|.
name|contains
argument_list|(
literal|"wal"
argument_list|)
condition|)
block|{
name|storeFiles
operator|.
name|remove
argument_list|(
name|i
operator|--
argument_list|)
expr_stmt|;
block|}
block|}
name|storeFiles
operator|.
name|remove
argument_list|(
name|HRegionFileSystem
operator|.
name|REGION_INFO_FILE
argument_list|)
expr_stmt|;
return|return
name|storeFiles
return|;
block|}
block|}
end_class

end_unit

