begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Callable
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * A Callable<T> that will be retried.  If {@link #call(int)} invocation throws exceptions,  * we will call {@link #throwable(Throwable, boolean)} with whatever the exception was.  * @param<T>  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|RetryingCallable
parameter_list|<
name|T
parameter_list|>
block|{
comment|/**    * Prepare by setting up any connections to servers, etc., ahead of {@link #call(int)} invocation.    * @param reload Set this to true if need to requery locations    * @throws IOException e    */
name|void
name|prepare
parameter_list|(
specifier|final
name|boolean
name|reload
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called when {@link #call(int)} throws an exception and we are going to retry; take action to    * make it so we succeed on next call (clear caches, do relookup of locations, etc.).    * @param t    * @param retrying True if we are in retrying mode (we are not in retrying mode when max    * retries == 1; we ARE in retrying mode if retries> 1 even when we are the last attempt)    */
name|void
name|throwable
parameter_list|(
specifier|final
name|Throwable
name|t
parameter_list|,
name|boolean
name|retrying
parameter_list|)
function_decl|;
comment|/**    * Computes a result, or throws an exception if unable to do so.    *    * @param callTimeout - the time available for this call. 0 for infinite.    * @return computed result    * @throws Exception if unable to compute a result    */
name|T
name|call
parameter_list|(
name|int
name|callTimeout
parameter_list|)
throws|throws
name|Exception
function_decl|;
comment|/**    * @return Some details from the implementation that we would like to add to a terminating    * exception; i.e. a fatal exception is being thrown ending retries and we might like to add    * more implementation-specific detail on to the exception being thrown.    */
name|String
name|getExceptionMessageAdditionalDetail
parameter_list|()
function_decl|;
comment|/**    * @param pause    * @param tries    * @return Suggestion on how much to sleep between retries    */
name|long
name|sleep
parameter_list|(
specifier|final
name|long
name|pause
parameter_list|,
specifier|final
name|int
name|tries
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

