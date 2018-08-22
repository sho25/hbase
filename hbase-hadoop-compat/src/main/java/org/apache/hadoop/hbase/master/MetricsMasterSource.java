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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|metrics
operator|.
name|BaseSource
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
name|metrics
operator|.
name|OperationMetrics
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
comment|/**  * Interface that classes that expose metrics about the master will implement.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MetricsMasterSource
extends|extends
name|BaseSource
block|{
comment|/**    * The name of the metrics    */
name|String
name|METRICS_NAME
init|=
literal|"Server"
decl_stmt|;
comment|/**    * The context metrics will be under.    */
name|String
name|METRICS_CONTEXT
init|=
literal|"master"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under in jmx    */
name|String
name|METRICS_JMX_CONTEXT
init|=
literal|"Master,sub="
operator|+
name|METRICS_NAME
decl_stmt|;
comment|/**    * Description    */
name|String
name|METRICS_DESCRIPTION
init|=
literal|"Metrics about HBase master server"
decl_stmt|;
comment|// Strings used for exporting to metrics system.
name|String
name|MASTER_ACTIVE_TIME_NAME
init|=
literal|"masterActiveTime"
decl_stmt|;
name|String
name|MASTER_START_TIME_NAME
init|=
literal|"masterStartTime"
decl_stmt|;
name|String
name|MASTER_FINISHED_INITIALIZATION_TIME_NAME
init|=
literal|"masterFinishedInitializationTime"
decl_stmt|;
name|String
name|AVERAGE_LOAD_NAME
init|=
literal|"averageLoad"
decl_stmt|;
name|String
name|LIVE_REGION_SERVERS_NAME
init|=
literal|"liveRegionServers"
decl_stmt|;
name|String
name|DEAD_REGION_SERVERS_NAME
init|=
literal|"deadRegionServers"
decl_stmt|;
name|String
name|NUM_REGION_SERVERS_NAME
init|=
literal|"numRegionServers"
decl_stmt|;
name|String
name|NUM_DEAD_REGION_SERVERS_NAME
init|=
literal|"numDeadRegionServers"
decl_stmt|;
name|String
name|ZOOKEEPER_QUORUM_NAME
init|=
literal|"zookeeperQuorum"
decl_stmt|;
name|String
name|SERVER_NAME_NAME
init|=
literal|"serverName"
decl_stmt|;
name|String
name|CLUSTER_ID_NAME
init|=
literal|"clusterId"
decl_stmt|;
name|String
name|IS_ACTIVE_MASTER_NAME
init|=
literal|"isActiveMaster"
decl_stmt|;
name|String
name|SPLIT_PLAN_COUNT_NAME
init|=
literal|"splitPlanCount"
decl_stmt|;
name|String
name|MERGE_PLAN_COUNT_NAME
init|=
literal|"mergePlanCount"
decl_stmt|;
name|String
name|ONLINE_REGION_COUNT_NAME
init|=
literal|"onlineRegionCount"
decl_stmt|;
name|String
name|OFFLINE_REGION_COUNT_NAME
init|=
literal|"offlineRegionCount"
decl_stmt|;
name|String
name|CLUSTER_REQUESTS_NAME
init|=
literal|"clusterRequests"
decl_stmt|;
name|String
name|MASTER_ACTIVE_TIME_DESC
init|=
literal|"Master Active Time"
decl_stmt|;
name|String
name|MASTER_START_TIME_DESC
init|=
literal|"Master Start Time"
decl_stmt|;
name|String
name|MASTER_FINISHED_INITIALIZATION_TIME_DESC
init|=
literal|"Timestamp when Master has finished initializing"
decl_stmt|;
name|String
name|AVERAGE_LOAD_DESC
init|=
literal|"AverageLoad"
decl_stmt|;
name|String
name|LIVE_REGION_SERVERS_DESC
init|=
literal|"Names of live RegionServers"
decl_stmt|;
name|String
name|NUMBER_OF_REGION_SERVERS_DESC
init|=
literal|"Number of RegionServers"
decl_stmt|;
name|String
name|DEAD_REGION_SERVERS_DESC
init|=
literal|"Names of dead RegionServers"
decl_stmt|;
name|String
name|NUMBER_OF_DEAD_REGION_SERVERS_DESC
init|=
literal|"Number of dead RegionServers"
decl_stmt|;
name|String
name|ZOOKEEPER_QUORUM_DESC
init|=
literal|"ZooKeeper Quorum"
decl_stmt|;
name|String
name|SERVER_NAME_DESC
init|=
literal|"Server Name"
decl_stmt|;
name|String
name|CLUSTER_ID_DESC
init|=
literal|"Cluster Id"
decl_stmt|;
name|String
name|IS_ACTIVE_MASTER_DESC
init|=
literal|"Is Active Master"
decl_stmt|;
name|String
name|SPLIT_PLAN_COUNT_DESC
init|=
literal|"Number of Region Split Plans executed"
decl_stmt|;
name|String
name|MERGE_PLAN_COUNT_DESC
init|=
literal|"Number of Region Merge Plans executed"
decl_stmt|;
name|String
name|ONLINE_REGION_COUNT_DESC
init|=
literal|"Number of Online Regions"
decl_stmt|;
name|String
name|OFFLINE_REGION_COUNT_DESC
init|=
literal|"Number of Offline Regions"
decl_stmt|;
name|String
name|SERVER_CRASH_METRIC_PREFIX
init|=
literal|"serverCrash"
decl_stmt|;
comment|/**    * Increment the number of requests the cluster has seen.    *    * @param inc Ammount to increment the total by.    */
name|void
name|incRequests
parameter_list|(
specifier|final
name|long
name|inc
parameter_list|)
function_decl|;
comment|/**    * @return {@link OperationMetrics} containing common metrics for server crash operation    */
name|OperationMetrics
name|getServerCrashMetrics
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

