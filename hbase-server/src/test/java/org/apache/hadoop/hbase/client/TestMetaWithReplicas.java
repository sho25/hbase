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
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Set
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
name|Abortable
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
name|ClusterMetrics
operator|.
name|Option
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
name|MetaTableAccessor
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
name|TableNotFoundException
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
name|master
operator|.
name|assignment
operator|.
name|AssignmentManager
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
name|master
operator|.
name|assignment
operator|.
name|AssignmentTestingUtil
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|testclassification
operator|.
name|LargeTests
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
name|LoadBalancerTracker
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
name|MetaTableLocator
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
name|hadoop
operator|.
name|hbase
operator|.
name|zookeeper
operator|.
name|ZKWatcher
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
name|ZNodePaths
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
comment|/**  * Tests the scenarios where replicas are enabled for the meta table  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestMetaWithReplicas
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
name|TestMetaWithReplicas
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TestMetaWithReplicas
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|REGIONSERVERS_COUNT
init|=
literal|3
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
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"zookeeper.session.timeout"
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|META_REPLICAS_NUM
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|StorefileRefresherChore
operator|.
name|REGIONSERVER_STOREFILE_REFRESH_PERIOD
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|REGIONSERVERS_COUNT
argument_list|)
expr_stmt|;
name|AssignmentManager
name|am
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|ServerName
argument_list|>
name|sns
init|=
operator|new
name|HashSet
argument_list|<
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
name|ServerName
name|hbaseMetaServerName
init|=
name|MetaTableLocator
operator|.
name|getMetaRegionLocation
argument_list|(
name|TEST_UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"HBASE:META DEPLOY: on "
operator|+
name|hbaseMetaServerName
argument_list|)
expr_stmt|;
name|sns
operator|.
name|add
argument_list|(
name|hbaseMetaServerName
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|replicaId
init|=
literal|1
init|;
name|replicaId
operator|<
literal|3
condition|;
name|replicaId
operator|++
control|)
block|{
name|RegionInfo
name|h
init|=
name|RegionReplicaUtil
operator|.
name|getRegionInfoForReplica
argument_list|(
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|replicaId
argument_list|)
decl_stmt|;
name|AssignmentTestingUtil
operator|.
name|waitForAssignment
argument_list|(
name|am
argument_list|,
name|h
argument_list|)
expr_stmt|;
name|ServerName
name|sn
init|=
name|am
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionServerOfRegion
argument_list|(
name|h
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|sn
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"HBASE:META DEPLOY: "
operator|+
name|h
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" on "
operator|+
name|sn
argument_list|)
expr_stmt|;
name|sns
operator|.
name|add
argument_list|(
name|sn
argument_list|)
expr_stmt|;
block|}
comment|// Fun. All meta region replicas have ended up on the one server. This will cause this test
comment|// to fail ... sometimes.
if|if
condition|(
name|sns
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|int
name|count
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getLiveRegionServerThreads
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"count="
operator|+
name|count
argument_list|,
name|count
operator|==
name|REGIONSERVERS_COUNT
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"All hbase:meta replicas are on the one server; moving hbase:meta: "
operator|+
name|sns
argument_list|)
expr_stmt|;
name|int
name|metaServerIndex
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getServerWithMeta
argument_list|()
decl_stmt|;
name|int
name|newServerIndex
init|=
name|metaServerIndex
decl_stmt|;
while|while
condition|(
name|newServerIndex
operator|==
name|metaServerIndex
condition|)
block|{
name|newServerIndex
operator|=
operator|(
name|newServerIndex
operator|+
literal|1
operator|)
operator|%
name|REGIONSERVERS_COUNT
expr_stmt|;
block|}
name|assertNotEquals
argument_list|(
name|metaServerIndex
argument_list|,
name|newServerIndex
argument_list|)
expr_stmt|;
name|ServerName
name|destinationServerName
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
name|newServerIndex
argument_list|)
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|ServerName
name|metaServerName
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
name|metaServerIndex
argument_list|)
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|assertNotEquals
argument_list|(
name|destinationServerName
argument_list|,
name|metaServerName
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|move
argument_list|(
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|destinationServerName
argument_list|)
expr_stmt|;
block|}
comment|// Disable the balancer
name|LoadBalancerTracker
name|l
init|=
operator|new
name|LoadBalancerTracker
argument_list|(
name|TEST_UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
operator|new
name|Abortable
argument_list|()
block|{
name|AtomicBoolean
name|aborted
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
name|aborted
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|aborted
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
decl_stmt|;
name|l
operator|.
name|setBalancerOn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"All meta replicas assigned"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
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
annotation|@
name|Test
specifier|public
name|void
name|testMetaHTDReplicaCount
parameter_list|()
throws|throws
name|Exception
block|{
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getDescriptor
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
operator|.
name|getRegionReplication
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testZookeeperNodesForReplicas
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Checks all the znodes exist when meta's replicas are enabled
name|ZKWatcher
name|zkw
init|=
name|TEST_UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|String
name|baseZNode
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_ZOOKEEPER_ZNODE_PARENT
argument_list|)
decl_stmt|;
name|String
name|primaryMetaZnode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|baseZNode
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.metaserver"
argument_list|,
literal|"meta-region-server"
argument_list|)
argument_list|)
decl_stmt|;
comment|// check that the data in the znode is parseable (this would also mean the znode exists)
name|byte
index|[]
name|data
init|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|primaryMetaZnode
argument_list|)
decl_stmt|;
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|data
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|String
name|secZnode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|baseZNode
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.metaserver"
argument_list|,
literal|"meta-region-server"
argument_list|)
operator|+
literal|"-"
operator|+
name|i
argument_list|)
decl_stmt|;
name|String
name|str
init|=
name|zkw
operator|.
name|getZNodePaths
argument_list|()
operator|.
name|getZNodeForReplica
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|str
operator|.
name|equals
argument_list|(
name|secZnode
argument_list|)
argument_list|)
expr_stmt|;
comment|// check that the data in the znode is parseable (this would also mean the znode exists)
name|data
operator|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|secZnode
argument_list|)
expr_stmt|;
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testShutdownHandling
parameter_list|()
throws|throws
name|Exception
block|{
comment|// This test creates a table, flushes the meta (with 3 replicas), kills the
comment|// server holding the primary meta replica. Then it does a put/get into/from
comment|// the test table. The put/get operations would use the replicas to locate the
comment|// location of the test table's region
name|shutdownMetaAndDoValidations
argument_list|(
name|TEST_UTIL
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|shutdownMetaAndDoValidations
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|)
throws|throws
name|Exception
block|{
comment|// This test creates a table, flushes the meta (with 3 replicas), kills the
comment|// server holding the primary meta replica. Then it does a put/get into/from
comment|// the test table. The put/get operations would use the replicas to locate the
comment|// location of the test table's region
name|ZKWatcher
name|zkw
init|=
name|util
operator|.
name|getZooKeeperWatcher
argument_list|()
decl_stmt|;
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
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|USE_META_REPLICAS
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|String
name|baseZNode
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_ZOOKEEPER_ZNODE_PARENT
argument_list|)
decl_stmt|;
name|String
name|primaryMetaZnode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|baseZNode
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.metaserver"
argument_list|,
literal|"meta-region-server"
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|data
init|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|primaryMetaZnode
argument_list|)
decl_stmt|;
name|ServerName
name|primary
init|=
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|data
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Primary="
operator|+
name|primary
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|TableName
name|TABLE
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testShutdownHandling"
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|FAMILIES
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
block|}
decl_stmt|;
if|if
condition|(
name|util
operator|.
name|getAdmin
argument_list|()
operator|.
name|tableExists
argument_list|(
name|TABLE
argument_list|)
condition|)
block|{
name|util
operator|.
name|getAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|TABLE
argument_list|)
expr_stmt|;
name|util
operator|.
name|getAdmin
argument_list|()
operator|.
name|deleteTable
argument_list|(
name|TABLE
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|ServerName
name|master
init|=
literal|null
decl_stmt|;
try|try
init|(
name|Connection
name|c
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|)
block|{
try|try
init|(
name|Table
name|htable
init|=
name|util
operator|.
name|createTable
argument_list|(
name|TABLE
argument_list|,
name|FAMILIES
argument_list|)
init|)
block|{
name|util
operator|.
name|getAdmin
argument_list|()
operator|.
name|flush
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|conf
operator|.
name|getInt
argument_list|(
name|StorefileRefresherChore
operator|.
name|REGIONSERVER_STOREFILE_REFRESH_PERIOD
argument_list|,
literal|30000
argument_list|)
operator|*
literal|6
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regions
init|=
name|MetaTableAccessor
operator|.
name|getTableRegions
argument_list|(
name|c
argument_list|,
name|TABLE
argument_list|)
decl_stmt|;
name|HRegionLocation
name|hrl
init|=
name|MetaTableAccessor
operator|.
name|getRegionLocation
argument_list|(
name|c
argument_list|,
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
comment|// Ensure that the primary server for test table is not the same one as the primary
comment|// of the meta region since we will be killing the srv holding the meta's primary...
comment|// We want to be able to write to the test table even when the meta is not present ..
comment|// If the servers are the same, then move the test table's region out of the server
comment|// to another random server
if|if
condition|(
name|hrl
operator|.
name|getServerName
argument_list|()
operator|.
name|equals
argument_list|(
name|primary
argument_list|)
condition|)
block|{
name|util
operator|.
name|getAdmin
argument_list|()
operator|.
name|move
argument_list|(
name|hrl
operator|.
name|getRegion
argument_list|()
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
expr_stmt|;
comment|// wait for the move to complete
do|do
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|hrl
operator|=
name|MetaTableAccessor
operator|.
name|getRegionLocation
argument_list|(
name|c
argument_list|,
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|primary
operator|.
name|equals
argument_list|(
name|hrl
operator|.
name|getServerName
argument_list|()
argument_list|)
condition|)
do|;
name|util
operator|.
name|getAdmin
argument_list|()
operator|.
name|flush
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|conf
operator|.
name|getInt
argument_list|(
name|StorefileRefresherChore
operator|.
name|REGIONSERVER_STOREFILE_REFRESH_PERIOD
argument_list|,
literal|30000
argument_list|)
operator|*
literal|3
argument_list|)
expr_stmt|;
block|}
comment|// Ensure all metas are not on same hbase:meta replica=0 server!
name|master
operator|=
name|util
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|.
name|getClusterMetrics
argument_list|()
operator|.
name|getMasterName
argument_list|()
expr_stmt|;
comment|// kill the master so that regionserver recovery is not triggered at all
comment|// for the meta server
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping master="
operator|+
name|master
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|util
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|.
name|stopMaster
argument_list|(
name|master
argument_list|)
expr_stmt|;
name|util
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|.
name|waitForMasterToStop
argument_list|(
name|master
argument_list|,
literal|60000
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Master "
operator|+
name|master
operator|+
literal|" stopped!"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|master
operator|.
name|equals
argument_list|(
name|primary
argument_list|)
condition|)
block|{
name|util
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|.
name|killRegionServer
argument_list|(
name|primary
argument_list|)
expr_stmt|;
name|util
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|.
name|waitForRegionServerToStop
argument_list|(
name|primary
argument_list|,
literal|60000
argument_list|)
expr_stmt|;
block|}
name|c
operator|.
name|clearRegionLocationCache
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Running GETs"
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|htable
init|=
name|c
operator|.
name|getTable
argument_list|(
name|TABLE
argument_list|)
init|)
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
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
name|row
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|BufferedMutator
name|m
init|=
name|c
operator|.
name|getBufferedMutator
argument_list|(
name|TABLE
argument_list|)
decl_stmt|;
name|m
operator|.
name|mutate
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|m
operator|.
name|flush
argument_list|()
expr_stmt|;
comment|// Try to do a get of the row that was just put
name|Result
name|r
init|=
name|htable
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|row
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|r
operator|.
name|getRow
argument_list|()
argument_list|,
name|row
argument_list|)
argument_list|)
expr_stmt|;
comment|// now start back the killed servers and disable use of replicas. That would mean
comment|// calls go to the primary
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting Master"
argument_list|)
expr_stmt|;
name|util
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|.
name|startMaster
argument_list|(
name|master
operator|.
name|getHostname
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|util
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|.
name|startRegionServer
argument_list|(
name|primary
operator|.
name|getHostname
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|util
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|.
name|waitForActiveAndReadyMaster
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Master active!"
argument_list|)
expr_stmt|;
name|c
operator|.
name|clearRegionLocationCache
argument_list|()
expr_stmt|;
block|}
block|}
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|USE_META_REPLICAS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Running GETs no replicas"
argument_list|)
expr_stmt|;
try|try
init|(
name|Connection
name|c
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|)
block|{
try|try
init|(
name|Table
name|htable
init|=
name|c
operator|.
name|getTable
argument_list|(
name|TABLE
argument_list|)
init|)
block|{
name|Result
name|r
init|=
name|htable
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|row
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|r
operator|.
name|getRow
argument_list|()
argument_list|,
name|row
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAccessingUnknownTables
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|USE_META_REPLICAS
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
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
name|TableNotFoundException
name|t
parameter_list|)
block|{
return|return;
block|}
name|fail
argument_list|(
literal|"Expected TableNotFoundException"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMetaAddressChange
parameter_list|()
throws|throws
name|Exception
block|{
comment|// checks that even when the meta's location changes, the various
comment|// caches update themselves. Uses the master operations to test
comment|// this
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|ZKWatcher
name|zkw
init|=
name|TEST_UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
decl_stmt|;
name|String
name|baseZNode
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_ZOOKEEPER_ZNODE_PARENT
argument_list|)
decl_stmt|;
name|String
name|primaryMetaZnode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|baseZNode
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.metaserver"
argument_list|,
literal|"meta-region-server"
argument_list|)
argument_list|)
decl_stmt|;
comment|// check that the data in the znode is parseable (this would also mean the znode exists)
name|byte
index|[]
name|data
init|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|primaryMetaZnode
argument_list|)
decl_stmt|;
name|ServerName
name|currentServer
init|=
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|data
argument_list|)
decl_stmt|;
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|liveServers
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getClusterMetrics
argument_list|(
name|EnumSet
operator|.
name|of
argument_list|(
name|Option
operator|.
name|LIVE_SERVERS
argument_list|)
argument_list|)
operator|.
name|getLiveServerMetrics
argument_list|()
operator|.
name|keySet
argument_list|()
decl_stmt|;
name|ServerName
name|moveToServer
init|=
literal|null
decl_stmt|;
for|for
control|(
name|ServerName
name|s
range|:
name|liveServers
control|)
block|{
if|if
condition|(
operator|!
name|currentServer
operator|.
name|equals
argument_list|(
name|s
argument_list|)
condition|)
block|{
name|moveToServer
operator|=
name|s
expr_stmt|;
block|}
block|}
name|assertNotNull
argument_list|(
name|moveToServer
argument_list|)
expr_stmt|;
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
literal|"f"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|tableExists
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|move
argument_list|(
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|moveToServer
argument_list|)
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
name|assertNotEquals
argument_list|(
name|currentServer
argument_list|,
name|moveToServer
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"CurrentServer="
operator|+
name|currentServer
operator|+
literal|", moveToServer="
operator|+
name|moveToServer
argument_list|)
expr_stmt|;
specifier|final
name|int
name|max
init|=
literal|10000
decl_stmt|;
do|do
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|data
operator|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|primaryMetaZnode
argument_list|)
expr_stmt|;
name|currentServer
operator|=
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
do|while
condition|(
operator|!
name|moveToServer
operator|.
name|equals
argument_list|(
name|currentServer
argument_list|)
operator|&&
name|i
operator|<
name|max
condition|)
do|;
comment|//wait for 10 seconds overall
name|assertNotEquals
argument_list|(
name|max
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|isTableDisabled
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testShutdownOfReplicaHolder
parameter_list|()
throws|throws
name|Exception
block|{
comment|// checks that the when the server holding meta replica is shut down, the meta replica
comment|// can be recovered
try|try
init|(
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
init|;
name|RegionLocator
name|locator
operator|=
name|conn
operator|.
name|getRegionLocator
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
init|)
block|{
name|HRegionLocation
name|hrl
init|=
name|locator
operator|.
name|getRegionLocations
argument_list|(
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
literal|true
argument_list|)
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|ServerName
name|oldServer
init|=
name|hrl
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|.
name|killRegionServer
argument_list|(
name|oldServer
argument_list|)
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
do|do
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Waiting for the replica "
operator|+
name|hrl
operator|.
name|getRegion
argument_list|()
operator|+
literal|" to come up"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
comment|// wait for the detection/recovery
name|hrl
operator|=
name|locator
operator|.
name|getRegionLocations
argument_list|(
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
literal|true
argument_list|)
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
do|while
condition|(
operator|(
name|hrl
operator|==
literal|null
operator|||
name|hrl
operator|.
name|getServerName
argument_list|()
operator|.
name|equals
argument_list|(
name|oldServer
argument_list|)
operator|)
operator|&&
name|i
operator|<
literal|3
condition|)
do|;
name|assertNotEquals
argument_list|(
literal|3
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

