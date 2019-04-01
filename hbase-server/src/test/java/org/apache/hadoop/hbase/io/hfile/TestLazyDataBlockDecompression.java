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
name|assertTrue
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
name|Arrays
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
name|Random
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterables
import|;
end_import

begin_comment
comment|/**  * A kind of integration test at the intersection of {@link HFileBlock}, {@link CacheConfig},  * and {@link LruBlockCache}.  */
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
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestLazyDataBlockDecompression
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
name|TestLazyDataBlockDecompression
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
name|TestLazyDataBlockDecompression
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
name|FileSystem
name|fs
decl_stmt|;
annotation|@
name|Parameterized
operator|.
name|Parameter
argument_list|(
literal|0
argument_list|)
specifier|public
name|boolean
name|cacheOnWrite
decl_stmt|;
annotation|@
name|Parameterized
operator|.
name|Parameters
specifier|public
specifier|static
name|Iterable
argument_list|<
name|Object
index|[]
argument_list|>
name|data
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Object
index|[]
index|[]
block|{
block|{
literal|false
block|}
block|,
block|{
literal|true
block|}
block|}
argument_list|)
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
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
name|fs
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Write {@code entryCount} random keyvalues to a new HFile at {@code path}. Returns the row    * bytes of the KeyValues written, in the order they were written.    */
specifier|private
specifier|static
name|void
name|writeHFile
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|CacheConfig
name|cc
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|HFileContext
name|cxt
parameter_list|,
name|int
name|entryCount
parameter_list|)
throws|throws
name|IOException
block|{
name|HFile
operator|.
name|Writer
name|writer
init|=
operator|new
name|HFile
operator|.
name|WriterFactory
argument_list|(
name|conf
argument_list|,
name|cc
argument_list|)
operator|.
name|withPath
argument_list|(
name|fs
argument_list|,
name|path
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|cxt
argument_list|)
operator|.
name|create
argument_list|()
decl_stmt|;
comment|// write a bunch of random kv's
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
literal|9713312
argument_list|)
decl_stmt|;
comment|// some seed.
specifier|final
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
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
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|keyBytes
init|=
name|RandomKeyValueUtil
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
name|valueBytes
init|=
name|RandomKeyValueUtil
operator|.
name|randomValue
argument_list|(
name|rand
argument_list|)
decl_stmt|;
comment|// make a real keyvalue so that hfile tool can examine it
name|writer
operator|.
name|append
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|keyBytes
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|valueBytes
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Read all blocks from {@code path} to populate {@code blockCache}.    */
specifier|private
specifier|static
name|void
name|cacheBlocks
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|CacheConfig
name|cacheConfig
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|HFileContext
name|cxt
parameter_list|)
throws|throws
name|IOException
block|{
name|FSDataInputStreamWrapper
name|fsdis
init|=
operator|new
name|FSDataInputStreamWrapper
argument_list|(
name|fs
argument_list|,
name|path
argument_list|)
decl_stmt|;
name|long
name|fileSize
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|path
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
operator|.
name|getStream
argument_list|(
literal|false
argument_list|)
argument_list|,
name|fileSize
argument_list|)
decl_stmt|;
name|HFile
operator|.
name|Reader
name|reader
init|=
operator|new
name|HFileReaderImpl
argument_list|(
name|path
argument_list|,
name|trailer
argument_list|,
name|fsdis
argument_list|,
name|fileSize
argument_list|,
name|cacheConfig
argument_list|,
name|fsdis
operator|.
name|getHfs
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|reader
operator|.
name|loadFileInfo
argument_list|()
expr_stmt|;
name|long
name|offset
init|=
name|trailer
operator|.
name|getFirstDataBlockOffset
argument_list|()
decl_stmt|,
name|max
init|=
name|trailer
operator|.
name|getLastDataBlockOffset
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HFileBlock
argument_list|>
name|blocks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|4
argument_list|)
decl_stmt|;
name|HFileBlock
name|block
decl_stmt|;
while|while
condition|(
name|offset
operator|<=
name|max
condition|)
block|{
name|block
operator|=
name|reader
operator|.
name|readBlock
argument_list|(
name|offset
argument_list|,
operator|-
literal|1
argument_list|,
comment|/* cacheBlock */
literal|true
argument_list|,
comment|/* pread */
literal|false
argument_list|,
comment|/* isCompaction */
literal|false
argument_list|,
comment|/* updateCacheMetrics */
literal|true
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|offset
operator|+=
name|block
operator|.
name|getOnDiskSizeWithHeader
argument_list|()
expr_stmt|;
name|blocks
operator|.
name|add
argument_list|(
name|block
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"read "
operator|+
name|Iterables
operator|.
name|toString
argument_list|(
name|blocks
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompressionIncreasesEffectiveBlockCacheSize
parameter_list|()
throws|throws
name|Exception
block|{
comment|// enough room for 2 uncompressed block
name|int
name|maxSize
init|=
call|(
name|int
call|)
argument_list|(
name|HConstants
operator|.
name|DEFAULT_BLOCKSIZE
operator|*
literal|2.1
argument_list|)
decl_stmt|;
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
literal|"testCompressionIncreasesEffectiveBlockcacheSize"
argument_list|)
decl_stmt|;
name|HFileContext
name|context
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withCompression
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|GZ
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"context="
operator|+
name|context
argument_list|)
expr_stmt|;
comment|// setup cache with lazy-decompression disabled.
name|Configuration
name|lazyCompressDisabled
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|lazyCompressDisabled
operator|.
name|setBoolean
argument_list|(
name|CacheConfig
operator|.
name|CACHE_BLOCKS_ON_WRITE_KEY
argument_list|,
name|cacheOnWrite
argument_list|)
expr_stmt|;
name|lazyCompressDisabled
operator|.
name|setBoolean
argument_list|(
name|CacheConfig
operator|.
name|CACHE_BLOOM_BLOCKS_ON_WRITE_KEY
argument_list|,
name|cacheOnWrite
argument_list|)
expr_stmt|;
name|lazyCompressDisabled
operator|.
name|setBoolean
argument_list|(
name|CacheConfig
operator|.
name|CACHE_INDEX_BLOCKS_ON_WRITE_KEY
argument_list|,
name|cacheOnWrite
argument_list|)
expr_stmt|;
name|lazyCompressDisabled
operator|.
name|setBoolean
argument_list|(
name|CacheConfig
operator|.
name|CACHE_DATA_BLOCKS_COMPRESSED_KEY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|CacheConfig
name|cc
init|=
operator|new
name|CacheConfig
argument_list|(
name|lazyCompressDisabled
argument_list|,
operator|new
name|LruBlockCache
argument_list|(
name|maxSize
argument_list|,
name|HConstants
operator|.
name|DEFAULT_BLOCKSIZE
argument_list|,
literal|false
argument_list|,
name|lazyCompressDisabled
argument_list|)
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|cc
operator|.
name|shouldCacheDataCompressed
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|cc
operator|.
name|isCombinedBlockCache
argument_list|()
argument_list|)
expr_stmt|;
name|LruBlockCache
name|disabledBlockCache
init|=
operator|(
name|LruBlockCache
operator|)
name|cc
operator|.
name|getBlockCache
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"disabledBlockCache="
operator|+
name|disabledBlockCache
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"test inconsistency detected."
argument_list|,
name|maxSize
argument_list|,
name|disabledBlockCache
operator|.
name|getMaxSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"eviction thread spawned unintentionally."
argument_list|,
name|disabledBlockCache
operator|.
name|getEvictionThread
argument_list|()
operator|==
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"freshly created blockcache contains blocks."
argument_list|,
literal|0
argument_list|,
name|disabledBlockCache
operator|.
name|getBlockCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// 2000 kv's is ~3.6 full unencoded data blocks.
comment|// Requires a conf and CacheConfig but should not be specific to this instance's cache settings
name|writeHFile
argument_list|(
name|lazyCompressDisabled
argument_list|,
name|cc
argument_list|,
name|fs
argument_list|,
name|hfilePath
argument_list|,
name|context
argument_list|,
literal|2000
argument_list|)
expr_stmt|;
comment|// populate the cache
name|cacheBlocks
argument_list|(
name|lazyCompressDisabled
argument_list|,
name|cc
argument_list|,
name|fs
argument_list|,
name|hfilePath
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|long
name|disabledBlockCount
init|=
name|disabledBlockCache
operator|.
name|getBlockCount
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"blockcache should contain blocks. disabledBlockCount="
operator|+
name|disabledBlockCount
argument_list|,
name|disabledBlockCount
operator|>
literal|0
argument_list|)
expr_stmt|;
name|long
name|disabledEvictedCount
init|=
name|disabledBlockCache
operator|.
name|getStats
argument_list|()
operator|.
name|getEvictedCount
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|BlockCacheKey
argument_list|,
name|LruCachedBlock
argument_list|>
name|e
range|:
name|disabledBlockCache
operator|.
name|getMapForTests
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|HFileBlock
name|block
init|=
operator|(
name|HFileBlock
operator|)
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|getBuffer
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"found a packed block, block="
operator|+
name|block
argument_list|,
name|block
operator|.
name|isUnpacked
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// count blocks with lazy decompression
name|Configuration
name|lazyCompressEnabled
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|lazyCompressEnabled
operator|.
name|setBoolean
argument_list|(
name|CacheConfig
operator|.
name|CACHE_BLOCKS_ON_WRITE_KEY
argument_list|,
name|cacheOnWrite
argument_list|)
expr_stmt|;
name|lazyCompressEnabled
operator|.
name|setBoolean
argument_list|(
name|CacheConfig
operator|.
name|CACHE_BLOOM_BLOCKS_ON_WRITE_KEY
argument_list|,
name|cacheOnWrite
argument_list|)
expr_stmt|;
name|lazyCompressEnabled
operator|.
name|setBoolean
argument_list|(
name|CacheConfig
operator|.
name|CACHE_INDEX_BLOCKS_ON_WRITE_KEY
argument_list|,
name|cacheOnWrite
argument_list|)
expr_stmt|;
name|lazyCompressEnabled
operator|.
name|setBoolean
argument_list|(
name|CacheConfig
operator|.
name|CACHE_DATA_BLOCKS_COMPRESSED_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|cc
operator|=
operator|new
name|CacheConfig
argument_list|(
name|lazyCompressEnabled
argument_list|,
operator|new
name|LruBlockCache
argument_list|(
name|maxSize
argument_list|,
name|HConstants
operator|.
name|DEFAULT_BLOCKSIZE
argument_list|,
literal|false
argument_list|,
name|lazyCompressEnabled
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"test improperly configured."
argument_list|,
name|cc
operator|.
name|shouldCacheDataCompressed
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cc
operator|.
name|getBlockCache
argument_list|()
operator|.
name|get
argument_list|()
operator|instanceof
name|LruBlockCache
argument_list|)
expr_stmt|;
name|LruBlockCache
name|enabledBlockCache
init|=
operator|(
name|LruBlockCache
operator|)
name|cc
operator|.
name|getBlockCache
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"enabledBlockCache="
operator|+
name|enabledBlockCache
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"test inconsistency detected"
argument_list|,
name|maxSize
argument_list|,
name|enabledBlockCache
operator|.
name|getMaxSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"eviction thread spawned unintentionally."
argument_list|,
name|enabledBlockCache
operator|.
name|getEvictionThread
argument_list|()
operator|==
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"freshly created blockcache contains blocks."
argument_list|,
literal|0
argument_list|,
name|enabledBlockCache
operator|.
name|getBlockCount
argument_list|()
argument_list|)
expr_stmt|;
name|cacheBlocks
argument_list|(
name|lazyCompressEnabled
argument_list|,
name|cc
argument_list|,
name|fs
argument_list|,
name|hfilePath
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|long
name|enabledBlockCount
init|=
name|enabledBlockCache
operator|.
name|getBlockCount
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"blockcache should contain blocks. enabledBlockCount="
operator|+
name|enabledBlockCount
argument_list|,
name|enabledBlockCount
operator|>
literal|0
argument_list|)
expr_stmt|;
name|long
name|enabledEvictedCount
init|=
name|enabledBlockCache
operator|.
name|getStats
argument_list|()
operator|.
name|getEvictedCount
argument_list|()
decl_stmt|;
name|int
name|candidatesFound
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|BlockCacheKey
argument_list|,
name|LruCachedBlock
argument_list|>
name|e
range|:
name|enabledBlockCache
operator|.
name|getMapForTests
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|candidatesFound
operator|++
expr_stmt|;
name|HFileBlock
name|block
init|=
operator|(
name|HFileBlock
operator|)
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|getBuffer
argument_list|()
decl_stmt|;
if|if
condition|(
name|cc
operator|.
name|shouldCacheCompressed
argument_list|(
name|block
operator|.
name|getBlockType
argument_list|()
operator|.
name|getCategory
argument_list|()
argument_list|)
condition|)
block|{
name|assertFalse
argument_list|(
literal|"found an unpacked block, block="
operator|+
name|block
operator|+
literal|", block buffer capacity="
operator|+
name|block
operator|.
name|getBufferWithoutHeader
argument_list|()
operator|.
name|capacity
argument_list|()
argument_list|,
name|block
operator|.
name|isUnpacked
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
literal|"did not find any candidates for compressed caching. Invalid test."
argument_list|,
name|candidatesFound
operator|>
literal|0
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"disabledBlockCount="
operator|+
name|disabledBlockCount
operator|+
literal|", enabledBlockCount="
operator|+
name|enabledBlockCount
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"enabling compressed data blocks should increase the effective cache size. "
operator|+
literal|"disabledBlockCount="
operator|+
name|disabledBlockCount
operator|+
literal|", enabledBlockCount="
operator|+
name|enabledBlockCount
argument_list|,
name|disabledBlockCount
operator|<
name|enabledBlockCount
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"disabledEvictedCount="
operator|+
name|disabledEvictedCount
operator|+
literal|", enabledEvictedCount="
operator|+
name|enabledEvictedCount
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"enabling compressed data blocks should reduce the number of evictions. "
operator|+
literal|"disabledEvictedCount="
operator|+
name|disabledEvictedCount
operator|+
literal|", enabledEvictedCount="
operator|+
name|enabledEvictedCount
argument_list|,
name|enabledEvictedCount
operator|<
name|disabledEvictedCount
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

