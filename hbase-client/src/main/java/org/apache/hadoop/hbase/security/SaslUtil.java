begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|TreeMap
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
name|commons
operator|.
name|codec
operator|.
name|binary
operator|.
name|Base64
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SaslUtil
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
name|SaslUtil
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SASL_DEFAULT_REALM
init|=
literal|"default"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|SWITCH_TO_SIMPLE_AUTH
init|=
operator|-
literal|88
decl_stmt|;
specifier|public
enum|enum
name|QualityOfProtection
block|{
name|AUTHENTICATION
argument_list|(
literal|"auth"
argument_list|)
block|,
name|INTEGRITY
argument_list|(
literal|"auth-int"
argument_list|)
block|,
name|PRIVACY
argument_list|(
literal|"auth-conf"
argument_list|)
block|;
specifier|private
specifier|final
name|String
name|saslQop
decl_stmt|;
name|QualityOfProtection
parameter_list|(
name|String
name|saslQop
parameter_list|)
block|{
name|this
operator|.
name|saslQop
operator|=
name|saslQop
expr_stmt|;
block|}
specifier|public
name|String
name|getSaslQop
parameter_list|()
block|{
return|return
name|saslQop
return|;
block|}
specifier|public
name|boolean
name|matches
parameter_list|(
name|String
name|stringQop
parameter_list|)
block|{
if|if
condition|(
name|saslQop
operator|.
name|equals
argument_list|(
name|stringQop
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Use authentication/integrity/privacy as value for rpc protection "
operator|+
literal|"configurations instead of auth/auth-int/auth-conf."
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
name|name
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|stringQop
argument_list|)
return|;
block|}
block|}
comment|/** Splitting fully qualified Kerberos name into parts */
specifier|public
specifier|static
name|String
index|[]
name|splitKerberosName
parameter_list|(
name|String
name|fullName
parameter_list|)
block|{
return|return
name|fullName
operator|.
name|split
argument_list|(
literal|"[/@]"
argument_list|)
return|;
block|}
specifier|static
name|String
name|encodeIdentifier
parameter_list|(
name|byte
index|[]
name|identifier
parameter_list|)
block|{
return|return
operator|new
name|String
argument_list|(
name|Base64
operator|.
name|encodeBase64
argument_list|(
name|identifier
argument_list|)
argument_list|)
return|;
block|}
specifier|static
name|byte
index|[]
name|decodeIdentifier
parameter_list|(
name|String
name|identifier
parameter_list|)
block|{
return|return
name|Base64
operator|.
name|decodeBase64
argument_list|(
name|identifier
operator|.
name|getBytes
argument_list|()
argument_list|)
return|;
block|}
specifier|static
name|char
index|[]
name|encodePassword
parameter_list|(
name|byte
index|[]
name|password
parameter_list|)
block|{
return|return
operator|new
name|String
argument_list|(
name|Base64
operator|.
name|encodeBase64
argument_list|(
name|password
argument_list|)
argument_list|)
operator|.
name|toCharArray
argument_list|()
return|;
block|}
comment|/**    * Returns {@link org.apache.hadoop.hbase.security.SaslUtil.QualityOfProtection}    * corresponding to the given {@code stringQop} value.    * @throws IllegalArgumentException If stringQop doesn't match any QOP.    */
specifier|public
specifier|static
name|QualityOfProtection
name|getQop
parameter_list|(
name|String
name|stringQop
parameter_list|)
block|{
for|for
control|(
name|QualityOfProtection
name|qop
range|:
name|QualityOfProtection
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|qop
operator|.
name|matches
argument_list|(
name|stringQop
argument_list|)
condition|)
block|{
return|return
name|qop
return|;
block|}
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid qop: "
operator|+
name|stringQop
operator|+
literal|". It must be one of 'authentication', 'integrity', 'privacy'."
argument_list|)
throw|;
block|}
comment|/**    * @param rpcProtection Value of 'hbase.rpc.protection' configuration.    * @return Map with values for SASL properties.    */
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|initSaslProperties
parameter_list|(
name|String
name|rpcProtection
parameter_list|)
block|{
name|String
name|saslQop
decl_stmt|;
if|if
condition|(
name|rpcProtection
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|saslQop
operator|=
name|QualityOfProtection
operator|.
name|AUTHENTICATION
operator|.
name|getSaslQop
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|String
index|[]
name|qops
init|=
name|rpcProtection
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|StringBuilder
name|saslQopBuilder
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
literal|0
init|;
name|i
operator|<
name|qops
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|QualityOfProtection
name|qop
init|=
name|getQop
argument_list|(
name|qops
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|saslQopBuilder
operator|.
name|append
argument_list|(
literal|","
argument_list|)
operator|.
name|append
argument_list|(
name|qop
operator|.
name|getSaslQop
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|saslQop
operator|=
name|saslQopBuilder
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// remove first ','
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|saslProps
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
name|saslProps
operator|.
name|put
argument_list|(
name|Sasl
operator|.
name|QOP
argument_list|,
name|saslQop
argument_list|)
expr_stmt|;
name|saslProps
operator|.
name|put
argument_list|(
name|Sasl
operator|.
name|SERVER_AUTH
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
return|return
name|saslProps
return|;
block|}
specifier|static
name|void
name|safeDispose
parameter_list|(
name|SaslClient
name|saslClient
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
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error disposing of SASL client"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
name|void
name|safeDispose
parameter_list|(
name|SaslServer
name|saslServer
parameter_list|)
block|{
try|try
block|{
name|saslServer
operator|.
name|dispose
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SaslException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error disposing of SASL server"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

