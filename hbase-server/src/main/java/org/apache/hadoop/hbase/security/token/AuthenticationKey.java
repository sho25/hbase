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
name|token
package|;
end_package

begin_import
import|import
name|javax
operator|.
name|crypto
operator|.
name|SecretKey
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|Arrays
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
name|hbase
operator|.
name|util
operator|.
name|Bytes
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
name|io
operator|.
name|Writable
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
name|io
operator|.
name|WritableUtils
import|;
end_import

begin_comment
comment|/**  * Represents a secret key used for signing and verifying authentication tokens  * by {@link AuthenticationTokenSecretManager}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|AuthenticationKey
implements|implements
name|Writable
block|{
specifier|private
name|int
name|id
decl_stmt|;
specifier|private
name|long
name|expirationDate
decl_stmt|;
specifier|private
name|SecretKey
name|secret
decl_stmt|;
specifier|public
name|AuthenticationKey
parameter_list|()
block|{
comment|// for Writable
block|}
specifier|public
name|AuthenticationKey
parameter_list|(
name|int
name|keyId
parameter_list|,
name|long
name|expirationDate
parameter_list|,
name|SecretKey
name|key
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|keyId
expr_stmt|;
name|this
operator|.
name|expirationDate
operator|=
name|expirationDate
expr_stmt|;
name|this
operator|.
name|secret
operator|=
name|key
expr_stmt|;
block|}
specifier|public
name|int
name|getKeyId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
specifier|public
name|long
name|getExpiration
parameter_list|()
block|{
return|return
name|expirationDate
return|;
block|}
specifier|public
name|void
name|setExpiration
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
name|expirationDate
operator|=
name|timestamp
expr_stmt|;
block|}
name|SecretKey
name|getKey
parameter_list|()
block|{
return|return
name|secret
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|id
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
call|(
name|int
call|)
argument_list|(
name|expirationDate
operator|^
operator|(
name|expirationDate
operator|>>>
literal|32
operator|)
argument_list|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
operator|(
name|secret
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|Arrays
operator|.
name|hashCode
argument_list|(
name|secret
operator|.
name|getEncoded
argument_list|()
argument_list|)
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
name|obj
operator|==
literal|null
operator|||
operator|!
operator|(
name|obj
operator|instanceof
name|AuthenticationKey
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|AuthenticationKey
name|other
init|=
operator|(
name|AuthenticationKey
operator|)
name|obj
decl_stmt|;
return|return
name|id
operator|==
name|other
operator|.
name|getKeyId
argument_list|()
operator|&&
name|expirationDate
operator|==
name|other
operator|.
name|getExpiration
argument_list|()
operator|&&
operator|(
name|secret
operator|==
literal|null
condition|?
name|other
operator|.
name|getKey
argument_list|()
operator|==
literal|null
else|:
name|other
operator|.
name|getKey
argument_list|()
operator|!=
literal|null
operator|&&
name|Bytes
operator|.
name|equals
argument_list|(
name|secret
operator|.
name|getEncoded
argument_list|()
argument_list|,
name|other
operator|.
name|getKey
argument_list|()
operator|.
name|getEncoded
argument_list|()
argument_list|)
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|buf
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|"AuthenticationKey[ "
argument_list|)
operator|.
name|append
argument_list|(
literal|"id="
argument_list|)
operator|.
name|append
argument_list|(
name|id
argument_list|)
operator|.
name|append
argument_list|(
literal|", expiration="
argument_list|)
operator|.
name|append
argument_list|(
name|expirationDate
argument_list|)
operator|.
name|append
argument_list|(
literal|" ]"
argument_list|)
expr_stmt|;
return|return
name|buf
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|WritableUtils
operator|.
name|writeVInt
argument_list|(
name|out
argument_list|,
name|id
argument_list|)
expr_stmt|;
name|WritableUtils
operator|.
name|writeVLong
argument_list|(
name|out
argument_list|,
name|expirationDate
argument_list|)
expr_stmt|;
if|if
condition|(
name|secret
operator|==
literal|null
condition|)
block|{
name|WritableUtils
operator|.
name|writeVInt
argument_list|(
name|out
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|byte
index|[]
name|keyBytes
init|=
name|secret
operator|.
name|getEncoded
argument_list|()
decl_stmt|;
name|WritableUtils
operator|.
name|writeVInt
argument_list|(
name|out
argument_list|,
name|keyBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|keyBytes
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|id
operator|=
name|WritableUtils
operator|.
name|readVInt
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|expirationDate
operator|=
name|WritableUtils
operator|.
name|readVLong
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|int
name|keyLength
init|=
name|WritableUtils
operator|.
name|readVInt
argument_list|(
name|in
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyLength
operator|<
literal|0
condition|)
block|{
name|secret
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|byte
index|[]
name|keyBytes
init|=
operator|new
name|byte
index|[
name|keyLength
index|]
decl_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|keyBytes
argument_list|)
expr_stmt|;
name|secret
operator|=
name|AuthenticationTokenSecretManager
operator|.
name|createSecretKey
argument_list|(
name|keyBytes
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

