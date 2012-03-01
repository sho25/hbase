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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
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
name|io
operator|.
name|InputStream
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
name|classification
operator|.
name|InterfaceStability
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
name|EncoderBufferTooSmallException
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
name|io
operator|.
name|WritableUtils
import|;
end_import

begin_comment
comment|/**  * Utility functions for working with byte buffers, such as reading/writing  * variable-length long numbers.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|final
class|class
name|ByteBufferUtils
block|{
comment|// "Compressed integer" serialization helper constants.
specifier|private
specifier|final
specifier|static
name|int
name|VALUE_MASK
init|=
literal|0x7f
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|int
name|NEXT_BIT_SHIFT
init|=
literal|7
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|int
name|NEXT_BIT_MASK
init|=
literal|1
operator|<<
literal|7
decl_stmt|;
specifier|private
name|ByteBufferUtils
parameter_list|()
block|{   }
comment|/**    * Similar to {@link WritableUtils#writeVLong(java.io.DataOutput, long)},    * but writes to a {@link ByteBuffer}.    */
specifier|public
specifier|static
name|void
name|writeVLong
parameter_list|(
name|ByteBuffer
name|out
parameter_list|,
name|long
name|i
parameter_list|)
block|{
if|if
condition|(
name|i
operator|>=
operator|-
literal|112
operator|&&
name|i
operator|<=
literal|127
condition|)
block|{
name|out
operator|.
name|put
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|)
expr_stmt|;
return|return;
block|}
name|int
name|len
init|=
operator|-
literal|112
decl_stmt|;
if|if
condition|(
name|i
operator|<
literal|0
condition|)
block|{
name|i
operator|^=
operator|-
literal|1L
expr_stmt|;
comment|// take one's complement
name|len
operator|=
operator|-
literal|120
expr_stmt|;
block|}
name|long
name|tmp
init|=
name|i
decl_stmt|;
while|while
condition|(
name|tmp
operator|!=
literal|0
condition|)
block|{
name|tmp
operator|=
name|tmp
operator|>>
literal|8
expr_stmt|;
name|len
operator|--
expr_stmt|;
block|}
name|out
operator|.
name|put
argument_list|(
operator|(
name|byte
operator|)
name|len
argument_list|)
expr_stmt|;
name|len
operator|=
operator|(
name|len
operator|<
operator|-
literal|120
operator|)
condition|?
operator|-
operator|(
name|len
operator|+
literal|120
operator|)
else|:
operator|-
operator|(
name|len
operator|+
literal|112
operator|)
expr_stmt|;
for|for
control|(
name|int
name|idx
init|=
name|len
init|;
name|idx
operator|!=
literal|0
condition|;
name|idx
operator|--
control|)
block|{
name|int
name|shiftbits
init|=
operator|(
name|idx
operator|-
literal|1
operator|)
operator|*
literal|8
decl_stmt|;
name|long
name|mask
init|=
literal|0xFFL
operator|<<
name|shiftbits
decl_stmt|;
name|out
operator|.
name|put
argument_list|(
call|(
name|byte
call|)
argument_list|(
operator|(
name|i
operator|&
name|mask
operator|)
operator|>>
name|shiftbits
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Similar to {@link WritableUtils#readVLong(DataInput)} but reads from a    * {@link ByteBuffer}.    */
specifier|public
specifier|static
name|long
name|readVLong
parameter_list|(
name|ByteBuffer
name|in
parameter_list|)
block|{
name|byte
name|firstByte
init|=
name|in
operator|.
name|get
argument_list|()
decl_stmt|;
name|int
name|len
init|=
name|WritableUtils
operator|.
name|decodeVIntSize
argument_list|(
name|firstByte
argument_list|)
decl_stmt|;
if|if
condition|(
name|len
operator|==
literal|1
condition|)
block|{
return|return
name|firstByte
return|;
block|}
name|long
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|idx
init|=
literal|0
init|;
name|idx
operator|<
name|len
operator|-
literal|1
condition|;
name|idx
operator|++
control|)
block|{
name|byte
name|b
init|=
name|in
operator|.
name|get
argument_list|()
decl_stmt|;
name|i
operator|=
name|i
operator|<<
literal|8
expr_stmt|;
name|i
operator|=
name|i
operator||
operator|(
name|b
operator|&
literal|0xFF
operator|)
expr_stmt|;
block|}
return|return
operator|(
name|WritableUtils
operator|.
name|isNegativeVInt
argument_list|(
name|firstByte
argument_list|)
condition|?
operator|(
name|i
operator|^
operator|-
literal|1L
operator|)
else|:
name|i
operator|)
return|;
block|}
comment|/**    * Put in buffer integer using 7 bit encoding. For each written byte:    * 7 bits are used to store value    * 1 bit is used to indicate whether there is next bit.    * @param value Int to be compressed.    * @param out Where to put compressed data    * @return Number of bytes written.    * @throws IOException on stream error    */
specifier|public
specifier|static
name|int
name|putCompressedInt
parameter_list|(
name|OutputStream
name|out
parameter_list|,
specifier|final
name|int
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|i
init|=
literal|0
decl_stmt|;
name|int
name|tmpvalue
init|=
name|value
decl_stmt|;
do|do
block|{
name|byte
name|b
init|=
call|(
name|byte
call|)
argument_list|(
name|tmpvalue
operator|&
name|VALUE_MASK
argument_list|)
decl_stmt|;
name|tmpvalue
operator|>>>=
name|NEXT_BIT_SHIFT
expr_stmt|;
if|if
condition|(
name|tmpvalue
operator|!=
literal|0
condition|)
block|{
name|b
operator||=
operator|(
name|byte
operator|)
name|NEXT_BIT_MASK
expr_stmt|;
block|}
name|out
operator|.
name|write
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
do|while
condition|(
name|tmpvalue
operator|!=
literal|0
condition|)
do|;
return|return
name|i
return|;
block|}
comment|/**     * Put in output stream 32 bit integer (Big Endian byte order).     * @param out Where to put integer.     * @param value Value of integer.     * @throws IOException On stream error.     */
specifier|public
specifier|static
name|void
name|putInt
parameter_list|(
name|OutputStream
name|out
parameter_list|,
specifier|final
name|int
name|value
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
name|Bytes
operator|.
name|SIZEOF_INT
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
operator|--
name|i
control|)
block|{
name|out
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|value
operator|>>>
operator|(
name|i
operator|*
literal|8
operator|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Copy the data to the output stream and update position in buffer.    * @param out the stream to write bytes to    * @param in the buffer to read bytes from    * @param length the number of bytes to copy    */
specifier|public
specifier|static
name|void
name|moveBufferToStream
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|ByteBuffer
name|in
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
name|copyBufferToStream
argument_list|(
name|out
argument_list|,
name|in
argument_list|,
name|in
operator|.
name|position
argument_list|()
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|skip
argument_list|(
name|in
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
comment|/**    * Copy data from a buffer to an output stream. Does not update the position    * in the buffer.    * @param out the stream to write bytes to    * @param in the buffer to read bytes from    * @param offset the offset in the buffer (from the buffer's array offset)    *      to start copying bytes from    * @param length the number of bytes to copy    */
specifier|public
specifier|static
name|void
name|copyBufferToStream
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|ByteBuffer
name|in
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|in
operator|.
name|hasArray
argument_list|()
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|in
operator|.
name|array
argument_list|()
argument_list|,
name|in
operator|.
name|arrayOffset
argument_list|()
operator|+
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
operator|++
name|i
control|)
block|{
name|out
operator|.
name|write
argument_list|(
name|in
operator|.
name|get
argument_list|(
name|offset
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|int
name|putLong
parameter_list|(
name|OutputStream
name|out
parameter_list|,
specifier|final
name|long
name|value
parameter_list|,
specifier|final
name|int
name|fitInBytes
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|tmpValue
init|=
name|value
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
name|fitInBytes
condition|;
operator|++
name|i
control|)
block|{
name|out
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|tmpValue
operator|&
literal|0xff
argument_list|)
argument_list|)
expr_stmt|;
name|tmpValue
operator|>>>=
literal|8
expr_stmt|;
block|}
return|return
name|fitInBytes
return|;
block|}
comment|/**    * Check how many bytes are required to store value.    * @param value Value which size will be tested.    * @return How many bytes are required to store value.    */
specifier|public
specifier|static
name|int
name|longFitsIn
parameter_list|(
specifier|final
name|long
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|<
literal|0
condition|)
block|{
return|return
literal|8
return|;
block|}
if|if
condition|(
name|value
operator|<
operator|(
literal|1l
operator|<<
literal|4
operator|*
literal|8
operator|)
condition|)
block|{
comment|// no more than 4 bytes
if|if
condition|(
name|value
operator|<
operator|(
literal|1l
operator|<<
literal|2
operator|*
literal|8
operator|)
condition|)
block|{
if|if
condition|(
name|value
operator|<
operator|(
literal|1l
operator|<<
literal|1
operator|*
literal|8
operator|)
condition|)
block|{
return|return
literal|1
return|;
block|}
return|return
literal|2
return|;
block|}
if|if
condition|(
name|value
operator|<
operator|(
literal|1l
operator|<<
literal|3
operator|*
literal|8
operator|)
condition|)
block|{
return|return
literal|3
return|;
block|}
return|return
literal|4
return|;
block|}
comment|// more than 4 bytes
if|if
condition|(
name|value
operator|<
operator|(
literal|1l
operator|<<
literal|6
operator|*
literal|8
operator|)
condition|)
block|{
if|if
condition|(
name|value
operator|<
operator|(
literal|1l
operator|<<
literal|5
operator|*
literal|8
operator|)
condition|)
block|{
return|return
literal|5
return|;
block|}
return|return
literal|6
return|;
block|}
if|if
condition|(
name|value
operator|<
operator|(
literal|1l
operator|<<
literal|7
operator|*
literal|8
operator|)
condition|)
block|{
return|return
literal|7
return|;
block|}
return|return
literal|8
return|;
block|}
comment|/**    * Check how many bytes is required to store value.    * @param value Value which size will be tested.    * @return How many bytes are required to store value.    */
specifier|public
specifier|static
name|int
name|intFitsIn
parameter_list|(
specifier|final
name|int
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|<
literal|0
condition|)
block|{
return|return
literal|4
return|;
block|}
if|if
condition|(
name|value
operator|<
operator|(
literal|1
operator|<<
literal|2
operator|*
literal|8
operator|)
condition|)
block|{
if|if
condition|(
name|value
operator|<
operator|(
literal|1
operator|<<
literal|1
operator|*
literal|8
operator|)
condition|)
block|{
return|return
literal|1
return|;
block|}
return|return
literal|2
return|;
block|}
if|if
condition|(
name|value
operator|<=
operator|(
literal|1
operator|<<
literal|3
operator|*
literal|8
operator|)
condition|)
block|{
return|return
literal|3
return|;
block|}
return|return
literal|4
return|;
block|}
comment|/**    * Read integer from stream coded in 7 bits and increment position.    * @return the integer that has been read    * @throws IOException    */
specifier|public
specifier|static
name|int
name|readCompressedInt
parameter_list|(
name|InputStream
name|input
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|result
init|=
literal|0
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
name|byte
name|b
decl_stmt|;
do|do
block|{
name|b
operator|=
operator|(
name|byte
operator|)
name|input
operator|.
name|read
argument_list|()
expr_stmt|;
name|result
operator|+=
operator|(
name|b
operator|&
name|VALUE_MASK
operator|)
operator|<<
operator|(
name|NEXT_BIT_SHIFT
operator|*
name|i
operator|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
if|if
condition|(
name|i
operator|>
name|Bytes
operator|.
name|SIZEOF_INT
operator|+
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Corrupted compressed int (too long: "
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
operator|+
literal|" bytes)"
argument_list|)
throw|;
block|}
block|}
do|while
condition|(
literal|0
operator|!=
operator|(
name|b
operator|&
name|NEXT_BIT_MASK
operator|)
condition|)
do|;
return|return
name|result
return|;
block|}
comment|/**    * Read integer from buffer coded in 7 bits and increment position.    * @return Read integer.    */
specifier|public
specifier|static
name|int
name|readCompressedInt
parameter_list|(
name|ByteBuffer
name|buffer
parameter_list|)
block|{
name|byte
name|b
init|=
name|buffer
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|b
operator|&
name|NEXT_BIT_MASK
operator|)
operator|!=
literal|0
condition|)
block|{
return|return
operator|(
name|b
operator|&
name|VALUE_MASK
operator|)
operator|+
operator|(
name|readCompressedInt
argument_list|(
name|buffer
argument_list|)
operator|<<
name|NEXT_BIT_SHIFT
operator|)
return|;
block|}
return|return
name|b
operator|&
name|VALUE_MASK
return|;
block|}
comment|/**    * Read long which was written to fitInBytes bytes and increment position.    * @param fitInBytes In how many bytes given long is stored.    * @return The value of parsed long.    * @throws IOException    */
specifier|public
specifier|static
name|long
name|readLong
parameter_list|(
name|InputStream
name|in
parameter_list|,
specifier|final
name|int
name|fitInBytes
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|tmpLong
init|=
literal|0
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
name|fitInBytes
condition|;
operator|++
name|i
control|)
block|{
name|tmpLong
operator||=
operator|(
name|in
operator|.
name|read
argument_list|()
operator|&
literal|0xffl
operator|)
operator|<<
operator|(
literal|8
operator|*
name|i
operator|)
expr_stmt|;
block|}
return|return
name|tmpLong
return|;
block|}
comment|/**    * Read long which was written to fitInBytes bytes and increment position.    * @param fitInBytes In how many bytes given long is stored.    * @return The value of parsed long.    */
specifier|public
specifier|static
name|long
name|readLong
parameter_list|(
name|ByteBuffer
name|in
parameter_list|,
specifier|final
name|int
name|fitInBytes
parameter_list|)
block|{
name|long
name|tmpLength
init|=
literal|0
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
name|fitInBytes
condition|;
operator|++
name|i
control|)
block|{
name|tmpLength
operator||=
operator|(
name|in
operator|.
name|get
argument_list|()
operator|&
literal|0xffl
operator|)
operator|<<
operator|(
literal|8l
operator|*
name|i
operator|)
expr_stmt|;
block|}
return|return
name|tmpLength
return|;
block|}
comment|/**    * Asserts that there is at least the given amount of unfilled space    * remaining in the given buffer.    * @param out typically, the buffer we are writing to    * @param length the required space in the buffer    * @throws EncoderBufferTooSmallException If there are no enough bytes.    */
specifier|public
specifier|static
name|void
name|ensureSpace
parameter_list|(
name|ByteBuffer
name|out
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|EncoderBufferTooSmallException
block|{
if|if
condition|(
name|out
operator|.
name|position
argument_list|()
operator|+
name|length
operator|>
name|out
operator|.
name|limit
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|EncoderBufferTooSmallException
argument_list|(
literal|"Buffer position="
operator|+
name|out
operator|.
name|position
argument_list|()
operator|+
literal|", buffer limit="
operator|+
name|out
operator|.
name|limit
argument_list|()
operator|+
literal|", length to be written="
operator|+
name|length
argument_list|)
throw|;
block|}
block|}
comment|/**    * Copy the given number of bytes from the given stream and put it at the    * current position of the given buffer, updating the position in the buffer.    * @param out the buffer to write data to    * @param in the stream to read data from    * @param length the number of bytes to read/write    */
specifier|public
specifier|static
name|void
name|copyFromStreamToBuffer
parameter_list|(
name|ByteBuffer
name|out
parameter_list|,
name|DataInputStream
name|in
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|out
operator|.
name|hasArray
argument_list|()
condition|)
block|{
name|in
operator|.
name|readFully
argument_list|(
name|out
operator|.
name|array
argument_list|()
argument_list|,
name|out
operator|.
name|position
argument_list|()
operator|+
name|out
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|skip
argument_list|(
name|out
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
operator|++
name|i
control|)
block|{
name|out
operator|.
name|put
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Copy from one buffer to another from given offset    * @param out destination buffer    * @param in source buffer    * @param sourceOffset offset in the source buffer    * @param length how many bytes to copy    */
specifier|public
specifier|static
name|void
name|copyFromBufferToBuffer
parameter_list|(
name|ByteBuffer
name|out
parameter_list|,
name|ByteBuffer
name|in
parameter_list|,
name|int
name|sourceOffset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
if|if
condition|(
name|in
operator|.
name|hasArray
argument_list|()
operator|&&
name|out
operator|.
name|hasArray
argument_list|()
condition|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|in
operator|.
name|array
argument_list|()
argument_list|,
name|sourceOffset
operator|+
name|in
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|out
operator|.
name|array
argument_list|()
argument_list|,
name|out
operator|.
name|position
argument_list|()
operator|+
name|out
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|skip
argument_list|(
name|out
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
operator|++
name|i
control|)
block|{
name|out
operator|.
name|put
argument_list|(
name|in
operator|.
name|get
argument_list|(
name|sourceOffset
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Find length of common prefix of two parts in the buffer    * @param buffer Where parts are located.    * @param offsetLeft Offset of the first part.    * @param offsetRight Offset of the second part.    * @param limit Maximal length of common prefix.    * @return Length of prefix.    */
specifier|public
specifier|static
name|int
name|findCommonPrefix
parameter_list|(
name|ByteBuffer
name|buffer
parameter_list|,
name|int
name|offsetLeft
parameter_list|,
name|int
name|offsetRight
parameter_list|,
name|int
name|limit
parameter_list|)
block|{
name|int
name|prefix
init|=
literal|0
decl_stmt|;
for|for
control|(
init|;
name|prefix
operator|<
name|limit
condition|;
operator|++
name|prefix
control|)
block|{
if|if
condition|(
name|buffer
operator|.
name|get
argument_list|(
name|offsetLeft
operator|+
name|prefix
argument_list|)
operator|!=
name|buffer
operator|.
name|get
argument_list|(
name|offsetRight
operator|+
name|prefix
argument_list|)
condition|)
block|{
break|break;
block|}
block|}
return|return
name|prefix
return|;
block|}
comment|/**    * Find length of common prefix in two arrays.    * @param left Array to be compared.    * @param leftOffset Offset in left array.    * @param leftLength Length of left array.    * @param right Array to be compared.    * @param rightArray Offset in right array.    * @param rightLength Length of right array.    */
specifier|public
specifier|static
name|int
name|findCommonPrefix
parameter_list|(
name|byte
index|[]
name|left
parameter_list|,
name|int
name|leftOffset
parameter_list|,
name|int
name|leftLength
parameter_list|,
name|byte
index|[]
name|right
parameter_list|,
name|int
name|rightOffset
parameter_list|,
name|int
name|rightLength
parameter_list|)
block|{
name|int
name|length
init|=
name|Math
operator|.
name|min
argument_list|(
name|leftLength
argument_list|,
name|rightLength
argument_list|)
decl_stmt|;
name|int
name|result
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|result
operator|<
name|length
operator|&&
name|left
index|[
name|leftOffset
operator|+
name|result
index|]
operator|==
name|right
index|[
name|rightOffset
operator|+
name|result
index|]
condition|)
block|{
name|result
operator|++
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * Check whether two parts in the same buffer are equal.    * @param buffer In which buffer there are parts    * @param offsetLeft Beginning of first part.    * @param lengthLeft Length of the first part.    * @param offsetRight Beginning of the second part.    * @param lengthRight Length of the second part.    * @return    */
specifier|public
specifier|static
name|boolean
name|arePartsEqual
parameter_list|(
name|ByteBuffer
name|buffer
parameter_list|,
name|int
name|offsetLeft
parameter_list|,
name|int
name|lengthLeft
parameter_list|,
name|int
name|offsetRight
parameter_list|,
name|int
name|lengthRight
parameter_list|)
block|{
if|if
condition|(
name|lengthLeft
operator|!=
name|lengthRight
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|buffer
operator|.
name|hasArray
argument_list|()
condition|)
block|{
return|return
literal|0
operator|==
name|Bytes
operator|.
name|compareTo
argument_list|(
name|buffer
operator|.
name|array
argument_list|()
argument_list|,
name|buffer
operator|.
name|arrayOffset
argument_list|()
operator|+
name|offsetLeft
argument_list|,
name|lengthLeft
argument_list|,
name|buffer
operator|.
name|array
argument_list|()
argument_list|,
name|buffer
operator|.
name|arrayOffset
argument_list|()
operator|+
name|offsetRight
argument_list|,
name|lengthRight
argument_list|)
return|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|lengthRight
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|buffer
operator|.
name|get
argument_list|(
name|offsetLeft
operator|+
name|i
argument_list|)
operator|!=
name|buffer
operator|.
name|get
argument_list|(
name|offsetRight
operator|+
name|i
argument_list|)
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
comment|/**    * Increment position in buffer.    * @param buffer In this buffer.    * @param length By that many bytes.    */
specifier|public
specifier|static
name|void
name|skip
parameter_list|(
name|ByteBuffer
name|buffer
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|buffer
operator|.
name|position
argument_list|(
name|buffer
operator|.
name|position
argument_list|()
operator|+
name|length
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

