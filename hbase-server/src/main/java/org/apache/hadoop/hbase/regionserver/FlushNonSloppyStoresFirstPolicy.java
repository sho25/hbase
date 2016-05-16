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

begin_comment
comment|/**  * A {@link FlushPolicy} that only flushes store larger than a given threshold. If no store is large  * enough, then all stores will be flushed.  * Gives priority to selecting regular stores first, and only if no other  * option, selects sloppy stores which normaly require more memory.  */
end_comment

begin_class
specifier|public
class|class
name|FlushNonSloppyStoresFirstPolicy
extends|extends
name|FlushLargeStoresPolicy
block|{
specifier|private
name|Collection
argument_list|<
name|Store
argument_list|>
name|regularStores
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|Collection
argument_list|<
name|Store
argument_list|>
name|sloppyStores
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * @return the stores need to be flushed.    */
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
name|Collection
argument_list|<
name|Store
argument_list|>
name|specificStoresToFlush
init|=
operator|new
name|HashSet
argument_list|<
name|Store
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Store
name|store
range|:
name|regularStores
control|)
block|{
if|if
condition|(
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
for|for
control|(
name|Store
name|store
range|:
name|sloppyStores
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
return|return
name|region
operator|.
name|stores
operator|.
name|values
argument_list|()
return|;
block|}
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
name|this
operator|.
name|flushSizeLowerBound
operator|=
name|getFlushSizeLowerBound
argument_list|(
name|region
argument_list|)
expr_stmt|;
for|for
control|(
name|Store
name|store
range|:
name|region
operator|.
name|stores
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|store
operator|.
name|getMemStore
argument_list|()
operator|.
name|isSloppy
argument_list|()
condition|)
block|{
name|sloppyStores
operator|.
name|add
argument_list|(
name|store
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|regularStores
operator|.
name|add
argument_list|(
name|store
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

