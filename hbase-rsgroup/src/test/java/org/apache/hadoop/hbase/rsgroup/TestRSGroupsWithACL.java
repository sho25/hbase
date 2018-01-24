begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *   http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|AuthUtil
operator|.
name|toGroupEntry
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|TableNotFoundException
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
name|ColumnFamilyDescriptorBuilder
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
name|TableDescriptorBuilder
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
name|AccessControlClient
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
name|AccessControlLists
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
name|SecureTestUtil
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
name|TableAuthManager
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
name|testclassification
operator|.
name|SecurityTests
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
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Performs authorization checks for rsgroup operations, according to different  * levels of authorized users.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|SecurityTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestRSGroupsWithACL
extends|extends
name|SecureTestUtil
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestRSGroupsWithACL
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|TableName
name|TEST_TABLE
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testtable1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
name|Connection
name|systemUserConnection
decl_stmt|;
comment|// user with all permissions
specifier|private
specifier|static
name|User
name|SUPERUSER
decl_stmt|;
comment|// user granted with all global permission
specifier|private
specifier|static
name|User
name|USER_ADMIN
decl_stmt|;
comment|// user with rw permissions on column family.
specifier|private
specifier|static
name|User
name|USER_RW
decl_stmt|;
comment|// user with read-only permissions
specifier|private
specifier|static
name|User
name|USER_RO
decl_stmt|;
comment|// user is table owner. will have all permissions on table
specifier|private
specifier|static
name|User
name|USER_OWNER
decl_stmt|;
comment|// user with create table permissions alone
specifier|private
specifier|static
name|User
name|USER_CREATE
decl_stmt|;
comment|// user with no permissions
specifier|private
specifier|static
name|User
name|USER_NONE
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|GROUP_ADMIN
init|=
literal|"group_admin"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|GROUP_CREATE
init|=
literal|"group_create"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|GROUP_READ
init|=
literal|"group_read"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|GROUP_WRITE
init|=
literal|"group_write"
decl_stmt|;
specifier|private
specifier|static
name|User
name|USER_GROUP_ADMIN
decl_stmt|;
specifier|private
specifier|static
name|User
name|USER_GROUP_CREATE
decl_stmt|;
specifier|private
specifier|static
name|User
name|USER_GROUP_READ
decl_stmt|;
specifier|private
specifier|static
name|User
name|USER_GROUP_WRITE
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|TEST_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|RSGroupAdminEndpoint
name|rsGroupAdminEndpoint
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
comment|// setup configuration
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_MASTER_LOADBALANCER_CLASS
argument_list|,
name|RSGroupBasedLoadBalancer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Enable security
name|enableSecurity
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// Verify enableSecurity sets up what we require
name|verifyConfiguration
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// Enable rsgroup
name|configureRSGroupAdminEndpoint
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|rsGroupAdminEndpoint
operator|=
operator|(
name|RSGroupAdminEndpoint
operator|)
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|RSGroupAdminEndpoint
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Wait for the ACL table to become available
name|TEST_UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|AccessControlLists
operator|.
name|ACL_TABLE_NAME
argument_list|)
expr_stmt|;
comment|// create a set of test users
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
name|USER_ADMIN
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"admin2"
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
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
literal|"rwuser"
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|USER_RO
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"rouser"
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|USER_OWNER
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"owner"
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
literal|"tbl_create"
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|USER_NONE
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"nouser"
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|USER_GROUP_ADMIN
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"user_group_admin"
argument_list|,
operator|new
name|String
index|[]
block|{
name|GROUP_ADMIN
block|}
argument_list|)
expr_stmt|;
name|USER_GROUP_CREATE
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"user_group_create"
argument_list|,
operator|new
name|String
index|[]
block|{
name|GROUP_CREATE
block|}
argument_list|)
expr_stmt|;
name|USER_GROUP_READ
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"user_group_read"
argument_list|,
operator|new
name|String
index|[]
block|{
name|GROUP_READ
block|}
argument_list|)
expr_stmt|;
name|USER_GROUP_WRITE
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"user_group_write"
argument_list|,
operator|new
name|String
index|[]
block|{
name|GROUP_WRITE
block|}
argument_list|)
expr_stmt|;
name|systemUserConnection
operator|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
expr_stmt|;
name|setUpTableAndUserPermissions
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|setUpTableAndUserPermissions
parameter_list|()
throws|throws
name|Exception
block|{
name|TableDescriptorBuilder
name|tableBuilder
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|ColumnFamilyDescriptorBuilder
name|cfd
init|=
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TEST_FAMILY
argument_list|)
decl_stmt|;
name|cfd
operator|.
name|setMaxVersions
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|tableBuilder
operator|.
name|addColumnFamily
argument_list|(
name|cfd
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|tableBuilder
operator|.
name|setValue
argument_list|(
name|TableDescriptorBuilder
operator|.
name|OWNER
argument_list|,
name|USER_OWNER
operator|.
name|getShortName
argument_list|()
argument_list|)
expr_stmt|;
name|createTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|tableBuilder
operator|.
name|build
argument_list|()
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"s"
argument_list|)
block|}
argument_list|)
expr_stmt|;
comment|// Set up initial grants
name|grantGlobal
argument_list|(
name|TEST_UTIL
argument_list|,
name|USER_ADMIN
operator|.
name|getShortName
argument_list|()
argument_list|,
name|Permission
operator|.
name|Action
operator|.
name|ADMIN
argument_list|,
name|Permission
operator|.
name|Action
operator|.
name|CREATE
argument_list|,
name|Permission
operator|.
name|Action
operator|.
name|READ
argument_list|,
name|Permission
operator|.
name|Action
operator|.
name|WRITE
argument_list|)
expr_stmt|;
name|grantOnTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|USER_RW
operator|.
name|getShortName
argument_list|()
argument_list|,
name|TEST_TABLE
argument_list|,
name|TEST_FAMILY
argument_list|,
literal|null
argument_list|,
name|Permission
operator|.
name|Action
operator|.
name|READ
argument_list|,
name|Permission
operator|.
name|Action
operator|.
name|WRITE
argument_list|)
expr_stmt|;
comment|// USER_CREATE is USER_RW plus CREATE permissions
name|grantOnTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|USER_CREATE
operator|.
name|getShortName
argument_list|()
argument_list|,
name|TEST_TABLE
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|Permission
operator|.
name|Action
operator|.
name|CREATE
argument_list|,
name|Permission
operator|.
name|Action
operator|.
name|READ
argument_list|,
name|Permission
operator|.
name|Action
operator|.
name|WRITE
argument_list|)
expr_stmt|;
name|grantOnTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|USER_RO
operator|.
name|getShortName
argument_list|()
argument_list|,
name|TEST_TABLE
argument_list|,
name|TEST_FAMILY
argument_list|,
literal|null
argument_list|,
name|Permission
operator|.
name|Action
operator|.
name|READ
argument_list|)
expr_stmt|;
name|grantGlobal
argument_list|(
name|TEST_UTIL
argument_list|,
name|toGroupEntry
argument_list|(
name|GROUP_ADMIN
argument_list|)
argument_list|,
name|Permission
operator|.
name|Action
operator|.
name|ADMIN
argument_list|)
expr_stmt|;
name|grantGlobal
argument_list|(
name|TEST_UTIL
argument_list|,
name|toGroupEntry
argument_list|(
name|GROUP_CREATE
argument_list|)
argument_list|,
name|Permission
operator|.
name|Action
operator|.
name|CREATE
argument_list|)
expr_stmt|;
name|grantGlobal
argument_list|(
name|TEST_UTIL
argument_list|,
name|toGroupEntry
argument_list|(
name|GROUP_READ
argument_list|)
argument_list|,
name|Permission
operator|.
name|Action
operator|.
name|READ
argument_list|)
expr_stmt|;
name|grantGlobal
argument_list|(
name|TEST_UTIL
argument_list|,
name|toGroupEntry
argument_list|(
name|GROUP_WRITE
argument_list|)
argument_list|,
name|Permission
operator|.
name|Action
operator|.
name|WRITE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|AccessControlLists
operator|.
name|getTablePermissions
argument_list|(
name|conf
argument_list|,
name|TEST_TABLE
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|AccessControlClient
operator|.
name|getUserPermissions
argument_list|(
name|systemUserConnection
argument_list|,
name|TEST_TABLE
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"error during call of AccessControlClient.getUserPermissions. "
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"error during call of AccessControlClient.getUserPermissions."
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|cleanUp
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Clean the _acl_ table
try|try
block|{
name|deleteTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|TEST_TABLE
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableNotFoundException
name|ex
parameter_list|)
block|{
comment|// Test deleted the table, no problem
name|LOG
operator|.
name|info
argument_list|(
literal|"Test deleted table "
operator|+
name|TEST_TABLE
argument_list|)
expr_stmt|;
block|}
comment|// Verify all table/namespace permissions are erased
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|AccessControlLists
operator|.
name|getTablePermissions
argument_list|(
name|conf
argument_list|,
name|TEST_TABLE
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|AccessControlLists
operator|.
name|getNamespacePermissions
argument_list|(
name|conf
argument_list|,
name|TEST_TABLE
operator|.
name|getNamespaceAsString
argument_list|()
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|cleanUp
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
name|int
name|total
init|=
name|TableAuthManager
operator|.
name|getTotalRefCount
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Unexpected reference count: "
operator|+
name|total
argument_list|,
name|total
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|configureRSGroupAdminEndpoint
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|currentCoprocessors
init|=
name|conf
operator|.
name|get
argument_list|(
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|)
decl_stmt|;
name|String
name|coprocessors
init|=
name|RSGroupAdminEndpoint
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|currentCoprocessors
operator|!=
literal|null
condition|)
block|{
name|coprocessors
operator|+=
literal|","
operator|+
name|currentCoprocessors
expr_stmt|;
block|}
name|conf
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|coprocessors
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_MASTER_LOADBALANCER_CLASS
argument_list|,
name|RSGroupBasedLoadBalancer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetRSGroupInfo
parameter_list|()
throws|throws
name|Exception
block|{
name|AccessTestAction
name|action
init|=
parameter_list|()
lambda|->
block|{
name|rsGroupAdminEndpoint
operator|.
name|checkPermission
argument_list|(
literal|"getRSGroupInfo"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
decl_stmt|;
name|verifyAllowed
argument_list|(
name|action
argument_list|,
name|SUPERUSER
argument_list|,
name|USER_ADMIN
argument_list|,
name|USER_GROUP_ADMIN
argument_list|)
expr_stmt|;
name|verifyDenied
argument_list|(
name|action
argument_list|,
name|USER_CREATE
argument_list|,
name|USER_OWNER
argument_list|,
name|USER_RW
argument_list|,
name|USER_RO
argument_list|,
name|USER_NONE
argument_list|,
name|USER_GROUP_READ
argument_list|,
name|USER_GROUP_WRITE
argument_list|,
name|USER_GROUP_CREATE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetRSGroupInfoOfTable
parameter_list|()
throws|throws
name|Exception
block|{
name|AccessTestAction
name|action
init|=
parameter_list|()
lambda|->
block|{
name|rsGroupAdminEndpoint
operator|.
name|checkPermission
argument_list|(
literal|"getRSGroupInfoOfTable"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
decl_stmt|;
name|verifyAllowed
argument_list|(
name|action
argument_list|,
name|SUPERUSER
argument_list|,
name|USER_ADMIN
argument_list|,
name|USER_GROUP_ADMIN
argument_list|)
expr_stmt|;
name|verifyDenied
argument_list|(
name|action
argument_list|,
name|USER_CREATE
argument_list|,
name|USER_OWNER
argument_list|,
name|USER_RW
argument_list|,
name|USER_RO
argument_list|,
name|USER_NONE
argument_list|,
name|USER_GROUP_READ
argument_list|,
name|USER_GROUP_WRITE
argument_list|,
name|USER_GROUP_CREATE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMoveServers
parameter_list|()
throws|throws
name|Exception
block|{
name|AccessTestAction
name|action
init|=
parameter_list|()
lambda|->
block|{
name|rsGroupAdminEndpoint
operator|.
name|checkPermission
argument_list|(
literal|"moveServers"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
decl_stmt|;
name|verifyAllowed
argument_list|(
name|action
argument_list|,
name|SUPERUSER
argument_list|,
name|USER_ADMIN
argument_list|,
name|USER_GROUP_ADMIN
argument_list|)
expr_stmt|;
name|verifyDenied
argument_list|(
name|action
argument_list|,
name|USER_CREATE
argument_list|,
name|USER_OWNER
argument_list|,
name|USER_RW
argument_list|,
name|USER_RO
argument_list|,
name|USER_NONE
argument_list|,
name|USER_GROUP_READ
argument_list|,
name|USER_GROUP_WRITE
argument_list|,
name|USER_GROUP_CREATE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMoveTables
parameter_list|()
throws|throws
name|Exception
block|{
name|AccessTestAction
name|action
init|=
parameter_list|()
lambda|->
block|{
name|rsGroupAdminEndpoint
operator|.
name|checkPermission
argument_list|(
literal|"moveTables"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
decl_stmt|;
name|verifyAllowed
argument_list|(
name|action
argument_list|,
name|SUPERUSER
argument_list|,
name|USER_ADMIN
argument_list|,
name|USER_GROUP_ADMIN
argument_list|)
expr_stmt|;
name|verifyDenied
argument_list|(
name|action
argument_list|,
name|USER_CREATE
argument_list|,
name|USER_OWNER
argument_list|,
name|USER_RW
argument_list|,
name|USER_RO
argument_list|,
name|USER_NONE
argument_list|,
name|USER_GROUP_READ
argument_list|,
name|USER_GROUP_WRITE
argument_list|,
name|USER_GROUP_CREATE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAddRSGroup
parameter_list|()
throws|throws
name|Exception
block|{
name|AccessTestAction
name|action
init|=
parameter_list|()
lambda|->
block|{
name|rsGroupAdminEndpoint
operator|.
name|checkPermission
argument_list|(
literal|"addRSGroup"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
decl_stmt|;
name|verifyAllowed
argument_list|(
name|action
argument_list|,
name|SUPERUSER
argument_list|,
name|USER_ADMIN
argument_list|,
name|USER_GROUP_ADMIN
argument_list|)
expr_stmt|;
name|verifyDenied
argument_list|(
name|action
argument_list|,
name|USER_CREATE
argument_list|,
name|USER_OWNER
argument_list|,
name|USER_RW
argument_list|,
name|USER_RO
argument_list|,
name|USER_NONE
argument_list|,
name|USER_GROUP_READ
argument_list|,
name|USER_GROUP_WRITE
argument_list|,
name|USER_GROUP_CREATE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRemoveRSGroup
parameter_list|()
throws|throws
name|Exception
block|{
name|AccessTestAction
name|action
init|=
parameter_list|()
lambda|->
block|{
name|rsGroupAdminEndpoint
operator|.
name|checkPermission
argument_list|(
literal|"removeRSGroup"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
decl_stmt|;
name|verifyAllowed
argument_list|(
name|action
argument_list|,
name|SUPERUSER
argument_list|,
name|USER_ADMIN
argument_list|,
name|USER_GROUP_ADMIN
argument_list|)
expr_stmt|;
name|verifyDenied
argument_list|(
name|action
argument_list|,
name|USER_CREATE
argument_list|,
name|USER_OWNER
argument_list|,
name|USER_RW
argument_list|,
name|USER_RO
argument_list|,
name|USER_NONE
argument_list|,
name|USER_GROUP_READ
argument_list|,
name|USER_GROUP_WRITE
argument_list|,
name|USER_GROUP_CREATE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBalanceRSGroup
parameter_list|()
throws|throws
name|Exception
block|{
name|AccessTestAction
name|action
init|=
parameter_list|()
lambda|->
block|{
name|rsGroupAdminEndpoint
operator|.
name|checkPermission
argument_list|(
literal|"balanceRSGroup"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
decl_stmt|;
name|verifyAllowed
argument_list|(
name|action
argument_list|,
name|SUPERUSER
argument_list|,
name|USER_ADMIN
argument_list|,
name|USER_GROUP_ADMIN
argument_list|)
expr_stmt|;
name|verifyDenied
argument_list|(
name|action
argument_list|,
name|USER_CREATE
argument_list|,
name|USER_OWNER
argument_list|,
name|USER_RW
argument_list|,
name|USER_RO
argument_list|,
name|USER_NONE
argument_list|,
name|USER_GROUP_READ
argument_list|,
name|USER_GROUP_WRITE
argument_list|,
name|USER_GROUP_CREATE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testListRSGroup
parameter_list|()
throws|throws
name|Exception
block|{
name|AccessTestAction
name|action
init|=
parameter_list|()
lambda|->
block|{
name|rsGroupAdminEndpoint
operator|.
name|checkPermission
argument_list|(
literal|"listRSGroup"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
decl_stmt|;
name|verifyAllowed
argument_list|(
name|action
argument_list|,
name|SUPERUSER
argument_list|,
name|USER_ADMIN
argument_list|,
name|USER_GROUP_ADMIN
argument_list|)
expr_stmt|;
name|verifyDenied
argument_list|(
name|action
argument_list|,
name|USER_CREATE
argument_list|,
name|USER_OWNER
argument_list|,
name|USER_RW
argument_list|,
name|USER_RO
argument_list|,
name|USER_NONE
argument_list|,
name|USER_GROUP_READ
argument_list|,
name|USER_GROUP_WRITE
argument_list|,
name|USER_GROUP_CREATE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetRSGroupInfoOfServer
parameter_list|()
throws|throws
name|Exception
block|{
name|AccessTestAction
name|action
init|=
parameter_list|()
lambda|->
block|{
name|rsGroupAdminEndpoint
operator|.
name|checkPermission
argument_list|(
literal|"getRSGroupInfoOfServer"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
decl_stmt|;
name|verifyAllowed
argument_list|(
name|action
argument_list|,
name|SUPERUSER
argument_list|,
name|USER_ADMIN
argument_list|,
name|USER_GROUP_ADMIN
argument_list|)
expr_stmt|;
name|verifyDenied
argument_list|(
name|action
argument_list|,
name|USER_CREATE
argument_list|,
name|USER_OWNER
argument_list|,
name|USER_RW
argument_list|,
name|USER_RO
argument_list|,
name|USER_NONE
argument_list|,
name|USER_GROUP_READ
argument_list|,
name|USER_GROUP_WRITE
argument_list|,
name|USER_GROUP_CREATE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMoveServersAndTables
parameter_list|()
throws|throws
name|Exception
block|{
name|AccessTestAction
name|action
init|=
parameter_list|()
lambda|->
block|{
name|rsGroupAdminEndpoint
operator|.
name|checkPermission
argument_list|(
literal|"moveServersAndTables"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
decl_stmt|;
name|verifyAllowed
argument_list|(
name|action
argument_list|,
name|SUPERUSER
argument_list|,
name|USER_ADMIN
argument_list|,
name|USER_GROUP_ADMIN
argument_list|)
expr_stmt|;
name|verifyDenied
argument_list|(
name|action
argument_list|,
name|USER_CREATE
argument_list|,
name|USER_OWNER
argument_list|,
name|USER_RW
argument_list|,
name|USER_RO
argument_list|,
name|USER_NONE
argument_list|,
name|USER_GROUP_READ
argument_list|,
name|USER_GROUP_WRITE
argument_list|,
name|USER_GROUP_CREATE
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

