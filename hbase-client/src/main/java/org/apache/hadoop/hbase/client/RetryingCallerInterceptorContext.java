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
comment|/**  * The context object used in the {@link RpcRetryingCaller} to enable  * {@link RetryingCallerInterceptor} to intercept calls.  * {@link RetryingCallerInterceptorContext} is the piece of information unique  * to a retrying call that transfers information from the call into the  * {@link RetryingCallerInterceptor} so that {@link RetryingCallerInterceptor}  * can take appropriate action according to the specific logic  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|abstract
class|class
name|RetryingCallerInterceptorContext
block|{
specifier|protected
name|RetryingCallerInterceptorContext
parameter_list|()
block|{   }
comment|/**    * This function clears the internal state of the context object.    */
specifier|public
specifier|abstract
name|void
name|clear
parameter_list|()
function_decl|;
comment|/**    * This prepares the context object by populating it with information specific    * to the implementation of the {@link RetryingCallerInterceptor} along with    * which this will be used.    *     * @param callable    *          : The {@link RetryingCallable} that contains the information about    *          the call that is being made.    * @return A new {@link RetryingCallerInterceptorContext} object that can be    *         used for use in the current retrying call    */
specifier|public
specifier|abstract
name|RetryingCallerInterceptorContext
name|prepare
parameter_list|(
name|RetryingCallable
argument_list|<
name|?
argument_list|>
name|callable
parameter_list|)
function_decl|;
comment|/**    * Telescopic extension that takes which of the many retries we are currently    * in.    *     * @param callable    *          : The {@link RetryingCallable} that contains the information about    *          the call that is being made.    * @param tries    *          : The retry number that we are currently in.    * @return A new context object that can be used for use in the current    *         retrying call    */
specifier|public
specifier|abstract
name|RetryingCallerInterceptorContext
name|prepare
parameter_list|(
name|RetryingCallable
argument_list|<
name|?
argument_list|>
name|callable
parameter_list|,
name|int
name|tries
parameter_list|)
function_decl|;
block|}
end_class

end_unit

