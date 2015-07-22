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
name|mob
operator|.
name|compactions
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

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
name|fs
operator|.
name|FileStatus
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
name|util
operator|.
name|EnvironmentEdgeManager
import|;
end_import

begin_comment
comment|/**  * An implementation of {@link MobCompactionRequest} that is used in  * {@link PartitionedMobCompactor}.  * The mob files that have the same start key and date in their names belong to  * the same partition.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|PartitionedMobCompactionRequest
extends|extends
name|MobCompactionRequest
block|{
specifier|protected
name|Collection
argument_list|<
name|FileStatus
argument_list|>
name|delFiles
decl_stmt|;
specifier|protected
name|Collection
argument_list|<
name|CompactionPartition
argument_list|>
name|compactionPartitions
decl_stmt|;
specifier|public
name|PartitionedMobCompactionRequest
parameter_list|(
name|Collection
argument_list|<
name|CompactionPartition
argument_list|>
name|compactionPartitions
parameter_list|,
name|Collection
argument_list|<
name|FileStatus
argument_list|>
name|delFiles
parameter_list|)
block|{
name|this
operator|.
name|selectionTime
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
expr_stmt|;
name|this
operator|.
name|compactionPartitions
operator|=
name|compactionPartitions
expr_stmt|;
name|this
operator|.
name|delFiles
operator|=
name|delFiles
expr_stmt|;
block|}
comment|/**    * Gets the compaction partitions.    * @return The compaction partitions.    */
specifier|public
name|Collection
argument_list|<
name|CompactionPartition
argument_list|>
name|getCompactionPartitions
parameter_list|()
block|{
return|return
name|this
operator|.
name|compactionPartitions
return|;
block|}
comment|/**    * Gets the del files.    * @return The del files.    */
specifier|public
name|Collection
argument_list|<
name|FileStatus
argument_list|>
name|getDelFiles
parameter_list|()
block|{
return|return
name|this
operator|.
name|delFiles
return|;
block|}
comment|/**    * The partition in the mob compaction.    * The mob files that have the same start key and date in their names belong to    * the same partition.    */
specifier|protected
specifier|static
class|class
name|CompactionPartition
block|{
specifier|private
name|List
argument_list|<
name|FileStatus
argument_list|>
name|files
init|=
operator|new
name|ArrayList
argument_list|<
name|FileStatus
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|CompactionPartitionId
name|partitionId
decl_stmt|;
specifier|public
name|CompactionPartition
parameter_list|(
name|CompactionPartitionId
name|partitionId
parameter_list|)
block|{
name|this
operator|.
name|partitionId
operator|=
name|partitionId
expr_stmt|;
block|}
specifier|public
name|CompactionPartitionId
name|getPartitionId
parameter_list|()
block|{
return|return
name|this
operator|.
name|partitionId
return|;
block|}
specifier|public
name|void
name|addFile
parameter_list|(
name|FileStatus
name|file
parameter_list|)
block|{
name|files
operator|.
name|add
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|FileStatus
argument_list|>
name|listFiles
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|files
argument_list|)
return|;
block|}
block|}
comment|/**    * The partition id that consists of start key and date of the mob file name.    */
specifier|public
specifier|static
class|class
name|CompactionPartitionId
block|{
specifier|private
name|String
name|startKey
decl_stmt|;
specifier|private
name|String
name|date
decl_stmt|;
specifier|public
name|CompactionPartitionId
parameter_list|(
name|String
name|startKey
parameter_list|,
name|String
name|date
parameter_list|)
block|{
if|if
condition|(
name|startKey
operator|==
literal|null
operator|||
name|date
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Neither of start key and date could be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|startKey
operator|=
name|startKey
expr_stmt|;
name|this
operator|.
name|date
operator|=
name|date
expr_stmt|;
block|}
specifier|public
name|String
name|getStartKey
parameter_list|()
block|{
return|return
name|this
operator|.
name|startKey
return|;
block|}
specifier|public
name|String
name|getDate
parameter_list|()
block|{
return|return
name|this
operator|.
name|date
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
literal|17
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|startKey
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|date
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|CompactionPartitionId
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|CompactionPartitionId
name|another
init|=
operator|(
name|CompactionPartitionId
operator|)
name|obj
decl_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|startKey
operator|.
name|equals
argument_list|(
name|another
operator|.
name|startKey
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|this
operator|.
name|date
operator|.
name|equals
argument_list|(
name|another
operator|.
name|date
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
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
operator|new
name|StringBuilder
argument_list|(
name|startKey
argument_list|)
operator|.
name|append
argument_list|(
name|date
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

