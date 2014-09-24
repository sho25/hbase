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
name|NamespaceDescriptor
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
name|master
operator|.
name|ServerManager
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
name|SnapshotOfRegionAssignmentFromMeta
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
name|FavoredNodesPlan
operator|.
name|Position
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
name|Pair
import|;
end_import

begin_comment
comment|/**  * An implementation of the {@link LoadBalancer} that assigns favored nodes for  * each region. There is a Primary RegionServer that hosts the region, and then  * there is Secondary and Tertiary RegionServers. Currently, the favored nodes  * information is used in creating HDFS files - the Primary RegionServer passes  * the primary, secondary, tertiary node addresses as hints to the DistributedFileSystem  * API for creating files on the filesystem. These nodes are treated as hints by  * the HDFS to place the blocks of the file. This alleviates the problem to do with  * reading from remote nodes (since we can make the Secondary RegionServer as the new  * Primary RegionServer) after a region is recovered. This should help provide consistent  * read latencies for the regions even when their primary region servers die.  *  */
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
name|FavoredNodeLoadBalancer
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
name|FavoredNodeLoadBalancer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|FavoredNodesPlan
name|globalFavoredNodesAssignmentPlan
decl_stmt|;
specifier|private
name|RackManager
name|rackManager
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|globalFavoredNodesAssignmentPlan
operator|=
operator|new
name|FavoredNodesPlan
argument_list|()
expr_stmt|;
name|this
operator|.
name|rackManager
operator|=
operator|new
name|RackManager
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|super
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
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
name|clusterState
parameter_list|)
block|{
comment|//TODO. Look at is whether Stochastic loadbalancer can be integrated with this
name|List
argument_list|<
name|RegionPlan
argument_list|>
name|plans
init|=
operator|new
name|ArrayList
argument_list|<
name|RegionPlan
argument_list|>
argument_list|()
decl_stmt|;
comment|//perform a scan of the meta to get the latest updates (if any)
name|SnapshotOfRegionAssignmentFromMeta
name|snaphotOfRegionAssignment
init|=
operator|new
name|SnapshotOfRegionAssignmentFromMeta
argument_list|(
name|super
operator|.
name|services
operator|.
name|getShortCircuitConnection
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|snaphotOfRegionAssignment
operator|.
name|initialize
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ie
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Not running balancer since exception was thrown "
operator|+
name|ie
argument_list|)
expr_stmt|;
return|return
name|plans
return|;
block|}
name|globalFavoredNodesAssignmentPlan
operator|=
name|snaphotOfRegionAssignment
operator|.
name|getExistingAssignmentPlan
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|ServerName
argument_list|,
name|ServerName
argument_list|>
name|serverNameToServerNameWithoutCode
init|=
operator|new
name|HashMap
argument_list|<
name|ServerName
argument_list|,
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|ServerName
argument_list|,
name|ServerName
argument_list|>
name|serverNameWithoutCodeToServerName
init|=
operator|new
name|HashMap
argument_list|<
name|ServerName
argument_list|,
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
name|ServerManager
name|serverMgr
init|=
name|super
operator|.
name|services
operator|.
name|getServerManager
argument_list|()
decl_stmt|;
for|for
control|(
name|ServerName
name|sn
range|:
name|serverMgr
operator|.
name|getOnlineServersList
argument_list|()
control|)
block|{
name|ServerName
name|s
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
name|ServerName
operator|.
name|NON_STARTCODE
argument_list|)
decl_stmt|;
name|serverNameToServerNameWithoutCode
operator|.
name|put
argument_list|(
name|sn
argument_list|,
name|s
argument_list|)
expr_stmt|;
name|serverNameWithoutCodeToServerName
operator|.
name|put
argument_list|(
name|s
argument_list|,
name|sn
argument_list|)
expr_stmt|;
block|}
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
name|clusterState
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|ServerName
name|currentServer
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
comment|//get a server without the startcode for the currentServer
name|ServerName
name|currentServerWithoutStartCode
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
name|currentServer
operator|.
name|getHostname
argument_list|()
argument_list|,
name|currentServer
operator|.
name|getPort
argument_list|()
argument_list|,
name|ServerName
operator|.
name|NON_STARTCODE
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|list
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|region
range|:
name|list
control|)
block|{
if|if
condition|(
name|region
operator|.
name|getTable
argument_list|()
operator|.
name|getNamespaceAsString
argument_list|()
operator|.
name|equals
argument_list|(
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE_NAME_STR
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|List
argument_list|<
name|ServerName
argument_list|>
name|favoredNodes
init|=
name|globalFavoredNodesAssignmentPlan
operator|.
name|getFavoredNodes
argument_list|(
name|region
argument_list|)
decl_stmt|;
if|if
condition|(
name|favoredNodes
operator|==
literal|null
operator|||
name|favoredNodes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|equals
argument_list|(
name|currentServerWithoutStartCode
argument_list|)
condition|)
block|{
continue|continue;
comment|//either favorednodes does not exist or we are already on the primary node
block|}
name|ServerName
name|destination
init|=
literal|null
decl_stmt|;
comment|//check whether the primary is available
name|destination
operator|=
name|serverNameWithoutCodeToServerName
operator|.
name|get
argument_list|(
name|favoredNodes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|destination
operator|==
literal|null
condition|)
block|{
comment|//check whether the region is on secondary/tertiary
if|if
condition|(
name|currentServerWithoutStartCode
operator|.
name|equals
argument_list|(
name|favoredNodes
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|||
name|currentServerWithoutStartCode
operator|.
name|equals
argument_list|(
name|favoredNodes
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
condition|)
block|{
continue|continue;
block|}
comment|//the region is currently on none of the favored nodes
comment|//get it on one of them if possible
name|ServerLoad
name|l1
init|=
name|super
operator|.
name|services
operator|.
name|getServerManager
argument_list|()
operator|.
name|getLoad
argument_list|(
name|serverNameWithoutCodeToServerName
operator|.
name|get
argument_list|(
name|favoredNodes
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|ServerLoad
name|l2
init|=
name|super
operator|.
name|services
operator|.
name|getServerManager
argument_list|()
operator|.
name|getLoad
argument_list|(
name|serverNameWithoutCodeToServerName
operator|.
name|get
argument_list|(
name|favoredNodes
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|l1
operator|!=
literal|null
operator|&&
name|l2
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|l1
operator|.
name|getLoad
argument_list|()
operator|>
name|l2
operator|.
name|getLoad
argument_list|()
condition|)
block|{
name|destination
operator|=
name|serverNameWithoutCodeToServerName
operator|.
name|get
argument_list|(
name|favoredNodes
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|destination
operator|=
name|serverNameWithoutCodeToServerName
operator|.
name|get
argument_list|(
name|favoredNodes
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|l1
operator|!=
literal|null
condition|)
block|{
name|destination
operator|=
name|serverNameWithoutCodeToServerName
operator|.
name|get
argument_list|(
name|favoredNodes
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|l2
operator|!=
literal|null
condition|)
block|{
name|destination
operator|=
name|serverNameWithoutCodeToServerName
operator|.
name|get
argument_list|(
name|favoredNodes
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|destination
operator|!=
literal|null
condition|)
block|{
name|RegionPlan
name|plan
init|=
operator|new
name|RegionPlan
argument_list|(
name|region
argument_list|,
name|currentServer
argument_list|,
name|destination
argument_list|)
decl_stmt|;
name|plans
operator|.
name|add
argument_list|(
name|plan
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|plans
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|roundRobinAssignment
parameter_list|(
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
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
name|assignmentMap
decl_stmt|;
try|try
block|{
name|FavoredNodeAssignmentHelper
name|assignmentHelper
init|=
operator|new
name|FavoredNodeAssignmentHelper
argument_list|(
name|servers
argument_list|,
name|rackManager
argument_list|)
decl_stmt|;
name|assignmentHelper
operator|.
name|initialize
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|assignmentHelper
operator|.
name|canPlaceFavoredNodes
argument_list|()
condition|)
block|{
return|return
name|super
operator|.
name|roundRobinAssignment
argument_list|(
name|regions
argument_list|,
name|servers
argument_list|)
return|;
block|}
comment|// Segregate the regions into two types:
comment|// 1. The regions that have favored node assignment, and where at least
comment|//    one of the favored node is still alive. In this case, try to adhere
comment|//    to the current favored nodes assignment as much as possible - i.e.,
comment|//    if the current primary is gone, then make the secondary or tertiary
comment|//    as the new host for the region (based on their current load).
comment|//    Note that we don't change the favored
comment|//    node assignments here (even though one or more favored node is currently
comment|//    down). It is up to the balanceCluster to do this hard work. The HDFS
comment|//    can handle the fact that some nodes in the favored nodes hint is down
comment|//    It'd allocate some other DNs. In combination with stale settings for HDFS,
comment|//    we should be just fine.
comment|// 2. The regions that currently don't have favored node assignment. We will
comment|//    need to come up with favored nodes assignments for them. The corner case
comment|//    in (1) above is that all the nodes are unavailable and in that case, we
comment|//    will note that this region doesn't have favored nodes.
name|Pair
argument_list|<
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|segregatedRegions
init|=
name|segregateRegionsAndAssignRegionsWithFavoredNodes
argument_list|(
name|regions
argument_list|,
name|servers
argument_list|)
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
name|regionsWithFavoredNodesMap
init|=
name|segregatedRegions
operator|.
name|getFirst
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionsWithNoFavoredNodes
init|=
name|segregatedRegions
operator|.
name|getSecond
argument_list|()
decl_stmt|;
name|assignmentMap
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
argument_list|()
expr_stmt|;
name|roundRobinAssignmentImpl
argument_list|(
name|assignmentHelper
argument_list|,
name|assignmentMap
argument_list|,
name|regionsWithNoFavoredNodes
argument_list|,
name|servers
argument_list|)
expr_stmt|;
comment|// merge the assignment maps
name|assignmentMap
operator|.
name|putAll
argument_list|(
name|regionsWithFavoredNodesMap
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Encountered exception while doing favored-nodes assignment "
operator|+
name|ex
operator|+
literal|" Falling back to regular assignment"
argument_list|)
expr_stmt|;
name|assignmentMap
operator|=
name|super
operator|.
name|roundRobinAssignment
argument_list|(
name|regions
argument_list|,
name|servers
argument_list|)
expr_stmt|;
block|}
return|return
name|assignmentMap
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|randomAssignment
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
parameter_list|)
block|{
try|try
block|{
name|FavoredNodeAssignmentHelper
name|assignmentHelper
init|=
operator|new
name|FavoredNodeAssignmentHelper
argument_list|(
name|servers
argument_list|,
name|rackManager
argument_list|)
decl_stmt|;
name|assignmentHelper
operator|.
name|initialize
argument_list|()
expr_stmt|;
name|ServerName
name|primary
init|=
name|super
operator|.
name|randomAssignment
argument_list|(
name|regionInfo
argument_list|,
name|servers
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|assignmentHelper
operator|.
name|canPlaceFavoredNodes
argument_list|()
condition|)
block|{
return|return
name|primary
return|;
block|}
name|List
argument_list|<
name|ServerName
argument_list|>
name|favoredNodes
init|=
name|globalFavoredNodesAssignmentPlan
operator|.
name|getFavoredNodes
argument_list|(
name|regionInfo
argument_list|)
decl_stmt|;
comment|// check if we have a favored nodes mapping for this region and if so, return
comment|// a server from the favored nodes list if the passed 'servers' contains this
comment|// server as well (available servers, that is)
if|if
condition|(
name|favoredNodes
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ServerName
name|s
range|:
name|favoredNodes
control|)
block|{
name|ServerName
name|serverWithLegitStartCode
init|=
name|availableServersContains
argument_list|(
name|servers
argument_list|,
name|s
argument_list|)
decl_stmt|;
if|if
condition|(
name|serverWithLegitStartCode
operator|!=
literal|null
condition|)
block|{
return|return
name|serverWithLegitStartCode
return|;
block|}
block|}
block|}
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
literal|1
argument_list|)
decl_stmt|;
name|regions
operator|.
name|add
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|primaryRSMap
init|=
operator|new
name|HashMap
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|primaryRSMap
operator|.
name|put
argument_list|(
name|regionInfo
argument_list|,
name|primary
argument_list|)
expr_stmt|;
name|assignSecondaryAndTertiaryNodesForRegion
argument_list|(
name|assignmentHelper
argument_list|,
name|regions
argument_list|,
name|primaryRSMap
argument_list|)
expr_stmt|;
return|return
name|primary
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Encountered exception while doing favored-nodes (random)assignment "
operator|+
name|ex
operator|+
literal|" Falling back to regular assignment"
argument_list|)
expr_stmt|;
return|return
name|super
operator|.
name|randomAssignment
argument_list|(
name|regionInfo
argument_list|,
name|servers
argument_list|)
return|;
block|}
block|}
specifier|private
name|Pair
argument_list|<
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|segregateRegionsAndAssignRegionsWithFavoredNodes
parameter_list|(
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|availableServers
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
name|assignmentMapForFavoredNodes
init|=
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
name|regions
operator|.
name|size
argument_list|()
operator|/
literal|2
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionsWithNoFavoredNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|(
name|regions
operator|.
name|size
argument_list|()
operator|/
literal|2
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|region
range|:
name|regions
control|)
block|{
name|List
argument_list|<
name|ServerName
argument_list|>
name|favoredNodes
init|=
name|globalFavoredNodesAssignmentPlan
operator|.
name|getFavoredNodes
argument_list|(
name|region
argument_list|)
decl_stmt|;
name|ServerName
name|primaryHost
init|=
literal|null
decl_stmt|;
name|ServerName
name|secondaryHost
init|=
literal|null
decl_stmt|;
name|ServerName
name|tertiaryHost
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|favoredNodes
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ServerName
name|s
range|:
name|favoredNodes
control|)
block|{
name|ServerName
name|serverWithLegitStartCode
init|=
name|availableServersContains
argument_list|(
name|availableServers
argument_list|,
name|s
argument_list|)
decl_stmt|;
if|if
condition|(
name|serverWithLegitStartCode
operator|!=
literal|null
condition|)
block|{
name|FavoredNodesPlan
operator|.
name|Position
name|position
init|=
name|FavoredNodesPlan
operator|.
name|getFavoredServerPosition
argument_list|(
name|favoredNodes
argument_list|,
name|s
argument_list|)
decl_stmt|;
if|if
condition|(
name|Position
operator|.
name|PRIMARY
operator|.
name|equals
argument_list|(
name|position
argument_list|)
condition|)
block|{
name|primaryHost
operator|=
name|serverWithLegitStartCode
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Position
operator|.
name|SECONDARY
operator|.
name|equals
argument_list|(
name|position
argument_list|)
condition|)
block|{
name|secondaryHost
operator|=
name|serverWithLegitStartCode
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Position
operator|.
name|TERTIARY
operator|.
name|equals
argument_list|(
name|position
argument_list|)
condition|)
block|{
name|tertiaryHost
operator|=
name|serverWithLegitStartCode
expr_stmt|;
block|}
block|}
block|}
name|assignRegionToAvailableFavoredNode
argument_list|(
name|assignmentMapForFavoredNodes
argument_list|,
name|region
argument_list|,
name|primaryHost
argument_list|,
name|secondaryHost
argument_list|,
name|tertiaryHost
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|primaryHost
operator|==
literal|null
operator|&&
name|secondaryHost
operator|==
literal|null
operator|&&
name|tertiaryHost
operator|==
literal|null
condition|)
block|{
comment|//all favored nodes unavailable
name|regionsWithNoFavoredNodes
operator|.
name|add
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|Pair
argument_list|<
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
argument_list|(
name|assignmentMapForFavoredNodes
argument_list|,
name|regionsWithNoFavoredNodes
argument_list|)
return|;
block|}
comment|// Do a check of the hostname and port and return the servername from the servers list
comment|// that matched (the favoredNode will have a startcode of -1 but we want the real
comment|// server with the legit startcode
specifier|private
name|ServerName
name|availableServersContains
parameter_list|(
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
parameter_list|,
name|ServerName
name|favoredNode
parameter_list|)
block|{
for|for
control|(
name|ServerName
name|server
range|:
name|servers
control|)
block|{
if|if
condition|(
name|ServerName
operator|.
name|isSameHostnameAndPort
argument_list|(
name|favoredNode
argument_list|,
name|server
argument_list|)
condition|)
block|{
return|return
name|server
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|assignRegionToAvailableFavoredNode
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
name|assignmentMapForFavoredNodes
parameter_list|,
name|HRegionInfo
name|region
parameter_list|,
name|ServerName
name|primaryHost
parameter_list|,
name|ServerName
name|secondaryHost
parameter_list|,
name|ServerName
name|tertiaryHost
parameter_list|)
block|{
if|if
condition|(
name|primaryHost
operator|!=
literal|null
condition|)
block|{
name|addRegionToMap
argument_list|(
name|assignmentMapForFavoredNodes
argument_list|,
name|region
argument_list|,
name|primaryHost
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|secondaryHost
operator|!=
literal|null
operator|&&
name|tertiaryHost
operator|!=
literal|null
condition|)
block|{
comment|// assign the region to the one with a lower load
comment|// (both have the desired hdfs blocks)
name|ServerName
name|s
decl_stmt|;
name|ServerLoad
name|tertiaryLoad
init|=
name|super
operator|.
name|services
operator|.
name|getServerManager
argument_list|()
operator|.
name|getLoad
argument_list|(
name|tertiaryHost
argument_list|)
decl_stmt|;
name|ServerLoad
name|secondaryLoad
init|=
name|super
operator|.
name|services
operator|.
name|getServerManager
argument_list|()
operator|.
name|getLoad
argument_list|(
name|secondaryHost
argument_list|)
decl_stmt|;
if|if
condition|(
name|secondaryLoad
operator|.
name|getLoad
argument_list|()
operator|<
name|tertiaryLoad
operator|.
name|getLoad
argument_list|()
condition|)
block|{
name|s
operator|=
name|secondaryHost
expr_stmt|;
block|}
else|else
block|{
name|s
operator|=
name|tertiaryHost
expr_stmt|;
block|}
name|addRegionToMap
argument_list|(
name|assignmentMapForFavoredNodes
argument_list|,
name|region
argument_list|,
name|s
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|secondaryHost
operator|!=
literal|null
condition|)
block|{
name|addRegionToMap
argument_list|(
name|assignmentMapForFavoredNodes
argument_list|,
name|region
argument_list|,
name|secondaryHost
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|tertiaryHost
operator|!=
literal|null
condition|)
block|{
name|addRegionToMap
argument_list|(
name|assignmentMapForFavoredNodes
argument_list|,
name|region
argument_list|,
name|tertiaryHost
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|addRegionToMap
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
name|assignmentMapForFavoredNodes
parameter_list|,
name|HRegionInfo
name|region
parameter_list|,
name|ServerName
name|host
parameter_list|)
block|{
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionsOnServer
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|(
name|regionsOnServer
operator|=
name|assignmentMapForFavoredNodes
operator|.
name|get
argument_list|(
name|host
argument_list|)
operator|)
operator|==
literal|null
condition|)
block|{
name|regionsOnServer
operator|=
operator|new
name|ArrayList
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|()
expr_stmt|;
name|assignmentMapForFavoredNodes
operator|.
name|put
argument_list|(
name|host
argument_list|,
name|regionsOnServer
argument_list|)
expr_stmt|;
block|}
name|regionsOnServer
operator|.
name|add
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|ServerName
argument_list|>
name|getFavoredNodes
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|)
block|{
return|return
name|this
operator|.
name|globalFavoredNodesAssignmentPlan
operator|.
name|getFavoredNodes
argument_list|(
name|regionInfo
argument_list|)
return|;
block|}
specifier|private
name|void
name|roundRobinAssignmentImpl
parameter_list|(
name|FavoredNodeAssignmentHelper
name|assignmentHelper
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
name|assignmentMap
parameter_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
parameter_list|)
block|{
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|primaryRSMap
init|=
operator|new
name|HashMap
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
comment|// figure the primary RSs
name|assignmentHelper
operator|.
name|placePrimaryRSAsRoundRobin
argument_list|(
name|assignmentMap
argument_list|,
name|primaryRSMap
argument_list|,
name|regions
argument_list|)
expr_stmt|;
name|assignSecondaryAndTertiaryNodesForRegion
argument_list|(
name|assignmentHelper
argument_list|,
name|regions
argument_list|,
name|primaryRSMap
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assignSecondaryAndTertiaryNodesForRegion
parameter_list|(
name|FavoredNodeAssignmentHelper
name|assignmentHelper
parameter_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|,
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|primaryRSMap
parameter_list|)
block|{
comment|// figure the secondary and tertiary RSs
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
index|[]
argument_list|>
name|secondaryAndTertiaryRSMap
init|=
name|assignmentHelper
operator|.
name|placeSecondaryAndTertiaryRS
argument_list|(
name|primaryRSMap
argument_list|)
decl_stmt|;
comment|// now record all the assignments so that we can serve queries later
for|for
control|(
name|HRegionInfo
name|region
range|:
name|regions
control|)
block|{
comment|// Store the favored nodes without startCode for the ServerName objects
comment|// We don't care about the startcode; but only the hostname really
name|List
argument_list|<
name|ServerName
argument_list|>
name|favoredNodesForRegion
init|=
operator|new
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|ServerName
name|sn
init|=
name|primaryRSMap
operator|.
name|get
argument_list|(
name|region
argument_list|)
decl_stmt|;
name|favoredNodesForRegion
operator|.
name|add
argument_list|(
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
name|ServerName
operator|.
name|NON_STARTCODE
argument_list|)
argument_list|)
expr_stmt|;
name|ServerName
index|[]
name|secondaryAndTertiaryNodes
init|=
name|secondaryAndTertiaryRSMap
operator|.
name|get
argument_list|(
name|region
argument_list|)
decl_stmt|;
if|if
condition|(
name|secondaryAndTertiaryNodes
operator|!=
literal|null
condition|)
block|{
name|favoredNodesForRegion
operator|.
name|add
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
name|secondaryAndTertiaryNodes
index|[
literal|0
index|]
operator|.
name|getHostname
argument_list|()
argument_list|,
name|secondaryAndTertiaryNodes
index|[
literal|0
index|]
operator|.
name|getPort
argument_list|()
argument_list|,
name|ServerName
operator|.
name|NON_STARTCODE
argument_list|)
argument_list|)
expr_stmt|;
name|favoredNodesForRegion
operator|.
name|add
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
name|secondaryAndTertiaryNodes
index|[
literal|1
index|]
operator|.
name|getHostname
argument_list|()
argument_list|,
name|secondaryAndTertiaryNodes
index|[
literal|1
index|]
operator|.
name|getPort
argument_list|()
argument_list|,
name|ServerName
operator|.
name|NON_STARTCODE
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|globalFavoredNodesAssignmentPlan
operator|.
name|updateFavoredNodesMap
argument_list|(
name|region
argument_list|,
name|favoredNodesForRegion
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

