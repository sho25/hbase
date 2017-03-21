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
name|Iterator
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
name|lang
operator|.
name|NotImplementedException
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

begin_comment
comment|/**  * A scanner of a single memstore segment.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SegmentScanner
implements|implements
name|KeyValueScanner
block|{
comment|/**    * Order of this scanner relative to other scanners. See    * {@link KeyValueScanner#getScannerOrder()}.    */
specifier|private
name|long
name|scannerOrder
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|DEFAULT_SCANNER_ORDER
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
comment|// the observed structure
specifier|protected
specifier|final
name|Segment
name|segment
decl_stmt|;
comment|// the highest relevant MVCC
specifier|private
name|long
name|readPoint
decl_stmt|;
comment|// the current iterator that can be reinitialized by
comment|// seek(), backwardSeek(), or reseek()
specifier|protected
name|Iterator
argument_list|<
name|Cell
argument_list|>
name|iter
decl_stmt|;
comment|// the pre-calculated cell to be returned by peek()
specifier|protected
name|Cell
name|current
init|=
literal|null
decl_stmt|;
comment|// or next()
comment|// A flag represents whether could stop skipping KeyValues for MVCC
comment|// if have encountered the next row. Only used for reversed scan
specifier|private
name|boolean
name|stopSkippingKVsIfNextRow
init|=
literal|false
decl_stmt|;
comment|// last iterated KVs by seek (to restore the iterator state after reseek)
specifier|private
name|Cell
name|last
init|=
literal|null
decl_stmt|;
comment|// flag to indicate if this scanner is closed
specifier|protected
name|boolean
name|closed
init|=
literal|false
decl_stmt|;
specifier|protected
name|SegmentScanner
parameter_list|(
name|Segment
name|segment
parameter_list|,
name|long
name|readPoint
parameter_list|)
block|{
name|this
argument_list|(
name|segment
argument_list|,
name|readPoint
argument_list|,
name|DEFAULT_SCANNER_ORDER
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param scannerOrder see {@link KeyValueScanner#getScannerOrder()}.    * Scanners are ordered from 0 (oldest) to newest in increasing order.    */
specifier|protected
name|SegmentScanner
parameter_list|(
name|Segment
name|segment
parameter_list|,
name|long
name|readPoint
parameter_list|,
name|long
name|scannerOrder
parameter_list|)
block|{
name|this
operator|.
name|segment
operator|=
name|segment
expr_stmt|;
name|this
operator|.
name|readPoint
operator|=
name|readPoint
expr_stmt|;
comment|//increase the reference count so the underlying structure will not be de-allocated
name|this
operator|.
name|segment
operator|.
name|incScannerCount
argument_list|()
expr_stmt|;
name|iter
operator|=
name|segment
operator|.
name|iterator
argument_list|()
expr_stmt|;
comment|// the initialization of the current is required for working with heap of SegmentScanners
name|updateCurrent
argument_list|()
expr_stmt|;
name|this
operator|.
name|scannerOrder
operator|=
name|scannerOrder
expr_stmt|;
if|if
condition|(
name|current
operator|==
literal|null
condition|)
block|{
comment|// nothing to fetch from this scanner
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Look at the next Cell in this scanner, but do not iterate the scanner    * @return the currently observed Cell    */
annotation|@
name|Override
specifier|public
name|Cell
name|peek
parameter_list|()
block|{
comment|// sanity check, the current should be always valid
if|if
condition|(
name|closed
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|current
operator|!=
literal|null
operator|&&
name|current
operator|.
name|getSequenceId
argument_list|()
operator|>
name|readPoint
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"current is invalid: read point is "
operator|+
name|readPoint
operator|+
literal|", "
operator|+
literal|"while current sequence id is "
operator|+
name|current
operator|.
name|getSequenceId
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|current
return|;
block|}
comment|/**    * Return the next Cell in this scanner, iterating the scanner    * @return the next Cell or null if end of scanner    */
annotation|@
name|Override
specifier|public
name|Cell
name|next
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Cell
name|oldCurrent
init|=
name|current
decl_stmt|;
name|updateCurrent
argument_list|()
expr_stmt|;
comment|// update the currently observed Cell
return|return
name|oldCurrent
return|;
block|}
comment|/**    * Seek the scanner at or after the specified Cell.    * @param cell seek value    * @return true if scanner has values left, false if end of scanner    */
annotation|@
name|Override
specifier|public
name|boolean
name|seek
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|cell
operator|==
literal|null
condition|)
block|{
name|close
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// restart the iterator from new key
name|iter
operator|=
name|getIterator
argument_list|(
name|cell
argument_list|)
expr_stmt|;
comment|// last is going to be reinitialized in the next getNext() call
name|last
operator|=
literal|null
expr_stmt|;
name|updateCurrent
argument_list|()
expr_stmt|;
return|return
operator|(
name|current
operator|!=
literal|null
operator|)
return|;
block|}
specifier|protected
name|Iterator
argument_list|<
name|Cell
argument_list|>
name|getIterator
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
return|return
name|segment
operator|.
name|tailSet
argument_list|(
name|cell
argument_list|)
operator|.
name|iterator
argument_list|()
return|;
block|}
comment|/**    * Reseek the scanner at or after the specified KeyValue.    * This method is guaranteed to seek at or after the required key only if the    * key comes after the current position of the scanner. Should not be used    * to seek to a key which may come before the current position.    *    * @param cell seek value (should be non-null)    * @return true if scanner has values left, false if end of scanner    */
annotation|@
name|Override
specifier|public
name|boolean
name|reseek
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|/*     See HBASE-4195& HBASE-3855& HBASE-6591 for the background on this implementation.     This code is executed concurrently with flush and puts, without locks.     The ideal implementation for performance would use the sub skip list implicitly     pointed by the iterator. Unfortunately the Java API does not offer a method to     get it. So we remember the last keys we iterated to and restore     the reseeked set to at least that point.     */
name|iter
operator|=
name|getIterator
argument_list|(
name|getHighest
argument_list|(
name|cell
argument_list|,
name|last
argument_list|)
argument_list|)
expr_stmt|;
name|updateCurrent
argument_list|()
expr_stmt|;
return|return
operator|(
name|current
operator|!=
literal|null
operator|)
return|;
block|}
comment|/**    * Seek the scanner at or before the row of specified Cell, it firstly    * tries to seek the scanner at or after the specified Cell, return if    * peek KeyValue of scanner has the same row with specified Cell,    * otherwise seek the scanner at the first Cell of the row which is the    * previous row of specified KeyValue    *    * @param key seek Cell    * @return true if the scanner is at the valid KeyValue, false if such Cell does not exist    */
annotation|@
name|Override
specifier|public
name|boolean
name|backwardSeek
parameter_list|(
name|Cell
name|key
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return
literal|false
return|;
block|}
name|seek
argument_list|(
name|key
argument_list|)
expr_stmt|;
comment|// seek forward then go backward
if|if
condition|(
name|peek
argument_list|()
operator|==
literal|null
operator|||
name|segment
operator|.
name|compareRows
argument_list|(
name|peek
argument_list|()
argument_list|,
name|key
argument_list|)
operator|>
literal|0
condition|)
block|{
return|return
name|seekToPreviousRow
argument_list|(
name|key
argument_list|)
return|;
block|}
return|return
literal|true
return|;
block|}
comment|/**    * Seek the scanner at the first Cell of the row which is the previous row    * of specified key    *    * @param cell seek value    * @return true if the scanner at the first valid Cell of previous row,    *     false if not existing such Cell    */
annotation|@
name|Override
specifier|public
name|boolean
name|seekToPreviousRow
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return
literal|false
return|;
block|}
name|boolean
name|keepSeeking
decl_stmt|;
name|Cell
name|key
init|=
name|cell
decl_stmt|;
do|do
block|{
name|Cell
name|firstKeyOnRow
init|=
name|CellUtil
operator|.
name|createFirstOnRow
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|SortedSet
argument_list|<
name|Cell
argument_list|>
name|cellHead
init|=
name|segment
operator|.
name|headSet
argument_list|(
name|firstKeyOnRow
argument_list|)
decl_stmt|;
name|Cell
name|lastCellBeforeRow
init|=
name|cellHead
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
name|cellHead
operator|.
name|last
argument_list|()
decl_stmt|;
if|if
condition|(
name|lastCellBeforeRow
operator|==
literal|null
condition|)
block|{
name|current
operator|=
literal|null
expr_stmt|;
return|return
literal|false
return|;
block|}
name|Cell
name|firstKeyOnPreviousRow
init|=
name|CellUtil
operator|.
name|createFirstOnRow
argument_list|(
name|lastCellBeforeRow
argument_list|)
decl_stmt|;
name|this
operator|.
name|stopSkippingKVsIfNextRow
operator|=
literal|true
expr_stmt|;
name|seek
argument_list|(
name|firstKeyOnPreviousRow
argument_list|)
expr_stmt|;
name|this
operator|.
name|stopSkippingKVsIfNextRow
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|peek
argument_list|()
operator|==
literal|null
operator|||
name|segment
operator|.
name|getComparator
argument_list|()
operator|.
name|compareRows
argument_list|(
name|peek
argument_list|()
argument_list|,
name|firstKeyOnPreviousRow
argument_list|)
operator|>
literal|0
condition|)
block|{
name|keepSeeking
operator|=
literal|true
expr_stmt|;
name|key
operator|=
name|firstKeyOnPreviousRow
expr_stmt|;
continue|continue;
block|}
else|else
block|{
name|keepSeeking
operator|=
literal|false
expr_stmt|;
block|}
block|}
do|while
condition|(
name|keepSeeking
condition|)
do|;
return|return
literal|true
return|;
block|}
comment|/**    * Seek the scanner at the first KeyValue of last row    *    * @return true if scanner has values left, false if the underlying data is empty    */
annotation|@
name|Override
specifier|public
name|boolean
name|seekToLastRow
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Cell
name|higherCell
init|=
name|segment
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
name|segment
operator|.
name|last
argument_list|()
decl_stmt|;
if|if
condition|(
name|higherCell
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Cell
name|firstCellOnLastRow
init|=
name|CellUtil
operator|.
name|createFirstOnRow
argument_list|(
name|higherCell
argument_list|)
decl_stmt|;
if|if
condition|(
name|seek
argument_list|(
name|firstCellOnLastRow
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
name|seekToPreviousRow
argument_list|(
name|higherCell
argument_list|)
return|;
block|}
block|}
comment|/**    * @see KeyValueScanner#getScannerOrder()    */
annotation|@
name|Override
specifier|public
name|long
name|getScannerOrder
parameter_list|()
block|{
return|return
name|scannerOrder
return|;
block|}
comment|/**    * Close the KeyValue scanner.    */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return;
block|}
name|getSegment
argument_list|()
operator|.
name|decScannerCount
argument_list|()
expr_stmt|;
name|closed
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * This functionality should be resolved in the higher level which is    * MemStoreScanner, currently returns true as default. Doesn't throw    * IllegalStateException in order not to change the signature of the    * overridden method    */
annotation|@
name|Override
specifier|public
name|boolean
name|shouldUseScanner
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|Store
name|store
parameter_list|,
name|long
name|oldestUnexpiredTS
parameter_list|)
block|{
return|return
name|getSegment
argument_list|()
operator|.
name|shouldSeek
argument_list|(
name|scan
argument_list|,
name|oldestUnexpiredTS
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|requestSeek
parameter_list|(
name|Cell
name|c
parameter_list|,
name|boolean
name|forward
parameter_list|,
name|boolean
name|useBloom
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|NonLazyKeyValueScanner
operator|.
name|doRealSeek
argument_list|(
name|this
argument_list|,
name|c
argument_list|,
name|forward
argument_list|)
return|;
block|}
comment|/**    * This scanner is working solely on the in-memory MemStore and doesn't work on    * store files, MutableCellSetSegmentScanner always does the seek,    * therefore always returning true.    */
annotation|@
name|Override
specifier|public
name|boolean
name|realSeekDone
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
comment|/**    * This function should be never called on scanners that always do real seek operations (i.e. most    * of the scanners and also this one). The easiest way to achieve this is to call    * {@link #realSeekDone()} first.    */
annotation|@
name|Override
specifier|public
name|void
name|enforceSeek
parameter_list|()
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|NotImplementedException
argument_list|(
literal|"enforceSeek cannot be called on a SegmentScanner"
argument_list|)
throw|;
block|}
comment|/**    * @return true if this is a file scanner. Otherwise a memory scanner is assumed.    */
annotation|@
name|Override
specifier|public
name|boolean
name|isFileScanner
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**    * @return the next key in the index (the key to seek to the next block)    *     if known, or null otherwise    *     Not relevant for in-memory scanner    */
annotation|@
name|Override
specifier|public
name|Cell
name|getNextIndexedKey
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
comment|/**    * Called after a batch of rows scanned (RPC) and set to be returned to client. Any in between    * cleanup can be done here. Nothing to be done for MutableCellSetSegmentScanner.    */
annotation|@
name|Override
specifier|public
name|void
name|shipped
parameter_list|()
throws|throws
name|IOException
block|{
comment|// do nothing
block|}
comment|//debug method
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
literal|"Store segment scanner of type "
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
literal|"Scanner order "
operator|+
name|getScannerOrder
argument_list|()
operator|+
literal|"; "
expr_stmt|;
name|res
operator|+=
name|getSegment
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
return|return
name|res
return|;
block|}
comment|/********************* Private Methods **********************/
specifier|private
name|Segment
name|getSegment
parameter_list|()
block|{
return|return
name|segment
return|;
block|}
comment|/**    * Private internal method for iterating over the segment,    * skipping the cells with irrelevant MVCC    */
specifier|protected
name|void
name|updateCurrent
parameter_list|()
block|{
name|Cell
name|startKV
init|=
name|current
decl_stmt|;
name|Cell
name|next
init|=
literal|null
decl_stmt|;
try|try
block|{
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|next
operator|=
name|iter
operator|.
name|next
argument_list|()
expr_stmt|;
if|if
condition|(
name|next
operator|.
name|getSequenceId
argument_list|()
operator|<=
name|this
operator|.
name|readPoint
condition|)
block|{
name|current
operator|=
name|next
expr_stmt|;
return|return;
comment|// skip irrelevant versions
block|}
if|if
condition|(
name|stopSkippingKVsIfNextRow
operator|&&
comment|// for backwardSeek() stay in the
name|startKV
operator|!=
literal|null
operator|&&
comment|// boundaries of a single row
name|segment
operator|.
name|compareRows
argument_list|(
name|next
argument_list|,
name|startKV
argument_list|)
operator|>
literal|0
condition|)
block|{
name|current
operator|=
literal|null
expr_stmt|;
return|return;
block|}
block|}
comment|// end of while
name|current
operator|=
literal|null
expr_stmt|;
comment|// nothing found
block|}
finally|finally
block|{
if|if
condition|(
name|next
operator|!=
literal|null
condition|)
block|{
comment|// in all cases, remember the last KV we iterated to, needed for reseek()
name|last
operator|=
name|next
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Private internal method that returns the higher of the two key values, or null    * if they are both null    */
specifier|private
name|Cell
name|getHighest
parameter_list|(
name|Cell
name|first
parameter_list|,
name|Cell
name|second
parameter_list|)
block|{
if|if
condition|(
name|first
operator|==
literal|null
operator|&&
name|second
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|first
operator|!=
literal|null
operator|&&
name|second
operator|!=
literal|null
condition|)
block|{
name|int
name|compare
init|=
name|segment
operator|.
name|compare
argument_list|(
name|first
argument_list|,
name|second
argument_list|)
decl_stmt|;
return|return
operator|(
name|compare
operator|>
literal|0
condition|?
name|first
else|:
name|second
operator|)
return|;
block|}
return|return
operator|(
name|first
operator|!=
literal|null
condition|?
name|first
else|:
name|second
operator|)
return|;
block|}
block|}
end_class

end_unit

