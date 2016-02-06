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
name|MetricsCollector
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
name|Interns
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
name|MutableCounterLong
import|;
end_import

begin_comment
comment|/**  * Hadoop2 implementation of MetricsRegionServerSource.  *  * Implements BaseSource through BaseSourceImpl, following the pattern  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
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
name|MetricHistogram
name|replayHisto
decl_stmt|;
specifier|private
specifier|final
name|MetricHistogram
name|scanNextHisto
decl_stmt|;
specifier|private
specifier|final
name|MutableCounterLong
name|slowPut
decl_stmt|;
specifier|private
specifier|final
name|MutableCounterLong
name|slowDelete
decl_stmt|;
specifier|private
specifier|final
name|MutableCounterLong
name|slowGet
decl_stmt|;
specifier|private
specifier|final
name|MutableCounterLong
name|slowIncrement
decl_stmt|;
specifier|private
specifier|final
name|MutableCounterLong
name|slowAppend
decl_stmt|;
specifier|private
specifier|final
name|MutableCounterLong
name|splitRequest
decl_stmt|;
specifier|private
specifier|final
name|MutableCounterLong
name|splitSuccess
decl_stmt|;
specifier|private
specifier|final
name|MetricHistogram
name|splitTimeHisto
decl_stmt|;
specifier|private
specifier|final
name|MetricHistogram
name|flushTimeHisto
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
name|newTimeHistogram
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
literal|0L
argument_list|)
expr_stmt|;
name|deleteHisto
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newTimeHistogram
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
literal|0L
argument_list|)
expr_stmt|;
name|getHisto
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newTimeHistogram
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
literal|0L
argument_list|)
expr_stmt|;
name|incrementHisto
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newTimeHistogram
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
literal|0L
argument_list|)
expr_stmt|;
name|appendHisto
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newTimeHistogram
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
literal|0L
argument_list|)
expr_stmt|;
name|replayHisto
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newTimeHistogram
argument_list|(
name|REPLAY_KEY
argument_list|)
expr_stmt|;
name|scanNextHisto
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newTimeHistogram
argument_list|(
name|SCAN_NEXT_KEY
argument_list|)
expr_stmt|;
name|splitTimeHisto
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newTimeHistogram
argument_list|(
name|SPLIT_KEY
argument_list|)
expr_stmt|;
name|flushTimeHisto
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newTimeHistogram
argument_list|(
name|FLUSH_KEY
argument_list|)
expr_stmt|;
name|splitRequest
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|SPLIT_REQUEST_KEY
argument_list|,
name|SPLIT_REQUEST_DESC
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|splitSuccess
operator|=
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|SPLIT_SUCCESS_KEY
argument_list|,
name|SPLIT_SUCCESS_DESC
argument_list|,
literal|0L
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
name|updateReplay
parameter_list|(
name|long
name|t
parameter_list|)
block|{
name|replayHisto
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
name|updateScannerNext
parameter_list|(
name|long
name|scanSize
parameter_list|)
block|{
name|scanNextHisto
operator|.
name|add
argument_list|(
name|scanSize
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
annotation|@
name|Override
specifier|public
name|void
name|incrSplitRequest
parameter_list|()
block|{
name|splitRequest
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrSplitSuccess
parameter_list|()
block|{
name|splitSuccess
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateSplitTime
parameter_list|(
name|long
name|t
parameter_list|)
block|{
name|splitTimeHisto
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
name|updateFlushTime
parameter_list|(
name|long
name|t
parameter_list|)
block|{
name|flushTimeHisto
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
comment|/**    * Yes this is a get function that doesn't return anything.  Thanks Hadoop for breaking all    * expectations of java programmers.  Instead of returning anything Hadoop metrics expects    * getMetrics to push the metrics into the collector.    *    * @param metricsCollector Collector to accept metrics    * @param all              push all or only changed?    */
annotation|@
name|Override
specifier|public
name|void
name|getMetrics
parameter_list|(
name|MetricsCollector
name|metricsCollector
parameter_list|,
name|boolean
name|all
parameter_list|)
block|{
name|MetricsRecordBuilder
name|mrb
init|=
name|metricsCollector
operator|.
name|addRecord
argument_list|(
name|metricsName
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
name|Interns
operator|.
name|info
argument_list|(
name|REGION_COUNT
argument_list|,
name|REGION_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getNumOnlineRegions
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|STORE_COUNT
argument_list|,
name|STORE_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getNumStores
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|WALFILE_COUNT
argument_list|,
name|WALFILE_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getNumWALFiles
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|WALFILE_SIZE
argument_list|,
name|WALFILE_SIZE_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getWALFileSize
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|STOREFILE_COUNT
argument_list|,
name|STOREFILE_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getNumStoreFiles
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|MEMSTORE_SIZE
argument_list|,
name|MEMSTORE_SIZE_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getMemstoreSize
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|STOREFILE_SIZE
argument_list|,
name|STOREFILE_SIZE_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getStoreFileSize
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|RS_START_TIME_NAME
argument_list|,
name|RS_START_TIME_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getStartCode
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|TOTAL_REQUEST_COUNT
argument_list|,
name|TOTAL_REQUEST_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getTotalRequestCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|READ_REQUEST_COUNT
argument_list|,
name|READ_REQUEST_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getReadRequestsCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|FILTERED_READ_REQUEST_COUNT
argument_list|,
name|FILTERED_READ_REQUEST_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getFilteredReadRequestsCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|WRITE_REQUEST_COUNT
argument_list|,
name|WRITE_REQUEST_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getWriteRequestsCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|RPC_GET_REQUEST_COUNT
argument_list|,
name|RPC_GET_REQUEST_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getRpcGetRequestsCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|RPC_SCAN_REQUEST_COUNT
argument_list|,
name|RPC_SCAN_REQUEST_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getRpcScanRequestsCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|RPC_MULTI_REQUEST_COUNT
argument_list|,
name|RPC_MULTI_REQUEST_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getRpcMultiRequestsCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|RPC_MUTATE_REQUEST_COUNT
argument_list|,
name|RPC_MUTATE_REQUEST_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getRpcMutateRequestsCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|CHECK_MUTATE_FAILED_COUNT
argument_list|,
name|CHECK_MUTATE_FAILED_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getCheckAndMutateChecksFailed
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|CHECK_MUTATE_PASSED_COUNT
argument_list|,
name|CHECK_MUTATE_PASSED_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getCheckAndMutateChecksPassed
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|STOREFILE_INDEX_SIZE
argument_list|,
name|STOREFILE_INDEX_SIZE_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getStoreFileIndexSize
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|STATIC_INDEX_SIZE
argument_list|,
name|STATIC_INDEX_SIZE_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getTotalStaticIndexSize
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|STATIC_BLOOM_SIZE
argument_list|,
name|STATIC_BLOOM_SIZE_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getTotalStaticBloomSize
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|NUMBER_OF_MUTATIONS_WITHOUT_WAL
argument_list|,
name|NUMBER_OF_MUTATIONS_WITHOUT_WAL_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getNumMutationsWithoutWAL
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|DATA_SIZE_WITHOUT_WAL
argument_list|,
name|DATA_SIZE_WITHOUT_WAL_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getDataInMemoryWithoutWAL
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|PERCENT_FILES_LOCAL
argument_list|,
name|PERCENT_FILES_LOCAL_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getPercentFileLocal
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|PERCENT_FILES_LOCAL_SECONDARY_REGIONS
argument_list|,
name|PERCENT_FILES_LOCAL_SECONDARY_REGIONS_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getPercentFileLocalSecondaryRegions
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|SPLIT_QUEUE_LENGTH
argument_list|,
name|SPLIT_QUEUE_LENGTH_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getSplitQueueSize
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|COMPACTION_QUEUE_LENGTH
argument_list|,
name|COMPACTION_QUEUE_LENGTH_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getCompactionQueueSize
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|FLUSH_QUEUE_LENGTH
argument_list|,
name|FLUSH_QUEUE_LENGTH_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getFlushQueueSize
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|BLOCK_CACHE_FREE_SIZE
argument_list|,
name|BLOCK_CACHE_FREE_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getBlockCacheFreeSize
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|BLOCK_CACHE_COUNT
argument_list|,
name|BLOCK_CACHE_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getBlockCacheCount
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|BLOCK_CACHE_SIZE
argument_list|,
name|BLOCK_CACHE_SIZE_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getBlockCacheSize
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|BLOCK_CACHE_HIT_COUNT
argument_list|,
name|BLOCK_CACHE_HIT_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getBlockCacheHitCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|BLOCK_CACHE_PRIMARY_HIT_COUNT
argument_list|,
name|BLOCK_CACHE_PRIMARY_HIT_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getBlockCachePrimaryHitCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|BLOCK_CACHE_MISS_COUNT
argument_list|,
name|BLOCK_COUNT_MISS_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getBlockCacheMissCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|BLOCK_CACHE_PRIMARY_MISS_COUNT
argument_list|,
name|BLOCK_COUNT_PRIMARY_MISS_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getBlockCachePrimaryMissCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|BLOCK_CACHE_EVICTION_COUNT
argument_list|,
name|BLOCK_CACHE_EVICTION_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getBlockCacheEvictedCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|BLOCK_CACHE_PRIMARY_EVICTION_COUNT
argument_list|,
name|BLOCK_CACHE_PRIMARY_EVICTION_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getBlockCachePrimaryEvictedCount
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|BLOCK_CACHE_HIT_PERCENT
argument_list|,
name|BLOCK_CACHE_HIT_PERCENT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getBlockCacheHitPercent
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|BLOCK_CACHE_EXPRESS_HIT_PERCENT
argument_list|,
name|BLOCK_CACHE_EXPRESS_HIT_PERCENT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getBlockCacheHitCachingPercent
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|BLOCK_CACHE_FAILED_INSERTION_COUNT
argument_list|,
name|BLOCK_CACHE_FAILED_INSERTION_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getBlockCacheFailedInsertions
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|UPDATES_BLOCKED_TIME
argument_list|,
name|UPDATES_BLOCKED_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getUpdatesBlockedTime
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|FLUSHED_CELLS
argument_list|,
name|FLUSHED_CELLS_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getFlushedCellsCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|COMPACTED_CELLS
argument_list|,
name|COMPACTED_CELLS_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getCompactedCellsCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|MAJOR_COMPACTED_CELLS
argument_list|,
name|MAJOR_COMPACTED_CELLS_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getMajorCompactedCellsCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|FLUSHED_CELLS_SIZE
argument_list|,
name|FLUSHED_CELLS_SIZE_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getFlushedCellsSize
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|COMPACTED_CELLS_SIZE
argument_list|,
name|COMPACTED_CELLS_SIZE_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getCompactedCellsSize
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|MAJOR_COMPACTED_CELLS_SIZE
argument_list|,
name|MAJOR_COMPACTED_CELLS_SIZE_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getMajorCompactedCellsSize
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|CELLS_COUNT_COMPACTED_FROM_MOB
argument_list|,
name|CELLS_COUNT_COMPACTED_FROM_MOB_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getCellsCountCompactedFromMob
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|CELLS_COUNT_COMPACTED_TO_MOB
argument_list|,
name|CELLS_COUNT_COMPACTED_TO_MOB_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getCellsCountCompactedToMob
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|CELLS_SIZE_COMPACTED_FROM_MOB
argument_list|,
name|CELLS_SIZE_COMPACTED_FROM_MOB_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getCellsSizeCompactedFromMob
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|CELLS_SIZE_COMPACTED_TO_MOB
argument_list|,
name|CELLS_SIZE_COMPACTED_TO_MOB_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getCellsSizeCompactedToMob
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|MOB_FLUSH_COUNT
argument_list|,
name|MOB_FLUSH_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getMobFlushCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|MOB_FLUSHED_CELLS_COUNT
argument_list|,
name|MOB_FLUSHED_CELLS_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getMobFlushedCellsCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|MOB_FLUSHED_CELLS_SIZE
argument_list|,
name|MOB_FLUSHED_CELLS_SIZE_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getMobFlushedCellsSize
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|MOB_SCAN_CELLS_COUNT
argument_list|,
name|MOB_SCAN_CELLS_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getMobScanCellsCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|MOB_SCAN_CELLS_SIZE
argument_list|,
name|MOB_SCAN_CELLS_SIZE_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getMobScanCellsSize
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|MOB_FILE_CACHE_COUNT
argument_list|,
name|MOB_FILE_CACHE_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getMobFileCacheCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|MOB_FILE_CACHE_ACCESS_COUNT
argument_list|,
name|MOB_FILE_CACHE_ACCESS_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getMobFileCacheAccessCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|MOB_FILE_CACHE_MISS_COUNT
argument_list|,
name|MOB_FILE_CACHE_MISS_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getMobFileCacheMissCount
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|MOB_FILE_CACHE_EVICTED_COUNT
argument_list|,
name|MOB_FILE_CACHE_EVICTED_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getMobFileCacheEvictedCount
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|MOB_FILE_CACHE_HIT_PERCENT
argument_list|,
name|MOB_FILE_CACHE_HIT_PERCENT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getMobFileCacheHitPercent
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|HEDGED_READS
argument_list|,
name|HEDGED_READS_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getHedgedReadOps
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|HEDGED_READ_WINS
argument_list|,
name|HEDGED_READ_WINS_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getHedgedReadWins
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|BLOCKED_REQUESTS_COUNT
argument_list|,
name|BLOCKED_REQUESTS_COUNT_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getBlockedRequestsCount
argument_list|()
argument_list|)
operator|.
name|tag
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|ZOOKEEPER_QUORUM_NAME
argument_list|,
name|ZOOKEEPER_QUORUM_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getZookeeperQuorum
argument_list|()
argument_list|)
operator|.
name|tag
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|SERVER_NAME_NAME
argument_list|,
name|SERVER_NAME_DESC
argument_list|)
argument_list|,
name|rsWrap
operator|.
name|getServerName
argument_list|()
argument_list|)
operator|.
name|tag
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|CLUSTER_ID_NAME
argument_list|,
name|CLUSTER_ID_DESC
argument_list|)
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

