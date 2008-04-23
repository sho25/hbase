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
name|SortedMap
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
name|HStoreKey
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
name|ImmutableBytesWritable
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
name|io
operator|.
name|MapFile
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
name|io
operator|.
name|Text
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
name|HStoreKey
name|keys
index|[]
decl_stmt|;
comment|// Values that correspond to those keys
specifier|private
name|byte
index|[]
index|[]
name|vals
decl_stmt|;
comment|// Readers we go against.
specifier|private
name|MapFile
operator|.
name|Reader
index|[]
name|readers
decl_stmt|;
comment|// Store this scanner came out of.
specifier|private
specifier|final
name|HStore
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
specifier|public
name|StoreFileScanner
parameter_list|(
specifier|final
name|HStore
name|store
parameter_list|,
specifier|final
name|long
name|timestamp
parameter_list|,
specifier|final
name|Text
index|[]
name|targetCols
parameter_list|,
specifier|final
name|Text
name|firstRow
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|timestamp
argument_list|,
name|targetCols
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
name|openReaders
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
comment|/*    * Go open new Reader iterators and cue them at<code>firstRow</code>.    * Closes existing Readers if any.    * @param firstRow    * @throws IOException    */
specifier|private
name|void
name|openReaders
parameter_list|(
specifier|final
name|Text
name|firstRow
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|readers
operator|!=
literal|null
condition|)
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
name|readers
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|this
operator|.
name|readers
index|[
name|i
index|]
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|// Open our own copies of the Readers here inside in the scanner.
name|this
operator|.
name|readers
operator|=
operator|new
name|MapFile
operator|.
name|Reader
index|[
name|this
operator|.
name|store
operator|.
name|getStorefiles
argument_list|()
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
comment|// Most recent map file should be first
name|int
name|i
init|=
name|readers
operator|.
name|length
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|HStoreFile
name|curHSF
range|:
name|store
operator|.
name|getStorefiles
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|readers
index|[
name|i
operator|--
index|]
operator|=
name|curHSF
operator|.
name|getReader
argument_list|(
name|store
operator|.
name|fs
argument_list|,
name|store
operator|.
name|bloomFilter
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|keys
operator|=
operator|new
name|HStoreKey
index|[
name|readers
operator|.
name|length
index|]
expr_stmt|;
name|this
operator|.
name|vals
operator|=
operator|new
name|byte
index|[
name|readers
operator|.
name|length
index|]
index|[]
expr_stmt|;
comment|// Advance the readers to the first pos.
for|for
control|(
name|i
operator|=
literal|0
init|;
name|i
operator|<
name|readers
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|keys
index|[
name|i
index|]
operator|=
operator|new
name|HStoreKey
argument_list|()
expr_stmt|;
if|if
condition|(
name|firstRow
operator|.
name|getLength
argument_list|()
operator|!=
literal|0
condition|)
block|{
if|if
condition|(
name|findFirstRow
argument_list|(
name|i
argument_list|,
name|firstRow
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
operator|.
name|getColumn
argument_list|()
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
name|HStoreKey
name|key
parameter_list|,
name|SortedMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
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
name|ViableRow
name|viableRow
init|=
name|getNextViableRow
argument_list|()
decl_stmt|;
comment|// Grab all the values that match this row/timestamp
name|boolean
name|insertedItem
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|viableRow
operator|.
name|getRow
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|key
operator|.
name|setRow
argument_list|(
name|viableRow
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|key
operator|.
name|setVersion
argument_list|(
name|viableRow
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|key
operator|.
name|setColumn
argument_list|(
operator|new
name|Text
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
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
name|keys
index|[
name|i
index|]
operator|.
name|getRow
argument_list|()
operator|.
name|compareTo
argument_list|(
name|viableRow
operator|.
name|getRow
argument_list|()
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
name|viableRow
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
if|if
condition|(
operator|!
name|results
operator|.
name|containsKey
argument_list|(
name|keys
index|[
name|i
index|]
operator|.
name|getColumn
argument_list|()
argument_list|)
condition|)
block|{
name|results
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
name|keys
index|[
name|i
index|]
operator|.
name|getColumn
argument_list|()
argument_list|)
argument_list|,
name|vals
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|insertedItem
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
name|keys
index|[
name|i
index|]
operator|.
name|getRow
argument_list|()
operator|.
name|compareTo
argument_list|(
name|viableRow
operator|.
name|getRow
argument_list|()
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
operator|(
operator|!
name|columnMatch
argument_list|(
name|i
argument_list|)
operator|)
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
block|}
return|return
name|insertedItem
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
comment|// Data stucture to hold next, viable row (and timestamp).
class|class
name|ViableRow
block|{
specifier|private
specifier|final
name|Text
name|row
decl_stmt|;
specifier|private
specifier|final
name|long
name|ts
decl_stmt|;
name|ViableRow
parameter_list|(
specifier|final
name|Text
name|r
parameter_list|,
specifier|final
name|long
name|t
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
name|r
expr_stmt|;
name|this
operator|.
name|ts
operator|=
name|t
expr_stmt|;
block|}
specifier|public
name|Text
name|getRow
parameter_list|()
block|{
return|return
name|this
operator|.
name|row
return|;
block|}
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|this
operator|.
name|ts
return|;
block|}
block|}
comment|/*    * @return An instance of<code>ViableRow</code>    * @throws IOException    */
specifier|private
name|ViableRow
name|getNextViableRow
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Find the next viable row label (and timestamp).
name|Text
name|viableRow
init|=
literal|null
decl_stmt|;
name|long
name|viableTimestamp
init|=
operator|-
literal|1
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
operator|&&
operator|(
name|columnMatch
argument_list|(
name|i
argument_list|)
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
operator|<=
name|this
operator|.
name|timestamp
operator|)
operator|&&
operator|(
operator|(
name|viableRow
operator|==
literal|null
operator|)
operator|||
operator|(
name|keys
index|[
name|i
index|]
operator|.
name|getRow
argument_list|()
operator|.
name|compareTo
argument_list|(
name|viableRow
argument_list|)
operator|<
literal|0
operator|)
operator|||
operator|(
operator|(
name|keys
index|[
name|i
index|]
operator|.
name|getRow
argument_list|()
operator|.
name|compareTo
argument_list|(
name|viableRow
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
name|viableRow
operator|=
operator|new
name|Text
argument_list|(
name|keys
index|[
name|i
index|]
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|viableTimestamp
operator|=
name|keys
index|[
name|i
index|]
operator|.
name|getTimestamp
argument_list|()
expr_stmt|;
block|}
block|}
return|return
operator|new
name|ViableRow
argument_list|(
name|viableRow
argument_list|,
name|viableTimestamp
argument_list|)
return|;
block|}
comment|/**    * The user didn't want to start scanning at the first row. This method    * seeks to the requested row.    *    * @param i which iterator to advance    * @param firstRow seek to this row    * @return true if this is the first row or if the row was not found    */
name|boolean
name|findFirstRow
parameter_list|(
name|int
name|i
parameter_list|,
name|Text
name|firstRow
parameter_list|)
throws|throws
name|IOException
block|{
name|ImmutableBytesWritable
name|ibw
init|=
operator|new
name|ImmutableBytesWritable
argument_list|()
decl_stmt|;
name|HStoreKey
name|firstKey
init|=
operator|(
name|HStoreKey
operator|)
name|readers
index|[
name|i
index|]
operator|.
name|getClosest
argument_list|(
operator|new
name|HStoreKey
argument_list|(
name|firstRow
argument_list|)
argument_list|,
name|ibw
argument_list|)
decl_stmt|;
if|if
condition|(
name|firstKey
operator|==
literal|null
condition|)
block|{
comment|// Didn't find it. Close the scanner and return TRUE
name|closeSubScanner
argument_list|(
name|i
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
name|this
operator|.
name|vals
index|[
name|i
index|]
operator|=
name|ibw
operator|.
name|get
argument_list|()
expr_stmt|;
name|keys
index|[
name|i
index|]
operator|.
name|setRow
argument_list|(
name|firstKey
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|keys
index|[
name|i
index|]
operator|.
name|setColumn
argument_list|(
name|firstKey
operator|.
name|getColumn
argument_list|()
argument_list|)
expr_stmt|;
name|keys
index|[
name|i
index|]
operator|.
name|setVersion
argument_list|(
name|firstKey
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|columnMatch
argument_list|(
name|i
argument_list|)
return|;
block|}
comment|/**    * Get the next value from the specified reader.    *     * @param i which reader to fetch next value from    * @return true if there is more data available    */
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
name|ImmutableBytesWritable
name|ibw
init|=
operator|new
name|ImmutableBytesWritable
argument_list|()
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
operator|!
name|readers
index|[
name|i
index|]
operator|.
name|next
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|,
name|ibw
argument_list|)
condition|)
block|{
name|closeSubScanner
argument_list|(
name|i
argument_list|)
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|keys
index|[
name|i
index|]
operator|.
name|getTimestamp
argument_list|()
operator|<=
name|this
operator|.
name|timestamp
condition|)
block|{
name|vals
index|[
name|i
index|]
operator|=
name|ibw
operator|.
name|get
argument_list|()
expr_stmt|;
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
comment|/** Close down the indicated reader. */
name|void
name|closeSubScanner
parameter_list|(
name|int
name|i
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|readers
index|[
name|i
index|]
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|readers
index|[
name|i
index|]
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|store
operator|.
name|storeName
operator|+
literal|" closing sub-scanner"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|readers
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
name|keys
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
name|vals
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
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
name|readers
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|readers
index|[
name|i
index|]
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|readers
index|[
name|i
index|]
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|store
operator|.
name|storeName
operator|+
literal|" closing scanner"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
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
name|ViableRow
name|viableRow
init|=
name|getNextViableRow
argument_list|()
decl_stmt|;
name|openReaders
argument_list|(
name|viableRow
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
name|viableRow
operator|.
name|getRow
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

