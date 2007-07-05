begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|Text
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
name|TestPageRowFilter
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testPageSize
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|pageSize
init|=
literal|3
decl_stmt|;
name|RowFilterInterface
name|filter
init|=
operator|new
name|PageRowFilter
argument_list|(
name|pageSize
argument_list|)
decl_stmt|;
name|testFiltersBeyondPageSize
argument_list|(
name|filter
argument_list|,
name|pageSize
argument_list|)
expr_stmt|;
comment|// Test reset works by going in again.
name|filter
operator|.
name|reset
argument_list|()
expr_stmt|;
name|testFiltersBeyondPageSize
argument_list|(
name|filter
argument_list|,
name|pageSize
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testFiltersBeyondPageSize
parameter_list|(
specifier|final
name|RowFilterInterface
name|filter
parameter_list|,
specifier|final
name|int
name|pageSize
parameter_list|)
block|{
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
name|Text
name|row
init|=
operator|new
name|Text
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|boolean
name|filterOut
init|=
name|filter
operator|.
name|filter
argument_list|(
name|row
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|filterOut
condition|)
block|{
name|assertFalse
argument_list|(
literal|"Disagrees with 'filter'"
argument_list|,
name|filter
operator|.
name|filterAllRemaining
argument_list|()
argument_list|)
expr_stmt|;
name|filter
operator|.
name|acceptedRow
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Once we have all for a page, calls to filterAllRemaining should
comment|// stay true.
name|assertTrue
argument_list|(
literal|"Disagrees with 'filter'"
argument_list|,
name|filter
operator|.
name|filterAllRemaining
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|i
operator|>=
name|pageSize
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

