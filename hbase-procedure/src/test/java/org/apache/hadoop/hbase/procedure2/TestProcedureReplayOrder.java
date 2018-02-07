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
name|AtomicLong
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
name|procedure2
operator|.
name|store
operator|.
name|wal
operator|.
name|WALProcedureStore
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
name|LargeTests
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Int64Value
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
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestProcedureReplayOrder
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
name|TestProcedureReplayOrder
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
name|TestProcedureReplayOrder
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_THREADS
init|=
literal|16
decl_stmt|;
specifier|private
name|ProcedureExecutor
argument_list|<
name|Void
argument_list|>
name|procExecutor
decl_stmt|;
specifier|private
name|TestProcedureEnv
name|procEnv
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
name|htu
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|WALProcedureStore
operator|.
name|SYNC_WAIT_MSEC_CONF_KEY
argument_list|,
literal|25
argument_list|)
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
name|TestProcedureEnv
argument_list|()
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
name|procStore
operator|.
name|start
argument_list|(
name|NUM_THREADS
argument_list|)
expr_stmt|;
name|procExecutor
operator|.
name|start
argument_list|(
literal|1
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
name|testSingleStepReplayOrder
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|NUM_PROC_XTHREAD
init|=
literal|32
decl_stmt|;
specifier|final
name|int
name|NUM_PROCS
init|=
name|NUM_THREADS
operator|*
name|NUM_PROC_XTHREAD
decl_stmt|;
comment|// submit the procedures
name|submitProcedures
argument_list|(
name|NUM_THREADS
argument_list|,
name|NUM_PROC_XTHREAD
argument_list|,
name|TestSingleStepProcedure
operator|.
name|class
argument_list|)
expr_stmt|;
while|while
condition|(
name|procEnv
operator|.
name|getExecId
argument_list|()
operator|<
name|NUM_PROCS
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
comment|// restart the executor and allow the procedures to run
name|ProcedureTestingUtility
operator|.
name|restart
argument_list|(
name|procExecutor
argument_list|)
expr_stmt|;
comment|// wait the execution of all the procedures and
comment|// assert that the execution order was sorted by procId
name|ProcedureTestingUtility
operator|.
name|waitNoProcedureRunning
argument_list|(
name|procExecutor
argument_list|)
expr_stmt|;
name|procEnv
operator|.
name|assertSortedExecList
argument_list|(
name|NUM_PROCS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultiStepReplayOrder
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|NUM_PROC_XTHREAD
init|=
literal|24
decl_stmt|;
specifier|final
name|int
name|NUM_PROCS
init|=
name|NUM_THREADS
operator|*
operator|(
name|NUM_PROC_XTHREAD
operator|*
literal|2
operator|)
decl_stmt|;
comment|// submit the procedures
name|submitProcedures
argument_list|(
name|NUM_THREADS
argument_list|,
name|NUM_PROC_XTHREAD
argument_list|,
name|TestTwoStepProcedure
operator|.
name|class
argument_list|)
expr_stmt|;
while|while
condition|(
name|procEnv
operator|.
name|getExecId
argument_list|()
operator|<
name|NUM_PROCS
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
comment|// restart the executor and allow the procedures to run
name|ProcedureTestingUtility
operator|.
name|restart
argument_list|(
name|procExecutor
argument_list|)
expr_stmt|;
comment|// wait the execution of all the procedures and
comment|// assert that the execution order was sorted by procId
name|ProcedureTestingUtility
operator|.
name|waitNoProcedureRunning
argument_list|(
name|procExecutor
argument_list|)
expr_stmt|;
name|procEnv
operator|.
name|assertSortedExecList
argument_list|(
name|NUM_PROCS
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|submitProcedures
parameter_list|(
specifier|final
name|int
name|nthreads
parameter_list|,
specifier|final
name|int
name|nprocPerThread
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|procClazz
parameter_list|)
throws|throws
name|Exception
block|{
name|Thread
index|[]
name|submitThreads
init|=
operator|new
name|Thread
index|[
name|nthreads
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
name|submitThreads
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|submitThreads
index|[
name|i
index|]
operator|=
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
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|nprocPerThread
condition|;
operator|++
name|i
control|)
block|{
try|try
block|{
name|procExecutor
operator|.
name|submitProcedure
argument_list|(
operator|(
name|Procedure
operator|)
name|procClazz
operator|.
name|getDeclaredConstructor
argument_list|()
operator|.
name|newInstance
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"unable to instantiate the procedure"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"failure during the proc.newInstance(): "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|submitThreads
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|submitThreads
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|submitThreads
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|submitThreads
index|[
name|i
index|]
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|TestProcedureEnv
block|{
specifier|private
name|ArrayList
argument_list|<
name|TestProcedure
argument_list|>
name|execList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|AtomicLong
name|execTimestamp
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|public
name|long
name|getExecId
parameter_list|()
block|{
return|return
name|execTimestamp
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|long
name|nextExecId
parameter_list|()
block|{
return|return
name|execTimestamp
operator|.
name|incrementAndGet
argument_list|()
return|;
block|}
specifier|public
name|void
name|addToExecList
parameter_list|(
specifier|final
name|TestProcedure
name|proc
parameter_list|)
block|{
name|execList
operator|.
name|add
argument_list|(
name|proc
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|assertSortedExecList
parameter_list|(
name|int
name|numProcs
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|numProcs
argument_list|,
name|execList
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"EXEC LIST: "
operator|+
name|execList
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
name|execList
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|;
operator|++
name|i
control|)
block|{
name|TestProcedure
name|a
init|=
name|execList
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|TestProcedure
name|b
init|=
name|execList
operator|.
name|get
argument_list|(
name|i
operator|+
literal|1
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"exec list not sorted: "
operator|+
name|a
operator|+
literal|"< "
operator|+
name|b
argument_list|,
name|a
operator|.
name|getExecId
argument_list|()
operator|>
name|b
operator|.
name|getExecId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
specifier|abstract
class|class
name|TestProcedure
extends|extends
name|Procedure
argument_list|<
name|TestProcedureEnv
argument_list|>
block|{
specifier|protected
name|long
name|execId
init|=
literal|0
decl_stmt|;
specifier|protected
name|int
name|step
init|=
literal|0
decl_stmt|;
specifier|public
name|long
name|getExecId
parameter_list|()
block|{
return|return
name|execId
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|rollback
parameter_list|(
name|TestProcedureEnv
name|env
parameter_list|)
block|{ }
annotation|@
name|Override
specifier|protected
name|boolean
name|abort
parameter_list|(
name|TestProcedureEnv
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
name|void
name|serializeStateData
parameter_list|(
name|ProcedureStateSerializer
name|serializer
parameter_list|)
throws|throws
name|IOException
block|{
name|Int64Value
operator|.
name|Builder
name|builder
init|=
name|Int64Value
operator|.
name|newBuilder
argument_list|()
operator|.
name|setValue
argument_list|(
name|execId
argument_list|)
decl_stmt|;
name|serializer
operator|.
name|serialize
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|deserializeStateData
parameter_list|(
name|ProcedureStateSerializer
name|serializer
parameter_list|)
throws|throws
name|IOException
block|{
name|Int64Value
name|value
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|Int64Value
operator|.
name|class
argument_list|)
decl_stmt|;
name|execId
operator|=
name|value
operator|.
name|getValue
argument_list|()
expr_stmt|;
name|step
operator|=
literal|2
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|TestSingleStepProcedure
extends|extends
name|TestProcedure
block|{
specifier|public
name|TestSingleStepProcedure
parameter_list|()
block|{ }
annotation|@
name|Override
specifier|protected
name|Procedure
index|[]
name|execute
parameter_list|(
name|TestProcedureEnv
name|env
parameter_list|)
throws|throws
name|ProcedureYieldException
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"execute procedure step="
operator|+
name|step
operator|+
literal|": "
operator|+
name|this
argument_list|)
expr_stmt|;
if|if
condition|(
name|step
operator|==
literal|0
condition|)
block|{
name|step
operator|=
literal|1
expr_stmt|;
name|execId
operator|=
name|env
operator|.
name|nextExecId
argument_list|()
expr_stmt|;
return|return
operator|new
name|Procedure
index|[]
block|{
name|this
block|}
return|;
block|}
elseif|else
if|if
condition|(
name|step
operator|==
literal|2
condition|)
block|{
name|env
operator|.
name|addToExecList
argument_list|(
name|this
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
throw|throw
operator|new
name|ProcedureYieldException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"SingleStep(procId="
operator|+
name|getProcId
argument_list|()
operator|+
literal|" execId="
operator|+
name|execId
operator|+
literal|")"
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|TestTwoStepProcedure
extends|extends
name|TestProcedure
block|{
specifier|public
name|TestTwoStepProcedure
parameter_list|()
block|{ }
annotation|@
name|Override
specifier|protected
name|Procedure
index|[]
name|execute
parameter_list|(
name|TestProcedureEnv
name|env
parameter_list|)
throws|throws
name|ProcedureYieldException
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"execute procedure step="
operator|+
name|step
operator|+
literal|": "
operator|+
name|this
argument_list|)
expr_stmt|;
if|if
condition|(
name|step
operator|==
literal|0
condition|)
block|{
name|step
operator|=
literal|1
expr_stmt|;
name|execId
operator|=
name|env
operator|.
name|nextExecId
argument_list|()
expr_stmt|;
return|return
operator|new
name|Procedure
index|[]
block|{
operator|new
name|TestSingleStepProcedure
argument_list|()
block|}
return|;
block|}
elseif|else
if|if
condition|(
name|step
operator|==
literal|2
condition|)
block|{
name|env
operator|.
name|addToExecList
argument_list|(
name|this
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
throw|throw
operator|new
name|ProcedureYieldException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"TwoStep(procId="
operator|+
name|getProcId
argument_list|()
operator|+
literal|" execId="
operator|+
name|execId
operator|+
literal|")"
return|;
block|}
block|}
block|}
end_class

end_unit

