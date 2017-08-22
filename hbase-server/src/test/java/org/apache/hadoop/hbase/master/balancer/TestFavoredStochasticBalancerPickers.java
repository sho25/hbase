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
name|master
operator|.
name|balancer
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
name|Arrays
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
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|HRegionInfo
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
name|master
operator|.
name|RackManager
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
name|favored
operator|.
name|FavoredNodesManager
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
name|BaseLoadBalancer
operator|.
name|Cluster
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
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
name|TestFavoredStochasticBalancerPickers
extends|extends
name|BalancerTestBase
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
name|TestFavoredStochasticBalancerPickers
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
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
name|SLAVES
init|=
literal|6
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|REGIONS
init|=
name|SLAVES
operator|*
literal|3
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|Admin
name|admin
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
comment|// Enable favored nodes based load balancer
name|conf
operator|.
name|setClass
argument_list|(
name|HConstants
operator|.
name|HBASE_MASTER_LOADBALANCER_CLASS
argument_list|,
name|LoadOnlyFavoredStochasticBalancer
operator|.
name|class
argument_list|,
name|LoadBalancer
operator|.
name|class
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.master.balancer.stochastic.maxRunningTime"
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.balancer.stochastic.moveCost"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.master.balancer.stochastic.execute.maxSteps"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|BaseLoadBalancer
operator|.
name|TABLES_ON_MASTER
argument_list|,
literal|"none"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|startCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|SLAVES
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|waitClusterUp
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|waitForActiveAndReadyMaster
argument_list|(
literal|120
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|admin
operator|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|admin
operator|.
name|setBalancerRunning
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|stopCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|cleanupTestDir
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Ignore
annotation|@
name|Test
specifier|public
name|void
name|testPickers
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testPickers"
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzz"
argument_list|)
argument_list|,
name|REGIONS
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|loadTable
argument_list|(
name|admin
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|ServerName
name|masterServerName
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getServerHoldingMeta
argument_list|()
decl_stmt|;
specifier|final
name|ServerName
name|mostLoadedServer
init|=
name|getRSWithMaxRegions
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|masterServerName
argument_list|)
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|mostLoadedServer
argument_list|)
expr_stmt|;
name|int
name|numRegions
init|=
name|admin
operator|.
name|getOnlineRegions
argument_list|(
name|mostLoadedServer
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
name|ServerName
name|source
init|=
name|getRSWithMaxRegions
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|masterServerName
argument_list|,
name|mostLoadedServer
argument_list|)
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|int
name|regionsToMove
init|=
name|admin
operator|.
name|getOnlineRegions
argument_list|(
name|source
argument_list|)
operator|.
name|size
argument_list|()
operator|/
literal|2
decl_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|hris
init|=
name|admin
operator|.
name|getOnlineRegions
argument_list|(
name|source
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
name|regionsToMove
condition|;
name|i
operator|++
control|)
block|{
name|admin
operator|.
name|move
argument_list|(
name|hris
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|mostLoadedServer
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Moving region: "
operator|+
name|hris
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" to "
operator|+
name|mostLoadedServer
argument_list|)
expr_stmt|;
block|}
specifier|final
name|int
name|finalRegions
init|=
name|numRegions
operator|+
name|regionsToMove
decl_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilNoRegionsInTransition
argument_list|(
literal|60000
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
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
name|int
name|numRegions
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getOnlineRegions
argument_list|(
name|mostLoadedServer
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
return|return
operator|(
name|numRegions
operator|==
name|finalRegions
operator|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|startRegionServerAndWait
argument_list|(
literal|60000
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|serverAssignments
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|ClusterStatus
name|status
init|=
name|admin
operator|.
name|getClusterStatus
argument_list|()
decl_stmt|;
for|for
control|(
name|ServerName
name|sn
range|:
name|status
operator|.
name|getServers
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|ServerName
operator|.
name|isSameAddress
argument_list|(
name|sn
argument_list|,
name|masterServerName
argument_list|)
condition|)
block|{
name|serverAssignments
operator|.
name|put
argument_list|(
name|sn
argument_list|,
name|admin
operator|.
name|getOnlineRegions
argument_list|(
name|sn
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|RegionLocationFinder
name|regionFinder
init|=
operator|new
name|RegionLocationFinder
argument_list|()
decl_stmt|;
name|regionFinder
operator|.
name|setClusterStatus
argument_list|(
name|admin
operator|.
name|getClusterStatus
argument_list|()
argument_list|)
expr_stmt|;
name|regionFinder
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|regionFinder
operator|.
name|setServices
argument_list|(
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
argument_list|)
expr_stmt|;
name|Cluster
name|cluster
init|=
operator|new
name|Cluster
argument_list|(
name|serverAssignments
argument_list|,
literal|null
argument_list|,
name|regionFinder
argument_list|,
operator|new
name|RackManager
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|LoadOnlyFavoredStochasticBalancer
name|balancer
init|=
operator|(
name|LoadOnlyFavoredStochasticBalancer
operator|)
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getLoadBalancer
argument_list|()
decl_stmt|;
name|FavoredNodesManager
name|fnm
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getFavoredNodesManager
argument_list|()
decl_stmt|;
name|cluster
operator|.
name|sortServersByRegionCount
argument_list|()
expr_stmt|;
name|Integer
index|[]
name|servers
init|=
name|cluster
operator|.
name|serverIndicesSortedByRegionCount
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Servers sorted by region count:"
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|servers
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Cluster dump: "
operator|+
name|cluster
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|mostLoadedServer
operator|.
name|equals
argument_list|(
name|cluster
operator|.
name|servers
index|[
name|servers
index|[
name|servers
operator|.
name|length
operator|-
literal|1
index|]
index|]
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Most loaded server: "
operator|+
name|mostLoadedServer
operator|+
literal|" does not match: "
operator|+
name|cluster
operator|.
name|servers
index|[
name|servers
index|[
name|servers
operator|.
name|length
operator|-
literal|1
index|]
index|]
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|mostLoadedServer
argument_list|,
name|cluster
operator|.
name|servers
index|[
name|servers
index|[
name|servers
operator|.
name|length
operator|-
literal|1
index|]
index|]
argument_list|)
expr_stmt|;
name|FavoredStochasticBalancer
operator|.
name|FavoredNodeLoadPicker
name|loadPicker
init|=
name|balancer
operator|.
expr|new
name|FavoredNodeLoadPicker
argument_list|()
decl_stmt|;
name|boolean
name|userRegionPicked
init|=
literal|false
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|userRegionPicked
condition|)
block|{
break|break;
block|}
else|else
block|{
name|Cluster
operator|.
name|Action
name|action
init|=
name|loadPicker
operator|.
name|generate
argument_list|(
name|cluster
argument_list|)
decl_stmt|;
if|if
condition|(
name|action
operator|.
name|type
operator|==
name|Cluster
operator|.
name|Action
operator|.
name|Type
operator|.
name|MOVE_REGION
condition|)
block|{
name|Cluster
operator|.
name|MoveRegionAction
name|moveRegionAction
init|=
operator|(
name|Cluster
operator|.
name|MoveRegionAction
operator|)
name|action
decl_stmt|;
name|HRegionInfo
name|region
init|=
name|cluster
operator|.
name|regions
index|[
name|moveRegionAction
operator|.
name|region
index|]
decl_stmt|;
name|assertNotEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|moveRegionAction
operator|.
name|toServer
argument_list|)
expr_stmt|;
name|ServerName
name|destinationServer
init|=
name|cluster
operator|.
name|servers
index|[
name|moveRegionAction
operator|.
name|toServer
index|]
decl_stmt|;
name|assertEquals
argument_list|(
name|cluster
operator|.
name|servers
index|[
name|moveRegionAction
operator|.
name|fromServer
index|]
argument_list|,
name|mostLoadedServer
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|region
operator|.
name|getTable
argument_list|()
operator|.
name|isSystemTable
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|ServerName
argument_list|>
name|favNodes
init|=
name|fnm
operator|.
name|getFavoredNodes
argument_list|(
name|region
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|favNodes
operator|.
name|contains
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
name|destinationServer
operator|.
name|getHostAndPort
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|userRegionPicked
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
block|}
name|assertTrue
argument_list|(
literal|"load picker did not pick expected regions in 100 iterations."
argument_list|,
name|userRegionPicked
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ServerName
name|getRSWithMaxRegions
parameter_list|(
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
name|excludeNodes
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|maxRegions
init|=
literal|0
decl_stmt|;
name|ServerName
name|maxLoadedServer
init|=
literal|null
decl_stmt|;
for|for
control|(
name|ServerName
name|sn
range|:
name|admin
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getServers
argument_list|()
control|)
block|{
if|if
condition|(
name|admin
operator|.
name|getOnlineRegions
argument_list|(
name|sn
argument_list|)
operator|.
name|size
argument_list|()
operator|>
name|maxRegions
condition|)
block|{
if|if
condition|(
name|excludeNodes
operator|==
literal|null
operator|||
operator|!
name|doesMatchExcludeNodes
argument_list|(
name|excludeNodes
argument_list|,
name|sn
argument_list|)
condition|)
block|{
name|maxRegions
operator|=
name|admin
operator|.
name|getOnlineRegions
argument_list|(
name|sn
argument_list|)
operator|.
name|size
argument_list|()
expr_stmt|;
name|maxLoadedServer
operator|=
name|sn
expr_stmt|;
block|}
block|}
block|}
return|return
name|maxLoadedServer
return|;
block|}
specifier|private
name|boolean
name|doesMatchExcludeNodes
parameter_list|(
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
name|excludeNodes
parameter_list|,
name|ServerName
name|sn
parameter_list|)
block|{
for|for
control|(
name|ServerName
name|excludeSN
range|:
name|excludeNodes
control|)
block|{
if|if
condition|(
name|ServerName
operator|.
name|isSameAddress
argument_list|(
name|sn
argument_list|,
name|excludeSN
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

