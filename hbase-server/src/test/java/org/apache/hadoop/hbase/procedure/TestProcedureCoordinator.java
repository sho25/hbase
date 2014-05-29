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
name|assertNull
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
name|anyString
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
name|atLeastOnce
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
name|doAnswer
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
name|doThrow
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
name|inOrder
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
name|mock
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
name|reset
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
name|TimeUnit
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
name|SmallTests
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
name|InOrder
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
comment|/**  * Test Procedure coordinator operation.  *<p>  * This only works correctly when we do<i>class level parallelization</i> of tests. If we do method  * level serialization this class will likely throw all kinds of errors.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestProcedureCoordinator
block|{
comment|// general test constants
specifier|private
specifier|static
specifier|final
name|long
name|WAKE_FREQUENCY
init|=
literal|1000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|TIMEOUT
init|=
literal|100000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|POOL_KEEP_ALIVE
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|nodeName
init|=
literal|"node"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|procName
init|=
literal|"some op"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|procData
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
specifier|private
specifier|static
specifier|final
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
literal|"remote1"
argument_list|,
literal|"remote2"
argument_list|)
decl_stmt|;
comment|// setup the mocks
specifier|private
specifier|final
name|ProcedureCoordinatorRpcs
name|controller
init|=
name|mock
argument_list|(
name|ProcedureCoordinatorRpcs
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Procedure
name|task
init|=
name|mock
argument_list|(
name|Procedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ForeignExceptionDispatcher
name|monitor
init|=
name|mock
argument_list|(
name|ForeignExceptionDispatcher
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// handle to the coordinator for each test
specifier|private
name|ProcedureCoordinator
name|coordinator
decl_stmt|;
annotation|@
name|After
specifier|public
name|void
name|resetTest
parameter_list|()
throws|throws
name|IOException
block|{
comment|// reset all the mocks used for the tests
name|reset
argument_list|(
name|controller
argument_list|,
name|task
argument_list|,
name|monitor
argument_list|)
expr_stmt|;
comment|// close the open coordinator, if it was used
if|if
condition|(
name|coordinator
operator|!=
literal|null
condition|)
name|coordinator
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|ProcedureCoordinator
name|buildNewCoordinator
parameter_list|()
block|{
name|ThreadPoolExecutor
name|pool
init|=
name|ProcedureCoordinator
operator|.
name|defaultPool
argument_list|(
name|nodeName
argument_list|,
literal|1
argument_list|,
name|POOL_KEEP_ALIVE
argument_list|)
decl_stmt|;
return|return
name|spy
argument_list|(
operator|new
name|ProcedureCoordinator
argument_list|(
name|controller
argument_list|,
name|pool
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Currently we can only handle one procedure at a time.  This makes sure we handle that and    * reject submitting more.    */
annotation|@
name|Test
specifier|public
name|void
name|testThreadPoolSize
parameter_list|()
throws|throws
name|Exception
block|{
name|ProcedureCoordinator
name|coordinator
init|=
name|buildNewCoordinator
argument_list|()
decl_stmt|;
name|Procedure
name|proc
init|=
operator|new
name|Procedure
argument_list|(
name|coordinator
argument_list|,
name|monitor
argument_list|,
name|WAKE_FREQUENCY
argument_list|,
name|TIMEOUT
argument_list|,
name|procName
argument_list|,
name|procData
argument_list|,
name|expected
argument_list|)
decl_stmt|;
name|Procedure
name|procSpy
init|=
name|spy
argument_list|(
name|proc
argument_list|)
decl_stmt|;
name|Procedure
name|proc2
init|=
operator|new
name|Procedure
argument_list|(
name|coordinator
argument_list|,
name|monitor
argument_list|,
name|WAKE_FREQUENCY
argument_list|,
name|TIMEOUT
argument_list|,
name|procName
operator|+
literal|"2"
argument_list|,
name|procData
argument_list|,
name|expected
argument_list|)
decl_stmt|;
name|Procedure
name|procSpy2
init|=
name|spy
argument_list|(
name|proc2
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|coordinator
operator|.
name|createProcedure
argument_list|(
name|any
argument_list|(
name|ForeignExceptionDispatcher
operator|.
name|class
argument_list|)
argument_list|,
name|eq
argument_list|(
name|procName
argument_list|)
argument_list|,
name|eq
argument_list|(
name|procData
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
name|procSpy
argument_list|,
name|procSpy2
argument_list|)
expr_stmt|;
name|coordinator
operator|.
name|startProcedure
argument_list|(
name|procSpy
operator|.
name|getErrorMonitor
argument_list|()
argument_list|,
name|procName
argument_list|,
name|procData
argument_list|,
name|expected
argument_list|)
expr_stmt|;
comment|// null here means second procedure failed to start.
name|assertNull
argument_list|(
literal|"Coordinator successfully ran two tasks at once with a single thread pool."
argument_list|,
name|coordinator
operator|.
name|startProcedure
argument_list|(
name|proc2
operator|.
name|getErrorMonitor
argument_list|()
argument_list|,
literal|"another op"
argument_list|,
name|procData
argument_list|,
name|expected
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check handling a connection failure correctly if we get it during the acquiring phase    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testUnreachableControllerDuringPrepare
parameter_list|()
throws|throws
name|Exception
block|{
name|coordinator
operator|=
name|buildNewCoordinator
argument_list|()
expr_stmt|;
comment|// setup the proc
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
literal|"cohort"
argument_list|)
decl_stmt|;
name|Procedure
name|proc
init|=
operator|new
name|Procedure
argument_list|(
name|coordinator
argument_list|,
name|WAKE_FREQUENCY
argument_list|,
name|TIMEOUT
argument_list|,
name|procName
argument_list|,
name|procData
argument_list|,
name|expected
argument_list|)
decl_stmt|;
specifier|final
name|Procedure
name|procSpy
init|=
name|spy
argument_list|(
name|proc
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|coordinator
operator|.
name|createProcedure
argument_list|(
name|any
argument_list|(
name|ForeignExceptionDispatcher
operator|.
name|class
argument_list|)
argument_list|,
name|eq
argument_list|(
name|procName
argument_list|)
argument_list|,
name|eq
argument_list|(
name|procData
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
name|procSpy
argument_list|)
expr_stmt|;
comment|// use the passed controller responses
name|IOException
name|cause
init|=
operator|new
name|IOException
argument_list|(
literal|"Failed to reach comms during acquire"
argument_list|)
decl_stmt|;
name|doThrow
argument_list|(
name|cause
argument_list|)
operator|.
name|when
argument_list|(
name|controller
argument_list|)
operator|.
name|sendGlobalBarrierAcquire
argument_list|(
name|eq
argument_list|(
name|procSpy
argument_list|)
argument_list|,
name|eq
argument_list|(
name|procData
argument_list|)
argument_list|,
name|anyListOf
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
comment|// run the operation
name|proc
operator|=
name|coordinator
operator|.
name|startProcedure
argument_list|(
name|proc
operator|.
name|getErrorMonitor
argument_list|()
argument_list|,
name|procName
argument_list|,
name|procData
argument_list|,
name|expected
argument_list|)
expr_stmt|;
comment|// and wait for it to finish
while|while
condition|(
operator|!
name|proc
operator|.
name|completedLatch
operator|.
name|await
argument_list|(
name|WAKE_FREQUENCY
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
condition|)
empty_stmt|;
name|verify
argument_list|(
name|procSpy
argument_list|,
name|atLeastOnce
argument_list|()
argument_list|)
operator|.
name|receive
argument_list|(
name|any
argument_list|(
name|ForeignException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|coordinator
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|rpcConnectionFailure
argument_list|(
name|anyString
argument_list|()
argument_list|,
name|eq
argument_list|(
name|cause
argument_list|)
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|controller
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|sendGlobalBarrierAcquire
argument_list|(
name|procSpy
argument_list|,
name|procData
argument_list|,
name|expected
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|controller
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|sendGlobalBarrierReached
argument_list|(
name|any
argument_list|(
name|Procedure
operator|.
name|class
argument_list|)
argument_list|,
name|anyListOf
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check handling a connection failure correctly if we get it during the barrier phase    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testUnreachableControllerDuringCommit
parameter_list|()
throws|throws
name|Exception
block|{
name|coordinator
operator|=
name|buildNewCoordinator
argument_list|()
expr_stmt|;
comment|// setup the task and spy on it
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
literal|"cohort"
argument_list|)
decl_stmt|;
specifier|final
name|Procedure
name|spy
init|=
name|spy
argument_list|(
operator|new
name|Procedure
argument_list|(
name|coordinator
argument_list|,
name|WAKE_FREQUENCY
argument_list|,
name|TIMEOUT
argument_list|,
name|procName
argument_list|,
name|procData
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
argument_list|(
name|ForeignExceptionDispatcher
operator|.
name|class
argument_list|)
argument_list|,
name|eq
argument_list|(
name|procName
argument_list|)
argument_list|,
name|eq
argument_list|(
name|procData
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
name|spy
argument_list|)
expr_stmt|;
comment|// use the passed controller responses
name|IOException
name|cause
init|=
operator|new
name|IOException
argument_list|(
literal|"Failed to reach controller during prepare"
argument_list|)
decl_stmt|;
name|doAnswer
argument_list|(
operator|new
name|AcquireBarrierAnswer
argument_list|(
name|procName
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"cohort"
block|}
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|controller
argument_list|)
operator|.
name|sendGlobalBarrierAcquire
argument_list|(
name|eq
argument_list|(
name|spy
argument_list|)
argument_list|,
name|eq
argument_list|(
name|procData
argument_list|)
argument_list|,
name|anyListOf
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|doThrow
argument_list|(
name|cause
argument_list|)
operator|.
name|when
argument_list|(
name|controller
argument_list|)
operator|.
name|sendGlobalBarrierReached
argument_list|(
name|eq
argument_list|(
name|spy
argument_list|)
argument_list|,
name|anyListOf
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
comment|// run the operation
name|Procedure
name|task
init|=
name|coordinator
operator|.
name|startProcedure
argument_list|(
name|spy
operator|.
name|getErrorMonitor
argument_list|()
argument_list|,
name|procName
argument_list|,
name|procData
argument_list|,
name|expected
argument_list|)
decl_stmt|;
comment|// and wait for it to finish
while|while
condition|(
operator|!
name|task
operator|.
name|completedLatch
operator|.
name|await
argument_list|(
name|WAKE_FREQUENCY
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
condition|)
empty_stmt|;
name|verify
argument_list|(
name|spy
argument_list|,
name|atLeastOnce
argument_list|()
argument_list|)
operator|.
name|receive
argument_list|(
name|any
argument_list|(
name|ForeignException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|coordinator
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|rpcConnectionFailure
argument_list|(
name|anyString
argument_list|()
argument_list|,
name|eq
argument_list|(
name|cause
argument_list|)
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|controller
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|sendGlobalBarrierAcquire
argument_list|(
name|eq
argument_list|(
name|spy
argument_list|)
argument_list|,
name|eq
argument_list|(
name|procData
argument_list|)
argument_list|,
name|anyListOf
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|controller
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|sendGlobalBarrierReached
argument_list|(
name|any
argument_list|(
name|Procedure
operator|.
name|class
argument_list|)
argument_list|,
name|anyListOf
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testNoCohort
parameter_list|()
throws|throws
name|Exception
block|{
name|runSimpleProcedure
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testSingleCohortOrchestration
parameter_list|()
throws|throws
name|Exception
block|{
name|runSimpleProcedure
argument_list|(
literal|"one"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testMultipleCohortOrchestration
parameter_list|()
throws|throws
name|Exception
block|{
name|runSimpleProcedure
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
specifier|public
name|void
name|runSimpleProcedure
parameter_list|(
name|String
modifier|...
name|members
parameter_list|)
throws|throws
name|Exception
block|{
name|coordinator
operator|=
name|buildNewCoordinator
argument_list|()
expr_stmt|;
name|Procedure
name|task
init|=
operator|new
name|Procedure
argument_list|(
name|coordinator
argument_list|,
name|monitor
argument_list|,
name|WAKE_FREQUENCY
argument_list|,
name|TIMEOUT
argument_list|,
name|procName
argument_list|,
name|procData
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|members
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|Procedure
name|spy
init|=
name|spy
argument_list|(
name|task
argument_list|)
decl_stmt|;
name|runCoordinatedProcedure
argument_list|(
name|spy
argument_list|,
name|members
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that if nodes join the barrier early we still correctly handle the progress    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testEarlyJoiningBarrier
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
index|[]
name|cohort
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
block|,
literal|"four"
block|}
decl_stmt|;
name|coordinator
operator|=
name|buildNewCoordinator
argument_list|()
expr_stmt|;
specifier|final
name|ProcedureCoordinator
name|ref
init|=
name|coordinator
decl_stmt|;
name|Procedure
name|task
init|=
operator|new
name|Procedure
argument_list|(
name|coordinator
argument_list|,
name|monitor
argument_list|,
name|WAKE_FREQUENCY
argument_list|,
name|TIMEOUT
argument_list|,
name|procName
argument_list|,
name|procData
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|cohort
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|Procedure
name|spy
init|=
name|spy
argument_list|(
name|task
argument_list|)
decl_stmt|;
name|AcquireBarrierAnswer
name|prepare
init|=
operator|new
name|AcquireBarrierAnswer
argument_list|(
name|procName
argument_list|,
name|cohort
argument_list|)
block|{
specifier|public
name|void
name|doWork
parameter_list|()
block|{
comment|// then do some fun where we commit before all nodes have prepared
comment|// "one" commits before anyone else is done
name|ref
operator|.
name|memberAcquiredBarrier
argument_list|(
name|this
operator|.
name|opName
argument_list|,
name|this
operator|.
name|cohort
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|ref
operator|.
name|memberFinishedBarrier
argument_list|(
name|this
operator|.
name|opName
argument_list|,
name|this
operator|.
name|cohort
index|[
literal|0
index|]
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
comment|// but "two" takes a while
name|ref
operator|.
name|memberAcquiredBarrier
argument_list|(
name|this
operator|.
name|opName
argument_list|,
name|this
operator|.
name|cohort
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
comment|// "three"jumps ahead
name|ref
operator|.
name|memberAcquiredBarrier
argument_list|(
name|this
operator|.
name|opName
argument_list|,
name|this
operator|.
name|cohort
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|ref
operator|.
name|memberFinishedBarrier
argument_list|(
name|this
operator|.
name|opName
argument_list|,
name|this
operator|.
name|cohort
index|[
literal|2
index|]
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
comment|// and "four" takes a while
name|ref
operator|.
name|memberAcquiredBarrier
argument_list|(
name|this
operator|.
name|opName
argument_list|,
name|this
operator|.
name|cohort
index|[
literal|3
index|]
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|BarrierAnswer
name|commit
init|=
operator|new
name|BarrierAnswer
argument_list|(
name|procName
argument_list|,
name|cohort
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|doWork
parameter_list|()
block|{
name|ref
operator|.
name|memberFinishedBarrier
argument_list|(
name|opName
argument_list|,
name|this
operator|.
name|cohort
index|[
literal|1
index|]
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|ref
operator|.
name|memberFinishedBarrier
argument_list|(
name|opName
argument_list|,
name|this
operator|.
name|cohort
index|[
literal|3
index|]
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|runCoordinatedOperation
argument_list|(
name|spy
argument_list|,
name|prepare
argument_list|,
name|commit
argument_list|,
name|cohort
argument_list|)
expr_stmt|;
block|}
comment|/**    * Just run a procedure with the standard name and data, with not special task for the mock    * coordinator (it works just like a regular coordinator). For custom behavior see    * {@link #runCoordinatedOperation(Procedure, AcquireBarrierAnswer, BarrierAnswer, String[])}    * .    * @param spy Spy on a real {@link Procedure}    * @param cohort expected cohort members    * @throws Exception on failure    */
specifier|public
name|void
name|runCoordinatedProcedure
parameter_list|(
name|Procedure
name|spy
parameter_list|,
name|String
modifier|...
name|cohort
parameter_list|)
throws|throws
name|Exception
block|{
name|runCoordinatedOperation
argument_list|(
name|spy
argument_list|,
operator|new
name|AcquireBarrierAnswer
argument_list|(
name|procName
argument_list|,
name|cohort
argument_list|)
argument_list|,
operator|new
name|BarrierAnswer
argument_list|(
name|procName
argument_list|,
name|cohort
argument_list|)
argument_list|,
name|cohort
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|runCoordinatedOperation
parameter_list|(
name|Procedure
name|spy
parameter_list|,
name|AcquireBarrierAnswer
name|prepare
parameter_list|,
name|String
modifier|...
name|cohort
parameter_list|)
throws|throws
name|Exception
block|{
name|runCoordinatedOperation
argument_list|(
name|spy
argument_list|,
name|prepare
argument_list|,
operator|new
name|BarrierAnswer
argument_list|(
name|procName
argument_list|,
name|cohort
argument_list|)
argument_list|,
name|cohort
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|runCoordinatedOperation
parameter_list|(
name|Procedure
name|spy
parameter_list|,
name|BarrierAnswer
name|commit
parameter_list|,
name|String
modifier|...
name|cohort
parameter_list|)
throws|throws
name|Exception
block|{
name|runCoordinatedOperation
argument_list|(
name|spy
argument_list|,
operator|new
name|AcquireBarrierAnswer
argument_list|(
name|procName
argument_list|,
name|cohort
argument_list|)
argument_list|,
name|commit
argument_list|,
name|cohort
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|runCoordinatedOperation
parameter_list|(
name|Procedure
name|spy
parameter_list|,
name|AcquireBarrierAnswer
name|prepareOperation
parameter_list|,
name|BarrierAnswer
name|commitOperation
parameter_list|,
name|String
modifier|...
name|cohort
parameter_list|)
throws|throws
name|Exception
block|{
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
name|cohort
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|coordinator
operator|.
name|createProcedure
argument_list|(
name|any
argument_list|(
name|ForeignExceptionDispatcher
operator|.
name|class
argument_list|)
argument_list|,
name|eq
argument_list|(
name|procName
argument_list|)
argument_list|,
name|eq
argument_list|(
name|procData
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
name|spy
argument_list|)
expr_stmt|;
comment|// use the passed controller responses
name|doAnswer
argument_list|(
name|prepareOperation
argument_list|)
operator|.
name|when
argument_list|(
name|controller
argument_list|)
operator|.
name|sendGlobalBarrierAcquire
argument_list|(
name|spy
argument_list|,
name|procData
argument_list|,
name|expected
argument_list|)
expr_stmt|;
name|doAnswer
argument_list|(
name|commitOperation
argument_list|)
operator|.
name|when
argument_list|(
name|controller
argument_list|)
operator|.
name|sendGlobalBarrierReached
argument_list|(
name|eq
argument_list|(
name|spy
argument_list|)
argument_list|,
name|anyListOf
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
comment|// run the operation
name|Procedure
name|task
init|=
name|coordinator
operator|.
name|startProcedure
argument_list|(
name|spy
operator|.
name|getErrorMonitor
argument_list|()
argument_list|,
name|procName
argument_list|,
name|procData
argument_list|,
name|expected
argument_list|)
decl_stmt|;
comment|// and wait for it to finish
name|task
operator|.
name|waitForCompleted
argument_list|()
expr_stmt|;
comment|// make sure we mocked correctly
name|prepareOperation
operator|.
name|ensureRan
argument_list|()
expr_stmt|;
comment|// we never got an exception
name|InOrder
name|inorder
init|=
name|inOrder
argument_list|(
name|spy
argument_list|,
name|controller
argument_list|)
decl_stmt|;
name|inorder
operator|.
name|verify
argument_list|(
name|spy
argument_list|)
operator|.
name|sendGlobalBarrierStart
argument_list|()
expr_stmt|;
name|inorder
operator|.
name|verify
argument_list|(
name|controller
argument_list|)
operator|.
name|sendGlobalBarrierAcquire
argument_list|(
name|task
argument_list|,
name|procData
argument_list|,
name|expected
argument_list|)
expr_stmt|;
name|inorder
operator|.
name|verify
argument_list|(
name|spy
argument_list|)
operator|.
name|sendGlobalBarrierReached
argument_list|()
expr_stmt|;
name|inorder
operator|.
name|verify
argument_list|(
name|controller
argument_list|)
operator|.
name|sendGlobalBarrierReached
argument_list|(
name|eq
argument_list|(
name|task
argument_list|)
argument_list|,
name|anyListOf
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|abstract
class|class
name|OperationAnswer
implements|implements
name|Answer
argument_list|<
name|Void
argument_list|>
block|{
specifier|private
name|boolean
name|ran
init|=
literal|false
decl_stmt|;
specifier|public
name|void
name|ensureRan
parameter_list|()
block|{
name|assertTrue
argument_list|(
literal|"Prepare mocking didn't actually run!"
argument_list|,
name|ran
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|Void
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|this
operator|.
name|ran
operator|=
literal|true
expr_stmt|;
name|doWork
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|protected
specifier|abstract
name|void
name|doWork
parameter_list|()
throws|throws
name|Throwable
function_decl|;
block|}
comment|/**    * Just tell the current coordinator that each of the nodes has prepared    */
specifier|private
class|class
name|AcquireBarrierAnswer
extends|extends
name|OperationAnswer
block|{
specifier|protected
specifier|final
name|String
index|[]
name|cohort
decl_stmt|;
specifier|protected
specifier|final
name|String
name|opName
decl_stmt|;
specifier|public
name|AcquireBarrierAnswer
parameter_list|(
name|String
name|opName
parameter_list|,
name|String
modifier|...
name|cohort
parameter_list|)
block|{
name|this
operator|.
name|cohort
operator|=
name|cohort
expr_stmt|;
name|this
operator|.
name|opName
operator|=
name|opName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|doWork
parameter_list|()
block|{
if|if
condition|(
name|cohort
operator|==
literal|null
condition|)
return|return;
for|for
control|(
name|String
name|member
range|:
name|cohort
control|)
block|{
name|TestProcedureCoordinator
operator|.
name|this
operator|.
name|coordinator
operator|.
name|memberAcquiredBarrier
argument_list|(
name|opName
argument_list|,
name|member
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Just tell the current coordinator that each of the nodes has committed    */
specifier|private
class|class
name|BarrierAnswer
extends|extends
name|OperationAnswer
block|{
specifier|protected
specifier|final
name|String
index|[]
name|cohort
decl_stmt|;
specifier|protected
specifier|final
name|String
name|opName
decl_stmt|;
specifier|public
name|BarrierAnswer
parameter_list|(
name|String
name|opName
parameter_list|,
name|String
modifier|...
name|cohort
parameter_list|)
block|{
name|this
operator|.
name|cohort
operator|=
name|cohort
expr_stmt|;
name|this
operator|.
name|opName
operator|=
name|opName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|doWork
parameter_list|()
block|{
if|if
condition|(
name|cohort
operator|==
literal|null
condition|)
return|return;
for|for
control|(
name|String
name|member
range|:
name|cohort
control|)
block|{
name|TestProcedureCoordinator
operator|.
name|this
operator|.
name|coordinator
operator|.
name|memberFinishedBarrier
argument_list|(
name|opName
argument_list|,
name|member
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

