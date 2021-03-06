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
name|util
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
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|TestJRubyFormat
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
name|TestJRubyFormat
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testPrint
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"null"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"boolean"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"number"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"string"
argument_list|,
literal|"str"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"binary"
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|}
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"list"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|1
argument_list|,
literal|"2"
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|jrubyString
init|=
name|JRubyFormat
operator|.
name|print
argument_list|(
name|map
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"{ null => '', boolean => 'true', number => '1', "
operator|+
literal|"string => 'str', binary => '010203', "
operator|+
literal|"list => [ '1', '2', 'true' ] }"
argument_list|,
name|jrubyString
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEscape
parameter_list|()
block|{
name|String
name|jrubyString
init|=
name|JRubyFormat
operator|.
name|print
argument_list|(
literal|"\\\'\n\r\t\f"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"'\\\\\\'\\n\\r\\t\\f'"
argument_list|,
name|jrubyString
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

