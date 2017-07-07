begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|crypto
operator|.
name|aes
package|;
end_package

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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|crypto
operator|.
name|stream
operator|.
name|CryptoOutputStream
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
name|classification
operator|.
name|InterfaceStability
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
name|io
operator|.
name|crypto
operator|.
name|Encryptor
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|crypto
operator|.
name|spec
operator|.
name|IvParameterSpec
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
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|Key
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|SecureRandom
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|CommonsCryptoAESEncryptor
implements|implements
name|Encryptor
block|{
specifier|private
name|String
name|cipherMode
decl_stmt|;
specifier|private
name|Properties
name|properties
decl_stmt|;
specifier|private
name|Key
name|key
decl_stmt|;
specifier|private
name|byte
index|[]
name|iv
decl_stmt|;
specifier|private
name|boolean
name|initialized
init|=
literal|false
decl_stmt|;
specifier|private
name|SecureRandom
name|rng
decl_stmt|;
specifier|public
name|CommonsCryptoAESEncryptor
parameter_list|(
name|String
name|cipherMode
parameter_list|,
name|Properties
name|properties
parameter_list|,
name|SecureRandom
name|rng
parameter_list|)
block|{
name|this
operator|.
name|cipherMode
operator|=
name|cipherMode
expr_stmt|;
name|this
operator|.
name|properties
operator|=
name|properties
expr_stmt|;
name|this
operator|.
name|rng
operator|=
name|rng
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setKey
parameter_list|(
name|Key
name|key
parameter_list|)
block|{
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getIvLength
parameter_list|()
block|{
return|return
name|CommonsCryptoAES
operator|.
name|IV_LENGTH
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getBlockSize
parameter_list|()
block|{
return|return
name|CommonsCryptoAES
operator|.
name|BLOCK_SIZE
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getIv
parameter_list|()
block|{
return|return
name|iv
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setIv
parameter_list|(
name|byte
index|[]
name|iv
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|iv
argument_list|,
literal|"IV cannot be null"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|iv
operator|.
name|length
operator|==
name|CommonsCryptoAES
operator|.
name|IV_LENGTH
argument_list|,
literal|"Invalid IV length"
argument_list|)
expr_stmt|;
name|this
operator|.
name|iv
operator|=
name|iv
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|OutputStream
name|createEncryptionStream
parameter_list|(
name|OutputStream
name|out
parameter_list|)
block|{
if|if
condition|(
operator|!
name|initialized
condition|)
block|{
name|reset
argument_list|()
expr_stmt|;
block|}
try|try
block|{
return|return
operator|new
name|CryptoOutputStream
argument_list|(
name|cipherMode
argument_list|,
name|properties
argument_list|,
name|out
argument_list|,
name|key
argument_list|,
operator|new
name|IvParameterSpec
argument_list|(
name|iv
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
block|{
if|if
condition|(
name|iv
operator|==
literal|null
condition|)
block|{
name|iv
operator|=
operator|new
name|byte
index|[
name|getIvLength
argument_list|()
index|]
expr_stmt|;
name|rng
operator|.
name|nextBytes
argument_list|(
name|iv
argument_list|)
expr_stmt|;
block|}
name|initialized
operator|=
literal|true
expr_stmt|;
block|}
block|}
end_class

end_unit

