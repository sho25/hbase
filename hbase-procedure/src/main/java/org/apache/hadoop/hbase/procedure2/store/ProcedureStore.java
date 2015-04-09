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
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|Public
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
comment|/**      * triggered when the store is not able to write out data.      * the main process should abort.      */
name|void
name|abortProcess
parameter_list|()
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
comment|/**    * Acquire the lease for the procedure store.    */
name|void
name|recoverLease
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Load the Procedures in the store.    * @return the set of procedures present in the store    */
name|Iterator
argument_list|<
name|Procedure
argument_list|>
name|load
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * When a procedure is submitted to the executor insert(proc, null) will be called.    * 'proc' has a 'RUNNABLE' state and the initial information required to start up.    *    * When a procedure is executed and it returns children insert(proc, subprocs) will be called.    * 'proc' has a 'WAITING' state and an update state.    * 'subprocs' are the children in 'RUNNABLE' state with the initial information.    *    * @param proc the procedure to serialize and write to the store.    * @param subprocs the newly created child of the proc.    */
name|void
name|insert
parameter_list|(
name|Procedure
name|proc
parameter_list|,
name|Procedure
index|[]
name|subprocs
parameter_list|)
function_decl|;
comment|/**    * The specified procedure was executed,    * and the new state should be written to the store.    * @param proc the procedure to serialize and write to the store.    */
name|void
name|update
parameter_list|(
name|Procedure
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
block|}
end_interface

end_unit

