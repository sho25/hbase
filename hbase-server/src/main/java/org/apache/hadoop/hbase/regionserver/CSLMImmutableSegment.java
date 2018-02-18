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
name|util
operator|.
name|ClassSize
import|;
end_import

begin_comment
comment|/**  * CSLMImmutableSegment is an abstract class that extends the API supported by a {@link Segment},  * and {@link ImmutableSegment}. This immutable segment is working with CellSet with  * ConcurrentSkipListMap (CSLM) delegatee.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CSLMImmutableSegment
extends|extends
name|ImmutableSegment
block|{
specifier|public
specifier|static
specifier|final
name|long
name|DEEP_OVERHEAD_CSLM
init|=
name|ImmutableSegment
operator|.
name|DEEP_OVERHEAD
operator|+
name|ClassSize
operator|.
name|CONCURRENT_SKIPLISTMAP
decl_stmt|;
comment|/**------------------------------------------------------------------------    * Copy C-tor to be used when new CSLMImmutableSegment is being built from a Mutable one.    * This C-tor should be used when active MutableSegment is pushed into the compaction    * pipeline and becomes an ImmutableSegment.    */
specifier|protected
name|CSLMImmutableSegment
parameter_list|(
name|Segment
name|segment
parameter_list|)
block|{
name|super
argument_list|(
name|segment
argument_list|)
expr_stmt|;
comment|// update the segment metadata heap size
name|long
name|indexOverhead
init|=
operator|-
name|MutableSegment
operator|.
name|DEEP_OVERHEAD
operator|+
name|DEEP_OVERHEAD_CSLM
decl_stmt|;
name|incSize
argument_list|(
literal|0
argument_list|,
name|indexOverhead
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// CSLM is always on-heap
block|}
annotation|@
name|Override
specifier|protected
name|long
name|indexEntrySize
parameter_list|()
block|{
return|return
name|ClassSize
operator|.
name|CONCURRENT_SKIPLISTMAP_ENTRY
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|canBeFlattened
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

