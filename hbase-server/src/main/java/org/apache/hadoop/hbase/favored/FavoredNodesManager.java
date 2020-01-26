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
name|favored
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ServerName
operator|.
name|NON_STARTCODE
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|favored
operator|.
name|FavoredNodeAssignmentHelper
operator|.
name|FAVORED_NODES_NUM
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|favored
operator|.
name|FavoredNodesPlan
operator|.
name|Position
operator|.
name|PRIMARY
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|favored
operator|.
name|FavoredNodesPlan
operator|.
name|Position
operator|.
name|SECONDARY
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|favored
operator|.
name|FavoredNodesPlan
operator|.
name|Position
operator|.
name|TERTIARY
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|hdfs
operator|.
name|DFSConfigKeys
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
name|hdfs
operator|.
name|HdfsConfiguration
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
name|NetUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
import|;
end_import

begin_comment
comment|/**  * FavoredNodesManager is responsible for maintaining favored nodes info in internal cache and  * META table. Its the centralized store for all favored nodes information. All reads and updates  * should be done through this class. There should only be one instance of  * {@link FavoredNodesManager} in Master. {@link FavoredNodesPlan} and favored node information  * from {@link SnapshotOfRegionAssignmentFromMeta} should not be used outside this class (except  * for tools that only read or fortest cases). All other classes including Favored balancers  * and {@link FavoredNodeAssignmentHelper} should use {@link FavoredNodesManager} for any  * read/write/deletes to favored nodes.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|FavoredNodesManager
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|FavoredNodesManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|FavoredNodesPlan
name|globalFavoredNodesAssignmentPlan
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
name|primaryRSToRegionMap
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
name|secondaryRSToRegionMap
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
name|teritiaryRSToRegionMap
decl_stmt|;
specifier|private
specifier|final
name|MasterServices
name|masterServices
decl_stmt|;
specifier|private
specifier|final
name|RackManager
name|rackManager
decl_stmt|;
comment|/**    * Datanode port to be used for Favored Nodes.    */
specifier|private
name|int
name|datanodeDataTransferPort
decl_stmt|;
specifier|public
name|FavoredNodesManager
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
name|this
operator|.
name|globalFavoredNodesAssignmentPlan
operator|=
operator|new
name|FavoredNodesPlan
argument_list|()
expr_stmt|;
name|this
operator|.
name|primaryRSToRegionMap
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|secondaryRSToRegionMap
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|teritiaryRSToRegionMap
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|rackManager
operator|=
operator|new
name|RackManager
argument_list|(
name|masterServices
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|initialize
parameter_list|(
name|SnapshotOfRegionAssignmentFromMeta
name|snapshot
parameter_list|)
block|{
comment|// Add snapshot to structures made on creation. Current structures may have picked
comment|// up data between construction and the scan of meta needed before this method
comment|// is called.  See HBASE-23737 "[Flakey Tests] TestFavoredNodeTableImport fails 30% of the time"
name|this
operator|.
name|globalFavoredNodesAssignmentPlan
operator|.
name|updateFavoredNodesMap
argument_list|(
name|snapshot
operator|.
name|getExistingAssignmentPlan
argument_list|()
argument_list|)
expr_stmt|;
name|primaryRSToRegionMap
operator|.
name|putAll
argument_list|(
name|snapshot
operator|.
name|getPrimaryToRegionInfoMap
argument_list|()
argument_list|)
expr_stmt|;
name|secondaryRSToRegionMap
operator|.
name|putAll
argument_list|(
name|snapshot
operator|.
name|getSecondaryToRegionInfoMap
argument_list|()
argument_list|)
expr_stmt|;
name|teritiaryRSToRegionMap
operator|.
name|putAll
argument_list|(
name|snapshot
operator|.
name|getTertiaryToRegionInfoMap
argument_list|()
argument_list|)
expr_stmt|;
name|datanodeDataTransferPort
operator|=
name|getDataNodePort
argument_list|()
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|int
name|getDataNodePort
parameter_list|()
block|{
name|HdfsConfiguration
operator|.
name|init
argument_list|()
expr_stmt|;
name|Configuration
name|dnConf
init|=
operator|new
name|HdfsConfiguration
argument_list|(
name|masterServices
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|dnPort
init|=
name|NetUtils
operator|.
name|createSocketAddr
argument_list|(
name|dnConf
operator|.
name|get
argument_list|(
name|DFSConfigKeys
operator|.
name|DFS_DATANODE_ADDRESS_KEY
argument_list|,
name|DFSConfigKeys
operator|.
name|DFS_DATANODE_ADDRESS_DEFAULT
argument_list|)
argument_list|)
operator|.
name|getPort
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Loaded default datanode port for FN: "
operator|+
name|datanodeDataTransferPort
argument_list|)
expr_stmt|;
return|return
name|dnPort
return|;
block|}
specifier|public
specifier|synchronized
name|List
argument_list|<
name|ServerName
argument_list|>
name|getFavoredNodes
parameter_list|(
name|RegionInfo
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
comment|/*    * Favored nodes are not applicable for system tables. We will use this to check before    * we apply any favored nodes logic on a region.    */
specifier|public
specifier|static
name|boolean
name|isFavoredNodeApplicable
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|)
block|{
return|return
operator|!
name|regionInfo
operator|.
name|getTable
argument_list|()
operator|.
name|isSystemTable
argument_list|()
return|;
block|}
comment|/**    * Filter and return regions for which favored nodes is not applicable.    * @return set of regions for which favored nodes is not applicable    */
specifier|public
specifier|static
name|Set
argument_list|<
name|RegionInfo
argument_list|>
name|filterNonFNApplicableRegions
parameter_list|(
name|Collection
argument_list|<
name|RegionInfo
argument_list|>
name|regions
parameter_list|)
block|{
return|return
name|regions
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|r
lambda|->
operator|!
name|isFavoredNodeApplicable
argument_list|(
name|r
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
return|;
block|}
comment|/*    * This should only be used when sending FN information to the region servers. Instead of    * sending the region server port, we use the datanode port. This helps in centralizing the DN    * port logic in Master. The RS uses the port from the favored node list as hints.    */
specifier|public
specifier|synchronized
name|List
argument_list|<
name|ServerName
argument_list|>
name|getFavoredNodesWithDNPort
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|)
block|{
if|if
condition|(
name|getFavoredNodes
argument_list|(
name|regionInfo
argument_list|)
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|ServerName
argument_list|>
name|fnWithDNPort
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|ServerName
name|sn
range|:
name|getFavoredNodes
argument_list|(
name|regionInfo
argument_list|)
control|)
block|{
name|fnWithDNPort
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
name|datanodeDataTransferPort
argument_list|,
name|NON_STARTCODE
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|fnWithDNPort
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|updateFavoredNodes
parameter_list|(
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
argument_list|>
name|regionFNMap
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
argument_list|>
name|regionToFavoredNodes
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|RegionInfo
argument_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
argument_list|>
name|entry
range|:
name|regionFNMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|RegionInfo
name|regionInfo
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
comment|/*        * None of the following error conditions should happen. If it does, there is an issue with        * favored nodes generation or the regions its called on.        */
if|if
condition|(
name|servers
operator|.
name|size
argument_list|()
operator|!=
name|Sets
operator|.
name|newHashSet
argument_list|(
name|servers
argument_list|)
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Duplicates found: "
operator|+
name|servers
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|isFavoredNodeApplicable
argument_list|(
name|regionInfo
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Can't update FN for a un-applicable region: "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" with "
operator|+
name|servers
argument_list|)
throw|;
block|}
if|if
condition|(
name|servers
operator|.
name|size
argument_list|()
operator|!=
name|FAVORED_NODES_NUM
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"At least "
operator|+
name|FAVORED_NODES_NUM
operator|+
literal|" favored nodes should be present for region : "
operator|+
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|" current FN servers:"
operator|+
name|servers
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|ServerName
argument_list|>
name|serversWithNoStartCodes
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|ServerName
name|sn
range|:
name|servers
control|)
block|{
if|if
condition|(
name|sn
operator|.
name|getStartcode
argument_list|()
operator|==
name|NON_STARTCODE
condition|)
block|{
name|serversWithNoStartCodes
operator|.
name|add
argument_list|(
name|sn
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|serversWithNoStartCodes
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
name|NON_STARTCODE
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|regionToFavoredNodes
operator|.
name|put
argument_list|(
name|regionInfo
argument_list|,
name|serversWithNoStartCodes
argument_list|)
expr_stmt|;
block|}
comment|// Lets do a bulk update to meta since that reduces the RPC's
name|FavoredNodeAssignmentHelper
operator|.
name|updateMetaWithFavoredNodesInfo
argument_list|(
name|regionToFavoredNodes
argument_list|,
name|masterServices
operator|.
name|getConnection
argument_list|()
argument_list|)
expr_stmt|;
name|deleteFavoredNodesForRegions
argument_list|(
name|regionToFavoredNodes
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|RegionInfo
argument_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
argument_list|>
name|entry
range|:
name|regionToFavoredNodes
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|RegionInfo
name|regionInfo
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|serversWithNoStartCodes
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|globalFavoredNodesAssignmentPlan
operator|.
name|updateFavoredNodesMap
argument_list|(
name|regionInfo
argument_list|,
name|serversWithNoStartCodes
argument_list|)
expr_stmt|;
name|addToReplicaLoad
argument_list|(
name|regionInfo
argument_list|,
name|serversWithNoStartCodes
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|synchronized
name|void
name|addToReplicaLoad
parameter_list|(
name|RegionInfo
name|hri
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
parameter_list|)
block|{
name|ServerName
name|serverToUse
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
name|servers
operator|.
name|get
argument_list|(
name|PRIMARY
operator|.
name|ordinal
argument_list|()
argument_list|)
operator|.
name|getAddress
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|NON_STARTCODE
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regionList
init|=
name|primaryRSToRegionMap
operator|.
name|get
argument_list|(
name|serverToUse
argument_list|)
decl_stmt|;
if|if
condition|(
name|regionList
operator|==
literal|null
condition|)
block|{
name|regionList
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|regionList
operator|.
name|add
argument_list|(
name|hri
argument_list|)
expr_stmt|;
name|primaryRSToRegionMap
operator|.
name|put
argument_list|(
name|serverToUse
argument_list|,
name|regionList
argument_list|)
expr_stmt|;
name|serverToUse
operator|=
name|ServerName
operator|.
name|valueOf
argument_list|(
name|servers
operator|.
name|get
argument_list|(
name|SECONDARY
operator|.
name|ordinal
argument_list|()
argument_list|)
operator|.
name|getHostAndPort
argument_list|()
argument_list|,
name|NON_STARTCODE
argument_list|)
expr_stmt|;
name|regionList
operator|=
name|secondaryRSToRegionMap
operator|.
name|get
argument_list|(
name|serverToUse
argument_list|)
expr_stmt|;
if|if
condition|(
name|regionList
operator|==
literal|null
condition|)
block|{
name|regionList
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|regionList
operator|.
name|add
argument_list|(
name|hri
argument_list|)
expr_stmt|;
name|secondaryRSToRegionMap
operator|.
name|put
argument_list|(
name|serverToUse
argument_list|,
name|regionList
argument_list|)
expr_stmt|;
name|serverToUse
operator|=
name|ServerName
operator|.
name|valueOf
argument_list|(
name|servers
operator|.
name|get
argument_list|(
name|TERTIARY
operator|.
name|ordinal
argument_list|()
argument_list|)
operator|.
name|getHostAndPort
argument_list|()
argument_list|,
name|NON_STARTCODE
argument_list|)
expr_stmt|;
name|regionList
operator|=
name|teritiaryRSToRegionMap
operator|.
name|get
argument_list|(
name|serverToUse
argument_list|)
expr_stmt|;
if|if
condition|(
name|regionList
operator|==
literal|null
condition|)
block|{
name|regionList
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|regionList
operator|.
name|add
argument_list|(
name|hri
argument_list|)
expr_stmt|;
name|teritiaryRSToRegionMap
operator|.
name|put
argument_list|(
name|serverToUse
argument_list|,
name|regionList
argument_list|)
expr_stmt|;
block|}
comment|/*    * Get the replica count for the servers provided.    *    * For each server, replica count includes three counts for primary, secondary and tertiary.    * If a server is the primary favored node for 10 regions, secondary for 5 and tertiary    * for 1, then the list would be [10, 5, 1]. If the server is newly added to the cluster is    * not a favored node for any region, the replica count would be [0, 0, 0].    */
specifier|public
specifier|synchronized
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|getReplicaLoad
parameter_list|(
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
name|Integer
argument_list|>
argument_list|>
name|result
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
for|for
control|(
name|ServerName
name|sn
range|:
name|servers
control|)
block|{
name|ServerName
name|serverWithNoStartCode
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
name|sn
operator|.
name|getHostAndPort
argument_list|()
argument_list|,
name|NON_STARTCODE
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|countList
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
if|if
condition|(
name|primaryRSToRegionMap
operator|.
name|containsKey
argument_list|(
name|serverWithNoStartCode
argument_list|)
condition|)
block|{
name|countList
operator|.
name|add
argument_list|(
name|primaryRSToRegionMap
operator|.
name|get
argument_list|(
name|serverWithNoStartCode
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|countList
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|secondaryRSToRegionMap
operator|.
name|containsKey
argument_list|(
name|serverWithNoStartCode
argument_list|)
condition|)
block|{
name|countList
operator|.
name|add
argument_list|(
name|secondaryRSToRegionMap
operator|.
name|get
argument_list|(
name|serverWithNoStartCode
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|countList
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|teritiaryRSToRegionMap
operator|.
name|containsKey
argument_list|(
name|serverWithNoStartCode
argument_list|)
condition|)
block|{
name|countList
operator|.
name|add
argument_list|(
name|teritiaryRSToRegionMap
operator|.
name|get
argument_list|(
name|serverWithNoStartCode
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|countList
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|put
argument_list|(
name|sn
argument_list|,
name|countList
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|deleteFavoredNodesForRegion
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|)
block|{
name|List
argument_list|<
name|ServerName
argument_list|>
name|favNodes
init|=
name|getFavoredNodes
argument_list|(
name|regionInfo
argument_list|)
decl_stmt|;
if|if
condition|(
name|favNodes
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|primaryRSToRegionMap
operator|.
name|containsKey
argument_list|(
name|favNodes
operator|.
name|get
argument_list|(
name|PRIMARY
operator|.
name|ordinal
argument_list|()
argument_list|)
argument_list|)
condition|)
block|{
name|primaryRSToRegionMap
operator|.
name|get
argument_list|(
name|favNodes
operator|.
name|get
argument_list|(
name|PRIMARY
operator|.
name|ordinal
argument_list|()
argument_list|)
argument_list|)
operator|.
name|remove
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|secondaryRSToRegionMap
operator|.
name|containsKey
argument_list|(
name|favNodes
operator|.
name|get
argument_list|(
name|SECONDARY
operator|.
name|ordinal
argument_list|()
argument_list|)
argument_list|)
condition|)
block|{
name|secondaryRSToRegionMap
operator|.
name|get
argument_list|(
name|favNodes
operator|.
name|get
argument_list|(
name|SECONDARY
operator|.
name|ordinal
argument_list|()
argument_list|)
argument_list|)
operator|.
name|remove
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|teritiaryRSToRegionMap
operator|.
name|containsKey
argument_list|(
name|favNodes
operator|.
name|get
argument_list|(
name|TERTIARY
operator|.
name|ordinal
argument_list|()
argument_list|)
argument_list|)
condition|)
block|{
name|teritiaryRSToRegionMap
operator|.
name|get
argument_list|(
name|favNodes
operator|.
name|get
argument_list|(
name|TERTIARY
operator|.
name|ordinal
argument_list|()
argument_list|)
argument_list|)
operator|.
name|remove
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
block|}
name|globalFavoredNodesAssignmentPlan
operator|.
name|removeFavoredNodes
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|synchronized
name|void
name|deleteFavoredNodesForRegions
parameter_list|(
name|Collection
argument_list|<
name|RegionInfo
argument_list|>
name|regionInfoList
parameter_list|)
block|{
for|for
control|(
name|RegionInfo
name|regionInfo
range|:
name|regionInfoList
control|)
block|{
name|deleteFavoredNodesForRegion
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|VisibleForTesting
specifier|public
specifier|synchronized
name|Set
argument_list|<
name|RegionInfo
argument_list|>
name|getRegionsOfFavoredNode
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
block|{
name|Set
argument_list|<
name|RegionInfo
argument_list|>
name|regionInfos
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
name|ServerName
name|serverToUse
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
name|serverName
operator|.
name|getHostAndPort
argument_list|()
argument_list|,
name|NON_STARTCODE
argument_list|)
decl_stmt|;
if|if
condition|(
name|primaryRSToRegionMap
operator|.
name|containsKey
argument_list|(
name|serverToUse
argument_list|)
condition|)
block|{
name|regionInfos
operator|.
name|addAll
argument_list|(
name|primaryRSToRegionMap
operator|.
name|get
argument_list|(
name|serverToUse
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|secondaryRSToRegionMap
operator|.
name|containsKey
argument_list|(
name|serverToUse
argument_list|)
condition|)
block|{
name|regionInfos
operator|.
name|addAll
argument_list|(
name|secondaryRSToRegionMap
operator|.
name|get
argument_list|(
name|serverToUse
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|teritiaryRSToRegionMap
operator|.
name|containsKey
argument_list|(
name|serverToUse
argument_list|)
condition|)
block|{
name|regionInfos
operator|.
name|addAll
argument_list|(
name|teritiaryRSToRegionMap
operator|.
name|get
argument_list|(
name|serverToUse
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|regionInfos
return|;
block|}
specifier|public
name|RackManager
name|getRackManager
parameter_list|()
block|{
return|return
name|rackManager
return|;
block|}
block|}
end_class

end_unit

