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
name|lang
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
name|util
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
name|util
operator|.
name|FastLongHistogram
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
comment|// Double buffer the two FastLongHistograms.
comment|// As they are reset they learn how the buckets should be spaced
comment|// So keep two around and use them
specifier|protected
specifier|final
name|FastLongHistogram
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
specifier|protected
specifier|final
name|Counter
name|counter
init|=
operator|new
name|Counter
argument_list|(
literal|0
argument_list|)
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
argument_list|(
name|name
argument_list|,
name|description
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
operator|<<
literal|2
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|MutableHistogram
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|description
parameter_list|,
name|long
name|maxExpected
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
name|FastLongHistogram
argument_list|(
name|FastLongHistogram
operator|.
name|DEFAULT_NBINS
argument_list|,
literal|1
argument_list|,
name|maxExpected
argument_list|)
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
name|counter
operator|.
name|increment
argument_list|()
expr_stmt|;
name|histogram
operator|.
name|add
argument_list|(
name|val
argument_list|,
literal|1
argument_list|)
expr_stmt|;
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
comment|// Get a reference to the old histogram.
name|FastLongHistogram
name|histo
init|=
name|histogram
operator|.
name|reset
argument_list|()
decl_stmt|;
if|if
condition|(
name|histo
operator|!=
literal|null
condition|)
block|{
name|updateSnapshotMetrics
argument_list|(
name|metricsRecordBuilder
argument_list|,
name|histo
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|updateSnapshotMetrics
parameter_list|(
name|MetricsRecordBuilder
name|metricsRecordBuilder
parameter_list|,
name|FastLongHistogram
name|histo
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
name|counter
operator|.
name|get
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
name|histo
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
name|histo
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
name|histo
operator|.
name|getMean
argument_list|()
argument_list|)
expr_stmt|;
name|long
index|[]
name|percentiles
init|=
name|histo
operator|.
name|getQuantiles
argument_list|()
decl_stmt|;
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
name|percentiles
index|[
literal|0
index|]
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
name|percentiles
index|[
literal|1
index|]
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
name|percentiles
index|[
literal|2
index|]
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
name|percentiles
index|[
literal|3
index|]
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
name|percentiles
index|[
literal|4
index|]
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
name|percentiles
index|[
literal|5
index|]
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
name|percentiles
index|[
literal|6
index|]
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
name|percentiles
index|[
literal|7
index|]
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

