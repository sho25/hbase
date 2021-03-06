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
comment|/**  * Tags are part of cells and helps to add metadata about them.  * Metadata could be ACLs, visibility labels, etc.  *<p>  * Each Tag is having a type (one byte) and value part. The max value length for a Tag is 65533.  *<p>  * See {@link TagType} for reserved tag types.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|Tag
block|{
specifier|public
specifier|final
specifier|static
name|int
name|TYPE_LENGTH_SIZE
init|=
name|Bytes
operator|.
name|SIZEOF_BYTE
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|int
name|TAG_LENGTH_SIZE
init|=
name|Bytes
operator|.
name|SIZEOF_SHORT
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|int
name|INFRASTRUCTURE_SIZE
init|=
name|TYPE_LENGTH_SIZE
operator|+
name|TAG_LENGTH_SIZE
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|MAX_TAG_LENGTH
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
operator|-
name|TAG_LENGTH_SIZE
decl_stmt|;
comment|/**    * Custom tags if created are suggested to be above this range. So that    * it does not overlap with internal tag types    */
specifier|public
specifier|static
specifier|final
name|byte
name|CUSTOM_TAG_TYPE_RANGE
init|=
operator|(
name|byte
operator|)
literal|64
decl_stmt|;
comment|/**    * @return the tag type    */
name|byte
name|getType
parameter_list|()
function_decl|;
comment|/**    * @return Offset of tag value within the backed buffer    */
name|int
name|getValueOffset
parameter_list|()
function_decl|;
comment|/**    * @return Length of tag value within the backed buffer    */
name|int
name|getValueLength
parameter_list|()
function_decl|;
comment|/**    * Tells whether or not this Tag is backed by a byte array.    * @return true when this Tag is backed by byte array    */
name|boolean
name|hasArray
parameter_list|()
function_decl|;
comment|/**    * @return The array containing the value bytes.    * @throws UnsupportedOperationException    *           when {@link #hasArray()} return false. Use {@link #getValueByteBuffer()} in such    *           situation    */
name|byte
index|[]
name|getValueArray
parameter_list|()
function_decl|;
comment|/**    * @return The {@link java.nio.ByteBuffer} containing the value bytes.    */
name|ByteBuffer
name|getValueByteBuffer
parameter_list|()
function_decl|;
comment|/**    * Returns tag value in a new byte array. Primarily for use client-side. If server-side, use    * {@link Tag#getValueArray()} with appropriate {@link Tag#getValueOffset()} and    * {@link Tag#getValueLength()} instead to save on allocations.    * @param tag The Tag whose value to be returned    * @return tag value in a new byte array.    */
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
block|}
end_interface

end_unit

