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
name|regionserver
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

begin_comment
comment|/**  * A collection of exposed metrics for space quotas from an HBase RegionServer.  */
end_comment

begin_interface
specifier|public
interface|interface
name|MetricsRegionServerQuotaSource
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
literal|"regionserver"
decl_stmt|;
name|String
name|METRICS_DESCRIPTION
init|=
literal|"Metrics about HBase RegionServer Quotas"
decl_stmt|;
name|String
name|METRICS_JMX_CONTEXT
init|=
literal|"RegionServer,sub="
operator|+
name|METRICS_NAME
decl_stmt|;
name|String
name|NUM_TABLES_IN_VIOLATION_NAME
init|=
literal|"numTablesInViolation"
decl_stmt|;
name|String
name|NUM_SPACE_SNAPSHOTS_RECEIVED_NAME
init|=
literal|"numSpaceSnapshotsReceived"
decl_stmt|;
name|String
name|FILE_SYSTEM_UTILIZATION_CHORE_TIME
init|=
literal|"fileSystemUtilizationChoreTime"
decl_stmt|;
name|String
name|SPACE_QUOTA_REFRESHER_CHORE_TIME
init|=
literal|"spaceQuotaRefresherChoreTime"
decl_stmt|;
name|String
name|NUM_REGION_SIZE_REPORT_NAME
init|=
literal|"numRegionSizeReports"
decl_stmt|;
name|String
name|REGION_SIZE_REPORTING_CHORE_TIME_NAME
init|=
literal|"regionSizeReportingChoreTime"
decl_stmt|;
comment|/**    * Updates the metric tracking how many tables this RegionServer has marked as in violation    * of their space quota.    */
name|void
name|updateNumTablesInSpaceQuotaViolation
parameter_list|(
name|long
name|tablesInViolation
parameter_list|)
function_decl|;
comment|/**    * Updates the metric tracking how many tables this RegionServer has received    * {@code SpaceQuotaSnapshot}s for.    *    * @param numSnapshots The number of {@code SpaceQuotaSnapshot}s received from the Master.    */
name|void
name|updateNumTableSpaceQuotaSnapshots
parameter_list|(
name|long
name|numSnapshots
parameter_list|)
function_decl|;
comment|/**    * Updates the metric tracking how much time was spent scanning the filesystem to compute    * the size of each region hosted by this RegionServer.    *    * @param time The execution time of the chore in milliseconds.    */
name|void
name|incrementSpaceQuotaFileSystemScannerChoreTime
parameter_list|(
name|long
name|time
parameter_list|)
function_decl|;
comment|/**    * Updates the metric tracking how much time was spent updating the RegionServer with the    * latest information on space quotas from the {@code hbase:quota} table.    *    * @param time The execution time of the chore in milliseconds.    */
name|void
name|incrementSpaceQuotaRefresherChoreTime
parameter_list|(
name|long
name|time
parameter_list|)
function_decl|;
comment|/**    * Updates the metric tracking how many region size reports were sent from this RegionServer to    * the Master. These reports contain information on the size of each Region hosted locally.    *    * @param numReportsSent The number of region size reports sent    */
name|void
name|incrementNumRegionSizeReportsSent
parameter_list|(
name|long
name|numReportsSent
parameter_list|)
function_decl|;
comment|/**    * Updates the metric tracking how much time was spent sending region size reports to the Master    * by the RegionSizeReportingChore.    *    * @param time The execution time in milliseconds.    */
name|void
name|incrementRegionSizeReportingChoreTime
parameter_list|(
name|long
name|time
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

