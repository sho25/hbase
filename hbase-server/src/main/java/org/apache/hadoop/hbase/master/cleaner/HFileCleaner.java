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
name|Comparator
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
name|Map
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
name|BlockingQueue
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
name|conf
operator|.
name|ConfigurationObserver
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
name|HFileLink
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
name|regionserver
operator|.
name|StoreFileInfo
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
name|StealJobQueue
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

begin_comment
comment|/**  * This Chore, every time it runs, will clear the HFiles in the hfile archive  * folder that are deletable for each HFile cleaner in the chain.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HFileCleaner
extends|extends
name|CleanerChore
argument_list|<
name|BaseHFileCleanerDelegate
argument_list|>
implements|implements
name|ConfigurationObserver
block|{
specifier|public
specifier|static
specifier|final
name|String
name|MASTER_HFILE_CLEANER_PLUGINS
init|=
literal|"hbase.master.hfilecleaner.plugins"
decl_stmt|;
specifier|public
name|HFileCleaner
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
name|directory
parameter_list|,
name|DirScanPool
name|pool
parameter_list|)
block|{
name|this
argument_list|(
name|period
argument_list|,
name|stopper
argument_list|,
name|conf
argument_list|,
name|fs
argument_list|,
name|directory
argument_list|,
name|pool
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|// Configuration key for large/small throttle point
specifier|public
specifier|final
specifier|static
name|String
name|HFILE_DELETE_THROTTLE_THRESHOLD
init|=
literal|"hbase.regionserver.thread.hfilecleaner.throttle"
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|int
name|DEFAULT_HFILE_DELETE_THROTTLE_THRESHOLD
init|=
literal|64
operator|*
literal|1024
operator|*
literal|1024
decl_stmt|;
comment|// 64M
comment|// Configuration key for large queue initial size
specifier|public
specifier|final
specifier|static
name|String
name|LARGE_HFILE_QUEUE_INIT_SIZE
init|=
literal|"hbase.regionserver.hfilecleaner.large.queue.size"
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|int
name|DEFAULT_LARGE_HFILE_QUEUE_INIT_SIZE
init|=
literal|10240
decl_stmt|;
comment|// Configuration key for small queue initial size
specifier|public
specifier|final
specifier|static
name|String
name|SMALL_HFILE_QUEUE_INIT_SIZE
init|=
literal|"hbase.regionserver.hfilecleaner.small.queue.size"
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|int
name|DEFAULT_SMALL_HFILE_QUEUE_INIT_SIZE
init|=
literal|10240
decl_stmt|;
comment|// Configuration key for large file delete thread number
specifier|public
specifier|final
specifier|static
name|String
name|LARGE_HFILE_DELETE_THREAD_NUMBER
init|=
literal|"hbase.regionserver.hfilecleaner.large.thread.count"
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|int
name|DEFAULT_LARGE_HFILE_DELETE_THREAD_NUMBER
init|=
literal|1
decl_stmt|;
comment|// Configuration key for small file delete thread number
specifier|public
specifier|final
specifier|static
name|String
name|SMALL_HFILE_DELETE_THREAD_NUMBER
init|=
literal|"hbase.regionserver.hfilecleaner.small.thread.count"
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|int
name|DEFAULT_SMALL_HFILE_DELETE_THREAD_NUMBER
init|=
literal|1
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HFILE_DELETE_THREAD_TIMEOUT_MSEC
init|=
literal|"hbase.regionserver.hfilecleaner.thread.timeout.msec"
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|static
specifier|final
name|long
name|DEFAULT_HFILE_DELETE_THREAD_TIMEOUT_MSEC
init|=
literal|60
operator|*
literal|1000L
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HFILE_DELETE_THREAD_CHECK_INTERVAL_MSEC
init|=
literal|"hbase.regionserver.hfilecleaner.thread.check.interval.msec"
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|static
specifier|final
name|long
name|DEFAULT_HFILE_DELETE_THREAD_CHECK_INTERVAL_MSEC
init|=
literal|1000L
decl_stmt|;
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
name|HFileCleaner
operator|.
name|class
argument_list|)
decl_stmt|;
name|StealJobQueue
argument_list|<
name|HFileDeleteTask
argument_list|>
name|largeFileQueue
decl_stmt|;
name|BlockingQueue
argument_list|<
name|HFileDeleteTask
argument_list|>
name|smallFileQueue
decl_stmt|;
specifier|private
name|int
name|throttlePoint
decl_stmt|;
specifier|private
name|int
name|largeQueueInitSize
decl_stmt|;
specifier|private
name|int
name|smallQueueInitSize
decl_stmt|;
specifier|private
name|int
name|largeFileDeleteThreadNumber
decl_stmt|;
specifier|private
name|int
name|smallFileDeleteThreadNumber
decl_stmt|;
specifier|private
name|long
name|cleanerThreadTimeoutMsec
decl_stmt|;
specifier|private
name|long
name|cleanerThreadCheckIntervalMsec
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Thread
argument_list|>
name|threads
init|=
operator|new
name|ArrayList
argument_list|<
name|Thread
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|running
decl_stmt|;
specifier|private
name|AtomicLong
name|deletedLargeFiles
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|private
name|AtomicLong
name|deletedSmallFiles
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
comment|/**    * @param period the period of time to sleep between each run    * @param stopper the stopper    * @param conf configuration to use    * @param fs handle to the FS    * @param directory directory to be cleaned    * @param pool the thread pool used to scan directories    * @param params params could be used in subclass of BaseHFileCleanerDelegate    */
specifier|public
name|HFileCleaner
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
name|directory
parameter_list|,
name|DirScanPool
name|pool
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{
name|this
argument_list|(
literal|"HFileCleaner"
argument_list|,
name|period
argument_list|,
name|stopper
argument_list|,
name|conf
argument_list|,
name|fs
argument_list|,
name|directory
argument_list|,
name|MASTER_HFILE_CLEANER_PLUGINS
argument_list|,
name|pool
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
comment|/**    * For creating customized HFileCleaner.    * @param name name of the chore being run    * @param period the period of time to sleep between each run    * @param stopper the stopper    * @param conf configuration to use    * @param fs handle to the FS    * @param directory directory to be cleaned    * @param confKey configuration key for the classes to instantiate    * @param pool the thread pool used to scan directories    * @param params params could be used in subclass of BaseHFileCleanerDelegate    */
specifier|public
name|HFileCleaner
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|period
parameter_list|,
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
name|directory
parameter_list|,
name|String
name|confKey
parameter_list|,
name|DirScanPool
name|pool
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|period
argument_list|,
name|stopper
argument_list|,
name|conf
argument_list|,
name|fs
argument_list|,
name|directory
argument_list|,
name|confKey
argument_list|,
name|pool
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|throttlePoint
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HFILE_DELETE_THROTTLE_THRESHOLD
argument_list|,
name|DEFAULT_HFILE_DELETE_THROTTLE_THRESHOLD
argument_list|)
expr_stmt|;
name|largeQueueInitSize
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|LARGE_HFILE_QUEUE_INIT_SIZE
argument_list|,
name|DEFAULT_LARGE_HFILE_QUEUE_INIT_SIZE
argument_list|)
expr_stmt|;
name|smallQueueInitSize
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|SMALL_HFILE_QUEUE_INIT_SIZE
argument_list|,
name|DEFAULT_SMALL_HFILE_QUEUE_INIT_SIZE
argument_list|)
expr_stmt|;
name|largeFileQueue
operator|=
operator|new
name|StealJobQueue
argument_list|<>
argument_list|(
name|largeQueueInitSize
argument_list|,
name|smallQueueInitSize
argument_list|,
name|COMPARATOR
argument_list|)
expr_stmt|;
name|smallFileQueue
operator|=
name|largeFileQueue
operator|.
name|getStealFromQueue
argument_list|()
expr_stmt|;
name|largeFileDeleteThreadNumber
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|LARGE_HFILE_DELETE_THREAD_NUMBER
argument_list|,
name|DEFAULT_LARGE_HFILE_DELETE_THREAD_NUMBER
argument_list|)
expr_stmt|;
name|smallFileDeleteThreadNumber
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|SMALL_HFILE_DELETE_THREAD_NUMBER
argument_list|,
name|DEFAULT_SMALL_HFILE_DELETE_THREAD_NUMBER
argument_list|)
expr_stmt|;
name|cleanerThreadTimeoutMsec
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|HFILE_DELETE_THREAD_TIMEOUT_MSEC
argument_list|,
name|DEFAULT_HFILE_DELETE_THREAD_TIMEOUT_MSEC
argument_list|)
expr_stmt|;
name|cleanerThreadCheckIntervalMsec
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|HFILE_DELETE_THREAD_CHECK_INTERVAL_MSEC
argument_list|,
name|DEFAULT_HFILE_DELETE_THREAD_CHECK_INTERVAL_MSEC
argument_list|)
expr_stmt|;
name|startHFileDeleteThreads
argument_list|()
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
if|if
condition|(
name|HFileLink
operator|.
name|isBackReferencesDir
argument_list|(
name|file
argument_list|)
operator|||
name|HFileLink
operator|.
name|isBackReferencesDir
argument_list|(
name|file
operator|.
name|getParent
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
name|StoreFileInfo
operator|.
name|validateStoreFileName
argument_list|(
name|file
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Exposed for TESTING!    */
specifier|public
name|List
argument_list|<
name|BaseHFileCleanerDelegate
argument_list|>
name|getDelegatesForTesting
parameter_list|()
block|{
return|return
name|this
operator|.
name|cleanersChain
return|;
block|}
annotation|@
name|Override
specifier|public
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
name|int
name|deletedFiles
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|HFileDeleteTask
argument_list|>
name|tasks
init|=
operator|new
name|ArrayList
argument_list|<
name|HFileDeleteTask
argument_list|>
argument_list|()
decl_stmt|;
comment|// construct delete tasks and add into relative queue
for|for
control|(
name|FileStatus
name|file
range|:
name|filesToDelete
control|)
block|{
name|HFileDeleteTask
name|task
init|=
name|deleteFile
argument_list|(
name|file
argument_list|)
decl_stmt|;
if|if
condition|(
name|task
operator|!=
literal|null
condition|)
block|{
name|tasks
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
block|}
comment|// wait for each submitted task to finish
for|for
control|(
name|HFileDeleteTask
name|task
range|:
name|tasks
control|)
block|{
if|if
condition|(
name|task
operator|.
name|getResult
argument_list|(
name|cleanerThreadCheckIntervalMsec
argument_list|)
condition|)
block|{
name|deletedFiles
operator|++
expr_stmt|;
block|}
block|}
return|return
name|deletedFiles
return|;
block|}
comment|/**    * Construct an {@link HFileDeleteTask} for each file to delete and add into the correct queue    * @param file the file to delete    * @return HFileDeleteTask to track progress    */
specifier|private
name|HFileDeleteTask
name|deleteFile
parameter_list|(
name|FileStatus
name|file
parameter_list|)
block|{
name|HFileDeleteTask
name|task
init|=
operator|new
name|HFileDeleteTask
argument_list|(
name|file
argument_list|,
name|cleanerThreadTimeoutMsec
argument_list|)
decl_stmt|;
name|boolean
name|enqueued
init|=
name|dispatch
argument_list|(
name|task
argument_list|)
decl_stmt|;
return|return
name|enqueued
condition|?
name|task
else|:
literal|null
return|;
block|}
specifier|private
name|boolean
name|dispatch
parameter_list|(
name|HFileDeleteTask
name|task
parameter_list|)
block|{
if|if
condition|(
name|task
operator|.
name|fileLength
operator|>=
name|this
operator|.
name|throttlePoint
condition|)
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|largeFileQueue
operator|.
name|offer
argument_list|(
name|task
argument_list|)
condition|)
block|{
comment|// should never arrive here as long as we use PriorityQueue
name|LOG
operator|.
name|trace
argument_list|(
literal|"Large file deletion queue is full"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|smallFileQueue
operator|.
name|offer
argument_list|(
name|task
argument_list|)
condition|)
block|{
comment|// should never arrive here as long as we use PriorityQueue
name|LOG
operator|.
name|trace
argument_list|(
literal|"Small file deletion queue is full"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
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
name|stopHFileDeleteThreads
argument_list|()
expr_stmt|;
block|}
comment|/**    * Start threads for hfile deletion    */
specifier|private
name|void
name|startHFileDeleteThreads
parameter_list|()
block|{
specifier|final
name|String
name|n
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|running
operator|=
literal|true
expr_stmt|;
comment|// start thread for large file deletion
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|largeFileDeleteThreadNumber
condition|;
name|i
operator|++
control|)
block|{
name|Thread
name|large
init|=
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|consumerLoop
argument_list|(
name|largeFileQueue
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|large
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|large
operator|.
name|setName
argument_list|(
name|n
operator|+
literal|"-HFileCleaner.large."
operator|+
name|i
operator|+
literal|"-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|large
operator|.
name|start
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Starting for large file={}"
argument_list|,
name|large
argument_list|)
expr_stmt|;
name|threads
operator|.
name|add
argument_list|(
name|large
argument_list|)
expr_stmt|;
block|}
comment|// start thread for small file deletion
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|smallFileDeleteThreadNumber
condition|;
name|i
operator|++
control|)
block|{
name|Thread
name|small
init|=
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|consumerLoop
argument_list|(
name|smallFileQueue
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|small
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|small
operator|.
name|setName
argument_list|(
name|n
operator|+
literal|"-HFileCleaner.small."
operator|+
name|i
operator|+
literal|"-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|small
operator|.
name|start
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Starting for small files={}"
argument_list|,
name|small
argument_list|)
expr_stmt|;
name|threads
operator|.
name|add
argument_list|(
name|small
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|consumerLoop
parameter_list|(
name|BlockingQueue
argument_list|<
name|HFileDeleteTask
argument_list|>
name|queue
parameter_list|)
block|{
try|try
block|{
while|while
condition|(
name|running
condition|)
block|{
name|HFileDeleteTask
name|task
init|=
literal|null
decl_stmt|;
try|try
block|{
name|task
operator|=
name|queue
operator|.
name|take
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Interrupted while trying to take a task from queue"
argument_list|,
name|e
argument_list|)
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|task
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Removing {}"
argument_list|,
name|task
operator|.
name|filePath
argument_list|)
expr_stmt|;
name|boolean
name|succeed
decl_stmt|;
try|try
block|{
name|succeed
operator|=
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|task
operator|.
name|filePath
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to delete {}"
argument_list|,
name|task
operator|.
name|filePath
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|succeed
operator|=
literal|false
expr_stmt|;
block|}
name|task
operator|.
name|setResult
argument_list|(
name|succeed
argument_list|)
expr_stmt|;
if|if
condition|(
name|succeed
condition|)
block|{
name|countDeletedFiles
argument_list|(
name|task
operator|.
name|fileLength
operator|>=
name|throttlePoint
argument_list|,
name|queue
operator|==
name|largeFileQueue
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
finally|finally
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Exit {}"
argument_list|,
name|Thread
operator|.
name|currentThread
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Currently only for testing purpose
specifier|private
name|void
name|countDeletedFiles
parameter_list|(
name|boolean
name|isLargeFile
parameter_list|,
name|boolean
name|fromLargeQueue
parameter_list|)
block|{
if|if
condition|(
name|isLargeFile
condition|)
block|{
if|if
condition|(
name|deletedLargeFiles
operator|.
name|get
argument_list|()
operator|==
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Deleted more than Long.MAX_VALUE large files, reset counter to 0"
argument_list|)
expr_stmt|;
name|deletedLargeFiles
operator|.
name|set
argument_list|(
literal|0L
argument_list|)
expr_stmt|;
block|}
name|deletedLargeFiles
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|deletedSmallFiles
operator|.
name|get
argument_list|()
operator|==
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Deleted more than Long.MAX_VALUE small files, reset counter to 0"
argument_list|)
expr_stmt|;
name|deletedSmallFiles
operator|.
name|set
argument_list|(
literal|0L
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fromLargeQueue
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Stolen a small file deletion task in large file thread"
argument_list|)
expr_stmt|;
block|}
name|deletedSmallFiles
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Stop threads for hfile deletion    */
specifier|private
name|void
name|stopHFileDeleteThreads
parameter_list|()
block|{
name|running
operator|=
literal|false
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stopping file delete threads"
argument_list|)
expr_stmt|;
for|for
control|(
name|Thread
name|thread
range|:
name|threads
control|)
block|{
name|thread
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
specifier|final
name|Comparator
argument_list|<
name|HFileDeleteTask
argument_list|>
name|COMPARATOR
init|=
operator|new
name|Comparator
argument_list|<
name|HFileDeleteTask
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|HFileDeleteTask
name|o1
parameter_list|,
name|HFileDeleteTask
name|o2
parameter_list|)
block|{
comment|// larger file first so reverse compare
name|int
name|cmp
init|=
name|Long
operator|.
name|compare
argument_list|(
name|o2
operator|.
name|fileLength
argument_list|,
name|o1
operator|.
name|fileLength
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|!=
literal|0
condition|)
block|{
return|return
name|cmp
return|;
block|}
comment|// just use hashCode to generate a stable result.
return|return
name|System
operator|.
name|identityHashCode
argument_list|(
name|o1
argument_list|)
operator|-
name|System
operator|.
name|identityHashCode
argument_list|(
name|o2
argument_list|)
return|;
block|}
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
class|class
name|HFileDeleteTask
block|{
name|boolean
name|done
init|=
literal|false
decl_stmt|;
name|boolean
name|result
decl_stmt|;
specifier|final
name|Path
name|filePath
decl_stmt|;
specifier|final
name|long
name|fileLength
decl_stmt|;
specifier|final
name|long
name|timeoutMsec
decl_stmt|;
specifier|public
name|HFileDeleteTask
parameter_list|(
name|FileStatus
name|file
parameter_list|,
name|long
name|timeoutMsec
parameter_list|)
block|{
name|this
operator|.
name|filePath
operator|=
name|file
operator|.
name|getPath
argument_list|()
expr_stmt|;
name|this
operator|.
name|fileLength
operator|=
name|file
operator|.
name|getLen
argument_list|()
expr_stmt|;
name|this
operator|.
name|timeoutMsec
operator|=
name|timeoutMsec
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|setResult
parameter_list|(
name|boolean
name|result
parameter_list|)
block|{
name|this
operator|.
name|done
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|result
operator|=
name|result
expr_stmt|;
name|notify
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|getResult
parameter_list|(
name|long
name|waitIfNotFinished
parameter_list|)
block|{
name|long
name|waitTimeMsec
init|=
literal|0
decl_stmt|;
try|try
block|{
while|while
condition|(
operator|!
name|done
condition|)
block|{
name|long
name|startTimeNanos
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|wait
argument_list|(
name|waitIfNotFinished
argument_list|)
expr_stmt|;
name|waitTimeMsec
operator|+=
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|convert
argument_list|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|startTimeNanos
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
expr_stmt|;
if|if
condition|(
name|done
condition|)
block|{
return|return
name|this
operator|.
name|result
return|;
block|}
if|if
condition|(
name|waitTimeMsec
operator|>
name|timeoutMsec
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Wait more than "
operator|+
name|timeoutMsec
operator|+
literal|" ms for deleting "
operator|+
name|this
operator|.
name|filePath
operator|+
literal|", exit..."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
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
literal|"Interrupted while waiting for result of deleting "
operator|+
name|filePath
operator|+
literal|", will return false"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
name|this
operator|.
name|result
return|;
block|}
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|List
argument_list|<
name|Thread
argument_list|>
name|getCleanerThreads
parameter_list|()
block|{
return|return
name|threads
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|long
name|getNumOfDeletedLargeFiles
parameter_list|()
block|{
return|return
name|deletedLargeFiles
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|long
name|getNumOfDeletedSmallFiles
parameter_list|()
block|{
return|return
name|deletedSmallFiles
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|long
name|getLargeQueueInitSize
parameter_list|()
block|{
return|return
name|largeQueueInitSize
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|long
name|getSmallQueueInitSize
parameter_list|()
block|{
return|return
name|smallQueueInitSize
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|long
name|getThrottlePoint
parameter_list|()
block|{
return|return
name|throttlePoint
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
annotation|@
name|VisibleForTesting
name|long
name|getCleanerThreadCheckIntervalMsec
parameter_list|()
block|{
return|return
name|cleanerThreadCheckIntervalMsec
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
if|if
condition|(
operator|!
name|checkAndUpdateConfigurations
argument_list|(
name|conf
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Update configuration triggered but nothing changed for this cleaner"
argument_list|)
expr_stmt|;
return|return;
block|}
name|stopHFileDeleteThreads
argument_list|()
expr_stmt|;
comment|// record the left over tasks
name|List
argument_list|<
name|HFileDeleteTask
argument_list|>
name|leftOverTasks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|largeFileQueue
operator|.
name|size
argument_list|()
operator|+
name|smallFileQueue
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|leftOverTasks
operator|.
name|addAll
argument_list|(
name|largeFileQueue
argument_list|)
expr_stmt|;
name|leftOverTasks
operator|.
name|addAll
argument_list|(
name|smallFileQueue
argument_list|)
expr_stmt|;
name|largeFileQueue
operator|=
operator|new
name|StealJobQueue
argument_list|<>
argument_list|(
name|largeQueueInitSize
argument_list|,
name|smallQueueInitSize
argument_list|,
name|COMPARATOR
argument_list|)
expr_stmt|;
name|smallFileQueue
operator|=
name|largeFileQueue
operator|.
name|getStealFromQueue
argument_list|()
expr_stmt|;
name|threads
operator|.
name|clear
argument_list|()
expr_stmt|;
name|startHFileDeleteThreads
argument_list|()
expr_stmt|;
comment|// re-dispatch the left over tasks
for|for
control|(
name|HFileDeleteTask
name|task
range|:
name|leftOverTasks
control|)
block|{
name|dispatch
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Check new configuration and update settings if value changed    * @param conf The new configuration    * @return true if any configuration for HFileCleaner changes, false if no change    */
specifier|private
name|boolean
name|checkAndUpdateConfigurations
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|boolean
name|updated
init|=
literal|false
decl_stmt|;
name|int
name|throttlePoint
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|HFILE_DELETE_THROTTLE_THRESHOLD
argument_list|,
name|DEFAULT_HFILE_DELETE_THROTTLE_THRESHOLD
argument_list|)
decl_stmt|;
if|if
condition|(
name|throttlePoint
operator|!=
name|this
operator|.
name|throttlePoint
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Updating throttle point, from {} to {}"
argument_list|,
name|this
operator|.
name|throttlePoint
argument_list|,
name|throttlePoint
argument_list|)
expr_stmt|;
name|this
operator|.
name|throttlePoint
operator|=
name|throttlePoint
expr_stmt|;
name|updated
operator|=
literal|true
expr_stmt|;
block|}
name|int
name|largeQueueInitSize
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|LARGE_HFILE_QUEUE_INIT_SIZE
argument_list|,
name|DEFAULT_LARGE_HFILE_QUEUE_INIT_SIZE
argument_list|)
decl_stmt|;
if|if
condition|(
name|largeQueueInitSize
operator|!=
name|this
operator|.
name|largeQueueInitSize
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Updating largeQueueInitSize, from {} to {}"
argument_list|,
name|this
operator|.
name|largeQueueInitSize
argument_list|,
name|largeQueueInitSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|largeQueueInitSize
operator|=
name|largeQueueInitSize
expr_stmt|;
name|updated
operator|=
literal|true
expr_stmt|;
block|}
name|int
name|smallQueueInitSize
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|SMALL_HFILE_QUEUE_INIT_SIZE
argument_list|,
name|DEFAULT_SMALL_HFILE_QUEUE_INIT_SIZE
argument_list|)
decl_stmt|;
if|if
condition|(
name|smallQueueInitSize
operator|!=
name|this
operator|.
name|smallQueueInitSize
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Updating smallQueueInitSize, from {} to {}"
argument_list|,
name|this
operator|.
name|smallQueueInitSize
argument_list|,
name|smallQueueInitSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|smallQueueInitSize
operator|=
name|smallQueueInitSize
expr_stmt|;
name|updated
operator|=
literal|true
expr_stmt|;
block|}
name|int
name|largeFileDeleteThreadNumber
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|LARGE_HFILE_DELETE_THREAD_NUMBER
argument_list|,
name|DEFAULT_LARGE_HFILE_DELETE_THREAD_NUMBER
argument_list|)
decl_stmt|;
if|if
condition|(
name|largeFileDeleteThreadNumber
operator|!=
name|this
operator|.
name|largeFileDeleteThreadNumber
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Updating largeFileDeleteThreadNumber, from {} to {}"
argument_list|,
name|this
operator|.
name|largeFileDeleteThreadNumber
argument_list|,
name|largeFileDeleteThreadNumber
argument_list|)
expr_stmt|;
name|this
operator|.
name|largeFileDeleteThreadNumber
operator|=
name|largeFileDeleteThreadNumber
expr_stmt|;
name|updated
operator|=
literal|true
expr_stmt|;
block|}
name|int
name|smallFileDeleteThreadNumber
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|SMALL_HFILE_DELETE_THREAD_NUMBER
argument_list|,
name|DEFAULT_SMALL_HFILE_DELETE_THREAD_NUMBER
argument_list|)
decl_stmt|;
if|if
condition|(
name|smallFileDeleteThreadNumber
operator|!=
name|this
operator|.
name|smallFileDeleteThreadNumber
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Updating smallFileDeleteThreadNumber, from {} to {}"
argument_list|,
name|this
operator|.
name|smallFileDeleteThreadNumber
argument_list|,
name|smallFileDeleteThreadNumber
argument_list|)
expr_stmt|;
name|this
operator|.
name|smallFileDeleteThreadNumber
operator|=
name|smallFileDeleteThreadNumber
expr_stmt|;
name|updated
operator|=
literal|true
expr_stmt|;
block|}
name|long
name|cleanerThreadTimeoutMsec
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|HFILE_DELETE_THREAD_TIMEOUT_MSEC
argument_list|,
name|DEFAULT_HFILE_DELETE_THREAD_TIMEOUT_MSEC
argument_list|)
decl_stmt|;
if|if
condition|(
name|cleanerThreadTimeoutMsec
operator|!=
name|this
operator|.
name|cleanerThreadTimeoutMsec
condition|)
block|{
name|this
operator|.
name|cleanerThreadTimeoutMsec
operator|=
name|cleanerThreadTimeoutMsec
expr_stmt|;
name|updated
operator|=
literal|true
expr_stmt|;
block|}
name|long
name|cleanerThreadCheckIntervalMsec
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|HFILE_DELETE_THREAD_CHECK_INTERVAL_MSEC
argument_list|,
name|DEFAULT_HFILE_DELETE_THREAD_CHECK_INTERVAL_MSEC
argument_list|)
decl_stmt|;
if|if
condition|(
name|cleanerThreadCheckIntervalMsec
operator|!=
name|this
operator|.
name|cleanerThreadCheckIntervalMsec
condition|)
block|{
name|this
operator|.
name|cleanerThreadCheckIntervalMsec
operator|=
name|cleanerThreadCheckIntervalMsec
expr_stmt|;
name|updated
operator|=
literal|true
expr_stmt|;
block|}
return|return
name|updated
return|;
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
name|super
operator|.
name|cancel
argument_list|(
name|mayInterruptIfRunning
argument_list|)
expr_stmt|;
for|for
control|(
name|Thread
name|t
range|:
name|this
operator|.
name|threads
control|)
block|{
name|t
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

