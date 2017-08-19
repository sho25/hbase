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

begin_comment
comment|/**  * Simple Variable Length Integer encoding.  Left bit of 0 means we are on the last byte.  If left  * bit of the current byte is 1, then there is at least one more byte.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|UVLongTool
block|{
specifier|public
specifier|static
specifier|final
name|byte
name|BYTE_7_RIGHT_BITS_SET
init|=
literal|127
decl_stmt|,
name|BYTE_LEFT_BIT_SET
init|=
operator|-
literal|128
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|LONG_7_RIGHT_BITS_SET
init|=
literal|127
decl_stmt|,
name|LONG_8TH_BIT_SET
init|=
literal|128
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|MAX_VALUE_BYTES
init|=
operator|new
name|byte
index|[]
block|{
operator|-
literal|1
block|,
operator|-
literal|1
block|,
operator|-
literal|1
block|,
operator|-
literal|1
block|,
operator|-
literal|1
block|,
operator|-
literal|1
block|,
operator|-
literal|1
block|,
operator|-
literal|1
block|,
literal|127
block|}
decl_stmt|;
comment|/********************* long -&gt; bytes **************************/
specifier|public
specifier|static
name|int
name|numBytes
parameter_list|(
name|long
name|in
parameter_list|)
block|{
comment|// do a check for illegal arguments if not protected
if|if
condition|(
name|in
operator|==
literal|0
condition|)
block|{
return|return
literal|1
return|;
block|}
comment|// doesn't work with the formula below
return|return
operator|(
literal|70
operator|-
name|Long
operator|.
name|numberOfLeadingZeros
argument_list|(
name|in
argument_list|)
operator|)
operator|/
literal|7
return|;
comment|// 70 comes from 64+(7-1)
block|}
specifier|public
specifier|static
name|byte
index|[]
name|getBytes
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|int
name|numBytes
init|=
name|numBytes
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
name|numBytes
index|]
decl_stmt|;
name|long
name|remainder
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
name|numBytes
operator|-
literal|1
condition|;
operator|++
name|i
control|)
block|{
name|bytes
index|[
name|i
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
operator|(
name|remainder
operator|&
name|LONG_7_RIGHT_BITS_SET
operator|)
operator||
name|LONG_8TH_BIT_SET
argument_list|)
expr_stmt|;
comment|// set the left bit
name|remainder
operator|>>=
literal|7
expr_stmt|;
block|}
name|bytes
index|[
name|numBytes
operator|-
literal|1
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|remainder
operator|&
name|LONG_7_RIGHT_BITS_SET
argument_list|)
expr_stmt|;
comment|// do not set the left bit
return|return
name|bytes
return|;
block|}
specifier|public
specifier|static
name|int
name|writeBytes
parameter_list|(
name|long
name|value
parameter_list|,
name|OutputStream
name|os
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|numBytes
init|=
name|numBytes
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|long
name|remainder
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
name|numBytes
operator|-
literal|1
condition|;
operator|++
name|i
control|)
block|{
comment|// set the left bit
name|os
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
operator|(
name|remainder
operator|&
name|LONG_7_RIGHT_BITS_SET
operator|)
operator||
name|LONG_8TH_BIT_SET
argument_list|)
argument_list|)
expr_stmt|;
name|remainder
operator|>>=
literal|7
expr_stmt|;
block|}
comment|// do not set the left bit
name|os
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|remainder
operator|&
name|LONG_7_RIGHT_BITS_SET
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|numBytes
return|;
block|}
comment|/******************** bytes -&gt; long **************************/
specifier|public
specifier|static
name|long
name|getLong
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
block|{
return|return
name|getLong
argument_list|(
operator|new
name|SingleByteBuff
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|)
argument_list|)
argument_list|,
literal|0
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|long
name|getLong
parameter_list|(
name|ByteBuff
name|buf
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
name|long
name|value
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
condition|;
operator|++
name|i
control|)
block|{
name|byte
name|b
init|=
name|buf
operator|.
name|get
argument_list|(
name|offset
operator|+
name|i
argument_list|)
decl_stmt|;
name|long
name|shifted
init|=
name|BYTE_7_RIGHT_BITS_SET
operator|&
name|b
decl_stmt|;
comment|// kill leftmost bit
name|shifted
operator|<<=
literal|7
operator|*
name|i
expr_stmt|;
name|value
operator||=
name|shifted
expr_stmt|;
if|if
condition|(
name|b
operator|>=
literal|0
condition|)
block|{
break|break;
block|}
comment|// first bit was 0, so that's the last byte in the VarLong
block|}
return|return
name|value
return|;
block|}
specifier|public
specifier|static
name|long
name|getLong
parameter_list|(
name|InputStream
name|is
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|value
init|=
literal|0
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
name|int
name|b
decl_stmt|;
do|do
block|{
name|b
operator|=
name|is
operator|.
name|read
argument_list|()
expr_stmt|;
name|long
name|shifted
init|=
name|BYTE_7_RIGHT_BITS_SET
operator|&
name|b
decl_stmt|;
comment|// kill leftmost bit
name|shifted
operator|<<=
literal|7
operator|*
name|i
expr_stmt|;
name|value
operator||=
name|shifted
expr_stmt|;
operator|++
name|i
expr_stmt|;
block|}
do|while
condition|(
name|b
operator|>
name|Byte
operator|.
name|MAX_VALUE
condition|)
do|;
return|return
name|value
return|;
block|}
block|}
end_class

end_unit

