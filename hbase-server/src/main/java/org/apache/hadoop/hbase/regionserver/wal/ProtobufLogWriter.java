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
implements|implements
name|HLog
operator|.
name|Writer
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
specifier|private
name|FSDataOutputStream
name|output
decl_stmt|;
specifier|private
name|Codec
operator|.
name|Encoder
name|cellEncoder
decl_stmt|;
specifier|private
name|WALCellCodec
operator|.
name|ByteStringCompressor
name|compressor
decl_stmt|;
comment|/** Context used by our wal dictionary compressor.    * Null if we're not to do our custom dictionary compression. */
specifier|private
name|CompressionContext
name|compressionContext
decl_stmt|;
specifier|public
name|ProtobufLogWriter
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
parameter_list|)
throws|throws
name|IOException
block|{
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
name|conf
operator|.
name|getBoolean
argument_list|(
name|HConstants
operator|.
name|ENABLE_WAL_COMPRESSION
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|doCompress
condition|)
block|{
try|try
block|{
name|this
operator|.
name|compressionContext
operator|=
operator|new
name|CompressionContext
argument_list|(
name|LRUDictionary
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to initiate CompressionContext"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
name|int
name|bufferSize
init|=
name|fs
operator|.
name|getConf
argument_list|()
operator|.
name|getInt
argument_list|(
literal|"io.file.buffer.size"
argument_list|,
literal|4096
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
name|fs
operator|.
name|getDefaultReplication
argument_list|()
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
name|fs
operator|.
name|getDefaultBlockSize
argument_list|()
argument_list|)
decl_stmt|;
name|output
operator|=
name|fs
operator|.
name|create
argument_list|(
name|path
argument_list|,
literal|true
argument_list|,
name|bufferSize
argument_list|,
name|replication
argument_list|,
name|blockSize
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
name|build
argument_list|()
operator|.
name|writeDelimitedTo
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|WALCellCodec
name|codec
init|=
operator|new
name|WALCellCodec
argument_list|(
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
name|LOG
operator|.
name|debug
argument_list|(
literal|"Writing protobuf WAL; path="
operator|+
name|path
operator|+
literal|", compression="
operator|+
name|doCompress
argument_list|)
expr_stmt|;
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
block|}
end_class

end_unit

