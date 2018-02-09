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
name|Map
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
name|TableName
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
name|Pair
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
name|ReplicationPeerImpl
implements|implements
name|ReplicationPeer
block|{
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|String
name|id
decl_stmt|;
specifier|private
specifier|volatile
name|ReplicationPeerConfig
name|peerConfig
decl_stmt|;
specifier|private
specifier|volatile
name|PeerState
name|peerState
decl_stmt|;
comment|// The lower 16 bits are the current sync replication state, the higher 16 bits are the new sync
comment|// replication state. Embedded in one int so user can not get an inconsistency view of state and
comment|// new state.
specifier|private
specifier|volatile
name|int
name|syncReplicationStateBits
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|SHIFT
init|=
literal|16
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|AND_BITS
init|=
literal|0xFFFF
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|ReplicationPeerConfigListener
argument_list|>
name|peerConfigListeners
decl_stmt|;
comment|/**    * Constructor that takes all the objects required to communicate with the specified peer, except    * for the region server addresses.    * @param conf configuration object to this peer    * @param id string representation of this peer's identifier    * @param peerConfig configuration for the replication peer    */
specifier|public
name|ReplicationPeerImpl
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|id
parameter_list|,
name|ReplicationPeerConfig
name|peerConfig
parameter_list|,
name|boolean
name|peerState
parameter_list|,
name|SyncReplicationState
name|syncReplicationState
parameter_list|,
name|SyncReplicationState
name|newSyncReplicationState
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
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|peerState
operator|=
name|peerState
condition|?
name|PeerState
operator|.
name|ENABLED
else|:
name|PeerState
operator|.
name|DISABLED
expr_stmt|;
name|this
operator|.
name|peerConfig
operator|=
name|peerConfig
expr_stmt|;
name|this
operator|.
name|syncReplicationStateBits
operator|=
name|syncReplicationState
operator|.
name|value
argument_list|()
operator||
operator|(
name|newSyncReplicationState
operator|.
name|value
argument_list|()
operator|<<
name|SHIFT
operator|)
expr_stmt|;
name|this
operator|.
name|peerConfigListeners
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|setPeerState
parameter_list|(
name|boolean
name|enabled
parameter_list|)
block|{
name|this
operator|.
name|peerState
operator|=
name|enabled
condition|?
name|PeerState
operator|.
name|ENABLED
else|:
name|PeerState
operator|.
name|DISABLED
expr_stmt|;
block|}
specifier|public
name|void
name|setPeerConfig
parameter_list|(
name|ReplicationPeerConfig
name|peerConfig
parameter_list|)
block|{
name|this
operator|.
name|peerConfig
operator|=
name|peerConfig
expr_stmt|;
name|peerConfigListeners
operator|.
name|forEach
argument_list|(
name|listener
lambda|->
name|listener
operator|.
name|peerConfigUpdated
argument_list|(
name|peerConfig
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setNewSyncReplicationState
parameter_list|(
name|SyncReplicationState
name|newState
parameter_list|)
block|{
name|this
operator|.
name|syncReplicationStateBits
operator|=
operator|(
name|this
operator|.
name|syncReplicationStateBits
operator|&
name|AND_BITS
operator|)
operator||
operator|(
name|newState
operator|.
name|value
argument_list|()
operator|<<
name|SHIFT
operator|)
expr_stmt|;
block|}
specifier|public
name|void
name|transitSyncReplicationState
parameter_list|()
block|{
name|this
operator|.
name|syncReplicationStateBits
operator|=
operator|(
name|this
operator|.
name|syncReplicationStateBits
operator|>>>
name|SHIFT
operator|)
operator||
operator|(
name|SyncReplicationState
operator|.
name|NONE
operator|.
name|value
argument_list|()
operator|<<
name|SHIFT
operator|)
expr_stmt|;
block|}
comment|/**    * Get the identifier of this peer    * @return string representation of the id (short)    */
annotation|@
name|Override
specifier|public
name|String
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
annotation|@
name|Override
specifier|public
name|PeerState
name|getPeerState
parameter_list|()
block|{
return|return
name|peerState
return|;
block|}
specifier|private
specifier|static
name|SyncReplicationState
name|getSyncReplicationState
parameter_list|(
name|int
name|bits
parameter_list|)
block|{
return|return
name|SyncReplicationState
operator|.
name|valueOf
argument_list|(
name|bits
operator|&
name|AND_BITS
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|SyncReplicationState
name|getNewSyncReplicationState
parameter_list|(
name|int
name|bits
parameter_list|)
block|{
return|return
name|SyncReplicationState
operator|.
name|valueOf
argument_list|(
name|bits
operator|>>>
name|SHIFT
argument_list|)
return|;
block|}
specifier|public
name|Pair
argument_list|<
name|SyncReplicationState
argument_list|,
name|SyncReplicationState
argument_list|>
name|getSyncReplicationStateAndNewState
parameter_list|()
block|{
name|int
name|bits
init|=
name|this
operator|.
name|syncReplicationStateBits
decl_stmt|;
return|return
name|Pair
operator|.
name|newPair
argument_list|(
name|getSyncReplicationState
argument_list|(
name|bits
argument_list|)
argument_list|,
name|getNewSyncReplicationState
argument_list|(
name|bits
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|SyncReplicationState
name|getNewSyncReplicationState
parameter_list|()
block|{
return|return
name|getNewSyncReplicationState
argument_list|(
name|syncReplicationStateBits
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SyncReplicationState
name|getSyncReplicationState
parameter_list|()
block|{
return|return
name|getSyncReplicationState
argument_list|(
name|syncReplicationStateBits
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReplicationPeerConfig
name|getPeerConfig
parameter_list|()
block|{
return|return
name|peerConfig
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|getTableCFs
parameter_list|()
block|{
return|return
name|this
operator|.
name|peerConfig
operator|.
name|getTableCFsMap
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getNamespaces
parameter_list|()
block|{
return|return
name|this
operator|.
name|peerConfig
operator|.
name|getNamespaces
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getPeerBandwidth
parameter_list|()
block|{
return|return
name|this
operator|.
name|peerConfig
operator|.
name|getBandwidth
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|registerPeerConfigListener
parameter_list|(
name|ReplicationPeerConfigListener
name|listener
parameter_list|)
block|{
name|this
operator|.
name|peerConfigListeners
operator|.
name|add
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

