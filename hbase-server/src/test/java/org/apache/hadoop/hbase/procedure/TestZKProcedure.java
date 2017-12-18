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
name|Matchers
operator|.
name|any
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|anyListOf
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|eq
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
name|atMost
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
name|when
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadPoolExecutor
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
name|hadoop
operator|.
name|hbase
operator|.
name|Abortable
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
name|errorhandling
operator|.
name|TimeoutException
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
operator|.
name|SubprocedureImpl
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
name|ZKWatcher
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
name|internal
operator|.
name|matchers
operator|.
name|ArrayEquals
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
comment|/**  * Cluster-wide testing of a distributed three-phase commit using a 'real' zookeeper cluster  */
end_comment

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
name|TestZKProcedure
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
name|TestZKProcedure
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
specifier|final
name|String
name|COORDINATOR_NODE_NAME
init|=
literal|"coordinator"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|KEEP_ALIVE
init|=
literal|100
decl_stmt|;
comment|// seconds
specifier|private
specifier|static
specifier|final
name|int
name|POOL_SIZE
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|TIMEOUT
init|=
literal|10000
decl_stmt|;
comment|// when debugging make this larger for debugging
specifier|private
specifier|static
specifier|final
name|long
name|WAKE_FREQUENCY
init|=
literal|500
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|opName
init|=
literal|"op"
decl_stmt|;
specifier|private
specifier|static
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
block|}
decl_stmt|;
comment|// TODO what is this used for?
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
specifier|private
specifier|static
name|ZKWatcher
name|newZooKeeperWatcher
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|ZKWatcher
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|"testing utility"
argument_list|,
operator|new
name|Abortable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected abort in distributed three phase commit test:"
operator|+
name|why
argument_list|,
name|e
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEmptyMemberSet
parameter_list|()
throws|throws
name|Exception
block|{
name|runCommit
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSingleMember
parameter_list|()
throws|throws
name|Exception
block|{
name|runCommit
argument_list|(
literal|"one"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultipleMembers
parameter_list|()
throws|throws
name|Exception
block|{
name|runCommit
argument_list|(
literal|"one"
argument_list|,
literal|"two"
argument_list|,
literal|"three"
argument_list|,
literal|"four"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runCommit
parameter_list|(
name|String
modifier|...
name|members
parameter_list|)
throws|throws
name|Exception
block|{
comment|// make sure we just have an empty list
if|if
condition|(
name|members
operator|==
literal|null
condition|)
block|{
name|members
operator|=
operator|new
name|String
index|[
literal|0
index|]
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|expected
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|members
argument_list|)
decl_stmt|;
comment|// setup the constants
name|ZKWatcher
name|coordZkw
init|=
name|newZooKeeperWatcher
argument_list|()
decl_stmt|;
name|String
name|opDescription
init|=
literal|"coordination test - "
operator|+
name|members
operator|.
name|length
operator|+
literal|" cohort members"
decl_stmt|;
comment|// start running the controller
name|ZKProcedureCoordinator
name|coordinatorComms
init|=
operator|new
name|ZKProcedureCoordinator
argument_list|(
name|coordZkw
argument_list|,
name|opDescription
argument_list|,
name|COORDINATOR_NODE_NAME
argument_list|)
decl_stmt|;
name|ThreadPoolExecutor
name|pool
init|=
name|ProcedureCoordinator
operator|.
name|defaultPool
argument_list|(
name|COORDINATOR_NODE_NAME
argument_list|,
name|POOL_SIZE
argument_list|,
name|KEEP_ALIVE
argument_list|)
decl_stmt|;
name|ProcedureCoordinator
name|coordinator
init|=
operator|new
name|ProcedureCoordinator
argument_list|(
name|coordinatorComms
argument_list|,
name|pool
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|Procedure
name|createProcedure
parameter_list|(
name|ForeignExceptionDispatcher
name|fed
parameter_list|,
name|String
name|procName
parameter_list|,
name|byte
index|[]
name|procArgs
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|expectedMembers
parameter_list|)
block|{
return|return
name|Mockito
operator|.
name|spy
argument_list|(
name|super
operator|.
name|createProcedure
argument_list|(
name|fed
argument_list|,
name|procName
argument_list|,
name|procArgs
argument_list|,
name|expectedMembers
argument_list|)
argument_list|)
return|;
block|}
block|}
decl_stmt|;
comment|// build and start members
comment|// NOTE: There is a single subprocedure builder for all members here.
name|SubprocedureFactory
name|subprocFactory
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|SubprocedureFactory
operator|.
name|class
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Pair
argument_list|<
name|ProcedureMember
argument_list|,
name|ZKProcedureMemberRpcs
argument_list|>
argument_list|>
name|procMembers
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|members
operator|.
name|length
argument_list|)
decl_stmt|;
comment|// start each member
for|for
control|(
name|String
name|member
range|:
name|members
control|)
block|{
name|ZKWatcher
name|watcher
init|=
name|newZooKeeperWatcher
argument_list|()
decl_stmt|;
name|ZKProcedureMemberRpcs
name|comms
init|=
operator|new
name|ZKProcedureMemberRpcs
argument_list|(
name|watcher
argument_list|,
name|opDescription
argument_list|)
decl_stmt|;
name|ThreadPoolExecutor
name|pool2
init|=
name|ProcedureMember
operator|.
name|defaultPool
argument_list|(
name|member
argument_list|,
literal|1
argument_list|,
name|KEEP_ALIVE
argument_list|)
decl_stmt|;
name|ProcedureMember
name|procMember
init|=
operator|new
name|ProcedureMember
argument_list|(
name|comms
argument_list|,
name|pool2
argument_list|,
name|subprocFactory
argument_list|)
decl_stmt|;
name|procMembers
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<>
argument_list|(
name|procMember
argument_list|,
name|comms
argument_list|)
argument_list|)
expr_stmt|;
name|comms
operator|.
name|start
argument_list|(
name|member
argument_list|,
name|procMember
argument_list|)
expr_stmt|;
block|}
comment|// setup mock member subprocedures
specifier|final
name|List
argument_list|<
name|Subprocedure
argument_list|>
name|subprocs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|procMembers
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ForeignExceptionDispatcher
name|cohortMonitor
init|=
operator|new
name|ForeignExceptionDispatcher
argument_list|()
decl_stmt|;
name|Subprocedure
name|commit
init|=
name|Mockito
operator|.
name|spy
argument_list|(
operator|new
name|SubprocedureImpl
argument_list|(
name|procMembers
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFirst
argument_list|()
argument_list|,
name|opName
argument_list|,
name|cohortMonitor
argument_list|,
name|WAKE_FREQUENCY
argument_list|,
name|TIMEOUT
argument_list|)
argument_list|)
decl_stmt|;
name|subprocs
operator|.
name|add
argument_list|(
name|commit
argument_list|)
expr_stmt|;
block|}
comment|// link subprocedure to buildNewOperation invocation.
specifier|final
name|AtomicInteger
name|i
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// NOTE: would be racy if not an AtomicInteger
name|Mockito
operator|.
name|when
argument_list|(
name|subprocFactory
operator|.
name|buildSubprocedure
argument_list|(
name|Mockito
operator|.
name|eq
argument_list|(
name|opName
argument_list|)
argument_list|,
operator|(
name|byte
index|[]
operator|)
name|Mockito
operator|.
name|argThat
argument_list|(
operator|new
name|ArrayEquals
argument_list|(
name|data
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Subprocedure
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Subprocedure
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|int
name|index
init|=
name|i
operator|.
name|getAndIncrement
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Task size:"
operator|+
name|subprocs
operator|.
name|size
argument_list|()
operator|+
literal|", getting:"
operator|+
name|index
argument_list|)
expr_stmt|;
name|Subprocedure
name|commit
init|=
name|subprocs
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
return|return
name|commit
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// setup spying on the coordinator
comment|//    Procedure proc = Mockito.spy(procBuilder.createProcedure(coordinator, opName, data, expected));
comment|//    Mockito.when(procBuilder.build(coordinator, opName, data, expected)).thenReturn(proc);
comment|// start running the operation
name|Procedure
name|task
init|=
name|coordinator
operator|.
name|startProcedure
argument_list|(
operator|new
name|ForeignExceptionDispatcher
argument_list|()
argument_list|,
name|opName
argument_list|,
name|data
argument_list|,
name|expected
argument_list|)
decl_stmt|;
comment|//    assertEquals("Didn't mock coordinator task", proc, task);
comment|// verify all things ran as expected
comment|//    waitAndVerifyProc(proc, once, once, never(), once, false);
name|waitAndVerifyProc
argument_list|(
name|task
argument_list|,
name|once
argument_list|,
name|once
argument_list|,
name|never
argument_list|()
argument_list|,
name|once
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|verifyCohortSuccessful
argument_list|(
name|expected
argument_list|,
name|subprocFactory
argument_list|,
name|subprocs
argument_list|,
name|once
argument_list|,
name|once
argument_list|,
name|never
argument_list|()
argument_list|,
name|once
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// close all the things
name|closeAll
argument_list|(
name|coordinator
argument_list|,
name|coordinatorComms
argument_list|,
name|procMembers
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test a distributed commit with multiple cohort members, where one of the cohort members has a    * timeout exception during the prepare stage.    */
annotation|@
name|Test
specifier|public
name|void
name|testMultiCohortWithMemberTimeoutDuringPrepare
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|opDescription
init|=
literal|"error injection coordination"
decl_stmt|;
name|String
index|[]
name|cohortMembers
init|=
operator|new
name|String
index|[]
block|{
literal|"one"
block|,
literal|"two"
block|,
literal|"three"
block|}
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
name|cohortMembers
argument_list|)
decl_stmt|;
comment|// error constants
specifier|final
name|int
name|memberErrorIndex
init|=
literal|2
decl_stmt|;
specifier|final
name|CountDownLatch
name|coordinatorReceivedErrorLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
comment|// start running the coordinator and its controller
name|ZKWatcher
name|coordinatorWatcher
init|=
name|newZooKeeperWatcher
argument_list|()
decl_stmt|;
name|ZKProcedureCoordinator
name|coordinatorController
init|=
operator|new
name|ZKProcedureCoordinator
argument_list|(
name|coordinatorWatcher
argument_list|,
name|opDescription
argument_list|,
name|COORDINATOR_NODE_NAME
argument_list|)
decl_stmt|;
name|ThreadPoolExecutor
name|pool
init|=
name|ProcedureCoordinator
operator|.
name|defaultPool
argument_list|(
name|COORDINATOR_NODE_NAME
argument_list|,
name|POOL_SIZE
argument_list|,
name|KEEP_ALIVE
argument_list|)
decl_stmt|;
name|ProcedureCoordinator
name|coordinator
init|=
name|spy
argument_list|(
operator|new
name|ProcedureCoordinator
argument_list|(
name|coordinatorController
argument_list|,
name|pool
argument_list|)
argument_list|)
decl_stmt|;
comment|// start a member for each node
name|SubprocedureFactory
name|subprocFactory
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|SubprocedureFactory
operator|.
name|class
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Pair
argument_list|<
name|ProcedureMember
argument_list|,
name|ZKProcedureMemberRpcs
argument_list|>
argument_list|>
name|members
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|expected
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|member
range|:
name|expected
control|)
block|{
name|ZKWatcher
name|watcher
init|=
name|newZooKeeperWatcher
argument_list|()
decl_stmt|;
name|ZKProcedureMemberRpcs
name|controller
init|=
operator|new
name|ZKProcedureMemberRpcs
argument_list|(
name|watcher
argument_list|,
name|opDescription
argument_list|)
decl_stmt|;
name|ThreadPoolExecutor
name|pool2
init|=
name|ProcedureMember
operator|.
name|defaultPool
argument_list|(
name|member
argument_list|,
literal|1
argument_list|,
name|KEEP_ALIVE
argument_list|)
decl_stmt|;
name|ProcedureMember
name|mem
init|=
operator|new
name|ProcedureMember
argument_list|(
name|controller
argument_list|,
name|pool2
argument_list|,
name|subprocFactory
argument_list|)
decl_stmt|;
name|members
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<>
argument_list|(
name|mem
argument_list|,
name|controller
argument_list|)
argument_list|)
expr_stmt|;
name|controller
operator|.
name|start
argument_list|(
name|member
argument_list|,
name|mem
argument_list|)
expr_stmt|;
block|}
comment|// setup mock subprocedures
specifier|final
name|List
argument_list|<
name|Subprocedure
argument_list|>
name|cohortTasks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|int
index|[]
name|elem
init|=
operator|new
name|int
index|[
literal|1
index|]
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
name|members
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ForeignExceptionDispatcher
name|cohortMonitor
init|=
operator|new
name|ForeignExceptionDispatcher
argument_list|()
decl_stmt|;
specifier|final
name|ProcedureMember
name|comms
init|=
name|members
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFirst
argument_list|()
decl_stmt|;
name|Subprocedure
name|commit
init|=
name|Mockito
operator|.
name|spy
argument_list|(
operator|new
name|SubprocedureImpl
argument_list|(
name|comms
argument_list|,
name|opName
argument_list|,
name|cohortMonitor
argument_list|,
name|WAKE_FREQUENCY
argument_list|,
name|TIMEOUT
argument_list|)
argument_list|)
decl_stmt|;
comment|// This nasty bit has one of the impls throw a TimeoutException
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
name|int
name|index
init|=
name|elem
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|index
operator|==
name|memberErrorIndex
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Sending error to coordinator"
argument_list|)
expr_stmt|;
name|ForeignException
name|remoteCause
init|=
operator|new
name|ForeignException
argument_list|(
literal|"TIMER"
argument_list|,
operator|new
name|TimeoutException
argument_list|(
literal|"subprocTimeout"
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|,
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|Subprocedure
name|r
init|=
operator|(
operator|(
name|Subprocedure
operator|)
name|invocation
operator|.
name|getMock
argument_list|()
operator|)
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Remote commit failure, not propagating error:"
operator|+
name|remoteCause
argument_list|)
expr_stmt|;
name|comms
operator|.
name|receiveAbortProcedure
argument_list|(
name|r
operator|.
name|getName
argument_list|()
argument_list|,
name|remoteCause
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r
operator|.
name|isComplete
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// don't complete the error phase until the coordinator has gotten the error
comment|// notification (which ensures that we never progress past prepare)
try|try
block|{
name|Procedure
operator|.
name|waitForLatch
argument_list|(
name|coordinatorReceivedErrorLatch
argument_list|,
operator|new
name|ForeignExceptionDispatcher
argument_list|()
argument_list|,
name|WAKE_FREQUENCY
argument_list|,
literal|"coordinator received error"
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
literal|"Wait for latch interrupted, done:"
operator|+
operator|(
name|coordinatorReceivedErrorLatch
operator|.
name|getCount
argument_list|()
operator|==
literal|0
operator|)
argument_list|)
expr_stmt|;
comment|// reset the interrupt status on the thread
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
name|elem
index|[
literal|0
index|]
operator|=
operator|++
name|index
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
name|commit
argument_list|)
operator|.
name|acquireBarrier
argument_list|()
expr_stmt|;
name|cohortTasks
operator|.
name|add
argument_list|(
name|commit
argument_list|)
expr_stmt|;
block|}
comment|// pass out a task per member
specifier|final
name|AtomicInteger
name|taskIndex
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|subprocFactory
operator|.
name|buildSubprocedure
argument_list|(
name|Mockito
operator|.
name|eq
argument_list|(
name|opName
argument_list|)
argument_list|,
operator|(
name|byte
index|[]
operator|)
name|Mockito
operator|.
name|argThat
argument_list|(
operator|new
name|ArrayEquals
argument_list|(
name|data
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Subprocedure
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Subprocedure
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|int
name|index
init|=
name|taskIndex
operator|.
name|getAndIncrement
argument_list|()
decl_stmt|;
name|Subprocedure
name|commit
init|=
name|cohortTasks
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
return|return
name|commit
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// setup spying on the coordinator
name|ForeignExceptionDispatcher
name|coordinatorTaskErrorMonitor
init|=
name|Mockito
operator|.
name|spy
argument_list|(
operator|new
name|ForeignExceptionDispatcher
argument_list|()
argument_list|)
decl_stmt|;
name|Procedure
name|coordinatorTask
init|=
name|Mockito
operator|.
name|spy
argument_list|(
operator|new
name|Procedure
argument_list|(
name|coordinator
argument_list|,
name|coordinatorTaskErrorMonitor
argument_list|,
name|WAKE_FREQUENCY
argument_list|,
name|TIMEOUT
argument_list|,
name|opName
argument_list|,
name|data
argument_list|,
name|expected
argument_list|)
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|coordinator
operator|.
name|createProcedure
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
name|opName
argument_list|)
argument_list|,
name|eq
argument_list|(
name|data
argument_list|)
argument_list|,
name|anyListOf
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|coordinatorTask
argument_list|)
expr_stmt|;
comment|// count down the error latch when we get the remote error
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
comment|// pass on the error to the master
name|invocation
operator|.
name|callRealMethod
argument_list|()
expr_stmt|;
comment|// then count down the got error latch
name|coordinatorReceivedErrorLatch
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
name|coordinatorTask
argument_list|)
operator|.
name|receive
argument_list|(
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
expr_stmt|;
comment|// ----------------------------
comment|// start running the operation
comment|// ----------------------------
name|Procedure
name|task
init|=
name|coordinator
operator|.
name|startProcedure
argument_list|(
name|coordinatorTaskErrorMonitor
argument_list|,
name|opName
argument_list|,
name|data
argument_list|,
name|expected
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Didn't mock coordinator task"
argument_list|,
name|coordinatorTask
argument_list|,
name|task
argument_list|)
expr_stmt|;
comment|// wait for the task to complete
try|try
block|{
name|task
operator|.
name|waitForCompleted
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ForeignException
name|fe
parameter_list|)
block|{
comment|// this may get caught or may not
block|}
comment|// -------------
comment|// verification
comment|// -------------
comment|// always expect prepared, never committed, and possible to have cleanup and finish (racy since
comment|// error case)
name|waitAndVerifyProc
argument_list|(
name|coordinatorTask
argument_list|,
name|once
argument_list|,
name|never
argument_list|()
argument_list|,
name|once
argument_list|,
name|atMost
argument_list|(
literal|1
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyCohortSuccessful
argument_list|(
name|expected
argument_list|,
name|subprocFactory
argument_list|,
name|cohortTasks
argument_list|,
name|once
argument_list|,
name|never
argument_list|()
argument_list|,
name|once
argument_list|,
name|once
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// close all the open things
name|closeAll
argument_list|(
name|coordinator
argument_list|,
name|coordinatorController
argument_list|,
name|members
argument_list|)
expr_stmt|;
block|}
comment|/**    * Wait for the coordinator task to complete, and verify all the mocks    * @param task to wait on    * @throws Exception on unexpected failure    */
specifier|private
name|void
name|waitAndVerifyProc
parameter_list|(
name|Procedure
name|proc
parameter_list|,
name|VerificationMode
name|prepare
parameter_list|,
name|VerificationMode
name|commit
parameter_list|,
name|VerificationMode
name|cleanup
parameter_list|,
name|VerificationMode
name|finish
parameter_list|,
name|boolean
name|opHasError
parameter_list|)
throws|throws
name|Exception
block|{
name|boolean
name|caughtError
init|=
literal|false
decl_stmt|;
try|try
block|{
name|proc
operator|.
name|waitForCompleted
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ForeignException
name|fe
parameter_list|)
block|{
name|caughtError
operator|=
literal|true
expr_stmt|;
block|}
comment|// make sure that the task called all the expected phases
name|Mockito
operator|.
name|verify
argument_list|(
name|proc
argument_list|,
name|prepare
argument_list|)
operator|.
name|sendGlobalBarrierStart
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|proc
argument_list|,
name|commit
argument_list|)
operator|.
name|sendGlobalBarrierReached
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|proc
argument_list|,
name|finish
argument_list|)
operator|.
name|sendGlobalBarrierComplete
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Operation error state was unexpected"
argument_list|,
name|opHasError
argument_list|,
name|proc
operator|.
name|getErrorMonitor
argument_list|()
operator|.
name|hasException
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Operation error state was unexpected"
argument_list|,
name|opHasError
argument_list|,
name|caughtError
argument_list|)
expr_stmt|;
block|}
comment|/**    * Wait for the coordinator task to complete, and verify all the mocks    * @param task to wait on    * @throws Exception on unexpected failure    */
specifier|private
name|void
name|waitAndVerifySubproc
parameter_list|(
name|Subprocedure
name|op
parameter_list|,
name|VerificationMode
name|prepare
parameter_list|,
name|VerificationMode
name|commit
parameter_list|,
name|VerificationMode
name|cleanup
parameter_list|,
name|VerificationMode
name|finish
parameter_list|,
name|boolean
name|opHasError
parameter_list|)
throws|throws
name|Exception
block|{
name|boolean
name|caughtError
init|=
literal|false
decl_stmt|;
try|try
block|{
name|op
operator|.
name|waitForLocallyCompleted
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ForeignException
name|fe
parameter_list|)
block|{
name|caughtError
operator|=
literal|true
expr_stmt|;
block|}
comment|// make sure that the task called all the expected phases
name|Mockito
operator|.
name|verify
argument_list|(
name|op
argument_list|,
name|prepare
argument_list|)
operator|.
name|acquireBarrier
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|op
argument_list|,
name|commit
argument_list|)
operator|.
name|insideBarrier
argument_list|()
expr_stmt|;
comment|// We cannot guarantee that cleanup has run so we don't check it.
name|assertEquals
argument_list|(
literal|"Operation error state was unexpected"
argument_list|,
name|opHasError
argument_list|,
name|op
operator|.
name|getErrorCheckable
argument_list|()
operator|.
name|hasException
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Operation error state was unexpected"
argument_list|,
name|opHasError
argument_list|,
name|caughtError
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyCohortSuccessful
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|cohortNames
parameter_list|,
name|SubprocedureFactory
name|subprocFactory
parameter_list|,
name|Iterable
argument_list|<
name|Subprocedure
argument_list|>
name|cohortTasks
parameter_list|,
name|VerificationMode
name|prepare
parameter_list|,
name|VerificationMode
name|commit
parameter_list|,
name|VerificationMode
name|cleanup
parameter_list|,
name|VerificationMode
name|finish
parameter_list|,
name|boolean
name|opHasError
parameter_list|)
throws|throws
name|Exception
block|{
comment|// make sure we build the correct number of cohort members
name|Mockito
operator|.
name|verify
argument_list|(
name|subprocFactory
argument_list|,
name|Mockito
operator|.
name|times
argument_list|(
name|cohortNames
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
operator|.
name|buildSubprocedure
argument_list|(
name|Mockito
operator|.
name|eq
argument_list|(
name|opName
argument_list|)
argument_list|,
operator|(
name|byte
index|[]
operator|)
name|Mockito
operator|.
name|argThat
argument_list|(
operator|new
name|ArrayEquals
argument_list|(
name|data
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// verify that we ran each of the operations cleanly
name|int
name|j
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Subprocedure
name|op
range|:
name|cohortTasks
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Checking mock:"
operator|+
operator|(
name|j
operator|++
operator|)
argument_list|)
expr_stmt|;
name|waitAndVerifySubproc
argument_list|(
name|op
argument_list|,
name|prepare
argument_list|,
name|commit
argument_list|,
name|cleanup
argument_list|,
name|finish
argument_list|,
name|opHasError
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|closeAll
parameter_list|(
name|ProcedureCoordinator
name|coordinator
parameter_list|,
name|ZKProcedureCoordinator
name|coordinatorController
parameter_list|,
name|List
argument_list|<
name|Pair
argument_list|<
name|ProcedureMember
argument_list|,
name|ZKProcedureMemberRpcs
argument_list|>
argument_list|>
name|cohort
parameter_list|)
throws|throws
name|IOException
block|{
comment|// make sure we close all the resources
for|for
control|(
name|Pair
argument_list|<
name|ProcedureMember
argument_list|,
name|ZKProcedureMemberRpcs
argument_list|>
name|member
range|:
name|cohort
control|)
block|{
name|member
operator|.
name|getFirst
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
name|member
operator|.
name|getSecond
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|coordinator
operator|.
name|close
argument_list|()
expr_stmt|;
name|coordinatorController
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

