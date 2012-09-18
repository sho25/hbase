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

begin_comment
comment|/**  *   BaseMetricsSource for dynamic metrics to announce to Metrics2  */
end_comment

begin_interface
specifier|public
interface|interface
name|BaseMetricsSource
block|{
comment|/**    * Clear out the metrics and re-prepare the source.    */
specifier|public
name|void
name|init
parameter_list|()
function_decl|;
comment|/**    * Set a gauge to a specific value.    *    * @param gaugeName the name of the gauge    * @param value     the value    */
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
function_decl|;
comment|/**    * Add some amount to a gauge.    *    * @param gaugeName the name of the gauge    * @param delta     the amount to change the gauge by.    */
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
function_decl|;
comment|/**    * Subtract some amount from a gauge.    *    * @param gaugeName the name of the gauge    * @param delta     the amount to change the gauge by.    */
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
function_decl|;
comment|/**    * Remove a gauge and no longer announce it.    *    * @param key Name of the gauge to remove.    */
specifier|public
name|void
name|removeGauge
parameter_list|(
name|String
name|key
parameter_list|)
function_decl|;
comment|/**    * Add some amount to a counter.    *    * @param counterName the name of the counter    * @param delta       the amount to change the counter by.    */
specifier|public
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
comment|/**    * Remove a counter and stop announcing it to metrics2.    *    * @param key    */
specifier|public
name|void
name|removeCounter
parameter_list|(
name|String
name|key
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

