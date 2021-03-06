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
name|hbtop
operator|.
name|screen
operator|.
name|AbstractScreenView
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
name|Screen
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
name|hbtop
operator|.
name|terminal
operator|.
name|KeyPress
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
name|terminal
operator|.
name|Terminal
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
comment|/**  * The input mode in the top screen.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|InputModeScreenView
extends|extends
name|AbstractScreenView
block|{
specifier|private
specifier|final
name|int
name|row
decl_stmt|;
specifier|private
specifier|final
name|InputModeScreenPresenter
name|inputModeScreenPresenter
decl_stmt|;
specifier|public
name|InputModeScreenView
parameter_list|(
name|Screen
name|screen
parameter_list|,
name|Terminal
name|terminal
parameter_list|,
name|int
name|row
parameter_list|,
name|String
name|message
parameter_list|,
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
name|super
argument_list|(
name|screen
argument_list|,
name|terminal
argument_list|)
expr_stmt|;
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
name|this
operator|.
name|inputModeScreenPresenter
operator|=
operator|new
name|InputModeScreenPresenter
argument_list|(
name|this
argument_list|,
name|message
argument_list|,
name|histories
argument_list|,
name|resultListener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|()
block|{
name|inputModeScreenPresenter
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ScreenView
name|handleKeyPress
parameter_list|(
name|KeyPress
name|keyPress
parameter_list|)
block|{
switch|switch
condition|(
name|keyPress
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|Enter
case|:
return|return
name|inputModeScreenPresenter
operator|.
name|returnToNextScreen
argument_list|()
return|;
case|case
name|Character
case|:
name|inputModeScreenPresenter
operator|.
name|character
argument_list|(
name|keyPress
operator|.
name|getCharacter
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|Backspace
case|:
name|inputModeScreenPresenter
operator|.
name|backspace
argument_list|()
expr_stmt|;
break|break;
case|case
name|Delete
case|:
name|inputModeScreenPresenter
operator|.
name|delete
argument_list|()
expr_stmt|;
break|break;
case|case
name|ArrowLeft
case|:
name|inputModeScreenPresenter
operator|.
name|arrowLeft
argument_list|()
expr_stmt|;
break|break;
case|case
name|ArrowRight
case|:
name|inputModeScreenPresenter
operator|.
name|arrowRight
argument_list|()
expr_stmt|;
break|break;
case|case
name|Home
case|:
name|inputModeScreenPresenter
operator|.
name|home
argument_list|()
expr_stmt|;
break|break;
case|case
name|End
case|:
name|inputModeScreenPresenter
operator|.
name|end
argument_list|()
expr_stmt|;
break|break;
case|case
name|ArrowUp
case|:
name|inputModeScreenPresenter
operator|.
name|arrowUp
argument_list|()
expr_stmt|;
break|break;
case|case
name|ArrowDown
case|:
name|inputModeScreenPresenter
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
break|break;
default|default:
break|break;
block|}
return|return
name|this
return|;
block|}
specifier|public
name|void
name|showInput
parameter_list|(
name|String
name|message
parameter_list|,
name|String
name|inputString
parameter_list|,
name|int
name|cursorPosition
parameter_list|)
block|{
name|getTerminalPrinter
argument_list|(
name|row
argument_list|)
operator|.
name|startBold
argument_list|()
operator|.
name|print
argument_list|(
name|message
argument_list|)
operator|.
name|stopBold
argument_list|()
operator|.
name|print
argument_list|(
literal|" "
argument_list|)
operator|.
name|print
argument_list|(
name|inputString
argument_list|)
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
name|setCursorPosition
argument_list|(
name|message
operator|.
name|length
argument_list|()
operator|+
literal|1
operator|+
name|cursorPosition
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|refreshTerminal
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

