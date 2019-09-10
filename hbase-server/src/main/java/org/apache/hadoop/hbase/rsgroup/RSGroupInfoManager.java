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
name|List
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Interface used to manage RSGroupInfo storage. An implementation has the option to support offline  * mode. See {@code RSGroupBasedLoadBalancer}.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|RSGroupInfoManager
block|{
name|void
name|start
parameter_list|()
function_decl|;
comment|/**    * Add given RSGroupInfo to existing list of group infos.    */
name|void
name|addRSGroup
parameter_list|(
name|RSGroupInfo
name|rsGroupInfo
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Remove a region server group.    */
name|void
name|removeRSGroup
parameter_list|(
name|String
name|groupName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Move servers to a new group.    * @param servers list of servers, must be part of the same group    * @param srcGroup groupName being moved from    * @param dstGroup groupName being moved to    * @return Set of servers moved (May be a subset of {@code servers}).    */
name|Set
argument_list|<
name|Address
argument_list|>
name|moveServers
parameter_list|(
name|Set
argument_list|<
name|Address
argument_list|>
name|servers
parameter_list|,
name|String
name|srcGroup
parameter_list|,
name|String
name|dstGroup
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Gets the group info of server.    */
name|RSGroupInfo
name|getRSGroupOfServer
parameter_list|(
name|Address
name|serverHostPort
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Gets {@code RSGroupInfo} for the given group name.    */
name|RSGroupInfo
name|getRSGroup
parameter_list|(
name|String
name|groupName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * List the existing {@code RSGroupInfo}s.    */
name|List
argument_list|<
name|RSGroupInfo
argument_list|>
name|listRSGroups
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Refresh/reload the group information from the persistent store    */
name|void
name|refresh
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Whether the manager is able to fully return group metadata    * @return whether the manager is in online mode    */
name|boolean
name|isOnline
parameter_list|()
function_decl|;
comment|/**    * Remove decommissioned servers from rsgroup    * @param servers set of servers to remove    */
name|void
name|removeServers
parameter_list|(
name|Set
argument_list|<
name|Address
argument_list|>
name|servers
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

