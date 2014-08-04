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
name|UUID
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
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|Put
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
name|client
operator|.
name|Scan
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
name|apache
operator|.
name|log4j
operator|.
name|Level
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|Before
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
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestScanEarlyTermination
extends|extends
name|SecureTestUtil
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestScanEarlyTermination
operator|.
name|class
argument_list|)
decl_stmt|;
static|static
block|{
name|Logger
operator|.
name|getLogger
argument_list|(
name|AccessController
operator|.
name|class
argument_list|)
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|TRACE
argument_list|)
expr_stmt|;
name|Logger
operator|.
name|getLogger
argument_list|(
name|AccessControlFilter
operator|.
name|class
argument_list|)
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|TRACE
argument_list|)
expr_stmt|;
name|Logger
operator|.
name|getLogger
argument_list|(
name|TableAuthManager
operator|.
name|class
argument_list|)
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|TRACE
argument_list|)
expr_stmt|;
block|}
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
specifier|final
name|byte
index|[]
name|TEST_FAMILY1
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
specifier|final
name|byte
index|[]
name|TEST_FAMILY2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TEST_ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testrow"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TEST_Q1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TEST_Q2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ZERO
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0L
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
name|User
name|USER_OWNER
decl_stmt|;
specifier|private
specifier|static
name|User
name|USER_OTHER
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
name|AccessController
name|ac
init|=
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
decl_stmt|;
name|cpHost
operator|.
name|createEnvironment
argument_list|(
name|AccessController
operator|.
name|class
argument_list|,
name|ac
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
init|=
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
decl_stmt|;
name|rsHost
operator|.
name|createEnvironment
argument_list|(
name|AccessController
operator|.
name|class
argument_list|,
name|ac
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
comment|// create a set of test users
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
name|USER_OTHER
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"other"
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
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
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|htd
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
name|htd
operator|.
name|setOwner
argument_list|(
name|USER_OWNER
argument_list|)
expr_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAMILY1
argument_list|)
decl_stmt|;
name|hcd
operator|.
name|setMaxVersions
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|hcd
operator|=
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAMILY2
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMaxVersions
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
comment|// Enable backwards compatible early termination behavior in the HTD. We
comment|// want to confirm that the per-table configuration is properly picked up.
name|htd
operator|.
name|setConfiguration
argument_list|(
name|AccessControlConstants
operator|.
name|CF_ATTRIBUTE_EARLY_OUT
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|htd
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
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Clean the _acl_ table
try|try
block|{
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|TEST_TABLE
operator|.
name|getTableName
argument_list|()
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
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
operator|.
name|getTableName
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
name|testEarlyScanTermination
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Grant USER_OTHER access to TEST_FAMILY1 only
name|grantOnTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|USER_OTHER
operator|.
name|getShortName
argument_list|()
argument_list|,
name|TEST_TABLE
operator|.
name|getTableName
argument_list|()
argument_list|,
name|TEST_FAMILY1
argument_list|,
literal|null
argument_list|,
name|Action
operator|.
name|READ
argument_list|)
expr_stmt|;
comment|// Set up test data
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
comment|// force a new RS connection
name|conf
operator|.
name|set
argument_list|(
literal|"testkey"
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|TEST_TABLE
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|TEST_ROW
argument_list|)
operator|.
name|add
argument_list|(
name|TEST_FAMILY1
argument_list|,
name|TEST_Q1
argument_list|,
name|ZERO
argument_list|)
decl_stmt|;
name|t
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
comment|// Set a READ cell ACL for USER_OTHER on this value in FAMILY2
name|put
operator|=
operator|new
name|Put
argument_list|(
name|TEST_ROW
argument_list|)
operator|.
name|add
argument_list|(
name|TEST_FAMILY2
argument_list|,
name|TEST_Q1
argument_list|,
name|ZERO
argument_list|)
expr_stmt|;
name|put
operator|.
name|setACL
argument_list|(
name|USER_OTHER
operator|.
name|getShortName
argument_list|()
argument_list|,
operator|new
name|Permission
argument_list|(
name|Action
operator|.
name|READ
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
comment|// Set an empty cell ACL for USER_OTHER on this other value in FAMILY2
name|put
operator|=
operator|new
name|Put
argument_list|(
name|TEST_ROW
argument_list|)
operator|.
name|add
argument_list|(
name|TEST_FAMILY2
argument_list|,
name|TEST_Q2
argument_list|,
name|ZERO
argument_list|)
expr_stmt|;
name|put
operator|.
name|setACL
argument_list|(
name|USER_OTHER
operator|.
name|getShortName
argument_list|()
argument_list|,
operator|new
name|Permission
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|t
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
name|USER_OWNER
argument_list|)
expr_stmt|;
comment|// A scan of FAMILY1 will be allowed
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
comment|// force a new RS connection
name|conf
operator|.
name|set
argument_list|(
literal|"testkey"
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|TEST_TABLE
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
operator|.
name|addFamily
argument_list|(
name|TEST_FAMILY1
argument_list|)
decl_stmt|;
name|Result
name|result
init|=
name|t
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
name|assertTrue
argument_list|(
literal|"Improper exclusion"
argument_list|,
name|result
operator|.
name|containsColumn
argument_list|(
name|TEST_FAMILY1
argument_list|,
name|TEST_Q1
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Improper inclusion"
argument_list|,
name|result
operator|.
name|containsColumn
argument_list|(
name|TEST_FAMILY2
argument_list|,
name|TEST_Q1
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|result
operator|.
name|listCells
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
finally|finally
block|{
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|,
name|USER_OTHER
argument_list|)
expr_stmt|;
comment|// A scan of FAMILY1 and FAMILY2 will produce results for FAMILY1 without
comment|// throwing an exception, however no cells from FAMILY2 will be returned
comment|// because we early out checks at the CF level.
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
comment|// force a new RS connection
name|conf
operator|.
name|set
argument_list|(
literal|"testkey"
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|TEST_TABLE
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|Result
name|result
init|=
name|t
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
name|assertTrue
argument_list|(
literal|"Improper exclusion"
argument_list|,
name|result
operator|.
name|containsColumn
argument_list|(
name|TEST_FAMILY1
argument_list|,
name|TEST_Q1
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Improper inclusion"
argument_list|,
name|result
operator|.
name|containsColumn
argument_list|(
name|TEST_FAMILY2
argument_list|,
name|TEST_Q1
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|result
operator|.
name|listCells
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
finally|finally
block|{
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|,
name|USER_OTHER
argument_list|)
expr_stmt|;
comment|// A scan of FAMILY2 will throw an AccessDeniedException
name|verifyDeniedWithException
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
comment|// force a new RS connection
name|conf
operator|.
name|set
argument_list|(
literal|"testkey"
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|TEST_TABLE
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
operator|.
name|addFamily
argument_list|(
name|TEST_FAMILY2
argument_list|)
decl_stmt|;
name|Result
name|result
init|=
name|t
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
return|return
name|result
operator|.
name|listCells
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
finally|finally
block|{
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|,
name|USER_OTHER
argument_list|)
expr_stmt|;
comment|// Now grant USER_OTHER access to TEST_FAMILY2:TEST_Q2
name|grantOnTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|USER_OTHER
operator|.
name|getShortName
argument_list|()
argument_list|,
name|TEST_TABLE
operator|.
name|getTableName
argument_list|()
argument_list|,
name|TEST_FAMILY2
argument_list|,
name|TEST_Q2
argument_list|,
name|Action
operator|.
name|READ
argument_list|)
expr_stmt|;
comment|// A scan of FAMILY1 and FAMILY2 will produce combined results. In FAMILY2
comment|// we have access granted to Q2 at the CF level. Because we early out
comment|// checks at the CF level the cell ACL on Q1 also granting access is ignored.
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
comment|// force a new RS connection
name|conf
operator|.
name|set
argument_list|(
literal|"testkey"
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|TEST_TABLE
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|Result
name|result
init|=
name|t
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
name|assertTrue
argument_list|(
literal|"Improper exclusion"
argument_list|,
name|result
operator|.
name|containsColumn
argument_list|(
name|TEST_FAMILY1
argument_list|,
name|TEST_Q1
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Improper inclusion"
argument_list|,
name|result
operator|.
name|containsColumn
argument_list|(
name|TEST_FAMILY2
argument_list|,
name|TEST_Q1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Improper exclusion"
argument_list|,
name|result
operator|.
name|containsColumn
argument_list|(
name|TEST_FAMILY2
argument_list|,
name|TEST_Q2
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|result
operator|.
name|listCells
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
finally|finally
block|{
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|,
name|USER_OTHER
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

