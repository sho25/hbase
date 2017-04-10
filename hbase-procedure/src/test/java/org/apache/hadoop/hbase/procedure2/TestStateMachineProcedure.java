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
name|ProcedureTestingUtility
operator|.
name|NoopProcedure
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
name|TestStateMachineProcedure
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
name|TestStateMachineProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Exception
name|TEST_FAILURE_EXCEPTION
init|=
operator|new
name|Exception
argument_list|(
literal|"test failure"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
specifier|final
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|other
condition|)
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|other
operator|instanceof
name|Exception
operator|)
condition|)
return|return
literal|false
return|;
comment|// we are going to serialize the exception in the test,
comment|// so the instance comparison will not match
return|return
name|getMessage
argument_list|()
operator|.
name|equals
argument_list|(
operator|(
operator|(
name|Exception
operator|)
name|other
operator|)
operator|.
name|getMessage
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|getMessage
argument_list|()
operator|.
name|hashCode
argument_list|()
return|;
block|}
block|}
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
name|ProcedureTestingUtility
operator|.
name|setKillAndToggleBeforeStoreUpdate
argument_list|(
name|procExecutor
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"expected executor to be running"
argument_list|,
name|procExecutor
operator|.
name|isRunning
argument_list|()
argument_list|)
expr_stmt|;
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
name|testChildOnLastStep
parameter_list|()
block|{
name|long
name|procId
init|=
name|procExecutor
operator|.
name|submitProcedure
argument_list|(
operator|new
name|TestSMProcedure
argument_list|()
argument_list|)
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExecutor
argument_list|,
name|procId
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|procExecutor
operator|.
name|getEnvironment
argument_list|()
operator|.
name|execCount
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|procExecutor
operator|.
name|getEnvironment
argument_list|()
operator|.
name|rollbackCount
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotFailed
argument_list|(
name|procExecutor
argument_list|,
name|procId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testChildOnLastStepDoubleExecution
parameter_list|()
throws|throws
name|Exception
block|{
name|ProcedureTestingUtility
operator|.
name|setKillAndToggleBeforeStoreUpdate
argument_list|(
name|procExecutor
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|long
name|procId
init|=
name|procExecutor
operator|.
name|submitProcedure
argument_list|(
operator|new
name|TestSMProcedure
argument_list|()
argument_list|)
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|testRecoveryAndDoubleExecution
argument_list|(
name|procExecutor
argument_list|,
name|procId
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|procExecutor
operator|.
name|getEnvironment
argument_list|()
operator|.
name|execCount
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|procExecutor
operator|.
name|getEnvironment
argument_list|()
operator|.
name|rollbackCount
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotFailed
argument_list|(
name|procExecutor
argument_list|,
name|procId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testChildOnLastStepWithRollback
parameter_list|()
block|{
name|procExecutor
operator|.
name|getEnvironment
argument_list|()
operator|.
name|triggerChildRollback
operator|=
literal|true
expr_stmt|;
name|long
name|procId
init|=
name|procExecutor
operator|.
name|submitProcedure
argument_list|(
operator|new
name|TestSMProcedure
argument_list|()
argument_list|)
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExecutor
argument_list|,
name|procId
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|procExecutor
operator|.
name|getEnvironment
argument_list|()
operator|.
name|execCount
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|procExecutor
operator|.
name|getEnvironment
argument_list|()
operator|.
name|rollbackCount
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|Throwable
name|cause
init|=
name|ProcedureTestingUtility
operator|.
name|assertProcFailed
argument_list|(
name|procExecutor
argument_list|,
name|procId
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|TEST_FAILURE_EXCEPTION
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testChildOnLastStepWithRollbackDoubleExecution
parameter_list|()
throws|throws
name|Exception
block|{
name|procExecutor
operator|.
name|getEnvironment
argument_list|()
operator|.
name|triggerChildRollback
operator|=
literal|true
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|setKillAndToggleBeforeStoreUpdate
argument_list|(
name|procExecutor
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|long
name|procId
init|=
name|procExecutor
operator|.
name|submitProcedure
argument_list|(
operator|new
name|TestSMProcedure
argument_list|()
argument_list|)
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|testRecoveryAndDoubleExecution
argument_list|(
name|procExecutor
argument_list|,
name|procId
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|procExecutor
operator|.
name|getEnvironment
argument_list|()
operator|.
name|execCount
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|procExecutor
operator|.
name|getEnvironment
argument_list|()
operator|.
name|rollbackCount
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|Throwable
name|cause
init|=
name|ProcedureTestingUtility
operator|.
name|assertProcFailed
argument_list|(
name|procExecutor
argument_list|,
name|procId
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|TEST_FAILURE_EXCEPTION
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
specifier|public
enum|enum
name|TestSMProcedureState
block|{
name|STEP_1
block|,
name|STEP_2
block|}
empty_stmt|;
specifier|public
specifier|static
class|class
name|TestSMProcedure
extends|extends
name|StateMachineProcedure
argument_list|<
name|TestProcEnv
argument_list|,
name|TestSMProcedureState
argument_list|>
block|{
specifier|protected
name|Flow
name|executeFromState
parameter_list|(
name|TestProcEnv
name|env
parameter_list|,
name|TestSMProcedureState
name|state
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"EXEC "
operator|+
name|state
operator|+
literal|" "
operator|+
name|this
argument_list|)
expr_stmt|;
name|env
operator|.
name|execCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|STEP_1
case|:
name|setNextState
argument_list|(
name|TestSMProcedureState
operator|.
name|STEP_2
argument_list|)
expr_stmt|;
break|break;
case|case
name|STEP_2
case|:
name|addChildProcedure
argument_list|(
operator|new
name|SimpleChildProcedure
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|NO_MORE_STATE
return|;
block|}
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
block|}
specifier|protected
name|void
name|rollbackState
parameter_list|(
name|TestProcEnv
name|env
parameter_list|,
name|TestSMProcedureState
name|state
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"ROLLBACK "
operator|+
name|state
operator|+
literal|" "
operator|+
name|this
argument_list|)
expr_stmt|;
name|env
operator|.
name|rollbackCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|TestSMProcedureState
name|getState
parameter_list|(
name|int
name|stateId
parameter_list|)
block|{
return|return
name|TestSMProcedureState
operator|.
name|values
argument_list|()
index|[
name|stateId
index|]
return|;
block|}
specifier|protected
name|int
name|getStateId
parameter_list|(
name|TestSMProcedureState
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
specifier|protected
name|TestSMProcedureState
name|getInitialState
parameter_list|()
block|{
return|return
name|TestSMProcedureState
operator|.
name|STEP_1
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|SimpleChildProcedure
extends|extends
name|NoopProcedure
argument_list|<
name|TestProcEnv
argument_list|>
block|{
specifier|protected
name|Procedure
index|[]
name|execute
parameter_list|(
name|TestProcEnv
name|env
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"EXEC "
operator|+
name|this
argument_list|)
expr_stmt|;
name|env
operator|.
name|execCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|triggerChildRollback
condition|)
block|{
name|setFailure
argument_list|(
literal|"test-failure"
argument_list|,
name|TEST_FAILURE_EXCEPTION
argument_list|)
expr_stmt|;
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
name|env
operator|.
name|rollbackCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
class|class
name|TestProcEnv
block|{
name|AtomicInteger
name|execCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|AtomicInteger
name|rollbackCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|boolean
name|triggerChildRollback
init|=
literal|false
decl_stmt|;
block|}
block|}
end_class

end_unit

