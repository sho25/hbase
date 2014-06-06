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
name|master
operator|.
name|AssignmentManager
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
name|RegionServerServices
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

begin_comment
comment|/**  * Cocoordination operations for opening regions.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|OpenRegionCoordination
block|{
comment|//---------------------
comment|// RS-side operations
comment|//---------------------
comment|/**    * Tries to move regions to OPENED state.    *    * @param r Region we're working on.    * @param ord details about region opening task    * @return whether transition was successful or not    * @throws java.io.IOException    */
name|boolean
name|transitionToOpened
parameter_list|(
name|HRegion
name|r
parameter_list|,
name|OpenRegionDetails
name|ord
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Transitions region from offline to opening state.    * @param regionInfo region we're working on.    * @param ord details about opening task.    * @return true if successful, false otherwise    */
name|boolean
name|transitionFromOfflineToOpening
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
name|OpenRegionDetails
name|ord
parameter_list|)
function_decl|;
comment|/**    * Heartbeats to prevent timeouts.    *    * @param ord details about opening task.    * @param regionInfo region we're working on.    * @param rsServices instance of RegionServerrServices    * @param context used for logging purposes only    * @return true if successful heartbeat, false otherwise.    */
name|boolean
name|tickleOpening
parameter_list|(
name|OpenRegionDetails
name|ord
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|,
name|RegionServerServices
name|rsServices
parameter_list|,
name|String
name|context
parameter_list|)
function_decl|;
comment|/**    * Tries transition region from offline to failed open.    * @param rsServices instance of RegionServerServices    * @param hri region we're working on    * @param ord details about region opening task    * @return true if successful, false otherwise    */
name|boolean
name|tryTransitionFromOfflineToFailedOpen
parameter_list|(
name|RegionServerServices
name|rsServices
parameter_list|,
name|HRegionInfo
name|hri
parameter_list|,
name|OpenRegionDetails
name|ord
parameter_list|)
function_decl|;
comment|/**    * Tries transition from Opening to Failed open.    * @param hri region we're working on    * @param ord details about region opening task    * @return true if successfu. false otherwise.    */
name|boolean
name|tryTransitionFromOpeningToFailedOpen
parameter_list|(
name|HRegionInfo
name|hri
parameter_list|,
name|OpenRegionDetails
name|ord
parameter_list|)
function_decl|;
comment|/**    * Construct OpenRegionDetails instance from part of protobuf request.    * @return instance of OpenRegionDetails.    */
name|OpenRegionDetails
name|parseFromProtoRequest
parameter_list|(
name|AdminProtos
operator|.
name|OpenRegionRequest
operator|.
name|RegionOpenInfo
name|regionOpenInfo
parameter_list|)
function_decl|;
comment|/**    * Get details object with params for case when we're opening on    * regionserver side with all "default" properties.    */
name|OpenRegionDetails
name|getDetailsForNonCoordinatedOpening
parameter_list|()
function_decl|;
comment|//-------------------------
comment|// HMaster-side operations
comment|//-------------------------
comment|/**    * Commits opening operation on HM side (steps required for "commit"    * are determined by coordination implementation).    * @return true if committed successfully, false otherwise.    */
specifier|public
name|boolean
name|commitOpenOnMasterSide
parameter_list|(
name|AssignmentManager
name|assignmentManager
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|,
name|OpenRegionDetails
name|ord
parameter_list|)
function_decl|;
comment|/**    * Interface for region opening tasks. Used to carry implementation details in    * encapsulated way through Handlers to the coordination API.    */
specifier|static
interface|interface
name|OpenRegionDetails
block|{
comment|/**      * Sets server name on which opening operation is running.      */
name|void
name|setServerName
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
function_decl|;
comment|/**      * @return server name on which opening op is running.      */
name|ServerName
name|getServerName
parameter_list|()
function_decl|;
block|}
block|}
end_interface

end_unit

