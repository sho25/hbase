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
name|TestChildProcedures
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
name|TestChildProcedures
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TestChildProcedures
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
name|TestProcEnv
name|procEnv
decl_stmt|;
specifier|private
specifier|static
name|ProcedureExecutor
argument_list|<
name|TestProcEnv
argument_list|>
name|procExecutor
decl_stmt|;
specifier|private
specifier|static
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
name|procEnv
operator|=
operator|new
name|TestProcEnv
argument_list|()
expr_stmt|;
name|procStore
operator|=
name|ProcedureTestingUtility
operator|.
name|createStore
argument_list|(
name|htu
operator|.
name|getConfiguration
argument_list|()
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
name|procEnv
argument_list|,
name|procStore
argument_list|)
expr_stmt|;
name|procExecutor
operator|.
name|testing
operator|=
operator|new
name|ProcedureExecutor
operator|.
name|Testing
argument_list|()
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
name|testChildLoad
parameter_list|()
throws|throws
name|Exception
block|{
name|procEnv
operator|.
name|toggleKillBeforeStoreUpdate
operator|=
literal|false
expr_stmt|;
name|TestRootProcedure
name|proc
init|=
operator|new
name|TestRootProcedure
argument_list|()
decl_stmt|;
name|long
name|procId
init|=
name|ProcedureTestingUtility
operator|.
name|submitAndWait
argument_list|(
name|procExecutor
argument_list|,
name|proc
argument_list|)
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|restart
argument_list|(
name|procExecutor
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExecutor
argument_list|,
name|proc
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"expected completed proc"
argument_list|,
name|procExecutor
operator|.
name|isFinished
argument_list|(
name|procId
argument_list|)
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
name|testChildLoadWithSteppedRestart
parameter_list|()
throws|throws
name|Exception
block|{
name|procEnv
operator|.
name|toggleKillBeforeStoreUpdate
operator|=
literal|true
expr_stmt|;
name|TestRootProcedure
name|proc
init|=
operator|new
name|TestRootProcedure
argument_list|()
decl_stmt|;
name|long
name|procId
init|=
name|ProcedureTestingUtility
operator|.
name|submitAndWait
argument_list|(
name|procExecutor
argument_list|,
name|proc
argument_list|)
decl_stmt|;
name|int
name|restartCount
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|!
name|procExecutor
operator|.
name|isFinished
argument_list|(
name|procId
argument_list|)
condition|)
block|{
name|ProcedureTestingUtility
operator|.
name|restart
argument_list|(
name|procExecutor
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExecutor
argument_list|,
name|proc
argument_list|)
expr_stmt|;
name|restartCount
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|restartCount
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"expected completed proc"
argument_list|,
name|procExecutor
operator|.
name|isFinished
argument_list|(
name|procId
argument_list|)
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
name|testChildRollbackLoad
parameter_list|()
throws|throws
name|Exception
block|{
name|procEnv
operator|.
name|toggleKillBeforeStoreUpdate
operator|=
literal|false
expr_stmt|;
name|procEnv
operator|.
name|triggerRollbackOnChild
operator|=
literal|true
expr_stmt|;
name|TestRootProcedure
name|proc
init|=
operator|new
name|TestRootProcedure
argument_list|()
decl_stmt|;
name|long
name|procId
init|=
name|ProcedureTestingUtility
operator|.
name|submitAndWait
argument_list|(
name|procExecutor
argument_list|,
name|proc
argument_list|)
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|restart
argument_list|(
name|procExecutor
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExecutor
argument_list|,
name|proc
argument_list|)
expr_stmt|;
name|assertProcFailed
argument_list|(
name|procId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testChildRollbackLoadWithSteppedRestart
parameter_list|()
throws|throws
name|Exception
block|{
name|procEnv
operator|.
name|toggleKillBeforeStoreUpdate
operator|=
literal|true
expr_stmt|;
name|procEnv
operator|.
name|triggerRollbackOnChild
operator|=
literal|true
expr_stmt|;
name|TestRootProcedure
name|proc
init|=
operator|new
name|TestRootProcedure
argument_list|()
decl_stmt|;
name|long
name|procId
init|=
name|ProcedureTestingUtility
operator|.
name|submitAndWait
argument_list|(
name|procExecutor
argument_list|,
name|proc
argument_list|)
decl_stmt|;
name|int
name|restartCount
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|!
name|procExecutor
operator|.
name|isFinished
argument_list|(
name|procId
argument_list|)
condition|)
block|{
name|ProcedureTestingUtility
operator|.
name|restart
argument_list|(
name|procExecutor
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExecutor
argument_list|,
name|proc
argument_list|)
expr_stmt|;
name|restartCount
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|restartCount
argument_list|)
expr_stmt|;
name|assertProcFailed
argument_list|(
name|procId
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertProcFailed
parameter_list|(
name|long
name|procId
parameter_list|)
block|{
name|assertTrue
argument_list|(
literal|"expected completed proc"
argument_list|,
name|procExecutor
operator|.
name|isFinished
argument_list|(
name|procId
argument_list|)
argument_list|)
expr_stmt|;
name|Procedure
argument_list|<
name|?
argument_list|>
name|result
init|=
name|procExecutor
operator|.
name|getResult
argument_list|(
name|procId
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|result
operator|.
name|isFailed
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|result
operator|.
name|getException
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|TestRootProcedure
extends|extends
name|SequentialProcedure
argument_list|<
name|TestProcEnv
argument_list|>
block|{
specifier|public
name|TestRootProcedure
parameter_list|()
block|{}
annotation|@
name|Override
specifier|public
name|Procedure
index|[]
name|execute
parameter_list|(
name|TestProcEnv
name|env
parameter_list|)
block|{
if|if
condition|(
name|env
operator|.
name|toggleKillBeforeStoreUpdate
condition|)
block|{
name|ProcedureTestingUtility
operator|.
name|toggleKillBeforeStoreUpdate
argument_list|(
name|procExecutor
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|Procedure
index|[]
block|{
operator|new
name|TestChildProcedure
argument_list|()
block|,
operator|new
name|TestChildProcedure
argument_list|()
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|rollback
parameter_list|(
name|TestProcEnv
name|env
parameter_list|)
block|{     }
annotation|@
name|Override
specifier|public
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
block|}
specifier|public
specifier|static
class|class
name|TestChildProcedure
extends|extends
name|SequentialProcedure
argument_list|<
name|TestProcEnv
argument_list|>
block|{
specifier|public
name|TestChildProcedure
parameter_list|()
block|{}
annotation|@
name|Override
specifier|public
name|Procedure
index|[]
name|execute
parameter_list|(
name|TestProcEnv
name|env
parameter_list|)
block|{
if|if
condition|(
name|env
operator|.
name|toggleKillBeforeStoreUpdate
condition|)
block|{
name|ProcedureTestingUtility
operator|.
name|toggleKillBeforeStoreUpdate
argument_list|(
name|procExecutor
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|env
operator|.
name|triggerRollbackOnChild
condition|)
block|{
name|setFailure
argument_list|(
literal|"test"
argument_list|,
operator|new
name|Exception
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|rollback
parameter_list|(
name|TestProcEnv
name|env
parameter_list|)
block|{     }
annotation|@
name|Override
specifier|public
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
block|}
specifier|private
specifier|static
class|class
name|TestProcEnv
block|{
specifier|public
name|boolean
name|toggleKillBeforeStoreUpdate
init|=
literal|false
decl_stmt|;
specifier|public
name|boolean
name|triggerRollbackOnChild
init|=
literal|false
decl_stmt|;
block|}
block|}
end_class

end_unit

