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
name|Set
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
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_comment
comment|/**  * This provides an interface for clients of replication to view replication queues. These queues  * keep track of the sources(WALs/HFile references) that still need to be replicated to remote  * clusters.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|ReplicationQueuesClient
block|{
comment|/**    * Initialize the replication queue client interface.    */
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Get a list of all region servers that have outstanding replication queues. These servers could    * be alive, dead or from a previous run of the cluster.    * @return a list of server names    * @throws KeeperException zookeeper exception    */
name|List
argument_list|<
name|String
argument_list|>
name|getListOfReplicators
parameter_list|()
throws|throws
name|KeeperException
function_decl|;
comment|/**    * Get a list of all WALs in the given queue on the given region server.    * @param serverName the server name of the region server that owns the queue    * @param queueId a String that identifies the queue    * @return a list of WALs, null if this region server is dead and has no outstanding queues    * @throws KeeperException zookeeper exception    */
name|List
argument_list|<
name|String
argument_list|>
name|getLogsInQueue
parameter_list|(
name|String
name|serverName
parameter_list|,
name|String
name|queueId
parameter_list|)
throws|throws
name|KeeperException
function_decl|;
comment|/**    * Get a list of all queues for the specified region server.    * @param serverName the server name of the region server that owns the set of queues    * @return a list of queueIds, null if this region server is not a replicator.    */
name|List
argument_list|<
name|String
argument_list|>
name|getAllQueues
parameter_list|(
name|String
name|serverName
parameter_list|)
throws|throws
name|KeeperException
function_decl|;
comment|/**    * Load all wals in all replication queues from ZK. This method guarantees to return a    * snapshot which contains all WALs in the zookeeper at the start of this call even there    * is concurrent queue failover. However, some newly created WALs during the call may    * not be included.    */
name|Set
argument_list|<
name|String
argument_list|>
name|getAllWALs
parameter_list|()
throws|throws
name|KeeperException
function_decl|;
comment|/**    * Get the change version number of replication hfile references node. This can be used as    * optimistic locking to get a consistent snapshot of the replication queues of hfile references.    * @return change version number of hfile references node    */
name|int
name|getHFileRefsNodeChangeVersion
parameter_list|()
throws|throws
name|KeeperException
function_decl|;
comment|/**    * Get list of all peers from hfile reference queue.    * @return a list of peer ids    * @throws KeeperException zookeeper exception    */
name|List
argument_list|<
name|String
argument_list|>
name|getAllPeersFromHFileRefsQueue
parameter_list|()
throws|throws
name|KeeperException
function_decl|;
comment|/**    * Get a list of all hfile references in the given peer.    * @param peerId a String that identifies the peer    * @return a list of hfile references, null if not found any    * @throws KeeperException zookeeper exception    */
name|List
argument_list|<
name|String
argument_list|>
name|getReplicableHFiles
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|KeeperException
function_decl|;
block|}
end_interface

end_unit

