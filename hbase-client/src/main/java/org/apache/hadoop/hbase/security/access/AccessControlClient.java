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
name|HTableDescriptor
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
name|ConnectionFactory
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
comment|/**    * Grants permission on the specified table for the specified user    * @param conf    * @param tableName    * @param userName    * @param family    * @param qual    * @param actions    * @throws Throwable    */
specifier|public
specifier|static
name|void
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
name|Permission
operator|.
name|Action
modifier|...
name|actions
parameter_list|)
throws|throws
name|Throwable
block|{
comment|// TODO: Make it so caller passes in a Connection rather than have us do this expensive
comment|// setup each time.  This class only used in test and shell at moment though.
try|try
init|(
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|)
block|{
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
name|ProtobufUtil
operator|.
name|grant
argument_list|(
name|getAccessControlServiceStub
argument_list|(
name|table
argument_list|)
argument_list|,
name|userName
argument_list|,
name|tableName
argument_list|,
name|family
argument_list|,
name|qual
argument_list|,
name|actions
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Grants permission on the specified namespace for the specified user.    * @param conf    * @param namespace    * @param userName    * @param actions    * @throws Throwable    */
specifier|public
specifier|static
name|void
name|grant
parameter_list|(
name|Configuration
name|conf
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
comment|// TODO: Make it so caller passes in a Connection rather than have us do this expensive
comment|// setup each time.  This class only used in test and shell at moment though.
try|try
init|(
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|)
block|{
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
name|ProtobufUtil
operator|.
name|grant
argument_list|(
name|getAccessControlServiceStub
argument_list|(
name|table
argument_list|)
argument_list|,
name|userName
argument_list|,
name|namespace
argument_list|,
name|actions
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|boolean
name|isAccessControllerRunning
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|MasterNotRunningException
throws|,
name|ZooKeeperConnectionException
throws|,
name|IOException
block|{
comment|// TODO: Make it so caller passes in a Connection rather than have us do this expensive
comment|// setup each time.  This class only used in test and shell at moment though.
try|try
init|(
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
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
block|}
comment|/**    * Revokes the permission on the table    * @param conf    * @param tableName    * @param username    * @param family    * @param qualifier    * @param actions    * @throws Throwable    */
specifier|public
specifier|static
name|void
name|revoke
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
comment|// TODO: Make it so caller passes in a Connection rather than have us do this expensive
comment|// setup each time.  This class only used in test and shell at moment though.
try|try
init|(
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|)
block|{
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
name|ProtobufUtil
operator|.
name|revoke
argument_list|(
name|getAccessControlServiceStub
argument_list|(
name|table
argument_list|)
argument_list|,
name|username
argument_list|,
name|tableName
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|actions
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Revokes the permission on the table for the specified user.    * @param conf    * @param namespace    * @param userName    * @param actions    * @throws Throwable    */
specifier|public
specifier|static
name|void
name|revoke
parameter_list|(
name|Configuration
name|conf
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
comment|// TODO: Make it so caller passes in a Connection rather than have us do this expensive
comment|// setup each time.  This class only used in test and shell at moment though.
try|try
init|(
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|)
block|{
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
name|ProtobufUtil
operator|.
name|revoke
argument_list|(
name|getAccessControlServiceStub
argument_list|(
name|table
argument_list|)
argument_list|,
name|userName
argument_list|,
name|namespace
argument_list|,
name|actions
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * List all the userPermissions matching the given pattern.    * @param conf    * @param tableRegex The regular expression string to match against    * @return - returns an array of UserPermissions    * @throws Throwable    */
specifier|public
specifier|static
name|List
argument_list|<
name|UserPermission
argument_list|>
name|getUserPermissions
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|tableRegex
parameter_list|)
throws|throws
name|Throwable
block|{
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
comment|// TODO: Make it so caller passes in a Connection rather than have us do this expensive
comment|// setup each time.  This class only used in test and shell at moment though.
try|try
init|(
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|)
block|{
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
name|HTableDescriptor
index|[]
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
name|ProtobufUtil
operator|.
name|getUserPermissions
argument_list|(
name|protocol
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
name|String
name|namespace
init|=
name|tableRegex
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|permList
operator|=
name|ProtobufUtil
operator|.
name|getUserPermissions
argument_list|(
name|protocol
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|namespace
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|htds
operator|=
name|admin
operator|.
name|listTables
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
name|tableRegex
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|HTableDescriptor
name|hd
range|:
name|htds
control|)
block|{
name|permList
operator|.
name|addAll
argument_list|(
name|ProtobufUtil
operator|.
name|getUserPermissions
argument_list|(
name|protocol
argument_list|,
name|hd
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
return|return
name|permList
return|;
block|}
block|}
end_class

end_unit

