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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
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

begin_comment
comment|/**  * Perform read/write to the replication peer storage.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|ReplicationPeerStorage
block|{
comment|/**    * Add a replication peer.    * @throws ReplicationException if there are errors accessing the storage service.    */
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
function_decl|;
comment|/**    * Remove a replication peer.    * @throws ReplicationException if there are errors accessing the storage service.    */
name|void
name|removePeer
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Set the state of peer, {@code true} to {@code ENABLED}, otherwise to {@code DISABLED}.    * @throws ReplicationException if there are errors accessing the storage service.    */
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
function_decl|;
comment|/**    * Update the config a replication peer.    * @throws ReplicationException if there are errors accessing the storage service.    */
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
function_decl|;
comment|/**    * Return the peer ids of all replication peers.    * @throws ReplicationException if there are errors accessing the storage service.    */
name|List
argument_list|<
name|String
argument_list|>
name|listPeerIds
parameter_list|()
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Test whether a replication peer is enabled.    * @throws ReplicationException if there are errors accessing the storage service.    */
name|boolean
name|isPeerEnabled
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Get the peer config of a replication peer.    * @throws ReplicationException if there are errors accessing the storage service.    */
name|Optional
argument_list|<
name|ReplicationPeerConfig
argument_list|>
name|getPeerConfig
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

