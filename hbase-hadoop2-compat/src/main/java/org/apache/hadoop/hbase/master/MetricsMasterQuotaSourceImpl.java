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
name|yetus
operator|.
name|audience
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
name|metrics
operator|.
name|BaseSourceImpl
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
name|metrics2
operator|.
name|MetricHistogram
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
name|metrics2
operator|.
name|MetricsCollector
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
name|metrics2
operator|.
name|MetricsRecordBuilder
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
name|metrics2
operator|.
name|lib
operator|.
name|Interns
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
name|metrics2
operator|.
name|lib
operator|.
name|MutableGaugeLong
import|;
end_import

begin_comment
comment|/**  * Implementation of {@link MetricsMasterQuotaSource} which writes the values passed in via the  * interface to the metrics backend.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsMasterQuotaSourceImpl
extends|extends
name|BaseSourceImpl
implements|implements
name|MetricsMasterQuotaSource
block|{
specifier|private
specifier|final
name|MetricsMasterWrapper
name|wrapper
decl_stmt|;
specifier|private
specifier|final
name|MutableGaugeLong
name|spaceQuotasGauge
decl_stmt|;
specifier|private
specifier|final
name|MutableGaugeLong
name|tablesViolatingQuotasGauge
decl_stmt|;
specifier|private
specifier|final
name|MutableGaugeLong
name|namespacesViolatingQuotasGauge
decl_stmt|;
specifier|private
specifier|final
name|MutableGaugeLong
name|regionSpaceReportsGauge
decl_stmt|;
specifier|private
specifier|final
name|MetricHistogram
name|quotaObserverTimeHisto
decl_stmt|;
specifier|private
specifier|final
name|MetricHistogram
name|snapshotObserverTimeHisto
decl_stmt|;
specifier|private
specifier|final
name|MetricHistogram
name|snapshotObserverSizeComputationTimeHisto
decl_stmt|;
specifier|private
specifier|final
name|MetricHistogram
name|snapshotObserverSnapshotFetchTimeHisto
decl_stmt|;
specifier|public
name|MetricsMasterQuotaSourceImpl
parameter_list|(
name|MetricsMasterWrapper
name|wrapper
parameter_list|)
block|{
name|this
argument_list|(
name|METRICS_NAME
argument_list|,
name|METRICS_DESCRIPTION
argument_list|,
name|METRICS_CONTEXT
argument_list|,
name|METRICS_JMX_CONTEXT
argument_list|,
name|wrapper
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MetricsMasterQuotaSourceImpl
parameter_list|(
name|String
name|metricsName
parameter_list|,
name|String
name|metricsDescription
parameter_list|,
name|String
name|metricsContext
parameter_list|,
name|String
name|metricsJmxContext
parameter_list|,
name|MetricsMasterWrapper
name|wrapper
parameter_list|)
block|{
name|super
argument_list|(
name|metricsName
argument_list|,
name|metricsDescription
argument_list|,
name|metricsContext
argument_list|,
name|metricsJmxContext
argument_list|)
expr_stmt|;
name|this
operator|.
name|wrapper
operator|=
name|wrapper
expr_stmt|;
name|spaceQuotasGauge
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newGauge
argument_list|(
name|NUM_SPACE_QUOTAS_NAME
argument_list|,
name|NUM_SPACE_QUOTAS_DESC
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|tablesViolatingQuotasGauge
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newGauge
argument_list|(
name|NUM_TABLES_QUOTA_VIOLATIONS_NAME
argument_list|,
name|NUM_TABLES_QUOTA_VIOLATIONS_DESC
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|namespacesViolatingQuotasGauge
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newGauge
argument_list|(
name|NUM_NS_QUOTA_VIOLATIONS_NAME
argument_list|,
name|NUM_NS_QUOTA_VIOLATIONS_DESC
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|regionSpaceReportsGauge
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newGauge
argument_list|(
name|NUM_REGION_SIZE_REPORTS_NAME
argument_list|,
name|NUM_REGION_SIZE_REPORTS_DESC
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|quotaObserverTimeHisto
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newTimeHistogram
argument_list|(
name|QUOTA_OBSERVER_CHORE_TIME_NAME
argument_list|,
name|QUOTA_OBSERVER_CHORE_TIME_DESC
argument_list|)
expr_stmt|;
name|snapshotObserverTimeHisto
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newTimeHistogram
argument_list|(
name|SNAPSHOT_OBSERVER_CHORE_TIME_NAME
argument_list|,
name|SNAPSHOT_OBSERVER_CHORE_TIME_DESC
argument_list|)
expr_stmt|;
name|snapshotObserverSizeComputationTimeHisto
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newTimeHistogram
argument_list|(
name|SNAPSHOT_OBSERVER_SIZE_COMPUTATION_TIME_NAME
argument_list|,
name|SNAPSHOT_OBSERVER_SIZE_COMPUTATION_TIME_DESC
argument_list|)
expr_stmt|;
name|snapshotObserverSnapshotFetchTimeHisto
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newTimeHistogram
argument_list|(
name|SNAPSHOT_OBSERVER_FETCH_TIME_NAME
argument_list|,
name|SNAPSHOT_OBSERVER_FETCH_TIME_DESC
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateNumSpaceQuotas
parameter_list|(
name|long
name|numSpaceQuotas
parameter_list|)
block|{
name|spaceQuotasGauge
operator|.
name|set
argument_list|(
name|numSpaceQuotas
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateNumTablesInSpaceQuotaViolation
parameter_list|(
name|long
name|numTablesInViolation
parameter_list|)
block|{
name|tablesViolatingQuotasGauge
operator|.
name|set
argument_list|(
name|numTablesInViolation
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateNumNamespacesInSpaceQuotaViolation
parameter_list|(
name|long
name|numNamespacesInViolation
parameter_list|)
block|{
name|namespacesViolatingQuotasGauge
operator|.
name|set
argument_list|(
name|numNamespacesInViolation
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateNumCurrentSpaceQuotaRegionSizeReports
parameter_list|(
name|long
name|numCurrentRegionSizeReports
parameter_list|)
block|{
name|regionSpaceReportsGauge
operator|.
name|set
argument_list|(
name|numCurrentRegionSizeReports
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrementSpaceQuotaObserverChoreTime
parameter_list|(
name|long
name|time
parameter_list|)
block|{
name|quotaObserverTimeHisto
operator|.
name|add
argument_list|(
name|time
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrementSnapshotObserverChoreTime
parameter_list|(
name|long
name|time
parameter_list|)
block|{
name|snapshotObserverTimeHisto
operator|.
name|add
argument_list|(
name|time
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|getMetrics
parameter_list|(
name|MetricsCollector
name|metricsCollector
parameter_list|,
name|boolean
name|all
parameter_list|)
block|{
name|MetricsRecordBuilder
name|record
init|=
name|metricsCollector
operator|.
name|addRecord
argument_list|(
name|metricsRegistry
operator|.
name|info
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|wrapper
operator|!=
literal|null
condition|)
block|{
comment|// Summarize the tables
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
name|tableUsages
init|=
name|wrapper
operator|.
name|getTableSpaceUtilization
argument_list|()
decl_stmt|;
name|String
name|tableSummary
init|=
literal|"[]"
decl_stmt|;
if|if
condition|(
name|tableUsages
operator|!=
literal|null
operator|&&
operator|!
name|tableUsages
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|tableSummary
operator|=
name|generateJsonQuotaSummary
argument_list|(
name|tableUsages
operator|.
name|entrySet
argument_list|()
argument_list|,
literal|"table"
argument_list|)
expr_stmt|;
block|}
name|record
operator|.
name|tag
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|TABLE_QUOTA_USAGE_NAME
argument_list|,
name|TABLE_QUOTA_USAGE_DESC
argument_list|)
argument_list|,
name|tableSummary
argument_list|)
expr_stmt|;
comment|// Summarize the namespaces
name|String
name|nsSummary
init|=
literal|"[]"
decl_stmt|;
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
name|namespaceUsages
init|=
name|wrapper
operator|.
name|getNamespaceSpaceUtilization
argument_list|()
decl_stmt|;
if|if
condition|(
name|namespaceUsages
operator|!=
literal|null
operator|&&
operator|!
name|namespaceUsages
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|nsSummary
operator|=
name|generateJsonQuotaSummary
argument_list|(
name|namespaceUsages
operator|.
name|entrySet
argument_list|()
argument_list|,
literal|"namespace"
argument_list|)
expr_stmt|;
block|}
name|record
operator|.
name|tag
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|NS_QUOTA_USAGE_NAME
argument_list|,
name|NS_QUOTA_USAGE_DESC
argument_list|)
argument_list|,
name|nsSummary
argument_list|)
expr_stmt|;
block|}
name|metricsRegistry
operator|.
name|snapshot
argument_list|(
name|record
argument_list|,
name|all
argument_list|)
expr_stmt|;
block|}
comment|/**    * Summarizes the usage and limit for many targets (table or namespace) into JSON.    */
specifier|private
name|String
name|generateJsonQuotaSummary
parameter_list|(
name|Iterable
argument_list|<
name|Entry
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
argument_list|>
name|data
parameter_list|,
name|String
name|target
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
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
name|tableUsage
range|:
name|data
control|)
block|{
name|String
name|tableName
init|=
name|tableUsage
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|long
name|usage
init|=
name|tableUsage
operator|.
name|getValue
argument_list|()
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|long
name|limit
init|=
name|tableUsage
operator|.
name|getValue
argument_list|()
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|sb
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"{"
argument_list|)
operator|.
name|append
argument_list|(
name|target
argument_list|)
operator|.
name|append
argument_list|(
literal|"="
argument_list|)
operator|.
name|append
argument_list|(
name|tableName
argument_list|)
operator|.
name|append
argument_list|(
literal|", usage="
argument_list|)
operator|.
name|append
argument_list|(
name|usage
argument_list|)
operator|.
name|append
argument_list|(
literal|", limit="
argument_list|)
operator|.
name|append
argument_list|(
name|limit
argument_list|)
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|insert
argument_list|(
literal|0
argument_list|,
literal|"["
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrementSnapshotObserverSnapshotComputationTime
parameter_list|(
name|long
name|time
parameter_list|)
block|{
name|snapshotObserverSizeComputationTimeHisto
operator|.
name|add
argument_list|(
name|time
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrementSnapshotObserverSnapshotFetchTime
parameter_list|(
name|long
name|time
parameter_list|)
block|{
name|snapshotObserverSnapshotFetchTimeHisto
operator|.
name|add
argument_list|(
name|time
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

