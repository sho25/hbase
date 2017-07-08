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
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * A {@link FlushPolicy} that only flushes store larger a given threshold. If no store is large  * enough, then all stores will be flushed.  */
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
class|class
name|FlushAllLargeStoresPolicy
extends|extends
name|FlushLargeStoresPolicy
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
name|FlushAllLargeStoresPolicy
operator|.
name|class
argument_list|)
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
name|int
name|familyNumber
init|=
name|region
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getColumnFamilyCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|familyNumber
operator|<=
literal|1
condition|)
block|{
comment|// No need to parse and set flush size lower bound if only one family
comment|// Family number might also be zero in some of our unit test case
return|return;
block|}
name|this
operator|.
name|flushSizeLowerBound
operator|=
name|getFlushSizeLowerBound
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|Store
argument_list|>
name|selectStoresToFlush
parameter_list|()
block|{
comment|// no need to select stores if only one family
if|if
condition|(
name|region
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getColumnFamilyCount
argument_list|()
operator|==
literal|1
condition|)
block|{
return|return
name|region
operator|.
name|stores
operator|.
name|values
argument_list|()
return|;
block|}
comment|// start selection
name|Collection
argument_list|<
name|Store
argument_list|>
name|stores
init|=
name|region
operator|.
name|stores
operator|.
name|values
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Store
argument_list|>
name|specificStoresToFlush
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Store
name|store
range|:
name|stores
control|)
block|{
if|if
condition|(
name|shouldFlush
argument_list|(
name|store
argument_list|)
condition|)
block|{
name|specificStoresToFlush
operator|.
name|add
argument_list|(
name|store
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|specificStoresToFlush
operator|.
name|isEmpty
argument_list|()
condition|)
return|return
name|specificStoresToFlush
return|;
comment|// Didn't find any CFs which were above the threshold for selection.
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Since none of the CFs were above the size, flushing all."
argument_list|)
expr_stmt|;
block|}
return|return
name|stores
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|shouldFlush
parameter_list|(
name|Store
name|store
parameter_list|)
block|{
return|return
operator|(
name|super
operator|.
name|shouldFlush
argument_list|(
name|store
argument_list|)
operator|||
name|region
operator|.
name|shouldFlushStore
argument_list|(
name|store
argument_list|)
operator|)
return|;
block|}
block|}
end_class

end_unit

