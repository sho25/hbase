begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Arrays
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
name|HRegionLocation
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
name|MetaTableAccessor
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
name|MetaTableAccessor
operator|.
name|Visitor
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
name|RegionLocations
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
name|client
operator|.
name|Connection
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
name|Result
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
name|favored
operator|.
name|FavoredNodeAssignmentHelper
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
name|favored
operator|.
name|FavoredNodesPlan
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

begin_comment
comment|/**  * Used internally for reading meta and constructing datastructures that are  * then queried, for things like regions to regionservers, table to regions, etc.  * It also records the favored nodes mapping for regions.  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SnapshotOfRegionAssignmentFromMeta
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
name|SnapshotOfRegionAssignmentFromMeta
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Connection
name|connection
decl_stmt|;
comment|/** the table name to region map */
specifier|private
specifier|final
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
name|tableToRegionMap
decl_stmt|;
comment|/** the region to region server map */
comment|//private final Map<RegionInfo, ServerName> regionToRegionServerMap;
specifier|private
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|ServerName
argument_list|>
name|regionToRegionServerMap
decl_stmt|;
comment|/** the region name to region info map */
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|RegionInfo
argument_list|>
name|regionNameToRegionInfoMap
decl_stmt|;
comment|/** the regionServer to region map */
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
name|currentRSToRegionMap
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
comment|/** the existing assignment plan in the hbase:meta region */
specifier|private
specifier|final
name|FavoredNodesPlan
name|existingAssignmentPlan
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|TableName
argument_list|>
name|disabledTables
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|excludeOfflinedSplitParents
decl_stmt|;
specifier|public
name|SnapshotOfRegionAssignmentFromMeta
parameter_list|(
name|Connection
name|connection
parameter_list|)
block|{
name|this
argument_list|(
name|connection
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SnapshotOfRegionAssignmentFromMeta
parameter_list|(
name|Connection
name|connection
parameter_list|,
name|Set
argument_list|<
name|TableName
argument_list|>
name|disabledTables
parameter_list|,
name|boolean
name|excludeOfflinedSplitParents
parameter_list|)
block|{
name|this
operator|.
name|connection
operator|=
name|connection
expr_stmt|;
name|tableToRegionMap
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|regionToRegionServerMap
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|currentRSToRegionMap
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|primaryRSToRegionMap
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|secondaryRSToRegionMap
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|teritiaryRSToRegionMap
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|regionNameToRegionInfoMap
operator|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
expr_stmt|;
name|existingAssignmentPlan
operator|=
operator|new
name|FavoredNodesPlan
argument_list|()
expr_stmt|;
name|this
operator|.
name|disabledTables
operator|=
name|disabledTables
expr_stmt|;
name|this
operator|.
name|excludeOfflinedSplitParents
operator|=
name|excludeOfflinedSplitParents
expr_stmt|;
block|}
comment|/**    * Initialize the region assignment snapshot by scanning the hbase:meta table    * @throws IOException    */
specifier|public
name|void
name|initialize
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Start to scan the hbase:meta for the current region assignment "
operator|+
literal|"snappshot"
argument_list|)
expr_stmt|;
comment|// TODO: at some point this code could live in the MetaTableAccessor
name|Visitor
name|v
init|=
operator|new
name|Visitor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|visit
parameter_list|(
name|Result
name|result
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
if|if
condition|(
name|result
operator|==
literal|null
operator|||
name|result
operator|.
name|isEmpty
argument_list|()
condition|)
return|return
literal|true
return|;
name|RegionLocations
name|rl
init|=
name|MetaTableAccessor
operator|.
name|getRegionLocations
argument_list|(
name|result
argument_list|)
decl_stmt|;
if|if
condition|(
name|rl
operator|==
literal|null
condition|)
return|return
literal|true
return|;
name|RegionInfo
name|hri
init|=
name|rl
operator|.
name|getRegionLocation
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
if|if
condition|(
name|hri
operator|==
literal|null
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|hri
operator|.
name|getTable
argument_list|()
operator|==
literal|null
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|disabledTables
operator|.
name|contains
argument_list|(
name|hri
operator|.
name|getTable
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
comment|// Are we to include split parents in the list?
if|if
condition|(
name|excludeOfflinedSplitParents
operator|&&
name|hri
operator|.
name|isSplit
argument_list|()
condition|)
return|return
literal|true
return|;
name|HRegionLocation
index|[]
name|hrls
init|=
name|rl
operator|.
name|getRegionLocations
argument_list|()
decl_stmt|;
comment|// Add the current assignment to the snapshot for all replicas
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|hrls
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|hrls
index|[
name|i
index|]
operator|==
literal|null
condition|)
continue|continue;
name|hri
operator|=
name|hrls
index|[
name|i
index|]
operator|.
name|getRegionInfo
argument_list|()
expr_stmt|;
if|if
condition|(
name|hri
operator|==
literal|null
condition|)
continue|continue;
name|addAssignment
argument_list|(
name|hri
argument_list|,
name|hrls
index|[
name|i
index|]
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|addRegion
argument_list|(
name|hri
argument_list|)
expr_stmt|;
block|}
name|hri
operator|=
name|rl
operator|.
name|getRegionLocation
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
expr_stmt|;
comment|// the code below is to handle favored nodes
name|byte
index|[]
name|favoredNodes
init|=
name|result
operator|.
name|getValue
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|FavoredNodeAssignmentHelper
operator|.
name|FAVOREDNODES_QUALIFIER
argument_list|)
decl_stmt|;
if|if
condition|(
name|favoredNodes
operator|==
literal|null
condition|)
return|return
literal|true
return|;
comment|// Add the favored nodes into assignment plan
name|ServerName
index|[]
name|favoredServerList
init|=
name|FavoredNodeAssignmentHelper
operator|.
name|getFavoredNodesList
argument_list|(
name|favoredNodes
argument_list|)
decl_stmt|;
comment|// Add the favored nodes into assignment plan
name|existingAssignmentPlan
operator|.
name|updateFavoredNodesMap
argument_list|(
name|hri
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|favoredServerList
argument_list|)
argument_list|)
expr_stmt|;
comment|/*            * Typically there should be FAVORED_NODES_NUM favored nodes for a region in meta. If            * there is less than FAVORED_NODES_NUM, lets use as much as we can but log a warning.            */
if|if
condition|(
name|favoredServerList
operator|.
name|length
operator|!=
name|FavoredNodeAssignmentHelper
operator|.
name|FAVORED_NODES_NUM
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Insufficient favored nodes for region "
operator|+
name|hri
operator|+
literal|" fn: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|favoredServerList
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|favoredServerList
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
operator|==
name|PRIMARY
operator|.
name|ordinal
argument_list|()
condition|)
name|addPrimaryAssignment
argument_list|(
name|hri
argument_list|,
name|favoredServerList
index|[
name|i
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|==
name|SECONDARY
operator|.
name|ordinal
argument_list|()
condition|)
name|addSecondaryAssignment
argument_list|(
name|hri
argument_list|,
name|favoredServerList
index|[
name|i
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|==
name|TERTIARY
operator|.
name|ordinal
argument_list|()
condition|)
name|addTeritiaryAssignment
argument_list|(
name|hri
argument_list|,
name|favoredServerList
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Catche remote exception "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
operator|+
literal|" when processing"
operator|+
name|result
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
block|}
decl_stmt|;
comment|// Scan hbase:meta to pick up user regions
name|MetaTableAccessor
operator|.
name|fullScanRegions
argument_list|(
name|connection
argument_list|,
name|v
argument_list|)
expr_stmt|;
comment|//regionToRegionServerMap = regions;
name|LOG
operator|.
name|info
argument_list|(
literal|"Finished to scan the hbase:meta for the current region assignment"
operator|+
literal|"snapshot"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addRegion
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|)
block|{
comment|// Process the region name to region info map
name|regionNameToRegionInfoMap
operator|.
name|put
argument_list|(
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|regionInfo
argument_list|)
expr_stmt|;
comment|// Process the table to region map
name|TableName
name|tableName
init|=
name|regionInfo
operator|.
name|getTable
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regionList
init|=
name|tableToRegionMap
operator|.
name|get
argument_list|(
name|tableName
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
comment|// Add the current region info into the tableToRegionMap
name|regionList
operator|.
name|add
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
name|tableToRegionMap
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|regionList
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addAssignment
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|,
name|ServerName
name|server
parameter_list|)
block|{
comment|// Process the region to region server map
name|regionToRegionServerMap
operator|.
name|put
argument_list|(
name|regionInfo
argument_list|,
name|server
argument_list|)
expr_stmt|;
if|if
condition|(
name|server
operator|==
literal|null
condition|)
return|return;
comment|// Process the region server to region map
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regionList
init|=
name|currentRSToRegionMap
operator|.
name|get
argument_list|(
name|server
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
name|regionInfo
argument_list|)
expr_stmt|;
name|currentRSToRegionMap
operator|.
name|put
argument_list|(
name|server
argument_list|,
name|regionList
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addPrimaryAssignment
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|,
name|ServerName
name|server
parameter_list|)
block|{
comment|// Process the region server to region map
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
name|server
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
name|regionInfo
argument_list|)
expr_stmt|;
name|primaryRSToRegionMap
operator|.
name|put
argument_list|(
name|server
argument_list|,
name|regionList
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addSecondaryAssignment
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|,
name|ServerName
name|server
parameter_list|)
block|{
comment|// Process the region server to region map
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regionList
init|=
name|secondaryRSToRegionMap
operator|.
name|get
argument_list|(
name|server
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
name|regionInfo
argument_list|)
expr_stmt|;
name|secondaryRSToRegionMap
operator|.
name|put
argument_list|(
name|server
argument_list|,
name|regionList
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addTeritiaryAssignment
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|,
name|ServerName
name|server
parameter_list|)
block|{
comment|// Process the region server to region map
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regionList
init|=
name|teritiaryRSToRegionMap
operator|.
name|get
argument_list|(
name|server
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
name|regionInfo
argument_list|)
expr_stmt|;
name|teritiaryRSToRegionMap
operator|.
name|put
argument_list|(
name|server
argument_list|,
name|regionList
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the regioninfo for a region    * @return the regioninfo    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|RegionInfo
argument_list|>
name|getRegionNameToRegionInfoMap
parameter_list|()
block|{
return|return
name|this
operator|.
name|regionNameToRegionInfoMap
return|;
block|}
comment|/**    * Get regions for tables    * @return a mapping from table to regions    */
specifier|public
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
name|getTableToRegionMap
parameter_list|()
block|{
return|return
name|tableToRegionMap
return|;
block|}
comment|/**    * Get region to region server map    * @return region to region server map    */
specifier|public
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|ServerName
argument_list|>
name|getRegionToRegionServerMap
parameter_list|()
block|{
return|return
name|regionToRegionServerMap
return|;
block|}
comment|/**    * Get regionserver to region map    * @return regionserver to region map    */
specifier|public
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
name|getRegionServerToRegionMap
parameter_list|()
block|{
return|return
name|currentRSToRegionMap
return|;
block|}
comment|/**    * Get the favored nodes plan    * @return the existing favored nodes plan    */
specifier|public
name|FavoredNodesPlan
name|getExistingAssignmentPlan
parameter_list|()
block|{
return|return
name|this
operator|.
name|existingAssignmentPlan
return|;
block|}
comment|/**    * Get the table set    * @return the table set    */
specifier|public
name|Set
argument_list|<
name|TableName
argument_list|>
name|getTableSet
parameter_list|()
block|{
return|return
name|this
operator|.
name|tableToRegionMap
operator|.
name|keySet
argument_list|()
return|;
block|}
specifier|public
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
name|getSecondaryToRegionInfoMap
parameter_list|()
block|{
return|return
name|this
operator|.
name|secondaryRSToRegionMap
return|;
block|}
specifier|public
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
name|getTertiaryToRegionInfoMap
parameter_list|()
block|{
return|return
name|this
operator|.
name|teritiaryRSToRegionMap
return|;
block|}
specifier|public
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
name|getPrimaryToRegionInfoMap
parameter_list|()
block|{
return|return
name|this
operator|.
name|primaryRSToRegionMap
return|;
block|}
block|}
end_class

end_unit

