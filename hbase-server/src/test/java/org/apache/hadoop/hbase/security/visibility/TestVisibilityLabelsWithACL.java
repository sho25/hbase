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
name|visibility
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
name|security
operator|.
name|visibility
operator|.
name|VisibilityConstants
operator|.
name|LABELS_TABLE_NAME
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
name|assertNull
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
name|ResultScanner
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
name|protobuf
operator|.
name|generated
operator|.
name|VisibilityLabelsProtos
operator|.
name|GetAuthsResponse
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
name|VisibilityLabelsProtos
operator|.
name|VisibilityLabelsResponse
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
name|PermissionStorage
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
name|TestVisibilityLabelsWithACL
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
name|TestVisibilityLabelsWithACL
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|PRIVATE
init|=
literal|"private"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CONFIDENTIAL
init|=
literal|"confidential"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SECRET
init|=
literal|"secret"
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
name|row1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|fam
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"info"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|qual
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
annotation|@
name|Rule
specifier|public
specifier|final
name|TestName
name|TEST_NAME
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|User
name|SUPERUSER
decl_stmt|;
specifier|private
specifier|static
name|User
name|NORMAL_USER1
decl_stmt|;
specifier|private
specifier|static
name|User
name|NORMAL_USER2
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
literal|"hbase.coprocessor.master.classes"
argument_list|,
name|AccessController
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|","
operator|+
name|VisibilityController
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.coprocessor.region.classes"
argument_list|,
name|AccessController
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|","
operator|+
name|VisibilityController
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableEnabled
argument_list|(
name|PermissionStorage
operator|.
name|ACL_TABLE_NAME
operator|.
name|getName
argument_list|()
argument_list|,
literal|50000
argument_list|)
expr_stmt|;
comment|// Wait for the labels table to become available
name|TEST_UTIL
operator|.
name|waitTableEnabled
argument_list|(
name|LABELS_TABLE_NAME
operator|.
name|getName
argument_list|()
argument_list|,
literal|50000
argument_list|)
expr_stmt|;
name|addLabels
argument_list|()
expr_stmt|;
comment|// Create users for testing
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
name|NORMAL_USER1
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"user1"
argument_list|,
operator|new
name|String
index|[]
block|{}
argument_list|)
expr_stmt|;
name|NORMAL_USER2
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"user2"
argument_list|,
operator|new
name|String
index|[]
block|{}
argument_list|)
expr_stmt|;
comment|// Grant users EXEC privilege on the labels table. For the purposes of this
comment|// test, we want to insure that access is denied even with the ability to access
comment|// the endpoint.
name|SecureTestUtil
operator|.
name|grantOnTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|NORMAL_USER1
operator|.
name|getShortName
argument_list|()
argument_list|,
name|LABELS_TABLE_NAME
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|Permission
operator|.
name|Action
operator|.
name|EXEC
argument_list|)
expr_stmt|;
name|SecureTestUtil
operator|.
name|grantOnTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|NORMAL_USER2
operator|.
name|getShortName
argument_list|()
argument_list|,
name|LABELS_TABLE_NAME
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|Permission
operator|.
name|Action
operator|.
name|EXEC
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
name|testScanForUserWithFewerLabelAuthsThanLabelsInScanAuthorizations
parameter_list|()
throws|throws
name|Throwable
block|{
name|String
index|[]
name|auths
init|=
block|{
name|SECRET
block|}
decl_stmt|;
name|String
name|user
init|=
literal|"user2"
decl_stmt|;
name|VisibilityClient
operator|.
name|setAuths
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|auths
argument_list|,
name|user
argument_list|)
expr_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|TEST_NAME
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Table
name|table
init|=
name|createTableAndWriteDataWithLabels
argument_list|(
name|tableName
argument_list|,
name|SECRET
operator|+
literal|"&"
operator|+
name|CONFIDENTIAL
operator|+
literal|"&!"
operator|+
name|PRIVATE
argument_list|,
name|SECRET
operator|+
literal|"&!"
operator|+
name|PRIVATE
argument_list|)
decl_stmt|;
name|SecureTestUtil
operator|.
name|grantOnTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|NORMAL_USER2
operator|.
name|getShortName
argument_list|()
argument_list|,
name|tableName
argument_list|,
literal|null
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
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
name|scanAction
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|s
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
name|SECRET
argument_list|,
name|CONFIDENTIAL
argument_list|)
argument_list|)
expr_stmt|;
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
init|;
name|Table
name|t
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
init|)
block|{
name|ResultScanner
name|scanner
init|=
name|t
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|Result
name|result
init|=
name|scanner
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
operator|!
name|result
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2"
argument_list|)
argument_list|,
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|NORMAL_USER2
operator|.
name|runAs
argument_list|(
name|scanAction
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScanForSuperUserWithFewerLabelAuths
parameter_list|()
throws|throws
name|Throwable
block|{
name|String
index|[]
name|auths
init|=
block|{
name|SECRET
block|}
decl_stmt|;
name|String
name|user
init|=
literal|"admin"
decl_stmt|;
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
name|VisibilityClient
operator|.
name|setAuths
argument_list|(
name|conn
argument_list|,
name|auths
argument_list|,
name|user
argument_list|)
expr_stmt|;
block|}
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|TEST_NAME
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Table
name|table
init|=
name|createTableAndWriteDataWithLabels
argument_list|(
name|tableName
argument_list|,
name|SECRET
operator|+
literal|"&"
operator|+
name|CONFIDENTIAL
operator|+
literal|"&!"
operator|+
name|PRIVATE
argument_list|,
name|SECRET
operator|+
literal|"&!"
operator|+
name|PRIVATE
argument_list|)
decl_stmt|;
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
name|scanAction
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|s
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
name|SECRET
argument_list|,
name|CONFIDENTIAL
argument_list|)
argument_list|)
expr_stmt|;
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
init|;
name|Table
name|t
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
init|)
block|{
name|ResultScanner
name|scanner
init|=
name|t
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|Result
index|[]
name|result
init|=
name|scanner
operator|.
name|next
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|length
operator|==
literal|2
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|SUPERUSER
operator|.
name|runAs
argument_list|(
name|scanAction
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetForSuperUserWithFewerLabelAuths
parameter_list|()
throws|throws
name|Throwable
block|{
name|String
index|[]
name|auths
init|=
block|{
name|SECRET
block|}
decl_stmt|;
name|String
name|user
init|=
literal|"admin"
decl_stmt|;
name|VisibilityClient
operator|.
name|setAuths
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|auths
argument_list|,
name|user
argument_list|)
expr_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|TEST_NAME
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Table
name|table
init|=
name|createTableAndWriteDataWithLabels
argument_list|(
name|tableName
argument_list|,
name|SECRET
operator|+
literal|"&"
operator|+
name|CONFIDENTIAL
operator|+
literal|"&!"
operator|+
name|PRIVATE
argument_list|,
name|SECRET
operator|+
literal|"&!"
operator|+
name|PRIVATE
argument_list|)
decl_stmt|;
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
name|scanAction
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
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
name|row1
argument_list|)
decl_stmt|;
name|g
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
name|SECRET
argument_list|,
name|CONFIDENTIAL
argument_list|)
argument_list|)
expr_stmt|;
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
init|;
name|Table
name|t
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
init|)
block|{
name|Result
name|result
init|=
name|t
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
operator|!
name|result
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|SUPERUSER
operator|.
name|runAs
argument_list|(
name|scanAction
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testVisibilityLabelsForUserWithNoAuths
parameter_list|()
throws|throws
name|Throwable
block|{
name|String
name|user
init|=
literal|"admin"
decl_stmt|;
name|String
index|[]
name|auths
init|=
block|{
name|SECRET
block|}
decl_stmt|;
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
name|VisibilityClient
operator|.
name|clearAuths
argument_list|(
name|conn
argument_list|,
name|auths
argument_list|,
name|user
argument_list|)
expr_stmt|;
comment|// Removing all auths if any.
name|VisibilityClient
operator|.
name|setAuths
argument_list|(
name|conn
argument_list|,
name|auths
argument_list|,
literal|"user1"
argument_list|)
expr_stmt|;
block|}
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|TEST_NAME
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Table
name|table
init|=
name|createTableAndWriteDataWithLabels
argument_list|(
name|tableName
argument_list|,
name|SECRET
argument_list|)
decl_stmt|;
name|SecureTestUtil
operator|.
name|grantOnTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|NORMAL_USER1
operator|.
name|getShortName
argument_list|()
argument_list|,
name|tableName
argument_list|,
literal|null
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
name|SecureTestUtil
operator|.
name|grantOnTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|NORMAL_USER2
operator|.
name|getShortName
argument_list|()
argument_list|,
name|tableName
argument_list|,
literal|null
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
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
name|getAction
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
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
name|row1
argument_list|)
decl_stmt|;
name|g
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
name|SECRET
argument_list|,
name|CONFIDENTIAL
argument_list|)
argument_list|)
expr_stmt|;
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
init|;
name|Table
name|t
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
init|)
block|{
name|Result
name|result
init|=
name|t
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|NORMAL_USER2
operator|.
name|runAs
argument_list|(
name|getAction
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLabelsTableOpsWithDifferentUsers
parameter_list|()
throws|throws
name|Throwable
block|{
name|PrivilegedExceptionAction
argument_list|<
name|VisibilityLabelsResponse
argument_list|>
name|action
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|VisibilityLabelsResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|VisibilityLabelsResponse
name|run
parameter_list|()
throws|throws
name|Exception
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
return|return
name|VisibilityClient
operator|.
name|addLabels
argument_list|(
name|conn
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"l1"
block|,
literal|"l2"
block|}
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{         }
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|VisibilityLabelsResponse
name|response
init|=
name|NORMAL_USER1
operator|.
name|runAs
argument_list|(
name|action
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"org.apache.hadoop.hbase.security.AccessDeniedException"
argument_list|,
name|response
operator|.
name|getResult
argument_list|(
literal|0
argument_list|)
operator|.
name|getException
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"org.apache.hadoop.hbase.security.AccessDeniedException"
argument_list|,
name|response
operator|.
name|getResult
argument_list|(
literal|1
argument_list|)
operator|.
name|getException
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|action
operator|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|VisibilityLabelsResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|VisibilityLabelsResponse
name|run
parameter_list|()
throws|throws
name|Exception
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
return|return
name|VisibilityClient
operator|.
name|setAuths
argument_list|(
name|conn
argument_list|,
operator|new
name|String
index|[]
block|{
name|CONFIDENTIAL
block|,
name|PRIVATE
block|}
argument_list|,
literal|"user1"
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{         }
return|return
literal|null
return|;
block|}
block|}
expr_stmt|;
name|response
operator|=
name|NORMAL_USER1
operator|.
name|runAs
argument_list|(
name|action
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"org.apache.hadoop.hbase.security.AccessDeniedException"
argument_list|,
name|response
operator|.
name|getResult
argument_list|(
literal|0
argument_list|)
operator|.
name|getException
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"org.apache.hadoop.hbase.security.AccessDeniedException"
argument_list|,
name|response
operator|.
name|getResult
argument_list|(
literal|1
argument_list|)
operator|.
name|getException
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|action
operator|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|VisibilityLabelsResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|VisibilityLabelsResponse
name|run
parameter_list|()
throws|throws
name|Exception
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
return|return
name|VisibilityClient
operator|.
name|setAuths
argument_list|(
name|conn
argument_list|,
operator|new
name|String
index|[]
block|{
name|CONFIDENTIAL
block|,
name|PRIVATE
block|}
argument_list|,
literal|"user1"
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{         }
return|return
literal|null
return|;
block|}
block|}
expr_stmt|;
name|response
operator|=
name|SUPERUSER
operator|.
name|runAs
argument_list|(
name|action
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|response
operator|.
name|getResult
argument_list|(
literal|0
argument_list|)
operator|.
name|getException
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|response
operator|.
name|getResult
argument_list|(
literal|1
argument_list|)
operator|.
name|getException
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|action
operator|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|VisibilityLabelsResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|VisibilityLabelsResponse
name|run
parameter_list|()
throws|throws
name|Exception
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
return|return
name|VisibilityClient
operator|.
name|clearAuths
argument_list|(
name|conn
argument_list|,
operator|new
name|String
index|[]
block|{
name|CONFIDENTIAL
block|,
name|PRIVATE
block|}
argument_list|,
literal|"user1"
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{         }
return|return
literal|null
return|;
block|}
block|}
expr_stmt|;
name|response
operator|=
name|NORMAL_USER1
operator|.
name|runAs
argument_list|(
name|action
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"org.apache.hadoop.hbase.security.AccessDeniedException"
argument_list|,
name|response
operator|.
name|getResult
argument_list|(
literal|0
argument_list|)
operator|.
name|getException
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"org.apache.hadoop.hbase.security.AccessDeniedException"
argument_list|,
name|response
operator|.
name|getResult
argument_list|(
literal|1
argument_list|)
operator|.
name|getException
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|response
operator|=
name|VisibilityClient
operator|.
name|clearAuths
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
operator|new
name|String
index|[]
block|{
name|CONFIDENTIAL
block|,
name|PRIVATE
block|}
argument_list|,
literal|"user1"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|response
operator|.
name|getResult
argument_list|(
literal|0
argument_list|)
operator|.
name|getException
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|response
operator|.
name|getResult
argument_list|(
literal|1
argument_list|)
operator|.
name|getException
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|VisibilityClient
operator|.
name|setAuths
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
operator|new
name|String
index|[]
block|{
name|CONFIDENTIAL
block|,
name|PRIVATE
block|}
argument_list|,
literal|"user3"
argument_list|)
expr_stmt|;
name|PrivilegedExceptionAction
argument_list|<
name|GetAuthsResponse
argument_list|>
name|action1
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|GetAuthsResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|GetAuthsResponse
name|run
parameter_list|()
throws|throws
name|Exception
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
return|return
name|VisibilityClient
operator|.
name|getAuths
argument_list|(
name|conn
argument_list|,
literal|"user3"
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{         }
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|GetAuthsResponse
name|authsResponse
init|=
name|NORMAL_USER1
operator|.
name|runAs
argument_list|(
name|action1
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|authsResponse
argument_list|)
expr_stmt|;
name|authsResponse
operator|=
name|SUPERUSER
operator|.
name|runAs
argument_list|(
name|action1
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|authsList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|authsResponse
operator|.
name|getAuthList
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|ByteString
name|authBS
range|:
name|authsResponse
operator|.
name|getAuthList
argument_list|()
control|)
block|{
name|authsList
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|authBS
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|authsList
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|authsList
operator|.
name|contains
argument_list|(
name|CONFIDENTIAL
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|authsList
operator|.
name|contains
argument_list|(
name|PRIVATE
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|Table
name|createTableAndWriteDataWithLabels
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|String
modifier|...
name|labelExps
parameter_list|)
throws|throws
name|Exception
block|{
name|Table
name|table
init|=
literal|null
decl_stmt|;
try|try
block|{
name|table
operator|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|fam
argument_list|)
expr_stmt|;
name|int
name|i
init|=
literal|1
decl_stmt|;
name|List
argument_list|<
name|Put
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|labelExps
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|labelExp
range|:
name|labelExps
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
literal|"row"
operator|+
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|fam
argument_list|,
name|qual
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|put
operator|.
name|setCellVisibility
argument_list|(
operator|new
name|CellVisibility
argument_list|(
name|labelExp
argument_list|)
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
name|table
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|table
return|;
block|}
specifier|private
specifier|static
name|void
name|addLabels
parameter_list|()
throws|throws
name|IOException
block|{
name|String
index|[]
name|labels
init|=
block|{
name|SECRET
block|,
name|CONFIDENTIAL
block|,
name|PRIVATE
block|}
decl_stmt|;
try|try
block|{
name|VisibilityClient
operator|.
name|addLabels
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|labels
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|t
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

