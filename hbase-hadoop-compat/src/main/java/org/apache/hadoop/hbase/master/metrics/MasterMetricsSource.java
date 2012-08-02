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
name|metrics
operator|.
name|BaseMetricsSource
import|;
end_import

begin_comment
comment|/**  * Interface that classes that expose metrics about the master will implement.  */
end_comment

begin_interface
specifier|public
interface|interface
name|MasterMetricsSource
extends|extends
name|BaseMetricsSource
block|{
comment|/**    * The name of the metrics    */
specifier|public
specifier|static
specifier|final
name|String
name|METRICS_NAME
init|=
literal|"HMaster"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under.    */
specifier|public
specifier|static
specifier|final
name|String
name|METRICS_CONTEXT
init|=
literal|"HMaster,sub=Dynamic"
decl_stmt|;
comment|/**    * Description    */
specifier|public
specifier|static
specifier|final
name|String
name|METRICS_DESCRIPTION
init|=
literal|"Metrics about HBase master server"
decl_stmt|;
comment|/**    * Increment the number of requests the cluster has seen.    * @param inc Ammount to increment the total by.    */
specifier|public
name|void
name|incRequests
parameter_list|(
specifier|final
name|int
name|inc
parameter_list|)
function_decl|;
comment|/**    * Set the number of regions in transition.    * @param ritCount count of the regions in transition.    */
specifier|public
name|void
name|setRIT
parameter_list|(
name|int
name|ritCount
parameter_list|)
function_decl|;
comment|/**    * Set the count of the number of regions that have been in transition over the threshold time.    * @param ritCountOverThreshold number of regions in transition for longer than threshold.    */
specifier|public
name|void
name|setRITCountOverThreshold
parameter_list|(
name|int
name|ritCountOverThreshold
parameter_list|)
function_decl|;
comment|/**    * Set the oldest region in transition.    * @param age age of the oldest RIT.    */
specifier|public
name|void
name|setRITOldestAge
parameter_list|(
name|long
name|age
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

