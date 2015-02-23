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
name|TimeUnit
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
name|util
operator|.
name|ConstantDelayQueue
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
name|MultiThreadedUpdater
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
name|MultiThreadedWriter
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
name|ServerRegionReplicaUtil
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
comment|/**  * Integration test for testing async wal replication to secondary region replicas. Sets up a table  * with given region replication (default 2), and uses LoadTestTool client writer, updater and  * reader threads for writes and reads and verification. It uses a delay queue with a given delay  * ("read_delay_ms", default 5000ms) between the writer/updater and reader threads to make the  * written items available to readers. This means that a reader will only start reading from a row  * written by the writer / updater after 5secs has passed. The reader thread performs the reads from  * the given region replica id (default 1) to perform the reads. Async wal replication has to finish  * with the replication of the edits before read_delay_ms to the given region replica id so that  * the read and verify will not fail.  *  * The job will run for<b>at least<b> given runtime (default 10min) by running a concurrent  * writer and reader workload followed by a concurrent updater and reader workload for  * num_keys_per_server.  *<p>  * Example usage:  *<pre>  * hbase org.apache.hadoop.hbase.IntegrationTestRegionReplicaReplication  * -DIntegrationTestRegionReplicaReplication.num_keys_per_server=10000  * -Dhbase.IntegrationTestRegionReplicaReplication.runtime=600000  * -DIntegrationTestRegionReplicaReplication.read_delay_ms=5000  * -DIntegrationTestRegionReplicaReplication.region_replication=3  * -DIntegrationTestRegionReplicaReplication.region_replica_id=2  * -DIntegrationTestRegionReplicaReplication.num_read_threads=100  * -DIntegrationTestRegionReplicaReplication.num_write_threads=100  *</pre>  */
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
name|IntegrationTestRegionReplicaReplication
extends|extends
name|IntegrationTestIngest
block|{
specifier|private
specifier|static
specifier|final
name|String
name|TEST_NAME
init|=
name|IntegrationTestRegionReplicaReplication
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OPT_READ_DELAY_MS
init|=
literal|"read_delay_ms"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_REGION_REPLICATION
init|=
literal|2
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|SERVER_COUNT
init|=
literal|1
decl_stmt|;
comment|// number of slaves for the smallest cluster
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|DEFAULT_COLUMN_FAMILIES
init|=
operator|new
name|String
index|[]
block|{
literal|"f1"
block|,
literal|"f2"
block|,
literal|"f3"
block|}
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|int
name|getMinServerCount
parameter_list|()
block|{
return|return
name|SERVER_COUNT
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
name|TEST_NAME
argument_list|,
name|LoadTestTool
operator|.
name|OPT_REGION_REPLICATION
argument_list|)
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|DEFAULT_REGION_REPLICATION
argument_list|)
argument_list|)
expr_stmt|;
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
name|TEST_NAME
argument_list|,
name|LoadTestTool
operator|.
name|OPT_COLUMN_FAMILIES
argument_list|)
argument_list|,
name|StringUtils
operator|.
name|join
argument_list|(
literal|","
argument_list|,
name|DEFAULT_COLUMN_FAMILIES
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.table.sanity.checks"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// enable async wal replication to region replicas for unit tests
name|conf
operator|.
name|setBoolean
argument_list|(
name|ServerRegionReplicaUtil
operator|.
name|REGION_REPLICA_REPLICATION_CONF_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|REPLICATION_ENABLE_KEY
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
name|HREGION_MEMSTORE_FLUSH_SIZE
argument_list|,
literal|1024L
operator|*
literal|1024
operator|*
literal|4
argument_list|)
expr_stmt|;
comment|// flush every 4 MB
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.blockingStoreFiles"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|super
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|Test
specifier|public
name|void
name|testIngest
parameter_list|()
throws|throws
name|Exception
block|{
name|runIngestTest
argument_list|(
name|JUNIT_RUN_TIME
argument_list|,
literal|25000
argument_list|,
literal|10
argument_list|,
literal|1024
argument_list|,
literal|10
argument_list|,
literal|20
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|startMonkey
parameter_list|()
throws|throws
name|Exception
block|{
comment|// TODO: disabled for now
block|}
comment|/**    * This extends MultiThreadedWriter to add a configurable delay to the keys written by the writer    * threads to become available to the MultiThradedReader threads. We add this delay because of    * the async nature of the wal replication to region replicas.    */
specifier|public
specifier|static
class|class
name|DelayingMultiThreadedWriter
extends|extends
name|MultiThreadedWriter
block|{
specifier|private
name|long
name|delayMs
decl_stmt|;
specifier|public
name|DelayingMultiThreadedWriter
parameter_list|(
name|LoadTestDataGenerator
name|dataGen
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|TableName
name|tableName
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
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|BlockingQueue
argument_list|<
name|Long
argument_list|>
name|createWriteKeysQueue
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|delayMs
operator|=
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
name|IntegrationTestRegionReplicaReplication
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|OPT_READ_DELAY_MS
argument_list|)
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
return|return
operator|new
name|ConstantDelayQueue
argument_list|<
name|Long
argument_list|>
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
name|delayMs
argument_list|)
return|;
block|}
block|}
comment|/**    * This extends MultiThreadedWriter to add a configurable delay to the keys written by the writer    * threads to become available to the MultiThradedReader threads. We add this delay because of    * the async nature of the wal replication to region replicas.    */
specifier|public
specifier|static
class|class
name|DelayingMultiThreadedUpdater
extends|extends
name|MultiThreadedUpdater
block|{
specifier|private
name|long
name|delayMs
decl_stmt|;
specifier|public
name|DelayingMultiThreadedUpdater
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
name|updatePercent
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
name|updatePercent
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|BlockingQueue
argument_list|<
name|Long
argument_list|>
name|createWriteKeysQueue
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|delayMs
operator|=
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
name|IntegrationTestRegionReplicaReplication
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|OPT_READ_DELAY_MS
argument_list|)
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
return|return
operator|new
name|ConstantDelayQueue
argument_list|<
name|Long
argument_list|>
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
name|delayMs
argument_list|)
return|;
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
literal|"Running ingest"
argument_list|)
expr_stmt|;
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
comment|// sleep for some time so that the cache for disabled tables does not interfere.
name|Threads
operator|.
name|sleep
argument_list|(
name|getConf
argument_list|()
operator|.
name|getInt
argument_list|(
literal|"hbase.region.replica.replication.cache.disabledAndDroppedTables.expiryMs"
argument_list|,
literal|5000
argument_list|)
operator|+
literal|1000
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
while|while
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|<
literal|0.9
operator|*
name|runtime
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Intended run time: "
operator|+
operator|(
name|runtime
operator|/
literal|60000
operator|)
operator|+
literal|" min, left:"
operator|+
operator|(
operator|(
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
operator|)
operator|/
literal|60000
operator|)
operator|+
literal|" min"
argument_list|)
expr_stmt|;
name|int
name|verifyPercent
init|=
literal|100
decl_stmt|;
name|int
name|updatePercent
init|=
literal|20
decl_stmt|;
name|int
name|ret
init|=
operator|-
literal|1
decl_stmt|;
name|int
name|regionReplicaId
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s.%s"
argument_list|,
name|TEST_NAME
argument_list|,
name|LoadTestTool
operator|.
name|OPT_REGION_REPLICA_ID
argument_list|)
argument_list|,
literal|1
argument_list|)
decl_stmt|;
comment|// we will run writers and readers at the same time.
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
name|getArgsForLoadTestTool
argument_list|(
literal|""
argument_list|,
literal|""
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
literal|"-write"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
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
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-"
operator|+
name|LoadTestTool
operator|.
name|OPT_MULTIPUT
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-writer"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|DelayingMultiThreadedWriter
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// inject writer class
name|args
operator|.
name|add
argument_list|(
literal|"-read"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%d:%d"
argument_list|,
name|verifyPercent
argument_list|,
name|readThreads
argument_list|)
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-"
operator|+
name|LoadTestTool
operator|.
name|OPT_REGION_REPLICA_ID
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|regionReplicaId
argument_list|)
argument_list|)
expr_stmt|;
name|ret
operator|=
name|loadTool
operator|.
name|run
argument_list|(
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
argument_list|)
expr_stmt|;
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
name|args
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|getArgsForLoadTestTool
argument_list|(
literal|""
argument_list|,
literal|""
argument_list|,
name|startKey
argument_list|,
name|numKeys
argument_list|)
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-update"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s:%s:1"
argument_list|,
name|updatePercent
argument_list|,
name|writeThreads
argument_list|)
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-updater"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|DelayingMultiThreadedUpdater
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// inject updater class
name|args
operator|.
name|add
argument_list|(
literal|"-read"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%d:%d"
argument_list|,
name|verifyPercent
argument_list|,
name|readThreads
argument_list|)
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-"
operator|+
name|LoadTestTool
operator|.
name|OPT_REGION_REPLICA_ID
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|regionReplicaId
argument_list|)
argument_list|)
expr_stmt|;
name|ret
operator|=
name|loadTool
operator|.
name|run
argument_list|(
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
argument_list|)
expr_stmt|;
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
name|startKey
operator|+=
name|numKeys
expr_stmt|;
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
name|IntegrationTestRegionReplicaReplication
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

