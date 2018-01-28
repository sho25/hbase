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
name|anyString
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
name|SmallTests
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

begin_comment
comment|/**  * Demonstrate how Procedure handles single members, multiple members, and errors semantics  */
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
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestProcedure
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
name|TestProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
name|ProcedureCoordinator
name|coord
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|coord
operator|=
name|mock
argument_list|(
name|ProcedureCoordinator
operator|.
name|class
argument_list|)
expr_stmt|;
specifier|final
name|ProcedureCoordinatorRpcs
name|comms
init|=
name|mock
argument_list|(
name|ProcedureCoordinatorRpcs
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|coord
operator|.
name|getRpcs
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|comms
argument_list|)
expr_stmt|;
comment|// make it not null
block|}
specifier|static
class|class
name|LatchedProcedure
extends|extends
name|Procedure
block|{
name|CountDownLatch
name|startedAcquireBarrier
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|CountDownLatch
name|startedDuringBarrier
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|CountDownLatch
name|completedProcedure
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|public
name|LatchedProcedure
parameter_list|(
name|ProcedureCoordinator
name|coord
parameter_list|,
name|ForeignExceptionDispatcher
name|monitor
parameter_list|,
name|long
name|wakeFreq
parameter_list|,
name|long
name|timeout
parameter_list|,
name|String
name|opName
parameter_list|,
name|byte
index|[]
name|data
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|expectedMembers
parameter_list|)
block|{
name|super
argument_list|(
name|coord
argument_list|,
name|monitor
argument_list|,
name|wakeFreq
argument_list|,
name|timeout
argument_list|,
name|opName
argument_list|,
name|data
argument_list|,
name|expectedMembers
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|sendGlobalBarrierStart
parameter_list|()
block|{
name|startedAcquireBarrier
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|sendGlobalBarrierReached
parameter_list|()
block|{
name|startedDuringBarrier
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|sendGlobalBarrierComplete
parameter_list|()
block|{
name|completedProcedure
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * With a single member, verify ordered execution.  The Coordinator side is run in a separate    * thread so we can only trigger from members and wait for particular state latches.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testSingleMember
parameter_list|()
throws|throws
name|Exception
block|{
comment|// The member
name|List
argument_list|<
name|String
argument_list|>
name|members
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|members
operator|.
name|add
argument_list|(
literal|"member"
argument_list|)
expr_stmt|;
name|LatchedProcedure
name|proc
init|=
operator|new
name|LatchedProcedure
argument_list|(
name|coord
argument_list|,
operator|new
name|ForeignExceptionDispatcher
argument_list|()
argument_list|,
literal|100
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
literal|"op"
argument_list|,
literal|null
argument_list|,
name|members
argument_list|)
decl_stmt|;
specifier|final
name|LatchedProcedure
name|procspy
init|=
name|spy
argument_list|(
name|proc
argument_list|)
decl_stmt|;
comment|// coordinator: start the barrier procedure
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|procspy
operator|.
name|call
argument_list|()
expr_stmt|;
block|}
block|}
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// coordinator: wait for the barrier to be acquired, then send start barrier
name|proc
operator|.
name|startedAcquireBarrier
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// we only know that {@link Procedure#sendStartBarrier()} was called, and others are blocked.
name|verify
argument_list|(
name|procspy
argument_list|)
operator|.
name|sendGlobalBarrierStart
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|procspy
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|sendGlobalBarrierReached
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|procspy
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|sendGlobalBarrierComplete
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|procspy
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|barrierAcquiredByMember
argument_list|(
name|anyString
argument_list|()
argument_list|)
expr_stmt|;
comment|// member: trigger global barrier acquisition
name|proc
operator|.
name|barrierAcquiredByMember
argument_list|(
name|members
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// coordinator: wait for global barrier to be acquired.
name|proc
operator|.
name|acquiredBarrierLatch
operator|.
name|await
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|procspy
argument_list|)
operator|.
name|sendGlobalBarrierStart
argument_list|()
expr_stmt|;
comment|// old news
comment|// since two threads, we cannot guarantee that {@link Procedure#sendSatsifiedBarrier()} was
comment|// or was not called here.
comment|// member: trigger global barrier release
name|proc
operator|.
name|barrierReleasedByMember
argument_list|(
name|members
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
comment|// coordinator: wait for procedure to be completed
name|proc
operator|.
name|completedProcedure
operator|.
name|await
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|procspy
argument_list|)
operator|.
name|sendGlobalBarrierReached
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|procspy
argument_list|)
operator|.
name|sendGlobalBarrierComplete
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|procspy
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|receive
argument_list|(
name|any
argument_list|()
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
name|testMultipleMember
parameter_list|()
throws|throws
name|Exception
block|{
comment|// 2 members
name|List
argument_list|<
name|String
argument_list|>
name|members
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|members
operator|.
name|add
argument_list|(
literal|"member1"
argument_list|)
expr_stmt|;
name|members
operator|.
name|add
argument_list|(
literal|"member2"
argument_list|)
expr_stmt|;
name|LatchedProcedure
name|proc
init|=
operator|new
name|LatchedProcedure
argument_list|(
name|coord
argument_list|,
operator|new
name|ForeignExceptionDispatcher
argument_list|()
argument_list|,
literal|100
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
literal|"op"
argument_list|,
literal|null
argument_list|,
name|members
argument_list|)
decl_stmt|;
specifier|final
name|LatchedProcedure
name|procspy
init|=
name|spy
argument_list|(
name|proc
argument_list|)
decl_stmt|;
comment|// start the barrier procedure
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|procspy
operator|.
name|call
argument_list|()
expr_stmt|;
block|}
block|}
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// coordinator: wait for the barrier to be acquired, then send start barrier
name|procspy
operator|.
name|startedAcquireBarrier
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// we only know that {@link Procedure#sendStartBarrier()} was called, and others are blocked.
name|verify
argument_list|(
name|procspy
argument_list|)
operator|.
name|sendGlobalBarrierStart
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|procspy
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|sendGlobalBarrierReached
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|procspy
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|sendGlobalBarrierComplete
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|procspy
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|barrierAcquiredByMember
argument_list|(
name|anyString
argument_list|()
argument_list|)
expr_stmt|;
comment|// no externals
comment|// member0: [1/2] trigger global barrier acquisition.
name|procspy
operator|.
name|barrierAcquiredByMember
argument_list|(
name|members
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// coordinator not satisified.
name|verify
argument_list|(
name|procspy
argument_list|)
operator|.
name|sendGlobalBarrierStart
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|procspy
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|sendGlobalBarrierReached
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|procspy
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|sendGlobalBarrierComplete
argument_list|()
expr_stmt|;
comment|// member 1: [2/2] trigger global barrier acquisition.
name|procspy
operator|.
name|barrierAcquiredByMember
argument_list|(
name|members
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|// coordinator: wait for global barrier to be acquired.
name|procspy
operator|.
name|startedDuringBarrier
operator|.
name|await
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|procspy
argument_list|)
operator|.
name|sendGlobalBarrierStart
argument_list|()
expr_stmt|;
comment|// old news
comment|// member 1, 2: trigger global barrier release
name|procspy
operator|.
name|barrierReleasedByMember
argument_list|(
name|members
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|procspy
operator|.
name|barrierReleasedByMember
argument_list|(
name|members
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
comment|// coordinator wait for procedure to be completed
name|procspy
operator|.
name|completedProcedure
operator|.
name|await
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|procspy
argument_list|)
operator|.
name|sendGlobalBarrierReached
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|procspy
argument_list|)
operator|.
name|sendGlobalBarrierComplete
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|procspy
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|receive
argument_list|(
name|any
argument_list|()
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
name|testErrorPropagation
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|members
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|members
operator|.
name|add
argument_list|(
literal|"member"
argument_list|)
expr_stmt|;
name|Procedure
name|proc
init|=
operator|new
name|Procedure
argument_list|(
name|coord
argument_list|,
operator|new
name|ForeignExceptionDispatcher
argument_list|()
argument_list|,
literal|100
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
literal|"op"
argument_list|,
literal|null
argument_list|,
name|members
argument_list|)
decl_stmt|;
specifier|final
name|Procedure
name|procspy
init|=
name|spy
argument_list|(
name|proc
argument_list|)
decl_stmt|;
name|ForeignException
name|cause
init|=
operator|new
name|ForeignException
argument_list|(
literal|"SRC"
argument_list|,
literal|"External Exception"
argument_list|)
decl_stmt|;
name|proc
operator|.
name|receive
argument_list|(
name|cause
argument_list|)
expr_stmt|;
comment|// start the barrier procedure
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|procspy
operator|.
name|call
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
name|t
operator|.
name|join
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|procspy
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|sendGlobalBarrierStart
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|procspy
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|sendGlobalBarrierReached
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|procspy
argument_list|)
operator|.
name|sendGlobalBarrierComplete
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
name|testBarrieredErrorPropagation
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|members
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|members
operator|.
name|add
argument_list|(
literal|"member"
argument_list|)
expr_stmt|;
name|LatchedProcedure
name|proc
init|=
operator|new
name|LatchedProcedure
argument_list|(
name|coord
argument_list|,
operator|new
name|ForeignExceptionDispatcher
argument_list|()
argument_list|,
literal|100
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
literal|"op"
argument_list|,
literal|null
argument_list|,
name|members
argument_list|)
decl_stmt|;
specifier|final
name|LatchedProcedure
name|procspy
init|=
name|spy
argument_list|(
name|proc
argument_list|)
decl_stmt|;
comment|// start the barrier procedure
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|procspy
operator|.
name|call
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// now test that we can put an error in before the commit phase runs
name|procspy
operator|.
name|startedAcquireBarrier
operator|.
name|await
argument_list|()
expr_stmt|;
name|ForeignException
name|cause
init|=
operator|new
name|ForeignException
argument_list|(
literal|"SRC"
argument_list|,
literal|"External Exception"
argument_list|)
decl_stmt|;
name|procspy
operator|.
name|receive
argument_list|(
name|cause
argument_list|)
expr_stmt|;
name|procspy
operator|.
name|barrierAcquiredByMember
argument_list|(
name|members
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|join
argument_list|()
expr_stmt|;
comment|// verify state of all the object
name|verify
argument_list|(
name|procspy
argument_list|)
operator|.
name|sendGlobalBarrierStart
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|procspy
argument_list|)
operator|.
name|sendGlobalBarrierComplete
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|procspy
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|sendGlobalBarrierReached
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

