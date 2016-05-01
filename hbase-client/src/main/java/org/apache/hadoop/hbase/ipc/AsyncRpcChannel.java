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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|concurrent
operator|.
name|EventExecutor
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Promise
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
name|client
operator|.
name|MetricsConnection
import|;
end_import

begin_comment
comment|/**  * Interface for Async Rpc Channels  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|AsyncRpcChannel
block|{
comment|/**    * Calls method on channel    * @param method to call    * @param controller to run call with    * @param request to send    * @param responsePrototype to construct response with    */
name|Promise
argument_list|<
name|Message
argument_list|>
name|callMethod
parameter_list|(
specifier|final
name|Descriptors
operator|.
name|MethodDescriptor
name|method
parameter_list|,
specifier|final
name|PayloadCarryingRpcController
name|controller
parameter_list|,
specifier|final
name|Message
name|request
parameter_list|,
specifier|final
name|Message
name|responsePrototype
parameter_list|,
name|MetricsConnection
operator|.
name|CallStats
name|callStats
parameter_list|)
function_decl|;
comment|/**    * Get the EventLoop on which this channel operated    * @return EventLoop    */
name|EventExecutor
name|getEventExecutor
parameter_list|()
function_decl|;
comment|/**    * Close connection    * @param cause of closure.    */
name|void
name|close
parameter_list|(
name|Throwable
name|cause
parameter_list|)
function_decl|;
comment|/**    * Check if the connection is alive    *    * @return true if alive    */
name|boolean
name|isAlive
parameter_list|()
function_decl|;
comment|/**    * Get the address on which this channel operates    * @return InetSocketAddress    */
name|InetSocketAddress
name|getAddress
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

