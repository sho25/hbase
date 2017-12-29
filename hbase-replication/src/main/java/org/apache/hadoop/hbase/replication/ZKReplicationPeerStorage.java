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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|hbase
operator|.
name|client
operator|.
name|replication
operator|.
name|ReplicationPeerConfigUtil
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
name|exceptions
operator|.
name|DeserializationException
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
name|CollectionUtils
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
name|ZKUtil
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
name|ZKUtil
operator|.
name|ZKUtilOp
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
name|ZKWatcher
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
name|ZNodePaths
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
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ReplicationProtos
import|;
end_import

begin_comment
comment|/**  * ZK based replication peer storage.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ZKReplicationPeerStorage
extends|extends
name|ZKReplicationStorageBase
implements|implements
name|ReplicationPeerStorage
block|{
specifier|public
specifier|static
specifier|final
name|String
name|PEERS_ZNODE
init|=
literal|"zookeeper.znode.replication.peers"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PEERS_ZNODE_DEFAULT
init|=
literal|"peers"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PEERS_STATE_ZNODE
init|=
literal|"zookeeper.znode.replication.peers.state"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PEERS_STATE_ZNODE_DEFAULT
init|=
literal|"peer-state"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|ENABLED_ZNODE_BYTES
init|=
name|toByteArray
argument_list|(
name|ReplicationProtos
operator|.
name|ReplicationState
operator|.
name|State
operator|.
name|ENABLED
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|DISABLED_ZNODE_BYTES
init|=
name|toByteArray
argument_list|(
name|ReplicationProtos
operator|.
name|ReplicationState
operator|.
name|State
operator|.
name|DISABLED
argument_list|)
decl_stmt|;
comment|/**    * The name of the znode that contains the replication status of a remote slave (i.e. peer)    * cluster.    */
specifier|private
specifier|final
name|String
name|peerStateNodeName
decl_stmt|;
comment|/**    * The name of the znode that contains a list of all remote slave (i.e. peer) clusters.    */
specifier|private
specifier|final
name|String
name|peersZNode
decl_stmt|;
specifier|public
name|ZKReplicationPeerStorage
parameter_list|(
name|ZKWatcher
name|zookeeper
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|zookeeper
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|peerStateNodeName
operator|=
name|conf
operator|.
name|get
argument_list|(
name|PEERS_STATE_ZNODE
argument_list|,
name|PEERS_STATE_ZNODE_DEFAULT
argument_list|)
expr_stmt|;
name|String
name|peersZNodeName
init|=
name|conf
operator|.
name|get
argument_list|(
name|PEERS_ZNODE
argument_list|,
name|PEERS_ZNODE_DEFAULT
argument_list|)
decl_stmt|;
name|this
operator|.
name|peersZNode
operator|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|replicationZNode
argument_list|,
name|peersZNodeName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|String
name|getPeerStateNode
parameter_list|(
name|String
name|peerId
parameter_list|)
block|{
return|return
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|getPeerNode
argument_list|(
name|peerId
argument_list|)
argument_list|,
name|peerStateNodeName
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|String
name|getPeerNode
parameter_list|(
name|String
name|peerId
parameter_list|)
block|{
return|return
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|peersZNode
argument_list|,
name|peerId
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addPeer
parameter_list|(
name|String
name|peerId
parameter_list|,
name|ReplicationPeerConfig
name|peerConfig
parameter_list|,
name|boolean
name|enabled
parameter_list|)
throws|throws
name|ReplicationException
block|{
try|try
block|{
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zookeeper
argument_list|,
name|peersZNode
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|multiOrSequential
argument_list|(
name|zookeeper
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|ZKUtilOp
operator|.
name|createAndFailSilent
argument_list|(
name|getPeerNode
argument_list|(
name|peerId
argument_list|)
argument_list|,
name|ReplicationPeerConfigUtil
operator|.
name|toByteArray
argument_list|(
name|peerConfig
argument_list|)
argument_list|)
argument_list|,
name|ZKUtilOp
operator|.
name|createAndFailSilent
argument_list|(
name|getPeerStateNode
argument_list|(
name|peerId
argument_list|)
argument_list|,
name|enabled
condition|?
name|ENABLED_ZNODE_BYTES
else|:
name|DISABLED_ZNODE_BYTES
argument_list|)
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"Could not add peer with id="
operator|+
name|peerId
operator|+
literal|", peerConfif=>"
operator|+
name|peerConfig
operator|+
literal|", state="
operator|+
operator|(
name|enabled
condition|?
literal|"ENABLED"
else|:
literal|"DISABLED"
operator|)
argument_list|,
name|e
argument_list|)
throw|;
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
name|ReplicationException
block|{
try|try
block|{
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|zookeeper
argument_list|,
name|getPeerNode
argument_list|(
name|peerId
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"Could not remove peer with id="
operator|+
name|peerId
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setPeerState
parameter_list|(
name|String
name|peerId
parameter_list|,
name|boolean
name|enabled
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|byte
index|[]
name|stateBytes
init|=
name|enabled
condition|?
name|ENABLED_ZNODE_BYTES
else|:
name|DISABLED_ZNODE_BYTES
decl_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|setData
argument_list|(
name|zookeeper
argument_list|,
name|getPeerStateNode
argument_list|(
name|peerId
argument_list|)
argument_list|,
name|stateBytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"Unable to change state of the peer with id="
operator|+
name|peerId
argument_list|,
name|e
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
parameter_list|,
name|ReplicationPeerConfig
name|peerConfig
parameter_list|)
throws|throws
name|ReplicationException
block|{
try|try
block|{
name|ZKUtil
operator|.
name|setData
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|getPeerNode
argument_list|(
name|peerId
argument_list|)
argument_list|,
name|ReplicationPeerConfigUtil
operator|.
name|toByteArray
argument_list|(
name|peerConfig
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"There was a problem trying to save changes to the "
operator|+
literal|"replication peer "
operator|+
name|peerId
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|listPeerIds
parameter_list|()
throws|throws
name|ReplicationException
block|{
try|try
block|{
return|return
name|CollectionUtils
operator|.
name|nullToEmpty
argument_list|(
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|zookeeper
argument_list|,
name|peersZNode
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"Cannot get the list of peers"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isPeerEnabled
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
block|{
try|try
block|{
return|return
name|Arrays
operator|.
name|equals
argument_list|(
name|ENABLED_ZNODE_BYTES
argument_list|,
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zookeeper
argument_list|,
name|getPeerStateNode
argument_list|(
name|peerId
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
decl||
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"Unable to get status of the peer with id="
operator|+
name|peerId
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|ReplicationPeerConfig
name|getPeerConfig
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|byte
index|[]
name|data
decl_stmt|;
try|try
block|{
name|data
operator|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zookeeper
argument_list|,
name|getPeerNode
argument_list|(
name|peerId
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
decl||
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"Error getting configuration for peer with id="
operator|+
name|peerId
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|data
operator|==
literal|null
operator|||
name|data
operator|.
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"Replication peer config data shouldn't be empty, peerId="
operator|+
name|peerId
argument_list|)
throw|;
block|}
try|try
block|{
return|return
name|ReplicationPeerConfigUtil
operator|.
name|parsePeerFrom
argument_list|(
name|data
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|DeserializationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"Failed to parse replication peer config for peer with id="
operator|+
name|peerId
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

