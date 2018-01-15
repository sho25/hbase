begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|assertFalse
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
name|RegionInfo
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
name|coordination
operator|.
name|ZkSplitLogWorkerCoordination
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
name|HMaster
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
name|LoadBalancer
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
name|balancer
operator|.
name|SimpleLoadBalancer
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
name|FSUtils
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
name|zookeeper
operator|.
name|KeeperException
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
name|MiscTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestZooKeeper
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
name|TestZooKeeper
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
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Test we can first start the ZK cluster by itself
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|.
name|startMiniDFSCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|ZK_SESSION_TIMEOUT
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setClass
argument_list|(
name|HConstants
operator|.
name|HBASE_MASTER_LOADBALANCER_CLASS
argument_list|,
name|MockLoadBalancer
operator|.
name|class
argument_list|,
name|LoadBalancer
operator|.
name|class
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
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniHBaseCluster
argument_list|(
literal|2
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|after
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
comment|// Some regionserver could fail to delete its znode.
comment|// So shutdown could hang. Let's kill them all instead.
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|killAll
argument_list|()
expr_stmt|;
comment|// Still need to clean things up
name|TEST_UTIL
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
operator|.
name|delete
argument_list|(
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|TEST_UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
literal|"/hbase"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|120000
argument_list|)
specifier|public
name|void
name|testRegionServerSessionExpired
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting "
operator|+
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|expireRegionServerSession
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|testSanity
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testMasterSessionExpired
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting "
operator|+
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|expireMasterSession
argument_list|()
expr_stmt|;
name|testSanity
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Master recovery when the znode already exists. Internally, this    *  test differs from {@link #testMasterSessionExpired} because here    *  the master znode will exist in ZK.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testMasterZKSessionRecoveryFailure
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting "
operator|+
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|HMaster
name|m
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|m
operator|.
name|abort
argument_list|(
literal|"Test recovery from zk session expired"
argument_list|,
operator|new
name|KeeperException
operator|.
name|SessionExpiredException
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|m
operator|.
name|isStopped
argument_list|()
argument_list|)
expr_stmt|;
comment|// Master doesn't recover any more
name|testSanity
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Make sure we can use the cluster    */
specifier|private
name|void
name|testSanity
parameter_list|(
specifier|final
name|String
name|testName
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|tableName
init|=
name|testName
operator|+
literal|"_"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|TableDescriptor
name|desc
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
operator|.
name|addColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
literal|"fam"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
try|try
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|desc
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
block|}
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
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
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
literal|"testrow"
argument_list|)
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
literal|"fam"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testdata"
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Putting table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Tests that the master does not call retainAssignment after recovery from expired zookeeper    * session. Without the HBASE-6046 fix master always tries to assign all the user regions by    * calling retainAssignment.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testRegionAssignmentAfterMasterRecoveryDueToZKExpiry
parameter_list|()
throws|throws
name|Exception
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|cluster
operator|.
name|startRegionServer
argument_list|()
expr_stmt|;
name|cluster
operator|.
name|waitForActiveAndReadyMaster
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
name|HMaster
name|m
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
specifier|final
name|ZKWatcher
name|zkw
init|=
name|m
operator|.
name|getZooKeeper
argument_list|()
decl_stmt|;
comment|// now the cluster is up. So assign some regions.
try|try
init|(
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|byte
index|[]
index|[]
name|SPLIT_KEYS
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
literal|"a"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"d"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"e"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"g"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"h"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"i"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"j"
argument_list|)
block|}
decl_stmt|;
name|TableDescriptor
name|htd
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
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
operator|.
name|addColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|,
name|SPLIT_KEYS
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilNoRegionsInTransition
argument_list|(
literal|60000
argument_list|)
expr_stmt|;
name|m
operator|.
name|getZooKeeper
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
name|MockLoadBalancer
operator|.
name|retainAssignCalled
operator|=
literal|false
expr_stmt|;
specifier|final
name|int
name|expectedNumOfListeners
init|=
name|countPermanentListeners
argument_list|(
name|zkw
argument_list|)
decl_stmt|;
name|m
operator|.
name|abort
argument_list|(
literal|"Test recovery from zk session expired"
argument_list|,
operator|new
name|KeeperException
operator|.
name|SessionExpiredException
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|m
operator|.
name|isStopped
argument_list|()
argument_list|)
expr_stmt|;
comment|// Master doesn't recover any more
comment|// The recovered master should not call retainAssignment, as it is not a
comment|// clean startup.
name|assertFalse
argument_list|(
literal|"Retain assignment should not be called"
argument_list|,
name|MockLoadBalancer
operator|.
name|retainAssignCalled
argument_list|)
expr_stmt|;
comment|// number of listeners should be same as the value before master aborted
comment|// wait for new master is initialized
name|cluster
operator|.
name|waitForActiveAndReadyMaster
argument_list|(
literal|120000
argument_list|)
expr_stmt|;
specifier|final
name|HMaster
name|newMaster
init|=
name|cluster
operator|.
name|getMasterThread
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedNumOfListeners
argument_list|,
name|countPermanentListeners
argument_list|(
name|newMaster
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Count listeners in zkw excluding listeners, that belongs to workers or other    * temporary processes.    */
specifier|private
name|int
name|countPermanentListeners
parameter_list|(
name|ZKWatcher
name|watcher
parameter_list|)
block|{
return|return
name|countListeners
argument_list|(
name|watcher
argument_list|,
name|ZkSplitLogWorkerCoordination
operator|.
name|class
argument_list|)
return|;
block|}
comment|/**    * Count listeners in zkw excluding provided classes    */
specifier|private
name|int
name|countListeners
parameter_list|(
name|ZKWatcher
name|watcher
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
modifier|...
name|exclude
parameter_list|)
block|{
name|int
name|cnt
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Object
name|o
range|:
name|watcher
operator|.
name|getListeners
argument_list|()
control|)
block|{
name|boolean
name|skip
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
argument_list|>
name|aClass
range|:
name|exclude
control|)
block|{
if|if
condition|(
name|aClass
operator|.
name|isAssignableFrom
argument_list|(
name|o
operator|.
name|getClass
argument_list|()
argument_list|)
condition|)
block|{
name|skip
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|skip
condition|)
block|{
name|cnt
operator|+=
literal|1
expr_stmt|;
block|}
block|}
return|return
name|cnt
return|;
block|}
comment|/**    * Tests whether the logs are split when master recovers from a expired zookeeper session and an    * RS goes down.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testLogSplittingAfterMasterRecoveryDueToZKExpiry
parameter_list|()
throws|throws
name|Exception
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|cluster
operator|.
name|startRegionServer
argument_list|()
expr_stmt|;
name|HMaster
name|m
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
comment|// now the cluster is up. So assign some regions.
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|Table
name|table
init|=
literal|null
decl_stmt|;
try|try
block|{
name|byte
index|[]
index|[]
name|SPLIT_KEYS
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
literal|"1"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"2"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"3"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"4"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"5"
argument_list|)
block|}
decl_stmt|;
name|TableDescriptor
name|htd
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
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
operator|.
name|addColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
literal|"col"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|,
name|SPLIT_KEYS
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilNoRegionsInTransition
argument_list|(
literal|60000
argument_list|)
expr_stmt|;
name|table
operator|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|Put
name|p
decl_stmt|;
name|int
name|numberOfPuts
decl_stmt|;
for|for
control|(
name|numberOfPuts
operator|=
literal|0
init|;
name|numberOfPuts
operator|<
literal|6
condition|;
name|numberOfPuts
operator|++
control|)
block|{
name|p
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|numberOfPuts
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ql"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
operator|+
name|numberOfPuts
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|m
operator|.
name|getZooKeeper
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
name|m
operator|.
name|abort
argument_list|(
literal|"Test recovery from zk session expired"
argument_list|,
operator|new
name|KeeperException
operator|.
name|SessionExpiredException
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|m
operator|.
name|isStopped
argument_list|()
argument_list|)
expr_stmt|;
comment|// Master doesn't recover any more
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|abort
argument_list|(
literal|"Aborting"
argument_list|)
expr_stmt|;
comment|// Without patch for HBASE-6046 this test case will always timeout
comment|// with patch the test case should pass.
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|int
name|numberOfRows
init|=
literal|0
decl_stmt|;
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|Result
index|[]
name|result
init|=
name|scanner
operator|.
name|next
argument_list|(
literal|1
argument_list|)
decl_stmt|;
while|while
condition|(
name|result
operator|!=
literal|null
operator|&&
name|result
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|numberOfRows
operator|++
expr_stmt|;
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Number of rows should be equal to number of puts."
argument_list|,
name|numberOfPuts
argument_list|,
name|numberOfRows
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|static
class|class
name|MockLoadBalancer
extends|extends
name|SimpleLoadBalancer
block|{
specifier|static
name|boolean
name|retainAssignCalled
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
name|retainAssignment
parameter_list|(
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|ServerName
argument_list|>
name|regions
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
parameter_list|)
throws|throws
name|HBaseIOException
block|{
name|retainAssignCalled
operator|=
literal|true
expr_stmt|;
return|return
name|super
operator|.
name|retainAssignment
argument_list|(
name|regions
argument_list|,
name|servers
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

