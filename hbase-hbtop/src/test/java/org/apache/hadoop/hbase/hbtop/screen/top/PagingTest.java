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
name|screen
operator|.
name|top
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
name|PagingTest
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
name|PagingTest
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testArrowUpAndArrowDown
parameter_list|()
block|{
name|Paging
name|paging
init|=
operator|new
name|Paging
argument_list|()
decl_stmt|;
name|paging
operator|.
name|updatePageSize
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|updateRecordsSize
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|2
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|3
argument_list|,
literal|1
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|4
argument_list|,
literal|2
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|4
argument_list|,
literal|2
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|3
argument_list|,
literal|2
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|2
argument_list|,
literal|2
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPageUpAndPageDown
parameter_list|()
block|{
name|Paging
name|paging
init|=
operator|new
name|Paging
argument_list|()
decl_stmt|;
name|paging
operator|.
name|updatePageSize
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|updateRecordsSize
argument_list|(
literal|8
argument_list|)
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|pageDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|3
argument_list|,
literal|3
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|paging
operator|.
name|pageDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|6
argument_list|,
literal|5
argument_list|,
literal|8
argument_list|)
expr_stmt|;
name|paging
operator|.
name|pageDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|7
argument_list|,
literal|5
argument_list|,
literal|8
argument_list|)
expr_stmt|;
name|paging
operator|.
name|pageDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|7
argument_list|,
literal|5
argument_list|,
literal|8
argument_list|)
expr_stmt|;
name|paging
operator|.
name|pageUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|4
argument_list|,
literal|4
argument_list|,
literal|7
argument_list|)
expr_stmt|;
name|paging
operator|.
name|pageUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|paging
operator|.
name|pageUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|pageUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInit
parameter_list|()
block|{
name|Paging
name|paging
init|=
operator|new
name|Paging
argument_list|()
decl_stmt|;
name|paging
operator|.
name|updatePageSize
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|updateRecordsSize
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|pageDown
argument_list|()
expr_stmt|;
name|paging
operator|.
name|pageDown
argument_list|()
expr_stmt|;
name|paging
operator|.
name|pageDown
argument_list|()
expr_stmt|;
name|paging
operator|.
name|pageDown
argument_list|()
expr_stmt|;
name|paging
operator|.
name|init
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWhenPageSizeGraterThanRecordsSize
parameter_list|()
block|{
name|Paging
name|paging
init|=
operator|new
name|Paging
argument_list|()
decl_stmt|;
name|paging
operator|.
name|updatePageSize
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|paging
operator|.
name|updateRecordsSize
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|2
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|2
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|pageDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|2
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|pageDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|2
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|pageUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|pageUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWhenPageSizeIsZero
parameter_list|()
block|{
name|Paging
name|paging
init|=
operator|new
name|Paging
argument_list|()
decl_stmt|;
name|paging
operator|.
name|updatePageSize
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|paging
operator|.
name|updateRecordsSize
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|paging
operator|.
name|pageDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|paging
operator|.
name|pageUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWhenRecordsSizeIsZero
parameter_list|()
block|{
name|Paging
name|paging
init|=
operator|new
name|Paging
argument_list|()
decl_stmt|;
name|paging
operator|.
name|updatePageSize
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|updateRecordsSize
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|paging
operator|.
name|pageDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|paging
operator|.
name|pageUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWhenChangingPageSizeDynamically
parameter_list|()
block|{
name|Paging
name|paging
init|=
operator|new
name|Paging
argument_list|()
decl_stmt|;
name|paging
operator|.
name|updatePageSize
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|updateRecordsSize
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|updatePageSize
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|3
argument_list|,
literal|2
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|paging
operator|.
name|updatePageSize
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|3
argument_list|,
literal|1
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|paging
operator|.
name|updatePageSize
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|3
argument_list|,
literal|0
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|paging
operator|.
name|updatePageSize
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|3
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|4
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|3
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|paging
operator|.
name|pageDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|3
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|paging
operator|.
name|pageUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|3
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|paging
operator|.
name|updatePageSize
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|3
argument_list|,
literal|3
argument_list|,
literal|4
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWhenChangingRecordsSizeDynamically
parameter_list|()
block|{
name|Paging
name|paging
init|=
operator|new
name|Paging
argument_list|()
decl_stmt|;
name|paging
operator|.
name|updatePageSize
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|updateRecordsSize
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|updateRecordsSize
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|paging
operator|.
name|getCurrentPosition
argument_list|()
argument_list|,
name|is
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|paging
operator|.
name|getPageStartPosition
argument_list|()
argument_list|,
name|is
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|paging
operator|.
name|getPageEndPosition
argument_list|()
argument_list|,
name|is
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|paging
operator|.
name|updateRecordsSize
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|2
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|paging
operator|.
name|updateRecordsSize
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|paging
operator|.
name|updateRecordsSize
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|paging
operator|.
name|arrowUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|paging
operator|.
name|pageDown
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|paging
operator|.
name|pageUp
argument_list|()
expr_stmt|;
name|assertPaging
argument_list|(
name|paging
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertPaging
parameter_list|(
name|Paging
name|paging
parameter_list|,
name|int
name|currentPosition
parameter_list|,
name|int
name|pageStartPosition
parameter_list|,
name|int
name|pageEndPosition
parameter_list|)
block|{
name|assertThat
argument_list|(
name|paging
operator|.
name|getCurrentPosition
argument_list|()
argument_list|,
name|is
argument_list|(
name|currentPosition
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|paging
operator|.
name|getPageStartPosition
argument_list|()
argument_list|,
name|is
argument_list|(
name|pageStartPosition
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|paging
operator|.
name|getPageEndPosition
argument_list|()
argument_list|,
name|is
argument_list|(
name|pageEndPosition
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

