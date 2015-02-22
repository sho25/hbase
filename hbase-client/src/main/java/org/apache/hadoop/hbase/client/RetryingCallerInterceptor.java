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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * This class is designed to fit into the RetryingCaller class which forms the  * central piece of intelligence for the client side retries for most calls.  *   * One can extend this class and intercept the RetryingCaller and add additional  * logic into the execution of a simple HTable operations like get, delete etc.  *   * Concrete implementations of this calls are supposed to the thread safe. The  * object is used across threads to identify the fast failing threads.  *   * For a concrete use case see {@link PreemptiveFastFailInterceptor}  *   * Example use case :   * try {  *   interceptor.intercept  *   doAction()  * } catch (Exception e) {  *   interceptor.handleFailure  * } finally {  *   interceptor.updateFaulireInfo  * }  *   * The {@link RetryingCallerInterceptor} also acts as a factory  * for getting a new {@link RetryingCallerInterceptorContext}.  *   */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|abstract
class|class
name|RetryingCallerInterceptor
block|{
specifier|protected
name|RetryingCallerInterceptor
parameter_list|()
block|{
comment|// Empty constructor protected for NoOpRetryableCallerInterceptor
block|}
comment|/**    * This returns the context object for the current call.    *     * @return context : the context that needs to be used during this call.    */
specifier|public
specifier|abstract
name|RetryingCallerInterceptorContext
name|createEmptyContext
parameter_list|()
function_decl|;
comment|/**    * Call this function in case we caught a failure during retries.    *     * @param context    *          : The context object that we obtained previously.    * @param t    *          : The exception that we caught in this particular try    * @throws IOException    */
specifier|public
specifier|abstract
name|void
name|handleFailure
parameter_list|(
name|RetryingCallerInterceptorContext
name|context
parameter_list|,
name|Throwable
name|t
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Call this function alongside the actual call done on the callable.    *     * @param abstractRetryingCallerInterceptorContext    * @throws PreemptiveFastFailException    */
specifier|public
specifier|abstract
name|void
name|intercept
parameter_list|(
name|RetryingCallerInterceptorContext
name|abstractRetryingCallerInterceptorContext
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Call this function to update at the end of the retry. This is not necessary    * to happen.    *     * @param context    */
specifier|public
specifier|abstract
name|void
name|updateFailureInfo
parameter_list|(
name|RetryingCallerInterceptorContext
name|context
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
specifier|abstract
name|String
name|toString
parameter_list|()
function_decl|;
block|}
end_class

end_unit

