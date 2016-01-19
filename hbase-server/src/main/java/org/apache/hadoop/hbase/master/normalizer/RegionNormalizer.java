begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|normalizer
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|HBaseIOException
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
name|HRegionInfo
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
name|TableName
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
name|master
operator|.
name|MasterServices
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
name|master
operator|.
name|normalizer
operator|.
name|NormalizationPlan
operator|.
name|PlanType
import|;
end_import

begin_comment
comment|/**  * Performs "normalization" of regions on the cluster, making sure that suboptimal  * choice of split keys doesn't leave cluster in a situation when some regions are  * substantially larger than others for considerable amount of time.  *  * Users who want to use this feature could either use default {@link SimpleRegionNormalizer}  * or plug in their own implementation. Please note that overly aggressive normalization rules  * (attempting to make all regions perfectly equal in size) could potentially lead to  * "split/merge storms".  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|RegionNormalizer
block|{
comment|/**    * Set the master service. Must be called before first call to    * {@link #computePlanForTable(TableName)}.    * @param masterServices master services to use    */
name|void
name|setMasterServices
parameter_list|(
name|MasterServices
name|masterServices
parameter_list|)
function_decl|;
comment|/**    * Computes next optimal normalization plan.    * @param table table to normalize    * @return normalization actions to perform. Null if no action to take    */
name|List
argument_list|<
name|NormalizationPlan
argument_list|>
name|computePlanForTable
parameter_list|(
name|TableName
name|table
parameter_list|)
throws|throws
name|HBaseIOException
function_decl|;
comment|/**    * Notification for the case where plan couldn't be executed due to constraint violation, such as    * namespace quota    * @param hri the region which is involved in the plan    * @param type type of plan    */
name|void
name|planSkipped
parameter_list|(
name|HRegionInfo
name|hri
parameter_list|,
name|PlanType
name|type
parameter_list|)
function_decl|;
comment|/**    * @param type type of plan for which skipped count is to be returned    * @return the count of plans of specified type which were skipped    */
name|long
name|getSkippedCount
parameter_list|(
name|NormalizationPlan
operator|.
name|PlanType
name|type
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

