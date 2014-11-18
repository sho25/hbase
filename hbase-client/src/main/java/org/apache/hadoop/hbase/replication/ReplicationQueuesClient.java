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

begin_comment
comment|/**  * This provides an interface for clients of replication to view replication queues. These queues  * keep track of the WALs that still need to be replicated to remote clusters.  */
end_comment

begin_interface
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
comment|/**    * Get a list of all region servers that have outstanding replication queues. These servers could    * be alive, dead or from a previous run of the cluster.    * @return a list of server names    */
name|List
argument_list|<
name|String
argument_list|>
name|getListOfReplicators
parameter_list|()
function_decl|;
comment|/**    * Get a list of all WALs in the given queue on the given region server.    * @param serverName the server name of the region server that owns the queue    * @param queueId a String that identifies the queue    * @return a list of WALs, null if this region server is dead and has no outstanding queues    */
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
function_decl|;
block|}
end_interface

end_unit

