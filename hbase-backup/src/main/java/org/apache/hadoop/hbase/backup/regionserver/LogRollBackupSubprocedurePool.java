begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements. See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License. You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|backup
operator|.
name|regionserver
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|ExecutorCompletionService
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
name|ThreadPoolExecutor
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
name|hbase
operator|.
name|Abortable
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
name|errorhandling
operator|.
name|ForeignException
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
name|Threads
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

begin_comment
comment|/**  * Handle running each of the individual tasks for completing a backup procedure on a region  * server.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|LogRollBackupSubprocedurePool
implements|implements
name|Closeable
implements|,
name|Abortable
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
name|LogRollBackupSubprocedurePool
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Maximum number of concurrent snapshot region tasks that can run concurrently */
specifier|private
specifier|static
specifier|final
name|String
name|CONCURENT_BACKUP_TASKS_KEY
init|=
literal|"hbase.backup.region.concurrentTasks"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_CONCURRENT_BACKUP_TASKS
init|=
literal|3
decl_stmt|;
specifier|private
specifier|final
name|ExecutorCompletionService
argument_list|<
name|Void
argument_list|>
name|taskPool
decl_stmt|;
specifier|private
specifier|final
name|ThreadPoolExecutor
name|executor
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|aborted
decl_stmt|;
specifier|private
specifier|final
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
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|public
name|LogRollBackupSubprocedurePool
parameter_list|(
name|String
name|name
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
comment|// configure the executor service
name|long
name|keepAlive
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|LogRollRegionServerProcedureManager
operator|.
name|BACKUP_TIMEOUT_MILLIS_KEY
argument_list|,
name|LogRollRegionServerProcedureManager
operator|.
name|BACKUP_TIMEOUT_MILLIS_DEFAULT
argument_list|)
decl_stmt|;
name|int
name|threads
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|CONCURENT_BACKUP_TASKS_KEY
argument_list|,
name|DEFAULT_CONCURRENT_BACKUP_TASKS
argument_list|)
decl_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|executor
operator|=
operator|new
name|ThreadPoolExecutor
argument_list|(
literal|1
argument_list|,
name|threads
argument_list|,
name|keepAlive
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
operator|new
name|LinkedBlockingQueue
argument_list|<>
argument_list|()
argument_list|,
name|Threads
operator|.
name|newDaemonThreadFactory
argument_list|(
literal|"rs("
operator|+
name|name
operator|+
literal|")-backup"
argument_list|)
argument_list|)
expr_stmt|;
name|taskPool
operator|=
operator|new
name|ExecutorCompletionService
argument_list|<>
argument_list|(
name|executor
argument_list|)
expr_stmt|;
block|}
comment|/**    * Submit a task to the pool.    */
specifier|public
name|void
name|submitTask
parameter_list|(
specifier|final
name|Callable
argument_list|<
name|Void
argument_list|>
name|task
parameter_list|)
block|{
name|Future
argument_list|<
name|Void
argument_list|>
name|f
init|=
name|this
operator|.
name|taskPool
operator|.
name|submit
argument_list|(
name|task
argument_list|)
decl_stmt|;
name|futures
operator|.
name|add
argument_list|(
name|f
argument_list|)
expr_stmt|;
block|}
comment|/**    * Wait for all of the currently outstanding tasks submitted via {@link #submitTask(Callable)}    * @return<tt>true</tt> on success,<tt>false</tt> otherwise    * @throws ForeignException exception    */
specifier|public
name|boolean
name|waitForOutstandingTasks
parameter_list|()
throws|throws
name|ForeignException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Waiting for backup procedure to finish."
argument_list|)
expr_stmt|;
try|try
block|{
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
argument_list|()
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
if|if
condition|(
name|aborted
condition|)
block|{
throw|throw
operator|new
name|ForeignException
argument_list|(
literal|"Interrupted and found to be aborted while waiting for tasks!"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|ForeignException
condition|)
block|{
throw|throw
operator|(
name|ForeignException
operator|)
name|e
operator|.
name|getCause
argument_list|()
throw|;
block|}
throw|throw
operator|new
name|ForeignException
argument_list|(
name|name
argument_list|,
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
throw|;
block|}
finally|finally
block|{
comment|// close off remaining tasks
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
if|if
condition|(
operator|!
name|f
operator|.
name|isDone
argument_list|()
condition|)
block|{
name|f
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Attempt to cleanly shutdown any running tasks - allows currently running tasks to cleanly    * finish    */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|executor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|aborted
condition|)
block|{
return|return;
block|}
name|this
operator|.
name|aborted
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Aborting because: "
operator|+
name|why
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|this
operator|.
name|executor
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
name|this
operator|.
name|aborted
return|;
block|}
block|}
end_class

end_unit

