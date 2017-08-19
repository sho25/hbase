begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|CellScanner
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|BlockingService
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|MethodDescriptor
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
name|shaded
operator|.
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|RPCProtos
operator|.
name|RequestHeader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|htrace
operator|.
name|TraceInfo
import|;
end_import

begin_comment
comment|/**  * Interface of all necessary to carry out a RPC method invocation on the server.  */
end_comment

begin_interface
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
interface|interface
name|RpcCall
extends|extends
name|RpcCallContext
block|{
comment|/**    * @return The service of this call.    */
name|BlockingService
name|getService
parameter_list|()
function_decl|;
comment|/**    * @return The service method.    */
name|MethodDescriptor
name|getMethod
parameter_list|()
function_decl|;
comment|/**    * @return The call parameter message.    */
name|Message
name|getParam
parameter_list|()
function_decl|;
comment|/**    * @return The CellScanner that can carry input and result payload.    */
name|CellScanner
name|getCellScanner
parameter_list|()
function_decl|;
comment|/**    * @return The timestamp when the call is constructed.    */
name|long
name|getReceiveTime
parameter_list|()
function_decl|;
comment|/**    * @return The time when the call starts to be executed.    */
name|long
name|getStartTime
parameter_list|()
function_decl|;
comment|/**    * Set the time when the call starts to be executed.    */
name|void
name|setStartTime
parameter_list|(
name|long
name|startTime
parameter_list|)
function_decl|;
comment|/**    * @return The timeout of this call.    */
name|int
name|getTimeout
parameter_list|()
function_decl|;
comment|/**    * @return The Priority of this call.    */
name|int
name|getPriority
parameter_list|()
function_decl|;
comment|/**    * Return the deadline of this call. If we can not complete this call in time,    * we can throw a TimeoutIOException and RPCServer will drop it.    * @return The system timestamp of deadline.    */
name|long
name|getDeadline
parameter_list|()
function_decl|;
comment|/**    * Used to calculate the request call queue size.    * If the total request call size exceeds a limit, the call will be rejected.    * @return The raw size of this call.    */
name|long
name|getSize
parameter_list|()
function_decl|;
comment|/**    * @return The request header of this call.    */
name|RequestHeader
name|getHeader
parameter_list|()
function_decl|;
comment|/**    * @return Port of remote address in this call    */
name|int
name|getRemotePort
parameter_list|()
function_decl|;
comment|/**    * Set the response resulting from this RPC call.    * @param param The result message as response.    * @param cells The CellScanner that possibly carries the payload.    * @param errorThrowable The error Throwable resulting from the call.    * @param error Extra error message.    */
name|void
name|setResponse
parameter_list|(
name|Message
name|param
parameter_list|,
name|CellScanner
name|cells
parameter_list|,
name|Throwable
name|errorThrowable
parameter_list|,
name|String
name|error
parameter_list|)
function_decl|;
comment|/**    * Send the response of this RPC call.    * Implementation provides the underlying facility (connection, etc) to send.    * @throws IOException    */
name|void
name|sendResponseIfReady
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Do the necessary cleanup after the call if needed.    */
name|void
name|cleanup
parameter_list|()
function_decl|;
comment|/**    * @return A short string format of this call without possibly lengthy params    */
name|String
name|toShortString
parameter_list|()
function_decl|;
comment|/**    * @return TraceInfo attached to this call.    */
name|TraceInfo
name|getTraceInfo
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

