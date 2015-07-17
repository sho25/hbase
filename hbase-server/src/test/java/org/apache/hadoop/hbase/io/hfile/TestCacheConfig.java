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
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|MemoryUsage
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
name|Map
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
name|LargeTests
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
name|bucket
operator|.
name|BucketCache
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
name|util
operator|.
name|Threads
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

begin_comment
comment|/**  * Tests that {@link CacheConfig} does as expected.  */
end_comment

begin_comment
comment|// This test is marked as a large test though it runs in a short amount of time
end_comment

begin_comment
comment|// (seconds).  It is large because it depends on being able to reset the global
end_comment

begin_comment
comment|// blockcache instance which is in a global variable.  Experience has it that
end_comment

begin_comment
comment|// tests clash on the global variable if this test is run as small sized test.
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
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestCacheConfig
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
name|TestCacheConfig
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|static
class|class
name|Deserializer
implements|implements
name|CacheableDeserializer
argument_list|<
name|Cacheable
argument_list|>
block|{
specifier|private
specifier|final
name|Cacheable
name|cacheable
decl_stmt|;
specifier|private
name|int
name|deserializedIdentifier
init|=
literal|0
decl_stmt|;
name|Deserializer
parameter_list|(
specifier|final
name|Cacheable
name|c
parameter_list|)
block|{
name|deserializedIdentifier
operator|=
name|CacheableDeserializerIdManager
operator|.
name|registerDeserializer
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|cacheable
operator|=
name|c
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getDeserialiserIdentifier
parameter_list|()
block|{
return|return
name|deserializedIdentifier
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cacheable
name|deserialize
parameter_list|(
name|ByteBuff
name|b
parameter_list|,
name|boolean
name|reuse
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Deserialized "
operator|+
name|b
operator|+
literal|", reuse="
operator|+
name|reuse
argument_list|)
expr_stmt|;
return|return
name|cacheable
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cacheable
name|deserialize
parameter_list|(
name|ByteBuff
name|b
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Deserialized "
operator|+
name|b
argument_list|)
expr_stmt|;
return|return
name|cacheable
return|;
block|}
block|}
empty_stmt|;
specifier|static
class|class
name|IndexCacheEntry
extends|extends
name|DataCacheEntry
block|{
specifier|private
specifier|static
name|IndexCacheEntry
name|SINGLETON
init|=
operator|new
name|IndexCacheEntry
argument_list|()
decl_stmt|;
specifier|public
name|IndexCacheEntry
parameter_list|()
block|{
name|super
argument_list|(
name|SINGLETON
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|BlockType
name|getBlockType
parameter_list|()
block|{
return|return
name|BlockType
operator|.
name|ROOT_INDEX
return|;
block|}
block|}
specifier|static
class|class
name|DataCacheEntry
implements|implements
name|Cacheable
block|{
specifier|private
specifier|static
specifier|final
name|int
name|SIZE
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
name|DataCacheEntry
name|SINGLETON
init|=
operator|new
name|DataCacheEntry
argument_list|()
decl_stmt|;
specifier|final
name|CacheableDeserializer
argument_list|<
name|Cacheable
argument_list|>
name|deserializer
decl_stmt|;
name|DataCacheEntry
parameter_list|()
block|{
name|this
argument_list|(
name|SINGLETON
argument_list|)
expr_stmt|;
block|}
name|DataCacheEntry
parameter_list|(
specifier|final
name|Cacheable
name|c
parameter_list|)
block|{
name|this
operator|.
name|deserializer
operator|=
operator|new
name|Deserializer
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"size="
operator|+
name|SIZE
operator|+
literal|", type="
operator|+
name|getBlockType
argument_list|()
return|;
block|}
empty_stmt|;
annotation|@
name|Override
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
return|return
name|SIZE
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getSerializedLength
parameter_list|()
block|{
return|return
name|SIZE
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serialize
parameter_list|(
name|ByteBuffer
name|destination
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Serialized "
operator|+
name|this
operator|+
literal|" to "
operator|+
name|destination
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|CacheableDeserializer
argument_list|<
name|Cacheable
argument_list|>
name|getDeserializer
parameter_list|()
block|{
return|return
name|this
operator|.
name|deserializer
return|;
block|}
annotation|@
name|Override
specifier|public
name|BlockType
name|getBlockType
parameter_list|()
block|{
return|return
name|BlockType
operator|.
name|DATA
return|;
block|}
block|}
empty_stmt|;
specifier|static
class|class
name|MetaCacheEntry
extends|extends
name|DataCacheEntry
block|{
annotation|@
name|Override
specifier|public
name|BlockType
name|getBlockType
parameter_list|()
block|{
return|return
name|BlockType
operator|.
name|INTERMEDIATE_INDEX
return|;
block|}
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
name|CacheConfig
operator|.
name|GLOBAL_BLOCK_CACHE_INSTANCE
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Let go of current block cache.
name|CacheConfig
operator|.
name|GLOBAL_BLOCK_CACHE_INSTANCE
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * @param cc    * @param doubling If true, addition of element ups counter by 2, not 1, because element added    * to onheap and offheap caches.    * @param sizing True if we should run sizing test (doesn't always apply).    */
name|void
name|basicBlockCacheOps
parameter_list|(
specifier|final
name|CacheConfig
name|cc
parameter_list|,
specifier|final
name|boolean
name|doubling
parameter_list|,
specifier|final
name|boolean
name|sizing
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|cc
operator|.
name|isBlockCacheEnabled
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CacheConfig
operator|.
name|DEFAULT_IN_MEMORY
operator|==
name|cc
operator|.
name|isInMemory
argument_list|()
argument_list|)
expr_stmt|;
name|BlockCache
name|bc
init|=
name|cc
operator|.
name|getBlockCache
argument_list|()
decl_stmt|;
name|BlockCacheKey
name|bck
init|=
operator|new
name|BlockCacheKey
argument_list|(
literal|"f"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|Cacheable
name|c
init|=
operator|new
name|DataCacheEntry
argument_list|()
decl_stmt|;
comment|// Do asserts on block counting.
name|long
name|initialBlockCount
init|=
name|bc
operator|.
name|getBlockCount
argument_list|()
decl_stmt|;
name|bc
operator|.
name|cacheBlock
argument_list|(
name|bck
argument_list|,
name|c
argument_list|,
name|cc
operator|.
name|isInMemory
argument_list|()
argument_list|,
name|cc
operator|.
name|isCacheDataInL1
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|doubling
condition|?
literal|2
else|:
literal|1
argument_list|,
name|bc
operator|.
name|getBlockCount
argument_list|()
operator|-
name|initialBlockCount
argument_list|)
expr_stmt|;
name|bc
operator|.
name|evictBlock
argument_list|(
name|bck
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|initialBlockCount
argument_list|,
name|bc
operator|.
name|getBlockCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// Do size accounting.  Do it after the above 'warm-up' because it looks like some
comment|// buffers do lazy allocation so sizes are off on first go around.
if|if
condition|(
name|sizing
condition|)
block|{
name|long
name|originalSize
init|=
name|bc
operator|.
name|getCurrentSize
argument_list|()
decl_stmt|;
name|bc
operator|.
name|cacheBlock
argument_list|(
name|bck
argument_list|,
name|c
argument_list|,
name|cc
operator|.
name|isInMemory
argument_list|()
argument_list|,
name|cc
operator|.
name|isCacheDataInL1
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bc
operator|.
name|getCurrentSize
argument_list|()
operator|>
name|originalSize
argument_list|)
expr_stmt|;
name|bc
operator|.
name|evictBlock
argument_list|(
name|bck
argument_list|)
expr_stmt|;
name|long
name|size
init|=
name|bc
operator|.
name|getCurrentSize
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|originalSize
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @param cc    * @param filename    * @return    */
specifier|private
name|long
name|cacheDataBlock
parameter_list|(
specifier|final
name|CacheConfig
name|cc
parameter_list|,
specifier|final
name|String
name|filename
parameter_list|)
block|{
name|BlockCacheKey
name|bck
init|=
operator|new
name|BlockCacheKey
argument_list|(
name|filename
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|Cacheable
name|c
init|=
operator|new
name|DataCacheEntry
argument_list|()
decl_stmt|;
comment|// Do asserts on block counting.
name|cc
operator|.
name|getBlockCache
argument_list|()
operator|.
name|cacheBlock
argument_list|(
name|bck
argument_list|,
name|c
argument_list|,
name|cc
operator|.
name|isInMemory
argument_list|()
argument_list|,
name|cc
operator|.
name|isCacheDataInL1
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|cc
operator|.
name|getBlockCache
argument_list|()
operator|.
name|getBlockCount
argument_list|()
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCacheConfigDefaultLRUBlockCache
parameter_list|()
block|{
name|CacheConfig
name|cc
init|=
operator|new
name|CacheConfig
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|cc
operator|.
name|isBlockCacheEnabled
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CacheConfig
operator|.
name|DEFAULT_IN_MEMORY
operator|==
name|cc
operator|.
name|isInMemory
argument_list|()
argument_list|)
expr_stmt|;
name|basicBlockCacheOps
argument_list|(
name|cc
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cc
operator|.
name|getBlockCache
argument_list|()
operator|instanceof
name|LruBlockCache
argument_list|)
expr_stmt|;
block|}
comment|/**    * Assert that the caches are deployed with CombinedBlockCache and of the appropriate sizes.    */
annotation|@
name|Test
specifier|public
name|void
name|testOffHeapBucketCacheConfig
parameter_list|()
block|{
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|BUCKET_CACHE_IOENGINE_KEY
argument_list|,
literal|"offheap"
argument_list|)
expr_stmt|;
name|doBucketCacheConfigTest
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOnHeapBucketCacheConfig
parameter_list|()
block|{
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|BUCKET_CACHE_IOENGINE_KEY
argument_list|,
literal|"heap"
argument_list|)
expr_stmt|;
name|doBucketCacheConfigTest
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFileBucketCacheConfig
parameter_list|()
throws|throws
name|IOException
block|{
name|HBaseTestingUtility
name|htu
init|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|htu
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
literal|"bc.txt"
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|fs
operator|.
name|create
argument_list|(
name|p
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|BUCKET_CACHE_IOENGINE_KEY
argument_list|,
literal|"file:"
operator|+
name|p
argument_list|)
expr_stmt|;
name|doBucketCacheConfigTest
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|htu
operator|.
name|cleanupTestDir
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|doBucketCacheConfigTest
parameter_list|()
block|{
specifier|final
name|int
name|bcSize
init|=
literal|100
decl_stmt|;
name|this
operator|.
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|BUCKET_CACHE_SIZE_KEY
argument_list|,
name|bcSize
argument_list|)
expr_stmt|;
name|CacheConfig
name|cc
init|=
operator|new
name|CacheConfig
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|basicBlockCacheOps
argument_list|(
name|cc
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cc
operator|.
name|getBlockCache
argument_list|()
operator|instanceof
name|CombinedBlockCache
argument_list|)
expr_stmt|;
comment|// TODO: Assert sizes allocated are right and proportions.
name|CombinedBlockCache
name|cbc
init|=
operator|(
name|CombinedBlockCache
operator|)
name|cc
operator|.
name|getBlockCache
argument_list|()
decl_stmt|;
name|BlockCache
index|[]
name|bcs
init|=
name|cbc
operator|.
name|getBlockCaches
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|bcs
index|[
literal|0
index|]
operator|instanceof
name|LruBlockCache
argument_list|)
expr_stmt|;
name|LruBlockCache
name|lbc
init|=
operator|(
name|LruBlockCache
operator|)
name|bcs
index|[
literal|0
index|]
decl_stmt|;
name|assertEquals
argument_list|(
name|CacheConfig
operator|.
name|getLruCacheSize
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|ManagementFactory
operator|.
name|getMemoryMXBean
argument_list|()
operator|.
name|getHeapMemoryUsage
argument_list|()
argument_list|)
argument_list|,
name|lbc
operator|.
name|getMaxSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bcs
index|[
literal|1
index|]
operator|instanceof
name|BucketCache
argument_list|)
expr_stmt|;
name|BucketCache
name|bc
init|=
operator|(
name|BucketCache
operator|)
name|bcs
index|[
literal|1
index|]
decl_stmt|;
comment|// getMaxSize comes back in bytes but we specified size in MB
name|assertEquals
argument_list|(
name|bcSize
argument_list|,
name|bc
operator|.
name|getMaxSize
argument_list|()
operator|/
operator|(
literal|1024
operator|*
literal|1024
operator|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Assert that when BUCKET_CACHE_COMBINED_KEY is false, the non-default, that we deploy    * LruBlockCache as L1 with a BucketCache for L2.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|10000
argument_list|)
specifier|public
name|void
name|testBucketCacheConfigL1L2Setup
parameter_list|()
block|{
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|BUCKET_CACHE_IOENGINE_KEY
argument_list|,
literal|"offheap"
argument_list|)
expr_stmt|;
comment|// Make lru size is smaller than bcSize for sure.  Need this to be true so when eviction
comment|// from L1 happens, it does not fail because L2 can't take the eviction because block too big.
name|this
operator|.
name|conf
operator|.
name|setFloat
argument_list|(
name|HConstants
operator|.
name|HFILE_BLOCK_CACHE_SIZE_KEY
argument_list|,
literal|0.001f
argument_list|)
expr_stmt|;
name|MemoryUsage
name|mu
init|=
name|ManagementFactory
operator|.
name|getMemoryMXBean
argument_list|()
operator|.
name|getHeapMemoryUsage
argument_list|()
decl_stmt|;
name|long
name|lruExpectedSize
init|=
name|CacheConfig
operator|.
name|getLruCacheSize
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|mu
argument_list|)
decl_stmt|;
specifier|final
name|int
name|bcSize
init|=
literal|100
decl_stmt|;
name|long
name|bcExpectedSize
init|=
literal|100
operator|*
literal|1024
operator|*
literal|1024
decl_stmt|;
comment|// MB.
name|assertTrue
argument_list|(
name|lruExpectedSize
operator|<
name|bcExpectedSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|BUCKET_CACHE_SIZE_KEY
argument_list|,
name|bcSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|setBoolean
argument_list|(
name|CacheConfig
operator|.
name|BUCKET_CACHE_COMBINED_KEY
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
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|basicBlockCacheOps
argument_list|(
name|cc
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cc
operator|.
name|getBlockCache
argument_list|()
operator|instanceof
name|LruBlockCache
argument_list|)
expr_stmt|;
comment|// TODO: Assert sizes allocated are right and proportions.
name|LruBlockCache
name|lbc
init|=
operator|(
name|LruBlockCache
operator|)
name|cc
operator|.
name|getBlockCache
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|lruExpectedSize
argument_list|,
name|lbc
operator|.
name|getMaxSize
argument_list|()
argument_list|)
expr_stmt|;
name|BlockCache
name|bc
init|=
name|lbc
operator|.
name|getVictimHandler
argument_list|()
decl_stmt|;
comment|// getMaxSize comes back in bytes but we specified size in MB
name|assertEquals
argument_list|(
name|bcExpectedSize
argument_list|,
operator|(
operator|(
name|BucketCache
operator|)
name|bc
operator|)
operator|.
name|getMaxSize
argument_list|()
argument_list|)
expr_stmt|;
comment|// Test the L1+L2 deploy works as we'd expect with blocks evicted from L1 going to L2.
name|long
name|initialL1BlockCount
init|=
name|lbc
operator|.
name|getBlockCount
argument_list|()
decl_stmt|;
name|long
name|initialL2BlockCount
init|=
name|bc
operator|.
name|getBlockCount
argument_list|()
decl_stmt|;
name|Cacheable
name|c
init|=
operator|new
name|DataCacheEntry
argument_list|()
decl_stmt|;
name|BlockCacheKey
name|bck
init|=
operator|new
name|BlockCacheKey
argument_list|(
literal|"bck"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|lbc
operator|.
name|cacheBlock
argument_list|(
name|bck
argument_list|,
name|c
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|initialL1BlockCount
operator|+
literal|1
argument_list|,
name|lbc
operator|.
name|getBlockCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|initialL2BlockCount
argument_list|,
name|bc
operator|.
name|getBlockCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// Force evictions by putting in a block too big.
specifier|final
name|long
name|justTooBigSize
init|=
name|lbc
operator|.
name|acceptableSize
argument_list|()
operator|+
literal|1
decl_stmt|;
name|lbc
operator|.
name|cacheBlock
argument_list|(
operator|new
name|BlockCacheKey
argument_list|(
literal|"bck2"
argument_list|,
literal|0
argument_list|)
argument_list|,
operator|new
name|DataCacheEntry
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
return|return
name|justTooBigSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getSerializedLength
parameter_list|()
block|{
return|return
operator|(
name|int
operator|)
name|heapSize
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// The eviction thread in lrublockcache needs to run.
while|while
condition|(
name|initialL1BlockCount
operator|!=
name|lbc
operator|.
name|getBlockCount
argument_list|()
condition|)
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|initialL1BlockCount
argument_list|,
name|lbc
operator|.
name|getBlockCount
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|count
init|=
name|bc
operator|.
name|getBlockCount
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|initialL2BlockCount
operator|+
literal|1
operator|<=
name|count
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test the cacheDataInL1 flag.  When set, data blocks should be cached in the l1 tier, up in    * LruBlockCache when using CombinedBlockCcahe.    */
annotation|@
name|Test
specifier|public
name|void
name|testCacheDataInL1
parameter_list|()
block|{
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|BUCKET_CACHE_IOENGINE_KEY
argument_list|,
literal|"offheap"
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|BUCKET_CACHE_SIZE_KEY
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|CacheConfig
name|cc
init|=
operator|new
name|CacheConfig
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|cc
operator|.
name|getBlockCache
argument_list|()
operator|instanceof
name|CombinedBlockCache
argument_list|)
expr_stmt|;
name|CombinedBlockCache
name|cbc
init|=
operator|(
name|CombinedBlockCache
operator|)
name|cc
operator|.
name|getBlockCache
argument_list|()
decl_stmt|;
comment|// Add a data block.  Should go into L2, into the Bucket Cache, not the LruBlockCache.
name|cacheDataBlock
argument_list|(
name|cc
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|LruBlockCache
name|lrubc
init|=
operator|(
name|LruBlockCache
operator|)
name|cbc
operator|.
name|getBlockCaches
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|assertDataBlockCount
argument_list|(
name|lrubc
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// Enable our test flag.
name|cc
operator|.
name|setCacheDataInL1
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|cacheDataBlock
argument_list|(
name|cc
argument_list|,
literal|"2"
argument_list|)
expr_stmt|;
name|assertDataBlockCount
argument_list|(
name|lrubc
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|cc
operator|.
name|setCacheDataInL1
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|cacheDataBlock
argument_list|(
name|cc
argument_list|,
literal|"3"
argument_list|)
expr_stmt|;
name|assertDataBlockCount
argument_list|(
name|lrubc
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertDataBlockCount
parameter_list|(
specifier|final
name|LruBlockCache
name|bc
parameter_list|,
specifier|final
name|int
name|expected
parameter_list|)
block|{
name|Map
argument_list|<
name|BlockType
argument_list|,
name|Integer
argument_list|>
name|blocks
init|=
name|bc
operator|.
name|getBlockTypeCountsForTest
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|blocks
operator|==
literal|null
condition|?
literal|0
else|:
name|blocks
operator|.
name|get
argument_list|(
name|BlockType
operator|.
name|DATA
argument_list|)
operator|==
literal|null
condition|?
literal|0
else|:
name|blocks
operator|.
name|get
argument_list|(
name|BlockType
operator|.
name|DATA
argument_list|)
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

