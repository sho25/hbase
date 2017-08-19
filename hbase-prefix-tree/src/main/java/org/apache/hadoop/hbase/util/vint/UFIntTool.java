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
operator|.
name|vint
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
name|OutputStream
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
name|hadoop
operator|.
name|hbase
operator|.
name|nio
operator|.
name|ByteBuff
import|;
end_import

begin_comment
comment|/**  * UFInt is an abbreviation for Unsigned Fixed-width Integer.  *  * This class converts between positive ints and 1-4 bytes that represent the int.  All input ints  * must be positive.  Max values stored in N bytes are:  *  * N=1: 2^8  =&gt;           256  * N=2: 2^16 =&gt;        65,536  * N=3: 2^24 =&gt;    16,777,216  * N=4: 2^31 =&gt; 2,147,483,648 (Integer.MAX_VALUE)  *  * This was created to get most of the memory savings of a variable length integer when encoding  * an array of input integers, but to fix the number of bytes for each integer to the number needed  * to store the maximum integer in the array.  This enables a binary search to be performed on the  * array of encoded integers.  *  * PrefixTree nodes often store offsets into a block that can fit into 1 or 2 bytes.  Note that if  * the maximum value of an array of numbers needs 2 bytes, then it's likely that a majority of the  * numbers will also require 2 bytes.  *  * warnings:  *  * no input validation for max performance  *  * no negatives  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|UFIntTool
block|{
specifier|private
specifier|static
specifier|final
name|int
name|NUM_BITS_IN_LONG
init|=
literal|64
decl_stmt|;
specifier|public
specifier|static
name|long
name|maxValueForNumBytes
parameter_list|(
name|int
name|numBytes
parameter_list|)
block|{
return|return
operator|(
literal|1L
operator|<<
operator|(
name|numBytes
operator|*
literal|8
operator|)
operator|)
operator|-
literal|1
return|;
block|}
specifier|public
specifier|static
name|int
name|numBytes
parameter_list|(
specifier|final
name|long
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|0
condition|)
block|{
comment|// 0 doesn't work with the formula below
return|return
literal|1
return|;
block|}
return|return
operator|(
name|NUM_BITS_IN_LONG
operator|+
literal|7
operator|-
name|Long
operator|.
name|numberOfLeadingZeros
argument_list|(
name|value
argument_list|)
operator|)
operator|/
literal|8
return|;
block|}
specifier|public
specifier|static
name|byte
index|[]
name|getBytes
parameter_list|(
name|int
name|outputWidth
parameter_list|,
specifier|final
name|long
name|value
parameter_list|)
block|{
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
name|outputWidth
index|]
decl_stmt|;
name|writeBytes
argument_list|(
name|outputWidth
argument_list|,
name|value
argument_list|,
name|bytes
argument_list|,
literal|0
argument_list|)
expr_stmt|;
return|return
name|bytes
return|;
block|}
specifier|public
specifier|static
name|void
name|writeBytes
parameter_list|(
name|int
name|outputWidth
parameter_list|,
specifier|final
name|long
name|value
parameter_list|,
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
name|bytes
index|[
name|offset
operator|+
name|outputWidth
operator|-
literal|1
index|]
operator|=
operator|(
name|byte
operator|)
name|value
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
name|outputWidth
operator|-
literal|2
init|;
name|i
operator|>=
literal|0
condition|;
operator|--
name|i
control|)
block|{
name|bytes
index|[
name|offset
operator|+
name|i
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|value
operator|>>>
operator|(
name|outputWidth
operator|-
name|i
operator|-
literal|1
operator|)
operator|*
literal|8
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
specifier|final
name|long
index|[]
name|MASKS
init|=
operator|new
name|long
index|[]
block|{
operator|(
name|long
operator|)
literal|255
block|,
operator|(
name|long
operator|)
literal|255
operator|<<
literal|8
block|,
operator|(
name|long
operator|)
literal|255
operator|<<
literal|16
block|,
operator|(
name|long
operator|)
literal|255
operator|<<
literal|24
block|,
operator|(
name|long
operator|)
literal|255
operator|<<
literal|32
block|,
operator|(
name|long
operator|)
literal|255
operator|<<
literal|40
block|,
operator|(
name|long
operator|)
literal|255
operator|<<
literal|48
block|,
operator|(
name|long
operator|)
literal|255
operator|<<
literal|56
block|}
decl_stmt|;
specifier|public
specifier|static
name|void
name|writeBytes
parameter_list|(
name|int
name|outputWidth
parameter_list|,
specifier|final
name|long
name|value
parameter_list|,
name|OutputStream
name|os
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
name|outputWidth
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
name|os
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
operator|(
name|value
operator|&
name|MASKS
index|[
name|i
index|]
operator|)
operator|>>>
operator|(
literal|8
operator|*
name|i
operator|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|long
name|fromBytes
parameter_list|(
specifier|final
name|byte
index|[]
name|bytes
parameter_list|)
block|{
name|long
name|value
init|=
literal|0
decl_stmt|;
name|value
operator||=
name|bytes
index|[
literal|0
index|]
operator|&
literal|0xff
expr_stmt|;
comment|// these seem to do ok without casting the byte to int
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|bytes
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|value
operator|<<=
literal|8
expr_stmt|;
name|value
operator||=
name|bytes
index|[
name|i
index|]
operator|&
literal|0xff
expr_stmt|;
block|}
return|return
name|value
return|;
block|}
specifier|public
specifier|static
name|long
name|fromBytes
parameter_list|(
specifier|final
name|ByteBuff
name|buf
parameter_list|,
specifier|final
name|int
name|offset
parameter_list|,
specifier|final
name|int
name|width
parameter_list|)
block|{
name|long
name|value
init|=
literal|0
decl_stmt|;
name|value
operator||=
name|buf
operator|.
name|get
argument_list|(
name|offset
operator|+
literal|0
argument_list|)
operator|&
literal|0xff
expr_stmt|;
comment|// these seem to do ok without casting the byte to int
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|width
condition|;
operator|++
name|i
control|)
block|{
name|value
operator|<<=
literal|8
expr_stmt|;
name|value
operator||=
name|buf
operator|.
name|get
argument_list|(
name|i
operator|+
name|offset
argument_list|)
operator|&
literal|0xff
expr_stmt|;
block|}
return|return
name|value
return|;
block|}
block|}
end_class

end_unit

