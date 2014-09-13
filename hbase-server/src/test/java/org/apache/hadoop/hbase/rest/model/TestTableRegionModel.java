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
operator|.
name|model
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
name|TestTableRegionModel
extends|extends
name|TestModelBase
argument_list|<
name|TableRegionModel
argument_list|>
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
specifier|public
name|TestTableRegionModel
parameter_list|()
throws|throws
name|Exception
block|{
name|super
argument_list|(
name|TableRegionModel
operator|.
name|class
argument_list|)
expr_stmt|;
name|AS_XML
operator|=
literal|"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Region endKey=\"enp5eng=\" "
operator|+
literal|"id=\"8731042424\" location=\"testhost:9876\" "
operator|+
literal|"name=\"testtable,abracadbra,8731042424.ad9860f031282c46ed431d7af8f94aca.\" "
operator|+
literal|"startKey=\"YWJyYWNhZGJyYQ==\"/>"
expr_stmt|;
name|AS_JSON
operator|=
literal|"{\"endKey\":\"enp5eng=\",\"id\":8731042424,\"location\":\"testhost:9876\","
operator|+
literal|"\"name\":\"testtable,abracadbra,8731042424.ad9860f031282c46ed431d7af8f94aca.\",\""
operator|+
literal|"startKey\":\"YWJyYWNhZGJyYQ==\"}"
expr_stmt|;
block|}
specifier|protected
name|TableRegionModel
name|buildTestModel
parameter_list|()
block|{
name|TableRegionModel
name|model
init|=
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
decl_stmt|;
return|return
name|model
return|;
block|}
specifier|protected
name|void
name|checkModel
parameter_list|(
name|TableRegionModel
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
name|model
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
name|model
operator|.
name|getId
argument_list|()
argument_list|,
name|ID
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|model
operator|.
name|getLocation
argument_list|()
argument_list|,
name|LOCATION
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|model
operator|.
name|getName
argument_list|()
argument_list|,
name|TABLE
operator|+
literal|","
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|START_KEY
argument_list|)
operator|+
literal|","
operator|+
name|Long
operator|.
name|toString
argument_list|(
name|ID
argument_list|)
operator|+
literal|".ad9860f031282c46ed431d7af8f94aca."
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testGetName
parameter_list|()
block|{
name|TableRegionModel
name|model
init|=
name|buildTestModel
argument_list|()
decl_stmt|;
name|String
name|modelName
init|=
name|model
operator|.
name|getName
argument_list|()
decl_stmt|;
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TABLE
argument_list|)
argument_list|,
name|START_KEY
argument_list|,
name|END_KEY
argument_list|,
literal|false
argument_list|,
name|ID
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|modelName
argument_list|,
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testSetName
parameter_list|()
block|{
name|TableRegionModel
name|model
init|=
name|buildTestModel
argument_list|()
decl_stmt|;
name|String
name|name
init|=
name|model
operator|.
name|getName
argument_list|()
decl_stmt|;
name|model
operator|.
name|setName
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|name
argument_list|,
name|model
operator|.
name|getName
argument_list|()
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
comment|//no pb ignore
block|}
block|}
end_class

end_unit

