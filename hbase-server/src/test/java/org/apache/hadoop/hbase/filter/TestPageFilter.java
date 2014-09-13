begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|IOException
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

begin_comment
comment|/**  * Tests for the page filter  */
end_comment

begin_class
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
name|TestPageFilter
block|{
specifier|static
specifier|final
name|int
name|ROW_LIMIT
init|=
literal|3
decl_stmt|;
comment|/**    * test page size filter    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testPageSize
parameter_list|()
throws|throws
name|Exception
block|{
name|Filter
name|f
init|=
operator|new
name|PageFilter
argument_list|(
name|ROW_LIMIT
argument_list|)
decl_stmt|;
name|pageSizeTests
argument_list|(
name|f
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test filter serialization    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|Exception
block|{
name|Filter
name|f
init|=
operator|new
name|PageFilter
argument_list|(
name|ROW_LIMIT
argument_list|)
decl_stmt|;
comment|// Decompose mainFilter to bytes.
name|byte
index|[]
name|buffer
init|=
name|f
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
comment|// Recompose mainFilter.
name|Filter
name|newFilter
init|=
name|PageFilter
operator|.
name|parseFrom
argument_list|(
name|buffer
argument_list|)
decl_stmt|;
comment|// Ensure the serialization preserved the filter by running a full test.
name|pageSizeTests
argument_list|(
name|newFilter
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|pageSizeTests
parameter_list|(
name|Filter
name|f
parameter_list|)
throws|throws
name|Exception
block|{
name|testFiltersBeyondPageSize
argument_list|(
name|f
argument_list|,
name|ROW_LIMIT
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testFiltersBeyondPageSize
parameter_list|(
specifier|final
name|Filter
name|f
parameter_list|,
specifier|final
name|int
name|pageSize
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
operator|(
name|pageSize
operator|*
literal|2
operator|)
condition|;
name|i
operator|++
control|)
block|{
name|boolean
name|filterOut
init|=
name|f
operator|.
name|filterRow
argument_list|()
decl_stmt|;
if|if
condition|(
name|filterOut
condition|)
block|{
break|break;
block|}
else|else
block|{
name|count
operator|++
expr_stmt|;
block|}
comment|// If at last row, should tell us to skip all remaining
if|if
condition|(
name|count
operator|==
name|pageSize
condition|)
block|{
name|assertTrue
argument_list|(
name|f
operator|.
name|filterAllRemaining
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertFalse
argument_list|(
name|f
operator|.
name|filterAllRemaining
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
name|pageSize
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

