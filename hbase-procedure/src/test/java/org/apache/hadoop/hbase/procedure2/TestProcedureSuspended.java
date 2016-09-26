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
name|procedure2
package|;
end_package

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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|AtomicLong
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
name|HBaseCommonTestingUtility
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
name|store
operator|.
name|ProcedureStore
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
name|store
operator|.
name|NoopProcedureStore
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
name|TestProcedureSuspended
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestProcedureSuspended
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|PROCEDURE_EXECUTOR_SLOTS
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Procedure
name|NULL_PROC
init|=
literal|null
decl_stmt|;
specifier|private
name|ProcedureExecutor
argument_list|<
name|TestProcEnv
argument_list|>
name|procExecutor
decl_stmt|;
specifier|private
name|ProcedureStore
name|procStore
decl_stmt|;
specifier|private
name|HBaseCommonTestingUtility
name|htu
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
name|htu
operator|=
operator|new
name|HBaseCommonTestingUtility
argument_list|()
expr_stmt|;
name|procStore
operator|=
operator|new
name|NoopProcedureStore
argument_list|()
expr_stmt|;
name|procExecutor
operator|=
operator|new
name|ProcedureExecutor
argument_list|(
name|htu
operator|.
name|getConfiguration
argument_list|()
argument_list|,
operator|new
name|TestProcEnv
argument_list|()
argument_list|,
name|procStore
argument_list|)
expr_stmt|;
name|procStore
operator|.
name|start
argument_list|(
name|PROCEDURE_EXECUTOR_SLOTS
argument_list|)
expr_stmt|;
name|procExecutor
operator|.
name|start
argument_list|(
name|PROCEDURE_EXECUTOR_SLOTS
argument_list|,
literal|true
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
name|procExecutor
operator|.
name|stop
argument_list|()
expr_stmt|;
name|procStore
operator|.
name|stop
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|10000
argument_list|)
specifier|public
name|void
name|testSuspendWhileHoldingLocks
parameter_list|()
block|{
specifier|final
name|AtomicBoolean
name|lockA
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|final
name|AtomicBoolean
name|lockB
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|final
name|TestLockProcedure
name|p1keyA
init|=
operator|new
name|TestLockProcedure
argument_list|(
name|lockA
argument_list|,
literal|"keyA"
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
specifier|final
name|TestLockProcedure
name|p2keyA
init|=
operator|new
name|TestLockProcedure
argument_list|(
name|lockA
argument_list|,
literal|"keyA"
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
specifier|final
name|TestLockProcedure
name|p3keyB
init|=
operator|new
name|TestLockProcedure
argument_list|(
name|lockB
argument_list|,
literal|"keyB"
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|procExecutor
operator|.
name|submitProcedure
argument_list|(
name|p1keyA
argument_list|)
expr_stmt|;
name|procExecutor
operator|.
name|submitProcedure
argument_list|(
name|p2keyA
argument_list|)
expr_stmt|;
name|procExecutor
operator|.
name|submitProcedure
argument_list|(
name|p3keyB
argument_list|)
expr_stmt|;
comment|// first run p1, p3 are able to run p2 is blocked by p1
name|waitAndAssertTimestamp
argument_list|(
name|p1keyA
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|waitAndAssertTimestamp
argument_list|(
name|p2keyA
argument_list|,
literal|0
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|waitAndAssertTimestamp
argument_list|(
name|p3keyB
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|lockA
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|lockB
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// release p3
name|p3keyB
operator|.
name|setThrowSuspend
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|procExecutor
operator|.
name|getRunnableSet
argument_list|()
operator|.
name|addFront
argument_list|(
name|p3keyB
argument_list|)
expr_stmt|;
name|waitAndAssertTimestamp
argument_list|(
name|p1keyA
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|waitAndAssertTimestamp
argument_list|(
name|p2keyA
argument_list|,
literal|0
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|waitAndAssertTimestamp
argument_list|(
name|p3keyB
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|lockA
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// wait until p3 is fully completed
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExecutor
argument_list|,
name|p3keyB
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|lockB
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// rollback p2 and wait until is fully completed
name|p1keyA
operator|.
name|setTriggerRollback
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|procExecutor
operator|.
name|getRunnableSet
argument_list|()
operator|.
name|addFront
argument_list|(
name|p1keyA
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExecutor
argument_list|,
name|p1keyA
argument_list|)
expr_stmt|;
comment|// p2 should start and suspend
name|waitAndAssertTimestamp
argument_list|(
name|p1keyA
argument_list|,
literal|4
argument_list|,
literal|60000
argument_list|)
expr_stmt|;
name|waitAndAssertTimestamp
argument_list|(
name|p2keyA
argument_list|,
literal|1
argument_list|,
literal|7
argument_list|)
expr_stmt|;
name|waitAndAssertTimestamp
argument_list|(
name|p3keyB
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|lockA
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// wait until p2 is fully completed
name|p2keyA
operator|.
name|setThrowSuspend
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|procExecutor
operator|.
name|getRunnableSet
argument_list|()
operator|.
name|addFront
argument_list|(
name|p2keyA
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExecutor
argument_list|,
name|p2keyA
argument_list|)
expr_stmt|;
name|waitAndAssertTimestamp
argument_list|(
name|p1keyA
argument_list|,
literal|4
argument_list|,
literal|60000
argument_list|)
expr_stmt|;
name|waitAndAssertTimestamp
argument_list|(
name|p2keyA
argument_list|,
literal|2
argument_list|,
literal|8
argument_list|)
expr_stmt|;
name|waitAndAssertTimestamp
argument_list|(
name|p3keyB
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|lockA
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|lockB
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|10000
argument_list|)
specifier|public
name|void
name|testYieldWhileHoldingLocks
parameter_list|()
block|{
specifier|final
name|AtomicBoolean
name|lock
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|final
name|TestLockProcedure
name|p1
init|=
operator|new
name|TestLockProcedure
argument_list|(
name|lock
argument_list|,
literal|"key"
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
specifier|final
name|TestLockProcedure
name|p2
init|=
operator|new
name|TestLockProcedure
argument_list|(
name|lock
argument_list|,
literal|"key"
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|procExecutor
operator|.
name|submitProcedure
argument_list|(
name|p1
argument_list|)
expr_stmt|;
name|procExecutor
operator|.
name|submitProcedure
argument_list|(
name|p2
argument_list|)
expr_stmt|;
comment|// try to execute a bunch of yield on p1, p2 should be blocked
while|while
condition|(
name|p1
operator|.
name|getTimestamps
argument_list|()
operator|.
name|size
argument_list|()
operator|<
literal|100
condition|)
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|p2
operator|.
name|getTimestamps
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// wait until p1 is completed
name|p1
operator|.
name|setThrowYield
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExecutor
argument_list|,
name|p1
argument_list|)
expr_stmt|;
comment|// try to execute a bunch of yield on p2
while|while
condition|(
name|p2
operator|.
name|getTimestamps
argument_list|()
operator|.
name|size
argument_list|()
operator|<
literal|100
condition|)
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|p1
operator|.
name|getTimestamps
argument_list|()
operator|.
name|get
argument_list|(
name|p1
operator|.
name|getTimestamps
argument_list|()
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|longValue
argument_list|()
operator|+
literal|1
argument_list|,
name|p2
operator|.
name|getTimestamps
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
comment|// wait until p2 is completed
name|p1
operator|.
name|setThrowYield
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExecutor
argument_list|,
name|p1
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|waitAndAssertTimestamp
parameter_list|(
name|TestLockProcedure
name|proc
parameter_list|,
name|int
name|size
parameter_list|,
name|int
name|lastTs
parameter_list|)
block|{
specifier|final
name|ArrayList
argument_list|<
name|Long
argument_list|>
name|timestamps
init|=
name|proc
operator|.
name|getTimestamps
argument_list|()
decl_stmt|;
while|while
condition|(
name|timestamps
operator|.
name|size
argument_list|()
operator|<
name|size
condition|)
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|proc
operator|+
literal|" -> "
operator|+
name|timestamps
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|size
argument_list|,
name|timestamps
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|size
operator|>
literal|0
condition|)
block|{
name|assertEquals
argument_list|(
name|lastTs
argument_list|,
name|timestamps
operator|.
name|get
argument_list|(
name|timestamps
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|TestLockProcedure
extends|extends
name|Procedure
argument_list|<
name|TestProcEnv
argument_list|>
block|{
specifier|private
specifier|final
name|ArrayList
argument_list|<
name|Long
argument_list|>
name|timestamps
init|=
operator|new
name|ArrayList
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|String
name|key
decl_stmt|;
specifier|private
name|boolean
name|triggerRollback
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|throwSuspend
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|throwYield
init|=
literal|false
decl_stmt|;
specifier|private
name|AtomicBoolean
name|lock
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|hasLock
init|=
literal|false
decl_stmt|;
specifier|public
name|TestLockProcedure
parameter_list|(
specifier|final
name|AtomicBoolean
name|lock
parameter_list|,
specifier|final
name|String
name|key
parameter_list|,
specifier|final
name|boolean
name|throwYield
parameter_list|,
specifier|final
name|boolean
name|throwSuspend
parameter_list|)
block|{
name|this
operator|.
name|lock
operator|=
name|lock
expr_stmt|;
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
name|this
operator|.
name|throwYield
operator|=
name|throwYield
expr_stmt|;
name|this
operator|.
name|throwSuspend
operator|=
name|throwSuspend
expr_stmt|;
block|}
specifier|public
name|void
name|setThrowYield
parameter_list|(
specifier|final
name|boolean
name|throwYield
parameter_list|)
block|{
name|this
operator|.
name|throwYield
operator|=
name|throwYield
expr_stmt|;
block|}
specifier|public
name|void
name|setThrowSuspend
parameter_list|(
specifier|final
name|boolean
name|throwSuspend
parameter_list|)
block|{
name|this
operator|.
name|throwSuspend
operator|=
name|throwSuspend
expr_stmt|;
block|}
specifier|public
name|void
name|setTriggerRollback
parameter_list|(
specifier|final
name|boolean
name|triggerRollback
parameter_list|)
block|{
name|this
operator|.
name|triggerRollback
operator|=
name|triggerRollback
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Procedure
index|[]
name|execute
parameter_list|(
specifier|final
name|TestProcEnv
name|env
parameter_list|)
throws|throws
name|ProcedureYieldException
throws|,
name|ProcedureSuspendedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"EXECUTE "
operator|+
name|this
operator|+
literal|" suspend "
operator|+
operator|(
name|lock
operator|!=
literal|null
operator|)
argument_list|)
expr_stmt|;
name|timestamps
operator|.
name|add
argument_list|(
name|env
operator|.
name|nextTimestamp
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|triggerRollback
condition|)
block|{
name|setFailure
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
operator|new
name|Exception
argument_list|(
literal|"injected failure"
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|throwYield
condition|)
block|{
throw|throw
operator|new
name|ProcedureYieldException
argument_list|()
throw|;
block|}
elseif|else
if|if
condition|(
name|throwSuspend
condition|)
block|{
throw|throw
operator|new
name|ProcedureSuspendedException
argument_list|()
throw|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|rollback
parameter_list|(
specifier|final
name|TestProcEnv
name|env
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"ROLLBACK "
operator|+
name|this
argument_list|)
expr_stmt|;
name|timestamps
operator|.
name|add
argument_list|(
name|env
operator|.
name|nextTimestamp
argument_list|()
operator|*
literal|10000
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|acquireLock
parameter_list|(
specifier|final
name|TestProcEnv
name|env
parameter_list|)
block|{
if|if
condition|(
operator|(
name|hasLock
operator|=
name|lock
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
operator|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"ACQUIRE LOCK "
operator|+
name|this
operator|+
literal|" "
operator|+
operator|(
name|hasLock
operator|)
argument_list|)
expr_stmt|;
block|}
return|return
name|hasLock
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|releaseLock
parameter_list|(
specifier|final
name|TestProcEnv
name|env
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"RELEASE LOCK "
operator|+
name|this
operator|+
literal|" "
operator|+
name|hasLock
argument_list|)
expr_stmt|;
name|lock
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|hasLock
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|holdLock
parameter_list|(
specifier|final
name|TestProcEnv
name|env
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|hasLock
parameter_list|(
specifier|final
name|TestProcEnv
name|env
parameter_list|)
block|{
return|return
name|hasLock
return|;
block|}
specifier|public
name|ArrayList
argument_list|<
name|Long
argument_list|>
name|getTimestamps
parameter_list|()
block|{
return|return
name|timestamps
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|toStringClassDetails
parameter_list|(
name|StringBuilder
name|builder
parameter_list|)
block|{
name|builder
operator|.
name|append
argument_list|(
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"("
operator|+
name|key
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|abort
parameter_list|(
name|TestProcEnv
name|env
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|serializeStateData
parameter_list|(
specifier|final
name|OutputStream
name|stream
parameter_list|)
throws|throws
name|IOException
block|{     }
annotation|@
name|Override
specifier|protected
name|void
name|deserializeStateData
parameter_list|(
specifier|final
name|InputStream
name|stream
parameter_list|)
throws|throws
name|IOException
block|{     }
block|}
specifier|private
specifier|static
class|class
name|TestProcEnv
block|{
specifier|public
specifier|final
name|AtomicLong
name|timestamp
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|public
name|long
name|nextTimestamp
parameter_list|()
block|{
return|return
name|timestamp
operator|.
name|incrementAndGet
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

