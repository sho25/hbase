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
name|KeyValue
operator|.
name|KeyComparator
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
name|Get
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * This is the primary class used to process KeyValues during a Get or Scan  * operation.  *<p>  * It encapsulates the handling of the column and version input parameters to   * the query through a {@link ColumnTracker}.  *<p>  * Deletes are handled using the {@link DeleteTracker}.  *<p>  * All other query parameters are accessed from the client-specified Get.  *<p>  * The primary method used is {@link #match} with the current KeyValue.  It will  * return a {@link QueryMatcher.MatchCode}   *   * , deletes,  * versions,   */
end_comment

begin_class
specifier|public
class|class
name|QueryMatcher
block|{
comment|/**    * {@link #match} return codes.  These instruct the scanner moving through    * memstores and StoreFiles what to do with the current KeyValue.    *<p>    * Additionally, this contains "early-out" language to tell the scanner to    * move on to the next File (memstore or Storefile), or to return immediately.    */
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
block|,   }
comment|/** Keeps track of deletes */
specifier|protected
name|DeleteTracker
name|deletes
decl_stmt|;
comment|/** Keeps track of columns and versions */
specifier|protected
name|ColumnTracker
name|columns
decl_stmt|;
comment|/** Key to seek to in memstore and StoreFiles */
specifier|protected
name|KeyValue
name|startKey
decl_stmt|;
comment|/** Row comparator for the region this query is for */
name|KeyComparator
name|rowComparator
decl_stmt|;
comment|/** Row the query is on */
specifier|protected
name|byte
index|[]
name|row
decl_stmt|;
comment|/** TimeRange the query is for */
specifier|protected
name|TimeRange
name|tr
decl_stmt|;
comment|/** Oldest allowed version stamp for TTL enforcement */
specifier|protected
name|long
name|oldestStamp
decl_stmt|;
specifier|protected
name|Filter
name|filter
decl_stmt|;
comment|/**    * Constructs a QueryMatcher for a Get.    * @param get    * @param family    * @param columns    * @param ttl    * @param rowComparator    */
specifier|public
name|QueryMatcher
parameter_list|(
name|Get
name|get
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
name|KeyComparator
name|rowComparator
parameter_list|,
name|int
name|maxVersions
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
name|get
operator|.
name|getRow
argument_list|()
expr_stmt|;
name|this
operator|.
name|filter
operator|=
name|get
operator|.
name|getFilter
argument_list|()
expr_stmt|;
name|this
operator|.
name|tr
operator|=
name|get
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
name|this
operator|.
name|deletes
operator|=
operator|new
name|GetDeleteTracker
argument_list|()
expr_stmt|;
name|this
operator|.
name|startKey
operator|=
name|KeyValue
operator|.
name|createFirstOnRow
argument_list|(
name|row
argument_list|)
expr_stmt|;
comment|// Single branch to deal with two types of Gets (columns vs all in family)
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
name|this
operator|.
name|columns
operator|=
operator|new
name|WildcardColumnTracker
argument_list|(
name|maxVersions
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
comment|// For the subclasses.
specifier|protected
name|QueryMatcher
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constructs a copy of an existing QueryMatcher with a new row.    * @param matcher    * @param row    */
specifier|public
name|QueryMatcher
parameter_list|(
name|QueryMatcher
name|matcher
parameter_list|,
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
name|this
operator|.
name|filter
operator|=
name|matcher
operator|.
name|filter
expr_stmt|;
name|this
operator|.
name|tr
operator|=
name|matcher
operator|.
name|getTimeRange
argument_list|()
expr_stmt|;
name|this
operator|.
name|oldestStamp
operator|=
name|matcher
operator|.
name|getOldestStamp
argument_list|()
expr_stmt|;
name|this
operator|.
name|rowComparator
operator|=
name|matcher
operator|.
name|getRowComparator
argument_list|()
expr_stmt|;
name|this
operator|.
name|columns
operator|=
name|matcher
operator|.
name|getColumnTracker
argument_list|()
expr_stmt|;
name|this
operator|.
name|deletes
operator|=
name|matcher
operator|.
name|getDeleteTracker
argument_list|()
expr_stmt|;
name|this
operator|.
name|startKey
operator|=
name|matcher
operator|.
name|getStartKey
argument_list|()
expr_stmt|;
name|reset
argument_list|()
expr_stmt|;
block|}
comment|/**    * Main method for ColumnMatcher.    *<p>    * Determines whether the specified KeyValue should be included in the    * result or not.    *<p>    * Contains additional language to early-out of the current file or to    * return immediately.    *<p>    * Things to be checked:<ul>    *<li>Row    *<li>TTL    *<li>Type    *<li>TimeRange    *<li>Deletes    *<li>Column    *<li>Versions    * @param kv KeyValue to check    * @return MatchCode: include, skip, next, done    */
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
name|DONE
return|;
comment|// done_row
block|}
if|if
condition|(
name|this
operator|.
name|filter
operator|!=
literal|null
operator|&&
name|this
operator|.
name|filter
operator|.
name|filterAllRemaining
argument_list|()
condition|)
block|{
return|return
name|MatchCode
operator|.
name|DONE
return|;
block|}
comment|// Directly act on KV buffer
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
name|keyLength
init|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|bytes
argument_list|,
name|offset
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
argument_list|)
decl_stmt|;
name|offset
operator|+=
name|Bytes
operator|.
name|SIZEOF_SHORT
expr_stmt|;
comment|// scanners are relying on us to check the row first, and return
comment|// "NEXT" when we are there.
comment|/* Check ROW      * If past query's row, go to next StoreFile      * If not reached query's row, go to next KeyValue      */
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
comment|// Have reached the next row
return|return
name|MatchCode
operator|.
name|NEXT
return|;
comment|// got_to_next_row (end)
block|}
elseif|else
if|if
condition|(
name|ret
operator|>=
literal|1
condition|)
block|{
comment|// At a previous row
return|return
name|MatchCode
operator|.
name|SKIP
return|;
comment|// skip_to_cur_row
block|}
name|offset
operator|+=
name|rowLength
expr_stmt|;
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
name|Bytes
operator|.
name|SIZEOF_BYTE
operator|+
name|familyLength
expr_stmt|;
name|int
name|columnLength
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
name|kv
operator|.
name|getOffset
argument_list|()
operator|)
operator|-
name|KeyValue
operator|.
name|TIMESTAMP_TYPE_SIZE
decl_stmt|;
name|int
name|columnOffset
init|=
name|offset
decl_stmt|;
name|offset
operator|+=
name|columnLength
expr_stmt|;
comment|/* Check TTL      * If expired, go to next KeyValue      */
name|long
name|timestamp
init|=
name|Bytes
operator|.
name|toLong
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|)
decl_stmt|;
if|if
condition|(
name|isExpired
argument_list|(
name|timestamp
argument_list|)
condition|)
block|{
comment|// reached the expired part, for scans, this indicates we're done.
return|return
name|MatchCode
operator|.
name|NEXT
return|;
comment|// done_row
block|}
name|offset
operator|+=
name|Bytes
operator|.
name|SIZEOF_LONG
expr_stmt|;
comment|/* Check TYPE      * If a delete within (or after) time range, add to deletes      * Move to next KeyValue      */
name|byte
name|type
init|=
name|bytes
index|[
name|offset
index|]
decl_stmt|;
comment|// if delete type == delete family, return done_row
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
name|columnOffset
argument_list|,
name|columnLength
argument_list|,
name|timestamp
argument_list|,
name|type
argument_list|)
expr_stmt|;
block|}
return|return
name|MatchCode
operator|.
name|SKIP
return|;
comment|// skip the delete cell.
block|}
comment|/* Check TimeRange      * If outside of range, move to next KeyValue      */
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
comment|// optimization chances here.
block|}
comment|/* Check Deletes      * If deleted, move to next KeyValue       */
if|if
condition|(
operator|!
name|deletes
operator|.
name|isEmpty
argument_list|()
operator|&&
name|deletes
operator|.
name|isDeleted
argument_list|(
name|bytes
argument_list|,
name|columnOffset
argument_list|,
name|columnLength
argument_list|,
name|timestamp
argument_list|)
condition|)
block|{
comment|// 2 types of deletes:
comment|// affects 1 cell or 1 column, so just skip the keyvalues.
comment|// - delete family, so just skip to the next row.
return|return
name|MatchCode
operator|.
name|SKIP
return|;
block|}
comment|/* Check Column and Versions      * Returns a MatchCode directly, identical language      * If matched column without enough versions, include      * If enough versions of this column or does not match, skip      * If have moved past       * If enough versions of everything,       * TODO: No mapping from Filter.ReturnCode to MatchCode.      */
name|MatchCode
name|mc
init|=
name|columns
operator|.
name|checkColumn
argument_list|(
name|bytes
argument_list|,
name|columnOffset
argument_list|,
name|columnLength
argument_list|)
decl_stmt|;
if|if
condition|(
name|mc
operator|==
name|MatchCode
operator|.
name|INCLUDE
operator|&&
name|this
operator|.
name|filter
operator|!=
literal|null
condition|)
block|{
switch|switch
condition|(
name|this
operator|.
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|kv
argument_list|)
condition|)
block|{
case|case
name|INCLUDE
case|:
return|return
name|MatchCode
operator|.
name|INCLUDE
return|;
case|case
name|SKIP
case|:
return|return
name|MatchCode
operator|.
name|SKIP
return|;
default|default:
return|return
name|MatchCode
operator|.
name|DONE
return|;
block|}
block|}
return|return
name|mc
return|;
block|}
comment|// should be in KeyValue.
specifier|protected
name|boolean
name|isDelete
parameter_list|(
name|byte
name|type
parameter_list|)
block|{
return|return
operator|(
name|type
operator|!=
name|KeyValue
operator|.
name|Type
operator|.
name|Put
operator|.
name|getCode
argument_list|()
operator|)
return|;
block|}
specifier|protected
name|boolean
name|isExpired
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
return|return
operator|(
name|timestamp
operator|<
name|oldestStamp
operator|)
return|;
block|}
comment|/**    * If matcher returns SEEK_NEXT_COL you may be able    * to get a hint of the next column to seek to - call this.    * If it returns null, there is no hint.    *    * @return immediately after match returns SEEK_NEXT_COL - null if no hint,    *  else the next column we want    */
specifier|public
name|ColumnCount
name|getSeekColumn
parameter_list|()
block|{
return|return
name|this
operator|.
name|columns
operator|.
name|getColumnHint
argument_list|()
return|;
block|}
comment|/**    * Called after reading each section (memstore, snapshot, storefiles).    *<p>    * This method will update the internal structures to be accurate for    * the next section.     */
specifier|public
name|void
name|update
parameter_list|()
block|{
name|this
operator|.
name|deletes
operator|.
name|update
argument_list|()
expr_stmt|;
name|this
operator|.
name|columns
operator|.
name|update
argument_list|()
expr_stmt|;
block|}
comment|/**    * Resets the current columns and deletes    */
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
if|if
condition|(
name|this
operator|.
name|filter
operator|!=
literal|null
condition|)
name|this
operator|.
name|filter
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
comment|/**    * Set current row    * @param row    */
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
block|}
comment|/**    *     * @return the start key    */
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
comment|/**    * @return the TimeRange    */
specifier|public
name|TimeRange
name|getTimeRange
parameter_list|()
block|{
return|return
name|this
operator|.
name|tr
return|;
block|}
comment|/**    * @return the oldest stamp    */
specifier|public
name|long
name|getOldestStamp
parameter_list|()
block|{
return|return
name|this
operator|.
name|oldestStamp
return|;
block|}
comment|/**    * @return current KeyComparator    */
specifier|public
name|KeyComparator
name|getRowComparator
parameter_list|()
block|{
return|return
name|this
operator|.
name|rowComparator
return|;
block|}
comment|/**    * @return ColumnTracker    */
specifier|public
name|ColumnTracker
name|getColumnTracker
parameter_list|()
block|{
return|return
name|this
operator|.
name|columns
return|;
block|}
comment|/**    * @return DeleteTracker    */
specifier|public
name|DeleteTracker
name|getDeleteTracker
parameter_list|()
block|{
return|return
name|this
operator|.
name|deletes
return|;
block|}
comment|/**    *     * @return<code>true</code> when done.    */
specifier|public
name|boolean
name|isDone
parameter_list|()
block|{
return|return
name|this
operator|.
name|columns
operator|.
name|done
argument_list|()
return|;
block|}
block|}
end_class

end_unit

