begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Cellersion 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY CellIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

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
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableMap
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

begin_comment
comment|/**  * CellFlatMap stores a constant number of elements and is immutable after creation stage.  * Being immutable, the CellFlatMap can be implemented as array.  * The actual array can be on- or off-heap and is implemented in concrete class derived from CellFlatMap.  * The CellFlatMap uses no synchronization primitives, it is assumed to be created by a  * single thread and then it can be read-only by multiple threads.  *  * The "flat" in the name, means that the memory layout of the Map is sequential array and thus  * requires less memory than ConcurrentSkipListMap.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|CellFlatMap
implements|implements
name|NavigableMap
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|CellFlatMap
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Comparator
argument_list|<
name|?
super|super
name|Cell
argument_list|>
name|comparator
decl_stmt|;
specifier|protected
name|int
name|minCellIdx
init|=
literal|0
decl_stmt|;
comment|// the index of the minimal cell (for sub-sets)
specifier|protected
name|int
name|maxCellIdx
init|=
literal|0
decl_stmt|;
comment|// the index of the cell after the maximal cell (for sub-sets)
specifier|private
name|boolean
name|descending
init|=
literal|false
decl_stmt|;
comment|/* C-tor */
specifier|public
name|CellFlatMap
parameter_list|(
name|Comparator
argument_list|<
name|?
super|super
name|Cell
argument_list|>
name|comparator
parameter_list|,
name|int
name|min
parameter_list|,
name|int
name|max
parameter_list|,
name|boolean
name|d
parameter_list|)
block|{
name|this
operator|.
name|comparator
operator|=
name|comparator
expr_stmt|;
name|this
operator|.
name|minCellIdx
operator|=
name|min
expr_stmt|;
name|this
operator|.
name|maxCellIdx
operator|=
name|max
expr_stmt|;
name|this
operator|.
name|descending
operator|=
name|d
expr_stmt|;
block|}
comment|/* Used for abstract CellFlatMap creation, implemented by derived class */
specifier|protected
specifier|abstract
name|CellFlatMap
name|createSubCellFlatMap
parameter_list|(
name|int
name|min
parameter_list|,
name|int
name|max
parameter_list|,
name|boolean
name|descending
parameter_list|)
function_decl|;
comment|/* Returns the i-th cell in the cell block */
specifier|protected
specifier|abstract
name|Cell
name|getCell
parameter_list|(
name|int
name|i
parameter_list|)
function_decl|;
comment|/**    * Binary search for a given key in between given boundaries of the array.    * Positive returned numbers mean the index.    * Negative returned numbers means the key not found.    *    * The absolute value of the output is the    * possible insert index for the searched key    *    * In twos-complement, (-1 * insertion point)-1 is the bitwise not of the insert point.    *    *    * @param needle The key to look for in all of the entries    * @return Same return value as Arrays.binarySearch.    */
specifier|private
name|int
name|find
parameter_list|(
name|Cell
name|needle
parameter_list|)
block|{
name|int
name|begin
init|=
name|minCellIdx
decl_stmt|;
name|int
name|end
init|=
name|maxCellIdx
operator|-
literal|1
decl_stmt|;
while|while
condition|(
name|begin
operator|<=
name|end
condition|)
block|{
name|int
name|mid
init|=
name|begin
operator|+
operator|(
operator|(
name|end
operator|-
name|begin
operator|)
operator|>>
literal|1
operator|)
decl_stmt|;
name|Cell
name|midCell
init|=
name|getCell
argument_list|(
name|mid
argument_list|)
decl_stmt|;
name|int
name|compareRes
init|=
name|comparator
operator|.
name|compare
argument_list|(
name|midCell
argument_list|,
name|needle
argument_list|)
decl_stmt|;
if|if
condition|(
name|compareRes
operator|==
literal|0
condition|)
block|{
return|return
name|mid
return|;
comment|// 0 means equals. We found the key
block|}
comment|// Key not found. Check the comparison results; reverse the meaning of
comment|// the comparison in case the order is descending (using XOR)
if|if
condition|(
operator|(
name|compareRes
operator|<
literal|0
operator|)
operator|^
name|descending
condition|)
block|{
comment|// midCell is less than needle so we need to look at farther up
name|begin
operator|=
name|mid
operator|+
literal|1
expr_stmt|;
block|}
else|else
block|{
comment|// midCell is greater than needle so we need to look down
name|end
operator|=
name|mid
operator|-
literal|1
expr_stmt|;
block|}
block|}
return|return
operator|(
operator|-
literal|1
operator|*
name|begin
operator|)
operator|-
literal|1
return|;
block|}
comment|/**    * Get the index of the given anchor key for creating subsequent set.    * It doesn't matter whether the given key exists in the set or not.    * taking into consideration whether    * the key should be inclusive or exclusive.    */
specifier|private
name|int
name|getValidIndex
parameter_list|(
name|Cell
name|key
parameter_list|,
name|boolean
name|inclusive
parameter_list|,
name|boolean
name|tail
parameter_list|)
block|{
specifier|final
name|int
name|index
init|=
name|find
argument_list|(
name|key
argument_list|)
decl_stmt|;
comment|// get the valid (positive) insertion point from the output of the find() method
name|int
name|insertionPoint
init|=
name|index
operator|<
literal|0
condition|?
operator|~
name|index
else|:
name|index
decl_stmt|;
comment|// correct the insertion point in case the given anchor key DOES EXIST in the set
if|if
condition|(
name|index
operator|>=
literal|0
condition|)
block|{
if|if
condition|(
name|descending
operator|&&
operator|!
operator|(
name|tail
operator|^
name|inclusive
operator|)
condition|)
block|{
comment|// for the descending case
comment|// if anchor for head set (tail=false) AND anchor is not inclusive -> move the insertion pt
comment|// if anchor for tail set (tail=true) AND the keys is inclusive -> move the insertion point
comment|// because the end index of a set is the index of the cell after the maximal cell
name|insertionPoint
operator|+=
literal|1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|descending
operator|&&
operator|(
name|tail
operator|^
name|inclusive
operator|)
condition|)
block|{
comment|// for the ascending case
comment|// if anchor for head set (tail=false) AND anchor is inclusive -> move the insertion point
comment|// because the end index of a set is the index of the cell after the maximal cell
comment|// if anchor for tail set (tail=true) AND the keys is not inclusive -> move the insertion pt
name|insertionPoint
operator|+=
literal|1
expr_stmt|;
block|}
block|}
comment|// insert the insertion point into the valid range,
comment|// as we may enlarge it too much in the above correction
return|return
name|Math
operator|.
name|min
argument_list|(
name|Math
operator|.
name|max
argument_list|(
name|insertionPoint
argument_list|,
name|minCellIdx
argument_list|)
argument_list|,
name|maxCellIdx
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Comparator
argument_list|<
name|?
super|super
name|Cell
argument_list|>
name|comparator
parameter_list|()
block|{
return|return
name|comparator
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|maxCellIdx
operator|-
name|minCellIdx
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
operator|(
name|size
argument_list|()
operator|==
literal|0
operator|)
return|;
block|}
comment|// ---------------- Sub-Maps ----------------
annotation|@
name|Override
specifier|public
name|NavigableMap
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
name|subMap
parameter_list|(
name|Cell
name|fromKey
parameter_list|,
name|boolean
name|fromInclusive
parameter_list|,
name|Cell
name|toKey
parameter_list|,
name|boolean
name|toInclusive
parameter_list|)
block|{
specifier|final
name|int
name|lessCellIndex
init|=
name|getValidIndex
argument_list|(
name|fromKey
argument_list|,
name|fromInclusive
argument_list|,
literal|true
argument_list|)
decl_stmt|;
specifier|final
name|int
name|greaterCellIndex
init|=
name|getValidIndex
argument_list|(
name|toKey
argument_list|,
name|toInclusive
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|descending
condition|)
block|{
return|return
name|createSubCellFlatMap
argument_list|(
name|greaterCellIndex
argument_list|,
name|lessCellIndex
argument_list|,
name|descending
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|createSubCellFlatMap
argument_list|(
name|lessCellIndex
argument_list|,
name|greaterCellIndex
argument_list|,
name|descending
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|NavigableMap
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
name|headMap
parameter_list|(
name|Cell
name|toKey
parameter_list|,
name|boolean
name|inclusive
parameter_list|)
block|{
if|if
condition|(
name|descending
condition|)
block|{
return|return
name|createSubCellFlatMap
argument_list|(
name|getValidIndex
argument_list|(
name|toKey
argument_list|,
name|inclusive
argument_list|,
literal|false
argument_list|)
argument_list|,
name|maxCellIdx
argument_list|,
name|descending
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|createSubCellFlatMap
argument_list|(
name|minCellIdx
argument_list|,
name|getValidIndex
argument_list|(
name|toKey
argument_list|,
name|inclusive
argument_list|,
literal|false
argument_list|)
argument_list|,
name|descending
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|NavigableMap
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
name|tailMap
parameter_list|(
name|Cell
name|fromKey
parameter_list|,
name|boolean
name|inclusive
parameter_list|)
block|{
if|if
condition|(
name|descending
condition|)
block|{
return|return
name|createSubCellFlatMap
argument_list|(
name|minCellIdx
argument_list|,
name|getValidIndex
argument_list|(
name|fromKey
argument_list|,
name|inclusive
argument_list|,
literal|true
argument_list|)
argument_list|,
name|descending
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|createSubCellFlatMap
argument_list|(
name|getValidIndex
argument_list|(
name|fromKey
argument_list|,
name|inclusive
argument_list|,
literal|true
argument_list|)
argument_list|,
name|maxCellIdx
argument_list|,
name|descending
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|NavigableMap
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
name|descendingMap
parameter_list|()
block|{
return|return
name|createSubCellFlatMap
argument_list|(
name|minCellIdx
argument_list|,
name|maxCellIdx
argument_list|,
literal|true
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|NavigableMap
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
name|subMap
parameter_list|(
name|Cell
name|k1
parameter_list|,
name|Cell
name|k2
parameter_list|)
block|{
return|return
name|this
operator|.
name|subMap
argument_list|(
name|k1
argument_list|,
literal|true
argument_list|,
name|k2
argument_list|,
literal|true
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|NavigableMap
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
name|headMap
parameter_list|(
name|Cell
name|k
parameter_list|)
block|{
return|return
name|this
operator|.
name|headMap
argument_list|(
name|k
argument_list|,
literal|true
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|NavigableMap
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
name|tailMap
parameter_list|(
name|Cell
name|k
parameter_list|)
block|{
return|return
name|this
operator|.
name|tailMap
argument_list|(
name|k
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|// -------------------------------- Key's getters --------------------------------
annotation|@
name|Override
specifier|public
name|Cell
name|firstKey
parameter_list|()
block|{
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|descending
condition|?
name|getCell
argument_list|(
name|maxCellIdx
operator|-
literal|1
argument_list|)
else|:
name|getCell
argument_list|(
name|minCellIdx
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|lastKey
parameter_list|()
block|{
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|descending
condition|?
name|getCell
argument_list|(
name|minCellIdx
argument_list|)
else|:
name|getCell
argument_list|(
name|maxCellIdx
operator|-
literal|1
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|lowerKey
parameter_list|(
name|Cell
name|k
parameter_list|)
block|{
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|index
init|=
name|find
argument_list|(
name|k
argument_list|)
decl_stmt|;
comment|// If index>=0 there's a key exactly equal
name|index
operator|=
operator|(
name|index
operator|>=
literal|0
operator|)
condition|?
name|index
operator|-
literal|1
else|:
operator|-
operator|(
name|index
operator|)
expr_stmt|;
return|return
operator|(
name|index
operator|<
name|minCellIdx
operator|||
name|index
operator|>=
name|maxCellIdx
operator|)
condition|?
literal|null
else|:
name|getCell
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|floorKey
parameter_list|(
name|Cell
name|k
parameter_list|)
block|{
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|index
init|=
name|find
argument_list|(
name|k
argument_list|)
decl_stmt|;
name|index
operator|=
operator|(
name|index
operator|>=
literal|0
operator|)
condition|?
name|index
else|:
operator|-
operator|(
name|index
operator|)
expr_stmt|;
return|return
operator|(
name|index
operator|<
name|minCellIdx
operator|||
name|index
operator|>=
name|maxCellIdx
operator|)
condition|?
literal|null
else|:
name|getCell
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|ceilingKey
parameter_list|(
name|Cell
name|k
parameter_list|)
block|{
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|index
init|=
name|find
argument_list|(
name|k
argument_list|)
decl_stmt|;
name|index
operator|=
operator|(
name|index
operator|>=
literal|0
operator|)
condition|?
name|index
else|:
operator|-
operator|(
name|index
operator|)
operator|+
literal|1
expr_stmt|;
return|return
operator|(
name|index
operator|<
name|minCellIdx
operator|||
name|index
operator|>=
name|maxCellIdx
operator|)
condition|?
literal|null
else|:
name|getCell
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|higherKey
parameter_list|(
name|Cell
name|k
parameter_list|)
block|{
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|index
init|=
name|find
argument_list|(
name|k
argument_list|)
decl_stmt|;
name|index
operator|=
operator|(
name|index
operator|>=
literal|0
operator|)
condition|?
name|index
operator|+
literal|1
else|:
operator|-
operator|(
name|index
operator|)
operator|+
literal|1
expr_stmt|;
return|return
operator|(
name|index
operator|<
name|minCellIdx
operator|||
name|index
operator|>=
name|maxCellIdx
operator|)
condition|?
literal|null
else|:
name|getCell
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|containsKey
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|int
name|index
init|=
name|find
argument_list|(
operator|(
name|Cell
operator|)
name|o
argument_list|)
decl_stmt|;
return|return
operator|(
name|index
operator|>=
literal|0
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|containsValue
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
comment|// use containsKey(Object o) instead
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Use containsKey(Object o) instead"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|get
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|int
name|index
init|=
name|find
argument_list|(
operator|(
name|Cell
operator|)
name|o
argument_list|)
decl_stmt|;
return|return
operator|(
name|index
operator|>=
literal|0
operator|)
condition|?
name|getCell
argument_list|(
name|index
argument_list|)
else|:
literal|null
return|;
block|}
comment|// -------------------------------- Entry's getters --------------------------------
specifier|private
specifier|static
class|class
name|CellFlatMapEntry
implements|implements
name|Entry
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
block|{
specifier|private
specifier|final
name|Cell
name|cell
decl_stmt|;
specifier|public
name|CellFlatMapEntry
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|this
operator|.
name|cell
operator|=
name|cell
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|getKey
parameter_list|()
block|{
return|return
name|cell
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|getValue
parameter_list|()
block|{
return|return
name|cell
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|setValue
parameter_list|(
name|Cell
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Entry
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
name|lowerEntry
parameter_list|(
name|Cell
name|k
parameter_list|)
block|{
name|Cell
name|cell
init|=
name|lowerKey
argument_list|(
name|k
argument_list|)
decl_stmt|;
if|if
condition|(
name|cell
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|CellFlatMapEntry
argument_list|(
name|cell
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Entry
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
name|higherEntry
parameter_list|(
name|Cell
name|k
parameter_list|)
block|{
name|Cell
name|cell
init|=
name|higherKey
argument_list|(
name|k
argument_list|)
decl_stmt|;
if|if
condition|(
name|cell
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|CellFlatMapEntry
argument_list|(
name|cell
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Entry
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
name|ceilingEntry
parameter_list|(
name|Cell
name|k
parameter_list|)
block|{
name|Cell
name|cell
init|=
name|ceilingKey
argument_list|(
name|k
argument_list|)
decl_stmt|;
if|if
condition|(
name|cell
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|CellFlatMapEntry
argument_list|(
name|cell
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Entry
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
name|floorEntry
parameter_list|(
name|Cell
name|k
parameter_list|)
block|{
name|Cell
name|cell
init|=
name|floorKey
argument_list|(
name|k
argument_list|)
decl_stmt|;
if|if
condition|(
name|cell
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|CellFlatMapEntry
argument_list|(
name|cell
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Entry
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
name|firstEntry
parameter_list|()
block|{
name|Cell
name|cell
init|=
name|firstKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|cell
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|CellFlatMapEntry
argument_list|(
name|cell
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Entry
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
name|lastEntry
parameter_list|()
block|{
name|Cell
name|cell
init|=
name|lastKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|cell
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|CellFlatMapEntry
argument_list|(
name|cell
argument_list|)
return|;
block|}
comment|// The following 2 methods (pollFirstEntry, pollLastEntry) are unsupported because these are updating methods.
annotation|@
name|Override
specifier|public
name|Entry
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
name|pollFirstEntry
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Entry
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
name|pollLastEntry
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
comment|// -------------------------------- Updates --------------------------------
comment|// All updating methods below are unsupported.
comment|// Assuming an array of Cells will be allocated externally,
comment|// fill up with Cells and provided in construction time.
comment|// Later the structure is immutable.
annotation|@
name|Override
specifier|public
name|Cell
name|put
parameter_list|(
name|Cell
name|k
parameter_list|,
name|Cell
name|v
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|remove
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|putAll
parameter_list|(
name|Map
argument_list|<
name|?
extends|extends
name|Cell
argument_list|,
name|?
extends|extends
name|Cell
argument_list|>
name|map
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
comment|// -------------------------------- Sub-Sets --------------------------------
annotation|@
name|Override
specifier|public
name|NavigableSet
argument_list|<
name|Cell
argument_list|>
name|navigableKeySet
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|NavigableSet
argument_list|<
name|Cell
argument_list|>
name|descendingKeySet
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|NavigableSet
argument_list|<
name|Cell
argument_list|>
name|keySet
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|Cell
argument_list|>
name|values
parameter_list|()
block|{
return|return
operator|new
name|CellFlatMapCollection
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|Entry
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
argument_list|>
name|entrySet
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
comment|// -------------------------------- Iterator K --------------------------------
specifier|private
specifier|final
class|class
name|CellFlatMapIterator
implements|implements
name|Iterator
argument_list|<
name|Cell
argument_list|>
block|{
name|int
name|index
decl_stmt|;
specifier|private
name|CellFlatMapIterator
parameter_list|()
block|{
name|index
operator|=
name|descending
condition|?
name|maxCellIdx
operator|-
literal|1
else|:
name|minCellIdx
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|descending
condition|?
operator|(
name|index
operator|>=
name|minCellIdx
operator|)
else|:
operator|(
name|index
operator|<
name|maxCellIdx
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|next
parameter_list|()
block|{
name|Cell
name|result
init|=
name|getCell
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|descending
condition|)
block|{
name|index
operator|--
expr_stmt|;
block|}
else|else
block|{
name|index
operator|++
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
comment|// -------------------------------- Collection --------------------------------
specifier|private
specifier|final
class|class
name|CellFlatMapCollection
implements|implements
name|Collection
argument_list|<
name|Cell
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|CellFlatMap
operator|.
name|this
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|CellFlatMap
operator|.
name|this
operator|.
name|isEmpty
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|contains
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
name|containsKey
argument_list|(
name|o
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|Cell
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|CellFlatMapIterator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
index|[]
name|toArray
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
index|[]
name|toArray
parameter_list|(
name|T
index|[]
name|ts
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|add
parameter_list|(
name|Cell
name|k
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|remove
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|containsAll
parameter_list|(
name|Collection
argument_list|<
name|?
argument_list|>
name|collection
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|addAll
parameter_list|(
name|Collection
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
name|collection
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|removeAll
parameter_list|(
name|Collection
argument_list|<
name|?
argument_list|>
name|collection
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|retainAll
parameter_list|(
name|Collection
argument_list|<
name|?
argument_list|>
name|collection
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
block|}
end_class

end_unit

