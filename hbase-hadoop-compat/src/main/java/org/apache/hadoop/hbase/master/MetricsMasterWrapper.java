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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|util
operator|.
name|PairOfSameType
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
comment|/**  * This is the interface that will expose information to hadoop1/hadoop2 implementations of the  * MetricsMasterSource.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MetricsMasterWrapper
block|{
comment|/**    * Get ServerName    */
name|String
name|getServerName
parameter_list|()
function_decl|;
comment|/**    * Get Average Load    *    * @return Average Load    */
name|double
name|getAverageLoad
parameter_list|()
function_decl|;
comment|/**    * Get the Cluster ID    *    * @return Cluster ID    */
name|String
name|getClusterId
parameter_list|()
function_decl|;
comment|/**    * Get the ZooKeeper Quorum Info    *    * @return ZooKeeper Quorum Info    */
name|String
name|getZookeeperQuorum
parameter_list|()
function_decl|;
comment|/**    * Get the co-processors    *    * @return Co-processors    */
name|String
index|[]
name|getCoprocessors
parameter_list|()
function_decl|;
comment|/**    * Get hbase master start time    *    * @return Start time of master in milliseconds    */
name|long
name|getStartTime
parameter_list|()
function_decl|;
comment|/**    * Get the hbase master active time    *    * @return Time in milliseconds when master became active    */
name|long
name|getActiveTime
parameter_list|()
function_decl|;
comment|/**    * Whether this master is the active master    *    * @return True if this is the active master    */
name|boolean
name|getIsActiveMaster
parameter_list|()
function_decl|;
comment|/**    * Get the live region servers    *    * @return Live region servers    */
name|String
name|getRegionServers
parameter_list|()
function_decl|;
comment|/**    * Get the number of live region servers    *    * @return number of Live region servers    */
name|int
name|getNumRegionServers
parameter_list|()
function_decl|;
comment|/**    * Get the dead region servers    *    * @return Dead region Servers    */
name|String
name|getDeadRegionServers
parameter_list|()
function_decl|;
comment|/**    * Get the number of dead region servers    *    * @return number of Dead region Servers    */
name|int
name|getNumDeadRegionServers
parameter_list|()
function_decl|;
comment|/**    * Get the number of master WAL files.    */
name|long
name|getNumWALFiles
parameter_list|()
function_decl|;
comment|/**    * Get the number of region split plans executed.    */
name|long
name|getSplitPlanCount
parameter_list|()
function_decl|;
comment|/**    * Get the number of region merge plans executed.    */
name|long
name|getMergePlanCount
parameter_list|()
function_decl|;
comment|/**    * Gets the space usage and limit for each table.    */
name|Map
argument_list|<
name|String
argument_list|,
name|Entry
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
argument_list|>
name|getTableSpaceUtilization
parameter_list|()
function_decl|;
comment|/**    * Gets the space usage and limit for each namespace.    */
name|Map
argument_list|<
name|String
argument_list|,
name|Entry
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
argument_list|>
name|getNamespaceSpaceUtilization
parameter_list|()
function_decl|;
comment|/**    * Get the time in Millis when the master finished initializing/becoming the active master    */
name|long
name|getMasterInitializationTime
parameter_list|()
function_decl|;
comment|/**    * Get the online and offline region counts    *    * @return pair of count for online regions and offline regions    */
name|PairOfSameType
argument_list|<
name|Integer
argument_list|>
name|getRegionCounts
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

