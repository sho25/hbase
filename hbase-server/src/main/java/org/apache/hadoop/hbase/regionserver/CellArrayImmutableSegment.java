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
name|CellUtil
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * CellArrayImmutableSegment extends the API supported by a {@link Segment},  * and {@link ImmutableSegment}. This immutable segment is working with CellSet with  * CellArrayMap delegatee.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CellArrayImmutableSegment
extends|extends
name|ImmutableSegment
block|{
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
comment|/////////////////////  CONSTRUCTORS  /////////////////////
comment|/**------------------------------------------------------------------------    * C-tor to be used when new CellArrayImmutableSegment is a result of compaction of a    * list of older ImmutableSegments.    * The given iterator returns the Cells that "survived" the compaction.    */
specifier|protected
name|CellArrayImmutableSegment
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
name|MemStoreCompactionStrategy
operator|.
name|Action
name|action
parameter_list|)
block|{
name|super
argument_list|(
literal|null
argument_list|,
name|comparator
argument_list|,
name|memStoreLAB
argument_list|)
expr_stmt|;
comment|// initiailize the CellSet with NULL
name|incSize
argument_list|(
literal|0
argument_list|,
name|DEEP_OVERHEAD_CAM
argument_list|)
expr_stmt|;
comment|// build the new CellSet based on CellArrayMap and update the CellSet of the new Segment
name|initializeCellSet
argument_list|(
name|numOfCells
argument_list|,
name|iterator
argument_list|,
name|action
argument_list|)
expr_stmt|;
block|}
comment|/**------------------------------------------------------------------------    * C-tor to be used when new CellChunkImmutableSegment is built as a result of flattening    * of CSLMImmutableSegment    * The given iterator returns the Cells that "survived" the compaction.    */
specifier|protected
name|CellArrayImmutableSegment
parameter_list|(
name|CSLMImmutableSegment
name|segment
parameter_list|,
name|MemStoreSizing
name|memstoreSizing
parameter_list|,
name|MemStoreCompactionStrategy
operator|.
name|Action
name|action
parameter_list|)
block|{
name|super
argument_list|(
name|segment
argument_list|)
expr_stmt|;
comment|// initiailize the upper class
name|incSize
argument_list|(
literal|0
argument_list|,
name|DEEP_OVERHEAD_CAM
operator|-
name|CSLMImmutableSegment
operator|.
name|DEEP_OVERHEAD_CSLM
argument_list|)
expr_stmt|;
name|int
name|numOfCells
init|=
name|segment
operator|.
name|getCellsCount
argument_list|()
decl_stmt|;
comment|// build the new CellSet based on CellChunkMap and update the CellSet of this Segment
name|reinitializeCellSet
argument_list|(
name|numOfCells
argument_list|,
name|segment
operator|.
name|getScanner
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
argument_list|,
name|segment
operator|.
name|getCellSet
argument_list|()
argument_list|,
name|action
argument_list|)
expr_stmt|;
comment|// arrange the meta-data size, decrease all meta-data sizes related to SkipList;
comment|// add sizes of CellArrayMap entry (reinitializeCellSet doesn't take the care for the sizes)
name|long
name|newSegmentSizeDelta
init|=
name|numOfCells
operator|*
operator|(
name|indexEntrySize
argument_list|()
operator|-
name|ClassSize
operator|.
name|CONCURRENT_SKIPLISTMAP_ENTRY
operator|)
decl_stmt|;
name|incSize
argument_list|(
literal|0
argument_list|,
name|newSegmentSizeDelta
argument_list|)
expr_stmt|;
name|memstoreSizing
operator|.
name|incMemStoreSize
argument_list|(
literal|0
argument_list|,
name|newSegmentSizeDelta
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|long
name|indexEntrySize
parameter_list|()
block|{
return|return
name|ClassSize
operator|.
name|CELL_ARRAY_MAP_ENTRY
return|;
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
comment|/////////////////////  PRIVATE METHODS  /////////////////////
comment|/*------------------------------------------------------------------------*/
comment|// Create CellSet based on CellArrayMap from compacting iterator
specifier|private
name|void
name|initializeCellSet
parameter_list|(
name|int
name|numOfCells
parameter_list|,
name|MemStoreSegmentsIterator
name|iterator
parameter_list|,
name|MemStoreCompactionStrategy
operator|.
name|Action
name|action
parameter_list|)
block|{
name|boolean
name|merge
init|=
operator|(
name|action
operator|==
name|MemStoreCompactionStrategy
operator|.
name|Action
operator|.
name|MERGE
operator|||
name|action
operator|==
name|MemStoreCompactionStrategy
operator|.
name|Action
operator|.
name|MERGE_COUNT_UNIQUE_KEYS
operator|)
decl_stmt|;
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
name|int
name|numUniqueKeys
init|=
literal|0
decl_stmt|;
name|Cell
name|prev
init|=
literal|null
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
comment|// second parameter true, because in compaction/merge the addition of the cell to new segment
comment|// is always successful
name|updateMetaInfo
argument_list|(
name|c
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// updates the size per cell
if|if
condition|(
name|action
operator|==
name|MemStoreCompactionStrategy
operator|.
name|Action
operator|.
name|MERGE_COUNT_UNIQUE_KEYS
condition|)
block|{
comment|//counting number of unique keys
if|if
condition|(
name|prev
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|CellUtil
operator|.
name|matchingRowColumnBytes
argument_list|(
name|prev
argument_list|,
name|c
argument_list|)
condition|)
block|{
name|numUniqueKeys
operator|++
expr_stmt|;
block|}
block|}
else|else
block|{
name|numUniqueKeys
operator|++
expr_stmt|;
block|}
block|}
name|prev
operator|=
name|c
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|action
operator|==
name|MemStoreCompactionStrategy
operator|.
name|Action
operator|.
name|COMPACT
condition|)
block|{
name|numUniqueKeys
operator|=
name|numOfCells
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|action
operator|!=
name|MemStoreCompactionStrategy
operator|.
name|Action
operator|.
name|MERGE_COUNT_UNIQUE_KEYS
condition|)
block|{
name|numUniqueKeys
operator|=
name|CellSet
operator|.
name|UNKNOWN_NUM_UNIQUES
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
name|this
operator|.
name|setCellSet
argument_list|(
literal|null
argument_list|,
operator|new
name|CellSet
argument_list|(
name|cam
argument_list|,
name|numUniqueKeys
argument_list|)
argument_list|)
expr_stmt|;
comment|// update the CellSet of this Segment
block|}
comment|/*------------------------------------------------------------------------*/
comment|// Create CellSet based on CellChunkMap from current ConcurrentSkipListMap based CellSet
comment|// (without compacting iterator)
comment|// We do not consider cells bigger than chunks!
specifier|private
name|void
name|reinitializeCellSet
parameter_list|(
name|int
name|numOfCells
parameter_list|,
name|KeyValueScanner
name|segmentScanner
parameter_list|,
name|CellSet
name|oldCellSet
parameter_list|,
name|MemStoreCompactionStrategy
operator|.
name|Action
name|action
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
name|int
name|numUniqueKeys
init|=
literal|0
decl_stmt|;
name|Cell
name|prev
init|=
literal|null
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
if|if
condition|(
name|action
operator|==
name|MemStoreCompactionStrategy
operator|.
name|Action
operator|.
name|FLATTEN_COUNT_UNIQUE_KEYS
condition|)
block|{
comment|//counting number of unique keys
if|if
condition|(
name|prev
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|CellUtil
operator|.
name|matchingRowColumn
argument_list|(
name|prev
argument_list|,
name|curCell
argument_list|)
condition|)
block|{
name|numUniqueKeys
operator|++
expr_stmt|;
block|}
block|}
else|else
block|{
name|numUniqueKeys
operator|++
expr_stmt|;
block|}
block|}
name|prev
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
if|if
condition|(
name|action
operator|!=
name|MemStoreCompactionStrategy
operator|.
name|Action
operator|.
name|FLATTEN_COUNT_UNIQUE_KEYS
condition|)
block|{
name|numUniqueKeys
operator|=
name|CellSet
operator|.
name|UNKNOWN_NUM_UNIQUES
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
comment|// update the CellSet of this Segment
name|this
operator|.
name|setCellSet
argument_list|(
name|oldCellSet
argument_list|,
operator|new
name|CellSet
argument_list|(
name|cam
argument_list|,
name|numUniqueKeys
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

