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
name|UUID
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
name|FSDataInputStream
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
name|HBaseInterfaceAudience
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
name|TableName
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
name|protobuf
operator|.
name|generated
operator|.
name|WALProtos
operator|.
name|WALTrailer
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
comment|/**  * HLog records all the edits to HStore.  It is the hbase write-ahead-log (WAL).  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
comment|// TODO: Rename interface to WAL
specifier|public
interface|interface
name|HLog
block|{
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
name|long
name|NO_SEQUENCE_ID
init|=
operator|-
literal|1
decl_stmt|;
comment|/** File Extension used while splitting an HLog into regions (HBASE-2312) */
comment|// TODO: this seems like an implementation detail that does not belong here.
name|String
name|SPLITTING_EXT
init|=
literal|"-splitting"
decl_stmt|;
name|boolean
name|SPLIT_SKIP_ERRORS_DEFAULT
init|=
literal|false
decl_stmt|;
comment|/** The hbase:meta region's HLog filename extension.*/
comment|// TODO: Implementation detail.  Does not belong in here.
name|String
name|META_HLOG_FILE_EXTN
init|=
literal|".meta"
decl_stmt|;
comment|/**    * Configuration name of HLog Trailer's warning size. If a waltrailer's size is greater than the    * configured size, a warning is logged. This is used with Protobuf reader/writer.    */
comment|// TODO: Implementation detail.  Why in here?
name|String
name|WAL_TRAILER_WARN_SIZE
init|=
literal|"hbase.regionserver.waltrailer.warn.size"
decl_stmt|;
name|int
name|DEFAULT_WAL_TRAILER_WARN_SIZE
init|=
literal|1024
operator|*
literal|1024
decl_stmt|;
comment|// 1MB
comment|// TODO: Implementation detail.  Why in here?
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
name|String
name|RECOVERED_LOG_TMPFILE_SUFFIX
init|=
literal|".temp"
decl_stmt|;
comment|/**    * WAL Reader Interface    */
interface|interface
name|Reader
block|{
comment|/**      * @param fs File system.      * @param path Path.      * @param c Configuration.      * @param s Input stream that may have been pre-opened by the caller; may be null.      */
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
parameter_list|,
name|FSDataInputStream
name|s
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
comment|/**      * @return the WALTrailer of the current HLog. It may be null in case of legacy or corrupt WAL      * files.      */
comment|// TODO: What we need a trailer on WAL for?  It won't be present on last WAL most of the time.
comment|// What then?
name|WALTrailer
name|getWALTrailer
parameter_list|()
function_decl|;
block|}
comment|/**    * WAL Writer Intrface.    */
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
parameter_list|,
name|boolean
name|overwritable
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
comment|/**      * Sets HLog/WAL's WALTrailer. This trailer is appended at the end of WAL on closing.      * @param walTrailer trailer to append to WAL.      */
comment|// TODO: Why a trailer on the log?
name|void
name|setWALTrailer
parameter_list|(
name|WALTrailer
name|walTrailer
parameter_list|)
function_decl|;
block|}
comment|/**    * Utility class that lets us keep track of the edit and it's associated key. Only used when    * splitting logs.    */
comment|// TODO: Remove this Writable.
comment|// TODO: Why is this in here?  Implementation detail?
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|REPLICATION
argument_list|)
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
comment|/**      * Constructor for both params      *      * @param edit log's edit      * @param key log's key      */
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
comment|/**      * Gets the edit      *      * @return edit      */
specifier|public
name|WALEdit
name|getEdit
parameter_list|()
block|{
return|return
name|edit
return|;
block|}
comment|/**      * Gets the key      *      * @return key      */
specifier|public
name|HLogKey
name|getKey
parameter_list|()
block|{
return|return
name|key
return|;
block|}
comment|/**      * Set compression context for this entry.      *      * @param compressionContext Compression context      */
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
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
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
comment|/**    * Registers WALActionsListener    *    * @param listener    */
name|void
name|registerWALActionsListener
parameter_list|(
specifier|final
name|WALActionsListener
name|listener
parameter_list|)
function_decl|;
comment|/**    * Unregisters WALActionsListener    *    * @param listener    */
name|boolean
name|unregisterWALActionsListener
parameter_list|(
specifier|final
name|WALActionsListener
name|listener
parameter_list|)
function_decl|;
comment|/**    * @return Current state of the monotonically increasing file id.    */
comment|// TODO: Remove.  Implementation detail.
name|long
name|getFilenum
parameter_list|()
function_decl|;
comment|/**    * @return the number of HLog files    */
name|int
name|getNumLogFiles
parameter_list|()
function_decl|;
comment|/**    * @return the size of HLog files    */
name|long
name|getLogFileSize
parameter_list|()
function_decl|;
comment|// TODO: Log rolling should not be in this interface.
comment|/**    * Roll the log writer. That is, start writing log messages to a new file.    *    *<p>    * The implementation is synchronized in order to make sure there's one rollWriter    * running at any given time.    *    * @return If lots of logs, flush the returned regions so next time through we    *         can clean logs. Returns null if nothing to flush. Names are actual    *         region names as returned by {@link HRegionInfo#getEncodedName()}    * @throws org.apache.hadoop.hbase.regionserver.wal.FailedLogCloseException    * @throws IOException    */
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
comment|/**    * Roll the log writer. That is, start writing log messages to a new file.    *    *<p>    * The implementation is synchronized in order to make sure there's one rollWriter    * running at any given time.    *    * @param force    *          If true, force creation of a new writer even if no entries have    *          been written to the current writer    * @return If lots of logs, flush the returned regions so next time through we    *         can clean logs. Returns null if nothing to flush. Names are actual    *         region names as returned by {@link HRegionInfo#getEncodedName()}    * @throws org.apache.hadoop.hbase.regionserver.wal.FailedLogCloseException    * @throws IOException    */
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
comment|/**    * Shut down the log.    *    * @throws IOException    */
name|void
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Shut down the log and delete the log directory.    * Used by tests only and in rare cases where we need a log just temporarily while bootstrapping    * a region or running migrations.    *    * @throws IOException    */
name|void
name|closeAndDelete
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Same as {@link #appendNoSync(HRegionInfo, TableName, WALEdit, List, long, HTableDescriptor,    *   AtomicLong, boolean, long, long)}    * except it causes a sync on the log    * @param info    * @param tableName    * @param edits    * @param now    * @param htd    * @param sequenceId    * @throws IOException    * @deprecated For tests only and even then, should use    * {@link #appendNoSync(HTableDescriptor, HRegionInfo, HLogKey, WALEdit, AtomicLong, boolean,    * List)} and {@link #sync()} instead.    */
annotation|@
name|Deprecated
annotation|@
name|VisibleForTesting
specifier|public
name|void
name|append
parameter_list|(
name|HRegionInfo
name|info
parameter_list|,
name|TableName
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
parameter_list|,
name|AtomicLong
name|sequenceId
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * For notification post append to the writer.  Used by metrics system at least.    * @param entry    * @param elapsedTime    * @return Size of this append.    */
name|long
name|postAppend
parameter_list|(
specifier|final
name|Entry
name|entry
parameter_list|,
specifier|final
name|long
name|elapsedTime
parameter_list|)
function_decl|;
comment|/**    * For notification post writer sync.  Used by metrics system at least.    * @param timeInMillis How long the filesystem sync took in milliseconds.    * @param handlerSyncs How many sync handler calls were released by this call to filesystem    * sync.    */
name|void
name|postSync
parameter_list|(
specifier|final
name|long
name|timeInMillis
parameter_list|,
specifier|final
name|int
name|handlerSyncs
parameter_list|)
function_decl|;
comment|/**    * Append a set of edits to the WAL. WAL edits are keyed by (encoded) regionName, rowname, and    * log-sequence-id. The WAL is not flushed/sync'd after this transaction completes BUT on return    * this edit must have its region edit/sequence id assigned else it messes up our unification    * of mvcc and sequenceid.    * @param info    * @param tableName    * @param edits    * @param clusterIds    * @param now    * @param htd    * @param sequenceId A reference to the atomic long the<code>info</code> region is using as    * source of its incrementing edits sequence id.  Inside in this call we will increment it and    * attach the sequence to the edit we apply the WAL.    * @param isInMemstore Always true except for case where we are writing a compaction completion    * record into the WAL; in this case the entry is just so we can finish an unfinished compaction    * -- it is not an edit for memstore.    * @param nonceGroup    * @param nonce    * @return Returns a 'transaction id'.  Do not use. This is an internal implementation detail and    * cannot be respected in all implementations; i.e. the append/sync machine may or may not be    * able to sync an explicit edit only (the current default implementation syncs up to the time    * of the sync call syncing whatever is behind the sync).    * @throws IOException    * @deprecated Use {@link #appendNoSync(HTableDescriptor, HRegionInfo, HLogKey, WALEdit, AtomicLong, boolean, List)}    * instead because you can get back the region edit/sequenceid; it is set into the passed in    *<code>key</code>.    */
annotation|@
name|Deprecated
name|long
name|appendNoSync
parameter_list|(
name|HRegionInfo
name|info
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|WALEdit
name|edits
parameter_list|,
name|List
argument_list|<
name|UUID
argument_list|>
name|clusterIds
parameter_list|,
specifier|final
name|long
name|now
parameter_list|,
name|HTableDescriptor
name|htd
parameter_list|,
name|AtomicLong
name|sequenceId
parameter_list|,
name|boolean
name|isInMemstore
parameter_list|,
name|long
name|nonceGroup
parameter_list|,
name|long
name|nonce
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Append a set of edits to the WAL. The WAL is not flushed/sync'd after this transaction    * completes BUT on return this edit must have its region edit/sequence id assigned    * else it messes up our unification of mvcc and sequenceid.  On return<code>key</code> will    * have the region edit/sequence id filled in.    * @param info    * @param key Modified by this call; we add to it this edits region edit/sequence id.    * @param edits Edits to append. MAY CONTAIN NO EDITS for case where we want to get an edit    * sequence id that is after all currently appended edits.    * @param htd    * @param sequenceId A reference to the atomic long the<code>info</code> region is using as    * source of its incrementing edits sequence id.  Inside in this call we will increment it and    * attach the sequence to the edit we apply the WAL.    * @param inMemstore Always true except for case where we are writing a compaction completion    * record into the WAL; in this case the entry is just so we can finish an unfinished compaction    * -- it is not an edit for memstore.    * @param memstoreKVs list of KVs added into memstore    * @return Returns a 'transaction id' and<code>key</code> will have the region edit/sequence id    * in it.    * @throws IOException    */
name|long
name|appendNoSync
parameter_list|(
name|HTableDescriptor
name|htd
parameter_list|,
name|HRegionInfo
name|info
parameter_list|,
name|HLogKey
name|key
parameter_list|,
name|WALEdit
name|edits
parameter_list|,
name|AtomicLong
name|sequenceId
parameter_list|,
name|boolean
name|inMemstore
parameter_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
name|memstoreKVs
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|// TODO: Do we need all these versions of sync?
name|void
name|hsync
parameter_list|()
throws|throws
name|IOException
function_decl|;
name|void
name|hflush
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Sync what we have in the WAL.    * @throws IOException    */
name|void
name|sync
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Sync the WAL if the txId was not already sync'd.    * @param txid Transaction id to sync to.    * @throws IOException    */
name|void
name|sync
parameter_list|(
name|long
name|txid
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * WAL keeps track of the sequence numbers that were not yet flushed from memstores    * in order to be able to do cleanup. This method tells WAL that some region is about    * to flush memstore.    *    *<p>We stash the oldest seqNum for the region, and let the the next edit inserted in this    * region be recorded in {@link #append(HRegionInfo, TableName, WALEdit, long, HTableDescriptor,    * AtomicLong)} as new oldest seqnum.    * In case of flush being aborted, we put the stashed value back; in case of flush succeeding,    * the seqNum of that first edit after start becomes the valid oldest seqNum for this region.    *    * @return true if the flush can proceed, false in case wal is closing (ususally, when server is    * closing) and flush couldn't be started.    */
name|boolean
name|startCacheFlush
parameter_list|(
specifier|final
name|byte
index|[]
name|encodedRegionName
parameter_list|)
function_decl|;
comment|/**    * Complete the cache flush.    * @param encodedRegionName Encoded region name.    */
name|void
name|completeCacheFlush
parameter_list|(
specifier|final
name|byte
index|[]
name|encodedRegionName
parameter_list|)
function_decl|;
comment|/**    * Abort a cache flush. Call if the flush fails. Note that the only recovery    * for an aborted flush currently is a restart of the regionserver so the    * snapshot content dropped by the failure gets restored to the memstore.v    * @param encodedRegionName Encoded region name.    */
name|void
name|abortCacheFlush
parameter_list|(
name|byte
index|[]
name|encodedRegionName
parameter_list|)
function_decl|;
comment|/**    * @return Coprocessor host.    */
name|WALCoprocessorHost
name|getCoprocessorHost
parameter_list|()
function_decl|;
comment|/**    * Get LowReplication-Roller status    *    * @return lowReplicationRollEnabled    */
comment|// TODO: This is implementation detail?
name|boolean
name|isLowReplicationRollEnabled
parameter_list|()
function_decl|;
comment|/** Gets the earliest sequence number in the memstore for this particular region.    * This can serve as best-effort "recent" WAL number for this region.    * @param encodedRegionName The region to get the number for.    * @return The number if present, HConstants.NO_SEQNUM if absent.    */
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

