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

begin_comment
comment|/**  * The ongoing MemStore Compaction manager, dispatches a solo running compaction and interrupts  * the compaction if requested. The compaction is interrupted and stopped by CompactingMemStore,  * for example when another compaction needs to be started.  * Prior to compaction the MemStoreCompactor evaluates  * the compacting ratio and aborts the compaction if it is not worthy.  * The MemStoreScanner is used to traverse the compaction pipeline. The MemStoreScanner  * is included in internal store scanner, where all compaction logic is implemented.  * Threads safety: It is assumed that the compaction pipeline is immutable,  * therefore no special synchronization is required.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MemStoreCompactor
block|{
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
name|ClassSize
operator|.
name|OBJECT
operator|+
literal|4
operator|*
name|ClassSize
operator|.
name|REFERENCE
comment|// compactingMemStore, versionedList, isInterrupted, strategy (the reference)
comment|// "action" is an enum and thus it is a class with static final constants,
comment|// so counting only the size of the reference to it and not the size of the internals
operator|+
name|Bytes
operator|.
name|SIZEOF_INT
comment|// compactionKVMax
operator|+
name|ClassSize
operator|.
name|ATOMIC_BOOLEAN
comment|// isInterrupted (the internals)
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
name|MemStoreCompactor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|CompactingMemStore
name|compactingMemStore
decl_stmt|;
comment|// a static version of the segment list from the pipeline
specifier|private
name|VersionedSegmentsList
name|versionedList
decl_stmt|;
comment|// a flag raised when compaction is requested to stop
specifier|private
specifier|final
name|AtomicBoolean
name|isInterrupted
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
comment|// the limit to the size of the groups to be later provided to MemStoreSegmentsIterator
specifier|private
specifier|final
name|int
name|compactionKVMax
decl_stmt|;
specifier|private
name|MemStoreCompactionStrategy
name|strategy
decl_stmt|;
specifier|public
name|MemStoreCompactor
parameter_list|(
name|CompactingMemStore
name|compactingMemStore
parameter_list|,
name|MemoryCompactionPolicy
name|compactionPolicy
parameter_list|)
throws|throws
name|IllegalArgumentIOException
block|{
name|this
operator|.
name|compactingMemStore
operator|=
name|compactingMemStore
expr_stmt|;
name|this
operator|.
name|compactionKVMax
operator|=
name|compactingMemStore
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|COMPACTION_KV_MAX
argument_list|,
name|HConstants
operator|.
name|COMPACTION_KV_MAX_DEFAULT
argument_list|)
expr_stmt|;
name|initiateCompactionStrategy
argument_list|(
name|compactionPolicy
argument_list|,
name|compactingMemStore
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|compactingMemStore
operator|.
name|getFamilyName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**----------------------------------------------------------------------    * The request to dispatch the compaction asynchronous task.    * The method returns true if compaction was successfully dispatched, or false if there    * is already an ongoing compaction or no segments to compact.    */
specifier|public
name|boolean
name|start
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|compactingMemStore
operator|.
name|hasImmutableSegments
argument_list|()
condition|)
block|{
comment|// no compaction on empty pipeline
return|return
literal|false
return|;
block|}
comment|// get a snapshot of the list of the segments from the pipeline,
comment|// this local copy of the list is marked with specific version
name|versionedList
operator|=
name|compactingMemStore
operator|.
name|getImmutableSegments
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Starting on {}/{}"
argument_list|,
name|compactingMemStore
operator|.
name|getStore
argument_list|()
operator|.
name|getHRegion
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|compactingMemStore
operator|.
name|getStore
argument_list|()
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
name|HStore
name|store
init|=
name|compactingMemStore
operator|.
name|getStore
argument_list|()
decl_stmt|;
name|RegionCoprocessorHost
name|cpHost
init|=
name|store
operator|.
name|getCoprocessorHost
argument_list|()
decl_stmt|;
if|if
condition|(
name|cpHost
operator|!=
literal|null
condition|)
block|{
name|cpHost
operator|.
name|preMemStoreCompaction
argument_list|(
name|store
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|doCompaction
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|cpHost
operator|!=
literal|null
condition|)
block|{
name|cpHost
operator|.
name|postMemStoreCompaction
argument_list|(
name|store
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
block|}
comment|/**----------------------------------------------------------------------   * The request to cancel the compaction asynchronous task   * The compaction may still happen if the request was sent too late   * Non-blocking request   */
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|isInterrupted
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|resetStats
parameter_list|()
block|{
name|strategy
operator|.
name|resetStats
argument_list|()
expr_stmt|;
block|}
comment|/**----------------------------------------------------------------------   * Reset the interruption indicator and clear the pointers in order to allow good   * garbage collection   */
specifier|private
name|void
name|releaseResources
parameter_list|()
block|{
name|isInterrupted
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|versionedList
operator|=
literal|null
expr_stmt|;
block|}
comment|/**----------------------------------------------------------------------   * The worker thread performs the compaction asynchronously.   * The solo (per compactor) thread only reads the compaction pipeline.   * There is at most one thread per memstore instance.   */
specifier|private
name|void
name|doCompaction
parameter_list|()
block|{
name|ImmutableSegment
name|result
init|=
literal|null
decl_stmt|;
name|boolean
name|resultSwapped
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|isInterrupted
operator|.
name|get
argument_list|()
condition|)
block|{
comment|// if the entire process is interrupted cancel flattening
return|return;
comment|// the compaction also doesn't start when interrupted
block|}
name|MemStoreCompactionStrategy
operator|.
name|Action
name|nextStep
init|=
name|strategy
operator|.
name|getAction
argument_list|(
name|versionedList
argument_list|)
decl_stmt|;
name|boolean
name|merge
init|=
operator|(
name|nextStep
operator|==
name|MemStoreCompactionStrategy
operator|.
name|Action
operator|.
name|MERGE
operator|||
name|nextStep
operator|==
name|MemStoreCompactionStrategy
operator|.
name|Action
operator|.
name|MERGE_COUNT_UNIQUE_KEYS
operator|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|nextStep
operator|==
name|MemStoreCompactionStrategy
operator|.
name|Action
operator|.
name|NOOP
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|nextStep
operator|==
name|MemStoreCompactionStrategy
operator|.
name|Action
operator|.
name|FLATTEN
operator|||
name|nextStep
operator|==
name|MemStoreCompactionStrategy
operator|.
name|Action
operator|.
name|FLATTEN_COUNT_UNIQUE_KEYS
condition|)
block|{
comment|// some Segment in the pipeline is with SkipList index, make it flat
name|compactingMemStore
operator|.
name|flattenOneSegment
argument_list|(
name|versionedList
operator|.
name|getVersion
argument_list|()
argument_list|,
name|nextStep
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Create one segment representing all segments in the compaction pipeline,
comment|// either by compaction or by merge
if|if
condition|(
operator|!
name|isInterrupted
operator|.
name|get
argument_list|()
condition|)
block|{
name|result
operator|=
name|createSubstitution
argument_list|(
name|nextStep
argument_list|)
expr_stmt|;
block|}
comment|// Substitute the pipeline with one segment
if|if
condition|(
operator|!
name|isInterrupted
operator|.
name|get
argument_list|()
condition|)
block|{
name|resultSwapped
operator|=
name|compactingMemStore
operator|.
name|swapCompactedSegments
argument_list|(
name|versionedList
argument_list|,
name|result
argument_list|,
name|merge
argument_list|)
expr_stmt|;
if|if
condition|(
name|resultSwapped
condition|)
block|{
comment|// update compaction strategy
name|strategy
operator|.
name|updateStats
argument_list|(
name|result
argument_list|)
expr_stmt|;
comment|// update the wal so it can be truncated and not get too long
name|compactingMemStore
operator|.
name|updateLowestUnflushedSequenceIdInWAL
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// only if greater
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Interrupting the MemStore in-memory compaction for store "
operator|+
name|compactingMemStore
operator|.
name|getFamilyName
argument_list|()
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
comment|// For the MERGE case, if the result was created, but swap didn't happen,
comment|// we DON'T need to close the result segment (meaning its MSLAB)!
comment|// Because closing the result segment means closing the chunks of all segments
comment|// in the compaction pipeline, which still have ongoing scans.
if|if
condition|(
operator|!
name|merge
operator|&&
operator|(
name|result
operator|!=
literal|null
operator|)
operator|&&
operator|!
name|resultSwapped
condition|)
block|{
name|result
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|releaseResources
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**----------------------------------------------------------------------    * Creation of the ImmutableSegment either by merge or copy-compact of the segments of the    * pipeline, based on the Compactor Iterator. The new ImmutableSegment is returned.    */
specifier|private
name|ImmutableSegment
name|createSubstitution
parameter_list|(
name|MemStoreCompactionStrategy
operator|.
name|Action
name|action
parameter_list|)
throws|throws
name|IOException
block|{
name|ImmutableSegment
name|result
init|=
literal|null
decl_stmt|;
name|MemStoreSegmentsIterator
name|iterator
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|action
condition|)
block|{
case|case
name|COMPACT
case|:
name|iterator
operator|=
operator|new
name|MemStoreCompactorSegmentsIterator
argument_list|(
name|versionedList
operator|.
name|getStoreSegments
argument_list|()
argument_list|,
name|compactingMemStore
operator|.
name|getComparator
argument_list|()
argument_list|,
name|compactionKVMax
argument_list|,
name|compactingMemStore
operator|.
name|getStore
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|=
name|SegmentFactory
operator|.
name|instance
argument_list|()
operator|.
name|createImmutableSegmentByCompaction
argument_list|(
name|compactingMemStore
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|compactingMemStore
operator|.
name|getComparator
argument_list|()
argument_list|,
name|iterator
argument_list|,
name|versionedList
operator|.
name|getNumOfCells
argument_list|()
argument_list|,
name|compactingMemStore
operator|.
name|getIndexType
argument_list|()
argument_list|,
name|action
argument_list|)
expr_stmt|;
name|iterator
operator|.
name|close
argument_list|()
expr_stmt|;
break|break;
case|case
name|MERGE
case|:
case|case
name|MERGE_COUNT_UNIQUE_KEYS
case|:
name|iterator
operator|=
operator|new
name|MemStoreMergerSegmentsIterator
argument_list|(
name|versionedList
operator|.
name|getStoreSegments
argument_list|()
argument_list|,
name|compactingMemStore
operator|.
name|getComparator
argument_list|()
argument_list|,
name|compactionKVMax
argument_list|)
expr_stmt|;
name|result
operator|=
name|SegmentFactory
operator|.
name|instance
argument_list|()
operator|.
name|createImmutableSegmentByMerge
argument_list|(
name|compactingMemStore
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|compactingMemStore
operator|.
name|getComparator
argument_list|()
argument_list|,
name|iterator
argument_list|,
name|versionedList
operator|.
name|getNumOfCells
argument_list|()
argument_list|,
name|versionedList
operator|.
name|getStoreSegments
argument_list|()
argument_list|,
name|compactingMemStore
operator|.
name|getIndexType
argument_list|()
argument_list|,
name|action
argument_list|)
expr_stmt|;
name|iterator
operator|.
name|close
argument_list|()
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unknown action "
operator|+
name|action
argument_list|)
throw|;
comment|// sanity check
block|}
return|return
name|result
return|;
block|}
annotation|@
name|VisibleForTesting
name|void
name|initiateCompactionStrategy
parameter_list|(
name|MemoryCompactionPolicy
name|compType
parameter_list|,
name|Configuration
name|configuration
parameter_list|,
name|String
name|cfName
parameter_list|)
throws|throws
name|IllegalArgumentIOException
block|{
assert|assert
operator|(
name|compType
operator|!=
name|MemoryCompactionPolicy
operator|.
name|NONE
operator|)
assert|;
switch|switch
condition|(
name|compType
condition|)
block|{
case|case
name|BASIC
case|:
name|strategy
operator|=
operator|new
name|BasicMemStoreCompactionStrategy
argument_list|(
name|configuration
argument_list|,
name|cfName
argument_list|)
expr_stmt|;
break|break;
case|case
name|EAGER
case|:
name|strategy
operator|=
operator|new
name|EagerMemStoreCompactionStrategy
argument_list|(
name|configuration
argument_list|,
name|cfName
argument_list|)
expr_stmt|;
break|break;
case|case
name|ADAPTIVE
case|:
name|strategy
operator|=
operator|new
name|AdaptiveMemStoreCompactionStrategy
argument_list|(
name|configuration
argument_list|,
name|cfName
argument_list|)
expr_stmt|;
break|break;
default|default:
comment|// sanity check
throw|throw
operator|new
name|IllegalArgumentIOException
argument_list|(
literal|"Unknown memory compaction type "
operator|+
name|compType
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

