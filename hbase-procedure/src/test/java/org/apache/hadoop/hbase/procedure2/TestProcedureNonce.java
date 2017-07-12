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
name|AtomicReference
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|security
operator|.
name|User
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
name|Bytes
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
name|NonceKey
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
name|TestProcedureNonce
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
name|TestProcedureNonce
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
literal|2
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
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testCompletedProcWithSameNonce
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|long
name|nonceGroup
init|=
literal|123
decl_stmt|;
specifier|final
name|long
name|nonce
init|=
literal|2222
decl_stmt|;
comment|// register the nonce
specifier|final
name|NonceKey
name|nonceKey
init|=
name|procExecutor
operator|.
name|createNonceKey
argument_list|(
name|nonceGroup
argument_list|,
name|nonce
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|procExecutor
operator|.
name|registerNonce
argument_list|(
name|nonceKey
argument_list|)
operator|>=
literal|0
argument_list|)
expr_stmt|;
comment|// Submit a proc and wait for its completion
name|Procedure
name|proc
init|=
operator|new
name|TestSingleStepProcedure
argument_list|()
decl_stmt|;
name|long
name|procId
init|=
name|procExecutor
operator|.
name|submitProcedure
argument_list|(
name|proc
argument_list|,
name|nonceKey
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
comment|// Restart
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
name|procId
argument_list|)
expr_stmt|;
comment|// try to register a procedure with the same nonce
comment|// we should get back the old procId
name|assertEquals
argument_list|(
name|procId
argument_list|,
name|procExecutor
operator|.
name|registerNonce
argument_list|(
name|nonceKey
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
name|ProcedureTestingUtility
operator|.
name|assertProcNotFailed
argument_list|(
name|result
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
name|testRunningProcWithSameNonce
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|long
name|nonceGroup
init|=
literal|456
decl_stmt|;
specifier|final
name|long
name|nonce
init|=
literal|33333
decl_stmt|;
comment|// register the nonce
specifier|final
name|NonceKey
name|nonceKey
init|=
name|procExecutor
operator|.
name|createNonceKey
argument_list|(
name|nonceGroup
argument_list|,
name|nonce
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|procExecutor
operator|.
name|registerNonce
argument_list|(
name|nonceKey
argument_list|)
operator|>=
literal|0
argument_list|)
expr_stmt|;
comment|// Submit a proc and use a latch to prevent the step execution until we submitted proc2
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|TestSingleStepProcedure
name|proc
init|=
operator|new
name|TestSingleStepProcedure
argument_list|()
decl_stmt|;
name|procEnv
operator|.
name|setWaitLatch
argument_list|(
name|latch
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
argument_list|,
name|nonceKey
argument_list|)
decl_stmt|;
while|while
condition|(
name|proc
operator|.
name|step
operator|!=
literal|1
condition|)
name|Threads
operator|.
name|sleep
argument_list|(
literal|25
argument_list|)
expr_stmt|;
comment|// try to register a procedure with the same nonce
comment|// we should get back the old procId
name|assertEquals
argument_list|(
name|procId
argument_list|,
name|procExecutor
operator|.
name|registerNonce
argument_list|(
name|nonceKey
argument_list|)
argument_list|)
expr_stmt|;
comment|// complete the procedure
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
comment|// Restart, the procedure is not completed yet
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
name|procId
argument_list|)
expr_stmt|;
comment|// try to register a procedure with the same nonce
comment|// we should get back the old procId
name|assertEquals
argument_list|(
name|procId
argument_list|,
name|procExecutor
operator|.
name|registerNonce
argument_list|(
name|nonceKey
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
name|ProcedureTestingUtility
operator|.
name|assertProcNotFailed
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetFailureResultForNonce
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|long
name|nonceGroup
init|=
literal|234
decl_stmt|;
specifier|final
name|long
name|nonce
init|=
literal|55555
decl_stmt|;
comment|// check and register the request nonce
specifier|final
name|NonceKey
name|nonceKey
init|=
name|procExecutor
operator|.
name|createNonceKey
argument_list|(
name|nonceGroup
argument_list|,
name|nonce
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|procExecutor
operator|.
name|registerNonce
argument_list|(
name|nonceKey
argument_list|)
operator|>=
literal|0
argument_list|)
expr_stmt|;
name|procExecutor
operator|.
name|setFailureResultForNonce
argument_list|(
name|nonceKey
argument_list|,
literal|"testProc"
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
argument_list|,
operator|new
name|IOException
argument_list|(
literal|"test failure"
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|long
name|procId
init|=
name|procExecutor
operator|.
name|registerNonce
argument_list|(
name|nonceKey
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
name|ProcedureTestingUtility
operator|.
name|assertProcFailed
argument_list|(
name|result
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
name|testConcurrentNonceRegistration
parameter_list|()
throws|throws
name|IOException
block|{
name|testConcurrentNonceRegistration
argument_list|(
literal|true
argument_list|,
literal|567
argument_list|,
literal|44444
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
name|testConcurrentNonceRegistrationWithRollback
parameter_list|()
throws|throws
name|IOException
block|{
name|testConcurrentNonceRegistration
argument_list|(
literal|false
argument_list|,
literal|890
argument_list|,
literal|55555
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testConcurrentNonceRegistration
parameter_list|(
specifier|final
name|boolean
name|submitProcedure
parameter_list|,
specifier|final
name|long
name|nonceGroup
parameter_list|,
specifier|final
name|long
name|nonce
parameter_list|)
throws|throws
name|IOException
block|{
comment|// register the nonce
specifier|final
name|NonceKey
name|nonceKey
init|=
name|procExecutor
operator|.
name|createNonceKey
argument_list|(
name|nonceGroup
argument_list|,
name|nonce
argument_list|)
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|Throwable
argument_list|>
name|t1Exception
init|=
operator|new
name|AtomicReference
argument_list|()
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|Throwable
argument_list|>
name|t2Exception
init|=
operator|new
name|AtomicReference
argument_list|()
decl_stmt|;
specifier|final
name|CountDownLatch
name|t1NonceRegisteredLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|t2BeforeNonceRegisteredLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|Thread
index|[]
name|threads
init|=
operator|new
name|Thread
index|[
literal|2
index|]
decl_stmt|;
name|threads
index|[
literal|0
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
try|try
block|{
comment|// release the nonce and wake t2
name|assertFalse
argument_list|(
literal|"unexpected already registered nonce"
argument_list|,
name|procExecutor
operator|.
name|registerNonce
argument_list|(
name|nonceKey
argument_list|)
operator|>=
literal|0
argument_list|)
expr_stmt|;
name|t1NonceRegisteredLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
comment|// hold the submission until t2 is registering the nonce
name|t2BeforeNonceRegisteredLatch
operator|.
name|await
argument_list|()
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
if|if
condition|(
name|submitProcedure
condition|)
block|{
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|TestSingleStepProcedure
name|proc
init|=
operator|new
name|TestSingleStepProcedure
argument_list|()
decl_stmt|;
name|procEnv
operator|.
name|setWaitLatch
argument_list|(
name|latch
argument_list|)
expr_stmt|;
name|procExecutor
operator|.
name|submitProcedure
argument_list|(
name|proc
argument_list|,
name|nonceKey
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
comment|// complete the procedure
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|procExecutor
operator|.
name|unregisterNonceIfProcedureWasNotSubmitted
argument_list|(
name|nonceKey
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|t1Exception
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|t1NonceRegisteredLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|t2BeforeNonceRegisteredLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
expr_stmt|;
name|threads
index|[
literal|1
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
try|try
block|{
comment|// wait until t1 has registered the nonce
name|t1NonceRegisteredLatch
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// register the nonce
name|t2BeforeNonceRegisteredLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
literal|"unexpected non registered nonce"
argument_list|,
name|procExecutor
operator|.
name|registerNonce
argument_list|(
name|nonceKey
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|t2Exception
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|t1NonceRegisteredLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|t2BeforeNonceRegisteredLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
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
name|threads
operator|.
name|length
condition|;
operator|++
name|i
control|)
name|threads
index|[
name|i
index|]
operator|.
name|start
argument_list|()
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
name|threads
operator|.
name|length
condition|;
operator|++
name|i
control|)
name|Threads
operator|.
name|shutdown
argument_list|(
name|threads
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitNoProcedureRunning
argument_list|(
name|procExecutor
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|t1Exception
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|t2Exception
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|TestSingleStepProcedure
extends|extends
name|SequentialProcedure
argument_list|<
name|TestProcEnv
argument_list|>
block|{
specifier|private
name|int
name|step
init|=
literal|0
decl_stmt|;
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
name|TestProcEnv
name|env
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|step
operator|++
expr_stmt|;
name|env
operator|.
name|waitOnLatch
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"execute procedure "
operator|+
name|this
operator|+
literal|" step="
operator|+
name|step
argument_list|)
expr_stmt|;
name|step
operator|++
expr_stmt|;
name|setResult
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|step
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
name|TestProcEnv
name|env
parameter_list|)
block|{ }
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
literal|true
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|TestProcEnv
block|{
specifier|private
name|CountDownLatch
name|latch
init|=
literal|null
decl_stmt|;
comment|/**      * set/unset a latch. every procedure execute() step will wait on the latch if any.      */
specifier|public
name|void
name|setWaitLatch
parameter_list|(
name|CountDownLatch
name|latch
parameter_list|)
block|{
name|this
operator|.
name|latch
operator|=
name|latch
expr_stmt|;
block|}
specifier|public
name|void
name|waitOnLatch
parameter_list|()
throws|throws
name|InterruptedException
block|{
if|if
condition|(
name|latch
operator|!=
literal|null
condition|)
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

