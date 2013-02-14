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
name|regionserver
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
name|Bytes
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
comment|/**  * A query matcher that is specifically designed for the scan case.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ScanQueryMatcher
block|{
comment|// Optimization so we can skip lots of compares when we decide to skip
comment|// to the next row.
specifier|private
name|boolean
name|stickyNextRow
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|stopRow
decl_stmt|;
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
comment|/*    * The following three booleans define how we deal with deletes.    * There are three different aspects:    * 1. Whether to keep delete markers. This is used in compactions.    *    Minor compactions always keep delete markers.    * 2. Whether to keep deleted rows. This is also used in compactions,    *    if the store is set to keep deleted rows. This implies keeping    *    the delete markers as well.    *    In this case deleted rows are subject to the normal max version    *    and TTL/min version rules just like "normal" rows.    * 3. Whether a scan can do time travel queries even before deleted    *    marker to reach deleted rows.    */
comment|/** whether to retain delete markers */
specifier|private
specifier|final
name|boolean
name|retainDeletesInOutput
decl_stmt|;
comment|/** whether to return deleted rows */
specifier|private
specifier|final
name|boolean
name|keepDeletedCells
decl_stmt|;
comment|/** whether time range queries can see rows "behind" a delete */
specifier|private
specifier|final
name|boolean
name|seePastDeleteMarkers
decl_stmt|;
comment|/** Keeps track of columns and versions */
specifier|private
specifier|final
name|ColumnTracker
name|columns
decl_stmt|;
comment|/** Key to seek to in memstore and StoreFiles */
specifier|private
specifier|final
name|KeyValue
name|startKey
decl_stmt|;
comment|/** Row comparator for the region this query is for */
specifier|private
specifier|final
name|KeyValue
operator|.
name|KeyComparator
name|rowComparator
decl_stmt|;
comment|/* row is not private for tests */
comment|/** Row the query is on */
name|byte
index|[]
name|row
decl_stmt|;
name|int
name|rowOffset
decl_stmt|;
name|short
name|rowLength
decl_stmt|;
comment|/**    * Oldest put in any of the involved store files    * Used to decide whether it is ok to delete    * family delete marker of this store keeps    * deleted KVs.    */
specifier|private
specifier|final
name|long
name|earliestPutTs
decl_stmt|;
comment|/** readPoint over which the KVs are unconditionally included */
specifier|protected
name|long
name|maxReadPointToTrackVersions
decl_stmt|;
comment|/**    * This variable shows whether there is an null column in the query. There    * always exists a null column in the wildcard column query.    * There maybe exists a null column in the explicit column query based on the    * first column.    * */
specifier|private
name|boolean
name|hasNullColumn
init|=
literal|true
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
comment|// influence.  may appear out of order.
specifier|private
specifier|final
name|long
name|timeToPurgeDeletes
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isUserScan
decl_stmt|;
comment|/**    * Construct a QueryMatcher for a scan    * @param scan    * @param scanInfo The store's immutable scan info    * @param columns    * @param scanType Type of the scan    * @param earliestPutTs Earliest put seen in any of the store files.    * @param oldestUnexpiredTS the oldest timestamp we are interested in,    *  based on TTL    */
specifier|public
name|ScanQueryMatcher
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
name|rowComparator
operator|=
name|scanInfo
operator|.
name|getComparator
argument_list|()
operator|.
name|getRawComparator
argument_list|()
expr_stmt|;
name|this
operator|.
name|deletes
operator|=
operator|new
name|ScanDeleteTracker
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
name|this
operator|.
name|startKey
operator|=
name|KeyValue
operator|.
name|createFirstDeleteFamilyOnRow
argument_list|(
name|scan
operator|.
name|getStartRow
argument_list|()
argument_list|,
name|scanInfo
operator|.
name|getFamily
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
name|earliestPutTs
operator|=
name|earliestPutTs
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
comment|/* how to deal with deletes */
name|this
operator|.
name|isUserScan
operator|=
name|scanType
operator|==
name|ScanType
operator|.
name|USER_SCAN
expr_stmt|;
comment|// keep deleted cells: if compaction or raw scan
name|this
operator|.
name|keepDeletedCells
operator|=
operator|(
name|scanInfo
operator|.
name|getKeepDeletedCells
argument_list|()
operator|&&
operator|!
name|isUserScan
operator|)
operator|||
name|scan
operator|.
name|isRaw
argument_list|()
expr_stmt|;
comment|// retain deletes: if minor compaction or raw scan
name|this
operator|.
name|retainDeletesInOutput
operator|=
name|scanType
operator|==
name|ScanType
operator|.
name|MINOR_COMPACT
operator|||
name|scan
operator|.
name|isRaw
argument_list|()
expr_stmt|;
comment|// seePastDeleteMarker: user initiated scans
name|this
operator|.
name|seePastDeleteMarkers
operator|=
name|scanInfo
operator|.
name|getKeepDeletedCells
argument_list|()
operator|&&
name|isUserScan
expr_stmt|;
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
comment|// there is always a null column in the wildcard column query.
name|hasNullColumn
operator|=
literal|true
expr_stmt|;
comment|// use a specialized scan for wildcard column tracker.
name|this
operator|.
name|columns
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
comment|// whether there is null column in the explicit column query
name|hasNullColumn
operator|=
operator|(
name|columns
operator|.
name|first
argument_list|()
operator|.
name|length
operator|==
literal|0
operator|)
expr_stmt|;
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
block|}
comment|/*    * Constructor for tests    */
name|ScanQueryMatcher
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
name|ScanType
operator|.
name|USER_SCAN
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
comment|/* max Readpoint to track versions */
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|oldestUnexpiredTS
argument_list|)
expr_stmt|;
block|}
comment|/**    *    * @return  whether there is an null column in the query    */
specifier|public
name|boolean
name|hasNullColumnInQuery
parameter_list|()
block|{
return|return
name|hasNullColumn
return|;
block|}
comment|/**    * Determines if the caller should do one of several things:    * - seek/skip to the next row (MatchCode.SEEK_NEXT_ROW)    * - seek/skip to the next column (MatchCode.SEEK_NEXT_COL)    * - include the current KeyValue (MatchCode.INCLUDE)    * - ignore the current KeyValue (MatchCode.SKIP)    * - got to the next row (MatchCode.DONE)    *    * @param kv KeyValue to check    * @return The match code instance.    * @throws IOException in case there is an internal consistency problem    *      caused by a data corruption.    */
specifier|public
name|MatchCode
name|match
parameter_list|(
name|KeyValue
name|kv
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
name|this
operator|.
name|rowOffset
argument_list|,
name|this
operator|.
name|rowLength
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
name|SEEK_NEXT_ROW
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
comment|// check for early out based on timestamp alone
if|if
condition|(
name|columns
operator|.
name|isDone
argument_list|(
name|timestamp
argument_list|)
condition|)
block|{
return|return
name|columns
operator|.
name|getNextRowOrNextColumn
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|qualLength
argument_list|)
return|;
block|}
comment|/*      * The delete logic is pretty complicated now.      * This is corroborated by the following:      * 1. The store might be instructed to keep deleted rows around.      * 2. A scan can optionally see past a delete marker now.      * 3. If deleted rows are kept, we have to find out when we can      *    remove the delete markers.      * 4. Family delete markers are always first (regardless of their TS)      * 5. Delete markers should not be counted as version      * 6. Delete markers affect puts of the *same* TS      * 7. Delete marker need to be version counted together with puts      *    they affect      */
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
name|kv
operator|.
name|isDelete
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|keepDeletedCells
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
name|seePastDeleteMarkers
condition|?
name|tr
operator|.
name|withinTimeRange
argument_list|(
name|timestamp
argument_list|)
else|:
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
name|kv
operator|.
name|getMemstoreTS
argument_list|()
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
block|}
comment|// Can't early out now, because DelFam come before any other keys
block|}
if|if
condition|(
name|retainDeletesInOutput
operator|||
operator|(
operator|!
name|isUserScan
operator|&&
operator|(
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|timestamp
operator|)
operator|<=
name|timeToPurgeDeletes
operator|)
operator|||
name|kv
operator|.
name|getMemstoreTS
argument_list|()
operator|>
name|maxReadPointToTrackVersions
condition|)
block|{
comment|// always include or it is not time yet to check whether it is OK
comment|// to purge deltes or not
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
name|bytes
argument_list|,
name|offset
argument_list|,
name|qualLength
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
name|bytes
argument_list|,
name|offset
argument_list|,
name|qualLength
argument_list|,
name|timestamp
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
name|bytes
argument_list|,
name|offset
argument_list|,
name|qualLength
argument_list|)
return|;
case|case
name|VERSION_DELETED
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
name|bytes
argument_list|,
name|offset
argument_list|,
name|qualLength
argument_list|)
return|;
block|}
comment|/**      * Filters should be checked before checking column trackers. If we do      * otherwise, as was previously being done, ColumnTracker may increment its      * counter for even that KV which may be discarded later on by Filter. This      * would lead to incorrect results in certain cases.      */
name|ReturnCode
name|filterResponse
init|=
name|ReturnCode
operator|.
name|SKIP
decl_stmt|;
if|if
condition|(
name|filter
operator|!=
literal|null
condition|)
block|{
name|filterResponse
operator|=
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|kv
argument_list|)
expr_stmt|;
if|if
condition|(
name|filterResponse
operator|==
name|ReturnCode
operator|.
name|SKIP
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
name|filterResponse
operator|==
name|ReturnCode
operator|.
name|NEXT_COL
condition|)
block|{
return|return
name|columns
operator|.
name|getNextRowOrNextColumn
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|qualLength
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|filterResponse
operator|==
name|ReturnCode
operator|.
name|NEXT_ROW
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
elseif|else
if|if
condition|(
name|filterResponse
operator|==
name|ReturnCode
operator|.
name|SEEK_NEXT_USING_HINT
condition|)
block|{
return|return
name|MatchCode
operator|.
name|SEEK_NEXT_USING_HINT
return|;
block|}
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
argument_list|,
name|timestamp
argument_list|,
name|type
argument_list|,
name|kv
operator|.
name|getMemstoreTS
argument_list|()
operator|>
name|maxReadPointToTrackVersions
argument_list|)
decl_stmt|;
comment|/*      * According to current implementation, colChecker can only be      * SEEK_NEXT_COL, SEEK_NEXT_ROW, SKIP or INCLUDE. Therefore, always return      * the MatchCode. If it is SEEK_NEXT_ROW, also set stickyNextRow.      */
if|if
condition|(
name|colChecker
operator|==
name|MatchCode
operator|.
name|SEEK_NEXT_ROW
condition|)
block|{
name|stickyNextRow
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|filter
operator|!=
literal|null
operator|&&
name|colChecker
operator|==
name|MatchCode
operator|.
name|INCLUDE
operator|&&
name|filterResponse
operator|==
name|ReturnCode
operator|.
name|INCLUDE_AND_NEXT_COL
condition|)
block|{
return|return
name|MatchCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_COL
return|;
block|}
return|return
name|colChecker
return|;
block|}
specifier|public
name|boolean
name|moreRowsMayExistAfter
parameter_list|(
name|KeyValue
name|kv
parameter_list|)
block|{
if|if
condition|(
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|stopRow
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
operator|&&
name|rowComparator
operator|.
name|compareRows
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|stopRow
argument_list|,
literal|0
argument_list|,
name|stopRow
operator|.
name|length
argument_list|)
operator|>=
literal|0
condition|)
block|{
comment|// KV>= STOPROW
comment|// then NO there is nothing left.
return|return
literal|false
return|;
block|}
else|else
block|{
return|return
literal|true
return|;
block|}
block|}
comment|/**    * Set current row    * @param row    */
specifier|public
name|void
name|setRow
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|int
name|offset
parameter_list|,
name|short
name|length
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
name|this
operator|.
name|rowOffset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|rowLength
operator|=
name|length
expr_stmt|;
name|reset
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|this
operator|.
name|deletes
operator|.
name|reset
argument_list|()
expr_stmt|;
name|this
operator|.
name|columns
operator|.
name|reset
argument_list|()
expr_stmt|;
name|stickyNextRow
operator|=
literal|false
expr_stmt|;
block|}
comment|/**    *    * @return the start key    */
specifier|public
name|KeyValue
name|getStartKey
parameter_list|()
block|{
return|return
name|this
operator|.
name|startKey
return|;
block|}
comment|/**    *    * @return the Filter    */
name|Filter
name|getFilter
parameter_list|()
block|{
return|return
name|this
operator|.
name|filter
return|;
block|}
specifier|public
name|KeyValue
name|getNextKeyHint
parameter_list|(
name|KeyValue
name|kv
parameter_list|)
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
name|getNextKeyHint
argument_list|(
name|kv
argument_list|)
return|;
block|}
block|}
specifier|public
name|KeyValue
name|getKeyForNextColumn
parameter_list|(
name|KeyValue
name|kv
parameter_list|)
block|{
name|ColumnCount
name|nextColumn
init|=
name|columns
operator|.
name|getColumnHint
argument_list|()
decl_stmt|;
if|if
condition|(
name|nextColumn
operator|==
literal|null
condition|)
block|{
return|return
name|KeyValue
operator|.
name|createLastOnRow
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getFamilyLength
argument_list|()
argument_list|,
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|KeyValue
operator|.
name|createFirstOnRow
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getFamilyLength
argument_list|()
argument_list|,
name|nextColumn
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|nextColumn
operator|.
name|getOffset
argument_list|()
argument_list|,
name|nextColumn
operator|.
name|getLength
argument_list|()
argument_list|)
return|;
block|}
block|}
specifier|public
name|KeyValue
name|getKeyForNextRow
parameter_list|(
name|KeyValue
name|kv
parameter_list|)
block|{
return|return
name|KeyValue
operator|.
name|createLastOnRow
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowLength
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
return|;
block|}
comment|/**    * {@link #match} return codes.  These instruct the scanner moving through    * memstores and StoreFiles what to do with the current KeyValue.    *<p>    * Additionally, this contains "early-out" language to tell the scanner to    * move on to the next File (memstore or Storefile), or to return immediately.    */
specifier|public
specifier|static
enum|enum
name|MatchCode
block|{
comment|/**      * Include KeyValue in the returned result      */
name|INCLUDE
block|,
comment|/**      * Do not include KeyValue in the returned result      */
name|SKIP
block|,
comment|/**      * Do not include, jump to next StoreFile or memstore (in time order)      */
name|NEXT
block|,
comment|/**      * Do not include, return current result      */
name|DONE
block|,
comment|/**      * These codes are used by the ScanQueryMatcher      */
comment|/**      * Done with the row, seek there.      */
name|SEEK_NEXT_ROW
block|,
comment|/**      * Done with column, seek to next.      */
name|SEEK_NEXT_COL
block|,
comment|/**      * Done with scan, thanks to the row filter.      */
name|DONE_SCAN
block|,
comment|/*      * Seek to next key which is given as hint.      */
name|SEEK_NEXT_USING_HINT
block|,
comment|/**      * Include KeyValue and done with column, seek to next.      */
name|INCLUDE_AND_SEEK_NEXT_COL
block|,
comment|/**      * Include KeyValue and done with row, seek to next.      */
name|INCLUDE_AND_SEEK_NEXT_ROW
block|,   }
block|}
end_class

end_unit

