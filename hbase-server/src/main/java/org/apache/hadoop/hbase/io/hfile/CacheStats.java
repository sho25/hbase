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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
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
name|LongAdder
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
name|metrics
operator|.
name|impl
operator|.
name|FastLongHistogram
import|;
end_import

begin_comment
comment|/**  * Class that implements cache metrics.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CacheStats
block|{
comment|/** Sliding window statistics. The number of metric periods to include in    * sliding window hit ratio calculations.    */
specifier|static
specifier|final
name|int
name|DEFAULT_WINDOW_PERIODS
init|=
literal|5
decl_stmt|;
comment|/** The number of getBlock requests that were cache hits */
specifier|private
specifier|final
name|LongAdder
name|hitCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
comment|/** The number of getBlock requests that were cache hits from primary replica */
specifier|private
specifier|final
name|LongAdder
name|primaryHitCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
comment|/**    * The number of getBlock requests that were cache hits, but only from    * requests that were set to use the block cache.  This is because all reads    * attempt to read from the block cache even if they will not put new blocks    * into the block cache.  See HBASE-2253 for more information.    */
specifier|private
specifier|final
name|LongAdder
name|hitCachingCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
comment|/** The number of getBlock requests that were cache misses */
specifier|private
specifier|final
name|LongAdder
name|missCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
comment|/** The number of getBlock requests for primary replica that were cache misses */
specifier|private
specifier|final
name|LongAdder
name|primaryMissCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
comment|/**    * The number of getBlock requests that were cache misses, but only from    * requests that were set to use the block cache.    */
specifier|private
specifier|final
name|LongAdder
name|missCachingCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
comment|/** The number of times an eviction has occurred */
specifier|private
specifier|final
name|LongAdder
name|evictionCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
comment|/** The total number of blocks that have been evicted */
specifier|private
specifier|final
name|LongAdder
name|evictedBlockCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
comment|/** The total number of blocks for primary replica that have been evicted */
specifier|private
specifier|final
name|LongAdder
name|primaryEvictedBlockCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
comment|/** The total number of blocks that were not inserted. */
specifier|private
specifier|final
name|AtomicLong
name|failedInserts
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|/** Per Block Type Counts */
specifier|private
specifier|final
name|LongAdder
name|dataMissCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongAdder
name|leafIndexMissCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongAdder
name|bloomChunkMissCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongAdder
name|metaMissCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongAdder
name|rootIndexMissCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongAdder
name|intermediateIndexMissCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongAdder
name|fileInfoMissCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongAdder
name|generalBloomMetaMissCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongAdder
name|deleteFamilyBloomMissCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongAdder
name|trailerMissCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongAdder
name|dataHitCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongAdder
name|leafIndexHitCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongAdder
name|bloomChunkHitCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongAdder
name|metaHitCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongAdder
name|rootIndexHitCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongAdder
name|intermediateIndexHitCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongAdder
name|fileInfoHitCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongAdder
name|generalBloomMetaHitCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongAdder
name|deleteFamilyBloomHitCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongAdder
name|trailerHitCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
comment|/** The number of metrics periods to include in window */
specifier|private
specifier|final
name|int
name|numPeriodsInWindow
decl_stmt|;
comment|/** Hit counts for each period in window */
specifier|private
specifier|final
name|long
index|[]
name|hitCounts
decl_stmt|;
comment|/** Caching hit counts for each period in window */
specifier|private
specifier|final
name|long
index|[]
name|hitCachingCounts
decl_stmt|;
comment|/** Access counts for each period in window */
specifier|private
specifier|final
name|long
index|[]
name|requestCounts
decl_stmt|;
comment|/** Caching access counts for each period in window */
specifier|private
specifier|final
name|long
index|[]
name|requestCachingCounts
decl_stmt|;
comment|/** Last hit count read */
specifier|private
name|long
name|lastHitCount
init|=
literal|0
decl_stmt|;
comment|/** Last hit caching count read */
specifier|private
name|long
name|lastHitCachingCount
init|=
literal|0
decl_stmt|;
comment|/** Last request count read */
specifier|private
name|long
name|lastRequestCount
init|=
literal|0
decl_stmt|;
comment|/** Last request caching count read */
specifier|private
name|long
name|lastRequestCachingCount
init|=
literal|0
decl_stmt|;
comment|/** Current window index (next to be updated) */
specifier|private
name|int
name|windowIndex
init|=
literal|0
decl_stmt|;
comment|/**    * Keep running age at eviction time    */
specifier|private
name|FastLongHistogram
name|ageAtEviction
decl_stmt|;
specifier|private
name|long
name|startTime
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
specifier|public
name|CacheStats
parameter_list|(
specifier|final
name|String
name|name
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|DEFAULT_WINDOW_PERIODS
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CacheStats
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
name|int
name|numPeriodsInWindow
parameter_list|)
block|{
name|this
operator|.
name|numPeriodsInWindow
operator|=
name|numPeriodsInWindow
expr_stmt|;
name|this
operator|.
name|hitCounts
operator|=
operator|new
name|long
index|[
name|numPeriodsInWindow
index|]
expr_stmt|;
name|this
operator|.
name|hitCachingCounts
operator|=
operator|new
name|long
index|[
name|numPeriodsInWindow
index|]
expr_stmt|;
name|this
operator|.
name|requestCounts
operator|=
operator|new
name|long
index|[
name|numPeriodsInWindow
index|]
expr_stmt|;
name|this
operator|.
name|requestCachingCounts
operator|=
operator|new
name|long
index|[
name|numPeriodsInWindow
index|]
expr_stmt|;
name|this
operator|.
name|ageAtEviction
operator|=
operator|new
name|FastLongHistogram
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|AgeSnapshot
name|snapshot
init|=
name|getAgeAtEvictionSnapshot
argument_list|()
decl_stmt|;
return|return
literal|"hitCount="
operator|+
name|getHitCount
argument_list|()
operator|+
literal|", hitCachingCount="
operator|+
name|getHitCachingCount
argument_list|()
operator|+
literal|", missCount="
operator|+
name|getMissCount
argument_list|()
operator|+
literal|", missCachingCount="
operator|+
name|getMissCachingCount
argument_list|()
operator|+
literal|", evictionCount="
operator|+
name|getEvictionCount
argument_list|()
operator|+
literal|", evictedBlockCount="
operator|+
name|getEvictedCount
argument_list|()
operator|+
literal|", primaryMissCount="
operator|+
name|getPrimaryMissCount
argument_list|()
operator|+
literal|", primaryHitCount="
operator|+
name|getPrimaryHitCount
argument_list|()
operator|+
literal|", evictedAgeMean="
operator|+
name|snapshot
operator|.
name|getMean
argument_list|()
return|;
block|}
specifier|public
name|void
name|miss
parameter_list|(
name|boolean
name|caching
parameter_list|,
name|boolean
name|primary
parameter_list|,
name|BlockType
name|type
parameter_list|)
block|{
name|missCount
operator|.
name|increment
argument_list|()
expr_stmt|;
if|if
condition|(
name|primary
condition|)
name|primaryMissCount
operator|.
name|increment
argument_list|()
expr_stmt|;
if|if
condition|(
name|caching
condition|)
name|missCachingCount
operator|.
name|increment
argument_list|()
expr_stmt|;
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
return|return;
block|}
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|DATA
case|:
case|case
name|ENCODED_DATA
case|:
name|dataMissCount
operator|.
name|increment
argument_list|()
expr_stmt|;
break|break;
case|case
name|LEAF_INDEX
case|:
name|leafIndexMissCount
operator|.
name|increment
argument_list|()
expr_stmt|;
break|break;
case|case
name|BLOOM_CHUNK
case|:
name|bloomChunkMissCount
operator|.
name|increment
argument_list|()
expr_stmt|;
break|break;
case|case
name|META
case|:
name|metaMissCount
operator|.
name|increment
argument_list|()
expr_stmt|;
break|break;
case|case
name|INTERMEDIATE_INDEX
case|:
name|intermediateIndexMissCount
operator|.
name|increment
argument_list|()
expr_stmt|;
break|break;
case|case
name|ROOT_INDEX
case|:
name|rootIndexMissCount
operator|.
name|increment
argument_list|()
expr_stmt|;
break|break;
case|case
name|FILE_INFO
case|:
name|fileInfoMissCount
operator|.
name|increment
argument_list|()
expr_stmt|;
break|break;
case|case
name|GENERAL_BLOOM_META
case|:
name|generalBloomMetaMissCount
operator|.
name|increment
argument_list|()
expr_stmt|;
break|break;
case|case
name|DELETE_FAMILY_BLOOM_META
case|:
name|deleteFamilyBloomMissCount
operator|.
name|increment
argument_list|()
expr_stmt|;
break|break;
case|case
name|TRAILER
case|:
name|trailerMissCount
operator|.
name|increment
argument_list|()
expr_stmt|;
break|break;
default|default:
comment|// If there's a new type that's fine
comment|// Ignore it for now. This is metrics don't exception.
break|break;
block|}
block|}
specifier|public
name|void
name|hit
parameter_list|(
name|boolean
name|caching
parameter_list|,
name|boolean
name|primary
parameter_list|,
name|BlockType
name|type
parameter_list|)
block|{
name|hitCount
operator|.
name|increment
argument_list|()
expr_stmt|;
if|if
condition|(
name|primary
condition|)
name|primaryHitCount
operator|.
name|increment
argument_list|()
expr_stmt|;
if|if
condition|(
name|caching
condition|)
name|hitCachingCount
operator|.
name|increment
argument_list|()
expr_stmt|;
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
return|return;
block|}
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|DATA
case|:
case|case
name|ENCODED_DATA
case|:
name|dataHitCount
operator|.
name|increment
argument_list|()
expr_stmt|;
break|break;
case|case
name|LEAF_INDEX
case|:
name|leafIndexHitCount
operator|.
name|increment
argument_list|()
expr_stmt|;
break|break;
case|case
name|BLOOM_CHUNK
case|:
name|bloomChunkHitCount
operator|.
name|increment
argument_list|()
expr_stmt|;
break|break;
case|case
name|META
case|:
name|metaHitCount
operator|.
name|increment
argument_list|()
expr_stmt|;
break|break;
case|case
name|INTERMEDIATE_INDEX
case|:
name|intermediateIndexHitCount
operator|.
name|increment
argument_list|()
expr_stmt|;
break|break;
case|case
name|ROOT_INDEX
case|:
name|rootIndexHitCount
operator|.
name|increment
argument_list|()
expr_stmt|;
break|break;
case|case
name|FILE_INFO
case|:
name|fileInfoHitCount
operator|.
name|increment
argument_list|()
expr_stmt|;
break|break;
case|case
name|GENERAL_BLOOM_META
case|:
name|generalBloomMetaHitCount
operator|.
name|increment
argument_list|()
expr_stmt|;
break|break;
case|case
name|DELETE_FAMILY_BLOOM_META
case|:
name|deleteFamilyBloomHitCount
operator|.
name|increment
argument_list|()
expr_stmt|;
break|break;
case|case
name|TRAILER
case|:
name|trailerHitCount
operator|.
name|increment
argument_list|()
expr_stmt|;
break|break;
default|default:
comment|// If there's a new type that's fine
comment|// Ignore it for now. This is metrics don't exception.
break|break;
block|}
block|}
specifier|public
name|void
name|evict
parameter_list|()
block|{
name|evictionCount
operator|.
name|increment
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|evicted
parameter_list|(
specifier|final
name|long
name|t
parameter_list|,
name|boolean
name|primary
parameter_list|)
block|{
if|if
condition|(
name|t
operator|>
name|this
operator|.
name|startTime
condition|)
block|{
name|this
operator|.
name|ageAtEviction
operator|.
name|add
argument_list|(
operator|(
name|t
operator|-
name|this
operator|.
name|startTime
operator|)
operator|/
name|BlockCacheUtil
operator|.
name|NANOS_PER_SECOND
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|evictedBlockCount
operator|.
name|increment
argument_list|()
expr_stmt|;
if|if
condition|(
name|primary
condition|)
block|{
name|primaryEvictedBlockCount
operator|.
name|increment
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|long
name|failInsert
parameter_list|()
block|{
return|return
name|failedInserts
operator|.
name|incrementAndGet
argument_list|()
return|;
block|}
comment|// All of the counts of misses and hits.
specifier|public
name|long
name|getDataMissCount
parameter_list|()
block|{
return|return
name|dataMissCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getLeafIndexMissCount
parameter_list|()
block|{
return|return
name|leafIndexMissCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getBloomChunkMissCount
parameter_list|()
block|{
return|return
name|bloomChunkMissCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getMetaMissCount
parameter_list|()
block|{
return|return
name|metaMissCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getRootIndexMissCount
parameter_list|()
block|{
return|return
name|rootIndexMissCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getIntermediateIndexMissCount
parameter_list|()
block|{
return|return
name|intermediateIndexMissCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getFileInfoMissCount
parameter_list|()
block|{
return|return
name|fileInfoMissCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getGeneralBloomMetaMissCount
parameter_list|()
block|{
return|return
name|generalBloomMetaMissCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getDeleteFamilyBloomMissCount
parameter_list|()
block|{
return|return
name|deleteFamilyBloomMissCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getTrailerMissCount
parameter_list|()
block|{
return|return
name|trailerMissCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getDataHitCount
parameter_list|()
block|{
return|return
name|dataHitCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getLeafIndexHitCount
parameter_list|()
block|{
return|return
name|leafIndexHitCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getBloomChunkHitCount
parameter_list|()
block|{
return|return
name|bloomChunkHitCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getMetaHitCount
parameter_list|()
block|{
return|return
name|metaHitCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getRootIndexHitCount
parameter_list|()
block|{
return|return
name|rootIndexHitCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getIntermediateIndexHitCount
parameter_list|()
block|{
return|return
name|intermediateIndexHitCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getFileInfoHitCount
parameter_list|()
block|{
return|return
name|fileInfoHitCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getGeneralBloomMetaHitCount
parameter_list|()
block|{
return|return
name|generalBloomMetaHitCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getDeleteFamilyBloomHitCount
parameter_list|()
block|{
return|return
name|deleteFamilyBloomHitCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getTrailerHitCount
parameter_list|()
block|{
return|return
name|trailerHitCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getRequestCount
parameter_list|()
block|{
return|return
name|getHitCount
argument_list|()
operator|+
name|getMissCount
argument_list|()
return|;
block|}
specifier|public
name|long
name|getRequestCachingCount
parameter_list|()
block|{
return|return
name|getHitCachingCount
argument_list|()
operator|+
name|getMissCachingCount
argument_list|()
return|;
block|}
specifier|public
name|long
name|getMissCount
parameter_list|()
block|{
return|return
name|missCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getPrimaryMissCount
parameter_list|()
block|{
return|return
name|primaryMissCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getMissCachingCount
parameter_list|()
block|{
return|return
name|missCachingCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getHitCount
parameter_list|()
block|{
return|return
name|hitCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getPrimaryHitCount
parameter_list|()
block|{
return|return
name|primaryHitCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getHitCachingCount
parameter_list|()
block|{
return|return
name|hitCachingCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getEvictionCount
parameter_list|()
block|{
return|return
name|evictionCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getEvictedCount
parameter_list|()
block|{
return|return
name|this
operator|.
name|evictedBlockCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|long
name|getPrimaryEvictedCount
parameter_list|()
block|{
return|return
name|primaryEvictedBlockCount
operator|.
name|sum
argument_list|()
return|;
block|}
specifier|public
name|double
name|getHitRatio
parameter_list|()
block|{
return|return
operator|(
operator|(
name|double
operator|)
name|getHitCount
argument_list|()
operator|/
operator|(
name|double
operator|)
name|getRequestCount
argument_list|()
operator|)
return|;
block|}
specifier|public
name|double
name|getHitCachingRatio
parameter_list|()
block|{
return|return
operator|(
operator|(
name|double
operator|)
name|getHitCachingCount
argument_list|()
operator|/
operator|(
name|double
operator|)
name|getRequestCachingCount
argument_list|()
operator|)
return|;
block|}
specifier|public
name|double
name|getMissRatio
parameter_list|()
block|{
return|return
operator|(
operator|(
name|double
operator|)
name|getMissCount
argument_list|()
operator|/
operator|(
name|double
operator|)
name|getRequestCount
argument_list|()
operator|)
return|;
block|}
specifier|public
name|double
name|getMissCachingRatio
parameter_list|()
block|{
return|return
operator|(
operator|(
name|double
operator|)
name|getMissCachingCount
argument_list|()
operator|/
operator|(
name|double
operator|)
name|getRequestCachingCount
argument_list|()
operator|)
return|;
block|}
specifier|public
name|double
name|evictedPerEviction
parameter_list|()
block|{
return|return
operator|(
operator|(
name|double
operator|)
name|getEvictedCount
argument_list|()
operator|/
operator|(
name|double
operator|)
name|getEvictionCount
argument_list|()
operator|)
return|;
block|}
specifier|public
name|long
name|getFailedInserts
parameter_list|()
block|{
return|return
name|failedInserts
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|void
name|rollMetricsPeriod
parameter_list|()
block|{
name|hitCounts
index|[
name|windowIndex
index|]
operator|=
name|getHitCount
argument_list|()
operator|-
name|lastHitCount
expr_stmt|;
name|lastHitCount
operator|=
name|getHitCount
argument_list|()
expr_stmt|;
name|hitCachingCounts
index|[
name|windowIndex
index|]
operator|=
name|getHitCachingCount
argument_list|()
operator|-
name|lastHitCachingCount
expr_stmt|;
name|lastHitCachingCount
operator|=
name|getHitCachingCount
argument_list|()
expr_stmt|;
name|requestCounts
index|[
name|windowIndex
index|]
operator|=
name|getRequestCount
argument_list|()
operator|-
name|lastRequestCount
expr_stmt|;
name|lastRequestCount
operator|=
name|getRequestCount
argument_list|()
expr_stmt|;
name|requestCachingCounts
index|[
name|windowIndex
index|]
operator|=
name|getRequestCachingCount
argument_list|()
operator|-
name|lastRequestCachingCount
expr_stmt|;
name|lastRequestCachingCount
operator|=
name|getRequestCachingCount
argument_list|()
expr_stmt|;
name|windowIndex
operator|=
operator|(
name|windowIndex
operator|+
literal|1
operator|)
operator|%
name|numPeriodsInWindow
expr_stmt|;
block|}
specifier|public
name|long
name|getSumHitCountsPastNPeriods
parameter_list|()
block|{
return|return
name|sum
argument_list|(
name|hitCounts
argument_list|)
return|;
block|}
specifier|public
name|long
name|getSumRequestCountsPastNPeriods
parameter_list|()
block|{
return|return
name|sum
argument_list|(
name|requestCounts
argument_list|)
return|;
block|}
specifier|public
name|long
name|getSumHitCachingCountsPastNPeriods
parameter_list|()
block|{
return|return
name|sum
argument_list|(
name|hitCachingCounts
argument_list|)
return|;
block|}
specifier|public
name|long
name|getSumRequestCachingCountsPastNPeriods
parameter_list|()
block|{
return|return
name|sum
argument_list|(
name|requestCachingCounts
argument_list|)
return|;
block|}
specifier|public
name|double
name|getHitRatioPastNPeriods
parameter_list|()
block|{
name|double
name|ratio
init|=
operator|(
operator|(
name|double
operator|)
name|getSumHitCountsPastNPeriods
argument_list|()
operator|/
operator|(
name|double
operator|)
name|getSumRequestCountsPastNPeriods
argument_list|()
operator|)
decl_stmt|;
return|return
name|Double
operator|.
name|isNaN
argument_list|(
name|ratio
argument_list|)
condition|?
literal|0
else|:
name|ratio
return|;
block|}
specifier|public
name|double
name|getHitCachingRatioPastNPeriods
parameter_list|()
block|{
name|double
name|ratio
init|=
operator|(
operator|(
name|double
operator|)
name|getSumHitCachingCountsPastNPeriods
argument_list|()
operator|/
operator|(
name|double
operator|)
name|getSumRequestCachingCountsPastNPeriods
argument_list|()
operator|)
decl_stmt|;
return|return
name|Double
operator|.
name|isNaN
argument_list|(
name|ratio
argument_list|)
condition|?
literal|0
else|:
name|ratio
return|;
block|}
specifier|public
name|AgeSnapshot
name|getAgeAtEvictionSnapshot
parameter_list|()
block|{
return|return
operator|new
name|AgeSnapshot
argument_list|(
name|this
operator|.
name|ageAtEviction
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|long
name|sum
parameter_list|(
name|long
index|[]
name|counts
parameter_list|)
block|{
return|return
name|Arrays
operator|.
name|stream
argument_list|(
name|counts
argument_list|)
operator|.
name|sum
argument_list|()
return|;
block|}
block|}
end_class

end_unit

