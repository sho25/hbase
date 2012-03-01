begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedSet
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
name|hadoop
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
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Simple {@link java.util.SortedSet} implementation that uses an internal  * {@link java.util.TreeSet} to provide ordering. All mutation operations  * create a new copy of the<code>TreeSet</code> instance, so are very  * expensive.  This class is only intended for use on small, very rarely  * written collections that expect highly concurrent reads. Read operations  * are performed on a reference to the internal<code>TreeSet</code> at the  * time of invocation, so will not see any mutations to the collection during  * their operation.  *  *<p>Note that due to the use of a {@link java.util.TreeSet} internally,  * a {@link java.util.Comparator} instance must be provided, or collection  * elements must implement {@link java.lang.Comparable}.  *</p>  * @param<E> A class implementing {@link java.lang.Comparable} or able to be  * compared by a provided comparator.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|SortedCopyOnWriteSet
parameter_list|<
name|E
parameter_list|>
implements|implements
name|SortedSet
argument_list|<
name|E
argument_list|>
block|{
specifier|private
name|SortedSet
argument_list|<
name|E
argument_list|>
name|internalSet
decl_stmt|;
specifier|public
name|SortedCopyOnWriteSet
parameter_list|()
block|{
name|this
operator|.
name|internalSet
operator|=
operator|new
name|TreeSet
argument_list|<
name|E
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|SortedCopyOnWriteSet
parameter_list|(
name|Collection
argument_list|<
name|?
extends|extends
name|E
argument_list|>
name|c
parameter_list|)
block|{
name|this
operator|.
name|internalSet
operator|=
operator|new
name|TreeSet
argument_list|<
name|E
argument_list|>
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SortedCopyOnWriteSet
parameter_list|(
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
name|internalSet
operator|=
operator|new
name|TreeSet
argument_list|<
name|E
argument_list|>
argument_list|(
name|comparator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|internalSet
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
name|internalSet
operator|.
name|isEmpty
argument_list|()
return|;
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
name|internalSet
operator|.
name|contains
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
name|E
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|internalSet
operator|.
name|iterator
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
return|return
name|internalSet
operator|.
name|toArray
argument_list|()
return|;
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
name|a
parameter_list|)
block|{
return|return
name|internalSet
operator|.
name|toArray
argument_list|(
name|a
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|boolean
name|add
parameter_list|(
name|E
name|e
parameter_list|)
block|{
name|SortedSet
argument_list|<
name|E
argument_list|>
name|newSet
init|=
operator|new
name|TreeSet
argument_list|<
name|E
argument_list|>
argument_list|(
name|internalSet
argument_list|)
decl_stmt|;
name|boolean
name|added
init|=
name|newSet
operator|.
name|add
argument_list|(
name|e
argument_list|)
decl_stmt|;
name|internalSet
operator|=
name|newSet
expr_stmt|;
return|return
name|added
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|boolean
name|remove
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|SortedSet
argument_list|<
name|E
argument_list|>
name|newSet
init|=
operator|new
name|TreeSet
argument_list|<
name|E
argument_list|>
argument_list|(
name|internalSet
argument_list|)
decl_stmt|;
name|boolean
name|removed
init|=
name|newSet
operator|.
name|remove
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|internalSet
operator|=
name|newSet
expr_stmt|;
return|return
name|removed
return|;
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
name|c
parameter_list|)
block|{
return|return
name|internalSet
operator|.
name|containsAll
argument_list|(
name|c
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|boolean
name|addAll
parameter_list|(
name|Collection
argument_list|<
name|?
extends|extends
name|E
argument_list|>
name|c
parameter_list|)
block|{
name|SortedSet
argument_list|<
name|E
argument_list|>
name|newSet
init|=
operator|new
name|TreeSet
argument_list|<
name|E
argument_list|>
argument_list|(
name|internalSet
argument_list|)
decl_stmt|;
name|boolean
name|changed
init|=
name|newSet
operator|.
name|addAll
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|internalSet
operator|=
name|newSet
expr_stmt|;
return|return
name|changed
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|boolean
name|retainAll
parameter_list|(
name|Collection
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
name|SortedSet
argument_list|<
name|E
argument_list|>
name|newSet
init|=
operator|new
name|TreeSet
argument_list|<
name|E
argument_list|>
argument_list|(
name|internalSet
argument_list|)
decl_stmt|;
name|boolean
name|changed
init|=
name|newSet
operator|.
name|retainAll
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|internalSet
operator|=
name|newSet
expr_stmt|;
return|return
name|changed
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|boolean
name|removeAll
parameter_list|(
name|Collection
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
name|SortedSet
argument_list|<
name|E
argument_list|>
name|newSet
init|=
operator|new
name|TreeSet
argument_list|<
name|E
argument_list|>
argument_list|(
name|internalSet
argument_list|)
decl_stmt|;
name|boolean
name|changed
init|=
name|newSet
operator|.
name|removeAll
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|internalSet
operator|=
name|newSet
expr_stmt|;
return|return
name|changed
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|clear
parameter_list|()
block|{
name|Comparator
argument_list|<
name|?
super|super
name|E
argument_list|>
name|comparator
init|=
name|internalSet
operator|.
name|comparator
argument_list|()
decl_stmt|;
if|if
condition|(
name|comparator
operator|!=
literal|null
condition|)
block|{
name|internalSet
operator|=
operator|new
name|TreeSet
argument_list|<
name|E
argument_list|>
argument_list|(
name|comparator
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|internalSet
operator|=
operator|new
name|TreeSet
argument_list|<
name|E
argument_list|>
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
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
name|internalSet
operator|.
name|comparator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|SortedSet
argument_list|<
name|E
argument_list|>
name|subSet
parameter_list|(
name|E
name|fromElement
parameter_list|,
name|E
name|toElement
parameter_list|)
block|{
return|return
name|internalSet
operator|.
name|subSet
argument_list|(
name|fromElement
argument_list|,
name|toElement
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SortedSet
argument_list|<
name|E
argument_list|>
name|headSet
parameter_list|(
name|E
name|toElement
parameter_list|)
block|{
return|return
name|internalSet
operator|.
name|headSet
argument_list|(
name|toElement
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SortedSet
argument_list|<
name|E
argument_list|>
name|tailSet
parameter_list|(
name|E
name|fromElement
parameter_list|)
block|{
return|return
name|internalSet
operator|.
name|tailSet
argument_list|(
name|fromElement
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|E
name|first
parameter_list|()
block|{
return|return
name|internalSet
operator|.
name|first
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|E
name|last
parameter_list|()
block|{
return|return
name|internalSet
operator|.
name|last
argument_list|()
return|;
block|}
block|}
end_class

end_unit

