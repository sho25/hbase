begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2006 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
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
name|ReadWriteLock
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

begin_comment
comment|/*******************************************************************************  * The HMemcache holds in-memory modifications to the HRegion.  This is really a  * wrapper around a TreeMap that helps us when staging the Memcache out to disk.  ******************************************************************************/
end_comment

begin_class
specifier|public
class|class
name|HMemcache
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HMemcache
operator|.
name|class
argument_list|)
decl_stmt|;
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|BytesWritable
argument_list|>
name|memcache
init|=
operator|new
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|()
decl_stmt|;
name|Vector
argument_list|<
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|>
name|history
init|=
operator|new
name|Vector
argument_list|<
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|BytesWritable
argument_list|>
name|snapshot
init|=
literal|null
decl_stmt|;
name|ReadWriteLock
name|locker
init|=
operator|new
name|ReentrantReadWriteLock
argument_list|()
decl_stmt|;
specifier|public
name|HMemcache
parameter_list|()
block|{   }
specifier|public
specifier|static
class|class
name|Snapshot
block|{
specifier|public
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|BytesWritable
argument_list|>
name|memcacheSnapshot
init|=
literal|null
decl_stmt|;
specifier|public
name|long
name|sequenceId
init|=
literal|0
decl_stmt|;
specifier|public
name|Snapshot
parameter_list|()
block|{     }
block|}
comment|/**    * Returns a snapshot of the current HMemcache with a known HLog     * sequence number at the same time.    *    * We need to prevent any writing to the cache during this time,    * so we obtain a write lock for the duration of the operation.    *     *<p>If this method returns non-null, client must call    * {@link #deleteSnapshot()} to clear 'snapshot-in-progress'    * state when finished with the returned {@link Snapshot}.    *     * @return frozen HMemcache TreeMap and HLog sequence number.    */
specifier|public
name|Snapshot
name|snapshotMemcacheForLog
parameter_list|(
name|HLog
name|log
parameter_list|)
throws|throws
name|IOException
block|{
name|Snapshot
name|retval
init|=
operator|new
name|Snapshot
argument_list|()
decl_stmt|;
name|this
operator|.
name|locker
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
name|snapshot
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Snapshot in progress!"
argument_list|)
throw|;
block|}
if|if
condition|(
name|memcache
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"memcache empty. Skipping snapshot"
argument_list|)
expr_stmt|;
return|return
name|retval
return|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"starting memcache snapshot"
argument_list|)
expr_stmt|;
name|retval
operator|.
name|memcacheSnapshot
operator|=
name|memcache
expr_stmt|;
name|this
operator|.
name|snapshot
operator|=
name|memcache
expr_stmt|;
name|history
operator|.
name|add
argument_list|(
name|memcache
argument_list|)
expr_stmt|;
name|memcache
operator|=
operator|new
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|()
expr_stmt|;
name|retval
operator|.
name|sequenceId
operator|=
name|log
operator|.
name|startCacheFlush
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"memcache snapshot complete"
argument_list|)
expr_stmt|;
return|return
name|retval
return|;
block|}
finally|finally
block|{
name|this
operator|.
name|locker
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Delete the snapshot, remove from history.    *    * Modifying the structure means we need to obtain a writelock.    */
specifier|public
name|void
name|deleteSnapshot
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|locker
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
name|snapshot
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Snapshot not present!"
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"deleting snapshot"
argument_list|)
expr_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|>
name|it
init|=
name|history
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|BytesWritable
argument_list|>
name|cur
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|snapshot
operator|==
name|cur
condition|)
block|{
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
name|this
operator|.
name|snapshot
operator|=
literal|null
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"snapshot deleted"
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|this
operator|.
name|locker
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Store a value.      *    * Operation uses a write lock.    */
specifier|public
name|void
name|add
parameter_list|(
name|Text
name|row
parameter_list|,
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|columns
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|locker
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
for|for
control|(
name|Iterator
argument_list|<
name|Text
argument_list|>
name|it
init|=
name|columns
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Text
name|column
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|byte
index|[]
name|val
init|=
name|columns
operator|.
name|get
argument_list|(
name|column
argument_list|)
decl_stmt|;
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|(
name|row
argument_list|,
name|column
argument_list|,
name|timestamp
argument_list|)
decl_stmt|;
name|memcache
operator|.
name|put
argument_list|(
name|key
argument_list|,
operator|new
name|BytesWritable
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|this
operator|.
name|locker
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Look back through all the backlog TreeMaps to find the target.    *    * We only need a readlock here.    */
specifier|public
name|byte
index|[]
index|[]
name|get
parameter_list|(
name|HStoreKey
name|key
parameter_list|,
name|int
name|numVersions
parameter_list|)
block|{
name|Vector
argument_list|<
name|byte
index|[]
argument_list|>
name|results
init|=
operator|new
name|Vector
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|this
operator|.
name|locker
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|Vector
argument_list|<
name|byte
index|[]
argument_list|>
name|result
init|=
name|get
argument_list|(
name|memcache
argument_list|,
name|key
argument_list|,
name|numVersions
operator|-
name|results
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|results
operator|.
name|addAll
argument_list|(
literal|0
argument_list|,
name|result
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
name|history
operator|.
name|size
argument_list|()
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|--
control|)
block|{
if|if
condition|(
name|numVersions
operator|>
literal|0
operator|&&
name|results
operator|.
name|size
argument_list|()
operator|>=
name|numVersions
condition|)
block|{
break|break;
block|}
name|result
operator|=
name|get
argument_list|(
name|history
operator|.
name|elementAt
argument_list|(
name|i
argument_list|)
argument_list|,
name|key
argument_list|,
name|numVersions
operator|-
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|results
operator|.
name|addAll
argument_list|(
name|results
operator|.
name|size
argument_list|()
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|results
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
return|return
operator|(
name|byte
index|[]
index|[]
operator|)
name|results
operator|.
name|toArray
argument_list|(
operator|new
name|byte
index|[
name|results
operator|.
name|size
argument_list|()
index|]
index|[]
argument_list|)
return|;
block|}
block|}
finally|finally
block|{
name|this
operator|.
name|locker
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Return all the available columns for the given key.  The key indicates a     * row and timestamp, but not a column name.    *    * The returned object should map column names to byte arrays (byte[]).    */
specifier|public
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|getFull
parameter_list|(
name|HStoreKey
name|key
parameter_list|)
throws|throws
name|IOException
block|{
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|results
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|this
operator|.
name|locker
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|internalGetFull
argument_list|(
name|memcache
argument_list|,
name|key
argument_list|,
name|results
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
name|history
operator|.
name|size
argument_list|()
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|BytesWritable
argument_list|>
name|cur
init|=
name|history
operator|.
name|elementAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|internalGetFull
argument_list|(
name|cur
argument_list|,
name|key
argument_list|,
name|results
argument_list|)
expr_stmt|;
block|}
return|return
name|results
return|;
block|}
finally|finally
block|{
name|this
operator|.
name|locker
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
name|void
name|internalGetFull
parameter_list|(
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|BytesWritable
argument_list|>
name|map
parameter_list|,
name|HStoreKey
name|key
parameter_list|,
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|results
parameter_list|)
block|{
name|SortedMap
argument_list|<
name|HStoreKey
argument_list|,
name|BytesWritable
argument_list|>
name|tailMap
init|=
name|map
operator|.
name|tailMap
argument_list|(
name|key
argument_list|)
decl_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|HStoreKey
argument_list|>
name|it
init|=
name|tailMap
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|HStoreKey
name|itKey
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|Text
name|itCol
init|=
name|itKey
operator|.
name|getColumn
argument_list|()
decl_stmt|;
if|if
condition|(
name|results
operator|.
name|get
argument_list|(
name|itCol
argument_list|)
operator|==
literal|null
operator|&&
name|key
operator|.
name|matchesWithoutColumn
argument_list|(
name|itKey
argument_list|)
condition|)
block|{
name|BytesWritable
name|val
init|=
name|tailMap
operator|.
name|get
argument_list|(
name|itKey
argument_list|)
decl_stmt|;
name|results
operator|.
name|put
argument_list|(
name|itCol
argument_list|,
name|val
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|key
operator|.
name|getRow
argument_list|()
operator|.
name|compareTo
argument_list|(
name|itKey
operator|.
name|getRow
argument_list|()
argument_list|)
operator|>
literal|0
condition|)
block|{
break|break;
block|}
block|}
block|}
comment|/**    * Examine a single map for the desired key.    *    * We assume that all locking is done at a higher-level. No locking within     * this method.    *    * TODO - This is kinda slow.  We need a data structure that allows for     * proximity-searches, not just precise-matches.    */
name|Vector
argument_list|<
name|byte
index|[]
argument_list|>
name|get
parameter_list|(
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|BytesWritable
argument_list|>
name|map
parameter_list|,
name|HStoreKey
name|key
parameter_list|,
name|int
name|numVersions
parameter_list|)
block|{
name|Vector
argument_list|<
name|byte
index|[]
argument_list|>
name|result
init|=
operator|new
name|Vector
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|HStoreKey
name|curKey
init|=
operator|new
name|HStoreKey
argument_list|(
name|key
operator|.
name|getRow
argument_list|()
argument_list|,
name|key
operator|.
name|getColumn
argument_list|()
argument_list|,
name|key
operator|.
name|getTimestamp
argument_list|()
argument_list|)
decl_stmt|;
name|SortedMap
argument_list|<
name|HStoreKey
argument_list|,
name|BytesWritable
argument_list|>
name|tailMap
init|=
name|map
operator|.
name|tailMap
argument_list|(
name|curKey
argument_list|)
decl_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|HStoreKey
argument_list|>
name|it
init|=
name|tailMap
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|HStoreKey
name|itKey
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|itKey
operator|.
name|matchesRowCol
argument_list|(
name|curKey
argument_list|)
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|tailMap
operator|.
name|get
argument_list|(
name|itKey
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|curKey
operator|.
name|setVersion
argument_list|(
name|itKey
operator|.
name|getTimestamp
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|numVersions
operator|>
literal|0
operator|&&
name|result
operator|.
name|size
argument_list|()
operator|>=
name|numVersions
condition|)
block|{
break|break;
block|}
block|}
return|return
name|result
return|;
block|}
comment|/**    * Return a scanner over the keys in the HMemcache    */
specifier|public
name|HScannerInterface
name|getScanner
parameter_list|(
name|long
name|timestamp
parameter_list|,
name|Text
name|targetCols
index|[]
parameter_list|,
name|Text
name|firstRow
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|HMemcacheScanner
argument_list|(
name|timestamp
argument_list|,
name|targetCols
argument_list|,
name|firstRow
argument_list|)
return|;
block|}
comment|//////////////////////////////////////////////////////////////////////////////
comment|// HMemcacheScanner implements the HScannerInterface.
comment|// It lets the caller scan the contents of the Memcache.
comment|//////////////////////////////////////////////////////////////////////////////
class|class
name|HMemcacheScanner
extends|extends
name|HAbstractScanner
block|{
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|BytesWritable
argument_list|>
name|backingMaps
index|[]
decl_stmt|;
name|Iterator
argument_list|<
name|HStoreKey
argument_list|>
name|keyIterators
index|[]
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|HMemcacheScanner
parameter_list|(
name|long
name|timestamp
parameter_list|,
name|Text
name|targetCols
index|[]
parameter_list|,
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
name|locker
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|this
operator|.
name|backingMaps
operator|=
operator|new
name|TreeMap
index|[
name|history
operator|.
name|size
argument_list|()
operator|+
literal|1
index|]
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|>
name|it
init|=
name|history
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|backingMaps
index|[
name|i
operator|++
index|]
operator|=
name|it
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
name|backingMaps
index|[
name|backingMaps
operator|.
name|length
operator|-
literal|1
index|]
operator|=
name|memcache
expr_stmt|;
name|this
operator|.
name|keyIterators
operator|=
operator|new
name|Iterator
index|[
name|backingMaps
operator|.
name|length
index|]
expr_stmt|;
name|this
operator|.
name|keys
operator|=
operator|new
name|HStoreKey
index|[
name|backingMaps
operator|.
name|length
index|]
expr_stmt|;
name|this
operator|.
name|vals
operator|=
operator|new
name|BytesWritable
index|[
name|backingMaps
operator|.
name|length
index|]
expr_stmt|;
comment|// Generate list of iterators
name|HStoreKey
name|firstKey
init|=
operator|new
name|HStoreKey
argument_list|(
name|firstRow
argument_list|)
decl_stmt|;
for|for
control|(
name|i
operator|=
literal|0
init|;
name|i
operator|<
name|backingMaps
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
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
name|keyIterators
index|[
name|i
index|]
operator|=
name|backingMaps
index|[
name|i
index|]
operator|.
name|tailMap
argument_list|(
name|firstKey
argument_list|)
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|keyIterators
index|[
name|i
index|]
operator|=
name|backingMaps
index|[
name|i
index|]
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
expr_stmt|;
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
operator|!
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
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**      * The user didn't want to start scanning at the first row. This method      * seeks to the requested row.      *      * @param i         - which iterator to advance      * @param firstRow  - seek to this row      * @return          - true if this is the first row      */
name|boolean
name|findFirstRow
parameter_list|(
name|int
name|i
parameter_list|,
name|Text
name|firstRow
parameter_list|)
block|{
return|return
operator|(
operator|(
name|firstRow
operator|.
name|getLength
argument_list|()
operator|==
literal|0
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
name|equals
argument_list|(
name|firstRow
argument_list|)
operator|)
operator|)
return|;
block|}
comment|/**      * Get the next value from the specified iterater.      *       * @param i - which iterator to fetch next value from      * @return - true if there is more data available      */
name|boolean
name|getNext
parameter_list|(
name|int
name|i
parameter_list|)
block|{
if|if
condition|(
operator|!
name|keyIterators
index|[
name|i
index|]
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|closeSubScanner
argument_list|(
name|i
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|this
operator|.
name|keys
index|[
name|i
index|]
operator|=
name|keyIterators
index|[
name|i
index|]
operator|.
name|next
argument_list|()
expr_stmt|;
name|this
operator|.
name|vals
index|[
name|i
index|]
operator|=
name|backingMaps
index|[
name|i
index|]
operator|.
name|get
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
comment|/** Shut down an individual map iterator. */
name|void
name|closeSubScanner
parameter_list|(
name|int
name|i
parameter_list|)
block|{
name|keyIterators
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
name|backingMaps
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
comment|/** Shut down map iterators, and release the lock */
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|scannerClosed
condition|)
block|{
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
name|keyIterators
index|[
name|i
index|]
operator|!=
literal|null
condition|)
block|{
name|closeSubScanner
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|locker
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
name|scannerClosed
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

