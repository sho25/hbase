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
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
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
name|ConcurrentModificationException
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
name|BlockingQueue
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
name|LinkedBlockingQueue
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|locks
operator|.
name|ReentrantLock
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
name|DroppedSnapshotException
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
name|HBaseConfiguration
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
name|RemoteExceptionHandler
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * Thread that flushes cache on request  *  * NOTE: This class extends Thread rather than Chore because the sleep time  * can be interrupted when there is something to do, rather than the Chore  * sleep time which is invariant.  *   * @see FlushRequester  */
end_comment

begin_class
class|class
name|MemStoreFlusher
extends|extends
name|Thread
implements|implements
name|FlushRequester
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
name|MemStoreFlusher
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|BlockingQueue
argument_list|<
name|HRegion
argument_list|>
name|flushQueue
init|=
operator|new
name|LinkedBlockingQueue
argument_list|<
name|HRegion
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|HashSet
argument_list|<
name|HRegion
argument_list|>
name|regionsInQueue
init|=
operator|new
name|HashSet
argument_list|<
name|HRegion
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|long
name|threadWakeFrequency
decl_stmt|;
specifier|private
specifier|final
name|HRegionServer
name|server
decl_stmt|;
specifier|private
specifier|final
name|ReentrantLock
name|lock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
name|long
name|globalMemStoreLimit
decl_stmt|;
specifier|protected
specifier|final
name|long
name|globalMemStoreLimitLowMark
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|float
name|DEFAULT_UPPER
init|=
literal|0.4f
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|float
name|DEFAULT_LOWER
init|=
literal|0.25f
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|UPPER_KEY
init|=
literal|"hbase.regionserver.global.memstore.upperLimit"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|LOWER_KEY
init|=
literal|"hbase.regionserver.global.memstore.lowerLimit"
decl_stmt|;
specifier|private
name|long
name|blockingStoreFilesNumber
decl_stmt|;
specifier|private
name|long
name|blockingWaitTime
decl_stmt|;
comment|/**    * @param conf    * @param server    */
specifier|public
name|MemStoreFlusher
parameter_list|(
specifier|final
name|HBaseConfiguration
name|conf
parameter_list|,
specifier|final
name|HRegionServer
name|server
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|server
operator|=
name|server
expr_stmt|;
name|this
operator|.
name|threadWakeFrequency
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|HConstants
operator|.
name|THREAD_WAKE_FREQUENCY
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|long
name|max
init|=
name|ManagementFactory
operator|.
name|getMemoryMXBean
argument_list|()
operator|.
name|getHeapMemoryUsage
argument_list|()
operator|.
name|getMax
argument_list|()
decl_stmt|;
name|this
operator|.
name|globalMemStoreLimit
operator|=
name|globalMemStoreLimit
argument_list|(
name|max
argument_list|,
name|DEFAULT_UPPER
argument_list|,
name|UPPER_KEY
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|long
name|lower
init|=
name|globalMemStoreLimit
argument_list|(
name|max
argument_list|,
name|DEFAULT_LOWER
argument_list|,
name|LOWER_KEY
argument_list|,
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|lower
operator|>
name|this
operator|.
name|globalMemStoreLimit
condition|)
block|{
name|lower
operator|=
name|this
operator|.
name|globalMemStoreLimit
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting globalMemStoreLimitLowMark == globalMemStoreLimit "
operator|+
literal|"because supplied "
operator|+
name|LOWER_KEY
operator|+
literal|" was> "
operator|+
name|UPPER_KEY
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|globalMemStoreLimitLowMark
operator|=
name|lower
expr_stmt|;
name|this
operator|.
name|blockingStoreFilesNumber
operator|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.hstore.blockingStoreFiles"
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|blockingStoreFilesNumber
operator|==
operator|-
literal|1
condition|)
block|{
name|this
operator|.
name|blockingStoreFilesNumber
operator|=
literal|1
operator|+
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.hstore.compactionThreshold"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|blockingWaitTime
operator|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.hstore.blockingWaitTime"
argument_list|,
literal|90000
argument_list|)
expr_stmt|;
comment|// default of 180 seconds
name|LOG
operator|.
name|info
argument_list|(
literal|"globalMemStoreLimit="
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|this
operator|.
name|globalMemStoreLimit
argument_list|)
operator|+
literal|", globalMemStoreLimitLowMark="
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|this
operator|.
name|globalMemStoreLimitLowMark
argument_list|)
operator|+
literal|", maxHeap="
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|max
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Calculate size using passed<code>key</code> for configured    * percentage of<code>max</code>.    * @param max    * @param defaultLimit    * @param key    * @param c    * @return Limit.    */
specifier|static
name|long
name|globalMemStoreLimit
parameter_list|(
specifier|final
name|long
name|max
parameter_list|,
specifier|final
name|float
name|defaultLimit
parameter_list|,
specifier|final
name|String
name|key
parameter_list|,
specifier|final
name|HBaseConfiguration
name|c
parameter_list|)
block|{
name|float
name|limit
init|=
name|c
operator|.
name|getFloat
argument_list|(
name|key
argument_list|,
name|defaultLimit
argument_list|)
decl_stmt|;
return|return
name|getMemStoreLimit
argument_list|(
name|max
argument_list|,
name|limit
argument_list|,
name|defaultLimit
argument_list|)
return|;
block|}
specifier|static
name|long
name|getMemStoreLimit
parameter_list|(
specifier|final
name|long
name|max
parameter_list|,
specifier|final
name|float
name|limit
parameter_list|,
specifier|final
name|float
name|defaultLimit
parameter_list|)
block|{
if|if
condition|(
name|limit
operator|>=
literal|0.9f
operator|||
name|limit
operator|<
literal|0.1f
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Setting global memstore limit to default of "
operator|+
name|defaultLimit
operator|+
literal|" because supplied value outside allowed range of 0.1 -> 0.9"
argument_list|)
expr_stmt|;
block|}
return|return
call|(
name|long
call|)
argument_list|(
name|max
operator|*
name|limit
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
operator|!
name|this
operator|.
name|server
operator|.
name|isStopRequested
argument_list|()
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|threadWakeFrequency
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
continue|continue;
block|}
block|}
while|while
condition|(
operator|!
name|server
operator|.
name|isStopRequested
argument_list|()
condition|)
block|{
name|HRegion
name|r
init|=
literal|null
decl_stmt|;
try|try
block|{
name|r
operator|=
name|flushQueue
operator|.
name|poll
argument_list|(
name|threadWakeFrequency
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
if|if
condition|(
name|r
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
operator|!
name|flushRegion
argument_list|(
name|r
argument_list|,
literal|false
argument_list|)
condition|)
block|{
break|break;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
continue|continue;
block|}
catch|catch
parameter_list|(
name|ConcurrentModificationException
name|ex
parameter_list|)
block|{
continue|continue;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Cache flush failed"
operator|+
operator|(
name|r
operator|!=
literal|null
condition|?
operator|(
literal|" for region "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|r
operator|.
name|getRegionName
argument_list|()
argument_list|)
operator|)
else|:
literal|""
operator|)
argument_list|,
name|ex
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|server
operator|.
name|checkFileSystem
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
name|regionsInQueue
operator|.
name|clear
argument_list|()
expr_stmt|;
name|flushQueue
operator|.
name|clear
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|getName
argument_list|()
operator|+
literal|" exiting"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|request
parameter_list|(
name|HRegion
name|r
parameter_list|)
block|{
synchronized|synchronized
init|(
name|regionsInQueue
init|)
block|{
if|if
condition|(
operator|!
name|regionsInQueue
operator|.
name|contains
argument_list|(
name|r
argument_list|)
condition|)
block|{
name|regionsInQueue
operator|.
name|add
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|flushQueue
operator|.
name|add
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Only interrupt once it's done with a run through the work loop.    */
name|void
name|interruptIfNecessary
parameter_list|()
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|this
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|/*    * Flush a region.    *     * @param region the region to be flushed    * @param removeFromQueue True if the region needs to be removed from the    * flush queue. False if called from the main flusher run loop and true if    * called from flushSomeRegions to relieve memory pressure from the region    * server.  If<code>true</code>, we are in a state of emergency; we are not    * taking on updates regionserver-wide, not until memory is flushed.  In this    * case, do not let a compaction run inline with blocked updates. Compactions    * can take a long time. Stopping compactions, there is a danger that number    * of flushes will overwhelm compaction on a busy server; we'll have to see.    * That compactions do not run when called out of flushSomeRegions means that    * compactions can be reported by the historian without danger of deadlock    * (HBASE-670).    *     *<p>In the main run loop, regions have already been removed from the flush    * queue, and if this method is called for the relief of memory pressure,    * this may not be necessarily true. We want to avoid trying to remove     * region from the queue because if it has already been removed, it requires a    * sequential scan of the queue to determine that it is not in the queue.    *     *<p>If called from flushSomeRegions, the region may be in the queue but    * it may have been determined that the region had a significant amount of     * memory in use and needed to be flushed to relieve memory pressure. In this    * case, its flush may preempt the pending request in the queue, and if so,    * it needs to be removed from the queue to avoid flushing the region    * multiple times.    *     * @return true if the region was successfully flushed, false otherwise. If     * false, there will be accompanying log messages explaining why the log was    * not flushed.    */
specifier|private
name|boolean
name|flushRegion
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|boolean
name|removeFromQueue
parameter_list|)
block|{
name|checkStoreFileCount
argument_list|(
name|region
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|regionsInQueue
init|)
block|{
comment|// See comment above for removeFromQueue on why we do not
comment|// take the region out of the set. If removeFromQueue is true, remove it
comment|// from the queue too if it is there. This didn't used to be a
comment|// constraint, but now that HBASE-512 is in play, we need to try and
comment|// limit double-flushing of regions.
if|if
condition|(
name|regionsInQueue
operator|.
name|remove
argument_list|(
name|region
argument_list|)
operator|&&
name|removeFromQueue
condition|)
block|{
name|flushQueue
operator|.
name|remove
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
block|}
try|try
block|{
comment|// See comment above for removeFromQueue on why we do not
comment|// compact if removeFromQueue is true. Note that region.flushCache()
comment|// only returns true if a flush is done and if a compaction is needed.
if|if
condition|(
name|region
operator|.
name|flushcache
argument_list|()
operator|&&
operator|!
name|removeFromQueue
condition|)
block|{
name|server
operator|.
name|compactSplitThread
operator|.
name|compactionRequested
argument_list|(
name|region
argument_list|,
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|DroppedSnapshotException
name|ex
parameter_list|)
block|{
comment|// Cache flush can fail in a few places. If it fails in a critical
comment|// section, we get a DroppedSnapshotException and a replay of hlog
comment|// is required. Currently the only way to do this is a restart of
comment|// the server. Abort because hdfs is probably bad (HBASE-644 is a case
comment|// where hdfs was bad but passed the hdfs check).
name|LOG
operator|.
name|fatal
argument_list|(
literal|"Replay of hlog required. Forcing server shutdown"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|server
operator|.
name|abort
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
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
literal|"Cache flush failed"
operator|+
operator|(
name|region
operator|!=
literal|null
condition|?
operator|(
literal|" for region "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|region
operator|.
name|getRegionName
argument_list|()
argument_list|)
operator|)
else|:
literal|""
operator|)
argument_list|,
name|RemoteExceptionHandler
operator|.
name|checkIOException
argument_list|(
name|ex
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|server
operator|.
name|checkFileSystem
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
comment|/*    * If too many store files already, schedule a compaction and pause a while    * before going on with compaction.    * @param region Region to check.    */
specifier|private
name|void
name|checkStoreFileCount
parameter_list|(
specifier|final
name|HRegion
name|region
parameter_list|)
block|{
comment|// If catalog region, do not ever hold up writes (isMetaRegion returns
comment|// true if ROOT or META region).
if|if
condition|(
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|isMetaRegion
argument_list|()
condition|)
return|return;
name|int
name|count
init|=
literal|0
decl_stmt|;
name|boolean
name|triggered
init|=
literal|false
decl_stmt|;
name|boolean
name|finished
init|=
literal|false
decl_stmt|;
while|while
condition|(
name|count
operator|++
operator|<
operator|(
name|blockingWaitTime
operator|/
literal|500
operator|)
condition|)
block|{
name|finished
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|Store
name|hstore
range|:
name|region
operator|.
name|stores
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|hstore
operator|.
name|getStorefilesCount
argument_list|()
operator|>
name|this
operator|.
name|blockingStoreFilesNumber
condition|)
block|{
comment|// only log once
if|if
condition|(
operator|!
name|triggered
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Too many store files for region "
operator|+
name|region
operator|+
literal|": "
operator|+
name|hstore
operator|.
name|getStorefilesCount
argument_list|()
operator|+
literal|", requesting compaction and "
operator|+
literal|"waiting"
argument_list|)
expr_stmt|;
name|this
operator|.
name|server
operator|.
name|compactSplitThread
operator|.
name|compactionRequested
argument_list|(
name|region
argument_list|,
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|triggered
operator|=
literal|true
expr_stmt|;
block|}
comment|// pending compaction, not finished
name|finished
operator|=
literal|false
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|500
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
if|if
condition|(
name|triggered
operator|&&
name|finished
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Compaction has completed, we waited "
operator|+
operator|(
name|count
operator|*
literal|500
operator|)
operator|+
literal|"ms, "
operator|+
literal|"finishing flush of region "
operator|+
name|region
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|triggered
operator|&&
operator|!
name|finished
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Tried to hold up flushing for compactions of region "
operator|+
name|region
operator|+
literal|" but have waited longer than "
operator|+
name|blockingWaitTime
operator|+
literal|"ms, continuing"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Check if the regionserver's memstore memory usage is greater than the     * limit. If so, flush regions with the biggest memstores until we're down    * to the lower limit. This method blocks callers until we're down to a safe    * amount of memstore consumption.    */
specifier|public
specifier|synchronized
name|void
name|reclaimMemStoreMemory
parameter_list|()
block|{
if|if
condition|(
name|server
operator|.
name|getGlobalMemStoreSize
argument_list|()
operator|>=
name|globalMemStoreLimit
condition|)
block|{
name|flushSomeRegions
argument_list|()
expr_stmt|;
block|}
block|}
comment|/*    * Emergency!  Need to flush memory.    */
specifier|private
specifier|synchronized
name|void
name|flushSomeRegions
parameter_list|()
block|{
comment|// keep flushing until we hit the low water mark
name|long
name|globalMemStoreSize
init|=
operator|-
literal|1
decl_stmt|;
name|ArrayList
argument_list|<
name|HRegion
argument_list|>
name|regionsToCompact
init|=
operator|new
name|ArrayList
argument_list|<
name|HRegion
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|SortedMap
argument_list|<
name|Long
argument_list|,
name|HRegion
argument_list|>
name|m
init|=
name|this
operator|.
name|server
operator|.
name|getCopyOfOnlineRegionsSortedBySize
argument_list|()
init|;
operator|(
name|globalMemStoreSize
operator|=
name|server
operator|.
name|getGlobalMemStoreSize
argument_list|()
operator|)
operator|>=
name|this
operator|.
name|globalMemStoreLimitLowMark
condition|;
control|)
block|{
comment|// flush the region with the biggest memstore
if|if
condition|(
name|m
operator|.
name|size
argument_list|()
operator|<=
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"No online regions to flush though we've been asked flush "
operator|+
literal|"some; globalMemStoreSize="
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|globalMemStoreSize
argument_list|)
operator|+
literal|", globalMemStoreLimitLowMark="
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|this
operator|.
name|globalMemStoreLimitLowMark
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
name|HRegion
name|biggestMemStoreRegion
init|=
name|m
operator|.
name|remove
argument_list|(
name|m
operator|.
name|firstKey
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Forced flushing of "
operator|+
name|biggestMemStoreRegion
operator|.
name|toString
argument_list|()
operator|+
literal|" because global memstore limit of "
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|this
operator|.
name|globalMemStoreLimit
argument_list|)
operator|+
literal|" exceeded; currently "
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|globalMemStoreSize
argument_list|)
operator|+
literal|" and flushing till "
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|this
operator|.
name|globalMemStoreLimitLowMark
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|flushRegion
argument_list|(
name|biggestMemStoreRegion
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Flush failed"
argument_list|)
expr_stmt|;
break|break;
block|}
name|regionsToCompact
operator|.
name|add
argument_list|(
name|biggestMemStoreRegion
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|HRegion
name|region
range|:
name|regionsToCompact
control|)
block|{
name|server
operator|.
name|compactSplitThread
operator|.
name|compactionRequested
argument_list|(
name|region
argument_list|,
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

