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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  *   BaseSource for dynamic metrics to announce to Metrics2.  *   In hbase-hadoop{1|2}-compat there is an implementation of this interface.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|BaseSource
block|{
name|String
name|HBASE_METRICS_SYSTEM_NAME
init|=
literal|"HBase"
decl_stmt|;
comment|/**    * Clear out the metrics and re-prepare the source.    */
name|void
name|init
parameter_list|()
function_decl|;
comment|/**    * Set a gauge to a specific value.    *    * @param gaugeName the name of the gauge    * @param value     the value    */
name|void
name|setGauge
parameter_list|(
name|String
name|gaugeName
parameter_list|,
name|long
name|value
parameter_list|)
function_decl|;
comment|/**    * Add some amount to a gauge.    *    * @param gaugeName the name of the gauge    * @param delta     the amount to change the gauge by.    */
name|void
name|incGauge
parameter_list|(
name|String
name|gaugeName
parameter_list|,
name|long
name|delta
parameter_list|)
function_decl|;
comment|/**    * Subtract some amount from a gauge.    *    * @param gaugeName the name of the gauge    * @param delta     the amount to change the gauge by.    */
name|void
name|decGauge
parameter_list|(
name|String
name|gaugeName
parameter_list|,
name|long
name|delta
parameter_list|)
function_decl|;
comment|/**    * Remove a metric and no longer announce it.    *    * @param key Name of the gauge to remove.    */
name|void
name|removeMetric
parameter_list|(
name|String
name|key
parameter_list|)
function_decl|;
comment|/**    * Add some amount to a counter.    *    * @param counterName the name of the counter    * @param delta       the amount to change the counter by.    */
name|void
name|incCounters
parameter_list|(
name|String
name|counterName
parameter_list|,
name|long
name|delta
parameter_list|)
function_decl|;
comment|/**    * Add some value to a histogram.    *    * @param name the name of the histogram    * @param value the value to add to the histogram    */
name|void
name|updateHistogram
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|value
parameter_list|)
function_decl|;
comment|/**    * Get the metrics context.  For hadoop metrics2 system this is usually an all lowercased string.    * eg. regionserver, master, thriftserver    *    * @return The string context used to register this source to hadoop's metrics2 system.    */
name|String
name|getMetricsContext
parameter_list|()
function_decl|;
comment|/**    * Get the description of what this source exposes.    */
name|String
name|getMetricsDescription
parameter_list|()
function_decl|;
comment|/**    * Get the name of the context in JMX that this source will be exposed through.    * This is in ObjectName format. With the default context being Hadoop -&gt; HBase    */
name|String
name|getMetricsJmxContext
parameter_list|()
function_decl|;
comment|/**    * Get the name of the metrics that are being exported by this source.    * Eg. IPC, GC, WAL    */
name|String
name|getMetricsName
parameter_list|()
function_decl|;
specifier|default
name|MetricRegistryInfo
name|getMetricRegistryInfo
parameter_list|()
block|{
return|return
operator|new
name|MetricRegistryInfo
argument_list|(
name|getMetricsName
argument_list|()
argument_list|,
name|getMetricsDescription
argument_list|()
argument_list|,
name|getMetricsContext
argument_list|()
argument_list|,
name|getMetricsJmxContext
argument_list|()
argument_list|,
literal|true
argument_list|)
return|;
block|}
block|}
end_interface

end_unit

