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
name|DynamicMetricsRegistry
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
name|MetricMutableQuantiles
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
name|MutableCounterLong
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
name|MutableHistogram
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
name|source
operator|.
name|JvmMetrics
import|;
end_import

begin_comment
comment|/**  * Hadoop 2 implementation of BaseMetricsSource (using metrics2 framework)  */
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
enum|enum
name|DefaultMetricsSystemInitializer
block|{
name|INSTANCE
block|;
specifier|private
name|boolean
name|inited
init|=
literal|false
decl_stmt|;
specifier|private
name|JvmMetrics
name|jvmMetricsSource
decl_stmt|;
specifier|synchronized
name|void
name|init
parameter_list|(
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
name|inited
condition|)
return|return;
name|inited
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
name|jvmMetricsSource
operator|=
name|JvmMetrics
operator|.
name|create
argument_list|(
name|name
argument_list|,
literal|""
argument_list|,
name|DefaultMetricsSystem
operator|.
name|instance
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_METRICS_SYSTEM_NAME
init|=
literal|"hbase"
decl_stmt|;
specifier|protected
specifier|final
name|DynamicMetricsRegistry
name|metricsRegistry
decl_stmt|;
specifier|protected
specifier|final
name|String
name|metricsName
decl_stmt|;
specifier|protected
specifier|final
name|String
name|metricsDescription
decl_stmt|;
specifier|protected
specifier|final
name|String
name|metricsContext
decl_stmt|;
specifier|protected
specifier|final
name|String
name|metricsJmxContext
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
parameter_list|,
name|String
name|metricsJmxContext
parameter_list|)
block|{
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
name|this
operator|.
name|metricsContext
operator|=
name|metricsContext
expr_stmt|;
name|this
operator|.
name|metricsJmxContext
operator|=
name|metricsJmxContext
expr_stmt|;
name|metricsRegistry
operator|=
operator|new
name|DynamicMetricsRegistry
argument_list|(
name|metricsName
argument_list|)
operator|.
name|setContext
argument_list|(
name|metricsContext
argument_list|)
expr_stmt|;
name|DefaultMetricsSystemInitializer
operator|.
name|INSTANCE
operator|.
name|init
argument_list|(
name|metricsName
argument_list|)
expr_stmt|;
comment|//Register this instance.
name|DefaultMetricsSystem
operator|.
name|instance
argument_list|()
operator|.
name|register
argument_list|(
name|metricsJmxContext
argument_list|,
name|metricsDescription
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|init
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|()
block|{
name|this
operator|.
name|metricsRegistry
operator|.
name|clearMetrics
argument_list|()
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
name|MutableGaugeLong
name|gaugeInt
init|=
name|metricsRegistry
operator|.
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
name|MutableGaugeLong
name|gaugeInt
init|=
name|metricsRegistry
operator|.
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
name|MutableGaugeLong
name|gaugeInt
init|=
name|metricsRegistry
operator|.
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
name|MutableCounterLong
name|counter
init|=
name|metricsRegistry
operator|.
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
annotation|@
name|Override
specifier|public
name|void
name|updateHistogram
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|value
parameter_list|)
block|{
name|MutableHistogram
name|histo
init|=
name|metricsRegistry
operator|.
name|getHistogram
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|histo
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateQuantile
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|value
parameter_list|)
block|{
name|MetricMutableQuantiles
name|histo
init|=
name|metricsRegistry
operator|.
name|getQuantile
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|histo
operator|.
name|add
argument_list|(
name|value
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
name|metricsRegistry
operator|.
name|removeMetric
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
name|metricsRegistry
operator|.
name|removeMetric
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|DynamicMetricsRegistry
name|getMetricsRegistry
parameter_list|()
block|{
return|return
name|metricsRegistry
return|;
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
name|metricsRegistry
operator|.
name|snapshot
argument_list|(
name|metricsCollector
operator|.
name|addRecord
argument_list|(
name|metricsRegistry
operator|.
name|info
argument_list|()
argument_list|)
argument_list|,
name|all
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

