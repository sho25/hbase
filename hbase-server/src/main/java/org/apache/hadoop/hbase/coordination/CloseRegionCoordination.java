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
name|coordination
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
name|HRegionInfo
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
name|ServerName
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
name|AdminProtos
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
name|HRegion
import|;
end_import

begin_comment
comment|/**  * Coordinated operations for close region handlers.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|CloseRegionCoordination
block|{
comment|/**    * Called before actual region closing to check that we can do close operation    * on this region.    * @param regionInfo region being closed    * @param crd details about closing operation    * @return true if caller shall proceed and close, false if need to abort closing.    */
name|boolean
name|checkClosingState
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
name|CloseRegionDetails
name|crd
parameter_list|)
function_decl|;
comment|/**    * Called after region is closed to notify all interesting parties / "register"    * region as finally closed.    * @param region region being closed    * @param sn ServerName on which task runs    * @param crd details about closing operation    */
name|void
name|setClosedState
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|ServerName
name|sn
parameter_list|,
name|CloseRegionDetails
name|crd
parameter_list|)
function_decl|;
comment|/**    * Construct CloseRegionDetails instance from CloseRegionRequest.    * @return instance of CloseRegionDetails    */
name|CloseRegionDetails
name|parseFromProtoRequest
parameter_list|(
name|AdminProtos
operator|.
name|CloseRegionRequest
name|request
parameter_list|)
function_decl|;
comment|/**    * Get details object with params for case when we're closing on    * regionserver side internally (not because of RPC call from master),    * so we don't parse details from protobuf request.    */
name|CloseRegionDetails
name|getDetaultDetails
parameter_list|()
function_decl|;
comment|/**    * Marker interface for region closing tasks. Used to carry implementation details in    * encapsulated way through Handlers to the consensus API.    */
specifier|static
interface|interface
name|CloseRegionDetails
block|{   }
block|}
end_interface

end_unit

