begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ArrayList
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
name|regex
operator|.
name|Pattern
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
name|lang3
operator|.
name|StringUtils
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
name|MasterNotRunningException
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
name|ZooKeeperConnectionException
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
name|Admin
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
name|Table
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
name|TableDescriptor
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
name|security
operator|.
name|SecurityCapability
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
name|CoprocessorRpcChannel
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
name|AccessControlProtos
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
name|AccessControlProtos
operator|.
name|AccessControlService
operator|.
name|BlockingInterface
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Utility client for doing access control admin operations.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|AccessControlClient
block|{
specifier|public
specifier|static
specifier|final
name|TableName
name|ACL_TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE_NAME_STR
argument_list|,
literal|"acl"
argument_list|)
decl_stmt|;
comment|/**    * Return true if authorization is supported and enabled    * @param connection The connection to use    * @return true if authorization is supported and enabled, false otherwise    * @throws IOException    */
specifier|public
specifier|static
name|boolean
name|isAuthorizationEnabled
parameter_list|(
name|Connection
name|connection
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|connection
operator|.
name|getAdmin
argument_list|()
operator|.
name|getSecurityCapabilities
argument_list|()
operator|.
name|contains
argument_list|(
name|SecurityCapability
operator|.
name|AUTHORIZATION
argument_list|)
return|;
block|}
comment|/**    * Return true if cell authorization is supported and enabled    * @param connection The connection to use    * @return true if cell authorization is supported and enabled, false otherwise    * @throws IOException    */
specifier|public
specifier|static
name|boolean
name|isCellAuthorizationEnabled
parameter_list|(
name|Connection
name|connection
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|connection
operator|.
name|getAdmin
argument_list|()
operator|.
name|getSecurityCapabilities
argument_list|()
operator|.
name|contains
argument_list|(
name|SecurityCapability
operator|.
name|CELL_AUTHORIZATION
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|BlockingInterface
name|getAccessControlServiceStub
parameter_list|(
name|Table
name|ht
parameter_list|)
throws|throws
name|IOException
block|{
name|CoprocessorRpcChannel
name|service
init|=
name|ht
operator|.
name|coprocessorService
argument_list|(
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
name|BlockingInterface
name|protocol
init|=
name|AccessControlProtos
operator|.
name|AccessControlService
operator|.
name|newBlockingStub
argument_list|(
name|service
argument_list|)
decl_stmt|;
return|return
name|protocol
return|;
block|}
comment|/**    * Grants permission on the specified table for the specified user    * @param connection The Connection instance to use    * @param tableName    * @param userName    * @param family    * @param qual    * @param mergeExistingPermissions If set to false, later granted permissions will override    *          previous granted permissions. otherwise, it'll merge with previous granted    *          permissions.    * @param actions    * @throws Throwable    */
specifier|private
specifier|static
name|void
name|grant
parameter_list|(
name|Connection
name|connection
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|String
name|userName
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qual
parameter_list|,
name|boolean
name|mergeExistingPermissions
parameter_list|,
specifier|final
name|Permission
operator|.
name|Action
modifier|...
name|actions
parameter_list|)
throws|throws
name|Throwable
block|{
name|connection
operator|.
name|getAdmin
argument_list|()
operator|.
name|grant
argument_list|(
name|userName
argument_list|,
operator|new
name|TablePermission
argument_list|(
name|tableName
argument_list|,
name|family
argument_list|,
name|qual
argument_list|,
name|actions
argument_list|)
argument_list|,
name|mergeExistingPermissions
argument_list|)
expr_stmt|;
block|}
comment|/**    * Grants permission on the specified table for the specified user.    * If permissions for a specified user exists, later granted permissions will override previous granted permissions.    * @param connection The Connection instance to use    * @param tableName    * @param userName    * @param family    * @param qual    * @param actions    * @throws Throwable    */
specifier|public
specifier|static
name|void
name|grant
parameter_list|(
name|Connection
name|connection
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|String
name|userName
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qual
parameter_list|,
specifier|final
name|Permission
operator|.
name|Action
modifier|...
name|actions
parameter_list|)
throws|throws
name|Throwable
block|{
name|grant
argument_list|(
name|connection
argument_list|,
name|tableName
argument_list|,
name|userName
argument_list|,
name|family
argument_list|,
name|qual
argument_list|,
literal|true
argument_list|,
name|actions
argument_list|)
expr_stmt|;
block|}
comment|/**    * Grants permission on the specified namespace for the specified user.    * @param connection    * @param namespace    * @param userName    * @param mergeExistingPermissions If set to false, later granted permissions will override    *          previous granted permissions. otherwise, it'll merge with previous granted    *          permissions.    * @param actions    * @throws Throwable    */
specifier|private
specifier|static
name|void
name|grant
parameter_list|(
name|Connection
name|connection
parameter_list|,
specifier|final
name|String
name|namespace
parameter_list|,
specifier|final
name|String
name|userName
parameter_list|,
name|boolean
name|mergeExistingPermissions
parameter_list|,
specifier|final
name|Permission
operator|.
name|Action
modifier|...
name|actions
parameter_list|)
throws|throws
name|Throwable
block|{
name|connection
operator|.
name|getAdmin
argument_list|()
operator|.
name|grant
argument_list|(
name|userName
argument_list|,
operator|new
name|NamespacePermission
argument_list|(
name|namespace
argument_list|,
name|actions
argument_list|)
argument_list|,
name|mergeExistingPermissions
argument_list|)
expr_stmt|;
block|}
comment|/**    * Grants permission on the specified namespace for the specified user.    * If permissions on the specified namespace exists, later granted permissions will override previous granted    * permissions.    * @param connection The Connection instance to use    * @param namespace    * @param userName    * @param actions    * @throws Throwable    */
specifier|public
specifier|static
name|void
name|grant
parameter_list|(
name|Connection
name|connection
parameter_list|,
specifier|final
name|String
name|namespace
parameter_list|,
specifier|final
name|String
name|userName
parameter_list|,
specifier|final
name|Permission
operator|.
name|Action
modifier|...
name|actions
parameter_list|)
throws|throws
name|Throwable
block|{
name|grant
argument_list|(
name|connection
argument_list|,
name|namespace
argument_list|,
name|userName
argument_list|,
literal|true
argument_list|,
name|actions
argument_list|)
expr_stmt|;
block|}
comment|/**    * Grant global permissions for the specified user.    * @param connection    * @param userName    * @param mergeExistingPermissions If set to false, later granted permissions will override    *          previous granted permissions. otherwise, it'll merge with previous granted    *          permissions.    * @param actions    * @throws Throwable    */
specifier|private
specifier|static
name|void
name|grant
parameter_list|(
name|Connection
name|connection
parameter_list|,
specifier|final
name|String
name|userName
parameter_list|,
name|boolean
name|mergeExistingPermissions
parameter_list|,
specifier|final
name|Permission
operator|.
name|Action
modifier|...
name|actions
parameter_list|)
throws|throws
name|Throwable
block|{
name|connection
operator|.
name|getAdmin
argument_list|()
operator|.
name|grant
argument_list|(
name|userName
argument_list|,
operator|new
name|GlobalPermission
argument_list|(
name|actions
argument_list|)
argument_list|,
name|mergeExistingPermissions
argument_list|)
expr_stmt|;
block|}
comment|/**    * Grant global permissions for the specified user.    * If permissions for the specified user exists, later granted permissions will override previous granted    * permissions.    * @param connection    * @param userName    * @param actions    * @throws Throwable    */
specifier|public
specifier|static
name|void
name|grant
parameter_list|(
name|Connection
name|connection
parameter_list|,
specifier|final
name|String
name|userName
parameter_list|,
specifier|final
name|Permission
operator|.
name|Action
modifier|...
name|actions
parameter_list|)
throws|throws
name|Throwable
block|{
name|grant
argument_list|(
name|connection
argument_list|,
name|userName
argument_list|,
literal|true
argument_list|,
name|actions
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|boolean
name|isAccessControllerRunning
parameter_list|(
name|Connection
name|connection
parameter_list|)
throws|throws
name|MasterNotRunningException
throws|,
name|ZooKeeperConnectionException
throws|,
name|IOException
block|{
try|try
init|(
name|Admin
name|admin
init|=
name|connection
operator|.
name|getAdmin
argument_list|()
init|)
block|{
return|return
name|admin
operator|.
name|isTableAvailable
argument_list|(
name|ACL_TABLE_NAME
argument_list|)
return|;
block|}
block|}
comment|/**    * Revokes the permission on the table    * @param connection The Connection instance to use    * @param tableName    * @param username    * @param family    * @param qualifier    * @param actions    * @throws Throwable    */
specifier|public
specifier|static
name|void
name|revoke
parameter_list|(
name|Connection
name|connection
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|String
name|username
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|Permission
operator|.
name|Action
modifier|...
name|actions
parameter_list|)
throws|throws
name|Throwable
block|{
name|connection
operator|.
name|getAdmin
argument_list|()
operator|.
name|revoke
argument_list|(
name|username
argument_list|,
operator|new
name|TablePermission
argument_list|(
name|tableName
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|actions
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Revokes the permission on the namespace for the specified user.    * @param connection The Connection instance to use    * @param namespace    * @param userName    * @param actions    * @throws Throwable    */
specifier|public
specifier|static
name|void
name|revoke
parameter_list|(
name|Connection
name|connection
parameter_list|,
specifier|final
name|String
name|namespace
parameter_list|,
specifier|final
name|String
name|userName
parameter_list|,
specifier|final
name|Permission
operator|.
name|Action
modifier|...
name|actions
parameter_list|)
throws|throws
name|Throwable
block|{
name|connection
operator|.
name|getAdmin
argument_list|()
operator|.
name|revoke
argument_list|(
name|userName
argument_list|,
operator|new
name|NamespacePermission
argument_list|(
name|namespace
argument_list|,
name|actions
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Revoke global permissions for the specified user.    * @param connection The Connection instance to use    */
specifier|public
specifier|static
name|void
name|revoke
parameter_list|(
name|Connection
name|connection
parameter_list|,
specifier|final
name|String
name|userName
parameter_list|,
specifier|final
name|Permission
operator|.
name|Action
modifier|...
name|actions
parameter_list|)
throws|throws
name|Throwable
block|{
name|connection
operator|.
name|getAdmin
argument_list|()
operator|.
name|revoke
argument_list|(
name|userName
argument_list|,
operator|new
name|GlobalPermission
argument_list|(
name|actions
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * List all the userPermissions matching the given pattern. If pattern is null, the behavior is    * dependent on whether user has global admin privileges or not. If yes, the global permissions    * along with the list of superusers would be returned. Else, no rows get returned.    * @param connection The Connection instance to use    * @param tableRegex The regular expression string to match against    * @return List of UserPermissions    * @throws Throwable    */
specifier|public
specifier|static
name|List
argument_list|<
name|UserPermission
argument_list|>
name|getUserPermissions
parameter_list|(
name|Connection
name|connection
parameter_list|,
name|String
name|tableRegex
parameter_list|)
throws|throws
name|Throwable
block|{
return|return
name|getUserPermissions
argument_list|(
name|connection
argument_list|,
name|tableRegex
argument_list|,
name|HConstants
operator|.
name|EMPTY_STRING
argument_list|)
return|;
block|}
comment|/**    * List all the userPermissions matching the given table pattern and user name.    * @param connection Connection    * @param tableRegex The regular expression string to match against    * @param userName User name, if empty then all user permissions will be retrieved.    * @return List of UserPermissions    * @throws Throwable on failure    */
specifier|public
specifier|static
name|List
argument_list|<
name|UserPermission
argument_list|>
name|getUserPermissions
parameter_list|(
name|Connection
name|connection
parameter_list|,
name|String
name|tableRegex
parameter_list|,
name|String
name|userName
parameter_list|)
throws|throws
name|Throwable
block|{
comment|/**      * TODO: Pass an rpcController HBaseRpcController controller = ((ClusterConnection)      * connection).getRpcControllerFactory().newController();      */
name|List
argument_list|<
name|UserPermission
argument_list|>
name|permList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|ACL_TABLE_NAME
argument_list|)
init|)
block|{
try|try
init|(
name|Admin
name|admin
init|=
name|connection
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|CoprocessorRpcChannel
name|service
init|=
name|table
operator|.
name|coprocessorService
argument_list|(
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
name|BlockingInterface
name|protocol
init|=
name|AccessControlProtos
operator|.
name|AccessControlService
operator|.
name|newBlockingStub
argument_list|(
name|service
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|TableDescriptor
argument_list|>
name|htds
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|tableRegex
operator|==
literal|null
operator|||
name|tableRegex
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|permList
operator|=
name|AccessControlUtil
operator|.
name|getUserPermissions
argument_list|(
literal|null
argument_list|,
name|protocol
argument_list|,
name|userName
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|tableRegex
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|==
literal|'@'
condition|)
block|{
comment|// Namespaces
name|String
name|namespaceRegex
init|=
name|tableRegex
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
decl_stmt|;
for|for
control|(
name|NamespaceDescriptor
name|nsds
range|:
name|admin
operator|.
name|listNamespaceDescriptors
argument_list|()
control|)
block|{
comment|// Read out all
comment|// namespaces
name|String
name|namespace
init|=
name|nsds
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|namespace
operator|.
name|matches
argument_list|(
name|namespaceRegex
argument_list|)
condition|)
block|{
comment|// Match the given namespace regex?
name|permList
operator|.
name|addAll
argument_list|(
name|AccessControlUtil
operator|.
name|getUserPermissions
argument_list|(
literal|null
argument_list|,
name|protocol
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|namespace
argument_list|)
argument_list|,
name|userName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
comment|// Tables
name|htds
operator|=
name|admin
operator|.
name|listTableDescriptors
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
name|tableRegex
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|TableDescriptor
name|htd
range|:
name|htds
control|)
block|{
name|permList
operator|.
name|addAll
argument_list|(
name|AccessControlUtil
operator|.
name|getUserPermissions
argument_list|(
literal|null
argument_list|,
name|protocol
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|userName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|permList
return|;
block|}
comment|/**    * List all the userPermissions matching the given table pattern and column family.    * @param connection Connection    * @param tableRegex The regular expression string to match against. It shouldn't be null, empty    *          or a namespace regular expression.    * @param columnFamily Column family    * @return List of UserPermissions    * @throws Throwable on failure    */
specifier|public
specifier|static
name|List
argument_list|<
name|UserPermission
argument_list|>
name|getUserPermissions
parameter_list|(
name|Connection
name|connection
parameter_list|,
name|String
name|tableRegex
parameter_list|,
name|byte
index|[]
name|columnFamily
parameter_list|)
throws|throws
name|Throwable
block|{
return|return
name|getUserPermissions
argument_list|(
name|connection
argument_list|,
name|tableRegex
argument_list|,
name|columnFamily
argument_list|,
literal|null
argument_list|,
name|HConstants
operator|.
name|EMPTY_STRING
argument_list|)
return|;
block|}
comment|/**    * List all the userPermissions matching the given table pattern, column family and user name.    * @param connection Connection    * @param tableRegex The regular expression string to match against. It shouldn't be null, empty    *          or a namespace regular expression.    * @param columnFamily Column family    * @param userName User name, if empty then all user permissions will be retrieved.    * @return List of UserPermissions    * @throws Throwable on failure    */
specifier|public
specifier|static
name|List
argument_list|<
name|UserPermission
argument_list|>
name|getUserPermissions
parameter_list|(
name|Connection
name|connection
parameter_list|,
name|String
name|tableRegex
parameter_list|,
name|byte
index|[]
name|columnFamily
parameter_list|,
name|String
name|userName
parameter_list|)
throws|throws
name|Throwable
block|{
return|return
name|getUserPermissions
argument_list|(
name|connection
argument_list|,
name|tableRegex
argument_list|,
name|columnFamily
argument_list|,
literal|null
argument_list|,
name|userName
argument_list|)
return|;
block|}
comment|/**    * List all the userPermissions matching the given table pattern, column family and column    * qualifier.    * @param connection Connection    * @param tableRegex The regular expression string to match against. It shouldn't be null, empty    *          or a namespace regular expression.    * @param columnFamily Column family    * @param columnQualifier Column qualifier    * @return List of UserPermissions    * @throws Throwable on failure    */
specifier|public
specifier|static
name|List
argument_list|<
name|UserPermission
argument_list|>
name|getUserPermissions
parameter_list|(
name|Connection
name|connection
parameter_list|,
name|String
name|tableRegex
parameter_list|,
name|byte
index|[]
name|columnFamily
parameter_list|,
name|byte
index|[]
name|columnQualifier
parameter_list|)
throws|throws
name|Throwable
block|{
return|return
name|getUserPermissions
argument_list|(
name|connection
argument_list|,
name|tableRegex
argument_list|,
name|columnFamily
argument_list|,
name|columnQualifier
argument_list|,
name|HConstants
operator|.
name|EMPTY_STRING
argument_list|)
return|;
block|}
comment|/**    * List all the userPermissions matching the given table pattern, column family and column    * qualifier.    * @param connection Connection    * @param tableRegex The regular expression string to match against. It shouldn't be null, empty    *          or a namespace regular expression.    * @param columnFamily Column family    * @param columnQualifier Column qualifier    * @param userName User name, if empty then all user permissions will be retrieved.    * @return List of UserPermissions    * @throws Throwable on failure    */
specifier|public
specifier|static
name|List
argument_list|<
name|UserPermission
argument_list|>
name|getUserPermissions
parameter_list|(
name|Connection
name|connection
parameter_list|,
name|String
name|tableRegex
parameter_list|,
name|byte
index|[]
name|columnFamily
parameter_list|,
name|byte
index|[]
name|columnQualifier
parameter_list|,
name|String
name|userName
parameter_list|)
throws|throws
name|Throwable
block|{
if|if
condition|(
name|tableRegex
operator|==
literal|null
operator|||
name|tableRegex
operator|.
name|isEmpty
argument_list|()
operator|||
name|tableRegex
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|==
literal|'@'
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Table name can't be null or empty or a namespace."
argument_list|)
throw|;
block|}
comment|/**      * TODO: Pass an rpcController HBaseRpcController controller = ((ClusterConnection)      * connection).getRpcControllerFactory().newController();      */
name|List
argument_list|<
name|UserPermission
argument_list|>
name|permList
init|=
operator|new
name|ArrayList
argument_list|<
name|UserPermission
argument_list|>
argument_list|()
decl_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|ACL_TABLE_NAME
argument_list|)
init|)
block|{
try|try
init|(
name|Admin
name|admin
init|=
name|connection
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|CoprocessorRpcChannel
name|service
init|=
name|table
operator|.
name|coprocessorService
argument_list|(
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
name|BlockingInterface
name|protocol
init|=
name|AccessControlProtos
operator|.
name|AccessControlService
operator|.
name|newBlockingStub
argument_list|(
name|service
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|TableDescriptor
argument_list|>
name|htds
init|=
name|admin
operator|.
name|listTableDescriptors
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
name|tableRegex
argument_list|)
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|// Retrieve table permissions
for|for
control|(
name|TableDescriptor
name|htd
range|:
name|htds
control|)
block|{
name|permList
operator|.
name|addAll
argument_list|(
name|AccessControlUtil
operator|.
name|getUserPermissions
argument_list|(
literal|null
argument_list|,
name|protocol
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|columnFamily
argument_list|,
name|columnQualifier
argument_list|,
name|userName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|permList
return|;
block|}
comment|/**    * Validates whether specified user has permission to perform actions on the mentioned table,    * column family or column qualifier.    * @param connection Connection    * @param tableName Table name, it shouldn't be null or empty.    * @param columnFamily The column family. Optional argument, can be empty. If empty then    *          validation will happen at table level.    * @param columnQualifier The column qualifier. Optional argument, can be empty. If empty then    *          validation will happen at table and column family level. columnQualifier will not be    *          considered if columnFamily is passed as null or empty.    * @param userName User name, it shouldn't be null or empty.    * @param actions Actions    * @return true if access allowed to the specified user, otherwise false.    * @throws Throwable on failure    */
specifier|public
specifier|static
name|boolean
name|hasPermission
parameter_list|(
name|Connection
name|connection
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|columnFamily
parameter_list|,
name|String
name|columnQualifier
parameter_list|,
name|String
name|userName
parameter_list|,
name|Permission
operator|.
name|Action
modifier|...
name|actions
parameter_list|)
throws|throws
name|Throwable
block|{
return|return
name|hasPermission
argument_list|(
name|connection
argument_list|,
name|tableName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|columnFamily
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|columnQualifier
argument_list|)
argument_list|,
name|userName
argument_list|,
name|actions
argument_list|)
return|;
block|}
comment|/**    * Validates whether specified user has permission to perform actions on the mentioned table,    * column family or column qualifier.    * @param connection Connection    * @param tableName Table name, it shouldn't be null or empty.    * @param columnFamily The column family. Optional argument, can be empty. If empty then    *          validation will happen at table level.    * @param columnQualifier The column qualifier. Optional argument, can be empty. If empty then    *          validation will happen at table and column family level. columnQualifier will not be    *          considered if columnFamily is passed as null or empty.    * @param userName User name, it shouldn't be null or empty.    * @param actions Actions    * @return true if access allowed to the specified user, otherwise false.    * @throws Throwable on failure    */
specifier|public
specifier|static
name|boolean
name|hasPermission
parameter_list|(
name|Connection
name|connection
parameter_list|,
name|String
name|tableName
parameter_list|,
name|byte
index|[]
name|columnFamily
parameter_list|,
name|byte
index|[]
name|columnQualifier
parameter_list|,
name|String
name|userName
parameter_list|,
name|Permission
operator|.
name|Action
modifier|...
name|actions
parameter_list|)
throws|throws
name|Throwable
block|{
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|tableName
argument_list|)
operator|||
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|userName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Table and user name can't be null or empty."
argument_list|)
throw|;
block|}
name|boolean
name|hasPermission
init|=
literal|false
decl_stmt|;
comment|/**      * todo: pass an rpccontroller hbaserpccontroller controller = ((clusterconnection)      * connection).getrpccontrollerfactory().newcontroller();      */
try|try
init|(
name|Table
name|table
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|ACL_TABLE_NAME
argument_list|)
init|)
block|{
name|CoprocessorRpcChannel
name|service
init|=
name|table
operator|.
name|coprocessorService
argument_list|(
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
name|BlockingInterface
name|protocol
init|=
name|AccessControlProtos
operator|.
name|AccessControlService
operator|.
name|newBlockingStub
argument_list|(
name|service
argument_list|)
decl_stmt|;
comment|// Check whether user has permission
name|hasPermission
operator|=
name|AccessControlUtil
operator|.
name|hasPermission
argument_list|(
literal|null
argument_list|,
name|protocol
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|columnFamily
argument_list|,
name|columnQualifier
argument_list|,
name|userName
argument_list|,
name|actions
argument_list|)
expr_stmt|;
block|}
return|return
name|hasPermission
return|;
block|}
block|}
end_class

end_unit

