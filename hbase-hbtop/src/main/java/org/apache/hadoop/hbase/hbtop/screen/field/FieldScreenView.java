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
name|field
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumMap
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
comment|/**  * The screen where we can change the displayed fields, the sort key and the order of the fields.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|FieldScreenView
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
name|FIELD_START_ROW
init|=
literal|5
decl_stmt|;
specifier|private
specifier|final
name|FieldScreenPresenter
name|fieldScreenPresenter
decl_stmt|;
specifier|public
name|FieldScreenView
parameter_list|(
name|Screen
name|screen
parameter_list|,
name|Terminal
name|terminal
parameter_list|,
name|Field
name|sortField
parameter_list|,
name|List
argument_list|<
name|Field
argument_list|>
name|fields
parameter_list|,
name|EnumMap
argument_list|<
name|Field
argument_list|,
name|Boolean
argument_list|>
name|fieldDisplayMap
parameter_list|,
name|FieldScreenPresenter
operator|.
name|ResultListener
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
name|fieldScreenPresenter
operator|=
operator|new
name|FieldScreenPresenter
argument_list|(
name|this
argument_list|,
name|sortField
argument_list|,
name|fields
argument_list|,
name|fieldDisplayMap
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
name|fieldScreenPresenter
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
name|fieldScreenPresenter
operator|.
name|transitionToNextScreen
argument_list|()
return|;
case|case
name|ArrowUp
case|:
name|fieldScreenPresenter
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
name|fieldScreenPresenter
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
name|fieldScreenPresenter
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
name|fieldScreenPresenter
operator|.
name|pageDown
argument_list|()
expr_stmt|;
return|return
name|this
return|;
case|case
name|ArrowRight
case|:
name|fieldScreenPresenter
operator|.
name|turnOnMoveMode
argument_list|()
expr_stmt|;
return|return
name|this
return|;
case|case
name|ArrowLeft
case|:
case|case
name|Enter
case|:
name|fieldScreenPresenter
operator|.
name|turnOffMoveMode
argument_list|()
expr_stmt|;
return|return
name|this
return|;
default|default:
comment|// Do nothing
break|break;
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
literal|'d'
case|:
case|case
literal|' '
case|:
name|fieldScreenPresenter
operator|.
name|switchFieldDisplay
argument_list|()
expr_stmt|;
break|break;
case|case
literal|'s'
case|:
name|fieldScreenPresenter
operator|.
name|setSortField
argument_list|()
expr_stmt|;
break|break;
case|case
literal|'q'
case|:
return|return
name|fieldScreenPresenter
operator|.
name|transitionToNextScreen
argument_list|()
return|;
default|default:
comment|// Do nothing
break|break;
block|}
return|return
name|this
return|;
block|}
specifier|public
name|void
name|showFieldScreen
parameter_list|(
name|String
name|sortFieldHeader
parameter_list|,
name|List
argument_list|<
name|Field
argument_list|>
name|fields
parameter_list|,
name|EnumMap
argument_list|<
name|Field
argument_list|,
name|Boolean
argument_list|>
name|fieldDisplayMap
parameter_list|,
name|int
name|currentPosition
parameter_list|,
name|int
name|headerMaxLength
parameter_list|,
name|int
name|descriptionMaxLength
parameter_list|,
name|boolean
name|moveMode
parameter_list|)
block|{
name|showScreenDescription
argument_list|(
name|sortFieldHeader
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
name|fields
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Field
name|field
init|=
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|showField
argument_list|(
name|i
argument_list|,
name|field
argument_list|,
name|fieldDisplayMap
operator|.
name|get
argument_list|(
name|field
argument_list|)
argument_list|,
name|i
operator|==
name|currentPosition
argument_list|,
name|headerMaxLength
argument_list|,
name|descriptionMaxLength
argument_list|,
name|moveMode
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|showScreenDescription
parameter_list|(
name|String
name|sortFieldHeader
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
literal|"Fields Management"
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
literal|"Current Sort Field: "
argument_list|)
operator|.
name|startBold
argument_list|()
operator|.
name|print
argument_list|(
name|sortFieldHeader
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
literal|"Navigate with up/down, Right selects for move then<Enter> or Left commits,"
argument_list|)
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
name|printer
operator|.
name|print
argument_list|(
literal|"'d' or<Space> toggles display, 's' sets sort. Use 'q' or<Esc> to end!"
argument_list|)
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|showField
parameter_list|(
name|int
name|pos
parameter_list|,
name|Field
name|field
parameter_list|,
name|boolean
name|display
parameter_list|,
name|boolean
name|selected
parameter_list|,
name|int
name|fieldHeaderMaxLength
parameter_list|,
name|int
name|fieldDescriptionMaxLength
parameter_list|,
name|boolean
name|moveMode
parameter_list|)
block|{
name|String
name|fieldHeader
init|=
name|String
operator|.
name|format
argument_list|(
literal|"%-"
operator|+
name|fieldHeaderMaxLength
operator|+
literal|"s"
argument_list|,
name|field
operator|.
name|getHeader
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|fieldDescription
init|=
name|String
operator|.
name|format
argument_list|(
literal|"%-"
operator|+
name|fieldDescriptionMaxLength
operator|+
literal|"s"
argument_list|,
name|field
operator|.
name|getDescription
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|row
init|=
name|FIELD_START_ROW
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
name|String
name|prefix
init|=
name|display
condition|?
literal|"* "
else|:
literal|"  "
decl_stmt|;
if|if
condition|(
name|moveMode
condition|)
block|{
name|printer
operator|.
name|print
argument_list|(
name|prefix
argument_list|)
expr_stmt|;
if|if
condition|(
name|display
condition|)
block|{
name|printer
operator|.
name|startBold
argument_list|()
expr_stmt|;
block|}
name|printer
operator|.
name|startHighlight
argument_list|()
operator|.
name|printFormat
argument_list|(
literal|"%s = %s"
argument_list|,
name|fieldHeader
argument_list|,
name|fieldDescription
argument_list|)
operator|.
name|stopHighlight
argument_list|()
expr_stmt|;
if|if
condition|(
name|display
condition|)
block|{
name|printer
operator|.
name|stopBold
argument_list|()
expr_stmt|;
block|}
name|printer
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|printer
operator|.
name|print
argument_list|(
name|prefix
argument_list|)
expr_stmt|;
if|if
condition|(
name|display
condition|)
block|{
name|printer
operator|.
name|startBold
argument_list|()
expr_stmt|;
block|}
name|printer
operator|.
name|startHighlight
argument_list|()
operator|.
name|print
argument_list|(
name|fieldHeader
argument_list|)
operator|.
name|stopHighlight
argument_list|()
operator|.
name|printFormat
argument_list|(
literal|" = %s"
argument_list|,
name|fieldDescription
argument_list|)
expr_stmt|;
if|if
condition|(
name|display
condition|)
block|{
name|printer
operator|.
name|stopBold
argument_list|()
expr_stmt|;
block|}
name|printer
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|display
condition|)
block|{
name|printer
operator|.
name|print
argument_list|(
literal|"* "
argument_list|)
operator|.
name|startBold
argument_list|()
operator|.
name|printFormat
argument_list|(
literal|"%s = %s"
argument_list|,
name|fieldHeader
argument_list|,
name|fieldDescription
argument_list|)
operator|.
name|stopBold
argument_list|()
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
literal|"  %s = %s"
argument_list|,
name|fieldHeader
argument_list|,
name|fieldDescription
argument_list|)
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

