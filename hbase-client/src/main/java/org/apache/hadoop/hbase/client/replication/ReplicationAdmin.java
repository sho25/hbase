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
name|Map
operator|.
name|Entry
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
name|commons
operator|.
name|lang
operator|.
name|StringUtils
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
name|Abortable
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
name|HColumnDescriptor
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
name|HTableDescriptor
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
name|InterfaceStability
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
name|ReplicationFactory
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
name|ReplicationPeers
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
name|ReplicationQueuesClient
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
name|ZooKeeperWatcher
import|;
end_import

begin_import
import|import
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
comment|/**  *<p>  * This class provides the administrative interface to HBase cluster  * replication. In order to use it, the cluster and the client using  * ReplicationAdmin must be configured with<code>hbase.replication</code>  * set to true.  *</p>  *<p>  * Adding a new peer results in creating new outbound connections from every  * region server to a subset of region servers on the slave cluster. Each  * new stream of replication will start replicating from the beginning of the  * current WAL, meaning that edits from that past will be replicated.  *</p>  *<p>  * Removing a peer is a destructive and irreversible operation that stops  * all the replication streams for the given cluster and deletes the metadata  * used to keep track of the replication state.  *</p>  *<p>  * To see which commands are available in the shell, type  *<code>replication</code>.  *</p>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
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
literal|"columnFamlyName"
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
specifier|private
specifier|final
name|Connection
name|connection
decl_stmt|;
comment|// TODO: replication should be managed by master. All the classes except ReplicationAdmin should
comment|// be moved to hbase-server. Resolve it in HBASE-11392.
specifier|private
specifier|final
name|ReplicationQueuesClient
name|replicationQueuesClient
decl_stmt|;
specifier|private
specifier|final
name|ReplicationPeers
name|replicationPeers
decl_stmt|;
comment|/**    * A watcher used by replicationPeers and replicationQueuesClient. Keep reference so can dispose    * on {@link #close()}.    */
specifier|private
specifier|final
name|ZooKeeperWatcher
name|zkw
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
if|if
condition|(
operator|!
name|conf
operator|.
name|getBoolean
argument_list|(
name|HConstants
operator|.
name|REPLICATION_ENABLE_KEY
argument_list|,
name|HConstants
operator|.
name|REPLICATION_ENABLE_DEFAULT
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"hbase.replication isn't true, please "
operator|+
literal|"enable it in order to use replication"
argument_list|)
throw|;
block|}
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
name|zkw
operator|=
name|createZooKeeperWatcher
argument_list|()
expr_stmt|;
try|try
block|{
name|this
operator|.
name|replicationPeers
operator|=
name|ReplicationFactory
operator|.
name|getReplicationPeers
argument_list|(
name|zkw
argument_list|,
name|conf
argument_list|,
name|this
operator|.
name|connection
argument_list|)
expr_stmt|;
name|this
operator|.
name|replicationPeers
operator|.
name|init
argument_list|()
expr_stmt|;
name|this
operator|.
name|replicationQueuesClient
operator|=
name|ReplicationFactory
operator|.
name|getReplicationQueuesClient
argument_list|(
name|zkw
argument_list|,
name|conf
argument_list|,
name|this
operator|.
name|connection
argument_list|)
expr_stmt|;
name|this
operator|.
name|replicationQueuesClient
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ReplicationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Error initializing the replication admin client."
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|ZooKeeperWatcher
name|createZooKeeperWatcher
parameter_list|()
throws|throws
name|IOException
block|{
comment|// This Abortable doesn't 'abort'... it just logs.
return|return
operator|new
name|ZooKeeperWatcher
argument_list|(
name|connection
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|"ReplicationAdmin"
argument_list|,
operator|new
name|Abortable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|why
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|// We used to call system.exit here but this script can be embedded by other programs that
comment|// want to do replication stuff... so inappropriate calling System.exit. Just log for now.
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
argument_list|)
return|;
block|}
comment|/**    * Add a new peer cluster to replicate to.    * @param id a short name that identifies the cluster    * @param clusterKey the concatenation of the slave cluster's    *<code>hbase.zookeeper.quorum:hbase.zookeeper.property.clientPort:zookeeper.znode.parent</code>    * @throws IllegalStateException if there's already one slave since    * multi-slave isn't supported yet.    * @deprecated Use addPeer(String, ReplicationPeerConfig, Map) instead.    */
annotation|@
name|Deprecated
specifier|public
name|void
name|addPeer
parameter_list|(
name|String
name|id
parameter_list|,
name|String
name|clusterKey
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|this
operator|.
name|addPeer
argument_list|(
name|id
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|clusterKey
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Deprecated
specifier|public
name|void
name|addPeer
parameter_list|(
name|String
name|id
parameter_list|,
name|String
name|clusterKey
parameter_list|,
name|String
name|tableCFs
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|this
operator|.
name|replicationPeers
operator|.
name|addPeer
argument_list|(
name|id
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|clusterKey
argument_list|)
argument_list|,
name|tableCFs
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add a new remote slave cluster for replication.    * @param id a short name that identifies the cluster    * @param peerConfig configuration for the replication slave cluster    * @param tableCfs the table and column-family list which will be replicated for this peer.    * A map from tableName to column family names. An empty collection can be passed    * to indicate replicating all column families. Pass null for replicating all table and column    * families    */
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
block|{
name|this
operator|.
name|replicationPeers
operator|.
name|addPeer
argument_list|(
name|id
argument_list|,
name|peerConfig
argument_list|,
name|getTableCfsStr
argument_list|(
name|tableCfs
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
if|if
condition|(
name|tableCFsConfig
operator|==
literal|null
operator|||
name|tableCFsConfig
operator|.
name|trim
argument_list|()
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
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
comment|// TODO: This should be a PB object rather than a String to be parsed!! See HBASE-11393
comment|// parse out (table, cf-list) pairs from tableCFsConfig
comment|// format: "table1:cf1,cf2;table2:cfA,cfB"
name|String
index|[]
name|tables
init|=
name|tableCFsConfig
operator|.
name|split
argument_list|(
literal|";"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|tab
range|:
name|tables
control|)
block|{
comment|// 1 ignore empty table config
name|tab
operator|=
name|tab
operator|.
name|trim
argument_list|()
expr_stmt|;
if|if
condition|(
name|tab
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
continue|continue;
block|}
comment|// 2 split to "table" and "cf1,cf2"
comment|//   for each table: "table:cf1,cf2" or "table"
name|String
index|[]
name|pair
init|=
name|tab
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
name|String
name|tabName
init|=
name|pair
index|[
literal|0
index|]
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
name|pair
operator|.
name|length
operator|>
literal|2
operator|||
name|tabName
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"ignore invalid tableCFs setting: "
operator|+
name|tab
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// 3 parse "cf1,cf2" part to List<cf>
name|List
argument_list|<
name|String
argument_list|>
name|cfs
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|pair
operator|.
name|length
operator|==
literal|2
condition|)
block|{
name|String
index|[]
name|cfsList
init|=
name|pair
index|[
literal|1
index|]
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|cf
range|:
name|cfsList
control|)
block|{
name|String
name|cfName
init|=
name|cf
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
name|cfName
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|cfs
operator|==
literal|null
condition|)
block|{
name|cfs
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|cfs
operator|.
name|add
argument_list|(
name|cfName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// 4 put<table, List<cf>> to map
if|if
condition|(
name|tableCFsMap
operator|==
literal|null
condition|)
block|{
name|tableCFsMap
operator|=
operator|new
name|HashMap
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|tableCFsMap
operator|.
name|put
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tabName
argument_list|)
argument_list|,
name|cfs
argument_list|)
expr_stmt|;
block|}
return|return
name|tableCFsMap
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|static
name|String
name|getTableCfsStr
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
name|tableCfs
parameter_list|)
block|{
name|String
name|tableCfsStr
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|tableCfs
operator|!=
literal|null
condition|)
block|{
comment|// Format: table1:cf1,cf2;table2:cfA,cfB;table3
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
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
if|if
condition|(
name|builder
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|";"
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|StringUtils
operator|.
name|join
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
literal|","
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|tableCfsStr
operator|=
name|builder
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
return|return
name|tableCfsStr
return|;
block|}
comment|/**    * Removes a peer cluster and stops the replication to it.    * @param id a short name that identifies the cluster    */
specifier|public
name|void
name|removePeer
parameter_list|(
name|String
name|id
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|this
operator|.
name|replicationPeers
operator|.
name|removePeer
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
comment|/**    * Restart the replication stream to the specified peer.    * @param id a short name that identifies the cluster    */
specifier|public
name|void
name|enablePeer
parameter_list|(
name|String
name|id
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|this
operator|.
name|replicationPeers
operator|.
name|enablePeer
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
comment|/**    * Stop the replication stream to the specified peer.    * @param id a short name that identifies the cluster    */
specifier|public
name|void
name|disablePeer
parameter_list|(
name|String
name|id
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|this
operator|.
name|replicationPeers
operator|.
name|disablePeer
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the number of slave clusters the local cluster has.    * @return number of slave clusters    */
specifier|public
name|int
name|getPeersCount
parameter_list|()
block|{
return|return
name|this
operator|.
name|replicationPeers
operator|.
name|getAllPeerIds
argument_list|()
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**    * Map of this cluster's peers for display.    * @return A map of peer ids to peer cluster keys    * @deprecated use {@link #listPeerConfigs()}    */
annotation|@
name|Deprecated
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|listPeers
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|ReplicationPeerConfig
argument_list|>
name|peers
init|=
name|this
operator|.
name|listPeerConfigs
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ret
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|peers
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|ReplicationPeerConfig
argument_list|>
name|entry
range|:
name|peers
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|ret
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|ReplicationPeerConfig
argument_list|>
name|listPeerConfigs
parameter_list|()
block|{
return|return
name|this
operator|.
name|replicationPeers
operator|.
name|getAllPeerConfigs
argument_list|()
return|;
block|}
specifier|public
name|ReplicationPeerConfig
name|getPeerConfig
parameter_list|(
name|String
name|id
parameter_list|)
throws|throws
name|ReplicationException
block|{
return|return
name|this
operator|.
name|replicationPeers
operator|.
name|getReplicationPeerConfig
argument_list|(
name|id
argument_list|)
return|;
block|}
comment|/**    * Get the replicable table-cf config of the specified peer.    * @param id a short name that identifies the cluster    */
specifier|public
name|String
name|getPeerTableCFs
parameter_list|(
name|String
name|id
parameter_list|)
throws|throws
name|ReplicationException
block|{
return|return
name|this
operator|.
name|replicationPeers
operator|.
name|getPeerTableCFsConfig
argument_list|(
name|id
argument_list|)
return|;
block|}
comment|/**    * Set the replicable table-cf config of the specified peer    * @param id a short name that identifies the cluster    * @deprecated use {@link #setPeerTableCFs(String, Map)}    */
annotation|@
name|Deprecated
specifier|public
name|void
name|setPeerTableCFs
parameter_list|(
name|String
name|id
parameter_list|,
name|String
name|tableCFs
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|this
operator|.
name|replicationPeers
operator|.
name|setPeerTableCFsConfig
argument_list|(
name|id
argument_list|,
name|tableCFs
argument_list|)
expr_stmt|;
block|}
comment|/**    * Append the replicable table-cf config of the specified peer    * @param id a short that identifies the cluster    * @param tableCfs table-cfs config str    * @throws ReplicationException    */
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
block|{
name|appendPeerTableCFs
argument_list|(
name|id
argument_list|,
name|parseTableCFsFromConfig
argument_list|(
name|tableCfs
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Append the replicable table-cf config of the specified peer    * @param id a short that identifies the cluster    * @param tableCfs A map from tableName to column family names    * @throws ReplicationException    */
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
name|parseTableCFsFromConfig
argument_list|(
name|getPeerTableCFs
argument_list|(
name|id
argument_list|)
argument_list|)
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
argument_list|<
name|String
argument_list|>
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
name|setPeerTableCFs
argument_list|(
name|id
argument_list|,
name|preTableCfs
argument_list|)
expr_stmt|;
block|}
comment|/**    * Remove some table-cfs from table-cfs config of the specified peer    * @param id a short name that identifies the cluster    * @param tableCf table-cfs config str    * @throws ReplicationException    */
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
block|{
name|removePeerTableCFs
argument_list|(
name|id
argument_list|,
name|parseTableCFsFromConfig
argument_list|(
name|tableCf
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Remove some table-cfs from config of the specified peer    * @param id a short name that identifies the cluster    * @param tableCfs A map from tableName to column family names    * @throws ReplicationException    */
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
name|parseTableCFsFromConfig
argument_list|(
name|getPeerTableCFs
argument_list|(
name|id
argument_list|)
argument_list|)
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
name|removeCfs
operator|==
literal|null
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
name|removeCfs
operator|!=
literal|null
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
argument_list|<
name|String
argument_list|>
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
name|removeCfs
operator|!=
literal|null
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
name|removeCfs
operator|==
literal|null
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
name|setPeerTableCFs
argument_list|(
name|id
argument_list|,
name|preTableCfs
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the replicable table-cf config of the specified peer    * @param id a short name that identifies the cluster    * @param tableCfs the table and column-family list which will be replicated for this peer.    * A map from tableName to column family names. An empty collection can be passed    * to indicate replicating all column families. Pass null for replicating all table and column    * families    */
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
name|ReplicationException
block|{
name|this
operator|.
name|replicationPeers
operator|.
name|setPeerTableCFsConfig
argument_list|(
name|id
argument_list|,
name|getTableCfsStr
argument_list|(
name|tableCfs
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the state of the specified peer cluster    * @param id String format of the Short name that identifies the peer,    * an IllegalArgumentException is thrown if it doesn't exist    * @return true if replication is enabled to that peer, false if it isn't    */
specifier|public
name|boolean
name|getPeerState
parameter_list|(
name|String
name|id
parameter_list|)
throws|throws
name|ReplicationException
block|{
return|return
name|this
operator|.
name|replicationPeers
operator|.
name|getStatusOfPeerFromBackingStore
argument_list|(
name|id
argument_list|)
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
name|zkw
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|zkw
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
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
block|}
comment|/**    * Find all column families that are replicated from this cluster    * @return the full list of the replicated column families of this cluster as:    *        tableName, family name, replicationType    *    * Currently replicationType is Global. In the future, more replication    * types may be extended here. For example    *  1) the replication may only apply to selected peers instead of all peers    *  2) the replicationType may indicate the host Cluster servers as Slave    *     for the table:columnFam.    */
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
argument_list|<
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|Admin
name|admin
init|=
name|connection
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|HTableDescriptor
index|[]
name|tables
decl_stmt|;
try|try
block|{
name|tables
operator|=
name|admin
operator|.
name|listTables
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|admin
operator|!=
literal|null
condition|)
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|HTableDescriptor
name|table
range|:
name|tables
control|)
block|{
name|HColumnDescriptor
index|[]
name|columns
init|=
name|table
operator|.
name|getColumnFamilies
argument_list|()
decl_stmt|;
name|String
name|tableName
init|=
name|table
operator|.
name|getNameAsString
argument_list|()
decl_stmt|;
for|for
control|(
name|HColumnDescriptor
name|column
range|:
name|columns
control|)
block|{
if|if
condition|(
name|column
operator|.
name|getScope
argument_list|()
operator|!=
name|HConstants
operator|.
name|REPLICATION_SCOPE_LOCAL
condition|)
block|{
comment|// At this moment, the columfam is replicated to all peers
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
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|replicationEntry
operator|.
name|put
argument_list|(
name|TNAME
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|replicationEntry
operator|.
name|put
argument_list|(
name|CFNAME
argument_list|,
name|column
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
name|replicationEntry
operator|.
name|put
argument_list|(
name|REPLICATIONTYPE
argument_list|,
name|REPLICATIONGLOBAL
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
block|}
block|}
return|return
name|replicationColFams
return|;
block|}
block|}
end_class

end_unit

