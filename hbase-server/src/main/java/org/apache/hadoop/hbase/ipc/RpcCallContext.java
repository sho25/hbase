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
name|ipc
package|;
end_package

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetAddress
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|VersionInfo
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
name|security
operator|.
name|User
import|;
end_import

begin_interface
specifier|public
interface|interface
name|RpcCallContext
extends|extends
name|Delayable
block|{
comment|/**    * Check if the caller who made this IPC call has disconnected.    * If called from outside the context of IPC, this does nothing.    * @return&lt; 0 if the caller is still connected. The time in ms    *  since the disconnection otherwise    */
name|long
name|disconnectSince
parameter_list|()
function_decl|;
comment|/**    * If the client connected and specified a codec to use, then we will use this codec making    * cellblocks to return.  If the client did not specify a codec, we assume it does not support    * cellblocks and will return all content protobuf'd (though it makes our serving slower).    * We need to ask this question per call because a server could be hosting both clients that    * support cellblocks while fielding requests from clients that do not.    * @return True if the client supports cellblocks, else return all content in pb    */
name|boolean
name|isClientCellBlockSupport
parameter_list|()
function_decl|;
comment|/**    * Returns the user credentials associated with the current RPC request or    *<code>null</code> if no credentials were provided.    * @return A User    */
name|User
name|getRequestUser
parameter_list|()
function_decl|;
comment|/**    * @return Current request's user name or null if none ongoing.    */
name|String
name|getRequestUserName
parameter_list|()
function_decl|;
comment|/**    * @return Address of remote client if a request is ongoing, else null    */
name|InetAddress
name|getRemoteAddress
parameter_list|()
function_decl|;
comment|/**    * @return the client version info, or null if the information is not present    */
name|VersionInfo
name|getClientVersionInfo
parameter_list|()
function_decl|;
comment|/**    * Sets a callback which has to be executed at the end of this RPC call. Such a callback is an    * optional one for any Rpc call.    *    * @param callback    */
name|void
name|setCallBack
parameter_list|(
name|RpcCallback
name|callback
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

