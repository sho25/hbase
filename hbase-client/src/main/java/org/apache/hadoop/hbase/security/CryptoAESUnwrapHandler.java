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
name|security
package|;
end_package

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
name|hbase
operator|.
name|thirdparty
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
name|ChannelHandlerContext
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
name|SimpleChannelInboundHandler
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
name|io
operator|.
name|crypto
operator|.
name|aes
operator|.
name|CryptoAES
import|;
end_import

begin_comment
comment|/**  * Unwrap messages with Crypto AES. Should be placed after a  * io.netty.handler.codec.LengthFieldBasedFrameDecoder  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CryptoAESUnwrapHandler
extends|extends
name|SimpleChannelInboundHandler
argument_list|<
name|ByteBuf
argument_list|>
block|{
specifier|private
specifier|final
name|CryptoAES
name|cryptoAES
decl_stmt|;
specifier|public
name|CryptoAESUnwrapHandler
parameter_list|(
name|CryptoAES
name|cryptoAES
parameter_list|)
block|{
name|this
operator|.
name|cryptoAES
operator|=
name|cryptoAES
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
name|ByteBuf
name|msg
parameter_list|)
throws|throws
name|Exception
block|{
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
name|msg
operator|.
name|readableBytes
argument_list|()
index|]
decl_stmt|;
name|msg
operator|.
name|readBytes
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|fireChannelRead
argument_list|(
name|Unpooled
operator|.
name|wrappedBuffer
argument_list|(
name|cryptoAES
operator|.
name|unwrap
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|length
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

