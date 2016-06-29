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
name|NavigableSet
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
name|CellUtil
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

begin_comment
comment|/**  * An abstract class, which implements the behaviour shared by all concrete memstore instances.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|AbstractMemStore
implements|implements
name|MemStore
block|{
specifier|private
specifier|static
specifier|final
name|long
name|NO_SNAPSHOT_ID
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|CellComparator
name|comparator
decl_stmt|;
comment|// active segment absorbs write operations
specifier|private
specifier|volatile
name|MutableSegment
name|active
decl_stmt|;
comment|// Snapshot of memstore.  Made for flusher.
specifier|private
specifier|volatile
name|ImmutableSegment
name|snapshot
decl_stmt|;
specifier|protected
specifier|volatile
name|long
name|snapshotId
decl_stmt|;
comment|// Used to track when to flush
specifier|private
specifier|volatile
name|long
name|timeOfOldestEdit
decl_stmt|;
specifier|public
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
operator|(
literal|4
operator|*
name|ClassSize
operator|.
name|REFERENCE
operator|)
operator|+
operator|(
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_LONG
operator|)
argument_list|)
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|long
name|DEEP_OVERHEAD
init|=
name|ClassSize
operator|.
name|align
argument_list|(
name|FIXED_OVERHEAD
operator|+
operator|(
name|ClassSize
operator|.
name|ATOMIC_LONG
operator|+
name|ClassSize
operator|.
name|TIMERANGE_TRACKER
operator|+
name|ClassSize
operator|.
name|CELL_SKIPLIST_SET
operator|+
name|ClassSize
operator|.
name|CONCURRENT_SKIPLISTMAP
operator|)
argument_list|)
decl_stmt|;
specifier|protected
name|AbstractMemStore
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|CellComparator
name|c
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|comparator
operator|=
name|c
expr_stmt|;
name|resetCellSet
argument_list|()
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
name|conf
argument_list|,
name|c
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|snapshotId
operator|=
name|NO_SNAPSHOT_ID
expr_stmt|;
block|}
specifier|protected
name|void
name|resetCellSet
parameter_list|()
block|{
comment|// Reset heap to not include any keys
name|this
operator|.
name|active
operator|=
name|SegmentFactory
operator|.
name|instance
argument_list|()
operator|.
name|createMutableSegment
argument_list|(
name|conf
argument_list|,
name|comparator
argument_list|,
name|DEEP_OVERHEAD
argument_list|)
expr_stmt|;
name|this
operator|.
name|timeOfOldestEdit
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
comment|/*   * Calculate how the MemStore size has changed.  Includes overhead of the   * backing Map.   * @param cell   * @param notPresent True if the cell was NOT present in the set.   * @return change in size   */
specifier|static
name|long
name|heapSizeChange
parameter_list|(
specifier|final
name|Cell
name|cell
parameter_list|,
specifier|final
name|boolean
name|notPresent
parameter_list|)
block|{
return|return
name|notPresent
condition|?
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|CONCURRENT_SKIPLISTMAP_ENTRY
operator|+
name|CellUtil
operator|.
name|estimatedHeapSizeOf
argument_list|(
name|cell
argument_list|)
argument_list|)
else|:
literal|0
return|;
block|}
comment|/**    * Updates the wal with the lowest sequence id (oldest entry) that is still in memory    * @param onlyIfMoreRecent a flag that marks whether to update the sequence id no matter what or    *                      only if it is greater than the previous sequence id    */
specifier|public
specifier|abstract
name|void
name|updateLowestUnflushedSequenceIdInWAL
parameter_list|(
name|boolean
name|onlyIfMoreRecent
parameter_list|)
function_decl|;
comment|/**    * Write an update    * @param cell the cell to be added    * @return approximate size of the passed cell& newly added cell which maybe different than the    *         passed-in cell    */
annotation|@
name|Override
specifier|public
name|long
name|add
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|Cell
name|toAdd
init|=
name|maybeCloneWithAllocator
argument_list|(
name|cell
argument_list|)
decl_stmt|;
return|return
name|internalAdd
argument_list|(
name|toAdd
argument_list|)
return|;
block|}
comment|/**    * Update or insert the specified Cells.    *<p>    * For each Cell, insert into MemStore.  This will atomically upsert the    * value for that row/family/qualifier.  If a Cell did already exist,    * it will then be removed.    *<p>    * Currently the memstoreTS is kept at 0 so as each insert happens, it will    * be immediately visible.  May want to change this so it is atomic across    * all Cells.    *<p>    * This is called under row lock, so Get operations will still see updates    * atomically.  Scans will only see each Cell update as atomic.    *    * @param cells the cells to be updated    * @param readpoint readpoint below which we can safely remove duplicate KVs    * @return change in memstore size    */
annotation|@
name|Override
specifier|public
name|long
name|upsert
parameter_list|(
name|Iterable
argument_list|<
name|Cell
argument_list|>
name|cells
parameter_list|,
name|long
name|readpoint
parameter_list|)
block|{
name|long
name|size
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
name|size
operator|+=
name|upsert
argument_list|(
name|cell
argument_list|,
name|readpoint
argument_list|)
expr_stmt|;
block|}
return|return
name|size
return|;
block|}
comment|/**    * @return Oldest timestamp of all the Cells in the MemStore    */
annotation|@
name|Override
specifier|public
name|long
name|timeOfOldestEdit
parameter_list|()
block|{
return|return
name|timeOfOldestEdit
return|;
block|}
comment|/**    * Write a delete    * @param deleteCell the cell to be deleted    * @return approximate size of the passed key and value.    */
annotation|@
name|Override
specifier|public
name|long
name|delete
parameter_list|(
name|Cell
name|deleteCell
parameter_list|)
block|{
name|Cell
name|toAdd
init|=
name|maybeCloneWithAllocator
argument_list|(
name|deleteCell
argument_list|)
decl_stmt|;
name|long
name|s
init|=
name|internalAdd
argument_list|(
name|toAdd
argument_list|)
decl_stmt|;
return|return
name|s
return|;
block|}
comment|/**    * The passed snapshot was successfully persisted; it can be let go.    * @param id Id of the snapshot to clean out.    * @see MemStore#snapshot()    */
annotation|@
name|Override
specifier|public
name|void
name|clearSnapshot
parameter_list|(
name|long
name|id
parameter_list|)
throws|throws
name|UnexpectedStateException
block|{
if|if
condition|(
name|this
operator|.
name|snapshotId
operator|!=
name|id
condition|)
block|{
throw|throw
operator|new
name|UnexpectedStateException
argument_list|(
literal|"Current snapshot id is "
operator|+
name|this
operator|.
name|snapshotId
operator|+
literal|",passed "
operator|+
name|id
argument_list|)
throw|;
block|}
comment|// OK. Passed in snapshot is same as current snapshot. If not-empty,
comment|// create a new snapshot and let the old one go.
name|Segment
name|oldSnapshot
init|=
name|this
operator|.
name|snapshot
decl_stmt|;
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
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|snapshotId
operator|=
name|NO_SNAPSHOT_ID
expr_stmt|;
name|oldSnapshot
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Get the entire heap usage for this MemStore not including keys in the    * snapshot.    */
annotation|@
name|Override
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
return|return
name|getActive
argument_list|()
operator|.
name|getSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getSnapshotSize
parameter_list|()
block|{
return|return
name|getSnapshot
argument_list|()
operator|.
name|getSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuffer
name|buf
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
name|int
name|i
init|=
literal|1
decl_stmt|;
try|try
block|{
for|for
control|(
name|Segment
name|segment
range|:
name|getSegments
argument_list|()
control|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|"Segment ("
operator|+
name|i
operator|+
literal|") "
operator|+
name|segment
operator|.
name|toString
argument_list|()
operator|+
literal|"; "
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
name|e
operator|.
name|toString
argument_list|()
return|;
block|}
return|return
name|buf
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|protected
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
specifier|protected
name|void
name|dump
parameter_list|(
name|Log
name|log
parameter_list|)
block|{
name|active
operator|.
name|dump
argument_list|(
name|log
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|dump
argument_list|(
name|log
argument_list|)
expr_stmt|;
block|}
comment|/**    * Inserts the specified Cell into MemStore and deletes any existing    * versions of the same row/family/qualifier as the specified Cell.    *<p>    * First, the specified Cell is inserted into the Memstore.    *<p>    * If there are any existing Cell in this MemStore with the same row,    * family, and qualifier, they are removed.    *<p>    * Callers must hold the read lock.    *    * @param cell the cell to be updated    * @param readpoint readpoint below which we can safely remove duplicate KVs    * @return change in size of MemStore    */
specifier|private
name|long
name|upsert
parameter_list|(
name|Cell
name|cell
parameter_list|,
name|long
name|readpoint
parameter_list|)
block|{
comment|// Add the Cell to the MemStore
comment|// Use the internalAdd method here since we (a) already have a lock
comment|// and (b) cannot safely use the MSLAB here without potentially
comment|// hitting OOME - see TestMemStore.testUpsertMSLAB for a
comment|// test that triggers the pathological case if we don't avoid MSLAB
comment|// here.
name|long
name|addedSize
init|=
name|internalAdd
argument_list|(
name|cell
argument_list|)
decl_stmt|;
comment|// Get the Cells for the row/family/qualifier regardless of timestamp.
comment|// For this case we want to clean up any other puts
name|Cell
name|firstCell
init|=
name|KeyValueUtil
operator|.
name|createFirstOnRow
argument_list|(
name|cell
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyLength
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
decl_stmt|;
name|SortedSet
argument_list|<
name|Cell
argument_list|>
name|ss
init|=
name|active
operator|.
name|tailSet
argument_list|(
name|firstCell
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|Cell
argument_list|>
name|it
init|=
name|ss
operator|.
name|iterator
argument_list|()
decl_stmt|;
comment|// versions visible to oldest scanner
name|int
name|versionsVisible
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Cell
name|cur
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|cell
operator|==
name|cur
condition|)
block|{
comment|// ignore the one just put in
continue|continue;
block|}
comment|// check that this is the row and column we are interested in, otherwise bail
if|if
condition|(
name|CellUtil
operator|.
name|matchingRow
argument_list|(
name|cell
argument_list|,
name|cur
argument_list|)
operator|&&
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|cell
argument_list|,
name|cur
argument_list|)
condition|)
block|{
comment|// only remove Puts that concurrent scanners cannot possibly see
if|if
condition|(
name|cur
operator|.
name|getTypeByte
argument_list|()
operator|==
name|KeyValue
operator|.
name|Type
operator|.
name|Put
operator|.
name|getCode
argument_list|()
operator|&&
name|cur
operator|.
name|getSequenceId
argument_list|()
operator|<=
name|readpoint
condition|)
block|{
if|if
condition|(
name|versionsVisible
operator|>=
literal|1
condition|)
block|{
comment|// if we get here we have seen at least one version visible to the oldest scanner,
comment|// which means we can prove that no scanner will see this version
comment|// false means there was a change, so give us the size.
name|long
name|delta
init|=
name|heapSizeChange
argument_list|(
name|cur
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|addedSize
operator|-=
name|delta
expr_stmt|;
name|active
operator|.
name|incSize
argument_list|(
operator|-
name|delta
argument_list|)
expr_stmt|;
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
name|setOldestEditTimeToNow
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|versionsVisible
operator|++
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
comment|// past the row or column, done
break|break;
block|}
block|}
return|return
name|addedSize
return|;
block|}
comment|/*    * @param a    * @param b    * @return Return lowest of a or b or null if both a and b are null    */
specifier|protected
name|Cell
name|getLowest
parameter_list|(
specifier|final
name|Cell
name|a
parameter_list|,
specifier|final
name|Cell
name|b
parameter_list|)
block|{
if|if
condition|(
name|a
operator|==
literal|null
condition|)
block|{
return|return
name|b
return|;
block|}
if|if
condition|(
name|b
operator|==
literal|null
condition|)
block|{
return|return
name|a
return|;
block|}
return|return
name|comparator
operator|.
name|compareRows
argument_list|(
name|a
argument_list|,
name|b
argument_list|)
operator|<=
literal|0
condition|?
name|a
else|:
name|b
return|;
block|}
comment|/*    * @param key Find row that follows this one.  If null, return first.    * @param set Set to look in for a row beyond<code>row</code>.    * @return Next row or null if none found.  If one found, will be a new    * KeyValue -- can be destroyed by subsequent calls to this method.    */
specifier|protected
name|Cell
name|getNextRow
parameter_list|(
specifier|final
name|Cell
name|key
parameter_list|,
specifier|final
name|NavigableSet
argument_list|<
name|Cell
argument_list|>
name|set
parameter_list|)
block|{
name|Cell
name|result
init|=
literal|null
decl_stmt|;
name|SortedSet
argument_list|<
name|Cell
argument_list|>
name|tail
init|=
name|key
operator|==
literal|null
condition|?
name|set
else|:
name|set
operator|.
name|tailSet
argument_list|(
name|key
argument_list|)
decl_stmt|;
comment|// Iterate until we fall into the next row; i.e. move off current row
for|for
control|(
name|Cell
name|cell
range|:
name|tail
control|)
block|{
if|if
condition|(
name|comparator
operator|.
name|compareRows
argument_list|(
name|cell
argument_list|,
name|key
argument_list|)
operator|<=
literal|0
condition|)
block|{
continue|continue;
block|}
comment|// Note: Not suppressing deletes or expired cells.  Needs to be handled
comment|// by higher up functions.
name|result
operator|=
name|cell
expr_stmt|;
break|break;
block|}
return|return
name|result
return|;
block|}
comment|/**    * Given the specs of a column, update it, first by inserting a new record,    * then removing the old one.  Since there is only 1 KeyValue involved, the memstoreTS    * will be set to 0, thus ensuring that they instantly appear to anyone. The underlying    * store will ensure that the insert/delete each are atomic. A scanner/reader will either    * get the new value, or the old value and all readers will eventually only see the new    * value after the old was removed.    */
annotation|@
name|VisibleForTesting
annotation|@
name|Override
specifier|public
name|long
name|updateColumnValue
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|long
name|newValue
parameter_list|,
name|long
name|now
parameter_list|)
block|{
name|Cell
name|firstCell
init|=
name|KeyValueUtil
operator|.
name|createFirstOnRow
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
comment|// Is there a Cell in 'snapshot' with the same TS? If so, upgrade the timestamp a bit.
name|Cell
name|snc
init|=
name|snapshot
operator|.
name|getFirstAfter
argument_list|(
name|firstCell
argument_list|)
decl_stmt|;
if|if
condition|(
name|snc
operator|!=
literal|null
condition|)
block|{
comment|// is there a matching Cell in the snapshot?
if|if
condition|(
name|CellUtil
operator|.
name|matchingRow
argument_list|(
name|snc
argument_list|,
name|firstCell
argument_list|)
operator|&&
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|snc
argument_list|,
name|firstCell
argument_list|)
condition|)
block|{
if|if
condition|(
name|snc
operator|.
name|getTimestamp
argument_list|()
operator|==
name|now
condition|)
block|{
name|now
operator|+=
literal|1
expr_stmt|;
block|}
block|}
block|}
comment|// logic here: the new ts MUST be at least 'now'. But it could be larger if necessary.
comment|// But the timestamp should also be max(now, mostRecentTsInMemstore)
comment|// so we cant add the new Cell w/o knowing what's there already, but we also
comment|// want to take this chance to delete some cells. So two loops (sad)
name|SortedSet
argument_list|<
name|Cell
argument_list|>
name|ss
init|=
name|getActive
argument_list|()
operator|.
name|tailSet
argument_list|(
name|firstCell
argument_list|)
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|ss
control|)
block|{
comment|// if this isnt the row we are interested in, then bail:
if|if
condition|(
operator|!
name|CellUtil
operator|.
name|matchingColumn
argument_list|(
name|cell
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|)
operator|||
operator|!
name|CellUtil
operator|.
name|matchingRow
argument_list|(
name|cell
argument_list|,
name|firstCell
argument_list|)
condition|)
block|{
break|break;
comment|// rows dont match, bail.
block|}
comment|// if the qualifier matches and it's a put, just RM it out of the active.
if|if
condition|(
name|cell
operator|.
name|getTypeByte
argument_list|()
operator|==
name|KeyValue
operator|.
name|Type
operator|.
name|Put
operator|.
name|getCode
argument_list|()
operator|&&
name|cell
operator|.
name|getTimestamp
argument_list|()
operator|>
name|now
operator|&&
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|firstCell
argument_list|,
name|cell
argument_list|)
condition|)
block|{
name|now
operator|=
name|cell
operator|.
name|getTimestamp
argument_list|()
expr_stmt|;
block|}
block|}
comment|// create or update (upsert) a new Cell with
comment|// 'now' and a 0 memstoreTS == immediately visible
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|cells
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|now
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|newValue
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|upsert
argument_list|(
name|cells
argument_list|,
literal|1L
argument_list|)
return|;
block|}
specifier|private
name|Cell
name|maybeCloneWithAllocator
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
return|return
name|active
operator|.
name|maybeCloneWithAllocator
argument_list|(
name|cell
argument_list|)
return|;
block|}
comment|/**    * Internal version of add() that doesn't clone Cells with the    * allocator, and doesn't take the lock.    *    * Callers should ensure they already have the read lock taken    */
specifier|private
name|long
name|internalAdd
parameter_list|(
specifier|final
name|Cell
name|toAdd
parameter_list|)
block|{
name|long
name|s
init|=
name|active
operator|.
name|add
argument_list|(
name|toAdd
argument_list|)
decl_stmt|;
name|setOldestEditTimeToNow
argument_list|()
expr_stmt|;
name|checkActiveSize
argument_list|()
expr_stmt|;
return|return
name|s
return|;
block|}
specifier|private
name|void
name|setOldestEditTimeToNow
parameter_list|()
block|{
if|if
condition|(
name|timeOfOldestEdit
operator|==
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
name|timeOfOldestEdit
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
name|long
name|keySize
parameter_list|()
block|{
return|return
name|heapSize
argument_list|()
operator|-
name|DEEP_OVERHEAD
return|;
block|}
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
name|MutableSegment
name|getActive
parameter_list|()
block|{
return|return
name|active
return|;
block|}
specifier|protected
name|ImmutableSegment
name|getSnapshot
parameter_list|()
block|{
return|return
name|snapshot
return|;
block|}
specifier|protected
name|AbstractMemStore
name|setSnapshot
parameter_list|(
name|ImmutableSegment
name|snapshot
parameter_list|)
block|{
name|this
operator|.
name|snapshot
operator|=
name|snapshot
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|protected
name|void
name|setSnapshotSize
parameter_list|(
name|long
name|snapshotSize
parameter_list|)
block|{
name|getSnapshot
argument_list|()
operator|.
name|setSize
argument_list|(
name|snapshotSize
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check whether anything need to be done based on the current active set size    */
specifier|protected
specifier|abstract
name|void
name|checkActiveSize
parameter_list|()
function_decl|;
comment|/**    * Returns an ordered list of segments from most recent to oldest in memstore    * @return an ordered list of segments from most recent to oldest in memstore    */
specifier|protected
specifier|abstract
name|List
argument_list|<
name|Segment
argument_list|>
name|getSegments
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit

