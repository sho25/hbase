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
name|BaseSource
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
name|JvmPauseMonitorSource
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

begin_comment
comment|/**  * Interface for classes that expose metrics about the regionserver.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MetricsRegionServerSource
extends|extends
name|BaseSource
extends|,
name|JvmPauseMonitorSource
block|{
comment|/**    * The name of the metrics    */
name|String
name|METRICS_NAME
init|=
literal|"Server"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under.    */
name|String
name|METRICS_CONTEXT
init|=
literal|"regionserver"
decl_stmt|;
comment|/**    * Description    */
name|String
name|METRICS_DESCRIPTION
init|=
literal|"Metrics about HBase RegionServer"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under in jmx    */
name|String
name|METRICS_JMX_CONTEXT
init|=
literal|"RegionServer,sub="
operator|+
name|METRICS_NAME
decl_stmt|;
comment|/**    * Update the Put time histogram    *    * @param t time it took    */
name|void
name|updatePut
parameter_list|(
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the PutBatch time histogram if a batch contains a Put op    * @param t time it took    */
name|void
name|updatePutBatch
parameter_list|(
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the Delete time histogram    *    * @param t time it took    */
name|void
name|updateDelete
parameter_list|(
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the Delete time histogram if a batch contains a delete op    * @param t time it took    */
name|void
name|updateDeleteBatch
parameter_list|(
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update checkAndDelete histogram    * @param t time it took    */
name|void
name|updateCheckAndDelete
parameter_list|(
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update checkAndPut histogram    * @param t time it took    */
name|void
name|updateCheckAndPut
parameter_list|(
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the Get time histogram .    *    * @param t time it took    */
name|void
name|updateGet
parameter_list|(
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the Increment time histogram.    *    * @param t time it took    */
name|void
name|updateIncrement
parameter_list|(
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the Append time histogram.    *    * @param t time it took    */
name|void
name|updateAppend
parameter_list|(
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the Replay time histogram.    *    * @param t time it took    */
name|void
name|updateReplay
parameter_list|(
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the scan size.    *    * @param scanSize size of the scan    */
name|void
name|updateScanSize
parameter_list|(
name|long
name|scanSize
parameter_list|)
function_decl|;
comment|/**    * Update the scan time.    * */
name|void
name|updateScanTime
parameter_list|(
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Increment the number of slow Puts that have happened.    */
name|void
name|incrSlowPut
parameter_list|()
function_decl|;
comment|/**    * Increment the number of slow Deletes that have happened.    */
name|void
name|incrSlowDelete
parameter_list|()
function_decl|;
comment|/**    * Increment the number of slow Gets that have happened.    */
name|void
name|incrSlowGet
parameter_list|()
function_decl|;
comment|/**    * Increment the number of slow Increments that have happened.    */
name|void
name|incrSlowIncrement
parameter_list|()
function_decl|;
comment|/**    * Increment the number of slow Appends that have happened.    */
name|void
name|incrSlowAppend
parameter_list|()
function_decl|;
comment|/**    * Update the split transaction time histogram    * @param t time it took, in milliseconds    */
name|void
name|updateSplitTime
parameter_list|(
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Increment number of a requested splits    */
name|void
name|incrSplitRequest
parameter_list|()
function_decl|;
comment|/**    * Increment number of successful splits    */
name|void
name|incrSplitSuccess
parameter_list|()
function_decl|;
comment|/**    * Update the flush time histogram    * @param t time it took, in milliseconds    */
name|void
name|updateFlushTime
parameter_list|(
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the flush memstore size histogram    * @param bytes the number of bytes in the memstore    */
name|void
name|updateFlushMemStoreSize
parameter_list|(
name|long
name|bytes
parameter_list|)
function_decl|;
comment|/**    * Update the flush output file size histogram    * @param bytes the number of bytes in the output file    */
name|void
name|updateFlushOutputSize
parameter_list|(
name|long
name|bytes
parameter_list|)
function_decl|;
comment|/**    * Update the compaction time histogram, both major and minor    * @param isMajor whether compaction is a major compaction    * @param t time it took, in milliseconds    */
name|void
name|updateCompactionTime
parameter_list|(
name|boolean
name|isMajor
parameter_list|,
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the compaction input number of files histogram    * @param isMajor whether compaction is a major compaction    * @param c number of files    */
name|void
name|updateCompactionInputFileCount
parameter_list|(
name|boolean
name|isMajor
parameter_list|,
name|long
name|c
parameter_list|)
function_decl|;
comment|/**    * Update the compaction total input file size histogram    * @param isMajor whether compaction is a major compaction    * @param bytes the number of bytes of the compaction input file    */
name|void
name|updateCompactionInputSize
parameter_list|(
name|boolean
name|isMajor
parameter_list|,
name|long
name|bytes
parameter_list|)
function_decl|;
comment|/**    * Update the compaction output number of files histogram    * @param isMajor whether compaction is a major compaction    * @param c number of files    */
name|void
name|updateCompactionOutputFileCount
parameter_list|(
name|boolean
name|isMajor
parameter_list|,
name|long
name|c
parameter_list|)
function_decl|;
comment|/**    * Update the compaction total output file size    * @param isMajor whether compaction is a major compaction    * @param bytes the number of bytes of the compaction input file    */
name|void
name|updateCompactionOutputSize
parameter_list|(
name|boolean
name|isMajor
parameter_list|,
name|long
name|bytes
parameter_list|)
function_decl|;
comment|// Strings used for exporting to metrics system.
name|String
name|REGION_COUNT
init|=
literal|"regionCount"
decl_stmt|;
name|String
name|REGION_COUNT_DESC
init|=
literal|"Number of regions"
decl_stmt|;
name|String
name|STORE_COUNT
init|=
literal|"storeCount"
decl_stmt|;
name|String
name|STORE_COUNT_DESC
init|=
literal|"Number of Stores"
decl_stmt|;
name|String
name|WALFILE_COUNT
init|=
literal|"hlogFileCount"
decl_stmt|;
name|String
name|WALFILE_COUNT_DESC
init|=
literal|"Number of WAL Files"
decl_stmt|;
name|String
name|WALFILE_SIZE
init|=
literal|"hlogFileSize"
decl_stmt|;
name|String
name|WALFILE_SIZE_DESC
init|=
literal|"Size of all WAL Files"
decl_stmt|;
name|String
name|STOREFILE_COUNT
init|=
literal|"storeFileCount"
decl_stmt|;
name|String
name|STOREFILE_COUNT_DESC
init|=
literal|"Number of Store Files"
decl_stmt|;
name|String
name|STORE_REF_COUNT
init|=
literal|"storeRefCount"
decl_stmt|;
name|String
name|STORE_REF_COUNT_DESC
init|=
literal|"Store reference count"
decl_stmt|;
name|String
name|MAX_STORE_FILE_REF_COUNT
init|=
literal|"maxStoreFileRefCount"
decl_stmt|;
name|String
name|MEMSTORE_SIZE
init|=
literal|"memStoreSize"
decl_stmt|;
name|String
name|MEMSTORE_SIZE_DESC
init|=
literal|"Size of the memstore"
decl_stmt|;
name|String
name|STOREFILE_SIZE
init|=
literal|"storeFileSize"
decl_stmt|;
name|String
name|STOREFILE_SIZE_GROWTH_RATE
init|=
literal|"storeFileSizeGrowthRate"
decl_stmt|;
name|String
name|MAX_STORE_FILE_AGE
init|=
literal|"maxStoreFileAge"
decl_stmt|;
name|String
name|MIN_STORE_FILE_AGE
init|=
literal|"minStoreFileAge"
decl_stmt|;
name|String
name|AVG_STORE_FILE_AGE
init|=
literal|"avgStoreFileAge"
decl_stmt|;
name|String
name|NUM_REFERENCE_FILES
init|=
literal|"numReferenceFiles"
decl_stmt|;
name|String
name|MAX_STORE_FILE_AGE_DESC
init|=
literal|"Max age of store files hosted on this RegionServer"
decl_stmt|;
name|String
name|MIN_STORE_FILE_AGE_DESC
init|=
literal|"Min age of store files hosted on this RegionServer"
decl_stmt|;
name|String
name|AVG_STORE_FILE_AGE_DESC
init|=
literal|"Average age of store files hosted on this RegionServer"
decl_stmt|;
name|String
name|NUM_REFERENCE_FILES_DESC
init|=
literal|"Number of reference file on this RegionServer"
decl_stmt|;
name|String
name|STOREFILE_SIZE_DESC
init|=
literal|"Size of storefiles being served."
decl_stmt|;
name|String
name|STOREFILE_SIZE_GROWTH_RATE_DESC
init|=
literal|"Bytes per second by which the size of storefiles being served grows."
decl_stmt|;
name|String
name|TOTAL_REQUEST_COUNT
init|=
literal|"totalRequestCount"
decl_stmt|;
name|String
name|TOTAL_REQUEST_COUNT_DESC
init|=
literal|"Total number of requests this RegionServer has answered; increments the count once for "
operator|+
literal|"EVERY access whether an admin operation, a Scan, a Put or Put of 1M rows, or a Get "
operator|+
literal|"of a non-existent row"
decl_stmt|;
name|String
name|TOTAL_ROW_ACTION_REQUEST_COUNT
init|=
literal|"totalRowActionRequestCount"
decl_stmt|;
name|String
name|TOTAL_ROW_ACTION_REQUEST_COUNT_DESC
init|=
literal|"Total number of region requests this RegionServer has answered; counts by row-level "
operator|+
literal|"action at the RPC Server (Sums 'readRequestsCount' and 'writeRequestsCount'); counts"
operator|+
literal|"once per access whether a Put of 1M rows or a Get that returns 1M Results"
decl_stmt|;
name|String
name|READ_REQUEST_COUNT
init|=
literal|"readRequestCount"
decl_stmt|;
name|String
name|FILTERED_READ_REQUEST_COUNT
init|=
literal|"filteredReadRequestCount"
decl_stmt|;
name|String
name|FILTERED_READ_REQUEST_COUNT_DESC
init|=
literal|"Number of read requests this region server has answered."
decl_stmt|;
name|String
name|READ_REQUEST_COUNT_DESC
init|=
literal|"Number of read requests with non-empty Results that this RegionServer has answered."
decl_stmt|;
name|String
name|READ_REQUEST_RATE_PER_SECOND
init|=
literal|"readRequestRatePerSecond"
decl_stmt|;
name|String
name|READ_REQUEST_RATE_DESC
init|=
literal|"Rate of answering the read requests by this region server per second."
decl_stmt|;
name|String
name|CP_REQUEST_COUNT
init|=
literal|"cpRequestCount"
decl_stmt|;
name|String
name|CP_REQUEST_COUNT_DESC
init|=
literal|"Number of coprocessor service requests this region server has answered."
decl_stmt|;
name|String
name|WRITE_REQUEST_COUNT
init|=
literal|"writeRequestCount"
decl_stmt|;
name|String
name|WRITE_REQUEST_COUNT_DESC
init|=
literal|"Number of mutation requests this RegionServer has answered."
decl_stmt|;
name|String
name|WRITE_REQUEST_RATE_PER_SECOND
init|=
literal|"writeRequestRatePerSecond"
decl_stmt|;
name|String
name|WRITE_REQUEST_RATE_DESC
init|=
literal|"Rate of answering the mutation requests by this region server per second."
decl_stmt|;
name|String
name|CHECK_MUTATE_FAILED_COUNT
init|=
literal|"checkMutateFailedCount"
decl_stmt|;
name|String
name|CHECK_MUTATE_FAILED_COUNT_DESC
init|=
literal|"Number of Check and Mutate calls that failed the checks."
decl_stmt|;
name|String
name|CHECK_MUTATE_PASSED_COUNT
init|=
literal|"checkMutatePassedCount"
decl_stmt|;
name|String
name|CHECK_MUTATE_PASSED_COUNT_DESC
init|=
literal|"Number of Check and Mutate calls that passed the checks."
decl_stmt|;
name|String
name|STOREFILE_INDEX_SIZE
init|=
literal|"storeFileIndexSize"
decl_stmt|;
name|String
name|STOREFILE_INDEX_SIZE_DESC
init|=
literal|"Size of indexes in storefiles on disk."
decl_stmt|;
name|String
name|STATIC_INDEX_SIZE
init|=
literal|"staticIndexSize"
decl_stmt|;
name|String
name|STATIC_INDEX_SIZE_DESC
init|=
literal|"Uncompressed size of the static indexes."
decl_stmt|;
name|String
name|STATIC_BLOOM_SIZE
init|=
literal|"staticBloomSize"
decl_stmt|;
name|String
name|STATIC_BLOOM_SIZE_DESC
init|=
literal|"Uncompressed size of the static bloom filters."
decl_stmt|;
name|String
name|NUMBER_OF_MUTATIONS_WITHOUT_WAL
init|=
literal|"mutationsWithoutWALCount"
decl_stmt|;
name|String
name|NUMBER_OF_MUTATIONS_WITHOUT_WAL_DESC
init|=
literal|"Number of mutations that have been sent by clients with the write ahead logging turned off."
decl_stmt|;
name|String
name|DATA_SIZE_WITHOUT_WAL
init|=
literal|"mutationsWithoutWALSize"
decl_stmt|;
name|String
name|DATA_SIZE_WITHOUT_WAL_DESC
init|=
literal|"Size of data that has been sent by clients with the write ahead logging turned off."
decl_stmt|;
name|String
name|PERCENT_FILES_LOCAL
init|=
literal|"percentFilesLocal"
decl_stmt|;
name|String
name|PERCENT_FILES_LOCAL_DESC
init|=
literal|"The percent of HFiles that are stored on the local hdfs data node."
decl_stmt|;
name|String
name|PERCENT_FILES_LOCAL_SECONDARY_REGIONS
init|=
literal|"percentFilesLocalSecondaryRegions"
decl_stmt|;
name|String
name|PERCENT_FILES_LOCAL_SECONDARY_REGIONS_DESC
init|=
literal|"The percent of HFiles used by secondary regions that are stored on the local hdfs data node."
decl_stmt|;
name|String
name|SPLIT_QUEUE_LENGTH
init|=
literal|"splitQueueLength"
decl_stmt|;
name|String
name|SPLIT_QUEUE_LENGTH_DESC
init|=
literal|"Length of the queue for splits."
decl_stmt|;
name|String
name|COMPACTION_QUEUE_LENGTH
init|=
literal|"compactionQueueLength"
decl_stmt|;
name|String
name|LARGE_COMPACTION_QUEUE_LENGTH
init|=
literal|"largeCompactionQueueLength"
decl_stmt|;
name|String
name|SMALL_COMPACTION_QUEUE_LENGTH
init|=
literal|"smallCompactionQueueLength"
decl_stmt|;
name|String
name|COMPACTION_QUEUE_LENGTH_DESC
init|=
literal|"Length of the queue for compactions."
decl_stmt|;
name|String
name|LARGE_COMPACTION_QUEUE_LENGTH_DESC
init|=
literal|"Length of the queue for compactions with input size "
operator|+
literal|"larger than throttle threshold (2.5GB by default)"
decl_stmt|;
name|String
name|SMALL_COMPACTION_QUEUE_LENGTH_DESC
init|=
literal|"Length of the queue for compactions with input size "
operator|+
literal|"smaller than throttle threshold (2.5GB by default)"
decl_stmt|;
name|String
name|FLUSH_QUEUE_LENGTH
init|=
literal|"flushQueueLength"
decl_stmt|;
name|String
name|FLUSH_QUEUE_LENGTH_DESC
init|=
literal|"Length of the queue for region flushes"
decl_stmt|;
name|String
name|BLOCK_CACHE_FREE_SIZE
init|=
literal|"blockCacheFreeSize"
decl_stmt|;
name|String
name|BLOCK_CACHE_FREE_DESC
init|=
literal|"Size of the block cache that is not occupied."
decl_stmt|;
name|String
name|BLOCK_CACHE_COUNT
init|=
literal|"blockCacheCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_COUNT_DESC
init|=
literal|"Number of block in the block cache."
decl_stmt|;
name|String
name|BLOCK_CACHE_SIZE
init|=
literal|"blockCacheSize"
decl_stmt|;
name|String
name|BLOCK_CACHE_SIZE_DESC
init|=
literal|"Size of the block cache."
decl_stmt|;
name|String
name|BLOCK_CACHE_HIT_COUNT
init|=
literal|"blockCacheHitCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_HIT_COUNT_DESC
init|=
literal|"Count of the hit on the block cache."
decl_stmt|;
name|String
name|BLOCK_CACHE_PRIMARY_HIT_COUNT
init|=
literal|"blockCacheHitCountPrimary"
decl_stmt|;
name|String
name|BLOCK_CACHE_PRIMARY_HIT_COUNT_DESC
init|=
literal|"Count of hit on primary replica in the block cache."
decl_stmt|;
name|String
name|BLOCK_CACHE_MISS_COUNT
init|=
literal|"blockCacheMissCount"
decl_stmt|;
name|String
name|BLOCK_COUNT_MISS_COUNT_DESC
init|=
literal|"Number of requests for a block that missed the block cache."
decl_stmt|;
name|String
name|BLOCK_CACHE_PRIMARY_MISS_COUNT
init|=
literal|"blockCacheMissCountPrimary"
decl_stmt|;
name|String
name|BLOCK_COUNT_PRIMARY_MISS_COUNT_DESC
init|=
literal|"Number of requests for a block of primary replica that missed the block cache."
decl_stmt|;
name|String
name|BLOCK_CACHE_EVICTION_COUNT
init|=
literal|"blockCacheEvictionCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_EVICTION_COUNT_DESC
init|=
literal|"Count of the number of blocks evicted from the block cache."
operator|+
literal|"(Not including blocks evicted because of HFile removal)"
decl_stmt|;
name|String
name|BLOCK_CACHE_PRIMARY_EVICTION_COUNT
init|=
literal|"blockCacheEvictionCountPrimary"
decl_stmt|;
name|String
name|BLOCK_CACHE_PRIMARY_EVICTION_COUNT_DESC
init|=
literal|"Count of the number of blocks evicted from primary replica in the block cache."
decl_stmt|;
name|String
name|BLOCK_CACHE_HIT_PERCENT
init|=
literal|"blockCacheCountHitPercent"
decl_stmt|;
name|String
name|BLOCK_CACHE_HIT_PERCENT_DESC
init|=
literal|"Percent of block cache requests that are hits"
decl_stmt|;
name|String
name|BLOCK_CACHE_EXPRESS_HIT_PERCENT
init|=
literal|"blockCacheExpressHitPercent"
decl_stmt|;
name|String
name|BLOCK_CACHE_EXPRESS_HIT_PERCENT_DESC
init|=
literal|"The percent of the time that requests with the cache turned on hit the cache."
decl_stmt|;
name|String
name|BLOCK_CACHE_FAILED_INSERTION_COUNT
init|=
literal|"blockCacheFailedInsertionCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_FAILED_INSERTION_COUNT_DESC
init|=
literal|"Number of times that a block cache "
operator|+
literal|"insertion failed. Usually due to size restrictions."
decl_stmt|;
name|String
name|BLOCK_CACHE_DATA_MISS_COUNT
init|=
literal|"blockCacheDataMissCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_ENCODED_DATA_MISS_COUNT
init|=
literal|"blockCacheEncodedDataMissCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_LEAF_INDEX_MISS_COUNT
init|=
literal|"blockCacheLeafIndexMissCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_BLOOM_CHUNK_MISS_COUNT
init|=
literal|"blockCacheBloomChunkMissCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_META_MISS_COUNT
init|=
literal|"blockCacheMetaMissCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_ROOT_INDEX_MISS_COUNT
init|=
literal|"blockCacheRootIndexMissCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_INTERMEDIATE_INDEX_MISS_COUNT
init|=
literal|"blockCacheIntermediateIndexMissCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_FILE_INFO_MISS_COUNT
init|=
literal|"blockCacheFileInfoMissCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_GENERAL_BLOOM_META_MISS_COUNT
init|=
literal|"blockCacheGeneralBloomMetaMissCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_DELETE_FAMILY_BLOOM_MISS_COUNT
init|=
literal|"blockCacheDeleteFamilyBloomMissCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_TRAILER_MISS_COUNT
init|=
literal|"blockCacheTrailerMissCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_DATA_HIT_COUNT
init|=
literal|"blockCacheDataHitCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_ENCODED_DATA_HIT_COUNT
init|=
literal|"blockCacheEncodedDataHitCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_LEAF_INDEX_HIT_COUNT
init|=
literal|"blockCacheLeafIndexHitCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_BLOOM_CHUNK_HIT_COUNT
init|=
literal|"blockCacheBloomChunkHitCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_META_HIT_COUNT
init|=
literal|"blockCacheMetaHitCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_ROOT_INDEX_HIT_COUNT
init|=
literal|"blockCacheRootIndexHitCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_INTERMEDIATE_INDEX_HIT_COUNT
init|=
literal|"blockCacheIntermediateIndexHitCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_FILE_INFO_HIT_COUNT
init|=
literal|"blockCacheFileInfoHitCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_GENERAL_BLOOM_META_HIT_COUNT
init|=
literal|"blockCacheGeneralBloomMetaHitCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_DELETE_FAMILY_BLOOM_HIT_COUNT
init|=
literal|"blockCacheDeleteFamilyBloomHitCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_TRAILER_HIT_COUNT
init|=
literal|"blockCacheTrailerHitCount"
decl_stmt|;
name|String
name|L1_CACHE_HIT_COUNT
init|=
literal|"l1CacheHitCount"
decl_stmt|;
name|String
name|L1_CACHE_HIT_COUNT_DESC
init|=
literal|"L1 cache hit count."
decl_stmt|;
name|String
name|L1_CACHE_MISS_COUNT
init|=
literal|"l1CacheMissCount"
decl_stmt|;
name|String
name|L1_CACHE_MISS_COUNT_DESC
init|=
literal|"L1 cache miss count."
decl_stmt|;
name|String
name|L1_CACHE_HIT_RATIO
init|=
literal|"l1CacheHitRatio"
decl_stmt|;
name|String
name|L1_CACHE_HIT_RATIO_DESC
init|=
literal|"L1 cache hit ratio."
decl_stmt|;
name|String
name|L1_CACHE_MISS_RATIO
init|=
literal|"l1CacheMissRatio"
decl_stmt|;
name|String
name|L1_CACHE_MISS_RATIO_DESC
init|=
literal|"L1 cache miss ratio."
decl_stmt|;
name|String
name|L2_CACHE_HIT_COUNT
init|=
literal|"l2CacheHitCount"
decl_stmt|;
name|String
name|L2_CACHE_HIT_COUNT_DESC
init|=
literal|"L2 cache hit count."
decl_stmt|;
name|String
name|L2_CACHE_MISS_COUNT
init|=
literal|"l2CacheMissCount"
decl_stmt|;
name|String
name|L2_CACHE_MISS_COUNT_DESC
init|=
literal|"L2 cache miss count."
decl_stmt|;
name|String
name|L2_CACHE_HIT_RATIO
init|=
literal|"l2CacheHitRatio"
decl_stmt|;
name|String
name|L2_CACHE_HIT_RATIO_DESC
init|=
literal|"L2 cache hit ratio."
decl_stmt|;
name|String
name|L2_CACHE_MISS_RATIO
init|=
literal|"l2CacheMissRatio"
decl_stmt|;
name|String
name|L2_CACHE_MISS_RATIO_DESC
init|=
literal|"L2 cache miss ratio."
decl_stmt|;
name|String
name|RS_START_TIME_NAME
init|=
literal|"regionServerStartTime"
decl_stmt|;
name|String
name|ZOOKEEPER_QUORUM_NAME
init|=
literal|"zookeeperQuorum"
decl_stmt|;
name|String
name|SERVER_NAME_NAME
init|=
literal|"serverName"
decl_stmt|;
name|String
name|CLUSTER_ID_NAME
init|=
literal|"clusterId"
decl_stmt|;
name|String
name|RS_START_TIME_DESC
init|=
literal|"RegionServer Start Time"
decl_stmt|;
name|String
name|ZOOKEEPER_QUORUM_DESC
init|=
literal|"ZooKeeper Quorum"
decl_stmt|;
name|String
name|SERVER_NAME_DESC
init|=
literal|"Server Name"
decl_stmt|;
name|String
name|CLUSTER_ID_DESC
init|=
literal|"Cluster Id"
decl_stmt|;
name|String
name|UPDATES_BLOCKED_TIME
init|=
literal|"updatesBlockedTime"
decl_stmt|;
name|String
name|UPDATES_BLOCKED_DESC
init|=
literal|"Number of MS updates have been blocked so that the memstore can be flushed."
decl_stmt|;
name|String
name|DELETE_KEY
init|=
literal|"delete"
decl_stmt|;
name|String
name|CHECK_AND_DELETE_KEY
init|=
literal|"checkAndDelete"
decl_stmt|;
name|String
name|CHECK_AND_PUT_KEY
init|=
literal|"checkAndPut"
decl_stmt|;
name|String
name|DELETE_BATCH_KEY
init|=
literal|"deleteBatch"
decl_stmt|;
name|String
name|GET_SIZE_KEY
init|=
literal|"getSize"
decl_stmt|;
name|String
name|GET_KEY
init|=
literal|"get"
decl_stmt|;
name|String
name|INCREMENT_KEY
init|=
literal|"increment"
decl_stmt|;
name|String
name|PUT_KEY
init|=
literal|"put"
decl_stmt|;
name|String
name|PUT_BATCH_KEY
init|=
literal|"putBatch"
decl_stmt|;
name|String
name|APPEND_KEY
init|=
literal|"append"
decl_stmt|;
name|String
name|REPLAY_KEY
init|=
literal|"replay"
decl_stmt|;
name|String
name|SCAN_KEY
init|=
literal|"scan"
decl_stmt|;
name|String
name|SCAN_SIZE_KEY
init|=
literal|"scanSize"
decl_stmt|;
name|String
name|SCAN_TIME_KEY
init|=
literal|"scanTime"
decl_stmt|;
name|String
name|SLOW_PUT_KEY
init|=
literal|"slowPutCount"
decl_stmt|;
name|String
name|SLOW_GET_KEY
init|=
literal|"slowGetCount"
decl_stmt|;
name|String
name|SLOW_DELETE_KEY
init|=
literal|"slowDeleteCount"
decl_stmt|;
name|String
name|SLOW_INCREMENT_KEY
init|=
literal|"slowIncrementCount"
decl_stmt|;
name|String
name|SLOW_APPEND_KEY
init|=
literal|"slowAppendCount"
decl_stmt|;
name|String
name|SLOW_PUT_DESC
init|=
literal|"The number of batches containing puts that took over 1000ms to complete"
decl_stmt|;
name|String
name|SLOW_DELETE_DESC
init|=
literal|"The number of batches containing delete(s) that took over 1000ms to complete"
decl_stmt|;
name|String
name|SLOW_GET_DESC
init|=
literal|"The number of Gets that took over 1000ms to complete"
decl_stmt|;
name|String
name|SLOW_INCREMENT_DESC
init|=
literal|"The number of Increments that took over 1000ms to complete"
decl_stmt|;
name|String
name|SLOW_APPEND_DESC
init|=
literal|"The number of Appends that took over 1000ms to complete"
decl_stmt|;
name|String
name|FLUSHED_CELLS
init|=
literal|"flushedCellsCount"
decl_stmt|;
name|String
name|FLUSHED_CELLS_DESC
init|=
literal|"The number of cells flushed to disk"
decl_stmt|;
name|String
name|FLUSHED_CELLS_SIZE
init|=
literal|"flushedCellsSize"
decl_stmt|;
name|String
name|FLUSHED_CELLS_SIZE_DESC
init|=
literal|"The total amount of data flushed to disk, in bytes"
decl_stmt|;
name|String
name|COMPACTED_CELLS
init|=
literal|"compactedCellsCount"
decl_stmt|;
name|String
name|COMPACTED_CELLS_DESC
init|=
literal|"The number of cells processed during minor compactions"
decl_stmt|;
name|String
name|COMPACTED_CELLS_SIZE
init|=
literal|"compactedCellsSize"
decl_stmt|;
name|String
name|COMPACTED_CELLS_SIZE_DESC
init|=
literal|"The total amount of data processed during minor compactions, in bytes"
decl_stmt|;
name|String
name|MAJOR_COMPACTED_CELLS
init|=
literal|"majorCompactedCellsCount"
decl_stmt|;
name|String
name|MAJOR_COMPACTED_CELLS_DESC
init|=
literal|"The number of cells processed during major compactions"
decl_stmt|;
name|String
name|MAJOR_COMPACTED_CELLS_SIZE
init|=
literal|"majorCompactedCellsSize"
decl_stmt|;
name|String
name|MAJOR_COMPACTED_CELLS_SIZE_DESC
init|=
literal|"The total amount of data processed during major compactions, in bytes"
decl_stmt|;
name|String
name|CELLS_COUNT_COMPACTED_TO_MOB
init|=
literal|"cellsCountCompactedToMob"
decl_stmt|;
name|String
name|CELLS_COUNT_COMPACTED_TO_MOB_DESC
init|=
literal|"The number of cells moved to mob during compaction"
decl_stmt|;
name|String
name|CELLS_COUNT_COMPACTED_FROM_MOB
init|=
literal|"cellsCountCompactedFromMob"
decl_stmt|;
name|String
name|CELLS_COUNT_COMPACTED_FROM_MOB_DESC
init|=
literal|"The number of cells moved from mob during compaction"
decl_stmt|;
name|String
name|CELLS_SIZE_COMPACTED_TO_MOB
init|=
literal|"cellsSizeCompactedToMob"
decl_stmt|;
name|String
name|CELLS_SIZE_COMPACTED_TO_MOB_DESC
init|=
literal|"The total amount of cells move to mob during compaction, in bytes"
decl_stmt|;
name|String
name|CELLS_SIZE_COMPACTED_FROM_MOB
init|=
literal|"cellsSizeCompactedFromMob"
decl_stmt|;
name|String
name|CELLS_SIZE_COMPACTED_FROM_MOB_DESC
init|=
literal|"The total amount of cells move from mob during compaction, in bytes"
decl_stmt|;
name|String
name|MOB_FLUSH_COUNT
init|=
literal|"mobFlushCount"
decl_stmt|;
name|String
name|MOB_FLUSH_COUNT_DESC
init|=
literal|"The number of the flushes in mob-enabled stores"
decl_stmt|;
name|String
name|MOB_FLUSHED_CELLS_COUNT
init|=
literal|"mobFlushedCellsCount"
decl_stmt|;
name|String
name|MOB_FLUSHED_CELLS_COUNT_DESC
init|=
literal|"The number of mob cells flushed to disk"
decl_stmt|;
name|String
name|MOB_FLUSHED_CELLS_SIZE
init|=
literal|"mobFlushedCellsSize"
decl_stmt|;
name|String
name|MOB_FLUSHED_CELLS_SIZE_DESC
init|=
literal|"The total amount of mob cells flushed to disk, in bytes"
decl_stmt|;
name|String
name|MOB_SCAN_CELLS_COUNT
init|=
literal|"mobScanCellsCount"
decl_stmt|;
name|String
name|MOB_SCAN_CELLS_COUNT_DESC
init|=
literal|"The number of scanned mob cells"
decl_stmt|;
name|String
name|MOB_SCAN_CELLS_SIZE
init|=
literal|"mobScanCellsSize"
decl_stmt|;
name|String
name|MOB_SCAN_CELLS_SIZE_DESC
init|=
literal|"The total amount of scanned mob cells, in bytes"
decl_stmt|;
name|String
name|MOB_FILE_CACHE_ACCESS_COUNT
init|=
literal|"mobFileCacheAccessCount"
decl_stmt|;
name|String
name|MOB_FILE_CACHE_ACCESS_COUNT_DESC
init|=
literal|"The count of accesses to the mob file cache"
decl_stmt|;
name|String
name|MOB_FILE_CACHE_MISS_COUNT
init|=
literal|"mobFileCacheMissCount"
decl_stmt|;
name|String
name|MOB_FILE_CACHE_MISS_COUNT_DESC
init|=
literal|"The count of misses to the mob file cache"
decl_stmt|;
name|String
name|MOB_FILE_CACHE_HIT_PERCENT
init|=
literal|"mobFileCacheHitPercent"
decl_stmt|;
name|String
name|MOB_FILE_CACHE_HIT_PERCENT_DESC
init|=
literal|"The hit percent to the mob file cache"
decl_stmt|;
name|String
name|MOB_FILE_CACHE_EVICTED_COUNT
init|=
literal|"mobFileCacheEvictedCount"
decl_stmt|;
name|String
name|MOB_FILE_CACHE_EVICTED_COUNT_DESC
init|=
literal|"The number of items evicted from the mob file cache"
decl_stmt|;
name|String
name|MOB_FILE_CACHE_COUNT
init|=
literal|"mobFileCacheCount"
decl_stmt|;
name|String
name|MOB_FILE_CACHE_COUNT_DESC
init|=
literal|"The count of cached mob files"
decl_stmt|;
name|String
name|HEDGED_READS
init|=
literal|"hedgedReads"
decl_stmt|;
name|String
name|HEDGED_READS_DESC
init|=
literal|"The number of times we started a hedged read"
decl_stmt|;
name|String
name|HEDGED_READ_WINS
init|=
literal|"hedgedReadWins"
decl_stmt|;
name|String
name|HEDGED_READ_WINS_DESC
init|=
literal|"The number of times we started a hedged read and a hedged read won"
decl_stmt|;
name|String
name|BLOCKED_REQUESTS_COUNT
init|=
literal|"blockedRequestCount"
decl_stmt|;
name|String
name|BLOCKED_REQUESTS_COUNT_DESC
init|=
literal|"The number of blocked requests because of memstore size is "
operator|+
literal|"larger than blockingMemStoreSize"
decl_stmt|;
name|String
name|SPLIT_KEY
init|=
literal|"splitTime"
decl_stmt|;
name|String
name|SPLIT_REQUEST_KEY
init|=
literal|"splitRequestCount"
decl_stmt|;
name|String
name|SPLIT_REQUEST_DESC
init|=
literal|"Number of splits requested"
decl_stmt|;
name|String
name|SPLIT_SUCCESS_KEY
init|=
literal|"splitSuccessCount"
decl_stmt|;
name|String
name|SPLIT_SUCCESS_DESC
init|=
literal|"Number of successfully executed splits"
decl_stmt|;
name|String
name|FLUSH_TIME
init|=
literal|"flushTime"
decl_stmt|;
name|String
name|FLUSH_TIME_DESC
init|=
literal|"Histogram for the time in millis for memstore flush"
decl_stmt|;
name|String
name|FLUSH_MEMSTORE_SIZE
init|=
literal|"flushMemstoreSize"
decl_stmt|;
name|String
name|FLUSH_MEMSTORE_SIZE_DESC
init|=
literal|"Histogram for number of bytes in the memstore for a flush"
decl_stmt|;
name|String
name|FLUSH_OUTPUT_SIZE
init|=
literal|"flushOutputSize"
decl_stmt|;
name|String
name|FLUSH_OUTPUT_SIZE_DESC
init|=
literal|"Histogram for number of bytes in the resulting file for a flush"
decl_stmt|;
name|String
name|FLUSHED_OUTPUT_BYTES
init|=
literal|"flushedOutputBytes"
decl_stmt|;
name|String
name|FLUSHED_OUTPUT_BYTES_DESC
init|=
literal|"Total number of bytes written from flush"
decl_stmt|;
name|String
name|FLUSHED_MEMSTORE_BYTES
init|=
literal|"flushedMemstoreBytes"
decl_stmt|;
name|String
name|FLUSHED_MEMSTORE_BYTES_DESC
init|=
literal|"Total number of bytes of cells in memstore from flush"
decl_stmt|;
name|String
name|COMPACTION_TIME
init|=
literal|"compactionTime"
decl_stmt|;
name|String
name|COMPACTION_TIME_DESC
init|=
literal|"Histogram for the time in millis for compaction, both major and minor"
decl_stmt|;
name|String
name|COMPACTION_INPUT_FILE_COUNT
init|=
literal|"compactionInputFileCount"
decl_stmt|;
name|String
name|COMPACTION_INPUT_FILE_COUNT_DESC
init|=
literal|"Histogram for the compaction input number of files, both major and minor"
decl_stmt|;
name|String
name|COMPACTION_INPUT_SIZE
init|=
literal|"compactionInputSize"
decl_stmt|;
name|String
name|COMPACTION_INPUT_SIZE_DESC
init|=
literal|"Histogram for the compaction total input file sizes, both major and minor"
decl_stmt|;
name|String
name|COMPACTION_OUTPUT_FILE_COUNT
init|=
literal|"compactionOutputFileCount"
decl_stmt|;
name|String
name|COMPACTION_OUTPUT_FILE_COUNT_DESC
init|=
literal|"Histogram for the compaction output number of files, both major and minor"
decl_stmt|;
name|String
name|COMPACTION_OUTPUT_SIZE
init|=
literal|"compactionOutputSize"
decl_stmt|;
name|String
name|COMPACTION_OUTPUT_SIZE_DESC
init|=
literal|"Histogram for the compaction total output file sizes, both major and minor"
decl_stmt|;
name|String
name|COMPACTED_INPUT_BYTES
init|=
literal|"compactedInputBytes"
decl_stmt|;
name|String
name|COMPACTED_INPUT_BYTES_DESC
init|=
literal|"Total number of bytes that is read for compaction, both major and minor"
decl_stmt|;
name|String
name|COMPACTED_OUTPUT_BYTES
init|=
literal|"compactedOutputBytes"
decl_stmt|;
name|String
name|COMPACTED_OUTPUT_BYTES_DESC
init|=
literal|"Total number of bytes that is output from compaction, both major and minor"
decl_stmt|;
name|String
name|MAJOR_COMPACTION_TIME
init|=
literal|"majorCompactionTime"
decl_stmt|;
name|String
name|MAJOR_COMPACTION_TIME_DESC
init|=
literal|"Histogram for the time in millis for compaction, major only"
decl_stmt|;
name|String
name|MAJOR_COMPACTION_INPUT_FILE_COUNT
init|=
literal|"majorCompactionInputFileCount"
decl_stmt|;
name|String
name|MAJOR_COMPACTION_INPUT_FILE_COUNT_DESC
init|=
literal|"Histogram for the compaction input number of files, major only"
decl_stmt|;
name|String
name|MAJOR_COMPACTION_INPUT_SIZE
init|=
literal|"majorCompactionInputSize"
decl_stmt|;
name|String
name|MAJOR_COMPACTION_INPUT_SIZE_DESC
init|=
literal|"Histogram for the compaction total input file sizes, major only"
decl_stmt|;
name|String
name|MAJOR_COMPACTION_OUTPUT_FILE_COUNT
init|=
literal|"majorCompactionOutputFileCount"
decl_stmt|;
name|String
name|MAJOR_COMPACTION_OUTPUT_FILE_COUNT_DESC
init|=
literal|"Histogram for the compaction output number of files, major only"
decl_stmt|;
name|String
name|MAJOR_COMPACTION_OUTPUT_SIZE
init|=
literal|"majorCompactionOutputSize"
decl_stmt|;
name|String
name|MAJOR_COMPACTION_OUTPUT_SIZE_DESC
init|=
literal|"Histogram for the compaction total output file sizes, major only"
decl_stmt|;
name|String
name|MAJOR_COMPACTED_INPUT_BYTES
init|=
literal|"majorCompactedInputBytes"
decl_stmt|;
name|String
name|MAJOR_COMPACTED_INPUT_BYTES_DESC
init|=
literal|"Total number of bytes that is read for compaction, major only"
decl_stmt|;
name|String
name|MAJOR_COMPACTED_OUTPUT_BYTES
init|=
literal|"majorCompactedOutputBytes"
decl_stmt|;
name|String
name|MAJOR_COMPACTED_OUTPUT_BYTES_DESC
init|=
literal|"Total number of bytes that is output from compaction, major only"
decl_stmt|;
name|String
name|RPC_GET_REQUEST_COUNT
init|=
literal|"rpcGetRequestCount"
decl_stmt|;
name|String
name|RPC_GET_REQUEST_COUNT_DESC
init|=
literal|"Number of rpc get requests this RegionServer has answered."
decl_stmt|;
name|String
name|RPC_SCAN_REQUEST_COUNT
init|=
literal|"rpcScanRequestCount"
decl_stmt|;
name|String
name|RPC_SCAN_REQUEST_COUNT_DESC
init|=
literal|"Number of rpc scan requests this RegionServer has answered."
decl_stmt|;
name|String
name|RPC_MULTI_REQUEST_COUNT
init|=
literal|"rpcMultiRequestCount"
decl_stmt|;
name|String
name|RPC_MULTI_REQUEST_COUNT_DESC
init|=
literal|"Number of rpc multi requests this RegionServer has answered."
decl_stmt|;
name|String
name|RPC_MUTATE_REQUEST_COUNT
init|=
literal|"rpcMutateRequestCount"
decl_stmt|;
name|String
name|RPC_MUTATE_REQUEST_COUNT_DESC
init|=
literal|"Number of rpc mutation requests this RegionServer has answered."
decl_stmt|;
name|String
name|AVERAGE_REGION_SIZE
init|=
literal|"averageRegionSize"
decl_stmt|;
name|String
name|AVERAGE_REGION_SIZE_DESC
init|=
literal|"Average region size over the RegionServer including memstore and storefile sizes."
decl_stmt|;
comment|/** Metrics for {@link org.apache.hadoop.hbase.io.ByteBuffAllocator} **/
name|String
name|BYTE_BUFF_ALLOCATOR_HEAP_ALLOCATION_BYTES
init|=
literal|"ByteBuffAllocatorHeapAllocationBytes"
decl_stmt|;
name|String
name|BYTE_BUFF_ALLOCATOR_HEAP_ALLOCATION_BYTES_DESC
init|=
literal|"Bytes of heap allocation from ByteBuffAllocator"
decl_stmt|;
name|String
name|BYTE_BUFF_ALLOCATOR_POOL_ALLOCATION_BYTES
init|=
literal|"ByteBuffAllocatorPoolAllocationBytes"
decl_stmt|;
name|String
name|BYTE_BUFF_ALLOCATOR_POOL_ALLOCATION_BYTES_DESC
init|=
literal|"Bytes of pool allocation from ByteBuffAllocator"
decl_stmt|;
name|String
name|BYTE_BUFF_ALLOCATOR_HEAP_ALLOCATION_RATIO
init|=
literal|"ByteBuffAllocatorHeapAllocationRatio"
decl_stmt|;
name|String
name|BYTE_BUFF_ALLOCATOR_HEAP_ALLOCATION_RATIO_DESC
init|=
literal|"Ratio of heap allocation from ByteBuffAllocator, means heapAllocation/totalAllocation"
decl_stmt|;
name|String
name|BYTE_BUFF_ALLOCATOR_TOTAL_BUFFER_COUNT
init|=
literal|"ByteBuffAllocatorTotalBufferCount"
decl_stmt|;
name|String
name|BYTE_BUFF_ALLOCATOR_TOTAL_BUFFER_COUNT_DESC
init|=
literal|"Total buffer count in ByteBuffAllocator"
decl_stmt|;
name|String
name|BYTE_BUFF_ALLOCATOR_USED_BUFFER_COUNT
init|=
literal|"ByteBuffAllocatorUsedBufferCount"
decl_stmt|;
name|String
name|BYTE_BUFF_ALLOCATOR_USED_BUFFER_COUNT_DESC
init|=
literal|"Used buffer count in ByteBuffAllocator"
decl_stmt|;
block|}
end_interface

end_unit

