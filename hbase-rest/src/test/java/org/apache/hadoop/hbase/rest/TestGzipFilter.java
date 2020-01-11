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
name|rest
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
name|assertNotNull
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
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|zip
operator|.
name|GZIPInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|zip
operator|.
name|GZIPOutputStream
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
name|ColumnFamilyDescriptor
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
name|rest
operator|.
name|client
operator|.
name|Client
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
name|rest
operator|.
name|client
operator|.
name|Cluster
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
name|rest
operator|.
name|client
operator|.
name|Response
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
name|RestTests
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
name|http
operator|.
name|Header
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|message
operator|.
name|BasicHeader
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
block|{
name|RestTests
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
name|TestGzipFilter
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
name|TestGzipFilter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|TABLE
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestGzipFilter"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CFA
init|=
literal|"a"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COLUMN_1
init|=
name|CFA
operator|+
literal|":1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COLUMN_2
init|=
name|CFA
operator|+
literal|":2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ROW_1
init|=
literal|"testrow1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VALUE_1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testvalue1"
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
specifier|final
name|HBaseRESTTestingUtility
name|REST_TEST_UTIL
init|=
operator|new
name|HBaseRESTTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Client
name|client
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|REST_TEST_UTIL
operator|.
name|startServletContainer
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|client
operator|=
operator|new
name|Client
argument_list|(
operator|new
name|Cluster
argument_list|()
operator|.
name|add
argument_list|(
literal|"localhost"
argument_list|,
name|REST_TEST_UTIL
operator|.
name|getServletPort
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|TABLE
argument_list|)
condition|)
block|{
return|return;
block|}
name|TableDescriptorBuilder
name|tableDescriptorBuilder
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TABLE
argument_list|)
decl_stmt|;
name|ColumnFamilyDescriptor
name|columnFamilyDescriptor
init|=
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|CFA
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|tableDescriptorBuilder
operator|.
name|setColumnFamily
argument_list|(
name|columnFamilyDescriptor
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|tableDescriptorBuilder
operator|.
name|build
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
name|REST_TEST_UTIL
operator|.
name|shutdownServletContainer
argument_list|()
expr_stmt|;
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
name|testGzipFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|path
init|=
literal|"/"
operator|+
name|TABLE
operator|+
literal|"/"
operator|+
name|ROW_1
operator|+
literal|"/"
operator|+
name|COLUMN_1
decl_stmt|;
name|ByteArrayOutputStream
name|bos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|GZIPOutputStream
name|os
init|=
operator|new
name|GZIPOutputStream
argument_list|(
name|bos
argument_list|)
decl_stmt|;
name|os
operator|.
name|write
argument_list|(
name|VALUE_1
argument_list|)
expr_stmt|;
name|os
operator|.
name|close
argument_list|()
expr_stmt|;
name|byte
index|[]
name|value_1_gzip
init|=
name|bos
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
comment|// input side filter
name|Header
index|[]
name|headers
init|=
operator|new
name|Header
index|[
literal|2
index|]
decl_stmt|;
name|headers
index|[
literal|0
index|]
operator|=
operator|new
name|BasicHeader
argument_list|(
literal|"Content-Type"
argument_list|,
name|Constants
operator|.
name|MIMETYPE_BINARY
argument_list|)
expr_stmt|;
name|headers
index|[
literal|1
index|]
operator|=
operator|new
name|BasicHeader
argument_list|(
literal|"Content-Encoding"
argument_list|,
literal|"gzip"
argument_list|)
expr_stmt|;
name|Response
name|response
init|=
name|client
operator|.
name|put
argument_list|(
name|path
argument_list|,
name|headers
argument_list|,
name|value_1_gzip
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TABLE
argument_list|)
decl_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|ROW_1
argument_list|)
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|CFA
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
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
name|byte
index|[]
name|value
init|=
name|result
operator|.
name|getValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|CFA
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|value
argument_list|,
name|VALUE_1
argument_list|)
argument_list|)
expr_stmt|;
comment|// output side filter
name|headers
index|[
literal|0
index|]
operator|=
operator|new
name|BasicHeader
argument_list|(
literal|"Accept"
argument_list|,
name|Constants
operator|.
name|MIMETYPE_BINARY
argument_list|)
expr_stmt|;
name|headers
index|[
literal|1
index|]
operator|=
operator|new
name|BasicHeader
argument_list|(
literal|"Accept-Encoding"
argument_list|,
literal|"gzip"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|path
argument_list|,
name|headers
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
name|ByteArrayInputStream
name|bis
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
decl_stmt|;
name|GZIPInputStream
name|is
init|=
operator|new
name|GZIPInputStream
argument_list|(
name|bis
argument_list|)
decl_stmt|;
name|value
operator|=
operator|new
name|byte
index|[
name|VALUE_1
operator|.
name|length
index|]
expr_stmt|;
name|is
operator|.
name|read
argument_list|(
name|value
argument_list|,
literal|0
argument_list|,
name|VALUE_1
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|value
argument_list|,
name|VALUE_1
argument_list|)
argument_list|)
expr_stmt|;
name|is
operator|.
name|close
argument_list|()
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|testScannerResultCodes
argument_list|()
expr_stmt|;
block|}
name|void
name|testScannerResultCodes
parameter_list|()
throws|throws
name|Exception
block|{
name|Header
index|[]
name|headers
init|=
operator|new
name|Header
index|[
literal|3
index|]
decl_stmt|;
name|headers
index|[
literal|0
index|]
operator|=
operator|new
name|BasicHeader
argument_list|(
literal|"Content-Type"
argument_list|,
name|Constants
operator|.
name|MIMETYPE_XML
argument_list|)
expr_stmt|;
name|headers
index|[
literal|1
index|]
operator|=
operator|new
name|BasicHeader
argument_list|(
literal|"Accept"
argument_list|,
name|Constants
operator|.
name|MIMETYPE_JSON
argument_list|)
expr_stmt|;
name|headers
index|[
literal|2
index|]
operator|=
operator|new
name|BasicHeader
argument_list|(
literal|"Accept-Encoding"
argument_list|,
literal|"gzip"
argument_list|)
expr_stmt|;
name|Response
name|response
init|=
name|client
operator|.
name|post
argument_list|(
literal|"/"
operator|+
name|TABLE
operator|+
literal|"/scanner"
argument_list|,
name|headers
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"<Scanner/>"
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|201
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|scannerUrl
init|=
name|response
operator|.
name|getLocation
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|scannerUrl
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|scannerUrl
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|scannerUrl
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|204
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

