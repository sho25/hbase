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
name|wal
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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

begin_comment
comment|// imports we use from yet-to-be-moved regionsever.wal
end_comment

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
name|CompressionContext
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
name|FailedLogCloseException
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
name|HLogKey
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
name|WALActionsListener
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
name|WALCoprocessorHost
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
comment|/**  * A Write Ahead Log (WAL) provides service for reading, writing waledits. This interface provides  * APIs for WAL users (such as RegionServer) to use the WAL (do append, sync, etc).  *  * Note that some internals, such as log rolling and performance evaluation tools, will use  * WAL.equals to determine if they have already seen a given WAL.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|WAL
block|{
comment|/**    * Registers WALActionsListener    */
name|void
name|registerWALActionsListener
parameter_list|(
specifier|final
name|WALActionsListener
name|listener
parameter_list|)
function_decl|;
comment|/**    * Unregisters WALActionsListener    */
name|boolean
name|unregisterWALActionsListener
parameter_list|(
specifier|final
name|WALActionsListener
name|listener
parameter_list|)
function_decl|;
comment|/**    * Roll the log writer. That is, start writing log messages to a new file.    *    *<p>    * The implementation is synchronized in order to make sure there's one rollWriter    * running at any given time.    *    * @return If lots of logs, flush the returned regions so next time through we    *         can clean logs. Returns null if nothing to flush. Names are actual    *         region names as returned by {@link HRegionInfo#getEncodedName()}    */
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
comment|/**    * Roll the log writer. That is, start writing log messages to a new file.    *    *<p>    * The implementation is synchronized in order to make sure there's one rollWriter    * running at any given time.    *    * @param force    *          If true, force creation of a new writer even if no entries have    *          been written to the current writer    * @return If lots of logs, flush the returned regions so next time through we    *         can clean logs. Returns null if nothing to flush. Names are actual    *         region names as returned by {@link HRegionInfo#getEncodedName()}    */
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
comment|/**    * Stop accepting new writes. If we have unsynced writes still in buffer, sync them.    * Extant edits are left in place in backing storage to be replayed later.    */
name|void
name|shutdown
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Caller no longer needs any edits from this WAL. Implementers are free to reclaim    * underlying resources after this call; i.e. filesystem based WALs can archive or    * delete files.    */
name|void
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Append a set of edits to the WAL. The WAL is not flushed/sync'd after this transaction    * completes BUT on return this edit must have its region edit/sequence id assigned    * else it messes up our unification of mvcc and sequenceid.  On return<code>key</code> will    * have the region edit/sequence id filled in.    * @param info    * @param key Modified by this call; we add to it this edits region edit/sequence id.    * @param edits Edits to append. MAY CONTAIN NO EDITS for case where we want to get an edit    * sequence id that is after all currently appended edits.    * @param htd used to give scope for replication TODO refactor out in favor of table name and info    * @param sequenceId A reference to the atomic long the<code>info</code> region is using as    * source of its incrementing edits sequence id.  Inside in this call we will increment it and    * attach the sequence to the edit we apply the WAL.    * @param inMemstore Always true except for case where we are writing a compaction completion    * record into the WAL; in this case the entry is just so we can finish an unfinished compaction    * -- it is not an edit for memstore.    * @param memstoreKVs list of KVs added into memstore    * @return Returns a 'transaction id' and<code>key</code> will have the region edit/sequence id    * in it.    */
name|long
name|append
parameter_list|(
name|HTableDescriptor
name|htd
parameter_list|,
name|HRegionInfo
name|info
parameter_list|,
name|WALKey
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
name|Cell
argument_list|>
name|memstoreKVs
parameter_list|)
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
comment|/**    * WAL keeps track of the sequence numbers that are as yet not flushed im memstores    * in order to be able to do accounting to figure which WALs can be let go. This method tells WAL    * that some region is about to flush. The flush can be the whole region or for a column family    * of the region only.    *    *<p>Currently, it is expected that the update lock is held for the region; i.e. no    * concurrent appends while we set up cache flush.    * @param families Families to flush. May be a subset of all families in the region.    * @return Returns {@link HConstants#NO_SEQNUM} if we are flushing the whole region OR if    * we are flushing a subset of all families but there are no edits in those families not    * being flushed; in other words, this is effectively same as a flush of all of the region    * though we were passed a subset of regions. Otherwise, it returns the sequence id of the    * oldest/lowest outstanding edit.    * @see #completeCacheFlush(byte[])    * @see #abortCacheFlush(byte[])    */
name|Long
name|startCacheFlush
parameter_list|(
specifier|final
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
parameter_list|)
function_decl|;
comment|/**    * Complete the cache flush.    * @param encodedRegionName Encoded region name.    * @see #startCacheFlush(byte[], Set)    * @see #abortCacheFlush(byte[])    */
name|void
name|completeCacheFlush
parameter_list|(
specifier|final
name|byte
index|[]
name|encodedRegionName
parameter_list|)
function_decl|;
comment|/**    * Abort a cache flush. Call if the flush fails. Note that the only recovery    * for an aborted flush currently is a restart of the regionserver so the    * snapshot content dropped by the failure gets restored to the memstore.    * @param encodedRegionName Encoded region name.    */
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
comment|/**    * Gets the earliest unflushed sequence id in the memstore for the region.    * @param encodedRegionName The region to get the number for.    * @return The earliest/lowest/oldest sequence id if present, HConstants.NO_SEQNUM if absent.    * @deprecated Since version 1.2.0. Removing because not used and exposes subtle internal    * workings. Use {@link #getEarliestMemstoreSeqNum(byte[], byte[])}    */
annotation|@
name|VisibleForTesting
annotation|@
name|Deprecated
name|long
name|getEarliestMemstoreSeqNum
parameter_list|(
name|byte
index|[]
name|encodedRegionName
parameter_list|)
function_decl|;
comment|/**    * Gets the earliest unflushed sequence id in the memstore for the store.    * @param encodedRegionName The region to get the number for.    * @param familyName The family to get the number for.    * @return The earliest/lowest/oldest sequence id if present, HConstants.NO_SEQNUM if absent.    */
name|long
name|getEarliestMemstoreSeqNum
parameter_list|(
name|byte
index|[]
name|encodedRegionName
parameter_list|,
name|byte
index|[]
name|familyName
parameter_list|)
function_decl|;
comment|/**    * Human readable identifying information about the state of this WAL.    * Implementors are encouraged to include information appropriate for debugging.    * Consumers are advised not to rely on the details of the returned String; it does    * not have a defined structure.    */
annotation|@
name|Override
name|String
name|toString
parameter_list|()
function_decl|;
comment|/**    * When outside clients need to consume persisted WALs, they rely on a provided    * Reader.    */
interface|interface
name|Reader
extends|extends
name|Closeable
block|{
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
comment|/**    * Utility class that lets us keep track of the edit with it's key.    */
class|class
name|Entry
block|{
specifier|private
name|WALEdit
name|edit
decl_stmt|;
specifier|private
name|WALKey
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
comment|// we use HLogKey here instead of WALKey directly to support legacy coprocessors.
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
name|WALKey
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
name|WALKey
name|getKey
parameter_list|()
block|{
return|return
name|key
return|;
block|}
comment|/**      * Set compression context for this entry.      *      * @param compressionContext      *          Compression context      */
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
block|}
block|}
end_interface

end_unit

