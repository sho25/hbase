begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|stargate
package|;
end_package

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|MessageDigest
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/** Representation of an authorized user */
end_comment

begin_class
specifier|public
class|class
name|User
implements|implements
name|Constants
block|{
specifier|public
specifier|static
specifier|final
name|User
name|DEFAULT_USER
init|=
operator|new
name|User
argument_list|(
literal|"default"
argument_list|,
literal|"00000000000000000000000000000000"
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
specifier|private
name|String
name|name
decl_stmt|;
specifier|private
name|String
name|token
decl_stmt|;
specifier|private
name|boolean
name|admin
decl_stmt|;
specifier|private
name|boolean
name|disabled
init|=
literal|false
decl_stmt|;
comment|/**    * Constructor    *<p>    * Creates an access token. (Normally, you don't want this.)    * @param name user name    * @param admin true if user has administrator privilege    * @throws Exception     */
specifier|public
name|User
parameter_list|(
name|String
name|name
parameter_list|,
name|boolean
name|admin
parameter_list|)
throws|throws
name|Exception
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|admin
operator|=
name|admin
expr_stmt|;
name|byte
index|[]
name|digest
init|=
name|MessageDigest
operator|.
name|getInstance
argument_list|(
literal|"MD5"
argument_list|)
operator|.
name|digest
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|name
argument_list|)
argument_list|)
decl_stmt|;
name|StringBuffer
name|sb
init|=
operator|new
name|StringBuffer
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
name|digest
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|Integer
operator|.
name|toHexString
argument_list|(
literal|0xff
operator|&
name|digest
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|token
operator|=
name|sb
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constructor    * @param name user name    * @param token access token, a 16 char hex string    * @param admin true if user has administrator privilege    */
specifier|public
name|User
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|token
parameter_list|,
name|boolean
name|admin
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|token
argument_list|,
name|admin
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor    * @param name user name    * @param token access token, a 16 char hex string    * @param admin true if user has administrator privilege    * @param disabled true if user is disabled    */
specifier|public
name|User
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|token
parameter_list|,
name|boolean
name|admin
parameter_list|,
name|boolean
name|disabled
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|token
operator|=
name|token
expr_stmt|;
name|this
operator|.
name|admin
operator|=
name|admin
expr_stmt|;
name|this
operator|.
name|disabled
operator|=
name|disabled
expr_stmt|;
block|}
comment|/**    * @return user name    */
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/**    * @param name user name    */
specifier|public
name|void
name|setName
parameter_list|(
specifier|final
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
comment|/**    * @return access token, a 16 char hex string    */
specifier|public
name|String
name|getToken
parameter_list|()
block|{
return|return
name|token
return|;
block|}
comment|/**    * @param token access token, a 16 char hex string    */
specifier|public
name|void
name|setToken
parameter_list|(
specifier|final
name|String
name|token
parameter_list|)
block|{
name|this
operator|.
name|token
operator|=
name|token
expr_stmt|;
block|}
comment|/**    * @return true if user has administrator privilege    */
specifier|public
name|boolean
name|isAdmin
parameter_list|()
block|{
return|return
name|admin
return|;
block|}
comment|/**    * @param admin true if user has administrator privilege    */
specifier|public
name|void
name|setAdmin
parameter_list|(
specifier|final
name|boolean
name|admin
parameter_list|)
block|{
name|this
operator|.
name|admin
operator|=
name|admin
expr_stmt|;
block|}
comment|/**    * @return true if user is disabled    */
specifier|public
name|boolean
name|isDisabled
parameter_list|()
block|{
return|return
name|disabled
return|;
block|}
comment|/**    * @param admin true if user is disabled    */
specifier|public
name|void
name|setDisabled
parameter_list|(
name|boolean
name|disabled
parameter_list|)
block|{
name|this
operator|.
name|disabled
operator|=
name|disabled
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
specifier|final
name|int
name|prime
init|=
literal|31
decl_stmt|;
name|int
name|result
init|=
literal|1
decl_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
operator|(
name|admin
condition|?
literal|1231
else|:
literal|1237
operator|)
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
operator|(
name|disabled
condition|?
literal|1231
else|:
literal|1237
operator|)
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
operator|(
operator|(
name|name
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|name
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
operator|(
operator|(
name|token
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|token
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|User
name|other
init|=
operator|(
name|User
operator|)
name|obj
decl_stmt|;
if|if
condition|(
name|admin
operator|!=
name|other
operator|.
name|admin
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|disabled
operator|!=
name|other
operator|.
name|disabled
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|name
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|other
operator|.
name|name
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
block|}
elseif|else
if|if
condition|(
operator|!
name|name
operator|.
name|equals
argument_list|(
name|other
operator|.
name|name
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|token
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|other
operator|.
name|token
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
block|}
elseif|else
if|if
condition|(
operator|!
name|token
operator|.
name|equals
argument_list|(
name|other
operator|.
name|token
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

