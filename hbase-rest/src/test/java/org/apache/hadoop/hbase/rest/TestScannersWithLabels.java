begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|CellUtil
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
name|visibility
operator|.
name|CellVisibility
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
name|visibility
operator|.
name|ScanLabelGenerator
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
name|visibility
operator|.
name|SimpleScanLabelGenerator
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
name|visibility
operator|.
name|VisibilityClient
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
name|visibility
operator|.
name|VisibilityConstants
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
name|visibility
operator|.
name|VisibilityController
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
name|visibility
operator|.
name|VisibilityUtils
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
name|Iterator
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
name|TestScannersWithLabels
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
literal|"TestScannersWithLabels"
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
specifier|final
specifier|static
name|String
name|TOPSECRET
init|=
literal|"topsecret"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|PUBLIC
init|=
literal|"public"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|PRIVATE
init|=
literal|"private"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|CONFIDENTIAL
init|=
literal|"confidential"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|SECRET
init|=
literal|"secret"
decl_stmt|;
specifier|private
specifier|static
name|User
name|SUPERUSER
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
name|int
name|insertData
parameter_list|(
name|TableName
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
name|CellUtil
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
literal|9
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|9
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
literal|"row"
operator|+
name|i
argument_list|)
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
name|addColumn
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
name|put
operator|.
name|setCellVisibility
argument_list|(
operator|new
name|CellVisibility
argument_list|(
literal|"("
operator|+
name|SECRET
operator|+
literal|"|"
operator|+
name|CONFIDENTIAL
operator|+
literal|")"
operator|+
literal|"&"
operator|+
literal|"!"
operator|+
name|TOPSECRET
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
block|}
try|try
init|(
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
name|tableName
argument_list|)
init|)
block|{
name|table
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
block|}
return|return
name|puts
operator|.
name|size
argument_list|()
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
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setClass
argument_list|(
name|VisibilityUtils
operator|.
name|VISIBILITY_LABEL_GENERATOR_CLASS
argument_list|,
name|SimpleScanLabelGenerator
operator|.
name|class
argument_list|,
name|ScanLabelGenerator
operator|.
name|class
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hfile.format.version"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.superuser"
argument_list|,
name|SUPERUSER
operator|.
name|getShortName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.coprocessor.master.classes"
argument_list|,
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
literal|1
argument_list|)
expr_stmt|;
comment|// Wait for the labels table to become available
name|TEST_UTIL
operator|.
name|waitTableEnabled
argument_list|(
name|VisibilityConstants
operator|.
name|LABELS_TABLE_NAME
operator|.
name|getName
argument_list|()
argument_list|,
literal|50000
argument_list|)
expr_stmt|;
name|createLabels
argument_list|()
expr_stmt|;
name|setAuths
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
name|insertData
argument_list|(
name|TABLE
argument_list|,
name|COLUMN_1
argument_list|,
literal|1.0
argument_list|)
expr_stmt|;
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
specifier|private
specifier|static
name|void
name|createLabels
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
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
specifier|public
name|VisibilityLabelsResponse
name|run
parameter_list|()
throws|throws
name|Exception
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
block|,
name|PUBLIC
block|,
name|TOPSECRET
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
name|addLabels
argument_list|(
name|conn
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
name|action
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|setAuths
parameter_list|()
throws|throws
name|Exception
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
block|,
name|PUBLIC
block|,
name|TOPSECRET
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
name|setAuths
argument_list|(
name|conn
argument_list|,
name|labels
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSimpleScannerXMLWithLabelsThatReceivesNoData
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
name|model
operator|.
name|addLabel
argument_list|(
name|PUBLIC
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
comment|// Respond with 204 as there are no cells to be retrieved
name|assertEquals
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
argument_list|,
literal|204
argument_list|)
expr_stmt|;
comment|// With no content in the payload, the 'Content-Type' header is not echo back
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSimpleScannerXMLWithLabelsThatReceivesData
parameter_list|()
throws|throws
name|IOException
throws|,
name|JAXBException
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
literal|5
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
name|model
operator|.
name|addLabel
argument_list|(
name|SECRET
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
comment|// Respond with 204 as there are no cells to be retrieved
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
name|assertEquals
argument_list|(
name|countCellSet
argument_list|(
name|cellSet
argument_list|)
argument_list|,
literal|5
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

