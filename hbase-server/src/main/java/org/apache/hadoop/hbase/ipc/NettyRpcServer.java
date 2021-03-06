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
name|java
operator|.
name|io
operator|.
name|InterruptedIOException
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
name|util
operator|.
name|List
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
name|CountDownLatch
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
name|Server
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
name|monitoring
operator|.
name|MonitoredRPCHandler
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
name|regionserver
operator|.
name|HRegionServer
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
name|HBasePolicyProvider
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
name|NettyEventLoopGroupConfig
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
name|security
operator|.
name|authorize
operator|.
name|ServiceAuthorizationManager
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|BlockingService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|MethodDescriptor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|bootstrap
operator|.
name|ServerBootstrap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelPipeline
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ServerChannel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|group
operator|.
name|ChannelGroup
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|group
operator|.
name|DefaultChannelGroup
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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
name|NioServerSocketChannel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|FixedLengthFrameDecoder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|concurrent
operator|.
name|DefaultThreadFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|concurrent
operator|.
name|GlobalEventExecutor
import|;
end_import

begin_comment
comment|/**  * An RPC server with Netty4 implementation.  * @since 2.0.0  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
block|{
name|HBaseInterfaceAudience
operator|.
name|CONFIG
block|}
argument_list|)
specifier|public
class|class
name|NettyRpcServer
extends|extends
name|RpcServer
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|NettyRpcServer
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Name of property to change netty rpc server eventloop thread count. Default is 0.    * Tests may set this down from unlimited.    */
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_NETTY_EVENTLOOP_RPCSERVER_THREADCOUNT_KEY
init|=
literal|"hbase.netty.eventloop.rpcserver.thread.count"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|EVENTLOOP_THREADCOUNT_DEFAULT
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|InetSocketAddress
name|bindAddress
decl_stmt|;
specifier|private
specifier|final
name|CountDownLatch
name|closed
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Channel
name|serverChannel
decl_stmt|;
specifier|private
specifier|final
name|ChannelGroup
name|allChannels
init|=
operator|new
name|DefaultChannelGroup
argument_list|(
name|GlobalEventExecutor
operator|.
name|INSTANCE
argument_list|,
literal|true
argument_list|)
decl_stmt|;
specifier|public
name|NettyRpcServer
parameter_list|(
name|Server
name|server
parameter_list|,
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|BlockingServiceAndInterface
argument_list|>
name|services
parameter_list|,
name|InetSocketAddress
name|bindAddress
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|RpcScheduler
name|scheduler
parameter_list|,
name|boolean
name|reservoirEnabled
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|server
argument_list|,
name|name
argument_list|,
name|services
argument_list|,
name|bindAddress
argument_list|,
name|conf
argument_list|,
name|scheduler
argument_list|,
name|reservoirEnabled
argument_list|)
expr_stmt|;
name|this
operator|.
name|bindAddress
operator|=
name|bindAddress
expr_stmt|;
name|EventLoopGroup
name|eventLoopGroup
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|ServerChannel
argument_list|>
name|channelClass
decl_stmt|;
if|if
condition|(
name|server
operator|instanceof
name|HRegionServer
condition|)
block|{
name|NettyEventLoopGroupConfig
name|config
init|=
operator|(
operator|(
name|HRegionServer
operator|)
name|server
operator|)
operator|.
name|getEventLoopGroupConfig
argument_list|()
decl_stmt|;
name|eventLoopGroup
operator|=
name|config
operator|.
name|group
argument_list|()
expr_stmt|;
name|channelClass
operator|=
name|config
operator|.
name|serverChannelClass
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|int
name|threadCount
init|=
name|server
operator|==
literal|null
condition|?
name|EVENTLOOP_THREADCOUNT_DEFAULT
else|:
name|server
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|HBASE_NETTY_EVENTLOOP_RPCSERVER_THREADCOUNT_KEY
argument_list|,
name|EVENTLOOP_THREADCOUNT_DEFAULT
argument_list|)
decl_stmt|;
name|eventLoopGroup
operator|=
operator|new
name|NioEventLoopGroup
argument_list|(
name|threadCount
argument_list|,
operator|new
name|DefaultThreadFactory
argument_list|(
literal|"NettyRpcServer"
argument_list|,
literal|true
argument_list|,
name|Thread
operator|.
name|MAX_PRIORITY
argument_list|)
argument_list|)
expr_stmt|;
name|channelClass
operator|=
name|NioServerSocketChannel
operator|.
name|class
expr_stmt|;
block|}
name|ServerBootstrap
name|bootstrap
init|=
operator|new
name|ServerBootstrap
argument_list|()
operator|.
name|group
argument_list|(
name|eventLoopGroup
argument_list|)
operator|.
name|channel
argument_list|(
name|channelClass
argument_list|)
operator|.
name|childOption
argument_list|(
name|ChannelOption
operator|.
name|TCP_NODELAY
argument_list|,
name|tcpNoDelay
argument_list|)
operator|.
name|childOption
argument_list|(
name|ChannelOption
operator|.
name|SO_KEEPALIVE
argument_list|,
name|tcpKeepAlive
argument_list|)
operator|.
name|childOption
argument_list|(
name|ChannelOption
operator|.
name|SO_REUSEADDR
argument_list|,
literal|true
argument_list|)
operator|.
name|childHandler
argument_list|(
operator|new
name|ChannelInitializer
argument_list|<
name|Channel
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|void
name|initChannel
parameter_list|(
name|Channel
name|ch
parameter_list|)
throws|throws
name|Exception
block|{
name|ChannelPipeline
name|pipeline
init|=
name|ch
operator|.
name|pipeline
argument_list|()
decl_stmt|;
name|FixedLengthFrameDecoder
name|preambleDecoder
init|=
operator|new
name|FixedLengthFrameDecoder
argument_list|(
literal|6
argument_list|)
decl_stmt|;
name|preambleDecoder
operator|.
name|setSingleDecode
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|pipeline
operator|.
name|addLast
argument_list|(
literal|"preambleDecoder"
argument_list|,
name|preambleDecoder
argument_list|)
expr_stmt|;
name|pipeline
operator|.
name|addLast
argument_list|(
literal|"preambleHandler"
argument_list|,
name|createNettyRpcServerPreambleHandler
argument_list|()
argument_list|)
expr_stmt|;
name|pipeline
operator|.
name|addLast
argument_list|(
literal|"frameDecoder"
argument_list|,
operator|new
name|NettyRpcFrameDecoder
argument_list|(
name|maxRequestSize
argument_list|)
argument_list|)
expr_stmt|;
name|pipeline
operator|.
name|addLast
argument_list|(
literal|"decoder"
argument_list|,
operator|new
name|NettyRpcServerRequestDecoder
argument_list|(
name|allChannels
argument_list|,
name|metrics
argument_list|)
argument_list|)
expr_stmt|;
name|pipeline
operator|.
name|addLast
argument_list|(
literal|"encoder"
argument_list|,
operator|new
name|NettyRpcServerResponseEncoder
argument_list|(
name|metrics
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
decl_stmt|;
try|try
block|{
name|serverChannel
operator|=
name|bootstrap
operator|.
name|bind
argument_list|(
name|this
operator|.
name|bindAddress
argument_list|)
operator|.
name|sync
argument_list|()
operator|.
name|channel
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Bind to {}"
argument_list|,
name|serverChannel
operator|.
name|localAddress
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
name|initReconfigurable
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|scheduler
operator|.
name|init
argument_list|(
operator|new
name|RpcSchedulerContext
argument_list|(
name|this
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|protected
name|NettyRpcServerPreambleHandler
name|createNettyRpcServerPreambleHandler
parameter_list|()
block|{
return|return
operator|new
name|NettyRpcServerPreambleHandler
argument_list|(
name|NettyRpcServer
operator|.
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|start
parameter_list|()
block|{
if|if
condition|(
name|started
condition|)
block|{
return|return;
block|}
name|authTokenSecretMgr
operator|=
name|createSecretManager
argument_list|()
expr_stmt|;
if|if
condition|(
name|authTokenSecretMgr
operator|!=
literal|null
condition|)
block|{
name|setSecretManager
argument_list|(
name|authTokenSecretMgr
argument_list|)
expr_stmt|;
name|authTokenSecretMgr
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|authManager
operator|=
operator|new
name|ServiceAuthorizationManager
argument_list|()
expr_stmt|;
name|HBasePolicyProvider
operator|.
name|init
argument_list|(
name|conf
argument_list|,
name|authManager
argument_list|)
expr_stmt|;
name|scheduler
operator|.
name|start
argument_list|()
expr_stmt|;
name|started
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|stop
parameter_list|()
block|{
if|if
condition|(
operator|!
name|running
condition|)
block|{
return|return;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping server on "
operator|+
name|this
operator|.
name|serverChannel
operator|.
name|localAddress
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|authTokenSecretMgr
operator|!=
literal|null
condition|)
block|{
name|authTokenSecretMgr
operator|.
name|stop
argument_list|()
expr_stmt|;
name|authTokenSecretMgr
operator|=
literal|null
expr_stmt|;
block|}
name|allChannels
operator|.
name|close
argument_list|()
operator|.
name|awaitUninterruptibly
argument_list|()
expr_stmt|;
name|serverChannel
operator|.
name|close
argument_list|()
expr_stmt|;
name|scheduler
operator|.
name|stop
argument_list|()
expr_stmt|;
name|closed
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|running
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|join
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|closed
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|InetSocketAddress
name|getListenerAddress
parameter_list|()
block|{
return|return
operator|(
operator|(
name|InetSocketAddress
operator|)
name|serverChannel
operator|.
name|localAddress
argument_list|()
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setSocketSendBufSize
parameter_list|(
name|int
name|size
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|int
name|getNumOpenConnections
parameter_list|()
block|{
name|int
name|channelsCount
init|=
name|allChannels
operator|.
name|size
argument_list|()
decl_stmt|;
comment|// allChannels also contains the server channel, so exclude that from the count.
return|return
name|channelsCount
operator|>
literal|0
condition|?
name|channelsCount
operator|-
literal|1
else|:
name|channelsCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|Pair
argument_list|<
name|Message
argument_list|,
name|CellScanner
argument_list|>
name|call
parameter_list|(
name|BlockingService
name|service
parameter_list|,
name|MethodDescriptor
name|md
parameter_list|,
name|Message
name|param
parameter_list|,
name|CellScanner
name|cellScanner
parameter_list|,
name|long
name|receiveTime
parameter_list|,
name|MonitoredRPCHandler
name|status
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|call
argument_list|(
name|service
argument_list|,
name|md
argument_list|,
name|param
argument_list|,
name|cellScanner
argument_list|,
name|receiveTime
argument_list|,
name|status
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Pair
argument_list|<
name|Message
argument_list|,
name|CellScanner
argument_list|>
name|call
parameter_list|(
name|BlockingService
name|service
parameter_list|,
name|MethodDescriptor
name|md
parameter_list|,
name|Message
name|param
parameter_list|,
name|CellScanner
name|cellScanner
parameter_list|,
name|long
name|receiveTime
parameter_list|,
name|MonitoredRPCHandler
name|status
parameter_list|,
name|long
name|startTime
parameter_list|,
name|int
name|timeout
parameter_list|)
throws|throws
name|IOException
block|{
name|NettyServerCall
name|fakeCall
init|=
operator|new
name|NettyServerCall
argument_list|(
operator|-
literal|1
argument_list|,
name|service
argument_list|,
name|md
argument_list|,
literal|null
argument_list|,
name|param
argument_list|,
name|cellScanner
argument_list|,
literal|null
argument_list|,
operator|-
literal|1
argument_list|,
literal|null
argument_list|,
name|receiveTime
argument_list|,
name|timeout
argument_list|,
name|bbAllocator
argument_list|,
name|cellBlockBuilder
argument_list|,
literal|null
argument_list|)
decl_stmt|;
return|return
name|call
argument_list|(
name|fakeCall
argument_list|,
name|status
argument_list|)
return|;
block|}
block|}
end_class

end_unit

