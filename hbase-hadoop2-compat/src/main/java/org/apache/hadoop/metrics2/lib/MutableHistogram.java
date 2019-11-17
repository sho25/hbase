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
name|metrics2
operator|.
name|lib
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
name|lang3
operator|.
name|StringUtils
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
name|hbase
operator|.
name|metrics
operator|.
name|Snapshot
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
name|impl
operator|.
name|HistogramImpl
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
name|MetricsInfo
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * A histogram implementation that runs in constant space, and exports to hadoop2's metrics2 system.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MutableHistogram
extends|extends
name|MutableMetric
implements|implements
name|MetricHistogram
block|{
specifier|protected
name|HistogramImpl
name|histogram
decl_stmt|;
specifier|protected
specifier|final
name|String
name|name
decl_stmt|;
specifier|protected
specifier|final
name|String
name|desc
decl_stmt|;
specifier|public
name|MutableHistogram
parameter_list|(
name|MetricsInfo
name|info
parameter_list|)
block|{
name|this
argument_list|(
name|info
operator|.
name|name
argument_list|()
argument_list|,
name|info
operator|.
name|description
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MutableHistogram
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|description
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|StringUtils
operator|.
name|capitalize
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|desc
operator|=
name|StringUtils
operator|.
name|uncapitalize
argument_list|(
name|description
argument_list|)
expr_stmt|;
name|this
operator|.
name|histogram
operator|=
operator|new
name|HistogramImpl
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|add
parameter_list|(
specifier|final
name|long
name|val
parameter_list|)
block|{
name|histogram
operator|.
name|update
argument_list|(
name|val
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCount
parameter_list|()
block|{
return|return
name|histogram
operator|.
name|getCount
argument_list|()
return|;
block|}
specifier|public
name|long
name|getMax
parameter_list|()
block|{
return|return
name|histogram
operator|.
name|getMax
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|snapshot
parameter_list|(
name|MetricsRecordBuilder
name|metricsRecordBuilder
parameter_list|,
name|boolean
name|all
parameter_list|)
block|{
name|snapshot
argument_list|(
name|name
argument_list|,
name|desc
argument_list|,
name|histogram
argument_list|,
name|metricsRecordBuilder
argument_list|,
name|all
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|snapshot
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|desc
parameter_list|,
name|Histogram
name|histogram
parameter_list|,
name|MetricsRecordBuilder
name|metricsRecordBuilder
parameter_list|,
name|boolean
name|all
parameter_list|)
block|{
comment|// Get a reference to the old histogram.
name|Snapshot
name|snapshot
init|=
name|histogram
operator|.
name|snapshot
argument_list|()
decl_stmt|;
if|if
condition|(
name|snapshot
operator|!=
literal|null
condition|)
block|{
name|updateSnapshotMetrics
argument_list|(
name|name
argument_list|,
name|desc
argument_list|,
name|histogram
argument_list|,
name|snapshot
argument_list|,
name|metricsRecordBuilder
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
specifier|static
name|void
name|updateSnapshotMetrics
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|desc
parameter_list|,
name|Histogram
name|histogram
parameter_list|,
name|Snapshot
name|snapshot
parameter_list|,
name|MetricsRecordBuilder
name|metricsRecordBuilder
parameter_list|)
block|{
name|metricsRecordBuilder
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|name
operator|+
name|NUM_OPS_METRIC_NAME
argument_list|,
name|desc
argument_list|)
argument_list|,
name|histogram
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|metricsRecordBuilder
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|name
operator|+
name|MIN_METRIC_NAME
argument_list|,
name|desc
argument_list|)
argument_list|,
name|snapshot
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|metricsRecordBuilder
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|name
operator|+
name|MAX_METRIC_NAME
argument_list|,
name|desc
argument_list|)
argument_list|,
name|snapshot
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
name|metricsRecordBuilder
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|name
operator|+
name|MEAN_METRIC_NAME
argument_list|,
name|desc
argument_list|)
argument_list|,
name|snapshot
operator|.
name|getMean
argument_list|()
argument_list|)
expr_stmt|;
name|metricsRecordBuilder
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|name
operator|+
name|TWENTY_FIFTH_PERCENTILE_METRIC_NAME
argument_list|,
name|desc
argument_list|)
argument_list|,
name|snapshot
operator|.
name|get25thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|metricsRecordBuilder
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|name
operator|+
name|MEDIAN_METRIC_NAME
argument_list|,
name|desc
argument_list|)
argument_list|,
name|snapshot
operator|.
name|getMedian
argument_list|()
argument_list|)
expr_stmt|;
name|metricsRecordBuilder
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|name
operator|+
name|SEVENTY_FIFTH_PERCENTILE_METRIC_NAME
argument_list|,
name|desc
argument_list|)
argument_list|,
name|snapshot
operator|.
name|get75thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|metricsRecordBuilder
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|name
operator|+
name|NINETIETH_PERCENTILE_METRIC_NAME
argument_list|,
name|desc
argument_list|)
argument_list|,
name|snapshot
operator|.
name|get90thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|metricsRecordBuilder
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|name
operator|+
name|NINETY_FIFTH_PERCENTILE_METRIC_NAME
argument_list|,
name|desc
argument_list|)
argument_list|,
name|snapshot
operator|.
name|get95thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|metricsRecordBuilder
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|name
operator|+
name|NINETY_EIGHTH_PERCENTILE_METRIC_NAME
argument_list|,
name|desc
argument_list|)
argument_list|,
name|snapshot
operator|.
name|get98thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|metricsRecordBuilder
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|name
operator|+
name|NINETY_NINETH_PERCENTILE_METRIC_NAME
argument_list|,
name|desc
argument_list|)
argument_list|,
name|snapshot
operator|.
name|get99thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|metricsRecordBuilder
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|name
operator|+
name|NINETY_NINE_POINT_NINETH_PERCENTILE_METRIC_NAME
argument_list|,
name|desc
argument_list|)
argument_list|,
name|snapshot
operator|.
name|get999thPercentile
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

