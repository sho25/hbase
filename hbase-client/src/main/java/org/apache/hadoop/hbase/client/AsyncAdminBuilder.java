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
import|import static
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
name|ConnectionUtils
operator|.
name|retries2Attempts
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
comment|/**  * For creating {@link AsyncAdmin}. The implementation should have default configurations set before  * returning the builder to user. So users are free to only set the configs they care about to  * create a new AsyncAdmin instance.  * @since 2.0.0  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|AsyncAdminBuilder
block|{
comment|/**    * Set timeout for a whole admin operation. Operation timeout and max attempt times(or max retry    * times) are both limitations for retrying, we will stop retrying when we reach any of the    * limitations.    * @return this for invocation chaining    */
name|AsyncAdminBuilder
name|setOperationTimeout
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
function_decl|;
comment|/**    * Set timeout for each rpc request.    * @return this for invocation chaining    */
name|AsyncAdminBuilder
name|setRpcTimeout
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
function_decl|;
comment|/**    * Set the base pause time for retrying. We use an exponential policy to generate sleep time when    * retrying.    * @return this for invocation chaining    * @see #setRetryPauseForCQTBE(long, TimeUnit)    */
name|AsyncAdminBuilder
name|setRetryPause
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
function_decl|;
comment|/**    * Set the base pause time for retrying when we hit {@code CallQueueTooBigException}. We use an    * exponential policy to generate sleep time when retrying.    *<p/>    * This value should be greater than the normal pause value which could be set with the above    * {@link #setRetryPause(long, TimeUnit)} method, as usually {@code CallQueueTooBigException}    * means the server is overloaded. We just use the normal pause value for    * {@code CallQueueTooBigException} if here you specify a smaller value.    * @see #setRetryPause(long, TimeUnit)    */
name|AsyncAdminBuilder
name|setRetryPauseForCQTBE
parameter_list|(
name|long
name|pause
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
function_decl|;
comment|/**    * Set the max retry times for an admin operation. Usually it is the max attempt times minus 1.    * Operation timeout and max attempt times(or max retry times) are both limitations for retrying,    * we will stop retrying when we reach any of the limitations.    * @return this for invocation chaining    */
specifier|default
name|AsyncAdminBuilder
name|setMaxRetries
parameter_list|(
name|int
name|maxRetries
parameter_list|)
block|{
return|return
name|setMaxAttempts
argument_list|(
name|retries2Attempts
argument_list|(
name|maxRetries
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Set the max attempt times for an admin operation. Usually it is the max retry times plus 1.    * Operation timeout and max attempt times(or max retry times) are both limitations for retrying,    * we will stop retrying when we reach any of the limitations.    * @return this for invocation chaining    */
name|AsyncAdminBuilder
name|setMaxAttempts
parameter_list|(
name|int
name|maxAttempts
parameter_list|)
function_decl|;
comment|/**    * Set the number of retries that are allowed before we start to log.    * @return this for invocation chaining    */
name|AsyncAdminBuilder
name|setStartLogErrorsCnt
parameter_list|(
name|int
name|startLogErrorsCnt
parameter_list|)
function_decl|;
comment|/**    * Create a {@link AsyncAdmin} instance.    * @return a {@link AsyncAdmin} instance    */
name|AsyncAdmin
name|build
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

