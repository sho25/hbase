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
name|wal
package|;
end_package

begin_import
import|import static
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
name|ConcurrentMapUtils
operator|.
name|computeIfAbsent
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
name|Collection
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
name|HashMap
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
name|Set
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
name|ConcurrentHashMap
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
name|ConcurrentMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|ImmutableByteArray
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * Accounting of sequence ids per region and then by column family. So we can keep our accounting  * current, call startCacheFlush and then finishedCacheFlush or abortCacheFlush so this instance can  * keep abreast of the state of sequence id persistence. Also call update per append.  *<p>  * For the implementation, we assume that all the {@code encodedRegionName} passed in are gotten by  * {@link org.apache.hadoop.hbase.client.RegionInfo#getEncodedNameAsBytes()}. So it is safe to use  * it as a hash key. And for family name, we use {@link ImmutableByteArray} as key. This is because  * hash based map is much faster than RBTree or CSLM and here we are on the critical write path. See  * HBASE-16278 for more details.  *</p>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|SequenceIdAccounting
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SequenceIdAccounting
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * This lock ties all operations on {@link SequenceIdAccounting#flushingSequenceIds} and    * {@link #lowestUnflushedSequenceIds} Maps. {@link #lowestUnflushedSequenceIds} has the    * lowest outstanding sequence ids EXCEPT when flushing. When we flush, the current    * lowest set for the region/column family are moved (atomically because of this lock) to    * {@link #flushingSequenceIds}.    *     *<p>The two Maps are tied by this locking object EXCEPT when we go to update the lowest    * entry; see {@link #lowestUnflushedSequenceIds}. In here is a putIfAbsent call on    * {@link #lowestUnflushedSequenceIds}. In this latter case, we will add this lowest    * sequence id if we find that there is no entry for the current column family. There will be no    * entry only if we just came up OR we have moved aside current set of lowest sequence ids    * because the current set are being flushed (by putting them into {@link #flushingSequenceIds}).    * This is how we pick up the next 'lowest' sequence id per region per column family to be used    * figuring what is in the next flush.    */
specifier|private
specifier|final
name|Object
name|tieLock
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
comment|/**    * Map of encoded region names and family names to their OLDEST -- i.e. their first,    * the longest-lived, their 'earliest', the 'lowest' -- sequence id.    *    *<p>When we flush, the current lowest sequence ids get cleared and added to    * {@link #flushingSequenceIds}. The next append that comes in, is then added    * here to {@link #lowestUnflushedSequenceIds} as the next lowest sequenceid.    *    *<p>If flush fails, currently server is aborted so no need to restore previous sequence ids.    *<p>Needs to be concurrent Maps because we use putIfAbsent updating oldest.    */
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|byte
index|[]
argument_list|,
name|ConcurrentMap
argument_list|<
name|ImmutableByteArray
argument_list|,
name|Long
argument_list|>
argument_list|>
name|lowestUnflushedSequenceIds
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * Map of encoded region names and family names to their lowest or OLDEST sequence/edit id    * currently being flushed out to hfiles. Entries are moved here from    * {@link #lowestUnflushedSequenceIds} while the lock {@link #tieLock} is held    * (so movement between the Maps is atomic).    */
specifier|private
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Map
argument_list|<
name|ImmutableByteArray
argument_list|,
name|Long
argument_list|>
argument_list|>
name|flushingSequenceIds
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    *<p>    * Map of region encoded names to the latest/highest region sequence id. Updated on each call to    * append.    *</p>    *<p>    * This map uses byte[] as the key, and uses reference equality. It works in our use case as we    * use {@link org.apache.hadoop.hbase.client.RegionInfo#getEncodedNameAsBytes()} as keys. For a    * given region, it always returns the same array.    *</p>    */
specifier|private
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|highestSequenceIds
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * Returns the lowest unflushed sequence id for the region.    * @return Lowest outstanding unflushed sequenceid for<code>encodedRegionName</code>. Will    * return {@link HConstants#NO_SEQNUM} when none.    */
name|long
name|getLowestSequenceId
parameter_list|(
specifier|final
name|byte
index|[]
name|encodedRegionName
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|tieLock
init|)
block|{
name|Map
argument_list|<
name|?
argument_list|,
name|Long
argument_list|>
name|m
init|=
name|this
operator|.
name|flushingSequenceIds
operator|.
name|get
argument_list|(
name|encodedRegionName
argument_list|)
decl_stmt|;
name|long
name|flushingLowest
init|=
name|m
operator|!=
literal|null
condition|?
name|getLowestSequenceId
argument_list|(
name|m
argument_list|)
else|:
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|m
operator|=
name|this
operator|.
name|lowestUnflushedSequenceIds
operator|.
name|get
argument_list|(
name|encodedRegionName
argument_list|)
expr_stmt|;
name|long
name|unflushedLowest
init|=
name|m
operator|!=
literal|null
condition|?
name|getLowestSequenceId
argument_list|(
name|m
argument_list|)
else|:
name|HConstants
operator|.
name|NO_SEQNUM
decl_stmt|;
return|return
name|Math
operator|.
name|min
argument_list|(
name|flushingLowest
argument_list|,
name|unflushedLowest
argument_list|)
return|;
block|}
block|}
comment|/**    * @return Lowest outstanding unflushed sequenceid for<code>encodedRegionname</code> and    *<code>familyName</code>. Returned sequenceid may be for an edit currently being    *         flushed.    */
name|long
name|getLowestSequenceId
parameter_list|(
specifier|final
name|byte
index|[]
name|encodedRegionName
parameter_list|,
specifier|final
name|byte
index|[]
name|familyName
parameter_list|)
block|{
name|ImmutableByteArray
name|familyNameWrapper
init|=
name|ImmutableByteArray
operator|.
name|wrap
argument_list|(
name|familyName
argument_list|)
decl_stmt|;
synchronized|synchronized
init|(
name|this
operator|.
name|tieLock
init|)
block|{
name|Map
argument_list|<
name|ImmutableByteArray
argument_list|,
name|Long
argument_list|>
name|m
init|=
name|this
operator|.
name|flushingSequenceIds
operator|.
name|get
argument_list|(
name|encodedRegionName
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|!=
literal|null
condition|)
block|{
name|Long
name|lowest
init|=
name|m
operator|.
name|get
argument_list|(
name|familyNameWrapper
argument_list|)
decl_stmt|;
if|if
condition|(
name|lowest
operator|!=
literal|null
condition|)
block|{
return|return
name|lowest
return|;
block|}
block|}
name|m
operator|=
name|this
operator|.
name|lowestUnflushedSequenceIds
operator|.
name|get
argument_list|(
name|encodedRegionName
argument_list|)
expr_stmt|;
if|if
condition|(
name|m
operator|!=
literal|null
condition|)
block|{
name|Long
name|lowest
init|=
name|m
operator|.
name|get
argument_list|(
name|familyNameWrapper
argument_list|)
decl_stmt|;
if|if
condition|(
name|lowest
operator|!=
literal|null
condition|)
block|{
return|return
name|lowest
return|;
block|}
block|}
block|}
return|return
name|HConstants
operator|.
name|NO_SEQNUM
return|;
block|}
comment|/**    * Reset the accounting of highest sequenceid by regionname.    * @return Return the previous accounting Map of regions to the last sequence id written into    * each.    */
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|resetHighest
parameter_list|()
block|{
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|old
init|=
name|this
operator|.
name|highestSequenceIds
decl_stmt|;
name|this
operator|.
name|highestSequenceIds
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
return|return
name|old
return|;
block|}
comment|/**    * We've been passed a new sequenceid for the region. Set it as highest seen for this region and    * if we are to record oldest, or lowest sequenceids, save it as oldest seen if nothing    * currently older.    * @param encodedRegionName    * @param families    * @param sequenceid    * @param lowest Whether to keep running account of oldest sequence id.    */
name|void
name|update
parameter_list|(
name|byte
index|[]
name|encodedRegionName
parameter_list|,
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|families
parameter_list|,
name|long
name|sequenceid
parameter_list|,
specifier|final
name|boolean
name|lowest
parameter_list|)
block|{
name|Long
name|l
init|=
name|Long
operator|.
name|valueOf
argument_list|(
name|sequenceid
argument_list|)
decl_stmt|;
name|this
operator|.
name|highestSequenceIds
operator|.
name|put
argument_list|(
name|encodedRegionName
argument_list|,
name|l
argument_list|)
expr_stmt|;
if|if
condition|(
name|lowest
condition|)
block|{
name|ConcurrentMap
argument_list|<
name|ImmutableByteArray
argument_list|,
name|Long
argument_list|>
name|m
init|=
name|getOrCreateLowestSequenceIds
argument_list|(
name|encodedRegionName
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|familyName
range|:
name|families
control|)
block|{
name|m
operator|.
name|putIfAbsent
argument_list|(
name|ImmutableByteArray
operator|.
name|wrap
argument_list|(
name|familyName
argument_list|)
argument_list|,
name|l
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Clear all the records of the given region as it is going to be closed.    *<p/>    * We will call this once we get the region close marker. We need this because that, if we use    * Durability.ASYNC_WAL, after calling startCacheFlush, we may still get some ongoing wal entries    * that has not been processed yet, this will lead to orphan records in the    * lowestUnflushedSequenceIds and then cause too many WAL files.    *<p/>    * See HBASE-23157 for more details.    */
name|void
name|onRegionClose
parameter_list|(
name|byte
index|[]
name|encodedRegionName
parameter_list|)
block|{
synchronized|synchronized
init|(
name|tieLock
init|)
block|{
name|this
operator|.
name|lowestUnflushedSequenceIds
operator|.
name|remove
argument_list|(
name|encodedRegionName
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|ImmutableByteArray
argument_list|,
name|Long
argument_list|>
name|flushing
init|=
name|this
operator|.
name|flushingSequenceIds
operator|.
name|remove
argument_list|(
name|encodedRegionName
argument_list|)
decl_stmt|;
if|if
condition|(
name|flushing
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Still have flushing records when closing {}, {}"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|encodedRegionName
argument_list|)
argument_list|,
name|flushing
operator|.
name|entrySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|e
lambda|->
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|"->"
operator|+
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|joining
argument_list|(
literal|","
argument_list|,
literal|"{"
argument_list|,
literal|"}"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|highestSequenceIds
operator|.
name|remove
argument_list|(
name|encodedRegionName
argument_list|)
expr_stmt|;
block|}
comment|/**    * Update the store sequence id, e.g., upon executing in-memory compaction    */
name|void
name|updateStore
parameter_list|(
name|byte
index|[]
name|encodedRegionName
parameter_list|,
name|byte
index|[]
name|familyName
parameter_list|,
name|Long
name|sequenceId
parameter_list|,
name|boolean
name|onlyIfGreater
parameter_list|)
block|{
if|if
condition|(
name|sequenceId
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|Long
name|highest
init|=
name|this
operator|.
name|highestSequenceIds
operator|.
name|get
argument_list|(
name|encodedRegionName
argument_list|)
decl_stmt|;
if|if
condition|(
name|highest
operator|==
literal|null
operator|||
name|sequenceId
operator|>
name|highest
condition|)
block|{
name|this
operator|.
name|highestSequenceIds
operator|.
name|put
argument_list|(
name|encodedRegionName
argument_list|,
name|sequenceId
argument_list|)
expr_stmt|;
block|}
name|ImmutableByteArray
name|familyNameWrapper
init|=
name|ImmutableByteArray
operator|.
name|wrap
argument_list|(
name|familyName
argument_list|)
decl_stmt|;
synchronized|synchronized
init|(
name|this
operator|.
name|tieLock
init|)
block|{
name|ConcurrentMap
argument_list|<
name|ImmutableByteArray
argument_list|,
name|Long
argument_list|>
name|m
init|=
name|getOrCreateLowestSequenceIds
argument_list|(
name|encodedRegionName
argument_list|)
decl_stmt|;
name|boolean
name|replaced
init|=
literal|false
decl_stmt|;
while|while
condition|(
operator|!
name|replaced
condition|)
block|{
name|Long
name|oldSeqId
init|=
name|m
operator|.
name|get
argument_list|(
name|familyNameWrapper
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldSeqId
operator|==
literal|null
condition|)
block|{
name|m
operator|.
name|put
argument_list|(
name|familyNameWrapper
argument_list|,
name|sequenceId
argument_list|)
expr_stmt|;
name|replaced
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|onlyIfGreater
condition|)
block|{
if|if
condition|(
name|sequenceId
operator|>
name|oldSeqId
condition|)
block|{
name|replaced
operator|=
name|m
operator|.
name|replace
argument_list|(
name|familyNameWrapper
argument_list|,
name|oldSeqId
argument_list|,
name|sequenceId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
return|return;
block|}
block|}
else|else
block|{
comment|// replace even if sequence id is not greater than oldSeqId
name|m
operator|.
name|put
argument_list|(
name|familyNameWrapper
argument_list|,
name|sequenceId
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
block|}
block|}
annotation|@
name|VisibleForTesting
name|ConcurrentMap
argument_list|<
name|ImmutableByteArray
argument_list|,
name|Long
argument_list|>
name|getOrCreateLowestSequenceIds
parameter_list|(
name|byte
index|[]
name|encodedRegionName
parameter_list|)
block|{
comment|// Intentionally, this access is done outside of this.regionSequenceIdLock. Done per append.
return|return
name|computeIfAbsent
argument_list|(
name|this
operator|.
name|lowestUnflushedSequenceIds
argument_list|,
name|encodedRegionName
argument_list|,
name|ConcurrentHashMap
operator|::
operator|new
argument_list|)
return|;
block|}
comment|/**    * @param sequenceids Map to search for lowest value.    * @return Lowest value found in<code>sequenceids</code>.    */
specifier|private
specifier|static
name|long
name|getLowestSequenceId
parameter_list|(
name|Map
argument_list|<
name|?
argument_list|,
name|Long
argument_list|>
name|sequenceids
parameter_list|)
block|{
name|long
name|lowest
init|=
name|HConstants
operator|.
name|NO_SEQNUM
decl_stmt|;
for|for
control|(
name|Long
name|sid
range|:
name|sequenceids
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|lowest
operator|==
name|HConstants
operator|.
name|NO_SEQNUM
operator|||
name|sid
operator|.
name|longValue
argument_list|()
operator|<
name|lowest
condition|)
block|{
name|lowest
operator|=
name|sid
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|lowest
return|;
block|}
comment|/**    * @param src    * @return New Map that has same keys as<code>src</code> but instead of a Map for a value, it    *         instead has found the smallest sequence id and it returns that as the value instead.    */
specifier|private
parameter_list|<
name|T
extends|extends
name|Map
argument_list|<
name|?
argument_list|,
name|Long
argument_list|>
parameter_list|>
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|flattenToLowestSequenceId
parameter_list|(
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|T
argument_list|>
name|src
parameter_list|)
block|{
if|if
condition|(
name|src
operator|==
literal|null
operator|||
name|src
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|tgt
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|T
argument_list|>
name|entry
range|:
name|src
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|long
name|lowestSeqId
init|=
name|getLowestSequenceId
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|lowestSeqId
operator|!=
name|HConstants
operator|.
name|NO_SEQNUM
condition|)
block|{
name|tgt
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|lowestSeqId
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|tgt
return|;
block|}
comment|/**    * @param encodedRegionName Region to flush.    * @param families Families to flush. May be a subset of all families in the region.    * @return Returns {@link HConstants#NO_SEQNUM} if we are flushing the whole region OR if    * we are flushing a subset of all families but there are no edits in those families not    * being flushed; in other words, this is effectively same as a flush of all of the region    * though we were passed a subset of regions. Otherwise, it returns the sequence id of the    * oldest/lowest outstanding edit.    */
name|Long
name|startCacheFlush
parameter_list|(
specifier|final
name|byte
index|[]
name|encodedRegionName
parameter_list|,
specifier|final
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|families
parameter_list|)
block|{
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|familytoSeq
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|familyName
range|:
name|families
control|)
block|{
name|familytoSeq
operator|.
name|put
argument_list|(
name|familyName
argument_list|,
name|HConstants
operator|.
name|NO_SEQNUM
argument_list|)
expr_stmt|;
block|}
return|return
name|startCacheFlush
argument_list|(
name|encodedRegionName
argument_list|,
name|familytoSeq
argument_list|)
return|;
block|}
name|Long
name|startCacheFlush
parameter_list|(
specifier|final
name|byte
index|[]
name|encodedRegionName
parameter_list|,
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|familyToSeq
parameter_list|)
block|{
name|Map
argument_list|<
name|ImmutableByteArray
argument_list|,
name|Long
argument_list|>
name|oldSequenceIds
init|=
literal|null
decl_stmt|;
name|Long
name|lowestUnflushedInRegion
init|=
name|HConstants
operator|.
name|NO_SEQNUM
decl_stmt|;
synchronized|synchronized
init|(
name|tieLock
init|)
block|{
name|Map
argument_list|<
name|ImmutableByteArray
argument_list|,
name|Long
argument_list|>
name|m
init|=
name|this
operator|.
name|lowestUnflushedSequenceIds
operator|.
name|get
argument_list|(
name|encodedRegionName
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|!=
literal|null
condition|)
block|{
comment|// NOTE: Removal from this.lowestUnflushedSequenceIds must be done in controlled
comment|// circumstance because another concurrent thread now may add sequenceids for this family
comment|// (see above in getOrCreateLowestSequenceId). Make sure you are ok with this. Usually it
comment|// is fine because updates are blocked when this method is called. Make sure!!!
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|entry
range|:
name|familyToSeq
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|ImmutableByteArray
name|familyNameWrapper
init|=
name|ImmutableByteArray
operator|.
name|wrap
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|Long
name|seqId
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|==
name|HConstants
operator|.
name|NO_SEQNUM
condition|)
block|{
name|seqId
operator|=
name|m
operator|.
name|remove
argument_list|(
name|familyNameWrapper
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|seqId
operator|=
name|m
operator|.
name|replace
argument_list|(
name|familyNameWrapper
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|seqId
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|oldSequenceIds
operator|==
literal|null
condition|)
block|{
name|oldSequenceIds
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|oldSequenceIds
operator|.
name|put
argument_list|(
name|familyNameWrapper
argument_list|,
name|seqId
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|oldSequenceIds
operator|!=
literal|null
operator|&&
operator|!
name|oldSequenceIds
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|flushingSequenceIds
operator|.
name|put
argument_list|(
name|encodedRegionName
argument_list|,
name|oldSequenceIds
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Flushing Map not cleaned up for "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|encodedRegionName
argument_list|)
operator|+
literal|", sequenceid="
operator|+
name|oldSequenceIds
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|m
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// Remove it otherwise it will be in oldestUnflushedStoreSequenceIds for ever
comment|// even if the region is already moved to other server.
comment|// Do not worry about data racing, we held write lock of region when calling
comment|// startCacheFlush, so no one can add value to the map we removed.
name|this
operator|.
name|lowestUnflushedSequenceIds
operator|.
name|remove
argument_list|(
name|encodedRegionName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Flushing a subset of the region families. Return the sequence id of the oldest entry.
name|lowestUnflushedInRegion
operator|=
name|Collections
operator|.
name|min
argument_list|(
name|m
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Do this check outside lock.
if|if
condition|(
name|oldSequenceIds
operator|!=
literal|null
operator|&&
name|oldSequenceIds
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// TODO: if we have no oldStoreSeqNum, and WAL is not disabled, presumably either
comment|// the region is already flushing (which would make this call invalid), or there
comment|// were no appends after last flush, so why are we starting flush? Maybe we should
comment|// assert not empty. Less rigorous, but safer, alternative is telling the caller to stop.
comment|// For now preserve old logic.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Couldn't find oldest sequenceid for "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|encodedRegionName
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|lowestUnflushedInRegion
return|;
block|}
name|void
name|completeCacheFlush
parameter_list|(
specifier|final
name|byte
index|[]
name|encodedRegionName
parameter_list|)
block|{
synchronized|synchronized
init|(
name|tieLock
init|)
block|{
name|this
operator|.
name|flushingSequenceIds
operator|.
name|remove
argument_list|(
name|encodedRegionName
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|abortCacheFlush
parameter_list|(
specifier|final
name|byte
index|[]
name|encodedRegionName
parameter_list|)
block|{
comment|// Method is called when we are crashing down because failed write flush AND it is called
comment|// if we fail prepare. The below is for the fail prepare case; we restore the old sequence ids.
name|Map
argument_list|<
name|ImmutableByteArray
argument_list|,
name|Long
argument_list|>
name|flushing
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|ImmutableByteArray
argument_list|,
name|Long
argument_list|>
name|tmpMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// Here we are moving sequenceids from flushing back to unflushed; doing opposite of what
comment|// happened in startCacheFlush. During prepare phase, we have update lock on the region so
comment|// no edits should be coming in via append.
synchronized|synchronized
init|(
name|tieLock
init|)
block|{
name|flushing
operator|=
name|this
operator|.
name|flushingSequenceIds
operator|.
name|remove
argument_list|(
name|encodedRegionName
argument_list|)
expr_stmt|;
if|if
condition|(
name|flushing
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|ImmutableByteArray
argument_list|,
name|Long
argument_list|>
name|unflushed
init|=
name|getOrCreateLowestSequenceIds
argument_list|(
name|encodedRegionName
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ImmutableByteArray
argument_list|,
name|Long
argument_list|>
name|e
range|:
name|flushing
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|// Set into unflushed the 'old' oldest sequenceid and if any value in flushed with this
comment|// value, it will now be in tmpMap.
name|tmpMap
operator|.
name|put
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|unflushed
operator|.
name|put
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Here we are doing some 'test' to see if edits are going in out of order. What is it for?
comment|// Carried over from old code.
if|if
condition|(
name|flushing
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ImmutableByteArray
argument_list|,
name|Long
argument_list|>
name|e
range|:
name|flushing
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Long
name|currentId
init|=
name|tmpMap
operator|.
name|get
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentId
operator|!=
literal|null
operator|&&
name|currentId
operator|.
name|longValue
argument_list|()
operator|<
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|longValue
argument_list|()
condition|)
block|{
name|String
name|errorStr
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|encodedRegionName
argument_list|)
operator|+
literal|" family "
operator|+
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|" acquired edits out of order current memstore seq="
operator|+
name|currentId
operator|+
literal|", previous oldest unflushed id="
operator|+
name|e
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|errorStr
argument_list|)
expr_stmt|;
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|halt
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * See if passed<code>sequenceids</code> are lower -- i.e. earlier -- than any outstanding    * sequenceids, sequenceids we are holding on to in this accounting instance.    * @param sequenceids Keyed by encoded region name. Cannot be null (doesn't make sense for it to    *          be null).    * @param keysBlocking An optional collection that is used to return the specific keys that are    *          causing this method to return false.    * @return true if all sequenceids are lower, older than, the old sequenceids in this instance.    */
name|boolean
name|areAllLower
parameter_list|(
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|sequenceids
parameter_list|,
name|Collection
argument_list|<
name|byte
index|[]
argument_list|>
name|keysBlocking
parameter_list|)
block|{
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|flushing
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|unflushed
init|=
literal|null
decl_stmt|;
synchronized|synchronized
init|(
name|this
operator|.
name|tieLock
init|)
block|{
comment|// Get a flattened -- only the oldest sequenceid -- copy of current flushing and unflushed
comment|// data structures to use in tests below.
name|flushing
operator|=
name|flattenToLowestSequenceId
argument_list|(
name|this
operator|.
name|flushingSequenceIds
argument_list|)
expr_stmt|;
name|unflushed
operator|=
name|flattenToLowestSequenceId
argument_list|(
name|this
operator|.
name|lowestUnflushedSequenceIds
argument_list|)
expr_stmt|;
block|}
name|boolean
name|result
init|=
literal|true
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|e
range|:
name|sequenceids
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|long
name|oldestFlushing
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|long
name|oldestUnflushed
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
if|if
condition|(
name|flushing
operator|!=
literal|null
operator|&&
name|flushing
operator|.
name|containsKey
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|oldestFlushing
operator|=
name|flushing
operator|.
name|get
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|unflushed
operator|!=
literal|null
operator|&&
name|unflushed
operator|.
name|containsKey
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|oldestUnflushed
operator|=
name|unflushed
operator|.
name|get
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|long
name|min
init|=
name|Math
operator|.
name|min
argument_list|(
name|oldestFlushing
argument_list|,
name|oldestUnflushed
argument_list|)
decl_stmt|;
if|if
condition|(
name|min
operator|<=
name|e
operator|.
name|getValue
argument_list|()
condition|)
block|{
if|if
condition|(
name|keysBlocking
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|result
operator|=
literal|false
expr_stmt|;
name|keysBlocking
operator|.
name|add
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
comment|// Continue examining the map so we could log all regions blocking this WAL.
block|}
block|}
return|return
name|result
return|;
block|}
comment|/**    * Iterates over the given Map and compares sequence ids with corresponding entries in    * {@link #lowestUnflushedSequenceIds}. If a region in    * {@link #lowestUnflushedSequenceIds} has a sequence id less than that passed in    *<code>sequenceids</code> then return it.    * @param sequenceids Sequenceids keyed by encoded region name.    * @return regions found in this instance with sequence ids less than those passed in.    */
name|byte
index|[]
index|[]
name|findLower
parameter_list|(
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|sequenceids
parameter_list|)
block|{
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|toFlush
init|=
literal|null
decl_stmt|;
comment|// Keeping the old behavior of iterating unflushedSeqNums under oldestSeqNumsLock.
synchronized|synchronized
init|(
name|tieLock
init|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|e
range|:
name|sequenceids
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Map
argument_list|<
name|ImmutableByteArray
argument_list|,
name|Long
argument_list|>
name|m
init|=
name|this
operator|.
name|lowestUnflushedSequenceIds
operator|.
name|get
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
comment|// The lowest sequence id outstanding for this region.
name|long
name|lowest
init|=
name|getLowestSequenceId
argument_list|(
name|m
argument_list|)
decl_stmt|;
if|if
condition|(
name|lowest
operator|!=
name|HConstants
operator|.
name|NO_SEQNUM
operator|&&
name|lowest
operator|<=
name|e
operator|.
name|getValue
argument_list|()
condition|)
block|{
if|if
condition|(
name|toFlush
operator|==
literal|null
condition|)
block|{
name|toFlush
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|toFlush
operator|.
name|add
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|toFlush
operator|==
literal|null
condition|?
literal|null
else|:
name|toFlush
operator|.
name|toArray
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
index|[]
argument_list|)
return|;
block|}
block|}
end_class

end_unit

