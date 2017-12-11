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
operator|.
name|aes
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

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
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|AccessController
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
name|PrivilegedAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|Provider
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
name|security
operator|.
name|SecureRandomSpi
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|Security
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
name|Encryptor
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
name|testclassification
operator|.
name|MiscTests
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
name|testclassification
operator|.
name|SmallTests
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
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestCommonsAES
block|{
comment|// Validation for AES in CTR mode with a 128 bit key
comment|// From NIST Special Publication 800-38A
annotation|@
name|Test
specifier|public
name|void
name|testAESAlgorithm
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|Cipher
name|aes
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
name|assertEquals
argument_list|(
name|CommonsCryptoAES
operator|.
name|KEY_LENGTH
argument_list|,
name|aes
operator|.
name|getKeyLength
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|CommonsCryptoAES
operator|.
name|IV_LENGTH
argument_list|,
name|aes
operator|.
name|getIvLength
argument_list|()
argument_list|)
expr_stmt|;
name|Encryptor
name|e
init|=
name|aes
operator|.
name|getEncryptor
argument_list|()
decl_stmt|;
name|e
operator|.
name|setKey
argument_list|(
operator|new
name|SecretKeySpec
argument_list|(
name|Bytes
operator|.
name|fromHex
argument_list|(
literal|"2b7e151628aed2a6abf7158809cf4f3c"
argument_list|)
argument_list|,
literal|"AES"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|.
name|setIv
argument_list|(
name|Bytes
operator|.
name|fromHex
argument_list|(
literal|"f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff"
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
name|cout
operator|.
name|write
argument_list|(
name|Bytes
operator|.
name|fromHex
argument_list|(
literal|"6bc1bee22e409f96e93d7e117393172a"
argument_list|)
argument_list|)
expr_stmt|;
name|cout
operator|.
name|write
argument_list|(
name|Bytes
operator|.
name|fromHex
argument_list|(
literal|"ae2d8a571e03ac9c9eb76fac45af8e51"
argument_list|)
argument_list|)
expr_stmt|;
name|cout
operator|.
name|write
argument_list|(
name|Bytes
operator|.
name|fromHex
argument_list|(
literal|"30c81c46a35ce411e5fbc1191a0a52ef"
argument_list|)
argument_list|)
expr_stmt|;
name|cout
operator|.
name|write
argument_list|(
name|Bytes
operator|.
name|fromHex
argument_list|(
literal|"f69f2445df4f9b17ad2b417be66c3710"
argument_list|)
argument_list|)
expr_stmt|;
name|cout
operator|.
name|close
argument_list|()
expr_stmt|;
name|ByteArrayInputStream
name|in
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|out
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|b
init|=
operator|new
name|byte
index|[
literal|16
index|]
decl_stmt|;
name|IOUtils
operator|.
name|readFully
argument_list|(
name|in
argument_list|,
name|b
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Failed #1"
argument_list|,
name|Bytes
operator|.
name|equals
argument_list|(
name|b
argument_list|,
name|Bytes
operator|.
name|fromHex
argument_list|(
literal|"874d6191b620e3261bef6864990db6ce"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|readFully
argument_list|(
name|in
argument_list|,
name|b
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Failed #2"
argument_list|,
name|Bytes
operator|.
name|equals
argument_list|(
name|b
argument_list|,
name|Bytes
operator|.
name|fromHex
argument_list|(
literal|"9806f66b7970fdff8617187bb9fffdff"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|readFully
argument_list|(
name|in
argument_list|,
name|b
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Failed #3"
argument_list|,
name|Bytes
operator|.
name|equals
argument_list|(
name|b
argument_list|,
name|Bytes
operator|.
name|fromHex
argument_list|(
literal|"5ae4df3edbd5d35e5b4f09020db03eab"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|readFully
argument_list|(
name|in
argument_list|,
name|b
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Failed #4"
argument_list|,
name|Bytes
operator|.
name|equals
argument_list|(
name|b
argument_list|,
name|Bytes
operator|.
name|fromHex
argument_list|(
literal|"1e031dda2fbe03d1792170a0f3009cee"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAlternateRNG
parameter_list|()
throws|throws
name|Exception
block|{
name|Security
operator|.
name|addProvider
argument_list|(
operator|new
name|TestProvider
argument_list|()
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|AES
operator|.
name|RNG_ALGORITHM_KEY
argument_list|,
literal|"TestRNG"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|AES
operator|.
name|RNG_PROVIDER_KEY
argument_list|,
literal|"TEST"
argument_list|)
expr_stmt|;
name|DefaultCipherProvider
operator|.
name|getInstance
argument_list|()
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|AES
name|aes
init|=
operator|new
name|AES
argument_list|(
name|DefaultCipherProvider
operator|.
name|getInstance
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"AES did not find alternate RNG"
argument_list|,
literal|"TestRNG"
argument_list|,
name|aes
operator|.
name|getRNG
argument_list|()
operator|.
name|getAlgorithm
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|static
class|class
name|TestProvider
extends|extends
name|Provider
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
name|TestProvider
parameter_list|()
block|{
name|super
argument_list|(
literal|"TEST"
argument_list|,
literal|1.0
argument_list|,
literal|"Test provider"
argument_list|)
expr_stmt|;
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|run
parameter_list|()
block|{
name|put
argument_list|(
literal|"SecureRandom.TestRNG"
argument_list|,
name|TestCommonsAES
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"$TestRNG"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Must be public for instantiation by the SecureRandom SPI
specifier|public
specifier|static
class|class
name|TestRNG
extends|extends
name|SecureRandomSpi
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|SecureRandom
name|rng
decl_stmt|;
specifier|public
name|TestRNG
parameter_list|()
block|{
try|try
block|{
name|rng
operator|=
name|SecureRandom
operator|.
name|getInstance
argument_list|(
literal|"SHA1PRNG"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchAlgorithmException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Unable to create SecureRandom instance"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|engineSetSeed
parameter_list|(
name|byte
index|[]
name|seed
parameter_list|)
block|{
name|rng
operator|.
name|setSeed
argument_list|(
name|seed
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|engineNextBytes
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
block|{
name|rng
operator|.
name|nextBytes
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|byte
index|[]
name|engineGenerateSeed
parameter_list|(
name|int
name|numBytes
parameter_list|)
block|{
return|return
name|rng
operator|.
name|generateSeed
argument_list|(
name|numBytes
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

