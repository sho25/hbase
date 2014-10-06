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
name|replication
operator|.
name|regionserver
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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
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
name|when
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
name|commons
operator|.
name|logging
operator|.
name|impl
operator|.
name|Log4JLogger
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
name|Waiter
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
name|Connection
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
name|ConnectionFactory
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
name|HConnection
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
name|HConnectionManager
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
name|RpcRetryingCaller
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
name|client
operator|.
name|replication
operator|.
name|ReplicationAdmin
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
name|wal
operator|.
name|HLog
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
name|wal
operator|.
name|HLogKey
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
name|wal
operator|.
name|WALEdit
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
name|replication
operator|.
name|ReplicationException
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
name|replication
operator|.
name|ReplicationPeerConfig
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
name|FlakeyTests
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
name|zookeeper
operator|.
name|ZKUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Level
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
name|Ignore
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
comment|/**  * Tests RegionReplicaReplicationEndpoint class by setting up region replicas and verifying  * async wal replication replays the edits to the secondary region in various scenarios.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|FlakeyTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestRegionReplicaReplicationEndpoint
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
name|TestRegionReplicaReplicationEndpoint
operator|.
name|class
argument_list|)
decl_stmt|;
static|static
block|{
operator|(
operator|(
name|Log4JLogger
operator|)
name|RpcRetryingCaller
operator|.
name|LOG
operator|)
operator|.
name|getLogger
argument_list|()
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|ALL
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|int
name|NB_SERVERS
init|=
literal|2
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|HTU
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeClass
parameter_list|()
throws|throws
name|Exception
block|{
comment|/*     Configuration conf = HTU.getConfiguration();     conf.setFloat("hbase.regionserver.logroll.multiplier", 0.0003f);     conf.setInt("replication.source.size.capacity", 10240);     conf.setLong("replication.source.sleepforretries", 100);     conf.setInt("hbase.regionserver.maxlogs", 10);     conf.setLong("hbase.master.logcleaner.ttl", 10);     conf.setInt("zookeeper.recovery.retry", 1);     conf.setInt("zookeeper.recovery.retry.intervalmill", 10);     conf.setBoolean(HConstants.REPLICATION_ENABLE_KEY, true);     conf.setBoolean(ServerRegionReplicaUtil.REGION_REPLICA_REPLICATION_CONF_KEY, true);     conf.setLong(HConstants.THREAD_WAKE_FREQUENCY, 100);     conf.setInt("replication.stats.thread.period.seconds", 5);     conf.setBoolean("hbase.tests.use.shortcircuit.reads", false);     conf.setInt(HConstants.HBASE_CLIENT_RETRIES_NUMBER, 3); // less number of retries is needed     conf.setInt("hbase.client.serverside.retries.multiplier", 1);      HTU.startMiniCluster(NB_SERVERS);*/
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
throws|throws
name|Exception
block|{
comment|/*     HTU.shutdownMiniCluster();     */
block|}
annotation|@
name|Ignore
argument_list|(
literal|"To be fixed before 1.0"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testRegionReplicaReplicationPeerIsCreated
parameter_list|()
throws|throws
name|IOException
throws|,
name|ReplicationException
block|{
comment|// create a table with region replicas. Check whether the replication peer is created
comment|// and replication started.
name|ReplicationAdmin
name|admin
init|=
operator|new
name|ReplicationAdmin
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|peerId
init|=
literal|"region_replica_replication"
decl_stmt|;
if|if
condition|(
name|admin
operator|.
name|getPeerConfig
argument_list|(
name|peerId
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|admin
operator|.
name|removePeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
name|HTableDescriptor
name|htd
init|=
name|HTU
operator|.
name|createTableDescriptor
argument_list|(
literal|"testReplicationPeerIsCreated_no_region_replicas"
argument_list|)
decl_stmt|;
name|HTU
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|ReplicationPeerConfig
name|peerConfig
init|=
name|admin
operator|.
name|getPeerConfig
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|peerConfig
argument_list|)
expr_stmt|;
name|htd
operator|=
name|HTU
operator|.
name|createTableDescriptor
argument_list|(
literal|"testReplicationPeerIsCreated"
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setRegionReplication
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
comment|// assert peer configuration is correct
name|peerConfig
operator|=
name|admin
operator|.
name|getPeerConfig
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|peerConfig
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|peerConfig
operator|.
name|getClusterKey
argument_list|()
argument_list|,
name|ZKUtil
operator|.
name|getZooKeeperClusterKey
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|peerConfig
operator|.
name|getReplicationEndpointImpl
argument_list|()
argument_list|,
name|RegionReplicaReplicationEndpoint
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|testRegionReplicaReplication
parameter_list|(
name|int
name|regionReplication
parameter_list|)
throws|throws
name|Exception
block|{
comment|// test region replica replication. Create a table with single region, write some data
comment|// ensure that data is replicated to the secondary region
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testRegionReplicaReplicationWithReplicas_"
operator|+
name|regionReplication
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
name|HTU
operator|.
name|createTableDescriptor
argument_list|(
name|tableName
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|htd
operator|.
name|setRegionReplication
argument_list|(
name|regionReplication
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|TableName
name|tableNameNoReplicas
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testRegionReplicaReplicationWithReplicas_NO_REPLICAS"
argument_list|)
decl_stmt|;
name|HTU
operator|.
name|deleteTableIfAny
argument_list|(
name|tableNameNoReplicas
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|createTable
argument_list|(
name|tableNameNoReplicas
argument_list|,
name|HBaseTestingUtility
operator|.
name|fam1
argument_list|)
expr_stmt|;
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|Table
name|tableNoReplicas
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableNameNoReplicas
argument_list|)
decl_stmt|;
try|try
block|{
comment|// load some data to the non-replicated table
name|HTU
operator|.
name|loadNumericRows
argument_list|(
name|tableNoReplicas
argument_list|,
name|HBaseTestingUtility
operator|.
name|fam1
argument_list|,
literal|6000
argument_list|,
literal|7000
argument_list|)
expr_stmt|;
comment|// load the data to the table
name|HTU
operator|.
name|loadNumericRows
argument_list|(
name|table
argument_list|,
name|HBaseTestingUtility
operator|.
name|fam1
argument_list|,
literal|0
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|verifyReplication
argument_list|(
name|tableName
argument_list|,
name|regionReplication
argument_list|,
literal|0
argument_list|,
literal|6000
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|tableNoReplicas
operator|.
name|close
argument_list|()
expr_stmt|;
name|HTU
operator|.
name|deleteTableIfAny
argument_list|(
name|tableNameNoReplicas
argument_list|)
expr_stmt|;
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|verifyReplication
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|int
name|regionReplication
parameter_list|,
specifier|final
name|int
name|startRow
parameter_list|,
specifier|final
name|int
name|endRow
parameter_list|)
throws|throws
name|Exception
block|{
comment|// find the regions
specifier|final
name|HRegion
index|[]
name|regions
init|=
operator|new
name|HRegion
index|[
name|regionReplication
index|]
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
name|NB_SERVERS
condition|;
name|i
operator|++
control|)
block|{
name|HRegionServer
name|rs
init|=
name|HTU
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegion
argument_list|>
name|onlineRegions
init|=
name|rs
operator|.
name|getOnlineRegions
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegion
name|region
range|:
name|onlineRegions
control|)
block|{
name|regions
index|[
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getReplicaId
argument_list|()
index|]
operator|=
name|region
expr_stmt|;
block|}
block|}
for|for
control|(
name|HRegion
name|region
range|:
name|regions
control|)
block|{
name|assertNotNull
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|regionReplication
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|HRegion
name|region
init|=
name|regions
index|[
name|i
index|]
decl_stmt|;
comment|// wait until all the data is replicated to all secondary regions
name|Waiter
operator|.
name|waitFor
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|60000
argument_list|,
operator|new
name|Waiter
operator|.
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
name|LOG
operator|.
name|info
argument_list|(
literal|"verifying replication for region replica:"
operator|+
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|HTU
operator|.
name|verifyNumericRows
argument_list|(
name|region
argument_list|,
name|HBaseTestingUtility
operator|.
name|fam1
argument_list|,
name|startRow
argument_list|,
name|endRow
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Verification from secondary region is not complete yet. Got:"
operator|+
name|ex
operator|+
literal|" "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
comment|// still wait
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Ignore
argument_list|(
literal|"To be fixed before 1.0"
argument_list|)
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testRegionReplicaReplicationWith2Replicas
parameter_list|()
throws|throws
name|Exception
block|{
name|testRegionReplicaReplication
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
argument_list|(
literal|"To be fixed before 1.0"
argument_list|)
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testRegionReplicaReplicationWith3Replicas
parameter_list|()
throws|throws
name|Exception
block|{
name|testRegionReplicaReplication
argument_list|(
literal|3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
argument_list|(
literal|"To be fixed before 1.0"
argument_list|)
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testRegionReplicaReplicationWith10Replicas
parameter_list|()
throws|throws
name|Exception
block|{
name|testRegionReplicaReplication
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
argument_list|(
literal|"To be fixed before 1.0"
argument_list|)
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testRegionReplicaReplicationForFlushAndCompaction
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Tests a table with region replication 3. Writes some data, and causes flushes and
comment|// compactions. Verifies that the data is readable from the replicas. Note that this
comment|// does not test whether the replicas actually pick up flushed files and apply compaction
comment|// to their stores
name|int
name|regionReplication
init|=
literal|3
decl_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testRegionReplicaReplicationForFlushAndCompaction"
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
name|HTU
operator|.
name|createTableDescriptor
argument_list|(
name|tableName
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|htd
operator|.
name|setRegionReplication
argument_list|(
name|regionReplication
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
try|try
block|{
comment|// load the data to the table
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|6000
condition|;
name|i
operator|+=
literal|1000
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Writing data from "
operator|+
name|i
operator|+
literal|" to "
operator|+
operator|(
name|i
operator|+
literal|1000
operator|)
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|loadNumericRows
argument_list|(
name|table
argument_list|,
name|HBaseTestingUtility
operator|.
name|fam1
argument_list|,
name|i
argument_list|,
name|i
operator|+
literal|1000
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"flushing table"
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"compacting table"
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|compact
argument_list|(
name|tableName
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|verifyReplication
argument_list|(
name|tableName
argument_list|,
name|regionReplication
argument_list|,
literal|0
argument_list|,
literal|6000
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Ignore
argument_list|(
literal|"To be fixed before 1.0"
argument_list|)
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testRegionReplicaReplicationIgnoresDisabledTables
parameter_list|()
throws|throws
name|Exception
block|{
name|testRegionReplicaReplicationIgnoresDisabledTables
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
argument_list|(
literal|"To be fixed before 1.0"
argument_list|)
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testRegionReplicaReplicationIgnoresDroppedTables
parameter_list|()
throws|throws
name|Exception
block|{
name|testRegionReplicaReplicationIgnoresDisabledTables
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testRegionReplicaReplicationIgnoresDisabledTables
parameter_list|(
name|boolean
name|dropTable
parameter_list|)
throws|throws
name|Exception
block|{
comment|// tests having edits from a disabled or dropped table is handled correctly by skipping those
comment|// entries and further edits after the edits from dropped/disabled table can be replicated
comment|// without problems.
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testRegionReplicaReplicationIgnoresDisabledTables"
operator|+
name|dropTable
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
name|HTU
operator|.
name|createTableDescriptor
argument_list|(
name|tableName
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|regionReplication
init|=
literal|3
decl_stmt|;
name|htd
operator|.
name|setRegionReplication
argument_list|(
name|regionReplication
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|deleteTableIfAny
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|TableName
name|toBeDisabledTable
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|dropTable
condition|?
literal|"droppedTable"
else|:
literal|"disabledTable"
argument_list|)
decl_stmt|;
name|HTU
operator|.
name|deleteTableIfAny
argument_list|(
name|toBeDisabledTable
argument_list|)
expr_stmt|;
name|htd
operator|=
name|HTU
operator|.
name|createTableDescriptor
argument_list|(
name|toBeDisabledTable
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setRegionReplication
argument_list|(
name|regionReplication
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
comment|// both tables are created, now pause replication
name|ReplicationAdmin
name|admin
init|=
operator|new
name|ReplicationAdmin
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|admin
operator|.
name|disablePeer
argument_list|(
name|ServerRegionReplicaUtil
operator|.
name|getReplicationPeerId
argument_list|()
argument_list|)
expr_stmt|;
comment|// now that the replication is disabled, write to the table to be dropped, then drop the table.
name|HConnection
name|connection
init|=
name|HConnectionManager
operator|.
name|createConnection
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|Table
name|tableToBeDisabled
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|toBeDisabledTable
argument_list|)
decl_stmt|;
name|HTU
operator|.
name|loadNumericRows
argument_list|(
name|tableToBeDisabled
argument_list|,
name|HBaseTestingUtility
operator|.
name|fam1
argument_list|,
literal|6000
argument_list|,
literal|7000
argument_list|)
expr_stmt|;
name|AtomicLong
name|skippedEdits
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
name|RegionReplicaReplicationEndpoint
operator|.
name|RegionReplicaOutputSink
name|sink
init|=
name|mock
argument_list|(
name|RegionReplicaReplicationEndpoint
operator|.
name|RegionReplicaOutputSink
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|sink
operator|.
name|getSkippedEditsCounter
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|skippedEdits
argument_list|)
expr_stmt|;
name|RegionReplicaReplicationEndpoint
operator|.
name|RegionReplicaSinkWriter
name|sinkWriter
init|=
operator|new
name|RegionReplicaReplicationEndpoint
operator|.
name|RegionReplicaSinkWriter
argument_list|(
name|sink
argument_list|,
operator|(
name|ClusterConnection
operator|)
name|connection
argument_list|,
name|Executors
operator|.
name|newSingleThreadExecutor
argument_list|()
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
name|HRegionLocation
name|hrl
init|=
name|connection
operator|.
name|locateRegion
argument_list|(
name|toBeDisabledTable
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
decl_stmt|;
name|byte
index|[]
name|encodedRegionName
init|=
name|hrl
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedNameAsBytes
argument_list|()
decl_stmt|;
name|HLog
operator|.
name|Entry
name|entry
init|=
operator|new
name|HLog
operator|.
name|Entry
argument_list|(
operator|new
name|HLogKey
argument_list|(
name|encodedRegionName
argument_list|,
name|toBeDisabledTable
argument_list|,
literal|1
argument_list|)
argument_list|,
operator|new
name|WALEdit
argument_list|()
argument_list|)
decl_stmt|;
name|HTU
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|toBeDisabledTable
argument_list|)
expr_stmt|;
comment|// disable the table
if|if
condition|(
name|dropTable
condition|)
block|{
name|HTU
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|deleteTable
argument_list|(
name|toBeDisabledTable
argument_list|)
expr_stmt|;
block|}
name|sinkWriter
operator|.
name|append
argument_list|(
name|toBeDisabledTable
argument_list|,
name|encodedRegionName
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|entry
argument_list|,
name|entry
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|skippedEdits
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
comment|// load some data to the to-be-dropped table
comment|// load the data to the table
name|HTU
operator|.
name|loadNumericRows
argument_list|(
name|table
argument_list|,
name|HBaseTestingUtility
operator|.
name|fam1
argument_list|,
literal|0
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
comment|// now enable the replication
name|admin
operator|.
name|enablePeer
argument_list|(
name|ServerRegionReplicaUtil
operator|.
name|getReplicationPeerId
argument_list|()
argument_list|)
expr_stmt|;
name|verifyReplication
argument_list|(
name|tableName
argument_list|,
name|regionReplication
argument_list|,
literal|0
argument_list|,
literal|6000
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|tableToBeDisabled
operator|.
name|close
argument_list|()
expr_stmt|;
name|HTU
operator|.
name|deleteTableIfAny
argument_list|(
name|toBeDisabledTable
argument_list|)
expr_stmt|;
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

