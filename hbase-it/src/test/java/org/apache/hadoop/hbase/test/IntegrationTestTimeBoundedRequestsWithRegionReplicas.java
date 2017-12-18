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
name|test
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
name|List
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
name|ScheduledExecutorService
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
name|ScheduledFuture
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
name|AtomicLong
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
name|lang3
operator|.
name|RandomUtils
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
name|HRegionLocation
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
name|IntegrationTestIngest
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
name|IntegrationTestingUtility
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
name|RegionLocations
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
name|IntegrationTests
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
name|chaos
operator|.
name|factories
operator|.
name|MonkeyFactory
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
name|client
operator|.
name|Admin
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
name|client
operator|.
name|ClusterConnection
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
name|client
operator|.
name|Consistency
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
name|client
operator|.
name|Get
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
name|client
operator|.
name|Result
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
name|client
operator|.
name|Table
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
name|regionserver
operator|.
name|StorefileRefresherChore
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
name|LoadTestTool
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
name|MultiThreadedReader
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|test
operator|.
name|LoadTestDataGenerator
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
name|util
operator|.
name|StringUtils
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
name|util
operator|.
name|ToolRunner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * An IntegrationTest for doing reads with a timeout, to a read-only table with region  * replicas. ChaosMonkey is run which kills the region servers and master, but ensures  * that meta region server is not killed, and at most 2 region servers are dead at any point  * in time. The expected behavior is that all reads with stale mode true will return  * before the timeout (5 sec by default). The test fails if the read requests does not finish  * in time.  *  *<p> This test uses LoadTestTool to read and write the data from a single client but  * multiple threads. The data is written first, then we allow the region replicas to catch  * up. Then we start the reader threads doing get requests with stale mode true. Chaos Monkey is  * started after some delay (20 sec by default) after the reader threads are started so that  * there is enough time to fully cache meta.  *  * These parameters (and some other parameters from LoadTestTool) can be used to  * control behavior, given values are default:  *<pre>  * -Dhbase.IntegrationTestTimeBoundedRequestsWithRegionReplicas.runtime=600000  * -DIntegrationTestTimeBoundedRequestsWithRegionReplicas.num_regions_per_server=5  * -DIntegrationTestTimeBoundedRequestsWithRegionReplicas.get_timeout_ms=5000  * -DIntegrationTestTimeBoundedRequestsWithRegionReplicas.num_keys_per_server=2500  * -DIntegrationTestTimeBoundedRequestsWithRegionReplicas.region_replication=3  * -DIntegrationTestTimeBoundedRequestsWithRegionReplicas.num_read_threads=20  * -DIntegrationTestTimeBoundedRequestsWithRegionReplicas.num_write_threads=20  * -DIntegrationTestTimeBoundedRequestsWithRegionReplicas.num_regions_per_server=5  * -DIntegrationTestTimeBoundedRequestsWithRegionReplicas.chaos_monkey_delay=20000  *</pre>  * Use this test with "serverKilling" ChaosMonkey. Sample usage:  *<pre>  * hbase org.apache.hadoop.hbase.test.IntegrationTestTimeBoundedRequestsWithRegionReplicas  * -Dhbase.IntegrationTestTimeBoundedRequestsWithRegionReplicas.runtime=600000  * -DIntegrationTestTimeBoundedRequestsWithRegionReplicas.num_write_threads=40  * -DIntegrationTestTimeBoundedRequestsWithRegionReplicas.num_read_threads=40  * -Dhbase.ipc.client.allowsInterrupt=true --monkey serverKilling  *</pre>  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|IntegrationTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|IntegrationTestTimeBoundedRequestsWithRegionReplicas
extends|extends
name|IntegrationTestIngest
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
name|IntegrationTestTimeBoundedRequestsWithRegionReplicas
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEST_NAME
init|=
name|IntegrationTestTimeBoundedRequestsWithRegionReplicas
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|long
name|DEFAULT_GET_TIMEOUT
init|=
literal|5000
decl_stmt|;
comment|// 5 sec
specifier|protected
specifier|static
specifier|final
name|String
name|GET_TIMEOUT_KEY
init|=
literal|"get_timeout_ms"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|long
name|DEFAUL_CHAOS_MONKEY_DELAY
init|=
literal|20
operator|*
literal|1000
decl_stmt|;
comment|// 20 sec
specifier|protected
specifier|static
specifier|final
name|String
name|CHAOS_MONKEY_DELAY_KEY
init|=
literal|"chaos_monkey_delay"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|DEFAULT_REGION_REPLICATION
init|=
literal|3
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|startMonkey
parameter_list|()
throws|throws
name|Exception
block|{
comment|// we do not want to start the monkey at the start of the test.
block|}
annotation|@
name|Override
specifier|protected
name|MonkeyFactory
name|getDefaultMonkeyFactory
parameter_list|()
block|{
return|return
name|MonkeyFactory
operator|.
name|getFactory
argument_list|(
name|MonkeyFactory
operator|.
name|CALM
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// default replication for this test is 3
name|String
name|clazz
init|=
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setIfUnset
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s.%s"
argument_list|,
name|clazz
argument_list|,
name|LoadTestTool
operator|.
name|OPT_REGION_REPLICATION
argument_list|)
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|DEFAULT_REGION_REPLICATION
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|writeData
parameter_list|(
name|int
name|colsPerKey
parameter_list|,
name|int
name|recordSize
parameter_list|,
name|int
name|writeThreads
parameter_list|,
name|long
name|startKey
parameter_list|,
name|long
name|numKeys
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|ret
init|=
name|loadTool
operator|.
name|run
argument_list|(
name|getArgsForLoadTestTool
argument_list|(
literal|"-write"
argument_list|,
name|String
operator|.
name|format
argument_list|(
literal|"%d:%d:%d"
argument_list|,
name|colsPerKey
argument_list|,
name|recordSize
argument_list|,
name|writeThreads
argument_list|)
argument_list|,
name|startKey
argument_list|,
name|numKeys
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
literal|0
operator|!=
name|ret
condition|)
block|{
name|String
name|errorMsg
init|=
literal|"Load failed with error code "
operator|+
name|ret
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|errorMsg
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
name|errorMsg
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|runIngestTest
parameter_list|(
name|long
name|defaultRunTime
parameter_list|,
name|long
name|keysPerServerPerIter
parameter_list|,
name|int
name|colsPerKey
parameter_list|,
name|int
name|recordSize
parameter_list|,
name|int
name|writeThreads
parameter_list|,
name|int
name|readThreads
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Cluster size:"
operator|+
name|util
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getServersSize
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|String
name|runtimeKey
init|=
name|String
operator|.
name|format
argument_list|(
name|RUN_TIME_KEY
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|runtime
init|=
name|util
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getLong
argument_list|(
name|runtimeKey
argument_list|,
name|defaultRunTime
argument_list|)
decl_stmt|;
name|long
name|startKey
init|=
literal|0
decl_stmt|;
name|long
name|numKeys
init|=
name|getNumKeys
argument_list|(
name|keysPerServerPerIter
argument_list|)
decl_stmt|;
comment|// write data once
name|LOG
operator|.
name|info
argument_list|(
literal|"Writing some data to the table"
argument_list|)
expr_stmt|;
name|writeData
argument_list|(
name|colsPerKey
argument_list|,
name|recordSize
argument_list|,
name|writeThreads
argument_list|,
name|startKey
argument_list|,
name|numKeys
argument_list|)
expr_stmt|;
comment|// flush the table
name|LOG
operator|.
name|info
argument_list|(
literal|"Flushing the table"
argument_list|)
expr_stmt|;
name|Admin
name|admin
init|=
name|util
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|getTablename
argument_list|()
argument_list|)
expr_stmt|;
comment|// re-open the regions to make sure that the replicas are up to date
name|long
name|refreshTime
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|StorefileRefresherChore
operator|.
name|REGIONSERVER_STOREFILE_REFRESH_PERIOD
argument_list|,
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|refreshTime
operator|>
literal|0
operator|&&
name|refreshTime
operator|<=
literal|10000
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Sleeping "
operator|+
name|refreshTime
operator|+
literal|"ms to ensure that the data is replicated"
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
name|refreshTime
operator|*
literal|3
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Reopening the table"
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|getTablename
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|getTablename
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// We should only start the ChaosMonkey after the readers are started and have cached
comment|// all of the region locations. Because the meta is not replicated, the timebounded reads
comment|// will timeout if meta server is killed.
comment|// We will start the chaos monkey after 1 minute, and since the readers are reading random
comment|// keys, it should be enough to cache every region entry.
name|long
name|chaosMonkeyDelay
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s.%s"
argument_list|,
name|TEST_NAME
argument_list|,
name|CHAOS_MONKEY_DELAY_KEY
argument_list|)
argument_list|,
name|DEFAUL_CHAOS_MONKEY_DELAY
argument_list|)
decl_stmt|;
name|ScheduledExecutorService
name|executorService
init|=
name|Executors
operator|.
name|newSingleThreadScheduledExecutor
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"ChaosMonkey delay is : %d seconds. Will start %s "
operator|+
literal|"ChaosMonkey after delay"
argument_list|,
name|chaosMonkeyDelay
operator|/
literal|1000
argument_list|,
name|monkeyToUse
argument_list|)
argument_list|)
expr_stmt|;
name|ScheduledFuture
argument_list|<
name|?
argument_list|>
name|result
init|=
name|executorService
operator|.
name|schedule
argument_list|(
operator|new
name|Runnable
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting ChaosMonkey"
argument_list|)
expr_stmt|;
name|monkey
operator|.
name|start
argument_list|()
expr_stmt|;
name|monkey
operator|.
name|waitForStop
argument_list|()
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
name|warn
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|,
name|chaosMonkeyDelay
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
comment|// set the intended run time for the reader. The reader will do read requests
comment|// to random keys for this amount of time.
name|long
name|remainingTime
init|=
name|runtime
operator|-
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|)
decl_stmt|;
if|if
condition|(
name|remainingTime
operator|<=
literal|0
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"The amount of time left for the test to perform random reads is "
operator|+
literal|"non-positive. Increase the test execution time via "
operator|+
name|String
operator|.
name|format
argument_list|(
name|RUN_TIME_KEY
argument_list|,
name|IntegrationTestTimeBoundedRequestsWithRegionReplicas
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
operator|+
literal|" or reduce the amount of data written per server via "
operator|+
name|IntegrationTestTimeBoundedRequestsWithRegionReplicas
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"."
operator|+
name|IntegrationTestIngest
operator|.
name|NUM_KEYS_PER_SERVER_KEY
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No time remains to execute random reads"
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Reading random keys from the table for "
operator|+
name|remainingTime
operator|/
literal|60000
operator|+
literal|" min"
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|setLong
argument_list|(
name|String
operator|.
name|format
argument_list|(
name|RUN_TIME_KEY
argument_list|,
name|TimeBoundedMultiThreadedReader
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
argument_list|,
name|remainingTime
argument_list|)
expr_stmt|;
comment|// load tool shares the same conf
comment|// now start the readers which will run for configured run time
try|try
block|{
name|int
name|ret
init|=
name|loadTool
operator|.
name|run
argument_list|(
name|getArgsForLoadTestTool
argument_list|(
literal|"-read"
argument_list|,
name|String
operator|.
name|format
argument_list|(
literal|"100:%d"
argument_list|,
name|readThreads
argument_list|)
argument_list|,
name|startKey
argument_list|,
name|numKeys
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
literal|0
operator|!=
name|ret
condition|)
block|{
name|String
name|errorMsg
init|=
literal|"Verification failed with error code "
operator|+
name|ret
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|errorMsg
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
name|errorMsg
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
name|result
operator|.
name|cancel
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|monkey
operator|.
name|stop
argument_list|(
literal|"Stopping the test"
argument_list|)
expr_stmt|;
name|monkey
operator|.
name|waitForStop
argument_list|()
expr_stmt|;
name|executorService
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|String
index|[]
name|getArgsForLoadTestTool
parameter_list|(
name|String
name|mode
parameter_list|,
name|String
name|modeSpecificArg
parameter_list|,
name|long
name|startKey
parameter_list|,
name|long
name|numKeys
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|args
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|super
operator|.
name|getArgsForLoadTestTool
argument_list|(
name|mode
argument_list|,
name|modeSpecificArg
argument_list|,
name|startKey
argument_list|,
name|numKeys
argument_list|)
argument_list|)
decl_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-reader"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|TimeBoundedMultiThreadedReader
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|args
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|args
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
specifier|public
specifier|static
class|class
name|TimeBoundedMultiThreadedReader
extends|extends
name|MultiThreadedReader
block|{
specifier|protected
name|long
name|timeoutNano
decl_stmt|;
specifier|protected
name|AtomicLong
name|timedOutReads
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|protected
name|long
name|runTime
decl_stmt|;
specifier|protected
name|Thread
name|timeoutThread
decl_stmt|;
specifier|protected
name|AtomicLong
name|staleReads
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|public
name|TimeBoundedMultiThreadedReader
parameter_list|(
name|LoadTestDataGenerator
name|dataGen
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|double
name|verifyPercent
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|dataGen
argument_list|,
name|conf
argument_list|,
name|tableName
argument_list|,
name|verifyPercent
argument_list|)
expr_stmt|;
name|long
name|timeoutMs
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s.%s"
argument_list|,
name|TEST_NAME
argument_list|,
name|GET_TIMEOUT_KEY
argument_list|)
argument_list|,
name|DEFAULT_GET_TIMEOUT
argument_list|)
decl_stmt|;
name|timeoutNano
operator|=
name|timeoutMs
operator|*
literal|1000000
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Timeout for gets: "
operator|+
name|timeoutMs
argument_list|)
expr_stmt|;
name|String
name|runTimeKey
init|=
name|String
operator|.
name|format
argument_list|(
name|RUN_TIME_KEY
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|runTime
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|runTimeKey
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|runTime
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Please configure "
operator|+
name|runTimeKey
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|waitForFinish
parameter_list|()
block|{
try|try
block|{
name|this
operator|.
name|timeoutThread
operator|.
name|join
argument_list|()
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
name|this
operator|.
name|aborted
operator|=
literal|true
expr_stmt|;
name|super
operator|.
name|waitForFinish
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|progressInfo
parameter_list|()
block|{
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|(
name|super
operator|.
name|progressInfo
argument_list|()
argument_list|)
decl_stmt|;
name|appendToStatus
argument_list|(
name|builder
argument_list|,
literal|"stale_reads"
argument_list|,
name|staleReads
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|appendToStatus
argument_list|(
name|builder
argument_list|,
literal|"get_timeouts"
argument_list|,
name|timedOutReads
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|long
name|startKey
parameter_list|,
name|long
name|endKey
parameter_list|,
name|int
name|numThreads
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|start
argument_list|(
name|startKey
argument_list|,
name|endKey
argument_list|,
name|numThreads
argument_list|)
expr_stmt|;
name|this
operator|.
name|timeoutThread
operator|=
operator|new
name|TimeoutThread
argument_list|(
name|this
operator|.
name|runTime
argument_list|)
expr_stmt|;
name|this
operator|.
name|timeoutThread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|HBaseReaderThread
name|createReaderThread
parameter_list|(
name|int
name|readerId
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|TimeBoundedMultiThreadedReaderThread
argument_list|(
name|readerId
argument_list|)
return|;
block|}
specifier|private
class|class
name|TimeoutThread
extends|extends
name|Thread
block|{
name|long
name|timeout
decl_stmt|;
name|long
name|reportInterval
init|=
literal|60000
decl_stmt|;
specifier|public
name|TimeoutThread
parameter_list|(
name|long
name|timeout
parameter_list|)
block|{
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|long
name|rem
init|=
name|Math
operator|.
name|min
argument_list|(
name|timeout
argument_list|,
name|reportInterval
argument_list|)
decl_stmt|;
if|if
condition|(
name|rem
operator|<=
literal|0
condition|)
block|{
break|break;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Remaining execution time:"
operator|+
name|timeout
operator|/
literal|60000
operator|+
literal|" min"
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
name|rem
argument_list|)
expr_stmt|;
name|timeout
operator|-=
name|rem
expr_stmt|;
block|}
block|}
block|}
specifier|public
class|class
name|TimeBoundedMultiThreadedReaderThread
extends|extends
name|MultiThreadedReader
operator|.
name|HBaseReaderThread
block|{
specifier|public
name|TimeBoundedMultiThreadedReaderThread
parameter_list|(
name|int
name|readerId
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|readerId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Get
name|createGet
parameter_list|(
name|long
name|keyToRead
parameter_list|)
throws|throws
name|IOException
block|{
name|Get
name|get
init|=
name|super
operator|.
name|createGet
argument_list|(
name|keyToRead
argument_list|)
decl_stmt|;
name|get
operator|.
name|setConsistency
argument_list|(
name|Consistency
operator|.
name|TIMELINE
argument_list|)
expr_stmt|;
return|return
name|get
return|;
block|}
annotation|@
name|Override
specifier|protected
name|long
name|getNextKeyToRead
parameter_list|()
block|{
comment|// always read a random key, assuming that the writer has finished writing all keys
name|long
name|key
init|=
name|startKey
operator|+
name|Math
operator|.
name|abs
argument_list|(
name|RandomUtils
operator|.
name|nextLong
argument_list|()
argument_list|)
operator|%
operator|(
name|endKey
operator|-
name|startKey
operator|)
decl_stmt|;
return|return
name|key
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|verifyResultsAndUpdateMetrics
parameter_list|(
name|boolean
name|verify
parameter_list|,
name|Get
index|[]
name|gets
parameter_list|,
name|long
name|elapsedNano
parameter_list|,
name|Result
index|[]
name|results
parameter_list|,
name|Table
name|table
parameter_list|,
name|boolean
name|isNullExpected
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|verifyResultsAndUpdateMetrics
argument_list|(
name|verify
argument_list|,
name|gets
argument_list|,
name|elapsedNano
argument_list|,
name|results
argument_list|,
name|table
argument_list|,
name|isNullExpected
argument_list|)
expr_stmt|;
for|for
control|(
name|Result
name|r
range|:
name|results
control|)
block|{
if|if
condition|(
name|r
operator|.
name|isStale
argument_list|()
condition|)
name|staleReads
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
comment|// we actually do not timeout and cancel the reads after timeout. We just wait for the RPC
comment|// to complete, but if the request took longer than timeout, we treat that as error.
if|if
condition|(
name|elapsedNano
operator|>
name|timeoutNano
condition|)
block|{
name|timedOutReads
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|numReadFailures
operator|.
name|addAndGet
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// fail the test
for|for
control|(
name|Result
name|r
range|:
name|results
control|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"FAILED FOR "
operator|+
name|r
argument_list|)
expr_stmt|;
name|RegionLocations
name|rl
init|=
operator|(
operator|(
name|ClusterConnection
operator|)
name|connection
operator|)
operator|.
name|locateRegion
argument_list|(
name|tableName
argument_list|,
name|r
operator|.
name|getRow
argument_list|()
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|HRegionLocation
name|locations
index|[]
init|=
name|rl
operator|.
name|getRegionLocations
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionLocation
name|h
range|:
name|locations
control|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"LOCATION "
operator|+
name|h
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|IntegrationTestingUtility
operator|.
name|setUseDistributedCluster
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|int
name|ret
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|conf
argument_list|,
operator|new
name|IntegrationTestTimeBoundedRequestsWithRegionReplicas
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|ret
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

