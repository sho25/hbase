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

begin_comment
comment|/**  * Interface for classes that expose metrics about the regionserver.  */
end_comment

begin_interface
specifier|public
interface|interface
name|MetricsRegionServerSource
extends|extends
name|BaseSource
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
comment|/**    * Update the Delete time histogram    *    * @param t time it took    */
name|void
name|updateDelete
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
name|STOREFILE_SIZE_DESC
init|=
literal|"Size of storefiles being served."
decl_stmt|;
name|String
name|TOTAL_REQUEST_COUNT
init|=
literal|"totalRequestCount"
decl_stmt|;
name|String
name|TOTAL_REQUEST_COUNT_DESC
init|=
literal|"Total number of requests this RegionServer has answered."
decl_stmt|;
name|String
name|READ_REQUEST_COUNT
init|=
literal|"readRequestCount"
decl_stmt|;
name|String
name|READ_REQUEST_COUNT_DESC
init|=
literal|"Number of read requests this region server has answered."
decl_stmt|;
name|String
name|WRITE_REQUEST_COUNT
init|=
literal|"writeRequestCount"
decl_stmt|;
name|String
name|WRITE_REQUEST_COUNT_DESC
init|=
literal|"Number of mutation requests this region server has answered."
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
name|BLOCK_CACHE_EVICTION_COUNT
init|=
literal|"blockCacheEvictionCount"
decl_stmt|;
name|String
name|BLOCK_CACHE_EVICTION_COUNT_DESC
init|=
literal|"Count of the number of blocks evicted from the block cache."
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
literal|"Zookeeper Quorum"
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
name|MUTATE_KEY
init|=
literal|"mutate"
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
name|SCAN_NEXT_KEY
init|=
literal|"scanNext"
decl_stmt|;
name|String
name|SLOW_MUTATE_KEY
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
name|SLOW_MUTATE_DESC
init|=
literal|"The number of Multis that took over 1000ms to complete"
decl_stmt|;
name|String
name|SLOW_DELETE_DESC
init|=
literal|"The number of Deletes that took over 1000ms to complete"
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
literal|"splitSuccessCounnt"
decl_stmt|;
name|String
name|SPLIT_SUCCESS_DESC
init|=
literal|"Number of successfully executed splits"
decl_stmt|;
name|String
name|FLUSH_KEY
init|=
literal|"flushTime"
decl_stmt|;
block|}
end_interface

end_unit

