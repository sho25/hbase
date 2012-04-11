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
name|List
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
name|ConcurrentMap
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
name|classification
operator|.
name|InterfaceAudience
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
name|BlockCacheKey
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
name|io
operator|.
name|hfile
operator|.
name|CacheableDeserializer
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
name|cache
operator|.
name|CacheBuilder
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
name|cache
operator|.
name|RemovalListener
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
name|cache
operator|.
name|RemovalNotification
import|;
end_import

begin_comment
comment|/**  * SingleSizeCache is a slab allocated cache that caches elements up to a single  * size. It uses a slab allocator (Slab.java) to divide a direct bytebuffer,  * into evenly sized blocks. Any cached data will take up exactly 1 block. An  * exception will be thrown if the cached data cannot fit into the blockSize of  * this SingleSizeCache.  *  * Eviction and LRUness is taken care of by Guava's MapMaker, which creates a  * ConcurrentLinkedHashMap.  *  **/
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SingleSizeCache
implements|implements
name|BlockCache
implements|,
name|HeapSize
block|{
specifier|private
specifier|final
name|Slab
name|backingStore
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|BlockCacheKey
argument_list|,
name|CacheablePair
argument_list|>
name|backingMap
decl_stmt|;
specifier|private
specifier|final
name|int
name|numBlocks
decl_stmt|;
specifier|private
specifier|final
name|int
name|blockSize
decl_stmt|;
specifier|private
specifier|final
name|CacheStats
name|stats
decl_stmt|;
specifier|private
specifier|final
name|SlabItemActionWatcher
name|actionWatcher
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|size
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|timeSinceLastAccess
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|long
name|CACHE_FIXED_OVERHEAD
init|=
name|ClassSize
operator|.
name|align
argument_list|(
operator|(
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
operator|)
operator|+
operator|(
literal|5
operator|*
name|ClassSize
operator|.
name|REFERENCE
operator|)
operator|+
operator|+
name|ClassSize
operator|.
name|OBJECT
argument_list|)
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
name|SingleSizeCache
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Default constructor. Specify the size of the blocks, number of blocks, and    * the SlabCache this cache will be assigned to.    *    *    * @param blockSize the size of each block, in bytes    *    * @param numBlocks the number of blocks of blockSize this cache will hold.    *    * @param master the SlabCache this SingleSlabCache is assigned to.    */
specifier|public
name|SingleSizeCache
parameter_list|(
name|int
name|blockSize
parameter_list|,
name|int
name|numBlocks
parameter_list|,
name|SlabItemActionWatcher
name|master
parameter_list|)
block|{
name|this
operator|.
name|blockSize
operator|=
name|blockSize
expr_stmt|;
name|this
operator|.
name|numBlocks
operator|=
name|numBlocks
expr_stmt|;
name|backingStore
operator|=
operator|new
name|Slab
argument_list|(
name|blockSize
argument_list|,
name|numBlocks
argument_list|)
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
name|actionWatcher
operator|=
name|master
expr_stmt|;
name|this
operator|.
name|size
operator|=
operator|new
name|AtomicLong
argument_list|(
name|CACHE_FIXED_OVERHEAD
operator|+
name|backingStore
operator|.
name|heapSize
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|timeSinceLastAccess
operator|=
operator|new
name|AtomicLong
argument_list|()
expr_stmt|;
comment|// This evictionListener is called whenever the cache automatically
comment|// evicts
comment|// something.
name|RemovalListener
argument_list|<
name|BlockCacheKey
argument_list|,
name|CacheablePair
argument_list|>
name|listener
init|=
operator|new
name|RemovalListener
argument_list|<
name|BlockCacheKey
argument_list|,
name|CacheablePair
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onRemoval
parameter_list|(
name|RemovalNotification
argument_list|<
name|BlockCacheKey
argument_list|,
name|CacheablePair
argument_list|>
name|notification
parameter_list|)
block|{
if|if
condition|(
operator|!
name|notification
operator|.
name|wasEvicted
argument_list|()
condition|)
block|{
comment|// Only process removals by eviction, not by replacement or
comment|// explicit removal
return|return;
block|}
name|CacheablePair
name|value
init|=
name|notification
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|timeSinceLastAccess
operator|.
name|set
argument_list|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|value
operator|.
name|recentlyAccessed
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|stats
operator|.
name|evict
argument_list|()
expr_stmt|;
name|doEviction
argument_list|(
name|notification
operator|.
name|getKey
argument_list|()
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|backingMap
operator|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|maximumSize
argument_list|(
name|numBlocks
operator|-
literal|1
argument_list|)
operator|.
name|removalListener
argument_list|(
name|listener
argument_list|)
operator|.
operator|<
name|BlockCacheKey
operator|,
name|CacheablePair
operator|>
name|build
argument_list|()
operator|.
name|asMap
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cacheBlock
parameter_list|(
name|BlockCacheKey
name|blockName
parameter_list|,
name|Cacheable
name|toBeCached
parameter_list|)
block|{
name|ByteBuffer
name|storedBlock
decl_stmt|;
try|try
block|{
name|storedBlock
operator|=
name|backingStore
operator|.
name|alloc
argument_list|(
name|toBeCached
operator|.
name|getSerializedLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"SlabAllocator was interrupted while waiting for block to become available"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
name|CacheablePair
name|newEntry
init|=
operator|new
name|CacheablePair
argument_list|(
name|toBeCached
operator|.
name|getDeserializer
argument_list|()
argument_list|,
name|storedBlock
argument_list|)
decl_stmt|;
name|toBeCached
operator|.
name|serialize
argument_list|(
name|storedBlock
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|this
init|)
block|{
name|CacheablePair
name|alreadyCached
init|=
name|backingMap
operator|.
name|putIfAbsent
argument_list|(
name|blockName
argument_list|,
name|newEntry
argument_list|)
decl_stmt|;
if|if
condition|(
name|alreadyCached
operator|!=
literal|null
condition|)
block|{
name|backingStore
operator|.
name|free
argument_list|(
name|storedBlock
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"already cached "
operator|+
name|blockName
argument_list|)
throw|;
block|}
if|if
condition|(
name|actionWatcher
operator|!=
literal|null
condition|)
block|{
name|actionWatcher
operator|.
name|onInsertion
argument_list|(
name|blockName
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
block|}
name|newEntry
operator|.
name|recentlyAccessed
operator|.
name|set
argument_list|(
name|System
operator|.
name|nanoTime
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|size
operator|.
name|addAndGet
argument_list|(
name|newEntry
operator|.
name|heapSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Cacheable
name|getBlock
parameter_list|(
name|BlockCacheKey
name|key
parameter_list|,
name|boolean
name|caching
parameter_list|)
block|{
name|CacheablePair
name|contentBlock
init|=
name|backingMap
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|contentBlock
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
name|stats
operator|.
name|hit
argument_list|(
name|caching
argument_list|)
expr_stmt|;
comment|// If lock cannot be obtained, that means we're undergoing eviction.
try|try
block|{
name|contentBlock
operator|.
name|recentlyAccessed
operator|.
name|set
argument_list|(
name|System
operator|.
name|nanoTime
argument_list|()
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|contentBlock
init|)
block|{
if|if
condition|(
name|contentBlock
operator|.
name|serializedData
operator|==
literal|null
condition|)
block|{
comment|// concurrently evicted
name|LOG
operator|.
name|warn
argument_list|(
literal|"Concurrent eviction of "
operator|+
name|key
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
name|contentBlock
operator|.
name|deserializer
operator|.
name|deserialize
argument_list|(
name|contentBlock
operator|.
name|serializedData
operator|.
name|asReadOnlyBuffer
argument_list|()
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Deserializer threw an exception. This may indicate a bug."
argument_list|,
name|t
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Evicts the block    *    * @param key the key of the entry we are going to evict    * @return the evicted ByteBuffer    */
specifier|public
name|boolean
name|evictBlock
parameter_list|(
name|BlockCacheKey
name|key
parameter_list|)
block|{
name|stats
operator|.
name|evict
argument_list|()
expr_stmt|;
name|CacheablePair
name|evictedBlock
init|=
name|backingMap
operator|.
name|remove
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|evictedBlock
operator|!=
literal|null
condition|)
block|{
name|doEviction
argument_list|(
name|key
argument_list|,
name|evictedBlock
argument_list|)
expr_stmt|;
block|}
return|return
name|evictedBlock
operator|!=
literal|null
return|;
block|}
specifier|private
name|void
name|doEviction
parameter_list|(
name|BlockCacheKey
name|key
parameter_list|,
name|CacheablePair
name|evictedBlock
parameter_list|)
block|{
name|long
name|evictedHeap
init|=
literal|0
decl_stmt|;
synchronized|synchronized
init|(
name|evictedBlock
init|)
block|{
if|if
condition|(
name|evictedBlock
operator|.
name|serializedData
operator|==
literal|null
condition|)
block|{
comment|// someone else already freed
return|return;
block|}
name|evictedHeap
operator|=
name|evictedBlock
operator|.
name|heapSize
argument_list|()
expr_stmt|;
name|ByteBuffer
name|bb
init|=
name|evictedBlock
operator|.
name|serializedData
decl_stmt|;
name|evictedBlock
operator|.
name|serializedData
operator|=
literal|null
expr_stmt|;
name|backingStore
operator|.
name|free
argument_list|(
name|bb
argument_list|)
expr_stmt|;
comment|// We have to do this callback inside the synchronization here.
comment|// Otherwise we can have the following interleaving:
comment|// Thread A calls getBlock():
comment|// SlabCache directs call to this SingleSizeCache
comment|// It gets the CacheablePair object
comment|// Thread B runs eviction
comment|// doEviction() is called and sets serializedData = null, here.
comment|// Thread A sees the null serializedData, and returns null
comment|// Thread A calls cacheBlock on the same block, and gets
comment|// "already cached" since the block is still in backingStore
if|if
condition|(
name|actionWatcher
operator|!=
literal|null
condition|)
block|{
name|actionWatcher
operator|.
name|onEviction
argument_list|(
name|key
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
block|}
name|stats
operator|.
name|evicted
argument_list|()
expr_stmt|;
name|size
operator|.
name|addAndGet
argument_list|(
operator|-
literal|1
operator|*
name|evictedHeap
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|logStats
parameter_list|()
block|{
name|long
name|milliseconds
init|=
name|this
operator|.
name|timeSinceLastAccess
operator|.
name|get
argument_list|()
operator|/
literal|1000000
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"For Slab of size "
operator|+
name|this
operator|.
name|blockSize
operator|+
literal|": "
operator|+
name|this
operator|.
name|getOccupiedSize
argument_list|()
operator|/
name|this
operator|.
name|blockSize
operator|+
literal|" occupied, out of a capacity of "
operator|+
name|this
operator|.
name|numBlocks
operator|+
literal|" blocks. HeapSize is "
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|this
operator|.
name|heapSize
argument_list|()
argument_list|)
operator|+
literal|" bytes."
operator|+
literal|", "
operator|+
literal|"churnTime="
operator|+
name|StringUtils
operator|.
name|formatTime
argument_list|(
name|milliseconds
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Slab Stats: "
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
literal|"0"
else|:
operator|(
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
literal|"%, "
operator|)
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
literal|"0"
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
literal|"%, "
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
operator|+
literal|", "
operator|+
literal|"evictedPerRun="
operator|+
name|stats
operator|.
name|evictedPerEviction
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
name|backingStore
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
return|return
name|this
operator|.
name|size
operator|.
name|get
argument_list|()
operator|+
name|backingStore
operator|.
name|heapSize
argument_list|()
return|;
block|}
specifier|public
name|long
name|size
parameter_list|()
block|{
return|return
operator|(
name|long
operator|)
name|this
operator|.
name|blockSize
operator|*
operator|(
name|long
operator|)
name|this
operator|.
name|numBlocks
return|;
block|}
specifier|public
name|long
name|getFreeSize
parameter_list|()
block|{
return|return
operator|(
name|long
operator|)
name|backingStore
operator|.
name|getBlocksRemaining
argument_list|()
operator|*
operator|(
name|long
operator|)
name|blockSize
return|;
block|}
specifier|public
name|long
name|getOccupiedSize
parameter_list|()
block|{
return|return
call|(
name|long
call|)
argument_list|(
name|numBlocks
operator|-
name|backingStore
operator|.
name|getBlocksRemaining
argument_list|()
argument_list|)
operator|*
operator|(
name|long
operator|)
name|blockSize
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
annotation|@
name|Override
specifier|public
name|long
name|getBlockCount
parameter_list|()
block|{
return|return
name|numBlocks
operator|-
name|backingStore
operator|.
name|getBlocksRemaining
argument_list|()
return|;
block|}
comment|/* Since its offheap, it doesn't matter if its in memory or not */
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
name|buf
parameter_list|,
name|boolean
name|inMemory
parameter_list|)
block|{
name|this
operator|.
name|cacheBlock
argument_list|(
name|cacheKey
argument_list|,
name|buf
argument_list|)
expr_stmt|;
block|}
comment|/*    * This is never called, as evictions are handled in the SlabCache layer,    * implemented in the event we want to use this as a standalone cache.    */
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
name|evictedCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|BlockCacheKey
name|e
range|:
name|backingMap
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|e
operator|.
name|getHfileName
argument_list|()
operator|.
name|equals
argument_list|(
name|hfileName
argument_list|)
condition|)
block|{
name|this
operator|.
name|evictBlock
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|evictedCount
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
literal|0
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
comment|/* Just a pair class, holds a reference to the parent cacheable */
specifier|private
specifier|static
class|class
name|CacheablePair
implements|implements
name|HeapSize
block|{
specifier|final
name|CacheableDeserializer
argument_list|<
name|Cacheable
argument_list|>
name|deserializer
decl_stmt|;
name|ByteBuffer
name|serializedData
decl_stmt|;
name|AtomicLong
name|recentlyAccessed
decl_stmt|;
specifier|private
name|CacheablePair
parameter_list|(
name|CacheableDeserializer
argument_list|<
name|Cacheable
argument_list|>
name|deserializer
parameter_list|,
name|ByteBuffer
name|serializedData
parameter_list|)
block|{
name|this
operator|.
name|recentlyAccessed
operator|=
operator|new
name|AtomicLong
argument_list|()
expr_stmt|;
name|this
operator|.
name|deserializer
operator|=
name|deserializer
expr_stmt|;
name|this
operator|.
name|serializedData
operator|=
name|serializedData
expr_stmt|;
block|}
comment|/*      * Heapsize overhead of this is the default object overhead, the heapsize of      * the serialized object, and the cost of a reference to the bytebuffer,      * which is already accounted for in SingleSizeCache      */
annotation|@
name|Override
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
return|return
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|OBJECT
operator|+
name|ClassSize
operator|.
name|REFERENCE
operator|*
literal|3
operator|+
name|ClassSize
operator|.
name|ATOMIC_LONG
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

