begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hfile
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
name|assertFalse
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
name|assertNotNull
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
name|DataInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|SecureRandom
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|fs
operator|.
name|FSDataInputStream
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
name|fs
operator|.
name|FSDataOutputStream
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|Cell
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
name|HBaseClassTestRule
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
name|HBaseTestingUtility
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
name|KeyValue
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
name|KeyValueUtil
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
name|ByteBuffAllocator
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
name|compress
operator|.
name|Compression
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
name|io
operator|.
name|crypto
operator|.
name|KeyProviderForTesting
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
name|encoding
operator|.
name|DataBlockEncoding
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
name|IOTests
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|RedundantKVGenerator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|IOTests
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
name|TestHFileEncryption
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestHFileEncryption
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TestHFileEncryption
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|SecureRandom
name|RNG
init|=
operator|new
name|SecureRandom
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|static
name|Encryption
operator|.
name|Context
name|cryptoContext
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// Disable block cache in this test.
name|conf
operator|.
name|setFloat
argument_list|(
name|HConstants
operator|.
name|HFILE_BLOCK_CACHE_SIZE_KEY
argument_list|,
literal|0.0f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|CRYPTO_KEYPROVIDER_CONF_KEY
argument_list|,
name|KeyProviderForTesting
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|CRYPTO_MASTERKEY_NAME_CONF_KEY
argument_list|,
literal|"hbase"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hfile.format.version"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|cryptoContext
operator|=
name|Encryption
operator|.
name|newContext
argument_list|(
name|conf
argument_list|)
expr_stmt|;
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
name|aes
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
name|assertNotNull
argument_list|(
name|aes
argument_list|)
expr_stmt|;
name|cryptoContext
operator|.
name|setCipher
argument_list|(
name|aes
argument_list|)
expr_stmt|;
name|byte
index|[]
name|key
init|=
operator|new
name|byte
index|[
name|aes
operator|.
name|getKeyLength
argument_list|()
index|]
decl_stmt|;
name|RNG
operator|.
name|nextBytes
argument_list|(
name|key
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
specifier|private
name|int
name|writeBlock
parameter_list|(
name|FSDataOutputStream
name|os
parameter_list|,
name|HFileContext
name|fileContext
parameter_list|,
name|int
name|size
parameter_list|)
throws|throws
name|IOException
block|{
name|HFileBlock
operator|.
name|Writer
name|hbw
init|=
operator|new
name|HFileBlock
operator|.
name|Writer
argument_list|(
literal|null
argument_list|,
name|fileContext
argument_list|)
decl_stmt|;
name|DataOutputStream
name|dos
init|=
name|hbw
operator|.
name|startWriting
argument_list|(
name|BlockType
operator|.
name|DATA
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|size
condition|;
name|j
operator|++
control|)
block|{
name|dos
operator|.
name|writeInt
argument_list|(
name|j
argument_list|)
expr_stmt|;
block|}
name|hbw
operator|.
name|writeHeaderAndData
argument_list|(
name|os
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Wrote a block at "
operator|+
name|os
operator|.
name|getPos
argument_list|()
operator|+
literal|" with"
operator|+
literal|" onDiskSizeWithHeader="
operator|+
name|hbw
operator|.
name|getOnDiskSizeWithHeader
argument_list|()
operator|+
literal|" uncompressedSizeWithoutHeader="
operator|+
name|hbw
operator|.
name|getOnDiskSizeWithoutHeader
argument_list|()
operator|+
literal|" uncompressedSizeWithoutHeader="
operator|+
name|hbw
operator|.
name|getUncompressedSizeWithoutHeader
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|hbw
operator|.
name|getOnDiskSizeWithHeader
argument_list|()
return|;
block|}
specifier|private
name|long
name|readAndVerifyBlock
parameter_list|(
name|long
name|pos
parameter_list|,
name|HFileContext
name|ctx
parameter_list|,
name|HFileBlock
operator|.
name|FSReaderImpl
name|hbr
parameter_list|,
name|int
name|size
parameter_list|)
throws|throws
name|IOException
block|{
name|HFileBlock
name|b
init|=
name|hbr
operator|.
name|readBlockData
argument_list|(
name|pos
argument_list|,
operator|-
literal|1
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|HFile
operator|.
name|getAndResetChecksumFailuresCount
argument_list|()
argument_list|)
expr_stmt|;
name|b
operator|.
name|sanityCheck
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|b
operator|.
name|isUnpacked
argument_list|()
argument_list|)
expr_stmt|;
name|b
operator|=
name|b
operator|.
name|unpack
argument_list|(
name|ctx
argument_list|,
name|hbr
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Read a block at "
operator|+
name|pos
operator|+
literal|" with"
operator|+
literal|" onDiskSizeWithHeader="
operator|+
name|b
operator|.
name|getOnDiskSizeWithHeader
argument_list|()
operator|+
literal|" uncompressedSizeWithoutHeader="
operator|+
name|b
operator|.
name|getOnDiskSizeWithoutHeader
argument_list|()
operator|+
literal|" uncompressedSizeWithoutHeader="
operator|+
name|b
operator|.
name|getUncompressedSizeWithoutHeader
argument_list|()
argument_list|)
expr_stmt|;
name|DataInputStream
name|dis
init|=
name|b
operator|.
name|getByteStream
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|int
name|read
init|=
name|dis
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|read
operator|!=
name|i
condition|)
block|{
name|fail
argument_list|(
literal|"Block data corrupt at element "
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|b
operator|.
name|getOnDiskSizeWithHeader
argument_list|()
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDataBlockEncryption
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|int
name|blocks
init|=
literal|10
decl_stmt|;
specifier|final
name|int
index|[]
name|blockSizes
init|=
operator|new
name|int
index|[
name|blocks
index|]
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
name|blocks
condition|;
name|i
operator|++
control|)
block|{
name|blockSizes
index|[
name|i
index|]
operator|=
operator|(
literal|1024
operator|+
name|RNG
operator|.
name|nextInt
argument_list|(
literal|1024
operator|*
literal|63
argument_list|)
operator|)
operator|/
name|Bytes
operator|.
name|SIZEOF_INT
expr_stmt|;
block|}
for|for
control|(
name|Compression
operator|.
name|Algorithm
name|compression
range|:
name|TestHFileBlock
operator|.
name|COMPRESSION_ALGORITHMS
control|)
block|{
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
literal|"block_v3_"
operator|+
name|compression
operator|+
literal|"_AES"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"testDataBlockEncryption: encryption=AES compression="
operator|+
name|compression
argument_list|)
expr_stmt|;
name|long
name|totalSize
init|=
literal|0
decl_stmt|;
name|HFileContext
name|fileContext
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withCompression
argument_list|(
name|compression
argument_list|)
operator|.
name|withEncryptionContext
argument_list|(
name|cryptoContext
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|FSDataOutputStream
name|os
init|=
name|fs
operator|.
name|create
argument_list|(
name|path
argument_list|)
decl_stmt|;
try|try
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
name|blocks
condition|;
name|i
operator|++
control|)
block|{
name|totalSize
operator|+=
name|writeBlock
argument_list|(
name|os
argument_list|,
name|fileContext
argument_list|,
name|blockSizes
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|os
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|FSDataInputStream
name|is
init|=
name|fs
operator|.
name|open
argument_list|(
name|path
argument_list|)
decl_stmt|;
try|try
block|{
name|HFileBlock
operator|.
name|FSReaderImpl
name|hbr
init|=
operator|new
name|HFileBlock
operator|.
name|FSReaderImpl
argument_list|(
name|is
argument_list|,
name|totalSize
argument_list|,
name|fileContext
argument_list|,
name|ByteBuffAllocator
operator|.
name|HEAP
argument_list|)
decl_stmt|;
name|long
name|pos
init|=
literal|0
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
name|blocks
condition|;
name|i
operator|++
control|)
block|{
name|pos
operator|+=
name|readAndVerifyBlock
argument_list|(
name|pos
argument_list|,
name|fileContext
argument_list|,
name|hbr
argument_list|,
name|blockSizes
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|is
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHFileEncryptionMetadata
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|CacheConfig
name|cacheConf
init|=
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|HFileContext
name|fileContext
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withEncryptionContext
argument_list|(
name|cryptoContext
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// write a simple encrypted hfile
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
literal|"cryptometa.hfile"
argument_list|)
decl_stmt|;
name|FSDataOutputStream
name|out
init|=
name|fs
operator|.
name|create
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|HFile
operator|.
name|Writer
name|writer
init|=
name|HFile
operator|.
name|getWriterFactory
argument_list|(
name|conf
argument_list|,
name|cacheConf
argument_list|)
operator|.
name|withOutputStream
argument_list|(
name|out
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|fileContext
argument_list|)
operator|.
name|create
argument_list|()
decl_stmt|;
try|try
block|{
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
argument_list|)
argument_list|,
literal|null
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
argument_list|)
decl_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// read it back in and validate correct crypto metadata
name|HFile
operator|.
name|Reader
name|reader
init|=
name|HFile
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|cacheConf
argument_list|,
literal|true
argument_list|,
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|reader
operator|.
name|loadFileInfo
argument_list|()
expr_stmt|;
name|FixedFileTrailer
name|trailer
init|=
name|reader
operator|.
name|getTrailer
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|trailer
operator|.
name|getEncryptionKey
argument_list|()
argument_list|)
expr_stmt|;
name|Encryption
operator|.
name|Context
name|readerContext
init|=
name|reader
operator|.
name|getFileContext
argument_list|()
operator|.
name|getEncryptionContext
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|readerContext
operator|.
name|getCipher
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|cryptoContext
operator|.
name|getCipher
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|readerContext
operator|.
name|getKeyBytes
argument_list|()
argument_list|,
name|cryptoContext
operator|.
name|getKeyBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHFileEncryption
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Create 1000 random test KVs
name|RedundantKVGenerator
name|generator
init|=
operator|new
name|RedundantKVGenerator
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|testKvs
init|=
name|generator
operator|.
name|generateTestKeyValues
argument_list|(
literal|1000
argument_list|)
decl_stmt|;
comment|// Iterate through data block encoding and compression combinations
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|CacheConfig
name|cacheConf
init|=
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
decl_stmt|;
for|for
control|(
name|DataBlockEncoding
name|encoding
range|:
name|DataBlockEncoding
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|Compression
operator|.
name|Algorithm
name|compression
range|:
name|TestHFileBlock
operator|.
name|COMPRESSION_ALGORITHMS
control|)
block|{
name|HFileContext
name|fileContext
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withBlockSize
argument_list|(
literal|4096
argument_list|)
comment|// small blocks
operator|.
name|withEncryptionContext
argument_list|(
name|cryptoContext
argument_list|)
operator|.
name|withCompression
argument_list|(
name|compression
argument_list|)
operator|.
name|withDataBlockEncoding
argument_list|(
name|encoding
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// write a new test HFile
name|LOG
operator|.
name|info
argument_list|(
literal|"Writing with "
operator|+
name|fileContext
argument_list|)
expr_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getRandomUUID
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|".hfile"
argument_list|)
decl_stmt|;
name|FSDataOutputStream
name|out
init|=
name|fs
operator|.
name|create
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|HFile
operator|.
name|Writer
name|writer
init|=
name|HFile
operator|.
name|getWriterFactory
argument_list|(
name|conf
argument_list|,
name|cacheConf
argument_list|)
operator|.
name|withOutputStream
argument_list|(
name|out
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|fileContext
argument_list|)
operator|.
name|create
argument_list|()
decl_stmt|;
try|try
block|{
for|for
control|(
name|KeyValue
name|kv
range|:
name|testKvs
control|)
block|{
name|writer
operator|.
name|append
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// read it back in
name|LOG
operator|.
name|info
argument_list|(
literal|"Reading with "
operator|+
name|fileContext
argument_list|)
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
name|HFileScanner
name|scanner
init|=
literal|null
decl_stmt|;
name|HFile
operator|.
name|Reader
name|reader
init|=
name|HFile
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|cacheConf
argument_list|,
literal|true
argument_list|,
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|reader
operator|.
name|loadFileInfo
argument_list|()
expr_stmt|;
name|FixedFileTrailer
name|trailer
init|=
name|reader
operator|.
name|getTrailer
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|trailer
operator|.
name|getEncryptionKey
argument_list|()
argument_list|)
expr_stmt|;
name|scanner
operator|=
name|reader
operator|.
name|getScanner
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Initial seekTo failed"
argument_list|,
name|scanner
operator|.
name|seekTo
argument_list|()
argument_list|)
expr_stmt|;
do|do
block|{
name|Cell
name|kv
init|=
name|scanner
operator|.
name|getCell
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Read back an unexpected or invalid KV"
argument_list|,
name|testKvs
operator|.
name|contains
argument_list|(
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|kv
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
do|while
condition|(
name|scanner
operator|.
name|next
argument_list|()
condition|)
do|;
block|}
finally|finally
block|{
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Did not read back as many KVs as written"
argument_list|,
name|i
argument_list|,
name|testKvs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Test random seeks with pread
name|LOG
operator|.
name|info
argument_list|(
literal|"Random seeking with "
operator|+
name|fileContext
argument_list|)
expr_stmt|;
name|reader
operator|=
name|HFile
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|cacheConf
argument_list|,
literal|true
argument_list|,
name|conf
argument_list|)
expr_stmt|;
try|try
block|{
name|scanner
operator|=
name|reader
operator|.
name|getScanner
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Initial seekTo failed"
argument_list|,
name|scanner
operator|.
name|seekTo
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|i
operator|=
literal|0
init|;
name|i
operator|<
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|KeyValue
name|kv
init|=
name|testKvs
operator|.
name|get
argument_list|(
name|RNG
operator|.
name|nextInt
argument_list|(
name|testKvs
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Unable to find KV as expected: "
operator|+
name|kv
argument_list|,
literal|0
argument_list|,
name|scanner
operator|.
name|seekTo
argument_list|(
name|kv
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

