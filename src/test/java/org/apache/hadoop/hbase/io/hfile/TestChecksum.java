begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|*
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
name|ByteArrayInputStream
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
name|DataInputStream
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
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|zip
operator|.
name|Checksum
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
name|hfile
operator|.
name|Compression
operator|.
name|Algorithm
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
name|ChecksumType
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
name|compress
operator|.
name|Compressor
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
name|hfile
operator|.
name|Compression
operator|.
name|Algorithm
operator|.
name|*
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
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestChecksum
block|{
comment|// change this value to activate more logs
specifier|private
specifier|static
specifier|final
name|boolean
name|detailedLogging
init|=
literal|true
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|boolean
index|[]
name|BOOLEAN_VALUES
init|=
operator|new
name|boolean
index|[]
block|{
literal|false
block|,
literal|true
block|}
decl_stmt|;
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
name|algo
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
name|HFile
operator|.
name|DEFAULT_CHECKSUM_TYPE
argument_list|,
name|HFile
operator|.
name|DEFAULT_BYTES_PER_CHECKSUM
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
name|HFileBlock
operator|.
name|FSReader
name|hbr
init|=
operator|new
name|FSReaderV2Test
argument_list|(
name|is
argument_list|,
name|algo
argument_list|,
name|totalSize
argument_list|,
name|HFile
operator|.
name|MAX_FORMAT_VERSION
argument_list|,
name|fs
argument_list|,
name|path
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
operator|-
literal|1
argument_list|,
name|pread
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
name|ByteBuffer
name|bb
init|=
name|b
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
name|getChecksumFailuresCount
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
operator|-
literal|1
argument_list|,
name|pread
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|HFile
operator|.
name|getChecksumFailuresCount
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
operator|-
literal|1
argument_list|,
name|pread
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|HFile
operator|.
name|getChecksumFailuresCount
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
operator|-
literal|1
argument_list|,
name|pread
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|HFile
operator|.
name|getChecksumFailuresCount
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
name|newfs
operator|.
name|open
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|hbr
operator|=
operator|new
name|FSReaderV2Test
argument_list|(
name|is
argument_list|,
name|algo
argument_list|,
name|totalSize
argument_list|,
name|HFile
operator|.
name|MAX_FORMAT_VERSION
argument_list|,
name|newfs
argument_list|,
name|path
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
operator|-
literal|1
argument_list|,
name|pread
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
name|getChecksumFailuresCount
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
comment|/**     * Test different values of bytesPerChecksum    */
annotation|@
name|Test
specifier|public
name|void
name|testChecksumChunks
parameter_list|()
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
name|algo
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
name|HFile
operator|.
name|DEFAULT_CHECKSUM_TYPE
argument_list|,
name|bytesPerChecksum
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
name|HFileBlock
operator|.
name|HEADER_SIZE
argument_list|,
name|bytesPerChecksum
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"testChecksumChunks: pread="
operator|+
name|pread
operator|+
literal|", bytesPerChecksum="
operator|+
name|bytesPerChecksum
operator|+
literal|", fileSize="
operator|+
name|totalSize
operator|+
literal|", dataSize="
operator|+
name|dataSize
operator|+
literal|", expectedChunks="
operator|+
name|expectedChunks
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
name|HFileBlock
operator|.
name|FSReader
name|hbr
init|=
operator|new
name|HFileBlock
operator|.
name|FSReaderV2
argument_list|(
name|is
argument_list|,
name|nochecksum
argument_list|,
name|algo
argument_list|,
name|totalSize
argument_list|,
name|HFile
operator|.
name|MAX_FORMAT_VERSION
argument_list|,
name|hfs
argument_list|,
name|path
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
operator|-
literal|1
argument_list|,
name|pread
argument_list|)
decl_stmt|;
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
name|HFileBlock
operator|.
name|HEADER_SIZE
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
name|getChecksumFailuresCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**     * Test to ensure that these is at least one valid checksum implementation    */
annotation|@
name|Test
specifier|public
name|void
name|testChecksumAlgorithm
parameter_list|()
throws|throws
name|IOException
block|{
name|ChecksumType
name|type
init|=
name|ChecksumType
operator|.
name|CRC32
decl_stmt|;
name|assertEquals
argument_list|(
name|ChecksumType
operator|.
name|nameToType
argument_list|(
name|type
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ChecksumType
operator|.
name|valueOf
argument_list|(
name|type
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|type
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|val
operator|!=
name|i
condition|)
block|{
name|String
name|msg
init|=
literal|"testChecksumCorruption: data mismatch at index "
operator|+
name|i
operator|+
literal|" expected "
operator|+
name|i
operator|+
literal|" found "
operator|+
name|val
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|org
operator|.
name|junit
operator|.
name|Rule
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
name|cu
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
argument_list|()
decl_stmt|;
comment|/**    * A class that introduces hbase-checksum failures while     * reading  data from hfiles. This should trigger the hdfs level    * checksum validations.    */
specifier|static
specifier|private
class|class
name|FSReaderV2Test
extends|extends
name|HFileBlock
operator|.
name|FSReaderV2
block|{
name|FSReaderV2Test
parameter_list|(
name|FSDataInputStream
name|istream
parameter_list|,
name|Algorithm
name|algo
parameter_list|,
name|long
name|fileSize
parameter_list|,
name|int
name|minorVersion
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|istream
argument_list|,
name|istream
argument_list|,
name|algo
argument_list|,
name|fileSize
argument_list|,
name|minorVersion
argument_list|,
operator|(
name|HFileSystem
operator|)
name|fs
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|validateBlockChecksum
parameter_list|(
name|HFileBlock
name|block
parameter_list|,
name|byte
index|[]
name|data
parameter_list|,
name|int
name|hdrSize
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|false
return|;
comment|// checksum validation failure
block|}
block|}
block|}
end_class

end_unit

