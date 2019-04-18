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
name|security
operator|.
name|access
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
name|assertFalse
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
name|CoprocessorEnvironment
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
name|HBaseClassTestRule
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
name|ObserverContextImpl
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
name|RegionCoprocessorEnvironment
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
name|RegionServerCoprocessorEnvironment
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
name|regionserver
operator|.
name|HRegion
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
name|regionserver
operator|.
name|HRegionServer
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
name|regionserver
operator|.
name|RegionCoprocessorHost
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
name|regionserver
operator|.
name|RegionServerCoprocessorHost
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
name|testclassification
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|JVMClusterUtil
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
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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
name|junit
operator|.
name|rules
operator|.
name|TestName
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
comment|/**  * Performs checks for reference counting w.r.t. AuthManager which is used by  * AccessController.  *  * NOTE: Only one test in  here. In AMv2, there is problem deleting because  * we are missing auth. For now disabled. See the cleanup method.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|SecurityTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestAccessController3
extends|extends
name|SecureTestUtil
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestAccessController3
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TestAccessController
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
comment|/** The systemUserConnection created here is tied to the system user. In case, you are planning    * to create AccessTestAction, DON'T use this systemUserConnection as the 'doAs' user    * gets  eclipsed by the system user. */
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
comment|// user with admin rights on the column family
specifier|private
specifier|static
name|User
name|USER_ADMIN_CF
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
comment|// TODO: convert this test to cover the full matrix in
comment|// https://hbase.apache.org/book/appendix_acl_matrix.html
comment|// creating all Scope x Permission combinations
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
name|MasterCoprocessorEnvironment
name|CP_ENV
decl_stmt|;
specifier|private
specifier|static
name|AccessController
name|ACCESS_CONTROLLER
decl_stmt|;
specifier|private
specifier|static
name|RegionServerCoprocessorEnvironment
name|RSCP_ENV
decl_stmt|;
specifier|private
specifier|static
name|RegionCoprocessorEnvironment
name|RCP_ENV
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|callSuperTwice
init|=
literal|true
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
comment|// class with faulty stop() method, controlled by flag
specifier|public
specifier|static
class|class
name|FaultyAccessController
extends|extends
name|AccessController
block|{
specifier|public
name|FaultyAccessController
parameter_list|()
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
block|{
name|super
operator|.
name|stop
argument_list|(
name|env
argument_list|)
expr_stmt|;
if|if
condition|(
name|callSuperTwice
condition|)
block|{
name|super
operator|.
name|stop
argument_list|(
name|env
argument_list|)
expr_stmt|;
block|}
block|}
block|}
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
comment|// Enable security
name|enableSecurity
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|String
name|accessControllerClassName
init|=
name|FaultyAccessController
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|// In this particular test case, we can't use SecureBulkLoadEndpoint because its doAs will fail
comment|// to move a file for a random user
name|conf
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|REGION_COPROCESSOR_CONF_KEY
argument_list|,
name|accessControllerClassName
argument_list|)
expr_stmt|;
comment|// Verify enableSecurity sets up what we require
name|verifyConfiguration
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// Enable EXEC permission checking
name|conf
operator|.
name|setBoolean
argument_list|(
name|AccessControlConstants
operator|.
name|EXEC_PERMISSION_CHECKS_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|MasterCoprocessorHost
name|cpHost
init|=
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
decl_stmt|;
name|cpHost
operator|.
name|load
argument_list|(
name|FaultyAccessController
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
name|accessControllerClassName
argument_list|)
expr_stmt|;
name|CP_ENV
operator|=
name|cpHost
operator|.
name|createEnvironment
argument_list|(
name|ACCESS_CONTROLLER
argument_list|,
name|Coprocessor
operator|.
name|PRIORITY_HIGHEST
argument_list|,
literal|1
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|RegionServerCoprocessorHost
name|rsHost
decl_stmt|;
do|do
block|{
name|rsHost
operator|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionServerCoprocessorHost
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|rsHost
operator|==
literal|null
condition|)
do|;
name|RSCP_ENV
operator|=
name|rsHost
operator|.
name|createEnvironment
argument_list|(
name|ACCESS_CONTROLLER
argument_list|,
name|Coprocessor
operator|.
name|PRIORITY_HIGHEST
argument_list|,
literal|1
argument_list|,
name|conf
argument_list|)
expr_stmt|;
comment|// Wait for the ACL table to become available
name|TEST_UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|PermissionStorage
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
name|USER_ADMIN_CF
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"col_family_admin"
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
name|HRegionServer
name|rs
init|=
literal|null
decl_stmt|;
for|for
control|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|thread
range|:
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
control|)
block|{
name|rs
operator|=
name|thread
operator|.
name|getRegionServer
argument_list|()
expr_stmt|;
block|}
name|cleanUp
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
literal|"region server should have aborted due to FaultyAccessController"
argument_list|,
name|rs
operator|.
name|isAborted
argument_list|()
argument_list|)
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
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAMILY
argument_list|)
decl_stmt|;
name|hcd
operator|.
name|setMaxVersions
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setOwner
argument_list|(
name|USER_OWNER
argument_list|)
expr_stmt|;
name|createTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|htd
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
name|HRegion
name|region
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|TEST_TABLE
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|RegionCoprocessorHost
name|rcpHost
init|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
decl_stmt|;
name|RCP_ENV
operator|=
name|rcpHost
operator|.
name|createEnvironment
argument_list|(
name|ACCESS_CONTROLLER
argument_list|,
name|Coprocessor
operator|.
name|PRIORITY_HIGHEST
argument_list|,
literal|1
argument_list|,
name|conf
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
name|grantOnTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|USER_ADMIN_CF
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
name|ADMIN
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
literal|5
argument_list|,
name|PermissionStorage
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
literal|5
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
comment|// TODO: Skipping delete because of access issues w/ AMv2.
comment|// AMv1 seems to crash servers on exit too for same lack of
comment|// auth perms but it gets hung up.
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
name|PermissionStorage
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
name|PermissionStorage
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
name|Test
specifier|public
name|void
name|testTableCreate
parameter_list|()
throws|throws
name|Exception
block|{
name|AccessTestAction
name|createTable
init|=
operator|new
name|AccessTestAction
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|ACCESS_CONTROLLER
operator|.
name|preCreateTable
argument_list|(
name|ObserverContextImpl
operator|.
name|createAndPrepare
argument_list|(
name|CP_ENV
argument_list|)
argument_list|,
name|htd
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
comment|// verify that superuser can create tables
name|verifyAllowed
argument_list|(
name|createTable
argument_list|,
name|SUPERUSER
argument_list|,
name|USER_ADMIN
argument_list|,
name|USER_GROUP_CREATE
argument_list|)
expr_stmt|;
comment|// all others should be denied
name|verifyDenied
argument_list|(
name|createTable
argument_list|,
name|USER_CREATE
argument_list|,
name|USER_RW
argument_list|,
name|USER_RO
argument_list|,
name|USER_NONE
argument_list|,
name|USER_GROUP_ADMIN
argument_list|,
name|USER_GROUP_READ
argument_list|,
name|USER_GROUP_WRITE
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

