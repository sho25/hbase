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
name|java
operator|.
name|util
operator|.
name|Optional
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
name|HBaseInterfaceAudience
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * General purpose factory for creating various metrics.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|MetricRegistry
extends|extends
name|MetricSet
block|{
comment|/**    * Get or construct a {@link Timer} used to measure durations and report rates.    *    * @param name the name of the timer.    * @return An instance of {@link Timer}.    */
name|Timer
name|timer
parameter_list|(
name|String
name|name
parameter_list|)
function_decl|;
comment|/**    * Get or construct a {@link Histogram} used to measure a distribution of values.    *    * @param name The name of the Histogram.    * @return An instance of {@link Histogram}.    */
name|Histogram
name|histogram
parameter_list|(
name|String
name|name
parameter_list|)
function_decl|;
comment|/**    * Get or construct a {@link Meter} used to measure durations and report distributions (a    * combination of a {@link Timer} and a {@link Histogram}.    *    * @param name The name of the Meter.    * @return An instance of {@link Meter}.    */
name|Meter
name|meter
parameter_list|(
name|String
name|name
parameter_list|)
function_decl|;
comment|/**    * Get or construct a {@link Counter} used to track a mutable number.    *    * @param name The name of the Counter    * @return An instance of {@link Counter}.    */
name|Counter
name|counter
parameter_list|(
name|String
name|name
parameter_list|)
function_decl|;
comment|/**    * Register a {@link Gauge}. The Gauge will be invoked at a period defined by the implementation    * of {@link MetricRegistry}.    * @param name The name of the Gauge.    * @param gauge A callback to compute the current value.    * @return the registered gauge, or the existing gauge    */
parameter_list|<
name|T
parameter_list|>
name|Gauge
argument_list|<
name|T
argument_list|>
name|register
parameter_list|(
name|String
name|name
parameter_list|,
name|Gauge
argument_list|<
name|T
argument_list|>
name|gauge
parameter_list|)
function_decl|;
comment|/**    * Registers the {@link Metric} with the given name if there does not exist one with the same    * name. Returns the newly registered or existing Metric.    * @param name The name of the Metric.    * @param metric the metric to register    * @return the registered metric, or the existing metrid    */
name|Metric
name|register
parameter_list|(
name|String
name|name
parameter_list|,
name|Metric
name|metric
parameter_list|)
function_decl|;
comment|/**    * Registers the {@link Metric}s in the given MetricSet.    * @param metricSet set of metrics to register.    */
name|void
name|registerAll
parameter_list|(
name|MetricSet
name|metricSet
parameter_list|)
function_decl|;
comment|/**    * Returns previously registered metric with the name if any.    * @param name the name of the metric    * @return previously registered metric    */
name|Optional
argument_list|<
name|Metric
argument_list|>
name|get
parameter_list|(
name|String
name|name
parameter_list|)
function_decl|;
comment|/**    * Removes the metric with the given name.    *    * @param name the name of the metric    * @return true if the metric is removed.    */
name|boolean
name|remove
parameter_list|(
name|String
name|name
parameter_list|)
function_decl|;
comment|/**    * Return the MetricRegistryInfo object for this registry.    * @return MetricRegistryInfo describing the registry.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
name|MetricRegistryInfo
name|getMetricRegistryInfo
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

