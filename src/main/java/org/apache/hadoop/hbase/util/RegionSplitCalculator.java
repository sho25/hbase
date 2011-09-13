begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
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
name|Comparator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|util
operator|.
name|Bytes
operator|.
name|ByteArrayComparator
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ArrayListMultimap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Multimap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|TreeMultimap
import|;
end_import

begin_comment
comment|/**  * This is a generic region split calculator. It requires Ranges that provide  * start, end, and a comparator. It works in two phases -- the first adds ranges  * and rejects backwards ranges. Then one calls calcRegions to generate the  * multimap that has a start split key as a key and possibly multiple Ranges as  * members.  *   * To traverse, one normally would get the split set, and iterate through the  * calcRegions. Normal regions would have only one entry, holes would have zero,  * and any overlaps would have multiple entries.  *   * The interface is a bit cumbersome currently but is exposed this way so that  * clients can choose how to iterate through the region splits.  *   * @param<R>  */
end_comment

begin_class
specifier|public
class|class
name|RegionSplitCalculator
parameter_list|<
name|R
extends|extends
name|KeyRange
parameter_list|>
block|{
specifier|final
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RegionSplitCalculator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Comparator
argument_list|<
name|R
argument_list|>
name|rangeCmp
decl_stmt|;
comment|/**    * This contains a sorted set of all the possible split points    *     * Invariant: once populated this has 0 entries if empty or at most n+1 values    * where n == number of added ranges.    */
specifier|private
specifier|final
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
name|splits
init|=
operator|new
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
comment|/**    * This is a map from start key to regions with the same start key.    *     * Invariant: This always have n values in total    */
specifier|private
specifier|final
name|Multimap
argument_list|<
name|byte
index|[]
argument_list|,
name|R
argument_list|>
name|starts
init|=
name|ArrayListMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
comment|/**    * SPECIAL CASE    */
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|ENDKEY
init|=
literal|null
decl_stmt|;
specifier|public
name|RegionSplitCalculator
parameter_list|(
name|Comparator
argument_list|<
name|R
argument_list|>
name|cmp
parameter_list|)
block|{
name|rangeCmp
operator|=
name|cmp
expr_stmt|;
block|}
specifier|public
specifier|final
specifier|static
name|Comparator
argument_list|<
name|byte
index|[]
argument_list|>
name|BYTES_COMPARATOR
init|=
operator|new
name|ByteArrayComparator
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|byte
index|[]
name|l
parameter_list|,
name|byte
index|[]
name|r
parameter_list|)
block|{
if|if
condition|(
name|l
operator|==
literal|null
operator|&&
name|r
operator|==
literal|null
condition|)
return|return
literal|0
return|;
if|if
condition|(
name|l
operator|==
literal|null
condition|)
return|return
literal|1
return|;
if|if
condition|(
name|r
operator|==
literal|null
condition|)
return|return
operator|-
literal|1
return|;
return|return
name|super
operator|.
name|compare
argument_list|(
name|l
argument_list|,
name|r
argument_list|)
return|;
block|}
block|}
decl_stmt|;
comment|/**    * SPECIAL CASE wrapper for empty end key    *     * @return ENDKEY if end key is empty, else normal endkey.    */
specifier|private
name|byte
index|[]
name|specialEndKey
parameter_list|(
name|R
name|range
parameter_list|)
block|{
name|byte
index|[]
name|end
init|=
name|range
operator|.
name|getEndKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|end
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|ENDKEY
return|;
block|}
return|return
name|end
return|;
block|}
comment|/**    * Adds an edge to the split calculator    *     * @return true if is included, false if backwards/invalid    */
specifier|public
name|boolean
name|add
parameter_list|(
name|R
name|range
parameter_list|)
block|{
name|byte
index|[]
name|start
init|=
name|range
operator|.
name|getStartKey
argument_list|()
decl_stmt|;
name|byte
index|[]
name|end
init|=
name|specialEndKey
argument_list|(
name|range
argument_list|)
decl_stmt|;
if|if
condition|(
name|end
operator|!=
name|ENDKEY
operator|&&
name|Bytes
operator|.
name|compareTo
argument_list|(
name|start
argument_list|,
name|end
argument_list|)
operator|>
literal|0
condition|)
block|{
comment|// don't allow backwards edges
name|LOG
operator|.
name|debug
argument_list|(
literal|"attempted to add backwards edge: "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|start
argument_list|)
operator|+
literal|" "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|end
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|splits
operator|.
name|add
argument_list|(
name|start
argument_list|)
expr_stmt|;
name|splits
operator|.
name|add
argument_list|(
name|end
argument_list|)
expr_stmt|;
name|starts
operator|.
name|put
argument_list|(
name|start
argument_list|,
name|range
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
comment|/**    * Generates a coverage multimap from split key to Regions that start with the    * split key.    *     * @return coverage multimap    */
specifier|public
name|Multimap
argument_list|<
name|byte
index|[]
argument_list|,
name|R
argument_list|>
name|calcCoverage
parameter_list|()
block|{
comment|// This needs to be sorted to force the use of the comparator on the values,
comment|// otherwise byte array comparison isn't used
name|Multimap
argument_list|<
name|byte
index|[]
argument_list|,
name|R
argument_list|>
name|regions
init|=
name|TreeMultimap
operator|.
name|create
argument_list|(
name|BYTES_COMPARATOR
argument_list|,
name|rangeCmp
argument_list|)
decl_stmt|;
comment|// march through all splits from the start points
for|for
control|(
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|Collection
argument_list|<
name|R
argument_list|>
argument_list|>
name|start
range|:
name|starts
operator|.
name|asMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|byte
index|[]
name|key
init|=
name|start
operator|.
name|getKey
argument_list|()
decl_stmt|;
for|for
control|(
name|R
name|r
range|:
name|start
operator|.
name|getValue
argument_list|()
control|)
block|{
name|regions
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|r
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|coveredSplit
range|:
name|splits
operator|.
name|subSet
argument_list|(
name|r
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|specialEndKey
argument_list|(
name|r
argument_list|)
argument_list|)
control|)
block|{
name|regions
operator|.
name|put
argument_list|(
name|coveredSplit
argument_list|,
name|r
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|regions
return|;
block|}
specifier|public
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
name|getSplits
parameter_list|()
block|{
return|return
name|splits
return|;
block|}
specifier|public
name|Multimap
argument_list|<
name|byte
index|[]
argument_list|,
name|R
argument_list|>
name|getStarts
parameter_list|()
block|{
return|return
name|starts
return|;
block|}
block|}
end_class

end_unit

