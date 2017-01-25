begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations under  * the License.  */
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
name|DigestException
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
name|MessageDigest
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|NoSuchAlgorithmException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|spec
operator|.
name|InvalidKeySpecException
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
name|concurrent
operator|.
name|ConcurrentHashMap
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|crypto
operator|.
name|SecretKeyFactory
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
name|PBEKeySpec
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
name|io
operator|.
name|IOUtils
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
name|HBaseConfiguration
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
name|hbase
operator|.
name|util
operator|.
name|Pair
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * A facade for encryption algorithms and related support.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|final
class|class
name|Encryption
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
name|Encryption
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Crypto context    */
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|static
class|class
name|Context
extends|extends
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
block|{
comment|/** The null crypto context */
specifier|public
specifier|static
specifier|final
name|Context
name|NONE
init|=
operator|new
name|Context
argument_list|()
decl_stmt|;
specifier|private
name|Context
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|private
name|Context
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Context
name|setCipher
parameter_list|(
name|Cipher
name|cipher
parameter_list|)
block|{
name|super
operator|.
name|setCipher
argument_list|(
name|cipher
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|Context
name|setKey
parameter_list|(
name|Key
name|key
parameter_list|)
block|{
name|super
operator|.
name|setKey
argument_list|(
name|key
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Context
name|setKey
parameter_list|(
name|byte
index|[]
name|key
parameter_list|)
block|{
name|super
operator|.
name|setKey
argument_list|(
operator|new
name|SecretKeySpec
argument_list|(
name|key
argument_list|,
name|getCipher
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
specifier|public
specifier|static
name|Context
name|newContext
parameter_list|()
block|{
return|return
operator|new
name|Context
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|Context
name|newContext
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
operator|new
name|Context
argument_list|(
name|conf
argument_list|)
return|;
block|}
comment|// Prevent instantiation
specifier|private
name|Encryption
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Get an cipher given a name    * @param name the cipher name    * @return the cipher, or null if a suitable one could not be found    */
specifier|public
specifier|static
name|Cipher
name|getCipher
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|name
parameter_list|)
block|{
return|return
name|getCipherProvider
argument_list|(
name|conf
argument_list|)
operator|.
name|getCipher
argument_list|(
name|name
argument_list|)
return|;
block|}
comment|/**    * Get names of supported encryption algorithms    *    * @return Array of strings, each represents a supported encryption algorithm    */
specifier|public
specifier|static
name|String
index|[]
name|getSupportedCiphers
parameter_list|()
block|{
return|return
name|getSupportedCiphers
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Get names of supported encryption algorithms    *    * @return Array of strings, each represents a supported encryption algorithm    */
specifier|public
specifier|static
name|String
index|[]
name|getSupportedCiphers
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|getCipherProvider
argument_list|(
name|conf
argument_list|)
operator|.
name|getSupportedCiphers
argument_list|()
return|;
block|}
comment|/**    * Return the MD5 digest of the concatenation of the supplied arguments.    */
specifier|public
specifier|static
name|byte
index|[]
name|hash128
parameter_list|(
name|String
modifier|...
name|args
parameter_list|)
block|{
name|byte
index|[]
name|result
init|=
operator|new
name|byte
index|[
literal|16
index|]
decl_stmt|;
try|try
block|{
name|MessageDigest
name|md
init|=
name|MessageDigest
operator|.
name|getInstance
argument_list|(
literal|"MD5"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|arg
range|:
name|args
control|)
block|{
name|md
operator|.
name|update
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|arg
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|md
operator|.
name|digest
argument_list|(
name|result
argument_list|,
literal|0
argument_list|,
name|result
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchAlgorithmException
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
catch|catch
parameter_list|(
name|DigestException
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
comment|/**    * Return the MD5 digest of the concatenation of the supplied arguments.    */
specifier|public
specifier|static
name|byte
index|[]
name|hash128
parameter_list|(
name|byte
index|[]
modifier|...
name|args
parameter_list|)
block|{
name|byte
index|[]
name|result
init|=
operator|new
name|byte
index|[
literal|16
index|]
decl_stmt|;
try|try
block|{
name|MessageDigest
name|md
init|=
name|MessageDigest
operator|.
name|getInstance
argument_list|(
literal|"MD5"
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|arg
range|:
name|args
control|)
block|{
name|md
operator|.
name|update
argument_list|(
name|arg
argument_list|)
expr_stmt|;
block|}
name|md
operator|.
name|digest
argument_list|(
name|result
argument_list|,
literal|0
argument_list|,
name|result
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchAlgorithmException
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
catch|catch
parameter_list|(
name|DigestException
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
comment|/**    * Return the SHA-256 digest of the concatenation of the supplied arguments.    */
specifier|public
specifier|static
name|byte
index|[]
name|hash256
parameter_list|(
name|String
modifier|...
name|args
parameter_list|)
block|{
name|byte
index|[]
name|result
init|=
operator|new
name|byte
index|[
literal|32
index|]
decl_stmt|;
try|try
block|{
name|MessageDigest
name|md
init|=
name|MessageDigest
operator|.
name|getInstance
argument_list|(
literal|"SHA-256"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|arg
range|:
name|args
control|)
block|{
name|md
operator|.
name|update
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|arg
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|md
operator|.
name|digest
argument_list|(
name|result
argument_list|,
literal|0
argument_list|,
name|result
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchAlgorithmException
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
catch|catch
parameter_list|(
name|DigestException
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
comment|/**    * Return the SHA-256 digest of the concatenation of the supplied arguments.    */
specifier|public
specifier|static
name|byte
index|[]
name|hash256
parameter_list|(
name|byte
index|[]
modifier|...
name|args
parameter_list|)
block|{
name|byte
index|[]
name|result
init|=
operator|new
name|byte
index|[
literal|32
index|]
decl_stmt|;
try|try
block|{
name|MessageDigest
name|md
init|=
name|MessageDigest
operator|.
name|getInstance
argument_list|(
literal|"SHA-256"
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|arg
range|:
name|args
control|)
block|{
name|md
operator|.
name|update
argument_list|(
name|arg
argument_list|)
expr_stmt|;
block|}
name|md
operator|.
name|digest
argument_list|(
name|result
argument_list|,
literal|0
argument_list|,
name|result
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchAlgorithmException
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
catch|catch
parameter_list|(
name|DigestException
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
comment|/**    * Return a 128 bit key derived from the concatenation of the supplied    * arguments using PBKDF2WithHmacSHA1 at 10,000 iterations.    *     */
specifier|public
specifier|static
name|byte
index|[]
name|pbkdf128
parameter_list|(
name|String
modifier|...
name|args
parameter_list|)
block|{
name|byte
index|[]
name|salt
init|=
operator|new
name|byte
index|[
literal|128
index|]
decl_stmt|;
name|Bytes
operator|.
name|random
argument_list|(
name|salt
argument_list|)
expr_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|s
range|:
name|args
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
name|PBEKeySpec
name|spec
init|=
operator|new
name|PBEKeySpec
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
operator|.
name|toCharArray
argument_list|()
argument_list|,
name|salt
argument_list|,
literal|10000
argument_list|,
literal|128
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|SecretKeyFactory
operator|.
name|getInstance
argument_list|(
literal|"PBKDF2WithHmacSHA1"
argument_list|)
operator|.
name|generateSecret
argument_list|(
name|spec
argument_list|)
operator|.
name|getEncoded
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchAlgorithmException
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
catch|catch
parameter_list|(
name|InvalidKeySpecException
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
comment|/**    * Return a 128 bit key derived from the concatenation of the supplied    * arguments using PBKDF2WithHmacSHA1 at 10,000 iterations.    *     */
specifier|public
specifier|static
name|byte
index|[]
name|pbkdf128
parameter_list|(
name|byte
index|[]
modifier|...
name|args
parameter_list|)
block|{
name|byte
index|[]
name|salt
init|=
operator|new
name|byte
index|[
literal|128
index|]
decl_stmt|;
name|Bytes
operator|.
name|random
argument_list|(
name|salt
argument_list|)
expr_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|b
range|:
name|args
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|Arrays
operator|.
name|toString
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|PBEKeySpec
name|spec
init|=
operator|new
name|PBEKeySpec
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
operator|.
name|toCharArray
argument_list|()
argument_list|,
name|salt
argument_list|,
literal|10000
argument_list|,
literal|128
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|SecretKeyFactory
operator|.
name|getInstance
argument_list|(
literal|"PBKDF2WithHmacSHA1"
argument_list|)
operator|.
name|generateSecret
argument_list|(
name|spec
argument_list|)
operator|.
name|getEncoded
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchAlgorithmException
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
catch|catch
parameter_list|(
name|InvalidKeySpecException
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
comment|/**    * Encrypt a block of plaintext    *<p>    * The encryptor's state will be finalized. It should be reinitialized or    * returned to the pool.    * @param out ciphertext    * @param src plaintext    * @param offset    * @param length    * @param e    * @throws IOException     */
specifier|public
specifier|static
name|void
name|encrypt
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|byte
index|[]
name|src
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|Encryptor
name|e
parameter_list|)
throws|throws
name|IOException
block|{
name|OutputStream
name|cout
init|=
name|e
operator|.
name|createEncryptionStream
argument_list|(
name|out
argument_list|)
decl_stmt|;
try|try
block|{
name|cout
operator|.
name|write
argument_list|(
name|src
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cout
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Encrypt a block of plaintext    * @param out ciphertext    * @param src plaintext    * @param offset    * @param length    * @param context    * @param iv    * @throws IOException     */
specifier|public
specifier|static
name|void
name|encrypt
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|byte
index|[]
name|src
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
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
name|Encryptor
name|e
init|=
name|context
operator|.
name|getCipher
argument_list|()
operator|.
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
comment|// can be null
name|e
operator|.
name|reset
argument_list|()
expr_stmt|;
name|encrypt
argument_list|(
name|out
argument_list|,
name|src
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
comment|/**    * Encrypt a stream of plaintext given an encryptor    *<p>    * The encryptor's state will be finalized. It should be reinitialized or    * returned to the pool.    * @param out ciphertext    * @param in plaintext    * @param e    * @throws IOException    */
specifier|public
specifier|static
name|void
name|encrypt
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|InputStream
name|in
parameter_list|,
name|Encryptor
name|e
parameter_list|)
throws|throws
name|IOException
block|{
name|OutputStream
name|cout
init|=
name|e
operator|.
name|createEncryptionStream
argument_list|(
name|out
argument_list|)
decl_stmt|;
try|try
block|{
name|IOUtils
operator|.
name|copy
argument_list|(
name|in
argument_list|,
name|cout
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cout
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Encrypt a stream of plaintext given a context and IV    * @param out ciphertext    * @param in plaintet    * @param context    * @param iv    * @throws IOException    */
specifier|public
specifier|static
name|void
name|encrypt
parameter_list|(
name|OutputStream
name|out
parameter_list|,
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
name|Encryptor
name|e
init|=
name|context
operator|.
name|getCipher
argument_list|()
operator|.
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
comment|// can be null
name|e
operator|.
name|reset
argument_list|()
expr_stmt|;
name|encrypt
argument_list|(
name|out
argument_list|,
name|in
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
comment|/**    * Decrypt a block of ciphertext read in from a stream with the given    * cipher and context    *<p>    * The decryptor's state will be finalized. It should be reinitialized or    * returned to the pool.    * @param dest    * @param destOffset    * @param in    * @param destSize    * @param d    * @throws IOException    */
specifier|public
specifier|static
name|void
name|decrypt
parameter_list|(
name|byte
index|[]
name|dest
parameter_list|,
name|int
name|destOffset
parameter_list|,
name|InputStream
name|in
parameter_list|,
name|int
name|destSize
parameter_list|,
name|Decryptor
name|d
parameter_list|)
throws|throws
name|IOException
block|{
name|InputStream
name|cin
init|=
name|d
operator|.
name|createDecryptionStream
argument_list|(
name|in
argument_list|)
decl_stmt|;
try|try
block|{
name|IOUtils
operator|.
name|readFully
argument_list|(
name|cin
argument_list|,
name|dest
argument_list|,
name|destOffset
argument_list|,
name|destSize
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Decrypt a block of ciphertext from a stream given a context and IV    * @param dest    * @param destOffset    * @param in    * @param destSize    * @param context    * @param iv    * @throws IOException    */
specifier|public
specifier|static
name|void
name|decrypt
parameter_list|(
name|byte
index|[]
name|dest
parameter_list|,
name|int
name|destOffset
parameter_list|,
name|InputStream
name|in
parameter_list|,
name|int
name|destSize
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
name|Decryptor
name|d
init|=
name|context
operator|.
name|getCipher
argument_list|()
operator|.
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
comment|// can be null
name|decrypt
argument_list|(
name|dest
argument_list|,
name|destOffset
argument_list|,
name|in
argument_list|,
name|destSize
argument_list|,
name|d
argument_list|)
expr_stmt|;
block|}
comment|/**    * Decrypt a stream of ciphertext given a decryptor    * @param out    * @param in    * @param outLen    * @param d    * @throws IOException    */
specifier|public
specifier|static
name|void
name|decrypt
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|InputStream
name|in
parameter_list|,
name|int
name|outLen
parameter_list|,
name|Decryptor
name|d
parameter_list|)
throws|throws
name|IOException
block|{
name|InputStream
name|cin
init|=
name|d
operator|.
name|createDecryptionStream
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|byte
name|buf
index|[]
init|=
operator|new
name|byte
index|[
literal|8
operator|*
literal|1024
index|]
decl_stmt|;
name|long
name|remaining
init|=
name|outLen
decl_stmt|;
try|try
block|{
while|while
condition|(
name|remaining
operator|>
literal|0
condition|)
block|{
name|int
name|toRead
init|=
call|(
name|int
call|)
argument_list|(
name|remaining
operator|<
name|buf
operator|.
name|length
condition|?
name|remaining
else|:
name|buf
operator|.
name|length
argument_list|)
decl_stmt|;
name|int
name|read
init|=
name|cin
operator|.
name|read
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|toRead
argument_list|)
decl_stmt|;
if|if
condition|(
name|read
operator|<
literal|0
condition|)
block|{
break|break;
block|}
name|out
operator|.
name|write
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|read
argument_list|)
expr_stmt|;
name|remaining
operator|-=
name|read
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|cin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Decrypt a stream of ciphertext given a context and IV    * @param out    * @param in    * @param outLen    * @param context    * @param iv    * @throws IOException    */
specifier|public
specifier|static
name|void
name|decrypt
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|InputStream
name|in
parameter_list|,
name|int
name|outLen
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
name|Decryptor
name|d
init|=
name|context
operator|.
name|getCipher
argument_list|()
operator|.
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
comment|// can be null
name|decrypt
argument_list|(
name|out
argument_list|,
name|in
argument_list|,
name|outLen
argument_list|,
name|d
argument_list|)
expr_stmt|;
block|}
comment|/**    * Resolves a key for the given subject    * @param subject    * @param conf    * @return a key for the given subject    * @throws IOException if the key is not found    */
specifier|public
specifier|static
name|Key
name|getSecretKeyForSubject
parameter_list|(
name|String
name|subject
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|KeyProvider
name|provider
init|=
operator|(
name|KeyProvider
operator|)
name|getKeyProvider
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|provider
operator|!=
literal|null
condition|)
try|try
block|{
name|Key
index|[]
name|keys
init|=
name|provider
operator|.
name|getKeys
argument_list|(
operator|new
name|String
index|[]
block|{
name|subject
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
name|keys
operator|!=
literal|null
operator|&&
name|keys
operator|.
name|length
operator|>
literal|0
condition|)
block|{
return|return
name|keys
index|[
literal|0
index|]
return|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"No key found for subject '"
operator|+
name|subject
operator|+
literal|"'"
argument_list|)
throw|;
block|}
comment|/**    * Encrypts a block of plaintext with the symmetric key resolved for the given subject    * @param out ciphertext    * @param in plaintext    * @param conf configuration    * @param cipher the encryption algorithm    * @param iv the initialization vector, can be null    * @throws IOException    */
specifier|public
specifier|static
name|void
name|encryptWithSubjectKey
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|InputStream
name|in
parameter_list|,
name|String
name|subject
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|Cipher
name|cipher
parameter_list|,
name|byte
index|[]
name|iv
parameter_list|)
throws|throws
name|IOException
block|{
name|Key
name|key
init|=
name|getSecretKeyForSubject
argument_list|(
name|subject
argument_list|,
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|key
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"No key found for subject '"
operator|+
name|subject
operator|+
literal|"'"
argument_list|)
throw|;
block|}
name|Encryptor
name|e
init|=
name|cipher
operator|.
name|getEncryptor
argument_list|()
decl_stmt|;
name|e
operator|.
name|setKey
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|e
operator|.
name|setIv
argument_list|(
name|iv
argument_list|)
expr_stmt|;
comment|// can be null
name|encrypt
argument_list|(
name|out
argument_list|,
name|in
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
comment|/**    * Decrypts a block of ciphertext with the symmetric key resolved for the given subject    * @param out plaintext    * @param in ciphertext    * @param outLen the expected plaintext length    * @param subject the subject's key alias    * @param conf configuration    * @param cipher the encryption algorithm    * @param iv the initialization vector, can be null    * @throws IOException    */
specifier|public
specifier|static
name|void
name|decryptWithSubjectKey
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|InputStream
name|in
parameter_list|,
name|int
name|outLen
parameter_list|,
name|String
name|subject
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|Cipher
name|cipher
parameter_list|,
name|byte
index|[]
name|iv
parameter_list|)
throws|throws
name|IOException
block|{
name|Key
name|key
init|=
name|getSecretKeyForSubject
argument_list|(
name|subject
argument_list|,
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|key
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"No key found for subject '"
operator|+
name|subject
operator|+
literal|"'"
argument_list|)
throw|;
block|}
name|Decryptor
name|d
init|=
name|cipher
operator|.
name|getDecryptor
argument_list|()
decl_stmt|;
name|d
operator|.
name|setKey
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|d
operator|.
name|setIv
argument_list|(
name|iv
argument_list|)
expr_stmt|;
comment|// can be null
try|try
block|{
name|decrypt
argument_list|(
name|out
argument_list|,
name|in
argument_list|,
name|outLen
argument_list|,
name|d
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// If the current cipher algorithm fails to unwrap, try the alternate cipher algorithm, if one
comment|// is configured
name|String
name|alternateAlgorithm
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|CRYPTO_ALTERNATE_KEY_ALGORITHM_CONF_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|alternateAlgorithm
operator|!=
literal|null
condition|)
block|{
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
literal|"Unable to decrypt data with current cipher algorithm '"
operator|+
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
operator|+
literal|"'. Trying with the alternate cipher algorithm '"
operator|+
name|alternateAlgorithm
operator|+
literal|"' configured."
argument_list|)
expr_stmt|;
block|}
name|Cipher
name|alterCipher
init|=
name|Encryption
operator|.
name|getCipher
argument_list|(
name|conf
argument_list|,
name|alternateAlgorithm
argument_list|)
decl_stmt|;
if|if
condition|(
name|alterCipher
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
name|alternateAlgorithm
operator|+
literal|"' not available"
argument_list|)
throw|;
block|}
name|d
operator|=
name|alterCipher
operator|.
name|getDecryptor
argument_list|()
expr_stmt|;
name|d
operator|.
name|setKey
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|d
operator|.
name|setIv
argument_list|(
name|iv
argument_list|)
expr_stmt|;
comment|// can be null
name|decrypt
argument_list|(
name|out
argument_list|,
name|in
argument_list|,
name|outLen
argument_list|,
name|d
argument_list|)
expr_stmt|;
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
block|}
specifier|private
specifier|static
name|ClassLoader
name|getClassLoaderForClass
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
name|ClassLoader
name|cl
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getContextClassLoader
argument_list|()
decl_stmt|;
if|if
condition|(
name|cl
operator|==
literal|null
condition|)
block|{
name|cl
operator|=
name|c
operator|.
name|getClassLoader
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|cl
operator|==
literal|null
condition|)
block|{
name|cl
operator|=
name|ClassLoader
operator|.
name|getSystemClassLoader
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|cl
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"A ClassLoader to load the Cipher could not be determined"
argument_list|)
throw|;
block|}
return|return
name|cl
return|;
block|}
specifier|public
specifier|static
name|CipherProvider
name|getCipherProvider
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|providerClassName
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|CRYPTO_CIPHERPROVIDER_CONF_KEY
argument_list|,
name|DefaultCipherProvider
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|CipherProvider
name|provider
init|=
operator|(
name|CipherProvider
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|getClassLoaderForClass
argument_list|(
name|CipherProvider
operator|.
name|class
argument_list|)
operator|.
name|loadClass
argument_list|(
name|providerClassName
argument_list|)
argument_list|,
name|conf
argument_list|)
decl_stmt|;
return|return
name|provider
return|;
block|}
catch|catch
parameter_list|(
name|Exception
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
specifier|static
specifier|final
name|Map
argument_list|<
name|Pair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|,
name|KeyProvider
argument_list|>
name|keyProviderCache
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|KeyProvider
name|getKeyProvider
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|providerClassName
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|CRYPTO_KEYPROVIDER_CONF_KEY
argument_list|,
name|KeyStoreKeyProvider
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|providerParameters
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|CRYPTO_KEYPROVIDER_PARAMETERS_KEY
argument_list|,
literal|""
argument_list|)
decl_stmt|;
try|try
block|{
name|Pair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|providerCacheKey
init|=
operator|new
name|Pair
argument_list|<>
argument_list|(
name|providerClassName
argument_list|,
name|providerParameters
argument_list|)
decl_stmt|;
name|KeyProvider
name|provider
init|=
name|keyProviderCache
operator|.
name|get
argument_list|(
name|providerCacheKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|provider
operator|!=
literal|null
condition|)
block|{
return|return
name|provider
return|;
block|}
name|provider
operator|=
operator|(
name|KeyProvider
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|getClassLoaderForClass
argument_list|(
name|KeyProvider
operator|.
name|class
argument_list|)
operator|.
name|loadClass
argument_list|(
name|providerClassName
argument_list|)
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|provider
operator|.
name|init
argument_list|(
name|providerParameters
argument_list|)
expr_stmt|;
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
literal|"Installed "
operator|+
name|providerClassName
operator|+
literal|" into key provider cache"
argument_list|)
expr_stmt|;
block|}
name|keyProviderCache
operator|.
name|put
argument_list|(
name|providerCacheKey
argument_list|,
name|provider
argument_list|)
expr_stmt|;
return|return
name|provider
return|;
block|}
catch|catch
parameter_list|(
name|Exception
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
specifier|public
specifier|static
name|void
name|incrementIv
parameter_list|(
name|byte
index|[]
name|iv
parameter_list|)
block|{
name|incrementIv
argument_list|(
name|iv
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|incrementIv
parameter_list|(
name|byte
index|[]
name|iv
parameter_list|,
name|int
name|v
parameter_list|)
block|{
name|int
name|length
init|=
name|iv
operator|.
name|length
decl_stmt|;
name|boolean
name|carry
init|=
literal|true
decl_stmt|;
comment|// TODO: Optimize for v> 1, e.g. 16, 32
do|do
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|carry
condition|)
block|{
name|iv
index|[
name|i
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
operator|(
name|iv
index|[
name|i
index|]
operator|+
literal|1
operator|)
operator|&
literal|0xFF
argument_list|)
expr_stmt|;
name|carry
operator|=
literal|0
operator|==
name|iv
index|[
name|i
index|]
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
block|}
name|v
operator|--
expr_stmt|;
block|}
do|while
condition|(
name|v
operator|>
literal|0
condition|)
do|;
block|}
block|}
end_class

end_unit

