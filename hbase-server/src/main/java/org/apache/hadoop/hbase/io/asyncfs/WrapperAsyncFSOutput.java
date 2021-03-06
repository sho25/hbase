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
name|io
operator|.
name|asyncfs
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
name|ExecutorService
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
name|Executors
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
name|io
operator|.
name|ByteArrayOutputStream
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
name|CancelableProgressable
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
name|hdfs
operator|.
name|protocol
operator|.
name|DatanodeInfo
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
name|hbase
operator|.
name|thirdparty
operator|.
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
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
import|;
end_import

begin_comment
comment|/**  * An {@link AsyncFSOutput} wraps a {@link FSDataOutputStream}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|WrapperAsyncFSOutput
implements|implements
name|AsyncFSOutput
block|{
specifier|private
specifier|final
name|FSDataOutputStream
name|out
decl_stmt|;
specifier|private
name|ByteArrayOutputStream
name|buffer
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ExecutorService
name|executor
decl_stmt|;
specifier|public
name|WrapperAsyncFSOutput
parameter_list|(
name|Path
name|file
parameter_list|,
name|FSDataOutputStream
name|out
parameter_list|)
block|{
name|this
operator|.
name|out
operator|=
name|out
expr_stmt|;
name|this
operator|.
name|executor
operator|=
name|Executors
operator|.
name|newSingleThreadExecutor
argument_list|(
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|setNameFormat
argument_list|(
literal|"AsyncFSOutputFlusher-"
operator|+
name|file
operator|.
name|toString
argument_list|()
operator|.
name|replace
argument_list|(
literal|"%"
argument_list|,
literal|"%%"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
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
parameter_list|)
block|{
name|write
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|length
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
block|{
name|buffer
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
name|writeInt
parameter_list|(
name|int
name|i
parameter_list|)
block|{
name|buffer
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
name|ByteBuffer
name|bb
parameter_list|)
block|{
name|buffer
operator|.
name|write
argument_list|(
name|bb
argument_list|,
name|bb
operator|.
name|position
argument_list|()
argument_list|,
name|bb
operator|.
name|remaining
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|buffered
parameter_list|()
block|{
return|return
name|buffer
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|DatanodeInfo
index|[]
name|getPipeline
parameter_list|()
block|{
return|return
operator|new
name|DatanodeInfo
index|[
literal|0
index|]
return|;
block|}
specifier|private
name|void
name|flush0
parameter_list|(
name|CompletableFuture
argument_list|<
name|Long
argument_list|>
name|future
parameter_list|,
name|ByteArrayOutputStream
name|buffer
parameter_list|,
name|boolean
name|sync
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|buffer
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|buffer
operator|.
name|getBuffer
argument_list|()
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|sync
condition|)
block|{
name|out
operator|.
name|hsync
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|hflush
argument_list|()
expr_stmt|;
block|}
block|}
name|future
operator|.
name|complete
argument_list|(
name|out
operator|.
name|getPos
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|Long
argument_list|>
name|flush
parameter_list|(
name|boolean
name|sync
parameter_list|)
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
name|ByteArrayOutputStream
name|buffer
init|=
name|this
operator|.
name|buffer
decl_stmt|;
name|this
operator|.
name|buffer
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|()
expr_stmt|;
name|executor
operator|.
name|execute
argument_list|(
parameter_list|()
lambda|->
name|flush0
argument_list|(
name|future
argument_list|,
name|buffer
argument_list|,
name|sync
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|future
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|recoverAndClose
parameter_list|(
name|CancelableProgressable
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|executor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
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
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
name|buffer
operator|.
name|size
argument_list|()
operator|==
literal|0
argument_list|,
literal|"should call flush first before calling close"
argument_list|)
expr_stmt|;
name|executor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isBroken
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

