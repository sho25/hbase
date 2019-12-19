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
import|import static
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
name|FutureUtils
operator|.
name|addListener
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
name|atomic
operator|.
name|AtomicInteger
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
name|wal
operator|.
name|WALProvider
operator|.
name|AsyncWriter
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
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_comment
comment|/**  * An {@link AsyncWriter} wrapper which writes data to a set of {@link AsyncWriter} instances.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|CombinedAsyncWriter
implements|implements
name|AsyncWriter
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
name|CombinedAsyncWriter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|AsyncWriter
argument_list|>
name|writers
decl_stmt|;
specifier|private
name|CombinedAsyncWriter
parameter_list|(
name|ImmutableList
argument_list|<
name|AsyncWriter
argument_list|>
name|writers
parameter_list|)
block|{
name|this
operator|.
name|writers
operator|=
name|writers
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLength
parameter_list|()
block|{
return|return
name|writers
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getLength
argument_list|()
return|;
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
name|Exception
name|error
init|=
literal|null
decl_stmt|;
for|for
control|(
name|AsyncWriter
name|writer
range|:
name|writers
control|)
block|{
try|try
block|{
name|writer
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
literal|"close writer failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
name|error
operator|==
literal|null
condition|)
block|{
name|error
operator|=
name|e
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to close at least one writer, please see the warn log above. "
operator|+
literal|"The cause is the first exception occurred"
argument_list|,
name|error
argument_list|)
throw|;
block|}
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
name|writers
operator|.
name|forEach
argument_list|(
name|w
lambda|->
name|w
operator|.
name|append
argument_list|(
name|entry
argument_list|)
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
parameter_list|(
name|boolean
name|forceSync
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
name|AtomicInteger
name|remaining
init|=
operator|new
name|AtomicInteger
argument_list|(
name|writers
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|writers
operator|.
name|forEach
argument_list|(
name|w
lambda|->
name|addListener
argument_list|(
name|w
operator|.
name|sync
argument_list|(
name|forceSync
argument_list|)
argument_list|,
parameter_list|(
name|length
parameter_list|,
name|error
parameter_list|)
lambda|->
block|{
block|if (error != null
argument_list|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
name|error
argument_list|)
block|;
return|return;
block|}
if|if
condition|(
name|remaining
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
name|future
operator|.
name|complete
argument_list|(
name|length
argument_list|)
expr_stmt|;
block|}
block|}
block|)
end_class

begin_empty_stmt
unit|)
empty_stmt|;
end_empty_stmt

begin_return
return|return
name|future
return|;
end_return

begin_function
unit|}    public
specifier|static
name|CombinedAsyncWriter
name|create
parameter_list|(
name|AsyncWriter
name|writer
parameter_list|,
name|AsyncWriter
modifier|...
name|writers
parameter_list|)
block|{
return|return
operator|new
name|CombinedAsyncWriter
argument_list|(
name|ImmutableList
operator|.
expr|<
name|AsyncWriter
operator|>
name|builder
argument_list|()
operator|.
name|add
argument_list|(
name|writer
argument_list|)
operator|.
name|add
argument_list|(
name|writers
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
end_function

unit|}
end_unit

