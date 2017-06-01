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
name|rsgroup
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|ArrayListMultimap
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
name|LinkedListMultimap
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
name|ListMultimap
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
name|Lists
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
name|Maps
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
name|Collection
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
name|HashSet
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
name|Set
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
name|HBaseIOException
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
name|constraint
operator|.
name|ConstraintException
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
name|MasterServices
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
name|balancer
operator|.
name|StochasticLoadBalancer
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
name|net
operator|.
name|Address
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * GroupBasedLoadBalancer, used when Region Server Grouping is configured (HBase-6721)  * It does region balance based on a table's group membership.  *  * Most assignment methods contain two exclusive code paths: Online - when the group  * table is online and Offline - when it is unavailable.  *  * During Offline, assignments are assigned based on cached information in zookeeper.  * If unavailable (ie bootstrap) then regions are assigned randomly.  *  * Once the GROUP table has been assigned, the balancer switches to Online and will then  * start providing appropriate assignments for user tables.  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RSGroupBasedLoadBalancer
implements|implements
name|RSGroupableBalancer
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
name|RSGroupBasedLoadBalancer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|config
decl_stmt|;
specifier|private
name|ClusterStatus
name|clusterStatus
decl_stmt|;
specifier|private
name|MasterServices
name|masterServices
decl_stmt|;
specifier|private
specifier|volatile
name|RSGroupInfoManager
name|rsGroupInfoManager
decl_stmt|;
specifier|private
name|LoadBalancer
name|internalBalancer
decl_stmt|;
comment|/**    * Used by reflection in {@link org.apache.hadoop.hbase.master.balancer.LoadBalancerFactory}.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|RSGroupBasedLoadBalancer
parameter_list|()
block|{}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|config
return|;
block|}
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
name|this
operator|.
name|config
operator|=
name|conf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setClusterStatus
parameter_list|(
name|ClusterStatus
name|st
parameter_list|)
block|{
name|this
operator|.
name|clusterStatus
operator|=
name|st
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setMasterServices
parameter_list|(
name|MasterServices
name|masterServices
parameter_list|)
block|{
name|this
operator|.
name|masterServices
operator|=
name|masterServices
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
name|TableName
name|tableName
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
name|clusterState
parameter_list|)
throws|throws
name|HBaseIOException
block|{
return|return
name|balanceCluster
argument_list|(
name|clusterState
argument_list|)
return|;
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
throws|throws
name|HBaseIOException
block|{
if|if
condition|(
operator|!
name|isOnline
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ConstraintException
argument_list|(
name|RSGroupInfoManager
operator|.
name|RSGROUP_TABLE_NAME
operator|+
literal|" is not online, unable to perform balance"
argument_list|)
throw|;
block|}
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|correctedState
init|=
name|correctAssignments
argument_list|(
name|clusterState
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|RegionPlan
argument_list|>
name|regionPlans
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|misplacedRegions
init|=
name|correctedState
operator|.
name|get
argument_list|(
name|LoadBalancer
operator|.
name|BOGUS_SERVER_NAME
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|regionInfo
range|:
name|misplacedRegions
control|)
block|{
name|regionPlans
operator|.
name|add
argument_list|(
operator|new
name|RegionPlan
argument_list|(
name|regionInfo
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|List
argument_list|<
name|RSGroupInfo
argument_list|>
name|rsgi
init|=
name|rsGroupInfoManager
operator|.
name|listRSGroups
argument_list|()
decl_stmt|;
for|for
control|(
name|RSGroupInfo
name|info
range|:
name|rsgi
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
name|groupClusterState
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|TableName
argument_list|,
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
argument_list|>
name|groupClusterLoad
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Address
name|sName
range|:
name|info
operator|.
name|getServers
argument_list|()
control|)
block|{
for|for
control|(
name|ServerName
name|curr
range|:
name|clusterState
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|curr
operator|.
name|getAddress
argument_list|()
operator|.
name|equals
argument_list|(
name|sName
argument_list|)
condition|)
block|{
name|groupClusterState
operator|.
name|put
argument_list|(
name|curr
argument_list|,
name|correctedState
operator|.
name|get
argument_list|(
name|curr
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|groupClusterLoad
operator|.
name|put
argument_list|(
name|HConstants
operator|.
name|ENSEMBLE_TABLE_NAME
argument_list|,
name|groupClusterState
argument_list|)
expr_stmt|;
name|this
operator|.
name|internalBalancer
operator|.
name|setClusterLoad
argument_list|(
name|groupClusterLoad
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|RegionPlan
argument_list|>
name|groupPlans
init|=
name|this
operator|.
name|internalBalancer
operator|.
name|balanceCluster
argument_list|(
name|groupClusterState
argument_list|)
decl_stmt|;
if|if
condition|(
name|groupPlans
operator|!=
literal|null
condition|)
block|{
name|regionPlans
operator|.
name|addAll
argument_list|(
name|groupPlans
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|exp
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception while balancing cluster."
argument_list|,
name|exp
argument_list|)
expr_stmt|;
name|regionPlans
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
return|return
name|regionPlans
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
throws|throws
name|HBaseIOException
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
name|assignments
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|HRegionInfo
argument_list|>
name|regionMap
init|=
name|ArrayListMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|ServerName
argument_list|>
name|serverMap
init|=
name|ArrayListMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
name|generateGroupMaps
argument_list|(
name|regions
argument_list|,
name|servers
argument_list|,
name|regionMap
argument_list|,
name|serverMap
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|groupKey
range|:
name|regionMap
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|regionMap
operator|.
name|get
argument_list|(
name|groupKey
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
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
name|result
init|=
name|this
operator|.
name|internalBalancer
operator|.
name|roundRobinAssignment
argument_list|(
name|regionMap
operator|.
name|get
argument_list|(
name|groupKey
argument_list|)
argument_list|,
name|serverMap
operator|.
name|get
argument_list|(
name|groupKey
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
name|assignments
operator|.
name|putAll
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|assignments
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
name|retainAssignment
parameter_list|(
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|regions
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
parameter_list|)
throws|throws
name|HBaseIOException
block|{
try|try
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
name|assignments
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|HRegionInfo
argument_list|>
name|groupToRegion
init|=
name|ArrayListMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|HRegionInfo
argument_list|>
name|misplacedRegions
init|=
name|getMisplacedRegions
argument_list|(
name|regions
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|region
range|:
name|regions
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|misplacedRegions
operator|.
name|contains
argument_list|(
name|region
argument_list|)
condition|)
block|{
name|String
name|groupName
init|=
name|rsGroupInfoManager
operator|.
name|getRSGroupOfTable
argument_list|(
name|region
operator|.
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
name|groupToRegion
operator|.
name|put
argument_list|(
name|groupName
argument_list|,
name|region
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Now the "groupToRegion" map has only the regions which have correct
comment|// assignments.
for|for
control|(
name|String
name|key
range|:
name|groupToRegion
operator|.
name|keySet
argument_list|()
control|)
block|{
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|currentAssignmentMap
init|=
operator|new
name|TreeMap
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionList
init|=
name|groupToRegion
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|RSGroupInfo
name|info
init|=
name|rsGroupInfoManager
operator|.
name|getRSGroup
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|candidateList
init|=
name|filterOfflineServers
argument_list|(
name|info
argument_list|,
name|servers
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|region
range|:
name|regionList
control|)
block|{
name|currentAssignmentMap
operator|.
name|put
argument_list|(
name|region
argument_list|,
name|regions
operator|.
name|get
argument_list|(
name|region
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|candidateList
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|assignments
operator|.
name|putAll
argument_list|(
name|this
operator|.
name|internalBalancer
operator|.
name|retainAssignment
argument_list|(
name|currentAssignmentMap
argument_list|,
name|candidateList
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|HRegionInfo
name|region
range|:
name|misplacedRegions
control|)
block|{
name|String
name|groupName
init|=
name|rsGroupInfoManager
operator|.
name|getRSGroupOfTable
argument_list|(
name|region
operator|.
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
empty_stmt|;
name|RSGroupInfo
name|info
init|=
name|rsGroupInfoManager
operator|.
name|getRSGroup
argument_list|(
name|groupName
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|candidateList
init|=
name|filterOfflineServers
argument_list|(
name|info
argument_list|,
name|servers
argument_list|)
decl_stmt|;
name|ServerName
name|server
init|=
name|this
operator|.
name|internalBalancer
operator|.
name|randomAssignment
argument_list|(
name|region
argument_list|,
name|candidateList
argument_list|)
decl_stmt|;
if|if
condition|(
name|server
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|assignments
operator|.
name|containsKey
argument_list|(
name|server
argument_list|)
condition|)
block|{
name|assignments
operator|.
name|put
argument_list|(
name|server
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assignments
operator|.
name|get
argument_list|(
name|server
argument_list|)
operator|.
name|add
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|//if not server is available assign to bogus so it ends up in RIT
if|if
condition|(
operator|!
name|assignments
operator|.
name|containsKey
argument_list|(
name|LoadBalancer
operator|.
name|BOGUS_SERVER_NAME
argument_list|)
condition|)
block|{
name|assignments
operator|.
name|put
argument_list|(
name|LoadBalancer
operator|.
name|BOGUS_SERVER_NAME
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assignments
operator|.
name|get
argument_list|(
name|LoadBalancer
operator|.
name|BOGUS_SERVER_NAME
argument_list|)
operator|.
name|add
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|assignments
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HBaseIOException
argument_list|(
literal|"Failed to do online retain assignment"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|randomAssignment
parameter_list|(
name|HRegionInfo
name|region
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
parameter_list|)
throws|throws
name|HBaseIOException
block|{
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|HRegionInfo
argument_list|>
name|regionMap
init|=
name|LinkedListMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|ServerName
argument_list|>
name|serverMap
init|=
name|LinkedListMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
name|generateGroupMaps
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|region
argument_list|)
argument_list|,
name|servers
argument_list|,
name|regionMap
argument_list|,
name|serverMap
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|filteredServers
init|=
name|serverMap
operator|.
name|get
argument_list|(
name|regionMap
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|this
operator|.
name|internalBalancer
operator|.
name|randomAssignment
argument_list|(
name|region
argument_list|,
name|filteredServers
argument_list|)
return|;
block|}
specifier|private
name|void
name|generateGroupMaps
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
parameter_list|,
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|HRegionInfo
argument_list|>
name|regionMap
parameter_list|,
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|ServerName
argument_list|>
name|serverMap
parameter_list|)
throws|throws
name|HBaseIOException
block|{
try|try
block|{
for|for
control|(
name|HRegionInfo
name|region
range|:
name|regions
control|)
block|{
name|String
name|groupName
init|=
name|rsGroupInfoManager
operator|.
name|getRSGroupOfTable
argument_list|(
name|region
operator|.
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|groupName
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Group for table "
operator|+
name|region
operator|.
name|getTable
argument_list|()
operator|+
literal|" is null"
argument_list|)
expr_stmt|;
block|}
name|regionMap
operator|.
name|put
argument_list|(
name|groupName
argument_list|,
name|region
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|groupKey
range|:
name|regionMap
operator|.
name|keySet
argument_list|()
control|)
block|{
name|RSGroupInfo
name|info
init|=
name|rsGroupInfoManager
operator|.
name|getRSGroup
argument_list|(
name|groupKey
argument_list|)
decl_stmt|;
name|serverMap
operator|.
name|putAll
argument_list|(
name|groupKey
argument_list|,
name|filterOfflineServers
argument_list|(
name|info
argument_list|,
name|servers
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|serverMap
operator|.
name|get
argument_list|(
name|groupKey
argument_list|)
operator|.
name|size
argument_list|()
operator|<
literal|1
condition|)
block|{
name|serverMap
operator|.
name|put
argument_list|(
name|groupKey
argument_list|,
name|LoadBalancer
operator|.
name|BOGUS_SERVER_NAME
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HBaseIOException
argument_list|(
literal|"Failed to generate group maps"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|List
argument_list|<
name|ServerName
argument_list|>
name|filterOfflineServers
parameter_list|(
name|RSGroupInfo
name|RSGroupInfo
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|onlineServers
parameter_list|)
block|{
if|if
condition|(
name|RSGroupInfo
operator|!=
literal|null
condition|)
block|{
return|return
name|filterServers
argument_list|(
name|RSGroupInfo
operator|.
name|getServers
argument_list|()
argument_list|,
name|onlineServers
argument_list|)
return|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"RSGroup Information found to be null. Some regions might be unassigned."
argument_list|)
expr_stmt|;
return|return
name|Collections
operator|.
name|EMPTY_LIST
return|;
block|}
block|}
comment|/**    * Filter servers based on the online servers.    *    * @param servers    *          the servers    * @param onlineServers    *          List of servers which are online.    * @return the list    */
specifier|private
name|List
argument_list|<
name|ServerName
argument_list|>
name|filterServers
parameter_list|(
name|Collection
argument_list|<
name|Address
argument_list|>
name|servers
parameter_list|,
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|onlineServers
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
name|finalList
init|=
operator|new
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Address
name|server
range|:
name|servers
control|)
block|{
for|for
control|(
name|ServerName
name|curr
range|:
name|onlineServers
control|)
block|{
if|if
condition|(
name|curr
operator|.
name|getAddress
argument_list|()
operator|.
name|equals
argument_list|(
name|server
argument_list|)
condition|)
block|{
name|finalList
operator|.
name|add
argument_list|(
name|curr
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|finalList
return|;
block|}
specifier|private
name|Set
argument_list|<
name|HRegionInfo
argument_list|>
name|getMisplacedRegions
parameter_list|(
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|regions
parameter_list|)
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|HRegionInfo
argument_list|>
name|misplacedRegions
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|region
range|:
name|regions
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|HRegionInfo
name|regionInfo
init|=
name|region
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|ServerName
name|assignedServer
init|=
name|region
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|RSGroupInfo
name|info
init|=
name|rsGroupInfoManager
operator|.
name|getRSGroup
argument_list|(
name|rsGroupInfoManager
operator|.
name|getRSGroupOfTable
argument_list|(
name|regionInfo
operator|.
name|getTable
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|assignedServer
operator|!=
literal|null
operator|&&
operator|(
name|info
operator|==
literal|null
operator|||
operator|!
name|info
operator|.
name|containsServer
argument_list|(
name|assignedServer
operator|.
name|getAddress
argument_list|()
argument_list|)
operator|)
condition|)
block|{
name|RSGroupInfo
name|otherInfo
init|=
literal|null
decl_stmt|;
name|otherInfo
operator|=
name|rsGroupInfoManager
operator|.
name|getRSGroupOfServer
argument_list|(
name|assignedServer
operator|.
name|getAddress
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found misplaced region: "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" on server: "
operator|+
name|assignedServer
operator|+
literal|" found in group: "
operator|+
name|otherInfo
operator|+
literal|" outside of group: "
operator|+
operator|(
name|info
operator|==
literal|null
condition|?
literal|"UNKNOWN"
else|:
name|info
operator|.
name|getName
argument_list|()
operator|)
argument_list|)
expr_stmt|;
name|misplacedRegions
operator|.
name|add
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|misplacedRegions
return|;
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
name|correctAssignments
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
name|existingAssignments
parameter_list|)
throws|throws
name|HBaseIOException
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
name|correctAssignments
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|misplacedRegions
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
name|correctAssignments
operator|.
name|put
argument_list|(
name|LoadBalancer
operator|.
name|BOGUS_SERVER_NAME
argument_list|,
operator|new
name|LinkedList
argument_list|<>
argument_list|()
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
name|assignments
range|:
name|existingAssignments
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|ServerName
name|sName
init|=
name|assignments
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|correctAssignments
operator|.
name|put
argument_list|(
name|sName
argument_list|,
operator|new
name|LinkedList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|assignments
operator|.
name|getValue
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|region
range|:
name|regions
control|)
block|{
name|RSGroupInfo
name|info
init|=
literal|null
decl_stmt|;
try|try
block|{
name|info
operator|=
name|rsGroupInfoManager
operator|.
name|getRSGroup
argument_list|(
name|rsGroupInfoManager
operator|.
name|getRSGroupOfTable
argument_list|(
name|region
operator|.
name|getTable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|exp
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"RSGroup information null for region of table "
operator|+
name|region
operator|.
name|getTable
argument_list|()
argument_list|,
name|exp
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|info
operator|==
literal|null
operator|)
operator|||
operator|(
operator|!
name|info
operator|.
name|containsServer
argument_list|(
name|sName
operator|.
name|getAddress
argument_list|()
argument_list|)
operator|)
condition|)
block|{
name|correctAssignments
operator|.
name|get
argument_list|(
name|LoadBalancer
operator|.
name|BOGUS_SERVER_NAME
argument_list|)
operator|.
name|add
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|correctAssignments
operator|.
name|get
argument_list|(
name|sName
argument_list|)
operator|.
name|add
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|//TODO bulk unassign?
comment|//unassign misplaced regions, so that they are assigned to correct groups.
for|for
control|(
name|HRegionInfo
name|info
range|:
name|misplacedRegions
control|)
block|{
try|try
block|{
name|this
operator|.
name|masterServices
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|unassign
argument_list|(
name|info
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HBaseIOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|correctAssignments
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|()
throws|throws
name|HBaseIOException
block|{
try|try
block|{
if|if
condition|(
name|rsGroupInfoManager
operator|==
literal|null
condition|)
block|{
name|List
argument_list|<
name|RSGroupAdminEndpoint
argument_list|>
name|cps
init|=
name|masterServices
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|findCoprocessors
argument_list|(
name|RSGroupAdminEndpoint
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|cps
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
name|String
name|msg
init|=
literal|"Expected one implementation of GroupAdminEndpoint but found "
operator|+
name|cps
operator|.
name|size
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HBaseIOException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
name|rsGroupInfoManager
operator|=
name|cps
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getGroupInfoManager
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HBaseIOException
argument_list|(
literal|"Failed to initialize GroupInfoManagerImpl"
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// Create the balancer
name|Class
argument_list|<
name|?
extends|extends
name|LoadBalancer
argument_list|>
name|balancerKlass
init|=
name|config
operator|.
name|getClass
argument_list|(
name|HBASE_RSGROUP_LOADBALANCER_CLASS
argument_list|,
name|StochasticLoadBalancer
operator|.
name|class
argument_list|,
name|LoadBalancer
operator|.
name|class
argument_list|)
decl_stmt|;
name|internalBalancer
operator|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|balancerKlass
argument_list|,
name|config
argument_list|)
expr_stmt|;
name|internalBalancer
operator|.
name|setMasterServices
argument_list|(
name|masterServices
argument_list|)
expr_stmt|;
name|internalBalancer
operator|.
name|setClusterStatus
argument_list|(
name|clusterStatus
argument_list|)
expr_stmt|;
name|internalBalancer
operator|.
name|setConf
argument_list|(
name|config
argument_list|)
expr_stmt|;
name|internalBalancer
operator|.
name|initialize
argument_list|()
expr_stmt|;
block|}
specifier|public
name|boolean
name|isOnline
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|rsGroupInfoManager
operator|==
literal|null
condition|)
return|return
literal|false
return|;
return|return
name|this
operator|.
name|rsGroupInfoManager
operator|.
name|isOnline
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setClusterLoad
parameter_list|(
name|Map
argument_list|<
name|TableName
argument_list|,
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
argument_list|>
name|clusterLoad
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|regionOnline
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
name|ServerName
name|sn
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|regionOffline
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|onConfigurationChange
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|//DO nothing for now
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|void
name|setRsGroupInfoManager
parameter_list|(
name|RSGroupInfoManager
name|rsGroupInfoManager
parameter_list|)
block|{
name|this
operator|.
name|rsGroupInfoManager
operator|=
name|rsGroupInfoManager
expr_stmt|;
block|}
block|}
end_class

end_unit

