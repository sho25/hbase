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
name|Closeable
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
name|net
operator|.
name|Address
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
comment|/**    * Gets the regionserver group information.    *    * @param groupName the group name    * @return An instance of RSGroupInfo    */
name|RSGroupInfo
name|getRSGroupInfo
parameter_list|(
name|String
name|groupName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Gets the regionserver group info of table.    *    * @param tableName the table name    * @return An instance of RSGroupInfo.    */
name|RSGroupInfo
name|getRSGroupInfoOfTable
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Move a set of serves to another group    *    *    * @param servers set of servers, must be in the form HOST:PORT    * @param targetGroup the target group    * @throws java.io.IOException Signals that an I/O exception has occurred.    */
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
comment|/**    * Move tables to a new group.    * This will unassign all of a table's region so it can be reassigned to the correct group.    * @param tables list of tables to move    * @param targetGroup target group    * @throws java.io.IOException on failure to move tables    */
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
comment|/**    * Add a new group    * @param name name of the group    * @throws java.io.IOException on failure to add group    */
name|void
name|addRSGroup
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Remove a regionserver group    * @param name name of the group    * @throws java.io.IOException on failure to remove group    */
name|void
name|removeRSGroup
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Balance the regions in a group    *    * @param name the name of the group to balance    * @return boolean whether balance ran or not    * @throws java.io.IOException on unexpected failure to balance group    */
name|boolean
name|balanceRSGroup
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Lists the existing groups.    *    * @return Collection of RSGroupInfo.    */
name|List
argument_list|<
name|RSGroupInfo
argument_list|>
name|listRSGroups
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Retrieve the RSGroupInfo a server is affiliated to    * @param hostPort HostPort to get RSGroupInfo for    * @return RSGroupInfo associated with the server    * @throws java.io.IOException on unexpected failure to retrieve GroupInfo    */
name|RSGroupInfo
name|getRSGroupOfServer
parameter_list|(
name|Address
name|hostPort
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

