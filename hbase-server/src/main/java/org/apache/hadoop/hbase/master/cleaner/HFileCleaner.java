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
name|hbase
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
name|long
name|deletedLargeFiles
init|=
literal|0L
decl_stmt|;
specifier|private
name|long
name|deletedSmallFiles
init|=
literal|0L
decl_stmt|;
comment|/**    * @param period the period of time to sleep between each run    * @param stopper the stopper    * @param conf configuration to use    * @param fs handle to the FS    * @param directory directory to be cleaned    * @param params params could be used in subclass of BaseHFileCleanerDelegate    */
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
argument_list|)
expr_stmt|;
name|smallFileQueue
operator|=
name|largeFileQueue
operator|.
name|getStealFromQueue
argument_list|()
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
argument_list|()
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
literal|"Large file deletion queue is full"
argument_list|)
expr_stmt|;
block|}
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
literal|"Small file deletion queue is full"
argument_list|)
expr_stmt|;
block|}
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
literal|"-HFileCleaner.large-"
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
literal|"Starting hfile cleaner for large files: "
operator|+
name|large
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|threads
operator|.
name|add
argument_list|(
name|large
argument_list|)
expr_stmt|;
comment|// start thread for small file deletion
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
literal|"-HFileCleaner.small-"
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
literal|"Starting hfile cleaner for small files: "
operator|+
name|small
operator|.
name|getName
argument_list|()
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
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Interrupted while trying to take a task from queue"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
if|if
condition|(
name|task
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removing: "
operator|+
name|task
operator|.
name|filePath
operator|+
literal|" from archive"
argument_list|)
expr_stmt|;
block|}
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
literal|"Failed to delete file "
operator|+
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
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Exit thread: "
operator|+
name|Thread
operator|.
name|currentThread
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
operator|==
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleted more than Long.MAX_VALUE large files, reset counter to 0"
argument_list|)
expr_stmt|;
name|deletedLargeFiles
operator|=
literal|0L
expr_stmt|;
block|}
name|deletedLargeFiles
operator|++
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|deletedSmallFiles
operator|==
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleted more than Long.MAX_VALUE small files, reset counter to 0"
argument_list|)
expr_stmt|;
name|deletedSmallFiles
operator|=
literal|0L
expr_stmt|;
block|}
if|if
condition|(
name|fromLargeQueue
operator|&&
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
literal|"Stolen a small file deletion task in large file thread"
argument_list|)
expr_stmt|;
block|}
name|deletedSmallFiles
operator|++
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
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stopping file delete threads"
argument_list|)
expr_stmt|;
block|}
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
specifier|static
class|class
name|HFileDeleteTask
implements|implements
name|Comparable
argument_list|<
name|HFileDeleteTask
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|long
name|MAX_WAIT
init|=
literal|60
operator|*
literal|1000L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|WAIT_UNIT
init|=
literal|1000L
decl_stmt|;
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
specifier|public
name|HFileDeleteTask
parameter_list|(
name|FileStatus
name|file
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
parameter_list|()
block|{
name|long
name|waitTime
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
name|wait
argument_list|(
name|WAIT_UNIT
argument_list|)
expr_stmt|;
name|waitTime
operator|+=
name|WAIT_UNIT
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
name|waitTime
operator|>
name|MAX_WAIT
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Wait more than "
operator|+
name|MAX_WAIT
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
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|HFileDeleteTask
name|o
parameter_list|)
block|{
name|long
name|sub
init|=
name|this
operator|.
name|fileLength
operator|-
name|o
operator|.
name|fileLength
decl_stmt|;
comment|// smaller value with higher priority in PriorityQueue, and we intent to delete the larger
comment|// file first.
return|return
operator|(
name|sub
operator|>
literal|0
operator|)
condition|?
operator|-
literal|1
else|:
operator|(
name|sub
operator|<
literal|0
condition|?
literal|1
else|:
literal|0
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
operator|||
operator|!
operator|(
name|o
operator|instanceof
name|HFileDeleteTask
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|HFileDeleteTask
name|otherTask
init|=
operator|(
name|HFileDeleteTask
operator|)
name|o
decl_stmt|;
return|return
name|this
operator|.
name|filePath
operator|.
name|equals
argument_list|(
name|otherTask
operator|.
name|filePath
argument_list|)
operator|&&
operator|(
name|this
operator|.
name|fileLength
operator|==
name|otherTask
operator|.
name|fileLength
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|filePath
operator|.
name|hashCode
argument_list|()
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
name|Override
specifier|public
name|void
name|onConfigurationChange
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"Updating configuration for HFileCleaner, previous throttle point: "
argument_list|)
operator|.
name|append
argument_list|(
name|throttlePoint
argument_list|)
operator|.
name|append
argument_list|(
literal|", largeQueueInitSize: "
argument_list|)
operator|.
name|append
argument_list|(
name|largeQueueInitSize
argument_list|)
operator|.
name|append
argument_list|(
literal|", smallQueueInitSize: "
argument_list|)
operator|.
name|append
argument_list|(
name|smallQueueInitSize
argument_list|)
expr_stmt|;
name|stopHFileDeleteThreads
argument_list|()
expr_stmt|;
name|this
operator|.
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
name|this
operator|.
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
name|this
operator|.
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
argument_list|()
decl_stmt|;
for|for
control|(
name|HFileDeleteTask
name|task
range|:
name|largeFileQueue
control|)
block|{
name|leftOverTasks
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|HFileDeleteTask
name|task
range|:
name|smallFileQueue
control|)
block|{
name|leftOverTasks
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
name|largeFileQueue
operator|=
operator|new
name|StealJobQueue
argument_list|<>
argument_list|(
name|largeQueueInitSize
argument_list|,
name|smallQueueInitSize
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
name|builder
operator|.
name|append
argument_list|(
literal|"; new throttle point: "
argument_list|)
operator|.
name|append
argument_list|(
name|throttlePoint
argument_list|)
operator|.
name|append
argument_list|(
literal|", largeQueueInitSize: "
argument_list|)
operator|.
name|append
argument_list|(
name|largeQueueInitSize
argument_list|)
operator|.
name|append
argument_list|(
literal|", smallQueueInitSize: "
argument_list|)
operator|.
name|append
argument_list|(
name|smallQueueInitSize
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|builder
operator|.
name|toString
argument_list|()
argument_list|)
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
block|}
end_class

end_unit

