begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ArrayList
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
name|PriorityBlockingQueue
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|classification
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
name|classification
operator|.
name|InterfaceStability
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|WALProtos
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|WALProtos
operator|.
name|BulkLoadDescriptor
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|WALProtos
operator|.
name|StoreDescriptor
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
name|RSRpcServices
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
name|wal
operator|.
name|WALEdit
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
name|ReplicationQueueInfo
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
name|WALEntryFilter
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
name|regionserver
operator|.
name|WALEntryStream
operator|.
name|WALEntryStreamRuntimeException
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
name|Threads
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

begin_comment
comment|/**  * Reads and filters WAL entries, groups the filtered entries into batches, and puts the batches onto a queue  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|ReplicationSourceWALReaderThread
extends|extends
name|Thread
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
name|ReplicationSourceWALReaderThread
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|PriorityBlockingQueue
argument_list|<
name|Path
argument_list|>
name|logQueue
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|BlockingQueue
argument_list|<
name|WALEntryBatch
argument_list|>
name|entryBatchQueue
decl_stmt|;
comment|// max (heap) size of each batch - multiply by number of batches in queue to get total
specifier|private
name|long
name|replicationBatchSizeCapacity
decl_stmt|;
comment|// max count of each batch - multiply by number of batches in queue to get total
specifier|private
name|int
name|replicationBatchCountCapacity
decl_stmt|;
comment|// position in the WAL to start reading at
specifier|private
name|long
name|currentPosition
decl_stmt|;
specifier|private
name|WALEntryFilter
name|filter
decl_stmt|;
specifier|private
name|long
name|sleepForRetries
decl_stmt|;
comment|//Indicates whether this particular worker is running
specifier|private
name|boolean
name|isReaderRunning
init|=
literal|true
decl_stmt|;
specifier|private
name|ReplicationQueueInfo
name|replicationQueueInfo
decl_stmt|;
specifier|private
name|int
name|maxRetriesMultiplier
decl_stmt|;
specifier|private
name|MetricsSource
name|metrics
decl_stmt|;
specifier|private
name|AtomicLong
name|totalBufferUsed
decl_stmt|;
specifier|private
name|long
name|totalBufferQuota
decl_stmt|;
comment|/**    * Creates a reader worker for a given WAL queue. Reads WAL entries off a given queue, batches the    * entries, and puts them on a batch queue.    * @param manager replication manager    * @param replicationQueueInfo    * @param logQueue The WAL queue to read off of    * @param startPosition position in the first WAL to start reading from    * @param fs the files system to use    * @param conf configuration to use    * @param filter The filter to use while reading    * @param metrics replication metrics    */
specifier|public
name|ReplicationSourceWALReaderThread
parameter_list|(
name|ReplicationSourceManager
name|manager
parameter_list|,
name|ReplicationQueueInfo
name|replicationQueueInfo
parameter_list|,
name|PriorityBlockingQueue
argument_list|<
name|Path
argument_list|>
name|logQueue
parameter_list|,
name|long
name|startPosition
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|WALEntryFilter
name|filter
parameter_list|,
name|MetricsSource
name|metrics
parameter_list|)
block|{
name|this
operator|.
name|replicationQueueInfo
operator|=
name|replicationQueueInfo
expr_stmt|;
name|this
operator|.
name|logQueue
operator|=
name|logQueue
expr_stmt|;
name|this
operator|.
name|currentPosition
operator|=
name|startPosition
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
name|this
operator|.
name|replicationBatchSizeCapacity
operator|=
name|this
operator|.
name|conf
operator|.
name|getLong
argument_list|(
literal|"replication.source.size.capacity"
argument_list|,
literal|1024
operator|*
literal|1024
operator|*
literal|64
argument_list|)
expr_stmt|;
name|this
operator|.
name|replicationBatchCountCapacity
operator|=
name|this
operator|.
name|conf
operator|.
name|getInt
argument_list|(
literal|"replication.source.nb.capacity"
argument_list|,
literal|25000
argument_list|)
expr_stmt|;
comment|// memory used will be batchSizeCapacity * (nb.batches + 1)
comment|// the +1 is for the current thread reading before placing onto the queue
name|int
name|batchCount
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"replication.source.nb.batches"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|this
operator|.
name|totalBufferUsed
operator|=
name|manager
operator|.
name|getTotalBufferUsed
argument_list|()
expr_stmt|;
name|this
operator|.
name|totalBufferQuota
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SOURCE_TOTAL_BUFFER_KEY
argument_list|,
name|HConstants
operator|.
name|REPLICATION_SOURCE_TOTAL_BUFFER_DFAULT
argument_list|)
expr_stmt|;
name|this
operator|.
name|sleepForRetries
operator|=
name|this
operator|.
name|conf
operator|.
name|getLong
argument_list|(
literal|"replication.source.sleepforretries"
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
comment|// 1 second
name|this
operator|.
name|maxRetriesMultiplier
operator|=
name|this
operator|.
name|conf
operator|.
name|getInt
argument_list|(
literal|"replication.source.maxretriesmultiplier"
argument_list|,
literal|300
argument_list|)
expr_stmt|;
comment|// 5 minutes @ 1 sec per
name|this
operator|.
name|metrics
operator|=
name|metrics
expr_stmt|;
name|this
operator|.
name|entryBatchQueue
operator|=
operator|new
name|LinkedBlockingQueue
argument_list|<>
argument_list|(
name|batchCount
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"peerClusterZnode="
operator|+
name|replicationQueueInfo
operator|.
name|getPeerClusterZnode
argument_list|()
operator|+
literal|", ReplicationSourceWALReaderThread : "
operator|+
name|replicationQueueInfo
operator|.
name|getPeerId
argument_list|()
operator|+
literal|" inited, replicationBatchSizeCapacity="
operator|+
name|replicationBatchSizeCapacity
operator|+
literal|", replicationBatchCountCapacity="
operator|+
name|replicationBatchCountCapacity
operator|+
literal|", replicationBatchQueueCapacity="
operator|+
name|batchCount
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|int
name|sleepMultiplier
init|=
literal|1
decl_stmt|;
while|while
condition|(
name|isReaderRunning
argument_list|()
condition|)
block|{
comment|// we only loop back here if something fatal happened to our stream
try|try
init|(
name|WALEntryStream
name|entryStream
init|=
operator|new
name|WALEntryStream
argument_list|(
name|logQueue
argument_list|,
name|fs
argument_list|,
name|conf
argument_list|,
name|currentPosition
argument_list|,
name|metrics
argument_list|)
init|)
block|{
while|while
condition|(
name|isReaderRunning
argument_list|()
condition|)
block|{
comment|// loop here to keep reusing stream while we can
if|if
condition|(
operator|!
name|checkQuota
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|WALEntryBatch
name|batch
init|=
literal|null
decl_stmt|;
while|while
condition|(
name|entryStream
operator|.
name|hasNext
argument_list|()
condition|)
block|{
if|if
condition|(
name|batch
operator|==
literal|null
condition|)
block|{
name|batch
operator|=
operator|new
name|WALEntryBatch
argument_list|(
name|replicationBatchCountCapacity
argument_list|,
name|entryStream
operator|.
name|getCurrentPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Entry
name|entry
init|=
name|entryStream
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|updateSerialReplPos
argument_list|(
name|batch
argument_list|,
name|entry
argument_list|)
condition|)
block|{
name|batch
operator|.
name|lastWalPosition
operator|=
name|entryStream
operator|.
name|getPosition
argument_list|()
expr_stmt|;
break|break;
block|}
name|entry
operator|=
name|filterEntry
argument_list|(
name|entry
argument_list|)
expr_stmt|;
if|if
condition|(
name|entry
operator|!=
literal|null
condition|)
block|{
name|WALEdit
name|edit
init|=
name|entry
operator|.
name|getEdit
argument_list|()
decl_stmt|;
if|if
condition|(
name|edit
operator|!=
literal|null
operator|&&
operator|!
name|edit
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|long
name|entrySize
init|=
name|getEntrySize
argument_list|(
name|entry
argument_list|)
decl_stmt|;
name|batch
operator|.
name|addEntry
argument_list|(
name|entry
argument_list|)
expr_stmt|;
name|updateBatchStats
argument_list|(
name|batch
argument_list|,
name|entry
argument_list|,
name|entryStream
operator|.
name|getPosition
argument_list|()
argument_list|,
name|entrySize
argument_list|)
expr_stmt|;
name|boolean
name|totalBufferTooLarge
init|=
name|acquireBufferQuota
argument_list|(
name|entrySize
argument_list|)
decl_stmt|;
comment|// Stop if too many entries or too big
if|if
condition|(
name|totalBufferTooLarge
operator|||
name|batch
operator|.
name|getHeapSize
argument_list|()
operator|>=
name|replicationBatchSizeCapacity
operator|||
name|batch
operator|.
name|getNbEntries
argument_list|()
operator|>=
name|replicationBatchCountCapacity
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
if|if
condition|(
name|batch
operator|!=
literal|null
operator|&&
operator|(
operator|!
name|batch
operator|.
name|getLastSeqIds
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|||
name|batch
operator|.
name|getNbEntries
argument_list|()
operator|>
literal|0
operator|)
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Read %s WAL entries eligible for replication"
argument_list|,
name|batch
operator|.
name|getNbEntries
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|entryBatchQueue
operator|.
name|put
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|sleepMultiplier
operator|=
literal|1
expr_stmt|;
block|}
else|else
block|{
comment|// got no entries and didn't advance position in WAL
name|LOG
operator|.
name|trace
argument_list|(
literal|"Didn't read any new entries from WAL"
argument_list|)
expr_stmt|;
if|if
condition|(
name|replicationQueueInfo
operator|.
name|isQueueRecovered
argument_list|()
condition|)
block|{
comment|// we're done with queue recovery, shut ourself down
name|setReaderRunning
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// shuts down shipper thread immediately
name|entryBatchQueue
operator|.
name|put
argument_list|(
name|batch
operator|!=
literal|null
condition|?
name|batch
else|:
operator|new
name|WALEntryBatch
argument_list|(
name|replicationBatchCountCapacity
argument_list|,
name|entryStream
operator|.
name|getCurrentPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepForRetries
argument_list|)
expr_stmt|;
block|}
block|}
name|currentPosition
operator|=
name|entryStream
operator|.
name|getPosition
argument_list|()
expr_stmt|;
name|entryStream
operator|.
name|reset
argument_list|()
expr_stmt|;
comment|// reuse stream
block|}
block|}
catch|catch
parameter_list|(
name|IOException
decl||
name|WALEntryStreamRuntimeException
name|e
parameter_list|)
block|{
comment|// stream related
if|if
condition|(
name|sleepMultiplier
operator|<
name|maxRetriesMultiplier
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Failed to read stream of replication entries: "
operator|+
name|e
argument_list|)
expr_stmt|;
name|sleepMultiplier
operator|++
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to read stream of replication entries"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|Threads
operator|.
name|sleep
argument_list|(
name|sleepForRetries
operator|*
name|sleepMultiplier
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Interrupted while sleeping between WAL reads"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|//returns false if we've already exceeded the global quota
specifier|private
name|boolean
name|checkQuota
parameter_list|()
block|{
comment|// try not to go over total quota
if|if
condition|(
name|totalBufferUsed
operator|.
name|get
argument_list|()
operator|>
name|totalBufferQuota
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
name|sleepForRetries
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|Entry
name|filterEntry
parameter_list|(
name|Entry
name|entry
parameter_list|)
block|{
name|Entry
name|filtered
init|=
name|filter
operator|.
name|filter
argument_list|(
name|entry
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|!=
literal|null
operator|&&
name|filtered
operator|==
literal|null
condition|)
block|{
name|metrics
operator|.
name|incrLogEditsFiltered
argument_list|()
expr_stmt|;
block|}
return|return
name|filtered
return|;
block|}
comment|/**    * @return true if we should stop reading because we're at REGION_CLOSE    */
specifier|private
name|boolean
name|updateSerialReplPos
parameter_list|(
name|WALEntryBatch
name|batch
parameter_list|,
name|Entry
name|entry
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|entry
operator|.
name|hasSerialReplicationScope
argument_list|()
condition|)
block|{
name|String
name|key
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
name|batch
operator|.
name|setLastPosition
argument_list|(
name|key
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getSequenceId
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|entry
operator|.
name|getEdit
argument_list|()
operator|.
name|getCells
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|WALProtos
operator|.
name|RegionEventDescriptor
name|maybeEvent
init|=
name|WALEdit
operator|.
name|getRegionEventDescriptor
argument_list|(
name|entry
operator|.
name|getEdit
argument_list|()
operator|.
name|getCells
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|maybeEvent
operator|!=
literal|null
operator|&&
name|maybeEvent
operator|.
name|getEventType
argument_list|()
operator|==
name|WALProtos
operator|.
name|RegionEventDescriptor
operator|.
name|EventType
operator|.
name|REGION_CLOSE
condition|)
block|{
comment|// In serially replication, if we move a region to another RS and move it back, we may
comment|// read logs crossing two sections. We should break at REGION_CLOSE and push the first
comment|// section first in case of missing the middle section belonging to the other RS.
comment|// In a worker thread, if we can push the first log of a region, we can push all logs
comment|// in the same region without waiting until we read a close marker because next time
comment|// we read logs in this region, it must be a new section and not adjacent with this
comment|// region. Mark it negative.
name|batch
operator|.
name|setLastPosition
argument_list|(
name|key
argument_list|,
operator|-
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getSequenceId
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Retrieves the next batch of WAL entries from the queue, waiting up to the specified time for a    * batch to become available    * @return A batch of entries, along with the position in the log after reading the batch    * @throws InterruptedException if interrupted while waiting    */
specifier|public
name|WALEntryBatch
name|take
parameter_list|()
throws|throws
name|InterruptedException
block|{
return|return
name|entryBatchQueue
operator|.
name|take
argument_list|()
return|;
block|}
specifier|private
name|long
name|getEntrySize
parameter_list|(
name|Entry
name|entry
parameter_list|)
block|{
name|WALEdit
name|edit
init|=
name|entry
operator|.
name|getEdit
argument_list|()
decl_stmt|;
return|return
name|edit
operator|.
name|heapSize
argument_list|()
operator|+
name|calculateTotalSizeOfStoreFiles
argument_list|(
name|edit
argument_list|)
return|;
block|}
specifier|private
name|void
name|updateBatchStats
parameter_list|(
name|WALEntryBatch
name|batch
parameter_list|,
name|Entry
name|entry
parameter_list|,
name|long
name|entryPosition
parameter_list|,
name|long
name|entrySize
parameter_list|)
block|{
name|WALEdit
name|edit
init|=
name|entry
operator|.
name|getEdit
argument_list|()
decl_stmt|;
if|if
condition|(
name|edit
operator|!=
literal|null
operator|&&
operator|!
name|edit
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|batch
operator|.
name|incrementHeapSize
argument_list|(
name|entrySize
argument_list|)
expr_stmt|;
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|nbRowsAndHFiles
init|=
name|countDistinctRowKeysAndHFiles
argument_list|(
name|edit
argument_list|)
decl_stmt|;
name|batch
operator|.
name|incrementNbRowKeys
argument_list|(
name|nbRowsAndHFiles
operator|.
name|getFirst
argument_list|()
argument_list|)
expr_stmt|;
name|batch
operator|.
name|incrementNbHFiles
argument_list|(
name|nbRowsAndHFiles
operator|.
name|getSecond
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|batch
operator|.
name|lastWalPosition
operator|=
name|entryPosition
expr_stmt|;
block|}
comment|/**    * Count the number of different row keys in the given edit because of mini-batching. We assume    * that there's at least one Cell in the WALEdit.    * @param edit edit to count row keys from    * @return number of different row keys and HFiles    */
specifier|private
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|countDistinctRowKeysAndHFiles
parameter_list|(
name|WALEdit
name|edit
parameter_list|)
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|edit
operator|.
name|getCells
argument_list|()
decl_stmt|;
name|int
name|distinctRowKeys
init|=
literal|1
decl_stmt|;
name|int
name|totalHFileEntries
init|=
literal|0
decl_stmt|;
name|Cell
name|lastCell
init|=
name|cells
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|int
name|totalCells
init|=
name|edit
operator|.
name|size
argument_list|()
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
name|totalCells
condition|;
name|i
operator|++
control|)
block|{
comment|// Count HFiles to be replicated
if|if
condition|(
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|cells
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|WALEdit
operator|.
name|BULK_LOAD
argument_list|)
condition|)
block|{
try|try
block|{
name|BulkLoadDescriptor
name|bld
init|=
name|WALEdit
operator|.
name|getBulkLoadDescriptor
argument_list|(
name|cells
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|StoreDescriptor
argument_list|>
name|stores
init|=
name|bld
operator|.
name|getStoresList
argument_list|()
decl_stmt|;
name|int
name|totalStores
init|=
name|stores
operator|.
name|size
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|totalStores
condition|;
name|j
operator|++
control|)
block|{
name|totalHFileEntries
operator|+=
name|stores
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|getStoreFileList
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
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
literal|"Failed to deserialize bulk load entry from wal edit. "
operator|+
literal|"Then its hfiles count will not be added into metric."
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|CellUtil
operator|.
name|matchingRows
argument_list|(
name|cells
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|lastCell
argument_list|)
condition|)
block|{
name|distinctRowKeys
operator|++
expr_stmt|;
block|}
name|lastCell
operator|=
name|cells
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|result
init|=
operator|new
name|Pair
argument_list|<>
argument_list|(
name|distinctRowKeys
argument_list|,
name|totalHFileEntries
argument_list|)
decl_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * Calculate the total size of all the store files    * @param edit edit to count row keys from    * @return the total size of the store files    */
specifier|private
name|int
name|calculateTotalSizeOfStoreFiles
parameter_list|(
name|WALEdit
name|edit
parameter_list|)
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|edit
operator|.
name|getCells
argument_list|()
decl_stmt|;
name|int
name|totalStoreFilesSize
init|=
literal|0
decl_stmt|;
name|int
name|totalCells
init|=
name|edit
operator|.
name|size
argument_list|()
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
name|totalCells
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|cells
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|WALEdit
operator|.
name|BULK_LOAD
argument_list|)
condition|)
block|{
try|try
block|{
name|BulkLoadDescriptor
name|bld
init|=
name|WALEdit
operator|.
name|getBulkLoadDescriptor
argument_list|(
name|cells
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|StoreDescriptor
argument_list|>
name|stores
init|=
name|bld
operator|.
name|getStoresList
argument_list|()
decl_stmt|;
name|int
name|totalStores
init|=
name|stores
operator|.
name|size
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|totalStores
condition|;
name|j
operator|++
control|)
block|{
name|totalStoreFilesSize
operator|+=
name|stores
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|getStoreFileSizeBytes
argument_list|()
expr_stmt|;
block|}
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
literal|"Failed to deserialize bulk load entry from wal edit. "
operator|+
literal|"Size of HFiles part of cell will not be considered in replication "
operator|+
literal|"request size calculation."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|totalStoreFilesSize
return|;
block|}
comment|/**    * @param size delta size for grown buffer    * @return true if we should clear buffer and push all    */
specifier|private
name|boolean
name|acquireBufferQuota
parameter_list|(
name|long
name|size
parameter_list|)
block|{
return|return
name|totalBufferUsed
operator|.
name|addAndGet
argument_list|(
name|size
argument_list|)
operator|>=
name|totalBufferQuota
return|;
block|}
comment|/**    * @return whether the reader thread is running    */
specifier|public
name|boolean
name|isReaderRunning
parameter_list|()
block|{
return|return
name|isReaderRunning
operator|&&
operator|!
name|isInterrupted
argument_list|()
return|;
block|}
comment|/**    * @param readerRunning the readerRunning to set    */
specifier|public
name|void
name|setReaderRunning
parameter_list|(
name|boolean
name|readerRunning
parameter_list|)
block|{
name|this
operator|.
name|isReaderRunning
operator|=
name|readerRunning
expr_stmt|;
block|}
comment|/**    * Holds a batch of WAL entries to replicate, along with some statistics    *    */
specifier|static
class|class
name|WALEntryBatch
block|{
specifier|private
name|List
argument_list|<
name|Entry
argument_list|>
name|walEntries
decl_stmt|;
comment|// last WAL that was read
specifier|private
name|Path
name|lastWalPath
decl_stmt|;
comment|// position in WAL of last entry in this batch
specifier|private
name|long
name|lastWalPosition
init|=
literal|0
decl_stmt|;
comment|// number of distinct row keys in this batch
specifier|private
name|int
name|nbRowKeys
init|=
literal|0
decl_stmt|;
comment|// number of HFiles
specifier|private
name|int
name|nbHFiles
init|=
literal|0
decl_stmt|;
comment|// heap size of data we need to replicate
specifier|private
name|long
name|heapSize
init|=
literal|0
decl_stmt|;
comment|// save the last sequenceid for each region if the table has serial-replication scope
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|lastSeqIds
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**      * @param walEntries      * @param lastWalPath Path of the WAL the last entry in this batch was read from      * @param lastWalPosition Position in the WAL the last entry in this batch was read from      */
specifier|private
name|WALEntryBatch
parameter_list|(
name|int
name|maxNbEntries
parameter_list|,
name|Path
name|lastWalPath
parameter_list|)
block|{
name|this
operator|.
name|walEntries
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|maxNbEntries
argument_list|)
expr_stmt|;
name|this
operator|.
name|lastWalPath
operator|=
name|lastWalPath
expr_stmt|;
block|}
specifier|public
name|void
name|addEntry
parameter_list|(
name|Entry
name|entry
parameter_list|)
block|{
name|walEntries
operator|.
name|add
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
comment|/**      * @return the WAL Entries.      */
specifier|public
name|List
argument_list|<
name|Entry
argument_list|>
name|getWalEntries
parameter_list|()
block|{
return|return
name|walEntries
return|;
block|}
comment|/**      * @return the path of the last WAL that was read.      */
specifier|public
name|Path
name|getLastWalPath
parameter_list|()
block|{
return|return
name|lastWalPath
return|;
block|}
comment|/**      * @return the position in the last WAL that was read.      */
specifier|public
name|long
name|getLastWalPosition
parameter_list|()
block|{
return|return
name|lastWalPosition
return|;
block|}
specifier|public
name|int
name|getNbEntries
parameter_list|()
block|{
return|return
name|walEntries
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**      * @return the number of distinct row keys in this batch      */
specifier|public
name|int
name|getNbRowKeys
parameter_list|()
block|{
return|return
name|nbRowKeys
return|;
block|}
comment|/**      * @return the number of HFiles in this batch      */
specifier|public
name|int
name|getNbHFiles
parameter_list|()
block|{
return|return
name|nbHFiles
return|;
block|}
comment|/**      * @return total number of operations in this batch      */
specifier|public
name|int
name|getNbOperations
parameter_list|()
block|{
return|return
name|getNbRowKeys
argument_list|()
operator|+
name|getNbHFiles
argument_list|()
return|;
block|}
comment|/**      * @return the heap size of this batch      */
specifier|public
name|long
name|getHeapSize
parameter_list|()
block|{
return|return
name|heapSize
return|;
block|}
comment|/**      * @return the last sequenceid for each region if the table has serial-replication scope      */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|getLastSeqIds
parameter_list|()
block|{
return|return
name|lastSeqIds
return|;
block|}
specifier|private
name|void
name|incrementNbRowKeys
parameter_list|(
name|int
name|increment
parameter_list|)
block|{
name|nbRowKeys
operator|+=
name|increment
expr_stmt|;
block|}
specifier|private
name|void
name|incrementNbHFiles
parameter_list|(
name|int
name|increment
parameter_list|)
block|{
name|nbHFiles
operator|+=
name|increment
expr_stmt|;
block|}
specifier|private
name|void
name|incrementHeapSize
parameter_list|(
name|long
name|increment
parameter_list|)
block|{
name|heapSize
operator|+=
name|increment
expr_stmt|;
block|}
specifier|private
name|void
name|setLastPosition
parameter_list|(
name|String
name|region
parameter_list|,
name|Long
name|sequenceId
parameter_list|)
block|{
name|getLastSeqIds
argument_list|()
operator|.
name|put
argument_list|(
name|region
argument_list|,
name|sequenceId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

