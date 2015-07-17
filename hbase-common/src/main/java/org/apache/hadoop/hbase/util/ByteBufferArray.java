begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|concurrent
operator|.
name|locks
operator|.
name|Lock
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|locks
operator|.
name|ReentrantLock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|nio
operator|.
name|MultiByteBuff
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
name|SingleByteBuff
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * This class manages an array of ByteBuffers with a default size 4MB. These  * buffers are sequential and could be considered as a large buffer.It supports  * reading/writing data from this large buffer with a position and offset  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|ByteBufferArray
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ByteBufferArray
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|int
name|DEFAULT_BUFFER_SIZE
init|=
literal|4
operator|*
literal|1024
operator|*
literal|1024
decl_stmt|;
specifier|private
name|ByteBuffer
name|buffers
index|[]
decl_stmt|;
specifier|private
name|Lock
name|locks
index|[]
decl_stmt|;
specifier|private
name|int
name|bufferSize
decl_stmt|;
specifier|private
name|int
name|bufferCount
decl_stmt|;
comment|/**    * We allocate a number of byte buffers as the capacity. In order not to out    * of the array bounds for the last byte(see {@link ByteBufferArray#multiple}),    * we will allocate one additional buffer with capacity 0;    * @param capacity total size of the byte buffer array    * @param directByteBuffer true if we allocate direct buffer    */
specifier|public
name|ByteBufferArray
parameter_list|(
name|long
name|capacity
parameter_list|,
name|boolean
name|directByteBuffer
parameter_list|)
block|{
name|this
operator|.
name|bufferSize
operator|=
name|DEFAULT_BUFFER_SIZE
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|bufferSize
operator|>
operator|(
name|capacity
operator|/
literal|16
operator|)
condition|)
name|this
operator|.
name|bufferSize
operator|=
operator|(
name|int
operator|)
name|roundUp
argument_list|(
name|capacity
operator|/
literal|16
argument_list|,
literal|32768
argument_list|)
expr_stmt|;
name|this
operator|.
name|bufferCount
operator|=
call|(
name|int
call|)
argument_list|(
name|roundUp
argument_list|(
name|capacity
argument_list|,
name|bufferSize
argument_list|)
operator|/
name|bufferSize
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Allocating buffers total="
operator|+
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|capacity
argument_list|)
operator|+
literal|", sizePerBuffer="
operator|+
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|bufferSize
argument_list|)
operator|+
literal|", count="
operator|+
name|bufferCount
operator|+
literal|", direct="
operator|+
name|directByteBuffer
argument_list|)
expr_stmt|;
name|buffers
operator|=
operator|new
name|ByteBuffer
index|[
name|bufferCount
operator|+
literal|1
index|]
expr_stmt|;
name|locks
operator|=
operator|new
name|Lock
index|[
name|bufferCount
operator|+
literal|1
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
name|bufferCount
condition|;
name|i
operator|++
control|)
block|{
name|locks
index|[
name|i
index|]
operator|=
operator|new
name|ReentrantLock
argument_list|()
expr_stmt|;
if|if
condition|(
name|i
operator|<
name|bufferCount
condition|)
block|{
name|buffers
index|[
name|i
index|]
operator|=
name|directByteBuffer
condition|?
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|bufferSize
argument_list|)
else|:
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|bufferSize
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|buffers
index|[
name|i
index|]
operator|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|long
name|roundUp
parameter_list|(
name|long
name|n
parameter_list|,
name|long
name|to
parameter_list|)
block|{
return|return
operator|(
operator|(
name|n
operator|+
name|to
operator|-
literal|1
operator|)
operator|/
name|to
operator|)
operator|*
name|to
return|;
block|}
comment|/**    * Transfers bytes from this buffer array into the given destination array    * @param start start position in the ByteBufferArray    * @param len The maximum number of bytes to be written to the given array    * @param dstArray The array into which bytes are to be written    * @return number of bytes read    */
specifier|public
name|int
name|getMultiple
parameter_list|(
name|long
name|start
parameter_list|,
name|int
name|len
parameter_list|,
name|byte
index|[]
name|dstArray
parameter_list|)
block|{
return|return
name|getMultiple
argument_list|(
name|start
argument_list|,
name|len
argument_list|,
name|dstArray
argument_list|,
literal|0
argument_list|)
return|;
block|}
comment|/**    * Transfers bytes from this buffer array into the given destination array    * @param start start offset of this buffer array    * @param len The maximum number of bytes to be written to the given array    * @param dstArray The array into which bytes are to be written    * @param dstOffset The offset within the given array of the first byte to be    *          written    * @return number of bytes read    */
specifier|public
name|int
name|getMultiple
parameter_list|(
name|long
name|start
parameter_list|,
name|int
name|len
parameter_list|,
name|byte
index|[]
name|dstArray
parameter_list|,
name|int
name|dstOffset
parameter_list|)
block|{
name|multiple
argument_list|(
name|start
argument_list|,
name|len
argument_list|,
name|dstArray
argument_list|,
name|dstOffset
argument_list|,
name|GET_MULTIPLE_VISTOR
argument_list|)
expr_stmt|;
return|return
name|len
return|;
block|}
specifier|private
specifier|final
specifier|static
name|Visitor
name|GET_MULTIPLE_VISTOR
init|=
operator|new
name|Visitor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|visit
parameter_list|(
name|ByteBuffer
name|bb
parameter_list|,
name|byte
index|[]
name|array
parameter_list|,
name|int
name|arrayIdx
parameter_list|,
name|int
name|len
parameter_list|)
block|{
name|bb
operator|.
name|get
argument_list|(
name|array
argument_list|,
name|arrayIdx
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
comment|/**    * Transfers bytes from the given source array into this buffer array    * @param start start offset of this buffer array    * @param len The maximum number of bytes to be read from the given array    * @param srcArray The array from which bytes are to be read    */
specifier|public
name|void
name|putMultiple
parameter_list|(
name|long
name|start
parameter_list|,
name|int
name|len
parameter_list|,
name|byte
index|[]
name|srcArray
parameter_list|)
block|{
name|putMultiple
argument_list|(
name|start
argument_list|,
name|len
argument_list|,
name|srcArray
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    * Transfers bytes from the given source array into this buffer array    * @param start start offset of this buffer array    * @param len The maximum number of bytes to be read from the given array    * @param srcArray The array from which bytes are to be read    * @param srcOffset The offset within the given array of the first byte to be    *          read    */
specifier|public
name|void
name|putMultiple
parameter_list|(
name|long
name|start
parameter_list|,
name|int
name|len
parameter_list|,
name|byte
index|[]
name|srcArray
parameter_list|,
name|int
name|srcOffset
parameter_list|)
block|{
name|multiple
argument_list|(
name|start
argument_list|,
name|len
argument_list|,
name|srcArray
argument_list|,
name|srcOffset
argument_list|,
name|PUT_MULTIPLE_VISITOR
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|final
specifier|static
name|Visitor
name|PUT_MULTIPLE_VISITOR
init|=
operator|new
name|Visitor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|visit
parameter_list|(
name|ByteBuffer
name|bb
parameter_list|,
name|byte
index|[]
name|array
parameter_list|,
name|int
name|arrayIdx
parameter_list|,
name|int
name|len
parameter_list|)
block|{
name|bb
operator|.
name|put
argument_list|(
name|array
argument_list|,
name|arrayIdx
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
specifier|private
interface|interface
name|Visitor
block|{
comment|/**      * Visit the given byte buffer, if it is a read action, we will transfer the      * bytes from the buffer to the destination array, else if it is a write      * action, we will transfer the bytes from the source array to the buffer      * @param bb byte buffer      * @param array a source or destination byte array      * @param arrayOffset offset of the byte array      * @param len read/write length      */
name|void
name|visit
parameter_list|(
name|ByteBuffer
name|bb
parameter_list|,
name|byte
index|[]
name|array
parameter_list|,
name|int
name|arrayOffset
parameter_list|,
name|int
name|len
parameter_list|)
function_decl|;
block|}
comment|/**    * Access(read or write) this buffer array with a position and length as the    * given array. Here we will only lock one buffer even if it may be need visit    * several buffers. The consistency is guaranteed by the caller.    * @param start start offset of this buffer array    * @param len The maximum number of bytes to be accessed    * @param array The array from/to which bytes are to be read/written    * @param arrayOffset The offset within the given array of the first byte to    *          be read or written    * @param visitor implement of how to visit the byte buffer    */
name|void
name|multiple
parameter_list|(
name|long
name|start
parameter_list|,
name|int
name|len
parameter_list|,
name|byte
index|[]
name|array
parameter_list|,
name|int
name|arrayOffset
parameter_list|,
name|Visitor
name|visitor
parameter_list|)
block|{
assert|assert
name|len
operator|>=
literal|0
assert|;
name|long
name|end
init|=
name|start
operator|+
name|len
decl_stmt|;
name|int
name|startBuffer
init|=
call|(
name|int
call|)
argument_list|(
name|start
operator|/
name|bufferSize
argument_list|)
decl_stmt|,
name|startOffset
init|=
call|(
name|int
call|)
argument_list|(
name|start
operator|%
name|bufferSize
argument_list|)
decl_stmt|;
name|int
name|endBuffer
init|=
call|(
name|int
call|)
argument_list|(
name|end
operator|/
name|bufferSize
argument_list|)
decl_stmt|,
name|endOffset
init|=
call|(
name|int
call|)
argument_list|(
name|end
operator|%
name|bufferSize
argument_list|)
decl_stmt|;
assert|assert
name|array
operator|.
name|length
operator|>=
name|len
operator|+
name|arrayOffset
assert|;
assert|assert
name|startBuffer
operator|>=
literal|0
operator|&&
name|startBuffer
operator|<
name|bufferCount
assert|;
assert|assert
name|endBuffer
operator|>=
literal|0
operator|&&
name|endBuffer
operator|<
name|bufferCount
operator|||
operator|(
name|endBuffer
operator|==
name|bufferCount
operator|&&
name|endOffset
operator|==
literal|0
operator|)
assert|;
if|if
condition|(
name|startBuffer
operator|>=
name|locks
operator|.
name|length
operator|||
name|startBuffer
operator|<
literal|0
condition|)
block|{
name|String
name|msg
init|=
literal|"Failed multiple, start="
operator|+
name|start
operator|+
literal|",startBuffer="
operator|+
name|startBuffer
operator|+
literal|",bufferSize="
operator|+
name|bufferSize
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
name|int
name|srcIndex
init|=
literal|0
decl_stmt|,
name|cnt
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|startBuffer
init|;
name|i
operator|<=
name|endBuffer
condition|;
operator|++
name|i
control|)
block|{
name|Lock
name|lock
init|=
name|locks
index|[
name|i
index|]
decl_stmt|;
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|ByteBuffer
name|bb
init|=
name|buffers
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|i
operator|==
name|startBuffer
condition|)
block|{
name|cnt
operator|=
name|bufferSize
operator|-
name|startOffset
expr_stmt|;
if|if
condition|(
name|cnt
operator|>
name|len
condition|)
name|cnt
operator|=
name|len
expr_stmt|;
name|bb
operator|.
name|limit
argument_list|(
name|startOffset
operator|+
name|cnt
argument_list|)
operator|.
name|position
argument_list|(
name|startOffset
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|i
operator|==
name|endBuffer
condition|)
block|{
name|cnt
operator|=
name|endOffset
expr_stmt|;
name|bb
operator|.
name|limit
argument_list|(
name|cnt
argument_list|)
operator|.
name|position
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|cnt
operator|=
name|bufferSize
expr_stmt|;
name|bb
operator|.
name|limit
argument_list|(
name|cnt
argument_list|)
operator|.
name|position
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|visitor
operator|.
name|visit
argument_list|(
name|bb
argument_list|,
name|array
argument_list|,
name|srcIndex
operator|+
name|arrayOffset
argument_list|,
name|cnt
argument_list|)
expr_stmt|;
name|srcIndex
operator|+=
name|cnt
expr_stmt|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
assert|assert
name|srcIndex
operator|==
name|len
assert|;
block|}
comment|/**    * Creates a ByteBuff from a given array of ByteBuffers from the given offset to the    * length specified. For eg, if there are 4 buffers forming an array each with length 10 and    * if we call asSubBuffer(5, 10) then we will create an MBB consisting of two BBs    * and the first one be a BB from 'position' 5 to a 'length' 5 and the 2nd BB will be from    * 'position' 0 to 'length' 5.    * @param offset    * @param len    * @return a ByteBuff formed from the underlying ByteBuffers    */
specifier|public
name|ByteBuff
name|asSubByteBuff
parameter_list|(
name|long
name|offset
parameter_list|,
name|int
name|len
parameter_list|)
block|{
assert|assert
name|len
operator|>=
literal|0
assert|;
name|long
name|end
init|=
name|offset
operator|+
name|len
decl_stmt|;
name|int
name|startBuffer
init|=
call|(
name|int
call|)
argument_list|(
name|offset
operator|/
name|bufferSize
argument_list|)
decl_stmt|,
name|startBufferOffset
init|=
call|(
name|int
call|)
argument_list|(
name|offset
operator|%
name|bufferSize
argument_list|)
decl_stmt|;
name|int
name|endBuffer
init|=
call|(
name|int
call|)
argument_list|(
name|end
operator|/
name|bufferSize
argument_list|)
decl_stmt|,
name|endBufferOffset
init|=
call|(
name|int
call|)
argument_list|(
name|end
operator|%
name|bufferSize
argument_list|)
decl_stmt|;
assert|assert
name|startBuffer
operator|>=
literal|0
operator|&&
name|startBuffer
operator|<
name|bufferCount
assert|;
assert|assert
name|endBuffer
operator|>=
literal|0
operator|&&
name|endBuffer
operator|<
name|bufferCount
operator|||
operator|(
name|endBuffer
operator|==
name|bufferCount
operator|&&
name|endBufferOffset
operator|==
literal|0
operator|)
assert|;
if|if
condition|(
name|startBuffer
operator|>=
name|locks
operator|.
name|length
operator|||
name|startBuffer
operator|<
literal|0
condition|)
block|{
name|String
name|msg
init|=
literal|"Failed subArray, start="
operator|+
name|offset
operator|+
literal|",startBuffer="
operator|+
name|startBuffer
operator|+
literal|",bufferSize="
operator|+
name|bufferSize
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
name|int
name|srcIndex
init|=
literal|0
decl_stmt|,
name|cnt
init|=
operator|-
literal|1
decl_stmt|;
name|ByteBuffer
index|[]
name|mbb
init|=
operator|new
name|ByteBuffer
index|[
name|endBuffer
operator|-
name|startBuffer
operator|+
literal|1
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|startBuffer
init|,
name|j
init|=
literal|0
init|;
name|i
operator|<=
name|endBuffer
condition|;
operator|++
name|i
operator|,
name|j
operator|++
control|)
block|{
name|Lock
name|lock
init|=
name|locks
index|[
name|i
index|]
decl_stmt|;
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|ByteBuffer
name|bb
init|=
name|buffers
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|i
operator|==
name|startBuffer
condition|)
block|{
name|cnt
operator|=
name|bufferSize
operator|-
name|startBufferOffset
expr_stmt|;
if|if
condition|(
name|cnt
operator|>
name|len
condition|)
name|cnt
operator|=
name|len
expr_stmt|;
name|ByteBuffer
name|dup
init|=
name|bb
operator|.
name|duplicate
argument_list|()
decl_stmt|;
name|dup
operator|.
name|limit
argument_list|(
name|startBufferOffset
operator|+
name|cnt
argument_list|)
operator|.
name|position
argument_list|(
name|startBufferOffset
argument_list|)
expr_stmt|;
name|mbb
index|[
name|j
index|]
operator|=
name|dup
operator|.
name|slice
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|i
operator|==
name|endBuffer
condition|)
block|{
name|cnt
operator|=
name|endBufferOffset
expr_stmt|;
name|ByteBuffer
name|dup
init|=
name|bb
operator|.
name|duplicate
argument_list|()
decl_stmt|;
name|dup
operator|.
name|position
argument_list|(
literal|0
argument_list|)
operator|.
name|limit
argument_list|(
name|cnt
argument_list|)
expr_stmt|;
name|mbb
index|[
name|j
index|]
operator|=
name|dup
operator|.
name|slice
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|cnt
operator|=
name|bufferSize
expr_stmt|;
name|ByteBuffer
name|dup
init|=
name|bb
operator|.
name|duplicate
argument_list|()
decl_stmt|;
name|dup
operator|.
name|position
argument_list|(
literal|0
argument_list|)
operator|.
name|limit
argument_list|(
name|cnt
argument_list|)
expr_stmt|;
name|mbb
index|[
name|j
index|]
operator|=
name|dup
operator|.
name|slice
argument_list|()
expr_stmt|;
block|}
name|srcIndex
operator|+=
name|cnt
expr_stmt|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
assert|assert
name|srcIndex
operator|==
name|len
assert|;
if|if
condition|(
name|mbb
operator|.
name|length
operator|>
literal|1
condition|)
block|{
return|return
operator|new
name|MultiByteBuff
argument_list|(
name|mbb
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|SingleByteBuff
argument_list|(
name|mbb
index|[
literal|0
index|]
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

