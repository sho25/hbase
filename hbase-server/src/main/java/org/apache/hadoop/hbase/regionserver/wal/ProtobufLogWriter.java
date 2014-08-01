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
name|IOException
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
name|FSDataOutputStream
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
name|protobuf
operator|.
name|generated
operator|.
name|WALProtos
operator|.
name|WALHeader
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
name|FSUtils
import|;
end_import

begin_comment
comment|/**  * Writer for protobuf-based WAL.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ProtobufLogWriter
extends|extends
name|WriterBase
block|{
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
name|FSDataOutputStream
name|output
decl_stmt|;
specifier|protected
name|Codec
operator|.
name|Encoder
name|cellEncoder
decl_stmt|;
specifier|protected
name|WALCellCodec
operator|.
name|ByteStringCompressor
name|compressor
decl_stmt|;
specifier|private
name|boolean
name|trailerWritten
decl_stmt|;
specifier|private
name|WALTrailer
name|trailer
decl_stmt|;
comment|// maximum size of the wal Trailer in bytes. If a user writes/reads a trailer with size larger
comment|// than this size, it is written/read respectively, with a WARN message in the log.
specifier|private
name|int
name|trailerWarnSize
decl_stmt|;
specifier|public
name|ProtobufLogWriter
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|WALCellCodec
name|getCodec
parameter_list|(
name|Configuration
name|conf
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
name|compressionContext
argument_list|)
return|;
block|}
specifier|protected
name|WALHeader
name|buildWALHeader
parameter_list|(
name|WALHeader
operator|.
name|Builder
name|builder
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|builder
operator|.
name|hasWriterClsName
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setWriterClsName
argument_list|(
name|ProtobufLogWriter
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
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
name|boolean
name|overwritable
parameter_list|)
throws|throws
name|IOException
block|{
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
name|overwritable
argument_list|)
expr_stmt|;
assert|assert
name|this
operator|.
name|output
operator|==
literal|null
assert|;
name|boolean
name|doCompress
init|=
name|initializeCompressionContext
argument_list|(
name|conf
argument_list|,
name|path
argument_list|)
decl_stmt|;
name|this
operator|.
name|trailerWarnSize
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HLog
operator|.
name|WAL_TRAILER_WARN_SIZE
argument_list|,
name|HLog
operator|.
name|DEFAULT_WAL_TRAILER_WARN_SIZE
argument_list|)
expr_stmt|;
name|int
name|bufferSize
init|=
name|FSUtils
operator|.
name|getDefaultBufferSize
argument_list|(
name|fs
argument_list|)
decl_stmt|;
name|short
name|replication
init|=
operator|(
name|short
operator|)
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.regionserver.hlog.replication"
argument_list|,
name|FSUtils
operator|.
name|getDefaultReplication
argument_list|(
name|fs
argument_list|,
name|path
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|blockSize
init|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.regionserver.hlog.blocksize"
argument_list|,
name|FSUtils
operator|.
name|getDefaultBlockSize
argument_list|(
name|fs
argument_list|,
name|path
argument_list|)
argument_list|)
decl_stmt|;
name|output
operator|=
name|fs
operator|.
name|createNonRecursive
argument_list|(
name|path
argument_list|,
name|overwritable
argument_list|,
name|bufferSize
argument_list|,
name|replication
argument_list|,
name|blockSize
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|output
operator|.
name|write
argument_list|(
name|ProtobufLogReader
operator|.
name|PB_WAL_MAGIC
argument_list|)
expr_stmt|;
name|boolean
name|doTagCompress
init|=
name|doCompress
operator|&&
name|conf
operator|.
name|getBoolean
argument_list|(
name|CompressionContext
operator|.
name|ENABLE_WAL_TAGS_COMPRESSION
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|buildWALHeader
argument_list|(
name|WALHeader
operator|.
name|newBuilder
argument_list|()
operator|.
name|setHasCompression
argument_list|(
name|doCompress
argument_list|)
operator|.
name|setHasTagCompression
argument_list|(
name|doTagCompress
argument_list|)
argument_list|)
operator|.
name|writeDelimitedTo
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|initAfterHeader
argument_list|(
name|doCompress
argument_list|)
expr_stmt|;
comment|// instantiate trailer to default value.
name|trailer
operator|=
name|WALTrailer
operator|.
name|newBuilder
argument_list|()
operator|.
name|build
argument_list|()
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
literal|"Initialized protobuf WAL="
operator|+
name|path
operator|+
literal|", compression="
operator|+
name|doCompress
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|initAfterHeader
parameter_list|(
name|boolean
name|doCompress
parameter_list|)
throws|throws
name|IOException
block|{
name|WALCellCodec
name|codec
init|=
name|getCodec
argument_list|(
name|conf
argument_list|,
name|this
operator|.
name|compressionContext
argument_list|)
decl_stmt|;
name|this
operator|.
name|cellEncoder
operator|=
name|codec
operator|.
name|getEncoder
argument_list|(
name|this
operator|.
name|output
argument_list|)
expr_stmt|;
if|if
condition|(
name|doCompress
condition|)
block|{
name|this
operator|.
name|compressor
operator|=
name|codec
operator|.
name|getByteStringCompressor
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|append
parameter_list|(
name|HLog
operator|.
name|Entry
name|entry
parameter_list|)
throws|throws
name|IOException
block|{
name|entry
operator|.
name|setCompressionContext
argument_list|(
name|compressionContext
argument_list|)
expr_stmt|;
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getBuilder
argument_list|(
name|compressor
argument_list|)
operator|.
name|setFollowingKvCount
argument_list|(
name|entry
operator|.
name|getEdit
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
operator|.
name|writeDelimitedTo
argument_list|(
name|output
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|entry
operator|.
name|getEdit
argument_list|()
operator|.
name|getKeyValues
argument_list|()
control|)
block|{
comment|// cellEncoder must assume little about the stream, since we write PB and cells in turn.
name|cellEncoder
operator|.
name|write
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
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
name|output
operator|!=
literal|null
condition|)
block|{
try|try
block|{
if|if
condition|(
operator|!
name|trailerWritten
condition|)
name|writeWALTrailer
argument_list|()
expr_stmt|;
name|this
operator|.
name|output
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|npe
parameter_list|)
block|{
comment|// Can get a NPE coming up from down in DFSClient$DFSOutputStream#close
name|LOG
operator|.
name|warn
argument_list|(
name|npe
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|output
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|protected
name|WALTrailer
name|buildWALTrailer
parameter_list|(
name|WALTrailer
operator|.
name|Builder
name|builder
parameter_list|)
block|{
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
name|void
name|writeWALTrailer
parameter_list|()
block|{
try|try
block|{
name|int
name|trailerSize
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|trailer
operator|==
literal|null
condition|)
block|{
comment|// use default trailer.
name|LOG
operator|.
name|warn
argument_list|(
literal|"WALTrailer is null. Continuing with default."
argument_list|)
expr_stmt|;
name|this
operator|.
name|trailer
operator|=
name|buildWALTrailer
argument_list|(
name|WALTrailer
operator|.
name|newBuilder
argument_list|()
argument_list|)
expr_stmt|;
name|trailerSize
operator|=
name|this
operator|.
name|trailer
operator|.
name|getSerializedSize
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|(
name|trailerSize
operator|=
name|this
operator|.
name|trailer
operator|.
name|getSerializedSize
argument_list|()
operator|)
operator|>
name|this
operator|.
name|trailerWarnSize
condition|)
block|{
comment|// continue writing after warning the user.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Please investigate WALTrailer usage. Trailer size> maximum size : "
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
name|this
operator|.
name|trailer
operator|.
name|writeTo
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeInt
argument_list|(
name|trailerSize
argument_list|)
expr_stmt|;
name|output
operator|.
name|write
argument_list|(
name|ProtobufLogReader
operator|.
name|PB_WAL_COMPLETE_MAGIC
argument_list|)
expr_stmt|;
name|this
operator|.
name|trailerWritten
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Got IOException while writing trailer"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|sync
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|this
operator|.
name|output
operator|.
name|flush
argument_list|()
expr_stmt|;
name|this
operator|.
name|output
operator|.
name|sync
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|npe
parameter_list|)
block|{
comment|// Concurrent close...
throw|throw
operator|new
name|IOException
argument_list|(
name|npe
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLength
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
return|return
name|this
operator|.
name|output
operator|.
name|getPos
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|npe
parameter_list|)
block|{
comment|// Concurrent close...
throw|throw
operator|new
name|IOException
argument_list|(
name|npe
argument_list|)
throw|;
block|}
block|}
specifier|public
name|FSDataOutputStream
name|getStream
parameter_list|()
block|{
return|return
name|this
operator|.
name|output
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setWALTrailer
parameter_list|(
name|WALTrailer
name|walTrailer
parameter_list|)
block|{
name|this
operator|.
name|trailer
operator|=
name|walTrailer
expr_stmt|;
block|}
block|}
end_class

end_unit

