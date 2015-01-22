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
name|client
operator|.
name|example
package|;
end_package

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
name|Configured
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
name|TableName
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
name|client
operator|.
name|BufferedMutator
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
name|client
operator|.
name|BufferedMutatorParams
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
name|client
operator|.
name|Connection
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
name|client
operator|.
name|ConnectionFactory
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
name|client
operator|.
name|Put
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
name|client
operator|.
name|RetriesExhaustedWithDetailsException
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
name|util
operator|.
name|Tool
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
name|ToolRunner
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
name|ArrayList
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Callable
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Future
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
name|TimeUnit
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
name|TimeoutException
import|;
end_import

begin_comment
comment|/**  * An example of using the {@link BufferedMutator} interface.  */
end_comment

begin_class
specifier|public
class|class
name|BufferedMutatorExample
extends|extends
name|Configured
implements|implements
name|Tool
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
name|BufferedMutatorExample
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|POOL_SIZE
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|TASK_COUNT
init|=
literal|100
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|TABLE
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
throws|,
name|TimeoutException
block|{
comment|/** a callback invoked when an asynchronous write fails. */
specifier|final
name|BufferedMutator
operator|.
name|ExceptionListener
name|listener
init|=
operator|new
name|BufferedMutator
operator|.
name|ExceptionListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onException
parameter_list|(
name|RetriesExhaustedWithDetailsException
name|e
parameter_list|,
name|BufferedMutator
name|mutator
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|e
operator|.
name|getNumExceptions
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed to sent put "
operator|+
name|e
operator|.
name|getRow
argument_list|(
name|i
argument_list|)
operator|+
literal|"."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|BufferedMutatorParams
name|params
init|=
operator|new
name|BufferedMutatorParams
argument_list|(
name|TABLE
argument_list|)
operator|.
name|listener
argument_list|(
name|listener
argument_list|)
decl_stmt|;
comment|//
comment|// step 1: create a single Connection and a BufferedMutator, shared by all worker threads.
comment|//
try|try
init|(
specifier|final
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|getConf
argument_list|()
argument_list|)
init|;
name|final
name|BufferedMutator
name|mutator
operator|=
name|conn
operator|.
name|getBufferedMutator
argument_list|(
name|params
argument_list|)
init|)
block|{
comment|/** worker pool that operates on BufferedTable instances */
specifier|final
name|ExecutorService
name|workerPool
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|POOL_SIZE
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Future
argument_list|<
name|Void
argument_list|>
argument_list|>
name|futures
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|TASK_COUNT
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|TASK_COUNT
condition|;
name|i
operator|++
control|)
block|{
name|futures
operator|.
name|add
argument_list|(
name|workerPool
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
comment|//
comment|// step 2: each worker sends edits to the shared BufferedMutator instance. They all use
comment|// the same backing buffer, call-back "listener", and RPC executor pool.
comment|//
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"someRow"
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"someQualifier"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"some value"
argument_list|)
argument_list|)
expr_stmt|;
name|mutator
operator|.
name|mutate
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// do work... maybe you want to call mutator.flush() after many edits to ensure any of
comment|// this worker's edits are sent before exiting the Callable
return|return
literal|null
return|;
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//
comment|// step 3: clean up the worker pool, shut down.
comment|//
for|for
control|(
name|Future
argument_list|<
name|Void
argument_list|>
name|f
range|:
name|futures
control|)
block|{
name|f
operator|.
name|get
argument_list|(
literal|5
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
expr_stmt|;
block|}
name|workerPool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// exception while creating/destroying Connection or BufferedMutator
name|LOG
operator|.
name|info
argument_list|(
literal|"exception while creating/destroying Connection or BufferedMutator"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
comment|// BufferedMutator.close() ensures all work is flushed. Could be the custom listener is
comment|// invoked from here.
return|return
literal|0
return|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|ToolRunner
operator|.
name|run
argument_list|(
operator|new
name|BufferedMutatorExample
argument_list|()
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

