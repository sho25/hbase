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
comment|/**  * Group user API interface used between client and server.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|RSGroupAdmin
block|{
comment|/**    * Gets {@code RSGroupInfo} for given group name.    */
name|RSGroupInfo
name|getRSGroupInfo
parameter_list|(
name|String
name|groupName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Gets {@code RSGroupInfo} for the given table's group.    */
name|RSGroupInfo
name|getRSGroupInfoOfTable
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Move given set of servers to the specified target RegionServer group.    */
name|void
name|moveServers
parameter_list|(
name|Set
argument_list|<
name|Address
argument_list|>
name|servers
parameter_list|,
name|String
name|targetGroup
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Move given set of tables to the specified target RegionServer group.    * This will unassign all of a table's region so it can be reassigned to the correct group.    */
name|void
name|moveTables
parameter_list|(
name|Set
argument_list|<
name|TableName
argument_list|>
name|tables
parameter_list|,
name|String
name|targetGroup
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Creates a new RegionServer group with the given name.    */
name|void
name|addRSGroup
parameter_list|(
name|String
name|groupName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Removes RegionServer group associated with the given name.    */
name|void
name|removeRSGroup
parameter_list|(
name|String
name|groupName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Balance regions in the given RegionServer group.    *    * @return boolean Whether balance ran or not    */
name|boolean
name|balanceRSGroup
parameter_list|(
name|String
name|groupName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Lists current set of RegionServer groups.    */
name|List
argument_list|<
name|RSGroupInfo
argument_list|>
name|listRSGroups
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Retrieve the RSGroupInfo a server is affiliated to    * @param hostPort HostPort to get RSGroupInfo for    */
name|RSGroupInfo
name|getRSGroupOfServer
parameter_list|(
name|Address
name|hostPort
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Move given set of servers and tables to the specified target RegionServer group.    * @param servers set of servers to move    * @param tables set of tables to move    * @param targetGroup the target group name    * @throws IOException if moving the server and tables fail    */
name|void
name|moveServersAndTables
parameter_list|(
name|Set
argument_list|<
name|Address
argument_list|>
name|servers
parameter_list|,
name|Set
argument_list|<
name|TableName
argument_list|>
name|tables
parameter_list|,
name|String
name|targetGroup
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Remove decommissioned servers from rsgroup.    * 1. Sometimes we may find the server aborted due to some hardware failure and we must offline    * the server for repairing. Or we need to move some servers to join other clusters.    * So we need to remove these servers from the rsgroup.    * 2. Dead/recovering/live servers will be disallowed.    * @param servers set of servers to remove    */
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

