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
name|channel
operator|.
name|ChannelHandlerContext
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
name|SimpleChannelInboundHandler
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
name|security
operator|.
name|PrivilegedExceptionAction
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
name|FallbackDisallowedException
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
name|token
operator|.
name|Token
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
name|token
operator|.
name|TokenIdentifier
import|;
end_import

begin_comment
comment|/**  * Implement SASL logic for netty rpc client.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|NettyHBaseSaslRpcClientHandler
extends|extends
name|SimpleChannelInboundHandler
argument_list|<
name|ByteBuf
argument_list|>
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
name|NettyHBaseSaslRpcClientHandler
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|UserGroupInformation
name|ugi
decl_stmt|;
specifier|private
specifier|final
name|NettyHBaseSaslRpcClient
name|saslRpcClient
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
comment|// flag to mark if Crypto AES encryption is enable
specifier|private
name|boolean
name|needProcessConnectionHeader
init|=
literal|false
decl_stmt|;
comment|/**    * @param saslPromise {@code true} if success, {@code false} if server tells us to fallback to    *          simple.    */
specifier|public
name|NettyHBaseSaslRpcClientHandler
parameter_list|(
name|Promise
argument_list|<
name|Boolean
argument_list|>
name|saslPromise
parameter_list|,
name|UserGroupInformation
name|ugi
parameter_list|,
name|AuthMethod
name|method
parameter_list|,
name|Token
argument_list|<
name|?
extends|extends
name|TokenIdentifier
argument_list|>
name|token
parameter_list|,
name|String
name|serverPrincipal
parameter_list|,
name|boolean
name|fallbackAllowed
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|saslPromise
operator|=
name|saslPromise
expr_stmt|;
name|this
operator|.
name|ugi
operator|=
name|ugi
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|saslRpcClient
operator|=
operator|new
name|NettyHBaseSaslRpcClient
argument_list|(
name|method
argument_list|,
name|token
argument_list|,
name|serverPrincipal
argument_list|,
name|fallbackAllowed
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.rpc.protection"
argument_list|,
name|SaslUtil
operator|.
name|QualityOfProtection
operator|.
name|AUTHENTICATION
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|writeResponse
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|byte
index|[]
name|response
parameter_list|)
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
literal|"Will send token of size "
operator|+
name|response
operator|.
name|length
operator|+
literal|" from initSASLContext."
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|writeAndFlush
argument_list|(
name|ctx
operator|.
name|alloc
argument_list|()
operator|.
name|buffer
argument_list|(
literal|4
operator|+
name|response
operator|.
name|length
argument_list|)
operator|.
name|writeInt
argument_list|(
name|response
operator|.
name|length
argument_list|)
operator|.
name|writeBytes
argument_list|(
name|response
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|tryComplete
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|)
block|{
if|if
condition|(
operator|!
name|saslRpcClient
operator|.
name|isComplete
argument_list|()
condition|)
block|{
return|return;
block|}
name|saslRpcClient
operator|.
name|setupSaslHandler
argument_list|(
name|ctx
operator|.
name|pipeline
argument_list|()
argument_list|)
expr_stmt|;
name|setCryptoAESOption
argument_list|()
expr_stmt|;
name|saslPromise
operator|.
name|setSuccess
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setCryptoAESOption
parameter_list|()
block|{
name|boolean
name|saslEncryptionEnabled
init|=
name|SaslUtil
operator|.
name|QualityOfProtection
operator|.
name|PRIVACY
operator|.
name|getSaslQop
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|saslRpcClient
operator|.
name|getSaslQOP
argument_list|()
argument_list|)
decl_stmt|;
name|needProcessConnectionHeader
operator|=
name|saslEncryptionEnabled
operator|&&
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"hbase.rpc.crypto.encryption.aes.enabled"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isNeedProcessConnectionHeader
parameter_list|()
block|{
return|return
name|needProcessConnectionHeader
return|;
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
name|byte
index|[]
name|initialResponse
init|=
name|ugi
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|run
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|saslRpcClient
operator|.
name|getInitialResponse
argument_list|()
return|;
block|}
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
name|initialResponse
operator|!=
literal|null
condition|)
block|{
name|writeResponse
argument_list|(
name|ctx
argument_list|,
name|initialResponse
argument_list|)
expr_stmt|;
block|}
name|tryComplete
argument_list|(
name|ctx
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
name|int
name|len
init|=
name|msg
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|len
operator|==
name|SaslUtil
operator|.
name|SWITCH_TO_SIMPLE_AUTH
condition|)
block|{
name|saslRpcClient
operator|.
name|dispose
argument_list|()
expr_stmt|;
if|if
condition|(
name|saslRpcClient
operator|.
name|fallbackAllowed
condition|)
block|{
name|saslPromise
operator|.
name|trySuccess
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|saslPromise
operator|.
name|tryFailure
argument_list|(
operator|new
name|FallbackDisallowedException
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return;
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
literal|"Will read input token of size "
operator|+
name|len
operator|+
literal|" for processing by initSASLContext"
argument_list|)
expr_stmt|;
block|}
specifier|final
name|byte
index|[]
name|challenge
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
name|challenge
argument_list|)
expr_stmt|;
name|byte
index|[]
name|response
init|=
name|ugi
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|run
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|saslRpcClient
operator|.
name|evaluateChallenge
argument_list|(
name|challenge
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
name|response
operator|!=
literal|null
condition|)
block|{
name|writeResponse
argument_list|(
name|ctx
argument_list|,
name|response
argument_list|)
expr_stmt|;
block|}
name|tryComplete
argument_list|(
name|ctx
argument_list|)
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
throws|throws
name|Exception
block|{
name|saslRpcClient
operator|.
name|dispose
argument_list|()
expr_stmt|;
name|saslPromise
operator|.
name|tryFailure
argument_list|(
operator|new
name|IOException
argument_list|(
literal|"Connection closed"
argument_list|)
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|fireChannelInactive
argument_list|()
expr_stmt|;
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
name|saslRpcClient
operator|.
name|dispose
argument_list|()
expr_stmt|;
name|saslPromise
operator|.
name|tryFailure
argument_list|(
name|cause
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

