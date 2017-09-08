begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|EOFException
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
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|List
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
name|fs
operator|.
name|Path
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|FSDataInputStream
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
name|LimitInputStream
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
name|shaded
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|generated
operator|.
name|WALProtos
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
name|protobuf
operator|.
name|generated
operator|.
name|WALProtos
operator|.
name|WALHeader
operator|.
name|Builder
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
name|protobuf
operator|.
name|generated
operator|.
name|WALProtos
operator|.
name|WALEdit
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
name|protobuf
operator|.
name|generated
operator|.
name|WALProtos
operator|.
name|WALTrailer
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
name|wal
operator|.
name|WAL
operator|.
name|Entry
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|CodedInputStream
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
import|;
end_import

begin_comment
comment|/**  * A Protobuf based WAL has the following structure:  *<p>  *&lt;PB_WAL_MAGIC&gt;&lt;WALHeader&gt;&lt;WALEdits&gt;...&lt;WALEdits&gt;&lt;Trailer&gt;  *&lt;TrailerSize&gt;&lt;PB_WAL_COMPLETE_MAGIC&gt;  *</p>  * The Reader reads meta information (WAL Compression state, WALTrailer, etc) in  * ProtobufLogReader#initReader(FSDataInputStream). A WALTrailer is an extensible structure  * which is appended at the end of the WAL. This is empty for now; it can contain some meta  * information such as Region level stats, etc in future.  */
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
name|ProtobufLogReader
extends|extends
name|ReaderBase
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ProtobufLogReader
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// public for WALFactory until we move everything to o.a.h.h.wal
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|PB_WAL_MAGIC
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"PWAL"
argument_list|)
decl_stmt|;
comment|// public for TestWALSplit
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|PB_WAL_COMPLETE_MAGIC
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"LAWP"
argument_list|)
decl_stmt|;
comment|/**    * Configuration name of WAL Trailer's warning size. If a waltrailer's size is greater than the    * configured size, providers should log a warning. e.g. this is used with Protobuf reader/writer.    */
specifier|static
specifier|final
name|String
name|WAL_TRAILER_WARN_SIZE
init|=
literal|"hbase.regionserver.waltrailer.warn.size"
decl_stmt|;
specifier|static
specifier|final
name|int
name|DEFAULT_WAL_TRAILER_WARN_SIZE
init|=
literal|1024
operator|*
literal|1024
decl_stmt|;
comment|// 1MB
specifier|protected
name|FSDataInputStream
name|inputStream
decl_stmt|;
specifier|protected
name|Codec
operator|.
name|Decoder
name|cellDecoder
decl_stmt|;
specifier|protected
name|WALCellCodec
operator|.
name|ByteStringUncompressor
name|byteStringUncompressor
decl_stmt|;
specifier|protected
name|boolean
name|hasCompression
init|=
literal|false
decl_stmt|;
specifier|protected
name|boolean
name|hasTagCompression
init|=
literal|false
decl_stmt|;
comment|// walEditsStopOffset is the position of the last byte to read. After reading the last WALEdit
comment|// entry in the wal, the inputstream's position is equal to walEditsStopOffset.
specifier|private
name|long
name|walEditsStopOffset
decl_stmt|;
specifier|private
name|boolean
name|trailerPresent
decl_stmt|;
specifier|protected
name|WALTrailer
name|trailer
decl_stmt|;
comment|// maximum size of the wal Trailer in bytes. If a user writes/reads a trailer with size larger
comment|// than this size, it is written/read respectively, with a WARN message in the log.
specifier|protected
name|int
name|trailerWarnSize
decl_stmt|;
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|writerClsNames
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
static|static
block|{
name|writerClsNames
operator|.
name|add
argument_list|(
name|ProtobufLogWriter
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|writerClsNames
operator|.
name|add
argument_list|(
name|AsyncProtobufLogWriter
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// cell codec classname
specifier|private
name|String
name|codecClsName
init|=
literal|null
decl_stmt|;
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|long
name|trailerSize
parameter_list|()
block|{
if|if
condition|(
name|trailerPresent
condition|)
block|{
comment|// sizeof PB_WAL_COMPLETE_MAGIC + sizof trailerSize + trailer
specifier|final
name|long
name|calculatedSize
init|=
name|PB_WAL_COMPLETE_MAGIC
operator|.
name|length
operator|+
name|Bytes
operator|.
name|SIZEOF_INT
operator|+
name|trailer
operator|.
name|getSerializedSize
argument_list|()
decl_stmt|;
specifier|final
name|long
name|expectedSize
init|=
name|fileLength
operator|-
name|walEditsStopOffset
decl_stmt|;
if|if
condition|(
name|expectedSize
operator|!=
name|calculatedSize
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"After parsing the trailer, we expect the total footer to be "
operator|+
name|expectedSize
operator|+
literal|" bytes, but we calculate it as being "
operator|+
name|calculatedSize
argument_list|)
expr_stmt|;
block|}
return|return
name|expectedSize
return|;
block|}
else|else
block|{
return|return
operator|-
literal|1L
return|;
block|}
block|}
enum|enum
name|WALHdrResult
block|{
name|EOF
block|,
comment|// stream is at EOF when method starts
name|SUCCESS
block|,
name|UNKNOWN_WRITER_CLS
comment|// name of writer class isn't recognized
block|}
comment|// context for WALHdr carrying information such as Cell Codec classname
specifier|static
class|class
name|WALHdrContext
block|{
name|WALHdrResult
name|result
decl_stmt|;
name|String
name|cellCodecClsName
decl_stmt|;
name|WALHdrContext
parameter_list|(
name|WALHdrResult
name|result
parameter_list|,
name|String
name|cellCodecClsName
parameter_list|)
block|{
name|this
operator|.
name|result
operator|=
name|result
expr_stmt|;
name|this
operator|.
name|cellCodecClsName
operator|=
name|cellCodecClsName
expr_stmt|;
block|}
name|WALHdrResult
name|getResult
parameter_list|()
block|{
return|return
name|result
return|;
block|}
name|String
name|getCellCodecClsName
parameter_list|()
block|{
return|return
name|cellCodecClsName
return|;
block|}
block|}
specifier|public
name|ProtobufLogReader
parameter_list|()
block|{
name|super
argument_list|()
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
if|if
condition|(
name|this
operator|.
name|inputStream
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|inputStream
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
name|inputStream
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|getPosition
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|inputStream
operator|.
name|getPos
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|clsName
init|=
name|initInternal
argument_list|(
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|initAfterCompression
argument_list|(
name|clsName
argument_list|)
expr_stmt|;
comment|// We need a new decoder (at least).
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|FSDataInputStream
name|stream
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|trailerWarnSize
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|WAL_TRAILER_WARN_SIZE
argument_list|,
name|DEFAULT_WAL_TRAILER_WARN_SIZE
argument_list|)
expr_stmt|;
name|super
operator|.
name|init
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|,
name|stream
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|initReader
parameter_list|(
name|FSDataInputStream
name|stream
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|initInternal
argument_list|(
name|stream
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/*    * Returns names of the accepted writer classes    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getWriterClsNames
parameter_list|()
block|{
return|return
name|writerClsNames
return|;
block|}
comment|/*    * Returns the cell codec classname    */
specifier|public
name|String
name|getCodecClsName
parameter_list|()
block|{
return|return
name|codecClsName
return|;
block|}
specifier|protected
name|WALHdrContext
name|readHeader
parameter_list|(
name|Builder
name|builder
parameter_list|,
name|FSDataInputStream
name|stream
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|res
init|=
name|builder
operator|.
name|mergeDelimitedFrom
argument_list|(
name|stream
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|res
condition|)
return|return
operator|new
name|WALHdrContext
argument_list|(
name|WALHdrResult
operator|.
name|EOF
argument_list|,
literal|null
argument_list|)
return|;
if|if
condition|(
name|builder
operator|.
name|hasWriterClsName
argument_list|()
operator|&&
operator|!
name|getWriterClsNames
argument_list|()
operator|.
name|contains
argument_list|(
name|builder
operator|.
name|getWriterClsName
argument_list|()
argument_list|)
condition|)
block|{
return|return
operator|new
name|WALHdrContext
argument_list|(
name|WALHdrResult
operator|.
name|UNKNOWN_WRITER_CLS
argument_list|,
literal|null
argument_list|)
return|;
block|}
name|String
name|clsName
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|builder
operator|.
name|hasCellCodecClsName
argument_list|()
condition|)
block|{
name|clsName
operator|=
name|builder
operator|.
name|getCellCodecClsName
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|WALHdrContext
argument_list|(
name|WALHdrResult
operator|.
name|SUCCESS
argument_list|,
name|clsName
argument_list|)
return|;
block|}
specifier|private
name|String
name|initInternal
parameter_list|(
name|FSDataInputStream
name|stream
parameter_list|,
name|boolean
name|isFirst
parameter_list|)
throws|throws
name|IOException
block|{
name|close
argument_list|()
expr_stmt|;
name|long
name|expectedPos
init|=
name|PB_WAL_MAGIC
operator|.
name|length
decl_stmt|;
if|if
condition|(
name|stream
operator|==
literal|null
condition|)
block|{
name|stream
operator|=
name|fs
operator|.
name|open
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|stream
operator|.
name|seek
argument_list|(
name|expectedPos
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|stream
operator|.
name|getPos
argument_list|()
operator|!=
name|expectedPos
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"The stream is at invalid position: "
operator|+
name|stream
operator|.
name|getPos
argument_list|()
argument_list|)
throw|;
block|}
comment|// Initialize metadata or, when we reset, just skip the header.
name|WALProtos
operator|.
name|WALHeader
operator|.
name|Builder
name|builder
init|=
name|WALProtos
operator|.
name|WALHeader
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|WALHdrContext
name|hdrCtxt
init|=
name|readHeader
argument_list|(
name|builder
argument_list|,
name|stream
argument_list|)
decl_stmt|;
name|WALHdrResult
name|walHdrRes
init|=
name|hdrCtxt
operator|.
name|getResult
argument_list|()
decl_stmt|;
if|if
condition|(
name|walHdrRes
operator|==
name|WALHdrResult
operator|.
name|EOF
condition|)
block|{
throw|throw
operator|new
name|EOFException
argument_list|(
literal|"Couldn't read WAL PB header"
argument_list|)
throw|;
block|}
if|if
condition|(
name|walHdrRes
operator|==
name|WALHdrResult
operator|.
name|UNKNOWN_WRITER_CLS
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Got unknown writer class: "
operator|+
name|builder
operator|.
name|getWriterClsName
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|isFirst
condition|)
block|{
name|WALProtos
operator|.
name|WALHeader
name|header
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|this
operator|.
name|hasCompression
operator|=
name|header
operator|.
name|hasHasCompression
argument_list|()
operator|&&
name|header
operator|.
name|getHasCompression
argument_list|()
expr_stmt|;
name|this
operator|.
name|hasTagCompression
operator|=
name|header
operator|.
name|hasHasTagCompression
argument_list|()
operator|&&
name|header
operator|.
name|getHasTagCompression
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|inputStream
operator|=
name|stream
expr_stmt|;
name|this
operator|.
name|walEditsStopOffset
operator|=
name|this
operator|.
name|fileLength
expr_stmt|;
name|long
name|currentPosition
init|=
name|stream
operator|.
name|getPos
argument_list|()
decl_stmt|;
name|trailerPresent
operator|=
name|setTrailerIfPresent
argument_list|()
expr_stmt|;
name|this
operator|.
name|seekOnFs
argument_list|(
name|currentPosition
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
name|LOG
operator|.
name|trace
argument_list|(
literal|"After reading the trailer: walEditsStopOffset: "
operator|+
name|this
operator|.
name|walEditsStopOffset
operator|+
literal|", fileLength: "
operator|+
name|this
operator|.
name|fileLength
operator|+
literal|", "
operator|+
literal|"trailerPresent: "
operator|+
operator|(
name|trailerPresent
condition|?
literal|"true, size: "
operator|+
name|trailer
operator|.
name|getSerializedSize
argument_list|()
else|:
literal|"false"
operator|)
operator|+
literal|", currentPosition: "
operator|+
name|currentPosition
argument_list|)
expr_stmt|;
block|}
name|codecClsName
operator|=
name|hdrCtxt
operator|.
name|getCellCodecClsName
argument_list|()
expr_stmt|;
return|return
name|hdrCtxt
operator|.
name|getCellCodecClsName
argument_list|()
return|;
block|}
comment|/**    * To check whether a trailer is present in a WAL, it seeks to position (fileLength -    * PB_WAL_COMPLETE_MAGIC.size() - Bytes.SIZEOF_INT). It reads the int value to know the size of    * the trailer, and checks whether the trailer is present at the end or not by comparing the last    * PB_WAL_COMPLETE_MAGIC.size() bytes. In case trailer is not present, it returns false;    * otherwise, sets the trailer and sets this.walEditsStopOffset variable up to the point just    * before the trailer.    *<ul>    * The trailer is ignored in case:    *<li>fileLength is 0 or not correct (when file is under recovery, etc).    *<li>the trailer size is negative.    *</ul>    *<p>    * In case the trailer size> this.trailerMaxSize, it is read after a WARN message.    * @return true if a valid trailer is present    * @throws IOException    */
specifier|private
name|boolean
name|setTrailerIfPresent
parameter_list|()
block|{
try|try
block|{
name|long
name|trailerSizeOffset
init|=
name|this
operator|.
name|fileLength
operator|-
operator|(
name|PB_WAL_COMPLETE_MAGIC
operator|.
name|length
operator|+
name|Bytes
operator|.
name|SIZEOF_INT
operator|)
decl_stmt|;
if|if
condition|(
name|trailerSizeOffset
operator|<=
literal|0
condition|)
return|return
literal|false
return|;
comment|// no trailer possible.
name|this
operator|.
name|seekOnFs
argument_list|(
name|trailerSizeOffset
argument_list|)
expr_stmt|;
comment|// read the int as trailer size.
name|int
name|trailerSize
init|=
name|this
operator|.
name|inputStream
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|ByteBuffer
name|buf
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|ProtobufLogReader
operator|.
name|PB_WAL_COMPLETE_MAGIC
operator|.
name|length
argument_list|)
decl_stmt|;
name|this
operator|.
name|inputStream
operator|.
name|readFully
argument_list|(
name|buf
operator|.
name|array
argument_list|()
argument_list|,
name|buf
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|buf
operator|.
name|capacity
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|buf
operator|.
name|array
argument_list|()
argument_list|,
name|PB_WAL_COMPLETE_MAGIC
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"No trailer found."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
name|trailerSize
operator|<
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Invalid trailer Size "
operator|+
name|trailerSize
operator|+
literal|", ignoring the trailer"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
elseif|else
if|if
condition|(
name|trailerSize
operator|>
name|this
operator|.
name|trailerWarnSize
condition|)
block|{
comment|// continue reading after warning the user.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Please investigate WALTrailer usage. Trailer size> maximum configured size : "
operator|+
name|trailerSize
operator|+
literal|"> "
operator|+
name|this
operator|.
name|trailerWarnSize
argument_list|)
expr_stmt|;
block|}
comment|// seek to the position where trailer starts.
name|long
name|positionOfTrailer
init|=
name|trailerSizeOffset
operator|-
name|trailerSize
decl_stmt|;
name|this
operator|.
name|seekOnFs
argument_list|(
name|positionOfTrailer
argument_list|)
expr_stmt|;
comment|// read the trailer.
name|buf
operator|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|trailerSize
argument_list|)
expr_stmt|;
comment|// for trailer.
name|this
operator|.
name|inputStream
operator|.
name|readFully
argument_list|(
name|buf
operator|.
name|array
argument_list|()
argument_list|,
name|buf
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|buf
operator|.
name|capacity
argument_list|()
argument_list|)
expr_stmt|;
name|trailer
operator|=
name|WALTrailer
operator|.
name|parseFrom
argument_list|(
name|buf
operator|.
name|array
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|walEditsStopOffset
operator|=
name|positionOfTrailer
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Got IOE while reading the trailer. Continuing as if no trailer is present."
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
specifier|protected
name|WALCellCodec
name|getCodec
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|cellCodecClsName
parameter_list|,
name|CompressionContext
name|compressionContext
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|WALCellCodec
operator|.
name|create
argument_list|(
name|conf
argument_list|,
name|cellCodecClsName
argument_list|,
name|compressionContext
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|initAfterCompression
parameter_list|()
throws|throws
name|IOException
block|{
name|initAfterCompression
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|initAfterCompression
parameter_list|(
name|String
name|cellCodecClsName
parameter_list|)
throws|throws
name|IOException
block|{
name|WALCellCodec
name|codec
init|=
name|getCodec
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|cellCodecClsName
argument_list|,
name|this
operator|.
name|compressionContext
argument_list|)
decl_stmt|;
name|this
operator|.
name|cellDecoder
operator|=
name|codec
operator|.
name|getDecoder
argument_list|(
name|this
operator|.
name|inputStream
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|hasCompression
condition|)
block|{
name|this
operator|.
name|byteStringUncompressor
operator|=
name|codec
operator|.
name|getByteStringUncompressor
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|hasCompression
parameter_list|()
block|{
return|return
name|this
operator|.
name|hasCompression
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|hasTagCompression
parameter_list|()
block|{
return|return
name|this
operator|.
name|hasTagCompression
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|readNext
parameter_list|(
name|Entry
name|entry
parameter_list|)
throws|throws
name|IOException
block|{
while|while
condition|(
literal|true
condition|)
block|{
comment|// OriginalPosition might be< 0 on local fs; if so, it is useless to us.
name|long
name|originalPosition
init|=
name|this
operator|.
name|inputStream
operator|.
name|getPos
argument_list|()
decl_stmt|;
if|if
condition|(
name|trailerPresent
operator|&&
name|originalPosition
operator|>
literal|0
operator|&&
name|originalPosition
operator|==
name|this
operator|.
name|walEditsStopOffset
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Reached end of expected edits area at offset "
operator|+
name|originalPosition
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
name|WALEdit
operator|.
name|Builder
name|builder
init|=
name|WALEdit
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|long
name|size
init|=
literal|0
decl_stmt|;
try|try
block|{
name|long
name|available
init|=
operator|-
literal|1
decl_stmt|;
try|try
block|{
name|int
name|firstByte
init|=
name|this
operator|.
name|inputStream
operator|.
name|read
argument_list|()
decl_stmt|;
if|if
condition|(
name|firstByte
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|EOFException
argument_list|(
literal|"First byte is negative at offset "
operator|+
name|originalPosition
argument_list|)
throw|;
block|}
name|size
operator|=
name|CodedInputStream
operator|.
name|readRawVarint32
argument_list|(
name|firstByte
argument_list|,
name|this
operator|.
name|inputStream
argument_list|)
expr_stmt|;
comment|// available may be< 0 on local fs for instance.  If so, can't depend on it.
name|available
operator|=
name|this
operator|.
name|inputStream
operator|.
name|available
argument_list|()
expr_stmt|;
if|if
condition|(
name|available
operator|>
literal|0
operator|&&
name|available
operator|<
name|size
condition|)
block|{
throw|throw
operator|new
name|EOFException
argument_list|(
literal|"Available stream not enough for edit, "
operator|+
literal|"inputStream.available()= "
operator|+
name|this
operator|.
name|inputStream
operator|.
name|available
argument_list|()
operator|+
literal|", "
operator|+
literal|"entry size= "
operator|+
name|size
operator|+
literal|" at offset = "
operator|+
name|this
operator|.
name|inputStream
operator|.
name|getPos
argument_list|()
argument_list|)
throw|;
block|}
name|ProtobufUtil
operator|.
name|mergeFrom
argument_list|(
name|builder
argument_list|,
operator|new
name|LimitInputStream
argument_list|(
name|this
operator|.
name|inputStream
argument_list|,
name|size
argument_list|)
argument_list|,
operator|(
name|int
operator|)
name|size
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|ipbe
parameter_list|)
block|{
throw|throw
operator|(
name|EOFException
operator|)
operator|new
name|EOFException
argument_list|(
literal|"Invalid PB, EOF? Ignoring; originalPosition="
operator|+
name|originalPosition
operator|+
literal|", currentPosition="
operator|+
name|this
operator|.
name|inputStream
operator|.
name|getPos
argument_list|()
operator|+
literal|", messageSize="
operator|+
name|size
operator|+
literal|", currentAvailable="
operator|+
name|available
argument_list|)
operator|.
name|initCause
argument_list|(
name|ipbe
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|builder
operator|.
name|isInitialized
argument_list|()
condition|)
block|{
comment|// TODO: not clear if we should try to recover from corrupt PB that looks semi-legit.
comment|//       If we can get the KV count, we could, theoretically, try to get next record.
throw|throw
operator|new
name|EOFException
argument_list|(
literal|"Partial PB while reading WAL, "
operator|+
literal|"probably an unexpected EOF, ignoring. current offset="
operator|+
name|this
operator|.
name|inputStream
operator|.
name|getPos
argument_list|()
argument_list|)
throw|;
block|}
name|WALEdit
name|walKey
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|readFieldsFromPb
argument_list|(
name|walKey
argument_list|,
name|this
operator|.
name|byteStringUncompressor
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|walKey
operator|.
name|hasFollowingKvCount
argument_list|()
operator|||
literal|0
operator|==
name|walKey
operator|.
name|getFollowingKvCount
argument_list|()
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"WALKey has no KVs that follow it; trying the next one. current offset="
operator|+
name|this
operator|.
name|inputStream
operator|.
name|getPos
argument_list|()
argument_list|)
expr_stmt|;
block|}
continue|continue;
block|}
name|int
name|expectedCells
init|=
name|walKey
operator|.
name|getFollowingKvCount
argument_list|()
decl_stmt|;
name|long
name|posBefore
init|=
name|this
operator|.
name|inputStream
operator|.
name|getPos
argument_list|()
decl_stmt|;
try|try
block|{
name|int
name|actualCells
init|=
name|entry
operator|.
name|getEdit
argument_list|()
operator|.
name|readFromCells
argument_list|(
name|cellDecoder
argument_list|,
name|expectedCells
argument_list|)
decl_stmt|;
if|if
condition|(
name|expectedCells
operator|!=
name|actualCells
condition|)
block|{
throw|throw
operator|new
name|EOFException
argument_list|(
literal|"Only read "
operator|+
name|actualCells
argument_list|)
throw|;
comment|// other info added in catch
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|String
name|posAfterStr
init|=
literal|"<unknown>"
decl_stmt|;
try|try
block|{
name|posAfterStr
operator|=
name|this
operator|.
name|inputStream
operator|.
name|getPos
argument_list|()
operator|+
literal|""
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Error getting pos for error message - ignoring"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
name|String
name|message
init|=
literal|" while reading "
operator|+
name|expectedCells
operator|+
literal|" WAL KVs; started reading at "
operator|+
name|posBefore
operator|+
literal|" and read up to "
operator|+
name|posAfterStr
decl_stmt|;
name|IOException
name|realEofEx
init|=
name|extractHiddenEof
argument_list|(
name|ex
argument_list|)
decl_stmt|;
throw|throw
operator|(
name|EOFException
operator|)
operator|new
name|EOFException
argument_list|(
literal|"EOF "
operator|+
name|message
argument_list|)
operator|.
name|initCause
argument_list|(
name|realEofEx
operator|!=
literal|null
condition|?
name|realEofEx
else|:
name|ex
argument_list|)
throw|;
block|}
if|if
condition|(
name|trailerPresent
operator|&&
name|this
operator|.
name|inputStream
operator|.
name|getPos
argument_list|()
operator|>
name|this
operator|.
name|walEditsStopOffset
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Read WALTrailer while reading WALEdits. wal: "
operator|+
name|this
operator|.
name|path
operator|+
literal|", inputStream.getPos(): "
operator|+
name|this
operator|.
name|inputStream
operator|.
name|getPos
argument_list|()
operator|+
literal|", walEditsStopOffset: "
operator|+
name|this
operator|.
name|walEditsStopOffset
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|EOFException
argument_list|(
literal|"Read WALTrailer while reading WALEdits"
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|EOFException
name|eof
parameter_list|)
block|{
comment|// If originalPosition is< 0, it is rubbish and we cannot use it (probably local fs)
if|if
condition|(
name|originalPosition
operator|<
literal|0
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Encountered a malformed edit, but can't seek back to last good position because originalPosition is negative. last offset="
operator|+
name|this
operator|.
name|inputStream
operator|.
name|getPos
argument_list|()
argument_list|,
name|eof
argument_list|)
expr_stmt|;
block|}
throw|throw
name|eof
throw|;
block|}
comment|// Else restore our position to original location in hope that next time through we will
comment|// read successfully.
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Encountered a malformed edit, seeking back to last good position in file, from "
operator|+
name|inputStream
operator|.
name|getPos
argument_list|()
operator|+
literal|" to "
operator|+
name|originalPosition
argument_list|,
name|eof
argument_list|)
expr_stmt|;
block|}
name|seekOnFs
argument_list|(
name|originalPosition
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
specifier|private
name|IOException
name|extractHiddenEof
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
comment|// There are two problems we are dealing with here. Hadoop stream throws generic exception
comment|// for EOF, not EOFException; and scanner further hides it inside RuntimeException.
name|IOException
name|ioEx
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|ex
operator|instanceof
name|EOFException
condition|)
block|{
return|return
operator|(
name|EOFException
operator|)
name|ex
return|;
block|}
elseif|else
if|if
condition|(
name|ex
operator|instanceof
name|IOException
condition|)
block|{
name|ioEx
operator|=
operator|(
name|IOException
operator|)
name|ex
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ex
operator|instanceof
name|RuntimeException
operator|&&
name|ex
operator|.
name|getCause
argument_list|()
operator|!=
literal|null
operator|&&
name|ex
operator|.
name|getCause
argument_list|()
operator|instanceof
name|IOException
condition|)
block|{
name|ioEx
operator|=
operator|(
name|IOException
operator|)
name|ex
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|ioEx
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|ioEx
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"EOF"
argument_list|)
condition|)
return|return
name|ioEx
return|;
return|return
literal|null
return|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|seekOnFs
parameter_list|(
name|long
name|pos
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|inputStream
operator|.
name|seek
argument_list|(
name|pos
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

