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
name|quotas
package|;
end_package

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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|TableName
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
name|protobuf
operator|.
name|generated
operator|.
name|QuotaProtos
operator|.
name|Quotas
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
name|EnvironmentEdgeManager
import|;
end_import

begin_comment
comment|/**  * In-Memory state of the user quotas  */
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
class|class
name|UserQuotaState
extends|extends
name|QuotaState
block|{
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|QuotaLimiter
argument_list|>
name|namespaceLimiters
init|=
literal|null
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|TableName
argument_list|,
name|QuotaLimiter
argument_list|>
name|tableLimiters
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|bypassGlobals
init|=
literal|false
decl_stmt|;
specifier|public
name|UserQuotaState
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|UserQuotaState
parameter_list|(
specifier|final
name|long
name|updateTs
parameter_list|)
block|{
name|super
argument_list|(
name|updateTs
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"UserQuotaState(ts="
operator|+
name|getLastUpdate
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|bypassGlobals
condition|)
name|builder
operator|.
name|append
argument_list|(
literal|" bypass-globals"
argument_list|)
expr_stmt|;
if|if
condition|(
name|isBypass
argument_list|()
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|" bypass"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|getGlobalLimiterWithoutUpdatingLastQuery
argument_list|()
operator|!=
name|NoopQuotaLimiter
operator|.
name|get
argument_list|()
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|" global-limiter"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|tableLimiters
operator|!=
literal|null
operator|&&
operator|!
name|tableLimiters
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|" ["
argument_list|)
expr_stmt|;
for|for
control|(
name|TableName
name|table
range|:
name|tableLimiters
operator|.
name|keySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|" "
operator|+
name|table
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
literal|" ]"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|namespaceLimiters
operator|!=
literal|null
operator|&&
operator|!
name|namespaceLimiters
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|" ["
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|ns
range|:
name|namespaceLimiters
operator|.
name|keySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|" "
operator|+
name|ns
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
literal|" ]"
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|append
argument_list|(
literal|')'
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * @return true if there is no quota information associated to this object    */
annotation|@
name|Override
specifier|public
specifier|synchronized
name|boolean
name|isBypass
parameter_list|()
block|{
return|return
operator|!
name|bypassGlobals
operator|&&
name|getGlobalLimiterWithoutUpdatingLastQuery
argument_list|()
operator|==
name|NoopQuotaLimiter
operator|.
name|get
argument_list|()
operator|&&
operator|(
name|tableLimiters
operator|==
literal|null
operator|||
name|tableLimiters
operator|.
name|isEmpty
argument_list|()
operator|)
operator|&&
operator|(
name|namespaceLimiters
operator|==
literal|null
operator|||
name|namespaceLimiters
operator|.
name|isEmpty
argument_list|()
operator|)
return|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|hasBypassGlobals
parameter_list|()
block|{
return|return
name|bypassGlobals
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setQuotas
parameter_list|(
specifier|final
name|Quotas
name|quotas
parameter_list|)
block|{
name|super
operator|.
name|setQuotas
argument_list|(
name|quotas
argument_list|)
expr_stmt|;
name|bypassGlobals
operator|=
name|quotas
operator|.
name|getBypassGlobals
argument_list|()
expr_stmt|;
block|}
comment|/**    * Add the quota information of the specified table.    * (This operation is part of the QuotaState setup)    */
specifier|public
name|void
name|setQuotas
parameter_list|(
specifier|final
name|TableName
name|table
parameter_list|,
name|Quotas
name|quotas
parameter_list|)
block|{
name|tableLimiters
operator|=
name|setLimiter
argument_list|(
name|tableLimiters
argument_list|,
name|table
argument_list|,
name|quotas
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add the quota information of the specified namespace.    * (This operation is part of the QuotaState setup)    */
specifier|public
name|void
name|setQuotas
parameter_list|(
specifier|final
name|String
name|namespace
parameter_list|,
name|Quotas
name|quotas
parameter_list|)
block|{
name|namespaceLimiters
operator|=
name|setLimiter
argument_list|(
name|namespaceLimiters
argument_list|,
name|namespace
argument_list|,
name|quotas
argument_list|)
expr_stmt|;
block|}
specifier|private
parameter_list|<
name|K
parameter_list|>
name|Map
argument_list|<
name|K
argument_list|,
name|QuotaLimiter
argument_list|>
name|setLimiter
parameter_list|(
name|Map
argument_list|<
name|K
argument_list|,
name|QuotaLimiter
argument_list|>
name|limiters
parameter_list|,
specifier|final
name|K
name|key
parameter_list|,
specifier|final
name|Quotas
name|quotas
parameter_list|)
block|{
if|if
condition|(
name|limiters
operator|==
literal|null
condition|)
block|{
name|limiters
operator|=
operator|new
name|HashMap
argument_list|<
name|K
argument_list|,
name|QuotaLimiter
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|QuotaLimiter
name|limiter
init|=
name|quotas
operator|.
name|hasThrottle
argument_list|()
condition|?
name|QuotaLimiterFactory
operator|.
name|fromThrottle
argument_list|(
name|quotas
operator|.
name|getThrottle
argument_list|()
argument_list|)
else|:
literal|null
decl_stmt|;
if|if
condition|(
name|limiter
operator|!=
literal|null
operator|&&
operator|!
name|limiter
operator|.
name|isBypass
argument_list|()
condition|)
block|{
name|limiters
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|limiter
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|limiters
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
return|return
name|limiters
return|;
block|}
comment|/**    * Perform an update of the quota state based on the other quota state object.    * (This operation is executed by the QuotaCache)    */
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|update
parameter_list|(
specifier|final
name|QuotaState
name|other
parameter_list|)
block|{
name|super
operator|.
name|update
argument_list|(
name|other
argument_list|)
expr_stmt|;
if|if
condition|(
name|other
operator|instanceof
name|UserQuotaState
condition|)
block|{
name|UserQuotaState
name|uOther
init|=
operator|(
name|UserQuotaState
operator|)
name|other
decl_stmt|;
name|tableLimiters
operator|=
name|updateLimiters
argument_list|(
name|tableLimiters
argument_list|,
name|uOther
operator|.
name|tableLimiters
argument_list|)
expr_stmt|;
name|namespaceLimiters
operator|=
name|updateLimiters
argument_list|(
name|namespaceLimiters
argument_list|,
name|uOther
operator|.
name|namespaceLimiters
argument_list|)
expr_stmt|;
name|bypassGlobals
operator|=
name|uOther
operator|.
name|bypassGlobals
expr_stmt|;
block|}
else|else
block|{
name|tableLimiters
operator|=
literal|null
expr_stmt|;
name|namespaceLimiters
operator|=
literal|null
expr_stmt|;
name|bypassGlobals
operator|=
literal|false
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
parameter_list|<
name|K
parameter_list|>
name|Map
argument_list|<
name|K
argument_list|,
name|QuotaLimiter
argument_list|>
name|updateLimiters
parameter_list|(
specifier|final
name|Map
argument_list|<
name|K
argument_list|,
name|QuotaLimiter
argument_list|>
name|map
parameter_list|,
specifier|final
name|Map
argument_list|<
name|K
argument_list|,
name|QuotaLimiter
argument_list|>
name|otherMap
parameter_list|)
block|{
if|if
condition|(
name|map
operator|==
literal|null
condition|)
block|{
return|return
name|otherMap
return|;
block|}
if|if
condition|(
name|otherMap
operator|!=
literal|null
condition|)
block|{
comment|// To Remove
name|Set
argument_list|<
name|K
argument_list|>
name|toRemove
init|=
operator|new
name|HashSet
argument_list|<
name|K
argument_list|>
argument_list|(
name|map
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
name|toRemove
operator|.
name|removeAll
argument_list|(
name|otherMap
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|keySet
argument_list|()
operator|.
name|removeAll
argument_list|(
name|toRemove
argument_list|)
expr_stmt|;
comment|// To Update/Add
for|for
control|(
specifier|final
name|Map
operator|.
name|Entry
argument_list|<
name|K
argument_list|,
name|QuotaLimiter
argument_list|>
name|entry
range|:
name|otherMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|QuotaLimiter
name|limiter
init|=
name|map
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|limiter
operator|==
literal|null
condition|)
block|{
name|limiter
operator|=
name|entry
operator|.
name|getValue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|limiter
operator|=
name|QuotaLimiterFactory
operator|.
name|update
argument_list|(
name|limiter
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|map
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|limiter
argument_list|)
expr_stmt|;
block|}
return|return
name|map
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Return the limiter for the specified table associated with this quota.    * If the table does not have its own quota limiter the global one will be returned.    * In case there is no quota limiter associated with this object a noop limiter will be returned.    *    * @return the quota limiter for the specified table    */
specifier|public
specifier|synchronized
name|QuotaLimiter
name|getTableLimiter
parameter_list|(
specifier|final
name|TableName
name|table
parameter_list|)
block|{
name|lastQuery
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
expr_stmt|;
if|if
condition|(
name|tableLimiters
operator|!=
literal|null
condition|)
block|{
name|QuotaLimiter
name|limiter
init|=
name|tableLimiters
operator|.
name|get
argument_list|(
name|table
argument_list|)
decl_stmt|;
if|if
condition|(
name|limiter
operator|!=
literal|null
condition|)
return|return
name|limiter
return|;
block|}
if|if
condition|(
name|namespaceLimiters
operator|!=
literal|null
condition|)
block|{
name|QuotaLimiter
name|limiter
init|=
name|namespaceLimiters
operator|.
name|get
argument_list|(
name|table
operator|.
name|getNamespaceAsString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|limiter
operator|!=
literal|null
condition|)
return|return
name|limiter
return|;
block|}
return|return
name|getGlobalLimiterWithoutUpdatingLastQuery
argument_list|()
return|;
block|}
block|}
end_class

end_unit

