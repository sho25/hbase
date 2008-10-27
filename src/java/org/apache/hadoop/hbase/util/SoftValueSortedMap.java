begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_comment
comment|/**  * A SortedMap implementation that uses Soft Reference values  * internally to make it play well with the GC when in a low-memory  * situation. Use as a cache where you also need SortedMap functionality.  *   * @param<K> key class  * @param<V> value class  */
end_comment

begin_class
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
name|ReferenceQueueUtil
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
name|rq
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
comment|/**    * Constructor    * @param c    */
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
comment|/** For headMap and tailMap support */
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
operator|.
name|internalMap
operator|=
name|original
expr_stmt|;
name|this
operator|.
name|rq
operator|=
operator|new
name|ReferenceQueueUtil
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
name|this
operator|.
name|internalMap
argument_list|)
expr_stmt|;
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
name|this
operator|.
name|rq
operator|.
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
operator|.
name|getReferenceQueue
argument_list|()
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
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|void
name|putAll
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|Map
name|map
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
name|this
operator|.
name|rq
operator|.
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
specifier|public
name|V
name|remove
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
name|this
operator|.
name|rq
operator|.
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
specifier|public
name|boolean
name|containsKey
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
name|this
operator|.
name|rq
operator|.
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
specifier|public
name|boolean
name|containsValue
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|Object
name|value
parameter_list|)
block|{
comment|/*    checkReferences();     return internalMap.containsValue(value);*/
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
name|this
operator|.
name|rq
operator|.
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
specifier|public
name|K
name|lastKey
parameter_list|()
block|{
name|this
operator|.
name|rq
operator|.
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
name|this
operator|.
name|rq
operator|.
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
argument_list|)
return|;
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
name|this
operator|.
name|rq
operator|.
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
argument_list|)
return|;
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
name|this
operator|.
name|rq
operator|.
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
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
name|this
operator|.
name|rq
operator|.
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
specifier|public
name|int
name|size
parameter_list|()
block|{
name|this
operator|.
name|rq
operator|.
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
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|this
operator|.
name|rq
operator|.
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
specifier|public
name|Set
argument_list|<
name|K
argument_list|>
name|keySet
parameter_list|()
block|{
name|this
operator|.
name|rq
operator|.
name|checkReferences
argument_list|()
expr_stmt|;
return|return
name|this
operator|.
name|internalMap
operator|.
name|keySet
argument_list|()
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|Comparator
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
name|this
operator|.
name|rq
operator|.
name|checkReferences
argument_list|()
expr_stmt|;
name|Set
argument_list|<
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
argument_list|>
name|entries
init|=
name|this
operator|.
name|internalMap
operator|.
name|entrySet
argument_list|()
decl_stmt|;
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
name|real_entries
init|=
operator|new
name|TreeSet
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
name|entries
control|)
block|{
name|real_entries
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
name|real_entries
return|;
block|}
specifier|public
name|Collection
argument_list|<
name|V
argument_list|>
name|values
parameter_list|()
block|{
name|this
operator|.
name|rq
operator|.
name|checkReferences
argument_list|()
expr_stmt|;
name|Collection
argument_list|<
name|SoftValue
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
name|softValues
init|=
name|this
operator|.
name|internalMap
operator|.
name|values
argument_list|()
decl_stmt|;
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
name|softValues
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
end_class

end_unit

