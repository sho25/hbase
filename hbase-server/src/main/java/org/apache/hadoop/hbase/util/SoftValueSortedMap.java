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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|ref
operator|.
name|Reference
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|ref
operator|.
name|ReferenceQueue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|ref
operator|.
name|SoftReference
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
name|LinkedHashSet
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
comment|/**  * A SortedMap implementation that uses Soft Reference values  * internally to make it play well with the GC when in a low-memory  * situation. Use as a cache where you also need SortedMap functionality.  *  * @param<K> key class  * @param<V> value class  */
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
name|SoftValueSortedMap
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
implements|implements
name|SortedMap
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
specifier|private
specifier|final
name|SortedMap
argument_list|<
name|K
argument_list|,
name|SoftValue
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
name|internalMap
decl_stmt|;
specifier|private
specifier|final
name|ReferenceQueue
argument_list|<
name|V
argument_list|>
name|rq
init|=
operator|new
name|ReferenceQueue
argument_list|<
name|V
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Object
name|sync
decl_stmt|;
comment|/** Constructor */
specifier|public
name|SoftValueSortedMap
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|TreeMap
argument_list|<
name|K
argument_list|,
name|SoftValue
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor    * @param c comparator    */
specifier|public
name|SoftValueSortedMap
parameter_list|(
specifier|final
name|Comparator
argument_list|<
name|K
argument_list|>
name|c
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|TreeMap
argument_list|<
name|K
argument_list|,
name|SoftValue
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
argument_list|(
name|c
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** Internal constructor    * @param original object to wrap and synchronize on    */
specifier|private
name|SoftValueSortedMap
parameter_list|(
name|SortedMap
argument_list|<
name|K
argument_list|,
name|SoftValue
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
name|original
parameter_list|)
block|{
name|this
argument_list|(
name|original
argument_list|,
name|original
argument_list|)
expr_stmt|;
block|}
comment|/** Internal constructor    * For headMap, tailMap, and subMap support    * @param original object to wrap    * @param sync object to synchronize on    */
specifier|private
name|SoftValueSortedMap
parameter_list|(
name|SortedMap
argument_list|<
name|K
argument_list|,
name|SoftValue
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
name|original
parameter_list|,
name|Object
name|sync
parameter_list|)
block|{
name|this
operator|.
name|internalMap
operator|=
name|original
expr_stmt|;
name|this
operator|.
name|sync
operator|=
name|sync
expr_stmt|;
block|}
comment|/**    * Checks soft references and cleans any that have been placed on    * ReferenceQueue.  Call if get/put etc. are not called regularly.    * Internally these call checkReferences on each access.    * @return How many references cleared.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
name|int
name|checkReferences
parameter_list|()
block|{
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Reference
argument_list|<
name|?
extends|extends
name|V
argument_list|>
name|ref
init|;
operator|(
name|ref
operator|=
name|this
operator|.
name|rq
operator|.
name|poll
argument_list|()
operator|)
operator|!=
literal|null
condition|;
control|)
block|{
name|i
operator|++
expr_stmt|;
name|this
operator|.
name|internalMap
operator|.
name|remove
argument_list|(
operator|(
operator|(
name|SoftValue
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
operator|)
name|ref
operator|)
operator|.
name|key
argument_list|)
expr_stmt|;
block|}
return|return
name|i
return|;
block|}
specifier|public
name|V
name|put
parameter_list|(
name|K
name|key
parameter_list|,
name|V
name|value
parameter_list|)
block|{
synchronized|synchronized
init|(
name|sync
init|)
block|{
name|checkReferences
argument_list|()
expr_stmt|;
name|SoftValue
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|oldValue
init|=
name|this
operator|.
name|internalMap
operator|.
name|put
argument_list|(
name|key
argument_list|,
operator|new
name|SoftValue
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|(
name|key
argument_list|,
name|value
argument_list|,
name|this
operator|.
name|rq
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|oldValue
operator|==
literal|null
condition|?
literal|null
else|:
name|oldValue
operator|.
name|get
argument_list|()
return|;
block|}
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
name|K
argument_list|,
name|?
extends|extends
name|V
argument_list|>
name|m
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
specifier|public
name|V
name|get
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
synchronized|synchronized
init|(
name|sync
init|)
block|{
name|checkReferences
argument_list|()
expr_stmt|;
name|SoftValue
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|value
init|=
name|this
operator|.
name|internalMap
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|value
operator|.
name|get
argument_list|()
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|internalMap
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
name|value
operator|.
name|get
argument_list|()
return|;
block|}
block|}
specifier|public
name|V
name|remove
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
synchronized|synchronized
init|(
name|sync
init|)
block|{
name|checkReferences
argument_list|()
expr_stmt|;
name|SoftValue
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|value
init|=
name|this
operator|.
name|internalMap
operator|.
name|remove
argument_list|(
name|key
argument_list|)
decl_stmt|;
return|return
name|value
operator|==
literal|null
condition|?
literal|null
else|:
name|value
operator|.
name|get
argument_list|()
return|;
block|}
block|}
specifier|public
name|boolean
name|containsKey
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
synchronized|synchronized
init|(
name|sync
init|)
block|{
name|checkReferences
argument_list|()
expr_stmt|;
return|return
name|this
operator|.
name|internalMap
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
return|;
block|}
block|}
specifier|public
name|boolean
name|containsValue
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Don't support containsValue!"
argument_list|)
throw|;
block|}
specifier|public
name|K
name|firstKey
parameter_list|()
block|{
synchronized|synchronized
init|(
name|sync
init|)
block|{
name|checkReferences
argument_list|()
expr_stmt|;
return|return
name|internalMap
operator|.
name|firstKey
argument_list|()
return|;
block|}
block|}
specifier|public
name|K
name|lastKey
parameter_list|()
block|{
synchronized|synchronized
init|(
name|sync
init|)
block|{
name|checkReferences
argument_list|()
expr_stmt|;
return|return
name|internalMap
operator|.
name|lastKey
argument_list|()
return|;
block|}
block|}
specifier|public
name|SoftValueSortedMap
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|headMap
parameter_list|(
name|K
name|key
parameter_list|)
block|{
synchronized|synchronized
init|(
name|sync
init|)
block|{
name|checkReferences
argument_list|()
expr_stmt|;
return|return
operator|new
name|SoftValueSortedMap
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|(
name|this
operator|.
name|internalMap
operator|.
name|headMap
argument_list|(
name|key
argument_list|)
argument_list|,
name|sync
argument_list|)
return|;
block|}
block|}
specifier|public
name|SoftValueSortedMap
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|tailMap
parameter_list|(
name|K
name|key
parameter_list|)
block|{
synchronized|synchronized
init|(
name|sync
init|)
block|{
name|checkReferences
argument_list|()
expr_stmt|;
return|return
operator|new
name|SoftValueSortedMap
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|(
name|this
operator|.
name|internalMap
operator|.
name|tailMap
argument_list|(
name|key
argument_list|)
argument_list|,
name|sync
argument_list|)
return|;
block|}
block|}
specifier|public
name|SoftValueSortedMap
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|subMap
parameter_list|(
name|K
name|fromKey
parameter_list|,
name|K
name|toKey
parameter_list|)
block|{
synchronized|synchronized
init|(
name|sync
init|)
block|{
name|checkReferences
argument_list|()
expr_stmt|;
return|return
operator|new
name|SoftValueSortedMap
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|(
name|this
operator|.
name|internalMap
operator|.
name|subMap
argument_list|(
name|fromKey
argument_list|,
name|toKey
argument_list|)
argument_list|,
name|sync
argument_list|)
return|;
block|}
block|}
comment|/*    * retrieves the value associated with the greatest key strictly less than    *  the given key, or null if there is no such key    * @param key the key we're interested in    */
specifier|public
specifier|synchronized
name|V
name|lowerValueByKey
parameter_list|(
name|K
name|key
parameter_list|)
block|{
synchronized|synchronized
init|(
name|sync
init|)
block|{
name|checkReferences
argument_list|()
expr_stmt|;
name|Map
operator|.
name|Entry
argument_list|<
name|K
argument_list|,
name|SoftValue
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
name|entry
init|=
operator|(
operator|(
name|NavigableMap
argument_list|<
name|K
argument_list|,
name|SoftValue
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
operator|)
name|this
operator|.
name|internalMap
operator|)
operator|.
name|lowerEntry
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|SoftValue
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|value
operator|.
name|get
argument_list|()
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|internalMap
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
name|value
operator|.
name|get
argument_list|()
return|;
block|}
block|}
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
synchronized|synchronized
init|(
name|sync
init|)
block|{
name|checkReferences
argument_list|()
expr_stmt|;
return|return
name|this
operator|.
name|internalMap
operator|.
name|isEmpty
argument_list|()
return|;
block|}
block|}
specifier|public
name|int
name|size
parameter_list|()
block|{
synchronized|synchronized
init|(
name|sync
init|)
block|{
name|checkReferences
argument_list|()
expr_stmt|;
return|return
name|this
operator|.
name|internalMap
operator|.
name|size
argument_list|()
return|;
block|}
block|}
specifier|public
name|void
name|clear
parameter_list|()
block|{
synchronized|synchronized
init|(
name|sync
init|)
block|{
name|checkReferences
argument_list|()
expr_stmt|;
name|this
operator|.
name|internalMap
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|Set
argument_list|<
name|K
argument_list|>
name|keySet
parameter_list|()
block|{
synchronized|synchronized
init|(
name|sync
init|)
block|{
name|checkReferences
argument_list|()
expr_stmt|;
comment|// this is not correct as per SortedMap contract (keySet should be
comment|// modifiable)
comment|// needed here so that another thread cannot modify the keyset
comment|// without locking
return|return
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
name|this
operator|.
name|internalMap
operator|.
name|keySet
argument_list|()
argument_list|)
return|;
block|}
block|}
specifier|public
name|Comparator
argument_list|<
name|?
super|super
name|K
argument_list|>
name|comparator
parameter_list|()
block|{
return|return
name|this
operator|.
name|internalMap
operator|.
name|comparator
argument_list|()
return|;
block|}
specifier|public
name|Set
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
name|entrySet
parameter_list|()
block|{
synchronized|synchronized
init|(
name|sync
init|)
block|{
name|checkReferences
argument_list|()
expr_stmt|;
comment|// this is not correct as per SortedMap contract (entrySet should be
comment|// backed by map)
name|Set
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
name|realEntries
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|K
argument_list|,
name|SoftValue
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
name|entry
range|:
name|this
operator|.
name|internalMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|realEntries
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|realEntries
return|;
block|}
block|}
specifier|public
name|Collection
argument_list|<
name|V
argument_list|>
name|values
parameter_list|()
block|{
synchronized|synchronized
init|(
name|sync
init|)
block|{
name|checkReferences
argument_list|()
expr_stmt|;
name|ArrayList
argument_list|<
name|V
argument_list|>
name|hardValues
init|=
operator|new
name|ArrayList
argument_list|<
name|V
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|SoftValue
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|softValue
range|:
name|this
operator|.
name|internalMap
operator|.
name|values
argument_list|()
control|)
block|{
name|hardValues
operator|.
name|add
argument_list|(
name|softValue
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|hardValues
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|SoftValue
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
extends|extends
name|SoftReference
argument_list|<
name|V
argument_list|>
implements|implements
name|Map
operator|.
name|Entry
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
specifier|final
name|K
name|key
decl_stmt|;
name|SoftValue
parameter_list|(
name|K
name|key
parameter_list|,
name|V
name|value
parameter_list|,
name|ReferenceQueue
argument_list|<
name|V
argument_list|>
name|q
parameter_list|)
block|{
name|super
argument_list|(
name|value
argument_list|,
name|q
argument_list|)
expr_stmt|;
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
block|}
specifier|public
name|K
name|getKey
parameter_list|()
block|{
return|return
name|this
operator|.
name|key
return|;
block|}
specifier|public
name|V
name|getValue
parameter_list|()
block|{
return|return
name|get
argument_list|()
return|;
block|}
specifier|public
name|V
name|setValue
parameter_list|(
name|V
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

