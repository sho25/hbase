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
name|test
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
name|hbase
operator|.
name|metrics
operator|.
name|BaseSource
import|;
end_import

begin_comment
comment|/** Interface of a class to make assertions about metrics values. */
end_comment

begin_interface
specifier|public
interface|interface
name|MetricsAssertHelper
block|{
comment|/**    * Init helper.  This method will make sure that the metrics system is set    * up for tests.    */
name|void
name|init
parameter_list|()
function_decl|;
comment|/**    * Assert that a tag exists and has a given value.    *    * @param name     The name of the tag.    * @param expected The expected value    * @param source   The BaseSource{@link BaseSource} that will provide the tags,    *                 gauges, and counters.    */
name|void
name|assertTag
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|expected
parameter_list|,
name|BaseSource
name|source
parameter_list|)
function_decl|;
comment|/**    * Assert that a gauge exists and that it's value is equal to the expected value.    *    * @param name     The name of the gauge    * @param expected The expected value of the gauge.    * @param source   The BaseSource{@link BaseSource} that will provide the tags,    *                 gauges, and counters.    */
name|void
name|assertGauge
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|expected
parameter_list|,
name|BaseSource
name|source
parameter_list|)
function_decl|;
comment|/**    * Assert that a gauge exists and it's value is greater than a given value    *    * @param name     The name of the gauge    * @param expected Value that the gauge is expected to be greater than    * @param source   The BaseSource{@link BaseSource} that will provide the tags,    *                 gauges, and counters.    */
name|void
name|assertGaugeGt
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|expected
parameter_list|,
name|BaseSource
name|source
parameter_list|)
function_decl|;
comment|/**    * Assert that a gauge exists and it's value is less than a given value    *    * @param name     The name of the gauge    * @param expected Value that the gauge is expected to be less than    * @param source   The BaseSource{@link BaseSource} that will provide the tags,    *                 gauges, and counters.    */
name|void
name|assertGaugeLt
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|expected
parameter_list|,
name|BaseSource
name|source
parameter_list|)
function_decl|;
comment|/**    * Assert that a gauge exists and that it's value is equal to the expected value.    *    * @param name     The name of the gauge    * @param expected The expected value of the gauge.    * @param source   The BaseSource{@link BaseSource} that will provide the tags,    *                 gauges, and counters.    */
name|void
name|assertGauge
parameter_list|(
name|String
name|name
parameter_list|,
name|double
name|expected
parameter_list|,
name|BaseSource
name|source
parameter_list|)
function_decl|;
comment|/**    * Assert that a gauge exists and it's value is greater than a given value    *    * @param name     The name of the gauge    * @param expected Value that the gauge is expected to be greater than    * @param source   The BaseSource{@link BaseSource} that will provide the tags,    *                 gauges, and counters.    */
name|void
name|assertGaugeGt
parameter_list|(
name|String
name|name
parameter_list|,
name|double
name|expected
parameter_list|,
name|BaseSource
name|source
parameter_list|)
function_decl|;
comment|/**    * Assert that a gauge exists and it's value is less than a given value    *    * @param name     The name of the gauge    * @param expected Value that the gauge is expected to be less than    * @param source   The BaseSource{@link BaseSource} that will provide the tags,    *                 gauges, and counters.    */
name|void
name|assertGaugeLt
parameter_list|(
name|String
name|name
parameter_list|,
name|double
name|expected
parameter_list|,
name|BaseSource
name|source
parameter_list|)
function_decl|;
comment|/**    * Assert that a counter exists and that it's value is equal to the expected value.    *    * @param name     The name of the counter.    * @param expected The expected value    * @param source   The BaseSource{@link BaseSource} that will provide the tags,    *                 gauges, and counters.    */
name|void
name|assertCounter
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|expected
parameter_list|,
name|BaseSource
name|source
parameter_list|)
function_decl|;
comment|/**    * Assert that a counter exists and that it's value is greater than the given value.    *    * @param name     The name of the counter.    * @param expected The value the counter is expected to be greater than.    * @param source   The BaseSource{@link BaseSource} that will provide the tags,    *                 gauges, and counters.    */
name|void
name|assertCounterGt
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|expected
parameter_list|,
name|BaseSource
name|source
parameter_list|)
function_decl|;
comment|/**    * Assert that a counter exists and that it's value is less than the given value.    *    * @param name     The name of the counter.    * @param expected The value the counter is expected to be less than.    * @param source   The BaseSource{@link BaseSource} that will provide the tags,    *                 gauges, and counters.    */
name|void
name|assertCounterLt
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|expected
parameter_list|,
name|BaseSource
name|source
parameter_list|)
function_decl|;
comment|/**    * Get the value of a counter.    *    * @param name   name of the counter.    * @param source The BaseSource{@link BaseSource} that will provide the tags,    *               gauges, and counters.    * @return long value of the counter.    */
name|long
name|getCounter
parameter_list|(
name|String
name|name
parameter_list|,
name|BaseSource
name|source
parameter_list|)
function_decl|;
comment|/**    * Check if a dynamic counter exists.    *    * @param name   name of the counter.    * @param source The BaseSource{@link BaseSource} that will provide the tags,    *               gauges, and counters.    * @return boolean true id counter metric exists.    */
name|boolean
name|checkCounterExists
parameter_list|(
name|String
name|name
parameter_list|,
name|BaseSource
name|source
parameter_list|)
function_decl|;
comment|/**    * Get the value of a gauge as a double.    *    * @param name   name of the gauge.    * @param source The BaseSource{@link BaseSource} that will provide the tags,    *               gauges, and counters.    * @return double value of the gauge.    */
name|double
name|getGaugeDouble
parameter_list|(
name|String
name|name
parameter_list|,
name|BaseSource
name|source
parameter_list|)
function_decl|;
comment|/**    * Get the value of a gauge as a long.    *    * @param name   name of the gauge.    * @param source The BaseSource{@link BaseSource} that will provide the tags,    *               gauges, and counters.    * @return long value of the gauge.    */
name|long
name|getGaugeLong
parameter_list|(
name|String
name|name
parameter_list|,
name|BaseSource
name|source
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

