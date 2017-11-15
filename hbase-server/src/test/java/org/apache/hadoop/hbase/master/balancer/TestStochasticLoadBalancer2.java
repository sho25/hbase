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
name|hbase
operator|.
name|CategoryBasedTimeout
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
name|FlakeyTests
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
name|TestRule
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|FlakeyTests
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
name|TestStochasticLoadBalancer2
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
name|TestStochasticLoadBalancer2
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
specifier|final
name|TestRule
name|timeout
init|=
name|CategoryBasedTimeout
operator|.
name|builder
argument_list|()
operator|.
name|withTimeout
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
operator|.
name|withLookingForStuckThread
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
block|{
name|conf
operator|.
name|setFloat
argument_list|(
literal|"hbase.master.balancer.stochastic.maxMovePercent"
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|StochasticLoadBalancer
operator|.
name|MAX_STEPS_KEY
argument_list|,
literal|2000000L
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
literal|"hbase.master.balancer.stochastic.localityCost"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.master.balancer.stochastic.maxRunningTime"
argument_list|,
literal|90
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|// 90 sec
name|conf
operator|.
name|setFloat
argument_list|(
literal|"hbase.master.balancer.stochastic.minCostNeedBalance"
argument_list|,
literal|0.05f
argument_list|)
expr_stmt|;
name|loadBalancer
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|after
parameter_list|()
block|{
comment|// reset config to make sure balancer run
name|loadBalancer
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionReplicasOnMidCluster
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
literal|40
operator|*
literal|200
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
literal|30
decl_stmt|;
comment|//all regions are mostly balanced
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
annotation|@
name|Test
specifier|public
name|void
name|testRegionReplicasOnLargeCluster
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
literal|20
operator|*
name|numNodes
decl_stmt|;
comment|// 20 * replication regions per RS
name|int
name|numRegionsPerServer
init|=
literal|19
decl_stmt|;
comment|// all servers except one
name|int
name|numTables
init|=
literal|100
decl_stmt|;
name|int
name|replication
init|=
literal|3
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
annotation|@
name|Test
specifier|public
name|void
name|testRegionReplicasOnMidClusterHighReplication
parameter_list|()
block|{
name|conf
operator|.
name|setLong
argument_list|(
name|StochasticLoadBalancer
operator|.
name|MAX_STEPS_KEY
argument_list|,
literal|4000000L
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.master.balancer.stochastic.maxRunningTime"
argument_list|,
literal|120
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|// 120 sec
name|loadBalancer
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|int
name|numNodes
init|=
literal|80
decl_stmt|;
name|int
name|numRegions
init|=
literal|6
operator|*
name|numNodes
decl_stmt|;
name|int
name|replication
init|=
literal|80
decl_stmt|;
comment|// 80 replicas per region, one for each server
name|int
name|numRegionsPerServer
init|=
literal|5
decl_stmt|;
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
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionReplicationOnMidClusterReplicationGreaterThanNumNodes
parameter_list|()
block|{
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.master.balancer.stochastic.maxRunningTime"
argument_list|,
literal|120
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|// 120 sec
name|loadBalancer
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|int
name|numNodes
init|=
literal|40
decl_stmt|;
name|int
name|numRegions
init|=
literal|6
operator|*
literal|50
decl_stmt|;
name|int
name|replication
init|=
literal|50
decl_stmt|;
comment|// 50 replicas per region, more than numNodes
name|int
name|numRegionsPerServer
init|=
literal|6
decl_stmt|;
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
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

