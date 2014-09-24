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
name|master
package|;
end_package

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
name|java
operator|.
name|util
operator|.
name|Map
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
name|conf
operator|.
name|Configurable
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
name|ClusterStatus
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
name|HBaseIOException
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
name|Stoppable
import|;
end_import

begin_comment
comment|/**  * Makes decisions about the placement and movement of Regions across  * RegionServers.  *  *<p>Cluster-wide load balancing will occur only when there are no regions in  * transition and according to a fixed period of a time using {@link #balanceCluster(Map)}.  *  *<p>Inline region placement with {@link #immediateAssignment} can be used when  * the Master needs to handle closed regions that it currently does not have  * a destination set for.  This can happen during master failover.  *  *<p>On cluster startup, bulk assignment can be used to determine  * locations for all Regions in a cluster.  *  *<p>This classes produces plans for the {@link AssignmentManager} to execute.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|LoadBalancer
extends|extends
name|Configurable
extends|,
name|Stoppable
block|{
comment|/**    * Set the current cluster status.  This allows a LoadBalancer to map host name to a server    * @param st    */
name|void
name|setClusterStatus
parameter_list|(
name|ClusterStatus
name|st
parameter_list|)
function_decl|;
comment|/**    * Set the master service.    * @param masterServices    */
name|void
name|setMasterServices
parameter_list|(
name|MasterServices
name|masterServices
parameter_list|)
function_decl|;
comment|/**    * Perform the major balance operation    * @param clusterState    * @return List of plans    */
name|List
argument_list|<
name|RegionPlan
argument_list|>
name|balanceCluster
parameter_list|(
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|clusterState
parameter_list|)
throws|throws
name|HBaseIOException
function_decl|;
comment|/**    * Perform a Round Robin assignment of regions.    * @param regions    * @param servers    * @return Map of servername to regioninfos    */
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|roundRobinAssignment
parameter_list|(
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
parameter_list|)
throws|throws
name|HBaseIOException
function_decl|;
comment|/**    * Assign regions to the previously hosting region server    * @param regions    * @param servers    * @return List of plans    */
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|retainAssignment
parameter_list|(
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|regions
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
parameter_list|)
throws|throws
name|HBaseIOException
function_decl|;
comment|/**    * Sync assign a region    * @param regions    * @param servers     * @return Map regioninfos to servernames    */
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|immediateAssignment
parameter_list|(
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
parameter_list|)
throws|throws
name|HBaseIOException
function_decl|;
comment|/**    * Get a random region server from the list    * @param regionInfo Region for which this selection is being done.    * @param servers    * @return Servername    */
name|ServerName
name|randomAssignment
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
parameter_list|)
throws|throws
name|HBaseIOException
function_decl|;
comment|/**    * Initialize the load balancer. Must be called after setters.    * @throws HBaseIOException    */
name|void
name|initialize
parameter_list|()
throws|throws
name|HBaseIOException
function_decl|;
comment|/**    * Marks the region as online at balancer.    * @param regionInfo    * @param sn    */
name|void
name|regionOnline
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
name|ServerName
name|sn
parameter_list|)
function_decl|;
comment|/**    * Marks the region as offline at balancer.    * @param regionInfo    */
name|void
name|regionOffline
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

