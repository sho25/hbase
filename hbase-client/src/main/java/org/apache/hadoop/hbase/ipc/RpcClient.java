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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|BlockingRpcChannel
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
name|security
operator|.
name|User
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
import|;
end_import

begin_comment
comment|/**  * Interface for RpcClient implementations so ConnectionManager can handle it.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|RpcClient
extends|extends
name|Closeable
block|{
specifier|public
specifier|final
specifier|static
name|String
name|FAILED_SERVER_EXPIRY_KEY
init|=
literal|"hbase.ipc.client.failed.servers.expiry"
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|int
name|FAILED_SERVER_EXPIRY_DEFAULT
init|=
literal|2000
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|String
name|IDLE_TIME
init|=
literal|"hbase.ipc.client.connection.minIdleTimeBeforeClose"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|IPC_CLIENT_FALLBACK_TO_SIMPLE_AUTH_ALLOWED_KEY
init|=
literal|"hbase.ipc.client.fallback-to-simple-auth-allowed"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|IPC_CLIENT_FALLBACK_TO_SIMPLE_AUTH_ALLOWED_DEFAULT
init|=
literal|false
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SPECIFIC_WRITE_THREAD
init|=
literal|"hbase.ipc.client.specificThreadForWriting"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_CODEC_CLASS
init|=
literal|"hbase.client.default.rpc.codec"
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|String
name|SOCKET_TIMEOUT_CONNECT
init|=
literal|"hbase.ipc.client.socket.timeout.connect"
decl_stmt|;
comment|/**    * How long we wait when we wait for an answer. It's not the operation time, it's the time    * we wait when we start to receive an answer, when the remote write starts to send the data.    */
specifier|public
specifier|final
specifier|static
name|String
name|SOCKET_TIMEOUT_READ
init|=
literal|"hbase.ipc.client.socket.timeout.read"
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|String
name|SOCKET_TIMEOUT_WRITE
init|=
literal|"hbase.ipc.client.socket.timeout.write"
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|int
name|DEFAULT_SOCKET_TIMEOUT_CONNECT
init|=
literal|10000
decl_stmt|;
comment|// 10 seconds
specifier|public
specifier|final
specifier|static
name|int
name|DEFAULT_SOCKET_TIMEOUT_READ
init|=
literal|20000
decl_stmt|;
comment|// 20 seconds
specifier|public
specifier|final
specifier|static
name|int
name|DEFAULT_SOCKET_TIMEOUT_WRITE
init|=
literal|60000
decl_stmt|;
comment|// 60 seconds
comment|// Used by the server, for compatibility with old clients.
comment|// The client in 0.99+ does not ping the server.
specifier|final
specifier|static
name|int
name|PING_CALL_ID
init|=
operator|-
literal|1
decl_stmt|;
comment|/**    * Creates a "channel" that can be used by a blocking protobuf service.  Useful setting up    * protobuf blocking stubs.    *    * @return A blocking rpc channel that goes via this rpc client instance.    */
specifier|public
name|BlockingRpcChannel
name|createBlockingRpcChannel
parameter_list|(
name|ServerName
name|sn
parameter_list|,
name|User
name|user
parameter_list|,
name|int
name|rpcTimeout
parameter_list|)
function_decl|;
comment|/**    * Interrupt the connections to the given server. This should be called if the server    * is known as actually dead. This will not prevent current operation to be retried, and,    * depending on their own behavior, they may retry on the same server. This can be a feature,    * for example at startup. In any case, they're likely to get connection refused (if the    * process died) or no route to host: i.e. their next retries should be faster and with a    * safe exception.    */
specifier|public
name|void
name|cancelConnections
parameter_list|(
name|ServerName
name|sn
parameter_list|)
function_decl|;
comment|/**    * Stop all threads related to this client.  No further calls may be made    * using this client.    */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

