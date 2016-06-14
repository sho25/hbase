begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ipc
package|;
end_package

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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|CodedOutputStream
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
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
name|BufferOverflowException
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
name|commons
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
name|commons
operator|.
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|Configurable
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
name|CellScanner
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
name|DoNotRetryIOException
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
name|HBaseIOException
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
name|io
operator|.
name|ByteBufferInputStream
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
name|ByteBufferOutputStream
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
name|ByteBufferPool
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
name|ByteBufferListOutputStream
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
name|ClassSize
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
name|CodecPool
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
name|CompressionCodec
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
name|CompressionInputStream
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
name|Decompressor
import|;
end_import

begin_comment
comment|/**  * Utility to help ipc'ing.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|IPCUtil
block|{
comment|// LOG is being used in TestIPCUtil
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|IPCUtil
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * How much we think the decompressor will expand the original compressed content.    */
specifier|private
specifier|final
name|int
name|cellBlockDecompressionMultiplier
decl_stmt|;
specifier|private
specifier|final
name|int
name|cellBlockBuildingInitialBufferSize
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|public
name|IPCUtil
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|cellBlockDecompressionMultiplier
operator|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.ipc.cellblock.decompression.buffersize.multiplier"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
comment|// Guess that 16k is a good size for rpc buffer.  Could go bigger.  See the TODO below in
comment|// #buildCellBlock.
name|this
operator|.
name|cellBlockBuildingInitialBufferSize
operator|=
name|ClassSize
operator|.
name|align
argument_list|(
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.ipc.cellblock.building.initial.buffersize"
argument_list|,
literal|16
operator|*
literal|1024
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Thrown if a cellscanner but no codec to encode it with.    */
specifier|public
specifier|static
class|class
name|CellScannerButNoCodecException
extends|extends
name|HBaseIOException
block|{}
empty_stmt|;
comment|/**    * Puts CellScanner Cells into a cell block using passed in<code>codec</code> and/or    *<code>compressor</code>.    * @param codec to use for encoding    * @param compressor to use for encoding    * @param cellScanner to encode    * @return Null or byte buffer filled with a cellblock filled with passed-in Cells encoded using    *   passed in<code>codec</code> and/or<code>compressor</code>; the returned buffer has been    *   flipped and is ready for reading.  Use limit to find total size.    * @throws IOException if encoding the cells fail    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"resource"
argument_list|)
specifier|public
name|ByteBuffer
name|buildCellBlock
parameter_list|(
specifier|final
name|Codec
name|codec
parameter_list|,
specifier|final
name|CompressionCodec
name|compressor
parameter_list|,
specifier|final
name|CellScanner
name|cellScanner
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|cellScanner
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|codec
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|CellScannerButNoCodecException
argument_list|()
throw|;
block|}
name|int
name|bufferSize
init|=
name|this
operator|.
name|cellBlockBuildingInitialBufferSize
decl_stmt|;
name|ByteBufferOutputStream
name|baos
init|=
operator|new
name|ByteBufferOutputStream
argument_list|(
name|bufferSize
argument_list|)
decl_stmt|;
name|encodeCellsTo
argument_list|(
name|baos
argument_list|,
name|cellScanner
argument_list|,
name|codec
argument_list|,
name|compressor
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
if|if
condition|(
name|bufferSize
operator|<
name|baos
operator|.
name|size
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Buffer grew from initial bufferSize="
operator|+
name|bufferSize
operator|+
literal|" to "
operator|+
name|baos
operator|.
name|size
argument_list|()
operator|+
literal|"; up hbase.ipc.cellblock.building.initial.buffersize?"
argument_list|)
expr_stmt|;
block|}
block|}
name|ByteBuffer
name|bb
init|=
name|baos
operator|.
name|getByteBuffer
argument_list|()
decl_stmt|;
comment|// If no cells, don't mess around. Just return null (could be a bunch of existence checking
comment|// gets or something -- stuff that does not return a cell).
if|if
condition|(
operator|!
name|bb
operator|.
name|hasRemaining
argument_list|()
condition|)
return|return
literal|null
return|;
return|return
name|bb
return|;
block|}
specifier|private
name|void
name|encodeCellsTo
parameter_list|(
name|ByteBufferOutputStream
name|bbos
parameter_list|,
name|CellScanner
name|cellScanner
parameter_list|,
name|Codec
name|codec
parameter_list|,
name|CompressionCodec
name|compressor
parameter_list|)
throws|throws
name|IOException
block|{
name|OutputStream
name|os
init|=
name|bbos
decl_stmt|;
name|Compressor
name|poolCompressor
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
name|compressor
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|compressor
operator|instanceof
name|Configurable
condition|)
block|{
operator|(
operator|(
name|Configurable
operator|)
name|compressor
operator|)
operator|.
name|setConf
argument_list|(
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
block|}
name|poolCompressor
operator|=
name|CodecPool
operator|.
name|getCompressor
argument_list|(
name|compressor
argument_list|)
expr_stmt|;
name|os
operator|=
name|compressor
operator|.
name|createOutputStream
argument_list|(
name|os
argument_list|,
name|poolCompressor
argument_list|)
expr_stmt|;
block|}
name|Codec
operator|.
name|Encoder
name|encoder
init|=
name|codec
operator|.
name|getEncoder
argument_list|(
name|os
argument_list|)
decl_stmt|;
while|while
condition|(
name|cellScanner
operator|.
name|advance
argument_list|()
condition|)
block|{
name|encoder
operator|.
name|write
argument_list|(
name|cellScanner
operator|.
name|current
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|encoder
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|BufferOverflowException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|os
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|poolCompressor
operator|!=
literal|null
condition|)
block|{
name|CodecPool
operator|.
name|returnCompressor
argument_list|(
name|poolCompressor
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Puts CellScanner Cells into a cell block using passed in<code>codec</code> and/or    *<code>compressor</code>.    * @param codec to use for encoding    * @param compressor to use for encoding    * @param cellScanner to encode    * @param pool Pool of ByteBuffers to make use of.    * @return Null or byte buffer filled with a cellblock filled with passed-in Cells encoded using    *   passed in<code>codec</code> and/or<code>compressor</code>; the returned buffer has been    *   flipped and is ready for reading.  Use limit to find total size. If<code>pool</code> was not    *   null, then this returned ByteBuffer came from there and should be returned to the pool when    *   done.    * @throws IOException if encoding the cells fail    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"resource"
argument_list|)
specifier|public
name|ByteBufferListOutputStream
name|buildCellBlockStream
parameter_list|(
name|Codec
name|codec
parameter_list|,
name|CompressionCodec
name|compressor
parameter_list|,
name|CellScanner
name|cellScanner
parameter_list|,
name|ByteBufferPool
name|pool
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|cellScanner
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|codec
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|CellScannerButNoCodecException
argument_list|()
throw|;
block|}
assert|assert
name|pool
operator|!=
literal|null
assert|;
name|ByteBufferListOutputStream
name|bbos
init|=
operator|new
name|ByteBufferListOutputStream
argument_list|(
name|pool
argument_list|)
decl_stmt|;
name|encodeCellsTo
argument_list|(
name|bbos
argument_list|,
name|cellScanner
argument_list|,
name|codec
argument_list|,
name|compressor
argument_list|)
expr_stmt|;
if|if
condition|(
name|bbos
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|bbos
operator|.
name|releaseResources
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
name|bbos
return|;
block|}
comment|/**    * @param codec to use for cellblock    * @param cellBlock to encode    * @return CellScanner to work against the content of<code>cellBlock</code>    * @throws IOException if encoding fails    */
specifier|public
name|CellScanner
name|createCellScanner
parameter_list|(
specifier|final
name|Codec
name|codec
parameter_list|,
specifier|final
name|CompressionCodec
name|compressor
parameter_list|,
specifier|final
name|byte
index|[]
name|cellBlock
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Use this method from Client side to create the CellScanner
name|ByteBuffer
name|cellBlockBuf
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|cellBlock
argument_list|)
decl_stmt|;
if|if
condition|(
name|compressor
operator|!=
literal|null
condition|)
block|{
name|cellBlockBuf
operator|=
name|decompress
argument_list|(
name|compressor
argument_list|,
name|cellBlockBuf
argument_list|)
expr_stmt|;
block|}
comment|// Not making the Decoder over the ByteBuffer purposefully. The Decoder over the BB will
comment|// make Cells directly over the passed BB. This method is called at client side and we don't
comment|// want the Cells to share the same byte[] where the RPC response is being read. Caching of any
comment|// of the Cells at user's app level will make it not possible to GC the response byte[]
return|return
name|codec
operator|.
name|getDecoder
argument_list|(
operator|new
name|ByteBufferInputStream
argument_list|(
name|cellBlockBuf
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * @param codec to use for cellblock    * @param cellBlock ByteBuffer containing the cells written by the Codec. The buffer should be    *   position()'ed at the start of the cell block and limit()'ed at the end.    * @return CellScanner to work against the content of<code>cellBlock</code>.    *   All cells created out of the CellScanner will share the same ByteBuffer being passed.    * @throws IOException if cell encoding fails    */
specifier|public
name|CellScanner
name|createCellScannerReusingBuffers
parameter_list|(
specifier|final
name|Codec
name|codec
parameter_list|,
specifier|final
name|CompressionCodec
name|compressor
parameter_list|,
name|ByteBuffer
name|cellBlock
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Use this method from HRS to create the CellScanner
comment|// If compressed, decompress it first before passing it on else we will leak compression
comment|// resources if the stream is not closed properly after we let it out.
if|if
condition|(
name|compressor
operator|!=
literal|null
condition|)
block|{
name|cellBlock
operator|=
name|decompress
argument_list|(
name|compressor
argument_list|,
name|cellBlock
argument_list|)
expr_stmt|;
block|}
return|return
name|codec
operator|.
name|getDecoder
argument_list|(
name|cellBlock
argument_list|)
return|;
block|}
specifier|private
name|ByteBuffer
name|decompress
parameter_list|(
name|CompressionCodec
name|compressor
parameter_list|,
name|ByteBuffer
name|cellBlock
parameter_list|)
throws|throws
name|IOException
block|{
comment|// GZIPCodec fails w/ NPE if no configuration.
if|if
condition|(
name|compressor
operator|instanceof
name|Configurable
condition|)
block|{
operator|(
operator|(
name|Configurable
operator|)
name|compressor
operator|)
operator|.
name|setConf
argument_list|(
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
block|}
name|Decompressor
name|poolDecompressor
init|=
name|CodecPool
operator|.
name|getDecompressor
argument_list|(
name|compressor
argument_list|)
decl_stmt|;
name|CompressionInputStream
name|cis
init|=
name|compressor
operator|.
name|createInputStream
argument_list|(
operator|new
name|ByteBufferInputStream
argument_list|(
name|cellBlock
argument_list|)
argument_list|,
name|poolDecompressor
argument_list|)
decl_stmt|;
name|ByteBufferOutputStream
name|bbos
decl_stmt|;
try|try
block|{
comment|// TODO: This is ugly. The buffer will be resized on us if we guess wrong.
comment|// TODO: Reuse buffers.
name|bbos
operator|=
operator|new
name|ByteBufferOutputStream
argument_list|(
name|cellBlock
operator|.
name|remaining
argument_list|()
operator|*
name|this
operator|.
name|cellBlockDecompressionMultiplier
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|copy
argument_list|(
name|cis
argument_list|,
name|bbos
argument_list|)
expr_stmt|;
name|bbos
operator|.
name|close
argument_list|()
expr_stmt|;
name|cellBlock
operator|=
name|bbos
operator|.
name|getByteBuffer
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|CodecPool
operator|.
name|returnDecompressor
argument_list|(
name|poolDecompressor
argument_list|)
expr_stmt|;
block|}
return|return
name|cellBlock
return|;
block|}
comment|/**    * Write out header, param, and cell block if there is one.    * @param dos Stream to write into    * @param header to write    * @param param to write    * @param cellBlock to write    * @return Total number of bytes written.    * @throws IOException if write action fails    */
specifier|public
specifier|static
name|int
name|write
parameter_list|(
specifier|final
name|OutputStream
name|dos
parameter_list|,
specifier|final
name|Message
name|header
parameter_list|,
specifier|final
name|Message
name|param
parameter_list|,
specifier|final
name|ByteBuffer
name|cellBlock
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Must calculate total size and write that first so other side can read it all in in one
comment|// swoop.  This is dictated by how the server is currently written.  Server needs to change
comment|// if we are to be able to write without the length prefixing.
name|int
name|totalSize
init|=
name|IPCUtil
operator|.
name|getTotalSizeWhenWrittenDelimited
argument_list|(
name|header
argument_list|,
name|param
argument_list|)
decl_stmt|;
if|if
condition|(
name|cellBlock
operator|!=
literal|null
condition|)
block|{
name|totalSize
operator|+=
name|cellBlock
operator|.
name|remaining
argument_list|()
expr_stmt|;
block|}
return|return
name|write
argument_list|(
name|dos
argument_list|,
name|header
argument_list|,
name|param
argument_list|,
name|cellBlock
argument_list|,
name|totalSize
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|int
name|write
parameter_list|(
specifier|final
name|OutputStream
name|dos
parameter_list|,
specifier|final
name|Message
name|header
parameter_list|,
specifier|final
name|Message
name|param
parameter_list|,
specifier|final
name|ByteBuffer
name|cellBlock
parameter_list|,
specifier|final
name|int
name|totalSize
parameter_list|)
throws|throws
name|IOException
block|{
comment|// I confirmed toBytes does same as DataOutputStream#writeInt.
name|dos
operator|.
name|write
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|totalSize
argument_list|)
argument_list|)
expr_stmt|;
comment|// This allocates a buffer that is the size of the message internally.
name|header
operator|.
name|writeDelimitedTo
argument_list|(
name|dos
argument_list|)
expr_stmt|;
if|if
condition|(
name|param
operator|!=
literal|null
condition|)
block|{
name|param
operator|.
name|writeDelimitedTo
argument_list|(
name|dos
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cellBlock
operator|!=
literal|null
condition|)
block|{
name|dos
operator|.
name|write
argument_list|(
name|cellBlock
operator|.
name|array
argument_list|()
argument_list|,
literal|0
argument_list|,
name|cellBlock
operator|.
name|remaining
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|dos
operator|.
name|flush
argument_list|()
expr_stmt|;
return|return
name|totalSize
return|;
block|}
comment|/**    * @return Size on the wire when the two messages are written with writeDelimitedTo    */
specifier|public
specifier|static
name|int
name|getTotalSizeWhenWrittenDelimited
parameter_list|(
name|Message
modifier|...
name|messages
parameter_list|)
block|{
name|int
name|totalSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Message
name|m
range|:
name|messages
control|)
block|{
if|if
condition|(
name|m
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|totalSize
operator|+=
name|m
operator|.
name|getSerializedSize
argument_list|()
expr_stmt|;
name|totalSize
operator|+=
name|CodedOutputStream
operator|.
name|computeRawVarint32Size
argument_list|(
name|m
operator|.
name|getSerializedSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|totalSize
operator|<
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
return|return
name|totalSize
return|;
block|}
block|}
end_class

end_unit

