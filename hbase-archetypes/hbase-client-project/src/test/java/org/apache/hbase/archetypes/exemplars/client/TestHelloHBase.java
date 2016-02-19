begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|archetypes
operator|.
name|exemplars
operator|.
name|client
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

begin_comment
comment|/**  * Unit testing for HelloHBase.  */
end_comment

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
name|TestHelloHBase
block|{
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
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
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
name|testNamespaceExists
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|NONEXISTENT_NAMESPACE
init|=
literal|"xyzpdq_nonexistent"
decl_stmt|;
specifier|final
name|String
name|EXISTING_NAMESPACE
init|=
literal|"pdqxyz_myExistingNamespace"
decl_stmt|;
name|boolean
name|exists
decl_stmt|;
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|exists
operator|=
name|HelloHBase
operator|.
name|namespaceExists
argument_list|(
name|admin
argument_list|,
name|NONEXISTENT_NAMESPACE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"#namespaceExists failed: found nonexistent namespace."
argument_list|,
literal|false
argument_list|,
name|exists
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|EXISTING_NAMESPACE
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|exists
operator|=
name|HelloHBase
operator|.
name|namespaceExists
argument_list|(
name|admin
argument_list|,
name|EXISTING_NAMESPACE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"#namespaceExists failed: did NOT find existing namespace."
argument_list|,
literal|true
argument_list|,
name|exists
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteNamespace
argument_list|(
name|EXISTING_NAMESPACE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateNamespaceAndTable
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
name|HelloHBase
operator|.
name|createNamespaceAndTable
argument_list|(
name|admin
argument_list|)
expr_stmt|;
name|boolean
name|namespaceExists
init|=
name|HelloHBase
operator|.
name|namespaceExists
argument_list|(
name|admin
argument_list|,
name|HelloHBase
operator|.
name|MY_NAMESPACE_NAME
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"#createNamespaceAndTable failed to create namespace."
argument_list|,
literal|true
argument_list|,
name|namespaceExists
argument_list|)
expr_stmt|;
name|boolean
name|tableExists
init|=
name|admin
operator|.
name|tableExists
argument_list|(
name|HelloHBase
operator|.
name|MY_TABLE_NAME
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"#createNamespaceAndTable failed to create table."
argument_list|,
literal|true
argument_list|,
name|tableExists
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|HelloHBase
operator|.
name|MY_TABLE_NAME
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|HelloHBase
operator|.
name|MY_TABLE_NAME
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteNamespace
argument_list|(
name|HelloHBase
operator|.
name|MY_NAMESPACE_NAME
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPutRowToTable
parameter_list|()
throws|throws
name|IOException
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|HelloHBase
operator|.
name|MY_NAMESPACE_NAME
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|HelloHBase
operator|.
name|MY_TABLE_NAME
argument_list|,
name|HelloHBase
operator|.
name|MY_COLUMN_FAMILY_NAME
argument_list|)
decl_stmt|;
name|HelloHBase
operator|.
name|putRowToTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|Result
name|row
init|=
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|HelloHBase
operator|.
name|MY_ROW_ID
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"#putRowToTable failed to store row."
argument_list|,
literal|false
argument_list|,
name|row
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|HelloHBase
operator|.
name|MY_TABLE_NAME
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteNamespace
argument_list|(
name|HelloHBase
operator|.
name|MY_NAMESPACE_NAME
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteRow
parameter_list|()
throws|throws
name|IOException
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|HelloHBase
operator|.
name|MY_NAMESPACE_NAME
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|HelloHBase
operator|.
name|MY_TABLE_NAME
argument_list|,
name|HelloHBase
operator|.
name|MY_COLUMN_FAMILY_NAME
argument_list|)
decl_stmt|;
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|HelloHBase
operator|.
name|MY_ROW_ID
argument_list|)
operator|.
name|addColumn
argument_list|(
name|HelloHBase
operator|.
name|MY_COLUMN_FAMILY_NAME
argument_list|,
name|HelloHBase
operator|.
name|MY_FIRST_COLUMN_QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"xyz"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|HelloHBase
operator|.
name|deleteRow
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|Result
name|row
init|=
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|HelloHBase
operator|.
name|MY_ROW_ID
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"#deleteRow failed to delete row."
argument_list|,
literal|true
argument_list|,
name|row
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|HelloHBase
operator|.
name|MY_TABLE_NAME
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteNamespace
argument_list|(
name|HelloHBase
operator|.
name|MY_NAMESPACE_NAME
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

