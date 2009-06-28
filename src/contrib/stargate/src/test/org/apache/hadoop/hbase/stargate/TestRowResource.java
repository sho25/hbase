begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|stargate
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
name|stargate
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
name|stargate
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
name|stargate
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
name|stargate
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
name|stargate
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
name|stargate
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_class
specifier|public
class|class
name|TestRowResource
extends|extends
name|MiniClusterTestCase
block|{
specifier|private
specifier|static
specifier|final
name|String
name|TABLE
init|=
literal|"TestRowResource"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COLUMN_1
init|=
literal|"a:"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COLUMN_2
init|=
literal|"b:"
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
name|String
name|VALUE_1
init|=
literal|"testvalue1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ROW_2
init|=
literal|"testrow2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|VALUE_2
init|=
literal|"testvalue2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ROW_3
init|=
literal|"testrow3"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|VALUE_3
init|=
literal|"testvalue3"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ROW_4
init|=
literal|"testrow4"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|VALUE_4
init|=
literal|"testvalue4"
decl_stmt|;
specifier|private
name|Client
name|client
decl_stmt|;
specifier|private
name|JAXBContext
name|context
decl_stmt|;
specifier|private
name|Marshaller
name|marshaller
decl_stmt|;
specifier|private
name|Unmarshaller
name|unmarshaller
decl_stmt|;
specifier|private
name|HBaseAdmin
name|admin
decl_stmt|;
specifier|public
name|TestRowResource
parameter_list|()
throws|throws
name|JAXBException
block|{
name|super
argument_list|()
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
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
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
name|testServletPort
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
expr_stmt|;
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
name|COLUMN_1
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
name|COLUMN_2
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
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|client
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
specifier|private
name|Response
name|deleteRow
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|row
parameter_list|)
throws|throws
name|IOException
block|{
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
literal|'/'
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|Response
name|response
init|=
name|client
operator|.
name|delete
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|Thread
operator|.
name|yield
argument_list|()
expr_stmt|;
comment|// yield for the minicluster threads
return|return
name|response
return|;
block|}
specifier|private
name|Response
name|deleteValue
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|row
parameter_list|,
name|String
name|column
parameter_list|)
throws|throws
name|IOException
block|{
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
literal|'/'
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|column
argument_list|)
expr_stmt|;
name|Response
name|response
init|=
name|client
operator|.
name|delete
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|Thread
operator|.
name|yield
argument_list|()
expr_stmt|;
comment|// yield for the minicluster threads
return|return
name|response
return|;
block|}
specifier|private
name|Response
name|getValueXML
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|row
parameter_list|,
name|String
name|column
parameter_list|)
throws|throws
name|IOException
block|{
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
literal|'/'
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|column
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
name|MIMETYPE_XML
argument_list|)
decl_stmt|;
name|Thread
operator|.
name|yield
argument_list|()
expr_stmt|;
comment|// yield for the minicluster threads
return|return
name|response
return|;
block|}
specifier|private
name|Response
name|getValuePB
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|row
parameter_list|,
name|String
name|column
parameter_list|)
throws|throws
name|IOException
block|{
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
literal|'/'
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|column
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
name|MIMETYPE_PROTOBUF
argument_list|)
decl_stmt|;
name|Thread
operator|.
name|yield
argument_list|()
expr_stmt|;
comment|// yield for the minicluster threads
return|return
name|response
return|;
block|}
specifier|private
name|Response
name|putValueXML
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|row
parameter_list|,
name|String
name|column
parameter_list|,
name|String
name|value
parameter_list|)
throws|throws
name|IOException
throws|,
name|JAXBException
block|{
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
literal|'/'
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|column
argument_list|)
expr_stmt|;
name|RowModel
name|rowModel
init|=
operator|new
name|RowModel
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|rowModel
operator|.
name|addCell
argument_list|(
operator|new
name|CellModel
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|column
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|CellSetModel
name|cellSetModel
init|=
operator|new
name|CellSetModel
argument_list|()
decl_stmt|;
name|cellSetModel
operator|.
name|addRow
argument_list|(
name|rowModel
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
name|cellSetModel
argument_list|,
name|writer
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
operator|.
name|toString
argument_list|()
argument_list|,
name|MIMETYPE_XML
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|writer
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Thread
operator|.
name|yield
argument_list|()
expr_stmt|;
comment|// yield for the minicluster threads
return|return
name|response
return|;
block|}
specifier|private
name|void
name|checkValueXML
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|row
parameter_list|,
name|String
name|column
parameter_list|,
name|String
name|value
parameter_list|)
throws|throws
name|IOException
throws|,
name|JAXBException
block|{
name|Response
name|response
init|=
name|getValueXML
argument_list|(
name|table
argument_list|,
name|row
argument_list|,
name|column
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
name|RowModel
name|rowModel
init|=
name|cellSet
operator|.
name|getRows
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|CellModel
name|cell
init|=
name|rowModel
operator|.
name|getCells
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|cell
operator|.
name|getColumn
argument_list|()
argument_list|)
argument_list|,
name|column
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|cell
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Response
name|putValuePB
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|row
parameter_list|,
name|String
name|column
parameter_list|,
name|String
name|value
parameter_list|)
throws|throws
name|IOException
block|{
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
literal|'/'
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
name|column
argument_list|)
expr_stmt|;
name|RowModel
name|rowModel
init|=
operator|new
name|RowModel
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|rowModel
operator|.
name|addCell
argument_list|(
operator|new
name|CellModel
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|column
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|CellSetModel
name|cellSetModel
init|=
operator|new
name|CellSetModel
argument_list|()
decl_stmt|;
name|cellSetModel
operator|.
name|addRow
argument_list|(
name|rowModel
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
operator|.
name|toString
argument_list|()
argument_list|,
name|MIMETYPE_PROTOBUF
argument_list|,
name|cellSetModel
operator|.
name|createProtobufOutput
argument_list|()
argument_list|)
decl_stmt|;
name|Thread
operator|.
name|yield
argument_list|()
expr_stmt|;
comment|// yield for the minicluster threads
return|return
name|response
return|;
block|}
specifier|private
name|void
name|checkValuePB
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|row
parameter_list|,
name|String
name|column
parameter_list|,
name|String
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|Response
name|response
init|=
name|getValuePB
argument_list|(
name|table
argument_list|,
name|row
argument_list|,
name|column
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
name|RowModel
name|rowModel
init|=
name|cellSet
operator|.
name|getRows
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|CellModel
name|cell
init|=
name|rowModel
operator|.
name|getCells
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|cell
operator|.
name|getColumn
argument_list|()
argument_list|)
argument_list|,
name|column
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|cell
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testSingleCellGetPutXML
parameter_list|()
throws|throws
name|IOException
throws|,
name|JAXBException
block|{
name|Response
name|response
init|=
name|getValueXML
argument_list|(
name|TABLE
argument_list|,
name|ROW_1
argument_list|,
name|COLUMN_1
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
name|response
operator|=
name|putValueXML
argument_list|(
name|TABLE
argument_list|,
name|ROW_1
argument_list|,
name|COLUMN_1
argument_list|,
name|VALUE_1
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
name|checkValueXML
argument_list|(
name|TABLE
argument_list|,
name|ROW_1
argument_list|,
name|COLUMN_1
argument_list|,
name|VALUE_1
argument_list|)
expr_stmt|;
name|response
operator|=
name|putValueXML
argument_list|(
name|TABLE
argument_list|,
name|ROW_1
argument_list|,
name|COLUMN_2
argument_list|,
name|VALUE_1
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
name|response
operator|=
name|deleteValue
argument_list|(
name|TABLE
argument_list|,
name|ROW_1
argument_list|,
name|COLUMN_1
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
name|response
operator|=
name|getValueXML
argument_list|(
name|TABLE
argument_list|,
name|ROW_1
argument_list|,
name|COLUMN_1
argument_list|)
expr_stmt|;
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
name|checkValueXML
argument_list|(
name|TABLE
argument_list|,
name|ROW_1
argument_list|,
name|COLUMN_2
argument_list|,
name|VALUE_1
argument_list|)
expr_stmt|;
name|response
operator|=
name|deleteRow
argument_list|(
name|TABLE
argument_list|,
name|ROW_1
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
specifier|public
name|void
name|testSingleCellGetPutPB
parameter_list|()
throws|throws
name|IOException
throws|,
name|JAXBException
block|{
name|Response
name|response
init|=
name|getValuePB
argument_list|(
name|TABLE
argument_list|,
name|ROW_2
argument_list|,
name|COLUMN_1
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
name|response
operator|=
name|putValuePB
argument_list|(
name|TABLE
argument_list|,
name|ROW_2
argument_list|,
name|COLUMN_1
argument_list|,
name|VALUE_1
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
name|checkValuePB
argument_list|(
name|TABLE
argument_list|,
name|ROW_2
argument_list|,
name|COLUMN_1
argument_list|,
name|VALUE_1
argument_list|)
expr_stmt|;
name|response
operator|=
name|putValuePB
argument_list|(
name|TABLE
argument_list|,
name|ROW_2
argument_list|,
name|COLUMN_2
argument_list|,
name|VALUE_1
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
name|response
operator|=
name|putValuePB
argument_list|(
name|TABLE
argument_list|,
name|ROW_2
argument_list|,
name|COLUMN_2
argument_list|,
name|VALUE_2
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
name|checkValuePB
argument_list|(
name|TABLE
argument_list|,
name|ROW_2
argument_list|,
name|COLUMN_2
argument_list|,
name|VALUE_2
argument_list|)
expr_stmt|;
name|response
operator|=
name|deleteRow
argument_list|(
name|TABLE
argument_list|,
name|ROW_2
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
specifier|public
name|void
name|testSingleCellGetPutBinary
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|String
name|path
init|=
literal|"/"
operator|+
name|TABLE
operator|+
literal|"/"
operator|+
name|ROW_3
operator|+
literal|"/"
operator|+
name|COLUMN_1
decl_stmt|;
specifier|final
name|byte
index|[]
name|body
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_3
argument_list|)
decl_stmt|;
name|Response
name|response
init|=
name|client
operator|.
name|put
argument_list|(
name|path
argument_list|,
name|MIMETYPE_BINARY
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
literal|200
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|yield
argument_list|()
expr_stmt|;
comment|// yield for minicluster threads
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|path
argument_list|,
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
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|,
name|body
argument_list|)
argument_list|)
expr_stmt|;
name|boolean
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
literal|"X-Timestamp"
argument_list|)
condition|)
block|{
name|foundTimestampHeader
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
name|assertTrue
argument_list|(
name|foundTimestampHeader
argument_list|)
expr_stmt|;
name|response
operator|=
name|deleteRow
argument_list|(
name|TABLE
argument_list|,
name|ROW_3
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
specifier|public
name|void
name|testSingleCellGetJSON
parameter_list|()
throws|throws
name|IOException
throws|,
name|JAXBException
block|{
specifier|final
name|String
name|path
init|=
literal|"/"
operator|+
name|TABLE
operator|+
literal|"/"
operator|+
name|ROW_4
operator|+
literal|"/"
operator|+
name|COLUMN_1
decl_stmt|;
name|Response
name|response
init|=
name|client
operator|.
name|put
argument_list|(
name|path
argument_list|,
name|MIMETYPE_BINARY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_4
argument_list|)
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
name|Thread
operator|.
name|yield
argument_list|()
expr_stmt|;
comment|// yield for minicluster threads
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|path
argument_list|,
name|MIMETYPE_JSON
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
name|response
operator|=
name|deleteRow
argument_list|(
name|TABLE
argument_list|,
name|ROW_4
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
specifier|public
name|void
name|testMultiCellGetPutXML
parameter_list|()
throws|throws
name|IOException
throws|,
name|JAXBException
block|{
name|String
name|path
init|=
literal|"/"
operator|+
name|TABLE
operator|+
literal|"/fakerow"
decl_stmt|;
comment|// deliberate nonexistent row
name|CellSetModel
name|cellSetModel
init|=
operator|new
name|CellSetModel
argument_list|()
decl_stmt|;
name|RowModel
name|rowModel
init|=
operator|new
name|RowModel
argument_list|(
name|ROW_1
argument_list|)
decl_stmt|;
name|rowModel
operator|.
name|addCell
argument_list|(
operator|new
name|CellModel
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN_1
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|rowModel
operator|.
name|addCell
argument_list|(
operator|new
name|CellModel
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN_2
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|cellSetModel
operator|.
name|addRow
argument_list|(
name|rowModel
argument_list|)
expr_stmt|;
name|rowModel
operator|=
operator|new
name|RowModel
argument_list|(
name|ROW_2
argument_list|)
expr_stmt|;
name|rowModel
operator|.
name|addCell
argument_list|(
operator|new
name|CellModel
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN_1
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|rowModel
operator|.
name|addCell
argument_list|(
operator|new
name|CellModel
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN_2
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_4
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|cellSetModel
operator|.
name|addRow
argument_list|(
name|rowModel
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
name|cellSetModel
argument_list|,
name|writer
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
name|MIMETYPE_XML
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|writer
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Thread
operator|.
name|yield
argument_list|()
expr_stmt|;
comment|// yield for the minicluster threads
comment|// make sure the fake row was not actually created
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|path
argument_list|)
expr_stmt|;
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
comment|// check that all of the values were created
name|checkValueXML
argument_list|(
name|TABLE
argument_list|,
name|ROW_1
argument_list|,
name|COLUMN_1
argument_list|,
name|VALUE_1
argument_list|)
expr_stmt|;
name|checkValueXML
argument_list|(
name|TABLE
argument_list|,
name|ROW_1
argument_list|,
name|COLUMN_2
argument_list|,
name|VALUE_2
argument_list|)
expr_stmt|;
name|checkValueXML
argument_list|(
name|TABLE
argument_list|,
name|ROW_2
argument_list|,
name|COLUMN_1
argument_list|,
name|VALUE_3
argument_list|)
expr_stmt|;
name|checkValueXML
argument_list|(
name|TABLE
argument_list|,
name|ROW_2
argument_list|,
name|COLUMN_2
argument_list|,
name|VALUE_4
argument_list|)
expr_stmt|;
name|response
operator|=
name|deleteRow
argument_list|(
name|TABLE
argument_list|,
name|ROW_1
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
name|response
operator|=
name|deleteRow
argument_list|(
name|TABLE
argument_list|,
name|ROW_2
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
specifier|public
name|void
name|testMultiCellGetPutPB
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|path
init|=
literal|"/"
operator|+
name|TABLE
operator|+
literal|"/fakerow"
decl_stmt|;
comment|// deliberate nonexistent row
name|CellSetModel
name|cellSetModel
init|=
operator|new
name|CellSetModel
argument_list|()
decl_stmt|;
name|RowModel
name|rowModel
init|=
operator|new
name|RowModel
argument_list|(
name|ROW_1
argument_list|)
decl_stmt|;
name|rowModel
operator|.
name|addCell
argument_list|(
operator|new
name|CellModel
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN_1
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|rowModel
operator|.
name|addCell
argument_list|(
operator|new
name|CellModel
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN_2
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|cellSetModel
operator|.
name|addRow
argument_list|(
name|rowModel
argument_list|)
expr_stmt|;
name|rowModel
operator|=
operator|new
name|RowModel
argument_list|(
name|ROW_2
argument_list|)
expr_stmt|;
name|rowModel
operator|.
name|addCell
argument_list|(
operator|new
name|CellModel
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN_1
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|rowModel
operator|.
name|addCell
argument_list|(
operator|new
name|CellModel
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN_2
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_4
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|cellSetModel
operator|.
name|addRow
argument_list|(
name|rowModel
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
name|MIMETYPE_PROTOBUF
argument_list|,
name|cellSetModel
operator|.
name|createProtobufOutput
argument_list|()
argument_list|)
decl_stmt|;
name|Thread
operator|.
name|yield
argument_list|()
expr_stmt|;
comment|// yield for the minicluster threads
comment|// make sure the fake row was not actually created
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|path
argument_list|)
expr_stmt|;
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
comment|// check that all of the values were created
name|checkValuePB
argument_list|(
name|TABLE
argument_list|,
name|ROW_1
argument_list|,
name|COLUMN_1
argument_list|,
name|VALUE_1
argument_list|)
expr_stmt|;
name|checkValuePB
argument_list|(
name|TABLE
argument_list|,
name|ROW_1
argument_list|,
name|COLUMN_2
argument_list|,
name|VALUE_2
argument_list|)
expr_stmt|;
name|checkValuePB
argument_list|(
name|TABLE
argument_list|,
name|ROW_2
argument_list|,
name|COLUMN_1
argument_list|,
name|VALUE_3
argument_list|)
expr_stmt|;
name|checkValuePB
argument_list|(
name|TABLE
argument_list|,
name|ROW_2
argument_list|,
name|COLUMN_2
argument_list|,
name|VALUE_4
argument_list|)
expr_stmt|;
name|response
operator|=
name|deleteRow
argument_list|(
name|TABLE
argument_list|,
name|ROW_1
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
name|response
operator|=
name|deleteRow
argument_list|(
name|TABLE
argument_list|,
name|ROW_2
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
block|}
end_class

end_unit

