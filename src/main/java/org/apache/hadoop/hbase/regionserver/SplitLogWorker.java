begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
import|import static
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
name|ZKSplitLog
operator|.
name|Counters
operator|.
name|*
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
name|io
operator|.
name|InterruptedIOException
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
name|master
operator|.
name|SplitLogManager
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
name|wal
operator|.
name|HLogSplitter
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
name|FSUtils
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
name|ZKSplitLog
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
name|ZKSplitLog
operator|.
name|TaskState
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
name|ZKUtil
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
name|ZooKeeperListener
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
name|zookeeper
operator|.
name|AsyncCallback
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|data
operator|.
name|Stat
import|;
end_import

begin_comment
comment|/**  * This worker is spawned in every regionserver (should we also spawn one in  * the master?). The Worker waits for log splitting tasks to be put up by the  * {@link SplitLogManager} running in the master and races with other workers  * in other serves to acquire those tasks. The coordination is done via  * zookeeper. All the action takes place at /hbase/splitlog znode.  *<p>  * If a worker has successfully moved the task from state UNASSIGNED to  * OWNED then it owns the task. It keeps heart beating the manager by  * periodically moving the task from OWNED to OWNED state. On success it  * moves the task to SUCCESS. On unrecoverable error it moves task state to  * ERR. If it cannot continue but wants the master to retry the task then it  * moves the task state to RESIGNED.  *<p>  * The manager can take a task away from a worker by moving the task from  * OWNED to UNASSIGNED. In the absence of a global lock there is a  * unavoidable race here - a worker might have just finished its task when it  * is stripped of its ownership. Here we rely on the idempotency of the log  * splitting task for correctness  */
end_comment

begin_class
specifier|public
class|class
name|SplitLogWorker
extends|extends
name|ZooKeeperListener
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
specifier|private
specifier|final
name|String
name|serverName
decl_stmt|;
specifier|private
specifier|final
name|TaskExecutor
name|executor
decl_stmt|;
specifier|private
name|long
name|zkretries
decl_stmt|;
specifier|private
name|Object
name|taskReadyLock
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
specifier|volatile
name|int
name|taskReadySeq
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|String
name|currentTask
init|=
literal|null
decl_stmt|;
specifier|private
name|int
name|currentVersion
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|exitWorker
decl_stmt|;
specifier|private
name|Object
name|grabTaskLock
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|workerInGrabTask
init|=
literal|false
decl_stmt|;
specifier|public
name|SplitLogWorker
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|String
name|serverName
parameter_list|,
name|TaskExecutor
name|executor
parameter_list|)
block|{
name|super
argument_list|(
name|watcher
argument_list|)
expr_stmt|;
name|this
operator|.
name|serverName
operator|=
name|serverName
expr_stmt|;
name|this
operator|.
name|executor
operator|=
name|executor
expr_stmt|;
name|this
operator|.
name|zkretries
operator|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.splitlog.zk.retries"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SplitLogWorker
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|String
name|serverName
parameter_list|)
block|{
name|this
argument_list|(
name|watcher
argument_list|,
name|conf
argument_list|,
name|serverName
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
name|CancelableProgressable
name|p
parameter_list|)
block|{
name|Path
name|rootdir
decl_stmt|;
name|FileSystem
name|fs
decl_stmt|;
try|try
block|{
name|rootdir
operator|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|fs
operator|=
name|rootdir
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
name|String
name|tmpname
init|=
name|ZKSplitLog
operator|.
name|getSplitLogDirTmpComponent
argument_list|(
name|serverName
argument_list|,
name|filename
argument_list|)
decl_stmt|;
if|if
condition|(
name|HLogSplitter
operator|.
name|splitLogFileToTemp
argument_list|(
name|rootdir
argument_list|,
name|tmpname
argument_list|,
name|fs
operator|.
name|getFileStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|filename
argument_list|)
argument_list|)
argument_list|,
name|fs
argument_list|,
name|conf
argument_list|,
name|p
argument_list|)
operator|==
literal|false
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
name|LOG
operator|.
name|info
argument_list|(
literal|"SplitLogWorker starting"
argument_list|)
expr_stmt|;
name|this
operator|.
name|watcher
operator|.
name|registerListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|int
name|res
decl_stmt|;
comment|// wait for master to create the splitLogZnode
name|res
operator|=
operator|-
literal|1
expr_stmt|;
while|while
condition|(
name|res
operator|==
operator|-
literal|1
condition|)
block|{
try|try
block|{
name|res
operator|=
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|splitLogZNode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
comment|// ignore
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception when checking for "
operator|+
name|watcher
operator|.
name|splitLogZNode
operator|+
literal|" ... retrying"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|res
operator|==
operator|-
literal|1
condition|)
block|{
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
name|watcher
operator|.
name|splitLogZNode
operator|+
literal|" znode does not exist,"
operator|+
literal|" waiting for master to create one"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
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
name|debug
argument_list|(
literal|"Interrupted while waiting for "
operator|+
name|watcher
operator|.
name|splitLogZNode
argument_list|)
expr_stmt|;
assert|assert
name|exitWorker
operator|==
literal|true
assert|;
block|}
block|}
block|}
name|taskLoop
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"SplitLogWorker exiting"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Wait for tasks to become available at /hbase/splitlog zknode. Grab a task    * one at a time. This policy puts an upper-limit on the number of    * simultaneous log splitting that could be happening in a cluster.    *<p>    * Synchronization using {@link #task_ready_signal_seq} ensures that it will    * try to grab every task that has been put up    */
specifier|private
name|void
name|taskLoop
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|seq_start
init|=
name|taskReadySeq
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|paths
init|=
name|getTaskList
argument_list|()
decl_stmt|;
if|if
condition|(
name|paths
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Could not get tasks, did someone remove "
operator|+
name|this
operator|.
name|watcher
operator|.
name|splitLogZNode
operator|+
literal|" ... worker thread exiting."
argument_list|)
expr_stmt|;
return|return;
block|}
name|int
name|offset
init|=
call|(
name|int
call|)
argument_list|(
name|Math
operator|.
name|random
argument_list|()
operator|*
name|paths
operator|.
name|size
argument_list|()
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
name|paths
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|int
name|idx
init|=
operator|(
name|i
operator|+
name|offset
operator|)
operator|%
name|paths
operator|.
name|size
argument_list|()
decl_stmt|;
comment|// don't call ZKSplitLog.getNodeName() because that will lead to
comment|// double encoding of the path name
name|grabTask
argument_list|(
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|watcher
operator|.
name|splitLogZNode
argument_list|,
name|paths
operator|.
name|get
argument_list|(
name|idx
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|exitWorker
operator|==
literal|true
condition|)
block|{
return|return;
block|}
block|}
synchronized|synchronized
init|(
name|taskReadyLock
init|)
block|{
while|while
condition|(
name|seq_start
operator|==
name|taskReadySeq
condition|)
block|{
try|try
block|{
name|taskReadyLock
operator|.
name|wait
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
name|warn
argument_list|(
literal|"SplitLogWorker inteurrpted while waiting for task,"
operator|+
literal|" exiting"
argument_list|,
name|e
argument_list|)
expr_stmt|;
assert|assert
name|exitWorker
operator|==
literal|true
assert|;
return|return;
block|}
block|}
block|}
block|}
block|}
comment|/**    * try to grab a 'lock' on the task zk node to own and execute the task.    *<p>    * @param path zk node for the task    */
specifier|private
name|void
name|grabTask
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|Stat
name|stat
init|=
operator|new
name|Stat
argument_list|()
decl_stmt|;
name|long
name|t
init|=
operator|-
literal|1
decl_stmt|;
name|byte
index|[]
name|data
decl_stmt|;
synchronized|synchronized
init|(
name|grabTaskLock
init|)
block|{
name|currentTask
operator|=
name|path
expr_stmt|;
name|workerInGrabTask
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|Thread
operator|.
name|interrupted
argument_list|()
condition|)
block|{
return|return;
block|}
block|}
try|try
block|{
try|try
block|{
if|if
condition|(
operator|(
name|data
operator|=
name|ZKUtil
operator|.
name|getDataNoWatch
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|path
argument_list|,
name|stat
argument_list|)
operator|)
operator|==
literal|null
condition|)
block|{
name|tot_wkr_failed_to_grab_task_no_data
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
return|return;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to get data for znode "
operator|+
name|path
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|tot_wkr_failed_to_grab_task_exception
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|TaskState
operator|.
name|TASK_UNASSIGNED
operator|.
name|equals
argument_list|(
name|data
argument_list|)
operator|==
literal|false
condition|)
block|{
name|tot_wkr_failed_to_grab_task_owned
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
return|return;
block|}
name|currentVersion
operator|=
name|stat
operator|.
name|getVersion
argument_list|()
expr_stmt|;
if|if
condition|(
name|ownTask
argument_list|()
operator|==
literal|false
condition|)
block|{
name|tot_wkr_failed_to_grab_task_lost_race
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|ZKSplitLog
operator|.
name|isRescanNode
argument_list|(
name|watcher
argument_list|,
name|currentTask
argument_list|)
condition|)
block|{
name|endTask
argument_list|(
name|TaskState
operator|.
name|TASK_DONE
argument_list|,
name|tot_wkr_task_acquired_rescan
argument_list|)
expr_stmt|;
return|return;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"worker "
operator|+
name|serverName
operator|+
literal|" acquired task "
operator|+
name|path
argument_list|)
expr_stmt|;
name|tot_wkr_task_acquired
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|getDataSetWatchAsync
argument_list|()
expr_stmt|;
name|t
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|TaskExecutor
operator|.
name|Status
name|status
decl_stmt|;
name|status
operator|=
name|executor
operator|.
name|exec
argument_list|(
name|ZKSplitLog
operator|.
name|getFileName
argument_list|(
name|currentTask
argument_list|)
argument_list|,
operator|new
name|CancelableProgressable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|progress
parameter_list|()
block|{
if|if
condition|(
name|ownTask
argument_list|()
operator|==
literal|false
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to heartbeat the task"
operator|+
name|currentTask
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|status
condition|)
block|{
case|case
name|DONE
case|:
name|endTask
argument_list|(
name|TaskState
operator|.
name|TASK_DONE
argument_list|,
name|tot_wkr_task_done
argument_list|)
expr_stmt|;
break|break;
case|case
name|PREEMPTED
case|:
name|tot_wkr_preempt_task
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"task execution prempted "
operator|+
name|path
argument_list|)
expr_stmt|;
break|break;
case|case
name|ERR
case|:
if|if
condition|(
operator|!
name|exitWorker
condition|)
block|{
name|endTask
argument_list|(
name|TaskState
operator|.
name|TASK_ERR
argument_list|,
name|tot_wkr_task_err
argument_list|)
expr_stmt|;
break|break;
block|}
comment|// if the RS is exiting then there is probably a tons of stuff
comment|// that can go wrong. Resign instead of signaling error.
comment|//$FALL-THROUGH$
case|case
name|RESIGNED
case|:
if|if
condition|(
name|exitWorker
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"task execution interrupted because worker is exiting "
operator|+
name|path
argument_list|)
expr_stmt|;
name|endTask
argument_list|(
name|TaskState
operator|.
name|TASK_RESIGNED
argument_list|,
name|tot_wkr_task_resigned
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tot_wkr_preempt_task
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"task execution interrupted via zk by manager "
operator|+
name|path
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|t
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"worker "
operator|+
name|serverName
operator|+
literal|" done with task "
operator|+
name|path
operator|+
literal|" in "
operator|+
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|t
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|grabTaskLock
init|)
block|{
name|workerInGrabTask
operator|=
literal|false
expr_stmt|;
comment|// clear the interrupt from stopTask() otherwise the next task will
comment|// suffer
name|Thread
operator|.
name|interrupted
argument_list|()
expr_stmt|;
block|}
block|}
return|return;
block|}
comment|/**    * Try to own the task by transitioning the zk node data from UNASSIGNED to    * OWNED.    *<p>    * This method is also used to periodically heartbeat the task progress by    * transitioning the node from OWNED to OWNED.    *<p>    * @return true if task path is successfully locked    */
specifier|private
name|boolean
name|ownTask
parameter_list|()
block|{
try|try
block|{
name|Stat
name|stat
init|=
name|this
operator|.
name|watcher
operator|.
name|getZooKeeper
argument_list|()
operator|.
name|setData
argument_list|(
name|currentTask
argument_list|,
name|TaskState
operator|.
name|TASK_OWNED
operator|.
name|get
argument_list|(
name|serverName
argument_list|)
argument_list|,
name|currentVersion
argument_list|)
decl_stmt|;
if|if
condition|(
name|stat
operator|==
literal|null
condition|)
block|{
return|return
operator|(
literal|false
operator|)
return|;
block|}
name|currentVersion
operator|=
name|stat
operator|.
name|getVersion
argument_list|()
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
literal|"hearbeat for path "
operator|+
name|currentTask
operator|+
literal|" successful, version = "
operator|+
name|currentVersion
argument_list|)
expr_stmt|;
block|}
name|tot_wkr_task_heartbeat
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
return|return
operator|(
literal|true
operator|)
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
comment|// either Bad Version or Node has been removed
name|LOG
operator|.
name|warn
argument_list|(
literal|"failed to assert ownership for "
operator|+
name|currentTask
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e1
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Interrupted while trying to assert ownership of "
operator|+
name|currentTask
operator|+
literal|" "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e1
argument_list|)
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
name|tot_wkr_task_heartbeat_failed
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
return|return
operator|(
literal|false
operator|)
return|;
block|}
comment|/**    * endTask() can fail and the only way to recover out of it is for the    * {@link SplitLogManager} to timeout the task node.    * @param ts    * @param ctr    */
specifier|private
name|void
name|endTask
parameter_list|(
name|ZKSplitLog
operator|.
name|TaskState
name|ts
parameter_list|,
name|AtomicLong
name|ctr
parameter_list|)
block|{
name|String
name|path
init|=
name|currentTask
decl_stmt|;
name|currentTask
operator|=
literal|null
expr_stmt|;
try|try
block|{
if|if
condition|(
name|ZKUtil
operator|.
name|setData
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|path
argument_list|,
name|ts
operator|.
name|get
argument_list|(
name|serverName
argument_list|)
argument_list|,
name|currentVersion
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"successfully transitioned task "
operator|+
name|path
operator|+
literal|" to final state "
operator|+
name|ts
argument_list|)
expr_stmt|;
name|ctr
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
return|return;
block|}
name|LOG
operator|.
name|warn
argument_list|(
literal|"failed to transistion task "
operator|+
name|path
operator|+
literal|" to end state "
operator|+
name|ts
operator|+
literal|" because of version mismatch "
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|BadVersionException
name|bve
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"transisition task "
operator|+
name|path
operator|+
literal|" to "
operator|+
name|ts
operator|+
literal|" failed because of version mismatch"
argument_list|,
name|bve
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NoNodeException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"logic error - end task "
operator|+
name|path
operator|+
literal|" "
operator|+
name|ts
operator|+
literal|" failed because task doesn't exist"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"failed to end task, "
operator|+
name|path
operator|+
literal|" "
operator|+
name|ts
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|tot_wkr_final_transistion_failed
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
return|return;
block|}
name|void
name|getDataSetWatchAsync
parameter_list|()
block|{
name|this
operator|.
name|watcher
operator|.
name|getZooKeeper
argument_list|()
operator|.
name|getData
argument_list|(
name|currentTask
argument_list|,
name|this
operator|.
name|watcher
argument_list|,
operator|new
name|GetDataAsyncCallback
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|tot_wkr_get_data_queued
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
name|void
name|getDataSetWatchSuccess
parameter_list|(
name|String
name|path
parameter_list|,
name|byte
index|[]
name|data
parameter_list|)
block|{
synchronized|synchronized
init|(
name|grabTaskLock
init|)
block|{
if|if
condition|(
name|workerInGrabTask
condition|)
block|{
comment|// currentTask can change but that's ok
name|String
name|taskpath
init|=
name|currentTask
decl_stmt|;
if|if
condition|(
name|taskpath
operator|!=
literal|null
operator|&&
name|taskpath
operator|.
name|equals
argument_list|(
name|path
argument_list|)
condition|)
block|{
comment|// have to compare data. cannot compare version because then there
comment|// will be race with ownTask()
comment|// cannot just check whether the node has been transitioned to
comment|// UNASSIGNED because by the time this worker sets the data watch
comment|// the node might have made two transitions - from owned by this
comment|// worker to unassigned to owned by another worker
if|if
condition|(
operator|!
name|TaskState
operator|.
name|TASK_OWNED
operator|.
name|equals
argument_list|(
name|data
argument_list|,
name|serverName
argument_list|)
operator|&&
operator|!
name|TaskState
operator|.
name|TASK_DONE
operator|.
name|equals
argument_list|(
name|data
argument_list|,
name|serverName
argument_list|)
operator|&&
operator|!
name|TaskState
operator|.
name|TASK_ERR
operator|.
name|equals
argument_list|(
name|data
argument_list|,
name|serverName
argument_list|)
operator|&&
operator|!
name|TaskState
operator|.
name|TASK_RESIGNED
operator|.
name|equals
argument_list|(
name|data
argument_list|,
name|serverName
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"task "
operator|+
name|taskpath
operator|+
literal|" preempted from server "
operator|+
name|serverName
operator|+
literal|" ... current task state and owner - "
operator|+
operator|new
name|String
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|stopTask
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
name|void
name|getDataSetWatchFailure
parameter_list|(
name|String
name|path
parameter_list|)
block|{
synchronized|synchronized
init|(
name|grabTaskLock
init|)
block|{
if|if
condition|(
name|workerInGrabTask
condition|)
block|{
comment|// currentTask can change but that's ok
name|String
name|taskpath
init|=
name|currentTask
decl_stmt|;
if|if
condition|(
name|taskpath
operator|!=
literal|null
operator|&&
name|taskpath
operator|.
name|equals
argument_list|(
name|path
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"retrying data watch on "
operator|+
name|path
argument_list|)
expr_stmt|;
name|tot_wkr_get_data_retry
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|getDataSetWatchAsync
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// no point setting a watch on the task which this worker is not
comment|// working upon anymore
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeDataChanged
parameter_list|(
name|String
name|path
parameter_list|)
block|{
comment|// there will be a self generated dataChanged event every time ownTask()
comment|// heartbeats the task znode by upping its version
synchronized|synchronized
init|(
name|grabTaskLock
init|)
block|{
if|if
condition|(
name|workerInGrabTask
condition|)
block|{
comment|// currentTask can change
name|String
name|taskpath
init|=
name|currentTask
decl_stmt|;
if|if
condition|(
name|taskpath
operator|!=
literal|null
operator|&&
name|taskpath
operator|.
name|equals
argument_list|(
name|path
argument_list|)
condition|)
block|{
name|getDataSetWatchAsync
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|getTaskList
parameter_list|()
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
name|zkretries
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
return|return
operator|(
name|ZKUtil
operator|.
name|listChildrenAndWatchForNewChildren
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|this
operator|.
name|watcher
operator|.
name|splitLogZNode
argument_list|)
operator|)
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Could not get children of znode "
operator|+
name|this
operator|.
name|watcher
operator|.
name|splitLogZNode
argument_list|,
name|e
argument_list|)
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e1
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Interrupted while trying to get task list ..."
argument_list|,
name|e1
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
block|}
name|LOG
operator|.
name|warn
argument_list|(
literal|"Tried "
operator|+
name|zkretries
operator|+
literal|" times, still couldn't fetch "
operator|+
literal|"children of "
operator|+
name|watcher
operator|.
name|splitLogZNode
operator|+
literal|" giving up"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeChildrenChanged
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|path
operator|.
name|equals
argument_list|(
name|watcher
operator|.
name|splitLogZNode
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"tasks arrived or departed"
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|taskReadyLock
init|)
block|{
name|taskReadySeq
operator|++
expr_stmt|;
name|taskReadyLock
operator|.
name|notify
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * If the worker is doing a task i.e. splitting a log file then stop the task.    * It doesn't exit the worker thread.    */
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
name|serverName
argument_list|)
expr_stmt|;
name|exitWorker
operator|=
literal|false
expr_stmt|;
name|worker
operator|.
name|start
argument_list|()
expr_stmt|;
return|return;
block|}
comment|/**    * stop the SplitLogWorker thread    */
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|exitWorker
operator|=
literal|true
expr_stmt|;
name|stopTask
argument_list|()
expr_stmt|;
block|}
comment|/**    * Asynchronous handler for zk get-data-set-watch on node results.    */
class|class
name|GetDataAsyncCallback
implements|implements
name|AsyncCallback
operator|.
name|DataCallback
block|{
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|GetDataAsyncCallback
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|processResult
parameter_list|(
name|int
name|rc
parameter_list|,
name|String
name|path
parameter_list|,
name|Object
name|ctx
parameter_list|,
name|byte
index|[]
name|data
parameter_list|,
name|Stat
name|stat
parameter_list|)
block|{
name|tot_wkr_get_data_result
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|rc
operator|!=
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"getdata rc = "
operator|+
name|KeeperException
operator|.
name|Code
operator|.
name|get
argument_list|(
name|rc
argument_list|)
operator|+
literal|" "
operator|+
name|path
argument_list|)
expr_stmt|;
name|getDataSetWatchFailure
argument_list|(
name|path
argument_list|)
expr_stmt|;
return|return;
block|}
name|getDataSetWatchSuccess
argument_list|(
name|path
argument_list|,
name|data
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
comment|/**    * Objects implementing this interface actually do the task that has been    * acquired by a {@link SplitLogWorker}. Since there isn't a water-tight    * guarantee that two workers will not be executing the same task therefore it    * is better to have workers prepare the task and then have the    * {@link SplitLogManager} commit the work in    * {@link SplitLogManager.TaskFinisher}    */
specifier|static
specifier|public
interface|interface
name|TaskExecutor
block|{
specifier|static
specifier|public
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
empty_stmt|;
block|}
end_class

begin_function_decl
specifier|public
name|Status
name|exec
parameter_list|(
name|String
name|name
parameter_list|,
name|CancelableProgressable
name|p
parameter_list|)
function_decl|;
end_function_decl

unit|} }
end_unit

