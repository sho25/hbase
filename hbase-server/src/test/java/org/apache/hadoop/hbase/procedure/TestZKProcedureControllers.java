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
name|mockito
operator|.
name|Mockito
operator|.
name|never
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|spy
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|times
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verify
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|Pair
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
name|ZooKeeperWatcher
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

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|invocation
operator|.
name|InvocationOnMock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|Answer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|verification
operator|.
name|VerificationMode
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
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * Test zookeeper-based, procedure controllers  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestZKProcedureControllers
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestZKProcedureControllers
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
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
specifier|final
name|String
name|COHORT_NODE_NAME
init|=
literal|"expected"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CONTROLLER_NODE_NAME
init|=
literal|"controller"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|VerificationMode
name|once
init|=
name|Mockito
operator|.
name|times
argument_list|(
literal|1
argument_list|)
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupTest
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniZKCluster
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
name|UTIL
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Smaller test to just test the actuation on the cohort member    * @throws Exception on failure    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|15000
argument_list|)
specifier|public
name|void
name|testSimpleZKCohortMemberController
parameter_list|()
throws|throws
name|Exception
block|{
name|ZooKeeperWatcher
name|watcher
init|=
name|UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
decl_stmt|;
specifier|final
name|String
name|operationName
init|=
literal|"instanceTest"
decl_stmt|;
specifier|final
name|Subprocedure
name|sub
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|Subprocedure
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|sub
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|operationName
argument_list|)
expr_stmt|;
specifier|final
name|byte
index|[]
name|data
init|=
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|}
decl_stmt|;
specifier|final
name|CountDownLatch
name|prepared
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|committed
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|ForeignExceptionDispatcher
name|monitor
init|=
name|spy
argument_list|(
operator|new
name|ForeignExceptionDispatcher
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|ZKProcedureMemberRpcs
name|controller
init|=
operator|new
name|ZKProcedureMemberRpcs
argument_list|(
name|watcher
argument_list|,
literal|"testSimple"
argument_list|,
name|COHORT_NODE_NAME
argument_list|)
decl_stmt|;
comment|// mock out cohort member callbacks
specifier|final
name|ProcedureMember
name|member
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ProcedureMember
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|sub
argument_list|)
operator|.
name|when
argument_list|(
name|member
argument_list|)
operator|.
name|createSubprocedure
argument_list|(
name|operationName
argument_list|,
name|data
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|controller
operator|.
name|sendMemberAcquired
argument_list|(
name|sub
argument_list|)
expr_stmt|;
name|prepared
operator|.
name|countDown
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|member
argument_list|)
operator|.
name|submitSubprocedure
argument_list|(
name|sub
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|controller
operator|.
name|sendMemberCompleted
argument_list|(
name|sub
argument_list|)
expr_stmt|;
name|committed
operator|.
name|countDown
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|member
argument_list|)
operator|.
name|receivedReachedGlobalBarrier
argument_list|(
name|operationName
argument_list|)
expr_stmt|;
comment|// start running the listener
name|controller
operator|.
name|start
argument_list|(
name|member
argument_list|)
expr_stmt|;
comment|// set a prepare node from a 'coordinator'
name|String
name|prepare
init|=
name|ZKProcedureUtil
operator|.
name|getAcquireBarrierNode
argument_list|(
name|controller
operator|.
name|getZkController
argument_list|()
argument_list|,
name|operationName
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|createSetData
argument_list|(
name|watcher
argument_list|,
name|prepare
argument_list|,
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
comment|// wait for the operation to be prepared
name|prepared
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// create the commit node so we update the operation to enter the commit phase
name|String
name|commit
init|=
name|ZKProcedureUtil
operator|.
name|getReachedBarrierNode
argument_list|(
name|controller
operator|.
name|getZkController
argument_list|()
argument_list|,
name|operationName
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found prepared, posting commit node:"
operator|+
name|commit
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|createAndFailSilent
argument_list|(
name|watcher
argument_list|,
name|commit
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Commit node:"
operator|+
name|commit
operator|+
literal|", exists:"
operator|+
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|watcher
argument_list|,
name|commit
argument_list|)
argument_list|)
expr_stmt|;
name|committed
operator|.
name|await
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|monitor
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|receive
argument_list|(
name|Mockito
operator|.
name|any
argument_list|(
name|ForeignException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
comment|// XXX: broken due to composition.
comment|//    verify(member, never()).getManager().controllerConnectionFailure(Mockito.anyString(),
comment|//      Mockito.any(IOException.class));
comment|// cleanup after the test
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|watcher
argument_list|,
name|controller
operator|.
name|getZkController
argument_list|()
operator|.
name|getBaseZnode
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Didn't delete prepare node"
argument_list|,
operator|-
literal|1
argument_list|,
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|watcher
argument_list|,
name|prepare
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Didn't delete commit node"
argument_list|,
operator|-
literal|1
argument_list|,
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|watcher
argument_list|,
name|commit
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|15000
argument_list|)
specifier|public
name|void
name|testZKCoordinatorControllerWithNoCohort
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|operationName
init|=
literal|"no cohort controller test"
decl_stmt|;
specifier|final
name|byte
index|[]
name|data
init|=
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|}
decl_stmt|;
name|runMockCommitWithOrchestratedControllers
argument_list|(
name|startCoordinatorFirst
argument_list|,
name|operationName
argument_list|,
name|data
argument_list|)
expr_stmt|;
name|runMockCommitWithOrchestratedControllers
argument_list|(
name|startCohortFirst
argument_list|,
name|operationName
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|15000
argument_list|)
specifier|public
name|void
name|testZKCoordinatorControllerWithSingleMemberCohort
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|operationName
init|=
literal|"single member controller test"
decl_stmt|;
specifier|final
name|byte
index|[]
name|data
init|=
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|}
decl_stmt|;
name|runMockCommitWithOrchestratedControllers
argument_list|(
name|startCoordinatorFirst
argument_list|,
name|operationName
argument_list|,
name|data
argument_list|,
literal|"cohort"
argument_list|)
expr_stmt|;
name|runMockCommitWithOrchestratedControllers
argument_list|(
name|startCohortFirst
argument_list|,
name|operationName
argument_list|,
name|data
argument_list|,
literal|"cohort"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|15000
argument_list|)
specifier|public
name|void
name|testZKCoordinatorControllerMultipleCohort
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|operationName
init|=
literal|"multi member controller test"
decl_stmt|;
specifier|final
name|byte
index|[]
name|data
init|=
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|}
decl_stmt|;
name|runMockCommitWithOrchestratedControllers
argument_list|(
name|startCoordinatorFirst
argument_list|,
name|operationName
argument_list|,
name|data
argument_list|,
literal|"cohort"
argument_list|,
literal|"cohort2"
argument_list|,
literal|"cohort3"
argument_list|)
expr_stmt|;
name|runMockCommitWithOrchestratedControllers
argument_list|(
name|startCohortFirst
argument_list|,
name|operationName
argument_list|,
name|data
argument_list|,
literal|"cohort"
argument_list|,
literal|"cohort2"
argument_list|,
literal|"cohort3"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runMockCommitWithOrchestratedControllers
parameter_list|(
name|StartControllers
name|controllers
parameter_list|,
name|String
name|operationName
parameter_list|,
name|byte
index|[]
name|data
parameter_list|,
name|String
modifier|...
name|cohort
parameter_list|)
throws|throws
name|Exception
block|{
name|ZooKeeperWatcher
name|watcher
init|=
name|UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|expected
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|cohort
argument_list|)
decl_stmt|;
specifier|final
name|Subprocedure
name|sub
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|Subprocedure
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|sub
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|operationName
argument_list|)
expr_stmt|;
name|CountDownLatch
name|prepared
init|=
operator|new
name|CountDownLatch
argument_list|(
name|expected
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|CountDownLatch
name|committed
init|=
operator|new
name|CountDownLatch
argument_list|(
name|expected
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
comment|// mock out coordinator so we can keep track of zk progress
name|ProcedureCoordinator
name|coordinator
init|=
name|setupMockCoordinator
argument_list|(
name|operationName
argument_list|,
name|prepared
argument_list|,
name|committed
argument_list|)
decl_stmt|;
name|ProcedureMember
name|member
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ProcedureMember
operator|.
name|class
argument_list|)
decl_stmt|;
name|Pair
argument_list|<
name|ZKProcedureCoordinatorRpcs
argument_list|,
name|List
argument_list|<
name|ZKProcedureMemberRpcs
argument_list|>
argument_list|>
name|pair
init|=
name|controllers
operator|.
name|start
argument_list|(
name|watcher
argument_list|,
name|operationName
argument_list|,
name|coordinator
argument_list|,
name|CONTROLLER_NODE_NAME
argument_list|,
name|member
argument_list|,
name|expected
argument_list|)
decl_stmt|;
name|ZKProcedureCoordinatorRpcs
name|controller
init|=
name|pair
operator|.
name|getFirst
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ZKProcedureMemberRpcs
argument_list|>
name|cohortControllers
init|=
name|pair
operator|.
name|getSecond
argument_list|()
decl_stmt|;
comment|// start the operation
name|Procedure
name|p
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|Procedure
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|p
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|operationName
argument_list|)
expr_stmt|;
name|controller
operator|.
name|sendGlobalBarrierAcquire
argument_list|(
name|p
argument_list|,
name|data
argument_list|,
name|expected
argument_list|)
expr_stmt|;
comment|// post the prepare node for each expected node
for|for
control|(
name|ZKProcedureMemberRpcs
name|cc
range|:
name|cohortControllers
control|)
block|{
name|cc
operator|.
name|sendMemberAcquired
argument_list|(
name|sub
argument_list|)
expr_stmt|;
block|}
comment|// wait for all the notifications to reach the coordinator
name|prepared
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// make sure we got the all the nodes and no more
name|Mockito
operator|.
name|verify
argument_list|(
name|coordinator
argument_list|,
name|times
argument_list|(
name|expected
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
operator|.
name|memberAcquiredBarrier
argument_list|(
name|Mockito
operator|.
name|eq
argument_list|(
name|operationName
argument_list|)
argument_list|,
name|Mockito
operator|.
name|anyString
argument_list|()
argument_list|)
expr_stmt|;
comment|// kick off the commit phase
name|controller
operator|.
name|sendGlobalBarrierReached
argument_list|(
name|p
argument_list|,
name|expected
argument_list|)
expr_stmt|;
comment|// post the committed node for each expected node
for|for
control|(
name|ZKProcedureMemberRpcs
name|cc
range|:
name|cohortControllers
control|)
block|{
name|cc
operator|.
name|sendMemberCompleted
argument_list|(
name|sub
argument_list|)
expr_stmt|;
block|}
comment|// wait for all commit notifications to reach the coordinator
name|committed
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// make sure we got the all the nodes and no more
name|Mockito
operator|.
name|verify
argument_list|(
name|coordinator
argument_list|,
name|times
argument_list|(
name|expected
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
operator|.
name|memberFinishedBarrier
argument_list|(
name|Mockito
operator|.
name|eq
argument_list|(
name|operationName
argument_list|)
argument_list|,
name|Mockito
operator|.
name|anyString
argument_list|()
argument_list|)
expr_stmt|;
name|controller
operator|.
name|resetMembers
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// verify all behavior
name|verifyZooKeeperClean
argument_list|(
name|operationName
argument_list|,
name|watcher
argument_list|,
name|controller
operator|.
name|getZkProcedureUtil
argument_list|()
argument_list|)
expr_stmt|;
name|verifyCohort
argument_list|(
name|member
argument_list|,
name|cohortControllers
operator|.
name|size
argument_list|()
argument_list|,
name|operationName
argument_list|,
name|data
argument_list|)
expr_stmt|;
name|verifyCoordinator
argument_list|(
name|operationName
argument_list|,
name|coordinator
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
comment|// TODO Broken by composition.
comment|//  @Test
comment|//  public void testCoordinatorControllerHandlesEarlyPrepareNodes() throws Exception {
comment|//    runEarlyPrepareNodes(startCoordinatorFirst, "testEarlyPreparenodes", new byte[] { 1, 2, 3 },
comment|//      "cohort1", "cohort2");
comment|//    runEarlyPrepareNodes(startCohortFirst, "testEarlyPreparenodes", new byte[] { 1, 2, 3 },
comment|//      "cohort1", "cohort2");
comment|//  }
specifier|public
name|void
name|runEarlyPrepareNodes
parameter_list|(
name|StartControllers
name|controllers
parameter_list|,
name|String
name|operationName
parameter_list|,
name|byte
index|[]
name|data
parameter_list|,
name|String
modifier|...
name|cohort
parameter_list|)
throws|throws
name|Exception
block|{
name|ZooKeeperWatcher
name|watcher
init|=
name|UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|expected
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|cohort
argument_list|)
decl_stmt|;
specifier|final
name|Subprocedure
name|sub
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|Subprocedure
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|sub
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|operationName
argument_list|)
expr_stmt|;
specifier|final
name|CountDownLatch
name|prepared
init|=
operator|new
name|CountDownLatch
argument_list|(
name|expected
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|committed
init|=
operator|new
name|CountDownLatch
argument_list|(
name|expected
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
comment|// mock out coordinator so we can keep track of zk progress
name|ProcedureCoordinator
name|coordinator
init|=
name|setupMockCoordinator
argument_list|(
name|operationName
argument_list|,
name|prepared
argument_list|,
name|committed
argument_list|)
decl_stmt|;
name|ProcedureMember
name|member
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ProcedureMember
operator|.
name|class
argument_list|)
decl_stmt|;
name|Procedure
name|p
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|Procedure
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|p
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|operationName
argument_list|)
expr_stmt|;
name|Pair
argument_list|<
name|ZKProcedureCoordinatorRpcs
argument_list|,
name|List
argument_list|<
name|ZKProcedureMemberRpcs
argument_list|>
argument_list|>
name|pair
init|=
name|controllers
operator|.
name|start
argument_list|(
name|watcher
argument_list|,
name|operationName
argument_list|,
name|coordinator
argument_list|,
name|CONTROLLER_NODE_NAME
argument_list|,
name|member
argument_list|,
name|expected
argument_list|)
decl_stmt|;
name|ZKProcedureCoordinatorRpcs
name|controller
init|=
name|pair
operator|.
name|getFirst
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ZKProcedureMemberRpcs
argument_list|>
name|cohortControllers
init|=
name|pair
operator|.
name|getSecond
argument_list|()
decl_stmt|;
comment|// post 1/2 the prepare nodes early
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|cohortControllers
operator|.
name|size
argument_list|()
operator|/
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|cohortControllers
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|sendMemberAcquired
argument_list|(
name|sub
argument_list|)
expr_stmt|;
block|}
comment|// start the operation
name|controller
operator|.
name|sendGlobalBarrierAcquire
argument_list|(
name|p
argument_list|,
name|data
argument_list|,
name|expected
argument_list|)
expr_stmt|;
comment|// post the prepare node for each expected node
for|for
control|(
name|ZKProcedureMemberRpcs
name|cc
range|:
name|cohortControllers
control|)
block|{
name|cc
operator|.
name|sendMemberAcquired
argument_list|(
name|sub
argument_list|)
expr_stmt|;
block|}
comment|// wait for all the notifications to reach the coordinator
name|prepared
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// make sure we got the all the nodes and no more
name|Mockito
operator|.
name|verify
argument_list|(
name|coordinator
argument_list|,
name|times
argument_list|(
name|expected
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
operator|.
name|memberAcquiredBarrier
argument_list|(
name|Mockito
operator|.
name|eq
argument_list|(
name|operationName
argument_list|)
argument_list|,
name|Mockito
operator|.
name|anyString
argument_list|()
argument_list|)
expr_stmt|;
comment|// kick off the commit phase
name|controller
operator|.
name|sendGlobalBarrierReached
argument_list|(
name|p
argument_list|,
name|expected
argument_list|)
expr_stmt|;
comment|// post the committed node for each expected node
for|for
control|(
name|ZKProcedureMemberRpcs
name|cc
range|:
name|cohortControllers
control|)
block|{
name|cc
operator|.
name|sendMemberCompleted
argument_list|(
name|sub
argument_list|)
expr_stmt|;
block|}
comment|// wait for all commit notifications to reach the coordiantor
name|committed
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// make sure we got the all the nodes and no more
name|Mockito
operator|.
name|verify
argument_list|(
name|coordinator
argument_list|,
name|times
argument_list|(
name|expected
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
operator|.
name|memberFinishedBarrier
argument_list|(
name|Mockito
operator|.
name|eq
argument_list|(
name|operationName
argument_list|)
argument_list|,
name|Mockito
operator|.
name|anyString
argument_list|()
argument_list|)
expr_stmt|;
name|controller
operator|.
name|resetMembers
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// verify all behavior
name|verifyZooKeeperClean
argument_list|(
name|operationName
argument_list|,
name|watcher
argument_list|,
name|controller
operator|.
name|getZkProcedureUtil
argument_list|()
argument_list|)
expr_stmt|;
name|verifyCohort
argument_list|(
name|member
argument_list|,
name|cohortControllers
operator|.
name|size
argument_list|()
argument_list|,
name|operationName
argument_list|,
name|data
argument_list|)
expr_stmt|;
name|verifyCoordinator
argument_list|(
name|operationName
argument_list|,
name|coordinator
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return a mock {@link ProcedureCoordinator} that just counts down the    *         prepared and committed latch for called to the respective method    */
specifier|private
name|ProcedureCoordinator
name|setupMockCoordinator
parameter_list|(
name|String
name|operationName
parameter_list|,
specifier|final
name|CountDownLatch
name|prepared
parameter_list|,
specifier|final
name|CountDownLatch
name|committed
parameter_list|)
block|{
name|ProcedureCoordinator
name|coordinator
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ProcedureCoordinator
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|mock
argument_list|(
name|ProcedureCoordinator
operator|.
name|class
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|prepared
operator|.
name|countDown
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|coordinator
argument_list|)
operator|.
name|memberAcquiredBarrier
argument_list|(
name|Mockito
operator|.
name|eq
argument_list|(
name|operationName
argument_list|)
argument_list|,
name|Mockito
operator|.
name|anyString
argument_list|()
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|committed
operator|.
name|countDown
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|coordinator
argument_list|)
operator|.
name|memberFinishedBarrier
argument_list|(
name|Mockito
operator|.
name|eq
argument_list|(
name|operationName
argument_list|)
argument_list|,
name|Mockito
operator|.
name|anyString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|coordinator
return|;
block|}
comment|/**    * Verify that the prepare, commit and abort nodes for the operation are removed from zookeeper    */
specifier|private
name|void
name|verifyZooKeeperClean
parameter_list|(
name|String
name|operationName
parameter_list|,
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|ZKProcedureUtil
name|controller
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|prepare
init|=
name|ZKProcedureUtil
operator|.
name|getAcquireBarrierNode
argument_list|(
name|controller
argument_list|,
name|operationName
argument_list|)
decl_stmt|;
name|String
name|commit
init|=
name|ZKProcedureUtil
operator|.
name|getReachedBarrierNode
argument_list|(
name|controller
argument_list|,
name|operationName
argument_list|)
decl_stmt|;
name|String
name|abort
init|=
name|ZKProcedureUtil
operator|.
name|getAbortNode
argument_list|(
name|controller
argument_list|,
name|operationName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Didn't delete prepare node"
argument_list|,
operator|-
literal|1
argument_list|,
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|watcher
argument_list|,
name|prepare
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Didn't delete commit node"
argument_list|,
operator|-
literal|1
argument_list|,
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|watcher
argument_list|,
name|commit
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Didn't delete abort node"
argument_list|,
operator|-
literal|1
argument_list|,
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|watcher
argument_list|,
name|abort
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify the cohort controller got called once per expected node to start the operation    */
specifier|private
name|void
name|verifyCohort
parameter_list|(
name|ProcedureMember
name|member
parameter_list|,
name|int
name|cohortSize
parameter_list|,
name|String
name|operationName
parameter_list|,
name|byte
index|[]
name|data
parameter_list|)
block|{
comment|//    verify(member, Mockito.times(cohortSize)).submitSubprocedure(Mockito.eq(operationName),
comment|//      (byte[]) Mockito.argThat(new ArrayEquals(data)));
name|verify
argument_list|(
name|member
argument_list|,
name|Mockito
operator|.
name|times
argument_list|(
name|cohortSize
argument_list|)
argument_list|)
operator|.
name|submitSubprocedure
argument_list|(
name|Mockito
operator|.
name|any
argument_list|(
name|Subprocedure
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify that the coordinator only got called once for each expected node    */
specifier|private
name|void
name|verifyCoordinator
parameter_list|(
name|String
name|operationName
parameter_list|,
name|ProcedureCoordinator
name|coordinator
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|expected
parameter_list|)
block|{
comment|// verify that we got all the expected nodes
for|for
control|(
name|String
name|node
range|:
name|expected
control|)
block|{
name|verify
argument_list|(
name|coordinator
argument_list|,
name|once
argument_list|)
operator|.
name|memberAcquiredBarrier
argument_list|(
name|operationName
argument_list|,
name|node
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|coordinator
argument_list|,
name|once
argument_list|)
operator|.
name|memberFinishedBarrier
argument_list|(
name|operationName
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Specify how the controllers that should be started (not spy/mockable) for the test.    */
specifier|private
specifier|abstract
class|class
name|StartControllers
block|{
specifier|public
specifier|abstract
name|Pair
argument_list|<
name|ZKProcedureCoordinatorRpcs
argument_list|,
name|List
argument_list|<
name|ZKProcedureMemberRpcs
argument_list|>
argument_list|>
name|start
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|String
name|operationName
parameter_list|,
name|ProcedureCoordinator
name|coordinator
parameter_list|,
name|String
name|controllerName
parameter_list|,
name|ProcedureMember
name|member
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|cohortNames
parameter_list|)
throws|throws
name|Exception
function_decl|;
block|}
specifier|private
specifier|final
name|StartControllers
name|startCoordinatorFirst
init|=
operator|new
name|StartControllers
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Pair
argument_list|<
name|ZKProcedureCoordinatorRpcs
argument_list|,
name|List
argument_list|<
name|ZKProcedureMemberRpcs
argument_list|>
argument_list|>
name|start
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|String
name|operationName
parameter_list|,
name|ProcedureCoordinator
name|coordinator
parameter_list|,
name|String
name|controllerName
parameter_list|,
name|ProcedureMember
name|member
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|expected
parameter_list|)
throws|throws
name|Exception
block|{
comment|// start the controller
name|ZKProcedureCoordinatorRpcs
name|controller
init|=
operator|new
name|ZKProcedureCoordinatorRpcs
argument_list|(
name|watcher
argument_list|,
name|operationName
argument_list|,
name|CONTROLLER_NODE_NAME
argument_list|)
decl_stmt|;
name|controller
operator|.
name|start
argument_list|(
name|coordinator
argument_list|)
expr_stmt|;
comment|// make a cohort controller for each expected node
name|List
argument_list|<
name|ZKProcedureMemberRpcs
argument_list|>
name|cohortControllers
init|=
operator|new
name|ArrayList
argument_list|<
name|ZKProcedureMemberRpcs
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|nodeName
range|:
name|expected
control|)
block|{
name|ZKProcedureMemberRpcs
name|cc
init|=
operator|new
name|ZKProcedureMemberRpcs
argument_list|(
name|watcher
argument_list|,
name|operationName
argument_list|,
name|nodeName
argument_list|)
decl_stmt|;
name|cc
operator|.
name|start
argument_list|(
name|member
argument_list|)
expr_stmt|;
name|cohortControllers
operator|.
name|add
argument_list|(
name|cc
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|Pair
argument_list|<
name|ZKProcedureCoordinatorRpcs
argument_list|,
name|List
argument_list|<
name|ZKProcedureMemberRpcs
argument_list|>
argument_list|>
argument_list|(
name|controller
argument_list|,
name|cohortControllers
argument_list|)
return|;
block|}
block|}
decl_stmt|;
comment|/**    * Check for the possible race condition where a cohort member starts after the controller and    * therefore could miss a new operation    */
specifier|private
specifier|final
name|StartControllers
name|startCohortFirst
init|=
operator|new
name|StartControllers
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Pair
argument_list|<
name|ZKProcedureCoordinatorRpcs
argument_list|,
name|List
argument_list|<
name|ZKProcedureMemberRpcs
argument_list|>
argument_list|>
name|start
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|String
name|operationName
parameter_list|,
name|ProcedureCoordinator
name|coordinator
parameter_list|,
name|String
name|controllerName
parameter_list|,
name|ProcedureMember
name|member
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|expected
parameter_list|)
throws|throws
name|Exception
block|{
comment|// make a cohort controller for each expected node
name|List
argument_list|<
name|ZKProcedureMemberRpcs
argument_list|>
name|cohortControllers
init|=
operator|new
name|ArrayList
argument_list|<
name|ZKProcedureMemberRpcs
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|nodeName
range|:
name|expected
control|)
block|{
name|ZKProcedureMemberRpcs
name|cc
init|=
operator|new
name|ZKProcedureMemberRpcs
argument_list|(
name|watcher
argument_list|,
name|operationName
argument_list|,
name|nodeName
argument_list|)
decl_stmt|;
name|cc
operator|.
name|start
argument_list|(
name|member
argument_list|)
expr_stmt|;
name|cohortControllers
operator|.
name|add
argument_list|(
name|cc
argument_list|)
expr_stmt|;
block|}
comment|// start the controller
name|ZKProcedureCoordinatorRpcs
name|controller
init|=
operator|new
name|ZKProcedureCoordinatorRpcs
argument_list|(
name|watcher
argument_list|,
name|operationName
argument_list|,
name|CONTROLLER_NODE_NAME
argument_list|)
decl_stmt|;
name|controller
operator|.
name|start
argument_list|(
name|coordinator
argument_list|)
expr_stmt|;
return|return
operator|new
name|Pair
argument_list|<
name|ZKProcedureCoordinatorRpcs
argument_list|,
name|List
argument_list|<
name|ZKProcedureMemberRpcs
argument_list|>
argument_list|>
argument_list|(
name|controller
argument_list|,
name|cohortControllers
argument_list|)
return|;
block|}
block|}
decl_stmt|;
block|}
end_class

end_unit

