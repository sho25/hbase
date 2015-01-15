begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ChannelDuplexHandler
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
name|ChannelPromise
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
name|ipc
operator|.
name|RemoteException
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

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|callback
operator|.
name|CallbackHandler
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|Sasl
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|SaslClient
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|SaslException
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
name|nio
operator|.
name|charset
operator|.
name|Charset
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_comment
comment|/**  * Handles Sasl connections  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SaslClientHandler
extends|extends
name|ChannelDuplexHandler
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SaslClientHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|fallbackAllowed
decl_stmt|;
comment|/**    * Used for client or server's token to send or receive from each other.    */
specifier|private
specifier|final
name|SaslClient
name|saslClient
decl_stmt|;
specifier|private
specifier|final
name|SaslExceptionHandler
name|exceptionHandler
decl_stmt|;
specifier|private
specifier|final
name|SaslSuccessfulConnectHandler
name|successfulConnectHandler
decl_stmt|;
specifier|private
name|byte
index|[]
name|saslToken
decl_stmt|;
specifier|private
name|boolean
name|firstRead
init|=
literal|true
decl_stmt|;
specifier|private
name|int
name|retryCount
init|=
literal|0
decl_stmt|;
specifier|private
name|Random
name|random
decl_stmt|;
comment|/**    * Constructor    *    * @param method                   auth method    * @param token                    for Sasl    * @param serverPrincipal          Server's Kerberos principal name    * @param fallbackAllowed          True if server may also fall back to less secure connection    * @param rpcProtection            Quality of protection. Integrity or privacy    * @param exceptionHandler         handler for exceptions    * @param successfulConnectHandler handler for succesful connects    * @throws java.io.IOException if handler could not be created    */
specifier|public
name|SaslClientHandler
parameter_list|(
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
name|String
name|rpcProtection
parameter_list|,
name|SaslExceptionHandler
name|exceptionHandler
parameter_list|,
name|SaslSuccessfulConnectHandler
name|successfulConnectHandler
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|fallbackAllowed
operator|=
name|fallbackAllowed
expr_stmt|;
name|this
operator|.
name|exceptionHandler
operator|=
name|exceptionHandler
expr_stmt|;
name|this
operator|.
name|successfulConnectHandler
operator|=
name|successfulConnectHandler
expr_stmt|;
name|SaslUtil
operator|.
name|initSaslProperties
argument_list|(
name|rpcProtection
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|method
condition|)
block|{
case|case
name|DIGEST
case|:
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating SASL "
operator|+
name|AuthMethod
operator|.
name|DIGEST
operator|.
name|getMechanismName
argument_list|()
operator|+
literal|" client to authenticate to service at "
operator|+
name|token
operator|.
name|getService
argument_list|()
argument_list|)
expr_stmt|;
name|saslClient
operator|=
name|createDigestSaslClient
argument_list|(
operator|new
name|String
index|[]
block|{
name|AuthMethod
operator|.
name|DIGEST
operator|.
name|getMechanismName
argument_list|()
block|}
argument_list|,
name|SaslUtil
operator|.
name|SASL_DEFAULT_REALM
argument_list|,
operator|new
name|HBaseSaslRpcClient
operator|.
name|SaslClientCallbackHandler
argument_list|(
name|token
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|KERBEROS
case|:
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
literal|"Creating SASL "
operator|+
name|AuthMethod
operator|.
name|KERBEROS
operator|.
name|getMechanismName
argument_list|()
operator|+
literal|" client. Server's Kerberos principal name is "
operator|+
name|serverPrincipal
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|serverPrincipal
operator|==
literal|null
operator|||
name|serverPrincipal
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to specify server's Kerberos principal name"
argument_list|)
throw|;
block|}
name|String
index|[]
name|names
init|=
name|SaslUtil
operator|.
name|splitKerberosName
argument_list|(
name|serverPrincipal
argument_list|)
decl_stmt|;
if|if
condition|(
name|names
operator|.
name|length
operator|!=
literal|3
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Kerberos principal does not have the expected format: "
operator|+
name|serverPrincipal
argument_list|)
throw|;
block|}
name|saslClient
operator|=
name|createKerberosSaslClient
argument_list|(
operator|new
name|String
index|[]
block|{
name|AuthMethod
operator|.
name|KERBEROS
operator|.
name|getMechanismName
argument_list|()
block|}
argument_list|,
name|names
index|[
literal|0
index|]
argument_list|,
name|names
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unknown authentication method "
operator|+
name|method
argument_list|)
throw|;
block|}
if|if
condition|(
name|saslClient
operator|==
literal|null
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to find SASL client implementation"
argument_list|)
throw|;
block|}
comment|/**    * Create a Digest Sasl client    *    * @param mechanismNames            names of mechanisms    * @param saslDefaultRealm          default realm for sasl    * @param saslClientCallbackHandler handler for the client    * @return new SaslClient    * @throws java.io.IOException if creation went wrong    */
specifier|protected
name|SaslClient
name|createDigestSaslClient
parameter_list|(
name|String
index|[]
name|mechanismNames
parameter_list|,
name|String
name|saslDefaultRealm
parameter_list|,
name|CallbackHandler
name|saslClientCallbackHandler
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|Sasl
operator|.
name|createSaslClient
argument_list|(
name|mechanismNames
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|saslDefaultRealm
argument_list|,
name|SaslUtil
operator|.
name|SASL_PROPS
argument_list|,
name|saslClientCallbackHandler
argument_list|)
return|;
block|}
comment|/**    * Create Kerberos client    *    * @param mechanismNames names of mechanisms    * @param userFirstPart  first part of username    * @param userSecondPart second part of username    * @return new SaslClient    * @throws java.io.IOException if fails    */
specifier|protected
name|SaslClient
name|createKerberosSaslClient
parameter_list|(
name|String
index|[]
name|mechanismNames
parameter_list|,
name|String
name|userFirstPart
parameter_list|,
name|String
name|userSecondPart
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|Sasl
operator|.
name|createSaslClient
argument_list|(
name|mechanismNames
argument_list|,
literal|null
argument_list|,
name|userFirstPart
argument_list|,
name|userSecondPart
argument_list|,
name|SaslUtil
operator|.
name|SASL_PROPS
argument_list|,
literal|null
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|channelUnregistered
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|)
throws|throws
name|Exception
block|{
name|saslClient
operator|.
name|dispose
argument_list|()
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
name|this
operator|.
name|saslToken
operator|=
operator|new
name|byte
index|[
literal|0
index|]
expr_stmt|;
if|if
condition|(
name|saslClient
operator|.
name|hasInitialResponse
argument_list|()
condition|)
block|{
name|saslToken
operator|=
name|saslClient
operator|.
name|evaluateChallenge
argument_list|(
name|saslToken
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|saslToken
operator|!=
literal|null
condition|)
block|{
name|writeSaslToken
argument_list|(
name|ctx
argument_list|,
name|saslToken
argument_list|)
expr_stmt|;
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
literal|"Have sent token of size "
operator|+
name|saslToken
operator|.
name|length
operator|+
literal|" from initSASLContext."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|channelRead
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|Object
name|msg
parameter_list|)
throws|throws
name|Exception
block|{
name|ByteBuf
name|in
init|=
operator|(
name|ByteBuf
operator|)
name|msg
decl_stmt|;
comment|// If not complete, try to negotiate
if|if
condition|(
operator|!
name|saslClient
operator|.
name|isComplete
argument_list|()
condition|)
block|{
while|while
condition|(
operator|!
name|saslClient
operator|.
name|isComplete
argument_list|()
operator|&&
name|in
operator|.
name|isReadable
argument_list|()
condition|)
block|{
name|readStatus
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|int
name|len
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|firstRead
condition|)
block|{
name|firstRead
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|len
operator|==
name|SaslUtil
operator|.
name|SWITCH_TO_SIMPLE_AUTH
condition|)
block|{
if|if
condition|(
operator|!
name|fallbackAllowed
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Server asks us to fall back to SIMPLE auth, "
operator|+
literal|"but this "
operator|+
literal|"client is configured to only allow secure connections."
argument_list|)
throw|;
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
literal|"Server asks us to fall back to simple auth."
argument_list|)
expr_stmt|;
block|}
name|saslClient
operator|.
name|dispose
argument_list|()
expr_stmt|;
name|ctx
operator|.
name|pipeline
argument_list|()
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|successfulConnectHandler
operator|.
name|onSuccess
argument_list|(
name|ctx
operator|.
name|channel
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
name|saslToken
operator|=
operator|new
name|byte
index|[
name|len
index|]
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|debug
argument_list|(
literal|"Will read input token of size "
operator|+
name|saslToken
operator|.
name|length
operator|+
literal|" for processing by initSASLContext"
argument_list|)
expr_stmt|;
name|in
operator|.
name|readBytes
argument_list|(
name|saslToken
argument_list|)
expr_stmt|;
name|saslToken
operator|=
name|saslClient
operator|.
name|evaluateChallenge
argument_list|(
name|saslToken
argument_list|)
expr_stmt|;
if|if
condition|(
name|saslToken
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|debug
argument_list|(
literal|"Will send token of size "
operator|+
name|saslToken
operator|.
name|length
operator|+
literal|" from initSASLContext."
argument_list|)
expr_stmt|;
name|writeSaslToken
argument_list|(
name|ctx
argument_list|,
name|saslToken
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|saslClient
operator|.
name|isComplete
argument_list|()
condition|)
block|{
name|String
name|qop
init|=
operator|(
name|String
operator|)
name|saslClient
operator|.
name|getNegotiatedProperty
argument_list|(
name|Sasl
operator|.
name|QOP
argument_list|)
decl_stmt|;
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
literal|"SASL client context established. Negotiated QoP: "
operator|+
name|qop
argument_list|)
expr_stmt|;
block|}
name|boolean
name|useWrap
init|=
name|qop
operator|!=
literal|null
operator|&&
operator|!
literal|"auth"
operator|.
name|equalsIgnoreCase
argument_list|(
name|qop
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|useWrap
condition|)
block|{
name|ctx
operator|.
name|pipeline
argument_list|()
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
name|successfulConnectHandler
operator|.
name|onSuccess
argument_list|(
name|ctx
operator|.
name|channel
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Normal wrapped reading
else|else
block|{
try|try
block|{
name|int
name|length
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
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
literal|"Actual length is "
operator|+
name|length
argument_list|)
expr_stmt|;
block|}
name|saslToken
operator|=
operator|new
name|byte
index|[
name|length
index|]
expr_stmt|;
name|in
operator|.
name|readBytes
argument_list|(
name|saslToken
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexOutOfBoundsException
name|e
parameter_list|)
block|{
return|return;
block|}
try|try
block|{
name|ByteBuf
name|b
init|=
name|ctx
operator|.
name|channel
argument_list|()
operator|.
name|alloc
argument_list|()
operator|.
name|buffer
argument_list|(
name|saslToken
operator|.
name|length
argument_list|)
decl_stmt|;
name|b
operator|.
name|writeBytes
argument_list|(
name|saslClient
operator|.
name|unwrap
argument_list|(
name|saslToken
argument_list|,
literal|0
argument_list|,
name|saslToken
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|fireChannelRead
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SaslException
name|se
parameter_list|)
block|{
try|try
block|{
name|saslClient
operator|.
name|dispose
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SaslException
name|ignored
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Ignoring SASL exception"
argument_list|,
name|ignored
argument_list|)
expr_stmt|;
block|}
throw|throw
name|se
throw|;
block|}
block|}
block|}
comment|/**    * Write SASL token    *    * @param ctx       to write to    * @param saslToken to write    */
specifier|private
name|void
name|writeSaslToken
parameter_list|(
specifier|final
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|byte
index|[]
name|saslToken
parameter_list|)
block|{
name|ByteBuf
name|b
init|=
name|ctx
operator|.
name|alloc
argument_list|()
operator|.
name|buffer
argument_list|(
literal|4
operator|+
name|saslToken
operator|.
name|length
argument_list|)
decl_stmt|;
name|b
operator|.
name|writeInt
argument_list|(
name|saslToken
operator|.
name|length
argument_list|)
expr_stmt|;
name|b
operator|.
name|writeBytes
argument_list|(
name|saslToken
argument_list|,
literal|0
argument_list|,
name|saslToken
operator|.
name|length
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|writeAndFlush
argument_list|(
name|b
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
if|if
condition|(
operator|!
name|future
operator|.
name|isSuccess
argument_list|()
condition|)
block|{
name|exceptionCaught
argument_list|(
name|ctx
argument_list|,
name|future
operator|.
name|cause
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the read status    *    * @param inStream to read    * @throws org.apache.hadoop.ipc.RemoteException if status was not success    */
specifier|private
specifier|static
name|void
name|readStatus
parameter_list|(
name|ByteBuf
name|inStream
parameter_list|)
throws|throws
name|RemoteException
block|{
name|int
name|status
init|=
name|inStream
operator|.
name|readInt
argument_list|()
decl_stmt|;
comment|// read status
if|if
condition|(
name|status
operator|!=
name|SaslStatus
operator|.
name|SUCCESS
operator|.
name|state
condition|)
block|{
throw|throw
operator|new
name|RemoteException
argument_list|(
name|inStream
operator|.
name|toString
argument_list|(
name|Charset
operator|.
name|forName
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|,
name|inStream
operator|.
name|toString
argument_list|(
name|Charset
operator|.
name|forName
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|)
throw|;
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
throws|throws
name|Exception
block|{
name|saslClient
operator|.
name|dispose
argument_list|()
expr_stmt|;
name|ctx
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|random
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|random
operator|=
operator|new
name|Random
argument_list|()
expr_stmt|;
block|}
name|exceptionHandler
operator|.
name|handle
argument_list|(
name|this
operator|.
name|retryCount
operator|++
argument_list|,
name|this
operator|.
name|random
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
specifier|final
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
comment|// If not complete, try to negotiate
if|if
condition|(
operator|!
name|saslClient
operator|.
name|isComplete
argument_list|()
condition|)
block|{
name|super
operator|.
name|write
argument_list|(
name|ctx
argument_list|,
name|msg
argument_list|,
name|promise
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ByteBuf
name|in
init|=
operator|(
name|ByteBuf
operator|)
name|msg
decl_stmt|;
try|try
block|{
name|saslToken
operator|=
name|saslClient
operator|.
name|wrap
argument_list|(
name|in
operator|.
name|array
argument_list|()
argument_list|,
name|in
operator|.
name|readerIndex
argument_list|()
argument_list|,
name|in
operator|.
name|readableBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SaslException
name|se
parameter_list|)
block|{
try|try
block|{
name|saslClient
operator|.
name|dispose
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SaslException
name|ignored
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Ignoring SASL exception"
argument_list|,
name|ignored
argument_list|)
expr_stmt|;
block|}
name|promise
operator|.
name|setFailure
argument_list|(
name|se
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|saslToken
operator|!=
literal|null
condition|)
block|{
name|ByteBuf
name|out
init|=
name|ctx
operator|.
name|channel
argument_list|()
operator|.
name|alloc
argument_list|()
operator|.
name|buffer
argument_list|(
literal|4
operator|+
name|saslToken
operator|.
name|length
argument_list|)
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|saslToken
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytes
argument_list|(
name|saslToken
argument_list|,
literal|0
argument_list|,
name|saslToken
operator|.
name|length
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|write
argument_list|(
name|out
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
if|if
condition|(
operator|!
name|future
operator|.
name|isSuccess
argument_list|()
condition|)
block|{
name|exceptionCaught
argument_list|(
name|ctx
argument_list|,
name|future
operator|.
name|cause
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|saslToken
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Handler for exceptions during Sasl connection    */
specifier|public
interface|interface
name|SaslExceptionHandler
block|{
comment|/**      * Handle the exception      *      * @param retryCount current retry count      * @param random     to create new backoff with      * @param cause      of fail      */
specifier|public
name|void
name|handle
parameter_list|(
name|int
name|retryCount
parameter_list|,
name|Random
name|random
parameter_list|,
name|Throwable
name|cause
parameter_list|)
function_decl|;
block|}
comment|/**    * Handler for successful connects    */
specifier|public
interface|interface
name|SaslSuccessfulConnectHandler
block|{
comment|/**      * Runs on success      *      * @param channel which is successfully authenticated      */
specifier|public
name|void
name|onSuccess
parameter_list|(
name|Channel
name|channel
parameter_list|)
function_decl|;
block|}
block|}
end_class

end_unit

