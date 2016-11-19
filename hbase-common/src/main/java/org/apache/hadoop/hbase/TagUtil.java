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
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|Tag
operator|.
name|TAG_LENGTH_SIZE
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
name|nio
operator|.
name|ByteBuffer
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
name|io
operator|.
name|util
operator|.
name|StreamUtils
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
name|Pair
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|TagUtil
block|{
comment|// If you would like to check the length of tags, please call {@link TagUtil#checkForTagsLength()}.
specifier|private
specifier|static
specifier|final
name|int
name|MAX_TAGS_LENGTH
init|=
operator|(
literal|2
operator|*
name|Short
operator|.
name|MAX_VALUE
operator|)
operator|+
literal|1
decl_stmt|;
comment|/**    * Private constructor to keep this class from being instantiated.    */
specifier|private
name|TagUtil
parameter_list|()
block|{}
comment|/**    * Returns tag value in a new byte array.    * Primarily for use client-side. If server-side, use    * {@link Tag#getValueArray()} with appropriate {@link Tag#getValueOffset()}    * and {@link Tag#getValueLength()} instead to save on allocations.    *    * @param tag The Tag whose value to be returned    * @return tag value in a new byte array.    */
specifier|public
specifier|static
name|byte
index|[]
name|cloneValue
parameter_list|(
name|Tag
name|tag
parameter_list|)
block|{
name|int
name|tagLength
init|=
name|tag
operator|.
name|getValueLength
argument_list|()
decl_stmt|;
name|byte
index|[]
name|tagArr
init|=
operator|new
name|byte
index|[
name|tagLength
index|]
decl_stmt|;
if|if
condition|(
name|tag
operator|.
name|hasArray
argument_list|()
condition|)
block|{
name|Bytes
operator|.
name|putBytes
argument_list|(
name|tagArr
argument_list|,
literal|0
argument_list|,
name|tag
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|tag
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|tagLength
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ByteBufferUtils
operator|.
name|copyFromBufferToArray
argument_list|(
name|tagArr
argument_list|,
name|tag
operator|.
name|getValueByteBuffer
argument_list|()
argument_list|,
name|tag
operator|.
name|getValueOffset
argument_list|()
argument_list|,
literal|0
argument_list|,
name|tagLength
argument_list|)
expr_stmt|;
block|}
return|return
name|tagArr
return|;
block|}
comment|/**    * Creates list of tags from given byte array, expected that it is in the expected tag format.    *    * @param b The byte array    * @param offset The offset in array where tag bytes begin    * @param length Total length of all tags bytes    * @return List of tags    */
specifier|public
specifier|static
name|List
argument_list|<
name|Tag
argument_list|>
name|asList
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|List
argument_list|<
name|Tag
argument_list|>
name|tags
init|=
operator|new
name|ArrayList
argument_list|<
name|Tag
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|pos
init|=
name|offset
decl_stmt|;
while|while
condition|(
name|pos
operator|<
name|offset
operator|+
name|length
condition|)
block|{
name|int
name|tagLen
init|=
name|Bytes
operator|.
name|readAsInt
argument_list|(
name|b
argument_list|,
name|pos
argument_list|,
name|TAG_LENGTH_SIZE
argument_list|)
decl_stmt|;
name|tags
operator|.
name|add
argument_list|(
operator|new
name|ArrayBackedTag
argument_list|(
name|b
argument_list|,
name|pos
argument_list|,
name|tagLen
operator|+
name|TAG_LENGTH_SIZE
argument_list|)
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|TAG_LENGTH_SIZE
operator|+
name|tagLen
expr_stmt|;
block|}
return|return
name|tags
return|;
block|}
comment|/**    * Creates list of tags from given ByteBuffer, expected that it is in the expected tag format.    *    * @param b The ByteBuffer    * @param offset The offset in ByteBuffer where tag bytes begin    * @param length Total length of all tags bytes    * @return List of tags    */
specifier|public
specifier|static
name|List
argument_list|<
name|Tag
argument_list|>
name|asList
parameter_list|(
name|ByteBuffer
name|b
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|List
argument_list|<
name|Tag
argument_list|>
name|tags
init|=
operator|new
name|ArrayList
argument_list|<
name|Tag
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|pos
init|=
name|offset
decl_stmt|;
while|while
condition|(
name|pos
operator|<
name|offset
operator|+
name|length
condition|)
block|{
name|int
name|tagLen
init|=
name|ByteBufferUtils
operator|.
name|readAsInt
argument_list|(
name|b
argument_list|,
name|pos
argument_list|,
name|TAG_LENGTH_SIZE
argument_list|)
decl_stmt|;
name|tags
operator|.
name|add
argument_list|(
operator|new
name|OffheapTag
argument_list|(
name|b
argument_list|,
name|pos
argument_list|,
name|tagLen
operator|+
name|TAG_LENGTH_SIZE
argument_list|)
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|TAG_LENGTH_SIZE
operator|+
name|tagLen
expr_stmt|;
block|}
return|return
name|tags
return|;
block|}
comment|/**    * Write a list of tags into a byte array    *    * @param tags The list of tags    * @return the serialized tag data as bytes    */
specifier|public
specifier|static
name|byte
index|[]
name|fromList
parameter_list|(
name|List
argument_list|<
name|Tag
argument_list|>
name|tags
parameter_list|)
block|{
if|if
condition|(
name|tags
operator|==
literal|null
operator|||
name|tags
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
return|;
block|}
name|int
name|length
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Tag
name|tag
range|:
name|tags
control|)
block|{
name|length
operator|+=
name|tag
operator|.
name|getValueLength
argument_list|()
operator|+
name|Tag
operator|.
name|INFRASTRUCTURE_SIZE
expr_stmt|;
block|}
name|byte
index|[]
name|b
init|=
operator|new
name|byte
index|[
name|length
index|]
decl_stmt|;
name|int
name|pos
init|=
literal|0
decl_stmt|;
name|int
name|tlen
decl_stmt|;
for|for
control|(
name|Tag
name|tag
range|:
name|tags
control|)
block|{
name|tlen
operator|=
name|tag
operator|.
name|getValueLength
argument_list|()
expr_stmt|;
name|pos
operator|=
name|Bytes
operator|.
name|putAsShort
argument_list|(
name|b
argument_list|,
name|pos
argument_list|,
name|tlen
operator|+
name|Tag
operator|.
name|TYPE_LENGTH_SIZE
argument_list|)
expr_stmt|;
name|pos
operator|=
name|Bytes
operator|.
name|putByte
argument_list|(
name|b
argument_list|,
name|pos
argument_list|,
name|tag
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|tag
operator|.
name|hasArray
argument_list|()
condition|)
block|{
name|pos
operator|=
name|Bytes
operator|.
name|putBytes
argument_list|(
name|b
argument_list|,
name|pos
argument_list|,
name|tag
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|tag
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|tlen
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ByteBufferUtils
operator|.
name|copyFromBufferToArray
argument_list|(
name|b
argument_list|,
name|tag
operator|.
name|getValueByteBuffer
argument_list|()
argument_list|,
name|tag
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|pos
argument_list|,
name|tlen
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|tlen
expr_stmt|;
block|}
block|}
return|return
name|b
return|;
block|}
comment|/**    * Converts the value bytes of the given tag into a long value    * @param tag The Tag    * @return value as long    */
specifier|public
specifier|static
name|long
name|getValueAsLong
parameter_list|(
name|Tag
name|tag
parameter_list|)
block|{
if|if
condition|(
name|tag
operator|.
name|hasArray
argument_list|()
condition|)
block|{
return|return
name|Bytes
operator|.
name|toLong
argument_list|(
name|tag
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|tag
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|tag
operator|.
name|getValueLength
argument_list|()
argument_list|)
return|;
block|}
return|return
name|ByteBufferUtils
operator|.
name|toLong
argument_list|(
name|tag
operator|.
name|getValueByteBuffer
argument_list|()
argument_list|,
name|tag
operator|.
name|getValueOffset
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Converts the value bytes of the given tag into a byte value    * @param tag The Tag    * @return value as byte    */
specifier|public
specifier|static
name|byte
name|getValueAsByte
parameter_list|(
name|Tag
name|tag
parameter_list|)
block|{
if|if
condition|(
name|tag
operator|.
name|hasArray
argument_list|()
condition|)
block|{
return|return
name|tag
operator|.
name|getValueArray
argument_list|()
index|[
name|tag
operator|.
name|getValueOffset
argument_list|()
index|]
return|;
block|}
return|return
name|ByteBufferUtils
operator|.
name|toByte
argument_list|(
name|tag
operator|.
name|getValueByteBuffer
argument_list|()
argument_list|,
name|tag
operator|.
name|getValueOffset
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Converts the value bytes of the given tag into a String value    * @param tag The Tag    * @return value as String    */
specifier|public
specifier|static
name|String
name|getValueAsString
parameter_list|(
name|Tag
name|tag
parameter_list|)
block|{
if|if
condition|(
name|tag
operator|.
name|hasArray
argument_list|()
condition|)
block|{
return|return
name|Bytes
operator|.
name|toString
argument_list|(
name|tag
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|tag
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|tag
operator|.
name|getValueLength
argument_list|()
argument_list|)
return|;
block|}
return|return
name|Bytes
operator|.
name|toString
argument_list|(
name|cloneValue
argument_list|(
name|tag
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Matches the value part of given tags    * @param t1 Tag to match the value    * @param t2 Tag to match the value    * @return True if values of both tags are same.    */
specifier|public
specifier|static
name|boolean
name|matchingValue
parameter_list|(
name|Tag
name|t1
parameter_list|,
name|Tag
name|t2
parameter_list|)
block|{
if|if
condition|(
name|t1
operator|.
name|hasArray
argument_list|()
operator|&&
name|t2
operator|.
name|hasArray
argument_list|()
condition|)
block|{
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|t1
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|t1
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|t1
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|t2
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|t2
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|t2
operator|.
name|getValueLength
argument_list|()
argument_list|)
return|;
block|}
if|if
condition|(
name|t1
operator|.
name|hasArray
argument_list|()
condition|)
block|{
return|return
name|ByteBufferUtils
operator|.
name|equals
argument_list|(
name|t2
operator|.
name|getValueByteBuffer
argument_list|()
argument_list|,
name|t2
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|t2
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|t1
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|t1
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|t1
operator|.
name|getValueLength
argument_list|()
argument_list|)
return|;
block|}
if|if
condition|(
name|t2
operator|.
name|hasArray
argument_list|()
condition|)
block|{
return|return
name|ByteBufferUtils
operator|.
name|equals
argument_list|(
name|t1
operator|.
name|getValueByteBuffer
argument_list|()
argument_list|,
name|t1
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|t1
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|t2
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|t2
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|t2
operator|.
name|getValueLength
argument_list|()
argument_list|)
return|;
block|}
return|return
name|ByteBufferUtils
operator|.
name|equals
argument_list|(
name|t1
operator|.
name|getValueByteBuffer
argument_list|()
argument_list|,
name|t1
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|t1
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|t2
operator|.
name|getValueByteBuffer
argument_list|()
argument_list|,
name|t2
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|t2
operator|.
name|getValueLength
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Copies the tag's value bytes to the given byte array    * @param tag The Tag    * @param out The byte array where to copy the Tag value.    * @param offset The offset within 'out' array where to copy the Tag value.    */
specifier|public
specifier|static
name|void
name|copyValueTo
parameter_list|(
name|Tag
name|tag
parameter_list|,
name|byte
index|[]
name|out
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
if|if
condition|(
name|tag
operator|.
name|hasArray
argument_list|()
condition|)
block|{
name|Bytes
operator|.
name|putBytes
argument_list|(
name|out
argument_list|,
name|offset
argument_list|,
name|tag
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|tag
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|tag
operator|.
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ByteBufferUtils
operator|.
name|copyFromBufferToArray
argument_list|(
name|out
argument_list|,
name|tag
operator|.
name|getValueByteBuffer
argument_list|()
argument_list|,
name|tag
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|offset
argument_list|,
name|tag
operator|.
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Reads an int value stored as a VInt at tag's given offset.    * @param tag The Tag    * @param offset The offset where VInt bytes begin    * @return A pair of the int value and number of bytes taken to store VInt    * @throws IOException When varint is malformed and not able to be read correctly    */
specifier|public
specifier|static
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|readVIntValuePart
parameter_list|(
name|Tag
name|tag
parameter_list|,
name|int
name|offset
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|tag
operator|.
name|hasArray
argument_list|()
condition|)
block|{
return|return
name|StreamUtils
operator|.
name|readRawVarint32
argument_list|(
name|tag
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|offset
argument_list|)
return|;
block|}
return|return
name|StreamUtils
operator|.
name|readRawVarint32
argument_list|(
name|tag
operator|.
name|getValueByteBuffer
argument_list|()
argument_list|,
name|offset
argument_list|)
return|;
block|}
comment|/**    * @return A List&lt;Tag&gt; of any Tags found in<code>cell</code> else null.    */
specifier|public
specifier|static
name|List
argument_list|<
name|Tag
argument_list|>
name|carryForwardTags
parameter_list|(
specifier|final
name|Cell
name|cell
parameter_list|)
block|{
return|return
name|carryForwardTags
argument_list|(
literal|null
argument_list|,
name|cell
argument_list|)
return|;
block|}
comment|/**    * Add to<code>tagsOrNull</code> any Tags<code>cell</code> is carrying or null if none.    */
specifier|public
specifier|static
name|List
argument_list|<
name|Tag
argument_list|>
name|carryForwardTags
parameter_list|(
specifier|final
name|List
argument_list|<
name|Tag
argument_list|>
name|tagsOrNull
parameter_list|,
specifier|final
name|Cell
name|cell
parameter_list|)
block|{
name|Iterator
argument_list|<
name|Tag
argument_list|>
name|itr
init|=
name|CellUtil
operator|.
name|tagsIterator
argument_list|(
name|cell
argument_list|)
decl_stmt|;
if|if
condition|(
name|itr
operator|==
name|EMPTY_TAGS_ITR
condition|)
block|{
comment|// If no Tags, return early.
return|return
name|tagsOrNull
return|;
block|}
name|List
argument_list|<
name|Tag
argument_list|>
name|tags
init|=
name|tagsOrNull
decl_stmt|;
if|if
condition|(
name|tags
operator|==
literal|null
condition|)
block|{
name|tags
operator|=
operator|new
name|ArrayList
argument_list|<
name|Tag
argument_list|>
argument_list|()
expr_stmt|;
block|}
while|while
condition|(
name|itr
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|tags
operator|.
name|add
argument_list|(
name|itr
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|tags
return|;
block|}
comment|/**    * @return Carry forward the TTL tag.    */
specifier|public
specifier|static
name|List
argument_list|<
name|Tag
argument_list|>
name|carryForwardTTLTag
parameter_list|(
specifier|final
name|List
argument_list|<
name|Tag
argument_list|>
name|tagsOrNull
parameter_list|,
specifier|final
name|long
name|ttl
parameter_list|)
block|{
if|if
condition|(
name|ttl
operator|==
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
return|return
name|tagsOrNull
return|;
block|}
name|List
argument_list|<
name|Tag
argument_list|>
name|tags
init|=
name|tagsOrNull
decl_stmt|;
comment|// If we are making the array in here, given we are the last thing checked, we'll be only thing
comment|// in the array so set its size to '1' (I saw this being done in earlier version of
comment|// tag-handling).
if|if
condition|(
name|tags
operator|==
literal|null
condition|)
block|{
name|tags
operator|=
operator|new
name|ArrayList
argument_list|<
name|Tag
argument_list|>
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|tags
operator|.
name|add
argument_list|(
operator|new
name|ArrayBackedTag
argument_list|(
name|TagType
operator|.
name|TTL_TAG_TYPE
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|ttl
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|tags
return|;
block|}
comment|/**    * Iterator returned when no Tags. Used by CellUtil too.    */
specifier|static
specifier|final
name|Iterator
argument_list|<
name|Tag
argument_list|>
name|EMPTY_TAGS_ITR
init|=
operator|new
name|Iterator
argument_list|<
name|Tag
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"IT_NO_SUCH_ELEMENT"
argument_list|,
name|justification
operator|=
literal|"Intentional"
argument_list|)
specifier|public
name|Tag
name|next
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
decl_stmt|;
comment|/**    * Check the length of tags. If it is invalid, throw IllegalArgumentException    *    * @param tagsLength    * @throws IllegalArgumentException if tagslength is invalid    */
specifier|public
specifier|static
name|void
name|checkForTagsLength
parameter_list|(
name|int
name|tagsLength
parameter_list|)
block|{
if|if
condition|(
name|tagsLength
operator|>
name|MAX_TAGS_LENGTH
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"tagslength "
operator|+
name|tagsLength
operator|+
literal|"> "
operator|+
name|MAX_TAGS_LENGTH
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

