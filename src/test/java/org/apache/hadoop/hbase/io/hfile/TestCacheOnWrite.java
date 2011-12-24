begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|EnumMap
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
name|*
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
name|DataBlockEncodings
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
name|regionserver
operator|.
name|StoreFile
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
name|BloomFilterFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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

begin_comment
comment|/**  * Tests {@link HFile} cache-on-write functionality for the following block  * types: data blocks, non-root index blocks, and Bloom filter blocks.  */
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
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestCacheOnWrite
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
name|TestCacheOnWrite
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
name|CacheConfig
name|cacheConf
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
literal|12983177L
argument_list|)
decl_stmt|;
specifier|private
name|Path
name|storeFilePath
decl_stmt|;
specifier|private
name|BlockCache
name|blockCache
decl_stmt|;
specifier|private
name|String
name|testDescription
decl_stmt|;
specifier|private
specifier|final
name|CacheOnWriteType
name|cowType
decl_stmt|;
specifier|private
specifier|final
name|Compression
operator|.
name|Algorithm
name|compress
decl_stmt|;
specifier|private
specifier|final
name|BlockEncoderTestType
name|encoderType
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DATA_BLOCK_SIZE
init|=
literal|2048
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_KV
init|=
literal|25000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|INDEX_BLOCK_SIZE
init|=
literal|512
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|BLOOM_BLOCK_SIZE
init|=
literal|4096
decl_stmt|;
comment|/** The number of valid key types possible in a store file */
specifier|private
specifier|static
specifier|final
name|int
name|NUM_VALID_KEY_TYPES
init|=
name|KeyValue
operator|.
name|Type
operator|.
name|values
argument_list|()
operator|.
name|length
operator|-
literal|2
decl_stmt|;
specifier|private
specifier|static
enum|enum
name|CacheOnWriteType
block|{
name|DATA_BLOCKS
parameter_list|(
name|CacheConfig
operator|.
name|CACHE_BLOCKS_ON_WRITE_KEY
parameter_list|,
name|BlockType
operator|.
name|DATA
parameter_list|,
name|BlockType
operator|.
name|ENCODED_DATA
parameter_list|)
operator|,
constructor|BLOOM_BLOCKS(CacheConfig.CACHE_BLOOM_BLOCKS_ON_WRITE_KEY
operator|,
constructor|BlockType.BLOOM_CHUNK
block|)
enum|,
name|INDEX_BLOCKS
parameter_list|(
name|CacheConfig
operator|.
name|CACHE_INDEX_BLOCKS_ON_WRITE_KEY
parameter_list|,
name|BlockType
operator|.
name|LEAF_INDEX
parameter_list|,
name|BlockType
operator|.
name|INTERMEDIATE_INDEX
parameter_list|)
constructor_decl|;
specifier|private
specifier|final
name|String
name|confKey
decl_stmt|;
specifier|private
specifier|final
name|BlockType
name|blockType1
decl_stmt|;
specifier|private
specifier|final
name|BlockType
name|blockType2
decl_stmt|;
specifier|private
name|CacheOnWriteType
parameter_list|(
name|String
name|confKey
parameter_list|,
name|BlockType
name|blockType
parameter_list|)
block|{
name|this
argument_list|(
name|confKey
argument_list|,
name|blockType
argument_list|,
name|blockType
argument_list|)
expr_stmt|;
block|}
specifier|private
name|CacheOnWriteType
parameter_list|(
name|String
name|confKey
parameter_list|,
name|BlockType
name|blockType1
parameter_list|,
name|BlockType
name|blockType2
parameter_list|)
block|{
name|this
operator|.
name|blockType1
operator|=
name|blockType1
expr_stmt|;
name|this
operator|.
name|blockType2
operator|=
name|blockType2
expr_stmt|;
name|this
operator|.
name|confKey
operator|=
name|confKey
expr_stmt|;
block|}
specifier|public
name|boolean
name|shouldBeCached
parameter_list|(
name|BlockType
name|blockType
parameter_list|)
block|{
return|return
name|blockType
operator|==
name|blockType1
operator|||
name|blockType
operator|==
name|blockType2
return|;
block|}
specifier|public
name|void
name|modifyConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
for|for
control|(
name|CacheOnWriteType
name|cowType
range|:
name|CacheOnWriteType
operator|.
name|values
argument_list|()
control|)
block|{
name|conf
operator|.
name|setBoolean
argument_list|(
name|cowType
operator|.
name|confKey
argument_list|,
name|cowType
operator|==
name|this
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

begin_decl_stmt
specifier|private
specifier|static
specifier|final
name|DataBlockEncodings
operator|.
name|Algorithm
name|ENCODING_ALGO
init|=
name|DataBlockEncodings
operator|.
name|Algorithm
operator|.
name|PREFIX
decl_stmt|;
end_decl_stmt

begin_comment
comment|/** Provides fancy names for four combinations of two booleans */
end_comment

begin_enum
specifier|private
specifier|static
enum|enum
name|BlockEncoderTestType
block|{
name|NO_BLOCK_ENCODING
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
block|,
name|BLOCK_ENCODING_IN_CACHE_ONLY
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
block|,
name|BLOCK_ENCODING_ON_DISK_ONLY
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|)
block|,
name|BLOCK_ENCODING_EVERYWHERE
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
block|;
specifier|private
specifier|final
name|boolean
name|encodeOnDisk
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|encodeInCache
decl_stmt|;
name|BlockEncoderTestType
parameter_list|(
name|boolean
name|encodeOnDisk
parameter_list|,
name|boolean
name|encodeInCache
parameter_list|)
block|{
name|this
operator|.
name|encodeOnDisk
operator|=
name|encodeOnDisk
expr_stmt|;
name|this
operator|.
name|encodeInCache
operator|=
name|encodeInCache
expr_stmt|;
block|}
specifier|public
name|HFileDataBlockEncoder
name|getEncoder
parameter_list|()
block|{
comment|// We always use an encoded seeker. It should not have effect if there
comment|// is no encoding in cache.
return|return
operator|new
name|HFileDataBlockEncoderImpl
argument_list|(
name|encodeOnDisk
condition|?
name|ENCODING_ALGO
else|:
name|DataBlockEncodings
operator|.
name|Algorithm
operator|.
name|NONE
argument_list|,
name|encodeInCache
condition|?
name|ENCODING_ALGO
else|:
name|DataBlockEncodings
operator|.
name|Algorithm
operator|.
name|NONE
argument_list|,
literal|true
argument_list|)
return|;
block|}
block|}
end_enum

begin_constructor
specifier|public
name|TestCacheOnWrite
parameter_list|(
name|CacheOnWriteType
name|cowType
parameter_list|,
name|Compression
operator|.
name|Algorithm
name|compress
parameter_list|,
name|BlockEncoderTestType
name|encoderType
parameter_list|)
block|{
name|this
operator|.
name|cowType
operator|=
name|cowType
expr_stmt|;
name|this
operator|.
name|compress
operator|=
name|compress
expr_stmt|;
name|this
operator|.
name|encoderType
operator|=
name|encoderType
expr_stmt|;
name|testDescription
operator|=
literal|"[cacheOnWrite="
operator|+
name|cowType
operator|+
literal|", compress="
operator|+
name|compress
operator|+
literal|", encoderType="
operator|+
name|encoderType
operator|+
literal|"]"
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|testDescription
argument_list|)
expr_stmt|;
block|}
end_constructor

begin_function
annotation|@
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|getParameters
parameter_list|()
block|{
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|cowTypes
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
index|[]
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|CacheOnWriteType
name|cowType
range|:
name|CacheOnWriteType
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
name|compress
range|:
name|HBaseTestingUtility
operator|.
name|COMPRESSION_ALGORITHMS
control|)
block|{
for|for
control|(
name|BlockEncoderTestType
name|encoderType
range|:
name|BlockEncoderTestType
operator|.
name|values
argument_list|()
control|)
block|{
name|cowTypes
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
name|cowType
block|,
name|compress
block|,
name|encoderType
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|cowTypes
return|;
block|}
end_function

begin_function
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
name|conf
operator|.
name|setInt
argument_list|(
name|HFile
operator|.
name|FORMAT_VERSION_KEY
argument_list|,
name|HFile
operator|.
name|MAX_FORMAT_VERSION
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HFileBlockIndex
operator|.
name|MAX_CHUNK_SIZE_KEY
argument_list|,
name|INDEX_BLOCK_SIZE
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|BloomFilterFactory
operator|.
name|IO_STOREFILE_BLOOM_BLOCK_SIZE
argument_list|,
name|BLOOM_BLOCK_SIZE
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|CacheConfig
operator|.
name|CACHE_BLOCKS_ON_WRITE_KEY
argument_list|,
name|cowType
operator|.
name|shouldBeCached
argument_list|(
name|BlockType
operator|.
name|DATA
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|CacheConfig
operator|.
name|CACHE_INDEX_BLOCKS_ON_WRITE_KEY
argument_list|,
name|cowType
operator|.
name|shouldBeCached
argument_list|(
name|BlockType
operator|.
name|LEAF_INDEX
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|CacheConfig
operator|.
name|CACHE_BLOOM_BLOCKS_ON_WRITE_KEY
argument_list|,
name|cowType
operator|.
name|shouldBeCached
argument_list|(
name|BlockType
operator|.
name|BLOOM_CHUNK
argument_list|)
argument_list|)
expr_stmt|;
name|cowType
operator|.
name|modifyConf
argument_list|(
name|conf
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
name|cacheConf
operator|=
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|blockCache
operator|=
name|cacheConf
operator|.
name|getBlockCache
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"setUp()"
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
name|cacheConf
operator|=
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|blockCache
operator|=
name|cacheConf
operator|.
name|getBlockCache
argument_list|()
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|Test
specifier|public
name|void
name|testCacheOnWrite
parameter_list|()
throws|throws
name|IOException
block|{
name|writeStoreFile
argument_list|()
expr_stmt|;
name|readStoreFile
argument_list|()
expr_stmt|;
block|}
end_function

begin_function
specifier|private
name|void
name|readStoreFile
parameter_list|()
throws|throws
name|IOException
block|{
name|HFileReaderV2
name|reader
init|=
operator|(
name|HFileReaderV2
operator|)
name|HFile
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|storeFilePath
argument_list|,
name|cacheConf
argument_list|,
name|encoderType
operator|.
name|getEncoder
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"HFile information: "
operator|+
name|reader
argument_list|)
expr_stmt|;
name|HFileScanner
name|scanner
init|=
name|reader
operator|.
name|getScanner
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|testDescription
argument_list|,
name|scanner
operator|.
name|seekTo
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|offset
init|=
literal|0
decl_stmt|;
name|HFileBlock
name|prevBlock
init|=
literal|null
decl_stmt|;
name|EnumMap
argument_list|<
name|BlockType
argument_list|,
name|Integer
argument_list|>
name|blockCountByType
init|=
operator|new
name|EnumMap
argument_list|<
name|BlockType
argument_list|,
name|Integer
argument_list|>
argument_list|(
name|BlockType
operator|.
name|class
argument_list|)
decl_stmt|;
while|while
condition|(
name|offset
operator|<
name|reader
operator|.
name|getTrailer
argument_list|()
operator|.
name|getLoadOnOpenDataOffset
argument_list|()
condition|)
block|{
name|long
name|onDiskSize
init|=
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|prevBlock
operator|!=
literal|null
condition|)
block|{
name|onDiskSize
operator|=
name|prevBlock
operator|.
name|getNextBlockOnDiskSizeWithHeader
argument_list|()
expr_stmt|;
block|}
comment|// Flags: don't cache the block, use pread, this is not a compaction.
name|HFileBlock
name|block
init|=
name|reader
operator|.
name|readBlock
argument_list|(
name|offset
argument_list|,
name|onDiskSize
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|BlockCacheKey
name|blockCacheKey
init|=
name|HFile
operator|.
name|getBlockCacheKey
argument_list|(
name|reader
operator|.
name|getName
argument_list|()
argument_list|,
name|offset
argument_list|)
decl_stmt|;
name|boolean
name|isCached
init|=
name|blockCache
operator|.
name|getBlock
argument_list|(
name|blockCacheKey
argument_list|,
literal|true
argument_list|)
operator|!=
literal|null
decl_stmt|;
name|boolean
name|shouldBeCached
init|=
name|cowType
operator|.
name|shouldBeCached
argument_list|(
name|block
operator|.
name|getBlockType
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|testDescription
operator|+
literal|" "
operator|+
name|block
argument_list|,
name|shouldBeCached
argument_list|,
name|isCached
argument_list|)
expr_stmt|;
name|prevBlock
operator|=
name|block
expr_stmt|;
name|offset
operator|+=
name|block
operator|.
name|getOnDiskSizeWithHeader
argument_list|()
expr_stmt|;
name|BlockType
name|bt
init|=
name|block
operator|.
name|getBlockType
argument_list|()
decl_stmt|;
name|Integer
name|count
init|=
name|blockCountByType
operator|.
name|get
argument_list|(
name|bt
argument_list|)
decl_stmt|;
name|blockCountByType
operator|.
name|put
argument_list|(
name|bt
argument_list|,
operator|(
name|count
operator|==
literal|null
condition|?
literal|0
else|:
name|count
operator|)
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Block count by type: "
operator|+
name|blockCountByType
argument_list|)
expr_stmt|;
name|String
name|countByType
init|=
name|blockCountByType
operator|.
name|toString
argument_list|()
decl_stmt|;
name|BlockType
name|cachedDataBlockType
init|=
name|encoderType
operator|.
name|encodeInCache
condition|?
name|BlockType
operator|.
name|ENCODED_DATA
else|:
name|BlockType
operator|.
name|DATA
decl_stmt|;
name|assertEquals
argument_list|(
literal|"{"
operator|+
name|cachedDataBlockType
operator|+
literal|"=1379, LEAF_INDEX=173, BLOOM_CHUNK=9, INTERMEDIATE_INDEX=24}"
argument_list|,
name|countByType
argument_list|)
expr_stmt|;
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
end_function

begin_function
specifier|public
specifier|static
name|KeyValue
operator|.
name|Type
name|generateKeyType
parameter_list|(
name|Random
name|rand
parameter_list|)
block|{
if|if
condition|(
name|rand
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
comment|// Let's make half of KVs puts.
return|return
name|KeyValue
operator|.
name|Type
operator|.
name|Put
return|;
block|}
else|else
block|{
name|KeyValue
operator|.
name|Type
name|keyType
init|=
name|KeyValue
operator|.
name|Type
operator|.
name|values
argument_list|()
index|[
literal|1
operator|+
name|rand
operator|.
name|nextInt
argument_list|(
name|NUM_VALID_KEY_TYPES
argument_list|)
index|]
decl_stmt|;
if|if
condition|(
name|keyType
operator|==
name|KeyValue
operator|.
name|Type
operator|.
name|Minimum
operator|||
name|keyType
operator|==
name|KeyValue
operator|.
name|Type
operator|.
name|Maximum
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Generated an invalid key type: "
operator|+
name|keyType
operator|+
literal|". "
operator|+
literal|"Probably the layout of KeyValue.Type has changed."
argument_list|)
throw|;
block|}
return|return
name|keyType
return|;
block|}
block|}
end_function

begin_function
specifier|public
name|void
name|writeStoreFile
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|storeFileParentDir
init|=
operator|new
name|Path
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
literal|"test_cache_on_write"
argument_list|)
decl_stmt|;
name|StoreFile
operator|.
name|Writer
name|sfw
init|=
name|StoreFile
operator|.
name|createWriter
argument_list|(
name|fs
argument_list|,
name|storeFileParentDir
argument_list|,
name|DATA_BLOCK_SIZE
argument_list|,
name|compress
argument_list|,
name|encoderType
operator|.
name|getEncoder
argument_list|()
argument_list|,
name|KeyValue
operator|.
name|COMPARATOR
argument_list|,
name|conf
argument_list|,
name|cacheConf
argument_list|,
name|StoreFile
operator|.
name|BloomType
operator|.
name|ROWCOL
argument_list|,
name|NUM_KV
argument_list|)
decl_stmt|;
specifier|final
name|int
name|rowLen
init|=
literal|32
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
name|NUM_KV
condition|;
operator|++
name|i
control|)
block|{
name|byte
index|[]
name|k
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
name|byte
index|[]
name|v
init|=
name|TestHFileWriterV2
operator|.
name|randomValue
argument_list|(
name|rand
argument_list|)
decl_stmt|;
name|int
name|cfLen
init|=
name|rand
operator|.
name|nextInt
argument_list|(
name|k
operator|.
name|length
operator|-
name|rowLen
operator|+
literal|1
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|k
argument_list|,
literal|0
argument_list|,
name|rowLen
argument_list|,
name|k
argument_list|,
name|rowLen
argument_list|,
name|cfLen
argument_list|,
name|k
argument_list|,
name|rowLen
operator|+
name|cfLen
argument_list|,
name|k
operator|.
name|length
operator|-
name|rowLen
operator|-
name|cfLen
argument_list|,
name|rand
operator|.
name|nextLong
argument_list|()
argument_list|,
name|generateKeyType
argument_list|(
name|rand
argument_list|)
argument_list|,
name|v
argument_list|,
literal|0
argument_list|,
name|v
operator|.
name|length
argument_list|)
decl_stmt|;
name|sfw
operator|.
name|append
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
name|sfw
operator|.
name|close
argument_list|()
expr_stmt|;
name|storeFilePath
operator|=
name|sfw
operator|.
name|getPath
argument_list|()
expr_stmt|;
block|}
end_function

begin_decl_stmt
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
end_decl_stmt

unit|}
end_unit

