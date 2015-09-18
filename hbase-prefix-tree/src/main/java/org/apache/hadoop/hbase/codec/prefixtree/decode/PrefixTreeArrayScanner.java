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
name|CellScanner
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
name|decode
operator|.
name|column
operator|.
name|ColumnReader
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
name|decode
operator|.
name|row
operator|.
name|RowNodeReader
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
name|decode
operator|.
name|timestamp
operator|.
name|MvccVersionDecoder
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
name|decode
operator|.
name|timestamp
operator|.
name|TimestampDecoder
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
name|encode
operator|.
name|other
operator|.
name|ColumnNodeType
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
name|nio
operator|.
name|ByteBuff
import|;
end_import

begin_comment
comment|/**  * Extends PtCell and manipulates its protected fields.  Could alternatively contain a PtCell and  * call get/set methods.  *  * This is an "Array" scanner to distinguish from a future "ByteBuffer" scanner.  This  * implementation requires that the bytes be in a normal java byte[] for performance.  The  * alternative ByteBuffer implementation would allow for accessing data in an off-heap ByteBuffer  * without copying the whole buffer on-heap.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|PrefixTreeArrayScanner
extends|extends
name|PrefixTreeCell
implements|implements
name|CellScanner
block|{
comment|/***************** fields ********************************/
specifier|protected
name|PrefixTreeBlockMeta
name|blockMeta
decl_stmt|;
specifier|protected
name|boolean
name|beforeFirst
decl_stmt|;
specifier|protected
name|boolean
name|afterLast
decl_stmt|;
specifier|protected
name|RowNodeReader
index|[]
name|rowNodes
decl_stmt|;
specifier|protected
name|int
name|rowNodeStackIndex
decl_stmt|;
specifier|protected
name|RowNodeReader
name|currentRowNode
decl_stmt|;
specifier|protected
name|ColumnReader
name|familyReader
decl_stmt|;
specifier|protected
name|ColumnReader
name|qualifierReader
decl_stmt|;
specifier|protected
name|ColumnReader
name|tagsReader
decl_stmt|;
specifier|protected
name|TimestampDecoder
name|timestampDecoder
decl_stmt|;
specifier|protected
name|MvccVersionDecoder
name|mvccVersionDecoder
decl_stmt|;
specifier|protected
name|boolean
name|nubCellsRemain
decl_stmt|;
specifier|protected
name|int
name|currentCellIndex
decl_stmt|;
comment|/*********************** construct ******************************/
comment|// pass in blockMeta so we can initialize buffers big enough for all cells in the block
specifier|public
name|PrefixTreeArrayScanner
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
name|this
operator|.
name|rowNodes
operator|=
operator|new
name|RowNodeReader
index|[
name|rowTreeDepth
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|rowNodes
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|rowNodes
index|[
name|i
index|]
operator|=
operator|new
name|RowNodeReader
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|rowBuffer
operator|=
operator|new
name|byte
index|[
name|rowBufferLength
index|]
expr_stmt|;
name|this
operator|.
name|familyBuffer
operator|=
operator|new
name|byte
index|[
name|PrefixTreeBlockMeta
operator|.
name|MAX_FAMILY_LENGTH
index|]
expr_stmt|;
name|this
operator|.
name|familyReader
operator|=
operator|new
name|ColumnReader
argument_list|(
name|familyBuffer
argument_list|,
name|ColumnNodeType
operator|.
name|FAMILY
argument_list|)
expr_stmt|;
name|this
operator|.
name|qualifierBuffer
operator|=
operator|new
name|byte
index|[
name|qualifierBufferLength
index|]
expr_stmt|;
name|this
operator|.
name|tagsBuffer
operator|=
operator|new
name|byte
index|[
name|tagsBufferLength
index|]
expr_stmt|;
name|this
operator|.
name|qualifierReader
operator|=
operator|new
name|ColumnReader
argument_list|(
name|qualifierBuffer
argument_list|,
name|ColumnNodeType
operator|.
name|QUALIFIER
argument_list|)
expr_stmt|;
name|this
operator|.
name|tagsReader
operator|=
operator|new
name|ColumnReader
argument_list|(
name|tagsBuffer
argument_list|,
name|ColumnNodeType
operator|.
name|TAGS
argument_list|)
expr_stmt|;
name|this
operator|.
name|timestampDecoder
operator|=
operator|new
name|TimestampDecoder
argument_list|()
expr_stmt|;
name|this
operator|.
name|mvccVersionDecoder
operator|=
operator|new
name|MvccVersionDecoder
argument_list|()
expr_stmt|;
block|}
comment|/**************** init helpers ***************************************/
comment|/**    * Call when first accessing a block.    * @return entirely new scanner if false    */
specifier|public
name|boolean
name|areBuffersBigEnough
parameter_list|()
block|{
if|if
condition|(
name|rowNodes
operator|.
name|length
operator|<
name|blockMeta
operator|.
name|getRowTreeDepth
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|rowBuffer
operator|.
name|length
operator|<
name|blockMeta
operator|.
name|getMaxRowLength
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|qualifierBuffer
operator|.
name|length
operator|<
name|blockMeta
operator|.
name|getMaxQualifierLength
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|tagsBuffer
operator|.
name|length
operator|<
name|blockMeta
operator|.
name|getMaxTagsLength
argument_list|()
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
specifier|public
name|void
name|initOnBlock
parameter_list|(
name|PrefixTreeBlockMeta
name|blockMeta
parameter_list|,
name|ByteBuff
name|block
parameter_list|,
name|boolean
name|includeMvccVersion
parameter_list|)
block|{
name|this
operator|.
name|block
operator|=
name|block
expr_stmt|;
name|this
operator|.
name|blockMeta
operator|=
name|blockMeta
expr_stmt|;
name|this
operator|.
name|familyOffset
operator|=
name|familyBuffer
operator|.
name|length
expr_stmt|;
name|this
operator|.
name|familyReader
operator|.
name|initOnBlock
argument_list|(
name|blockMeta
argument_list|,
name|block
argument_list|)
expr_stmt|;
name|this
operator|.
name|qualifierOffset
operator|=
name|qualifierBuffer
operator|.
name|length
expr_stmt|;
name|this
operator|.
name|qualifierReader
operator|.
name|initOnBlock
argument_list|(
name|blockMeta
argument_list|,
name|block
argument_list|)
expr_stmt|;
name|this
operator|.
name|tagsOffset
operator|=
name|tagsBuffer
operator|.
name|length
expr_stmt|;
name|this
operator|.
name|tagsReader
operator|.
name|initOnBlock
argument_list|(
name|blockMeta
argument_list|,
name|block
argument_list|)
expr_stmt|;
name|this
operator|.
name|timestampDecoder
operator|.
name|initOnBlock
argument_list|(
name|blockMeta
argument_list|,
name|block
argument_list|)
expr_stmt|;
name|this
operator|.
name|mvccVersionDecoder
operator|.
name|initOnBlock
argument_list|(
name|blockMeta
argument_list|,
name|block
argument_list|)
expr_stmt|;
name|this
operator|.
name|includeMvccVersion
operator|=
name|includeMvccVersion
expr_stmt|;
name|resetToBeforeFirstEntry
argument_list|()
expr_stmt|;
block|}
comment|// Does this have to be in the CellScanner Interface?  TODO
specifier|public
name|void
name|resetToBeforeFirstEntry
parameter_list|()
block|{
name|beforeFirst
operator|=
literal|true
expr_stmt|;
name|afterLast
operator|=
literal|false
expr_stmt|;
name|rowNodeStackIndex
operator|=
operator|-
literal|1
expr_stmt|;
name|currentRowNode
operator|=
literal|null
expr_stmt|;
name|rowLength
operator|=
literal|0
expr_stmt|;
name|familyOffset
operator|=
name|familyBuffer
operator|.
name|length
expr_stmt|;
name|familyLength
operator|=
literal|0
expr_stmt|;
name|qualifierOffset
operator|=
name|blockMeta
operator|.
name|getMaxQualifierLength
argument_list|()
expr_stmt|;
name|qualifierLength
operator|=
literal|0
expr_stmt|;
name|nubCellsRemain
operator|=
literal|false
expr_stmt|;
name|currentCellIndex
operator|=
operator|-
literal|1
expr_stmt|;
name|timestamp
operator|=
operator|-
literal|1L
expr_stmt|;
name|type
operator|=
name|DEFAULT_TYPE
expr_stmt|;
name|absoluteValueOffset
operator|=
literal|0
expr_stmt|;
comment|//use 0 vs -1 so the cell is valid when value hasn't been initialized
name|valueLength
operator|=
literal|0
expr_stmt|;
comment|// had it at -1, but that causes null Cell to add up to the wrong length
name|tagsOffset
operator|=
name|blockMeta
operator|.
name|getMaxTagsLength
argument_list|()
expr_stmt|;
name|tagsLength
operator|=
literal|0
expr_stmt|;
block|}
comment|/**    * Call this before putting the scanner back into a pool so it doesn't hold the last used block    * in memory.    */
specifier|public
name|void
name|releaseBlockReference
parameter_list|()
block|{
name|block
operator|=
literal|null
expr_stmt|;
block|}
comment|/********************** CellScanner **********************/
annotation|@
name|Override
specifier|public
name|Cell
name|current
parameter_list|()
block|{
if|if
condition|(
name|isOutOfBounds
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|(
name|Cell
operator|)
name|this
return|;
block|}
comment|/******************* Object methods ************************/
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
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|super
operator|.
name|hashCode
argument_list|()
return|;
block|}
comment|/**    * Override PrefixTreeCell.toString() with a check to see if the current cell is valid.    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|Cell
name|currentCell
init|=
name|current
argument_list|()
decl_stmt|;
if|if
condition|(
name|currentCell
operator|==
literal|null
condition|)
block|{
return|return
literal|"null"
return|;
block|}
return|return
operator|(
operator|(
name|PrefixTreeCell
operator|)
name|currentCell
operator|)
operator|.
name|getKeyValueString
argument_list|()
return|;
block|}
comment|/******************* advance ***************************/
specifier|public
name|boolean
name|positionAtFirstCell
parameter_list|()
block|{
name|reInitFirstNode
argument_list|()
expr_stmt|;
return|return
name|advance
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|advance
parameter_list|()
block|{
if|if
condition|(
name|afterLast
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|hasOccurrences
argument_list|()
condition|)
block|{
name|resetToBeforeFirstEntry
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|beforeFirst
operator|||
name|isLastCellInRow
argument_list|()
condition|)
block|{
name|nextRow
argument_list|()
expr_stmt|;
if|if
condition|(
name|afterLast
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
else|else
block|{
operator|++
name|currentCellIndex
expr_stmt|;
block|}
name|populateNonRowFields
argument_list|(
name|currentCellIndex
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|public
name|boolean
name|nextRow
parameter_list|()
block|{
name|nextRowInternal
argument_list|()
expr_stmt|;
if|if
condition|(
name|afterLast
condition|)
block|{
return|return
literal|false
return|;
block|}
name|populateNonRowFields
argument_list|(
name|currentCellIndex
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
comment|/**    * This method is safe to call when the scanner is not on a fully valid row node, as in the case    * of a row token miss in the Searcher    * @return true if we are positioned on a valid row, false if past end of block    */
specifier|protected
name|boolean
name|nextRowInternal
parameter_list|()
block|{
if|if
condition|(
name|afterLast
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|beforeFirst
condition|)
block|{
name|initFirstNode
argument_list|()
expr_stmt|;
if|if
condition|(
name|currentRowNode
operator|.
name|hasOccurrences
argument_list|()
condition|)
block|{
if|if
condition|(
name|currentRowNode
operator|.
name|isNub
argument_list|()
condition|)
block|{
name|nubCellsRemain
operator|=
literal|true
expr_stmt|;
block|}
name|currentCellIndex
operator|=
literal|0
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
if|if
condition|(
name|currentRowNode
operator|.
name|isLeaf
argument_list|()
condition|)
block|{
name|discardCurrentRowNode
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
while|while
condition|(
operator|!
name|afterLast
condition|)
block|{
if|if
condition|(
name|nubCellsRemain
condition|)
block|{
name|nubCellsRemain
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|currentRowNode
operator|.
name|hasMoreFanNodes
argument_list|()
condition|)
block|{
name|followNextFan
argument_list|()
expr_stmt|;
if|if
condition|(
name|currentRowNode
operator|.
name|hasOccurrences
argument_list|()
condition|)
block|{
comment|// found some values
name|currentCellIndex
operator|=
literal|0
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
else|else
block|{
name|discardCurrentRowNode
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|false
return|;
comment|// went past the end
block|}
comment|/**************** secondary traversal methods ******************************/
specifier|protected
name|void
name|reInitFirstNode
parameter_list|()
block|{
name|resetToBeforeFirstEntry
argument_list|()
expr_stmt|;
name|initFirstNode
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|initFirstNode
parameter_list|()
block|{
name|int
name|offsetIntoUnderlyingStructure
init|=
name|blockMeta
operator|.
name|getAbsoluteRowOffset
argument_list|()
decl_stmt|;
name|rowNodeStackIndex
operator|=
literal|0
expr_stmt|;
name|currentRowNode
operator|=
name|rowNodes
index|[
literal|0
index|]
expr_stmt|;
name|currentRowNode
operator|.
name|initOnBlock
argument_list|(
name|blockMeta
argument_list|,
name|block
argument_list|,
name|offsetIntoUnderlyingStructure
argument_list|)
expr_stmt|;
name|appendCurrentTokenToRowBuffer
argument_list|()
expr_stmt|;
name|beforeFirst
operator|=
literal|false
expr_stmt|;
block|}
specifier|protected
name|void
name|followFirstFan
parameter_list|()
block|{
name|followFan
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|followPreviousFan
parameter_list|()
block|{
name|int
name|nextFanPosition
init|=
name|currentRowNode
operator|.
name|getFanIndex
argument_list|()
operator|-
literal|1
decl_stmt|;
name|followFan
argument_list|(
name|nextFanPosition
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|followCurrentFan
parameter_list|()
block|{
name|int
name|currentFanPosition
init|=
name|currentRowNode
operator|.
name|getFanIndex
argument_list|()
decl_stmt|;
name|followFan
argument_list|(
name|currentFanPosition
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|followNextFan
parameter_list|()
block|{
name|int
name|nextFanPosition
init|=
name|currentRowNode
operator|.
name|getFanIndex
argument_list|()
operator|+
literal|1
decl_stmt|;
name|followFan
argument_list|(
name|nextFanPosition
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|followLastFan
parameter_list|()
block|{
name|followFan
argument_list|(
name|currentRowNode
operator|.
name|getLastFanIndex
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|followFan
parameter_list|(
name|int
name|fanIndex
parameter_list|)
block|{
name|currentRowNode
operator|.
name|setFanIndex
argument_list|(
name|fanIndex
argument_list|)
expr_stmt|;
name|appendToRowBuffer
argument_list|(
name|currentRowNode
operator|.
name|getFanByte
argument_list|(
name|fanIndex
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|nextOffsetIntoUnderlyingStructure
init|=
name|currentRowNode
operator|.
name|getOffset
argument_list|()
operator|+
name|currentRowNode
operator|.
name|getNextNodeOffset
argument_list|(
name|fanIndex
argument_list|,
name|blockMeta
argument_list|)
decl_stmt|;
operator|++
name|rowNodeStackIndex
expr_stmt|;
name|currentRowNode
operator|=
name|rowNodes
index|[
name|rowNodeStackIndex
index|]
expr_stmt|;
name|currentRowNode
operator|.
name|initOnBlock
argument_list|(
name|blockMeta
argument_list|,
name|block
argument_list|,
name|nextOffsetIntoUnderlyingStructure
argument_list|)
expr_stmt|;
comment|//TODO getToken is spewing garbage
name|appendCurrentTokenToRowBuffer
argument_list|()
expr_stmt|;
if|if
condition|(
name|currentRowNode
operator|.
name|isNub
argument_list|()
condition|)
block|{
name|nubCellsRemain
operator|=
literal|true
expr_stmt|;
block|}
name|currentCellIndex
operator|=
literal|0
expr_stmt|;
block|}
comment|/**    * @param forwards which marker to set if we overflow    */
specifier|protected
name|void
name|discardCurrentRowNode
parameter_list|(
name|boolean
name|forwards
parameter_list|)
block|{
name|RowNodeReader
name|rowNodeBeingPopped
init|=
name|currentRowNode
decl_stmt|;
operator|--
name|rowNodeStackIndex
expr_stmt|;
comment|// pop it off the stack
if|if
condition|(
name|rowNodeStackIndex
operator|<
literal|0
condition|)
block|{
name|currentRowNode
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|forwards
condition|)
block|{
name|markAfterLast
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|markBeforeFirst
argument_list|()
expr_stmt|;
block|}
return|return;
block|}
name|popFromRowBuffer
argument_list|(
name|rowNodeBeingPopped
argument_list|)
expr_stmt|;
name|currentRowNode
operator|=
name|rowNodes
index|[
name|rowNodeStackIndex
index|]
expr_stmt|;
block|}
specifier|protected
name|void
name|markBeforeFirst
parameter_list|()
block|{
name|beforeFirst
operator|=
literal|true
expr_stmt|;
name|afterLast
operator|=
literal|false
expr_stmt|;
name|currentRowNode
operator|=
literal|null
expr_stmt|;
block|}
specifier|protected
name|void
name|markAfterLast
parameter_list|()
block|{
name|beforeFirst
operator|=
literal|false
expr_stmt|;
name|afterLast
operator|=
literal|true
expr_stmt|;
name|currentRowNode
operator|=
literal|null
expr_stmt|;
block|}
comment|/***************** helper methods **************************/
specifier|protected
name|void
name|appendCurrentTokenToRowBuffer
parameter_list|()
block|{
name|block
operator|.
name|get
argument_list|(
name|currentRowNode
operator|.
name|getTokenArrayOffset
argument_list|()
argument_list|,
name|rowBuffer
argument_list|,
name|rowLength
argument_list|,
name|currentRowNode
operator|.
name|getTokenLength
argument_list|()
argument_list|)
expr_stmt|;
name|rowLength
operator|+=
name|currentRowNode
operator|.
name|getTokenLength
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|appendToRowBuffer
parameter_list|(
name|byte
name|b
parameter_list|)
block|{
name|rowBuffer
index|[
name|rowLength
index|]
operator|=
name|b
expr_stmt|;
operator|++
name|rowLength
expr_stmt|;
block|}
specifier|protected
name|void
name|popFromRowBuffer
parameter_list|(
name|RowNodeReader
name|rowNodeBeingPopped
parameter_list|)
block|{
name|rowLength
operator|-=
name|rowNodeBeingPopped
operator|.
name|getTokenLength
argument_list|()
expr_stmt|;
operator|--
name|rowLength
expr_stmt|;
comment|// pop the parent's fan byte
block|}
specifier|protected
name|boolean
name|hasOccurrences
parameter_list|()
block|{
return|return
name|currentRowNode
operator|!=
literal|null
operator|&&
name|currentRowNode
operator|.
name|hasOccurrences
argument_list|()
return|;
block|}
specifier|protected
name|boolean
name|isBranch
parameter_list|()
block|{
return|return
name|currentRowNode
operator|!=
literal|null
operator|&&
operator|!
name|currentRowNode
operator|.
name|hasOccurrences
argument_list|()
operator|&&
name|currentRowNode
operator|.
name|hasChildren
argument_list|()
return|;
block|}
specifier|protected
name|boolean
name|isNub
parameter_list|()
block|{
return|return
name|currentRowNode
operator|!=
literal|null
operator|&&
name|currentRowNode
operator|.
name|hasOccurrences
argument_list|()
operator|&&
name|currentRowNode
operator|.
name|hasChildren
argument_list|()
return|;
block|}
specifier|protected
name|boolean
name|isLeaf
parameter_list|()
block|{
return|return
name|currentRowNode
operator|!=
literal|null
operator|&&
name|currentRowNode
operator|.
name|hasOccurrences
argument_list|()
operator|&&
operator|!
name|currentRowNode
operator|.
name|hasChildren
argument_list|()
return|;
block|}
comment|//TODO expose this in a PrefixTreeScanner interface
specifier|public
name|boolean
name|isBeforeFirst
parameter_list|()
block|{
return|return
name|beforeFirst
return|;
block|}
specifier|public
name|boolean
name|isAfterLast
parameter_list|()
block|{
return|return
name|afterLast
return|;
block|}
specifier|protected
name|boolean
name|isOutOfBounds
parameter_list|()
block|{
return|return
name|beforeFirst
operator|||
name|afterLast
return|;
block|}
specifier|protected
name|boolean
name|isFirstCellInRow
parameter_list|()
block|{
return|return
name|currentCellIndex
operator|==
literal|0
return|;
block|}
specifier|protected
name|boolean
name|isLastCellInRow
parameter_list|()
block|{
return|return
name|currentCellIndex
operator|==
name|currentRowNode
operator|.
name|getLastCellIndex
argument_list|()
return|;
block|}
comment|/********************* fill in family/qualifier/ts/type/value ************/
specifier|protected
name|int
name|populateNonRowFieldsAndCompareTo
parameter_list|(
name|int
name|cellNum
parameter_list|,
name|Cell
name|key
parameter_list|)
block|{
name|populateNonRowFields
argument_list|(
name|cellNum
argument_list|)
expr_stmt|;
return|return
name|comparator
operator|.
name|compareKeyIgnoresMvcc
argument_list|(
name|this
argument_list|,
name|key
argument_list|)
return|;
block|}
specifier|protected
name|void
name|populateFirstNonRowFields
parameter_list|()
block|{
name|populateNonRowFields
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|populatePreviousNonRowFields
parameter_list|()
block|{
name|populateNonRowFields
argument_list|(
name|currentCellIndex
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|populateLastNonRowFields
parameter_list|()
block|{
name|populateNonRowFields
argument_list|(
name|currentRowNode
operator|.
name|getLastCellIndex
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|populateNonRowFields
parameter_list|(
name|int
name|cellIndex
parameter_list|)
block|{
name|currentCellIndex
operator|=
name|cellIndex
expr_stmt|;
name|populateFamily
argument_list|()
expr_stmt|;
name|populateQualifier
argument_list|()
expr_stmt|;
comment|// Read tags only if there are tags in the meta
if|if
condition|(
name|blockMeta
operator|.
name|getNumTagsBytes
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|populateTag
argument_list|()
expr_stmt|;
block|}
name|populateTimestamp
argument_list|()
expr_stmt|;
name|populateMvccVersion
argument_list|()
expr_stmt|;
name|populateType
argument_list|()
expr_stmt|;
name|populateValueOffsets
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|populateFamily
parameter_list|()
block|{
name|int
name|familyTreeIndex
init|=
name|currentRowNode
operator|.
name|getFamilyOffset
argument_list|(
name|currentCellIndex
argument_list|,
name|blockMeta
argument_list|)
decl_stmt|;
name|familyOffset
operator|=
name|familyReader
operator|.
name|populateBuffer
argument_list|(
name|familyTreeIndex
argument_list|)
operator|.
name|getColumnOffset
argument_list|()
expr_stmt|;
name|familyLength
operator|=
name|familyReader
operator|.
name|getColumnLength
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|populateQualifier
parameter_list|()
block|{
name|int
name|qualifierTreeIndex
init|=
name|currentRowNode
operator|.
name|getColumnOffset
argument_list|(
name|currentCellIndex
argument_list|,
name|blockMeta
argument_list|)
decl_stmt|;
name|qualifierOffset
operator|=
name|qualifierReader
operator|.
name|populateBuffer
argument_list|(
name|qualifierTreeIndex
argument_list|)
operator|.
name|getColumnOffset
argument_list|()
expr_stmt|;
name|qualifierLength
operator|=
name|qualifierReader
operator|.
name|getColumnLength
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|populateTag
parameter_list|()
block|{
name|int
name|tagTreeIndex
init|=
name|currentRowNode
operator|.
name|getTagOffset
argument_list|(
name|currentCellIndex
argument_list|,
name|blockMeta
argument_list|)
decl_stmt|;
name|tagsOffset
operator|=
name|tagsReader
operator|.
name|populateBuffer
argument_list|(
name|tagTreeIndex
argument_list|)
operator|.
name|getColumnOffset
argument_list|()
expr_stmt|;
name|tagsLength
operator|=
name|tagsReader
operator|.
name|getColumnLength
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|populateTimestamp
parameter_list|()
block|{
if|if
condition|(
name|blockMeta
operator|.
name|isAllSameTimestamp
argument_list|()
condition|)
block|{
name|timestamp
operator|=
name|blockMeta
operator|.
name|getMinTimestamp
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|int
name|timestampIndex
init|=
name|currentRowNode
operator|.
name|getTimestampIndex
argument_list|(
name|currentCellIndex
argument_list|,
name|blockMeta
argument_list|)
decl_stmt|;
name|timestamp
operator|=
name|timestampDecoder
operator|.
name|getLong
argument_list|(
name|timestampIndex
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|populateMvccVersion
parameter_list|()
block|{
if|if
condition|(
name|blockMeta
operator|.
name|isAllSameMvccVersion
argument_list|()
condition|)
block|{
name|mvccVersion
operator|=
name|blockMeta
operator|.
name|getMinMvccVersion
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|int
name|mvccVersionIndex
init|=
name|currentRowNode
operator|.
name|getMvccVersionIndex
argument_list|(
name|currentCellIndex
argument_list|,
name|blockMeta
argument_list|)
decl_stmt|;
name|mvccVersion
operator|=
name|mvccVersionDecoder
operator|.
name|getMvccVersion
argument_list|(
name|mvccVersionIndex
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|populateType
parameter_list|()
block|{
name|int
name|typeInt
decl_stmt|;
if|if
condition|(
name|blockMeta
operator|.
name|isAllSameType
argument_list|()
condition|)
block|{
name|typeInt
operator|=
name|blockMeta
operator|.
name|getAllTypes
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|typeInt
operator|=
name|currentRowNode
operator|.
name|getType
argument_list|(
name|currentCellIndex
argument_list|,
name|blockMeta
argument_list|)
expr_stmt|;
block|}
name|type
operator|=
name|PrefixTreeCell
operator|.
name|TYPES
index|[
name|typeInt
index|]
expr_stmt|;
block|}
specifier|protected
name|void
name|populateValueOffsets
parameter_list|()
block|{
name|int
name|offsetIntoValueSection
init|=
name|currentRowNode
operator|.
name|getValueOffset
argument_list|(
name|currentCellIndex
argument_list|,
name|blockMeta
argument_list|)
decl_stmt|;
name|absoluteValueOffset
operator|=
name|blockMeta
operator|.
name|getAbsoluteValueOffset
argument_list|()
operator|+
name|offsetIntoValueSection
expr_stmt|;
name|valueLength
operator|=
name|currentRowNode
operator|.
name|getValueLength
argument_list|(
name|currentCellIndex
argument_list|,
name|blockMeta
argument_list|)
expr_stmt|;
name|this
operator|.
name|block
operator|.
name|asSubByteBuffer
argument_list|(
name|this
operator|.
name|absoluteValueOffset
argument_list|,
name|valueLength
argument_list|,
name|pair
argument_list|)
expr_stmt|;
block|}
comment|/**************** getters ***************************/
specifier|public
name|PrefixTreeBlockMeta
name|getBlockMeta
parameter_list|()
block|{
return|return
name|blockMeta
return|;
block|}
specifier|public
name|int
name|getMaxRowTreeStackNodes
parameter_list|()
block|{
return|return
name|rowNodes
operator|.
name|length
return|;
block|}
specifier|public
name|int
name|getRowBufferLength
parameter_list|()
block|{
return|return
name|rowBuffer
operator|.
name|length
return|;
block|}
specifier|public
name|int
name|getQualifierBufferLength
parameter_list|()
block|{
return|return
name|qualifierBuffer
operator|.
name|length
return|;
block|}
specifier|public
name|int
name|getTagBufferLength
parameter_list|()
block|{
return|return
name|tagsBuffer
operator|.
name|length
return|;
block|}
block|}
end_class

end_unit

