begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|// Functions implemented by all the master protocols (e.g. MasterAdminProtocol,
end_comment

begin_comment
comment|// MasterMonitorProtocol).  Currently, this is only isMasterRunning, which is used,
end_comment

begin_comment
comment|// on proxy creation, to check if the master has been stopped.  If it has,
end_comment

begin_comment
comment|// a MasterNotRunningException is thrown back to the client, and the client retries.
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
name|protobuf
operator|.
name|generated
operator|.
name|MasterProtos
operator|.
name|IsMasterRunningRequest
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
name|protobuf
operator|.
name|generated
operator|.
name|MasterProtos
operator|.
name|IsMasterRunningResponse
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
name|protobuf
operator|.
name|generated
operator|.
name|MasterProtos
operator|.
name|MasterService
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
name|ipc
operator|.
name|VersionedProtocol
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
name|RpcController
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
name|ServiceException
import|;
end_import

begin_interface
specifier|public
interface|interface
name|MasterProtocol
extends|extends
name|VersionedProtocol
extends|,
name|MasterService
operator|.
name|BlockingInterface
block|{
comment|/**    * @param c Unused (set to null).    * @param req IsMasterRunningRequest    * @return IsMasterRunningRequest that contains:<br>    * isMasterRunning: true if master is available    * @throws ServiceException    */
specifier|public
name|IsMasterRunningResponse
name|isMasterRunning
parameter_list|(
name|RpcController
name|c
parameter_list|,
name|IsMasterRunningRequest
name|req
parameter_list|)
throws|throws
name|ServiceException
function_decl|;
block|}
end_interface

end_unit

