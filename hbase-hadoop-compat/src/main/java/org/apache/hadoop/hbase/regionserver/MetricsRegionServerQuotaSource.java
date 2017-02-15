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
comment|/**  * A collection of exposed metrics for HBase quotas from an HBase RegionServer.  */
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
comment|/**    * Updates the metric tracking how many tables this RegionServer has marked as in violation    * of their space quota.    */
name|void
name|updateNumTablesInSpaceQuotaViolation
parameter_list|(
name|long
name|tablesInViolation
parameter_list|)
function_decl|;
comment|/**    * Updates the metric tracking how many tables this RegionServer has received    * {@code SpaceQuotaSnapshot}s for.    */
name|void
name|updateNumTableSpaceQuotaSnapshots
parameter_list|(
name|long
name|numSnapshots
parameter_list|)
function_decl|;
comment|/**    * Updates the metric tracking how much time was spent scanning the filesystem to compute    * the size of each region hosted by this RegionServer.    */
name|void
name|incrementSpaceQuotaFileSystemScannerChoreTime
parameter_list|(
name|long
name|time
parameter_list|)
function_decl|;
comment|/**    * Updates the metric tracking how much time was spent updating the RegionServer with the    * lastest information on space quotas from the {@code hbase:quota} table.    */
name|void
name|incrementSpaceQuotaRefresherChoreTime
parameter_list|(
name|long
name|time
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

