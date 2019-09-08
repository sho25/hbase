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
name|any
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
name|eq
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
name|inOrder
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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
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
name|function
operator|.
name|Function
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
name|screen
operator|.
name|ScreenView
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
name|InOrder
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
name|InputModeScreenPresenterTest
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
name|InputModeScreenPresenterTest
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEST_INPUT_MESSAGE
init|=
literal|"test input message"
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|InputModeScreenView
name|inputModeScreenView
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|TopScreenView
name|topScreenView
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|Function
argument_list|<
name|String
argument_list|,
name|ScreenView
argument_list|>
name|resultListener
decl_stmt|;
specifier|private
name|InputModeScreenPresenter
name|inputModeScreenPresenter
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
name|String
argument_list|>
name|histories
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|histories
operator|.
name|add
argument_list|(
literal|"history1"
argument_list|)
expr_stmt|;
name|histories
operator|.
name|add
argument_list|(
literal|"history2"
argument_list|)
expr_stmt|;
name|inputModeScreenPresenter
operator|=
operator|new
name|InputModeScreenPresenter
argument_list|(
name|inputModeScreenView
argument_list|,
name|TEST_INPUT_MESSAGE
argument_list|,
name|histories
argument_list|,
name|resultListener
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
name|inputModeScreenPresenter
operator|.
name|init
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|eq
argument_list|(
name|TEST_INPUT_MESSAGE
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|""
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCharacter
parameter_list|()
block|{
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
literal|'a'
argument_list|)
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
literal|'b'
argument_list|)
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
literal|'c'
argument_list|)
expr_stmt|;
name|InOrder
name|inOrder
init|=
name|inOrder
argument_list|(
name|inputModeScreenView
argument_list|)
decl_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"ab"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testArrowLeftAndRight
parameter_list|()
block|{
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
literal|'a'
argument_list|)
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
literal|'b'
argument_list|)
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
literal|'c'
argument_list|)
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|arrowLeft
argument_list|()
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|arrowLeft
argument_list|()
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|arrowLeft
argument_list|()
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|arrowLeft
argument_list|()
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|arrowRight
argument_list|()
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|arrowRight
argument_list|()
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|arrowRight
argument_list|()
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|arrowRight
argument_list|()
expr_stmt|;
name|InOrder
name|inOrder
init|=
name|inOrder
argument_list|(
name|inputModeScreenView
argument_list|)
decl_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"ab"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHomeAndEnd
parameter_list|()
block|{
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
literal|'a'
argument_list|)
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
literal|'b'
argument_list|)
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
literal|'c'
argument_list|)
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|home
argument_list|()
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|home
argument_list|()
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|end
argument_list|()
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|end
argument_list|()
expr_stmt|;
name|InOrder
name|inOrder
init|=
name|inOrder
argument_list|(
name|inputModeScreenView
argument_list|)
decl_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"ab"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBackspace
parameter_list|()
block|{
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
literal|'a'
argument_list|)
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
literal|'b'
argument_list|)
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
literal|'c'
argument_list|)
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|backspace
argument_list|()
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|backspace
argument_list|()
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|backspace
argument_list|()
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|backspace
argument_list|()
expr_stmt|;
name|InOrder
name|inOrder
init|=
name|inOrder
argument_list|(
name|inputModeScreenView
argument_list|)
decl_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"ab"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"ab"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|""
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDelete
parameter_list|()
block|{
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
literal|'a'
argument_list|)
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
literal|'b'
argument_list|)
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
literal|'c'
argument_list|)
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|delete
argument_list|()
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|arrowLeft
argument_list|()
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|delete
argument_list|()
expr_stmt|;
name|InOrder
name|inOrder
init|=
name|inOrder
argument_list|(
name|inputModeScreenView
argument_list|)
decl_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"ab"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"ab"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHistories
parameter_list|()
block|{
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
literal|'a'
argument_list|)
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
literal|'b'
argument_list|)
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
literal|'c'
argument_list|)
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|arrowUp
argument_list|()
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|arrowUp
argument_list|()
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|arrowUp
argument_list|()
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
name|InOrder
name|inOrder
init|=
name|inOrder
argument_list|(
name|inputModeScreenView
argument_list|)
decl_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"ab"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"history2"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"history1"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|inputModeScreenView
argument_list|)
operator|.
name|showInput
argument_list|(
name|any
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|"history2"
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|8
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
name|when
argument_list|(
name|resultListener
operator|.
name|apply
argument_list|(
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|topScreenView
argument_list|)
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
literal|'a'
argument_list|)
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
literal|'b'
argument_list|)
expr_stmt|;
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
literal|'c'
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|inputModeScreenPresenter
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
name|verify
argument_list|(
name|resultListener
argument_list|)
operator|.
name|apply
argument_list|(
name|eq
argument_list|(
literal|"abc"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
