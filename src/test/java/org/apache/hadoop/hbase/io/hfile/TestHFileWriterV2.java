begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|Text
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

begin_comment
comment|/**  * Testing writing a version 2 {@link HFile}. This is a low-level test written  * during the development of {@link HFileWriterV2}.  */
end_comment

begin_class
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
name|HBaseTestingUtility
operator|.
name|getTestDir
argument_list|()
argument_list|,
literal|"testHFileFormatV2"
argument_list|)
decl_stmt|;
specifier|final
name|Compression
operator|.
name|Algorithm
name|COMPRESS_ALGO
init|=
name|Compression
operator|.
name|Algorithm
operator|.
name|GZ
decl_stmt|;
name|HFileWriterV2
name|writer
init|=
operator|new
name|HFileWriterV2
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|hfilePath
argument_list|,
literal|4096
argument_list|,
name|COMPRESS_ALGO
argument_list|,
name|KeyValue
operator|.
name|KEY_COMPARATOR
argument_list|)
decl_stmt|;
name|long
name|totalKeyLength
init|=
literal|0
decl_stmt|;
name|long
name|totalValueLength
init|=
literal|0
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
specifier|final
name|int
name|ENTRY_COUNT
init|=
literal|10000
decl_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|keys
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
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
name|ENTRY_COUNT
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
name|writer
operator|.
name|append
argument_list|(
name|keyBytes
argument_list|,
name|valueBytes
argument_list|)
expr_stmt|;
name|totalKeyLength
operator|+=
name|keyBytes
operator|.
name|length
expr_stmt|;
name|totalValueLength
operator|+=
name|valueBytes
operator|.
name|length
expr_stmt|;
name|keys
operator|.
name|add
argument_list|(
name|keyBytes
argument_list|)
expr_stmt|;
name|values
operator|.
name|add
argument_list|(
name|valueBytes
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
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ENTRY_COUNT
argument_list|,
name|trailer
operator|.
name|getEntryCount
argument_list|()
argument_list|)
expr_stmt|;
name|HFileBlock
operator|.
name|FSReader
name|blockReader
init|=
operator|new
name|HFileBlock
operator|.
name|FSReaderV2
argument_list|(
name|fsdis
argument_list|,
name|COMPRESS_ALGO
argument_list|,
name|fileSize
argument_list|)
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
comment|// A brute-force check to see that all keys and values are correct.
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|key
argument_list|,
name|keys
operator|.
name|get
argument_list|(
name|entriesRead
argument_list|)
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
name|values
operator|.
name|get
argument_list|(
name|entriesRead
argument_list|)
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
name|ENTRY_COUNT
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
name|block
operator|.
name|readInto
argument_list|(
name|t
argument_list|)
expr_stmt|;
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

