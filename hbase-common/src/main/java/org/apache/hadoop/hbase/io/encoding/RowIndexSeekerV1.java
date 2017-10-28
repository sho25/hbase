begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|io
operator|.
name|encoding
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
name|ByteBufferCell
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
name|ByteBufferKeyOnlyKeyValue
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
name|ByteBufferKeyValue
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
name|SizeCachedKeyValue
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
name|SizeCachedNoTagsKeyValue
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
name|io
operator|.
name|encoding
operator|.
name|AbstractDataBlockEncoder
operator|.
name|AbstractEncodedSeeker
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
name|ByteBufferUtils
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
name|ObjectIntPair
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RowIndexSeekerV1
extends|extends
name|AbstractEncodedSeeker
block|{
comment|// A temp pair object which will be reused by ByteBuff#asSubByteBuffer calls. This avoids too
comment|// many object creations.
specifier|protected
specifier|final
name|ObjectIntPair
argument_list|<
name|ByteBuffer
argument_list|>
name|tmpPair
init|=
operator|new
name|ObjectIntPair
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|ByteBuff
name|currentBuffer
decl_stmt|;
specifier|private
name|SeekerState
name|current
init|=
operator|new
name|SeekerState
argument_list|()
decl_stmt|;
comment|// always valid
specifier|private
name|SeekerState
name|previous
init|=
operator|new
name|SeekerState
argument_list|()
decl_stmt|;
comment|// may not be valid
specifier|private
name|int
name|rowNumber
decl_stmt|;
specifier|private
name|ByteBuff
name|rowOffsets
init|=
literal|null
decl_stmt|;
specifier|public
name|RowIndexSeekerV1
parameter_list|(
name|CellComparator
name|comparator
parameter_list|,
name|HFileBlockDecodingContext
name|decodingCtx
parameter_list|)
block|{
name|super
argument_list|(
name|comparator
argument_list|,
name|decodingCtx
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setCurrentBuffer
parameter_list|(
name|ByteBuff
name|buffer
parameter_list|)
block|{
name|int
name|onDiskSize
init|=
name|buffer
operator|.
name|getInt
argument_list|(
name|buffer
operator|.
name|limit
argument_list|()
operator|-
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
decl_stmt|;
comment|// Data part
name|ByteBuff
name|dup
init|=
name|buffer
operator|.
name|duplicate
argument_list|()
decl_stmt|;
name|dup
operator|.
name|position
argument_list|(
name|buffer
operator|.
name|position
argument_list|()
argument_list|)
expr_stmt|;
name|dup
operator|.
name|limit
argument_list|(
name|buffer
operator|.
name|position
argument_list|()
operator|+
name|onDiskSize
argument_list|)
expr_stmt|;
name|currentBuffer
operator|=
name|dup
operator|.
name|slice
argument_list|()
expr_stmt|;
name|current
operator|.
name|currentBuffer
operator|=
name|currentBuffer
expr_stmt|;
name|buffer
operator|.
name|skip
argument_list|(
name|onDiskSize
argument_list|)
expr_stmt|;
comment|// Row offset
name|rowNumber
operator|=
name|buffer
operator|.
name|getInt
argument_list|()
expr_stmt|;
name|int
name|totalRowOffsetsLength
init|=
name|Bytes
operator|.
name|SIZEOF_INT
operator|*
name|rowNumber
decl_stmt|;
name|ByteBuff
name|rowDup
init|=
name|buffer
operator|.
name|duplicate
argument_list|()
decl_stmt|;
name|rowDup
operator|.
name|position
argument_list|(
name|buffer
operator|.
name|position
argument_list|()
argument_list|)
expr_stmt|;
name|rowDup
operator|.
name|limit
argument_list|(
name|buffer
operator|.
name|position
argument_list|()
operator|+
name|totalRowOffsetsLength
argument_list|)
expr_stmt|;
name|rowOffsets
operator|=
name|rowDup
operator|.
name|slice
argument_list|()
expr_stmt|;
name|decodeFirst
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|getKey
parameter_list|()
block|{
if|if
condition|(
name|current
operator|.
name|keyBuffer
operator|.
name|hasArray
argument_list|()
condition|)
block|{
return|return
operator|new
name|KeyValue
operator|.
name|KeyOnlyKeyValue
argument_list|(
name|current
operator|.
name|keyBuffer
operator|.
name|array
argument_list|()
argument_list|,
name|current
operator|.
name|keyBuffer
operator|.
name|arrayOffset
argument_list|()
operator|+
name|current
operator|.
name|keyBuffer
operator|.
name|position
argument_list|()
argument_list|,
name|current
operator|.
name|keyLength
argument_list|)
return|;
block|}
else|else
block|{
name|byte
index|[]
name|key
init|=
operator|new
name|byte
index|[
name|current
operator|.
name|keyLength
index|]
decl_stmt|;
name|ByteBufferUtils
operator|.
name|copyFromBufferToArray
argument_list|(
name|key
argument_list|,
name|current
operator|.
name|keyBuffer
argument_list|,
name|current
operator|.
name|keyBuffer
operator|.
name|position
argument_list|()
argument_list|,
literal|0
argument_list|,
name|current
operator|.
name|keyLength
argument_list|)
expr_stmt|;
return|return
operator|new
name|KeyValue
operator|.
name|KeyOnlyKeyValue
argument_list|(
name|key
argument_list|,
literal|0
argument_list|,
name|current
operator|.
name|keyLength
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getValueShallowCopy
parameter_list|()
block|{
name|currentBuffer
operator|.
name|asSubByteBuffer
argument_list|(
name|current
operator|.
name|valueOffset
argument_list|,
name|current
operator|.
name|valueLength
argument_list|,
name|tmpPair
argument_list|)
expr_stmt|;
name|ByteBuffer
name|dup
init|=
name|tmpPair
operator|.
name|getFirst
argument_list|()
operator|.
name|duplicate
argument_list|()
decl_stmt|;
name|dup
operator|.
name|position
argument_list|(
name|tmpPair
operator|.
name|getSecond
argument_list|()
argument_list|)
expr_stmt|;
name|dup
operator|.
name|limit
argument_list|(
name|tmpPair
operator|.
name|getSecond
argument_list|()
operator|+
name|current
operator|.
name|valueLength
argument_list|)
expr_stmt|;
return|return
name|dup
operator|.
name|slice
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|getCell
parameter_list|()
block|{
return|return
name|current
operator|.
name|toCell
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|rewind
parameter_list|()
block|{
name|currentBuffer
operator|.
name|rewind
argument_list|()
expr_stmt|;
name|decodeFirst
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|()
block|{
if|if
condition|(
operator|!
name|currentBuffer
operator|.
name|hasRemaining
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|decodeNext
argument_list|()
expr_stmt|;
name|previous
operator|.
name|invalidate
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|private
name|int
name|binarySearch
parameter_list|(
name|Cell
name|seekCell
parameter_list|,
name|boolean
name|seekBefore
parameter_list|)
block|{
name|int
name|low
init|=
literal|0
decl_stmt|;
name|int
name|high
init|=
name|rowNumber
operator|-
literal|1
decl_stmt|;
name|int
name|mid
init|=
operator|(
name|low
operator|+
name|high
operator|)
operator|>>>
literal|1
decl_stmt|;
name|int
name|comp
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|low
operator|<=
name|high
condition|)
block|{
name|mid
operator|=
operator|(
name|low
operator|+
name|high
operator|)
operator|>>>
literal|1
expr_stmt|;
name|ByteBuffer
name|row
init|=
name|getRow
argument_list|(
name|mid
argument_list|)
decl_stmt|;
name|comp
operator|=
name|compareRows
argument_list|(
name|row
argument_list|,
name|seekCell
argument_list|)
expr_stmt|;
if|if
condition|(
name|comp
operator|<
literal|0
condition|)
block|{
name|low
operator|=
name|mid
operator|+
literal|1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|comp
operator|>
literal|0
condition|)
block|{
name|high
operator|=
name|mid
operator|-
literal|1
expr_stmt|;
block|}
else|else
block|{
comment|// key found
if|if
condition|(
name|seekBefore
condition|)
block|{
return|return
name|mid
operator|-
literal|1
return|;
block|}
else|else
block|{
return|return
name|mid
return|;
block|}
block|}
block|}
comment|// key not found.
if|if
condition|(
name|comp
operator|>
literal|0
condition|)
block|{
return|return
name|mid
operator|-
literal|1
return|;
block|}
else|else
block|{
return|return
name|mid
return|;
block|}
block|}
specifier|private
name|int
name|compareRows
parameter_list|(
name|ByteBuffer
name|row
parameter_list|,
name|Cell
name|seekCell
parameter_list|)
block|{
if|if
condition|(
name|seekCell
operator|instanceof
name|ByteBufferCell
condition|)
block|{
return|return
name|ByteBufferUtils
operator|.
name|compareTo
argument_list|(
name|row
argument_list|,
name|row
operator|.
name|position
argument_list|()
argument_list|,
name|row
operator|.
name|remaining
argument_list|()
argument_list|,
operator|(
operator|(
name|ByteBufferCell
operator|)
name|seekCell
operator|)
operator|.
name|getRowByteBuffer
argument_list|()
argument_list|,
operator|(
operator|(
name|ByteBufferCell
operator|)
name|seekCell
operator|)
operator|.
name|getRowPosition
argument_list|()
argument_list|,
name|seekCell
operator|.
name|getRowLength
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|ByteBufferUtils
operator|.
name|compareTo
argument_list|(
name|row
argument_list|,
name|row
operator|.
name|position
argument_list|()
argument_list|,
name|row
operator|.
name|remaining
argument_list|()
argument_list|,
name|seekCell
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|seekCell
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|seekCell
operator|.
name|getRowLength
argument_list|()
argument_list|)
return|;
block|}
block|}
specifier|private
name|ByteBuffer
name|getRow
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|int
name|offset
init|=
name|rowOffsets
operator|.
name|getIntAfterPosition
argument_list|(
name|index
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
decl_stmt|;
name|ByteBuff
name|block
init|=
name|currentBuffer
operator|.
name|duplicate
argument_list|()
decl_stmt|;
name|block
operator|.
name|position
argument_list|(
name|offset
operator|+
name|Bytes
operator|.
name|SIZEOF_LONG
argument_list|)
expr_stmt|;
name|short
name|rowLen
init|=
name|block
operator|.
name|getShort
argument_list|()
decl_stmt|;
name|block
operator|.
name|asSubByteBuffer
argument_list|(
name|block
operator|.
name|position
argument_list|()
argument_list|,
name|rowLen
argument_list|,
name|tmpPair
argument_list|)
expr_stmt|;
name|ByteBuffer
name|row
init|=
name|tmpPair
operator|.
name|getFirst
argument_list|()
decl_stmt|;
name|row
operator|.
name|position
argument_list|(
name|tmpPair
operator|.
name|getSecond
argument_list|()
argument_list|)
operator|.
name|limit
argument_list|(
name|tmpPair
operator|.
name|getSecond
argument_list|()
operator|+
name|rowLen
argument_list|)
expr_stmt|;
return|return
name|row
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|seekToKeyInBlock
parameter_list|(
name|Cell
name|seekCell
parameter_list|,
name|boolean
name|seekBefore
parameter_list|)
block|{
name|previous
operator|.
name|invalidate
argument_list|()
expr_stmt|;
name|int
name|index
init|=
name|binarySearch
argument_list|(
name|seekCell
argument_list|,
name|seekBefore
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|<
literal|0
condition|)
block|{
return|return
name|HConstants
operator|.
name|INDEX_KEY_MAGIC
return|;
comment|// using optimized index key
block|}
else|else
block|{
name|int
name|offset
init|=
name|rowOffsets
operator|.
name|getIntAfterPosition
argument_list|(
name|index
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
decl_stmt|;
if|if
condition|(
name|offset
operator|!=
literal|0
condition|)
block|{
name|decodeAtPosition
argument_list|(
name|offset
argument_list|)
expr_stmt|;
block|}
block|}
do|do
block|{
name|int
name|comp
decl_stmt|;
name|comp
operator|=
name|PrivateCellUtil
operator|.
name|compareKeyIgnoresMvcc
argument_list|(
name|comparator
argument_list|,
name|seekCell
argument_list|,
name|current
operator|.
name|currentKey
argument_list|)
expr_stmt|;
if|if
condition|(
name|comp
operator|==
literal|0
condition|)
block|{
comment|// exact match
if|if
condition|(
name|seekBefore
condition|)
block|{
if|if
condition|(
operator|!
name|previous
operator|.
name|isValid
argument_list|()
condition|)
block|{
comment|// The caller (seekBefore) has to ensure that we are not at the
comment|// first key in the block.
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Cannot seekBefore if "
operator|+
literal|"positioned at the first key in the block: key="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|seekCell
operator|.
name|getRowArray
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
name|moveToPrevious
argument_list|()
expr_stmt|;
return|return
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
if|if
condition|(
name|comp
operator|<
literal|0
condition|)
block|{
comment|// already too large, check previous
if|if
condition|(
name|previous
operator|.
name|isValid
argument_list|()
condition|)
block|{
name|moveToPrevious
argument_list|()
expr_stmt|;
block|}
else|else
block|{
return|return
name|HConstants
operator|.
name|INDEX_KEY_MAGIC
return|;
comment|// using optimized index key
block|}
return|return
literal|1
return|;
block|}
comment|// move to next, if more data is available
if|if
condition|(
name|currentBuffer
operator|.
name|hasRemaining
argument_list|()
condition|)
block|{
name|previous
operator|.
name|copyFromNext
argument_list|(
name|current
argument_list|)
expr_stmt|;
name|decodeNext
argument_list|()
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
block|}
do|while
condition|(
literal|true
condition|)
do|;
comment|// we hit the end of the block, not an exact match
return|return
literal|1
return|;
block|}
specifier|private
name|void
name|moveToPrevious
parameter_list|()
block|{
if|if
condition|(
operator|!
name|previous
operator|.
name|isValid
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Can move back only once and not in first key in the block."
argument_list|)
throw|;
block|}
name|SeekerState
name|tmp
init|=
name|previous
decl_stmt|;
name|previous
operator|=
name|current
expr_stmt|;
name|current
operator|=
name|tmp
expr_stmt|;
comment|// move after last key value
name|currentBuffer
operator|.
name|position
argument_list|(
name|current
operator|.
name|nextKvOffset
argument_list|)
expr_stmt|;
name|previous
operator|.
name|invalidate
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareKey
parameter_list|(
name|CellComparator
name|comparator
parameter_list|,
name|Cell
name|key
parameter_list|)
block|{
return|return
name|PrivateCellUtil
operator|.
name|compareKeyIgnoresMvcc
argument_list|(
name|comparator
argument_list|,
name|key
argument_list|,
name|current
operator|.
name|currentKey
argument_list|)
return|;
block|}
specifier|protected
name|void
name|decodeFirst
parameter_list|()
block|{
name|decodeNext
argument_list|()
expr_stmt|;
name|previous
operator|.
name|invalidate
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|decodeAtPosition
parameter_list|(
name|int
name|position
parameter_list|)
block|{
name|currentBuffer
operator|.
name|position
argument_list|(
name|position
argument_list|)
expr_stmt|;
name|decodeNext
argument_list|()
expr_stmt|;
name|previous
operator|.
name|invalidate
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|decodeNext
parameter_list|()
block|{
name|current
operator|.
name|startOffset
operator|=
name|currentBuffer
operator|.
name|position
argument_list|()
expr_stmt|;
name|long
name|ll
init|=
name|currentBuffer
operator|.
name|getLongAfterPosition
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// Read top half as an int of key length and bottom int as value length
name|current
operator|.
name|keyLength
operator|=
call|(
name|int
call|)
argument_list|(
name|ll
operator|>>
name|Integer
operator|.
name|SIZE
argument_list|)
expr_stmt|;
name|current
operator|.
name|valueLength
operator|=
call|(
name|int
call|)
argument_list|(
name|Bytes
operator|.
name|MASK_FOR_LOWER_INT_IN_LONG
operator|^
name|ll
argument_list|)
expr_stmt|;
name|currentBuffer
operator|.
name|skip
argument_list|(
name|Bytes
operator|.
name|SIZEOF_LONG
argument_list|)
expr_stmt|;
comment|// key part
name|currentBuffer
operator|.
name|asSubByteBuffer
argument_list|(
name|currentBuffer
operator|.
name|position
argument_list|()
argument_list|,
name|current
operator|.
name|keyLength
argument_list|,
name|tmpPair
argument_list|)
expr_stmt|;
name|ByteBuffer
name|key
init|=
name|tmpPair
operator|.
name|getFirst
argument_list|()
operator|.
name|duplicate
argument_list|()
decl_stmt|;
name|key
operator|.
name|position
argument_list|(
name|tmpPair
operator|.
name|getSecond
argument_list|()
argument_list|)
operator|.
name|limit
argument_list|(
name|tmpPair
operator|.
name|getSecond
argument_list|()
operator|+
name|current
operator|.
name|keyLength
argument_list|)
expr_stmt|;
name|current
operator|.
name|keyBuffer
operator|=
name|key
expr_stmt|;
name|currentBuffer
operator|.
name|skip
argument_list|(
name|current
operator|.
name|keyLength
argument_list|)
expr_stmt|;
comment|// value part
name|current
operator|.
name|valueOffset
operator|=
name|currentBuffer
operator|.
name|position
argument_list|()
expr_stmt|;
name|currentBuffer
operator|.
name|skip
argument_list|(
name|current
operator|.
name|valueLength
argument_list|)
expr_stmt|;
if|if
condition|(
name|includesTags
argument_list|()
condition|)
block|{
name|decodeTags
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|includesMvcc
argument_list|()
condition|)
block|{
name|current
operator|.
name|memstoreTS
operator|=
name|ByteBuff
operator|.
name|readVLong
argument_list|(
name|currentBuffer
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|current
operator|.
name|memstoreTS
operator|=
literal|0
expr_stmt|;
block|}
name|current
operator|.
name|nextKvOffset
operator|=
name|currentBuffer
operator|.
name|position
argument_list|()
expr_stmt|;
name|current
operator|.
name|currentKey
operator|.
name|setKey
argument_list|(
name|current
operator|.
name|keyBuffer
argument_list|,
name|tmpPair
operator|.
name|getSecond
argument_list|()
argument_list|,
name|current
operator|.
name|keyLength
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|decodeTags
parameter_list|()
block|{
name|current
operator|.
name|tagsLength
operator|=
name|currentBuffer
operator|.
name|getShortAfterPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|currentBuffer
operator|.
name|skip
argument_list|(
name|Bytes
operator|.
name|SIZEOF_SHORT
argument_list|)
expr_stmt|;
name|current
operator|.
name|tagsOffset
operator|=
name|currentBuffer
operator|.
name|position
argument_list|()
expr_stmt|;
name|currentBuffer
operator|.
name|skip
argument_list|(
name|current
operator|.
name|tagsLength
argument_list|)
expr_stmt|;
block|}
specifier|private
class|class
name|SeekerState
block|{
comment|/**      * The size of a (key length, value length) tuple that prefixes each entry      * in a data block.      */
specifier|public
specifier|final
specifier|static
name|int
name|KEY_VALUE_LEN_SIZE
init|=
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
decl_stmt|;
specifier|protected
name|ByteBuff
name|currentBuffer
decl_stmt|;
specifier|protected
name|int
name|startOffset
init|=
operator|-
literal|1
decl_stmt|;
specifier|protected
name|int
name|valueOffset
init|=
operator|-
literal|1
decl_stmt|;
specifier|protected
name|int
name|keyLength
decl_stmt|;
specifier|protected
name|int
name|valueLength
decl_stmt|;
specifier|protected
name|int
name|tagsLength
init|=
literal|0
decl_stmt|;
specifier|protected
name|int
name|tagsOffset
init|=
operator|-
literal|1
decl_stmt|;
specifier|protected
name|ByteBuffer
name|keyBuffer
init|=
literal|null
decl_stmt|;
specifier|protected
name|long
name|memstoreTS
decl_stmt|;
specifier|protected
name|int
name|nextKvOffset
decl_stmt|;
comment|// buffer backed keyonlyKV
specifier|private
name|ByteBufferKeyOnlyKeyValue
name|currentKey
init|=
operator|new
name|ByteBufferKeyOnlyKeyValue
argument_list|()
decl_stmt|;
specifier|protected
name|boolean
name|isValid
parameter_list|()
block|{
return|return
name|valueOffset
operator|!=
operator|-
literal|1
return|;
block|}
specifier|protected
name|void
name|invalidate
parameter_list|()
block|{
name|valueOffset
operator|=
operator|-
literal|1
expr_stmt|;
name|currentKey
operator|=
operator|new
name|ByteBufferKeyOnlyKeyValue
argument_list|()
expr_stmt|;
name|currentBuffer
operator|=
literal|null
expr_stmt|;
block|}
comment|/**      * Copy the state from the next one into this instance (the previous state placeholder). Used to      * save the previous state when we are advancing the seeker to the next key/value.      */
specifier|protected
name|void
name|copyFromNext
parameter_list|(
name|SeekerState
name|nextState
parameter_list|)
block|{
name|keyBuffer
operator|=
name|nextState
operator|.
name|keyBuffer
expr_stmt|;
name|currentKey
operator|.
name|setKey
argument_list|(
name|nextState
operator|.
name|keyBuffer
argument_list|,
name|nextState
operator|.
name|currentKey
operator|.
name|getRowPosition
argument_list|()
operator|-
name|Bytes
operator|.
name|SIZEOF_SHORT
argument_list|,
name|nextState
operator|.
name|keyLength
argument_list|)
expr_stmt|;
name|startOffset
operator|=
name|nextState
operator|.
name|startOffset
expr_stmt|;
name|valueOffset
operator|=
name|nextState
operator|.
name|valueOffset
expr_stmt|;
name|keyLength
operator|=
name|nextState
operator|.
name|keyLength
expr_stmt|;
name|valueLength
operator|=
name|nextState
operator|.
name|valueLength
expr_stmt|;
name|nextKvOffset
operator|=
name|nextState
operator|.
name|nextKvOffset
expr_stmt|;
name|memstoreTS
operator|=
name|nextState
operator|.
name|memstoreTS
expr_stmt|;
name|currentBuffer
operator|=
name|nextState
operator|.
name|currentBuffer
expr_stmt|;
name|tagsOffset
operator|=
name|nextState
operator|.
name|tagsOffset
expr_stmt|;
name|tagsLength
operator|=
name|nextState
operator|.
name|tagsLength
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|CellUtil
operator|.
name|getCellKeyAsString
argument_list|(
name|toCell
argument_list|()
argument_list|)
return|;
block|}
specifier|protected
name|int
name|getCellBufSize
parameter_list|()
block|{
name|int
name|kvBufSize
init|=
name|KEY_VALUE_LEN_SIZE
operator|+
name|keyLength
operator|+
name|valueLength
decl_stmt|;
if|if
condition|(
name|includesTags
argument_list|()
condition|)
block|{
name|kvBufSize
operator|+=
name|Bytes
operator|.
name|SIZEOF_SHORT
operator|+
name|tagsLength
expr_stmt|;
block|}
return|return
name|kvBufSize
return|;
block|}
specifier|public
name|Cell
name|toCell
parameter_list|()
block|{
name|Cell
name|ret
decl_stmt|;
name|int
name|cellBufSize
init|=
name|getCellBufSize
argument_list|()
decl_stmt|;
name|long
name|seqId
init|=
literal|0l
decl_stmt|;
if|if
condition|(
name|includesMvcc
argument_list|()
condition|)
block|{
name|seqId
operator|=
name|memstoreTS
expr_stmt|;
block|}
if|if
condition|(
name|currentBuffer
operator|.
name|hasArray
argument_list|()
condition|)
block|{
comment|// TODO : reduce the varieties of KV here. Check if based on a boolean
comment|// we can handle the 'no tags' case.
if|if
condition|(
name|tagsLength
operator|>
literal|0
condition|)
block|{
name|ret
operator|=
operator|new
name|SizeCachedKeyValue
argument_list|(
name|currentBuffer
operator|.
name|array
argument_list|()
argument_list|,
name|currentBuffer
operator|.
name|arrayOffset
argument_list|()
operator|+
name|startOffset
argument_list|,
name|cellBufSize
argument_list|,
name|seqId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ret
operator|=
operator|new
name|SizeCachedNoTagsKeyValue
argument_list|(
name|currentBuffer
operator|.
name|array
argument_list|()
argument_list|,
name|currentBuffer
operator|.
name|arrayOffset
argument_list|()
operator|+
name|startOffset
argument_list|,
name|cellBufSize
argument_list|,
name|seqId
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|currentBuffer
operator|.
name|asSubByteBuffer
argument_list|(
name|startOffset
argument_list|,
name|cellBufSize
argument_list|,
name|tmpPair
argument_list|)
expr_stmt|;
name|ByteBuffer
name|buf
init|=
name|tmpPair
operator|.
name|getFirst
argument_list|()
decl_stmt|;
if|if
condition|(
name|buf
operator|.
name|isDirect
argument_list|()
condition|)
block|{
name|ret
operator|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|buf
argument_list|,
name|tmpPair
operator|.
name|getSecond
argument_list|()
argument_list|,
name|cellBufSize
argument_list|,
name|seqId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|tagsLength
operator|>
literal|0
condition|)
block|{
name|ret
operator|=
operator|new
name|SizeCachedKeyValue
argument_list|(
name|buf
operator|.
name|array
argument_list|()
argument_list|,
name|buf
operator|.
name|arrayOffset
argument_list|()
operator|+
name|tmpPair
operator|.
name|getSecond
argument_list|()
argument_list|,
name|cellBufSize
argument_list|,
name|seqId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ret
operator|=
operator|new
name|SizeCachedNoTagsKeyValue
argument_list|(
name|buf
operator|.
name|array
argument_list|()
argument_list|,
name|buf
operator|.
name|arrayOffset
argument_list|()
operator|+
name|tmpPair
operator|.
name|getSecond
argument_list|()
argument_list|,
name|cellBufSize
argument_list|,
name|seqId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|ret
return|;
block|}
block|}
block|}
end_class

end_unit

