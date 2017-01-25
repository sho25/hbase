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
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * A metric which measure the rate at which some operation is invoked.  */
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
name|Meter
extends|extends
name|Metric
block|{
comment|/**    * Records one occurrence.    */
name|void
name|mark
parameter_list|()
function_decl|;
comment|/**    * Records {@code events} occurrences.    *    * @param events Number of occurrences to record.    */
name|void
name|mark
parameter_list|(
name|long
name|events
parameter_list|)
function_decl|;
comment|/**    * Returns the number of events.    * @return the number of events.    */
name|long
name|getCount
parameter_list|()
function_decl|;
comment|/**    * Returns the mean rate at which events have occurred since the meter was created.    * @return the mean rate at which events have occurred since the meter was created    */
name|double
name|getMeanRate
parameter_list|()
function_decl|;
comment|/**    * Returns the one-minute exponentially-weighted moving average rate at which events have    * occurred since the meter was created.    *<p/>    * This rate has the same exponential decay factor as the one-minute load average in the {@code    * top} Unix command.    *    * @return the one-minute exponentially-weighted moving average rate at which events have    *         occurred since the meter was created    */
name|double
name|getOneMinuteRate
parameter_list|()
function_decl|;
comment|/**    * Returns the five-minute exponentially-weighted moving average rate at which events have    * occurred since the meter was created.    *<p/>    * This rate has the same exponential decay factor as the five-minute load average in the {@code    * top} Unix command.    *    * @return the five-minute exponentially-weighted moving average rate at which events have    *         occurred since the meter was created    */
name|double
name|getFiveMinuteRate
parameter_list|()
function_decl|;
comment|/**    * Returns the fifteen-minute exponentially-weighted moving average rate at which events have    * occurred since the meter was created.    *<p/>    * This rate has the same exponential decay factor as the fifteen-minute load average in the    * {@code top} Unix command.    *    * @return the fifteen-minute exponentially-weighted moving average rate at which events have    *         occurred since the meter was created    */
name|double
name|getFifteenMinuteRate
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

