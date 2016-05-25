begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
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
name|Locale
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
name|AuthorizeCallback
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
name|ipc
operator|.
name|RpcServer
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
name|security
operator|.
name|SaslUtil
operator|.
name|QualityOfProtection
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
name|SecretManager
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
name|SecretManager
operator|.
name|InvalidToken
import|;
end_import

begin_comment
comment|/**  * A utility class for dealing with SASL on RPC server  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HBaseSaslRpcServer
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
name|HBaseSaslRpcServer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|saslProps
init|=
literal|null
decl_stmt|;
specifier|public
specifier|static
name|void
name|init
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|saslProps
operator|=
name|SaslUtil
operator|.
name|initSaslProperties
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.rpc.protection"
argument_list|,
name|QualityOfProtection
operator|.
name|AUTHENTICATION
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getSaslProps
parameter_list|()
block|{
return|return
name|saslProps
return|;
block|}
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|TokenIdentifier
parameter_list|>
name|T
name|getIdentifier
parameter_list|(
name|String
name|id
parameter_list|,
name|SecretManager
argument_list|<
name|T
argument_list|>
name|secretManager
parameter_list|)
throws|throws
name|InvalidToken
block|{
name|byte
index|[]
name|tokenId
init|=
name|SaslUtil
operator|.
name|decodeIdentifier
argument_list|(
name|id
argument_list|)
decl_stmt|;
name|T
name|tokenIdentifier
init|=
name|secretManager
operator|.
name|createIdentifier
argument_list|()
decl_stmt|;
try|try
block|{
name|tokenIdentifier
operator|.
name|readFields
argument_list|(
operator|new
name|DataInputStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|tokenId
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|InvalidToken
operator|)
operator|new
name|InvalidToken
argument_list|(
literal|"Can't de-serialize tokenIdentifier"
argument_list|)
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|tokenIdentifier
return|;
block|}
comment|/** CallbackHandler for SASL DIGEST-MD5 mechanism */
specifier|public
specifier|static
class|class
name|SaslDigestCallbackHandler
implements|implements
name|CallbackHandler
block|{
specifier|private
name|SecretManager
argument_list|<
name|TokenIdentifier
argument_list|>
name|secretManager
decl_stmt|;
specifier|private
name|RpcServer
operator|.
name|Connection
name|connection
decl_stmt|;
specifier|public
name|SaslDigestCallbackHandler
parameter_list|(
name|SecretManager
argument_list|<
name|TokenIdentifier
argument_list|>
name|secretManager
parameter_list|,
name|RpcServer
operator|.
name|Connection
name|connection
parameter_list|)
block|{
name|this
operator|.
name|secretManager
operator|=
name|secretManager
expr_stmt|;
name|this
operator|.
name|connection
operator|=
name|connection
expr_stmt|;
block|}
specifier|private
name|char
index|[]
name|getPassword
parameter_list|(
name|TokenIdentifier
name|tokenid
parameter_list|)
throws|throws
name|InvalidToken
block|{
return|return
name|SaslUtil
operator|.
name|encodePassword
argument_list|(
name|secretManager
operator|.
name|retrievePassword
argument_list|(
name|tokenid
argument_list|)
argument_list|)
return|;
block|}
comment|/** {@inheritDoc} */
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
name|InvalidToken
throws|,
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
name|AuthorizeCallback
name|ac
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
name|AuthorizeCallback
condition|)
block|{
name|ac
operator|=
operator|(
name|AuthorizeCallback
operator|)
name|callback
expr_stmt|;
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
continue|continue;
comment|// realm is ignored
block|}
else|else
block|{
throw|throw
operator|new
name|UnsupportedCallbackException
argument_list|(
name|callback
argument_list|,
literal|"Unrecognized SASL DIGEST-MD5 Callback"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|pc
operator|!=
literal|null
condition|)
block|{
name|TokenIdentifier
name|tokenIdentifier
init|=
name|getIdentifier
argument_list|(
name|nc
operator|.
name|getDefaultName
argument_list|()
argument_list|,
name|secretManager
argument_list|)
decl_stmt|;
name|char
index|[]
name|password
init|=
name|getPassword
argument_list|(
name|tokenIdentifier
argument_list|)
decl_stmt|;
name|UserGroupInformation
name|user
init|=
literal|null
decl_stmt|;
name|user
operator|=
name|tokenIdentifier
operator|.
name|getUser
argument_list|()
expr_stmt|;
comment|// may throw exception
name|connection
operator|.
name|attemptingUser
operator|=
name|user
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
literal|"SASL server DIGEST-MD5 callback: setting password "
operator|+
literal|"for client: "
operator|+
name|tokenIdentifier
operator|.
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|pc
operator|.
name|setPassword
argument_list|(
name|password
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ac
operator|!=
literal|null
condition|)
block|{
name|String
name|authid
init|=
name|ac
operator|.
name|getAuthenticationID
argument_list|()
decl_stmt|;
name|String
name|authzid
init|=
name|ac
operator|.
name|getAuthorizationID
argument_list|()
decl_stmt|;
if|if
condition|(
name|authid
operator|.
name|equals
argument_list|(
name|authzid
argument_list|)
condition|)
block|{
name|ac
operator|.
name|setAuthorized
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ac
operator|.
name|setAuthorized
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ac
operator|.
name|isAuthorized
argument_list|()
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
name|String
name|username
init|=
name|getIdentifier
argument_list|(
name|authzid
argument_list|,
name|secretManager
argument_list|)
operator|.
name|getUser
argument_list|()
operator|.
name|getUserName
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"SASL server DIGEST-MD5 callback: setting "
operator|+
literal|"canonicalized client ID: "
operator|+
name|username
argument_list|)
expr_stmt|;
block|}
name|ac
operator|.
name|setAuthorizedID
argument_list|(
name|authzid
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/** CallbackHandler for SASL GSSAPI Kerberos mechanism */
specifier|public
specifier|static
class|class
name|SaslGssCallbackHandler
implements|implements
name|CallbackHandler
block|{
comment|/** {@inheritDoc} */
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
name|AuthorizeCallback
name|ac
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
name|AuthorizeCallback
condition|)
block|{
name|ac
operator|=
operator|(
name|AuthorizeCallback
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
literal|"Unrecognized SASL GSSAPI Callback"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|ac
operator|!=
literal|null
condition|)
block|{
name|String
name|authid
init|=
name|ac
operator|.
name|getAuthenticationID
argument_list|()
decl_stmt|;
name|String
name|authzid
init|=
name|ac
operator|.
name|getAuthorizationID
argument_list|()
decl_stmt|;
if|if
condition|(
name|authid
operator|.
name|equals
argument_list|(
name|authzid
argument_list|)
condition|)
block|{
name|ac
operator|.
name|setAuthorized
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ac
operator|.
name|setAuthorized
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ac
operator|.
name|isAuthorized
argument_list|()
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
literal|"SASL server GSSAPI callback: setting "
operator|+
literal|"canonicalized client ID: "
operator|+
name|authzid
argument_list|)
expr_stmt|;
name|ac
operator|.
name|setAuthorizedID
argument_list|(
name|authzid
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

