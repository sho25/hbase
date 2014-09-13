begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
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
name|HConstants
operator|.
name|NO_NONCE
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
name|*
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
name|Chore
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
name|HBaseConfiguration
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
name|RegionServerTests
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
name|Stoppable
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
name|EnvironmentEdgeManager
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
name|ManualEnvironmentEdge
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
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
name|TestServerNonceManager
block|{
annotation|@
name|Test
specifier|public
name|void
name|testNormalStartEnd
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|long
index|[]
name|numbers
init|=
operator|new
name|long
index|[]
block|{
name|NO_NONCE
block|,
literal|1
block|,
literal|2
block|,
name|Long
operator|.
name|MAX_VALUE
block|,
name|Long
operator|.
name|MIN_VALUE
block|}
decl_stmt|;
name|ServerNonceManager
name|nm
init|=
name|createManager
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
name|numbers
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numbers
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
name|assertTrue
argument_list|(
name|nm
operator|.
name|startOperation
argument_list|(
name|numbers
index|[
name|i
index|]
argument_list|,
name|numbers
index|[
name|j
index|]
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Should be able to start operation the second time w/o nonces.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numbers
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|assertTrue
argument_list|(
name|nm
operator|.
name|startOperation
argument_list|(
name|numbers
index|[
name|i
index|]
argument_list|,
name|NO_NONCE
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Fail all operations - should be able to restart.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numbers
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numbers
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
name|nm
operator|.
name|endOperation
argument_list|(
name|numbers
index|[
name|i
index|]
argument_list|,
name|numbers
index|[
name|j
index|]
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|nm
operator|.
name|startOperation
argument_list|(
name|numbers
index|[
name|i
index|]
argument_list|,
name|numbers
index|[
name|j
index|]
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Succeed all operations - should not be able to restart, except for NO_NONCE.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numbers
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numbers
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
name|nm
operator|.
name|endOperation
argument_list|(
name|numbers
index|[
name|i
index|]
argument_list|,
name|numbers
index|[
name|j
index|]
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|numbers
index|[
name|j
index|]
operator|==
name|NO_NONCE
argument_list|,
name|nm
operator|.
name|startOperation
argument_list|(
name|numbers
index|[
name|i
index|]
argument_list|,
name|numbers
index|[
name|j
index|]
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoEndWithoutStart
parameter_list|()
block|{
name|ServerNonceManager
name|nm
init|=
name|createManager
argument_list|()
decl_stmt|;
try|try
block|{
name|nm
operator|.
name|endOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|1
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should have thrown"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|err
parameter_list|)
block|{}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCleanup
parameter_list|()
throws|throws
name|Exception
block|{
name|ManualEnvironmentEdge
name|edge
init|=
operator|new
name|ManualEnvironmentEdge
argument_list|()
decl_stmt|;
name|EnvironmentEdgeManager
operator|.
name|injectEdge
argument_list|(
name|edge
argument_list|)
expr_stmt|;
try|try
block|{
name|ServerNonceManager
name|nm
init|=
name|createManager
argument_list|(
literal|6
argument_list|)
decl_stmt|;
name|Chore
name|cleanup
init|=
name|nm
operator|.
name|createCleanupChore
argument_list|(
name|Mockito
operator|.
name|mock
argument_list|(
name|Stoppable
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
name|edge
operator|.
name|setValue
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|nm
operator|.
name|startOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|1
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|nm
operator|.
name|startOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|2
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|nm
operator|.
name|startOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|3
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|edge
operator|.
name|setValue
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|nm
operator|.
name|endOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|1
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|edge
operator|.
name|setValue
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|nm
operator|.
name|endOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|2
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|edge
operator|.
name|setValue
argument_list|(
literal|9
argument_list|)
expr_stmt|;
name|cleanup
operator|.
name|choreForTesting
argument_list|()
expr_stmt|;
comment|// Nonce 1 has been cleaned up.
name|assertTrue
argument_list|(
name|nm
operator|.
name|startOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|1
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Nonce 2 has not been cleaned up.
name|assertFalse
argument_list|(
name|nm
operator|.
name|startOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|2
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Nonce 3 was active and active ops should never be cleaned up; try to end and start.
name|nm
operator|.
name|endOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|3
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|nm
operator|.
name|startOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|3
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|edge
operator|.
name|setValue
argument_list|(
literal|11
argument_list|)
expr_stmt|;
name|cleanup
operator|.
name|choreForTesting
argument_list|()
expr_stmt|;
comment|// Now, nonce 2 has been cleaned up.
name|assertTrue
argument_list|(
name|nm
operator|.
name|startOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|2
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|EnvironmentEdgeManager
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWalNonces
parameter_list|()
throws|throws
name|Exception
block|{
name|ManualEnvironmentEdge
name|edge
init|=
operator|new
name|ManualEnvironmentEdge
argument_list|()
decl_stmt|;
name|EnvironmentEdgeManager
operator|.
name|injectEdge
argument_list|(
name|edge
argument_list|)
expr_stmt|;
try|try
block|{
name|ServerNonceManager
name|nm
init|=
name|createManager
argument_list|(
literal|6
argument_list|)
decl_stmt|;
name|Chore
name|cleanup
init|=
name|nm
operator|.
name|createCleanupChore
argument_list|(
name|Mockito
operator|.
name|mock
argument_list|(
name|Stoppable
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
comment|// Add nonces from WAL, including dups.
name|edge
operator|.
name|setValue
argument_list|(
literal|12
argument_list|)
expr_stmt|;
name|nm
operator|.
name|reportOperationFromWal
argument_list|(
name|NO_NONCE
argument_list|,
literal|1
argument_list|,
literal|8
argument_list|)
expr_stmt|;
name|nm
operator|.
name|reportOperationFromWal
argument_list|(
name|NO_NONCE
argument_list|,
literal|2
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|nm
operator|.
name|reportOperationFromWal
argument_list|(
name|NO_NONCE
argument_list|,
literal|3
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|nm
operator|.
name|reportOperationFromWal
argument_list|(
name|NO_NONCE
argument_list|,
literal|3
argument_list|,
literal|6
argument_list|)
expr_stmt|;
comment|// WAL nonces should prevent cross-server conflicts.
name|assertFalse
argument_list|(
name|nm
operator|.
name|startOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|1
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Make sure we ignore very old nonces, but not borderline old nonces.
name|assertTrue
argument_list|(
name|nm
operator|.
name|startOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|2
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|nm
operator|.
name|startOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|3
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Make sure grace period is counted from recovery time.
name|edge
operator|.
name|setValue
argument_list|(
literal|17
argument_list|)
expr_stmt|;
name|cleanup
operator|.
name|choreForTesting
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|nm
operator|.
name|startOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|1
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|nm
operator|.
name|startOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|3
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|edge
operator|.
name|setValue
argument_list|(
literal|19
argument_list|)
expr_stmt|;
name|cleanup
operator|.
name|choreForTesting
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|nm
operator|.
name|startOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|1
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|nm
operator|.
name|startOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|3
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|EnvironmentEdgeManager
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConcurrentAttempts
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|ServerNonceManager
name|nm
init|=
name|createManager
argument_list|()
decl_stmt|;
name|nm
operator|.
name|startOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|1
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
expr_stmt|;
name|TestRunnable
name|tr
init|=
operator|new
name|TestRunnable
argument_list|(
name|nm
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
decl_stmt|;
name|Thread
name|t
init|=
name|tr
operator|.
name|start
argument_list|()
decl_stmt|;
name|waitForThreadToBlockOrExit
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|nm
operator|.
name|endOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|1
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// operation succeeded
name|t
operator|.
name|join
argument_list|()
expr_stmt|;
comment|// thread must now unblock and not proceed (result checked inside).
name|tr
operator|.
name|propagateError
argument_list|()
expr_stmt|;
name|nm
operator|.
name|startOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|2
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
expr_stmt|;
name|tr
operator|=
operator|new
name|TestRunnable
argument_list|(
name|nm
argument_list|,
literal|2
argument_list|,
literal|true
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|=
name|tr
operator|.
name|start
argument_list|()
expr_stmt|;
name|waitForThreadToBlockOrExit
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|nm
operator|.
name|endOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|2
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|t
operator|.
name|join
argument_list|()
expr_stmt|;
comment|// thread must now unblock and allow us to proceed (result checked inside).
name|tr
operator|.
name|propagateError
argument_list|()
expr_stmt|;
name|nm
operator|.
name|endOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|2
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// that is to say we should be able to end operation
name|nm
operator|.
name|startOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|3
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
expr_stmt|;
name|tr
operator|=
operator|new
name|TestRunnable
argument_list|(
name|nm
argument_list|,
literal|4
argument_list|,
literal|true
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
expr_stmt|;
name|tr
operator|.
name|start
argument_list|()
operator|.
name|join
argument_list|()
expr_stmt|;
comment|// nonce 3 must have no bearing on nonce 4
name|tr
operator|.
name|propagateError
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStopWaiting
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|ServerNonceManager
name|nm
init|=
name|createManager
argument_list|()
decl_stmt|;
name|nm
operator|.
name|setConflictWaitIterationMs
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Stoppable
name|stoppingStoppable
init|=
name|createStoppable
argument_list|()
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|stoppingStoppable
operator|.
name|isStopped
argument_list|()
argument_list|)
operator|.
name|thenAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Boolean
argument_list|>
argument_list|()
block|{
name|AtomicInteger
name|answer
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|3
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Boolean
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
return|return
literal|0
operator|<
name|answer
operator|.
name|decrementAndGet
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|nm
operator|.
name|startOperation
argument_list|(
name|NO_NONCE
argument_list|,
literal|1
argument_list|,
name|createStoppable
argument_list|()
argument_list|)
expr_stmt|;
name|TestRunnable
name|tr
init|=
operator|new
name|TestRunnable
argument_list|(
name|nm
argument_list|,
literal|1
argument_list|,
literal|null
argument_list|,
name|stoppingStoppable
argument_list|)
decl_stmt|;
name|Thread
name|t
init|=
name|tr
operator|.
name|start
argument_list|()
decl_stmt|;
name|waitForThreadToBlockOrExit
argument_list|(
name|t
argument_list|)
expr_stmt|;
comment|// thread must eventually throw
name|t
operator|.
name|join
argument_list|()
expr_stmt|;
name|tr
operator|.
name|propagateError
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|waitForThreadToBlockOrExit
parameter_list|(
name|Thread
name|t
parameter_list|)
throws|throws
name|InterruptedException
block|{
for|for
control|(
name|int
name|i
init|=
literal|9
init|;
name|i
operator|>=
literal|0
condition|;
operator|--
name|i
control|)
block|{
if|if
condition|(
name|t
operator|.
name|getState
argument_list|()
operator|==
name|Thread
operator|.
name|State
operator|.
name|TIMED_WAITING
operator|||
name|t
operator|.
name|getState
argument_list|()
operator|==
name|Thread
operator|.
name|State
operator|.
name|WAITING
operator|||
name|t
operator|.
name|getState
argument_list|()
operator|==
name|Thread
operator|.
name|State
operator|.
name|BLOCKED
operator|||
name|t
operator|.
name|getState
argument_list|()
operator|==
name|Thread
operator|.
name|State
operator|.
name|TERMINATED
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|i
operator|>
literal|0
condition|)
name|Thread
operator|.
name|sleep
argument_list|(
literal|300
argument_list|)
expr_stmt|;
block|}
comment|// Thread didn't block in 3 seconds. What is it doing? Continue the test, we'd rather
comment|// have a very strange false positive then false negative due to timing.
block|}
specifier|private
specifier|static
class|class
name|TestRunnable
implements|implements
name|Runnable
block|{
specifier|public
specifier|final
name|CountDownLatch
name|startedLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
comment|// It's the final countdown!
specifier|private
specifier|final
name|ServerNonceManager
name|nm
decl_stmt|;
specifier|private
specifier|final
name|long
name|nonce
decl_stmt|;
specifier|private
specifier|final
name|Boolean
name|expected
decl_stmt|;
specifier|private
specifier|final
name|Stoppable
name|stoppable
decl_stmt|;
specifier|private
name|Throwable
name|throwable
init|=
literal|null
decl_stmt|;
specifier|public
name|TestRunnable
parameter_list|(
name|ServerNonceManager
name|nm
parameter_list|,
name|long
name|nonce
parameter_list|,
name|Boolean
name|expected
parameter_list|,
name|Stoppable
name|stoppable
parameter_list|)
block|{
name|this
operator|.
name|nm
operator|=
name|nm
expr_stmt|;
name|this
operator|.
name|nonce
operator|=
name|nonce
expr_stmt|;
name|this
operator|.
name|expected
operator|=
name|expected
expr_stmt|;
name|this
operator|.
name|stoppable
operator|=
name|stoppable
expr_stmt|;
block|}
specifier|public
name|void
name|propagateError
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|throwable
operator|==
literal|null
condition|)
return|return;
throw|throw
operator|new
name|Exception
argument_list|(
name|throwable
argument_list|)
throw|;
block|}
specifier|public
name|Thread
name|start
parameter_list|()
block|{
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|t
operator|=
name|Threads
operator|.
name|setDaemonThreadRunning
argument_list|(
name|t
argument_list|)
expr_stmt|;
try|try
block|{
name|startedLatch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Unexpected"
argument_list|)
expr_stmt|;
block|}
return|return
name|t
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|startedLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|boolean
name|shouldThrow
init|=
name|expected
operator|==
literal|null
decl_stmt|;
name|boolean
name|hasThrown
init|=
literal|true
decl_stmt|;
try|try
block|{
name|boolean
name|result
init|=
name|nm
operator|.
name|startOperation
argument_list|(
name|NO_NONCE
argument_list|,
name|nonce
argument_list|,
name|stoppable
argument_list|)
decl_stmt|;
name|hasThrown
operator|=
literal|false
expr_stmt|;
if|if
condition|(
operator|!
name|shouldThrow
condition|)
block|{
name|assertEquals
argument_list|(
name|expected
operator|.
name|booleanValue
argument_list|()
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
operator|!
name|shouldThrow
condition|)
block|{
name|throwable
operator|=
name|t
expr_stmt|;
block|}
block|}
if|if
condition|(
name|shouldThrow
operator|&&
operator|!
name|hasThrown
condition|)
block|{
name|throwable
operator|=
operator|new
name|AssertionError
argument_list|(
literal|"Should have thrown"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|Stoppable
name|createStoppable
parameter_list|()
block|{
name|Stoppable
name|s
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|Stoppable
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|s
operator|.
name|isStopped
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
return|return
name|s
return|;
block|}
specifier|private
name|ServerNonceManager
name|createManager
parameter_list|()
block|{
return|return
name|createManager
argument_list|(
literal|null
argument_list|)
return|;
block|}
specifier|private
name|ServerNonceManager
name|createManager
parameter_list|(
name|Integer
name|gracePeriod
parameter_list|)
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
if|if
condition|(
name|gracePeriod
operator|!=
literal|null
condition|)
block|{
name|conf
operator|.
name|setInt
argument_list|(
name|ServerNonceManager
operator|.
name|HASH_NONCE_GRACE_PERIOD_KEY
argument_list|,
name|gracePeriod
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ServerNonceManager
argument_list|(
name|conf
argument_list|)
return|;
block|}
block|}
end_class

end_unit

