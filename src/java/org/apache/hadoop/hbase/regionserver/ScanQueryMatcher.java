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
name|KeyValue
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
name|Scan
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
name|filter
operator|.
name|Filter
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
name|filter
operator|.
name|RowFilterInterface
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
name|filter
operator|.
name|Filter
operator|.
name|ReturnCode
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
comment|/**  * A query matcher that is specifically designed for the scan case.  */
end_comment

begin_class
specifier|public
class|class
name|ScanQueryMatcher
extends|extends
name|QueryMatcher
block|{
specifier|private
name|Filter
name|filter
decl_stmt|;
comment|// have to support old style filter for now.
specifier|private
name|RowFilterInterface
name|oldFilter
decl_stmt|;
comment|// Optimization so we can skip lots of compares when we decide to skip
comment|// to the next row.
specifier|private
name|boolean
name|stickyNextRow
decl_stmt|;
specifier|private
name|KeyValue
name|stopKey
init|=
literal|null
decl_stmt|;
comment|/**    * Constructs a QueryMatcher for a Scan.    * @param scan    * @param family    * @param columns    * @param ttl    * @param rowComparator    */
specifier|public
name|ScanQueryMatcher
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
parameter_list|,
name|long
name|ttl
parameter_list|,
name|KeyValue
operator|.
name|KeyComparator
name|rowComparator
parameter_list|,
name|int
name|maxVersions
parameter_list|)
block|{
name|this
operator|.
name|tr
operator|=
name|scan
operator|.
name|getTimeRange
argument_list|()
expr_stmt|;
name|this
operator|.
name|oldestStamp
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|ttl
expr_stmt|;
name|this
operator|.
name|rowComparator
operator|=
name|rowComparator
expr_stmt|;
comment|// shouldn't this be ScanDeleteTracker?
name|this
operator|.
name|deletes
operator|=
operator|new
name|ScanDeleteTracker
argument_list|(
name|rowComparator
argument_list|)
expr_stmt|;
name|this
operator|.
name|startKey
operator|=
name|KeyValue
operator|.
name|createFirstOnRow
argument_list|(
name|scan
operator|.
name|getStartRow
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|stopKey
operator|=
name|KeyValue
operator|.
name|createFirstOnRow
argument_list|(
name|scan
operator|.
name|getStopRow
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|filter
operator|=
name|scan
operator|.
name|getFilter
argument_list|()
expr_stmt|;
name|this
operator|.
name|oldFilter
operator|=
name|scan
operator|.
name|getOldFilter
argument_list|()
expr_stmt|;
comment|// Single branch to deal with two types of reads (columns vs all in family)
if|if
condition|(
name|columns
operator|==
literal|null
operator|||
name|columns
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// use a specialized scan for wildcard column tracker.
name|this
operator|.
name|columns
operator|=
operator|new
name|ScanWildcardColumnTracker
argument_list|(
name|maxVersions
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// We can share the ExplicitColumnTracker, diff is we reset
comment|// between rows, not between storefiles.
name|this
operator|.
name|columns
operator|=
operator|new
name|ExplicitColumnTracker
argument_list|(
name|columns
argument_list|,
name|maxVersions
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Determines if the caller should do one of several things:    * - seek/skip to the next row (MatchCode.SEEK_NEXT_ROW)    * - seek/skip to the next column (MatchCode.SEEK_NEXT_COL)    * - include the current KeyValue (MatchCode.INCLUDE)    * - ignore the current KeyValue (MatchCode.SKIP)    * - got to the next row (MatchCode.DONE)    *     * @param kv KeyValue to check    * @return The match code instance.    */
specifier|public
name|MatchCode
name|match
parameter_list|(
name|KeyValue
name|kv
parameter_list|)
block|{
if|if
condition|(
name|filter
operator|!=
literal|null
operator|&&
name|filter
operator|.
name|filterAllRemaining
argument_list|()
condition|)
block|{
return|return
name|MatchCode
operator|.
name|DONE_SCAN
return|;
block|}
elseif|else
if|if
condition|(
name|oldFilter
operator|!=
literal|null
operator|&&
name|oldFilter
operator|.
name|filterAllRemaining
argument_list|()
condition|)
block|{
comment|// the old filter runs only if the other filter didnt work.
return|return
name|MatchCode
operator|.
name|DONE_SCAN
return|;
block|}
name|byte
index|[]
name|bytes
init|=
name|kv
operator|.
name|getBuffer
argument_list|()
decl_stmt|;
name|int
name|offset
init|=
name|kv
operator|.
name|getOffset
argument_list|()
decl_stmt|;
name|int
name|initialOffset
init|=
name|offset
decl_stmt|;
name|int
name|keyLength
init|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
decl_stmt|;
name|offset
operator|+=
name|KeyValue
operator|.
name|ROW_OFFSET
expr_stmt|;
name|short
name|rowLength
init|=
name|Bytes
operator|.
name|toShort
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|Bytes
operator|.
name|SIZEOF_SHORT
argument_list|)
decl_stmt|;
name|offset
operator|+=
name|Bytes
operator|.
name|SIZEOF_SHORT
expr_stmt|;
name|int
name|ret
init|=
name|this
operator|.
name|rowComparator
operator|.
name|compareRows
argument_list|(
name|row
argument_list|,
literal|0
argument_list|,
name|row
operator|.
name|length
argument_list|,
name|bytes
argument_list|,
name|offset
argument_list|,
name|rowLength
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
operator|<=
operator|-
literal|1
condition|)
block|{
return|return
name|MatchCode
operator|.
name|DONE
return|;
block|}
elseif|else
if|if
condition|(
name|ret
operator|>=
literal|1
condition|)
block|{
comment|// could optimize this, if necessary?
comment|// Could also be called SEEK_TO_CURRENT_ROW, but this
comment|// should be rare/never happens.
return|return
name|MatchCode
operator|.
name|SKIP
return|;
block|}
comment|// optimize case.
if|if
condition|(
name|this
operator|.
name|stickyNextRow
condition|)
return|return
name|MatchCode
operator|.
name|SEEK_NEXT_ROW
return|;
comment|// Give the row filter a chance to do it's job.
if|if
condition|(
name|filter
operator|!=
literal|null
operator|&&
name|filter
operator|.
name|filterRowKey
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|rowLength
argument_list|)
condition|)
block|{
name|stickyNextRow
operator|=
literal|true
expr_stmt|;
comment|// optimize to keep from calling the filter too much.
return|return
name|MatchCode
operator|.
name|SEEK_NEXT_ROW
return|;
block|}
elseif|else
if|if
condition|(
name|oldFilter
operator|!=
literal|null
operator|&&
name|oldFilter
operator|.
name|filterRowKey
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|rowLength
argument_list|)
condition|)
block|{
name|stickyNextRow
operator|=
literal|true
expr_stmt|;
return|return
name|MatchCode
operator|.
name|SEEK_NEXT_ROW
return|;
block|}
if|if
condition|(
name|this
operator|.
name|columns
operator|.
name|done
argument_list|()
condition|)
block|{
name|stickyNextRow
operator|=
literal|true
expr_stmt|;
return|return
name|MatchCode
operator|.
name|SEEK_NEXT_ROW
return|;
block|}
comment|//Passing rowLength
name|offset
operator|+=
name|rowLength
expr_stmt|;
comment|//Skipping family
name|byte
name|familyLength
init|=
name|bytes
index|[
name|offset
index|]
decl_stmt|;
name|offset
operator|+=
name|familyLength
operator|+
literal|1
expr_stmt|;
name|int
name|qualLength
init|=
name|keyLength
operator|+
name|KeyValue
operator|.
name|ROW_OFFSET
operator|-
operator|(
name|offset
operator|-
name|initialOffset
operator|)
operator|-
name|KeyValue
operator|.
name|TIMESTAMP_TYPE_SIZE
decl_stmt|;
name|long
name|timestamp
init|=
name|kv
operator|.
name|getTimestamp
argument_list|()
decl_stmt|;
if|if
condition|(
name|isExpired
argument_list|(
name|timestamp
argument_list|)
condition|)
block|{
comment|// done, the rest wil also be expired as well.
name|stickyNextRow
operator|=
literal|true
expr_stmt|;
return|return
name|MatchCode
operator|.
name|SEEK_NEXT_ROW
return|;
block|}
name|byte
name|type
init|=
name|kv
operator|.
name|getType
argument_list|()
decl_stmt|;
if|if
condition|(
name|isDelete
argument_list|(
name|type
argument_list|)
condition|)
block|{
if|if
condition|(
name|tr
operator|.
name|withinOrAfterTimeRange
argument_list|(
name|timestamp
argument_list|)
condition|)
block|{
name|this
operator|.
name|deletes
operator|.
name|add
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|qualLength
argument_list|,
name|timestamp
argument_list|,
name|type
argument_list|)
expr_stmt|;
comment|// Can't early out now, because DelFam come before any other keys
block|}
comment|// May be able to optimize the SKIP here, if we matched
comment|// due to a DelFam, we can skip to next row
comment|// due to a DelCol, we can skip to next col
comment|// But it requires more info out of isDelete().
comment|// needful -> million column challenge.
return|return
name|MatchCode
operator|.
name|SKIP
return|;
block|}
if|if
condition|(
operator|!
name|tr
operator|.
name|withinTimeRange
argument_list|(
name|timestamp
argument_list|)
condition|)
block|{
return|return
name|MatchCode
operator|.
name|SKIP
return|;
block|}
if|if
condition|(
name|deletes
operator|.
name|isDeleted
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|qualLength
argument_list|,
name|timestamp
argument_list|)
condition|)
block|{
return|return
name|MatchCode
operator|.
name|SKIP
return|;
block|}
name|MatchCode
name|colChecker
init|=
name|columns
operator|.
name|checkColumn
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|qualLength
argument_list|)
decl_stmt|;
comment|// if SKIP -> SEEK_NEXT_COL
comment|// if (NEXT,DONE) -> SEEK_NEXT_ROW
comment|// if (INCLUDE) -> INCLUDE
if|if
condition|(
name|colChecker
operator|==
name|MatchCode
operator|.
name|SKIP
condition|)
block|{
return|return
name|MatchCode
operator|.
name|SEEK_NEXT_COL
return|;
block|}
elseif|else
if|if
condition|(
name|colChecker
operator|==
name|MatchCode
operator|.
name|NEXT
operator|||
name|colChecker
operator|==
name|MatchCode
operator|.
name|DONE
condition|)
block|{
name|stickyNextRow
operator|=
literal|true
expr_stmt|;
return|return
name|MatchCode
operator|.
name|SEEK_NEXT_ROW
return|;
block|}
comment|// else INCLUDE
comment|// if (colChecker == MatchCode.INCLUDE)
comment|// give the filter a chance to run.
if|if
condition|(
name|filter
operator|==
literal|null
condition|)
return|return
name|MatchCode
operator|.
name|INCLUDE
return|;
name|ReturnCode
name|filterResponse
init|=
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|kv
argument_list|)
decl_stmt|;
if|if
condition|(
name|filterResponse
operator|==
name|ReturnCode
operator|.
name|INCLUDE
condition|)
return|return
name|MatchCode
operator|.
name|INCLUDE
return|;
if|if
condition|(
name|filterResponse
operator|==
name|ReturnCode
operator|.
name|SKIP
condition|)
return|return
name|MatchCode
operator|.
name|SKIP
return|;
comment|// else if (filterResponse == ReturnCode.NEXT_ROW)
name|stickyNextRow
operator|=
literal|true
expr_stmt|;
return|return
name|MatchCode
operator|.
name|SEEK_NEXT_ROW
return|;
block|}
comment|/**    * If the row was otherwise going to be included, call this to last-minute    * check.    *     * @return<code>true</code> if the row should be filtered.    */
specifier|public
name|boolean
name|filterEntireRow
parameter_list|()
block|{
return|return
name|filter
operator|==
literal|null
condition|?
literal|false
else|:
name|filter
operator|.
name|filterRow
argument_list|()
return|;
block|}
comment|/**    * Set current row    * @param row    */
annotation|@
name|Override
specifier|public
name|void
name|setRow
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
name|reset
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|super
operator|.
name|reset
argument_list|()
expr_stmt|;
name|stickyNextRow
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|filter
operator|!=
literal|null
condition|)
name|filter
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

