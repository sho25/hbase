begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Map
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
name|locks
operator|.
name|Lock
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
name|locks
operator|.
name|ReentrantLock
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
name|HServerInfo
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
name|master
operator|.
name|metrics
operator|.
name|MasterMetrics
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
name|regionserver
operator|.
name|wal
operator|.
name|HLog
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
name|wal
operator|.
name|HLogSplitter
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

begin_comment
comment|/**  * This class abstracts a bunch of operations the HMaster needs to interact with  * the underlying file system, including splitting log files, checking file  * system status, etc.  */
end_comment

begin_class
specifier|public
class|class
name|MasterFileSystem
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
name|MasterFileSystem
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// HBase configuration
name|Configuration
name|conf
decl_stmt|;
comment|// master status
name|Server
name|master
decl_stmt|;
comment|// metrics for master
name|MasterMetrics
name|metrics
decl_stmt|;
comment|// Keep around for convenience.
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
comment|// Is the fileystem ok?
specifier|private
specifier|volatile
name|boolean
name|fsOk
init|=
literal|true
decl_stmt|;
comment|// The Path to the old logs dir
specifier|private
specifier|final
name|Path
name|oldLogDir
decl_stmt|;
comment|// root hbase directory on the FS
specifier|private
specifier|final
name|Path
name|rootdir
decl_stmt|;
comment|// create the split log lock
specifier|final
name|Lock
name|splitLogLock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
specifier|public
name|MasterFileSystem
parameter_list|(
name|Server
name|master
parameter_list|,
name|MasterMetrics
name|metrics
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|conf
operator|=
name|master
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
name|this
operator|.
name|metrics
operator|=
name|metrics
expr_stmt|;
comment|// Set filesystem to be that of this.rootdir else we get complaints about
comment|// mismatched filesystems if hbase.rootdir is hdfs and fs.defaultFS is
comment|// default localfs.  Presumption is that rootdir is fully-qualified before
comment|// we get to here with appropriate fs scheme.
name|this
operator|.
name|rootdir
operator|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// Cover both bases, the old way of setting default fs and the new.
comment|// We're supposed to run on 0.20 and 0.21 anyways.
name|conf
operator|.
name|set
argument_list|(
literal|"fs.default.name"
argument_list|,
name|this
operator|.
name|rootdir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"fs.defaultFS"
argument_list|,
name|this
operator|.
name|rootdir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// setup the filesystem variable
name|this
operator|.
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// set up the archived logs path
name|this
operator|.
name|oldLogDir
operator|=
operator|new
name|Path
argument_list|(
name|this
operator|.
name|rootdir
argument_list|,
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
argument_list|)
expr_stmt|;
name|createInitialFileSystemLayout
argument_list|()
expr_stmt|;
block|}
comment|/**    * Create initial layout in filesystem.    *<ol>    *<li>Check if the root region exists and is readable, if not create it.    * Create hbase.version and the -ROOT- directory if not one.    *</li>    *<li>Create a log archive directory for RS to put archived logs</li>    *</ol>    * Idempotent.    */
specifier|private
name|void
name|createInitialFileSystemLayout
parameter_list|()
throws|throws
name|IOException
block|{
comment|// check if the root directory exists
name|checkRootDir
argument_list|(
name|this
operator|.
name|rootdir
argument_list|,
name|conf
argument_list|,
name|this
operator|.
name|fs
argument_list|)
expr_stmt|;
comment|// Make sure the region servers can archive their old logs
if|if
condition|(
operator|!
name|this
operator|.
name|fs
operator|.
name|exists
argument_list|(
name|this
operator|.
name|oldLogDir
argument_list|)
condition|)
block|{
name|this
operator|.
name|fs
operator|.
name|mkdirs
argument_list|(
name|this
operator|.
name|oldLogDir
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|FileSystem
name|getFileSystem
parameter_list|()
block|{
return|return
name|this
operator|.
name|fs
return|;
block|}
comment|/**    * Get the directory where old logs go    * @return the dir    */
specifier|public
name|Path
name|getOldLogDir
parameter_list|()
block|{
return|return
name|this
operator|.
name|oldLogDir
return|;
block|}
comment|/**    * Checks to see if the file system is still accessible.    * If not, sets closed    * @return false if file system is not available    */
specifier|public
name|boolean
name|checkFileSystem
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|fsOk
condition|)
block|{
try|try
block|{
name|FSUtils
operator|.
name|checkFileSystemAvailable
argument_list|(
name|this
operator|.
name|fs
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|master
operator|.
name|abort
argument_list|(
literal|"Shutting down HBase cluster: file system not available"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|this
operator|.
name|fsOk
operator|=
literal|false
expr_stmt|;
block|}
block|}
return|return
name|this
operator|.
name|fsOk
return|;
block|}
comment|/**    * @return HBase root dir.    * @throws IOException    */
specifier|public
name|Path
name|getRootDir
parameter_list|()
block|{
return|return
name|this
operator|.
name|rootdir
return|;
block|}
comment|/**    * Inspect the log directory to recover any log file without    * an active region server.    * @param onlineServers Map of online servers keyed by    * {@link HServerInfo#getServerName()}    */
name|void
name|splitLogAfterStartup
parameter_list|(
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|HServerInfo
argument_list|>
name|onlineServers
parameter_list|)
block|{
name|Path
name|logsDirPath
init|=
operator|new
name|Path
argument_list|(
name|this
operator|.
name|rootdir
argument_list|,
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|fs
operator|.
name|exists
argument_list|(
name|logsDirPath
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
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed exists test on "
operator|+
name|logsDirPath
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|FileStatus
index|[]
name|logFolders
decl_stmt|;
try|try
block|{
name|logFolders
operator|=
name|this
operator|.
name|fs
operator|.
name|listStatus
argument_list|(
name|logsDirPath
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed listing "
operator|+
name|logsDirPath
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|logFolders
operator|==
literal|null
operator|||
name|logFolders
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"No log files to split, proceeding..."
argument_list|)
expr_stmt|;
return|return;
block|}
for|for
control|(
name|FileStatus
name|status
range|:
name|logFolders
control|)
block|{
name|String
name|serverName
init|=
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|onlineServers
operator|.
name|get
argument_list|(
name|serverName
argument_list|)
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Log folder "
operator|+
name|status
operator|.
name|getPath
argument_list|()
operator|+
literal|" doesn't belong "
operator|+
literal|"to a known region server, splitting"
argument_list|)
expr_stmt|;
name|splitLog
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Log folder "
operator|+
name|status
operator|.
name|getPath
argument_list|()
operator|+
literal|" belongs to an existing region server"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|splitLog
parameter_list|(
specifier|final
name|String
name|serverName
parameter_list|)
block|{
name|this
operator|.
name|splitLogLock
operator|.
name|lock
argument_list|()
expr_stmt|;
name|long
name|splitTime
init|=
literal|0
decl_stmt|,
name|splitLogSize
init|=
literal|0
decl_stmt|;
name|Path
name|logDir
init|=
operator|new
name|Path
argument_list|(
name|this
operator|.
name|rootdir
argument_list|,
name|HLog
operator|.
name|getHLogDirectoryName
argument_list|(
name|serverName
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|HLogSplitter
name|splitter
init|=
name|HLogSplitter
operator|.
name|createLogSplitter
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|splitter
operator|.
name|splitLog
argument_list|(
name|this
operator|.
name|rootdir
argument_list|,
name|logDir
argument_list|,
name|oldLogDir
argument_list|,
name|this
operator|.
name|fs
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|splitTime
operator|=
name|splitter
operator|.
name|getTime
argument_list|()
expr_stmt|;
name|splitLogSize
operator|=
name|splitter
operator|.
name|getSize
argument_list|()
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
name|error
argument_list|(
literal|"Failed splitting "
operator|+
name|logDir
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|this
operator|.
name|splitLogLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|metrics
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|metrics
operator|.
name|addSplit
argument_list|(
name|splitTime
argument_list|,
name|splitLogSize
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Get the rootdir.  Make sure its wholesome and exists before returning.    * @param rd    * @param conf    * @param fs    * @return hbase.rootdir (after checks for existence and bootstrapping if    * needed populating the directory with necessary bootup files).    * @throws IOException    */
specifier|private
specifier|static
name|Path
name|checkRootDir
parameter_list|(
specifier|final
name|Path
name|rd
parameter_list|,
specifier|final
name|Configuration
name|c
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
comment|// If FS is in safe mode wait till out of it.
name|FSUtils
operator|.
name|waitOnSafeMode
argument_list|(
name|c
argument_list|,
name|c
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|THREAD_WAKE_FREQUENCY
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
argument_list|)
expr_stmt|;
comment|// Filesystem is good. Go ahead and check for hbase.rootdir.
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|rd
argument_list|)
condition|)
block|{
name|fs
operator|.
name|mkdirs
argument_list|(
name|rd
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|setVersion
argument_list|(
name|fs
argument_list|,
name|rd
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|FSUtils
operator|.
name|checkVersion
argument_list|(
name|fs
argument_list|,
name|rd
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// Make sure the root region directory exists!
if|if
condition|(
operator|!
name|FSUtils
operator|.
name|rootRegionExists
argument_list|(
name|fs
argument_list|,
name|rd
argument_list|)
condition|)
block|{
name|bootstrap
argument_list|(
name|rd
argument_list|,
name|c
argument_list|)
expr_stmt|;
block|}
return|return
name|rd
return|;
block|}
specifier|private
specifier|static
name|void
name|bootstrap
parameter_list|(
specifier|final
name|Path
name|rd
parameter_list|,
specifier|final
name|Configuration
name|c
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"BOOTSTRAP: creating ROOT and first META regions"
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Bootstrapping, make sure blockcache is off.  Else, one will be
comment|// created here in bootstap and it'll need to be cleaned up.  Better to
comment|// not make it in first place.  Turn off block caching for bootstrap.
comment|// Enable after.
name|HRegionInfo
name|rootHRI
init|=
operator|new
name|HRegionInfo
argument_list|(
name|HRegionInfo
operator|.
name|ROOT_REGIONINFO
argument_list|)
decl_stmt|;
name|setInfoFamilyCaching
argument_list|(
name|rootHRI
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|HRegionInfo
name|metaHRI
init|=
operator|new
name|HRegionInfo
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
decl_stmt|;
name|setInfoFamilyCaching
argument_list|(
name|metaHRI
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|HRegion
name|root
init|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|rootHRI
argument_list|,
name|rd
argument_list|,
name|c
argument_list|)
decl_stmt|;
name|HRegion
name|meta
init|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|metaHRI
argument_list|,
name|rd
argument_list|,
name|c
argument_list|)
decl_stmt|;
name|setInfoFamilyCaching
argument_list|(
name|rootHRI
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|setInfoFamilyCaching
argument_list|(
name|metaHRI
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// Add first region from the META table to the ROOT region.
name|HRegion
operator|.
name|addRegionToMETA
argument_list|(
name|root
argument_list|,
name|meta
argument_list|)
expr_stmt|;
name|root
operator|.
name|close
argument_list|()
expr_stmt|;
name|root
operator|.
name|getLog
argument_list|()
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
name|meta
operator|.
name|close
argument_list|()
expr_stmt|;
name|meta
operator|.
name|getLog
argument_list|()
operator|.
name|closeAndDelete
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
operator|=
name|RemoteExceptionHandler
operator|.
name|checkIOException
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"bootstrap"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
comment|/**    * @param hri Set all family block caching to<code>b</code>    * @param b    */
specifier|private
specifier|static
name|void
name|setInfoFamilyCaching
parameter_list|(
specifier|final
name|HRegionInfo
name|hri
parameter_list|,
specifier|final
name|boolean
name|b
parameter_list|)
block|{
for|for
control|(
name|HColumnDescriptor
name|hcd
range|:
name|hri
operator|.
name|getTableDesc
argument_list|()
operator|.
name|families
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|hcd
operator|.
name|getName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
condition|)
block|{
name|hcd
operator|.
name|setBlockCacheEnabled
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setInMemory
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|deleteRegion
parameter_list|(
name|HRegionInfo
name|region
parameter_list|)
throws|throws
name|IOException
block|{
name|fs
operator|.
name|delete
argument_list|(
name|HRegion
operator|.
name|getRegionDir
argument_list|(
name|rootdir
argument_list|,
name|region
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|deleteTable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|fs
operator|.
name|delete
argument_list|(
operator|new
name|Path
argument_list|(
name|rootdir
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateRegionInfo
parameter_list|(
name|HRegionInfo
name|region
parameter_list|)
block|{
comment|// TODO implement this.  i think this is currently broken in trunk i don't
comment|//      see this getting updated.
comment|//      @see HRegion.checkRegioninfoOnFilesystem()
block|}
specifier|public
name|void
name|deleteFamily
parameter_list|(
name|HRegionInfo
name|region
parameter_list|,
name|byte
index|[]
name|familyName
parameter_list|)
throws|throws
name|IOException
block|{
name|fs
operator|.
name|delete
argument_list|(
name|Store
operator|.
name|getStoreHomedir
argument_list|(
operator|new
name|Path
argument_list|(
name|rootdir
argument_list|,
name|region
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|,
name|region
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|familyName
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

