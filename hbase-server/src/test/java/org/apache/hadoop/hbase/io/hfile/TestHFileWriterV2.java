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
name|assertTrue
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
name|IOException
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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
name|KeyValue
operator|.
name|KVComparator
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
name|compress
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
name|io
operator|.
name|hfile
operator|.
name|HFile
operator|.
name|FileInfo
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
name|Writables
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
name|Text
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

begin_comment
comment|/**  * Testing writing a version 2 {@link HFile}. This is a low-level test written  * during the development of {@link HFileWriterV2}.  */
end_comment

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
name|TestHFileWriterV2
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
name|TestHFileWriterV2
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
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHFileFormatV2
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|hfilePath
init|=
operator|new
name|Path
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
literal|"testHFileFormatV2"
argument_list|)
decl_stmt|;
specifier|final
name|Compression
operator|.
name|Algorithm
name|compressAlgo
init|=
name|Compression
operator|.
name|Algorithm
operator|.
name|GZ
decl_stmt|;
specifier|final
name|int
name|entryCount
init|=
literal|10000
decl_stmt|;
name|writeDataAndReadFromHFile
argument_list|(
name|hfilePath
argument_list|,
name|compressAlgo
argument_list|,
name|entryCount
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMidKeyInHFile
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|hfilePath
init|=
operator|new
name|Path
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
literal|"testMidKeyInHFile"
argument_list|)
decl_stmt|;
name|Compression
operator|.
name|Algorithm
name|compressAlgo
init|=
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
decl_stmt|;
name|int
name|entryCount
init|=
literal|50000
decl_stmt|;
name|writeDataAndReadFromHFile
argument_list|(
name|hfilePath
argument_list|,
name|compressAlgo
argument_list|,
name|entryCount
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|writeDataAndReadFromHFile
parameter_list|(
name|Path
name|hfilePath
parameter_list|,
name|Algorithm
name|compressAlgo
parameter_list|,
name|int
name|entryCount
parameter_list|,
name|boolean
name|findMidKey
parameter_list|)
throws|throws
name|IOException
block|{
name|HFileContext
name|context
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withBlockSize
argument_list|(
literal|4096
argument_list|)
operator|.
name|withCompression
argument_list|(
name|compressAlgo
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HFileWriterV2
name|writer
init|=
operator|(
name|HFileWriterV2
operator|)
operator|new
name|HFileWriterV2
operator|.
name|WriterFactoryV2
argument_list|(
name|conf
argument_list|,
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
argument_list|)
operator|.
name|withPath
argument_list|(
name|fs
argument_list|,
name|hfilePath
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|context
argument_list|)
operator|.
name|create
argument_list|()
decl_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
literal|9713312
argument_list|)
decl_stmt|;
comment|// Just a fixed seed.
name|List
argument_list|<
name|KeyValue
argument_list|>
name|keyValues
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|(
name|entryCount
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
name|entryCount
condition|;
operator|++
name|i
control|)
block|{
name|byte
index|[]
name|keyBytes
init|=
name|randomOrderedKey
argument_list|(
name|rand
argument_list|,
name|i
argument_list|)
decl_stmt|;
comment|// A random-length random value.
name|byte
index|[]
name|valueBytes
init|=
name|randomValue
argument_list|(
name|rand
argument_list|)
decl_stmt|;
name|KeyValue
name|keyValue
init|=
operator|new
name|KeyValue
argument_list|(
name|keyBytes
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|valueBytes
argument_list|)
decl_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|keyValue
argument_list|)
expr_stmt|;
name|keyValues
operator|.
name|add
argument_list|(
name|keyValue
argument_list|)
expr_stmt|;
block|}
comment|// Add in an arbitrary order. They will be sorted lexicographically by
comment|// the key.
name|writer
operator|.
name|appendMetaBlock
argument_list|(
literal|"CAPITAL_OF_USA"
argument_list|,
operator|new
name|Text
argument_list|(
literal|"Washington, D.C."
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|appendMetaBlock
argument_list|(
literal|"CAPITAL_OF_RUSSIA"
argument_list|,
operator|new
name|Text
argument_list|(
literal|"Moscow"
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|appendMetaBlock
argument_list|(
literal|"CAPITAL_OF_FRANCE"
argument_list|,
operator|new
name|Text
argument_list|(
literal|"Paris"
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|FSDataInputStream
name|fsdis
init|=
name|fs
operator|.
name|open
argument_list|(
name|hfilePath
argument_list|)
decl_stmt|;
comment|// A "manual" version of a new-format HFile reader. This unit test was
comment|// written before the V2 reader was fully implemented.
name|long
name|fileSize
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|hfilePath
argument_list|)
operator|.
name|getLen
argument_list|()
decl_stmt|;
name|FixedFileTrailer
name|trailer
init|=
name|FixedFileTrailer
operator|.
name|readFromStream
argument_list|(
name|fsdis
argument_list|,
name|fileSize
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|trailer
operator|.
name|getMajorVersion
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|entryCount
argument_list|,
name|trailer
operator|.
name|getEntryCount
argument_list|()
argument_list|)
expr_stmt|;
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withHBaseCheckSum
argument_list|(
literal|true
argument_list|)
operator|.
name|withIncludesMvcc
argument_list|(
literal|false
argument_list|)
operator|.
name|withIncludesTags
argument_list|(
literal|false
argument_list|)
operator|.
name|withCompression
argument_list|(
name|compressAlgo
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HFileBlock
operator|.
name|FSReader
name|blockReader
init|=
operator|new
name|HFileBlock
operator|.
name|FSReaderImpl
argument_list|(
name|fsdis
argument_list|,
name|fileSize
argument_list|,
name|meta
argument_list|)
decl_stmt|;
comment|// Comparator class name is stored in the trailer in version 2.
name|KVComparator
name|comparator
init|=
name|trailer
operator|.
name|createComparator
argument_list|()
decl_stmt|;
name|HFileBlockIndex
operator|.
name|BlockIndexReader
name|dataBlockIndexReader
init|=
operator|new
name|HFileBlockIndex
operator|.
name|BlockIndexReader
argument_list|(
name|comparator
argument_list|,
name|trailer
operator|.
name|getNumDataIndexLevels
argument_list|()
argument_list|)
decl_stmt|;
name|HFileBlockIndex
operator|.
name|BlockIndexReader
name|metaBlockIndexReader
init|=
operator|new
name|HFileBlockIndex
operator|.
name|BlockIndexReader
argument_list|(
name|KeyValue
operator|.
name|RAW_COMPARATOR
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|HFileBlock
operator|.
name|BlockIterator
name|blockIter
init|=
name|blockReader
operator|.
name|blockRange
argument_list|(
name|trailer
operator|.
name|getLoadOnOpenDataOffset
argument_list|()
argument_list|,
name|fileSize
operator|-
name|trailer
operator|.
name|getTrailerSize
argument_list|()
argument_list|)
decl_stmt|;
comment|// Data index. We also read statistics about the block index written after
comment|// the root level.
name|dataBlockIndexReader
operator|.
name|readMultiLevelIndexRoot
argument_list|(
name|blockIter
operator|.
name|nextBlockWithBlockType
argument_list|(
name|BlockType
operator|.
name|ROOT_INDEX
argument_list|)
argument_list|,
name|trailer
operator|.
name|getDataIndexCount
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|findMidKey
condition|)
block|{
name|byte
index|[]
name|midkey
init|=
name|dataBlockIndexReader
operator|.
name|midkey
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"Midkey should not be null"
argument_list|,
name|midkey
argument_list|)
expr_stmt|;
block|}
comment|// Meta index.
name|metaBlockIndexReader
operator|.
name|readRootIndex
argument_list|(
name|blockIter
operator|.
name|nextBlockWithBlockType
argument_list|(
name|BlockType
operator|.
name|ROOT_INDEX
argument_list|)
operator|.
name|getByteStream
argument_list|()
argument_list|,
name|trailer
operator|.
name|getMetaIndexCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// File info
name|FileInfo
name|fileInfo
init|=
operator|new
name|FileInfo
argument_list|()
decl_stmt|;
name|fileInfo
operator|.
name|read
argument_list|(
name|blockIter
operator|.
name|nextBlockWithBlockType
argument_list|(
name|BlockType
operator|.
name|FILE_INFO
argument_list|)
operator|.
name|getByteStream
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|keyValueFormatVersion
init|=
name|fileInfo
operator|.
name|get
argument_list|(
name|HFileWriterV2
operator|.
name|KEY_VALUE_VERSION
argument_list|)
decl_stmt|;
name|boolean
name|includeMemstoreTS
init|=
name|keyValueFormatVersion
operator|!=
literal|null
operator|&&
name|Bytes
operator|.
name|toInt
argument_list|(
name|keyValueFormatVersion
argument_list|)
operator|>
literal|0
decl_stmt|;
comment|// Counters for the number of key/value pairs and the number of blocks
name|int
name|entriesRead
init|=
literal|0
decl_stmt|;
name|int
name|blocksRead
init|=
literal|0
decl_stmt|;
name|long
name|memstoreTS
init|=
literal|0
decl_stmt|;
comment|// Scan blocks the way the reader would scan them
name|fsdis
operator|.
name|seek
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|long
name|curBlockPos
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|curBlockPos
operator|<=
name|trailer
operator|.
name|getLastDataBlockOffset
argument_list|()
condition|)
block|{
name|HFileBlock
name|block
init|=
name|blockReader
operator|.
name|readBlockData
argument_list|(
name|curBlockPos
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|BlockType
operator|.
name|DATA
argument_list|,
name|block
operator|.
name|getBlockType
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|meta
operator|.
name|isCompressedOrEncrypted
argument_list|()
condition|)
block|{
name|assertFalse
argument_list|(
name|block
operator|.
name|isUnpacked
argument_list|()
argument_list|)
expr_stmt|;
name|block
operator|=
name|block
operator|.
name|unpack
argument_list|(
name|meta
argument_list|,
name|blockReader
argument_list|)
expr_stmt|;
block|}
name|ByteBuffer
name|buf
init|=
name|block
operator|.
name|getBufferWithoutHeader
argument_list|()
decl_stmt|;
while|while
condition|(
name|buf
operator|.
name|hasRemaining
argument_list|()
condition|)
block|{
name|int
name|keyLen
init|=
name|buf
operator|.
name|getInt
argument_list|()
decl_stmt|;
name|int
name|valueLen
init|=
name|buf
operator|.
name|getInt
argument_list|()
decl_stmt|;
name|byte
index|[]
name|key
init|=
operator|new
name|byte
index|[
name|keyLen
index|]
decl_stmt|;
name|buf
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
name|valueLen
index|]
decl_stmt|;
name|buf
operator|.
name|get
argument_list|(
name|value
argument_list|)
expr_stmt|;
if|if
condition|(
name|includeMemstoreTS
condition|)
block|{
name|ByteArrayInputStream
name|byte_input
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|buf
operator|.
name|array
argument_list|()
argument_list|,
name|buf
operator|.
name|arrayOffset
argument_list|()
operator|+
name|buf
operator|.
name|position
argument_list|()
argument_list|,
name|buf
operator|.
name|remaining
argument_list|()
argument_list|)
decl_stmt|;
name|DataInputStream
name|data_input
init|=
operator|new
name|DataInputStream
argument_list|(
name|byte_input
argument_list|)
decl_stmt|;
name|memstoreTS
operator|=
name|WritableUtils
operator|.
name|readVLong
argument_list|(
name|data_input
argument_list|)
expr_stmt|;
name|buf
operator|.
name|position
argument_list|(
name|buf
operator|.
name|position
argument_list|()
operator|+
name|WritableUtils
operator|.
name|getVIntSize
argument_list|(
name|memstoreTS
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// A brute-force check to see that all keys and values are correct.
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|key
argument_list|,
name|keyValues
operator|.
name|get
argument_list|(
name|entriesRead
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|value
argument_list|,
name|keyValues
operator|.
name|get
argument_list|(
name|entriesRead
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
operator|++
name|entriesRead
expr_stmt|;
block|}
operator|++
name|blocksRead
expr_stmt|;
name|curBlockPos
operator|+=
name|block
operator|.
name|getOnDiskSizeWithHeader
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Finished reading: entries="
operator|+
name|entriesRead
operator|+
literal|", blocksRead="
operator|+
name|blocksRead
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|entryCount
argument_list|,
name|entriesRead
argument_list|)
expr_stmt|;
comment|// Meta blocks. We can scan until the load-on-open data offset (which is
comment|// the root block index offset in version 2) because we are not testing
comment|// intermediate-level index blocks here.
name|int
name|metaCounter
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|fsdis
operator|.
name|getPos
argument_list|()
operator|<
name|trailer
operator|.
name|getLoadOnOpenDataOffset
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Current offset: "
operator|+
name|fsdis
operator|.
name|getPos
argument_list|()
operator|+
literal|", scanning until "
operator|+
name|trailer
operator|.
name|getLoadOnOpenDataOffset
argument_list|()
argument_list|)
expr_stmt|;
name|HFileBlock
name|block
init|=
name|blockReader
operator|.
name|readBlockData
argument_list|(
name|curBlockPos
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|,
literal|false
argument_list|)
operator|.
name|unpack
argument_list|(
name|meta
argument_list|,
name|blockReader
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|BlockType
operator|.
name|META
argument_list|,
name|block
operator|.
name|getBlockType
argument_list|()
argument_list|)
expr_stmt|;
name|Text
name|t
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
name|ByteBuffer
name|buf
init|=
name|block
operator|.
name|getBufferWithoutHeader
argument_list|()
decl_stmt|;
if|if
condition|(
name|Writables
operator|.
name|getWritable
argument_list|(
name|buf
operator|.
name|array
argument_list|()
argument_list|,
name|buf
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|buf
operator|.
name|limit
argument_list|()
argument_list|,
name|t
argument_list|)
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to deserialize block "
operator|+
name|this
operator|+
literal|" into a "
operator|+
name|t
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
throw|;
block|}
name|Text
name|expectedText
init|=
operator|(
name|metaCounter
operator|==
literal|0
condition|?
operator|new
name|Text
argument_list|(
literal|"Paris"
argument_list|)
else|:
name|metaCounter
operator|==
literal|1
condition|?
operator|new
name|Text
argument_list|(
literal|"Moscow"
argument_list|)
else|:
operator|new
name|Text
argument_list|(
literal|"Washington, D.C."
argument_list|)
operator|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedText
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Read meta block data: "
operator|+
name|t
argument_list|)
expr_stmt|;
operator|++
name|metaCounter
expr_stmt|;
name|curBlockPos
operator|+=
name|block
operator|.
name|getOnDiskSizeWithHeader
argument_list|()
expr_stmt|;
block|}
name|fsdis
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// Static stuff used by various HFile v2 unit tests
specifier|private
specifier|static
specifier|final
name|String
name|COLUMN_FAMILY_NAME
init|=
literal|"_-myColumnFamily-_"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MIN_ROW_OR_QUALIFIER_LENGTH
init|=
literal|64
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_ROW_OR_QUALIFIER_LENGTH
init|=
literal|128
decl_stmt|;
comment|/**    * Generates a random key that is guaranteed to increase as the given index i    * increases. The result consists of a prefix, which is a deterministic    * increasing function of i, and a random suffix.    *    * @param rand    *          random number generator to use    * @param i    * @return    */
specifier|public
specifier|static
name|byte
index|[]
name|randomOrderedKey
parameter_list|(
name|Random
name|rand
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|StringBuilder
name|k
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
comment|// The fixed-length lexicographically increasing part of the key.
for|for
control|(
name|int
name|bitIndex
init|=
literal|31
init|;
name|bitIndex
operator|>=
literal|0
condition|;
operator|--
name|bitIndex
control|)
block|{
if|if
condition|(
operator|(
name|i
operator|&
operator|(
literal|1
operator|<<
name|bitIndex
operator|)
operator|)
operator|==
literal|0
condition|)
name|k
operator|.
name|append
argument_list|(
literal|"a"
argument_list|)
expr_stmt|;
else|else
name|k
operator|.
name|append
argument_list|(
literal|"b"
argument_list|)
expr_stmt|;
block|}
comment|// A random-length random suffix of the key.
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|rand
operator|.
name|nextInt
argument_list|(
literal|50
argument_list|)
condition|;
operator|++
name|j
control|)
name|k
operator|.
name|append
argument_list|(
name|randomReadableChar
argument_list|(
name|rand
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|keyBytes
init|=
name|k
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|()
decl_stmt|;
return|return
name|keyBytes
return|;
block|}
specifier|public
specifier|static
name|byte
index|[]
name|randomValue
parameter_list|(
name|Random
name|rand
parameter_list|)
block|{
name|StringBuilder
name|v
init|=
operator|new
name|StringBuilder
argument_list|()
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
literal|1
operator|+
name|rand
operator|.
name|nextInt
argument_list|(
literal|2000
argument_list|)
condition|;
operator|++
name|j
control|)
block|{
name|v
operator|.
name|append
argument_list|(
call|(
name|char
call|)
argument_list|(
literal|32
operator|+
name|rand
operator|.
name|nextInt
argument_list|(
literal|95
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|valueBytes
init|=
name|v
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|()
decl_stmt|;
return|return
name|valueBytes
return|;
block|}
specifier|public
specifier|static
specifier|final
name|char
name|randomReadableChar
parameter_list|(
name|Random
name|rand
parameter_list|)
block|{
name|int
name|i
init|=
name|rand
operator|.
name|nextInt
argument_list|(
literal|26
operator|*
literal|2
operator|+
literal|10
operator|+
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|<
literal|26
condition|)
return|return
call|(
name|char
call|)
argument_list|(
literal|'A'
operator|+
name|i
argument_list|)
return|;
name|i
operator|-=
literal|26
expr_stmt|;
if|if
condition|(
name|i
operator|<
literal|26
condition|)
return|return
call|(
name|char
call|)
argument_list|(
literal|'a'
operator|+
name|i
argument_list|)
return|;
name|i
operator|-=
literal|26
expr_stmt|;
if|if
condition|(
name|i
operator|<
literal|10
condition|)
return|return
call|(
name|char
call|)
argument_list|(
literal|'0'
operator|+
name|i
argument_list|)
return|;
name|i
operator|-=
literal|10
expr_stmt|;
assert|assert
name|i
operator|==
literal|0
assert|;
return|return
literal|'_'
return|;
block|}
specifier|public
specifier|static
name|byte
index|[]
name|randomRowOrQualifier
parameter_list|(
name|Random
name|rand
parameter_list|)
block|{
name|StringBuilder
name|field
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|int
name|fieldLen
init|=
name|MIN_ROW_OR_QUALIFIER_LENGTH
operator|+
name|rand
operator|.
name|nextInt
argument_list|(
name|MAX_ROW_OR_QUALIFIER_LENGTH
operator|-
name|MIN_ROW_OR_QUALIFIER_LENGTH
operator|+
literal|1
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
name|fieldLen
condition|;
operator|++
name|i
control|)
name|field
operator|.
name|append
argument_list|(
name|randomReadableChar
argument_list|(
name|rand
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|field
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|KeyValue
name|randomKeyValue
parameter_list|(
name|Random
name|rand
parameter_list|)
block|{
return|return
operator|new
name|KeyValue
argument_list|(
name|randomRowOrQualifier
argument_list|(
name|rand
argument_list|)
argument_list|,
name|COLUMN_FAMILY_NAME
operator|.
name|getBytes
argument_list|()
argument_list|,
name|randomRowOrQualifier
argument_list|(
name|rand
argument_list|)
argument_list|,
name|randomValue
argument_list|(
name|rand
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

