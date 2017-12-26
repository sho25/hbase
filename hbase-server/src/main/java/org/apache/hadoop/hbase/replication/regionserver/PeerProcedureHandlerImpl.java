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
name|ReplicationPeerImpl
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
name|PeerProcedureHandlerImpl
implements|implements
name|PeerProcedureHandler
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
name|PeerProcedureHandlerImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ReplicationSourceManager
name|replicationSourceManager
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
name|ReplicationException
throws|,
name|IOException
block|{
name|replicationSourceManager
operator|.
name|addPeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
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
name|ReplicationException
throws|,
name|IOException
block|{
name|replicationSourceManager
operator|.
name|removePeer
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
name|ReplicationPeerImpl
name|peer
init|=
name|replicationSourceManager
operator|.
name|getReplicationPeers
argument_list|()
operator|.
name|getConnectedPeer
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
if|if
condition|(
name|peer
operator|!=
literal|null
condition|)
block|{
name|peer
operator|.
name|refreshPeerState
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"disable replication peer, id: "
operator|+
name|peerId
operator|+
literal|", new state: "
operator|+
name|peer
operator|.
name|getPeerState
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"No connected peer found, peerId="
operator|+
name|peerId
argument_list|)
throw|;
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
name|ReplicationPeerImpl
name|peer
init|=
name|replicationSourceManager
operator|.
name|getReplicationPeers
argument_list|()
operator|.
name|getConnectedPeer
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
if|if
condition|(
name|peer
operator|!=
literal|null
condition|)
block|{
name|peer
operator|.
name|refreshPeerState
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"enable replication peer, id: "
operator|+
name|peerId
operator|+
literal|", new state: "
operator|+
name|peer
operator|.
name|getPeerState
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"No connected peer found, peerId="
operator|+
name|peerId
argument_list|)
throw|;
block|}
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
name|ReplicationPeerImpl
name|peer
init|=
name|replicationSourceManager
operator|.
name|getReplicationPeers
argument_list|()
operator|.
name|getConnectedPeer
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
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
literal|"No connected peer found, peerId="
operator|+
name|peerId
argument_list|)
throw|;
block|}
name|peer
operator|.
name|refreshPeerConfig
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

