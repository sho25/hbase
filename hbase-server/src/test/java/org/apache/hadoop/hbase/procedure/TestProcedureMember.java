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
name|verifyZeroInteractions
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
name|concurrent
operator|.
name|ThreadPoolExecutor
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

begin_comment
comment|/**  * Test the procedure member, and it's error handling mechanisms.  */
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
name|TestProcedureMember
block|{
specifier|private
specifier|static
specifier|final
name|long
name|WAKE_FREQUENCY
init|=
literal|100
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
specifier|final
name|String
name|op
init|=
literal|"some op"
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|data
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
specifier|private
specifier|final
name|ForeignExceptionDispatcher
name|mockListener
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
specifier|private
specifier|final
name|SubprocedureFactory
name|mockBuilder
init|=
name|mock
argument_list|(
name|SubprocedureFactory
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ProcedureMemberRpcs
name|mockMemberComms
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ProcedureMemberRpcs
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ProcedureMember
name|member
decl_stmt|;
specifier|private
name|ForeignExceptionDispatcher
name|dispatcher
decl_stmt|;
name|Subprocedure
name|spySub
decl_stmt|;
comment|/**    * Reset all the mock objects    */
annotation|@
name|After
specifier|public
name|void
name|resetTest
parameter_list|()
block|{
name|reset
argument_list|(
name|mockListener
argument_list|,
name|mockBuilder
argument_list|,
name|mockMemberComms
argument_list|)
expr_stmt|;
if|if
condition|(
name|member
operator|!=
literal|null
condition|)
try|try
block|{
name|member
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Build a member using the class level mocks    * @return member to use for tests    */
specifier|private
name|ProcedureMember
name|buildCohortMember
parameter_list|()
block|{
name|String
name|name
init|=
literal|"node"
decl_stmt|;
name|ThreadPoolExecutor
name|pool
init|=
name|ProcedureMember
operator|.
name|defaultPool
argument_list|(
name|name
argument_list|,
literal|1
argument_list|,
name|POOL_KEEP_ALIVE
argument_list|)
decl_stmt|;
return|return
operator|new
name|ProcedureMember
argument_list|(
name|mockMemberComms
argument_list|,
name|pool
argument_list|,
name|mockBuilder
argument_list|)
return|;
block|}
comment|/**    * Setup a procedure member that returns the spied-upon {@link Subprocedure}.    */
specifier|private
name|void
name|buildCohortMemberPair
parameter_list|()
throws|throws
name|IOException
block|{
name|dispatcher
operator|=
operator|new
name|ForeignExceptionDispatcher
argument_list|()
expr_stmt|;
name|String
name|name
init|=
literal|"node"
decl_stmt|;
name|ThreadPoolExecutor
name|pool
init|=
name|ProcedureMember
operator|.
name|defaultPool
argument_list|(
name|name
argument_list|,
literal|1
argument_list|,
name|POOL_KEEP_ALIVE
argument_list|)
decl_stmt|;
name|member
operator|=
operator|new
name|ProcedureMember
argument_list|(
name|mockMemberComms
argument_list|,
name|pool
argument_list|,
name|mockBuilder
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockMemberComms
operator|.
name|getMemberName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|"membername"
argument_list|)
expr_stmt|;
comment|// needed for generating exception
name|Subprocedure
name|subproc
init|=
operator|new
name|EmptySubprocedure
argument_list|(
name|member
argument_list|,
name|dispatcher
argument_list|)
decl_stmt|;
name|spySub
operator|=
name|spy
argument_list|(
name|subproc
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockBuilder
operator|.
name|buildSubprocedure
argument_list|(
name|op
argument_list|,
name|data
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|spySub
argument_list|)
expr_stmt|;
name|addCommitAnswer
argument_list|()
expr_stmt|;
block|}
comment|/**    * Add a 'in barrier phase' response to the mock controller when it gets a acquired notification    */
specifier|private
name|void
name|addCommitAnswer
parameter_list|()
throws|throws
name|IOException
block|{
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
name|member
operator|.
name|receivedReachedGlobalBarrier
argument_list|(
name|op
argument_list|)
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
name|mockMemberComms
argument_list|)
operator|.
name|sendMemberAcquired
argument_list|(
name|any
argument_list|(
name|Subprocedure
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test the normal sub procedure execution case.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|500
argument_list|)
specifier|public
name|void
name|testSimpleRun
parameter_list|()
throws|throws
name|Exception
block|{
name|member
operator|=
name|buildCohortMember
argument_list|()
expr_stmt|;
name|EmptySubprocedure
name|subproc
init|=
operator|new
name|EmptySubprocedure
argument_list|(
name|member
argument_list|,
name|mockListener
argument_list|)
decl_stmt|;
name|EmptySubprocedure
name|spy
init|=
name|spy
argument_list|(
name|subproc
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockBuilder
operator|.
name|buildSubprocedure
argument_list|(
name|op
argument_list|,
name|data
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|spy
argument_list|)
expr_stmt|;
comment|// when we get a prepare, then start the commit phase
name|addCommitAnswer
argument_list|()
expr_stmt|;
comment|// run the operation
comment|// build a new operation
name|Subprocedure
name|subproc1
init|=
name|member
operator|.
name|createSubprocedure
argument_list|(
name|op
argument_list|,
name|data
argument_list|)
decl_stmt|;
name|member
operator|.
name|submitSubprocedure
argument_list|(
name|subproc1
argument_list|)
expr_stmt|;
comment|// and wait for it to finish
name|subproc
operator|.
name|waitForLocallyCompleted
argument_list|()
expr_stmt|;
comment|// make sure everything ran in order
name|InOrder
name|order
init|=
name|inOrder
argument_list|(
name|mockMemberComms
argument_list|,
name|spy
argument_list|)
decl_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|spy
argument_list|)
operator|.
name|acquireBarrier
argument_list|()
expr_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|mockMemberComms
argument_list|)
operator|.
name|sendMemberAcquired
argument_list|(
name|eq
argument_list|(
name|spy
argument_list|)
argument_list|)
expr_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|spy
argument_list|)
operator|.
name|insideBarrier
argument_list|()
expr_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|mockMemberComms
argument_list|)
operator|.
name|sendMemberCompleted
argument_list|(
name|eq
argument_list|(
name|spy
argument_list|)
argument_list|,
name|eq
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|mockMemberComms
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|sendMemberAborted
argument_list|(
name|eq
argument_list|(
name|spy
argument_list|)
argument_list|,
name|any
argument_list|(
name|ForeignException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Make sure we call cleanup etc, when we have an exception during    * {@link Subprocedure#acquireBarrier()}.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testMemberPrepareException
parameter_list|()
throws|throws
name|Exception
block|{
name|buildCohortMemberPair
argument_list|()
expr_stmt|;
comment|// mock an exception on Subprocedure's prepare
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
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Forced IOException in member acquireBarrier"
argument_list|)
throw|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|spySub
argument_list|)
operator|.
name|acquireBarrier
argument_list|()
expr_stmt|;
comment|// run the operation
comment|// build a new operation
name|Subprocedure
name|subproc
init|=
name|member
operator|.
name|createSubprocedure
argument_list|(
name|op
argument_list|,
name|data
argument_list|)
decl_stmt|;
name|member
operator|.
name|submitSubprocedure
argument_list|(
name|subproc
argument_list|)
expr_stmt|;
comment|// if the operation doesn't die properly, then this will timeout
name|member
operator|.
name|closeAndWait
argument_list|(
name|TIMEOUT
argument_list|)
expr_stmt|;
comment|// make sure everything ran in order
name|InOrder
name|order
init|=
name|inOrder
argument_list|(
name|mockMemberComms
argument_list|,
name|spySub
argument_list|)
decl_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|spySub
argument_list|)
operator|.
name|acquireBarrier
argument_list|()
expr_stmt|;
comment|// Later phases not run
name|order
operator|.
name|verify
argument_list|(
name|mockMemberComms
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|sendMemberAcquired
argument_list|(
name|eq
argument_list|(
name|spySub
argument_list|)
argument_list|)
expr_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|spySub
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|insideBarrier
argument_list|()
expr_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|mockMemberComms
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|sendMemberCompleted
argument_list|(
name|eq
argument_list|(
name|spySub
argument_list|)
argument_list|,
name|eq
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
comment|// error recovery path exercised
name|order
operator|.
name|verify
argument_list|(
name|spySub
argument_list|)
operator|.
name|cancel
argument_list|(
name|anyString
argument_list|()
argument_list|,
name|any
argument_list|(
name|Exception
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|spySub
argument_list|)
operator|.
name|cleanup
argument_list|(
name|any
argument_list|(
name|Exception
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Make sure we call cleanup etc, when we have an exception during prepare.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testSendMemberAcquiredCommsFailure
parameter_list|()
throws|throws
name|Exception
block|{
name|buildCohortMemberPair
argument_list|()
expr_stmt|;
comment|// mock an exception on Subprocedure's prepare
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
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Forced IOException in memeber prepare"
argument_list|)
throw|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|mockMemberComms
argument_list|)
operator|.
name|sendMemberAcquired
argument_list|(
name|any
argument_list|(
name|Subprocedure
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
comment|// run the operation
comment|// build a new operation
name|Subprocedure
name|subproc
init|=
name|member
operator|.
name|createSubprocedure
argument_list|(
name|op
argument_list|,
name|data
argument_list|)
decl_stmt|;
name|member
operator|.
name|submitSubprocedure
argument_list|(
name|subproc
argument_list|)
expr_stmt|;
comment|// if the operation doesn't die properly, then this will timeout
name|member
operator|.
name|closeAndWait
argument_list|(
name|TIMEOUT
argument_list|)
expr_stmt|;
comment|// make sure everything ran in order
name|InOrder
name|order
init|=
name|inOrder
argument_list|(
name|mockMemberComms
argument_list|,
name|spySub
argument_list|)
decl_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|spySub
argument_list|)
operator|.
name|acquireBarrier
argument_list|()
expr_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|mockMemberComms
argument_list|)
operator|.
name|sendMemberAcquired
argument_list|(
name|eq
argument_list|(
name|spySub
argument_list|)
argument_list|)
expr_stmt|;
comment|// Later phases not run
name|order
operator|.
name|verify
argument_list|(
name|spySub
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|insideBarrier
argument_list|()
expr_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|mockMemberComms
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|sendMemberCompleted
argument_list|(
name|eq
argument_list|(
name|spySub
argument_list|)
argument_list|,
name|eq
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
comment|// error recovery path exercised
name|order
operator|.
name|verify
argument_list|(
name|spySub
argument_list|)
operator|.
name|cancel
argument_list|(
name|anyString
argument_list|()
argument_list|,
name|any
argument_list|(
name|Exception
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|spySub
argument_list|)
operator|.
name|cleanup
argument_list|(
name|any
argument_list|(
name|Exception
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Fail correctly if coordinator aborts the procedure.  The subprocedure will not interrupt a    * running {@link Subprocedure#prepare} -- prepare needs to finish first, and the the abort    * is checked.  Thus, the {@link Subprocedure#prepare} should succeed but later get rolled back    * via {@link Subprocedure#cleanup}.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testCoordinatorAbort
parameter_list|()
throws|throws
name|Exception
block|{
name|buildCohortMemberPair
argument_list|()
expr_stmt|;
comment|// mock that another node timed out or failed to prepare
specifier|final
name|TimeoutException
name|oate
init|=
operator|new
name|TimeoutException
argument_list|(
literal|"bogus timeout"
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|,
literal|0
argument_list|)
decl_stmt|;
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
comment|// inject a remote error (this would have come from an external thread)
name|spySub
operator|.
name|cancel
argument_list|(
literal|"bogus message"
argument_list|,
name|oate
argument_list|)
expr_stmt|;
comment|// sleep the wake frequency since that is what we promised
name|Thread
operator|.
name|sleep
argument_list|(
name|WAKE_FREQUENCY
argument_list|)
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
name|spySub
argument_list|)
operator|.
name|waitForReachedGlobalBarrier
argument_list|()
expr_stmt|;
comment|// run the operation
comment|// build a new operation
name|Subprocedure
name|subproc
init|=
name|member
operator|.
name|createSubprocedure
argument_list|(
name|op
argument_list|,
name|data
argument_list|)
decl_stmt|;
name|member
operator|.
name|submitSubprocedure
argument_list|(
name|subproc
argument_list|)
expr_stmt|;
comment|// if the operation doesn't die properly, then this will timeout
name|member
operator|.
name|closeAndWait
argument_list|(
name|TIMEOUT
argument_list|)
expr_stmt|;
comment|// make sure everything ran in order
name|InOrder
name|order
init|=
name|inOrder
argument_list|(
name|mockMemberComms
argument_list|,
name|spySub
argument_list|)
decl_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|spySub
argument_list|)
operator|.
name|acquireBarrier
argument_list|()
expr_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|mockMemberComms
argument_list|)
operator|.
name|sendMemberAcquired
argument_list|(
name|eq
argument_list|(
name|spySub
argument_list|)
argument_list|)
expr_stmt|;
comment|// Later phases not run
name|order
operator|.
name|verify
argument_list|(
name|spySub
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|insideBarrier
argument_list|()
expr_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|mockMemberComms
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|sendMemberCompleted
argument_list|(
name|eq
argument_list|(
name|spySub
argument_list|)
argument_list|,
name|eq
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
comment|// error recovery path exercised
name|order
operator|.
name|verify
argument_list|(
name|spySub
argument_list|)
operator|.
name|cancel
argument_list|(
name|anyString
argument_list|()
argument_list|,
name|any
argument_list|(
name|Exception
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|spySub
argument_list|)
operator|.
name|cleanup
argument_list|(
name|any
argument_list|(
name|Exception
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Handle failures if a member's commit phase fails.    *    * NOTE: This is the core difference that makes this different from traditional 2PC.  In true    * 2PC the transaction is committed just before the coordinator sends commit messages to the    * member.  Members are then responsible for reading its TX log.  This implementation actually    * rolls back, and thus breaks the normal TX guarantees.   */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testMemberCommitException
parameter_list|()
throws|throws
name|Exception
block|{
name|buildCohortMemberPair
argument_list|()
expr_stmt|;
comment|// mock an exception on Subprocedure's prepare
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
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Forced IOException in memeber prepare"
argument_list|)
throw|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|spySub
argument_list|)
operator|.
name|insideBarrier
argument_list|()
expr_stmt|;
comment|// run the operation
comment|// build a new operation
name|Subprocedure
name|subproc
init|=
name|member
operator|.
name|createSubprocedure
argument_list|(
name|op
argument_list|,
name|data
argument_list|)
decl_stmt|;
name|member
operator|.
name|submitSubprocedure
argument_list|(
name|subproc
argument_list|)
expr_stmt|;
comment|// if the operation doesn't die properly, then this will timeout
name|member
operator|.
name|closeAndWait
argument_list|(
name|TIMEOUT
argument_list|)
expr_stmt|;
comment|// make sure everything ran in order
name|InOrder
name|order
init|=
name|inOrder
argument_list|(
name|mockMemberComms
argument_list|,
name|spySub
argument_list|)
decl_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|spySub
argument_list|)
operator|.
name|acquireBarrier
argument_list|()
expr_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|mockMemberComms
argument_list|)
operator|.
name|sendMemberAcquired
argument_list|(
name|eq
argument_list|(
name|spySub
argument_list|)
argument_list|)
expr_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|spySub
argument_list|)
operator|.
name|insideBarrier
argument_list|()
expr_stmt|;
comment|// Later phases not run
name|order
operator|.
name|verify
argument_list|(
name|mockMemberComms
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|sendMemberCompleted
argument_list|(
name|eq
argument_list|(
name|spySub
argument_list|)
argument_list|,
name|eq
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
comment|// error recovery path exercised
name|order
operator|.
name|verify
argument_list|(
name|spySub
argument_list|)
operator|.
name|cancel
argument_list|(
name|anyString
argument_list|()
argument_list|,
name|any
argument_list|(
name|Exception
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|spySub
argument_list|)
operator|.
name|cleanup
argument_list|(
name|any
argument_list|(
name|Exception
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Handle Failures if a member's commit phase succeeds but notification to coordinator fails    *    * NOTE: This is the core difference that makes this different from traditional 2PC.  In true    * 2PC the transaction is committed just before the coordinator sends commit messages to the    * member.  Members are then responsible for reading its TX log.  This implementation actually    * rolls back, and thus breaks the normal TX guarantees.   */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testMemberCommitCommsFailure
parameter_list|()
throws|throws
name|Exception
block|{
name|buildCohortMemberPair
argument_list|()
expr_stmt|;
specifier|final
name|TimeoutException
name|oate
init|=
operator|new
name|TimeoutException
argument_list|(
literal|"bogus timeout"
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|,
literal|0
argument_list|)
decl_stmt|;
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
comment|// inject a remote error (this would have come from an external thread)
name|spySub
operator|.
name|cancel
argument_list|(
literal|"commit comms fail"
argument_list|,
name|oate
argument_list|)
expr_stmt|;
comment|// sleep the wake frequency since that is what we promised
name|Thread
operator|.
name|sleep
argument_list|(
name|WAKE_FREQUENCY
argument_list|)
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
name|mockMemberComms
argument_list|)
operator|.
name|sendMemberCompleted
argument_list|(
name|any
argument_list|(
name|Subprocedure
operator|.
name|class
argument_list|)
argument_list|,
name|eq
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
comment|// run the operation
comment|// build a new operation
name|Subprocedure
name|subproc
init|=
name|member
operator|.
name|createSubprocedure
argument_list|(
name|op
argument_list|,
name|data
argument_list|)
decl_stmt|;
name|member
operator|.
name|submitSubprocedure
argument_list|(
name|subproc
argument_list|)
expr_stmt|;
comment|// if the operation doesn't die properly, then this will timeout
name|member
operator|.
name|closeAndWait
argument_list|(
name|TIMEOUT
argument_list|)
expr_stmt|;
comment|// make sure everything ran in order
name|InOrder
name|order
init|=
name|inOrder
argument_list|(
name|mockMemberComms
argument_list|,
name|spySub
argument_list|)
decl_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|spySub
argument_list|)
operator|.
name|acquireBarrier
argument_list|()
expr_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|mockMemberComms
argument_list|)
operator|.
name|sendMemberAcquired
argument_list|(
name|eq
argument_list|(
name|spySub
argument_list|)
argument_list|)
expr_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|spySub
argument_list|)
operator|.
name|insideBarrier
argument_list|()
expr_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|mockMemberComms
argument_list|)
operator|.
name|sendMemberCompleted
argument_list|(
name|eq
argument_list|(
name|spySub
argument_list|)
argument_list|,
name|eq
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
comment|// error recovery path exercised
name|order
operator|.
name|verify
argument_list|(
name|spySub
argument_list|)
operator|.
name|cancel
argument_list|(
name|anyString
argument_list|()
argument_list|,
name|any
argument_list|(
name|Exception
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|spySub
argument_list|)
operator|.
name|cleanup
argument_list|(
name|any
argument_list|(
name|Exception
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Fail correctly on getting an external error while waiting for the prepared latch    * @throws Exception on failure    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testPropagateConnectionErrorBackToManager
parameter_list|()
throws|throws
name|Exception
block|{
comment|// setup the operation
name|member
operator|=
name|buildCohortMember
argument_list|()
expr_stmt|;
name|ProcedureMember
name|memberSpy
init|=
name|spy
argument_list|(
name|member
argument_list|)
decl_stmt|;
comment|// setup the commit and the spy
specifier|final
name|ForeignExceptionDispatcher
name|dispatcher
init|=
operator|new
name|ForeignExceptionDispatcher
argument_list|()
decl_stmt|;
name|ForeignExceptionDispatcher
name|dispSpy
init|=
name|spy
argument_list|(
name|dispatcher
argument_list|)
decl_stmt|;
name|Subprocedure
name|commit
init|=
operator|new
name|EmptySubprocedure
argument_list|(
name|member
argument_list|,
name|dispatcher
argument_list|)
decl_stmt|;
name|Subprocedure
name|spy
init|=
name|spy
argument_list|(
name|commit
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockBuilder
operator|.
name|buildSubprocedure
argument_list|(
name|op
argument_list|,
name|data
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|spy
argument_list|)
expr_stmt|;
comment|// fail during the prepare phase
name|doThrow
argument_list|(
operator|new
name|ForeignException
argument_list|(
literal|"SRC"
argument_list|,
literal|"prepare exception"
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|spy
argument_list|)
operator|.
name|acquireBarrier
argument_list|()
expr_stmt|;
comment|// and throw a connection error when we try to tell the controller about it
name|doThrow
argument_list|(
operator|new
name|IOException
argument_list|(
literal|"Controller is down!"
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|mockMemberComms
argument_list|)
operator|.
name|sendMemberAborted
argument_list|(
name|eq
argument_list|(
name|spy
argument_list|)
argument_list|,
name|any
argument_list|(
name|ForeignException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
comment|// run the operation
comment|// build a new operation
name|Subprocedure
name|subproc
init|=
name|memberSpy
operator|.
name|createSubprocedure
argument_list|(
name|op
argument_list|,
name|data
argument_list|)
decl_stmt|;
name|memberSpy
operator|.
name|submitSubprocedure
argument_list|(
name|subproc
argument_list|)
expr_stmt|;
comment|// if the operation doesn't die properly, then this will timeout
name|memberSpy
operator|.
name|closeAndWait
argument_list|(
name|TIMEOUT
argument_list|)
expr_stmt|;
comment|// make sure everything ran in order
name|InOrder
name|order
init|=
name|inOrder
argument_list|(
name|mockMemberComms
argument_list|,
name|spy
argument_list|,
name|dispSpy
argument_list|)
decl_stmt|;
comment|// make sure we acquire.
name|order
operator|.
name|verify
argument_list|(
name|spy
argument_list|)
operator|.
name|acquireBarrier
argument_list|()
expr_stmt|;
name|order
operator|.
name|verify
argument_list|(
name|mockMemberComms
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|sendMemberAcquired
argument_list|(
name|spy
argument_list|)
expr_stmt|;
comment|// TODO Need to do another refactor to get this to propagate to the coordinator.
comment|// make sure we pass a remote exception back the controller
comment|//    order.verify(mockMemberComms).sendMemberAborted(eq(spy),
comment|//      any(ExternalException.class));
comment|//    order.verify(dispSpy).receiveError(anyString(),
comment|//        any(ExternalException.class), any());
block|}
comment|/**    * Test that the cohort member correctly doesn't attempt to start a task when the builder cannot    * correctly build a new task for the requested operation    * @throws Exception on failure    */
annotation|@
name|Test
specifier|public
name|void
name|testNoTaskToBeRunFromRequest
parameter_list|()
throws|throws
name|Exception
block|{
name|ThreadPoolExecutor
name|pool
init|=
name|mock
argument_list|(
name|ThreadPoolExecutor
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockBuilder
operator|.
name|buildSubprocedure
argument_list|(
name|op
argument_list|,
name|data
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|null
argument_list|)
operator|.
name|thenThrow
argument_list|(
operator|new
name|IllegalStateException
argument_list|(
literal|"Wrong state!"
argument_list|)
argument_list|,
operator|new
name|IllegalArgumentException
argument_list|(
literal|"can't understand the args"
argument_list|)
argument_list|)
expr_stmt|;
name|member
operator|=
operator|new
name|ProcedureMember
argument_list|(
name|mockMemberComms
argument_list|,
name|pool
argument_list|,
name|mockBuilder
argument_list|)
expr_stmt|;
comment|// builder returns null
comment|// build a new operation
name|Subprocedure
name|subproc
init|=
name|member
operator|.
name|createSubprocedure
argument_list|(
name|op
argument_list|,
name|data
argument_list|)
decl_stmt|;
name|member
operator|.
name|submitSubprocedure
argument_list|(
name|subproc
argument_list|)
expr_stmt|;
comment|// throws an illegal state exception
try|try
block|{
comment|// build a new operation
name|Subprocedure
name|subproc2
init|=
name|member
operator|.
name|createSubprocedure
argument_list|(
name|op
argument_list|,
name|data
argument_list|)
decl_stmt|;
name|member
operator|.
name|submitSubprocedure
argument_list|(
name|subproc2
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|ise
parameter_list|)
block|{     }
comment|// throws an illegal argument exception
try|try
block|{
comment|// build a new operation
name|Subprocedure
name|subproc3
init|=
name|member
operator|.
name|createSubprocedure
argument_list|(
name|op
argument_list|,
name|data
argument_list|)
decl_stmt|;
name|member
operator|.
name|submitSubprocedure
argument_list|(
name|subproc3
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|iae
parameter_list|)
block|{     }
comment|// no request should reach the pool
name|verifyZeroInteractions
argument_list|(
name|pool
argument_list|)
expr_stmt|;
comment|// get two abort requests
comment|// TODO Need to do another refactor to get this to propagate to the coordinator.
comment|// verify(mockMemberComms, times(2)).sendMemberAborted(any(Subprocedure.class), any(ExternalException.class));
block|}
comment|/**    * Helper {@link Procedure} who's phase for each step is just empty    */
specifier|public
class|class
name|EmptySubprocedure
extends|extends
name|SubprocedureImpl
block|{
specifier|public
name|EmptySubprocedure
parameter_list|(
name|ProcedureMember
name|member
parameter_list|,
name|ForeignExceptionDispatcher
name|dispatcher
parameter_list|)
block|{
name|super
argument_list|(
name|member
argument_list|,
name|op
argument_list|,
name|dispatcher
argument_list|,
comment|// TODO 1000000 is an arbitrary number that I picked.
name|WAKE_FREQUENCY
argument_list|,
name|TIMEOUT
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

