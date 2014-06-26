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
name|util
package|;
end_package

begin_comment
comment|/**  * An abstract implementation of the ByteRange API  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractByteRange
implements|implements
name|ByteRange
block|{
specifier|public
specifier|static
specifier|final
name|int
name|UNSET_HASH_VALUE
init|=
operator|-
literal|1
decl_stmt|;
comment|// Note to maintainers: Do not make these final, as the intention is to
comment|// reuse objects of this class
comment|/**    * The array containing the bytes in this range. It will be>= length.    */
specifier|protected
name|byte
index|[]
name|bytes
decl_stmt|;
comment|/**    * The index of the first byte in this range. {@code ByteRange.get(0)} will    * return bytes[offset].    */
specifier|protected
name|int
name|offset
decl_stmt|;
comment|/**    * The number of bytes in the range. Offset + length must be<= bytes.length    */
specifier|protected
name|int
name|length
decl_stmt|;
comment|/**    * Variable for lazy-caching the hashCode of this range. Useful for frequently    * used ranges, long-lived ranges, or long ranges.    */
specifier|protected
name|int
name|hash
init|=
name|UNSET_HASH_VALUE
decl_stmt|;
comment|//
comment|// methods for managing the backing array and range viewport
comment|//
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getBytes
parameter_list|()
block|{
return|return
name|bytes
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|abstract
name|ByteRange
name|unset
parameter_list|()
function_decl|;
annotation|@
name|Override
specifier|public
name|ByteRange
name|set
parameter_list|(
name|int
name|capacity
parameter_list|)
block|{
return|return
name|set
argument_list|(
operator|new
name|byte
index|[
name|capacity
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteRange
name|set
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
block|{
if|if
condition|(
literal|null
operator|==
name|bytes
condition|)
return|return
name|unset
argument_list|()
return|;
name|clearHashCache
argument_list|()
expr_stmt|;
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
literal|0
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|bytes
operator|.
name|length
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteRange
name|set
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
literal|null
operator|==
name|bytes
condition|)
return|return
name|unset
argument_list|()
return|;
name|clearHashCache
argument_list|()
expr_stmt|;
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
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getOffset
parameter_list|()
block|{
return|return
name|offset
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteRange
name|setOffset
parameter_list|(
name|int
name|offset
parameter_list|)
block|{
name|clearHashCache
argument_list|()
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getLength
parameter_list|()
block|{
return|return
name|length
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteRange
name|setLength
parameter_list|(
name|int
name|length
parameter_list|)
block|{
name|clearHashCache
argument_list|()
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|isEmpty
argument_list|(
name|this
argument_list|)
return|;
block|}
comment|/**    * @return true when {@code range} is of zero length, false otherwise.    */
specifier|public
specifier|static
name|boolean
name|isEmpty
parameter_list|(
name|ByteRange
name|range
parameter_list|)
block|{
return|return
name|range
operator|==
literal|null
operator|||
name|range
operator|.
name|getLength
argument_list|()
operator|==
literal|0
return|;
block|}
comment|//
comment|// methods for retrieving data
comment|//
annotation|@
name|Override
specifier|public
name|byte
name|get
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|bytes
index|[
name|offset
operator|+
name|index
index|]
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteRange
name|get
parameter_list|(
name|int
name|index
parameter_list|,
name|byte
index|[]
name|dst
parameter_list|)
block|{
if|if
condition|(
literal|0
operator|==
name|dst
operator|.
name|length
condition|)
return|return
name|this
return|;
return|return
name|get
argument_list|(
name|index
argument_list|,
name|dst
argument_list|,
literal|0
argument_list|,
name|dst
operator|.
name|length
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteRange
name|get
parameter_list|(
name|int
name|index
parameter_list|,
name|byte
index|[]
name|dst
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
literal|0
operator|==
name|length
condition|)
return|return
name|this
return|;
name|System
operator|.
name|arraycopy
argument_list|(
name|this
operator|.
name|bytes
argument_list|,
name|this
operator|.
name|offset
operator|+
name|index
argument_list|,
name|dst
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|short
name|getShort
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|int
name|offset
init|=
name|this
operator|.
name|offset
operator|+
name|index
decl_stmt|;
name|short
name|n
init|=
literal|0
decl_stmt|;
name|n
operator|^=
name|bytes
index|[
name|offset
index|]
operator|&
literal|0xFF
expr_stmt|;
name|n
operator|<<=
literal|8
expr_stmt|;
name|n
operator|^=
name|bytes
index|[
name|offset
operator|+
literal|1
index|]
operator|&
literal|0xFF
expr_stmt|;
return|return
name|n
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getInt
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|int
name|offset
init|=
name|this
operator|.
name|offset
operator|+
name|index
decl_stmt|;
name|int
name|n
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|offset
init|;
name|i
operator|<
operator|(
name|offset
operator|+
name|Bytes
operator|.
name|SIZEOF_INT
operator|)
condition|;
name|i
operator|++
control|)
block|{
name|n
operator|<<=
literal|8
expr_stmt|;
name|n
operator|^=
name|bytes
index|[
name|i
index|]
operator|&
literal|0xFF
expr_stmt|;
block|}
return|return
name|n
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLong
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|int
name|offset
init|=
name|this
operator|.
name|offset
operator|+
name|index
decl_stmt|;
name|long
name|l
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|offset
init|;
name|i
operator|<
name|offset
operator|+
name|Bytes
operator|.
name|SIZEOF_LONG
condition|;
name|i
operator|++
control|)
block|{
name|l
operator|<<=
literal|8
expr_stmt|;
name|l
operator|^=
name|bytes
index|[
name|i
index|]
operator|&
literal|0xFF
expr_stmt|;
block|}
return|return
name|l
return|;
block|}
comment|// Copied from com.google.protobuf.CodedInputStream
annotation|@
name|Override
specifier|public
name|long
name|getVLong
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|int
name|shift
init|=
literal|0
decl_stmt|;
name|long
name|result
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|shift
operator|<
literal|64
condition|)
block|{
specifier|final
name|byte
name|b
init|=
name|get
argument_list|(
name|index
operator|++
argument_list|)
decl_stmt|;
name|result
operator||=
call|(
name|long
call|)
argument_list|(
name|b
operator|&
literal|0x7F
argument_list|)
operator|<<
name|shift
expr_stmt|;
if|if
condition|(
operator|(
name|b
operator|&
literal|0x80
operator|)
operator|==
literal|0
condition|)
block|{
break|break;
block|}
name|shift
operator|+=
literal|7
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
specifier|static
name|int
name|getVLongSize
parameter_list|(
name|long
name|val
parameter_list|)
block|{
name|int
name|rPos
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|(
name|val
operator|&
operator|~
literal|0x7F
operator|)
operator|!=
literal|0
condition|)
block|{
name|val
operator|>>>=
literal|7
expr_stmt|;
name|rPos
operator|++
expr_stmt|;
block|}
return|return
name|rPos
operator|+
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|abstract
name|ByteRange
name|put
parameter_list|(
name|int
name|index
parameter_list|,
name|byte
name|val
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
specifier|abstract
name|ByteRange
name|put
parameter_list|(
name|int
name|index
parameter_list|,
name|byte
index|[]
name|val
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
specifier|abstract
name|ByteRange
name|put
parameter_list|(
name|int
name|index
parameter_list|,
name|byte
index|[]
name|val
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
specifier|abstract
name|ByteRange
name|putInt
parameter_list|(
name|int
name|index
parameter_list|,
name|int
name|val
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
specifier|abstract
name|ByteRange
name|putLong
parameter_list|(
name|int
name|index
parameter_list|,
name|long
name|val
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
specifier|abstract
name|ByteRange
name|putShort
parameter_list|(
name|int
name|index
parameter_list|,
name|short
name|val
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
specifier|abstract
name|int
name|putVLong
parameter_list|(
name|int
name|index
parameter_list|,
name|long
name|val
parameter_list|)
function_decl|;
comment|//
comment|// methods for duplicating the current instance
comment|//
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|deepCopyToNewArray
parameter_list|()
block|{
name|byte
index|[]
name|result
init|=
operator|new
name|byte
index|[
name|length
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|result
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|deepCopyTo
parameter_list|(
name|byte
index|[]
name|destination
parameter_list|,
name|int
name|destinationOffset
parameter_list|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|destination
argument_list|,
name|destinationOffset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|deepCopySubRangeTo
parameter_list|(
name|int
name|innerOffset
parameter_list|,
name|int
name|copyLength
parameter_list|,
name|byte
index|[]
name|destination
parameter_list|,
name|int
name|destinationOffset
parameter_list|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|bytes
argument_list|,
name|offset
operator|+
name|innerOffset
argument_list|,
name|destination
argument_list|,
name|destinationOffset
argument_list|,
name|copyLength
argument_list|)
expr_stmt|;
block|}
comment|//
comment|// methods used for comparison
comment|//
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
if|if
condition|(
name|isHashCached
argument_list|()
condition|)
block|{
comment|// hash is already calculated and cached
return|return
name|hash
return|;
block|}
if|if
condition|(
name|this
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// return 0 for empty ByteRange
name|hash
operator|=
literal|0
expr_stmt|;
return|return
name|hash
return|;
block|}
name|int
name|off
init|=
name|offset
decl_stmt|;
name|hash
operator|=
literal|0
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
name|length
condition|;
name|i
operator|++
control|)
block|{
name|hash
operator|=
literal|31
operator|*
name|hash
operator|+
name|bytes
index|[
name|off
operator|++
index|]
expr_stmt|;
block|}
return|return
name|hash
return|;
block|}
specifier|protected
name|boolean
name|isHashCached
parameter_list|()
block|{
return|return
name|hash
operator|!=
name|UNSET_HASH_VALUE
return|;
block|}
specifier|protected
name|void
name|clearHashCache
parameter_list|()
block|{
name|hash
operator|=
name|UNSET_HASH_VALUE
expr_stmt|;
block|}
comment|/**    * Bitwise comparison of each byte in the array. Unsigned comparison, not    * paying attention to java's signed bytes.    */
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|ByteRange
name|other
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|compareTo
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|other
operator|.
name|getBytes
argument_list|()
argument_list|,
name|other
operator|.
name|getOffset
argument_list|()
argument_list|,
name|other
operator|.
name|getLength
argument_list|()
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
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
block|}
end_class

end_unit

