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
name|Int32Value
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
name|TestProcedureEvents
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
name|TestProcedureEvents
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
name|TestProcedureEvents
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|TestProcEnv
name|procEnv
decl_stmt|;
specifier|private
name|ProcedureStore
name|procStore
decl_stmt|;
specifier|private
name|ProcedureExecutor
argument_list|<
name|TestProcEnv
argument_list|>
name|procExecutor
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
name|Path
name|testDir
init|=
name|htu
operator|.
name|getDataTestDir
argument_list|()
decl_stmt|;
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
literal|1
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
name|procExecutor
operator|.
name|join
argument_list|()
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
name|testTimeoutEventProcedure
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|NTIMEOUTS
init|=
literal|5
decl_stmt|;
name|TestTimeoutEventProcedure
name|proc
init|=
operator|new
name|TestTimeoutEventProcedure
argument_list|(
literal|500
argument_list|,
name|NTIMEOUTS
argument_list|)
decl_stmt|;
name|procExecutor
operator|.
name|submitProcedure
argument_list|(
name|proc
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExecutor
argument_list|,
name|proc
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertIsAbortException
argument_list|(
name|procExecutor
operator|.
name|getResult
argument_list|(
name|proc
operator|.
name|getProcId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|NTIMEOUTS
operator|+
literal|1
argument_list|,
name|proc
operator|.
name|getTimeoutsCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTimeoutEventProcedureDoubleExecution
parameter_list|()
throws|throws
name|Exception
block|{
name|testTimeoutEventProcedureDoubleExecution
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTimeoutEventProcedureDoubleExecutionKillIfSuspended
parameter_list|()
throws|throws
name|Exception
block|{
name|testTimeoutEventProcedureDoubleExecution
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testTimeoutEventProcedureDoubleExecution
parameter_list|(
specifier|final
name|boolean
name|killIfSuspended
parameter_list|)
throws|throws
name|Exception
block|{
name|TestTimeoutEventProcedure
name|proc
init|=
operator|new
name|TestTimeoutEventProcedure
argument_list|(
literal|1000
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|setKillAndToggleBeforeStoreUpdate
argument_list|(
name|procExecutor
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|setKillIfSuspended
argument_list|(
name|procExecutor
argument_list|,
name|killIfSuspended
argument_list|)
expr_stmt|;
name|long
name|procId
init|=
name|procExecutor
operator|.
name|submitProcedure
argument_list|(
name|proc
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
name|ProcedureTestingUtility
operator|.
name|assertIsAbortException
argument_list|(
name|procExecutor
operator|.
name|getResult
argument_list|(
name|proc
operator|.
name|getProcId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * This Event+Procedure exhibits following behavior:    *<ul>    *<li>On procedure execute()    *<ul>    *<li>If had enough timeouts, abort the procedure. Else....</li>    *<li>Suspend the event and add self to its suspend queue</li>    *<li>Go into waiting state</li>    *</ul>    *</li>    *<li>    *     On waiting timeout    *<ul>    *<li>Wake the event (which adds this procedure back into scheduler queue), and set own's    *       state to RUNNABLE (so can be executed again).</li>    *</ul>    *</li>    *</ul>    */
specifier|public
specifier|static
class|class
name|TestTimeoutEventProcedure
extends|extends
name|NoopProcedure
argument_list|<
name|TestProcEnv
argument_list|>
block|{
specifier|private
specifier|final
name|ProcedureEvent
name|event
init|=
operator|new
name|ProcedureEvent
argument_list|(
literal|"timeout-event"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicInteger
name|ntimeouts
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
name|int
name|maxTimeouts
init|=
literal|1
decl_stmt|;
specifier|public
name|TestTimeoutEventProcedure
parameter_list|()
block|{}
specifier|public
name|TestTimeoutEventProcedure
parameter_list|(
specifier|final
name|int
name|timeoutMsec
parameter_list|,
specifier|final
name|int
name|maxTimeouts
parameter_list|)
block|{
name|this
operator|.
name|maxTimeouts
operator|=
name|maxTimeouts
expr_stmt|;
name|setTimeout
argument_list|(
name|timeoutMsec
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|getTimeoutsCount
parameter_list|()
block|{
return|return
name|ntimeouts
operator|.
name|get
argument_list|()
return|;
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
literal|" ntimeouts="
operator|+
name|ntimeouts
argument_list|)
expr_stmt|;
if|if
condition|(
name|ntimeouts
operator|.
name|get
argument_list|()
operator|>
name|maxTimeouts
condition|)
block|{
name|setAbortFailure
argument_list|(
literal|"test"
argument_list|,
literal|"give up after "
operator|+
name|ntimeouts
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|event
operator|.
name|suspend
argument_list|()
expr_stmt|;
if|if
condition|(
name|event
operator|.
name|suspendIfNotReady
argument_list|(
name|this
argument_list|)
condition|)
block|{
name|setState
argument_list|(
name|ProcedureState
operator|.
name|WAITING_TIMEOUT
argument_list|)
expr_stmt|;
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
specifier|synchronized
name|boolean
name|setTimeoutFailure
parameter_list|(
specifier|final
name|TestProcEnv
name|env
parameter_list|)
block|{
name|int
name|n
init|=
name|ntimeouts
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"HANDLE TIMEOUT "
operator|+
name|this
operator|+
literal|" ntimeouts="
operator|+
name|n
argument_list|)
expr_stmt|;
name|setState
argument_list|(
name|ProcedureState
operator|.
name|RUNNABLE
argument_list|)
expr_stmt|;
name|event
operator|.
name|wake
argument_list|(
operator|(
name|AbstractProcedureScheduler
operator|)
name|env
operator|.
name|getProcedureScheduler
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|afterReplay
parameter_list|(
specifier|final
name|TestProcEnv
name|env
parameter_list|)
block|{
if|if
condition|(
name|getState
argument_list|()
operator|==
name|ProcedureState
operator|.
name|WAITING_TIMEOUT
condition|)
block|{
name|event
operator|.
name|suspend
argument_list|()
expr_stmt|;
name|event
operator|.
name|suspendIfNotReady
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
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
name|Int32Value
operator|.
name|Builder
name|ntimeoutsBuilder
init|=
name|Int32Value
operator|.
name|newBuilder
argument_list|()
operator|.
name|setValue
argument_list|(
name|ntimeouts
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|serializer
operator|.
name|serialize
argument_list|(
name|ntimeoutsBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|Int32Value
operator|.
name|Builder
name|maxTimeoutsBuilder
init|=
name|Int32Value
operator|.
name|newBuilder
argument_list|()
operator|.
name|setValue
argument_list|(
name|maxTimeouts
argument_list|)
decl_stmt|;
name|serializer
operator|.
name|serialize
argument_list|(
name|maxTimeoutsBuilder
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
name|Int32Value
name|ntimeoutsValue
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|Int32Value
operator|.
name|class
argument_list|)
decl_stmt|;
name|ntimeouts
operator|.
name|set
argument_list|(
name|ntimeoutsValue
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|Int32Value
name|maxTimeoutsValue
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|Int32Value
operator|.
name|class
argument_list|)
decl_stmt|;
name|maxTimeouts
operator|=
name|maxTimeoutsValue
operator|.
name|getValue
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
class|class
name|TestProcEnv
block|{
specifier|public
name|ProcedureScheduler
name|getProcedureScheduler
parameter_list|()
block|{
return|return
name|procExecutor
operator|.
name|getScheduler
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

