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
name|assertFalse
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
name|TestEnforcingScanLabelGenerator
block|{
specifier|public
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
specifier|public
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
name|ROW_1
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
name|CF
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|Q1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q1"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|Q2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q2"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|Q3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q3"
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
specifier|public
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
specifier|public
specifier|static
name|User
name|SUPERUSER
decl_stmt|;
specifier|public
specifier|static
name|User
name|TESTUSER
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
name|VisibilityTestUtil
operator|.
name|enableVisiblityLabels
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|String
name|classes
init|=
name|DefinedSetFilterScanLabelGenerator
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
operator|+
literal|" , "
operator|+
name|EnforcingScanLabelGenerator
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|VisibilityUtils
operator|.
name|VISIBILITY_LABEL_GENERATOR_CLASS
argument_list|,
name|classes
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.superuser"
argument_list|,
literal|"admin"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
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
name|TESTUSER
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"test"
argument_list|,
operator|new
name|String
index|[]
block|{ }
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
comment|// Set up for the test
name|SUPERUSER
operator|.
name|runAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
specifier|public
name|Void
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
name|SECRET
block|,
name|CONFIDENTIAL
block|}
argument_list|)
expr_stmt|;
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
block|, }
argument_list|,
name|TESTUSER
operator|.
name|getShortName
argument_list|()
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
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEnforcingScanLabelGenerator
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
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
name|SUPERUSER
operator|.
name|runAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
specifier|public
name|Void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
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
name|table
operator|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|CF
argument_list|)
init|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW_1
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|CF
argument_list|,
name|Q1
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
name|SECRET
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|ROW_1
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|CF
argument_list|,
name|Q2
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
name|CONFIDENTIAL
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|ROW_1
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|CF
argument_list|,
name|Q3
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
comment|// Test that super user can see all the cells.
name|SUPERUSER
operator|.
name|runAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
specifier|public
name|Void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
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
name|table
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
comment|// Test that super user can see all the cells.
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|ROW_1
argument_list|)
decl_stmt|;
name|Result
name|result
init|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Missing authorization"
argument_list|,
name|result
operator|.
name|containsColumn
argument_list|(
name|CF
argument_list|,
name|Q1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Missing authorization"
argument_list|,
name|result
operator|.
name|containsColumn
argument_list|(
name|CF
argument_list|,
name|Q2
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Missing authorization"
argument_list|,
name|result
operator|.
name|containsColumn
argument_list|(
name|CF
argument_list|,
name|Q3
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|TESTUSER
operator|.
name|runAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
specifier|public
name|Void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
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
name|table
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
comment|// Test that we enforce the defined set
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|ROW_1
argument_list|)
decl_stmt|;
name|get
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
operator|new
name|String
index|[]
block|{
name|SECRET
block|,
name|CONFIDENTIAL
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
literal|"Inappropriate authorization"
argument_list|,
name|result
operator|.
name|containsColumn
argument_list|(
name|CF
argument_list|,
name|Q1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Missing authorization"
argument_list|,
name|result
operator|.
name|containsColumn
argument_list|(
name|CF
argument_list|,
name|Q2
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Inappropriate filtering"
argument_list|,
name|result
operator|.
name|containsColumn
argument_list|(
name|CF
argument_list|,
name|Q3
argument_list|)
argument_list|)
expr_stmt|;
comment|// Test that we also enforce the defined set for the user if no auths are provided
name|get
operator|=
operator|new
name|Get
argument_list|(
name|ROW_1
argument_list|)
expr_stmt|;
name|result
operator|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Inappropriate authorization"
argument_list|,
name|result
operator|.
name|containsColumn
argument_list|(
name|CF
argument_list|,
name|Q1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Missing authorization"
argument_list|,
name|result
operator|.
name|containsColumn
argument_list|(
name|CF
argument_list|,
name|Q2
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Inappropriate filtering"
argument_list|,
name|result
operator|.
name|containsColumn
argument_list|(
name|CF
argument_list|,
name|Q3
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
block|}
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
block|}
end_class

end_unit

