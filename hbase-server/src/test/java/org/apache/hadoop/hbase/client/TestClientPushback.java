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
name|client
operator|.
name|AsyncProcessTask
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
name|backoff
operator|.
name|ClientBackoffPolicy
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
name|backoff
operator|.
name|ExponentialClientBackoffPolicy
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
name|backoff
operator|.
name|ServerStatistics
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
name|coprocessor
operator|.
name|Batch
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
name|HRegion
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
name|HRegionServer
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
name|MemstoreSize
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
name|Region
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
name|EnvironmentEdgeManager
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
name|BeforeClass
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|MetricsConnection
operator|.
name|CLIENT_SIDE_METRICS_ENABLED_KEY
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
name|assertNotEquals
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
name|assertNotNull
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

begin_comment
comment|/**  * Test that we can actually send and use region metrics to slowdown client writes  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestClientPushback
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
name|TestClientPushback
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"client-pushback"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|flushSizeBytes
init|=
literal|256
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// enable backpressure
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|ENABLE_CLIENT_BACKPRESSURE
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// use the exponential backoff policy
name|conf
operator|.
name|setClass
argument_list|(
name|ClientBackoffPolicy
operator|.
name|BACKOFF_POLICY_CLASS
argument_list|,
name|ExponentialClientBackoffPolicy
operator|.
name|class
argument_list|,
name|ClientBackoffPolicy
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// turn the memstore size way down so we don't need to write a lot to see changes in memstore
comment|// load
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|HREGION_MEMSTORE_FLUSH_SIZE
argument_list|,
name|flushSizeBytes
argument_list|)
expr_stmt|;
comment|// ensure we block the flushes when we are double that flushsize
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|HREGION_MEMSTORE_BLOCK_MULTIPLIER
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HREGION_MEMSTORE_BLOCK_MULTIPLIER
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|CLIENT_SIDE_METRICS_ENABLED_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|family
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|teardownCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
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
name|testClientTracksServerPushback
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|ClusterConnection
name|conn
init|=
operator|(
name|ClusterConnection
operator|)
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|BufferedMutatorImpl
name|mutator
init|=
operator|(
name|BufferedMutatorImpl
operator|)
name|conn
operator|.
name|getBufferedMutator
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HRegionServer
name|rs
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Region
name|region
init|=
name|rs
operator|.
name|getOnlineRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Writing some data to "
operator|+
name|tableName
argument_list|)
expr_stmt|;
comment|// write some data
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|mutator
operator|.
name|mutate
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|mutator
operator|.
name|flush
argument_list|()
expr_stmt|;
comment|// get the current load on RS. Hopefully memstore isn't flushed since we wrote the the data
name|int
name|load
init|=
call|(
name|int
call|)
argument_list|(
operator|(
operator|(
operator|(
name|HRegion
operator|)
name|region
operator|)
operator|.
name|addAndGetMemstoreSize
argument_list|(
operator|new
name|MemstoreSize
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|)
argument_list|)
operator|*
literal|100
operator|)
operator|/
name|flushSizeBytes
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Done writing some data to "
operator|+
name|tableName
argument_list|)
expr_stmt|;
comment|// get the stats for the region hosting our table
name|ClientBackoffPolicy
name|backoffPolicy
init|=
name|conn
operator|.
name|getBackoffPolicy
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Backoff policy is not correctly configured"
argument_list|,
name|backoffPolicy
operator|instanceof
name|ExponentialClientBackoffPolicy
argument_list|)
expr_stmt|;
name|ServerStatisticTracker
name|stats
init|=
name|conn
operator|.
name|getStatisticsTracker
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"No stats configured for the client!"
argument_list|,
name|stats
argument_list|)
expr_stmt|;
comment|// get the names so we can query the stats
name|ServerName
name|server
init|=
name|rs
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|byte
index|[]
name|regionName
init|=
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
comment|// check to see we found some load on the memstore
name|ServerStatistics
name|serverStats
init|=
name|stats
operator|.
name|getServerStatsForTesting
argument_list|(
name|server
argument_list|)
decl_stmt|;
name|ServerStatistics
operator|.
name|RegionStatistics
name|regionStats
init|=
name|serverStats
operator|.
name|getStatsForRegion
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"We did not find some load on the memstore"
argument_list|,
name|load
argument_list|,
name|regionStats
operator|.
name|getMemstoreLoadPercent
argument_list|()
argument_list|)
expr_stmt|;
comment|// check that the load reported produces a nonzero delay
name|long
name|backoffTime
init|=
name|backoffPolicy
operator|.
name|getBackoffTime
argument_list|(
name|server
argument_list|,
name|regionName
argument_list|,
name|serverStats
argument_list|)
decl_stmt|;
name|assertNotEquals
argument_list|(
literal|"Reported load does not produce a backoff"
argument_list|,
name|backoffTime
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Backoff calculated for "
operator|+
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" @ "
operator|+
name|server
operator|+
literal|" is "
operator|+
name|backoffTime
argument_list|)
expr_stmt|;
comment|// Reach into the connection and submit work directly to AsyncProcess so we can
comment|// monitor how long the submission was delayed via a callback
name|List
argument_list|<
name|Row
argument_list|>
name|ops
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|ops
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|AtomicLong
name|endTime
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
name|long
name|startTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|Batch
operator|.
name|Callback
argument_list|<
name|Result
argument_list|>
name|callback
init|=
parameter_list|(
name|byte
index|[]
name|r
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|Result
name|result
parameter_list|)
lambda|->
block|{
name|endTime
operator|.
name|set
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
decl_stmt|;
name|AsyncProcessTask
argument_list|<
name|Result
argument_list|>
name|task
init|=
name|AsyncProcessTask
operator|.
name|newBuilder
argument_list|(
name|callback
argument_list|)
operator|.
name|setPool
argument_list|(
name|mutator
operator|.
name|getPool
argument_list|()
argument_list|)
operator|.
name|setTableName
argument_list|(
name|tableName
argument_list|)
operator|.
name|setRowAccess
argument_list|(
name|ops
argument_list|)
operator|.
name|setSubmittedRows
argument_list|(
name|AsyncProcessTask
operator|.
name|SubmittedRows
operator|.
name|AT_LEAST_ONE
argument_list|)
operator|.
name|setOperationTimeout
argument_list|(
name|conn
operator|.
name|getConnectionConfiguration
argument_list|()
operator|.
name|getOperationTimeout
argument_list|()
argument_list|)
operator|.
name|setRpcTimeout
argument_list|(
literal|60
operator|*
literal|1000
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|mutator
operator|.
name|getAsyncProcess
argument_list|()
operator|.
name|submit
argument_list|(
name|task
argument_list|)
expr_stmt|;
comment|// Currently the ExponentialClientBackoffPolicy under these test conditions
comment|// produces a backoffTime of 151 milliseconds. This is long enough so the
comment|// wait and related checks below are reasonable. Revisit if the backoff
comment|// time reported by above debug logging has significantly deviated.
name|String
name|name
init|=
name|server
operator|.
name|getServerName
argument_list|()
operator|+
literal|","
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
name|MetricsConnection
operator|.
name|RegionStats
name|rsStats
init|=
name|conn
operator|.
name|getConnectionMetrics
argument_list|()
operator|.
name|serverStats
operator|.
name|get
argument_list|(
name|server
argument_list|)
operator|.
name|get
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|name
argument_list|,
name|rsStats
operator|.
name|name
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rsStats
operator|.
name|heapOccupancyHist
operator|.
name|getSnapshot
argument_list|()
operator|.
name|getMean
argument_list|()
argument_list|,
operator|(
name|double
operator|)
name|regionStats
operator|.
name|getHeapOccupancyPercent
argument_list|()
argument_list|,
literal|0.1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rsStats
operator|.
name|memstoreLoadHist
operator|.
name|getSnapshot
argument_list|()
operator|.
name|getMean
argument_list|()
argument_list|,
operator|(
name|double
operator|)
name|regionStats
operator|.
name|getMemstoreLoadPercent
argument_list|()
argument_list|,
literal|0.1
argument_list|)
expr_stmt|;
name|MetricsConnection
operator|.
name|RunnerStats
name|runnerStats
init|=
name|conn
operator|.
name|getConnectionMetrics
argument_list|()
operator|.
name|runnerStats
decl_stmt|;
name|assertEquals
argument_list|(
name|runnerStats
operator|.
name|delayRunners
operator|.
name|getCount
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|runnerStats
operator|.
name|normalRunners
operator|.
name|getCount
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|""
argument_list|,
name|runnerStats
operator|.
name|delayIntevalHist
operator|.
name|getSnapshot
argument_list|()
operator|.
name|getMean
argument_list|()
argument_list|,
operator|(
name|double
operator|)
name|backoffTime
argument_list|,
literal|0.1
argument_list|)
expr_stmt|;
name|latch
operator|.
name|await
argument_list|(
name|backoffTime
operator|*
literal|2
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
literal|"AsyncProcess did not submit the work time"
argument_list|,
name|endTime
operator|.
name|get
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"AsyncProcess did not delay long enough"
argument_list|,
name|endTime
operator|.
name|get
argument_list|()
operator|-
name|startTime
operator|>=
name|backoffTime
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMutateRowStats
parameter_list|()
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|ClusterConnection
name|conn
init|=
operator|(
name|ClusterConnection
operator|)
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HRegionServer
name|rs
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Region
name|region
init|=
name|rs
operator|.
name|getOnlineRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|RowMutations
name|mutations
init|=
operator|new
name|RowMutations
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value2"
argument_list|)
argument_list|)
expr_stmt|;
name|mutations
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|table
operator|.
name|mutateRow
argument_list|(
name|mutations
argument_list|)
expr_stmt|;
name|ServerStatisticTracker
name|stats
init|=
name|conn
operator|.
name|getStatisticsTracker
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"No stats configured for the client!"
argument_list|,
name|stats
argument_list|)
expr_stmt|;
comment|// get the names so we can query the stats
name|ServerName
name|server
init|=
name|rs
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|byte
index|[]
name|regionName
init|=
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
comment|// check to see we found some load on the memstore
name|ServerStatistics
name|serverStats
init|=
name|stats
operator|.
name|getServerStatsForTesting
argument_list|(
name|server
argument_list|)
decl_stmt|;
name|ServerStatistics
operator|.
name|RegionStatistics
name|regionStats
init|=
name|serverStats
operator|.
name|getStatsForRegion
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|regionStats
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|regionStats
operator|.
name|getMemstoreLoadPercent
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

