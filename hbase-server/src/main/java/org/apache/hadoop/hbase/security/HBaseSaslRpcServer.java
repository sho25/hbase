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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
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
name|SaslException
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
name|SaslServer
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

begin_comment
comment|/**  * A utility class that encapsulates SASL logic for RPC server. Copied from  *<code>org.apache.hadoop.security</code>  */
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
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HBaseSaslRpcServer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AttemptingUserProvidingSaslServer
name|serverWithProvider
decl_stmt|;
specifier|private
specifier|final
name|SaslServer
name|saslServer
decl_stmt|;
specifier|public
name|HBaseSaslRpcServer
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|SaslServerAuthenticationProvider
name|provider
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|saslProps
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
name|serverWithProvider
operator|=
name|provider
operator|.
name|createServer
argument_list|(
name|secretManager
argument_list|,
name|saslProps
argument_list|)
expr_stmt|;
name|saslServer
operator|=
name|serverWithProvider
operator|.
name|getServer
argument_list|()
expr_stmt|;
block|}
specifier|public
name|boolean
name|isComplete
parameter_list|()
block|{
return|return
name|saslServer
operator|.
name|isComplete
argument_list|()
return|;
block|}
specifier|public
name|byte
index|[]
name|evaluateResponse
parameter_list|(
name|byte
index|[]
name|response
parameter_list|)
throws|throws
name|SaslException
block|{
return|return
name|saslServer
operator|.
name|evaluateResponse
argument_list|(
name|response
argument_list|)
return|;
block|}
comment|/** Release resources used by wrapped saslServer */
specifier|public
name|void
name|dispose
parameter_list|()
block|{
name|SaslUtil
operator|.
name|safeDispose
argument_list|(
name|saslServer
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getAttemptingUser
parameter_list|()
block|{
name|Optional
argument_list|<
name|UserGroupInformation
argument_list|>
name|optionalUser
init|=
name|serverWithProvider
operator|.
name|getAttemptingUser
argument_list|()
decl_stmt|;
if|if
condition|(
name|optionalUser
operator|.
name|isPresent
argument_list|()
condition|)
block|{
name|optionalUser
operator|.
name|get
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
return|return
literal|"Unknown"
return|;
block|}
specifier|public
name|byte
index|[]
name|wrap
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|SaslException
block|{
return|return
name|saslServer
operator|.
name|wrap
argument_list|(
name|buf
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
return|;
block|}
specifier|public
name|byte
index|[]
name|unwrap
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|SaslException
block|{
return|return
name|saslServer
operator|.
name|unwrap
argument_list|(
name|buf
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
return|;
block|}
specifier|public
name|String
name|getNegotiatedQop
parameter_list|()
block|{
return|return
operator|(
name|String
operator|)
name|saslServer
operator|.
name|getNegotiatedProperty
argument_list|(
name|Sasl
operator|.
name|QOP
argument_list|)
return|;
block|}
specifier|public
name|String
name|getAuthorizationID
parameter_list|()
block|{
return|return
name|saslServer
operator|.
name|getAuthorizationID
argument_list|()
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
block|}
end_class

end_unit

