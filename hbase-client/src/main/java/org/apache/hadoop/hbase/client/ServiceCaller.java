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
name|client
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

begin_comment
comment|/**  * Delegate to a protobuf rpc call.  *<p>  * Usually, it is just a simple lambda expression, like:  *  *<pre>  *<code>  * (stub, controller, rpcCallback) -> {  *   XXXRequest request = ...; // prepare the request  *   stub.xxx(controller, request, rpcCallback);  * }  *</code>  *</pre>  *  * And if already have the {@code request}, the lambda expression will be:  *  *<pre>  *<code>  * (stub, controller, rpcCallback) -> stub.xxx(controller, request, rpcCallback)  *</code>  *</pre>  *  * @param<S> the type of the protobuf Service you want to call.  * @param<R> the type of the return value.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|FunctionalInterface
specifier|public
interface|interface
name|ServiceCaller
parameter_list|<
name|S
parameter_list|,
name|R
parameter_list|>
block|{
comment|/**    * Represent the actual protobuf rpc call.    * @param stub the asynchronous stub    * @param controller the rpc controller, has already been prepared for you    * @param rpcCallback the rpc callback, has already been prepared for you    */
name|void
name|call
parameter_list|(
name|S
name|stub
parameter_list|,
name|RpcController
name|controller
parameter_list|,
name|RpcCallback
argument_list|<
name|R
argument_list|>
name|rpcCallback
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

