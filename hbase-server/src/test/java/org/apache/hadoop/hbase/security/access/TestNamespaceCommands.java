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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
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
name|Coprocessor
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
name|HBaseTestingUtility
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
name|HColumnDescriptor
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
name|MediumTests
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
name|Get
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
name|coprocessor
operator|.
name|CoprocessorHost
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
name|coprocessor
operator|.
name|MasterCoprocessorEnvironment
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
name|coprocessor
operator|.
name|ObserverContext
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
name|MasterCoprocessorHost
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
name|security
operator|.
name|User
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
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|BlockingRpcChannel
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
specifier|public
class|class
name|TestNamespaceCommands
extends|extends
name|SecureTestUtil
block|{
specifier|private
specifier|static
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|String
name|TestNamespace
init|=
literal|"ns1"
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
name|MasterCoprocessorEnvironment
name|CP_ENV
decl_stmt|;
specifier|private
specifier|static
name|AccessController
name|ACCESS_CONTROLLER
decl_stmt|;
comment|//user with all permissions
specifier|private
specifier|static
name|User
name|SUPERUSER
decl_stmt|;
comment|// user with rw permissions
specifier|private
specifier|static
name|User
name|USER_RW
decl_stmt|;
comment|// user with create table permissions alone
specifier|private
specifier|static
name|User
name|USER_CREATE
decl_stmt|;
comment|// user with permission on namespace for testing all operations.
specifier|private
specifier|static
name|User
name|USER_NSP_WRITE
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|SecureTestUtil
operator|.
name|enableSecurity
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|AccessController
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|SUPERUSER
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"admin"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"supergroup"
block|}
argument_list|)
expr_stmt|;
name|USER_RW
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"rw_user"
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|USER_CREATE
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"create_user"
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|USER_NSP_WRITE
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"namespace_write"
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|TestNamespace
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|// Wait for the ACL table to become available
name|UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|AccessControlLists
operator|.
name|ACL_TABLE_NAME
operator|.
name|getName
argument_list|()
argument_list|,
literal|30
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|HTable
name|acl
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|AccessControlLists
operator|.
name|ACL_TABLE_NAME
argument_list|)
decl_stmt|;
name|MasterCoprocessorHost
name|cpHost
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getCoprocessorHost
argument_list|()
decl_stmt|;
name|cpHost
operator|.
name|load
argument_list|(
name|AccessController
operator|.
name|class
argument_list|,
name|Coprocessor
operator|.
name|PRIORITY_HIGHEST
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|ACCESS_CONTROLLER
operator|=
operator|(
name|AccessController
operator|)
name|cpHost
operator|.
name|findCoprocessor
argument_list|(
name|AccessController
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|BlockingRpcChannel
name|service
init|=
name|acl
operator|.
name|coprocessorService
argument_list|(
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
name|AccessControlService
operator|.
name|BlockingInterface
name|protocol
init|=
name|AccessControlService
operator|.
name|newBlockingStub
argument_list|(
name|service
argument_list|)
decl_stmt|;
name|ProtobufUtil
operator|.
name|grant
argument_list|(
name|protocol
argument_list|,
name|USER_NSP_WRITE
operator|.
name|getShortName
argument_list|()
argument_list|,
name|TestNamespace
argument_list|,
name|Action
operator|.
name|WRITE
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|acl
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|deleteNamespace
argument_list|(
name|TestNamespace
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAclTableEntries
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|userTestNamespace
init|=
literal|"userTestNsp"
decl_stmt|;
name|AccessControlService
operator|.
name|BlockingInterface
name|protocol
init|=
literal|null
decl_stmt|;
name|HTable
name|acl
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|AccessControlLists
operator|.
name|ACL_TABLE_NAME
argument_list|)
decl_stmt|;
try|try
block|{
name|BlockingRpcChannel
name|service
init|=
name|acl
operator|.
name|coprocessorService
argument_list|(
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
name|protocol
operator|=
name|AccessControlService
operator|.
name|newBlockingStub
argument_list|(
name|service
argument_list|)
expr_stmt|;
name|ProtobufUtil
operator|.
name|grant
argument_list|(
name|protocol
argument_list|,
name|userTestNamespace
argument_list|,
name|TestNamespace
argument_list|,
name|Permission
operator|.
name|Action
operator|.
name|WRITE
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|acl
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|userTestNamespace
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|result
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
name|perms
init|=
name|AccessControlLists
operator|.
name|getNamespacePermissions
argument_list|(
name|conf
argument_list|,
name|TestNamespace
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|perms
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TablePermission
argument_list|>
name|namespacePerms
init|=
name|perms
operator|.
name|get
argument_list|(
name|userTestNamespace
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|perms
operator|.
name|containsKey
argument_list|(
name|userTestNamespace
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|namespacePerms
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TestNamespace
argument_list|,
name|namespacePerms
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getNamespace
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|namespacePerms
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFamily
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|namespacePerms
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getQualifier
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|namespacePerms
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getActions
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Permission
operator|.
name|Action
operator|.
name|WRITE
argument_list|,
name|namespacePerms
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getActions
argument_list|()
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
comment|// Now revoke and check.
name|ProtobufUtil
operator|.
name|revoke
argument_list|(
name|protocol
argument_list|,
name|userTestNamespace
argument_list|,
name|TestNamespace
argument_list|,
name|Permission
operator|.
name|Action
operator|.
name|WRITE
argument_list|)
expr_stmt|;
name|perms
operator|=
name|AccessControlLists
operator|.
name|getNamespacePermissions
argument_list|(
name|conf
argument_list|,
name|TestNamespace
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|perms
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|acl
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testModifyNamespace
parameter_list|()
throws|throws
name|Exception
block|{
name|PrivilegedExceptionAction
name|modifyNamespace
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|()
block|{
specifier|public
name|Object
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|ACCESS_CONTROLLER
operator|.
name|preModifyNamespace
argument_list|(
name|ObserverContext
operator|.
name|createAndPrepare
argument_list|(
name|CP_ENV
argument_list|,
literal|null
argument_list|)
argument_list|,
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|TestNamespace
argument_list|)
operator|.
name|addConfiguration
argument_list|(
literal|"abc"
argument_list|,
literal|"156"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
comment|// verify that superuser or hbase admin can modify namespaces.
name|verifyAllowed
argument_list|(
name|modifyNamespace
argument_list|,
name|SUPERUSER
argument_list|)
expr_stmt|;
comment|// all others should be denied
name|verifyDenied
argument_list|(
name|modifyNamespace
argument_list|,
name|USER_NSP_WRITE
argument_list|,
name|USER_CREATE
argument_list|,
name|USER_RW
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGrantRevoke
parameter_list|()
throws|throws
name|Exception
block|{
comment|//Only HBase super user should be able to grant and revoke permissions to
comment|// namespaces.
specifier|final
name|String
name|testUser
init|=
literal|"testUser"
decl_stmt|;
name|PrivilegedExceptionAction
name|grantAction
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|()
block|{
specifier|public
name|Object
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|HTable
name|acl
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|AccessControlLists
operator|.
name|ACL_TABLE_NAME
argument_list|)
decl_stmt|;
try|try
block|{
name|BlockingRpcChannel
name|service
init|=
name|acl
operator|.
name|coprocessorService
argument_list|(
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
name|AccessControlService
operator|.
name|BlockingInterface
name|protocol
init|=
name|AccessControlService
operator|.
name|newBlockingStub
argument_list|(
name|service
argument_list|)
decl_stmt|;
name|ProtobufUtil
operator|.
name|grant
argument_list|(
name|protocol
argument_list|,
name|testUser
argument_list|,
name|TestNamespace
argument_list|,
name|Action
operator|.
name|WRITE
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|acl
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|PrivilegedExceptionAction
name|revokeAction
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|()
block|{
specifier|public
name|Object
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|HTable
name|acl
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|AccessControlLists
operator|.
name|ACL_TABLE_NAME
argument_list|)
decl_stmt|;
try|try
block|{
name|BlockingRpcChannel
name|service
init|=
name|acl
operator|.
name|coprocessorService
argument_list|(
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
name|AccessControlService
operator|.
name|BlockingInterface
name|protocol
init|=
name|AccessControlService
operator|.
name|newBlockingStub
argument_list|(
name|service
argument_list|)
decl_stmt|;
name|ProtobufUtil
operator|.
name|revoke
argument_list|(
name|protocol
argument_list|,
name|testUser
argument_list|,
name|TestNamespace
argument_list|,
name|Action
operator|.
name|WRITE
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|acl
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|verifyAllowed
argument_list|(
name|grantAction
argument_list|,
name|SUPERUSER
argument_list|)
expr_stmt|;
name|verifyDenied
argument_list|(
name|grantAction
argument_list|,
name|USER_CREATE
argument_list|,
name|USER_RW
argument_list|)
expr_stmt|;
name|verifyAllowed
argument_list|(
name|revokeAction
argument_list|,
name|SUPERUSER
argument_list|)
expr_stmt|;
name|verifyDenied
argument_list|(
name|revokeAction
argument_list|,
name|USER_CREATE
argument_list|,
name|USER_RW
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

