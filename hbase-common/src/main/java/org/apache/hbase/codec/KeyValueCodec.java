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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|KeyValue
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
name|hbase
operator|.
name|Cell
import|;
end_import

begin_comment
comment|/**  * Codec that does KeyValue version 1 serialization.  *   *<p>Encodes by casting Cell to KeyValue and writing out the backing array with a length prefix.  * This is how KVs were serialized in Puts, Deletes and Results pre-0.96.  Its what would  * happen if you called the Writable#write KeyValue implementation.  This encoder will fail  * if the passed Cell is not an old-school pre-0.96 KeyValue.  Does not copy bytes writing.  * It just writes them direct to the passed stream.  *  *<p>If you wrote two KeyValues to this encoder, it would look like this in the stream:  *<pre>  * length-of-KeyValue1 // A java int with the length of KeyValue1 backing array  * KeyValue1 backing array filled with a KeyValue serialized in its particular format  * length-of-KeyValue2  * KeyValue2 backing array  *</pre>  */
end_comment

begin_class
specifier|public
class|class
name|KeyValueCodec
implements|implements
name|Codec
block|{
specifier|static
class|class
name|KeyValueEncoder
extends|extends
name|BaseEncoder
block|{
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
comment|// This is crass and will not work when KV changes. Also if passed a non-kv Cell, it will
comment|// make expensive copy.
try|try
block|{
name|KeyValue
operator|.
name|oswrite
argument_list|(
operator|(
name|KeyValue
operator|)
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|cell
argument_list|)
argument_list|,
name|this
operator|.
name|out
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|CodecException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|static
class|class
name|KeyValueDecoder
extends|extends
name|BaseDecoder
block|{
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
name|Cell
name|parseCell
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|KeyValue
operator|.
name|iscreate
argument_list|(
name|in
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

