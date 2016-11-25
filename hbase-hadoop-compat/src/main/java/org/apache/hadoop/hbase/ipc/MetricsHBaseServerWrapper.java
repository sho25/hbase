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
name|ipc
package|;
end_package

begin_interface
specifier|public
interface|interface
name|MetricsHBaseServerWrapper
block|{
name|long
name|getTotalQueueSize
parameter_list|()
function_decl|;
name|int
name|getGeneralQueueLength
parameter_list|()
function_decl|;
name|int
name|getReplicationQueueLength
parameter_list|()
function_decl|;
name|int
name|getPriorityQueueLength
parameter_list|()
function_decl|;
name|int
name|getNumOpenConnections
parameter_list|()
function_decl|;
name|int
name|getActiveRpcHandlerCount
parameter_list|()
function_decl|;
name|long
name|getNumGeneralCallsDropped
parameter_list|()
function_decl|;
name|long
name|getNumLifoModeSwitches
parameter_list|()
function_decl|;
name|int
name|getWriteQueueLength
parameter_list|()
function_decl|;
name|int
name|getReadQueueLength
parameter_list|()
function_decl|;
name|int
name|getScanQueueLength
parameter_list|()
function_decl|;
name|int
name|getActiveWriteRpcHandlerCount
parameter_list|()
function_decl|;
name|int
name|getActiveReadRpcHandlerCount
parameter_list|()
function_decl|;
name|int
name|getActiveScanRpcHandlerCount
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

