begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|prefixtree
package|;
end_package

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
name|KeyValue
operator|.
name|KeyComparator
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
operator|.
name|MetaKeyComparator
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
name|codec
operator|.
name|prefixtree
operator|.
name|decode
operator|.
name|DecoderFactory
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
name|codec
operator|.
name|prefixtree
operator|.
name|decode
operator|.
name|PrefixTreeArraySearcher
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
name|codec
operator|.
name|prefixtree
operator|.
name|encode
operator|.
name|EncoderFactory
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
name|codec
operator|.
name|prefixtree
operator|.
name|encode
operator|.
name|PrefixTreeEncoder
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
name|codec
operator|.
name|prefixtree
operator|.
name|scanner
operator|.
name|CellSearcher
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
name|compress
operator|.
name|Compression
operator|.
name|Algorithm
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
name|encoding
operator|.
name|DataBlockEncoder
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
name|encoding
operator|.
name|DataBlockEncoding
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
name|encoding
operator|.
name|HFileBlockDecodingContext
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
name|encoding
operator|.
name|HFileBlockDefaultDecodingContext
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
name|encoding
operator|.
name|HFileBlockDefaultEncodingContext
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
name|encoding
operator|.
name|HFileBlockEncodingContext
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
name|hfile
operator|.
name|BlockType
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
name|io
operator|.
name|RawComparator
import|;
end_import

begin_comment
comment|/**  * This class is created via reflection in DataBlockEncoding enum. Update the enum if class name or  * package changes.  *<p/>  * PrefixTreeDataBlockEncoder implementation of DataBlockEncoder. This is the primary entry point  * for PrefixTree encoding and decoding. Encoding is delegated to instances of  * {@link PrefixTreeEncoder}, and decoding is delegated to instances of  * {@link org.apache.hadoop.hbase.codec.prefixtree.scanner.CellSearcher}. Encoder and decoder instances are  * created and recycled by static PtEncoderFactory and PtDecoderFactory.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|PrefixTreeCodec
implements|implements
name|DataBlockEncoder
block|{
comment|/**    * no-arg constructor for reflection    */
specifier|public
name|PrefixTreeCodec
parameter_list|()
block|{   }
comment|/**    * Copied from BufferedDataBlockEncoder. Almost definitely can be improved, but i'm not familiar    * enough with the concept of the HFileBlockEncodingContext.    */
annotation|@
name|Override
specifier|public
name|void
name|encodeKeyValues
parameter_list|(
name|ByteBuffer
name|in
parameter_list|,
name|boolean
name|includesMvccVersion
parameter_list|,
name|HFileBlockEncodingContext
name|blkEncodingCtx
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|blkEncodingCtx
operator|.
name|getClass
argument_list|()
operator|!=
name|HFileBlockDefaultEncodingContext
operator|.
name|class
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" only accepts "
operator|+
name|HFileBlockDefaultEncodingContext
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|" as the "
operator|+
literal|"encoding context."
argument_list|)
throw|;
block|}
name|HFileBlockDefaultEncodingContext
name|encodingCtx
init|=
operator|(
name|HFileBlockDefaultEncodingContext
operator|)
name|blkEncodingCtx
decl_stmt|;
name|encodingCtx
operator|.
name|prepareEncoding
argument_list|()
expr_stmt|;
name|DataOutputStream
name|dataOut
init|=
name|encodingCtx
operator|.
name|getOutputStreamForEncoder
argument_list|()
decl_stmt|;
name|internalEncodeKeyValues
argument_list|(
name|dataOut
argument_list|,
name|in
argument_list|,
name|includesMvccVersion
argument_list|)
expr_stmt|;
comment|//do i need to check this, or will it always be DataBlockEncoding.PREFIX_TREE?
if|if
condition|(
name|encodingCtx
operator|.
name|getDataBlockEncoding
argument_list|()
operator|!=
name|DataBlockEncoding
operator|.
name|NONE
condition|)
block|{
name|encodingCtx
operator|.
name|postEncoding
argument_list|(
name|BlockType
operator|.
name|ENCODED_DATA
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|encodingCtx
operator|.
name|postEncoding
argument_list|(
name|BlockType
operator|.
name|DATA
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|internalEncodeKeyValues
parameter_list|(
name|DataOutputStream
name|encodedOutputStream
parameter_list|,
name|ByteBuffer
name|rawKeyValues
parameter_list|,
name|boolean
name|includesMvccVersion
parameter_list|)
throws|throws
name|IOException
block|{
name|rawKeyValues
operator|.
name|rewind
argument_list|()
expr_stmt|;
name|PrefixTreeEncoder
name|builder
init|=
name|EncoderFactory
operator|.
name|checkOut
argument_list|(
name|encodedOutputStream
argument_list|,
name|includesMvccVersion
argument_list|)
decl_stmt|;
try|try
block|{
name|KeyValue
name|kv
decl_stmt|;
while|while
condition|(
operator|(
name|kv
operator|=
name|KeyValueUtil
operator|.
name|nextShallowCopy
argument_list|(
name|rawKeyValues
argument_list|,
name|includesMvccVersion
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|write
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|EncoderFactory
operator|.
name|checkIn
argument_list|(
name|builder
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|decodeKeyValues
parameter_list|(
name|DataInputStream
name|source
parameter_list|,
name|boolean
name|includesMvccVersion
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|decodeKeyValues
argument_list|(
name|source
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|includesMvccVersion
argument_list|)
return|;
block|}
comment|/**    * I don't think this method is called during normal HBase operation, so efficiency is not    * important.    */
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|decodeKeyValues
parameter_list|(
name|DataInputStream
name|source
parameter_list|,
name|int
name|allocateHeaderLength
parameter_list|,
name|int
name|skipLastBytes
parameter_list|,
name|boolean
name|includesMvccVersion
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteBuffer
name|sourceAsBuffer
init|=
name|ByteBufferUtils
operator|.
name|drainInputStreamToBuffer
argument_list|(
name|source
argument_list|)
decl_stmt|;
comment|// waste
name|sourceAsBuffer
operator|.
name|mark
argument_list|()
expr_stmt|;
name|PrefixTreeBlockMeta
name|blockMeta
init|=
operator|new
name|PrefixTreeBlockMeta
argument_list|(
name|sourceAsBuffer
argument_list|)
decl_stmt|;
name|sourceAsBuffer
operator|.
name|rewind
argument_list|()
expr_stmt|;
name|int
name|numV1BytesWithHeader
init|=
name|allocateHeaderLength
operator|+
name|blockMeta
operator|.
name|getNumKeyValueBytes
argument_list|()
decl_stmt|;
name|byte
index|[]
name|keyValueBytesWithHeader
init|=
operator|new
name|byte
index|[
name|numV1BytesWithHeader
index|]
decl_stmt|;
name|ByteBuffer
name|result
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|keyValueBytesWithHeader
argument_list|)
decl_stmt|;
name|result
operator|.
name|rewind
argument_list|()
expr_stmt|;
name|CellSearcher
name|searcher
init|=
literal|null
decl_stmt|;
try|try
block|{
name|searcher
operator|=
name|DecoderFactory
operator|.
name|checkOut
argument_list|(
name|sourceAsBuffer
argument_list|,
name|includesMvccVersion
argument_list|)
expr_stmt|;
while|while
condition|(
name|searcher
operator|.
name|advance
argument_list|()
condition|)
block|{
name|KeyValue
name|currentCell
init|=
name|KeyValueUtil
operator|.
name|copyToNewKeyValue
argument_list|(
name|searcher
operator|.
name|current
argument_list|()
argument_list|)
decl_stmt|;
comment|// needs to be modified for DirectByteBuffers. no existing methods to
comment|// write VLongs to byte[]
name|int
name|offset
init|=
name|result
operator|.
name|arrayOffset
argument_list|()
operator|+
name|result
operator|.
name|position
argument_list|()
decl_stmt|;
name|KeyValueUtil
operator|.
name|appendToByteArray
argument_list|(
name|currentCell
argument_list|,
name|result
operator|.
name|array
argument_list|()
argument_list|,
name|offset
argument_list|)
expr_stmt|;
name|int
name|keyValueLength
init|=
name|KeyValueUtil
operator|.
name|length
argument_list|(
name|currentCell
argument_list|)
decl_stmt|;
name|ByteBufferUtils
operator|.
name|skip
argument_list|(
name|result
argument_list|,
name|keyValueLength
argument_list|)
expr_stmt|;
name|offset
operator|+=
name|keyValueLength
expr_stmt|;
if|if
condition|(
name|includesMvccVersion
condition|)
block|{
name|ByteBufferUtils
operator|.
name|writeVLong
argument_list|(
name|result
argument_list|,
name|currentCell
operator|.
name|getMvccVersion
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|result
operator|.
name|position
argument_list|(
name|result
operator|.
name|limit
argument_list|()
argument_list|)
expr_stmt|;
comment|//make it appear as if we were appending
return|return
name|result
return|;
block|}
finally|finally
block|{
name|DecoderFactory
operator|.
name|checkIn
argument_list|(
name|searcher
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getFirstKeyInBlock
parameter_list|(
name|ByteBuffer
name|block
parameter_list|)
block|{
name|block
operator|.
name|rewind
argument_list|()
expr_stmt|;
name|PrefixTreeArraySearcher
name|searcher
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|//should i includeMemstoreTS (second argument)?  i think PrefixKeyDeltaEncoder is, so i will
name|searcher
operator|=
name|DecoderFactory
operator|.
name|checkOut
argument_list|(
name|block
argument_list|,
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|searcher
operator|.
name|positionAtFirstCell
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|KeyValueUtil
operator|.
name|copyKeyToNewByteBuffer
argument_list|(
name|searcher
operator|.
name|current
argument_list|()
argument_list|)
return|;
block|}
finally|finally
block|{
name|DecoderFactory
operator|.
name|checkIn
argument_list|(
name|searcher
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|HFileBlockEncodingContext
name|newDataBlockEncodingContext
parameter_list|(
name|Algorithm
name|compressionAlgorithm
parameter_list|,
name|DataBlockEncoding
name|encoding
parameter_list|,
name|byte
index|[]
name|header
parameter_list|)
block|{
if|if
condition|(
name|DataBlockEncoding
operator|.
name|PREFIX_TREE
operator|!=
name|encoding
condition|)
block|{
comment|//i'm not sure why encoding is in the interface.  Each encoder implementation should probably
comment|//know it's encoding type
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"only DataBlockEncoding.PREFIX_TREE supported"
argument_list|)
throw|;
block|}
return|return
operator|new
name|HFileBlockDefaultEncodingContext
argument_list|(
name|compressionAlgorithm
argument_list|,
name|encoding
argument_list|,
name|header
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|HFileBlockDecodingContext
name|newDataBlockDecodingContext
parameter_list|(
name|Algorithm
name|compressionAlgorithm
parameter_list|)
block|{
return|return
operator|new
name|HFileBlockDefaultDecodingContext
argument_list|(
name|compressionAlgorithm
argument_list|)
return|;
block|}
comment|/**    * Is this the correct handling of an illegal comparator?  How to prevent that from getting all    * the way to this point.    */
annotation|@
name|Override
specifier|public
name|EncodedSeeker
name|createSeeker
parameter_list|(
name|RawComparator
argument_list|<
name|byte
index|[]
argument_list|>
name|comparator
parameter_list|,
name|boolean
name|includesMvccVersion
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|comparator
operator|instanceof
name|KeyComparator
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"comparator must be KeyValue.KeyComparator"
argument_list|)
throw|;
block|}
if|if
condition|(
name|comparator
operator|instanceof
name|MetaKeyComparator
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"DataBlockEncoding.PREFIX_TREE not compatible with META "
operator|+
literal|"table"
argument_list|)
throw|;
block|}
return|return
operator|new
name|PrefixTreeSeeker
argument_list|(
name|includesMvccVersion
argument_list|)
return|;
block|}
block|}
end_class

end_unit

