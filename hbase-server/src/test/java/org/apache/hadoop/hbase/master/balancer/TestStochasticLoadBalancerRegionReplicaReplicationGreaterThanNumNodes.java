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
name|TestStochasticLoadBalancerRegionReplicaReplicationGreaterThanNumNodes
extends|extends
name|BalancerTestBase2
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
name|TestStochasticLoadBalancerRegionReplicaReplicationGreaterThanNumNodes
operator|.
name|class
argument_list|)
decl_stmt|;
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

