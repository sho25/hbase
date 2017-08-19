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
name|Iterator
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
name|KeyValue
operator|.
name|Type
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
name|KeyValueUtil
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
name|Tag
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
name|TagType
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
name|TagUtil
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
name|ShipperListener
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
name|security
operator|.
name|visibility
operator|.
name|VisibilityNewVersionBehaivorTracker
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
name|security
operator|.
name|visibility
operator|.
name|VisibilityScanDeleteTracker
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
name|Pair
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
specifier|abstract
class|class
name|ScanQueryMatcher
implements|implements
name|ShipperListener
block|{
comment|/**    * {@link #match} return codes. These instruct the scanner moving through memstores and StoreFiles    * what to do with the current KeyValue.    *<p>    * Additionally, this contains "early-out" language to tell the scanner to move on to the next    * File (memstore or Storefile), or to return immediately.    */
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
comment|/**      * Seek to next key which is given as hint.      */
name|SEEK_NEXT_USING_HINT
block|,
comment|/**      * Include KeyValue and done with column, seek to next.      */
name|INCLUDE_AND_SEEK_NEXT_COL
block|,
comment|/**      * Include KeyValue and done with row, seek to next.      */
name|INCLUDE_AND_SEEK_NEXT_ROW
block|,   }
comment|/** Row comparator for the region this query is for */
specifier|protected
specifier|final
name|CellComparator
name|rowComparator
decl_stmt|;
comment|/** Key to seek to in memstore and StoreFiles */
specifier|protected
specifier|final
name|Cell
name|startKey
decl_stmt|;
comment|/** Keeps track of columns and versions */
specifier|protected
specifier|final
name|ColumnTracker
name|columns
decl_stmt|;
comment|/** The oldest timestamp we are interested in, based on TTL */
specifier|protected
specifier|final
name|long
name|oldestUnexpiredTS
decl_stmt|;
specifier|protected
specifier|final
name|long
name|now
decl_stmt|;
comment|/** Row the query is on */
specifier|protected
name|Cell
name|currentRow
decl_stmt|;
specifier|protected
name|ScanQueryMatcher
parameter_list|(
name|Cell
name|startKey
parameter_list|,
name|ScanInfo
name|scanInfo
parameter_list|,
name|ColumnTracker
name|columns
parameter_list|,
name|long
name|oldestUnexpiredTS
parameter_list|,
name|long
name|now
parameter_list|)
block|{
name|this
operator|.
name|rowComparator
operator|=
name|scanInfo
operator|.
name|getComparator
argument_list|()
expr_stmt|;
name|this
operator|.
name|startKey
operator|=
name|startKey
expr_stmt|;
name|this
operator|.
name|oldestUnexpiredTS
operator|=
name|oldestUnexpiredTS
expr_stmt|;
name|this
operator|.
name|now
operator|=
name|now
expr_stmt|;
name|this
operator|.
name|columns
operator|=
name|columns
expr_stmt|;
block|}
comment|/**    * @param cell    * @param oldestTimestamp    * @return true if the cell is expired    */
specifier|private
specifier|static
name|boolean
name|isCellTTLExpired
parameter_list|(
specifier|final
name|Cell
name|cell
parameter_list|,
specifier|final
name|long
name|oldestTimestamp
parameter_list|,
specifier|final
name|long
name|now
parameter_list|)
block|{
comment|// Look for a TTL tag first. Use it instead of the family setting if
comment|// found. If a cell has multiple TTLs, resolve the conflict by using the
comment|// first tag encountered.
name|Iterator
argument_list|<
name|Tag
argument_list|>
name|i
init|=
name|CellUtil
operator|.
name|tagsIterator
argument_list|(
name|cell
argument_list|)
decl_stmt|;
while|while
condition|(
name|i
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Tag
name|t
init|=
name|i
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|TagType
operator|.
name|TTL_TAG_TYPE
operator|==
name|t
operator|.
name|getType
argument_list|()
condition|)
block|{
comment|// Unlike in schema cell TTLs are stored in milliseconds, no need
comment|// to convert
name|long
name|ts
init|=
name|cell
operator|.
name|getTimestamp
argument_list|()
decl_stmt|;
assert|assert
name|t
operator|.
name|getValueLength
argument_list|()
operator|==
name|Bytes
operator|.
name|SIZEOF_LONG
assert|;
name|long
name|ttl
init|=
name|TagUtil
operator|.
name|getValueAsLong
argument_list|(
name|t
argument_list|)
decl_stmt|;
if|if
condition|(
name|ts
operator|+
name|ttl
operator|<
name|now
condition|)
block|{
return|return
literal|true
return|;
block|}
comment|// Per cell TTLs cannot extend lifetime beyond family settings, so
comment|// fall through to check that
break|break;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Check before the delete logic.    * @return null means continue.    */
specifier|protected
specifier|final
name|MatchCode
name|preCheck
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
if|if
condition|(
name|currentRow
operator|==
literal|null
condition|)
block|{
comment|// Since the curCell is null it means we are already sure that we have moved over to the next
comment|// row
return|return
name|MatchCode
operator|.
name|DONE
return|;
block|}
comment|// if row key is changed, then we know that we have moved over to the next row
if|if
condition|(
name|rowComparator
operator|.
name|compareRows
argument_list|(
name|currentRow
argument_list|,
name|cell
argument_list|)
operator|!=
literal|0
condition|)
block|{
return|return
name|MatchCode
operator|.
name|DONE
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
return|return
name|MatchCode
operator|.
name|SEEK_NEXT_ROW
return|;
block|}
name|long
name|timestamp
init|=
name|cell
operator|.
name|getTimestamp
argument_list|()
decl_stmt|;
comment|// check if this is a fake cell. The fake cell is an optimization, we should make the scanner
comment|// seek to next column or next row. See StoreFileScanner.requestSeek for more details.
comment|// check for early out based on timestamp alone
if|if
condition|(
name|timestamp
operator|==
name|HConstants
operator|.
name|OLDEST_TIMESTAMP
operator|||
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
name|cell
argument_list|)
return|;
block|}
comment|// check if the cell is expired by cell TTL
if|if
condition|(
name|isCellTTLExpired
argument_list|(
name|cell
argument_list|,
name|this
operator|.
name|oldestUnexpiredTS
argument_list|,
name|this
operator|.
name|now
argument_list|)
condition|)
block|{
return|return
name|MatchCode
operator|.
name|SKIP
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|protected
specifier|final
name|MatchCode
name|checkDeleted
parameter_list|(
name|DeleteTracker
name|deletes
parameter_list|,
name|Cell
name|cell
parameter_list|)
block|{
if|if
condition|(
name|deletes
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|!
operator|(
name|deletes
operator|instanceof
name|NewVersionBehaviorTracker
operator|)
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// MvccSensitiveTracker always need check all cells to save some infos.
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
if|if
condition|(
operator|!
operator|(
name|deletes
operator|instanceof
name|NewVersionBehaviorTracker
operator|)
condition|)
block|{
comment|// MvccSensitive can not seek to next because the Put with lower ts may have higher mvcc
return|return
name|columns
operator|.
name|getNextRowOrNextColumn
argument_list|(
name|cell
argument_list|)
return|;
block|}
case|case
name|VERSION_DELETED
case|:
case|case
name|FAMILY_VERSION_DELETED
case|:
case|case
name|VERSION_MASKED
case|:
return|return
name|MatchCode
operator|.
name|SKIP
return|;
case|case
name|NOT_DELETED
case|:
return|return
literal|null
return|;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected delete result: "
operator|+
name|deleteResult
argument_list|)
throw|;
block|}
block|}
comment|/**    * Determines if the caller should do one of several things:    *<ul>    *<li>seek/skip to the next row (MatchCode.SEEK_NEXT_ROW)</li>    *<li>seek/skip to the next column (MatchCode.SEEK_NEXT_COL)</li>    *<li>include the current KeyValue (MatchCode.INCLUDE)</li>    *<li>ignore the current KeyValue (MatchCode.SKIP)</li>    *<li>got to the next row (MatchCode.DONE)</li>    *</ul>    * @param cell KeyValue to check    * @return The match code instance.    * @throws IOException in case there is an internal consistency problem caused by a data    *           corruption.    */
specifier|public
specifier|abstract
name|MatchCode
name|match
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @return the start key    */
specifier|public
name|Cell
name|getStartKey
parameter_list|()
block|{
return|return
name|startKey
return|;
block|}
comment|/**    * @return whether there is an null column in the query    */
specifier|public
specifier|abstract
name|boolean
name|hasNullColumnInQuery
parameter_list|()
function_decl|;
comment|/**    * @return a cell represent the current row    */
specifier|public
name|Cell
name|currentRow
parameter_list|()
block|{
return|return
name|currentRow
return|;
block|}
comment|/**    * Make {@link #currentRow()} return null.    */
specifier|public
name|void
name|clearCurrentRow
parameter_list|()
block|{
name|currentRow
operator|=
literal|null
expr_stmt|;
block|}
specifier|protected
specifier|abstract
name|void
name|reset
parameter_list|()
function_decl|;
comment|/**    * Set the row when there is change in row    * @param currentRow    */
specifier|public
name|void
name|setToNewRow
parameter_list|(
name|Cell
name|currentRow
parameter_list|)
block|{
name|this
operator|.
name|currentRow
operator|=
name|currentRow
expr_stmt|;
name|columns
operator|.
name|reset
argument_list|()
expr_stmt|;
name|reset
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|abstract
name|boolean
name|isUserScan
parameter_list|()
function_decl|;
comment|/**    * @return Returns false if we know there are no more rows to be scanned (We've reached the    *<code>stopRow</code> or we are scanning on row only because this Scan is for a Get,    *         etc.    */
specifier|public
specifier|abstract
name|boolean
name|moreRowsMayExistAfter
parameter_list|(
name|Cell
name|cell
parameter_list|)
function_decl|;
specifier|public
name|Cell
name|getKeyForNextColumn
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
comment|// We aren't sure whether any DeleteFamily cells exist, so we can't skip to next column.
comment|// TODO: Current way disable us to seek to next column quickly. Is there any better solution?
comment|// see HBASE-18471 for more details
comment|// see TestFromClientSide3#testScanAfterDeletingSpecifiedRow
comment|// see TestFromClientSide3#testScanAfterDeletingSpecifiedRowV2
if|if
condition|(
name|cell
operator|.
name|getQualifierLength
argument_list|()
operator|==
literal|0
condition|)
block|{
name|Cell
name|nextKey
init|=
name|CellUtil
operator|.
name|createNextOnRowCol
argument_list|(
name|cell
argument_list|)
decl_stmt|;
if|if
condition|(
name|nextKey
operator|!=
name|cell
condition|)
block|{
return|return
name|nextKey
return|;
block|}
comment|// The cell is at the end of row/family/qualifier, so it is impossible to find any DeleteFamily cells.
comment|// Let us seek to next column.
block|}
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
name|CellUtil
operator|.
name|createLastOnRowCol
argument_list|(
name|cell
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|CellUtil
operator|.
name|createFirstOnRowCol
argument_list|(
name|cell
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
comment|/**    * @param nextIndexed the key of the next entry in the block index (if any)    * @param currentCell The Cell we're using to calculate the seek key    * @return result of the compare between the indexed key and the key portion of the passed cell    */
specifier|public
name|int
name|compareKeyForNextRow
parameter_list|(
name|Cell
name|nextIndexed
parameter_list|,
name|Cell
name|currentCell
parameter_list|)
block|{
return|return
name|rowComparator
operator|.
name|compareKeyBasedOnColHint
argument_list|(
name|nextIndexed
argument_list|,
name|currentCell
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
argument_list|,
name|HConstants
operator|.
name|OLDEST_TIMESTAMP
argument_list|,
name|Type
operator|.
name|Minimum
operator|.
name|getCode
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @param nextIndexed the key of the next entry in the block index (if any)    * @param currentCell The Cell we're using to calculate the seek key    * @return result of the compare between the indexed key and the key portion of the passed cell    */
specifier|public
name|int
name|compareKeyForNextColumn
parameter_list|(
name|Cell
name|nextIndexed
parameter_list|,
name|Cell
name|currentCell
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
name|rowComparator
operator|.
name|compareKeyBasedOnColHint
argument_list|(
name|nextIndexed
argument_list|,
name|currentCell
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
argument_list|,
name|HConstants
operator|.
name|OLDEST_TIMESTAMP
argument_list|,
name|Type
operator|.
name|Minimum
operator|.
name|getCode
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|rowComparator
operator|.
name|compareKeyBasedOnColHint
argument_list|(
name|nextIndexed
argument_list|,
name|currentCell
argument_list|,
name|currentCell
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|currentCell
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
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|Type
operator|.
name|Maximum
operator|.
name|getCode
argument_list|()
argument_list|)
return|;
block|}
block|}
comment|/**    * @return the Filter    */
specifier|public
specifier|abstract
name|Filter
name|getFilter
parameter_list|()
function_decl|;
comment|/**    * Delegate to {@link Filter#getNextCellHint(Cell)}. If no filter, return {@code null}.    */
specifier|public
specifier|abstract
name|Cell
name|getNextKeyHint
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
specifier|public
name|void
name|beforeShipped
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|currentRow
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|currentRow
operator|=
name|CellUtil
operator|.
name|createFirstOnRow
argument_list|(
name|CellUtil
operator|.
name|copyRow
argument_list|(
name|this
operator|.
name|currentRow
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|columns
operator|!=
literal|null
condition|)
block|{
name|columns
operator|.
name|beforeShipped
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
specifier|static
name|Cell
name|createStartKeyFromRow
parameter_list|(
name|byte
index|[]
name|startRow
parameter_list|,
name|ScanInfo
name|scanInfo
parameter_list|)
block|{
return|return
name|CellUtil
operator|.
name|createFirstDeleteFamilyCellOnRow
argument_list|(
name|startRow
argument_list|,
name|scanInfo
operator|.
name|getFamily
argument_list|()
argument_list|)
return|;
block|}
specifier|protected
specifier|static
name|Pair
argument_list|<
name|DeleteTracker
argument_list|,
name|ColumnTracker
argument_list|>
name|getTrackers
parameter_list|(
name|RegionCoprocessorHost
name|host
parameter_list|,
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
parameter_list|,
name|ScanInfo
name|scanInfo
parameter_list|,
name|long
name|oldestUnexpiredTS
parameter_list|,
name|Scan
name|userScan
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|resultMaxVersion
init|=
name|scanInfo
operator|.
name|getMaxVersions
argument_list|()
decl_stmt|;
name|int
name|maxVersionToCheck
init|=
name|resultMaxVersion
decl_stmt|;
if|if
condition|(
name|userScan
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|userScan
operator|.
name|isRaw
argument_list|()
condition|)
block|{
name|resultMaxVersion
operator|=
name|userScan
operator|.
name|getMaxVersions
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|resultMaxVersion
operator|=
name|Math
operator|.
name|min
argument_list|(
name|userScan
operator|.
name|getMaxVersions
argument_list|()
argument_list|,
name|scanInfo
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|maxVersionToCheck
operator|=
name|userScan
operator|.
name|hasFilter
argument_list|()
condition|?
name|scanInfo
operator|.
name|getMaxVersions
argument_list|()
else|:
name|resultMaxVersion
expr_stmt|;
block|}
name|DeleteTracker
name|deleteTracker
decl_stmt|;
if|if
condition|(
name|scanInfo
operator|.
name|isNewVersionBehavior
argument_list|()
operator|&&
operator|(
name|userScan
operator|==
literal|null
operator|||
operator|!
name|userScan
operator|.
name|isRaw
argument_list|()
operator|)
condition|)
block|{
name|deleteTracker
operator|=
operator|new
name|NewVersionBehaviorTracker
argument_list|(
name|columns
argument_list|,
name|scanInfo
operator|.
name|getMinVersions
argument_list|()
argument_list|,
name|scanInfo
operator|.
name|getMaxVersions
argument_list|()
argument_list|,
name|resultMaxVersion
argument_list|,
name|oldestUnexpiredTS
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|deleteTracker
operator|=
operator|new
name|ScanDeleteTracker
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|host
operator|!=
literal|null
condition|)
block|{
name|deleteTracker
operator|=
name|host
operator|.
name|postInstantiateDeleteTracker
argument_list|(
name|deleteTracker
argument_list|)
expr_stmt|;
if|if
condition|(
name|deleteTracker
operator|instanceof
name|VisibilityScanDeleteTracker
operator|&&
name|scanInfo
operator|.
name|isNewVersionBehavior
argument_list|()
condition|)
block|{
name|deleteTracker
operator|=
operator|new
name|VisibilityNewVersionBehaivorTracker
argument_list|(
name|columns
argument_list|,
name|scanInfo
operator|.
name|getMinVersions
argument_list|()
argument_list|,
name|scanInfo
operator|.
name|getMaxVersions
argument_list|()
argument_list|,
name|resultMaxVersion
argument_list|,
name|oldestUnexpiredTS
argument_list|)
expr_stmt|;
block|}
block|}
name|ColumnTracker
name|columnTracker
decl_stmt|;
if|if
condition|(
name|deleteTracker
operator|instanceof
name|NewVersionBehaviorTracker
condition|)
block|{
name|columnTracker
operator|=
operator|(
name|NewVersionBehaviorTracker
operator|)
name|deleteTracker
expr_stmt|;
block|}
elseif|else
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
name|maxVersionToCheck
argument_list|,
name|oldestUnexpiredTS
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
name|maxVersionToCheck
argument_list|,
name|oldestUnexpiredTS
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|Pair
argument_list|<>
argument_list|(
name|deleteTracker
argument_list|,
name|columnTracker
argument_list|)
return|;
block|}
comment|// Used only for testing purposes
specifier|static
name|MatchCode
name|checkColumn
parameter_list|(
name|ColumnTracker
name|columnTracker
parameter_list|,
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|long
name|ttl
parameter_list|,
name|byte
name|type
parameter_list|,
name|boolean
name|ignoreCount
parameter_list|)
throws|throws
name|IOException
block|{
name|KeyValue
name|kv
init|=
name|KeyValueUtil
operator|.
name|createFirstOnRow
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
decl_stmt|;
name|MatchCode
name|matchCode
init|=
name|columnTracker
operator|.
name|checkColumn
argument_list|(
name|kv
argument_list|,
name|type
argument_list|)
decl_stmt|;
if|if
condition|(
name|matchCode
operator|==
name|MatchCode
operator|.
name|INCLUDE
condition|)
block|{
return|return
name|columnTracker
operator|.
name|checkVersions
argument_list|(
name|kv
argument_list|,
name|ttl
argument_list|,
name|type
argument_list|,
name|ignoreCount
argument_list|)
return|;
block|}
return|return
name|matchCode
return|;
block|}
block|}
end_class

end_unit

