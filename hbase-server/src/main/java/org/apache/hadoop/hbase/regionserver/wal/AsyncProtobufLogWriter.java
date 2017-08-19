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
name|regionserver
operator|.
name|wal
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Throwables
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
name|channel
operator|.
name|Channel
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
name|channel
operator|.
name|EventLoop
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
name|InterruptedIOException
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
name|concurrent
operator|.
name|CompletableFuture
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Consumer
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
name|Cell
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
name|asyncfs
operator|.
name|AsyncFSOutput
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
name|asyncfs
operator|.
name|AsyncFSOutputHelper
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
name|wal
operator|.
name|AsyncFSWALProvider
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

begin_comment
comment|/**  * AsyncWriter for protobuf-based WAL.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|AsyncProtobufLogWriter
extends|extends
name|AbstractProtobufLogWriter
implements|implements
name|AsyncFSWALProvider
operator|.
name|AsyncWriter
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
name|AsyncProtobufLogWriter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|EventLoop
name|eventLoop
decl_stmt|;
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|Channel
argument_list|>
name|channelClass
decl_stmt|;
specifier|private
name|AsyncFSOutput
name|output
decl_stmt|;
specifier|private
specifier|static
specifier|final
class|class
name|OutputStreamWrapper
extends|extends
name|OutputStream
implements|implements
name|ByteBufferWriter
block|{
specifier|private
specifier|final
name|AsyncFSOutput
name|out
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|oneByteBuf
init|=
operator|new
name|byte
index|[
literal|1
index|]
decl_stmt|;
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
name|oneByteBuf
index|[
literal|0
index|]
operator|=
operator|(
name|byte
operator|)
name|b
expr_stmt|;
name|write
argument_list|(
name|oneByteBuf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|OutputStreamWrapper
parameter_list|(
name|AsyncFSOutput
name|out
parameter_list|)
block|{
name|this
operator|.
name|out
operator|=
name|out
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|ByteBuffer
name|b
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
name|ByteBuffer
name|bb
init|=
name|b
operator|.
name|duplicate
argument_list|()
decl_stmt|;
name|bb
operator|.
name|position
argument_list|(
name|off
argument_list|)
expr_stmt|;
name|bb
operator|.
name|limit
argument_list|(
name|off
operator|+
name|len
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|bb
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeInt
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|i
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
name|b
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
block|}
specifier|private
name|OutputStream
name|asyncOutputWrapper
decl_stmt|;
specifier|public
name|AsyncProtobufLogWriter
parameter_list|(
name|EventLoop
name|eventLoop
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Channel
argument_list|>
name|channelClass
parameter_list|)
block|{
name|this
operator|.
name|eventLoop
operator|=
name|eventLoop
expr_stmt|;
name|this
operator|.
name|channelClass
operator|=
name|channelClass
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|append
parameter_list|(
name|Entry
name|entry
parameter_list|)
block|{
name|int
name|buffered
init|=
name|output
operator|.
name|buffered
argument_list|()
decl_stmt|;
name|entry
operator|.
name|setCompressionContext
argument_list|(
name|compressionContext
argument_list|)
expr_stmt|;
try|try
block|{
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
name|asyncOutputWrapper
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
name|AssertionError
argument_list|(
literal|"should not happen"
argument_list|,
name|e
argument_list|)
throw|;
block|}
try|try
block|{
for|for
control|(
name|Cell
name|cell
range|:
name|entry
operator|.
name|getEdit
argument_list|()
operator|.
name|getCells
argument_list|()
control|)
block|{
name|cellEncoder
operator|.
name|write
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"should not happen"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|length
operator|.
name|addAndGet
argument_list|(
name|output
operator|.
name|buffered
argument_list|()
operator|-
name|buffered
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|Long
argument_list|>
name|sync
parameter_list|()
block|{
return|return
name|output
operator|.
name|flush
argument_list|(
literal|false
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
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
operator|==
literal|null
condition|)
block|{
return|return;
block|}
try|try
block|{
name|writeWALTrailer
argument_list|()
expr_stmt|;
name|output
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"normal close failed, try recover"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|output
operator|.
name|recoverAndClose
argument_list|(
literal|null
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
specifier|public
name|AsyncFSOutput
name|getOutput
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
specifier|protected
name|void
name|initOutput
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|boolean
name|overwritable
parameter_list|,
name|int
name|bufferSize
parameter_list|,
name|short
name|replication
parameter_list|,
name|long
name|blockSize
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|output
operator|=
name|AsyncFSOutputHelper
operator|.
name|createOutput
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|overwritable
argument_list|,
literal|false
argument_list|,
name|replication
argument_list|,
name|blockSize
argument_list|,
name|eventLoop
argument_list|,
name|channelClass
argument_list|)
expr_stmt|;
name|this
operator|.
name|asyncOutputWrapper
operator|=
operator|new
name|OutputStreamWrapper
argument_list|(
name|output
argument_list|)
expr_stmt|;
block|}
specifier|private
name|long
name|write
parameter_list|(
name|Consumer
argument_list|<
name|CompletableFuture
argument_list|<
name|Long
argument_list|>
argument_list|>
name|action
parameter_list|)
throws|throws
name|IOException
block|{
name|CompletableFuture
argument_list|<
name|Long
argument_list|>
name|future
init|=
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
decl_stmt|;
name|eventLoop
operator|.
name|execute
argument_list|(
parameter_list|()
lambda|->
name|action
operator|.
name|accept
argument_list|(
name|future
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
return|return
name|future
operator|.
name|get
argument_list|()
operator|.
name|longValue
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|InterruptedIOException
name|ioe
init|=
operator|new
name|InterruptedIOException
argument_list|()
decl_stmt|;
name|ioe
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
name|ioe
throw|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
name|Throwables
operator|.
name|propagateIfPossible
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|,
name|IOException
operator|.
name|class
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|long
name|writeMagicAndWALHeader
parameter_list|(
name|byte
index|[]
name|magic
parameter_list|,
name|WALHeader
name|header
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|write
argument_list|(
name|future
lambda|->
block|{
name|output
operator|.
name|write
argument_list|(
name|magic
argument_list|)
expr_stmt|;
try|try
block|{
name|header
operator|.
name|writeDelimitedTo
argument_list|(
name|asyncOutputWrapper
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// should not happen
throw|throw
operator|new
name|AssertionError
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|output
operator|.
name|flush
argument_list|(
literal|false
argument_list|)
operator|.
name|whenComplete
argument_list|(
parameter_list|(
name|len
parameter_list|,
name|error
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
name|error
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|future
operator|.
name|complete
argument_list|(
name|len
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|long
name|writeWALTrailerAndMagic
parameter_list|(
name|WALTrailer
name|trailer
parameter_list|,
name|byte
index|[]
name|magic
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|write
argument_list|(
name|future
lambda|->
block|{
try|try
block|{
name|trailer
operator|.
name|writeTo
argument_list|(
name|asyncOutputWrapper
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// should not happen
throw|throw
operator|new
name|AssertionError
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|output
operator|.
name|writeInt
argument_list|(
name|trailer
operator|.
name|getSerializedSize
argument_list|()
argument_list|)
expr_stmt|;
name|output
operator|.
name|write
argument_list|(
name|magic
argument_list|)
expr_stmt|;
name|output
operator|.
name|flush
argument_list|(
literal|false
argument_list|)
operator|.
name|whenComplete
argument_list|(
parameter_list|(
name|len
parameter_list|,
name|error
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
name|error
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|future
operator|.
name|complete
argument_list|(
name|len
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|OutputStream
name|getOutputStreamForCellEncoder
parameter_list|()
block|{
return|return
name|asyncOutputWrapper
return|;
block|}
block|}
end_class

end_unit

