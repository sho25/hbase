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
comment|/**  * A metric which measures the distribution of values.  */
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
name|Histogram
extends|extends
name|Metric
block|{
comment|/**    * Adds a new value to the distribution.    *    * @param value The value to add    */
name|void
name|update
parameter_list|(
name|int
name|value
parameter_list|)
function_decl|;
comment|/**    * Adds a new value to the distribution.    *    * @param value The value to add    */
name|void
name|update
parameter_list|(
name|long
name|value
parameter_list|)
function_decl|;
comment|/**    * Return the total number of values added to the histogram.    * @return the total number of values.    */
name|long
name|getCount
parameter_list|()
function_decl|;
comment|/**    * Snapshot the current values in the Histogram    * @return a Snapshot of the distribution.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
name|Snapshot
name|snapshot
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

