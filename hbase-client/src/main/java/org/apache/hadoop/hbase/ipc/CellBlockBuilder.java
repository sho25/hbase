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
name|ipc
package|;
end_package

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
name|shaded
operator|.
name|io
operator|.
name|netty
operator|.
name|buffer
operator|.
name|ByteBuf
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
name|shaded
operator|.
name|io
operator|.
name|netty
operator|.
name|buffer
operator|.
name|ByteBufAllocator
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
name|shaded
operator|.
name|io
operator|.
name|netty
operator|.
name|buffer
operator|.
name|ByteBufOutputStream
import|;
end_import

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
name|nio
operator|.
name|SingleByteBuff
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
comment|/**  * Helper class for building cell block.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|CellBlockBuilder
block|{
comment|// LOG is being used in TestCellBlockBuilder
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|CellBlockBuilder
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
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
specifier|public
name|CellBlockBuilder
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
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
comment|// Guess that 16k is a good size for rpc buffer. Could go bigger. See the TODO below in
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
specifier|private
interface|interface
name|OutputStreamSupplier
block|{
name|OutputStream
name|get
parameter_list|(
name|int
name|expectedSize
parameter_list|)
function_decl|;
name|int
name|size
parameter_list|()
function_decl|;
block|}
specifier|private
specifier|static
specifier|final
class|class
name|ByteBufferOutputStreamSupplier
implements|implements
name|OutputStreamSupplier
block|{
specifier|private
name|ByteBufferOutputStream
name|baos
decl_stmt|;
annotation|@
name|Override
specifier|public
name|OutputStream
name|get
parameter_list|(
name|int
name|expectedSize
parameter_list|)
block|{
name|baos
operator|=
operator|new
name|ByteBufferOutputStream
argument_list|(
name|expectedSize
argument_list|)
expr_stmt|;
return|return
name|baos
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|baos
operator|.
name|size
argument_list|()
return|;
block|}
block|}
comment|/**    * Puts CellScanner Cells into a cell block using passed in<code>codec</code> and/or    *<code>compressor</code>.    * @param codec    * @param compressor    * @param cellScanner    * @return Null or byte buffer filled with a cellblock filled with passed-in Cells encoded using    *         passed in<code>codec</code> and/or<code>compressor</code>; the returned buffer has    *         been flipped and is ready for reading. Use limit to find total size.    * @throws IOException    */
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
name|ByteBufferOutputStreamSupplier
name|supplier
init|=
operator|new
name|ByteBufferOutputStreamSupplier
argument_list|()
decl_stmt|;
if|if
condition|(
name|buildCellBlock
argument_list|(
name|codec
argument_list|,
name|compressor
argument_list|,
name|cellScanner
argument_list|,
name|supplier
argument_list|)
condition|)
block|{
name|ByteBuffer
name|bb
init|=
name|supplier
operator|.
name|baos
operator|.
name|getByteBuffer
argument_list|()
decl_stmt|;
comment|// If no cells, don't mess around. Just return null (could be a bunch of existence checking
comment|// gets or something -- stuff that does not return a cell).
return|return
name|bb
operator|.
name|hasRemaining
argument_list|()
condition|?
name|bb
else|:
literal|null
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|private
specifier|static
specifier|final
class|class
name|ByteBufOutputStreamSupplier
implements|implements
name|OutputStreamSupplier
block|{
specifier|private
specifier|final
name|ByteBufAllocator
name|alloc
decl_stmt|;
specifier|private
name|ByteBuf
name|buf
decl_stmt|;
specifier|public
name|ByteBufOutputStreamSupplier
parameter_list|(
name|ByteBufAllocator
name|alloc
parameter_list|)
block|{
name|this
operator|.
name|alloc
operator|=
name|alloc
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|OutputStream
name|get
parameter_list|(
name|int
name|expectedSize
parameter_list|)
block|{
name|buf
operator|=
name|alloc
operator|.
name|buffer
argument_list|(
name|expectedSize
argument_list|)
expr_stmt|;
return|return
operator|new
name|ByteBufOutputStream
argument_list|(
name|buf
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|buf
operator|.
name|writerIndex
argument_list|()
return|;
block|}
block|}
specifier|public
name|ByteBuf
name|buildCellBlock
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
name|ByteBufAllocator
name|alloc
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteBufOutputStreamSupplier
name|supplier
init|=
operator|new
name|ByteBufOutputStreamSupplier
argument_list|(
name|alloc
argument_list|)
decl_stmt|;
if|if
condition|(
name|buildCellBlock
argument_list|(
name|codec
argument_list|,
name|compressor
argument_list|,
name|cellScanner
argument_list|,
name|supplier
argument_list|)
condition|)
block|{
return|return
name|supplier
operator|.
name|buf
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|private
name|boolean
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
parameter_list|,
name|OutputStreamSupplier
name|supplier
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
literal|false
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
name|cellBlockBuildingInitialBufferSize
decl_stmt|;
name|encodeCellsTo
argument_list|(
name|supplier
operator|.
name|get
argument_list|(
name|bufferSize
argument_list|)
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
operator|&&
name|bufferSize
operator|<
name|supplier
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
name|supplier
operator|.
name|size
argument_list|()
operator|+
literal|"; up hbase.ipc.cellblock.building.initial.buffersize?"
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|void
name|encodeCellsTo
parameter_list|(
name|OutputStream
name|os
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
decl||
name|IndexOutOfBoundsException
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
comment|/**    * Puts CellScanner Cells into a cell block using passed in<code>codec</code> and/or    *<code>compressor</code>.    * @param codec to use for encoding    * @param compressor to use for encoding    * @param cellScanner to encode    * @param pool Pool of ByteBuffers to make use of.    * @return Null or byte buffer filled with a cellblock filled with passed-in Cells encoded using    *         passed in<code>codec</code> and/or<code>compressor</code>; the returned buffer has    *         been flipped and is ready for reading. Use limit to find total size. If    *<code>pool</code> was not null, then this returned ByteBuffer came from there and    *         should be returned to the pool when done.    * @throws IOException if encoding the cells fail    */
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
if|if
condition|(
name|compressor
operator|!=
literal|null
condition|)
block|{
name|ByteBuffer
name|cellBlockBuf
init|=
name|decompress
argument_list|(
name|compressor
argument_list|,
name|cellBlock
argument_list|)
decl_stmt|;
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
name|ByteArrayInputStream
argument_list|(
name|cellBlock
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * @param codec to use for cellblock    * @param cellBlock ByteBuffer containing the cells written by the Codec. The buffer should be    *          position()'ed at the start of the cell block and limit()'ed at the end.    * @return CellScanner to work against the content of<code>cellBlock</code>. All cells created    *         out of the CellScanner will share the same ByteBuffer being passed.    * @throws IOException if cell encoding fails    */
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
name|ByteBuff
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
name|byte
index|[]
name|compressedCellBlock
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteBuffer
name|cellBlock
init|=
name|decompress
argument_list|(
name|compressor
argument_list|,
operator|new
name|ByteArrayInputStream
argument_list|(
name|compressedCellBlock
argument_list|)
argument_list|,
name|compressedCellBlock
operator|.
name|length
operator|*
name|this
operator|.
name|cellBlockDecompressionMultiplier
argument_list|)
decl_stmt|;
return|return
name|cellBlock
return|;
block|}
specifier|private
name|ByteBuff
name|decompress
parameter_list|(
name|CompressionCodec
name|compressor
parameter_list|,
name|ByteBuff
name|compressedCellBlock
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteBuffer
name|cellBlock
init|=
name|decompress
argument_list|(
name|compressor
argument_list|,
operator|new
name|ByteBuffInputStream
argument_list|(
name|compressedCellBlock
argument_list|)
argument_list|,
name|compressedCellBlock
operator|.
name|remaining
argument_list|()
operator|*
name|this
operator|.
name|cellBlockDecompressionMultiplier
argument_list|)
decl_stmt|;
return|return
operator|new
name|SingleByteBuff
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
name|InputStream
name|cellBlockStream
parameter_list|,
name|int
name|osInitialSize
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
name|cellBlockStream
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
name|osInitialSize
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
return|return
name|bbos
operator|.
name|getByteBuffer
argument_list|()
return|;
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
block|}
block|}
end_class

end_unit

