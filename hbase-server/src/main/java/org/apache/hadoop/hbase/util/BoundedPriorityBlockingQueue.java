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
name|locks
operator|.
name|Condition
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
name|locks
operator|.
name|ReentrantLock
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
name|BlockingQueue
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
name|TimeUnit
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
name|AbstractQueue
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * A generic bounded blocking Priority-Queue.  *  * The elements of the priority queue are ordered according to the Comparator  * provided at queue construction time.  *  * If multiple elements have the same priority this queue orders them in  * FIFO (first-in-first-out) manner.  * The head of this queue is the least element with respect to the specified  * ordering. If multiple elements are tied for least value, the head is the  * first one inserted.  * The queue retrieval operations poll, remove, peek, and element access the  * element at the head of the queue.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|BoundedPriorityBlockingQueue
parameter_list|<
name|E
parameter_list|>
extends|extends
name|AbstractQueue
argument_list|<
name|E
argument_list|>
implements|implements
name|BlockingQueue
argument_list|<
name|E
argument_list|>
block|{
specifier|private
specifier|static
class|class
name|PriorityQueue
parameter_list|<
name|E
parameter_list|>
block|{
specifier|private
specifier|final
name|Comparator
argument_list|<
name|?
super|super
name|E
argument_list|>
name|comparator
decl_stmt|;
specifier|private
specifier|final
name|E
index|[]
name|objects
decl_stmt|;
specifier|private
name|int
name|head
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|tail
init|=
literal|0
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|PriorityQueue
parameter_list|(
name|int
name|capacity
parameter_list|,
name|Comparator
argument_list|<
name|?
super|super
name|E
argument_list|>
name|comparator
parameter_list|)
block|{
name|this
operator|.
name|objects
operator|=
operator|(
name|E
index|[]
operator|)
operator|new
name|Object
index|[
name|capacity
index|]
expr_stmt|;
name|this
operator|.
name|comparator
operator|=
name|comparator
expr_stmt|;
block|}
specifier|public
name|void
name|add
parameter_list|(
name|E
name|elem
parameter_list|)
block|{
if|if
condition|(
name|tail
operator|==
name|objects
operator|.
name|length
condition|)
block|{
comment|// shift down |-----AAAAAAA|
name|tail
operator|-=
name|head
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|objects
argument_list|,
name|head
argument_list|,
name|objects
argument_list|,
literal|0
argument_list|,
name|tail
argument_list|)
expr_stmt|;
name|head
operator|=
literal|0
expr_stmt|;
block|}
if|if
condition|(
name|tail
operator|==
name|head
operator|||
name|comparator
operator|.
name|compare
argument_list|(
name|objects
index|[
name|tail
operator|-
literal|1
index|]
argument_list|,
name|elem
argument_list|)
operator|<=
literal|0
condition|)
block|{
comment|// Append
name|objects
index|[
name|tail
operator|++
index|]
operator|=
name|elem
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|head
operator|>
literal|0
operator|&&
name|comparator
operator|.
name|compare
argument_list|(
name|objects
index|[
name|head
index|]
argument_list|,
name|elem
argument_list|)
operator|>
literal|0
condition|)
block|{
comment|// Prepend
name|objects
index|[
operator|--
name|head
index|]
operator|=
name|elem
expr_stmt|;
block|}
else|else
block|{
comment|// Insert in the middle
name|int
name|index
init|=
name|upperBound
argument_list|(
name|head
argument_list|,
name|tail
operator|-
literal|1
argument_list|,
name|elem
argument_list|)
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|objects
argument_list|,
name|index
argument_list|,
name|objects
argument_list|,
name|index
operator|+
literal|1
argument_list|,
name|tail
operator|-
name|index
argument_list|)
expr_stmt|;
name|objects
index|[
name|index
index|]
operator|=
name|elem
expr_stmt|;
name|tail
operator|++
expr_stmt|;
block|}
block|}
specifier|public
name|E
name|peek
parameter_list|()
block|{
return|return
operator|(
name|head
operator|!=
name|tail
operator|)
condition|?
name|objects
index|[
name|head
index|]
else|:
literal|null
return|;
block|}
specifier|public
name|E
name|poll
parameter_list|()
block|{
name|E
name|elem
init|=
name|objects
index|[
name|head
index|]
decl_stmt|;
name|objects
index|[
name|head
index|]
operator|=
literal|null
expr_stmt|;
name|head
operator|=
operator|(
name|head
operator|+
literal|1
operator|)
operator|%
name|objects
operator|.
name|length
expr_stmt|;
if|if
condition|(
name|head
operator|==
literal|0
condition|)
name|tail
operator|=
literal|0
expr_stmt|;
return|return
name|elem
return|;
block|}
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|tail
operator|-
name|head
return|;
block|}
specifier|public
name|Comparator
argument_list|<
name|?
super|super
name|E
argument_list|>
name|comparator
parameter_list|()
block|{
return|return
name|this
operator|.
name|comparator
return|;
block|}
specifier|public
name|boolean
name|contains
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
name|head
init|;
name|i
operator|<
name|tail
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|objects
index|[
name|i
index|]
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|public
name|int
name|remainingCapacity
parameter_list|()
block|{
return|return
name|this
operator|.
name|objects
operator|.
name|length
operator|-
operator|(
name|tail
operator|-
name|head
operator|)
return|;
block|}
specifier|private
name|int
name|upperBound
parameter_list|(
name|int
name|start
parameter_list|,
name|int
name|end
parameter_list|,
name|E
name|key
parameter_list|)
block|{
while|while
condition|(
name|start
operator|<
name|end
condition|)
block|{
name|int
name|mid
init|=
operator|(
name|start
operator|+
name|end
operator|)
operator|>>>
literal|1
decl_stmt|;
name|E
name|mitem
init|=
name|objects
index|[
name|mid
index|]
decl_stmt|;
name|int
name|cmp
init|=
name|comparator
operator|.
name|compare
argument_list|(
name|mitem
argument_list|,
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|>
literal|0
condition|)
block|{
name|end
operator|=
name|mid
expr_stmt|;
block|}
else|else
block|{
name|start
operator|=
name|mid
operator|+
literal|1
expr_stmt|;
block|}
block|}
return|return
name|start
return|;
block|}
block|}
comment|// Lock used for all operations
specifier|private
specifier|final
name|ReentrantLock
name|lock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
comment|// Condition for blocking when empty
specifier|private
specifier|final
name|Condition
name|notEmpty
init|=
name|lock
operator|.
name|newCondition
argument_list|()
decl_stmt|;
comment|// Wait queue for waiting puts
specifier|private
specifier|final
name|Condition
name|notFull
init|=
name|lock
operator|.
name|newCondition
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|PriorityQueue
argument_list|<
name|E
argument_list|>
name|queue
decl_stmt|;
comment|/**    * Creates a PriorityQueue with the specified capacity that orders its    * elements according to the specified comparator.    * @param capacity the capacity of this queue    * @param comparator the comparator that will be used to order this priority queue    */
specifier|public
name|BoundedPriorityBlockingQueue
parameter_list|(
name|int
name|capacity
parameter_list|,
name|Comparator
argument_list|<
name|?
super|super
name|E
argument_list|>
name|comparator
parameter_list|)
block|{
name|this
operator|.
name|queue
operator|=
operator|new
name|PriorityQueue
argument_list|<>
argument_list|(
name|capacity
argument_list|,
name|comparator
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|offer
parameter_list|(
name|E
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|==
literal|null
condition|)
throw|throw
operator|new
name|NullPointerException
argument_list|()
throw|;
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
name|queue
operator|.
name|remainingCapacity
argument_list|()
operator|>
literal|0
condition|)
block|{
name|this
operator|.
name|queue
operator|.
name|add
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|notEmpty
operator|.
name|signal
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
specifier|public
name|void
name|put
parameter_list|(
name|E
name|e
parameter_list|)
throws|throws
name|InterruptedException
block|{
if|if
condition|(
name|e
operator|==
literal|null
condition|)
throw|throw
operator|new
name|NullPointerException
argument_list|()
throw|;
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
while|while
condition|(
name|queue
operator|.
name|remainingCapacity
argument_list|()
operator|==
literal|0
condition|)
block|{
name|notFull
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|queue
operator|.
name|add
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|notEmpty
operator|.
name|signal
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|boolean
name|offer
parameter_list|(
name|E
name|e
parameter_list|,
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|InterruptedException
block|{
if|if
condition|(
name|e
operator|==
literal|null
condition|)
throw|throw
operator|new
name|NullPointerException
argument_list|()
throw|;
name|long
name|nanos
init|=
name|unit
operator|.
name|toNanos
argument_list|(
name|timeout
argument_list|)
decl_stmt|;
name|lock
operator|.
name|lockInterruptibly
argument_list|()
expr_stmt|;
try|try
block|{
while|while
condition|(
name|queue
operator|.
name|remainingCapacity
argument_list|()
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|nanos
operator|<=
literal|0
condition|)
return|return
literal|false
return|;
name|nanos
operator|=
name|notFull
operator|.
name|awaitNanos
argument_list|(
name|nanos
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|queue
operator|.
name|add
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|notEmpty
operator|.
name|signal
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|E
name|take
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|E
name|result
init|=
literal|null
decl_stmt|;
name|lock
operator|.
name|lockInterruptibly
argument_list|()
expr_stmt|;
try|try
block|{
while|while
condition|(
name|queue
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|notEmpty
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
name|result
operator|=
name|queue
operator|.
name|poll
argument_list|()
expr_stmt|;
name|notFull
operator|.
name|signal
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
name|E
name|poll
parameter_list|()
block|{
name|E
name|result
init|=
literal|null
decl_stmt|;
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
name|queue
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|result
operator|=
name|queue
operator|.
name|poll
argument_list|()
expr_stmt|;
name|notFull
operator|.
name|signal
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
name|E
name|poll
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|long
name|nanos
init|=
name|unit
operator|.
name|toNanos
argument_list|(
name|timeout
argument_list|)
decl_stmt|;
name|lock
operator|.
name|lockInterruptibly
argument_list|()
expr_stmt|;
name|E
name|result
init|=
literal|null
decl_stmt|;
try|try
block|{
while|while
condition|(
name|queue
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|&&
name|nanos
operator|>
literal|0
condition|)
block|{
name|nanos
operator|=
name|notEmpty
operator|.
name|awaitNanos
argument_list|(
name|nanos
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|queue
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|result
operator|=
name|queue
operator|.
name|poll
argument_list|()
expr_stmt|;
block|}
name|notFull
operator|.
name|signal
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
name|E
name|peek
parameter_list|()
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|queue
operator|.
name|peek
argument_list|()
return|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|int
name|size
parameter_list|()
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|queue
operator|.
name|size
argument_list|()
return|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|Iterator
argument_list|<
name|E
argument_list|>
name|iterator
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
name|Comparator
argument_list|<
name|?
super|super
name|E
argument_list|>
name|comparator
parameter_list|()
block|{
return|return
name|queue
operator|.
name|comparator
argument_list|()
return|;
block|}
specifier|public
name|int
name|remainingCapacity
parameter_list|()
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|queue
operator|.
name|remainingCapacity
argument_list|()
return|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
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
specifier|public
name|boolean
name|contains
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|queue
operator|.
name|contains
argument_list|(
name|o
argument_list|)
return|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|int
name|drainTo
parameter_list|(
name|Collection
argument_list|<
name|?
super|super
name|E
argument_list|>
name|c
parameter_list|)
block|{
return|return
name|drainTo
argument_list|(
name|c
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
return|;
block|}
specifier|public
name|int
name|drainTo
parameter_list|(
name|Collection
argument_list|<
name|?
super|super
name|E
argument_list|>
name|c
parameter_list|,
name|int
name|maxElements
parameter_list|)
block|{
if|if
condition|(
name|c
operator|==
literal|null
condition|)
throw|throw
operator|new
name|NullPointerException
argument_list|()
throw|;
if|if
condition|(
name|c
operator|==
name|this
condition|)
throw|throw
operator|new
name|IllegalArgumentException
argument_list|()
throw|;
if|if
condition|(
name|maxElements
operator|<=
literal|0
condition|)
return|return
literal|0
return|;
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|int
name|n
init|=
name|Math
operator|.
name|min
argument_list|(
name|queue
operator|.
name|size
argument_list|()
argument_list|,
name|maxElements
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|n
condition|;
operator|++
name|i
control|)
block|{
name|c
operator|.
name|add
argument_list|(
name|queue
operator|.
name|poll
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|n
return|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

