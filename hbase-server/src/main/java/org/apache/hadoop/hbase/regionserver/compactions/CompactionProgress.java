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
name|regionserver
operator|.
name|compactions
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * This class holds information relevant for tracking the progress of a  * compaction.  *  *<p>The metrics tracked allow one to calculate the percent completion of the  * compaction based on the number of Key/Value pairs already compacted vs.  * total amount scheduled to be compacted.  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CompactionProgress
block|{
comment|/** the total compacting key values in currently running compaction */
specifier|public
name|long
name|totalCompactingKVs
decl_stmt|;
comment|/** the completed count of key values in currently running compaction */
specifier|public
name|long
name|currentCompactedKVs
init|=
literal|0
decl_stmt|;
comment|/** the total size of data processed by the currently running compaction, in bytes */
specifier|public
name|long
name|totalCompactedSize
init|=
literal|0
decl_stmt|;
comment|/** Constructor    * @param totalCompactingKVs the total Key/Value pairs to be compacted    */
specifier|public
name|CompactionProgress
parameter_list|(
name|long
name|totalCompactingKVs
parameter_list|)
block|{
name|this
operator|.
name|totalCompactingKVs
operator|=
name|totalCompactingKVs
expr_stmt|;
block|}
comment|/** getter for calculated percent complete    * @return float    */
specifier|public
name|float
name|getProgressPct
parameter_list|()
block|{
return|return
operator|(
name|float
operator|)
name|currentCompactedKVs
operator|/
name|totalCompactingKVs
return|;
block|}
comment|/**    * Cancels the compaction progress, setting things to 0.    */
specifier|public
name|void
name|cancel
parameter_list|()
block|{
name|this
operator|.
name|currentCompactedKVs
operator|=
name|this
operator|.
name|totalCompactingKVs
operator|=
literal|0
expr_stmt|;
block|}
comment|/**    * Marks the compaction as complete by setting total to current KV count;    * Total KV count is an estimate, so there might be a discrepancy otherwise.    */
specifier|public
name|void
name|complete
parameter_list|()
block|{
name|this
operator|.
name|totalCompactingKVs
operator|=
name|this
operator|.
name|currentCompactedKVs
expr_stmt|;
block|}
comment|/**    * @return the total compacting key values in currently running compaction    */
specifier|public
name|long
name|getTotalCompactingKvs
parameter_list|()
block|{
return|return
name|totalCompactingKVs
return|;
block|}
comment|/**    * @return the completed count of key values in currently running compaction    */
specifier|public
name|long
name|getCurrentCompactedKvs
parameter_list|()
block|{
return|return
name|currentCompactedKVs
return|;
block|}
comment|/**    * @return the total data size processed by the currently running compaction, in bytes    */
specifier|public
name|long
name|getTotalCompactedSize
parameter_list|()
block|{
return|return
name|totalCompactedSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%d/%d (%.2f%%)"
argument_list|,
name|currentCompactedKVs
argument_list|,
name|totalCompactingKVs
argument_list|,
literal|100
operator|*
name|getProgressPct
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

