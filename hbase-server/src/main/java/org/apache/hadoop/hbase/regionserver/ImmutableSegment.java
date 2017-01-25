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
name|ExtendedCell
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
name|client
operator|.
name|Scan
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
name|TimeRange
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

begin_comment
comment|/**  * ImmutableSegment is an abstract class that extends the API supported by a {@link Segment},  * and is not needed for a {@link MutableSegment}. Specifically, the method  * {@link ImmutableSegment#getSnapshotScanner()} builds a special scanner for the  * {@link MemStoreSnapshot} object.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ImmutableSegment
extends|extends
name|Segment
block|{
specifier|private
specifier|static
specifier|final
name|long
name|DEEP_OVERHEAD
init|=
name|Segment
operator|.
name|DEEP_OVERHEAD
operator|+
operator|(
literal|2
operator|*
name|ClassSize
operator|.
name|REFERENCE
operator|)
comment|// Refs to timeRange and type
operator|+
name|ClassSize
operator|.
name|TIMERANGE
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|DEEP_OVERHEAD_CSLM
init|=
name|DEEP_OVERHEAD
operator|+
name|ClassSize
operator|.
name|CONCURRENT_SKIPLISTMAP
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|DEEP_OVERHEAD_CAM
init|=
name|DEEP_OVERHEAD
operator|+
name|ClassSize
operator|.
name|CELL_ARRAY_MAP
decl_stmt|;
comment|/**    * Types of ImmutableSegment    */
specifier|public
enum|enum
name|Type
block|{
name|SKIPLIST_MAP_BASED
block|,
name|ARRAY_MAP_BASED
block|,   }
comment|/**    * This is an immutable segment so use the read-only TimeRange rather than the heavy-weight    * TimeRangeTracker with all its synchronization when doing time range stuff.    */
specifier|private
specifier|final
name|TimeRange
name|timeRange
decl_stmt|;
specifier|private
name|Type
name|type
init|=
name|Type
operator|.
name|SKIPLIST_MAP_BASED
decl_stmt|;
comment|// whether it is based on CellFlatMap or ConcurrentSkipListMap
specifier|private
name|boolean
name|isFlat
parameter_list|()
block|{
return|return
operator|(
name|type
operator|!=
name|Type
operator|.
name|SKIPLIST_MAP_BASED
operator|)
return|;
block|}
comment|/////////////////////  CONSTRUCTORS  /////////////////////
comment|/**------------------------------------------------------------------------    * Empty C-tor to be used only for CompositeImmutableSegment    */
specifier|protected
name|ImmutableSegment
parameter_list|(
name|CellComparator
name|comparator
parameter_list|)
block|{
name|super
argument_list|(
name|comparator
argument_list|)
expr_stmt|;
name|this
operator|.
name|timeRange
operator|=
literal|null
expr_stmt|;
block|}
comment|/**------------------------------------------------------------------------    * Copy C-tor to be used when new ImmutableSegment is being built from a Mutable one.    * This C-tor should be used when active MutableSegment is pushed into the compaction    * pipeline and becomes an ImmutableSegment.    */
specifier|protected
name|ImmutableSegment
parameter_list|(
name|Segment
name|segment
parameter_list|)
block|{
name|super
argument_list|(
name|segment
argument_list|)
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|Type
operator|.
name|SKIPLIST_MAP_BASED
expr_stmt|;
name|this
operator|.
name|timeRange
operator|=
name|this
operator|.
name|timeRangeTracker
operator|==
literal|null
condition|?
literal|null
else|:
name|this
operator|.
name|timeRangeTracker
operator|.
name|toTimeRange
argument_list|()
expr_stmt|;
block|}
comment|/**------------------------------------------------------------------------    * C-tor to be used when new CELL_ARRAY BASED ImmutableSegment is a result of compaction of a    * list of older ImmutableSegments.    * The given iterator returns the Cells that "survived" the compaction.    * The input parameter "type" exists for future use when more types of flat ImmutableSegments    * are going to be introduced.    */
specifier|protected
name|ImmutableSegment
parameter_list|(
name|CellComparator
name|comparator
parameter_list|,
name|MemStoreSegmentsIterator
name|iterator
parameter_list|,
name|MemStoreLAB
name|memStoreLAB
parameter_list|,
name|int
name|numOfCells
parameter_list|,
name|Type
name|type
parameter_list|,
name|boolean
name|merge
parameter_list|)
block|{
name|super
argument_list|(
literal|null
argument_list|,
comment|// initiailize the CellSet with NULL
name|comparator
argument_list|,
name|memStoreLAB
argument_list|)
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
comment|// build the true CellSet based on CellArrayMap
name|CellSet
name|cs
init|=
name|createCellArrayMapSet
argument_list|(
name|numOfCells
argument_list|,
name|iterator
argument_list|,
name|merge
argument_list|)
decl_stmt|;
name|this
operator|.
name|setCellSet
argument_list|(
literal|null
argument_list|,
name|cs
argument_list|)
expr_stmt|;
comment|// update the CellSet of the new Segment
name|this
operator|.
name|timeRange
operator|=
name|this
operator|.
name|timeRangeTracker
operator|==
literal|null
condition|?
literal|null
else|:
name|this
operator|.
name|timeRangeTracker
operator|.
name|toTimeRange
argument_list|()
expr_stmt|;
block|}
comment|/**------------------------------------------------------------------------    * C-tor to be used when new SKIP-LIST BASED ImmutableSegment is a result of compaction of a    * list of older ImmutableSegments.    * The given iterator returns the Cells that "survived" the compaction.    */
specifier|protected
name|ImmutableSegment
parameter_list|(
name|CellComparator
name|comparator
parameter_list|,
name|MemStoreSegmentsIterator
name|iterator
parameter_list|,
name|MemStoreLAB
name|memStoreLAB
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|CellSet
argument_list|(
name|comparator
argument_list|)
argument_list|,
comment|// initiailize the CellSet with empty CellSet
name|comparator
argument_list|,
name|memStoreLAB
argument_list|)
expr_stmt|;
name|type
operator|=
name|Type
operator|.
name|SKIPLIST_MAP_BASED
expr_stmt|;
name|MemstoreSize
name|memstoreSize
init|=
operator|new
name|MemstoreSize
argument_list|()
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Cell
name|c
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
comment|// The scanner is doing all the elimination logic
comment|// now we just copy it to the new segment
name|Cell
name|newKV
init|=
name|maybeCloneWithAllocator
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|boolean
name|usedMSLAB
init|=
operator|(
name|newKV
operator|!=
name|c
operator|)
decl_stmt|;
name|internalAdd
argument_list|(
name|newKV
argument_list|,
name|usedMSLAB
argument_list|,
name|memstoreSize
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|timeRange
operator|=
name|this
operator|.
name|timeRangeTracker
operator|==
literal|null
condition|?
literal|null
else|:
name|this
operator|.
name|timeRangeTracker
operator|.
name|toTimeRange
argument_list|()
expr_stmt|;
block|}
comment|/////////////////////  PUBLIC METHODS  /////////////////////
comment|/**    * Builds a special scanner for the MemStoreSnapshot object that is different than the    * general segment scanner.    * @return a special scanner for the MemStoreSnapshot object    */
specifier|public
name|KeyValueScanner
name|getSnapshotScanner
parameter_list|()
block|{
return|return
operator|new
name|SnapshotScanner
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|shouldSeek
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|long
name|oldestUnexpiredTS
parameter_list|)
block|{
return|return
name|this
operator|.
name|timeRange
operator|.
name|includesTimeRange
argument_list|(
name|scan
operator|.
name|getTimeRange
argument_list|()
argument_list|)
operator|&&
name|this
operator|.
name|timeRange
operator|.
name|getMax
argument_list|()
operator|>=
name|oldestUnexpiredTS
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMinTimestamp
parameter_list|()
block|{
return|return
name|this
operator|.
name|timeRange
operator|.
name|getMin
argument_list|()
return|;
block|}
specifier|public
name|int
name|getNumOfSegments
parameter_list|()
block|{
return|return
literal|1
return|;
block|}
specifier|public
name|List
argument_list|<
name|Segment
argument_list|>
name|getAllSegments
parameter_list|()
block|{
name|List
argument_list|<
name|Segment
argument_list|>
name|res
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|this
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|res
return|;
block|}
comment|/**------------------------------------------------------------------------    * Change the CellSet of this ImmutableSegment from one based on ConcurrentSkipListMap to one    * based on CellArrayMap.    * If this ImmutableSegment is not based on ConcurrentSkipListMap , this is NOOP    *    * Synchronization of the CellSet replacement:    * The reference to the CellSet is AtomicReference and is updated only when ImmutableSegment    * is constructed (single thread) or flattened. The flattening happens as part of a single    * thread of compaction, but to be on the safe side the initial CellSet is locally saved    * before the flattening and then replaced using CAS instruction.    */
specifier|public
name|boolean
name|flatten
parameter_list|(
name|MemstoreSize
name|memstoreSize
parameter_list|)
block|{
if|if
condition|(
name|isFlat
argument_list|()
condition|)
return|return
literal|false
return|;
name|CellSet
name|oldCellSet
init|=
name|getCellSet
argument_list|()
decl_stmt|;
name|int
name|numOfCells
init|=
name|getCellsCount
argument_list|()
decl_stmt|;
comment|// build the new (CellSet CellArrayMap based)
name|CellSet
name|newCellSet
init|=
name|recreateCellArrayMapSet
argument_list|(
name|numOfCells
argument_list|)
decl_stmt|;
name|type
operator|=
name|Type
operator|.
name|ARRAY_MAP_BASED
expr_stmt|;
name|setCellSet
argument_list|(
name|oldCellSet
argument_list|,
name|newCellSet
argument_list|)
expr_stmt|;
comment|// arrange the meta-data size, decrease all meta-data sizes related to SkipList
comment|// (recreateCellArrayMapSet doesn't take the care for the sizes)
name|long
name|newSegmentSizeDelta
init|=
operator|-
operator|(
name|numOfCells
operator|*
name|ClassSize
operator|.
name|CONCURRENT_SKIPLISTMAP_ENTRY
operator|)
decl_stmt|;
comment|// add size of CellArrayMap and meta-data overhead per Cell
name|newSegmentSizeDelta
operator|=
name|newSegmentSizeDelta
operator|+
name|numOfCells
operator|*
name|ClassSize
operator|.
name|CELL_ARRAY_MAP_ENTRY
expr_stmt|;
name|incSize
argument_list|(
literal|0
argument_list|,
name|newSegmentSizeDelta
argument_list|)
expr_stmt|;
if|if
condition|(
name|memstoreSize
operator|!=
literal|null
condition|)
block|{
name|memstoreSize
operator|.
name|incMemstoreSize
argument_list|(
literal|0
argument_list|,
name|newSegmentSizeDelta
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
comment|/////////////////////  PRIVATE METHODS  /////////////////////
comment|/*------------------------------------------------------------------------*/
comment|// Create CellSet based on CellArrayMap from compacting iterator
specifier|private
name|CellSet
name|createCellArrayMapSet
parameter_list|(
name|int
name|numOfCells
parameter_list|,
name|MemStoreSegmentsIterator
name|iterator
parameter_list|,
name|boolean
name|merge
parameter_list|)
block|{
name|Cell
index|[]
name|cells
init|=
operator|new
name|Cell
index|[
name|numOfCells
index|]
decl_stmt|;
comment|// build the Cell Array
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Cell
name|c
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
comment|// The scanner behind the iterator is doing all the elimination logic
if|if
condition|(
name|merge
condition|)
block|{
comment|// if this is merge we just move the Cell object without copying MSLAB
comment|// the sizes still need to be updated in the new segment
name|cells
index|[
name|i
index|]
operator|=
name|c
expr_stmt|;
block|}
else|else
block|{
comment|// now we just copy it to the new segment (also MSLAB copy)
name|cells
index|[
name|i
index|]
operator|=
name|maybeCloneWithAllocator
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
name|boolean
name|useMSLAB
init|=
operator|(
name|getMemStoreLAB
argument_list|()
operator|!=
literal|null
operator|)
decl_stmt|;
comment|// second parameter true, because in compaction addition of the cell to new segment
comment|// is always successful
name|updateMetaInfo
argument_list|(
name|c
argument_list|,
literal|true
argument_list|,
name|useMSLAB
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// updates the size per cell
name|i
operator|++
expr_stmt|;
block|}
comment|// build the immutable CellSet
name|CellArrayMap
name|cam
init|=
operator|new
name|CellArrayMap
argument_list|(
name|getComparator
argument_list|()
argument_list|,
name|cells
argument_list|,
literal|0
argument_list|,
name|i
argument_list|,
literal|false
argument_list|)
decl_stmt|;
return|return
operator|new
name|CellSet
argument_list|(
name|cam
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|long
name|heapOverheadChange
parameter_list|(
name|Cell
name|cell
parameter_list|,
name|boolean
name|succ
parameter_list|)
block|{
if|if
condition|(
name|succ
condition|)
block|{
switch|switch
condition|(
name|this
operator|.
name|type
condition|)
block|{
case|case
name|SKIPLIST_MAP_BASED
case|:
return|return
name|super
operator|.
name|heapOverheadChange
argument_list|(
name|cell
argument_list|,
name|succ
argument_list|)
return|;
case|case
name|ARRAY_MAP_BASED
case|:
if|if
condition|(
name|cell
operator|instanceof
name|ExtendedCell
condition|)
block|{
return|return
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|CELL_ARRAY_MAP_ENTRY
operator|+
operator|(
operator|(
name|ExtendedCell
operator|)
name|cell
operator|)
operator|.
name|heapOverhead
argument_list|()
argument_list|)
return|;
block|}
return|return
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|CELL_ARRAY_MAP_ENTRY
operator|+
name|KeyValue
operator|.
name|FIXED_OVERHEAD
argument_list|)
return|;
block|}
block|}
return|return
literal|0
return|;
block|}
comment|/*------------------------------------------------------------------------*/
comment|// Create CellSet based on CellArrayMap from current ConcurrentSkipListMap based CellSet
comment|// (without compacting iterator)
specifier|private
name|CellSet
name|recreateCellArrayMapSet
parameter_list|(
name|int
name|numOfCells
parameter_list|)
block|{
name|Cell
index|[]
name|cells
init|=
operator|new
name|Cell
index|[
name|numOfCells
index|]
decl_stmt|;
comment|// build the Cell Array
name|Cell
name|curCell
decl_stmt|;
name|int
name|idx
init|=
literal|0
decl_stmt|;
comment|// create this segment scanner with maximal possible read point, to go over all Cells
name|KeyValueScanner
name|segmentScanner
init|=
name|this
operator|.
name|getScanner
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
try|try
block|{
while|while
condition|(
operator|(
name|curCell
operator|=
name|segmentScanner
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|cells
index|[
name|idx
operator|++
index|]
operator|=
name|curCell
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ie
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|ie
argument_list|)
throw|;
block|}
finally|finally
block|{
name|segmentScanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// build the immutable CellSet
name|CellArrayMap
name|cam
init|=
operator|new
name|CellArrayMap
argument_list|(
name|getComparator
argument_list|()
argument_list|,
name|cells
argument_list|,
literal|0
argument_list|,
name|idx
argument_list|,
literal|false
argument_list|)
decl_stmt|;
return|return
operator|new
name|CellSet
argument_list|(
name|cam
argument_list|)
return|;
block|}
block|}
end_class

end_unit

