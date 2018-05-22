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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|ArrayUtils
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|IndividualBytesFieldCell
implements|implements
name|ExtendedCell
implements|,
name|Cloneable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|FIXED_OVERHEAD
init|=
name|ClassSize
operator|.
name|align
argument_list|(
comment|// do alignment(padding gap)
name|ClassSize
operator|.
name|OBJECT
comment|// object header
operator|+
name|KeyValue
operator|.
name|TIMESTAMP_TYPE_SIZE
comment|// timestamp and type
operator|+
name|Bytes
operator|.
name|SIZEOF_LONG
comment|// sequence id
operator|+
literal|5
operator|*
name|ClassSize
operator|.
name|REFERENCE
argument_list|)
decl_stmt|;
comment|// references to all byte arrays: row, family, qualifier, value, tags
comment|// The following fields are backed by individual byte arrays
specifier|private
specifier|final
name|byte
index|[]
name|row
decl_stmt|;
specifier|private
specifier|final
name|int
name|rOffset
decl_stmt|;
specifier|private
specifier|final
name|int
name|rLength
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|family
decl_stmt|;
specifier|private
specifier|final
name|int
name|fOffset
decl_stmt|;
specifier|private
specifier|final
name|int
name|fLength
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|qualifier
decl_stmt|;
specifier|private
specifier|final
name|int
name|qOffset
decl_stmt|;
specifier|private
specifier|final
name|int
name|qLength
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|value
decl_stmt|;
specifier|private
specifier|final
name|int
name|vOffset
decl_stmt|;
specifier|private
specifier|final
name|int
name|vLength
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|tags
decl_stmt|;
comment|// A byte array, rather than an array of org.apache.hadoop.hbase.Tag
specifier|private
specifier|final
name|int
name|tagsOffset
decl_stmt|;
specifier|private
specifier|final
name|int
name|tagsLength
decl_stmt|;
comment|// Other fields
specifier|private
name|long
name|timestamp
decl_stmt|;
specifier|private
specifier|final
name|byte
name|type
decl_stmt|;
comment|// A byte, rather than org.apache.hadoop.hbase.KeyValue.Type
specifier|private
name|long
name|seqId
decl_stmt|;
specifier|public
name|IndividualBytesFieldCell
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
name|timestamp
parameter_list|,
name|KeyValue
operator|.
name|Type
name|type
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
name|this
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|timestamp
argument_list|,
name|type
argument_list|,
literal|0L
comment|/* sequence id */
argument_list|,
name|value
argument_list|,
literal|null
comment|/* tags */
argument_list|)
expr_stmt|;
block|}
specifier|public
name|IndividualBytesFieldCell
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
name|timestamp
parameter_list|,
name|KeyValue
operator|.
name|Type
name|type
parameter_list|,
name|long
name|seqId
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|byte
index|[]
name|tags
parameter_list|)
block|{
name|this
argument_list|(
name|row
argument_list|,
literal|0
argument_list|,
name|ArrayUtils
operator|.
name|getLength
argument_list|(
name|row
argument_list|)
argument_list|,
name|family
argument_list|,
literal|0
argument_list|,
name|ArrayUtils
operator|.
name|getLength
argument_list|(
name|family
argument_list|)
argument_list|,
name|qualifier
argument_list|,
literal|0
argument_list|,
name|ArrayUtils
operator|.
name|getLength
argument_list|(
name|qualifier
argument_list|)
argument_list|,
name|timestamp
argument_list|,
name|type
argument_list|,
name|seqId
argument_list|,
name|value
argument_list|,
literal|0
argument_list|,
name|ArrayUtils
operator|.
name|getLength
argument_list|(
name|value
argument_list|)
argument_list|,
name|tags
argument_list|,
literal|0
argument_list|,
name|ArrayUtils
operator|.
name|getLength
argument_list|(
name|tags
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|IndividualBytesFieldCell
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|int
name|rOffset
parameter_list|,
name|int
name|rLength
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|int
name|fOffset
parameter_list|,
name|int
name|fLength
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|int
name|qOffset
parameter_list|,
name|int
name|qLength
parameter_list|,
name|long
name|timestamp
parameter_list|,
name|KeyValue
operator|.
name|Type
name|type
parameter_list|,
name|long
name|seqId
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|int
name|vOffset
parameter_list|,
name|int
name|vLength
parameter_list|,
name|byte
index|[]
name|tags
parameter_list|,
name|int
name|tagsOffset
parameter_list|,
name|int
name|tagsLength
parameter_list|)
block|{
comment|// Check row, family, qualifier and value
name|KeyValue
operator|.
name|checkParameters
argument_list|(
name|row
argument_list|,
name|rLength
argument_list|,
comment|// row and row length
name|family
argument_list|,
name|fLength
argument_list|,
comment|// family and family length
name|qLength
argument_list|,
comment|// qualifier length
name|vLength
argument_list|)
expr_stmt|;
comment|// value length
comment|// Check timestamp
if|if
condition|(
name|timestamp
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Timestamp cannot be negative. ts="
operator|+
name|timestamp
argument_list|)
throw|;
block|}
comment|// Check tags
name|RawCell
operator|.
name|checkForTagsLength
argument_list|(
name|tagsLength
argument_list|)
expr_stmt|;
name|checkArrayBounds
argument_list|(
name|row
argument_list|,
name|rOffset
argument_list|,
name|rLength
argument_list|)
expr_stmt|;
name|checkArrayBounds
argument_list|(
name|family
argument_list|,
name|fOffset
argument_list|,
name|fLength
argument_list|)
expr_stmt|;
name|checkArrayBounds
argument_list|(
name|qualifier
argument_list|,
name|qOffset
argument_list|,
name|qLength
argument_list|)
expr_stmt|;
name|checkArrayBounds
argument_list|(
name|value
argument_list|,
name|vOffset
argument_list|,
name|vLength
argument_list|)
expr_stmt|;
name|checkArrayBounds
argument_list|(
name|tags
argument_list|,
name|tagsOffset
argument_list|,
name|tagsLength
argument_list|)
expr_stmt|;
comment|// No local copy is made, but reference to the input directly
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
name|this
operator|.
name|rOffset
operator|=
name|rOffset
expr_stmt|;
name|this
operator|.
name|rLength
operator|=
name|rLength
expr_stmt|;
name|this
operator|.
name|family
operator|=
name|family
expr_stmt|;
name|this
operator|.
name|fOffset
operator|=
name|fOffset
expr_stmt|;
name|this
operator|.
name|fLength
operator|=
name|fLength
expr_stmt|;
name|this
operator|.
name|qualifier
operator|=
name|qualifier
expr_stmt|;
name|this
operator|.
name|qOffset
operator|=
name|qOffset
expr_stmt|;
name|this
operator|.
name|qLength
operator|=
name|qLength
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
name|this
operator|.
name|vOffset
operator|=
name|vOffset
expr_stmt|;
name|this
operator|.
name|vLength
operator|=
name|vLength
expr_stmt|;
name|this
operator|.
name|tags
operator|=
name|tags
expr_stmt|;
name|this
operator|.
name|tagsOffset
operator|=
name|tagsOffset
expr_stmt|;
name|this
operator|.
name|tagsLength
operator|=
name|tagsLength
expr_stmt|;
comment|// Set others
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
operator|.
name|getCode
argument_list|()
expr_stmt|;
name|this
operator|.
name|seqId
operator|=
name|seqId
expr_stmt|;
block|}
specifier|private
name|void
name|checkArrayBounds
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
if|if
condition|(
name|offset
operator|<
literal|0
operator|||
name|length
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Negative number! offset="
operator|+
name|offset
operator|+
literal|"and length="
operator|+
name|length
argument_list|)
throw|;
block|}
if|if
condition|(
name|bytes
operator|==
literal|null
operator|&&
operator|(
name|offset
operator|!=
literal|0
operator|||
name|length
operator|!=
literal|0
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Null bytes array but offset="
operator|+
name|offset
operator|+
literal|"and length="
operator|+
name|length
argument_list|)
throw|;
block|}
if|if
condition|(
name|bytes
operator|!=
literal|null
operator|&&
name|bytes
operator|.
name|length
operator|<
name|offset
operator|+
name|length
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Out of bounds! bytes.length="
operator|+
name|bytes
operator|.
name|length
operator|+
literal|", offset="
operator|+
name|offset
operator|+
literal|", length="
operator|+
name|length
argument_list|)
throw|;
block|}
block|}
specifier|private
name|long
name|heapOverhead
parameter_list|()
block|{
return|return
name|FIXED_OVERHEAD
operator|+
name|ClassSize
operator|.
name|ARRAY
comment|// row      , can not be null
operator|+
operator|(
operator|(
name|family
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|ClassSize
operator|.
name|ARRAY
operator|)
comment|// family   , can be null
operator|+
operator|(
operator|(
name|qualifier
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|ClassSize
operator|.
name|ARRAY
operator|)
comment|// qualifier, can be null
operator|+
operator|(
operator|(
name|value
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|ClassSize
operator|.
name|ARRAY
operator|)
comment|// value    , can be null
operator|+
operator|(
operator|(
name|tags
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|ClassSize
operator|.
name|ARRAY
operator|)
return|;
comment|// tags     , can be null
block|}
comment|/**    * Implement Cell interface    */
comment|// 1) Row
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getRowArray
parameter_list|()
block|{
comment|// If row is null, the constructor will reject it, by {@link KeyValue#checkParameters()},
comment|// so it is safe to return row without checking.
return|return
name|row
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
name|rOffset
return|;
block|}
annotation|@
name|Override
specifier|public
name|short
name|getRowLength
parameter_list|()
block|{
comment|// If row is null or rLength is invalid, the constructor will reject it, by {@link KeyValue#checkParameters()},
comment|// so it is safe to call rLength and make the type conversion.
return|return
call|(
name|short
call|)
argument_list|(
name|rLength
argument_list|)
return|;
block|}
comment|// 2) Family
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getFamilyArray
parameter_list|()
block|{
comment|// Family could be null
return|return
operator|(
name|family
operator|==
literal|null
operator|)
condition|?
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
else|:
name|family
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
name|fOffset
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|getFamilyLength
parameter_list|()
block|{
comment|// If fLength is invalid, the constructor will reject it, by {@link KeyValue#checkParameters()},
comment|// so it is safe to make the type conversion.
return|return
call|(
name|byte
call|)
argument_list|(
name|fLength
argument_list|)
return|;
block|}
comment|// 3) Qualifier
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getQualifierArray
parameter_list|()
block|{
comment|// Qualifier could be null
return|return
operator|(
name|qualifier
operator|==
literal|null
operator|)
condition|?
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
else|:
name|qualifier
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
name|qOffset
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
name|qLength
return|;
block|}
comment|// 4) Timestamp
annotation|@
name|Override
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|timestamp
return|;
block|}
comment|//5) Type
annotation|@
name|Override
specifier|public
name|byte
name|getTypeByte
parameter_list|()
block|{
return|return
name|type
return|;
block|}
comment|//6) Sequence id
annotation|@
name|Override
specifier|public
name|long
name|getSequenceId
parameter_list|()
block|{
return|return
name|seqId
return|;
block|}
comment|//7) Value
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getValueArray
parameter_list|()
block|{
comment|// Value could be null
return|return
operator|(
name|value
operator|==
literal|null
operator|)
condition|?
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
else|:
name|value
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
name|vOffset
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
name|vLength
return|;
block|}
comment|// 8) Tags
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getTagsArray
parameter_list|()
block|{
comment|// Tags can could null
return|return
operator|(
name|tags
operator|==
literal|null
operator|)
condition|?
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
else|:
name|tags
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
name|tagsOffset
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getTagsLength
parameter_list|()
block|{
return|return
name|tagsLength
return|;
block|}
comment|/**    * Implement HeapSize interface    */
annotation|@
name|Override
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
comment|// Size of array headers are already included into overhead, so do not need to include it for each byte array
return|return
name|heapOverhead
argument_list|()
comment|// overhead, with array headers included
operator|+
name|ClassSize
operator|.
name|align
argument_list|(
name|getRowLength
argument_list|()
argument_list|)
comment|// row
operator|+
name|ClassSize
operator|.
name|align
argument_list|(
name|getFamilyLength
argument_list|()
argument_list|)
comment|// family
operator|+
name|ClassSize
operator|.
name|align
argument_list|(
name|getQualifierLength
argument_list|()
argument_list|)
comment|// qualifier
operator|+
name|ClassSize
operator|.
name|align
argument_list|(
name|getValueLength
argument_list|()
argument_list|)
comment|// value
operator|+
name|ClassSize
operator|.
name|align
argument_list|(
name|getTagsLength
argument_list|()
argument_list|)
return|;
comment|// tags
block|}
comment|/**    * Implement Cloneable interface    */
annotation|@
name|Override
specifier|public
name|Object
name|clone
parameter_list|()
throws|throws
name|CloneNotSupportedException
block|{
return|return
name|super
operator|.
name|clone
argument_list|()
return|;
comment|// only a shadow copy
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
if|if
condition|(
name|seqId
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Sequence Id cannot be negative. ts="
operator|+
name|seqId
argument_list|)
throw|;
block|}
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
name|void
name|setTimestamp
parameter_list|(
name|long
name|ts
parameter_list|)
block|{
if|if
condition|(
name|ts
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Timestamp cannot be negative. ts="
operator|+
name|ts
argument_list|)
throw|;
block|}
name|this
operator|.
name|timestamp
operator|=
name|ts
expr_stmt|;
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
block|{
name|setTimestamp
argument_list|(
name|Bytes
operator|.
name|toLong
argument_list|(
name|ts
argument_list|,
literal|0
argument_list|)
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
block|}
end_class

end_unit

