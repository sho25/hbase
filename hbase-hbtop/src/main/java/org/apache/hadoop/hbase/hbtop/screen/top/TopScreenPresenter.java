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
name|ArrayList
import|;
end_import

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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
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
name|screen
operator|.
name|field
operator|.
name|FieldScreenView
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
name|help
operator|.
name|HelpScreenView
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
name|mode
operator|.
name|ModeScreenView
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
comment|/**  * The presentation logic for the top screen.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|TopScreenPresenter
block|{
specifier|private
specifier|final
name|TopScreenView
name|topScreenView
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|refreshDelay
decl_stmt|;
specifier|private
name|long
name|lastRefreshTimestamp
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|adjustFieldLength
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|TopScreenModel
name|topScreenModel
decl_stmt|;
specifier|private
name|int
name|terminalLength
decl_stmt|;
specifier|private
name|int
name|horizontalScroll
decl_stmt|;
specifier|private
specifier|final
name|Paging
name|paging
init|=
operator|new
name|Paging
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|EnumMap
argument_list|<
name|Field
argument_list|,
name|Boolean
argument_list|>
name|fieldDisplayMap
init|=
operator|new
name|EnumMap
argument_list|<>
argument_list|(
name|Field
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|EnumMap
argument_list|<
name|Field
argument_list|,
name|Integer
argument_list|>
name|fieldLengthMap
init|=
operator|new
name|EnumMap
argument_list|<>
argument_list|(
name|Field
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|TopScreenPresenter
parameter_list|(
name|TopScreenView
name|topScreenView
parameter_list|,
name|long
name|initialRefreshDelay
parameter_list|,
name|TopScreenModel
name|topScreenModel
parameter_list|)
block|{
name|this
operator|.
name|topScreenView
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|topScreenView
argument_list|)
expr_stmt|;
name|this
operator|.
name|refreshDelay
operator|=
operator|new
name|AtomicLong
argument_list|(
name|initialRefreshDelay
argument_list|)
expr_stmt|;
name|this
operator|.
name|topScreenModel
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|topScreenModel
argument_list|)
expr_stmt|;
name|initFieldDisplayMapAndFieldLengthMap
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|()
block|{
name|terminalLength
operator|=
name|topScreenView
operator|.
name|getTerminalSize
argument_list|()
operator|.
name|getColumns
argument_list|()
expr_stmt|;
name|paging
operator|.
name|updatePageSize
argument_list|(
name|topScreenView
operator|.
name|getPageSize
argument_list|()
argument_list|)
expr_stmt|;
name|topScreenView
operator|.
name|hideCursor
argument_list|()
expr_stmt|;
block|}
specifier|public
name|long
name|refresh
parameter_list|(
name|boolean
name|force
parameter_list|)
block|{
if|if
condition|(
operator|!
name|force
condition|)
block|{
name|long
name|delay
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|lastRefreshTimestamp
decl_stmt|;
if|if
condition|(
name|delay
operator|<
name|refreshDelay
operator|.
name|get
argument_list|()
condition|)
block|{
return|return
name|refreshDelay
operator|.
name|get
argument_list|()
operator|-
name|delay
return|;
block|}
block|}
name|TerminalSize
name|newTerminalSize
init|=
name|topScreenView
operator|.
name|doResizeIfNecessary
argument_list|()
decl_stmt|;
if|if
condition|(
name|newTerminalSize
operator|!=
literal|null
condition|)
block|{
name|terminalLength
operator|=
name|newTerminalSize
operator|.
name|getColumns
argument_list|()
expr_stmt|;
name|paging
operator|.
name|updatePageSize
argument_list|(
name|topScreenView
operator|.
name|getPageSize
argument_list|()
argument_list|)
expr_stmt|;
name|topScreenView
operator|.
name|clearTerminal
argument_list|()
expr_stmt|;
block|}
name|topScreenModel
operator|.
name|refreshMetricsData
argument_list|()
expr_stmt|;
name|paging
operator|.
name|updateRecordsSize
argument_list|(
name|topScreenModel
operator|.
name|getRecords
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|adjustFieldLengthIfNeeded
argument_list|()
expr_stmt|;
name|topScreenView
operator|.
name|showTopScreen
argument_list|(
name|topScreenModel
operator|.
name|getSummary
argument_list|()
argument_list|,
name|getDisplayedHeaders
argument_list|()
argument_list|,
name|getDisplayedRecords
argument_list|()
argument_list|,
name|getSelectedRecord
argument_list|()
argument_list|)
expr_stmt|;
name|topScreenView
operator|.
name|refreshTerminal
argument_list|()
expr_stmt|;
name|lastRefreshTimestamp
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
return|return
name|refreshDelay
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|void
name|adjustFieldLength
parameter_list|()
block|{
name|adjustFieldLength
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|refresh
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|adjustFieldLengthIfNeeded
parameter_list|()
block|{
if|if
condition|(
name|adjustFieldLength
operator|.
name|get
argument_list|()
condition|)
block|{
name|adjustFieldLength
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
for|for
control|(
name|Field
name|f
range|:
name|topScreenModel
operator|.
name|getFields
argument_list|()
control|)
block|{
if|if
condition|(
name|f
operator|.
name|isAutoAdjust
argument_list|()
condition|)
block|{
name|int
name|maxLength
init|=
name|topScreenModel
operator|.
name|getRecords
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|r
lambda|->
name|r
operator|.
name|get
argument_list|(
name|f
argument_list|)
operator|.
name|asString
argument_list|()
operator|.
name|length
argument_list|()
argument_list|)
operator|.
name|max
argument_list|(
name|Integer
operator|::
name|compareTo
argument_list|)
operator|.
name|orElse
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|fieldLengthMap
operator|.
name|put
argument_list|(
name|f
argument_list|,
name|Math
operator|.
name|max
argument_list|(
name|maxLength
argument_list|,
name|f
operator|.
name|getHeader
argument_list|()
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|List
argument_list|<
name|Header
argument_list|>
name|getDisplayedHeaders
parameter_list|()
block|{
name|List
argument_list|<
name|Field
argument_list|>
name|displayFields
init|=
name|topScreenModel
operator|.
name|getFields
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|fieldDisplayMap
operator|::
name|get
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
if|if
condition|(
name|displayFields
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|horizontalScroll
operator|=
literal|0
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|horizontalScroll
operator|>
name|displayFields
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|)
block|{
name|horizontalScroll
operator|=
name|displayFields
operator|.
name|size
argument_list|()
operator|-
literal|1
expr_stmt|;
block|}
name|List
argument_list|<
name|Header
argument_list|>
name|ret
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|length
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|horizontalScroll
init|;
name|i
operator|<
name|displayFields
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
name|displayFields
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|int
name|fieldLength
init|=
name|fieldLengthMap
operator|.
name|get
argument_list|(
name|field
argument_list|)
decl_stmt|;
name|length
operator|+=
name|fieldLength
operator|+
literal|1
expr_stmt|;
if|if
condition|(
name|length
operator|>
name|terminalLength
condition|)
block|{
break|break;
block|}
name|ret
operator|.
name|add
argument_list|(
operator|new
name|Header
argument_list|(
name|field
argument_list|,
name|fieldLength
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
specifier|private
name|List
argument_list|<
name|Record
argument_list|>
name|getDisplayedRecords
parameter_list|()
block|{
name|List
argument_list|<
name|Record
argument_list|>
name|ret
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|paging
operator|.
name|getPageStartPosition
argument_list|()
init|;
name|i
operator|<
name|paging
operator|.
name|getPageEndPosition
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ret
operator|.
name|add
argument_list|(
name|topScreenModel
operator|.
name|getRecords
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
specifier|private
name|Record
name|getSelectedRecord
parameter_list|()
block|{
if|if
condition|(
name|topScreenModel
operator|.
name|getRecords
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|topScreenModel
operator|.
name|getRecords
argument_list|()
operator|.
name|get
argument_list|(
name|paging
operator|.
name|getCurrentPosition
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|void
name|arrowUp
parameter_list|()
block|{
name|paging
operator|.
name|arrowUp
argument_list|()
expr_stmt|;
name|refresh
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|arrowDown
parameter_list|()
block|{
name|paging
operator|.
name|arrowDown
argument_list|()
expr_stmt|;
name|refresh
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|pageUp
parameter_list|()
block|{
name|paging
operator|.
name|pageUp
argument_list|()
expr_stmt|;
name|refresh
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|pageDown
parameter_list|()
block|{
name|paging
operator|.
name|pageDown
argument_list|()
expr_stmt|;
name|refresh
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|arrowLeft
parameter_list|()
block|{
if|if
condition|(
name|horizontalScroll
operator|>
literal|0
condition|)
block|{
name|horizontalScroll
operator|-=
literal|1
expr_stmt|;
block|}
name|refresh
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|arrowRight
parameter_list|()
block|{
if|if
condition|(
name|horizontalScroll
operator|<
name|getHeaderSize
argument_list|()
operator|-
literal|1
condition|)
block|{
name|horizontalScroll
operator|+=
literal|1
expr_stmt|;
block|}
name|refresh
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|home
parameter_list|()
block|{
if|if
condition|(
name|horizontalScroll
operator|>
literal|0
condition|)
block|{
name|horizontalScroll
operator|=
literal|0
expr_stmt|;
block|}
name|refresh
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|end
parameter_list|()
block|{
name|int
name|headerSize
init|=
name|getHeaderSize
argument_list|()
decl_stmt|;
name|horizontalScroll
operator|=
name|headerSize
operator|==
literal|0
condition|?
literal|0
else|:
name|headerSize
operator|-
literal|1
expr_stmt|;
name|refresh
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|getHeaderSize
parameter_list|()
block|{
return|return
operator|(
name|int
operator|)
name|topScreenModel
operator|.
name|getFields
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|fieldDisplayMap
operator|::
name|get
argument_list|)
operator|.
name|count
argument_list|()
return|;
block|}
specifier|public
name|void
name|switchSortOrder
parameter_list|()
block|{
name|topScreenModel
operator|.
name|switchSortOrder
argument_list|()
expr_stmt|;
name|refresh
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ScreenView
name|transitionToHelpScreen
parameter_list|(
name|Screen
name|screen
parameter_list|,
name|Terminal
name|terminal
parameter_list|)
block|{
return|return
operator|new
name|HelpScreenView
argument_list|(
name|screen
argument_list|,
name|terminal
argument_list|,
name|refreshDelay
operator|.
name|get
argument_list|()
argument_list|,
name|topScreenView
argument_list|)
return|;
block|}
specifier|public
name|ScreenView
name|transitionToModeScreen
parameter_list|(
name|Screen
name|screen
parameter_list|,
name|Terminal
name|terminal
parameter_list|)
block|{
return|return
operator|new
name|ModeScreenView
argument_list|(
name|screen
argument_list|,
name|terminal
argument_list|,
name|topScreenModel
operator|.
name|getCurrentMode
argument_list|()
argument_list|,
name|this
operator|::
name|switchMode
argument_list|,
name|topScreenView
argument_list|)
return|;
block|}
specifier|public
name|ScreenView
name|transitionToFieldScreen
parameter_list|(
name|Screen
name|screen
parameter_list|,
name|Terminal
name|terminal
parameter_list|)
block|{
return|return
operator|new
name|FieldScreenView
argument_list|(
name|screen
argument_list|,
name|terminal
argument_list|,
name|topScreenModel
operator|.
name|getCurrentSortField
argument_list|()
argument_list|,
name|topScreenModel
operator|.
name|getFields
argument_list|()
argument_list|,
name|fieldDisplayMap
argument_list|,
parameter_list|(
name|sortKey
parameter_list|,
name|fields
parameter_list|,
name|fieldDisplayMap
parameter_list|)
lambda|->
block|{
name|topScreenModel
operator|.
name|setSortFieldAndFields
argument_list|(
name|sortKey
argument_list|,
name|fields
argument_list|)
expr_stmt|;
name|this
operator|.
name|fieldDisplayMap
operator|.
name|clear
argument_list|()
expr_stmt|;
name|this
operator|.
name|fieldDisplayMap
operator|.
name|putAll
argument_list|(
name|fieldDisplayMap
argument_list|)
expr_stmt|;
block|}
argument_list|,
name|topScreenView
argument_list|)
return|;
block|}
specifier|private
name|void
name|switchMode
parameter_list|(
name|Mode
name|nextMode
parameter_list|)
block|{
name|topScreenModel
operator|.
name|switchMode
argument_list|(
name|nextMode
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|reset
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|drillDown
parameter_list|()
block|{
name|Record
name|selectedRecord
init|=
name|getSelectedRecord
argument_list|()
decl_stmt|;
if|if
condition|(
name|selectedRecord
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|topScreenModel
operator|.
name|drillDown
argument_list|(
name|selectedRecord
argument_list|)
condition|)
block|{
name|reset
argument_list|()
expr_stmt|;
name|refresh
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|reset
parameter_list|()
block|{
name|initFieldDisplayMapAndFieldLengthMap
argument_list|()
expr_stmt|;
name|adjustFieldLength
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|paging
operator|.
name|init
argument_list|()
expr_stmt|;
name|horizontalScroll
operator|=
literal|0
expr_stmt|;
name|topScreenView
operator|.
name|clearTerminal
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|initFieldDisplayMapAndFieldLengthMap
parameter_list|()
block|{
name|fieldDisplayMap
operator|.
name|clear
argument_list|()
expr_stmt|;
name|fieldLengthMap
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|FieldInfo
name|fieldInfo
range|:
name|topScreenModel
operator|.
name|getFieldInfos
argument_list|()
control|)
block|{
name|fieldDisplayMap
operator|.
name|put
argument_list|(
name|fieldInfo
operator|.
name|getField
argument_list|()
argument_list|,
name|fieldInfo
operator|.
name|isDisplayByDefault
argument_list|()
argument_list|)
expr_stmt|;
name|fieldLengthMap
operator|.
name|put
argument_list|(
name|fieldInfo
operator|.
name|getField
argument_list|()
argument_list|,
name|fieldInfo
operator|.
name|getDefaultLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|ScreenView
name|goToMessageMode
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
parameter_list|)
block|{
return|return
operator|new
name|MessageModeScreenView
argument_list|(
name|screen
argument_list|,
name|terminal
argument_list|,
name|row
argument_list|,
name|message
argument_list|,
name|topScreenView
argument_list|)
return|;
block|}
specifier|public
name|ScreenView
name|goToInputModeForRefreshDelay
parameter_list|(
name|Screen
name|screen
parameter_list|,
name|Terminal
name|terminal
parameter_list|,
name|int
name|row
parameter_list|)
block|{
return|return
operator|new
name|InputModeScreenView
argument_list|(
name|screen
argument_list|,
name|terminal
argument_list|,
name|row
argument_list|,
literal|"Change refresh delay from "
operator|+
operator|(
name|double
operator|)
name|refreshDelay
operator|.
name|get
argument_list|()
operator|/
literal|1000
operator|+
literal|" to"
argument_list|,
literal|null
argument_list|,
parameter_list|(
name|inputString
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|inputString
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|topScreenView
return|;
block|}
name|double
name|delay
decl_stmt|;
try|try
block|{
name|delay
operator|=
name|Double
operator|.
name|valueOf
argument_list|(
name|inputString
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
return|return
name|goToMessageMode
argument_list|(
name|screen
argument_list|,
name|terminal
argument_list|,
name|row
argument_list|,
literal|"Unacceptable floating point"
argument_list|)
return|;
block|}
name|refreshDelay
operator|.
name|set
argument_list|(
call|(
name|long
call|)
argument_list|(
name|delay
operator|*
literal|1000
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|topScreenView
return|;
block|}
argument_list|)
return|;
block|}
specifier|public
name|ScreenView
name|goToInputModeForFilter
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
name|boolean
name|ignoreCase
parameter_list|)
block|{
return|return
operator|new
name|InputModeScreenView
argument_list|(
name|screen
argument_list|,
name|terminal
argument_list|,
name|row
argument_list|,
literal|"add filter #"
operator|+
operator|(
name|topScreenModel
operator|.
name|getFilters
argument_list|()
operator|.
name|size
argument_list|()
operator|+
literal|1
operator|)
operator|+
literal|" ("
operator|+
operator|(
name|ignoreCase
condition|?
literal|"ignoring case"
else|:
literal|"case sensitive"
operator|)
operator|+
literal|") as: [!]FLD?VAL"
argument_list|,
name|topScreenModel
operator|.
name|getFilterHistories
argument_list|()
argument_list|,
parameter_list|(
name|inputString
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|inputString
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|topScreenView
return|;
block|}
if|if
condition|(
operator|!
name|topScreenModel
operator|.
name|addFilter
argument_list|(
name|inputString
argument_list|,
name|ignoreCase
argument_list|)
condition|)
block|{
return|return
name|goToMessageMode
argument_list|(
name|screen
argument_list|,
name|terminal
argument_list|,
name|row
argument_list|,
literal|"Unacceptable filter expression"
argument_list|)
return|;
block|}
name|paging
operator|.
name|init
argument_list|()
expr_stmt|;
return|return
name|topScreenView
return|;
block|}
argument_list|)
return|;
block|}
specifier|public
name|void
name|clearFilters
parameter_list|()
block|{
name|topScreenModel
operator|.
name|clearFilters
argument_list|()
expr_stmt|;
name|paging
operator|.
name|init
argument_list|()
expr_stmt|;
name|refresh
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ScreenView
name|goToFilterDisplayMode
parameter_list|(
name|Screen
name|screen
parameter_list|,
name|Terminal
name|terminal
parameter_list|,
name|int
name|row
parameter_list|)
block|{
return|return
operator|new
name|FilterDisplayModeScreenView
argument_list|(
name|screen
argument_list|,
name|terminal
argument_list|,
name|row
argument_list|,
name|topScreenModel
operator|.
name|getFilters
argument_list|()
argument_list|,
name|topScreenView
argument_list|)
return|;
block|}
block|}
end_class

end_unit

