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
name|codec
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
name|hadoop
operator|.
name|hbase
operator|.
name|Cell
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
name|HBaseInterfaceAudience
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
name|KeyValueUtil
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
name|NoTagsByteBufferKeyValue
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
name|NoTagsKeyValue
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Codec that does KeyValue version 1 serialization.  *  *<p>Encodes Cell as serialized in KeyValue with total length prefix.  * This is how KVs were serialized in Puts, Deletes and Results pre-0.96.  Its what would  * happen if you called the Writable#write KeyValue implementation.  This encoder will fail  * if the passed Cell is not an old-school pre-0.96 KeyValue.  Does not copy bytes writing.  * It just writes them direct to the passed stream.  *  *<p>If you wrote two KeyValues to this encoder, it would look like this in the stream:  *<pre>  * length-of-KeyValue1 // A java int with the length of KeyValue1 backing array  * KeyValue1 backing array filled with a KeyValue serialized in its particular format  * length-of-KeyValue2  * KeyValue2 backing array  *</pre>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|CONFIG
argument_list|)
specifier|public
class|class
name|KeyValueCodec
implements|implements
name|Codec
block|{
specifier|public
specifier|static
class|class
name|KeyValueEncoder
extends|extends
name|BaseEncoder
block|{
specifier|public
name|KeyValueEncoder
parameter_list|(
specifier|final
name|OutputStream
name|out
parameter_list|)
block|{
name|super
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
name|checkFlushed
argument_list|()
expr_stmt|;
comment|// Do not write tags over RPC
name|ByteBufferUtils
operator|.
name|putInt
argument_list|(
name|this
operator|.
name|out
argument_list|,
name|KeyValueUtil
operator|.
name|getSerializedSize
argument_list|(
name|cell
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|KeyValueUtil
operator|.
name|oswrite
argument_list|(
name|cell
argument_list|,
name|out
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|KeyValueDecoder
extends|extends
name|BaseDecoder
block|{
specifier|public
name|KeyValueDecoder
parameter_list|(
specifier|final
name|InputStream
name|in
parameter_list|)
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Cell
name|parseCell
parameter_list|()
throws|throws
name|IOException
block|{
comment|// No tags here
return|return
name|KeyValueUtil
operator|.
name|createKeyValueFromInputStream
argument_list|(
name|in
argument_list|,
literal|false
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|ByteBuffKeyValueDecoder
implements|implements
name|Codec
operator|.
name|Decoder
block|{
specifier|protected
specifier|final
name|ByteBuff
name|buf
decl_stmt|;
specifier|protected
name|Cell
name|current
init|=
literal|null
decl_stmt|;
specifier|public
name|ByteBuffKeyValueDecoder
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
annotation|@
name|Override
specifier|public
name|boolean
name|advance
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|buf
operator|.
name|hasRemaining
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|int
name|len
init|=
name|buf
operator|.
name|getInt
argument_list|()
decl_stmt|;
name|ByteBuffer
name|bb
init|=
name|buf
operator|.
name|asSubByteBuffer
argument_list|(
name|len
argument_list|)
decl_stmt|;
if|if
condition|(
name|bb
operator|.
name|isDirect
argument_list|()
condition|)
block|{
name|this
operator|.
name|current
operator|=
name|createCell
argument_list|(
name|bb
argument_list|,
name|bb
operator|.
name|position
argument_list|()
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|current
operator|=
name|createCell
argument_list|(
name|bb
operator|.
name|array
argument_list|()
argument_list|,
name|bb
operator|.
name|arrayOffset
argument_list|()
operator|+
name|bb
operator|.
name|position
argument_list|()
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
name|buf
operator|.
name|skip
argument_list|(
name|len
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|current
parameter_list|()
block|{
return|return
name|this
operator|.
name|current
return|;
block|}
specifier|protected
name|Cell
name|createCell
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|len
parameter_list|)
block|{
return|return
operator|new
name|NoTagsKeyValue
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|,
name|len
argument_list|)
return|;
block|}
specifier|protected
name|Cell
name|createCell
parameter_list|(
name|ByteBuffer
name|bb
parameter_list|,
name|int
name|pos
parameter_list|,
name|int
name|len
parameter_list|)
block|{
comment|// We know there is not going to be any tags.
return|return
operator|new
name|NoTagsByteBufferKeyValue
argument_list|(
name|bb
argument_list|,
name|pos
argument_list|,
name|len
argument_list|)
return|;
block|}
block|}
comment|/**    * Implementation depends on {@link InputStream#available()}    */
annotation|@
name|Override
specifier|public
name|Decoder
name|getDecoder
parameter_list|(
specifier|final
name|InputStream
name|is
parameter_list|)
block|{
return|return
operator|new
name|KeyValueDecoder
argument_list|(
name|is
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Decoder
name|getDecoder
parameter_list|(
name|ByteBuff
name|buf
parameter_list|)
block|{
return|return
operator|new
name|ByteBuffKeyValueDecoder
argument_list|(
name|buf
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Encoder
name|getEncoder
parameter_list|(
name|OutputStream
name|os
parameter_list|)
block|{
return|return
operator|new
name|KeyValueEncoder
argument_list|(
name|os
argument_list|)
return|;
block|}
block|}
end_class

end_unit

