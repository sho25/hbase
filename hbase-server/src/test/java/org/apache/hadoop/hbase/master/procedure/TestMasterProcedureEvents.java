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
name|procedure
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
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
name|HBaseTestingUtility
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
name|HColumnDescriptor
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
name|HRegionInfo
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
name|HTableDescriptor
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
name|TableName
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
name|Table
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
name|HMaster
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
name|Procedure
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
name|ProcedureEvent
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
name|ProcedureExecutor
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
name|store
operator|.
name|wal
operator|.
name|WALProcedureStore
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
name|testclassification
operator|.
name|MasterTests
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
name|testclassification
operator|.
name|MediumTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestMasterProcedureEvents
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
name|TestCreateTableProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|void
name|setupConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|conf
operator|.
name|setInt
argument_list|(
name|MasterProcedureConstants
operator|.
name|MASTER_PROCEDURE_THREADS
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|WALProcedureStore
operator|.
name|USE_HSYNC_CONF_KEY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|setupConf
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitUntilNoRegionsInTransition
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|cleanupTest
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"failure shutting down cluster"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|HTableDescriptor
name|htd
range|:
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|listTables
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Tear down, remove table="
operator|+
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|deleteTable
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testMasterInitializedEvent
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testMasterInitializedEvent"
argument_list|)
decl_stmt|;
name|HMaster
name|master
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
init|=
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
decl_stmt|;
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
literal|"f"
argument_list|)
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|master
operator|.
name|isInitialized
argument_list|()
condition|)
name|Thread
operator|.
name|sleep
argument_list|(
literal|250
argument_list|)
expr_stmt|;
name|master
operator|.
name|setInitialized
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// fake it, set back later
comment|// check event wait/wake
name|testProcedureEventWaitWake
argument_list|(
name|master
argument_list|,
name|master
operator|.
name|getInitializedEvent
argument_list|()
argument_list|,
operator|new
name|CreateTableProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd
argument_list|,
operator|new
name|HRegionInfo
index|[]
block|{
name|hri
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testServerCrashProcedureEvent
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testServerCrashProcedureEventTb"
argument_list|)
decl_stmt|;
name|HMaster
name|master
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
init|=
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
decl_stmt|;
while|while
condition|(
operator|!
name|master
operator|.
name|isServerCrashProcessingEnabled
argument_list|()
operator|||
operator|!
name|master
operator|.
name|isInitialized
argument_list|()
operator|||
name|master
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|isRegionsInTransition
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|25
argument_list|)
expr_stmt|;
block|}
name|UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|HBaseTestingUtility
operator|.
name|COLUMNS
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|t
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
comment|// Load the table with a bit of data so some logs to split and some edits in each region.
name|UTIL
operator|.
name|loadTable
argument_list|(
name|t
argument_list|,
name|HBaseTestingUtility
operator|.
name|COLUMNS
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
name|master
operator|.
name|setServerCrashProcessingEnabled
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// fake it, set back later
comment|// Kill a server. Master will notice but do nothing other than add it to list of dead servers.
name|HRegionServer
name|hrs
init|=
name|getServerWithRegions
argument_list|()
decl_stmt|;
name|boolean
name|carryingMeta
init|=
name|master
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|isCarryingMeta
argument_list|(
name|hrs
operator|.
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|killRegionServer
argument_list|(
name|hrs
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|hrs
operator|.
name|join
argument_list|()
expr_stmt|;
comment|// Wait until the expiration of the server has arrived at the master. We won't process it
comment|// by queuing a ServerCrashProcedure because we have disabled crash processing... but wait
comment|// here so ServerManager gets notice and adds expired server to appropriate queues.
while|while
condition|(
operator|!
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|isServerDead
argument_list|(
name|hrs
operator|.
name|getServerName
argument_list|()
argument_list|)
condition|)
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
comment|// Do some of the master processing of dead servers so when SCP runs, it has expected 'state'.
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|moveFromOnelineToDeadServers
argument_list|(
name|hrs
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
comment|// check event wait/wake
name|testProcedureEventWaitWake
argument_list|(
name|master
argument_list|,
name|master
operator|.
name|getServerCrashProcessingEnabledEvent
argument_list|()
argument_list|,
operator|new
name|ServerCrashProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|hrs
operator|.
name|getServerName
argument_list|()
argument_list|,
literal|true
argument_list|,
name|carryingMeta
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testProcedureEventWaitWake
parameter_list|(
specifier|final
name|HMaster
name|master
parameter_list|,
specifier|final
name|ProcedureEvent
name|event
parameter_list|,
specifier|final
name|Procedure
name|proc
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
init|=
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
decl_stmt|;
specifier|final
name|MasterProcedureScheduler
name|procSched
init|=
name|procExec
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getProcedureScheduler
argument_list|()
decl_stmt|;
specifier|final
name|long
name|startPollCalls
init|=
name|procSched
operator|.
name|getPollCalls
argument_list|()
decl_stmt|;
specifier|final
name|long
name|startNullPollCalls
init|=
name|procSched
operator|.
name|getNullPollCalls
argument_list|()
decl_stmt|;
comment|// check that nothing is in the event queue
name|LOG
operator|.
name|debug
argument_list|(
literal|"checking "
operator|+
name|event
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|event
operator|.
name|isReady
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|event
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// submit the procedure
name|LOG
operator|.
name|debug
argument_list|(
literal|"submit "
operator|+
name|proc
argument_list|)
expr_stmt|;
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
name|proc
argument_list|)
decl_stmt|;
comment|// wait until the event is in the queue (proc executed and got into suspended state)
name|LOG
operator|.
name|debug
argument_list|(
literal|"wait procedure suspended on "
operator|+
name|event
argument_list|)
expr_stmt|;
while|while
condition|(
name|event
operator|.
name|size
argument_list|()
operator|<
literal|1
condition|)
name|Thread
operator|.
name|sleep
argument_list|(
literal|25
argument_list|)
expr_stmt|;
comment|// check that the proc is in the event queue
name|LOG
operator|.
name|debug
argument_list|(
literal|"checking "
operator|+
name|event
operator|+
literal|" size="
operator|+
name|event
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|event
operator|.
name|isReady
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|event
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// wake the event
name|LOG
operator|.
name|debug
argument_list|(
literal|"wake "
operator|+
name|event
argument_list|)
expr_stmt|;
name|procSched
operator|.
name|wakeEvent
argument_list|(
name|event
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|event
operator|.
name|isReady
argument_list|()
argument_list|)
expr_stmt|;
comment|// wait until proc completes
name|LOG
operator|.
name|debug
argument_list|(
literal|"waiting "
operator|+
name|proc
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
comment|// check that nothing is in the event queue and the event is not suspended
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|event
operator|.
name|isReady
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|event
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"completed execution of "
operator|+
name|proc
operator|+
literal|" pollCalls="
operator|+
operator|(
name|procSched
operator|.
name|getPollCalls
argument_list|()
operator|-
name|startPollCalls
operator|)
operator|+
literal|" nullPollCalls="
operator|+
operator|(
name|procSched
operator|.
name|getNullPollCalls
argument_list|()
operator|-
name|startNullPollCalls
operator|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HRegionServer
name|getServerWithRegions
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
literal|3
condition|;
operator|++
name|i
control|)
block|{
name|HRegionServer
name|hrs
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|hrs
operator|.
name|getNumberOfOnlineRegions
argument_list|()
operator|>
literal|0
condition|)
block|{
return|return
name|hrs
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

