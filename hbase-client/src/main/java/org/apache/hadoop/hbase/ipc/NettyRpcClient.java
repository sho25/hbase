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
name|concurrent
operator|.
name|DefaultThreadFactory
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
name|SocketAddress
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
name|util
operator|.
name|Pair
import|;
end_import

begin_comment
comment|/**  * Netty client for the requests and responses.  */
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
name|NettyRpcClient
extends|extends
name|AbstractRpcClient
argument_list|<
name|NettyRpcConnection
argument_list|>
block|{
specifier|final
name|EventLoopGroup
name|group
decl_stmt|;
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|Channel
argument_list|>
name|channelClass
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|shutdownGroupWhenClose
decl_stmt|;
specifier|public
name|NettyRpcClient
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
name|MetricsConnection
name|metrics
parameter_list|)
block|{
name|super
argument_list|(
name|configuration
argument_list|,
name|clusterId
argument_list|,
name|localAddress
argument_list|,
name|metrics
argument_list|)
expr_stmt|;
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
name|groupAndChannelClass
init|=
name|NettyRpcClientConfigHelper
operator|.
name|getEventLoopConfig
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|groupAndChannelClass
operator|==
literal|null
condition|)
block|{
comment|// Use our own EventLoopGroup.
name|this
operator|.
name|group
operator|=
operator|new
name|NioEventLoopGroup
argument_list|(
literal|0
argument_list|,
operator|new
name|DefaultThreadFactory
argument_list|(
literal|"IPC-NioEventLoopGroup"
argument_list|,
literal|true
argument_list|,
name|Thread
operator|.
name|MAX_PRIORITY
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|channelClass
operator|=
name|NioSocketChannel
operator|.
name|class
expr_stmt|;
name|this
operator|.
name|shutdownGroupWhenClose
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|group
operator|=
name|groupAndChannelClass
operator|.
name|getFirst
argument_list|()
expr_stmt|;
name|this
operator|.
name|channelClass
operator|=
name|groupAndChannelClass
operator|.
name|getSecond
argument_list|()
expr_stmt|;
name|this
operator|.
name|shutdownGroupWhenClose
operator|=
literal|false
expr_stmt|;
block|}
block|}
comment|/** Used in test only. */
name|NettyRpcClient
parameter_list|(
name|Configuration
name|configuration
parameter_list|)
block|{
name|this
argument_list|(
name|configuration
argument_list|,
name|HConstants
operator|.
name|CLUSTER_ID_DEFAULT
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|NettyRpcConnection
name|createConnection
parameter_list|(
name|ConnectionId
name|remoteId
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|NettyRpcConnection
argument_list|(
name|this
argument_list|,
name|remoteId
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|closeInternal
parameter_list|()
block|{
if|if
condition|(
name|shutdownGroupWhenClose
condition|)
block|{
name|group
operator|.
name|shutdownGracefully
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

