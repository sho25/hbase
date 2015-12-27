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
name|procedure
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
name|DaemonThreadFactory
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
name|RegionServerServices
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
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
name|errorhandling
operator|.
name|ForeignExceptionDispatcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_class
specifier|public
class|class
name|SimpleRSProcedureManager
extends|extends
name|RegionServerProcedureManager
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
name|SimpleRSProcedureManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|RegionServerServices
name|rss
decl_stmt|;
specifier|private
name|ProcedureMemberRpcs
name|memberRpcs
decl_stmt|;
specifier|private
name|ProcedureMember
name|member
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|RegionServerServices
name|rss
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|rss
operator|=
name|rss
expr_stmt|;
name|ZooKeeperWatcher
name|zkw
init|=
name|rss
operator|.
name|getZooKeeper
argument_list|()
decl_stmt|;
name|this
operator|.
name|memberRpcs
operator|=
operator|new
name|ZKProcedureMemberRpcs
argument_list|(
name|zkw
argument_list|,
name|getProcedureSignature
argument_list|()
argument_list|)
expr_stmt|;
name|ThreadPoolExecutor
name|pool
init|=
name|ProcedureMember
operator|.
name|defaultPool
argument_list|(
name|rss
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|this
operator|.
name|member
operator|=
operator|new
name|ProcedureMember
argument_list|(
name|memberRpcs
argument_list|,
name|pool
argument_list|,
operator|new
name|SimleSubprocedureBuilder
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Initialized: "
operator|+
name|rss
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{
name|this
operator|.
name|memberRpcs
operator|.
name|start
argument_list|(
name|rss
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|member
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Started."
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|boolean
name|force
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"stop: "
operator|+
name|force
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|member
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|this
operator|.
name|memberRpcs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|getProcedureSignature
parameter_list|()
block|{
return|return
name|SimpleMasterProcedureManager
operator|.
name|SIMPLE_SIGNATURE
return|;
block|}
comment|/**    * If in a running state, creates the specified subprocedure for handling a procedure.    * @return Subprocedure to submit to the ProcedureMemeber.    */
specifier|public
name|Subprocedure
name|buildSubprocedure
parameter_list|(
name|String
name|name
parameter_list|)
block|{
comment|// don't run a procedure if the parent is stop(ping)
if|if
condition|(
name|rss
operator|.
name|isStopping
argument_list|()
operator|||
name|rss
operator|.
name|isStopped
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Can't start procedure on RS: "
operator|+
name|rss
operator|.
name|getServerName
argument_list|()
operator|+
literal|", because stopping/stopped!"
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Attempting to run a procedure."
argument_list|)
expr_stmt|;
name|ForeignExceptionDispatcher
name|errorDispatcher
init|=
operator|new
name|ForeignExceptionDispatcher
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
name|rss
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|SimpleSubprocedurePool
name|taskManager
init|=
operator|new
name|SimpleSubprocedurePool
argument_list|(
name|rss
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
return|return
operator|new
name|SimpleSubprocedure
argument_list|(
name|rss
argument_list|,
name|member
argument_list|,
name|errorDispatcher
argument_list|,
name|taskManager
argument_list|,
name|name
argument_list|)
return|;
block|}
comment|/**    * Build the actual procedure runner that will do all the 'hard' work    */
specifier|public
class|class
name|SimleSubprocedureBuilder
implements|implements
name|SubprocedureFactory
block|{
annotation|@
name|Override
specifier|public
name|Subprocedure
name|buildSubprocedure
parameter_list|(
name|String
name|name
parameter_list|,
name|byte
index|[]
name|data
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Building procedure: "
operator|+
name|name
argument_list|)
expr_stmt|;
return|return
name|SimpleRSProcedureManager
operator|.
name|this
operator|.
name|buildSubprocedure
argument_list|(
name|name
argument_list|)
return|;
block|}
block|}
specifier|public
class|class
name|SimpleSubprocedurePool
implements|implements
name|Closeable
implements|,
name|Abortable
block|{
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
argument_list|<
name|Future
argument_list|<
name|Void
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|public
name|SimpleSubprocedurePool
parameter_list|(
name|String
name|name
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
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
literal|1
argument_list|,
literal|500
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
operator|new
name|LinkedBlockingQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|()
argument_list|,
operator|new
name|DaemonThreadFactory
argument_list|(
literal|"rs("
operator|+
name|name
operator|+
literal|")-procedure-pool"
argument_list|)
argument_list|)
expr_stmt|;
name|taskPool
operator|=
operator|new
name|ExecutorCompletionService
argument_list|<
name|Void
argument_list|>
argument_list|(
name|executor
argument_list|)
expr_stmt|;
block|}
comment|/**      * Submit a task to the pool.      */
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
comment|/**      * Wait for all of the currently outstanding tasks submitted via {@link #submitTask(Callable)}      *      * @return<tt>true</tt> on success,<tt>false</tt> otherwise      * @throws ForeignException      */
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
literal|"Waiting for procedure to finish."
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
throw|throw
operator|new
name|ForeignException
argument_list|(
literal|"Interrupted and found to be aborted while waiting for tasks!"
argument_list|,
name|e
argument_list|)
throw|;
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
comment|/**      * Attempt to cleanly shutdown any running tasks - allows currently running tasks to cleanly      * finish      */
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
return|return;
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
specifier|public
class|class
name|SimpleSubprocedure
extends|extends
name|Subprocedure
block|{
specifier|private
specifier|final
name|RegionServerServices
name|rss
decl_stmt|;
specifier|private
specifier|final
name|SimpleSubprocedurePool
name|taskManager
decl_stmt|;
specifier|public
name|SimpleSubprocedure
parameter_list|(
name|RegionServerServices
name|rss
parameter_list|,
name|ProcedureMember
name|member
parameter_list|,
name|ForeignExceptionDispatcher
name|errorListener
parameter_list|,
name|SimpleSubprocedurePool
name|taskManager
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|member
argument_list|,
name|name
argument_list|,
name|errorListener
argument_list|,
literal|500
argument_list|,
literal|60000
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Constructing a SimpleSubprocedure."
argument_list|)
expr_stmt|;
name|this
operator|.
name|rss
operator|=
name|rss
expr_stmt|;
name|this
operator|.
name|taskManager
operator|=
name|taskManager
expr_stmt|;
block|}
comment|/**      * Callable task.      * TODO. We don't need a thread pool to execute roll log. This can be simplified      * with no use of subprocedurepool.      */
class|class
name|RSSimpleTask
implements|implements
name|Callable
argument_list|<
name|Void
argument_list|>
block|{
name|RSSimpleTask
parameter_list|()
block|{}
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Execute subprocedure on "
operator|+
name|rss
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|private
name|void
name|execute
parameter_list|()
throws|throws
name|ForeignException
block|{
name|monitor
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
comment|// running a task (e.g., roll log, flush table) on region server
name|taskManager
operator|.
name|submitTask
argument_list|(
operator|new
name|RSSimpleTask
argument_list|()
argument_list|)
expr_stmt|;
name|monitor
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
comment|// wait for everything to complete.
name|taskManager
operator|.
name|waitForOutstandingTasks
argument_list|()
expr_stmt|;
name|monitor
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|acquireBarrier
parameter_list|()
throws|throws
name|ForeignException
block|{
comment|// do nothing, executing in inside barrier step.
block|}
comment|/**      * do a log roll.      */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|insideBarrier
parameter_list|()
throws|throws
name|ForeignException
block|{
name|execute
argument_list|()
expr_stmt|;
return|return
name|SimpleMasterProcedureManager
operator|.
name|SIMPLE_DATA
operator|.
name|getBytes
argument_list|()
return|;
block|}
comment|/**      * Cancel threads if they haven't finished.      */
annotation|@
name|Override
specifier|public
name|void
name|cleanup
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|taskManager
operator|.
name|abort
argument_list|(
literal|"Aborting simple subprocedure tasks due to error"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

