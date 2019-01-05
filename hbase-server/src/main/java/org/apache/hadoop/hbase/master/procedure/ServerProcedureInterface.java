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
name|master
operator|.
name|procedure
package|;
end_package

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
name|ServerName
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

begin_comment
comment|/**  * Procedures that handle servers -- e.g. server crash -- must implement this Interface.  * It is used by the procedure runner to figure locking and what queuing.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|ServerProcedureInterface
block|{
specifier|public
enum|enum
name|ServerOperationType
block|{
name|CRASH_HANDLER
block|,
name|SWITCH_RPC_THROTTLE
block|,
comment|/**      * help find a available region server as worker and release worker after task done      * invoke SPLIT_WAL_REMOTE operation to send real WAL splitting request to worker      * manage the split wal task flow, will retry if SPLIT_WAL_REMOTE failed      */
name|SPLIT_WAL
block|,
comment|/**      * send the split WAL request to region server and handle the response      */
name|SPLIT_WAL_REMOTE
block|}
comment|/**    * @return Name of this server instance.    */
name|ServerName
name|getServerName
parameter_list|()
function_decl|;
comment|/**    * @return True if this server has an hbase:meta table region.    */
name|boolean
name|hasMetaTableRegion
parameter_list|()
function_decl|;
comment|/**    * Given an operation type we can take decisions about what to do with pending operations.    * e.g. if we get a crash handler and we have some assignment operation pending    * we can abort those operations.    * @return the operation type that the procedure is executing.    */
name|ServerOperationType
name|getServerOperationType
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

