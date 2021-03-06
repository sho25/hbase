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
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|Exchanger
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
name|AfterClass
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
name|BeforeClass
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
name|Rule
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
name|junit
operator|.
name|rules
operator|.
name|TestName
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
name|TestForceUpdateProcedure
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
name|TestForceUpdateProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HBaseCommonTestingUtility
name|UTIL
init|=
operator|new
name|HBaseCommonTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|WALProcedureStore
name|STORE
decl_stmt|;
specifier|private
specifier|static
name|ProcedureExecutor
argument_list|<
name|Void
argument_list|>
name|EXEC
decl_stmt|;
specifier|private
specifier|static
name|Exchanger
argument_list|<
name|Boolean
argument_list|>
name|EXCHANGER
init|=
operator|new
name|Exchanger
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|int
name|WAL_COUNT
init|=
literal|5
decl_stmt|;
annotation|@
name|Rule
specifier|public
specifier|final
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|private
name|void
name|createStoreAndExecutor
parameter_list|()
throws|throws
name|IOException
block|{
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|CompletedProcedureCleaner
operator|.
name|CLEANER_INTERVAL_CONF_KEY
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|Path
name|logDir
init|=
name|UTIL
operator|.
name|getDataTestDir
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|STORE
operator|=
name|ProcedureTestingUtility
operator|.
name|createWalStore
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|logDir
argument_list|)
expr_stmt|;
name|STORE
operator|.
name|start
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|EXEC
operator|=
operator|new
name|ProcedureExecutor
argument_list|<
name|Void
argument_list|>
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|null
argument_list|,
name|STORE
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|initAndStartWorkers
argument_list|(
name|EXEC
argument_list|,
literal|1
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|IOException
block|{
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|WALProcedureStore
operator|.
name|WAL_COUNT_WARN_THRESHOLD_CONF_KEY
argument_list|,
name|WAL_COUNT
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|stopStoreAndExecutor
parameter_list|()
block|{
name|EXEC
operator|.
name|stop
argument_list|()
expr_stmt|;
name|STORE
operator|.
name|stop
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|EXEC
operator|=
literal|null
expr_stmt|;
name|STORE
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|IOException
block|{
name|UTIL
operator|.
name|cleanupTestDir
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
name|createStoreAndExecutor
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
name|stopStoreAndExecutor
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
specifier|final
class|class
name|WaitingProcedure
extends|extends
name|NoopProcedure
argument_list|<
name|Void
argument_list|>
block|{
annotation|@
name|Override
specifier|protected
name|Procedure
argument_list|<
name|Void
argument_list|>
index|[]
name|execute
parameter_list|(
name|Void
name|env
parameter_list|)
throws|throws
name|ProcedureYieldException
throws|,
name|ProcedureSuspendedException
throws|,
name|InterruptedException
block|{
name|EXCHANGER
operator|.
name|exchange
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
expr_stmt|;
name|setState
argument_list|(
name|ProcedureState
operator|.
name|WAITING_TIMEOUT
argument_list|)
expr_stmt|;
name|setTimeout
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ProcedureSuspendedException
argument_list|()
throw|;
block|}
block|}
specifier|public
specifier|static
specifier|final
class|class
name|ParentProcedure
extends|extends
name|NoopProcedure
argument_list|<
name|Void
argument_list|>
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
specifier|protected
name|Procedure
argument_list|<
name|Void
argument_list|>
index|[]
name|execute
parameter_list|(
name|Void
name|env
parameter_list|)
throws|throws
name|ProcedureYieldException
throws|,
name|ProcedureSuspendedException
throws|,
name|InterruptedException
block|{
return|return
operator|new
name|Procedure
index|[]
block|{
operator|new
name|NoopProcedure
argument_list|<>
argument_list|()
block|,
operator|new
name|WaitingProcedure
argument_list|()
block|}
return|;
block|}
block|}
specifier|public
specifier|static
specifier|final
class|class
name|ExchangeProcedure
extends|extends
name|NoopProcedure
argument_list|<
name|Void
argument_list|>
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
specifier|protected
name|Procedure
argument_list|<
name|Void
argument_list|>
index|[]
name|execute
parameter_list|(
name|Void
name|env
parameter_list|)
throws|throws
name|ProcedureYieldException
throws|,
name|ProcedureSuspendedException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|EXCHANGER
operator|.
name|exchange
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
condition|)
block|{
return|return
operator|new
name|Procedure
index|[]
block|{
name|this
block|}
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
specifier|public
specifier|static
specifier|final
class|class
name|NoopNoAckProcedure
extends|extends
name|NoopProcedure
argument_list|<
name|Void
argument_list|>
block|{
annotation|@
name|Override
specifier|protected
name|boolean
name|shouldWaitClientAck
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
specifier|public
name|void
name|testProcedureStuck
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|EXEC
operator|.
name|submitProcedure
argument_list|(
operator|new
name|ParentProcedure
argument_list|()
argument_list|)
expr_stmt|;
name|EXCHANGER
operator|.
name|exchange
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|10000
argument_list|,
parameter_list|()
lambda|->
name|EXEC
operator|.
name|getActiveExecutorCount
argument_list|()
operator|==
literal|0
argument_list|)
expr_stmt|;
comment|// The above operations are used to make sure that we have persist the states of the two
comment|// procedures.
name|long
name|procId
init|=
name|EXEC
operator|.
name|submitProcedure
argument_list|(
operator|new
name|ExchangeProcedure
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|STORE
operator|.
name|getActiveLogs
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
name|WAL_COUNT
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|STORE
operator|.
name|rollWriterForTesting
argument_list|()
argument_list|)
expr_stmt|;
comment|// The WaitinProcedure never gets updated so we can not delete the oldest wal file, so the
comment|// number of wal files will increase
name|assertEquals
argument_list|(
literal|2
operator|+
name|i
argument_list|,
name|STORE
operator|.
name|getActiveLogs
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|EXCHANGER
operator|.
name|exchange
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
name|STORE
operator|.
name|rollWriterForTesting
argument_list|()
expr_stmt|;
comment|// Finish the ExchangeProcedure
name|EXCHANGER
operator|.
name|exchange
argument_list|(
name|Boolean
operator|.
name|FALSE
argument_list|)
expr_stmt|;
comment|// Make sure that we can delete several wal files because we force update the state of
comment|// WaitingProcedure. Notice that the last closed wal files can not be deleted, as when rolling
comment|// the newest wal file does not have anything in it, and in the closed file we still have the
comment|// state for the ExchangeProcedure so it can not be deleted
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|10000
argument_list|,
parameter_list|()
lambda|->
name|STORE
operator|.
name|getActiveLogs
argument_list|()
operator|.
name|size
argument_list|()
operator|<=
literal|2
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|10000
argument_list|,
parameter_list|()
lambda|->
name|EXEC
operator|.
name|isFinished
argument_list|(
name|procId
argument_list|)
argument_list|)
expr_stmt|;
comment|// Make sure that after the force update we could still load the procedures
name|stopStoreAndExecutor
argument_list|()
expr_stmt|;
name|createStoreAndExecutor
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|,
name|Procedure
argument_list|<
name|Void
argument_list|>
argument_list|>
name|procMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|EXEC
operator|.
name|getActiveProceduresNoCopy
argument_list|()
operator|.
name|forEach
argument_list|(
name|p
lambda|->
name|procMap
operator|.
name|put
argument_list|(
name|p
operator|.
name|getClass
argument_list|()
argument_list|,
name|p
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|procMap
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|ParentProcedure
name|parentProc
init|=
operator|(
name|ParentProcedure
operator|)
name|procMap
operator|.
name|get
argument_list|(
name|ParentProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|ProcedureState
operator|.
name|WAITING
argument_list|,
name|parentProc
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|WaitingProcedure
name|waitingProc
init|=
operator|(
name|WaitingProcedure
operator|)
name|procMap
operator|.
name|get
argument_list|(
name|WaitingProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|ProcedureState
operator|.
name|WAITING_TIMEOUT
argument_list|,
name|waitingProc
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|NoopProcedure
argument_list|<
name|Void
argument_list|>
name|noopProc
init|=
operator|(
name|NoopProcedure
argument_list|<
name|Void
argument_list|>
operator|)
name|procMap
operator|.
name|get
argument_list|(
name|NoopProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|ProcedureState
operator|.
name|SUCCESS
argument_list|,
name|noopProc
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompletedProcedure
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
name|long
name|procId
init|=
name|EXEC
operator|.
name|submitProcedure
argument_list|(
operator|new
name|ExchangeProcedure
argument_list|()
argument_list|)
decl_stmt|;
name|EXCHANGER
operator|.
name|exchange
argument_list|(
name|Boolean
operator|.
name|FALSE
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|10000
argument_list|,
parameter_list|()
lambda|->
name|EXEC
operator|.
name|isFinished
argument_list|(
name|procId
argument_list|)
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
name|WAL_COUNT
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|STORE
operator|.
name|rollWriterForTesting
argument_list|()
argument_list|)
expr_stmt|;
comment|// The exchange procedure is completed but still not deleted yet so we can not delete the
comment|// oldest wal file
name|long
name|pid
init|=
name|EXEC
operator|.
name|submitProcedure
argument_list|(
operator|new
name|NoopNoAckProcedure
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
operator|+
name|i
argument_list|,
name|STORE
operator|.
name|getActiveLogs
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|10000
argument_list|,
parameter_list|()
lambda|->
name|EXEC
operator|.
name|isFinished
argument_list|(
name|pid
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Only the exchange procedure can not be deleted
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|10000
argument_list|,
parameter_list|()
lambda|->
name|EXEC
operator|.
name|getCompletedSize
argument_list|()
operator|==
literal|1
argument_list|)
expr_stmt|;
name|STORE
operator|.
name|rollWriterForTesting
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|10000
argument_list|,
parameter_list|()
lambda|->
name|STORE
operator|.
name|getActiveLogs
argument_list|()
operator|.
name|size
argument_list|()
operator|<=
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

