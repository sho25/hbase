begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
operator|.
name|hfile
package|;
end_package

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
name|concurrent
operator|.
name|ConcurrentHashMap
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
name|AtomicInteger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
comment|/**  * This class is used to manage the identifiers for {@link CacheableDeserializer}.  * All deserializers are registered with this Manager via the  * {@link #registerDeserializer(CacheableDeserializer)}}. On registration, we return an  * int *identifier* for this deserializer. The int identifier is passed to  * {@link #getDeserializer(int)}} to obtain the registered deserializer instance.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CacheableDeserializerIdManager
block|{
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|CacheableDeserializer
argument_list|<
name|Cacheable
argument_list|>
argument_list|>
name|registeredDeserializers
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|AtomicInteger
name|identifier
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|/**    * Register the given {@link Cacheable} -- usually an hfileblock instance, these implement    * the Cacheable Interface -- deserializer and generate an unique identifier id for it and return    * this as our result.    * @return the identifier of given cacheable deserializer    * @see #getDeserializer(int)    */
specifier|public
specifier|static
name|int
name|registerDeserializer
parameter_list|(
name|CacheableDeserializer
argument_list|<
name|Cacheable
argument_list|>
name|cd
parameter_list|)
block|{
name|int
name|idx
init|=
name|identifier
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
comment|// No synchronization here because keys will be unique
name|registeredDeserializers
operator|.
name|put
argument_list|(
name|idx
argument_list|,
name|cd
argument_list|)
expr_stmt|;
return|return
name|idx
return|;
block|}
comment|/**    * Get the cacheable deserializer registered at the given identifier Id.    * @see #registerDeserializer(CacheableDeserializer)    */
specifier|public
specifier|static
name|CacheableDeserializer
argument_list|<
name|Cacheable
argument_list|>
name|getDeserializer
parameter_list|(
name|int
name|id
parameter_list|)
block|{
return|return
name|registeredDeserializers
operator|.
name|get
argument_list|(
name|id
argument_list|)
return|;
block|}
comment|/**    * Snapshot a map of the current identifiers to class names for reconstruction on reading out    * of a file.    */
specifier|public
specifier|static
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|save
parameter_list|()
block|{
comment|// No synchronization here because weakly consistent view should be good enough
comment|// The assumed risk is that we might not see a new serializer that comes in while iterating,
comment|// but with a synchronized block, we won't see it anyway
return|return
name|registeredDeserializers
operator|.
name|entrySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toMap
argument_list|(
name|Map
operator|.
name|Entry
operator|::
name|getKey
argument_list|,
name|e
lambda|->
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

