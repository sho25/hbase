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
name|SortedSet
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
name|util
operator|.
name|Pair
import|;
end_import

begin_comment
comment|/**  * This provides an interface for maintaining a region server's replication queues. These queues  * keep track of the WALs and HFile references (if hbase.replication.bulkload.enabled is enabled)  * that still need to be replicated to remote clusters.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|ReplicationQueues
block|{
comment|/**    * Initialize the region server replication queue interface.    * @param serverName The server name of the region server that owns the replication queues this    *          interface manages.    */
name|void
name|init
parameter_list|(
name|String
name|serverName
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Remove a replication queue.    * @param queueId a String that identifies the queue.    */
name|void
name|removeQueue
parameter_list|(
name|String
name|queueId
parameter_list|)
function_decl|;
comment|/**    * Add a new WAL file to the given queue. If the queue does not exist it is created.    * @param queueId a String that identifies the queue.    * @param filename name of the WAL    */
name|void
name|addLog
parameter_list|(
name|String
name|queueId
parameter_list|,
name|String
name|filename
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Remove an WAL file from the given queue.    * @param queueId a String that identifies the queue.    * @param filename name of the WAL    */
name|void
name|removeLog
parameter_list|(
name|String
name|queueId
parameter_list|,
name|String
name|filename
parameter_list|)
function_decl|;
comment|/**    * Set the current position for a specific WAL in a given queue.    * @param queueId a String that identifies the queue    * @param filename name of the WAL    * @param position the current position in the file    */
name|void
name|setLogPosition
parameter_list|(
name|String
name|queueId
parameter_list|,
name|String
name|filename
parameter_list|,
name|long
name|position
parameter_list|)
function_decl|;
comment|/**    * Get the current position for a specific WAL in a given queue.    * @param queueId a String that identifies the queue    * @param filename name of the WAL    * @return the current position in the file    */
name|long
name|getLogPosition
parameter_list|(
name|String
name|queueId
parameter_list|,
name|String
name|filename
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Remove all replication queues for this region server.    */
name|void
name|removeAllQueues
parameter_list|()
function_decl|;
comment|/**    * Get a list of all WALs in the given queue.    * @param queueId a String that identifies the queue    * @return a list of WALs, null if no such queue exists for this server    */
name|List
argument_list|<
name|String
argument_list|>
name|getLogsInQueue
parameter_list|(
name|String
name|queueId
parameter_list|)
function_decl|;
comment|/**    * Get a list of all queues for this region server.    * @return a list of queueIds, an empty list if this region server is dead and has no outstanding queues    */
name|List
argument_list|<
name|String
argument_list|>
name|getAllQueues
parameter_list|()
function_decl|;
comment|/**    * Get queueIds from a dead region server, whose queues has not been claimed by other region    * servers.    * @return empty if the queue exists but no children, null if the queue does not exist.   */
name|List
argument_list|<
name|String
argument_list|>
name|getUnClaimedQueueIds
parameter_list|(
name|String
name|regionserver
parameter_list|)
function_decl|;
comment|/**    * Take ownership for the queue identified by queueId and belongs to a dead region server.    * @param regionserver the id of the dead region server    * @param queueId the id of the queue    * @return the new PeerId and A SortedSet of WALs in its queue, and null if no unclaimed queue.    */
name|Pair
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
name|claimQueue
parameter_list|(
name|String
name|regionserver
parameter_list|,
name|String
name|queueId
parameter_list|)
function_decl|;
comment|/**    * Remove the znode of region server if the queue is empty.    * @param regionserver    */
name|void
name|removeReplicatorIfQueueIsEmpty
parameter_list|(
name|String
name|regionserver
parameter_list|)
function_decl|;
comment|/**    * Get a list of all region servers that have outstanding replication queues. These servers could    * be alive, dead or from a previous run of the cluster.    * @return a list of server names    */
name|List
argument_list|<
name|String
argument_list|>
name|getListOfReplicators
parameter_list|()
function_decl|;
comment|/**    * Checks if the provided znode is the same as this region server's    * @param regionserver the id of the region server    * @return if this is this rs's znode    */
name|boolean
name|isThisOurRegionServer
parameter_list|(
name|String
name|regionserver
parameter_list|)
function_decl|;
comment|/**    * Add a peer to hfile reference queue if peer does not exist.    * @param peerId peer cluster id to be added    * @throws ReplicationException if fails to add a peer id to hfile reference queue    */
name|void
name|addPeerToHFileRefs
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Remove a peer from hfile reference queue.    * @param peerId peer cluster id to be removed    */
name|void
name|removePeerFromHFileRefs
parameter_list|(
name|String
name|peerId
parameter_list|)
function_decl|;
comment|/**    * Add new hfile references to the queue.    * @param peerId peer cluster id to which the hfiles need to be replicated    * @param files list of hfile references to be added    * @throws ReplicationException if fails to add a hfile reference    */
name|void
name|addHFileRefs
parameter_list|(
name|String
name|peerId
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|files
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Remove hfile references from the queue.    * @param peerId peer cluster id from which this hfile references needs to be removed    * @param files list of hfile references to be removed    */
name|void
name|removeHFileRefs
parameter_list|(
name|String
name|peerId
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|files
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

