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
name|channel
operator|.
name|EventLoop
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
name|DefaultPromise
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|EnvironmentEdgeManager
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
name|ExceptionUtil
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
name|ipc
operator|.
name|RemoteException
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

begin_comment
comment|/**  * Represents an Async Hbase call and its response.  *  * Responses are passed on to its given doneHandler and failures to the rpcController  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|AsyncCall
extends|extends
name|DefaultPromise
argument_list|<
name|Message
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|AsyncCall
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|int
name|id
decl_stmt|;
specifier|final
name|Descriptors
operator|.
name|MethodDescriptor
name|method
decl_stmt|;
specifier|final
name|Message
name|param
decl_stmt|;
specifier|final
name|PayloadCarryingRpcController
name|controller
decl_stmt|;
specifier|final
name|Message
name|responseDefaultType
decl_stmt|;
specifier|final
name|long
name|startTime
decl_stmt|;
specifier|final
name|long
name|rpcTimeout
decl_stmt|;
comment|/**    * Constructor    *    * @param eventLoop           for call    * @param connectId           connection id    * @param md                  the method descriptor    * @param param               parameters to send to Server    * @param controller          controller for response    * @param responseDefaultType the default response type    */
specifier|public
name|AsyncCall
parameter_list|(
name|EventLoop
name|eventLoop
parameter_list|,
name|int
name|connectId
parameter_list|,
name|Descriptors
operator|.
name|MethodDescriptor
name|md
parameter_list|,
name|Message
name|param
parameter_list|,
name|PayloadCarryingRpcController
name|controller
parameter_list|,
name|Message
name|responseDefaultType
parameter_list|)
block|{
name|super
argument_list|(
name|eventLoop
argument_list|)
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|connectId
expr_stmt|;
name|this
operator|.
name|method
operator|=
name|md
expr_stmt|;
name|this
operator|.
name|param
operator|=
name|param
expr_stmt|;
name|this
operator|.
name|controller
operator|=
name|controller
expr_stmt|;
name|this
operator|.
name|responseDefaultType
operator|=
name|responseDefaultType
expr_stmt|;
name|this
operator|.
name|startTime
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
expr_stmt|;
name|this
operator|.
name|rpcTimeout
operator|=
name|controller
operator|.
name|hasCallTimeout
argument_list|()
condition|?
name|controller
operator|.
name|getCallTimeout
argument_list|()
else|:
literal|0
expr_stmt|;
block|}
comment|/**    * Get the start time    *    * @return start time for the call    */
specifier|public
name|long
name|getStartTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|startTime
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"callId: "
operator|+
name|this
operator|.
name|id
operator|+
literal|" methodName: "
operator|+
name|this
operator|.
name|method
operator|.
name|getName
argument_list|()
operator|+
literal|" param {"
operator|+
operator|(
name|this
operator|.
name|param
operator|!=
literal|null
condition|?
name|ProtobufUtil
operator|.
name|getShortTextFormat
argument_list|(
name|this
operator|.
name|param
argument_list|)
else|:
literal|""
operator|)
operator|+
literal|"}"
return|;
block|}
comment|/**    * Set success with a cellBlockScanner    *    * @param value            to set    * @param cellBlockScanner to set    */
specifier|public
name|void
name|setSuccess
parameter_list|(
name|Message
name|value
parameter_list|,
name|CellScanner
name|cellBlockScanner
parameter_list|)
block|{
if|if
condition|(
name|cellBlockScanner
operator|!=
literal|null
condition|)
block|{
name|controller
operator|.
name|setCellScanner
argument_list|(
name|cellBlockScanner
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|long
name|callTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|startTime
decl_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"Call: "
operator|+
name|method
operator|.
name|getName
argument_list|()
operator|+
literal|", callTime: "
operator|+
name|callTime
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|setSuccess
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set failed    *    * @param exception to set    */
specifier|public
name|void
name|setFailed
parameter_list|(
name|IOException
name|exception
parameter_list|)
block|{
if|if
condition|(
name|ExceptionUtil
operator|.
name|isInterrupt
argument_list|(
name|exception
argument_list|)
condition|)
block|{
name|exception
operator|=
name|ExceptionUtil
operator|.
name|asInterrupt
argument_list|(
name|exception
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|exception
operator|instanceof
name|RemoteException
condition|)
block|{
name|exception
operator|=
operator|(
operator|(
name|RemoteException
operator|)
name|exception
operator|)
operator|.
name|unwrapRemoteException
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|setFailure
argument_list|(
name|exception
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the rpc timeout    *    * @return current timeout for this call    */
specifier|public
name|long
name|getRpcTimeout
parameter_list|()
block|{
return|return
name|rpcTimeout
return|;
block|}
block|}
end_class

end_unit

