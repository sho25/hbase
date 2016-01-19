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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|RegionLoad
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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

begin_comment
comment|/**  * Simple implementation of region normalizer.  *  * Logic in use:  *  *<ol>  *<li> get all regions of a given table  *<li> get avg size S of each region (by total size of store files reported in RegionLoad)  *<li> If biggest region is bigger than S * 2, it is kindly requested to split,  *    and normalization stops  *<li> Otherwise, two smallest region R1 and its smallest neighbor R2 are kindly requested  *    to merge, if R1 + R1&lt;  S, and normalization stops  *<li> Otherwise, no action is performed  *</ol>  *<p>  * Region sizes are coarse and approximate on the order of megabytes. Additionally,  * "empty" regions (less than 1MB, with the previous note) are not merged away. This  * is by design to prevent normalization from undoing the pre-splitting of a table.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SimpleRegionNormalizer
implements|implements
name|RegionNormalizer
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SimpleRegionNormalizer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MIN_REGION_COUNT
init|=
literal|3
decl_stmt|;
specifier|private
name|MasterServices
name|masterServices
decl_stmt|;
specifier|private
specifier|static
name|long
index|[]
name|skippedCount
init|=
operator|new
name|long
index|[
name|NormalizationPlan
operator|.
name|PlanType
operator|.
name|values
argument_list|()
operator|.
name|length
index|]
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
name|planSkipped
parameter_list|(
name|HRegionInfo
name|hri
parameter_list|,
name|PlanType
name|type
parameter_list|)
block|{
name|skippedCount
index|[
name|type
operator|.
name|ordinal
argument_list|()
index|]
operator|++
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getSkippedCount
parameter_list|(
name|NormalizationPlan
operator|.
name|PlanType
name|type
parameter_list|)
block|{
return|return
name|skippedCount
index|[
name|type
operator|.
name|ordinal
argument_list|()
index|]
return|;
block|}
comment|// Comparator that gives higher priority to region Split plan
specifier|private
name|Comparator
argument_list|<
name|NormalizationPlan
argument_list|>
name|planComparator
init|=
operator|new
name|Comparator
argument_list|<
name|NormalizationPlan
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|NormalizationPlan
name|plan
parameter_list|,
name|NormalizationPlan
name|plan2
parameter_list|)
block|{
if|if
condition|(
name|plan
operator|instanceof
name|SplitNormalizationPlan
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|plan2
operator|instanceof
name|SplitNormalizationPlan
condition|)
block|{
return|return
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
block|}
decl_stmt|;
comment|/**    * Computes next most "urgent" normalization action on the table.    * Action may be either a split, or a merge, or no action.    *    * @param table table to normalize    * @return normalization plan to execute    */
annotation|@
name|Override
specifier|public
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
block|{
if|if
condition|(
name|table
operator|==
literal|null
operator|||
name|table
operator|.
name|isSystemTable
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Normalization of system table "
operator|+
name|table
operator|+
literal|" isn't allowed"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|NormalizationPlan
argument_list|>
name|plans
init|=
operator|new
name|ArrayList
argument_list|<
name|NormalizationPlan
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|tableRegions
init|=
name|masterServices
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionsOfTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
comment|//TODO: should we make min number of regions a config param?
if|if
condition|(
name|tableRegions
operator|==
literal|null
operator|||
name|tableRegions
operator|.
name|size
argument_list|()
operator|<
name|MIN_REGION_COUNT
condition|)
block|{
name|int
name|nrRegions
init|=
name|tableRegions
operator|==
literal|null
condition|?
literal|0
else|:
name|tableRegions
operator|.
name|size
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table "
operator|+
name|table
operator|+
literal|" has "
operator|+
name|nrRegions
operator|+
literal|" regions, required min number"
operator|+
literal|" of regions for normalizer to run is "
operator|+
name|MIN_REGION_COUNT
operator|+
literal|", not running normalizer"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Computing normalization plan for table: "
operator|+
name|table
operator|+
literal|", number of regions: "
operator|+
name|tableRegions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|totalSizeMb
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|tableRegions
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|HRegionInfo
name|hri
init|=
name|tableRegions
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|long
name|regionSize
init|=
name|getRegionSize
argument_list|(
name|hri
argument_list|)
decl_stmt|;
name|totalSizeMb
operator|+=
name|regionSize
expr_stmt|;
block|}
name|double
name|avgRegionSize
init|=
name|totalSizeMb
operator|/
operator|(
name|double
operator|)
name|tableRegions
operator|.
name|size
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table "
operator|+
name|table
operator|+
literal|", total aggregated regions size: "
operator|+
name|totalSizeMb
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table "
operator|+
name|table
operator|+
literal|", average region size: "
operator|+
name|avgRegionSize
argument_list|)
expr_stmt|;
name|int
name|candidateIdx
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|candidateIdx
operator|<
name|tableRegions
operator|.
name|size
argument_list|()
condition|)
block|{
name|HRegionInfo
name|hri
init|=
name|tableRegions
operator|.
name|get
argument_list|(
name|candidateIdx
argument_list|)
decl_stmt|;
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
name|debug
argument_list|(
literal|"Table "
operator|+
name|table
operator|+
literal|", large region "
operator|+
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" has size "
operator|+
name|regionSize
operator|+
literal|", more than twice avg size, splitting"
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
else|else
block|{
if|if
condition|(
name|candidateIdx
operator|==
name|tableRegions
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|)
block|{
break|break;
block|}
name|HRegionInfo
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
operator|+
name|regionSize2
operator|<
name|avgRegionSize
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table "
operator|+
name|table
operator|+
literal|", small region size: "
operator|+
name|regionSize
operator|+
literal|" plus its neighbor size: "
operator|+
name|regionSize2
operator|+
literal|", less than the avg size "
operator|+
name|avgRegionSize
operator|+
literal|", merging them"
argument_list|)
expr_stmt|;
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
block|}
name|candidateIdx
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|plans
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"No normalization needed, regions look good for table: "
operator|+
name|table
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|plans
argument_list|,
name|planComparator
argument_list|)
expr_stmt|;
return|return
name|plans
return|;
block|}
specifier|private
name|long
name|getRegionSize
parameter_list|(
name|HRegionInfo
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
name|RegionLoad
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
name|getRegionsLoad
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
return|return
name|regionLoad
operator|.
name|getStorefileSizeMB
argument_list|()
return|;
block|}
block|}
end_class

end_unit

