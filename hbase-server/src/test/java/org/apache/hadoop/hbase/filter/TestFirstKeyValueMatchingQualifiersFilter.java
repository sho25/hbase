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
name|filter
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|KeyValue
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
name|FilterTests
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
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
annotation|@
name|Category
argument_list|(
block|{
name|FilterTests
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
name|TestFirstKeyValueMatchingQualifiersFilter
extends|extends
name|TestCase
block|{
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_QUALIFIER_1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_QUALIFIER_2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo_2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_QUALIFIER_3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo_3"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VAL_1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
comment|/**    * Test the functionality of    * {@link FirstKeyValueMatchingQualifiersFilter#filterCell(org.apache.hadoop.hbase.Cell)}    *     * @throws Exception    */
specifier|public
name|void
name|testFirstKeyMatchingQualifierFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|quals
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
name|quals
operator|.
name|add
argument_list|(
name|COLUMN_QUALIFIER_1
argument_list|)
expr_stmt|;
name|quals
operator|.
name|add
argument_list|(
name|COLUMN_QUALIFIER_2
argument_list|)
expr_stmt|;
name|Filter
name|filter
init|=
operator|new
name|FirstKeyValueMatchingQualifiersFilter
argument_list|(
name|quals
argument_list|)
decl_stmt|;
comment|// Match in first attempt
name|KeyValue
name|cell
decl_stmt|;
name|cell
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER_1
argument_list|,
name|VAL_1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"includeAndSetFlag"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|cell
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER_2
argument_list|,
name|VAL_1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"flagIsSetSkipToNextRow"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|NEXT_ROW
argument_list|)
expr_stmt|;
comment|// A mismatch in first attempt and match in second attempt.
name|filter
operator|.
name|reset
argument_list|()
expr_stmt|;
name|cell
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER_3
argument_list|,
name|VAL_1
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"includeFlagIsUnset"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|cell
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER_2
argument_list|,
name|VAL_1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"includeAndSetFlag"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|cell
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER_1
argument_list|,
name|VAL_1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"flagIsSetSkipToNextRow"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|NEXT_ROW
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

