begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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
name|DataOutputStream
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
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|io
operator|.
name|DataInputBuffer
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
name|Writable
import|;
end_import

begin_comment
comment|/**  * Utility class with methods for manipulating Writable objects  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|Writables
block|{
comment|/**    * @param w writable    * @return The bytes of<code>w</code> gotten by running its    * {@link Writable#write(java.io.DataOutput)} method.    * @throws IOException e    * @see #getWritable(byte[], Writable)    */
specifier|public
specifier|static
name|byte
index|[]
name|getBytes
parameter_list|(
specifier|final
name|Writable
name|w
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|w
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Writable cannot be null"
argument_list|)
throw|;
block|}
name|ByteArrayOutputStream
name|byteStream
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|out
init|=
operator|new
name|DataOutputStream
argument_list|(
name|byteStream
argument_list|)
decl_stmt|;
try|try
block|{
name|w
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
name|out
operator|=
literal|null
expr_stmt|;
return|return
name|byteStream
operator|.
name|toByteArray
argument_list|()
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|out
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Put a bunch of Writables as bytes all into the one byte array.    * @param ws writable    * @return The bytes of<code>w</code> gotten by running its    * {@link Writable#write(java.io.DataOutput)} method.    * @throws IOException e    */
specifier|public
specifier|static
name|byte
index|[]
name|getBytes
parameter_list|(
specifier|final
name|Writable
modifier|...
name|ws
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|bytes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|ws
operator|.
name|length
argument_list|)
decl_stmt|;
name|int
name|size
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Writable
name|w
range|:
name|ws
control|)
block|{
name|byte
index|[]
name|b
init|=
name|getBytes
argument_list|(
name|w
argument_list|)
decl_stmt|;
name|size
operator|+=
name|b
operator|.
name|length
expr_stmt|;
name|bytes
operator|.
name|add
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|result
init|=
operator|new
name|byte
index|[
name|size
index|]
decl_stmt|;
name|int
name|offset
init|=
literal|0
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|b
range|:
name|bytes
control|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|result
argument_list|,
name|offset
argument_list|,
name|b
operator|.
name|length
argument_list|)
expr_stmt|;
name|offset
operator|+=
name|b
operator|.
name|length
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * Set bytes into the passed Writable by calling its    * {@link Writable#readFields(java.io.DataInput)}.    * @param bytes serialized bytes    * @param w An empty Writable (usually made by calling the null-arg    * constructor).    * @return The passed Writable after its readFields has been called fed    * by the passed<code>bytes</code> array or IllegalArgumentException    * if passed null or an empty<code>bytes</code> array.    * @throws IOException e    * @throws IllegalArgumentException    */
specifier|public
specifier|static
name|Writable
name|getWritable
parameter_list|(
specifier|final
name|byte
index|[]
name|bytes
parameter_list|,
specifier|final
name|Writable
name|w
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getWritable
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|length
argument_list|,
name|w
argument_list|)
return|;
block|}
comment|/**    * Set bytes into the passed Writable by calling its    * {@link Writable#readFields(java.io.DataInput)}.    * @param bytes serialized bytes    * @param offset offset into array    * @param length length of data    * @param w An empty Writable (usually made by calling the null-arg    * constructor).    * @return The passed Writable after its readFields has been called fed    * by the passed<code>bytes</code> array or IllegalArgumentException    * if passed null or an empty<code>bytes</code> array.    * @throws IOException e    * @throws IllegalArgumentException    */
specifier|public
specifier|static
name|Writable
name|getWritable
parameter_list|(
specifier|final
name|byte
index|[]
name|bytes
parameter_list|,
specifier|final
name|int
name|offset
parameter_list|,
specifier|final
name|int
name|length
parameter_list|,
specifier|final
name|Writable
name|w
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|bytes
operator|==
literal|null
operator|||
name|length
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't build a writable with empty "
operator|+
literal|"bytes array"
argument_list|)
throw|;
block|}
if|if
condition|(
name|w
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Writable cannot be null"
argument_list|)
throw|;
block|}
name|DataInputBuffer
name|in
init|=
operator|new
name|DataInputBuffer
argument_list|()
decl_stmt|;
try|try
block|{
name|in
operator|.
name|reset
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|w
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|w
return|;
block|}
finally|finally
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Copy one Writable to another.  Copies bytes using data streams.    * @param src Source Writable    * @param tgt Target Writable    * @return The target Writable.    * @throws IOException e    */
specifier|public
specifier|static
name|Writable
name|copyWritable
parameter_list|(
specifier|final
name|Writable
name|src
parameter_list|,
specifier|final
name|Writable
name|tgt
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|copyWritable
argument_list|(
name|getBytes
argument_list|(
name|src
argument_list|)
argument_list|,
name|tgt
argument_list|)
return|;
block|}
comment|/**    * Copy one Writable to another.  Copies bytes using data streams.    * @param bytes Source Writable    * @param tgt Target Writable    * @return The target Writable.    * @throws IOException e    */
specifier|public
specifier|static
name|Writable
name|copyWritable
parameter_list|(
specifier|final
name|byte
index|[]
name|bytes
parameter_list|,
specifier|final
name|Writable
name|tgt
parameter_list|)
throws|throws
name|IOException
block|{
name|DataInputStream
name|dis
init|=
operator|new
name|DataInputStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|bytes
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|tgt
operator|.
name|readFields
argument_list|(
name|dis
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|dis
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|tgt
return|;
block|}
block|}
end_class

end_unit

