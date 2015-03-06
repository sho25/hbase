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
name|DataInput
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
name|HeapSize
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
comment|/**    * Puts CellScanner Cells into a cell block using passed in<code>codec</code> and/or    *<code>compressor</code>.    * @param codec    * @param compressor    * @param cellScanner    * @return Null or byte buffer filled with a cellblock filled with passed-in Cells encoded using    * passed in<code>codec</code> and/or<code>compressor</code>; the returned buffer has been    * flipped and is ready for reading.  Use limit to find total size.    * @throws IOException    */
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
return|return
name|buildCellBlock
argument_list|(
name|codec
argument_list|,
name|compressor
argument_list|,
name|cellScanner
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Puts CellScanner Cells into a cell block using passed in<code>codec</code> and/or    *<code>compressor</code>.    * @param codec    * @param compressor    * @param cellScanner    * @param bb ByteBuffer to use. Can be null. You'd pass in a ByteBuffer if you want to practice    * recycling. If the passed in ByteBuffer is too small, it is discarded and a new one allotted    * so you will get back the passed-in ByteBuffer or a new, right-sized one. SIDE EFFECT!!!!!    * @return Null or byte buffer filled with a cellblock filled with passed-in Cells encoded using    * passed in<code>codec</code> and/or<code>compressor</code>; the returned buffer has been    * flipped and is ready for reading.  Use limit to find total size.    * @throws IOException    */
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
parameter_list|,
specifier|final
name|ByteBuffer
name|bb
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
return|return
literal|null
return|;
if|if
condition|(
name|codec
operator|==
literal|null
condition|)
throw|throw
operator|new
name|CellScannerButNoCodecException
argument_list|()
throw|;
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
literal|null
decl_stmt|;
if|if
condition|(
name|bb
operator|!=
literal|null
condition|)
block|{
name|bufferSize
operator|=
name|bb
operator|.
name|capacity
argument_list|()
expr_stmt|;
name|baos
operator|=
operator|new
name|ByteBufferOutputStream
argument_list|(
name|bb
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Then we need to make our own to return.
if|if
condition|(
name|cellScanner
operator|instanceof
name|HeapSize
condition|)
block|{
name|long
name|longSize
init|=
operator|(
operator|(
name|HeapSize
operator|)
name|cellScanner
operator|)
operator|.
name|heapSize
argument_list|()
decl_stmt|;
comment|// Just make sure we don't have a size bigger than an int.
if|if
condition|(
name|longSize
operator|>
name|Integer
operator|.
name|MAX_VALUE
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Size "
operator|+
name|longSize
operator|+
literal|"> "
operator|+
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
throw|;
block|}
name|bufferSize
operator|=
name|ClassSize
operator|.
name|align
argument_list|(
operator|(
name|int
operator|)
name|longSize
argument_list|)
expr_stmt|;
block|}
name|baos
operator|=
operator|new
name|ByteBufferOutputStream
argument_list|(
name|bufferSize
argument_list|)
expr_stmt|;
block|}
name|OutputStream
name|os
init|=
name|baos
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
name|int
name|count
init|=
literal|0
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
name|count
operator|++
expr_stmt|;
block|}
name|encoder
operator|.
name|flush
argument_list|()
expr_stmt|;
comment|// If no cells, don't mess around.  Just return null (could be a bunch of existence checking
comment|// gets or something -- stuff that does not return a cell).
if|if
condition|(
name|count
operator|==
literal|0
condition|)
return|return
literal|null
return|;
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
name|CodecPool
operator|.
name|returnCompressor
argument_list|(
name|poolCompressor
argument_list|)
expr_stmt|;
block|}
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
return|return
name|baos
operator|.
name|getByteBuffer
argument_list|()
return|;
block|}
comment|/**    * @param codec    * @param cellBlock    * @return CellScanner to work against the content of<code>cellBlock</code>    * @throws IOException    */
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
return|return
name|createCellScanner
argument_list|(
name|codec
argument_list|,
name|compressor
argument_list|,
name|cellBlock
argument_list|,
literal|0
argument_list|,
name|cellBlock
operator|.
name|length
argument_list|)
return|;
block|}
comment|/**    * @param codec    * @param cellBlock    * @param offset    * @param length    * @return CellScanner to work against the content of<code>cellBlock</code>    * @throws IOException    */
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
parameter_list|,
specifier|final
name|int
name|offset
parameter_list|,
specifier|final
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
comment|// If compressed, decompress it first before passing it on else we will leak compression
comment|// resources if the stream is not closed properly after we let it out.
name|InputStream
name|is
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|compressor
operator|!=
literal|null
condition|)
block|{
comment|// GZIPCodec fails w/ NPE if no configuration.
if|if
condition|(
name|compressor
operator|instanceof
name|Configurable
condition|)
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
name|ByteArrayInputStream
argument_list|(
name|cellBlock
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
argument_list|,
name|poolDecompressor
argument_list|)
decl_stmt|;
name|ByteBufferOutputStream
name|bbos
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// TODO: This is ugly.  The buffer will be resized on us if we guess wrong.
comment|// TODO: Reuse buffers.
name|bbos
operator|=
operator|new
name|ByteBufferOutputStream
argument_list|(
operator|(
name|length
operator|-
name|offset
operator|)
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
name|ByteBuffer
name|bb
init|=
name|bbos
operator|.
name|getByteBuffer
argument_list|()
decl_stmt|;
name|is
operator|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|bb
operator|.
name|array
argument_list|()
argument_list|,
literal|0
argument_list|,
name|bb
operator|.
name|limit
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|is
operator|!=
literal|null
condition|)
name|is
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|bbos
operator|!=
literal|null
condition|)
name|bbos
operator|.
name|close
argument_list|()
expr_stmt|;
name|CodecPool
operator|.
name|returnDecompressor
argument_list|(
name|poolDecompressor
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|is
operator|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|cellBlock
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
return|return
name|codec
operator|.
name|getDecoder
argument_list|(
name|is
argument_list|)
return|;
block|}
comment|/**    * @param m Message to serialize delimited; i.e. w/ a vint of its size preceeding its    * serialization.    * @return The passed in Message serialized with delimiter.  Return null if<code>m</code> is null    * @throws IOException    */
specifier|public
specifier|static
name|ByteBuffer
name|getDelimitedMessageAsByteBuffer
parameter_list|(
specifier|final
name|Message
name|m
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|m
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|int
name|serializedSize
init|=
name|m
operator|.
name|getSerializedSize
argument_list|()
decl_stmt|;
name|int
name|vintSize
init|=
name|CodedOutputStream
operator|.
name|computeRawVarint32Size
argument_list|(
name|serializedSize
argument_list|)
decl_stmt|;
name|byte
index|[]
name|buffer
init|=
operator|new
name|byte
index|[
name|serializedSize
operator|+
name|vintSize
index|]
decl_stmt|;
comment|// Passing in a byte array saves COS creating a buffer which it does when using streams.
name|CodedOutputStream
name|cos
init|=
name|CodedOutputStream
operator|.
name|newInstance
argument_list|(
name|buffer
argument_list|)
decl_stmt|;
comment|// This will write out the vint preamble and the message serialized.
name|cos
operator|.
name|writeMessageNoTag
argument_list|(
name|m
argument_list|)
expr_stmt|;
name|cos
operator|.
name|flush
argument_list|()
expr_stmt|;
name|cos
operator|.
name|checkNoSpaceLeft
argument_list|()
expr_stmt|;
return|return
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|)
return|;
block|}
comment|/**    * Write out header, param, and cell block if there is one.    * @param dos    * @param header    * @param param    * @param cellBlock    * @return Total number of bytes written.    * @throws IOException    */
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
name|totalSize
operator|+=
name|cellBlock
operator|.
name|remaining
argument_list|()
expr_stmt|;
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
name|param
operator|.
name|writeDelimitedTo
argument_list|(
name|dos
argument_list|)
expr_stmt|;
if|if
condition|(
name|cellBlock
operator|!=
literal|null
condition|)
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
name|dos
operator|.
name|flush
argument_list|()
expr_stmt|;
return|return
name|totalSize
return|;
block|}
comment|/**    * Read in chunks of 8K (HBASE-7239)    * @param in    * @param dest    * @param offset    * @param len    * @throws IOException    */
specifier|public
specifier|static
name|void
name|readChunked
parameter_list|(
specifier|final
name|DataInput
name|in
parameter_list|,
name|byte
index|[]
name|dest
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|maxRead
init|=
literal|8192
decl_stmt|;
for|for
control|(
init|;
name|offset
operator|<
name|len
condition|;
name|offset
operator|+=
name|maxRead
control|)
block|{
name|in
operator|.
name|readFully
argument_list|(
name|dest
argument_list|,
name|offset
argument_list|,
name|Math
operator|.
name|min
argument_list|(
name|len
operator|-
name|offset
argument_list|,
name|maxRead
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
continue|continue;
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

