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
operator|.
name|row
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
name|SimpleMutableByteRange
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
name|vint
operator|.
name|UFIntTool
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
name|vint
operator|.
name|UVIntTool
import|;
end_import

begin_comment
comment|/**  * Position one of these appropriately in the data block and you can call its methods to retrieve  * information necessary to decode the cells in the row.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RowNodeReader
block|{
comment|/************* fields ***********************************/
specifier|protected
name|byte
index|[]
name|block
decl_stmt|;
specifier|protected
name|int
name|offset
decl_stmt|;
specifier|protected
name|int
name|fanIndex
decl_stmt|;
specifier|protected
name|int
name|numCells
decl_stmt|;
specifier|protected
name|int
name|tokenOffset
decl_stmt|;
specifier|protected
name|int
name|tokenLength
decl_stmt|;
specifier|protected
name|int
name|fanOffset
decl_stmt|;
specifier|protected
name|int
name|fanOut
decl_stmt|;
specifier|protected
name|int
name|familyOffsetsOffset
decl_stmt|;
specifier|protected
name|int
name|qualifierOffsetsOffset
decl_stmt|;
specifier|protected
name|int
name|timestampIndexesOffset
decl_stmt|;
specifier|protected
name|int
name|mvccVersionIndexesOffset
decl_stmt|;
specifier|protected
name|int
name|operationTypesOffset
decl_stmt|;
specifier|protected
name|int
name|valueOffsetsOffset
decl_stmt|;
specifier|protected
name|int
name|valueLengthsOffset
decl_stmt|;
specifier|protected
name|int
name|tagOffsetsOffset
decl_stmt|;
specifier|protected
name|int
name|nextNodeOffsetsOffset
decl_stmt|;
comment|/******************* construct **************************/
specifier|public
name|void
name|initOnBlock
parameter_list|(
name|PrefixTreeBlockMeta
name|blockMeta
parameter_list|,
name|byte
index|[]
name|block
parameter_list|,
name|int
name|offset
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
name|offset
operator|=
name|offset
expr_stmt|;
name|resetFanIndex
argument_list|()
expr_stmt|;
name|this
operator|.
name|tokenLength
operator|=
name|UVIntTool
operator|.
name|getInt
argument_list|(
name|block
argument_list|,
name|offset
argument_list|)
expr_stmt|;
name|this
operator|.
name|tokenOffset
operator|=
name|offset
operator|+
name|UVIntTool
operator|.
name|numBytes
argument_list|(
name|tokenLength
argument_list|)
expr_stmt|;
name|this
operator|.
name|fanOut
operator|=
name|UVIntTool
operator|.
name|getInt
argument_list|(
name|block
argument_list|,
name|tokenOffset
operator|+
name|tokenLength
argument_list|)
expr_stmt|;
name|this
operator|.
name|fanOffset
operator|=
name|tokenOffset
operator|+
name|tokenLength
operator|+
name|UVIntTool
operator|.
name|numBytes
argument_list|(
name|fanOut
argument_list|)
expr_stmt|;
name|this
operator|.
name|numCells
operator|=
name|UVIntTool
operator|.
name|getInt
argument_list|(
name|block
argument_list|,
name|fanOffset
operator|+
name|fanOut
argument_list|)
expr_stmt|;
name|this
operator|.
name|familyOffsetsOffset
operator|=
name|fanOffset
operator|+
name|fanOut
operator|+
name|UVIntTool
operator|.
name|numBytes
argument_list|(
name|numCells
argument_list|)
expr_stmt|;
name|this
operator|.
name|qualifierOffsetsOffset
operator|=
name|familyOffsetsOffset
operator|+
name|numCells
operator|*
name|blockMeta
operator|.
name|getFamilyOffsetWidth
argument_list|()
expr_stmt|;
name|this
operator|.
name|tagOffsetsOffset
operator|=
name|this
operator|.
name|qualifierOffsetsOffset
operator|+
name|numCells
operator|*
name|blockMeta
operator|.
name|getQualifierOffsetWidth
argument_list|()
expr_stmt|;
comment|// TODO : This code may not be needed now..As we always consider tags to be present
if|if
condition|(
name|blockMeta
operator|.
name|getTagsOffsetWidth
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// Make both of them same so that we know that there are no tags
name|this
operator|.
name|tagOffsetsOffset
operator|=
name|this
operator|.
name|qualifierOffsetsOffset
expr_stmt|;
name|this
operator|.
name|timestampIndexesOffset
operator|=
name|qualifierOffsetsOffset
operator|+
name|numCells
operator|*
name|blockMeta
operator|.
name|getQualifierOffsetWidth
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|timestampIndexesOffset
operator|=
name|tagOffsetsOffset
operator|+
name|numCells
operator|*
name|blockMeta
operator|.
name|getTagsOffsetWidth
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|mvccVersionIndexesOffset
operator|=
name|timestampIndexesOffset
operator|+
name|numCells
operator|*
name|blockMeta
operator|.
name|getTimestampIndexWidth
argument_list|()
expr_stmt|;
name|this
operator|.
name|operationTypesOffset
operator|=
name|mvccVersionIndexesOffset
operator|+
name|numCells
operator|*
name|blockMeta
operator|.
name|getMvccVersionIndexWidth
argument_list|()
expr_stmt|;
name|this
operator|.
name|valueOffsetsOffset
operator|=
name|operationTypesOffset
operator|+
name|numCells
operator|*
name|blockMeta
operator|.
name|getKeyValueTypeWidth
argument_list|()
expr_stmt|;
name|this
operator|.
name|valueLengthsOffset
operator|=
name|valueOffsetsOffset
operator|+
name|numCells
operator|*
name|blockMeta
operator|.
name|getValueOffsetWidth
argument_list|()
expr_stmt|;
name|this
operator|.
name|nextNodeOffsetsOffset
operator|=
name|valueLengthsOffset
operator|+
name|numCells
operator|*
name|blockMeta
operator|.
name|getValueLengthWidth
argument_list|()
expr_stmt|;
block|}
comment|/******************** methods ****************************/
specifier|public
name|boolean
name|isLeaf
parameter_list|()
block|{
return|return
name|fanOut
operator|==
literal|0
return|;
block|}
specifier|public
name|boolean
name|isNub
parameter_list|()
block|{
return|return
name|fanOut
operator|>
literal|0
operator|&&
name|numCells
operator|>
literal|0
return|;
block|}
specifier|public
name|boolean
name|isBranch
parameter_list|()
block|{
return|return
name|fanOut
operator|>
literal|0
operator|&&
name|numCells
operator|==
literal|0
return|;
block|}
specifier|public
name|boolean
name|hasOccurrences
parameter_list|()
block|{
return|return
name|numCells
operator|>
literal|0
return|;
block|}
specifier|public
name|int
name|getTokenArrayOffset
parameter_list|()
block|{
return|return
name|tokenOffset
return|;
block|}
specifier|public
name|int
name|getTokenLength
parameter_list|()
block|{
return|return
name|tokenLength
return|;
block|}
specifier|public
name|byte
name|getFanByte
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|block
index|[
name|fanOffset
operator|+
name|i
index|]
return|;
block|}
comment|/**    * for debugging    */
specifier|protected
name|String
name|getFanByteReadable
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|block
argument_list|,
name|fanOffset
operator|+
name|i
argument_list|,
literal|1
argument_list|)
return|;
block|}
specifier|public
name|int
name|getFamilyOffset
parameter_list|(
name|int
name|index
parameter_list|,
name|PrefixTreeBlockMeta
name|blockMeta
parameter_list|)
block|{
name|int
name|fIntWidth
init|=
name|blockMeta
operator|.
name|getFamilyOffsetWidth
argument_list|()
decl_stmt|;
name|int
name|startIndex
init|=
name|familyOffsetsOffset
operator|+
name|fIntWidth
operator|*
name|index
decl_stmt|;
return|return
operator|(
name|int
operator|)
name|UFIntTool
operator|.
name|fromBytes
argument_list|(
name|block
argument_list|,
name|startIndex
argument_list|,
name|fIntWidth
argument_list|)
return|;
block|}
specifier|public
name|int
name|getColumnOffset
parameter_list|(
name|int
name|index
parameter_list|,
name|PrefixTreeBlockMeta
name|blockMeta
parameter_list|)
block|{
name|int
name|fIntWidth
init|=
name|blockMeta
operator|.
name|getQualifierOffsetWidth
argument_list|()
decl_stmt|;
name|int
name|startIndex
init|=
name|qualifierOffsetsOffset
operator|+
name|fIntWidth
operator|*
name|index
decl_stmt|;
return|return
operator|(
name|int
operator|)
name|UFIntTool
operator|.
name|fromBytes
argument_list|(
name|block
argument_list|,
name|startIndex
argument_list|,
name|fIntWidth
argument_list|)
return|;
block|}
specifier|public
name|int
name|getTagOffset
parameter_list|(
name|int
name|index
parameter_list|,
name|PrefixTreeBlockMeta
name|blockMeta
parameter_list|)
block|{
name|int
name|fIntWidth
init|=
name|blockMeta
operator|.
name|getTagsOffsetWidth
argument_list|()
decl_stmt|;
name|int
name|startIndex
init|=
name|tagOffsetsOffset
operator|+
name|fIntWidth
operator|*
name|index
decl_stmt|;
return|return
operator|(
name|int
operator|)
name|UFIntTool
operator|.
name|fromBytes
argument_list|(
name|block
argument_list|,
name|startIndex
argument_list|,
name|fIntWidth
argument_list|)
return|;
block|}
specifier|public
name|int
name|getTimestampIndex
parameter_list|(
name|int
name|index
parameter_list|,
name|PrefixTreeBlockMeta
name|blockMeta
parameter_list|)
block|{
name|int
name|fIntWidth
init|=
name|blockMeta
operator|.
name|getTimestampIndexWidth
argument_list|()
decl_stmt|;
name|int
name|startIndex
init|=
name|timestampIndexesOffset
operator|+
name|fIntWidth
operator|*
name|index
decl_stmt|;
return|return
operator|(
name|int
operator|)
name|UFIntTool
operator|.
name|fromBytes
argument_list|(
name|block
argument_list|,
name|startIndex
argument_list|,
name|fIntWidth
argument_list|)
return|;
block|}
specifier|public
name|int
name|getMvccVersionIndex
parameter_list|(
name|int
name|index
parameter_list|,
name|PrefixTreeBlockMeta
name|blockMeta
parameter_list|)
block|{
name|int
name|fIntWidth
init|=
name|blockMeta
operator|.
name|getMvccVersionIndexWidth
argument_list|()
decl_stmt|;
name|int
name|startIndex
init|=
name|mvccVersionIndexesOffset
operator|+
name|fIntWidth
operator|*
name|index
decl_stmt|;
return|return
operator|(
name|int
operator|)
name|UFIntTool
operator|.
name|fromBytes
argument_list|(
name|block
argument_list|,
name|startIndex
argument_list|,
name|fIntWidth
argument_list|)
return|;
block|}
specifier|public
name|int
name|getType
parameter_list|(
name|int
name|index
parameter_list|,
name|PrefixTreeBlockMeta
name|blockMeta
parameter_list|)
block|{
if|if
condition|(
name|blockMeta
operator|.
name|isAllSameType
argument_list|()
condition|)
block|{
return|return
name|blockMeta
operator|.
name|getAllTypes
argument_list|()
return|;
block|}
return|return
name|block
index|[
name|operationTypesOffset
operator|+
name|index
index|]
return|;
block|}
specifier|public
name|int
name|getValueOffset
parameter_list|(
name|int
name|index
parameter_list|,
name|PrefixTreeBlockMeta
name|blockMeta
parameter_list|)
block|{
name|int
name|fIntWidth
init|=
name|blockMeta
operator|.
name|getValueOffsetWidth
argument_list|()
decl_stmt|;
name|int
name|startIndex
init|=
name|valueOffsetsOffset
operator|+
name|fIntWidth
operator|*
name|index
decl_stmt|;
name|int
name|offset
init|=
operator|(
name|int
operator|)
name|UFIntTool
operator|.
name|fromBytes
argument_list|(
name|block
argument_list|,
name|startIndex
argument_list|,
name|fIntWidth
argument_list|)
decl_stmt|;
return|return
name|offset
return|;
block|}
specifier|public
name|int
name|getValueLength
parameter_list|(
name|int
name|index
parameter_list|,
name|PrefixTreeBlockMeta
name|blockMeta
parameter_list|)
block|{
name|int
name|fIntWidth
init|=
name|blockMeta
operator|.
name|getValueLengthWidth
argument_list|()
decl_stmt|;
name|int
name|startIndex
init|=
name|valueLengthsOffset
operator|+
name|fIntWidth
operator|*
name|index
decl_stmt|;
name|int
name|length
init|=
operator|(
name|int
operator|)
name|UFIntTool
operator|.
name|fromBytes
argument_list|(
name|block
argument_list|,
name|startIndex
argument_list|,
name|fIntWidth
argument_list|)
decl_stmt|;
return|return
name|length
return|;
block|}
specifier|public
name|int
name|getNextNodeOffset
parameter_list|(
name|int
name|index
parameter_list|,
name|PrefixTreeBlockMeta
name|blockMeta
parameter_list|)
block|{
name|int
name|fIntWidth
init|=
name|blockMeta
operator|.
name|getNextNodeOffsetWidth
argument_list|()
decl_stmt|;
name|int
name|startIndex
init|=
name|nextNodeOffsetsOffset
operator|+
name|fIntWidth
operator|*
name|index
decl_stmt|;
return|return
operator|(
name|int
operator|)
name|UFIntTool
operator|.
name|fromBytes
argument_list|(
name|block
argument_list|,
name|startIndex
argument_list|,
name|fIntWidth
argument_list|)
return|;
block|}
specifier|public
name|String
name|getBranchNubLeafIndicator
parameter_list|()
block|{
if|if
condition|(
name|isNub
argument_list|()
condition|)
block|{
return|return
literal|"N"
return|;
block|}
return|return
name|isBranch
argument_list|()
condition|?
literal|"B"
else|:
literal|"L"
return|;
block|}
specifier|public
name|boolean
name|hasChildren
parameter_list|()
block|{
return|return
name|fanOut
operator|>
literal|0
return|;
block|}
specifier|public
name|int
name|getLastFanIndex
parameter_list|()
block|{
return|return
name|fanOut
operator|-
literal|1
return|;
block|}
specifier|public
name|int
name|getLastCellIndex
parameter_list|()
block|{
return|return
name|numCells
operator|-
literal|1
return|;
block|}
specifier|public
name|int
name|getNumCells
parameter_list|()
block|{
return|return
name|numCells
return|;
block|}
specifier|public
name|int
name|getFanOut
parameter_list|()
block|{
return|return
name|fanOut
return|;
block|}
specifier|public
name|byte
index|[]
name|getToken
parameter_list|()
block|{
comment|// TODO pass in reusable ByteRange
return|return
operator|new
name|SimpleMutableByteRange
argument_list|(
name|block
argument_list|,
name|tokenOffset
argument_list|,
name|tokenLength
argument_list|)
operator|.
name|deepCopyToNewArray
argument_list|()
return|;
block|}
specifier|public
name|int
name|getOffset
parameter_list|()
block|{
return|return
name|offset
return|;
block|}
specifier|public
name|int
name|whichFanNode
parameter_list|(
name|byte
name|searchForByte
parameter_list|)
block|{
if|if
condition|(
operator|!
name|hasFan
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"This row node has no fan, so can't search it"
argument_list|)
throw|;
block|}
name|int
name|fanIndexInBlock
init|=
name|Bytes
operator|.
name|unsignedBinarySearch
argument_list|(
name|block
argument_list|,
name|fanOffset
argument_list|,
name|fanOffset
operator|+
name|fanOut
argument_list|,
name|searchForByte
argument_list|)
decl_stmt|;
if|if
condition|(
name|fanIndexInBlock
operator|>=
literal|0
condition|)
block|{
comment|// found it, but need to adjust for position of fan in overall block
return|return
name|fanIndexInBlock
operator|-
name|fanOffset
return|;
block|}
return|return
name|fanIndexInBlock
operator|+
name|fanOffset
operator|+
literal|1
return|;
comment|// didn't find it, so compensate in reverse
block|}
specifier|public
name|void
name|resetFanIndex
parameter_list|()
block|{
name|fanIndex
operator|=
operator|-
literal|1
expr_stmt|;
comment|// just the way the logic currently works
block|}
specifier|public
name|int
name|getFanIndex
parameter_list|()
block|{
return|return
name|fanIndex
return|;
block|}
specifier|public
name|void
name|setFanIndex
parameter_list|(
name|int
name|fanIndex
parameter_list|)
block|{
name|this
operator|.
name|fanIndex
operator|=
name|fanIndex
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasFan
parameter_list|()
block|{
return|return
name|fanOut
operator|>
literal|0
return|;
block|}
specifier|public
name|boolean
name|hasPreviousFanNodes
parameter_list|()
block|{
return|return
name|fanOut
operator|>
literal|0
operator|&&
name|fanIndex
operator|>
literal|0
return|;
block|}
specifier|public
name|boolean
name|hasMoreFanNodes
parameter_list|()
block|{
return|return
name|fanIndex
operator|<
name|getLastFanIndex
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isOnLastFanNode
parameter_list|()
block|{
return|return
operator|!
name|hasMoreFanNodes
argument_list|()
return|;
block|}
comment|/*************** standard methods **************************/
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
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"fan:"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|block
argument_list|,
name|fanOffset
argument_list|,
name|fanOut
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|",token:"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|block
argument_list|,
name|tokenOffset
argument_list|,
name|tokenLength
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|",numCells:"
operator|+
name|numCells
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|",fanIndex:"
operator|+
name|fanIndex
argument_list|)
expr_stmt|;
if|if
condition|(
name|fanIndex
operator|>=
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"("
operator|+
name|getFanByteReadable
argument_list|(
name|fanIndex
argument_list|)
operator|+
literal|")"
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
block|}
end_class

end_unit

