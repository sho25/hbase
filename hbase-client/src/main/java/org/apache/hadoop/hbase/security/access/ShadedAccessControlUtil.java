begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|security
operator|.
name|access
operator|.
name|Permission
operator|.
name|Action
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
name|ArrayListMultimap
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
name|ListMultimap
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
name|protobuf
operator|.
name|ByteString
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
name|shaded
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|AccessControlProtos
operator|.
name|GrantRequest
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|AccessControlProtos
operator|.
name|RevokeRequest
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
import|;
end_import

begin_comment
comment|/**  * Convert protobuf objects in AccessControl.proto under hbase-protocol-shaded to user-oriented  * objects and vice versa.<br>  *  * In HBASE-15638, we create a hbase-protocol-shaded module for upgrading protobuf version to 3.x,  * but there are still some coprocessor endpoints(such as AccessControl, Authentication,  * MulitRowMutation) which depend on hbase-protocol module for CPEP compatibility. In fact, we use  * PB objects in AccessControl.proto under hbase-protocol for access control logic and use shaded  * AccessControl.proto only for serializing/deserializing permissions of .snapshotinfo.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ShadedAccessControlUtil
block|{
comment|/**    * Convert a client user permission to a user permission shaded proto.    */
specifier|public
specifier|static
name|AccessControlProtos
operator|.
name|Permission
operator|.
name|Action
name|toPermissionAction
parameter_list|(
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
switch|switch
condition|(
name|action
condition|)
block|{
case|case
name|READ
case|:
return|return
name|AccessControlProtos
operator|.
name|Permission
operator|.
name|Action
operator|.
name|READ
return|;
case|case
name|WRITE
case|:
return|return
name|AccessControlProtos
operator|.
name|Permission
operator|.
name|Action
operator|.
name|WRITE
return|;
case|case
name|EXEC
case|:
return|return
name|AccessControlProtos
operator|.
name|Permission
operator|.
name|Action
operator|.
name|EXEC
return|;
case|case
name|CREATE
case|:
return|return
name|AccessControlProtos
operator|.
name|Permission
operator|.
name|Action
operator|.
name|CREATE
return|;
case|case
name|ADMIN
case|:
return|return
name|AccessControlProtos
operator|.
name|Permission
operator|.
name|Action
operator|.
name|ADMIN
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown action value "
operator|+
name|action
operator|.
name|name
argument_list|()
argument_list|)
throw|;
block|}
comment|/**    * Convert a Permission.Action shaded proto to a client Permission.Action object.    */
specifier|public
specifier|static
name|Permission
operator|.
name|Action
name|toPermissionAction
parameter_list|(
name|AccessControlProtos
operator|.
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
switch|switch
condition|(
name|action
condition|)
block|{
case|case
name|READ
case|:
return|return
name|Permission
operator|.
name|Action
operator|.
name|READ
return|;
case|case
name|WRITE
case|:
return|return
name|Permission
operator|.
name|Action
operator|.
name|WRITE
return|;
case|case
name|EXEC
case|:
return|return
name|Permission
operator|.
name|Action
operator|.
name|EXEC
return|;
case|case
name|CREATE
case|:
return|return
name|Permission
operator|.
name|Action
operator|.
name|CREATE
return|;
case|case
name|ADMIN
case|:
return|return
name|Permission
operator|.
name|Action
operator|.
name|ADMIN
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown action value "
operator|+
name|action
operator|.
name|name
argument_list|()
argument_list|)
throw|;
block|}
comment|/**    * Converts a list of Permission.Action shaded proto to a list of client Permission.Action    * objects.    * @param protoActions the list of shaded protobuf Actions    * @return the converted list of Actions    */
specifier|public
specifier|static
name|List
argument_list|<
name|Permission
operator|.
name|Action
argument_list|>
name|toPermissionActions
parameter_list|(
name|List
argument_list|<
name|AccessControlProtos
operator|.
name|Permission
operator|.
name|Action
argument_list|>
name|protoActions
parameter_list|)
block|{
name|List
argument_list|<
name|Permission
operator|.
name|Action
argument_list|>
name|actions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|protoActions
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|AccessControlProtos
operator|.
name|Permission
operator|.
name|Action
name|a
range|:
name|protoActions
control|)
block|{
name|actions
operator|.
name|add
argument_list|(
name|toPermissionAction
argument_list|(
name|a
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|actions
return|;
block|}
specifier|public
specifier|static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|TableName
name|toTableName
parameter_list|(
name|HBaseProtos
operator|.
name|TableName
name|tableNamePB
parameter_list|)
block|{
return|return
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableNamePB
operator|.
name|getNamespace
argument_list|()
operator|.
name|asReadOnlyByteBuffer
argument_list|()
argument_list|,
name|tableNamePB
operator|.
name|getQualifier
argument_list|()
operator|.
name|asReadOnlyByteBuffer
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|HBaseProtos
operator|.
name|TableName
name|toProtoTableName
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
name|HBaseProtos
operator|.
name|TableName
operator|.
name|newBuilder
argument_list|()
operator|.
name|setNamespace
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|tableName
operator|.
name|getNamespace
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setQualifier
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|tableName
operator|.
name|getQualifier
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Converts a Permission shaded proto to a client TablePermission object.    * @param proto the protobuf Permission    * @return the converted TablePermission    */
specifier|public
specifier|static
name|Permission
name|toPermission
parameter_list|(
name|AccessControlProtos
operator|.
name|Permission
name|proto
parameter_list|)
block|{
if|if
condition|(
name|proto
operator|.
name|getType
argument_list|()
operator|==
name|AccessControlProtos
operator|.
name|Permission
operator|.
name|Type
operator|.
name|Global
condition|)
block|{
name|AccessControlProtos
operator|.
name|GlobalPermission
name|perm
init|=
name|proto
operator|.
name|getGlobalPermission
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Action
argument_list|>
name|actions
init|=
name|toPermissionActions
argument_list|(
name|perm
operator|.
name|getActionList
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|GlobalPermission
argument_list|(
name|actions
operator|.
name|toArray
argument_list|(
operator|new
name|Permission
operator|.
name|Action
index|[
name|actions
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|proto
operator|.
name|getType
argument_list|()
operator|==
name|AccessControlProtos
operator|.
name|Permission
operator|.
name|Type
operator|.
name|Namespace
condition|)
block|{
name|AccessControlProtos
operator|.
name|NamespacePermission
name|perm
init|=
name|proto
operator|.
name|getNamespacePermission
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Permission
operator|.
name|Action
argument_list|>
name|actions
init|=
name|toPermissionActions
argument_list|(
name|perm
operator|.
name|getActionList
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|proto
operator|.
name|hasNamespacePermission
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Namespace must not be empty in NamespacePermission"
argument_list|)
throw|;
block|}
name|String
name|ns
init|=
name|perm
operator|.
name|getNamespaceName
argument_list|()
operator|.
name|toStringUtf8
argument_list|()
decl_stmt|;
return|return
operator|new
name|NamespacePermission
argument_list|(
name|ns
argument_list|,
name|actions
operator|.
name|toArray
argument_list|(
operator|new
name|Permission
operator|.
name|Action
index|[
name|actions
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|proto
operator|.
name|getType
argument_list|()
operator|==
name|AccessControlProtos
operator|.
name|Permission
operator|.
name|Type
operator|.
name|Table
condition|)
block|{
name|AccessControlProtos
operator|.
name|TablePermission
name|perm
init|=
name|proto
operator|.
name|getTablePermission
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Permission
operator|.
name|Action
argument_list|>
name|actions
init|=
name|toPermissionActions
argument_list|(
name|perm
operator|.
name|getActionList
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
literal|null
decl_stmt|;
name|byte
index|[]
name|family
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|perm
operator|.
name|hasTableName
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"TableName cannot be empty"
argument_list|)
throw|;
block|}
name|TableName
name|table
init|=
name|toTableName
argument_list|(
name|perm
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|perm
operator|.
name|hasFamily
argument_list|()
condition|)
name|family
operator|=
name|perm
operator|.
name|getFamily
argument_list|()
operator|.
name|toByteArray
argument_list|()
expr_stmt|;
if|if
condition|(
name|perm
operator|.
name|hasQualifier
argument_list|()
condition|)
name|qualifier
operator|=
name|perm
operator|.
name|getQualifier
argument_list|()
operator|.
name|toByteArray
argument_list|()
expr_stmt|;
return|return
operator|new
name|TablePermission
argument_list|(
name|table
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|actions
operator|.
name|toArray
argument_list|(
operator|new
name|Permission
operator|.
name|Action
index|[
name|actions
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
return|;
block|}
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unrecognize Perm Type: "
operator|+
name|proto
operator|.
name|getType
argument_list|()
argument_list|)
throw|;
block|}
comment|/**    * Convert a client Permission to a Permission shaded proto    * @param perm the client Permission    * @return the protobuf Permission    */
specifier|public
specifier|static
name|AccessControlProtos
operator|.
name|Permission
name|toPermission
parameter_list|(
name|Permission
name|perm
parameter_list|)
block|{
name|AccessControlProtos
operator|.
name|Permission
operator|.
name|Builder
name|ret
init|=
name|AccessControlProtos
operator|.
name|Permission
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|perm
operator|instanceof
name|NamespacePermission
condition|)
block|{
name|NamespacePermission
name|nsPerm
init|=
operator|(
name|NamespacePermission
operator|)
name|perm
decl_stmt|;
name|ret
operator|.
name|setType
argument_list|(
name|AccessControlProtos
operator|.
name|Permission
operator|.
name|Type
operator|.
name|Namespace
argument_list|)
expr_stmt|;
name|AccessControlProtos
operator|.
name|NamespacePermission
operator|.
name|Builder
name|builder
init|=
name|AccessControlProtos
operator|.
name|NamespacePermission
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setNamespaceName
argument_list|(
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
name|protobuf
operator|.
name|ByteString
operator|.
name|copyFromUtf8
argument_list|(
name|nsPerm
operator|.
name|getNamespace
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Permission
operator|.
name|Action
index|[]
name|actions
init|=
name|perm
operator|.
name|getActions
argument_list|()
decl_stmt|;
if|if
condition|(
name|actions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Permission
operator|.
name|Action
name|a
range|:
name|actions
control|)
block|{
name|builder
operator|.
name|addAction
argument_list|(
name|toPermissionAction
argument_list|(
name|a
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|ret
operator|.
name|setNamespacePermission
argument_list|(
name|builder
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|perm
operator|instanceof
name|TablePermission
condition|)
block|{
name|TablePermission
name|tablePerm
init|=
operator|(
name|TablePermission
operator|)
name|perm
decl_stmt|;
name|ret
operator|.
name|setType
argument_list|(
name|AccessControlProtos
operator|.
name|Permission
operator|.
name|Type
operator|.
name|Table
argument_list|)
expr_stmt|;
name|AccessControlProtos
operator|.
name|TablePermission
operator|.
name|Builder
name|builder
init|=
name|AccessControlProtos
operator|.
name|TablePermission
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setTableName
argument_list|(
name|toProtoTableName
argument_list|(
name|tablePerm
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|tablePerm
operator|.
name|hasFamily
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setFamily
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|tablePerm
operator|.
name|getFamily
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|tablePerm
operator|.
name|hasQualifier
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setQualifier
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|tablePerm
operator|.
name|getQualifier
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Permission
operator|.
name|Action
index|[]
name|actions
init|=
name|perm
operator|.
name|getActions
argument_list|()
decl_stmt|;
if|if
condition|(
name|actions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Permission
operator|.
name|Action
name|a
range|:
name|actions
control|)
block|{
name|builder
operator|.
name|addAction
argument_list|(
name|toPermissionAction
argument_list|(
name|a
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|ret
operator|.
name|setTablePermission
argument_list|(
name|builder
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// perm.getAccessScope() == Permission.Scope.GLOBAL
name|ret
operator|.
name|setType
argument_list|(
name|AccessControlProtos
operator|.
name|Permission
operator|.
name|Type
operator|.
name|Global
argument_list|)
expr_stmt|;
name|AccessControlProtos
operator|.
name|GlobalPermission
operator|.
name|Builder
name|builder
init|=
name|AccessControlProtos
operator|.
name|GlobalPermission
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|Permission
operator|.
name|Action
index|[]
name|actions
init|=
name|perm
operator|.
name|getActions
argument_list|()
decl_stmt|;
if|if
condition|(
name|actions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Permission
operator|.
name|Action
name|a
range|:
name|actions
control|)
block|{
name|builder
operator|.
name|addAction
argument_list|(
name|toPermissionAction
argument_list|(
name|a
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|ret
operator|.
name|setGlobalPermission
argument_list|(
name|builder
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Convert a shaded protobuf UserTablePermissions to a ListMultimap&lt;String, TablePermission&gt;    * where key is username.    * @param proto the protobuf UserPermission    * @return the converted UserPermission    */
specifier|public
specifier|static
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|Permission
argument_list|>
name|toUserTablePermissions
parameter_list|(
name|AccessControlProtos
operator|.
name|UsersAndPermissions
name|proto
parameter_list|)
block|{
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|Permission
argument_list|>
name|perms
init|=
name|ArrayListMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
name|AccessControlProtos
operator|.
name|UsersAndPermissions
operator|.
name|UserPermissions
name|userPerm
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|proto
operator|.
name|getUserPermissionsCount
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|userPerm
operator|=
name|proto
operator|.
name|getUserPermissions
argument_list|(
name|i
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|userPerm
operator|.
name|getPermissionsCount
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|Permission
name|perm
init|=
name|toPermission
argument_list|(
name|userPerm
operator|.
name|getPermissions
argument_list|(
name|j
argument_list|)
argument_list|)
decl_stmt|;
name|perms
operator|.
name|put
argument_list|(
name|userPerm
operator|.
name|getUser
argument_list|()
operator|.
name|toStringUtf8
argument_list|()
argument_list|,
name|perm
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|perms
return|;
block|}
comment|/**    * Convert a ListMultimap&lt;String, TablePermission&gt; where key is username to a shaded    * protobuf UserPermission    * @param perm the list of user and table permissions    * @return the protobuf UserTablePermissions    */
specifier|public
specifier|static
name|AccessControlProtos
operator|.
name|UsersAndPermissions
name|toUserTablePermissions
parameter_list|(
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|UserPermission
argument_list|>
name|perm
parameter_list|)
block|{
name|AccessControlProtos
operator|.
name|UsersAndPermissions
operator|.
name|Builder
name|builder
init|=
name|AccessControlProtos
operator|.
name|UsersAndPermissions
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Collection
argument_list|<
name|UserPermission
argument_list|>
argument_list|>
name|entry
range|:
name|perm
operator|.
name|asMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|AccessControlProtos
operator|.
name|UsersAndPermissions
operator|.
name|UserPermissions
operator|.
name|Builder
name|userPermBuilder
init|=
name|AccessControlProtos
operator|.
name|UsersAndPermissions
operator|.
name|UserPermissions
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|userPermBuilder
operator|.
name|setUser
argument_list|(
name|ByteString
operator|.
name|copyFromUtf8
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|UserPermission
name|userPerm
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|userPermBuilder
operator|.
name|addPermissions
argument_list|(
name|toPermission
argument_list|(
name|userPerm
operator|.
name|getPermission
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|addUserPermissions
argument_list|(
name|userPermBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Converts a user permission proto to a client user permission object.    * @param proto the protobuf UserPermission    * @return the converted UserPermission    */
specifier|public
specifier|static
name|UserPermission
name|toUserPermission
parameter_list|(
name|AccessControlProtos
operator|.
name|UserPermission
name|proto
parameter_list|)
block|{
return|return
operator|new
name|UserPermission
argument_list|(
name|proto
operator|.
name|getUser
argument_list|()
operator|.
name|toStringUtf8
argument_list|()
argument_list|,
name|toPermission
argument_list|(
name|proto
operator|.
name|getPermission
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Convert a client user permission to a user permission proto    * @param perm the client UserPermission    * @return the protobuf UserPermission    */
specifier|public
specifier|static
name|AccessControlProtos
operator|.
name|UserPermission
name|toUserPermission
parameter_list|(
name|UserPermission
name|perm
parameter_list|)
block|{
return|return
name|AccessControlProtos
operator|.
name|UserPermission
operator|.
name|newBuilder
argument_list|()
operator|.
name|setUser
argument_list|(
name|ByteString
operator|.
name|copyFromUtf8
argument_list|(
name|perm
operator|.
name|getUser
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setPermission
argument_list|(
name|toPermission
argument_list|(
name|perm
operator|.
name|getPermission
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|GrantRequest
name|buildGrantRequest
parameter_list|(
name|UserPermission
name|userPermission
parameter_list|,
name|boolean
name|mergeExistingPermissions
parameter_list|)
block|{
return|return
name|GrantRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setUserPermission
argument_list|(
name|toUserPermission
argument_list|(
name|userPermission
argument_list|)
argument_list|)
operator|.
name|setMergeExistingPermissions
argument_list|(
name|mergeExistingPermissions
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|RevokeRequest
name|buildRevokeRequest
parameter_list|(
name|UserPermission
name|userPermission
parameter_list|)
block|{
return|return
name|RevokeRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setUserPermission
argument_list|(
name|toUserPermission
argument_list|(
name|userPermission
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

