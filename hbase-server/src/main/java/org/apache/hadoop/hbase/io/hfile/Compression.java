begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations under  * the License.  */
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
name|hfile
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FilterOutputStream
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
name|CompressionOutputStream
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
name|GzipCodec
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
name|DefaultCodec
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * Compression related stuff.  * Copied from hadoop-3315 tfile.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|Compression
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|Compression
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Prevent the instantiation of class.    */
specifier|private
name|Compression
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|static
class|class
name|FinishOnFlushCompressionStream
extends|extends
name|FilterOutputStream
block|{
specifier|public
name|FinishOnFlushCompressionStream
parameter_list|(
name|CompressionOutputStream
name|cout
parameter_list|)
block|{
name|super
argument_list|(
name|cout
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|byte
name|b
index|[]
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|b
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|flush
parameter_list|()
throws|throws
name|IOException
block|{
name|CompressionOutputStream
name|cout
init|=
operator|(
name|CompressionOutputStream
operator|)
name|out
decl_stmt|;
name|cout
operator|.
name|finish
argument_list|()
expr_stmt|;
name|cout
operator|.
name|flush
argument_list|()
expr_stmt|;
name|cout
operator|.
name|resetState
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Returns the classloader to load the Codec class from.    * @return    */
specifier|private
specifier|static
name|ClassLoader
name|getClassLoaderForCodec
parameter_list|()
block|{
name|ClassLoader
name|cl
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getContextClassLoader
argument_list|()
decl_stmt|;
if|if
condition|(
name|cl
operator|==
literal|null
condition|)
block|{
name|cl
operator|=
name|Compression
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|cl
operator|==
literal|null
condition|)
block|{
name|cl
operator|=
name|ClassLoader
operator|.
name|getSystemClassLoader
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|cl
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"A ClassLoader to load the Codec could not be determined"
argument_list|)
throw|;
block|}
return|return
name|cl
return|;
block|}
comment|/**    * Compression algorithms. The ordinal of these cannot change or else you    * risk breaking all existing HFiles out there.  Even the ones that are    * not compressed! (They use the NONE algorithm)    */
specifier|public
specifier|static
enum|enum
name|Algorithm
block|{
name|LZO
argument_list|(
literal|"lzo"
argument_list|)
block|{
comment|// Use base type to avoid compile-time dependencies.
specifier|private
specifier|transient
name|CompressionCodec
name|lzoCodec
decl_stmt|;
annotation|@
name|Override
name|CompressionCodec
name|getCodec
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|lzoCodec
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|externalCodec
init|=
name|getClassLoaderForCodec
argument_list|()
operator|.
name|loadClass
argument_list|(
literal|"com.hadoop.compression.lzo.LzoCodec"
argument_list|)
decl_stmt|;
name|lzoCodec
operator|=
operator|(
name|CompressionCodec
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|externalCodec
argument_list|,
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|lzoCodec
return|;
block|}
block|}
block|,
name|GZ
argument_list|(
literal|"gz"
argument_list|)
block|{
specifier|private
specifier|transient
name|GzipCodec
name|codec
decl_stmt|;
annotation|@
name|Override
name|DefaultCodec
name|getCodec
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|codec
operator|==
literal|null
condition|)
block|{
name|codec
operator|=
operator|new
name|ReusableStreamGzipCodec
argument_list|()
expr_stmt|;
name|codec
operator|.
name|setConf
argument_list|(
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|codec
return|;
block|}
block|}
block|,
name|NONE
argument_list|(
literal|"none"
argument_list|)
block|{
annotation|@
name|Override
name|DefaultCodec
name|getCodec
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|InputStream
name|createDecompressionStream
parameter_list|(
name|InputStream
name|downStream
parameter_list|,
name|Decompressor
name|decompressor
parameter_list|,
name|int
name|downStreamBufferSize
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|downStreamBufferSize
operator|>
literal|0
condition|)
block|{
return|return
operator|new
name|BufferedInputStream
argument_list|(
name|downStream
argument_list|,
name|downStreamBufferSize
argument_list|)
return|;
block|}
comment|// else {
comment|// Make sure we bypass FSInputChecker buffer.
comment|// return new BufferedInputStream(downStream, 1024);
comment|// }
comment|// }
return|return
name|downStream
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|OutputStream
name|createCompressionStream
parameter_list|(
name|OutputStream
name|downStream
parameter_list|,
name|Compressor
name|compressor
parameter_list|,
name|int
name|downStreamBufferSize
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|downStreamBufferSize
operator|>
literal|0
condition|)
block|{
return|return
operator|new
name|BufferedOutputStream
argument_list|(
name|downStream
argument_list|,
name|downStreamBufferSize
argument_list|)
return|;
block|}
return|return
name|downStream
return|;
block|}
block|}
block|,
name|SNAPPY
argument_list|(
literal|"snappy"
argument_list|)
block|{
comment|// Use base type to avoid compile-time dependencies.
specifier|private
specifier|transient
name|CompressionCodec
name|snappyCodec
decl_stmt|;
annotation|@
name|Override
name|CompressionCodec
name|getCodec
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|snappyCodec
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|externalCodec
init|=
name|getClassLoaderForCodec
argument_list|()
operator|.
name|loadClass
argument_list|(
literal|"org.apache.hadoop.io.compress.SnappyCodec"
argument_list|)
decl_stmt|;
name|snappyCodec
operator|=
operator|(
name|CompressionCodec
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|externalCodec
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|snappyCodec
return|;
block|}
block|}
block|,
name|LZ4
argument_list|(
literal|"lz4"
argument_list|)
block|{
comment|// Use base type to avoid compile-time dependencies.
specifier|private
specifier|transient
name|CompressionCodec
name|lz4Codec
decl_stmt|;
annotation|@
name|Override
name|CompressionCodec
name|getCodec
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|lz4Codec
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|externalCodec
init|=
name|getClassLoaderForCodec
argument_list|()
operator|.
name|loadClass
argument_list|(
literal|"org.apache.hadoop.io.compress.Lz4Codec"
argument_list|)
decl_stmt|;
name|lz4Codec
operator|=
operator|(
name|CompressionCodec
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|externalCodec
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|lz4Codec
return|;
block|}
block|}
block|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|String
name|compressName
decl_stmt|;
comment|// data input buffer size to absorb small reads from application.
specifier|private
specifier|static
specifier|final
name|int
name|DATA_IBUF_SIZE
init|=
literal|1
operator|*
literal|1024
decl_stmt|;
comment|// data output buffer size to absorb small writes from application.
specifier|private
specifier|static
specifier|final
name|int
name|DATA_OBUF_SIZE
init|=
literal|4
operator|*
literal|1024
decl_stmt|;
name|Algorithm
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
operator|new
name|Configuration
argument_list|()
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hadoop.native.lib"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|compressName
operator|=
name|name
expr_stmt|;
block|}
specifier|abstract
name|CompressionCodec
name|getCodec
parameter_list|(
name|Configuration
name|conf
parameter_list|)
function_decl|;
specifier|public
name|InputStream
name|createDecompressionStream
parameter_list|(
name|InputStream
name|downStream
parameter_list|,
name|Decompressor
name|decompressor
parameter_list|,
name|int
name|downStreamBufferSize
parameter_list|)
throws|throws
name|IOException
block|{
name|CompressionCodec
name|codec
init|=
name|getCodec
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// Set the internal buffer size to read from down stream.
if|if
condition|(
name|downStreamBufferSize
operator|>
literal|0
condition|)
block|{
operator|(
operator|(
name|Configurable
operator|)
name|codec
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"io.file.buffer.size"
argument_list|,
name|downStreamBufferSize
argument_list|)
expr_stmt|;
block|}
name|CompressionInputStream
name|cis
init|=
name|codec
operator|.
name|createInputStream
argument_list|(
name|downStream
argument_list|,
name|decompressor
argument_list|)
decl_stmt|;
name|BufferedInputStream
name|bis2
init|=
operator|new
name|BufferedInputStream
argument_list|(
name|cis
argument_list|,
name|DATA_IBUF_SIZE
argument_list|)
decl_stmt|;
return|return
name|bis2
return|;
block|}
specifier|public
name|OutputStream
name|createCompressionStream
parameter_list|(
name|OutputStream
name|downStream
parameter_list|,
name|Compressor
name|compressor
parameter_list|,
name|int
name|downStreamBufferSize
parameter_list|)
throws|throws
name|IOException
block|{
name|OutputStream
name|bos1
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|downStreamBufferSize
operator|>
literal|0
condition|)
block|{
name|bos1
operator|=
operator|new
name|BufferedOutputStream
argument_list|(
name|downStream
argument_list|,
name|downStreamBufferSize
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|bos1
operator|=
name|downStream
expr_stmt|;
block|}
name|CompressionOutputStream
name|cos
init|=
name|createPlainCompressionStream
argument_list|(
name|bos1
argument_list|,
name|compressor
argument_list|)
decl_stmt|;
name|BufferedOutputStream
name|bos2
init|=
operator|new
name|BufferedOutputStream
argument_list|(
operator|new
name|FinishOnFlushCompressionStream
argument_list|(
name|cos
argument_list|)
argument_list|,
name|DATA_OBUF_SIZE
argument_list|)
decl_stmt|;
return|return
name|bos2
return|;
block|}
comment|/**      * Creates a compression stream without any additional wrapping into      * buffering streams.      */
specifier|public
name|CompressionOutputStream
name|createPlainCompressionStream
parameter_list|(
name|OutputStream
name|downStream
parameter_list|,
name|Compressor
name|compressor
parameter_list|)
throws|throws
name|IOException
block|{
name|CompressionCodec
name|codec
init|=
name|getCodec
argument_list|(
name|conf
argument_list|)
decl_stmt|;
operator|(
operator|(
name|Configurable
operator|)
name|codec
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"io.file.buffer.size"
argument_list|,
literal|32
operator|*
literal|1024
argument_list|)
expr_stmt|;
return|return
name|codec
operator|.
name|createOutputStream
argument_list|(
name|downStream
argument_list|,
name|compressor
argument_list|)
return|;
block|}
specifier|public
name|Compressor
name|getCompressor
parameter_list|()
block|{
name|CompressionCodec
name|codec
init|=
name|getCodec
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|codec
operator|!=
literal|null
condition|)
block|{
name|Compressor
name|compressor
init|=
name|CodecPool
operator|.
name|getCompressor
argument_list|(
name|codec
argument_list|)
decl_stmt|;
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
operator|.
name|finished
argument_list|()
condition|)
block|{
comment|// Somebody returns the compressor to CodecPool but is still using
comment|// it.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Compressor obtained from CodecPool is already finished()"
argument_list|)
expr_stmt|;
comment|// throw new AssertionError(
comment|// "Compressor obtained from CodecPool is already finished()");
block|}
name|compressor
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
return|return
name|compressor
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|void
name|returnCompressor
parameter_list|(
name|Compressor
name|compressor
parameter_list|)
block|{
if|if
condition|(
name|compressor
operator|!=
literal|null
condition|)
block|{
name|CodecPool
operator|.
name|returnCompressor
argument_list|(
name|compressor
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Decompressor
name|getDecompressor
parameter_list|()
block|{
name|CompressionCodec
name|codec
init|=
name|getCodec
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|codec
operator|!=
literal|null
condition|)
block|{
name|Decompressor
name|decompressor
init|=
name|CodecPool
operator|.
name|getDecompressor
argument_list|(
name|codec
argument_list|)
decl_stmt|;
if|if
condition|(
name|decompressor
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|decompressor
operator|.
name|finished
argument_list|()
condition|)
block|{
comment|// Somebody returns the decompressor to CodecPool but is still using
comment|// it.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Deompressor obtained from CodecPool is already finished()"
argument_list|)
expr_stmt|;
comment|// throw new AssertionError(
comment|// "Decompressor obtained from CodecPool is already finished()");
block|}
name|decompressor
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
return|return
name|decompressor
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|void
name|returnDecompressor
parameter_list|(
name|Decompressor
name|decompressor
parameter_list|)
block|{
if|if
condition|(
name|decompressor
operator|!=
literal|null
condition|)
block|{
name|CodecPool
operator|.
name|returnDecompressor
argument_list|(
name|decompressor
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|compressName
return|;
block|}
block|}
specifier|public
specifier|static
name|Algorithm
name|getCompressionAlgorithmByName
parameter_list|(
name|String
name|compressName
parameter_list|)
block|{
name|Algorithm
index|[]
name|algos
init|=
name|Algorithm
operator|.
name|class
operator|.
name|getEnumConstants
argument_list|()
decl_stmt|;
for|for
control|(
name|Algorithm
name|a
range|:
name|algos
control|)
block|{
if|if
condition|(
name|a
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|compressName
argument_list|)
condition|)
block|{
return|return
name|a
return|;
block|}
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unsupported compression algorithm name: "
operator|+
name|compressName
argument_list|)
throw|;
block|}
specifier|static
name|String
index|[]
name|getSupportedAlgorithms
parameter_list|()
block|{
name|Algorithm
index|[]
name|algos
init|=
name|Algorithm
operator|.
name|class
operator|.
name|getEnumConstants
argument_list|()
decl_stmt|;
name|String
index|[]
name|ret
init|=
operator|new
name|String
index|[
name|algos
operator|.
name|length
index|]
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Algorithm
name|a
range|:
name|algos
control|)
block|{
name|ret
index|[
name|i
operator|++
index|]
operator|=
name|a
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
comment|/**    * Decompresses data from the given stream using the configured compression    * algorithm. It will throw an exception if the dest buffer does not have    * enough space to hold the decompressed data.    *    * @param dest    *          the output bytes buffer    * @param destOffset    *          start writing position of the output buffer    * @param bufferedBoundedStream    *          a stream to read compressed data from, bounded to the exact amount    *          of compressed data    * @param compressedSize    *          compressed data size, header not included    * @param uncompressedSize    *          uncompressed data size, header not included    * @param compressAlgo    *          compression algorithm used    * @throws IOException    */
specifier|public
specifier|static
name|void
name|decompress
parameter_list|(
name|byte
index|[]
name|dest
parameter_list|,
name|int
name|destOffset
parameter_list|,
name|InputStream
name|bufferedBoundedStream
parameter_list|,
name|int
name|compressedSize
parameter_list|,
name|int
name|uncompressedSize
parameter_list|,
name|Compression
operator|.
name|Algorithm
name|compressAlgo
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|dest
operator|.
name|length
operator|-
name|destOffset
operator|<
name|uncompressedSize
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Output buffer does not have enough space to hold "
operator|+
name|uncompressedSize
operator|+
literal|" decompressed bytes, available: "
operator|+
operator|(
name|dest
operator|.
name|length
operator|-
name|destOffset
operator|)
argument_list|)
throw|;
block|}
name|Decompressor
name|decompressor
init|=
literal|null
decl_stmt|;
try|try
block|{
name|decompressor
operator|=
name|compressAlgo
operator|.
name|getDecompressor
argument_list|()
expr_stmt|;
name|InputStream
name|is
init|=
name|compressAlgo
operator|.
name|createDecompressionStream
argument_list|(
name|bufferedBoundedStream
argument_list|,
name|decompressor
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|IOUtils
operator|.
name|readFully
argument_list|(
name|is
argument_list|,
name|dest
argument_list|,
name|destOffset
argument_list|,
name|uncompressedSize
argument_list|)
expr_stmt|;
name|is
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|decompressor
operator|!=
literal|null
condition|)
block|{
name|compressAlgo
operator|.
name|returnDecompressor
argument_list|(
name|decompressor
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

