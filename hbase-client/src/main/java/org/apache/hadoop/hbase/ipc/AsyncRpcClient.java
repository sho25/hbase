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
name|io
operator|.
name|netty
operator|.
name|bootstrap
operator|.
name|Bootstrap
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|buffer
operator|.
name|PooledByteBufAllocator
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|Channel
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelInitializer
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelOption
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|EventLoopGroup
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|epoll
operator|.
name|EpollEventLoopGroup
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|epoll
operator|.
name|EpollSocketChannel
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|nio
operator|.
name|NioEventLoopGroup
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|socket
operator|.
name|SocketChannel
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|socket
operator|.
name|nio
operator|.
name|NioSocketChannel
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|HashedWheelTimer
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Future
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|concurrent
operator|.
name|GenericFutureListener
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Promise
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
name|nio
operator|.
name|ByteBuffer
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
name|ExecutionException
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeoutException
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
name|atomic
operator|.
name|AtomicInteger
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
name|HBaseInterfaceAudience
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
name|util
operator|.
name|JVM
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
name|hbase
operator|.
name|util
operator|.
name|Threads
import|;
end_import

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
name|RpcCallback
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
name|RpcChannel
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

begin_comment
comment|/**  * Netty client for the requests and responses  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|CONFIG
argument_list|)
specifier|public
class|class
name|AsyncRpcClient
extends|extends
name|AbstractRpcClient
block|{
specifier|public
specifier|static
specifier|final
name|String
name|CLIENT_MAX_THREADS
init|=
literal|"hbase.rpc.client.threads.max"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|USE_NATIVE_TRANSPORT
init|=
literal|"hbase.rpc.client.nativetransport"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|USE_GLOBAL_EVENT_LOOP_GROUP
init|=
literal|"hbase.rpc.client.globaleventloopgroup"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|HashedWheelTimer
name|WHEEL_TIMER
init|=
operator|new
name|HashedWheelTimer
argument_list|(
literal|100
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ChannelInitializer
argument_list|<
name|SocketChannel
argument_list|>
name|DEFAULT_CHANNEL_INITIALIZER
init|=
operator|new
name|ChannelInitializer
argument_list|<
name|SocketChannel
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|void
name|initChannel
parameter_list|(
name|SocketChannel
name|ch
parameter_list|)
throws|throws
name|Exception
block|{
comment|//empty initializer
block|}
block|}
decl_stmt|;
specifier|protected
specifier|final
name|AtomicInteger
name|callIdCnt
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|PoolMap
argument_list|<
name|Integer
argument_list|,
name|AsyncRpcChannel
argument_list|>
name|connections
decl_stmt|;
specifier|final
name|FailedServers
name|failedServers
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|final
name|Bootstrap
name|bootstrap
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|useGlobalEventLoopGroup
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|static
name|Pair
argument_list|<
name|EventLoopGroup
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Channel
argument_list|>
argument_list|>
name|GLOBAL_EVENT_LOOP_GROUP
decl_stmt|;
specifier|private
specifier|synchronized
specifier|static
name|Pair
argument_list|<
name|EventLoopGroup
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Channel
argument_list|>
argument_list|>
name|getGlobalEventLoopGroup
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|GLOBAL_EVENT_LOOP_GROUP
operator|==
literal|null
condition|)
block|{
name|GLOBAL_EVENT_LOOP_GROUP
operator|=
name|createEventLoopGroup
argument_list|(
name|conf
argument_list|)
expr_stmt|;
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
literal|"Create global event loop group "
operator|+
name|GLOBAL_EVENT_LOOP_GROUP
operator|.
name|getFirst
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|GLOBAL_EVENT_LOOP_GROUP
return|;
block|}
specifier|private
specifier|static
name|Pair
argument_list|<
name|EventLoopGroup
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Channel
argument_list|>
argument_list|>
name|createEventLoopGroup
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// Max amount of threads to use. 0 lets Netty decide based on amount of cores
name|int
name|maxThreads
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|CLIENT_MAX_THREADS
argument_list|,
literal|0
argument_list|)
decl_stmt|;
comment|// Config to enable native transport. Does not seem to be stable at time of implementation
comment|// although it is not extensively tested.
name|boolean
name|epollEnabled
init|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|USE_NATIVE_TRANSPORT
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|// Use the faster native epoll transport mechanism on linux if enabled
if|if
condition|(
name|epollEnabled
operator|&&
name|JVM
operator|.
name|isLinux
argument_list|()
condition|)
block|{
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
literal|"Create EpollEventLoopGroup with maxThreads = "
operator|+
name|maxThreads
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|Pair
argument_list|<
name|EventLoopGroup
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Channel
argument_list|>
argument_list|>
argument_list|(
operator|new
name|EpollEventLoopGroup
argument_list|(
name|maxThreads
argument_list|,
name|Threads
operator|.
name|newDaemonThreadFactory
argument_list|(
literal|"AsyncRpcChannel"
argument_list|)
argument_list|)
argument_list|,
name|EpollSocketChannel
operator|.
name|class
argument_list|)
return|;
block|}
else|else
block|{
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
literal|"Create NioEventLoopGroup with maxThreads = "
operator|+
name|maxThreads
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|Pair
argument_list|<
name|EventLoopGroup
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Channel
argument_list|>
argument_list|>
argument_list|(
operator|new
name|NioEventLoopGroup
argument_list|(
name|maxThreads
argument_list|,
name|Threads
operator|.
name|newDaemonThreadFactory
argument_list|(
literal|"AsyncRpcChannel"
argument_list|)
argument_list|)
argument_list|,
name|NioSocketChannel
operator|.
name|class
argument_list|)
return|;
block|}
block|}
comment|/**    * Constructor for tests    *    * @param configuration      to HBase    * @param clusterId          for the cluster    * @param localAddress       local address to connect to    * @param channelInitializer for custom channel handlers    */
annotation|@
name|VisibleForTesting
name|AsyncRpcClient
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|String
name|clusterId
parameter_list|,
name|SocketAddress
name|localAddress
parameter_list|,
name|ChannelInitializer
argument_list|<
name|SocketChannel
argument_list|>
name|channelInitializer
parameter_list|)
block|{
name|super
argument_list|(
name|configuration
argument_list|,
name|clusterId
argument_list|,
name|localAddress
argument_list|)
expr_stmt|;
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
literal|"Starting async Hbase RPC client"
argument_list|)
expr_stmt|;
block|}
name|Pair
argument_list|<
name|EventLoopGroup
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Channel
argument_list|>
argument_list|>
name|eventLoopGroupAndChannelClass
decl_stmt|;
name|this
operator|.
name|useGlobalEventLoopGroup
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|USE_GLOBAL_EVENT_LOOP_GROUP
argument_list|,
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|useGlobalEventLoopGroup
condition|)
block|{
name|eventLoopGroupAndChannelClass
operator|=
name|getGlobalEventLoopGroup
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|eventLoopGroupAndChannelClass
operator|=
name|createEventLoopGroup
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
block|}
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
literal|"Use "
operator|+
operator|(
name|useGlobalEventLoopGroup
condition|?
literal|"global"
else|:
literal|"individual"
operator|)
operator|+
literal|" event loop group "
operator|+
name|eventLoopGroupAndChannelClass
operator|.
name|getFirst
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|connections
operator|=
operator|new
name|PoolMap
argument_list|<>
argument_list|(
name|getPoolType
argument_list|(
name|configuration
argument_list|)
argument_list|,
name|getPoolSize
argument_list|(
name|configuration
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|failedServers
operator|=
operator|new
name|FailedServers
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
name|int
name|operationTimeout
init|=
name|configuration
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|)
decl_stmt|;
comment|// Configure the default bootstrap.
name|this
operator|.
name|bootstrap
operator|=
operator|new
name|Bootstrap
argument_list|()
expr_stmt|;
name|bootstrap
operator|.
name|group
argument_list|(
name|eventLoopGroupAndChannelClass
operator|.
name|getFirst
argument_list|()
argument_list|)
operator|.
name|channel
argument_list|(
name|eventLoopGroupAndChannelClass
operator|.
name|getSecond
argument_list|()
argument_list|)
operator|.
name|option
argument_list|(
name|ChannelOption
operator|.
name|ALLOCATOR
argument_list|,
name|PooledByteBufAllocator
operator|.
name|DEFAULT
argument_list|)
operator|.
name|option
argument_list|(
name|ChannelOption
operator|.
name|TCP_NODELAY
argument_list|,
name|tcpNoDelay
argument_list|)
operator|.
name|option
argument_list|(
name|ChannelOption
operator|.
name|SO_KEEPALIVE
argument_list|,
name|tcpKeepAlive
argument_list|)
operator|.
name|option
argument_list|(
name|ChannelOption
operator|.
name|CONNECT_TIMEOUT_MILLIS
argument_list|,
name|operationTimeout
argument_list|)
expr_stmt|;
if|if
condition|(
name|channelInitializer
operator|==
literal|null
condition|)
block|{
name|channelInitializer
operator|=
name|DEFAULT_CHANNEL_INITIALIZER
expr_stmt|;
block|}
name|bootstrap
operator|.
name|handler
argument_list|(
name|channelInitializer
argument_list|)
expr_stmt|;
if|if
condition|(
name|localAddress
operator|!=
literal|null
condition|)
block|{
name|bootstrap
operator|.
name|localAddress
argument_list|(
name|localAddress
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Constructor    *    * @param configuration to HBase    * @param clusterId     for the cluster    * @param localAddress  local address to connect to    */
specifier|public
name|AsyncRpcClient
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|String
name|clusterId
parameter_list|,
name|SocketAddress
name|localAddress
parameter_list|)
block|{
name|this
argument_list|(
name|configuration
argument_list|,
name|clusterId
argument_list|,
name|localAddress
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Make a call, passing<code>param</code>, to the IPC server running at    *<code>address</code> which is servicing the<code>protocol</code> protocol,    * with the<code>ticket</code> credentials, returning the value.    * Throws exceptions if there are network problems or if the remote code    * threw an exception.    *    * @param ticket Be careful which ticket you pass. A new user will mean a new Connection.    *               {@link org.apache.hadoop.hbase.security.UserProvider#getCurrent()} makes a new    *               instance of User each time so will be a new Connection each time.    * @return A pair with the Message response and the Cell data (if any).    * @throws InterruptedException if call is interrupted    * @throws java.io.IOException  if a connection failure is encountered    */
annotation|@
name|Override
specifier|protected
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
name|addr
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
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
specifier|final
name|AsyncRpcChannel
name|connection
init|=
name|createRpcChannel
argument_list|(
name|md
operator|.
name|getService
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|addr
argument_list|,
name|ticket
argument_list|)
decl_stmt|;
name|Promise
argument_list|<
name|Message
argument_list|>
name|promise
init|=
name|connection
operator|.
name|callMethod
argument_list|(
name|md
argument_list|,
name|pcrc
argument_list|,
name|param
argument_list|,
name|returnType
argument_list|)
decl_stmt|;
name|long
name|timeout
init|=
name|pcrc
operator|.
name|hasCallTimeout
argument_list|()
condition|?
name|pcrc
operator|.
name|getCallTimeout
argument_list|()
else|:
literal|0
decl_stmt|;
try|try
block|{
name|Message
name|response
init|=
name|timeout
operator|>
literal|0
condition|?
name|promise
operator|.
name|get
argument_list|(
name|timeout
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
else|:
name|promise
operator|.
name|get
argument_list|()
decl_stmt|;
return|return
operator|new
name|Pair
argument_list|<>
argument_list|(
name|response
argument_list|,
name|pcrc
operator|.
name|cellScanner
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|IOException
condition|)
block|{
throw|throw
operator|(
name|IOException
operator|)
name|e
operator|.
name|getCause
argument_list|()
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|TimeoutException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|CallTimeoutException
argument_list|(
name|promise
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/**    * Call method async    */
specifier|private
name|void
name|callMethod
parameter_list|(
name|Descriptors
operator|.
name|MethodDescriptor
name|md
parameter_list|,
specifier|final
name|PayloadCarryingRpcController
name|pcrc
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
name|addr
parameter_list|,
specifier|final
name|RpcCallback
argument_list|<
name|Message
argument_list|>
name|done
parameter_list|)
block|{
specifier|final
name|AsyncRpcChannel
name|connection
decl_stmt|;
try|try
block|{
name|connection
operator|=
name|createRpcChannel
argument_list|(
name|md
operator|.
name|getService
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|addr
argument_list|,
name|ticket
argument_list|)
expr_stmt|;
name|connection
operator|.
name|callMethod
argument_list|(
name|md
argument_list|,
name|pcrc
argument_list|,
name|param
argument_list|,
name|returnType
argument_list|)
operator|.
name|addListener
argument_list|(
operator|new
name|GenericFutureListener
argument_list|<
name|Future
argument_list|<
name|Message
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|operationComplete
parameter_list|(
name|Future
argument_list|<
name|Message
argument_list|>
name|future
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|future
operator|.
name|isSuccess
argument_list|()
condition|)
block|{
name|Throwable
name|cause
init|=
name|future
operator|.
name|cause
argument_list|()
decl_stmt|;
if|if
condition|(
name|cause
operator|instanceof
name|IOException
condition|)
block|{
name|pcrc
operator|.
name|setFailed
argument_list|(
operator|(
name|IOException
operator|)
name|cause
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|pcrc
operator|.
name|setFailed
argument_list|(
operator|new
name|IOException
argument_list|(
name|cause
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
try|try
block|{
name|done
operator|.
name|run
argument_list|(
name|future
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
name|Throwable
name|cause
init|=
name|e
operator|.
name|getCause
argument_list|()
decl_stmt|;
if|if
condition|(
name|cause
operator|instanceof
name|IOException
condition|)
block|{
name|pcrc
operator|.
name|setFailed
argument_list|(
operator|(
name|IOException
operator|)
name|cause
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|pcrc
operator|.
name|setFailed
argument_list|(
operator|new
name|IOException
argument_list|(
name|cause
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|pcrc
operator|.
name|setFailed
argument_list|(
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|StoppedRpcClientException
decl||
name|FailedServerException
name|e
parameter_list|)
block|{
name|pcrc
operator|.
name|setFailed
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|boolean
name|closed
init|=
literal|false
decl_stmt|;
comment|/**    * Close netty    */
specifier|public
name|void
name|close
parameter_list|()
block|{
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
literal|"Stopping async HBase RPC client"
argument_list|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|connections
init|)
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return;
block|}
name|closed
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|AsyncRpcChannel
name|conn
range|:
name|connections
operator|.
name|values
argument_list|()
control|)
block|{
name|conn
operator|.
name|close
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
comment|// do not close global EventLoopGroup.
if|if
condition|(
operator|!
name|useGlobalEventLoopGroup
condition|)
block|{
name|bootstrap
operator|.
name|group
argument_list|()
operator|.
name|shutdownGracefully
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Create a cell scanner    *    * @param cellBlock to create scanner for    * @return CellScanner    * @throws java.io.IOException on error on creation cell scanner    */
specifier|public
name|CellScanner
name|createCellScanner
parameter_list|(
name|byte
index|[]
name|cellBlock
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ipcUtil
operator|.
name|createCellScanner
argument_list|(
name|this
operator|.
name|codec
argument_list|,
name|this
operator|.
name|compressor
argument_list|,
name|cellBlock
argument_list|)
return|;
block|}
comment|/**    * Build cell block    *    * @param cells to create block with    * @return ByteBuffer with cells    * @throws java.io.IOException if block creation fails    */
specifier|public
name|ByteBuffer
name|buildCellBlock
parameter_list|(
name|CellScanner
name|cells
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ipcUtil
operator|.
name|buildCellBlock
argument_list|(
name|this
operator|.
name|codec
argument_list|,
name|this
operator|.
name|compressor
argument_list|,
name|cells
argument_list|)
return|;
block|}
comment|/**    * Creates an RPC client    *    * @param serviceName    name of servicce    * @param location       to connect to    * @param ticket         for current user    * @return new RpcChannel    * @throws StoppedRpcClientException when Rpc client is stopped    * @throws FailedServerException if server failed    */
specifier|private
name|AsyncRpcChannel
name|createRpcChannel
parameter_list|(
name|String
name|serviceName
parameter_list|,
name|InetSocketAddress
name|location
parameter_list|,
name|User
name|ticket
parameter_list|)
throws|throws
name|StoppedRpcClientException
throws|,
name|FailedServerException
block|{
comment|// Check if server is failed
if|if
condition|(
name|this
operator|.
name|failedServers
operator|.
name|isFailedServer
argument_list|(
name|location
argument_list|)
condition|)
block|{
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
literal|"Not trying to connect to "
operator|+
name|location
operator|+
literal|" this server is in the failed servers list"
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|FailedServerException
argument_list|(
literal|"This server is in the failed servers list: "
operator|+
name|location
argument_list|)
throw|;
block|}
name|int
name|hashCode
init|=
name|ConnectionId
operator|.
name|hashCode
argument_list|(
name|ticket
argument_list|,
name|serviceName
argument_list|,
name|location
argument_list|)
decl_stmt|;
name|AsyncRpcChannel
name|rpcChannel
decl_stmt|;
synchronized|synchronized
init|(
name|connections
init|)
block|{
if|if
condition|(
name|closed
condition|)
block|{
throw|throw
operator|new
name|StoppedRpcClientException
argument_list|()
throw|;
block|}
name|rpcChannel
operator|=
name|connections
operator|.
name|get
argument_list|(
name|hashCode
argument_list|)
expr_stmt|;
if|if
condition|(
name|rpcChannel
operator|==
literal|null
condition|)
block|{
name|rpcChannel
operator|=
operator|new
name|AsyncRpcChannel
argument_list|(
name|this
operator|.
name|bootstrap
argument_list|,
name|this
argument_list|,
name|ticket
argument_list|,
name|serviceName
argument_list|,
name|location
argument_list|)
expr_stmt|;
name|connections
operator|.
name|put
argument_list|(
name|hashCode
argument_list|,
name|rpcChannel
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|rpcChannel
return|;
block|}
comment|/**    * Interrupt the connections to the given ip:port server. This should be called if the server    * is known as actually dead. This will not prevent current operation to be retried, and,    * depending on their own behavior, they may retry on the same server. This can be a feature,    * for example at startup. In any case, they're likely to get connection refused (if the    * process died) or no route to host: i.e. there next retries should be faster and with a    * safe exception.    *    * @param sn server to cancel connections for    */
annotation|@
name|Override
specifier|public
name|void
name|cancelConnections
parameter_list|(
name|ServerName
name|sn
parameter_list|)
block|{
synchronized|synchronized
init|(
name|connections
init|)
block|{
for|for
control|(
name|AsyncRpcChannel
name|rpcChannel
range|:
name|connections
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|rpcChannel
operator|.
name|isAlive
argument_list|()
operator|&&
name|rpcChannel
operator|.
name|address
operator|.
name|getPort
argument_list|()
operator|==
name|sn
operator|.
name|getPort
argument_list|()
operator|&&
name|rpcChannel
operator|.
name|address
operator|.
name|getHostName
argument_list|()
operator|.
name|contentEquals
argument_list|(
name|sn
operator|.
name|getHostname
argument_list|()
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"The server on "
operator|+
name|sn
operator|.
name|toString
argument_list|()
operator|+
literal|" is dead - stopping the connection "
operator|+
name|rpcChannel
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|rpcChannel
operator|.
name|close
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Remove connection from pool    */
specifier|public
name|void
name|removeConnection
parameter_list|(
name|AsyncRpcChannel
name|connection
parameter_list|)
block|{
name|int
name|connectionHashCode
init|=
name|connection
operator|.
name|getConnectionHashCode
argument_list|()
decl_stmt|;
synchronized|synchronized
init|(
name|connections
init|)
block|{
comment|// we use address as cache key, so we should check here to prevent removing the
comment|// wrong connection
name|AsyncRpcChannel
name|connectionInPool
init|=
name|this
operator|.
name|connections
operator|.
name|get
argument_list|(
name|connectionHashCode
argument_list|)
decl_stmt|;
if|if
condition|(
name|connectionInPool
operator|==
name|connection
condition|)
block|{
name|this
operator|.
name|connections
operator|.
name|remove
argument_list|(
name|connectionHashCode
argument_list|)
expr_stmt|;
block|}
elseif|else
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
name|String
operator|.
name|format
argument_list|(
literal|"%s already removed, expected instance %08x, actual %08x"
argument_list|,
name|connection
operator|.
name|toString
argument_list|()
argument_list|,
name|System
operator|.
name|identityHashCode
argument_list|(
name|connection
argument_list|)
argument_list|,
name|System
operator|.
name|identityHashCode
argument_list|(
name|connectionInPool
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Creates a "channel" that can be used by a protobuf service.  Useful setting up    * protobuf stubs.    *    * @param sn server name describing location of server    * @param user which is to use the connection    * @param rpcTimeout default rpc operation timeout    *    * @return A rpc channel that goes via this rpc client instance.    * @throws IOException when channel could not be created    */
specifier|public
name|RpcChannel
name|createRpcChannel
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|,
specifier|final
name|User
name|user
parameter_list|,
name|int
name|rpcTimeout
parameter_list|)
block|{
return|return
operator|new
name|RpcChannelImplementation
argument_list|(
name|this
argument_list|,
name|sn
argument_list|,
name|user
argument_list|,
name|rpcTimeout
argument_list|)
return|;
block|}
comment|/**    * Blocking rpc channel that goes via hbase rpc.    */
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
class|class
name|RpcChannelImplementation
implements|implements
name|RpcChannel
block|{
specifier|private
specifier|final
name|InetSocketAddress
name|isa
decl_stmt|;
specifier|private
specifier|final
name|AsyncRpcClient
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
name|RpcChannelImplementation
parameter_list|(
specifier|final
name|AsyncRpcClient
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
name|void
name|callMethod
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
parameter_list|,
name|RpcCallback
argument_list|<
name|Message
argument_list|>
name|done
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
name|this
operator|.
name|rpcClient
operator|.
name|callMethod
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
argument_list|,
name|done
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

