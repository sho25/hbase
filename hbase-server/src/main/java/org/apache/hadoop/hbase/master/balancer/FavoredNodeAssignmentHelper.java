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
name|Random
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
name|catalog
operator|.
name|CatalogTracker
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
name|catalog
operator|.
name|MetaEditor
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
name|catalog
operator|.
name|MetaReader
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
name|catalog
operator|.
name|MetaReader
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
name|client
operator|.
name|Put
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|FavoredNodes
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
name|EnvironmentEdgeManager
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
import|;
end_import

begin_comment
comment|/**  * Helper class for {@link FavoredNodeLoadBalancer} that has all the intelligence  * for racks, meta scans, etc. Instantiated by the {@link FavoredNodeLoadBalancer}  * when needed (from within calls like  * {@link FavoredNodeLoadBalancer#randomAssignment(HRegionInfo, List)}).  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|FavoredNodeAssignmentHelper
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
name|FavoredNodeAssignmentHelper
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|RackManager
name|rackManager
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
argument_list|>
name|rackToRegionServerMap
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|uniqueRackList
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|ServerName
argument_list|,
name|String
argument_list|>
name|regionServerToRackMap
decl_stmt|;
specifier|private
name|Random
name|random
decl_stmt|;
specifier|private
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|FAVOREDNODES_QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fn"
argument_list|)
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|short
name|FAVORED_NODES_NUM
init|=
literal|3
decl_stmt|;
specifier|public
name|FavoredNodeAssignmentHelper
parameter_list|(
specifier|final
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|this
argument_list|(
name|servers
argument_list|,
operator|new
name|RackManager
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|FavoredNodeAssignmentHelper
parameter_list|(
specifier|final
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
parameter_list|,
specifier|final
name|RackManager
name|rackManager
parameter_list|)
block|{
name|this
operator|.
name|servers
operator|=
name|servers
expr_stmt|;
name|this
operator|.
name|rackManager
operator|=
name|rackManager
expr_stmt|;
name|this
operator|.
name|rackToRegionServerMap
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|regionServerToRackMap
operator|=
operator|new
name|HashMap
argument_list|<
name|ServerName
argument_list|,
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|uniqueRackList
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|random
operator|=
operator|new
name|Random
argument_list|()
expr_stmt|;
block|}
comment|/**    * Perform full scan of the meta table similar to    * {@link MetaReader#fullScan(CatalogTracker, Set, boolean)} except that this is    * aware of the favored nodes    * @param catalogTracker    * @param disabledTables    * @param excludeOfflinedSplitParents    * @param balancer required because we need to let the balancer know about the    * current favored nodes from meta scan    * @return Returns a map of every region to it's currently assigned server,    * according to META.  If the region does not have an assignment it will have    * a null value in the map.    * @throws IOException    */
specifier|public
specifier|static
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|fullScan
parameter_list|(
name|CatalogTracker
name|catalogTracker
parameter_list|,
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|disabledTables
parameter_list|,
specifier|final
name|boolean
name|excludeOfflinedSplitParents
parameter_list|,
name|FavoredNodeLoadBalancer
name|balancer
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|regions
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
specifier|final
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
index|[]
argument_list|>
name|favoredNodesMap
init|=
operator|new
name|HashMap
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
index|[]
argument_list|>
argument_list|()
decl_stmt|;
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
name|r
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|r
operator|==
literal|null
operator|||
name|r
operator|.
name|isEmpty
argument_list|()
condition|)
return|return
literal|true
return|;
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|region
init|=
name|HRegionInfo
operator|.
name|getHRegionInfoAndServerName
argument_list|(
name|r
argument_list|)
decl_stmt|;
name|HRegionInfo
name|hri
init|=
name|region
operator|.
name|getFirst
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
name|getTableNameAsString
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
name|getTableNameAsString
argument_list|()
argument_list|)
condition|)
return|return
literal|true
return|;
comment|// Are we to include split parents in the list?
if|if
condition|(
name|excludeOfflinedSplitParents
operator|&&
name|hri
operator|.
name|isSplitParent
argument_list|()
condition|)
return|return
literal|true
return|;
name|regions
operator|.
name|put
argument_list|(
name|hri
argument_list|,
name|region
operator|.
name|getSecond
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|favoredNodes
init|=
name|r
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
operator|!=
literal|null
condition|)
block|{
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
name|favoredNodesMap
operator|.
name|put
argument_list|(
name|hri
argument_list|,
name|favoredServerList
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
block|}
decl_stmt|;
name|MetaReader
operator|.
name|fullScan
argument_list|(
name|catalogTracker
argument_list|,
name|v
argument_list|)
expr_stmt|;
name|balancer
operator|.
name|noteFavoredNodes
argument_list|(
name|favoredNodesMap
argument_list|)
expr_stmt|;
return|return
name|regions
return|;
block|}
specifier|public
specifier|static
name|void
name|updateMetaWithFavoredNodesInfo
parameter_list|(
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
argument_list|>
name|regionToFavoredNodes
parameter_list|,
name|CatalogTracker
name|catalogTracker
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Put
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<
name|Put
argument_list|>
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
name|Put
name|put
init|=
name|makePutFromRegionInfo
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|put
operator|!=
literal|null
condition|)
block|{
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
block|}
name|MetaEditor
operator|.
name|putsToMetaTable
argument_list|(
name|catalogTracker
argument_list|,
name|puts
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Added "
operator|+
name|puts
operator|.
name|size
argument_list|()
operator|+
literal|" regions in META"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Generates and returns a Put containing the region info for the catalog table    * and the servers    * @param regionInfo    * @param favoredNodeList    * @return Put object    */
specifier|static
name|Put
name|makePutFromRegionInfo
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|favoredNodeList
parameter_list|)
throws|throws
name|IOException
block|{
name|Put
name|put
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|favoredNodeList
operator|!=
literal|null
condition|)
block|{
name|put
operator|=
name|MetaEditor
operator|.
name|makePutFromRegionInfo
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
name|byte
index|[]
name|favoredNodes
init|=
name|getFavoredNodes
argument_list|(
name|favoredNodeList
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|FAVOREDNODES_QUALIFIER
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|favoredNodes
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Create the region "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" with favored nodes "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|favoredNodes
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|put
return|;
block|}
comment|/**    * @param favoredNodes The PB'ed bytes of favored nodes    * @return the array of {@link ServerName} for the byte array of favored nodes.    * @throws InvalidProtocolBufferException    */
specifier|public
specifier|static
name|ServerName
index|[]
name|getFavoredNodesList
parameter_list|(
name|byte
index|[]
name|favoredNodes
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
block|{
name|FavoredNodes
name|f
init|=
name|FavoredNodes
operator|.
name|parseFrom
argument_list|(
name|favoredNodes
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HBaseProtos
operator|.
name|ServerName
argument_list|>
name|protoNodes
init|=
name|f
operator|.
name|getFavoredNodeList
argument_list|()
decl_stmt|;
name|ServerName
index|[]
name|servers
init|=
operator|new
name|ServerName
index|[
name|protoNodes
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|HBaseProtos
operator|.
name|ServerName
name|node
range|:
name|protoNodes
control|)
block|{
name|servers
index|[
name|i
operator|++
index|]
operator|=
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
return|return
name|servers
return|;
block|}
comment|/**    * @param serverList    * @return PB'ed bytes of {@link FavoredNodes} generated by the server list.    */
specifier|static
name|byte
index|[]
name|getFavoredNodes
parameter_list|(
name|List
argument_list|<
name|ServerName
argument_list|>
name|serverAddrList
parameter_list|)
block|{
name|FavoredNodes
operator|.
name|Builder
name|f
init|=
name|FavoredNodes
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|ServerName
name|s
range|:
name|serverAddrList
control|)
block|{
name|HBaseProtos
operator|.
name|ServerName
operator|.
name|Builder
name|b
init|=
name|HBaseProtos
operator|.
name|ServerName
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|b
operator|.
name|setHostName
argument_list|(
name|s
operator|.
name|getHostname
argument_list|()
argument_list|)
expr_stmt|;
name|b
operator|.
name|setPort
argument_list|(
name|s
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|b
operator|.
name|setStartCode
argument_list|(
name|s
operator|.
name|getStartcode
argument_list|()
argument_list|)
expr_stmt|;
name|f
operator|.
name|addFavoredNode
argument_list|(
name|b
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|f
operator|.
name|build
argument_list|()
operator|.
name|toByteArray
argument_list|()
return|;
block|}
comment|// Place the regions round-robin across the racks picking one server from each
comment|// rack at a time. Start with a random rack, and a random server from every rack.
comment|// If a rack doesn't have enough servers it will go to the next rack and so on.
comment|// for choosing a primary.
comment|// For example, if 4 racks (r1 .. r4) with 8 servers (s1..s8) each, one possible
comment|// placement could be r2:s5, r3:s5, r4:s5, r1:s5, r2:s6, r3:s6..
comment|// If there were fewer servers in one rack, say r3, which had 3 servers, one possible
comment|// placement could be r2:s5,<skip-r3>, r4:s5, r1:s5, r2:s6,<skip-r3> ...
comment|// The regions should be distributed proportionately to the racksizes
name|void
name|placePrimaryRSAsRoundRobin
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
name|assignmentMap
parameter_list|,
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|primaryRSMap
parameter_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|rackList
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|rackToRegionServerMap
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|rackList
operator|.
name|addAll
argument_list|(
name|rackToRegionServerMap
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|rackIndex
init|=
name|random
operator|.
name|nextInt
argument_list|(
name|rackList
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|maxRackSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
argument_list|>
name|r
range|:
name|rackToRegionServerMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|r
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
operator|>
name|maxRackSize
condition|)
block|{
name|maxRackSize
operator|=
name|r
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
block|}
name|int
name|numIterations
init|=
literal|0
decl_stmt|;
name|int
name|firstServerIndex
init|=
name|random
operator|.
name|nextInt
argument_list|(
name|maxRackSize
argument_list|)
decl_stmt|;
comment|// Initialize the current processing host index.
name|int
name|serverIndex
init|=
name|firstServerIndex
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|regionInfo
range|:
name|regions
control|)
block|{
name|List
argument_list|<
name|ServerName
argument_list|>
name|currentServerList
decl_stmt|;
name|String
name|rackName
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|rackName
operator|=
name|rackList
operator|.
name|get
argument_list|(
name|rackIndex
argument_list|)
expr_stmt|;
name|numIterations
operator|++
expr_stmt|;
comment|// Get the server list for the current rack
name|currentServerList
operator|=
name|rackToRegionServerMap
operator|.
name|get
argument_list|(
name|rackName
argument_list|)
expr_stmt|;
if|if
condition|(
name|serverIndex
operator|>=
name|currentServerList
operator|.
name|size
argument_list|()
condition|)
block|{
comment|//not enough machines in this rack
if|if
condition|(
name|numIterations
operator|%
name|rackList
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
if|if
condition|(
operator|++
name|serverIndex
operator|>=
name|maxRackSize
condition|)
name|serverIndex
operator|=
literal|0
expr_stmt|;
block|}
if|if
condition|(
operator|(
operator|++
name|rackIndex
operator|)
operator|>=
name|rackList
operator|.
name|size
argument_list|()
condition|)
block|{
name|rackIndex
operator|=
literal|0
expr_stmt|;
comment|// reset the rack index to 0
block|}
block|}
else|else
break|break;
block|}
comment|// Get the current process region server
name|ServerName
name|currentServer
init|=
name|currentServerList
operator|.
name|get
argument_list|(
name|serverIndex
argument_list|)
decl_stmt|;
comment|// Place the current region with the current primary region server
name|primaryRSMap
operator|.
name|put
argument_list|(
name|regionInfo
argument_list|,
name|currentServer
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionsForServer
init|=
name|assignmentMap
operator|.
name|get
argument_list|(
name|currentServer
argument_list|)
decl_stmt|;
if|if
condition|(
name|regionsForServer
operator|==
literal|null
condition|)
block|{
name|regionsForServer
operator|=
operator|new
name|ArrayList
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|()
expr_stmt|;
name|assignmentMap
operator|.
name|put
argument_list|(
name|currentServer
argument_list|,
name|regionsForServer
argument_list|)
expr_stmt|;
block|}
name|regionsForServer
operator|.
name|add
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
comment|// Set the next processing index
if|if
condition|(
name|numIterations
operator|%
name|rackList
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
operator|++
name|serverIndex
expr_stmt|;
block|}
if|if
condition|(
operator|(
operator|++
name|rackIndex
operator|)
operator|>=
name|rackList
operator|.
name|size
argument_list|()
condition|)
block|{
name|rackIndex
operator|=
literal|0
expr_stmt|;
comment|// reset the rack index to 0
block|}
block|}
block|}
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
index|[]
argument_list|>
name|placeSecondaryAndTertiaryRS
parameter_list|(
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|primaryRSMap
parameter_list|)
block|{
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
index|[]
argument_list|>
name|secondaryAndTertiaryMap
init|=
operator|new
name|HashMap
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
index|[]
argument_list|>
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
name|entry
range|:
name|primaryRSMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|// Get the target region and its primary region server rack
name|HRegionInfo
name|regionInfo
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|ServerName
name|primaryRS
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
try|try
block|{
comment|// Create the secondary and tertiary region server pair object.
name|ServerName
index|[]
name|favoredNodes
decl_stmt|;
comment|// Get the rack for the primary region server
name|String
name|primaryRack
init|=
name|rackManager
operator|.
name|getRack
argument_list|(
name|primaryRS
argument_list|)
decl_stmt|;
if|if
condition|(
name|getTotalNumberOfRacks
argument_list|()
operator|==
literal|1
condition|)
block|{
name|favoredNodes
operator|=
name|singleRackCase
argument_list|(
name|regionInfo
argument_list|,
name|primaryRS
argument_list|,
name|primaryRack
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|favoredNodes
operator|=
name|multiRackCase
argument_list|(
name|regionInfo
argument_list|,
name|primaryRS
argument_list|,
name|primaryRack
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|favoredNodes
operator|!=
literal|null
condition|)
block|{
name|secondaryAndTertiaryMap
operator|.
name|put
argument_list|(
name|regionInfo
argument_list|,
name|favoredNodes
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Place the secondary and tertiary region server for region "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Cannot place the favored nodes for region "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" because "
operator|+
name|e
argument_list|)
expr_stmt|;
continue|continue;
block|}
block|}
return|return
name|secondaryAndTertiaryMap
return|;
block|}
specifier|private
name|ServerName
index|[]
name|singleRackCase
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
name|ServerName
name|primaryRS
parameter_list|,
name|String
name|primaryRack
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Single rack case: have to pick the secondary and tertiary
comment|// from the same rack
name|List
argument_list|<
name|ServerName
argument_list|>
name|serverList
init|=
name|getServersFromRack
argument_list|(
name|primaryRack
argument_list|)
decl_stmt|;
if|if
condition|(
name|serverList
operator|.
name|size
argument_list|()
operator|<=
literal|2
condition|)
block|{
comment|// Single region server case: cannot not place the favored nodes
comment|// on any server; !domain.canPlaceFavoredNodes()
return|return
literal|null
return|;
block|}
else|else
block|{
comment|// Randomly select two region servers from the server list and make sure
comment|// they are not overlap with the primary region server;
name|Set
argument_list|<
name|ServerName
argument_list|>
name|serverSkipSet
init|=
operator|new
name|HashSet
argument_list|<
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
name|serverSkipSet
operator|.
name|add
argument_list|(
name|primaryRS
argument_list|)
expr_stmt|;
comment|// Place the secondary RS
name|ServerName
name|secondaryRS
init|=
name|getOneRandomServer
argument_list|(
name|primaryRack
argument_list|,
name|serverSkipSet
argument_list|)
decl_stmt|;
comment|// Skip the secondary for the tertiary placement
name|serverSkipSet
operator|.
name|add
argument_list|(
name|secondaryRS
argument_list|)
expr_stmt|;
comment|// Place the tertiary RS
name|ServerName
name|tertiaryRS
init|=
name|getOneRandomServer
argument_list|(
name|primaryRack
argument_list|,
name|serverSkipSet
argument_list|)
decl_stmt|;
if|if
condition|(
name|secondaryRS
operator|==
literal|null
operator|||
name|tertiaryRS
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Cannot place the secondary and terinary"
operator|+
literal|"region server for region "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Create the secondary and tertiary pair
name|ServerName
index|[]
name|favoredNodes
init|=
operator|new
name|ServerName
index|[
literal|2
index|]
decl_stmt|;
name|favoredNodes
index|[
literal|0
index|]
operator|=
name|secondaryRS
expr_stmt|;
name|favoredNodes
index|[
literal|1
index|]
operator|=
name|tertiaryRS
expr_stmt|;
return|return
name|favoredNodes
return|;
block|}
block|}
specifier|private
name|ServerName
index|[]
name|multiRackCase
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
name|ServerName
name|primaryRS
parameter_list|,
name|String
name|primaryRack
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Random to choose the secondary and tertiary region server
comment|// from another rack to place the secondary and tertiary
comment|// Random to choose one rack except for the current rack
name|Set
argument_list|<
name|String
argument_list|>
name|rackSkipSet
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|rackSkipSet
operator|.
name|add
argument_list|(
name|primaryRack
argument_list|)
expr_stmt|;
name|ServerName
index|[]
name|favoredNodes
init|=
operator|new
name|ServerName
index|[
literal|2
index|]
decl_stmt|;
name|String
name|secondaryRack
init|=
name|getOneRandomRack
argument_list|(
name|rackSkipSet
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|serverList
init|=
name|getServersFromRack
argument_list|(
name|secondaryRack
argument_list|)
decl_stmt|;
if|if
condition|(
name|serverList
operator|.
name|size
argument_list|()
operator|>=
literal|2
condition|)
block|{
comment|// Randomly pick up two servers from this secondary rack
comment|// Place the secondary RS
name|ServerName
name|secondaryRS
init|=
name|getOneRandomServer
argument_list|(
name|secondaryRack
argument_list|)
decl_stmt|;
comment|// Skip the secondary for the tertiary placement
name|Set
argument_list|<
name|ServerName
argument_list|>
name|skipServerSet
init|=
operator|new
name|HashSet
argument_list|<
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
name|skipServerSet
operator|.
name|add
argument_list|(
name|secondaryRS
argument_list|)
expr_stmt|;
comment|// Place the tertiary RS
name|ServerName
name|tertiaryRS
init|=
name|getOneRandomServer
argument_list|(
name|secondaryRack
argument_list|,
name|skipServerSet
argument_list|)
decl_stmt|;
if|if
condition|(
name|secondaryRS
operator|==
literal|null
operator|||
name|tertiaryRS
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Cannot place the secondary and terinary"
operator|+
literal|"region server for region "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Create the secondary and tertiary pair
name|favoredNodes
index|[
literal|0
index|]
operator|=
name|secondaryRS
expr_stmt|;
name|favoredNodes
index|[
literal|1
index|]
operator|=
name|tertiaryRS
expr_stmt|;
block|}
else|else
block|{
comment|// Pick the secondary rs from this secondary rack
comment|// and pick the tertiary from another random rack
name|favoredNodes
index|[
literal|0
index|]
operator|=
name|getOneRandomServer
argument_list|(
name|secondaryRack
argument_list|)
expr_stmt|;
comment|// Pick the tertiary
if|if
condition|(
name|getTotalNumberOfRacks
argument_list|()
operator|==
literal|2
condition|)
block|{
comment|// Pick the tertiary from the same rack of the primary RS
name|Set
argument_list|<
name|ServerName
argument_list|>
name|serverSkipSet
init|=
operator|new
name|HashSet
argument_list|<
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
name|serverSkipSet
operator|.
name|add
argument_list|(
name|primaryRS
argument_list|)
expr_stmt|;
name|favoredNodes
index|[
literal|1
index|]
operator|=
name|getOneRandomServer
argument_list|(
name|primaryRack
argument_list|,
name|serverSkipSet
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Pick the tertiary from another rack
name|rackSkipSet
operator|.
name|add
argument_list|(
name|secondaryRack
argument_list|)
expr_stmt|;
name|String
name|tertiaryRandomRack
init|=
name|getOneRandomRack
argument_list|(
name|rackSkipSet
argument_list|)
decl_stmt|;
name|favoredNodes
index|[
literal|1
index|]
operator|=
name|getOneRandomServer
argument_list|(
name|tertiaryRandomRack
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|favoredNodes
return|;
block|}
name|boolean
name|canPlaceFavoredNodes
parameter_list|()
block|{
name|int
name|serverSize
init|=
name|this
operator|.
name|regionServerToRackMap
operator|.
name|size
argument_list|()
decl_stmt|;
return|return
operator|(
name|serverSize
operator|>=
name|FAVORED_NODES_NUM
operator|)
return|;
block|}
name|void
name|initialize
parameter_list|()
block|{
for|for
control|(
name|ServerName
name|sn
range|:
name|this
operator|.
name|servers
control|)
block|{
name|String
name|rackName
init|=
name|this
operator|.
name|rackManager
operator|.
name|getRack
argument_list|(
name|sn
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|serverList
init|=
name|this
operator|.
name|rackToRegionServerMap
operator|.
name|get
argument_list|(
name|rackName
argument_list|)
decl_stmt|;
if|if
condition|(
name|serverList
operator|==
literal|null
condition|)
block|{
name|serverList
operator|=
operator|new
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
argument_list|()
expr_stmt|;
comment|// Add the current rack to the unique rack list
name|this
operator|.
name|uniqueRackList
operator|.
name|add
argument_list|(
name|rackName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|serverList
operator|.
name|contains
argument_list|(
name|sn
argument_list|)
condition|)
block|{
name|serverList
operator|.
name|add
argument_list|(
name|sn
argument_list|)
expr_stmt|;
name|this
operator|.
name|rackToRegionServerMap
operator|.
name|put
argument_list|(
name|rackName
argument_list|,
name|serverList
argument_list|)
expr_stmt|;
name|this
operator|.
name|regionServerToRackMap
operator|.
name|put
argument_list|(
name|sn
argument_list|,
name|rackName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|int
name|getTotalNumberOfRacks
parameter_list|()
block|{
return|return
name|this
operator|.
name|uniqueRackList
operator|.
name|size
argument_list|()
return|;
block|}
specifier|private
name|List
argument_list|<
name|ServerName
argument_list|>
name|getServersFromRack
parameter_list|(
name|String
name|rack
parameter_list|)
block|{
return|return
name|this
operator|.
name|rackToRegionServerMap
operator|.
name|get
argument_list|(
name|rack
argument_list|)
return|;
block|}
specifier|private
name|ServerName
name|getOneRandomServer
parameter_list|(
name|String
name|rack
parameter_list|,
name|Set
argument_list|<
name|ServerName
argument_list|>
name|skipServerSet
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|rack
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|serverList
init|=
name|this
operator|.
name|rackToRegionServerMap
operator|.
name|get
argument_list|(
name|rack
argument_list|)
decl_stmt|;
if|if
condition|(
name|serverList
operator|==
literal|null
condition|)
return|return
literal|null
return|;
comment|// Get a random server except for any servers from the skip set
if|if
condition|(
name|skipServerSet
operator|!=
literal|null
operator|&&
name|serverList
operator|.
name|size
argument_list|()
operator|<=
name|skipServerSet
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot randomly pick another random server"
argument_list|)
throw|;
block|}
name|ServerName
name|randomServer
decl_stmt|;
do|do
block|{
name|int
name|randomIndex
init|=
name|random
operator|.
name|nextInt
argument_list|(
name|serverList
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|randomServer
operator|=
name|serverList
operator|.
name|get
argument_list|(
name|randomIndex
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|skipServerSet
operator|!=
literal|null
operator|&&
name|skipServerSet
operator|.
name|contains
argument_list|(
name|randomServer
argument_list|)
condition|)
do|;
return|return
name|randomServer
return|;
block|}
specifier|private
name|ServerName
name|getOneRandomServer
parameter_list|(
name|String
name|rack
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|getOneRandomServer
argument_list|(
name|rack
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|private
name|String
name|getOneRandomRack
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|skipRackSet
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|skipRackSet
operator|==
literal|null
operator|||
name|uniqueRackList
operator|.
name|size
argument_list|()
operator|<=
name|skipRackSet
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot randomly pick another random server"
argument_list|)
throw|;
block|}
name|String
name|randomRack
decl_stmt|;
do|do
block|{
name|int
name|randomIndex
init|=
name|random
operator|.
name|nextInt
argument_list|(
name|this
operator|.
name|uniqueRackList
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|randomRack
operator|=
name|this
operator|.
name|uniqueRackList
operator|.
name|get
argument_list|(
name|randomIndex
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|skipRackSet
operator|.
name|contains
argument_list|(
name|randomRack
argument_list|)
condition|)
do|;
return|return
name|randomRack
return|;
block|}
block|}
end_class

end_unit

