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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|Map
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
name|Callback
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
name|auth
operator|.
name|callback
operator|.
name|NameCallback
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
name|PasswordCallback
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
name|UnsupportedCallbackException
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
name|RealmCallback
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
name|RealmChoiceCallback
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
comment|/**  * A utility class that encapsulates SASL logic for RPC client. Copied from  *<code>org.apache.hadoop.security</code>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|AbstractHBaseSaslRpcClient
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
name|AbstractHBaseSaslRpcClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|EMPTY_TOKEN
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
specifier|protected
specifier|final
name|SaslClient
name|saslClient
decl_stmt|;
specifier|protected
specifier|final
name|boolean
name|fallbackAllowed
decl_stmt|;
specifier|protected
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|saslProps
decl_stmt|;
comment|/**    * Create a HBaseSaslRpcClient for an authentication method    * @param method the requested authentication method    * @param token token to use if needed by the authentication method    * @param serverPrincipal the server principal that we are trying to set the connection up to    * @param fallbackAllowed does the client allow fallback to simple authentication    * @throws IOException    */
specifier|protected
name|AbstractHBaseSaslRpcClient
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
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|method
argument_list|,
name|token
argument_list|,
name|serverPrincipal
argument_list|,
name|fallbackAllowed
argument_list|,
literal|"authentication"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a HBaseSaslRpcClient for an authentication method    * @param method the requested authentication method    * @param token token to use if needed by the authentication method    * @param serverPrincipal the server principal that we are trying to set the connection up to    * @param fallbackAllowed does the client allow fallback to simple authentication    * @param rpcProtection the protection level ("authentication", "integrity" or "privacy")    * @throws IOException    */
specifier|protected
name|AbstractHBaseSaslRpcClient
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
name|saslProps
operator|=
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
name|length
argument_list|()
operator|==
literal|0
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
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to find SASL client implementation"
argument_list|)
throw|;
block|}
block|}
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
name|saslProps
argument_list|,
name|saslClientCallbackHandler
argument_list|)
return|;
block|}
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
name|saslProps
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
name|byte
index|[]
name|getInitialResponse
parameter_list|()
throws|throws
name|SaslException
block|{
if|if
condition|(
name|saslClient
operator|.
name|hasInitialResponse
argument_list|()
condition|)
block|{
return|return
name|saslClient
operator|.
name|evaluateChallenge
argument_list|(
name|EMPTY_TOKEN
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|EMPTY_TOKEN
return|;
block|}
block|}
specifier|public
name|boolean
name|isComplete
parameter_list|()
block|{
return|return
name|saslClient
operator|.
name|isComplete
argument_list|()
return|;
block|}
specifier|public
name|byte
index|[]
name|evaluateChallenge
parameter_list|(
name|byte
index|[]
name|challenge
parameter_list|)
throws|throws
name|SaslException
block|{
return|return
name|saslClient
operator|.
name|evaluateChallenge
argument_list|(
name|challenge
argument_list|)
return|;
block|}
comment|/** Release resources used by wrapped saslClient */
specifier|public
name|void
name|dispose
parameter_list|()
block|{
name|SaslUtil
operator|.
name|safeDispose
argument_list|(
name|saslClient
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|static
class|class
name|SaslClientCallbackHandler
implements|implements
name|CallbackHandler
block|{
specifier|private
specifier|final
name|String
name|userName
decl_stmt|;
specifier|private
specifier|final
name|char
index|[]
name|userPassword
decl_stmt|;
specifier|public
name|SaslClientCallbackHandler
parameter_list|(
name|Token
argument_list|<
name|?
extends|extends
name|TokenIdentifier
argument_list|>
name|token
parameter_list|)
block|{
name|this
operator|.
name|userName
operator|=
name|SaslUtil
operator|.
name|encodeIdentifier
argument_list|(
name|token
operator|.
name|getIdentifier
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|userPassword
operator|=
name|SaslUtil
operator|.
name|encodePassword
argument_list|(
name|token
operator|.
name|getPassword
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handle
parameter_list|(
name|Callback
index|[]
name|callbacks
parameter_list|)
throws|throws
name|UnsupportedCallbackException
block|{
name|NameCallback
name|nc
init|=
literal|null
decl_stmt|;
name|PasswordCallback
name|pc
init|=
literal|null
decl_stmt|;
name|RealmCallback
name|rc
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Callback
name|callback
range|:
name|callbacks
control|)
block|{
if|if
condition|(
name|callback
operator|instanceof
name|RealmChoiceCallback
condition|)
block|{
continue|continue;
block|}
elseif|else
if|if
condition|(
name|callback
operator|instanceof
name|NameCallback
condition|)
block|{
name|nc
operator|=
operator|(
name|NameCallback
operator|)
name|callback
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|callback
operator|instanceof
name|PasswordCallback
condition|)
block|{
name|pc
operator|=
operator|(
name|PasswordCallback
operator|)
name|callback
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|callback
operator|instanceof
name|RealmCallback
condition|)
block|{
name|rc
operator|=
operator|(
name|RealmCallback
operator|)
name|callback
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|UnsupportedCallbackException
argument_list|(
name|callback
argument_list|,
literal|"Unrecognized SASL client callback"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|nc
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
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"SASL client callback: setting username: "
operator|+
name|userName
argument_list|)
expr_stmt|;
block|}
name|nc
operator|.
name|setName
argument_list|(
name|userName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|pc
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
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"SASL client callback: setting userPassword"
argument_list|)
expr_stmt|;
block|}
name|pc
operator|.
name|setPassword
argument_list|(
name|userPassword
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|rc
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
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"SASL client callback: setting realm: "
operator|+
name|rc
operator|.
name|getDefaultText
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|rc
operator|.
name|setText
argument_list|(
name|rc
operator|.
name|getDefaultText
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

