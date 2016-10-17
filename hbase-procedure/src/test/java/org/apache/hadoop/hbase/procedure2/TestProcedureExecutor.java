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
name|util
operator|.
name|concurrent
operator|.
name|Semaphore
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
name|TimeUnit
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
name|TestProcedureExecutor
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
name|TestProcedureExecutor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|TestProcEnv
name|procEnv
decl_stmt|;
specifier|private
name|NoopProcedureStore
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
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|htu
operator|=
operator|new
name|HBaseCommonTestingUtility
argument_list|()
expr_stmt|;
comment|// NOTE: The executor will be created by each test
name|procEnv
operator|=
operator|new
name|TestProcEnv
argument_list|()
expr_stmt|;
name|procStore
operator|=
operator|new
name|NoopProcedureStore
argument_list|()
expr_stmt|;
name|procStore
operator|.
name|start
argument_list|(
literal|1
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
name|Exception
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
block|}
specifier|private
name|void
name|createNewExecutor
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|int
name|numThreads
parameter_list|)
throws|throws
name|Exception
block|{
name|procExecutor
operator|=
operator|new
name|ProcedureExecutor
argument_list|(
name|conf
argument_list|,
name|procEnv
argument_list|,
name|procStore
argument_list|)
expr_stmt|;
name|procExecutor
operator|.
name|start
argument_list|(
name|numThreads
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
literal|60000
argument_list|)
specifier|public
name|void
name|testWorkerStuck
parameter_list|()
throws|throws
name|Exception
block|{
comment|// replace the executor
specifier|final
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|htu
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
literal|"hbase.procedure.worker.add.stuck.percentage"
argument_list|,
literal|0.5f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.procedure.worker.monitor.interval.msec"
argument_list|,
literal|500
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.procedure.worker.stuck.threshold.msec"
argument_list|,
literal|750
argument_list|)
expr_stmt|;
specifier|final
name|int
name|NUM_THREADS
init|=
literal|2
decl_stmt|;
name|createNewExecutor
argument_list|(
name|conf
argument_list|,
name|NUM_THREADS
argument_list|)
expr_stmt|;
name|Semaphore
name|latch1
init|=
operator|new
name|Semaphore
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|latch1
operator|.
name|acquire
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|BusyWaitProcedure
name|busyProc1
init|=
operator|new
name|BusyWaitProcedure
argument_list|(
name|latch1
argument_list|)
decl_stmt|;
name|Semaphore
name|latch2
init|=
operator|new
name|Semaphore
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|latch2
operator|.
name|acquire
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|BusyWaitProcedure
name|busyProc2
init|=
operator|new
name|BusyWaitProcedure
argument_list|(
name|latch2
argument_list|)
decl_stmt|;
name|long
name|busyProcId1
init|=
name|procExecutor
operator|.
name|submitProcedure
argument_list|(
name|busyProc1
argument_list|)
decl_stmt|;
name|long
name|busyProcId2
init|=
name|procExecutor
operator|.
name|submitProcedure
argument_list|(
name|busyProc2
argument_list|)
decl_stmt|;
name|long
name|otherProcId
init|=
name|procExecutor
operator|.
name|submitProcedure
argument_list|(
operator|new
name|NoopProcedure
argument_list|()
argument_list|)
decl_stmt|;
comment|// wait until a new worker is being created
name|int
name|threads1
init|=
name|waitThreadCount
argument_list|(
name|NUM_THREADS
operator|+
literal|1
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"new threads got created: "
operator|+
operator|(
name|threads1
operator|-
name|NUM_THREADS
operator|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|NUM_THREADS
operator|+
literal|1
argument_list|,
name|threads1
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExecutor
argument_list|,
name|otherProcId
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|procExecutor
operator|.
name|isFinished
argument_list|(
name|otherProcId
argument_list|)
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotFailed
argument_list|(
name|procExecutor
argument_list|,
name|otherProcId
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|procExecutor
operator|.
name|isRunning
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|procExecutor
operator|.
name|isFinished
argument_list|(
name|busyProcId1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|procExecutor
operator|.
name|isFinished
argument_list|(
name|busyProcId2
argument_list|)
argument_list|)
expr_stmt|;
comment|// terminate the busy procedures
name|latch1
operator|.
name|release
argument_list|()
expr_stmt|;
name|latch2
operator|.
name|release
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"set keep alive and wait threads being removed"
argument_list|)
expr_stmt|;
name|procExecutor
operator|.
name|setKeepAliveTime
argument_list|(
literal|500L
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|int
name|threads2
init|=
name|waitThreadCount
argument_list|(
name|NUM_THREADS
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"threads got removed: "
operator|+
operator|(
name|threads1
operator|-
name|threads2
operator|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|NUM_THREADS
argument_list|,
name|threads2
argument_list|)
expr_stmt|;
comment|// terminate the busy procedures
name|latch1
operator|.
name|release
argument_list|()
expr_stmt|;
name|latch2
operator|.
name|release
argument_list|()
expr_stmt|;
comment|// wait for all procs to complete
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExecutor
argument_list|,
name|busyProcId1
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExecutor
argument_list|,
name|busyProcId2
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotFailed
argument_list|(
name|procExecutor
argument_list|,
name|busyProcId1
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotFailed
argument_list|(
name|procExecutor
argument_list|,
name|busyProcId2
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|waitThreadCount
parameter_list|(
specifier|final
name|int
name|expectedThreads
parameter_list|)
block|{
while|while
condition|(
name|procExecutor
operator|.
name|isRunning
argument_list|()
condition|)
block|{
if|if
condition|(
name|procExecutor
operator|.
name|getWorkerThreadCount
argument_list|()
operator|==
name|expectedThreads
condition|)
block|{
break|break;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"waiting for thread count="
operator|+
name|expectedThreads
operator|+
literal|" current="
operator|+
name|procExecutor
operator|.
name|getWorkerThreadCount
argument_list|()
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleepWithoutInterrupt
argument_list|(
literal|250
argument_list|)
expr_stmt|;
block|}
return|return
name|procExecutor
operator|.
name|getWorkerThreadCount
argument_list|()
return|;
block|}
specifier|public
specifier|static
class|class
name|BusyWaitProcedure
extends|extends
name|NoopProcedure
argument_list|<
name|TestProcEnv
argument_list|>
block|{
specifier|private
specifier|final
name|Semaphore
name|latch
decl_stmt|;
specifier|public
name|BusyWaitProcedure
parameter_list|(
specifier|final
name|Semaphore
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
block|{
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"worker started "
operator|+
name|this
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|latch
operator|.
name|tryAcquire
argument_list|(
literal|1
argument_list|,
literal|30
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"waited too long"
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"worker step 2 "
operator|+
name|this
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|latch
operator|.
name|tryAcquire
argument_list|(
literal|1
argument_list|,
literal|30
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"waited too long"
argument_list|)
throw|;
block|}
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
literal|"got unexpected exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|setFailure
argument_list|(
literal|"BusyWaitProcedure"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
specifier|private
class|class
name|TestProcEnv
block|{ }
block|}
end_class

end_unit

