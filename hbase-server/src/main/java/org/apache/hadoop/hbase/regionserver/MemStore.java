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
name|rmi
operator|.
name|UnexpectedException
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
name|io
operator|.
name|HeapSize
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
extends|extends
name|HeapSize
block|{
comment|/**    * Creates a snapshot of the current memstore. Snapshot must be cleared by call to    * {@link #clearSnapshot(long)}.    * @return {@link MemStoreSnapshot}    */
name|MemStoreSnapshot
name|snapshot
parameter_list|()
function_decl|;
comment|/**    * Clears the current snapshot of the Memstore.    * @param id    * @see #snapshot()    */
name|void
name|clearSnapshot
parameter_list|(
name|long
name|id
parameter_list|)
throws|throws
name|UnexpectedException
function_decl|;
comment|/**    * On flush, how much memory we will clear.    * Flush will first clear out the data in snapshot if any (It will take a second flush    * invocation to clear the current Cell set). If snapshot is empty, current    * Cell set will be flushed.    *    * @return size of data that is going to be flushed    */
name|long
name|getFlushableSize
parameter_list|()
function_decl|;
comment|/**    * Write an update    * @param cell    * @return approximate size of the passed key and value.    */
name|long
name|add
parameter_list|(
specifier|final
name|Cell
name|cell
parameter_list|)
function_decl|;
comment|/**    * @return Oldest timestamp of all the Cells in the MemStore    */
name|long
name|timeOfOldestEdit
parameter_list|()
function_decl|;
comment|/**    * Remove n key from the memstore. Only kvs that have the same key and the same memstoreTS are    * removed. It is ok to not update timeRangeTracker in this call.    * @param cell    */
name|void
name|rollback
parameter_list|(
specifier|final
name|Cell
name|cell
parameter_list|)
function_decl|;
comment|/**    * Write a delete    * @param deleteCell    * @return approximate size of the passed key and value.    */
name|long
name|delete
parameter_list|(
specifier|final
name|Cell
name|deleteCell
parameter_list|)
function_decl|;
comment|/**    * Find the key that matches<i>row</i> exactly, or the one that immediately precedes it. The    * target row key is set in state.    * @param state column/delete tracking state    */
name|void
name|getRowKeyAtOrBefore
parameter_list|(
specifier|final
name|GetClosestRowBeforeTracker
name|state
parameter_list|)
function_decl|;
comment|/**    * Given the specs of a column, update it, first by inserting a new record,    * then removing the old one.  Since there is only 1 KeyValue involved, the memstoreTS    * will be set to 0, thus ensuring that they instantly appear to anyone. The underlying    * store will ensure that the insert/delete each are atomic. A scanner/reader will either    * get the new value, or the old value and all readers will eventually only see the new    * value after the old was removed.    *    * @param row    * @param family    * @param qualifier    * @param newValue    * @param now    * @return Timestamp    */
name|long
name|updateColumnValue
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|long
name|newValue
parameter_list|,
name|long
name|now
parameter_list|)
function_decl|;
comment|/**    * Update or insert the specified cells.    *<p>    * For each Cell, insert into MemStore. This will atomically upsert the value for that    * row/family/qualifier. If a Cell did already exist, it will then be removed.    *<p>    * Currently the memstoreTS is kept at 0 so as each insert happens, it will be immediately    * visible. May want to change this so it is atomic across all KeyValues.    *<p>    * This is called under row lock, so Get operations will still see updates atomically. Scans will    * only see each KeyValue update as atomic.    * @param cells    * @param readpoint readpoint below which we can safely remove duplicate Cells.    * @return change in memstore size    */
name|long
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
function_decl|;
comment|/**    * @return Total memory occupied by this MemStore.    */
name|long
name|size
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

