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
name|replication
package|;
end_package

begin_import
import|import static
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
name|ReplicationUtils
operator|.
name|getPeerRemoteWALDir
import|;
end_import

begin_import
import|import static
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
name|ReplicationUtils
operator|.
name|getPeerReplayWALDir
import|;
end_import

begin_import
import|import static
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
name|ReplicationUtils
operator|.
name|getPeerSnapshotWALDir
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
name|HashMap
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
name|BlockingQueue
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
name|LinkedBlockingQueue
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
name|TimeUnit
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
name|MasterServices
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
name|ReplicationUtils
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReplaySyncReplicationWALManager
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
name|ReplaySyncReplicationWALManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|MasterServices
name|services
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|final
name|Path
name|walRootDir
decl_stmt|;
specifier|private
specifier|final
name|Path
name|remoteWALDir
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|BlockingQueue
argument_list|<
name|ServerName
argument_list|>
argument_list|>
name|availServers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|ReplaySyncReplicationWALManager
parameter_list|(
name|MasterServices
name|services
parameter_list|)
block|{
name|this
operator|.
name|services
operator|=
name|services
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getWALFileSystem
argument_list|()
expr_stmt|;
name|this
operator|.
name|walRootDir
operator|=
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getWALRootDir
argument_list|()
expr_stmt|;
name|this
operator|.
name|remoteWALDir
operator|=
operator|new
name|Path
argument_list|(
name|this
operator|.
name|walRootDir
argument_list|,
name|ReplicationUtils
operator|.
name|REMOTE_WAL_DIR_NAME
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|createPeerRemoteWALDir
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|peerRemoteWALDir
init|=
name|getPeerRemoteWALDir
argument_list|(
name|remoteWALDir
argument_list|,
name|peerId
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|peerRemoteWALDir
argument_list|)
operator|&&
operator|!
name|fs
operator|.
name|mkdirs
argument_list|(
name|peerRemoteWALDir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to mkdir "
operator|+
name|peerRemoteWALDir
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|rename
parameter_list|(
name|Path
name|src
parameter_list|,
name|Path
name|dst
parameter_list|,
name|String
name|peerId
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
name|src
argument_list|)
condition|)
block|{
name|deleteDir
argument_list|(
name|dst
argument_list|,
name|peerId
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|rename
argument_list|(
name|src
argument_list|,
name|dst
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to rename dir from "
operator|+
name|src
operator|+
literal|" to "
operator|+
name|dst
operator|+
literal|" for peer id="
operator|+
name|peerId
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Renamed dir from {} to {} for peer id={}"
argument_list|,
name|src
argument_list|,
name|dst
argument_list|,
name|peerId
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
name|dst
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Want to rename from "
operator|+
name|src
operator|+
literal|" to "
operator|+
name|dst
operator|+
literal|", but they both do not exist"
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|renameToPeerReplayWALDir
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|IOException
block|{
name|rename
argument_list|(
name|getPeerRemoteWALDir
argument_list|(
name|remoteWALDir
argument_list|,
name|peerId
argument_list|)
argument_list|,
name|getPeerReplayWALDir
argument_list|(
name|remoteWALDir
argument_list|,
name|peerId
argument_list|)
argument_list|,
name|peerId
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|renameToPeerSnapshotWALDir
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|IOException
block|{
name|rename
argument_list|(
name|getPeerReplayWALDir
argument_list|(
name|remoteWALDir
argument_list|,
name|peerId
argument_list|)
argument_list|,
name|getPeerSnapshotWALDir
argument_list|(
name|remoteWALDir
argument_list|,
name|peerId
argument_list|)
argument_list|,
name|peerId
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|getReplayWALsAndCleanUpUnusedFiles
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|peerReplayWALDir
init|=
name|getPeerReplayWALDir
argument_list|(
name|remoteWALDir
argument_list|,
name|peerId
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|status
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|peerReplayWALDir
argument_list|,
name|p
lambda|->
name|p
operator|.
name|getName
argument_list|()
operator|.
name|endsWith
argument_list|(
name|ReplicationUtils
operator|.
name|RENAME_WAL_SUFFIX
argument_list|)
argument_list|)
control|)
block|{
name|Path
name|src
operator|=
name|status
operator|.
name|getPath
argument_list|()
block|;
name|String
name|srcName
operator|=
name|src
operator|.
name|getName
argument_list|()
block|;
name|String
name|dstName
operator|=
name|srcName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|srcName
operator|.
name|length
argument_list|()
operator|-
name|ReplicationUtils
operator|.
name|RENAME_WAL_SUFFIX
operator|.
name|length
argument_list|()
argument_list|)
block|;
name|FSUtils
operator|.
name|renameFile
argument_list|(
name|fs
argument_list|,
name|src
argument_list|,
operator|new
name|Path
argument_list|(
name|src
operator|.
name|getParent
argument_list|()
argument_list|,
name|dstName
argument_list|)
argument_list|)
block|;     }
name|List
argument_list|<
name|Path
argument_list|>
name|wals
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|status
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|peerReplayWALDir
argument_list|)
control|)
block|{
name|Path
name|path
init|=
name|status
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|path
operator|.
name|getName
argument_list|()
operator|.
name|endsWith
argument_list|(
name|ReplicationUtils
operator|.
name|SYNC_WAL_SUFFIX
argument_list|)
condition|)
block|{
name|wals
operator|.
name|add
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|delete
argument_list|(
name|path
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can not delete unused file: "
operator|+
name|path
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|wals
return|;
block|}
specifier|public
name|void
name|snapshotPeerReplayWALDir
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|peerReplayWALDir
init|=
name|getPeerReplayWALDir
argument_list|(
name|remoteWALDir
argument_list|,
name|peerId
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|peerReplayWALDir
argument_list|)
operator|&&
operator|!
name|fs
operator|.
name|delete
argument_list|(
name|peerReplayWALDir
argument_list|,
literal|true
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to remove replay wals dir "
operator|+
name|peerReplayWALDir
operator|+
literal|" for peer id="
operator|+
name|peerId
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|deleteDir
parameter_list|(
name|Path
name|dir
parameter_list|,
name|String
name|peerId
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|delete
argument_list|(
name|dir
argument_list|,
literal|true
argument_list|)
operator|&&
name|fs
operator|.
name|exists
argument_list|(
name|dir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to remove dir "
operator|+
name|dir
operator|+
literal|" for peer id="
operator|+
name|peerId
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|removePeerRemoteWALs
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|IOException
block|{
name|deleteDir
argument_list|(
name|getPeerRemoteWALDir
argument_list|(
name|remoteWALDir
argument_list|,
name|peerId
argument_list|)
argument_list|,
name|peerId
argument_list|)
expr_stmt|;
name|deleteDir
argument_list|(
name|getPeerReplayWALDir
argument_list|(
name|remoteWALDir
argument_list|,
name|peerId
argument_list|)
argument_list|,
name|peerId
argument_list|)
expr_stmt|;
name|deleteDir
argument_list|(
name|getPeerSnapshotWALDir
argument_list|(
name|remoteWALDir
argument_list|,
name|peerId
argument_list|)
argument_list|,
name|peerId
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|initPeerWorkers
parameter_list|(
name|String
name|peerId
parameter_list|)
block|{
name|BlockingQueue
argument_list|<
name|ServerName
argument_list|>
name|servers
init|=
operator|new
name|LinkedBlockingQueue
argument_list|<>
argument_list|()
decl_stmt|;
name|services
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServers
argument_list|()
operator|.
name|keySet
argument_list|()
operator|.
name|forEach
argument_list|(
name|server
lambda|->
name|servers
operator|.
name|offer
argument_list|(
name|server
argument_list|)
argument_list|)
expr_stmt|;
name|availServers
operator|.
name|put
argument_list|(
name|peerId
argument_list|,
name|servers
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ServerName
name|getAvailServer
parameter_list|(
name|String
name|peerId
parameter_list|,
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|InterruptedException
block|{
return|return
name|availServers
operator|.
name|get
argument_list|(
name|peerId
argument_list|)
operator|.
name|poll
argument_list|(
name|timeout
argument_list|,
name|unit
argument_list|)
return|;
block|}
specifier|public
name|void
name|addAvailServer
parameter_list|(
name|String
name|peerId
parameter_list|,
name|ServerName
name|server
parameter_list|)
block|{
name|availServers
operator|.
name|get
argument_list|(
name|peerId
argument_list|)
operator|.
name|offer
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|removeWALRootPath
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
name|String
name|pathStr
init|=
name|path
operator|.
name|toString
argument_list|()
decl_stmt|;
comment|// remove the "/" too.
return|return
name|pathStr
operator|.
name|substring
argument_list|(
name|walRootDir
operator|.
name|toString
argument_list|()
operator|.
name|length
argument_list|()
operator|+
literal|1
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|Path
name|getRemoteWALDir
parameter_list|()
block|{
return|return
name|remoteWALDir
return|;
block|}
block|}
end_class

end_unit

