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
name|hadoop
operator|.
name|hbase
operator|.
name|classification
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
name|log
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
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|SASL_PROPS
init|=
operator|new
name|TreeMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
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
specifier|static
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
specifier|public
specifier|final
name|String
name|saslQop
decl_stmt|;
specifier|private
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
comment|/**    * Returns {@link org.apache.hadoop.hbase.security.SaslUtil.QualityOfProtection}    * corresponding to the given {@code stringQop} value. Returns null if value is    * invalid.    */
specifier|public
specifier|static
name|QualityOfProtection
name|getQop
parameter_list|(
name|String
name|stringQop
parameter_list|)
block|{
name|QualityOfProtection
name|qop
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|QualityOfProtection
operator|.
name|AUTHENTICATION
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
name|stringQop
argument_list|)
operator|||
name|QualityOfProtection
operator|.
name|AUTHENTICATION
operator|.
name|saslQop
operator|.
name|equals
argument_list|(
name|stringQop
argument_list|)
condition|)
block|{
name|qop
operator|=
name|QualityOfProtection
operator|.
name|AUTHENTICATION
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|QualityOfProtection
operator|.
name|INTEGRITY
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
name|stringQop
argument_list|)
operator|||
name|QualityOfProtection
operator|.
name|INTEGRITY
operator|.
name|saslQop
operator|.
name|equals
argument_list|(
name|stringQop
argument_list|)
condition|)
block|{
name|qop
operator|=
name|QualityOfProtection
operator|.
name|INTEGRITY
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|QualityOfProtection
operator|.
name|PRIVACY
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
name|stringQop
argument_list|)
operator|||
name|QualityOfProtection
operator|.
name|PRIVACY
operator|.
name|saslQop
operator|.
name|equals
argument_list|(
name|stringQop
argument_list|)
condition|)
block|{
name|qop
operator|=
name|QualityOfProtection
operator|.
name|PRIVACY
expr_stmt|;
block|}
if|if
condition|(
name|qop
operator|==
literal|null
condition|)
block|{
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
if|if
condition|(
name|QualityOfProtection
operator|.
name|AUTHENTICATION
operator|.
name|saslQop
operator|.
name|equals
argument_list|(
name|stringQop
argument_list|)
operator|||
name|QualityOfProtection
operator|.
name|INTEGRITY
operator|.
name|saslQop
operator|.
name|equals
argument_list|(
name|stringQop
argument_list|)
operator|||
name|QualityOfProtection
operator|.
name|PRIVACY
operator|.
name|saslQop
operator|.
name|equals
argument_list|(
name|stringQop
argument_list|)
condition|)
block|{
name|log
operator|.
name|warn
argument_list|(
literal|"Use authentication/integrity/privacy as value for rpc protection "
operator|+
literal|"configurations instead of auth/auth-int/auth-conf."
argument_list|)
expr_stmt|;
block|}
return|return
name|qop
return|;
block|}
specifier|static
name|void
name|initSaslProperties
parameter_list|(
name|String
name|rpcProtection
parameter_list|)
block|{
name|QualityOfProtection
name|saslQOP
init|=
name|getQop
argument_list|(
name|rpcProtection
argument_list|)
decl_stmt|;
if|if
condition|(
name|saslQOP
operator|==
literal|null
condition|)
block|{
name|saslQOP
operator|=
name|QualityOfProtection
operator|.
name|AUTHENTICATION
expr_stmt|;
block|}
name|SaslUtil
operator|.
name|SASL_PROPS
operator|.
name|put
argument_list|(
name|Sasl
operator|.
name|QOP
argument_list|,
name|saslQOP
operator|.
name|getSaslQop
argument_list|()
argument_list|)
expr_stmt|;
name|SaslUtil
operator|.
name|SASL_PROPS
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
block|}
block|}
end_class

end_unit

