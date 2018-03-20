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

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MetricsAssignmentManagerSource
extends|extends
name|BaseSource
block|{
comment|/**    * The name of the metrics    */
name|String
name|METRICS_NAME
init|=
literal|"AssignmentManager"
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
literal|"Metrics about HBase master assignment manager."
decl_stmt|;
comment|// RIT metrics
name|String
name|RIT_COUNT_NAME
init|=
literal|"ritCount"
decl_stmt|;
name|String
name|RIT_COUNT_OVER_THRESHOLD_NAME
init|=
literal|"ritCountOverThreshold"
decl_stmt|;
name|String
name|RIT_OLDEST_AGE_NAME
init|=
literal|"ritOldestAge"
decl_stmt|;
name|String
name|RIT_DURATION_NAME
init|=
literal|"ritDuration"
decl_stmt|;
name|String
name|RIT_COUNT_DESC
init|=
literal|"Current number of Regions In Transition (Gauge)."
decl_stmt|;
name|String
name|RIT_COUNT_OVER_THRESHOLD_DESC
init|=
literal|"Current number of Regions In Transition over threshold time (Gauge)."
decl_stmt|;
name|String
name|RIT_OLDEST_AGE_DESC
init|=
literal|"Timestamp in milliseconds of the oldest Region In Transition (Gauge)."
decl_stmt|;
name|String
name|RIT_DURATION_DESC
init|=
literal|"Total durations in milliseconds for all Regions in Transition (Histogram)."
decl_stmt|;
name|String
name|ASSIGN_METRIC_PREFIX
init|=
literal|"assign"
decl_stmt|;
name|String
name|UNASSIGN_METRIC_PREFIX
init|=
literal|"unassign"
decl_stmt|;
name|String
name|SPLIT_METRIC_PREFIX
init|=
literal|"split"
decl_stmt|;
name|String
name|MERGE_METRIC_PREFIX
init|=
literal|"merge"
decl_stmt|;
name|String
name|OPERATION_COUNT_NAME
init|=
literal|"operationCount"
decl_stmt|;
comment|/**    * Set the number of regions in transition.    *    * @param ritCount count of the regions in transition.    */
name|void
name|setRIT
parameter_list|(
name|int
name|ritCount
parameter_list|)
function_decl|;
comment|/**    * Set the count of the number of regions that have been in transition over the threshold time.    *    * @param ritCountOverThreshold number of regions in transition for longer than threshold.    */
name|void
name|setRITCountOverThreshold
parameter_list|(
name|int
name|ritCountOverThreshold
parameter_list|)
function_decl|;
comment|/**    * Set the oldest region in transition.    *    * @param age age of the oldest RIT.    */
name|void
name|setRITOldestAge
parameter_list|(
name|long
name|age
parameter_list|)
function_decl|;
name|void
name|updateRitDuration
parameter_list|(
name|long
name|duration
parameter_list|)
function_decl|;
comment|/**    * TODO: Remove. This may not be needed now as assign and unassign counts are tracked separately    * Increment the count of operations (assign/unassign).    */
name|void
name|incrementOperationCounter
parameter_list|()
function_decl|;
comment|/**    * @return {@link OperationMetrics} containing common metrics for assign operation    */
name|OperationMetrics
name|getAssignMetrics
parameter_list|()
function_decl|;
comment|/**    * @return {@link OperationMetrics} containing common metrics for unassign operation    */
name|OperationMetrics
name|getUnassignMetrics
parameter_list|()
function_decl|;
comment|/**    * @return {@link OperationMetrics} containing common metrics for split operation    */
name|OperationMetrics
name|getSplitMetrics
parameter_list|()
function_decl|;
comment|/**    * @return {@link OperationMetrics} containing common metrics for merge operation    */
name|OperationMetrics
name|getMergeMetrics
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

