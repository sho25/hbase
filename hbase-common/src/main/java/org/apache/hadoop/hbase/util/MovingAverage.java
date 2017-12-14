begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
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

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * The purpose of introduction of {@link MovingAverage} mainly is to measure execution time of a  * specific method, which can help us to know its performance fluctuation in response to different  * machine states or situations, better case, then to act accordingly.  *<br>  * In different situation, different {@link MovingAverage} algorithm can be used based on needs.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
specifier|abstract
class|class
name|MovingAverage
parameter_list|<
name|T
parameter_list|>
block|{
specifier|private
specifier|final
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|MovingAverage
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Mark start time of an execution.    * @return time in ns.    */
specifier|protected
name|long
name|start
parameter_list|()
block|{
return|return
name|System
operator|.
name|nanoTime
argument_list|()
return|;
block|}
comment|/**    * Mark end time of an execution, and return its interval.    * @param startTime start time of an execution    * @return elapsed time    */
specifier|protected
name|long
name|stop
parameter_list|(
name|long
name|startTime
parameter_list|)
block|{
return|return
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|startTime
return|;
block|}
comment|/**    * Measure elapsed time of a measurable method.    * @param measurable method implements {@link TimeMeasurable}    * @return T it refers to the original return type of the measurable method    */
specifier|public
name|T
name|measure
parameter_list|(
name|TimeMeasurable
argument_list|<
name|T
argument_list|>
name|measurable
parameter_list|)
block|{
name|long
name|startTime
init|=
name|start
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Start to measure at: {} ns."
argument_list|,
name|startTime
argument_list|)
expr_stmt|;
comment|// Here may throw exceptions which should be taken care by caller, not here.
comment|// If exception occurs, this time wouldn't count.
name|T
name|result
init|=
name|measurable
operator|.
name|measure
argument_list|()
decl_stmt|;
name|long
name|elapsed
init|=
name|stop
argument_list|(
name|startTime
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Elapse: {} ns."
argument_list|,
name|elapsed
argument_list|)
expr_stmt|;
name|updateMostRecentTime
argument_list|(
name|elapsed
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * Update the most recent data.    * @param elapsed elapsed time of the most recent measurement    */
specifier|protected
specifier|abstract
name|void
name|updateMostRecentTime
parameter_list|(
name|long
name|elapsed
parameter_list|)
function_decl|;
comment|/**    * Get average execution time of the measured method.    * @return average time in ns    */
specifier|public
specifier|abstract
name|double
name|getAverageTime
parameter_list|()
function_decl|;
block|}
end_class

end_unit

