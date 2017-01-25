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
name|regionserver
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|MemoryType
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
name|ConcurrentSkipListMap
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
name|util
operator|.
name|MemorySizeUtil
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
name|Pair
import|;
end_import

begin_comment
comment|/**  * RegionServerAccounting keeps record of some basic real time information about  * the Region Server. Currently, it keeps record the global memstore size and global memstore heap  * overhead. It also tracks the replay edits per region.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RegionServerAccounting
block|{
comment|// memstore data size
specifier|private
specifier|final
name|AtomicLong
name|globalMemstoreDataSize
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// memstore heap over head size
specifier|private
specifier|final
name|AtomicLong
name|globalMemstoreHeapOverhead
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// Store the edits size during replaying WAL. Use this to roll back the
comment|// global memstore size once a region opening failed.
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|byte
index|[]
argument_list|,
name|MemstoreSize
argument_list|>
name|replayEditsPerRegion
init|=
operator|new
name|ConcurrentSkipListMap
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|long
name|globalMemStoreLimit
decl_stmt|;
specifier|private
specifier|final
name|float
name|globalMemStoreLimitLowMarkPercent
decl_stmt|;
specifier|private
name|long
name|globalMemStoreLimitLowMark
decl_stmt|;
specifier|private
specifier|final
name|MemoryType
name|memType
decl_stmt|;
specifier|private
name|long
name|globalOnHeapMemstoreLimit
decl_stmt|;
specifier|private
name|long
name|globalOnHeapMemstoreLimitLowMark
decl_stmt|;
specifier|public
name|RegionServerAccounting
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|Pair
argument_list|<
name|Long
argument_list|,
name|MemoryType
argument_list|>
name|globalMemstoreSizePair
init|=
name|MemorySizeUtil
operator|.
name|getGlobalMemstoreSize
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|this
operator|.
name|globalMemStoreLimit
operator|=
name|globalMemstoreSizePair
operator|.
name|getFirst
argument_list|()
expr_stmt|;
name|this
operator|.
name|memType
operator|=
name|globalMemstoreSizePair
operator|.
name|getSecond
argument_list|()
expr_stmt|;
name|this
operator|.
name|globalMemStoreLimitLowMarkPercent
operator|=
name|MemorySizeUtil
operator|.
name|getGlobalMemStoreHeapLowerMark
argument_list|(
name|conf
argument_list|,
name|this
operator|.
name|memType
operator|==
name|MemoryType
operator|.
name|HEAP
argument_list|)
expr_stmt|;
comment|// When off heap memstore in use we configure the global off heap space for memstore as bytes
comment|// not as % of max memory size. In such case, the lower water mark should be specified using the
comment|// key "hbase.regionserver.global.memstore.size.lower.limit" which says % of the global upper
comment|// bound and defaults to 95%. In on heap case also specifying this way is ideal. But in the past
comment|// we used to take lower bound also as the % of xmx (38% as default). For backward compatibility
comment|// for this deprecated config,we will fall back to read that config when new one is missing.
comment|// Only for on heap case, do this fallback mechanism. For off heap it makes no sense.
comment|// TODO When to get rid of the deprecated config? ie
comment|// "hbase.regionserver.global.memstore.lowerLimit". Can get rid of this boolean passing then.
name|this
operator|.
name|globalMemStoreLimitLowMark
operator|=
call|(
name|long
call|)
argument_list|(
name|this
operator|.
name|globalMemStoreLimit
operator|*
name|this
operator|.
name|globalMemStoreLimitLowMarkPercent
argument_list|)
expr_stmt|;
name|this
operator|.
name|globalOnHeapMemstoreLimit
operator|=
name|MemorySizeUtil
operator|.
name|getOnheapGlobalMemstoreSize
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|globalOnHeapMemstoreLimitLowMark
operator|=
call|(
name|long
call|)
argument_list|(
name|this
operator|.
name|globalOnHeapMemstoreLimit
operator|*
name|this
operator|.
name|globalMemStoreLimitLowMarkPercent
argument_list|)
expr_stmt|;
block|}
specifier|public
name|long
name|getGlobalMemstoreLimit
parameter_list|()
block|{
return|return
name|this
operator|.
name|globalMemStoreLimit
return|;
block|}
specifier|public
name|long
name|getOnheapGlobalMemstoreLimit
parameter_list|()
block|{
return|return
name|this
operator|.
name|globalOnHeapMemstoreLimit
return|;
block|}
comment|// Called by the tuners.
specifier|public
name|void
name|setGlobalMemstoreLimits
parameter_list|(
name|long
name|newGlobalMemstoreLimit
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|memType
operator|==
name|MemoryType
operator|.
name|HEAP
condition|)
block|{
name|this
operator|.
name|globalMemStoreLimit
operator|=
name|newGlobalMemstoreLimit
expr_stmt|;
name|this
operator|.
name|globalMemStoreLimitLowMark
operator|=
call|(
name|long
call|)
argument_list|(
name|this
operator|.
name|globalMemStoreLimit
operator|*
name|this
operator|.
name|globalMemStoreLimitLowMarkPercent
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|globalOnHeapMemstoreLimit
operator|=
name|newGlobalMemstoreLimit
expr_stmt|;
name|this
operator|.
name|globalOnHeapMemstoreLimitLowMark
operator|=
call|(
name|long
call|)
argument_list|(
name|this
operator|.
name|globalOnHeapMemstoreLimit
operator|*
name|this
operator|.
name|globalMemStoreLimitLowMarkPercent
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|boolean
name|isOffheap
parameter_list|()
block|{
return|return
name|this
operator|.
name|memType
operator|==
name|MemoryType
operator|.
name|NON_HEAP
return|;
block|}
specifier|public
name|long
name|getGlobalMemstoreLimitLowMark
parameter_list|()
block|{
return|return
name|this
operator|.
name|globalMemStoreLimitLowMark
return|;
block|}
specifier|public
name|float
name|getGlobalMemstoreLimitLowMarkPercent
parameter_list|()
block|{
return|return
name|this
operator|.
name|globalMemStoreLimitLowMarkPercent
return|;
block|}
comment|/**    * @return the global Memstore data size in the RegionServer    */
specifier|public
name|long
name|getGlobalMemstoreDataSize
parameter_list|()
block|{
return|return
name|globalMemstoreDataSize
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * @return the global memstore heap overhead size in the RegionServer    */
specifier|public
name|long
name|getGlobalMemstoreHeapOverhead
parameter_list|()
block|{
return|return
name|this
operator|.
name|globalMemstoreHeapOverhead
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * @return the global memstore data size and heap overhead size for an onheap memstore    * whereas return the heap overhead size for an offheap memstore    */
specifier|public
name|long
name|getGlobalMemstoreSize
parameter_list|()
block|{
if|if
condition|(
name|isOffheap
argument_list|()
condition|)
block|{
comment|// get only the heap overhead for offheap memstore
return|return
name|getGlobalMemstoreHeapOverhead
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|getGlobalMemstoreDataSize
argument_list|()
operator|+
name|getGlobalMemstoreHeapOverhead
argument_list|()
return|;
block|}
block|}
comment|/**    * @param memStoreSize the Memstore size will be added to     *        the global Memstore size     */
specifier|public
name|void
name|incGlobalMemstoreSize
parameter_list|(
name|MemstoreSize
name|memStoreSize
parameter_list|)
block|{
name|globalMemstoreDataSize
operator|.
name|addAndGet
argument_list|(
name|memStoreSize
operator|.
name|getDataSize
argument_list|()
argument_list|)
expr_stmt|;
name|globalMemstoreHeapOverhead
operator|.
name|addAndGet
argument_list|(
name|memStoreSize
operator|.
name|getHeapOverhead
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|decGlobalMemstoreSize
parameter_list|(
name|MemstoreSize
name|memStoreSize
parameter_list|)
block|{
name|globalMemstoreDataSize
operator|.
name|addAndGet
argument_list|(
operator|-
name|memStoreSize
operator|.
name|getDataSize
argument_list|()
argument_list|)
expr_stmt|;
name|globalMemstoreHeapOverhead
operator|.
name|addAndGet
argument_list|(
operator|-
name|memStoreSize
operator|.
name|getHeapOverhead
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Return true if we are above the memstore high water mark    * @return the flushtype    */
specifier|public
name|FlushType
name|isAboveHighWaterMark
parameter_list|()
block|{
comment|// for onheap memstore we check if the global memstore size and the
comment|// global heap overhead is greater than the global memstore limit
if|if
condition|(
name|memType
operator|==
name|MemoryType
operator|.
name|HEAP
condition|)
block|{
if|if
condition|(
name|getGlobalMemstoreDataSize
argument_list|()
operator|+
name|getGlobalMemstoreHeapOverhead
argument_list|()
operator|>=
name|globalMemStoreLimit
condition|)
block|{
return|return
name|FlushType
operator|.
name|ABOVE_ONHEAP_HIGHER_MARK
return|;
block|}
block|}
else|else
block|{
comment|// If the configured memstore is offheap, check for two things
comment|// 1) If the global memstore data size is greater than the configured
comment|// 'hbase.regionserver.offheap.global.memstore.size'
comment|// 2) If the global memstore heap size is greater than the configured onheap
comment|// global memstore limit 'hbase.regionserver.global.memstore.size'.
comment|// We do this to avoid OOME incase of scenarios where the heap is occupied with
comment|// lot of onheap references to the cells in memstore
if|if
condition|(
name|getGlobalMemstoreDataSize
argument_list|()
operator|>=
name|globalMemStoreLimit
condition|)
block|{
comment|// Indicates that global memstore size is above the configured
comment|// 'hbase.regionserver.offheap.global.memstore.size'
return|return
name|FlushType
operator|.
name|ABOVE_OFFHEAP_HIGHER_MARK
return|;
block|}
elseif|else
if|if
condition|(
name|getGlobalMemstoreHeapOverhead
argument_list|()
operator|>=
name|this
operator|.
name|globalOnHeapMemstoreLimit
condition|)
block|{
comment|// Indicates that the offheap memstore's heap overhead is greater than the
comment|// configured 'hbase.regionserver.global.memstore.size'.
return|return
name|FlushType
operator|.
name|ABOVE_ONHEAP_HIGHER_MARK
return|;
block|}
block|}
return|return
name|FlushType
operator|.
name|NORMAL
return|;
block|}
comment|/**    * Return true if we're above the low watermark    */
specifier|public
name|FlushType
name|isAboveLowWaterMark
parameter_list|()
block|{
comment|// for onheap memstore we check if the global memstore size and the
comment|// global heap overhead is greater than the global memstore lower mark limit
if|if
condition|(
name|memType
operator|==
name|MemoryType
operator|.
name|HEAP
condition|)
block|{
if|if
condition|(
name|getGlobalMemstoreDataSize
argument_list|()
operator|+
name|getGlobalMemstoreHeapOverhead
argument_list|()
operator|>=
name|globalMemStoreLimitLowMark
condition|)
block|{
return|return
name|FlushType
operator|.
name|ABOVE_ONHEAP_LOWER_MARK
return|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|getGlobalMemstoreDataSize
argument_list|()
operator|>=
name|globalMemStoreLimitLowMark
condition|)
block|{
comment|// Indicates that the offheap memstore's data size is greater than the global memstore
comment|// lower limit
return|return
name|FlushType
operator|.
name|ABOVE_OFFHEAP_LOWER_MARK
return|;
block|}
elseif|else
if|if
condition|(
name|getGlobalMemstoreHeapOverhead
argument_list|()
operator|>=
name|globalOnHeapMemstoreLimitLowMark
condition|)
block|{
comment|// Indicates that the offheap memstore's heap overhead is greater than the global memstore
comment|// onheap lower limit
return|return
name|FlushType
operator|.
name|ABOVE_ONHEAP_LOWER_MARK
return|;
block|}
block|}
return|return
name|FlushType
operator|.
name|NORMAL
return|;
block|}
comment|/**    * @return the flush pressure of all stores on this regionserver. The value should be greater than    *         or equal to 0.0, and any value greater than 1.0 means we enter the emergency state that    *         global memstore size already exceeds lower limit.    */
specifier|public
name|double
name|getFlushPressure
parameter_list|()
block|{
if|if
condition|(
name|memType
operator|==
name|MemoryType
operator|.
name|HEAP
condition|)
block|{
return|return
operator|(
name|getGlobalMemstoreDataSize
argument_list|()
operator|+
name|getGlobalMemstoreHeapOverhead
argument_list|()
operator|)
operator|*
literal|1.0
operator|/
name|globalMemStoreLimitLowMark
return|;
block|}
else|else
block|{
return|return
name|Math
operator|.
name|max
argument_list|(
name|getGlobalMemstoreDataSize
argument_list|()
operator|*
literal|1.0
operator|/
name|globalMemStoreLimitLowMark
argument_list|,
name|getGlobalMemstoreHeapOverhead
argument_list|()
operator|*
literal|1.0
operator|/
name|globalOnHeapMemstoreLimitLowMark
argument_list|)
return|;
block|}
block|}
comment|/***    * Add memStoreSize to replayEditsPerRegion.    *    * @param regionName region name.    * @param memStoreSize the Memstore size will be added to replayEditsPerRegion.    */
specifier|public
name|void
name|addRegionReplayEditsSize
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|,
name|MemstoreSize
name|memStoreSize
parameter_list|)
block|{
name|MemstoreSize
name|replayEdistsSize
init|=
name|replayEditsPerRegion
operator|.
name|get
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
comment|// All ops on the same MemstoreSize object is going to be done by single thread, sequentially
comment|// only. First calls to this method to increment the per region reply edits size and then call
comment|// to either rollbackRegionReplayEditsSize or clearRegionReplayEditsSize as per the result of
comment|// the region open operation. No need to handle multi thread issues on one region's entry in
comment|// this Map.
if|if
condition|(
name|replayEdistsSize
operator|==
literal|null
condition|)
block|{
name|replayEdistsSize
operator|=
operator|new
name|MemstoreSize
argument_list|()
expr_stmt|;
name|replayEditsPerRegion
operator|.
name|put
argument_list|(
name|regionName
argument_list|,
name|replayEdistsSize
argument_list|)
expr_stmt|;
block|}
name|replayEdistsSize
operator|.
name|incMemstoreSize
argument_list|(
name|memStoreSize
argument_list|)
expr_stmt|;
block|}
comment|/**    * Roll back the global MemStore size for a specified region when this region    * can't be opened.    *     * @param regionName the region which could not open.    */
specifier|public
name|void
name|rollbackRegionReplayEditsSize
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|)
block|{
name|MemstoreSize
name|replayEditsSize
init|=
name|replayEditsPerRegion
operator|.
name|get
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
if|if
condition|(
name|replayEditsSize
operator|!=
literal|null
condition|)
block|{
name|clearRegionReplayEditsSize
argument_list|(
name|regionName
argument_list|)
expr_stmt|;
name|decGlobalMemstoreSize
argument_list|(
name|replayEditsSize
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Clear a region from replayEditsPerRegion.    *     * @param regionName region name.    */
specifier|public
name|void
name|clearRegionReplayEditsSize
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|)
block|{
name|replayEditsPerRegion
operator|.
name|remove
argument_list|(
name|regionName
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

