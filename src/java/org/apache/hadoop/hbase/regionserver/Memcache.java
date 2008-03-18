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
name|TreeMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|List
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|io
operator|.
name|Text
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

begin_comment
comment|/**  * The Memcache holds in-memory modifications to the HRegion.  This is really a  * wrapper around a TreeMap that helps us when staging the Memcache out to disk.  */
end_comment

begin_class
class|class
name|Memcache
block|{
comment|// Note that since these structures are always accessed with a lock held,
comment|// no additional synchronization is required.
annotation|@
name|SuppressWarnings
argument_list|(
literal|"hiding"
argument_list|)
specifier|private
specifier|final
name|SortedMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
name|memcache
init|=
name|Collections
operator|.
name|synchronizedSortedMap
argument_list|(
operator|new
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
specifier|volatile
name|SortedMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
name|snapshot
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"hiding"
argument_list|)
specifier|private
specifier|final
name|ReentrantReadWriteLock
name|lock
init|=
operator|new
name|ReentrantReadWriteLock
argument_list|()
decl_stmt|;
comment|/**    * Constructor    */
specifier|public
name|Memcache
parameter_list|()
block|{
name|snapshot
operator|=
name|Collections
operator|.
name|synchronizedSortedMap
argument_list|(
operator|new
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Creates a snapshot of the current Memcache    */
name|void
name|snapshot
parameter_list|()
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
synchronized|synchronized
init|(
name|memcache
init|)
block|{
if|if
condition|(
name|memcache
operator|.
name|size
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|snapshot
operator|.
name|putAll
argument_list|(
name|memcache
argument_list|)
expr_stmt|;
name|memcache
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
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
comment|/**    * @return memcache snapshot    */
name|SortedMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
name|getSnapshot
parameter_list|()
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
name|SortedMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
name|currentSnapshot
init|=
name|snapshot
decl_stmt|;
name|snapshot
operator|=
name|Collections
operator|.
name|synchronizedSortedMap
argument_list|(
operator|new
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|currentSnapshot
return|;
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
comment|/**    * Store a value.      * @param key    * @param value    */
name|void
name|add
parameter_list|(
specifier|final
name|HStoreKey
name|key
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|)
block|{
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
comment|/**    * Look back through all the backlog TreeMaps to find the target.    * @param key    * @param numVersions    * @return An array of byte arrays ordered by timestamp.    */
name|List
argument_list|<
name|Cell
argument_list|>
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
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|results
decl_stmt|;
synchronized|synchronized
init|(
name|memcache
init|)
block|{
name|results
operator|=
name|internalGet
argument_list|(
name|memcache
argument_list|,
name|key
argument_list|,
name|numVersions
argument_list|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|snapshot
init|)
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
name|internalGet
argument_list|(
name|snapshot
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
name|results
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
comment|/**    * Return all the available columns for the given key.  The key indicates a     * row and timestamp, but not a column name.    *    * The returned object should map column names to byte arrays (byte[]).    * @param key    * @param results    */
name|void
name|getFull
parameter_list|(
name|HStoreKey
name|key
parameter_list|,
name|Set
argument_list|<
name|Text
argument_list|>
name|columns
parameter_list|,
name|Map
argument_list|<
name|Text
argument_list|,
name|Long
argument_list|>
name|deletes
parameter_list|,
name|SortedMap
argument_list|<
name|Text
argument_list|,
name|Cell
argument_list|>
name|results
parameter_list|)
block|{
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
synchronized|synchronized
init|(
name|memcache
init|)
block|{
name|internalGetFull
argument_list|(
name|memcache
argument_list|,
name|key
argument_list|,
name|columns
argument_list|,
name|deletes
argument_list|,
name|results
argument_list|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|snapshot
init|)
block|{
name|internalGetFull
argument_list|(
name|snapshot
argument_list|,
name|key
argument_list|,
name|columns
argument_list|,
name|deletes
argument_list|,
name|results
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
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|internalGetFull
parameter_list|(
name|SortedMap
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
name|Set
argument_list|<
name|Text
argument_list|>
name|columns
parameter_list|,
name|Map
argument_list|<
name|Text
argument_list|,
name|Long
argument_list|>
name|deletes
parameter_list|,
name|SortedMap
argument_list|<
name|Text
argument_list|,
name|Cell
argument_list|>
name|results
parameter_list|)
block|{
if|if
condition|(
name|map
operator|.
name|isEmpty
argument_list|()
operator|||
name|key
operator|==
literal|null
condition|)
block|{
return|return;
block|}
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
if|if
condition|(
name|columns
operator|==
literal|null
operator|||
name|columns
operator|.
name|contains
argument_list|(
name|itKey
operator|.
name|getColumn
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|HLogEdit
operator|.
name|isDeleted
argument_list|(
name|val
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|deletes
operator|.
name|containsKey
argument_list|(
name|itCol
argument_list|)
operator|||
name|deletes
operator|.
name|get
argument_list|(
name|itCol
argument_list|)
operator|.
name|longValue
argument_list|()
operator|<
name|itKey
operator|.
name|getTimestamp
argument_list|()
condition|)
block|{
name|deletes
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
name|itCol
argument_list|)
argument_list|,
name|itKey
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
operator|(
name|deletes
operator|.
name|containsKey
argument_list|(
name|itCol
argument_list|)
operator|&&
name|deletes
operator|.
name|get
argument_list|(
name|itCol
argument_list|)
operator|.
name|longValue
argument_list|()
operator|>=
name|itKey
operator|.
name|getTimestamp
argument_list|()
operator|)
condition|)
block|{
name|results
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
name|itCol
argument_list|)
argument_list|,
operator|new
name|Cell
argument_list|(
name|val
argument_list|,
name|itKey
operator|.
name|getTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
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
operator|<
literal|0
condition|)
block|{
break|break;
block|}
block|}
block|}
comment|/**    * @param row    * @param timestamp    * @return the key that matches<i>row</i> exactly, or the one that    * immediately preceeds it.    */
specifier|public
name|Text
name|getRowKeyAtOrBefore
parameter_list|(
specifier|final
name|Text
name|row
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
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
name|Text
name|key_memcache
init|=
literal|null
decl_stmt|;
name|Text
name|key_snapshot
init|=
literal|null
decl_stmt|;
try|try
block|{
synchronized|synchronized
init|(
name|memcache
init|)
block|{
name|key_memcache
operator|=
name|internalGetRowKeyAtOrBefore
argument_list|(
name|memcache
argument_list|,
name|row
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|snapshot
init|)
block|{
name|key_snapshot
operator|=
name|internalGetRowKeyAtOrBefore
argument_list|(
name|snapshot
argument_list|,
name|row
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|key_memcache
operator|==
literal|null
operator|&&
name|key_snapshot
operator|==
literal|null
condition|)
block|{
comment|// didn't find any candidates, return null
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|key_memcache
operator|==
literal|null
operator|&&
name|key_snapshot
operator|!=
literal|null
condition|)
block|{
return|return
name|key_snapshot
return|;
block|}
elseif|else
if|if
condition|(
name|key_memcache
operator|!=
literal|null
operator|&&
name|key_snapshot
operator|==
literal|null
condition|)
block|{
return|return
name|key_memcache
return|;
block|}
elseif|else
if|if
condition|(
operator|(
name|key_memcache
operator|!=
literal|null
operator|&&
name|key_memcache
operator|.
name|equals
argument_list|(
name|row
argument_list|)
operator|)
operator|||
operator|(
name|key_snapshot
operator|!=
literal|null
operator|&&
name|key_snapshot
operator|.
name|equals
argument_list|(
name|row
argument_list|)
operator|)
condition|)
block|{
comment|// if either is a precise match, return the original row.
return|return
name|row
return|;
block|}
elseif|else
if|if
condition|(
name|key_memcache
operator|!=
literal|null
condition|)
block|{
comment|// no precise matches, so return the one that is closer to the search
comment|// key (greatest)
return|return
name|key_memcache
operator|.
name|compareTo
argument_list|(
name|key_snapshot
argument_list|)
operator|>
literal|0
condition|?
name|key_memcache
else|:
name|key_snapshot
return|;
block|}
return|return
literal|null
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
specifier|private
name|Text
name|internalGetRowKeyAtOrBefore
parameter_list|(
name|SortedMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
name|map
parameter_list|,
name|Text
name|key
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
comment|// TODO: account for deleted cells
name|HStoreKey
name|search_key
init|=
operator|new
name|HStoreKey
argument_list|(
name|key
argument_list|,
name|timestamp
argument_list|)
decl_stmt|;
comment|// get all the entries that come equal or after our search key
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
name|search_key
argument_list|)
decl_stmt|;
comment|// if the first item in the tail has a matching row, then we have an
comment|// exact match, and we should return that item
if|if
condition|(
operator|!
name|tailMap
operator|.
name|isEmpty
argument_list|()
operator|&&
name|tailMap
operator|.
name|firstKey
argument_list|()
operator|.
name|getRow
argument_list|()
operator|.
name|equals
argument_list|(
name|key
argument_list|)
condition|)
block|{
comment|// seek forward past any cells that don't fulfill the timestamp
comment|// argument
name|Iterator
argument_list|<
name|HStoreKey
argument_list|>
name|key_iterator
init|=
name|tailMap
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|HStoreKey
name|found_key
init|=
name|key_iterator
operator|.
name|next
argument_list|()
decl_stmt|;
comment|// keep seeking so long as we're in the same row, and the timstamp
comment|// isn't as small as we'd like, and there are more cells to check
while|while
condition|(
name|found_key
operator|.
name|getRow
argument_list|()
operator|.
name|equals
argument_list|(
name|key
argument_list|)
operator|&&
name|found_key
operator|.
name|getTimestamp
argument_list|()
operator|>
name|timestamp
operator|&&
name|key_iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|found_key
operator|=
name|key_iterator
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
comment|// if this check fails, then we've iterated through all the keys that
comment|// match by row, but none match by timestamp, so we fall through to
comment|// the headMap case.
if|if
condition|(
name|found_key
operator|.
name|getTimestamp
argument_list|()
operator|<=
name|timestamp
condition|)
block|{
comment|// we didn't find a key that matched by timestamp, so we have to
comment|// return null;
comment|/*          LOG.debug("Went searching for " + key + ", found " + found_key.getRow());*/
return|return
name|found_key
operator|.
name|getRow
argument_list|()
return|;
block|}
block|}
comment|// the tail didn't contain the key we're searching for, so we should
comment|// use the last key in the headmap as the closest before
name|SortedMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
name|headMap
init|=
name|map
operator|.
name|headMap
argument_list|(
name|search_key
argument_list|)
decl_stmt|;
return|return
name|headMap
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
name|headMap
operator|.
name|lastKey
argument_list|()
operator|.
name|getRow
argument_list|()
return|;
block|}
comment|/**    * Examine a single map for the desired key.    *    * TODO - This is kinda slow.  We need a data structure that allows for     * proximity-searches, not just precise-matches.    *     * @param map    * @param key    * @param numVersions    * @return Ordered list of items found in passed<code>map</code>.  If no    * matching values, returns an empty list (does not return null).    */
specifier|private
name|ArrayList
argument_list|<
name|Cell
argument_list|>
name|internalGet
parameter_list|(
specifier|final
name|SortedMap
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
name|Cell
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
decl_stmt|;
comment|// TODO: If get is of a particular version -- numVersions == 1 -- we
comment|// should be able to avoid all of the tailmap creations and iterations
comment|// below.
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
if|if
condition|(
name|itKey
operator|.
name|matchesRowCol
argument_list|(
name|key
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|HLogEdit
operator|.
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
operator|new
name|Cell
argument_list|(
name|tailMap
operator|.
name|get
argument_list|(
name|itKey
argument_list|)
argument_list|,
name|itKey
operator|.
name|getTimestamp
argument_list|()
argument_list|)
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
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|List
argument_list|<
name|HStoreKey
argument_list|>
name|results
decl_stmt|;
synchronized|synchronized
init|(
name|memcache
init|)
block|{
name|results
operator|=
name|internalGetKeys
argument_list|(
name|this
operator|.
name|memcache
argument_list|,
name|origin
argument_list|,
name|versions
argument_list|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|snapshot
init|)
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
name|internalGetKeys
argument_list|(
name|snapshot
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
name|readLock
argument_list|()
operator|.
name|unlock
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
name|internalGetKeys
parameter_list|(
specifier|final
name|SortedMap
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
comment|// if there's no column name, then compare rows and timestamps
if|if
condition|(
name|origin
operator|.
name|getColumn
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
comment|// if the current and origin row don't match, then we can jump
comment|// out of the loop entirely.
if|if
condition|(
operator|!
name|key
operator|.
name|getRow
argument_list|()
operator|.
name|equals
argument_list|(
name|origin
operator|.
name|getRow
argument_list|()
argument_list|)
condition|)
block|{
break|break;
block|}
comment|// if the rows match but the timestamp is newer, skip it so we can
comment|// get to the ones we actually want.
if|if
condition|(
name|key
operator|.
name|getTimestamp
argument_list|()
operator|>
name|origin
operator|.
name|getTimestamp
argument_list|()
condition|)
block|{
continue|continue;
block|}
block|}
else|else
block|{
comment|// compare rows and columns
comment|// if the key doesn't match the row and column, then we're done, since
comment|// all the cells are ordered.
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
block|}
if|if
condition|(
operator|!
name|HLogEdit
operator|.
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
name|HLogEdit
operator|.
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
comment|/**    * @return a scanner over the keys in the Memcache    */
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
comment|// Here we rely on ReentrantReadWriteLock's ability to acquire multiple
comment|// locks by the same thread and to be able to downgrade a write lock to
comment|// a read lock. We need to hold a lock throughout this method, but only
comment|// need the write lock while creating the memcache snapshot
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
comment|// hold write lock during memcache snapshot
name|snapshot
argument_list|()
expr_stmt|;
comment|// snapshot memcache
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
comment|// acquire read lock
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
comment|// downgrade to read lock
try|try
block|{
comment|// Prevent a cache flush while we are constructing the scanner
return|return
operator|new
name|MemcacheScanner
argument_list|(
name|timestamp
argument_list|,
name|targetCols
argument_list|,
name|firstRow
argument_list|)
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
comment|//////////////////////////////////////////////////////////////////////////////
comment|// MemcacheScanner implements the HScannerInterface.
comment|// It lets the caller scan the contents of the Memcache.
comment|//////////////////////////////////////////////////////////////////////////////
class|class
name|MemcacheScanner
extends|extends
name|HAbstractScanner
block|{
name|SortedMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
name|backingMap
decl_stmt|;
name|Iterator
argument_list|<
name|HStoreKey
argument_list|>
name|keyIterator
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|MemcacheScanner
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
try|try
block|{
name|this
operator|.
name|backingMap
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
name|this
operator|.
name|backingMap
operator|.
name|putAll
argument_list|(
name|snapshot
argument_list|)
expr_stmt|;
name|this
operator|.
name|keys
operator|=
operator|new
name|HStoreKey
index|[
literal|1
index|]
expr_stmt|;
name|this
operator|.
name|vals
operator|=
operator|new
name|byte
index|[
literal|1
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
if|if
condition|(
name|firstRow
operator|!=
literal|null
operator|&&
name|firstRow
operator|.
name|getLength
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|keyIterator
operator|=
name|backingMap
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
name|keyIterator
operator|=
name|backingMap
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
literal|0
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|findFirstRow
argument_list|(
literal|0
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
literal|0
argument_list|)
condition|)
block|{
break|break;
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
literal|"error initializing Memcache scanner: "
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
literal|"error initializing Memcache scanner"
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
literal|"error initializing Memcache scanner: "
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
name|keyIterator
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
name|keyIterator
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
name|backingMap
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
name|keyIterator
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
name|backingMap
operator|=
literal|null
expr_stmt|;
block|}
comment|/** Shut down map iterators */
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
if|if
condition|(
name|keyIterator
operator|!=
literal|null
condition|)
block|{
name|closeSubScanner
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|scannerClosed
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

