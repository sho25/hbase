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
name|Collection
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
name|client
operator|.
name|Durability
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
name|protobuf
operator|.
name|Message
import|;
end_import

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
comment|/**  * Defines the procedure to atomically perform multiple scans and mutations  * on a HRegion.  *  * This is invoked by HRegion#processRowsWithLocks().  * This class performs scans and generates mutations and WAL edits.  * The locks and MVCC will be handled by HRegion.  *  * The RowProcessor user code could have data that needs to be   * sent across for proper initialization at the server side. The generic type   * parameter S is the type of the request data sent to the server.  * The generic type parameter T is the return type of RowProcessor.getResult().  */
specifier|public
interface|interface
name|RowProcessor
parameter_list|<
name|S
extends|extends
name|Message
parameter_list|,
name|T
extends|extends
name|Message
parameter_list|>
block|{
comment|/**    * Rows to lock while operation.    * They have to be sorted with<code>RowProcessor</code>    * to avoid deadlock.    */
name|Collection
argument_list|<
name|byte
index|[]
argument_list|>
name|getRowsToLock
parameter_list|()
function_decl|;
comment|/**    * Obtain the processing result. All row processor implementations must    * implement this, even if the method is simply returning an empty    * Message.    */
name|T
name|getResult
parameter_list|()
function_decl|;
comment|/**    * Is this operation read only? If this is true, process() should not add    * any mutations or it throws IOException.    * @return ture if read only operation    */
name|boolean
name|readOnly
parameter_list|()
function_decl|;
comment|/**    * HRegion handles the locks and MVCC and invokes this method properly.    *    * You should override this to create your own RowProcessor.    *    * If you are doing read-modify-write here, you should consider using    *<code>IsolationLevel.READ_UNCOMMITTED</code> for scan because    * we advance MVCC after releasing the locks for optimization purpose.    *    * @param now the current system millisecond    * @param region the HRegion    * @param mutations the output mutations to apply to memstore    * @param walEdit the output WAL edits to apply to write ahead log    */
name|void
name|process
parameter_list|(
name|long
name|now
parameter_list|,
name|HRegion
name|region
parameter_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
name|mutations
parameter_list|,
name|WALEdit
name|walEdit
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * The hook to be executed before process().    *    * @param region the HRegion    * @param walEdit the output WAL edits to apply to write ahead log    */
name|void
name|preProcess
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|WALEdit
name|walEdit
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * The hook to be executed after process().    *    * @param region the HRegion    * @param walEdit the output WAL edits to apply to write ahead log    */
name|void
name|postProcess
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|WALEdit
name|walEdit
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @return The cluster ids that have the change.    */
name|List
argument_list|<
name|UUID
argument_list|>
name|getClusterIds
parameter_list|()
function_decl|;
comment|/**    * Human readable name of the processor    * @return The name of the processor    */
name|String
name|getName
parameter_list|()
function_decl|;
comment|/**    * This method should return any additional data that is needed on the    * server side to construct the RowProcessor. The server will pass this to    * the {@link #initialize(Message msg)} method. If there is no RowProcessor    * specific data then null should be returned.    * @return the PB message    * @throws IOException    */
name|S
name|getRequestData
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * This method should initialize any field(s) of the RowProcessor with    * a parsing of the passed message bytes (used on the server side).    * @param msg    * @throws IOException    */
name|void
name|initialize
parameter_list|(
name|S
name|msg
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @return The {@link Durability} to use    */
name|Durability
name|useDurability
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

