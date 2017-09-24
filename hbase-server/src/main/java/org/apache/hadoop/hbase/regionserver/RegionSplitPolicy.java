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
name|java
operator|.
name|util
operator|.
name|Optional
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
name|conf
operator|.
name|Configured
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
name|HBaseInterfaceAudience
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
name|util
operator|.
name|ReflectionUtils
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * A split policy determines when a region should be split.  * @see SteppingSplitPolicy Default split policy since 2.0.0  * @see IncreasingToUpperBoundRegionSplitPolicy Default split policy since  *      0.94.0  * @see ConstantSizeRegionSplitPolicy Default split policy before 0.94.0  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|CONFIG
argument_list|)
specifier|public
specifier|abstract
class|class
name|RegionSplitPolicy
extends|extends
name|Configured
block|{
specifier|private
specifier|static
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|RegionSplitPolicy
argument_list|>
name|DEFAULT_SPLIT_POLICY_CLASS
init|=
name|SteppingSplitPolicy
operator|.
name|class
decl_stmt|;
comment|/**    * The region configured for this split policy.    */
specifier|protected
name|HRegion
name|region
decl_stmt|;
comment|/**    * Upon construction, this method will be called with the region    * to be governed. It will be called once and only once.    */
specifier|protected
name|void
name|configureForRegion
parameter_list|(
name|HRegion
name|region
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
name|this
operator|.
name|region
operator|==
literal|null
argument_list|,
literal|"Policy already configured for region {}"
argument_list|,
name|this
operator|.
name|region
argument_list|)
expr_stmt|;
name|this
operator|.
name|region
operator|=
name|region
expr_stmt|;
block|}
comment|/**    * @return true if the specified region should be split.    */
specifier|protected
specifier|abstract
name|boolean
name|shouldSplit
parameter_list|()
function_decl|;
comment|/**    * @return the key at which the region should be split, or null    * if it cannot be split. This will only be called if shouldSplit    * previously returned true.    */
specifier|protected
name|byte
index|[]
name|getSplitPoint
parameter_list|()
block|{
name|byte
index|[]
name|explicitSplitPoint
init|=
name|this
operator|.
name|region
operator|.
name|getExplicitSplitPoint
argument_list|()
decl_stmt|;
if|if
condition|(
name|explicitSplitPoint
operator|!=
literal|null
condition|)
block|{
return|return
name|explicitSplitPoint
return|;
block|}
name|List
argument_list|<
name|HStore
argument_list|>
name|stores
init|=
name|region
operator|.
name|getStores
argument_list|()
decl_stmt|;
name|byte
index|[]
name|splitPointFromLargestStore
init|=
literal|null
decl_stmt|;
name|long
name|largestStoreSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|HStore
name|s
range|:
name|stores
control|)
block|{
name|Optional
argument_list|<
name|byte
index|[]
argument_list|>
name|splitPoint
init|=
name|s
operator|.
name|getSplitPoint
argument_list|()
decl_stmt|;
comment|// Store also returns null if it has references as way of indicating it is not splittable
name|long
name|storeSize
init|=
name|s
operator|.
name|getSize
argument_list|()
decl_stmt|;
if|if
condition|(
name|splitPoint
operator|.
name|isPresent
argument_list|()
operator|&&
name|largestStoreSize
operator|<
name|storeSize
condition|)
block|{
name|splitPointFromLargestStore
operator|=
name|splitPoint
operator|.
name|get
argument_list|()
expr_stmt|;
name|largestStoreSize
operator|=
name|storeSize
expr_stmt|;
block|}
block|}
return|return
name|splitPointFromLargestStore
return|;
block|}
comment|/**    * Create the RegionSplitPolicy configured for the given table.    * @param region    * @param conf    * @return a RegionSplitPolicy    * @throws IOException    */
specifier|public
specifier|static
name|RegionSplitPolicy
name|create
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|Class
argument_list|<
name|?
extends|extends
name|RegionSplitPolicy
argument_list|>
name|clazz
init|=
name|getSplitPolicyClass
argument_list|(
name|region
operator|.
name|getTableDescriptor
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|RegionSplitPolicy
name|policy
init|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|clazz
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|policy
operator|.
name|configureForRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
return|return
name|policy
return|;
block|}
specifier|public
specifier|static
name|Class
argument_list|<
name|?
extends|extends
name|RegionSplitPolicy
argument_list|>
name|getSplitPolicyClass
parameter_list|(
name|TableDescriptor
name|htd
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|className
init|=
name|htd
operator|.
name|getRegionSplitPolicyClassName
argument_list|()
decl_stmt|;
if|if
condition|(
name|className
operator|==
literal|null
condition|)
block|{
name|className
operator|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|HBASE_REGION_SPLIT_POLICY_KEY
argument_list|,
name|DEFAULT_SPLIT_POLICY_CLASS
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|RegionSplitPolicy
argument_list|>
name|clazz
init|=
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|)
operator|.
name|asSubclass
argument_list|(
name|RegionSplitPolicy
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
name|clazz
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to load configured region split policy '"
operator|+
name|className
operator|+
literal|"' for table '"
operator|+
name|htd
operator|.
name|getTableName
argument_list|()
operator|+
literal|"'"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * In {@link HRegionFileSystem#splitStoreFile(org.apache.hadoop.hbase.HRegionInfo, String,    * HStoreFile, byte[], boolean, RegionSplitPolicy)} we are not creating the split reference    * if split row not lies in the StoreFile range. But in some use cases we may need to create    * the split reference even when the split row not lies in the range. This method can be used    * to decide, whether to skip the the StoreFile range check or not.    * @return whether to skip the StoreFile range check or not    * @param familyName    * @return whether to skip the StoreFile range check or not    */
specifier|protected
name|boolean
name|skipStoreFileRangeCheck
parameter_list|(
name|String
name|familyName
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

