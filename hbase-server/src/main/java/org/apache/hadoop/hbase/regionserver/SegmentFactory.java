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
name|ArrayList
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
comment|/**  * A singleton store segment factory.  * Generate concrete store segments.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|SegmentFactory
block|{
specifier|private
name|SegmentFactory
parameter_list|()
block|{}
specifier|private
specifier|static
name|SegmentFactory
name|instance
init|=
operator|new
name|SegmentFactory
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|SegmentFactory
name|instance
parameter_list|()
block|{
return|return
name|instance
return|;
block|}
comment|// create composite immutable segment from a list of segments
comment|// for snapshot consisting of multiple segments
specifier|public
name|CompositeImmutableSegment
name|createCompositeImmutableSegment
parameter_list|(
specifier|final
name|CellComparator
name|comparator
parameter_list|,
name|List
argument_list|<
name|ImmutableSegment
argument_list|>
name|segments
parameter_list|)
block|{
return|return
operator|new
name|CompositeImmutableSegment
argument_list|(
name|comparator
argument_list|,
name|segments
argument_list|)
return|;
block|}
comment|// create new flat immutable segment from compacting old immutable segments
comment|// for compaction
specifier|public
name|ImmutableSegment
name|createImmutableSegmentByCompaction
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|CellComparator
name|comparator
parameter_list|,
name|MemStoreSegmentsIterator
name|iterator
parameter_list|,
name|int
name|numOfCells
parameter_list|,
name|CompactingMemStore
operator|.
name|IndexType
name|idxType
parameter_list|)
throws|throws
name|IOException
block|{
name|MemStoreLAB
name|memStoreLAB
init|=
name|MemStoreLAB
operator|.
name|newInstance
argument_list|(
name|conf
argument_list|)
decl_stmt|;
return|return
name|createImmutableSegment
argument_list|(
name|conf
argument_list|,
name|comparator
argument_list|,
name|iterator
argument_list|,
name|memStoreLAB
argument_list|,
name|numOfCells
argument_list|,
name|MemStoreCompactor
operator|.
name|Action
operator|.
name|COMPACT
argument_list|,
name|idxType
argument_list|)
return|;
block|}
comment|// create empty immutable segment
comment|// for initializations
specifier|public
name|ImmutableSegment
name|createImmutableSegment
parameter_list|(
name|CellComparator
name|comparator
parameter_list|)
block|{
name|MutableSegment
name|segment
init|=
name|generateMutableSegment
argument_list|(
literal|null
argument_list|,
name|comparator
argument_list|,
literal|null
argument_list|)
decl_stmt|;
return|return
name|createImmutableSegment
argument_list|(
name|segment
argument_list|)
return|;
block|}
comment|// create not-flat immutable segment from mutable segment
specifier|public
name|ImmutableSegment
name|createImmutableSegment
parameter_list|(
name|MutableSegment
name|segment
parameter_list|)
block|{
return|return
operator|new
name|CSLMImmutableSegment
argument_list|(
name|segment
argument_list|)
return|;
block|}
comment|// create mutable segment
specifier|public
name|MutableSegment
name|createMutableSegment
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
name|CellComparator
name|comparator
parameter_list|)
block|{
name|MemStoreLAB
name|memStoreLAB
init|=
name|MemStoreLAB
operator|.
name|newInstance
argument_list|(
name|conf
argument_list|)
decl_stmt|;
return|return
name|generateMutableSegment
argument_list|(
name|conf
argument_list|,
name|comparator
argument_list|,
name|memStoreLAB
argument_list|)
return|;
block|}
comment|// create new flat immutable segment from merging old immutable segments
comment|// for merge
specifier|public
name|ImmutableSegment
name|createImmutableSegmentByMerge
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|CellComparator
name|comparator
parameter_list|,
name|MemStoreSegmentsIterator
name|iterator
parameter_list|,
name|int
name|numOfCells
parameter_list|,
name|List
argument_list|<
name|ImmutableSegment
argument_list|>
name|segments
parameter_list|,
name|CompactingMemStore
operator|.
name|IndexType
name|idxType
parameter_list|)
throws|throws
name|IOException
block|{
name|MemStoreLAB
name|memStoreLAB
init|=
name|getMergedMemStoreLAB
argument_list|(
name|conf
argument_list|,
name|segments
argument_list|)
decl_stmt|;
return|return
name|createImmutableSegment
argument_list|(
name|conf
argument_list|,
name|comparator
argument_list|,
name|iterator
argument_list|,
name|memStoreLAB
argument_list|,
name|numOfCells
argument_list|,
name|MemStoreCompactor
operator|.
name|Action
operator|.
name|MERGE
argument_list|,
name|idxType
argument_list|)
return|;
block|}
comment|// create flat immutable segment from non-flat immutable segment
comment|// for flattening
specifier|public
name|ImmutableSegment
name|createImmutableSegmentByFlattening
parameter_list|(
name|CSLMImmutableSegment
name|segment
parameter_list|,
name|CompactingMemStore
operator|.
name|IndexType
name|idxType
parameter_list|,
name|MemStoreSize
name|memstoreSize
parameter_list|)
block|{
name|ImmutableSegment
name|res
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|idxType
condition|)
block|{
case|case
name|CHUNK_MAP
case|:
name|res
operator|=
operator|new
name|CellChunkImmutableSegment
argument_list|(
name|segment
argument_list|,
name|memstoreSize
argument_list|)
expr_stmt|;
break|break;
case|case
name|CSLM_MAP
case|:
assert|assert
literal|false
assert|;
comment|// non-flat segment can not be the result of flattening
break|break;
case|case
name|ARRAY_MAP
case|:
name|res
operator|=
operator|new
name|CellArrayImmutableSegment
argument_list|(
name|segment
argument_list|,
name|memstoreSize
argument_list|)
expr_stmt|;
break|break;
block|}
return|return
name|res
return|;
block|}
comment|//****** private methods to instantiate concrete store segments **********//
specifier|private
name|ImmutableSegment
name|createImmutableSegment
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|CellComparator
name|comparator
parameter_list|,
name|MemStoreSegmentsIterator
name|iterator
parameter_list|,
name|MemStoreLAB
name|memStoreLAB
parameter_list|,
name|int
name|numOfCells
parameter_list|,
name|MemStoreCompactor
operator|.
name|Action
name|action
parameter_list|,
name|CompactingMemStore
operator|.
name|IndexType
name|idxType
parameter_list|)
block|{
name|ImmutableSegment
name|res
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|idxType
condition|)
block|{
case|case
name|CHUNK_MAP
case|:
name|res
operator|=
operator|new
name|CellChunkImmutableSegment
argument_list|(
name|comparator
argument_list|,
name|iterator
argument_list|,
name|memStoreLAB
argument_list|,
name|numOfCells
argument_list|,
name|action
argument_list|)
expr_stmt|;
break|break;
case|case
name|CSLM_MAP
case|:
assert|assert
literal|false
assert|;
comment|// non-flat segment can not be created here
break|break;
case|case
name|ARRAY_MAP
case|:
name|res
operator|=
operator|new
name|CellArrayImmutableSegment
argument_list|(
name|comparator
argument_list|,
name|iterator
argument_list|,
name|memStoreLAB
argument_list|,
name|numOfCells
argument_list|,
name|action
argument_list|)
expr_stmt|;
break|break;
block|}
return|return
name|res
return|;
block|}
specifier|private
name|MutableSegment
name|generateMutableSegment
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
name|CellComparator
name|comparator
parameter_list|,
name|MemStoreLAB
name|memStoreLAB
parameter_list|)
block|{
comment|// TBD use configuration to set type of segment
name|CellSet
name|set
init|=
operator|new
name|CellSet
argument_list|(
name|comparator
argument_list|)
decl_stmt|;
return|return
operator|new
name|MutableSegment
argument_list|(
name|set
argument_list|,
name|comparator
argument_list|,
name|memStoreLAB
argument_list|)
return|;
block|}
specifier|private
name|MemStoreLAB
name|getMergedMemStoreLAB
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|List
argument_list|<
name|ImmutableSegment
argument_list|>
name|segments
parameter_list|)
block|{
name|List
argument_list|<
name|MemStoreLAB
argument_list|>
name|mslabs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|conf
operator|.
name|getBoolean
argument_list|(
name|MemStoreLAB
operator|.
name|USEMSLAB_KEY
argument_list|,
name|MemStoreLAB
operator|.
name|USEMSLAB_DEFAULT
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
for|for
control|(
name|ImmutableSegment
name|segment
range|:
name|segments
control|)
block|{
name|mslabs
operator|.
name|add
argument_list|(
name|segment
operator|.
name|getMemStoreLAB
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ImmutableMemStoreLAB
argument_list|(
name|mslabs
argument_list|)
return|;
block|}
block|}
end_class

end_unit

