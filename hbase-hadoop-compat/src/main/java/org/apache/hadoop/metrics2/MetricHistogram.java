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
package|;
end_package

begin_comment
comment|/**  * Metrics Histogram interface.  Implementing classes will expose computed  * quartile values through the metrics system.  */
end_comment

begin_interface
specifier|public
interface|interface
name|MetricHistogram
block|{
comment|//Strings used to create metrics names.
name|String
name|NUM_OPS_METRIC_NAME
init|=
literal|"_num_ops"
decl_stmt|;
name|String
name|MIN_METRIC_NAME
init|=
literal|"_min"
decl_stmt|;
name|String
name|MAX_METRIC_NAME
init|=
literal|"_max"
decl_stmt|;
name|String
name|MEAN_METRIC_NAME
init|=
literal|"_mean"
decl_stmt|;
name|String
name|MEDIAN_METRIC_NAME
init|=
literal|"_median"
decl_stmt|;
name|String
name|SEVENTY_FIFTH_PERCENTILE_METRIC_NAME
init|=
literal|"_75th_percentile"
decl_stmt|;
name|String
name|NINETY_FIFTH_PERCENTILE_METRIC_NAME
init|=
literal|"_95th_percentile"
decl_stmt|;
name|String
name|NINETY_NINETH_PERCENTILE_METRIC_NAME
init|=
literal|"_99th_percentile"
decl_stmt|;
comment|/**    * Add a single value to a histogram's stream of values.    * @param value    */
name|void
name|add
parameter_list|(
name|long
name|value
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

