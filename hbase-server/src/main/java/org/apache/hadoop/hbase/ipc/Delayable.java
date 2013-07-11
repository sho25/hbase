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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * A call whose response can be delayed by the server.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|Delayable
block|{
comment|/**    * Signal that the call response should be delayed, thus freeing the RPC    * server to handle different requests.    *    * @param delayReturnValue Controls whether the return value of the call    * should be set when ending the delay or right away.  There are cases when    * the return value can be set right away, even if the call is delayed.    */
name|void
name|startDelay
parameter_list|(
name|boolean
name|delayReturnValue
parameter_list|)
function_decl|;
comment|/**    * @return is the call delayed?    */
name|boolean
name|isDelayed
parameter_list|()
function_decl|;
comment|/**    * @return is the return value delayed?    */
name|boolean
name|isReturnValueDelayed
parameter_list|()
function_decl|;
comment|/**    * Signal that the  RPC server is now allowed to send the response.    * @param result The value to return to the caller.  If the corresponding    * delay response specified that the return value should    * not be delayed, this parameter must be null.    * @throws IOException    */
name|void
name|endDelay
parameter_list|(
name|Object
name|result
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Signal the end of a delayed RPC, without specifying the return value.  Use    * this only if the return value was not delayed    * @throws IOException    */
name|void
name|endDelay
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * End the call, throwing and exception to the caller.  This works regardless    * of the return value being delayed.    * @param t Object to throw to the client.    * @throws IOException    */
name|void
name|endDelayThrowing
parameter_list|(
name|Throwable
name|t
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

