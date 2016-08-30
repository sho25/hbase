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
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcController
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
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

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|SocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|UnknownHostException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|Configuration
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
name|CellScanner
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
name|HConstants
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
name|client
operator|.
name|MetricsConnection
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
name|codec
operator|.
name|Codec
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
name|codec
operator|.
name|KeyValueCodec
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
name|UserProvider
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
name|util
operator|.
name|EnvironmentEdgeManager
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
name|util
operator|.
name|Pair
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
name|util
operator|.
name|PoolMap
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
name|io
operator|.
name|compress
operator|.
name|CompressionCodec
import|;
end_import

begin_comment
comment|/**  * Provides the basics for a RpcClient implementation like configuration and Logging.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|AbstractRpcClient
implements|implements
name|RpcClient
block|{
comment|// Log level is being changed in tests
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|AbstractRpcClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|protected
name|String
name|clusterId
decl_stmt|;
specifier|protected
specifier|final
name|SocketAddress
name|localAddr
decl_stmt|;
specifier|protected
specifier|final
name|MetricsConnection
name|metrics
decl_stmt|;
specifier|protected
name|UserProvider
name|userProvider
decl_stmt|;
specifier|protected
specifier|final
name|CellBlockBuilder
name|cellBlockBuilder
decl_stmt|;
specifier|protected
specifier|final
name|int
name|minIdleTimeBeforeClose
decl_stmt|;
comment|// if the connection is idle for more than this
comment|// time (in ms), it will be closed at any moment.
specifier|protected
specifier|final
name|int
name|maxRetries
decl_stmt|;
comment|//the max. no. of retries for socket connections
specifier|protected
specifier|final
name|long
name|failureSleep
decl_stmt|;
comment|// Time to sleep before retry on failure.
specifier|protected
specifier|final
name|boolean
name|tcpNoDelay
decl_stmt|;
comment|// if T then disable Nagle's Algorithm
specifier|protected
specifier|final
name|boolean
name|tcpKeepAlive
decl_stmt|;
comment|// if T then use keepalives
specifier|protected
specifier|final
name|Codec
name|codec
decl_stmt|;
specifier|protected
specifier|final
name|CompressionCodec
name|compressor
decl_stmt|;
specifier|protected
specifier|final
name|boolean
name|fallbackAllowed
decl_stmt|;
specifier|protected
specifier|final
name|int
name|connectTO
decl_stmt|;
specifier|protected
specifier|final
name|int
name|readTO
decl_stmt|;
specifier|protected
specifier|final
name|int
name|writeTO
decl_stmt|;
comment|/**    * Construct an IPC client for the cluster<code>clusterId</code>    *    * @param conf configuration    * @param clusterId the cluster id    * @param localAddr client socket bind address.    * @param metrics the connection metrics    */
specifier|public
name|AbstractRpcClient
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|clusterId
parameter_list|,
name|SocketAddress
name|localAddr
parameter_list|,
name|MetricsConnection
name|metrics
parameter_list|)
block|{
name|this
operator|.
name|userProvider
operator|=
name|UserProvider
operator|.
name|instantiate
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|localAddr
operator|=
name|localAddr
expr_stmt|;
name|this
operator|.
name|tcpKeepAlive
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"hbase.ipc.client.tcpkeepalive"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterId
operator|=
name|clusterId
operator|!=
literal|null
condition|?
name|clusterId
else|:
name|HConstants
operator|.
name|CLUSTER_ID_DEFAULT
expr_stmt|;
name|this
operator|.
name|failureSleep
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_PAUSE
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_PAUSE
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxRetries
operator|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.ipc.client.connect.max.retries"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|tcpNoDelay
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"hbase.ipc.client.tcpnodelay"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|cellBlockBuilder
operator|=
operator|new
name|CellBlockBuilder
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|minIdleTimeBeforeClose
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|IDLE_TIME
argument_list|,
literal|120000
argument_list|)
expr_stmt|;
comment|// 2 minutes
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|codec
operator|=
name|getCodec
argument_list|()
expr_stmt|;
name|this
operator|.
name|compressor
operator|=
name|getCompressor
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|fallbackAllowed
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|IPC_CLIENT_FALLBACK_TO_SIMPLE_AUTH_ALLOWED_KEY
argument_list|,
name|IPC_CLIENT_FALLBACK_TO_SIMPLE_AUTH_ALLOWED_DEFAULT
argument_list|)
expr_stmt|;
name|this
operator|.
name|connectTO
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|SOCKET_TIMEOUT_CONNECT
argument_list|,
name|DEFAULT_SOCKET_TIMEOUT_CONNECT
argument_list|)
expr_stmt|;
name|this
operator|.
name|readTO
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|SOCKET_TIMEOUT_READ
argument_list|,
name|DEFAULT_SOCKET_TIMEOUT_READ
argument_list|)
expr_stmt|;
name|this
operator|.
name|writeTO
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|SOCKET_TIMEOUT_WRITE
argument_list|,
name|DEFAULT_SOCKET_TIMEOUT_WRITE
argument_list|)
expr_stmt|;
name|this
operator|.
name|metrics
operator|=
name|metrics
expr_stmt|;
comment|// login the server principal (if using secure Hadoop)
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Codec="
operator|+
name|this
operator|.
name|codec
operator|+
literal|", compressor="
operator|+
name|this
operator|.
name|compressor
operator|+
literal|", tcpKeepAlive="
operator|+
name|this
operator|.
name|tcpKeepAlive
operator|+
literal|", tcpNoDelay="
operator|+
name|this
operator|.
name|tcpNoDelay
operator|+
literal|", connectTO="
operator|+
name|this
operator|.
name|connectTO
operator|+
literal|", readTO="
operator|+
name|this
operator|.
name|readTO
operator|+
literal|", writeTO="
operator|+
name|this
operator|.
name|writeTO
operator|+
literal|", minIdleTimeBeforeClose="
operator|+
name|this
operator|.
name|minIdleTimeBeforeClose
operator|+
literal|", maxRetries="
operator|+
name|this
operator|.
name|maxRetries
operator|+
literal|", fallbackAllowed="
operator|+
name|this
operator|.
name|fallbackAllowed
operator|+
literal|", bind address="
operator|+
operator|(
name|this
operator|.
name|localAddr
operator|!=
literal|null
condition|?
name|this
operator|.
name|localAddr
else|:
literal|"null"
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|String
name|getDefaultCodec
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|)
block|{
comment|// If "hbase.client.default.rpc.codec" is empty string -- you can't set it to null because
comment|// Configuration will complain -- then no default codec (and we'll pb everything).  Else
comment|// default is KeyValueCodec
return|return
name|c
operator|.
name|get
argument_list|(
name|DEFAULT_CODEC_CLASS
argument_list|,
name|KeyValueCodec
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Encapsulate the ugly casting and RuntimeException conversion in private method.    * @return Codec to use on this client.    */
name|Codec
name|getCodec
parameter_list|()
block|{
comment|// For NO CODEC, "hbase.client.rpc.codec" must be configured with empty string AND
comment|// "hbase.client.default.rpc.codec" also -- because default is to do cell block encoding.
name|String
name|className
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|RPC_CODEC_CONF_KEY
argument_list|,
name|getDefaultCodec
argument_list|(
name|this
operator|.
name|conf
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|className
operator|==
literal|null
operator|||
name|className
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
try|try
block|{
return|return
operator|(
name|Codec
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|)
operator|.
name|newInstance
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed getting codec "
operator|+
name|className
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasCellBlockSupport
parameter_list|()
block|{
return|return
name|this
operator|.
name|codec
operator|!=
literal|null
return|;
block|}
comment|/**    * Encapsulate the ugly casting and RuntimeException conversion in private method.    * @param conf configuration    * @return The compressor to use on this client.    */
specifier|private
specifier|static
name|CompressionCodec
name|getCompressor
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|className
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.client.rpc.compressor"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|className
operator|==
literal|null
operator|||
name|className
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
try|try
block|{
return|return
operator|(
name|CompressionCodec
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|)
operator|.
name|newInstance
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed getting compressor "
operator|+
name|className
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Return the pool type specified in the configuration, which must be set to    * either {@link org.apache.hadoop.hbase.util.PoolMap.PoolType#RoundRobin} or    * {@link org.apache.hadoop.hbase.util.PoolMap.PoolType#ThreadLocal},    * otherwise default to the former.    *    * For applications with many user threads, use a small round-robin pool. For    * applications with few user threads, you may want to try using a    * thread-local pool. In any case, the number of {@link org.apache.hadoop.hbase.ipc.RpcClient}    * instances should not exceed the operating system's hard limit on the number of    * connections.    *    * @param config configuration    * @return either a {@link org.apache.hadoop.hbase.util.PoolMap.PoolType#RoundRobin} or    *         {@link org.apache.hadoop.hbase.util.PoolMap.PoolType#ThreadLocal}    */
specifier|protected
specifier|static
name|PoolMap
operator|.
name|PoolType
name|getPoolType
parameter_list|(
name|Configuration
name|config
parameter_list|)
block|{
return|return
name|PoolMap
operator|.
name|PoolType
operator|.
name|valueOf
argument_list|(
name|config
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_IPC_POOL_TYPE
argument_list|)
argument_list|,
name|PoolMap
operator|.
name|PoolType
operator|.
name|RoundRobin
argument_list|,
name|PoolMap
operator|.
name|PoolType
operator|.
name|ThreadLocal
argument_list|)
return|;
block|}
comment|/**    * Return the pool size specified in the configuration, which is applicable only if    * the pool type is {@link org.apache.hadoop.hbase.util.PoolMap.PoolType#RoundRobin}.    *    * @param config configuration    * @return the maximum pool size    */
specifier|protected
specifier|static
name|int
name|getPoolSize
parameter_list|(
name|Configuration
name|config
parameter_list|)
block|{
return|return
name|config
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_IPC_POOL_SIZE
argument_list|,
literal|1
argument_list|)
return|;
block|}
comment|/**    * Make a blocking call. Throws exceptions if there are network problems or if the remote code    * threw an exception.    *    * @param ticket Be careful which ticket you pass. A new user will mean a new Connection.    *               {@link UserProvider#getCurrent()} makes a new instance of User each time so    *               will be a    *               new Connection each time.    * @return A pair with the Message response and the Cell data (if any).    */
name|Message
name|callBlockingMethod
parameter_list|(
name|Descriptors
operator|.
name|MethodDescriptor
name|md
parameter_list|,
name|PayloadCarryingRpcController
name|pcrc
parameter_list|,
name|Message
name|param
parameter_list|,
name|Message
name|returnType
parameter_list|,
specifier|final
name|User
name|ticket
parameter_list|,
specifier|final
name|InetSocketAddress
name|isa
parameter_list|)
throws|throws
name|ServiceException
block|{
if|if
condition|(
name|pcrc
operator|==
literal|null
condition|)
block|{
name|pcrc
operator|=
operator|new
name|PayloadCarryingRpcController
argument_list|()
expr_stmt|;
block|}
name|Pair
argument_list|<
name|Message
argument_list|,
name|CellScanner
argument_list|>
name|val
decl_stmt|;
try|try
block|{
specifier|final
name|MetricsConnection
operator|.
name|CallStats
name|cs
init|=
name|MetricsConnection
operator|.
name|newCallStats
argument_list|()
decl_stmt|;
name|cs
operator|.
name|setStartTime
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|)
expr_stmt|;
name|val
operator|=
name|call
argument_list|(
name|pcrc
argument_list|,
name|md
argument_list|,
name|param
argument_list|,
name|returnType
argument_list|,
name|ticket
argument_list|,
name|isa
argument_list|,
name|cs
argument_list|)
expr_stmt|;
comment|// Shove the results into controller so can be carried across the proxy/pb service void.
name|pcrc
operator|.
name|setCellScanner
argument_list|(
name|val
operator|.
name|getSecond
argument_list|()
argument_list|)
expr_stmt|;
name|cs
operator|.
name|setCallTimeMs
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|cs
operator|.
name|getStartTime
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|metrics
operator|!=
literal|null
condition|)
block|{
name|metrics
operator|.
name|updateRpc
argument_list|(
name|md
argument_list|,
name|param
argument_list|,
name|cs
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Call: "
operator|+
name|md
operator|.
name|getName
argument_list|()
operator|+
literal|", callTime: "
operator|+
name|cs
operator|.
name|getCallTimeMs
argument_list|()
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
block|}
return|return
name|val
operator|.
name|getFirst
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ServiceException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Make a call, passing<code>param</code>, to the IPC server running at    *<code>address</code> which is servicing the<code>protocol</code> protocol,    * with the<code>ticket</code> credentials, returning the value.    * Throws exceptions if there are network problems or if the remote code    * threw an exception.    *    * @param ticket Be careful which ticket you pass. A new user will mean a new Connection.    *               {@link UserProvider#getCurrent()} makes a new instance of User each time so    *               will be a    *               new Connection each time.    * @return A pair with the Message response and the Cell data (if any).    * @throws InterruptedException if call is interrupted    * @throws java.io.IOException if transport failed    */
specifier|protected
specifier|abstract
name|Pair
argument_list|<
name|Message
argument_list|,
name|CellScanner
argument_list|>
name|call
parameter_list|(
name|PayloadCarryingRpcController
name|pcrc
parameter_list|,
name|Descriptors
operator|.
name|MethodDescriptor
name|md
parameter_list|,
name|Message
name|param
parameter_list|,
name|Message
name|returnType
parameter_list|,
name|User
name|ticket
parameter_list|,
name|InetSocketAddress
name|isa
parameter_list|,
name|MetricsConnection
operator|.
name|CallStats
name|callStats
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
function_decl|;
annotation|@
name|Override
specifier|public
name|BlockingRpcChannel
name|createBlockingRpcChannel
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|,
specifier|final
name|User
name|ticket
parameter_list|,
name|int
name|defaultOperationTimeout
parameter_list|)
throws|throws
name|UnknownHostException
block|{
return|return
operator|new
name|BlockingRpcChannelImplementation
argument_list|(
name|this
argument_list|,
name|sn
argument_list|,
name|ticket
argument_list|,
name|defaultOperationTimeout
argument_list|)
return|;
block|}
comment|/**    * Configure a payload carrying controller    * @param controller to configure    * @param channelOperationTimeout timeout for operation    * @return configured payload controller    */
specifier|static
name|PayloadCarryingRpcController
name|configurePayloadCarryingRpcController
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|int
name|channelOperationTimeout
parameter_list|)
block|{
name|PayloadCarryingRpcController
name|pcrc
decl_stmt|;
if|if
condition|(
name|controller
operator|!=
literal|null
operator|&&
name|controller
operator|instanceof
name|PayloadCarryingRpcController
condition|)
block|{
name|pcrc
operator|=
operator|(
name|PayloadCarryingRpcController
operator|)
name|controller
expr_stmt|;
if|if
condition|(
operator|!
name|pcrc
operator|.
name|hasCallTimeout
argument_list|()
condition|)
block|{
name|pcrc
operator|.
name|setCallTimeout
argument_list|(
name|channelOperationTimeout
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|pcrc
operator|=
operator|new
name|PayloadCarryingRpcController
argument_list|()
expr_stmt|;
name|pcrc
operator|.
name|setCallTimeout
argument_list|(
name|channelOperationTimeout
argument_list|)
expr_stmt|;
block|}
return|return
name|pcrc
return|;
block|}
comment|/**    * Blocking rpc channel that goes via hbase rpc.    */
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
class|class
name|BlockingRpcChannelImplementation
implements|implements
name|BlockingRpcChannel
block|{
specifier|private
specifier|final
name|InetSocketAddress
name|isa
decl_stmt|;
specifier|private
specifier|final
name|AbstractRpcClient
name|rpcClient
decl_stmt|;
specifier|private
specifier|final
name|User
name|ticket
decl_stmt|;
specifier|private
specifier|final
name|int
name|channelOperationTimeout
decl_stmt|;
comment|/**      * @param channelOperationTimeout - the default timeout when no timeout is given      */
specifier|protected
name|BlockingRpcChannelImplementation
parameter_list|(
specifier|final
name|AbstractRpcClient
name|rpcClient
parameter_list|,
specifier|final
name|ServerName
name|sn
parameter_list|,
specifier|final
name|User
name|ticket
parameter_list|,
name|int
name|channelOperationTimeout
parameter_list|)
throws|throws
name|UnknownHostException
block|{
name|this
operator|.
name|isa
operator|=
operator|new
name|InetSocketAddress
argument_list|(
name|sn
operator|.
name|getHostname
argument_list|()
argument_list|,
name|sn
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|isa
operator|.
name|isUnresolved
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|UnknownHostException
argument_list|(
name|sn
operator|.
name|getHostname
argument_list|()
argument_list|)
throw|;
block|}
name|this
operator|.
name|rpcClient
operator|=
name|rpcClient
expr_stmt|;
name|this
operator|.
name|ticket
operator|=
name|ticket
expr_stmt|;
name|this
operator|.
name|channelOperationTimeout
operator|=
name|channelOperationTimeout
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Message
name|callBlockingMethod
parameter_list|(
name|Descriptors
operator|.
name|MethodDescriptor
name|md
parameter_list|,
name|RpcController
name|controller
parameter_list|,
name|Message
name|param
parameter_list|,
name|Message
name|returnType
parameter_list|)
throws|throws
name|ServiceException
block|{
name|PayloadCarryingRpcController
name|pcrc
init|=
name|configurePayloadCarryingRpcController
argument_list|(
name|controller
argument_list|,
name|channelOperationTimeout
argument_list|)
decl_stmt|;
return|return
name|this
operator|.
name|rpcClient
operator|.
name|callBlockingMethod
argument_list|(
name|md
argument_list|,
name|pcrc
argument_list|,
name|param
argument_list|,
name|returnType
argument_list|,
name|this
operator|.
name|ticket
argument_list|,
name|this
operator|.
name|isa
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

