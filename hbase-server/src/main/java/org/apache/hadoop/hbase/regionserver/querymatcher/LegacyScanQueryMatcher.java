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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|Arrays
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
name|CellComparator
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
name|HConstants
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
name|KeepDeletedCells
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
name|regionserver
operator|.
name|ScanType
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
name|querymatcher
operator|.
name|DeleteTracker
operator|.
name|DeleteResult
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
name|EnvironmentEdgeManager
import|;
end_import

begin_comment
comment|/**  * The old query matcher implementation. Used to keep compatibility for coprocessor that could  * overwrite the StoreScanner before compaction. Should be removed once we find a better way to do  * filtering during compaction.  */
end_comment

begin_class
annotation|@
name|Deprecated
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|LegacyScanQueryMatcher
extends|extends
name|ScanQueryMatcher
block|{
specifier|private
specifier|final
name|TimeRange
name|tr
decl_stmt|;
specifier|private
specifier|final
name|Filter
name|filter
decl_stmt|;
comment|/** Keeps track of deletes */
specifier|private
specifier|final
name|DeleteTracker
name|deletes
decl_stmt|;
comment|/**    * The following three booleans define how we deal with deletes. There are three different    * aspects:    *<ol>    *<li>Whether to keep delete markers. This is used in compactions. Minor compactions always keep    * delete markers.</li>    *<li>Whether to keep deleted rows. This is also used in compactions, if the store is set to keep    * deleted rows. This implies keeping the delete markers as well.</li> In this case deleted rows    * are subject to the normal max version and TTL/min version rules just like "normal" rows.    *<li>Whether a scan can do time travel queries even before deleted marker to reach deleted    * rows.</li>    *</ol>    */
comment|/** whether to retain delete markers */
specifier|private
name|boolean
name|retainDeletesInOutput
decl_stmt|;
comment|/** whether to return deleted rows */
specifier|private
specifier|final
name|KeepDeletedCells
name|keepDeletedCells
decl_stmt|;
comment|// By default, when hbase.hstore.time.to.purge.deletes is 0ms, a delete
comment|// marker is always removed during a major compaction. If set to non-zero
comment|// value then major compaction will try to keep a delete marker around for
comment|// the given number of milliseconds. We want to keep the delete markers
comment|// around a bit longer because old puts might appear out-of-order. For
comment|// example, during log replication between two clusters.
comment|//
comment|// If the delete marker has lived longer than its column-family's TTL then
comment|// the delete marker will be removed even if time.to.purge.deletes has not
comment|// passed. This is because all the Puts that this delete marker can influence
comment|// would have also expired. (Removing of delete markers on col family TTL will
comment|// not happen if min-versions is set to non-zero)
comment|//
comment|// But, if time.to.purge.deletes has not expired then a delete
comment|// marker will not be removed just because there are no Puts that it is
comment|// currently influencing. This is because Puts, that this delete can
comment|// influence. may appear out of order.
specifier|private
specifier|final
name|long
name|timeToPurgeDeletes
decl_stmt|;
comment|/**    * This variable shows whether there is an null column in the query. There always exists a null    * column in the wildcard column query. There maybe exists a null column in the explicit column    * query based on the first column.    */
specifier|private
specifier|final
name|boolean
name|hasNullColumn
decl_stmt|;
comment|/** readPoint over which the KVs are unconditionally included */
specifier|private
specifier|final
name|long
name|maxReadPointToTrackVersions
decl_stmt|;
comment|/**    * Oldest put in any of the involved store files Used to decide whether it is ok to delete family    * delete marker of this store keeps deleted KVs.    */
specifier|protected
specifier|final
name|long
name|earliestPutTs
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|stopRow
decl_stmt|;
specifier|private
name|byte
index|[]
name|dropDeletesFromRow
init|=
literal|null
decl_stmt|,
name|dropDeletesToRow
init|=
literal|null
decl_stmt|;
specifier|private
name|LegacyScanQueryMatcher
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
name|DeleteTracker
name|deletes
parameter_list|,
name|ScanType
name|scanType
parameter_list|,
name|long
name|readPointToUse
parameter_list|,
name|long
name|earliestPutTs
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
name|createStartKeyFromRow
argument_list|(
name|scan
operator|.
name|getStartRow
argument_list|()
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
name|this
operator|.
name|hasNullColumn
operator|=
name|hasNullColumn
expr_stmt|;
name|this
operator|.
name|deletes
operator|=
name|deletes
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
name|maxReadPointToTrackVersions
operator|=
name|readPointToUse
expr_stmt|;
name|this
operator|.
name|timeToPurgeDeletes
operator|=
name|scanInfo
operator|.
name|getTimeToPurgeDeletes
argument_list|()
expr_stmt|;
name|this
operator|.
name|earliestPutTs
operator|=
name|earliestPutTs
expr_stmt|;
comment|/* how to deal with deletes */
name|this
operator|.
name|keepDeletedCells
operator|=
name|scanInfo
operator|.
name|getKeepDeletedCells
argument_list|()
expr_stmt|;
name|this
operator|.
name|retainDeletesInOutput
operator|=
name|scanType
operator|==
name|ScanType
operator|.
name|COMPACT_RETAIN_DELETES
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
block|}
specifier|private
name|LegacyScanQueryMatcher
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
name|DeleteTracker
name|deletes
parameter_list|,
name|ScanType
name|scanType
parameter_list|,
name|long
name|readPointToUse
parameter_list|,
name|long
name|earliestPutTs
parameter_list|,
name|long
name|oldestUnexpiredTS
parameter_list|,
name|long
name|now
parameter_list|,
name|byte
index|[]
name|dropDeletesFromRow
parameter_list|,
name|byte
index|[]
name|dropDeletesToRow
parameter_list|)
block|{
name|this
argument_list|(
name|scan
argument_list|,
name|scanInfo
argument_list|,
name|columns
argument_list|,
name|hasNullColumn
argument_list|,
name|deletes
argument_list|,
name|scanType
argument_list|,
name|readPointToUse
argument_list|,
name|earliestPutTs
argument_list|,
name|oldestUnexpiredTS
argument_list|,
name|now
argument_list|)
expr_stmt|;
name|this
operator|.
name|dropDeletesFromRow
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|dropDeletesFromRow
argument_list|)
expr_stmt|;
name|this
operator|.
name|dropDeletesToRow
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|dropDeletesToRow
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|MatchCode
name|match
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
name|MatchCode
name|returnCode
init|=
name|preCheck
argument_list|(
name|cell
argument_list|)
decl_stmt|;
if|if
condition|(
name|returnCode
operator|!=
literal|null
condition|)
block|{
return|return
name|returnCode
return|;
block|}
comment|/*      * The delete logic is pretty complicated now.      * This is corroborated by the following:      * 1. The store might be instructed to keep deleted rows around.      * 2. A scan can optionally see past a delete marker now.      * 3. If deleted rows are kept, we have to find out when we can      *    remove the delete markers.      * 4. Family delete markers are always first (regardless of their TS)      * 5. Delete markers should not be counted as version      * 6. Delete markers affect puts of the *same* TS      * 7. Delete marker need to be version counted together with puts      *    they affect      */
name|long
name|timestamp
init|=
name|cell
operator|.
name|getTimestamp
argument_list|()
decl_stmt|;
name|byte
name|typeByte
init|=
name|cell
operator|.
name|getTypeByte
argument_list|()
decl_stmt|;
name|long
name|mvccVersion
init|=
name|cell
operator|.
name|getSequenceId
argument_list|()
decl_stmt|;
if|if
condition|(
name|CellUtil
operator|.
name|isDelete
argument_list|(
name|typeByte
argument_list|)
condition|)
block|{
if|if
condition|(
name|keepDeletedCells
operator|==
name|KeepDeletedCells
operator|.
name|FALSE
operator|||
operator|(
name|keepDeletedCells
operator|==
name|KeepDeletedCells
operator|.
name|TTL
operator|&&
name|timestamp
operator|<
name|oldestUnexpiredTS
operator|)
condition|)
block|{
comment|// first ignore delete markers if the scanner can do so, and the
comment|// range does not include the marker
comment|//
comment|// during flushes and compactions also ignore delete markers newer
comment|// than the readpoint of any open scanner, this prevents deleted
comment|// rows that could still be seen by a scanner from being collected
name|boolean
name|includeDeleteMarker
init|=
name|tr
operator|.
name|withinOrAfterTimeRange
argument_list|(
name|timestamp
argument_list|)
decl_stmt|;
if|if
condition|(
name|includeDeleteMarker
operator|&&
name|mvccVersion
operator|<=
name|maxReadPointToTrackVersions
condition|)
block|{
name|this
operator|.
name|deletes
operator|.
name|add
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
comment|// Can't early out now, because DelFam come before any other keys
block|}
if|if
condition|(
name|timeToPurgeDeletes
operator|>
literal|0
operator|&&
operator|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|timestamp
operator|)
operator|<=
name|timeToPurgeDeletes
condition|)
block|{
return|return
name|MatchCode
operator|.
name|INCLUDE
return|;
block|}
elseif|else
if|if
condition|(
name|retainDeletesInOutput
operator|||
name|mvccVersion
operator|>
name|maxReadPointToTrackVersions
condition|)
block|{
comment|// always include or it is not time yet to check whether it is OK
comment|// to purge deltes or not
comment|// if this is not a user scan (compaction), we can filter this deletemarker right here
comment|// otherwise (i.e. a "raw" scan) we fall through to normal version and timerange checking
return|return
name|MatchCode
operator|.
name|INCLUDE
return|;
block|}
elseif|else
if|if
condition|(
name|keepDeletedCells
operator|==
name|KeepDeletedCells
operator|.
name|TRUE
operator|||
operator|(
name|keepDeletedCells
operator|==
name|KeepDeletedCells
operator|.
name|TTL
operator|&&
name|timestamp
operator|>=
name|oldestUnexpiredTS
operator|)
condition|)
block|{
if|if
condition|(
name|timestamp
operator|<
name|earliestPutTs
condition|)
block|{
comment|// keeping delete rows, but there are no puts older than
comment|// this delete in the store files.
return|return
name|columns
operator|.
name|getNextRowOrNextColumn
argument_list|(
name|cell
argument_list|)
return|;
block|}
comment|// else: fall through and do version counting on the
comment|// delete markers
block|}
else|else
block|{
return|return
name|MatchCode
operator|.
name|SKIP
return|;
block|}
comment|// note the following next else if...
comment|// delete marker are not subject to other delete markers
block|}
elseif|else
if|if
condition|(
operator|!
name|this
operator|.
name|deletes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|DeleteResult
name|deleteResult
init|=
name|deletes
operator|.
name|isDeleted
argument_list|(
name|cell
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|deleteResult
condition|)
block|{
case|case
name|FAMILY_DELETED
case|:
case|case
name|COLUMN_DELETED
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
name|VERSION_DELETED
case|:
case|case
name|FAMILY_VERSION_DELETED
case|:
return|return
name|MatchCode
operator|.
name|SKIP
return|;
case|case
name|NOT_DELETED
case|:
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"UNEXPECTED"
argument_list|)
throw|;
block|}
block|}
name|int
name|timestampComparison
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
name|timestampComparison
operator|>=
literal|1
condition|)
block|{
return|return
name|MatchCode
operator|.
name|SKIP
return|;
block|}
elseif|else
if|if
condition|(
name|timestampComparison
operator|<=
operator|-
literal|1
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
operator|==
name|MatchCode
operator|.
name|INCLUDE
condition|)
block|{
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
name|stickyNextRow
operator|=
literal|true
expr_stmt|;
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
comment|//It means it is either include or include and seek next
break|break;
block|}
block|}
comment|/*        * STEP 4: Reaching this step means the column is part of the requested columns and either        * the filter is null or the filter has returned INCLUDE or INCLUDE_AND_NEXT_COL response.        * Now check the number of versions needed. This method call returns SKIP, INCLUDE,        * INCLUDE_AND_SEEK_NEXT_ROW, INCLUDE_AND_SEEK_NEXT_COL.        *        * FilterResponse            ColumnChecker               Desired behavior        * INCLUDE                   SKIP                        row has already been included, SKIP.        * INCLUDE                   INCLUDE                     INCLUDE        * INCLUDE                   INCLUDE_AND_SEEK_NEXT_COL   INCLUDE_AND_SEEK_NEXT_COL        * INCLUDE                   INCLUDE_AND_SEEK_NEXT_ROW   INCLUDE_AND_SEEK_NEXT_ROW        * INCLUDE_AND_SEEK_NEXT_COL SKIP                        row has already been included, SKIP.        * INCLUDE_AND_SEEK_NEXT_COL INCLUDE                     INCLUDE_AND_SEEK_NEXT_COL        * INCLUDE_AND_SEEK_NEXT_COL INCLUDE_AND_SEEK_NEXT_COL   INCLUDE_AND_SEEK_NEXT_COL        * INCLUDE_AND_SEEK_NEXT_COL INCLUDE_AND_SEEK_NEXT_ROW   INCLUDE_AND_SEEK_NEXT_ROW        *        * In all the above scenarios, we return the column checker return value except for        * FilterResponse (INCLUDE_AND_SEEK_NEXT_COL) and ColumnChecker(INCLUDE)        */
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
name|mvccVersion
operator|>
name|maxReadPointToTrackVersions
argument_list|)
expr_stmt|;
comment|//Optimize with stickyNextRow
name|boolean
name|seekNextRowFromEssential
init|=
name|filterResponse
operator|==
name|ReturnCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_ROW
operator|&&
name|filter
operator|.
name|isFamilyEssential
argument_list|(
name|cell
operator|.
name|getFamilyArray
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|colChecker
operator|==
name|MatchCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_ROW
operator|||
name|seekNextRowFromEssential
condition|)
block|{
name|stickyNextRow
operator|=
literal|true
expr_stmt|;
block|}
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
name|stickyNextRow
operator|=
operator|(
name|colChecker
operator|==
name|MatchCode
operator|.
name|SEEK_NEXT_ROW
operator|)
condition|?
literal|true
else|:
name|stickyNextRow
expr_stmt|;
return|return
name|colChecker
return|;
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
comment|/**    * Handle partial-drop-deletes. As we match keys in order, when we have a range from which we can    * drop deletes, we can set retainDeletesInOutput to false for the duration of this range only,    * and maintain consistency.    */
specifier|private
name|void
name|checkPartialDropDeleteRange
parameter_list|(
name|Cell
name|curCell
parameter_list|)
block|{
comment|// If partial-drop-deletes are used, initially, dropDeletesFromRow and dropDeletesToRow
comment|// are both set, and the matcher is set to retain deletes. We assume ordered keys. When
comment|// dropDeletesFromRow is leq current kv, we start dropping deletes and reset
comment|// dropDeletesFromRow; thus the 2nd "if" starts to apply.
if|if
condition|(
operator|(
name|dropDeletesFromRow
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|Arrays
operator|.
name|equals
argument_list|(
name|dropDeletesFromRow
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
operator|||
operator|(
name|CellComparator
operator|.
name|COMPARATOR
operator|.
name|compareRows
argument_list|(
name|curCell
argument_list|,
name|dropDeletesFromRow
argument_list|,
literal|0
argument_list|,
name|dropDeletesFromRow
operator|.
name|length
argument_list|)
operator|>=
literal|0
operator|)
operator|)
condition|)
block|{
name|retainDeletesInOutput
operator|=
literal|false
expr_stmt|;
name|dropDeletesFromRow
operator|=
literal|null
expr_stmt|;
block|}
comment|// If dropDeletesFromRow is null and dropDeletesToRow is set, we are inside the partial-
comment|// drop-deletes range. When dropDeletesToRow is leq current kv, we stop dropping deletes,
comment|// and reset dropDeletesToRow so that we don't do any more compares.
if|if
condition|(
operator|(
name|dropDeletesFromRow
operator|==
literal|null
operator|)
operator|&&
operator|(
name|dropDeletesToRow
operator|!=
literal|null
operator|)
operator|&&
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|dropDeletesToRow
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
operator|&&
operator|(
name|CellComparator
operator|.
name|COMPARATOR
operator|.
name|compareRows
argument_list|(
name|curCell
argument_list|,
name|dropDeletesToRow
argument_list|,
literal|0
argument_list|,
name|dropDeletesToRow
operator|.
name|length
argument_list|)
operator|>=
literal|0
operator|)
condition|)
block|{
name|retainDeletesInOutput
operator|=
literal|true
expr_stmt|;
name|dropDeletesToRow
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|reset
parameter_list|()
block|{
name|checkPartialDropDeleteRange
argument_list|(
name|currentRow
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isUserScan
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
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
operator|<
literal|0
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
specifier|public
specifier|static
name|LegacyScanQueryMatcher
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
name|ScanType
name|scanType
parameter_list|,
name|long
name|readPointToUse
parameter_list|,
name|long
name|earliestPutTs
parameter_list|,
name|long
name|oldestUnexpiredTS
parameter_list|,
name|long
name|now
parameter_list|,
name|byte
index|[]
name|dropDeletesFromRow
parameter_list|,
name|byte
index|[]
name|dropDeletesToRow
parameter_list|,
name|RegionCoprocessorHost
name|regionCoprocessorHost
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|maxVersions
init|=
name|Math
operator|.
name|min
argument_list|(
name|scan
operator|.
name|getMaxVersions
argument_list|()
argument_list|,
name|scanInfo
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|hasNullColumn
decl_stmt|;
name|ColumnTracker
name|columnTracker
decl_stmt|;
if|if
condition|(
name|columns
operator|==
literal|null
operator|||
name|columns
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// there is always a null column in the wildcard column query.
name|hasNullColumn
operator|=
literal|true
expr_stmt|;
comment|// use a specialized scan for wildcard column tracker.
name|columnTracker
operator|=
operator|new
name|ScanWildcardColumnTracker
argument_list|(
name|scanInfo
operator|.
name|getMinVersions
argument_list|()
argument_list|,
name|maxVersions
argument_list|,
name|oldestUnexpiredTS
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// We can share the ExplicitColumnTracker, diff is we reset
comment|// between rows, not between storefiles.
comment|// whether there is null column in the explicit column query
name|hasNullColumn
operator|=
name|columns
operator|.
name|first
argument_list|()
operator|.
name|length
operator|==
literal|0
expr_stmt|;
name|columnTracker
operator|=
operator|new
name|ExplicitColumnTracker
argument_list|(
name|columns
argument_list|,
name|scanInfo
operator|.
name|getMinVersions
argument_list|()
argument_list|,
name|maxVersions
argument_list|,
name|oldestUnexpiredTS
argument_list|)
expr_stmt|;
block|}
name|DeleteTracker
name|deletes
init|=
name|instantiateDeleteTracker
argument_list|(
name|regionCoprocessorHost
argument_list|)
decl_stmt|;
if|if
condition|(
name|dropDeletesFromRow
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|LegacyScanQueryMatcher
argument_list|(
name|scan
argument_list|,
name|scanInfo
argument_list|,
name|columnTracker
argument_list|,
name|hasNullColumn
argument_list|,
name|deletes
argument_list|,
name|scanType
argument_list|,
name|readPointToUse
argument_list|,
name|earliestPutTs
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
operator|new
name|LegacyScanQueryMatcher
argument_list|(
name|scan
argument_list|,
name|scanInfo
argument_list|,
name|columnTracker
argument_list|,
name|hasNullColumn
argument_list|,
name|deletes
argument_list|,
name|scanType
argument_list|,
name|readPointToUse
argument_list|,
name|earliestPutTs
argument_list|,
name|oldestUnexpiredTS
argument_list|,
name|now
argument_list|,
name|dropDeletesFromRow
argument_list|,
name|dropDeletesToRow
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

