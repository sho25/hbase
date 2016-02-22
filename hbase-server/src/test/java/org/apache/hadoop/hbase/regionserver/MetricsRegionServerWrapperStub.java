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
name|regionserver
package|;
end_package

begin_class
specifier|public
class|class
name|MetricsRegionServerWrapperStub
implements|implements
name|MetricsRegionServerWrapper
block|{
annotation|@
name|Override
specifier|public
name|String
name|getServerName
parameter_list|()
block|{
return|return
literal|"test"
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getClusterId
parameter_list|()
block|{
return|return
literal|"tClusterId"
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getZookeeperQuorum
parameter_list|()
block|{
return|return
literal|"zk"
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getCoprocessors
parameter_list|()
block|{
return|return
literal|"co-process"
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
literal|100
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumOnlineRegions
parameter_list|()
block|{
return|return
literal|101
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumStores
parameter_list|()
block|{
return|return
literal|2
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
literal|300
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
literal|1025
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
literal|1900
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMaxStoreFileAge
parameter_list|()
block|{
return|return
literal|2
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMinStoreFileAge
parameter_list|()
block|{
return|return
literal|2
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getAvgStoreFileAge
parameter_list|()
block|{
return|return
literal|2
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumReferenceFiles
parameter_list|()
block|{
return|return
literal|2
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
literal|0
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
literal|899
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
literal|997
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getFilteredReadRequestsCount
parameter_list|()
block|{
return|return
literal|1997
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
literal|707
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getRpcGetRequestsCount
parameter_list|()
block|{
return|return
literal|521
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getRpcScanRequestsCount
parameter_list|()
block|{
return|return
literal|101
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getRpcMultiRequestsCount
parameter_list|()
block|{
return|return
literal|486
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getRpcMutateRequestsCount
parameter_list|()
block|{
return|return
literal|606
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
literal|401
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
literal|405
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
literal|406
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
literal|407
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
literal|408
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
literal|409
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
literal|410
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|getPercentFileLocal
parameter_list|()
block|{
return|return
literal|99
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|getPercentFileLocalSecondaryRegions
parameter_list|()
block|{
return|return
literal|99
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getCompactionQueueSize
parameter_list|()
block|{
return|return
literal|411
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getSmallCompactionQueueSize
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getLargeCompactionQueueSize
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getFlushQueueSize
parameter_list|()
block|{
return|return
literal|412
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBlockCacheFreeSize
parameter_list|()
block|{
return|return
literal|413
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBlockCacheCount
parameter_list|()
block|{
return|return
literal|414
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBlockCacheSize
parameter_list|()
block|{
return|return
literal|415
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBlockCacheHitCount
parameter_list|()
block|{
return|return
literal|416
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBlockCachePrimaryHitCount
parameter_list|()
block|{
return|return
literal|422
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBlockCacheMissCount
parameter_list|()
block|{
return|return
literal|417
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBlockCachePrimaryMissCount
parameter_list|()
block|{
return|return
literal|421
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBlockCacheEvictedCount
parameter_list|()
block|{
return|return
literal|418
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBlockCachePrimaryEvictedCount
parameter_list|()
block|{
return|return
literal|420
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|getBlockCacheHitPercent
parameter_list|()
block|{
return|return
literal|98
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|getBlockCacheHitCachingPercent
parameter_list|()
block|{
return|return
literal|97
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBlockCacheFailedInsertions
parameter_list|()
block|{
return|return
literal|36
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getUpdatesBlockedTime
parameter_list|()
block|{
return|return
literal|419
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|forceRecompute
parameter_list|()
block|{
comment|//IGNORED.
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumWALFiles
parameter_list|()
block|{
return|return
literal|10
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
literal|1024000
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumWALSlowAppend
parameter_list|()
block|{
return|return
literal|0
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
literal|100000000
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
literal|10000000
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
literal|1000000
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
literal|1024000000
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
literal|102400000
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
literal|10240000
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getHedgedReadOps
parameter_list|()
block|{
return|return
literal|100
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
literal|10
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
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getSplitQueueSize
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCellsCountCompactedToMob
parameter_list|()
block|{
return|return
literal|20
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCellsCountCompactedFromMob
parameter_list|()
block|{
return|return
literal|10
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCellsSizeCompactedToMob
parameter_list|()
block|{
return|return
literal|200
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCellsSizeCompactedFromMob
parameter_list|()
block|{
return|return
literal|100
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMobFlushCount
parameter_list|()
block|{
return|return
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMobFlushedCellsCount
parameter_list|()
block|{
return|return
literal|10
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMobFlushedCellsSize
parameter_list|()
block|{
return|return
literal|1000
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMobScanCellsCount
parameter_list|()
block|{
return|return
literal|10
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMobScanCellsSize
parameter_list|()
block|{
return|return
literal|1000
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMobFileCacheAccessCount
parameter_list|()
block|{
return|return
literal|100
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMobFileCacheMissCount
parameter_list|()
block|{
return|return
literal|50
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMobFileCacheEvictedCount
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMobFileCacheCount
parameter_list|()
block|{
return|return
literal|100
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|getMobFileCacheHitPercent
parameter_list|()
block|{
return|return
literal|50
return|;
block|}
block|}
end_class

end_unit

