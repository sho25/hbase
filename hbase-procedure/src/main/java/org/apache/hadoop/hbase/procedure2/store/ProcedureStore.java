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
name|procedure2
operator|.
name|store
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
name|yetus
operator|.
name|audience
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
name|procedure2
operator|.
name|Procedure
import|;
end_import

begin_comment
comment|/**  * The ProcedureStore is used by the executor to persist the state of each procedure execution.  * This allows to resume the execution of pending/in-progress procedures in case  * of machine failure or service shutdown.  */
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
name|ProcedureStore
block|{
comment|/**    * Store listener interface.    * The main process should register a listener and respond to the store events.    */
specifier|public
interface|interface
name|ProcedureStoreListener
block|{
comment|/**      * triggered when the store sync is completed.      */
name|void
name|postSync
parameter_list|()
function_decl|;
comment|/**      * triggered when the store is not able to write out data.      * the main process should abort.      */
name|void
name|abortProcess
parameter_list|()
function_decl|;
block|}
comment|/**    * An Iterator over a collection of Procedure    */
specifier|public
interface|interface
name|ProcedureIterator
block|{
comment|/**      * Reset the Iterator by seeking to the beginning of the list.      */
name|void
name|reset
parameter_list|()
function_decl|;
comment|/**      * Returns true if the iterator has more elements.      * (In other words, returns true if next() would return a Procedure      * rather than throwing an exception.)      * @return true if the iterator has more procedures      */
name|boolean
name|hasNext
parameter_list|()
function_decl|;
comment|/**      * @return true if the iterator next element is a completed procedure.      */
name|boolean
name|isNextFinished
parameter_list|()
function_decl|;
comment|/**      * Skip the next procedure      */
name|void
name|skipNext
parameter_list|()
function_decl|;
comment|/**      * Returns the next procedure in the iteration.      * @throws IOException if there was an error fetching/deserializing the procedure      * @return the next procedure in the iteration.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
name|Procedure
name|next
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    * Interface passed to the ProcedureStore.load() method to handle the store-load events.    */
specifier|public
interface|interface
name|ProcedureLoader
block|{
comment|/**      * Called by ProcedureStore.load() to notify about the maximum proc-id in the store.      * @param maxProcId the highest proc-id in the store      */
name|void
name|setMaxProcId
parameter_list|(
name|long
name|maxProcId
parameter_list|)
function_decl|;
comment|/**      * Called by the ProcedureStore.load() every time a set of procedures are ready to be executed.      * The ProcedureIterator passed to the method, has the procedure sorted in replay-order.      * @param procIter iterator over the procedures ready to be added to the executor.      */
name|void
name|load
parameter_list|(
name|ProcedureIterator
name|procIter
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Called by the ProcedureStore.load() in case we have procedures not-ready to be added to      * the executor, which probably means they are corrupted since some information/link is missing.      * @param procIter iterator over the procedures not ready to be added to the executor, corrupted      */
name|void
name|handleCorrupted
parameter_list|(
name|ProcedureIterator
name|procIter
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    * Add the listener to the notification list.    * @param listener The AssignmentListener to register    */
name|void
name|registerListener
parameter_list|(
name|ProcedureStoreListener
name|listener
parameter_list|)
function_decl|;
comment|/**    * Remove the listener from the notification list.    * @param listener The AssignmentListener to unregister    * @return true if the listner was in the list and it was removed, otherwise false.    */
name|boolean
name|unregisterListener
parameter_list|(
name|ProcedureStoreListener
name|listener
parameter_list|)
function_decl|;
comment|/**    * Start/Open the procedure store    * @param numThreads    */
name|void
name|start
parameter_list|(
name|int
name|numThreads
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Stop/Close the procedure store    * @param abort true if the stop is an abort    */
name|void
name|stop
parameter_list|(
name|boolean
name|abort
parameter_list|)
function_decl|;
comment|/**    * @return true if the store is running, otherwise false.    */
name|boolean
name|isRunning
parameter_list|()
function_decl|;
comment|/**    * @return the number of threads/slots passed to start()    */
name|int
name|getNumThreads
parameter_list|()
function_decl|;
comment|/**    * Set the number of procedure running.    * This can be used, for example, by the store to know how long to wait before a sync.    * @return how many procedures are running (may not be same as<code>count</code>).    */
name|int
name|setRunningProcedureCount
parameter_list|(
name|int
name|count
parameter_list|)
function_decl|;
comment|/**    * Acquire the lease for the procedure store.    */
name|void
name|recoverLease
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Load the Procedures in the store.    * @param loader the ProcedureLoader that will handle the store-load events    */
name|void
name|load
parameter_list|(
name|ProcedureLoader
name|loader
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * When a procedure is submitted to the executor insert(proc, null) will be called.    * 'proc' has a 'RUNNABLE' state and the initial information required to start up.    *    * When a procedure is executed and it returns children insert(proc, subprocs) will be called.    * 'proc' has a 'WAITING' state and an update state.    * 'subprocs' are the children in 'RUNNABLE' state with the initial information.    *    * @param proc the procedure to serialize and write to the store.    * @param subprocs the newly created child of the proc.    */
name|void
name|insert
parameter_list|(
name|Procedure
argument_list|<
name|?
argument_list|>
name|proc
parameter_list|,
name|Procedure
argument_list|<
name|?
argument_list|>
index|[]
name|subprocs
parameter_list|)
function_decl|;
comment|/**    * Serialize a set of new procedures.    * These procedures are freshly submitted to the executor and each procedure    * has a 'RUNNABLE' state and the initial information required to start up.    *    * @param procs the procedures to serialize and write to the store.    */
name|void
name|insert
parameter_list|(
name|Procedure
argument_list|<
name|?
argument_list|>
index|[]
name|procs
parameter_list|)
function_decl|;
comment|/**    * The specified procedure was executed,    * and the new state should be written to the store.    * @param proc the procedure to serialize and write to the store.    */
name|void
name|update
parameter_list|(
name|Procedure
argument_list|<
name|?
argument_list|>
name|proc
parameter_list|)
function_decl|;
comment|/**    * The specified procId was removed from the executor,    * due to completion, abort or failure.    * The store implementor should remove all the information about the specified procId.    * @param procId the ID of the procedure to remove.    */
name|void
name|delete
parameter_list|(
name|long
name|procId
parameter_list|)
function_decl|;
comment|/**    * The parent procedure completed.    * Update the state and mark all the child deleted.    * @param parentProc the parent procedure to serialize and write to the store.    * @param subProcIds the IDs of the sub-procedure to remove.    */
name|void
name|delete
parameter_list|(
name|Procedure
argument_list|<
name|?
argument_list|>
name|parentProc
parameter_list|,
name|long
index|[]
name|subProcIds
parameter_list|)
function_decl|;
comment|/**    * The specified procIds were removed from the executor,    * due to completion, abort or failure.    * The store implementor should remove all the information about the specified procIds.    * @param procIds the IDs of the procedures to remove.    * @param offset the array offset from where to start to delete    * @param count the number of IDs to delete    */
name|void
name|delete
parameter_list|(
name|long
index|[]
name|procIds
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|count
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

