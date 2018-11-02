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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|HBaseClassTestRule
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
name|HConstants
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
name|RegionInfo
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
name|master
operator|.
name|assignment
operator|.
name|AssignmentTestingUtil
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
name|ProcedureMetrics
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
name|testclassification
operator|.
name|MasterTests
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
name|ClassRule
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestServerCrashProcedure
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestServerCrashProcedure
operator|.
name|class
argument_list|)
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
name|TestServerCrashProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|HBaseTestingUtility
name|util
decl_stmt|;
specifier|private
name|ProcedureMetrics
name|serverCrashProcMetrics
decl_stmt|;
specifier|private
name|long
name|serverCrashSubmittedCount
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|serverCrashFailedCount
init|=
literal|0
decl_stmt|;
specifier|private
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
name|set
argument_list|(
literal|"hbase.balancer.tablesOnMaster"
argument_list|,
literal|"none"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_SERVERSIDE_RETRIES_MULTIPLIER
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
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
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|setupConf
argument_list|(
name|this
operator|.
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|startMiniCluster
argument_list|()
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
name|serverCrashProcMetrics
operator|=
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
name|getMasterMetrics
argument_list|()
operator|.
name|getServerCrashProcMetrics
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|startMiniCluster
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
annotation|@
name|Test
specifier|public
name|void
name|testCrashTargetRs
parameter_list|()
throws|throws
name|Exception
block|{
name|testRecoveryAndDoubleExecution
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRecoveryAndDoubleExecutionOnRsWithMeta
parameter_list|()
throws|throws
name|Exception
block|{
name|testRecoveryAndDoubleExecution
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRecoveryAndDoubleExecutionOnRsWithoutMeta
parameter_list|()
throws|throws
name|Exception
block|{
name|testRecoveryAndDoubleExecution
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|long
name|getSCPProcId
parameter_list|(
name|ProcedureExecutor
argument_list|<
name|?
argument_list|>
name|procExec
parameter_list|)
block|{
name|util
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
parameter_list|()
lambda|->
operator|!
name|procExec
operator|.
name|getProcedures
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|procExec
operator|.
name|getActiveProcIds
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|mapToLong
argument_list|(
name|Long
operator|::
name|longValue
argument_list|)
operator|.
name|min
argument_list|()
operator|.
name|getAsLong
argument_list|()
return|;
block|}
comment|/**    * Run server crash procedure steps twice to test idempotency and that we are persisting all    * needed state.    */
specifier|private
name|void
name|testRecoveryAndDoubleExecution
parameter_list|(
name|boolean
name|carryingMeta
parameter_list|,
name|boolean
name|doubleExecution
parameter_list|)
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
literal|"testRecoveryAndDoubleExecution-carryingMeta-"
operator|+
name|carryingMeta
operator|+
literal|"-doubleExecution-"
operator|+
name|doubleExecution
argument_list|)
decl_stmt|;
try|try
init|(
name|Table
name|t
init|=
name|createTable
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
specifier|final
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
name|assertTrue
argument_list|(
literal|"expected some rows"
argument_list|,
name|count
operator|>
literal|0
argument_list|)
expr_stmt|;
specifier|final
name|String
name|checksum
init|=
name|util
operator|.
name|checksumRows
argument_list|(
name|t
argument_list|)
decl_stmt|;
comment|// Run the procedure executor outside the master so we can mess with it. Need to disable
comment|// Master's running of the server crash processing.
specifier|final
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
comment|// find the first server that match the request and executes the test
name|ServerName
name|rsToKill
init|=
literal|null
decl_stmt|;
for|for
control|(
name|RegionInfo
name|hri
range|:
name|util
operator|.
name|getAdmin
argument_list|()
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
control|)
block|{
specifier|final
name|ServerName
name|serverName
init|=
name|AssignmentTestingUtil
operator|.
name|getServerHoldingRegion
argument_list|(
name|util
argument_list|,
name|hri
argument_list|)
decl_stmt|;
if|if
condition|(
name|AssignmentTestingUtil
operator|.
name|isServerHoldingMeta
argument_list|(
name|util
argument_list|,
name|serverName
argument_list|)
operator|==
name|carryingMeta
condition|)
block|{
name|rsToKill
operator|=
name|serverName
expr_stmt|;
break|break;
block|}
block|}
comment|// Enable test flags and then queue the crash procedure.
name|ProcedureTestingUtility
operator|.
name|waitNoProcedureRunning
argument_list|(
name|procExec
argument_list|)
expr_stmt|;
if|if
condition|(
name|doubleExecution
condition|)
block|{
comment|// For SCP, if you enable this then we will enter an infinite loop, as we will crash between
comment|// queue and open for TRSP, and then going back to queue, as we will use the crash rs as the
comment|// target server since it is recored in hbase:meta.
name|ProcedureTestingUtility
operator|.
name|setKillIfHasParent
argument_list|(
name|procExec
argument_list|,
literal|false
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
comment|// kill the RS
name|AssignmentTestingUtil
operator|.
name|killRs
argument_list|(
name|util
argument_list|,
name|rsToKill
argument_list|)
expr_stmt|;
name|long
name|procId
init|=
name|getSCPProcId
argument_list|(
name|procExec
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
block|}
else|else
block|{
comment|// kill the RS
name|AssignmentTestingUtil
operator|.
name|killRs
argument_list|(
name|util
argument_list|,
name|rsToKill
argument_list|)
expr_stmt|;
name|long
name|procId
init|=
name|getSCPProcId
argument_list|(
name|procExec
argument_list|)
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
block|}
name|assertReplicaDistributed
argument_list|(
name|t
argument_list|)
expr_stmt|;
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
name|assertEquals
argument_list|(
name|checksum
argument_list|,
name|util
operator|.
name|checksumRows
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|throwable
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Test failed!"
argument_list|,
name|throwable
argument_list|)
expr_stmt|;
throw|throw
name|throwable
throw|;
block|}
block|}
specifier|protected
name|void
name|assertReplicaDistributed
parameter_list|(
specifier|final
name|Table
name|t
parameter_list|)
block|{
return|return;
block|}
specifier|protected
name|Table
name|createTable
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Table
name|t
init|=
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
decl_stmt|;
return|return
name|t
return|;
block|}
specifier|private
name|void
name|collectMasterMetrics
parameter_list|()
block|{
name|serverCrashSubmittedCount
operator|=
name|serverCrashProcMetrics
operator|.
name|getSubmittedCounter
argument_list|()
operator|.
name|getCount
argument_list|()
expr_stmt|;
name|serverCrashFailedCount
operator|=
name|serverCrashProcMetrics
operator|.
name|getFailedCounter
argument_list|()
operator|.
name|getCount
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

