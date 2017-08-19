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
operator|.
name|metrics
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
name|cache
operator|.
name|CacheBuilder
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|cache
operator|.
name|CacheLoader
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|cache
operator|.
name|LoadingCache
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
name|TimeUnit
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
name|yetus
operator|.
name|audience
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
name|metrics2
operator|.
name|MetricsInfo
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
name|metrics2
operator|.
name|MetricsTag
import|;
end_import

begin_comment
comment|/**  * Helpers to create interned metrics info  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|final
class|class
name|Interns
block|{
specifier|private
specifier|static
name|LoadingCache
argument_list|<
name|String
argument_list|,
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|MetricsInfo
argument_list|>
argument_list|>
name|infoCache
init|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|expireAfterAccess
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|DAYS
argument_list|)
operator|.
name|build
argument_list|(
operator|new
name|CacheLoader
argument_list|<
name|String
argument_list|,
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|MetricsInfo
argument_list|>
argument_list|>
argument_list|()
block|{
specifier|public
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|MetricsInfo
argument_list|>
name|load
parameter_list|(
name|String
name|key
parameter_list|)
block|{
return|return
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
return|;
block|}
block|}
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|LoadingCache
argument_list|<
name|MetricsInfo
argument_list|,
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|MetricsTag
argument_list|>
argument_list|>
name|tagCache
init|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|expireAfterAccess
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|DAYS
argument_list|)
operator|.
name|build
argument_list|(
operator|new
name|CacheLoader
argument_list|<
name|MetricsInfo
argument_list|,
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|MetricsTag
argument_list|>
argument_list|>
argument_list|()
block|{
specifier|public
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|MetricsTag
argument_list|>
name|load
parameter_list|(
name|MetricsInfo
name|key
parameter_list|)
block|{
return|return
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
return|;
block|}
block|}
argument_list|)
decl_stmt|;
specifier|private
name|Interns
parameter_list|()
block|{}
comment|/**    * Get a metric info object    *    * @return an interned metric info object    */
specifier|public
specifier|static
name|MetricsInfo
name|info
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|description
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|MetricsInfo
argument_list|>
name|map
init|=
name|infoCache
operator|.
name|getUnchecked
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|MetricsInfo
name|info
init|=
name|map
operator|.
name|get
argument_list|(
name|description
argument_list|)
decl_stmt|;
if|if
condition|(
name|info
operator|==
literal|null
condition|)
block|{
name|info
operator|=
operator|new
name|MetricsInfoImpl
argument_list|(
name|name
argument_list|,
name|description
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|description
argument_list|,
name|info
argument_list|)
expr_stmt|;
block|}
return|return
name|info
return|;
block|}
comment|/**    * Get a metrics tag    *    * @param info  of the tag    * @param value of the tag    * @return an interned metrics tag    */
specifier|public
specifier|static
name|MetricsTag
name|tag
parameter_list|(
name|MetricsInfo
name|info
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|MetricsTag
argument_list|>
name|map
init|=
name|tagCache
operator|.
name|getUnchecked
argument_list|(
name|info
argument_list|)
decl_stmt|;
name|MetricsTag
name|tag
init|=
name|map
operator|.
name|get
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|tag
operator|==
literal|null
condition|)
block|{
name|tag
operator|=
operator|new
name|MetricsTag
argument_list|(
name|info
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|value
argument_list|,
name|tag
argument_list|)
expr_stmt|;
block|}
return|return
name|tag
return|;
block|}
comment|/**    * Get a metrics tag    *    * @param name        of the tag    * @param description of the tag    * @param value       of the tag    * @return an interned metrics tag    */
specifier|public
specifier|static
name|MetricsTag
name|tag
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|description
parameter_list|,
name|String
name|value
parameter_list|)
block|{
return|return
name|tag
argument_list|(
name|info
argument_list|(
name|name
argument_list|,
name|description
argument_list|)
argument_list|,
name|value
argument_list|)
return|;
block|}
block|}
end_class

end_unit

