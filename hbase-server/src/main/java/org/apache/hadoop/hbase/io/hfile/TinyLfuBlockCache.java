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
name|java
operator|.
name|util
operator|.
name|Objects
operator|.
name|requireNonNull
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|Executor
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
name|Executors
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
name|ScheduledExecutorService
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
name|TimeUnit
import|;
end_import

begin_import
import|import
name|com
operator|.
name|github
operator|.
name|benmanes
operator|.
name|caffeine
operator|.
name|cache
operator|.
name|Cache
import|;
end_import

begin_import
import|import
name|com
operator|.
name|github
operator|.
name|benmanes
operator|.
name|caffeine
operator|.
name|cache
operator|.
name|Caffeine
import|;
end_import

begin_import
import|import
name|com
operator|.
name|github
operator|.
name|benmanes
operator|.
name|caffeine
operator|.
name|cache
operator|.
name|Policy
operator|.
name|Eviction
import|;
end_import

begin_import
import|import
name|com
operator|.
name|github
operator|.
name|benmanes
operator|.
name|caffeine
operator|.
name|cache
operator|.
name|RemovalCause
import|;
end_import

begin_import
import|import
name|com
operator|.
name|github
operator|.
name|benmanes
operator|.
name|caffeine
operator|.
name|cache
operator|.
name|RemovalListener
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
name|util
operator|.
name|StringUtils
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
name|annotations
operator|.
name|VisibleForTesting
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
name|base
operator|.
name|MoreObjects
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
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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

begin_comment
comment|/**  * A block cache that is memory-aware using {@link HeapSize}, memory bounded using the W-TinyLFU  * eviction algorithm, and concurrent. This implementation delegates to a Caffeine cache to provide  * O(1) read and write operations.  *<ul>  *<li>W-TinyLFU: http://arxiv.org/pdf/1512.00727.pdf</li>  *<li>Caffeine: https://github.com/ben-manes/caffeine</li>  *<li>Cache design: http://highscalability.com/blog/2016/1/25/design-of-a-modern-cache.html</li>  *</ul>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|TinyLfuBlockCache
implements|implements
name|FirstLevelBlockCache
block|{
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
name|TinyLfuBlockCache
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|MAX_BLOCK_SIZE
init|=
literal|"hbase.tinylfu.max.block.size"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|DEFAULT_MAX_BLOCK_SIZE
init|=
literal|16L
operator|*
literal|1024L
operator|*
literal|1024L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|STAT_THREAD_PERIOD_SECONDS
init|=
literal|5
operator|*
literal|60
decl_stmt|;
specifier|private
specifier|final
name|Eviction
argument_list|<
name|BlockCacheKey
argument_list|,
name|Cacheable
argument_list|>
name|policy
decl_stmt|;
specifier|private
specifier|final
name|ScheduledExecutorService
name|statsThreadPool
decl_stmt|;
specifier|private
specifier|final
name|long
name|maxBlockSize
decl_stmt|;
specifier|private
specifier|final
name|CacheStats
name|stats
decl_stmt|;
specifier|private
name|BlockCache
name|victimCache
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|final
name|Cache
argument_list|<
name|BlockCacheKey
argument_list|,
name|Cacheable
argument_list|>
name|cache
decl_stmt|;
comment|/**    * Creates a block cache.    *    * @param maximumSizeInBytes maximum size of this cache, in bytes    * @param avgBlockSize expected average size of blocks, in bytes    * @param executor the cache's executor    * @param conf additional configuration    */
specifier|public
name|TinyLfuBlockCache
parameter_list|(
name|long
name|maximumSizeInBytes
parameter_list|,
name|long
name|avgBlockSize
parameter_list|,
name|Executor
name|executor
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|this
argument_list|(
name|maximumSizeInBytes
argument_list|,
name|avgBlockSize
argument_list|,
name|conf
operator|.
name|getLong
argument_list|(
name|MAX_BLOCK_SIZE
argument_list|,
name|DEFAULT_MAX_BLOCK_SIZE
argument_list|)
argument_list|,
name|executor
argument_list|)
expr_stmt|;
block|}
comment|/**    * Creates a block cache.    *    * @param maximumSizeInBytes maximum size of this cache, in bytes    * @param avgBlockSize expected average size of blocks, in bytes    * @param maxBlockSize maximum size of a block, in bytes    * @param executor the cache's executor    */
specifier|public
name|TinyLfuBlockCache
parameter_list|(
name|long
name|maximumSizeInBytes
parameter_list|,
name|long
name|avgBlockSize
parameter_list|,
name|long
name|maxBlockSize
parameter_list|,
name|Executor
name|executor
parameter_list|)
block|{
name|this
operator|.
name|cache
operator|=
name|Caffeine
operator|.
name|newBuilder
argument_list|()
operator|.
name|executor
argument_list|(
name|executor
argument_list|)
operator|.
name|maximumWeight
argument_list|(
name|maximumSizeInBytes
argument_list|)
operator|.
name|removalListener
argument_list|(
operator|new
name|EvictionListener
argument_list|()
argument_list|)
operator|.
name|weigher
argument_list|(
parameter_list|(
name|BlockCacheKey
name|key
parameter_list|,
name|Cacheable
name|value
parameter_list|)
lambda|->
operator|(
name|int
operator|)
name|Math
operator|.
name|min
argument_list|(
name|value
operator|.
name|heapSize
argument_list|()
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
operator|.
name|initialCapacity
argument_list|(
operator|(
name|int
operator|)
name|Math
operator|.
name|ceil
argument_list|(
operator|(
literal|1.2
operator|*
name|maximumSizeInBytes
operator|)
operator|/
name|avgBlockSize
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|this
operator|.
name|maxBlockSize
operator|=
name|maxBlockSize
expr_stmt|;
name|this
operator|.
name|policy
operator|=
name|cache
operator|.
name|policy
argument_list|()
operator|.
name|eviction
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
name|this
operator|.
name|stats
operator|=
operator|new
name|CacheStats
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|statsThreadPool
operator|=
name|Executors
operator|.
name|newSingleThreadScheduledExecutor
argument_list|(
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setNameFormat
argument_list|(
literal|"TinyLfuBlockCacheStatsExecutor"
argument_list|)
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|statsThreadPool
operator|.
name|scheduleAtFixedRate
argument_list|(
name|this
operator|::
name|logStats
argument_list|,
name|STAT_THREAD_PERIOD_SECONDS
argument_list|,
name|STAT_THREAD_PERIOD_SECONDS
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setVictimCache
parameter_list|(
name|BlockCache
name|victimCache
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|victimCache
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"The victim cache has already been set"
argument_list|)
throw|;
block|}
name|this
operator|.
name|victimCache
operator|=
name|requireNonNull
argument_list|(
name|victimCache
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|size
parameter_list|()
block|{
return|return
name|policy
operator|.
name|getMaximum
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getFreeSize
parameter_list|()
block|{
return|return
name|size
argument_list|()
operator|-
name|getCurrentSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCurrentSize
parameter_list|()
block|{
return|return
name|policy
operator|.
name|weightedSize
argument_list|()
operator|.
name|getAsLong
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBlockCount
parameter_list|()
block|{
return|return
name|cache
operator|.
name|estimatedSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
return|return
name|getCurrentSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setMaxSize
parameter_list|(
name|long
name|size
parameter_list|)
block|{
name|policy
operator|.
name|setMaximum
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|containsBlock
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|)
block|{
return|return
name|cache
operator|.
name|asMap
argument_list|()
operator|.
name|containsKey
argument_list|(
name|cacheKey
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cacheable
name|getBlock
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|,
name|boolean
name|caching
parameter_list|,
name|boolean
name|repeat
parameter_list|,
name|boolean
name|updateCacheMetrics
parameter_list|)
block|{
name|Cacheable
name|value
init|=
name|cache
operator|.
name|getIfPresent
argument_list|(
name|cacheKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|repeat
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|updateCacheMetrics
condition|)
block|{
name|stats
operator|.
name|miss
argument_list|(
name|caching
argument_list|,
name|cacheKey
operator|.
name|isPrimary
argument_list|()
argument_list|,
name|cacheKey
operator|.
name|getBlockType
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|victimCache
operator|!=
literal|null
condition|)
block|{
name|value
operator|=
name|victimCache
operator|.
name|getBlock
argument_list|(
name|cacheKey
argument_list|,
name|caching
argument_list|,
name|repeat
argument_list|,
name|updateCacheMetrics
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|value
operator|!=
literal|null
operator|)
operator|&&
name|caching
condition|)
block|{
if|if
condition|(
operator|(
name|value
operator|instanceof
name|HFileBlock
operator|)
operator|&&
operator|(
operator|(
name|HFileBlock
operator|)
name|value
operator|)
operator|.
name|usesSharedMemory
argument_list|()
condition|)
block|{
name|value
operator|=
operator|(
operator|(
name|HFileBlock
operator|)
name|value
operator|)
operator|.
name|deepClone
argument_list|()
expr_stmt|;
block|}
name|cacheBlock
argument_list|(
name|cacheKey
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|updateCacheMetrics
condition|)
block|{
name|stats
operator|.
name|hit
argument_list|(
name|caching
argument_list|,
name|cacheKey
operator|.
name|isPrimary
argument_list|()
argument_list|,
name|cacheKey
operator|.
name|getBlockType
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|value
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cacheBlock
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|,
name|Cacheable
name|value
parameter_list|,
name|boolean
name|inMemory
parameter_list|)
block|{
name|cacheBlock
argument_list|(
name|cacheKey
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cacheBlock
parameter_list|(
name|BlockCacheKey
name|key
parameter_list|,
name|Cacheable
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|.
name|heapSize
argument_list|()
operator|>
name|maxBlockSize
condition|)
block|{
comment|// If there are a lot of blocks that are too big this can make the logs too noisy (2% logged)
if|if
condition|(
name|stats
operator|.
name|failInsert
argument_list|()
operator|%
literal|50
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Trying to cache too large a block %s @ %,d is %,d which is larger than %,d"
argument_list|,
name|key
operator|.
name|getHfileName
argument_list|()
argument_list|,
name|key
operator|.
name|getOffset
argument_list|()
argument_list|,
name|value
operator|.
name|heapSize
argument_list|()
argument_list|,
name|DEFAULT_MAX_BLOCK_SIZE
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|cache
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|evictBlock
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|)
block|{
name|Cacheable
name|value
init|=
name|cache
operator|.
name|asMap
argument_list|()
operator|.
name|remove
argument_list|(
name|cacheKey
argument_list|)
decl_stmt|;
return|return
operator|(
name|value
operator|!=
literal|null
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|evictBlocksByHfileName
parameter_list|(
name|String
name|hfileName
parameter_list|)
block|{
name|int
name|evicted
init|=
literal|0
decl_stmt|;
for|for
control|(
name|BlockCacheKey
name|key
range|:
name|cache
operator|.
name|asMap
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|key
operator|.
name|getHfileName
argument_list|()
operator|.
name|equals
argument_list|(
name|hfileName
argument_list|)
operator|&&
name|evictBlock
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|evicted
operator|++
expr_stmt|;
block|}
block|}
if|if
condition|(
name|victimCache
operator|!=
literal|null
condition|)
block|{
name|evicted
operator|+=
name|victimCache
operator|.
name|evictBlocksByHfileName
argument_list|(
name|hfileName
argument_list|)
expr_stmt|;
block|}
return|return
name|evicted
return|;
block|}
annotation|@
name|Override
specifier|public
name|CacheStats
name|getStats
parameter_list|()
block|{
return|return
name|stats
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
if|if
condition|(
name|victimCache
operator|!=
literal|null
condition|)
block|{
name|victimCache
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
name|statsThreadPool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|BlockCache
index|[]
name|getBlockCaches
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|CachedBlock
argument_list|>
name|iterator
parameter_list|()
block|{
name|long
name|now
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
return|return
name|cache
operator|.
name|asMap
argument_list|()
operator|.
name|entrySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|entry
lambda|->
operator|(
name|CachedBlock
operator|)
operator|new
name|CachedBlockView
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|now
argument_list|)
argument_list|)
operator|.
name|iterator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|returnBlock
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|,
name|Cacheable
name|block
parameter_list|)
block|{
comment|// There is no SHARED type here in L1. But the block might have been served from the L2 victim
comment|// cache (when the Combined mode = false). So just try return this block to the victim cache.
comment|// Note : In case of CombinedBlockCache we will have this victim cache configured for L1
comment|// cache. But CombinedBlockCache will only call returnBlock on L2 cache.
if|if
condition|(
name|victimCache
operator|!=
literal|null
condition|)
block|{
name|victimCache
operator|.
name|returnBlock
argument_list|(
name|cacheKey
argument_list|,
name|block
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|logStats
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"totalSize="
operator|+
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|heapSize
argument_list|()
argument_list|)
operator|+
literal|", "
operator|+
literal|"freeSize="
operator|+
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|getFreeSize
argument_list|()
argument_list|)
operator|+
literal|", "
operator|+
literal|"max="
operator|+
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|size
argument_list|()
argument_list|)
operator|+
literal|", "
operator|+
literal|"blockCount="
operator|+
name|getBlockCount
argument_list|()
operator|+
literal|", "
operator|+
literal|"accesses="
operator|+
name|stats
operator|.
name|getRequestCount
argument_list|()
operator|+
literal|", "
operator|+
literal|"hits="
operator|+
name|stats
operator|.
name|getHitCount
argument_list|()
operator|+
literal|", "
operator|+
literal|"hitRatio="
operator|+
operator|(
name|stats
operator|.
name|getHitCount
argument_list|()
operator|==
literal|0
condition|?
literal|"0,"
else|:
name|StringUtils
operator|.
name|formatPercent
argument_list|(
name|stats
operator|.
name|getHitRatio
argument_list|()
argument_list|,
literal|2
argument_list|)
operator|+
literal|", "
operator|)
operator|+
literal|"cachingAccesses="
operator|+
name|stats
operator|.
name|getRequestCachingCount
argument_list|()
operator|+
literal|", "
operator|+
literal|"cachingHits="
operator|+
name|stats
operator|.
name|getHitCachingCount
argument_list|()
operator|+
literal|", "
operator|+
literal|"cachingHitsRatio="
operator|+
operator|(
name|stats
operator|.
name|getHitCachingCount
argument_list|()
operator|==
literal|0
condition|?
literal|"0,"
else|:
operator|(
name|StringUtils
operator|.
name|formatPercent
argument_list|(
name|stats
operator|.
name|getHitCachingRatio
argument_list|()
argument_list|,
literal|2
argument_list|)
operator|+
literal|", "
operator|)
operator|)
operator|+
literal|"evictions="
operator|+
name|stats
operator|.
name|getEvictionCount
argument_list|()
operator|+
literal|", "
operator|+
literal|"evicted="
operator|+
name|stats
operator|.
name|getEvictedCount
argument_list|()
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
name|MoreObjects
operator|.
name|toStringHelper
argument_list|(
name|this
argument_list|)
operator|.
name|add
argument_list|(
literal|"blockCount"
argument_list|,
name|getBlockCount
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
literal|"currentSize"
argument_list|,
name|getCurrentSize
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
literal|"freeSize"
argument_list|,
name|getFreeSize
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
literal|"maxSize"
argument_list|,
name|size
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
literal|"heapSize"
argument_list|,
name|heapSize
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
literal|"victimCache"
argument_list|,
operator|(
name|victimCache
operator|!=
literal|null
operator|)
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/** A removal listener to asynchronously record evictions and populate the victim cache. */
specifier|private
specifier|final
class|class
name|EvictionListener
implements|implements
name|RemovalListener
argument_list|<
name|BlockCacheKey
argument_list|,
name|Cacheable
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|void
name|onRemoval
parameter_list|(
name|BlockCacheKey
name|key
parameter_list|,
name|Cacheable
name|value
parameter_list|,
name|RemovalCause
name|cause
parameter_list|)
block|{
if|if
condition|(
operator|!
name|cause
operator|.
name|wasEvicted
argument_list|()
condition|)
block|{
comment|// An explicit eviction (invalidation) is not added to the victim cache as the data may
comment|// no longer be valid for subsequent queries.
return|return;
block|}
name|recordEviction
argument_list|()
expr_stmt|;
if|if
condition|(
name|victimCache
operator|==
literal|null
condition|)
block|{
return|return;
block|}
elseif|else
if|if
condition|(
name|victimCache
operator|instanceof
name|BucketCache
condition|)
block|{
name|BucketCache
name|victimBucketCache
init|=
operator|(
name|BucketCache
operator|)
name|victimCache
decl_stmt|;
name|victimBucketCache
operator|.
name|cacheBlockWithWait
argument_list|(
name|key
argument_list|,
name|value
argument_list|,
comment|/* inMemory */
literal|true
argument_list|,
comment|/* wait */
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|victimCache
operator|.
name|cacheBlock
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Records an eviction. The number of eviction operations and evicted blocks are identical, as    * an eviction is triggered immediately when the capacity has been exceeded. An eviction is    * performed asynchronously. See the library's documentation for details on write buffers,    * batching, and maintenance behavior.    */
specifier|private
name|void
name|recordEviction
parameter_list|()
block|{
comment|// FIXME: Currently does not capture the insertion time
name|stats
operator|.
name|evicted
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|stats
operator|.
name|evict
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
class|class
name|CachedBlockView
implements|implements
name|CachedBlock
block|{
specifier|private
specifier|static
specifier|final
name|Comparator
argument_list|<
name|CachedBlock
argument_list|>
name|COMPARATOR
init|=
name|Comparator
operator|.
name|comparing
argument_list|(
name|CachedBlock
operator|::
name|getFilename
argument_list|)
operator|.
name|thenComparing
argument_list|(
name|CachedBlock
operator|::
name|getOffset
argument_list|)
operator|.
name|thenComparing
argument_list|(
name|CachedBlock
operator|::
name|getCachedTime
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|BlockCacheKey
name|key
decl_stmt|;
specifier|private
specifier|final
name|Cacheable
name|value
decl_stmt|;
specifier|private
specifier|final
name|long
name|now
decl_stmt|;
specifier|public
name|CachedBlockView
parameter_list|(
name|BlockCacheKey
name|key
parameter_list|,
name|Cacheable
name|value
parameter_list|,
name|long
name|now
parameter_list|)
block|{
name|this
operator|.
name|now
operator|=
name|now
expr_stmt|;
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|BlockPriority
name|getBlockPriority
parameter_list|()
block|{
comment|// This does not appear to be used in any meaningful way and is irrelevant to this cache
return|return
name|BlockPriority
operator|.
name|MEMORY
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
name|value
operator|.
name|getBlockType
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getOffset
parameter_list|()
block|{
return|return
name|key
operator|.
name|getOffset
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getSize
parameter_list|()
block|{
return|return
name|value
operator|.
name|heapSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCachedTime
parameter_list|()
block|{
comment|// This does not appear to be used in any meaningful way, so not captured
return|return
literal|0L
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getFilename
parameter_list|()
block|{
return|return
name|key
operator|.
name|getHfileName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|CachedBlock
name|other
parameter_list|)
block|{
return|return
name|COMPARATOR
operator|.
name|compare
argument_list|(
name|this
argument_list|,
name|other
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|==
name|this
condition|)
block|{
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|CachedBlock
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|CachedBlock
name|other
init|=
operator|(
name|CachedBlock
operator|)
name|obj
decl_stmt|;
return|return
name|compareTo
argument_list|(
name|other
argument_list|)
operator|==
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|key
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|BlockCacheUtil
operator|.
name|toString
argument_list|(
name|this
argument_list|,
name|now
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMaxSize
parameter_list|()
block|{
return|return
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCurrentDataSize
parameter_list|()
block|{
return|return
name|getCurrentSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getDataBlockCount
parameter_list|()
block|{
return|return
name|getBlockCount
argument_list|()
return|;
block|}
block|}
end_class

end_unit

