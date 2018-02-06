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
name|concurrent
operator|.
name|locks
operator|.
name|Lock
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
name|ReplicationException
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
operator|.
name|PeerState
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
name|ReplicationPeerConfig
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
name|ReplicationPeerImpl
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
name|KeyLocker
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|PeerProcedureHandlerImpl
implements|implements
name|PeerProcedureHandler
block|{
specifier|private
specifier|final
name|ReplicationSourceManager
name|replicationSourceManager
decl_stmt|;
specifier|private
specifier|final
name|KeyLocker
argument_list|<
name|String
argument_list|>
name|peersLock
init|=
operator|new
name|KeyLocker
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|PeerProcedureHandlerImpl
parameter_list|(
name|ReplicationSourceManager
name|replicationSourceManager
parameter_list|)
block|{
name|this
operator|.
name|replicationSourceManager
operator|=
name|replicationSourceManager
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addPeer
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|IOException
block|{
name|Lock
name|peerLock
init|=
name|peersLock
operator|.
name|acquireLock
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
try|try
block|{
name|replicationSourceManager
operator|.
name|addPeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|peerLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|removePeer
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|IOException
block|{
name|Lock
name|peerLock
init|=
name|peersLock
operator|.
name|acquireLock
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|replicationSourceManager
operator|.
name|getReplicationPeers
argument_list|()
operator|.
name|getPeer
argument_list|(
name|peerId
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|replicationSourceManager
operator|.
name|removePeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|peerLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|refreshPeerState
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
throws|,
name|IOException
block|{
name|PeerState
name|newState
decl_stmt|;
name|Lock
name|peerLock
init|=
name|peersLock
operator|.
name|acquireLock
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
name|ReplicationPeerImpl
name|peer
init|=
literal|null
decl_stmt|;
name|PeerState
name|oldState
init|=
literal|null
decl_stmt|;
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
name|peer
operator|=
name|replicationSourceManager
operator|.
name|getReplicationPeers
argument_list|()
operator|.
name|getPeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
if|if
condition|(
name|peer
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"Peer with id="
operator|+
name|peerId
operator|+
literal|" is not cached."
argument_list|)
throw|;
block|}
name|oldState
operator|=
name|peer
operator|.
name|getPeerState
argument_list|()
expr_stmt|;
name|newState
operator|=
name|replicationSourceManager
operator|.
name|getReplicationPeers
argument_list|()
operator|.
name|refreshPeerState
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
comment|// RS need to start work with the new replication state change
if|if
condition|(
name|oldState
operator|.
name|equals
argument_list|(
name|PeerState
operator|.
name|ENABLED
argument_list|)
operator|&&
name|newState
operator|.
name|equals
argument_list|(
name|PeerState
operator|.
name|DISABLED
argument_list|)
condition|)
block|{
name|replicationSourceManager
operator|.
name|refreshSources
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
name|success
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|success
operator|&&
name|peer
operator|!=
literal|null
condition|)
block|{
comment|// Reset peer state if refresh source failed
name|peer
operator|.
name|setPeerState
argument_list|(
name|oldState
operator|.
name|equals
argument_list|(
name|PeerState
operator|.
name|ENABLED
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|peerLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|enablePeer
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
throws|,
name|IOException
block|{
name|refreshPeerState
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|disablePeer
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
throws|,
name|IOException
block|{
name|refreshPeerState
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updatePeerConfig
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
throws|,
name|IOException
block|{
name|Lock
name|peerLock
init|=
name|peersLock
operator|.
name|acquireLock
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
name|ReplicationPeerImpl
name|peer
init|=
literal|null
decl_stmt|;
name|ReplicationPeerConfig
name|oldConfig
init|=
literal|null
decl_stmt|;
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
name|peer
operator|=
name|replicationSourceManager
operator|.
name|getReplicationPeers
argument_list|()
operator|.
name|getPeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
if|if
condition|(
name|peer
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"Peer with id="
operator|+
name|peerId
operator|+
literal|" is not cached."
argument_list|)
throw|;
block|}
name|oldConfig
operator|=
name|peer
operator|.
name|getPeerConfig
argument_list|()
expr_stmt|;
name|ReplicationPeerConfig
name|newConfig
init|=
name|replicationSourceManager
operator|.
name|getReplicationPeers
argument_list|()
operator|.
name|refreshPeerConfig
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
comment|// RS need to start work with the new replication config change
if|if
condition|(
operator|!
name|ReplicationUtils
operator|.
name|isKeyConfigEqual
argument_list|(
name|oldConfig
argument_list|,
name|newConfig
argument_list|)
condition|)
block|{
name|replicationSourceManager
operator|.
name|refreshSources
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
name|success
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|success
operator|&&
name|peer
operator|!=
literal|null
condition|)
block|{
comment|// Reset peer config if refresh source failed
name|peer
operator|.
name|setPeerConfig
argument_list|(
name|oldConfig
argument_list|)
expr_stmt|;
block|}
name|peerLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

