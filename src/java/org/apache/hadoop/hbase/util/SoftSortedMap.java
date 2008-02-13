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
name|TreeSet
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
name|Collection
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

begin_comment
comment|/**  * A SortedMap implementation that uses SoftReferences internally to make it  * play well with the GC when in a low-memory situation.  */
end_comment

begin_class
specifier|public
class|class
name|SoftSortedMap
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
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SoftSortedMap
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
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
init|=
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
decl_stmt|;
specifier|protected
name|ReferenceQueue
name|referenceQueue
init|=
operator|new
name|ReferenceQueue
argument_list|()
decl_stmt|;
comment|/** Constructor */
specifier|public
name|SoftSortedMap
parameter_list|()
block|{}
comment|/** For headMap and tailMap support */
specifier|private
name|SoftSortedMap
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
name|internalMap
operator|=
name|original
expr_stmt|;
block|}
comment|/* Client methods */
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
name|referenceQueue
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
specifier|public
name|void
name|putAll
parameter_list|(
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
name|internalMap
operator|.
name|get
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
name|V
name|remove
parameter_list|(
name|Object
name|key
parameter_list|)
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
name|checkReferences
argument_list|()
expr_stmt|;
return|return
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
name|Object
name|value
parameter_list|)
block|{
name|checkReferences
argument_list|()
expr_stmt|;
return|return
name|internalMap
operator|.
name|containsValue
argument_list|(
name|value
argument_list|)
return|;
block|}
specifier|public
name|K
name|firstKey
parameter_list|()
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
specifier|public
name|K
name|lastKey
parameter_list|()
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
specifier|public
name|SoftSortedMap
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
name|checkReferences
argument_list|()
expr_stmt|;
return|return
operator|new
name|SoftSortedMap
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|(
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
name|SoftSortedMap
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
name|checkReferences
argument_list|()
expr_stmt|;
return|return
operator|new
name|SoftSortedMap
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|(
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
name|SoftSortedMap
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
name|checkReferences
argument_list|()
expr_stmt|;
return|return
operator|new
name|SoftSortedMap
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|(
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
name|checkReferences
argument_list|()
expr_stmt|;
return|return
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
name|checkReferences
argument_list|()
expr_stmt|;
return|return
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
return|return
name|internalMap
operator|.
name|keySet
argument_list|()
return|;
block|}
specifier|public
name|Comparator
name|comparator
parameter_list|()
block|{
return|return
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
comment|/**    * Check the reference queue and delete anything that has since gone away    */
specifier|private
name|void
name|checkReferences
parameter_list|()
block|{
name|SoftValue
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|sv
decl_stmt|;
while|while
condition|(
operator|(
name|sv
operator|=
operator|(
name|SoftValue
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
operator|)
name|referenceQueue
operator|.
name|poll
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Reference for key "
operator|+
name|sv
operator|.
name|key
operator|.
name|toString
argument_list|()
operator|+
literal|" has been cleared."
argument_list|)
expr_stmt|;
block|}
name|internalMap
operator|.
name|remove
argument_list|(
name|sv
operator|.
name|key
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * A SoftReference derivative so that we can track down what keys to remove.    */
specifier|private
class|class
name|SoftValue
parameter_list|<
name|K2
parameter_list|,
name|V2
parameter_list|>
extends|extends
name|SoftReference
argument_list|<
name|V2
argument_list|>
implements|implements
name|Map
operator|.
name|Entry
argument_list|<
name|K2
argument_list|,
name|V2
argument_list|>
block|{
name|K2
name|key
decl_stmt|;
name|SoftValue
parameter_list|(
name|K2
name|key
parameter_list|,
name|V2
name|value
parameter_list|,
name|ReferenceQueue
name|queue
parameter_list|)
block|{
name|super
argument_list|(
name|value
argument_list|,
name|queue
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
name|K2
name|getKey
parameter_list|()
block|{
return|return
name|key
return|;
block|}
specifier|public
name|V2
name|getValue
parameter_list|()
block|{
return|return
name|get
argument_list|()
return|;
block|}
specifier|public
name|V2
name|setValue
parameter_list|(
name|V2
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

