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
operator|.
name|example
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
name|net
operator|.
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
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
name|HBaseConfiguration
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
name|TableName
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
name|AsyncConnection
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
name|ConnectionFactory
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
name|Get
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
name|Put
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
name|NettyRpcClientConfigHelper
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
name|Bytes
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
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|common
operator|.
name|base
operator|.
name|Throwables
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelHandlerContext
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|SimpleChannelInboundHandler
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
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
name|NioSocketChannel
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
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|DefaultFullHttpResponse
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
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|FullHttpRequest
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
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|HttpHeaderNames
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
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|HttpObjectAggregator
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
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|HttpResponseStatus
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
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|HttpServerCodec
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
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|HttpVersion
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
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|QueryStringDecoder
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
comment|/**  * A simple example on how to use {@link org.apache.hadoop.hbase.client.AsyncTable} to write a fully  * asynchronous HTTP proxy server. The {@link AsyncConnection} will share the same event loop with  * the HTTP server.  *<p>  * The request URL is:  *  *<pre>  * http://&lt;host&gt;:&lt;port&gt;/&lt;table&gt;/&lt;rowgt;/&lt;family&gt;:&lt;qualifier&gt;  *</pre>  *  * Use HTTP GET to fetch data, and use HTTP PUT to put data. Encode the value as the request content  * when doing PUT.  */
end_comment

begin_class
specifier|public
class|class
name|HttpProxyExample
block|{
specifier|private
specifier|final
name|EventLoopGroup
name|bossGroup
init|=
operator|new
name|NioEventLoopGroup
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|EventLoopGroup
name|workerGroup
init|=
operator|new
name|NioEventLoopGroup
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|int
name|port
decl_stmt|;
specifier|private
name|AsyncConnection
name|conn
decl_stmt|;
specifier|private
name|Channel
name|serverChannel
decl_stmt|;
specifier|private
name|ChannelGroup
name|channelGroup
decl_stmt|;
specifier|public
name|HttpProxyExample
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|int
name|port
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|port
operator|=
name|port
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
class|class
name|Params
block|{
specifier|public
specifier|final
name|String
name|table
decl_stmt|;
specifier|public
specifier|final
name|String
name|row
decl_stmt|;
specifier|public
specifier|final
name|String
name|family
decl_stmt|;
specifier|public
specifier|final
name|String
name|qualifier
decl_stmt|;
specifier|public
name|Params
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|row
parameter_list|,
name|String
name|family
parameter_list|,
name|String
name|qualifier
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
name|this
operator|.
name|family
operator|=
name|family
expr_stmt|;
name|this
operator|.
name|qualifier
operator|=
name|qualifier
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
specifier|final
class|class
name|RequestHandler
extends|extends
name|SimpleChannelInboundHandler
argument_list|<
name|FullHttpRequest
argument_list|>
block|{
specifier|private
specifier|final
name|AsyncConnection
name|conn
decl_stmt|;
specifier|private
specifier|final
name|ChannelGroup
name|channelGroup
decl_stmt|;
specifier|public
name|RequestHandler
parameter_list|(
name|AsyncConnection
name|conn
parameter_list|,
name|ChannelGroup
name|channelGroup
parameter_list|)
block|{
name|this
operator|.
name|conn
operator|=
name|conn
expr_stmt|;
name|this
operator|.
name|channelGroup
operator|=
name|channelGroup
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|channelActive
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|)
block|{
name|channelGroup
operator|.
name|add
argument_list|(
name|ctx
operator|.
name|channel
argument_list|()
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|fireChannelActive
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|channelInactive
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|)
block|{
name|channelGroup
operator|.
name|remove
argument_list|(
name|ctx
operator|.
name|channel
argument_list|()
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|fireChannelInactive
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|write
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|HttpResponseStatus
name|status
parameter_list|,
name|Optional
argument_list|<
name|String
argument_list|>
name|content
parameter_list|)
block|{
name|DefaultFullHttpResponse
name|resp
decl_stmt|;
if|if
condition|(
name|content
operator|.
name|isPresent
argument_list|()
condition|)
block|{
name|ByteBuf
name|buf
init|=
name|ctx
operator|.
name|alloc
argument_list|()
operator|.
name|buffer
argument_list|()
operator|.
name|writeBytes
argument_list|(
name|content
operator|.
name|get
argument_list|()
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
decl_stmt|;
name|resp
operator|=
operator|new
name|DefaultFullHttpResponse
argument_list|(
name|HttpVersion
operator|.
name|HTTP_1_1
argument_list|,
name|status
argument_list|,
name|buf
argument_list|)
expr_stmt|;
name|resp
operator|.
name|headers
argument_list|()
operator|.
name|set
argument_list|(
name|HttpHeaderNames
operator|.
name|CONTENT_LENGTH
argument_list|,
name|buf
operator|.
name|readableBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|resp
operator|=
operator|new
name|DefaultFullHttpResponse
argument_list|(
name|HttpVersion
operator|.
name|HTTP_1_1
argument_list|,
name|status
argument_list|)
expr_stmt|;
block|}
name|resp
operator|.
name|headers
argument_list|()
operator|.
name|set
argument_list|(
name|HttpHeaderNames
operator|.
name|CONTENT_TYPE
argument_list|,
literal|"text-plain; charset=UTF-8"
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|writeAndFlush
argument_list|(
name|resp
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Params
name|parse
parameter_list|(
name|FullHttpRequest
name|req
parameter_list|)
block|{
name|String
index|[]
name|components
init|=
operator|new
name|QueryStringDecoder
argument_list|(
name|req
operator|.
name|uri
argument_list|()
argument_list|)
operator|.
name|path
argument_list|()
operator|.
name|split
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|components
operator|.
name|length
operator|==
literal|4
argument_list|,
literal|"Unrecognized uri: %s"
argument_list|,
name|req
operator|.
name|uri
argument_list|()
argument_list|)
expr_stmt|;
comment|// path is start with '/' so split will give an empty component
name|String
index|[]
name|cfAndCq
init|=
name|components
index|[
literal|3
index|]
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|cfAndCq
operator|.
name|length
operator|==
literal|2
argument_list|,
literal|"Unrecognized uri: %s"
argument_list|,
name|req
operator|.
name|uri
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|Params
argument_list|(
name|components
index|[
literal|1
index|]
argument_list|,
name|components
index|[
literal|2
index|]
argument_list|,
name|cfAndCq
index|[
literal|0
index|]
argument_list|,
name|cfAndCq
index|[
literal|1
index|]
argument_list|)
return|;
block|}
specifier|private
name|void
name|get
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|FullHttpRequest
name|req
parameter_list|)
block|{
name|Params
name|params
init|=
name|parse
argument_list|(
name|req
argument_list|)
decl_stmt|;
name|conn
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|params
operator|.
name|table
argument_list|)
argument_list|)
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|params
operator|.
name|row
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|params
operator|.
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|params
operator|.
name|qualifier
argument_list|)
argument_list|)
argument_list|)
operator|.
name|whenComplete
argument_list|(
parameter_list|(
name|r
parameter_list|,
name|e
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|e
operator|!=
literal|null
condition|)
block|{
name|exceptionCaught
argument_list|(
name|ctx
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|byte
index|[]
name|value
init|=
name|r
operator|.
name|getValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|params
operator|.
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|params
operator|.
name|qualifier
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|write
argument_list|(
name|ctx
argument_list|,
name|HttpResponseStatus
operator|.
name|OK
argument_list|,
name|Optional
operator|.
name|of
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|write
argument_list|(
name|ctx
argument_list|,
name|HttpResponseStatus
operator|.
name|NOT_FOUND
argument_list|,
name|Optional
operator|.
name|empty
argument_list|()
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
name|put
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|FullHttpRequest
name|req
parameter_list|)
block|{
name|Params
name|params
init|=
name|parse
argument_list|(
name|req
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
name|req
operator|.
name|content
argument_list|()
operator|.
name|readableBytes
argument_list|()
index|]
decl_stmt|;
name|req
operator|.
name|content
argument_list|()
operator|.
name|readBytes
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|conn
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|params
operator|.
name|table
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|params
operator|.
name|row
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|params
operator|.
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|params
operator|.
name|qualifier
argument_list|)
argument_list|,
name|value
argument_list|)
argument_list|)
operator|.
name|whenComplete
argument_list|(
parameter_list|(
name|r
parameter_list|,
name|e
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|e
operator|!=
literal|null
condition|)
block|{
name|exceptionCaught
argument_list|(
name|ctx
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|write
argument_list|(
name|ctx
argument_list|,
name|HttpResponseStatus
operator|.
name|OK
argument_list|,
name|Optional
operator|.
name|empty
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|channelRead0
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|FullHttpRequest
name|req
parameter_list|)
block|{
switch|switch
condition|(
name|req
operator|.
name|method
argument_list|()
operator|.
name|name
argument_list|()
condition|)
block|{
case|case
literal|"GET"
case|:
name|get
argument_list|(
name|ctx
argument_list|,
name|req
argument_list|)
expr_stmt|;
break|break;
case|case
literal|"PUT"
case|:
name|put
argument_list|(
name|ctx
argument_list|,
name|req
argument_list|)
expr_stmt|;
break|break;
default|default:
name|write
argument_list|(
name|ctx
argument_list|,
name|HttpResponseStatus
operator|.
name|METHOD_NOT_ALLOWED
argument_list|,
name|Optional
operator|.
name|empty
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|exceptionCaught
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
if|if
condition|(
name|cause
operator|instanceof
name|IllegalArgumentException
condition|)
block|{
name|write
argument_list|(
name|ctx
argument_list|,
name|HttpResponseStatus
operator|.
name|BAD_REQUEST
argument_list|,
name|Optional
operator|.
name|of
argument_list|(
name|cause
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|write
argument_list|(
name|ctx
argument_list|,
name|HttpResponseStatus
operator|.
name|INTERNAL_SERVER_ERROR
argument_list|,
name|Optional
operator|.
name|of
argument_list|(
name|Throwables
operator|.
name|getStackTraceAsString
argument_list|(
name|cause
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|NettyRpcClientConfigHelper
operator|.
name|setEventLoopConfig
argument_list|(
name|conf
argument_list|,
name|workerGroup
argument_list|,
name|NioSocketChannel
operator|.
name|class
argument_list|)
expr_stmt|;
name|conn
operator|=
name|ConnectionFactory
operator|.
name|createAsyncConnection
argument_list|(
name|conf
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|channelGroup
operator|=
operator|new
name|DefaultChannelGroup
argument_list|(
name|GlobalEventExecutor
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
name|serverChannel
operator|=
operator|new
name|ServerBootstrap
argument_list|()
operator|.
name|group
argument_list|(
name|bossGroup
argument_list|,
name|workerGroup
argument_list|)
operator|.
name|channel
argument_list|(
name|NioServerSocketChannel
operator|.
name|class
argument_list|)
operator|.
name|childOption
argument_list|(
name|ChannelOption
operator|.
name|TCP_NODELAY
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
name|ch
operator|.
name|pipeline
argument_list|()
operator|.
name|addFirst
argument_list|(
operator|new
name|HttpServerCodec
argument_list|()
argument_list|,
operator|new
name|HttpObjectAggregator
argument_list|(
literal|4
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
argument_list|,
operator|new
name|RequestHandler
argument_list|(
name|conn
argument_list|,
name|channelGroup
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|bind
argument_list|(
name|port
argument_list|)
operator|.
name|syncUninterruptibly
argument_list|()
operator|.
name|channel
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|join
parameter_list|()
block|{
name|serverChannel
operator|.
name|closeFuture
argument_list|()
operator|.
name|awaitUninterruptibly
argument_list|()
expr_stmt|;
block|}
specifier|public
name|int
name|port
parameter_list|()
block|{
if|if
condition|(
name|serverChannel
operator|==
literal|null
condition|)
block|{
return|return
name|port
return|;
block|}
else|else
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
operator|.
name|getPort
argument_list|()
return|;
block|}
block|}
specifier|public
name|void
name|stop
parameter_list|()
throws|throws
name|IOException
block|{
name|serverChannel
operator|.
name|close
argument_list|()
operator|.
name|syncUninterruptibly
argument_list|()
expr_stmt|;
name|serverChannel
operator|=
literal|null
expr_stmt|;
name|channelGroup
operator|.
name|close
argument_list|()
operator|.
name|syncUninterruptibly
argument_list|()
expr_stmt|;
name|channelGroup
operator|=
literal|null
expr_stmt|;
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
name|conn
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|int
name|port
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|HttpProxyExample
name|proxy
init|=
operator|new
name|HttpProxyExample
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|,
name|port
argument_list|)
decl_stmt|;
name|proxy
operator|.
name|start
argument_list|()
expr_stmt|;
name|proxy
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

