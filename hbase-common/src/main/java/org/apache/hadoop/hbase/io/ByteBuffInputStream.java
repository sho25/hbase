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
name|InputStream
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Not thread safe!  *<p>  * Please note that the reads will cause position movement on wrapped ByteBuff.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ByteBuffInputStream
extends|extends
name|InputStream
block|{
specifier|private
name|ByteBuff
name|buf
decl_stmt|;
specifier|public
name|ByteBuffInputStream
parameter_list|(
name|ByteBuff
name|buf
parameter_list|)
block|{
name|this
operator|.
name|buf
operator|=
name|buf
expr_stmt|;
block|}
comment|/**    * Reads the next byte of data from this input stream. The value byte is returned as an    *<code>int</code> in the range<code>0</code> to<code>255</code>. If no byte is available    * because the end of the stream has been reached, the value<code>-1</code> is returned.    * @return the next byte of data, or<code>-1</code> if the end of the stream has been reached.    */
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|buf
operator|.
name|hasRemaining
argument_list|()
condition|)
block|{
return|return
operator|(
name|this
operator|.
name|buf
operator|.
name|get
argument_list|()
operator|&
literal|0xff
operator|)
return|;
block|}
return|return
operator|-
literal|1
return|;
block|}
comment|/**    * Reads up to next<code>len</code> bytes of data from buffer into passed array(starting from    * given offset).    * @param b the array into which the data is read.    * @param off the start offset in the destination array<code>b</code>    * @param len the maximum number of bytes to read.    * @return the total number of bytes actually read into the buffer, or<code>-1</code> if not even    *         1 byte can be read because the end of the stream has been reached.    */
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|(
name|byte
name|b
index|[]
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
block|{
name|int
name|avail
init|=
name|available
argument_list|()
decl_stmt|;
if|if
condition|(
name|avail
operator|<=
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|len
operator|<=
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
if|if
condition|(
name|len
operator|>
name|avail
condition|)
block|{
name|len
operator|=
name|avail
expr_stmt|;
block|}
name|this
operator|.
name|buf
operator|.
name|get
argument_list|(
name|b
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
expr_stmt|;
return|return
name|len
return|;
block|}
comment|/**    * Skips<code>n</code> bytes of input from this input stream. Fewer bytes might be skipped if the    * end of the input stream is reached. The actual number<code>k</code> of bytes to be skipped is    * equal to the smaller of<code>n</code> and remaining bytes in the stream.    * @param n the number of bytes to be skipped.    * @return the actual number of bytes skipped.    */
annotation|@
name|Override
specifier|public
name|long
name|skip
parameter_list|(
name|long
name|n
parameter_list|)
block|{
name|long
name|k
init|=
name|Math
operator|.
name|min
argument_list|(
name|n
argument_list|,
name|available
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|k
operator|<=
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
name|this
operator|.
name|buf
operator|.
name|skip
argument_list|(
operator|(
name|int
operator|)
name|k
argument_list|)
expr_stmt|;
return|return
name|k
return|;
block|}
comment|/**    * @return  the number of remaining bytes that can be read (or skipped    *          over) from this input stream.    */
annotation|@
name|Override
specifier|public
name|int
name|available
parameter_list|()
block|{
return|return
name|this
operator|.
name|buf
operator|.
name|remaining
argument_list|()
return|;
block|}
block|}
end_class

end_unit

