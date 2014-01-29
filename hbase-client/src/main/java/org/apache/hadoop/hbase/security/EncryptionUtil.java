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
name|ByteArrayOutputStream
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
name|KeyException
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|HBaseZeroCopyByteString
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
name|HConstants
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
name|Encryption
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
name|protobuf
operator|.
name|generated
operator|.
name|EncryptionProtos
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
comment|/**  * Some static utility methods for encryption uses in hbase-client.  */
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
name|EncryptionUtil
block|{
specifier|static
specifier|private
specifier|final
name|SecureRandom
name|RNG
init|=
operator|new
name|SecureRandom
argument_list|()
decl_stmt|;
comment|/**    * Protect a key by encrypting it with the secret key of the given subject.    * The configuration must be set up correctly for key alias resolution.    * @param conf configuration    * @param key the raw key bytes    * @param algorithm the algorithm to use with this key material    * @return the encrypted key bytes    * @throws IOException    */
specifier|public
specifier|static
name|byte
index|[]
name|wrapKey
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|byte
index|[]
name|key
parameter_list|,
name|String
name|algorithm
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|wrapKey
argument_list|(
name|conf
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|CRYPTO_MASTERKEY_NAME_CONF_KEY
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
operator|.
name|getShortName
argument_list|()
argument_list|)
argument_list|,
operator|new
name|SecretKeySpec
argument_list|(
name|key
argument_list|,
name|algorithm
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Protect a key by encrypting it with the secret key of the given subject.    * The configuration must be set up correctly for key alias resolution. Keys    * are always wrapped using AES.    * @param conf configuration    * @param subject subject key alias    * @param key the key    * @return the encrypted key bytes    */
specifier|public
specifier|static
name|byte
index|[]
name|wrapKey
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|subject
parameter_list|,
name|Key
name|key
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Wrap the key with AES
name|Cipher
name|cipher
init|=
name|Encryption
operator|.
name|getCipher
argument_list|(
name|conf
argument_list|,
literal|"AES"
argument_list|)
decl_stmt|;
if|if
condition|(
name|cipher
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cipher 'AES' not available"
argument_list|)
throw|;
block|}
name|EncryptionProtos
operator|.
name|WrappedKey
operator|.
name|Builder
name|builder
init|=
name|EncryptionProtos
operator|.
name|WrappedKey
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setAlgorithm
argument_list|(
name|key
operator|.
name|getAlgorithm
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|iv
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|cipher
operator|.
name|getIvLength
argument_list|()
operator|>
literal|0
condition|)
block|{
name|iv
operator|=
operator|new
name|byte
index|[
name|cipher
operator|.
name|getIvLength
argument_list|()
index|]
expr_stmt|;
name|RNG
operator|.
name|nextBytes
argument_list|(
name|iv
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setIv
argument_list|(
name|HBaseZeroCopyByteString
operator|.
name|wrap
argument_list|(
name|iv
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|keyBytes
init|=
name|key
operator|.
name|getEncoded
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setLength
argument_list|(
name|keyBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setHash
argument_list|(
name|HBaseZeroCopyByteString
operator|.
name|wrap
argument_list|(
name|Encryption
operator|.
name|hash128
argument_list|(
name|keyBytes
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ByteArrayOutputStream
name|out
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|Encryption
operator|.
name|encryptWithSubjectKey
argument_list|(
name|out
argument_list|,
operator|new
name|ByteArrayInputStream
argument_list|(
name|keyBytes
argument_list|)
argument_list|,
name|subject
argument_list|,
name|conf
argument_list|,
name|cipher
argument_list|,
name|iv
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setData
argument_list|(
name|HBaseZeroCopyByteString
operator|.
name|wrap
argument_list|(
name|out
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Build and return the protobuf message
name|out
operator|.
name|reset
argument_list|()
expr_stmt|;
name|builder
operator|.
name|build
argument_list|()
operator|.
name|writeDelimitedTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
return|return
name|out
operator|.
name|toByteArray
argument_list|()
return|;
block|}
comment|/**    * Unwrap a key by decrypting it with the secret key of the given subject.    * The configuration must be set up correctly for key alias resolution. Keys    * are always unwrapped using AES.    * @param conf configuration    * @param subject subject key alias    * @param value the encrypted key bytes    * @return the raw key bytes    * @throws IOException    * @throws KeyException    */
specifier|public
specifier|static
name|Key
name|unwrapKey
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|subject
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|IOException
throws|,
name|KeyException
block|{
name|EncryptionProtos
operator|.
name|WrappedKey
name|wrappedKey
init|=
name|EncryptionProtos
operator|.
name|WrappedKey
operator|.
name|PARSER
operator|.
name|parseDelimitedFrom
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|value
argument_list|)
argument_list|)
decl_stmt|;
name|Cipher
name|cipher
init|=
name|Encryption
operator|.
name|getCipher
argument_list|(
name|conf
argument_list|,
literal|"AES"
argument_list|)
decl_stmt|;
if|if
condition|(
name|cipher
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Algorithm 'AES' not available"
argument_list|)
throw|;
block|}
name|ByteArrayOutputStream
name|out
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|byte
index|[]
name|iv
init|=
name|wrappedKey
operator|.
name|hasIv
argument_list|()
condition|?
name|wrappedKey
operator|.
name|getIv
argument_list|()
operator|.
name|toByteArray
argument_list|()
else|:
literal|null
decl_stmt|;
name|Encryption
operator|.
name|decryptWithSubjectKey
argument_list|(
name|out
argument_list|,
name|wrappedKey
operator|.
name|getData
argument_list|()
operator|.
name|newInput
argument_list|()
argument_list|,
name|wrappedKey
operator|.
name|getLength
argument_list|()
argument_list|,
name|subject
argument_list|,
name|conf
argument_list|,
name|cipher
argument_list|,
name|iv
argument_list|)
expr_stmt|;
name|byte
index|[]
name|keyBytes
init|=
name|out
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
if|if
condition|(
name|wrappedKey
operator|.
name|hasHash
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|wrappedKey
operator|.
name|getHash
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|Encryption
operator|.
name|hash128
argument_list|(
name|keyBytes
argument_list|)
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|KeyException
argument_list|(
literal|"Key was not successfully unwrapped"
argument_list|)
throw|;
block|}
block|}
return|return
operator|new
name|SecretKeySpec
argument_list|(
name|keyBytes
argument_list|,
name|wrappedKey
operator|.
name|getAlgorithm
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

