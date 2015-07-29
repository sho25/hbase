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
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentMap
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
name|metrics2
operator|.
name|MetricsException
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
name|hadoop
operator|.
name|metrics2
operator|.
name|MetricsTag
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
name|impl
operator|.
name|MsInfo
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Objects
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
import|;
end_import

begin_comment
comment|/**  * An optional metrics registry class for creating and maintaining a  * collection of MetricsMutables, making writing metrics source easier.  * NOTE: this is a copy of org.apache.hadoop.metrics2.lib.MetricsRegistry with added one  *       feature: metrics can be removed. When HADOOP-8313 is fixed, usages of this class  *       should be substituted with org.apache.hadoop.metrics2.lib.MetricsRegistry.  *       This implementation also provides handy methods for creating metrics  *       dynamically.  *       Another difference is that metricsMap implementation is substituted with  *       thread-safe map, as we allow dynamic metrics additions/removals.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DynamicMetricsRegistry
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
name|DynamicMetricsRegistry
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|MutableMetric
argument_list|>
name|metricsMap
init|=
name|Maps
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|MetricsTag
argument_list|>
name|tagsMap
init|=
name|Maps
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|MetricsInfo
name|metricsInfo
decl_stmt|;
specifier|private
specifier|final
name|DefaultMetricsSystemHelper
name|helper
init|=
operator|new
name|DefaultMetricsSystemHelper
argument_list|()
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
index|[]
name|histogramSuffixes
init|=
operator|new
name|String
index|[]
block|{
literal|"_num_ops"
block|,
literal|"_min"
block|,
literal|"_max"
block|,
literal|"_median"
block|,
literal|"_75th_percentile"
block|,
literal|"_95th_percentile"
block|,
literal|"_99th_percentile"
block|}
decl_stmt|;
comment|/**    * Construct the registry with a record name    * @param name  of the record of the metrics    */
specifier|public
name|DynamicMetricsRegistry
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|name
argument_list|,
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Construct the registry with a metadata object    * @param info  the info object for the metrics record/group    */
specifier|public
name|DynamicMetricsRegistry
parameter_list|(
name|MetricsInfo
name|info
parameter_list|)
block|{
name|metricsInfo
operator|=
name|info
expr_stmt|;
block|}
comment|/**    * @return the info object of the metrics registry    */
specifier|public
name|MetricsInfo
name|info
parameter_list|()
block|{
return|return
name|metricsInfo
return|;
block|}
comment|/**    * Get a metric by name    * @param name  of the metric    * @return the metric object    */
specifier|public
name|MutableMetric
name|get
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|metricsMap
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
comment|/**    * Get a tag by name    * @param name  of the tag    * @return the tag object    */
specifier|public
name|MetricsTag
name|getTag
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|tagsMap
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
comment|/**    * Create a mutable integer counter    * @param name  of the metric    * @param desc  metric description    * @param iVal  initial value    * @return a new counter object    */
specifier|public
name|MutableCounterInt
name|newCounter
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|desc
parameter_list|,
name|int
name|iVal
parameter_list|)
block|{
return|return
name|newCounter
argument_list|(
operator|new
name|MetricsInfoImpl
argument_list|(
name|name
argument_list|,
name|desc
argument_list|)
argument_list|,
name|iVal
argument_list|)
return|;
block|}
comment|/**    * Create a mutable integer counter    * @param info  metadata of the metric    * @param iVal  initial value    * @return a new counter object    */
specifier|public
name|MutableCounterInt
name|newCounter
parameter_list|(
name|MetricsInfo
name|info
parameter_list|,
name|int
name|iVal
parameter_list|)
block|{
name|MutableCounterInt
name|ret
init|=
operator|new
name|MutableCounterInt
argument_list|(
name|info
argument_list|,
name|iVal
argument_list|)
decl_stmt|;
return|return
name|addNewMetricIfAbsent
argument_list|(
name|info
operator|.
name|name
argument_list|()
argument_list|,
name|ret
argument_list|,
name|MutableCounterInt
operator|.
name|class
argument_list|)
return|;
block|}
comment|/**    * Create a mutable long integer counter    * @param name  of the metric    * @param desc  metric description    * @param iVal  initial value    * @return a new counter object    */
specifier|public
name|MutableCounterLong
name|newCounter
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|desc
parameter_list|,
name|long
name|iVal
parameter_list|)
block|{
return|return
name|newCounter
argument_list|(
operator|new
name|MetricsInfoImpl
argument_list|(
name|name
argument_list|,
name|desc
argument_list|)
argument_list|,
name|iVal
argument_list|)
return|;
block|}
comment|/**    * Create a mutable long integer counter    * @param info  metadata of the metric    * @param iVal  initial value    * @return a new counter object    */
specifier|public
name|MutableCounterLong
name|newCounter
parameter_list|(
name|MetricsInfo
name|info
parameter_list|,
name|long
name|iVal
parameter_list|)
block|{
name|MutableCounterLong
name|ret
init|=
operator|new
name|MutableCounterLong
argument_list|(
name|info
argument_list|,
name|iVal
argument_list|)
decl_stmt|;
return|return
name|addNewMetricIfAbsent
argument_list|(
name|info
operator|.
name|name
argument_list|()
argument_list|,
name|ret
argument_list|,
name|MutableCounterLong
operator|.
name|class
argument_list|)
return|;
block|}
comment|/**    * Create a mutable integer gauge    * @param name  of the metric    * @param desc  metric description    * @param iVal  initial value    * @return a new gauge object    */
specifier|public
name|MutableGaugeInt
name|newGauge
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|desc
parameter_list|,
name|int
name|iVal
parameter_list|)
block|{
return|return
name|newGauge
argument_list|(
operator|new
name|MetricsInfoImpl
argument_list|(
name|name
argument_list|,
name|desc
argument_list|)
argument_list|,
name|iVal
argument_list|)
return|;
block|}
comment|/**    * Create a mutable integer gauge    * @param info  metadata of the metric    * @param iVal  initial value    * @return a new gauge object    */
specifier|public
name|MutableGaugeInt
name|newGauge
parameter_list|(
name|MetricsInfo
name|info
parameter_list|,
name|int
name|iVal
parameter_list|)
block|{
name|MutableGaugeInt
name|ret
init|=
operator|new
name|MutableGaugeInt
argument_list|(
name|info
argument_list|,
name|iVal
argument_list|)
decl_stmt|;
return|return
name|addNewMetricIfAbsent
argument_list|(
name|info
operator|.
name|name
argument_list|()
argument_list|,
name|ret
argument_list|,
name|MutableGaugeInt
operator|.
name|class
argument_list|)
return|;
block|}
comment|/**    * Create a mutable long integer gauge    * @param name  of the metric    * @param desc  metric description    * @param iVal  initial value    * @return a new gauge object    */
specifier|public
name|MutableGaugeLong
name|newGauge
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|desc
parameter_list|,
name|long
name|iVal
parameter_list|)
block|{
return|return
name|newGauge
argument_list|(
operator|new
name|MetricsInfoImpl
argument_list|(
name|name
argument_list|,
name|desc
argument_list|)
argument_list|,
name|iVal
argument_list|)
return|;
block|}
comment|/**    * Create a mutable long integer gauge    * @param info  metadata of the metric    * @param iVal  initial value    * @return a new gauge object    */
specifier|public
name|MutableGaugeLong
name|newGauge
parameter_list|(
name|MetricsInfo
name|info
parameter_list|,
name|long
name|iVal
parameter_list|)
block|{
name|MutableGaugeLong
name|ret
init|=
operator|new
name|MutableGaugeLong
argument_list|(
name|info
argument_list|,
name|iVal
argument_list|)
decl_stmt|;
return|return
name|addNewMetricIfAbsent
argument_list|(
name|info
operator|.
name|name
argument_list|()
argument_list|,
name|ret
argument_list|,
name|MutableGaugeLong
operator|.
name|class
argument_list|)
return|;
block|}
comment|/**    * Create a mutable metric with stats    * @param name  of the metric    * @param desc  metric description    * @param sampleName  of the metric (e.g., "Ops")    * @param valueName   of the metric (e.g., "Time" or "Latency")    * @param extended    produce extended stat (stdev, min/max etc.) if true.    * @return a new mutable stat metric object    */
specifier|public
name|MutableStat
name|newStat
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|desc
parameter_list|,
name|String
name|sampleName
parameter_list|,
name|String
name|valueName
parameter_list|,
name|boolean
name|extended
parameter_list|)
block|{
name|MutableStat
name|ret
init|=
operator|new
name|MutableStat
argument_list|(
name|name
argument_list|,
name|desc
argument_list|,
name|sampleName
argument_list|,
name|valueName
argument_list|,
name|extended
argument_list|)
decl_stmt|;
return|return
name|addNewMetricIfAbsent
argument_list|(
name|name
argument_list|,
name|ret
argument_list|,
name|MutableStat
operator|.
name|class
argument_list|)
return|;
block|}
comment|/**    * Create a mutable metric with stats    * @param name  of the metric    * @param desc  metric description    * @param sampleName  of the metric (e.g., "Ops")    * @param valueName   of the metric (e.g., "Time" or "Latency")    * @return a new mutable metric object    */
specifier|public
name|MutableStat
name|newStat
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|desc
parameter_list|,
name|String
name|sampleName
parameter_list|,
name|String
name|valueName
parameter_list|)
block|{
return|return
name|newStat
argument_list|(
name|name
argument_list|,
name|desc
argument_list|,
name|sampleName
argument_list|,
name|valueName
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Create a mutable rate metric    * @param name  of the metric    * @return a new mutable metric object    */
specifier|public
name|MutableRate
name|newRate
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|newRate
argument_list|(
name|name
argument_list|,
name|name
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Create a mutable rate metric    * @param name  of the metric    * @param description of the metric    * @return a new mutable rate metric object    */
specifier|public
name|MutableRate
name|newRate
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|description
parameter_list|)
block|{
return|return
name|newRate
argument_list|(
name|name
argument_list|,
name|description
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Create a mutable rate metric (for throughput measurement)    * @param name  of the metric    * @param desc  description    * @param extended  produce extended stat (stdev/min/max etc.) if true    * @return a new mutable rate metric object    */
specifier|public
name|MutableRate
name|newRate
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|desc
parameter_list|,
name|boolean
name|extended
parameter_list|)
block|{
return|return
name|newRate
argument_list|(
name|name
argument_list|,
name|desc
argument_list|,
name|extended
argument_list|,
literal|true
argument_list|)
return|;
block|}
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|MutableRate
name|newRate
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|desc
parameter_list|,
name|boolean
name|extended
parameter_list|,
name|boolean
name|returnExisting
parameter_list|)
block|{
if|if
condition|(
name|returnExisting
condition|)
block|{
name|MutableMetric
name|rate
init|=
name|metricsMap
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|rate
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|rate
operator|instanceof
name|MutableRate
condition|)
return|return
operator|(
name|MutableRate
operator|)
name|rate
return|;
throw|throw
operator|new
name|MetricsException
argument_list|(
literal|"Unexpected metrics type "
operator|+
name|rate
operator|.
name|getClass
argument_list|()
operator|+
literal|" for "
operator|+
name|name
argument_list|)
throw|;
block|}
block|}
name|MutableRate
name|ret
init|=
operator|new
name|MutableRate
argument_list|(
name|name
argument_list|,
name|desc
argument_list|,
name|extended
argument_list|)
decl_stmt|;
return|return
name|addNewMetricIfAbsent
argument_list|(
name|name
argument_list|,
name|ret
argument_list|,
name|MutableRate
operator|.
name|class
argument_list|)
return|;
block|}
comment|/**    * Create a new histogram.    * @param name Name of the histogram.    * @return A new MutableHistogram    */
specifier|public
name|MutableHistogram
name|newHistogram
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|newHistogram
argument_list|(
name|name
argument_list|,
literal|""
argument_list|)
return|;
block|}
comment|/**    * Create a new histogram.    * @param name The name of the histogram    * @param desc The description of the data in the histogram.    * @return A new MutableHistogram    */
specifier|public
name|MutableHistogram
name|newHistogram
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|desc
parameter_list|)
block|{
name|MutableHistogram
name|histo
init|=
operator|new
name|MutableHistogram
argument_list|(
name|name
argument_list|,
name|desc
argument_list|)
decl_stmt|;
return|return
name|addNewMetricIfAbsent
argument_list|(
name|name
argument_list|,
name|histo
argument_list|,
name|MutableHistogram
operator|.
name|class
argument_list|)
return|;
block|}
comment|/**    * Create a new MutableQuantile(A more accurate histogram).    * @param name The name of the histogram    * @return a new MutableQuantile    */
specifier|public
name|MetricMutableQuantiles
name|newQuantile
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|newQuantile
argument_list|(
name|name
argument_list|,
literal|""
argument_list|)
return|;
block|}
specifier|public
name|MetricMutableQuantiles
name|newQuantile
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|desc
parameter_list|)
block|{
name|MetricMutableQuantiles
name|histo
init|=
operator|new
name|MetricMutableQuantiles
argument_list|(
name|name
argument_list|,
name|desc
argument_list|,
literal|"Ops"
argument_list|,
literal|""
argument_list|,
literal|60
argument_list|)
decl_stmt|;
return|return
name|addNewMetricIfAbsent
argument_list|(
name|name
argument_list|,
name|histo
argument_list|,
name|MetricMutableQuantiles
operator|.
name|class
argument_list|)
return|;
block|}
specifier|synchronized
name|void
name|add
parameter_list|(
name|String
name|name
parameter_list|,
name|MutableMetric
name|metric
parameter_list|)
block|{
name|addNewMetricIfAbsent
argument_list|(
name|name
argument_list|,
name|metric
argument_list|,
name|MutableMetric
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add sample to a stat metric by name.    * @param name  of the metric    * @param value of the snapshot to add    */
specifier|public
name|void
name|add
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|value
parameter_list|)
block|{
name|MutableMetric
name|m
init|=
name|metricsMap
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|m
operator|instanceof
name|MutableStat
condition|)
block|{
operator|(
operator|(
name|MutableStat
operator|)
name|m
operator|)
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|MetricsException
argument_list|(
literal|"Unsupported add(value) for metric "
operator|+
name|name
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|metricsMap
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|newRate
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
comment|// default is a rate metric
name|add
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Set the metrics context tag    * @param name of the context    * @return the registry itself as a convenience    */
specifier|public
name|DynamicMetricsRegistry
name|setContext
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|tag
argument_list|(
name|MsInfo
operator|.
name|Context
argument_list|,
name|name
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**    * Add a tag to the metrics    * @param name  of the tag    * @param description of the tag    * @param value of the tag    * @return the registry (for keep adding tags)    */
specifier|public
name|DynamicMetricsRegistry
name|tag
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|description
parameter_list|,
name|String
name|value
parameter_list|)
block|{
return|return
name|tag
argument_list|(
name|name
argument_list|,
name|description
argument_list|,
name|value
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Add a tag to the metrics    * @param name  of the tag    * @param description of the tag    * @param value of the tag    * @param override  existing tag if true    * @return the registry (for keep adding tags)    */
specifier|public
name|DynamicMetricsRegistry
name|tag
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|description
parameter_list|,
name|String
name|value
parameter_list|,
name|boolean
name|override
parameter_list|)
block|{
return|return
name|tag
argument_list|(
operator|new
name|MetricsInfoImpl
argument_list|(
name|name
argument_list|,
name|description
argument_list|)
argument_list|,
name|value
argument_list|,
name|override
argument_list|)
return|;
block|}
comment|/**    * Add a tag to the metrics    * @param info  metadata of the tag    * @param value of the tag    * @param override existing tag if true    * @return the registry (for keep adding tags etc.)    */
specifier|public
name|DynamicMetricsRegistry
name|tag
parameter_list|(
name|MetricsInfo
name|info
parameter_list|,
name|String
name|value
parameter_list|,
name|boolean
name|override
parameter_list|)
block|{
name|MetricsTag
name|tag
init|=
name|Interns
operator|.
name|tag
argument_list|(
name|info
argument_list|,
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|override
condition|)
block|{
name|MetricsTag
name|existing
init|=
name|tagsMap
operator|.
name|putIfAbsent
argument_list|(
name|info
operator|.
name|name
argument_list|()
argument_list|,
name|tag
argument_list|)
decl_stmt|;
if|if
condition|(
name|existing
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|MetricsException
argument_list|(
literal|"Tag "
operator|+
name|info
operator|.
name|name
argument_list|()
operator|+
literal|" already exists!"
argument_list|)
throw|;
block|}
return|return
name|this
return|;
block|}
name|tagsMap
operator|.
name|put
argument_list|(
name|info
operator|.
name|name
argument_list|()
argument_list|,
name|tag
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|DynamicMetricsRegistry
name|tag
parameter_list|(
name|MetricsInfo
name|info
parameter_list|,
name|String
name|value
parameter_list|)
block|{
return|return
name|tag
argument_list|(
name|info
argument_list|,
name|value
argument_list|,
literal|false
argument_list|)
return|;
block|}
name|Collection
argument_list|<
name|MetricsTag
argument_list|>
name|tags
parameter_list|()
block|{
return|return
name|tagsMap
operator|.
name|values
argument_list|()
return|;
block|}
name|Collection
argument_list|<
name|MutableMetric
argument_list|>
name|metrics
parameter_list|()
block|{
return|return
name|metricsMap
operator|.
name|values
argument_list|()
return|;
block|}
comment|/**    * Sample all the mutable metrics and put the snapshot in the builder    * @param builder to contain the metrics snapshot    * @param all get all the metrics even if the values are not changed.    */
specifier|public
name|void
name|snapshot
parameter_list|(
name|MetricsRecordBuilder
name|builder
parameter_list|,
name|boolean
name|all
parameter_list|)
block|{
for|for
control|(
name|MetricsTag
name|tag
range|:
name|tags
argument_list|()
control|)
block|{
name|builder
operator|.
name|add
argument_list|(
name|tag
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|MutableMetric
name|metric
range|:
name|metrics
argument_list|()
control|)
block|{
name|metric
operator|.
name|snapshot
argument_list|(
name|builder
argument_list|,
name|all
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|toStringHelper
argument_list|(
name|this
argument_list|)
operator|.
name|add
argument_list|(
literal|"info"
argument_list|,
name|metricsInfo
argument_list|)
operator|.
name|add
argument_list|(
literal|"tags"
argument_list|,
name|tags
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
literal|"metrics"
argument_list|,
name|metrics
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Removes metric by name    * @param name name of the metric to remove    */
specifier|public
name|void
name|removeMetric
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|helper
operator|.
name|removeObjectName
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|metricsMap
operator|.
name|remove
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|removeHistogramMetrics
parameter_list|(
name|String
name|baseName
parameter_list|)
block|{
for|for
control|(
name|String
name|suffix
range|:
name|histogramSuffixes
control|)
block|{
name|removeMetric
argument_list|(
name|baseName
operator|+
name|suffix
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Get a MetricMutableGaugeLong from the storage.  If it is not there atomically put it.    *    * @param gaugeName              name of the gauge to create or get.    * @param potentialStartingValue value of the new gauge if we have to create it.    */
specifier|public
name|MutableGaugeLong
name|getLongGauge
parameter_list|(
name|String
name|gaugeName
parameter_list|,
name|long
name|potentialStartingValue
parameter_list|)
block|{
comment|//Try and get the guage.
name|MutableMetric
name|metric
init|=
name|metricsMap
operator|.
name|get
argument_list|(
name|gaugeName
argument_list|)
decl_stmt|;
comment|//If it's not there then try and put a new one in the storage.
if|if
condition|(
name|metric
operator|==
literal|null
condition|)
block|{
comment|//Create the potential new gauge.
name|MutableGaugeLong
name|newGauge
init|=
operator|new
name|MutableGaugeLong
argument_list|(
operator|new
name|MetricsInfoImpl
argument_list|(
name|gaugeName
argument_list|,
literal|""
argument_list|)
argument_list|,
name|potentialStartingValue
argument_list|)
decl_stmt|;
comment|// Try and put the gauge in.  This is atomic.
name|metric
operator|=
name|metricsMap
operator|.
name|putIfAbsent
argument_list|(
name|gaugeName
argument_list|,
name|newGauge
argument_list|)
expr_stmt|;
comment|//If the value we get back is null then the put was successful and we will return that.
comment|//otherwise gaugeLong should contain the thing that was in before the put could be completed.
if|if
condition|(
name|metric
operator|==
literal|null
condition|)
block|{
return|return
name|newGauge
return|;
block|}
block|}
if|if
condition|(
operator|!
operator|(
name|metric
operator|instanceof
name|MutableGaugeLong
operator|)
condition|)
block|{
throw|throw
operator|new
name|MetricsException
argument_list|(
literal|"Metric already exists in registry for metric name: "
operator|+
name|gaugeName
operator|+
literal|" and not of type MetricMutableGaugeLong"
argument_list|)
throw|;
block|}
return|return
operator|(
name|MutableGaugeLong
operator|)
name|metric
return|;
block|}
comment|/**    * Get a MetricMutableCounterLong from the storage.  If it is not there atomically put it.    *    * @param counterName            Name of the counter to get    * @param potentialStartingValue starting value if we have to create a new counter    */
specifier|public
name|MutableCounterLong
name|getLongCounter
parameter_list|(
name|String
name|counterName
parameter_list|,
name|long
name|potentialStartingValue
parameter_list|)
block|{
comment|//See getLongGauge for description on how this works.
name|MutableMetric
name|counter
init|=
name|metricsMap
operator|.
name|get
argument_list|(
name|counterName
argument_list|)
decl_stmt|;
if|if
condition|(
name|counter
operator|==
literal|null
condition|)
block|{
name|MutableCounterLong
name|newCounter
init|=
operator|new
name|MutableCounterLong
argument_list|(
operator|new
name|MetricsInfoImpl
argument_list|(
name|counterName
argument_list|,
literal|""
argument_list|)
argument_list|,
name|potentialStartingValue
argument_list|)
decl_stmt|;
name|counter
operator|=
name|metricsMap
operator|.
name|putIfAbsent
argument_list|(
name|counterName
argument_list|,
name|newCounter
argument_list|)
expr_stmt|;
if|if
condition|(
name|counter
operator|==
literal|null
condition|)
block|{
return|return
name|newCounter
return|;
block|}
block|}
if|if
condition|(
operator|!
operator|(
name|counter
operator|instanceof
name|MutableCounterLong
operator|)
condition|)
block|{
throw|throw
operator|new
name|MetricsException
argument_list|(
literal|"Metric already exists in registry for metric name: "
operator|+
name|counterName
operator|+
literal|" and not of type MetricMutableCounterLong"
argument_list|)
throw|;
block|}
return|return
operator|(
name|MutableCounterLong
operator|)
name|counter
return|;
block|}
specifier|public
name|MutableHistogram
name|getHistogram
parameter_list|(
name|String
name|histoName
parameter_list|)
block|{
comment|//See getLongGauge for description on how this works.
name|MutableMetric
name|histo
init|=
name|metricsMap
operator|.
name|get
argument_list|(
name|histoName
argument_list|)
decl_stmt|;
if|if
condition|(
name|histo
operator|==
literal|null
condition|)
block|{
name|MutableHistogram
name|newCounter
init|=
operator|new
name|MutableHistogram
argument_list|(
operator|new
name|MetricsInfoImpl
argument_list|(
name|histoName
argument_list|,
literal|""
argument_list|)
argument_list|)
decl_stmt|;
name|histo
operator|=
name|metricsMap
operator|.
name|putIfAbsent
argument_list|(
name|histoName
argument_list|,
name|newCounter
argument_list|)
expr_stmt|;
if|if
condition|(
name|histo
operator|==
literal|null
condition|)
block|{
return|return
name|newCounter
return|;
block|}
block|}
if|if
condition|(
operator|!
operator|(
name|histo
operator|instanceof
name|MutableHistogram
operator|)
condition|)
block|{
throw|throw
operator|new
name|MetricsException
argument_list|(
literal|"Metric already exists in registry for metric name: "
operator|+
name|histoName
operator|+
literal|" and not of type MutableHistogram"
argument_list|)
throw|;
block|}
return|return
operator|(
name|MutableHistogram
operator|)
name|histo
return|;
block|}
specifier|public
name|MetricMutableQuantiles
name|getQuantile
parameter_list|(
name|String
name|histoName
parameter_list|)
block|{
comment|//See getLongGauge for description on how this works.
name|MutableMetric
name|histo
init|=
name|metricsMap
operator|.
name|get
argument_list|(
name|histoName
argument_list|)
decl_stmt|;
if|if
condition|(
name|histo
operator|==
literal|null
condition|)
block|{
name|MetricMutableQuantiles
name|newCounter
init|=
operator|new
name|MetricMutableQuantiles
argument_list|(
name|histoName
argument_list|,
literal|""
argument_list|,
literal|"Ops"
argument_list|,
literal|""
argument_list|,
literal|60
argument_list|)
decl_stmt|;
name|histo
operator|=
name|metricsMap
operator|.
name|putIfAbsent
argument_list|(
name|histoName
argument_list|,
name|newCounter
argument_list|)
expr_stmt|;
if|if
condition|(
name|histo
operator|==
literal|null
condition|)
block|{
return|return
name|newCounter
return|;
block|}
block|}
if|if
condition|(
operator|!
operator|(
name|histo
operator|instanceof
name|MetricMutableQuantiles
operator|)
condition|)
block|{
throw|throw
operator|new
name|MetricsException
argument_list|(
literal|"Metric already exists in registry for metric name: "
operator|+
name|histoName
operator|+
literal|" and not of type MutableHistogram"
argument_list|)
throw|;
block|}
return|return
operator|(
name|MetricMutableQuantiles
operator|)
name|histo
return|;
block|}
specifier|private
parameter_list|<
name|T
extends|extends
name|MutableMetric
parameter_list|>
name|T
name|addNewMetricIfAbsent
parameter_list|(
name|String
name|name
parameter_list|,
name|T
name|ret
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|metricClass
parameter_list|)
block|{
comment|//If the value we get back is null then the put was successful and we will
comment|// return that. Otherwise metric should contain the thing that was in
comment|// before the put could be completed.
name|MutableMetric
name|metric
init|=
name|metricsMap
operator|.
name|putIfAbsent
argument_list|(
name|name
argument_list|,
name|ret
argument_list|)
decl_stmt|;
if|if
condition|(
name|metric
operator|==
literal|null
condition|)
block|{
return|return
name|ret
return|;
block|}
return|return
name|returnExistingWithCast
argument_list|(
name|metric
argument_list|,
name|metricClass
argument_list|,
name|name
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
parameter_list|<
name|T
parameter_list|>
name|T
name|returnExistingWithCast
parameter_list|(
name|MutableMetric
name|metric
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|metricClass
parameter_list|,
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
operator|!
name|metricClass
operator|.
name|isAssignableFrom
argument_list|(
name|metric
operator|.
name|getClass
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|MetricsException
argument_list|(
literal|"Metric already exists in registry for metric name: "
operator|+
name|name
operator|+
literal|" and not of type "
operator|+
name|metricClass
operator|+
literal|" but instead of type "
operator|+
name|metric
operator|.
name|getClass
argument_list|()
argument_list|)
throw|;
block|}
return|return
operator|(
name|T
operator|)
name|metric
return|;
block|}
specifier|public
name|void
name|clearMetrics
parameter_list|()
block|{
for|for
control|(
name|String
name|name
range|:
name|metricsMap
operator|.
name|keySet
argument_list|()
control|)
block|{
name|helper
operator|.
name|removeObjectName
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
name|metricsMap
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

