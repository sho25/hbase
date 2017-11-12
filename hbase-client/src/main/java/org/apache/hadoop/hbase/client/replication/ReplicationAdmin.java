begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
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
name|Closeable
import|;
end_import

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
name|ArrayList
import|;
end_import

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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|HConstants
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
name|ReplicationPeerNotFoundException
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
name|client
operator|.
name|Admin
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
name|Connection
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
name|ConnectionFactory
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
name|ReplicationPeerConfig
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
name|ReplicationPeerDescription
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  *<p>  * This class provides the administrative interface to HBase cluster  * replication.  *</p>  *<p>  * Adding a new peer results in creating new outbound connections from every  * region server to a subset of region servers on the slave cluster. Each  * new stream of replication will start replicating from the beginning of the  * current WAL, meaning that edits from that past will be replicated.  *</p>  *<p>  * Removing a peer is a destructive and irreversible operation that stops  * all the replication streams for the given cluster and deletes the metadata  * used to keep track of the replication state.  *</p>  *<p>  * To see which commands are available in the shell, type  *<code>replication</code>.  *</p>  *  * @deprecated use {@link org.apache.hadoop.hbase.client.Admin} instead.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|Deprecated
specifier|public
class|class
name|ReplicationAdmin
implements|implements
name|Closeable
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ReplicationAdmin
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TNAME
init|=
literal|"tableName"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CFNAME
init|=
literal|"columnFamilyName"
decl_stmt|;
comment|// only Global for now, can add other type
comment|// such as, 1) no global replication, or 2) the table is replicated to this cluster, etc.
specifier|public
specifier|static
specifier|final
name|String
name|REPLICATIONTYPE
init|=
literal|"replicationType"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|REPLICATIONGLOBAL
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|REPLICATIONSERIAL
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_SERIAL
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Connection
name|connection
decl_stmt|;
specifier|private
name|Admin
name|admin
decl_stmt|;
comment|/**    * Constructor that creates a connection to the local ZooKeeper ensemble.    * @param conf Configuration to use    * @throws IOException if an internal replication error occurs    * @throws RuntimeException if replication isn't enabled.    */
specifier|public
name|ReplicationAdmin
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|connection
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|admin
operator|=
name|connection
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
block|}
comment|/**    * Add a new remote slave cluster for replication.    * @param id a short name that identifies the cluster    * @param peerConfig configuration for the replication slave cluster    * @param tableCfs the table and column-family list which will be replicated for this peer.    * A map from tableName to column family names. An empty collection can be passed    * to indicate replicating all column families. Pass null for replicating all table and column    * families    * @deprecated as release of 2.0.0, and it will be removed in 3.0.0,    * use {@link #addPeer(String, ReplicationPeerConfig)} instead.    */
annotation|@
name|Deprecated
specifier|public
name|void
name|addPeer
parameter_list|(
name|String
name|id
parameter_list|,
name|ReplicationPeerConfig
name|peerConfig
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
name|tableCfs
parameter_list|)
throws|throws
name|ReplicationException
throws|,
name|IOException
block|{
if|if
condition|(
name|tableCfs
operator|!=
literal|null
condition|)
block|{
name|peerConfig
operator|.
name|setTableCFsMap
argument_list|(
name|tableCfs
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|admin
operator|.
name|addReplicationPeer
argument_list|(
name|id
argument_list|,
name|peerConfig
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add a new remote slave cluster for replication.    * @param id a short name that identifies the cluster    * @param peerConfig configuration for the replication slave cluster    * @deprecated use    *             {@link org.apache.hadoop.hbase.client.Admin#addReplicationPeer(String, ReplicationPeerConfig)}    *             instead    */
annotation|@
name|Deprecated
specifier|public
name|void
name|addPeer
parameter_list|(
name|String
name|id
parameter_list|,
name|ReplicationPeerConfig
name|peerConfig
parameter_list|)
throws|throws
name|ReplicationException
throws|,
name|IOException
block|{
name|checkNamespacesAndTableCfsConfigConflict
argument_list|(
name|peerConfig
operator|.
name|getNamespaces
argument_list|()
argument_list|,
name|peerConfig
operator|.
name|getTableCFsMap
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|admin
operator|.
name|addReplicationPeer
argument_list|(
name|id
argument_list|,
name|peerConfig
argument_list|)
expr_stmt|;
block|}
comment|/**    *  @deprecated as release of 2.0.0, and it will be removed in 3.0.0    * */
annotation|@
name|Deprecated
specifier|public
specifier|static
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|parseTableCFsFromConfig
parameter_list|(
name|String
name|tableCFsConfig
parameter_list|)
block|{
return|return
name|ReplicationPeerConfigUtil
operator|.
name|parseTableCFsFromConfig
argument_list|(
name|tableCFsConfig
argument_list|)
return|;
block|}
comment|/**    * @deprecated use    *             {@link org.apache.hadoop.hbase.client.Admin#updateReplicationPeerConfig(String, ReplicationPeerConfig)}    *             instead    */
annotation|@
name|Deprecated
specifier|public
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
name|IOException
block|{
name|this
operator|.
name|admin
operator|.
name|updateReplicationPeerConfig
argument_list|(
name|id
argument_list|,
name|peerConfig
argument_list|)
expr_stmt|;
block|}
comment|/**    * Removes a peer cluster and stops the replication to it.    * @param id a short name that identifies the cluster    * @deprecated use {@link org.apache.hadoop.hbase.client.Admin#removeReplicationPeer(String)} instead    */
annotation|@
name|Deprecated
specifier|public
name|void
name|removePeer
parameter_list|(
name|String
name|id
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|admin
operator|.
name|removeReplicationPeer
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
comment|/**    * Restart the replication stream to the specified peer.    * @param id a short name that identifies the cluster    * @deprecated use {@link org.apache.hadoop.hbase.client.Admin#enableReplicationPeer(String)}    *             instead    */
annotation|@
name|Deprecated
specifier|public
name|void
name|enablePeer
parameter_list|(
name|String
name|id
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|admin
operator|.
name|enableReplicationPeer
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
comment|/**    * Stop the replication stream to the specified peer.    * @param id a short name that identifies the cluster    * @deprecated use {@link org.apache.hadoop.hbase.client.Admin#disableReplicationPeer(String)}    *             instead    */
annotation|@
name|Deprecated
specifier|public
name|void
name|disablePeer
parameter_list|(
name|String
name|id
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|admin
operator|.
name|disableReplicationPeer
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the number of slave clusters the local cluster has.    * @return number of slave clusters    * @throws IOException    * @deprecated    */
annotation|@
name|Deprecated
specifier|public
name|int
name|getPeersCount
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|admin
operator|.
name|listReplicationPeers
argument_list|()
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**    * @deprecated use {@link org.apache.hadoop.hbase.client.Admin#listReplicationPeers()} instead    */
annotation|@
name|Deprecated
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|ReplicationPeerConfig
argument_list|>
name|listPeerConfigs
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|ReplicationPeerDescription
argument_list|>
name|peers
init|=
name|this
operator|.
name|admin
operator|.
name|listReplicationPeers
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|ReplicationPeerConfig
argument_list|>
name|result
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ReplicationPeerDescription
name|peer
range|:
name|peers
control|)
block|{
name|result
operator|.
name|put
argument_list|(
name|peer
operator|.
name|getPeerId
argument_list|()
argument_list|,
name|peer
operator|.
name|getPeerConfig
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * @deprecated use {@link org.apache.hadoop.hbase.client.Admin#getReplicationPeerConfig(String)}    *             instead    */
annotation|@
name|Deprecated
specifier|public
name|ReplicationPeerConfig
name|getPeerConfig
parameter_list|(
name|String
name|id
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|admin
operator|.
name|getReplicationPeerConfig
argument_list|(
name|id
argument_list|)
return|;
block|}
comment|/**    * Get the replicable table-cf config of the specified peer.    * @param id a short name that identifies the cluster    * @deprecated as release of 2.0.0, and it will be removed in 3.0.0,    * use {@link #getPeerConfig(String)} instead.    * */
annotation|@
name|Deprecated
specifier|public
name|String
name|getPeerTableCFs
parameter_list|(
name|String
name|id
parameter_list|)
throws|throws
name|IOException
block|{
name|ReplicationPeerConfig
name|peerConfig
init|=
name|admin
operator|.
name|getReplicationPeerConfig
argument_list|(
name|id
argument_list|)
decl_stmt|;
return|return
name|ReplicationPeerConfigUtil
operator|.
name|convertToString
argument_list|(
name|peerConfig
operator|.
name|getTableCFsMap
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Append the replicable table-cf config of the specified peer    * @param id a short that identifies the cluster    * @param tableCfs table-cfs config str    * @throws ReplicationException    * @throws IOException    * @deprecated as release of 2.0.0, and it will be removed in 3.0.0,    * use {@link #appendPeerTableCFs(String, Map)} instead.    */
annotation|@
name|Deprecated
specifier|public
name|void
name|appendPeerTableCFs
parameter_list|(
name|String
name|id
parameter_list|,
name|String
name|tableCfs
parameter_list|)
throws|throws
name|ReplicationException
throws|,
name|IOException
block|{
name|appendPeerTableCFs
argument_list|(
name|id
argument_list|,
name|ReplicationPeerConfigUtil
operator|.
name|parseTableCFsFromConfig
argument_list|(
name|tableCfs
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Append the replicable table-cf config of the specified peer    * @param id a short that identifies the cluster    * @param tableCfs A map from tableName to column family names    * @throws ReplicationException    * @throws IOException    */
annotation|@
name|Deprecated
specifier|public
name|void
name|appendPeerTableCFs
parameter_list|(
name|String
name|id
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
name|tableCfs
parameter_list|)
throws|throws
name|ReplicationException
throws|,
name|IOException
block|{
if|if
condition|(
name|tableCfs
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"tableCfs is null"
argument_list|)
throw|;
block|}
name|ReplicationPeerConfig
name|peerConfig
init|=
name|admin
operator|.
name|getReplicationPeerConfig
argument_list|(
name|id
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|preTableCfs
init|=
name|peerConfig
operator|.
name|getTableCFsMap
argument_list|()
decl_stmt|;
if|if
condition|(
name|preTableCfs
operator|==
literal|null
condition|)
block|{
name|setPeerTableCFs
argument_list|(
name|id
argument_list|,
name|tableCfs
argument_list|)
expr_stmt|;
return|return;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
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
name|entry
range|:
name|tableCfs
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|TableName
name|table
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|String
argument_list|>
name|appendCfs
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|preTableCfs
operator|.
name|containsKey
argument_list|(
name|table
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|cfs
init|=
name|preTableCfs
operator|.
name|get
argument_list|(
name|table
argument_list|)
decl_stmt|;
if|if
condition|(
name|cfs
operator|==
literal|null
operator|||
name|appendCfs
operator|==
literal|null
operator|||
name|appendCfs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|preTableCfs
operator|.
name|put
argument_list|(
name|table
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|cfSet
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|cfs
argument_list|)
decl_stmt|;
name|cfSet
operator|.
name|addAll
argument_list|(
name|appendCfs
argument_list|)
expr_stmt|;
name|preTableCfs
operator|.
name|put
argument_list|(
name|table
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|cfSet
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|appendCfs
operator|==
literal|null
operator|||
name|appendCfs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|preTableCfs
operator|.
name|put
argument_list|(
name|table
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|preTableCfs
operator|.
name|put
argument_list|(
name|table
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|appendCfs
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|updatePeerConfig
argument_list|(
name|id
argument_list|,
name|peerConfig
argument_list|)
expr_stmt|;
block|}
comment|/**    * Remove some table-cfs from table-cfs config of the specified peer    * @param id a short name that identifies the cluster    * @param tableCf table-cfs config str    * @throws ReplicationException    * @throws IOException    * @deprecated as release of 2.0.0, and it will be removed in 3.0.0,    * use {@link #removePeerTableCFs(String, Map)} instead.    */
annotation|@
name|Deprecated
specifier|public
name|void
name|removePeerTableCFs
parameter_list|(
name|String
name|id
parameter_list|,
name|String
name|tableCf
parameter_list|)
throws|throws
name|ReplicationException
throws|,
name|IOException
block|{
name|removePeerTableCFs
argument_list|(
name|id
argument_list|,
name|ReplicationPeerConfigUtil
operator|.
name|parseTableCFsFromConfig
argument_list|(
name|tableCf
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Remove some table-cfs from config of the specified peer    * @param id a short name that identifies the cluster    * @param tableCfs A map from tableName to column family names    * @throws ReplicationException    * @throws IOException    */
annotation|@
name|Deprecated
specifier|public
name|void
name|removePeerTableCFs
parameter_list|(
name|String
name|id
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
name|tableCfs
parameter_list|)
throws|throws
name|ReplicationException
throws|,
name|IOException
block|{
if|if
condition|(
name|tableCfs
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"tableCfs is null"
argument_list|)
throw|;
block|}
name|ReplicationPeerConfig
name|peerConfig
init|=
name|admin
operator|.
name|getReplicationPeerConfig
argument_list|(
name|id
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|preTableCfs
init|=
name|peerConfig
operator|.
name|getTableCFsMap
argument_list|()
decl_stmt|;
if|if
condition|(
name|preTableCfs
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"Table-Cfs for peer"
operator|+
name|id
operator|+
literal|" is null"
argument_list|)
throw|;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
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
name|entry
range|:
name|tableCfs
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|TableName
name|table
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|String
argument_list|>
name|removeCfs
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|preTableCfs
operator|.
name|containsKey
argument_list|(
name|table
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|cfs
init|=
name|preTableCfs
operator|.
name|get
argument_list|(
name|table
argument_list|)
decl_stmt|;
if|if
condition|(
name|cfs
operator|==
literal|null
operator|&&
operator|(
name|removeCfs
operator|==
literal|null
operator|||
name|removeCfs
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|preTableCfs
operator|.
name|remove
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cfs
operator|!=
literal|null
operator|&&
operator|(
name|removeCfs
operator|!=
literal|null
operator|&&
operator|!
name|removeCfs
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|cfSet
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|cfs
argument_list|)
decl_stmt|;
name|cfSet
operator|.
name|removeAll
argument_list|(
name|removeCfs
argument_list|)
expr_stmt|;
if|if
condition|(
name|cfSet
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|preTableCfs
operator|.
name|remove
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|preTableCfs
operator|.
name|put
argument_list|(
name|table
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|cfSet
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|cfs
operator|==
literal|null
operator|&&
operator|(
name|removeCfs
operator|!=
literal|null
operator|&&
operator|!
name|removeCfs
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"Cannot remove cf of table: "
operator|+
name|table
operator|+
literal|" which doesn't specify cfs from table-cfs config in peer: "
operator|+
name|id
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|cfs
operator|!=
literal|null
operator|&&
operator|(
name|removeCfs
operator|==
literal|null
operator|||
name|removeCfs
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"Cannot remove table: "
operator|+
name|table
operator|+
literal|" which has specified cfs from table-cfs config in peer: "
operator|+
name|id
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"No table: "
operator|+
name|table
operator|+
literal|" in table-cfs config of peer: "
operator|+
name|id
argument_list|)
throw|;
block|}
block|}
name|updatePeerConfig
argument_list|(
name|id
argument_list|,
name|peerConfig
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the replicable table-cf config of the specified peer    * @param id a short name that identifies the cluster    * @param tableCfs the table and column-family list which will be replicated for this peer.    * A map from tableName to column family names. An empty collection can be passed    * to indicate replicating all column families. Pass null for replicating all table and column    * families    */
annotation|@
name|Deprecated
specifier|public
name|void
name|setPeerTableCFs
parameter_list|(
name|String
name|id
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
name|tableCfs
parameter_list|)
throws|throws
name|IOException
block|{
name|ReplicationPeerConfig
name|peerConfig
init|=
name|getPeerConfig
argument_list|(
name|id
argument_list|)
decl_stmt|;
name|peerConfig
operator|.
name|setTableCFsMap
argument_list|(
name|tableCfs
argument_list|)
expr_stmt|;
name|updatePeerConfig
argument_list|(
name|id
argument_list|,
name|peerConfig
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the state of the specified peer cluster    * @param id String format of the Short name that identifies the peer,    * an IllegalArgumentException is thrown if it doesn't exist    * @return true if replication is enabled to that peer, false if it isn't    */
annotation|@
name|Deprecated
specifier|public
name|boolean
name|getPeerState
parameter_list|(
name|String
name|id
parameter_list|)
throws|throws
name|ReplicationException
throws|,
name|IOException
block|{
name|List
argument_list|<
name|ReplicationPeerDescription
argument_list|>
name|peers
init|=
name|admin
operator|.
name|listReplicationPeers
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
name|id
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|peers
operator|.
name|isEmpty
argument_list|()
operator|||
operator|!
name|id
operator|.
name|equals
argument_list|(
name|peers
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getPeerId
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ReplicationPeerNotFoundException
argument_list|(
name|id
argument_list|)
throw|;
block|}
return|return
name|peers
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|isEnabled
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|connection
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Find all column families that are replicated from this cluster    * @return the full list of the replicated column families of this cluster as:    *        tableName, family name, replicationType    *    * Currently replicationType is Global. In the future, more replication    * types may be extended here. For example    *  1) the replication may only apply to selected peers instead of all peers    *  2) the replicationType may indicate the host Cluster servers as Slave    *     for the table:columnFam.    * @deprecated use {@link org.apache.hadoop.hbase.client.Admin#listReplicatedTableCFs()} instead    */
annotation|@
name|Deprecated
specifier|public
name|List
argument_list|<
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|listReplicated
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|replicationColFams
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|admin
operator|.
name|listReplicatedTableCFs
argument_list|()
operator|.
name|forEach
argument_list|(
parameter_list|(
name|tableCFs
parameter_list|)
lambda|->
block|{
name|String
name|table
init|=
name|tableCFs
operator|.
name|getTable
argument_list|()
operator|.
name|getNameAsString
argument_list|()
decl_stmt|;
name|tableCFs
operator|.
name|getColumnFamilyMap
argument_list|()
operator|.
name|forEach
argument_list|(
parameter_list|(
name|cf
parameter_list|,
name|scope
parameter_list|)
lambda|->
block|{
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|replicationEntry
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|replicationEntry
operator|.
name|put
argument_list|(
name|TNAME
argument_list|,
name|table
argument_list|)
expr_stmt|;
name|replicationEntry
operator|.
name|put
argument_list|(
name|CFNAME
argument_list|,
name|cf
argument_list|)
expr_stmt|;
name|replicationEntry
operator|.
name|put
argument_list|(
name|REPLICATIONTYPE
argument_list|,
name|scope
operator|==
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
condition|?
name|REPLICATIONGLOBAL
else|:
name|REPLICATIONSERIAL
argument_list|)
expr_stmt|;
name|replicationColFams
operator|.
name|add
argument_list|(
name|replicationEntry
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
return|return
name|replicationColFams
return|;
block|}
comment|/**    * Enable a table's replication switch.    * @param tableName name of the table    * @throws IOException if a remote or network exception occurs    * @deprecated use {@link org.apache.hadoop.hbase.client.Admin#enableTableReplication(TableName)}    *             instead    */
annotation|@
name|Deprecated
specifier|public
name|void
name|enableTableRep
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|admin
operator|.
name|enableTableReplication
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|/**    * Disable a table's replication switch.    * @param tableName name of the table    * @throws IOException if a remote or network exception occurs    * @deprecated use {@link org.apache.hadoop.hbase.client.Admin#disableTableReplication(TableName)}    *             instead    */
annotation|@
name|Deprecated
specifier|public
name|void
name|disableTableRep
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|admin
operator|.
name|disableTableReplication
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|/**    * @deprecated use {@link org.apache.hadoop.hbase.client.Admin#listReplicationPeers()} instead    */
annotation|@
name|VisibleForTesting
annotation|@
name|Deprecated
name|List
argument_list|<
name|ReplicationPeerDescription
argument_list|>
name|listReplicationPeers
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|admin
operator|.
name|listReplicationPeers
argument_list|()
return|;
block|}
comment|/**    * Set a namespace in the peer config means that all tables in this namespace    * will be replicated to the peer cluster.    *    * 1. If you already have set a namespace in the peer config, then you can't set any table    *    of this namespace to the peer config.    * 2. If you already have set a table in the peer config, then you can't set this table's    *    namespace to the peer config.    *    * @param namespaces    * @param tableCfs    * @throws ReplicationException    */
specifier|private
name|void
name|checkNamespacesAndTableCfsConfigConflict
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|namespaces
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
name|tableCfs
parameter_list|)
throws|throws
name|ReplicationException
block|{
if|if
condition|(
name|namespaces
operator|==
literal|null
operator|||
name|namespaces
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|tableCfs
operator|==
literal|null
operator|||
name|tableCfs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
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
name|entry
range|:
name|tableCfs
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|TableName
name|table
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
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
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"Table-cfs config conflict with namespaces config in peer"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

