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
name|HBaseInterfaceAudience
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

begin_comment
comment|/**  * ReplicationPeer manages enabled / disabled state for the peer.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|REPLICATION
argument_list|)
specifier|public
interface|interface
name|ReplicationPeer
block|{
comment|/**    * State of the peer, whether it is enabled or not    */
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|REPLICATION
argument_list|)
enum|enum
name|PeerState
block|{
name|ENABLED
block|,
name|DISABLED
block|}
comment|/**    * Get the identifier of this peer    * @return string representation of the id    */
name|String
name|getId
parameter_list|()
function_decl|;
comment|/**    * Returns the state of the peer by reading local cache.    * @return the enabled state    */
name|PeerState
name|getPeerState
parameter_list|()
function_decl|;
comment|/**    * Returns the sync replication state of the peer by reading local cache.    *<p>    * If the peer is not a synchronous replication peer, a {@link SyncReplicationState#NONE} will be    * returned.    * @return the sync replication state    */
name|SyncReplicationState
name|getSyncReplicationState
parameter_list|()
function_decl|;
comment|/**    * Test whether the peer is enabled.    * @return {@code true} if enabled, otherwise {@code false}.    */
specifier|default
name|boolean
name|isPeerEnabled
parameter_list|()
block|{
return|return
name|getPeerState
argument_list|()
operator|==
name|PeerState
operator|.
name|ENABLED
return|;
block|}
comment|/**    * Get the peer config object    * @return the ReplicationPeerConfig for this peer    */
name|ReplicationPeerConfig
name|getPeerConfig
parameter_list|()
function_decl|;
comment|/**    * Get the configuration object required to communicate with this peer    * @return configuration object    */
name|Configuration
name|getConfiguration
parameter_list|()
function_decl|;
comment|/**    * Get replicable (table, cf-list) map of this peer    * @return the replicable (table, cf-list) map    */
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
function_decl|;
comment|/**    * Get replicable namespace set of this peer    * @return the replicable namespaces set    */
name|Set
argument_list|<
name|String
argument_list|>
name|getNamespaces
parameter_list|()
function_decl|;
comment|/**    * Get the per node bandwidth upper limit for this peer    * @return the bandwidth upper limit    */
name|long
name|getPeerBandwidth
parameter_list|()
function_decl|;
comment|/**    * Register a peer config listener to catch the peer config change event.    * @param listener listener to catch the peer config change event.    */
name|void
name|registerPeerConfigListener
parameter_list|(
name|ReplicationPeerConfigListener
name|listener
parameter_list|)
function_decl|;
comment|/**    * @deprecated since 2.1.0 and will be removed in 4.0.0. Use    *   {@link #registerPeerConfigListener(ReplicationPeerConfigListener)} instead.    * @see #registerPeerConfigListener(ReplicationPeerConfigListener)    * @see<a href="https://issues.apache.org/jira/browse/HBASE-10573">HBASE-19573</a>    */
annotation|@
name|Deprecated
specifier|default
name|void
name|trackPeerConfigChanges
parameter_list|(
name|ReplicationPeerConfigListener
name|listener
parameter_list|)
block|{
name|registerPeerConfigListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
end_interface

end_unit

