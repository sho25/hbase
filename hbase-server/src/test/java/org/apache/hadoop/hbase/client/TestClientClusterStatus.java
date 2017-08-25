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
name|CompletableFuture
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
name|ClusterStatus
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
name|ClusterStatus
operator|.
name|Options
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
name|MiniHBaseCluster
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
name|security
operator|.
name|User
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

begin_comment
comment|/**  * Test the ClusterStatus.  */
end_comment

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
name|TestClientClusterStatus
block|{
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
literal|"Test dead servers status"
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
name|ClusterStatus
name|origin
init|=
name|ADMIN
operator|.
name|getClusterStatus
argument_list|()
decl_stmt|;
name|ClusterStatus
name|defaults
init|=
name|ADMIN
operator|.
name|getClusterStatus
argument_list|(
name|Options
operator|.
name|getDefaultOptions
argument_list|()
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
name|assertTrue
argument_list|(
name|origin
operator|.
name|getAverageLoad
argument_list|()
operator|==
name|defaults
operator|.
name|getAverageLoad
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|origin
operator|.
name|getBackupMastersSize
argument_list|()
operator|==
name|defaults
operator|.
name|getBackupMastersSize
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|origin
operator|.
name|getDeadServersSize
argument_list|()
operator|==
name|defaults
operator|.
name|getDeadServersSize
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|origin
operator|.
name|getRegionsCount
argument_list|()
operator|==
name|defaults
operator|.
name|getRegionsCount
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|origin
operator|.
name|getServersSize
argument_list|()
operator|==
name|defaults
operator|.
name|getServersSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExclude
parameter_list|()
throws|throws
name|Exception
block|{
name|ClusterStatus
operator|.
name|Options
name|options
init|=
name|Options
operator|.
name|getDefaultOptions
argument_list|()
decl_stmt|;
comment|// Only retrieve master's coprocessors which are null in this test env.
name|options
operator|.
name|excludeHBaseVersion
argument_list|()
operator|.
name|excludeBackupMasters
argument_list|()
operator|.
name|excludeBalancerOn
argument_list|()
operator|.
name|excludeClusterId
argument_list|()
operator|.
name|excludeLiveServers
argument_list|()
operator|.
name|excludeDeadServers
argument_list|()
operator|.
name|excludeMaster
argument_list|()
operator|.
name|excludeRegionState
argument_list|()
expr_stmt|;
name|ClusterStatus
name|status
init|=
name|ADMIN
operator|.
name|getClusterStatus
argument_list|(
name|options
argument_list|)
decl_stmt|;
comment|// Other cluster status info should be either null or empty.
name|Assert
operator|.
name|assertTrue
argument_list|(
name|status
operator|.
name|getMasterCoprocessors
argument_list|()
operator|.
name|length
operator|==
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|status
operator|.
name|getHBaseVersion
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|status
operator|.
name|getBackupMasters
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|status
operator|.
name|getBalancerOn
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|status
operator|.
name|getClusterId
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|status
operator|.
name|getServers
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|status
operator|.
name|getDeadServerNames
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|status
operator|.
name|getMaster
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|status
operator|.
name|getBackupMasters
argument_list|()
operator|.
name|isEmpty
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
name|AsyncRegistry
name|registry
init|=
name|AsyncRegistryFactory
operator|.
name|getRegistry
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|AsyncConnectionImpl
name|asyncConnect
init|=
operator|new
name|AsyncConnectionImpl
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|registry
argument_list|,
name|registry
operator|.
name|getClusterId
argument_list|()
operator|.
name|get
argument_list|()
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
argument_list|)
decl_stmt|;
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
name|ClusterStatus
argument_list|>
name|originFuture
init|=
name|asyncAdmin
operator|.
name|getClusterStatus
argument_list|()
decl_stmt|;
name|CompletableFuture
argument_list|<
name|ClusterStatus
argument_list|>
name|defaultsFuture
init|=
name|asyncAdmin
operator|.
name|getClusterStatus
argument_list|(
name|Options
operator|.
name|getDefaultOptions
argument_list|()
argument_list|)
decl_stmt|;
name|ClusterStatus
name|origin
init|=
name|originFuture
operator|.
name|get
argument_list|()
decl_stmt|;
name|ClusterStatus
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
name|assertTrue
argument_list|(
name|origin
operator|.
name|getAverageLoad
argument_list|()
operator|==
name|defaults
operator|.
name|getAverageLoad
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|origin
operator|.
name|getBackupMastersSize
argument_list|()
operator|==
name|defaults
operator|.
name|getBackupMastersSize
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|origin
operator|.
name|getDeadServersSize
argument_list|()
operator|==
name|defaults
operator|.
name|getDeadServersSize
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|origin
operator|.
name|getRegionsCount
argument_list|()
operator|==
name|defaults
operator|.
name|getRegionsCount
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|origin
operator|.
name|getServersSize
argument_list|()
operator|==
name|defaults
operator|.
name|getServersSize
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|asyncConnect
operator|!=
literal|null
condition|)
block|{
name|asyncConnect
operator|.
name|close
argument_list|()
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
comment|// Retrieve live servers and dead servers info.
name|ClusterStatus
operator|.
name|Options
name|options
init|=
name|Options
operator|.
name|getDefaultOptions
argument_list|()
decl_stmt|;
name|options
operator|.
name|excludeHBaseVersion
argument_list|()
operator|.
name|excludeBackupMasters
argument_list|()
operator|.
name|excludeBalancerOn
argument_list|()
operator|.
name|excludeClusterId
argument_list|()
operator|.
name|excludeMaster
argument_list|()
operator|.
name|excludeMasterCoprocessors
argument_list|()
operator|.
name|excludeRegionState
argument_list|()
expr_stmt|;
name|ClusterStatus
name|status
init|=
name|ADMIN
operator|.
name|getClusterStatus
argument_list|(
name|options
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|status
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|status
operator|.
name|getServers
argument_list|()
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
comment|// live servers = nums of regionservers
comment|// By default, HMaster don't carry any regions so it won't report its load.
comment|// Hence, it won't be in the server list.
name|Assert
operator|.
name|assertEquals
argument_list|(
name|status
operator|.
name|getServers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|numRs
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|status
operator|.
name|getRegionsCount
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|status
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
name|status
operator|.
name|getDeadServersSize
argument_list|()
argument_list|)
expr_stmt|;
name|ServerName
name|deadServerName
init|=
name|status
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
name|ClusterStatus
operator|.
name|Options
name|options
init|=
name|Options
operator|.
name|getDefaultOptions
argument_list|()
decl_stmt|;
name|options
operator|.
name|excludeHBaseVersion
argument_list|()
operator|.
name|excludeBalancerOn
argument_list|()
operator|.
name|excludeClusterId
argument_list|()
operator|.
name|excludeLiveServers
argument_list|()
operator|.
name|excludeDeadServers
argument_list|()
operator|.
name|excludeMasterCoprocessors
argument_list|()
operator|.
name|excludeRegionState
argument_list|()
expr_stmt|;
name|ClusterStatus
name|status
init|=
name|ADMIN
operator|.
name|getClusterStatus
argument_list|(
name|options
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|status
operator|.
name|getMaster
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
name|status
operator|.
name|getBackupMastersSize
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
name|ClusterStatus
operator|.
name|Options
name|options
init|=
name|Options
operator|.
name|getDefaultOptions
argument_list|()
decl_stmt|;
name|options
operator|.
name|excludeMaster
argument_list|()
operator|.
name|excludeBackupMasters
argument_list|()
operator|.
name|excludeRegionState
argument_list|()
operator|.
name|excludeLiveServers
argument_list|()
operator|.
name|excludeBackupMasters
argument_list|()
expr_stmt|;
name|ClusterStatus
name|status
init|=
name|ADMIN
operator|.
name|getClusterStatus
argument_list|(
name|options
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|status
operator|.
name|getMasterCoprocessors
argument_list|()
operator|.
name|length
operator|==
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|status
operator|.
name|getHBaseVersion
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|status
operator|.
name|getClusterId
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|status
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
name|status
operator|.
name|getBalancerOn
argument_list|()
operator|&&
operator|!
name|status
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
name|ADMIN
operator|.
name|close
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit
