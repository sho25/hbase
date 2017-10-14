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
name|hbase
operator|.
name|CellComparator
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
name|util
operator|.
name|ClassSize
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
name|io
operator|.
name|TimeRange
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
name|Arrays
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
comment|/**  * ImmutableSegment is an abstract class that extends the API supported by a {@link Segment},  * and is not needed for a {@link MutableSegment}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|ImmutableSegment
extends|extends
name|Segment
block|{
specifier|public
specifier|static
specifier|final
name|long
name|DEEP_OVERHEAD
init|=
name|Segment
operator|.
name|DEEP_OVERHEAD
operator|+
name|ClassSize
operator|.
name|NON_SYNC_TIMERANGE_TRACKER
decl_stmt|;
comment|// each sub-type of immutable segment knows whether it is flat or not
specifier|protected
specifier|abstract
name|boolean
name|canBeFlattened
parameter_list|()
function_decl|;
comment|/////////////////////  CONSTRUCTORS  /////////////////////
comment|/**------------------------------------------------------------------------    * Empty C-tor to be used only for CompositeImmutableSegment    */
specifier|protected
name|ImmutableSegment
parameter_list|(
name|CellComparator
name|comparator
parameter_list|)
block|{
name|super
argument_list|(
name|comparator
argument_list|,
name|TimeRangeTracker
operator|.
name|create
argument_list|(
name|TimeRangeTracker
operator|.
name|Type
operator|.
name|NON_SYNC
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**------------------------------------------------------------------------    * C-tor to be used to build the derived classes    */
specifier|protected
name|ImmutableSegment
parameter_list|(
name|CellSet
name|cs
parameter_list|,
name|CellComparator
name|comparator
parameter_list|,
name|MemStoreLAB
name|memStoreLAB
parameter_list|)
block|{
name|super
argument_list|(
name|cs
argument_list|,
name|comparator
argument_list|,
name|memStoreLAB
argument_list|,
name|TimeRangeTracker
operator|.
name|create
argument_list|(
name|TimeRangeTracker
operator|.
name|Type
operator|.
name|NON_SYNC
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**------------------------------------------------------------------------    * Copy C-tor to be used when new CSLMImmutableSegment (derived) is being built from a Mutable one.    * This C-tor should be used when active MutableSegment is pushed into the compaction    * pipeline and becomes an ImmutableSegment.    */
specifier|protected
name|ImmutableSegment
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
block|}
comment|/////////////////////  PUBLIC METHODS  /////////////////////
specifier|public
name|int
name|getNumOfSegments
parameter_list|()
block|{
return|return
literal|1
return|;
block|}
specifier|public
name|List
argument_list|<
name|Segment
argument_list|>
name|getAllSegments
parameter_list|()
block|{
name|List
argument_list|<
name|Segment
argument_list|>
name|res
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|this
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|res
return|;
block|}
block|}
end_class

end_unit

