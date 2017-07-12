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
name|List
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ProcedureProtos
operator|.
name|ProcedureState
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
name|EnvironmentEdgeManager
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
name|TestProcedureExecution
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
name|TestProcedureExecution
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
name|Void
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
literal|null
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
specifier|private
specifier|static
class|class
name|TestProcedureException
extends|extends
name|IOException
block|{
specifier|public
name|TestProcedureException
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|super
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|TestSequentialProcedure
extends|extends
name|SequentialProcedure
argument_list|<
name|Void
argument_list|>
block|{
specifier|private
specifier|final
name|Procedure
index|[]
name|subProcs
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|state
decl_stmt|;
specifier|private
specifier|final
name|Exception
name|failure
decl_stmt|;
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|public
name|TestSequentialProcedure
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"recovery should not be triggered here"
argument_list|)
throw|;
block|}
specifier|public
name|TestSequentialProcedure
parameter_list|(
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|state
parameter_list|,
name|Procedure
modifier|...
name|subProcs
parameter_list|)
block|{
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
name|this
operator|.
name|subProcs
operator|=
name|subProcs
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|failure
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|TestSequentialProcedure
parameter_list|(
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|state
parameter_list|,
name|Exception
name|failure
parameter_list|)
block|{
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
name|this
operator|.
name|subProcs
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|failure
operator|=
name|failure
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Procedure
index|[]
name|execute
parameter_list|(
name|Void
name|env
parameter_list|)
block|{
name|state
operator|.
name|add
argument_list|(
name|name
operator|+
literal|"-execute"
argument_list|)
expr_stmt|;
if|if
condition|(
name|failure
operator|!=
literal|null
condition|)
block|{
name|setFailure
argument_list|(
operator|new
name|RemoteProcedureException
argument_list|(
name|name
operator|+
literal|"-failure"
argument_list|,
name|failure
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
name|subProcs
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|rollback
parameter_list|(
name|Void
name|env
parameter_list|)
block|{
name|state
operator|.
name|add
argument_list|(
name|name
operator|+
literal|"-rollback"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|abort
parameter_list|(
name|Void
name|env
parameter_list|)
block|{
name|state
operator|.
name|add
argument_list|(
name|name
operator|+
literal|"-abort"
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testBadSubprocList
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|state
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Procedure
name|subProc2
init|=
operator|new
name|TestSequentialProcedure
argument_list|(
literal|"subProc2"
argument_list|,
name|state
argument_list|)
decl_stmt|;
name|Procedure
name|subProc1
init|=
operator|new
name|TestSequentialProcedure
argument_list|(
literal|"subProc1"
argument_list|,
name|state
argument_list|,
name|subProc2
argument_list|,
name|NULL_PROC
argument_list|)
decl_stmt|;
name|Procedure
name|rootProc
init|=
operator|new
name|TestSequentialProcedure
argument_list|(
literal|"rootProc"
argument_list|,
name|state
argument_list|,
name|subProc1
argument_list|)
decl_stmt|;
name|long
name|rootId
init|=
name|ProcedureTestingUtility
operator|.
name|submitAndWait
argument_list|(
name|procExecutor
argument_list|,
name|rootProc
argument_list|)
decl_stmt|;
comment|// subProc1 has a "null" subprocedure which is catched as InvalidArgument
comment|// failed state with 2 execute and 2 rollback
name|LOG
operator|.
name|info
argument_list|(
name|state
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
name|rootId
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|state
operator|.
name|toString
argument_list|()
argument_list|,
name|result
operator|.
name|isFailed
argument_list|()
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertIsIllegalArgumentException
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|state
operator|.
name|toString
argument_list|()
argument_list|,
literal|4
argument_list|,
name|state
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"rootProc-execute"
argument_list|,
name|state
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"subProc1-execute"
argument_list|,
name|state
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"subProc1-rollback"
argument_list|,
name|state
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"rootProc-rollback"
argument_list|,
name|state
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testSingleSequentialProc
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|state
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Procedure
name|subProc2
init|=
operator|new
name|TestSequentialProcedure
argument_list|(
literal|"subProc2"
argument_list|,
name|state
argument_list|)
decl_stmt|;
name|Procedure
name|subProc1
init|=
operator|new
name|TestSequentialProcedure
argument_list|(
literal|"subProc1"
argument_list|,
name|state
argument_list|,
name|subProc2
argument_list|)
decl_stmt|;
name|Procedure
name|rootProc
init|=
operator|new
name|TestSequentialProcedure
argument_list|(
literal|"rootProc"
argument_list|,
name|state
argument_list|,
name|subProc1
argument_list|)
decl_stmt|;
name|long
name|rootId
init|=
name|ProcedureTestingUtility
operator|.
name|submitAndWait
argument_list|(
name|procExecutor
argument_list|,
name|rootProc
argument_list|)
decl_stmt|;
comment|// successful state, with 3 execute
name|LOG
operator|.
name|info
argument_list|(
name|state
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
name|rootId
argument_list|)
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotFailed
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|state
operator|.
name|toString
argument_list|()
argument_list|,
literal|3
argument_list|,
name|state
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testSingleSequentialProcRollback
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|state
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Procedure
name|subProc2
init|=
operator|new
name|TestSequentialProcedure
argument_list|(
literal|"subProc2"
argument_list|,
name|state
argument_list|,
operator|new
name|TestProcedureException
argument_list|(
literal|"fail test"
argument_list|)
argument_list|)
decl_stmt|;
name|Procedure
name|subProc1
init|=
operator|new
name|TestSequentialProcedure
argument_list|(
literal|"subProc1"
argument_list|,
name|state
argument_list|,
name|subProc2
argument_list|)
decl_stmt|;
name|Procedure
name|rootProc
init|=
operator|new
name|TestSequentialProcedure
argument_list|(
literal|"rootProc"
argument_list|,
name|state
argument_list|,
name|subProc1
argument_list|)
decl_stmt|;
name|long
name|rootId
init|=
name|ProcedureTestingUtility
operator|.
name|submitAndWait
argument_list|(
name|procExecutor
argument_list|,
name|rootProc
argument_list|)
decl_stmt|;
comment|// the 3rd proc fail, rollback after 2 successful execution
name|LOG
operator|.
name|info
argument_list|(
name|state
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
name|rootId
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|state
operator|.
name|toString
argument_list|()
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
name|Throwable
name|cause
init|=
name|ProcedureTestingUtility
operator|.
name|getExceptionCause
argument_list|(
name|result
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"expected TestProcedureException, got "
operator|+
name|cause
argument_list|,
name|cause
operator|instanceof
name|TestProcedureException
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|state
operator|.
name|toString
argument_list|()
argument_list|,
literal|6
argument_list|,
name|state
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"rootProc-execute"
argument_list|,
name|state
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"subProc1-execute"
argument_list|,
name|state
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"subProc2-execute"
argument_list|,
name|state
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"subProc2-rollback"
argument_list|,
name|state
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"subProc1-rollback"
argument_list|,
name|state
operator|.
name|get
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"rootProc-rollback"
argument_list|,
name|state
operator|.
name|get
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|TestFaultyRollback
extends|extends
name|SequentialProcedure
argument_list|<
name|Void
argument_list|>
block|{
specifier|private
name|int
name|retries
init|=
literal|0
decl_stmt|;
specifier|public
name|TestFaultyRollback
parameter_list|()
block|{ }
annotation|@
name|Override
specifier|protected
name|Procedure
index|[]
name|execute
parameter_list|(
name|Void
name|env
parameter_list|)
block|{
name|setFailure
argument_list|(
literal|"faulty-rollback-test"
argument_list|,
operator|new
name|TestProcedureException
argument_list|(
literal|"test faulty rollback"
argument_list|)
argument_list|)
expr_stmt|;
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
name|Void
name|env
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|++
name|retries
operator|<
literal|3
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"inject rollback failure "
operator|+
name|retries
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"injected failure number "
operator|+
name|retries
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"execute non faulty rollback step retries="
operator|+
name|retries
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|abort
parameter_list|(
name|Void
name|env
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testRollbackRetriableFailure
parameter_list|()
block|{
name|long
name|procId
init|=
name|ProcedureTestingUtility
operator|.
name|submitAndWait
argument_list|(
name|procExecutor
argument_list|,
operator|new
name|TestFaultyRollback
argument_list|()
argument_list|)
decl_stmt|;
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
name|assertTrue
argument_list|(
literal|"expected a failure"
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
name|Throwable
name|cause
init|=
name|ProcedureTestingUtility
operator|.
name|getExceptionCause
argument_list|(
name|result
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"expected TestProcedureException, got "
operator|+
name|cause
argument_list|,
name|cause
operator|instanceof
name|TestProcedureException
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|TestWaitingProcedure
extends|extends
name|SequentialProcedure
argument_list|<
name|Void
argument_list|>
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|state
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|hasChild
decl_stmt|;
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|public
name|TestWaitingProcedure
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"recovery should not be triggered here"
argument_list|)
throw|;
block|}
specifier|public
name|TestWaitingProcedure
parameter_list|(
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|state
parameter_list|,
name|boolean
name|hasChild
parameter_list|)
block|{
name|this
operator|.
name|hasChild
operator|=
name|hasChild
expr_stmt|;
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Procedure
index|[]
name|execute
parameter_list|(
name|Void
name|env
parameter_list|)
block|{
name|state
operator|.
name|add
argument_list|(
name|name
operator|+
literal|"-execute"
argument_list|)
expr_stmt|;
name|setState
argument_list|(
name|ProcedureState
operator|.
name|WAITING_TIMEOUT
argument_list|)
expr_stmt|;
return|return
name|hasChild
condition|?
operator|new
name|Procedure
index|[]
block|{
operator|new
name|TestWaitChild
argument_list|(
name|name
argument_list|,
name|state
argument_list|)
block|}
else|:
literal|null
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|rollback
parameter_list|(
name|Void
name|env
parameter_list|)
block|{
name|state
operator|.
name|add
argument_list|(
name|name
operator|+
literal|"-rollback"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|abort
parameter_list|(
name|Void
name|env
parameter_list|)
block|{
name|state
operator|.
name|add
argument_list|(
name|name
operator|+
literal|"-abort"
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|public
specifier|static
class|class
name|TestWaitChild
extends|extends
name|SequentialProcedure
argument_list|<
name|Void
argument_list|>
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|state
decl_stmt|;
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|public
name|TestWaitChild
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"recovery should not be triggered here"
argument_list|)
throw|;
block|}
specifier|public
name|TestWaitChild
parameter_list|(
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|state
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Procedure
index|[]
name|execute
parameter_list|(
name|Void
name|env
parameter_list|)
block|{
name|state
operator|.
name|add
argument_list|(
name|name
operator|+
literal|"-child-execute"
argument_list|)
expr_stmt|;
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
name|Void
name|env
parameter_list|)
block|{
name|state
operator|.
name|add
argument_list|(
name|name
operator|+
literal|"-child-rollback"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|abort
parameter_list|(
name|Void
name|env
parameter_list|)
block|{
name|state
operator|.
name|add
argument_list|(
name|name
operator|+
literal|"-child-abort"
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testAbortTimeout
parameter_list|()
block|{
specifier|final
name|int
name|PROC_TIMEOUT_MSEC
init|=
literal|2500
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|state
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Procedure
name|proc
init|=
operator|new
name|TestWaitingProcedure
argument_list|(
literal|"wproc"
argument_list|,
name|state
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|proc
operator|.
name|setTimeout
argument_list|(
name|PROC_TIMEOUT_MSEC
argument_list|)
expr_stmt|;
name|long
name|startTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|long
name|rootId
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
name|long
name|execTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|startTime
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|state
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"we didn't wait enough execTime="
operator|+
name|execTime
argument_list|,
name|execTime
operator|>=
name|PROC_TIMEOUT_MSEC
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
name|rootId
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|state
operator|.
name|toString
argument_list|()
argument_list|,
name|result
operator|.
name|isFailed
argument_list|()
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertIsTimeoutException
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|state
operator|.
name|toString
argument_list|()
argument_list|,
literal|2
argument_list|,
name|state
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"wproc-execute"
argument_list|,
name|state
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"wproc-rollback"
argument_list|,
name|state
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testAbortTimeoutWithChildren
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|state
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Procedure
name|proc
init|=
operator|new
name|TestWaitingProcedure
argument_list|(
literal|"wproc"
argument_list|,
name|state
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|proc
operator|.
name|setTimeout
argument_list|(
literal|2500
argument_list|)
expr_stmt|;
name|long
name|rootId
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
name|LOG
operator|.
name|info
argument_list|(
name|state
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
name|rootId
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|state
operator|.
name|toString
argument_list|()
argument_list|,
name|result
operator|.
name|isFailed
argument_list|()
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertIsTimeoutException
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|state
operator|.
name|toString
argument_list|()
argument_list|,
literal|4
argument_list|,
name|state
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"wproc-execute"
argument_list|,
name|state
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"wproc-child-execute"
argument_list|,
name|state
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"wproc-child-rollback"
argument_list|,
name|state
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"wproc-rollback"
argument_list|,
name|state
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

