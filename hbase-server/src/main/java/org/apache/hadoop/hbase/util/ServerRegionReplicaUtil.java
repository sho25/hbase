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
name|util
package|;
end_package

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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|client
operator|.
name|RegionInfo
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
name|RegionReplicaUtil
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
name|io
operator|.
name|HFileLink
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
name|io
operator|.
name|Reference
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
name|regionserver
operator|.
name|HRegion
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
name|regionserver
operator|.
name|StoreFileInfo
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
name|regionserver
operator|.
name|RegionReplicaReplicationEndpoint
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
name|ZKConfig
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Similar to {@link RegionReplicaUtil} but for the server side  */
end_comment

begin_class
specifier|public
class|class
name|ServerRegionReplicaUtil
extends|extends
name|RegionReplicaUtil
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ServerRegionReplicaUtil
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Whether asynchronous WAL replication to the secondary region replicas is enabled or not.    * If this is enabled, a replication peer named "region_replica_replication" will be created    * which will tail the logs and replicate the mutatations to region replicas for tables that    * have region replication&gt; 1. If this is enabled once, disabling this replication also    * requires disabling the replication peer using shell or {@link Admin} java class.    * Replication to secondary region replicas works over standard inter-cluster replication.·    */
specifier|public
specifier|static
specifier|final
name|String
name|REGION_REPLICA_REPLICATION_CONF_KEY
init|=
literal|"hbase.region.replica.replication.enabled"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|boolean
name|DEFAULT_REGION_REPLICA_REPLICATION
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|REGION_REPLICA_REPLICATION_PEER
init|=
literal|"region_replica_replication"
decl_stmt|;
comment|/**    * Enables or disables refreshing store files of secondary region replicas when the memory is    * above the global memstore lower limit. Refreshing the store files means that we will do a file    * list of the primary regions store files, and pick up new files. Also depending on the store    * files, we can drop some memstore contents which will free up memory.    */
specifier|public
specifier|static
specifier|final
name|String
name|REGION_REPLICA_STORE_FILE_REFRESH
init|=
literal|"hbase.region.replica.storefile.refresh"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|boolean
name|DEFAULT_REGION_REPLICA_STORE_FILE_REFRESH
init|=
literal|true
decl_stmt|;
comment|/**    * The multiplier to use when we want to refresh a secondary region instead of flushing a primary    * region. Default value assumes that for doing the file refresh, the biggest secondary should be    * 4 times bigger than the biggest primary.    */
specifier|public
specifier|static
specifier|final
name|String
name|REGION_REPLICA_STORE_FILE_REFRESH_MEMSTORE_MULTIPLIER
init|=
literal|"hbase.region.replica.storefile.refresh.memstore.multiplier"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|double
name|DEFAULT_REGION_REPLICA_STORE_FILE_REFRESH_MEMSTORE_MULTIPLIER
init|=
literal|4
decl_stmt|;
comment|/**    * Returns the regionInfo object to use for interacting with the file system.    * @return An RegionInfo object to interact with the filesystem    */
specifier|public
specifier|static
name|RegionInfo
name|getRegionInfoForFs
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|)
block|{
if|if
condition|(
name|regionInfo
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|RegionReplicaUtil
operator|.
name|getRegionInfoForDefaultReplica
argument_list|(
name|regionInfo
argument_list|)
return|;
block|}
comment|/**    * Returns whether this region replica can accept writes.    * @param region the HRegion object    * @return whether the replica is read only    */
specifier|public
specifier|static
name|boolean
name|isReadOnly
parameter_list|(
name|HRegion
name|region
parameter_list|)
block|{
return|return
name|region
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|isReadOnly
argument_list|()
operator|||
operator|!
name|isDefaultReplica
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Returns whether to replay the recovered edits to flush the results.    * Currently secondary region replicas do not replay the edits, since it would    * cause flushes which might affect the primary region. Primary regions even opened    * in read only mode should replay the edits.    * @param region the HRegion object    * @return whether recovered edits should be replayed.    */
specifier|public
specifier|static
name|boolean
name|shouldReplayRecoveredEdits
parameter_list|(
name|HRegion
name|region
parameter_list|)
block|{
return|return
name|isDefaultReplica
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Returns a StoreFileInfo from the given FileStatus. Secondary replicas refer to the    * files of the primary region, so an HFileLink is used to construct the StoreFileInfo. This    * way ensures that the secondary will be able to continue reading the store files even if    * they are moved to archive after compaction    * @throws IOException    */
specifier|public
specifier|static
name|StoreFileInfo
name|getStoreFileInfo
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|RegionInfo
name|regionInfo
parameter_list|,
name|RegionInfo
name|regionInfoForFs
parameter_list|,
name|String
name|familyName
parameter_list|,
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
comment|// if this is a primary region, just return the StoreFileInfo constructed from path
if|if
condition|(
name|RegionInfo
operator|.
name|COMPARATOR
operator|.
name|compare
argument_list|(
name|regionInfo
argument_list|,
name|regionInfoForFs
argument_list|)
operator|==
literal|0
condition|)
block|{
return|return
operator|new
name|StoreFileInfo
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|path
argument_list|)
return|;
block|}
comment|// else create a store file link. The link file does not exists on filesystem though.
name|HFileLink
name|link
init|=
name|HFileLink
operator|.
name|build
argument_list|(
name|conf
argument_list|,
name|regionInfoForFs
operator|.
name|getTable
argument_list|()
argument_list|,
name|regionInfoForFs
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|familyName
argument_list|,
name|path
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|StoreFileInfo
operator|.
name|isReference
argument_list|(
name|path
argument_list|)
condition|)
block|{
name|Reference
name|reference
init|=
name|Reference
operator|.
name|read
argument_list|(
name|fs
argument_list|,
name|path
argument_list|)
decl_stmt|;
return|return
operator|new
name|StoreFileInfo
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|link
operator|.
name|getFileStatus
argument_list|(
name|fs
argument_list|)
argument_list|,
name|reference
argument_list|)
return|;
block|}
return|return
operator|new
name|StoreFileInfo
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|link
operator|.
name|getFileStatus
argument_list|(
name|fs
argument_list|)
argument_list|,
name|link
argument_list|)
return|;
block|}
comment|/**    * Create replication peer for replicating to region replicas if needed.    * @param conf configuration to use    * @throws IOException    */
specifier|public
specifier|static
name|void
name|setupRegionReplicaReplication
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
name|isRegionReplicaReplicationEnabled
argument_list|(
name|conf
argument_list|)
condition|)
block|{
return|return;
block|}
name|Admin
name|admin
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|ReplicationPeerConfig
name|peerConfig
init|=
literal|null
decl_stmt|;
try|try
block|{
name|peerConfig
operator|=
name|admin
operator|.
name|getReplicationPeerConfig
argument_list|(
name|REGION_REPLICA_REPLICATION_PEER
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ReplicationPeerNotFoundException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Region replica replication peer id="
operator|+
name|REGION_REPLICA_REPLICATION_PEER
operator|+
literal|" not exist"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|peerConfig
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Region replica replication peer id="
operator|+
name|REGION_REPLICA_REPLICATION_PEER
operator|+
literal|" not exist. Creating..."
argument_list|)
expr_stmt|;
name|peerConfig
operator|=
operator|new
name|ReplicationPeerConfig
argument_list|()
expr_stmt|;
name|peerConfig
operator|.
name|setClusterKey
argument_list|(
name|ZKConfig
operator|.
name|getZooKeeperClusterKey
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|peerConfig
operator|.
name|setReplicationEndpointImpl
argument_list|(
name|RegionReplicaReplicationEndpoint
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|addReplicationPeer
argument_list|(
name|REGION_REPLICA_REPLICATION_PEER
argument_list|,
name|peerConfig
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|boolean
name|isRegionReplicaReplicationEnabled
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
name|REGION_REPLICA_REPLICATION_CONF_KEY
argument_list|,
name|DEFAULT_REGION_REPLICA_REPLICATION
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isRegionReplicaWaitForPrimaryFlushEnabled
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
name|REGION_REPLICA_WAIT_FOR_PRIMARY_FLUSH_CONF_KEY
argument_list|,
name|DEFAULT_REGION_REPLICA_WAIT_FOR_PRIMARY_FLUSH
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isRegionReplicaStoreFileRefreshEnabled
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
name|REGION_REPLICA_STORE_FILE_REFRESH
argument_list|,
name|DEFAULT_REGION_REPLICA_STORE_FILE_REFRESH
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|double
name|getRegionReplicaStoreFileRefreshMultiplier
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getDouble
argument_list|(
name|REGION_REPLICA_STORE_FILE_REFRESH_MEMSTORE_MULTIPLIER
argument_list|,
name|DEFAULT_REGION_REPLICA_STORE_FILE_REFRESH_MEMSTORE_MULTIPLIER
argument_list|)
return|;
block|}
comment|/**    * Return the peer id used for replicating to secondary region replicas    */
specifier|public
specifier|static
name|String
name|getReplicationPeerId
parameter_list|()
block|{
return|return
name|REGION_REPLICA_REPLICATION_PEER
return|;
block|}
block|}
end_class

end_unit

