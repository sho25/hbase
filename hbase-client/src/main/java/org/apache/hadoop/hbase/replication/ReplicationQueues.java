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
name|SortedMap
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
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_comment
comment|/**  * This provides an interface for maintaining a region server's replication queues. These queues  * keep track of the HLogs that still need to be replicated to remote clusters.  */
end_comment

begin_interface
specifier|public
interface|interface
name|ReplicationQueues
block|{
comment|/**    * Initialize the region server replication queue interface.    * @param serverName The server name of the region server that owns the replication queues this    *          interface manages.    */
specifier|public
name|void
name|init
parameter_list|(
name|String
name|serverName
parameter_list|)
function_decl|;
comment|/**    * Remove a replication queue.    * @param queueId a String that identifies the queue.    */
specifier|public
name|void
name|removeQueue
parameter_list|(
name|String
name|queueId
parameter_list|)
function_decl|;
comment|/**    * Add a new HLog file to the given queue. If the queue does not exist it is created.    * @param queueId a String that identifies the queue.    * @param filename name of the HLog    * @throws KeeperException    */
specifier|public
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
name|KeeperException
function_decl|;
comment|/**    * Remove an HLog file from the given queue.    * @param queueId a String that identifies the queue.    * @param filename name of the HLog    */
specifier|public
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
comment|/**    * Set the current position for a specific HLog in a given queue.    * @param queueId a String that identifies the queue    * @param filename name of the HLog    * @param position the current position in the file    */
specifier|public
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
comment|/**    * Get the current position for a specific HLog in a given queue.    * @param queueId a String that identifies the queue    * @param filename name of the HLog    * @return the current position in the file    */
specifier|public
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
name|KeeperException
function_decl|;
comment|/**    * Remove all replication queues for this region server.    */
specifier|public
name|void
name|removeAllQueues
parameter_list|()
function_decl|;
comment|/**    * Get a list of all HLogs in the given queue.    * @param queueId a String that identifies the queue    * @return a list of HLogs, null if this region server is dead and has no outstanding queues    */
specifier|public
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
comment|/**    * Get a list of all queues for this region server.    * @return a list of queueIds, null if this region server is dead and has no outstanding queues    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getAllQueues
parameter_list|()
function_decl|;
comment|/**    * Take ownership for the set of queues belonging to a dead region server.    * @param regionserver the id of the dead region server    * @return A SortedMap of the queues that have been claimed, including a SortedSet of HLogs in    *         each queue. Returns an empty map if no queues were failed-over.    */
specifier|public
name|SortedMap
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
name|claimQueues
parameter_list|(
name|String
name|regionserver
parameter_list|)
function_decl|;
comment|/**    * Get a list of all region servers that have outstanding replication queues. These servers could    * be alive, dead or from a previous run of the cluster.    * @return a list of server names    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getListOfReplicators
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

