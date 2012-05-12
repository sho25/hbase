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
name|security
operator|.
name|access
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ipc
operator|.
name|CoprocessorProtocol
import|;
end_import

begin_comment
comment|/**  * A custom protocol defined for maintaining and querying access control lists.  */
end_comment

begin_interface
specifier|public
interface|interface
name|AccessControllerProtocol
extends|extends
name|CoprocessorProtocol
block|{
specifier|public
specifier|static
specifier|final
name|long
name|VERSION
init|=
literal|1L
decl_stmt|;
comment|/**    * Grants the given user or group the privilege to perform the given actions    * @param userPermission the details of the provided user permissions    * @throws IOException if the grant could not be applied    */
specifier|public
name|void
name|grant
parameter_list|(
name|UserPermission
name|userPermission
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Grants the given user or group the privilege to perform the given actions    * over the specified scope contained in {@link TablePermission}    * @param user the user name, or, if prefixed with "@", group name receiving    * the grant    * @param permission the details of the provided permissions    * @throws IOException if the grant could not be applied    * @deprecated Use {@link #revoke(UserPermission userPermission)} instead    */
annotation|@
name|Deprecated
specifier|public
name|void
name|grant
parameter_list|(
name|byte
index|[]
name|user
parameter_list|,
name|TablePermission
name|permission
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Revokes a previously granted privilege from a user or group.    * Note that the provided {@link TablePermission} details must exactly match    * a stored grant.  For example, if user "bob" has been granted "READ" access    * to table "data", over column family and qualifer "info:colA", then the    * table, column family and column qualifier must all be specified.    * Attempting to revoke permissions over just the "data" table will have    * no effect.    * @param permission the details of the previously granted permission to revoke    * @throws IOException if the revocation could not be performed    */
specifier|public
name|void
name|revoke
parameter_list|(
name|UserPermission
name|userPermission
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Revokes a previously granted privilege from a user or group.    * Note that the provided {@link TablePermission} details must exactly match    * a stored grant.  For example, if user "bob" has been granted "READ" access    * to table "data", over column family and qualifer "info:colA", then the    * table, column family and column qualifier must all be specified.    * Attempting to revoke permissions over just the "data" table will have    * no effect.    * @param user the user name, or, if prefixed with "@", group name whose    * privileges are being revoked    * @param permission the details of the previously granted permission to revoke    * @throws IOException if the revocation could not be performed    * @deprecated Use {@link #revoke(UserPermission userPermission)} instead    */
annotation|@
name|Deprecated
specifier|public
name|void
name|revoke
parameter_list|(
name|byte
index|[]
name|user
parameter_list|,
name|TablePermission
name|permission
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Queries the permissions currently stored for the given table, returning    * a list of currently granted permissions, along with the user or group    * each is associated with.    * @param tableName the table of the permission grants to return    * @return a list of the currently granted permissions, with associated user    * or group names    * @throws IOException if there is an error querying the permissions    */
specifier|public
name|List
argument_list|<
name|UserPermission
argument_list|>
name|getUserPermissions
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Checks whether the given Permissions will pass the access checks for the    * current user. Global permissions can be checked from the -acl- table    * or any other table, however TablePermissions can only be checked by    * the table's regions. If access control checks fail this method throws    * AccessDeniedException.    * @param permissions to check for. Permission subclasses can be used    * to do more specific checks at the table/family/column level.    * @throws IOException if there is an error checking the permissions    */
specifier|public
name|void
name|checkPermissions
parameter_list|(
name|Permission
index|[]
name|permissions
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

