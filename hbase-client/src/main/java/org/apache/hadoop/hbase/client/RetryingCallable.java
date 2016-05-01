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
comment|/**  * A Callable&lt;T&gt; that will be retried.  If {@link #call(int)} invocation throws exceptions,  * we will call {@link #throwable(Throwable, boolean)} with whatever the exception was.  * @param<T> result class from executing<tt>this</tt>  */
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
extends|extends
name|RetryingCallableBase
block|{
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
block|}
end_interface

end_unit

