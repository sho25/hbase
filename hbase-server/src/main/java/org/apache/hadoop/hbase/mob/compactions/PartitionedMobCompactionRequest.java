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
name|fs
operator|.
name|Path
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
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|StoreFile
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
name|Bytes
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
name|List
argument_list|<
name|CompactionDelPartition
argument_list|>
name|delPartitions
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
name|List
argument_list|<
name|CompactionDelPartition
argument_list|>
name|delPartitions
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
name|delPartitions
operator|=
name|delPartitions
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
name|List
argument_list|<
name|CompactionDelPartition
argument_list|>
name|getDelPartitions
parameter_list|()
block|{
return|return
name|this
operator|.
name|delPartitions
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
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|CompactionPartitionId
name|partitionId
decl_stmt|;
comment|// The startKey and endKey of this partition, both are inclusive.
specifier|private
name|byte
index|[]
name|startKey
decl_stmt|;
specifier|private
name|byte
index|[]
name|endKey
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
specifier|public
name|int
name|getFileCount
parameter_list|()
block|{
return|return
name|files
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|byte
index|[]
name|getStartKey
parameter_list|()
block|{
return|return
name|startKey
return|;
block|}
comment|/**      * Set start key of this partition, only if the input startKey is less than      * the current start key.      */
specifier|public
name|void
name|setStartKey
parameter_list|(
specifier|final
name|byte
index|[]
name|startKey
parameter_list|)
block|{
if|if
condition|(
operator|(
name|this
operator|.
name|startKey
operator|==
literal|null
operator|)
operator|||
operator|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|startKey
argument_list|,
name|this
operator|.
name|startKey
argument_list|)
operator|<
literal|0
operator|)
condition|)
block|{
name|this
operator|.
name|startKey
operator|=
name|startKey
expr_stmt|;
block|}
block|}
specifier|public
name|byte
index|[]
name|getEndKey
parameter_list|()
block|{
return|return
name|endKey
return|;
block|}
comment|/**      * Set end key of this partition, only if the input endKey is greater than      * the current end key.      */
specifier|public
name|void
name|setEndKey
parameter_list|(
specifier|final
name|byte
index|[]
name|endKey
parameter_list|)
block|{
if|if
condition|(
operator|(
name|this
operator|.
name|endKey
operator|==
literal|null
operator|)
operator|||
operator|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|endKey
argument_list|,
name|this
operator|.
name|endKey
argument_list|)
operator|>
literal|0
operator|)
condition|)
block|{
name|this
operator|.
name|endKey
operator|=
name|endKey
expr_stmt|;
block|}
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
specifier|private
name|String
name|latestDate
decl_stmt|;
specifier|private
name|long
name|threshold
decl_stmt|;
specifier|public
name|CompactionPartitionId
parameter_list|()
block|{
comment|// initialize these fields to empty string
name|this
operator|.
name|startKey
operator|=
literal|""
expr_stmt|;
name|this
operator|.
name|date
operator|=
literal|""
expr_stmt|;
name|this
operator|.
name|latestDate
operator|=
literal|""
expr_stmt|;
name|this
operator|.
name|threshold
operator|=
literal|0
expr_stmt|;
block|}
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
name|this
operator|.
name|latestDate
operator|=
literal|""
expr_stmt|;
name|this
operator|.
name|threshold
operator|=
literal|0
expr_stmt|;
block|}
specifier|public
name|void
name|setThreshold
parameter_list|(
specifier|final
name|long
name|threshold
parameter_list|)
block|{
name|this
operator|.
name|threshold
operator|=
name|threshold
expr_stmt|;
block|}
specifier|public
name|long
name|getThreshold
parameter_list|()
block|{
return|return
name|this
operator|.
name|threshold
return|;
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
name|void
name|setStartKey
parameter_list|(
specifier|final
name|String
name|startKey
parameter_list|)
block|{
name|this
operator|.
name|startKey
operator|=
name|startKey
expr_stmt|;
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
specifier|public
name|void
name|setDate
parameter_list|(
specifier|final
name|String
name|date
parameter_list|)
block|{
name|this
operator|.
name|date
operator|=
name|date
expr_stmt|;
block|}
specifier|public
name|String
name|getLatestDate
parameter_list|()
block|{
return|return
name|this
operator|.
name|latestDate
return|;
block|}
specifier|public
name|void
name|updateLatestDate
parameter_list|(
specifier|final
name|String
name|latestDate
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|latestDate
operator|.
name|compareTo
argument_list|(
name|latestDate
argument_list|)
operator|<
literal|0
condition|)
block|{
name|this
operator|.
name|latestDate
operator|=
name|latestDate
expr_stmt|;
block|}
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
comment|/**    * The delete file partition in the mob compaction.    * The delete partition is defined as [startKey, endKey] pair.    * The mob delete files that have the same start key and end key belong to    * the same partition.    */
specifier|protected
specifier|static
class|class
name|CompactionDelPartition
block|{
specifier|private
name|List
argument_list|<
name|Path
argument_list|>
name|delFiles
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|List
argument_list|<
name|StoreFile
argument_list|>
name|storeFiles
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|CompactionDelPartitionId
name|id
decl_stmt|;
specifier|public
name|CompactionDelPartition
parameter_list|(
name|CompactionDelPartitionId
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
specifier|public
name|CompactionDelPartitionId
name|getId
parameter_list|()
block|{
return|return
name|this
operator|.
name|id
return|;
block|}
name|void
name|addDelFile
parameter_list|(
name|FileStatus
name|file
parameter_list|)
block|{
name|delFiles
operator|.
name|add
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addStoreFile
parameter_list|(
specifier|final
name|StoreFile
name|file
parameter_list|)
block|{
name|storeFiles
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
name|StoreFile
argument_list|>
name|getStoreFiles
parameter_list|()
block|{
return|return
name|storeFiles
return|;
block|}
name|List
argument_list|<
name|Path
argument_list|>
name|listDelFiles
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|delFiles
argument_list|)
return|;
block|}
name|void
name|addDelFileList
parameter_list|(
specifier|final
name|Collection
argument_list|<
name|Path
argument_list|>
name|list
parameter_list|)
block|{
name|delFiles
operator|.
name|addAll
argument_list|(
name|list
argument_list|)
expr_stmt|;
block|}
name|int
name|getDelFileCount
parameter_list|()
block|{
return|return
name|delFiles
operator|.
name|size
argument_list|()
return|;
block|}
name|void
name|cleanDelFiles
parameter_list|()
block|{
name|delFiles
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * The delete partition id that consists of start key and end key    */
specifier|public
specifier|static
class|class
name|CompactionDelPartitionId
implements|implements
name|Comparable
argument_list|<
name|CompactionDelPartitionId
argument_list|>
block|{
specifier|private
name|byte
index|[]
name|startKey
decl_stmt|;
specifier|private
name|byte
index|[]
name|endKey
decl_stmt|;
specifier|public
name|CompactionDelPartitionId
parameter_list|()
block|{     }
specifier|public
name|CompactionDelPartitionId
parameter_list|(
specifier|final
name|byte
index|[]
name|startKey
parameter_list|,
specifier|final
name|byte
index|[]
name|endKey
parameter_list|)
block|{
name|this
operator|.
name|startKey
operator|=
name|startKey
expr_stmt|;
name|this
operator|.
name|endKey
operator|=
name|endKey
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
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
name|void
name|setStartKey
parameter_list|(
specifier|final
name|byte
index|[]
name|startKey
parameter_list|)
block|{
name|this
operator|.
name|startKey
operator|=
name|startKey
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getEndKey
parameter_list|()
block|{
return|return
name|this
operator|.
name|endKey
return|;
block|}
specifier|public
name|void
name|setEndKey
parameter_list|(
specifier|final
name|byte
index|[]
name|endKey
parameter_list|)
block|{
name|this
operator|.
name|endKey
operator|=
name|endKey
expr_stmt|;
block|}
specifier|public
name|int
name|compareTo
parameter_list|(
name|CompactionDelPartitionId
name|o
parameter_list|)
block|{
comment|/*        * 1). Compare the start key, if the k1< k2, then k1 is less        * 2). If start Key is same, check endKey, k1< k2, k1 is less        *     If both are same, then they are equal.        */
name|int
name|result
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|this
operator|.
name|startKey
argument_list|,
name|o
operator|.
name|getStartKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
block|{
return|return
name|result
return|;
block|}
return|return
name|Bytes
operator|.
name|compareTo
argument_list|(
name|this
operator|.
name|endKey
argument_list|,
name|o
operator|.
name|getEndKey
argument_list|()
argument_list|)
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
name|java
operator|.
name|util
operator|.
name|Arrays
operator|.
name|hashCode
argument_list|(
name|startKey
argument_list|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|java
operator|.
name|util
operator|.
name|Arrays
operator|.
name|hashCode
argument_list|(
name|endKey
argument_list|)
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
name|CompactionDelPartitionId
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|CompactionDelPartitionId
name|another
init|=
operator|(
name|CompactionDelPartitionId
operator|)
name|obj
decl_stmt|;
return|return
operator|(
name|this
operator|.
name|compareTo
argument_list|(
name|another
argument_list|)
operator|==
literal|0
operator|)
return|;
block|}
block|}
block|}
end_class

end_unit

