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
name|hbase
operator|.
name|metrics
operator|.
name|BaseMetricsSource
import|;
end_import

begin_comment
comment|/**  * Interface that classes that expose metrics about the master will implement.  */
end_comment

begin_interface
specifier|public
interface|interface
name|MasterMetricsSource
extends|extends
name|BaseMetricsSource
block|{
comment|/**    * The name of the metrics    */
specifier|static
specifier|final
name|String
name|METRICS_NAME
init|=
literal|"HMaster"
decl_stmt|;
comment|/**    * The context metrics will be under.    */
specifier|static
specifier|final
name|String
name|METRICS_CONTEXT
init|=
literal|"hmaster"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under in jmx    */
specifier|static
specifier|final
name|String
name|METRICS_JMX_CONTEXT
init|=
literal|"HMaster"
decl_stmt|;
comment|/**    * Description    */
specifier|static
specifier|final
name|String
name|METRICS_DESCRIPTION
init|=
literal|"Metrics about HBase master server"
decl_stmt|;
comment|// Strings used for exporting to metrics system.
specifier|static
specifier|final
name|String
name|MASTER_ACTIVE_TIME_NAME
init|=
literal|"masterActiveTime"
decl_stmt|;
specifier|static
specifier|final
name|String
name|MASTER_START_TIME_NAME
init|=
literal|"masterStartTime"
decl_stmt|;
specifier|static
specifier|final
name|String
name|AVERAGE_LOAD_NAME
init|=
literal|"averageLoad"
decl_stmt|;
specifier|static
specifier|final
name|String
name|NUM_REGION_SERVERS_NAME
init|=
literal|"numRegionServers"
decl_stmt|;
specifier|static
specifier|final
name|String
name|NUM_DEAD_REGION_SERVERS_NAME
init|=
literal|"numDeadRegionServers"
decl_stmt|;
specifier|static
specifier|final
name|String
name|ZOOKEEPER_QUORUM_NAME
init|=
literal|"zookeeperQuorum"
decl_stmt|;
specifier|static
specifier|final
name|String
name|SERVER_NAME_NAME
init|=
literal|"serverName"
decl_stmt|;
specifier|static
specifier|final
name|String
name|CLUSTER_ID_NAME
init|=
literal|"clusterId"
decl_stmt|;
specifier|static
specifier|final
name|String
name|IS_ACTIVE_MASTER_NAME
init|=
literal|"isActiveMaster"
decl_stmt|;
specifier|static
specifier|final
name|String
name|SPLIT_TIME_NAME
init|=
literal|"hlogSplitTime"
decl_stmt|;
specifier|static
specifier|final
name|String
name|SPLIT_SIZE_NAME
init|=
literal|"hlogSplitSize"
decl_stmt|;
specifier|static
specifier|final
name|String
name|CLUSTER_REQUESTS_NAME
init|=
literal|"clusterRequests"
decl_stmt|;
specifier|static
specifier|final
name|String
name|RIT_COUNT_NAME
init|=
literal|"ritCount"
decl_stmt|;
specifier|static
specifier|final
name|String
name|RIT_COUNT_OVER_THRESHOLD_NAME
init|=
literal|"ritCountOverThreshold"
decl_stmt|;
specifier|static
specifier|final
name|String
name|RIT_OLDEST_AGE_NAME
init|=
literal|"ritOldestAge"
decl_stmt|;
specifier|static
specifier|final
name|String
name|MASTER_ACTIVE_TIME_DESC
init|=
literal|"Master Active Time"
decl_stmt|;
specifier|static
specifier|final
name|String
name|MASTER_START_TIME_DESC
init|=
literal|"Master Start Time"
decl_stmt|;
specifier|static
specifier|final
name|String
name|AVERAGE_LOAD_DESC
init|=
literal|"AverageLoad"
decl_stmt|;
specifier|static
specifier|final
name|String
name|NUMBER_OF_REGION_SERVERS_DESC
init|=
literal|"Number of RegionServers"
decl_stmt|;
specifier|static
specifier|final
name|String
name|NUMBER_OF_DEAD_REGION_SERVERS_DESC
init|=
literal|"Number of dead RegionServers"
decl_stmt|;
specifier|static
specifier|final
name|String
name|ZOOKEEPER_QUORUM_DESC
init|=
literal|"Zookeeper Quorum"
decl_stmt|;
specifier|static
specifier|final
name|String
name|SERVER_NAME_DESC
init|=
literal|"Server Name"
decl_stmt|;
specifier|static
specifier|final
name|String
name|CLUSTER_ID_DESC
init|=
literal|"Cluster Id"
decl_stmt|;
specifier|static
specifier|final
name|String
name|IS_ACTIVE_MASTER_DESC
init|=
literal|"Is Active Master"
decl_stmt|;
specifier|static
specifier|final
name|String
name|SPLIT_TIME_DESC
init|=
literal|"Time it takes to finish HLog.splitLog()"
decl_stmt|;
specifier|static
specifier|final
name|String
name|SPLIT_SIZE_DESC
init|=
literal|"Size of HLog files being split"
decl_stmt|;
comment|/**    * Increment the number of requests the cluster has seen.    * @param inc Ammount to increment the total by.    */
name|void
name|incRequests
parameter_list|(
specifier|final
name|int
name|inc
parameter_list|)
function_decl|;
comment|/**    * Set the number of regions in transition.    * @param ritCount count of the regions in transition.    */
name|void
name|setRIT
parameter_list|(
name|int
name|ritCount
parameter_list|)
function_decl|;
comment|/**    * Set the count of the number of regions that have been in transition over the threshold time.    * @param ritCountOverThreshold number of regions in transition for longer than threshold.    */
name|void
name|setRITCountOverThreshold
parameter_list|(
name|int
name|ritCountOverThreshold
parameter_list|)
function_decl|;
comment|/**    * Set the oldest region in transition.    * @param age age of the oldest RIT.    */
name|void
name|setRITOldestAge
parameter_list|(
name|long
name|age
parameter_list|)
function_decl|;
name|void
name|updateSplitTime
parameter_list|(
name|long
name|time
parameter_list|)
function_decl|;
name|void
name|updateSplitSize
parameter_list|(
name|long
name|size
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

