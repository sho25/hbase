begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

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
name|io
operator|.
name|OutputStream
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
name|HashMap
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
name|Map
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
name|collections4
operator|.
name|iterators
operator|.
name|UnmodifiableIterator
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
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * Do a shallow merge of multiple KV configuration pools. This is a very useful  * utility class to easily add per-object configurations in addition to wider  * scope settings. This is different from Configuration.addResource()  * functionality, which performs a deep merge and mutates the common data  * structure.  *<p>  * The iterator on CompoundConfiguration is unmodifiable. Obtaining iterator is an expensive  * operation.  *<p>  * For clarity: the shallow merge allows the user to mutate either of the  * configuration objects and have changes reflected everywhere. In contrast to a  * deep merge, that requires you to explicitly know all applicable copies to  * propagate changes.  *   * WARNING: The values set in the CompoundConfiguration are do not handle Property variable  * substitution.  However, if they are set in the underlying configuration substitutions are  * done.   */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CompoundConfiguration
extends|extends
name|Configuration
block|{
specifier|private
name|Configuration
name|mutableConf
init|=
literal|null
decl_stmt|;
comment|/**    * Default Constructor. Initializes empty configuration    */
specifier|public
name|CompoundConfiguration
parameter_list|()
block|{   }
comment|// Devs: these APIs are the same contract as their counterparts in
comment|// Configuration.java
specifier|private
interface|interface
name|ImmutableConfigMap
extends|extends
name|Iterable
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
block|{
name|String
name|get
parameter_list|(
name|String
name|key
parameter_list|)
function_decl|;
name|String
name|getRaw
parameter_list|(
name|String
name|key
parameter_list|)
function_decl|;
name|Class
argument_list|<
name|?
argument_list|>
name|getClassByName
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|ClassNotFoundException
function_decl|;
name|int
name|size
parameter_list|()
function_decl|;
block|}
specifier|private
specifier|final
name|List
argument_list|<
name|ImmutableConfigMap
argument_list|>
name|configs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|static
class|class
name|ImmutableConfWrapper
implements|implements
name|ImmutableConfigMap
block|{
specifier|private
specifier|final
name|Configuration
name|c
decl_stmt|;
name|ImmutableConfWrapper
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|c
operator|=
name|conf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|c
operator|.
name|iterator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|get
parameter_list|(
name|String
name|key
parameter_list|)
block|{
return|return
name|c
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getRaw
parameter_list|(
name|String
name|key
parameter_list|)
block|{
return|return
name|c
operator|.
name|getRaw
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|getClassByName
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|ClassNotFoundException
block|{
return|return
name|c
operator|.
name|getClassByName
argument_list|(
name|name
argument_list|)
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
name|c
operator|.
name|size
argument_list|()
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
name|c
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
comment|/**    * If set has been called, it will create a mutableConf.  This converts the mutableConf to an    * immutable one and resets it to allow a new mutable conf.  This is used when a new map or    * conf is added to the compound configuration to preserve proper override semantics.    */
name|void
name|freezeMutableConf
parameter_list|()
block|{
if|if
condition|(
name|mutableConf
operator|==
literal|null
condition|)
block|{
comment|// do nothing if there is no current mutableConf
return|return;
block|}
name|this
operator|.
name|configs
operator|.
name|add
argument_list|(
literal|0
argument_list|,
operator|new
name|ImmutableConfWrapper
argument_list|(
name|mutableConf
argument_list|)
argument_list|)
expr_stmt|;
name|mutableConf
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Add Hadoop Configuration object to config list.    * The added configuration overrides the previous ones if there are name collisions.    * @param conf configuration object    * @return this, for builder pattern    */
specifier|public
name|CompoundConfiguration
name|add
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
name|freezeMutableConf
argument_list|()
expr_stmt|;
if|if
condition|(
name|conf
operator|instanceof
name|CompoundConfiguration
condition|)
block|{
name|this
operator|.
name|configs
operator|.
name|addAll
argument_list|(
literal|0
argument_list|,
operator|(
operator|(
name|CompoundConfiguration
operator|)
name|conf
operator|)
operator|.
name|configs
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|// put new config at the front of the list (top priority)
name|this
operator|.
name|configs
operator|.
name|add
argument_list|(
literal|0
argument_list|,
operator|new
name|ImmutableConfWrapper
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Add Bytes map to config list. This map is generally    * created by HTableDescriptor or HColumnDescriptor, but can be abstractly    * used. The added configuration overrides the previous ones if there are    * name collisions.    *    * @param map    *          Bytes map    * @return this, for builder pattern    */
specifier|public
name|CompoundConfiguration
name|addBytesMap
parameter_list|(
specifier|final
name|Map
argument_list|<
name|Bytes
argument_list|,
name|Bytes
argument_list|>
name|map
parameter_list|)
block|{
name|freezeMutableConf
argument_list|()
expr_stmt|;
comment|// put new map at the front of the list (top priority)
name|this
operator|.
name|configs
operator|.
name|add
argument_list|(
literal|0
argument_list|,
operator|new
name|ImmutableConfigMap
argument_list|()
block|{
specifier|private
specifier|final
name|Map
argument_list|<
name|Bytes
argument_list|,
name|Bytes
argument_list|>
name|m
init|=
name|map
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|iterator
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ret
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Bytes
argument_list|,
name|Bytes
argument_list|>
name|entry
range|:
name|map
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|key
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|val
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|==
literal|null
condition|?
literal|null
else|:
name|Bytes
operator|.
name|toString
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|ret
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|get
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|Bytes
name|ibw
init|=
operator|new
name|Bytes
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|key
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|m
operator|.
name|containsKey
argument_list|(
name|ibw
argument_list|)
condition|)
return|return
literal|null
return|;
name|Bytes
name|value
init|=
name|m
operator|.
name|get
argument_list|(
name|ibw
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
operator|||
name|value
operator|.
name|get
argument_list|()
operator|==
literal|null
condition|)
return|return
literal|null
return|;
return|return
name|Bytes
operator|.
name|toString
argument_list|(
name|value
operator|.
name|get
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getRaw
parameter_list|(
name|String
name|key
parameter_list|)
block|{
return|return
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|getClassByName
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|ClassNotFoundException
block|{
return|return
literal|null
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
name|m
operator|.
name|size
argument_list|()
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
name|m
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Add String map to config list. This map is generally created by HTableDescriptor    * or HColumnDescriptor, but can be abstractly used. The added configuration    * overrides the previous ones if there are name collisions.    *    * @return this, for builder pattern    */
specifier|public
name|CompoundConfiguration
name|addStringMap
parameter_list|(
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
parameter_list|)
block|{
name|freezeMutableConf
argument_list|()
expr_stmt|;
comment|// put new map at the front of the list (top priority)
name|this
operator|.
name|configs
operator|.
name|add
argument_list|(
literal|0
argument_list|,
operator|new
name|ImmutableConfigMap
argument_list|()
block|{
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|m
init|=
name|map
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|map
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|get
parameter_list|(
name|String
name|key
parameter_list|)
block|{
return|return
name|m
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getRaw
parameter_list|(
name|String
name|key
parameter_list|)
block|{
return|return
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|getClassByName
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|ClassNotFoundException
block|{
return|return
literal|null
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
name|m
operator|.
name|size
argument_list|()
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
name|m
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"CompoundConfiguration: "
operator|+
name|this
operator|.
name|configs
operator|.
name|size
argument_list|()
operator|+
literal|" configs"
argument_list|)
expr_stmt|;
for|for
control|(
name|ImmutableConfigMap
name|m
range|:
name|this
operator|.
name|configs
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|m
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|get
parameter_list|(
name|String
name|key
parameter_list|)
block|{
if|if
condition|(
name|mutableConf
operator|!=
literal|null
condition|)
block|{
name|String
name|value
init|=
name|mutableConf
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
return|return
name|value
return|;
block|}
block|}
for|for
control|(
name|ImmutableConfigMap
name|m
range|:
name|this
operator|.
name|configs
control|)
block|{
name|String
name|value
init|=
name|m
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
return|return
name|value
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getRaw
parameter_list|(
name|String
name|key
parameter_list|)
block|{
if|if
condition|(
name|mutableConf
operator|!=
literal|null
condition|)
block|{
name|String
name|value
init|=
name|mutableConf
operator|.
name|getRaw
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
return|return
name|value
return|;
block|}
block|}
for|for
control|(
name|ImmutableConfigMap
name|m
range|:
name|this
operator|.
name|configs
control|)
block|{
name|String
name|value
init|=
name|m
operator|.
name|getRaw
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
return|return
name|value
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|getClassByName
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|ClassNotFoundException
block|{
if|if
condition|(
name|mutableConf
operator|!=
literal|null
condition|)
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|value
init|=
name|mutableConf
operator|.
name|getClassByName
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
return|return
name|value
return|;
block|}
block|}
for|for
control|(
name|ImmutableConfigMap
name|m
range|:
name|this
operator|.
name|configs
control|)
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|value
init|=
name|m
operator|.
name|getClassByName
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
return|return
name|value
return|;
block|}
block|}
throw|throw
operator|new
name|ClassNotFoundException
argument_list|()
throw|;
block|}
comment|// TODO: This method overestimates the number of configuration settings -- if a value is masked
comment|// by an overriding config or map, it will be counted multiple times.
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
name|int
name|ret
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|mutableConf
operator|!=
literal|null
condition|)
block|{
name|ret
operator|+=
name|mutableConf
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|ImmutableConfigMap
name|m
range|:
name|this
operator|.
name|configs
control|)
block|{
name|ret
operator|+=
name|m
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
comment|/**    * Get the value of the<code>name</code>. If the key is deprecated,    * it returns the value of the first key which replaces the deprecated key    * and is not null.    * If no such property exists,    * then<code>defaultValue</code> is returned.     * The CompooundConfiguration does not do property substitution.  To do so we need    * Configuration.getProps to be protected or package visible.  Though in hadoop2 it is    * protected, in hadoop1 the method is private and not accessible.    *     * All of the get* methods call this overridden get method.    *     * @param name property name.    * @param defaultValue default value.    * @return property value, or<code>defaultValue</code> if the property     *         doesn't exist.                        **/
annotation|@
name|Override
specifier|public
name|String
name|get
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|defaultValue
parameter_list|)
block|{
name|String
name|ret
init|=
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
return|return
name|ret
operator|==
literal|null
condition|?
name|defaultValue
else|:
name|ret
return|;
block|}
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|iterator
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ret
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// add in reverse order so that oldest get overridden.
if|if
condition|(
operator|!
name|configs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
name|configs
operator|.
name|size
argument_list|()
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|ImmutableConfigMap
name|map
init|=
name|configs
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|iter
init|=
name|map
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|ret
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// add mutations to this CompoundConfiguration last.
if|if
condition|(
name|mutableConf
operator|!=
literal|null
condition|)
block|{
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|miter
init|=
name|mutableConf
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|miter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
init|=
name|miter
operator|.
name|next
argument_list|()
decl_stmt|;
name|ret
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|UnmodifiableIterator
operator|.
name|unmodifiableIterator
argument_list|(
name|ret
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|set
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|mutableConf
operator|==
literal|null
condition|)
block|{
comment|// not thread safe
name|mutableConf
operator|=
operator|new
name|Configuration
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// an empty configuration
block|}
name|mutableConf
operator|.
name|set
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
comment|/***********************************************************************************************    * These methods are unsupported, and no code using CompoundConfiguration depend upon them.    * Quickly abort upon any attempts to use them.     **********************************************************************************************/
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Immutable Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
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
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Immutable Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeXml
parameter_list|(
name|OutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Immutable Configuration"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

