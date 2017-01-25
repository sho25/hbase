begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * A statictical sample of histogram values.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|Snapshot
block|{
comment|/**    * Return the values with the given quantiles.    * @param quantiles the requested quantiles.    * @return the value for the quantiles.    */
name|long
index|[]
name|getQuantiles
parameter_list|(
name|double
index|[]
name|quantiles
parameter_list|)
function_decl|;
comment|/**    * Return the values with the default quantiles.    * @return the value for default the quantiles.    */
name|long
index|[]
name|getQuantiles
parameter_list|()
function_decl|;
comment|/**    * Returns the number of values in the snapshot.    *    * @return the number of values    */
name|long
name|getCount
parameter_list|()
function_decl|;
comment|/**    * Returns the total count below the given value    * @param val the value    * @return the total count below the given value    */
name|long
name|getCountAtOrBelow
parameter_list|(
name|long
name|val
parameter_list|)
function_decl|;
comment|/**    * Returns the value at the 25th percentile in the distribution.    *    * @return the value at the 25th percentile    */
name|long
name|get25thPercentile
parameter_list|()
function_decl|;
comment|/**    * Returns the value at the 75th percentile in the distribution.    *    * @return the value at the 75th percentile    */
name|long
name|get75thPercentile
parameter_list|()
function_decl|;
comment|/**    * Returns the value at the 90th percentile in the distribution.    *    * @return the value at the 90th percentile    */
name|long
name|get90thPercentile
parameter_list|()
function_decl|;
comment|/**    * Returns the value at the 95th percentile in the distribution.    *    * @return the value at the 95th percentile    */
name|long
name|get95thPercentile
parameter_list|()
function_decl|;
comment|/**    * Returns the value at the 98th percentile in the distribution.    *    * @return the value at the 98th percentile    */
name|long
name|get98thPercentile
parameter_list|()
function_decl|;
comment|/**    * Returns the value at the 99th percentile in the distribution.    *    * @return the value at the 99th percentile    */
name|long
name|get99thPercentile
parameter_list|()
function_decl|;
comment|/**    * Returns the value at the 99.9th percentile in the distribution.    *    * @return the value at the 99.9th percentile    */
name|long
name|get999thPercentile
parameter_list|()
function_decl|;
comment|/**    * Returns the median value in the distribution.    *    * @return the median value    */
name|long
name|getMedian
parameter_list|()
function_decl|;
comment|/**    * Returns the highest value in the snapshot.    *    * @return the highest value    */
name|long
name|getMax
parameter_list|()
function_decl|;
comment|/**    * Returns the arithmetic mean of the values in the snapshot.    *    * @return the arithmetic mean    */
name|long
name|getMean
parameter_list|()
function_decl|;
comment|/**    * Returns the lowest value in the snapshot.    *    * @return the lowest value    */
name|long
name|getMin
parameter_list|()
function_decl|;
comment|// TODO: Dropwizard histograms also track stddev
block|}
end_interface

end_unit

