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
name|HTableDescriptor
import|;
end_import

begin_comment
comment|/**  * Split size is the number of regions that are on this server that all are  * of the same table, squared, times the region flush size OR the maximum  * region split size, whichever is smaller.  For example, if the flush size  * is 128M, then on first flush we will split which will make two regions  * that will split when their size is 2 * 2 * 128M = 512M.  If one of these  * regions splits, then there are three regions and now the split size is  * 3 * 3 * 128M =  1152M, and so on until we reach the configured  * maximum filesize and then from there on out, we'll use that.  */
end_comment

begin_class
specifier|public
class|class
name|IncreasingToUpperBoundRegionSplitPolicy
extends|extends
name|ConstantSizeRegionSplitPolicy
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|IncreasingToUpperBoundRegionSplitPolicy
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|long
name|flushSize
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
name|HTableDescriptor
name|desc
init|=
name|region
operator|.
name|getTableDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|desc
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|flushSize
operator|=
name|desc
operator|.
name|getMemStoreFlushSize
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|flushSize
operator|<=
literal|0
condition|)
block|{
name|this
operator|.
name|flushSize
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|HConstants
operator|.
name|HREGION_MEMSTORE_FLUSH_SIZE
argument_list|,
name|HTableDescriptor
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
if|if
condition|(
name|region
operator|.
name|shouldForceSplit
argument_list|()
condition|)
return|return
literal|true
return|;
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
name|Store
name|store
range|:
name|region
operator|.
name|getStores
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
comment|// If any of the stores is unable to split (eg they contain reference files)
comment|// then don't split
if|if
condition|(
operator|(
operator|!
name|store
operator|.
name|canSplit
argument_list|()
operator|)
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
name|size
operator|+
literal|", sizeToCheck="
operator|+
name|sizeToCheck
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
break|break;
block|}
block|}
return|return
name|foundABigStore
return|;
block|}
comment|/**    * @return Region max size or<code>count of regions squared * flushsize, which ever is    * smaller; guard against there being zero regions on this server.    */
name|long
name|getSizeToCheck
parameter_list|(
specifier|final
name|int
name|tableRegionsCount
parameter_list|)
block|{
return|return
name|tableRegionsCount
operator|==
literal|0
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
name|this
operator|.
name|flushSize
operator|*
operator|(
name|tableRegionsCount
operator|*
operator|(
name|long
operator|)
name|tableRegionsCount
operator|)
argument_list|)
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
name|this
operator|.
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
return|return
literal|0
return|;
name|TableName
name|tablename
init|=
name|this
operator|.
name|region
operator|.
name|getTableDesc
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
name|HRegion
argument_list|>
name|hri
init|=
name|rss
operator|.
name|getOnlineRegions
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
block|}
end_class

end_unit

