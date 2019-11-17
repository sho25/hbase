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
name|hbtop
operator|.
name|mode
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|is
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
name|assertThat
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
name|fail
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
name|hbtop
operator|.
name|Record
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
name|hbtop
operator|.
name|TestUtils
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
name|hbtop
operator|.
name|field
operator|.
name|Field
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
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|ClientModeTest
extends|extends
name|ModeTestBase
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
name|ClientModeTest
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|Mode
name|getMode
parameter_list|()
block|{
return|return
name|Mode
operator|.
name|CLIENT
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|assertRecords
parameter_list|(
name|List
argument_list|<
name|Record
argument_list|>
name|records
parameter_list|)
block|{
name|TestUtils
operator|.
name|assertRecordsInClientMode
argument_list|(
name|records
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|assertDrillDown
parameter_list|(
name|Record
name|currentRecord
parameter_list|,
name|DrillDownInfo
name|drillDownInfo
parameter_list|)
block|{
name|assertThat
argument_list|(
name|drillDownInfo
operator|.
name|getNextMode
argument_list|()
argument_list|,
name|is
argument_list|(
name|Mode
operator|.
name|USER
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|drillDownInfo
operator|.
name|getInitialFilters
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|client
init|=
name|currentRecord
operator|.
name|get
argument_list|(
name|Field
operator|.
name|CLIENT
argument_list|)
operator|.
name|asString
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|client
condition|)
block|{
case|case
literal|"CLIENT_A_FOO"
case|:
name|assertThat
argument_list|(
name|drillDownInfo
operator|.
name|getInitialFilters
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"CLIENT==CLIENT_A_FOO"
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|"CLIENT_B_FOO"
case|:
name|assertThat
argument_list|(
name|drillDownInfo
operator|.
name|getInitialFilters
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"CLIENT==CLIENT_B_FOO"
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|"CLIENT_A_BAR"
case|:
name|assertThat
argument_list|(
name|drillDownInfo
operator|.
name|getInitialFilters
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"CLIENT==CLIENT_A_BAR"
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|"CLIENT_B_BAR"
case|:
name|assertThat
argument_list|(
name|drillDownInfo
operator|.
name|getInitialFilters
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"CLIENT==CLIENT_B_BAR"
argument_list|)
argument_list|)
expr_stmt|;
break|break;
default|default:
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

