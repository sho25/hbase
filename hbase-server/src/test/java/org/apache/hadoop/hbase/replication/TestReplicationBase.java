begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
operator|.
name|HBaseAdmin
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
name|HTable
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
name|zookeeper
operator|.
name|MiniZooKeeperCluster
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
name|ZooKeeperWatcher
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
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestReplicationBase
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|Configuration
name|conf1
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
name|Configuration
name|conf2
decl_stmt|;
specifier|protected
specifier|static
name|Configuration
name|CONF_WITH_LOCALFS
decl_stmt|;
specifier|protected
specifier|static
name|ZooKeeperWatcher
name|zkw1
decl_stmt|;
specifier|protected
specifier|static
name|ZooKeeperWatcher
name|zkw2
decl_stmt|;
specifier|protected
specifier|static
name|ReplicationAdmin
name|admin
decl_stmt|;
specifier|protected
specifier|static
name|HTable
name|htable1
decl_stmt|;
specifier|protected
specifier|static
name|HTable
name|htable2
decl_stmt|;
specifier|protected
specifier|static
name|HBaseTestingUtility
name|utility1
decl_stmt|;
specifier|protected
specifier|static
name|HBaseTestingUtility
name|utility2
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
literal|10
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
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
name|conf1
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
literal|"/1"
argument_list|)
expr_stmt|;
comment|// smaller log roll size to trigger more events
name|conf1
operator|.
name|setFloat
argument_list|(
literal|"hbase.regionserver.logroll.multiplier"
argument_list|,
literal|0.0003f
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setInt
argument_list|(
literal|"replication.source.size.capacity"
argument_list|,
literal|10240
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setLong
argument_list|(
literal|"replication.source.sleepforretries"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.maxlogs"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setLong
argument_list|(
literal|"hbase.master.logcleaner.ttl"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setInt
argument_list|(
literal|"zookeeper.recovery.retry"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setInt
argument_list|(
literal|"zookeeper.recovery.retry.intervalmill"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf1
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
name|conf1
operator|.
name|setBoolean
argument_list|(
literal|"dfs.support.append"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf1
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
name|conf1
operator|.
name|setInt
argument_list|(
literal|"replication.stats.thread.period.seconds"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|utility1
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf1
argument_list|)
expr_stmt|;
name|utility1
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|MiniZooKeeperCluster
name|miniZK
init|=
name|utility1
operator|.
name|getZkCluster
argument_list|()
decl_stmt|;
comment|// Have to reget conf1 in case zk cluster location different
comment|// than default
name|conf1
operator|=
name|utility1
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|zkw1
operator|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|conf1
argument_list|,
literal|"cluster1"
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|admin
operator|=
operator|new
name|ReplicationAdmin
argument_list|(
name|conf1
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Setup first Zk"
argument_list|)
expr_stmt|;
comment|// Base conf2 on conf1 so it gets the right zk cluster.
name|conf2
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|conf1
argument_list|)
expr_stmt|;
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
literal|"hbase.client.retries.number"
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|conf2
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
name|conf2
operator|.
name|setBoolean
argument_list|(
literal|"dfs.support.append"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|utility2
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf2
argument_list|)
expr_stmt|;
name|utility2
operator|.
name|setZkCluster
argument_list|(
name|miniZK
argument_list|)
expr_stmt|;
name|zkw2
operator|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|conf2
argument_list|,
literal|"cluster2"
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|admin
operator|.
name|addPeer
argument_list|(
literal|"2"
argument_list|,
name|utility2
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
name|setIsReplication
argument_list|(
literal|true
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
name|conf1
argument_list|)
expr_stmt|;
name|utility1
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|utility2
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|table
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|fam
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|famName
argument_list|)
decl_stmt|;
name|fam
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
expr_stmt|;
name|table
operator|.
name|addFamily
argument_list|(
name|fam
argument_list|)
expr_stmt|;
name|fam
operator|=
operator|new
name|HColumnDescriptor
argument_list|(
name|noRepfamName
argument_list|)
expr_stmt|;
name|table
operator|.
name|addFamily
argument_list|(
name|fam
argument_list|)
expr_stmt|;
name|HBaseAdmin
name|admin1
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf1
argument_list|)
decl_stmt|;
name|HBaseAdmin
name|admin2
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf2
argument_list|)
decl_stmt|;
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
name|admin2
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|htable1
operator|=
operator|new
name|HTable
argument_list|(
name|conf1
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|htable1
operator|.
name|setWriteBufferSize
argument_list|(
literal|1024
argument_list|)
expr_stmt|;
name|htable2
operator|=
operator|new
name|HTable
argument_list|(
name|conf2
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|void
name|setIsReplication
parameter_list|(
name|boolean
name|rep
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Set rep "
operator|+
name|rep
argument_list|)
expr_stmt|;
name|admin
operator|.
name|setReplicating
argument_list|(
name|rep
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
name|utility2
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
name|utility1
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

