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
name|regionserver
operator|.
name|wal
package|;
end_package

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
name|conf
operator|.
name|Configuration
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
name|PrivateCellUtil
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
name|hbase
operator|.
name|codec
operator|.
name|BaseDecoder
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
name|BaseEncoder
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
name|Codec
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
name|KeyValueCodecWithTags
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
name|ByteBuffInputStream
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
name|ByteBufferWriter
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
name|ByteBufferWriterOutputStream
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
name|hbase
operator|.
name|util
operator|.
name|ReflectionUtils
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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
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
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|UnsafeByteOperations
import|;
end_import

begin_comment
comment|/**  * Compression in this class is lifted off Compressor/KeyValueCompression.  * This is a pure coincidence... they are independent and don't have to be compatible.  *  * This codec is used at server side for writing cells to WAL as well as for sending edits  * as part of the distributed splitting process.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
block|{
name|HBaseInterfaceAudience
operator|.
name|COPROC
block|,
name|HBaseInterfaceAudience
operator|.
name|PHOENIX
block|,
name|HBaseInterfaceAudience
operator|.
name|CONFIG
block|}
argument_list|)
specifier|public
class|class
name|WALCellCodec
implements|implements
name|Codec
block|{
comment|/** Configuration key for the class to use when encoding cells in the WAL */
specifier|public
specifier|static
specifier|final
name|String
name|WAL_CELL_CODEC_CLASS_KEY
init|=
literal|"hbase.regionserver.wal.codec"
decl_stmt|;
specifier|protected
specifier|final
name|CompressionContext
name|compression
decl_stmt|;
comment|/**    *<b>All subclasses must implement a no argument constructor</b>    */
specifier|public
name|WALCellCodec
parameter_list|()
block|{
name|this
operator|.
name|compression
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Default constructor -<b>all subclasses must implement a constructor with this signature</b>    * if they are to be dynamically loaded from the {@link Configuration}.    * @param conf configuration to configure<tt>this</tt>    * @param compression compression the codec should support, can be<tt>null</tt> to indicate no    *          compression    */
specifier|public
name|WALCellCodec
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|CompressionContext
name|compression
parameter_list|)
block|{
name|this
operator|.
name|compression
operator|=
name|compression
expr_stmt|;
block|}
specifier|public
specifier|static
name|Class
argument_list|<
name|?
argument_list|>
name|getWALCellCodecClass
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getClass
argument_list|(
name|WAL_CELL_CODEC_CLASS_KEY
argument_list|,
name|WALCellCodec
operator|.
name|class
argument_list|)
return|;
block|}
comment|/**    * Create and setup a {@link WALCellCodec} from the {@code cellCodecClsName} and    * CompressionContext, if {@code cellCodecClsName} is specified.    * Otherwise Cell Codec classname is read from {@link Configuration}.    * Fully prepares the codec for use.    * @param conf {@link Configuration} to read for the user-specified codec. If none is specified,    *          uses a {@link WALCellCodec}.    * @param cellCodecClsName name of codec    * @param compression compression the codec should use    * @return a {@link WALCellCodec} ready for use.    * @throws UnsupportedOperationException if the codec cannot be instantiated    */
specifier|public
specifier|static
name|WALCellCodec
name|create
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|cellCodecClsName
parameter_list|,
name|CompressionContext
name|compression
parameter_list|)
throws|throws
name|UnsupportedOperationException
block|{
if|if
condition|(
name|cellCodecClsName
operator|==
literal|null
condition|)
block|{
name|cellCodecClsName
operator|=
name|getWALCellCodecClass
argument_list|(
name|conf
argument_list|)
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
return|return
name|ReflectionUtils
operator|.
name|instantiateWithCustomCtor
argument_list|(
name|cellCodecClsName
argument_list|,
operator|new
name|Class
index|[]
block|{
name|Configuration
operator|.
name|class
block|,
name|CompressionContext
operator|.
name|class
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
name|conf
block|,
name|compression
block|}
argument_list|)
return|;
block|}
comment|/**    * Create and setup a {@link WALCellCodec} from the    * CompressionContext.    * Cell Codec classname is read from {@link Configuration}.    * Fully prepares the codec for use.    * @param conf {@link Configuration} to read for the user-specified codec. If none is specified,    *          uses a {@link WALCellCodec}.    * @param compression compression the codec should use    * @return a {@link WALCellCodec} ready for use.    * @throws UnsupportedOperationException if the codec cannot be instantiated    */
specifier|public
specifier|static
name|WALCellCodec
name|create
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|CompressionContext
name|compression
parameter_list|)
throws|throws
name|UnsupportedOperationException
block|{
name|String
name|cellCodecClsName
init|=
name|getWALCellCodecClass
argument_list|(
name|conf
argument_list|)
operator|.
name|getName
argument_list|()
decl_stmt|;
return|return
name|ReflectionUtils
operator|.
name|instantiateWithCustomCtor
argument_list|(
name|cellCodecClsName
argument_list|,
operator|new
name|Class
index|[]
block|{
name|Configuration
operator|.
name|class
block|,
name|CompressionContext
operator|.
name|class
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
name|conf
block|,
name|compression
block|}
argument_list|)
return|;
block|}
specifier|public
interface|interface
name|ByteStringCompressor
block|{
name|ByteString
name|compress
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|Enum
name|dictIndex
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
specifier|public
interface|interface
name|ByteStringUncompressor
block|{
name|byte
index|[]
name|uncompress
parameter_list|(
name|ByteString
name|data
parameter_list|,
name|Enum
name|dictIndex
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
specifier|static
class|class
name|StatelessUncompressor
implements|implements
name|ByteStringUncompressor
block|{
name|CompressionContext
name|compressionContext
decl_stmt|;
specifier|public
name|StatelessUncompressor
parameter_list|(
name|CompressionContext
name|compressionContext
parameter_list|)
block|{
name|this
operator|.
name|compressionContext
operator|=
name|compressionContext
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|uncompress
parameter_list|(
name|ByteString
name|data
parameter_list|,
name|Enum
name|dictIndex
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|WALCellCodec
operator|.
name|uncompressByteString
argument_list|(
name|data
argument_list|,
name|compressionContext
operator|.
name|getDictionary
argument_list|(
name|dictIndex
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|static
class|class
name|BaosAndCompressor
extends|extends
name|ByteArrayOutputStream
implements|implements
name|ByteStringCompressor
block|{
specifier|private
name|CompressionContext
name|compressionContext
decl_stmt|;
specifier|public
name|BaosAndCompressor
parameter_list|(
name|CompressionContext
name|compressionContext
parameter_list|)
block|{
name|this
operator|.
name|compressionContext
operator|=
name|compressionContext
expr_stmt|;
block|}
specifier|public
name|ByteString
name|toByteString
parameter_list|()
block|{
comment|// We need this copy to create the ByteString as the byte[] 'buf' is not immutable. We reuse
comment|// them.
return|return
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|this
operator|.
name|buf
argument_list|,
literal|0
argument_list|,
name|this
operator|.
name|count
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteString
name|compress
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|Enum
name|dictIndex
parameter_list|)
throws|throws
name|IOException
block|{
name|writeCompressed
argument_list|(
name|data
argument_list|,
name|dictIndex
argument_list|)
expr_stmt|;
comment|// We need this copy to create the ByteString as the byte[] 'buf' is not immutable. We reuse
comment|// them.
name|ByteString
name|result
init|=
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|this
operator|.
name|buf
argument_list|,
literal|0
argument_list|,
name|this
operator|.
name|count
argument_list|)
decl_stmt|;
name|reset
argument_list|()
expr_stmt|;
comment|// Only resets the count - we reuse the byte array.
return|return
name|result
return|;
block|}
specifier|private
name|void
name|writeCompressed
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|Enum
name|dictIndex
parameter_list|)
throws|throws
name|IOException
block|{
name|Dictionary
name|dict
init|=
name|compressionContext
operator|.
name|getDictionary
argument_list|(
name|dictIndex
argument_list|)
decl_stmt|;
assert|assert
name|dict
operator|!=
literal|null
assert|;
name|short
name|dictIdx
init|=
name|dict
operator|.
name|findEntry
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
name|dictIdx
operator|==
name|Dictionary
operator|.
name|NOT_IN_DICTIONARY
condition|)
block|{
name|write
argument_list|(
name|Dictionary
operator|.
name|NOT_IN_DICTIONARY
argument_list|)
expr_stmt|;
name|StreamUtils
operator|.
name|writeRawVInt32
argument_list|(
name|this
argument_list|,
name|data
operator|.
name|length
argument_list|)
expr_stmt|;
name|write
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|StreamUtils
operator|.
name|writeShort
argument_list|(
name|this
argument_list|,
name|dictIdx
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|static
class|class
name|NoneCompressor
implements|implements
name|ByteStringCompressor
block|{
annotation|@
name|Override
specifier|public
name|ByteString
name|compress
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|Enum
name|dictIndex
parameter_list|)
block|{
return|return
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|data
argument_list|)
return|;
block|}
block|}
specifier|static
class|class
name|NoneUncompressor
implements|implements
name|ByteStringUncompressor
block|{
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|uncompress
parameter_list|(
name|ByteString
name|data
parameter_list|,
name|Enum
name|dictIndex
parameter_list|)
block|{
return|return
name|data
operator|.
name|toByteArray
argument_list|()
return|;
block|}
block|}
specifier|private
specifier|static
name|byte
index|[]
name|uncompressByteString
parameter_list|(
name|ByteString
name|bs
parameter_list|,
name|Dictionary
name|dict
parameter_list|)
throws|throws
name|IOException
block|{
name|InputStream
name|in
init|=
name|bs
operator|.
name|newInput
argument_list|()
decl_stmt|;
name|byte
name|status
init|=
operator|(
name|byte
operator|)
name|in
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
name|byte
index|[]
name|arr
init|=
operator|new
name|byte
index|[
name|StreamUtils
operator|.
name|readRawVarint32
argument_list|(
name|in
argument_list|)
index|]
decl_stmt|;
name|int
name|bytesRead
init|=
name|in
operator|.
name|read
argument_list|(
name|arr
argument_list|)
decl_stmt|;
if|if
condition|(
name|bytesRead
operator|!=
name|arr
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot read; wanted "
operator|+
name|arr
operator|.
name|length
operator|+
literal|", but got "
operator|+
name|bytesRead
argument_list|)
throw|;
block|}
if|if
condition|(
name|dict
operator|!=
literal|null
condition|)
name|dict
operator|.
name|addEntry
argument_list|(
name|arr
argument_list|,
literal|0
argument_list|,
name|arr
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|arr
return|;
block|}
else|else
block|{
comment|// Status here is the higher-order byte of index of the dictionary entry.
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
name|in
operator|.
name|read
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|entry
init|=
name|dict
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
return|return
name|entry
return|;
block|}
block|}
specifier|static
class|class
name|CompressedKvEncoder
extends|extends
name|BaseEncoder
block|{
specifier|private
specifier|final
name|CompressionContext
name|compression
decl_stmt|;
specifier|public
name|CompressedKvEncoder
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|CompressionContext
name|compression
parameter_list|)
block|{
name|super
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|this
operator|.
name|compression
operator|=
name|compression
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
comment|// We first write the KeyValue infrastructure as VInts.
name|StreamUtils
operator|.
name|writeRawVInt32
argument_list|(
name|out
argument_list|,
name|KeyValueUtil
operator|.
name|keyLength
argument_list|(
name|cell
argument_list|)
argument_list|)
expr_stmt|;
name|StreamUtils
operator|.
name|writeRawVInt32
argument_list|(
name|out
argument_list|,
name|cell
operator|.
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
comment|// To support tags
name|int
name|tagsLength
init|=
name|cell
operator|.
name|getTagsLength
argument_list|()
decl_stmt|;
name|StreamUtils
operator|.
name|writeRawVInt32
argument_list|(
name|out
argument_list|,
name|tagsLength
argument_list|)
expr_stmt|;
name|PrivateCellUtil
operator|.
name|compressRow
argument_list|(
name|out
argument_list|,
name|cell
argument_list|,
name|compression
operator|.
name|getDictionary
argument_list|(
name|CompressionContext
operator|.
name|DictionaryIndex
operator|.
name|ROW
argument_list|)
argument_list|)
expr_stmt|;
name|PrivateCellUtil
operator|.
name|compressFamily
argument_list|(
name|out
argument_list|,
name|cell
argument_list|,
name|compression
operator|.
name|getDictionary
argument_list|(
name|CompressionContext
operator|.
name|DictionaryIndex
operator|.
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|PrivateCellUtil
operator|.
name|compressQualifier
argument_list|(
name|out
argument_list|,
name|cell
argument_list|,
name|compression
operator|.
name|getDictionary
argument_list|(
name|CompressionContext
operator|.
name|DictionaryIndex
operator|.
name|QUALIFIER
argument_list|)
argument_list|)
expr_stmt|;
comment|// Write timestamp, type and value as uncompressed.
name|StreamUtils
operator|.
name|writeLong
argument_list|(
name|out
argument_list|,
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|cell
operator|.
name|getTypeByte
argument_list|()
argument_list|)
expr_stmt|;
name|PrivateCellUtil
operator|.
name|writeValue
argument_list|(
name|out
argument_list|,
name|cell
argument_list|,
name|cell
operator|.
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|tagsLength
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|compression
operator|.
name|tagCompressionContext
operator|!=
literal|null
condition|)
block|{
comment|// Write tags using Dictionary compression
name|PrivateCellUtil
operator|.
name|compressTags
argument_list|(
name|out
argument_list|,
name|cell
argument_list|,
name|compression
operator|.
name|tagCompressionContext
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Tag compression is disabled within the WAL compression. Just write the tags bytes as
comment|// it is.
name|PrivateCellUtil
operator|.
name|writeTags
argument_list|(
name|out
argument_list|,
name|cell
argument_list|,
name|tagsLength
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|static
class|class
name|CompressedKvDecoder
extends|extends
name|BaseDecoder
block|{
specifier|private
specifier|final
name|CompressionContext
name|compression
decl_stmt|;
specifier|public
name|CompressedKvDecoder
parameter_list|(
name|InputStream
name|in
parameter_list|,
name|CompressionContext
name|compression
parameter_list|)
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|compression
operator|=
name|compression
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
name|int
name|keylength
init|=
name|StreamUtils
operator|.
name|readRawVarint32
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|int
name|vlength
init|=
name|StreamUtils
operator|.
name|readRawVarint32
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|int
name|tagsLength
init|=
name|StreamUtils
operator|.
name|readRawVarint32
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|int
name|length
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|tagsLength
operator|==
literal|0
condition|)
block|{
name|length
operator|=
name|KeyValue
operator|.
name|KEYVALUE_INFRASTRUCTURE_SIZE
operator|+
name|keylength
operator|+
name|vlength
expr_stmt|;
block|}
else|else
block|{
name|length
operator|=
name|KeyValue
operator|.
name|KEYVALUE_WITH_TAGS_INFRASTRUCTURE_SIZE
operator|+
name|keylength
operator|+
name|vlength
operator|+
name|tagsLength
expr_stmt|;
block|}
name|byte
index|[]
name|backingArray
init|=
operator|new
name|byte
index|[
name|length
index|]
decl_stmt|;
name|int
name|pos
init|=
literal|0
decl_stmt|;
name|pos
operator|=
name|Bytes
operator|.
name|putInt
argument_list|(
name|backingArray
argument_list|,
name|pos
argument_list|,
name|keylength
argument_list|)
expr_stmt|;
name|pos
operator|=
name|Bytes
operator|.
name|putInt
argument_list|(
name|backingArray
argument_list|,
name|pos
argument_list|,
name|vlength
argument_list|)
expr_stmt|;
comment|// the row
name|int
name|elemLen
init|=
name|readIntoArray
argument_list|(
name|backingArray
argument_list|,
name|pos
operator|+
name|Bytes
operator|.
name|SIZEOF_SHORT
argument_list|,
name|compression
operator|.
name|getDictionary
argument_list|(
name|CompressionContext
operator|.
name|DictionaryIndex
operator|.
name|ROW
argument_list|)
argument_list|)
decl_stmt|;
name|checkLength
argument_list|(
name|elemLen
argument_list|,
name|Short
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|pos
operator|=
name|Bytes
operator|.
name|putShort
argument_list|(
name|backingArray
argument_list|,
name|pos
argument_list|,
operator|(
name|short
operator|)
name|elemLen
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|elemLen
expr_stmt|;
comment|// family
name|elemLen
operator|=
name|readIntoArray
argument_list|(
name|backingArray
argument_list|,
name|pos
operator|+
name|Bytes
operator|.
name|SIZEOF_BYTE
argument_list|,
name|compression
operator|.
name|getDictionary
argument_list|(
name|CompressionContext
operator|.
name|DictionaryIndex
operator|.
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|checkLength
argument_list|(
name|elemLen
argument_list|,
name|Byte
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|pos
operator|=
name|Bytes
operator|.
name|putByte
argument_list|(
name|backingArray
argument_list|,
name|pos
argument_list|,
operator|(
name|byte
operator|)
name|elemLen
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|elemLen
expr_stmt|;
comment|// qualifier
name|elemLen
operator|=
name|readIntoArray
argument_list|(
name|backingArray
argument_list|,
name|pos
argument_list|,
name|compression
operator|.
name|getDictionary
argument_list|(
name|CompressionContext
operator|.
name|DictionaryIndex
operator|.
name|QUALIFIER
argument_list|)
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|elemLen
expr_stmt|;
comment|// timestamp, type and value
name|int
name|tsTypeValLen
init|=
name|length
operator|-
name|pos
decl_stmt|;
if|if
condition|(
name|tagsLength
operator|>
literal|0
condition|)
block|{
name|tsTypeValLen
operator|=
name|tsTypeValLen
operator|-
name|tagsLength
operator|-
name|KeyValue
operator|.
name|TAGS_LENGTH_SIZE
expr_stmt|;
block|}
name|IOUtils
operator|.
name|readFully
argument_list|(
name|in
argument_list|,
name|backingArray
argument_list|,
name|pos
argument_list|,
name|tsTypeValLen
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|tsTypeValLen
expr_stmt|;
comment|// tags
if|if
condition|(
name|tagsLength
operator|>
literal|0
condition|)
block|{
name|pos
operator|=
name|Bytes
operator|.
name|putAsShort
argument_list|(
name|backingArray
argument_list|,
name|pos
argument_list|,
name|tagsLength
argument_list|)
expr_stmt|;
if|if
condition|(
name|compression
operator|.
name|tagCompressionContext
operator|!=
literal|null
condition|)
block|{
name|compression
operator|.
name|tagCompressionContext
operator|.
name|uncompressTags
argument_list|(
name|in
argument_list|,
name|backingArray
argument_list|,
name|pos
argument_list|,
name|tagsLength
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|IOUtils
operator|.
name|readFully
argument_list|(
name|in
argument_list|,
name|backingArray
argument_list|,
name|pos
argument_list|,
name|tagsLength
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|KeyValue
argument_list|(
name|backingArray
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
return|;
block|}
specifier|private
name|int
name|readIntoArray
parameter_list|(
name|byte
index|[]
name|to
parameter_list|,
name|int
name|offset
parameter_list|,
name|Dictionary
name|dict
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
name|status
init|=
operator|(
name|byte
operator|)
name|in
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
comment|// status byte indicating that data to be read is not in dictionary.
comment|// if this isn't in the dictionary, we need to add to the dictionary.
name|int
name|length
init|=
name|StreamUtils
operator|.
name|readRawVarint32
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|IOUtils
operator|.
name|readFully
argument_list|(
name|in
argument_list|,
name|to
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|dict
operator|.
name|addEntry
argument_list|(
name|to
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
return|return
name|length
return|;
block|}
else|else
block|{
comment|// the status byte also acts as the higher order byte of the dictionary entry.
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
name|in
operator|.
name|read
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|entry
init|=
name|dict
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
comment|// now we write the uncompressed value.
name|Bytes
operator|.
name|putBytes
argument_list|(
name|to
argument_list|,
name|offset
argument_list|,
name|entry
argument_list|,
literal|0
argument_list|,
name|entry
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|entry
operator|.
name|length
return|;
block|}
block|}
specifier|private
specifier|static
name|void
name|checkLength
parameter_list|(
name|int
name|len
parameter_list|,
name|int
name|max
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|len
argument_list|<
literal|0
operator|||
name|len
argument_list|>
name|max
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Invalid length for compresesed portion of keyvalue: "
operator|+
name|len
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|EnsureKvEncoder
extends|extends
name|BaseEncoder
block|{
specifier|public
name|EnsureKvEncoder
parameter_list|(
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
comment|// Make sure to write tags into WAL
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
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|KeyValueUtil
operator|.
name|oswrite
argument_list|(
name|cell
argument_list|,
name|this
operator|.
name|out
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Decoder
name|getDecoder
parameter_list|(
name|InputStream
name|is
parameter_list|)
block|{
return|return
operator|(
name|compression
operator|==
literal|null
operator|)
condition|?
operator|new
name|KeyValueCodecWithTags
operator|.
name|KeyValueDecoder
argument_list|(
name|is
argument_list|)
else|:
operator|new
name|CompressedKvDecoder
argument_list|(
name|is
argument_list|,
name|compression
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
name|getDecoder
argument_list|(
operator|new
name|ByteBuffInputStream
argument_list|(
name|buf
argument_list|)
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
name|os
operator|=
operator|(
name|os
operator|instanceof
name|ByteBufferWriter
operator|)
condition|?
name|os
else|:
operator|new
name|ByteBufferWriterOutputStream
argument_list|(
name|os
argument_list|)
expr_stmt|;
if|if
condition|(
name|compression
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|EnsureKvEncoder
argument_list|(
name|os
argument_list|)
return|;
block|}
return|return
operator|new
name|CompressedKvEncoder
argument_list|(
name|os
argument_list|,
name|compression
argument_list|)
return|;
block|}
specifier|public
name|ByteStringCompressor
name|getByteStringCompressor
parameter_list|()
block|{
return|return
operator|new
name|BaosAndCompressor
argument_list|(
name|compression
argument_list|)
return|;
block|}
specifier|public
name|ByteStringUncompressor
name|getByteStringUncompressor
parameter_list|()
block|{
return|return
operator|new
name|StatelessUncompressor
argument_list|(
name|compression
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ByteStringCompressor
name|getNoneCompressor
parameter_list|()
block|{
return|return
operator|new
name|NoneCompressor
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|ByteStringUncompressor
name|getNoneUncompressor
parameter_list|()
block|{
return|return
operator|new
name|NoneUncompressor
argument_list|()
return|;
block|}
block|}
end_class

end_unit

