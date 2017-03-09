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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|hbase
operator|.
name|classification
operator|.
name|InterfaceStability
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
name|CompatibilitySingletonFactory
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
name|Counter
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
name|Histogram
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
name|hadoop
operator|.
name|hbase
operator|.
name|procedure2
operator|.
name|ProcedureMetrics
import|;
end_import

begin_comment
comment|/**  * This class is for maintaining the various master statistics  * and publishing them through the metrics interfaces.  *<p>  * This class has a number of metrics variables that are publicly accessible;  * these variables (objects) have methods to update their values.  */
end_comment

begin_class
annotation|@
name|InterfaceStability
operator|.
name|Evolving
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsMaster
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|MetricsMaster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|MetricsMasterSource
name|masterSource
decl_stmt|;
specifier|private
name|MetricsMasterProcSource
name|masterProcSource
decl_stmt|;
specifier|private
name|MetricsMasterQuotaSource
name|masterQuotaSource
decl_stmt|;
specifier|private
name|ProcedureMetrics
name|serverCrashProcMetrics
decl_stmt|;
specifier|public
name|MetricsMaster
parameter_list|(
name|MetricsMasterWrapper
name|masterWrapper
parameter_list|)
block|{
name|masterSource
operator|=
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsMasterSourceFactory
operator|.
name|class
argument_list|)
operator|.
name|create
argument_list|(
name|masterWrapper
argument_list|)
expr_stmt|;
name|masterProcSource
operator|=
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsMasterProcSourceFactory
operator|.
name|class
argument_list|)
operator|.
name|create
argument_list|(
name|masterWrapper
argument_list|)
expr_stmt|;
name|masterQuotaSource
operator|=
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsMasterQuotaSourceFactory
operator|.
name|class
argument_list|)
operator|.
name|create
argument_list|(
name|masterWrapper
argument_list|)
expr_stmt|;
name|serverCrashProcMetrics
operator|=
name|convertToProcedureMetrics
argument_list|(
name|masterSource
operator|.
name|getServerCrashMetrics
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// for unit-test usage
specifier|public
name|MetricsMasterSource
name|getMetricsSource
parameter_list|()
block|{
return|return
name|masterSource
return|;
block|}
specifier|public
name|MetricsMasterProcSource
name|getMetricsProcSource
parameter_list|()
block|{
return|return
name|masterProcSource
return|;
block|}
specifier|public
name|MetricsMasterQuotaSource
name|getMetricsQuotaSource
parameter_list|()
block|{
return|return
name|masterQuotaSource
return|;
block|}
comment|/**    * @param inc How much to add to requests.    */
specifier|public
name|void
name|incrementRequests
parameter_list|(
specifier|final
name|long
name|inc
parameter_list|)
block|{
name|masterSource
operator|.
name|incRequests
argument_list|(
name|inc
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sets the number of space quotas defined.    *    * @see MetricsMasterQuotaSource#updateNumSpaceQuotas(long)    */
specifier|public
name|void
name|setNumSpaceQuotas
parameter_list|(
specifier|final
name|long
name|numSpaceQuotas
parameter_list|)
block|{
name|masterQuotaSource
operator|.
name|updateNumSpaceQuotas
argument_list|(
name|numSpaceQuotas
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sets the number of table in violation of a space quota.    *    * @see MetricsMasterQuotaSource#updateNumTablesInSpaceQuotaViolation(long)    */
specifier|public
name|void
name|setNumTableInSpaceQuotaViolation
parameter_list|(
specifier|final
name|long
name|numTablesInViolation
parameter_list|)
block|{
name|masterQuotaSource
operator|.
name|updateNumTablesInSpaceQuotaViolation
argument_list|(
name|numTablesInViolation
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sets the number of namespaces in violation of a space quota.    *    * @see MetricsMasterQuotaSource#updateNumNamespacesInSpaceQuotaViolation(long)    */
specifier|public
name|void
name|setNumNamespacesInSpaceQuotaViolation
parameter_list|(
specifier|final
name|long
name|numNamespacesInViolation
parameter_list|)
block|{
name|masterQuotaSource
operator|.
name|updateNumNamespacesInSpaceQuotaViolation
argument_list|(
name|numNamespacesInViolation
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sets the number of region size reports the master currently has in memory.    *    * @see MetricsMasterQuotaSource#updateNumCurrentSpaceQuotaRegionSizeReports(long)    */
specifier|public
name|void
name|setNumRegionSizeReports
parameter_list|(
specifier|final
name|long
name|numRegionReports
parameter_list|)
block|{
name|masterQuotaSource
operator|.
name|updateNumCurrentSpaceQuotaRegionSizeReports
argument_list|(
name|numRegionReports
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sets the execution time of a period of the QuotaObserverChore.    *    * @param executionTime The execution time in milliseconds.    * @see MetricsMasterQuotaSource#incrementSpaceQuotaObserverChoreTime(long)    */
specifier|public
name|void
name|incrementQuotaObserverTime
parameter_list|(
specifier|final
name|long
name|executionTime
parameter_list|)
block|{
name|masterQuotaSource
operator|.
name|incrementSpaceQuotaObserverChoreTime
argument_list|(
name|executionTime
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return Set of metrics for assign procedure    */
specifier|public
name|ProcedureMetrics
name|getServerCrashProcMetrics
parameter_list|()
block|{
return|return
name|serverCrashProcMetrics
return|;
block|}
comment|/**    * This is utility function that converts {@link OperationMetrics} to {@link ProcedureMetrics}.    *    * NOTE: Procedure framework in hbase-procedure module accesses metrics common to most procedures    * through {@link ProcedureMetrics} interface. Metrics source classes in hbase-hadoop-compat    * module provides similar interface {@link OperationMetrics} that contains metrics common to    * most operations. As both hbase-procedure and hbase-hadoop-compat are lower level modules used    * by hbase-server (this) module and there is no dependency between them, this method does the    * required conversion.    */
specifier|public
specifier|static
name|ProcedureMetrics
name|convertToProcedureMetrics
parameter_list|(
specifier|final
name|OperationMetrics
name|metrics
parameter_list|)
block|{
return|return
operator|new
name|ProcedureMetrics
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Counter
name|getSubmittedCounter
parameter_list|()
block|{
return|return
name|metrics
operator|.
name|getSubmittedCounter
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Histogram
name|getTimeHisto
parameter_list|()
block|{
return|return
name|metrics
operator|.
name|getTimeHisto
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Counter
name|getFailedCounter
parameter_list|()
block|{
return|return
name|metrics
operator|.
name|getFailedCounter
argument_list|()
return|;
block|}
block|}
return|;
block|}
comment|/**    * Sets the execution time of a period of the {@code SnapshotQuotaObserverChore}.    */
specifier|public
name|void
name|incrementSnapshotObserverTime
parameter_list|(
specifier|final
name|long
name|executionTime
parameter_list|)
block|{
name|masterQuotaSource
operator|.
name|incrementSnapshotObserverChoreTime
argument_list|(
name|executionTime
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sets the execution time to compute the size of a single snapshot.    */
specifier|public
name|void
name|incrementSnapshotSizeComputationTime
parameter_list|(
specifier|final
name|long
name|executionTime
parameter_list|)
block|{
name|masterQuotaSource
operator|.
name|incrementSnapshotObserverSnapshotComputationTime
argument_list|(
name|executionTime
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sets the execution time to fetch the mapping of snapshots to originating table.    */
specifier|public
name|void
name|incrementSnapshotFetchTime
parameter_list|(
name|long
name|executionTime
parameter_list|)
block|{
name|masterQuotaSource
operator|.
name|incrementSnapshotObserverSnapshotFetchTime
argument_list|(
name|executionTime
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

