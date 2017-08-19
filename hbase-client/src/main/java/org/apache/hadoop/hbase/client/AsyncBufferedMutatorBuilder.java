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
comment|/**  * For creating {@link AsyncBufferedMutator}.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|AsyncBufferedMutatorBuilder
block|{
comment|/**    * Set timeout for the background flush operation.    */
name|AsyncBufferedMutatorBuilder
name|setOperationTimeout
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
function_decl|;
comment|/**    * Set timeout for each rpc request when doing background flush.    */
name|AsyncBufferedMutatorBuilder
name|setRpcTimeout
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
function_decl|;
comment|/**    * Set the base pause time for retrying. We use an exponential policy to generate sleep time when    * retrying.    */
name|AsyncBufferedMutatorBuilder
name|setRetryPause
parameter_list|(
name|long
name|pause
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
function_decl|;
comment|/**    * Set the max retry times for an operation. Usually it is the max attempt times minus 1.    *<p>    * Operation timeout and max attempt times(or max retry times) are both limitations for retrying,    * we will stop retrying when we reach any of the limitations.    * @see #setMaxAttempts(int)    * @see #setOperationTimeout(long, TimeUnit)    */
specifier|default
name|AsyncBufferedMutatorBuilder
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
comment|/**    * Set the max attempt times for an operation. Usually it is the max retry times plus 1. Operation    * timeout and max attempt times(or max retry times) are both limitations for retrying, we will    * stop retrying when we reach any of the limitations.    * @see #setMaxRetries(int)    * @see #setOperationTimeout(long, TimeUnit)    */
name|AsyncBufferedMutatorBuilder
name|setMaxAttempts
parameter_list|(
name|int
name|maxAttempts
parameter_list|)
function_decl|;
comment|/**    * Set the number of retries that are allowed before we start to log.    */
name|AsyncBufferedMutatorBuilder
name|setStartLogErrorsCnt
parameter_list|(
name|int
name|startLogErrorsCnt
parameter_list|)
function_decl|;
comment|/**    * Override the write buffer size specified by the provided {@link AsyncConnection}'s    * {@link org.apache.hadoop.conf.Configuration} instance, via the configuration key    * {@code hbase.client.write.buffer}.    */
name|AsyncBufferedMutatorBuilder
name|setWriteBufferSize
parameter_list|(
name|long
name|writeBufferSize
parameter_list|)
function_decl|;
comment|/**    * Create the {@link AsyncBufferedMutator} instance.    */
name|AsyncBufferedMutator
name|build
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

