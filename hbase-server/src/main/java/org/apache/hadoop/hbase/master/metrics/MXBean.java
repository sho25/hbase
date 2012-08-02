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
name|metrics
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
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * This is the JMX management interface for Hbase master information  */
end_comment

begin_interface
annotation|@
name|InterfaceStability
operator|.
name|Evolving
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MXBean
block|{
comment|/**    * Required for MXBean implementation    */
specifier|public
specifier|static
interface|interface
name|RegionsInTransitionInfo
block|{
comment|/**      * Name of region in transition      */
specifier|public
name|String
name|getRegionName
parameter_list|()
function_decl|;
comment|/**      * Current transition state      */
specifier|public
name|String
name|getRegionState
parameter_list|()
function_decl|;
comment|/**      * Get Region Server name      */
specifier|public
name|String
name|getRegionServerName
parameter_list|()
function_decl|;
comment|/**      * Get last update time      */
specifier|public
name|long
name|getLastUpdateTime
parameter_list|()
function_decl|;
block|}
comment|/**    * Get ServerName    */
specifier|public
name|String
name|getServerName
parameter_list|()
function_decl|;
comment|/**    * Get Average Load    * @return Average Load    */
specifier|public
name|double
name|getAverageLoad
parameter_list|()
function_decl|;
comment|/**    * Get the Cluster ID    * @return Cluster ID    */
specifier|public
name|String
name|getClusterId
parameter_list|()
function_decl|;
comment|/**    * Get the Zookeeper Quorum Info    * @return Zookeeper Quorum Info    */
specifier|public
name|String
name|getZookeeperQuorum
parameter_list|()
function_decl|;
comment|/**    * Get the co-processors    * @return Co-processors    */
specifier|public
name|String
index|[]
name|getCoprocessors
parameter_list|()
function_decl|;
comment|/**    * Get hbase master start time    * @return Start time of master in milliseconds    */
specifier|public
name|long
name|getMasterStartTime
parameter_list|()
function_decl|;
comment|/**    * Get the hbase master active time    * @return Time in milliseconds when master became active    */
specifier|public
name|long
name|getMasterActiveTime
parameter_list|()
function_decl|;
comment|/**    * Whether this master is the active master    * @return True if this is the active master    */
specifier|public
name|boolean
name|getIsActiveMaster
parameter_list|()
function_decl|;
comment|/**    * Get the live region servers    * @return Live region servers    */
specifier|public
name|int
name|getRegionServers
parameter_list|()
function_decl|;
comment|/**    * Get the dead region servers    * @return Dead region Servers    */
specifier|public
name|String
index|[]
name|getDeadRegionServers
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

