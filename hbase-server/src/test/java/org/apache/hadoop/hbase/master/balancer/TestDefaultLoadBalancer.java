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
name|testclassification
operator|.
name|MasterTests
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
name|net
operator|.
name|DNSToSwitchMapping
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
comment|/**  * Test the load balancer that is created by default.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestDefaultLoadBalancer
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
name|TestDefaultLoadBalancer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|LoadBalancer
name|loadBalancer
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
name|setClass
argument_list|(
literal|"hbase.util.ip.to.rack.determiner"
argument_list|,
name|MockMapping
operator|.
name|class
argument_list|,
name|DNSToSwitchMapping
operator|.
name|class
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.regions.slop"
argument_list|,
literal|"0"
argument_list|)
expr_stmt|;
name|loadBalancer
operator|=
operator|new
name|SimpleLoadBalancer
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
literal|14
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
literal|0
block|,
literal|0
block|,
literal|144
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1123
block|,
literal|133
block|,
literal|138
block|,
literal|12
block|,
literal|1444
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
literal|144
block|,
literal|1
block|,
literal|0
block|,
literal|4
block|,
literal|1
block|,
literal|1123
block|,
literal|133
block|,
literal|138
block|,
literal|12
block|,
literal|1444
block|}
block|,
operator|new
name|int
index|[]
block|{
literal|1538
block|,
literal|1392
block|,
literal|1561
block|,
literal|1557
block|,
literal|1535
block|,
literal|1553
block|,
literal|1385
block|,
literal|1542
block|,
literal|1619
block|}
block|}
decl_stmt|;
comment|/**    * Test the load balancing algorithm.    *    * Invariant is that all servers should be hosting either floor(average) or    * ceiling(average)    *    * @throws Exception    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
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
block|}
end_class

end_unit

