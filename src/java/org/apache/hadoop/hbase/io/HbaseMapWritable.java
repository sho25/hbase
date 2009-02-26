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
name|io
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|conf
operator|.
name|Configurable
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
name|conf
operator|.
name|Configuration
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
name|io
operator|.
name|Writable
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * A Writable Map.  * Like {@link org.apache.hadoop.io.MapWritable} but dumb. It will fail  * if passed a value type that it has not already been told about. Its  been  * primed with hbase Writables and byte [].  Keys are always byte arrays.  *  * @param<K><byte []> key  TODO: Parameter K is never used, could be removed.  * @param<V> value Expects a Writable or byte [].  */
end_comment

begin_class
specifier|public
class|class
name|HbaseMapWritable
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
implements|implements
name|SortedMap
argument_list|<
name|byte
index|[]
argument_list|,
name|V
argument_list|>
implements|,
name|Configurable
implements|,
name|Writable
implements|,
name|CodeToClassAndBack
block|{
specifier|private
name|AtomicReference
argument_list|<
name|Configuration
argument_list|>
name|conf
init|=
literal|null
decl_stmt|;
specifier|protected
name|SortedMap
argument_list|<
name|byte
index|[]
argument_list|,
name|V
argument_list|>
name|instance
init|=
literal|null
decl_stmt|;
comment|// Static maps of code to class and vice versa.  Includes types used in hbase
comment|// only. These maps are now initialized in a static loader interface instead
comment|// of in a static contructor for this class, this is done so that it is
comment|// possible to have a regular contructor here, so that different params can
comment|// be used.
comment|// Removed the old types like Text from the maps, if needed to add more types
comment|// this can be done in the StaticHBaseMapWritableLoader interface. Only
comment|// byte[] and Cell are supported now.
comment|//   static final Map<Byte, Class<?>> CODE_TO_CLASS =
comment|//     new HashMap<Byte, Class<?>>();
comment|//   static final Map<Class<?>, Byte> CLASS_TO_CODE =
comment|//     new HashMap<Class<?>, Byte>();
comment|/**    * The default contructor where a TreeMap is used    **/
specifier|public
name|HbaseMapWritable
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|V
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**   * Contructor where another SortedMap can be used   *    * @param map the SortedMap to be used    **/
specifier|public
name|HbaseMapWritable
parameter_list|(
name|SortedMap
argument_list|<
name|byte
index|[]
argument_list|,
name|V
argument_list|>
name|map
parameter_list|)
block|{
name|conf
operator|=
operator|new
name|AtomicReference
argument_list|<
name|Configuration
argument_list|>
argument_list|()
expr_stmt|;
name|instance
operator|=
name|map
expr_stmt|;
block|}
comment|/** @return the conf */
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
operator|.
name|get
argument_list|()
return|;
block|}
comment|/** @param conf the conf to set */
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|instance
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
specifier|public
name|boolean
name|containsKey
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
return|return
name|instance
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
return|return
name|instance
operator|.
name|containsValue
argument_list|(
name|value
argument_list|)
return|;
block|}
specifier|public
name|Set
argument_list|<
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|V
argument_list|>
argument_list|>
name|entrySet
parameter_list|()
block|{
return|return
name|instance
operator|.
name|entrySet
argument_list|()
return|;
block|}
specifier|public
name|V
name|get
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
return|return
name|instance
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|instance
operator|.
name|isEmpty
argument_list|()
return|;
block|}
specifier|public
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|keySet
parameter_list|()
block|{
return|return
name|instance
operator|.
name|keySet
argument_list|()
return|;
block|}
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|instance
operator|.
name|size
argument_list|()
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
return|return
name|instance
operator|.
name|values
argument_list|()
return|;
block|}
specifier|public
name|void
name|putAll
argument_list|(
name|Map
operator|<
condition|?
then|extends
name|byte
index|[]
argument_list|,
operator|?
expr|extends
name|V
operator|>
name|m
argument_list|)
block|{
name|this
operator|.
name|instance
operator|.
name|putAll
argument_list|(
name|m
argument_list|)
expr_stmt|;
block|}
specifier|public
name|V
name|remove
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
return|return
name|this
operator|.
name|instance
operator|.
name|remove
argument_list|(
name|key
argument_list|)
return|;
block|}
specifier|public
name|V
name|put
parameter_list|(
name|byte
index|[]
name|key
parameter_list|,
name|V
name|value
parameter_list|)
block|{
return|return
name|this
operator|.
name|instance
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
return|;
block|}
specifier|public
name|Comparator
operator|<
condition|?
name|super
name|byte
index|[]
operator|>
name|comparator
argument_list|()
block|{
return|return
name|this
operator|.
name|instance
operator|.
name|comparator
argument_list|()
return|;
block|}
specifier|public
name|byte
index|[]
name|firstKey
parameter_list|()
block|{
return|return
name|this
operator|.
name|instance
operator|.
name|firstKey
argument_list|()
return|;
block|}
specifier|public
name|SortedMap
argument_list|<
name|byte
index|[]
argument_list|,
name|V
argument_list|>
name|headMap
parameter_list|(
name|byte
index|[]
name|toKey
parameter_list|)
block|{
return|return
name|this
operator|.
name|instance
operator|.
name|headMap
argument_list|(
name|toKey
argument_list|)
return|;
block|}
specifier|public
name|byte
index|[]
name|lastKey
parameter_list|()
block|{
return|return
name|this
operator|.
name|instance
operator|.
name|lastKey
argument_list|()
return|;
block|}
specifier|public
name|SortedMap
argument_list|<
name|byte
index|[]
argument_list|,
name|V
argument_list|>
name|subMap
parameter_list|(
name|byte
index|[]
name|fromKey
parameter_list|,
name|byte
index|[]
name|toKey
parameter_list|)
block|{
return|return
name|this
operator|.
name|instance
operator|.
name|subMap
argument_list|(
name|fromKey
argument_list|,
name|toKey
argument_list|)
return|;
block|}
specifier|public
name|SortedMap
argument_list|<
name|byte
index|[]
argument_list|,
name|V
argument_list|>
name|tailMap
parameter_list|(
name|byte
index|[]
name|fromKey
parameter_list|)
block|{
return|return
name|this
operator|.
name|instance
operator|.
name|tailMap
argument_list|(
name|fromKey
argument_list|)
return|;
block|}
comment|// Writable
comment|/** @return the Class class for the specified id */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"boxing"
argument_list|)
specifier|protected
name|Class
argument_list|<
name|?
argument_list|>
name|getClass
parameter_list|(
name|byte
name|id
parameter_list|)
block|{
return|return
name|CODE_TO_CLASS
operator|.
name|get
argument_list|(
name|id
argument_list|)
return|;
block|}
comment|/** @return the id for the specified Class */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"boxing"
argument_list|)
specifier|protected
name|byte
name|getId
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|)
block|{
name|Byte
name|b
init|=
name|CLASS_TO_CODE
operator|.
name|get
argument_list|(
name|clazz
argument_list|)
decl_stmt|;
if|if
condition|(
name|b
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"Nothing for : "
operator|+
name|clazz
argument_list|)
throw|;
block|}
return|return
name|b
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|this
operator|.
name|instance
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Write out the number of entries in the map
name|out
operator|.
name|writeInt
argument_list|(
name|this
operator|.
name|instance
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Then write out each key/value pair
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|V
argument_list|>
name|e
range|:
name|instance
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|Byte
name|id
init|=
name|getId
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|Object
name|value
init|=
name|e
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|byte
index|[]
condition|)
block|{
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
operator|(
name|byte
index|[]
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
operator|(
operator|(
name|Writable
operator|)
name|value
operator|)
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
comment|// First clear the map.  Otherwise we will just accumulate
comment|// entries every time this method is called.
name|this
operator|.
name|instance
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// Read the number of entries in the map
name|int
name|entries
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
comment|// Then read each key/value pair
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|entries
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|key
init|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|Class
name|clazz
init|=
name|getClass
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
decl_stmt|;
name|V
name|value
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|clazz
operator|.
name|equals
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|)
condition|)
block|{
name|byte
index|[]
name|bytes
init|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|value
operator|=
operator|(
name|V
operator|)
name|bytes
expr_stmt|;
block|}
else|else
block|{
name|Writable
name|w
init|=
operator|(
name|Writable
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|clazz
argument_list|,
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|w
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|value
operator|=
operator|(
name|V
operator|)
name|w
expr_stmt|;
block|}
name|this
operator|.
name|instance
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

