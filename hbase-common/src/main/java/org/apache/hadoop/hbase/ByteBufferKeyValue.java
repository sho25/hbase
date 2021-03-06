begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|OutputStream
import|;
end_import

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
name|ClassSize
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
comment|/**  * This Cell is an implementation of {@link ByteBufferExtendedCell} where the data resides in  * off heap/ on heap ByteBuffer  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ByteBufferKeyValue
extends|extends
name|ByteBufferExtendedCell
block|{
specifier|protected
specifier|final
name|ByteBuffer
name|buf
decl_stmt|;
specifier|protected
specifier|final
name|int
name|offset
decl_stmt|;
specifier|protected
specifier|final
name|int
name|length
decl_stmt|;
specifier|private
name|long
name|seqId
init|=
literal|0
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|FIXED_OVERHEAD
init|=
name|ClassSize
operator|.
name|OBJECT
operator|+
name|ClassSize
operator|.
name|REFERENCE
operator|+
operator|(
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
operator|)
operator|+
name|Bytes
operator|.
name|SIZEOF_LONG
decl_stmt|;
specifier|public
name|ByteBufferKeyValue
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|long
name|seqId
parameter_list|)
block|{
name|this
operator|.
name|buf
operator|=
name|buf
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
name|this
operator|.
name|seqId
operator|=
name|seqId
expr_stmt|;
block|}
specifier|public
name|ByteBufferKeyValue
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|this
operator|.
name|buf
operator|=
name|buf
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|ByteBuffer
name|getBuffer
parameter_list|()
block|{
return|return
name|this
operator|.
name|buf
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|int
name|getOffset
parameter_list|()
block|{
return|return
name|this
operator|.
name|offset
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getRowArray
parameter_list|()
block|{
return|return
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getRowOffset
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|short
name|getRowLength
parameter_list|()
block|{
return|return
name|ByteBufferUtils
operator|.
name|toShort
argument_list|(
name|this
operator|.
name|buf
argument_list|,
name|this
operator|.
name|offset
operator|+
name|KeyValue
operator|.
name|ROW_OFFSET
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getFamilyArray
parameter_list|()
block|{
return|return
name|CellUtil
operator|.
name|cloneFamily
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getFamilyOffset
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|getFamilyLength
parameter_list|()
block|{
return|return
name|getFamilyLength
argument_list|(
name|getFamilyLengthPosition
argument_list|()
argument_list|)
return|;
block|}
name|int
name|getFamilyLengthPosition
parameter_list|()
block|{
return|return
name|getFamilyLengthPosition
argument_list|(
name|getRowLength
argument_list|()
argument_list|)
return|;
block|}
name|int
name|getFamilyLengthPosition
parameter_list|(
name|int
name|rowLength
parameter_list|)
block|{
return|return
name|this
operator|.
name|offset
operator|+
name|KeyValue
operator|.
name|ROW_KEY_OFFSET
operator|+
name|rowLength
return|;
block|}
name|byte
name|getFamilyLength
parameter_list|(
name|int
name|famLenPos
parameter_list|)
block|{
return|return
name|ByteBufferUtils
operator|.
name|toByte
argument_list|(
name|this
operator|.
name|buf
argument_list|,
name|famLenPos
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getQualifierArray
parameter_list|()
block|{
return|return
name|CellUtil
operator|.
name|cloneQualifier
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getQualifierOffset
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getQualifierLength
parameter_list|()
block|{
return|return
name|getQualifierLength
argument_list|(
name|getKeyLength
argument_list|()
argument_list|,
name|getRowLength
argument_list|()
argument_list|,
name|getFamilyLength
argument_list|()
argument_list|)
return|;
block|}
name|int
name|getQualifierLength
parameter_list|(
name|int
name|keyLength
parameter_list|,
name|int
name|rlength
parameter_list|,
name|int
name|flength
parameter_list|)
block|{
return|return
name|keyLength
operator|-
operator|(
name|int
operator|)
name|KeyValue
operator|.
name|getKeyDataStructureSize
argument_list|(
name|rlength
argument_list|,
name|flength
argument_list|,
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|getTimestamp
argument_list|(
name|getKeyLength
argument_list|()
argument_list|)
return|;
block|}
name|long
name|getTimestamp
parameter_list|(
name|int
name|keyLength
parameter_list|)
block|{
name|int
name|offset
init|=
name|getTimestampOffset
argument_list|(
name|keyLength
argument_list|)
decl_stmt|;
return|return
name|ByteBufferUtils
operator|.
name|toLong
argument_list|(
name|this
operator|.
name|buf
argument_list|,
name|offset
argument_list|)
return|;
block|}
name|int
name|getKeyLength
parameter_list|()
block|{
return|return
name|ByteBufferUtils
operator|.
name|toInt
argument_list|(
name|this
operator|.
name|buf
argument_list|,
name|this
operator|.
name|offset
argument_list|)
return|;
block|}
specifier|private
name|int
name|getTimestampOffset
parameter_list|(
name|int
name|keyLen
parameter_list|)
block|{
return|return
name|this
operator|.
name|offset
operator|+
name|KeyValue
operator|.
name|ROW_OFFSET
operator|+
name|keyLen
operator|-
name|KeyValue
operator|.
name|TIMESTAMP_TYPE_SIZE
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|getTypeByte
parameter_list|()
block|{
return|return
name|getTypeByte
argument_list|(
name|getKeyLength
argument_list|()
argument_list|)
return|;
block|}
name|byte
name|getTypeByte
parameter_list|(
name|int
name|keyLen
parameter_list|)
block|{
return|return
name|ByteBufferUtils
operator|.
name|toByte
argument_list|(
name|this
operator|.
name|buf
argument_list|,
name|this
operator|.
name|offset
operator|+
name|keyLen
operator|-
literal|1
operator|+
name|KeyValue
operator|.
name|ROW_OFFSET
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getSequenceId
parameter_list|()
block|{
return|return
name|this
operator|.
name|seqId
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setSequenceId
parameter_list|(
name|long
name|seqId
parameter_list|)
block|{
name|this
operator|.
name|seqId
operator|=
name|seqId
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getValueArray
parameter_list|()
block|{
return|return
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getValueOffset
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getValueLength
parameter_list|()
block|{
return|return
name|ByteBufferUtils
operator|.
name|toInt
argument_list|(
name|this
operator|.
name|buf
argument_list|,
name|this
operator|.
name|offset
operator|+
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getTagsArray
parameter_list|()
block|{
return|return
name|PrivateCellUtil
operator|.
name|cloneTags
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getTagsOffset
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getTagsLength
parameter_list|()
block|{
name|int
name|tagsLen
init|=
name|this
operator|.
name|length
operator|-
operator|(
name|getKeyLength
argument_list|()
operator|+
name|getValueLength
argument_list|()
operator|+
name|KeyValue
operator|.
name|KEYVALUE_INFRASTRUCTURE_SIZE
operator|)
decl_stmt|;
if|if
condition|(
name|tagsLen
operator|>
literal|0
condition|)
block|{
comment|// There are some Tag bytes in the byte[]. So reduce 2 bytes which is
comment|// added to denote the tags
comment|// length
name|tagsLen
operator|-=
name|KeyValue
operator|.
name|TAGS_LENGTH_SIZE
expr_stmt|;
block|}
return|return
name|tagsLen
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getRowByteBuffer
parameter_list|()
block|{
return|return
name|this
operator|.
name|buf
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getRowPosition
parameter_list|()
block|{
return|return
name|this
operator|.
name|offset
operator|+
name|KeyValue
operator|.
name|ROW_KEY_OFFSET
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getFamilyByteBuffer
parameter_list|()
block|{
return|return
name|this
operator|.
name|buf
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getFamilyPosition
parameter_list|()
block|{
return|return
name|getFamilyPosition
argument_list|(
name|getFamilyLengthPosition
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|int
name|getFamilyPosition
parameter_list|(
name|int
name|familyLengthPosition
parameter_list|)
block|{
return|return
name|familyLengthPosition
operator|+
name|Bytes
operator|.
name|SIZEOF_BYTE
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getQualifierByteBuffer
parameter_list|()
block|{
return|return
name|this
operator|.
name|buf
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getQualifierPosition
parameter_list|()
block|{
return|return
name|getQualifierPosition
argument_list|(
name|getFamilyPosition
argument_list|()
argument_list|,
name|getFamilyLength
argument_list|()
argument_list|)
return|;
block|}
name|int
name|getQualifierPosition
parameter_list|(
name|int
name|familyPosition
parameter_list|,
name|int
name|familyLength
parameter_list|)
block|{
return|return
name|familyPosition
operator|+
name|familyLength
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getValueByteBuffer
parameter_list|()
block|{
return|return
name|this
operator|.
name|buf
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getValuePosition
parameter_list|()
block|{
return|return
name|this
operator|.
name|offset
operator|+
name|KeyValue
operator|.
name|ROW_OFFSET
operator|+
name|getKeyLength
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getTagsByteBuffer
parameter_list|()
block|{
return|return
name|this
operator|.
name|buf
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getTagsPosition
parameter_list|()
block|{
name|int
name|tagsLen
init|=
name|getTagsLength
argument_list|()
decl_stmt|;
if|if
condition|(
name|tagsLen
operator|==
literal|0
condition|)
block|{
return|return
name|this
operator|.
name|offset
operator|+
name|this
operator|.
name|length
return|;
block|}
return|return
name|this
operator|.
name|offset
operator|+
name|this
operator|.
name|length
operator|-
name|tagsLen
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|buf
operator|.
name|hasArray
argument_list|()
condition|)
block|{
return|return
name|ClassSize
operator|.
name|align
argument_list|(
name|FIXED_OVERHEAD
operator|+
name|length
argument_list|)
return|;
block|}
return|return
name|ClassSize
operator|.
name|align
argument_list|(
name|FIXED_OVERHEAD
argument_list|)
operator|+
name|this
operator|.
name|getSerializedSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|write
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|boolean
name|withTags
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|length
init|=
name|getSerializedSize
argument_list|(
name|withTags
argument_list|)
decl_stmt|;
name|ByteBufferUtils
operator|.
name|copyBufferToStream
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|buf
argument_list|,
name|this
operator|.
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
return|return
name|length
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getSerializedSize
parameter_list|(
name|boolean
name|withTags
parameter_list|)
block|{
if|if
condition|(
name|withTags
condition|)
block|{
return|return
name|this
operator|.
name|length
return|;
block|}
return|return
name|getKeyLength
argument_list|()
operator|+
name|this
operator|.
name|getValueLength
argument_list|()
operator|+
name|KeyValue
operator|.
name|KEYVALUE_INFRASTRUCTURE_SIZE
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getSerializedSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|length
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
name|ByteBufferUtils
operator|.
name|copyFromBufferToBuffer
argument_list|(
name|this
operator|.
name|buf
argument_list|,
name|buf
argument_list|,
name|this
operator|.
name|offset
argument_list|,
name|offset
argument_list|,
name|this
operator|.
name|length
argument_list|)
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
name|toString
argument_list|(
name|this
argument_list|,
literal|true
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setTimestamp
parameter_list|(
name|long
name|ts
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteBufferUtils
operator|.
name|copyFromArrayToBuffer
argument_list|(
name|this
operator|.
name|buf
argument_list|,
name|this
operator|.
name|getTimestampOffset
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|ts
argument_list|)
argument_list|,
literal|0
argument_list|,
name|Bytes
operator|.
name|SIZEOF_LONG
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|getTimestampOffset
parameter_list|()
block|{
return|return
name|this
operator|.
name|offset
operator|+
name|KeyValue
operator|.
name|KEYVALUE_INFRASTRUCTURE_SIZE
operator|+
name|getKeyLength
argument_list|()
operator|-
name|KeyValue
operator|.
name|TIMESTAMP_TYPE_SIZE
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setTimestamp
parameter_list|(
name|byte
index|[]
name|ts
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteBufferUtils
operator|.
name|copyFromArrayToBuffer
argument_list|(
name|this
operator|.
name|buf
argument_list|,
name|this
operator|.
name|getTimestampOffset
argument_list|()
argument_list|,
name|ts
argument_list|,
literal|0
argument_list|,
name|Bytes
operator|.
name|SIZEOF_LONG
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ExtendedCell
name|deepClone
parameter_list|()
block|{
name|byte
index|[]
name|copy
init|=
operator|new
name|byte
index|[
name|this
operator|.
name|length
index|]
decl_stmt|;
name|ByteBufferUtils
operator|.
name|copyFromBufferToArray
argument_list|(
name|copy
argument_list|,
name|this
operator|.
name|buf
argument_list|,
name|this
operator|.
name|offset
argument_list|,
literal|0
argument_list|,
name|this
operator|.
name|length
argument_list|)
expr_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|copy
argument_list|,
literal|0
argument_list|,
name|copy
operator|.
name|length
argument_list|)
decl_stmt|;
name|kv
operator|.
name|setSequenceId
argument_list|(
name|this
operator|.
name|getSequenceId
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|kv
return|;
block|}
comment|/**    * Needed doing 'contains' on List. Only compares the key portion, not the value.    */
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|other
operator|instanceof
name|Cell
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|CellUtil
operator|.
name|equals
argument_list|(
name|this
argument_list|,
operator|(
name|Cell
operator|)
name|other
argument_list|)
return|;
block|}
comment|/**    * In line with {@link #equals(Object)}, only uses the key portion, not the value.    */
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|calculateHashForKey
argument_list|(
name|this
argument_list|)
return|;
block|}
specifier|private
name|int
name|calculateHashForKey
parameter_list|(
name|ByteBufferExtendedCell
name|cell
parameter_list|)
block|{
name|int
name|rowHash
init|=
name|ByteBufferUtils
operator|.
name|hashCode
argument_list|(
name|cell
operator|.
name|getRowByteBuffer
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowPosition
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowLength
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|familyHash
init|=
name|ByteBufferUtils
operator|.
name|hashCode
argument_list|(
name|cell
operator|.
name|getFamilyByteBuffer
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyPosition
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|qualifierHash
init|=
name|ByteBufferUtils
operator|.
name|hashCode
argument_list|(
name|cell
operator|.
name|getQualifierByteBuffer
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierPosition
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|hash
init|=
literal|31
operator|*
name|rowHash
operator|+
name|familyHash
decl_stmt|;
name|hash
operator|=
literal|31
operator|*
name|hash
operator|+
name|qualifierHash
expr_stmt|;
name|hash
operator|=
literal|31
operator|*
name|hash
operator|+
operator|(
name|int
operator|)
name|cell
operator|.
name|getTimestamp
argument_list|()
expr_stmt|;
name|hash
operator|=
literal|31
operator|*
name|hash
operator|+
name|cell
operator|.
name|getTypeByte
argument_list|()
expr_stmt|;
return|return
name|hash
return|;
block|}
block|}
end_class

end_unit

