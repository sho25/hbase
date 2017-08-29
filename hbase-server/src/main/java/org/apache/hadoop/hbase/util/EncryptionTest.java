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
name|util
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
name|HBaseInterfaceAudience
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
name|DefaultCipherProvider
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
name|io
operator|.
name|crypto
operator|.
name|KeyStoreKeyProvider
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
name|security
operator|.
name|EncryptionUtil
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|TOOLS
argument_list|)
specifier|public
class|class
name|EncryptionTest
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
name|EncryptionTest
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
name|keyProviderResults
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
name|cipherProviderResults
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
name|cipherResults
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|EncryptionTest
parameter_list|()
block|{   }
comment|/**    * Check that the configured key provider can be loaded and initialized, or    * throw an exception.    *    * @param conf    * @throws IOException    */
specifier|public
specifier|static
name|void
name|testKeyProvider
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
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
name|Boolean
name|result
init|=
name|keyProviderResults
operator|.
name|get
argument_list|(
name|providerClassName
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|Encryption
operator|.
name|getKeyProvider
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|keyProviderResults
operator|.
name|put
argument_list|(
name|providerClassName
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// most likely a RuntimeException
name|keyProviderResults
operator|.
name|put
argument_list|(
name|providerClassName
argument_list|,
literal|false
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Key provider "
operator|+
name|providerClassName
operator|+
literal|" failed test: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|result
operator|.
name|booleanValue
argument_list|()
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Key provider "
operator|+
name|providerClassName
operator|+
literal|" previously failed test"
argument_list|)
throw|;
block|}
block|}
comment|/**    * Check that the configured cipher provider can be loaded and initialized, or    * throw an exception.    *    * @param conf    * @throws IOException    */
specifier|public
specifier|static
name|void
name|testCipherProvider
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
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
name|Boolean
name|result
init|=
name|cipherProviderResults
operator|.
name|get
argument_list|(
name|providerClassName
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|Encryption
operator|.
name|getCipherProvider
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|cipherProviderResults
operator|.
name|put
argument_list|(
name|providerClassName
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// most likely a RuntimeException
name|cipherProviderResults
operator|.
name|put
argument_list|(
name|providerClassName
argument_list|,
literal|false
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cipher provider "
operator|+
name|providerClassName
operator|+
literal|" failed test: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|result
operator|.
name|booleanValue
argument_list|()
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cipher provider "
operator|+
name|providerClassName
operator|+
literal|" previously failed test"
argument_list|)
throw|;
block|}
block|}
comment|/**    * Check that the specified cipher can be loaded and initialized, or throw    * an exception. Verifies key and cipher provider configuration as a    * prerequisite for cipher verification.    *    * @param conf    * @param cipher    * @param key    * @throws IOException    */
specifier|public
specifier|static
name|void
name|testEncryption
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|String
name|cipher
parameter_list|,
name|byte
index|[]
name|key
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|cipher
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|testKeyProvider
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|testCipherProvider
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Boolean
name|result
init|=
name|cipherResults
operator|.
name|get
argument_list|(
name|cipher
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|Encryption
operator|.
name|Context
name|context
init|=
name|Encryption
operator|.
name|newContext
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|context
operator|.
name|setCipher
argument_list|(
name|Encryption
operator|.
name|getCipher
argument_list|(
name|conf
argument_list|,
name|cipher
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|key
operator|==
literal|null
condition|)
block|{
comment|// Make a random key since one was not provided
name|context
operator|.
name|setKey
argument_list|(
name|context
operator|.
name|getCipher
argument_list|()
operator|.
name|getRandomKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// This will be a wrapped key from schema
name|context
operator|.
name|setKey
argument_list|(
name|EncryptionUtil
operator|.
name|unwrapKey
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
literal|"hbase"
argument_list|)
argument_list|,
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|iv
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|getCipher
argument_list|()
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
name|context
operator|.
name|getCipher
argument_list|()
operator|.
name|getIvLength
argument_list|()
index|]
expr_stmt|;
name|Bytes
operator|.
name|random
argument_list|(
name|iv
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|plaintext
init|=
operator|new
name|byte
index|[
literal|1024
index|]
decl_stmt|;
name|Bytes
operator|.
name|random
argument_list|(
name|plaintext
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
name|encrypt
argument_list|(
name|out
argument_list|,
operator|new
name|ByteArrayInputStream
argument_list|(
name|plaintext
argument_list|)
argument_list|,
name|context
argument_list|,
name|iv
argument_list|)
expr_stmt|;
name|byte
index|[]
name|ciphertext
init|=
name|out
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|out
operator|.
name|reset
argument_list|()
expr_stmt|;
name|Encryption
operator|.
name|decrypt
argument_list|(
name|out
argument_list|,
operator|new
name|ByteArrayInputStream
argument_list|(
name|ciphertext
argument_list|)
argument_list|,
name|plaintext
operator|.
name|length
argument_list|,
name|context
argument_list|,
name|iv
argument_list|)
expr_stmt|;
name|byte
index|[]
name|test
init|=
name|out
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|plaintext
argument_list|,
name|test
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Did not pass encrypt/decrypt test"
argument_list|)
throw|;
block|}
name|cipherResults
operator|.
name|put
argument_list|(
name|cipher
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|cipherResults
operator|.
name|put
argument_list|(
name|cipher
argument_list|,
literal|false
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cipher "
operator|+
name|cipher
operator|+
literal|" failed test: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|result
operator|.
name|booleanValue
argument_list|()
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cipher "
operator|+
name|cipher
operator|+
literal|" previously failed test"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

