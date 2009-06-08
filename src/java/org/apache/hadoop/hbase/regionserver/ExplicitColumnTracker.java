begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableSet
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
name|regionserver
operator|.
name|QueryMatcher
operator|.
name|MatchCode
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * This class is used for the tracking and enforcement of columns and numbers   * of versions during the course of a Get or Scan operation, when explicit  * column qualifiers have been asked for in the query.  *  * With a little magic (see {@link ScanQueryMatcher}), we can use this matcher  * for both scans and gets.  The main difference is 'next' and 'done' collapse  * for the scan case (since we see all columns in order), and we only reset  * between rows.  *   *<p>  * This class is utilized by {@link QueryMatcher} through two methods:  *<ul><li>{@link #checkColumn} is called when a Put satisfies all other  * conditions of the query.  This method returns a {@link MatchCode} to define  * what action should be taken.  *<li>{@link #update} is called at the end of every StoreFile or Memcache.  *<p>  * This class is NOT thread-safe as queries are never multi-threaded   */
end_comment

begin_class
specifier|public
class|class
name|ExplicitColumnTracker
implements|implements
name|ColumnTracker
block|{
specifier|private
name|int
name|maxVersions
decl_stmt|;
specifier|private
name|List
argument_list|<
name|ColumnCount
argument_list|>
name|columns
decl_stmt|;
specifier|private
name|int
name|index
decl_stmt|;
specifier|private
name|ColumnCount
name|column
decl_stmt|;
specifier|private
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|origColumns
decl_stmt|;
comment|/**    * Default constructor.    * @param columns columns specified user in query    * @param maxVersions maximum versions to return per column    */
specifier|public
name|ExplicitColumnTracker
parameter_list|(
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
parameter_list|,
name|int
name|maxVersions
parameter_list|)
block|{
name|this
operator|.
name|maxVersions
operator|=
name|maxVersions
expr_stmt|;
name|this
operator|.
name|origColumns
operator|=
name|columns
expr_stmt|;
name|reset
argument_list|()
expr_stmt|;
block|}
comment|/**    * Done when there are no more columns to match against.    */
specifier|public
name|boolean
name|done
parameter_list|()
block|{
return|return
name|this
operator|.
name|columns
operator|.
name|size
argument_list|()
operator|==
literal|0
return|;
block|}
specifier|public
name|ColumnCount
name|getColumnHint
parameter_list|()
block|{
return|return
name|this
operator|.
name|column
return|;
block|}
comment|/**    * Checks against the parameters of the query and the columns which have    * already been processed by this query.    * @param bytes KeyValue buffer    * @param offset offset to the start of the qualifier    * @param length length of the qualifier    * @return MatchCode telling QueryMatcher what action to take    */
specifier|public
name|MatchCode
name|checkColumn
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
comment|// No more columns left, we are done with this query
if|if
condition|(
name|this
operator|.
name|columns
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
name|MatchCode
operator|.
name|DONE
return|;
comment|// done_row
block|}
comment|// No more columns to match against, done with storefile
if|if
condition|(
name|this
operator|.
name|column
operator|==
literal|null
condition|)
block|{
return|return
name|MatchCode
operator|.
name|NEXT
return|;
comment|// done_row
block|}
comment|// Compare specific column to current column
name|int
name|ret
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|column
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|column
operator|.
name|getOffset
argument_list|()
argument_list|,
name|column
operator|.
name|getLength
argument_list|()
argument_list|,
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
decl_stmt|;
comment|// Matches, decrement versions left and include
if|if
condition|(
name|ret
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|column
operator|.
name|decrement
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// Done with versions for this column
name|this
operator|.
name|columns
operator|.
name|remove
argument_list|(
name|this
operator|.
name|index
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|columns
operator|.
name|size
argument_list|()
operator|==
name|this
operator|.
name|index
condition|)
block|{
comment|// Will not hit any more columns in this storefile
name|this
operator|.
name|column
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|column
operator|=
name|this
operator|.
name|columns
operator|.
name|get
argument_list|(
name|this
operator|.
name|index
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|MatchCode
operator|.
name|INCLUDE
return|;
block|}
comment|// Specified column is bigger than current column
comment|// Move down current column and check again
if|if
condition|(
name|ret
operator|<=
operator|-
literal|1
condition|)
block|{
if|if
condition|(
operator|++
name|this
operator|.
name|index
operator|==
name|this
operator|.
name|columns
operator|.
name|size
argument_list|()
condition|)
block|{
comment|// No more to match, do not include, done with storefile
return|return
name|MatchCode
operator|.
name|NEXT
return|;
comment|// done_row
block|}
name|this
operator|.
name|column
operator|=
name|this
operator|.
name|columns
operator|.
name|get
argument_list|(
name|this
operator|.
name|index
argument_list|)
expr_stmt|;
return|return
name|checkColumn
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
comment|// Specified column is smaller than current column
comment|// Skip
return|return
name|MatchCode
operator|.
name|SKIP
return|;
comment|// skip to next column, with hint?
block|}
comment|/**    * Called at the end of every StoreFile or Memcache.    */
specifier|public
name|void
name|update
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|columns
operator|.
name|size
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|this
operator|.
name|index
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|column
operator|=
name|this
operator|.
name|columns
operator|.
name|get
argument_list|(
name|this
operator|.
name|index
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|index
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|column
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|// Called between every row.
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|buildColumnList
argument_list|(
name|this
operator|.
name|origColumns
argument_list|)
expr_stmt|;
name|this
operator|.
name|index
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|column
operator|=
name|this
operator|.
name|columns
operator|.
name|get
argument_list|(
name|this
operator|.
name|index
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|buildColumnList
parameter_list|(
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
parameter_list|)
block|{
name|this
operator|.
name|columns
operator|=
operator|new
name|ArrayList
argument_list|<
name|ColumnCount
argument_list|>
argument_list|(
name|columns
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|column
range|:
name|columns
control|)
block|{
name|this
operator|.
name|columns
operator|.
name|add
argument_list|(
operator|new
name|ColumnCount
argument_list|(
name|column
argument_list|,
name|maxVersions
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

