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
name|shaded
operator|.
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|CoalescingBufferQueue
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
name|ReferenceCountUtil
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
name|PromiseCombiner
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * wrap messages with Crypto AES.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CryptoAESWrapHandler
extends|extends
name|ChannelOutboundHandlerAdapter
block|{
specifier|private
specifier|final
name|CryptoAES
name|cryptoAES
decl_stmt|;
specifier|private
name|CoalescingBufferQueue
name|queue
decl_stmt|;
specifier|public
name|CryptoAESWrapHandler
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
specifier|public
name|void
name|handlerAdded
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|)
throws|throws
name|Exception
block|{
name|queue
operator|=
operator|new
name|CoalescingBufferQueue
argument_list|(
name|ctx
operator|.
name|channel
argument_list|()
argument_list|)
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
name|ByteBuf
condition|)
block|{
name|queue
operator|.
name|add
argument_list|(
operator|(
name|ByteBuf
operator|)
name|msg
argument_list|,
name|promise
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
annotation|@
name|Override
specifier|public
name|void
name|flush
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|queue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
name|ByteBuf
name|buf
init|=
literal|null
decl_stmt|;
try|try
block|{
name|ChannelPromise
name|promise
init|=
name|ctx
operator|.
name|newPromise
argument_list|()
decl_stmt|;
name|int
name|readableBytes
init|=
name|queue
operator|.
name|readableBytes
argument_list|()
decl_stmt|;
name|buf
operator|=
name|queue
operator|.
name|remove
argument_list|(
name|readableBytes
argument_list|,
name|promise
argument_list|)
expr_stmt|;
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
name|readableBytes
index|]
decl_stmt|;
name|buf
operator|.
name|readBytes
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
name|byte
index|[]
name|wrapperBytes
init|=
name|cryptoAES
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|length
argument_list|)
decl_stmt|;
name|ChannelPromise
name|lenPromise
init|=
name|ctx
operator|.
name|newPromise
argument_list|()
decl_stmt|;
name|ctx
operator|.
name|write
argument_list|(
name|ctx
operator|.
name|alloc
argument_list|()
operator|.
name|buffer
argument_list|(
literal|4
argument_list|)
operator|.
name|writeInt
argument_list|(
name|wrapperBytes
operator|.
name|length
argument_list|)
argument_list|,
name|lenPromise
argument_list|)
expr_stmt|;
name|ChannelPromise
name|contentPromise
init|=
name|ctx
operator|.
name|newPromise
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
name|wrapperBytes
argument_list|)
argument_list|,
name|contentPromise
argument_list|)
expr_stmt|;
name|PromiseCombiner
name|combiner
init|=
operator|new
name|PromiseCombiner
argument_list|()
decl_stmt|;
name|combiner
operator|.
name|addAll
argument_list|(
name|lenPromise
argument_list|,
name|contentPromise
argument_list|)
expr_stmt|;
name|combiner
operator|.
name|finish
argument_list|(
name|promise
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|buf
operator|!=
literal|null
condition|)
block|{
name|ReferenceCountUtil
operator|.
name|safeRelease
argument_list|(
name|buf
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|ChannelPromise
name|promise
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|queue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|queue
operator|.
name|releaseAndFailAll
argument_list|(
operator|new
name|IOException
argument_list|(
literal|"Connection closed"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|close
argument_list|(
name|promise
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

