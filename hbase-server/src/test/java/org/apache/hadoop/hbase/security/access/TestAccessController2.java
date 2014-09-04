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
name|*
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
name|LargeTests
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
name|HBaseAdmin
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|TestTableName
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

begin_class
annotation|@
name|Category
argument_list|(
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestAccessController2
extends|extends
name|SecureTestUtil
block|{
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TEST_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
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
annotation|@
name|Rule
specifier|public
name|TestTableName
name|TEST_TABLE
init|=
operator|new
name|TestTableName
argument_list|()
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
comment|// Verify enableSecurity sets up what we require
name|verifyConfiguration
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
comment|// Wait for the ACL table to become available
name|TEST_UTIL
operator|.
name|waitTableEnabled
argument_list|(
name|AccessControlLists
operator|.
name|ACL_TABLE_NAME
operator|.
name|getName
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
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateWithCorrectOwner
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Create a test user
name|User
name|testUser
init|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|"TestUser"
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
comment|// Grant the test user the ability to create tables
name|SecureTestUtil
operator|.
name|grantGlobal
argument_list|(
name|TEST_UTIL
argument_list|,
name|testUser
operator|.
name|getShortName
argument_list|()
argument_list|,
name|Action
operator|.
name|CREATE
argument_list|)
expr_stmt|;
name|verifyAllowed
argument_list|(
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
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TEST_TABLE
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|desc
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
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|admin
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
argument_list|,
name|testUser
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableEnabled
argument_list|(
name|TEST_TABLE
operator|.
name|getTableName
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Verify that owner permissions have been granted to the test user on the
comment|// table just created
name|List
argument_list|<
name|TablePermission
argument_list|>
name|perms
init|=
name|AccessControlLists
operator|.
name|getTablePermissions
argument_list|(
name|conf
argument_list|,
name|TEST_TABLE
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|.
name|get
argument_list|(
name|testUser
operator|.
name|getShortName
argument_list|()
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|perms
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|perms
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// Should be RWXCA
name|assertTrue
argument_list|(
name|perms
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|implies
argument_list|(
name|Permission
operator|.
name|Action
operator|.
name|READ
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|perms
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|implies
argument_list|(
name|Permission
operator|.
name|Action
operator|.
name|WRITE
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|perms
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|implies
argument_list|(
name|Permission
operator|.
name|Action
operator|.
name|EXEC
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|perms
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|implies
argument_list|(
name|Permission
operator|.
name|Action
operator|.
name|CREATE
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|perms
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|implies
argument_list|(
name|Permission
operator|.
name|Action
operator|.
name|ADMIN
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

