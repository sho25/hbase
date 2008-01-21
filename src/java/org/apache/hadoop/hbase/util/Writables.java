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
name|io
operator|.
name|UnsupportedEncodingException
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
name|HConstants
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
name|HRegionInfo
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
name|LongWritable
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
specifier|public
class|class
name|Writables
block|{
comment|/**    * @param w    * @return The bytes of<code>w</code> gotten by running its     * {@link Writable#write(java.io.DataOutput)} method.    * @throws IOException    * @see #getWritable(byte[], Writable)    */
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
comment|/**    * Set bytes into the passed Writable by calling its    * {@link Writable#readFields(java.io.DataInput)}.    * @param bytes    * @param w An empty Writable (usually made by calling the null-arg    * constructor).    * @return The passed Writable after its readFields has been called fed    * by the passed<code>bytes</code> array or IllegalArgumentException    * if passed null or an empty<code>bytes</code> array.    * @throws IOException    * @throws IllegalArgumentException    */
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
if|if
condition|(
name|bytes
operator|==
literal|null
operator|||
name|bytes
operator|.
name|length
operator|==
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
name|bytes
operator|.
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
comment|/**    * @param bytes    * @return A HRegionInfo instance built out of passed<code>bytes</code>.    * @throws IOException    */
specifier|public
specifier|static
name|HRegionInfo
name|getHRegionInfo
parameter_list|(
specifier|final
name|byte
index|[]
name|bytes
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|(
name|HRegionInfo
operator|)
name|getWritable
argument_list|(
name|bytes
argument_list|,
operator|new
name|HRegionInfo
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @param bytes    * @return A HRegionInfo instance built out of passed<code>bytes</code>    * or<code>null</code> if passed bytes are null or an empty array.    * @throws IOException    */
specifier|public
specifier|static
name|HRegionInfo
name|getHRegionInfoOrNull
parameter_list|(
specifier|final
name|byte
index|[]
name|bytes
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|(
name|bytes
operator|==
literal|null
operator|||
name|bytes
operator|.
name|length
operator|<=
literal|0
operator|)
condition|?
operator|(
name|HRegionInfo
operator|)
literal|null
else|:
name|getHRegionInfo
argument_list|(
name|bytes
argument_list|)
return|;
block|}
comment|/**    * Copy one Writable to another.  Copies bytes using data streams.    * @param src Source Writable    * @param tgt Target Writable    * @return The target Writable.    * @throws IOException    */
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
if|if
condition|(
name|src
operator|==
literal|null
operator|||
name|tgt
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Writables cannot be null"
argument_list|)
throw|;
block|}
name|byte
index|[]
name|bytes
init|=
name|getBytes
argument_list|(
name|src
argument_list|)
decl_stmt|;
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
comment|/**    * Convert a long value to a byte array    * @param val    * @return the byte array    * @throws IOException    */
specifier|public
specifier|static
name|byte
index|[]
name|longToBytes
parameter_list|(
name|long
name|val
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getBytes
argument_list|(
operator|new
name|LongWritable
argument_list|(
name|val
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Converts a byte array to a long value    * @param bytes    * @return the long value    * @throws IOException    */
specifier|public
specifier|static
name|long
name|bytesToLong
parameter_list|(
name|byte
index|[]
name|bytes
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
name|bytes
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
operator|-
literal|1L
return|;
block|}
return|return
operator|(
operator|(
name|LongWritable
operator|)
name|getWritable
argument_list|(
name|bytes
argument_list|,
operator|new
name|LongWritable
argument_list|()
argument_list|)
operator|)
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * Converts a string to a byte array in a consistent manner.    * @param s    * @return the byte array    * @throws UnsupportedEncodingException    */
specifier|public
specifier|static
name|byte
index|[]
name|stringToBytes
parameter_list|(
name|String
name|s
parameter_list|)
throws|throws
name|UnsupportedEncodingException
block|{
if|if
condition|(
name|s
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"string cannot be null"
argument_list|)
throw|;
block|}
return|return
name|s
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
return|;
block|}
comment|/**    * Converts a byte array to a string in a consistent manner.    * @param bytes    * @return the string    * @throws UnsupportedEncodingException    */
specifier|public
specifier|static
name|String
name|bytesToString
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
throws|throws
name|UnsupportedEncodingException
block|{
if|if
condition|(
name|bytes
operator|==
literal|null
operator|||
name|bytes
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|""
return|;
block|}
return|return
operator|new
name|String
argument_list|(
name|bytes
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
return|;
block|}
block|}
end_class

end_unit

