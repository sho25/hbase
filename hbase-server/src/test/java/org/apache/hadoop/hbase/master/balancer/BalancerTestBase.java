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
name|LinkedList
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
name|Random
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedSet
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
name|java
operator|.
name|util
operator|.
name|TreeSet
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

begin_comment
comment|/**  * Class used to be the base of unit tests on load balancers. It gives helper  * methods to create maps of {@link ServerName} to lists of {@link HRegionInfo}  * and to check list of region plans.  *  */
end_comment

begin_class
specifier|public
class|class
name|BalancerTestBase
block|{
specifier|protected
specifier|static
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|static
name|int
name|regionId
init|=
literal|0
decl_stmt|;
comment|/**    * Invariant is that all servers have between floor(avg) and ceiling(avg)    * number of regions.    */
specifier|public
name|void
name|assertClusterAsBalanced
parameter_list|(
name|List
argument_list|<
name|ServerAndLoad
argument_list|>
name|servers
parameter_list|)
block|{
name|int
name|numServers
init|=
name|servers
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|numRegions
init|=
literal|0
decl_stmt|;
name|int
name|maxRegions
init|=
literal|0
decl_stmt|;
name|int
name|minRegions
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
for|for
control|(
name|ServerAndLoad
name|server
range|:
name|servers
control|)
block|{
name|int
name|nr
init|=
name|server
operator|.
name|getLoad
argument_list|()
decl_stmt|;
if|if
condition|(
name|nr
operator|>
name|maxRegions
condition|)
block|{
name|maxRegions
operator|=
name|nr
expr_stmt|;
block|}
if|if
condition|(
name|nr
operator|<
name|minRegions
condition|)
block|{
name|minRegions
operator|=
name|nr
expr_stmt|;
block|}
name|numRegions
operator|+=
name|nr
expr_stmt|;
block|}
if|if
condition|(
name|maxRegions
operator|-
name|minRegions
operator|<
literal|2
condition|)
block|{
comment|// less than 2 between max and min, can't balance
return|return;
block|}
name|int
name|min
init|=
name|numRegions
operator|/
name|numServers
decl_stmt|;
name|int
name|max
init|=
name|numRegions
operator|%
name|numServers
operator|==
literal|0
condition|?
name|min
else|:
name|min
operator|+
literal|1
decl_stmt|;
for|for
control|(
name|ServerAndLoad
name|server
range|:
name|servers
control|)
block|{
name|assertTrue
argument_list|(
name|server
operator|.
name|getLoad
argument_list|()
operator|>=
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|server
operator|.
name|getLoad
argument_list|()
operator|<=
name|max
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|server
operator|.
name|getLoad
argument_list|()
operator|>=
name|min
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|String
name|printStats
parameter_list|(
name|List
argument_list|<
name|ServerAndLoad
argument_list|>
name|servers
parameter_list|)
block|{
name|int
name|numServers
init|=
name|servers
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|totalRegions
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ServerAndLoad
name|server
range|:
name|servers
control|)
block|{
name|totalRegions
operator|+=
name|server
operator|.
name|getLoad
argument_list|()
expr_stmt|;
block|}
name|float
name|average
init|=
operator|(
name|float
operator|)
name|totalRegions
operator|/
name|numServers
decl_stmt|;
name|int
name|max
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|ceil
argument_list|(
name|average
argument_list|)
decl_stmt|;
name|int
name|min
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|floor
argument_list|(
name|average
argument_list|)
decl_stmt|;
return|return
literal|"[srvr="
operator|+
name|numServers
operator|+
literal|" rgns="
operator|+
name|totalRegions
operator|+
literal|" avg="
operator|+
name|average
operator|+
literal|" max="
operator|+
name|max
operator|+
literal|" min="
operator|+
name|min
operator|+
literal|"]"
return|;
block|}
specifier|protected
name|List
argument_list|<
name|ServerAndLoad
argument_list|>
name|convertToList
parameter_list|(
specifier|final
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
parameter_list|)
block|{
name|List
argument_list|<
name|ServerAndLoad
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|ServerAndLoad
argument_list|>
argument_list|(
name|servers
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
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
name|e
range|:
name|servers
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|list
operator|.
name|add
argument_list|(
operator|new
name|ServerAndLoad
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|list
return|;
block|}
specifier|protected
name|String
name|printMock
parameter_list|(
name|List
argument_list|<
name|ServerAndLoad
argument_list|>
name|balancedCluster
parameter_list|)
block|{
name|SortedSet
argument_list|<
name|ServerAndLoad
argument_list|>
name|sorted
init|=
operator|new
name|TreeSet
argument_list|<
name|ServerAndLoad
argument_list|>
argument_list|(
name|balancedCluster
argument_list|)
decl_stmt|;
name|ServerAndLoad
index|[]
name|arr
init|=
name|sorted
operator|.
name|toArray
argument_list|(
operator|new
name|ServerAndLoad
index|[
name|sorted
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
name|sorted
operator|.
name|size
argument_list|()
operator|*
literal|4
operator|+
literal|4
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"{ "
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|arr
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|!=
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" , "
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|arr
index|[
name|i
index|]
operator|.
name|getServerName
argument_list|()
operator|.
name|getHostname
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|arr
index|[
name|i
index|]
operator|.
name|getLoad
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|" }"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * This assumes the RegionPlan HSI instances are the same ones in the map, so    * actually no need to even pass in the map, but I think it's clearer.    *    * @param list    * @param plans    * @return    */
specifier|protected
name|List
argument_list|<
name|ServerAndLoad
argument_list|>
name|reconcile
parameter_list|(
name|List
argument_list|<
name|ServerAndLoad
argument_list|>
name|list
parameter_list|,
name|List
argument_list|<
name|RegionPlan
argument_list|>
name|plans
parameter_list|,
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
parameter_list|)
block|{
name|List
argument_list|<
name|ServerAndLoad
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|ServerAndLoad
argument_list|>
argument_list|(
name|list
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|plans
operator|==
literal|null
condition|)
return|return
name|result
return|;
name|Map
argument_list|<
name|ServerName
argument_list|,
name|ServerAndLoad
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<
name|ServerName
argument_list|,
name|ServerAndLoad
argument_list|>
argument_list|(
name|list
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|ServerAndLoad
name|sl
range|:
name|list
control|)
block|{
name|map
operator|.
name|put
argument_list|(
name|sl
operator|.
name|getServerName
argument_list|()
argument_list|,
name|sl
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|RegionPlan
name|plan
range|:
name|plans
control|)
block|{
name|ServerName
name|source
init|=
name|plan
operator|.
name|getSource
argument_list|()
decl_stmt|;
name|updateLoad
argument_list|(
name|map
argument_list|,
name|source
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|ServerName
name|destination
init|=
name|plan
operator|.
name|getDestination
argument_list|()
decl_stmt|;
name|updateLoad
argument_list|(
name|map
argument_list|,
name|destination
argument_list|,
operator|+
literal|1
argument_list|)
expr_stmt|;
name|servers
operator|.
name|get
argument_list|(
name|source
argument_list|)
operator|.
name|remove
argument_list|(
name|plan
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
name|servers
operator|.
name|get
argument_list|(
name|destination
argument_list|)
operator|.
name|add
argument_list|(
name|plan
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|clear
argument_list|()
expr_stmt|;
name|result
operator|.
name|addAll
argument_list|(
name|map
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|protected
name|void
name|updateLoad
parameter_list|(
specifier|final
name|Map
argument_list|<
name|ServerName
argument_list|,
name|ServerAndLoad
argument_list|>
name|map
parameter_list|,
specifier|final
name|ServerName
name|sn
parameter_list|,
specifier|final
name|int
name|diff
parameter_list|)
block|{
name|ServerAndLoad
name|sal
init|=
name|map
operator|.
name|get
argument_list|(
name|sn
argument_list|)
decl_stmt|;
if|if
condition|(
name|sal
operator|==
literal|null
condition|)
name|sal
operator|=
operator|new
name|ServerAndLoad
argument_list|(
name|sn
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|sal
operator|=
operator|new
name|ServerAndLoad
argument_list|(
name|sn
argument_list|,
name|sal
operator|.
name|getLoad
argument_list|()
operator|+
name|diff
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|sn
argument_list|,
name|sal
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|mockClusterServers
parameter_list|(
name|int
index|[]
name|mockCluster
parameter_list|)
block|{
return|return
name|mockClusterServers
argument_list|(
name|mockCluster
argument_list|,
operator|-
literal|1
argument_list|)
return|;
block|}
specifier|protected
name|BaseLoadBalancer
operator|.
name|Cluster
name|mockCluster
parameter_list|(
name|int
index|[]
name|mockCluster
parameter_list|)
block|{
return|return
operator|new
name|BaseLoadBalancer
operator|.
name|Cluster
argument_list|(
literal|null
argument_list|,
name|mockClusterServers
argument_list|(
name|mockCluster
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|protected
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|mockClusterServers
parameter_list|(
name|int
index|[]
name|mockCluster
parameter_list|,
name|int
name|numTables
parameter_list|)
block|{
name|int
name|numServers
init|=
name|mockCluster
operator|.
name|length
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
name|servers
init|=
operator|new
name|TreeMap
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
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
name|numServers
condition|;
name|i
operator|++
control|)
block|{
name|int
name|numRegions
init|=
name|mockCluster
index|[
name|i
index|]
decl_stmt|;
name|ServerAndLoad
name|sal
init|=
name|randomServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|randomRegions
argument_list|(
name|numRegions
argument_list|,
name|numTables
argument_list|)
decl_stmt|;
name|servers
operator|.
name|put
argument_list|(
name|sal
operator|.
name|getServerName
argument_list|()
argument_list|,
name|regions
argument_list|)
expr_stmt|;
block|}
return|return
name|servers
return|;
block|}
specifier|private
name|Queue
argument_list|<
name|HRegionInfo
argument_list|>
name|regionQueue
init|=
operator|new
name|LinkedList
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|randomRegions
parameter_list|(
name|int
name|numRegions
parameter_list|)
block|{
return|return
name|randomRegions
argument_list|(
name|numRegions
argument_list|,
operator|-
literal|1
argument_list|)
return|;
block|}
specifier|protected
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|randomRegions
parameter_list|(
name|int
name|numRegions
parameter_list|,
name|int
name|numTables
parameter_list|)
block|{
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
operator|new
name|ArrayList
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|(
name|numRegions
argument_list|)
decl_stmt|;
name|byte
index|[]
name|start
init|=
operator|new
name|byte
index|[
literal|16
index|]
decl_stmt|;
name|byte
index|[]
name|end
init|=
operator|new
name|byte
index|[
literal|16
index|]
decl_stmt|;
name|rand
operator|.
name|nextBytes
argument_list|(
name|start
argument_list|)
expr_stmt|;
name|rand
operator|.
name|nextBytes
argument_list|(
name|end
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numRegions
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|regionQueue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|regions
operator|.
name|add
argument_list|(
name|regionQueue
operator|.
name|poll
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|Bytes
operator|.
name|putInt
argument_list|(
name|start
argument_list|,
literal|0
argument_list|,
name|numRegions
operator|<<
literal|1
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|putInt
argument_list|(
name|end
argument_list|,
literal|0
argument_list|,
operator|(
name|numRegions
operator|<<
literal|1
operator|)
operator|+
literal|1
argument_list|)
expr_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table"
operator|+
operator|(
name|numTables
operator|>
literal|0
condition|?
name|rand
operator|.
name|nextInt
argument_list|(
name|numTables
argument_list|)
else|:
name|i
operator|)
argument_list|)
decl_stmt|;
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|,
name|start
argument_list|,
name|end
argument_list|,
literal|false
argument_list|,
name|regionId
operator|++
argument_list|)
decl_stmt|;
name|regions
operator|.
name|add
argument_list|(
name|hri
argument_list|)
expr_stmt|;
block|}
return|return
name|regions
return|;
block|}
specifier|protected
name|void
name|returnRegions
parameter_list|(
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|)
block|{
name|regionQueue
operator|.
name|addAll
argument_list|(
name|regions
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Queue
argument_list|<
name|ServerName
argument_list|>
name|serverQueue
init|=
operator|new
name|LinkedList
argument_list|<
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
name|ServerAndLoad
name|randomServer
parameter_list|(
specifier|final
name|int
name|numRegionsPerServer
parameter_list|)
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|serverQueue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ServerName
name|sn
init|=
name|this
operator|.
name|serverQueue
operator|.
name|poll
argument_list|()
decl_stmt|;
return|return
operator|new
name|ServerAndLoad
argument_list|(
name|sn
argument_list|,
name|numRegionsPerServer
argument_list|)
return|;
block|}
name|String
name|host
init|=
literal|"srv"
operator|+
name|rand
operator|.
name|nextInt
argument_list|(
literal|100000
argument_list|)
decl_stmt|;
name|int
name|port
init|=
name|rand
operator|.
name|nextInt
argument_list|(
literal|60000
argument_list|)
decl_stmt|;
name|long
name|startCode
init|=
name|rand
operator|.
name|nextLong
argument_list|()
decl_stmt|;
name|ServerName
name|sn
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
name|host
argument_list|,
name|port
argument_list|,
name|startCode
argument_list|)
decl_stmt|;
return|return
operator|new
name|ServerAndLoad
argument_list|(
name|sn
argument_list|,
name|numRegionsPerServer
argument_list|)
return|;
block|}
specifier|protected
name|List
argument_list|<
name|ServerAndLoad
argument_list|>
name|randomServers
parameter_list|(
name|int
name|numServers
parameter_list|,
name|int
name|numRegionsPerServer
parameter_list|)
block|{
name|List
argument_list|<
name|ServerAndLoad
argument_list|>
name|servers
init|=
operator|new
name|ArrayList
argument_list|<
name|ServerAndLoad
argument_list|>
argument_list|(
name|numServers
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
name|numServers
condition|;
name|i
operator|++
control|)
block|{
name|servers
operator|.
name|add
argument_list|(
name|randomServer
argument_list|(
name|numRegionsPerServer
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|servers
return|;
block|}
specifier|protected
name|void
name|returnServer
parameter_list|(
name|ServerName
name|server
parameter_list|)
block|{
name|serverQueue
operator|.
name|add
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|returnServers
parameter_list|(
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
parameter_list|)
block|{
name|this
operator|.
name|serverQueue
operator|.
name|addAll
argument_list|(
name|servers
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

