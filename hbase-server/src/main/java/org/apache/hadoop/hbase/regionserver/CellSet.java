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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|SortedSet
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
name|ConcurrentSkipListMap
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

begin_comment
comment|/**  * A {@link java.util.Set} of {@link Cell}s, where an add will overwrite the entry if already  * exists in the set.  The call to add returns true if no value in the backing map or false if  * there was an entry with same key (though value may be different).  * implementation is tolerant of concurrent get and set and won't throw  * ConcurrentModificationException when iterating.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CellSet
implements|implements
name|NavigableSet
argument_list|<
name|Cell
argument_list|>
block|{
comment|// Implemented on top of a {@link java.util.concurrent.ConcurrentSkipListMap}
comment|// Differ from CSLS in one respect, where CSLS does "Adds the specified element to this set if it
comment|// is not already present.", this implementation "Adds the specified element to this set EVEN
comment|// if it is already present overwriting what was there previous".
comment|// Otherwise, has same attributes as ConcurrentSkipListSet
specifier|private
specifier|final
name|NavigableMap
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
name|delegatee
decl_stmt|;
comment|///
name|CellSet
parameter_list|(
specifier|final
name|CellComparator
name|c
parameter_list|)
block|{
name|this
operator|.
name|delegatee
operator|=
operator|new
name|ConcurrentSkipListMap
argument_list|<>
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
name|CellSet
parameter_list|(
specifier|final
name|NavigableMap
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
name|m
parameter_list|)
block|{
name|this
operator|.
name|delegatee
operator|=
name|m
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|NavigableMap
argument_list|<
name|Cell
argument_list|,
name|Cell
argument_list|>
name|getDelegatee
parameter_list|()
block|{
return|return
name|delegatee
return|;
block|}
specifier|public
name|Cell
name|ceiling
parameter_list|(
name|Cell
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
specifier|public
name|Iterator
argument_list|<
name|Cell
argument_list|>
name|descendingIterator
parameter_list|()
block|{
return|return
name|this
operator|.
name|delegatee
operator|.
name|descendingMap
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
specifier|public
name|NavigableSet
argument_list|<
name|Cell
argument_list|>
name|descendingSet
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
specifier|public
name|Cell
name|floor
parameter_list|(
name|Cell
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
specifier|public
name|SortedSet
argument_list|<
name|Cell
argument_list|>
name|headSet
parameter_list|(
specifier|final
name|Cell
name|toElement
parameter_list|)
block|{
return|return
name|headSet
argument_list|(
name|toElement
argument_list|,
literal|false
argument_list|)
return|;
block|}
specifier|public
name|NavigableSet
argument_list|<
name|Cell
argument_list|>
name|headSet
parameter_list|(
specifier|final
name|Cell
name|toElement
parameter_list|,
name|boolean
name|inclusive
parameter_list|)
block|{
return|return
operator|new
name|CellSet
argument_list|(
name|this
operator|.
name|delegatee
operator|.
name|headMap
argument_list|(
name|toElement
argument_list|,
name|inclusive
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|Cell
name|higher
parameter_list|(
name|Cell
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
specifier|public
name|Iterator
argument_list|<
name|Cell
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|this
operator|.
name|delegatee
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
specifier|public
name|Cell
name|lower
parameter_list|(
name|Cell
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
specifier|public
name|Cell
name|pollFirst
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
specifier|public
name|Cell
name|pollLast
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
specifier|public
name|SortedSet
argument_list|<
name|Cell
argument_list|>
name|subSet
parameter_list|(
name|Cell
name|fromElement
parameter_list|,
name|Cell
name|toElement
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
specifier|public
name|NavigableSet
argument_list|<
name|Cell
argument_list|>
name|subSet
parameter_list|(
name|Cell
name|fromElement
parameter_list|,
name|boolean
name|fromInclusive
parameter_list|,
name|Cell
name|toElement
parameter_list|,
name|boolean
name|toInclusive
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
specifier|public
name|SortedSet
argument_list|<
name|Cell
argument_list|>
name|tailSet
parameter_list|(
name|Cell
name|fromElement
parameter_list|)
block|{
return|return
name|tailSet
argument_list|(
name|fromElement
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|public
name|NavigableSet
argument_list|<
name|Cell
argument_list|>
name|tailSet
parameter_list|(
name|Cell
name|fromElement
parameter_list|,
name|boolean
name|inclusive
parameter_list|)
block|{
return|return
operator|new
name|CellSet
argument_list|(
name|this
operator|.
name|delegatee
operator|.
name|tailMap
argument_list|(
name|fromElement
argument_list|,
name|inclusive
argument_list|)
argument_list|)
return|;
block|}
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
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
comment|// TODO: why do we have a double traversing through map? Recall we have Cell to Cell mapping...
comment|// First for first/last key, which actually returns Cell and then get for the same Cell?
comment|// TODO: Consider just return the first/lastKey(), should be twice more effective...
specifier|public
name|Cell
name|first
parameter_list|()
block|{
return|return
name|this
operator|.
name|delegatee
operator|.
name|get
argument_list|(
name|this
operator|.
name|delegatee
operator|.
name|firstKey
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|Cell
name|last
parameter_list|()
block|{
return|return
name|this
operator|.
name|delegatee
operator|.
name|get
argument_list|(
name|this
operator|.
name|delegatee
operator|.
name|lastKey
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|add
parameter_list|(
name|Cell
name|e
parameter_list|)
block|{
return|return
name|this
operator|.
name|delegatee
operator|.
name|put
argument_list|(
name|e
argument_list|,
name|e
argument_list|)
operator|==
literal|null
return|;
block|}
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
name|c
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|this
operator|.
name|delegatee
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
specifier|public
name|boolean
name|contains
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
comment|//noinspection SuspiciousMethodCalls
return|return
name|this
operator|.
name|delegatee
operator|.
name|containsKey
argument_list|(
name|o
argument_list|)
return|;
block|}
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
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|this
operator|.
name|delegatee
operator|.
name|isEmpty
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|remove
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
name|this
operator|.
name|delegatee
operator|.
name|remove
argument_list|(
name|o
argument_list|)
operator|!=
literal|null
return|;
block|}
specifier|public
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
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
specifier|public
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
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
specifier|public
name|Cell
name|get
parameter_list|(
name|Cell
name|kv
parameter_list|)
block|{
return|return
name|this
operator|.
name|delegatee
operator|.
name|get
argument_list|(
name|kv
argument_list|)
return|;
block|}
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|this
operator|.
name|delegatee
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|Object
index|[]
name|toArray
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
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
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

