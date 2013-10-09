begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|encoding
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
name|assertArrayEquals
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
name|fail
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
name|DataOutputStream
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentSkipListSet
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
name|codec
operator|.
name|prefixtree
operator|.
name|PrefixTreeCodec
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
name|encoding
operator|.
name|DataBlockEncoder
operator|.
name|EncodedSeeker
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
name|HFileContext
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
name|HFileContextBuilder
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
name|CollectionBackedScanner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
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
comment|/**  * Tests scanning/seeking data with PrefixTree Encoding.  */
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
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestPrefixTreeEncoding
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
name|TestPrefixTreeEncoding
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CF
init|=
literal|"EncodingTestCF"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|CF_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|CF
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_ROWS_PER_BATCH
init|=
literal|50
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_COLS_PER_ROW
init|=
literal|20
decl_stmt|;
specifier|private
name|int
name|numBatchesWritten
init|=
literal|0
decl_stmt|;
specifier|private
name|ConcurrentSkipListSet
argument_list|<
name|KeyValue
argument_list|>
name|kvset
init|=
operator|new
name|ConcurrentSkipListSet
argument_list|<
name|KeyValue
argument_list|>
argument_list|(
name|KeyValue
operator|.
name|COMPARATOR
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|formatRowNum
init|=
literal|false
decl_stmt|;
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
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|paramList
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
index|[]
argument_list|>
argument_list|()
decl_stmt|;
block|{
name|paramList
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|false
block|}
argument_list|)
expr_stmt|;
name|paramList
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|true
block|}
argument_list|)
expr_stmt|;
block|}
return|return
name|paramList
return|;
block|}
specifier|private
specifier|final
name|boolean
name|includesTag
decl_stmt|;
specifier|public
name|TestPrefixTreeEncoding
parameter_list|(
name|boolean
name|includesTag
parameter_list|)
block|{
name|this
operator|.
name|includesTag
operator|=
name|includesTag
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|kvset
operator|.
name|clear
argument_list|()
expr_stmt|;
name|formatRowNum
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSeekBeforeWithFixedData
parameter_list|()
throws|throws
name|Exception
block|{
name|formatRowNum
operator|=
literal|true
expr_stmt|;
name|PrefixTreeCodec
name|encoder
init|=
operator|new
name|PrefixTreeCodec
argument_list|()
decl_stmt|;
name|int
name|batchId
init|=
name|numBatchesWritten
operator|++
decl_stmt|;
name|ByteBuffer
name|dataBuffer
init|=
name|generateFixedTestData
argument_list|(
name|kvset
argument_list|,
name|batchId
argument_list|,
literal|false
argument_list|,
name|includesTag
argument_list|)
decl_stmt|;
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withHBaseCheckSum
argument_list|(
literal|false
argument_list|)
operator|.
name|withIncludesMvcc
argument_list|(
literal|false
argument_list|)
operator|.
name|withIncludesTags
argument_list|(
name|includesTag
argument_list|)
operator|.
name|withCompressionAlgo
argument_list|(
name|Algorithm
operator|.
name|NONE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HFileBlockEncodingContext
name|blkEncodingCtx
init|=
operator|new
name|HFileBlockDefaultEncodingContext
argument_list|(
name|DataBlockEncoding
operator|.
name|PREFIX_TREE
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
name|meta
argument_list|)
decl_stmt|;
name|encoder
operator|.
name|encodeKeyValues
argument_list|(
name|dataBuffer
argument_list|,
name|blkEncodingCtx
argument_list|)
expr_stmt|;
name|EncodedSeeker
name|seeker
init|=
name|encoder
operator|.
name|createSeeker
argument_list|(
name|KeyValue
operator|.
name|COMPARATOR
argument_list|,
name|encoder
operator|.
name|newDataBlockDecodingContext
argument_list|(
name|meta
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|onDiskBytes
init|=
name|blkEncodingCtx
operator|.
name|getOnDiskBytesWithHeader
argument_list|()
decl_stmt|;
name|ByteBuffer
name|readBuffer
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|onDiskBytes
argument_list|,
name|DataBlockEncoding
operator|.
name|ID_SIZE
argument_list|,
name|onDiskBytes
operator|.
name|length
operator|-
name|DataBlockEncoding
operator|.
name|ID_SIZE
argument_list|)
decl_stmt|;
name|seeker
operator|.
name|setCurrentBuffer
argument_list|(
name|readBuffer
argument_list|)
expr_stmt|;
comment|// Seek before the first keyvalue;
name|KeyValue
name|seekKey
init|=
name|KeyValue
operator|.
name|createFirstDeleteFamilyOnRow
argument_list|(
name|getRowKey
argument_list|(
name|batchId
argument_list|,
literal|0
argument_list|)
argument_list|,
name|CF_BYTES
argument_list|)
decl_stmt|;
name|seeker
operator|.
name|seekToKeyInBlock
argument_list|(
name|seekKey
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|seekKey
operator|.
name|getKeyOffset
argument_list|()
argument_list|,
name|seekKey
operator|.
name|getKeyLength
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|seeker
operator|.
name|getKeyValue
argument_list|()
argument_list|)
expr_stmt|;
comment|// Seek before the middle keyvalue;
name|seekKey
operator|=
name|KeyValue
operator|.
name|createFirstDeleteFamilyOnRow
argument_list|(
name|getRowKey
argument_list|(
name|batchId
argument_list|,
name|NUM_ROWS_PER_BATCH
operator|/
literal|3
argument_list|)
argument_list|,
name|CF_BYTES
argument_list|)
expr_stmt|;
name|seeker
operator|.
name|seekToKeyInBlock
argument_list|(
name|seekKey
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|seekKey
operator|.
name|getKeyOffset
argument_list|()
argument_list|,
name|seekKey
operator|.
name|getKeyLength
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|seeker
operator|.
name|getKeyValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|getRowKey
argument_list|(
name|batchId
argument_list|,
name|NUM_ROWS_PER_BATCH
operator|/
literal|3
operator|-
literal|1
argument_list|)
argument_list|,
name|seeker
operator|.
name|getKeyValue
argument_list|()
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
comment|// Seek before the last keyvalue;
name|seekKey
operator|=
name|KeyValue
operator|.
name|createFirstDeleteFamilyOnRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzzz"
argument_list|)
argument_list|,
name|CF_BYTES
argument_list|)
expr_stmt|;
name|seeker
operator|.
name|seekToKeyInBlock
argument_list|(
name|seekKey
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|seekKey
operator|.
name|getKeyOffset
argument_list|()
argument_list|,
name|seekKey
operator|.
name|getKeyLength
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|seeker
operator|.
name|getKeyValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|getRowKey
argument_list|(
name|batchId
argument_list|,
name|NUM_ROWS_PER_BATCH
operator|-
literal|1
argument_list|)
argument_list|,
name|seeker
operator|.
name|getKeyValue
argument_list|()
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScanWithRandomData
parameter_list|()
throws|throws
name|Exception
block|{
name|PrefixTreeCodec
name|encoder
init|=
operator|new
name|PrefixTreeCodec
argument_list|()
decl_stmt|;
name|ByteBuffer
name|dataBuffer
init|=
name|generateRandomTestData
argument_list|(
name|kvset
argument_list|,
name|numBatchesWritten
operator|++
argument_list|,
name|includesTag
argument_list|)
decl_stmt|;
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withHBaseCheckSum
argument_list|(
literal|false
argument_list|)
operator|.
name|withIncludesMvcc
argument_list|(
literal|false
argument_list|)
operator|.
name|withIncludesTags
argument_list|(
name|includesTag
argument_list|)
operator|.
name|withCompressionAlgo
argument_list|(
name|Algorithm
operator|.
name|NONE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HFileBlockEncodingContext
name|blkEncodingCtx
init|=
operator|new
name|HFileBlockDefaultEncodingContext
argument_list|(
name|DataBlockEncoding
operator|.
name|PREFIX_TREE
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
name|meta
argument_list|)
decl_stmt|;
name|encoder
operator|.
name|encodeKeyValues
argument_list|(
name|dataBuffer
argument_list|,
name|blkEncodingCtx
argument_list|)
expr_stmt|;
name|EncodedSeeker
name|seeker
init|=
name|encoder
operator|.
name|createSeeker
argument_list|(
name|KeyValue
operator|.
name|COMPARATOR
argument_list|,
name|encoder
operator|.
name|newDataBlockDecodingContext
argument_list|(
name|meta
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|onDiskBytes
init|=
name|blkEncodingCtx
operator|.
name|getOnDiskBytesWithHeader
argument_list|()
decl_stmt|;
name|ByteBuffer
name|readBuffer
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|onDiskBytes
argument_list|,
name|DataBlockEncoding
operator|.
name|ID_SIZE
argument_list|,
name|onDiskBytes
operator|.
name|length
operator|-
name|DataBlockEncoding
operator|.
name|ID_SIZE
argument_list|)
decl_stmt|;
name|seeker
operator|.
name|setCurrentBuffer
argument_list|(
name|readBuffer
argument_list|)
expr_stmt|;
name|KeyValue
name|previousKV
init|=
literal|null
decl_stmt|;
do|do
block|{
name|KeyValue
name|currentKV
init|=
name|seeker
operator|.
name|getKeyValue
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|currentKV
argument_list|)
expr_stmt|;
if|if
condition|(
name|previousKV
operator|!=
literal|null
operator|&&
name|KeyValue
operator|.
name|COMPARATOR
operator|.
name|compare
argument_list|(
name|currentKV
argument_list|,
name|previousKV
argument_list|)
operator|<
literal|0
condition|)
block|{
name|dumpInputKVSet
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Current kv "
operator|+
name|currentKV
operator|+
literal|" is smaller than previous keyvalue "
operator|+
name|previousKV
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|includesTag
condition|)
block|{
name|assertFalse
argument_list|(
name|currentKV
operator|.
name|getTagsLength
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|currentKV
operator|.
name|getTagsLength
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
name|previousKV
operator|=
name|currentKV
expr_stmt|;
block|}
do|while
condition|(
name|seeker
operator|.
name|next
argument_list|()
condition|)
do|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSeekWithRandomData
parameter_list|()
throws|throws
name|Exception
block|{
name|PrefixTreeCodec
name|encoder
init|=
operator|new
name|PrefixTreeCodec
argument_list|()
decl_stmt|;
name|int
name|batchId
init|=
name|numBatchesWritten
operator|++
decl_stmt|;
name|ByteBuffer
name|dataBuffer
init|=
name|generateRandomTestData
argument_list|(
name|kvset
argument_list|,
name|batchId
argument_list|,
name|includesTag
argument_list|)
decl_stmt|;
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withHBaseCheckSum
argument_list|(
literal|false
argument_list|)
operator|.
name|withIncludesMvcc
argument_list|(
literal|false
argument_list|)
operator|.
name|withIncludesTags
argument_list|(
name|includesTag
argument_list|)
operator|.
name|withCompressionAlgo
argument_list|(
name|Algorithm
operator|.
name|NONE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HFileBlockEncodingContext
name|blkEncodingCtx
init|=
operator|new
name|HFileBlockDefaultEncodingContext
argument_list|(
name|DataBlockEncoding
operator|.
name|PREFIX_TREE
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
name|meta
argument_list|)
decl_stmt|;
name|encoder
operator|.
name|encodeKeyValues
argument_list|(
name|dataBuffer
argument_list|,
name|blkEncodingCtx
argument_list|)
expr_stmt|;
name|EncodedSeeker
name|seeker
init|=
name|encoder
operator|.
name|createSeeker
argument_list|(
name|KeyValue
operator|.
name|COMPARATOR
argument_list|,
name|encoder
operator|.
name|newDataBlockDecodingContext
argument_list|(
name|meta
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|onDiskBytes
init|=
name|blkEncodingCtx
operator|.
name|getOnDiskBytesWithHeader
argument_list|()
decl_stmt|;
name|ByteBuffer
name|readBuffer
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|onDiskBytes
argument_list|,
name|DataBlockEncoding
operator|.
name|ID_SIZE
argument_list|,
name|onDiskBytes
operator|.
name|length
operator|-
name|DataBlockEncoding
operator|.
name|ID_SIZE
argument_list|)
decl_stmt|;
name|verifySeeking
argument_list|(
name|seeker
argument_list|,
name|readBuffer
argument_list|,
name|batchId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSeekWithFixedData
parameter_list|()
throws|throws
name|Exception
block|{
name|PrefixTreeCodec
name|encoder
init|=
operator|new
name|PrefixTreeCodec
argument_list|()
decl_stmt|;
name|int
name|batchId
init|=
name|numBatchesWritten
operator|++
decl_stmt|;
name|ByteBuffer
name|dataBuffer
init|=
name|generateFixedTestData
argument_list|(
name|kvset
argument_list|,
name|batchId
argument_list|,
name|includesTag
argument_list|)
decl_stmt|;
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withHBaseCheckSum
argument_list|(
literal|false
argument_list|)
operator|.
name|withIncludesMvcc
argument_list|(
literal|false
argument_list|)
operator|.
name|withIncludesTags
argument_list|(
name|includesTag
argument_list|)
operator|.
name|withCompressionAlgo
argument_list|(
name|Algorithm
operator|.
name|NONE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HFileBlockEncodingContext
name|blkEncodingCtx
init|=
operator|new
name|HFileBlockDefaultEncodingContext
argument_list|(
name|DataBlockEncoding
operator|.
name|PREFIX_TREE
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
name|meta
argument_list|)
decl_stmt|;
name|encoder
operator|.
name|encodeKeyValues
argument_list|(
name|dataBuffer
argument_list|,
name|blkEncodingCtx
argument_list|)
expr_stmt|;
name|EncodedSeeker
name|seeker
init|=
name|encoder
operator|.
name|createSeeker
argument_list|(
name|KeyValue
operator|.
name|COMPARATOR
argument_list|,
name|encoder
operator|.
name|newDataBlockDecodingContext
argument_list|(
name|meta
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|onDiskBytes
init|=
name|blkEncodingCtx
operator|.
name|getOnDiskBytesWithHeader
argument_list|()
decl_stmt|;
name|ByteBuffer
name|readBuffer
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|onDiskBytes
argument_list|,
name|DataBlockEncoding
operator|.
name|ID_SIZE
argument_list|,
name|onDiskBytes
operator|.
name|length
operator|-
name|DataBlockEncoding
operator|.
name|ID_SIZE
argument_list|)
decl_stmt|;
name|verifySeeking
argument_list|(
name|seeker
argument_list|,
name|readBuffer
argument_list|,
name|batchId
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifySeeking
parameter_list|(
name|EncodedSeeker
name|encodeSeeker
parameter_list|,
name|ByteBuffer
name|encodedData
parameter_list|,
name|int
name|batchId
parameter_list|)
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvList
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
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
name|NUM_ROWS_PER_BATCH
condition|;
operator|++
name|i
control|)
block|{
name|kvList
operator|.
name|clear
argument_list|()
expr_stmt|;
name|encodeSeeker
operator|.
name|setCurrentBuffer
argument_list|(
name|encodedData
argument_list|)
expr_stmt|;
name|KeyValue
name|firstOnRow
init|=
name|KeyValue
operator|.
name|createFirstOnRow
argument_list|(
name|getRowKey
argument_list|(
name|batchId
argument_list|,
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|encodeSeeker
operator|.
name|seekToKeyInBlock
argument_list|(
name|firstOnRow
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|firstOnRow
operator|.
name|getKeyOffset
argument_list|()
argument_list|,
name|firstOnRow
operator|.
name|getKeyLength
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|boolean
name|hasMoreOfEncodeScanner
init|=
name|encodeSeeker
operator|.
name|next
argument_list|()
decl_stmt|;
name|CollectionBackedScanner
name|collectionScanner
init|=
operator|new
name|CollectionBackedScanner
argument_list|(
name|this
operator|.
name|kvset
argument_list|)
decl_stmt|;
name|boolean
name|hasMoreOfCollectionScanner
init|=
name|collectionScanner
operator|.
name|seek
argument_list|(
name|firstOnRow
argument_list|)
decl_stmt|;
if|if
condition|(
name|hasMoreOfEncodeScanner
operator|!=
name|hasMoreOfCollectionScanner
condition|)
block|{
name|dumpInputKVSet
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Get error result after seeking "
operator|+
name|firstOnRow
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hasMoreOfEncodeScanner
condition|)
block|{
if|if
condition|(
name|KeyValue
operator|.
name|COMPARATOR
operator|.
name|compare
argument_list|(
name|encodeSeeker
operator|.
name|getKeyValue
argument_list|()
argument_list|,
name|collectionScanner
operator|.
name|peek
argument_list|()
argument_list|)
operator|!=
literal|0
condition|)
block|{
name|dumpInputKVSet
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Expected "
operator|+
name|collectionScanner
operator|.
name|peek
argument_list|()
operator|+
literal|" actual "
operator|+
name|encodeSeeker
operator|.
name|getKeyValue
argument_list|()
operator|+
literal|", after seeking "
operator|+
name|firstOnRow
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|dumpInputKVSet
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Dumping input keyvalue set in error case:"
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvset
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|ByteBuffer
name|generateFixedTestData
parameter_list|(
name|ConcurrentSkipListSet
argument_list|<
name|KeyValue
argument_list|>
name|kvset
parameter_list|,
name|int
name|batchId
parameter_list|,
name|boolean
name|useTags
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|generateFixedTestData
argument_list|(
name|kvset
argument_list|,
name|batchId
argument_list|,
literal|true
argument_list|,
name|useTags
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|ByteBuffer
name|generateFixedTestData
parameter_list|(
name|ConcurrentSkipListSet
argument_list|<
name|KeyValue
argument_list|>
name|kvset
parameter_list|,
name|int
name|batchId
parameter_list|,
name|boolean
name|partial
parameter_list|,
name|boolean
name|useTags
parameter_list|)
throws|throws
name|Exception
block|{
name|ByteArrayOutputStream
name|baosInMemory
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|userDataStream
init|=
operator|new
name|DataOutputStream
argument_list|(
name|baosInMemory
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
name|NUM_ROWS_PER_BATCH
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|partial
operator|&&
name|i
operator|/
literal|10
operator|%
literal|2
operator|==
literal|1
condition|)
continue|continue;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|NUM_COLS_PER_ROW
condition|;
operator|++
name|j
control|)
block|{
if|if
condition|(
operator|!
name|useTags
condition|)
block|{
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|getRowKey
argument_list|(
name|batchId
argument_list|,
name|i
argument_list|)
argument_list|,
name|CF_BYTES
argument_list|,
name|getQualifier
argument_list|(
name|j
argument_list|)
argument_list|,
name|getValue
argument_list|(
name|batchId
argument_list|,
name|i
argument_list|,
name|j
argument_list|)
argument_list|)
decl_stmt|;
name|kvset
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|getRowKey
argument_list|(
name|batchId
argument_list|,
name|i
argument_list|)
argument_list|,
name|CF_BYTES
argument_list|,
name|getQualifier
argument_list|(
name|j
argument_list|)
argument_list|,
literal|0l
argument_list|,
name|getValue
argument_list|(
name|batchId
argument_list|,
name|i
argument_list|,
name|j
argument_list|)
argument_list|,
operator|new
name|Tag
index|[]
block|{
operator|new
name|Tag
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|,
literal|"metaValue1"
argument_list|)
block|}
argument_list|)
decl_stmt|;
name|kvset
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
block|}
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvset
control|)
block|{
name|userDataStream
operator|.
name|writeInt
argument_list|(
name|kv
operator|.
name|getKeyLength
argument_list|()
argument_list|)
expr_stmt|;
name|userDataStream
operator|.
name|writeInt
argument_list|(
name|kv
operator|.
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
name|userDataStream
operator|.
name|write
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getKeyOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getKeyLength
argument_list|()
argument_list|)
expr_stmt|;
name|userDataStream
operator|.
name|write
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|useTags
condition|)
block|{
name|userDataStream
operator|.
name|writeShort
argument_list|(
name|kv
operator|.
name|getTagsLength
argument_list|()
argument_list|)
expr_stmt|;
name|userDataStream
operator|.
name|write
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueOffset
argument_list|()
operator|+
name|kv
operator|.
name|getValueLength
argument_list|()
operator|+
name|Bytes
operator|.
name|SIZEOF_SHORT
argument_list|,
name|kv
operator|.
name|getTagsLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|baosInMemory
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|ByteBuffer
name|generateRandomTestData
parameter_list|(
name|ConcurrentSkipListSet
argument_list|<
name|KeyValue
argument_list|>
name|kvset
parameter_list|,
name|int
name|batchId
parameter_list|,
name|boolean
name|useTags
parameter_list|)
throws|throws
name|Exception
block|{
name|ByteArrayOutputStream
name|baosInMemory
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|userDataStream
init|=
operator|new
name|DataOutputStream
argument_list|(
name|baosInMemory
argument_list|)
decl_stmt|;
name|Random
name|random
init|=
operator|new
name|Random
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
name|NUM_ROWS_PER_BATCH
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|random
operator|.
name|nextInt
argument_list|(
literal|100
argument_list|)
operator|<
literal|50
condition|)
continue|continue;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|NUM_COLS_PER_ROW
condition|;
operator|++
name|j
control|)
block|{
if|if
condition|(
name|random
operator|.
name|nextInt
argument_list|(
literal|100
argument_list|)
operator|<
literal|50
condition|)
continue|continue;
if|if
condition|(
operator|!
name|useTags
condition|)
block|{
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|getRowKey
argument_list|(
name|batchId
argument_list|,
name|i
argument_list|)
argument_list|,
name|CF_BYTES
argument_list|,
name|getQualifier
argument_list|(
name|j
argument_list|)
argument_list|,
name|getValue
argument_list|(
name|batchId
argument_list|,
name|i
argument_list|,
name|j
argument_list|)
argument_list|)
decl_stmt|;
name|kvset
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|getRowKey
argument_list|(
name|batchId
argument_list|,
name|i
argument_list|)
argument_list|,
name|CF_BYTES
argument_list|,
name|getQualifier
argument_list|(
name|j
argument_list|)
argument_list|,
literal|0l
argument_list|,
name|getValue
argument_list|(
name|batchId
argument_list|,
name|i
argument_list|,
name|j
argument_list|)
argument_list|,
operator|new
name|Tag
index|[]
block|{
operator|new
name|Tag
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|,
literal|"metaValue1"
argument_list|)
block|}
argument_list|)
decl_stmt|;
name|kvset
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
block|}
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvset
control|)
block|{
name|userDataStream
operator|.
name|writeInt
argument_list|(
name|kv
operator|.
name|getKeyLength
argument_list|()
argument_list|)
expr_stmt|;
name|userDataStream
operator|.
name|writeInt
argument_list|(
name|kv
operator|.
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
name|userDataStream
operator|.
name|write
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getKeyOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getKeyLength
argument_list|()
argument_list|)
expr_stmt|;
name|userDataStream
operator|.
name|write
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|useTags
condition|)
block|{
name|userDataStream
operator|.
name|writeShort
argument_list|(
name|kv
operator|.
name|getTagsLength
argument_list|()
argument_list|)
expr_stmt|;
name|userDataStream
operator|.
name|write
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueOffset
argument_list|()
operator|+
name|kv
operator|.
name|getValueLength
argument_list|()
operator|+
name|Bytes
operator|.
name|SIZEOF_SHORT
argument_list|,
name|kv
operator|.
name|getTagsLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|baosInMemory
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|byte
index|[]
name|getRowKey
parameter_list|(
name|int
name|batchId
parameter_list|,
name|int
name|i
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"batch"
operator|+
name|batchId
operator|+
literal|"_row"
operator|+
operator|(
name|formatRowNum
condition|?
name|String
operator|.
name|format
argument_list|(
literal|"%04d"
argument_list|,
name|i
argument_list|)
else|:
name|i
operator|)
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|byte
index|[]
name|getQualifier
parameter_list|(
name|int
name|j
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"colfdfafhfhsdfhsdfh"
operator|+
name|j
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|byte
index|[]
name|getValue
parameter_list|(
name|int
name|batchId
parameter_list|,
name|int
name|i
parameter_list|,
name|int
name|j
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value_for_"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|getRowKey
argument_list|(
name|batchId
argument_list|,
name|i
argument_list|)
argument_list|)
operator|+
literal|"_col"
operator|+
name|j
argument_list|)
return|;
block|}
block|}
end_class

end_unit

