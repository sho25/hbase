begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Map
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|locks
operator|.
name|ReentrantReadWriteLock
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
name|io
operator|.
name|hfile
operator|.
name|HFileScanner
import|;
end_import

begin_comment
comment|/**  * A scanner that iterates through HStore files  */
end_comment

begin_class
class|class
name|StoreFileScanner
extends|extends
name|HAbstractScanner
implements|implements
name|ChangedReadersObserver
block|{
comment|// Keys retrieved from the sources
specifier|private
specifier|volatile
name|KeyValue
name|keys
index|[]
decl_stmt|;
comment|// Readers we go against.
specifier|private
specifier|volatile
name|HFileScanner
index|[]
name|scanners
decl_stmt|;
comment|// Store this scanner came out of.
specifier|private
specifier|final
name|Store
name|store
decl_stmt|;
comment|// Used around replacement of Readers if they change while we're scanning.
specifier|private
specifier|final
name|ReentrantReadWriteLock
name|lock
init|=
operator|new
name|ReentrantReadWriteLock
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
comment|/**    * @param store    * @param timestamp    * @param columns    * @param firstRow    * @param deletes Set of running deletes    * @throws IOException    */
specifier|public
name|StoreFileScanner
parameter_list|(
specifier|final
name|Store
name|store
parameter_list|,
specifier|final
name|long
name|timestamp
parameter_list|,
specifier|final
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
parameter_list|,
specifier|final
name|byte
index|[]
name|firstRow
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|timestamp
argument_list|,
name|columns
argument_list|)
expr_stmt|;
name|this
operator|.
name|store
operator|=
name|store
expr_stmt|;
name|this
operator|.
name|store
operator|.
name|addChangedReaderObserver
argument_list|(
name|this
argument_list|)
expr_stmt|;
try|try
block|{
name|openScanner
argument_list|(
name|firstRow
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|close
argument_list|()
expr_stmt|;
name|IOException
name|e
init|=
operator|new
name|IOException
argument_list|(
literal|"HStoreScanner failed construction"
argument_list|)
decl_stmt|;
name|e
operator|.
name|initCause
argument_list|(
name|ex
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
comment|/*    * Go open new scanners and cue them at<code>firstRow</code>.    * Closes existing Readers if any.    * @param firstRow    * @throws IOException    */
specifier|private
name|void
name|openScanner
parameter_list|(
specifier|final
name|byte
index|[]
name|firstRow
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|HFileScanner
argument_list|>
name|s
init|=
operator|new
name|ArrayList
argument_list|<
name|HFileScanner
argument_list|>
argument_list|(
name|this
operator|.
name|store
operator|.
name|getStorefiles
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Long
argument_list|,
name|StoreFile
argument_list|>
name|map
init|=
name|this
operator|.
name|store
operator|.
name|getStorefiles
argument_list|()
operator|.
name|descendingMap
argument_list|()
decl_stmt|;
for|for
control|(
name|StoreFile
name|f
range|:
name|map
operator|.
name|values
argument_list|()
control|)
block|{
name|s
operator|.
name|add
argument_list|(
name|f
operator|.
name|getReader
argument_list|()
operator|.
name|getScanner
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|scanners
operator|=
name|s
operator|.
name|toArray
argument_list|(
operator|new
name|HFileScanner
index|[]
block|{}
argument_list|)
expr_stmt|;
name|this
operator|.
name|keys
operator|=
operator|new
name|KeyValue
index|[
name|this
operator|.
name|scanners
operator|.
name|length
index|]
expr_stmt|;
comment|// Advance the readers to the first pos.
name|KeyValue
name|firstKey
init|=
operator|(
name|firstRow
operator|!=
literal|null
operator|&&
name|firstRow
operator|.
name|length
operator|>
literal|0
operator|)
condition|?
operator|new
name|KeyValue
argument_list|(
name|firstRow
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
else|:
literal|null
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
name|this
operator|.
name|scanners
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|firstKey
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|seekTo
argument_list|(
name|i
argument_list|,
name|firstKey
argument_list|)
condition|)
block|{
continue|continue;
block|}
block|}
while|while
condition|(
name|getNext
argument_list|(
name|i
argument_list|)
condition|)
block|{
if|if
condition|(
name|columnMatch
argument_list|(
name|i
argument_list|)
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
comment|/**    * For a particular column i, find all the matchers defined for the column.    * Compare the column family and column key using the matchers. The first one    * that matches returns true. If no matchers are successful, return false.    *     * @param i index into the keys array    * @return true if any of the matchers for the column match the column family    * and the column key.    * @throws IOException    */
name|boolean
name|columnMatch
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|columnMatch
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|)
return|;
block|}
comment|/**    * Get the next set of values for this scanner.    *     * @param key The key that matched    * @param results All the results for<code>key</code>    * @return true if a match was found    * @throws IOException    *     * @see org.apache.hadoop.hbase.regionserver.InternalScanner#next(org.apache.hadoop.hbase.HStoreKey, java.util.SortedMap)    */
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|scannerClosed
condition|)
block|{
return|return
literal|false
return|;
block|}
name|this
operator|.
name|lock
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
comment|// Find the next viable row label (and timestamp).
name|KeyValue
name|viable
init|=
name|getNextViableRow
argument_list|()
decl_stmt|;
if|if
condition|(
name|viable
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// Grab all the values that match this row/timestamp
name|boolean
name|addedItem
init|=
literal|false
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
name|keys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
comment|// Fetch the data
while|while
condition|(
operator|(
name|keys
index|[
name|i
index|]
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|this
operator|.
name|store
operator|.
name|comparator
operator|.
name|compareRows
argument_list|(
name|this
operator|.
name|keys
index|[
name|i
index|]
argument_list|,
name|viable
argument_list|)
operator|==
literal|0
operator|)
condition|)
block|{
comment|// If we are doing a wild card match or there are multiple matchers
comment|// per column, we need to scan all the older versions of this row
comment|// to pick up the rest of the family members
if|if
condition|(
operator|!
name|isWildcardScanner
argument_list|()
operator|&&
operator|!
name|isMultipleMatchScanner
argument_list|()
operator|&&
operator|(
name|keys
index|[
name|i
index|]
operator|.
name|getTimestamp
argument_list|()
operator|!=
name|viable
operator|.
name|getTimestamp
argument_list|()
operator|)
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|columnMatch
argument_list|(
name|i
argument_list|)
condition|)
block|{
comment|// We only want the first result for any specific family member
comment|// TODO: Do we have to keep a running list of column entries in
comment|// the results across all of the StoreScanner?  Like we do
comment|// doing getFull?
if|if
condition|(
operator|!
name|results
operator|.
name|contains
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|results
operator|.
name|add
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|addedItem
operator|=
literal|true
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|getNext
argument_list|(
name|i
argument_list|)
condition|)
block|{
name|closeSubScanner
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Advance the current scanner beyond the chosen row, to
comment|// a valid timestamp, so we're ready next time.
while|while
condition|(
operator|(
name|keys
index|[
name|i
index|]
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|(
name|this
operator|.
name|store
operator|.
name|comparator
operator|.
name|compareRows
argument_list|(
name|this
operator|.
name|keys
index|[
name|i
index|]
argument_list|,
name|viable
argument_list|)
operator|<=
literal|0
operator|)
operator|||
operator|(
name|keys
index|[
name|i
index|]
operator|.
name|getTimestamp
argument_list|()
operator|>
name|this
operator|.
name|timestamp
operator|)
operator|||
operator|!
name|columnMatch
argument_list|(
name|i
argument_list|)
operator|)
condition|)
block|{
name|getNext
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|addedItem
return|;
block|}
finally|finally
block|{
name|this
operator|.
name|lock
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|/*    * @return An instance of<code>ViableRow</code>    * @throws IOException    */
specifier|private
name|KeyValue
name|getNextViableRow
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Find the next viable row label (and timestamp).
name|KeyValue
name|viable
init|=
literal|null
decl_stmt|;
name|long
name|viableTimestamp
init|=
operator|-
literal|1
decl_stmt|;
name|long
name|ttl
init|=
name|store
operator|.
name|ttl
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
name|keys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
comment|// The first key that we find that matches may have a timestamp greater
comment|// than the one we're looking for. We have to advance to see if there
comment|// is an older version present, since timestamps are sorted descending
while|while
condition|(
name|keys
index|[
name|i
index|]
operator|!=
literal|null
operator|&&
name|keys
index|[
name|i
index|]
operator|.
name|getTimestamp
argument_list|()
operator|>
name|this
operator|.
name|timestamp
operator|&&
name|columnMatch
argument_list|(
name|i
argument_list|)
operator|&&
name|getNext
argument_list|(
name|i
argument_list|)
condition|)
block|{
if|if
condition|(
name|columnMatch
argument_list|(
name|i
argument_list|)
condition|)
block|{
break|break;
block|}
block|}
if|if
condition|(
operator|(
name|keys
index|[
name|i
index|]
operator|!=
literal|null
operator|)
comment|// If we get here and keys[i] is not null, we already know that the
comment|// column matches and the timestamp of the row is less than or equal
comment|// to this.timestamp, so we do not need to test that here
operator|&&
operator|(
operator|(
name|viable
operator|==
literal|null
operator|)
operator|||
operator|(
name|this
operator|.
name|store
operator|.
name|comparator
operator|.
name|compareRows
argument_list|(
name|this
operator|.
name|keys
index|[
name|i
index|]
argument_list|,
name|viable
argument_list|)
operator|<
literal|0
operator|)
operator|||
operator|(
operator|(
name|this
operator|.
name|store
operator|.
name|comparator
operator|.
name|compareRows
argument_list|(
name|this
operator|.
name|keys
index|[
name|i
index|]
argument_list|,
name|viable
argument_list|)
operator|==
literal|0
operator|)
operator|&&
operator|(
name|keys
index|[
name|i
index|]
operator|.
name|getTimestamp
argument_list|()
operator|>
name|viableTimestamp
operator|)
operator|)
operator|)
condition|)
block|{
if|if
condition|(
name|ttl
operator|==
name|HConstants
operator|.
name|FOREVER
operator|||
name|now
operator|<
name|keys
index|[
name|i
index|]
operator|.
name|getTimestamp
argument_list|()
operator|+
name|ttl
condition|)
block|{
name|viable
operator|=
name|keys
index|[
name|i
index|]
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"getNextViableRow :"
operator|+
name|keys
index|[
name|i
index|]
operator|+
literal|": expired, skipped"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|viable
return|;
block|}
comment|/*    * The user didn't want to start scanning at the first row. This method    * seeks to the requested row.    *    * @param i which iterator to advance    * @param firstRow seek to this row    * @return true if we found the first row and so the scanner is properly    * primed or true if the row was not found and this scanner is exhausted.    */
specifier|private
name|boolean
name|seekTo
parameter_list|(
name|int
name|i
parameter_list|,
specifier|final
name|KeyValue
name|firstKey
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|firstKey
operator|==
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|scanners
index|[
name|i
index|]
operator|.
name|seekTo
argument_list|()
condition|)
block|{
name|closeSubScanner
argument_list|(
name|i
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
else|else
block|{
comment|// TODO: sort columns and pass in column as part of key so we get closer.
if|if
condition|(
operator|!
name|Store
operator|.
name|getClosest
argument_list|(
name|this
operator|.
name|scanners
index|[
name|i
index|]
argument_list|,
name|firstKey
argument_list|)
condition|)
block|{
name|closeSubScanner
argument_list|(
name|i
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
name|this
operator|.
name|keys
index|[
name|i
index|]
operator|=
name|this
operator|.
name|scanners
index|[
name|i
index|]
operator|.
name|getKeyValue
argument_list|()
expr_stmt|;
return|return
name|isGoodKey
argument_list|(
name|this
operator|.
name|keys
index|[
name|i
index|]
argument_list|)
return|;
block|}
comment|/**    * Get the next value from the specified reader.    *     * @param i which reader to fetch next value from    * @return true if there is more data available    */
specifier|private
name|boolean
name|getNext
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|result
init|=
literal|false
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
operator|(
name|this
operator|.
name|scanners
index|[
name|i
index|]
operator|.
name|isSeeked
argument_list|()
operator|&&
operator|!
name|this
operator|.
name|scanners
index|[
name|i
index|]
operator|.
name|next
argument_list|()
operator|)
operator|||
operator|(
operator|!
name|this
operator|.
name|scanners
index|[
name|i
index|]
operator|.
name|isSeeked
argument_list|()
operator|&&
operator|!
name|this
operator|.
name|scanners
index|[
name|i
index|]
operator|.
name|seekTo
argument_list|()
operator|)
condition|)
block|{
name|closeSubScanner
argument_list|(
name|i
argument_list|)
expr_stmt|;
break|break;
block|}
name|this
operator|.
name|keys
index|[
name|i
index|]
operator|=
name|this
operator|.
name|scanners
index|[
name|i
index|]
operator|.
name|getKeyValue
argument_list|()
expr_stmt|;
if|if
condition|(
name|isGoodKey
argument_list|(
name|this
operator|.
name|keys
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|result
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
return|return
name|result
return|;
block|}
comment|/*    * @param kv    * @return True if good key candidate.    */
specifier|private
name|boolean
name|isGoodKey
parameter_list|(
specifier|final
name|KeyValue
name|kv
parameter_list|)
block|{
return|return
operator|!
name|Store
operator|.
name|isExpired
argument_list|(
name|kv
argument_list|,
name|this
operator|.
name|store
operator|.
name|ttl
argument_list|,
name|this
operator|.
name|now
argument_list|)
return|;
block|}
comment|/** Close down the indicated reader. */
specifier|private
name|void
name|closeSubScanner
parameter_list|(
name|int
name|i
parameter_list|)
block|{
name|this
operator|.
name|scanners
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|keys
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
comment|/** Shut it down! */
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|scannerClosed
condition|)
block|{
name|this
operator|.
name|store
operator|.
name|deleteChangedReaderObserver
argument_list|(
name|this
argument_list|)
expr_stmt|;
try|try
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|this
operator|.
name|scanners
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|closeSubScanner
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|this
operator|.
name|scannerClosed
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
comment|// Implementation of ChangedReadersObserver
specifier|public
name|void
name|updateReaders
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|lock
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
comment|// The keys are currently lined up at the next row to fetch.  Pass in
comment|// the current row as 'first' row and readers will be opened and cue'd
comment|// up so future call to next will start here.
name|KeyValue
name|viable
init|=
name|getNextViableRow
argument_list|()
decl_stmt|;
name|openScanner
argument_list|(
name|viable
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Replaced Scanner Readers at row "
operator|+
name|viable
operator|.
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|this
operator|.
name|lock
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

