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
name|io
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
name|Pair
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/*  * It seems like as soon as somebody sets himself to the task of creating VInt encoding, his mind  * blanks out for a split-second and he starts the work by wrapping it in the most convoluted  * interface he can come up with. Custom streams that allocate memory, DataOutput that is only used  * to write single bytes... We operate on simple streams. Thus, we are going to have a simple  * implementation copy-pasted from protobuf Coded*Stream.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|StreamUtils
block|{
specifier|public
specifier|static
name|void
name|writeRawVInt32
parameter_list|(
name|OutputStream
name|output
parameter_list|,
name|int
name|value
parameter_list|)
throws|throws
name|IOException
block|{
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
operator|(
name|value
operator|&
operator|~
literal|0x7F
operator|)
operator|==
literal|0
condition|)
block|{
name|output
operator|.
name|write
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return;
block|}
else|else
block|{
name|output
operator|.
name|write
argument_list|(
operator|(
name|value
operator|&
literal|0x7F
operator|)
operator||
literal|0x80
argument_list|)
expr_stmt|;
name|value
operator|>>>=
literal|7
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|int
name|readRawVarint32
parameter_list|(
name|InputStream
name|input
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
name|tmp
init|=
operator|(
name|byte
operator|)
name|input
operator|.
name|read
argument_list|()
decl_stmt|;
if|if
condition|(
name|tmp
operator|>=
literal|0
condition|)
block|{
return|return
name|tmp
return|;
block|}
name|int
name|result
init|=
name|tmp
operator|&
literal|0x7f
decl_stmt|;
if|if
condition|(
operator|(
name|tmp
operator|=
operator|(
name|byte
operator|)
name|input
operator|.
name|read
argument_list|()
operator|)
operator|>=
literal|0
condition|)
block|{
name|result
operator||=
name|tmp
operator|<<
literal|7
expr_stmt|;
block|}
else|else
block|{
name|result
operator||=
operator|(
name|tmp
operator|&
literal|0x7f
operator|)
operator|<<
literal|7
expr_stmt|;
if|if
condition|(
operator|(
name|tmp
operator|=
operator|(
name|byte
operator|)
name|input
operator|.
name|read
argument_list|()
operator|)
operator|>=
literal|0
condition|)
block|{
name|result
operator||=
name|tmp
operator|<<
literal|14
expr_stmt|;
block|}
else|else
block|{
name|result
operator||=
operator|(
name|tmp
operator|&
literal|0x7f
operator|)
operator|<<
literal|14
expr_stmt|;
if|if
condition|(
operator|(
name|tmp
operator|=
operator|(
name|byte
operator|)
name|input
operator|.
name|read
argument_list|()
operator|)
operator|>=
literal|0
condition|)
block|{
name|result
operator||=
name|tmp
operator|<<
literal|21
expr_stmt|;
block|}
else|else
block|{
name|result
operator||=
operator|(
name|tmp
operator|&
literal|0x7f
operator|)
operator|<<
literal|21
expr_stmt|;
name|result
operator||=
operator|(
name|tmp
operator|=
operator|(
name|byte
operator|)
name|input
operator|.
name|read
argument_list|()
operator|)
operator|<<
literal|28
expr_stmt|;
if|if
condition|(
name|tmp
operator|<
literal|0
condition|)
block|{
comment|// Discard upper 32 bits.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|5
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|input
operator|.
name|read
argument_list|()
operator|>=
literal|0
condition|)
block|{
return|return
name|result
return|;
block|}
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Malformed varint"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
return|return
name|result
return|;
block|}
specifier|public
specifier|static
name|int
name|readRawVarint32
parameter_list|(
name|ByteBuffer
name|input
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
name|tmp
init|=
name|input
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|tmp
operator|>=
literal|0
condition|)
block|{
return|return
name|tmp
return|;
block|}
name|int
name|result
init|=
name|tmp
operator|&
literal|0x7f
decl_stmt|;
if|if
condition|(
operator|(
name|tmp
operator|=
name|input
operator|.
name|get
argument_list|()
operator|)
operator|>=
literal|0
condition|)
block|{
name|result
operator||=
name|tmp
operator|<<
literal|7
expr_stmt|;
block|}
else|else
block|{
name|result
operator||=
operator|(
name|tmp
operator|&
literal|0x7f
operator|)
operator|<<
literal|7
expr_stmt|;
if|if
condition|(
operator|(
name|tmp
operator|=
name|input
operator|.
name|get
argument_list|()
operator|)
operator|>=
literal|0
condition|)
block|{
name|result
operator||=
name|tmp
operator|<<
literal|14
expr_stmt|;
block|}
else|else
block|{
name|result
operator||=
operator|(
name|tmp
operator|&
literal|0x7f
operator|)
operator|<<
literal|14
expr_stmt|;
if|if
condition|(
operator|(
name|tmp
operator|=
name|input
operator|.
name|get
argument_list|()
operator|)
operator|>=
literal|0
condition|)
block|{
name|result
operator||=
name|tmp
operator|<<
literal|21
expr_stmt|;
block|}
else|else
block|{
name|result
operator||=
operator|(
name|tmp
operator|&
literal|0x7f
operator|)
operator|<<
literal|21
expr_stmt|;
name|result
operator||=
operator|(
name|tmp
operator|=
name|input
operator|.
name|get
argument_list|()
operator|)
operator|<<
literal|28
expr_stmt|;
if|if
condition|(
name|tmp
operator|<
literal|0
condition|)
block|{
comment|// Discard upper 32 bits.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|5
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|input
operator|.
name|get
argument_list|()
operator|>=
literal|0
condition|)
block|{
return|return
name|result
return|;
block|}
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Malformed varint"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
return|return
name|result
return|;
block|}
comment|/**    * Reads a varInt value stored in an array.    *    * @param input    *          Input array where the varInt is available    * @param offset    *          Offset in the input array where varInt is available    * @return A pair of integers in which first value is the actual decoded varInt value and second    *         value as number of bytes taken by this varInt for it's storage in the input array.    * @throws IOException    */
specifier|public
specifier|static
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|readRawVarint32
parameter_list|(
name|byte
index|[]
name|input
parameter_list|,
name|int
name|offset
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|newOffset
init|=
name|offset
decl_stmt|;
name|byte
name|tmp
init|=
name|input
index|[
name|newOffset
operator|++
index|]
decl_stmt|;
if|if
condition|(
name|tmp
operator|>=
literal|0
condition|)
block|{
return|return
operator|new
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
argument_list|(
operator|(
name|int
operator|)
name|tmp
argument_list|,
name|newOffset
operator|-
name|offset
argument_list|)
return|;
block|}
name|int
name|result
init|=
name|tmp
operator|&
literal|0x7f
decl_stmt|;
name|tmp
operator|=
name|input
index|[
name|newOffset
operator|++
index|]
expr_stmt|;
if|if
condition|(
name|tmp
operator|>=
literal|0
condition|)
block|{
name|result
operator||=
name|tmp
operator|<<
literal|7
expr_stmt|;
block|}
else|else
block|{
name|result
operator||=
operator|(
name|tmp
operator|&
literal|0x7f
operator|)
operator|<<
literal|7
expr_stmt|;
name|tmp
operator|=
name|input
index|[
name|newOffset
operator|++
index|]
expr_stmt|;
if|if
condition|(
name|tmp
operator|>=
literal|0
condition|)
block|{
name|result
operator||=
name|tmp
operator|<<
literal|14
expr_stmt|;
block|}
else|else
block|{
name|result
operator||=
operator|(
name|tmp
operator|&
literal|0x7f
operator|)
operator|<<
literal|14
expr_stmt|;
name|tmp
operator|=
name|input
index|[
name|newOffset
operator|++
index|]
expr_stmt|;
if|if
condition|(
name|tmp
operator|>=
literal|0
condition|)
block|{
name|result
operator||=
name|tmp
operator|<<
literal|21
expr_stmt|;
block|}
else|else
block|{
name|result
operator||=
operator|(
name|tmp
operator|&
literal|0x7f
operator|)
operator|<<
literal|21
expr_stmt|;
name|tmp
operator|=
name|input
index|[
name|newOffset
operator|++
index|]
expr_stmt|;
name|result
operator||=
name|tmp
operator|<<
literal|28
expr_stmt|;
if|if
condition|(
name|tmp
operator|<
literal|0
condition|)
block|{
comment|// Discard upper 32 bits.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|tmp
operator|=
name|input
index|[
name|newOffset
operator|++
index|]
expr_stmt|;
if|if
condition|(
name|tmp
operator|>=
literal|0
condition|)
block|{
return|return
operator|new
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
argument_list|(
name|result
argument_list|,
name|newOffset
operator|-
name|offset
argument_list|)
return|;
block|}
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Malformed varint"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
return|return
operator|new
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
argument_list|(
name|result
argument_list|,
name|newOffset
operator|-
name|offset
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|short
name|toShort
parameter_list|(
name|byte
name|hi
parameter_list|,
name|byte
name|lo
parameter_list|)
block|{
name|short
name|s
init|=
call|(
name|short
call|)
argument_list|(
operator|(
operator|(
name|hi
operator|&
literal|0xFF
operator|)
operator|<<
literal|8
operator|)
operator||
operator|(
name|lo
operator|&
literal|0xFF
operator|)
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|s
operator|>=
literal|0
argument_list|)
expr_stmt|;
return|return
name|s
return|;
block|}
specifier|public
specifier|static
name|void
name|writeShort
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|short
name|v
parameter_list|)
throws|throws
name|IOException
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|v
operator|>=
literal|0
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|v
operator|>>
literal|8
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
name|v
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|writeInt
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|int
name|v
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|v
operator|>>
literal|24
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|v
operator|>>
literal|16
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|v
operator|>>
literal|8
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
name|v
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|writeLong
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|long
name|v
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|v
operator|>>
literal|56
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|v
operator|>>
literal|48
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|v
operator|>>
literal|40
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|v
operator|>>
literal|32
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|v
operator|>>
literal|24
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|v
operator|>>
literal|16
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|v
operator|>>
literal|8
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
name|v
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|long
name|readLong
parameter_list|(
name|InputStream
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|result
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|shift
init|=
literal|56
init|;
name|shift
operator|>=
literal|0
condition|;
name|shift
operator|-=
literal|8
control|)
block|{
name|long
name|x
init|=
name|in
operator|.
name|read
argument_list|()
decl_stmt|;
if|if
condition|(
name|x
operator|<
literal|0
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"EOF"
argument_list|)
throw|;
name|result
operator||=
operator|(
name|x
operator|<<
name|shift
operator|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

