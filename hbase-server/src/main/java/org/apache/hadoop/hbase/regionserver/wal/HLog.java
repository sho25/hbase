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
name|regionserver
operator|.
name|wal
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

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
name|io
operator|.
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|UUID
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|FileStatus
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
name|io
operator|.
name|Writable
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
name|HRegionInfo
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
name|HTableDescriptor
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|HLog
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HLog
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|METAFAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"METAFAMILY"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|METAROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"METAROW"
argument_list|)
decl_stmt|;
comment|/** File Extension used while splitting an HLog into regions (HBASE-2312) */
specifier|public
specifier|static
specifier|final
name|String
name|SPLITTING_EXT
init|=
literal|"-splitting"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|SPLIT_SKIP_ERRORS_DEFAULT
init|=
literal|false
decl_stmt|;
comment|/** The META region's HLog filename extension */
specifier|public
specifier|static
specifier|final
name|String
name|META_HLOG_FILE_EXTN
init|=
literal|".meta"
decl_stmt|;
comment|/*    * Name of directory that holds recovered edits written by the wal log    * splitting code, one per region    */
specifier|static
specifier|final
name|String
name|RECOVERED_EDITS_DIR
init|=
literal|"recovered.edits"
decl_stmt|;
specifier|static
specifier|final
name|Pattern
name|EDITFILES_NAME_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"-?[0-9]+"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|RECOVERED_LOG_TMPFILE_SUFFIX
init|=
literal|".temp"
decl_stmt|;
specifier|public
interface|interface
name|Reader
block|{
name|void
name|init
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|Configuration
name|c
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|void
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
name|Entry
name|next
parameter_list|()
throws|throws
name|IOException
function_decl|;
name|Entry
name|next
parameter_list|(
name|Entry
name|reuse
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|void
name|seek
parameter_list|(
name|long
name|pos
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|long
name|getPosition
parameter_list|()
throws|throws
name|IOException
function_decl|;
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
specifier|public
interface|interface
name|Writer
block|{
name|void
name|init
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|Configuration
name|c
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|void
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
name|void
name|sync
parameter_list|()
throws|throws
name|IOException
function_decl|;
name|void
name|append
parameter_list|(
name|Entry
name|entry
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|long
name|getLength
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    * Utility class that lets us keep track of the edit with it's key Only used    * when splitting logs    */
specifier|public
specifier|static
class|class
name|Entry
implements|implements
name|Writable
block|{
specifier|private
name|WALEdit
name|edit
decl_stmt|;
specifier|private
name|HLogKey
name|key
decl_stmt|;
specifier|public
name|Entry
parameter_list|()
block|{
name|edit
operator|=
operator|new
name|WALEdit
argument_list|()
expr_stmt|;
name|key
operator|=
operator|new
name|HLogKey
argument_list|()
expr_stmt|;
block|}
comment|/**      * Constructor for both params      *       * @param edit      *          log's edit      * @param key      *          log's key      */
specifier|public
name|Entry
parameter_list|(
name|HLogKey
name|key
parameter_list|,
name|WALEdit
name|edit
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
name|this
operator|.
name|edit
operator|=
name|edit
expr_stmt|;
block|}
comment|/**      * Gets the edit      *       * @return edit      */
specifier|public
name|WALEdit
name|getEdit
parameter_list|()
block|{
return|return
name|edit
return|;
block|}
comment|/**      * Gets the key      *       * @return key      */
specifier|public
name|HLogKey
name|getKey
parameter_list|()
block|{
return|return
name|key
return|;
block|}
comment|/**      * Set compression context for this entry.      *       * @param compressionContext      *          Compression context      */
specifier|public
name|void
name|setCompressionContext
parameter_list|(
name|CompressionContext
name|compressionContext
parameter_list|)
block|{
name|edit
operator|.
name|setCompressionContext
argument_list|(
name|compressionContext
argument_list|)
expr_stmt|;
name|key
operator|.
name|setCompressionContext
argument_list|(
name|compressionContext
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|this
operator|.
name|key
operator|+
literal|"="
operator|+
name|this
operator|.
name|edit
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|dataOutput
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|key
operator|.
name|write
argument_list|(
name|dataOutput
argument_list|)
expr_stmt|;
name|this
operator|.
name|edit
operator|.
name|write
argument_list|(
name|dataOutput
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|dataInput
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|key
operator|.
name|readFields
argument_list|(
name|dataInput
argument_list|)
expr_stmt|;
name|this
operator|.
name|edit
operator|.
name|readFields
argument_list|(
name|dataInput
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*    * registers WALActionsListener    *     * @param listener    */
specifier|public
name|void
name|registerWALActionsListener
parameter_list|(
specifier|final
name|WALActionsListener
name|listener
parameter_list|)
function_decl|;
comment|/*    * unregisters WALActionsListener    *     * @param listener    */
specifier|public
name|boolean
name|unregisterWALActionsListener
parameter_list|(
specifier|final
name|WALActionsListener
name|listener
parameter_list|)
function_decl|;
comment|/**    * @return Current state of the monotonically increasing file id.    */
specifier|public
name|long
name|getFilenum
parameter_list|()
function_decl|;
comment|/**    * Called by HRegionServer when it opens a new region to ensure that log    * sequence numbers are always greater than the latest sequence number of the    * region being brought on-line.    *     * @param newvalue    *          We'll set log edit/sequence number to this value if it is greater    *          than the current value.    */
specifier|public
name|void
name|setSequenceNumber
parameter_list|(
specifier|final
name|long
name|newvalue
parameter_list|)
function_decl|;
comment|/**    * @return log sequence number    */
specifier|public
name|long
name|getSequenceNumber
parameter_list|()
function_decl|;
comment|/**    * Roll the log writer. That is, start writing log messages to a new file.    *     * Because a log cannot be rolled during a cache flush, and a cache flush    * spans two method calls, a special lock needs to be obtained so that a cache    * flush cannot start when the log is being rolled and the log cannot be    * rolled during a cache flush.    *     *<p>    * Note that this method cannot be synchronized because it is possible that    * startCacheFlush runs, obtaining the cacheFlushLock, then this method could    * start which would obtain the lock on this but block on obtaining the    * cacheFlushLock and then completeCacheFlush could be called which would wait    * for the lock on this and consequently never release the cacheFlushLock    *     * @return If lots of logs, flush the returned regions so next time through we    *         can clean logs. Returns null if nothing to flush. Names are actual    *         region names as returned by {@link HRegionInfo#getEncodedName()}    * @throws org.apache.hadoop.hbase.regionserver.wal.FailedLogCloseException    * @throws IOException    */
specifier|public
name|byte
index|[]
index|[]
name|rollWriter
parameter_list|()
throws|throws
name|FailedLogCloseException
throws|,
name|IOException
function_decl|;
comment|/**    * Roll the log writer. That is, start writing log messages to a new file.    *     * Because a log cannot be rolled during a cache flush, and a cache flush    * spans two method calls, a special lock needs to be obtained so that a cache    * flush cannot start when the log is being rolled and the log cannot be    * rolled during a cache flush.    *     *<p>    * Note that this method cannot be synchronized because it is possible that    * startCacheFlush runs, obtaining the cacheFlushLock, then this method could    * start which would obtain the lock on this but block on obtaining the    * cacheFlushLock and then completeCacheFlush could be called which would wait    * for the lock on this and consequently never release the cacheFlushLock    *     * @param force    *          If true, force creation of a new writer even if no entries have    *          been written to the current writer    * @return If lots of logs, flush the returned regions so next time through we    *         can clean logs. Returns null if nothing to flush. Names are actual    *         region names as returned by {@link HRegionInfo#getEncodedName()}    * @throws org.apache.hadoop.hbase.regionserver.wal.FailedLogCloseException    * @throws IOException    */
specifier|public
name|byte
index|[]
index|[]
name|rollWriter
parameter_list|(
name|boolean
name|force
parameter_list|)
throws|throws
name|FailedLogCloseException
throws|,
name|IOException
function_decl|;
comment|/**    * Shut down the log.    *     * @throws IOException    */
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Shut down the log and delete the log directory    *     * @throws IOException    */
specifier|public
name|void
name|closeAndDelete
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Append an entry to the log.    *     * @param regionInfo    * @param logEdit    * @param logKey    * @param doSync    *          shall we sync after writing the transaction    * @return The txid of this transaction    * @throws IOException    */
specifier|public
name|long
name|append
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
name|HLogKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|,
name|HTableDescriptor
name|htd
parameter_list|,
name|boolean
name|doSync
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Only used in tests.    *     * @param info    * @param tableName    * @param edits    * @param now    * @param htd    * @throws IOException    */
specifier|public
name|void
name|append
parameter_list|(
name|HRegionInfo
name|info
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|WALEdit
name|edits
parameter_list|,
specifier|final
name|long
name|now
parameter_list|,
name|HTableDescriptor
name|htd
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Append a set of edits to the log. Log edits are keyed by (encoded)    * regionName, rowname, and log-sequence-id. The HLog is not flushed after    * this transaction is written to the log.    *     * @param info    * @param tableName    * @param edits    * @param clusterId    *          The originating clusterId for this edit (for replication)    * @param now    * @return txid of this transaction    * @throws IOException    */
specifier|public
name|long
name|appendNoSync
parameter_list|(
name|HRegionInfo
name|info
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|WALEdit
name|edits
parameter_list|,
name|UUID
name|clusterId
parameter_list|,
specifier|final
name|long
name|now
parameter_list|,
name|HTableDescriptor
name|htd
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Append a set of edits to the log. Log edits are keyed by (encoded)    * regionName, rowname, and log-sequence-id. The HLog is flushed after this    * transaction is written to the log.    *     * @param info    * @param tableName    * @param edits    * @param clusterId    *          The originating clusterId for this edit (for replication)    * @param now    * @param htd    * @return txid of this transaction    * @throws IOException    */
specifier|public
name|long
name|append
parameter_list|(
name|HRegionInfo
name|info
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|WALEdit
name|edits
parameter_list|,
name|UUID
name|clusterId
parameter_list|,
specifier|final
name|long
name|now
parameter_list|,
name|HTableDescriptor
name|htd
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|void
name|hsync
parameter_list|()
throws|throws
name|IOException
function_decl|;
specifier|public
name|void
name|hflush
parameter_list|()
throws|throws
name|IOException
function_decl|;
specifier|public
name|void
name|sync
parameter_list|()
throws|throws
name|IOException
function_decl|;
specifier|public
name|void
name|sync
parameter_list|(
name|long
name|txid
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Obtain a log sequence number.    */
specifier|public
name|long
name|obtainSeqNum
parameter_list|()
function_decl|;
comment|/**    * By acquiring a log sequence ID, we can allow log messages to continue while    * we flush the cache.    *     * Acquire a lock so that we do not roll the log between the start and    * completion of a cache-flush. Otherwise the log-seq-id for the flush will    * not appear in the correct logfile.    *     * Ensuring that flushes and log-rolls don't happen concurrently also allows    * us to temporarily put a log-seq-number in lastSeqWritten against the region    * being flushed that might not be the earliest in-memory log-seq-number for    * that region. By the time the flush is completed or aborted and before the    * cacheFlushLock is released it is ensured that lastSeqWritten again has the    * oldest in-memory edit's lsn for the region that was being flushed.    *     * In this method, by removing the entry in lastSeqWritten for the region    * being flushed we ensure that the next edit inserted in this region will be    * correctly recorded in    * {@link #append(HRegionInfo, byte[], WALEdit, long, HTableDescriptor)} The    * lsn of the earliest in-memory lsn - which is now in the memstore snapshot -    * is saved temporarily in the lastSeqWritten map while the flush is active.    *     * @return sequence ID to pass    *         {@link #completeCacheFlush(byte[], byte[], long, boolean)} (byte[],    *         byte[], long)}    * @see #completeCacheFlush(byte[], byte[], long, boolean)    * @see #abortCacheFlush(byte[])    */
specifier|public
name|long
name|startCacheFlush
parameter_list|(
specifier|final
name|byte
index|[]
name|encodedRegionName
parameter_list|)
function_decl|;
comment|/**    * Complete the cache flush    *     * Protected by cacheFlushLock    *     * @param encodedRegionName    * @param tableName    * @param logSeqId    * @throws IOException    */
specifier|public
name|void
name|completeCacheFlush
parameter_list|(
specifier|final
name|byte
index|[]
name|encodedRegionName
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|long
name|logSeqId
parameter_list|,
specifier|final
name|boolean
name|isMetaRegion
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Abort a cache flush. Call if the flush fails. Note that the only recovery    * for an aborted flush currently is a restart of the regionserver so the    * snapshot content dropped by the failure gets restored to the memstore.    */
specifier|public
name|void
name|abortCacheFlush
parameter_list|(
name|byte
index|[]
name|encodedRegionName
parameter_list|)
function_decl|;
comment|/**    * @return Coprocessor host.    */
specifier|public
name|WALCoprocessorHost
name|getCoprocessorHost
parameter_list|()
function_decl|;
comment|/**    * Get LowReplication-Roller status    *     * @return lowReplicationRollEnabled    */
specifier|public
name|boolean
name|isLowReplicationRollEnabled
parameter_list|()
function_decl|;
comment|/** Gets the earliest sequence number in the memstore for this particular region.    * This can serve as best-effort "recent" WAL number for this region.    * @param encodedRegionName The region to get the number for.    * @return The number if present, HConstants.NO_SEQNUM if absent.    */
specifier|public
name|long
name|getEarliestMemstoreSeqNum
parameter_list|(
name|byte
index|[]
name|encodedRegionName
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

