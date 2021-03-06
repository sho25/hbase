begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|IOException
import|;
end_import

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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
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
name|RegionMetrics
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
name|ServerName
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
name|Size
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
name|client
operator|.
name|MasterSwitchType
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
name|client
operator|.
name|RegionInfo
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
name|client
operator|.
name|TableDescriptor
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
name|MasterRpcServices
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
name|RegionState
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
name|assignment
operator|.
name|RegionStates
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
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
name|shaded
operator|.
name|protobuf
operator|.
name|RequestConverter
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|AbstractRegionNormalizer
implements|implements
name|RegionNormalizer
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|AbstractRegionNormalizer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|MasterServices
name|masterServices
decl_stmt|;
specifier|protected
name|MasterRpcServices
name|masterRpcServices
decl_stmt|;
comment|/**    * Set the master service.    * @param masterServices inject instance of MasterServices    */
annotation|@
name|Override
specifier|public
name|void
name|setMasterServices
parameter_list|(
name|MasterServices
name|masterServices
parameter_list|)
block|{
name|this
operator|.
name|masterServices
operator|=
name|masterServices
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setMasterRpcServices
parameter_list|(
name|MasterRpcServices
name|masterRpcServices
parameter_list|)
block|{
name|this
operator|.
name|masterRpcServices
operator|=
name|masterRpcServices
expr_stmt|;
block|}
comment|/**    * @param hri regioninfo    * @return size of region in MB and if region is not found than -1    */
specifier|protected
name|long
name|getRegionSize
parameter_list|(
name|RegionInfo
name|hri
parameter_list|)
block|{
name|ServerName
name|sn
init|=
name|masterServices
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionServerOfRegion
argument_list|(
name|hri
argument_list|)
decl_stmt|;
name|RegionMetrics
name|regionLoad
init|=
name|masterServices
operator|.
name|getServerManager
argument_list|()
operator|.
name|getLoad
argument_list|(
name|sn
argument_list|)
operator|.
name|getRegionMetrics
argument_list|()
operator|.
name|get
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|regionLoad
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"{} was not found in RegionsLoad"
argument_list|,
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|-
literal|1
return|;
block|}
return|return
operator|(
name|long
operator|)
name|regionLoad
operator|.
name|getStoreFileSize
argument_list|()
operator|.
name|get
argument_list|(
name|Size
operator|.
name|Unit
operator|.
name|MEGABYTE
argument_list|)
return|;
block|}
specifier|protected
name|boolean
name|isMergeEnabled
parameter_list|()
block|{
name|boolean
name|mergeEnabled
init|=
literal|true
decl_stmt|;
try|try
block|{
name|mergeEnabled
operator|=
name|masterRpcServices
operator|.
name|isSplitOrMergeEnabled
argument_list|(
literal|null
argument_list|,
name|RequestConverter
operator|.
name|buildIsSplitOrMergeEnabledRequest
argument_list|(
name|MasterSwitchType
operator|.
name|MERGE
argument_list|)
argument_list|)
operator|.
name|getEnabled
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to determine whether merge is enabled"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|mergeEnabled
return|;
block|}
specifier|protected
name|boolean
name|isSplitEnabled
parameter_list|()
block|{
name|boolean
name|splitEnabled
init|=
literal|true
decl_stmt|;
try|try
block|{
name|splitEnabled
operator|=
name|masterRpcServices
operator|.
name|isSplitOrMergeEnabled
argument_list|(
literal|null
argument_list|,
name|RequestConverter
operator|.
name|buildIsSplitOrMergeEnabledRequest
argument_list|(
name|MasterSwitchType
operator|.
name|SPLIT
argument_list|)
argument_list|)
operator|.
name|getEnabled
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|se
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to determine whether split is enabled"
argument_list|,
name|se
argument_list|)
expr_stmt|;
block|}
return|return
name|splitEnabled
return|;
block|}
comment|/**    * @param tableRegions regions of table to normalize    * @return average region size depending on    * @see org.apache.hadoop.hbase.client.TableDescriptor#getNormalizerTargetRegionCount()    * Also make sure tableRegions contains regions of the same table    */
specifier|protected
name|double
name|getAverageRegionSize
parameter_list|(
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|tableRegions
parameter_list|)
block|{
name|long
name|totalSizeMb
init|=
literal|0
decl_stmt|;
name|int
name|actualRegionCnt
init|=
literal|0
decl_stmt|;
for|for
control|(
name|RegionInfo
name|hri
range|:
name|tableRegions
control|)
block|{
name|long
name|regionSize
init|=
name|getRegionSize
argument_list|(
name|hri
argument_list|)
decl_stmt|;
comment|// don't consider regions that are in bytes for averaging the size.
if|if
condition|(
name|regionSize
operator|>
literal|0
condition|)
block|{
name|actualRegionCnt
operator|++
expr_stmt|;
name|totalSizeMb
operator|+=
name|regionSize
expr_stmt|;
block|}
block|}
name|TableName
name|table
init|=
name|tableRegions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTable
argument_list|()
decl_stmt|;
name|int
name|targetRegionCount
init|=
operator|-
literal|1
decl_stmt|;
name|long
name|targetRegionSize
init|=
operator|-
literal|1
decl_stmt|;
try|try
block|{
name|TableDescriptor
name|tableDescriptor
init|=
name|masterServices
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|get
argument_list|(
name|table
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableDescriptor
operator|!=
literal|null
condition|)
block|{
name|targetRegionCount
operator|=
name|tableDescriptor
operator|.
name|getNormalizerTargetRegionCount
argument_list|()
expr_stmt|;
name|targetRegionSize
operator|=
name|tableDescriptor
operator|.
name|getNormalizerTargetRegionSize
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table {}:  target region count is {}, target region size is {}"
argument_list|,
name|table
argument_list|,
name|targetRegionCount
argument_list|,
name|targetRegionSize
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"cannot get the target number and target size of table {}, they will be default value -1."
argument_list|,
name|table
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|double
name|avgRegionSize
decl_stmt|;
if|if
condition|(
name|targetRegionSize
operator|>
literal|0
condition|)
block|{
name|avgRegionSize
operator|=
name|targetRegionSize
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|targetRegionCount
operator|>
literal|0
condition|)
block|{
name|avgRegionSize
operator|=
name|totalSizeMb
operator|/
operator|(
name|double
operator|)
name|targetRegionCount
expr_stmt|;
block|}
else|else
block|{
name|avgRegionSize
operator|=
name|actualRegionCnt
operator|==
literal|0
condition|?
literal|0
else|:
name|totalSizeMb
operator|/
operator|(
name|double
operator|)
name|actualRegionCnt
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table {}, total aggregated regions size: {} and average region size {}"
argument_list|,
name|table
argument_list|,
name|totalSizeMb
argument_list|,
name|avgRegionSize
argument_list|)
expr_stmt|;
return|return
name|avgRegionSize
return|;
block|}
comment|/**    * Determine if a region in {@link RegionState} should be considered for a merge operation.    */
specifier|private
specifier|static
name|boolean
name|skipForMerge
parameter_list|(
specifier|final
name|RegionState
name|state
parameter_list|)
block|{
return|return
name|state
operator|==
literal|null
operator|||
operator|!
name|Objects
operator|.
name|equals
argument_list|(
name|state
operator|.
name|getState
argument_list|()
argument_list|,
name|RegionState
operator|.
name|State
operator|.
name|OPEN
argument_list|)
return|;
block|}
comment|/**    * Computes the merge plans that should be executed for this table to converge average region    * towards target average or target region count    * @param table table to normalize    * @return list of merge normalization plans    */
specifier|protected
name|List
argument_list|<
name|NormalizationPlan
argument_list|>
name|getMergeNormalizationPlan
parameter_list|(
name|TableName
name|table
parameter_list|)
block|{
specifier|final
name|RegionStates
name|regionStates
init|=
name|masterServices
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|tableRegions
init|=
name|regionStates
operator|.
name|getRegionsOfTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
specifier|final
name|double
name|avgRegionSize
init|=
name|getAverageRegionSize
argument_list|(
name|tableRegions
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table {}, average region size: {}. Computing normalization plan for table: {}, "
operator|+
literal|"number of regions: {}"
argument_list|,
name|table
argument_list|,
name|avgRegionSize
argument_list|,
name|table
argument_list|,
name|tableRegions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|List
argument_list|<
name|NormalizationPlan
argument_list|>
name|plans
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|candidateIdx
init|=
literal|0
init|;
name|candidateIdx
operator|<
name|tableRegions
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|;
name|candidateIdx
operator|++
control|)
block|{
specifier|final
name|RegionInfo
name|hri
init|=
name|tableRegions
operator|.
name|get
argument_list|(
name|candidateIdx
argument_list|)
decl_stmt|;
specifier|final
name|RegionInfo
name|hri2
init|=
name|tableRegions
operator|.
name|get
argument_list|(
name|candidateIdx
operator|+
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|skipForMerge
argument_list|(
name|regionStates
operator|.
name|getRegionState
argument_list|(
name|hri
argument_list|)
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|skipForMerge
argument_list|(
name|regionStates
operator|.
name|getRegionState
argument_list|(
name|hri2
argument_list|)
argument_list|)
condition|)
block|{
continue|continue;
block|}
specifier|final
name|long
name|regionSize
init|=
name|getRegionSize
argument_list|(
name|hri
argument_list|)
decl_stmt|;
specifier|final
name|long
name|regionSize2
init|=
name|getRegionSize
argument_list|(
name|hri2
argument_list|)
decl_stmt|;
if|if
condition|(
name|regionSize
operator|>=
literal|0
operator|&&
name|regionSize2
operator|>=
literal|0
operator|&&
name|regionSize
operator|+
name|regionSize2
operator|<
name|avgRegionSize
condition|)
block|{
comment|// at least one of the two regions should be older than MIN_REGION_DURATION days
name|plans
operator|.
name|add
argument_list|(
operator|new
name|MergeNormalizationPlan
argument_list|(
name|hri
argument_list|,
name|hri2
argument_list|)
argument_list|)
expr_stmt|;
name|candidateIdx
operator|++
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Skipping region {} of table {} with size {}"
argument_list|,
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|table
argument_list|,
name|regionSize
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|plans
return|;
block|}
comment|/**    * Determine if a region in {@link RegionState} should be considered for a split operation.    */
specifier|private
specifier|static
name|boolean
name|skipForSplit
parameter_list|(
specifier|final
name|RegionState
name|state
parameter_list|)
block|{
return|return
name|state
operator|==
literal|null
operator|||
operator|!
name|Objects
operator|.
name|equals
argument_list|(
name|state
operator|.
name|getState
argument_list|()
argument_list|,
name|RegionState
operator|.
name|State
operator|.
name|OPEN
argument_list|)
return|;
block|}
comment|/**    * Computes the split plans that should be executed for this table to converge average region size    * towards target average or target region count    * @param table table to normalize    * @return list of split normalization plans    */
specifier|protected
name|List
argument_list|<
name|NormalizationPlan
argument_list|>
name|getSplitNormalizationPlan
parameter_list|(
name|TableName
name|table
parameter_list|)
block|{
specifier|final
name|RegionStates
name|regionStates
init|=
name|masterServices
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|tableRegions
init|=
name|regionStates
operator|.
name|getRegionsOfTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
specifier|final
name|double
name|avgRegionSize
init|=
name|getAverageRegionSize
argument_list|(
name|tableRegions
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table {}, average region size: {}"
argument_list|,
name|table
argument_list|,
name|avgRegionSize
argument_list|)
expr_stmt|;
specifier|final
name|List
argument_list|<
name|NormalizationPlan
argument_list|>
name|plans
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|RegionInfo
name|hri
range|:
name|tableRegions
control|)
block|{
if|if
condition|(
name|skipForSplit
argument_list|(
name|regionStates
operator|.
name|getRegionState
argument_list|(
name|hri
argument_list|)
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|long
name|regionSize
init|=
name|getRegionSize
argument_list|(
name|hri
argument_list|)
decl_stmt|;
comment|// if the region is> 2 times larger than average, we split it, split
comment|// is more high priority normalization action than merge.
if|if
condition|(
name|regionSize
operator|>
literal|2
operator|*
name|avgRegionSize
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Table {}, large region {} has size {}, more than twice avg size {}, splitting"
argument_list|,
name|table
argument_list|,
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|regionSize
argument_list|,
name|avgRegionSize
argument_list|)
expr_stmt|;
name|plans
operator|.
name|add
argument_list|(
operator|new
name|SplitNormalizationPlan
argument_list|(
name|hri
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|plans
return|;
block|}
block|}
end_class

end_unit

