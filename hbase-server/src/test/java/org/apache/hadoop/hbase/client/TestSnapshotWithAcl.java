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
name|client
package|;
end_package

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
name|AccessControlConstants
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
name|AccessController
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
name|testclassification
operator|.
name|ClientTests
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
name|Assert
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestSnapshotWithAcl
extends|extends
name|SecureTestUtil
block|{
specifier|public
name|TableName
name|TEST_TABLE
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestSnapshotWithAcl"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ROW_COUNT
init|=
literal|30000
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
name|byte
index|[]
name|TEST_QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|TEST_ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r1"
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
specifier|private
specifier|static
name|HBaseAdmin
name|admin
init|=
literal|null
decl_stmt|;
comment|// user is table owner. will have all permissions on table
specifier|private
specifier|static
name|User
name|USER_OWNER
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
comment|// user with none permissions
specifier|private
specifier|static
name|User
name|USER_NONE
decl_stmt|;
specifier|static
class|class
name|AccessReadAction
implements|implements
name|AccessTestAction
block|{
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|public
name|AccessReadAction
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|TEST_ROW
argument_list|)
decl_stmt|;
name|g
operator|.
name|addFamily
argument_list|(
name|TEST_FAMILY
argument_list|)
expr_stmt|;
try|try
init|(
name|Connection
name|conn
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
name|t
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|t
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
empty_stmt|;
specifier|static
class|class
name|AccessWriteAction
implements|implements
name|AccessTestAction
block|{
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|public
name|AccessWriteAction
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|TEST_ROW
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|TEST_FAMILY
argument_list|,
name|TEST_QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
name|Connection
name|conn
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
name|t
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|null
return|;
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
name|conf
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|REGION_COPROCESSOR_CONF_KEY
argument_list|,
name|AccessController
operator|.
name|class
operator|.
name|getName
argument_list|()
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
name|USER_NONE
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"usernone"
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
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|admin
operator|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
expr_stmt|;
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
name|admin
operator|.
name|createTable
argument_list|(
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
name|TEST_UTIL
operator|.
name|waitTableEnabled
argument_list|(
name|TEST_TABLE
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
block|}
specifier|private
name|void
name|loadData
parameter_list|()
throws|throws
name|IOException
block|{
try|try
init|(
name|Connection
name|conn
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
name|t
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|TEST_TABLE
argument_list|)
init|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ROW_COUNT
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|TEST_FAMILY
argument_list|,
name|TEST_QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
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
block|}
block|}
block|}
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
specifier|private
name|void
name|verifyRows
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|Connection
name|conn
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
name|t
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
try|try
init|(
name|ResultScanner
name|scanner
init|=
name|t
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
init|)
block|{
name|Result
name|result
decl_stmt|;
name|int
name|rowCount
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|(
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|byte
index|[]
name|value
init|=
name|result
operator|.
name|getValue
argument_list|(
name|TEST_FAMILY
argument_list|,
name|TEST_QUALIFIER
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|value
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rowCount
operator|++
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|rowCount
argument_list|,
name|ROW_COUNT
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRestoreSnapshot
parameter_list|()
throws|throws
name|Exception
block|{
name|verifyAllowed
argument_list|(
operator|new
name|AccessReadAction
argument_list|(
name|TEST_TABLE
argument_list|)
argument_list|,
name|USER_OWNER
argument_list|,
name|USER_RO
argument_list|,
name|USER_RW
argument_list|)
expr_stmt|;
name|verifyDenied
argument_list|(
operator|new
name|AccessWriteAction
argument_list|(
name|TEST_TABLE
argument_list|)
argument_list|,
name|USER_RO
argument_list|,
name|USER_NONE
argument_list|)
expr_stmt|;
name|loadData
argument_list|()
expr_stmt|;
name|verifyRows
argument_list|(
name|TEST_TABLE
argument_list|)
expr_stmt|;
name|String
name|snapshotName1
init|=
literal|"testSnapshot1"
decl_stmt|;
name|admin
operator|.
name|snapshot
argument_list|(
name|snapshotName1
argument_list|,
name|TEST_TABLE
argument_list|)
expr_stmt|;
comment|// clone snapshot with restoreAcl true.
name|TableName
name|tableName1
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"tableName1"
argument_list|)
decl_stmt|;
name|admin
operator|.
name|cloneSnapshot
argument_list|(
name|snapshotName1
argument_list|,
name|tableName1
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyRows
argument_list|(
name|tableName1
argument_list|)
expr_stmt|;
name|verifyAllowed
argument_list|(
operator|new
name|AccessReadAction
argument_list|(
name|tableName1
argument_list|)
argument_list|,
name|USER_OWNER
argument_list|,
name|USER_RO
argument_list|,
name|USER_RW
argument_list|)
expr_stmt|;
name|verifyDenied
argument_list|(
operator|new
name|AccessWriteAction
argument_list|(
name|tableName1
argument_list|)
argument_list|,
name|USER_RO
argument_list|,
name|USER_NONE
argument_list|)
expr_stmt|;
comment|// clone snapshot with restoreAcl false.
name|TableName
name|tableName2
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"tableName2"
argument_list|)
decl_stmt|;
name|admin
operator|.
name|cloneSnapshot
argument_list|(
name|snapshotName1
argument_list|,
name|tableName2
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|verifyRows
argument_list|(
name|tableName2
argument_list|)
expr_stmt|;
name|verifyAllowed
argument_list|(
operator|new
name|AccessReadAction
argument_list|(
name|tableName2
argument_list|)
argument_list|,
name|USER_OWNER
argument_list|)
expr_stmt|;
name|verifyDenied
argument_list|(
operator|new
name|AccessWriteAction
argument_list|(
name|tableName2
argument_list|)
argument_list|,
name|USER_RO
argument_list|,
name|USER_RW
argument_list|,
name|USER_NONE
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

