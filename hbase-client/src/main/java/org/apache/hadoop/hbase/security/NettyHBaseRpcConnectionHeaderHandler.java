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
name|SimpleChannelInboundHandler
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
name|LengthFieldBasedFrameDecoder
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
name|Promise
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
import|;
end_import

begin_comment
comment|/**  * Implement logic to deal with the rpc connection header.  * @since 2.0.0  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|NettyHBaseRpcConnectionHeaderHandler
extends|extends
name|SimpleChannelInboundHandler
argument_list|<
name|ByteBuf
argument_list|>
block|{
specifier|private
specifier|final
name|Promise
argument_list|<
name|Boolean
argument_list|>
name|saslPromise
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|ByteBuf
name|connectionHeaderWithLength
decl_stmt|;
specifier|public
name|NettyHBaseRpcConnectionHeaderHandler
parameter_list|(
name|Promise
argument_list|<
name|Boolean
argument_list|>
name|saslPromise
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|ByteBuf
name|connectionHeaderWithLength
parameter_list|)
block|{
name|this
operator|.
name|saslPromise
operator|=
name|saslPromise
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|connectionHeaderWithLength
operator|=
name|connectionHeaderWithLength
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
comment|// read the ConnectionHeaderResponse from server
name|int
name|len
init|=
name|msg
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|byte
index|[]
name|buff
init|=
operator|new
name|byte
index|[
name|len
index|]
decl_stmt|;
name|msg
operator|.
name|readBytes
argument_list|(
name|buff
argument_list|)
expr_stmt|;
name|RPCProtos
operator|.
name|ConnectionHeaderResponse
name|connectionHeaderResponse
init|=
name|RPCProtos
operator|.
name|ConnectionHeaderResponse
operator|.
name|parseFrom
argument_list|(
name|buff
argument_list|)
decl_stmt|;
comment|// Get the CryptoCipherMeta, update the HBaseSaslRpcClient for Crypto Cipher
if|if
condition|(
name|connectionHeaderResponse
operator|.
name|hasCryptoCipherMeta
argument_list|()
condition|)
block|{
name|CryptoAES
name|cryptoAES
init|=
name|EncryptionUtil
operator|.
name|createCryptoAES
argument_list|(
name|connectionHeaderResponse
operator|.
name|getCryptoCipherMeta
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
comment|// replace the Sasl handler with Crypto AES handler
name|setupCryptoAESHandler
argument_list|(
name|ctx
operator|.
name|pipeline
argument_list|()
argument_list|,
name|cryptoAES
argument_list|)
expr_stmt|;
block|}
name|saslPromise
operator|.
name|setSuccess
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handlerAdded
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|)
block|{
try|try
block|{
comment|// send the connection header to server first
name|ctx
operator|.
name|writeAndFlush
argument_list|(
name|connectionHeaderWithLength
operator|.
name|retainedDuplicate
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// the exception thrown by handlerAdded will not be passed to the exceptionCaught below
comment|// because netty will remove a handler if handlerAdded throws an exception.
name|exceptionCaught
argument_list|(
name|ctx
argument_list|,
name|e
argument_list|)
expr_stmt|;
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
name|saslPromise
operator|.
name|tryFailure
argument_list|(
name|cause
argument_list|)
expr_stmt|;
block|}
comment|/**    * Remove handlers for sasl encryption and add handlers for Crypto AES encryption    */
specifier|private
name|void
name|setupCryptoAESHandler
parameter_list|(
name|ChannelPipeline
name|p
parameter_list|,
name|CryptoAES
name|cryptoAES
parameter_list|)
block|{
name|p
operator|.
name|remove
argument_list|(
name|SaslWrapHandler
operator|.
name|class
argument_list|)
expr_stmt|;
name|p
operator|.
name|remove
argument_list|(
name|SaslUnwrapHandler
operator|.
name|class
argument_list|)
expr_stmt|;
name|String
name|lengthDecoder
init|=
name|p
operator|.
name|context
argument_list|(
name|LengthFieldBasedFrameDecoder
operator|.
name|class
argument_list|)
operator|.
name|name
argument_list|()
decl_stmt|;
name|p
operator|.
name|addAfter
argument_list|(
name|lengthDecoder
argument_list|,
literal|null
argument_list|,
operator|new
name|CryptoAESUnwrapHandler
argument_list|(
name|cryptoAES
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|addAfter
argument_list|(
name|lengthDecoder
argument_list|,
literal|null
argument_list|,
operator|new
name|CryptoAESWrapHandler
argument_list|(
name|cryptoAES
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

