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
name|Collection
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
name|Tag
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
import|;
end_import

begin_comment
comment|/**  * Testing writing a version 3 {@link HFile}.  */
end_comment

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
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
name|TestHFileWriterV3
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
name|TestHFileWriterV3
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
specifier|private
name|boolean
name|useTags
decl_stmt|;
specifier|public
name|TestHFileWriterV3
parameter_list|(
name|boolean
name|useTags
parameter_list|)
block|{
name|this
operator|.
name|useTags
operator|=
name|useTags
expr_stmt|;
block|}
annotation|@
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
parameter_list|()
block|{
return|return
name|HBaseTestingUtility
operator|.
name|BOOLEAN_PARAMETERIZED
return|;
block|}
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
name|testHFileFormatV3
parameter_list|()
throws|throws
name|IOException
block|{
name|testHFileFormatV3Internals
argument_list|(
name|useTags
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testHFileFormatV3Internals
parameter_list|(
name|boolean
name|useTags
parameter_list|)
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
literal|"testHFileFormatV3"
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
argument_list|,
name|useTags
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
name|testMidKeyInHFileInternals
argument_list|(
name|useTags
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testMidKeyInHFileInternals
parameter_list|(
name|boolean
name|useTags
parameter_list|)
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
argument_list|,
name|useTags
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
parameter_list|,
name|boolean
name|useTags
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
name|withIncludesTags
argument_list|(
name|useTags
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
name|HFile
operator|.
name|Writer
name|writer
init|=
operator|new
name|HFileWriterFactory
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
name|withComparator
argument_list|(
name|KeyValue
operator|.
name|COMPARATOR
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
name|TestHFileWriterV2
operator|.
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
name|TestHFileWriterV2
operator|.
name|randomValue
argument_list|(
name|rand
argument_list|)
decl_stmt|;
name|KeyValue
name|keyValue
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|useTags
condition|)
block|{
name|ArrayList
argument_list|<
name|Tag
argument_list|>
name|tags
init|=
operator|new
name|ArrayList
argument_list|<
name|Tag
argument_list|>
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
literal|4
argument_list|)
condition|;
name|j
operator|++
control|)
block|{
name|byte
index|[]
name|tagBytes
init|=
operator|new
name|byte
index|[
literal|16
index|]
decl_stmt|;
name|rand
operator|.
name|nextBytes
argument_list|(
name|tagBytes
argument_list|)
expr_stmt|;
name|tags
operator|.
name|add
argument_list|(
operator|new
name|Tag
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|,
name|tagBytes
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|keyValue
operator|=
operator|new
name|KeyValue
argument_list|(
name|keyBytes
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|valueBytes
argument_list|,
name|tags
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|keyValue
operator|=
operator|new
name|KeyValue
argument_list|(
name|keyBytes
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|valueBytes
argument_list|)
expr_stmt|;
block|}
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
literal|3
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
name|withCompression
argument_list|(
name|compressAlgo
argument_list|)
operator|.
name|withIncludesMvcc
argument_list|(
literal|false
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
name|HFileWriterImpl
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
operator|.
name|unpack
argument_list|(
name|context
argument_list|,
name|blockReader
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
name|ByteBuffer
name|buf
init|=
name|block
operator|.
name|getBufferWithoutHeader
argument_list|()
decl_stmt|;
name|int
name|keyLen
init|=
operator|-
literal|1
decl_stmt|;
while|while
condition|(
name|buf
operator|.
name|hasRemaining
argument_list|()
condition|)
block|{
name|keyLen
operator|=
name|buf
operator|.
name|getInt
argument_list|()
expr_stmt|;
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
name|byte
index|[]
name|tagValue
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|useTags
condition|)
block|{
name|int
name|tagLen
init|=
operator|(
operator|(
name|buf
operator|.
name|get
argument_list|()
operator|&
literal|0xff
operator|)
operator|<<
literal|8
operator|)
operator|^
operator|(
name|buf
operator|.
name|get
argument_list|()
operator|&
literal|0xff
operator|)
decl_stmt|;
name|tagValue
operator|=
operator|new
name|byte
index|[
name|tagLen
index|]
expr_stmt|;
name|buf
operator|.
name|get
argument_list|(
name|tagValue
argument_list|)
expr_stmt|;
block|}
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
if|if
condition|(
name|useTags
condition|)
block|{
name|assertNotNull
argument_list|(
name|tagValue
argument_list|)
expr_stmt|;
name|KeyValue
name|tkv
init|=
name|keyValues
operator|.
name|get
argument_list|(
name|entriesRead
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|tagValue
operator|.
name|length
argument_list|,
name|tkv
operator|.
name|getTagsLength
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|tagValue
argument_list|,
literal|0
argument_list|,
name|tagValue
operator|.
name|length
argument_list|,
name|tkv
operator|.
name|getTagsArray
argument_list|()
argument_list|,
name|tkv
operator|.
name|getTagsOffset
argument_list|()
argument_list|,
name|tkv
operator|.
name|getTagsLength
argument_list|()
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
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
name|context
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
block|}
end_class

end_unit

