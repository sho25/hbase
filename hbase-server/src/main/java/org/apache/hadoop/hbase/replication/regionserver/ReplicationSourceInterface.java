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
operator|.
name|regionserver
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
name|Stoppable
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
name|replication
operator|.
name|ReplicationEndpoint
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
name|ReplicationQueues
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
name|Pair
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

begin_comment
comment|/**  * Interface that defines a replication source  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|ReplicationSourceInterface
block|{
comment|/**    * Initializer for the source    * @param conf the configuration to use    * @param fs the file system to use    * @param manager the manager to use    * @param replicationQueues    * @param replicationPeers    * @param stopper the stopper object for this region server    * @param peerClusterZnode    * @param clusterId    * @throws IOException    */
specifier|public
name|void
name|init
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
name|ReplicationSourceManager
name|manager
parameter_list|,
specifier|final
name|ReplicationQueues
name|replicationQueues
parameter_list|,
specifier|final
name|ReplicationPeers
name|replicationPeers
parameter_list|,
specifier|final
name|Stoppable
name|stopper
parameter_list|,
specifier|final
name|String
name|peerClusterZnode
parameter_list|,
specifier|final
name|UUID
name|clusterId
parameter_list|,
name|ReplicationEndpoint
name|replicationEndpoint
parameter_list|,
specifier|final
name|MetricsSource
name|metrics
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Add a log to the list of logs to replicate    * @param log path to the log to replicate    */
name|void
name|enqueueLog
parameter_list|(
name|Path
name|log
parameter_list|)
function_decl|;
comment|/**    * Add hfile names to the queue to be replicated.    * @param tableName Name of the table these files belongs to    * @param family Name of the family these files belong to    * @param pairs list of pairs of { HFile location in staging dir, HFile path in region dir which    *          will be added in the queue for replication}    * @throws ReplicationException If failed to add hfile references    */
name|void
name|addHFileRefs
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|List
argument_list|<
name|Pair
argument_list|<
name|Path
argument_list|,
name|Path
argument_list|>
argument_list|>
name|pairs
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Start the replication    */
name|void
name|startup
parameter_list|()
function_decl|;
comment|/**    * End the replication    * @param reason why it's terminating    */
name|void
name|terminate
parameter_list|(
name|String
name|reason
parameter_list|)
function_decl|;
comment|/**    * End the replication    * @param reason why it's terminating    * @param cause the error that's causing it    */
name|void
name|terminate
parameter_list|(
name|String
name|reason
parameter_list|,
name|Exception
name|cause
parameter_list|)
function_decl|;
comment|/**    * Get the current log that's replicated    * @return the current log    */
name|Path
name|getCurrentPath
parameter_list|()
function_decl|;
comment|/**    * Get the id that the source is replicating to    *    * @return peer cluster id    */
name|String
name|getPeerClusterZnode
parameter_list|()
function_decl|;
comment|/**    * Get the id that the source is replicating to.    *    * @return peer id    */
name|String
name|getPeerId
parameter_list|()
function_decl|;
comment|/**    * Get a string representation of the current statistics    * for this source    * @return printable stats    */
name|String
name|getStats
parameter_list|()
function_decl|;
comment|/**    * @return peer enabled or not    */
name|boolean
name|isPeerEnabled
parameter_list|()
function_decl|;
comment|/**    * @return active or not    */
name|boolean
name|isSourceActive
parameter_list|()
function_decl|;
comment|/**    * @return metrics of this replication source    */
name|MetricsSource
name|getSourceMetrics
parameter_list|()
function_decl|;
comment|/**    * @return the replication endpoint used by this replication source    */
name|ReplicationEndpoint
name|getReplicationEndpoint
parameter_list|()
function_decl|;
comment|/**    * @return the replication source manager    */
name|ReplicationSourceManager
name|getSourceManager
parameter_list|()
function_decl|;
comment|/**    * Try to throttle when the peer config with a bandwidth    * @param batchSize entries size will be pushed    * @throws InterruptedException    */
name|void
name|tryThrottle
parameter_list|(
name|int
name|batchSize
parameter_list|)
throws|throws
name|InterruptedException
function_decl|;
comment|/**    * Call this after the shipper thread ship some entries to peer cluster.    * @param entries pushed    * @param batchSize entries size pushed    */
name|void
name|postShipEdits
parameter_list|(
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
parameter_list|,
name|int
name|batchSize
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

