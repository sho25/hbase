begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ReplicationEndpoint
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
name|ReplicationSourceWALReaderThread
operator|.
name|WALEntryBatch
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
name|EnvironmentEdgeManager
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

begin_import
import|import
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
comment|/**  * This thread reads entries from a queue and ships them. Entries are placed onto the queue by  * ReplicationSourceWALReaderThread  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReplicationSourceShipperThread
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
name|ReplicationSourceShipperThread
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|protected
specifier|final
name|String
name|walGroupId
decl_stmt|;
specifier|protected
specifier|final
name|PriorityBlockingQueue
argument_list|<
name|Path
argument_list|>
name|queue
decl_stmt|;
specifier|protected
specifier|final
name|ReplicationSourceInterface
name|source
decl_stmt|;
comment|// Last position in the log that we sent to ZooKeeper
specifier|protected
name|long
name|lastLoggedPosition
init|=
operator|-
literal|1
decl_stmt|;
comment|// Path of the current log
specifier|protected
specifier|volatile
name|Path
name|currentPath
decl_stmt|;
comment|// Indicates whether this particular worker is running
specifier|private
name|boolean
name|workerRunning
init|=
literal|true
decl_stmt|;
specifier|protected
name|ReplicationSourceWALReaderThread
name|entryReader
decl_stmt|;
comment|// How long should we sleep for each retry
specifier|protected
specifier|final
name|long
name|sleepForRetries
decl_stmt|;
comment|// Maximum number of retries before taking bold actions
specifier|protected
specifier|final
name|int
name|maxRetriesMultiplier
decl_stmt|;
comment|// Use guava cache to set ttl for each key
specifier|private
specifier|final
name|LoadingCache
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
name|canSkipWaitingSet
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
name|Boolean
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Boolean
name|load
parameter_list|(
name|String
name|key
parameter_list|)
throws|throws
name|Exception
block|{
return|return
literal|false
return|;
block|}
block|}
argument_list|)
decl_stmt|;
specifier|public
name|ReplicationSourceShipperThread
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|walGroupId
parameter_list|,
name|PriorityBlockingQueue
argument_list|<
name|Path
argument_list|>
name|queue
parameter_list|,
name|ReplicationSourceInterface
name|source
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|walGroupId
operator|=
name|walGroupId
expr_stmt|;
name|this
operator|.
name|queue
operator|=
name|queue
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|source
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
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
comment|// Loop until we close down
while|while
condition|(
name|isActive
argument_list|()
condition|)
block|{
name|int
name|sleepMultiplier
init|=
literal|1
decl_stmt|;
comment|// Sleep until replication is enabled again
if|if
condition|(
operator|!
name|source
operator|.
name|isPeerEnabled
argument_list|()
condition|)
block|{
if|if
condition|(
name|sleepForRetries
argument_list|(
literal|"Replication is disabled"
argument_list|,
name|sleepMultiplier
argument_list|)
condition|)
block|{
name|sleepMultiplier
operator|++
expr_stmt|;
block|}
continue|continue;
block|}
while|while
condition|(
name|entryReader
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|sleepForRetries
argument_list|(
literal|"Replication WAL entry reader thread not initialized"
argument_list|,
name|sleepMultiplier
argument_list|)
condition|)
block|{
name|sleepMultiplier
operator|++
expr_stmt|;
block|}
block|}
try|try
block|{
name|WALEntryBatch
name|entryBatch
init|=
name|entryReader
operator|.
name|take
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|entry
range|:
name|entryBatch
operator|.
name|getLastSeqIds
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|waitingUntilCanPush
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
name|shipEdits
argument_list|(
name|entryBatch
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
literal|"Interrupted while waiting for next replication entry batch"
argument_list|,
name|e
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
comment|/**    * Do the shipping logic    */
specifier|protected
name|void
name|shipEdits
parameter_list|(
name|WALEntryBatch
name|entryBatch
parameter_list|)
block|{
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
init|=
name|entryBatch
operator|.
name|getWalEntries
argument_list|()
decl_stmt|;
name|long
name|lastReadPosition
init|=
name|entryBatch
operator|.
name|getLastWalPosition
argument_list|()
decl_stmt|;
name|currentPath
operator|=
name|entryBatch
operator|.
name|getLastWalPath
argument_list|()
expr_stmt|;
name|int
name|sleepMultiplier
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|entries
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|lastLoggedPosition
operator|!=
name|lastReadPosition
condition|)
block|{
comment|// Save positions to meta table before zk.
name|updateSerialRepPositions
argument_list|(
name|entryBatch
operator|.
name|getLastSeqIds
argument_list|()
argument_list|)
expr_stmt|;
name|updateLogPosition
argument_list|(
name|lastReadPosition
argument_list|)
expr_stmt|;
comment|// if there was nothing to ship and it's not an error
comment|// set "ageOfLastShippedOp" to<now> to indicate that we're current
name|source
operator|.
name|getSourceMetrics
argument_list|()
operator|.
name|setAgeOfLastShippedOp
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|,
name|walGroupId
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
name|int
name|currentSize
init|=
operator|(
name|int
operator|)
name|entryBatch
operator|.
name|getHeapSize
argument_list|()
decl_stmt|;
while|while
condition|(
name|isActive
argument_list|()
condition|)
block|{
try|try
block|{
try|try
block|{
name|source
operator|.
name|tryThrottle
argument_list|(
name|currentSize
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
name|debug
argument_list|(
literal|"Interrupted while sleeping for throttling control"
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
comment|// current thread might be interrupted to terminate
comment|// directly go back to while() for confirm this
continue|continue;
block|}
comment|// create replicateContext here, so the entries can be GC'd upon return from this call
comment|// stack
name|ReplicationEndpoint
operator|.
name|ReplicateContext
name|replicateContext
init|=
operator|new
name|ReplicationEndpoint
operator|.
name|ReplicateContext
argument_list|()
decl_stmt|;
name|replicateContext
operator|.
name|setEntries
argument_list|(
name|entries
argument_list|)
operator|.
name|setSize
argument_list|(
name|currentSize
argument_list|)
expr_stmt|;
name|replicateContext
operator|.
name|setWalGroupId
argument_list|(
name|walGroupId
argument_list|)
expr_stmt|;
name|long
name|startTimeNs
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
comment|// send the edits to the endpoint. Will block until the edits are shipped and acknowledged
name|boolean
name|replicated
init|=
name|source
operator|.
name|getReplicationEndpoint
argument_list|()
operator|.
name|replicate
argument_list|(
name|replicateContext
argument_list|)
decl_stmt|;
name|long
name|endTimeNs
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|replicated
condition|)
block|{
continue|continue;
block|}
else|else
block|{
name|sleepMultiplier
operator|=
name|Math
operator|.
name|max
argument_list|(
name|sleepMultiplier
operator|-
literal|1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|lastLoggedPosition
operator|!=
name|lastReadPosition
condition|)
block|{
comment|//Clean up hfile references
name|int
name|size
init|=
name|entries
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|cleanUpHFileRefs
argument_list|(
name|entries
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getEdit
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Save positions to meta table before zk.
name|updateSerialRepPositions
argument_list|(
name|entryBatch
operator|.
name|getLastSeqIds
argument_list|()
argument_list|)
expr_stmt|;
comment|//Log and clean up WAL logs
name|updateLogPosition
argument_list|(
name|lastReadPosition
argument_list|)
expr_stmt|;
block|}
name|source
operator|.
name|postShipEdits
argument_list|(
name|entries
argument_list|,
name|currentSize
argument_list|)
expr_stmt|;
comment|// FIXME check relationship between wal group and overall
name|source
operator|.
name|getSourceMetrics
argument_list|()
operator|.
name|shipBatch
argument_list|(
name|entryBatch
operator|.
name|getNbOperations
argument_list|()
argument_list|,
name|currentSize
argument_list|,
name|entryBatch
operator|.
name|getNbHFiles
argument_list|()
argument_list|)
expr_stmt|;
name|source
operator|.
name|getSourceMetrics
argument_list|()
operator|.
name|setAgeOfLastShippedOp
argument_list|(
name|entries
operator|.
name|get
argument_list|(
name|entries
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|getKey
argument_list|()
operator|.
name|getWriteTime
argument_list|()
argument_list|,
name|walGroupId
argument_list|)
expr_stmt|;
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
literal|"Replicated "
operator|+
name|entries
operator|.
name|size
argument_list|()
operator|+
literal|" entries or "
operator|+
name|entryBatch
operator|.
name|getNbOperations
argument_list|()
operator|+
literal|" operations in "
operator|+
operator|(
operator|(
name|endTimeNs
operator|-
name|startTimeNs
operator|)
operator|/
literal|1000000
operator|)
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|source
operator|.
name|getReplicationEndpoint
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" threw unknown exception:"
operator|+
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|ex
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|sleepForRetries
argument_list|(
literal|"ReplicationEndpoint threw exception"
argument_list|,
name|sleepMultiplier
argument_list|)
condition|)
block|{
name|sleepMultiplier
operator|++
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|waitingUntilCanPush
parameter_list|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|entry
parameter_list|)
block|{
name|String
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|long
name|seq
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|boolean
name|deleteKey
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|seq
operator|<=
literal|0
condition|)
block|{
comment|// There is a REGION_CLOSE marker, we can not continue skipping after this entry.
name|deleteKey
operator|=
literal|true
expr_stmt|;
name|seq
operator|=
operator|-
name|seq
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|canSkipWaitingSet
operator|.
name|getUnchecked
argument_list|(
name|key
argument_list|)
condition|)
block|{
try|try
block|{
name|source
operator|.
name|getSourceManager
argument_list|()
operator|.
name|waitUntilCanBePushed
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|key
argument_list|)
argument_list|,
name|seq
argument_list|,
name|source
operator|.
name|getPeerId
argument_list|()
argument_list|)
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
literal|"waitUntilCanBePushed fail"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"waitUntilCanBePushed fail"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"waitUntilCanBePushed interrupted"
argument_list|,
name|e
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
name|canSkipWaitingSet
operator|.
name|put
argument_list|(
name|key
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|deleteKey
condition|)
block|{
name|canSkipWaitingSet
operator|.
name|invalidate
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|cleanUpHFileRefs
parameter_list|(
name|WALEdit
name|edit
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|peerId
init|=
name|source
operator|.
name|getPeerId
argument_list|()
decl_stmt|;
if|if
condition|(
name|peerId
operator|.
name|contains
argument_list|(
literal|"-"
argument_list|)
condition|)
block|{
comment|// peerClusterZnode will be in the form peerId + "-" + rsZNode.
comment|// A peerId will not have "-" in its name, see HBASE-11394
name|peerId
operator|=
name|peerId
operator|.
name|split
argument_list|(
literal|"-"
argument_list|)
index|[
literal|0
index|]
expr_stmt|;
block|}
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
name|totalCells
init|=
name|cells
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
name|Cell
name|cell
init|=
name|cells
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|cell
argument_list|,
name|WALEdit
operator|.
name|BULK_LOAD
argument_list|)
condition|)
block|{
name|BulkLoadDescriptor
name|bld
init|=
name|WALEdit
operator|.
name|getBulkLoadDescriptor
argument_list|(
name|cell
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
name|List
argument_list|<
name|String
argument_list|>
name|storeFileList
init|=
name|stores
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|getStoreFileList
argument_list|()
decl_stmt|;
name|source
operator|.
name|getSourceManager
argument_list|()
operator|.
name|cleanUpHFileRefs
argument_list|(
name|peerId
argument_list|,
name|storeFileList
argument_list|)
expr_stmt|;
name|source
operator|.
name|getSourceMetrics
argument_list|()
operator|.
name|decrSizeOfHFileRefsQueue
argument_list|(
name|storeFileList
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|protected
name|void
name|updateLogPosition
parameter_list|(
name|long
name|lastReadPosition
parameter_list|)
block|{
name|source
operator|.
name|getSourceManager
argument_list|()
operator|.
name|logPositionAndCleanOldLogs
argument_list|(
name|currentPath
argument_list|,
name|source
operator|.
name|getPeerClusterZnode
argument_list|()
argument_list|,
name|lastReadPosition
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|lastLoggedPosition
operator|=
name|lastReadPosition
expr_stmt|;
block|}
specifier|private
name|void
name|updateSerialRepPositions
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|lastPositionsForSerialScope
parameter_list|)
block|{
try|try
block|{
name|MetaTableAccessor
operator|.
name|updateReplicationPositions
argument_list|(
name|source
operator|.
name|getSourceManager
argument_list|()
operator|.
name|getConnection
argument_list|()
argument_list|,
name|source
operator|.
name|getPeerId
argument_list|()
argument_list|,
name|lastPositionsForSerialScope
argument_list|)
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
literal|"updateReplicationPositions fail"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"updateReplicationPositions fail"
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|startup
parameter_list|(
name|UncaughtExceptionHandler
name|handler
parameter_list|)
block|{
name|String
name|name
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|Threads
operator|.
name|setDaemonThreadRunning
argument_list|(
name|this
argument_list|,
name|name
operator|+
literal|".replicationSource."
operator|+
name|walGroupId
operator|+
literal|","
operator|+
name|source
operator|.
name|getPeerClusterZnode
argument_list|()
argument_list|,
name|handler
argument_list|)
expr_stmt|;
block|}
specifier|public
name|PriorityBlockingQueue
argument_list|<
name|Path
argument_list|>
name|getLogQueue
parameter_list|()
block|{
return|return
name|this
operator|.
name|queue
return|;
block|}
specifier|public
name|Path
name|getCurrentPath
parameter_list|()
block|{
return|return
name|this
operator|.
name|currentPath
return|;
block|}
specifier|public
name|long
name|getCurrentPosition
parameter_list|()
block|{
return|return
name|this
operator|.
name|lastLoggedPosition
return|;
block|}
specifier|public
name|void
name|setWALReader
parameter_list|(
name|ReplicationSourceWALReaderThread
name|entryReader
parameter_list|)
block|{
name|this
operator|.
name|entryReader
operator|=
name|entryReader
expr_stmt|;
block|}
specifier|public
name|long
name|getStartPosition
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
specifier|protected
name|boolean
name|isActive
parameter_list|()
block|{
return|return
name|source
operator|.
name|isSourceActive
argument_list|()
operator|&&
name|workerRunning
operator|&&
operator|!
name|isInterrupted
argument_list|()
return|;
block|}
specifier|public
name|void
name|setWorkerRunning
parameter_list|(
name|boolean
name|workerRunning
parameter_list|)
block|{
name|entryReader
operator|.
name|setReaderRunning
argument_list|(
name|workerRunning
argument_list|)
expr_stmt|;
name|this
operator|.
name|workerRunning
operator|=
name|workerRunning
expr_stmt|;
block|}
comment|/**    * Do the sleeping logic    * @param msg Why we sleep    * @param sleepMultiplier by how many times the default sleeping time is augmented    * @return True if<code>sleepMultiplier</code> is&lt;<code>maxRetriesMultiplier</code>    */
specifier|public
name|boolean
name|sleepForRetries
parameter_list|(
name|String
name|msg
parameter_list|,
name|int
name|sleepMultiplier
parameter_list|)
block|{
try|try
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
name|msg
operator|+
literal|", sleeping "
operator|+
name|sleepForRetries
operator|+
literal|" times "
operator|+
name|sleepMultiplier
argument_list|)
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
name|this
operator|.
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
name|debug
argument_list|(
literal|"Interrupted while sleeping between retries"
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
return|return
name|sleepMultiplier
operator|<
name|maxRetriesMultiplier
return|;
block|}
block|}
end_class

end_unit
