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
operator|.
name|nio
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
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|nio
operator|.
name|channels
operator|.
name|ReadableByteChannel
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
name|ObjectIntPair
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
name|UnsafeAccess
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
name|UnsafeAvailChecker
import|;
end_import

begin_import
import|import
name|sun
operator|.
name|nio
operator|.
name|ch
operator|.
name|DirectBuffer
import|;
end_import

begin_comment
comment|/**  * An implementation of ByteBuff where a single BB backs the BBI. This just acts  * as a wrapper over a normal BB - offheap or onheap  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SingleByteBuff
extends|extends
name|ByteBuff
block|{
specifier|private
specifier|static
specifier|final
name|boolean
name|UNSAFE_AVAIL
init|=
name|UnsafeAvailChecker
operator|.
name|isAvailable
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|boolean
name|UNSAFE_UNALIGNED
init|=
name|UnsafeAvailChecker
operator|.
name|unaligned
argument_list|()
decl_stmt|;
comment|// Underlying BB
specifier|private
specifier|final
name|ByteBuffer
name|buf
decl_stmt|;
comment|// To access primitive values from underlying ByteBuffer using Unsafe
specifier|private
name|long
name|unsafeOffset
decl_stmt|;
specifier|private
name|Object
name|unsafeRef
init|=
literal|null
decl_stmt|;
specifier|public
name|SingleByteBuff
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|)
block|{
name|this
operator|.
name|buf
operator|=
name|buf
expr_stmt|;
if|if
condition|(
name|buf
operator|.
name|hasArray
argument_list|()
condition|)
block|{
name|this
operator|.
name|unsafeOffset
operator|=
name|UnsafeAccess
operator|.
name|BYTE_ARRAY_BASE_OFFSET
operator|+
name|buf
operator|.
name|arrayOffset
argument_list|()
expr_stmt|;
name|this
operator|.
name|unsafeRef
operator|=
name|buf
operator|.
name|array
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|unsafeOffset
operator|=
operator|(
operator|(
name|DirectBuffer
operator|)
name|buf
operator|)
operator|.
name|address
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|position
parameter_list|()
block|{
return|return
name|this
operator|.
name|buf
operator|.
name|position
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|SingleByteBuff
name|position
parameter_list|(
name|int
name|position
parameter_list|)
block|{
name|this
operator|.
name|buf
operator|.
name|position
argument_list|(
name|position
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|SingleByteBuff
name|skip
parameter_list|(
name|int
name|len
parameter_list|)
block|{
name|this
operator|.
name|buf
operator|.
name|position
argument_list|(
name|this
operator|.
name|buf
operator|.
name|position
argument_list|()
operator|+
name|len
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|SingleByteBuff
name|moveBack
parameter_list|(
name|int
name|len
parameter_list|)
block|{
name|this
operator|.
name|buf
operator|.
name|position
argument_list|(
name|this
operator|.
name|buf
operator|.
name|position
argument_list|()
operator|-
name|len
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|capacity
parameter_list|()
block|{
return|return
name|this
operator|.
name|buf
operator|.
name|capacity
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|limit
parameter_list|()
block|{
return|return
name|this
operator|.
name|buf
operator|.
name|limit
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|SingleByteBuff
name|limit
parameter_list|(
name|int
name|limit
parameter_list|)
block|{
name|this
operator|.
name|buf
operator|.
name|limit
argument_list|(
name|limit
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|SingleByteBuff
name|rewind
parameter_list|()
block|{
name|this
operator|.
name|buf
operator|.
name|rewind
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|SingleByteBuff
name|mark
parameter_list|()
block|{
name|this
operator|.
name|buf
operator|.
name|mark
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|asSubByteBuffer
parameter_list|(
name|int
name|length
parameter_list|)
block|{
comment|// Just return the single BB that is available
return|return
name|this
operator|.
name|buf
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|asSubByteBuffer
parameter_list|(
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|ObjectIntPair
argument_list|<
name|ByteBuffer
argument_list|>
name|pair
parameter_list|)
block|{
comment|// Just return the single BB that is available
name|pair
operator|.
name|setFirst
argument_list|(
name|this
operator|.
name|buf
argument_list|)
expr_stmt|;
name|pair
operator|.
name|setSecond
argument_list|(
name|offset
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|remaining
parameter_list|()
block|{
return|return
name|this
operator|.
name|buf
operator|.
name|remaining
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasRemaining
parameter_list|()
block|{
return|return
name|buf
operator|.
name|hasRemaining
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|SingleByteBuff
name|reset
parameter_list|()
block|{
name|this
operator|.
name|buf
operator|.
name|reset
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|SingleByteBuff
name|slice
parameter_list|()
block|{
return|return
operator|new
name|SingleByteBuff
argument_list|(
name|this
operator|.
name|buf
operator|.
name|slice
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SingleByteBuff
name|duplicate
parameter_list|()
block|{
return|return
operator|new
name|SingleByteBuff
argument_list|(
name|this
operator|.
name|buf
operator|.
name|duplicate
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|get
parameter_list|()
block|{
return|return
name|buf
operator|.
name|get
argument_list|()
return|;
block|}
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
if|if
condition|(
name|UNSAFE_AVAIL
condition|)
block|{
return|return
name|UnsafeAccess
operator|.
name|toByte
argument_list|(
name|this
operator|.
name|unsafeRef
argument_list|,
name|this
operator|.
name|unsafeOffset
operator|+
name|index
argument_list|)
return|;
block|}
return|return
name|this
operator|.
name|buf
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|getByteAfterPosition
parameter_list|(
name|int
name|offset
parameter_list|)
block|{
return|return
name|get
argument_list|(
name|this
operator|.
name|buf
operator|.
name|position
argument_list|()
operator|+
name|offset
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SingleByteBuff
name|put
parameter_list|(
name|byte
name|b
parameter_list|)
block|{
name|this
operator|.
name|buf
operator|.
name|put
argument_list|(
name|b
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|SingleByteBuff
name|put
parameter_list|(
name|int
name|index
parameter_list|,
name|byte
name|b
parameter_list|)
block|{
name|buf
operator|.
name|put
argument_list|(
name|index
argument_list|,
name|b
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|get
parameter_list|(
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
name|ByteBufferUtils
operator|.
name|copyFromBufferToArray
argument_list|(
name|dst
argument_list|,
name|buf
argument_list|,
name|buf
operator|.
name|position
argument_list|()
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|buf
operator|.
name|position
argument_list|(
name|buf
operator|.
name|position
argument_list|()
operator|+
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|get
parameter_list|(
name|int
name|sourceOffset
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
name|ByteBufferUtils
operator|.
name|copyFromBufferToArray
argument_list|(
name|dst
argument_list|,
name|buf
argument_list|,
name|sourceOffset
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|get
parameter_list|(
name|byte
index|[]
name|dst
parameter_list|)
block|{
name|get
argument_list|(
name|dst
argument_list|,
literal|0
argument_list|,
name|dst
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|SingleByteBuff
name|put
parameter_list|(
name|int
name|offset
parameter_list|,
name|ByteBuff
name|src
parameter_list|,
name|int
name|srcOffset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
if|if
condition|(
name|src
operator|instanceof
name|SingleByteBuff
condition|)
block|{
name|ByteBufferUtils
operator|.
name|copyFromBufferToBuffer
argument_list|(
operator|(
operator|(
name|SingleByteBuff
operator|)
name|src
operator|)
operator|.
name|buf
argument_list|,
name|this
operator|.
name|buf
argument_list|,
name|srcOffset
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// TODO we can do some optimization here? Call to asSubByteBuffer might
comment|// create a copy.
name|ObjectIntPair
argument_list|<
name|ByteBuffer
argument_list|>
name|pair
init|=
operator|new
name|ObjectIntPair
argument_list|<>
argument_list|()
decl_stmt|;
name|src
operator|.
name|asSubByteBuffer
argument_list|(
name|srcOffset
argument_list|,
name|length
argument_list|,
name|pair
argument_list|)
expr_stmt|;
if|if
condition|(
name|pair
operator|.
name|getFirst
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|ByteBufferUtils
operator|.
name|copyFromBufferToBuffer
argument_list|(
name|pair
operator|.
name|getFirst
argument_list|()
argument_list|,
name|this
operator|.
name|buf
argument_list|,
name|pair
operator|.
name|getSecond
argument_list|()
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|SingleByteBuff
name|put
parameter_list|(
name|byte
index|[]
name|src
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|ByteBufferUtils
operator|.
name|copyFromArrayToBuffer
argument_list|(
name|this
operator|.
name|buf
argument_list|,
name|src
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
name|SingleByteBuff
name|put
parameter_list|(
name|byte
index|[]
name|src
parameter_list|)
block|{
return|return
name|put
argument_list|(
name|src
argument_list|,
literal|0
argument_list|,
name|src
operator|.
name|length
argument_list|)
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
name|this
operator|.
name|buf
operator|.
name|hasArray
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|array
parameter_list|()
block|{
return|return
name|this
operator|.
name|buf
operator|.
name|array
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|arrayOffset
parameter_list|()
block|{
return|return
name|this
operator|.
name|buf
operator|.
name|arrayOffset
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|short
name|getShort
parameter_list|()
block|{
return|return
name|this
operator|.
name|buf
operator|.
name|getShort
argument_list|()
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
if|if
condition|(
name|UNSAFE_UNALIGNED
condition|)
block|{
return|return
name|UnsafeAccess
operator|.
name|toShort
argument_list|(
name|unsafeRef
argument_list|,
name|unsafeOffset
operator|+
name|index
argument_list|)
return|;
block|}
return|return
name|this
operator|.
name|buf
operator|.
name|getShort
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|short
name|getShortAfterPosition
parameter_list|(
name|int
name|offset
parameter_list|)
block|{
return|return
name|getShort
argument_list|(
name|this
operator|.
name|buf
operator|.
name|position
argument_list|()
operator|+
name|offset
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getInt
parameter_list|()
block|{
return|return
name|this
operator|.
name|buf
operator|.
name|getInt
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|SingleByteBuff
name|putInt
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|ByteBufferUtils
operator|.
name|putInt
argument_list|(
name|this
operator|.
name|buf
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
name|this
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
if|if
condition|(
name|UNSAFE_UNALIGNED
condition|)
block|{
return|return
name|UnsafeAccess
operator|.
name|toInt
argument_list|(
name|unsafeRef
argument_list|,
name|unsafeOffset
operator|+
name|index
argument_list|)
return|;
block|}
return|return
name|this
operator|.
name|buf
operator|.
name|getInt
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getIntAfterPosition
parameter_list|(
name|int
name|offset
parameter_list|)
block|{
return|return
name|getInt
argument_list|(
name|this
operator|.
name|buf
operator|.
name|position
argument_list|()
operator|+
name|offset
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLong
parameter_list|()
block|{
return|return
name|this
operator|.
name|buf
operator|.
name|getLong
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|SingleByteBuff
name|putLong
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|ByteBufferUtils
operator|.
name|putLong
argument_list|(
name|this
operator|.
name|buf
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
name|this
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
if|if
condition|(
name|UNSAFE_UNALIGNED
condition|)
block|{
return|return
name|UnsafeAccess
operator|.
name|toLong
argument_list|(
name|unsafeRef
argument_list|,
name|unsafeOffset
operator|+
name|index
argument_list|)
return|;
block|}
return|return
name|this
operator|.
name|buf
operator|.
name|getLong
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLongAfterPosition
parameter_list|(
name|int
name|offset
parameter_list|)
block|{
return|return
name|getLong
argument_list|(
name|this
operator|.
name|buf
operator|.
name|position
argument_list|()
operator|+
name|offset
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|toBytes
parameter_list|(
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|byte
index|[]
name|output
init|=
operator|new
name|byte
index|[
name|length
index|]
decl_stmt|;
name|ByteBufferUtils
operator|.
name|copyFromBufferToArray
argument_list|(
name|output
argument_list|,
name|buf
argument_list|,
name|offset
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
return|return
name|output
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|get
parameter_list|(
name|ByteBuffer
name|out
parameter_list|,
name|int
name|sourceOffset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|ByteBufferUtils
operator|.
name|copyFromBufferToBuffer
argument_list|(
name|buf
argument_list|,
name|out
argument_list|,
name|sourceOffset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|(
name|ReadableByteChannel
name|channel
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|channelRead
argument_list|(
name|channel
argument_list|,
name|buf
argument_list|)
return|;
block|}
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
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|SingleByteBuff
operator|)
condition|)
return|return
literal|false
return|;
return|return
name|this
operator|.
name|buf
operator|.
name|equals
argument_list|(
operator|(
operator|(
name|SingleByteBuff
operator|)
name|obj
operator|)
operator|.
name|buf
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
name|this
operator|.
name|buf
operator|.
name|hashCode
argument_list|()
return|;
block|}
comment|/**    * @return the ByteBuffer which this wraps.    */
annotation|@
name|VisibleForTesting
specifier|public
name|ByteBuffer
name|getEnclosingByteBuffer
parameter_list|()
block|{
return|return
name|this
operator|.
name|buf
return|;
block|}
block|}
end_class

end_unit

