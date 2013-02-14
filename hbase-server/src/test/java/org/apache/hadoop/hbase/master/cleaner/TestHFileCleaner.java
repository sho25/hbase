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
operator|.
name|cleaner
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
name|Server
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
name|catalog
operator|.
name|CatalogTracker
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
name|EnvironmentEdge
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
name|EnvironmentEdgeManager
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
name|TestHFileCleaner
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
name|TestHFileCleaner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
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
comment|// have to use a minidfs cluster because the localfs doesn't modify file times correctly
name|UTIL
operator|.
name|startMiniDFSCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|shutdownCluster
parameter_list|()
throws|throws
name|IOException
block|{
name|UTIL
operator|.
name|shutdownMiniDFSCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTTLCleaner
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|FileSystem
name|fs
init|=
name|UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|Path
name|root
init|=
name|UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|()
decl_stmt|;
name|Path
name|file
init|=
operator|new
name|Path
argument_list|(
name|root
argument_list|,
literal|"file"
argument_list|)
decl_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
name|file
argument_list|)
expr_stmt|;
name|long
name|createTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Test file not created!"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|file
argument_list|)
argument_list|)
expr_stmt|;
name|TimeToLiveHFileCleaner
name|cleaner
init|=
operator|new
name|TimeToLiveHFileCleaner
argument_list|()
decl_stmt|;
comment|// update the time info for the file, so the cleaner removes it
name|fs
operator|.
name|setTimes
argument_list|(
name|file
argument_list|,
name|createTime
operator|-
literal|100
argument_list|,
operator|-
literal|1
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
name|conf
operator|.
name|setLong
argument_list|(
name|TimeToLiveHFileCleaner
operator|.
name|TTL_CONF_KEY
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"File not set deletable - check mod time:"
operator|+
name|getFileStats
argument_list|(
name|file
argument_list|,
name|fs
argument_list|)
operator|+
literal|" with create time:"
operator|+
name|createTime
argument_list|,
name|cleaner
operator|.
name|isFileDeletable
argument_list|(
name|file
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param file to check    * @return loggable information about the file    */
specifier|private
name|String
name|getFileStats
parameter_list|(
name|Path
name|file
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
name|FileStatus
name|status
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|file
argument_list|)
decl_stmt|;
return|return
literal|"File"
operator|+
name|file
operator|+
literal|", mtime:"
operator|+
name|status
operator|.
name|getModificationTime
argument_list|()
operator|+
literal|", atime:"
operator|+
name|status
operator|.
name|getAccessTime
argument_list|()
return|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60
operator|*
literal|1000
argument_list|)
specifier|public
name|void
name|testHFileCleaning
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|EnvironmentEdge
name|originalEdge
init|=
name|EnvironmentEdgeManager
operator|.
name|getDelegate
argument_list|()
decl_stmt|;
name|String
name|prefix
init|=
literal|"someHFileThatWouldBeAUUID"
decl_stmt|;
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// set TTL
name|long
name|ttl
init|=
literal|2000
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HFileCleaner
operator|.
name|MASTER_HFILE_CLEANER_PLUGINS
argument_list|,
literal|"org.apache.hadoop.hbase.master.cleaner.TimeToLiveHFileCleaner"
argument_list|)
expr_stmt|;
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
name|Server
name|server
init|=
operator|new
name|DummyServer
argument_list|()
decl_stmt|;
name|Path
name|archivedHfileDir
init|=
operator|new
name|Path
argument_list|(
name|UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|()
argument_list|,
name|HConstants
operator|.
name|HFILE_ARCHIVE_DIRECTORY
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|HFileCleaner
name|cleaner
init|=
operator|new
name|HFileCleaner
argument_list|(
literal|1000
argument_list|,
name|server
argument_list|,
name|conf
argument_list|,
name|fs
argument_list|,
name|archivedHfileDir
argument_list|)
decl_stmt|;
comment|// Create 2 invalid files, 1 "recent" file, 1 very new file and 30 old files
specifier|final
name|long
name|createTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|archivedHfileDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|archivedHfileDir
argument_list|)
expr_stmt|;
comment|// Case 1: 1 invalid file, which should be deleted directly
name|fs
operator|.
name|createNewFile
argument_list|(
operator|new
name|Path
argument_list|(
name|archivedHfileDir
argument_list|,
literal|"dfd-dfd"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Case 2: 1 "recent" file, not even deletable for the first log cleaner
comment|// (TimeToLiveLogCleaner), so we are not going down the chain
name|LOG
operator|.
name|debug
argument_list|(
literal|"Now is: "
operator|+
name|createTime
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
literal|32
condition|;
name|i
operator|++
control|)
block|{
comment|// Case 3: old files which would be deletable for the first log cleaner
comment|// (TimeToLiveHFileCleaner),
name|Path
name|fileName
init|=
operator|new
name|Path
argument_list|(
name|archivedHfileDir
argument_list|,
operator|(
name|prefix
operator|+
literal|"."
operator|+
operator|(
name|createTime
operator|+
name|i
operator|)
operator|)
argument_list|)
decl_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
name|fileName
argument_list|)
expr_stmt|;
comment|// set the creation time past ttl to ensure that it gets removed
name|fs
operator|.
name|setTimes
argument_list|(
name|fileName
argument_list|,
name|createTime
operator|-
name|ttl
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating "
operator|+
name|getFileStats
argument_list|(
name|fileName
argument_list|,
name|fs
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Case 2: 1 newer file, not even deletable for the first log cleaner
comment|// (TimeToLiveLogCleaner), so we are not going down the chain
name|Path
name|saved
init|=
operator|new
name|Path
argument_list|(
name|archivedHfileDir
argument_list|,
name|prefix
operator|+
literal|".00000000000"
argument_list|)
decl_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
name|saved
argument_list|)
expr_stmt|;
comment|// set creation time within the ttl
name|fs
operator|.
name|setTimes
argument_list|(
name|saved
argument_list|,
name|createTime
operator|-
name|ttl
operator|/
literal|2
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating "
operator|+
name|getFileStats
argument_list|(
name|saved
argument_list|,
name|fs
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|FileStatus
name|stat
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|archivedHfileDir
argument_list|)
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|stat
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|33
argument_list|,
name|fs
operator|.
name|listStatus
argument_list|(
name|archivedHfileDir
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// set a custom edge manager to handle time checking
name|EnvironmentEdge
name|setTime
init|=
operator|new
name|EnvironmentEdge
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|long
name|currentTimeMillis
parameter_list|()
block|{
return|return
name|createTime
return|;
block|}
block|}
decl_stmt|;
name|EnvironmentEdgeManager
operator|.
name|injectEdge
argument_list|(
name|setTime
argument_list|)
expr_stmt|;
comment|// run the chore
name|cleaner
operator|.
name|chore
argument_list|()
expr_stmt|;
comment|// ensure we only end up with the saved file
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|fs
operator|.
name|listStatus
argument_list|(
name|archivedHfileDir
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|archivedHfileDir
argument_list|)
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Kept hfiles: "
operator|+
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
name|cleaner
operator|.
name|interrupt
argument_list|()
expr_stmt|;
comment|// reset the edge back to the original edge
name|EnvironmentEdgeManager
operator|.
name|injectEdge
argument_list|(
name|originalEdge
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRemovesEmptyDirectories
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// no cleaner policies = delete all files
name|conf
operator|.
name|setStrings
argument_list|(
name|HFileCleaner
operator|.
name|MASTER_HFILE_CLEANER_PLUGINS
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|Server
name|server
init|=
operator|new
name|DummyServer
argument_list|()
decl_stmt|;
name|Path
name|archivedHfileDir
init|=
operator|new
name|Path
argument_list|(
name|UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|()
argument_list|,
name|HConstants
operator|.
name|HFILE_ARCHIVE_DIRECTORY
argument_list|)
decl_stmt|;
comment|// setup the cleaner
name|FileSystem
name|fs
init|=
name|UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|HFileCleaner
name|cleaner
init|=
operator|new
name|HFileCleaner
argument_list|(
literal|1000
argument_list|,
name|server
argument_list|,
name|conf
argument_list|,
name|fs
argument_list|,
name|archivedHfileDir
argument_list|)
decl_stmt|;
comment|// make all the directories for archiving files
name|Path
name|table
init|=
operator|new
name|Path
argument_list|(
name|archivedHfileDir
argument_list|,
literal|"table"
argument_list|)
decl_stmt|;
name|Path
name|region
init|=
operator|new
name|Path
argument_list|(
name|table
argument_list|,
literal|"regionsomthing"
argument_list|)
decl_stmt|;
name|Path
name|family
init|=
operator|new
name|Path
argument_list|(
name|region
argument_list|,
literal|"fam"
argument_list|)
decl_stmt|;
name|Path
name|file
init|=
operator|new
name|Path
argument_list|(
name|family
argument_list|,
literal|"file12345"
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|family
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|family
argument_list|)
condition|)
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Couldn't create test family:"
operator|+
name|family
argument_list|)
throw|;
name|fs
operator|.
name|create
argument_list|(
name|file
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|file
argument_list|)
condition|)
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Test file didn't get created:"
operator|+
name|file
argument_list|)
throw|;
comment|// run the chore to cleanup the files (and the directories above it)
name|cleaner
operator|.
name|chore
argument_list|()
expr_stmt|;
comment|// make sure all the parent directories get removed
name|assertFalse
argument_list|(
literal|"family directory not removed for empty directory"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"region directory not removed for empty directory"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|region
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"table directory not removed for empty directory"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"archive directory"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|archivedHfileDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|static
class|class
name|DummyServer
implements|implements
name|Server
block|{
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|UTIL
operator|.
name|getConfiguration
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ZooKeeperWatcher
name|getZooKeeper
parameter_list|()
block|{
try|try
block|{
return|return
operator|new
name|ZooKeeperWatcher
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
literal|"dummy server"
argument_list|,
name|this
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|CatalogTracker
name|getCatalogTracker
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
operator|new
name|ServerName
argument_list|(
literal|"regionserver,60020,000000"
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{     }
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{     }
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

