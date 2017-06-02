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
name|CompatibilitySingletonFactory
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
name|procedure2
operator|.
name|ProcedureMetrics
import|;
end_import

begin_import
import|import static
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
name|MetricsMaster
operator|.
name|convertToProcedureMetrics
import|;
end_import

begin_class
specifier|public
class|class
name|MetricsAssignmentManager
block|{
specifier|private
specifier|final
name|MetricsAssignmentManagerSource
name|assignmentManagerSource
decl_stmt|;
specifier|private
specifier|final
name|ProcedureMetrics
name|assignProcMetrics
decl_stmt|;
specifier|private
specifier|final
name|ProcedureMetrics
name|unassignProcMetrics
decl_stmt|;
specifier|private
specifier|final
name|ProcedureMetrics
name|splitProcMetrics
decl_stmt|;
specifier|private
specifier|final
name|ProcedureMetrics
name|mergeProcMetrics
decl_stmt|;
specifier|public
name|MetricsAssignmentManager
parameter_list|()
block|{
name|assignmentManagerSource
operator|=
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsAssignmentManagerSource
operator|.
name|class
argument_list|)
expr_stmt|;
name|assignProcMetrics
operator|=
name|convertToProcedureMetrics
argument_list|(
name|assignmentManagerSource
operator|.
name|getAssignMetrics
argument_list|()
argument_list|)
expr_stmt|;
name|unassignProcMetrics
operator|=
name|convertToProcedureMetrics
argument_list|(
name|assignmentManagerSource
operator|.
name|getUnassignMetrics
argument_list|()
argument_list|)
expr_stmt|;
name|splitProcMetrics
operator|=
name|convertToProcedureMetrics
argument_list|(
name|assignmentManagerSource
operator|.
name|getSplitMetrics
argument_list|()
argument_list|)
expr_stmt|;
name|mergeProcMetrics
operator|=
name|convertToProcedureMetrics
argument_list|(
name|assignmentManagerSource
operator|.
name|getMergeMetrics
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MetricsAssignmentManagerSource
name|getMetricsProcSource
parameter_list|()
block|{
return|return
name|assignmentManagerSource
return|;
block|}
comment|/**    * set new value for number of regions in transition.    * @param ritCount    */
specifier|public
name|void
name|updateRITCount
parameter_list|(
specifier|final
name|int
name|ritCount
parameter_list|)
block|{
name|assignmentManagerSource
operator|.
name|setRIT
argument_list|(
name|ritCount
argument_list|)
expr_stmt|;
block|}
comment|/**    * update RIT count that are in this state for more than the threshold    * as defined by the property rit.metrics.threshold.time.    * @param ritCountOverThreshold    */
specifier|public
name|void
name|updateRITCountOverThreshold
parameter_list|(
specifier|final
name|int
name|ritCountOverThreshold
parameter_list|)
block|{
name|assignmentManagerSource
operator|.
name|setRITCountOverThreshold
argument_list|(
name|ritCountOverThreshold
argument_list|)
expr_stmt|;
block|}
comment|/**    * update the timestamp for oldest region in transition metrics.    * @param timestamp    */
specifier|public
name|void
name|updateRITOldestAge
parameter_list|(
specifier|final
name|long
name|timestamp
parameter_list|)
block|{
name|assignmentManagerSource
operator|.
name|setRITOldestAge
argument_list|(
name|timestamp
argument_list|)
expr_stmt|;
block|}
comment|/**    * update the duration metrics of region is transition    * @param duration    */
specifier|public
name|void
name|updateRitDuration
parameter_list|(
name|long
name|duration
parameter_list|)
block|{
name|assignmentManagerSource
operator|.
name|updateRitDuration
argument_list|(
name|duration
argument_list|)
expr_stmt|;
block|}
comment|/*    * TODO: Remove. This may not be required as assign and unassign operations are tracked separately    * Increment the count of assignment operation (assign/unassign).    */
specifier|public
name|void
name|incrementOperationCounter
parameter_list|()
block|{
name|assignmentManagerSource
operator|.
name|incrementOperationCounter
argument_list|()
expr_stmt|;
block|}
comment|/**    * @return Set of common metrics for assign procedure    */
specifier|public
name|ProcedureMetrics
name|getAssignProcMetrics
parameter_list|()
block|{
return|return
name|assignProcMetrics
return|;
block|}
comment|/**    * @return Set of common metrics for unassign procedure    */
specifier|public
name|ProcedureMetrics
name|getUnassignProcMetrics
parameter_list|()
block|{
return|return
name|unassignProcMetrics
return|;
block|}
comment|/**    * @return Set of common metrics for split procedure    */
specifier|public
name|ProcedureMetrics
name|getSplitProcMetrics
parameter_list|()
block|{
return|return
name|splitProcMetrics
return|;
block|}
comment|/**    * @return Set of common metrics for merge procedure    */
specifier|public
name|ProcedureMetrics
name|getMergeProcMetrics
parameter_list|()
block|{
return|return
name|mergeProcMetrics
return|;
block|}
block|}
end_class

end_unit

