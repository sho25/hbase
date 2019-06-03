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
name|assertArrayEquals
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
name|fail
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
name|ColumnFamilyDescriptorBuilder
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
name|Delete
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
name|Put
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
name|ResultScanner
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
name|Scan
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
name|TableDescriptor
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
name|TableDescriptorBuilder
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
name|JVMClusterUtil
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
name|MiniZooKeeperCluster
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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_comment
comment|/**  * This class is only a base for other integration-level replication tests.  * Do not add tests here.  * TestReplicationSmallTests is where tests that don't require bring machines up/down should go  * All other tests should have their own classes and extend this one  */
end_comment

begin_class
specifier|public
class|class
name|TestReplicationBase
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
name|TestReplicationBase
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|Configuration
name|CONF_WITH_LOCALFS
decl_stmt|;
specifier|protected
specifier|static
name|ReplicationAdmin
name|admin
decl_stmt|;
specifier|protected
specifier|static
name|Admin
name|hbaseAdmin
decl_stmt|;
specifier|protected
specifier|static
name|Table
name|htable1
decl_stmt|;
specifier|protected
specifier|static
name|Table
name|htable2
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL1
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL2
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|Configuration
name|CONF1
init|=
name|UTIL1
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|Configuration
name|CONF2
init|=
name|UTIL2
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|NUM_SLAVES1
init|=
literal|2
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|NUM_SLAVES2
init|=
literal|4
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|NB_ROWS_IN_BATCH
init|=
literal|100
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|NB_ROWS_IN_BIG_BATCH
init|=
name|NB_ROWS_IN_BATCH
operator|*
literal|10
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|long
name|SLEEP_TIME
init|=
literal|500
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|NB_RETRIES
init|=
literal|50
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
name|famName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
name|noRepfamName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"norep"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|PEER_ID2
init|=
literal|"2"
decl_stmt|;
specifier|protected
name|boolean
name|isSerialPeer
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|protected
name|boolean
name|isSyncPeer
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|protected
specifier|final
name|void
name|cleanUp
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// Starting and stopping replication can make us miss new logs,
comment|// rolling like this makes sure the most recent one gets added to the queue
for|for
control|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|r
range|:
name|UTIL1
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
control|)
block|{
name|UTIL1
operator|.
name|getAdmin
argument_list|()
operator|.
name|rollWALWriter
argument_list|(
name|r
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|int
name|rowCount
init|=
name|UTIL1
operator|.
name|countRows
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|UTIL1
operator|.
name|deleteTableData
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// truncating the table will send one Delete per row to the slave cluster
comment|// in an async fashion, which is why we cannot just call deleteTableData on
comment|// utility2 since late writes could make it to the slave in some way.
comment|// Instead, we truncate the first table and wait for all the Deletes to
comment|// make it to the slave.
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|int
name|lastCount
init|=
literal|0
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
name|NB_RETRIES
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|==
name|NB_RETRIES
operator|-
literal|1
condition|)
block|{
name|fail
argument_list|(
literal|"Waited too much time for truncate"
argument_list|)
expr_stmt|;
block|}
name|ResultScanner
name|scanner
init|=
name|htable2
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|Result
index|[]
name|res
init|=
name|scanner
operator|.
name|next
argument_list|(
name|rowCount
argument_list|)
decl_stmt|;
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|res
operator|.
name|length
operator|!=
literal|0
condition|)
block|{
if|if
condition|(
name|res
operator|.
name|length
operator|<
name|lastCount
condition|)
block|{
name|i
operator|--
expr_stmt|;
comment|// Don't increment timeout if we make progress
block|}
name|lastCount
operator|=
name|res
operator|.
name|length
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Still got "
operator|+
name|res
operator|.
name|length
operator|+
literal|" rows"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_TIME
argument_list|)
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
block|}
block|}
specifier|protected
specifier|static
name|void
name|waitForReplication
parameter_list|(
name|int
name|expectedRows
parameter_list|,
name|int
name|retries
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|Scan
name|scan
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
name|retries
condition|;
name|i
operator|++
control|)
block|{
name|scan
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
if|if
condition|(
name|i
operator|==
name|retries
operator|-
literal|1
condition|)
block|{
name|fail
argument_list|(
literal|"Waited too much time for normal batch replication"
argument_list|)
expr_stmt|;
block|}
name|ResultScanner
name|scanner
init|=
name|htable2
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|Result
index|[]
name|res
init|=
name|scanner
operator|.
name|next
argument_list|(
name|expectedRows
argument_list|)
decl_stmt|;
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|res
operator|.
name|length
operator|!=
name|expectedRows
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Only got "
operator|+
name|res
operator|.
name|length
operator|+
literal|" rows"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_TIME
argument_list|)
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
block|}
block|}
specifier|protected
specifier|static
name|void
name|loadData
parameter_list|(
name|String
name|prefix
parameter_list|,
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
block|{
name|loadData
argument_list|(
name|prefix
argument_list|,
name|row
argument_list|,
name|famName
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|void
name|loadData
parameter_list|(
name|String
name|prefix
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|familyName
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Put
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|NB_ROWS_IN_BATCH
argument_list|)
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
name|NB_ROWS_IN_BATCH
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|prefix
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|familyName
argument_list|,
name|row
argument_list|,
name|row
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
name|htable1
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|setupConfig
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|String
name|znodeParent
parameter_list|)
block|{
name|Configuration
name|conf
init|=
name|util
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
name|znodeParent
argument_list|)
expr_stmt|;
comment|// We don't want too many edits per batch sent to the ReplicationEndpoint to trigger
comment|// sufficient number of events. But we don't want to go too low because
comment|// HBaseInterClusterReplicationEndpoint partitions entries into batches and we want
comment|// more than one batch sent to the peer cluster for better testing.
name|conf
operator|.
name|setInt
argument_list|(
literal|"replication.source.size.capacity"
argument_list|,
literal|102400
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
literal|"replication.source.sleepforretries"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.maxlogs"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.master.logcleaner.ttl"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"zookeeper.recovery.retry"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"zookeeper.recovery.retry.intervalmill"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|THREAD_WAKE_FREQUENCY
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"replication.stats.thread.period.seconds"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.tests.use.shortcircuit.reads"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
literal|"replication.sleep.before.failover"
argument_list|,
literal|2000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"replication.source.maxretriesmultiplier"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
literal|"replication.source.ratio"
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"replication.source.eof.autorecovery"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.serial.replication.waiting.ms"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
block|}
specifier|static
name|void
name|configureClusters
parameter_list|(
name|HBaseTestingUtility
name|util1
parameter_list|,
name|HBaseTestingUtility
name|util2
parameter_list|)
block|{
name|setupConfig
argument_list|(
name|util1
argument_list|,
literal|"/1"
argument_list|)
expr_stmt|;
name|setupConfig
argument_list|(
name|util2
argument_list|,
literal|"/2"
argument_list|)
expr_stmt|;
name|Configuration
name|conf2
init|=
name|util2
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf2
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
literal|"/2"
argument_list|)
expr_stmt|;
name|conf2
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|conf2
operator|.
name|setBoolean
argument_list|(
literal|"hbase.tests.use.shortcircuit.reads"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|void
name|restartHBaseCluster
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|int
name|numSlaves
parameter_list|)
throws|throws
name|Exception
block|{
name|util
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
name|util
operator|.
name|restartHBaseCluster
argument_list|(
name|numSlaves
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|void
name|startClusters
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL1
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|MiniZooKeeperCluster
name|miniZK
init|=
name|UTIL1
operator|.
name|getZkCluster
argument_list|()
decl_stmt|;
name|admin
operator|=
operator|new
name|ReplicationAdmin
argument_list|(
name|CONF1
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Setup first Zk"
argument_list|)
expr_stmt|;
name|UTIL2
operator|.
name|setZkCluster
argument_list|(
name|miniZK
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Setup second Zk"
argument_list|)
expr_stmt|;
name|CONF_WITH_LOCALFS
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|CONF1
argument_list|)
expr_stmt|;
name|UTIL1
operator|.
name|startMiniCluster
argument_list|(
name|NUM_SLAVES1
argument_list|)
expr_stmt|;
comment|// Have a bunch of slave servers, because inter-cluster shipping logic uses number of sinks
comment|// as a component in deciding maximum number of parallel batches to send to the peer cluster.
name|UTIL2
operator|.
name|startMiniCluster
argument_list|(
name|NUM_SLAVES2
argument_list|)
expr_stmt|;
name|hbaseAdmin
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|CONF1
argument_list|)
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|TableDescriptor
name|table
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|famName
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
literal|100
argument_list|)
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|noRepfamName
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Connection
name|connection1
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|CONF1
argument_list|)
decl_stmt|;
name|Connection
name|connection2
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|CONF2
argument_list|)
decl_stmt|;
try|try
init|(
name|Admin
name|admin1
init|=
name|connection1
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin1
operator|.
name|createTable
argument_list|(
name|table
argument_list|,
name|HBaseTestingUtility
operator|.
name|KEYS_FOR_HBA_CREATE_TABLE
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|Admin
name|admin2
init|=
name|connection2
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin2
operator|.
name|createTable
argument_list|(
name|table
argument_list|,
name|HBaseTestingUtility
operator|.
name|KEYS_FOR_HBA_CREATE_TABLE
argument_list|)
expr_stmt|;
block|}
name|UTIL1
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|UTIL2
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|htable1
operator|=
name|connection1
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|htable2
operator|=
name|connection2
operator|.
name|getTable
argument_list|(
name|tableName
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
name|Exception
block|{
name|configureClusters
argument_list|(
name|UTIL1
argument_list|,
name|UTIL2
argument_list|)
expr_stmt|;
name|startClusters
argument_list|()
expr_stmt|;
block|}
specifier|private
name|boolean
name|peerExist
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|hbaseAdmin
operator|.
name|listReplicationPeers
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|anyMatch
argument_list|(
name|p
lambda|->
name|peerId
operator|.
name|equals
argument_list|(
name|p
operator|.
name|getPeerId
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUpBase
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|peerExist
argument_list|(
name|PEER_ID2
argument_list|)
condition|)
block|{
name|ReplicationPeerConfigBuilder
name|builder
init|=
name|ReplicationPeerConfig
operator|.
name|newBuilder
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|UTIL2
operator|.
name|getClusterKey
argument_list|()
argument_list|)
operator|.
name|setSerial
argument_list|(
name|isSerialPeer
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|isSyncPeer
argument_list|()
condition|)
block|{
name|FileSystem
name|fs2
init|=
name|UTIL2
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
comment|// The remote wal dir is not important as we do not use it in DA state, here we only need to
comment|// confirm that a sync peer in DA state can still replicate data to remote cluster
comment|// asynchronously.
name|builder
operator|.
name|setReplicateAllUserTables
argument_list|(
literal|false
argument_list|)
operator|.
name|setTableCFsMap
argument_list|(
name|ImmutableMap
operator|.
name|of
argument_list|(
name|tableName
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setRemoteWALDir
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/RemoteWAL"
argument_list|)
operator|.
name|makeQualified
argument_list|(
name|fs2
operator|.
name|getUri
argument_list|()
argument_list|,
name|fs2
operator|.
name|getWorkingDirectory
argument_list|()
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|hbaseAdmin
operator|.
name|addReplicationPeer
argument_list|(
name|PEER_ID2
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDownBase
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|peerExist
argument_list|(
name|PEER_ID2
argument_list|)
condition|)
block|{
name|hbaseAdmin
operator|.
name|removeReplicationPeer
argument_list|(
name|PEER_ID2
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
specifier|static
name|void
name|runSimplePutDeleteTest
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|famName
argument_list|,
name|row
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|htable1
operator|=
name|UTIL1
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|htable1
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
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
name|NB_RETRIES
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|==
name|NB_RETRIES
operator|-
literal|1
condition|)
block|{
name|fail
argument_list|(
literal|"Waited too much time for put replication"
argument_list|)
expr_stmt|;
block|}
name|Result
name|res
init|=
name|htable2
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
if|if
condition|(
name|res
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Row not available"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_TIME
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertArrayEquals
argument_list|(
name|row
argument_list|,
name|res
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|Delete
name|del
init|=
operator|new
name|Delete
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|htable1
operator|.
name|delete
argument_list|(
name|del
argument_list|)
expr_stmt|;
name|get
operator|=
operator|new
name|Get
argument_list|(
name|row
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
name|NB_RETRIES
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|==
name|NB_RETRIES
operator|-
literal|1
condition|)
block|{
name|fail
argument_list|(
literal|"Waited too much time for del replication"
argument_list|)
expr_stmt|;
block|}
name|Result
name|res
init|=
name|htable2
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
if|if
condition|(
name|res
operator|.
name|size
argument_list|()
operator|>=
literal|1
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Row not deleted"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_TIME
argument_list|)
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
block|}
block|}
specifier|protected
specifier|static
name|void
name|runSmallBatchTest
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// normal Batch tests
name|loadData
argument_list|(
literal|""
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|ResultScanner
name|scanner1
init|=
name|htable1
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|Result
index|[]
name|res1
init|=
name|scanner1
operator|.
name|next
argument_list|(
name|NB_ROWS_IN_BATCH
argument_list|)
decl_stmt|;
name|scanner1
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|NB_ROWS_IN_BATCH
argument_list|,
name|res1
operator|.
name|length
argument_list|)
expr_stmt|;
name|waitForReplication
argument_list|(
name|NB_ROWS_IN_BATCH
argument_list|,
name|NB_RETRIES
argument_list|)
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
name|Exception
block|{
name|htable2
operator|.
name|close
argument_list|()
expr_stmt|;
name|htable1
operator|.
name|close
argument_list|()
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
name|UTIL2
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
name|UTIL1
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

