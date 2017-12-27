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
name|conf
operator|.
name|Configuration
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
comment|/**  * MemStoreCompactionStrategy is the root of a class hierarchy which defines the strategy for  * choosing the next action to apply in an (in-memory) memstore compaction.  * Possible action are:  *  - No-op - do nothing  *  - Flatten - to change the segment's index from CSLM to a flat representation  *  - Merge - to merge the indices of the segments in the pipeline  *  - Compact - to merge the indices while removing data redundancies  *  * In addition while applying flat/merge actions it is possible to count the number of unique  * keys in the result segment.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|MemStoreCompactionStrategy
block|{
specifier|protected
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|MemStoreCompactionStrategy
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// The upper bound for the number of segments we store in the pipeline prior to merging.
specifier|public
specifier|static
specifier|final
name|String
name|COMPACTING_MEMSTORE_THRESHOLD_KEY
init|=
literal|"hbase.hregion.compacting.pipeline.segments.limit"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|COMPACTING_MEMSTORE_THRESHOLD_DEFAULT
init|=
literal|4
decl_stmt|;
comment|/**    * Types of actions to be done on the pipeline upon MemStoreCompaction invocation.    * Note that every value covers the previous ones, i.e. if MERGE is the action it implies    * that the youngest segment is going to be flatten anyway.    */
specifier|public
enum|enum
name|Action
block|{
name|NOOP
block|,
name|FLATTEN
block|,
comment|// flatten a segment in the pipeline
name|FLATTEN_COUNT_UNIQUE_KEYS
block|,
comment|// flatten a segment in the pipeline and count its unique keys
name|MERGE
block|,
comment|// merge all the segments in the pipeline into one
name|MERGE_COUNT_UNIQUE_KEYS
block|,
comment|// merge all pipeline segments into one and count its unique keys
name|COMPACT
comment|// compact the data of all pipeline segments
block|}
specifier|protected
specifier|final
name|String
name|cfName
decl_stmt|;
comment|// The limit on the number of the segments in the pipeline
specifier|protected
specifier|final
name|int
name|pipelineThreshold
decl_stmt|;
specifier|public
name|MemStoreCompactionStrategy
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|cfName
parameter_list|)
block|{
name|this
operator|.
name|cfName
operator|=
name|cfName
expr_stmt|;
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
block|{
name|pipelineThreshold
operator|=
name|COMPACTING_MEMSTORE_THRESHOLD_DEFAULT
expr_stmt|;
block|}
else|else
block|{
name|pipelineThreshold
operator|=
comment|// get the limit on the number of the segments in the pipeline
name|conf
operator|.
name|getInt
argument_list|(
name|COMPACTING_MEMSTORE_THRESHOLD_KEY
argument_list|,
name|COMPACTING_MEMSTORE_THRESHOLD_DEFAULT
argument_list|)
expr_stmt|;
block|}
block|}
comment|// get next compaction action to apply on compaction pipeline
specifier|public
specifier|abstract
name|Action
name|getAction
parameter_list|(
name|VersionedSegmentsList
name|versionedList
parameter_list|)
function_decl|;
comment|// update policy stats based on the segment that replaced previous versioned list (in
comment|// compaction pipeline)
specifier|public
name|void
name|updateStats
parameter_list|(
name|Segment
name|replacement
parameter_list|)
block|{}
comment|// resets policy stats
specifier|public
name|void
name|resetStats
parameter_list|()
block|{}
specifier|protected
name|Action
name|simpleMergeOrFlatten
parameter_list|(
name|VersionedSegmentsList
name|versionedList
parameter_list|,
name|String
name|strategy
parameter_list|)
block|{
name|int
name|numOfSegments
init|=
name|versionedList
operator|.
name|getNumOfSegments
argument_list|()
decl_stmt|;
if|if
condition|(
name|numOfSegments
operator|>
name|pipelineThreshold
condition|)
block|{
comment|// to avoid too many segments, merge now
name|LOG
operator|.
name|debug
argument_list|(
literal|"{} in-memory compaction of {}; merging {} segments"
argument_list|,
name|strategy
argument_list|,
name|cfName
argument_list|,
name|numOfSegments
argument_list|)
expr_stmt|;
return|return
name|getMergingAction
argument_list|()
return|;
block|}
comment|// just flatten a segment
name|LOG
operator|.
name|debug
argument_list|(
literal|"{} in-memory compaction of {}; flattening a segment"
argument_list|,
name|strategy
argument_list|,
name|cfName
argument_list|)
expr_stmt|;
return|return
name|getFlattenAction
argument_list|()
return|;
block|}
specifier|protected
name|Action
name|getMergingAction
parameter_list|()
block|{
return|return
name|Action
operator|.
name|MERGE
return|;
block|}
specifier|protected
name|Action
name|getFlattenAction
parameter_list|()
block|{
return|return
name|Action
operator|.
name|FLATTEN
return|;
block|}
specifier|protected
name|Action
name|compact
parameter_list|(
name|VersionedSegmentsList
name|versionedList
parameter_list|,
name|String
name|strategyInfo
parameter_list|)
block|{
name|int
name|numOfSegments
init|=
name|versionedList
operator|.
name|getNumOfSegments
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|strategyInfo
operator|+
literal|" memory compaction for store "
operator|+
name|cfName
operator|+
literal|" compacting "
operator|+
name|numOfSegments
operator|+
literal|" segments"
argument_list|)
expr_stmt|;
return|return
name|Action
operator|.
name|COMPACT
return|;
block|}
block|}
end_class

end_unit

