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
name|util
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
name|assertNull
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
name|util
operator|.
name|concurrent
operator|.
name|BlockingQueue
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
name|TimeUnit
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
name|TestStealJobQueue
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
name|TestStealJobQueue
operator|.
name|class
argument_list|)
decl_stmt|;
name|StealJobQueue
argument_list|<
name|Integer
argument_list|>
name|stealJobQueue
decl_stmt|;
name|BlockingQueue
argument_list|<
name|Integer
argument_list|>
name|stealFromQueue
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|stealJobQueue
operator|=
operator|new
name|StealJobQueue
argument_list|<>
argument_list|(
name|Integer
operator|::
name|compare
argument_list|)
expr_stmt|;
name|stealFromQueue
operator|=
name|stealJobQueue
operator|.
name|getStealFromQueue
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTake
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|stealJobQueue
operator|.
name|offer
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|stealFromQueue
operator|.
name|offer
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|stealJobQueue
operator|.
name|offer
argument_list|(
literal|15
argument_list|)
expr_stmt|;
name|stealJobQueue
operator|.
name|offer
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|stealJobQueue
operator|.
name|take
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|stealJobQueue
operator|.
name|take
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"always take from the main queue before trying to steal"
argument_list|,
literal|15
argument_list|,
name|stealJobQueue
operator|.
name|take
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|stealJobQueue
operator|.
name|take
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|stealFromQueue
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|stealJobQueue
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOfferInStealQueueFromShouldUnblock
parameter_list|()
throws|throws
name|InterruptedException
block|{
specifier|final
name|AtomicInteger
name|taken
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
name|Thread
name|consumer
init|=
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
name|Integer
name|n
init|=
name|stealJobQueue
operator|.
name|take
argument_list|()
decl_stmt|;
name|taken
operator|.
name|set
argument_list|(
name|n
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|consumer
operator|.
name|start
argument_list|()
expr_stmt|;
name|stealFromQueue
operator|.
name|offer
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|consumer
operator|.
name|join
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|taken
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|consumer
operator|.
name|interrupt
argument_list|()
expr_stmt|;
comment|//Ensure the consumer thread will stop.
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOfferInStealJobQueueShouldUnblock
parameter_list|()
throws|throws
name|InterruptedException
block|{
specifier|final
name|AtomicInteger
name|taken
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
name|Thread
name|consumer
init|=
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
name|Integer
name|n
init|=
name|stealJobQueue
operator|.
name|take
argument_list|()
decl_stmt|;
name|taken
operator|.
name|set
argument_list|(
name|n
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|consumer
operator|.
name|start
argument_list|()
expr_stmt|;
name|stealJobQueue
operator|.
name|offer
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|consumer
operator|.
name|join
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|taken
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|consumer
operator|.
name|interrupt
argument_list|()
expr_stmt|;
comment|//Ensure the consumer thread will stop.
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPoll
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|stealJobQueue
operator|.
name|offer
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|stealFromQueue
operator|.
name|offer
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|stealJobQueue
operator|.
name|offer
argument_list|(
literal|15
argument_list|)
expr_stmt|;
name|stealJobQueue
operator|.
name|offer
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|stealJobQueue
operator|.
name|poll
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|stealJobQueue
operator|.
name|poll
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"always take from the main queue before trying to steal"
argument_list|,
literal|15
argument_list|,
name|stealJobQueue
operator|.
name|poll
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|stealJobQueue
operator|.
name|poll
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|stealFromQueue
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|stealJobQueue
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|stealJobQueue
operator|.
name|poll
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPutInStealQueueFromShouldUnblockPoll
parameter_list|()
throws|throws
name|InterruptedException
block|{
specifier|final
name|AtomicInteger
name|taken
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
name|Thread
name|consumer
init|=
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
name|Integer
name|n
init|=
name|stealJobQueue
operator|.
name|poll
argument_list|(
literal|3
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|taken
operator|.
name|set
argument_list|(
name|n
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|consumer
operator|.
name|start
argument_list|()
expr_stmt|;
name|stealFromQueue
operator|.
name|put
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|consumer
operator|.
name|join
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|taken
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|consumer
operator|.
name|interrupt
argument_list|()
expr_stmt|;
comment|//Ensure the consumer thread will stop.
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAddInStealJobQueueShouldUnblockPoll
parameter_list|()
throws|throws
name|InterruptedException
block|{
specifier|final
name|AtomicInteger
name|taken
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
name|Thread
name|consumer
init|=
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
name|Integer
name|n
init|=
name|stealJobQueue
operator|.
name|poll
argument_list|(
literal|3
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|taken
operator|.
name|set
argument_list|(
name|n
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|consumer
operator|.
name|start
argument_list|()
expr_stmt|;
name|stealJobQueue
operator|.
name|add
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|consumer
operator|.
name|join
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|taken
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|consumer
operator|.
name|interrupt
argument_list|()
expr_stmt|;
comment|//Ensure the consumer thread will stop.
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInteractWithThreadPool
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|StealJobQueue
argument_list|<
name|Runnable
argument_list|>
name|stealTasksQueue
init|=
operator|new
name|StealJobQueue
argument_list|<>
argument_list|(
parameter_list|(
name|r1
parameter_list|,
name|r2
parameter_list|)
lambda|->
operator|(
operator|(
name|TestTask
operator|)
name|r1
operator|)
operator|.
name|compareTo
argument_list|(
operator|(
name|TestTask
operator|)
name|r2
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|stealJobCountDown
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|3
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|stealFromCountDown
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|ThreadPoolExecutor
name|stealPool
init|=
operator|new
name|ThreadPoolExecutor
argument_list|(
literal|3
argument_list|,
literal|3
argument_list|,
literal|1
argument_list|,
name|TimeUnit
operator|.
name|DAYS
argument_list|,
name|stealTasksQueue
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|afterExecute
parameter_list|(
name|Runnable
name|r
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
name|super
operator|.
name|afterExecute
argument_list|(
name|r
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|stealJobCountDown
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
comment|//This is necessary otherwise no worker will be running and stealing job
name|stealPool
operator|.
name|prestartAllCoreThreads
argument_list|()
expr_stmt|;
name|ThreadPoolExecutor
name|stealFromPool
init|=
operator|new
name|ThreadPoolExecutor
argument_list|(
literal|3
argument_list|,
literal|3
argument_list|,
literal|1
argument_list|,
name|TimeUnit
operator|.
name|DAYS
argument_list|,
name|stealTasksQueue
operator|.
name|getStealFromQueue
argument_list|()
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|afterExecute
parameter_list|(
name|Runnable
name|r
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
name|super
operator|.
name|afterExecute
argument_list|(
name|r
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|stealFromCountDown
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
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
literal|4
condition|;
name|i
operator|++
control|)
block|{
name|TestTask
name|task
init|=
operator|new
name|TestTask
argument_list|()
decl_stmt|;
name|stealFromPool
operator|.
name|execute
argument_list|(
name|task
argument_list|)
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
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|TestTask
name|task
init|=
operator|new
name|TestTask
argument_list|()
decl_stmt|;
name|stealPool
operator|.
name|execute
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
name|stealJobCountDown
operator|.
name|await
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|stealFromCountDown
operator|.
name|await
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|stealFromCountDown
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|stealJobCountDown
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
class|class
name|TestTask
extends|extends
name|Thread
implements|implements
name|Comparable
argument_list|<
name|TestTask
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|TestTask
name|o
parameter_list|)
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|200
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

