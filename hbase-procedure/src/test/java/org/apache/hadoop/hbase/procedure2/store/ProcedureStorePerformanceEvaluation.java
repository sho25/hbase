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
name|procedure2
operator|.
name|store
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
name|util
operator|.
name|Random
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
name|atomic
operator|.
name|AtomicBoolean
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
name|AtomicLong
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
name|procedure2
operator|.
name|ProcedureTestingUtility
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
name|procedure2
operator|.
name|util
operator|.
name|StringUtils
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
name|AbstractHBaseTool
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLine
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|Option
import|;
end_import

begin_comment
comment|/**  * Base class for testing procedure store performance.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|ProcedureStorePerformanceEvaluation
parameter_list|<
name|T
extends|extends
name|ProcedureStore
parameter_list|>
extends|extends
name|AbstractHBaseTool
block|{
comment|// Command line options and defaults.
specifier|public
specifier|static
name|String
name|DEFAULT_OUTPUT_PATH
init|=
literal|"proc-store"
decl_stmt|;
specifier|public
specifier|static
name|Option
name|OUTPUT_PATH_OPTION
init|=
operator|new
name|Option
argument_list|(
literal|"output"
argument_list|,
literal|true
argument_list|,
literal|"The output path. Default: "
operator|+
name|DEFAULT_OUTPUT_PATH
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|int
name|DEFAULT_NUM_THREADS
init|=
literal|20
decl_stmt|;
specifier|public
specifier|static
name|Option
name|NUM_THREADS_OPTION
init|=
operator|new
name|Option
argument_list|(
literal|"threads"
argument_list|,
literal|true
argument_list|,
literal|"Number of parallel threads which will write insert/updates/deletes to store. Default: "
operator|+
name|DEFAULT_NUM_THREADS
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|int
name|DEFAULT_NUM_PROCS
init|=
literal|1000000
decl_stmt|;
comment|// 1M
specifier|public
specifier|static
name|Option
name|NUM_PROCS_OPTION
init|=
operator|new
name|Option
argument_list|(
literal|"procs"
argument_list|,
literal|true
argument_list|,
literal|"Total number of procedures. Each procedure writes one insert and one update. Default: "
operator|+
name|DEFAULT_NUM_PROCS
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|int
name|DEFAULT_STATE_SIZE
init|=
literal|1024
decl_stmt|;
comment|// 1KB
specifier|public
specifier|static
name|Option
name|STATE_SIZE_OPTION
init|=
operator|new
name|Option
argument_list|(
literal|"state_size"
argument_list|,
literal|true
argument_list|,
literal|"Size of serialized state in bytes to write on update. Default: "
operator|+
name|DEFAULT_STATE_SIZE
operator|+
literal|"bytes"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|Option
name|SYNC_OPTION
init|=
operator|new
name|Option
argument_list|(
literal|"sync"
argument_list|,
literal|true
argument_list|,
literal|"Type of sync to use when writing WAL contents to file system. Accepted values: hflush, "
operator|+
literal|"hsync, nosync. Default: hflush"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|String
name|DEFAULT_SYNC_OPTION
init|=
literal|"hflush"
decl_stmt|;
specifier|protected
name|String
name|outputPath
decl_stmt|;
specifier|protected
name|int
name|numThreads
decl_stmt|;
specifier|protected
name|long
name|numProcs
decl_stmt|;
specifier|protected
name|String
name|syncType
decl_stmt|;
specifier|protected
name|int
name|stateSize
decl_stmt|;
specifier|protected
specifier|static
name|byte
index|[]
name|SERIALIZED_STATE
decl_stmt|;
specifier|protected
name|T
name|store
decl_stmt|;
comment|/** Used by {@link Worker}. */
specifier|private
name|AtomicLong
name|procIds
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
name|AtomicBoolean
name|workersFailed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
comment|// Timeout for worker threads.
specifier|private
specifier|static
specifier|final
name|int
name|WORKER_THREADS_TIMEOUT_SEC
init|=
literal|600
decl_stmt|;
comment|// in seconds
annotation|@
name|Override
specifier|protected
name|void
name|addOptions
parameter_list|()
block|{
name|addOption
argument_list|(
name|OUTPUT_PATH_OPTION
argument_list|)
expr_stmt|;
name|addOption
argument_list|(
name|NUM_THREADS_OPTION
argument_list|)
expr_stmt|;
name|addOption
argument_list|(
name|NUM_PROCS_OPTION
argument_list|)
expr_stmt|;
name|addOption
argument_list|(
name|SYNC_OPTION
argument_list|)
expr_stmt|;
name|addOption
argument_list|(
name|STATE_SIZE_OPTION
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|processOptions
parameter_list|(
name|CommandLine
name|cmd
parameter_list|)
block|{
name|outputPath
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|OUTPUT_PATH_OPTION
operator|.
name|getOpt
argument_list|()
argument_list|,
name|DEFAULT_OUTPUT_PATH
argument_list|)
expr_stmt|;
name|numThreads
operator|=
name|getOptionAsInt
argument_list|(
name|cmd
argument_list|,
name|NUM_THREADS_OPTION
operator|.
name|getOpt
argument_list|()
argument_list|,
name|DEFAULT_NUM_THREADS
argument_list|)
expr_stmt|;
name|numProcs
operator|=
name|getOptionAsInt
argument_list|(
name|cmd
argument_list|,
name|NUM_PROCS_OPTION
operator|.
name|getOpt
argument_list|()
argument_list|,
name|DEFAULT_NUM_PROCS
argument_list|)
expr_stmt|;
name|syncType
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|SYNC_OPTION
operator|.
name|getOpt
argument_list|()
argument_list|,
name|DEFAULT_SYNC_OPTION
argument_list|)
expr_stmt|;
assert|assert
literal|"hsync"
operator|.
name|equals
argument_list|(
name|syncType
argument_list|)
operator|||
literal|"hflush"
operator|.
name|equals
argument_list|(
name|syncType
argument_list|)
operator|||
literal|"nosync"
operator|.
name|equals
argument_list|(
name|syncType
argument_list|)
operator|:
literal|"sync argument can only accept one of these three values: hsync, hflush, nosync"
assert|;
name|stateSize
operator|=
name|getOptionAsInt
argument_list|(
name|cmd
argument_list|,
name|STATE_SIZE_OPTION
operator|.
name|getOpt
argument_list|()
argument_list|,
name|DEFAULT_STATE_SIZE
argument_list|)
expr_stmt|;
name|SERIALIZED_STATE
operator|=
operator|new
name|byte
index|[
name|stateSize
index|]
expr_stmt|;
operator|new
name|Random
argument_list|(
literal|12345
argument_list|)
operator|.
name|nextBytes
argument_list|(
name|SERIALIZED_STATE
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setUpProcedureStore
parameter_list|()
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|storeDir
init|=
name|fs
operator|.
name|makeQualified
argument_list|(
operator|new
name|Path
argument_list|(
name|outputPath
argument_list|)
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Procedure store directory : "
operator|+
name|storeDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|storeDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|store
operator|=
name|createProcedureStore
argument_list|(
name|storeDir
argument_list|)
expr_stmt|;
name|store
operator|.
name|start
argument_list|(
name|numThreads
argument_list|)
expr_stmt|;
name|store
operator|.
name|recoverLease
argument_list|()
expr_stmt|;
name|store
operator|.
name|load
argument_list|(
operator|new
name|ProcedureTestingUtility
operator|.
name|LoadCounter
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Starting new procedure store: "
operator|+
name|store
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|abstract
name|T
name|createProcedureStore
parameter_list|(
name|Path
name|storeDir
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|protected
name|void
name|postStop
parameter_list|(
name|T
name|store
parameter_list|)
throws|throws
name|IOException
block|{   }
specifier|private
name|void
name|tearDownProcedureStore
parameter_list|()
block|{
name|Path
name|storeDir
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
name|store
operator|!=
literal|null
condition|)
block|{
name|store
operator|.
name|stop
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|postStop
argument_list|(
name|store
argument_list|)
expr_stmt|;
block|}
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|storeDir
operator|=
name|fs
operator|.
name|makeQualified
argument_list|(
operator|new
name|Path
argument_list|(
name|outputPath
argument_list|)
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|storeDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Error: Couldn't delete log dir. You can delete it manually to free up "
operator|+
literal|"disk space. Location: "
operator|+
name|storeDir
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
specifier|abstract
name|void
name|printRawFormatResult
parameter_list|(
name|long
name|timeTakenNs
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|protected
name|int
name|doWork
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|setUpProcedureStore
argument_list|()
expr_stmt|;
name|ExecutorService
name|executor
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|numThreads
argument_list|)
decl_stmt|;
name|Future
argument_list|<
name|?
argument_list|>
index|[]
name|futures
init|=
operator|new
name|Future
argument_list|<
name|?
argument_list|>
index|[
name|numThreads
index|]
decl_stmt|;
comment|// Start worker threads.
name|long
name|start
init|=
name|System
operator|.
name|nanoTime
argument_list|()
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
name|numThreads
condition|;
name|i
operator|++
control|)
block|{
name|futures
index|[
name|i
index|]
operator|=
name|executor
operator|.
name|submit
argument_list|(
operator|new
name|Worker
argument_list|(
name|start
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|boolean
name|failure
init|=
literal|false
decl_stmt|;
try|try
block|{
for|for
control|(
name|Future
argument_list|<
name|?
argument_list|>
name|future
range|:
name|futures
control|)
block|{
name|long
name|timeout
init|=
name|start
operator|+
name|WORKER_THREADS_TIMEOUT_SEC
operator|*
literal|1000
operator|-
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|failure
operator||=
operator|(
name|future
operator|.
name|get
argument_list|(
name|timeout
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|.
name|equals
argument_list|(
name|EXIT_FAILURE
argument_list|)
operator|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Exception in worker thread."
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
return|return
name|EXIT_FAILURE
return|;
block|}
name|executor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
if|if
condition|(
name|failure
condition|)
block|{
return|return
name|EXIT_FAILURE
return|;
block|}
name|long
name|timeTakenNs
init|=
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|start
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"******************************************"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Num threads    : "
operator|+
name|numThreads
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Num procedures : "
operator|+
name|numProcs
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Sync type      : "
operator|+
name|syncType
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Time taken     : "
operator|+
name|TimeUnit
operator|.
name|NANOSECONDS
operator|.
name|toSeconds
argument_list|(
name|timeTakenNs
argument_list|)
operator|+
literal|"sec"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"******************************************"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Raw format for scripts"
argument_list|)
expr_stmt|;
name|printRawFormatResult
argument_list|(
name|timeTakenNs
argument_list|)
expr_stmt|;
return|return
name|EXIT_SUCCESS
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
return|return
name|EXIT_FAILURE
return|;
block|}
finally|finally
block|{
name|tearDownProcedureStore
argument_list|()
expr_stmt|;
block|}
block|}
comment|///////////////////////////////
comment|// HELPER CLASSES
comment|///////////////////////////////
comment|/**    * Callable to generate load for wal by inserting/deleting/updating procedures. If procedure store    * fails to roll log file (throws IOException), all threads quit, and at least one returns value    * of {@link AbstractHBaseTool#EXIT_FAILURE}.    */
specifier|private
specifier|final
class|class
name|Worker
implements|implements
name|Callable
argument_list|<
name|Integer
argument_list|>
block|{
specifier|private
specifier|final
name|long
name|start
decl_stmt|;
specifier|public
name|Worker
parameter_list|(
name|long
name|start
parameter_list|)
block|{
name|this
operator|.
name|start
operator|=
name|start
expr_stmt|;
block|}
comment|// TODO: Can also collect #procs, time taken by each thread to measure fairness.
annotation|@
name|Override
specifier|public
name|Integer
name|call
parameter_list|()
throws|throws
name|IOException
block|{
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|workersFailed
operator|.
name|get
argument_list|()
condition|)
block|{
return|return
name|EXIT_FAILURE
return|;
block|}
name|long
name|procId
init|=
name|procIds
operator|.
name|getAndIncrement
argument_list|()
decl_stmt|;
if|if
condition|(
name|procId
operator|>=
name|numProcs
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|procId
operator|!=
literal|0
operator|&&
name|procId
operator|%
literal|10000
operator|==
literal|0
condition|)
block|{
name|long
name|ns
init|=
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|start
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Wrote "
operator|+
name|procId
operator|+
literal|" procedures in "
operator|+
name|StringUtils
operator|.
name|humanTimeDiff
argument_list|(
name|TimeUnit
operator|.
name|NANOSECONDS
operator|.
name|toMillis
argument_list|(
name|ns
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|preWrite
argument_list|(
name|procId
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
comment|// Ask other threads to quit too.
name|workersFailed
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Exception when rolling log file. Current procId = "
operator|+
name|procId
argument_list|)
expr_stmt|;
name|ioe
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
return|return
name|EXIT_FAILURE
return|;
block|}
name|ProcedureTestingUtility
operator|.
name|TestProcedure
name|proc
init|=
operator|new
name|ProcedureTestingUtility
operator|.
name|TestProcedure
argument_list|(
name|procId
argument_list|)
decl_stmt|;
name|proc
operator|.
name|setData
argument_list|(
name|SERIALIZED_STATE
argument_list|)
expr_stmt|;
name|store
operator|.
name|insert
argument_list|(
name|proc
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|store
operator|.
name|update
argument_list|(
name|proc
argument_list|)
expr_stmt|;
block|}
return|return
name|EXIT_SUCCESS
return|;
block|}
block|}
specifier|protected
specifier|abstract
name|void
name|preWrite
parameter_list|(
name|long
name|procId
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit

