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
name|client
operator|.
name|Admin
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
name|Record
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
name|hadoop
operator|.
name|hbase
operator|.
name|hbtop
operator|.
name|terminal
operator|.
name|TerminalSize
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
comment|/**  * The screen that provides a dynamic real-time view for the HBase metrics.  *  * This shows the metric {@link Summary} and the metric {@link Record}s. The summary and the  * metrics are updated periodically (3 seconds by default).  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|TopScreenView
extends|extends
name|AbstractScreenView
block|{
specifier|private
specifier|static
specifier|final
name|int
name|SUMMARY_START_ROW
init|=
literal|0
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|SUMMARY_ROW_NUM
init|=
literal|7
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MESSAGE_ROW
init|=
literal|7
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|RECORD_HEADER_ROW
init|=
literal|8
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|RECORD_START_ROW
init|=
literal|9
decl_stmt|;
specifier|private
specifier|final
name|TopScreenPresenter
name|topScreenPresenter
decl_stmt|;
specifier|private
name|int
name|pageSize
decl_stmt|;
specifier|public
name|TopScreenView
parameter_list|(
name|Screen
name|screen
parameter_list|,
name|Terminal
name|terminal
parameter_list|,
name|long
name|initialRefreshDelay
parameter_list|,
name|Admin
name|admin
parameter_list|,
name|Mode
name|initialMode
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
name|topScreenPresenter
operator|=
operator|new
name|TopScreenPresenter
argument_list|(
name|this
argument_list|,
name|initialRefreshDelay
argument_list|,
operator|new
name|TopScreenModel
argument_list|(
name|admin
argument_list|,
name|initialMode
argument_list|)
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
name|topScreenPresenter
operator|.
name|init
argument_list|()
expr_stmt|;
name|long
name|delay
init|=
name|topScreenPresenter
operator|.
name|refresh
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|setTimer
argument_list|(
name|delay
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ScreenView
name|handleTimer
parameter_list|()
block|{
name|long
name|delay
init|=
name|topScreenPresenter
operator|.
name|refresh
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|setTimer
argument_list|(
name|delay
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Nullable
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
name|topScreenPresenter
operator|.
name|refresh
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|this
return|;
case|case
name|ArrowUp
case|:
name|topScreenPresenter
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
name|topScreenPresenter
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
return|return
name|this
return|;
case|case
name|ArrowLeft
case|:
name|topScreenPresenter
operator|.
name|arrowLeft
argument_list|()
expr_stmt|;
return|return
name|this
return|;
case|case
name|ArrowRight
case|:
name|topScreenPresenter
operator|.
name|arrowRight
argument_list|()
expr_stmt|;
return|return
name|this
return|;
case|case
name|PageUp
case|:
name|topScreenPresenter
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
name|topScreenPresenter
operator|.
name|pageDown
argument_list|()
expr_stmt|;
return|return
name|this
return|;
case|case
name|Home
case|:
name|topScreenPresenter
operator|.
name|home
argument_list|()
expr_stmt|;
return|return
name|this
return|;
case|case
name|End
case|:
name|topScreenPresenter
operator|.
name|end
argument_list|()
expr_stmt|;
return|return
name|this
return|;
case|case
name|Escape
case|:
return|return
literal|null
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
name|unknownCommandMessage
argument_list|()
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
literal|'R'
case|:
name|topScreenPresenter
operator|.
name|switchSortOrder
argument_list|()
expr_stmt|;
break|break;
case|case
literal|'f'
case|:
name|cancelTimer
argument_list|()
expr_stmt|;
return|return
name|topScreenPresenter
operator|.
name|transitionToFieldScreen
argument_list|(
name|getScreen
argument_list|()
argument_list|,
name|getTerminal
argument_list|()
argument_list|)
return|;
case|case
literal|'m'
case|:
name|cancelTimer
argument_list|()
expr_stmt|;
return|return
name|topScreenPresenter
operator|.
name|transitionToModeScreen
argument_list|(
name|getScreen
argument_list|()
argument_list|,
name|getTerminal
argument_list|()
argument_list|)
return|;
case|case
literal|'h'
case|:
name|cancelTimer
argument_list|()
expr_stmt|;
return|return
name|topScreenPresenter
operator|.
name|transitionToHelpScreen
argument_list|(
name|getScreen
argument_list|()
argument_list|,
name|getTerminal
argument_list|()
argument_list|)
return|;
case|case
literal|'d'
case|:
name|cancelTimer
argument_list|()
expr_stmt|;
return|return
name|topScreenPresenter
operator|.
name|goToInputModeForRefreshDelay
argument_list|(
name|getScreen
argument_list|()
argument_list|,
name|getTerminal
argument_list|()
argument_list|,
name|MESSAGE_ROW
argument_list|)
return|;
case|case
literal|'o'
case|:
name|cancelTimer
argument_list|()
expr_stmt|;
if|if
condition|(
name|keyPress
operator|.
name|isCtrl
argument_list|()
condition|)
block|{
return|return
name|topScreenPresenter
operator|.
name|goToFilterDisplayMode
argument_list|(
name|getScreen
argument_list|()
argument_list|,
name|getTerminal
argument_list|()
argument_list|,
name|MESSAGE_ROW
argument_list|)
return|;
block|}
return|return
name|topScreenPresenter
operator|.
name|goToInputModeForFilter
argument_list|(
name|getScreen
argument_list|()
argument_list|,
name|getTerminal
argument_list|()
argument_list|,
name|MESSAGE_ROW
argument_list|,
literal|true
argument_list|)
return|;
case|case
literal|'O'
case|:
name|cancelTimer
argument_list|()
expr_stmt|;
return|return
name|topScreenPresenter
operator|.
name|goToInputModeForFilter
argument_list|(
name|getScreen
argument_list|()
argument_list|,
name|getTerminal
argument_list|()
argument_list|,
name|MESSAGE_ROW
argument_list|,
literal|false
argument_list|)
return|;
case|case
literal|'='
case|:
name|topScreenPresenter
operator|.
name|clearFilters
argument_list|()
expr_stmt|;
break|break;
case|case
literal|'X'
case|:
name|topScreenPresenter
operator|.
name|adjustFieldLength
argument_list|()
expr_stmt|;
break|break;
case|case
literal|'i'
case|:
name|topScreenPresenter
operator|.
name|drillDown
argument_list|()
expr_stmt|;
break|break;
case|case
literal|'q'
case|:
return|return
literal|null
return|;
default|default:
return|return
name|unknownCommandMessage
argument_list|()
return|;
block|}
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|TerminalSize
name|getTerminalSize
parameter_list|()
block|{
name|TerminalSize
name|terminalSize
init|=
name|super
operator|.
name|getTerminalSize
argument_list|()
decl_stmt|;
name|updatePageSize
argument_list|(
name|terminalSize
argument_list|)
expr_stmt|;
return|return
name|terminalSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|TerminalSize
name|doResizeIfNecessary
parameter_list|()
block|{
name|TerminalSize
name|terminalSize
init|=
name|super
operator|.
name|doResizeIfNecessary
argument_list|()
decl_stmt|;
if|if
condition|(
name|terminalSize
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|updatePageSize
argument_list|(
name|terminalSize
argument_list|)
expr_stmt|;
return|return
name|terminalSize
return|;
block|}
specifier|private
name|void
name|updatePageSize
parameter_list|(
name|TerminalSize
name|terminalSize
parameter_list|)
block|{
name|pageSize
operator|=
name|terminalSize
operator|.
name|getRows
argument_list|()
operator|-
name|SUMMARY_ROW_NUM
operator|-
literal|2
expr_stmt|;
if|if
condition|(
name|pageSize
operator|<
literal|0
condition|)
block|{
name|pageSize
operator|=
literal|0
expr_stmt|;
block|}
block|}
specifier|public
name|int
name|getPageSize
parameter_list|()
block|{
return|return
name|pageSize
return|;
block|}
specifier|public
name|void
name|showTopScreen
parameter_list|(
name|Summary
name|summary
parameter_list|,
name|List
argument_list|<
name|Header
argument_list|>
name|headers
parameter_list|,
name|List
argument_list|<
name|Record
argument_list|>
name|records
parameter_list|,
name|Record
name|selectedRecord
parameter_list|)
block|{
name|showSummary
argument_list|(
name|summary
argument_list|)
expr_stmt|;
name|clearMessage
argument_list|()
expr_stmt|;
name|showHeaders
argument_list|(
name|headers
argument_list|)
expr_stmt|;
name|showRecords
argument_list|(
name|headers
argument_list|,
name|records
argument_list|,
name|selectedRecord
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|showSummary
parameter_list|(
name|Summary
name|summary
parameter_list|)
block|{
name|TerminalPrinter
name|printer
init|=
name|getTerminalPrinter
argument_list|(
name|SUMMARY_START_ROW
argument_list|)
decl_stmt|;
name|printer
operator|.
name|print
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"HBase hbtop - %s"
argument_list|,
name|summary
operator|.
name|getCurrentTime
argument_list|()
argument_list|)
argument_list|)
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
name|printer
operator|.
name|print
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Version: %s"
argument_list|,
name|summary
operator|.
name|getVersion
argument_list|()
argument_list|)
argument_list|)
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
name|printer
operator|.
name|print
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Cluster ID: %s"
argument_list|,
name|summary
operator|.
name|getClusterId
argument_list|()
argument_list|)
argument_list|)
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
name|printer
operator|.
name|print
argument_list|(
literal|"RegionServer(s): "
argument_list|)
operator|.
name|startBold
argument_list|()
operator|.
name|print
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|summary
operator|.
name|getServers
argument_list|()
argument_list|)
argument_list|)
operator|.
name|stopBold
argument_list|()
operator|.
name|print
argument_list|(
literal|" total, "
argument_list|)
operator|.
name|startBold
argument_list|()
operator|.
name|print
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|summary
operator|.
name|getLiveServers
argument_list|()
argument_list|)
argument_list|)
operator|.
name|stopBold
argument_list|()
operator|.
name|print
argument_list|(
literal|" live, "
argument_list|)
operator|.
name|startBold
argument_list|()
operator|.
name|print
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|summary
operator|.
name|getDeadServers
argument_list|()
argument_list|)
argument_list|)
operator|.
name|stopBold
argument_list|()
operator|.
name|print
argument_list|(
literal|" dead"
argument_list|)
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
name|printer
operator|.
name|print
argument_list|(
literal|"RegionCount: "
argument_list|)
operator|.
name|startBold
argument_list|()
operator|.
name|print
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|summary
operator|.
name|getRegionCount
argument_list|()
argument_list|)
argument_list|)
operator|.
name|stopBold
argument_list|()
operator|.
name|print
argument_list|(
literal|" total, "
argument_list|)
operator|.
name|startBold
argument_list|()
operator|.
name|print
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|summary
operator|.
name|getRitCount
argument_list|()
argument_list|)
argument_list|)
operator|.
name|stopBold
argument_list|()
operator|.
name|print
argument_list|(
literal|" rit"
argument_list|)
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
name|printer
operator|.
name|print
argument_list|(
literal|"Average Cluster Load: "
argument_list|)
operator|.
name|startBold
argument_list|()
operator|.
name|print
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%.2f"
argument_list|,
name|summary
operator|.
name|getAverageLoad
argument_list|()
argument_list|)
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
literal|"Aggregate Request/s: "
argument_list|)
operator|.
name|startBold
argument_list|()
operator|.
name|print
argument_list|(
name|Long
operator|.
name|toString
argument_list|(
name|summary
operator|.
name|getAggregateRequestPerSecond
argument_list|()
argument_list|)
argument_list|)
operator|.
name|stopBold
argument_list|()
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|showRecords
parameter_list|(
name|List
argument_list|<
name|Header
argument_list|>
name|headers
parameter_list|,
name|List
argument_list|<
name|Record
argument_list|>
name|records
parameter_list|,
name|Record
name|selectedRecord
parameter_list|)
block|{
name|TerminalPrinter
name|printer
init|=
name|getTerminalPrinter
argument_list|(
name|RECORD_START_ROW
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|buf
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|headers
operator|.
name|size
argument_list|()
argument_list|)
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
name|pageSize
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|<
name|records
operator|.
name|size
argument_list|()
condition|)
block|{
name|Record
name|record
init|=
name|records
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|buf
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|Header
name|header
range|:
name|headers
control|)
block|{
name|String
name|value
init|=
literal|""
decl_stmt|;
if|if
condition|(
name|record
operator|.
name|containsKey
argument_list|(
name|header
operator|.
name|getField
argument_list|()
argument_list|)
condition|)
block|{
name|value
operator|=
name|record
operator|.
name|get
argument_list|(
name|header
operator|.
name|getField
argument_list|()
argument_list|)
operator|.
name|asString
argument_list|()
expr_stmt|;
block|}
name|buf
operator|.
name|add
argument_list|(
name|limitLineLength
argument_list|(
name|String
operator|.
name|format
argument_list|(
name|header
operator|.
name|format
argument_list|()
argument_list|,
name|value
argument_list|)
argument_list|,
name|header
operator|.
name|getLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|recordString
init|=
name|String
operator|.
name|join
argument_list|(
literal|" "
argument_list|,
name|buf
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|recordString
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|recordString
operator|+=
literal|" "
expr_stmt|;
block|}
if|if
condition|(
name|record
operator|==
name|selectedRecord
condition|)
block|{
name|printer
operator|.
name|startHighlight
argument_list|()
operator|.
name|print
argument_list|(
name|recordString
argument_list|)
operator|.
name|stopHighlight
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
name|print
argument_list|(
name|recordString
argument_list|)
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|printer
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|showHeaders
parameter_list|(
name|List
argument_list|<
name|Header
argument_list|>
name|headers
parameter_list|)
block|{
name|String
name|header
init|=
name|headers
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|h
lambda|->
name|String
operator|.
name|format
argument_list|(
name|h
operator|.
name|format
argument_list|()
argument_list|,
name|h
operator|.
name|getField
argument_list|()
operator|.
name|getHeader
argument_list|()
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|joining
argument_list|(
literal|" "
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|header
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|header
operator|+=
literal|" "
expr_stmt|;
block|}
name|getTerminalPrinter
argument_list|(
name|RECORD_HEADER_ROW
argument_list|)
operator|.
name|startHighlight
argument_list|()
operator|.
name|print
argument_list|(
name|header
argument_list|)
operator|.
name|stopHighlight
argument_list|()
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
block|}
specifier|private
name|String
name|limitLineLength
parameter_list|(
name|String
name|line
parameter_list|,
name|int
name|length
parameter_list|)
block|{
if|if
condition|(
name|line
operator|.
name|length
argument_list|()
operator|>
name|length
condition|)
block|{
return|return
name|line
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|length
operator|-
literal|1
argument_list|)
operator|+
literal|"+"
return|;
block|}
return|return
name|line
return|;
block|}
specifier|private
name|void
name|clearMessage
parameter_list|()
block|{
name|getTerminalPrinter
argument_list|(
name|MESSAGE_ROW
argument_list|)
operator|.
name|print
argument_list|(
literal|""
argument_list|)
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
block|}
specifier|private
name|ScreenView
name|unknownCommandMessage
parameter_list|()
block|{
name|cancelTimer
argument_list|()
expr_stmt|;
return|return
name|topScreenPresenter
operator|.
name|goToMessageMode
argument_list|(
name|getScreen
argument_list|()
argument_list|,
name|getTerminal
argument_list|()
argument_list|,
name|MESSAGE_ROW
argument_list|,
literal|"Unknown command - try 'h' for help"
argument_list|)
return|;
block|}
block|}
end_class

end_unit

