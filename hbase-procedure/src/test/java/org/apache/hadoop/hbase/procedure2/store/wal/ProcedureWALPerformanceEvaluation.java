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
name|HBaseCommonTestingUtility
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

begin_class
specifier|public
class|class
name|ProcedureWALPerformanceEvaluation
extends|extends
name|AbstractHBaseTool
block|{
specifier|protected
specifier|static
specifier|final
name|HBaseCommonTestingUtility
name|UTIL
init|=
operator|new
name|HBaseCommonTestingUtility
argument_list|()
decl_stmt|;
comment|// Command line options and defaults.
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
literal|"Number of parallel threads which will write insert/updates/deletes to WAL. Default: "
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
name|DEFAULT_NUM_WALS
init|=
literal|0
decl_stmt|;
specifier|public
specifier|static
name|Option
name|NUM_WALS_OPTION
init|=
operator|new
name|Option
argument_list|(
literal|"wals"
argument_list|,
literal|true
argument_list|,
literal|"Number of WALs to write. If -ve or 0, uses "
operator|+
name|WALProcedureStore
operator|.
name|ROLL_THRESHOLD_CONF_KEY
operator|+
literal|" conf to roll the logs. Default: "
operator|+
name|DEFAULT_NUM_WALS
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
specifier|public
name|int
name|numThreads
decl_stmt|;
specifier|public
name|long
name|numProcs
decl_stmt|;
specifier|public
name|long
name|numProcsPerWal
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
comment|// never roll wall based on this value.
specifier|public
name|int
name|numWals
decl_stmt|;
specifier|public
name|String
name|syncType
decl_stmt|;
specifier|public
name|int
name|stateSize
decl_stmt|;
specifier|static
name|byte
index|[]
name|serializedState
decl_stmt|;
specifier|private
name|WALProcedureStore
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
comment|// Non-default configurations.
specifier|private
name|void
name|setupConf
parameter_list|()
block|{
name|conf
operator|.
name|setBoolean
argument_list|(
name|WALProcedureStore
operator|.
name|USE_HSYNC_CONF_KEY
argument_list|,
literal|"hsync"
operator|.
name|equals
argument_list|(
name|syncType
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|numWals
operator|>
literal|0
condition|)
block|{
name|conf
operator|.
name|setLong
argument_list|(
name|WALProcedureStore
operator|.
name|ROLL_THRESHOLD_CONF_KEY
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|numProcsPerWal
operator|=
name|numProcs
operator|/
name|numWals
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|setupProcedureStore
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|testDir
init|=
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|testDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|logDir
init|=
operator|new
name|Path
argument_list|(
name|testDir
argument_list|,
literal|"proc-logs"
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Logs directory : "
operator|+
name|logDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|logDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
literal|"nosync"
operator|.
name|equals
argument_list|(
name|syncType
argument_list|)
condition|)
block|{
name|store
operator|=
operator|new
name|NoSyncWalProcedureStore
argument_list|(
name|conf
argument_list|,
name|logDir
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|store
operator|=
name|ProcedureTestingUtility
operator|.
name|createWalStore
argument_list|(
name|conf
argument_list|,
name|logDir
argument_list|)
expr_stmt|;
block|}
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
literal|"Starting new log : "
operator|+
name|store
operator|.
name|getActiveLogs
argument_list|()
operator|.
name|get
argument_list|(
name|store
operator|.
name|getActiveLogs
argument_list|()
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|tearDownProcedureStore
parameter_list|()
block|{
name|store
operator|.
name|stop
argument_list|(
literal|false
argument_list|)
expr_stmt|;
try|try
block|{
name|store
operator|.
name|getFileSystem
argument_list|()
operator|.
name|delete
argument_list|(
name|store
operator|.
name|getWALDir
argument_list|()
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
name|store
operator|.
name|getWALDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Processes and validates command line options.    */
annotation|@
name|Override
specifier|public
name|void
name|processOptions
parameter_list|(
name|CommandLine
name|cmd
parameter_list|)
block|{
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
name|numWals
operator|=
name|getOptionAsInt
argument_list|(
name|cmd
argument_list|,
name|NUM_WALS_OPTION
operator|.
name|getOpt
argument_list|()
argument_list|,
name|DEFAULT_NUM_WALS
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
name|serializedState
operator|=
operator|new
name|byte
index|[
name|stateSize
index|]
expr_stmt|;
name|setupConf
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addOptions
parameter_list|()
block|{
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
name|NUM_WALS_OPTION
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
specifier|public
name|int
name|doWork
parameter_list|()
block|{
try|try
block|{
name|setupProcedureStore
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
name|currentTimeMillis
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
name|timeTaken
init|=
name|System
operator|.
name|currentTimeMillis
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
operator|(
name|timeTaken
operator|/
literal|1000.0f
operator|)
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
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"RESULT [%s=%s, %s=%s, %s=%s, %s=%s, %s=%s, "
operator|+
literal|"total_time_ms=%s]"
argument_list|,
name|NUM_PROCS_OPTION
operator|.
name|getOpt
argument_list|()
argument_list|,
name|numProcs
argument_list|,
name|STATE_SIZE_OPTION
operator|.
name|getOpt
argument_list|()
argument_list|,
name|stateSize
argument_list|,
name|SYNC_OPTION
operator|.
name|getOpt
argument_list|()
argument_list|,
name|syncType
argument_list|,
name|NUM_THREADS_OPTION
operator|.
name|getOpt
argument_list|()
argument_list|,
name|numThreads
argument_list|,
name|NUM_WALS_OPTION
operator|.
name|getOpt
argument_list|()
argument_list|,
name|numWals
argument_list|,
name|timeTaken
argument_list|)
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
comment|/**    * Callable to generate load for wal by inserting/deleting/updating procedures.    * If procedure store fails to roll log file (throws IOException), all threads quit, and at    * least one returns value of {@link AbstractHBaseTool#EXIT_FAILURE}.    */
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
name|ms
init|=
name|System
operator|.
name|currentTimeMillis
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
name|ms
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|procId
operator|>
literal|0
operator|&&
name|procId
operator|%
name|numProcsPerWal
operator|==
literal|0
condition|)
block|{
name|store
operator|.
name|rollWriterForTesting
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Starting new log : "
operator|+
name|store
operator|.
name|getActiveLogs
argument_list|()
operator|.
name|get
argument_list|(
name|store
operator|.
name|getActiveLogs
argument_list|()
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|serializedState
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
specifier|private
specifier|static
class|class
name|NoSyncWalProcedureStore
extends|extends
name|WALProcedureStore
block|{
specifier|public
name|NoSyncWalProcedureStore
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Path
name|logDir
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|logDir
argument_list|,
literal|null
argument_list|,
operator|new
name|WALProcedureStore
operator|.
name|LeaseRecovery
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|recoverFileLease
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
comment|// no-op
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|syncStream
parameter_list|(
name|FSDataOutputStream
name|stream
parameter_list|)
block|{
comment|// no-op
block|}
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
name|IOException
block|{
name|ProcedureWALPerformanceEvaluation
name|tool
init|=
operator|new
name|ProcedureWALPerformanceEvaluation
argument_list|()
decl_stmt|;
name|tool
operator|.
name|setConf
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|tool
operator|.
name|run
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

