begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|io
operator|.
name|ByteArrayOutputStream
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
name|HeapSize
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
name|io
operator|.
name|encoding
operator|.
name|HFileBlockDefaultEncodingContext
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
name|HFileBlockEncodingContext
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
name|TestHFileDataBlockEncoder
block|{
specifier|private
name|HFileDataBlockEncoder
name|blockEncoder
decl_stmt|;
specifier|private
name|RedundantKVGenerator
name|generator
init|=
operator|new
name|RedundantKVGenerator
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|includesMemstoreTS
decl_stmt|;
comment|/**    * Create test for given data block encoding configuration.    * @param blockEncoder What kind of encoding policy will be used.    */
specifier|public
name|TestHFileDataBlockEncoder
parameter_list|(
name|HFileDataBlockEncoder
name|blockEncoder
parameter_list|,
name|boolean
name|includesMemstoreTS
parameter_list|)
block|{
name|this
operator|.
name|blockEncoder
operator|=
name|blockEncoder
expr_stmt|;
name|this
operator|.
name|includesMemstoreTS
operator|=
name|includesMemstoreTS
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Encoding: "
operator|+
name|blockEncoder
operator|.
name|getDataBlockEncoding
argument_list|()
operator|+
literal|", includesMemstoreTS: "
operator|+
name|includesMemstoreTS
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test putting and taking out blocks into cache with different    * encoding options.    */
annotation|@
name|Test
specifier|public
name|void
name|testEncodingWithCache
parameter_list|()
throws|throws
name|IOException
block|{
name|testEncodingWithCacheInternals
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|testEncodingWithCacheInternals
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testEncodingWithCacheInternals
parameter_list|(
name|boolean
name|useTag
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
name|generator
operator|.
name|generateTestKeyValues
argument_list|(
literal|60
argument_list|,
name|useTag
argument_list|)
decl_stmt|;
name|HFileBlock
name|block
init|=
name|getSampleHFileBlock
argument_list|(
name|kvs
argument_list|,
name|useTag
argument_list|)
decl_stmt|;
name|HFileBlock
name|cacheBlock
init|=
name|createBlockOnDisk
argument_list|(
name|kvs
argument_list|,
name|block
argument_list|,
name|useTag
argument_list|)
decl_stmt|;
name|LruBlockCache
name|blockCache
init|=
operator|new
name|LruBlockCache
argument_list|(
literal|8
operator|*
literal|1024
operator|*
literal|1024
argument_list|,
literal|32
operator|*
literal|1024
argument_list|)
decl_stmt|;
name|BlockCacheKey
name|cacheKey
init|=
operator|new
name|BlockCacheKey
argument_list|(
literal|"test"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|blockCache
operator|.
name|cacheBlock
argument_list|(
name|cacheKey
argument_list|,
name|cacheBlock
argument_list|)
expr_stmt|;
name|HeapSize
name|heapSize
init|=
name|blockCache
operator|.
name|getBlock
argument_list|(
name|cacheKey
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|heapSize
operator|instanceof
name|HFileBlock
argument_list|)
expr_stmt|;
name|HFileBlock
name|returnedBlock
init|=
operator|(
name|HFileBlock
operator|)
name|heapSize
decl_stmt|;
empty_stmt|;
if|if
condition|(
name|blockEncoder
operator|.
name|getDataBlockEncoding
argument_list|()
operator|==
name|DataBlockEncoding
operator|.
name|NONE
condition|)
block|{
name|assertEquals
argument_list|(
name|block
operator|.
name|getBufferReadOnly
argument_list|()
argument_list|,
name|returnedBlock
operator|.
name|getBufferReadOnly
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|BlockType
operator|.
name|ENCODED_DATA
operator|!=
name|returnedBlock
operator|.
name|getBlockType
argument_list|()
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|blockEncoder
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|BlockType
operator|.
name|ENCODED_DATA
argument_list|,
name|returnedBlock
operator|.
name|getBlockType
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Test for HBASE-5746. */
annotation|@
name|Test
specifier|public
name|void
name|testHeaderSizeInCacheWithoutChecksum
parameter_list|()
throws|throws
name|Exception
block|{
name|testHeaderSizeInCacheWithoutChecksumInternals
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|testHeaderSizeInCacheWithoutChecksumInternals
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testHeaderSizeInCacheWithoutChecksumInternals
parameter_list|(
name|boolean
name|useTags
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|headerSize
init|=
name|HConstants
operator|.
name|HFILEBLOCK_HEADER_SIZE_NO_CHECKSUM
decl_stmt|;
comment|// Create some KVs and create the block with old-style header.
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
name|generator
operator|.
name|generateTestKeyValues
argument_list|(
literal|60
argument_list|,
name|useTags
argument_list|)
decl_stmt|;
name|ByteBuffer
name|keyValues
init|=
name|RedundantKVGenerator
operator|.
name|convertKvToByteBuffer
argument_list|(
name|kvs
argument_list|,
name|includesMemstoreTS
argument_list|)
decl_stmt|;
name|int
name|size
init|=
name|keyValues
operator|.
name|limit
argument_list|()
decl_stmt|;
name|ByteBuffer
name|buf
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|size
operator|+
name|headerSize
argument_list|)
decl_stmt|;
name|buf
operator|.
name|position
argument_list|(
name|headerSize
argument_list|)
expr_stmt|;
name|keyValues
operator|.
name|rewind
argument_list|()
expr_stmt|;
name|buf
operator|.
name|put
argument_list|(
name|keyValues
argument_list|)
expr_stmt|;
name|HFileContext
name|hfileContext
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
name|includesMemstoreTS
argument_list|)
operator|.
name|withIncludesTags
argument_list|(
name|useTags
argument_list|)
operator|.
name|withBlockSize
argument_list|(
literal|0
argument_list|)
operator|.
name|withChecksumType
argument_list|(
name|ChecksumType
operator|.
name|NULL
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HFileBlock
name|block
init|=
operator|new
name|HFileBlock
argument_list|(
name|BlockType
operator|.
name|DATA
argument_list|,
name|size
argument_list|,
name|size
argument_list|,
operator|-
literal|1
argument_list|,
name|buf
argument_list|,
name|HFileBlock
operator|.
name|FILL_HEADER
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
name|hfileContext
argument_list|)
decl_stmt|;
name|HFileBlock
name|cacheBlock
init|=
name|createBlockOnDisk
argument_list|(
name|kvs
argument_list|,
name|block
argument_list|,
name|useTags
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|headerSize
argument_list|,
name|cacheBlock
operator|.
name|getDummyHeaderForVersion
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test encoding.    * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testEncoding
parameter_list|()
throws|throws
name|IOException
block|{
name|testEncodingInternals
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|testEncodingInternals
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test encoding with offheap keyvalue. This test just verifies if the encoders    * work with DBB and does not use the getXXXArray() API    * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testEncodingWithOffheapKeyValue
parameter_list|()
throws|throws
name|IOException
block|{
comment|// usually we have just block without headers, but don't complicate that
try|try
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|kvs
init|=
name|generator
operator|.
name|generateTestExtendedOffheapKeyValues
argument_list|(
literal|60
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withIncludesMvcc
argument_list|(
name|includesMemstoreTS
argument_list|)
operator|.
name|withIncludesTags
argument_list|(
literal|true
argument_list|)
operator|.
name|withHBaseCheckSum
argument_list|(
literal|true
argument_list|)
operator|.
name|withCompression
argument_list|(
name|Algorithm
operator|.
name|NONE
argument_list|)
operator|.
name|withBlockSize
argument_list|(
literal|0
argument_list|)
operator|.
name|withChecksumType
argument_list|(
name|ChecksumType
operator|.
name|NULL
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|writeBlock
argument_list|(
name|kvs
argument_list|,
name|meta
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"No exception should have been thrown"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|testEncodingInternals
parameter_list|(
name|boolean
name|useTag
parameter_list|)
throws|throws
name|IOException
block|{
comment|// usually we have just block without headers, but don't complicate that
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
name|generator
operator|.
name|generateTestKeyValues
argument_list|(
literal|60
argument_list|,
name|useTag
argument_list|)
decl_stmt|;
name|HFileBlock
name|block
init|=
name|getSampleHFileBlock
argument_list|(
name|kvs
argument_list|,
name|useTag
argument_list|)
decl_stmt|;
name|HFileBlock
name|blockOnDisk
init|=
name|createBlockOnDisk
argument_list|(
name|kvs
argument_list|,
name|block
argument_list|,
name|useTag
argument_list|)
decl_stmt|;
if|if
condition|(
name|blockEncoder
operator|.
name|getDataBlockEncoding
argument_list|()
operator|!=
name|DataBlockEncoding
operator|.
name|NONE
condition|)
block|{
name|assertEquals
argument_list|(
name|BlockType
operator|.
name|ENCODED_DATA
argument_list|,
name|blockOnDisk
operator|.
name|getBlockType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|blockEncoder
operator|.
name|getDataBlockEncoding
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
name|blockOnDisk
operator|.
name|getDataBlockEncodingId
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|BlockType
operator|.
name|DATA
argument_list|,
name|blockOnDisk
operator|.
name|getBlockType
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|HFileBlock
name|getSampleHFileBlock
parameter_list|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
parameter_list|,
name|boolean
name|useTag
parameter_list|)
block|{
name|ByteBuffer
name|keyValues
init|=
name|RedundantKVGenerator
operator|.
name|convertKvToByteBuffer
argument_list|(
name|kvs
argument_list|,
name|includesMemstoreTS
argument_list|)
decl_stmt|;
name|int
name|size
init|=
name|keyValues
operator|.
name|limit
argument_list|()
decl_stmt|;
name|ByteBuffer
name|buf
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|size
operator|+
name|HConstants
operator|.
name|HFILEBLOCK_HEADER_SIZE
argument_list|)
decl_stmt|;
name|buf
operator|.
name|position
argument_list|(
name|HConstants
operator|.
name|HFILEBLOCK_HEADER_SIZE
argument_list|)
expr_stmt|;
name|keyValues
operator|.
name|rewind
argument_list|()
expr_stmt|;
name|buf
operator|.
name|put
argument_list|(
name|keyValues
argument_list|)
expr_stmt|;
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withIncludesMvcc
argument_list|(
name|includesMemstoreTS
argument_list|)
operator|.
name|withIncludesTags
argument_list|(
name|useTag
argument_list|)
operator|.
name|withHBaseCheckSum
argument_list|(
literal|true
argument_list|)
operator|.
name|withCompression
argument_list|(
name|Algorithm
operator|.
name|NONE
argument_list|)
operator|.
name|withBlockSize
argument_list|(
literal|0
argument_list|)
operator|.
name|withChecksumType
argument_list|(
name|ChecksumType
operator|.
name|NULL
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HFileBlock
name|b
init|=
operator|new
name|HFileBlock
argument_list|(
name|BlockType
operator|.
name|DATA
argument_list|,
name|size
argument_list|,
name|size
argument_list|,
operator|-
literal|1
argument_list|,
name|buf
argument_list|,
name|HFileBlock
operator|.
name|FILL_HEADER
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
name|meta
argument_list|)
decl_stmt|;
return|return
name|b
return|;
block|}
specifier|private
name|HFileBlock
name|createBlockOnDisk
parameter_list|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
parameter_list|,
name|HFileBlock
name|block
parameter_list|,
name|boolean
name|useTags
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|size
decl_stmt|;
name|HFileBlockEncodingContext
name|context
init|=
operator|new
name|HFileBlockDefaultEncodingContext
argument_list|(
name|blockEncoder
operator|.
name|getDataBlockEncoding
argument_list|()
argument_list|,
name|HConstants
operator|.
name|HFILEBLOCK_DUMMY_HEADER
argument_list|,
name|block
operator|.
name|getHFileContext
argument_list|()
argument_list|)
decl_stmt|;
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|baos
operator|.
name|write
argument_list|(
name|block
operator|.
name|getDummyHeaderForVersion
argument_list|()
argument_list|)
expr_stmt|;
name|DataOutputStream
name|dos
init|=
operator|new
name|DataOutputStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|blockEncoder
operator|.
name|startBlockEncoding
argument_list|(
name|context
argument_list|,
name|dos
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvs
control|)
block|{
name|blockEncoder
operator|.
name|encode
argument_list|(
name|kv
argument_list|,
name|context
argument_list|,
name|dos
argument_list|)
expr_stmt|;
block|}
name|blockEncoder
operator|.
name|endBlockEncoding
argument_list|(
name|context
argument_list|,
name|dos
argument_list|,
name|baos
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|BlockType
operator|.
name|DATA
argument_list|)
expr_stmt|;
name|byte
index|[]
name|encodedBytes
init|=
name|baos
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|size
operator|=
name|encodedBytes
operator|.
name|length
operator|-
name|block
operator|.
name|getDummyHeaderForVersion
argument_list|()
operator|.
name|length
expr_stmt|;
return|return
operator|new
name|HFileBlock
argument_list|(
name|context
operator|.
name|getBlockType
argument_list|()
argument_list|,
name|size
argument_list|,
name|size
argument_list|,
operator|-
literal|1
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|encodedBytes
argument_list|)
argument_list|,
name|HFileBlock
operator|.
name|FILL_HEADER
argument_list|,
literal|0
argument_list|,
name|block
operator|.
name|getOnDiskDataSizeWithHeader
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|,
name|block
operator|.
name|getHFileContext
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|void
name|writeBlock
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|kvs
parameter_list|,
name|HFileContext
name|fileContext
parameter_list|,
name|boolean
name|useTags
parameter_list|)
throws|throws
name|IOException
block|{
name|HFileBlockEncodingContext
name|context
init|=
operator|new
name|HFileBlockDefaultEncodingContext
argument_list|(
name|blockEncoder
operator|.
name|getDataBlockEncoding
argument_list|()
argument_list|,
name|HConstants
operator|.
name|HFILEBLOCK_DUMMY_HEADER
argument_list|,
name|fileContext
argument_list|)
decl_stmt|;
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|baos
operator|.
name|write
argument_list|(
name|HConstants
operator|.
name|HFILEBLOCK_DUMMY_HEADER
argument_list|)
expr_stmt|;
name|DataOutputStream
name|dos
init|=
operator|new
name|DataOutputStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|blockEncoder
operator|.
name|startBlockEncoding
argument_list|(
name|context
argument_list|,
name|dos
argument_list|)
expr_stmt|;
for|for
control|(
name|Cell
name|kv
range|:
name|kvs
control|)
block|{
name|blockEncoder
operator|.
name|encode
argument_list|(
name|kv
argument_list|,
name|context
argument_list|,
name|dos
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @return All possible data block encoding configurations    */
annotation|@
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|getAllConfigurations
parameter_list|()
block|{
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|configurations
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|DataBlockEncoding
name|diskAlgo
range|:
name|DataBlockEncoding
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|boolean
name|includesMemstoreTS
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
name|HFileDataBlockEncoder
name|dbe
init|=
operator|(
name|diskAlgo
operator|==
name|DataBlockEncoding
operator|.
name|NONE
operator|)
condition|?
name|NoOpDataBlockEncoder
operator|.
name|INSTANCE
else|:
operator|new
name|HFileDataBlockEncoderImpl
argument_list|(
name|diskAlgo
argument_list|)
decl_stmt|;
name|configurations
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
name|dbe
block|,
operator|new
name|Boolean
argument_list|(
name|includesMemstoreTS
argument_list|)
block|}
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|configurations
return|;
block|}
block|}
end_class

end_unit

