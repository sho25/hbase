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
name|replication
operator|.
name|regionserver
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|CompatibilitySingletonFactory
import|;
end_import

begin_comment
comment|/**  * This class is for maintaining the various replication statistics for a sink and publishing them  * through the metrics interfaces.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsSink
block|{
specifier|private
name|long
name|lastTimestampForAge
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|MetricsReplicationSinkSource
name|mss
decl_stmt|;
specifier|public
name|MetricsSink
parameter_list|()
block|{
name|mss
operator|=
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsReplicationSourceFactory
operator|.
name|class
argument_list|)
operator|.
name|getSink
argument_list|()
expr_stmt|;
block|}
comment|/**    * Set the age of the last applied operation    *    * @param timestamp The timestamp of the last operation applied.    * @return the age that was set    */
specifier|public
name|long
name|setAgeOfLastAppliedOp
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
name|long
name|age
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|lastTimestampForAge
operator|!=
name|timestamp
condition|)
block|{
name|lastTimestampForAge
operator|=
name|timestamp
expr_stmt|;
name|age
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|lastTimestampForAge
expr_stmt|;
block|}
name|mss
operator|.
name|setLastAppliedOpAge
argument_list|(
name|age
argument_list|)
expr_stmt|;
return|return
name|age
return|;
block|}
comment|/**    * Refreshing the age makes sure the value returned is the actual one and    * not the one set a replication time    * @return refreshed age    */
specifier|public
name|long
name|refreshAgeOfLastAppliedOp
parameter_list|()
block|{
return|return
name|setAgeOfLastAppliedOp
argument_list|(
name|lastTimestampForAge
argument_list|)
return|;
block|}
comment|/**    * Convience method to change metrics when a batch of operations are applied.    *    * @param batchSize    */
specifier|public
name|void
name|applyBatch
parameter_list|(
name|long
name|batchSize
parameter_list|)
block|{
name|mss
operator|.
name|incrAppliedBatches
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|mss
operator|.
name|incrAppliedOps
argument_list|(
name|batchSize
argument_list|)
expr_stmt|;
block|}
comment|/**    * Convience method to change metrics when a batch of operations are applied.    *    * @param batchSize total number of mutations that are applied/replicated    * @param hfileSize total number of hfiles that are applied/replicated    */
specifier|public
name|void
name|applyBatch
parameter_list|(
name|long
name|batchSize
parameter_list|,
name|long
name|hfileSize
parameter_list|)
block|{
name|applyBatch
argument_list|(
name|batchSize
argument_list|)
expr_stmt|;
name|mss
operator|.
name|incrAppliedHFiles
argument_list|(
name|hfileSize
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the Age of Last Applied Op    * @return ageOfLastAppliedOp    */
specifier|public
name|long
name|getAgeOfLastAppliedOp
parameter_list|()
block|{
return|return
name|mss
operator|.
name|getLastAppliedOpAge
argument_list|()
return|;
block|}
comment|/**    * Get the TimestampOfLastAppliedOp. If no replication Op applied yet, the value is the timestamp    * at which hbase instance starts    * @return timeStampsOfLastAppliedOp;    */
specifier|public
name|long
name|getTimestampOfLastAppliedOp
parameter_list|()
block|{
return|return
name|this
operator|.
name|lastTimestampForAge
return|;
block|}
block|}
end_class

end_unit

