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

begin_comment
comment|/**  * This is the interface that will expose RegionServer information to hadoop1/hadoop2  * implementations of the MetricsRegionServerSource.  */
end_comment

begin_interface
specifier|public
interface|interface
name|MetricsRegionServerWrapper
block|{
comment|/**    * Get ServerName    */
name|String
name|getServerName
parameter_list|()
function_decl|;
comment|/**    * Get the Cluster ID    *    * @return Cluster ID    */
name|String
name|getClusterId
parameter_list|()
function_decl|;
comment|/**    * Get the Zookeeper Quorum Info    *    * @return Zookeeper Quorum Info    */
name|String
name|getZookeeperQuorum
parameter_list|()
function_decl|;
comment|/**    * Get the co-processors    *    * @return Co-processors    */
name|String
name|getCoprocessors
parameter_list|()
function_decl|;
comment|/**    * Get HRegionServer start time    *    * @return Start time of RegionServer in milliseconds    */
name|long
name|getStartCode
parameter_list|()
function_decl|;
comment|/**    * The number of online regions    */
name|long
name|getNumOnlineRegions
parameter_list|()
function_decl|;
comment|/**    * Get the number of stores hosted on this region server.    */
name|long
name|getNumStores
parameter_list|()
function_decl|;
comment|/**    * Get the number of HLog files of this region server.    */
specifier|public
name|long
name|getNumHLogFiles
parameter_list|()
function_decl|;
comment|/**    * Get the size of HLog files of this region server.    */
specifier|public
name|long
name|getHLogFileSize
parameter_list|()
function_decl|;
comment|/**    * Get the number of store files hosted on this region server.    */
name|long
name|getNumStoreFiles
parameter_list|()
function_decl|;
comment|/**    * Get the size of the memstore on this region server.    */
name|long
name|getMemstoreSize
parameter_list|()
function_decl|;
comment|/**    * Get the total size of the store files this region server is serving from.    */
name|long
name|getStoreFileSize
parameter_list|()
function_decl|;
comment|/**    * Get the number of requests per second.    */
name|double
name|getRequestsPerSecond
parameter_list|()
function_decl|;
comment|/**    * Get the total number of requests per second.    */
name|long
name|getTotalRequestCount
parameter_list|()
function_decl|;
comment|/**    * Get the number of read requests to regions hosted on this region server.    */
name|long
name|getReadRequestsCount
parameter_list|()
function_decl|;
comment|/**    * Get the number of write requests to regions hosted on this region server.    */
name|long
name|getWriteRequestsCount
parameter_list|()
function_decl|;
comment|/**    * Get the number of CAS operations that failed.    */
name|long
name|getCheckAndMutateChecksFailed
parameter_list|()
function_decl|;
comment|/**    * Get the number of CAS operations that passed.    */
name|long
name|getCheckAndMutateChecksPassed
parameter_list|()
function_decl|;
comment|/**    * Get the Size (in bytes) of indexes in storefiles on disk.    */
name|long
name|getStoreFileIndexSize
parameter_list|()
function_decl|;
comment|/**    * Get the size (in bytes) of of the static indexes including the roots.    */
name|long
name|getTotalStaticIndexSize
parameter_list|()
function_decl|;
comment|/**    * Get the size (in bytes) of the static bloom filters.    */
name|long
name|getTotalStaticBloomSize
parameter_list|()
function_decl|;
comment|/**    * Number of mutations received with WAL explicitly turned off.    */
name|long
name|getNumMutationsWithoutWAL
parameter_list|()
function_decl|;
comment|/**    * Ammount of data in the memstore but not in the WAL because mutations explicitly had their    * WAL turned off.    */
name|long
name|getDataInMemoryWithoutWAL
parameter_list|()
function_decl|;
comment|/**    * Get the percent of HFiles' that are local.    */
name|int
name|getPercentFileLocal
parameter_list|()
function_decl|;
comment|/**    * Get the size of the compaction queue    */
name|int
name|getCompactionQueueSize
parameter_list|()
function_decl|;
name|int
name|getSmallCompactionQueueSize
parameter_list|()
function_decl|;
name|int
name|getLargeCompactionQueueSize
parameter_list|()
function_decl|;
comment|/**    * Get the size of the flush queue.    */
name|int
name|getFlushQueueSize
parameter_list|()
function_decl|;
comment|/**    * Get the size (in bytes) of the block cache that is free.    */
name|long
name|getBlockCacheFreeSize
parameter_list|()
function_decl|;
comment|/**    * Get the number of items in the block cache.    */
name|long
name|getBlockCacheCount
parameter_list|()
function_decl|;
comment|/**    * Get the total size (in bytes) of the block cache.    */
name|long
name|getBlockCacheSize
parameter_list|()
function_decl|;
comment|/**    * Get the count of hits to the block cache    */
name|long
name|getBlockCacheHitCount
parameter_list|()
function_decl|;
comment|/**    * Get the count of misses to the block cache.    */
name|long
name|getBlockCacheMissCount
parameter_list|()
function_decl|;
comment|/**    * Get the number of items evicted from the block cache.    */
name|long
name|getBlockCacheEvictedCount
parameter_list|()
function_decl|;
comment|/**    * Get the percent of all requests that hit the block cache.    */
name|double
name|getBlockCacheHitPercent
parameter_list|()
function_decl|;
comment|/**    * Get the percent of requests with the block cache turned on that hit the block cache.    */
name|int
name|getBlockCacheHitCachingPercent
parameter_list|()
function_decl|;
comment|/**    * Force a re-computation of the metrics.    */
name|void
name|forceRecompute
parameter_list|()
function_decl|;
comment|/**    * Get the amount of time that updates were blocked.    */
name|long
name|getUpdatesBlockedTime
parameter_list|()
function_decl|;
comment|/**    * Get the number of cells flushed to disk.    */
name|long
name|getFlushedCellsCount
parameter_list|()
function_decl|;
comment|/**    * Get the number of cells processed during minor compactions.    */
name|long
name|getCompactedCellsCount
parameter_list|()
function_decl|;
comment|/**    * Get the number of cells processed during major compactions.    */
name|long
name|getMajorCompactedCellsCount
parameter_list|()
function_decl|;
comment|/**    * Get the total amount of data flushed to disk, in bytes.    */
name|long
name|getFlushedCellsSize
parameter_list|()
function_decl|;
comment|/**    * Get the total amount of data processed during minor compactions, in bytes.    */
name|long
name|getCompactedCellsSize
parameter_list|()
function_decl|;
comment|/**    * Get the total amount of data processed during major compactions, in bytes.    */
name|long
name|getMajorCompactedCellsSize
parameter_list|()
function_decl|;
comment|/**    * @return Count of hedged read operations    */
specifier|public
name|long
name|getHedgedReadOps
parameter_list|()
function_decl|;
comment|/**    * @return Count of times a hedged read beat out the primary read.    */
specifier|public
name|long
name|getHedgedReadWins
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

