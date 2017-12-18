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
name|compress
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
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|zip
operator|.
name|GZIPOutputStream
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
name|JVM
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
name|CompressorStream
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
name|zlib
operator|.
name|ZlibFactory
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Fixes an inefficiency in Hadoop's Gzip codec, allowing to reuse compression  * streams.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReusableStreamGzipCodec
extends|extends
name|GzipCodec
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|Compression
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * A bridge that wraps around a DeflaterOutputStream to make it a    * CompressionOutputStream.    */
specifier|protected
specifier|static
class|class
name|ReusableGzipOutputStream
extends|extends
name|CompressorStream
block|{
specifier|private
specifier|static
specifier|final
name|int
name|GZIP_HEADER_LENGTH
init|=
literal|10
decl_stmt|;
comment|/**      * Fixed ten-byte gzip header. See {@link GZIPOutputStream}'s source for      * details.      */
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|GZIP_HEADER
decl_stmt|;
static|static
block|{
comment|// Capture the fixed ten-byte header hard-coded in GZIPOutputStream.
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|byte
index|[]
name|header
init|=
literal|null
decl_stmt|;
name|GZIPOutputStream
name|gzipStream
init|=
literal|null
decl_stmt|;
try|try
block|{
name|gzipStream
operator|=
operator|new
name|GZIPOutputStream
argument_list|(
name|baos
argument_list|)
expr_stmt|;
name|gzipStream
operator|.
name|finish
argument_list|()
expr_stmt|;
name|header
operator|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|,
literal|0
argument_list|,
name|GZIP_HEADER_LENGTH
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
literal|"Could not create gzip stream"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|gzipStream
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|gzipStream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|GZIP_HEADER
operator|=
name|header
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|ResetableGZIPOutputStream
extends|extends
name|GZIPOutputStream
block|{
specifier|private
specifier|static
specifier|final
name|int
name|TRAILER_SIZE
init|=
literal|8
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|boolean
name|HAS_BROKEN_FINISH
init|=
name|JVM
operator|.
name|isGZIPOutputStreamFinishBroken
argument_list|()
decl_stmt|;
specifier|public
name|ResetableGZIPOutputStream
parameter_list|(
name|OutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|resetState
parameter_list|()
throws|throws
name|IOException
block|{
name|def
operator|.
name|reset
argument_list|()
expr_stmt|;
name|crc
operator|.
name|reset
argument_list|()
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|GZIP_HEADER
argument_list|)
expr_stmt|;
block|}
comment|/**        * Override because certain implementation calls def.end() which        * causes problem when resetting the stream for reuse.        */
annotation|@
name|Override
specifier|public
name|void
name|finish
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|HAS_BROKEN_FINISH
condition|)
block|{
if|if
condition|(
operator|!
name|def
operator|.
name|finished
argument_list|()
condition|)
block|{
name|def
operator|.
name|finish
argument_list|()
expr_stmt|;
while|while
condition|(
operator|!
name|def
operator|.
name|finished
argument_list|()
condition|)
block|{
name|int
name|i
init|=
name|def
operator|.
name|deflate
argument_list|(
name|this
operator|.
name|buf
argument_list|,
literal|0
argument_list|,
name|this
operator|.
name|buf
operator|.
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|def
operator|.
name|finished
argument_list|()
operator|)
operator|&&
operator|(
name|i
operator|<=
name|this
operator|.
name|buf
operator|.
name|length
operator|-
name|TRAILER_SIZE
operator|)
condition|)
block|{
name|writeTrailer
argument_list|(
name|this
operator|.
name|buf
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|i
operator|+=
name|TRAILER_SIZE
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|this
operator|.
name|buf
argument_list|,
literal|0
argument_list|,
name|i
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|this
operator|.
name|buf
argument_list|,
literal|0
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
name|byte
index|[]
name|arrayOfByte
init|=
operator|new
name|byte
index|[
name|TRAILER_SIZE
index|]
decl_stmt|;
name|writeTrailer
argument_list|(
name|arrayOfByte
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|arrayOfByte
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|super
operator|.
name|finish
argument_list|()
expr_stmt|;
block|}
block|}
comment|/** re-implement because the relative method in jdk is invisible */
specifier|private
name|void
name|writeTrailer
parameter_list|(
name|byte
index|[]
name|paramArrayOfByte
parameter_list|,
name|int
name|paramInt
parameter_list|)
throws|throws
name|IOException
block|{
name|writeInt
argument_list|(
operator|(
name|int
operator|)
name|this
operator|.
name|crc
operator|.
name|getValue
argument_list|()
argument_list|,
name|paramArrayOfByte
argument_list|,
name|paramInt
argument_list|)
expr_stmt|;
name|writeInt
argument_list|(
name|this
operator|.
name|def
operator|.
name|getTotalIn
argument_list|()
argument_list|,
name|paramArrayOfByte
argument_list|,
name|paramInt
operator|+
literal|4
argument_list|)
expr_stmt|;
block|}
comment|/** re-implement because the relative method in jdk is invisible */
specifier|private
name|void
name|writeInt
parameter_list|(
name|int
name|paramInt1
parameter_list|,
name|byte
index|[]
name|paramArrayOfByte
parameter_list|,
name|int
name|paramInt2
parameter_list|)
throws|throws
name|IOException
block|{
name|writeShort
argument_list|(
name|paramInt1
operator|&
literal|0xFFFF
argument_list|,
name|paramArrayOfByte
argument_list|,
name|paramInt2
argument_list|)
expr_stmt|;
name|writeShort
argument_list|(
name|paramInt1
operator|>>
literal|16
operator|&
literal|0xFFFF
argument_list|,
name|paramArrayOfByte
argument_list|,
name|paramInt2
operator|+
literal|2
argument_list|)
expr_stmt|;
block|}
comment|/** re-implement because the relative method in jdk is invisible */
specifier|private
name|void
name|writeShort
parameter_list|(
name|int
name|paramInt1
parameter_list|,
name|byte
index|[]
name|paramArrayOfByte
parameter_list|,
name|int
name|paramInt2
parameter_list|)
throws|throws
name|IOException
block|{
name|paramArrayOfByte
index|[
name|paramInt2
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|paramInt1
operator|&
literal|0xFF
argument_list|)
expr_stmt|;
name|paramArrayOfByte
index|[
operator|(
name|paramInt2
operator|+
literal|1
operator|)
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|paramInt1
operator|>>
literal|8
operator|&
literal|0xFF
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|ReusableGzipOutputStream
parameter_list|(
name|OutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
operator|new
name|ResetableGZIPOutputStream
argument_list|(
name|out
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|out
operator|.
name|close
argument_list|()
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
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|int
name|b
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|b
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
index|[]
name|data
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
name|out
operator|.
name|write
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|finish
parameter_list|()
throws|throws
name|IOException
block|{
operator|(
operator|(
name|GZIPOutputStream
operator|)
name|out
operator|)
operator|.
name|finish
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|resetState
parameter_list|()
throws|throws
name|IOException
block|{
operator|(
operator|(
name|ResetableGZIPOutputStream
operator|)
name|out
operator|)
operator|.
name|resetState
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|CompressionOutputStream
name|createOutputStream
parameter_list|(
name|OutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|ZlibFactory
operator|.
name|isNativeZlibLoaded
argument_list|(
name|getConf
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|super
operator|.
name|createOutputStream
argument_list|(
name|out
argument_list|)
return|;
block|}
return|return
operator|new
name|ReusableGzipOutputStream
argument_list|(
name|out
argument_list|)
return|;
block|}
block|}
end_class

end_unit

