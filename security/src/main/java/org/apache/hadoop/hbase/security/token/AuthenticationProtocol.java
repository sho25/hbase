begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|security
operator|.
name|token
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
name|ipc
operator|.
name|CoprocessorProtocol
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
name|security
operator|.
name|token
operator|.
name|Token
import|;
end_import

begin_comment
comment|/**  * Defines a custom RPC protocol for obtaining authentication tokens  */
end_comment

begin_interface
specifier|public
interface|interface
name|AuthenticationProtocol
extends|extends
name|CoprocessorProtocol
block|{
comment|/**    * Obtains a token capable of authenticating as the current user for future    * connections.    * @return an authentication token for the current user    * @throws IOException If obtaining a token is denied or encounters an error    */
specifier|public
name|Token
argument_list|<
name|AuthenticationTokenIdentifier
argument_list|>
name|getAuthenticationToken
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Returns the currently authenticated username.    */
specifier|public
name|String
name|whoami
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

