begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Channels
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
name|WritableByteChannel
import|;
end_import

begin_comment
comment|/**  * Not thread safe!  */
end_comment

begin_class
specifier|public
class|class
name|ByteBufferOutputStream
extends|extends
name|OutputStream
block|{
specifier|protected
name|ByteBuffer
name|buf
decl_stmt|;
specifier|public
name|ByteBufferOutputStream
parameter_list|(
name|int
name|capacity
parameter_list|)
block|{
name|this
argument_list|(
name|capacity
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ByteBufferOutputStream
parameter_list|(
name|int
name|capacity
parameter_list|,
name|boolean
name|useDirectByteBuffer
parameter_list|)
block|{
if|if
condition|(
name|useDirectByteBuffer
condition|)
block|{
name|buf
operator|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|capacity
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|buf
operator|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|capacity
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|buf
operator|.
name|position
argument_list|()
return|;
block|}
comment|/**    * This flips the underlying BB so be sure to use it _last_!    * @return ByteBuffer    */
specifier|public
name|ByteBuffer
name|getByteBuffer
parameter_list|()
block|{
name|buf
operator|.
name|flip
argument_list|()
expr_stmt|;
return|return
name|buf
return|;
block|}
specifier|private
name|void
name|checkSizeAndGrow
parameter_list|(
name|int
name|extra
parameter_list|)
block|{
if|if
condition|(
operator|(
name|buf
operator|.
name|position
argument_list|()
operator|+
name|extra
operator|)
operator|>
name|buf
operator|.
name|limit
argument_list|()
condition|)
block|{
comment|// size calculation is complex, because we could overflow negative,
comment|// and/or not allocate enough space. this fixes that.
name|int
name|newSize
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|min
argument_list|(
operator|(
operator|(
operator|(
name|long
operator|)
name|buf
operator|.
name|capacity
argument_list|()
operator|)
operator|*
literal|2
operator|)
argument_list|,
call|(
name|long
call|)
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
decl_stmt|;
name|newSize
operator|=
name|Math
operator|.
name|max
argument_list|(
name|newSize
argument_list|,
name|buf
operator|.
name|position
argument_list|()
operator|+
name|extra
argument_list|)
expr_stmt|;
name|ByteBuffer
name|newBuf
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|newSize
argument_list|)
decl_stmt|;
name|buf
operator|.
name|flip
argument_list|()
expr_stmt|;
name|newBuf
operator|.
name|put
argument_list|(
name|buf
argument_list|)
expr_stmt|;
name|buf
operator|=
name|newBuf
expr_stmt|;
block|}
block|}
comment|// OutputStream
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
operator|.
name|put
argument_list|(
operator|(
name|byte
operator|)
name|b
argument_list|)
expr_stmt|;
block|}
comment|/**   * Writes the complete contents of this byte buffer output stream to   * the specified output stream argument.   *   * @param      out   the output stream to which to write the data.   * @exception  IOException  if an I/O error occurs.   */
specifier|public
specifier|synchronized
name|void
name|writeTo
parameter_list|(
name|OutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|WritableByteChannel
name|channel
init|=
name|Channels
operator|.
name|newChannel
argument_list|(
name|out
argument_list|)
decl_stmt|;
name|ByteBuffer
name|bb
init|=
name|buf
operator|.
name|duplicate
argument_list|()
decl_stmt|;
name|bb
operator|.
name|flip
argument_list|()
expr_stmt|;
name|channel
operator|.
name|write
argument_list|(
name|bb
argument_list|)
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
parameter_list|)
throws|throws
name|IOException
block|{
name|checkSizeAndGrow
argument_list|(
name|b
operator|.
name|length
argument_list|)
expr_stmt|;
name|buf
operator|.
name|put
argument_list|(
name|b
argument_list|)
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
name|buf
operator|.
name|put
argument_list|(
name|b
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|flush
parameter_list|()
throws|throws
name|IOException
block|{
comment|// noop
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
comment|// noop again. heh
block|}
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|(
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|ByteBuffer
name|bb
init|=
name|buf
operator|.
name|duplicate
argument_list|()
decl_stmt|;
name|bb
operator|.
name|flip
argument_list|()
expr_stmt|;
name|byte
index|[]
name|chunk
init|=
operator|new
name|byte
index|[
name|length
index|]
decl_stmt|;
name|bb
operator|.
name|position
argument_list|(
name|offset
argument_list|)
expr_stmt|;
name|bb
operator|.
name|get
argument_list|(
name|chunk
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
return|return
name|chunk
return|;
block|}
block|}
end_class

end_unit

