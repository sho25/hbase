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
name|regionserver
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
name|io
operator|.
name|InterruptedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|ConnectException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|SocketTimeoutException
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
name|NotServingRegionException
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
name|Server
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
name|RetriesExhaustedException
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
name|coordination
operator|.
name|BaseCoordinatedStateManager
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
name|coordination
operator|.
name|SplitLogWorkerCoordination
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ZooKeeperProtos
operator|.
name|SplitLogTask
operator|.
name|RecoveryMode
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
name|WALFactory
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
name|WALSplitter
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
name|hbase
operator|.
name|util
operator|.
name|ExceptionUtil
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
name|FSUtils
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
comment|/**  * This worker is spawned in every regionserver, including master. The Worker waits for log  * splitting tasks to be put up by the {@link org.apache.hadoop.hbase.master.SplitLogManager}   * running in the master and races with other workers in other serves to acquire those tasks.   * The coordination is done via coordination engine.  *<p>  * If a worker has successfully moved the task from state UNASSIGNED to OWNED then it owns the task.  * It keeps heart beating the manager by periodically moving the task from UNASSIGNED to OWNED  * state. On success it moves the task to TASK_DONE. On unrecoverable error it moves task state to  * ERR. If it cannot continue but wants the master to retry the task then it moves the task state to  * RESIGNED.  *<p>  * The manager can take a task away from a worker by moving the task from OWNED to UNASSIGNED. In  * the absence of a global lock there is a unavoidable race here - a worker might have just finished  * its task when it is stripped of its ownership. Here we rely on the idempotency of the log  * splitting task for correctness  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SplitLogWorker
implements|implements
name|Runnable
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
name|SplitLogWorker
operator|.
name|class
argument_list|)
decl_stmt|;
name|Thread
name|worker
decl_stmt|;
comment|// thread pool which executes recovery work
specifier|private
name|SplitLogWorkerCoordination
name|coordination
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|RegionServerServices
name|server
decl_stmt|;
specifier|public
name|SplitLogWorker
parameter_list|(
name|Server
name|hserver
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|RegionServerServices
name|server
parameter_list|,
name|TaskExecutor
name|splitTaskExecutor
parameter_list|)
block|{
name|this
operator|.
name|server
operator|=
name|server
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|coordination
operator|=
operator|(
operator|(
name|BaseCoordinatedStateManager
operator|)
name|hserver
operator|.
name|getCoordinatedStateManager
argument_list|()
operator|)
operator|.
name|getSplitLogWorkerCoordination
argument_list|()
expr_stmt|;
name|this
operator|.
name|server
operator|=
name|server
expr_stmt|;
name|coordination
operator|.
name|init
argument_list|(
name|server
argument_list|,
name|conf
argument_list|,
name|splitTaskExecutor
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SplitLogWorker
parameter_list|(
specifier|final
name|Server
name|hserver
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|RegionServerServices
name|server
parameter_list|,
specifier|final
name|LastSequenceId
name|sequenceIdChecker
parameter_list|,
specifier|final
name|WALFactory
name|factory
parameter_list|)
block|{
name|this
argument_list|(
name|server
argument_list|,
name|conf
argument_list|,
name|server
argument_list|,
operator|new
name|TaskExecutor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Status
name|exec
parameter_list|(
name|String
name|filename
parameter_list|,
name|RecoveryMode
name|mode
parameter_list|,
name|CancelableProgressable
name|p
parameter_list|)
block|{
name|Path
name|walDir
decl_stmt|;
name|FileSystem
name|fs
decl_stmt|;
try|try
block|{
name|walDir
operator|=
name|FSUtils
operator|.
name|getWALRootDir
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|fs
operator|=
name|walDir
operator|.
name|getFileSystem
argument_list|(
name|conf
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
literal|"could not find root dir or fs"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
name|Status
operator|.
name|RESIGNED
return|;
block|}
comment|// TODO have to correctly figure out when log splitting has been
comment|// interrupted or has encountered a transient error and when it has
comment|// encountered a bad non-retry-able persistent error.
try|try
block|{
if|if
condition|(
operator|!
name|WALSplitter
operator|.
name|splitLogFile
argument_list|(
name|walDir
argument_list|,
name|fs
operator|.
name|getFileStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|walDir
argument_list|,
name|filename
argument_list|)
argument_list|)
argument_list|,
name|fs
argument_list|,
name|conf
argument_list|,
name|p
argument_list|,
name|sequenceIdChecker
argument_list|,
name|server
operator|.
name|getCoordinatedStateManager
argument_list|()
argument_list|,
name|mode
argument_list|,
name|factory
argument_list|)
condition|)
block|{
return|return
name|Status
operator|.
name|PREEMPTED
return|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedIOException
name|iioe
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"log splitting of "
operator|+
name|filename
operator|+
literal|" interrupted, resigning"
argument_list|,
name|iioe
argument_list|)
expr_stmt|;
return|return
name|Status
operator|.
name|RESIGNED
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|Throwable
name|cause
init|=
name|e
operator|.
name|getCause
argument_list|()
decl_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|RetriesExhaustedException
operator|&&
operator|(
name|cause
operator|instanceof
name|NotServingRegionException
operator|||
name|cause
operator|instanceof
name|ConnectException
operator|||
name|cause
operator|instanceof
name|SocketTimeoutException
operator|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"log replaying of "
operator|+
name|filename
operator|+
literal|" can't connect to the target regionserver, "
operator|+
literal|"resigning"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
name|Status
operator|.
name|RESIGNED
return|;
block|}
elseif|else
if|if
condition|(
name|cause
operator|instanceof
name|InterruptedException
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"log splitting of "
operator|+
name|filename
operator|+
literal|" interrupted, resigning"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
name|Status
operator|.
name|RESIGNED
return|;
block|}
name|LOG
operator|.
name|warn
argument_list|(
literal|"log splitting of "
operator|+
name|filename
operator|+
literal|" failed, returning error"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
name|Status
operator|.
name|ERR
return|;
block|}
return|return
name|Status
operator|.
name|DONE
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"SplitLogWorker "
operator|+
name|server
operator|.
name|getServerName
argument_list|()
operator|+
literal|" starting"
argument_list|)
expr_stmt|;
name|coordination
operator|.
name|registerListener
argument_list|()
expr_stmt|;
comment|// wait for Coordination Engine is ready
name|boolean
name|res
init|=
literal|false
decl_stmt|;
while|while
condition|(
operator|!
name|res
operator|&&
operator|!
name|coordination
operator|.
name|isStop
argument_list|()
condition|)
block|{
name|res
operator|=
name|coordination
operator|.
name|isReady
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|coordination
operator|.
name|isStop
argument_list|()
condition|)
block|{
name|coordination
operator|.
name|taskLoop
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|ExceptionUtil
operator|.
name|isInterrupt
argument_list|(
name|t
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"SplitLogWorker interrupted. Exiting. "
operator|+
operator|(
name|coordination
operator|.
name|isStop
argument_list|()
condition|?
literal|""
else|:
literal|" (ERROR: exitWorker is not set, exiting anyway)"
operator|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// only a logical error can cause here. Printing it out
comment|// to make debugging easier
name|LOG
operator|.
name|error
argument_list|(
literal|"unexpected error "
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|coordination
operator|.
name|removeListener
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"SplitLogWorker "
operator|+
name|server
operator|.
name|getServerName
argument_list|()
operator|+
literal|" exiting"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * If the worker is doing a task i.e. splitting a log file then stop the task.    * It doesn't exit the worker thread.    */
specifier|public
name|void
name|stopTask
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Sending interrupt to stop the worker thread"
argument_list|)
expr_stmt|;
name|worker
operator|.
name|interrupt
argument_list|()
expr_stmt|;
comment|// TODO interrupt often gets swallowed, do what else?
block|}
comment|/**    * start the SplitLogWorker thread    */
specifier|public
name|void
name|start
parameter_list|()
block|{
name|worker
operator|=
operator|new
name|Thread
argument_list|(
literal|null
argument_list|,
name|this
argument_list|,
literal|"SplitLogWorker-"
operator|+
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|toShortString
argument_list|()
argument_list|)
expr_stmt|;
name|worker
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
comment|/**    * stop the SplitLogWorker thread    */
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|coordination
operator|.
name|stopProcessingTasks
argument_list|()
expr_stmt|;
name|stopTask
argument_list|()
expr_stmt|;
block|}
comment|/**    * Objects implementing this interface actually do the task that has been    * acquired by a {@link SplitLogWorker}. Since there isn't a water-tight    * guarantee that two workers will not be executing the same task therefore it    * is better to have workers prepare the task and then have the    * {@link org.apache.hadoop.hbase.master.SplitLogManager} commit the work in     * SplitLogManager.TaskFinisher    */
specifier|public
interface|interface
name|TaskExecutor
block|{
enum|enum
name|Status
block|{
name|DONE
parameter_list|()
operator|,
constructor|ERR(
block|)
enum|,
name|RESIGNED
parameter_list|()
operator|,
constructor|PREEMPTED(
block|)
block|}
end_class

begin_function_decl
name|Status
name|exec
parameter_list|(
name|String
name|name
parameter_list|,
name|RecoveryMode
name|mode
parameter_list|,
name|CancelableProgressable
name|p
parameter_list|)
function_decl|;
end_function_decl

begin_comment
unit|}
comment|/**    * Returns the number of tasks processed by coordination.    * This method is used by tests only    */
end_comment

begin_function
unit|@
name|VisibleForTesting
specifier|public
name|int
name|getTaskReadySeq
parameter_list|()
block|{
return|return
name|coordination
operator|.
name|getTaskReadySeq
argument_list|()
return|;
block|}
end_function

unit|}
end_unit

