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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetAddress
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
name|SaslClientAuthenticationProvider
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

begin_comment
comment|/**  * A utility class that encapsulates SASL logic for RPC client. Copied from  *<code>org.apache.hadoop.security</code>  * @since 2.0.0  */
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
comment|/**    * Create a HBaseSaslRpcClient for an authentication method    * @param conf the configuration object    * @param provider the authentication provider    * @param token token to use if needed by the authentication method    * @param serverAddr the address of the hbase service    * @param securityInfo the security details for the remote hbase service    * @param fallbackAllowed does the client allow fallback to simple authentication    * @throws IOException    */
specifier|protected
name|AbstractHBaseSaslRpcClient
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|SaslClientAuthenticationProvider
name|provider
parameter_list|,
name|Token
argument_list|<
name|?
extends|extends
name|TokenIdentifier
argument_list|>
name|token
parameter_list|,
name|InetAddress
name|serverAddr
parameter_list|,
name|SecurityInfo
name|securityInfo
parameter_list|,
name|boolean
name|fallbackAllowed
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|conf
argument_list|,
name|provider
argument_list|,
name|token
argument_list|,
name|serverAddr
argument_list|,
name|securityInfo
argument_list|,
name|fallbackAllowed
argument_list|,
literal|"authentication"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a HBaseSaslRpcClient for an authentication method    * @param conf configuration object    * @param provider the authentication provider    * @param token token to use if needed by the authentication method    * @param serverAddr the address of the hbase service    * @param securityInfo the security details for the remote hbase service    * @param fallbackAllowed does the client allow fallback to simple authentication    * @param rpcProtection the protection level ("authentication", "integrity" or "privacy")    * @throws IOException    */
specifier|protected
name|AbstractHBaseSaslRpcClient
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|SaslClientAuthenticationProvider
name|provider
parameter_list|,
name|Token
argument_list|<
name|?
extends|extends
name|TokenIdentifier
argument_list|>
name|token
parameter_list|,
name|InetAddress
name|serverAddr
parameter_list|,
name|SecurityInfo
name|securityInfo
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
name|saslClient
operator|=
name|provider
operator|.
name|createClient
argument_list|(
name|conf
argument_list|,
name|serverAddr
argument_list|,
name|securityInfo
argument_list|,
name|token
argument_list|,
name|fallbackAllowed
argument_list|,
name|saslProps
argument_list|)
expr_stmt|;
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
literal|"Authentication provider "
operator|+
name|provider
operator|.
name|getClass
argument_list|()
operator|+
literal|" returned a null SaslClient"
argument_list|)
throw|;
block|}
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
block|}
end_class

end_unit

