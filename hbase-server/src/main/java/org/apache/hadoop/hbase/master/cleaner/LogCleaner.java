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
name|master
operator|.
name|cleaner
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
name|HConstants
operator|.
name|HBASE_MASTER_LOGCLEANER_PLUGINS
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
name|CountDownLatch
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
name|LinkedBlockingQueue
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
name|FileStatus
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
name|Stoppable
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
name|master
operator|.
name|procedure
operator|.
name|MasterProcedureUtil
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
name|AbstractFSWALProvider
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
name|annotations
operator|.
name|VisibleForTesting
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

begin_comment
comment|/**  * This Chore, every time it runs, will attempt to delete the WALs and Procedure WALs in the old  * logs folder. The WAL is only deleted if none of the cleaner delegates says otherwise.  * @see BaseLogCleanerDelegate  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|LogCleaner
extends|extends
name|CleanerChore
argument_list|<
name|BaseLogCleanerDelegate
argument_list|>
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
name|LogCleaner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|OLD_WALS_CLEANER_THREAD_SIZE
init|=
literal|"hbase.oldwals.cleaner.thread.size"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_OLD_WALS_CLEANER_THREAD_SIZE
init|=
literal|2
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|OLD_WALS_CLEANER_THREAD_TIMEOUT_MSEC
init|=
literal|"hbase.oldwals.cleaner.thread.timeout.msec"
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|static
specifier|final
name|long
name|DEFAULT_OLD_WALS_CLEANER_THREAD_TIMEOUT_MSEC
init|=
literal|60
operator|*
literal|1000L
decl_stmt|;
specifier|private
specifier|final
name|LinkedBlockingQueue
argument_list|<
name|CleanerContext
argument_list|>
name|pendingDelete
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Thread
argument_list|>
name|oldWALsCleaner
decl_stmt|;
specifier|private
name|long
name|cleanerThreadTimeoutMsec
decl_stmt|;
comment|/**    * @param period the period of time to sleep between each run    * @param stopper the stopper    * @param conf configuration to use    * @param fs handle to the FS    * @param oldLogDir the path to the archived logs    */
specifier|public
name|LogCleaner
parameter_list|(
specifier|final
name|int
name|period
parameter_list|,
specifier|final
name|Stoppable
name|stopper
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|oldLogDir
parameter_list|)
block|{
name|super
argument_list|(
literal|"LogsCleaner"
argument_list|,
name|period
argument_list|,
name|stopper
argument_list|,
name|conf
argument_list|,
name|fs
argument_list|,
name|oldLogDir
argument_list|,
name|HBASE_MASTER_LOGCLEANER_PLUGINS
argument_list|)
expr_stmt|;
name|this
operator|.
name|pendingDelete
operator|=
operator|new
name|LinkedBlockingQueue
argument_list|<>
argument_list|()
expr_stmt|;
name|int
name|size
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|OLD_WALS_CLEANER_THREAD_SIZE
argument_list|,
name|DEFAULT_OLD_WALS_CLEANER_THREAD_SIZE
argument_list|)
decl_stmt|;
name|this
operator|.
name|oldWALsCleaner
operator|=
name|createOldWalsCleaner
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|this
operator|.
name|cleanerThreadTimeoutMsec
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|OLD_WALS_CLEANER_THREAD_TIMEOUT_MSEC
argument_list|,
name|DEFAULT_OLD_WALS_CLEANER_THREAD_TIMEOUT_MSEC
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|validate
parameter_list|(
name|Path
name|file
parameter_list|)
block|{
return|return
name|AbstractFSWALProvider
operator|.
name|validateWALFilename
argument_list|(
name|file
operator|.
name|getName
argument_list|()
argument_list|)
operator|||
name|MasterProcedureUtil
operator|.
name|validateProcedureWALFilename
argument_list|(
name|file
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onConfigurationChange
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
operator|.
name|onConfigurationChange
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|int
name|newSize
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|OLD_WALS_CLEANER_THREAD_SIZE
argument_list|,
name|DEFAULT_OLD_WALS_CLEANER_THREAD_SIZE
argument_list|)
decl_stmt|;
if|if
condition|(
name|newSize
operator|==
name|oldWALsCleaner
operator|.
name|size
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Size from configuration is the same as previous which "
operator|+
literal|"is {}, no need to update."
argument_list|,
name|newSize
argument_list|)
expr_stmt|;
return|return;
block|}
name|interruptOldWALsCleaner
argument_list|()
expr_stmt|;
name|oldWALsCleaner
operator|=
name|createOldWalsCleaner
argument_list|(
name|newSize
argument_list|)
expr_stmt|;
name|cleanerThreadTimeoutMsec
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|OLD_WALS_CLEANER_THREAD_TIMEOUT_MSEC
argument_list|,
name|DEFAULT_OLD_WALS_CLEANER_THREAD_TIMEOUT_MSEC
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|deleteFiles
parameter_list|(
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|filesToDelete
parameter_list|)
block|{
name|List
argument_list|<
name|CleanerContext
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|filesToDelete
control|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Scheduling file {} for deletion"
argument_list|,
name|file
argument_list|)
expr_stmt|;
if|if
condition|(
name|file
operator|!=
literal|null
condition|)
block|{
name|results
operator|.
name|add
argument_list|(
operator|new
name|CleanerContext
argument_list|(
name|file
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Old WAL files pending deletion: {}"
argument_list|,
name|results
argument_list|)
expr_stmt|;
name|pendingDelete
operator|.
name|addAll
argument_list|(
name|results
argument_list|)
expr_stmt|;
name|int
name|deletedFiles
init|=
literal|0
decl_stmt|;
for|for
control|(
name|CleanerContext
name|res
range|:
name|results
control|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Awaiting the results for deletion of old WAL file: {}"
argument_list|,
name|res
argument_list|)
expr_stmt|;
name|deletedFiles
operator|+=
name|res
operator|.
name|getResult
argument_list|(
name|this
operator|.
name|cleanerThreadTimeoutMsec
argument_list|)
condition|?
literal|1
else|:
literal|0
expr_stmt|;
block|}
return|return
name|deletedFiles
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|cleanup
parameter_list|()
block|{
name|super
operator|.
name|cleanup
argument_list|()
expr_stmt|;
name|interruptOldWALsCleaner
argument_list|()
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|int
name|getSizeOfCleaners
parameter_list|()
block|{
return|return
name|oldWALsCleaner
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
name|long
name|getCleanerThreadTimeoutMsec
parameter_list|()
block|{
return|return
name|cleanerThreadTimeoutMsec
return|;
block|}
specifier|private
name|List
argument_list|<
name|Thread
argument_list|>
name|createOldWalsCleaner
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating {} OldWALs cleaner threads"
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Thread
argument_list|>
name|oldWALsCleaner
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|size
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|Thread
name|cleaner
init|=
operator|new
name|Thread
argument_list|(
parameter_list|()
lambda|->
name|deleteFile
argument_list|()
argument_list|)
decl_stmt|;
name|cleaner
operator|.
name|setName
argument_list|(
literal|"OldWALsCleaner-"
operator|+
name|i
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|start
argument_list|()
expr_stmt|;
name|oldWALsCleaner
operator|.
name|add
argument_list|(
name|cleaner
argument_list|)
expr_stmt|;
block|}
return|return
name|oldWALsCleaner
return|;
block|}
specifier|private
name|void
name|interruptOldWALsCleaner
parameter_list|()
block|{
for|for
control|(
name|Thread
name|cleaner
range|:
name|oldWALsCleaner
control|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Interrupting thread: {}"
argument_list|,
name|cleaner
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
name|oldWALsCleaner
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|deleteFile
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
specifier|final
name|CleanerContext
name|context
init|=
name|pendingDelete
operator|.
name|take
argument_list|()
decl_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|FileStatus
name|oldWalFile
init|=
name|context
operator|.
name|getTargetToClean
argument_list|()
decl_stmt|;
try|try
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Attempting to delete old WAL file: {}"
argument_list|,
name|oldWalFile
argument_list|)
expr_stmt|;
name|boolean
name|succeed
init|=
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|oldWalFile
operator|.
name|getPath
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|context
operator|.
name|setResult
argument_list|(
name|succeed
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// fs.delete() fails.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to clean old WAL file"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|context
operator|.
name|setResult
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ite
parameter_list|)
block|{
comment|// It is most likely from configuration changing request
name|LOG
operator|.
name|warn
argument_list|(
literal|"Interrupted while cleaning old WALs, will "
operator|+
literal|"try to clean it next round. Exiting."
argument_list|)
expr_stmt|;
comment|// Restore interrupt status
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
return|return;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Exiting"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|cancel
parameter_list|(
name|boolean
name|mayInterruptIfRunning
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Cancelling LogCleaner"
argument_list|)
expr_stmt|;
name|super
operator|.
name|cancel
argument_list|(
name|mayInterruptIfRunning
argument_list|)
expr_stmt|;
name|interruptOldWALsCleaner
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
class|class
name|CleanerContext
block|{
specifier|final
name|FileStatus
name|target
decl_stmt|;
specifier|final
name|AtomicBoolean
name|result
decl_stmt|;
specifier|final
name|CountDownLatch
name|remainingResults
decl_stmt|;
specifier|private
name|CleanerContext
parameter_list|(
name|FileStatus
name|status
parameter_list|)
block|{
name|this
operator|.
name|target
operator|=
name|status
expr_stmt|;
name|this
operator|.
name|result
operator|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|remainingResults
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|void
name|setResult
parameter_list|(
name|boolean
name|res
parameter_list|)
block|{
name|this
operator|.
name|result
operator|.
name|set
argument_list|(
name|res
argument_list|)
expr_stmt|;
name|this
operator|.
name|remainingResults
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
name|boolean
name|getResult
parameter_list|(
name|long
name|waitIfNotFinished
parameter_list|)
block|{
try|try
block|{
name|boolean
name|completed
init|=
name|this
operator|.
name|remainingResults
operator|.
name|await
argument_list|(
name|waitIfNotFinished
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|completed
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Spend too much time [{}ms] to delete old WAL file: {}"
argument_list|,
name|waitIfNotFinished
argument_list|,
name|target
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Interrupted while awaiting deletion of WAL file: {}"
argument_list|,
name|target
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
name|result
operator|.
name|get
argument_list|()
return|;
block|}
name|FileStatus
name|getTargetToClean
parameter_list|()
block|{
return|return
name|target
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"CleanerContext [target="
operator|+
name|target
operator|+
literal|", result="
operator|+
name|result
operator|+
literal|"]"
return|;
block|}
block|}
block|}
end_class

end_unit

