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
name|master
operator|.
name|balancer
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
comment|/**  * This interface extends the basic metrics balancer source to add a function  * to report metrics that related to stochastic load balancer. The purpose is to  * offer an insight to the internal cost calculations that can be useful to tune  * the balancer. For details, refer to HBASE-13965  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MetricsStochasticBalancerSource
extends|extends
name|MetricsBalancerSource
block|{
comment|/**    * Updates the number of metrics reported to JMX    */
specifier|public
name|void
name|updateMetricsSize
parameter_list|(
name|int
name|size
parameter_list|)
function_decl|;
comment|/**    * Reports stochastic load balancer costs to JMX    */
specifier|public
name|void
name|updateStochasticCost
parameter_list|(
name|String
name|tableName
parameter_list|,
name|String
name|costFunctionName
parameter_list|,
name|String
name|costFunctionDesc
parameter_list|,
name|Double
name|value
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

