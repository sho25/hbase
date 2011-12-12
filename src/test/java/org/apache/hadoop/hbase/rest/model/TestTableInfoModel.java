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
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|SmallTests
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
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestTableInfoModel
extends|extends
name|TestCase
block|{
specifier|private
specifier|static
specifier|final
name|String
name|TABLE
init|=
literal|"testtable"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|START_KEY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"abracadbra"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|END_KEY
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
name|long
name|ID
init|=
literal|8731042424L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|LOCATION
init|=
literal|"testhost:9876"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|AS_XML
init|=
literal|"<TableInfo name=\"testtable\">"
operator|+
literal|"<Region location=\"testhost:9876\""
operator|+
literal|" endKey=\"enp5eng=\""
operator|+
literal|" startKey=\"YWJyYWNhZGJyYQ==\""
operator|+
literal|" id=\"8731042424\""
operator|+
literal|" name=\"testtable,abracadbra,8731042424\"/>"
operator|+
literal|"</TableInfo>"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|AS_PB
init|=
literal|"Cgl0ZXN0dGFibGUSSQofdGVzdHRhYmxlLGFicmFjYWRicmEsODczMTA0MjQyNBIKYWJyYWNhZGJy"
operator|+
literal|"YRoFenp5engg+MSkwyAqDXRlc3Rob3N0Ojk4NzY="
decl_stmt|;
specifier|private
name|JAXBContext
name|context
decl_stmt|;
specifier|public
name|TestTableInfoModel
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
name|TableInfoModel
operator|.
name|class
argument_list|,
name|TableRegionModel
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
specifier|private
name|TableInfoModel
name|buildTestModel
parameter_list|()
block|{
name|TableInfoModel
name|model
init|=
operator|new
name|TableInfoModel
argument_list|()
decl_stmt|;
name|model
operator|.
name|setName
argument_list|(
name|TABLE
argument_list|)
expr_stmt|;
name|model
operator|.
name|add
argument_list|(
operator|new
name|TableRegionModel
argument_list|(
name|TABLE
argument_list|,
name|ID
argument_list|,
name|START_KEY
argument_list|,
name|END_KEY
argument_list|,
name|LOCATION
argument_list|)
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
name|TableInfoModel
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
name|TableInfoModel
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
name|TableInfoModel
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
name|TableInfoModel
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
name|TableInfoModel
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
name|TableInfoModel
operator|)
operator|new
name|TableInfoModel
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
name|TableInfoModel
name|model
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|model
operator|.
name|getName
argument_list|()
argument_list|,
name|TABLE
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|TableRegionModel
argument_list|>
name|regions
init|=
name|model
operator|.
name|getRegions
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|TableRegionModel
name|region
init|=
name|regions
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|region
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|START_KEY
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|region
operator|.
name|getEndKey
argument_list|()
argument_list|,
name|END_KEY
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|region
operator|.
name|getId
argument_list|()
argument_list|,
name|ID
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|region
operator|.
name|getLocation
argument_list|()
argument_list|,
name|LOCATION
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|regions
operator|.
name|hasNext
argument_list|()
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
annotation|@
name|org
operator|.
name|junit
operator|.
name|Rule
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
name|cu
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
argument_list|()
decl_stmt|;
block|}
end_class

end_unit

