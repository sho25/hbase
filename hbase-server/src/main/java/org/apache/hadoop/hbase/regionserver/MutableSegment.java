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
name|Cell
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
name|CellComparator
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
name|client
operator|.
name|Scan
import|;
end_import

begin_comment
comment|/**  * A mutable segment in memstore, specifically the active segment.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MutableSegment
extends|extends
name|Segment
block|{
specifier|protected
name|MutableSegment
parameter_list|(
name|CellSet
name|cellSet
parameter_list|,
name|CellComparator
name|comparator
parameter_list|,
name|MemStoreLAB
name|memStoreLAB
parameter_list|,
name|long
name|size
parameter_list|)
block|{
name|super
argument_list|(
name|cellSet
argument_list|,
name|comparator
argument_list|,
name|memStoreLAB
argument_list|,
name|size
argument_list|,
name|ClassSize
operator|.
name|CONCURRENT_SKIPLISTMAP_ENTRY
argument_list|)
expr_stmt|;
block|}
comment|/**    * Adds the given cell into the segment    * @param cell the cell to add    * @param mslabUsed whether using MSLAB    * @return the change in the heap size    */
specifier|public
name|long
name|add
parameter_list|(
name|Cell
name|cell
parameter_list|,
name|boolean
name|mslabUsed
parameter_list|)
block|{
return|return
name|internalAdd
argument_list|(
name|cell
argument_list|,
name|mslabUsed
argument_list|)
return|;
block|}
comment|//methods for test
comment|/**    * Returns the first cell in the segment    * @return the first cell in the segment    */
name|Cell
name|first
parameter_list|()
block|{
return|return
name|this
operator|.
name|getCellSet
argument_list|()
operator|.
name|first
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|shouldSeek
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|long
name|oldestUnexpiredTS
parameter_list|)
block|{
return|return
operator|(
name|getTimeRangeTracker
argument_list|()
operator|.
name|includesTimeRange
argument_list|(
name|scan
operator|.
name|getTimeRange
argument_list|()
argument_list|)
operator|&&
operator|(
name|getTimeRangeTracker
argument_list|()
operator|.
name|getMax
argument_list|()
operator|>=
name|oldestUnexpiredTS
operator|)
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMinTimestamp
parameter_list|()
block|{
return|return
name|getTimeRangeTracker
argument_list|()
operator|.
name|getMin
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|keySize
parameter_list|()
block|{
return|return
name|size
operator|.
name|get
argument_list|()
operator|-
name|CompactingMemStore
operator|.
name|DEEP_OVERHEAD_PER_PIPELINE_SKIPLIST_ITEM
return|;
block|}
block|}
end_class

end_unit

