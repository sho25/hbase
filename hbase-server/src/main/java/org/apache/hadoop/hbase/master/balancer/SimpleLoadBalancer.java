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
name|Collections
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
name|NavigableMap
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
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|HBaseInterfaceAudience
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|MinMaxPriorityQueue
import|;
end_import

begin_comment
comment|/**  * Makes decisions about the placement and movement of Regions across  * RegionServers.  *  *<p>Cluster-wide load balancing will occur only when there are no regions in  * transition and according to a fixed period of a time using {@link #balanceCluster(Map)}.  *  *<p>Inline region placement with {@link #immediateAssignment} can be used when  * the Master needs to handle closed regions that it currently does not have  * a destination set for.  This can happen during master failover.  *  *<p>On cluster startup, bulk assignment can be used to determine  * locations for all Regions in a cluster.  *  *<p>This classes produces plans for the   * {@link org.apache.hadoop.hbase.master.AssignmentManager} to execute.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|CONFIG
argument_list|)
specifier|public
class|class
name|SimpleLoadBalancer
extends|extends
name|BaseLoadBalancer
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
name|SimpleLoadBalancer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Random
name|RANDOM
init|=
operator|new
name|Random
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|RegionInfoComparator
name|riComparator
init|=
operator|new
name|RegionInfoComparator
argument_list|()
decl_stmt|;
specifier|private
name|RegionPlan
operator|.
name|RegionPlanComparator
name|rpComparator
init|=
operator|new
name|RegionPlan
operator|.
name|RegionPlanComparator
argument_list|()
decl_stmt|;
comment|/**    * Stores additional per-server information about the regions added/removed    * during the run of the balancing algorithm.    *    * For servers that shed regions, we need to track which regions we have already    * shed.<b>nextRegionForUnload</b> contains the index in the list of regions on    * the server that is the next to be shed.    */
specifier|static
class|class
name|BalanceInfo
block|{
specifier|private
specifier|final
name|int
name|nextRegionForUnload
decl_stmt|;
specifier|private
name|int
name|numRegionsAdded
decl_stmt|;
specifier|public
name|BalanceInfo
parameter_list|(
name|int
name|nextRegionForUnload
parameter_list|,
name|int
name|numRegionsAdded
parameter_list|)
block|{
name|this
operator|.
name|nextRegionForUnload
operator|=
name|nextRegionForUnload
expr_stmt|;
name|this
operator|.
name|numRegionsAdded
operator|=
name|numRegionsAdded
expr_stmt|;
block|}
name|int
name|getNextRegionForUnload
parameter_list|()
block|{
return|return
name|nextRegionForUnload
return|;
block|}
name|int
name|getNumRegionsAdded
parameter_list|()
block|{
return|return
name|numRegionsAdded
return|;
block|}
name|void
name|setNumRegionsAdded
parameter_list|(
name|int
name|numAdded
parameter_list|)
block|{
name|this
operator|.
name|numRegionsAdded
operator|=
name|numAdded
expr_stmt|;
block|}
block|}
comment|/**    * Generate a global load balancing plan according to the specified map of    * server information to the most loaded regions of each server.    *    * The load balancing invariant is that all servers are within 1 region of the    * average number of regions per server.  If the average is an integer number,    * all servers will be balanced to the average.  Otherwise, all servers will    * have either floor(average) or ceiling(average) regions.    *    * HBASE-3609 Modeled regionsToMove using Guava's MinMaxPriorityQueue so that    *   we can fetch from both ends of the queue.    * At the beginning, we check whether there was empty region server    *   just discovered by Master. If so, we alternately choose new / old    *   regions from head / tail of regionsToMove, respectively. This alternation    *   avoids clustering young regions on the newly discovered region server.    *   Otherwise, we choose new regions from head of regionsToMove.    *    * Another improvement from HBASE-3609 is that we assign regions from    *   regionsToMove to underloaded servers in round-robin fashion.    *   Previously one underloaded server would be filled before we move onto    *   the next underloaded server, leading to clustering of young regions.    *    * Finally, we randomly shuffle underloaded servers so that they receive    *   offloaded regions relatively evenly across calls to balanceCluster().    *    * The algorithm is currently implemented as such:    *    *<ol>    *<li>Determine the two valid numbers of regions each server should have,    *<b>MIN</b>=floor(average) and<b>MAX</b>=ceiling(average).    *    *<li>Iterate down the most loaded servers, shedding regions from each so    *     each server hosts exactly<b>MAX</b> regions.  Stop once you reach a    *     server that already has&lt;=<b>MAX</b> regions.    *<p>    *     Order the regions to move from most recent to least.    *    *<li>Iterate down the least loaded servers, assigning regions so each server    *     has exactly<b>MIN</b> regions.  Stop once you reach a server that    *     already has&gt;=<b>MIN</b> regions.    *    *     Regions being assigned to underloaded servers are those that were shed    *     in the previous step.  It is possible that there were not enough    *     regions shed to fill each underloaded server to<b>MIN</b>.  If so we    *     end up with a number of regions required to do so,<b>neededRegions</b>.    *    *     It is also possible that we were able to fill each underloaded but ended    *     up with regions that were unassigned from overloaded servers but that    *     still do not have assignment.    *    *     If neither of these conditions hold (no regions needed to fill the    *     underloaded servers, no regions leftover from overloaded servers),    *     we are done and return.  Otherwise we handle these cases below.    *    *<li>If<b>neededRegions</b> is non-zero (still have underloaded servers),    *     we iterate the most loaded servers again, shedding a single server from    *     each (this brings them from having<b>MAX</b> regions to having    *<b>MIN</b> regions).    *    *<li>We now definitely have more regions that need assignment, either from    *     the previous step or from the original shedding from overloaded servers.    *     Iterate the least loaded servers filling each to<b>MIN</b>.    *    *<li>If we still have more regions that need assignment, again iterate the    *     least loaded servers, this time giving each one (filling them to    *<b>MAX</b>) until we run out.    *    *<li>All servers will now either host<b>MIN</b> or<b>MAX</b> regions.    *    *     In addition, any server hosting&gt;=<b>MAX</b> regions is guaranteed    *     to end up with<b>MAX</b> regions at the end of the balancing.  This    *     ensures the minimal number of regions possible are moved.    *</ol>    *    * TODO: We can at-most reassign the number of regions away from a particular    *       server to be how many they report as most loaded.    *       Should we just keep all assignment in memory?  Any objections?    *       Does this mean we need HeapSize on HMaster?  Or just careful monitor?    *       (current thinking is we will hold all assignments in memory)    *    * @param clusterMap Map of regionservers and their load/region information to    *                   a list of their most loaded regions    * @return a list of regions to be moved, including source and destination,    *         or null if cluster is already balanced    */
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|RegionPlan
argument_list|>
name|balanceCluster
parameter_list|(
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|clusterMap
parameter_list|)
block|{
name|List
argument_list|<
name|RegionPlan
argument_list|>
name|regionsToReturn
init|=
name|balanceMasterRegions
argument_list|(
name|clusterMap
argument_list|)
decl_stmt|;
if|if
condition|(
name|regionsToReturn
operator|!=
literal|null
operator|||
name|clusterMap
operator|==
literal|null
operator|||
name|clusterMap
operator|.
name|size
argument_list|()
operator|<=
literal|1
condition|)
block|{
return|return
name|regionsToReturn
return|;
block|}
if|if
condition|(
name|masterServerName
operator|!=
literal|null
operator|&&
name|clusterMap
operator|.
name|containsKey
argument_list|(
name|masterServerName
argument_list|)
condition|)
block|{
if|if
condition|(
name|clusterMap
operator|.
name|size
argument_list|()
operator|<=
literal|2
condition|)
block|{
return|return
literal|null
return|;
block|}
name|clusterMap
operator|=
operator|new
name|HashMap
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
argument_list|(
name|clusterMap
argument_list|)
expr_stmt|;
name|clusterMap
operator|.
name|remove
argument_list|(
name|masterServerName
argument_list|)
expr_stmt|;
block|}
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
comment|// construct a Cluster object with clusterMap and rest of the
comment|// argument as defaults
name|Cluster
name|c
init|=
operator|new
name|Cluster
argument_list|(
name|clusterMap
argument_list|,
literal|null
argument_list|,
name|this
operator|.
name|regionFinder
argument_list|,
name|this
operator|.
name|rackManager
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|needsBalance
argument_list|(
name|c
argument_list|)
condition|)
return|return
literal|null
return|;
name|ClusterLoadState
name|cs
init|=
operator|new
name|ClusterLoadState
argument_list|(
name|clusterMap
argument_list|)
decl_stmt|;
name|int
name|numServers
init|=
name|cs
operator|.
name|getNumServers
argument_list|()
decl_stmt|;
name|NavigableMap
argument_list|<
name|ServerAndLoad
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|serversByLoad
init|=
name|cs
operator|.
name|getServersByLoad
argument_list|()
decl_stmt|;
name|int
name|numRegions
init|=
name|cs
operator|.
name|getNumRegions
argument_list|()
decl_stmt|;
name|float
name|average
init|=
name|cs
operator|.
name|getLoadAverage
argument_list|()
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
name|average
decl_stmt|;
comment|// Using to check balance result.
name|StringBuilder
name|strBalanceParam
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|strBalanceParam
operator|.
name|append
argument_list|(
literal|"Balance parameter: numRegions="
argument_list|)
operator|.
name|append
argument_list|(
name|numRegions
argument_list|)
operator|.
name|append
argument_list|(
literal|", numServers="
argument_list|)
operator|.
name|append
argument_list|(
name|numServers
argument_list|)
operator|.
name|append
argument_list|(
literal|", max="
argument_list|)
operator|.
name|append
argument_list|(
name|max
argument_list|)
operator|.
name|append
argument_list|(
literal|", min="
argument_list|)
operator|.
name|append
argument_list|(
name|min
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|strBalanceParam
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Balance the cluster
comment|// TODO: Look at data block locality or a more complex load to do this
name|MinMaxPriorityQueue
argument_list|<
name|RegionPlan
argument_list|>
name|regionsToMove
init|=
name|MinMaxPriorityQueue
operator|.
name|orderedBy
argument_list|(
name|rpComparator
argument_list|)
operator|.
name|create
argument_list|()
decl_stmt|;
name|regionsToReturn
operator|=
operator|new
name|ArrayList
argument_list|<
name|RegionPlan
argument_list|>
argument_list|()
expr_stmt|;
comment|// Walk down most loaded, pruning each to the max
name|int
name|serversOverloaded
init|=
literal|0
decl_stmt|;
comment|// flag used to fetch regions from head and tail of list, alternately
name|boolean
name|fetchFromTail
init|=
literal|false
decl_stmt|;
name|Map
argument_list|<
name|ServerName
argument_list|,
name|BalanceInfo
argument_list|>
name|serverBalanceInfo
init|=
operator|new
name|TreeMap
argument_list|<
name|ServerName
argument_list|,
name|BalanceInfo
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ServerAndLoad
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|server
range|:
name|serversByLoad
operator|.
name|descendingMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|ServerAndLoad
name|sal
init|=
name|server
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|int
name|load
init|=
name|sal
operator|.
name|getLoad
argument_list|()
decl_stmt|;
if|if
condition|(
name|load
operator|<=
name|max
condition|)
block|{
name|serverBalanceInfo
operator|.
name|put
argument_list|(
name|sal
operator|.
name|getServerName
argument_list|()
argument_list|,
operator|new
name|BalanceInfo
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
name|serversOverloaded
operator|++
expr_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|server
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|int
name|numToOffload
init|=
name|Math
operator|.
name|min
argument_list|(
name|load
operator|-
name|max
argument_list|,
name|regions
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
comment|// account for the out-of-band regions which were assigned to this server
comment|// after some other region server crashed
name|Collections
operator|.
name|sort
argument_list|(
name|regions
argument_list|,
name|riComparator
argument_list|)
expr_stmt|;
name|int
name|numTaken
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
name|numToOffload
condition|;
control|)
block|{
name|HRegionInfo
name|hri
init|=
name|regions
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
comment|// fetch from head
if|if
condition|(
name|fetchFromTail
condition|)
block|{
name|hri
operator|=
name|regions
operator|.
name|get
argument_list|(
name|regions
operator|.
name|size
argument_list|()
operator|-
literal|1
operator|-
name|i
argument_list|)
expr_stmt|;
block|}
name|i
operator|++
expr_stmt|;
comment|// Don't rebalance special regions.
if|if
condition|(
name|shouldBeOnMaster
argument_list|(
name|hri
argument_list|)
operator|&&
name|masterServerName
operator|.
name|equals
argument_list|(
name|sal
operator|.
name|getServerName
argument_list|()
argument_list|)
condition|)
continue|continue;
name|regionsToMove
operator|.
name|add
argument_list|(
operator|new
name|RegionPlan
argument_list|(
name|hri
argument_list|,
name|sal
operator|.
name|getServerName
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|numTaken
operator|++
expr_stmt|;
if|if
condition|(
name|numTaken
operator|>=
name|numToOffload
condition|)
break|break;
block|}
name|serverBalanceInfo
operator|.
name|put
argument_list|(
name|sal
operator|.
name|getServerName
argument_list|()
argument_list|,
operator|new
name|BalanceInfo
argument_list|(
name|numToOffload
argument_list|,
operator|(
operator|-
literal|1
operator|)
operator|*
name|numTaken
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|int
name|totalNumMoved
init|=
name|regionsToMove
operator|.
name|size
argument_list|()
decl_stmt|;
comment|// Walk down least loaded, filling each to the min
name|int
name|neededRegions
init|=
literal|0
decl_stmt|;
comment|// number of regions needed to bring all up to min
name|fetchFromTail
operator|=
literal|false
expr_stmt|;
name|Map
argument_list|<
name|ServerName
argument_list|,
name|Integer
argument_list|>
name|underloadedServers
init|=
operator|new
name|HashMap
argument_list|<
name|ServerName
argument_list|,
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|maxToTake
init|=
name|numRegions
operator|-
name|min
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ServerAndLoad
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|server
range|:
name|serversByLoad
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|maxToTake
operator|==
literal|0
condition|)
break|break;
comment|// no more to take
name|int
name|load
init|=
name|server
operator|.
name|getKey
argument_list|()
operator|.
name|getLoad
argument_list|()
decl_stmt|;
if|if
condition|(
name|load
operator|>=
name|min
operator|&&
name|load
operator|>
literal|0
condition|)
block|{
continue|continue;
comment|// look for other servers which haven't reached min
block|}
name|int
name|regionsToPut
init|=
name|min
operator|-
name|load
decl_stmt|;
if|if
condition|(
name|regionsToPut
operator|==
literal|0
condition|)
block|{
name|regionsToPut
operator|=
literal|1
expr_stmt|;
block|}
name|maxToTake
operator|-=
name|regionsToPut
expr_stmt|;
name|underloadedServers
operator|.
name|put
argument_list|(
name|server
operator|.
name|getKey
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|,
name|regionsToPut
argument_list|)
expr_stmt|;
block|}
comment|// number of servers that get new regions
name|int
name|serversUnderloaded
init|=
name|underloadedServers
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|incr
init|=
literal|1
decl_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|sns
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|underloadedServers
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|ServerName
index|[
name|serversUnderloaded
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|shuffle
argument_list|(
name|sns
argument_list|,
name|RANDOM
argument_list|)
expr_stmt|;
while|while
condition|(
name|regionsToMove
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|int
name|cnt
init|=
literal|0
decl_stmt|;
name|int
name|i
init|=
name|incr
operator|>
literal|0
condition|?
literal|0
else|:
name|underloadedServers
operator|.
name|size
argument_list|()
operator|-
literal|1
decl_stmt|;
for|for
control|(
init|;
name|i
operator|>=
literal|0
operator|&&
name|i
operator|<
name|underloadedServers
operator|.
name|size
argument_list|()
condition|;
name|i
operator|+=
name|incr
control|)
block|{
if|if
condition|(
name|regionsToMove
operator|.
name|isEmpty
argument_list|()
condition|)
break|break;
name|ServerName
name|si
init|=
name|sns
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|int
name|numToTake
init|=
name|underloadedServers
operator|.
name|get
argument_list|(
name|si
argument_list|)
decl_stmt|;
if|if
condition|(
name|numToTake
operator|==
literal|0
condition|)
continue|continue;
name|addRegionPlan
argument_list|(
name|regionsToMove
argument_list|,
name|fetchFromTail
argument_list|,
name|si
argument_list|,
name|regionsToReturn
argument_list|)
expr_stmt|;
name|underloadedServers
operator|.
name|put
argument_list|(
name|si
argument_list|,
name|numToTake
operator|-
literal|1
argument_list|)
expr_stmt|;
name|cnt
operator|++
expr_stmt|;
name|BalanceInfo
name|bi
init|=
name|serverBalanceInfo
operator|.
name|get
argument_list|(
name|si
argument_list|)
decl_stmt|;
if|if
condition|(
name|bi
operator|==
literal|null
condition|)
block|{
name|bi
operator|=
operator|new
name|BalanceInfo
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|serverBalanceInfo
operator|.
name|put
argument_list|(
name|si
argument_list|,
name|bi
argument_list|)
expr_stmt|;
block|}
name|bi
operator|.
name|setNumRegionsAdded
argument_list|(
name|bi
operator|.
name|getNumRegionsAdded
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cnt
operator|==
literal|0
condition|)
break|break;
comment|// iterates underloadedServers in the other direction
name|incr
operator|=
operator|-
name|incr
expr_stmt|;
block|}
for|for
control|(
name|Integer
name|i
range|:
name|underloadedServers
operator|.
name|values
argument_list|()
control|)
block|{
comment|// If we still want to take some, increment needed
name|neededRegions
operator|+=
name|i
expr_stmt|;
block|}
comment|// If none needed to fill all to min and none left to drain all to max,
comment|// we are done
if|if
condition|(
name|neededRegions
operator|==
literal|0
operator|&&
name|regionsToMove
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|long
name|endTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Calculated a load balance in "
operator|+
operator|(
name|endTime
operator|-
name|startTime
operator|)
operator|+
literal|"ms. "
operator|+
literal|"Moving "
operator|+
name|totalNumMoved
operator|+
literal|" regions off of "
operator|+
name|serversOverloaded
operator|+
literal|" overloaded servers onto "
operator|+
name|serversUnderloaded
operator|+
literal|" less loaded servers"
argument_list|)
expr_stmt|;
return|return
name|regionsToReturn
return|;
block|}
comment|// Need to do a second pass.
comment|// Either more regions to assign out or servers that are still underloaded
comment|// If we need more to fill min, grab one from each most loaded until enough
if|if
condition|(
name|neededRegions
operator|!=
literal|0
condition|)
block|{
comment|// Walk down most loaded, grabbing one from each until we get enough
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ServerAndLoad
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|server
range|:
name|serversByLoad
operator|.
name|descendingMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|BalanceInfo
name|balanceInfo
init|=
name|serverBalanceInfo
operator|.
name|get
argument_list|(
name|server
operator|.
name|getKey
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|idx
init|=
name|balanceInfo
operator|==
literal|null
condition|?
literal|0
else|:
name|balanceInfo
operator|.
name|getNextRegionForUnload
argument_list|()
decl_stmt|;
if|if
condition|(
name|idx
operator|>=
name|server
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
condition|)
break|break;
name|HRegionInfo
name|region
init|=
name|server
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|(
name|idx
argument_list|)
decl_stmt|;
if|if
condition|(
name|region
operator|.
name|isMetaRegion
argument_list|()
condition|)
continue|continue;
comment|// Don't move meta regions.
name|regionsToMove
operator|.
name|add
argument_list|(
operator|new
name|RegionPlan
argument_list|(
name|region
argument_list|,
name|server
operator|.
name|getKey
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|totalNumMoved
operator|++
expr_stmt|;
if|if
condition|(
operator|--
name|neededRegions
operator|==
literal|0
condition|)
block|{
comment|// No more regions needed, done shedding
break|break;
block|}
block|}
block|}
comment|// Now we have a set of regions that must be all assigned out
comment|// Assign each underloaded up to the min, then if leftovers, assign to max
comment|// Walk down least loaded, assigning to each to fill up to min
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ServerAndLoad
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|server
range|:
name|serversByLoad
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|int
name|regionCount
init|=
name|server
operator|.
name|getKey
argument_list|()
operator|.
name|getLoad
argument_list|()
decl_stmt|;
if|if
condition|(
name|regionCount
operator|>=
name|min
condition|)
break|break;
name|BalanceInfo
name|balanceInfo
init|=
name|serverBalanceInfo
operator|.
name|get
argument_list|(
name|server
operator|.
name|getKey
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|balanceInfo
operator|!=
literal|null
condition|)
block|{
name|regionCount
operator|+=
name|balanceInfo
operator|.
name|getNumRegionsAdded
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|regionCount
operator|>=
name|min
condition|)
block|{
continue|continue;
block|}
name|int
name|numToTake
init|=
name|min
operator|-
name|regionCount
decl_stmt|;
name|int
name|numTaken
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|numTaken
operator|<
name|numToTake
operator|&&
literal|0
operator|<
name|regionsToMove
operator|.
name|size
argument_list|()
condition|)
block|{
name|addRegionPlan
argument_list|(
name|regionsToMove
argument_list|,
name|fetchFromTail
argument_list|,
name|server
operator|.
name|getKey
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|,
name|regionsToReturn
argument_list|)
expr_stmt|;
name|numTaken
operator|++
expr_stmt|;
block|}
block|}
comment|// If we still have regions to dish out, assign underloaded to max
if|if
condition|(
literal|0
operator|<
name|regionsToMove
operator|.
name|size
argument_list|()
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ServerAndLoad
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|server
range|:
name|serversByLoad
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|int
name|regionCount
init|=
name|server
operator|.
name|getKey
argument_list|()
operator|.
name|getLoad
argument_list|()
decl_stmt|;
name|BalanceInfo
name|balanceInfo
init|=
name|serverBalanceInfo
operator|.
name|get
argument_list|(
name|server
operator|.
name|getKey
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|balanceInfo
operator|!=
literal|null
condition|)
block|{
name|regionCount
operator|+=
name|balanceInfo
operator|.
name|getNumRegionsAdded
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|regionCount
operator|>=
name|max
condition|)
block|{
break|break;
block|}
name|addRegionPlan
argument_list|(
name|regionsToMove
argument_list|,
name|fetchFromTail
argument_list|,
name|server
operator|.
name|getKey
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|,
name|regionsToReturn
argument_list|)
expr_stmt|;
if|if
condition|(
name|regionsToMove
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
name|long
name|endTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|regionsToMove
operator|.
name|isEmpty
argument_list|()
operator|||
name|neededRegions
operator|!=
literal|0
condition|)
block|{
comment|// Emit data so can diagnose how balancer went astray.
name|LOG
operator|.
name|warn
argument_list|(
literal|"regionsToMove="
operator|+
name|totalNumMoved
operator|+
literal|", numServers="
operator|+
name|numServers
operator|+
literal|", serversOverloaded="
operator|+
name|serversOverloaded
operator|+
literal|", serversUnderloaded="
operator|+
name|serversUnderloaded
argument_list|)
expr_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
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
name|clusterMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|sb
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|warn
argument_list|(
literal|"Input "
operator|+
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// All done!
name|LOG
operator|.
name|info
argument_list|(
literal|"Done. Calculated a load balance in "
operator|+
operator|(
name|endTime
operator|-
name|startTime
operator|)
operator|+
literal|"ms. "
operator|+
literal|"Moving "
operator|+
name|totalNumMoved
operator|+
literal|" regions off of "
operator|+
name|serversOverloaded
operator|+
literal|" overloaded servers onto "
operator|+
name|serversUnderloaded
operator|+
literal|" less loaded servers"
argument_list|)
expr_stmt|;
return|return
name|regionsToReturn
return|;
block|}
comment|/**    * Add a region from the head or tail to the List of regions to return.    */
specifier|private
name|void
name|addRegionPlan
parameter_list|(
specifier|final
name|MinMaxPriorityQueue
argument_list|<
name|RegionPlan
argument_list|>
name|regionsToMove
parameter_list|,
specifier|final
name|boolean
name|fetchFromTail
parameter_list|,
specifier|final
name|ServerName
name|sn
parameter_list|,
name|List
argument_list|<
name|RegionPlan
argument_list|>
name|regionsToReturn
parameter_list|)
block|{
name|RegionPlan
name|rp
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|fetchFromTail
condition|)
name|rp
operator|=
name|regionsToMove
operator|.
name|remove
argument_list|()
expr_stmt|;
else|else
name|rp
operator|=
name|regionsToMove
operator|.
name|removeLast
argument_list|()
expr_stmt|;
name|rp
operator|.
name|setDestination
argument_list|(
name|sn
argument_list|)
expr_stmt|;
name|regionsToReturn
operator|.
name|add
argument_list|(
name|rp
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

