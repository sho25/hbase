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
name|favored
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentHashMap
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

begin_comment
comment|/**  * This class contains the mapping information between each region name and  * its favored region server list. Used by {@link FavoredNodeLoadBalancer} set  * of classes and from unit tests (hence the class is public)  *  * All the access to this class is thread-safe.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|FavoredNodesPlan
block|{
comment|/** the map between each region name and its favored region server list */
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
name|favoredNodesMap
decl_stmt|;
specifier|public
specifier|static
enum|enum
name|Position
block|{
name|PRIMARY
block|,
name|SECONDARY
block|,
name|TERTIARY
block|}
specifier|public
name|FavoredNodesPlan
parameter_list|()
block|{
name|favoredNodesMap
operator|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
comment|/**    * Update an assignment to the plan    * @param region    * @param servers    */
specifier|public
name|void
name|updateFavoredNodesMap
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
block|{
if|if
condition|(
name|region
operator|==
literal|null
operator|||
name|servers
operator|==
literal|null
operator|||
name|servers
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
name|this
operator|.
name|favoredNodesMap
operator|.
name|put
argument_list|(
name|region
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|servers
argument_list|)
expr_stmt|;
block|}
comment|/**    * Remove a favored node assignment    * @param region region    * @return the list of favored region server for this region based on the plan    */
specifier|public
name|List
argument_list|<
name|ServerName
argument_list|>
name|removeFavoredNodes
parameter_list|(
name|HRegionInfo
name|region
parameter_list|)
block|{
return|return
name|favoredNodesMap
operator|.
name|remove
argument_list|(
name|region
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @param region    * @return the list of favored region server for this region based on the plan    */
specifier|public
name|List
argument_list|<
name|ServerName
argument_list|>
name|getFavoredNodes
parameter_list|(
name|HRegionInfo
name|region
parameter_list|)
block|{
return|return
name|favoredNodesMap
operator|.
name|get
argument_list|(
name|region
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Return the position of the server in the favoredNodes list. Assumes the    * favoredNodes list is of size 3.    * @param favoredNodes    * @param server    * @return position    */
specifier|public
specifier|static
name|Position
name|getFavoredServerPosition
parameter_list|(
name|List
argument_list|<
name|ServerName
argument_list|>
name|favoredNodes
parameter_list|,
name|ServerName
name|server
parameter_list|)
block|{
if|if
condition|(
name|favoredNodes
operator|==
literal|null
operator|||
name|server
operator|==
literal|null
operator|||
name|favoredNodes
operator|.
name|size
argument_list|()
operator|!=
name|FavoredNodeAssignmentHelper
operator|.
name|FAVORED_NODES_NUM
condition|)
block|{
return|return
literal|null
return|;
block|}
for|for
control|(
name|Position
name|p
range|:
name|Position
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|ServerName
operator|.
name|isSameAddress
argument_list|(
name|favoredNodes
operator|.
name|get
argument_list|(
name|p
operator|.
name|ordinal
argument_list|()
argument_list|)
argument_list|,
name|server
argument_list|)
condition|)
block|{
return|return
name|p
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * @return the mapping between each region to its favored region server list    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
argument_list|>
name|getAssignmentMap
parameter_list|()
block|{
return|return
name|favoredNodesMap
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// To compare the map from objec o is identical to current assignment map.
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
argument_list|>
name|comparedMap
init|=
operator|(
operator|(
name|FavoredNodesPlan
operator|)
name|o
operator|)
operator|.
name|getAssignmentMap
argument_list|()
decl_stmt|;
comment|// compare the size
if|if
condition|(
name|comparedMap
operator|.
name|size
argument_list|()
operator|!=
name|this
operator|.
name|favoredNodesMap
operator|.
name|size
argument_list|()
condition|)
return|return
literal|false
return|;
comment|// compare each element in the assignment map
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
name|entry
range|:
name|comparedMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|List
argument_list|<
name|ServerName
argument_list|>
name|serverList
init|=
name|this
operator|.
name|favoredNodesMap
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|serverList
operator|==
literal|null
operator|&&
name|entry
operator|.
name|getValue
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
elseif|else
if|if
condition|(
name|serverList
operator|!=
literal|null
operator|&&
operator|!
name|serverList
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|favoredNodesMap
operator|.
name|hashCode
argument_list|()
return|;
block|}
block|}
end_class

end_unit

