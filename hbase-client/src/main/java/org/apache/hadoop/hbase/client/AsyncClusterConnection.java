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
name|util
operator|.
name|concurrent
operator|.
name|CompletableFuture
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
name|ServerName
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
name|RpcClient
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|FlushRegionResponse
import|;
end_import

begin_comment
comment|/**  * The asynchronous connection for internal usage.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|AsyncClusterConnection
extends|extends
name|AsyncConnection
block|{
comment|/**    * Get the admin service for the given region server.    */
name|AsyncRegionServerAdmin
name|getRegionServerAdmin
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
function_decl|;
comment|/**    * Get the nonce generator for this connection.    */
name|NonceGenerator
name|getNonceGenerator
parameter_list|()
function_decl|;
comment|/**    * Get the rpc client we used to communicate with other servers.    */
name|RpcClient
name|getRpcClient
parameter_list|()
function_decl|;
comment|/**    * Flush a region and get the response.    */
name|CompletableFuture
argument_list|<
name|FlushRegionResponse
argument_list|>
name|flush
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|,
name|boolean
name|writeFlushWALMarker
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

