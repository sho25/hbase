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
name|lang
operator|.
name|management
operator|.
name|RuntimeMXBean
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
name|List
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
name|CellComparator
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
name|KeyValue
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
name|ClassSize
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

begin_comment
comment|/**  * The MemStore holds in-memory modifications to the Store.  Modifications  * are {@link Cell}s.  When asked to flush, current memstore is moved  * to snapshot and is cleared.  We continue to serve edits out of new memstore  * and backing snapshot until flusher reports in that the flush succeeded. At  * this point we let the snapshot go.  *<p>  * The MemStore functions should not be called in parallel. Callers should hold  *  write and read locks. This is done in {@link HStore}.  *</p>  *  * TODO: Adjust size of the memstore when we remove items because they have  * been deleted.  * TODO: With new KVSLS, need to make sure we update HeapSize with difference  * in KV size.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DefaultMemStore
extends|extends
name|AbstractMemStore
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
name|DefaultMemStore
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|long
name|DEEP_OVERHEAD
init|=
name|ClassSize
operator|.
name|align
argument_list|(
name|AbstractMemStore
operator|.
name|DEEP_OVERHEAD
argument_list|)
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|long
name|FIXED_OVERHEAD
init|=
name|ClassSize
operator|.
name|align
argument_list|(
name|AbstractMemStore
operator|.
name|FIXED_OVERHEAD
argument_list|)
decl_stmt|;
comment|/**    * Default constructor. Used for tests.    */
specifier|public
name|DefaultMemStore
parameter_list|()
block|{
name|this
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|,
name|CellComparator
operator|.
name|COMPARATOR
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor.    * @param c Comparator    */
specifier|public
name|DefaultMemStore
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|CellComparator
name|c
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|c
argument_list|)
expr_stmt|;
block|}
comment|/**    * Creates a snapshot of the current memstore.    * Snapshot must be cleared by call to {@link #clearSnapshot(long)}    */
annotation|@
name|Override
specifier|public
name|MemStoreSnapshot
name|snapshot
parameter_list|()
block|{
comment|// If snapshot currently has entries, then flusher failed or didn't call
comment|// cleanup.  Log a warning.
if|if
condition|(
operator|!
name|this
operator|.
name|snapshot
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Snapshot called again without clearing previous. "
operator|+
literal|"Doing nothing. Another ongoing flush or did we fail last attempt?"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|snapshotId
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|active
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ImmutableSegment
name|immutableSegment
init|=
name|SegmentFactory
operator|.
name|instance
argument_list|()
operator|.
name|createImmutableSegment
argument_list|(
name|this
operator|.
name|active
argument_list|)
decl_stmt|;
name|this
operator|.
name|snapshot
operator|=
name|immutableSegment
expr_stmt|;
name|resetActive
argument_list|()
expr_stmt|;
block|}
block|}
return|return
operator|new
name|MemStoreSnapshot
argument_list|(
name|this
operator|.
name|snapshotId
argument_list|,
name|this
operator|.
name|snapshot
argument_list|)
return|;
block|}
comment|/**    * On flush, how much memory we will clear from the active cell set.    *    * @return size of data that is going to be flushed from active set    */
annotation|@
name|Override
specifier|public
name|MemStoreSize
name|getFlushableSize
parameter_list|()
block|{
name|MemStoreSize
name|snapshotSize
init|=
name|getSnapshotSize
argument_list|()
decl_stmt|;
return|return
name|snapshotSize
operator|.
name|getDataSize
argument_list|()
operator|>
literal|0
condition|?
name|snapshotSize
else|:
operator|new
name|MemStoreSize
argument_list|(
name|keySize
argument_list|()
argument_list|,
name|heapSize
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|long
name|keySize
parameter_list|()
block|{
return|return
name|this
operator|.
name|active
operator|.
name|keySize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|long
name|heapSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|active
operator|.
name|heapSize
argument_list|()
return|;
block|}
annotation|@
name|Override
comment|/*    * Scanners are ordered from 0 (oldest) to newest in increasing order.    */
specifier|public
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|getScanners
parameter_list|(
name|long
name|readPt
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|long
name|order
init|=
name|snapshot
operator|.
name|getNumOfSegments
argument_list|()
decl_stmt|;
name|order
operator|=
name|addToScanners
argument_list|(
name|active
argument_list|,
name|readPt
argument_list|,
name|order
argument_list|,
name|list
argument_list|)
expr_stmt|;
name|addToScanners
argument_list|(
name|snapshot
operator|.
name|getAllSegments
argument_list|()
argument_list|,
name|readPt
argument_list|,
name|order
argument_list|,
name|list
argument_list|)
expr_stmt|;
return|return
name|list
return|;
block|}
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|Segment
argument_list|>
name|getSegments
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Segment
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|this
operator|.
name|active
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
name|this
operator|.
name|snapshot
argument_list|)
expr_stmt|;
return|return
name|list
return|;
block|}
comment|/**    * @param cell Find the row that comes after this one.  If null, we return the    * first.    * @return Next row or null if none found.    */
name|Cell
name|getNextRow
parameter_list|(
specifier|final
name|Cell
name|cell
parameter_list|)
block|{
return|return
name|getLowest
argument_list|(
name|getNextRow
argument_list|(
name|cell
argument_list|,
name|this
operator|.
name|active
operator|.
name|getCellSet
argument_list|()
argument_list|)
argument_list|,
name|getNextRow
argument_list|(
name|cell
argument_list|,
name|this
operator|.
name|snapshot
operator|.
name|getCellSet
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateLowestUnflushedSequenceIdInWAL
parameter_list|(
name|boolean
name|onlyIfMoreRecent
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|MemStoreSize
name|size
parameter_list|()
block|{
return|return
operator|new
name|MemStoreSize
argument_list|(
name|this
operator|.
name|active
operator|.
name|keySize
argument_list|()
argument_list|,
name|this
operator|.
name|active
operator|.
name|heapSize
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Check whether anything need to be done based on the current active set size    * Nothing need to be done for the DefaultMemStore    */
annotation|@
name|Override
specifier|protected
name|void
name|checkActiveSize
parameter_list|()
block|{
return|return;
block|}
annotation|@
name|Override
specifier|public
name|long
name|preFlushSeqIDEstimation
parameter_list|()
block|{
return|return
name|HConstants
operator|.
name|NO_SEQNUM
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSloppy
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**    * Code to help figure if our approximation of object heap sizes is close    * enough.  See hbase-900.  Fills memstores then waits so user can heap    * dump and bring up resultant hprof in something like jprofiler which    * allows you get 'deep size' on objects.    * @param args main args    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|RuntimeMXBean
name|runtime
init|=
name|ManagementFactory
operator|.
name|getRuntimeMXBean
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"vmName="
operator|+
name|runtime
operator|.
name|getVmName
argument_list|()
operator|+
literal|", vmVendor="
operator|+
name|runtime
operator|.
name|getVmVendor
argument_list|()
operator|+
literal|", vmVersion="
operator|+
name|runtime
operator|.
name|getVmVersion
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"vmInputArguments="
operator|+
name|runtime
operator|.
name|getInputArguments
argument_list|()
argument_list|)
expr_stmt|;
name|DefaultMemStore
name|memstore1
init|=
operator|new
name|DefaultMemStore
argument_list|()
decl_stmt|;
comment|// TODO: x32 vs x64
specifier|final
name|int
name|count
init|=
literal|10000
decl_stmt|;
name|byte
index|[]
name|fam
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"umn"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|empty
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
name|MemStoreSize
name|memstoreSize
init|=
operator|new
name|MemStoreSize
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
name|count
condition|;
name|i
operator|++
control|)
block|{
comment|// Give each its own ts
name|memstore1
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|,
name|fam
argument_list|,
name|qf
argument_list|,
name|i
argument_list|,
name|empty
argument_list|)
argument_list|,
name|memstoreSize
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"memstore1 estimated size="
operator|+
operator|(
name|memstoreSize
operator|.
name|getDataSize
argument_list|()
operator|+
name|memstoreSize
operator|.
name|getHeapSize
argument_list|()
operator|)
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|memstore1
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|,
name|fam
argument_list|,
name|qf
argument_list|,
name|i
argument_list|,
name|empty
argument_list|)
argument_list|,
name|memstoreSize
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"memstore1 estimated size (2nd loading of same data)="
operator|+
operator|(
name|memstoreSize
operator|.
name|getDataSize
argument_list|()
operator|+
name|memstoreSize
operator|.
name|getHeapSize
argument_list|()
operator|)
argument_list|)
expr_stmt|;
comment|// Make a variably sized memstore.
name|DefaultMemStore
name|memstore2
init|=
operator|new
name|DefaultMemStore
argument_list|()
decl_stmt|;
name|memstoreSize
operator|=
operator|new
name|MemStoreSize
argument_list|()
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|memstore2
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|,
name|fam
argument_list|,
name|qf
argument_list|,
name|i
argument_list|,
operator|new
name|byte
index|[
name|i
index|]
argument_list|)
argument_list|,
name|memstoreSize
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"memstore2 estimated size="
operator|+
operator|(
name|memstoreSize
operator|.
name|getDataSize
argument_list|()
operator|+
name|memstoreSize
operator|.
name|getHeapSize
argument_list|()
operator|)
argument_list|)
expr_stmt|;
specifier|final
name|int
name|seconds
init|=
literal|30
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting "
operator|+
name|seconds
operator|+
literal|" seconds while heap dump is taken"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Exiting."
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

