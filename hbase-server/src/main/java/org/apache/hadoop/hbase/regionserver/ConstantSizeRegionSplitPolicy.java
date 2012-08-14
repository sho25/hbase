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

begin_comment
comment|/**  * A {@link RegionSplitPolicy} implementation which splits a region  * as soon as any of its store files exceeds a maximum configurable  * size.  *<p>  * This is the default split policy. From 0.94.0 on the default split policy has  * changed to {@link IncreasingToUpperBoundRegionSplitPolicy}  *</p>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ConstantSizeRegionSplitPolicy
extends|extends
name|RegionSplitPolicy
block|{
specifier|private
name|long
name|desiredMaxFileSize
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
name|long
name|maxFileSize
init|=
name|region
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getMaxFileSize
argument_list|()
decl_stmt|;
comment|// By default we split region if a file> HConstants.DEFAULT_MAX_FILE_SIZE.
if|if
condition|(
name|maxFileSize
operator|==
name|HConstants
operator|.
name|DEFAULT_MAX_FILE_SIZE
condition|)
block|{
name|maxFileSize
operator|=
name|getConf
argument_list|()
operator|.
name|getLong
argument_list|(
name|HConstants
operator|.
name|HREGION_MAX_FILESIZE
argument_list|,
name|HConstants
operator|.
name|DEFAULT_MAX_FILE_SIZE
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|desiredMaxFileSize
operator|=
name|maxFileSize
expr_stmt|;
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
for|for
control|(
name|HStore
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
comment|// If any of the stores are unable to split (eg they contain reference files)
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
if|if
condition|(
name|store
operator|.
name|getSize
argument_list|()
operator|>
name|desiredMaxFileSize
condition|)
block|{
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
name|long
name|getDesiredMaxFileSize
parameter_list|()
block|{
return|return
name|desiredMaxFileSize
return|;
block|}
block|}
end_class

end_unit

