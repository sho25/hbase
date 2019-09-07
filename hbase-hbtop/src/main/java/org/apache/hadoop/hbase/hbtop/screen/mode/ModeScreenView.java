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
name|mode
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
name|Consumer
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
name|hadoop
operator|.
name|hbase
operator|.
name|hbtop
operator|.
name|terminal
operator|.
name|TerminalPrinter
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
comment|/**  * The screen where we can choose the {@link Mode} in the top screen.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ModeScreenView
extends|extends
name|AbstractScreenView
block|{
specifier|private
specifier|static
specifier|final
name|int
name|SCREEN_DESCRIPTION_START_ROW
init|=
literal|0
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MODE_START_ROW
init|=
literal|4
decl_stmt|;
specifier|private
specifier|final
name|ModeScreenPresenter
name|modeScreenPresenter
decl_stmt|;
specifier|public
name|ModeScreenView
parameter_list|(
name|Screen
name|screen
parameter_list|,
name|Terminal
name|terminal
parameter_list|,
name|Mode
name|currentMode
parameter_list|,
name|Consumer
argument_list|<
name|Mode
argument_list|>
name|resultListener
parameter_list|,
name|ScreenView
name|nextScreenView
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
name|modeScreenPresenter
operator|=
operator|new
name|ModeScreenPresenter
argument_list|(
name|this
argument_list|,
name|currentMode
argument_list|,
name|resultListener
argument_list|,
name|nextScreenView
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
name|modeScreenPresenter
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
name|Escape
case|:
return|return
name|modeScreenPresenter
operator|.
name|transitionToNextScreen
argument_list|(
literal|false
argument_list|)
return|;
case|case
name|Enter
case|:
return|return
name|modeScreenPresenter
operator|.
name|transitionToNextScreen
argument_list|(
literal|true
argument_list|)
return|;
case|case
name|ArrowUp
case|:
name|modeScreenPresenter
operator|.
name|arrowUp
argument_list|()
expr_stmt|;
return|return
name|this
return|;
case|case
name|ArrowDown
case|:
name|modeScreenPresenter
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
return|return
name|this
return|;
case|case
name|PageUp
case|:
case|case
name|Home
case|:
name|modeScreenPresenter
operator|.
name|pageUp
argument_list|()
expr_stmt|;
return|return
name|this
return|;
case|case
name|PageDown
case|:
case|case
name|End
case|:
name|modeScreenPresenter
operator|.
name|pageDown
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
if|if
condition|(
name|keyPress
operator|.
name|getType
argument_list|()
operator|!=
name|KeyPress
operator|.
name|Type
operator|.
name|Character
condition|)
block|{
return|return
name|this
return|;
block|}
assert|assert
name|keyPress
operator|.
name|getCharacter
argument_list|()
operator|!=
literal|null
assert|;
switch|switch
condition|(
name|keyPress
operator|.
name|getCharacter
argument_list|()
condition|)
block|{
case|case
literal|'q'
case|:
return|return
name|modeScreenPresenter
operator|.
name|transitionToNextScreen
argument_list|(
literal|false
argument_list|)
return|;
block|}
return|return
name|this
return|;
block|}
specifier|public
name|void
name|showModeScreen
parameter_list|(
name|Mode
name|currentMode
parameter_list|,
name|List
argument_list|<
name|Mode
argument_list|>
name|modes
parameter_list|,
name|int
name|currentPosition
parameter_list|,
name|int
name|modeHeaderMaxLength
parameter_list|,
name|int
name|modeDescriptionMaxLength
parameter_list|)
block|{
name|showScreenDescription
argument_list|(
name|currentMode
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|modes
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|showMode
argument_list|(
name|i
argument_list|,
name|modes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|i
operator|==
name|currentPosition
argument_list|,
name|modeHeaderMaxLength
argument_list|,
name|modeDescriptionMaxLength
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|showScreenDescription
parameter_list|(
name|Mode
name|currentMode
parameter_list|)
block|{
name|TerminalPrinter
name|printer
init|=
name|getTerminalPrinter
argument_list|(
name|SCREEN_DESCRIPTION_START_ROW
argument_list|)
decl_stmt|;
name|printer
operator|.
name|startBold
argument_list|()
operator|.
name|print
argument_list|(
literal|"Mode Management"
argument_list|)
operator|.
name|stopBold
argument_list|()
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
name|printer
operator|.
name|print
argument_list|(
literal|"Current mode: "
argument_list|)
operator|.
name|startBold
argument_list|()
operator|.
name|print
argument_list|(
name|currentMode
operator|.
name|getHeader
argument_list|()
argument_list|)
operator|.
name|stopBold
argument_list|()
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
name|printer
operator|.
name|print
argument_list|(
literal|"Select mode followed by<Enter>"
argument_list|)
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|showMode
parameter_list|(
name|int
name|pos
parameter_list|,
name|Mode
name|mode
parameter_list|,
name|boolean
name|selected
parameter_list|,
name|int
name|modeHeaderMaxLength
parameter_list|,
name|int
name|modeDescriptionMaxLength
parameter_list|)
block|{
name|String
name|modeHeader
init|=
name|String
operator|.
name|format
argument_list|(
literal|"%-"
operator|+
name|modeHeaderMaxLength
operator|+
literal|"s"
argument_list|,
name|mode
operator|.
name|getHeader
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|modeDescription
init|=
name|String
operator|.
name|format
argument_list|(
literal|"%-"
operator|+
name|modeDescriptionMaxLength
operator|+
literal|"s"
argument_list|,
name|mode
operator|.
name|getDescription
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|row
init|=
name|MODE_START_ROW
operator|+
name|pos
decl_stmt|;
name|TerminalPrinter
name|printer
init|=
name|getTerminalPrinter
argument_list|(
name|row
argument_list|)
decl_stmt|;
if|if
condition|(
name|selected
condition|)
block|{
name|printer
operator|.
name|startHighlight
argument_list|()
operator|.
name|print
argument_list|(
name|modeHeader
argument_list|)
operator|.
name|stopHighlight
argument_list|()
operator|.
name|printFormat
argument_list|(
literal|" = %s"
argument_list|,
name|modeDescription
argument_list|)
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|printer
operator|.
name|printFormat
argument_list|(
literal|"%s = %s"
argument_list|,
name|modeHeader
argument_list|,
name|modeDescription
argument_list|)
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

