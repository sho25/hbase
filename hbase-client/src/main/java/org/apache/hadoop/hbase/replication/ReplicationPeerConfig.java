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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|Bytes
import|;
end_import

begin_comment
comment|/**  * A configuration for the replication peer cluster.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|ReplicationPeerConfig
block|{
specifier|private
name|String
name|clusterKey
decl_stmt|;
specifier|private
name|String
name|replicationEndpointImpl
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|peerData
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|configuration
decl_stmt|;
specifier|private
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
name|tableCFsMap
init|=
literal|null
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|namespaces
init|=
literal|null
decl_stmt|;
comment|// Default value is true, means replicate all user tables to peer cluster.
specifier|private
name|boolean
name|replicateAllUserTables
init|=
literal|true
decl_stmt|;
specifier|private
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
name|excludeTableCFsMap
init|=
literal|null
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|excludeNamespaces
init|=
literal|null
decl_stmt|;
specifier|private
name|long
name|bandwidth
init|=
literal|0
decl_stmt|;
specifier|private
name|ReplicationPeerConfig
parameter_list|(
name|ReplicationPeerConfigBuilderImpl
name|builder
parameter_list|)
block|{
name|this
operator|.
name|clusterKey
operator|=
name|builder
operator|.
name|clusterKey
expr_stmt|;
name|this
operator|.
name|replicationEndpointImpl
operator|=
name|builder
operator|.
name|replicationEndpointImpl
expr_stmt|;
name|this
operator|.
name|peerData
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|builder
operator|.
name|peerData
argument_list|)
expr_stmt|;
name|this
operator|.
name|configuration
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|builder
operator|.
name|configuration
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableCFsMap
operator|=
name|builder
operator|.
name|tableCFsMap
operator|!=
literal|null
condition|?
name|unmodifiableTableCFsMap
argument_list|(
name|builder
operator|.
name|tableCFsMap
argument_list|)
else|:
literal|null
expr_stmt|;
name|this
operator|.
name|namespaces
operator|=
name|builder
operator|.
name|namespaces
operator|!=
literal|null
condition|?
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
name|builder
operator|.
name|namespaces
argument_list|)
else|:
literal|null
expr_stmt|;
name|this
operator|.
name|replicateAllUserTables
operator|=
name|builder
operator|.
name|replicateAllUserTables
expr_stmt|;
name|this
operator|.
name|excludeTableCFsMap
operator|=
name|builder
operator|.
name|excludeTableCFsMap
operator|!=
literal|null
condition|?
name|unmodifiableTableCFsMap
argument_list|(
name|builder
operator|.
name|excludeTableCFsMap
argument_list|)
else|:
literal|null
expr_stmt|;
name|this
operator|.
name|excludeNamespaces
operator|=
name|builder
operator|.
name|excludeNamespaces
operator|!=
literal|null
condition|?
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
name|builder
operator|.
name|excludeNamespaces
argument_list|)
else|:
literal|null
expr_stmt|;
name|this
operator|.
name|bandwidth
operator|=
name|builder
operator|.
name|bandwidth
expr_stmt|;
block|}
specifier|private
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|unmodifiableTableCFsMap
parameter_list|(
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableCFsMap
parameter_list|)
block|{
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|newTableCFsMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|tableCFsMap
operator|.
name|forEach
argument_list|(
parameter_list|(
name|table
parameter_list|,
name|cfs
parameter_list|)
lambda|->
name|newTableCFsMap
operator|.
name|put
argument_list|(
name|table
argument_list|,
name|cfs
operator|!=
literal|null
condition|?
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|cfs
argument_list|)
else|:
literal|null
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|newTableCFsMap
argument_list|)
return|;
block|}
comment|/**    * @deprecated as release of 2.0.0, and it will be removed in 3.0.0. Use    *             {@link ReplicationPeerConfigBuilder} to create new ReplicationPeerConfig.    */
annotation|@
name|Deprecated
specifier|public
name|ReplicationPeerConfig
parameter_list|()
block|{
name|this
operator|.
name|peerData
operator|=
operator|new
name|TreeMap
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
expr_stmt|;
name|this
operator|.
name|configuration
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the clusterKey which is the concatenation of the slave cluster's:    * hbase.zookeeper.quorum:hbase.zookeeper.property.clientPort:zookeeper.znode.parent    * @deprecated as release of 2.0.0, and it will be removed in 3.0.0. Use    *             {@link ReplicationPeerConfigBuilder#setClusterKey(String)} instead.    */
annotation|@
name|Deprecated
specifier|public
name|ReplicationPeerConfig
name|setClusterKey
parameter_list|(
name|String
name|clusterKey
parameter_list|)
block|{
name|this
operator|.
name|clusterKey
operator|=
name|clusterKey
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Sets the ReplicationEndpoint plugin class for this peer.    * @param replicationEndpointImpl a class implementing ReplicationEndpoint    * @deprecated as release of 2.0.0, and it will be removed in 3.0.0. Use    *             {@link ReplicationPeerConfigBuilder#setReplicationEndpointImpl(String)} instead.    */
annotation|@
name|Deprecated
specifier|public
name|ReplicationPeerConfig
name|setReplicationEndpointImpl
parameter_list|(
name|String
name|replicationEndpointImpl
parameter_list|)
block|{
name|this
operator|.
name|replicationEndpointImpl
operator|=
name|replicationEndpointImpl
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|String
name|getClusterKey
parameter_list|()
block|{
return|return
name|clusterKey
return|;
block|}
specifier|public
name|String
name|getReplicationEndpointImpl
parameter_list|()
block|{
return|return
name|replicationEndpointImpl
return|;
block|}
specifier|public
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|getPeerData
parameter_list|()
block|{
return|return
name|peerData
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getConfiguration
parameter_list|()
block|{
return|return
name|configuration
return|;
block|}
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
name|getTableCFsMap
parameter_list|()
block|{
return|return
operator|(
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
operator|)
name|tableCFsMap
return|;
block|}
comment|/**    * @deprecated as release of 2.0.0, and it will be removed in 3.0.0. Use    *             {@link ReplicationPeerConfigBuilder#setTableCFsMap(Map)} instead.    */
annotation|@
name|Deprecated
specifier|public
name|ReplicationPeerConfig
name|setTableCFsMap
parameter_list|(
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
name|tableCFsMap
parameter_list|)
block|{
name|this
operator|.
name|tableCFsMap
operator|=
name|tableCFsMap
expr_stmt|;
return|return
name|this
return|;
block|}
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
name|namespaces
return|;
block|}
comment|/**    * @deprecated as release of 2.0.0, and it will be removed in 3.0.0. Use    *             {@link ReplicationPeerConfigBuilder#setNamespaces(Set)} instead.    */
annotation|@
name|Deprecated
specifier|public
name|ReplicationPeerConfig
name|setNamespaces
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|namespaces
parameter_list|)
block|{
name|this
operator|.
name|namespaces
operator|=
name|namespaces
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|long
name|getBandwidth
parameter_list|()
block|{
return|return
name|this
operator|.
name|bandwidth
return|;
block|}
comment|/**    * @deprecated as release of 2.0.0, and it will be removed in 3.0.0. Use    *             {@link ReplicationPeerConfigBuilder#setBandwidth(long)} instead.    */
annotation|@
name|Deprecated
specifier|public
name|ReplicationPeerConfig
name|setBandwidth
parameter_list|(
name|long
name|bandwidth
parameter_list|)
block|{
name|this
operator|.
name|bandwidth
operator|=
name|bandwidth
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|boolean
name|replicateAllUserTables
parameter_list|()
block|{
return|return
name|this
operator|.
name|replicateAllUserTables
return|;
block|}
comment|/**    * @deprecated as release of 2.0.0, and it will be removed in 3.0.0. Use    *             {@link ReplicationPeerConfigBuilder#setReplicateAllUserTables(boolean)} instead.    */
annotation|@
name|Deprecated
specifier|public
name|ReplicationPeerConfig
name|setReplicateAllUserTables
parameter_list|(
name|boolean
name|replicateAllUserTables
parameter_list|)
block|{
name|this
operator|.
name|replicateAllUserTables
operator|=
name|replicateAllUserTables
expr_stmt|;
return|return
name|this
return|;
block|}
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
name|getExcludeTableCFsMap
parameter_list|()
block|{
return|return
operator|(
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
operator|)
name|excludeTableCFsMap
return|;
block|}
comment|/**    * @deprecated as release of 2.0.0, and it will be removed in 3.0.0. Use    *             {@link ReplicationPeerConfigBuilder#setExcludeTableCFsMap(Map)} instead.    */
annotation|@
name|Deprecated
specifier|public
name|ReplicationPeerConfig
name|setExcludeTableCFsMap
parameter_list|(
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
name|tableCFsMap
parameter_list|)
block|{
name|this
operator|.
name|excludeTableCFsMap
operator|=
name|tableCFsMap
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getExcludeNamespaces
parameter_list|()
block|{
return|return
name|this
operator|.
name|excludeNamespaces
return|;
block|}
comment|/**    * @deprecated as release of 2.0.0, and it will be removed in 3.0.0. Use    *             {@link ReplicationPeerConfigBuilder#setExcludeNamespaces(Set)} instead.    */
annotation|@
name|Deprecated
specifier|public
name|ReplicationPeerConfig
name|setExcludeNamespaces
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|namespaces
parameter_list|)
block|{
name|this
operator|.
name|excludeNamespaces
operator|=
name|namespaces
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
specifier|static
name|ReplicationPeerConfigBuilder
name|newBuilder
parameter_list|()
block|{
return|return
operator|new
name|ReplicationPeerConfigBuilderImpl
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|ReplicationPeerConfigBuilder
name|newBuilder
parameter_list|(
name|ReplicationPeerConfig
name|peerConfig
parameter_list|)
block|{
name|ReplicationPeerConfigBuilderImpl
name|builder
init|=
operator|new
name|ReplicationPeerConfigBuilderImpl
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setClusterKey
argument_list|(
name|peerConfig
operator|.
name|getClusterKey
argument_list|()
argument_list|)
operator|.
name|setReplicationEndpointImpl
argument_list|(
name|peerConfig
operator|.
name|getReplicationEndpointImpl
argument_list|()
argument_list|)
operator|.
name|setPeerData
argument_list|(
name|peerConfig
operator|.
name|getPeerData
argument_list|()
argument_list|)
operator|.
name|setConfiguration
argument_list|(
name|peerConfig
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|setTableCFsMap
argument_list|(
name|peerConfig
operator|.
name|getTableCFsMap
argument_list|()
argument_list|)
operator|.
name|setNamespaces
argument_list|(
name|peerConfig
operator|.
name|getNamespaces
argument_list|()
argument_list|)
operator|.
name|setReplicateAllUserTables
argument_list|(
name|peerConfig
operator|.
name|replicateAllUserTables
argument_list|()
argument_list|)
operator|.
name|setExcludeTableCFsMap
argument_list|(
name|peerConfig
operator|.
name|getExcludeTableCFsMap
argument_list|()
argument_list|)
operator|.
name|setExcludeNamespaces
argument_list|(
name|peerConfig
operator|.
name|getExcludeNamespaces
argument_list|()
argument_list|)
operator|.
name|setBandwidth
argument_list|(
name|peerConfig
operator|.
name|getBandwidth
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
specifier|static
class|class
name|ReplicationPeerConfigBuilderImpl
implements|implements
name|ReplicationPeerConfigBuilder
block|{
specifier|private
name|String
name|clusterKey
decl_stmt|;
specifier|private
name|String
name|replicationEndpointImpl
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|peerData
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|configuration
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableCFsMap
init|=
literal|null
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|namespaces
init|=
literal|null
decl_stmt|;
comment|// Default value is true, means replicate all user tables to peer cluster.
specifier|private
name|boolean
name|replicateAllUserTables
init|=
literal|true
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|excludeTableCFsMap
init|=
literal|null
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|excludeNamespaces
init|=
literal|null
decl_stmt|;
specifier|private
name|long
name|bandwidth
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ReplicationPeerConfigBuilder
name|setClusterKey
parameter_list|(
name|String
name|clusterKey
parameter_list|)
block|{
name|this
operator|.
name|clusterKey
operator|=
name|clusterKey
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReplicationPeerConfigBuilder
name|setReplicationEndpointImpl
parameter_list|(
name|String
name|replicationEndpointImpl
parameter_list|)
block|{
name|this
operator|.
name|replicationEndpointImpl
operator|=
name|replicationEndpointImpl
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReplicationPeerConfigBuilder
name|setPeerData
parameter_list|(
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|peerData
parameter_list|)
block|{
name|this
operator|.
name|peerData
operator|=
name|peerData
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReplicationPeerConfigBuilder
name|setConfiguration
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|configuration
parameter_list|)
block|{
name|this
operator|.
name|configuration
operator|=
name|configuration
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReplicationPeerConfigBuilder
name|setTableCFsMap
parameter_list|(
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableCFsMap
parameter_list|)
block|{
name|this
operator|.
name|tableCFsMap
operator|=
name|tableCFsMap
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReplicationPeerConfigBuilder
name|setNamespaces
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|namespaces
parameter_list|)
block|{
name|this
operator|.
name|namespaces
operator|=
name|namespaces
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReplicationPeerConfigBuilder
name|setReplicateAllUserTables
parameter_list|(
name|boolean
name|replicateAllUserTables
parameter_list|)
block|{
name|this
operator|.
name|replicateAllUserTables
operator|=
name|replicateAllUserTables
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReplicationPeerConfigBuilder
name|setExcludeTableCFsMap
parameter_list|(
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|excludeTableCFsMap
parameter_list|)
block|{
name|this
operator|.
name|excludeTableCFsMap
operator|=
name|excludeTableCFsMap
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReplicationPeerConfigBuilder
name|setExcludeNamespaces
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|excludeNamespaces
parameter_list|)
block|{
name|this
operator|.
name|excludeNamespaces
operator|=
name|excludeNamespaces
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReplicationPeerConfigBuilder
name|setBandwidth
parameter_list|(
name|long
name|bandwidth
parameter_list|)
block|{
name|this
operator|.
name|bandwidth
operator|=
name|bandwidth
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReplicationPeerConfig
name|build
parameter_list|()
block|{
return|return
operator|new
name|ReplicationPeerConfig
argument_list|(
name|this
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"clusterKey="
argument_list|)
operator|.
name|append
argument_list|(
name|clusterKey
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"replicationEndpointImpl="
argument_list|)
operator|.
name|append
argument_list|(
name|replicationEndpointImpl
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"replicateAllUserTables="
argument_list|)
operator|.
name|append
argument_list|(
name|replicateAllUserTables
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
if|if
condition|(
name|replicateAllUserTables
condition|)
block|{
if|if
condition|(
name|excludeNamespaces
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|"excludeNamespaces="
argument_list|)
operator|.
name|append
argument_list|(
name|excludeNamespaces
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|excludeTableCFsMap
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|"excludeTableCFsMap="
argument_list|)
operator|.
name|append
argument_list|(
name|excludeTableCFsMap
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|namespaces
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|"namespaces="
argument_list|)
operator|.
name|append
argument_list|(
name|namespaces
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|tableCFsMap
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|"tableCFs="
argument_list|)
operator|.
name|append
argument_list|(
name|tableCFsMap
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|append
argument_list|(
literal|"bandwidth="
argument_list|)
operator|.
name|append
argument_list|(
name|bandwidth
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Decide whether the table need replicate to the peer cluster    * @param table name of the table    * @return true if the table need replicate to the peer cluster    */
specifier|public
name|boolean
name|needToReplicate
parameter_list|(
name|TableName
name|table
parameter_list|)
block|{
if|if
condition|(
name|replicateAllUserTables
condition|)
block|{
if|if
condition|(
name|excludeNamespaces
operator|!=
literal|null
operator|&&
name|excludeNamespaces
operator|.
name|contains
argument_list|(
name|table
operator|.
name|getNamespaceAsString
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|excludeTableCFsMap
operator|!=
literal|null
operator|&&
name|excludeTableCFsMap
operator|.
name|containsKey
argument_list|(
name|table
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
else|else
block|{
if|if
condition|(
name|namespaces
operator|!=
literal|null
operator|&&
name|namespaces
operator|.
name|contains
argument_list|(
name|table
operator|.
name|getNamespaceAsString
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|tableCFsMap
operator|!=
literal|null
operator|&&
name|tableCFsMap
operator|.
name|containsKey
argument_list|(
name|table
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

