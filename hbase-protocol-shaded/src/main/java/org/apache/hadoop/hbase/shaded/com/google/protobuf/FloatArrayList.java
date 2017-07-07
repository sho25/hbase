begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|// Protocol Buffers - Google's data interchange format
end_comment

begin_comment
comment|// Copyright 2008 Google Inc.  All rights reserved.
end_comment

begin_comment
comment|// https://developers.google.com/protocol-buffers/
end_comment

begin_comment
comment|//
end_comment

begin_comment
comment|// Redistribution and use in source and binary forms, with or without
end_comment

begin_comment
comment|// modification, are permitted provided that the following conditions are
end_comment

begin_comment
comment|// met:
end_comment

begin_comment
comment|//
end_comment

begin_comment
comment|//     * Redistributions of source code must retain the above copyright
end_comment

begin_comment
comment|// notice, this list of conditions and the following disclaimer.
end_comment

begin_comment
comment|//     * Redistributions in binary form must reproduce the above
end_comment

begin_comment
comment|// copyright notice, this list of conditions and the following disclaimer
end_comment

begin_comment
comment|// in the documentation and/or other materials provided with the
end_comment

begin_comment
comment|// distribution.
end_comment

begin_comment
comment|//     * Neither the name of Google Inc. nor the names of its
end_comment

begin_comment
comment|// contributors may be used to endorse or promote products derived from
end_comment

begin_comment
comment|// this software without specific prior written permission.
end_comment

begin_comment
comment|//
end_comment

begin_comment
comment|// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
end_comment

begin_comment
comment|// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
end_comment

begin_comment
comment|// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
end_comment

begin_comment
comment|// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
end_comment

begin_comment
comment|// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
end_comment

begin_comment
comment|// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
end_comment

begin_comment
comment|// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
end_comment

begin_comment
comment|// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
end_comment

begin_comment
comment|// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
end_comment

begin_comment
comment|// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
end_comment

begin_comment
comment|// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Internal
operator|.
name|FloatList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|RandomAccess
import|;
end_import

begin_comment
comment|/**  * An implementation of {@link FloatList} on top of a primitive array.  *  * @author dweis@google.com (Daniel Weis)  */
end_comment

begin_class
specifier|final
class|class
name|FloatArrayList
extends|extends
name|AbstractProtobufList
argument_list|<
name|Float
argument_list|>
implements|implements
name|FloatList
implements|,
name|RandomAccess
block|{
specifier|private
specifier|static
specifier|final
name|FloatArrayList
name|EMPTY_LIST
init|=
operator|new
name|FloatArrayList
argument_list|()
decl_stmt|;
static|static
block|{
name|EMPTY_LIST
operator|.
name|makeImmutable
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
name|FloatArrayList
name|emptyList
parameter_list|()
block|{
return|return
name|EMPTY_LIST
return|;
block|}
comment|/**    * The backing store for the list.    */
specifier|private
name|float
index|[]
name|array
decl_stmt|;
comment|/**    * The size of the list distinct from the length of the array. That is, it is the number of    * elements set in the list.    */
specifier|private
name|int
name|size
decl_stmt|;
comment|/**    * Constructs a new mutable {@code FloatArrayList} with default capacity.    */
name|FloatArrayList
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|float
index|[
name|DEFAULT_CAPACITY
index|]
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructs a new mutable {@code FloatArrayList}    * containing the same elements as {@code other}.    */
specifier|private
name|FloatArrayList
parameter_list|(
name|float
index|[]
name|other
parameter_list|,
name|int
name|size
parameter_list|)
block|{
name|array
operator|=
name|other
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|FloatArrayList
operator|)
condition|)
block|{
return|return
name|super
operator|.
name|equals
argument_list|(
name|o
argument_list|)
return|;
block|}
name|FloatArrayList
name|other
init|=
operator|(
name|FloatArrayList
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|size
operator|!=
name|other
operator|.
name|size
condition|)
block|{
return|return
literal|false
return|;
block|}
specifier|final
name|float
index|[]
name|arr
init|=
name|other
operator|.
name|array
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|array
index|[
name|i
index|]
operator|!=
name|arr
index|[
name|i
index|]
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
literal|1
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|result
operator|=
operator|(
literal|31
operator|*
name|result
operator|)
operator|+
name|Float
operator|.
name|floatToIntBits
argument_list|(
name|array
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|FloatList
name|mutableCopyWithCapacity
parameter_list|(
name|int
name|capacity
parameter_list|)
block|{
if|if
condition|(
name|capacity
operator|<
name|size
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|()
throw|;
block|}
return|return
operator|new
name|FloatArrayList
argument_list|(
name|Arrays
operator|.
name|copyOf
argument_list|(
name|array
argument_list|,
name|capacity
argument_list|)
argument_list|,
name|size
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Float
name|get
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|getFloat
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|float
name|getFloat
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|ensureIndexInRange
argument_list|(
name|index
argument_list|)
expr_stmt|;
return|return
name|array
index|[
name|index
index|]
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|size
return|;
block|}
annotation|@
name|Override
specifier|public
name|Float
name|set
parameter_list|(
name|int
name|index
parameter_list|,
name|Float
name|element
parameter_list|)
block|{
return|return
name|setFloat
argument_list|(
name|index
argument_list|,
name|element
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|float
name|setFloat
parameter_list|(
name|int
name|index
parameter_list|,
name|float
name|element
parameter_list|)
block|{
name|ensureIsMutable
argument_list|()
expr_stmt|;
name|ensureIndexInRange
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|float
name|previousValue
init|=
name|array
index|[
name|index
index|]
decl_stmt|;
name|array
index|[
name|index
index|]
operator|=
name|element
expr_stmt|;
return|return
name|previousValue
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|add
parameter_list|(
name|int
name|index
parameter_list|,
name|Float
name|element
parameter_list|)
block|{
name|addFloat
argument_list|(
name|index
argument_list|,
name|element
argument_list|)
expr_stmt|;
block|}
comment|/**    * Like {@link #add(Float)} but more efficient in that it doesn't box the element.    */
annotation|@
name|Override
specifier|public
name|void
name|addFloat
parameter_list|(
name|float
name|element
parameter_list|)
block|{
name|addFloat
argument_list|(
name|size
argument_list|,
name|element
argument_list|)
expr_stmt|;
block|}
comment|/**    * Like {@link #add(int, Float)} but more efficient in that it doesn't box the element.    */
specifier|private
name|void
name|addFloat
parameter_list|(
name|int
name|index
parameter_list|,
name|float
name|element
parameter_list|)
block|{
name|ensureIsMutable
argument_list|()
expr_stmt|;
if|if
condition|(
name|index
argument_list|<
literal|0
operator|||
name|index
argument_list|>
name|size
condition|)
block|{
throw|throw
operator|new
name|IndexOutOfBoundsException
argument_list|(
name|makeOutOfBoundsExceptionMessage
argument_list|(
name|index
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|size
operator|<
name|array
operator|.
name|length
condition|)
block|{
comment|// Shift everything over to make room
name|System
operator|.
name|arraycopy
argument_list|(
name|array
argument_list|,
name|index
argument_list|,
name|array
argument_list|,
name|index
operator|+
literal|1
argument_list|,
name|size
operator|-
name|index
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Resize to 1.5x the size
name|int
name|length
init|=
operator|(
operator|(
name|size
operator|*
literal|3
operator|)
operator|/
literal|2
operator|)
operator|+
literal|1
decl_stmt|;
name|float
index|[]
name|newArray
init|=
operator|new
name|float
index|[
name|length
index|]
decl_stmt|;
comment|// Copy the first part directly
name|System
operator|.
name|arraycopy
argument_list|(
name|array
argument_list|,
literal|0
argument_list|,
name|newArray
argument_list|,
literal|0
argument_list|,
name|index
argument_list|)
expr_stmt|;
comment|// Copy the rest shifted over by one to make room
name|System
operator|.
name|arraycopy
argument_list|(
name|array
argument_list|,
name|index
argument_list|,
name|newArray
argument_list|,
name|index
operator|+
literal|1
argument_list|,
name|size
operator|-
name|index
argument_list|)
expr_stmt|;
name|array
operator|=
name|newArray
expr_stmt|;
block|}
name|array
index|[
name|index
index|]
operator|=
name|element
expr_stmt|;
name|size
operator|++
expr_stmt|;
name|modCount
operator|++
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|addAll
parameter_list|(
name|Collection
argument_list|<
name|?
extends|extends
name|Float
argument_list|>
name|collection
parameter_list|)
block|{
name|ensureIsMutable
argument_list|()
expr_stmt|;
if|if
condition|(
name|collection
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|()
throw|;
block|}
comment|// We specialize when adding another FloatArrayList to avoid boxing elements.
if|if
condition|(
operator|!
operator|(
name|collection
operator|instanceof
name|FloatArrayList
operator|)
condition|)
block|{
return|return
name|super
operator|.
name|addAll
argument_list|(
name|collection
argument_list|)
return|;
block|}
name|FloatArrayList
name|list
init|=
operator|(
name|FloatArrayList
operator|)
name|collection
decl_stmt|;
if|if
condition|(
name|list
operator|.
name|size
operator|==
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
name|int
name|overflow
init|=
name|Integer
operator|.
name|MAX_VALUE
operator|-
name|size
decl_stmt|;
if|if
condition|(
name|overflow
operator|<
name|list
operator|.
name|size
condition|)
block|{
comment|// We can't actually represent a list this large.
throw|throw
operator|new
name|OutOfMemoryError
argument_list|()
throw|;
block|}
name|int
name|newSize
init|=
name|size
operator|+
name|list
operator|.
name|size
decl_stmt|;
if|if
condition|(
name|newSize
operator|>
name|array
operator|.
name|length
condition|)
block|{
name|array
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|array
argument_list|,
name|newSize
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|arraycopy
argument_list|(
name|list
operator|.
name|array
argument_list|,
literal|0
argument_list|,
name|array
argument_list|,
name|size
argument_list|,
name|list
operator|.
name|size
argument_list|)
expr_stmt|;
name|size
operator|=
name|newSize
expr_stmt|;
name|modCount
operator|++
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|remove
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|ensureIsMutable
argument_list|()
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
name|size
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|o
operator|.
name|equals
argument_list|(
name|array
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|array
argument_list|,
name|i
operator|+
literal|1
argument_list|,
name|array
argument_list|,
name|i
argument_list|,
name|size
operator|-
name|i
argument_list|)
expr_stmt|;
name|size
operator|--
expr_stmt|;
name|modCount
operator|++
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|Float
name|remove
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|ensureIsMutable
argument_list|()
expr_stmt|;
name|ensureIndexInRange
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|float
name|value
init|=
name|array
index|[
name|index
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|array
argument_list|,
name|index
operator|+
literal|1
argument_list|,
name|array
argument_list|,
name|index
argument_list|,
name|size
operator|-
name|index
argument_list|)
expr_stmt|;
name|size
operator|--
expr_stmt|;
name|modCount
operator|++
expr_stmt|;
return|return
name|value
return|;
block|}
comment|/**    * Ensures that the provided {@code index} is within the range of {@code [0, size]}. Throws an    * {@link IndexOutOfBoundsException} if it is not.    *    * @param index the index to verify is in range    */
specifier|private
name|void
name|ensureIndexInRange
parameter_list|(
name|int
name|index
parameter_list|)
block|{
if|if
condition|(
name|index
operator|<
literal|0
operator|||
name|index
operator|>=
name|size
condition|)
block|{
throw|throw
operator|new
name|IndexOutOfBoundsException
argument_list|(
name|makeOutOfBoundsExceptionMessage
argument_list|(
name|index
argument_list|)
argument_list|)
throw|;
block|}
block|}
specifier|private
name|String
name|makeOutOfBoundsExceptionMessage
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
literal|"Index:"
operator|+
name|index
operator|+
literal|", Size:"
operator|+
name|size
return|;
block|}
block|}
end_class

end_unit

