begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
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
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|FSDataInputStream
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
name|RemoteExceptionHandler
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
name|hdfs
operator|.
name|DistributedFileSystem
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
name|hdfs
operator|.
name|protocol
operator|.
name|FSConstants
import|;
end_import

begin_comment
comment|/**  * Utility methods for interacting with the underlying file system.  */
end_comment

begin_class
specifier|public
class|class
name|FSUtils
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
name|FSUtils
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Not instantiable    */
specifier|private
name|FSUtils
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Delete if exists.    * @param fs    * @param dir    * @return True if deleted<code>dir</code>    * @throws IOException    */
specifier|public
specifier|static
name|boolean
name|deleteDirectory
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|dir
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fs
operator|.
name|exists
argument_list|(
name|dir
argument_list|)
condition|?
name|fs
operator|.
name|delete
argument_list|(
name|dir
argument_list|,
literal|true
argument_list|)
else|:
literal|false
return|;
block|}
comment|/**    * Check if directory exists.  If it does not, create it.    * @param fs     * @param dir    * @return Path    * @throws IOException    */
specifier|public
name|Path
name|checkdir
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|dir
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|dir
argument_list|)
condition|)
block|{
name|fs
operator|.
name|mkdirs
argument_list|(
name|dir
argument_list|)
expr_stmt|;
block|}
return|return
name|dir
return|;
block|}
comment|/**    * Create file.    * @param fs    * @param p    * @return Path    * @throws IOException    */
specifier|public
specifier|static
name|Path
name|create
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|p
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|p
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"File already exists "
operator|+
name|p
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|fs
operator|.
name|createNewFile
argument_list|(
name|p
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed create of "
operator|+
name|p
argument_list|)
throw|;
block|}
return|return
name|p
return|;
block|}
comment|/**    * Checks to see if the specified file system is available    *     * @param fs    * @throws IOException    */
specifier|public
specifier|static
name|void
name|checkFileSystemAvailable
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
operator|(
name|fs
operator|instanceof
name|DistributedFileSystem
operator|)
condition|)
block|{
return|return;
block|}
name|IOException
name|exception
init|=
literal|null
decl_stmt|;
name|DistributedFileSystem
name|dfs
init|=
operator|(
name|DistributedFileSystem
operator|)
name|fs
decl_stmt|;
try|try
block|{
if|if
condition|(
name|dfs
operator|.
name|exists
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/"
argument_list|)
argument_list|)
condition|)
block|{
return|return;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|exception
operator|=
name|RemoteExceptionHandler
operator|.
name|checkIOException
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|fs
operator|.
name|close
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
name|error
argument_list|(
literal|"file system close failed: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|IOException
name|io
init|=
operator|new
name|IOException
argument_list|(
literal|"File system is not available"
argument_list|)
decl_stmt|;
name|io
operator|.
name|initCause
argument_list|(
name|exception
argument_list|)
expr_stmt|;
throw|throw
name|io
throw|;
block|}
comment|/**    * Verifies current version of file system    *     * @param fs    * @param rootdir    * @return null if no version file exists, version string otherwise.    * @throws IOException    */
specifier|public
specifier|static
name|String
name|getVersion
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|rootdir
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|versionFile
init|=
operator|new
name|Path
argument_list|(
name|rootdir
argument_list|,
name|HConstants
operator|.
name|VERSION_FILE_NAME
argument_list|)
decl_stmt|;
name|String
name|version
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|versionFile
argument_list|)
condition|)
block|{
name|FSDataInputStream
name|s
init|=
name|fs
operator|.
name|open
argument_list|(
name|versionFile
argument_list|)
decl_stmt|;
try|try
block|{
name|version
operator|=
name|DataInputStream
operator|.
name|readUTF
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|version
return|;
block|}
comment|/**    * Verifies current version of file system    *     * @param fs file system    * @param rootdir root directory of HBase installation    * @param message if true, issues a message on System.out     *     * @throws IOException    */
specifier|public
specifier|static
name|void
name|checkVersion
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|rootdir
parameter_list|,
name|boolean
name|message
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|version
init|=
name|getVersion
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|)
decl_stmt|;
if|if
condition|(
name|version
operator|==
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|rootRegionExists
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|)
condition|)
block|{
comment|// rootDir is empty (no version file and no root region)
comment|// just create new version file (HBASE-1195)
name|FSUtils
operator|.
name|setVersion
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
elseif|else
if|if
condition|(
name|version
operator|.
name|compareTo
argument_list|(
name|HConstants
operator|.
name|FILE_SYSTEM_VERSION
argument_list|)
operator|==
literal|0
condition|)
return|return;
comment|// version is deprecated require migration
comment|// Output on stdout so user sees it in terminal.
name|String
name|msg
init|=
literal|"File system needs to be upgraded. Run "
operator|+
literal|"the '${HBASE_HOME}/bin/hbase migrate' script."
decl_stmt|;
if|if
condition|(
name|message
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"WARNING! "
operator|+
name|msg
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|FileSystemVersionException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
comment|/**    * Sets version of file system    *     * @param fs    * @param rootdir    * @throws IOException    */
specifier|public
specifier|static
name|void
name|setVersion
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|rootdir
parameter_list|)
throws|throws
name|IOException
block|{
name|setVersion
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|,
name|HConstants
operator|.
name|FILE_SYSTEM_VERSION
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sets version of file system    *     * @param fs    * @param rootdir    * @param version    * @throws IOException    */
specifier|public
specifier|static
name|void
name|setVersion
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|rootdir
parameter_list|,
name|String
name|version
parameter_list|)
throws|throws
name|IOException
block|{
name|FSDataOutputStream
name|s
init|=
name|fs
operator|.
name|create
argument_list|(
operator|new
name|Path
argument_list|(
name|rootdir
argument_list|,
name|HConstants
operator|.
name|VERSION_FILE_NAME
argument_list|)
argument_list|)
decl_stmt|;
name|s
operator|.
name|writeUTF
argument_list|(
name|version
argument_list|)
expr_stmt|;
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Created version file at "
operator|+
name|rootdir
operator|.
name|toString
argument_list|()
operator|+
literal|" set its version at:"
operator|+
name|version
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verifies root directory path is a valid URI with a scheme    *     * @param root root directory path    * @return Passed<code>root</code> argument.    * @throws IOException if not a valid URI with a scheme    */
specifier|public
specifier|static
name|Path
name|validateRootPath
parameter_list|(
name|Path
name|root
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|URI
name|rootURI
init|=
operator|new
name|URI
argument_list|(
name|root
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|scheme
init|=
name|rootURI
operator|.
name|getScheme
argument_list|()
decl_stmt|;
if|if
condition|(
name|scheme
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Root directory does not have a scheme"
argument_list|)
throw|;
block|}
return|return
name|root
return|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
name|IOException
name|io
init|=
operator|new
name|IOException
argument_list|(
literal|"Root directory path is not a valid "
operator|+
literal|"URI -- check your "
operator|+
name|HConstants
operator|.
name|HBASE_DIR
operator|+
literal|" configuration"
argument_list|)
decl_stmt|;
name|io
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
name|io
throw|;
block|}
block|}
comment|/**    * If DFS, check safe mode and if so, wait until we clear it.    * @param conf    * @param wait Sleep between retries    * @throws IOException    */
specifier|public
specifier|static
name|void
name|waitOnSafeMode
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|long
name|wait
parameter_list|)
throws|throws
name|IOException
block|{
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
if|if
condition|(
operator|!
operator|(
name|fs
operator|instanceof
name|DistributedFileSystem
operator|)
condition|)
return|return;
name|DistributedFileSystem
name|dfs
init|=
operator|(
name|DistributedFileSystem
operator|)
name|fs
decl_stmt|;
comment|// Are there any data nodes up yet?
comment|// Currently the safe mode check falls through if the namenode is up but no
comment|// datanodes have reported in yet.
try|try
block|{
while|while
condition|(
name|dfs
operator|.
name|getDataNodeStats
argument_list|()
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for dfs to come up..."
argument_list|)
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|wait
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|//continue
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// getDataNodeStats can fail if superuser privilege is required to run
comment|// the datanode report, just ignore it
block|}
comment|// Make sure dfs is not in safe mode
while|while
condition|(
name|dfs
operator|.
name|setSafeMode
argument_list|(
name|FSConstants
operator|.
name|SafeModeAction
operator|.
name|SAFEMODE_GET
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for dfs to exit safe mode..."
argument_list|)
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|wait
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|//continue
block|}
block|}
block|}
comment|/**    * Return the 'path' component of a Path.  In Hadoop, Path is an URI.  This    * method returns the 'path' component of a Path's URI: e.g. If a Path is    *<code>hdfs://example.org:9000/hbase_trunk/TestTable/compaction.dir</code>,    * this method returns<code>/hbase_trunk/TestTable/compaction.dir</code>.    * This method is useful if you want to print out a Path without qualifying    * Filesystem instance.    * @param p Filesystem Path whose 'path' component we are to return.    * @return Path portion of the Filesystem     */
specifier|public
specifier|static
name|String
name|getPath
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
return|return
name|p
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
return|;
block|}
comment|/**    * @param c    * @return Path to hbase root directory: i.e.<code>hbase.rootdir</code> from    * configuration as a Path.    * @throws IOException     */
specifier|public
specifier|static
name|Path
name|getRootDir
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|Path
argument_list|(
name|c
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Checks if root region exists    *     * @param fs file system    * @param rootdir root directory of HBase installation    * @return true if exists    * @throws IOException    */
specifier|public
specifier|static
name|boolean
name|rootRegionExists
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|rootdir
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|rootRegionDir
init|=
name|HRegion
operator|.
name|getRegionDir
argument_list|(
name|rootdir
argument_list|,
name|HRegionInfo
operator|.
name|ROOT_REGIONINFO
argument_list|)
decl_stmt|;
return|return
name|fs
operator|.
name|exists
argument_list|(
name|rootRegionDir
argument_list|)
return|;
block|}
comment|/**    * Runs through the hbase rootdir and checks all stores have only    * one file in them -- that is, they've been major compacted.  Looks    * at root and meta tables too.    * @param fs    * @param hbaseRootDir    * @return True if this hbase install is major compacted.    * @throws IOException    */
specifier|public
specifier|static
name|boolean
name|isMajorCompacted
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|hbaseRootDir
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Presumes any directory under hbase.rootdir is a table.
name|FileStatus
index|[]
name|tableDirs
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|hbaseRootDir
argument_list|,
operator|new
name|DirFilter
argument_list|(
name|fs
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|tableDirs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
comment|// Skip the .log directory.  All others should be tables.  Inside a table,
comment|// there are compaction.dir directories to skip.  Otherwise, all else
comment|// should be regions.  Then in each region, should only be family
comment|// directories.  Under each of these, should be one file only.
name|Path
name|d
init|=
name|tableDirs
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|d
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|FileStatus
index|[]
name|regionDirs
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|d
argument_list|,
operator|new
name|DirFilter
argument_list|(
name|fs
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|regionDirs
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|Path
name|dd
init|=
name|regionDirs
index|[
name|j
index|]
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|dd
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|HREGION_COMPACTIONDIR_NAME
argument_list|)
condition|)
block|{
continue|continue;
block|}
comment|// Else its a region name.  Now look in region for families.
name|FileStatus
index|[]
name|familyDirs
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|dd
argument_list|,
operator|new
name|DirFilter
argument_list|(
name|fs
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|familyDirs
operator|.
name|length
condition|;
name|k
operator|++
control|)
block|{
name|Path
name|family
init|=
name|familyDirs
index|[
name|k
index|]
operator|.
name|getPath
argument_list|()
decl_stmt|;
comment|// Now in family make sure only one file.
name|FileStatus
index|[]
name|familyStatus
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|family
argument_list|)
decl_stmt|;
if|if
condition|(
name|familyStatus
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|family
operator|.
name|toString
argument_list|()
operator|+
literal|" has "
operator|+
name|familyStatus
operator|.
name|length
operator|+
literal|" files."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
comment|/**    * Returns the total overall fragmentation percentage. Includes .META. and     * -ROOT- as well.    *      * @param master  The master defining the HBase root and file system.    * @return A map for each table and its percentage.    * @throws IOException When scanning the directory fails.    */
specifier|public
specifier|static
name|int
name|getTotalTableFragmentation
parameter_list|(
specifier|final
name|HMaster
name|master
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|map
init|=
name|getTableFragmentation
argument_list|(
name|master
argument_list|)
decl_stmt|;
return|return
name|map
operator|!=
literal|null
operator|&&
name|map
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|?
name|map
operator|.
name|get
argument_list|(
literal|"-TOTAL-"
argument_list|)
operator|.
name|intValue
argument_list|()
else|:
operator|-
literal|1
return|;
block|}
comment|/**    * Runs through the HBase rootdir and checks how many stores for each table    * have more than one file in them. Checks -ROOT- and .META. too. The total     * percentage across all tables is stored under the special key "-TOTAL-".     *     * @param master  The master defining the HBase root and file system.    * @return A map for each table and its percentage.    * @throws IOException When scanning the directory fails.    */
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|getTableFragmentation
parameter_list|(
specifier|final
name|HMaster
name|master
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|path
init|=
name|master
operator|.
name|getRootDir
argument_list|()
decl_stmt|;
comment|// since HMaster.getFileSystem() is package private
name|FileSystem
name|fs
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|master
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|getTableFragmentation
argument_list|(
name|fs
argument_list|,
name|path
argument_list|)
return|;
block|}
comment|/**    * Runs through the HBase rootdir and checks how many stores for each table    * have more than one file in them. Checks -ROOT- and .META. too. The total     * percentage across all tables is stored under the special key "-TOTAL-".     *     * @param fs  The file system to use.    * @param hbaseRootDir  The root directory to scan.    * @return A map for each table and its percentage.    * @throws IOException When scanning the directory fails.    */
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|getTableFragmentation
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|hbaseRootDir
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|frags
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|cfCountTotal
init|=
literal|0
decl_stmt|;
name|int
name|cfFragTotal
init|=
literal|0
decl_stmt|;
name|DirFilter
name|df
init|=
operator|new
name|DirFilter
argument_list|(
name|fs
argument_list|)
decl_stmt|;
comment|// presumes any directory under hbase.rootdir is a table
name|FileStatus
index|[]
name|tableDirs
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|hbaseRootDir
argument_list|,
name|df
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|tableDirs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
comment|// Skip the .log directory.  All others should be tables.  Inside a table,
comment|// there are compaction.dir directories to skip.  Otherwise, all else
comment|// should be regions.  Then in each region, should only be family
comment|// directories.  Under each of these, should be one file only.
name|Path
name|d
init|=
name|tableDirs
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|d
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|int
name|cfCount
init|=
literal|0
decl_stmt|;
name|int
name|cfFrag
init|=
literal|0
decl_stmt|;
name|FileStatus
index|[]
name|regionDirs
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|d
argument_list|,
name|df
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|regionDirs
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|Path
name|dd
init|=
name|regionDirs
index|[
name|j
index|]
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|dd
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|HREGION_COMPACTIONDIR_NAME
argument_list|)
condition|)
block|{
continue|continue;
block|}
comment|// else its a region name, now look in region for families
name|FileStatus
index|[]
name|familyDirs
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|dd
argument_list|,
name|df
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|familyDirs
operator|.
name|length
condition|;
name|k
operator|++
control|)
block|{
name|cfCount
operator|++
expr_stmt|;
name|cfCountTotal
operator|++
expr_stmt|;
name|Path
name|family
init|=
name|familyDirs
index|[
name|k
index|]
operator|.
name|getPath
argument_list|()
decl_stmt|;
comment|// now in family make sure only one file
name|FileStatus
index|[]
name|familyStatus
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|family
argument_list|)
decl_stmt|;
if|if
condition|(
name|familyStatus
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|cfFrag
operator|++
expr_stmt|;
name|cfFragTotal
operator|++
expr_stmt|;
block|}
block|}
block|}
comment|// compute percentage per table and store in result list
name|frags
operator|.
name|put
argument_list|(
name|d
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|Integer
argument_list|(
name|Math
operator|.
name|round
argument_list|(
operator|(
name|float
operator|)
name|cfFrag
operator|/
name|cfCount
operator|*
literal|100
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// set overall percentage for all tables
name|frags
operator|.
name|put
argument_list|(
literal|"-TOTAL-"
argument_list|,
operator|new
name|Integer
argument_list|(
name|Math
operator|.
name|round
argument_list|(
operator|(
name|float
operator|)
name|cfFragTotal
operator|/
name|cfCountTotal
operator|*
literal|100
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|frags
return|;
block|}
comment|/**    * Expects to find -ROOT- directory.    * @param fs    * @param hbaseRootDir    * @return True if this a pre020 layout.    * @throws IOException    */
specifier|public
specifier|static
name|boolean
name|isPre020FileLayout
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|hbaseRootDir
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|mapfiles
init|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|hbaseRootDir
argument_list|,
literal|"-ROOT-"
argument_list|)
argument_list|,
literal|"70236052"
argument_list|)
argument_list|,
literal|"info"
argument_list|)
argument_list|,
literal|"mapfiles"
argument_list|)
decl_stmt|;
return|return
name|fs
operator|.
name|exists
argument_list|(
name|mapfiles
argument_list|)
return|;
block|}
comment|/**    * Runs through the hbase rootdir and checks all stores have only    * one file in them -- that is, they've been major compacted.  Looks    * at root and meta tables too.  This version differs from    * {@link #isMajorCompacted(FileSystem, Path)} in that it expects a    * pre-0.20.0 hbase layout on the filesystem.  Used migrating.    * @param fs    * @param hbaseRootDir    * @return True if this hbase install is major compacted.    * @throws IOException    */
specifier|public
specifier|static
name|boolean
name|isMajorCompactedPre020
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|hbaseRootDir
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Presumes any directory under hbase.rootdir is a table.
name|FileStatus
index|[]
name|tableDirs
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|hbaseRootDir
argument_list|,
operator|new
name|DirFilter
argument_list|(
name|fs
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|tableDirs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
comment|// Inside a table, there are compaction.dir directories to skip.
comment|// Otherwise, all else should be regions.  Then in each region, should
comment|// only be family directories.  Under each of these, should be a mapfile
comment|// and info directory and in these only one file.
name|Path
name|d
init|=
name|tableDirs
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|d
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|FileStatus
index|[]
name|regionDirs
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|d
argument_list|,
operator|new
name|DirFilter
argument_list|(
name|fs
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|regionDirs
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|Path
name|dd
init|=
name|regionDirs
index|[
name|j
index|]
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|dd
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|HREGION_COMPACTIONDIR_NAME
argument_list|)
condition|)
block|{
continue|continue;
block|}
comment|// Else its a region name.  Now look in region for families.
name|FileStatus
index|[]
name|familyDirs
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|dd
argument_list|,
operator|new
name|DirFilter
argument_list|(
name|fs
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|familyDirs
operator|.
name|length
condition|;
name|k
operator|++
control|)
block|{
name|Path
name|family
init|=
name|familyDirs
index|[
name|k
index|]
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|FileStatus
index|[]
name|infoAndMapfile
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|family
argument_list|)
decl_stmt|;
comment|// Assert that only info and mapfile in family dir.
if|if
condition|(
name|infoAndMapfile
operator|.
name|length
operator|!=
literal|0
operator|&&
name|infoAndMapfile
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|family
operator|.
name|toString
argument_list|()
operator|+
literal|" has more than just info and mapfile: "
operator|+
name|infoAndMapfile
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// Make sure directory named info or mapfile.
for|for
control|(
name|int
name|ll
init|=
literal|0
init|;
name|ll
operator|<
literal|2
condition|;
name|ll
operator|++
control|)
block|{
if|if
condition|(
name|infoAndMapfile
index|[
name|ll
index|]
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"info"
argument_list|)
operator|||
name|infoAndMapfile
index|[
name|ll
index|]
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"mapfiles"
argument_list|)
condition|)
continue|continue;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Unexpected directory name: "
operator|+
name|infoAndMapfile
index|[
name|ll
index|]
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// Now in family, there are 'mapfile' and 'info' subdirs.  Just
comment|// look in the 'mapfile' subdir.
name|FileStatus
index|[]
name|familyStatus
init|=
name|fs
operator|.
name|listStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|family
argument_list|,
literal|"mapfiles"
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|familyStatus
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|family
operator|.
name|toString
argument_list|()
operator|+
literal|" has "
operator|+
name|familyStatus
operator|.
name|length
operator|+
literal|" files."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
comment|/**    * A {@link PathFilter} that returns directories.    */
specifier|public
specifier|static
class|class
name|DirFilter
implements|implements
name|PathFilter
block|{
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
specifier|public
name|DirFilter
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|)
block|{
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
block|}
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
name|boolean
name|isdir
init|=
literal|false
decl_stmt|;
try|try
block|{
name|isdir
operator|=
name|this
operator|.
name|fs
operator|.
name|getFileStatus
argument_list|(
name|p
argument_list|)
operator|.
name|isDir
argument_list|()
expr_stmt|;
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
name|isdir
return|;
block|}
block|}
block|}
end_class

end_unit

