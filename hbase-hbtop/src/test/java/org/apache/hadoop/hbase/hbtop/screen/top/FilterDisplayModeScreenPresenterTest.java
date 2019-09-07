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
import|import static
name|org
operator|.
name|mockito
operator|.
name|ArgumentMatchers
operator|.
name|argThat
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verify
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|RecordFilter
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
name|hbtop
operator|.
name|field
operator|.
name|FieldInfo
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
name|mode
operator|.
name|Mode
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
name|Before
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
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|runners
operator|.
name|MockitoJUnitRunner
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
annotation|@
name|RunWith
argument_list|(
name|MockitoJUnitRunner
operator|.
name|class
argument_list|)
specifier|public
class|class
name|FilterDisplayModeScreenPresenterTest
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
name|FilterDisplayModeScreenPresenterTest
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|FilterDisplayModeScreenView
name|filterDisplayModeScreenView
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|TopScreenView
name|topScreenView
decl_stmt|;
specifier|private
name|FilterDisplayModeScreenPresenter
name|filterDisplayModeScreenPresenter
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|List
argument_list|<
name|Field
argument_list|>
name|fields
init|=
name|Mode
operator|.
name|REGION
operator|.
name|getFieldInfos
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|FieldInfo
operator|::
name|getField
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|RecordFilter
argument_list|>
name|filters
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|filters
operator|.
name|add
argument_list|(
name|RecordFilter
operator|.
name|parse
argument_list|(
literal|"NAMESPACE==namespace"
argument_list|,
name|fields
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|filters
operator|.
name|add
argument_list|(
name|RecordFilter
operator|.
name|parse
argument_list|(
literal|"TABLE==table"
argument_list|,
name|fields
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|filterDisplayModeScreenPresenter
operator|=
operator|new
name|FilterDisplayModeScreenPresenter
argument_list|(
name|filterDisplayModeScreenView
argument_list|,
name|filters
argument_list|,
name|topScreenView
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
name|filterDisplayModeScreenPresenter
operator|.
name|init
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|filterDisplayModeScreenView
argument_list|)
operator|.
name|showFilters
argument_list|(
name|argThat
argument_list|(
name|filters
lambda|->
name|filters
operator|.
name|size
argument_list|()
operator|==
literal|2
operator|&&
name|filters
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"NAMESPACE==namespace"
argument_list|)
operator|&&
name|filters
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"TABLE==table"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReturnToTopScreen
parameter_list|()
block|{
name|assertThat
argument_list|(
name|filterDisplayModeScreenPresenter
operator|.
name|returnToNextScreen
argument_list|()
argument_list|,
name|is
argument_list|(
name|topScreenView
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

