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
name|replication
operator|.
name|regionserver
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
name|PriorityBlockingQueue
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
name|replication
operator|.
name|ReplicationPeer
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
name|replication
operator|.
name|ReplicationQueueStorage
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
name|Threads
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
name|wal
operator|.
name|AbstractFSWALProvider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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
comment|/**  * Class that handles the recovered source of a replication stream, which is transfered from  * another dead region server. This will be closed when all logs are pushed to peer cluster.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RecoveredReplicationSource
extends|extends
name|ReplicationSource
block|{
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
name|RecoveredReplicationSource
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|String
name|actualPeerId
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|ReplicationSourceManager
name|manager
parameter_list|,
name|ReplicationQueueStorage
name|queueStorage
parameter_list|,
name|ReplicationPeer
name|replicationPeer
parameter_list|,
name|Server
name|server
parameter_list|,
name|String
name|peerClusterZnode
parameter_list|,
name|UUID
name|clusterId
parameter_list|,
name|WALFileLengthProvider
name|walFileLengthProvider
parameter_list|,
name|MetricsSource
name|metrics
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|init
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|manager
argument_list|,
name|queueStorage
argument_list|,
name|replicationPeer
argument_list|,
name|server
argument_list|,
name|peerClusterZnode
argument_list|,
name|clusterId
argument_list|,
name|walFileLengthProvider
argument_list|,
name|metrics
argument_list|)
expr_stmt|;
name|this
operator|.
name|actualPeerId
operator|=
name|this
operator|.
name|replicationQueueInfo
operator|.
name|getPeerId
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|RecoveredReplicationSourceShipper
name|createNewShipper
parameter_list|(
name|String
name|walGroupId
parameter_list|,
name|PriorityBlockingQueue
argument_list|<
name|Path
argument_list|>
name|queue
parameter_list|)
block|{
return|return
operator|new
name|RecoveredReplicationSourceShipper
argument_list|(
name|conf
argument_list|,
name|walGroupId
argument_list|,
name|queue
argument_list|,
name|this
argument_list|,
name|queueStorage
argument_list|)
return|;
block|}
specifier|public
name|void
name|locateRecoveredPaths
parameter_list|(
name|PriorityBlockingQueue
argument_list|<
name|Path
argument_list|>
name|queue
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|hasPathChanged
init|=
literal|false
decl_stmt|;
name|PriorityBlockingQueue
argument_list|<
name|Path
argument_list|>
name|newPaths
init|=
operator|new
name|PriorityBlockingQueue
argument_list|<
name|Path
argument_list|>
argument_list|(
name|queueSizePerGroup
argument_list|,
operator|new
name|LogsComparator
argument_list|()
argument_list|)
decl_stmt|;
name|pathsLoop
label|:
for|for
control|(
name|Path
name|path
range|:
name|queue
control|)
block|{
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|path
argument_list|)
condition|)
block|{
comment|// still in same location, don't need to do anything
name|newPaths
operator|.
name|add
argument_list|(
name|path
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// Path changed - try to find the right path.
name|hasPathChanged
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|server
operator|instanceof
name|ReplicationSyncUp
operator|.
name|DummyServer
condition|)
block|{
comment|// In the case of disaster/recovery, HMaster may be shutdown/crashed before flush data
comment|// from .logs to .oldlogs. Loop into .logs folders and check whether a match exists
name|Path
name|newPath
init|=
name|getReplSyncUpPath
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|newPaths
operator|.
name|add
argument_list|(
name|newPath
argument_list|)
expr_stmt|;
continue|continue;
block|}
else|else
block|{
comment|// See if Path exists in the dead RS folder (there could be a chain of failures
comment|// to look at)
name|List
argument_list|<
name|ServerName
argument_list|>
name|deadRegionServers
init|=
name|this
operator|.
name|replicationQueueInfo
operator|.
name|getDeadRegionServers
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"NB dead servers : "
operator|+
name|deadRegionServers
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|Path
name|walDir
init|=
name|FSUtils
operator|.
name|getWALRootDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
for|for
control|(
name|ServerName
name|curDeadServerName
range|:
name|deadRegionServers
control|)
block|{
specifier|final
name|Path
name|deadRsDirectory
init|=
operator|new
name|Path
argument_list|(
name|walDir
argument_list|,
name|AbstractFSWALProvider
operator|.
name|getWALDirectoryName
argument_list|(
name|curDeadServerName
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Path
index|[]
name|locs
init|=
operator|new
name|Path
index|[]
block|{
operator|new
name|Path
argument_list|(
name|deadRsDirectory
argument_list|,
name|path
operator|.
name|getName
argument_list|()
argument_list|)
block|,
operator|new
name|Path
argument_list|(
name|deadRsDirectory
operator|.
name|suffix
argument_list|(
name|AbstractFSWALProvider
operator|.
name|SPLITTING_EXT
argument_list|)
argument_list|,
name|path
operator|.
name|getName
argument_list|()
argument_list|)
block|}
decl_stmt|;
for|for
control|(
name|Path
name|possibleLogLocation
range|:
name|locs
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Possible location "
operator|+
name|possibleLogLocation
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|manager
operator|.
name|getFs
argument_list|()
operator|.
name|exists
argument_list|(
name|possibleLogLocation
argument_list|)
condition|)
block|{
comment|// We found the right new location
name|LOG
operator|.
name|info
argument_list|(
literal|"Log "
operator|+
name|path
operator|+
literal|" still exists at "
operator|+
name|possibleLogLocation
argument_list|)
expr_stmt|;
name|newPaths
operator|.
name|add
argument_list|(
name|possibleLogLocation
argument_list|)
expr_stmt|;
continue|continue
name|pathsLoop
continue|;
block|}
block|}
block|}
comment|// didn't find a new location
name|LOG
operator|.
name|error
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"WAL Path %s doesn't exist and couldn't find its new location"
argument_list|,
name|path
argument_list|)
argument_list|)
expr_stmt|;
name|newPaths
operator|.
name|add
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|hasPathChanged
condition|)
block|{
if|if
condition|(
name|newPaths
operator|.
name|size
argument_list|()
operator|!=
name|queue
operator|.
name|size
argument_list|()
condition|)
block|{
comment|// this shouldn't happen
name|LOG
operator|.
name|error
argument_list|(
literal|"Recovery queue size is incorrect"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Recovery queue size error"
argument_list|)
throw|;
block|}
comment|// put the correct locations in the queue
comment|// since this is a recovered queue with no new incoming logs,
comment|// there shouldn't be any concurrency issues
name|queue
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|Path
name|path
range|:
name|newPaths
control|)
block|{
name|queue
operator|.
name|add
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// N.B. the ReplicationSyncUp tool sets the manager.getWALDir to the root of the wal
comment|// area rather than to the wal area for a particular region server.
specifier|private
name|Path
name|getReplSyncUpPath
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|FileStatus
index|[]
name|rss
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|manager
operator|.
name|getLogDir
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|rs
range|:
name|rss
control|)
block|{
name|Path
name|p
init|=
name|rs
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|FileStatus
index|[]
name|logs
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|p
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|log
range|:
name|logs
control|)
block|{
name|p
operator|=
operator|new
name|Path
argument_list|(
name|p
argument_list|,
name|log
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|p
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|path
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Log "
operator|+
name|p
operator|.
name|getName
argument_list|()
operator|+
literal|" found at "
operator|+
name|p
argument_list|)
expr_stmt|;
return|return
name|p
return|;
block|}
block|}
block|}
name|LOG
operator|.
name|error
argument_list|(
literal|"Didn't find path for: "
operator|+
name|path
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|path
return|;
block|}
name|void
name|tryFinish
parameter_list|()
block|{
comment|// use synchronize to make sure one last thread will clean the queue
synchronized|synchronized
init|(
name|workerThreads
init|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
comment|// wait a short while for other worker thread to fully exit
name|boolean
name|allTasksDone
init|=
name|workerThreads
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|allMatch
argument_list|(
name|w
lambda|->
name|w
operator|.
name|isFinished
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|allTasksDone
condition|)
block|{
name|this
operator|.
name|getSourceMetrics
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|manager
operator|.
name|removeRecoveredSource
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Finished recovering queue {} with the following stats: {}"
argument_list|,
name|queueId
argument_list|,
name|getStats
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|getPeerId
parameter_list|()
block|{
return|return
name|this
operator|.
name|actualPeerId
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerWALsBelongTo
parameter_list|()
block|{
return|return
name|this
operator|.
name|replicationQueueInfo
operator|.
name|getDeadRegionServers
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isRecovered
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

