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
name|procedure2
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
name|OutputStream
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
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Similar to the ByteArrayOutputStream, with the exception that we can prepend an header.  * e.g. you write some data and you want to prepend an header that contains the data len or cksum.  *<code>  * ByteSlot slot = new ByteSlot();  * // write data  * slot.write(...);  * slot.write(...);  * // write header with the size of the written data  * slot.markHead();  * slot.write(Bytes.toBytes(slot.size()));  * // flush to stream as [header, data]  * slot.writeTo(stream);  *</code>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|ByteSlot
extends|extends
name|OutputStream
block|{
specifier|private
specifier|static
specifier|final
name|int
name|DOUBLE_GROW_LIMIT
init|=
literal|1
operator|<<
literal|20
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|GROW_ALIGN
init|=
literal|128
decl_stmt|;
specifier|private
name|byte
index|[]
name|buf
decl_stmt|;
specifier|private
name|int
name|head
decl_stmt|;
specifier|private
name|int
name|size
decl_stmt|;
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|head
operator|=
literal|0
expr_stmt|;
name|size
operator|=
literal|0
expr_stmt|;
block|}
specifier|public
name|void
name|markHead
parameter_list|()
block|{
name|head
operator|=
name|size
expr_stmt|;
block|}
specifier|public
name|int
name|getHead
parameter_list|()
block|{
return|return
name|head
return|;
block|}
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|size
return|;
block|}
specifier|public
name|byte
index|[]
name|getBuffer
parameter_list|()
block|{
return|return
name|buf
return|;
block|}
specifier|public
name|void
name|writeAt
parameter_list|(
name|int
name|offset
parameter_list|,
name|int
name|b
parameter_list|)
block|{
name|head
operator|=
name|Math
operator|.
name|min
argument_list|(
name|head
argument_list|,
name|offset
argument_list|)
expr_stmt|;
name|buf
index|[
name|offset
index|]
operator|=
operator|(
name|byte
operator|)
name|b
expr_stmt|;
block|}
specifier|public
name|void
name|write
parameter_list|(
name|int
name|b
parameter_list|)
block|{
name|ensureCapacity
argument_list|(
name|size
operator|+
literal|1
argument_list|)
expr_stmt|;
name|buf
index|[
name|size
operator|++
index|]
operator|=
operator|(
name|byte
operator|)
name|b
expr_stmt|;
block|}
specifier|public
name|void
name|write
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
block|{
name|ensureCapacity
argument_list|(
name|size
operator|+
name|len
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|b
argument_list|,
name|off
argument_list|,
name|buf
argument_list|,
name|size
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|size
operator|+=
name|len
expr_stmt|;
block|}
specifier|public
name|void
name|writeTo
parameter_list|(
specifier|final
name|OutputStream
name|stream
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|head
operator|!=
literal|0
condition|)
block|{
name|stream
operator|.
name|write
argument_list|(
name|buf
argument_list|,
name|head
argument_list|,
name|size
operator|-
name|head
argument_list|)
expr_stmt|;
name|stream
operator|.
name|write
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|head
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|stream
operator|.
name|write
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|ensureCapacity
parameter_list|(
name|int
name|minCapacity
parameter_list|)
block|{
name|minCapacity
operator|=
operator|(
name|minCapacity
operator|+
operator|(
name|GROW_ALIGN
operator|-
literal|1
operator|)
operator|)
operator|&
operator|-
name|GROW_ALIGN
expr_stmt|;
if|if
condition|(
name|buf
operator|==
literal|null
condition|)
block|{
name|buf
operator|=
operator|new
name|byte
index|[
name|minCapacity
index|]
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|minCapacity
operator|>
name|buf
operator|.
name|length
condition|)
block|{
name|int
name|newCapacity
init|=
name|buf
operator|.
name|length
operator|<<
literal|1
decl_stmt|;
if|if
condition|(
name|minCapacity
operator|>
name|newCapacity
operator|||
name|newCapacity
operator|>
name|DOUBLE_GROW_LIMIT
condition|)
block|{
name|newCapacity
operator|=
name|minCapacity
expr_stmt|;
block|}
name|buf
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|buf
argument_list|,
name|newCapacity
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

