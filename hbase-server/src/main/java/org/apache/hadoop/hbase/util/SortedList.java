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
name|ArrayList
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
name|Collections
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ListIterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|RandomAccess
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

begin_comment
comment|/**  * Simple sorted list implementation that uses {@link java.util.ArrayList} as  * the underlying collection so we can support RandomAccess. All mutations  * create a new copy of the<code>ArrayList</code> instance, so can be  * expensive. This class is only intended for use on small, very rarely  * written collections that expect highly concurrent reads.  *<p>  * Read operations are performed on a reference to the internal list at the  * time of invocation, so will not see any mutations to the collection during  * their operation. Iterating over list elements manually using the  * RandomAccess pattern involves multiple operations. For this to be safe get  * a reference to the internal list first using get().  *<p>  * If constructed with a {@link java.util.Comparator}, the list will be sorted  * using the comparator. Adding or changing an element using an index will  * trigger a resort.  *<p>  * Iterators are read-only. They cannot be used to remove elements.  */
end_comment

begin_class
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"UG_SYNC_SET_UNSYNC_GET"
argument_list|,
name|justification
operator|=
literal|"TODO: synchronization in here needs review!!!"
argument_list|)
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SortedList
parameter_list|<
name|E
parameter_list|>
implements|implements
name|List
argument_list|<
name|E
argument_list|>
implements|,
name|RandomAccess
block|{
specifier|private
specifier|volatile
name|List
argument_list|<
name|E
argument_list|>
name|list
decl_stmt|;
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
comment|/**    * Constructs an empty list with the default initial capacity that will be    * sorted using the given comparator.    *    * @param comparator the comparator    */
specifier|public
name|SortedList
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
name|list
operator|=
name|Collections
operator|.
name|emptyList
argument_list|()
expr_stmt|;
name|this
operator|.
name|comparator
operator|=
name|comparator
expr_stmt|;
block|}
comment|/**    * Constructs a list containing the elements of the given collection, in the    * order returned by the collection's iterator, that will be sorted with the    * given comparator.    *    * @param c the collection    * @param comparator the comparator    */
specifier|public
name|SortedList
parameter_list|(
name|Collection
argument_list|<
name|?
extends|extends
name|E
argument_list|>
name|c
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
name|list
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|E
argument_list|>
argument_list|(
name|c
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|comparator
operator|=
name|comparator
expr_stmt|;
block|}
comment|/**    * Returns a reference to the unmodifiable list currently backing the SortedList.    * Changes to the SortedList will not be reflected in this list. Use this    * method to get a reference for iterating over using the RandomAccess    * pattern.    */
specifier|public
name|List
argument_list|<
name|E
argument_list|>
name|get
parameter_list|()
block|{
comment|// FindBugs: UG_SYNC_SET_UNSYNC_GET complaint. Fix!!
return|return
name|list
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
name|list
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
name|list
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
name|list
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
name|list
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
name|list
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
name|list
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
name|ArrayList
argument_list|<
name|E
argument_list|>
name|newList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|list
argument_list|)
decl_stmt|;
name|boolean
name|changed
init|=
name|newList
operator|.
name|add
argument_list|(
name|e
argument_list|)
decl_stmt|;
if|if
condition|(
name|changed
condition|)
block|{
name|Collections
operator|.
name|sort
argument_list|(
name|newList
argument_list|,
name|comparator
argument_list|)
expr_stmt|;
block|}
name|list
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|newList
argument_list|)
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
name|remove
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|E
argument_list|>
name|newList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|list
argument_list|)
decl_stmt|;
comment|// Removals in ArrayList won't break sorting
name|boolean
name|changed
init|=
name|newList
operator|.
name|remove
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|list
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|newList
argument_list|)
expr_stmt|;
return|return
name|changed
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
name|list
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
name|ArrayList
argument_list|<
name|E
argument_list|>
name|newList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|list
argument_list|)
decl_stmt|;
name|boolean
name|changed
init|=
name|newList
operator|.
name|addAll
argument_list|(
name|c
argument_list|)
decl_stmt|;
if|if
condition|(
name|changed
condition|)
block|{
name|Collections
operator|.
name|sort
argument_list|(
name|newList
argument_list|,
name|comparator
argument_list|)
expr_stmt|;
block|}
name|list
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|newList
argument_list|)
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
name|addAll
parameter_list|(
name|int
name|index
parameter_list|,
name|Collection
argument_list|<
name|?
extends|extends
name|E
argument_list|>
name|c
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|E
argument_list|>
name|newList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|list
argument_list|)
decl_stmt|;
name|boolean
name|changed
init|=
name|newList
operator|.
name|addAll
argument_list|(
name|index
argument_list|,
name|c
argument_list|)
decl_stmt|;
if|if
condition|(
name|changed
condition|)
block|{
name|Collections
operator|.
name|sort
argument_list|(
name|newList
argument_list|,
name|comparator
argument_list|)
expr_stmt|;
block|}
name|list
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|newList
argument_list|)
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
name|ArrayList
argument_list|<
name|E
argument_list|>
name|newList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|list
argument_list|)
decl_stmt|;
comment|// Removals in ArrayList won't break sorting
name|boolean
name|changed
init|=
name|newList
operator|.
name|removeAll
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|list
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|newList
argument_list|)
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
name|ArrayList
argument_list|<
name|E
argument_list|>
name|newList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|list
argument_list|)
decl_stmt|;
comment|// Removals in ArrayList won't break sorting
name|boolean
name|changed
init|=
name|newList
operator|.
name|retainAll
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|list
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|newList
argument_list|)
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
name|list
operator|=
name|Collections
operator|.
name|emptyList
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|E
name|get
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|list
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|E
name|set
parameter_list|(
name|int
name|index
parameter_list|,
name|E
name|element
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|E
argument_list|>
name|newList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|list
argument_list|)
decl_stmt|;
name|E
name|result
init|=
name|newList
operator|.
name|set
argument_list|(
name|index
argument_list|,
name|element
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|list
argument_list|,
name|comparator
argument_list|)
expr_stmt|;
name|list
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|newList
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|add
parameter_list|(
name|int
name|index
parameter_list|,
name|E
name|element
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|E
argument_list|>
name|newList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|list
argument_list|)
decl_stmt|;
name|newList
operator|.
name|add
argument_list|(
name|index
argument_list|,
name|element
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|list
argument_list|,
name|comparator
argument_list|)
expr_stmt|;
name|list
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|newList
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|E
name|remove
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|E
argument_list|>
name|newList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|list
argument_list|)
decl_stmt|;
comment|// Removals in ArrayList won't break sorting
name|E
name|result
init|=
name|newList
operator|.
name|remove
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|list
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|newList
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|indexOf
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
name|list
operator|.
name|indexOf
argument_list|(
name|o
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|lastIndexOf
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
name|list
operator|.
name|lastIndexOf
argument_list|(
name|o
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ListIterator
argument_list|<
name|E
argument_list|>
name|listIterator
parameter_list|()
block|{
return|return
name|list
operator|.
name|listIterator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ListIterator
argument_list|<
name|E
argument_list|>
name|listIterator
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|list
operator|.
name|listIterator
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|E
argument_list|>
name|subList
parameter_list|(
name|int
name|fromIndex
parameter_list|,
name|int
name|toIndex
parameter_list|)
block|{
return|return
name|list
operator|.
name|subList
argument_list|(
name|fromIndex
argument_list|,
name|toIndex
argument_list|)
return|;
block|}
block|}
end_class

end_unit

