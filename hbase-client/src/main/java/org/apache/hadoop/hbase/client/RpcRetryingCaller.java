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

begin_comment
comment|/**  *  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|RpcRetryingCaller
parameter_list|<
name|T
parameter_list|>
block|{
name|void
name|cancel
parameter_list|()
function_decl|;
comment|/**    * Retries if invocation fails.    * @param callTimeout Timeout for this call    * @param callable The {@link RetryingCallable} to run.    * @return an object of type T    * @throws IOException if a remote or network exception occurs    * @throws RuntimeException other unspecified error    */
name|T
name|callWithRetries
parameter_list|(
name|RetryingCallable
argument_list|<
name|T
argument_list|>
name|callable
parameter_list|,
name|int
name|callTimeout
parameter_list|)
throws|throws
name|IOException
throws|,
name|RuntimeException
function_decl|;
comment|/**    * Call the server once only.    * {@link RetryingCallable} has a strange shape so we can do retries.  Use this invocation if you    * want to do a single call only (A call to {@link RetryingCallable#call(int)} will not likely    * succeed).    * @return an object of type T    * @throws IOException if a remote or network exception occurs    * @throws RuntimeException other unspecified error    */
name|T
name|callWithoutRetries
parameter_list|(
name|RetryingCallable
argument_list|<
name|T
argument_list|>
name|callable
parameter_list|,
name|int
name|callTimeout
parameter_list|)
throws|throws
name|IOException
throws|,
name|RuntimeException
function_decl|;
block|}
end_interface

end_unit

