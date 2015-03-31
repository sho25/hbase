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
name|List
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
name|TimeUnit
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
name|lang
operator|.
name|StringUtils
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
name|HDFSBlocksDistribution
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
name|ServerName
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
name|io
operator|.
name|hfile
operator|.
name|BlockCache
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
name|io
operator|.
name|hfile
operator|.
name|CacheConfig
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
name|io
operator|.
name|hfile
operator|.
name|CacheStats
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
name|wal
operator|.
name|DefaultWALProvider
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
name|hbase
operator|.
name|util
operator|.
name|FSUtils
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
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
name|hdfs
operator|.
name|DFSHedgedReadMetrics
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

begin_comment
comment|/**  * Impl for exposing HRegionServer Information through Hadoop's metrics 2 system.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|MetricsRegionServerWrapperImpl
implements|implements
name|MetricsRegionServerWrapper
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|MetricsRegionServerWrapperImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HRegionServer
name|regionServer
decl_stmt|;
specifier|private
name|BlockCache
name|blockCache
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|numStores
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|numWALFiles
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|walFileSize
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|numStoreFiles
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|memstoreSize
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|storeFileSize
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|double
name|requestsPerSecond
init|=
literal|0.0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|readRequestsCount
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|writeRequestsCount
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|checkAndMutateChecksFailed
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|checkAndMutateChecksPassed
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|storefileIndexSize
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|totalStaticIndexSize
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|totalStaticBloomSize
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|numMutationsWithoutWAL
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|dataInMemoryWithoutWAL
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|int
name|percentFileLocal
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|flushedCellsCount
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|compactedCellsCount
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|majorCompactedCellsCount
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|flushedCellsSize
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|compactedCellsSize
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|majorCompactedCellsSize
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|blockedRequestsCount
init|=
literal|0L
decl_stmt|;
specifier|private
name|CacheStats
name|cacheStats
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
comment|/**    * Can be null if not on hdfs.    */
specifier|private
name|DFSHedgedReadMetrics
name|dfsHedgedReadMetrics
decl_stmt|;
specifier|public
name|MetricsRegionServerWrapperImpl
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
name|initBlockCache
argument_list|()
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
name|RegionServerMetricsWrapperRunnable
argument_list|()
expr_stmt|;
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
name|this
operator|.
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
try|try
block|{
name|this
operator|.
name|dfsHedgedReadMetrics
operator|=
name|FSUtils
operator|.
name|getDFSHedgedReadMetrics
argument_list|(
name|regionServer
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
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
literal|"Failed to get hedged metrics"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Computing regionserver metrics every "
operator|+
name|this
operator|.
name|period
operator|+
literal|" milliseconds"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * It's possible that due to threading the block cache could not be initialized    * yet (testing multiple region servers in one jvm).  So we need to try and initialize    * the blockCache and cacheStats reference multiple times until we succeed.    */
specifier|private
specifier|synchronized
name|void
name|initBlockCache
parameter_list|()
block|{
name|CacheConfig
name|cacheConfig
init|=
name|this
operator|.
name|regionServer
operator|.
name|cacheConfig
decl_stmt|;
if|if
condition|(
name|cacheConfig
operator|!=
literal|null
operator|&&
name|this
operator|.
name|blockCache
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|blockCache
operator|=
name|cacheConfig
operator|.
name|getBlockCache
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|blockCache
operator|!=
literal|null
operator|&&
name|this
operator|.
name|cacheStats
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|cacheStats
operator|=
name|blockCache
operator|.
name|getStats
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|getClusterId
parameter_list|()
block|{
return|return
name|regionServer
operator|.
name|getClusterId
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getStartCode
parameter_list|()
block|{
return|return
name|regionServer
operator|.
name|getStartcode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getZookeeperQuorum
parameter_list|()
block|{
name|ZooKeeperWatcher
name|zk
init|=
name|regionServer
operator|.
name|getZooKeeper
argument_list|()
decl_stmt|;
if|if
condition|(
name|zk
operator|==
literal|null
condition|)
block|{
return|return
literal|""
return|;
block|}
return|return
name|zk
operator|.
name|getQuorum
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getCoprocessors
parameter_list|()
block|{
name|String
index|[]
name|coprocessors
init|=
name|regionServer
operator|.
name|getRegionServerCoprocessors
argument_list|()
decl_stmt|;
if|if
condition|(
name|coprocessors
operator|==
literal|null
operator|||
name|coprocessors
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|""
return|;
block|}
return|return
name|StringUtils
operator|.
name|join
argument_list|(
name|coprocessors
argument_list|,
literal|", "
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getServerName
parameter_list|()
block|{
name|ServerName
name|serverName
init|=
name|regionServer
operator|.
name|getServerName
argument_list|()
decl_stmt|;
if|if
condition|(
name|serverName
operator|==
literal|null
condition|)
block|{
return|return
literal|""
return|;
block|}
return|return
name|serverName
operator|.
name|getServerName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumOnlineRegions
parameter_list|()
block|{
name|Collection
argument_list|<
name|Region
argument_list|>
name|onlineRegionsLocalContext
init|=
name|regionServer
operator|.
name|getOnlineRegionsLocalContext
argument_list|()
decl_stmt|;
if|if
condition|(
name|onlineRegionsLocalContext
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|onlineRegionsLocalContext
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getTotalRequestCount
parameter_list|()
block|{
return|return
name|regionServer
operator|.
name|rpcServices
operator|.
name|requestCount
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getSplitQueueSize
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|regionServer
operator|.
name|compactSplitThread
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|this
operator|.
name|regionServer
operator|.
name|compactSplitThread
operator|.
name|getSplitQueueSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getCompactionQueueSize
parameter_list|()
block|{
comment|//The thread could be zero.  if so assume there is no queue.
if|if
condition|(
name|this
operator|.
name|regionServer
operator|.
name|compactSplitThread
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|this
operator|.
name|regionServer
operator|.
name|compactSplitThread
operator|.
name|getCompactionQueueSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getSmallCompactionQueueSize
parameter_list|()
block|{
comment|//The thread could be zero.  if so assume there is no queue.
if|if
condition|(
name|this
operator|.
name|regionServer
operator|.
name|compactSplitThread
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|this
operator|.
name|regionServer
operator|.
name|compactSplitThread
operator|.
name|getSmallCompactionQueueSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getLargeCompactionQueueSize
parameter_list|()
block|{
comment|//The thread could be zero.  if so assume there is no queue.
if|if
condition|(
name|this
operator|.
name|regionServer
operator|.
name|compactSplitThread
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|this
operator|.
name|regionServer
operator|.
name|compactSplitThread
operator|.
name|getLargeCompactionQueueSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getFlushQueueSize
parameter_list|()
block|{
comment|//If there is no flusher there should be no queue.
if|if
condition|(
name|this
operator|.
name|regionServer
operator|.
name|cacheFlusher
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|this
operator|.
name|regionServer
operator|.
name|cacheFlusher
operator|.
name|getFlushQueueSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBlockCacheCount
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|blockCache
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|this
operator|.
name|blockCache
operator|.
name|getBlockCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBlockCacheSize
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|blockCache
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|this
operator|.
name|blockCache
operator|.
name|getCurrentSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBlockCacheFreeSize
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|blockCache
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|this
operator|.
name|blockCache
operator|.
name|getFreeSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBlockCacheHitCount
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|cacheStats
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|this
operator|.
name|cacheStats
operator|.
name|getHitCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBlockCacheMissCount
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|cacheStats
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|this
operator|.
name|cacheStats
operator|.
name|getMissCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBlockCacheEvictedCount
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|cacheStats
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|this
operator|.
name|cacheStats
operator|.
name|getEvictedCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|getBlockCacheHitPercent
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|cacheStats
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
call|(
name|int
call|)
argument_list|(
name|this
operator|.
name|cacheStats
operator|.
name|getHitRatio
argument_list|()
operator|*
literal|100
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getBlockCacheHitCachingPercent
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|cacheStats
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
call|(
name|int
call|)
argument_list|(
name|this
operator|.
name|cacheStats
operator|.
name|getHitCachingRatio
argument_list|()
operator|*
literal|100
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|forceRecompute
parameter_list|()
block|{
name|this
operator|.
name|runnable
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumStores
parameter_list|()
block|{
return|return
name|numStores
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumWALFiles
parameter_list|()
block|{
return|return
name|numWALFiles
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getWALFileSize
parameter_list|()
block|{
return|return
name|walFileSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumStoreFiles
parameter_list|()
block|{
return|return
name|numStoreFiles
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMemstoreSize
parameter_list|()
block|{
return|return
name|memstoreSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getStoreFileSize
parameter_list|()
block|{
return|return
name|storeFileSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|getRequestsPerSecond
parameter_list|()
block|{
return|return
name|requestsPerSecond
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getReadRequestsCount
parameter_list|()
block|{
return|return
name|readRequestsCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getWriteRequestsCount
parameter_list|()
block|{
return|return
name|writeRequestsCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCheckAndMutateChecksFailed
parameter_list|()
block|{
return|return
name|checkAndMutateChecksFailed
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCheckAndMutateChecksPassed
parameter_list|()
block|{
return|return
name|checkAndMutateChecksPassed
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getStoreFileIndexSize
parameter_list|()
block|{
return|return
name|storefileIndexSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getTotalStaticIndexSize
parameter_list|()
block|{
return|return
name|totalStaticIndexSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getTotalStaticBloomSize
parameter_list|()
block|{
return|return
name|totalStaticBloomSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumMutationsWithoutWAL
parameter_list|()
block|{
return|return
name|numMutationsWithoutWAL
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getDataInMemoryWithoutWAL
parameter_list|()
block|{
return|return
name|dataInMemoryWithoutWAL
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getPercentFileLocal
parameter_list|()
block|{
return|return
name|percentFileLocal
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getUpdatesBlockedTime
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|regionServer
operator|.
name|cacheFlusher
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|this
operator|.
name|regionServer
operator|.
name|cacheFlusher
operator|.
name|getUpdatesBlockedMsHighWater
argument_list|()
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getFlushedCellsCount
parameter_list|()
block|{
return|return
name|flushedCellsCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCompactedCellsCount
parameter_list|()
block|{
return|return
name|compactedCellsCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMajorCompactedCellsCount
parameter_list|()
block|{
return|return
name|majorCompactedCellsCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getFlushedCellsSize
parameter_list|()
block|{
return|return
name|flushedCellsSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCompactedCellsSize
parameter_list|()
block|{
return|return
name|compactedCellsSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMajorCompactedCellsSize
parameter_list|()
block|{
return|return
name|majorCompactedCellsSize
return|;
block|}
comment|/**    * This is the runnable that will be executed on the executor every PERIOD number of seconds    * It will take metrics/numbers from all of the regions and use them to compute point in    * time metrics.    */
specifier|public
class|class
name|RegionServerMetricsWrapperRunnable
implements|implements
name|Runnable
block|{
specifier|private
name|long
name|lastRan
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|lastRequestCount
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|synchronized
specifier|public
name|void
name|run
parameter_list|()
block|{
name|initBlockCache
argument_list|()
expr_stmt|;
name|cacheStats
operator|=
name|blockCache
operator|.
name|getStats
argument_list|()
expr_stmt|;
name|HDFSBlocksDistribution
name|hdfsBlocksDistribution
init|=
operator|new
name|HDFSBlocksDistribution
argument_list|()
decl_stmt|;
name|long
name|tempNumStores
init|=
literal|0
decl_stmt|;
name|long
name|tempNumStoreFiles
init|=
literal|0
decl_stmt|;
name|long
name|tempMemstoreSize
init|=
literal|0
decl_stmt|;
name|long
name|tempStoreFileSize
init|=
literal|0
decl_stmt|;
name|long
name|tempReadRequestsCount
init|=
literal|0
decl_stmt|;
name|long
name|tempWriteRequestsCount
init|=
literal|0
decl_stmt|;
name|long
name|tempCheckAndMutateChecksFailed
init|=
literal|0
decl_stmt|;
name|long
name|tempCheckAndMutateChecksPassed
init|=
literal|0
decl_stmt|;
name|long
name|tempStorefileIndexSize
init|=
literal|0
decl_stmt|;
name|long
name|tempTotalStaticIndexSize
init|=
literal|0
decl_stmt|;
name|long
name|tempTotalStaticBloomSize
init|=
literal|0
decl_stmt|;
name|long
name|tempNumMutationsWithoutWAL
init|=
literal|0
decl_stmt|;
name|long
name|tempDataInMemoryWithoutWAL
init|=
literal|0
decl_stmt|;
name|int
name|tempPercentFileLocal
init|=
literal|0
decl_stmt|;
name|long
name|tempFlushedCellsCount
init|=
literal|0
decl_stmt|;
name|long
name|tempCompactedCellsCount
init|=
literal|0
decl_stmt|;
name|long
name|tempMajorCompactedCellsCount
init|=
literal|0
decl_stmt|;
name|long
name|tempFlushedCellsSize
init|=
literal|0
decl_stmt|;
name|long
name|tempCompactedCellsSize
init|=
literal|0
decl_stmt|;
name|long
name|tempMajorCompactedCellsSize
init|=
literal|0
decl_stmt|;
name|long
name|tempBlockedRequestsCount
init|=
literal|0L
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
name|tempNumMutationsWithoutWAL
operator|+=
name|r
operator|.
name|getNumMutationsWithoutWAL
argument_list|()
expr_stmt|;
name|tempDataInMemoryWithoutWAL
operator|+=
name|r
operator|.
name|getDataInMemoryWithoutWAL
argument_list|()
expr_stmt|;
name|tempReadRequestsCount
operator|+=
name|r
operator|.
name|getReadRequestsCount
argument_list|()
expr_stmt|;
name|tempWriteRequestsCount
operator|+=
name|r
operator|.
name|getWriteRequestsCount
argument_list|()
expr_stmt|;
name|tempCheckAndMutateChecksFailed
operator|+=
name|r
operator|.
name|getCheckAndMutateChecksFailed
argument_list|()
expr_stmt|;
name|tempCheckAndMutateChecksPassed
operator|+=
name|r
operator|.
name|getCheckAndMutateChecksPassed
argument_list|()
expr_stmt|;
name|tempBlockedRequestsCount
operator|+=
name|r
operator|.
name|getBlockedRequestsCount
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|Store
argument_list|>
name|storeList
init|=
name|r
operator|.
name|getStores
argument_list|()
decl_stmt|;
name|tempNumStores
operator|+=
name|storeList
operator|.
name|size
argument_list|()
expr_stmt|;
for|for
control|(
name|Store
name|store
range|:
name|storeList
control|)
block|{
name|tempNumStoreFiles
operator|+=
name|store
operator|.
name|getStorefilesCount
argument_list|()
expr_stmt|;
name|tempMemstoreSize
operator|+=
name|store
operator|.
name|getMemStoreSize
argument_list|()
expr_stmt|;
name|tempStoreFileSize
operator|+=
name|store
operator|.
name|getStorefilesSize
argument_list|()
expr_stmt|;
name|tempStorefileIndexSize
operator|+=
name|store
operator|.
name|getStorefilesIndexSize
argument_list|()
expr_stmt|;
name|tempTotalStaticBloomSize
operator|+=
name|store
operator|.
name|getTotalStaticBloomSize
argument_list|()
expr_stmt|;
name|tempTotalStaticIndexSize
operator|+=
name|store
operator|.
name|getTotalStaticIndexSize
argument_list|()
expr_stmt|;
name|tempFlushedCellsCount
operator|+=
name|store
operator|.
name|getFlushedCellsCount
argument_list|()
expr_stmt|;
name|tempCompactedCellsCount
operator|+=
name|store
operator|.
name|getCompactedCellsCount
argument_list|()
expr_stmt|;
name|tempMajorCompactedCellsCount
operator|+=
name|store
operator|.
name|getMajorCompactedCellsCount
argument_list|()
expr_stmt|;
name|tempFlushedCellsSize
operator|+=
name|store
operator|.
name|getFlushedCellsSize
argument_list|()
expr_stmt|;
name|tempCompactedCellsSize
operator|+=
name|store
operator|.
name|getCompactedCellsSize
argument_list|()
expr_stmt|;
name|tempMajorCompactedCellsSize
operator|+=
name|store
operator|.
name|getMajorCompactedCellsSize
argument_list|()
expr_stmt|;
block|}
name|hdfsBlocksDistribution
operator|.
name|add
argument_list|(
name|r
operator|.
name|getHDFSBlocksDistribution
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|float
name|localityIndex
init|=
name|hdfsBlocksDistribution
operator|.
name|getBlockLocalityIndex
argument_list|(
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getHostname
argument_list|()
argument_list|)
decl_stmt|;
name|tempPercentFileLocal
operator|=
call|(
name|int
call|)
argument_list|(
name|localityIndex
operator|*
literal|100
argument_list|)
expr_stmt|;
comment|//Compute the number of requests per second
name|long
name|currentTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
comment|// assume that it took PERIOD seconds to start the executor.
comment|// this is a guess but it's a pretty good one.
if|if
condition|(
name|lastRan
operator|==
literal|0
condition|)
block|{
name|lastRan
operator|=
name|currentTime
operator|-
name|period
expr_stmt|;
block|}
comment|//If we've time traveled keep the last requests per second.
if|if
condition|(
operator|(
name|currentTime
operator|-
name|lastRan
operator|)
operator|>
literal|0
condition|)
block|{
name|long
name|currentRequestCount
init|=
name|getTotalRequestCount
argument_list|()
decl_stmt|;
name|requestsPerSecond
operator|=
operator|(
name|currentRequestCount
operator|-
name|lastRequestCount
operator|)
operator|/
operator|(
operator|(
name|currentTime
operator|-
name|lastRan
operator|)
operator|/
literal|1000.0
operator|)
expr_stmt|;
name|lastRequestCount
operator|=
name|currentRequestCount
expr_stmt|;
block|}
name|lastRan
operator|=
name|currentTime
expr_stmt|;
name|numWALFiles
operator|=
name|DefaultWALProvider
operator|.
name|getNumLogFiles
argument_list|(
name|regionServer
operator|.
name|walFactory
argument_list|)
expr_stmt|;
name|walFileSize
operator|=
name|DefaultWALProvider
operator|.
name|getLogFileSize
argument_list|(
name|regionServer
operator|.
name|walFactory
argument_list|)
expr_stmt|;
comment|//Copy over computed values so that no thread sees half computed values.
name|numStores
operator|=
name|tempNumStores
expr_stmt|;
name|numStoreFiles
operator|=
name|tempNumStoreFiles
expr_stmt|;
name|memstoreSize
operator|=
name|tempMemstoreSize
expr_stmt|;
name|storeFileSize
operator|=
name|tempStoreFileSize
expr_stmt|;
name|readRequestsCount
operator|=
name|tempReadRequestsCount
expr_stmt|;
name|writeRequestsCount
operator|=
name|tempWriteRequestsCount
expr_stmt|;
name|checkAndMutateChecksFailed
operator|=
name|tempCheckAndMutateChecksFailed
expr_stmt|;
name|checkAndMutateChecksPassed
operator|=
name|tempCheckAndMutateChecksPassed
expr_stmt|;
name|storefileIndexSize
operator|=
name|tempStorefileIndexSize
expr_stmt|;
name|totalStaticIndexSize
operator|=
name|tempTotalStaticIndexSize
expr_stmt|;
name|totalStaticBloomSize
operator|=
name|tempTotalStaticBloomSize
expr_stmt|;
name|numMutationsWithoutWAL
operator|=
name|tempNumMutationsWithoutWAL
expr_stmt|;
name|dataInMemoryWithoutWAL
operator|=
name|tempDataInMemoryWithoutWAL
expr_stmt|;
name|percentFileLocal
operator|=
name|tempPercentFileLocal
expr_stmt|;
name|flushedCellsCount
operator|=
name|tempFlushedCellsCount
expr_stmt|;
name|compactedCellsCount
operator|=
name|tempCompactedCellsCount
expr_stmt|;
name|majorCompactedCellsCount
operator|=
name|tempMajorCompactedCellsCount
expr_stmt|;
name|flushedCellsSize
operator|=
name|tempFlushedCellsSize
expr_stmt|;
name|compactedCellsSize
operator|=
name|tempCompactedCellsSize
expr_stmt|;
name|majorCompactedCellsSize
operator|=
name|tempMajorCompactedCellsSize
expr_stmt|;
name|blockedRequestsCount
operator|=
name|tempBlockedRequestsCount
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|getHedgedReadOps
parameter_list|()
block|{
return|return
name|this
operator|.
name|dfsHedgedReadMetrics
operator|==
literal|null
condition|?
literal|0
else|:
name|this
operator|.
name|dfsHedgedReadMetrics
operator|.
name|getHedgedReadOps
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getHedgedReadWins
parameter_list|()
block|{
return|return
name|this
operator|.
name|dfsHedgedReadMetrics
operator|==
literal|null
condition|?
literal|0
else|:
name|this
operator|.
name|dfsHedgedReadMetrics
operator|.
name|getHedgedReadWins
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBlockedRequestsCount
parameter_list|()
block|{
return|return
name|blockedRequestsCount
return|;
block|}
block|}
end_class

end_unit

