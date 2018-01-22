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
name|regionserver
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
name|hadoop
operator|.
name|hbase
operator|.
name|HConstants
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
name|client
operator|.
name|TableDescriptorBuilder
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * Split size is the number of regions that are on this server that all are  * of the same table, cubed, times 2x the region flush size OR the maximum  * region split size, whichever is smaller.  *<p>  * For example, if the flush size is 128MB, then after two flushes (256MB) we  * will split which will make two regions that will split when their size is  * {@code 2^3 * 128MB*2 = 2048MB}.  *<p>  * If one of these regions splits, then there are three regions and now the  * split size is {@code 3^3 * 128MB*2 = 6912MB}, and so on until we reach the configured  * maximum file size and then from there on out, we'll use that.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|IncreasingToUpperBoundRegionSplitPolicy
extends|extends
name|ConstantSizeRegionSplitPolicy
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
name|IncreasingToUpperBoundRegionSplitPolicy
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|long
name|initialSize
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|configureForRegion
parameter_list|(
name|HRegion
name|region
parameter_list|)
block|{
name|super
operator|.
name|configureForRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|getConf
argument_list|()
decl_stmt|;
name|initialSize
operator|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.increasing.policy.initial.size"
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|initialSize
operator|>
literal|0
condition|)
block|{
return|return;
block|}
name|TableDescriptor
name|desc
init|=
name|region
operator|.
name|getTableDescriptor
argument_list|()
decl_stmt|;
if|if
condition|(
name|desc
operator|!=
literal|null
condition|)
block|{
name|initialSize
operator|=
literal|2
operator|*
name|desc
operator|.
name|getMemStoreFlushSize
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|initialSize
operator|<=
literal|0
condition|)
block|{
name|initialSize
operator|=
literal|2
operator|*
name|conf
operator|.
name|getLong
argument_list|(
name|HConstants
operator|.
name|HREGION_MEMSTORE_FLUSH_SIZE
argument_list|,
name|TableDescriptorBuilder
operator|.
name|DEFAULT_MEMSTORE_FLUSH_SIZE
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|shouldSplit
parameter_list|()
block|{
name|boolean
name|force
init|=
name|region
operator|.
name|shouldForceSplit
argument_list|()
decl_stmt|;
name|boolean
name|foundABigStore
init|=
literal|false
decl_stmt|;
comment|// Get count of regions that have the same common table as this.region
name|int
name|tableRegionsCount
init|=
name|getCountOfCommonTableRegions
argument_list|()
decl_stmt|;
comment|// Get size to check
name|long
name|sizeToCheck
init|=
name|getSizeToCheck
argument_list|(
name|tableRegionsCount
argument_list|)
decl_stmt|;
for|for
control|(
name|HStore
name|store
range|:
name|region
operator|.
name|getStores
argument_list|()
control|)
block|{
comment|// If any of the stores is unable to split (eg they contain reference files)
comment|// then don't split
if|if
condition|(
operator|!
name|store
operator|.
name|canSplit
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// Mark if any store is big enough
name|long
name|size
init|=
name|store
operator|.
name|getSize
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|>
name|sizeToCheck
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"ShouldSplit because "
operator|+
name|store
operator|.
name|getColumnFamilyName
argument_list|()
operator|+
literal|" size="
operator|+
name|StringUtils
operator|.
name|humanSize
argument_list|(
name|size
argument_list|)
operator|+
literal|", sizeToCheck="
operator|+
name|StringUtils
operator|.
name|humanSize
argument_list|(
name|sizeToCheck
argument_list|)
operator|+
literal|", regionsWithCommonTable="
operator|+
name|tableRegionsCount
argument_list|)
expr_stmt|;
name|foundABigStore
operator|=
literal|true
expr_stmt|;
block|}
block|}
return|return
name|foundABigStore
operator|||
name|force
return|;
block|}
comment|/**    * @return Count of regions on this server that share the table this.region    * belongs to    */
specifier|private
name|int
name|getCountOfCommonTableRegions
parameter_list|()
block|{
name|RegionServerServices
name|rss
init|=
name|region
operator|.
name|getRegionServerServices
argument_list|()
decl_stmt|;
comment|// Can be null in tests
if|if
condition|(
name|rss
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
name|TableName
name|tablename
init|=
name|region
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getTableName
argument_list|()
decl_stmt|;
name|int
name|tableRegionsCount
init|=
literal|0
decl_stmt|;
try|try
block|{
name|List
argument_list|<
name|?
extends|extends
name|Region
argument_list|>
name|hri
init|=
name|rss
operator|.
name|getRegions
argument_list|(
name|tablename
argument_list|)
decl_stmt|;
name|tableRegionsCount
operator|=
name|hri
operator|==
literal|null
operator|||
name|hri
operator|.
name|isEmpty
argument_list|()
condition|?
literal|0
else|:
name|hri
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Failed getOnlineRegions "
operator|+
name|tablename
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|tableRegionsCount
return|;
block|}
comment|/**    * @return Region max size or {@code count of regions cubed * 2 * flushsize},    * which ever is smaller; guard against there being zero regions on this server.    */
specifier|protected
name|long
name|getSizeToCheck
parameter_list|(
specifier|final
name|int
name|tableRegionsCount
parameter_list|)
block|{
comment|// safety check for 100 to avoid numerical overflow in extreme cases
return|return
name|tableRegionsCount
operator|==
literal|0
operator|||
name|tableRegionsCount
operator|>
literal|100
condition|?
name|getDesiredMaxFileSize
argument_list|()
else|:
name|Math
operator|.
name|min
argument_list|(
name|getDesiredMaxFileSize
argument_list|()
argument_list|,
name|initialSize
operator|*
name|tableRegionsCount
operator|*
name|tableRegionsCount
operator|*
name|tableRegionsCount
argument_list|)
return|;
block|}
block|}
end_class

end_unit

