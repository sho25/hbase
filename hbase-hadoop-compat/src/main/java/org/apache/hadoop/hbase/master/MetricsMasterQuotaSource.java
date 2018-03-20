begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * A collection of exposed metrics for space quotas from the HBase Master.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MetricsMasterQuotaSource
extends|extends
name|BaseSource
block|{
name|String
name|METRICS_NAME
init|=
literal|"Quotas"
decl_stmt|;
name|String
name|METRICS_CONTEXT
init|=
literal|"master"
decl_stmt|;
name|String
name|METRICS_JMX_CONTEXT
init|=
literal|"Master,sub="
operator|+
name|METRICS_NAME
decl_stmt|;
name|String
name|METRICS_DESCRIPTION
init|=
literal|"Metrics about HBase Quotas by the Master"
decl_stmt|;
name|String
name|NUM_SPACE_QUOTAS_NAME
init|=
literal|"numSpaceQuotas"
decl_stmt|;
name|String
name|NUM_SPACE_QUOTAS_DESC
init|=
literal|"Number of space quotas defined"
decl_stmt|;
name|String
name|NUM_TABLES_QUOTA_VIOLATIONS_NAME
init|=
literal|"numTablesInQuotaViolation"
decl_stmt|;
name|String
name|NUM_TABLES_QUOTA_VIOLATIONS_DESC
init|=
literal|"Number of tables violating space quotas"
decl_stmt|;
name|String
name|NUM_NS_QUOTA_VIOLATIONS_NAME
init|=
literal|"numNamespaceInQuotaViolation"
decl_stmt|;
name|String
name|NUM_NS_QUOTA_VIOLATIONS_DESC
init|=
literal|"Number of namespaces violating space quotas"
decl_stmt|;
name|String
name|NUM_REGION_SIZE_REPORTS_NAME
init|=
literal|"numRegionSizeReports"
decl_stmt|;
name|String
name|NUM_REGION_SIZE_REPORTS_DESC
init|=
literal|"Number of Region sizes reported"
decl_stmt|;
name|String
name|QUOTA_OBSERVER_CHORE_TIME_NAME
init|=
literal|"quotaObserverChoreTime"
decl_stmt|;
name|String
name|QUOTA_OBSERVER_CHORE_TIME_DESC
init|=
literal|"Histogram for the time in millis for the QuotaObserverChore"
decl_stmt|;
name|String
name|SNAPSHOT_OBSERVER_CHORE_TIME_NAME
init|=
literal|"snapshotQuotaObserverChoreTime"
decl_stmt|;
name|String
name|SNAPSHOT_OBSERVER_CHORE_TIME_DESC
init|=
literal|"Histogram for the time in millis for the SnapshotQuotaObserverChore"
decl_stmt|;
name|String
name|SNAPSHOT_OBSERVER_SIZE_COMPUTATION_TIME_NAME
init|=
literal|"snapshotObserverSizeComputationTime"
decl_stmt|;
name|String
name|SNAPSHOT_OBSERVER_SIZE_COMPUTATION_TIME_DESC
init|=
literal|"Histogram for the time in millis to compute the size of each snapshot"
decl_stmt|;
name|String
name|SNAPSHOT_OBSERVER_FETCH_TIME_NAME
init|=
literal|"snapshotObserverSnapshotFetchTime"
decl_stmt|;
name|String
name|SNAPSHOT_OBSERVER_FETCH_TIME_DESC
init|=
literal|"Histogram for the time in millis to fetch all snapshots from HBase"
decl_stmt|;
name|String
name|TABLE_QUOTA_USAGE_NAME
init|=
literal|"tableSpaceQuotaOverview"
decl_stmt|;
name|String
name|TABLE_QUOTA_USAGE_DESC
init|=
literal|"A JSON summary of the usage of all tables with space quotas"
decl_stmt|;
name|String
name|NS_QUOTA_USAGE_NAME
init|=
literal|"namespaceSpaceQuotaOverview"
decl_stmt|;
name|String
name|NS_QUOTA_USAGE_DESC
init|=
literal|"A JSON summary of the usage of all namespaces with space quotas"
decl_stmt|;
comment|/**    * Updates the metric tracking the number of space quotas defined in the system.    *    * @param numSpaceQuotas The number of space quotas defined    */
name|void
name|updateNumSpaceQuotas
parameter_list|(
name|long
name|numSpaceQuotas
parameter_list|)
function_decl|;
comment|/**    * Updates the metric tracking the number of tables the master has computed to be in    * violation of their space quota.    *    * @param numTablesInViolation The number of tables violating a space quota    */
name|void
name|updateNumTablesInSpaceQuotaViolation
parameter_list|(
name|long
name|numTablesInViolation
parameter_list|)
function_decl|;
comment|/**    * Updates the metric tracking the number of namespaces the master has computed to be in    * violation of their space quota.    *    * @param numNamespacesInViolation The number of namespaces violating a space quota    */
name|void
name|updateNumNamespacesInSpaceQuotaViolation
parameter_list|(
name|long
name|numNamespacesInViolation
parameter_list|)
function_decl|;
comment|/**    * Updates the metric tracking the number of region size reports the master is currently    * retaining in memory.    *    * @param numCurrentRegionSizeReports The number of region size reports the master is holding in    *    memory    */
name|void
name|updateNumCurrentSpaceQuotaRegionSizeReports
parameter_list|(
name|long
name|numCurrentRegionSizeReports
parameter_list|)
function_decl|;
comment|/**    * Updates the metric tracking the amount of time taken by the {@code QuotaObserverChore}    * which runs periodically.    *    * @param time The execution time of the chore in milliseconds    */
name|void
name|incrementSpaceQuotaObserverChoreTime
parameter_list|(
name|long
name|time
parameter_list|)
function_decl|;
comment|/**    * Updates the metric tracking the amount of time taken by the {@code SnapshotQuotaObserverChore}    * which runs periodically.    */
name|void
name|incrementSnapshotObserverChoreTime
parameter_list|(
name|long
name|time
parameter_list|)
function_decl|;
comment|/**    * Updates the metric tracking the amount of time taken by the {@code SnapshotQuotaObserverChore}    * to compute the size of one snapshot, relative to the files referenced by the originating table.    */
name|void
name|incrementSnapshotObserverSnapshotComputationTime
parameter_list|(
name|long
name|time
parameter_list|)
function_decl|;
comment|/**    * Updates the metric tracking the amount of time taken by the {@code SnapshotQuotaObserverChore}    * to fetch all snapshots.    */
name|void
name|incrementSnapshotObserverSnapshotFetchTime
parameter_list|(
name|long
name|time
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

