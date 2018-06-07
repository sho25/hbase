begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
import|import static
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
name|CollectionUtils
operator|.
name|computeIfAbsent
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
name|regionserver
operator|.
name|HRegionServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|ArrayList
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
name|concurrent
operator|.
name|ConcurrentHashMap
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
name|ScheduledChore
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
name|Stoppable
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|client
operator|.
name|Get
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
name|regionserver
operator|.
name|RegionServerServices
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|security
operator|.
name|UserGroupInformation
import|;
end_import

begin_comment
comment|/**  * Cache that keeps track of the quota settings for the users and tables that  * are interacting with it.  *  * To avoid blocking the operations if the requested quota is not in cache  * an "empty quota" will be returned and the request to fetch the quota information  * will be enqueued for the next refresh.  *  * TODO: At the moment the Cache has a Chore that will be triggered every 5min  * or on cache-miss events. Later the Quotas will be pushed using the notification system.  */
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
name|QuotaCache
implements|implements
name|Stoppable
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|QuotaCache
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|REFRESH_CONF_KEY
init|=
literal|"hbase.quota.refresh.period"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|REFRESH_DEFAULT_PERIOD
init|=
literal|5
operator|*
literal|60000
decl_stmt|;
comment|// 5min
specifier|private
specifier|static
specifier|final
name|int
name|EVICT_PERIOD_FACTOR
init|=
literal|5
decl_stmt|;
comment|// N * REFRESH_DEFAULT_PERIOD
comment|// for testing purpose only, enforce the cache to be always refreshed
specifier|static
name|boolean
name|TEST_FORCE_REFRESH
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|QuotaState
argument_list|>
name|namespaceQuotaCache
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentHashMap
argument_list|<
name|TableName
argument_list|,
name|QuotaState
argument_list|>
name|tableQuotaCache
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|UserQuotaState
argument_list|>
name|userQuotaCache
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|RegionServerServices
name|rsServices
decl_stmt|;
specifier|private
name|QuotaRefresherChore
name|refreshChore
decl_stmt|;
specifier|private
name|boolean
name|stopped
init|=
literal|true
decl_stmt|;
specifier|public
name|QuotaCache
parameter_list|(
specifier|final
name|RegionServerServices
name|rsServices
parameter_list|)
block|{
name|this
operator|.
name|rsServices
operator|=
name|rsServices
expr_stmt|;
block|}
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|IOException
block|{
name|stopped
operator|=
literal|false
expr_stmt|;
comment|// TODO: This will be replaced once we have the notification bus ready.
name|Configuration
name|conf
init|=
name|rsServices
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|int
name|period
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|REFRESH_CONF_KEY
argument_list|,
name|REFRESH_DEFAULT_PERIOD
argument_list|)
decl_stmt|;
name|refreshChore
operator|=
operator|new
name|QuotaRefresherChore
argument_list|(
name|period
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|rsServices
operator|.
name|getChoreService
argument_list|()
operator|.
name|scheduleChore
argument_list|(
name|refreshChore
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
specifier|final
name|String
name|why
parameter_list|)
block|{
if|if
condition|(
name|refreshChore
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stopping QuotaRefresherChore chore."
argument_list|)
expr_stmt|;
name|refreshChore
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|stopped
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
name|stopped
return|;
block|}
comment|/**    * Returns the limiter associated to the specified user/table.    *    * @param ugi the user to limit    * @param table the table to limit    * @return the limiter associated to the specified user/table    */
specifier|public
name|QuotaLimiter
name|getUserLimiter
parameter_list|(
specifier|final
name|UserGroupInformation
name|ugi
parameter_list|,
specifier|final
name|TableName
name|table
parameter_list|)
block|{
if|if
condition|(
name|table
operator|.
name|isSystemTable
argument_list|()
condition|)
block|{
return|return
name|NoopQuotaLimiter
operator|.
name|get
argument_list|()
return|;
block|}
return|return
name|getUserQuotaState
argument_list|(
name|ugi
argument_list|)
operator|.
name|getTableLimiter
argument_list|(
name|table
argument_list|)
return|;
block|}
comment|/**    * Returns the QuotaState associated to the specified user.    * @param ugi the user    * @return the quota info associated to specified user    */
specifier|public
name|UserQuotaState
name|getUserQuotaState
parameter_list|(
specifier|final
name|UserGroupInformation
name|ugi
parameter_list|)
block|{
return|return
name|computeIfAbsent
argument_list|(
name|userQuotaCache
argument_list|,
name|ugi
operator|.
name|getShortUserName
argument_list|()
argument_list|,
name|UserQuotaState
operator|::
operator|new
argument_list|,
name|this
operator|::
name|triggerCacheRefresh
argument_list|)
return|;
block|}
comment|/**    * Returns the limiter associated to the specified table.    *    * @param table the table to limit    * @return the limiter associated to the specified table    */
specifier|public
name|QuotaLimiter
name|getTableLimiter
parameter_list|(
specifier|final
name|TableName
name|table
parameter_list|)
block|{
return|return
name|getQuotaState
argument_list|(
name|this
operator|.
name|tableQuotaCache
argument_list|,
name|table
argument_list|)
operator|.
name|getGlobalLimiter
argument_list|()
return|;
block|}
comment|/**    * Returns the limiter associated to the specified namespace.    *    * @param namespace the namespace to limit    * @return the limiter associated to the specified namespace    */
specifier|public
name|QuotaLimiter
name|getNamespaceLimiter
parameter_list|(
specifier|final
name|String
name|namespace
parameter_list|)
block|{
return|return
name|getQuotaState
argument_list|(
name|this
operator|.
name|namespaceQuotaCache
argument_list|,
name|namespace
argument_list|)
operator|.
name|getGlobalLimiter
argument_list|()
return|;
block|}
comment|/**    * Returns the QuotaState requested. If the quota info is not in cache an empty one will be    * returned and the quota request will be enqueued for the next cache refresh.    */
specifier|private
parameter_list|<
name|K
parameter_list|>
name|QuotaState
name|getQuotaState
parameter_list|(
specifier|final
name|ConcurrentHashMap
argument_list|<
name|K
argument_list|,
name|QuotaState
argument_list|>
name|quotasMap
parameter_list|,
specifier|final
name|K
name|key
parameter_list|)
block|{
return|return
name|computeIfAbsent
argument_list|(
name|quotasMap
argument_list|,
name|key
argument_list|,
name|QuotaState
operator|::
operator|new
argument_list|,
name|this
operator|::
name|triggerCacheRefresh
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
name|void
name|triggerCacheRefresh
parameter_list|()
block|{
name|refreshChore
operator|.
name|triggerNow
argument_list|()
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|long
name|getLastUpdate
parameter_list|()
block|{
return|return
name|refreshChore
operator|.
name|lastUpdate
return|;
block|}
annotation|@
name|VisibleForTesting
name|Map
argument_list|<
name|String
argument_list|,
name|QuotaState
argument_list|>
name|getNamespaceQuotaCache
parameter_list|()
block|{
return|return
name|namespaceQuotaCache
return|;
block|}
annotation|@
name|VisibleForTesting
name|Map
argument_list|<
name|TableName
argument_list|,
name|QuotaState
argument_list|>
name|getTableQuotaCache
parameter_list|()
block|{
return|return
name|tableQuotaCache
return|;
block|}
annotation|@
name|VisibleForTesting
name|Map
argument_list|<
name|String
argument_list|,
name|UserQuotaState
argument_list|>
name|getUserQuotaCache
parameter_list|()
block|{
return|return
name|userQuotaCache
return|;
block|}
comment|// TODO: Remove this once we have the notification bus
specifier|private
class|class
name|QuotaRefresherChore
extends|extends
name|ScheduledChore
block|{
specifier|private
name|long
name|lastUpdate
init|=
literal|0
decl_stmt|;
specifier|public
name|QuotaRefresherChore
parameter_list|(
specifier|final
name|int
name|period
parameter_list|,
specifier|final
name|Stoppable
name|stoppable
parameter_list|)
block|{
name|super
argument_list|(
literal|"QuotaRefresherChore"
argument_list|,
name|stoppable
argument_list|,
name|period
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"GC_UNRELATED_TYPES"
argument_list|,
name|justification
operator|=
literal|"I do not understand why the complaints, it looks good to me -- FIX"
argument_list|)
specifier|protected
name|void
name|chore
parameter_list|()
block|{
comment|// Prefetch online tables/namespaces
for|for
control|(
name|TableName
name|table
range|:
operator|(
operator|(
name|HRegionServer
operator|)
name|QuotaCache
operator|.
name|this
operator|.
name|rsServices
operator|)
operator|.
name|getOnlineTables
argument_list|()
control|)
block|{
if|if
condition|(
name|table
operator|.
name|isSystemTable
argument_list|()
condition|)
continue|continue;
if|if
condition|(
operator|!
name|QuotaCache
operator|.
name|this
operator|.
name|tableQuotaCache
operator|.
name|containsKey
argument_list|(
name|table
argument_list|)
condition|)
block|{
name|QuotaCache
operator|.
name|this
operator|.
name|tableQuotaCache
operator|.
name|putIfAbsent
argument_list|(
name|table
argument_list|,
operator|new
name|QuotaState
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|String
name|ns
init|=
name|table
operator|.
name|getNamespaceAsString
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|QuotaCache
operator|.
name|this
operator|.
name|namespaceQuotaCache
operator|.
name|containsKey
argument_list|(
name|ns
argument_list|)
condition|)
block|{
name|QuotaCache
operator|.
name|this
operator|.
name|namespaceQuotaCache
operator|.
name|putIfAbsent
argument_list|(
name|ns
argument_list|,
operator|new
name|QuotaState
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|fetchNamespaceQuotaState
argument_list|()
expr_stmt|;
name|fetchTableQuotaState
argument_list|()
expr_stmt|;
name|fetchUserQuotaState
argument_list|()
expr_stmt|;
name|lastUpdate
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|fetchNamespaceQuotaState
parameter_list|()
block|{
name|fetch
argument_list|(
literal|"namespace"
argument_list|,
name|QuotaCache
operator|.
name|this
operator|.
name|namespaceQuotaCache
argument_list|,
operator|new
name|Fetcher
argument_list|<
name|String
argument_list|,
name|QuotaState
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Get
name|makeGet
parameter_list|(
specifier|final
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|QuotaState
argument_list|>
name|entry
parameter_list|)
block|{
return|return
name|QuotaUtil
operator|.
name|makeGetForNamespaceQuotas
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|QuotaState
argument_list|>
name|fetchEntries
parameter_list|(
specifier|final
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|QuotaUtil
operator|.
name|fetchNamespaceQuotas
argument_list|(
name|rsServices
operator|.
name|getConnection
argument_list|()
argument_list|,
name|gets
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|fetchTableQuotaState
parameter_list|()
block|{
name|fetch
argument_list|(
literal|"table"
argument_list|,
name|QuotaCache
operator|.
name|this
operator|.
name|tableQuotaCache
argument_list|,
operator|new
name|Fetcher
argument_list|<
name|TableName
argument_list|,
name|QuotaState
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Get
name|makeGet
parameter_list|(
specifier|final
name|Map
operator|.
name|Entry
argument_list|<
name|TableName
argument_list|,
name|QuotaState
argument_list|>
name|entry
parameter_list|)
block|{
return|return
name|QuotaUtil
operator|.
name|makeGetForTableQuotas
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|TableName
argument_list|,
name|QuotaState
argument_list|>
name|fetchEntries
parameter_list|(
specifier|final
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|QuotaUtil
operator|.
name|fetchTableQuotas
argument_list|(
name|rsServices
operator|.
name|getConnection
argument_list|()
argument_list|,
name|gets
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|fetchUserQuotaState
parameter_list|()
block|{
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|namespaces
init|=
name|QuotaCache
operator|.
name|this
operator|.
name|namespaceQuotaCache
operator|.
name|keySet
argument_list|()
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|TableName
argument_list|>
name|tables
init|=
name|QuotaCache
operator|.
name|this
operator|.
name|tableQuotaCache
operator|.
name|keySet
argument_list|()
decl_stmt|;
name|fetch
argument_list|(
literal|"user"
argument_list|,
name|QuotaCache
operator|.
name|this
operator|.
name|userQuotaCache
argument_list|,
operator|new
name|Fetcher
argument_list|<
name|String
argument_list|,
name|UserQuotaState
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Get
name|makeGet
parameter_list|(
specifier|final
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|UserQuotaState
argument_list|>
name|entry
parameter_list|)
block|{
return|return
name|QuotaUtil
operator|.
name|makeGetForUserQuotas
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|tables
argument_list|,
name|namespaces
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|UserQuotaState
argument_list|>
name|fetchEntries
parameter_list|(
specifier|final
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|QuotaUtil
operator|.
name|fetchUserQuotas
argument_list|(
name|rsServices
operator|.
name|getConnection
argument_list|()
argument_list|,
name|gets
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
parameter_list|<
name|K
parameter_list|,
name|V
extends|extends
name|QuotaState
parameter_list|>
name|void
name|fetch
parameter_list|(
specifier|final
name|String
name|type
parameter_list|,
specifier|final
name|ConcurrentHashMap
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|quotasMap
parameter_list|,
specifier|final
name|Fetcher
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|fetcher
parameter_list|)
block|{
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|long
name|refreshPeriod
init|=
name|getPeriod
argument_list|()
decl_stmt|;
name|long
name|evictPeriod
init|=
name|refreshPeriod
operator|*
name|EVICT_PERIOD_FACTOR
decl_stmt|;
comment|// Find the quota entries to update
name|List
argument_list|<
name|Get
argument_list|>
name|gets
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|K
argument_list|>
name|toRemove
init|=
operator|new
name|ArrayList
argument_list|<>
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
name|V
argument_list|>
name|entry
range|:
name|quotasMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|long
name|lastUpdate
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getLastUpdate
argument_list|()
decl_stmt|;
name|long
name|lastQuery
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getLastQuery
argument_list|()
decl_stmt|;
if|if
condition|(
name|lastQuery
operator|>
literal|0
operator|&&
operator|(
name|now
operator|-
name|lastQuery
operator|)
operator|>=
name|evictPeriod
condition|)
block|{
name|toRemove
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|TEST_FORCE_REFRESH
operator|||
operator|(
name|now
operator|-
name|lastUpdate
operator|)
operator|>=
name|refreshPeriod
condition|)
block|{
name|gets
operator|.
name|add
argument_list|(
name|fetcher
operator|.
name|makeGet
argument_list|(
name|entry
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
specifier|final
name|K
name|key
range|:
name|toRemove
control|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"evict "
operator|+
name|type
operator|+
literal|" key="
operator|+
name|key
argument_list|)
expr_stmt|;
block|}
name|quotasMap
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
comment|// fetch and update the quota entries
if|if
condition|(
operator|!
name|gets
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
try|try
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|entry
range|:
name|fetcher
operator|.
name|fetchEntries
argument_list|(
name|gets
argument_list|)
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|V
name|quotaInfo
init|=
name|quotasMap
operator|.
name|putIfAbsent
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
decl_stmt|;
if|if
condition|(
name|quotaInfo
operator|!=
literal|null
condition|)
block|{
name|quotaInfo
operator|.
name|update
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"refresh "
operator|+
name|type
operator|+
literal|" key="
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|" quotas="
operator|+
name|quotaInfo
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to read "
operator|+
name|type
operator|+
literal|" from quota table"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|static
interface|interface
name|Fetcher
parameter_list|<
name|Key
parameter_list|,
name|Value
parameter_list|>
block|{
name|Get
name|makeGet
parameter_list|(
name|Map
operator|.
name|Entry
argument_list|<
name|Key
argument_list|,
name|Value
argument_list|>
name|entry
parameter_list|)
function_decl|;
name|Map
argument_list|<
name|Key
argument_list|,
name|Value
argument_list|>
name|fetchEntries
parameter_list|(
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
block|}
end_class

end_unit

