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
name|hfile
package|;
end_package

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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
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
name|BlockType
operator|.
name|BlockCategory
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

begin_comment
comment|/**  * CombinedBlockCache is an abstraction layer that combines  * {@link LruBlockCache} and {@link BucketCache}. The smaller lruCache is used  * to cache bloom blocks and index blocks.  The larger l2Cache is used to  * cache data blocks. {@link #getBlock(BlockCacheKey, boolean, boolean, boolean)} reads  * first from the smaller lruCache before looking for the block in the l2Cache.  Blocks evicted  * from lruCache are put into the bucket cache.   * Metrics are the combined size and hits and misses of both caches.  *   */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CombinedBlockCache
implements|implements
name|ResizableBlockCache
implements|,
name|HeapSize
block|{
specifier|protected
specifier|final
name|LruBlockCache
name|lruCache
decl_stmt|;
specifier|protected
specifier|final
name|BlockCache
name|l2Cache
decl_stmt|;
specifier|protected
specifier|final
name|CombinedCacheStats
name|combinedCacheStats
decl_stmt|;
specifier|public
name|CombinedBlockCache
parameter_list|(
name|LruBlockCache
name|lruCache
parameter_list|,
name|BlockCache
name|l2Cache
parameter_list|)
block|{
name|this
operator|.
name|lruCache
operator|=
name|lruCache
expr_stmt|;
name|this
operator|.
name|l2Cache
operator|=
name|l2Cache
expr_stmt|;
name|this
operator|.
name|combinedCacheStats
operator|=
operator|new
name|CombinedCacheStats
argument_list|(
name|lruCache
operator|.
name|getStats
argument_list|()
argument_list|,
name|l2Cache
operator|.
name|getStats
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
name|long
name|l2size
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|l2Cache
operator|instanceof
name|HeapSize
condition|)
block|{
name|l2size
operator|=
operator|(
operator|(
name|HeapSize
operator|)
name|l2Cache
operator|)
operator|.
name|heapSize
argument_list|()
expr_stmt|;
block|}
return|return
name|lruCache
operator|.
name|heapSize
argument_list|()
operator|+
name|l2size
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
name|buf
parameter_list|,
name|boolean
name|inMemory
parameter_list|,
specifier|final
name|boolean
name|cacheDataInL1
parameter_list|)
block|{
name|boolean
name|metaBlock
init|=
name|buf
operator|.
name|getBlockType
argument_list|()
operator|.
name|getCategory
argument_list|()
operator|!=
name|BlockCategory
operator|.
name|DATA
decl_stmt|;
if|if
condition|(
name|metaBlock
operator|||
name|cacheDataInL1
condition|)
block|{
name|lruCache
operator|.
name|cacheBlock
argument_list|(
name|cacheKey
argument_list|,
name|buf
argument_list|,
name|inMemory
argument_list|,
name|cacheDataInL1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|l2Cache
operator|.
name|cacheBlock
argument_list|(
name|cacheKey
argument_list|,
name|buf
argument_list|,
name|inMemory
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
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
name|buf
parameter_list|)
block|{
name|cacheBlock
argument_list|(
name|cacheKey
argument_list|,
name|buf
argument_list|,
literal|false
argument_list|,
literal|false
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
comment|// TODO: is there a hole here, or just awkwardness since in the lruCache getBlock
comment|// we end up calling l2Cache.getBlock.
return|return
name|lruCache
operator|.
name|containsBlock
argument_list|(
name|cacheKey
argument_list|)
condition|?
name|lruCache
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
else|:
name|l2Cache
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
return|;
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
return|return
name|lruCache
operator|.
name|evictBlock
argument_list|(
name|cacheKey
argument_list|)
operator|||
name|l2Cache
operator|.
name|evictBlock
argument_list|(
name|cacheKey
argument_list|)
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
return|return
name|lruCache
operator|.
name|evictBlocksByHfileName
argument_list|(
name|hfileName
argument_list|)
operator|+
name|l2Cache
operator|.
name|evictBlocksByHfileName
argument_list|(
name|hfileName
argument_list|)
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
name|this
operator|.
name|combinedCacheStats
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
name|lruCache
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|l2Cache
operator|.
name|shutdown
argument_list|()
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
name|lruCache
operator|.
name|size
argument_list|()
operator|+
name|l2Cache
operator|.
name|size
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
name|lruCache
operator|.
name|getFreeSize
argument_list|()
operator|+
name|l2Cache
operator|.
name|getFreeSize
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
name|lruCache
operator|.
name|getCurrentSize
argument_list|()
operator|+
name|l2Cache
operator|.
name|getCurrentSize
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
name|lruCache
operator|.
name|getBlockCount
argument_list|()
operator|+
name|l2Cache
operator|.
name|getBlockCount
argument_list|()
return|;
block|}
specifier|public
specifier|static
class|class
name|CombinedCacheStats
extends|extends
name|CacheStats
block|{
specifier|private
specifier|final
name|CacheStats
name|lruCacheStats
decl_stmt|;
specifier|private
specifier|final
name|CacheStats
name|bucketCacheStats
decl_stmt|;
name|CombinedCacheStats
parameter_list|(
name|CacheStats
name|lbcStats
parameter_list|,
name|CacheStats
name|fcStats
parameter_list|)
block|{
name|super
argument_list|(
literal|"CombinedBlockCache"
argument_list|)
expr_stmt|;
name|this
operator|.
name|lruCacheStats
operator|=
name|lbcStats
expr_stmt|;
name|this
operator|.
name|bucketCacheStats
operator|=
name|fcStats
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getRequestCount
parameter_list|()
block|{
return|return
name|lruCacheStats
operator|.
name|getRequestCount
argument_list|()
operator|+
name|bucketCacheStats
operator|.
name|getRequestCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getRequestCachingCount
parameter_list|()
block|{
return|return
name|lruCacheStats
operator|.
name|getRequestCachingCount
argument_list|()
operator|+
name|bucketCacheStats
operator|.
name|getRequestCachingCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMissCount
parameter_list|()
block|{
return|return
name|lruCacheStats
operator|.
name|getMissCount
argument_list|()
operator|+
name|bucketCacheStats
operator|.
name|getMissCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getPrimaryMissCount
parameter_list|()
block|{
return|return
name|lruCacheStats
operator|.
name|getPrimaryMissCount
argument_list|()
operator|+
name|bucketCacheStats
operator|.
name|getPrimaryMissCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMissCachingCount
parameter_list|()
block|{
return|return
name|lruCacheStats
operator|.
name|getMissCachingCount
argument_list|()
operator|+
name|bucketCacheStats
operator|.
name|getMissCachingCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getHitCount
parameter_list|()
block|{
return|return
name|lruCacheStats
operator|.
name|getHitCount
argument_list|()
operator|+
name|bucketCacheStats
operator|.
name|getHitCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getPrimaryHitCount
parameter_list|()
block|{
return|return
name|lruCacheStats
operator|.
name|getPrimaryHitCount
argument_list|()
operator|+
name|bucketCacheStats
operator|.
name|getPrimaryHitCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getHitCachingCount
parameter_list|()
block|{
return|return
name|lruCacheStats
operator|.
name|getHitCachingCount
argument_list|()
operator|+
name|bucketCacheStats
operator|.
name|getHitCachingCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getEvictionCount
parameter_list|()
block|{
return|return
name|lruCacheStats
operator|.
name|getEvictionCount
argument_list|()
operator|+
name|bucketCacheStats
operator|.
name|getEvictionCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getEvictedCount
parameter_list|()
block|{
return|return
name|lruCacheStats
operator|.
name|getEvictedCount
argument_list|()
operator|+
name|bucketCacheStats
operator|.
name|getEvictedCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getPrimaryEvictedCount
parameter_list|()
block|{
return|return
name|lruCacheStats
operator|.
name|getPrimaryEvictedCount
argument_list|()
operator|+
name|bucketCacheStats
operator|.
name|getPrimaryEvictedCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|rollMetricsPeriod
parameter_list|()
block|{
name|lruCacheStats
operator|.
name|rollMetricsPeriod
argument_list|()
expr_stmt|;
name|bucketCacheStats
operator|.
name|rollMetricsPeriod
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getFailedInserts
parameter_list|()
block|{
return|return
name|lruCacheStats
operator|.
name|getFailedInserts
argument_list|()
operator|+
name|bucketCacheStats
operator|.
name|getFailedInserts
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getSumHitCountsPastNPeriods
parameter_list|()
block|{
return|return
name|lruCacheStats
operator|.
name|getSumHitCountsPastNPeriods
argument_list|()
operator|+
name|bucketCacheStats
operator|.
name|getSumHitCountsPastNPeriods
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getSumRequestCountsPastNPeriods
parameter_list|()
block|{
return|return
name|lruCacheStats
operator|.
name|getSumRequestCountsPastNPeriods
argument_list|()
operator|+
name|bucketCacheStats
operator|.
name|getSumRequestCountsPastNPeriods
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getSumHitCachingCountsPastNPeriods
parameter_list|()
block|{
return|return
name|lruCacheStats
operator|.
name|getSumHitCachingCountsPastNPeriods
argument_list|()
operator|+
name|bucketCacheStats
operator|.
name|getSumHitCachingCountsPastNPeriods
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getSumRequestCachingCountsPastNPeriods
parameter_list|()
block|{
return|return
name|lruCacheStats
operator|.
name|getSumRequestCachingCountsPastNPeriods
argument_list|()
operator|+
name|bucketCacheStats
operator|.
name|getSumRequestCachingCountsPastNPeriods
argument_list|()
return|;
block|}
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
return|return
operator|new
name|BlockCachesIterator
argument_list|(
name|getBlockCaches
argument_list|()
argument_list|)
return|;
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
operator|new
name|BlockCache
index|[]
block|{
name|this
operator|.
name|lruCache
block|,
name|this
operator|.
name|l2Cache
block|}
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
name|this
operator|.
name|lruCache
operator|.
name|setMaxSize
argument_list|(
name|size
argument_list|)
expr_stmt|;
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
comment|// A noop
name|this
operator|.
name|lruCache
operator|.
name|returnBlock
argument_list|(
name|cacheKey
argument_list|,
name|block
argument_list|)
expr_stmt|;
name|this
operator|.
name|l2Cache
operator|.
name|returnBlock
argument_list|(
name|cacheKey
argument_list|,
name|block
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|int
name|getRefCount
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|)
block|{
return|return
operator|(
operator|(
name|BucketCache
operator|)
name|this
operator|.
name|l2Cache
operator|)
operator|.
name|getRefCount
argument_list|(
name|cacheKey
argument_list|)
return|;
block|}
block|}
end_class

end_unit

