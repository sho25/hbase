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
name|metrics2
operator|.
name|MetricsBuilder
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
name|MetricsSource
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
name|DefaultMetricsSystem
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
name|MetricMutableCounterLong
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
name|MetricMutableGaugeLong
import|;
end_import

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
name|concurrent
operator|.
name|ConcurrentHashMap
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

begin_comment
comment|/**  * Hadoop 1 implementation of BaseMetricsSource  */
end_comment

begin_class
specifier|public
class|class
name|BaseMetricsSourceImpl
implements|implements
name|BaseMetricsSource
implements|,
name|MetricsSource
block|{
specifier|private
specifier|static
name|boolean
name|defaultMetricsSystemInited
init|=
literal|false
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_METRICS_SYSTEM_NAME
init|=
literal|"hbase"
decl_stmt|;
specifier|public
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|MetricMutableGaugeLong
argument_list|>
name|gauges
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|MetricMutableGaugeLong
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|MetricMutableCounterLong
argument_list|>
name|counters
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|MetricMutableCounterLong
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
name|String
name|metricsContext
decl_stmt|;
specifier|protected
name|String
name|metricsName
decl_stmt|;
specifier|protected
name|String
name|metricsDescription
decl_stmt|;
specifier|public
name|BaseMetricsSourceImpl
parameter_list|(
name|String
name|metricsName
parameter_list|,
name|String
name|metricsDescription
parameter_list|,
name|String
name|metricsContext
parameter_list|)
block|{
name|this
operator|.
name|metricsContext
operator|=
name|metricsContext
expr_stmt|;
name|this
operator|.
name|metricsName
operator|=
name|metricsName
expr_stmt|;
name|this
operator|.
name|metricsDescription
operator|=
name|metricsDescription
expr_stmt|;
if|if
condition|(
operator|!
name|defaultMetricsSystemInited
condition|)
block|{
comment|//Not too worried about mutli-threaded here as all it does is spam the logs.
name|defaultMetricsSystemInited
operator|=
literal|true
expr_stmt|;
name|DefaultMetricsSystem
operator|.
name|initialize
argument_list|(
name|HBASE_METRICS_SYSTEM_NAME
argument_list|)
expr_stmt|;
block|}
comment|//Register this instance.
name|DefaultMetricsSystem
operator|.
name|registerSource
argument_list|(
name|this
operator|.
name|metricsContext
argument_list|,
name|this
operator|.
name|metricsDescription
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set a single gauge to a value.    *    * @param gaugeName gauge name    * @param value     the new value of the gauge.    */
specifier|public
name|void
name|setGauge
parameter_list|(
name|String
name|gaugeName
parameter_list|,
name|long
name|value
parameter_list|)
block|{
name|MetricMutableGaugeLong
name|gaugeInt
init|=
name|getLongGauge
argument_list|(
name|gaugeName
argument_list|,
name|value
argument_list|)
decl_stmt|;
name|gaugeInt
operator|.
name|set
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add some amount to a gauge.    *    * @param gaugeName The name of the gauge to increment.    * @param delta     The amount to increment the gauge by.    */
specifier|public
name|void
name|incGauge
parameter_list|(
name|String
name|gaugeName
parameter_list|,
name|long
name|delta
parameter_list|)
block|{
name|MetricMutableGaugeLong
name|gaugeInt
init|=
name|getLongGauge
argument_list|(
name|gaugeName
argument_list|,
literal|0l
argument_list|)
decl_stmt|;
name|gaugeInt
operator|.
name|incr
argument_list|(
name|delta
argument_list|)
expr_stmt|;
block|}
comment|/**    * Decrease the value of a named gauge.    *    * @param gaugeName The name of the gauge.    * @param delta     the ammount to subtract from a gauge value.    */
specifier|public
name|void
name|decGauge
parameter_list|(
name|String
name|gaugeName
parameter_list|,
name|long
name|delta
parameter_list|)
block|{
name|MetricMutableGaugeLong
name|gaugeInt
init|=
name|getLongGauge
argument_list|(
name|gaugeName
argument_list|,
literal|0l
argument_list|)
decl_stmt|;
name|gaugeInt
operator|.
name|decr
argument_list|(
name|delta
argument_list|)
expr_stmt|;
block|}
comment|/**    * Increment a named counter by some value.    *    * @param key   the name of the counter    * @param delta the ammount to increment    */
specifier|public
name|void
name|incCounters
parameter_list|(
name|String
name|key
parameter_list|,
name|long
name|delta
parameter_list|)
block|{
name|MetricMutableCounterLong
name|counter
init|=
name|getLongCounter
argument_list|(
name|key
argument_list|,
literal|0l
argument_list|)
decl_stmt|;
name|counter
operator|.
name|incr
argument_list|(
name|delta
argument_list|)
expr_stmt|;
block|}
comment|/**    * Remove a named gauge.    *    * @param key    */
specifier|public
name|void
name|removeGauge
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|gauges
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
comment|/**    * Remove a named counter.    *    * @param key    */
specifier|public
name|void
name|removeCounter
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|counters
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
comment|/**    * Method to export all the metrics.    *    * @param metricsBuilder Builder to accept metrics    * @param all            push all or only changed?    */
annotation|@
name|Override
specifier|public
name|void
name|getMetrics
parameter_list|(
name|MetricsBuilder
name|metricsBuilder
parameter_list|,
name|boolean
name|all
parameter_list|)
block|{
name|MetricsRecordBuilder
name|rb
init|=
name|metricsBuilder
operator|.
name|addRecord
argument_list|(
name|metricsName
argument_list|)
operator|.
name|setContext
argument_list|(
name|metricsContext
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|MetricMutableCounterLong
argument_list|>
name|entry
range|:
name|counters
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|snapshot
argument_list|(
name|rb
argument_list|,
name|all
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|MetricMutableGaugeLong
argument_list|>
name|entry
range|:
name|gauges
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|snapshot
argument_list|(
name|rb
argument_list|,
name|all
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Get a MetricMutableGaugeLong from the storage.  If it is not there atomically put it.    *    * @param gaugeName              name of the gauge to create or get.    * @param potentialStartingValue value of the new counter if we have to create it.    * @return    */
specifier|private
name|MetricMutableGaugeLong
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
name|MetricMutableGaugeLong
name|gauge
init|=
name|gauges
operator|.
name|get
argument_list|(
name|gaugeName
argument_list|)
decl_stmt|;
comment|//If it's not there then try and put a new one in the storage.
if|if
condition|(
name|gauge
operator|==
literal|null
condition|)
block|{
comment|//Create the potential new gauge.
name|MetricMutableGaugeLong
name|newGauge
init|=
operator|new
name|MetricMutableGaugeLong
argument_list|(
name|gaugeName
argument_list|,
literal|""
argument_list|,
name|potentialStartingValue
argument_list|)
decl_stmt|;
comment|// Try and put the gauge in.  This is atomic.
name|gauge
operator|=
name|gauges
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
name|gauge
operator|==
literal|null
condition|)
block|{
name|gauge
operator|=
name|newGauge
expr_stmt|;
block|}
block|}
return|return
name|gauge
return|;
block|}
comment|/**    * Get a MetricMutableCounterLong from the storage.  If it is not there atomically put it.    *    * @param counterName            Name of the counter to get    * @param potentialStartingValue starting value if we have to create a new counter    * @return    */
specifier|private
name|MetricMutableCounterLong
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
name|MetricMutableCounterLong
name|counter
init|=
name|counters
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
name|MetricMutableCounterLong
name|newCounter
init|=
operator|new
name|MetricMutableCounterLong
argument_list|(
name|counterName
argument_list|,
literal|""
argument_list|,
name|potentialStartingValue
argument_list|)
decl_stmt|;
name|counter
operator|=
name|counters
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
name|counter
operator|=
name|newCounter
expr_stmt|;
block|}
block|}
return|return
name|counter
return|;
block|}
block|}
end_class

end_unit

