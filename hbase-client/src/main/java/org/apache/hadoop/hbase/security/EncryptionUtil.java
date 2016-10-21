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
name|java
operator|.
name|util
operator|.
name|Properties
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
name|commons
operator|.
name|crypto
operator|.
name|cipher
operator|.
name|CryptoCipherFactory
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
name|HColumnDescriptor
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|UnsafeByteOperations
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|RPCProtos
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
name|aes
operator|.
name|CryptoAES
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
specifier|final
class|class
name|EncryptionUtil
block|{
specifier|static
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|EncryptionUtil
operator|.
name|class
argument_list|)
decl_stmt|;
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
comment|/**    * Private constructor to keep this class from being instantiated.    */
specifier|private
name|EncryptionUtil
parameter_list|()
block|{   }
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
comment|/**    * Protect a key by encrypting it with the secret key of the given subject.    * The configuration must be set up correctly for key alias resolution.    * @param conf configuration    * @param subject subject key alias    * @param key the key    * @return the encrypted key bytes    */
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
comment|// Wrap the key with the configured encryption algorithm.
name|String
name|algorithm
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|CRYPTO_KEY_ALGORITHM_CONF_KEY
argument_list|,
name|HConstants
operator|.
name|CIPHER_AES
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
name|algorithm
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
literal|"Cipher '"
operator|+
name|algorithm
operator|+
literal|"' not available"
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
name|UnsafeByteOperations
operator|.
name|unsafeWrap
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
name|UnsafeByteOperations
operator|.
name|unsafeWrap
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
name|UnsafeByteOperations
operator|.
name|unsafeWrap
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
comment|/**    * Unwrap a key by decrypting it with the secret key of the given subject.    * The configuration must be set up correctly for key alias resolution.    * @param conf configuration    * @param subject subject key alias    * @param value the encrypted key bytes    * @return the raw key bytes    * @throws IOException    * @throws KeyException    */
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
name|String
name|algorithm
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|CRYPTO_KEY_ALGORITHM_CONF_KEY
argument_list|,
name|HConstants
operator|.
name|CIPHER_AES
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
name|algorithm
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
literal|"Cipher '"
operator|+
name|algorithm
operator|+
literal|"' not available"
argument_list|)
throw|;
block|}
return|return
name|getUnwrapKey
argument_list|(
name|conf
argument_list|,
name|subject
argument_list|,
name|wrappedKey
argument_list|,
name|cipher
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|Key
name|getUnwrapKey
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|subject
parameter_list|,
name|EncryptionProtos
operator|.
name|WrappedKey
name|wrappedKey
parameter_list|,
name|Cipher
name|cipher
parameter_list|)
throws|throws
name|IOException
throws|,
name|KeyException
block|{
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
comment|/**    * Unwrap a wal key by decrypting it with the secret key of the given subject. The configuration    * must be set up correctly for key alias resolution.    * @param conf configuration    * @param subject subject key alias    * @param value the encrypted key bytes    * @return the raw key bytes    * @throws IOException if key is not found for the subject, or if some I/O error occurs    * @throws KeyException if fail to unwrap the key    */
specifier|public
specifier|static
name|Key
name|unwrapWALKey
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
name|String
name|algorithm
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|CRYPTO_WAL_ALGORITHM_CONF_KEY
argument_list|,
name|HConstants
operator|.
name|CIPHER_AES
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
name|algorithm
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
literal|"Cipher '"
operator|+
name|algorithm
operator|+
literal|"' not available"
argument_list|)
throw|;
block|}
return|return
name|getUnwrapKey
argument_list|(
name|conf
argument_list|,
name|subject
argument_list|,
name|wrappedKey
argument_list|,
name|cipher
argument_list|)
return|;
block|}
comment|/**    * Helper to create an encyption context.    *    * @param conf The current configuration.    * @param family The current column descriptor.    * @return The created encryption context.    * @throws IOException if an encryption key for the column cannot be unwrapped    */
specifier|public
specifier|static
name|Encryption
operator|.
name|Context
name|createEncryptionContext
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HColumnDescriptor
name|family
parameter_list|)
throws|throws
name|IOException
block|{
name|Encryption
operator|.
name|Context
name|cryptoContext
init|=
name|Encryption
operator|.
name|Context
operator|.
name|NONE
decl_stmt|;
name|String
name|cipherName
init|=
name|family
operator|.
name|getEncryptionType
argument_list|()
decl_stmt|;
if|if
condition|(
name|cipherName
operator|!=
literal|null
condition|)
block|{
name|Cipher
name|cipher
decl_stmt|;
name|Key
name|key
decl_stmt|;
name|byte
index|[]
name|keyBytes
init|=
name|family
operator|.
name|getEncryptionKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|keyBytes
operator|!=
literal|null
condition|)
block|{
comment|// Family provides specific key material
name|key
operator|=
name|unwrapKey
argument_list|(
name|conf
argument_list|,
name|keyBytes
argument_list|)
expr_stmt|;
comment|// Use the algorithm the key wants
name|cipher
operator|=
name|Encryption
operator|.
name|getCipher
argument_list|(
name|conf
argument_list|,
name|key
operator|.
name|getAlgorithm
argument_list|()
argument_list|)
expr_stmt|;
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
literal|"Cipher '"
operator|+
name|key
operator|.
name|getAlgorithm
argument_list|()
operator|+
literal|"' is not available"
argument_list|)
throw|;
block|}
comment|// Fail if misconfigured
comment|// We use the encryption type specified in the column schema as a sanity check on
comment|// what the wrapped key is telling us
if|if
condition|(
operator|!
name|cipher
operator|.
name|getName
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|cipherName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Encryption for family '"
operator|+
name|family
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|"' configured with type '"
operator|+
name|cipherName
operator|+
literal|"' but key specifies algorithm '"
operator|+
name|cipher
operator|.
name|getName
argument_list|()
operator|+
literal|"'"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
comment|// Family does not provide key material, create a random key
name|cipher
operator|=
name|Encryption
operator|.
name|getCipher
argument_list|(
name|conf
argument_list|,
name|cipherName
argument_list|)
expr_stmt|;
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
literal|"Cipher '"
operator|+
name|cipherName
operator|+
literal|"' is not available"
argument_list|)
throw|;
block|}
name|key
operator|=
name|cipher
operator|.
name|getRandomKey
argument_list|()
expr_stmt|;
block|}
name|cryptoContext
operator|=
name|Encryption
operator|.
name|newContext
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|cryptoContext
operator|.
name|setCipher
argument_list|(
name|cipher
argument_list|)
expr_stmt|;
name|cryptoContext
operator|.
name|setKey
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
return|return
name|cryptoContext
return|;
block|}
comment|/**    * Helper for {@link #unwrapKey(Configuration, String, byte[])} which automatically uses the    * configured master and alternative keys, rather than having to specify a key type to unwrap    * with.    *    * The configuration must be set up correctly for key alias resolution.    *    * @param conf the current configuration    * @param keyBytes the key encrypted by master (or alternative) to unwrap    * @return the key bytes, decrypted    * @throws IOException if the key cannot be unwrapped    */
specifier|public
specifier|static
name|Key
name|unwrapKey
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|byte
index|[]
name|keyBytes
parameter_list|)
throws|throws
name|IOException
block|{
name|Key
name|key
decl_stmt|;
name|String
name|masterKeyName
init|=
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
decl_stmt|;
try|try
block|{
comment|// First try the master key
name|key
operator|=
name|unwrapKey
argument_list|(
name|conf
argument_list|,
name|masterKeyName
argument_list|,
name|keyBytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeyException
name|e
parameter_list|)
block|{
comment|// If the current master key fails to unwrap, try the alternate, if
comment|// one is configured
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Unable to unwrap key with current master key '"
operator|+
name|masterKeyName
operator|+
literal|"'"
argument_list|)
expr_stmt|;
block|}
name|String
name|alternateKeyName
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|CRYPTO_MASTERKEY_ALTERNATE_NAME_CONF_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|alternateKeyName
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|key
operator|=
name|unwrapKey
argument_list|(
name|conf
argument_list|,
name|alternateKeyName
argument_list|,
name|keyBytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeyException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|key
return|;
block|}
comment|/**    * Helper to create an instance of CryptoAES.    *    * @param conf The current configuration.    * @param cryptoCipherMeta The metadata for create CryptoAES.    * @return The instance of CryptoAES.    * @throws IOException if create CryptoAES failed    */
specifier|public
specifier|static
name|CryptoAES
name|createCryptoAES
parameter_list|(
name|RPCProtos
operator|.
name|CryptoCipherMeta
name|cryptoCipherMeta
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|Properties
name|properties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
comment|// the property for cipher class
name|properties
operator|.
name|setProperty
argument_list|(
name|CryptoCipherFactory
operator|.
name|CLASSES_KEY
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.rpc.crypto.encryption.aes.cipher.class"
argument_list|,
literal|"org.apache.commons.crypto.cipher.JceCipher"
argument_list|)
argument_list|)
expr_stmt|;
comment|// create SaslAES for client
return|return
operator|new
name|CryptoAES
argument_list|(
name|cryptoCipherMeta
operator|.
name|getTransformation
argument_list|()
argument_list|,
name|properties
argument_list|,
name|cryptoCipherMeta
operator|.
name|getInKey
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|cryptoCipherMeta
operator|.
name|getOutKey
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|cryptoCipherMeta
operator|.
name|getInIv
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|cryptoCipherMeta
operator|.
name|getOutIv
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

