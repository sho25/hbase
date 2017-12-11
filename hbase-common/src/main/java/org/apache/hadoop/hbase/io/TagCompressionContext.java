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
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
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
name|hadoop
operator|.
name|hbase
operator|.
name|Tag
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
name|io
operator|.
name|util
operator|.
name|Dictionary
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
name|io
operator|.
name|util
operator|.
name|StreamUtils
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
name|IOUtils
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
comment|/**  * Context that holds the dictionary for Tag compression and doing the compress/uncompress. This  * will be used for compressing tags while writing into HFiles and WALs.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|TagCompressionContext
block|{
specifier|private
specifier|final
name|Dictionary
name|tagDict
decl_stmt|;
specifier|public
name|TagCompressionContext
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Dictionary
argument_list|>
name|dictType
parameter_list|,
name|int
name|dictCapacity
parameter_list|)
throws|throws
name|SecurityException
throws|,
name|NoSuchMethodException
throws|,
name|InstantiationException
throws|,
name|IllegalAccessException
throws|,
name|InvocationTargetException
block|{
name|Constructor
argument_list|<
name|?
extends|extends
name|Dictionary
argument_list|>
name|dictConstructor
init|=
name|dictType
operator|.
name|getConstructor
argument_list|()
decl_stmt|;
name|tagDict
operator|=
name|dictConstructor
operator|.
name|newInstance
argument_list|()
expr_stmt|;
name|tagDict
operator|.
name|init
argument_list|(
name|dictCapacity
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|tagDict
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|/**    * Compress tags one by one and writes to the OutputStream.    * @param out Stream to which the compressed tags to be written    * @param in Source where tags are available    * @param offset Offset for the tags bytes    * @param length Length of all tag bytes    * @throws IOException    */
specifier|public
name|void
name|compressTags
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|byte
index|[]
name|in
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|pos
init|=
name|offset
decl_stmt|;
name|int
name|endOffset
init|=
name|pos
operator|+
name|length
decl_stmt|;
assert|assert
name|pos
operator|<
name|endOffset
assert|;
while|while
condition|(
name|pos
operator|<
name|endOffset
condition|)
block|{
name|int
name|tagLen
init|=
name|Bytes
operator|.
name|readAsInt
argument_list|(
name|in
argument_list|,
name|pos
argument_list|,
name|Tag
operator|.
name|TAG_LENGTH_SIZE
argument_list|)
decl_stmt|;
name|pos
operator|+=
name|Tag
operator|.
name|TAG_LENGTH_SIZE
expr_stmt|;
name|Dictionary
operator|.
name|write
argument_list|(
name|out
argument_list|,
name|in
argument_list|,
name|pos
argument_list|,
name|tagLen
argument_list|,
name|tagDict
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|tagLen
expr_stmt|;
block|}
block|}
comment|/**    * Compress tags one by one and writes to the OutputStream.    * @param out Stream to which the compressed tags to be written    * @param in Source buffer where tags are available    * @param offset Offset for the tags byte buffer    * @param length Length of all tag bytes    * @throws IOException    */
specifier|public
name|void
name|compressTags
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|ByteBuffer
name|in
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|in
operator|.
name|hasArray
argument_list|()
condition|)
block|{
name|compressTags
argument_list|(
name|out
argument_list|,
name|in
operator|.
name|array
argument_list|()
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|pos
init|=
name|offset
decl_stmt|;
name|int
name|endOffset
init|=
name|pos
operator|+
name|length
decl_stmt|;
assert|assert
name|pos
operator|<
name|endOffset
assert|;
while|while
condition|(
name|pos
operator|<
name|endOffset
condition|)
block|{
name|int
name|tagLen
init|=
name|ByteBufferUtils
operator|.
name|readAsInt
argument_list|(
name|in
argument_list|,
name|pos
argument_list|,
name|Tag
operator|.
name|TAG_LENGTH_SIZE
argument_list|)
decl_stmt|;
name|pos
operator|+=
name|Tag
operator|.
name|TAG_LENGTH_SIZE
expr_stmt|;
name|Dictionary
operator|.
name|write
argument_list|(
name|out
argument_list|,
name|in
argument_list|,
name|pos
argument_list|,
name|tagLen
argument_list|,
name|tagDict
argument_list|)
expr_stmt|;
empty_stmt|;
name|pos
operator|+=
name|tagLen
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Uncompress tags from the InputStream and writes to the destination array.    * @param src Stream where the compressed tags are available    * @param dest Destination array where to write the uncompressed tags    * @param offset Offset in destination where tags to be written    * @param length Length of all tag bytes    * @throws IOException    */
specifier|public
name|void
name|uncompressTags
parameter_list|(
name|InputStream
name|src
parameter_list|,
name|byte
index|[]
name|dest
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|endOffset
init|=
name|offset
operator|+
name|length
decl_stmt|;
while|while
condition|(
name|offset
operator|<
name|endOffset
condition|)
block|{
name|byte
name|status
init|=
operator|(
name|byte
operator|)
name|src
operator|.
name|read
argument_list|()
decl_stmt|;
if|if
condition|(
name|status
operator|==
name|Dictionary
operator|.
name|NOT_IN_DICTIONARY
condition|)
block|{
name|int
name|tagLen
init|=
name|StreamUtils
operator|.
name|readRawVarint32
argument_list|(
name|src
argument_list|)
decl_stmt|;
name|offset
operator|=
name|Bytes
operator|.
name|putAsShort
argument_list|(
name|dest
argument_list|,
name|offset
argument_list|,
name|tagLen
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|readFully
argument_list|(
name|src
argument_list|,
name|dest
argument_list|,
name|offset
argument_list|,
name|tagLen
argument_list|)
expr_stmt|;
name|tagDict
operator|.
name|addEntry
argument_list|(
name|dest
argument_list|,
name|offset
argument_list|,
name|tagLen
argument_list|)
expr_stmt|;
name|offset
operator|+=
name|tagLen
expr_stmt|;
block|}
else|else
block|{
name|short
name|dictIdx
init|=
name|StreamUtils
operator|.
name|toShort
argument_list|(
name|status
argument_list|,
operator|(
name|byte
operator|)
name|src
operator|.
name|read
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|entry
init|=
name|tagDict
operator|.
name|getEntry
argument_list|(
name|dictIdx
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Missing dictionary entry for index "
operator|+
name|dictIdx
argument_list|)
throw|;
block|}
name|offset
operator|=
name|Bytes
operator|.
name|putAsShort
argument_list|(
name|dest
argument_list|,
name|offset
argument_list|,
name|entry
operator|.
name|length
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|entry
argument_list|,
literal|0
argument_list|,
name|dest
argument_list|,
name|offset
argument_list|,
name|entry
operator|.
name|length
argument_list|)
expr_stmt|;
name|offset
operator|+=
name|entry
operator|.
name|length
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Uncompress tags from the input ByteBuffer and writes to the destination array.    * @param src Buffer where the compressed tags are available    * @param dest Destination array where to write the uncompressed tags    * @param offset Offset in destination where tags to be written    * @param length Length of all tag bytes    * @return bytes count read from source to uncompress all tags.    * @throws IOException    */
specifier|public
name|int
name|uncompressTags
parameter_list|(
name|ByteBuff
name|src
parameter_list|,
name|byte
index|[]
name|dest
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|srcBeginPos
init|=
name|src
operator|.
name|position
argument_list|()
decl_stmt|;
name|int
name|endOffset
init|=
name|offset
operator|+
name|length
decl_stmt|;
while|while
condition|(
name|offset
operator|<
name|endOffset
condition|)
block|{
name|byte
name|status
init|=
name|src
operator|.
name|get
argument_list|()
decl_stmt|;
name|int
name|tagLen
decl_stmt|;
if|if
condition|(
name|status
operator|==
name|Dictionary
operator|.
name|NOT_IN_DICTIONARY
condition|)
block|{
name|tagLen
operator|=
name|StreamUtils
operator|.
name|readRawVarint32
argument_list|(
name|src
argument_list|)
expr_stmt|;
name|offset
operator|=
name|Bytes
operator|.
name|putAsShort
argument_list|(
name|dest
argument_list|,
name|offset
argument_list|,
name|tagLen
argument_list|)
expr_stmt|;
name|src
operator|.
name|get
argument_list|(
name|dest
argument_list|,
name|offset
argument_list|,
name|tagLen
argument_list|)
expr_stmt|;
name|tagDict
operator|.
name|addEntry
argument_list|(
name|dest
argument_list|,
name|offset
argument_list|,
name|tagLen
argument_list|)
expr_stmt|;
name|offset
operator|+=
name|tagLen
expr_stmt|;
block|}
else|else
block|{
name|short
name|dictIdx
init|=
name|StreamUtils
operator|.
name|toShort
argument_list|(
name|status
argument_list|,
name|src
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|entry
init|=
name|tagDict
operator|.
name|getEntry
argument_list|(
name|dictIdx
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Missing dictionary entry for index "
operator|+
name|dictIdx
argument_list|)
throw|;
block|}
name|tagLen
operator|=
name|entry
operator|.
name|length
expr_stmt|;
name|offset
operator|=
name|Bytes
operator|.
name|putAsShort
argument_list|(
name|dest
argument_list|,
name|offset
argument_list|,
name|tagLen
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|entry
argument_list|,
literal|0
argument_list|,
name|dest
argument_list|,
name|offset
argument_list|,
name|tagLen
argument_list|)
expr_stmt|;
name|offset
operator|+=
name|tagLen
expr_stmt|;
block|}
block|}
return|return
name|src
operator|.
name|position
argument_list|()
operator|-
name|srcBeginPos
return|;
block|}
comment|/**    * Uncompress tags from the InputStream and writes to the destination buffer.    * @param src Stream where the compressed tags are available    * @param dest Destination buffer where to write the uncompressed tags    * @param length Length of all tag bytes    * @throws IOException when the dictionary does not have the entry    */
specifier|public
name|void
name|uncompressTags
parameter_list|(
name|InputStream
name|src
parameter_list|,
name|ByteBuffer
name|dest
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|dest
operator|.
name|hasArray
argument_list|()
condition|)
block|{
name|uncompressTags
argument_list|(
name|src
argument_list|,
name|dest
operator|.
name|array
argument_list|()
argument_list|,
name|dest
operator|.
name|arrayOffset
argument_list|()
operator|+
name|dest
operator|.
name|position
argument_list|()
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|byte
index|[]
name|tagBuf
init|=
operator|new
name|byte
index|[
name|length
index|]
decl_stmt|;
name|uncompressTags
argument_list|(
name|src
argument_list|,
name|tagBuf
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|dest
operator|.
name|put
argument_list|(
name|tagBuf
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

