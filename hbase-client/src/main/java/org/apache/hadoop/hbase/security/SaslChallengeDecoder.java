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
name|handler
operator|.
name|codec
operator|.
name|ByteToMessageDecoder
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
name|List
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
name|ipc
operator|.
name|RemoteException
import|;
end_import

begin_comment
comment|/**  * Decode the sasl challenge sent by RpcServer.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SaslChallengeDecoder
extends|extends
name|ByteToMessageDecoder
block|{
specifier|private
specifier|static
specifier|final
name|int
name|MAX_CHALLENGE_SIZE
init|=
literal|1024
operator|*
literal|1024
decl_stmt|;
comment|// 1M
specifier|private
name|ByteBuf
name|tryDecodeChallenge
parameter_list|(
name|ByteBuf
name|in
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|readableBytes
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|readableBytes
operator|<
literal|4
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|len
init|=
name|in
operator|.
name|getInt
argument_list|(
name|offset
argument_list|)
decl_stmt|;
if|if
condition|(
name|len
operator|<=
literal|0
condition|)
block|{
comment|// fall back to simple
name|in
operator|.
name|readerIndex
argument_list|(
name|offset
operator|+
literal|4
argument_list|)
expr_stmt|;
return|return
name|in
operator|.
name|retainedSlice
argument_list|(
name|offset
argument_list|,
literal|4
argument_list|)
return|;
block|}
if|if
condition|(
name|len
operator|>
name|MAX_CHALLENGE_SIZE
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Sasl challenge too large("
operator|+
name|len
operator|+
literal|"), max allowed is "
operator|+
name|MAX_CHALLENGE_SIZE
argument_list|)
throw|;
block|}
name|int
name|totalLen
init|=
literal|4
operator|+
name|len
decl_stmt|;
if|if
condition|(
name|readableBytes
operator|<
name|totalLen
condition|)
block|{
return|return
literal|null
return|;
block|}
name|in
operator|.
name|readerIndex
argument_list|(
name|offset
operator|+
name|totalLen
argument_list|)
expr_stmt|;
return|return
name|in
operator|.
name|retainedSlice
argument_list|(
name|offset
argument_list|,
name|totalLen
argument_list|)
return|;
block|}
comment|// will throw a RemoteException out if data is enough, so do not need to return anything.
specifier|private
name|void
name|tryDecodeError
parameter_list|(
name|ByteBuf
name|in
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|readableBytes
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|readableBytes
operator|<
literal|4
condition|)
block|{
return|return;
block|}
name|int
name|classLen
init|=
name|in
operator|.
name|getInt
argument_list|(
name|offset
argument_list|)
decl_stmt|;
if|if
condition|(
name|classLen
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Invalid exception class name length "
operator|+
name|classLen
argument_list|)
throw|;
block|}
if|if
condition|(
name|classLen
operator|>
name|MAX_CHALLENGE_SIZE
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Exception class name length too large("
operator|+
name|classLen
operator|+
literal|"), max allowed is "
operator|+
name|MAX_CHALLENGE_SIZE
argument_list|)
throw|;
block|}
if|if
condition|(
name|readableBytes
operator|<
literal|4
operator|+
name|classLen
operator|+
literal|4
condition|)
block|{
return|return;
block|}
name|int
name|msgLen
init|=
name|in
operator|.
name|getInt
argument_list|(
name|offset
operator|+
literal|4
operator|+
name|classLen
argument_list|)
decl_stmt|;
if|if
condition|(
name|msgLen
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Invalid exception message length "
operator|+
name|msgLen
argument_list|)
throw|;
block|}
if|if
condition|(
name|msgLen
operator|>
name|MAX_CHALLENGE_SIZE
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Exception message length too large("
operator|+
name|msgLen
operator|+
literal|"), max allowed is "
operator|+
name|MAX_CHALLENGE_SIZE
argument_list|)
throw|;
block|}
name|int
name|totalLen
init|=
name|classLen
operator|+
name|msgLen
operator|+
literal|8
decl_stmt|;
if|if
condition|(
name|readableBytes
operator|<
name|totalLen
condition|)
block|{
return|return;
block|}
name|String
name|className
init|=
name|in
operator|.
name|toString
argument_list|(
name|offset
operator|+
literal|4
argument_list|,
name|classLen
argument_list|,
name|HConstants
operator|.
name|UTF8_CHARSET
argument_list|)
decl_stmt|;
name|String
name|msg
init|=
name|in
operator|.
name|toString
argument_list|(
name|offset
operator|+
name|classLen
operator|+
literal|8
argument_list|,
name|msgLen
argument_list|,
name|HConstants
operator|.
name|UTF8_CHARSET
argument_list|)
decl_stmt|;
name|in
operator|.
name|readerIndex
argument_list|(
name|offset
operator|+
name|totalLen
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RemoteException
argument_list|(
name|className
argument_list|,
name|msg
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|decode
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|ByteBuf
name|in
parameter_list|,
name|List
argument_list|<
name|Object
argument_list|>
name|out
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|readableBytes
init|=
name|in
operator|.
name|readableBytes
argument_list|()
decl_stmt|;
if|if
condition|(
name|readableBytes
operator|<
literal|4
condition|)
block|{
return|return;
block|}
name|int
name|offset
init|=
name|in
operator|.
name|readerIndex
argument_list|()
decl_stmt|;
name|int
name|status
init|=
name|in
operator|.
name|getInt
argument_list|(
name|offset
argument_list|)
decl_stmt|;
if|if
condition|(
name|status
operator|==
name|SaslStatus
operator|.
name|SUCCESS
operator|.
name|state
condition|)
block|{
name|ByteBuf
name|challenge
init|=
name|tryDecodeChallenge
argument_list|(
name|in
argument_list|,
name|offset
operator|+
literal|4
argument_list|,
name|readableBytes
operator|-
literal|4
argument_list|)
decl_stmt|;
if|if
condition|(
name|challenge
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|add
argument_list|(
name|challenge
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|tryDecodeError
argument_list|(
name|in
argument_list|,
name|offset
operator|+
literal|4
argument_list|,
name|readableBytes
operator|-
literal|4
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

