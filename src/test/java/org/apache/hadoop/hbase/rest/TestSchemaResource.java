begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ColumnSchemaModel
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
name|TableSchemaModel
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
name|TestTableSchemaModel
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

begin_class
specifier|public
class|class
name|TestSchemaResource
block|{
specifier|private
specifier|static
name|String
name|TABLE1
init|=
literal|"TestSchemaResource1"
decl_stmt|;
specifier|private
specifier|static
name|String
name|TABLE2
init|=
literal|"TestSchemaResource2"
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
name|Configuration
name|conf
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
name|ColumnSchemaModel
operator|.
name|class
argument_list|,
name|TableSchemaModel
operator|.
name|class
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
name|byte
index|[]
name|toXML
parameter_list|(
name|TableSchemaModel
name|model
parameter_list|)
throws|throws
name|JAXBException
block|{
name|StringWriter
name|writer
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|context
operator|.
name|createMarshaller
argument_list|()
operator|.
name|marshal
argument_list|(
name|model
argument_list|,
name|writer
argument_list|)
expr_stmt|;
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
name|writer
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TableSchemaModel
name|fromXML
parameter_list|(
name|byte
index|[]
name|content
parameter_list|)
throws|throws
name|JAXBException
block|{
return|return
operator|(
name|TableSchemaModel
operator|)
name|context
operator|.
name|createUnmarshaller
argument_list|()
operator|.
name|unmarshal
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|content
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableCreateAndDeleteXML
parameter_list|()
throws|throws
name|IOException
throws|,
name|JAXBException
block|{
name|String
name|schemaPath
init|=
literal|"/"
operator|+
name|TABLE1
operator|+
literal|"/schema"
decl_stmt|;
name|TableSchemaModel
name|model
decl_stmt|;
name|Response
name|response
decl_stmt|;
name|HBaseAdmin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|admin
operator|.
name|tableExists
argument_list|(
name|TABLE1
argument_list|)
argument_list|)
expr_stmt|;
comment|// create the table
name|model
operator|=
name|TestTableSchemaModel
operator|.
name|buildTestModel
argument_list|(
name|TABLE1
argument_list|)
expr_stmt|;
name|TestTableSchemaModel
operator|.
name|checkModel
argument_list|(
name|model
argument_list|,
name|TABLE1
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|put
argument_list|(
name|schemaPath
argument_list|,
name|Constants
operator|.
name|MIMETYPE_XML
argument_list|,
name|toXML
argument_list|(
name|model
argument_list|)
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
comment|// recall the same put operation but in read-only mode
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
name|put
argument_list|(
name|schemaPath
argument_list|,
name|Constants
operator|.
name|MIMETYPE_XML
argument_list|,
name|toXML
argument_list|(
name|model
argument_list|)
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
comment|// retrieve the schema and validate it
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|schemaPath
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
name|model
operator|=
name|fromXML
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
expr_stmt|;
name|TestTableSchemaModel
operator|.
name|checkModel
argument_list|(
name|model
argument_list|,
name|TABLE1
argument_list|)
expr_stmt|;
comment|// delete the table
name|client
operator|.
name|delete
argument_list|(
name|schemaPath
argument_list|)
expr_stmt|;
comment|// make sure HBase concurs
name|assertFalse
argument_list|(
name|admin
operator|.
name|tableExists
argument_list|(
name|TABLE1
argument_list|)
argument_list|)
expr_stmt|;
comment|// return read-only setting back to default
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.rest.readonly"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableCreateAndDeletePB
parameter_list|()
throws|throws
name|IOException
throws|,
name|JAXBException
block|{
name|String
name|schemaPath
init|=
literal|"/"
operator|+
name|TABLE2
operator|+
literal|"/schema"
decl_stmt|;
name|TableSchemaModel
name|model
decl_stmt|;
name|Response
name|response
decl_stmt|;
name|HBaseAdmin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|admin
operator|.
name|tableExists
argument_list|(
name|TABLE2
argument_list|)
argument_list|)
expr_stmt|;
comment|// create the table
name|model
operator|=
name|TestTableSchemaModel
operator|.
name|buildTestModel
argument_list|(
name|TABLE2
argument_list|)
expr_stmt|;
name|TestTableSchemaModel
operator|.
name|checkModel
argument_list|(
name|model
argument_list|,
name|TABLE2
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|put
argument_list|(
name|schemaPath
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
comment|// recall the same put operation but in read-only mode
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
name|put
argument_list|(
name|schemaPath
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
literal|403
argument_list|)
expr_stmt|;
comment|// retrieve the schema and validate it
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|schemaPath
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
name|model
operator|=
operator|new
name|TableSchemaModel
argument_list|()
expr_stmt|;
name|model
operator|.
name|getObjectFromMessage
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
expr_stmt|;
name|TestTableSchemaModel
operator|.
name|checkModel
argument_list|(
name|model
argument_list|,
name|TABLE2
argument_list|)
expr_stmt|;
comment|// delete the table
name|client
operator|.
name|delete
argument_list|(
name|schemaPath
argument_list|)
expr_stmt|;
comment|// make sure HBase concurs
name|assertFalse
argument_list|(
name|admin
operator|.
name|tableExists
argument_list|(
name|TABLE2
argument_list|)
argument_list|)
expr_stmt|;
comment|// return read-only setting back to default
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.rest.readonly"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

