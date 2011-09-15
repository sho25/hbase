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
operator|.
name|slab
package|;
end_package

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigDecimal
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|TreeMap
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
name|ConcurrentHashMap
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
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
name|BlockCache
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
name|BlockCacheColumnFamilySummary
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
name|CacheStats
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
name|Cacheable
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
name|ClassSize
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_import
import|import
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

begin_comment
comment|/**  * SlabCache is composed of multiple SingleSizeCaches. It uses a TreeMap in  * order to determine where a given element fits. Redirects gets and puts to the  * correct SingleSizeCache.  *  **/
end_comment

begin_class
specifier|public
class|class
name|SlabCache
implements|implements
name|SlabItemEvictionWatcher
implements|,
name|BlockCache
implements|,
name|HeapSize
block|{
specifier|private
specifier|final
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|SingleSizeCache
argument_list|>
name|backingStore
decl_stmt|;
specifier|private
specifier|final
name|TreeMap
argument_list|<
name|Integer
argument_list|,
name|SingleSizeCache
argument_list|>
name|sizer
decl_stmt|;
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SlabCache
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|int
name|STAT_THREAD_PERIOD_SECS
init|=
literal|60
operator|*
literal|5
decl_stmt|;
specifier|private
specifier|final
name|ScheduledExecutorService
name|scheduleThreadPool
init|=
name|Executors
operator|.
name|newScheduledThreadPool
argument_list|(
literal|1
argument_list|,
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setNameFormat
argument_list|(
literal|"Slab Statistics #%d"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|size
decl_stmt|;
specifier|private
specifier|final
name|CacheStats
name|stats
decl_stmt|;
specifier|final
name|SlabStats
name|requestStats
decl_stmt|;
specifier|final
name|SlabStats
name|successfullyCachedStats
decl_stmt|;
specifier|private
specifier|final
name|long
name|avgBlockSize
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|CACHE_FIXED_OVERHEAD
init|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|SlabCache
operator|.
name|class
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|/**    * Default constructor, creates an empty SlabCache.    *    * @param size Total size allocated to the SlabCache. (Bytes)    * @param avgBlockSize Average size of a block being cached.    **/
specifier|public
name|SlabCache
parameter_list|(
name|long
name|size
parameter_list|,
name|long
name|avgBlockSize
parameter_list|)
block|{
name|this
operator|.
name|avgBlockSize
operator|=
name|avgBlockSize
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
name|this
operator|.
name|stats
operator|=
operator|new
name|CacheStats
argument_list|()
expr_stmt|;
name|this
operator|.
name|requestStats
operator|=
operator|new
name|SlabStats
argument_list|()
expr_stmt|;
name|this
operator|.
name|successfullyCachedStats
operator|=
operator|new
name|SlabStats
argument_list|()
expr_stmt|;
name|backingStore
operator|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|SingleSizeCache
argument_list|>
argument_list|()
expr_stmt|;
name|sizer
operator|=
operator|new
name|TreeMap
argument_list|<
name|Integer
argument_list|,
name|SingleSizeCache
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|scheduleThreadPool
operator|.
name|scheduleAtFixedRate
argument_list|(
operator|new
name|StatisticsThread
argument_list|(
name|this
argument_list|)
argument_list|,
name|STAT_THREAD_PERIOD_SECS
argument_list|,
name|STAT_THREAD_PERIOD_SECS
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
comment|/**    * A way of allocating the desired amount of Slabs of each particular size.    *    * This reads two lists from conf, hbase.offheap.slab.proportions and    * hbase.offheap.slab.sizes.    *    * The first list is the percentage of our total space we allocate to the    * slabs.    *    * The second list is blocksize of the slabs in bytes. (E.g. the slab holds    * blocks of this size).    *    * @param Configuration file.    */
specifier|public
name|void
name|addSlabByConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// Proportions we allocate to each slab of the total size.
name|String
index|[]
name|porportions
init|=
name|conf
operator|.
name|getStrings
argument_list|(
literal|"hbase.offheapcache.slab.proportions"
argument_list|,
literal|"0.80"
argument_list|,
literal|"0.20"
argument_list|)
decl_stmt|;
name|String
index|[]
name|sizes
init|=
name|conf
operator|.
name|getStrings
argument_list|(
literal|"hbase.offheapcache.slab.sizes"
argument_list|,
operator|new
name|Long
argument_list|(
name|avgBlockSize
operator|*
literal|11
operator|/
literal|10
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
operator|new
name|Long
argument_list|(
name|avgBlockSize
operator|*
literal|21
operator|/
literal|10
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|porportions
operator|.
name|length
operator|!=
name|sizes
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"SlabCache conf not "
operator|+
literal|"initialized, error in configuration. hbase.offheap.slab.proportions specifies "
operator|+
name|porportions
operator|.
name|length
operator|+
literal|" slabs while hbase.offheap.slab.sizes specifies "
operator|+
name|sizes
operator|.
name|length
operator|+
literal|" slabs "
operator|+
literal|"offheapslabporportions and offheapslabsizes"
argument_list|)
throw|;
block|}
comment|/* We use BigDecimals instead of floats because float rounding is annoying */
name|BigDecimal
index|[]
name|parsedProportions
init|=
name|stringArrayToBigDecimalArray
argument_list|(
name|porportions
argument_list|)
decl_stmt|;
name|BigDecimal
index|[]
name|parsedSizes
init|=
name|stringArrayToBigDecimalArray
argument_list|(
name|sizes
argument_list|)
decl_stmt|;
name|BigDecimal
name|sumProportions
init|=
operator|new
name|BigDecimal
argument_list|(
literal|0
argument_list|)
decl_stmt|;
for|for
control|(
name|BigDecimal
name|b
range|:
name|parsedProportions
control|)
block|{
comment|/* Make sure all proportions are greater than 0 */
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|b
operator|.
name|compareTo
argument_list|(
name|BigDecimal
operator|.
name|ZERO
argument_list|)
operator|==
literal|1
argument_list|,
literal|"Proportions in hbase.offheap.slab.proportions must be greater than 0!"
argument_list|)
expr_stmt|;
name|sumProportions
operator|=
name|sumProportions
operator|.
name|add
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
comment|/* If the sum is greater than 1 */
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|sumProportions
operator|.
name|compareTo
argument_list|(
name|BigDecimal
operator|.
name|ONE
argument_list|)
operator|!=
literal|1
argument_list|,
literal|"Sum of all proportions in hbase.offheap.slab.proportions must be less than 1"
argument_list|)
expr_stmt|;
comment|/* If the sum of all proportions is less than 0.99 */
if|if
condition|(
name|sumProportions
operator|.
name|compareTo
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|"0.99"
argument_list|)
argument_list|)
operator|==
operator|-
literal|1
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Sum of hbase.offheap.slab.proportions is less than 0.99! Memory is being wasted"
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|parsedProportions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|int
name|blockSize
init|=
name|parsedSizes
index|[
name|i
index|]
operator|.
name|intValue
argument_list|()
decl_stmt|;
name|int
name|numBlocks
init|=
operator|new
name|BigDecimal
argument_list|(
name|this
operator|.
name|size
argument_list|)
operator|.
name|multiply
argument_list|(
name|parsedProportions
index|[
name|i
index|]
argument_list|)
operator|.
name|divide
argument_list|(
name|parsedSizes
index|[
name|i
index|]
argument_list|,
name|BigDecimal
operator|.
name|ROUND_DOWN
argument_list|)
operator|.
name|intValue
argument_list|()
decl_stmt|;
name|addSlab
argument_list|(
name|blockSize
argument_list|,
name|numBlocks
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Gets the size of the slab cache a ByteBuffer of this size would be    * allocated to.    *    * @param size Size of the ByteBuffer we are checking.    *    * @return the Slab that the above bytebuffer would be allocated towards. If    *         object is too large, returns null.    */
name|Entry
argument_list|<
name|Integer
argument_list|,
name|SingleSizeCache
argument_list|>
name|getHigherBlock
parameter_list|(
name|int
name|size
parameter_list|)
block|{
return|return
name|sizer
operator|.
name|higherEntry
argument_list|(
name|size
operator|-
literal|1
argument_list|)
return|;
block|}
specifier|private
name|BigDecimal
index|[]
name|stringArrayToBigDecimalArray
parameter_list|(
name|String
index|[]
name|parsee
parameter_list|)
block|{
name|BigDecimal
index|[]
name|parsed
init|=
operator|new
name|BigDecimal
index|[
name|parsee
operator|.
name|length
index|]
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
name|parsee
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|parsed
index|[
name|i
index|]
operator|=
operator|new
name|BigDecimal
argument_list|(
name|parsee
index|[
name|i
index|]
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|parsed
return|;
block|}
specifier|private
name|void
name|addSlab
parameter_list|(
name|int
name|blockSize
parameter_list|,
name|int
name|numBlocks
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating a slab of blockSize "
operator|+
name|blockSize
operator|+
literal|" with "
operator|+
name|numBlocks
operator|+
literal|" blocks."
argument_list|)
expr_stmt|;
name|sizer
operator|.
name|put
argument_list|(
name|blockSize
argument_list|,
operator|new
name|SingleSizeCache
argument_list|(
name|blockSize
argument_list|,
name|numBlocks
argument_list|,
name|this
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Cache the block with the specified name and buffer. First finds what size    * SingleSlabCache it should fit in. If the block doesn't fit in any, it will    * return without doing anything.    *<p>    * It is assumed this will NEVER be called on an already cached block. If that    * is done, it is assumed that you are reinserting the same exact block due to    * a race condition, and will throw a runtime exception.    *    * @param blockName block name    * @param cachedItem block buffer    */
specifier|public
name|void
name|cacheBlock
parameter_list|(
name|String
name|blockName
parameter_list|,
name|Cacheable
name|cachedItem
parameter_list|)
block|{
name|Entry
argument_list|<
name|Integer
argument_list|,
name|SingleSizeCache
argument_list|>
name|scacheEntry
init|=
name|getHigherBlock
argument_list|(
name|cachedItem
operator|.
name|getSerializedLength
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|requestStats
operator|.
name|addin
argument_list|(
name|cachedItem
operator|.
name|getSerializedLength
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|scacheEntry
operator|==
literal|null
condition|)
block|{
return|return;
comment|// we can't cache, something too big.
block|}
name|this
operator|.
name|successfullyCachedStats
operator|.
name|addin
argument_list|(
name|cachedItem
operator|.
name|getSerializedLength
argument_list|()
argument_list|)
expr_stmt|;
name|SingleSizeCache
name|scache
init|=
name|scacheEntry
operator|.
name|getValue
argument_list|()
decl_stmt|;
comment|/*This will throw a runtime exception if we try to cache the same value twice*/
name|scache
operator|.
name|cacheBlock
argument_list|(
name|blockName
argument_list|,
name|cachedItem
argument_list|)
expr_stmt|;
comment|/*Spinlock, if we're spinlocking, that means an eviction hasn't taken place yet*/
while|while
condition|(
name|backingStore
operator|.
name|putIfAbsent
argument_list|(
name|blockName
argument_list|,
name|scache
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|Thread
operator|.
name|yield
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * We don't care about whether its in memory or not, so we just pass the call    * through.    */
specifier|public
name|void
name|cacheBlock
parameter_list|(
name|String
name|blockName
parameter_list|,
name|Cacheable
name|buf
parameter_list|,
name|boolean
name|inMemory
parameter_list|)
block|{
name|cacheBlock
argument_list|(
name|blockName
argument_list|,
name|buf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CacheStats
name|getStats
parameter_list|()
block|{
return|return
name|this
operator|.
name|stats
return|;
block|}
comment|/**    * Get the buffer of the block with the specified name.    *    * @param blockName block name    * @return buffer of specified block name, or null if not in cache    */
specifier|public
name|Cacheable
name|getBlock
parameter_list|(
name|String
name|key
parameter_list|,
name|boolean
name|caching
parameter_list|)
block|{
name|SingleSizeCache
name|cachedBlock
init|=
name|backingStore
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|cachedBlock
operator|==
literal|null
condition|)
block|{
name|stats
operator|.
name|miss
argument_list|(
name|caching
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|Cacheable
name|contentBlock
init|=
name|cachedBlock
operator|.
name|getBlock
argument_list|(
name|key
argument_list|,
name|caching
argument_list|)
decl_stmt|;
if|if
condition|(
name|contentBlock
operator|!=
literal|null
condition|)
block|{
name|stats
operator|.
name|hit
argument_list|(
name|caching
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|stats
operator|.
name|miss
argument_list|(
name|caching
argument_list|)
expr_stmt|;
block|}
return|return
name|contentBlock
return|;
block|}
comment|/**    * Evicts a block from the cache. This is public, and thus contributes to the    * the evict counter.    */
specifier|public
name|boolean
name|evictBlock
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|stats
operator|.
name|evict
argument_list|()
expr_stmt|;
return|return
name|onEviction
argument_list|(
name|key
argument_list|,
literal|true
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|onEviction
parameter_list|(
name|String
name|key
parameter_list|,
name|boolean
name|callAssignedCache
parameter_list|)
block|{
name|SingleSizeCache
name|cacheEntry
init|=
name|backingStore
operator|.
name|remove
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|cacheEntry
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|/* we need to bump up stats.evict, as this call came from the assignedCache. */
if|if
condition|(
name|callAssignedCache
operator|==
literal|false
condition|)
block|{
name|stats
operator|.
name|evict
argument_list|()
expr_stmt|;
block|}
name|stats
operator|.
name|evicted
argument_list|()
expr_stmt|;
if|if
condition|(
name|callAssignedCache
condition|)
block|{
name|cacheEntry
operator|.
name|evictBlock
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
comment|/**    * Sends a shutdown to all SingleSizeCache's contained by this cache.    *    * Also terminates the scheduleThreadPool.    */
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
for|for
control|(
name|SingleSizeCache
name|s
range|:
name|sizer
operator|.
name|values
argument_list|()
control|)
block|{
name|s
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|scheduleThreadPool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
name|long
name|childCacheSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|SingleSizeCache
name|s
range|:
name|sizer
operator|.
name|values
argument_list|()
control|)
block|{
name|childCacheSize
operator|+=
name|s
operator|.
name|heapSize
argument_list|()
expr_stmt|;
block|}
return|return
name|SlabCache
operator|.
name|CACHE_FIXED_OVERHEAD
operator|+
name|childCacheSize
return|;
block|}
specifier|public
name|long
name|size
parameter_list|()
block|{
return|return
name|this
operator|.
name|size
return|;
block|}
specifier|public
name|long
name|getFreeSize
parameter_list|()
block|{
return|return
literal|0
return|;
comment|// this cache, by default, allocates all its space.
block|}
specifier|public
name|long
name|getCurrentSize
parameter_list|()
block|{
return|return
name|size
return|;
block|}
specifier|public
name|long
name|getEvictedCount
parameter_list|()
block|{
return|return
name|stats
operator|.
name|getEvictedCount
argument_list|()
return|;
block|}
comment|/*    * Statistics thread. Periodically prints the cache statistics to the log.    */
specifier|static
class|class
name|StatisticsThread
extends|extends
name|Thread
block|{
name|SlabCache
name|ourcache
decl_stmt|;
specifier|public
name|StatisticsThread
parameter_list|(
name|SlabCache
name|slabCache
parameter_list|)
block|{
name|super
argument_list|(
literal|"SlabCache.StatisticsThread"
argument_list|)
expr_stmt|;
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|ourcache
operator|=
name|slabCache
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
for|for
control|(
name|SingleSizeCache
name|s
range|:
name|ourcache
operator|.
name|sizer
operator|.
name|values
argument_list|()
control|)
block|{
name|s
operator|.
name|logStats
argument_list|()
expr_stmt|;
block|}
name|SlabCache
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Current heap size is: "
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|ourcache
operator|.
name|heapSize
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Request Stats"
argument_list|)
expr_stmt|;
name|ourcache
operator|.
name|requestStats
operator|.
name|logStats
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Successfully Cached Stats"
argument_list|)
expr_stmt|;
name|ourcache
operator|.
name|successfullyCachedStats
operator|.
name|logStats
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Just like CacheStats, but more Slab specific. Finely grained profiling of    * sizes we store using logs.    *    */
specifier|static
class|class
name|SlabStats
block|{
comment|// the maximum size somebody will ever try to cache, then we multiply by 10
comment|// so we have finer grained stats.
specifier|final
name|int
name|MULTIPLIER
init|=
literal|10
decl_stmt|;
specifier|final
name|int
name|NUMDIVISIONS
init|=
call|(
name|int
call|)
argument_list|(
name|Math
operator|.
name|log
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|*
name|MULTIPLIER
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
index|[]
name|counts
init|=
operator|new
name|AtomicLong
index|[
name|NUMDIVISIONS
index|]
decl_stmt|;
specifier|public
name|SlabStats
parameter_list|()
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
name|NUMDIVISIONS
condition|;
name|i
operator|++
control|)
block|{
name|counts
index|[
name|i
index|]
operator|=
operator|new
name|AtomicLong
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|addin
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|int
name|index
init|=
call|(
name|int
call|)
argument_list|(
name|Math
operator|.
name|log
argument_list|(
name|size
argument_list|)
operator|*
name|MULTIPLIER
argument_list|)
decl_stmt|;
name|counts
index|[
name|index
index|]
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
specifier|public
name|AtomicLong
index|[]
name|getUsage
parameter_list|()
block|{
return|return
name|counts
return|;
block|}
name|double
name|getUpperBound
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|Math
operator|.
name|pow
argument_list|(
name|Math
operator|.
name|E
argument_list|,
operator|(
call|(
name|double
call|)
argument_list|(
name|index
operator|+
literal|0.5
argument_list|)
operator|/
operator|(
name|double
operator|)
name|MULTIPLIER
operator|)
argument_list|)
return|;
block|}
name|double
name|getLowerBound
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|Math
operator|.
name|pow
argument_list|(
name|Math
operator|.
name|E
argument_list|,
operator|(
call|(
name|double
call|)
argument_list|(
name|index
operator|-
literal|0.5
argument_list|)
operator|/
operator|(
name|double
operator|)
name|MULTIPLIER
operator|)
argument_list|)
return|;
block|}
specifier|public
name|void
name|logStats
parameter_list|()
block|{
name|AtomicLong
index|[]
name|fineGrainedStats
init|=
name|getUsage
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
name|fineGrainedStats
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|fineGrainedStats
index|[
name|i
index|]
operator|.
name|get
argument_list|()
operator|>
literal|0
condition|)
block|{
name|SlabCache
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"From  "
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
operator|(
name|long
operator|)
name|getLowerBound
argument_list|(
name|i
argument_list|)
argument_list|)
operator|+
literal|"- "
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
operator|(
name|long
operator|)
name|getUpperBound
argument_list|(
name|i
argument_list|)
argument_list|)
operator|+
literal|": "
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|fineGrainedStats
index|[
name|i
index|]
operator|.
name|get
argument_list|()
argument_list|)
operator|+
literal|" requests"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|public
name|int
name|evictBlocksByPrefix
parameter_list|(
name|String
name|prefix
parameter_list|)
block|{
name|int
name|numEvicted
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|key
range|:
name|backingStore
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|key
operator|.
name|startsWith
argument_list|(
name|prefix
argument_list|)
condition|)
block|{
if|if
condition|(
name|evictBlock
argument_list|(
name|key
argument_list|)
condition|)
operator|++
name|numEvicted
expr_stmt|;
block|}
block|}
return|return
name|numEvicted
return|;
block|}
comment|/*    * Not implemented. Extremely costly to do this from the off heap cache, you'd    * need to copy every object on heap once    */
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|BlockCacheColumnFamilySummary
argument_list|>
name|getBlockCacheColumnFamilySummaries
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
end_class

end_unit

