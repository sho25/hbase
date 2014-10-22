begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|StringUtils
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Service
import|;
end_import

begin_comment
comment|/**  * Used for server-side protobuf RPC service invocations.  This handler allows  * invocation exceptions to easily be passed through to the RPC server from coprocessor  * {@link Service} implementations.  *  *<p>  * When implementing {@link Service} defined methods, coprocessor endpoints can use the following  * pattern to pass exceptions back to the RPC client:  *<code>  * public void myMethod(RpcController controller, MyRequest request, RpcCallback<MyResponse> done) {  *   MyResponse response = null;  *   try {  *     // do processing  *     response = MyResponse.getDefaultInstance();  // or use a new builder to populate the response  *   } catch (IOException ioe) {  *     // pass exception back up  *     ResponseConverter.setControllerException(controller, ioe);  *   }  *   done.run(response);  * }  *</code>  *</p>  */
end_comment

begin_class
specifier|public
class|class
name|ServerRpcController
implements|implements
name|RpcController
block|{
comment|/**    * The exception thrown within    * {@link Service#callMethod(Descriptors.MethodDescriptor, RpcController, Message, RpcCallback)},    * if any.    */
comment|// TODO: it would be good widen this to just Throwable, but IOException is what we allow now
specifier|private
name|IOException
name|serviceException
decl_stmt|;
specifier|private
name|String
name|errorMessage
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|serviceException
operator|=
literal|null
expr_stmt|;
name|errorMessage
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|failed
parameter_list|()
block|{
return|return
operator|(
name|failedOnException
argument_list|()
operator|||
name|errorMessage
operator|!=
literal|null
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|errorText
parameter_list|()
block|{
return|return
name|errorMessage
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|startCancel
parameter_list|()
block|{
comment|// not implemented
block|}
annotation|@
name|Override
specifier|public
name|void
name|setFailed
parameter_list|(
name|String
name|message
parameter_list|)
block|{
name|errorMessage
operator|=
name|message
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isCanceled
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|notifyOnCancel
parameter_list|(
name|RpcCallback
argument_list|<
name|Object
argument_list|>
name|objectRpcCallback
parameter_list|)
block|{
comment|// not implemented
block|}
comment|/**    * Sets an exception to be communicated back to the {@link Service} client.    * @param ioe the exception encountered during execution of the service method    */
specifier|public
name|void
name|setFailedOn
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|serviceException
operator|=
name|ioe
expr_stmt|;
name|setFailed
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|ioe
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns any exception thrown during service method invocation, or {@code null} if no exception    * was thrown.  This can be used by clients to receive exceptions generated by RPC calls, even    * when {@link RpcCallback}s are used and no {@link com.google.protobuf.ServiceException} is    * declared.    */
specifier|public
name|IOException
name|getFailedOn
parameter_list|()
block|{
return|return
name|serviceException
return|;
block|}
comment|/**    * Returns whether or not a server exception was generated in the prior RPC invocation.    */
specifier|public
name|boolean
name|failedOnException
parameter_list|()
block|{
return|return
name|serviceException
operator|!=
literal|null
return|;
block|}
comment|/**    * Throws an IOException back out if one is currently stored.    */
specifier|public
name|void
name|checkFailed
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|failedOnException
argument_list|()
condition|)
block|{
throw|throw
name|getFailedOn
argument_list|()
throw|;
block|}
block|}
block|}
end_class

end_unit

