begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|executor
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
name|*
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
name|io
operator|.
name|StringWriter
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
name|ThreadPoolExecutor
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
name|*
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
name|Waiter
operator|.
name|Predicate
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
name|executor
operator|.
name|ExecutorService
operator|.
name|Executor
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
name|executor
operator|.
name|ExecutorService
operator|.
name|ExecutorStatus
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
name|MiscTests
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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|*
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
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
name|TestExecutorService
block|{
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
name|TestExecutorService
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testExecutorService
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|maxThreads
init|=
literal|5
decl_stmt|;
name|int
name|maxTries
init|=
literal|10
decl_stmt|;
name|int
name|sleepInterval
init|=
literal|10
decl_stmt|;
name|Server
name|mockedServer
init|=
name|mock
argument_list|(
name|Server
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockedServer
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|)
expr_stmt|;
comment|// Start an executor service pool with max 5 threads
name|ExecutorService
name|executorService
init|=
operator|new
name|ExecutorService
argument_list|(
literal|"unit_test"
argument_list|)
decl_stmt|;
name|executorService
operator|.
name|startExecutorService
argument_list|(
name|ExecutorType
operator|.
name|MASTER_SERVER_OPERATIONS
argument_list|,
name|maxThreads
argument_list|)
expr_stmt|;
name|Executor
name|executor
init|=
name|executorService
operator|.
name|getExecutor
argument_list|(
name|ExecutorType
operator|.
name|MASTER_SERVER_OPERATIONS
argument_list|)
decl_stmt|;
name|ThreadPoolExecutor
name|pool
init|=
name|executor
operator|.
name|threadPoolExecutor
decl_stmt|;
comment|// Assert no threads yet
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|pool
operator|.
name|getPoolSize
argument_list|()
argument_list|)
expr_stmt|;
name|AtomicBoolean
name|lock
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|AtomicInteger
name|counter
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// Submit maxThreads executors.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|maxThreads
condition|;
name|i
operator|++
control|)
block|{
name|executorService
operator|.
name|submit
argument_list|(
operator|new
name|TestEventHandler
argument_list|(
name|mockedServer
argument_list|,
name|EventType
operator|.
name|M_SERVER_SHUTDOWN
argument_list|,
name|lock
argument_list|,
name|counter
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// The TestEventHandler will increment counter when it starts.
name|int
name|tries
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|counter
operator|.
name|get
argument_list|()
operator|<
name|maxThreads
operator|&&
name|tries
operator|<
name|maxTries
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for all event handlers to start..."
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepInterval
argument_list|)
expr_stmt|;
name|tries
operator|++
expr_stmt|;
block|}
comment|// Assert that pool is at max threads.
name|assertEquals
argument_list|(
name|maxThreads
argument_list|,
name|counter
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|maxThreads
argument_list|,
name|pool
operator|.
name|getPoolSize
argument_list|()
argument_list|)
expr_stmt|;
name|ExecutorStatus
name|status
init|=
name|executor
operator|.
name|getStatus
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|status
operator|.
name|queuedEvents
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|status
operator|.
name|running
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|checkStatusDump
argument_list|(
name|status
argument_list|)
expr_stmt|;
comment|// Now interrupt the running Executor
synchronized|synchronized
init|(
name|lock
init|)
block|{
name|lock
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|lock
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
comment|// Executor increments counter again on way out so.... test that happened.
while|while
condition|(
name|counter
operator|.
name|get
argument_list|()
operator|<
operator|(
name|maxThreads
operator|*
literal|2
operator|)
operator|&&
name|tries
operator|<
name|maxTries
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Waiting for all event handlers to finish..."
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepInterval
argument_list|)
expr_stmt|;
name|tries
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|maxThreads
operator|*
literal|2
argument_list|,
name|counter
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|maxThreads
argument_list|,
name|pool
operator|.
name|getPoolSize
argument_list|()
argument_list|)
expr_stmt|;
comment|// Add more than the number of threads items.
comment|// Make sure we don't get RejectedExecutionException.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
operator|(
literal|2
operator|*
name|maxThreads
operator|)
condition|;
name|i
operator|++
control|)
block|{
name|executorService
operator|.
name|submit
argument_list|(
operator|new
name|TestEventHandler
argument_list|(
name|mockedServer
argument_list|,
name|EventType
operator|.
name|M_SERVER_SHUTDOWN
argument_list|,
name|lock
argument_list|,
name|counter
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Now interrupt the running Executor
synchronized|synchronized
init|(
name|lock
init|)
block|{
name|lock
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|lock
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
comment|// Make sure threads are still around even after their timetolive expires.
name|Thread
operator|.
name|sleep
argument_list|(
name|ExecutorService
operator|.
name|Executor
operator|.
name|keepAliveTimeInMillis
operator|*
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|maxThreads
argument_list|,
name|pool
operator|.
name|getPoolSize
argument_list|()
argument_list|)
expr_stmt|;
name|executorService
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|executorService
operator|.
name|getAllExecutorStatuses
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Test that submit doesn't throw NPEs
name|executorService
operator|.
name|submit
argument_list|(
operator|new
name|TestEventHandler
argument_list|(
name|mockedServer
argument_list|,
name|EventType
operator|.
name|M_SERVER_SHUTDOWN
argument_list|,
name|lock
argument_list|,
name|counter
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkStatusDump
parameter_list|(
name|ExecutorStatus
name|status
parameter_list|)
throws|throws
name|IOException
block|{
name|StringWriter
name|sw
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|status
operator|.
name|dumpTo
argument_list|(
name|sw
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|String
name|dump
init|=
name|sw
operator|.
name|toString
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Got status dump:\n"
operator|+
name|dump
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dump
operator|.
name|contains
argument_list|(
literal|"Waiting on java.util.concurrent.atomic.AtomicBoolean"
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|TestEventHandler
extends|extends
name|EventHandler
block|{
specifier|private
specifier|final
name|AtomicBoolean
name|lock
decl_stmt|;
specifier|private
name|AtomicInteger
name|counter
decl_stmt|;
specifier|public
name|TestEventHandler
parameter_list|(
name|Server
name|server
parameter_list|,
name|EventType
name|eventType
parameter_list|,
name|AtomicBoolean
name|lock
parameter_list|,
name|AtomicInteger
name|counter
parameter_list|)
block|{
name|super
argument_list|(
name|server
argument_list|,
name|eventType
argument_list|)
expr_stmt|;
name|this
operator|.
name|lock
operator|=
name|lock
expr_stmt|;
name|this
operator|.
name|counter
operator|=
name|counter
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|num
init|=
name|counter
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Running process #"
operator|+
name|num
operator|+
literal|", threadName="
operator|+
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|lock
init|)
block|{
while|while
condition|(
name|lock
operator|.
name|get
argument_list|()
condition|)
block|{
try|try
block|{
name|lock
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// do nothing
block|}
block|}
block|}
name|counter
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAborting
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
specifier|final
name|Server
name|server
init|=
name|mock
argument_list|(
name|Server
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|server
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|ExecutorService
name|executorService
init|=
operator|new
name|ExecutorService
argument_list|(
literal|"unit_test"
argument_list|)
decl_stmt|;
name|executorService
operator|.
name|startExecutorService
argument_list|(
name|ExecutorType
operator|.
name|MASTER_SERVER_OPERATIONS
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|executorService
operator|.
name|submit
argument_list|(
operator|new
name|EventHandler
argument_list|(
name|server
argument_list|,
name|EventType
operator|.
name|M_SERVER_SHUTDOWN
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|()
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Should cause abort"
argument_list|)
throw|;
block|}
block|}
argument_list|)
expr_stmt|;
name|Waiter
operator|.
name|waitFor
argument_list|(
name|conf
argument_list|,
literal|30000
argument_list|,
operator|new
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|verify
argument_list|(
name|server
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|abort
argument_list|(
name|anyString
argument_list|()
argument_list|,
operator|(
name|Throwable
operator|)
name|anyObject
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|executorService
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

