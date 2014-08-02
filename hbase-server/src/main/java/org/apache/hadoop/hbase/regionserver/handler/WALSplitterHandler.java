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
operator|.
name|handler
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
name|atomic
operator|.
name|AtomicInteger
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
name|ServerName
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
name|SplitLogCounters
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
name|SplitLogTask
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
name|executor
operator|.
name|EventHandler
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
name|executor
operator|.
name|EventType
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
name|regionserver
operator|.
name|SplitLogWorker
operator|.
name|TaskExecutor
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
name|SplitLogWorker
operator|.
name|TaskExecutor
operator|.
name|Status
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

begin_comment
comment|/**  * Handles log splitting a wal  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|WALSplitterHandler
extends|extends
name|EventHandler
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
name|WALSplitterHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ServerName
name|serverName
decl_stmt|;
specifier|private
specifier|final
name|CancelableProgressable
name|reporter
decl_stmt|;
specifier|private
specifier|final
name|AtomicInteger
name|inProgressTasks
decl_stmt|;
specifier|private
specifier|final
name|TaskExecutor
name|splitTaskExecutor
decl_stmt|;
specifier|private
specifier|final
name|RecoveryMode
name|mode
decl_stmt|;
specifier|private
specifier|final
name|SplitLogWorkerCoordination
operator|.
name|SplitTaskDetails
name|splitTaskDetails
decl_stmt|;
specifier|private
specifier|final
name|SplitLogWorkerCoordination
name|coordination
decl_stmt|;
specifier|public
name|WALSplitterHandler
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|,
name|SplitLogWorkerCoordination
name|coordination
parameter_list|,
name|SplitLogWorkerCoordination
operator|.
name|SplitTaskDetails
name|splitDetails
parameter_list|,
name|CancelableProgressable
name|reporter
parameter_list|,
name|AtomicInteger
name|inProgressTasks
parameter_list|,
name|TaskExecutor
name|splitTaskExecutor
parameter_list|,
name|RecoveryMode
name|mode
parameter_list|)
block|{
name|super
argument_list|(
name|server
argument_list|,
name|EventType
operator|.
name|RS_LOG_REPLAY
argument_list|)
expr_stmt|;
name|this
operator|.
name|splitTaskDetails
operator|=
name|splitDetails
expr_stmt|;
name|this
operator|.
name|coordination
operator|=
name|coordination
expr_stmt|;
name|this
operator|.
name|reporter
operator|=
name|reporter
expr_stmt|;
name|this
operator|.
name|inProgressTasks
operator|=
name|inProgressTasks
expr_stmt|;
name|this
operator|.
name|inProgressTasks
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|this
operator|.
name|serverName
operator|=
name|server
operator|.
name|getServerName
argument_list|()
expr_stmt|;
name|this
operator|.
name|splitTaskExecutor
operator|=
name|splitTaskExecutor
expr_stmt|;
name|this
operator|.
name|mode
operator|=
name|mode
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|()
throws|throws
name|IOException
block|{
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
try|try
block|{
name|Status
name|status
init|=
name|this
operator|.
name|splitTaskExecutor
operator|.
name|exec
argument_list|(
name|splitTaskDetails
operator|.
name|getWALFile
argument_list|()
argument_list|,
name|mode
argument_list|,
name|reporter
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|status
condition|)
block|{
case|case
name|DONE
case|:
name|coordination
operator|.
name|endTask
argument_list|(
operator|new
name|SplitLogTask
operator|.
name|Done
argument_list|(
name|this
operator|.
name|serverName
argument_list|,
name|this
operator|.
name|mode
argument_list|)
argument_list|,
name|SplitLogCounters
operator|.
name|tot_wkr_task_done
argument_list|,
name|splitTaskDetails
argument_list|)
expr_stmt|;
break|break;
case|case
name|PREEMPTED
case|:
name|SplitLogCounters
operator|.
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
name|splitTaskDetails
operator|.
name|getWALFile
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|ERR
case|:
if|if
condition|(
name|server
operator|!=
literal|null
operator|&&
operator|!
name|server
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|coordination
operator|.
name|endTask
argument_list|(
operator|new
name|SplitLogTask
operator|.
name|Err
argument_list|(
name|this
operator|.
name|serverName
argument_list|,
name|this
operator|.
name|mode
argument_list|)
argument_list|,
name|SplitLogCounters
operator|.
name|tot_wkr_task_err
argument_list|,
name|splitTaskDetails
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
name|server
operator|!=
literal|null
operator|&&
name|server
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"task execution interrupted because worker is exiting "
operator|+
name|splitTaskDetails
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|coordination
operator|.
name|endTask
argument_list|(
operator|new
name|SplitLogTask
operator|.
name|Resigned
argument_list|(
name|this
operator|.
name|serverName
argument_list|,
name|this
operator|.
name|mode
argument_list|)
argument_list|,
name|SplitLogCounters
operator|.
name|tot_wkr_task_resigned
argument_list|,
name|splitTaskDetails
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
finally|finally
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
name|splitTaskDetails
operator|.
name|toString
argument_list|()
operator|+
literal|" in "
operator|+
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|this
operator|.
name|inProgressTasks
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

