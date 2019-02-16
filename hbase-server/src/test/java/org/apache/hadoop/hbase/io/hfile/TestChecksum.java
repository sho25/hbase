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
operator|.
name|Algorithm
operator|.
name|GZ
import|;
end_import

begin_import
import|import static
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
operator|.
name|Algorithm
operator|.
name|NONE
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
name|nio
operator|.
name|BufferUnderflowException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
name|fs
operator|.
name|HFileSystem
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
name|FSDataInputStreamWrapper
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
name|nio
operator|.
name|ByteBuff
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
name|nio
operator|.
name|MultiByteBuff
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
name|nio
operator|.
name|SingleByteBuff
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
name|ChecksumType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|TestChecksum
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
name|TestChecksum
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
name|TestHFileBlock
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|Compression
operator|.
name|Algorithm
index|[]
name|COMPRESSION_ALGORITHMS
init|=
block|{
name|NONE
block|,
name|GZ
block|}
decl_stmt|;
specifier|static
specifier|final
name|int
index|[]
name|BYTES_PER_CHECKSUM
init|=
block|{
literal|50
block|,
literal|500
block|,
literal|688
block|,
literal|16
operator|*
literal|1024
block|,
operator|(
literal|16
operator|*
literal|1024
operator|+
literal|980
operator|)
block|,
literal|64
operator|*
literal|1024
block|}
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
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|HFileSystem
name|hfs
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|fs
operator|=
name|HFileSystem
operator|.
name|get
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|hfs
operator|=
operator|(
name|HFileSystem
operator|)
name|fs
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNewBlocksHaveDefaultChecksum
parameter_list|()
throws|throws
name|IOException
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
literal|"default_checksum"
argument_list|)
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
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
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
name|meta
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
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|1000
condition|;
operator|++
name|i
control|)
name|dos
operator|.
name|writeInt
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|hbw
operator|.
name|writeHeaderAndData
argument_list|(
name|os
argument_list|)
expr_stmt|;
name|int
name|totalSize
init|=
name|hbw
operator|.
name|getOnDiskSizeWithHeader
argument_list|()
decl_stmt|;
name|os
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Use hbase checksums.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|hfs
operator|.
name|useHBaseChecksum
argument_list|()
argument_list|)
expr_stmt|;
name|FSDataInputStreamWrapper
name|is
init|=
operator|new
name|FSDataInputStreamWrapper
argument_list|(
name|fs
argument_list|,
name|path
argument_list|)
decl_stmt|;
name|meta
operator|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withHBaseCheckSum
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|HFileBlock
operator|.
name|FSReader
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
operator|(
name|HFileSystem
operator|)
name|fs
argument_list|,
name|path
argument_list|,
name|meta
argument_list|,
name|ByteBuffAllocator
operator|.
name|HEAP
argument_list|)
decl_stmt|;
name|HFileBlock
name|b
init|=
name|hbr
operator|.
name|readBlockData
argument_list|(
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|b
operator|.
name|getChecksumType
argument_list|()
argument_list|,
name|ChecksumType
operator|.
name|getDefaultChecksumType
argument_list|()
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyMBBCheckSum
parameter_list|(
name|ByteBuff
name|buf
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|size
init|=
name|buf
operator|.
name|remaining
argument_list|()
operator|/
literal|2
operator|+
literal|1
decl_stmt|;
name|ByteBuff
name|mbb
init|=
operator|new
name|MultiByteBuff
argument_list|(
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|size
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|size
argument_list|)
argument_list|)
operator|.
name|position
argument_list|(
literal|0
argument_list|)
operator|.
name|limit
argument_list|(
name|buf
operator|.
name|remaining
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|buf
operator|.
name|position
argument_list|()
init|;
name|i
operator|<
name|buf
operator|.
name|limit
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|mbb
operator|.
name|put
argument_list|(
name|buf
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|mbb
operator|.
name|position
argument_list|(
literal|0
argument_list|)
operator|.
name|limit
argument_list|(
name|buf
operator|.
name|remaining
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|mbb
operator|.
name|remaining
argument_list|()
argument_list|,
name|buf
operator|.
name|remaining
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|mbb
operator|.
name|remaining
argument_list|()
operator|>
name|size
argument_list|)
expr_stmt|;
name|ChecksumUtil
operator|.
name|validateChecksum
argument_list|(
name|mbb
argument_list|,
literal|"test"
argument_list|,
literal|0
argument_list|,
name|HConstants
operator|.
name|HFILEBLOCK_HEADER_SIZE_NO_CHECKSUM
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifySBBCheckSum
parameter_list|(
name|ByteBuff
name|buf
parameter_list|)
throws|throws
name|IOException
block|{
name|ChecksumUtil
operator|.
name|validateChecksum
argument_list|(
name|buf
argument_list|,
literal|"test"
argument_list|,
literal|0
argument_list|,
name|HConstants
operator|.
name|HFILEBLOCK_HEADER_SIZE_NO_CHECKSUM
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testVerifyCheckSum
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|intCount
init|=
literal|10000
decl_stmt|;
for|for
control|(
name|ChecksumType
name|ckt
range|:
name|ChecksumType
operator|.
name|values
argument_list|()
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
literal|"checksum"
operator|+
name|ckt
operator|.
name|getName
argument_list|()
argument_list|)
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
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withChecksumType
argument_list|(
name|ckt
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
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
name|meta
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
name|i
init|=
literal|0
init|;
name|i
operator|<
name|intCount
condition|;
operator|++
name|i
control|)
block|{
name|dos
operator|.
name|writeInt
argument_list|(
name|i
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
name|int
name|totalSize
init|=
name|hbw
operator|.
name|getOnDiskSizeWithHeader
argument_list|()
decl_stmt|;
name|os
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Use hbase checksums.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|hfs
operator|.
name|useHBaseChecksum
argument_list|()
argument_list|)
expr_stmt|;
name|FSDataInputStreamWrapper
name|is
init|=
operator|new
name|FSDataInputStreamWrapper
argument_list|(
name|fs
argument_list|,
name|path
argument_list|)
decl_stmt|;
name|meta
operator|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withHBaseCheckSum
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|HFileBlock
operator|.
name|FSReader
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
operator|(
name|HFileSystem
operator|)
name|fs
argument_list|,
name|path
argument_list|,
name|meta
argument_list|,
name|ByteBuffAllocator
operator|.
name|HEAP
argument_list|)
decl_stmt|;
name|HFileBlock
name|b
init|=
name|hbr
operator|.
name|readBlockData
argument_list|(
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|// verify SingleByteBuff checksum.
name|verifySBBCheckSum
argument_list|(
name|b
operator|.
name|getBufferReadOnly
argument_list|()
argument_list|)
expr_stmt|;
comment|// verify MultiByteBuff checksum.
name|verifyMBBCheckSum
argument_list|(
name|b
operator|.
name|getBufferReadOnly
argument_list|()
argument_list|)
expr_stmt|;
name|ByteBuff
name|data
init|=
name|b
operator|.
name|getBufferWithoutHeader
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
name|intCount
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|i
argument_list|,
name|data
operator|.
name|getInt
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|data
operator|.
name|getInt
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|BufferUnderflowException
name|e
parameter_list|)
block|{
comment|// expected failure
block|}
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
block|}
block|}
comment|/**    * Introduce checksum failures and check that we can still read    * the data    */
annotation|@
name|Test
specifier|public
name|void
name|testChecksumCorruption
parameter_list|()
throws|throws
name|IOException
block|{
name|testChecksumCorruptionInternals
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|testChecksumCorruptionInternals
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|testChecksumCorruptionInternals
parameter_list|(
name|boolean
name|useTags
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|Compression
operator|.
name|Algorithm
name|algo
range|:
name|COMPRESSION_ALGORITHMS
control|)
block|{
for|for
control|(
name|boolean
name|pread
range|:
operator|new
name|boolean
index|[]
block|{
literal|false
block|,
literal|true
block|}
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"testChecksumCorruption: Compression algorithm: "
operator|+
name|algo
operator|+
literal|", pread="
operator|+
name|pread
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
literal|"blocks_v2_"
operator|+
name|algo
argument_list|)
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
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withCompression
argument_list|(
name|algo
argument_list|)
operator|.
name|withIncludesMvcc
argument_list|(
literal|true
argument_list|)
operator|.
name|withIncludesTags
argument_list|(
name|useTags
argument_list|)
operator|.
name|withBytesPerCheckSum
argument_list|(
name|HFile
operator|.
name|DEFAULT_BYTES_PER_CHECKSUM
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
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
name|meta
argument_list|)
decl_stmt|;
name|long
name|totalSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|blockId
init|=
literal|0
init|;
name|blockId
operator|<
literal|2
condition|;
operator|++
name|blockId
control|)
block|{
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
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|1234
condition|;
operator|++
name|i
control|)
name|dos
operator|.
name|writeInt
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|hbw
operator|.
name|writeHeaderAndData
argument_list|(
name|os
argument_list|)
expr_stmt|;
name|totalSize
operator|+=
name|hbw
operator|.
name|getOnDiskSizeWithHeader
argument_list|()
expr_stmt|;
block|}
name|os
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Use hbase checksums.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|hfs
operator|.
name|useHBaseChecksum
argument_list|()
argument_list|)
expr_stmt|;
comment|// Do a read that purposely introduces checksum verification failures.
name|FSDataInputStreamWrapper
name|is
init|=
operator|new
name|FSDataInputStreamWrapper
argument_list|(
name|fs
argument_list|,
name|path
argument_list|)
decl_stmt|;
name|meta
operator|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withCompression
argument_list|(
name|algo
argument_list|)
operator|.
name|withIncludesMvcc
argument_list|(
literal|true
argument_list|)
operator|.
name|withIncludesTags
argument_list|(
name|useTags
argument_list|)
operator|.
name|withHBaseCheckSum
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|HFileBlock
operator|.
name|FSReader
name|hbr
init|=
operator|new
name|CorruptedFSReaderImpl
argument_list|(
name|is
argument_list|,
name|totalSize
argument_list|,
name|fs
argument_list|,
name|path
argument_list|,
name|meta
argument_list|)
decl_stmt|;
name|HFileBlock
name|b
init|=
name|hbr
operator|.
name|readBlockData
argument_list|(
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
name|pread
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|b
operator|.
name|sanityCheck
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|4936
argument_list|,
name|b
operator|.
name|getUncompressedSizeWithoutHeader
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|algo
operator|==
name|GZ
condition|?
literal|2173
else|:
literal|4936
argument_list|,
name|b
operator|.
name|getOnDiskSizeWithoutHeader
argument_list|()
operator|-
name|b
operator|.
name|totalChecksumBytes
argument_list|()
argument_list|)
expr_stmt|;
comment|// read data back from the hfile, exclude header and checksum
name|ByteBuff
name|bb
init|=
name|b
operator|.
name|unpack
argument_list|(
name|meta
argument_list|,
name|hbr
argument_list|)
operator|.
name|getBufferWithoutHeader
argument_list|()
decl_stmt|;
comment|// read back data
name|DataInputStream
name|in
init|=
operator|new
name|DataInputStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|bb
operator|.
name|array
argument_list|()
argument_list|,
name|bb
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|bb
operator|.
name|limit
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
comment|// assert that we encountered hbase checksum verification failures
comment|// but still used hdfs checksums and read data successfully.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|HFile
operator|.
name|getAndResetChecksumFailuresCount
argument_list|()
argument_list|)
expr_stmt|;
name|validateData
argument_list|(
name|in
argument_list|)
expr_stmt|;
comment|// A single instance of hbase checksum failure causes the reader to
comment|// switch off hbase checksum verification for the next 100 read
comment|// requests. Verify that this is correct.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|HFileBlock
operator|.
name|CHECKSUM_VERIFICATION_NUM_IO_THRESHOLD
operator|+
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|b
operator|=
name|hbr
operator|.
name|readBlockData
argument_list|(
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
name|pread
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|b
operator|.
name|getBufferReadOnly
argument_list|()
operator|instanceof
name|SingleByteBuff
argument_list|)
expr_stmt|;
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
block|}
comment|// The next read should have hbase checksum verification reanabled,
comment|// we verify this by assertng that there was a hbase-checksum failure.
name|b
operator|=
name|hbr
operator|.
name|readBlockData
argument_list|(
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
name|pread
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|b
operator|.
name|getBufferReadOnly
argument_list|()
operator|instanceof
name|SingleByteBuff
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|HFile
operator|.
name|getAndResetChecksumFailuresCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// Since the above encountered a checksum failure, we switch
comment|// back to not checking hbase checksums.
name|b
operator|=
name|hbr
operator|.
name|readBlockData
argument_list|(
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
name|pread
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|b
operator|.
name|getBufferReadOnly
argument_list|()
operator|instanceof
name|SingleByteBuff
argument_list|)
expr_stmt|;
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
name|is
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Now, use a completely new reader. Switch off hbase checksums in
comment|// the configuration. In this case, we should not detect
comment|// any retries within hbase.
name|HFileSystem
name|newfs
init|=
operator|new
name|HFileSystem
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|newfs
operator|.
name|useHBaseChecksum
argument_list|()
argument_list|)
expr_stmt|;
name|is
operator|=
operator|new
name|FSDataInputStreamWrapper
argument_list|(
name|newfs
argument_list|,
name|path
argument_list|)
expr_stmt|;
name|hbr
operator|=
operator|new
name|CorruptedFSReaderImpl
argument_list|(
name|is
argument_list|,
name|totalSize
argument_list|,
name|newfs
argument_list|,
name|path
argument_list|,
name|meta
argument_list|)
expr_stmt|;
name|b
operator|=
name|hbr
operator|.
name|readBlockData
argument_list|(
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
name|pread
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|is
operator|.
name|close
argument_list|()
expr_stmt|;
name|b
operator|.
name|sanityCheck
argument_list|()
expr_stmt|;
name|b
operator|=
name|b
operator|.
name|unpack
argument_list|(
name|meta
argument_list|,
name|hbr
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4936
argument_list|,
name|b
operator|.
name|getUncompressedSizeWithoutHeader
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|algo
operator|==
name|GZ
condition|?
literal|2173
else|:
literal|4936
argument_list|,
name|b
operator|.
name|getOnDiskSizeWithoutHeader
argument_list|()
operator|-
name|b
operator|.
name|totalChecksumBytes
argument_list|()
argument_list|)
expr_stmt|;
comment|// read data back from the hfile, exclude header and checksum
name|bb
operator|=
name|b
operator|.
name|getBufferWithoutHeader
argument_list|()
expr_stmt|;
comment|// read back data
name|in
operator|=
operator|new
name|DataInputStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|bb
operator|.
name|array
argument_list|()
argument_list|,
name|bb
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|bb
operator|.
name|limit
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// assert that we did not encounter hbase checksum verification failures
comment|// but still used hdfs checksums and read data successfully.
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
name|validateData
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Test different values of bytesPerChecksum    */
annotation|@
name|Test
specifier|public
name|void
name|testChecksumChunks
parameter_list|()
throws|throws
name|IOException
block|{
name|testChecksumInternals
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|testChecksumInternals
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|testChecksumInternals
parameter_list|(
name|boolean
name|useTags
parameter_list|)
throws|throws
name|IOException
block|{
name|Compression
operator|.
name|Algorithm
name|algo
init|=
name|NONE
decl_stmt|;
for|for
control|(
name|boolean
name|pread
range|:
operator|new
name|boolean
index|[]
block|{
literal|false
block|,
literal|true
block|}
control|)
block|{
for|for
control|(
name|int
name|bytesPerChecksum
range|:
name|BYTES_PER_CHECKSUM
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
literal|"checksumChunk_"
operator|+
name|algo
operator|+
name|bytesPerChecksum
argument_list|)
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
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withCompression
argument_list|(
name|algo
argument_list|)
operator|.
name|withIncludesMvcc
argument_list|(
literal|true
argument_list|)
operator|.
name|withIncludesTags
argument_list|(
name|useTags
argument_list|)
operator|.
name|withHBaseCheckSum
argument_list|(
literal|true
argument_list|)
operator|.
name|withBytesPerCheckSum
argument_list|(
name|bytesPerChecksum
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
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
name|meta
argument_list|)
decl_stmt|;
comment|// write one block. The block has data
comment|// that is at least 6 times more than the checksum chunk size
name|long
name|dataSize
init|=
literal|0
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
init|;
name|dataSize
operator|<
literal|6
operator|*
name|bytesPerChecksum
condition|;
control|)
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
literal|1234
condition|;
operator|++
name|i
control|)
block|{
name|dos
operator|.
name|writeInt
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|dataSize
operator|+=
literal|4
expr_stmt|;
block|}
block|}
name|hbw
operator|.
name|writeHeaderAndData
argument_list|(
name|os
argument_list|)
expr_stmt|;
name|long
name|totalSize
init|=
name|hbw
operator|.
name|getOnDiskSizeWithHeader
argument_list|()
decl_stmt|;
name|os
operator|.
name|close
argument_list|()
expr_stmt|;
name|long
name|expectedChunks
init|=
name|ChecksumUtil
operator|.
name|numChunks
argument_list|(
name|dataSize
operator|+
name|HConstants
operator|.
name|HFILEBLOCK_HEADER_SIZE
argument_list|,
name|bytesPerChecksum
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"testChecksumChunks: pread={}, bytesPerChecksum={}, fileSize={}, "
operator|+
literal|"dataSize={}, expectedChunks={}, compression={}"
argument_list|,
name|pread
argument_list|,
name|bytesPerChecksum
argument_list|,
name|totalSize
argument_list|,
name|dataSize
argument_list|,
name|expectedChunks
argument_list|,
name|algo
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Verify hbase checksums.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|hfs
operator|.
name|useHBaseChecksum
argument_list|()
argument_list|)
expr_stmt|;
comment|// Read data back from file.
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
name|FSDataInputStream
name|nochecksum
init|=
name|hfs
operator|.
name|getNoChecksumFs
argument_list|()
operator|.
name|open
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|meta
operator|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withCompression
argument_list|(
name|algo
argument_list|)
operator|.
name|withIncludesMvcc
argument_list|(
literal|true
argument_list|)
operator|.
name|withIncludesTags
argument_list|(
name|useTags
argument_list|)
operator|.
name|withHBaseCheckSum
argument_list|(
literal|true
argument_list|)
operator|.
name|withBytesPerCheckSum
argument_list|(
name|bytesPerChecksum
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|HFileBlock
operator|.
name|FSReader
name|hbr
init|=
operator|new
name|HFileBlock
operator|.
name|FSReaderImpl
argument_list|(
operator|new
name|FSDataInputStreamWrapper
argument_list|(
name|is
argument_list|,
name|nochecksum
argument_list|)
argument_list|,
name|totalSize
argument_list|,
name|hfs
argument_list|,
name|path
argument_list|,
name|meta
argument_list|,
name|ByteBuffAllocator
operator|.
name|HEAP
argument_list|)
decl_stmt|;
name|HFileBlock
name|b
init|=
name|hbr
operator|.
name|readBlockData
argument_list|(
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
name|pread
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|b
operator|.
name|getBufferReadOnly
argument_list|()
operator|instanceof
name|SingleByteBuff
argument_list|)
expr_stmt|;
name|is
operator|.
name|close
argument_list|()
expr_stmt|;
name|b
operator|.
name|sanityCheck
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|dataSize
argument_list|,
name|b
operator|.
name|getUncompressedSizeWithoutHeader
argument_list|()
argument_list|)
expr_stmt|;
comment|// verify that we have the expected number of checksum chunks
name|assertEquals
argument_list|(
name|totalSize
argument_list|,
name|HConstants
operator|.
name|HFILEBLOCK_HEADER_SIZE
operator|+
name|dataSize
operator|+
name|expectedChunks
operator|*
name|HFileBlock
operator|.
name|CHECKSUM_SIZE
argument_list|)
expr_stmt|;
comment|// assert that we did not encounter hbase checksum verification failures
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
block|}
block|}
block|}
specifier|private
name|void
name|validateData
parameter_list|(
name|DataInputStream
name|in
parameter_list|)
throws|throws
name|IOException
block|{
comment|// validate data
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|1234
condition|;
name|i
operator|++
control|)
block|{
name|int
name|val
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"testChecksumCorruption: data mismatch at index "
operator|+
name|i
argument_list|,
name|i
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * This class is to test checksum behavior when data is corrupted. It mimics the following    * behavior:    *  - When fs checksum is disabled, hbase may get corrupted data from hdfs. If verifyChecksum    *  is true, it means hbase checksum is on and fs checksum is off, so we corrupt the data.    *  - When fs checksum is enabled, hdfs will get a different copy from another node, and will    *    always return correct data. So we don't corrupt the data when verifyChecksum for hbase is    *    off.    */
specifier|static
specifier|private
class|class
name|CorruptedFSReaderImpl
extends|extends
name|HFileBlock
operator|.
name|FSReaderImpl
block|{
comment|/**      * If set to true, corrupt reads using readAtOffset(...).      */
name|boolean
name|corruptDataStream
init|=
literal|false
decl_stmt|;
specifier|public
name|CorruptedFSReaderImpl
parameter_list|(
name|FSDataInputStreamWrapper
name|istream
parameter_list|,
name|long
name|fileSize
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|HFileContext
name|meta
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|istream
argument_list|,
name|fileSize
argument_list|,
operator|(
name|HFileSystem
operator|)
name|fs
argument_list|,
name|path
argument_list|,
name|meta
argument_list|,
name|ByteBuffAllocator
operator|.
name|HEAP
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|HFileBlock
name|readBlockDataInternal
parameter_list|(
name|FSDataInputStream
name|is
parameter_list|,
name|long
name|offset
parameter_list|,
name|long
name|onDiskSizeWithHeaderL
parameter_list|,
name|boolean
name|pread
parameter_list|,
name|boolean
name|verifyChecksum
parameter_list|,
name|boolean
name|updateMetrics
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|verifyChecksum
condition|)
block|{
name|corruptDataStream
operator|=
literal|true
expr_stmt|;
block|}
name|HFileBlock
name|b
init|=
name|super
operator|.
name|readBlockDataInternal
argument_list|(
name|is
argument_list|,
name|offset
argument_list|,
name|onDiskSizeWithHeaderL
argument_list|,
name|pread
argument_list|,
name|verifyChecksum
argument_list|,
name|updateMetrics
argument_list|)
decl_stmt|;
name|corruptDataStream
operator|=
literal|false
expr_stmt|;
return|return
name|b
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|readAtOffset
parameter_list|(
name|FSDataInputStream
name|istream
parameter_list|,
name|ByteBuff
name|dest
parameter_list|,
name|int
name|size
parameter_list|,
name|boolean
name|peekIntoNextBlock
parameter_list|,
name|long
name|fileOffset
parameter_list|,
name|boolean
name|pread
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|destOffset
init|=
name|dest
operator|.
name|position
argument_list|()
decl_stmt|;
name|boolean
name|returnValue
init|=
name|super
operator|.
name|readAtOffset
argument_list|(
name|istream
argument_list|,
name|dest
argument_list|,
name|size
argument_list|,
name|peekIntoNextBlock
argument_list|,
name|fileOffset
argument_list|,
name|pread
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|corruptDataStream
condition|)
block|{
return|return
name|returnValue
return|;
block|}
comment|// Corrupt 3rd character of block magic of next block's header.
if|if
condition|(
name|peekIntoNextBlock
condition|)
block|{
name|dest
operator|.
name|put
argument_list|(
name|destOffset
operator|+
name|size
operator|+
literal|3
argument_list|,
operator|(
name|byte
operator|)
literal|0b00000000
argument_list|)
expr_stmt|;
block|}
comment|// We might be reading this block's header too, corrupt it.
name|dest
operator|.
name|put
argument_list|(
name|destOffset
operator|+
literal|1
argument_list|,
operator|(
name|byte
operator|)
literal|0b00000000
argument_list|)
expr_stmt|;
comment|// Corrupt non header data
if|if
condition|(
name|size
operator|>
name|hdrSize
condition|)
block|{
name|dest
operator|.
name|put
argument_list|(
name|destOffset
operator|+
name|hdrSize
operator|+
literal|1
argument_list|,
operator|(
name|byte
operator|)
literal|0b00000000
argument_list|)
expr_stmt|;
block|}
return|return
name|returnValue
return|;
block|}
block|}
block|}
end_class

end_unit

