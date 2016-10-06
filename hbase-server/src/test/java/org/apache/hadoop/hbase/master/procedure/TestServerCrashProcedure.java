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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|MiniHBaseCluster
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
name|ResultScanner
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
name|Scan
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
name|LargeTests
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
name|Before
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
import|;
end_import

begin_comment
comment|/**  * It used to first run with DLS and then DLR but HBASE-12751 broke DLR so we disabled it here.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|LargeTests
operator|.
name|class
argument_list|)
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestServerCrashProcedure
block|{
comment|// Ugly junit parameterization. I just want to pass false and then true but seems like needs
comment|// to return sequences of two-element arrays.
annotation|@
name|Parameters
argument_list|(
name|name
operator|=
literal|"{index}: setting={0}"
argument_list|)
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|data
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Object
index|[]
index|[]
block|{
block|{
name|Boolean
operator|.
name|FALSE
block|,
operator|-
literal|1
block|}
block|}
argument_list|)
return|;
block|}
specifier|private
specifier|final
name|HBaseTestingUtility
name|util
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|util
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|setKillAndToggleBeforeStoreUpdate
argument_list|(
name|this
operator|.
name|util
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
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
name|MiniHBaseCluster
name|cluster
init|=
name|this
operator|.
name|util
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|HMaster
name|master
init|=
name|cluster
operator|==
literal|null
condition|?
literal|null
else|:
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
if|if
condition|(
name|master
operator|!=
literal|null
operator|&&
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|ProcedureTestingUtility
operator|.
name|setKillAndToggleBeforeStoreUpdate
argument_list|(
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|util
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|public
name|TestServerCrashProcedure
parameter_list|(
specifier|final
name|Boolean
name|b
parameter_list|,
specifier|final
name|int
name|ignore
parameter_list|)
block|{
name|this
operator|.
name|util
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hbase.master.distributed.log.replay"
argument_list|,
name|b
argument_list|)
expr_stmt|;
name|this
operator|.
name|util
operator|.
name|getConfiguration
argument_list|()
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
block|}
comment|/**    * Run server crash procedure steps twice to test idempotency and that we are persisting all    * needed state.    * @throws Exception    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testRecoveryAndDoubleExecutionOnline
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testRecoveryAndDoubleExecutionOnline"
argument_list|)
decl_stmt|;
name|this
operator|.
name|util
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|HBaseTestingUtility
operator|.
name|COLUMNS
argument_list|,
name|HBaseTestingUtility
operator|.
name|KEYS_FOR_HBA_CREATE_TABLE
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|t
init|=
name|this
operator|.
name|util
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
name|this
operator|.
name|util
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
name|int
name|count
init|=
name|util
operator|.
name|countRows
argument_list|(
name|t
argument_list|)
decl_stmt|;
comment|// Run the procedure executor outside the master so we can mess with it. Need to disable
comment|// Master's running of the server crash processing.
name|HMaster
name|master
init|=
name|this
operator|.
name|util
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
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
name|master
operator|.
name|setServerCrashProcessingEnabled
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// Kill a server. Master will notice but do nothing other than add it to list of dead servers.
name|HRegionServer
name|hrs
init|=
name|this
operator|.
name|util
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
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
name|this
operator|.
name|util
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
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
comment|// Now, reenable processing else we can't get a lock on the ServerCrashProcedure.
name|master
operator|.
name|setServerCrashProcessingEnabled
argument_list|(
literal|true
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
comment|// Enable test flags and then queue the crash procedure.
name|ProcedureTestingUtility
operator|.
name|waitNoProcedureRunning
argument_list|(
name|procExec
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|setKillAndToggleBeforeStoreUpdate
argument_list|(
name|procExec
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
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
decl_stmt|;
comment|// Now run through the procedure twice crashing the executor on each step...
name|MasterProcedureTestingUtility
operator|.
name|testRecoveryAndDoubleExecution
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
comment|// Assert all data came back.
name|assertEquals
argument_list|(
name|count
argument_list|,
name|util
operator|.
name|countRows
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

