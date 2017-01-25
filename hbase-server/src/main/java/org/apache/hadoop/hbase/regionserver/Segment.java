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
name|Iterator
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
name|SortedSet
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
name|AtomicReference
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
name|KeyValueUtil
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
comment|/**  * This is an abstraction of a segment maintained in a memstore, e.g., the active  * cell set or its snapshot.  *  * This abstraction facilitates the management of the compaction pipeline and the shifts of these  * segments from active set to snapshot set in the default implementation.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|Segment
block|{
specifier|final
specifier|static
name|long
name|FIXED_OVERHEAD
init|=
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|OBJECT
operator|+
literal|5
operator|*
name|ClassSize
operator|.
name|REFERENCE
comment|// cellSet, comparator, memStoreLAB, size, timeRangeTracker
operator|+
name|Bytes
operator|.
name|SIZEOF_LONG
comment|// minSequenceId
operator|+
name|Bytes
operator|.
name|SIZEOF_BOOLEAN
argument_list|)
decl_stmt|;
comment|// tagsPresent
specifier|public
specifier|final
specifier|static
name|long
name|DEEP_OVERHEAD
init|=
name|FIXED_OVERHEAD
operator|+
name|ClassSize
operator|.
name|ATOMIC_REFERENCE
operator|+
name|ClassSize
operator|.
name|CELL_SET
operator|+
name|ClassSize
operator|.
name|ATOMIC_LONG
operator|+
name|ClassSize
operator|.
name|TIMERANGE_TRACKER
decl_stmt|;
specifier|private
name|AtomicReference
argument_list|<
name|CellSet
argument_list|>
name|cellSet
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|CellComparator
name|comparator
decl_stmt|;
specifier|protected
name|long
name|minSequenceId
decl_stmt|;
specifier|private
name|MemStoreLAB
name|memStoreLAB
decl_stmt|;
comment|// Sum of sizes of all Cells added to this Segment. Cell's heapSize is considered. This is not
comment|// including the heap overhead of this class.
specifier|protected
specifier|final
name|AtomicLong
name|dataSize
decl_stmt|;
specifier|protected
specifier|final
name|AtomicLong
name|heapOverhead
decl_stmt|;
specifier|protected
specifier|final
name|TimeRangeTracker
name|timeRangeTracker
decl_stmt|;
specifier|protected
specifier|volatile
name|boolean
name|tagsPresent
decl_stmt|;
comment|// Empty constructor to be used when Segment is used as interface,
comment|// and there is no need in true Segments state
specifier|protected
name|Segment
parameter_list|(
name|CellComparator
name|comparator
parameter_list|)
block|{
name|this
operator|.
name|comparator
operator|=
name|comparator
expr_stmt|;
name|this
operator|.
name|dataSize
operator|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|heapOverhead
operator|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|timeRangeTracker
operator|=
operator|new
name|TimeRangeTracker
argument_list|()
expr_stmt|;
block|}
comment|// This constructor is used to create empty Segments.
specifier|protected
name|Segment
parameter_list|(
name|CellSet
name|cellSet
parameter_list|,
name|CellComparator
name|comparator
parameter_list|,
name|MemStoreLAB
name|memStoreLAB
parameter_list|)
block|{
name|this
operator|.
name|cellSet
operator|.
name|set
argument_list|(
name|cellSet
argument_list|)
expr_stmt|;
name|this
operator|.
name|comparator
operator|=
name|comparator
expr_stmt|;
name|this
operator|.
name|minSequenceId
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
name|this
operator|.
name|memStoreLAB
operator|=
name|memStoreLAB
expr_stmt|;
name|this
operator|.
name|dataSize
operator|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|heapOverhead
operator|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|tagsPresent
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|timeRangeTracker
operator|=
operator|new
name|TimeRangeTracker
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|Segment
parameter_list|(
name|Segment
name|segment
parameter_list|)
block|{
name|this
operator|.
name|cellSet
operator|.
name|set
argument_list|(
name|segment
operator|.
name|getCellSet
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|comparator
operator|=
name|segment
operator|.
name|getComparator
argument_list|()
expr_stmt|;
name|this
operator|.
name|minSequenceId
operator|=
name|segment
operator|.
name|getMinSequenceId
argument_list|()
expr_stmt|;
name|this
operator|.
name|memStoreLAB
operator|=
name|segment
operator|.
name|getMemStoreLAB
argument_list|()
expr_stmt|;
name|this
operator|.
name|dataSize
operator|=
operator|new
name|AtomicLong
argument_list|(
name|segment
operator|.
name|keySize
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|heapOverhead
operator|=
operator|new
name|AtomicLong
argument_list|(
name|segment
operator|.
name|heapOverhead
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|tagsPresent
operator|=
name|segment
operator|.
name|isTagsPresent
argument_list|()
expr_stmt|;
name|this
operator|.
name|timeRangeTracker
operator|=
name|segment
operator|.
name|getTimeRangeTracker
argument_list|()
expr_stmt|;
block|}
comment|/**    * Creates the scanner for the given read point    * @return a scanner for the given read point    */
specifier|public
name|KeyValueScanner
name|getScanner
parameter_list|(
name|long
name|readPoint
parameter_list|)
block|{
return|return
operator|new
name|SegmentScanner
argument_list|(
name|this
argument_list|,
name|readPoint
argument_list|)
return|;
block|}
comment|/**    * Creates the scanner for the given read point, and a specific order in a list    * @return a scanner for the given read point    */
specifier|public
name|KeyValueScanner
name|getScanner
parameter_list|(
name|long
name|readPoint
parameter_list|,
name|long
name|order
parameter_list|)
block|{
return|return
operator|new
name|SegmentScanner
argument_list|(
name|this
argument_list|,
name|readPoint
argument_list|,
name|order
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|getScanners
parameter_list|(
name|long
name|readPoint
parameter_list|,
name|long
name|order
parameter_list|)
block|{
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|scanners
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|scanners
operator|.
name|add
argument_list|(
name|getScanner
argument_list|(
name|readPoint
argument_list|,
name|order
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|scanners
return|;
block|}
comment|/**    * @return whether the segment has any cells    */
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|getCellSet
argument_list|()
operator|.
name|isEmpty
argument_list|()
return|;
block|}
comment|/**    * @return number of cells in segment    */
specifier|public
name|int
name|getCellsCount
parameter_list|()
block|{
return|return
name|getCellSet
argument_list|()
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**    * @return the first cell in the segment that has equal or greater key than the given cell    */
specifier|public
name|Cell
name|getFirstAfter
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|SortedSet
argument_list|<
name|Cell
argument_list|>
name|snTailSet
init|=
name|tailSet
argument_list|(
name|cell
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|snTailSet
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|snTailSet
operator|.
name|first
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Closing a segment before it is being discarded    */
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|memStoreLAB
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|memStoreLAB
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// do not set MSLab to null as scanners may still be reading the data here and need to decrease
comment|// the counter when they finish
block|}
comment|/**    * If the segment has a memory allocator the cell is being cloned to this space, and returned;    * otherwise the given cell is returned    * @return either the given cell or its clone    */
specifier|public
name|Cell
name|maybeCloneWithAllocator
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|memStoreLAB
operator|==
literal|null
condition|)
block|{
return|return
name|cell
return|;
block|}
name|Cell
name|cellFromMslab
init|=
name|this
operator|.
name|memStoreLAB
operator|.
name|copyCellInto
argument_list|(
name|cell
argument_list|)
decl_stmt|;
return|return
operator|(
name|cellFromMslab
operator|!=
literal|null
operator|)
condition|?
name|cellFromMslab
else|:
name|cell
return|;
block|}
comment|/**    * Get cell length after serialized in {@link KeyValue}    */
annotation|@
name|VisibleForTesting
specifier|static
name|int
name|getCellLength
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
return|return
name|KeyValueUtil
operator|.
name|length
argument_list|(
name|cell
argument_list|)
return|;
block|}
specifier|public
specifier|abstract
name|boolean
name|shouldSeek
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|long
name|oldestUnexpiredTS
parameter_list|)
function_decl|;
specifier|public
specifier|abstract
name|long
name|getMinTimestamp
parameter_list|()
function_decl|;
specifier|public
name|boolean
name|isTagsPresent
parameter_list|()
block|{
return|return
name|tagsPresent
return|;
block|}
specifier|public
name|void
name|incScannerCount
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|memStoreLAB
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|memStoreLAB
operator|.
name|incScannerCount
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|decScannerCount
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|memStoreLAB
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|memStoreLAB
operator|.
name|decScannerCount
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Setting the CellSet of the segment - used only for flat immutable segment for setting    * immutable CellSet after its creation in immutable segment constructor    * @return this object    */
specifier|protected
name|Segment
name|setCellSet
parameter_list|(
name|CellSet
name|cellSetOld
parameter_list|,
name|CellSet
name|cellSetNew
parameter_list|)
block|{
name|this
operator|.
name|cellSet
operator|.
name|compareAndSet
argument_list|(
name|cellSetOld
argument_list|,
name|cellSetNew
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * @return Sum of all cell's size.    */
specifier|public
name|long
name|keySize
parameter_list|()
block|{
return|return
name|this
operator|.
name|dataSize
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * @return The heap overhead of this segment.    */
specifier|public
name|long
name|heapOverhead
parameter_list|()
block|{
return|return
name|this
operator|.
name|heapOverhead
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * Updates the heap size counter of the segment by the given delta    */
specifier|protected
name|void
name|incSize
parameter_list|(
name|long
name|delta
parameter_list|,
name|long
name|heapOverhead
parameter_list|)
block|{
name|this
operator|.
name|dataSize
operator|.
name|addAndGet
argument_list|(
name|delta
argument_list|)
expr_stmt|;
name|this
operator|.
name|heapOverhead
operator|.
name|addAndGet
argument_list|(
name|heapOverhead
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|incHeapOverheadSize
parameter_list|(
name|long
name|delta
parameter_list|)
block|{
name|this
operator|.
name|heapOverhead
operator|.
name|addAndGet
argument_list|(
name|delta
argument_list|)
expr_stmt|;
block|}
specifier|public
name|long
name|getMinSequenceId
parameter_list|()
block|{
return|return
name|minSequenceId
return|;
block|}
specifier|public
name|TimeRangeTracker
name|getTimeRangeTracker
parameter_list|()
block|{
return|return
name|this
operator|.
name|timeRangeTracker
return|;
block|}
comment|//*** Methods for SegmentsScanner
specifier|public
name|Cell
name|last
parameter_list|()
block|{
return|return
name|getCellSet
argument_list|()
operator|.
name|last
argument_list|()
return|;
block|}
specifier|public
name|Iterator
argument_list|<
name|Cell
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|getCellSet
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
specifier|public
name|SortedSet
argument_list|<
name|Cell
argument_list|>
name|headSet
parameter_list|(
name|Cell
name|firstKeyOnRow
parameter_list|)
block|{
return|return
name|getCellSet
argument_list|()
operator|.
name|headSet
argument_list|(
name|firstKeyOnRow
argument_list|)
return|;
block|}
specifier|public
name|int
name|compare
parameter_list|(
name|Cell
name|left
parameter_list|,
name|Cell
name|right
parameter_list|)
block|{
return|return
name|getComparator
argument_list|()
operator|.
name|compare
argument_list|(
name|left
argument_list|,
name|right
argument_list|)
return|;
block|}
specifier|public
name|int
name|compareRows
parameter_list|(
name|Cell
name|left
parameter_list|,
name|Cell
name|right
parameter_list|)
block|{
return|return
name|getComparator
argument_list|()
operator|.
name|compareRows
argument_list|(
name|left
argument_list|,
name|right
argument_list|)
return|;
block|}
comment|/**    * @return a set of all cells in the segment    */
specifier|protected
name|CellSet
name|getCellSet
parameter_list|()
block|{
return|return
name|cellSet
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * Returns the Cell comparator used by this segment    * @return the Cell comparator used by this segment    */
specifier|protected
name|CellComparator
name|getComparator
parameter_list|()
block|{
return|return
name|comparator
return|;
block|}
specifier|protected
name|void
name|internalAdd
parameter_list|(
name|Cell
name|cell
parameter_list|,
name|boolean
name|mslabUsed
parameter_list|,
name|MemstoreSize
name|memstoreSize
parameter_list|)
block|{
name|boolean
name|succ
init|=
name|getCellSet
argument_list|()
operator|.
name|add
argument_list|(
name|cell
argument_list|)
decl_stmt|;
name|updateMetaInfo
argument_list|(
name|cell
argument_list|,
name|succ
argument_list|,
name|mslabUsed
argument_list|,
name|memstoreSize
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|updateMetaInfo
parameter_list|(
name|Cell
name|cellToAdd
parameter_list|,
name|boolean
name|succ
parameter_list|,
name|boolean
name|mslabUsed
parameter_list|,
name|MemstoreSize
name|memstoreSize
parameter_list|)
block|{
name|long
name|cellSize
init|=
literal|0
decl_stmt|;
comment|// If there's already a same cell in the CellSet and we are using MSLAB, we must count in the
comment|// MSLAB allocation size as well, or else there will be memory leak (occupied heap size larger
comment|// than the counted number)
if|if
condition|(
name|succ
operator|||
name|mslabUsed
condition|)
block|{
name|cellSize
operator|=
name|getCellLength
argument_list|(
name|cellToAdd
argument_list|)
expr_stmt|;
block|}
name|long
name|overhead
init|=
name|heapOverheadChange
argument_list|(
name|cellToAdd
argument_list|,
name|succ
argument_list|)
decl_stmt|;
name|incSize
argument_list|(
name|cellSize
argument_list|,
name|overhead
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
name|cellSize
argument_list|,
name|overhead
argument_list|)
expr_stmt|;
block|}
name|getTimeRangeTracker
argument_list|()
operator|.
name|includeTimestamp
argument_list|(
name|cellToAdd
argument_list|)
expr_stmt|;
name|minSequenceId
operator|=
name|Math
operator|.
name|min
argument_list|(
name|minSequenceId
argument_list|,
name|cellToAdd
operator|.
name|getSequenceId
argument_list|()
argument_list|)
expr_stmt|;
comment|// In no tags case this NoTagsKeyValue.getTagsLength() is a cheap call.
comment|// When we use ACL CP or Visibility CP which deals with Tags during
comment|// mutation, the TagRewriteCell.getTagsLength() is a cheaper call. We do not
comment|// parse the byte[] to identify the tags length.
if|if
condition|(
name|cellToAdd
operator|.
name|getTagsLength
argument_list|()
operator|>
literal|0
condition|)
block|{
name|tagsPresent
operator|=
literal|true
expr_stmt|;
block|}
block|}
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
name|CONCURRENT_SKIPLISTMAP_ENTRY
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
comment|// All cells in server side will be of type ExtendedCell. If not just go with estimation on
comment|// the heap overhead considering it is KeyValue.
return|return
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|CONCURRENT_SKIPLISTMAP_ENTRY
operator|+
name|KeyValue
operator|.
name|FIXED_OVERHEAD
argument_list|)
return|;
block|}
return|return
literal|0
return|;
block|}
comment|/**    * Returns a subset of the segment cell set, which starts with the given cell    * @param firstCell a cell in the segment    * @return a subset of the segment cell set, which starts with the given cell    */
specifier|protected
name|SortedSet
argument_list|<
name|Cell
argument_list|>
name|tailSet
parameter_list|(
name|Cell
name|firstCell
parameter_list|)
block|{
return|return
name|getCellSet
argument_list|()
operator|.
name|tailSet
argument_list|(
name|firstCell
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
name|MemStoreLAB
name|getMemStoreLAB
parameter_list|()
block|{
return|return
name|memStoreLAB
return|;
block|}
comment|// Debug methods
comment|/**    * Dumps all cells of the segment into the given log    */
name|void
name|dump
parameter_list|(
name|Log
name|log
parameter_list|)
block|{
for|for
control|(
name|Cell
name|cell
range|:
name|getCellSet
argument_list|()
control|)
block|{
name|log
operator|.
name|debug
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|res
init|=
literal|"Store segment of type "
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|"; "
decl_stmt|;
name|res
operator|+=
literal|"isEmpty "
operator|+
operator|(
name|isEmpty
argument_list|()
condition|?
literal|"yes"
else|:
literal|"no"
operator|)
operator|+
literal|"; "
expr_stmt|;
name|res
operator|+=
literal|"cellCount "
operator|+
name|getCellsCount
argument_list|()
operator|+
literal|"; "
expr_stmt|;
name|res
operator|+=
literal|"cellsSize "
operator|+
name|keySize
argument_list|()
operator|+
literal|"; "
expr_stmt|;
name|res
operator|+=
literal|"heapOverhead "
operator|+
name|heapOverhead
argument_list|()
operator|+
literal|"; "
expr_stmt|;
name|res
operator|+=
literal|"Min ts "
operator|+
name|getMinTimestamp
argument_list|()
operator|+
literal|"; "
expr_stmt|;
return|return
name|res
return|;
block|}
block|}
end_class

end_unit

