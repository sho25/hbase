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
name|RpcCallback
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
name|RpcController
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
name|CellScannable
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
name|HConstants
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Optionally carries Cells across the proxy/service interface down into ipc. On its way out it  * optionally carries a set of result Cell data. We stick the Cells here when we want to avoid  * having to protobuf them (for performance reasons). This class is used ferrying data across the  * proxy/protobuf service chasm. Also does call timeout. Used by client and server ipc'ing.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|HBaseRpcController
extends|extends
name|RpcController
extends|,
name|CellScannable
block|{
specifier|static
specifier|final
name|int
name|PRIORITY_UNSET
init|=
operator|-
literal|1
decl_stmt|;
comment|/**    * Only used to send cells to rpc server, the returned cells should be set by    * {@link #setDone(CellScanner)}.    */
name|void
name|setCellScanner
parameter_list|(
name|CellScanner
name|cellScanner
parameter_list|)
function_decl|;
comment|/**    * @param priority Priority for this request; should fall roughly in the range    *          {@link HConstants#NORMAL_QOS} to {@link HConstants#HIGH_QOS}    */
name|void
name|setPriority
parameter_list|(
name|int
name|priority
parameter_list|)
function_decl|;
comment|/**    * @param tn Set priority based off the table we are going against.    */
name|void
name|setPriority
parameter_list|(
specifier|final
name|TableName
name|tn
parameter_list|)
function_decl|;
comment|/**    * @return The priority of this request    */
name|int
name|getPriority
parameter_list|()
function_decl|;
name|int
name|getCallTimeout
parameter_list|()
function_decl|;
name|void
name|setCallTimeout
parameter_list|(
name|int
name|callTimeout
parameter_list|)
function_decl|;
name|boolean
name|hasCallTimeout
parameter_list|()
function_decl|;
comment|/**    * Set failed with an exception to pass on. For use in async rpc clients    * @param e exception to set with    */
name|void
name|setFailed
parameter_list|(
name|IOException
name|e
parameter_list|)
function_decl|;
comment|/**    * Return the failed exception, null if not failed.    */
name|IOException
name|getFailed
parameter_list|()
function_decl|;
comment|/**    *<b>IMPORTANT:</b> always call this method if the call finished without any exception to tell    * the {@code HBaseRpcController} that we are done.    */
name|void
name|setDone
parameter_list|(
name|CellScanner
name|cellScanner
parameter_list|)
function_decl|;
comment|/**    * A little different from the basic RpcController:    *<ol>    *<li>You can register multiple callbacks to an {@code HBaseRpcController}.</li>    *<li>The callback will not be called if the rpc call is finished without any cancellation.</li>    *<li>You can call me at client side also.</li>    *</ol>    */
annotation|@
name|Override
name|void
name|notifyOnCancel
parameter_list|(
name|RpcCallback
argument_list|<
name|Object
argument_list|>
name|callback
parameter_list|)
function_decl|;
interface|interface
name|CancellationCallback
block|{
name|void
name|run
parameter_list|(
name|boolean
name|cancelled
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    * If not cancelled, add the callback to cancellation callback list. And then execute the action    * with the cancellation state as a parameter. The implementation should guarantee that the    * cancellation state does not change during this call.    */
name|void
name|notifyOnCancel
parameter_list|(
name|RpcCallback
argument_list|<
name|Object
argument_list|>
name|callback
parameter_list|,
name|CancellationCallback
name|action
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

