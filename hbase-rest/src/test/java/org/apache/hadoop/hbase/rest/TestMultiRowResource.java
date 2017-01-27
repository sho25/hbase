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
name|hadoop
operator|.
name|hbase
operator|.
name|rest
package|;
end_package

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
name|*
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
name|rest
operator|.
name|model
operator|.
name|CellModel
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
name|model
operator|.
name|CellSetModel
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
name|model
operator|.
name|RowModel
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
name|provider
operator|.
name|JacksonProvider
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
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|ObjectMapper
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
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|core
operator|.
name|MediaType
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|JAXBContext
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|JAXBException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|Marshaller
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|Unmarshaller
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
name|Collection
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
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestMultiRowResource
block|{
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
literal|"TestRowResource"
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
name|CFB
init|=
literal|"b"
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
name|CFB
operator|+
literal|":2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ROW_1
init|=
literal|"testrow5"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|VALUE_1
init|=
literal|"testvalue5"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ROW_2
init|=
literal|"testrow6"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|VALUE_2
init|=
literal|"testvalue6"
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
specifier|private
specifier|static
name|JAXBContext
name|context
decl_stmt|;
specifier|private
specifier|static
name|Marshaller
name|marshaller
decl_stmt|;
specifier|private
specifier|static
name|Unmarshaller
name|unmarshaller
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
name|Header
name|extraHdr
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|csrfEnabled
init|=
literal|true
decl_stmt|;
annotation|@
name|Parameterized
operator|.
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|data
parameter_list|()
block|{
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|params
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
index|[]
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|params
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
name|Boolean
operator|.
name|TRUE
block|}
argument_list|)
expr_stmt|;
name|params
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
name|Boolean
operator|.
name|FALSE
block|}
argument_list|)
expr_stmt|;
return|return
name|params
return|;
block|}
specifier|public
name|TestMultiRowResource
parameter_list|(
name|Boolean
name|csrf
parameter_list|)
block|{
name|csrfEnabled
operator|=
name|csrf
expr_stmt|;
block|}
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
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|RESTServer
operator|.
name|REST_CSRF_ENABLED_KEY
argument_list|,
name|csrfEnabled
argument_list|)
expr_stmt|;
name|extraHdr
operator|=
operator|new
name|BasicHeader
argument_list|(
name|RESTServer
operator|.
name|REST_CSRF_CUSTOM_HEADER_DEFAULT
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|REST_TEST_UTIL
operator|.
name|startServletContainer
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|context
operator|=
name|JAXBContext
operator|.
name|newInstance
argument_list|(
name|CellModel
operator|.
name|class
argument_list|,
name|CellSetModel
operator|.
name|class
argument_list|,
name|RowModel
operator|.
name|class
argument_list|)
expr_stmt|;
name|marshaller
operator|=
name|context
operator|.
name|createMarshaller
argument_list|()
expr_stmt|;
name|unmarshaller
operator|=
name|context
operator|.
name|createUnmarshaller
argument_list|()
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
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|CFA
argument_list|)
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|CFB
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|htd
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
name|testMultiCellGetJSON
parameter_list|()
throws|throws
name|IOException
throws|,
name|JAXBException
block|{
name|String
name|row_5_url
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
name|String
name|row_6_url
init|=
literal|"/"
operator|+
name|TABLE
operator|+
literal|"/"
operator|+
name|ROW_2
operator|+
literal|"/"
operator|+
name|COLUMN_2
decl_stmt|;
name|StringBuilder
name|path
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|"/"
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|TABLE
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|"/multiget/?row="
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|ROW_1
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|"&row="
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|ROW_2
argument_list|)
expr_stmt|;
if|if
condition|(
name|csrfEnabled
condition|)
block|{
name|Response
name|response
init|=
name|client
operator|.
name|post
argument_list|(
name|row_5_url
argument_list|,
name|Constants
operator|.
name|MIMETYPE_BINARY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_1
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|400
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|client
operator|.
name|post
argument_list|(
name|row_5_url
argument_list|,
name|Constants
operator|.
name|MIMETYPE_BINARY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_1
argument_list|)
argument_list|,
name|extraHdr
argument_list|)
expr_stmt|;
name|client
operator|.
name|post
argument_list|(
name|row_6_url
argument_list|,
name|Constants
operator|.
name|MIMETYPE_BINARY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_2
argument_list|)
argument_list|,
name|extraHdr
argument_list|)
expr_stmt|;
name|Response
name|response
init|=
name|client
operator|.
name|get
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|,
name|Constants
operator|.
name|MIMETYPE_JSON
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
argument_list|,
literal|200
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Constants
operator|.
name|MIMETYPE_JSON
argument_list|,
name|response
operator|.
name|getHeader
argument_list|(
literal|"content-type"
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|delete
argument_list|(
name|row_5_url
argument_list|,
name|extraHdr
argument_list|)
expr_stmt|;
name|client
operator|.
name|delete
argument_list|(
name|row_6_url
argument_list|,
name|extraHdr
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultiCellGetXML
parameter_list|()
throws|throws
name|IOException
throws|,
name|JAXBException
block|{
name|String
name|row_5_url
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
name|String
name|row_6_url
init|=
literal|"/"
operator|+
name|TABLE
operator|+
literal|"/"
operator|+
name|ROW_2
operator|+
literal|"/"
operator|+
name|COLUMN_2
decl_stmt|;
name|StringBuilder
name|path
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|"/"
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|TABLE
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|"/multiget/?row="
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|ROW_1
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|"&row="
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|ROW_2
argument_list|)
expr_stmt|;
name|client
operator|.
name|post
argument_list|(
name|row_5_url
argument_list|,
name|Constants
operator|.
name|MIMETYPE_BINARY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_1
argument_list|)
argument_list|,
name|extraHdr
argument_list|)
expr_stmt|;
name|client
operator|.
name|post
argument_list|(
name|row_6_url
argument_list|,
name|Constants
operator|.
name|MIMETYPE_BINARY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_2
argument_list|)
argument_list|,
name|extraHdr
argument_list|)
expr_stmt|;
name|Response
name|response
init|=
name|client
operator|.
name|get
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|,
name|Constants
operator|.
name|MIMETYPE_XML
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
argument_list|,
literal|200
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Constants
operator|.
name|MIMETYPE_XML
argument_list|,
name|response
operator|.
name|getHeader
argument_list|(
literal|"content-type"
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|delete
argument_list|(
name|row_5_url
argument_list|,
name|extraHdr
argument_list|)
expr_stmt|;
name|client
operator|.
name|delete
argument_list|(
name|row_6_url
argument_list|,
name|extraHdr
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultiCellGetWithColsJSON
parameter_list|()
throws|throws
name|IOException
throws|,
name|JAXBException
block|{
name|String
name|row_5_url
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
name|String
name|row_6_url
init|=
literal|"/"
operator|+
name|TABLE
operator|+
literal|"/"
operator|+
name|ROW_2
operator|+
literal|"/"
operator|+
name|COLUMN_2
decl_stmt|;
name|StringBuilder
name|path
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|"/"
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|TABLE
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|"/multiget"
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|"/"
operator|+
name|COLUMN_1
operator|+
literal|","
operator|+
name|CFB
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|"?row="
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|ROW_1
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|"&row="
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|ROW_2
argument_list|)
expr_stmt|;
name|client
operator|.
name|post
argument_list|(
name|row_5_url
argument_list|,
name|Constants
operator|.
name|MIMETYPE_BINARY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_1
argument_list|)
argument_list|,
name|extraHdr
argument_list|)
expr_stmt|;
name|client
operator|.
name|post
argument_list|(
name|row_6_url
argument_list|,
name|Constants
operator|.
name|MIMETYPE_BINARY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_2
argument_list|)
argument_list|,
name|extraHdr
argument_list|)
expr_stmt|;
name|Response
name|response
init|=
name|client
operator|.
name|get
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|,
name|Constants
operator|.
name|MIMETYPE_JSON
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
argument_list|,
literal|200
argument_list|)
expr_stmt|;
name|ObjectMapper
name|mapper
init|=
operator|new
name|JacksonProvider
argument_list|()
operator|.
name|locateMapper
argument_list|(
name|CellSetModel
operator|.
name|class
argument_list|,
name|MediaType
operator|.
name|APPLICATION_JSON_TYPE
argument_list|)
decl_stmt|;
name|CellSetModel
name|cellSet
init|=
operator|(
name|CellSetModel
operator|)
name|mapper
operator|.
name|readValue
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|,
name|CellSetModel
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|cellSet
operator|.
name|getRows
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ROW_1
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|cellSet
operator|.
name|getRows
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|VALUE_1
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|cellSet
operator|.
name|getRows
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getCells
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ROW_2
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|cellSet
operator|.
name|getRows
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|VALUE_2
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|cellSet
operator|.
name|getRows
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getCells
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|delete
argument_list|(
name|row_5_url
argument_list|,
name|extraHdr
argument_list|)
expr_stmt|;
name|client
operator|.
name|delete
argument_list|(
name|row_6_url
argument_list|,
name|extraHdr
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultiCellGetJSONNotFound
parameter_list|()
throws|throws
name|IOException
throws|,
name|JAXBException
block|{
name|String
name|row_5_url
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
name|StringBuilder
name|path
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|"/"
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|TABLE
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|"/multiget/?row="
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|ROW_1
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|"&row="
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|ROW_2
argument_list|)
expr_stmt|;
name|client
operator|.
name|post
argument_list|(
name|row_5_url
argument_list|,
name|Constants
operator|.
name|MIMETYPE_BINARY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_1
argument_list|)
argument_list|,
name|extraHdr
argument_list|)
expr_stmt|;
name|Response
name|response
init|=
name|client
operator|.
name|get
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|,
name|Constants
operator|.
name|MIMETYPE_JSON
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
argument_list|,
literal|200
argument_list|)
expr_stmt|;
name|ObjectMapper
name|mapper
init|=
operator|new
name|JacksonProvider
argument_list|()
operator|.
name|locateMapper
argument_list|(
name|CellSetModel
operator|.
name|class
argument_list|,
name|MediaType
operator|.
name|APPLICATION_JSON_TYPE
argument_list|)
decl_stmt|;
name|CellSetModel
name|cellSet
init|=
operator|(
name|CellSetModel
operator|)
name|mapper
operator|.
name|readValue
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|,
name|CellSetModel
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|cellSet
operator|.
name|getRows
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ROW_1
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|cellSet
operator|.
name|getRows
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|VALUE_1
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|cellSet
operator|.
name|getRows
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getCells
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|delete
argument_list|(
name|row_5_url
argument_list|,
name|extraHdr
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

