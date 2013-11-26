begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
operator|.
name|encoding
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
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|NotImplementedException
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
name|hfile
operator|.
name|HFileContext
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
name|hadoop
operator|.
name|io
operator|.
name|compress
operator|.
name|Compressor
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * Encapsulates a data block compressed using a particular encoding algorithm.  * Useful for testing and benchmarking.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|EncodedDataBlock
block|{
specifier|private
name|byte
index|[]
name|rawKVs
decl_stmt|;
specifier|private
name|ByteBuffer
name|rawBuffer
decl_stmt|;
specifier|private
name|DataBlockEncoder
name|dataBlockEncoder
decl_stmt|;
specifier|private
name|byte
index|[]
name|cachedEncodedData
decl_stmt|;
specifier|private
specifier|final
name|HFileBlockEncodingContext
name|encodingCtx
decl_stmt|;
specifier|private
name|HFileContext
name|meta
decl_stmt|;
comment|/**    * Create a buffer which will be encoded using dataBlockEncoder.    * @param dataBlockEncoder Algorithm used for compression.    * @param encoding encoding type used    * @param rawKVs    * @param meta    */
specifier|public
name|EncodedDataBlock
parameter_list|(
name|DataBlockEncoder
name|dataBlockEncoder
parameter_list|,
name|DataBlockEncoding
name|encoding
parameter_list|,
name|byte
index|[]
name|rawKVs
parameter_list|,
name|HFileContext
name|meta
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|encoding
argument_list|,
literal|"Cannot create encoded data block with null encoder"
argument_list|)
expr_stmt|;
name|this
operator|.
name|dataBlockEncoder
operator|=
name|dataBlockEncoder
expr_stmt|;
name|encodingCtx
operator|=
name|dataBlockEncoder
operator|.
name|newDataBlockEncodingContext
argument_list|(
name|encoding
argument_list|,
name|HConstants
operator|.
name|HFILEBLOCK_DUMMY_HEADER
argument_list|,
name|meta
argument_list|)
expr_stmt|;
name|this
operator|.
name|rawKVs
operator|=
name|rawKVs
expr_stmt|;
name|this
operator|.
name|meta
operator|=
name|meta
expr_stmt|;
block|}
comment|/**    * Provides access to compressed value.    * @param headerSize header size of the block.    * @return Forwards sequential iterator.    */
specifier|public
name|Iterator
argument_list|<
name|Cell
argument_list|>
name|getIterator
parameter_list|(
name|int
name|headerSize
parameter_list|)
block|{
specifier|final
name|int
name|rawSize
init|=
name|rawKVs
operator|.
name|length
decl_stmt|;
name|byte
index|[]
name|encodedDataWithHeader
init|=
name|getEncodedData
argument_list|()
decl_stmt|;
name|int
name|bytesToSkip
init|=
name|headerSize
operator|+
name|Bytes
operator|.
name|SIZEOF_SHORT
decl_stmt|;
name|ByteArrayInputStream
name|bais
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|encodedDataWithHeader
argument_list|,
name|bytesToSkip
argument_list|,
name|encodedDataWithHeader
operator|.
name|length
operator|-
name|bytesToSkip
argument_list|)
decl_stmt|;
specifier|final
name|DataInputStream
name|dis
init|=
operator|new
name|DataInputStream
argument_list|(
name|bais
argument_list|)
decl_stmt|;
return|return
operator|new
name|Iterator
argument_list|<
name|Cell
argument_list|>
argument_list|()
block|{
specifier|private
name|ByteBuffer
name|decompressedData
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
if|if
condition|(
name|decompressedData
operator|==
literal|null
condition|)
block|{
return|return
name|rawSize
operator|>
literal|0
return|;
block|}
return|return
name|decompressedData
operator|.
name|hasRemaining
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|next
parameter_list|()
block|{
if|if
condition|(
name|decompressedData
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|decompressedData
operator|=
name|dataBlockEncoder
operator|.
name|decodeKeyValues
argument_list|(
name|dis
argument_list|,
name|dataBlockEncoder
operator|.
name|newDataBlockDecodingContext
argument_list|(
name|meta
argument_list|)
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
name|RuntimeException
argument_list|(
literal|"Problem with data block encoder, "
operator|+
literal|"most likely it requested more bytes than are available."
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|decompressedData
operator|.
name|rewind
argument_list|()
expr_stmt|;
block|}
name|int
name|offset
init|=
name|decompressedData
operator|.
name|position
argument_list|()
decl_stmt|;
name|int
name|klen
init|=
name|decompressedData
operator|.
name|getInt
argument_list|()
decl_stmt|;
name|int
name|vlen
init|=
name|decompressedData
operator|.
name|getInt
argument_list|()
decl_stmt|;
name|short
name|tagsLen
init|=
literal|0
decl_stmt|;
name|ByteBufferUtils
operator|.
name|skip
argument_list|(
name|decompressedData
argument_list|,
name|klen
operator|+
name|vlen
argument_list|)
expr_stmt|;
comment|// Read the tag length in case when steam contain tags
if|if
condition|(
name|meta
operator|.
name|isIncludesTags
argument_list|()
condition|)
block|{
name|tagsLen
operator|=
name|decompressedData
operator|.
name|getShort
argument_list|()
expr_stmt|;
name|ByteBufferUtils
operator|.
name|skip
argument_list|(
name|decompressedData
argument_list|,
name|tagsLen
argument_list|)
expr_stmt|;
block|}
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|decompressedData
operator|.
name|array
argument_list|()
argument_list|,
name|offset
argument_list|,
operator|(
name|int
operator|)
name|KeyValue
operator|.
name|getKeyValueDataStructureSize
argument_list|(
name|klen
argument_list|,
name|vlen
argument_list|,
name|tagsLen
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|meta
operator|.
name|isIncludesMvcc
argument_list|()
condition|)
block|{
name|long
name|mvccVersion
init|=
name|ByteBufferUtils
operator|.
name|readVLong
argument_list|(
name|decompressedData
argument_list|)
decl_stmt|;
name|kv
operator|.
name|setMvccVersion
argument_list|(
name|mvccVersion
argument_list|)
expr_stmt|;
block|}
return|return
name|kv
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|NotImplementedException
argument_list|(
literal|"remove() is not supported!"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"Iterator of: "
operator|+
name|dataBlockEncoder
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
return|;
block|}
block|}
return|;
block|}
comment|/**    * Find the size of minimal buffer that could store compressed data.    * @return Size in bytes of compressed data.    */
specifier|public
name|int
name|getSize
parameter_list|()
block|{
return|return
name|getEncodedData
argument_list|()
operator|.
name|length
return|;
block|}
comment|/**    * Find the size of compressed data assuming that buffer will be compressed    * using given algorithm.    * @param algo compression algorithm    * @param compressor compressor already requested from codec    * @param inputBuffer Array to be compressed.    * @param offset Offset to beginning of the data.    * @param length Length to be compressed.    * @return Size of compressed data in bytes.    * @throws IOException    */
specifier|public
specifier|static
name|int
name|getCompressedSize
parameter_list|(
name|Algorithm
name|algo
parameter_list|,
name|Compressor
name|compressor
parameter_list|,
name|byte
index|[]
name|inputBuffer
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
name|DataOutputStream
name|compressedStream
init|=
operator|new
name|DataOutputStream
argument_list|(
operator|new
name|IOUtils
operator|.
name|NullOutputStream
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|compressor
operator|!=
literal|null
condition|)
block|{
name|compressor
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
name|OutputStream
name|compressingStream
init|=
literal|null
decl_stmt|;
try|try
block|{
name|compressingStream
operator|=
name|algo
operator|.
name|createCompressionStream
argument_list|(
name|compressedStream
argument_list|,
name|compressor
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|compressingStream
operator|.
name|write
argument_list|(
name|inputBuffer
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|compressingStream
operator|.
name|flush
argument_list|()
expr_stmt|;
return|return
name|compressedStream
operator|.
name|size
argument_list|()
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|compressingStream
operator|!=
literal|null
condition|)
name|compressingStream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Estimate size after second stage of compression (e.g. LZO).    * @param comprAlgo compression algorithm to be used for compression    * @param compressor compressor corresponding to the given compression    *          algorithm    * @return Size after second stage of compression.    */
specifier|public
name|int
name|getEncodedCompressedSize
parameter_list|(
name|Algorithm
name|comprAlgo
parameter_list|,
name|Compressor
name|compressor
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|compressedBytes
init|=
name|getEncodedData
argument_list|()
decl_stmt|;
return|return
name|getCompressedSize
argument_list|(
name|comprAlgo
argument_list|,
name|compressor
argument_list|,
name|compressedBytes
argument_list|,
literal|0
argument_list|,
name|compressedBytes
operator|.
name|length
argument_list|)
return|;
block|}
comment|/** @return encoded data with header */
specifier|private
name|byte
index|[]
name|getEncodedData
parameter_list|()
block|{
if|if
condition|(
name|cachedEncodedData
operator|!=
literal|null
condition|)
block|{
return|return
name|cachedEncodedData
return|;
block|}
name|cachedEncodedData
operator|=
name|encodeData
argument_list|()
expr_stmt|;
return|return
name|cachedEncodedData
return|;
block|}
specifier|private
name|ByteBuffer
name|getUncompressedBuffer
parameter_list|()
block|{
if|if
condition|(
name|rawBuffer
operator|==
literal|null
operator|||
name|rawBuffer
operator|.
name|limit
argument_list|()
operator|<
name|rawKVs
operator|.
name|length
condition|)
block|{
name|rawBuffer
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|rawKVs
argument_list|)
expr_stmt|;
block|}
return|return
name|rawBuffer
return|;
block|}
comment|/**    * Do the encoding, but do not cache the encoded data.    * @return encoded data block with header and checksum    */
specifier|public
name|byte
index|[]
name|encodeData
parameter_list|()
block|{
try|try
block|{
name|this
operator|.
name|dataBlockEncoder
operator|.
name|encodeKeyValues
argument_list|(
name|getUncompressedBuffer
argument_list|()
argument_list|,
name|encodingCtx
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
name|RuntimeException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Bug in encoding part of algorithm %s. "
operator|+
literal|"Probably it requested more bytes than are available."
argument_list|,
name|toString
argument_list|()
argument_list|)
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|encodingCtx
operator|.
name|getUncompressedBytesWithHeader
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|dataBlockEncoder
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

