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
name|nio
operator|.
name|BufferOverflowException
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
name|Bytes
import|;
end_import

begin_comment
comment|/**  * Our own implementation of ByteArrayOutputStream where all methods are NOT synchronized and  * supports writing ByteBuffer directly to it.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ByteArrayOutputStream
extends|extends
name|OutputStream
implements|implements
name|ByteBufferSupportOutputStream
block|{
comment|// Borrowed from openJDK:
comment|// http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/8-b132/java/util/ArrayList.java#221
specifier|private
specifier|static
specifier|final
name|int
name|MAX_ARRAY_SIZE
init|=
name|Integer
operator|.
name|MAX_VALUE
operator|-
literal|8
decl_stmt|;
specifier|private
name|byte
index|[]
name|buf
decl_stmt|;
specifier|private
name|int
name|pos
init|=
literal|0
decl_stmt|;
specifier|public
name|ByteArrayOutputStream
parameter_list|()
block|{
name|this
argument_list|(
literal|32
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ByteArrayOutputStream
parameter_list|(
name|int
name|capacity
parameter_list|)
block|{
name|this
operator|.
name|buf
operator|=
operator|new
name|byte
index|[
name|capacity
index|]
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|ByteBuffer
name|b
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
name|checkSizeAndGrow
argument_list|(
name|len
argument_list|)
expr_stmt|;
name|ByteBufferUtils
operator|.
name|copyFromBufferToArray
argument_list|(
name|this
operator|.
name|buf
argument_list|,
name|b
argument_list|,
name|off
argument_list|,
name|this
operator|.
name|pos
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|this
operator|.
name|pos
operator|+=
name|len
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeInt
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|IOException
block|{
name|checkSizeAndGrow
argument_list|(
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|putInt
argument_list|(
name|this
operator|.
name|buf
argument_list|,
name|this
operator|.
name|pos
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|this
operator|.
name|pos
operator|+=
name|Bytes
operator|.
name|SIZEOF_INT
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|int
name|b
parameter_list|)
throws|throws
name|IOException
block|{
name|checkSizeAndGrow
argument_list|(
name|Bytes
operator|.
name|SIZEOF_BYTE
argument_list|)
expr_stmt|;
name|buf
index|[
name|this
operator|.
name|pos
index|]
operator|=
operator|(
name|byte
operator|)
name|b
expr_stmt|;
name|this
operator|.
name|pos
operator|++
expr_stmt|;
block|}
annotation|@
name|Override
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
throws|throws
name|IOException
block|{
name|checkSizeAndGrow
argument_list|(
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
name|this
operator|.
name|buf
argument_list|,
name|this
operator|.
name|pos
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|this
operator|.
name|pos
operator|+=
name|len
expr_stmt|;
block|}
specifier|private
name|void
name|checkSizeAndGrow
parameter_list|(
name|int
name|extra
parameter_list|)
block|{
name|long
name|capacityNeeded
init|=
name|this
operator|.
name|pos
operator|+
operator|(
name|long
operator|)
name|extra
decl_stmt|;
if|if
condition|(
name|capacityNeeded
operator|>
name|this
operator|.
name|buf
operator|.
name|length
condition|)
block|{
comment|// guarantee it's possible to fit
if|if
condition|(
name|capacityNeeded
operator|>
name|MAX_ARRAY_SIZE
condition|)
block|{
throw|throw
operator|new
name|BufferOverflowException
argument_list|()
throw|;
block|}
comment|// double until hit the cap
name|long
name|nextCapacity
init|=
name|Math
operator|.
name|min
argument_list|(
name|this
operator|.
name|buf
operator|.
name|length
operator|<<
literal|1
argument_list|,
name|MAX_ARRAY_SIZE
argument_list|)
decl_stmt|;
comment|// but make sure there is enough if twice the existing capacity is still too small
name|nextCapacity
operator|=
name|Math
operator|.
name|max
argument_list|(
name|nextCapacity
argument_list|,
name|capacityNeeded
argument_list|)
expr_stmt|;
if|if
condition|(
name|nextCapacity
operator|>
name|MAX_ARRAY_SIZE
condition|)
block|{
throw|throw
operator|new
name|BufferOverflowException
argument_list|()
throw|;
block|}
name|byte
index|[]
name|newBuf
init|=
operator|new
name|byte
index|[
operator|(
name|int
operator|)
name|nextCapacity
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|newBuf
argument_list|,
literal|0
argument_list|,
name|buf
operator|.
name|length
argument_list|)
expr_stmt|;
name|buf
operator|=
name|newBuf
expr_stmt|;
block|}
block|}
comment|/**    * Resets the<code>pos</code> field of this byte array output stream to zero. The output stream    * can be used again.    */
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|this
operator|.
name|pos
operator|=
literal|0
expr_stmt|;
block|}
comment|/**    * Copies the content of this Stream into a new byte array.    * @return  the contents of this output stream, as new byte array.    */
specifier|public
name|byte
name|toByteArray
argument_list|()
index|[]
block|{
return|return
name|Arrays
operator|.
name|copyOf
argument_list|(
name|buf
argument_list|,
name|pos
argument_list|)
return|;
block|}
comment|/**    * @return the underlying array where the data gets accumulated    */
specifier|public
name|byte
index|[]
name|getBuffer
parameter_list|()
block|{
return|return
name|this
operator|.
name|buf
return|;
block|}
comment|/**    * @return The current size of the buffer.    */
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|this
operator|.
name|pos
return|;
block|}
block|}
end_class

end_unit

