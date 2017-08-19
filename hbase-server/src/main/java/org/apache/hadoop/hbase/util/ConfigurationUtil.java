begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|AbstractMap
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

begin_comment
comment|/**  * Utilities for storing more complex collection types in  * {@link org.apache.hadoop.conf.Configuration} instances.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
specifier|final
class|class
name|ConfigurationUtil
block|{
comment|// TODO: hopefully this is a good delimiter; it's not in the base64 alphabet,
comment|// nor is it valid for paths
specifier|public
specifier|static
specifier|final
name|char
name|KVP_DELIMITER
init|=
literal|'^'
decl_stmt|;
comment|// Disallow instantiation
specifier|private
name|ConfigurationUtil
parameter_list|()
block|{    }
comment|/**    * Store a collection of Map.Entry's in conf, with each entry separated by ','    * and key values delimited by {@link #KVP_DELIMITER}    *    * @param conf      configuration to store the collection in    * @param key       overall key to store keyValues under    * @param keyValues kvps to be stored under key in conf    */
specifier|public
specifier|static
name|void
name|setKeyValues
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|key
parameter_list|,
name|Collection
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
name|keyValues
parameter_list|)
block|{
name|setKeyValues
argument_list|(
name|conf
argument_list|,
name|key
argument_list|,
name|keyValues
argument_list|,
name|KVP_DELIMITER
argument_list|)
expr_stmt|;
block|}
comment|/**    * Store a collection of Map.Entry's in conf, with each entry separated by ','    * and key values delimited by delimiter.    *    * @param conf      configuration to store the collection in    * @param key       overall key to store keyValues under    * @param keyValues kvps to be stored under key in conf    * @param delimiter character used to separate each kvp    */
specifier|public
specifier|static
name|void
name|setKeyValues
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|key
parameter_list|,
name|Collection
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
name|keyValues
parameter_list|,
name|char
name|delimiter
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|serializedKvps
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
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
name|kvp
range|:
name|keyValues
control|)
block|{
name|serializedKvps
operator|.
name|add
argument_list|(
name|kvp
operator|.
name|getKey
argument_list|()
operator|+
name|delimiter
operator|+
name|kvp
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|conf
operator|.
name|setStrings
argument_list|(
name|key
argument_list|,
name|serializedKvps
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|serializedKvps
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Retrieve a list of key value pairs from configuration, stored under the provided key    *    * @param conf configuration to retrieve kvps from    * @param key  key under which the key values are stored    * @return the list of kvps stored under key in conf, or null if the key isn't present.    * @see #setKeyValues(Configuration, String, Collection, char)    */
specifier|public
specifier|static
name|List
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
name|getKeyValues
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|key
parameter_list|)
block|{
return|return
name|getKeyValues
argument_list|(
name|conf
argument_list|,
name|key
argument_list|,
name|KVP_DELIMITER
argument_list|)
return|;
block|}
comment|/**    * Retrieve a list of key value pairs from configuration, stored under the provided key    *    * @param conf      configuration to retrieve kvps from    * @param key       key under which the key values are stored    * @param delimiter character used to separate each kvp    * @return the list of kvps stored under key in conf, or null if the key isn't present.    * @see #setKeyValues(Configuration, String, Collection, char)    */
specifier|public
specifier|static
name|List
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
name|getKeyValues
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|key
parameter_list|,
name|char
name|delimiter
parameter_list|)
block|{
name|String
index|[]
name|kvps
init|=
name|conf
operator|.
name|getStrings
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|kvps
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
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
name|rtn
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|kvp
range|:
name|kvps
control|)
block|{
name|String
index|[]
name|splitKvp
init|=
name|StringUtils
operator|.
name|split
argument_list|(
name|kvp
argument_list|,
name|delimiter
argument_list|)
decl_stmt|;
if|if
condition|(
name|splitKvp
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Expected key value pair for configuration key '"
operator|+
name|key
operator|+
literal|"'"
operator|+
literal|" to be of form '<key>"
operator|+
name|delimiter
operator|+
literal|"<value>; was "
operator|+
name|kvp
operator|+
literal|" instead"
argument_list|)
throw|;
block|}
name|rtn
operator|.
name|add
argument_list|(
operator|new
name|AbstractMap
operator|.
name|SimpleImmutableEntry
argument_list|<>
argument_list|(
name|splitKvp
index|[
literal|0
index|]
argument_list|,
name|splitKvp
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|rtn
return|;
block|}
block|}
end_class

end_unit

