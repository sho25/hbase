begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|codec
operator|.
name|prefixtree
operator|.
name|decode
package|;
end_package

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
name|PrivateCellUtil
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
name|codec
operator|.
name|prefixtree
operator|.
name|PrefixTreeBlockMeta
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
name|codec
operator|.
name|prefixtree
operator|.
name|scanner
operator|.
name|CellScannerPosition
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
name|codec
operator|.
name|prefixtree
operator|.
name|scanner
operator|.
name|CellSearcher
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|primitives
operator|.
name|UnsignedBytes
import|;
end_import

begin_comment
comment|/**  *<p>  * Searcher extends the capabilities of the Scanner + ReversibleScanner to add the ability to  * position itself on a requested Cell without scanning through cells before it. The PrefixTree is  * set up to be a Trie of rows, so finding a particular row is extremely cheap.  *</p>  * Once it finds the row, it does a binary search through the cells inside the row, which is not as  * fast as the trie search, but faster than iterating through every cell like existing block  * formats  * do. For this reason, this implementation is targeted towards schemas where rows are narrow  * enough  * to have several or many per block, and where you are generally looking for the entire row or  * the  * first cell. It will still be fast for wide rows or point queries, but could be improved upon.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|PrefixTreeArraySearcher
extends|extends
name|PrefixTreeArrayReversibleScanner
implements|implements
name|CellSearcher
block|{
comment|/*************** construct ******************************/
specifier|public
name|PrefixTreeArraySearcher
parameter_list|(
name|PrefixTreeBlockMeta
name|blockMeta
parameter_list|,
name|int
name|rowTreeDepth
parameter_list|,
name|int
name|rowBufferLength
parameter_list|,
name|int
name|qualifierBufferLength
parameter_list|,
name|int
name|tagsBufferLength
parameter_list|)
block|{
name|super
argument_list|(
name|blockMeta
argument_list|,
name|rowTreeDepth
argument_list|,
name|rowBufferLength
argument_list|,
name|qualifierBufferLength
argument_list|,
name|tagsBufferLength
argument_list|)
expr_stmt|;
block|}
comment|/********************* CellSearcher methods *******************/
annotation|@
name|Override
specifier|public
name|boolean
name|positionAt
parameter_list|(
name|Cell
name|key
parameter_list|)
block|{
return|return
name|CellScannerPosition
operator|.
name|AT
operator|==
name|positionAtOrAfter
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CellScannerPosition
name|positionAtOrBefore
parameter_list|(
name|Cell
name|key
parameter_list|)
block|{
name|reInitFirstNode
argument_list|()
expr_stmt|;
name|int
name|fanIndex
init|=
operator|-
literal|1
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
comment|//detect row mismatch.  break loop if mismatch
name|int
name|currentNodeDepth
init|=
name|rowLength
decl_stmt|;
name|int
name|rowTokenComparison
init|=
name|compareToCurrentToken
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|rowTokenComparison
operator|!=
literal|0
condition|)
block|{
return|return
name|fixRowTokenMissReverse
argument_list|(
name|rowTokenComparison
argument_list|)
return|;
block|}
comment|//exact row found, move on to qualifier& ts
if|if
condition|(
name|rowMatchesAfterCurrentPosition
argument_list|(
name|key
argument_list|)
condition|)
block|{
return|return
name|positionAtQualifierTimestamp
argument_list|(
name|key
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|//detect dead end (no fan to descend into)
if|if
condition|(
operator|!
name|currentRowNode
operator|.
name|hasFan
argument_list|()
condition|)
block|{
if|if
condition|(
name|hasOccurrences
argument_list|()
condition|)
block|{
comment|//must be leaf or nub
name|populateLastNonRowFields
argument_list|()
expr_stmt|;
return|return
name|CellScannerPosition
operator|.
name|BEFORE
return|;
block|}
else|else
block|{
comment|//TODO i don't think this case is exercised by any tests
return|return
name|fixRowFanMissReverse
argument_list|(
literal|0
argument_list|)
return|;
block|}
block|}
comment|//keep hunting for the rest of the row
name|byte
name|searchForByte
init|=
name|PrivateCellUtil
operator|.
name|getRowByte
argument_list|(
name|key
argument_list|,
name|currentNodeDepth
argument_list|)
decl_stmt|;
name|fanIndex
operator|=
name|currentRowNode
operator|.
name|whichFanNode
argument_list|(
name|searchForByte
argument_list|)
expr_stmt|;
if|if
condition|(
name|fanIndex
operator|<
literal|0
condition|)
block|{
comment|//no matching row.  return early
name|int
name|insertionPoint
init|=
operator|-
name|fanIndex
operator|-
literal|1
decl_stmt|;
return|return
name|fixRowFanMissReverse
argument_list|(
name|insertionPoint
argument_list|)
return|;
block|}
comment|//found a match, so dig deeper into the tree
name|followFan
argument_list|(
name|fanIndex
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Identical workflow as positionAtOrBefore, but split them to avoid having ~10 extra    * if-statements. Priority on readability and debugability.    */
annotation|@
name|Override
specifier|public
name|CellScannerPosition
name|positionAtOrAfter
parameter_list|(
name|Cell
name|key
parameter_list|)
block|{
name|reInitFirstNode
argument_list|()
expr_stmt|;
name|int
name|fanIndex
init|=
operator|-
literal|1
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
comment|//detect row mismatch.  break loop if mismatch
name|int
name|currentNodeDepth
init|=
name|rowLength
decl_stmt|;
name|int
name|rowTokenComparison
init|=
name|compareToCurrentToken
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|rowTokenComparison
operator|!=
literal|0
condition|)
block|{
return|return
name|fixRowTokenMissForward
argument_list|(
name|rowTokenComparison
argument_list|)
return|;
block|}
comment|//exact row found, move on to qualifier& ts
if|if
condition|(
name|rowMatchesAfterCurrentPosition
argument_list|(
name|key
argument_list|)
condition|)
block|{
return|return
name|positionAtQualifierTimestamp
argument_list|(
name|key
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|//detect dead end (no fan to descend into)
if|if
condition|(
operator|!
name|currentRowNode
operator|.
name|hasFan
argument_list|()
condition|)
block|{
if|if
condition|(
name|hasOccurrences
argument_list|()
condition|)
block|{
if|if
condition|(
name|rowLength
operator|<
name|key
operator|.
name|getRowLength
argument_list|()
condition|)
block|{
name|nextRow
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|populateFirstNonRowFields
argument_list|()
expr_stmt|;
block|}
return|return
name|CellScannerPosition
operator|.
name|AFTER
return|;
block|}
else|else
block|{
comment|//TODO i don't think this case is exercised by any tests
return|return
name|fixRowFanMissForward
argument_list|(
literal|0
argument_list|)
return|;
block|}
block|}
comment|//keep hunting for the rest of the row
name|byte
name|searchForByte
init|=
name|PrivateCellUtil
operator|.
name|getRowByte
argument_list|(
name|key
argument_list|,
name|currentNodeDepth
argument_list|)
decl_stmt|;
name|fanIndex
operator|=
name|currentRowNode
operator|.
name|whichFanNode
argument_list|(
name|searchForByte
argument_list|)
expr_stmt|;
if|if
condition|(
name|fanIndex
operator|<
literal|0
condition|)
block|{
comment|//no matching row.  return early
name|int
name|insertionPoint
init|=
operator|-
name|fanIndex
operator|-
literal|1
decl_stmt|;
return|return
name|fixRowFanMissForward
argument_list|(
name|insertionPoint
argument_list|)
return|;
block|}
comment|//found a match, so dig deeper into the tree
name|followFan
argument_list|(
name|fanIndex
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|seekForwardTo
parameter_list|(
name|Cell
name|key
parameter_list|)
block|{
if|if
condition|(
name|currentPositionIsAfter
argument_list|(
name|key
argument_list|)
condition|)
block|{
comment|//our position is after the requested key, so can't do anything
return|return
literal|false
return|;
block|}
return|return
name|positionAt
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CellScannerPosition
name|seekForwardToOrBefore
parameter_list|(
name|Cell
name|key
parameter_list|)
block|{
comment|//Do we even need this check or should upper layers avoid this situation.  It's relatively
comment|//expensive compared to the rest of the seek operation.
if|if
condition|(
name|currentPositionIsAfter
argument_list|(
name|key
argument_list|)
condition|)
block|{
comment|//our position is after the requested key, so can't do anything
return|return
name|CellScannerPosition
operator|.
name|AFTER
return|;
block|}
return|return
name|positionAtOrBefore
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CellScannerPosition
name|seekForwardToOrAfter
parameter_list|(
name|Cell
name|key
parameter_list|)
block|{
comment|//Do we even need this check or should upper layers avoid this situation.  It's relatively
comment|//expensive compared to the rest of the seek operation.
if|if
condition|(
name|currentPositionIsAfter
argument_list|(
name|key
argument_list|)
condition|)
block|{
comment|//our position is after the requested key, so can't do anything
return|return
name|CellScannerPosition
operator|.
name|AFTER
return|;
block|}
return|return
name|positionAtOrAfter
argument_list|(
name|key
argument_list|)
return|;
block|}
comment|/**    * The content of the buffers doesn't matter here, only that afterLast=true and beforeFirst=false    */
annotation|@
name|Override
specifier|public
name|void
name|positionAfterLastCell
parameter_list|()
block|{
name|resetToBeforeFirstEntry
argument_list|()
expr_stmt|;
name|beforeFirst
operator|=
literal|false
expr_stmt|;
name|afterLast
operator|=
literal|true
expr_stmt|;
block|}
comment|/***************** Object methods ***************************/
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
comment|//trivial override to confirm intent (findbugs)
return|return
name|super
operator|.
name|equals
argument_list|(
name|obj
argument_list|)
return|;
block|}
comment|/****************** internal methods ************************/
specifier|protected
name|boolean
name|currentPositionIsAfter
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
return|return
name|compareTo
argument_list|(
name|cell
argument_list|)
operator|>
literal|0
return|;
block|}
specifier|protected
name|CellScannerPosition
name|positionAtQualifierTimestamp
parameter_list|(
name|Cell
name|key
parameter_list|,
name|boolean
name|beforeOnMiss
parameter_list|)
block|{
name|int
name|minIndex
init|=
literal|0
decl_stmt|;
name|int
name|maxIndex
init|=
name|currentRowNode
operator|.
name|getLastCellIndex
argument_list|()
decl_stmt|;
name|int
name|diff
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|midIndex
init|=
operator|(
name|maxIndex
operator|+
name|minIndex
operator|)
operator|/
literal|2
decl_stmt|;
comment|//don't worry about overflow
name|diff
operator|=
name|populateNonRowFieldsAndCompareTo
argument_list|(
name|midIndex
argument_list|,
name|key
argument_list|)
expr_stmt|;
if|if
condition|(
name|diff
operator|==
literal|0
condition|)
block|{
comment|// found exact match
return|return
name|CellScannerPosition
operator|.
name|AT
return|;
block|}
elseif|else
if|if
condition|(
name|minIndex
operator|==
name|maxIndex
condition|)
block|{
comment|// even termination case
break|break;
block|}
elseif|else
if|if
condition|(
operator|(
name|minIndex
operator|+
literal|1
operator|)
operator|==
name|maxIndex
condition|)
block|{
comment|// odd termination case
name|diff
operator|=
name|populateNonRowFieldsAndCompareTo
argument_list|(
name|maxIndex
argument_list|,
name|key
argument_list|)
expr_stmt|;
if|if
condition|(
name|diff
operator|>
literal|0
condition|)
block|{
name|diff
operator|=
name|populateNonRowFieldsAndCompareTo
argument_list|(
name|minIndex
argument_list|,
name|key
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
elseif|else
if|if
condition|(
name|diff
operator|<
literal|0
condition|)
block|{
comment|// keep going forward
name|minIndex
operator|=
name|currentCellIndex
expr_stmt|;
block|}
else|else
block|{
comment|// went past it, back up
name|maxIndex
operator|=
name|currentCellIndex
expr_stmt|;
block|}
block|}
if|if
condition|(
name|diff
operator|==
literal|0
condition|)
block|{
return|return
name|CellScannerPosition
operator|.
name|AT
return|;
block|}
elseif|else
if|if
condition|(
name|diff
operator|<
literal|0
condition|)
block|{
comment|// we are before key
if|if
condition|(
name|beforeOnMiss
condition|)
block|{
return|return
name|CellScannerPosition
operator|.
name|BEFORE
return|;
block|}
if|if
condition|(
name|advance
argument_list|()
condition|)
block|{
return|return
name|CellScannerPosition
operator|.
name|AFTER
return|;
block|}
return|return
name|CellScannerPosition
operator|.
name|AFTER_LAST
return|;
block|}
else|else
block|{
comment|// we are after key
if|if
condition|(
operator|!
name|beforeOnMiss
condition|)
block|{
return|return
name|CellScannerPosition
operator|.
name|AFTER
return|;
block|}
if|if
condition|(
name|previous
argument_list|()
condition|)
block|{
return|return
name|CellScannerPosition
operator|.
name|BEFORE
return|;
block|}
return|return
name|CellScannerPosition
operator|.
name|BEFORE_FIRST
return|;
block|}
block|}
comment|/**    * compare this.row to key.row but starting at the current rowLength    * @param key Cell being searched for    * @return true if row buffer contents match key.row    */
specifier|protected
name|boolean
name|rowMatchesAfterCurrentPosition
parameter_list|(
name|Cell
name|key
parameter_list|)
block|{
if|if
condition|(
operator|!
name|currentRowNode
operator|.
name|hasOccurrences
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|int
name|thatRowLength
init|=
name|key
operator|.
name|getRowLength
argument_list|()
decl_stmt|;
if|if
condition|(
name|rowLength
operator|!=
name|thatRowLength
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
comment|// TODO move part of this to Cell comparator?
comment|/**    * Compare only the bytes within the window of the current token    * @param key    * @return return -1 if key is lessThan (before) this, 0 if equal, and 1 if key is after    */
specifier|protected
name|int
name|compareToCurrentToken
parameter_list|(
name|Cell
name|key
parameter_list|)
block|{
name|int
name|startIndex
init|=
name|rowLength
operator|-
name|currentRowNode
operator|.
name|getTokenLength
argument_list|()
decl_stmt|;
name|int
name|endIndexExclusive
init|=
name|startIndex
operator|+
name|currentRowNode
operator|.
name|getTokenLength
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|startIndex
init|;
name|i
operator|<
name|endIndexExclusive
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|i
operator|>=
name|key
operator|.
name|getRowLength
argument_list|()
condition|)
block|{
comment|// key was shorter, so it's first
return|return
operator|-
literal|1
return|;
block|}
name|byte
name|keyByte
init|=
name|PrivateCellUtil
operator|.
name|getRowByte
argument_list|(
name|key
argument_list|,
name|i
argument_list|)
decl_stmt|;
name|byte
name|thisByte
init|=
name|rowBuffer
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|keyByte
operator|==
name|thisByte
condition|)
block|{
continue|continue;
block|}
return|return
name|UnsignedBytes
operator|.
name|compare
argument_list|(
name|keyByte
argument_list|,
name|thisByte
argument_list|)
return|;
block|}
if|if
condition|(
operator|!
name|currentRowNode
operator|.
name|hasOccurrences
argument_list|()
operator|&&
name|rowLength
operator|>=
name|key
operator|.
name|getRowLength
argument_list|()
condition|)
block|{
comment|// key was shorter
return|return
operator|-
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
specifier|protected
name|void
name|followLastFansUntilExhausted
parameter_list|()
block|{
while|while
condition|(
name|currentRowNode
operator|.
name|hasFan
argument_list|()
condition|)
block|{
name|followLastFan
argument_list|()
expr_stmt|;
block|}
block|}
comment|/****************** complete seek when token mismatch ******************/
comment|/**    * @param searcherIsAfterInputKey&lt;0: input key is before the searcher's position<br>    *&gt;0: input key is after the searcher's position    */
specifier|protected
name|CellScannerPosition
name|fixRowTokenMissReverse
parameter_list|(
name|int
name|searcherIsAfterInputKey
parameter_list|)
block|{
if|if
condition|(
name|searcherIsAfterInputKey
operator|<
literal|0
condition|)
block|{
comment|//searcher position is after the input key, so back up
name|boolean
name|foundPreviousRow
init|=
name|previousRow
argument_list|(
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|foundPreviousRow
condition|)
block|{
name|populateLastNonRowFields
argument_list|()
expr_stmt|;
return|return
name|CellScannerPosition
operator|.
name|BEFORE
return|;
block|}
else|else
block|{
return|return
name|CellScannerPosition
operator|.
name|BEFORE_FIRST
return|;
block|}
block|}
else|else
block|{
comment|//searcher position is before the input key
if|if
condition|(
name|currentRowNode
operator|.
name|hasOccurrences
argument_list|()
condition|)
block|{
name|populateFirstNonRowFields
argument_list|()
expr_stmt|;
return|return
name|CellScannerPosition
operator|.
name|BEFORE
return|;
block|}
name|boolean
name|foundNextRow
init|=
name|nextRow
argument_list|()
decl_stmt|;
if|if
condition|(
name|foundNextRow
condition|)
block|{
return|return
name|CellScannerPosition
operator|.
name|AFTER
return|;
block|}
else|else
block|{
return|return
name|CellScannerPosition
operator|.
name|AFTER_LAST
return|;
block|}
block|}
block|}
comment|/**    * @param searcherIsAfterInputKey&lt;0: input key is before the searcher's position<br>    *&gt;0: input key is after the searcher's position    */
specifier|protected
name|CellScannerPosition
name|fixRowTokenMissForward
parameter_list|(
name|int
name|searcherIsAfterInputKey
parameter_list|)
block|{
if|if
condition|(
name|searcherIsAfterInputKey
operator|<
literal|0
condition|)
block|{
comment|//searcher position is after the input key
if|if
condition|(
name|currentRowNode
operator|.
name|hasOccurrences
argument_list|()
condition|)
block|{
name|populateFirstNonRowFields
argument_list|()
expr_stmt|;
return|return
name|CellScannerPosition
operator|.
name|AFTER
return|;
block|}
name|boolean
name|foundNextRow
init|=
name|nextRow
argument_list|()
decl_stmt|;
if|if
condition|(
name|foundNextRow
condition|)
block|{
return|return
name|CellScannerPosition
operator|.
name|AFTER
return|;
block|}
else|else
block|{
return|return
name|CellScannerPosition
operator|.
name|AFTER_LAST
return|;
block|}
block|}
else|else
block|{
comment|//searcher position is before the input key, so go forward
name|discardCurrentRowNode
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|boolean
name|foundNextRow
init|=
name|nextRow
argument_list|()
decl_stmt|;
if|if
condition|(
name|foundNextRow
condition|)
block|{
return|return
name|CellScannerPosition
operator|.
name|AFTER
return|;
block|}
else|else
block|{
return|return
name|CellScannerPosition
operator|.
name|AFTER_LAST
return|;
block|}
block|}
block|}
comment|/****************** complete seek when fan mismatch ******************/
specifier|protected
name|CellScannerPosition
name|fixRowFanMissReverse
parameter_list|(
name|int
name|fanInsertionPoint
parameter_list|)
block|{
if|if
condition|(
name|fanInsertionPoint
operator|==
literal|0
condition|)
block|{
comment|//we need to back up a row
if|if
condition|(
name|currentRowNode
operator|.
name|hasOccurrences
argument_list|()
condition|)
block|{
name|populateLastNonRowFields
argument_list|()
expr_stmt|;
return|return
name|CellScannerPosition
operator|.
name|BEFORE
return|;
block|}
name|boolean
name|foundPreviousRow
init|=
name|previousRow
argument_list|(
literal|true
argument_list|)
decl_stmt|;
comment|//true -> position on last cell in row
if|if
condition|(
name|foundPreviousRow
condition|)
block|{
name|populateLastNonRowFields
argument_list|()
expr_stmt|;
return|return
name|CellScannerPosition
operator|.
name|BEFORE
return|;
block|}
return|return
name|CellScannerPosition
operator|.
name|BEFORE_FIRST
return|;
block|}
comment|//follow the previous fan, but then descend recursively forward
name|followFan
argument_list|(
name|fanInsertionPoint
operator|-
literal|1
argument_list|)
expr_stmt|;
name|followLastFansUntilExhausted
argument_list|()
expr_stmt|;
name|populateLastNonRowFields
argument_list|()
expr_stmt|;
return|return
name|CellScannerPosition
operator|.
name|BEFORE
return|;
block|}
specifier|protected
name|CellScannerPosition
name|fixRowFanMissForward
parameter_list|(
name|int
name|fanInsertionPoint
parameter_list|)
block|{
if|if
condition|(
name|fanInsertionPoint
operator|>=
name|currentRowNode
operator|.
name|getFanOut
argument_list|()
condition|)
block|{
name|discardCurrentRowNode
argument_list|(
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|nextRow
argument_list|()
condition|)
block|{
return|return
name|CellScannerPosition
operator|.
name|AFTER_LAST
return|;
block|}
else|else
block|{
return|return
name|CellScannerPosition
operator|.
name|AFTER
return|;
block|}
block|}
name|followFan
argument_list|(
name|fanInsertionPoint
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasOccurrences
argument_list|()
condition|)
block|{
name|populateFirstNonRowFields
argument_list|()
expr_stmt|;
return|return
name|CellScannerPosition
operator|.
name|AFTER
return|;
block|}
if|if
condition|(
name|nextRowInternal
argument_list|()
condition|)
block|{
name|populateFirstNonRowFields
argument_list|()
expr_stmt|;
return|return
name|CellScannerPosition
operator|.
name|AFTER
return|;
block|}
else|else
block|{
return|return
name|CellScannerPosition
operator|.
name|AFTER_LAST
return|;
block|}
block|}
block|}
end_class

end_unit

