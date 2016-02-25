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
name|ipc
package|;
end_package

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
name|HBaseInterfaceAudience
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
name|net
operator|.
name|InetSocketAddress
import|;
end_import

begin_comment
comment|/**  * An interface for RPC request scheduling algorithm.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
block|{
name|HBaseInterfaceAudience
operator|.
name|COPROC
block|,
name|HBaseInterfaceAudience
operator|.
name|PHOENIX
block|}
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|abstract
class|class
name|RpcScheduler
block|{
comment|/** Exposes runtime information of a {@code RpcServer} that a {@code RpcScheduler} may need. */
specifier|static
specifier|abstract
class|class
name|Context
block|{
specifier|public
specifier|abstract
name|InetSocketAddress
name|getListenerAddress
parameter_list|()
function_decl|;
block|}
comment|/**    * Does some quick initialization. Heavy tasks (e.g. starting threads) should be    * done in {@link #start()}. This method is called before {@code start}.    *    * @param context provides methods to retrieve runtime information from    */
specifier|public
specifier|abstract
name|void
name|init
parameter_list|(
name|Context
name|context
parameter_list|)
function_decl|;
comment|/**    * Prepares for request serving. An implementation may start some handler threads here.    */
specifier|public
specifier|abstract
name|void
name|start
parameter_list|()
function_decl|;
comment|/** Stops serving new requests. */
specifier|public
specifier|abstract
name|void
name|stop
parameter_list|()
function_decl|;
comment|/**    * Dispatches an RPC request asynchronously. An implementation is free to choose to process the    * request immediately or delay it for later processing.    *    * @param task the request to be dispatched    */
specifier|public
specifier|abstract
name|boolean
name|dispatch
parameter_list|(
name|CallRunner
name|task
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
function_decl|;
comment|/** Retrieves length of the general queue for metrics. */
specifier|public
specifier|abstract
name|int
name|getGeneralQueueLength
parameter_list|()
function_decl|;
comment|/** Retrieves length of the priority queue for metrics. */
specifier|public
specifier|abstract
name|int
name|getPriorityQueueLength
parameter_list|()
function_decl|;
comment|/** Retrieves length of the replication queue for metrics. */
specifier|public
specifier|abstract
name|int
name|getReplicationQueueLength
parameter_list|()
function_decl|;
comment|/** Retrieves the number of active handler. */
specifier|public
specifier|abstract
name|int
name|getActiveRpcHandlerCount
parameter_list|()
function_decl|;
comment|/**    * If CoDel-based RPC executors are used, retrieves the number of Calls that were dropped    * from general queue because RPC executor is under high load; returns 0 otherwise.    */
specifier|public
specifier|abstract
name|long
name|getNumGeneralCallsDropped
parameter_list|()
function_decl|;
comment|/**    * If CoDel-based RPC executors are used, retrieves the number of Calls that were    * picked from the tail of the queue (indicating adaptive LIFO mode, when    * in the period of overloade we serve last requests first); returns 0 otherwise.    */
specifier|public
specifier|abstract
name|long
name|getNumLifoModeSwitches
parameter_list|()
function_decl|;
block|}
end_class

end_unit

