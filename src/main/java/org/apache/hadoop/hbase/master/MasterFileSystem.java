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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|UUID
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
name|InvalidFamilyOperationException
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
name|regionserver
operator|.
name|wal
operator|.
name|OrphanHLogAfterSplitException
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
name|util
operator|.
name|FSTableDescriptors
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
comment|// Persisted unique cluster ID
specifier|private
name|String
name|clusterId
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
specifier|final
name|boolean
name|distributedLogSplitting
decl_stmt|;
specifier|final
name|SplitLogManager
name|splitLogManager
decl_stmt|;
specifier|private
specifier|final
name|MasterServices
name|services
decl_stmt|;
specifier|public
name|MasterFileSystem
parameter_list|(
name|Server
name|master
parameter_list|,
name|MasterServices
name|services
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
name|services
operator|=
name|services
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
name|this
operator|.
name|fs
operator|=
name|this
operator|.
name|rootdir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|String
name|fsUri
init|=
name|this
operator|.
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"fs.default.name"
argument_list|,
name|fsUri
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"fs.defaultFS"
argument_list|,
name|fsUri
argument_list|)
expr_stmt|;
name|this
operator|.
name|distributedLogSplitting
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"hbase.master.distributed.log.splitting"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|distributedLogSplitting
condition|)
block|{
name|this
operator|.
name|splitLogManager
operator|=
operator|new
name|SplitLogManager
argument_list|(
name|master
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|master
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|master
argument_list|,
name|master
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|splitLogManager
operator|.
name|finishInitialization
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|splitLogManager
operator|=
literal|null
expr_stmt|;
block|}
comment|// setup the filesystem variable
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
name|FSUtils
operator|.
name|checkDfsSafeMode
argument_list|(
name|this
operator|.
name|conf
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
comment|/**    * @return HBase root dir.    */
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
comment|/**    * @return The unique identifier generated for this cluster    */
specifier|public
name|String
name|getClusterId
parameter_list|()
block|{
return|return
name|clusterId
return|;
block|}
comment|/**    * Inspect the log directory to recover any log file without    * an active region server.    * @param onlineServers Set of online servers keyed by    * {@link ServerName}    */
name|void
name|splitLogAfterStartup
parameter_list|(
specifier|final
name|Set
argument_list|<
name|ServerName
argument_list|>
name|onlineServers
parameter_list|)
block|{
name|boolean
name|retrySplitting
init|=
operator|!
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"hbase.hlog.split.skip.errors"
argument_list|,
name|HLog
operator|.
name|SPLIT_SKIP_ERRORS_DEFAULT
argument_list|)
decl_stmt|;
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
do|do
block|{
name|List
argument_list|<
name|ServerName
argument_list|>
name|serverNames
init|=
operator|new
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
argument_list|()
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
return|return;
name|FileStatus
index|[]
name|logFolders
init|=
name|this
operator|.
name|fs
operator|.
name|listStatus
argument_list|(
name|logsDirPath
argument_list|)
decl_stmt|;
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
name|sn
init|=
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|// truncate splitting suffix if present (for ServerName parsing)
if|if
condition|(
name|sn
operator|.
name|endsWith
argument_list|(
name|HLog
operator|.
name|SPLITTING_EXT
argument_list|)
condition|)
block|{
name|sn
operator|=
name|sn
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|sn
operator|.
name|length
argument_list|()
operator|-
name|HLog
operator|.
name|SPLITTING_EXT
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ServerName
name|serverName
init|=
name|ServerName
operator|.
name|parseServerName
argument_list|(
name|sn
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|onlineServers
operator|.
name|contains
argument_list|(
name|serverName
argument_list|)
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
name|serverNames
operator|.
name|add
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
name|splitLog
argument_list|(
name|serverNames
argument_list|)
expr_stmt|;
name|retrySplitting
operator|=
literal|false
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
literal|"Failed splitting of "
operator|+
name|serverNames
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|checkFileSystem
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Bad Filesystem, exiting"
argument_list|)
expr_stmt|;
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|halt
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|retrySplitting
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.hlog.split.failure.retry.interval"
argument_list|,
literal|30
operator|*
literal|1000
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Interrupted, returning w/o splitting at startup"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
name|retrySplitting
operator|=
literal|false
expr_stmt|;
block|}
block|}
block|}
do|while
condition|(
name|retrySplitting
condition|)
do|;
block|}
specifier|public
name|void
name|splitLog
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|ServerName
argument_list|>
name|serverNames
init|=
operator|new
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
name|serverNames
operator|.
name|add
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
name|splitLog
argument_list|(
name|serverNames
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|splitLog
parameter_list|(
specifier|final
name|List
argument_list|<
name|ServerName
argument_list|>
name|serverNames
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|splitTime
init|=
literal|0
decl_stmt|,
name|splitLogSize
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|logDirs
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
name|ServerName
name|serverName
range|:
name|serverNames
control|)
block|{
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
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|splitDir
init|=
name|logDir
operator|.
name|suffix
argument_list|(
name|HLog
operator|.
name|SPLITTING_EXT
argument_list|)
decl_stmt|;
comment|// rename the directory so a rogue RS doesn't create more HLogs
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|logDir
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|fs
operator|.
name|rename
argument_list|(
name|logDir
argument_list|,
name|splitDir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed fs.rename for log split: "
operator|+
name|logDir
argument_list|)
throw|;
block|}
name|logDir
operator|=
name|splitDir
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Renamed region directory: "
operator|+
name|splitDir
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|splitDir
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Log dir for server "
operator|+
name|serverName
operator|+
literal|" does not exist"
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|logDirs
operator|.
name|add
argument_list|(
name|splitDir
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|logDirs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"No logs to split"
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|distributedLogSplitting
condition|)
block|{
for|for
control|(
name|ServerName
name|serverName
range|:
name|serverNames
control|)
block|{
name|splitLogManager
operator|.
name|handleDeadWorker
argument_list|(
name|serverName
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|splitTime
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
try|try
block|{
name|splitLogSize
operator|=
name|splitLogManager
operator|.
name|splitLogDistributed
argument_list|(
name|logDirs
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|OrphanHLogAfterSplitException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Retrying distributed splitting for "
operator|+
name|serverNames
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|splitLogManager
operator|.
name|splitLogDistributed
argument_list|(
name|logDirs
argument_list|)
expr_stmt|;
block|}
name|splitTime
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|splitTime
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|Path
name|logDir
range|:
name|logDirs
control|)
block|{
comment|// splitLogLock ensures that dead region servers' logs are processed
comment|// one at a time
name|this
operator|.
name|splitLogLock
operator|.
name|lock
argument_list|()
expr_stmt|;
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
argument_list|,
name|rootdir
argument_list|,
name|logDir
argument_list|,
name|oldLogDir
argument_list|,
name|this
operator|.
name|fs
argument_list|)
decl_stmt|;
try|try
block|{
comment|// If FS is in safe mode, just wait till out of it.
name|FSUtils
operator|.
name|waitOnSafeMode
argument_list|(
name|conf
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|THREAD_WAKE_FREQUENCY
argument_list|,
literal|1000
argument_list|)
argument_list|)
expr_stmt|;
name|splitter
operator|.
name|splitLog
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|OrphanHLogAfterSplitException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Retrying splitting because of:"
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|//An HLogSplitter instance can only be used once.  Get new instance.
name|splitter
operator|=
name|HLogSplitter
operator|.
name|createLogSplitter
argument_list|(
name|conf
argument_list|,
name|rootdir
argument_list|,
name|logDir
argument_list|,
name|oldLogDir
argument_list|,
name|this
operator|.
name|fs
argument_list|)
expr_stmt|;
name|splitter
operator|.
name|splitLog
argument_list|()
expr_stmt|;
block|}
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
block|}
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
comment|// DFS leaves safe mode with 0 DNs when there are 0 blocks.
comment|// We used to handle this by checking the current DN count and waiting until
comment|// it is nonzero. With security, the check for datanode count doesn't work --
comment|// it is a privileged op. So instead we adopt the strategy of the jobtracker
comment|// and simply retry file creation during bootstrap indefinitely. As soon as
comment|// there is one datanode it will succeed. Permission problems should have
comment|// already been caught by mkdirs above.
name|FSUtils
operator|.
name|setVersion
argument_list|(
name|fs
argument_list|,
name|rd
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
block|}
else|else
block|{
comment|// as above
name|FSUtils
operator|.
name|checkVersion
argument_list|(
name|fs
argument_list|,
name|rd
argument_list|,
literal|true
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
block|}
comment|// Make sure cluster ID exists
if|if
condition|(
operator|!
name|FSUtils
operator|.
name|checkClusterIdExists
argument_list|(
name|fs
argument_list|,
name|rd
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
condition|)
block|{
name|FSUtils
operator|.
name|setClusterId
argument_list|(
name|fs
argument_list|,
name|rd
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
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
block|}
name|clusterId
operator|=
name|FSUtils
operator|.
name|getClusterId
argument_list|(
name|fs
argument_list|,
name|rd
argument_list|)
expr_stmt|;
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
name|createRootTableInfo
argument_list|(
name|rd
argument_list|)
expr_stmt|;
return|return
name|rd
return|;
block|}
specifier|private
name|void
name|createRootTableInfo
parameter_list|(
name|Path
name|rd
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Create ROOT tableInfo if required.
if|if
condition|(
operator|!
name|FSTableDescriptors
operator|.
name|isTableInfoExists
argument_list|(
name|fs
argument_list|,
name|rd
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|HRegionInfo
operator|.
name|ROOT_REGIONINFO
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
condition|)
block|{
name|FSTableDescriptors
operator|.
name|createTableDescriptor
argument_list|(
name|HTableDescriptor
operator|.
name|ROOT_TABLEDESC
argument_list|,
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
block|}
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
name|setInfoFamilyCachingForRoot
argument_list|(
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
name|setInfoFamilyCachingForMeta
argument_list|(
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
argument_list|,
name|HTableDescriptor
operator|.
name|ROOT_TABLEDESC
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
argument_list|,
name|HTableDescriptor
operator|.
name|META_TABLEDESC
argument_list|)
decl_stmt|;
name|setInfoFamilyCachingForRoot
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|setInfoFamilyCachingForMeta
argument_list|(
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
comment|/**    * Enable in-memory caching for -ROOT-    */
specifier|public
specifier|static
name|void
name|setInfoFamilyCachingForRoot
parameter_list|(
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
name|HTableDescriptor
operator|.
name|ROOT_TABLEDESC
operator|.
name|getColumnFamilies
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
comment|/**    * Enable in memory caching for .META.    */
specifier|public
specifier|static
name|void
name|setInfoFamilyCachingForMeta
parameter_list|(
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
name|HTableDescriptor
operator|.
name|META_TABLEDESC
operator|.
name|getColumnFamilies
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
name|stop
parameter_list|()
block|{
if|if
condition|(
name|splitLogManager
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|splitLogManager
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Create new HTableDescriptor in HDFS.    *     * @param htableDescriptor    */
specifier|public
name|void
name|createTableDescriptor
parameter_list|(
name|HTableDescriptor
name|htableDescriptor
parameter_list|)
throws|throws
name|IOException
block|{
name|FSTableDescriptors
operator|.
name|createTableDescriptor
argument_list|(
name|htableDescriptor
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Delete column of a table    * @param tableName    * @param familyName    * @return Modified HTableDescriptor with requested column deleted.    * @throws IOException    */
specifier|public
name|HTableDescriptor
name|deleteColumn
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|familyName
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"DeleteColumn. Table = "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
operator|+
literal|" family = "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|familyName
argument_list|)
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
name|this
operator|.
name|services
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|htd
operator|.
name|removeFamily
argument_list|(
name|familyName
argument_list|)
expr_stmt|;
name|this
operator|.
name|services
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|add
argument_list|(
name|htd
argument_list|)
expr_stmt|;
return|return
name|htd
return|;
block|}
comment|/**    * Modify Column of a table    * @param tableName    * @param hcd HColumnDesciptor    * @return Modified HTableDescriptor with the column modified.    * @throws IOException    */
specifier|public
name|HTableDescriptor
name|modifyColumn
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|HColumnDescriptor
name|hcd
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"AddModifyColumn. Table = "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
operator|+
literal|" HCD = "
operator|+
name|hcd
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
name|this
operator|.
name|services
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|byte
index|[]
name|familyName
init|=
name|hcd
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|htd
operator|.
name|hasFamily
argument_list|(
name|familyName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|InvalidFamilyOperationException
argument_list|(
literal|"Family '"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|familyName
argument_list|)
operator|+
literal|"' doesn't exists so cannot be modified"
argument_list|)
throw|;
block|}
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|this
operator|.
name|services
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|add
argument_list|(
name|htd
argument_list|)
expr_stmt|;
return|return
name|htd
return|;
block|}
comment|/**    * Add column to a table    * @param tableName    * @param hcd    * @return Modified HTableDescriptor with new column added.    * @throws IOException    */
specifier|public
name|HTableDescriptor
name|addColumn
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|HColumnDescriptor
name|hcd
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"AddColumn. Table = "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
operator|+
literal|" HCD = "
operator|+
name|hcd
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
name|this
operator|.
name|services
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|htd
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|InvalidFamilyOperationException
argument_list|(
literal|"Family '"
operator|+
name|hcd
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|"' cannot be modified as HTD is null"
argument_list|)
throw|;
block|}
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|this
operator|.
name|services
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|add
argument_list|(
name|htd
argument_list|)
expr_stmt|;
return|return
name|htd
return|;
block|}
block|}
end_class

end_unit

