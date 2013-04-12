begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|httpclient
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
name|Durability
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
name|model
operator|.
name|ScannerModel
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
name|TestScannerResource
block|{
specifier|private
specifier|static
specifier|final
name|String
name|TABLE
init|=
literal|"TestScannerResource"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|NONEXISTENT_TABLE
init|=
literal|"ThisTableDoesNotExist"
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
name|int
name|expectedRows1
decl_stmt|;
specifier|private
specifier|static
name|int
name|expectedRows2
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
name|int
name|insertData
parameter_list|(
name|String
name|tableName
parameter_list|,
name|String
name|column
parameter_list|,
name|double
name|prob
parameter_list|)
throws|throws
name|IOException
block|{
name|Random
name|rng
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|byte
index|[]
name|k
init|=
operator|new
name|byte
index|[
literal|3
index|]
decl_stmt|;
name|byte
index|[]
index|[]
name|famAndQf
init|=
name|KeyValue
operator|.
name|parseColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|column
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
name|b1
init|=
literal|'a'
init|;
name|b1
operator|<
literal|'z'
condition|;
name|b1
operator|++
control|)
block|{
for|for
control|(
name|byte
name|b2
init|=
literal|'a'
init|;
name|b2
operator|<
literal|'z'
condition|;
name|b2
operator|++
control|)
block|{
for|for
control|(
name|byte
name|b3
init|=
literal|'a'
init|;
name|b3
operator|<
literal|'z'
condition|;
name|b3
operator|++
control|)
block|{
if|if
condition|(
name|rng
operator|.
name|nextDouble
argument_list|()
operator|<
name|prob
condition|)
block|{
name|k
index|[
literal|0
index|]
operator|=
name|b1
expr_stmt|;
name|k
index|[
literal|1
index|]
operator|=
name|b2
expr_stmt|;
name|k
index|[
literal|2
index|]
operator|=
name|b3
expr_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|k
argument_list|)
decl_stmt|;
name|put
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|famAndQf
index|[
literal|0
index|]
argument_list|,
name|famAndQf
index|[
literal|1
index|]
argument_list|,
name|k
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
block|}
block|}
block|}
name|table
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
return|return
name|count
return|;
block|}
specifier|private
specifier|static
name|int
name|countCellSet
parameter_list|(
name|CellSetModel
name|model
parameter_list|)
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
name|Iterator
argument_list|<
name|RowModel
argument_list|>
name|rows
init|=
name|model
operator|.
name|getRows
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|rows
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|RowModel
name|row
init|=
name|rows
operator|.
name|next
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|CellModel
argument_list|>
name|cells
init|=
name|row
operator|.
name|getCells
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|cells
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|cells
operator|.
name|next
argument_list|()
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
block|}
return|return
name|count
return|;
block|}
specifier|private
specifier|static
name|int
name|fullTableScan
parameter_list|(
name|ScannerModel
name|model
parameter_list|)
throws|throws
name|IOException
block|{
name|model
operator|.
name|setBatch
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|Response
name|response
init|=
name|client
operator|.
name|put
argument_list|(
literal|"/"
operator|+
name|TABLE
operator|+
literal|"/scanner"
argument_list|,
name|Constants
operator|.
name|MIMETYPE_PROTOBUF
argument_list|,
name|model
operator|.
name|createProtobufOutput
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
argument_list|,
literal|201
argument_list|)
expr_stmt|;
name|String
name|scannerURI
init|=
name|response
operator|.
name|getLocation
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|scannerURI
argument_list|)
expr_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|scannerURI
argument_list|,
name|Constants
operator|.
name|MIMETYPE_PROTOBUF
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
operator|==
literal|200
operator|||
name|response
operator|.
name|getCode
argument_list|()
operator|==
literal|204
argument_list|)
expr_stmt|;
if|if
condition|(
name|response
operator|.
name|getCode
argument_list|()
operator|==
literal|200
condition|)
block|{
name|assertEquals
argument_list|(
name|Constants
operator|.
name|MIMETYPE_PROTOBUF
argument_list|,
name|response
operator|.
name|getHeader
argument_list|(
literal|"content-type"
argument_list|)
argument_list|)
expr_stmt|;
name|CellSetModel
name|cellSet
init|=
operator|new
name|CellSetModel
argument_list|()
decl_stmt|;
name|cellSet
operator|.
name|getObjectFromMessage
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|RowModel
argument_list|>
name|rows
init|=
name|cellSet
operator|.
name|getRows
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|rows
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|RowModel
name|row
init|=
name|rows
operator|.
name|next
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|CellModel
argument_list|>
name|cells
init|=
name|row
operator|.
name|getCells
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|cells
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|cells
operator|.
name|next
argument_list|()
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
break|break;
block|}
block|}
comment|// delete the scanner
name|response
operator|=
name|client
operator|.
name|delete
argument_list|(
name|scannerURI
argument_list|)
expr_stmt|;
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
return|return
name|count
return|;
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
argument_list|,
name|ScannerModel
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
name|HBaseAdmin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
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
name|expectedRows1
operator|=
name|insertData
argument_list|(
name|TABLE
argument_list|,
name|COLUMN_1
argument_list|,
literal|1.0
argument_list|)
expr_stmt|;
name|expectedRows2
operator|=
name|insertData
argument_list|(
name|TABLE
argument_list|,
name|COLUMN_2
argument_list|,
literal|0.5
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
name|testSimpleScannerXML
parameter_list|()
throws|throws
name|IOException
throws|,
name|JAXBException
block|{
specifier|final
name|int
name|BATCH_SIZE
init|=
literal|5
decl_stmt|;
comment|// new scanner
name|ScannerModel
name|model
init|=
operator|new
name|ScannerModel
argument_list|()
decl_stmt|;
name|model
operator|.
name|setBatch
argument_list|(
name|BATCH_SIZE
argument_list|)
expr_stmt|;
name|model
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN_1
argument_list|)
argument_list|)
expr_stmt|;
name|StringWriter
name|writer
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|marshaller
operator|.
name|marshal
argument_list|(
name|model
argument_list|,
name|writer
argument_list|)
expr_stmt|;
name|byte
index|[]
name|body
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|writer
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
comment|// test put operation is forbidden in read-only mode
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.rest.readonly"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|Response
name|response
init|=
name|client
operator|.
name|put
argument_list|(
literal|"/"
operator|+
name|TABLE
operator|+
literal|"/scanner"
argument_list|,
name|Constants
operator|.
name|MIMETYPE_XML
argument_list|,
name|body
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
argument_list|,
literal|403
argument_list|)
expr_stmt|;
name|String
name|scannerURI
init|=
name|response
operator|.
name|getLocation
argument_list|()
decl_stmt|;
name|assertNull
argument_list|(
name|scannerURI
argument_list|)
expr_stmt|;
comment|// recall previous put operation with read-only off
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.rest.readonly"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|put
argument_list|(
literal|"/"
operator|+
name|TABLE
operator|+
literal|"/scanner"
argument_list|,
name|Constants
operator|.
name|MIMETYPE_XML
argument_list|,
name|body
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
argument_list|,
literal|201
argument_list|)
expr_stmt|;
name|scannerURI
operator|=
name|response
operator|.
name|getLocation
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|scannerURI
argument_list|)
expr_stmt|;
comment|// get a cell set
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|scannerURI
argument_list|,
name|Constants
operator|.
name|MIMETYPE_XML
argument_list|)
expr_stmt|;
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
name|CellSetModel
name|cellSet
init|=
operator|(
name|CellSetModel
operator|)
name|unmarshaller
operator|.
name|unmarshal
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
comment|// confirm batch size conformance
name|assertEquals
argument_list|(
name|countCellSet
argument_list|(
name|cellSet
argument_list|)
argument_list|,
name|BATCH_SIZE
argument_list|)
expr_stmt|;
comment|// test delete scanner operation is forbidden in read-only mode
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.rest.readonly"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|delete
argument_list|(
name|scannerURI
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
argument_list|,
literal|403
argument_list|)
expr_stmt|;
comment|// recall previous delete scanner operation with read-only off
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.rest.readonly"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|delete
argument_list|(
name|scannerURI
argument_list|)
expr_stmt|;
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSimpleScannerPB
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|int
name|BATCH_SIZE
init|=
literal|10
decl_stmt|;
comment|// new scanner
name|ScannerModel
name|model
init|=
operator|new
name|ScannerModel
argument_list|()
decl_stmt|;
name|model
operator|.
name|setBatch
argument_list|(
name|BATCH_SIZE
argument_list|)
expr_stmt|;
name|model
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN_1
argument_list|)
argument_list|)
expr_stmt|;
comment|// test put operation is forbidden in read-only mode
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.rest.readonly"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|Response
name|response
init|=
name|client
operator|.
name|put
argument_list|(
literal|"/"
operator|+
name|TABLE
operator|+
literal|"/scanner"
argument_list|,
name|Constants
operator|.
name|MIMETYPE_PROTOBUF
argument_list|,
name|model
operator|.
name|createProtobufOutput
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
argument_list|,
literal|403
argument_list|)
expr_stmt|;
name|String
name|scannerURI
init|=
name|response
operator|.
name|getLocation
argument_list|()
decl_stmt|;
name|assertNull
argument_list|(
name|scannerURI
argument_list|)
expr_stmt|;
comment|// recall previous put operation with read-only off
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.rest.readonly"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|put
argument_list|(
literal|"/"
operator|+
name|TABLE
operator|+
literal|"/scanner"
argument_list|,
name|Constants
operator|.
name|MIMETYPE_PROTOBUF
argument_list|,
name|model
operator|.
name|createProtobufOutput
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
argument_list|,
literal|201
argument_list|)
expr_stmt|;
name|scannerURI
operator|=
name|response
operator|.
name|getLocation
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|scannerURI
argument_list|)
expr_stmt|;
comment|// get a cell set
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|scannerURI
argument_list|,
name|Constants
operator|.
name|MIMETYPE_PROTOBUF
argument_list|)
expr_stmt|;
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
name|MIMETYPE_PROTOBUF
argument_list|,
name|response
operator|.
name|getHeader
argument_list|(
literal|"content-type"
argument_list|)
argument_list|)
expr_stmt|;
name|CellSetModel
name|cellSet
init|=
operator|new
name|CellSetModel
argument_list|()
decl_stmt|;
name|cellSet
operator|.
name|getObjectFromMessage
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
expr_stmt|;
comment|// confirm batch size conformance
name|assertEquals
argument_list|(
name|countCellSet
argument_list|(
name|cellSet
argument_list|)
argument_list|,
name|BATCH_SIZE
argument_list|)
expr_stmt|;
comment|// test delete scanner operation is forbidden in read-only mode
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.rest.readonly"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|delete
argument_list|(
name|scannerURI
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
argument_list|,
literal|403
argument_list|)
expr_stmt|;
comment|// recall previous delete scanner operation with read-only off
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.rest.readonly"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|delete
argument_list|(
name|scannerURI
argument_list|)
expr_stmt|;
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSimpleScannerBinary
parameter_list|()
throws|throws
name|IOException
block|{
comment|// new scanner
name|ScannerModel
name|model
init|=
operator|new
name|ScannerModel
argument_list|()
decl_stmt|;
name|model
operator|.
name|setBatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|model
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN_1
argument_list|)
argument_list|)
expr_stmt|;
comment|// test put operation is forbidden in read-only mode
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.rest.readonly"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|Response
name|response
init|=
name|client
operator|.
name|put
argument_list|(
literal|"/"
operator|+
name|TABLE
operator|+
literal|"/scanner"
argument_list|,
name|Constants
operator|.
name|MIMETYPE_PROTOBUF
argument_list|,
name|model
operator|.
name|createProtobufOutput
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
argument_list|,
literal|403
argument_list|)
expr_stmt|;
name|String
name|scannerURI
init|=
name|response
operator|.
name|getLocation
argument_list|()
decl_stmt|;
name|assertNull
argument_list|(
name|scannerURI
argument_list|)
expr_stmt|;
comment|// recall previous put operation with read-only off
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.rest.readonly"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|put
argument_list|(
literal|"/"
operator|+
name|TABLE
operator|+
literal|"/scanner"
argument_list|,
name|Constants
operator|.
name|MIMETYPE_PROTOBUF
argument_list|,
name|model
operator|.
name|createProtobufOutput
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
argument_list|,
literal|201
argument_list|)
expr_stmt|;
name|scannerURI
operator|=
name|response
operator|.
name|getLocation
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|scannerURI
argument_list|)
expr_stmt|;
comment|// get a cell
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|scannerURI
argument_list|,
name|Constants
operator|.
name|MIMETYPE_BINARY
argument_list|)
expr_stmt|;
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
name|MIMETYPE_BINARY
argument_list|,
name|response
operator|.
name|getHeader
argument_list|(
literal|"content-type"
argument_list|)
argument_list|)
expr_stmt|;
comment|// verify that data was returned
name|assertTrue
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
comment|// verify that the expected X-headers are present
name|boolean
name|foundRowHeader
init|=
literal|false
decl_stmt|,
name|foundColumnHeader
init|=
literal|false
decl_stmt|,
name|foundTimestampHeader
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Header
name|header
range|:
name|response
operator|.
name|getHeaders
argument_list|()
control|)
block|{
if|if
condition|(
name|header
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"X-Row"
argument_list|)
condition|)
block|{
name|foundRowHeader
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|header
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"X-Column"
argument_list|)
condition|)
block|{
name|foundColumnHeader
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|header
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"X-Timestamp"
argument_list|)
condition|)
block|{
name|foundTimestampHeader
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
name|foundRowHeader
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|foundColumnHeader
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|foundTimestampHeader
argument_list|)
expr_stmt|;
comment|// test delete scanner operation is forbidden in read-only mode
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.rest.readonly"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|delete
argument_list|(
name|scannerURI
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
argument_list|,
literal|403
argument_list|)
expr_stmt|;
comment|// recall previous delete scanner operation with read-only off
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.rest.readonly"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|delete
argument_list|(
name|scannerURI
argument_list|)
expr_stmt|;
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFullTableScan
parameter_list|()
throws|throws
name|IOException
block|{
name|ScannerModel
name|model
init|=
operator|new
name|ScannerModel
argument_list|()
decl_stmt|;
name|model
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN_1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|fullTableScan
argument_list|(
name|model
argument_list|)
argument_list|,
name|expectedRows1
argument_list|)
expr_stmt|;
name|model
operator|=
operator|new
name|ScannerModel
argument_list|()
expr_stmt|;
name|model
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN_2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|fullTableScan
argument_list|(
name|model
argument_list|)
argument_list|,
name|expectedRows2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableDoesNotExist
parameter_list|()
throws|throws
name|IOException
throws|,
name|JAXBException
block|{
name|ScannerModel
name|model
init|=
operator|new
name|ScannerModel
argument_list|()
decl_stmt|;
name|StringWriter
name|writer
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|marshaller
operator|.
name|marshal
argument_list|(
name|model
argument_list|,
name|writer
argument_list|)
expr_stmt|;
name|byte
index|[]
name|body
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|writer
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|Response
name|response
init|=
name|client
operator|.
name|put
argument_list|(
literal|"/"
operator|+
name|NONEXISTENT_TABLE
operator|+
literal|"/scanner"
argument_list|,
name|Constants
operator|.
name|MIMETYPE_XML
argument_list|,
name|body
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
argument_list|,
literal|404
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

