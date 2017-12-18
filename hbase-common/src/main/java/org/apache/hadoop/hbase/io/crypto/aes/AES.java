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
name|InputStream
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
name|GeneralSecurityException
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
name|javax
operator|.
name|crypto
operator|.
name|spec
operator|.
name|SecretKeySpec
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
name|Cipher
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
name|CipherProvider
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
name|Context
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
name|Decryptor
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
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
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
name|annotations
operator|.
name|VisibleForTesting
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

begin_comment
comment|/**  * AES-128, provided by the JCE  *<p>  * Algorithm instances are pooled for reuse, so the cipher provider and mode  * are configurable but fixed at instantiation.  */
end_comment

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
name|AES
extends|extends
name|Cipher
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
name|AES
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CIPHER_MODE_KEY
init|=
literal|"hbase.crypto.algorithm.aes.mode"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CIPHER_PROVIDER_KEY
init|=
literal|"hbase.crypto.algorithm.aes.provider"
decl_stmt|;
specifier|private
specifier|final
name|String
name|rngAlgorithm
decl_stmt|;
specifier|private
specifier|final
name|String
name|cipherMode
decl_stmt|;
specifier|private
specifier|final
name|String
name|cipherProvider
decl_stmt|;
specifier|private
name|SecureRandom
name|rng
decl_stmt|;
specifier|public
name|AES
parameter_list|(
name|CipherProvider
name|provider
parameter_list|)
block|{
name|super
argument_list|(
name|provider
argument_list|)
expr_stmt|;
comment|// The JCE mode for Ciphers
name|cipherMode
operator|=
name|provider
operator|.
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
name|CIPHER_MODE_KEY
argument_list|,
literal|"AES/CTR/NoPadding"
argument_list|)
expr_stmt|;
comment|// The JCE provider, null if default
name|cipherProvider
operator|=
name|provider
operator|.
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
name|CIPHER_PROVIDER_KEY
argument_list|)
expr_stmt|;
comment|// RNG algorithm
name|rngAlgorithm
operator|=
name|provider
operator|.
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
name|RNG_ALGORITHM_KEY
argument_list|,
literal|"SHA1PRNG"
argument_list|)
expr_stmt|;
comment|// RNG provider, null if default
name|String
name|rngProvider
init|=
name|provider
operator|.
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
name|RNG_PROVIDER_KEY
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|rngProvider
operator|!=
literal|null
condition|)
block|{
name|rng
operator|=
name|SecureRandom
operator|.
name|getInstance
argument_list|(
name|rngAlgorithm
argument_list|,
name|rngProvider
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rng
operator|=
name|SecureRandom
operator|.
name|getInstance
argument_list|(
name|rngAlgorithm
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|GeneralSecurityException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Could not instantiate specified RNG, falling back to default"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|rng
operator|=
operator|new
name|SecureRandom
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"AES"
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getKeyLength
parameter_list|()
block|{
return|return
name|KEY_LENGTH
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getIvLength
parameter_list|()
block|{
return|return
name|IV_LENGTH
return|;
block|}
annotation|@
name|Override
specifier|public
name|Key
name|getRandomKey
parameter_list|()
block|{
name|byte
index|[]
name|keyBytes
init|=
operator|new
name|byte
index|[
name|getKeyLength
argument_list|()
index|]
decl_stmt|;
name|rng
operator|.
name|nextBytes
argument_list|(
name|keyBytes
argument_list|)
expr_stmt|;
return|return
operator|new
name|SecretKeySpec
argument_list|(
name|keyBytes
argument_list|,
name|getName
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Encryptor
name|getEncryptor
parameter_list|()
block|{
return|return
operator|new
name|AESEncryptor
argument_list|(
name|getJCECipherInstance
argument_list|()
argument_list|,
name|rng
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Decryptor
name|getDecryptor
parameter_list|()
block|{
return|return
operator|new
name|AESDecryptor
argument_list|(
name|getJCECipherInstance
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|OutputStream
name|createEncryptionStream
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|Context
name|context
parameter_list|,
name|byte
index|[]
name|iv
parameter_list|)
throws|throws
name|IOException
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|context
operator|.
name|getKey
argument_list|()
operator|!=
literal|null
argument_list|,
literal|"Context does not have a key"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|iv
argument_list|)
expr_stmt|;
name|Encryptor
name|e
init|=
name|getEncryptor
argument_list|()
decl_stmt|;
name|e
operator|.
name|setKey
argument_list|(
name|context
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|.
name|setIv
argument_list|(
name|iv
argument_list|)
expr_stmt|;
return|return
name|e
operator|.
name|createEncryptionStream
argument_list|(
name|out
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|OutputStream
name|createEncryptionStream
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|Encryptor
name|e
parameter_list|)
throws|throws
name|IOException
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return
name|e
operator|.
name|createEncryptionStream
argument_list|(
name|out
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|InputStream
name|createDecryptionStream
parameter_list|(
name|InputStream
name|in
parameter_list|,
name|Context
name|context
parameter_list|,
name|byte
index|[]
name|iv
parameter_list|)
throws|throws
name|IOException
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|context
operator|.
name|getKey
argument_list|()
operator|!=
literal|null
argument_list|,
literal|"Context does not have a key"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|iv
argument_list|)
expr_stmt|;
name|Decryptor
name|d
init|=
name|getDecryptor
argument_list|()
decl_stmt|;
name|d
operator|.
name|setKey
argument_list|(
name|context
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|d
operator|.
name|setIv
argument_list|(
name|iv
argument_list|)
expr_stmt|;
return|return
name|d
operator|.
name|createDecryptionStream
argument_list|(
name|in
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|InputStream
name|createDecryptionStream
parameter_list|(
name|InputStream
name|in
parameter_list|,
name|Decryptor
name|d
parameter_list|)
throws|throws
name|IOException
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|d
argument_list|)
expr_stmt|;
return|return
name|d
operator|.
name|createDecryptionStream
argument_list|(
name|in
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
name|SecureRandom
name|getRNG
parameter_list|()
block|{
return|return
name|rng
return|;
block|}
specifier|private
name|javax
operator|.
name|crypto
operator|.
name|Cipher
name|getJCECipherInstance
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
name|cipherProvider
operator|!=
literal|null
condition|)
block|{
return|return
name|javax
operator|.
name|crypto
operator|.
name|Cipher
operator|.
name|getInstance
argument_list|(
name|cipherMode
argument_list|,
name|cipherProvider
argument_list|)
return|;
block|}
return|return
name|javax
operator|.
name|crypto
operator|.
name|Cipher
operator|.
name|getInstance
argument_list|(
name|cipherMode
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|GeneralSecurityException
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
block|}
end_class

end_unit

