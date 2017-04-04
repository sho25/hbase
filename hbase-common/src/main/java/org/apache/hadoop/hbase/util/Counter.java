begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLongFieldUpdater
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReference
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
comment|/**  * High scalable counter. Thread safe.  * @deprecated use {@link java.util.concurrent.atomic.LongAdder} instead.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|Deprecated
specifier|public
class|class
name|Counter
block|{
specifier|private
specifier|static
specifier|final
name|int
name|MAX_CELLS_LENGTH
init|=
literal|1
operator|<<
literal|20
decl_stmt|;
specifier|private
specifier|static
class|class
name|Cell
block|{
comment|// Pads are added around the value to avoid cache-line contention with
comment|// another cell's value. The cache-line size is expected to be equal to or
comment|// less than about 128 Bytes (= 64 Bits * 16).
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|volatile
name|long
name|p0
decl_stmt|,
name|p1
decl_stmt|,
name|p2
decl_stmt|,
name|p3
decl_stmt|,
name|p4
decl_stmt|,
name|p5
decl_stmt|,
name|p6
decl_stmt|;
specifier|volatile
name|long
name|value
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|volatile
name|long
name|q0
decl_stmt|,
name|q1
decl_stmt|,
name|q2
decl_stmt|,
name|q3
decl_stmt|,
name|q4
decl_stmt|,
name|q5
decl_stmt|,
name|q6
decl_stmt|;
specifier|static
specifier|final
name|AtomicLongFieldUpdater
argument_list|<
name|Cell
argument_list|>
name|valueUpdater
init|=
name|AtomicLongFieldUpdater
operator|.
name|newUpdater
argument_list|(
name|Cell
operator|.
name|class
argument_list|,
literal|"value"
argument_list|)
decl_stmt|;
name|Cell
parameter_list|()
block|{}
name|Cell
parameter_list|(
name|long
name|initValue
parameter_list|)
block|{
name|value
operator|=
name|initValue
expr_stmt|;
block|}
name|long
name|get
parameter_list|()
block|{
return|return
name|value
return|;
block|}
name|boolean
name|add
parameter_list|(
name|long
name|delta
parameter_list|)
block|{
name|long
name|current
init|=
name|value
decl_stmt|;
return|return
name|valueUpdater
operator|.
name|compareAndSet
argument_list|(
name|this
argument_list|,
name|current
argument_list|,
name|current
operator|+
name|delta
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|Container
block|{
comment|/** The length should be a power of 2. */
specifier|final
name|Cell
index|[]
name|cells
decl_stmt|;
comment|/** True if a new extended container is going to replace this. */
specifier|final
name|AtomicBoolean
name|demoted
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
name|Container
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|Cell
index|[]
block|{
name|cell
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**      * @param cells the length should be a power of 2      */
name|Container
parameter_list|(
name|Cell
index|[]
name|cells
parameter_list|)
block|{
name|this
operator|.
name|cells
operator|=
name|cells
expr_stmt|;
block|}
block|}
specifier|private
specifier|final
name|AtomicReference
argument_list|<
name|Container
argument_list|>
name|containerRef
decl_stmt|;
specifier|public
name|Counter
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|Cell
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Counter
parameter_list|(
name|long
name|initValue
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|Cell
argument_list|(
name|initValue
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Counter
parameter_list|(
name|Cell
name|initCell
parameter_list|)
block|{
name|containerRef
operator|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|(
operator|new
name|Container
argument_list|(
name|initCell
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|int
name|hash
parameter_list|()
block|{
comment|// The logic is borrowed from high-scale-lib's ConcurrentAutoTable.
name|int
name|h
init|=
name|System
operator|.
name|identityHashCode
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
argument_list|)
decl_stmt|;
comment|// You would think that System.identityHashCode on the current thread
comment|// would be a good hash fcn, but actually on SunOS 5.8 it is pretty lousy
comment|// in the low bits.
name|h
operator|^=
operator|(
name|h
operator|>>>
literal|20
operator|)
operator|^
operator|(
name|h
operator|>>>
literal|12
operator|)
expr_stmt|;
comment|// Bit spreader, borrowed from Doug Lea
name|h
operator|^=
operator|(
name|h
operator|>>>
literal|7
operator|)
operator|^
operator|(
name|h
operator|>>>
literal|4
operator|)
expr_stmt|;
return|return
name|h
return|;
block|}
specifier|public
name|void
name|add
parameter_list|(
name|long
name|delta
parameter_list|)
block|{
name|Container
name|container
init|=
name|containerRef
operator|.
name|get
argument_list|()
decl_stmt|;
name|Cell
index|[]
name|cells
init|=
name|container
operator|.
name|cells
decl_stmt|;
name|int
name|mask
init|=
name|cells
operator|.
name|length
operator|-
literal|1
decl_stmt|;
name|int
name|baseIndex
init|=
name|hash
argument_list|()
decl_stmt|;
if|if
condition|(
name|cells
index|[
name|baseIndex
operator|&
name|mask
index|]
operator|.
name|add
argument_list|(
name|delta
argument_list|)
condition|)
block|{
return|return;
block|}
name|int
name|index
init|=
name|baseIndex
operator|+
literal|1
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|cells
index|[
name|index
operator|&
name|mask
index|]
operator|.
name|add
argument_list|(
name|delta
argument_list|)
condition|)
block|{
break|break;
block|}
name|index
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|index
operator|-
name|baseIndex
operator|>=
name|cells
operator|.
name|length
operator|&&
name|cells
operator|.
name|length
operator|<
name|MAX_CELLS_LENGTH
operator|&&
name|container
operator|.
name|demoted
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
if|if
condition|(
name|containerRef
operator|.
name|get
argument_list|()
operator|==
name|container
condition|)
block|{
name|Cell
index|[]
name|newCells
init|=
operator|new
name|Cell
index|[
name|cells
operator|.
name|length
operator|*
literal|2
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|cells
argument_list|,
literal|0
argument_list|,
name|newCells
argument_list|,
literal|0
argument_list|,
name|cells
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
name|cells
operator|.
name|length
init|;
name|i
operator|<
name|newCells
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|newCells
index|[
name|i
index|]
operator|=
operator|new
name|Cell
argument_list|()
expr_stmt|;
comment|// Fill all of the elements with instances. Creating a cell on demand
comment|// and putting it into the array makes a concurrent problem about
comment|// visibility or, in other words, happens-before relation, because
comment|// each element of the array is not volatile so that you should
comment|// establish the relation by some piggybacking.
block|}
name|containerRef
operator|.
name|compareAndSet
argument_list|(
name|container
argument_list|,
operator|new
name|Container
argument_list|(
name|newCells
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|increment
parameter_list|()
block|{
name|add
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|decrement
parameter_list|()
block|{
name|add
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|containerRef
operator|.
name|set
argument_list|(
operator|new
name|Container
argument_list|(
operator|new
name|Cell
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|long
name|get
parameter_list|()
block|{
name|long
name|sum
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|containerRef
operator|.
name|get
argument_list|()
operator|.
name|cells
control|)
block|{
name|sum
operator|+=
name|cell
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
return|return
name|sum
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|Cell
index|[]
name|cells
init|=
name|containerRef
operator|.
name|get
argument_list|()
operator|.
name|cells
decl_stmt|;
name|long
name|min
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|long
name|max
init|=
name|Long
operator|.
name|MIN_VALUE
decl_stmt|;
name|long
name|sum
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
name|long
name|value
init|=
name|cell
operator|.
name|get
argument_list|()
decl_stmt|;
name|sum
operator|+=
name|value
expr_stmt|;
if|if
condition|(
name|min
operator|>
name|value
condition|)
block|{
name|min
operator|=
name|value
expr_stmt|;
block|}
if|if
condition|(
name|max
operator|<
name|value
condition|)
block|{
name|max
operator|=
name|value
expr_stmt|;
block|}
block|}
return|return
operator|new
name|StringBuilder
argument_list|(
literal|100
argument_list|)
operator|.
name|append
argument_list|(
literal|"[value="
argument_list|)
operator|.
name|append
argument_list|(
name|sum
argument_list|)
operator|.
name|append
argument_list|(
literal|", cells=[length="
argument_list|)
operator|.
name|append
argument_list|(
name|cells
operator|.
name|length
argument_list|)
operator|.
name|append
argument_list|(
literal|", min="
argument_list|)
operator|.
name|append
argument_list|(
name|min
argument_list|)
operator|.
name|append
argument_list|(
literal|", max="
argument_list|)
operator|.
name|append
argument_list|(
name|max
argument_list|)
operator|.
name|append
argument_list|(
literal|"]]"
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

