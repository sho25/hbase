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
name|hadoop
operator|.
name|hbase
operator|.
name|classification
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
comment|/**    * Get the peer config object    * @return the ReplicationPeerConfig for this peer    */
specifier|public
name|ReplicationPeerConfig
name|getPeerConfig
parameter_list|()
function_decl|;
comment|/**    * Returns the state of the peer    * @return the enabled state    */
name|PeerState
name|getPeerState
parameter_list|()
function_decl|;
comment|/**    * Get the configuration object required to communicate with this peer    * @return configuration object    */
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
function_decl|;
comment|/**    * Get replicable (table, cf-list) map of this peer    * @return the replicable (table, cf-list) map    */
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
function_decl|;
block|}
end_interface

end_unit

