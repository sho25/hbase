begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HashSet
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
name|InterfaceStability
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
comment|/**  * Namespace POJO class. Used to represent and define namespaces.  *  * Descriptors will be persisted in an hbase table.  * This works since namespaces are essentially metadata of a group of tables  * as opposed to a more tangible container.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|NamespaceDescriptor
block|{
comment|/** System namespace name. */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|SYSTEM_NAMESPACE_NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"hbase"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SYSTEM_NAMESPACE_NAME_STR
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|SYSTEM_NAMESPACE_NAME
argument_list|)
decl_stmt|;
comment|/** Default namespace name. */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|DEFAULT_NAMESPACE_NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"default"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_NAMESPACE_NAME_STR
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|DEFAULT_NAMESPACE_NAME
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|NamespaceDescriptor
name|DEFAULT_NAMESPACE
init|=
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|DEFAULT_NAMESPACE_NAME_STR
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|NamespaceDescriptor
name|SYSTEM_NAMESPACE
init|=
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|SYSTEM_NAMESPACE_NAME_STR
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|RESERVED_NAMESPACES
decl_stmt|;
static|static
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|set
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|set
operator|.
name|add
argument_list|(
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE_NAME_STR
argument_list|)
expr_stmt|;
name|set
operator|.
name|add
argument_list|(
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE_NAME_STR
argument_list|)
expr_stmt|;
name|RESERVED_NAMESPACES
operator|=
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
name|set
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|final
specifier|static
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|RESERVED_NAMESPACES_BYTES
decl_stmt|;
static|static
block|{
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|set
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|BYTES_RAWCOMPARATOR
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|name
range|:
name|RESERVED_NAMESPACES
control|)
block|{
name|set
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|RESERVED_NAMESPACES_BYTES
operator|=
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
name|set
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|name
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|configuration
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Comparator
argument_list|<
name|NamespaceDescriptor
argument_list|>
name|NAMESPACE_DESCRIPTOR_COMPARATOR
init|=
operator|new
name|Comparator
argument_list|<
name|NamespaceDescriptor
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|NamespaceDescriptor
name|namespaceDescriptor
parameter_list|,
name|NamespaceDescriptor
name|namespaceDescriptor2
parameter_list|)
block|{
return|return
name|namespaceDescriptor
operator|.
name|getName
argument_list|()
operator|.
name|compareTo
argument_list|(
name|namespaceDescriptor2
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
block|}
decl_stmt|;
specifier|private
name|NamespaceDescriptor
parameter_list|()
block|{   }
specifier|private
name|NamespaceDescriptor
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/**    * Getter for accessing the configuration value by key    */
specifier|public
name|String
name|getConfigurationValue
parameter_list|(
name|String
name|key
parameter_list|)
block|{
return|return
name|configuration
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
comment|/**    * Getter for fetching an unmodifiable {@link #configuration} map.    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getConfiguration
parameter_list|()
block|{
comment|// shallow pointer copy
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|configuration
argument_list|)
return|;
block|}
comment|/**    * Setter for storing a configuration setting in {@link #configuration} map.    * @param key Config key. Same as XML config key e.g. hbase.something.or.other.    * @param value String value. If null, removes the setting.    */
specifier|public
name|void
name|setConfiguration
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|removeConfiguration
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|configuration
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
comment|/**    * Remove a config setting represented by the key from the {@link #configuration} map    */
specifier|public
name|void
name|removeConfiguration
parameter_list|(
specifier|final
name|String
name|key
parameter_list|)
block|{
name|configuration
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|s
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|s
operator|.
name|append
argument_list|(
literal|'{'
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
name|HConstants
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
literal|" => '"
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
literal|"'"
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|e
range|:
name|configuration
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|key
init|=
name|e
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|value
init|=
name|e
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|key
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|s
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
literal|" => '"
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
literal|"'"
argument_list|)
expr_stmt|;
block|}
name|s
operator|.
name|append
argument_list|(
literal|'}'
argument_list|)
expr_stmt|;
return|return
name|s
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|Builder
name|create
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Builder
name|create
parameter_list|(
name|NamespaceDescriptor
name|ns
parameter_list|)
block|{
return|return
operator|new
name|Builder
argument_list|(
name|ns
argument_list|)
return|;
block|}
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|static
class|class
name|Builder
block|{
specifier|private
name|String
name|bName
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|bConfiguration
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|Builder
parameter_list|(
name|NamespaceDescriptor
name|ns
parameter_list|)
block|{
name|this
operator|.
name|bName
operator|=
name|ns
operator|.
name|name
expr_stmt|;
name|this
operator|.
name|bConfiguration
operator|=
name|ns
operator|.
name|configuration
expr_stmt|;
block|}
specifier|private
name|Builder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|bName
operator|=
name|name
expr_stmt|;
block|}
specifier|public
name|Builder
name|addConfiguration
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|configuration
parameter_list|)
block|{
name|this
operator|.
name|bConfiguration
operator|.
name|putAll
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|addConfiguration
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|this
operator|.
name|bConfiguration
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|removeConfiguration
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|this
operator|.
name|bConfiguration
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|NamespaceDescriptor
name|build
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|bName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"A name has to be specified in a namespace."
argument_list|)
throw|;
block|}
name|NamespaceDescriptor
name|desc
init|=
operator|new
name|NamespaceDescriptor
argument_list|(
name|this
operator|.
name|bName
argument_list|)
decl_stmt|;
name|desc
operator|.
name|configuration
operator|=
name|this
operator|.
name|bConfiguration
expr_stmt|;
return|return
name|desc
return|;
block|}
block|}
block|}
end_class

end_unit

