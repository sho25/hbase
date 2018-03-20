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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|exceptions
operator|.
name|IllegalArgumentIOException
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
name|ThreadPoolExecutor
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
name|AtomicBoolean
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
name|CellComparator
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
name|MemoryCompactionPolicy
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
name|hbase
operator|.
name|util
operator|.
name|EnvironmentEdgeManager
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
name|wal
operator|.
name|WAL
import|;
end_import

begin_comment
comment|/**  * A memstore implementation which supports in-memory compaction.  * A compaction pipeline is added between the active set and the snapshot data structures;  * it consists of a list of kv-sets that are subject to compaction.  * Like the snapshot, all pipeline components are read-only; updates only affect the active set.  * To ensure this property we take advantage of the existing blocking mechanism -- the active set  * is pushed to the pipeline while holding the region's updatesLock in exclusive mode.  * Periodically, a compaction is applied in the background to all pipeline components resulting  * in a single read-only component. The ``old'' components are discarded when no scanner is reading  * them.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CompactingMemStore
extends|extends
name|AbstractMemStore
block|{
comment|// The external setting of the compacting MemStore behaviour
specifier|public
specifier|static
specifier|final
name|String
name|COMPACTING_MEMSTORE_TYPE_KEY
init|=
literal|"hbase.hregion.compacting.memstore.type"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|COMPACTING_MEMSTORE_TYPE_DEFAULT
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|MemoryCompactionPolicy
operator|.
name|BASIC
argument_list|)
decl_stmt|;
comment|// Default fraction of in-memory-flush size w.r.t. flush-to-disk size
specifier|public
specifier|static
specifier|final
name|String
name|IN_MEMORY_FLUSH_THRESHOLD_FACTOR_KEY
init|=
literal|"hbase.memstore.inmemoryflush.threshold.factor"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|double
name|IN_MEMORY_FLUSH_THRESHOLD_FACTOR_DEFAULT
init|=
literal|0.1
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
name|CompactingMemStore
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|HStore
name|store
decl_stmt|;
specifier|private
name|RegionServicesForStores
name|regionServices
decl_stmt|;
specifier|private
name|CompactionPipeline
name|pipeline
decl_stmt|;
specifier|protected
name|MemStoreCompactor
name|compactor
decl_stmt|;
specifier|private
name|long
name|inmemoryFlushSize
decl_stmt|;
comment|// the threshold on active size for in-memory flush
specifier|private
specifier|final
name|AtomicBoolean
name|inMemoryFlushInProgress
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
comment|// inWalReplay is true while we are synchronously replaying the edits from WAL
specifier|private
name|boolean
name|inWalReplay
init|=
literal|false
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|protected
specifier|final
name|AtomicBoolean
name|allowCompaction
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|compositeSnapshot
init|=
literal|true
decl_stmt|;
comment|/**    * Types of indexes (part of immutable segments) to be used after flattening,    * compaction, or merge are applied.    */
specifier|public
enum|enum
name|IndexType
block|{
name|CSLM_MAP
block|,
comment|// ConcurrentSkipLisMap
name|ARRAY_MAP
block|,
comment|// CellArrayMap
name|CHUNK_MAP
comment|// CellChunkMap
block|}
specifier|private
name|IndexType
name|indexType
init|=
name|IndexType
operator|.
name|ARRAY_MAP
decl_stmt|;
comment|// default implementation
specifier|public
specifier|static
specifier|final
name|long
name|DEEP_OVERHEAD
init|=
name|ClassSize
operator|.
name|align
argument_list|(
name|AbstractMemStore
operator|.
name|DEEP_OVERHEAD
operator|+
literal|7
operator|*
name|ClassSize
operator|.
name|REFERENCE
comment|// Store, RegionServicesForStores, CompactionPipeline,
comment|// MemStoreCompactor, inMemoryFlushInProgress, allowCompaction,
comment|// indexType
operator|+
name|Bytes
operator|.
name|SIZEOF_LONG
comment|// inmemoryFlushSize
operator|+
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_BOOLEAN
comment|// compositeSnapshot and inWalReplay
operator|+
literal|2
operator|*
name|ClassSize
operator|.
name|ATOMIC_BOOLEAN
comment|// inMemoryFlushInProgress and allowCompaction
operator|+
name|CompactionPipeline
operator|.
name|DEEP_OVERHEAD
operator|+
name|MemStoreCompactor
operator|.
name|DEEP_OVERHEAD
argument_list|)
decl_stmt|;
specifier|public
name|CompactingMemStore
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|CellComparator
name|c
parameter_list|,
name|HStore
name|store
parameter_list|,
name|RegionServicesForStores
name|regionServices
parameter_list|,
name|MemoryCompactionPolicy
name|compactionPolicy
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|c
argument_list|)
expr_stmt|;
name|this
operator|.
name|store
operator|=
name|store
expr_stmt|;
name|this
operator|.
name|regionServices
operator|=
name|regionServices
expr_stmt|;
name|this
operator|.
name|pipeline
operator|=
operator|new
name|CompactionPipeline
argument_list|(
name|getRegionServices
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|compactor
operator|=
name|createMemStoreCompactor
argument_list|(
name|compactionPolicy
argument_list|)
expr_stmt|;
if|if
condition|(
name|conf
operator|.
name|getBoolean
argument_list|(
name|MemStoreLAB
operator|.
name|USEMSLAB_KEY
argument_list|,
name|MemStoreLAB
operator|.
name|USEMSLAB_DEFAULT
argument_list|)
condition|)
block|{
comment|// if user requested to work with MSLABs (whether on- or off-heap), then the
comment|// immutable segments are going to use CellChunkMap as their index
name|indexType
operator|=
name|IndexType
operator|.
name|CHUNK_MAP
expr_stmt|;
block|}
else|else
block|{
name|indexType
operator|=
name|IndexType
operator|.
name|ARRAY_MAP
expr_stmt|;
block|}
comment|// initialization of the flush size should happen after initialization of the index type
comment|// so do not transfer the following method
name|initInmemoryFlushSize
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|protected
name|MemStoreCompactor
name|createMemStoreCompactor
parameter_list|(
name|MemoryCompactionPolicy
name|compactionPolicy
parameter_list|)
throws|throws
name|IllegalArgumentIOException
block|{
return|return
operator|new
name|MemStoreCompactor
argument_list|(
name|this
argument_list|,
name|compactionPolicy
argument_list|)
return|;
block|}
specifier|private
name|void
name|initInmemoryFlushSize
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|double
name|factor
init|=
literal|0
decl_stmt|;
name|long
name|memstoreFlushSize
init|=
name|getRegionServices
argument_list|()
operator|.
name|getMemStoreFlushSize
argument_list|()
decl_stmt|;
name|int
name|numStores
init|=
name|getRegionServices
argument_list|()
operator|.
name|getNumStores
argument_list|()
decl_stmt|;
if|if
condition|(
name|numStores
operator|<=
literal|1
condition|)
block|{
comment|// Family number might also be zero in some of our unit test case
name|numStores
operator|=
literal|1
expr_stmt|;
block|}
name|inmemoryFlushSize
operator|=
name|memstoreFlushSize
operator|/
name|numStores
expr_stmt|;
comment|// multiply by a factor (the same factor for all index types)
name|factor
operator|=
name|conf
operator|.
name|getDouble
argument_list|(
name|IN_MEMORY_FLUSH_THRESHOLD_FACTOR_KEY
argument_list|,
name|IN_MEMORY_FLUSH_THRESHOLD_FACTOR_DEFAULT
argument_list|)
expr_stmt|;
name|inmemoryFlushSize
operator|=
call|(
name|long
call|)
argument_list|(
name|inmemoryFlushSize
operator|*
name|factor
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting in-memory flush size threshold to {} and immutable segments index to type={}"
argument_list|,
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|inmemoryFlushSize
argument_list|)
argument_list|,
name|indexType
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return Total memory occupied by this MemStore. This won't include any size occupied by the    *         snapshot. We assume the snapshot will get cleared soon. This is not thread safe and    *         the memstore may be changed while computing its size. It is the responsibility of the    *         caller to make sure this doesn't happen.    */
annotation|@
name|Override
specifier|public
name|MemStoreSize
name|size
parameter_list|()
block|{
name|MemStoreSizing
name|memstoreSizing
init|=
operator|new
name|MemStoreSizing
argument_list|()
decl_stmt|;
name|memstoreSizing
operator|.
name|incMemStoreSize
argument_list|(
name|active
operator|.
name|getMemStoreSize
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Segment
name|item
range|:
name|pipeline
operator|.
name|getSegments
argument_list|()
control|)
block|{
name|memstoreSizing
operator|.
name|incMemStoreSize
argument_list|(
name|item
operator|.
name|getMemStoreSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|memstoreSizing
return|;
block|}
comment|/**    * This method is called before the flush is executed.    * @return an estimation (lower bound) of the unflushed sequence id in memstore after the flush    * is executed. if memstore will be cleared returns {@code HConstants.NO_SEQNUM}.    */
annotation|@
name|Override
specifier|public
name|long
name|preFlushSeqIDEstimation
parameter_list|()
block|{
if|if
condition|(
name|compositeSnapshot
condition|)
block|{
return|return
name|HConstants
operator|.
name|NO_SEQNUM
return|;
block|}
name|Segment
name|segment
init|=
name|getLastSegment
argument_list|()
decl_stmt|;
if|if
condition|(
name|segment
operator|==
literal|null
condition|)
block|{
return|return
name|HConstants
operator|.
name|NO_SEQNUM
return|;
block|}
return|return
name|segment
operator|.
name|getMinSequenceId
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSloppy
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
comment|/**    * Push the current active memstore segment into the pipeline    * and create a snapshot of the tail of current compaction pipeline    * Snapshot must be cleared by call to {@link #clearSnapshot}.    * {@link #clearSnapshot(long)}.    * @return {@link MemStoreSnapshot}    */
annotation|@
name|Override
specifier|public
name|MemStoreSnapshot
name|snapshot
parameter_list|()
block|{
comment|// If snapshot currently has entries, then flusher failed or didn't call
comment|// cleanup.  Log a warning.
if|if
condition|(
operator|!
name|this
operator|.
name|snapshot
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Snapshot called again without clearing previous. "
operator|+
literal|"Doing nothing. Another ongoing flush or did we fail last attempt?"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"FLUSHING TO DISK {}, store={}"
argument_list|,
name|getRegionServices
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|getFamilyName
argument_list|()
argument_list|)
expr_stmt|;
name|stopCompaction
argument_list|()
expr_stmt|;
name|pushActiveToPipeline
argument_list|(
name|this
operator|.
name|active
argument_list|)
expr_stmt|;
name|snapshotId
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
expr_stmt|;
comment|// in both cases whatever is pushed to snapshot is cleared from the pipeline
if|if
condition|(
name|compositeSnapshot
condition|)
block|{
name|pushPipelineToSnapshot
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|pushTailToSnapshot
argument_list|()
expr_stmt|;
block|}
name|compactor
operator|.
name|resetStats
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|MemStoreSnapshot
argument_list|(
name|snapshotId
argument_list|,
name|this
operator|.
name|snapshot
argument_list|)
return|;
block|}
comment|/**    * On flush, how much memory we will clear.    * @return size of data that is going to be flushed    */
annotation|@
name|Override
specifier|public
name|MemStoreSize
name|getFlushableSize
parameter_list|()
block|{
name|MemStoreSizing
name|snapshotSizing
init|=
name|getSnapshotSizing
argument_list|()
decl_stmt|;
if|if
condition|(
name|snapshotSizing
operator|.
name|getDataSize
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// if snapshot is empty the tail of the pipeline (or everything in the memstore) is flushed
if|if
condition|(
name|compositeSnapshot
condition|)
block|{
name|snapshotSizing
operator|=
name|pipeline
operator|.
name|getPipelineSizing
argument_list|()
expr_stmt|;
name|snapshotSizing
operator|.
name|incMemStoreSize
argument_list|(
name|active
operator|.
name|getMemStoreSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|snapshotSizing
operator|=
name|pipeline
operator|.
name|getTailSizing
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|snapshotSizing
operator|.
name|getDataSize
argument_list|()
operator|>
literal|0
condition|?
name|snapshotSizing
else|:
operator|new
name|MemStoreSize
argument_list|(
name|active
operator|.
name|getMemStoreSize
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|long
name|keySize
parameter_list|()
block|{
comment|// Need to consider keySize of all segments in pipeline and active
name|long
name|k
init|=
name|this
operator|.
name|active
operator|.
name|keySize
argument_list|()
decl_stmt|;
for|for
control|(
name|Segment
name|segment
range|:
name|this
operator|.
name|pipeline
operator|.
name|getSegments
argument_list|()
control|)
block|{
name|k
operator|+=
name|segment
operator|.
name|keySize
argument_list|()
expr_stmt|;
block|}
return|return
name|k
return|;
block|}
annotation|@
name|Override
specifier|protected
name|long
name|heapSize
parameter_list|()
block|{
comment|// Need to consider heapOverhead of all segments in pipeline and active
name|long
name|h
init|=
name|this
operator|.
name|active
operator|.
name|heapSize
argument_list|()
decl_stmt|;
for|for
control|(
name|Segment
name|segment
range|:
name|this
operator|.
name|pipeline
operator|.
name|getSegments
argument_list|()
control|)
block|{
name|h
operator|+=
name|segment
operator|.
name|heapSize
argument_list|()
expr_stmt|;
block|}
return|return
name|h
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateLowestUnflushedSequenceIdInWAL
parameter_list|(
name|boolean
name|onlyIfGreater
parameter_list|)
block|{
name|long
name|minSequenceId
init|=
name|pipeline
operator|.
name|getMinSequenceId
argument_list|()
decl_stmt|;
if|if
condition|(
name|minSequenceId
operator|!=
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
name|byte
index|[]
name|encodedRegionName
init|=
name|getRegionServices
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedNameAsBytes
argument_list|()
decl_stmt|;
name|byte
index|[]
name|familyName
init|=
name|getFamilyNameInBytes
argument_list|()
decl_stmt|;
name|WAL
name|WAL
init|=
name|getRegionServices
argument_list|()
operator|.
name|getWAL
argument_list|()
decl_stmt|;
if|if
condition|(
name|WAL
operator|!=
literal|null
condition|)
block|{
name|WAL
operator|.
name|updateStore
argument_list|(
name|encodedRegionName
argument_list|,
name|familyName
argument_list|,
name|minSequenceId
argument_list|,
name|onlyIfGreater
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * This message intends to inform the MemStore that next coming updates    * are going to be part of the replaying edits from WAL    */
annotation|@
name|Override
specifier|public
name|void
name|startReplayingFromWAL
parameter_list|()
block|{
name|inWalReplay
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * This message intends to inform the MemStore that the replaying edits from WAL    * are done    */
annotation|@
name|Override
specifier|public
name|void
name|stopReplayingFromWAL
parameter_list|()
block|{
name|inWalReplay
operator|=
literal|false
expr_stmt|;
block|}
comment|// the getSegments() method is used for tests only
annotation|@
name|VisibleForTesting
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|Segment
argument_list|>
name|getSegments
parameter_list|()
block|{
name|List
argument_list|<
name|?
extends|extends
name|Segment
argument_list|>
name|pipelineList
init|=
name|pipeline
operator|.
name|getSegments
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Segment
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|pipelineList
operator|.
name|size
argument_list|()
operator|+
literal|2
argument_list|)
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|this
operator|.
name|active
argument_list|)
expr_stmt|;
name|list
operator|.
name|addAll
argument_list|(
name|pipelineList
argument_list|)
expr_stmt|;
name|list
operator|.
name|addAll
argument_list|(
name|this
operator|.
name|snapshot
operator|.
name|getAllSegments
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|list
return|;
block|}
comment|// the following three methods allow to manipulate the settings of composite snapshot
specifier|public
name|void
name|setCompositeSnapshot
parameter_list|(
name|boolean
name|useCompositeSnapshot
parameter_list|)
block|{
name|this
operator|.
name|compositeSnapshot
operator|=
name|useCompositeSnapshot
expr_stmt|;
block|}
specifier|public
name|boolean
name|swapCompactedSegments
parameter_list|(
name|VersionedSegmentsList
name|versionedList
parameter_list|,
name|ImmutableSegment
name|result
parameter_list|,
name|boolean
name|merge
parameter_list|)
block|{
comment|// last true stands for updating the region size
return|return
name|pipeline
operator|.
name|swap
argument_list|(
name|versionedList
argument_list|,
name|result
argument_list|,
operator|!
name|merge
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**    * @param requesterVersion The caller must hold the VersionedList of the pipeline    *           with version taken earlier. This version must be passed as a parameter here.    *           The flattening happens only if versions match.    */
specifier|public
name|void
name|flattenOneSegment
parameter_list|(
name|long
name|requesterVersion
parameter_list|,
name|MemStoreCompactionStrategy
operator|.
name|Action
name|action
parameter_list|)
block|{
name|pipeline
operator|.
name|flattenOneSegment
argument_list|(
name|requesterVersion
argument_list|,
name|indexType
argument_list|,
name|action
argument_list|)
expr_stmt|;
block|}
comment|// setter is used only for testability
annotation|@
name|VisibleForTesting
name|void
name|setIndexType
parameter_list|(
name|IndexType
name|type
parameter_list|)
block|{
name|indexType
operator|=
name|type
expr_stmt|;
comment|// Because this functionality is for testing only and tests are setting in-memory flush size
comment|// according to their need, there is no setting of in-memory flush size, here.
comment|// If it is needed, please change in-memory flush size explicitly
block|}
specifier|public
name|IndexType
name|getIndexType
parameter_list|()
block|{
return|return
name|indexType
return|;
block|}
specifier|public
name|boolean
name|hasImmutableSegments
parameter_list|()
block|{
return|return
operator|!
name|pipeline
operator|.
name|isEmpty
argument_list|()
return|;
block|}
specifier|public
name|VersionedSegmentsList
name|getImmutableSegments
parameter_list|()
block|{
return|return
name|pipeline
operator|.
name|getVersionedList
argument_list|()
return|;
block|}
specifier|public
name|long
name|getSmallestReadPoint
parameter_list|()
block|{
return|return
name|store
operator|.
name|getSmallestReadPoint
argument_list|()
return|;
block|}
specifier|public
name|HStore
name|getStore
parameter_list|()
block|{
return|return
name|store
return|;
block|}
specifier|public
name|String
name|getFamilyName
parameter_list|()
block|{
return|return
name|Bytes
operator|.
name|toString
argument_list|(
name|getFamilyNameInBytes
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
comment|/*    * Scanners are ordered from 0 (oldest) to newest in increasing order.    */
specifier|public
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|getScanners
parameter_list|(
name|long
name|readPt
parameter_list|)
throws|throws
name|IOException
block|{
name|MutableSegment
name|activeTmp
init|=
name|active
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|Segment
argument_list|>
name|pipelineList
init|=
name|pipeline
operator|.
name|getSegments
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|Segment
argument_list|>
name|snapshotList
init|=
name|snapshot
operator|.
name|getAllSegments
argument_list|()
decl_stmt|;
name|long
name|order
init|=
literal|1L
operator|+
name|pipelineList
operator|.
name|size
argument_list|()
operator|+
name|snapshotList
operator|.
name|size
argument_list|()
decl_stmt|;
comment|// The list of elements in pipeline + the active element + the snapshot segment
comment|// The order is the Segment ordinal
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|list
init|=
name|createList
argument_list|(
operator|(
name|int
operator|)
name|order
argument_list|)
decl_stmt|;
name|order
operator|=
name|addToScanners
argument_list|(
name|activeTmp
argument_list|,
name|readPt
argument_list|,
name|order
argument_list|,
name|list
argument_list|)
expr_stmt|;
name|order
operator|=
name|addToScanners
argument_list|(
name|pipelineList
argument_list|,
name|readPt
argument_list|,
name|order
argument_list|,
name|list
argument_list|)
expr_stmt|;
name|addToScanners
argument_list|(
name|snapshotList
argument_list|,
name|readPt
argument_list|,
name|order
argument_list|,
name|list
argument_list|)
expr_stmt|;
return|return
name|list
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|protected
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|createList
parameter_list|(
name|int
name|capacity
parameter_list|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|capacity
argument_list|)
return|;
block|}
comment|/**    * Check whether anything need to be done based on the current active set size.    * The method is invoked upon every addition to the active set.    * For CompactingMemStore, flush the active set to the read-only memory if it's    * size is above threshold    */
annotation|@
name|Override
specifier|protected
name|void
name|checkActiveSize
parameter_list|()
block|{
if|if
condition|(
name|shouldFlushInMemory
argument_list|()
condition|)
block|{
comment|/* The thread is dispatched to flush-in-memory. This cannot be done       * on the same thread, because for flush-in-memory we require updatesLock       * in exclusive mode while this method (checkActiveSize) is invoked holding updatesLock       * in the shared mode. */
name|InMemoryFlushRunnable
name|runnable
init|=
operator|new
name|InMemoryFlushRunnable
argument_list|()
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Dispatching the MemStore in-memory flush for store "
operator|+
name|store
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|getPool
argument_list|()
operator|.
name|execute
argument_list|(
name|runnable
argument_list|)
expr_stmt|;
block|}
block|}
comment|// internally used method, externally visible only for tests
comment|// when invoked directly from tests it must be verified that the caller doesn't hold updatesLock,
comment|// otherwise there is a deadlock
annotation|@
name|VisibleForTesting
name|void
name|flushInMemory
parameter_list|()
throws|throws
name|IOException
block|{
comment|// setting the inMemoryFlushInProgress flag again for the case this method is invoked
comment|// directly (only in tests) in the common path setting from true to true is idempotent
name|inMemoryFlushInProgress
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Phase I: Update the pipeline
name|getRegionServices
argument_list|()
operator|.
name|blockUpdates
argument_list|()
expr_stmt|;
try|try
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"IN-MEMORY FLUSH: Pushing active segment into compaction pipeline"
argument_list|)
expr_stmt|;
name|pushActiveToPipeline
argument_list|(
name|this
operator|.
name|active
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|getRegionServices
argument_list|()
operator|.
name|unblockUpdates
argument_list|()
expr_stmt|;
block|}
comment|// Used by tests
if|if
condition|(
operator|!
name|allowCompaction
operator|.
name|get
argument_list|()
condition|)
block|{
return|return;
block|}
comment|// Phase II: Compact the pipeline
try|try
block|{
comment|// Speculative compaction execution, may be interrupted if flush is forced while
comment|// compaction is in progress
name|compactor
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to run in-memory compaction on {}/{}; exception={}"
argument_list|,
name|getRegionServices
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|getFamilyName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|inMemoryFlushInProgress
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"IN-MEMORY FLUSH: end"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|Segment
name|getLastSegment
parameter_list|()
block|{
name|Segment
name|localActive
init|=
name|getActive
argument_list|()
decl_stmt|;
name|Segment
name|tail
init|=
name|pipeline
operator|.
name|getTail
argument_list|()
decl_stmt|;
return|return
name|tail
operator|==
literal|null
condition|?
name|localActive
else|:
name|tail
return|;
block|}
specifier|private
name|byte
index|[]
name|getFamilyNameInBytes
parameter_list|()
block|{
return|return
name|store
operator|.
name|getColumnFamilyDescriptor
argument_list|()
operator|.
name|getName
argument_list|()
return|;
block|}
specifier|private
name|ThreadPoolExecutor
name|getPool
parameter_list|()
block|{
return|return
name|getRegionServices
argument_list|()
operator|.
name|getInMemoryCompactionPool
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|protected
name|boolean
name|shouldFlushInMemory
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|active
operator|.
name|keySize
argument_list|()
operator|>
name|inmemoryFlushSize
condition|)
block|{
comment|// size above flush threshold
if|if
condition|(
name|inWalReplay
condition|)
block|{
comment|// when replaying edits from WAL there is no need in in-memory flush
return|return
literal|false
return|;
comment|// regardless the size
block|}
comment|// the inMemoryFlushInProgress is CASed to be true here in order to mutual exclude
comment|// the insert of the active into the compaction pipeline
return|return
operator|(
name|inMemoryFlushInProgress
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
operator|)
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * The request to cancel the compaction asynchronous task (caused by in-memory flush)    * The compaction may still happen if the request was sent too late    * Non-blocking request    */
specifier|private
name|void
name|stopCompaction
parameter_list|()
block|{
if|if
condition|(
name|inMemoryFlushInProgress
operator|.
name|get
argument_list|()
condition|)
block|{
name|compactor
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|pushActiveToPipeline
parameter_list|(
name|MutableSegment
name|active
parameter_list|)
block|{
if|if
condition|(
operator|!
name|active
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|pipeline
operator|.
name|pushHead
argument_list|(
name|active
argument_list|)
expr_stmt|;
name|resetActive
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|pushTailToSnapshot
parameter_list|()
block|{
name|VersionedSegmentsList
name|segments
init|=
name|pipeline
operator|.
name|getVersionedTail
argument_list|()
decl_stmt|;
name|pushToSnapshot
argument_list|(
name|segments
operator|.
name|getStoreSegments
argument_list|()
argument_list|)
expr_stmt|;
comment|// In Swap: don't close segments (they are in snapshot now) and don't update the region size
name|pipeline
operator|.
name|swap
argument_list|(
name|segments
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|pushPipelineToSnapshot
parameter_list|()
block|{
name|int
name|iterationsCnt
init|=
literal|0
decl_stmt|;
name|boolean
name|done
init|=
literal|false
decl_stmt|;
while|while
condition|(
operator|!
name|done
condition|)
block|{
name|iterationsCnt
operator|++
expr_stmt|;
name|VersionedSegmentsList
name|segments
init|=
name|pipeline
operator|.
name|getVersionedList
argument_list|()
decl_stmt|;
name|pushToSnapshot
argument_list|(
name|segments
operator|.
name|getStoreSegments
argument_list|()
argument_list|)
expr_stmt|;
comment|// swap can return false in case the pipeline was updated by ongoing compaction
comment|// and the version increase, the chance of it happenning is very low
comment|// In Swap: don't close segments (they are in snapshot now) and don't update the region size
name|done
operator|=
name|pipeline
operator|.
name|swap
argument_list|(
name|segments
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
name|iterationsCnt
operator|>
literal|2
condition|)
block|{
comment|// practically it is impossible that this loop iterates more than two times
comment|// (because the compaction is stopped and none restarts it while in snapshot request),
comment|// however stopping here for the case of the infinite loop causing by any error
name|LOG
operator|.
name|warn
argument_list|(
literal|"Multiple unsuccessful attempts to push the compaction pipeline to snapshot,"
operator|+
literal|" while flushing to disk."
argument_list|)
expr_stmt|;
name|this
operator|.
name|snapshot
operator|=
name|SegmentFactory
operator|.
name|instance
argument_list|()
operator|.
name|createImmutableSegment
argument_list|(
name|getComparator
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
specifier|private
name|void
name|pushToSnapshot
parameter_list|(
name|List
argument_list|<
name|ImmutableSegment
argument_list|>
name|segments
parameter_list|)
block|{
if|if
condition|(
name|segments
operator|.
name|isEmpty
argument_list|()
condition|)
return|return;
if|if
condition|(
name|segments
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
operator|!
name|segments
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|this
operator|.
name|snapshot
operator|=
name|segments
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
return|return;
block|}
else|else
block|{
comment|// create composite snapshot
name|this
operator|.
name|snapshot
operator|=
name|SegmentFactory
operator|.
name|instance
argument_list|()
operator|.
name|createCompositeImmutableSegment
argument_list|(
name|getComparator
argument_list|()
argument_list|,
name|segments
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|RegionServicesForStores
name|getRegionServices
parameter_list|()
block|{
return|return
name|regionServices
return|;
block|}
comment|/**   * The in-memory-flusher thread performs the flush asynchronously.   * There is at most one thread per memstore instance.   * It takes the updatesLock exclusively, pushes active into the pipeline, releases updatesLock   * and compacts the pipeline.   */
specifier|private
class|class
name|InMemoryFlushRunnable
implements|implements
name|Runnable
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|flushInMemory
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to run memstore compaction. region "
operator|+
name|getRegionServices
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|"store: "
operator|+
name|getFamilyName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|VisibleForTesting
name|boolean
name|isMemStoreFlushingInMemory
parameter_list|()
block|{
return|return
name|inMemoryFlushInProgress
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * @param cell Find the row that comes after this one.  If null, we return the    *             first.    * @return Next row or null if none found.    */
name|Cell
name|getNextRow
parameter_list|(
specifier|final
name|Cell
name|cell
parameter_list|)
block|{
name|Cell
name|lowest
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|Segment
argument_list|>
name|segments
init|=
name|getSegments
argument_list|()
decl_stmt|;
for|for
control|(
name|Segment
name|segment
range|:
name|segments
control|)
block|{
if|if
condition|(
name|lowest
operator|==
literal|null
condition|)
block|{
name|lowest
operator|=
name|getNextRow
argument_list|(
name|cell
argument_list|,
name|segment
operator|.
name|getCellSet
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|lowest
operator|=
name|getLowest
argument_list|(
name|lowest
argument_list|,
name|getNextRow
argument_list|(
name|cell
argument_list|,
name|segment
operator|.
name|getCellSet
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|lowest
return|;
block|}
annotation|@
name|VisibleForTesting
name|long
name|getInmemoryFlushSize
parameter_list|()
block|{
return|return
name|inmemoryFlushSize
return|;
block|}
comment|// debug method
specifier|public
name|void
name|debug
parameter_list|()
block|{
name|String
name|msg
init|=
literal|"active size="
operator|+
name|this
operator|.
name|active
operator|.
name|keySize
argument_list|()
decl_stmt|;
name|msg
operator|+=
literal|" in-memory flush size is "
operator|+
name|inmemoryFlushSize
expr_stmt|;
name|msg
operator|+=
literal|" allow compaction is "
operator|+
operator|(
name|allowCompaction
operator|.
name|get
argument_list|()
condition|?
literal|"true"
else|:
literal|"false"
operator|)
expr_stmt|;
name|msg
operator|+=
literal|" inMemoryFlushInProgress is "
operator|+
operator|(
name|inMemoryFlushInProgress
operator|.
name|get
argument_list|()
condition|?
literal|"true"
else|:
literal|"false"
operator|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

