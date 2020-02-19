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
name|assertTrue
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|Map
operator|.
name|Entry
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
name|RegionReplicaUtil
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
name|MasterTests
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
block|{
name|MasterTests
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
name|TestStochasticLoadBalancerRegionReplica
extends|extends
name|BalancerTestBase
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
name|TestStochasticLoadBalancerRegionReplica
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testReplicaCost
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
name|RegionReplicaHostCostFunction
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
name|costFunction
operator|.
name|init
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
name|double
name|cost
init|=
name|costFunction
operator|.
name|cost
argument_list|()
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
name|testReplicaCostForReplicas
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
name|RegionReplicaHostCostFunction
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|int
index|[]
name|servers
init|=
operator|new
name|int
index|[]
block|{
literal|3
block|,
literal|3
block|,
literal|3
block|,
literal|3
block|,
literal|3
block|}
decl_stmt|;
name|TreeMap
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
name|clusterState
init|=
name|mockClusterServers
argument_list|(
name|servers
argument_list|)
decl_stmt|;
name|BaseLoadBalancer
operator|.
name|Cluster
name|cluster
decl_stmt|;
name|cluster
operator|=
operator|new
name|BaseLoadBalancer
operator|.
name|Cluster
argument_list|(
name|clusterState
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|costFunction
operator|.
name|init
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
name|double
name|costWithoutReplicas
init|=
name|costFunction
operator|.
name|cost
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|costWithoutReplicas
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// replicate the region from first server to the last server
name|RegionInfo
name|replica1
init|=
name|RegionReplicaUtil
operator|.
name|getRegionInfoForReplica
argument_list|(
name|clusterState
operator|.
name|firstEntry
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|clusterState
operator|.
name|lastEntry
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|add
argument_list|(
name|replica1
argument_list|)
expr_stmt|;
name|cluster
operator|=
operator|new
name|BaseLoadBalancer
operator|.
name|Cluster
argument_list|(
name|clusterState
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|costFunction
operator|.
name|init
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
name|double
name|costWith1ReplicaDifferentServer
init|=
name|costFunction
operator|.
name|cost
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|costWith1ReplicaDifferentServer
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// add a third replica to the last server
name|RegionInfo
name|replica2
init|=
name|RegionReplicaUtil
operator|.
name|getRegionInfoForReplica
argument_list|(
name|replica1
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|clusterState
operator|.
name|lastEntry
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|add
argument_list|(
name|replica2
argument_list|)
expr_stmt|;
name|cluster
operator|=
operator|new
name|BaseLoadBalancer
operator|.
name|Cluster
argument_list|(
name|clusterState
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|costFunction
operator|.
name|init
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
name|double
name|costWith1ReplicaSameServer
init|=
name|costFunction
operator|.
name|cost
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|costWith1ReplicaDifferentServer
operator|<
name|costWith1ReplicaSameServer
argument_list|)
expr_stmt|;
comment|// test with replication = 4 for following:
name|RegionInfo
name|replica3
decl_stmt|;
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
argument_list|>
name|it
decl_stmt|;
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
name|entry
decl_stmt|;
name|clusterState
operator|=
name|mockClusterServers
argument_list|(
name|servers
argument_list|)
expr_stmt|;
name|it
operator|=
name|clusterState
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
expr_stmt|;
name|entry
operator|=
name|it
operator|.
name|next
argument_list|()
expr_stmt|;
comment|// first server
name|RegionInfo
name|hri
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|replica1
operator|=
name|RegionReplicaUtil
operator|.
name|getRegionInfoForReplica
argument_list|(
name|hri
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|replica2
operator|=
name|RegionReplicaUtil
operator|.
name|getRegionInfoForReplica
argument_list|(
name|hri
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|replica3
operator|=
name|RegionReplicaUtil
operator|.
name|getRegionInfoForReplica
argument_list|(
name|hri
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|add
argument_list|(
name|replica1
argument_list|)
expr_stmt|;
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|add
argument_list|(
name|replica2
argument_list|)
expr_stmt|;
name|it
operator|.
name|next
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|add
argument_list|(
name|replica3
argument_list|)
expr_stmt|;
comment|// 2nd server
name|cluster
operator|=
operator|new
name|BaseLoadBalancer
operator|.
name|Cluster
argument_list|(
name|clusterState
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|costFunction
operator|.
name|init
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
name|double
name|costWith3ReplicasSameServer
init|=
name|costFunction
operator|.
name|cost
argument_list|()
decl_stmt|;
name|clusterState
operator|=
name|mockClusterServers
argument_list|(
name|servers
argument_list|)
expr_stmt|;
name|hri
operator|=
name|clusterState
operator|.
name|firstEntry
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|replica1
operator|=
name|RegionReplicaUtil
operator|.
name|getRegionInfoForReplica
argument_list|(
name|hri
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|replica2
operator|=
name|RegionReplicaUtil
operator|.
name|getRegionInfoForReplica
argument_list|(
name|hri
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|replica3
operator|=
name|RegionReplicaUtil
operator|.
name|getRegionInfoForReplica
argument_list|(
name|hri
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|clusterState
operator|.
name|firstEntry
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|add
argument_list|(
name|replica1
argument_list|)
expr_stmt|;
name|clusterState
operator|.
name|lastEntry
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|add
argument_list|(
name|replica2
argument_list|)
expr_stmt|;
name|clusterState
operator|.
name|lastEntry
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|add
argument_list|(
name|replica3
argument_list|)
expr_stmt|;
name|cluster
operator|=
operator|new
name|BaseLoadBalancer
operator|.
name|Cluster
argument_list|(
name|clusterState
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|costFunction
operator|.
name|init
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
name|double
name|costWith2ReplicasOnTwoServers
init|=
name|costFunction
operator|.
name|cost
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|costWith2ReplicasOnTwoServers
operator|<
name|costWith3ReplicasSameServer
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNeedsBalanceForColocatedReplicas
parameter_list|()
block|{
comment|// check for the case where there are two hosts and with one rack, and where
comment|// both the replicas are hosted on the same server
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regions
init|=
name|randomRegions
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|ServerName
name|s1
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"host1"
argument_list|,
literal|1000
argument_list|,
literal|11111
argument_list|)
decl_stmt|;
name|ServerName
name|s2
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"host11"
argument_list|,
literal|1000
argument_list|,
literal|11111
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
name|s1
argument_list|,
name|regions
argument_list|)
expr_stmt|;
name|regions
operator|.
name|add
argument_list|(
name|RegionReplicaUtil
operator|.
name|getRegionInfoForReplica
argument_list|(
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|// until the step above s1 holds two replicas of a region
name|regions
operator|=
name|randomRegions
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|s2
argument_list|,
name|regions
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|loadBalancer
operator|.
name|needsBalance
argument_list|(
operator|new
name|Cluster
argument_list|(
name|map
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// check for the case where there are two hosts on the same rack and there are two racks
comment|// and both the replicas are on the same rack
name|map
operator|.
name|clear
argument_list|()
expr_stmt|;
name|regions
operator|=
name|randomRegions
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regionsOnS2
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|regionsOnS2
operator|.
name|add
argument_list|(
name|RegionReplicaUtil
operator|.
name|getRegionInfoForReplica
argument_list|(
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|s1
argument_list|,
name|regions
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|s2
argument_list|,
name|regionsOnS2
argument_list|)
expr_stmt|;
comment|// add another server so that the cluster has some host on another rack
name|map
operator|.
name|put
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"host2"
argument_list|,
literal|1000
argument_list|,
literal|11111
argument_list|)
argument_list|,
name|randomRegions
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|loadBalancer
operator|.
name|needsBalance
argument_list|(
operator|new
name|Cluster
argument_list|(
name|map
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|ForTestRackManagerOne
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionReplicasOnSmallCluster
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
name|replication
init|=
literal|3
decl_stmt|;
comment|// 3 replicas per region
name|int
name|numRegionsPerServer
init|=
literal|80
decl_stmt|;
comment|// all regions are mostly balanced
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
name|replication
argument_list|,
name|numTables
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|ForTestRackManagerOne
extends|extends
name|RackManager
block|{
annotation|@
name|Override
specifier|public
name|String
name|getRack
parameter_list|(
name|ServerName
name|server
parameter_list|)
block|{
return|return
name|server
operator|.
name|getHostname
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|"1"
argument_list|)
condition|?
literal|"rack1"
else|:
literal|"rack2"
return|;
block|}
block|}
block|}
end_class

end_unit

