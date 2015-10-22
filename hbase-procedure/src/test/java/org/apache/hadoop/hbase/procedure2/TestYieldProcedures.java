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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|Assert
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
name|TestYieldProcedures
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
name|TestYieldProcedures
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
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|Path
name|testDir
decl_stmt|;
specifier|private
name|Path
name|logDir
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
name|testDir
operator|=
name|htu
operator|.
name|getDataTestDir
argument_list|()
expr_stmt|;
name|fs
operator|=
name|testDir
operator|.
name|getFileSystem
argument_list|(
name|htu
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|testDir
operator|.
name|depth
argument_list|()
operator|>
literal|1
argument_list|)
expr_stmt|;
name|logDir
operator|=
operator|new
name|Path
argument_list|(
name|testDir
argument_list|,
literal|"proc-logs"
argument_list|)
expr_stmt|;
name|procStore
operator|=
name|ProcedureTestingUtility
operator|.
name|createWalStore
argument_list|(
name|htu
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|logDir
argument_list|)
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
name|fs
operator|.
name|delete
argument_list|(
name|logDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testYieldEachExecutionStep
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|NUM_STATES
init|=
literal|3
decl_stmt|;
name|TestStateMachineProcedure
index|[]
name|procs
init|=
operator|new
name|TestStateMachineProcedure
index|[
literal|3
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
name|procs
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|procs
index|[
name|i
index|]
operator|=
operator|new
name|TestStateMachineProcedure
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|procExecutor
operator|.
name|submitProcedure
argument_list|(
name|procs
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|ProcedureTestingUtility
operator|.
name|waitNoProcedureRunning
argument_list|(
name|procExecutor
argument_list|)
expr_stmt|;
comment|// verify yield during execute()
name|long
name|prevTimestamp
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|execStep
init|=
literal|0
init|;
name|execStep
operator|<
name|NUM_STATES
condition|;
operator|++
name|execStep
control|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|procs
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|assertEquals
argument_list|(
name|NUM_STATES
operator|*
literal|2
argument_list|,
name|procs
index|[
name|i
index|]
operator|.
name|getExecutionInfo
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|TestStateMachineProcedure
operator|.
name|ExecutionInfo
name|info
init|=
name|procs
index|[
name|i
index|]
operator|.
name|getExecutionInfo
argument_list|()
operator|.
name|get
argument_list|(
name|execStep
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"i="
operator|+
name|i
operator|+
literal|" execStep="
operator|+
name|execStep
operator|+
literal|" timestamp="
operator|+
name|info
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|info
operator|.
name|isRollback
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|execStep
argument_list|,
name|info
operator|.
name|getStep
argument_list|()
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|prevTimestamp
operator|+
literal|1
argument_list|,
name|info
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|prevTimestamp
operator|++
expr_stmt|;
block|}
block|}
comment|// verify yield during rollback()
name|int
name|count
init|=
name|NUM_STATES
decl_stmt|;
for|for
control|(
name|int
name|execStep
init|=
name|NUM_STATES
operator|-
literal|1
init|;
name|execStep
operator|>=
literal|0
condition|;
operator|--
name|execStep
control|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|procs
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|assertEquals
argument_list|(
name|NUM_STATES
operator|*
literal|2
argument_list|,
name|procs
index|[
name|i
index|]
operator|.
name|getExecutionInfo
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|TestStateMachineProcedure
operator|.
name|ExecutionInfo
name|info
init|=
name|procs
index|[
name|i
index|]
operator|.
name|getExecutionInfo
argument_list|()
operator|.
name|get
argument_list|(
name|count
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"i="
operator|+
name|i
operator|+
literal|" execStep="
operator|+
name|execStep
operator|+
literal|" timestamp="
operator|+
name|info
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|info
operator|.
name|isRollback
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|execStep
argument_list|,
name|info
operator|.
name|getStep
argument_list|()
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|prevTimestamp
operator|+
literal|1
argument_list|,
name|info
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|prevTimestamp
operator|++
expr_stmt|;
block|}
name|count
operator|++
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testYieldOnInterrupt
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|NUM_STATES
init|=
literal|3
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
name|TestStateMachineProcedure
name|proc
init|=
operator|new
name|TestStateMachineProcedure
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|submitAndWait
argument_list|(
name|procExecutor
argument_list|,
name|proc
argument_list|)
expr_stmt|;
comment|// test execute (we execute steps twice, one has the IE the other completes)
name|assertEquals
argument_list|(
name|NUM_STATES
operator|*
literal|4
argument_list|,
name|proc
operator|.
name|getExecutionInfo
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUM_STATES
condition|;
operator|++
name|i
control|)
block|{
name|TestStateMachineProcedure
operator|.
name|ExecutionInfo
name|info
init|=
name|proc
operator|.
name|getExecutionInfo
argument_list|()
operator|.
name|get
argument_list|(
name|count
operator|++
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|info
operator|.
name|isRollback
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|info
operator|.
name|getStep
argument_list|()
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
name|info
operator|=
name|proc
operator|.
name|getExecutionInfo
argument_list|()
operator|.
name|get
argument_list|(
name|count
operator|++
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|info
operator|.
name|isRollback
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|info
operator|.
name|getStep
argument_list|()
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// test rollback (we execute steps twice, one has the IE the other completes)
for|for
control|(
name|int
name|i
init|=
name|NUM_STATES
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
operator|--
name|i
control|)
block|{
name|TestStateMachineProcedure
operator|.
name|ExecutionInfo
name|info
init|=
name|proc
operator|.
name|getExecutionInfo
argument_list|()
operator|.
name|get
argument_list|(
name|count
operator|++
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|info
operator|.
name|isRollback
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|info
operator|.
name|getStep
argument_list|()
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
name|info
operator|=
name|proc
operator|.
name|getExecutionInfo
argument_list|()
operator|.
name|get
argument_list|(
name|count
operator|++
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|info
operator|.
name|isRollback
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|info
operator|.
name|getStep
argument_list|()
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
specifier|public
specifier|static
class|class
name|TestStateMachineProcedure
extends|extends
name|StateMachineProcedure
argument_list|<
name|TestProcEnv
argument_list|,
name|TestStateMachineProcedure
operator|.
name|State
argument_list|>
block|{
enum|enum
name|State
block|{
name|STATE_1
block|,
name|STATE_2
block|,
name|STATE_3
block|}
specifier|public
class|class
name|ExecutionInfo
block|{
specifier|private
specifier|final
name|boolean
name|rollback
decl_stmt|;
specifier|private
specifier|final
name|long
name|timestamp
decl_stmt|;
specifier|private
specifier|final
name|State
name|step
decl_stmt|;
specifier|public
name|ExecutionInfo
parameter_list|(
name|long
name|timestamp
parameter_list|,
name|State
name|step
parameter_list|,
name|boolean
name|isRollback
parameter_list|)
block|{
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
name|this
operator|.
name|step
operator|=
name|step
expr_stmt|;
name|this
operator|.
name|rollback
operator|=
name|isRollback
expr_stmt|;
block|}
specifier|public
name|State
name|getStep
parameter_list|()
block|{
return|return
name|step
return|;
block|}
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|timestamp
return|;
block|}
specifier|public
name|boolean
name|isRollback
parameter_list|()
block|{
return|return
name|rollback
return|;
block|}
block|}
specifier|private
specifier|final
name|ArrayList
argument_list|<
name|ExecutionInfo
argument_list|>
name|executionInfo
init|=
operator|new
name|ArrayList
argument_list|<
name|ExecutionInfo
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|aborted
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|throwInterruptOnceOnEachStep
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|abortOnFinalStep
decl_stmt|;
specifier|public
name|TestStateMachineProcedure
parameter_list|()
block|{
name|this
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TestStateMachineProcedure
parameter_list|(
name|boolean
name|abortOnFinalStep
parameter_list|,
name|boolean
name|throwInterruptOnceOnEachStep
parameter_list|)
block|{
name|this
operator|.
name|abortOnFinalStep
operator|=
name|abortOnFinalStep
expr_stmt|;
name|this
operator|.
name|throwInterruptOnceOnEachStep
operator|=
name|throwInterruptOnceOnEachStep
expr_stmt|;
block|}
specifier|public
name|ArrayList
argument_list|<
name|ExecutionInfo
argument_list|>
name|getExecutionInfo
parameter_list|()
block|{
return|return
name|executionInfo
return|;
block|}
annotation|@
name|Override
specifier|protected
name|StateMachineProcedure
operator|.
name|Flow
name|executeFromState
parameter_list|(
name|TestProcEnv
name|env
parameter_list|,
name|State
name|state
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"execute step "
operator|+
name|state
argument_list|)
expr_stmt|;
name|executionInfo
operator|.
name|add
argument_list|(
operator|new
name|ExecutionInfo
argument_list|(
name|env
operator|.
name|nextTimestamp
argument_list|()
argument_list|,
name|state
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|150
argument_list|)
expr_stmt|;
if|if
condition|(
name|throwInterruptOnceOnEachStep
operator|&&
operator|(
operator|(
name|executionInfo
operator|.
name|size
argument_list|()
operator|-
literal|1
operator|)
operator|%
literal|2
operator|)
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"THROW INTERRUPT"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|InterruptedException
argument_list|(
literal|"test interrupt"
argument_list|)
throw|;
block|}
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|STATE_1
case|:
name|setNextState
argument_list|(
name|State
operator|.
name|STATE_2
argument_list|)
expr_stmt|;
break|break;
case|case
name|STATE_2
case|:
name|setNextState
argument_list|(
name|State
operator|.
name|STATE_3
argument_list|)
expr_stmt|;
break|break;
case|case
name|STATE_3
case|:
if|if
condition|(
name|abortOnFinalStep
condition|)
block|{
name|setFailure
argument_list|(
literal|"test"
argument_list|,
operator|new
name|IOException
argument_list|(
literal|"Requested abort on final step"
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|Flow
operator|.
name|NO_MORE_STATE
return|;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|rollbackState
parameter_list|(
name|TestProcEnv
name|env
parameter_list|,
specifier|final
name|State
name|state
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"rollback state "
operator|+
name|state
argument_list|)
expr_stmt|;
name|executionInfo
operator|.
name|add
argument_list|(
operator|new
name|ExecutionInfo
argument_list|(
name|env
operator|.
name|nextTimestamp
argument_list|()
argument_list|,
name|state
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|150
argument_list|)
expr_stmt|;
if|if
condition|(
name|throwInterruptOnceOnEachStep
operator|&&
operator|(
operator|(
name|executionInfo
operator|.
name|size
argument_list|()
operator|-
literal|1
operator|)
operator|%
literal|2
operator|)
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"THROW INTERRUPT"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|InterruptedException
argument_list|(
literal|"test interrupt"
argument_list|)
throw|;
block|}
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|STATE_1
case|:
break|break;
case|case
name|STATE_2
case|:
break|break;
case|case
name|STATE_3
case|:
break|break;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|State
name|getState
parameter_list|(
specifier|final
name|int
name|stateId
parameter_list|)
block|{
return|return
name|State
operator|.
name|values
argument_list|()
index|[
name|stateId
index|]
return|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|getStateId
parameter_list|(
specifier|final
name|State
name|state
parameter_list|)
block|{
return|return
name|state
operator|.
name|ordinal
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|State
name|getInitialState
parameter_list|()
block|{
return|return
name|State
operator|.
name|STATE_1
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|isYieldBeforeExecuteFromState
parameter_list|(
name|TestProcEnv
name|env
parameter_list|,
name|State
name|state
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
name|abort
parameter_list|(
name|TestProcEnv
name|env
parameter_list|)
block|{
name|aborted
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
block|}
end_class

end_unit

