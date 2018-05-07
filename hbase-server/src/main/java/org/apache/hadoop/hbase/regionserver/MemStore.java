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
name|exceptions
operator|.
name|UnexpectedStateException
import|;
end_import

begin_comment
comment|/**  * The MemStore holds in-memory modifications to the Store. Modifications are {@link Cell}s.  *<p>  * The MemStore functions should not be called in parallel. Callers should hold write and read  * locks. This is done in {@link HStore}.  *</p>  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MemStore
block|{
comment|/**    * Creates a snapshot of the current memstore. Snapshot must be cleared by call to    * {@link #clearSnapshot(long)}.    * @return {@link MemStoreSnapshot}    */
name|MemStoreSnapshot
name|snapshot
parameter_list|()
function_decl|;
comment|/**    * Clears the current snapshot of the Memstore.    * @param id    * @throws UnexpectedStateException    * @see #snapshot()    */
name|void
name|clearSnapshot
parameter_list|(
name|long
name|id
parameter_list|)
throws|throws
name|UnexpectedStateException
function_decl|;
comment|/**    * Flush will first clear out the data in snapshot if any (It will take a second flush    * invocation to clear the current Cell set). If snapshot is empty, current    * Cell set will be flushed.    *    * @return On flush, how much memory we will clear.    */
name|MemStoreSize
name|getFlushableSize
parameter_list|()
function_decl|;
comment|/**    * Return the size of the snapshot(s) if any    * @return size of the memstore snapshot    */
name|MemStoreSize
name|getSnapshotSize
parameter_list|()
function_decl|;
comment|/**    * Write an update    * @param cell    * @param memstoreSizing The delta in memstore size will be passed back via this.    *        This will include both data size and heap overhead delta.    */
name|void
name|add
parameter_list|(
specifier|final
name|Cell
name|cell
parameter_list|,
name|MemStoreSizing
name|memstoreSizing
parameter_list|)
function_decl|;
comment|/**    * Write the updates    * @param cells    * @param memstoreSizing The delta in memstore size will be passed back via this.    *        This will include both data size and heap overhead delta.    */
name|void
name|add
parameter_list|(
name|Iterable
argument_list|<
name|Cell
argument_list|>
name|cells
parameter_list|,
name|MemStoreSizing
name|memstoreSizing
parameter_list|)
function_decl|;
comment|/**    * @return Oldest timestamp of all the Cells in the MemStore    */
name|long
name|timeOfOldestEdit
parameter_list|()
function_decl|;
comment|/**    * Update or insert the specified cells.    *<p>    * For each Cell, insert into MemStore. This will atomically upsert the value for that    * row/family/qualifier. If a Cell did already exist, it will then be removed.    *<p>    * Currently the memstoreTS is kept at 0 so as each insert happens, it will be immediately    * visible. May want to change this so it is atomic across all KeyValues.    *<p>    * This is called under row lock, so Get operations will still see updates atomically. Scans will    * only see each KeyValue update as atomic.    * @param cells    * @param readpoint readpoint below which we can safely remove duplicate Cells.    * @param memstoreSizing The delta in memstore size will be passed back via this.    *        This will include both data size and heap overhead delta.    */
name|void
name|upsert
parameter_list|(
name|Iterable
argument_list|<
name|Cell
argument_list|>
name|cells
parameter_list|,
name|long
name|readpoint
parameter_list|,
name|MemStoreSizing
name|memstoreSizing
parameter_list|)
function_decl|;
comment|/**    * @return scanner over the memstore. This might include scanner over the snapshot when one is    * present.    */
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
function_decl|;
comment|/**    * @return Total memory occupied by this MemStore. This won't include any size occupied by the    *         snapshot. We assume the snapshot will get cleared soon. This is not thread safe and    *         the memstore may be changed while computing its size. It is the responsibility of the    *         caller to make sure this doesn't happen.    */
name|MemStoreSize
name|size
parameter_list|()
function_decl|;
comment|/**    * This method is called before the flush is executed.    * @return an estimation (lower bound) of the unflushed sequence id in memstore after the flush    * is executed. if memstore will be cleared returns {@code HConstants.NO_SEQNUM}.    */
name|long
name|preFlushSeqIDEstimation
parameter_list|()
function_decl|;
comment|/* Return true if the memstore may use some extra memory space*/
name|boolean
name|isSloppy
parameter_list|()
function_decl|;
comment|/**    * This message intends to inform the MemStore that next coming updates    * are going to be part of the replaying edits from WAL    */
specifier|default
name|void
name|startReplayingFromWAL
parameter_list|()
block|{
return|return;
block|}
comment|/**    * This message intends to inform the MemStore that the replaying edits from WAL    * are done    */
specifier|default
name|void
name|stopReplayingFromWAL
parameter_list|()
block|{
return|return;
block|}
block|}
end_interface

end_unit

