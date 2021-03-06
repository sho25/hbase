begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hbase
operator|.
name|Cell
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

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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

begin_comment
comment|/**  * FilterListWithAND represents an ordered list of filters which will be evaluated with an AND  * operator.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|FilterListWithAND
extends|extends
name|FilterListBase
block|{
specifier|private
name|List
argument_list|<
name|Filter
argument_list|>
name|seekHintFilters
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|FilterListWithAND
parameter_list|(
name|List
argument_list|<
name|Filter
argument_list|>
name|filters
parameter_list|)
block|{
name|super
argument_list|(
name|filters
argument_list|)
expr_stmt|;
comment|// For FilterList with AND, when call FL's transformCell(), we should transform cell for all
comment|// sub-filters (because all sub-filters return INCLUDE*). So here, fill this array with true. we
comment|// keep this in FilterListWithAND for abstracting the transformCell() in FilterListBase.
name|subFiltersIncludedCell
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|Collections
operator|.
name|nCopies
argument_list|(
name|filters
operator|.
name|size
argument_list|()
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addFilterLists
parameter_list|(
name|List
argument_list|<
name|Filter
argument_list|>
name|filters
parameter_list|)
block|{
if|if
condition|(
name|checkAndGetReversed
argument_list|(
name|filters
argument_list|,
name|isReversed
argument_list|()
argument_list|)
operator|!=
name|isReversed
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Filters in the list must have the same reversed flag"
argument_list|)
throw|;
block|}
name|this
operator|.
name|filters
operator|.
name|addAll
argument_list|(
name|filters
argument_list|)
expr_stmt|;
name|this
operator|.
name|subFiltersIncludedCell
operator|.
name|addAll
argument_list|(
name|Collections
operator|.
name|nCopies
argument_list|(
name|filters
operator|.
name|size
argument_list|()
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|formatLogFilters
parameter_list|(
name|List
argument_list|<
name|Filter
argument_list|>
name|logFilters
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"FilterList AND (%d/%d): %s"
argument_list|,
name|logFilters
operator|.
name|size
argument_list|()
argument_list|,
name|this
operator|.
name|size
argument_list|()
argument_list|,
name|logFilters
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * FilterList with MUST_PASS_ALL choose the maximal forward step among sub-filters in filter list.    * Let's call it: The Maximal Step Rule. So if filter-A in filter list return INCLUDE and filter-B    * in filter list return INCLUDE_AND_NEXT_COL, then the filter list should return    * INCLUDE_AND_NEXT_COL. For SEEK_NEXT_USING_HINT, it's more special, and in method    * filterCellWithMustPassAll(), if any sub-filter return SEEK_NEXT_USING_HINT, then our filter    * list will return SEEK_NEXT_USING_HINT. so we don't care about the SEEK_NEXT_USING_HINT here.    *<br/>    *<br/>    * The jump step will be:    *    *<pre>    * INCLUDE&lt; SKIP&lt; INCLUDE_AND_NEXT_COL&lt; NEXT_COL&lt; INCLUDE_AND_SEEK_NEXT_ROW&lt; NEXT_ROW&lt; SEEK_NEXT_USING_HINT    *</pre>    *    * Here, we have the following map to describe The Maximal Step Rule. if current return code (for    * previous sub-filters in filter list) is<strong>ReturnCode</strong>, and current filter returns    *<strong>localRC</strong>, then we should return map[ReturnCode][localRC] for the merged result,    * according to The Maximal Step Rule.<br/>    *    *<pre>    * LocalCode\ReturnCode       INCLUDE                    INCLUDE_AND_NEXT_COL      INCLUDE_AND_SEEK_NEXT_ROW  SKIP                  NEXT_COL              NEXT_ROW              SEEK_NEXT_USING_HINT    * INCLUDE                    INCLUDE                    INCLUDE_AND_NEXT_COL      INCLUDE_AND_SEEK_NEXT_ROW  SKIP                  NEXT_COL              NEXT_ROW              SEEK_NEXT_USING_HINT    * INCLUDE_AND_NEXT_COL       INCLUDE_AND_NEXT_COL       INCLUDE_AND_NEXT_COL      INCLUDE_AND_SEEK_NEXT_ROW  NEXT_COL              NEXT_COL              NEXT_ROW              SEEK_NEXT_USING_HINT    * INCLUDE_AND_SEEK_NEXT_ROW  INCLUDE_AND_SEEK_NEXT_ROW  INCLUDE_AND_SEEK_NEXT_ROW INCLUDE_AND_SEEK_NEXT_ROW  NEXT_ROW              NEXT_ROW              NEXT_ROW              SEEK_NEXT_USING_HINT    * SKIP                       SKIP                       NEXT_COL                  NEXT_ROW                   SKIP                  NEXT_COL              NEXT_ROW              SEEK_NEXT_USING_HINT    * NEXT_COL                   NEXT_COL                   NEXT_COL                  NEXT_ROW                   NEXT_COL              NEXT_COL              NEXT_ROW              SEEK_NEXT_USING_HINT    * NEXT_ROW                   NEXT_ROW                   NEXT_ROW                  NEXT_ROW                   NEXT_ROW              NEXT_ROW              NEXT_ROW              SEEK_NEXT_USING_HINT    * SEEK_NEXT_USING_HINT       SEEK_NEXT_USING_HINT       SEEK_NEXT_USING_HINT      SEEK_NEXT_USING_HINT       SEEK_NEXT_USING_HINT  SEEK_NEXT_USING_HINT  SEEK_NEXT_USING_HINT  SEEK_NEXT_USING_HINT    *</pre>    *    * @param rc Return code which is calculated by previous sub-filter(s) in filter list.    * @param localRC Return code of the current sub-filter in filter list.    * @return Return code which is merged by the return code of previous sub-filter(s) and the return    *         code of current sub-filter.    */
specifier|private
name|ReturnCode
name|mergeReturnCode
parameter_list|(
name|ReturnCode
name|rc
parameter_list|,
name|ReturnCode
name|localRC
parameter_list|)
block|{
if|if
condition|(
name|rc
operator|==
name|ReturnCode
operator|.
name|SEEK_NEXT_USING_HINT
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|SEEK_NEXT_USING_HINT
return|;
block|}
switch|switch
condition|(
name|localRC
condition|)
block|{
case|case
name|SEEK_NEXT_USING_HINT
case|:
return|return
name|ReturnCode
operator|.
name|SEEK_NEXT_USING_HINT
return|;
case|case
name|INCLUDE
case|:
return|return
name|rc
return|;
case|case
name|INCLUDE_AND_NEXT_COL
case|:
if|if
condition|(
name|isInReturnCodes
argument_list|(
name|rc
argument_list|,
name|ReturnCode
operator|.
name|INCLUDE
argument_list|,
name|ReturnCode
operator|.
name|INCLUDE_AND_NEXT_COL
argument_list|)
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|INCLUDE_AND_NEXT_COL
return|;
block|}
if|if
condition|(
name|isInReturnCodes
argument_list|(
name|rc
argument_list|,
name|ReturnCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_ROW
argument_list|)
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_ROW
return|;
block|}
if|if
condition|(
name|isInReturnCodes
argument_list|(
name|rc
argument_list|,
name|ReturnCode
operator|.
name|SKIP
argument_list|,
name|ReturnCode
operator|.
name|NEXT_COL
argument_list|)
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|NEXT_COL
return|;
block|}
if|if
condition|(
name|isInReturnCodes
argument_list|(
name|rc
argument_list|,
name|ReturnCode
operator|.
name|NEXT_ROW
argument_list|)
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|NEXT_ROW
return|;
block|}
break|break;
case|case
name|INCLUDE_AND_SEEK_NEXT_ROW
case|:
if|if
condition|(
name|isInReturnCodes
argument_list|(
name|rc
argument_list|,
name|ReturnCode
operator|.
name|INCLUDE
argument_list|,
name|ReturnCode
operator|.
name|INCLUDE_AND_NEXT_COL
argument_list|,
name|ReturnCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_ROW
argument_list|)
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_ROW
return|;
block|}
if|if
condition|(
name|isInReturnCodes
argument_list|(
name|rc
argument_list|,
name|ReturnCode
operator|.
name|SKIP
argument_list|,
name|ReturnCode
operator|.
name|NEXT_COL
argument_list|,
name|ReturnCode
operator|.
name|NEXT_ROW
argument_list|)
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|NEXT_ROW
return|;
block|}
break|break;
case|case
name|SKIP
case|:
if|if
condition|(
name|isInReturnCodes
argument_list|(
name|rc
argument_list|,
name|ReturnCode
operator|.
name|INCLUDE
argument_list|,
name|ReturnCode
operator|.
name|SKIP
argument_list|)
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|SKIP
return|;
block|}
if|if
condition|(
name|isInReturnCodes
argument_list|(
name|rc
argument_list|,
name|ReturnCode
operator|.
name|INCLUDE_AND_NEXT_COL
argument_list|,
name|ReturnCode
operator|.
name|NEXT_COL
argument_list|)
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|NEXT_COL
return|;
block|}
if|if
condition|(
name|isInReturnCodes
argument_list|(
name|rc
argument_list|,
name|ReturnCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_ROW
argument_list|,
name|ReturnCode
operator|.
name|NEXT_ROW
argument_list|)
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|NEXT_ROW
return|;
block|}
break|break;
case|case
name|NEXT_COL
case|:
if|if
condition|(
name|isInReturnCodes
argument_list|(
name|rc
argument_list|,
name|ReturnCode
operator|.
name|INCLUDE
argument_list|,
name|ReturnCode
operator|.
name|INCLUDE_AND_NEXT_COL
argument_list|,
name|ReturnCode
operator|.
name|SKIP
argument_list|,
name|ReturnCode
operator|.
name|NEXT_COL
argument_list|)
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|NEXT_COL
return|;
block|}
if|if
condition|(
name|isInReturnCodes
argument_list|(
name|rc
argument_list|,
name|ReturnCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_ROW
argument_list|,
name|ReturnCode
operator|.
name|NEXT_ROW
argument_list|)
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|NEXT_ROW
return|;
block|}
break|break;
case|case
name|NEXT_ROW
case|:
return|return
name|ReturnCode
operator|.
name|NEXT_ROW
return|;
block|}
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Received code is not valid. rc: "
operator|+
name|rc
operator|+
literal|", localRC: "
operator|+
name|localRC
argument_list|)
throw|;
block|}
specifier|private
name|boolean
name|isIncludeRelatedReturnCode
parameter_list|(
name|ReturnCode
name|rc
parameter_list|)
block|{
return|return
name|isInReturnCodes
argument_list|(
name|rc
argument_list|,
name|ReturnCode
operator|.
name|INCLUDE
argument_list|,
name|ReturnCode
operator|.
name|INCLUDE_AND_NEXT_COL
argument_list|,
name|ReturnCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_ROW
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterCell
parameter_list|(
name|Cell
name|c
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
name|ReturnCode
name|rc
init|=
name|ReturnCode
operator|.
name|INCLUDE
decl_stmt|;
name|this
operator|.
name|seekHintFilters
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|n
init|=
name|filters
operator|.
name|size
argument_list|()
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|Filter
name|filter
init|=
name|filters
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|filter
operator|.
name|filterAllRemaining
argument_list|()
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|NEXT_ROW
return|;
block|}
name|ReturnCode
name|localRC
decl_stmt|;
name|localRC
operator|=
name|filter
operator|.
name|filterCell
argument_list|(
name|c
argument_list|)
expr_stmt|;
if|if
condition|(
name|localRC
operator|==
name|ReturnCode
operator|.
name|SEEK_NEXT_USING_HINT
condition|)
block|{
name|seekHintFilters
operator|.
name|add
argument_list|(
name|filter
argument_list|)
expr_stmt|;
block|}
name|rc
operator|=
name|mergeReturnCode
argument_list|(
name|rc
argument_list|,
name|localRC
argument_list|)
expr_stmt|;
comment|// Only when rc is INCLUDE* case, we should pass the cell to the following sub-filters.
comment|// otherwise we may mess up the global state (such as offset, count..) in the following
comment|// sub-filters. (HBASE-20565)
if|if
condition|(
operator|!
name|isIncludeRelatedReturnCode
argument_list|(
name|rc
argument_list|)
condition|)
block|{
return|return
name|rc
return|;
block|}
block|}
if|if
condition|(
operator|!
name|seekHintFilters
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|SEEK_NEXT_USING_HINT
return|;
block|}
return|return
name|rc
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|n
init|=
name|filters
operator|.
name|size
argument_list|()
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|filters
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
name|seekHintFilters
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
name|Cell
name|firstRowCell
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|super
operator|.
name|filterRowKey
argument_list|(
name|firstRowCell
argument_list|)
return|;
block|}
name|boolean
name|retVal
init|=
literal|false
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|n
init|=
name|filters
operator|.
name|size
argument_list|()
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|Filter
name|filter
init|=
name|filters
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|filter
operator|.
name|filterAllRemaining
argument_list|()
operator|||
name|filter
operator|.
name|filterRowKey
argument_list|(
name|firstRowCell
argument_list|)
condition|)
block|{
comment|// Can't just return true here, because there are some filters (such as PrefixFilter) which
comment|// will catch the row changed event by filterRowKey(). If we return early here, those
comment|// filters will have no chance to update their row state.
name|retVal
operator|=
literal|true
expr_stmt|;
block|}
block|}
return|return
name|retVal
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterAllRemaining
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|super
operator|.
name|filterAllRemaining
argument_list|()
return|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|n
init|=
name|filters
operator|.
name|size
argument_list|()
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|filters
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|filterAllRemaining
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterRow
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|super
operator|.
name|filterRow
argument_list|()
return|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|n
init|=
name|filters
operator|.
name|size
argument_list|()
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|Filter
name|filter
init|=
name|filters
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|filter
operator|.
name|filterRow
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|getNextCellHint
parameter_list|(
name|Cell
name|currentCell
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|super
operator|.
name|getNextCellHint
argument_list|(
name|currentCell
argument_list|)
return|;
block|}
name|Cell
name|maxHint
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Filter
name|filter
range|:
name|seekHintFilters
control|)
block|{
if|if
condition|(
name|filter
operator|.
name|filterAllRemaining
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|Cell
name|curKeyHint
init|=
name|filter
operator|.
name|getNextCellHint
argument_list|(
name|currentCell
argument_list|)
decl_stmt|;
if|if
condition|(
name|maxHint
operator|==
literal|null
condition|)
block|{
name|maxHint
operator|=
name|curKeyHint
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|this
operator|.
name|compareCell
argument_list|(
name|maxHint
argument_list|,
name|curKeyHint
argument_list|)
operator|<
literal|0
condition|)
block|{
name|maxHint
operator|=
name|curKeyHint
expr_stmt|;
block|}
block|}
return|return
name|maxHint
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|FilterListWithAND
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
name|FilterListWithAND
name|f
init|=
operator|(
name|FilterListWithAND
operator|)
name|obj
decl_stmt|;
return|return
name|this
operator|.
name|filters
operator|.
name|equals
argument_list|(
name|f
operator|.
name|getFilters
argument_list|()
argument_list|)
operator|&&
name|this
operator|.
name|seekHintFilters
operator|.
name|equals
argument_list|(
name|f
operator|.
name|seekHintFilters
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|this
operator|.
name|seekHintFilters
argument_list|,
name|this
operator|.
name|filters
argument_list|)
return|;
block|}
block|}
end_class

end_unit

