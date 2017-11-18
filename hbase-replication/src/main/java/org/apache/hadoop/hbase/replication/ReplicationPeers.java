begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Collection
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
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Pair
import|;
end_import

begin_comment
comment|/**  * This provides an interface for maintaining a set of peer clusters. These peers are remote slave  * clusters that data is replicated to. A peer cluster can be in three different states:  *  * 1. Not-Registered - There is no notion of the peer cluster.  * 2. Registered - The peer has an id and is being tracked but there is no connection.  * 3. Connected - There is an active connection to the remote peer.  *  * In the registered or connected state, a peer cluster can either be enabled or disabled.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|ReplicationPeers
block|{
comment|/**    * Initialize the ReplicationPeers interface.    */
name|void
name|init
parameter_list|()
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Add a new remote slave cluster for replication.    * @param peerId a short that identifies the cluster    * @param peerConfig configuration for the replication slave cluster    */
specifier|default
name|void
name|registerPeer
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
name|registerPeer
argument_list|(
name|peerId
argument_list|,
name|peerConfig
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add a new remote slave cluster for replication.    * @param peerId a short that identifies the cluster    * @param peerConfig configuration for the replication slave cluster    * @param enabled peer state, true if ENABLED and false if DISABLED    */
name|void
name|registerPeer
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
function_decl|;
comment|/**    * Removes a remote slave cluster and stops the replication to it.    * @param peerId a short that identifies the cluster    */
name|void
name|unregisterPeer
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Method called after a peer has been connected. It will create a ReplicationPeer to track the    * newly connected cluster.    * @param peerId a short that identifies the cluster    * @return whether a ReplicationPeer was successfully created    * @throws ReplicationException    */
name|boolean
name|peerConnected
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Method called after a peer has been disconnected. It will remove the ReplicationPeer that    * tracked the disconnected cluster.    * @param peerId a short that identifies the cluster    */
name|void
name|peerDisconnected
parameter_list|(
name|String
name|peerId
parameter_list|)
function_decl|;
comment|/**    * Restart the replication to the specified remote slave cluster.    * @param peerId a short that identifies the cluster    */
name|void
name|enablePeer
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Stop the replication to the specified remote slave cluster.    * @param peerId a short that identifies the cluster    */
name|void
name|disablePeer
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Get the table and column-family list string of the peer from the underlying storage.    * @param peerId a short that identifies the cluster    */
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
name|getPeerTableCFsConfig
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Set the table and column-family list string of the peer to the underlying storage.    * @param peerId a short that identifies the cluster    * @param tableCFs the table and column-family list which will be replicated for this peer    */
specifier|public
name|void
name|setPeerTableCFsConfig
parameter_list|(
name|String
name|peerId
parameter_list|,
name|Map
argument_list|<
name|TableName
argument_list|,
name|?
extends|extends
name|Collection
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableCFs
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Returns the ReplicationPeer for the specified connected peer. This ReplicationPeer will    * continue to track changes to the Peer's state and config. This method returns null if no    * peer has been connected with the given peerId.    * @param peerId id for the peer    * @return ReplicationPeer object    */
name|ReplicationPeer
name|getConnectedPeer
parameter_list|(
name|String
name|peerId
parameter_list|)
function_decl|;
comment|/**    * Returns the set of peerIds of the clusters that have been connected and have an underlying    * ReplicationPeer.    * @return a Set of Strings for peerIds    */
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getConnectedPeerIds
parameter_list|()
function_decl|;
comment|/**    * Get the replication status for the specified connected remote slave cluster.    * The value might be read from cache, so it is recommended to    * use {@link #getStatusOfPeerFromBackingStore(String)}    * if reading the state after enabling or disabling it.    * @param peerId a short that identifies the cluster    * @return true if replication is enabled, false otherwise.    */
name|boolean
name|getStatusOfPeer
parameter_list|(
name|String
name|peerId
parameter_list|)
function_decl|;
comment|/**    * Get the replication status for the specified remote slave cluster, which doesn't    * have to be connected. The state is read directly from the backing store.    * @param peerId a short that identifies the cluster    * @return true if replication is enabled, false otherwise.    * @throws ReplicationException thrown if there's an error contacting the store    */
name|boolean
name|getStatusOfPeerFromBackingStore
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * List the cluster replication configs of all remote slave clusters (whether they are    * enabled/disabled or connected/disconnected).    * @return A map of peer ids to peer cluster keys    */
name|Map
argument_list|<
name|String
argument_list|,
name|ReplicationPeerConfig
argument_list|>
name|getAllPeerConfigs
parameter_list|()
function_decl|;
comment|/**    * List the peer ids of all remote slave clusters (whether they are enabled/disabled or    * connected/disconnected).    * @return A list of peer ids    */
name|List
argument_list|<
name|String
argument_list|>
name|getAllPeerIds
parameter_list|()
function_decl|;
comment|/**    * Returns the configured ReplicationPeerConfig for this peerId    * @param peerId a short name that identifies the cluster    * @return ReplicationPeerConfig for the peer    */
name|ReplicationPeerConfig
name|getReplicationPeerConfig
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Returns the configuration needed to talk to the remote slave cluster.    * @param peerId a short that identifies the cluster    * @return the configuration for the peer cluster, null if it was unable to get the configuration    */
name|Pair
argument_list|<
name|ReplicationPeerConfig
argument_list|,
name|Configuration
argument_list|>
name|getPeerConf
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Update the peerConfig for the a given peer cluster    * @param id a short that identifies the cluster    * @param peerConfig new config for the peer cluster    * @throws ReplicationException    */
name|void
name|updatePeerConfig
parameter_list|(
name|String
name|id
parameter_list|,
name|ReplicationPeerConfig
name|peerConfig
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
block|}
end_interface

end_unit

