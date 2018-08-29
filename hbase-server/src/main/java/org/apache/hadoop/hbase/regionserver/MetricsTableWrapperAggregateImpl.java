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
name|regionserver
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|HashMap
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ScheduledExecutorService
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
name|ScheduledFuture
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
name|hadoop
operator|.
name|hbase
operator|.
name|CompatibilitySingletonFactory
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
name|HConstants
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
name|hadoop
operator|.
name|metrics2
operator|.
name|MetricsExecutor
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
name|collect
operator|.
name|Sets
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsTableWrapperAggregateImpl
implements|implements
name|MetricsTableWrapperAggregate
implements|,
name|Closeable
block|{
specifier|private
specifier|final
name|HRegionServer
name|regionServer
decl_stmt|;
specifier|private
name|ScheduledExecutorService
name|executor
decl_stmt|;
specifier|private
name|Runnable
name|runnable
decl_stmt|;
specifier|private
name|long
name|period
decl_stmt|;
specifier|private
name|ScheduledFuture
argument_list|<
name|?
argument_list|>
name|tableMetricsUpdateTask
decl_stmt|;
specifier|private
name|ConcurrentHashMap
argument_list|<
name|TableName
argument_list|,
name|MetricsTableValues
argument_list|>
name|metricsTableMap
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|MetricsTableWrapperAggregateImpl
parameter_list|(
specifier|final
name|HRegionServer
name|regionServer
parameter_list|)
block|{
name|this
operator|.
name|regionServer
operator|=
name|regionServer
expr_stmt|;
name|this
operator|.
name|period
operator|=
name|regionServer
operator|.
name|conf
operator|.
name|getLong
argument_list|(
name|HConstants
operator|.
name|REGIONSERVER_METRICS_PERIOD
argument_list|,
name|HConstants
operator|.
name|DEFAULT_REGIONSERVER_METRICS_PERIOD
argument_list|)
operator|+
literal|1000
expr_stmt|;
name|this
operator|.
name|executor
operator|=
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsExecutor
operator|.
name|class
argument_list|)
operator|.
name|getExecutor
argument_list|()
expr_stmt|;
name|this
operator|.
name|runnable
operator|=
operator|new
name|TableMetricsWrapperRunnable
argument_list|()
expr_stmt|;
name|this
operator|.
name|tableMetricsUpdateTask
operator|=
name|this
operator|.
name|executor
operator|.
name|scheduleWithFixedDelay
argument_list|(
name|this
operator|.
name|runnable
argument_list|,
name|period
argument_list|,
name|this
operator|.
name|period
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
specifier|public
class|class
name|TableMetricsWrapperRunnable
implements|implements
name|Runnable
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|Map
argument_list|<
name|TableName
argument_list|,
name|MetricsTableValues
argument_list|>
name|localMetricsTableMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Region
name|r
range|:
name|regionServer
operator|.
name|getOnlineRegionsLocalContext
argument_list|()
control|)
block|{
name|TableName
name|tbl
init|=
name|r
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getTableName
argument_list|()
decl_stmt|;
name|MetricsTableValues
name|mt
init|=
name|localMetricsTableMap
operator|.
name|get
argument_list|(
name|tbl
argument_list|)
decl_stmt|;
if|if
condition|(
name|mt
operator|==
literal|null
condition|)
block|{
name|mt
operator|=
operator|new
name|MetricsTableValues
argument_list|()
expr_stmt|;
name|localMetricsTableMap
operator|.
name|put
argument_list|(
name|tbl
argument_list|,
name|mt
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|r
operator|.
name|getStores
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Store
name|store
range|:
name|r
operator|.
name|getStores
argument_list|()
control|)
block|{
name|mt
operator|.
name|storeFileCount
operator|+=
name|store
operator|.
name|getStorefilesCount
argument_list|()
expr_stmt|;
name|mt
operator|.
name|memstoreSize
operator|+=
operator|(
name|store
operator|.
name|getMemStoreSize
argument_list|()
operator|.
name|getDataSize
argument_list|()
operator|+
name|store
operator|.
name|getMemStoreSize
argument_list|()
operator|.
name|getHeapSize
argument_list|()
operator|+
name|store
operator|.
name|getMemStoreSize
argument_list|()
operator|.
name|getOffHeapSize
argument_list|()
operator|)
expr_stmt|;
name|mt
operator|.
name|storeFileSize
operator|+=
name|store
operator|.
name|getStorefilesSize
argument_list|()
expr_stmt|;
name|mt
operator|.
name|referenceFileCount
operator|+=
name|store
operator|.
name|getNumReferenceFiles
argument_list|()
expr_stmt|;
name|mt
operator|.
name|maxStoreFileAge
operator|=
name|Math
operator|.
name|max
argument_list|(
name|mt
operator|.
name|maxStoreFileAge
argument_list|,
name|store
operator|.
name|getMaxStoreFileAge
argument_list|()
operator|.
name|getAsLong
argument_list|()
argument_list|)
expr_stmt|;
name|mt
operator|.
name|minStoreFileAge
operator|=
name|Math
operator|.
name|min
argument_list|(
name|mt
operator|.
name|minStoreFileAge
argument_list|,
name|store
operator|.
name|getMinStoreFileAge
argument_list|()
operator|.
name|getAsLong
argument_list|()
argument_list|)
expr_stmt|;
name|mt
operator|.
name|totalStoreFileAge
operator|=
operator|(
name|long
operator|)
name|store
operator|.
name|getAvgStoreFileAge
argument_list|()
operator|.
name|getAsDouble
argument_list|()
operator|*
name|store
operator|.
name|getStorefilesCount
argument_list|()
expr_stmt|;
name|mt
operator|.
name|storeCount
operator|+=
literal|1
expr_stmt|;
block|}
name|mt
operator|.
name|regionCount
operator|+=
literal|1
expr_stmt|;
name|mt
operator|.
name|readRequestCount
operator|+=
name|r
operator|.
name|getReadRequestsCount
argument_list|()
expr_stmt|;
name|mt
operator|.
name|filteredReadRequestCount
operator|+=
name|getFilteredReadRequestCount
argument_list|(
name|tbl
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
name|mt
operator|.
name|writeRequestCount
operator|+=
name|r
operator|.
name|getWriteRequestsCount
argument_list|()
expr_stmt|;
block|}
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|TableName
argument_list|,
name|MetricsTableValues
argument_list|>
name|entry
range|:
name|localMetricsTableMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|TableName
name|tbl
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|metricsTableMap
operator|.
name|get
argument_list|(
name|tbl
argument_list|)
operator|==
literal|null
condition|)
block|{
comment|// this will add the Wrapper to the list of TableMetrics
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsRegionServerSourceFactory
operator|.
name|class
argument_list|)
operator|.
name|getTableAggregate
argument_list|()
operator|.
name|getOrCreateTableSource
argument_list|(
name|tbl
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|MetricsTableWrapperAggregateImpl
operator|.
name|this
argument_list|)
expr_stmt|;
block|}
name|metricsTableMap
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
name|Set
argument_list|<
name|TableName
argument_list|>
name|existingTableNames
init|=
name|Sets
operator|.
name|newHashSet
argument_list|(
name|metricsTableMap
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
name|existingTableNames
operator|.
name|removeAll
argument_list|(
name|localMetricsTableMap
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
name|MetricsTableAggregateSource
name|agg
init|=
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsRegionServerSourceFactory
operator|.
name|class
argument_list|)
operator|.
name|getTableAggregate
argument_list|()
decl_stmt|;
for|for
control|(
name|TableName
name|table
range|:
name|existingTableNames
control|)
block|{
name|agg
operator|.
name|deleteTableSource
argument_list|(
name|table
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|metricsTableMap
operator|.
name|get
argument_list|(
name|table
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|metricsTableMap
operator|.
name|remove
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|getReadRequestCount
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|MetricsTableValues
name|metricsTable
init|=
name|metricsTableMap
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|metricsTable
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
else|else
block|{
return|return
name|metricsTable
operator|.
name|readRequestCount
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCpRequestsCount
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|MetricsTableValues
name|metricsTable
init|=
name|metricsTableMap
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|metricsTable
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
else|else
block|{
return|return
name|metricsTable
operator|.
name|cpRequestCount
return|;
block|}
block|}
specifier|public
name|long
name|getFilteredReadRequestCount
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|MetricsTableValues
name|metricsTable
init|=
name|metricsTableMap
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|metricsTable
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|metricsTable
operator|.
name|filteredReadRequestCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getWriteRequestCount
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|MetricsTableValues
name|metricsTable
init|=
name|metricsTableMap
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|metricsTable
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
else|else
block|{
return|return
name|metricsTable
operator|.
name|writeRequestCount
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|getTotalRequestsCount
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|MetricsTableValues
name|metricsTable
init|=
name|metricsTableMap
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|metricsTable
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
else|else
block|{
return|return
name|metricsTable
operator|.
name|readRequestCount
operator|+
name|metricsTable
operator|.
name|writeRequestCount
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMemStoreSize
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|MetricsTableValues
name|metricsTable
init|=
name|metricsTableMap
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|metricsTable
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
else|else
block|{
return|return
name|metricsTable
operator|.
name|memstoreSize
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|getStoreFileSize
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|MetricsTableValues
name|metricsTable
init|=
name|metricsTableMap
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|metricsTable
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
else|else
block|{
return|return
name|metricsTable
operator|.
name|storeFileSize
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|getTableSize
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|MetricsTableValues
name|metricsTable
init|=
name|metricsTableMap
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|metricsTable
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
else|else
block|{
return|return
name|metricsTable
operator|.
name|memstoreSize
operator|+
name|metricsTable
operator|.
name|storeFileSize
return|;
block|}
block|}
specifier|public
name|long
name|getNumRegions
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|MetricsTableValues
name|metricsTable
init|=
name|metricsTableMap
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|metricsTable
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|metricsTable
operator|.
name|regionCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumStores
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|MetricsTableValues
name|metricsTable
init|=
name|metricsTableMap
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|metricsTable
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|metricsTable
operator|.
name|storeCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumStoreFiles
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|MetricsTableValues
name|metricsTable
init|=
name|metricsTableMap
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|metricsTable
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|metricsTable
operator|.
name|storeFileCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMaxStoreFileAge
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|MetricsTableValues
name|metricsTable
init|=
name|metricsTableMap
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|metricsTable
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|metricsTable
operator|.
name|maxStoreFileAge
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMinStoreFileAge
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|MetricsTableValues
name|metricsTable
init|=
name|metricsTableMap
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|metricsTable
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|metricsTable
operator|.
name|minStoreFileAge
operator|==
name|Long
operator|.
name|MAX_VALUE
condition|?
literal|0
else|:
name|metricsTable
operator|.
name|minStoreFileAge
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getAvgStoreFileAge
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|MetricsTableValues
name|metricsTable
init|=
name|metricsTableMap
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|metricsTable
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|metricsTable
operator|.
name|storeFileCount
operator|==
literal|0
condition|?
literal|0
else|:
operator|(
name|metricsTable
operator|.
name|totalStoreFileAge
operator|/
name|metricsTable
operator|.
name|storeFileCount
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumReferenceFiles
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|MetricsTableValues
name|metricsTable
init|=
name|metricsTableMap
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|metricsTable
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|metricsTable
operator|.
name|referenceFileCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getAvgRegionSize
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|MetricsTableValues
name|metricsTable
init|=
name|metricsTableMap
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|metricsTable
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|metricsTable
operator|.
name|regionCount
operator|==
literal|0
condition|?
literal|0
else|:
operator|(
name|metricsTable
operator|.
name|memstoreSize
operator|+
name|metricsTable
operator|.
name|storeFileSize
operator|)
operator|/
name|metricsTable
operator|.
name|regionCount
return|;
block|}
specifier|public
name|long
name|getCpRequestCount
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|MetricsTableValues
name|metricsTable
init|=
name|metricsTableMap
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|metricsTable
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|metricsTable
operator|.
name|cpRequestCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|tableMetricsUpdateTask
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|MetricsTableValues
block|{
name|long
name|readRequestCount
decl_stmt|;
name|long
name|filteredReadRequestCount
decl_stmt|;
name|long
name|writeRequestCount
decl_stmt|;
name|long
name|memstoreSize
decl_stmt|;
name|long
name|regionCount
decl_stmt|;
name|long
name|storeCount
decl_stmt|;
name|long
name|storeFileCount
decl_stmt|;
name|long
name|storeFileSize
decl_stmt|;
name|long
name|maxStoreFileAge
decl_stmt|;
name|long
name|minStoreFileAge
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|long
name|totalStoreFileAge
decl_stmt|;
name|long
name|referenceFileCount
decl_stmt|;
name|long
name|cpRequestCount
decl_stmt|;
block|}
block|}
end_class

end_unit

