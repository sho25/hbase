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
name|TestStochasticLoadBalancerRegionReplicaWithRacks
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
name|TestStochasticLoadBalancerRegionReplicaWithRacks
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
class|class
name|ForTestRackManager
extends|extends
name|RackManager
block|{
name|int
name|numRacks
decl_stmt|;
specifier|public
name|ForTestRackManager
parameter_list|(
name|int
name|numRacks
parameter_list|)
block|{
name|this
operator|.
name|numRacks
operator|=
name|numRacks
expr_stmt|;
block|}
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
literal|"rack_"
operator|+
operator|(
name|server
operator|.
name|hashCode
argument_list|()
operator|%
name|numRacks
operator|)
return|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionReplicationOnMidClusterWithRacks
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
literal|10000000L
argument_list|)
expr_stmt|;
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
literal|30
decl_stmt|;
name|int
name|numRegions
init|=
name|numNodes
operator|*
literal|30
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
literal|28
decl_stmt|;
name|int
name|numTables
init|=
literal|10
decl_stmt|;
name|int
name|numRacks
init|=
literal|4
decl_stmt|;
comment|// all replicas should be on a different rack
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|RegionInfo
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
name|replication
argument_list|,
name|numTables
argument_list|)
decl_stmt|;
name|RackManager
name|rm
init|=
operator|new
name|ForTestRackManager
argument_list|(
name|numRacks
argument_list|)
decl_stmt|;
name|testWithCluster
argument_list|(
name|serverMap
argument_list|,
name|rm
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

