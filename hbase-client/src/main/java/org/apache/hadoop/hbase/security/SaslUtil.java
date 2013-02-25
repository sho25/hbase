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

begin_class
specifier|public
class|class
name|SaslUtil
block|{
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
block|}
end_class

end_unit

