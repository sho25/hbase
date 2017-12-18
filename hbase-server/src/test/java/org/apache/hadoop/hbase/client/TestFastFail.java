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
name|client
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
name|java
operator|.
name|util
operator|.
name|Random
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
name|Callable
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
name|ExecutorService
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
name|Executors
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
name|Future
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
name|HBaseConfiguration
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
name|HBaseTestingUtility
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
name|HColumnDescriptor
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
name|HConstants
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
name|HTableDescriptor
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
name|ServerName
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
name|TableName
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
name|exceptions
operator|.
name|PreemptiveFastFailException
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
name|ipc
operator|.
name|RpcExecutor
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
name|ipc
operator|.
name|SimpleRpcScheduler
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
name|ClientTests
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
name|MediumTests
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
name|LoadTestKVGenerator
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
name|Ignore
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
name|MediumTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestFastFail
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
name|TestFastFail
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testFamily"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|int
name|SLAVES
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testQualifier"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|SLEEPTIME
init|=
literal|5000
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Just to prevent fastpath FIFO from picking calls up bypassing the queue.
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|RpcExecutor
operator|.
name|CALL_QUEUE_TYPE_CONF_KEY
argument_list|,
literal|"deadline"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|SLAVES
argument_list|)
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|MyPreemptiveFastFailInterceptor
operator|.
name|numBraveSouls
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|CallQueueTooBigPffeInterceptor
operator|.
name|numCallQueueTooBig
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Nothing to do.
block|}
annotation|@
name|Ignore
argument_list|(
literal|"Can go zombie -- see HBASE-14421; FIX"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testFastFail
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
specifier|final
name|String
name|tableName
init|=
name|name
operator|.
name|getMethodName
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaaa"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzzz"
argument_list|)
argument_list|,
literal|32
argument_list|)
expr_stmt|;
specifier|final
name|long
name|numRows
init|=
literal|1000
decl_stmt|;
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|,
name|SLEEPTIME
operator|*
literal|100
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_PAUSE
argument_list|,
name|SLEEPTIME
operator|/
literal|10
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_FAST_FAIL_MODE_ENABLED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_FAST_FAIL_THREASHOLD_MS
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setClass
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_FAST_FAIL_INTERCEPTOR_IMPL
argument_list|,
name|MyPreemptiveFastFailInterceptor
operator|.
name|class
argument_list|,
name|PreemptiveFastFailInterceptor
operator|.
name|class
argument_list|)
expr_stmt|;
specifier|final
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|/**      * Write numRows worth of data, so that the workers can arbitrarily read.      */
name|List
argument_list|<
name|Put
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numRows
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|rowKey
init|=
name|longToByteArrayKey
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|rowKey
decl_stmt|;
comment|// value is the same as the row key
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|Table
name|table
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
init|)
block|{
name|table
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Written all puts."
argument_list|)
expr_stmt|;
block|}
comment|/**      * The number of threads that are going to perform actions against the test      * table.      */
name|int
name|nThreads
init|=
literal|100
decl_stmt|;
name|ExecutorService
name|service
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|nThreads
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|continueOtherHalf
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|doneHalfway
init|=
operator|new
name|CountDownLatch
argument_list|(
name|nThreads
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|numSuccessfullThreads
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|numFailedThreads
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// The total time taken for the threads to perform the second put;
specifier|final
name|AtomicLong
name|totalTimeTaken
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|numBlockedWorkers
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|numPreemptiveFastFailExceptions
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Future
argument_list|<
name|Boolean
argument_list|>
argument_list|>
name|futures
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|nThreads
condition|;
name|i
operator|++
control|)
block|{
name|futures
operator|.
name|add
argument_list|(
name|service
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|Boolean
argument_list|>
argument_list|()
block|{
comment|/**          * The workers are going to perform a couple of reads. The second read          * will follow the killing of a regionserver so that we make sure that          * some of threads go into PreemptiveFastFailExcception          */
specifier|public
name|Boolean
name|call
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|Table
name|table
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
init|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|Math
operator|.
name|abs
argument_list|(
name|random
operator|.
name|nextInt
argument_list|()
argument_list|)
operator|%
literal|250
argument_list|)
expr_stmt|;
comment|// Add some jitter here
name|byte
index|[]
name|row
init|=
name|longToByteArrayKey
argument_list|(
name|Math
operator|.
name|abs
argument_list|(
name|random
operator|.
name|nextLong
argument_list|()
argument_list|)
operator|%
name|numRows
argument_list|)
decl_stmt|;
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|g
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
try|try
block|{
name|table
operator|.
name|get
argument_list|(
name|g
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
name|debug
argument_list|(
literal|"Get failed : "
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|doneHalfway
operator|.
name|countDown
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// Done with one get, proceeding to do the next one.
name|doneHalfway
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|continueOtherHalf
operator|.
name|await
argument_list|()
expr_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|g
operator|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|g
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
try|try
block|{
name|table
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
comment|// The get was successful
name|numSuccessfullThreads
operator|.
name|addAndGet
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|PreemptiveFastFailException
condition|)
block|{
comment|// We were issued a PreemptiveFastFailException
name|numPreemptiveFastFailExceptions
operator|.
name|addAndGet
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// Irrespective of PFFE, the request failed.
name|numFailedThreads
operator|.
name|addAndGet
argument_list|(
literal|1
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
finally|finally
block|{
name|long
name|enTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|totalTimeTaken
operator|.
name|addAndGet
argument_list|(
name|enTime
operator|-
name|startTime
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|enTime
operator|-
name|startTime
operator|)
operator|>=
name|SLEEPTIME
condition|)
block|{
comment|// Considering the slow workers as the blockedWorkers.
comment|// This assumes that the threads go full throttle at performing
comment|// actions. In case the thread scheduling itself is as slow as
comment|// SLEEPTIME, then this test might fail and so, we might have
comment|// set it to a higher number on slower machines.
name|numBlockedWorkers
operator|.
name|addAndGet
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
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
literal|"Caught unknown exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|doneHalfway
operator|.
name|countDown
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|doneHalfway
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// Kill a regionserver
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|getRpcServer
argument_list|()
operator|.
name|stop
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|stop
argument_list|(
literal|"Testing"
argument_list|)
expr_stmt|;
comment|// Let the threads continue going
name|continueOtherHalf
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|2
operator|*
name|SLEEPTIME
argument_list|)
expr_stmt|;
comment|// Start a RS in the cluster
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|startRegionServer
argument_list|()
expr_stmt|;
name|int
name|numThreadsReturnedFalse
init|=
literal|0
decl_stmt|;
name|int
name|numThreadsReturnedTrue
init|=
literal|0
decl_stmt|;
name|int
name|numThreadsThrewExceptions
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Future
argument_list|<
name|Boolean
argument_list|>
name|f
range|:
name|futures
control|)
block|{
try|try
block|{
name|numThreadsReturnedTrue
operator|+=
name|f
operator|.
name|get
argument_list|()
condition|?
literal|1
else|:
literal|0
expr_stmt|;
name|numThreadsReturnedFalse
operator|+=
name|f
operator|.
name|get
argument_list|()
condition|?
literal|0
else|:
literal|1
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|numThreadsThrewExceptions
operator|++
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"numThreadsReturnedFalse:"
operator|+
name|numThreadsReturnedFalse
operator|+
literal|" numThreadsReturnedTrue:"
operator|+
name|numThreadsReturnedTrue
operator|+
literal|" numThreadsThrewExceptions:"
operator|+
name|numThreadsThrewExceptions
operator|+
literal|" numFailedThreads:"
operator|+
name|numFailedThreads
operator|.
name|get
argument_list|()
operator|+
literal|" numSuccessfullThreads:"
operator|+
name|numSuccessfullThreads
operator|.
name|get
argument_list|()
operator|+
literal|" numBlockedWorkers:"
operator|+
name|numBlockedWorkers
operator|.
name|get
argument_list|()
operator|+
literal|" totalTimeWaited: "
operator|+
name|totalTimeTaken
operator|.
name|get
argument_list|()
operator|/
operator|(
name|numBlockedWorkers
operator|.
name|get
argument_list|()
operator|==
literal|0
condition|?
name|Long
operator|.
name|MAX_VALUE
else|:
name|numBlockedWorkers
operator|.
name|get
argument_list|()
operator|)
operator|+
literal|" numPFFEs: "
operator|+
name|numPreemptiveFastFailExceptions
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"The expected number of all the successfull and the failed "
operator|+
literal|"threads should equal the total number of threads that we spawned"
argument_list|,
name|nThreads
argument_list|,
name|numFailedThreads
operator|.
name|get
argument_list|()
operator|+
name|numSuccessfullThreads
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"All the failures should be coming from the secondput failure"
argument_list|,
name|numFailedThreads
operator|.
name|get
argument_list|()
argument_list|,
name|numThreadsReturnedFalse
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Number of threads that threw execution exceptions "
operator|+
literal|"otherwise should be 0"
argument_list|,
name|numThreadsThrewExceptions
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"The regionservers that returned true should equal to the"
operator|+
literal|" number of successful threads"
argument_list|,
name|numThreadsReturnedTrue
argument_list|,
name|numSuccessfullThreads
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"There will be atleast one thread that retried instead of failing"
argument_list|,
name|MyPreemptiveFastFailInterceptor
operator|.
name|numBraveSouls
operator|.
name|get
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"There will be atleast one PreemptiveFastFail exception,"
operator|+
literal|" otherwise, the test makes little sense."
operator|+
literal|"numPreemptiveFastFailExceptions: "
operator|+
name|numPreemptiveFastFailExceptions
operator|.
name|get
argument_list|()
argument_list|,
name|numPreemptiveFastFailExceptions
operator|.
name|get
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Only few thread should ideally be waiting for the dead "
operator|+
literal|"regionserver to be coming back. numBlockedWorkers:"
operator|+
name|numBlockedWorkers
operator|.
name|get
argument_list|()
operator|+
literal|" threads that retried : "
operator|+
name|MyPreemptiveFastFailInterceptor
operator|.
name|numBraveSouls
operator|.
name|get
argument_list|()
argument_list|,
name|numBlockedWorkers
operator|.
name|get
argument_list|()
operator|<=
name|MyPreemptiveFastFailInterceptor
operator|.
name|numBraveSouls
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCallQueueTooBigExceptionDoesntTriggerPffe
parameter_list|()
throws|throws
name|Exception
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
specifier|final
name|String
name|tableName
init|=
name|name
operator|.
name|getMethodName
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaaa"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzzz"
argument_list|)
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_PAUSE
argument_list|,
literal|500
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_FAST_FAIL_MODE_ENABLED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_FAST_FAIL_THREASHOLD_MS
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setClass
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_FAST_FAIL_INTERCEPTOR_IMPL
argument_list|,
name|CallQueueTooBigPffeInterceptor
operator|.
name|class
argument_list|,
name|PreemptiveFastFailInterceptor
operator|.
name|class
argument_list|)
expr_stmt|;
specifier|final
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|//Set max call queues size to 0
name|SimpleRpcScheduler
name|srs
init|=
operator|(
name|SimpleRpcScheduler
operator|)
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|getRpcServer
argument_list|()
operator|.
name|getScheduler
argument_list|()
decl_stmt|;
name|Configuration
name|newConf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|newConf
operator|.
name|setInt
argument_list|(
literal|"hbase.ipc.server.max.callqueue.length"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|srs
operator|.
name|onConfigurationChange
argument_list|(
name|newConf
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
init|)
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
operator|new
name|byte
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|ex
parameter_list|)
block|{     }
name|assertEquals
argument_list|(
literal|"We should have not entered PFFE mode on CQTBE, but we did;"
operator|+
literal|" number of times this mode should have been entered:"
argument_list|,
literal|0
argument_list|,
name|CallQueueTooBigPffeInterceptor
operator|.
name|numCallQueueTooBig
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|newConf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|newConf
operator|.
name|setInt
argument_list|(
literal|"hbase.ipc.server.max.callqueue.length"
argument_list|,
literal|250
argument_list|)
expr_stmt|;
name|srs
operator|.
name|onConfigurationChange
argument_list|(
name|newConf
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|MyPreemptiveFastFailInterceptor
extends|extends
name|PreemptiveFastFailInterceptor
block|{
specifier|public
specifier|static
name|AtomicInteger
name|numBraveSouls
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|boolean
name|shouldRetryInspiteOfFastFail
parameter_list|(
name|FailureInfo
name|fInfo
parameter_list|)
block|{
name|boolean
name|ret
init|=
name|super
operator|.
name|shouldRetryInspiteOfFastFail
argument_list|(
name|fInfo
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
condition|)
name|numBraveSouls
operator|.
name|addAndGet
argument_list|(
literal|1
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
specifier|public
name|MyPreemptiveFastFailInterceptor
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|byte
index|[]
name|longToByteArrayKey
parameter_list|(
name|long
name|rowKey
parameter_list|)
block|{
return|return
name|LoadTestKVGenerator
operator|.
name|md5PrefixedKey
argument_list|(
name|rowKey
argument_list|)
operator|.
name|getBytes
argument_list|()
return|;
block|}
specifier|public
specifier|static
class|class
name|CallQueueTooBigPffeInterceptor
extends|extends
name|PreemptiveFastFailInterceptor
block|{
specifier|public
specifier|static
name|AtomicInteger
name|numCallQueueTooBig
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|handleFailureToServer
parameter_list|(
name|ServerName
name|serverName
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
name|super
operator|.
name|handleFailureToServer
argument_list|(
name|serverName
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|numCallQueueTooBig
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
specifier|public
name|CallQueueTooBigPffeInterceptor
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

