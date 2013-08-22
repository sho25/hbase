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
name|java
operator|.
name|util
operator|.
name|UUID
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
name|classification
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
name|ServerName
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
comment|/**    * Add a new remote slave cluster for replication.    * @param peerId a short that identifies the cluster    * @param clusterKey the concatenation of the slave cluster's:    *          hbase.zookeeper.quorum:hbase.zookeeper.property.clientPort:zookeeper.znode.parent    */
name|void
name|addPeer
parameter_list|(
name|String
name|peerId
parameter_list|,
name|String
name|clusterKey
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Removes a remote slave cluster and stops the replication to it.    * @param peerId a short that identifies the cluster    */
name|void
name|removePeer
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
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
comment|/**    * Get the replication status for the specified connected remote slave cluster.    * The value might be read from cache, so it is recommended to    * use {@link #getStatusOfPeerFromBackingStore(String)}    * if reading the state after enabling or disabling it.    * @param peerId a short that identifies the cluster    * @return true if replication is enabled, false otherwise.    */
name|boolean
name|getStatusOfConnectedPeer
parameter_list|(
name|String
name|peerId
parameter_list|)
function_decl|;
comment|/**    * Get the replication status for the specified remote slave cluster, which doesn't    * have to be connected. The state is read directly from the backing store.    * @param peerId a short that identifies the cluster    * @return true if replication is enabled, false otherwise.    * @throws IOException Throws if there's an error contacting the store    */
name|boolean
name|getStatusOfPeerFromBackingStore
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Get a set of all connected remote slave clusters.    * @return set of peer ids    */
name|Set
argument_list|<
name|String
argument_list|>
name|getConnectedPeers
parameter_list|()
function_decl|;
comment|/**    * List the cluster keys of all remote slave clusters (whether they are enabled/disabled or    * connected/disconnected).    * @return A map of peer ids to peer cluster keys    */
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getAllPeerClusterKeys
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
comment|/**    * Attempt to connect to a new remote slave cluster.    * @param peerId a short that identifies the cluster    * @return true if a new connection was made, false if no new connection was made.    */
name|boolean
name|connectToPeer
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Disconnect from a remote slave cluster.    * @param peerId a short that identifies the cluster    */
name|void
name|disconnectFromPeer
parameter_list|(
name|String
name|peerId
parameter_list|)
function_decl|;
comment|/**    * Returns all region servers from given connected remote slave cluster.    * @param peerId a short that identifies the cluster    * @return addresses of all region servers in the peer cluster. Returns an empty list if the peer    *         cluster is unavailable or there are no region servers in the cluster.    */
name|List
argument_list|<
name|ServerName
argument_list|>
name|getRegionServersOfConnectedPeer
parameter_list|(
name|String
name|peerId
parameter_list|)
function_decl|;
comment|/**    * Get the timestamp of the last change in composition of a given peer cluster.    * @param peerId identifier of the peer cluster for which the timestamp is requested    * @return the timestamp (in milliseconds) of the last change to the composition of    *         the peer cluster    */
name|long
name|getTimestampOfLastChangeToPeer
parameter_list|(
name|String
name|peerId
parameter_list|)
function_decl|;
comment|/**    * Returns the UUID of the provided peer id.    * @param peerId the peer's ID that will be converted into a UUID    * @return a UUID or null if the peer cluster does not exist or is not connected.    */
name|UUID
name|getPeerUUID
parameter_list|(
name|String
name|peerId
parameter_list|)
function_decl|;
comment|/**    * Returns the configuration needed to talk to the remote slave cluster.    * @param peerId a short that identifies the cluster    * @return the configuration for the peer cluster, null if it was unable to get the configuration    */
name|Configuration
name|getPeerConf
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
block|}
end_interface

end_unit

