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
name|util
operator|.
name|HashMap
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
name|impl
operator|.
name|BackupSystemTable
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
name|backup
operator|.
name|master
operator|.
name|LogRollMasterProcedureManager
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
name|Connection
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
name|hadoop
operator|.
name|hbase
operator|.
name|procedure
operator|.
name|ProcedureMember
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
name|procedure
operator|.
name|Subprocedure
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
name|HRegionServer
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
name|regionserver
operator|.
name|wal
operator|.
name|AbstractFSWAL
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
name|EnvironmentEdgeManager
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
name|WAL
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
comment|/**  * This backup sub-procedure implementation forces a WAL rolling on a RS.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|LogRollBackupSubprocedure
extends|extends
name|Subprocedure
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
name|LogRollBackupSubprocedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|RegionServerServices
name|rss
decl_stmt|;
specifier|private
specifier|final
name|LogRollBackupSubprocedurePool
name|taskManager
decl_stmt|;
specifier|private
name|String
name|backupRoot
decl_stmt|;
specifier|public
name|LogRollBackupSubprocedure
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
name|long
name|wakeFrequency
parameter_list|,
name|long
name|timeout
parameter_list|,
name|LogRollBackupSubprocedurePool
name|taskManager
parameter_list|,
name|byte
index|[]
name|data
parameter_list|)
block|{
name|super
argument_list|(
name|member
argument_list|,
name|LogRollMasterProcedureManager
operator|.
name|ROLLLOG_PROCEDURE_NAME
argument_list|,
name|errorListener
argument_list|,
name|wakeFrequency
argument_list|,
name|timeout
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Constructing a LogRollBackupSubprocedure."
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
if|if
condition|(
name|data
operator|!=
literal|null
condition|)
block|{
name|backupRoot
operator|=
operator|new
name|String
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Callable task. TODO. We don't need a thread pool to execute roll log. This can be simplified    * with no use of sub-procedure pool.    */
class|class
name|RSRollLogTask
implements|implements
name|Callable
argument_list|<
name|Void
argument_list|>
block|{
name|RSRollLogTask
parameter_list|()
block|{     }
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
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
literal|"DRPC started: "
operator|+
name|rss
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|AbstractFSWAL
argument_list|<
name|?
argument_list|>
name|fsWAL
init|=
operator|(
name|AbstractFSWAL
argument_list|<
name|?
argument_list|>
operator|)
name|rss
operator|.
name|getWAL
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|long
name|filenum
init|=
name|fsWAL
operator|.
name|getFilenum
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|WAL
argument_list|>
name|wals
init|=
name|rss
operator|.
name|getWALs
argument_list|()
decl_stmt|;
name|long
name|highest
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|WAL
name|wal
range|:
name|wals
control|)
block|{
if|if
condition|(
name|wal
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
operator|(
operator|(
name|AbstractFSWAL
argument_list|<
name|?
argument_list|>
operator|)
name|wal
operator|)
operator|.
name|getFilenum
argument_list|()
operator|>
name|highest
condition|)
block|{
name|highest
operator|=
operator|(
operator|(
name|AbstractFSWAL
argument_list|<
name|?
argument_list|>
operator|)
name|wal
operator|)
operator|.
name|getFilenum
argument_list|()
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Trying to roll log in backup subprocedure, current log number: "
operator|+
name|filenum
operator|+
literal|" highest: "
operator|+
name|highest
operator|+
literal|" on "
operator|+
name|rss
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
operator|(
operator|(
name|HRegionServer
operator|)
name|rss
operator|)
operator|.
name|getWalRoller
argument_list|()
operator|.
name|requestRollAll
argument_list|()
expr_stmt|;
name|long
name|start
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
while|while
condition|(
operator|!
operator|(
operator|(
name|HRegionServer
operator|)
name|rss
operator|)
operator|.
name|getWalRoller
argument_list|()
operator|.
name|walRollFinished
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|20
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"log roll took "
operator|+
operator|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|start
operator|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"After roll log in backup subprocedure, current log number: "
operator|+
name|fsWAL
operator|.
name|getFilenum
argument_list|()
operator|+
literal|" on "
operator|+
name|rss
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|Connection
name|connection
init|=
name|rss
operator|.
name|getConnection
argument_list|()
decl_stmt|;
try|try
init|(
specifier|final
name|BackupSystemTable
name|table
init|=
operator|new
name|BackupSystemTable
argument_list|(
name|connection
argument_list|)
init|)
block|{
comment|// sanity check, good for testing
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|serverTimestampMap
init|=
name|table
operator|.
name|readRegionServerLastLogRollResult
argument_list|(
name|backupRoot
argument_list|)
decl_stmt|;
name|String
name|host
init|=
name|rss
operator|.
name|getServerName
argument_list|()
operator|.
name|getHostname
argument_list|()
decl_stmt|;
name|int
name|port
init|=
name|rss
operator|.
name|getServerName
argument_list|()
operator|.
name|getPort
argument_list|()
decl_stmt|;
name|String
name|server
init|=
name|host
operator|+
literal|":"
operator|+
name|port
decl_stmt|;
name|Long
name|sts
init|=
name|serverTimestampMap
operator|.
name|get
argument_list|(
name|host
argument_list|)
decl_stmt|;
if|if
condition|(
name|sts
operator|!=
literal|null
operator|&&
name|sts
operator|>
name|highest
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Won't update server's last roll log result: current="
operator|+
name|sts
operator|+
literal|" new="
operator|+
name|highest
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
comment|// write the log number to backup system table.
name|table
operator|.
name|writeRegionServerLastLogRollResult
argument_list|(
name|server
argument_list|,
name|highest
argument_list|,
name|backupRoot
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
block|}
specifier|private
name|void
name|rolllog
parameter_list|()
throws|throws
name|ForeignException
block|{
name|monitor
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
name|taskManager
operator|.
name|submitTask
argument_list|(
operator|new
name|RSRollLogTask
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
block|{
comment|// do nothing, executing in inside barrier step.
block|}
comment|/**    * do a log roll.    * @return some bytes    */
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
name|rolllog
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
comment|/**    * Cancel threads if they haven't finished.    */
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
literal|"Aborting log roll subprocedure tasks for backup due to error"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
comment|/**    * Hooray!    */
specifier|public
name|void
name|releaseBarrier
parameter_list|()
block|{
comment|// NO OP
block|}
block|}
end_class

end_unit

