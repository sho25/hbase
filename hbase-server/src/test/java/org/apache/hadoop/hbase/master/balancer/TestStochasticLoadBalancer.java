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
name|java
operator|.
name|util
operator|.
name|Queue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|RegionLoad
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
name|ServerLoad
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
name|RegionPlan
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
name|assertNull
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

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestStochasticLoadBalancer
extends|extends
name|BalancerTestBase
block|{
specifier|public
specifier|static
specifier|final
name|String
name|REGION_KEY
init|=
literal|"testRegion"
decl_stmt|;
specifier|private
specifier|static
name|StochasticLoadBalancer
name|loadBalancer
decl_stmt|;
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
name|TestStochasticLoadBalancer
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeAllTests
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
name|setFloat
argument_list|(
literal|"hbase.master.balancer.stochastic.maxMovePercent"
argument_list|,
literal|0.75f
argument_list|)
expr_stmt|;
name|loadBalancer
operator|=
operator|new
name|StochasticLoadBalancer
argument_list|()
expr_stmt|;
name|loadBalancer
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|// int[testnum][servernumber] -> numregions
name|int
index|[]
index|[]
name|clusterStateMocks
init|=
operator|new
name|int
index|[]
index|[]
block|{
comment|// 1 node
operator|new
name|int
index|[]
block|{
literal|0
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|1
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|10
block|}
block|,
comment|// 2 node
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|0
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|2
block|,
literal|0
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|2
block|,
literal|1
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|2
block|,
literal|2
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|2
block|,
literal|3
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|2
block|,
literal|4
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|1
block|,
literal|1
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|1
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|10
block|,
literal|1
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|514
block|,
literal|1432
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|47
block|,
literal|53
block|}
block|,
comment|// 3 node
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|2
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|2
block|,
literal|2
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|3
block|,
literal|0
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|4
block|,
literal|0
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|20
block|,
literal|20
block|,
literal|0
block|}
block|,
comment|// 4 node
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|2
block|,
literal|3
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|4
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|5
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|6
block|,
literal|6
block|,
literal|0
block|,
literal|0
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|6
block|,
literal|2
block|,
literal|0
block|,
literal|0
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|6
block|,
literal|1
block|,
literal|0
block|,
literal|0
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|6
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|4
block|,
literal|4
block|,
literal|4
block|,
literal|7
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|4
block|,
literal|4
block|,
literal|4
block|,
literal|8
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|7
block|}
block|,
comment|// 5 node
operator|new
name|int
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|4
block|}
block|,
comment|// more nodes
operator|new
name|int
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|,
literal|5
block|,
literal|6
block|,
literal|7
block|,
literal|8
block|,
literal|9
block|,
literal|10
block|,
literal|11
block|,
literal|12
block|,
literal|13
block|,
literal|14
block|,
literal|15
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|10
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|6
block|,
literal|6
block|,
literal|5
block|,
literal|6
block|,
literal|6
block|,
literal|6
block|,
literal|6
block|,
literal|6
block|,
literal|6
block|,
literal|1
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|54
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|55
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|56
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|16
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|8
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|9
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|10
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|123
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|155
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|10
block|,
literal|7
block|,
literal|12
block|,
literal|8
block|,
literal|11
block|,
literal|10
block|,
literal|9
block|,
literal|14
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|13
block|,
literal|14
block|,
literal|6
block|,
literal|10
block|,
literal|10
block|,
literal|10
block|,
literal|8
block|,
literal|10
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|130
block|,
literal|14
block|,
literal|60
block|,
literal|10
block|,
literal|100
block|,
literal|10
block|,
literal|80
block|,
literal|10
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|130
block|,
literal|140
block|,
literal|60
block|,
literal|100
block|,
literal|100
block|,
literal|100
block|,
literal|80
block|,
literal|100
block|}
block|}
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testKeepRegionLoad
parameter_list|()
throws|throws
name|Exception
block|{
name|ServerName
name|sn
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"test:8080"
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|int
name|numClusterStatusToAdd
init|=
literal|20000
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
name|numClusterStatusToAdd
condition|;
name|i
operator|++
control|)
block|{
name|ServerLoad
name|sl
init|=
name|mock
argument_list|(
name|ServerLoad
operator|.
name|class
argument_list|)
decl_stmt|;
name|RegionLoad
name|rl
init|=
name|mock
argument_list|(
name|RegionLoad
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|rl
operator|.
name|getStores
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|RegionLoad
argument_list|>
name|regionLoadMap
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|RegionLoad
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
name|regionLoadMap
operator|.
name|put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|REGION_KEY
argument_list|)
argument_list|,
name|rl
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|sl
operator|.
name|getRegionsLoad
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|regionLoadMap
argument_list|)
expr_stmt|;
name|ClusterStatus
name|clusterStatus
init|=
name|mock
argument_list|(
name|ClusterStatus
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|clusterStatus
operator|.
name|getServers
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|sn
argument_list|)
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|clusterStatus
operator|.
name|getLoad
argument_list|(
name|sn
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|sl
argument_list|)
expr_stmt|;
name|loadBalancer
operator|.
name|setClusterStatus
argument_list|(
name|clusterStatus
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|loadBalancer
operator|.
name|loads
operator|.
name|get
argument_list|(
name|REGION_KEY
argument_list|)
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|loadBalancer
operator|.
name|loads
operator|.
name|get
argument_list|(
name|REGION_KEY
argument_list|)
operator|.
name|size
argument_list|()
operator|==
literal|15
argument_list|)
expr_stmt|;
name|Queue
argument_list|<
name|RegionLoad
argument_list|>
name|loads
init|=
name|loadBalancer
operator|.
name|loads
operator|.
name|get
argument_list|(
name|REGION_KEY
argument_list|)
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|loads
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|RegionLoad
name|rl
init|=
name|loads
operator|.
name|remove
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|i
operator|+
operator|(
name|numClusterStatusToAdd
operator|-
literal|15
operator|)
argument_list|,
name|rl
operator|.
name|getStores
argument_list|()
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
block|}
comment|/**    * Test the load balancing algorithm.    *    * Invariant is that all servers should be hosting either floor(average) or    * ceiling(average)    *    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testBalanceCluster
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|int
index|[]
name|mockCluster
range|:
name|clusterStateMocks
control|)
block|{
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|servers
init|=
name|mockClusterServers
argument_list|(
name|mockCluster
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ServerAndLoad
argument_list|>
name|list
init|=
name|convertToList
argument_list|(
name|servers
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Mock Cluster : "
operator|+
name|printMock
argument_list|(
name|list
argument_list|)
operator|+
literal|" "
operator|+
name|printStats
argument_list|(
name|list
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|RegionPlan
argument_list|>
name|plans
init|=
name|loadBalancer
operator|.
name|balanceCluster
argument_list|(
name|servers
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ServerAndLoad
argument_list|>
name|balancedCluster
init|=
name|reconcile
argument_list|(
name|list
argument_list|,
name|plans
argument_list|,
name|servers
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Mock Balance : "
operator|+
name|printMock
argument_list|(
name|balancedCluster
argument_list|)
argument_list|)
expr_stmt|;
name|assertClusterAsBalanced
argument_list|(
name|balancedCluster
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|RegionPlan
argument_list|>
name|secondPlans
init|=
name|loadBalancer
operator|.
name|balanceCluster
argument_list|(
name|servers
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|secondPlans
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|entry
range|:
name|servers
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|returnRegions
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|returnServer
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSkewCost
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|StochasticLoadBalancer
operator|.
name|CostFunction
name|costFunction
init|=
operator|new
name|StochasticLoadBalancer
operator|.
name|RegionCountSkewCostFunction
argument_list|(
name|conf
argument_list|)
decl_stmt|;
for|for
control|(
name|int
index|[]
name|mockCluster
range|:
name|clusterStateMocks
control|)
block|{
name|double
name|cost
init|=
name|costFunction
operator|.
name|cost
argument_list|(
name|mockCluster
argument_list|(
name|mockCluster
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|cost
operator|>=
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cost
operator|<=
literal|1.01
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|costFunction
operator|.
name|cost
argument_list|(
name|mockCluster
argument_list|(
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|1
block|}
argument_list|)
argument_list|)
argument_list|,
literal|0.01
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|.75
argument_list|,
name|costFunction
operator|.
name|cost
argument_list|(
name|mockCluster
argument_list|(
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|1
block|,
literal|1
block|}
argument_list|)
argument_list|)
argument_list|,
literal|0.01
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|.5
argument_list|,
name|costFunction
operator|.
name|cost
argument_list|(
name|mockCluster
argument_list|(
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|}
argument_list|)
argument_list|)
argument_list|,
literal|0.01
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|.25
argument_list|,
name|costFunction
operator|.
name|cost
argument_list|(
name|mockCluster
argument_list|(
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|}
argument_list|)
argument_list|)
argument_list|,
literal|0.01
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|costFunction
operator|.
name|cost
argument_list|(
name|mockCluster
argument_list|(
operator|new
name|int
index|[]
block|{
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|}
argument_list|)
argument_list|)
argument_list|,
literal|0.01
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|costFunction
operator|.
name|cost
argument_list|(
name|mockCluster
argument_list|(
operator|new
name|int
index|[]
block|{
literal|10
block|,
literal|10
block|,
literal|10
block|,
literal|10
block|,
literal|10
block|}
argument_list|)
argument_list|)
argument_list|,
literal|0.01
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableSkewCost
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|StochasticLoadBalancer
operator|.
name|CostFunction
name|costFunction
init|=
operator|new
name|StochasticLoadBalancer
operator|.
name|TableSkewCostFunction
argument_list|(
name|conf
argument_list|)
decl_stmt|;
for|for
control|(
name|int
index|[]
name|mockCluster
range|:
name|clusterStateMocks
control|)
block|{
name|BaseLoadBalancer
operator|.
name|Cluster
name|cluster
init|=
name|mockCluster
argument_list|(
name|mockCluster
argument_list|)
decl_stmt|;
name|double
name|cost
init|=
name|costFunction
operator|.
name|cost
argument_list|(
name|cluster
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|cost
operator|>=
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cost
operator|<=
literal|1.01
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCostFromArray
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|StochasticLoadBalancer
operator|.
name|CostFromRegionLoadFunction
name|costFunction
init|=
operator|new
name|StochasticLoadBalancer
operator|.
name|MemstoreSizeCostFunction
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|double
index|[]
name|statOne
init|=
operator|new
name|double
index|[
literal|100
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|statOne
index|[
name|i
index|]
operator|=
literal|10
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|costFunction
operator|.
name|costFromArray
argument_list|(
name|statOne
argument_list|)
argument_list|,
literal|0.01
argument_list|)
expr_stmt|;
name|double
index|[]
name|statTwo
init|=
operator|new
name|double
index|[
literal|101
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|statTwo
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
block|}
name|statTwo
index|[
literal|100
index|]
operator|=
literal|100
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|costFunction
operator|.
name|costFromArray
argument_list|(
name|statTwo
argument_list|)
argument_list|,
literal|0.01
argument_list|)
expr_stmt|;
name|double
index|[]
name|statThree
init|=
operator|new
name|double
index|[
literal|200
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|statThree
index|[
name|i
index|]
operator|=
operator|(
literal|0
operator|)
expr_stmt|;
name|statThree
index|[
name|i
operator|+
literal|100
index|]
operator|=
literal|100
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|0.5
argument_list|,
name|costFunction
operator|.
name|costFromArray
argument_list|(
name|statThree
argument_list|)
argument_list|,
literal|0.01
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testLosingRs
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|numNodes
init|=
literal|3
decl_stmt|;
name|int
name|numRegions
init|=
literal|20
decl_stmt|;
name|int
name|numRegionsPerServer
init|=
literal|3
decl_stmt|;
comment|//all servers except one
name|int
name|numTables
init|=
literal|2
decl_stmt|;
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|serverMap
init|=
name|createServerMap
argument_list|(
name|numNodes
argument_list|,
name|numRegions
argument_list|,
name|numRegionsPerServer
argument_list|,
name|numTables
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ServerAndLoad
argument_list|>
name|list
init|=
name|convertToList
argument_list|(
name|serverMap
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|RegionPlan
argument_list|>
name|plans
init|=
name|loadBalancer
operator|.
name|balanceCluster
argument_list|(
name|serverMap
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|plans
argument_list|)
expr_stmt|;
comment|// Apply the plan to the mock cluster.
name|List
argument_list|<
name|ServerAndLoad
argument_list|>
name|balancedCluster
init|=
name|reconcile
argument_list|(
name|list
argument_list|,
name|plans
argument_list|,
name|serverMap
argument_list|)
decl_stmt|;
name|assertClusterAsBalanced
argument_list|(
name|balancedCluster
argument_list|)
expr_stmt|;
name|ServerName
name|sn
init|=
name|serverMap
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|ServerName
index|[
name|serverMap
operator|.
name|size
argument_list|()
index|]
argument_list|)
index|[
literal|0
index|]
decl_stmt|;
name|ServerName
name|deadSn
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
name|sn
operator|.
name|getHostname
argument_list|()
argument_list|,
name|sn
operator|.
name|getPort
argument_list|()
argument_list|,
name|sn
operator|.
name|getStartcode
argument_list|()
operator|-
literal|100
argument_list|)
decl_stmt|;
name|serverMap
operator|.
name|put
argument_list|(
name|deadSn
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|plans
operator|=
name|loadBalancer
operator|.
name|balanceCluster
argument_list|(
name|serverMap
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|plans
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testSmallCluster
parameter_list|()
block|{
name|int
name|numNodes
init|=
literal|10
decl_stmt|;
name|int
name|numRegions
init|=
literal|1000
decl_stmt|;
name|int
name|numRegionsPerServer
init|=
literal|40
decl_stmt|;
comment|//all servers except one
name|int
name|numTables
init|=
literal|10
decl_stmt|;
name|testWithCluster
argument_list|(
name|numNodes
argument_list|,
name|numRegions
argument_list|,
name|numRegionsPerServer
argument_list|,
name|numTables
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testSmallCluster2
parameter_list|()
block|{
name|int
name|numNodes
init|=
literal|20
decl_stmt|;
name|int
name|numRegions
init|=
literal|2000
decl_stmt|;
name|int
name|numRegionsPerServer
init|=
literal|40
decl_stmt|;
comment|//all servers except one
name|int
name|numTables
init|=
literal|10
decl_stmt|;
name|testWithCluster
argument_list|(
name|numNodes
argument_list|,
name|numRegions
argument_list|,
name|numRegionsPerServer
argument_list|,
name|numTables
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testSmallCluster3
parameter_list|()
block|{
name|int
name|numNodes
init|=
literal|20
decl_stmt|;
name|int
name|numRegions
init|=
literal|2000
decl_stmt|;
name|int
name|numRegionsPerServer
init|=
literal|1
decl_stmt|;
comment|// all servers except one
name|int
name|numTables
init|=
literal|10
decl_stmt|;
name|testWithCluster
argument_list|(
name|numNodes
argument_list|,
name|numRegions
argument_list|,
name|numRegionsPerServer
argument_list|,
name|numTables
argument_list|,
literal|false
comment|/* max moves */
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|800000
argument_list|)
specifier|public
name|void
name|testMidCluster
parameter_list|()
block|{
name|int
name|numNodes
init|=
literal|100
decl_stmt|;
name|int
name|numRegions
init|=
literal|10000
decl_stmt|;
name|int
name|numRegionsPerServer
init|=
literal|60
decl_stmt|;
comment|// all servers except one
name|int
name|numTables
init|=
literal|40
decl_stmt|;
name|testWithCluster
argument_list|(
name|numNodes
argument_list|,
name|numRegions
argument_list|,
name|numRegionsPerServer
argument_list|,
name|numTables
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|800000
argument_list|)
specifier|public
name|void
name|testMidCluster2
parameter_list|()
block|{
name|int
name|numNodes
init|=
literal|200
decl_stmt|;
name|int
name|numRegions
init|=
literal|100000
decl_stmt|;
name|int
name|numRegionsPerServer
init|=
literal|40
decl_stmt|;
comment|// all servers except one
name|int
name|numTables
init|=
literal|400
decl_stmt|;
name|testWithCluster
argument_list|(
name|numNodes
argument_list|,
name|numRegions
argument_list|,
name|numRegionsPerServer
argument_list|,
name|numTables
argument_list|,
literal|false
comment|/* num large num regions means may not always get to best balance with one run */
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|800000
argument_list|)
specifier|public
name|void
name|testMidCluster3
parameter_list|()
block|{
name|int
name|numNodes
init|=
literal|100
decl_stmt|;
name|int
name|numRegions
init|=
literal|2000
decl_stmt|;
name|int
name|numRegionsPerServer
init|=
literal|9
decl_stmt|;
comment|// all servers except one
name|int
name|numTables
init|=
literal|110
decl_stmt|;
name|testWithCluster
argument_list|(
name|numNodes
argument_list|,
name|numRegions
argument_list|,
name|numRegionsPerServer
argument_list|,
name|numTables
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// TODO(eclark): Make sure that the tables are well distributed.
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLargeCluster
parameter_list|()
block|{
name|int
name|numNodes
init|=
literal|1000
decl_stmt|;
name|int
name|numRegions
init|=
literal|100000
decl_stmt|;
comment|//100 regions per RS
name|int
name|numRegionsPerServer
init|=
literal|80
decl_stmt|;
comment|//all servers except one
name|int
name|numTables
init|=
literal|100
decl_stmt|;
name|testWithCluster
argument_list|(
name|numNodes
argument_list|,
name|numRegions
argument_list|,
name|numRegionsPerServer
argument_list|,
name|numTables
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|testWithCluster
parameter_list|(
name|int
name|numNodes
parameter_list|,
name|int
name|numRegions
parameter_list|,
name|int
name|numRegionsPerServer
parameter_list|,
name|int
name|numTables
parameter_list|,
name|boolean
name|assertFullyBalanced
parameter_list|)
block|{
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|serverMap
init|=
name|createServerMap
argument_list|(
name|numNodes
argument_list|,
name|numRegions
argument_list|,
name|numRegionsPerServer
argument_list|,
name|numTables
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ServerAndLoad
argument_list|>
name|list
init|=
name|convertToList
argument_list|(
name|serverMap
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Mock Cluster : "
operator|+
name|printMock
argument_list|(
name|list
argument_list|)
operator|+
literal|" "
operator|+
name|printStats
argument_list|(
name|list
argument_list|)
argument_list|)
expr_stmt|;
comment|// Run the balancer.
name|List
argument_list|<
name|RegionPlan
argument_list|>
name|plans
init|=
name|loadBalancer
operator|.
name|balanceCluster
argument_list|(
name|serverMap
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|plans
argument_list|)
expr_stmt|;
comment|// Check to see that this actually got to a stable place.
if|if
condition|(
name|assertFullyBalanced
condition|)
block|{
comment|// Apply the plan to the mock cluster.
name|List
argument_list|<
name|ServerAndLoad
argument_list|>
name|balancedCluster
init|=
name|reconcile
argument_list|(
name|list
argument_list|,
name|plans
argument_list|,
name|serverMap
argument_list|)
decl_stmt|;
comment|// Print out the cluster loads to make debugging easier.
name|LOG
operator|.
name|info
argument_list|(
literal|"Mock Balance : "
operator|+
name|printMock
argument_list|(
name|balancedCluster
argument_list|)
argument_list|)
expr_stmt|;
name|assertClusterAsBalanced
argument_list|(
name|balancedCluster
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|RegionPlan
argument_list|>
name|secondPlans
init|=
name|loadBalancer
operator|.
name|balanceCluster
argument_list|(
name|serverMap
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|secondPlans
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|createServerMap
parameter_list|(
name|int
name|numNodes
parameter_list|,
name|int
name|numRegions
parameter_list|,
name|int
name|numRegionsPerServer
parameter_list|,
name|int
name|numTables
parameter_list|)
block|{
comment|//construct a cluster of numNodes, having  a total of numRegions. Each RS will hold
comment|//numRegionsPerServer many regions except for the last one, which will host all the
comment|//remaining regions
name|int
index|[]
name|cluster
init|=
operator|new
name|int
index|[
name|numNodes
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
name|numNodes
condition|;
name|i
operator|++
control|)
block|{
name|cluster
index|[
name|i
index|]
operator|=
name|numRegionsPerServer
expr_stmt|;
block|}
name|cluster
index|[
name|cluster
operator|.
name|length
operator|-
literal|1
index|]
operator|=
name|numRegions
operator|-
operator|(
operator|(
name|cluster
operator|.
name|length
operator|-
literal|1
operator|)
operator|*
name|numRegionsPerServer
operator|)
expr_stmt|;
return|return
name|mockClusterServers
argument_list|(
name|cluster
argument_list|,
name|numTables
argument_list|)
return|;
block|}
block|}
end_class

end_unit

