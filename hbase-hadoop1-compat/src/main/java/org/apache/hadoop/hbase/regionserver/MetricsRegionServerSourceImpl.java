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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|metrics
operator|.
name|BaseSourceImpl
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
name|MetricHistogram
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
name|MetricsBuilder
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
name|MetricsRecordBuilder
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
name|lib
operator|.
name|MetricMutableCounterLong
import|;
end_import

begin_comment
comment|/**  * Hadoop1 implementation of MetricsRegionServerSource.  *  * Implements BaseSource through BaseSourceImpl, following the pattern  */
end_comment

begin_class
specifier|public
class|class
name|MetricsRegionServerSourceImpl
extends|extends
name|BaseSourceImpl
implements|implements
name|MetricsRegionServerSource
block|{
specifier|final
name|MetricsRegionServerWrapper
name|rsWrap
decl_stmt|;
specifier|private
specifier|final
name|MetricHistogram
name|putHisto
decl_stmt|;
specifier|private
specifier|final
name|MetricHistogram
name|deleteHisto
decl_stmt|;
specifier|private
specifier|final
name|MetricHistogram
name|getHisto
decl_stmt|;
specifier|private
specifier|final
name|MetricHistogram
name|incrementHisto
decl_stmt|;
specifier|private
specifier|final
name|MetricHistogram
name|appendHisto
decl_stmt|;
specifier|private
specifier|final
name|MetricMutableCounterLong
name|slowPut
decl_stmt|;
specifier|private
specifier|final
name|MetricMutableCounterLong
name|slowDelete
decl_stmt|;
specifier|private
specifier|final
name|MetricMutableCounterLong
name|slowGet
decl_stmt|;
specifier|private
specifier|final
name|MetricMutableCounterLong
name|slowIncrement
decl_stmt|;
specifier|private
specifier|final
name|MetricMutableCounterLong
name|slowAppend
decl_stmt|;
specifier|public
name|MetricsRegionServerSourceImpl
parameter_list|(
name|MetricsRegionServerWrapper
name|rsWrap
parameter_list|)
block|{
name|this
argument_list|(
name|METRICS_NAME
argument_list|,
name|METRICS_DESCRIPTION
argument_list|,
name|METRICS_CONTEXT
argument_list|,
name|METRICS_JMX_CONTEXT
argument_list|,
name|rsWrap
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MetricsRegionServerSourceImpl
parameter_list|(
name|String
name|metricsName
parameter_list|,
name|String
name|metricsDescription
parameter_list|,
name|String
name|metricsContext
parameter_list|,
name|String
name|metricsJmxContext
parameter_list|,
name|MetricsRegionServerWrapper
name|rsWrap
parameter_list|)
block|{
name|super
argument_list|(
name|metricsName
argument_list|,
name|metricsDescription
argument_list|,
name|metricsContext
argument_list|,
name|metricsJmxContext
argument_list|)
expr_stmt|;
name|this
operator|.
name|rsWrap
operator|=
name|rsWrap
expr_stmt|;
name|putHisto
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newHistogram
argument_list|(
name|MUTATE_KEY
argument_list|)
expr_stmt|;
name|slowPut
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|SLOW_MUTATE_KEY
argument_list|,
name|SLOW_MUTATE_DESC
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
name|deleteHisto
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newHistogram
argument_list|(
name|DELETE_KEY
argument_list|)
expr_stmt|;
name|slowDelete
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|SLOW_DELETE_KEY
argument_list|,
name|SLOW_DELETE_DESC
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
name|getHisto
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newHistogram
argument_list|(
name|GET_KEY
argument_list|)
expr_stmt|;
name|slowGet
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|SLOW_GET_KEY
argument_list|,
name|SLOW_GET_DESC
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
name|incrementHisto
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newHistogram
argument_list|(
name|INCREMENT_KEY
argument_list|)
expr_stmt|;
name|slowIncrement
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|SLOW_INCREMENT_KEY
argument_list|,
name|SLOW_INCREMENT_DESC
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
name|appendHisto
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newHistogram
argument_list|(
name|APPEND_KEY
argument_list|)
expr_stmt|;
name|slowAppend
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|SLOW_APPEND_KEY
argument_list|,
name|SLOW_APPEND_DESC
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updatePut
parameter_list|(
name|long
name|t
parameter_list|)
block|{
name|putHisto
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateDelete
parameter_list|(
name|long
name|t
parameter_list|)
block|{
name|deleteHisto
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateGet
parameter_list|(
name|long
name|t
parameter_list|)
block|{
name|getHisto
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateIncrement
parameter_list|(
name|long
name|t
parameter_list|)
block|{
name|incrementHisto
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateAppend
parameter_list|(
name|long
name|t
parameter_list|)
block|{
name|appendHisto
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrSlowPut
parameter_list|()
block|{
name|slowPut
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrSlowDelete
parameter_list|()
block|{
name|slowDelete
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrSlowGet
parameter_list|()
block|{
name|slowGet
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrSlowIncrement
parameter_list|()
block|{
name|slowIncrement
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrSlowAppend
parameter_list|()
block|{
name|slowAppend
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
comment|/**    * Yes this is a get function that doesn't return anything.  Thanks Hadoop for breaking all    * expectations of java programmers.  Instead of returning anything Hadoop metrics expects    * getMetrics to push the metrics into the metricsBuilder.    *    * @param metricsBuilder Builder to accept metrics    * @param all            push all or only changed?    */
annotation|@
name|Override
specifier|public
name|void
name|getMetrics
parameter_list|(
name|MetricsBuilder
name|metricsBuilder
parameter_list|,
name|boolean
name|all
parameter_list|)
block|{
name|MetricsRecordBuilder
name|mrb
init|=
name|metricsBuilder
operator|.
name|addRecord
argument_list|(
name|metricsName
argument_list|)
operator|.
name|setContext
argument_list|(
name|metricsContext
argument_list|)
decl_stmt|;
comment|// rsWrap can be null because this function is called inside of init.
if|if
condition|(
name|rsWrap
operator|!=
literal|null
condition|)
block|{
name|mrb
operator|.
name|addGauge
argument_list|(
name|REGION_COUNT
argument_list|,
name|REGION_COUNT_DESC
argument_list|,
name|rsWrap
operator|.
name|getNumOnlineRegions
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|STORE_COUNT
argument_list|,
name|STORE_COUNT_DESC
argument_list|,
name|rsWrap
operator|.
name|getNumStores
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|STOREFILE_COUNT
argument_list|,
name|STOREFILE_COUNT_DESC
argument_list|,
name|rsWrap
operator|.
name|getNumStoreFiles
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|MEMSTORE_SIZE
argument_list|,
name|MEMSTORE_SIZE_DESC
argument_list|,
name|rsWrap
operator|.
name|getMemstoreSize
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|STOREFILE_SIZE
argument_list|,
name|STOREFILE_SIZE_DESC
argument_list|,
name|rsWrap
operator|.
name|getStoreFileSize
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|RS_START_TIME_NAME
argument_list|,
name|RS_START_TIME_DESC
argument_list|,
name|rsWrap
operator|.
name|getStartCode
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|TOTAL_REQUEST_COUNT
argument_list|,
name|TOTAL_REQUEST_COUNT_DESC
argument_list|,
name|rsWrap
operator|.
name|getTotalRequestCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|READ_REQUEST_COUNT
argument_list|,
name|READ_REQUEST_COUNT_DESC
argument_list|,
name|rsWrap
operator|.
name|getReadRequestsCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|WRITE_REQUEST_COUNT
argument_list|,
name|WRITE_REQUEST_COUNT_DESC
argument_list|,
name|rsWrap
operator|.
name|getWriteRequestsCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|CHECK_MUTATE_FAILED_COUNT
argument_list|,
name|CHECK_MUTATE_FAILED_COUNT_DESC
argument_list|,
name|rsWrap
operator|.
name|getCheckAndMutateChecksFailed
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|CHECK_MUTATE_PASSED_COUNT
argument_list|,
name|CHECK_MUTATE_PASSED_COUNT_DESC
argument_list|,
name|rsWrap
operator|.
name|getCheckAndMutateChecksPassed
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|STOREFILE_INDEX_SIZE
argument_list|,
name|STOREFILE_INDEX_SIZE_DESC
argument_list|,
name|rsWrap
operator|.
name|getStoreFileIndexSize
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|STATIC_INDEX_SIZE
argument_list|,
name|STATIC_INDEX_SIZE_DESC
argument_list|,
name|rsWrap
operator|.
name|getTotalStaticIndexSize
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|STATIC_BLOOM_SIZE
argument_list|,
name|STATIC_BLOOM_SIZE_DESC
argument_list|,
name|rsWrap
operator|.
name|getTotalStaticBloomSize
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|NUMBER_OF_PUTS_WITHOUT_WAL
argument_list|,
name|NUMBER_OF_PUTS_WITHOUT_WAL_DESC
argument_list|,
name|rsWrap
operator|.
name|getNumPutsWithoutWAL
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|DATA_SIZE_WITHOUT_WAL
argument_list|,
name|DATA_SIZE_WITHOUT_WAL_DESC
argument_list|,
name|rsWrap
operator|.
name|getDataInMemoryWithoutWAL
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|PERCENT_FILES_LOCAL
argument_list|,
name|PERCENT_FILES_LOCAL_DESC
argument_list|,
name|rsWrap
operator|.
name|getPercentFileLocal
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|COMPACTION_QUEUE_LENGTH
argument_list|,
name|COMPACTION_QUEUE_LENGTH_DESC
argument_list|,
name|rsWrap
operator|.
name|getCompactionQueueSize
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|FLUSH_QUEUE_LENGTH
argument_list|,
name|FLUSH_QUEUE_LENGTH_DESC
argument_list|,
name|rsWrap
operator|.
name|getFlushQueueSize
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|BLOCK_CACHE_FREE_SIZE
argument_list|,
name|BLOCK_CACHE_FREE_DESC
argument_list|,
name|rsWrap
operator|.
name|getBlockCacheFreeSize
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|BLOCK_CACHE_COUNT
argument_list|,
name|BLOCK_CACHE_COUNT_DESC
argument_list|,
name|rsWrap
operator|.
name|getBlockCacheCount
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|BLOCK_CACHE_SIZE
argument_list|,
name|BLOCK_CACHE_SIZE_DESC
argument_list|,
name|rsWrap
operator|.
name|getBlockCacheSize
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|BLOCK_CACHE_HIT_COUNT
argument_list|,
name|BLOCK_CACHE_HIT_COUNT_DESC
argument_list|,
name|rsWrap
operator|.
name|getBlockCacheHitCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|BLOCK_CACHE_MISS_COUNT
argument_list|,
name|BLOCK_COUNT_MISS_COUNT_DESC
argument_list|,
name|rsWrap
operator|.
name|getBlockCacheMissCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|BLOCK_CACHE_EVICTION_COUNT
argument_list|,
name|BLOCK_CACHE_EVICTION_COUNT_DESC
argument_list|,
name|rsWrap
operator|.
name|getBlockCacheEvictedCount
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|BLOCK_CACHE_HIT_PERCENT
argument_list|,
name|BLOCK_CACHE_HIT_PERCENT_DESC
argument_list|,
name|rsWrap
operator|.
name|getBlockCacheHitPercent
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|BLOCK_CACHE_EXPRESS_HIT_PERCENT
argument_list|,
name|BLOCK_CACHE_EXPRESS_HIT_PERCENT_DESC
argument_list|,
name|rsWrap
operator|.
name|getBlockCacheHitCachingPercent
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|UPDATES_BLOCKED_TIME
argument_list|,
name|UPDATES_BLOCKED_DESC
argument_list|,
name|rsWrap
operator|.
name|getUpdatesBlockedTime
argument_list|()
argument_list|)
operator|.
name|tag
argument_list|(
name|ZOOKEEPER_QUORUM_NAME
argument_list|,
name|ZOOKEEPER_QUORUM_DESC
argument_list|,
name|rsWrap
operator|.
name|getZookeeperQuorum
argument_list|()
argument_list|)
operator|.
name|tag
argument_list|(
name|SERVER_NAME_NAME
argument_list|,
name|SERVER_NAME_DESC
argument_list|,
name|rsWrap
operator|.
name|getServerName
argument_list|()
argument_list|)
operator|.
name|tag
argument_list|(
name|CLUSTER_ID_NAME
argument_list|,
name|CLUSTER_ID_DESC
argument_list|,
name|rsWrap
operator|.
name|getClusterId
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|metricsRegistry
operator|.
name|snapshot
argument_list|(
name|mrb
argument_list|,
name|all
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

