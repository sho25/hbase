begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|EnumSet
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
name|CompletableFuture
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
name|Waiter
operator|.
name|Predicate
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
name|AsyncAdmin
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
name|AsyncConnection
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
name|coprocessor
operator|.
name|CoprocessorHost
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
name|MasterCoprocessor
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
name|MasterCoprocessorEnvironment
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
name|MasterObserver
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
name|testclassification
operator|.
name|SmallTests
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
operator|.
name|MasterThread
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
operator|.
name|RegionServerThread
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
name|Assert
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

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestClientClusterMetrics
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
name|TestClientClusterMetrics
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|UTIL
decl_stmt|;
specifier|private
specifier|static
name|Admin
name|ADMIN
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|int
name|SLAVES
init|=
literal|5
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|int
name|MASTERS
init|=
literal|3
decl_stmt|;
specifier|private
specifier|static
name|MiniHBaseCluster
name|CLUSTER
decl_stmt|;
specifier|private
specifier|static
name|HRegionServer
name|DEAD
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
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|MyObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
name|MASTERS
argument_list|,
name|SLAVES
argument_list|)
expr_stmt|;
name|CLUSTER
operator|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
expr_stmt|;
name|CLUSTER
operator|.
name|waitForActiveAndReadyMaster
argument_list|()
expr_stmt|;
name|ADMIN
operator|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
comment|// Kill one region server
name|List
argument_list|<
name|RegionServerThread
argument_list|>
name|rsts
init|=
name|CLUSTER
operator|.
name|getLiveRegionServerThreads
argument_list|()
decl_stmt|;
name|RegionServerThread
name|rst
init|=
name|rsts
operator|.
name|get
argument_list|(
name|rsts
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
decl_stmt|;
name|DEAD
operator|=
name|rst
operator|.
name|getRegionServer
argument_list|()
expr_stmt|;
name|DEAD
operator|.
name|stop
argument_list|(
literal|"Test dead servers metrics"
argument_list|)
expr_stmt|;
while|while
condition|(
name|rst
operator|.
name|isAlive
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|500
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDefaults
parameter_list|()
throws|throws
name|Exception
block|{
name|ClusterMetrics
name|origin
init|=
name|ADMIN
operator|.
name|getClusterMetrics
argument_list|()
decl_stmt|;
name|ClusterMetrics
name|defaults
init|=
name|ADMIN
operator|.
name|getClusterMetrics
argument_list|(
name|EnumSet
operator|.
name|allOf
argument_list|(
name|Option
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|origin
operator|.
name|getHBaseVersion
argument_list|()
argument_list|,
name|defaults
operator|.
name|getHBaseVersion
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|origin
operator|.
name|getClusterId
argument_list|()
argument_list|,
name|defaults
operator|.
name|getClusterId
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|origin
operator|.
name|getAverageLoad
argument_list|()
argument_list|,
name|defaults
operator|.
name|getAverageLoad
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|origin
operator|.
name|getBackupMasterNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|defaults
operator|.
name|getBackupMasterNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|origin
operator|.
name|getDeadServerNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|defaults
operator|.
name|getDeadServerNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|origin
operator|.
name|getRegionCount
argument_list|()
argument_list|,
name|defaults
operator|.
name|getRegionCount
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|origin
operator|.
name|getLiveServerMetrics
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|defaults
operator|.
name|getLiveServerMetrics
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|origin
operator|.
name|getMasterInfoPort
argument_list|()
argument_list|,
name|defaults
operator|.
name|getMasterInfoPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAsyncClient
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|AsyncConnection
name|asyncConnect
init|=
name|ConnectionFactory
operator|.
name|createAsyncConnection
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
init|)
block|{
name|AsyncAdmin
name|asyncAdmin
init|=
name|asyncConnect
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|CompletableFuture
argument_list|<
name|ClusterMetrics
argument_list|>
name|originFuture
init|=
name|asyncAdmin
operator|.
name|getClusterMetrics
argument_list|()
decl_stmt|;
name|CompletableFuture
argument_list|<
name|ClusterMetrics
argument_list|>
name|defaultsFuture
init|=
name|asyncAdmin
operator|.
name|getClusterMetrics
argument_list|(
name|EnumSet
operator|.
name|allOf
argument_list|(
name|Option
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
name|ClusterMetrics
name|origin
init|=
name|originFuture
operator|.
name|get
argument_list|()
decl_stmt|;
name|ClusterMetrics
name|defaults
init|=
name|defaultsFuture
operator|.
name|get
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|origin
operator|.
name|getHBaseVersion
argument_list|()
argument_list|,
name|defaults
operator|.
name|getHBaseVersion
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|origin
operator|.
name|getClusterId
argument_list|()
argument_list|,
name|defaults
operator|.
name|getClusterId
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|origin
operator|.
name|getHBaseVersion
argument_list|()
argument_list|,
name|defaults
operator|.
name|getHBaseVersion
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|origin
operator|.
name|getClusterId
argument_list|()
argument_list|,
name|defaults
operator|.
name|getClusterId
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|origin
operator|.
name|getAverageLoad
argument_list|()
argument_list|,
name|defaults
operator|.
name|getAverageLoad
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|origin
operator|.
name|getBackupMasterNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|defaults
operator|.
name|getBackupMasterNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|origin
operator|.
name|getDeadServerNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|defaults
operator|.
name|getDeadServerNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|origin
operator|.
name|getRegionCount
argument_list|()
argument_list|,
name|defaults
operator|.
name|getRegionCount
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|origin
operator|.
name|getLiveServerMetrics
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|defaults
operator|.
name|getLiveServerMetrics
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|origin
operator|.
name|getMasterInfoPort
argument_list|()
argument_list|,
name|defaults
operator|.
name|getMasterInfoPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLiveAndDeadServersStatus
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Count the number of live regionservers
name|List
argument_list|<
name|RegionServerThread
argument_list|>
name|regionserverThreads
init|=
name|CLUSTER
operator|.
name|getLiveRegionServerThreads
argument_list|()
decl_stmt|;
name|int
name|numRs
init|=
literal|0
decl_stmt|;
name|int
name|len
init|=
name|regionserverThreads
operator|.
name|size
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
name|len
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|regionserverThreads
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|isAlive
argument_list|()
condition|)
block|{
name|numRs
operator|++
expr_stmt|;
block|}
block|}
comment|// Depending on the (random) order of unit execution we may run this unit before the
comment|// minicluster is fully up and recovered from the RS shutdown done during test init.
name|Waiter
operator|.
name|waitFor
argument_list|(
name|CLUSTER
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|10
operator|*
literal|1000
argument_list|,
literal|100
argument_list|,
operator|new
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
name|ClusterMetrics
name|metrics
init|=
name|ADMIN
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
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|metrics
argument_list|)
expr_stmt|;
return|return
name|metrics
operator|.
name|getRegionCount
argument_list|()
operator|>
literal|0
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// Retrieve live servers and dead servers info.
name|EnumSet
argument_list|<
name|Option
argument_list|>
name|options
init|=
name|EnumSet
operator|.
name|of
argument_list|(
name|Option
operator|.
name|LIVE_SERVERS
argument_list|,
name|Option
operator|.
name|DEAD_SERVERS
argument_list|)
decl_stmt|;
name|ClusterMetrics
name|metrics
init|=
name|ADMIN
operator|.
name|getClusterMetrics
argument_list|(
name|options
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|metrics
argument_list|)
expr_stmt|;
comment|// exclude a dead region server
name|Assert
operator|.
name|assertEquals
argument_list|(
name|SLAVES
operator|-
literal|1
argument_list|,
name|numRs
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|numRs
operator|+
literal|1
comment|/*Master*/
argument_list|,
name|metrics
operator|.
name|getLiveServerMetrics
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|metrics
operator|.
name|getRegionCount
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|metrics
operator|.
name|getDeadServerNames
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|metrics
operator|.
name|getDeadServerNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|ServerName
name|deadServerName
init|=
name|metrics
operator|.
name|getDeadServerNames
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DEAD
operator|.
name|getServerName
argument_list|()
argument_list|,
name|deadServerName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMasterAndBackupMastersStatus
parameter_list|()
throws|throws
name|Exception
block|{
comment|// get all the master threads
name|List
argument_list|<
name|MasterThread
argument_list|>
name|masterThreads
init|=
name|CLUSTER
operator|.
name|getMasterThreads
argument_list|()
decl_stmt|;
name|int
name|numActive
init|=
literal|0
decl_stmt|;
name|int
name|activeIndex
init|=
literal|0
decl_stmt|;
name|ServerName
name|activeName
init|=
literal|null
decl_stmt|;
name|HMaster
name|active
init|=
literal|null
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
name|masterThreads
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|masterThreads
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getMaster
argument_list|()
operator|.
name|isActiveMaster
argument_list|()
condition|)
block|{
name|numActive
operator|++
expr_stmt|;
name|activeIndex
operator|=
name|i
expr_stmt|;
name|active
operator|=
name|masterThreads
operator|.
name|get
argument_list|(
name|activeIndex
argument_list|)
operator|.
name|getMaster
argument_list|()
expr_stmt|;
name|activeName
operator|=
name|active
operator|.
name|getServerName
argument_list|()
expr_stmt|;
block|}
block|}
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|active
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|numActive
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|MASTERS
argument_list|,
name|masterThreads
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Retrieve master and backup masters infos only.
name|EnumSet
argument_list|<
name|Option
argument_list|>
name|options
init|=
name|EnumSet
operator|.
name|of
argument_list|(
name|Option
operator|.
name|MASTER
argument_list|,
name|Option
operator|.
name|BACKUP_MASTERS
argument_list|)
decl_stmt|;
name|ClusterMetrics
name|metrics
init|=
name|ADMIN
operator|.
name|getClusterMetrics
argument_list|(
name|options
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|metrics
operator|.
name|getMasterName
argument_list|()
operator|.
name|equals
argument_list|(
name|activeName
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|MASTERS
operator|-
literal|1
argument_list|,
name|metrics
operator|.
name|getBackupMasterNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOtherStatusInfos
parameter_list|()
throws|throws
name|Exception
block|{
name|EnumSet
argument_list|<
name|Option
argument_list|>
name|options
init|=
name|EnumSet
operator|.
name|of
argument_list|(
name|Option
operator|.
name|MASTER_COPROCESSORS
argument_list|,
name|Option
operator|.
name|HBASE_VERSION
argument_list|,
name|Option
operator|.
name|CLUSTER_ID
argument_list|,
name|Option
operator|.
name|BALANCER_ON
argument_list|)
decl_stmt|;
name|ClusterMetrics
name|metrics
init|=
name|ADMIN
operator|.
name|getClusterMetrics
argument_list|(
name|options
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|metrics
operator|.
name|getMasterCoprocessorNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|metrics
operator|.
name|getHBaseVersion
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|metrics
operator|.
name|getClusterId
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|metrics
operator|.
name|getAverageLoad
argument_list|()
operator|==
literal|0.0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|metrics
operator|.
name|getBalancerOn
argument_list|()
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
if|if
condition|(
name|ADMIN
operator|!=
literal|null
condition|)
block|{
name|ADMIN
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testObserver
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|preCount
init|=
name|MyObserver
operator|.
name|PRE_COUNT
operator|.
name|get
argument_list|()
decl_stmt|;
name|int
name|postCount
init|=
name|MyObserver
operator|.
name|POST_COUNT
operator|.
name|get
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ADMIN
operator|.
name|getClusterMetrics
argument_list|()
operator|.
name|getMasterCoprocessorNames
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|anyMatch
argument_list|(
name|s
lambda|->
name|s
operator|.
name|equals
argument_list|(
name|MyObserver
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|preCount
operator|+
literal|1
argument_list|,
name|MyObserver
operator|.
name|PRE_COUNT
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|postCount
operator|+
literal|1
argument_list|,
name|MyObserver
operator|.
name|POST_COUNT
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|MyObserver
implements|implements
name|MasterCoprocessor
implements|,
name|MasterObserver
block|{
specifier|private
specifier|static
specifier|final
name|AtomicInteger
name|PRE_COUNT
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|AtomicInteger
name|POST_COUNT
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|MasterObserver
argument_list|>
name|getMasterObserver
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preGetClusterMetrics
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{
name|PRE_COUNT
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postGetClusterMetrics
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|ClusterMetrics
name|metrics
parameter_list|)
throws|throws
name|IOException
block|{
name|POST_COUNT
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

