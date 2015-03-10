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
name|List
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
name|TableDescriptors
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
name|wal
operator|.
name|WAL
operator|.
name|Entry
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
name|MetricsSource
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
name|util
operator|.
name|concurrent
operator|.
name|Service
import|;
end_import

begin_comment
comment|/**  * ReplicationEndpoint is a plugin which implements replication  * to other HBase clusters, or other systems. ReplicationEndpoint implementation  * can be specified at the peer creation time by specifying it  * in the {@link ReplicationPeerConfig}. A ReplicationEndpoint is run in a thread  * in each region server in the same process.  *<p>  * ReplicationEndpoint is closely tied to ReplicationSource in a producer-consumer  * relation. ReplicationSource is an HBase-private class which tails the logs and manages  * the queue of logs plus management and persistence of all the state for replication.  * ReplicationEndpoint on the other hand is responsible for doing the actual shipping  * and persisting of the WAL entries in the other cluster.  */
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
name|ReplicationEndpoint
extends|extends
name|Service
block|{
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|REPLICATION
argument_list|)
class|class
name|Context
block|{
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|final
name|TableDescriptors
name|tableDescriptors
decl_stmt|;
specifier|private
specifier|final
name|ReplicationPeerConfig
name|peerConfig
decl_stmt|;
specifier|private
specifier|final
name|ReplicationPeer
name|replicationPeer
decl_stmt|;
specifier|private
specifier|final
name|String
name|peerId
decl_stmt|;
specifier|private
specifier|final
name|UUID
name|clusterId
decl_stmt|;
specifier|private
specifier|final
name|MetricsSource
name|metrics
decl_stmt|;
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|Context
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|ReplicationPeerConfig
name|peerConfig
parameter_list|,
specifier|final
name|String
name|peerId
parameter_list|,
specifier|final
name|UUID
name|clusterId
parameter_list|,
specifier|final
name|ReplicationPeer
name|replicationPeer
parameter_list|,
specifier|final
name|MetricsSource
name|metrics
parameter_list|,
specifier|final
name|TableDescriptors
name|tableDescriptors
parameter_list|)
block|{
name|this
operator|.
name|peerConfig
operator|=
name|peerConfig
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|clusterId
operator|=
name|clusterId
expr_stmt|;
name|this
operator|.
name|peerId
operator|=
name|peerId
expr_stmt|;
name|this
operator|.
name|replicationPeer
operator|=
name|replicationPeer
expr_stmt|;
name|this
operator|.
name|metrics
operator|=
name|metrics
expr_stmt|;
name|this
operator|.
name|tableDescriptors
operator|=
name|tableDescriptors
expr_stmt|;
block|}
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
specifier|public
name|FileSystem
name|getFilesystem
parameter_list|()
block|{
return|return
name|fs
return|;
block|}
specifier|public
name|UUID
name|getClusterId
parameter_list|()
block|{
return|return
name|clusterId
return|;
block|}
specifier|public
name|String
name|getPeerId
parameter_list|()
block|{
return|return
name|peerId
return|;
block|}
specifier|public
name|ReplicationPeerConfig
name|getPeerConfig
parameter_list|()
block|{
return|return
name|peerConfig
return|;
block|}
specifier|public
name|ReplicationPeer
name|getReplicationPeer
parameter_list|()
block|{
return|return
name|replicationPeer
return|;
block|}
specifier|public
name|MetricsSource
name|getMetrics
parameter_list|()
block|{
return|return
name|metrics
return|;
block|}
specifier|public
name|TableDescriptors
name|getTableDescriptors
parameter_list|()
block|{
return|return
name|tableDescriptors
return|;
block|}
block|}
comment|/**    * Initialize the replication endpoint with the given context.    * @param context replication context    * @throws IOException    */
name|void
name|init
parameter_list|(
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/** Whether or not, the replication endpoint can replicate to it's source cluster with the same    * UUID */
name|boolean
name|canReplicateToSameCluster
parameter_list|()
function_decl|;
comment|/**    * Returns a UUID of the provided peer id. Every HBase cluster instance has a persisted    * associated UUID. If the replication is not performed to an actual HBase cluster (but    * some other system), the UUID returned has to uniquely identify the connected target system.    * @return a UUID or null if the peer cluster does not exist or is not connected.    */
name|UUID
name|getPeerUUID
parameter_list|()
function_decl|;
comment|/**    * Returns a WALEntryFilter to use for filtering out WALEntries from the log. Replication    * infrastructure will call this filter before sending the edits to shipEdits().    * @return a {@link WALEntryFilter} or null.    */
name|WALEntryFilter
name|getWALEntryfilter
parameter_list|()
function_decl|;
comment|/**    * A context for {@link ReplicationEndpoint#replicate(ReplicateContext)} method.    */
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|REPLICATION
argument_list|)
class|class
name|ReplicateContext
block|{
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
decl_stmt|;
name|int
name|size
decl_stmt|;
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|ReplicateContext
parameter_list|()
block|{     }
specifier|public
name|ReplicateContext
name|setEntries
parameter_list|(
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
parameter_list|)
block|{
name|this
operator|.
name|entries
operator|=
name|entries
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ReplicateContext
name|setSize
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|List
argument_list|<
name|Entry
argument_list|>
name|getEntries
parameter_list|()
block|{
return|return
name|entries
return|;
block|}
specifier|public
name|int
name|getSize
parameter_list|()
block|{
return|return
name|size
return|;
block|}
block|}
comment|/**    * Replicate the given set of entries (in the context) to the other cluster.    * Can block until all the given entries are replicated. Upon this method is returned,    * all entries that were passed in the context are assumed to be persisted in the    * target cluster.    * @param replicateContext a context where WAL entries and other    * parameters can be obtained.    */
name|boolean
name|replicate
parameter_list|(
name|ReplicateContext
name|replicateContext
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

