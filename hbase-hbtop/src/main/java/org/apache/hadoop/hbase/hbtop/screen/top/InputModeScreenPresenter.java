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
import|import
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|Nullable
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
name|Collections
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
name|Objects
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * The presentation logic for the input mode.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|InputModeScreenPresenter
block|{
specifier|private
specifier|final
name|InputModeScreenView
name|inputModeScreenView
decl_stmt|;
specifier|private
specifier|final
name|String
name|message
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|histories
decl_stmt|;
specifier|private
specifier|final
name|Function
argument_list|<
name|String
argument_list|,
name|ScreenView
argument_list|>
name|resultListener
decl_stmt|;
specifier|private
name|StringBuilder
name|inputString
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
specifier|private
name|int
name|cursorPosition
decl_stmt|;
specifier|private
name|int
name|historyPosition
init|=
operator|-
literal|1
decl_stmt|;
specifier|public
name|InputModeScreenPresenter
parameter_list|(
name|InputModeScreenView
name|inputModeScreenView
parameter_list|,
name|String
name|message
parameter_list|,
annotation|@
name|Nullable
name|List
argument_list|<
name|String
argument_list|>
name|histories
parameter_list|,
name|Function
argument_list|<
name|String
argument_list|,
name|ScreenView
argument_list|>
name|resultListener
parameter_list|)
block|{
name|this
operator|.
name|inputModeScreenView
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|inputModeScreenView
argument_list|)
expr_stmt|;
name|this
operator|.
name|message
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|message
argument_list|)
expr_stmt|;
if|if
condition|(
name|histories
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|histories
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|histories
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|histories
operator|=
name|Collections
operator|.
name|emptyList
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|resultListener
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|resultListener
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|()
block|{
name|inputModeScreenView
operator|.
name|showInput
argument_list|(
name|message
argument_list|,
name|inputString
operator|.
name|toString
argument_list|()
argument_list|,
name|cursorPosition
argument_list|)
expr_stmt|;
name|inputModeScreenView
operator|.
name|refreshTerminal
argument_list|()
expr_stmt|;
block|}
specifier|public
name|ScreenView
name|returnToNextScreen
parameter_list|()
block|{
name|inputModeScreenView
operator|.
name|hideCursor
argument_list|()
expr_stmt|;
name|String
name|result
init|=
name|inputString
operator|.
name|toString
argument_list|()
decl_stmt|;
return|return
name|resultListener
operator|.
name|apply
argument_list|(
name|result
argument_list|)
return|;
block|}
specifier|public
name|void
name|character
parameter_list|(
name|Character
name|character
parameter_list|)
block|{
name|inputString
operator|.
name|insert
argument_list|(
name|cursorPosition
argument_list|,
name|character
argument_list|)
expr_stmt|;
name|cursorPosition
operator|+=
literal|1
expr_stmt|;
name|inputModeScreenView
operator|.
name|showInput
argument_list|(
name|message
argument_list|,
name|inputString
operator|.
name|toString
argument_list|()
argument_list|,
name|cursorPosition
argument_list|)
expr_stmt|;
name|inputModeScreenView
operator|.
name|refreshTerminal
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|backspace
parameter_list|()
block|{
if|if
condition|(
name|cursorPosition
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|inputString
operator|.
name|deleteCharAt
argument_list|(
name|cursorPosition
operator|-
literal|1
argument_list|)
expr_stmt|;
name|cursorPosition
operator|-=
literal|1
expr_stmt|;
name|inputModeScreenView
operator|.
name|showInput
argument_list|(
name|message
argument_list|,
name|inputString
operator|.
name|toString
argument_list|()
argument_list|,
name|cursorPosition
argument_list|)
expr_stmt|;
name|inputModeScreenView
operator|.
name|refreshTerminal
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|delete
parameter_list|()
block|{
if|if
condition|(
name|inputString
operator|.
name|length
argument_list|()
operator|==
literal|0
operator|||
name|cursorPosition
operator|>
name|inputString
operator|.
name|length
argument_list|()
operator|-
literal|1
condition|)
block|{
return|return;
block|}
name|inputString
operator|.
name|deleteCharAt
argument_list|(
name|cursorPosition
argument_list|)
expr_stmt|;
name|inputModeScreenView
operator|.
name|showInput
argument_list|(
name|message
argument_list|,
name|inputString
operator|.
name|toString
argument_list|()
argument_list|,
name|cursorPosition
argument_list|)
expr_stmt|;
name|inputModeScreenView
operator|.
name|refreshTerminal
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|arrowLeft
parameter_list|()
block|{
if|if
condition|(
name|cursorPosition
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|cursorPosition
operator|-=
literal|1
expr_stmt|;
name|inputModeScreenView
operator|.
name|showInput
argument_list|(
name|message
argument_list|,
name|inputString
operator|.
name|toString
argument_list|()
argument_list|,
name|cursorPosition
argument_list|)
expr_stmt|;
name|inputModeScreenView
operator|.
name|refreshTerminal
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|arrowRight
parameter_list|()
block|{
if|if
condition|(
name|cursorPosition
operator|>
name|inputString
operator|.
name|length
argument_list|()
operator|-
literal|1
condition|)
block|{
return|return;
block|}
name|cursorPosition
operator|+=
literal|1
expr_stmt|;
name|inputModeScreenView
operator|.
name|showInput
argument_list|(
name|message
argument_list|,
name|inputString
operator|.
name|toString
argument_list|()
argument_list|,
name|cursorPosition
argument_list|)
expr_stmt|;
name|inputModeScreenView
operator|.
name|refreshTerminal
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|home
parameter_list|()
block|{
name|cursorPosition
operator|=
literal|0
expr_stmt|;
name|inputModeScreenView
operator|.
name|showInput
argument_list|(
name|message
argument_list|,
name|inputString
operator|.
name|toString
argument_list|()
argument_list|,
name|cursorPosition
argument_list|)
expr_stmt|;
name|inputModeScreenView
operator|.
name|refreshTerminal
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|end
parameter_list|()
block|{
name|cursorPosition
operator|=
name|inputString
operator|.
name|length
argument_list|()
expr_stmt|;
name|inputModeScreenView
operator|.
name|showInput
argument_list|(
name|message
argument_list|,
name|inputString
operator|.
name|toString
argument_list|()
argument_list|,
name|cursorPosition
argument_list|)
expr_stmt|;
name|inputModeScreenView
operator|.
name|refreshTerminal
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|arrowUp
parameter_list|()
block|{
if|if
condition|(
name|historyPosition
operator|==
literal|0
operator|||
name|histories
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|historyPosition
operator|==
operator|-
literal|1
condition|)
block|{
name|historyPosition
operator|=
name|histories
operator|.
name|size
argument_list|()
operator|-
literal|1
expr_stmt|;
block|}
else|else
block|{
name|historyPosition
operator|-=
literal|1
expr_stmt|;
block|}
name|inputString
operator|=
operator|new
name|StringBuilder
argument_list|(
name|histories
operator|.
name|get
argument_list|(
name|historyPosition
argument_list|)
argument_list|)
expr_stmt|;
name|cursorPosition
operator|=
name|inputString
operator|.
name|length
argument_list|()
expr_stmt|;
name|inputModeScreenView
operator|.
name|showInput
argument_list|(
name|message
argument_list|,
name|inputString
operator|.
name|toString
argument_list|()
argument_list|,
name|cursorPosition
argument_list|)
expr_stmt|;
name|inputModeScreenView
operator|.
name|refreshTerminal
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|arrowDown
parameter_list|()
block|{
if|if
condition|(
name|historyPosition
operator|==
operator|-
literal|1
operator|||
name|histories
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|historyPosition
operator|==
name|histories
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|)
block|{
name|historyPosition
operator|=
operator|-
literal|1
expr_stmt|;
name|inputString
operator|=
operator|new
name|StringBuilder
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|historyPosition
operator|+=
literal|1
expr_stmt|;
name|inputString
operator|=
operator|new
name|StringBuilder
argument_list|(
name|histories
operator|.
name|get
argument_list|(
name|historyPosition
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|cursorPosition
operator|=
name|inputString
operator|.
name|length
argument_list|()
expr_stmt|;
name|inputModeScreenView
operator|.
name|showInput
argument_list|(
name|message
argument_list|,
name|inputString
operator|.
name|toString
argument_list|()
argument_list|,
name|cursorPosition
argument_list|)
expr_stmt|;
name|inputModeScreenView
operator|.
name|refreshTerminal
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit
