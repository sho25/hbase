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
name|LocatedFileStatus
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
name|RemoteIterator
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
specifier|static
specifier|final
name|String
name|REPLAY_SUFFIX
init|=
literal|"-replay"
decl_stmt|;
specifier|private
specifier|final
name|MasterServices
name|services
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
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
name|conf
operator|=
name|services
operator|.
name|getConfiguration
argument_list|()
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
name|Path
name|getPeerRemoteWALDir
parameter_list|(
name|String
name|peerId
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|this
operator|.
name|remoteWALDir
argument_list|,
name|peerId
argument_list|)
return|;
block|}
specifier|private
name|Path
name|getPeerReplayWALDir
parameter_list|(
name|String
name|peerId
parameter_list|)
block|{
return|return
name|getPeerRemoteWALDir
argument_list|(
name|peerId
argument_list|)
operator|.
name|suffix
argument_list|(
name|REPLAY_SUFFIX
argument_list|)
return|;
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
specifier|public
name|void
name|renamePeerRemoteWALDir
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
name|peerId
argument_list|)
decl_stmt|;
name|Path
name|peerReplayWALDir
init|=
name|peerRemoteWALDir
operator|.
name|suffix
argument_list|(
name|REPLAY_SUFFIX
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|peerRemoteWALDir
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|rename
argument_list|(
name|peerRemoteWALDir
argument_list|,
name|peerReplayWALDir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed rename remote wal dir from "
operator|+
name|peerRemoteWALDir
operator|+
literal|" to "
operator|+
name|peerReplayWALDir
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
literal|"Rename remote wal dir from {} to {} for peer id={}"
argument_list|,
name|remoteWALDir
argument_list|,
name|peerReplayWALDir
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
name|peerReplayWALDir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Remote wal dir "
operator|+
name|peerRemoteWALDir
operator|+
literal|" and replay wal dir "
operator|+
name|peerReplayWALDir
operator|+
literal|" not exist for peer id="
operator|+
name|peerId
argument_list|)
throw|;
block|}
block|}
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|getReplayWALs
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
name|peerId
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|replayWals
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|RemoteIterator
argument_list|<
name|LocatedFileStatus
argument_list|>
name|iterator
init|=
name|fs
operator|.
name|listFiles
argument_list|(
name|peerReplayWALDir
argument_list|,
literal|false
argument_list|)
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|replayWals
operator|.
name|add
argument_list|(
name|iterator
operator|.
name|next
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|replayWals
return|;
block|}
specifier|public
name|void
name|removePeerReplayWALDir
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
block|}
end_class

end_unit

