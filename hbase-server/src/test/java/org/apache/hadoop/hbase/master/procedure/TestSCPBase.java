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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|RegionReplicaUtil
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
name|Region
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
name|JVMClusterUtil
operator|.
name|RegionServerThread
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
specifier|public
class|class
name|TestSCPBase
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
name|TestSCPBase
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|HBaseTestingUtility
name|util
decl_stmt|;
specifier|protected
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
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.split.writer.creation.bounded"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.hlog.splitlog.writer.threads"
argument_list|,
literal|8
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|HBASE_SPLIT_WAL_COORDINATED_BY_ZK
argument_list|,
literal|true
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
comment|/**    * Run server crash procedure steps twice to test idempotency and that we are persisting all    * needed state.    */
specifier|protected
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
name|HBaseTestingUtility
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
name|HBaseTestingUtility
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
block|}
specifier|protected
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
specifier|private
name|void
name|assertReplicaDistributed
parameter_list|(
name|Table
name|t
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|t
operator|.
name|getDescriptor
argument_list|()
operator|.
name|getRegionReplication
argument_list|()
operator|<=
literal|1
condition|)
block|{
return|return;
block|}
comment|// Assert all data came back.
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regionInfos
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|RegionServerThread
name|rs
range|:
name|this
operator|.
name|util
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
control|)
block|{
name|regionInfos
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|Region
name|r
range|:
name|rs
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getRegions
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|)
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"The region is "
operator|+
name|r
operator|.
name|getRegionInfo
argument_list|()
operator|+
literal|" the location is "
operator|+
name|rs
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|contains
argument_list|(
name|regionInfos
argument_list|,
name|r
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Am exiting"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Crashed replica regions should not be assigned to same region server"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|regionInfos
operator|.
name|add
argument_list|(
name|r
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|boolean
name|contains
parameter_list|(
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regionInfos
parameter_list|,
name|RegionInfo
name|regionInfo
parameter_list|)
block|{
for|for
control|(
name|RegionInfo
name|info
range|:
name|regionInfos
control|)
block|{
if|if
condition|(
name|RegionReplicaUtil
operator|.
name|isReplicasForSameRegion
argument_list|(
name|info
argument_list|,
name|regionInfo
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
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
argument_list|,
name|getRegionReplication
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|t
return|;
block|}
specifier|protected
name|int
name|getRegionReplication
parameter_list|()
block|{
return|return
literal|1
return|;
block|}
block|}
end_class

end_unit
