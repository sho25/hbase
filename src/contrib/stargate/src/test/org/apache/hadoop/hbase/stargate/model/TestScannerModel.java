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
operator|.
name|model
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
name|java
operator|.
name|io
operator|.
name|StringReader
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
name|hbase
operator|.
name|util
operator|.
name|Base64
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
name|junit
operator|.
name|framework
operator|.
name|TestCase
import|;
end_import

begin_class
specifier|public
class|class
name|TestScannerModel
extends|extends
name|TestCase
block|{
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|START_ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"abracadabra"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|END_ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzyzx"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"column1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"column2:foo"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|START_TIME
init|=
literal|1245219839331L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|END_TIME
init|=
literal|1245393318192L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|BATCH
init|=
literal|100
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|AS_XML
init|=
literal|"<Scanner startTime=\"1245219839331\""
operator|+
literal|" startRow=\"YWJyYWNhZGFicmE=\""
operator|+
literal|" endTime=\"1245393318192\""
operator|+
literal|" endRow=\"enp5eng=\""
operator|+
literal|" batch=\"100\">"
operator|+
literal|"<column>Y29sdW1uMQ==</column>"
operator|+
literal|"<column>Y29sdW1uMjpmb28=</column>"
operator|+
literal|"</Scanner>"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|AS_PB
init|=
literal|"CgthYnJhY2FkYWJyYRIFenp5engaB2NvbHVtbjEaC2NvbHVtbjI6Zm9vIGQo47qL554kMLDi57mf"
operator|+
literal|"JA=="
decl_stmt|;
specifier|private
name|JAXBContext
name|context
decl_stmt|;
specifier|public
name|TestScannerModel
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
name|ScannerModel
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ScannerModel
name|buildTestModel
parameter_list|()
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
name|setStartRow
argument_list|(
name|START_ROW
argument_list|)
expr_stmt|;
name|model
operator|.
name|setEndRow
argument_list|(
name|END_ROW
argument_list|)
expr_stmt|;
name|model
operator|.
name|addColumn
argument_list|(
name|COLUMN1
argument_list|)
expr_stmt|;
name|model
operator|.
name|addColumn
argument_list|(
name|COLUMN2
argument_list|)
expr_stmt|;
name|model
operator|.
name|setStartTime
argument_list|(
name|START_TIME
argument_list|)
expr_stmt|;
name|model
operator|.
name|setEndTime
argument_list|(
name|END_TIME
argument_list|)
expr_stmt|;
name|model
operator|.
name|setBatch
argument_list|(
name|BATCH
argument_list|)
expr_stmt|;
return|return
name|model
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|private
name|String
name|toXML
parameter_list|(
name|ScannerModel
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
name|writer
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|ScannerModel
name|fromXML
parameter_list|(
name|String
name|xml
parameter_list|)
throws|throws
name|JAXBException
block|{
return|return
operator|(
name|ScannerModel
operator|)
name|context
operator|.
name|createUnmarshaller
argument_list|()
operator|.
name|unmarshal
argument_list|(
operator|new
name|StringReader
argument_list|(
name|xml
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|private
name|byte
index|[]
name|toPB
parameter_list|(
name|ScannerModel
name|model
parameter_list|)
block|{
return|return
name|model
operator|.
name|createProtobufOutput
argument_list|()
return|;
block|}
specifier|private
name|ScannerModel
name|fromPB
parameter_list|(
name|String
name|pb
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|(
name|ScannerModel
operator|)
operator|new
name|ScannerModel
argument_list|()
operator|.
name|getObjectFromMessage
argument_list|(
name|Base64
operator|.
name|decode
argument_list|(
name|AS_PB
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|void
name|checkModel
parameter_list|(
name|ScannerModel
name|model
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|model
operator|.
name|getStartRow
argument_list|()
argument_list|,
name|START_ROW
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|model
operator|.
name|getEndRow
argument_list|()
argument_list|,
name|END_ROW
argument_list|)
argument_list|)
expr_stmt|;
name|boolean
name|foundCol1
init|=
literal|false
decl_stmt|,
name|foundCol2
init|=
literal|false
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|column
range|:
name|model
operator|.
name|getColumns
argument_list|()
control|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|column
argument_list|,
name|COLUMN1
argument_list|)
condition|)
block|{
name|foundCol1
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|column
argument_list|,
name|COLUMN2
argument_list|)
condition|)
block|{
name|foundCol2
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
name|foundCol1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|foundCol2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|model
operator|.
name|getStartTime
argument_list|()
argument_list|,
name|START_TIME
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|model
operator|.
name|getEndTime
argument_list|()
argument_list|,
name|END_TIME
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|model
operator|.
name|getBatch
argument_list|()
argument_list|,
name|BATCH
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testBuildModel
parameter_list|()
throws|throws
name|Exception
block|{
name|checkModel
argument_list|(
name|buildTestModel
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testFromXML
parameter_list|()
throws|throws
name|Exception
block|{
name|checkModel
argument_list|(
name|fromXML
argument_list|(
name|AS_XML
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testFromPB
parameter_list|()
throws|throws
name|Exception
block|{
name|checkModel
argument_list|(
name|fromPB
argument_list|(
name|AS_PB
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

