begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  *  * The portion of this file denoted by 'Copied from com.google.protobuf.CodedOutputStream'  * is from Protocol Buffers v2.5.0 under the following license  *  * Copyright 2008 Google Inc.  All rights reserved.  * http://code.google.com/p/protobuf/  *  * Redistribution and use in source and binary forms, with or without  * modification, are permitted provided that the following conditions are  * met:  *  *     * Redistributions of source code must retain the above copyright  * notice, this list of conditions and the following disclaimer.  *     * Redistributions in binary form must reproduce the above  * copyright notice, this list of conditions and the following disclaimer  * in the documentation and/or other materials provided with the  * distribution.  *     * Neither the name of Google Inc. nor the names of its  * contributors may be used to endorse or promote products derived from  * this software without specific prior written permission.  *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  */
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

begin_comment
comment|/**  * A basic mutable {@link ByteRange} implementation.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|SimpleMutableByteRange
extends|extends
name|AbstractByteRange
block|{
comment|/**    * Create a new {@code ByteRange} lacking a backing array and with an    * undefined viewport.    */
specifier|public
name|SimpleMutableByteRange
parameter_list|()
block|{
name|unset
argument_list|()
expr_stmt|;
block|}
comment|/**    * Create a new {@code ByteRange} over a new backing array of size    * {@code capacity}. The range's offset and length are 0 and {@code capacity},    * respectively.    *     * @param capacity    *          the size of the backing array.    */
specifier|public
name|SimpleMutableByteRange
parameter_list|(
name|int
name|capacity
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|byte
index|[
name|capacity
index|]
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a new {@code ByteRange} over the provided {@code bytes}.    *     * @param bytes    *          The array to wrap.    */
specifier|public
name|SimpleMutableByteRange
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
block|{
name|set
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a new {@code ByteRange} over the provided {@code bytes}.    *    * @param bytes    *          The array to wrap.    * @param offset    *          The offset into {@code bytes} considered the beginning of this    *          range.    * @param length    *          The length of this range.    */
specifier|public
name|SimpleMutableByteRange
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
name|set
argument_list|(
name|bytes
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
name|ByteRange
name|unset
parameter_list|()
block|{
name|clearHashCache
argument_list|()
expr_stmt|;
name|bytes
operator|=
literal|null
expr_stmt|;
name|offset
operator|=
literal|0
expr_stmt|;
name|length
operator|=
literal|0
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteRange
name|put
parameter_list|(
name|int
name|index
parameter_list|,
name|byte
name|val
parameter_list|)
block|{
name|bytes
index|[
name|offset
operator|+
name|index
index|]
operator|=
name|val
expr_stmt|;
name|clearHashCache
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
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
block|{
if|if
condition|(
literal|0
operator|==
name|val
operator|.
name|length
condition|)
return|return
name|this
return|;
return|return
name|put
argument_list|(
name|index
argument_list|,
name|val
argument_list|,
literal|0
argument_list|,
name|val
operator|.
name|length
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
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
name|val
argument_list|,
name|offset
argument_list|,
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
name|length
argument_list|)
expr_stmt|;
name|clearHashCache
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteRange
name|putShort
parameter_list|(
name|int
name|index
parameter_list|,
name|short
name|val
parameter_list|)
block|{
comment|// This writing is same as BB's putShort. When byte[] is wrapped in a BB and
comment|// call putShort(),
comment|// one can get the same result.
name|bytes
index|[
name|offset
operator|+
name|index
operator|+
literal|1
index|]
operator|=
operator|(
name|byte
operator|)
name|val
expr_stmt|;
name|val
operator|>>=
literal|8
expr_stmt|;
name|bytes
index|[
name|offset
operator|+
name|index
index|]
operator|=
operator|(
name|byte
operator|)
name|val
expr_stmt|;
name|clearHashCache
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteRange
name|putInt
parameter_list|(
name|int
name|index
parameter_list|,
name|int
name|val
parameter_list|)
block|{
comment|// This writing is same as BB's putInt. When byte[] is wrapped in a BB and
comment|// call getInt(), one
comment|// can get the same result.
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
operator|>
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|bytes
index|[
name|offset
operator|+
name|index
operator|+
name|i
index|]
operator|=
operator|(
name|byte
operator|)
name|val
expr_stmt|;
name|val
operator|>>>=
literal|8
expr_stmt|;
block|}
name|bytes
index|[
name|offset
operator|+
name|index
index|]
operator|=
operator|(
name|byte
operator|)
name|val
expr_stmt|;
name|clearHashCache
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteRange
name|putLong
parameter_list|(
name|int
name|index
parameter_list|,
name|long
name|val
parameter_list|)
block|{
comment|// This writing is same as BB's putLong. When byte[] is wrapped in a BB and
comment|// call putLong(), one
comment|// can get the same result.
for|for
control|(
name|int
name|i
init|=
name|Bytes
operator|.
name|SIZEOF_LONG
operator|-
literal|1
init|;
name|i
operator|>
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|bytes
index|[
name|offset
operator|+
name|index
operator|+
name|i
index|]
operator|=
operator|(
name|byte
operator|)
name|val
expr_stmt|;
name|val
operator|>>>=
literal|8
expr_stmt|;
block|}
name|bytes
index|[
name|offset
operator|+
name|index
index|]
operator|=
operator|(
name|byte
operator|)
name|val
expr_stmt|;
name|clearHashCache
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|// Copied from com.google.protobuf.CodedOutputStream v2.5.0 writeRawVarint64
annotation|@
name|Override
specifier|public
name|int
name|putVLong
parameter_list|(
name|int
name|index
parameter_list|,
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
literal|true
condition|)
block|{
if|if
condition|(
operator|(
name|val
operator|&
operator|~
literal|0x7F
operator|)
operator|==
literal|0
condition|)
block|{
name|bytes
index|[
name|offset
operator|+
name|index
operator|+
name|rPos
index|]
operator|=
operator|(
name|byte
operator|)
name|val
expr_stmt|;
break|break;
block|}
else|else
block|{
name|bytes
index|[
name|offset
operator|+
name|index
operator|+
name|rPos
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
operator|(
name|val
operator|&
literal|0x7F
operator|)
operator||
literal|0x80
argument_list|)
expr_stmt|;
name|val
operator|>>>=
literal|7
expr_stmt|;
block|}
name|rPos
operator|++
expr_stmt|;
block|}
name|clearHashCache
argument_list|()
expr_stmt|;
return|return
name|rPos
operator|+
literal|1
return|;
block|}
comment|// end copied from protobuf
annotation|@
name|Override
specifier|public
name|ByteRange
name|deepCopy
parameter_list|()
block|{
name|SimpleMutableByteRange
name|clone
init|=
operator|new
name|SimpleMutableByteRange
argument_list|(
name|deepCopyToNewArray
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|isHashCached
argument_list|()
condition|)
block|{
name|clone
operator|.
name|hash
operator|=
name|hash
expr_stmt|;
block|}
return|return
name|clone
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteRange
name|shallowCopy
parameter_list|()
block|{
name|SimpleMutableByteRange
name|clone
init|=
operator|new
name|SimpleMutableByteRange
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
name|isHashCached
argument_list|()
condition|)
block|{
name|clone
operator|.
name|hash
operator|=
name|hash
expr_stmt|;
block|}
return|return
name|clone
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteRange
name|shallowCopySubRange
parameter_list|(
name|int
name|innerOffset
parameter_list|,
name|int
name|copyLength
parameter_list|)
block|{
name|SimpleMutableByteRange
name|clone
init|=
operator|new
name|SimpleMutableByteRange
argument_list|(
name|bytes
argument_list|,
name|offset
operator|+
name|innerOffset
argument_list|,
name|copyLength
argument_list|)
decl_stmt|;
if|if
condition|(
name|isHashCached
argument_list|()
condition|)
block|{
name|clone
operator|.
name|hash
operator|=
name|hash
expr_stmt|;
block|}
return|return
name|clone
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|thatObject
parameter_list|)
block|{
if|if
condition|(
name|thatObject
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|this
operator|==
name|thatObject
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|hashCode
argument_list|()
operator|!=
name|thatObject
operator|.
name|hashCode
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|thatObject
operator|instanceof
name|SimpleMutableByteRange
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|SimpleMutableByteRange
name|that
init|=
operator|(
name|SimpleMutableByteRange
operator|)
name|thatObject
decl_stmt|;
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|that
operator|.
name|bytes
argument_list|,
name|that
operator|.
name|offset
argument_list|,
name|that
operator|.
name|length
argument_list|)
return|;
block|}
block|}
end_class

end_unit

