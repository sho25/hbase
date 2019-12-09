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
name|regionserver
package|;
end_package

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
name|HConstants
operator|.
name|REPLICATION_CLUSTER_ID
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
name|HConstants
operator|.
name|REPLICATION_CONF_DIR
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
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileOutputStream
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
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
name|fs
operator|.
name|FSDataOutputStream
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
name|Cell
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
name|CellBuilder
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
name|CellBuilderFactory
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
name|CellBuilderType
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
name|KeyValue
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
name|coprocessor
operator|.
name|ObserverContext
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
name|coprocessor
operator|.
name|RegionCoprocessor
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
name|coprocessor
operator|.
name|RegionCoprocessorEnvironment
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
name|coprocessor
operator|.
name|RegionObserver
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
name|io
operator|.
name|hfile
operator|.
name|HFile
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
name|io
operator|.
name|hfile
operator|.
name|HFileContextBuilder
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
name|replication
operator|.
name|TestReplicationBase
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
name|testclassification
operator|.
name|ReplicationTests
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
name|tool
operator|.
name|BulkLoadHFilesTool
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
name|Pair
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
name|hdfs
operator|.
name|MiniDFSCluster
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
name|BeforeClass
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
name|TemporaryFolder
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

begin_comment
comment|/**  * Integration test for bulk load replication. Defines three clusters, with the following  * replication topology: "1<-> 2<-> 3" (active-active between 1 and 2, and active-active between  * 2 and 3).  *  * For each of defined test clusters, it performs a bulk load, asserting values on bulk loaded file  * gets replicated to other two peers. Since we are doing 3 bulk loads, with the given replication  * topology all these bulk loads should get replicated only once on each peer. To assert this,  * this test defines a preBulkLoad coprocessor and adds it to all test table regions, on each of the  * clusters. This CP counts the amount of times bulk load actually gets invoked, certifying  * we are not entering the infinite loop condition addressed by HBASE-22380.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|ReplicationTests
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
name|TestBulkLoadReplication
extends|extends
name|TestReplicationBase
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
name|TestBulkLoadReplication
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestBulkLoadReplication
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|PEER1_CLUSTER_ID
init|=
literal|"peer1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|PEER2_CLUSTER_ID
init|=
literal|"peer2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|PEER3_CLUSTER_ID
init|=
literal|"peer3"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|PEER_ID1
init|=
literal|"1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|PEER_ID3
init|=
literal|"3"
decl_stmt|;
specifier|private
specifier|static
name|AtomicInteger
name|BULK_LOADS_COUNT
decl_stmt|;
specifier|private
specifier|static
name|CountDownLatch
name|BULK_LOAD_LATCH
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL3
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|Configuration
name|CONF3
init|=
name|UTIL3
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Path
name|BULK_LOAD_BASE_DIR
init|=
operator|new
name|Path
argument_list|(
literal|"/bulk_dir"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Table
name|htable3
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
annotation|@
name|ClassRule
specifier|public
specifier|static
name|TemporaryFolder
name|testFolder
init|=
operator|new
name|TemporaryFolder
argument_list|()
decl_stmt|;
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
name|setupBulkLoadConfigsForCluster
argument_list|(
name|CONF1
argument_list|,
name|PEER1_CLUSTER_ID
argument_list|)
expr_stmt|;
name|setupBulkLoadConfigsForCluster
argument_list|(
name|CONF2
argument_list|,
name|PEER2_CLUSTER_ID
argument_list|)
expr_stmt|;
name|setupBulkLoadConfigsForCluster
argument_list|(
name|CONF3
argument_list|,
name|PEER3_CLUSTER_ID
argument_list|)
expr_stmt|;
name|setupConfig
argument_list|(
name|UTIL3
argument_list|,
literal|"/3"
argument_list|)
expr_stmt|;
name|TestReplicationBase
operator|.
name|setUpBeforeClass
argument_list|()
expr_stmt|;
name|startThirdCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|startThirdCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Setup Zk to same one from UTIL1 and UTIL2"
argument_list|)
expr_stmt|;
name|UTIL3
operator|.
name|setZkCluster
argument_list|(
name|UTIL1
operator|.
name|getZkCluster
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL3
operator|.
name|startMiniCluster
argument_list|(
name|NUM_SLAVES1
argument_list|)
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
name|setMobEnabled
argument_list|(
literal|true
argument_list|)
operator|.
name|setMobThreshold
argument_list|(
literal|4000
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
name|connection3
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|CONF3
argument_list|)
decl_stmt|;
try|try
init|(
name|Admin
name|admin3
init|=
name|connection3
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin3
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
name|UTIL3
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|htable3
operator|=
name|connection3
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
annotation|@
name|Override
specifier|public
name|void
name|setUpBase
parameter_list|()
throws|throws
name|Exception
block|{
comment|//"super.setUpBase()" already sets replication from 1->2,
comment|//then on the subsequent lines, sets 2->1, 2->3 and 3->2.
comment|//So we have following topology: "1<-> 2<->3"
name|super
operator|.
name|setUpBase
argument_list|()
expr_stmt|;
name|ReplicationPeerConfig
name|peer1Config
init|=
name|getPeerConfigForCluster
argument_list|(
name|UTIL1
argument_list|)
decl_stmt|;
name|ReplicationPeerConfig
name|peer2Config
init|=
name|getPeerConfigForCluster
argument_list|(
name|UTIL2
argument_list|)
decl_stmt|;
name|ReplicationPeerConfig
name|peer3Config
init|=
name|getPeerConfigForCluster
argument_list|(
name|UTIL3
argument_list|)
decl_stmt|;
comment|//adds cluster1 as a remote peer on cluster2
name|UTIL2
operator|.
name|getAdmin
argument_list|()
operator|.
name|addReplicationPeer
argument_list|(
name|PEER_ID1
argument_list|,
name|peer1Config
argument_list|)
expr_stmt|;
comment|//adds cluster3 as a remote peer on cluster2
name|UTIL2
operator|.
name|getAdmin
argument_list|()
operator|.
name|addReplicationPeer
argument_list|(
name|PEER_ID3
argument_list|,
name|peer3Config
argument_list|)
expr_stmt|;
comment|//adds cluster2 as a remote peer on cluster3
name|UTIL3
operator|.
name|getAdmin
argument_list|()
operator|.
name|addReplicationPeer
argument_list|(
name|PEER_ID2
argument_list|,
name|peer2Config
argument_list|)
expr_stmt|;
name|setupCoprocessor
argument_list|(
name|UTIL1
argument_list|,
literal|"cluster1"
argument_list|)
expr_stmt|;
name|setupCoprocessor
argument_list|(
name|UTIL2
argument_list|,
literal|"cluster2"
argument_list|)
expr_stmt|;
name|setupCoprocessor
argument_list|(
name|UTIL3
argument_list|,
literal|"cluster3"
argument_list|)
expr_stmt|;
name|BULK_LOADS_COUNT
operator|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ReplicationPeerConfig
name|getPeerConfigForCluster
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|)
block|{
return|return
name|ReplicationPeerConfig
operator|.
name|newBuilder
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|util
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
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
name|void
name|setupCoprocessor
parameter_list|(
name|HBaseTestingUtility
name|cluster
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|cluster
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|forEach
argument_list|(
name|r
lambda|->
block|{
try|try
block|{
name|TestBulkLoadReplication
operator|.
name|BulkReplicationTestObserver
name|cp
init|=
name|r
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|TestBulkLoadReplication
operator|.
name|BulkReplicationTestObserver
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|cp
operator|==
literal|null
condition|)
block|{
name|r
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|load
argument_list|(
name|TestBulkLoadReplication
operator|.
name|BulkReplicationTestObserver
operator|.
name|class
argument_list|,
literal|0
argument_list|,
name|cluster
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|cp
operator|=
name|r
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|TestBulkLoadReplication
operator|.
name|BulkReplicationTestObserver
operator|.
name|class
argument_list|)
expr_stmt|;
name|cp
operator|.
name|clusterName
operator|=
name|cluster
operator|.
name|getClusterKey
argument_list|()
expr_stmt|;
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
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
annotation|@
name|Override
specifier|public
name|void
name|tearDownBase
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDownBase
argument_list|()
expr_stmt|;
name|UTIL2
operator|.
name|getAdmin
argument_list|()
operator|.
name|removeReplicationPeer
argument_list|(
name|PEER_ID1
argument_list|)
expr_stmt|;
name|UTIL2
operator|.
name|getAdmin
argument_list|()
operator|.
name|removeReplicationPeer
argument_list|(
name|PEER_ID3
argument_list|)
expr_stmt|;
name|UTIL3
operator|.
name|getAdmin
argument_list|()
operator|.
name|removeReplicationPeer
argument_list|(
name|PEER_ID2
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|void
name|setupBulkLoadConfigsForCluster
parameter_list|(
name|Configuration
name|config
parameter_list|,
name|String
name|clusterReplicationId
parameter_list|)
throws|throws
name|Exception
block|{
name|config
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|REPLICATION_BULKLOAD_ENABLE_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|config
operator|.
name|set
argument_list|(
name|REPLICATION_CLUSTER_ID
argument_list|,
name|clusterReplicationId
argument_list|)
expr_stmt|;
name|File
name|sourceConfigFolder
init|=
name|testFolder
operator|.
name|newFolder
argument_list|(
name|clusterReplicationId
argument_list|)
decl_stmt|;
name|File
name|sourceConfigFile
init|=
operator|new
name|File
argument_list|(
name|sourceConfigFolder
operator|.
name|getAbsolutePath
argument_list|()
operator|+
literal|"/hbase-site.xml"
argument_list|)
decl_stmt|;
name|config
operator|.
name|writeXml
argument_list|(
operator|new
name|FileOutputStream
argument_list|(
name|sourceConfigFile
argument_list|)
argument_list|)
expr_stmt|;
name|config
operator|.
name|set
argument_list|(
name|REPLICATION_CONF_DIR
argument_list|,
name|testFolder
operator|.
name|getRoot
argument_list|()
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBulkLoadReplicationActiveActive
parameter_list|()
throws|throws
name|Exception
block|{
name|Table
name|peer1TestTable
init|=
name|UTIL1
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TestReplicationBase
operator|.
name|tableName
argument_list|)
decl_stmt|;
name|Table
name|peer2TestTable
init|=
name|UTIL2
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TestReplicationBase
operator|.
name|tableName
argument_list|)
decl_stmt|;
name|Table
name|peer3TestTable
init|=
name|UTIL3
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TestReplicationBase
operator|.
name|tableName
argument_list|)
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"001"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"v1"
argument_list|)
decl_stmt|;
name|assertBulkLoadConditions
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
name|value
argument_list|,
name|UTIL1
argument_list|,
name|peer1TestTable
argument_list|,
name|peer2TestTable
argument_list|,
name|peer3TestTable
argument_list|)
expr_stmt|;
name|row
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"002"
argument_list|)
expr_stmt|;
name|value
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"v2"
argument_list|)
expr_stmt|;
name|assertBulkLoadConditions
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
name|value
argument_list|,
name|UTIL2
argument_list|,
name|peer1TestTable
argument_list|,
name|peer2TestTable
argument_list|,
name|peer3TestTable
argument_list|)
expr_stmt|;
name|row
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"003"
argument_list|)
expr_stmt|;
name|value
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"v3"
argument_list|)
expr_stmt|;
name|assertBulkLoadConditions
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
name|value
argument_list|,
name|UTIL3
argument_list|,
name|peer1TestTable
argument_list|,
name|peer2TestTable
argument_list|,
name|peer3TestTable
argument_list|)
expr_stmt|;
comment|//Additional wait to make sure no extra bulk load happens
name|Thread
operator|.
name|sleep
argument_list|(
literal|400
argument_list|)
expr_stmt|;
comment|//We have 3 bulk load events (1 initiated on each cluster).
comment|//Each event gets 3 counts (the originator cluster, plus the two peers),
comment|//so BULK_LOADS_COUNT expected value is 3 * 3 = 9.
name|assertEquals
argument_list|(
literal|9
argument_list|,
name|BULK_LOADS_COUNT
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|assertBulkLoadConditions
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|HBaseTestingUtility
name|utility
parameter_list|,
name|Table
modifier|...
name|tables
parameter_list|)
throws|throws
name|Exception
block|{
name|BULK_LOAD_LATCH
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|bulkLoadOnCluster
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
name|value
argument_list|,
name|utility
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|BULK_LOAD_LATCH
operator|.
name|await
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
argument_list|)
expr_stmt|;
name|assertTableHasValue
argument_list|(
name|tables
index|[
literal|0
index|]
argument_list|,
name|row
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|assertTableHasValue
argument_list|(
name|tables
index|[
literal|1
index|]
argument_list|,
name|row
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|assertTableHasValue
argument_list|(
name|tables
index|[
literal|2
index|]
argument_list|,
name|row
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|bulkLoadOnCluster
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|HBaseTestingUtility
name|cluster
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|bulkLoadFilePath
init|=
name|createHFileForFamilies
argument_list|(
name|row
argument_list|,
name|value
argument_list|,
name|cluster
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|copyToHdfs
argument_list|(
name|bulkLoadFilePath
argument_list|,
name|cluster
operator|.
name|getDFSCluster
argument_list|()
argument_list|)
expr_stmt|;
name|BulkLoadHFilesTool
name|bulkLoadHFilesTool
init|=
operator|new
name|BulkLoadHFilesTool
argument_list|(
name|cluster
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|bulkLoadHFilesTool
operator|.
name|bulkLoad
argument_list|(
name|tableName
argument_list|,
name|BULK_LOAD_BASE_DIR
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|copyToHdfs
parameter_list|(
name|String
name|bulkLoadFilePath
parameter_list|,
name|MiniDFSCluster
name|cluster
parameter_list|)
throws|throws
name|Exception
block|{
name|Path
name|bulkLoadDir
init|=
operator|new
name|Path
argument_list|(
name|BULK_LOAD_BASE_DIR
argument_list|,
literal|"f"
argument_list|)
decl_stmt|;
name|cluster
operator|.
name|getFileSystem
argument_list|()
operator|.
name|mkdirs
argument_list|(
name|bulkLoadDir
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|getFileSystem
argument_list|()
operator|.
name|copyFromLocalFile
argument_list|(
operator|new
name|Path
argument_list|(
name|bulkLoadFilePath
argument_list|)
argument_list|,
name|bulkLoadDir
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|assertTableHasValue
parameter_list|(
name|Table
name|table
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|Exception
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|Result
name|result
init|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|advance
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|value
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|result
operator|.
name|value
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|assertTableNoValue
parameter_list|(
name|Table
name|table
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|Exception
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|Result
name|result
init|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|createHFileForFamilies
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Configuration
name|clusterConfig
parameter_list|)
throws|throws
name|IOException
block|{
name|CellBuilder
name|cellBuilder
init|=
name|CellBuilderFactory
operator|.
name|create
argument_list|(
name|CellBuilderType
operator|.
name|DEEP_COPY
argument_list|)
decl_stmt|;
name|cellBuilder
operator|.
name|setRow
argument_list|(
name|row
argument_list|)
operator|.
name|setFamily
argument_list|(
name|TestReplicationBase
operator|.
name|famName
argument_list|)
operator|.
name|setQualifier
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
argument_list|)
operator|.
name|setValue
argument_list|(
name|value
argument_list|)
operator|.
name|setType
argument_list|(
name|Cell
operator|.
name|Type
operator|.
name|Put
argument_list|)
expr_stmt|;
name|HFile
operator|.
name|WriterFactory
name|hFileFactory
init|=
name|HFile
operator|.
name|getWriterFactoryNoCache
argument_list|(
name|clusterConfig
argument_list|)
decl_stmt|;
comment|// TODO We need a way to do this without creating files
name|File
name|hFileLocation
init|=
name|testFolder
operator|.
name|newFile
argument_list|()
decl_stmt|;
name|FSDataOutputStream
name|out
init|=
operator|new
name|FSDataOutputStream
argument_list|(
operator|new
name|FileOutputStream
argument_list|(
name|hFileLocation
argument_list|)
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
name|hFileFactory
operator|.
name|withOutputStream
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|hFileFactory
operator|.
name|withFileContext
argument_list|(
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|HFile
operator|.
name|Writer
name|writer
init|=
name|hFileFactory
operator|.
name|create
argument_list|()
decl_stmt|;
try|try
block|{
name|writer
operator|.
name|append
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|cellBuilder
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|hFileLocation
operator|.
name|getAbsoluteFile
argument_list|()
operator|.
name|getAbsolutePath
argument_list|()
return|;
block|}
specifier|public
specifier|static
class|class
name|BulkReplicationTestObserver
implements|implements
name|RegionCoprocessor
block|{
name|String
name|clusterName
decl_stmt|;
name|AtomicInteger
name|bulkLoadCounts
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|RegionObserver
argument_list|>
name|getRegionObserver
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
operator|new
name|RegionObserver
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|postBulkLoadHFile
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|>
name|stagingFamilyPaths
parameter_list|,
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Path
argument_list|>
argument_list|>
name|finalPaths
parameter_list|)
throws|throws
name|IOException
block|{
name|BULK_LOAD_LATCH
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|BULK_LOADS_COUNT
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Another file bulk loaded. Total for {}: {}"
argument_list|,
name|clusterName
argument_list|,
name|bulkLoadCounts
operator|.
name|addAndGet
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

