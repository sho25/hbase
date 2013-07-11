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
name|errorhandling
operator|.
name|ForeignException
import|;
end_import

begin_comment
comment|/**  * This is the notification interface for Procedures that encapsulates message passing from  * members to a coordinator.  Each of these calls should send a message to the coordinator.  */
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
name|ProcedureMemberRpcs
extends|extends
name|Closeable
block|{
comment|/**    * Initialize and start any threads or connections the member needs.    */
name|void
name|start
parameter_list|(
specifier|final
name|String
name|memberName
parameter_list|,
specifier|final
name|ProcedureMember
name|member
parameter_list|)
function_decl|;
comment|/**    * Each subprocedure is being executed on a member.  This is the identifier for the member.    * @return the member name    */
name|String
name|getMemberName
parameter_list|()
function_decl|;
comment|/**    * Notify the coordinator that we aborted the specified {@link Subprocedure}    *    * @param sub the {@link Subprocedure} we are aborting    * @param cause the reason why the member's subprocedure aborted    * @throws IOException thrown when the rpcs can't reach the other members of the procedure (and    *  thus can't recover).    */
name|void
name|sendMemberAborted
parameter_list|(
name|Subprocedure
name|sub
parameter_list|,
name|ForeignException
name|cause
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Notify the coordinator that the specified {@link Subprocedure} has acquired the locally required    * barrier condition.    *    * @param sub the specified {@link Subprocedure}    * @throws IOException if we can't reach the coordinator    */
name|void
name|sendMemberAcquired
parameter_list|(
name|Subprocedure
name|sub
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Notify the coordinator that the specified {@link Subprocedure} has completed the work that    * needed to be done under the global barrier.    *    * @param sub the specified {@link Subprocedure}    * @throws IOException if we can't reach the coordinator    */
name|void
name|sendMemberCompleted
parameter_list|(
name|Subprocedure
name|sub
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

