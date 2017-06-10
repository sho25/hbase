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
import|import static
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
name|CallEvent
operator|.
name|Type
operator|.
name|CANCELLED
import|;
end_import

begin_import
import|import static
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
name|CallEvent
operator|.
name|Type
operator|.
name|TIMEOUT
import|;
end_import

begin_import
import|import static
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
name|IPCUtil
operator|.
name|setCancelled
import|;
end_import

begin_import
import|import static
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
name|IPCUtil
operator|.
name|toIOE
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|timeout
operator|.
name|ReadTimeoutHandler
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
name|NettyHBaseRpcConnectionHeaderHandler
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
name|ByteBuf
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
name|ByteBufOutputStream
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
name|Unpooled
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
name|ChannelFuture
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
name|ChannelFutureListener
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
name|ChannelHandler
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
name|ChannelPipeline
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|LengthFieldBasedFrameDecoder
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|timeout
operator|.
name|IdleStateHandler
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
name|ReferenceCountUtil
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
name|FutureListener
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
name|util
operator|.
name|concurrent
operator|.
name|Executors
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
name|ScheduledExecutorService
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
name|ThreadLocalRandom
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
name|ipc
operator|.
name|BufferCallBeforeInitHandler
operator|.
name|BufferCallEvent
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
name|HBaseRpcController
operator|.
name|CancellationCallback
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
name|RPCProtos
operator|.
name|ConnectionHeader
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
name|NettyHBaseSaslRpcClientHandler
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
name|SaslChallengeDecoder
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|security
operator|.
name|UserGroupInformation
import|;
end_import

begin_comment
comment|/**  * RPC connection implementation based on netty.  *<p>  * Most operations are executed in handlers. Netty handler is always executed in the same  * thread(EventLoop) so no lock is needed.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|NettyRpcConnection
extends|extends
name|RpcConnection
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|NettyRpcConnection
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ScheduledExecutorService
name|RELOGIN_EXECUTOR
init|=
name|Executors
operator|.
name|newSingleThreadScheduledExecutor
argument_list|(
name|Threads
operator|.
name|newDaemonThreadFactory
argument_list|(
literal|"Relogin"
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|NettyRpcClient
name|rpcClient
decl_stmt|;
specifier|private
name|ByteBuf
name|connectionHeaderPreamble
decl_stmt|;
specifier|private
name|ByteBuf
name|connectionHeaderWithLength
decl_stmt|;
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"IS2_INCONSISTENT_SYNC"
argument_list|,
name|justification
operator|=
literal|"connect is also under lock as notifyOnCancel will call our action directly"
argument_list|)
specifier|private
name|Channel
name|channel
decl_stmt|;
name|NettyRpcConnection
parameter_list|(
name|NettyRpcClient
name|rpcClient
parameter_list|,
name|ConnectionId
name|remoteId
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|rpcClient
operator|.
name|conf
argument_list|,
name|AbstractRpcClient
operator|.
name|WHEEL_TIMER
argument_list|,
name|remoteId
argument_list|,
name|rpcClient
operator|.
name|clusterId
argument_list|,
name|rpcClient
operator|.
name|userProvider
operator|.
name|isHBaseSecurityEnabled
argument_list|()
argument_list|,
name|rpcClient
operator|.
name|codec
argument_list|,
name|rpcClient
operator|.
name|compressor
argument_list|)
expr_stmt|;
name|this
operator|.
name|rpcClient
operator|=
name|rpcClient
expr_stmt|;
name|byte
index|[]
name|connectionHeaderPreamble
init|=
name|getConnectionHeaderPreamble
argument_list|()
decl_stmt|;
name|this
operator|.
name|connectionHeaderPreamble
operator|=
name|Unpooled
operator|.
name|directBuffer
argument_list|(
name|connectionHeaderPreamble
operator|.
name|length
argument_list|)
operator|.
name|writeBytes
argument_list|(
name|connectionHeaderPreamble
argument_list|)
expr_stmt|;
name|ConnectionHeader
name|header
init|=
name|getConnectionHeader
argument_list|()
decl_stmt|;
name|this
operator|.
name|connectionHeaderWithLength
operator|=
name|Unpooled
operator|.
name|directBuffer
argument_list|(
literal|4
operator|+
name|header
operator|.
name|getSerializedSize
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|connectionHeaderWithLength
operator|.
name|writeInt
argument_list|(
name|header
operator|.
name|getSerializedSize
argument_list|()
argument_list|)
expr_stmt|;
name|header
operator|.
name|writeTo
argument_list|(
operator|new
name|ByteBufOutputStream
argument_list|(
name|this
operator|.
name|connectionHeaderWithLength
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
specifier|synchronized
name|void
name|callTimeout
parameter_list|(
name|Call
name|call
parameter_list|)
block|{
if|if
condition|(
name|channel
operator|!=
literal|null
condition|)
block|{
name|channel
operator|.
name|pipeline
argument_list|()
operator|.
name|fireUserEventTriggered
argument_list|(
operator|new
name|CallEvent
argument_list|(
name|TIMEOUT
argument_list|,
name|call
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|boolean
name|isActive
parameter_list|()
block|{
return|return
name|channel
operator|!=
literal|null
return|;
block|}
specifier|private
name|void
name|shutdown0
parameter_list|()
block|{
if|if
condition|(
name|channel
operator|!=
literal|null
condition|)
block|{
name|channel
operator|.
name|close
argument_list|()
expr_stmt|;
name|channel
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|shutdown
parameter_list|()
block|{
name|shutdown0
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|cleanupConnection
parameter_list|()
block|{
if|if
condition|(
name|connectionHeaderPreamble
operator|!=
literal|null
condition|)
block|{
name|ReferenceCountUtil
operator|.
name|safeRelease
argument_list|(
name|connectionHeaderPreamble
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|connectionHeaderWithLength
operator|!=
literal|null
condition|)
block|{
name|ReferenceCountUtil
operator|.
name|safeRelease
argument_list|(
name|connectionHeaderWithLength
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|established
parameter_list|(
name|Channel
name|ch
parameter_list|)
throws|throws
name|IOException
block|{
name|ChannelPipeline
name|p
init|=
name|ch
operator|.
name|pipeline
argument_list|()
decl_stmt|;
name|String
name|addBeforeHandler
init|=
name|p
operator|.
name|context
argument_list|(
name|BufferCallBeforeInitHandler
operator|.
name|class
argument_list|)
operator|.
name|name
argument_list|()
decl_stmt|;
name|p
operator|.
name|addBefore
argument_list|(
name|addBeforeHandler
argument_list|,
literal|null
argument_list|,
operator|new
name|IdleStateHandler
argument_list|(
literal|0
argument_list|,
name|rpcClient
operator|.
name|minIdleTimeBeforeClose
argument_list|,
literal|0
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|addBefore
argument_list|(
name|addBeforeHandler
argument_list|,
literal|null
argument_list|,
operator|new
name|LengthFieldBasedFrameDecoder
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
literal|0
argument_list|,
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|addBefore
argument_list|(
name|addBeforeHandler
argument_list|,
literal|null
argument_list|,
operator|new
name|NettyRpcDuplexHandler
argument_list|(
name|this
argument_list|,
name|rpcClient
operator|.
name|cellBlockBuilder
argument_list|,
name|codec
argument_list|,
name|compressor
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|fireUserEventTriggered
argument_list|(
name|BufferCallEvent
operator|.
name|success
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|reloginInProgress
decl_stmt|;
specifier|private
name|void
name|scheduleRelogin
parameter_list|(
name|Throwable
name|error
parameter_list|)
block|{
if|if
condition|(
name|error
operator|instanceof
name|FallbackDisallowedException
condition|)
block|{
return|return;
block|}
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|reloginInProgress
condition|)
block|{
return|return;
block|}
name|reloginInProgress
operator|=
literal|true
expr_stmt|;
name|RELOGIN_EXECUTOR
operator|.
name|schedule
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
name|shouldAuthenticateOverKrb
argument_list|()
condition|)
block|{
name|relogin
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"relogin failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|this
init|)
block|{
name|reloginInProgress
operator|=
literal|false
expr_stmt|;
block|}
block|}
block|}
argument_list|,
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextInt
argument_list|(
name|reloginMaxBackoff
argument_list|)
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|failInit
parameter_list|(
name|Channel
name|ch
parameter_list|,
name|IOException
name|e
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
comment|// fail all pending calls
name|ch
operator|.
name|pipeline
argument_list|()
operator|.
name|fireUserEventTriggered
argument_list|(
name|BufferCallEvent
operator|.
name|fail
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|shutdown0
argument_list|()
expr_stmt|;
return|return;
block|}
block|}
specifier|private
name|void
name|saslNegotiate
parameter_list|(
specifier|final
name|Channel
name|ch
parameter_list|)
block|{
name|UserGroupInformation
name|ticket
init|=
name|getUGI
argument_list|()
decl_stmt|;
if|if
condition|(
name|ticket
operator|==
literal|null
condition|)
block|{
name|failInit
argument_list|(
name|ch
argument_list|,
operator|new
name|FatalConnectionException
argument_list|(
literal|"ticket/user is null"
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|Promise
argument_list|<
name|Boolean
argument_list|>
name|saslPromise
init|=
name|ch
operator|.
name|eventLoop
argument_list|()
operator|.
name|newPromise
argument_list|()
decl_stmt|;
specifier|final
name|NettyHBaseSaslRpcClientHandler
name|saslHandler
decl_stmt|;
try|try
block|{
name|saslHandler
operator|=
operator|new
name|NettyHBaseSaslRpcClientHandler
argument_list|(
name|saslPromise
argument_list|,
name|ticket
argument_list|,
name|authMethod
argument_list|,
name|token
argument_list|,
name|serverPrincipal
argument_list|,
name|rpcClient
operator|.
name|fallbackAllowed
argument_list|,
name|this
operator|.
name|rpcClient
operator|.
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|failInit
argument_list|(
name|ch
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
name|ch
operator|.
name|pipeline
argument_list|()
operator|.
name|addFirst
argument_list|(
operator|new
name|SaslChallengeDecoder
argument_list|()
argument_list|,
name|saslHandler
argument_list|)
expr_stmt|;
name|saslPromise
operator|.
name|addListener
argument_list|(
operator|new
name|FutureListener
argument_list|<
name|Boolean
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
name|Boolean
argument_list|>
name|future
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|future
operator|.
name|isSuccess
argument_list|()
condition|)
block|{
name|ChannelPipeline
name|p
init|=
name|ch
operator|.
name|pipeline
argument_list|()
decl_stmt|;
name|p
operator|.
name|remove
argument_list|(
name|SaslChallengeDecoder
operator|.
name|class
argument_list|)
expr_stmt|;
name|p
operator|.
name|remove
argument_list|(
name|NettyHBaseSaslRpcClientHandler
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// check if negotiate with server for connection header is necessary
if|if
condition|(
name|saslHandler
operator|.
name|isNeedProcessConnectionHeader
argument_list|()
condition|)
block|{
name|Promise
argument_list|<
name|Boolean
argument_list|>
name|connectionHeaderPromise
init|=
name|ch
operator|.
name|eventLoop
argument_list|()
operator|.
name|newPromise
argument_list|()
decl_stmt|;
comment|// create the handler to handle the connection header
name|ChannelHandler
name|chHandler
init|=
operator|new
name|NettyHBaseRpcConnectionHeaderHandler
argument_list|(
name|connectionHeaderPromise
argument_list|,
name|conf
argument_list|,
name|connectionHeaderWithLength
argument_list|)
decl_stmt|;
comment|// add ReadTimeoutHandler to deal with server doesn't response connection header
comment|// because of the different configuration in client side and server side
name|p
operator|.
name|addFirst
argument_list|(
operator|new
name|ReadTimeoutHandler
argument_list|(
name|RpcClient
operator|.
name|DEFAULT_SOCKET_TIMEOUT_READ
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|addLast
argument_list|(
name|chHandler
argument_list|)
expr_stmt|;
name|connectionHeaderPromise
operator|.
name|addListener
argument_list|(
operator|new
name|FutureListener
argument_list|<
name|Boolean
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
name|Boolean
argument_list|>
name|future
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|future
operator|.
name|isSuccess
argument_list|()
condition|)
block|{
name|ChannelPipeline
name|p
init|=
name|ch
operator|.
name|pipeline
argument_list|()
decl_stmt|;
name|p
operator|.
name|remove
argument_list|(
name|ReadTimeoutHandler
operator|.
name|class
argument_list|)
expr_stmt|;
name|p
operator|.
name|remove
argument_list|(
name|NettyHBaseRpcConnectionHeaderHandler
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// don't send connection header, NettyHbaseRpcConnectionHeaderHandler
comment|// sent it already
name|established
argument_list|(
name|ch
argument_list|)
expr_stmt|;
block|}
else|else
block|{
specifier|final
name|Throwable
name|error
init|=
name|future
operator|.
name|cause
argument_list|()
decl_stmt|;
name|scheduleRelogin
argument_list|(
name|error
argument_list|)
expr_stmt|;
name|failInit
argument_list|(
name|ch
argument_list|,
name|toIOE
argument_list|(
name|error
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// send the connection header to server
name|ch
operator|.
name|write
argument_list|(
name|connectionHeaderWithLength
operator|.
name|retainedDuplicate
argument_list|()
argument_list|)
expr_stmt|;
name|established
argument_list|(
name|ch
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
specifier|final
name|Throwable
name|error
init|=
name|future
operator|.
name|cause
argument_list|()
decl_stmt|;
name|scheduleRelogin
argument_list|(
name|error
argument_list|)
expr_stmt|;
name|failInit
argument_list|(
name|ch
argument_list|,
name|toIOE
argument_list|(
name|error
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|connect
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
literal|"Connecting to "
operator|+
name|remoteId
operator|.
name|address
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|channel
operator|=
operator|new
name|Bootstrap
argument_list|()
operator|.
name|group
argument_list|(
name|rpcClient
operator|.
name|group
argument_list|)
operator|.
name|channel
argument_list|(
name|rpcClient
operator|.
name|channelClass
argument_list|)
operator|.
name|option
argument_list|(
name|ChannelOption
operator|.
name|TCP_NODELAY
argument_list|,
name|rpcClient
operator|.
name|isTcpNoDelay
argument_list|()
argument_list|)
operator|.
name|option
argument_list|(
name|ChannelOption
operator|.
name|SO_KEEPALIVE
argument_list|,
name|rpcClient
operator|.
name|tcpKeepAlive
argument_list|)
operator|.
name|option
argument_list|(
name|ChannelOption
operator|.
name|CONNECT_TIMEOUT_MILLIS
argument_list|,
name|rpcClient
operator|.
name|connectTO
argument_list|)
operator|.
name|handler
argument_list|(
operator|new
name|BufferCallBeforeInitHandler
argument_list|()
argument_list|)
operator|.
name|localAddress
argument_list|(
name|rpcClient
operator|.
name|localAddr
argument_list|)
operator|.
name|remoteAddress
argument_list|(
name|remoteId
operator|.
name|address
argument_list|)
operator|.
name|connect
argument_list|()
operator|.
name|addListener
argument_list|(
operator|new
name|ChannelFutureListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|operationComplete
parameter_list|(
name|ChannelFuture
name|future
parameter_list|)
throws|throws
name|Exception
block|{
name|Channel
name|ch
init|=
name|future
operator|.
name|channel
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|future
operator|.
name|isSuccess
argument_list|()
condition|)
block|{
name|failInit
argument_list|(
name|ch
argument_list|,
name|toIOE
argument_list|(
name|future
operator|.
name|cause
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|rpcClient
operator|.
name|failedServers
operator|.
name|addToFailedServers
argument_list|(
name|remoteId
operator|.
name|address
argument_list|)
expr_stmt|;
return|return;
block|}
name|ch
operator|.
name|writeAndFlush
argument_list|(
name|connectionHeaderPreamble
operator|.
name|retainedDuplicate
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|useSasl
condition|)
block|{
name|saslNegotiate
argument_list|(
name|ch
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// send the connection header to server
name|ch
operator|.
name|write
argument_list|(
name|connectionHeaderWithLength
operator|.
name|retainedDuplicate
argument_list|()
argument_list|)
expr_stmt|;
name|established
argument_list|(
name|ch
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
operator|.
name|channel
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|write
parameter_list|(
name|Channel
name|ch
parameter_list|,
specifier|final
name|Call
name|call
parameter_list|)
block|{
name|ch
operator|.
name|writeAndFlush
argument_list|(
name|call
argument_list|)
operator|.
name|addListener
argument_list|(
operator|new
name|ChannelFutureListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|operationComplete
parameter_list|(
name|ChannelFuture
name|future
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Fail the call if we failed to write it out. This usually because the channel is
comment|// closed. This is needed because we may shutdown the channel inside event loop and
comment|// there may still be some pending calls in the event loop queue after us.
if|if
condition|(
operator|!
name|future
operator|.
name|isSuccess
argument_list|()
condition|)
block|{
name|call
operator|.
name|setException
argument_list|(
name|toIOE
argument_list|(
name|future
operator|.
name|cause
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|sendRequest
parameter_list|(
specifier|final
name|Call
name|call
parameter_list|,
name|HBaseRpcController
name|hrc
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|reloginInProgress
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Can not send request because relogin is in progress."
argument_list|)
throw|;
block|}
name|hrc
operator|.
name|notifyOnCancel
argument_list|(
operator|new
name|RpcCallback
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
name|Object
name|parameter
parameter_list|)
block|{
name|setCancelled
argument_list|(
name|call
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|channel
operator|!=
literal|null
condition|)
block|{
name|channel
operator|.
name|pipeline
argument_list|()
operator|.
name|fireUserEventTriggered
argument_list|(
operator|new
name|CallEvent
argument_list|(
name|CANCELLED
argument_list|,
name|call
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
argument_list|,
operator|new
name|CancellationCallback
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
name|boolean
name|cancelled
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|cancelled
condition|)
block|{
name|setCancelled
argument_list|(
name|call
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|channel
operator|==
literal|null
condition|)
block|{
name|connect
argument_list|()
expr_stmt|;
block|}
name|scheduleTimeoutTask
argument_list|(
name|call
argument_list|)
expr_stmt|;
specifier|final
name|Channel
name|ch
init|=
name|channel
decl_stmt|;
comment|// We must move the whole writeAndFlush call inside event loop otherwise there will be a
comment|// race condition.
comment|// In netty's DefaultChannelPipeline, it will find the first outbound handler in the
comment|// current thread and then schedule a task to event loop which will start the process from
comment|// that outbound handler. It is possible that the first handler is
comment|// BufferCallBeforeInitHandler when we call writeAndFlush here, but the connection is set
comment|// up at the same time so in the event loop thread we remove the
comment|// BufferCallBeforeInitHandler, and then our writeAndFlush task comes, still calls the
comment|// write method of BufferCallBeforeInitHandler.
comment|// This may be considered as a bug of netty, but anyway there is a work around so let's
comment|// fix it by ourselves first.
if|if
condition|(
name|ch
operator|.
name|eventLoop
argument_list|()
operator|.
name|inEventLoop
argument_list|()
condition|)
block|{
name|write
argument_list|(
name|ch
argument_list|,
name|call
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ch
operator|.
name|eventLoop
argument_list|()
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|write
argument_list|(
name|ch
argument_list|,
name|call
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

