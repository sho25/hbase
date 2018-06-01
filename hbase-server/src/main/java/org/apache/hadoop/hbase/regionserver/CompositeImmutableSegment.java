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
name|io
operator|.
name|TimeRange
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
comment|/**  * The CompositeImmutableSegments is created as a collection of ImmutableSegments and supports  * the interface of a single ImmutableSegments.  * The CompositeImmutableSegments is planned to be used only as a snapshot,  * thus only relevant interfaces are supported  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CompositeImmutableSegment
extends|extends
name|ImmutableSegment
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|ImmutableSegment
argument_list|>
name|segments
decl_stmt|;
specifier|private
name|long
name|keySize
init|=
literal|0
decl_stmt|;
specifier|public
name|CompositeImmutableSegment
parameter_list|(
name|CellComparator
name|comparator
parameter_list|,
name|List
argument_list|<
name|ImmutableSegment
argument_list|>
name|segments
parameter_list|)
block|{
name|super
argument_list|(
name|comparator
argument_list|,
name|segments
argument_list|)
expr_stmt|;
name|this
operator|.
name|segments
operator|=
name|segments
expr_stmt|;
for|for
control|(
name|ImmutableSegment
name|s
range|:
name|segments
control|)
block|{
name|this
operator|.
name|timeRangeTracker
operator|.
name|includeTimestamp
argument_list|(
name|s
operator|.
name|getTimeRangeTracker
argument_list|()
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|timeRangeTracker
operator|.
name|includeTimestamp
argument_list|(
name|s
operator|.
name|getTimeRangeTracker
argument_list|()
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|keySize
operator|+=
name|s
operator|.
name|getDataSize
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|VisibleForTesting
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Segment
argument_list|>
name|getAllSegments
parameter_list|()
block|{
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|segments
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getNumOfSegments
parameter_list|()
block|{
return|return
name|segments
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**    * @return whether the segment has any cells    */
annotation|@
name|Override
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
for|for
control|(
name|ImmutableSegment
name|s
range|:
name|segments
control|)
block|{
if|if
condition|(
operator|!
name|s
operator|.
name|isEmpty
argument_list|()
condition|)
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
comment|/**    * @return number of cells in segment    */
annotation|@
name|Override
specifier|public
name|int
name|getCellsCount
parameter_list|()
block|{
name|int
name|result
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ImmutableSegment
name|s
range|:
name|segments
control|)
block|{
name|result
operator|+=
name|s
operator|.
name|getCellsCount
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * Closing a segment before it is being discarded    */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
for|for
control|(
name|ImmutableSegment
name|s
range|:
name|segments
control|)
block|{
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * If the segment has a memory allocator the cell is being cloned to this space, and returned;    * otherwise the given cell is returned    * @return either the given cell or its clone    */
annotation|@
name|Override
specifier|public
name|Cell
name|maybeCloneWithAllocator
parameter_list|(
name|Cell
name|cell
parameter_list|,
name|boolean
name|forceCloneOfBigCell
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Not supported by CompositeImmutableScanner"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|shouldSeek
parameter_list|(
name|TimeRange
name|tr
parameter_list|,
name|long
name|oldestUnexpiredTS
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Not supported by CompositeImmutableScanner"
argument_list|)
throw|;
block|}
comment|/**    * Creates the scanner for the given read point    * @return a scanner for the given read point    */
annotation|@
name|Override
specifier|public
name|KeyValueScanner
name|getScanner
parameter_list|(
name|long
name|readPoint
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Not supported by CompositeImmutableScanner"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|getScanners
parameter_list|(
name|long
name|readPoint
parameter_list|)
block|{
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|segments
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|AbstractMemStore
operator|.
name|addToScanners
argument_list|(
name|segments
argument_list|,
name|readPoint
argument_list|,
name|list
argument_list|)
expr_stmt|;
return|return
name|list
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isTagsPresent
parameter_list|()
block|{
for|for
control|(
name|ImmutableSegment
name|s
range|:
name|segments
control|)
block|{
if|if
condition|(
name|s
operator|.
name|isTagsPresent
argument_list|()
condition|)
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incScannerCount
parameter_list|()
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Not supported by CompositeImmutableScanner"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|decScannerCount
parameter_list|()
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Not supported by CompositeImmutableScanner"
argument_list|)
throw|;
block|}
comment|/**    * Setting the CellSet of the segment - used only for flat immutable segment for setting    * immutable CellSet after its creation in immutable segment constructor    * @return this object    */
annotation|@
name|Override
specifier|protected
name|CompositeImmutableSegment
name|setCellSet
parameter_list|(
name|CellSet
name|cellSetOld
parameter_list|,
name|CellSet
name|cellSetNew
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Not supported by CompositeImmutableScanner"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|protected
name|long
name|indexEntrySize
parameter_list|()
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Not supported by CompositeImmutableScanner"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|canBeFlattened
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**    * @return Sum of all cell sizes.    */
annotation|@
name|Override
specifier|public
name|long
name|getDataSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|keySize
return|;
block|}
comment|/**    * @return The heap size of this segment.    */
annotation|@
name|Override
specifier|public
name|long
name|getHeapSize
parameter_list|()
block|{
name|long
name|result
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ImmutableSegment
name|s
range|:
name|segments
control|)
block|{
name|result
operator|+=
name|s
operator|.
name|getHeapSize
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * Updates the heap size counter of the segment by the given delta    */
annotation|@
name|Override
specifier|public
name|long
name|incMemStoreSize
parameter_list|(
name|long
name|delta
parameter_list|,
name|long
name|heapOverhead
parameter_list|,
name|long
name|offHeapOverhead
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Not supported by CompositeImmutableScanner"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMinSequenceId
parameter_list|()
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Not supported by CompositeImmutableScanner"
argument_list|)
throw|;
block|}
annotation|@
name|Override
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
annotation|@
name|Override
specifier|public
name|Cell
name|last
parameter_list|()
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Not supported by CompositeImmutableScanner"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|Cell
argument_list|>
name|iterator
parameter_list|()
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Not supported by CompositeImmutableScanner"
argument_list|)
throw|;
block|}
annotation|@
name|Override
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
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Not supported by CompositeImmutableScanner"
argument_list|)
throw|;
block|}
annotation|@
name|Override
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
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Not supported by CompositeImmutableScanner"
argument_list|)
throw|;
block|}
annotation|@
name|Override
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
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Not supported by CompositeImmutableScanner"
argument_list|)
throw|;
block|}
comment|/**    * @return a set of all cells in the segment    */
annotation|@
name|Override
specifier|protected
name|CellSet
name|getCellSet
parameter_list|()
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Not supported by CompositeImmutableScanner"
argument_list|)
throw|;
block|}
annotation|@
name|Override
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
name|MemStoreSizing
name|memstoreSizing
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Not supported by CompositeImmutableScanner"
argument_list|)
throw|;
block|}
annotation|@
name|Override
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
name|MemStoreSizing
name|memstoreSizing
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Not supported by CompositeImmutableScanner"
argument_list|)
throw|;
block|}
comment|/**    * Returns a subset of the segment cell set, which starts with the given cell    * @param firstCell a cell in the segment    * @return a subset of the segment cell set, which starts with the given cell    */
annotation|@
name|Override
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
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Not supported by CompositeImmutableScanner"
argument_list|)
throw|;
block|}
comment|// Debug methods
comment|/**    * Dumps all cells of the segment into the given log    */
annotation|@
name|Override
name|void
name|dump
parameter_list|(
name|Logger
name|log
parameter_list|)
block|{
for|for
control|(
name|ImmutableSegment
name|s
range|:
name|segments
control|)
block|{
name|s
operator|.
name|dump
argument_list|(
name|log
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
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"This is CompositeImmutableSegment and those are its segments:: "
argument_list|)
decl_stmt|;
for|for
control|(
name|ImmutableSegment
name|s
range|:
name|segments
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|s
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|getSnapshotScanners
parameter_list|()
block|{
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|this
operator|.
name|segments
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|ImmutableSegment
name|segment
range|:
name|this
operator|.
name|segments
control|)
block|{
name|list
operator|.
name|add
argument_list|(
operator|new
name|SnapshotSegmentScanner
argument_list|(
name|segment
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|list
return|;
block|}
block|}
end_class

end_unit

