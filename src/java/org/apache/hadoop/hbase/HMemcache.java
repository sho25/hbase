begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Iterator
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
name|SortedMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|atomic
operator|.
name|AtomicLong
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
name|Text
import|;
end_import

begin_comment
comment|/**  * The HMemcache holds in-memory modifications to the HRegion.  This is really a  * wrapper around a TreeMap that helps us when staging the Memcache out to disk.  */
end_comment

begin_class
specifier|public
class|class
name|HMemcache
block|{
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
comment|// Note that since these structures are always accessed with a lock held,
comment|// no additional synchronization is required.
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
name|memcache
init|=
operator|new
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|ArrayList
argument_list|<
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|history
init|=
operator|new
name|ArrayList
argument_list|<
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
name|snapshot
init|=
literal|null
decl_stmt|;
specifier|final
name|HLocking
name|lock
init|=
operator|new
name|HLocking
argument_list|()
decl_stmt|;
comment|/*    * Approximate size in bytes of the payload carried by this memcache.    * Does not consider deletes nor adding again on same key.    */
specifier|private
name|AtomicLong
name|size
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|/**    * Constructor    */
specifier|public
name|HMemcache
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/** represents the state of the memcache at a specified point in time */
specifier|static
class|class
name|Snapshot
block|{
specifier|final
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
name|memcacheSnapshot
decl_stmt|;
specifier|final
name|long
name|sequenceId
decl_stmt|;
name|Snapshot
parameter_list|(
specifier|final
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
name|memcache
parameter_list|,
specifier|final
name|Long
name|i
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|memcacheSnapshot
operator|=
name|memcache
expr_stmt|;
name|this
operator|.
name|sequenceId
operator|=
name|i
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Returns a snapshot of the current HMemcache with a known HLog     * sequence number at the same time.    *    * We need to prevent any writing to the cache during this time,    * so we obtain a write lock for the duration of the operation.    *     *<p>If this method returns non-null, client must call    * {@link #deleteSnapshot()} to clear 'snapshot-in-progress'    * state when finished with the returned {@link Snapshot}.    *     * @return frozen HMemcache TreeMap and HLog sequence number.    */
name|Snapshot
name|snapshotMemcacheForLog
parameter_list|(
name|HLog
name|log
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|lock
operator|.
name|obtainWriteLock
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
comment|// If no entries in memcache.
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
return|return
literal|null
return|;
block|}
name|Snapshot
name|retval
init|=
operator|new
name|Snapshot
argument_list|(
name|memcache
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
name|log
operator|.
name|startCacheFlush
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
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
name|byte
index|[]
argument_list|>
argument_list|()
expr_stmt|;
comment|// Reset size of this memcache.
name|this
operator|.
name|size
operator|.
name|set
argument_list|(
literal|0
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
name|lock
operator|.
name|releaseWriteLock
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Delete the snapshot, remove from history.    *    * Modifying the structure means we need to obtain a writelock.    * @throws IOException    */
specifier|public
name|void
name|deleteSnapshot
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|lock
operator|.
name|obtainWriteLock
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
for|for
control|(
name|Iterator
argument_list|<
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
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
name|byte
index|[]
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
block|}
finally|finally
block|{
name|this
operator|.
name|lock
operator|.
name|releaseWriteLock
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Store a value.      * Operation uses a write lock.    * @param row    * @param columns    * @param timestamp    */
specifier|public
name|void
name|add
parameter_list|(
specifier|final
name|Text
name|row
parameter_list|,
specifier|final
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|columns
parameter_list|,
specifier|final
name|long
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|lock
operator|.
name|obtainWriteLock
argument_list|()
expr_stmt|;
try|try
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|es
range|:
name|columns
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|(
name|row
argument_list|,
name|es
operator|.
name|getKey
argument_list|()
argument_list|,
name|timestamp
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|es
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|this
operator|.
name|size
operator|.
name|addAndGet
argument_list|(
name|key
operator|.
name|getSize
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|size
operator|.
name|addAndGet
argument_list|(
operator|(
operator|(
name|value
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|value
operator|.
name|length
operator|)
argument_list|)
expr_stmt|;
name|memcache
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|this
operator|.
name|lock
operator|.
name|releaseWriteLock
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * @return Approximate size in bytes of payload carried by this memcache.    * Does not take into consideration deletes nor adding again on same key.    */
specifier|public
name|long
name|getSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|size
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * Look back through all the backlog TreeMaps to find the target.    * @param key    * @param numVersions    * @return An array of byte arrays ordered by timestamp.    */
specifier|public
name|byte
index|[]
index|[]
name|get
parameter_list|(
specifier|final
name|HStoreKey
name|key
parameter_list|,
specifier|final
name|int
name|numVersions
parameter_list|)
block|{
name|this
operator|.
name|lock
operator|.
name|obtainReadLock
argument_list|()
expr_stmt|;
try|try
block|{
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
name|results
init|=
name|get
argument_list|(
name|memcache
argument_list|,
name|key
argument_list|,
name|numVersions
argument_list|)
decl_stmt|;
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
name|results
operator|.
name|addAll
argument_list|(
name|results
operator|.
name|size
argument_list|()
argument_list|,
name|get
argument_list|(
name|history
operator|.
name|get
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
argument_list|)
expr_stmt|;
block|}
return|return
operator|(
name|results
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|)
condition|?
literal|null
else|:
name|ImmutableBytesWritable
operator|.
name|toArray
argument_list|(
name|results
argument_list|)
return|;
block|}
finally|finally
block|{
name|this
operator|.
name|lock
operator|.
name|releaseReadLock
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Return all the available columns for the given key.  The key indicates a     * row and timestamp, but not a column name.    *    * The returned object should map column names to byte arrays (byte[]).    * @param key    * @return All columns for given key.    */
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
name|lock
operator|.
name|obtainReadLock
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
name|byte
index|[]
argument_list|>
name|cur
init|=
name|history
operator|.
name|get
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
name|lock
operator|.
name|releaseReadLock
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
name|byte
index|[]
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
name|byte
index|[]
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
name|Map
operator|.
name|Entry
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
name|es
range|:
name|tailMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|HStoreKey
name|itKey
init|=
name|es
operator|.
name|getKey
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
name|byte
index|[]
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
comment|/**    * Examine a single map for the desired key.    *    * We assume that all locking is done at a higher-level. No locking within     * this method.    *    * TODO - This is kinda slow.  We need a data structure that allows for     * proximity-searches, not just precise-matches.    *     * @param map    * @param key    * @param numVersions    * @return Ordered list of items found in passed<code>map</code>.  If no    * matching values, returns an empty list (does not return null).    */
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
name|get
parameter_list|(
specifier|final
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
name|map
parameter_list|,
specifier|final
name|HStoreKey
name|key
parameter_list|,
specifier|final
name|int
name|numVersions
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
comment|// TODO: If get is of a particular version -- numVersions == 1 -- we
comment|// should be able to avoid all of the tailmap creations and iterations
comment|// below.
name|HStoreKey
name|curKey
init|=
operator|new
name|HStoreKey
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|SortedMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
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
name|Map
operator|.
name|Entry
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
name|es
range|:
name|tailMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|HStoreKey
name|itKey
init|=
name|es
operator|.
name|getKey
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
if|if
condition|(
operator|!
name|isDeleted
argument_list|(
name|es
operator|.
name|getValue
argument_list|()
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
comment|/**    * Get<code>versions</code> keys matching the origin key's    * row/column/timestamp and those of an older vintage    * Default access so can be accessed out of {@link HRegionServer}.    * @param origin Where to start searching.    * @param versions How many versions to return. Pass    * {@link HConstants.ALL_VERSIONS} to retrieve all.    * @return Ordered list of<code>versions</code> keys going from newest back.    * @throws IOException    */
name|List
argument_list|<
name|HStoreKey
argument_list|>
name|getKeys
parameter_list|(
specifier|final
name|HStoreKey
name|origin
parameter_list|,
specifier|final
name|int
name|versions
parameter_list|)
block|{
name|this
operator|.
name|lock
operator|.
name|obtainReadLock
argument_list|()
expr_stmt|;
try|try
block|{
name|List
argument_list|<
name|HStoreKey
argument_list|>
name|results
init|=
name|getKeys
argument_list|(
name|this
operator|.
name|memcache
argument_list|,
name|origin
argument_list|,
name|versions
argument_list|)
decl_stmt|;
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
name|results
operator|.
name|addAll
argument_list|(
name|results
operator|.
name|size
argument_list|()
argument_list|,
name|getKeys
argument_list|(
name|history
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|origin
argument_list|,
name|versions
operator|==
name|HConstants
operator|.
name|ALL_VERSIONS
condition|?
name|versions
else|:
operator|(
name|versions
operator|-
name|results
operator|.
name|size
argument_list|()
operator|)
argument_list|)
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
name|lock
operator|.
name|releaseReadLock
argument_list|()
expr_stmt|;
block|}
block|}
comment|/*    * @param origin Where to start searching.    * @param versions How many versions to return. Pass    * {@link HConstants.ALL_VERSIONS} to retrieve all.    * @return List of all keys that are of the same row and column and of    * equal or older timestamp.  If no keys, returns an empty List. Does not    * return null.    */
specifier|private
name|List
argument_list|<
name|HStoreKey
argument_list|>
name|getKeys
parameter_list|(
specifier|final
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
name|map
parameter_list|,
specifier|final
name|HStoreKey
name|origin
parameter_list|,
specifier|final
name|int
name|versions
parameter_list|)
block|{
name|List
argument_list|<
name|HStoreKey
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|HStoreKey
argument_list|>
argument_list|()
decl_stmt|;
name|SortedMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
name|tailMap
init|=
name|map
operator|.
name|tailMap
argument_list|(
name|origin
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
name|es
range|:
name|tailMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|HStoreKey
name|key
init|=
name|es
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|key
operator|.
name|matchesRowCol
argument_list|(
name|origin
argument_list|)
condition|)
block|{
break|break;
block|}
if|if
condition|(
operator|!
name|isDeleted
argument_list|(
name|es
operator|.
name|getValue
argument_list|()
argument_list|)
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
if|if
condition|(
name|versions
operator|!=
name|HConstants
operator|.
name|ALL_VERSIONS
operator|&&
name|result
operator|.
name|size
argument_list|()
operator|>=
name|versions
condition|)
block|{
comment|// We have enough results.  Return.
break|break;
block|}
block|}
block|}
return|return
name|result
return|;
block|}
comment|/**    * @param key    * @return True if an entry and its content is {@link HGlobals.deleteBytes}.    * Use checking values in store. On occasion the memcache has the fact that    * the cell has been deleted.    */
name|boolean
name|isDeleted
parameter_list|(
specifier|final
name|HStoreKey
name|key
parameter_list|)
block|{
return|return
name|isDeleted
argument_list|(
name|this
operator|.
name|memcache
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * @param value    * @return True if an entry and its content is {@link HGlobals.deleteBytes}.    */
name|boolean
name|isDeleted
parameter_list|(
specifier|final
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
operator|(
name|value
operator|==
literal|null
operator|)
condition|?
literal|false
else|:
name|HGlobals
operator|.
name|deleteBytes
operator|.
name|compareTo
argument_list|(
name|value
argument_list|)
operator|==
literal|0
return|;
block|}
comment|/**    * Return a scanner over the keys in the HMemcache    */
name|HInternalScannerInterface
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
specifier|final
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
name|backingMaps
index|[]
decl_stmt|;
specifier|final
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
name|HMemcacheScanner
parameter_list|(
specifier|final
name|long
name|timestamp
parameter_list|,
specifier|final
name|Text
name|targetCols
index|[]
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
name|lock
operator|.
name|obtainReadLock
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
comment|// Note that since we iterate through the backing maps from 0 to n, we
comment|// need to put the memcache first, the newest history second, ..., etc.
name|backingMaps
index|[
literal|0
index|]
operator|=
name|memcache
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
operator|>
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|backingMaps
index|[
name|i
operator|+
literal|1
index|]
operator|=
name|history
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
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
name|byte
index|[
name|backingMaps
operator|.
name|length
index|]
index|[]
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
name|int
name|i
init|=
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
name|keyIterators
index|[
name|i
index|]
operator|=
name|firstRow
operator|.
name|getLength
argument_list|()
operator|!=
literal|0
condition|?
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
else|:
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
name|RuntimeException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"error initializing HMemcache scanner: "
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|close
argument_list|()
expr_stmt|;
name|IOException
name|e
init|=
operator|new
name|IOException
argument_list|(
literal|"error initializing HMemcache scanner"
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
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"error initializing HMemcache scanner: "
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|close
argument_list|()
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
block|}
comment|/**      * The user didn't want to start scanning at the first row. This method      * seeks to the requested row.      *      * @param i which iterator to advance      * @param firstRow seek to this row      * @return true if this is the first row      */
annotation|@
name|Override
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
name|firstRow
operator|.
name|getLength
argument_list|()
operator|==
literal|0
operator|||
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
name|firstRow
argument_list|)
operator|>=
literal|0
return|;
block|}
comment|/**      * Get the next value from the specified iterator.      *       * @param i Which iterator to fetch next value from      * @return true if there is more data available      */
annotation|@
name|Override
name|boolean
name|getNext
parameter_list|(
name|int
name|i
parameter_list|)
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
break|break;
block|}
comment|// Check key is< than passed timestamp for this scanner.
name|HStoreKey
name|hsk
init|=
name|keyIterators
index|[
name|i
index|]
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|hsk
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"Unexpected null key"
argument_list|)
throw|;
block|}
if|if
condition|(
name|hsk
operator|.
name|getTimestamp
argument_list|()
operator|<=
name|this
operator|.
name|timestamp
condition|)
block|{
name|this
operator|.
name|keys
index|[
name|i
index|]
operator|=
name|hsk
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
comment|/** Shut down an individual map iterator. */
annotation|@
name|Override
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
name|lock
operator|.
name|releaseReadLock
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

