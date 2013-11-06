begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Collections
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
name|SortedSet
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
name|KeyValueUtil
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
name|Scan
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
name|monitoring
operator|.
name|MonitoredTask
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
name|compactions
operator|.
name|Compactor
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
name|CollectionBackedScanner
import|;
end_import

begin_comment
comment|/**  * Store flusher interface. Turns a snapshot of memstore into a set of store files (usually one).  * Custom implementation can be provided.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|abstract
class|class
name|StoreFlusher
block|{
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|protected
name|Store
name|store
decl_stmt|;
specifier|public
name|StoreFlusher
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Store
name|store
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
name|store
operator|=
name|store
expr_stmt|;
block|}
comment|/**    * Turns a snapshot of memstore into a set of store files.    * @param snapshot Memstore snapshot.    * @param cacheFlushSeqNum Log cache flush sequence number.    * @param snapshotTimeRangeTracker Time range tracker from the memstore    *                                 pertaining to the snapshot.    * @param flushedSize Out parameter for the size of the KVs flushed.    * @param status Task that represents the flush operation and may be updated with status.    * @return List of files written. Can be empty; must not be null.    */
specifier|public
specifier|abstract
name|List
argument_list|<
name|Path
argument_list|>
name|flushSnapshot
parameter_list|(
name|SortedSet
argument_list|<
name|KeyValue
argument_list|>
name|snapshot
parameter_list|,
name|long
name|cacheFlushSeqNum
parameter_list|,
name|TimeRangeTracker
name|snapshotTimeRangeTracker
parameter_list|,
name|AtomicLong
name|flushedSize
parameter_list|,
name|MonitoredTask
name|status
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|protected
name|void
name|finalizeWriter
parameter_list|(
name|StoreFile
operator|.
name|Writer
name|writer
parameter_list|,
name|long
name|cacheFlushSeqNum
parameter_list|,
name|MonitoredTask
name|status
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Write out the log sequence number that corresponds to this output
comment|// hfile. Also write current time in metadata as minFlushTime.
comment|// The hfile is current up to and including cacheFlushSeqNum.
name|status
operator|.
name|setStatus
argument_list|(
literal|"Flushing "
operator|+
name|store
operator|+
literal|": appending metadata"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|appendMetadata
argument_list|(
name|cacheFlushSeqNum
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|status
operator|.
name|setStatus
argument_list|(
literal|"Flushing "
operator|+
name|store
operator|+
literal|": closing flushed file"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Creates the scanner for flushing snapshot. Also calls coprocessors.    * @return The scanner; null if coprocessor is canceling the flush.    */
specifier|protected
name|InternalScanner
name|createScanner
parameter_list|(
name|SortedSet
argument_list|<
name|KeyValue
argument_list|>
name|snapshot
parameter_list|,
name|long
name|smallestReadPoint
parameter_list|)
throws|throws
name|IOException
block|{
name|KeyValueScanner
name|memstoreScanner
init|=
operator|new
name|CollectionBackedScanner
argument_list|(
name|snapshot
argument_list|,
name|store
operator|.
name|getComparator
argument_list|()
argument_list|)
decl_stmt|;
name|InternalScanner
name|scanner
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|store
operator|.
name|getCoprocessorHost
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|scanner
operator|=
name|store
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|preFlushScannerOpen
argument_list|(
name|store
argument_list|,
name|memstoreScanner
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|scanner
operator|==
literal|null
condition|)
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setMaxVersions
argument_list|(
name|store
operator|.
name|getScanInfo
argument_list|()
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
name|scanner
operator|=
operator|new
name|StoreScanner
argument_list|(
name|store
argument_list|,
name|store
operator|.
name|getScanInfo
argument_list|()
argument_list|,
name|scan
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|memstoreScanner
argument_list|)
argument_list|,
name|ScanType
operator|.
name|COMPACT_RETAIN_DELETES
argument_list|,
name|smallestReadPoint
argument_list|,
name|HConstants
operator|.
name|OLDEST_TIMESTAMP
argument_list|)
expr_stmt|;
block|}
assert|assert
name|scanner
operator|!=
literal|null
assert|;
if|if
condition|(
name|store
operator|.
name|getCoprocessorHost
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|store
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|preFlush
argument_list|(
name|store
argument_list|,
name|scanner
argument_list|)
return|;
block|}
return|return
name|scanner
return|;
block|}
comment|/**    * Performs memstore flush, writing data from scanner into sink.    * @param scanner Scanner to get data from.    * @param sink Sink to write data to. Could be StoreFile.Writer.    * @param smallestReadPoint Smallest read point used for the flush.    * @return Bytes flushed.    */
specifier|protected
name|long
name|performFlush
parameter_list|(
name|InternalScanner
name|scanner
parameter_list|,
name|Compactor
operator|.
name|CellSink
name|sink
parameter_list|,
name|long
name|smallestReadPoint
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|compactionKVMax
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|COMPACTION_KV_MAX
argument_list|,
name|HConstants
operator|.
name|COMPACTION_KV_MAX_DEFAULT
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|kvs
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|hasMore
decl_stmt|;
name|long
name|flushed
init|=
literal|0
decl_stmt|;
do|do
block|{
name|hasMore
operator|=
name|scanner
operator|.
name|next
argument_list|(
name|kvs
argument_list|,
name|compactionKVMax
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|kvs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|Cell
name|c
range|:
name|kvs
control|)
block|{
comment|// If we know that this KV is going to be included always, then let us
comment|// set its memstoreTS to 0. This will help us save space when writing to
comment|// disk.
name|KeyValue
name|kv
init|=
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|c
argument_list|)
decl_stmt|;
if|if
condition|(
name|kv
operator|.
name|getMvccVersion
argument_list|()
operator|<=
name|smallestReadPoint
condition|)
block|{
comment|// let us not change the original KV. It could be in the memstore
comment|// changing its memstoreTS could affect other threads/scanners.
name|kv
operator|=
name|kv
operator|.
name|shallowCopy
argument_list|()
expr_stmt|;
name|kv
operator|.
name|setMvccVersion
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|sink
operator|.
name|append
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|flushed
operator|+=
name|MemStore
operator|.
name|heapSizeChange
argument_list|(
name|kv
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|kvs
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
do|while
condition|(
name|hasMore
condition|)
do|;
return|return
name|flushed
return|;
block|}
block|}
end_class

end_unit

