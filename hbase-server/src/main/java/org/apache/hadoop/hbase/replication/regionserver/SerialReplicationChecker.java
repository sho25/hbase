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
name|replication
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
name|Arrays
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
name|TimeUnit
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
name|lang3
operator|.
name|mutable
operator|.
name|MutableLong
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
name|conf
operator|.
name|Configuration
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
name|MetaTableAccessor
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
name|MetaTableAccessor
operator|.
name|ReplicationBarrierResult
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
name|Connection
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
name|RegionInfo
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
name|master
operator|.
name|RegionState
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
name|replication
operator|.
name|ReplicationException
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
name|replication
operator|.
name|ReplicationQueueStorage
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
name|wal
operator|.
name|WAL
operator|.
name|Entry
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
name|cache
operator|.
name|Cache
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
name|cache
operator|.
name|CacheBuilder
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
name|cache
operator|.
name|CacheLoader
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
name|cache
operator|.
name|LoadingCache
import|;
end_import

begin_comment
comment|/**  *<p>  * Helper class to determine whether we can push a given WAL entry without breaking the replication  * order. The class is designed to per {@link ReplicationSourceWALReader}, so not thread safe.  *</p>  *<p>  * We record all the open sequence number for a region in a special family in meta, which is called  * 'rep_barrier', so there will be a sequence of open sequence number (b1, b2, b3, ...). We call  * [bn, bn+1) a range, and it is obvious that a region will always be on the same RS within a  * range.  *<p>  * When split and merge, we will also record the parent for the generated region(s) in the special  * family in meta. And also, we will write an extra 'open sequence number' for the parent  * region(s), which is the max sequence id of the region plus one.  *</p>  *</p>  *<p>  * For each peer, we record the last pushed sequence id for each region. It is managed by the  * replication storage.  *</p>  *<p>  * The algorithm works like this:  *<ol>  *<li>Locate the sequence id we want to push in the barriers</li>  *<li>If it is before the first barrier, we are safe to push. This usually because we enable serial  * replication for this table after we create the table and write data into the table.</li>  *<li>In general, if the previous range is finished, then we are safe to push. The way to determine  * whether a range is finish is straight-forward: check whether the last pushed sequence id is equal  * to the end barrier of the range minus 1. There are several exceptions:  *<ul>  *<li>If it is in the first range, we need to check whether there are parent regions. If so, we  * need to make sure that the data for parent regions have all been pushed.</li>  *<li>If it is in the last range, we need to check the region state. If state is OPENING, then we  * are not safe to push. This is because that, before we call reportRIT to master which update the  * open sequence number into meta table, we will write a open region event marker to WAL first, and  * its sequence id is greater than the newest open sequence number(which has not been updated to  * meta table yet so we do not know). For this scenario, the WAL entry for this open region event  * marker actually belongs to the range after the 'last' range, so we are not safe to push it.  * Otherwise the last pushed sequence id will be updated to this value and then we think the  * previous range has already been finished, but this is not true.</li>  *<li>Notice that the above two exceptions are not conflicts, since the first range can also be the  * last range if we only have one range.</li>  *</ul>  *</li>  *</ol>  *</p>  *<p>  * And for performance reason, we do not want to check meta for every WAL entry, so we introduce two  * in memory maps. The idea is simple:  *<ul>  *<li>If a range can be pushed, then put its end barrier into the {@code canPushUnder} map.</li>  *<li>Before accessing meta, first check the sequence id stored in the {@code canPushUnder} map. If  * the sequence id of WAL entry is less the one stored in {@code canPushUnder} map, then we are safe  * to push.</li>  *</ul>  * And for the last range, we do not have an end barrier, so we use the continuity of sequence id to  * determine whether we can push. The rule is:  *<ul>  *<li>When an entry is able to push, then put its sequence id into the {@code pushed} map.</li>  *<li>Check if the sequence id of WAL entry equals to the one stored in the {@code pushed} map plus  * one. If so, we are safe to push, and also update the {@code pushed} map with the sequence id of  * the WAL entry.</li>  *</ul>  *</p>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|SerialReplicationChecker
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
name|SerialReplicationChecker
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|REPLICATION_SERIALLY_WAITING_KEY
init|=
literal|"hbase.serial.replication.waiting.ms"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|REPLICATION_SERIALLY_WAITING_DEFAULT
init|=
literal|10000
decl_stmt|;
specifier|private
specifier|final
name|String
name|peerId
decl_stmt|;
specifier|private
specifier|final
name|ReplicationQueueStorage
name|storage
decl_stmt|;
specifier|private
specifier|final
name|Connection
name|conn
decl_stmt|;
specifier|private
specifier|final
name|long
name|waitTimeMs
decl_stmt|;
specifier|private
specifier|final
name|LoadingCache
argument_list|<
name|String
argument_list|,
name|MutableLong
argument_list|>
name|pushed
init|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|expireAfterAccess
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|DAYS
argument_list|)
operator|.
name|build
argument_list|(
operator|new
name|CacheLoader
argument_list|<
name|String
argument_list|,
name|MutableLong
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|MutableLong
name|load
parameter_list|(
name|String
name|key
parameter_list|)
throws|throws
name|Exception
block|{
return|return
operator|new
name|MutableLong
argument_list|(
name|HConstants
operator|.
name|NO_SEQNUM
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
comment|// Use guava cache to set ttl for each key
specifier|private
specifier|final
name|Cache
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|canPushUnder
init|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|expireAfterAccess
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|DAYS
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|public
name|SerialReplicationChecker
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ReplicationSource
name|source
parameter_list|)
block|{
name|this
operator|.
name|peerId
operator|=
name|source
operator|.
name|getPeerId
argument_list|()
expr_stmt|;
name|this
operator|.
name|storage
operator|=
name|source
operator|.
name|getQueueStorage
argument_list|()
expr_stmt|;
name|this
operator|.
name|conn
operator|=
name|source
operator|.
name|getServer
argument_list|()
operator|.
name|getConnection
argument_list|()
expr_stmt|;
name|this
operator|.
name|waitTimeMs
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|REPLICATION_SERIALLY_WAITING_KEY
argument_list|,
name|REPLICATION_SERIALLY_WAITING_DEFAULT
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|isRangeFinished
parameter_list|(
name|long
name|endBarrier
parameter_list|,
name|String
name|encodedRegionName
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|pushedSeqId
decl_stmt|;
try|try
block|{
name|pushedSeqId
operator|=
name|storage
operator|.
name|getLastSequenceId
argument_list|(
name|encodedRegionName
argument_list|,
name|peerId
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ReplicationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to get pushed sequence id for "
operator|+
name|encodedRegionName
operator|+
literal|", peer "
operator|+
name|peerId
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// endBarrier is the open sequence number. When opening a region, the open sequence number will
comment|// be set to the old max sequence id plus one, so here we need to minus one.
return|return
name|pushedSeqId
operator|>=
name|endBarrier
operator|-
literal|1
return|;
block|}
specifier|private
name|boolean
name|isParentFinished
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|)
throws|throws
name|IOException
block|{
name|long
index|[]
name|barriers
init|=
name|MetaTableAccessor
operator|.
name|getReplicationBarrier
argument_list|(
name|conn
argument_list|,
name|regionName
argument_list|)
decl_stmt|;
if|if
condition|(
name|barriers
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
name|isRangeFinished
argument_list|(
name|barriers
index|[
name|barriers
operator|.
name|length
operator|-
literal|1
index|]
argument_list|,
name|RegionInfo
operator|.
name|encodeRegionName
argument_list|(
name|regionName
argument_list|)
argument_list|)
return|;
block|}
comment|// We may write a open region marker to WAL before we write the open sequence number to meta, so
comment|// if a region is in OPENING state and we are in the last range, it is not safe to say we can push
comment|// even if the previous range is finished.
specifier|private
name|boolean
name|isLastRangeAndOpening
parameter_list|(
name|ReplicationBarrierResult
name|barrierResult
parameter_list|,
name|int
name|index
parameter_list|)
block|{
return|return
name|index
operator|==
name|barrierResult
operator|.
name|getBarriers
argument_list|()
operator|.
name|length
operator|&&
name|barrierResult
operator|.
name|getState
argument_list|()
operator|==
name|RegionState
operator|.
name|State
operator|.
name|OPENING
return|;
block|}
specifier|private
name|void
name|recordCanPush
parameter_list|(
name|String
name|encodedNameAsString
parameter_list|,
name|long
name|seqId
parameter_list|,
name|long
index|[]
name|barriers
parameter_list|,
name|int
name|index
parameter_list|)
block|{
if|if
condition|(
name|barriers
operator|.
name|length
operator|>
name|index
condition|)
block|{
name|canPushUnder
operator|.
name|put
argument_list|(
name|encodedNameAsString
argument_list|,
name|barriers
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
name|pushed
operator|.
name|getUnchecked
argument_list|(
name|encodedNameAsString
argument_list|)
operator|.
name|setValue
argument_list|(
name|seqId
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|canPush
parameter_list|(
name|Entry
name|entry
parameter_list|,
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|encodedNameAsString
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getEncodedRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|seqId
init|=
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getSequenceId
argument_list|()
decl_stmt|;
name|ReplicationBarrierResult
name|barrierResult
init|=
name|MetaTableAccessor
operator|.
name|getReplicationBarrierResult
argument_list|(
name|conn
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|,
name|row
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getEncodedRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Replication barrier for {}: {}"
argument_list|,
name|entry
argument_list|,
name|barrierResult
argument_list|)
expr_stmt|;
name|long
index|[]
name|barriers
init|=
name|barrierResult
operator|.
name|getBarriers
argument_list|()
decl_stmt|;
name|int
name|index
init|=
name|Arrays
operator|.
name|binarySearch
argument_list|(
name|barriers
argument_list|,
name|seqId
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|==
operator|-
literal|1
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"{} is before the first barrier, pass"
argument_list|,
name|entry
argument_list|)
expr_stmt|;
comment|// This means we are in the range before the first record openSeqNum, this usually because the
comment|// wal is written before we enable serial replication for this table, just return true since
comment|// we can not guarantee the order.
name|pushed
operator|.
name|getUnchecked
argument_list|(
name|encodedNameAsString
argument_list|)
operator|.
name|setValue
argument_list|(
name|seqId
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
comment|// The sequence id range is left closed and right open, so either we decrease the missed insert
comment|// point to make the index start from 0, or increase the hit insert point to make the index
comment|// start from 1. Here we choose the latter one.
if|if
condition|(
name|index
operator|<
literal|0
condition|)
block|{
name|index
operator|=
operator|-
name|index
operator|-
literal|1
expr_stmt|;
block|}
else|else
block|{
name|index
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|index
operator|==
literal|1
condition|)
block|{
comment|// we are in the first range, check whether we have parents
for|for
control|(
name|byte
index|[]
name|regionName
range|:
name|barrierResult
operator|.
name|getParentRegionNames
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|isParentFinished
argument_list|(
name|regionName
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Parent {} has not been finished yet for entry {}, give up"
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|regionName
argument_list|)
argument_list|,
name|entry
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
if|if
condition|(
name|isLastRangeAndOpening
argument_list|(
name|barrierResult
argument_list|,
name|index
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"{} is in the last range and the region is opening, give up"
argument_list|,
name|entry
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"{} is in the first range, pass"
argument_list|,
name|entry
argument_list|)
expr_stmt|;
name|recordCanPush
argument_list|(
name|encodedNameAsString
argument_list|,
name|seqId
argument_list|,
name|barriers
argument_list|,
literal|1
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
comment|// check whether the previous range is finished
if|if
condition|(
operator|!
name|isRangeFinished
argument_list|(
name|barriers
index|[
name|index
operator|-
literal|1
index|]
argument_list|,
name|encodedNameAsString
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Previous range for {} has not been finished yet, give up"
argument_list|,
name|entry
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
name|isLastRangeAndOpening
argument_list|(
name|barrierResult
argument_list|,
name|index
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"{} is in the last range and the region is opening, give up"
argument_list|,
name|entry
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"The previous range for {} has been finished, pass"
argument_list|,
name|entry
argument_list|)
expr_stmt|;
name|recordCanPush
argument_list|(
name|encodedNameAsString
argument_list|,
name|seqId
argument_list|,
name|barriers
argument_list|,
name|index
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|public
name|boolean
name|canPush
parameter_list|(
name|Entry
name|entry
parameter_list|,
name|Cell
name|firstCellInEdit
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|encodedNameAsString
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getEncodedRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|seqId
init|=
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getSequenceId
argument_list|()
decl_stmt|;
name|Long
name|canReplicateUnderSeqId
init|=
name|canPushUnder
operator|.
name|getIfPresent
argument_list|(
name|encodedNameAsString
argument_list|)
decl_stmt|;
if|if
condition|(
name|canReplicateUnderSeqId
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|seqId
operator|<
name|canReplicateUnderSeqId
operator|.
name|longValue
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"{} is before the end barrier {}, pass"
argument_list|,
name|entry
argument_list|,
name|canReplicateUnderSeqId
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"{} is beyond the previous end barrier {}, remove from cache"
argument_list|,
name|entry
argument_list|,
name|canReplicateUnderSeqId
argument_list|)
expr_stmt|;
comment|// we are already beyond the last safe point, remove
name|canPushUnder
operator|.
name|invalidate
argument_list|(
name|encodedNameAsString
argument_list|)
expr_stmt|;
block|}
comment|// This is for the case where the region is currently opened on us, if the sequence id is
comment|// continuous then we are safe to replicate. If there is a breakpoint, then maybe the region
comment|// has been moved to another RS and then back, so we need to check the barrier.
name|MutableLong
name|previousPushedSeqId
init|=
name|pushed
operator|.
name|getUnchecked
argument_list|(
name|encodedNameAsString
argument_list|)
decl_stmt|;
if|if
condition|(
name|seqId
operator|==
name|previousPushedSeqId
operator|.
name|longValue
argument_list|()
operator|+
literal|1
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"The sequence id for {} is continuous, pass"
argument_list|,
name|entry
argument_list|)
expr_stmt|;
name|previousPushedSeqId
operator|.
name|increment
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
name|canPush
argument_list|(
name|entry
argument_list|,
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|firstCellInEdit
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|void
name|waitUntilCanPush
parameter_list|(
name|Entry
name|entry
parameter_list|,
name|Cell
name|firstCellInEdit
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|byte
index|[]
name|row
init|=
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|firstCellInEdit
argument_list|)
decl_stmt|;
while|while
condition|(
operator|!
name|canPush
argument_list|(
name|entry
argument_list|,
name|row
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Can not push {}, wait"
argument_list|,
name|entry
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|waitTimeMs
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

