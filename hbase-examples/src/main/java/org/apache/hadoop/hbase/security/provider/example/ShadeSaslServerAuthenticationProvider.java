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
operator|.
name|provider
operator|.
name|example
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedReader
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
name|io
operator|.
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReference
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
name|fs
operator|.
name|FSDataInputStream
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|provider
operator|.
name|AttemptingUserProvidingSaslServer
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
name|provider
operator|.
name|SaslServerAuthenticationProvider
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
name|SecretManager
operator|.
name|InvalidToken
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
name|util
operator|.
name|StringUtils
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ShadeSaslServerAuthenticationProvider
extends|extends
name|ShadeSaslAuthenticationProvider
implements|implements
name|SaslServerAuthenticationProvider
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ShadeSaslServerAuthenticationProvider
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PASSWORD_FILE_KEY
init|=
literal|"hbase.security.shade.password.file"
decl_stmt|;
specifier|static
specifier|final
name|char
name|SEPARATOR
init|=
literal|'='
decl_stmt|;
specifier|private
name|AtomicReference
argument_list|<
name|UserGroupInformation
argument_list|>
name|attemptingUser
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|(
literal|null
argument_list|)
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|char
index|[]
argument_list|>
name|passwordDatabase
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|passwordDatabase
operator|=
name|readPasswordDB
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|AttemptingUserProvidingSaslServer
name|createServer
parameter_list|(
name|SecretManager
argument_list|<
name|TokenIdentifier
argument_list|>
name|secretManager
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|saslProps
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|AttemptingUserProvidingSaslServer
argument_list|(
operator|new
name|SaslPlainServer
argument_list|(
operator|new
name|ShadeSaslServerCallbackHandler
argument_list|(
name|attemptingUser
argument_list|,
name|passwordDatabase
argument_list|)
argument_list|)
argument_list|,
parameter_list|()
lambda|->
name|attemptingUser
operator|.
name|get
argument_list|()
argument_list|)
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|char
index|[]
argument_list|>
name|readPasswordDB
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|passwordFileName
init|=
name|conf
operator|.
name|get
argument_list|(
name|PASSWORD_FILE_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|passwordFileName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|PASSWORD_FILE_KEY
operator|+
literal|" is not defined in configuration, cannot use this implementation"
argument_list|)
throw|;
block|}
name|Path
name|passwordFile
init|=
operator|new
name|Path
argument_list|(
name|passwordFileName
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|passwordFile
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|passwordFile
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Configured password file does not exist: "
operator|+
name|passwordFile
argument_list|)
throw|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|char
index|[]
argument_list|>
name|passwordDb
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
try|try
init|(
name|FSDataInputStream
name|fdis
init|=
name|fs
operator|.
name|open
argument_list|(
name|passwordFile
argument_list|)
init|;
name|BufferedReader
name|reader
operator|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|fdis
argument_list|)
argument_list|)
init|)
block|{
name|String
name|line
init|=
literal|null
decl_stmt|;
name|int
name|offset
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|reader
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|line
operator|=
name|line
operator|.
name|trim
argument_list|()
expr_stmt|;
name|String
index|[]
name|parts
init|=
name|StringUtils
operator|.
name|split
argument_list|(
name|line
argument_list|,
name|SEPARATOR
argument_list|)
decl_stmt|;
if|if
condition|(
name|parts
operator|.
name|length
operator|<
literal|2
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Password file contains invalid record on line {}, skipping"
argument_list|,
name|offset
operator|+
literal|1
argument_list|)
expr_stmt|;
continue|continue;
block|}
specifier|final
name|String
name|username
init|=
name|parts
index|[
literal|0
index|]
decl_stmt|;
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|parts
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|builder
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
name|SEPARATOR
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
name|parts
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|passwordDb
operator|.
name|put
argument_list|(
name|username
argument_list|,
name|builder
operator|.
name|toString
argument_list|()
operator|.
name|toCharArray
argument_list|()
argument_list|)
expr_stmt|;
name|offset
operator|++
expr_stmt|;
block|}
block|}
return|return
name|passwordDb
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|supportsProtocolAuthentication
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|UserGroupInformation
name|getAuthorizedUgi
parameter_list|(
name|String
name|authzId
parameter_list|,
name|SecretManager
argument_list|<
name|TokenIdentifier
argument_list|>
name|secretManager
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|UserGroupInformation
operator|.
name|createRemoteUser
argument_list|(
name|authzId
argument_list|)
return|;
block|}
specifier|static
class|class
name|ShadeSaslServerCallbackHandler
implements|implements
name|CallbackHandler
block|{
specifier|private
specifier|final
name|AtomicReference
argument_list|<
name|UserGroupInformation
argument_list|>
name|attemptingUser
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|char
index|[]
argument_list|>
name|passwordDatabase
decl_stmt|;
specifier|public
name|ShadeSaslServerCallbackHandler
parameter_list|(
name|AtomicReference
argument_list|<
name|UserGroupInformation
argument_list|>
name|attemptingUser
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|char
index|[]
argument_list|>
name|passwordDatabase
parameter_list|)
block|{
name|this
operator|.
name|attemptingUser
operator|=
name|attemptingUser
expr_stmt|;
name|this
operator|.
name|passwordDatabase
operator|=
name|passwordDatabase
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
name|InvalidToken
throws|,
name|UnsupportedCallbackException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"SaslServerCallbackHandler called"
argument_list|,
operator|new
name|Exception
argument_list|()
argument_list|)
expr_stmt|;
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
literal|"Unrecognized SASL PLAIN Callback"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|nc
operator|!=
literal|null
operator|&&
name|pc
operator|!=
literal|null
condition|)
block|{
name|String
name|username
init|=
name|nc
operator|.
name|getName
argument_list|()
decl_stmt|;
name|UserGroupInformation
name|ugi
init|=
name|createUgiForRemoteUser
argument_list|(
name|username
argument_list|)
decl_stmt|;
name|attemptingUser
operator|.
name|set
argument_list|(
name|ugi
argument_list|)
expr_stmt|;
name|char
index|[]
name|clientPassword
init|=
name|pc
operator|.
name|getPassword
argument_list|()
decl_stmt|;
name|char
index|[]
name|actualPassword
init|=
name|passwordDatabase
operator|.
name|get
argument_list|(
name|username
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|clientPassword
argument_list|,
name|actualPassword
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|InvalidToken
argument_list|(
literal|"Authentication failed for "
operator|+
name|username
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
name|authenticatedUserId
init|=
name|ac
operator|.
name|getAuthenticationID
argument_list|()
decl_stmt|;
name|String
name|userRequestedToExecuteAs
init|=
name|ac
operator|.
name|getAuthorizationID
argument_list|()
decl_stmt|;
if|if
condition|(
name|authenticatedUserId
operator|.
name|equals
argument_list|(
name|userRequestedToExecuteAs
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
name|ac
operator|.
name|setAuthorizedID
argument_list|(
name|userRequestedToExecuteAs
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
block|}
block|}
name|UserGroupInformation
name|createUgiForRemoteUser
parameter_list|(
name|String
name|username
parameter_list|)
block|{
name|UserGroupInformation
name|ugi
init|=
name|UserGroupInformation
operator|.
name|createRemoteUser
argument_list|(
name|username
argument_list|)
decl_stmt|;
name|ugi
operator|.
name|setAuthenticationMethod
argument_list|(
name|ShadeSaslAuthenticationProvider
operator|.
name|METHOD
operator|.
name|getAuthMethod
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|ugi
return|;
block|}
block|}
block|}
end_class

end_unit

