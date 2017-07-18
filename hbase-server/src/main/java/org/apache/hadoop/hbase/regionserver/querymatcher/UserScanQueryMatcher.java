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
name|regionserver
operator|.
name|querymatcher
package|;
end_package

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
name|Cell
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
name|CellUtil
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
name|classification
operator|.
name|InterfaceAudience
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
name|io
operator|.
name|TimeRange
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
name|RegionCoprocessorHost
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
name|ScanInfo
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
name|Pair
import|;
end_import

begin_comment
comment|/**  * Query matcher for user scan.  *<p>  * We do not consider mvcc here because  * {@link org.apache.hadoop.hbase.regionserver.StoreFileScanner} and  * {@link org.apache.hadoop.hbase.regionserver.SegmentScanner} will only return a cell whose mvcc is  * less than or equal to given read point. For  * {@link org.apache.hadoop.hbase.client.IsolationLevel#READ_UNCOMMITTED}, we just set the read  * point to {@link Long#MAX_VALUE}, i.e. still do not need to consider it.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|UserScanQueryMatcher
extends|extends
name|ScanQueryMatcher
block|{
specifier|protected
specifier|final
name|boolean
name|hasNullColumn
decl_stmt|;
specifier|protected
specifier|final
name|Filter
name|filter
decl_stmt|;
specifier|protected
specifier|final
name|byte
index|[]
name|stopRow
decl_stmt|;
specifier|protected
specifier|final
name|TimeRange
name|tr
decl_stmt|;
specifier|private
specifier|static
name|Cell
name|createStartKey
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|ScanInfo
name|scanInfo
parameter_list|)
block|{
if|if
condition|(
name|scan
operator|.
name|includeStartRow
argument_list|()
condition|)
block|{
return|return
name|createStartKeyFromRow
argument_list|(
name|scan
operator|.
name|getStartRow
argument_list|()
argument_list|,
name|scanInfo
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|CellUtil
operator|.
name|createLastOnRow
argument_list|(
name|scan
operator|.
name|getStartRow
argument_list|()
argument_list|)
return|;
block|}
block|}
specifier|protected
name|UserScanQueryMatcher
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|ScanInfo
name|scanInfo
parameter_list|,
name|ColumnTracker
name|columns
parameter_list|,
name|boolean
name|hasNullColumn
parameter_list|,
name|long
name|oldestUnexpiredTS
parameter_list|,
name|long
name|now
parameter_list|)
block|{
name|super
argument_list|(
name|createStartKey
argument_list|(
name|scan
argument_list|,
name|scanInfo
argument_list|)
argument_list|,
name|scanInfo
argument_list|,
name|columns
argument_list|,
name|oldestUnexpiredTS
argument_list|,
name|now
argument_list|)
expr_stmt|;
name|this
operator|.
name|hasNullColumn
operator|=
name|hasNullColumn
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
name|stopRow
operator|=
name|scan
operator|.
name|getStopRow
argument_list|()
expr_stmt|;
name|TimeRange
name|timeRange
init|=
name|scan
operator|.
name|getColumnFamilyTimeRange
argument_list|()
operator|.
name|get
argument_list|(
name|scanInfo
operator|.
name|getFamily
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|timeRange
operator|==
literal|null
condition|)
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
block|}
else|else
block|{
name|this
operator|.
name|tr
operator|=
name|timeRange
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNullColumnInQuery
parameter_list|()
block|{
return|return
name|hasNullColumn
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isUserScan
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|Filter
name|getFilter
parameter_list|()
block|{
return|return
name|filter
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|getNextKeyHint
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|filter
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
return|return
name|filter
operator|.
name|getNextCellHint
argument_list|(
name|cell
argument_list|)
return|;
block|}
block|}
specifier|protected
specifier|final
name|MatchCode
name|matchColumn
parameter_list|(
name|Cell
name|cell
parameter_list|,
name|long
name|timestamp
parameter_list|,
name|byte
name|typeByte
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|tsCmp
init|=
name|tr
operator|.
name|compare
argument_list|(
name|timestamp
argument_list|)
decl_stmt|;
if|if
condition|(
name|tsCmp
operator|>
literal|0
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
name|tsCmp
operator|<
literal|0
condition|)
block|{
return|return
name|columns
operator|.
name|getNextRowOrNextColumn
argument_list|(
name|cell
argument_list|)
return|;
block|}
comment|// STEP 1: Check if the column is part of the requested columns
name|MatchCode
name|colChecker
init|=
name|columns
operator|.
name|checkColumn
argument_list|(
name|cell
argument_list|,
name|typeByte
argument_list|)
decl_stmt|;
if|if
condition|(
name|colChecker
operator|!=
name|MatchCode
operator|.
name|INCLUDE
condition|)
block|{
return|return
name|colChecker
return|;
block|}
name|ReturnCode
name|filterResponse
init|=
name|ReturnCode
operator|.
name|SKIP
decl_stmt|;
comment|// STEP 2: Yes, the column is part of the requested columns. Check if filter is present
if|if
condition|(
name|filter
operator|!=
literal|null
condition|)
block|{
comment|// STEP 3: Filter the key value and return if it filters out
name|filterResponse
operator|=
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|cell
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|filterResponse
condition|)
block|{
case|case
name|SKIP
case|:
return|return
name|MatchCode
operator|.
name|SKIP
return|;
case|case
name|NEXT_COL
case|:
return|return
name|columns
operator|.
name|getNextRowOrNextColumn
argument_list|(
name|cell
argument_list|)
return|;
case|case
name|NEXT_ROW
case|:
return|return
name|MatchCode
operator|.
name|SEEK_NEXT_ROW
return|;
case|case
name|SEEK_NEXT_USING_HINT
case|:
return|return
name|MatchCode
operator|.
name|SEEK_NEXT_USING_HINT
return|;
default|default:
comment|// It means it is either include or include and seek next
break|break;
block|}
block|}
comment|/*      * STEP 4: Reaching this step means the column is part of the requested columns and either      * the filter is null or the filter has returned INCLUDE or INCLUDE_AND_NEXT_COL response.      * Now check the number of versions needed. This method call returns SKIP, INCLUDE,      * INCLUDE_AND_SEEK_NEXT_ROW, INCLUDE_AND_SEEK_NEXT_COL.      *      * FilterResponse            ColumnChecker               Desired behavior      * INCLUDE                   SKIP                        row has already been included, SKIP.      * INCLUDE                   INCLUDE                     INCLUDE      * INCLUDE                   INCLUDE_AND_SEEK_NEXT_COL   INCLUDE_AND_SEEK_NEXT_COL      * INCLUDE                   INCLUDE_AND_SEEK_NEXT_ROW   INCLUDE_AND_SEEK_NEXT_ROW      * INCLUDE_AND_SEEK_NEXT_COL SKIP                        row has already been included, SKIP.      * INCLUDE_AND_SEEK_NEXT_COL INCLUDE                     INCLUDE_AND_SEEK_NEXT_COL      * INCLUDE_AND_SEEK_NEXT_COL INCLUDE_AND_SEEK_NEXT_COL   INCLUDE_AND_SEEK_NEXT_COL      * INCLUDE_AND_SEEK_NEXT_COL INCLUDE_AND_SEEK_NEXT_ROW   INCLUDE_AND_SEEK_NEXT_ROW      *      * In all the above scenarios, we return the column checker return value except for      * FilterResponse (INCLUDE_AND_SEEK_NEXT_COL) and ColumnChecker(INCLUDE)      */
name|colChecker
operator|=
name|columns
operator|.
name|checkVersions
argument_list|(
name|cell
argument_list|,
name|timestamp
argument_list|,
name|typeByte
argument_list|,
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
name|filterResponse
operator|==
name|ReturnCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_ROW
condition|)
block|{
if|if
condition|(
name|colChecker
operator|!=
name|MatchCode
operator|.
name|SKIP
condition|)
block|{
return|return
name|MatchCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_ROW
return|;
block|}
return|return
name|MatchCode
operator|.
name|SEEK_NEXT_ROW
return|;
block|}
return|return
operator|(
name|filterResponse
operator|==
name|ReturnCode
operator|.
name|INCLUDE_AND_NEXT_COL
operator|&&
name|colChecker
operator|==
name|MatchCode
operator|.
name|INCLUDE
operator|)
condition|?
name|MatchCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_COL
else|:
name|colChecker
return|;
block|}
specifier|protected
specifier|abstract
name|boolean
name|isGet
parameter_list|()
function_decl|;
specifier|protected
specifier|abstract
name|boolean
name|moreRowsMayExistsAfter
parameter_list|(
name|int
name|cmpToStopRow
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
name|boolean
name|moreRowsMayExistAfter
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
comment|// If a 'get' Scan -- we are doing a Get (every Get is a single-row Scan in implementation) --
comment|// then we are looking at one row only, the one specified in the Get coordinate..so we know
comment|// for sure that there are no more rows on this Scan
if|if
condition|(
name|isGet
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// If no stopRow, return that there may be more rows. The tests that follow depend on a
comment|// non-empty, non-default stopRow so this little test below short-circuits out doing the
comment|// following compares.
if|if
condition|(
name|this
operator|.
name|stopRow
operator|==
literal|null
operator|||
name|this
operator|.
name|stopRow
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
name|moreRowsMayExistsAfter
argument_list|(
name|rowComparator
operator|.
name|compareRows
argument_list|(
name|cell
argument_list|,
name|stopRow
argument_list|,
literal|0
argument_list|,
name|stopRow
operator|.
name|length
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|UserScanQueryMatcher
name|create
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|ScanInfo
name|scanInfo
parameter_list|,
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
parameter_list|,
name|long
name|oldestUnexpiredTS
parameter_list|,
name|long
name|now
parameter_list|,
name|RegionCoprocessorHost
name|regionCoprocessorHost
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|hasNullColumn
init|=
operator|!
operator|(
name|columns
operator|!=
literal|null
operator|&&
name|columns
operator|.
name|size
argument_list|()
operator|!=
literal|0
operator|&&
name|columns
operator|.
name|first
argument_list|()
operator|.
name|length
operator|!=
literal|0
operator|)
decl_stmt|;
name|Pair
argument_list|<
name|DeleteTracker
argument_list|,
name|ColumnTracker
argument_list|>
name|trackers
init|=
name|getTrackers
argument_list|(
name|regionCoprocessorHost
argument_list|,
name|columns
argument_list|,
name|scanInfo
argument_list|,
name|oldestUnexpiredTS
argument_list|,
name|scan
argument_list|)
decl_stmt|;
name|DeleteTracker
name|deleteTracker
init|=
name|trackers
operator|.
name|getFirst
argument_list|()
decl_stmt|;
name|ColumnTracker
name|columnTracker
init|=
name|trackers
operator|.
name|getSecond
argument_list|()
decl_stmt|;
if|if
condition|(
name|scan
operator|.
name|isRaw
argument_list|()
condition|)
block|{
return|return
name|RawScanQueryMatcher
operator|.
name|create
argument_list|(
name|scan
argument_list|,
name|scanInfo
argument_list|,
name|columnTracker
argument_list|,
name|hasNullColumn
argument_list|,
name|oldestUnexpiredTS
argument_list|,
name|now
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|NormalUserScanQueryMatcher
operator|.
name|create
argument_list|(
name|scan
argument_list|,
name|scanInfo
argument_list|,
name|columnTracker
argument_list|,
name|deleteTracker
argument_list|,
name|hasNullColumn
argument_list|,
name|oldestUnexpiredTS
argument_list|,
name|now
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

