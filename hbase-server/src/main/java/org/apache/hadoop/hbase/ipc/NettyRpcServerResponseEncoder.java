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
name|Unpooled
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
name|ChannelOutboundHandlerAdapter
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
name|ChannelPromise
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

begin_comment
comment|/**  * Encoder for {@link RpcResponse}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|NettyRpcServerResponseEncoder
extends|extends
name|ChannelOutboundHandlerAdapter
block|{
specifier|private
specifier|final
name|MetricsHBaseServer
name|metrics
decl_stmt|;
name|NettyRpcServerResponseEncoder
parameter_list|(
name|MetricsHBaseServer
name|metrics
parameter_list|)
block|{
name|this
operator|.
name|metrics
operator|=
name|metrics
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|Object
name|msg
parameter_list|,
name|ChannelPromise
name|promise
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|msg
operator|instanceof
name|RpcResponse
condition|)
block|{
name|RpcResponse
name|resp
init|=
operator|(
name|RpcResponse
operator|)
name|msg
decl_stmt|;
name|BufferChain
name|buf
init|=
name|resp
operator|.
name|getResponse
argument_list|()
decl_stmt|;
name|ctx
operator|.
name|write
argument_list|(
name|Unpooled
operator|.
name|wrappedBuffer
argument_list|(
name|buf
operator|.
name|getBuffers
argument_list|()
argument_list|)
argument_list|,
name|promise
argument_list|)
operator|.
name|addListener
argument_list|(
name|f
lambda|->
block|{
name|resp
operator|.
name|done
argument_list|()
expr_stmt|;
if|if
condition|(
name|f
operator|.
name|isSuccess
argument_list|()
condition|)
block|{
name|metrics
operator|.
name|sentBytes
argument_list|(
name|buf
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ctx
operator|.
name|write
argument_list|(
name|msg
argument_list|,
name|promise
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

