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
operator|.
name|model
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
name|util
operator|.
name|Iterator
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
name|testclassification
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
name|Bytes
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
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestTableInfoModel
extends|extends
name|TestModelBase
argument_list|<
name|TableInfoModel
argument_list|>
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
name|TestTableInfoModel
operator|.
name|class
argument_list|)
decl_stmt|;
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
specifier|public
name|TestTableInfoModel
parameter_list|()
throws|throws
name|Exception
block|{
name|super
argument_list|(
name|TableInfoModel
operator|.
name|class
argument_list|)
expr_stmt|;
name|AS_XML
operator|=
literal|"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><TableInfo "
operator|+
literal|"name=\"testtable\"><Region endKey=\"enp5eng=\" id=\"8731042424\" "
operator|+
literal|"location=\"testhost:9876\" "
operator|+
literal|"name=\"testtable,abracadbra,8731042424.ad9860f031282c46ed431d7af8f94aca.\" "
operator|+
literal|"startKey=\"YWJyYWNhZGJyYQ==\"/></TableInfo>"
expr_stmt|;
name|AS_PB
operator|=
literal|"Cgl0ZXN0dGFibGUSSQofdGVzdHRhYmxlLGFicmFjYWRicmEsODczMTA0MjQyNBIKYWJyYWNhZGJy"
operator|+
literal|"YRoFenp5engg+MSkwyAqDXRlc3Rob3N0Ojk4NzY="
expr_stmt|;
name|AS_JSON
operator|=
literal|"{\"name\":\"testtable\",\"Region\":[{\"endKey\":\"enp5eng=\",\"id\":8731042424,"
operator|+
literal|"\"location\":\"testhost:9876\",\""
operator|+
literal|"name\":\"testtable,abracadbra,8731042424.ad9860f031282c46ed431d7af8f94aca.\",\""
operator|+
literal|"startKey\":\"YWJyYWNhZGJyYQ==\"}]}"
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
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
name|Override
specifier|protected
name|void
name|checkModel
parameter_list|(
name|TableInfoModel
name|model
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|TABLE
argument_list|,
name|model
operator|.
name|getName
argument_list|()
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
name|ID
argument_list|,
name|region
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|LOCATION
argument_list|,
name|region
operator|.
name|getLocation
argument_list|()
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
annotation|@
name|Override
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
annotation|@
name|Override
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
annotation|@
name|Override
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

