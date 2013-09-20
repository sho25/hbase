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
name|procedure
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
name|errorhandling
operator|.
name|ForeignException
import|;
end_import

begin_comment
comment|/**  * RPCs for the coordinator to run a barriered procedure with subprocedures executed at  * distributed members.  * @see ProcedureCoordinator  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|ProcedureCoordinatorRpcs
extends|extends
name|Closeable
block|{
comment|/**    * Initialize and start threads necessary to connect an implementation's rpc mechanisms.    * @param listener    * @return true if succeed, false if encountered initialization errors.    */
name|boolean
name|start
parameter_list|(
specifier|final
name|ProcedureCoordinator
name|listener
parameter_list|)
function_decl|;
comment|/**    * Notify the members that the coordinator has aborted the procedure and that it should release    * barrier resources.    *    * @param procName name of the procedure that was aborted    * @param cause the reason why the procedure needs to be aborted    * @throws IOException if the rpcs can't reach the other members of the procedure (and can't    *           recover).    */
name|void
name|sendAbortToMembers
parameter_list|(
name|Procedure
name|procName
parameter_list|,
name|ForeignException
name|cause
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Notify the members to acquire barrier for the procedure    *    * @param procName name of the procedure to start    * @param info information that should be passed to all members    * @param members names of the members requested to reach the acquired phase    * @throws IllegalArgumentException if the procedure was already marked as failed    * @throws IOException if we can't reach the remote notification mechanism    */
name|void
name|sendGlobalBarrierAcquire
parameter_list|(
name|Procedure
name|procName
parameter_list|,
name|byte
index|[]
name|info
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|members
parameter_list|)
throws|throws
name|IOException
throws|,
name|IllegalArgumentException
function_decl|;
comment|/**    * Notify members that all members have acquired their parts of the barrier and that they can    * now execute under the global barrier.    *    * Must come after calling {@link #sendGlobalBarrierAcquire(Procedure, byte[], List)}    *    * @param procName name of the procedure to start    * @param members members to tell we have reached in-barrier phase    * @throws IOException if we can't reach the remote notification mechanism    */
name|void
name|sendGlobalBarrierReached
parameter_list|(
name|Procedure
name|procName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|members
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Notify Members to reset the distributed state for procedure    * @param procName name of the procedure to reset    * @throws IOException if the remote notification mechanism cannot be reached    */
name|void
name|resetMembers
parameter_list|(
name|Procedure
name|procName
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

