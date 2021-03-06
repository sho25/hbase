begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Bytes
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * This is a {@link Tag} implementation in which value is backed by an on heap byte array.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|ArrayBackedTag
implements|implements
name|Tag
block|{
specifier|private
specifier|final
name|byte
name|type
decl_stmt|;
comment|// TODO  extra type state needed?
specifier|private
specifier|final
name|byte
index|[]
name|bytes
decl_stmt|;
specifier|private
name|int
name|offset
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|length
init|=
literal|0
decl_stmt|;
comment|/**    * The special tag will write the length of each tag and that will be    * followed by the type and then the actual tag.    * So every time the length part is parsed we need to add + 1 byte to it to    * get the type and then get the actual tag.    */
specifier|public
name|ArrayBackedTag
parameter_list|(
name|byte
name|tagType
parameter_list|,
name|String
name|tag
parameter_list|)
block|{
name|this
argument_list|(
name|tagType
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tag
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Format for a tag :    * {@code<length of tag - 2 bytes><type code - 1 byte><tag>} tag length is serialized    * using 2 bytes only but as this will be unsigned, we can have max tag length of    * (Short.MAX_SIZE * 2) +1. It includes 1 byte type length and actual tag bytes length.    */
specifier|public
name|ArrayBackedTag
parameter_list|(
name|byte
name|tagType
parameter_list|,
name|byte
index|[]
name|tag
parameter_list|)
block|{
name|int
name|tagLength
init|=
name|tag
operator|.
name|length
operator|+
name|TYPE_LENGTH_SIZE
decl_stmt|;
if|if
condition|(
name|tagLength
operator|>
name|MAX_TAG_LENGTH
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid tag data being passed. Its length can not exceed "
operator|+
name|MAX_TAG_LENGTH
argument_list|)
throw|;
block|}
name|length
operator|=
name|TAG_LENGTH_SIZE
operator|+
name|tagLength
expr_stmt|;
name|bytes
operator|=
operator|new
name|byte
index|[
name|length
index|]
expr_stmt|;
name|int
name|pos
init|=
name|Bytes
operator|.
name|putAsShort
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|tagLength
argument_list|)
decl_stmt|;
name|pos
operator|=
name|Bytes
operator|.
name|putByte
argument_list|(
name|bytes
argument_list|,
name|pos
argument_list|,
name|tagType
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|putBytes
argument_list|(
name|bytes
argument_list|,
name|pos
argument_list|,
name|tag
argument_list|,
literal|0
argument_list|,
name|tag
operator|.
name|length
argument_list|)
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|tagType
expr_stmt|;
block|}
comment|/**    * Creates a Tag from the specified byte array and offset. Presumes    *<code>bytes</code> content starting at<code>offset</code> is formatted as    * a Tag blob.    * The bytes to include the tag type, tag length and actual tag bytes.    * @param offset offset to start of Tag    */
specifier|public
name|ArrayBackedTag
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
name|this
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|getLength
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|int
name|getLength
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
return|return
name|TAG_LENGTH_SIZE
operator|+
name|Bytes
operator|.
name|readAsInt
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|TAG_LENGTH_SIZE
argument_list|)
return|;
block|}
comment|/**    * Creates a Tag from the specified byte array, starting at offset, and for length    *<code>length</code>. Presumes<code>bytes</code> content starting at<code>offset</code> is    * formatted as a Tag blob.    */
specifier|public
name|ArrayBackedTag
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
name|length
operator|>
name|MAX_TAG_LENGTH
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid tag data being passed. Its length can not exceed "
operator|+
name|MAX_TAG_LENGTH
argument_list|)
throw|;
block|}
name|this
operator|.
name|bytes
operator|=
name|bytes
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
name|type
operator|=
name|bytes
index|[
name|offset
operator|+
name|TAG_LENGTH_SIZE
index|]
expr_stmt|;
block|}
comment|/**    * @return The byte array backing this Tag.    */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getValueArray
parameter_list|()
block|{
return|return
name|this
operator|.
name|bytes
return|;
block|}
comment|/**    * @return the tag type    */
annotation|@
name|Override
specifier|public
name|byte
name|getType
parameter_list|()
block|{
return|return
name|this
operator|.
name|type
return|;
block|}
comment|/**    * @return Length of actual tag bytes within the backed buffer    */
annotation|@
name|Override
specifier|public
name|int
name|getValueLength
parameter_list|()
block|{
return|return
name|this
operator|.
name|length
operator|-
name|INFRASTRUCTURE_SIZE
return|;
block|}
comment|/**    * @return Offset of actual tag bytes within the backed buffer    */
annotation|@
name|Override
specifier|public
name|int
name|getValueOffset
parameter_list|()
block|{
return|return
name|this
operator|.
name|offset
operator|+
name|INFRASTRUCTURE_SIZE
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasArray
parameter_list|()
block|{
return|return
literal|true
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
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"[Tag type : "
operator|+
name|this
operator|.
name|type
operator|+
literal|", value : "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|bytes
argument_list|,
name|getValueOffset
argument_list|()
argument_list|,
name|getValueLength
argument_list|()
argument_list|)
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

