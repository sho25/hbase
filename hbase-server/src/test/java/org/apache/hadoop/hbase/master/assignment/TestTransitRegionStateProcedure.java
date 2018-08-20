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
name|assignment
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
name|assertFalse
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
name|procedure
operator|.
name|MasterProcedureConstants
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
name|procedure
operator|.
name|MasterProcedureEnv
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
name|procedure
operator|.
name|MasterProcedureTestingUtility
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
name|HRegion
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Bytes
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
name|Before
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
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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
name|rules
operator|.
name|TestName
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
name|TestTransitRegionStateProcedure
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
name|TestTransitRegionStateProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|CF
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|private
name|TableName
name|tableName
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
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
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|balancerSwitch
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|tableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|CF
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|resetProcExecutorTestingKillFlag
parameter_list|()
block|{
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|setKillAndToggleBeforeStoreUpdate
argument_list|(
name|procExec
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"expected executor to be running"
argument_list|,
name|procExec
operator|.
name|isRunning
argument_list|()
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
name|IOException
block|{
name|resetProcExecutorTestingKillFlag
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testRecoveryAndDoubleExcution
parameter_list|(
name|TransitRegionStateProcedure
name|proc
parameter_list|)
throws|throws
name|Exception
block|{
name|HMaster
name|master
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|AssignmentManager
name|am
init|=
name|master
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
name|RegionStateNode
name|regionNode
init|=
name|am
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionStateNode
argument_list|(
name|proc
operator|.
name|getRegion
argument_list|()
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|regionNode
operator|.
name|isInTransition
argument_list|()
argument_list|)
expr_stmt|;
name|regionNode
operator|.
name|setProcedure
argument_list|(
name|proc
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|regionNode
operator|.
name|isInTransition
argument_list|()
argument_list|)
expr_stmt|;
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
name|proc
argument_list|)
decl_stmt|;
name|MasterProcedureTestingUtility
operator|.
name|testRecoveryAndDoubleExecution
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
name|regionNode
operator|=
name|am
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionStateNode
argument_list|(
name|proc
operator|.
name|getRegion
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|regionNode
operator|.
name|isInTransition
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRecoveryAndDoubleExecutionMove
parameter_list|()
throws|throws
name|Exception
block|{
name|MasterProcedureEnv
name|env
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getEnvironment
argument_list|()
decl_stmt|;
name|HRegion
name|region
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|long
name|openSeqNum
init|=
name|region
operator|.
name|getOpenSeqNum
argument_list|()
decl_stmt|;
name|TransitRegionStateProcedure
name|proc
init|=
name|TransitRegionStateProcedure
operator|.
name|move
argument_list|(
name|env
argument_list|,
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|testRecoveryAndDoubleExcution
argument_list|(
name|proc
argument_list|)
expr_stmt|;
name|HRegion
name|region2
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|long
name|openSeqNum2
init|=
name|region2
operator|.
name|getOpenSeqNum
argument_list|()
decl_stmt|;
comment|// confirm that the region is successfully opened
name|assertTrue
argument_list|(
name|openSeqNum2
operator|>
name|openSeqNum
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRecoveryAndDoubleExecutionReopen
parameter_list|()
throws|throws
name|Exception
block|{
name|MasterProcedureEnv
name|env
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getEnvironment
argument_list|()
decl_stmt|;
name|HRegionServer
name|rs
init|=
name|UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|rs
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|long
name|openSeqNum
init|=
name|region
operator|.
name|getOpenSeqNum
argument_list|()
decl_stmt|;
name|TransitRegionStateProcedure
name|proc
init|=
name|TransitRegionStateProcedure
operator|.
name|reopen
argument_list|(
name|env
argument_list|,
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
decl_stmt|;
name|testRecoveryAndDoubleExcution
argument_list|(
name|proc
argument_list|)
expr_stmt|;
comment|// should still be on the same RS
name|HRegion
name|region2
init|=
name|rs
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|long
name|openSeqNum2
init|=
name|region2
operator|.
name|getOpenSeqNum
argument_list|()
decl_stmt|;
comment|// confirm that the region is successfully opened
name|assertTrue
argument_list|(
name|openSeqNum2
operator|>
name|openSeqNum
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRecoveryAndDoubleExecutionUnassignAndAssign
parameter_list|()
throws|throws
name|Exception
block|{
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
name|MasterProcedureEnv
name|env
init|=
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getEnvironment
argument_list|()
decl_stmt|;
name|HRegion
name|region
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|RegionInfo
name|regionInfo
init|=
name|region
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
name|long
name|openSeqNum
init|=
name|region
operator|.
name|getOpenSeqNum
argument_list|()
decl_stmt|;
name|TransitRegionStateProcedure
name|unassign
init|=
name|TransitRegionStateProcedure
operator|.
name|unassign
argument_list|(
name|env
argument_list|,
name|regionInfo
argument_list|)
decl_stmt|;
name|testRecoveryAndDoubleExcution
argument_list|(
name|unassign
argument_list|)
expr_stmt|;
name|AssignmentManager
name|am
init|=
name|master
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|am
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionState
argument_list|(
name|regionInfo
argument_list|)
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
name|TransitRegionStateProcedure
name|assign
init|=
name|TransitRegionStateProcedure
operator|.
name|assign
argument_list|(
name|env
argument_list|,
name|regionInfo
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|testRecoveryAndDoubleExcution
argument_list|(
name|assign
argument_list|)
expr_stmt|;
name|HRegion
name|region2
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|long
name|openSeqNum2
init|=
name|region2
operator|.
name|getOpenSeqNum
argument_list|()
decl_stmt|;
comment|// confirm that the region is successfully opened
name|assertTrue
argument_list|(
name|openSeqNum2
operator|>
name|openSeqNum
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

