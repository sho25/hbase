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
name|Set
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
name|ConcurrentHashMap
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
name|ConcurrentMap
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
name|CompoundConfiguration
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
name|HBaseConfiguration
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

begin_comment
comment|/**  * This provides an class for maintaining a set of peer clusters. These peers are remote slave  * clusters that data is replicated to.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReplicationPeers
block|{
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
comment|// Map of peer clusters keyed by their id
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|ReplicationPeerImpl
argument_list|>
name|peerCache
decl_stmt|;
specifier|private
specifier|final
name|ReplicationPeerStorage
name|peerStorage
decl_stmt|;
name|ReplicationPeers
parameter_list|(
name|ZKWatcher
name|zookeeper
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|peerCache
operator|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|peerStorage
operator|=
name|ReplicationStorageFactory
operator|.
name|getReplicationPeerStorage
argument_list|(
name|zookeeper
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|ReplicationException
block|{
comment|// Loading all existing peerIds into peer cache.
for|for
control|(
name|String
name|peerId
range|:
name|this
operator|.
name|peerStorage
operator|.
name|listPeerIds
argument_list|()
control|)
block|{
name|addPeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|ReplicationPeerStorage
name|getPeerStorage
parameter_list|()
block|{
return|return
name|this
operator|.
name|peerStorage
return|;
block|}
comment|/**    * Method called after a peer has been connected. It will create a ReplicationPeer to track the    * newly connected cluster.    * @param peerId a short that identifies the cluster    * @return whether a ReplicationPeer was successfully created    * @throws ReplicationException if connecting to the peer fails    */
specifier|public
name|boolean
name|addPeer
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
block|{
if|if
condition|(
name|this
operator|.
name|peerCache
operator|.
name|containsKey
argument_list|(
name|peerId
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|peerCache
operator|.
name|put
argument_list|(
name|peerId
argument_list|,
name|createPeer
argument_list|(
name|peerId
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|public
name|void
name|removePeer
parameter_list|(
name|String
name|peerId
parameter_list|)
block|{
name|peerCache
operator|.
name|remove
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the peer state for the specified connected remote slave cluster. The value might be read    * from cache, so it is recommended to use {@link #peerStorage } to read storage directly if    * reading the state after enabling or disabling it.    * @param peerId a short that identifies the cluster    * @return true if replication is enabled, false otherwise.    */
specifier|public
name|boolean
name|isPeerEnabled
parameter_list|(
name|String
name|peerId
parameter_list|)
block|{
name|ReplicationPeer
name|replicationPeer
init|=
name|this
operator|.
name|peerCache
operator|.
name|get
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
if|if
condition|(
name|replicationPeer
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Peer with id= "
operator|+
name|peerId
operator|+
literal|" is not cached"
argument_list|)
throw|;
block|}
return|return
name|replicationPeer
operator|.
name|getPeerState
argument_list|()
operator|==
name|PeerState
operator|.
name|ENABLED
return|;
block|}
comment|/**    * Returns the ReplicationPeerImpl for the specified cached peer. This ReplicationPeer will    * continue to track changes to the Peer's state and config. This method returns null if no peer    * has been cached with the given peerId.    * @param peerId id for the peer    * @return ReplicationPeer object    */
specifier|public
name|ReplicationPeerImpl
name|getPeer
parameter_list|(
name|String
name|peerId
parameter_list|)
block|{
return|return
name|peerCache
operator|.
name|get
argument_list|(
name|peerId
argument_list|)
return|;
block|}
comment|/**    * Returns the set of peerIds of the clusters that have been connected and have an underlying    * ReplicationPeer.    * @return a Set of Strings for peerIds    */
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getAllPeerIds
parameter_list|()
block|{
return|return
name|peerCache
operator|.
name|keySet
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|Configuration
name|getPeerClusterConfiguration
parameter_list|(
name|ReplicationPeerConfig
name|peerConfig
parameter_list|,
name|Configuration
name|baseConf
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|Configuration
name|otherConf
decl_stmt|;
try|try
block|{
name|otherConf
operator|=
name|HBaseConfiguration
operator|.
name|createClusterConf
argument_list|(
name|baseConf
argument_list|,
name|peerConfig
operator|.
name|getClusterKey
argument_list|()
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
name|ReplicationException
argument_list|(
literal|"Can't get peer configuration for peer "
operator|+
name|peerConfig
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|peerConfig
operator|.
name|getConfiguration
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|CompoundConfiguration
name|compound
init|=
operator|new
name|CompoundConfiguration
argument_list|()
decl_stmt|;
name|compound
operator|.
name|add
argument_list|(
name|otherConf
argument_list|)
expr_stmt|;
name|compound
operator|.
name|addStringMap
argument_list|(
name|peerConfig
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|compound
return|;
block|}
return|return
name|otherConf
return|;
block|}
specifier|public
name|PeerState
name|refreshPeerState
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|ReplicationPeerImpl
name|peer
init|=
name|peerCache
operator|.
name|get
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
literal|"Peer with id="
operator|+
name|peerId
operator|+
literal|" is not cached."
argument_list|)
throw|;
block|}
name|peer
operator|.
name|setPeerState
argument_list|(
name|peerStorage
operator|.
name|isPeerEnabled
argument_list|(
name|peerId
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|peer
operator|.
name|getPeerState
argument_list|()
return|;
block|}
specifier|public
name|ReplicationPeerConfig
name|refreshPeerConfig
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|ReplicationPeerImpl
name|peer
init|=
name|peerCache
operator|.
name|get
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
literal|"Peer with id="
operator|+
name|peerId
operator|+
literal|" is not cached."
argument_list|)
throw|;
block|}
name|peer
operator|.
name|setPeerConfig
argument_list|(
name|peerStorage
operator|.
name|getPeerConfig
argument_list|(
name|peerId
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|peer
operator|.
name|getPeerConfig
argument_list|()
return|;
block|}
comment|/**    * Helper method to connect to a peer    * @param peerId peer's identifier    * @return object representing the peer    */
specifier|private
name|ReplicationPeerImpl
name|createPeer
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|ReplicationPeerConfig
name|peerConfig
init|=
name|peerStorage
operator|.
name|getPeerConfig
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
name|boolean
name|enabled
init|=
name|peerStorage
operator|.
name|isPeerEnabled
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
return|return
operator|new
name|ReplicationPeerImpl
argument_list|(
name|getPeerClusterConfiguration
argument_list|(
name|peerConfig
argument_list|,
name|conf
argument_list|)
argument_list|,
name|peerId
argument_list|,
name|enabled
argument_list|,
name|peerConfig
argument_list|)
return|;
block|}
block|}
end_class

end_unit

