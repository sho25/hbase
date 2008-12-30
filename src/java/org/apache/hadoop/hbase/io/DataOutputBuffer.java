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
name|*
import|;
end_import

begin_comment
comment|/** A reusable {@link DataOutput} implementation that writes to an in-memory  * buffer.  *   *<p>This is copy of Hadoop SequenceFile brought local so we can fix bugs;  * e.g. hbase-1097</p>  *  *<p>This saves memory over creating a new DataOutputStream and  * ByteArrayOutputStream each time data is written.  *  *<p>Typical usage is something like the following:<pre>  *  * DataOutputBuffer buffer = new DataOutputBuffer();  * while (... loop condition ...) {  *   buffer.reset();  *   ... write buffer using DataOutput methods ...  *   byte[] data = buffer.getData();  *   int dataLength = buffer.getLength();  *   ... write data to its ultimate destination ...  * }  *</pre>  *    */
end_comment

begin_class
specifier|public
class|class
name|DataOutputBuffer
extends|extends
name|DataOutputStream
block|{
specifier|private
specifier|static
class|class
name|Buffer
extends|extends
name|ByteArrayOutputStream
block|{
specifier|public
name|byte
index|[]
name|getData
parameter_list|()
block|{
return|return
name|buf
return|;
block|}
specifier|public
name|int
name|getLength
parameter_list|()
block|{
return|return
name|count
return|;
block|}
comment|// Keep the initial buffer around so can put it back in place on reset.
specifier|private
specifier|final
name|byte
index|[]
name|initialBuffer
decl_stmt|;
specifier|public
name|Buffer
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|initialBuffer
operator|=
name|this
operator|.
name|buf
expr_stmt|;
block|}
specifier|public
name|Buffer
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|super
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|this
operator|.
name|initialBuffer
operator|=
name|this
operator|.
name|buf
expr_stmt|;
block|}
specifier|public
name|void
name|write
parameter_list|(
name|DataInput
name|in
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|newcount
init|=
name|count
operator|+
name|len
decl_stmt|;
if|if
condition|(
name|newcount
operator|>
name|buf
operator|.
name|length
condition|)
block|{
name|byte
name|newbuf
index|[]
init|=
operator|new
name|byte
index|[
name|Math
operator|.
name|max
argument_list|(
name|buf
operator|.
name|length
operator|<<
literal|1
argument_list|,
name|newcount
argument_list|)
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
name|newbuf
argument_list|,
literal|0
argument_list|,
name|count
argument_list|)
expr_stmt|;
name|buf
operator|=
name|newbuf
expr_stmt|;
block|}
name|in
operator|.
name|readFully
argument_list|(
name|buf
argument_list|,
name|count
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|count
operator|=
name|newcount
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|reset
parameter_list|()
block|{
comment|// Rest the buffer so we don't keep around the shape of the biggest
comment|// value ever read.
name|this
operator|.
name|buf
operator|=
name|this
operator|.
name|initialBuffer
expr_stmt|;
name|super
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|Buffer
name|buffer
decl_stmt|;
comment|/** Constructs a new empty buffer. */
specifier|public
name|DataOutputBuffer
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|Buffer
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|DataOutputBuffer
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|Buffer
argument_list|(
name|size
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|DataOutputBuffer
parameter_list|(
name|Buffer
name|buffer
parameter_list|)
block|{
name|super
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
name|this
operator|.
name|buffer
operator|=
name|buffer
expr_stmt|;
block|}
comment|/** Returns the current contents of the buffer.    *  Data is only valid to {@link #getLength()}.    */
specifier|public
name|byte
index|[]
name|getData
parameter_list|()
block|{
return|return
name|buffer
operator|.
name|getData
argument_list|()
return|;
block|}
comment|/** Returns the length of the valid data currently in the buffer. */
specifier|public
name|int
name|getLength
parameter_list|()
block|{
return|return
name|buffer
operator|.
name|getLength
argument_list|()
return|;
block|}
comment|/** Resets the buffer to empty. */
specifier|public
name|DataOutputBuffer
name|reset
parameter_list|()
block|{
name|this
operator|.
name|written
operator|=
literal|0
expr_stmt|;
name|buffer
operator|.
name|reset
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/** Writes bytes from a DataInput directly into the buffer. */
specifier|public
name|void
name|write
parameter_list|(
name|DataInput
name|in
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
name|buffer
operator|.
name|write
argument_list|(
name|in
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
comment|/** Write to a file stream */
specifier|public
name|void
name|writeTo
parameter_list|(
name|OutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|buffer
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

