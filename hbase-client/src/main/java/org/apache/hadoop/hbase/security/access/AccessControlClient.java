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
name|Map
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
name|HBaseZeroCopyByteString
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
name|classification
operator|.
name|InterfaceStability
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
name|client
operator|.
name|HTable
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
name|coprocessor
operator|.
name|Batch
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
name|BlockingRpcCallback
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
name|ServerRpcController
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
name|protobuf
operator|.
name|generated
operator|.
name|AccessControlProtos
operator|.
name|GrantResponse
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
name|protobuf
operator|.
name|generated
operator|.
name|AccessControlProtos
operator|.
name|RevokeResponse
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
name|ByteString
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
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|AccessControlClient
block|{
comment|/**    * Grants permission on the specified table for the specified user    * @param conf    * @param tableName    * @param userName    * @param family    * @param qual    * @param actions    * @return GrantResponse    * @throws Throwable    */
specifier|public
specifier|static
name|GrantResponse
name|grant
parameter_list|(
name|Configuration
name|conf
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
name|AccessControlProtos
operator|.
name|Permission
operator|.
name|Action
modifier|...
name|actions
parameter_list|)
throws|throws
name|Throwable
block|{
name|HTable
name|ht
init|=
literal|null
decl_stmt|;
try|try
block|{
name|TableName
name|aclTableName
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
name|ht
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|aclTableName
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Batch
operator|.
name|Call
argument_list|<
name|AccessControlService
argument_list|,
name|GrantResponse
argument_list|>
name|callable
init|=
operator|new
name|Batch
operator|.
name|Call
argument_list|<
name|AccessControlService
argument_list|,
name|GrantResponse
argument_list|>
argument_list|()
block|{
name|ServerRpcController
name|controller
init|=
operator|new
name|ServerRpcController
argument_list|()
decl_stmt|;
name|BlockingRpcCallback
argument_list|<
name|GrantResponse
argument_list|>
name|rpcCallback
init|=
operator|new
name|BlockingRpcCallback
argument_list|<
name|GrantResponse
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|GrantResponse
name|call
parameter_list|(
name|AccessControlService
name|service
parameter_list|)
throws|throws
name|IOException
block|{
name|GrantRequest
operator|.
name|Builder
name|builder
init|=
name|GrantRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
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
name|AccessControlProtos
operator|.
name|TablePermission
operator|.
name|Builder
name|permissionBuilder
init|=
name|AccessControlProtos
operator|.
name|TablePermission
operator|.
name|newBuilder
argument_list|()
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
name|actions
control|)
block|{
name|permissionBuilder
operator|.
name|addAction
argument_list|(
name|a
argument_list|)
expr_stmt|;
block|}
name|permissionBuilder
operator|.
name|setTableName
argument_list|(
name|ProtobufUtil
operator|.
name|toProtoTableName
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|family
operator|!=
literal|null
condition|)
block|{
name|permissionBuilder
operator|.
name|setFamily
argument_list|(
name|HBaseZeroCopyByteString
operator|.
name|wrap
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|qual
operator|!=
literal|null
condition|)
block|{
name|permissionBuilder
operator|.
name|setQualifier
argument_list|(
name|HBaseZeroCopyByteString
operator|.
name|wrap
argument_list|(
name|qual
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
operator|.
name|setTablePermission
argument_list|(
name|permissionBuilder
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setUserPermission
argument_list|(
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
name|userName
argument_list|)
argument_list|)
operator|.
name|setPermission
argument_list|(
name|ret
argument_list|)
argument_list|)
expr_stmt|;
name|service
operator|.
name|grant
argument_list|(
name|controller
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|rpcCallback
argument_list|)
expr_stmt|;
return|return
name|rpcCallback
operator|.
name|get
argument_list|()
return|;
block|}
block|}
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|GrantResponse
argument_list|>
name|result
init|=
name|ht
operator|.
name|coprocessorService
argument_list|(
name|AccessControlService
operator|.
name|class
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|callable
argument_list|)
decl_stmt|;
return|return
name|result
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
return|;
comment|// There will be exactly one
comment|// region for labels
comment|// table and so one entry in
comment|// result Map.
block|}
finally|finally
block|{
if|if
condition|(
name|ht
operator|!=
literal|null
condition|)
block|{
name|ht
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Revokes the permission on the table    * @param conf    * @param username    * @param tableName    * @param family    * @param qualifier    * @param actions    * @return RevokeResponse    * @throws Throwable    */
specifier|public
specifier|static
name|RevokeResponse
name|revoke
parameter_list|(
name|Configuration
name|conf
parameter_list|,
specifier|final
name|String
name|username
parameter_list|,
specifier|final
name|TableName
name|tableName
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
name|AccessControlProtos
operator|.
name|Permission
operator|.
name|Action
modifier|...
name|actions
parameter_list|)
throws|throws
name|Throwable
block|{
name|HTable
name|ht
init|=
literal|null
decl_stmt|;
try|try
block|{
name|TableName
name|aclTableName
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
name|ht
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|aclTableName
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Batch
operator|.
name|Call
argument_list|<
name|AccessControlService
argument_list|,
name|AccessControlProtos
operator|.
name|RevokeResponse
argument_list|>
name|callable
init|=
operator|new
name|Batch
operator|.
name|Call
argument_list|<
name|AccessControlService
argument_list|,
name|AccessControlProtos
operator|.
name|RevokeResponse
argument_list|>
argument_list|()
block|{
name|ServerRpcController
name|controller
init|=
operator|new
name|ServerRpcController
argument_list|()
decl_stmt|;
name|BlockingRpcCallback
argument_list|<
name|AccessControlProtos
operator|.
name|RevokeResponse
argument_list|>
name|rpcCallback
init|=
operator|new
name|BlockingRpcCallback
argument_list|<
name|AccessControlProtos
operator|.
name|RevokeResponse
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|RevokeResponse
name|call
parameter_list|(
name|AccessControlService
name|service
parameter_list|)
throws|throws
name|IOException
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
name|AccessControlProtos
operator|.
name|TablePermission
operator|.
name|Builder
name|permissionBuilder
init|=
name|AccessControlProtos
operator|.
name|TablePermission
operator|.
name|newBuilder
argument_list|()
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
name|actions
control|)
block|{
name|permissionBuilder
operator|.
name|addAction
argument_list|(
name|a
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|tableName
operator|!=
literal|null
condition|)
block|{
name|permissionBuilder
operator|.
name|setTableName
argument_list|(
name|ProtobufUtil
operator|.
name|toProtoTableName
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|family
operator|!=
literal|null
condition|)
block|{
name|permissionBuilder
operator|.
name|setFamily
argument_list|(
name|HBaseZeroCopyByteString
operator|.
name|wrap
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|qualifier
operator|!=
literal|null
condition|)
block|{
name|permissionBuilder
operator|.
name|setQualifier
argument_list|(
name|HBaseZeroCopyByteString
operator|.
name|wrap
argument_list|(
name|qualifier
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
operator|.
name|setTablePermission
argument_list|(
name|permissionBuilder
argument_list|)
expr_stmt|;
name|RevokeRequest
name|builder
init|=
name|AccessControlProtos
operator|.
name|RevokeRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setUserPermission
argument_list|(
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
name|username
argument_list|)
argument_list|)
operator|.
name|setPermission
argument_list|(
name|ret
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|service
operator|.
name|revoke
argument_list|(
name|controller
argument_list|,
name|builder
argument_list|,
name|rpcCallback
argument_list|)
expr_stmt|;
return|return
name|rpcCallback
operator|.
name|get
argument_list|()
return|;
block|}
block|}
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|RevokeResponse
argument_list|>
name|result
init|=
name|ht
operator|.
name|coprocessorService
argument_list|(
name|AccessControlService
operator|.
name|class
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|callable
argument_list|)
decl_stmt|;
return|return
name|result
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|ht
operator|!=
literal|null
condition|)
block|{
name|ht
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

