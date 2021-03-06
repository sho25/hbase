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
name|MasterProcedureProtos
operator|.
name|RegionStateTransitionState
operator|.
name|REGION_STATE_TRANSITION_OPEN
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
name|Arrays
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
name|CountDownLatch
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
name|Admin
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
name|assignment
operator|.
name|TransitRegionStateProcedure
operator|.
name|TransitionType
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
name|ProcedureSuspendedException
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
name|ProcedureYieldException
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
name|Bytes
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
name|MasterProcedureProtos
operator|.
name|RegionStateTransitionState
import|;
end_import

begin_comment
comment|/**  * Tests bypass on a region assign/unassign  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestRegionBypass
block|{
specifier|private
specifier|final
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestRegionBypass
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TestRegionBypass
operator|.
name|class
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
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
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
name|startCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|stopCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|tableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Create a table. Has one region at least.
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|this
operator|.
name|tableName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBypass
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|MasterProcedureEnv
name|env
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
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
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regions
init|=
name|admin
operator|.
name|getRegions
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|RegionInfo
name|ri
range|:
name|regions
control|)
block|{
name|admin
operator|.
name|unassign
argument_list|(
name|ri
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Long
argument_list|>
name|pids
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|regions
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|RegionInfo
name|ri
range|:
name|regions
control|)
block|{
name|Procedure
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|p
init|=
operator|new
name|StallingAssignProcedure
argument_list|(
name|env
argument_list|,
name|ri
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
name|TransitionType
operator|.
name|ASSIGN
argument_list|)
decl_stmt|;
name|pids
operator|.
name|add
argument_list|(
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|submitProcedure
argument_list|(
name|p
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Long
name|pid
range|:
name|pids
control|)
block|{
while|while
condition|(
operator|!
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|isStarted
argument_list|(
name|pid
argument_list|)
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|Procedure
argument_list|<
name|MasterProcedureEnv
argument_list|>
argument_list|>
name|ps
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getProcedures
argument_list|()
decl_stmt|;
for|for
control|(
name|Procedure
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|p
range|:
name|ps
control|)
block|{
if|if
condition|(
name|p
operator|instanceof
name|StallingAssignProcedure
condition|)
block|{
name|List
argument_list|<
name|Boolean
argument_list|>
name|bs
init|=
name|TEST_UTIL
operator|.
name|getHbck
argument_list|()
operator|.
name|bypassProcedure
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|p
operator|.
name|getProcId
argument_list|()
argument_list|)
argument_list|,
literal|1000
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
for|for
control|(
name|Boolean
name|b
range|:
name|bs
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"BYPASSED {} {}"
argument_list|,
name|p
operator|.
name|getProcId
argument_list|()
argument_list|,
name|b
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Try and assign WITHOUT override flag. Should fail!.
for|for
control|(
name|RegionInfo
name|ri
range|:
name|regions
control|)
block|{
try|try
block|{
name|admin
operator|.
name|assign
argument_list|(
name|ri
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|dnrioe
parameter_list|)
block|{
comment|// Expected
name|LOG
operator|.
name|info
argument_list|(
literal|"Expected {}"
argument_list|,
name|dnrioe
argument_list|)
expr_stmt|;
block|}
block|}
while|while
condition|(
operator|!
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getActiveProcIds
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
comment|// Now assign with the override flag.
for|for
control|(
name|RegionInfo
name|ri
range|:
name|regions
control|)
block|{
name|TEST_UTIL
operator|.
name|getHbck
argument_list|()
operator|.
name|assigns
argument_list|(
name|Arrays
operator|.
expr|<
name|String
operator|>
name|asList
argument_list|(
name|ri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
while|while
condition|(
operator|!
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getActiveProcIds
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|RegionInfo
name|ri
range|:
name|regions
control|)
block|{
name|assertTrue
argument_list|(
name|ri
operator|.
name|toString
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|isRegionOnline
argument_list|(
name|ri
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * An AssignProcedure that Stalls just before the finish.    */
specifier|public
specifier|static
class|class
name|StallingAssignProcedure
extends|extends
name|TransitRegionStateProcedure
block|{
specifier|public
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|2
argument_list|)
decl_stmt|;
specifier|public
name|StallingAssignProcedure
parameter_list|()
block|{}
specifier|public
name|StallingAssignProcedure
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|RegionInfo
name|hri
parameter_list|,
name|ServerName
name|assignCandidate
parameter_list|,
name|boolean
name|forceNewPlan
parameter_list|,
name|TransitionType
name|type
parameter_list|)
block|{
name|super
argument_list|(
name|env
argument_list|,
name|hri
argument_list|,
name|assignCandidate
argument_list|,
name|forceNewPlan
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|init
argument_list|(
name|env
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|init
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|RegionStateNode
name|regionNode
init|=
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getOrCreateRegionStateNode
argument_list|(
name|getRegion
argument_list|()
argument_list|)
decl_stmt|;
name|regionNode
operator|.
name|setProcedure
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Flow
name|executeFromState
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|RegionStateTransitionState
name|state
parameter_list|)
throws|throws
name|ProcedureSuspendedException
throws|,
name|ProcedureYieldException
throws|,
name|InterruptedException
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|REGION_STATE_TRANSITION_GET_ASSIGN_CANDIDATE
case|:
name|LOG
operator|.
name|info
argument_list|(
literal|"LATCH1 {}"
argument_list|,
name|this
operator|.
name|latch
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|setNextState
argument_list|(
name|REGION_STATE_TRANSITION_OPEN
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|REGION_STATE_TRANSITION_OPEN
case|:
if|if
condition|(
name|latch
operator|.
name|getCount
argument_list|()
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"LATCH3 {}"
argument_list|,
name|this
operator|.
name|latch
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|NO_MORE_STATE
return|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"LATCH2 {}"
argument_list|,
name|this
operator|.
name|latch
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
block|}
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"unhandled state="
operator|+
name|state
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

