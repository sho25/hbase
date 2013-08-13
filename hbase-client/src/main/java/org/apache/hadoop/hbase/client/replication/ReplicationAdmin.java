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
name|client
operator|.
name|HConnection
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
name|HConnectionManager
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
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
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
name|HColumnDescriptor
import|;
end_import

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
name|Map
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
name|ArrayList
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
name|lang
operator|.
name|Integer
import|;
end_import

begin_comment
comment|/**  *<p>  * This class provides the administrative interface to HBase cluster  * replication. In order to use it, the cluster and the client using  * ReplicationAdmin must be configured with<code>hbase.replication</code>  * set to true.  *</p>  *<p>  * Adding a new peer results in creating new outbound connections from every  * region server to a subset of region servers on the slave cluster. Each  * new stream of replication will start replicating from the beginning of the  * current HLog, meaning that edits from that past will be replicated.  *</p>  *<p>  * Removing a peer is a destructive and irreversible operation that stops  * all the replication streams for the given cluster and deletes the metadata  * used to keep track of the replication state.  *</p>  *<p>  * To see which commands are available in the shell, type  *<code>replication</code>.  *</p>  */
end_comment

begin_class
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
name|HConnection
name|connection
decl_stmt|;
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
comment|/**    * Constructor that creates a connection to the local ZooKeeper ensemble.    * @param conf Configuration to use    * @throws IOException if the connection to ZK cannot be made    * @throws RuntimeException if replication isn't enabled.    */
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
literal|false
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
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|ZooKeeperWatcher
name|zkw
init|=
name|createZooKeeperWatcher
argument_list|()
decl_stmt|;
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
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable setup the ZooKeeper connection"
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
return|return
operator|new
name|ZooKeeperWatcher
argument_list|(
name|connection
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|"Replication Admin"
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
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
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
comment|/**    * Add a new peer cluster to replicate to.    * @param id a short that identifies the cluster    * @param clusterKey the concatenation of the slave cluster's    *<code>hbase.zookeeper.quorum:hbase.zookeeper.property.clientPort:zookeeper.znode.parent</code>    * @throws IllegalStateException if there's already one slave since    * multi-slave isn't supported yet.    */
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
name|IOException
block|{
name|this
operator|.
name|replicationPeers
operator|.
name|addPeer
argument_list|(
name|id
argument_list|,
name|clusterKey
argument_list|)
expr_stmt|;
block|}
comment|/**    * Removes a peer cluster and stops the replication to it.    * @param id a short that identifies the cluster    */
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
name|replicationPeers
operator|.
name|removePeer
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
comment|/**    * Restart the replication stream to the specified peer.    * @param id a short that identifies the cluster    */
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
name|replicationPeers
operator|.
name|enablePeer
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
comment|/**    * Stop the replication stream to the specified peer.    * @param id a short that identifies the cluster    */
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
comment|/**    * Map of this cluster's peers for display.    * @return A map of peer ids to peer cluster keys    */
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
return|return
name|this
operator|.
name|replicationPeers
operator|.
name|getAllPeerClusterKeys
argument_list|()
return|;
block|}
comment|/**    * Get the state of the specified peer cluster    * @param id String format of the Short that identifies the peer, an IllegalArgumentException    *           is thrown if it doesn't exist    * @return true if replication is enabled to that peer, false if it isn't    */
specifier|public
name|boolean
name|getPeerState
parameter_list|(
name|String
name|id
parameter_list|)
throws|throws
name|IOException
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
comment|/**    * Find all column families that are replicated from this cluster    * @return the full list of the replicated column families of this cluster as:    *        tableName, family name, replicationType    *    * Currently replicationType is Global. In the future, more replication    * types may be extended here. For example    *  1) the replication may only apply to selected peers instead of all peers    *  2) the replicationType may indicate the host Cluster servers as Slave    *     for the table:columnFam.             */
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
name|HTableDescriptor
index|[]
name|tables
init|=
name|this
operator|.
name|connection
operator|.
name|listTables
argument_list|()
decl_stmt|;
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

