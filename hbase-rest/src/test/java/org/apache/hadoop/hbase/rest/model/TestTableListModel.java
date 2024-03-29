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
name|TestTableListModel
extends|extends
name|TestModelBase
argument_list|<
name|TableListModel
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
name|TestTableListModel
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TABLE1
init|=
literal|"table1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TABLE2
init|=
literal|"table2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TABLE3
init|=
literal|"table3"
decl_stmt|;
specifier|public
name|TestTableListModel
parameter_list|()
throws|throws
name|Exception
block|{
name|super
argument_list|(
name|TableListModel
operator|.
name|class
argument_list|)
expr_stmt|;
name|AS_XML
operator|=
literal|"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><TableList><table "
operator|+
literal|"name=\"table1\"/><table name=\"table2\"/><table name=\"table3\"/></TableList>"
expr_stmt|;
name|AS_PB
operator|=
literal|"CgZ0YWJsZTEKBnRhYmxlMgoGdGFibGUz"
expr_stmt|;
name|AS_JSON
operator|=
literal|"{\"table\":[{\"name\":\"table1\"},{\"name\":\"table2\"},{\"name\":\"table3\"}]}"
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|TableListModel
name|buildTestModel
parameter_list|()
block|{
name|TableListModel
name|model
init|=
operator|new
name|TableListModel
argument_list|()
decl_stmt|;
name|model
operator|.
name|add
argument_list|(
operator|new
name|TableModel
argument_list|(
name|TABLE1
argument_list|)
argument_list|)
expr_stmt|;
name|model
operator|.
name|add
argument_list|(
operator|new
name|TableModel
argument_list|(
name|TABLE2
argument_list|)
argument_list|)
expr_stmt|;
name|model
operator|.
name|add
argument_list|(
operator|new
name|TableModel
argument_list|(
name|TABLE3
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
name|TableListModel
name|model
parameter_list|)
block|{
name|Iterator
argument_list|<
name|TableModel
argument_list|>
name|tables
init|=
name|model
operator|.
name|getTables
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|TableModel
name|table
init|=
name|tables
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|TABLE1
argument_list|,
name|table
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|=
name|tables
operator|.
name|next
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|TABLE2
argument_list|,
name|table
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|=
name|tables
operator|.
name|next
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|TABLE3
argument_list|,
name|table
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|tables
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

