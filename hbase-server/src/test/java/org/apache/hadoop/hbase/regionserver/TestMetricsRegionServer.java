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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotNull
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
name|CompatibilityFactory
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
name|HBaseClassTestRule
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
name|test
operator|.
name|MetricsAssertHelper
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
name|testclassification
operator|.
name|RegionServerTests
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
name|testclassification
operator|.
name|SmallTests
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
name|JvmPauseMonitor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_comment
comment|/**  * Unit test version of rs metrics tests.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestMetricsRegionServer
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestMetricsRegionServer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|MetricsAssertHelper
name|HELPER
init|=
name|CompatibilityFactory
operator|.
name|getInstance
argument_list|(
name|MetricsAssertHelper
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|MetricsRegionServerWrapperStub
name|wrapper
decl_stmt|;
specifier|private
name|MetricsRegionServer
name|rsm
decl_stmt|;
specifier|private
name|MetricsRegionServerSource
name|serverSource
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|classSetUp
parameter_list|()
block|{
name|HELPER
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|wrapper
operator|=
operator|new
name|MetricsRegionServerWrapperStub
argument_list|()
expr_stmt|;
name|rsm
operator|=
operator|new
name|MetricsRegionServer
argument_list|(
name|wrapper
argument_list|,
operator|new
name|Configuration
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|serverSource
operator|=
name|rsm
operator|.
name|getMetricsSource
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWrapperSource
parameter_list|()
block|{
name|HELPER
operator|.
name|assertTag
argument_list|(
literal|"serverName"
argument_list|,
literal|"test"
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertTag
argument_list|(
literal|"clusterId"
argument_list|,
literal|"tClusterId"
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertTag
argument_list|(
literal|"zookeeperQuorum"
argument_list|,
literal|"zk"
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"regionServerStartTime"
argument_list|,
literal|100
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"regionCount"
argument_list|,
literal|101
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"storeCount"
argument_list|,
literal|2
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"maxStoreFileAge"
argument_list|,
literal|2
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"minStoreFileAge"
argument_list|,
literal|2
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"avgStoreFileAge"
argument_list|,
literal|2
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"numReferenceFiles"
argument_list|,
literal|2
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"hlogFileCount"
argument_list|,
literal|10
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"hlogFileSize"
argument_list|,
literal|1024000
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"storeFileCount"
argument_list|,
literal|300
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"memstoreSize"
argument_list|,
literal|1025
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"storeFileSize"
argument_list|,
literal|1900
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"totalRequestCount"
argument_list|,
literal|899
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"totalRowActionRequestCount"
argument_list|,
name|HELPER
operator|.
name|getCounter
argument_list|(
literal|"readRequestCount"
argument_list|,
name|serverSource
argument_list|)
operator|+
name|HELPER
operator|.
name|getCounter
argument_list|(
literal|"writeRequestCount"
argument_list|,
name|serverSource
argument_list|)
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"readRequestCount"
argument_list|,
literal|997
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"cpRequestCount"
argument_list|,
literal|998
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"filteredReadRequestCount"
argument_list|,
literal|1997
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"writeRequestCount"
argument_list|,
literal|707
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"checkMutateFailedCount"
argument_list|,
literal|401
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"checkMutatePassedCount"
argument_list|,
literal|405
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"storeFileIndexSize"
argument_list|,
literal|406
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"staticIndexSize"
argument_list|,
literal|407
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"staticBloomSize"
argument_list|,
literal|408
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"mutationsWithoutWALCount"
argument_list|,
literal|409
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"mutationsWithoutWALSize"
argument_list|,
literal|410
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"percentFilesLocal"
argument_list|,
literal|99
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"percentFilesLocalSecondaryRegions"
argument_list|,
literal|99
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"compactionQueueLength"
argument_list|,
literal|411
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"flushQueueLength"
argument_list|,
literal|412
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"blockCacheFreeSize"
argument_list|,
literal|413
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"blockCacheCount"
argument_list|,
literal|414
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"blockCacheSize"
argument_list|,
literal|415
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"blockCacheHitCount"
argument_list|,
literal|416
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"blockCacheMissCount"
argument_list|,
literal|417
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"blockCacheEvictionCount"
argument_list|,
literal|418
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"blockCacheCountHitPercent"
argument_list|,
literal|98
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"blockCacheExpressHitPercent"
argument_list|,
literal|97
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"blockCacheFailedInsertionCount"
argument_list|,
literal|36
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"l1CacheHitCount"
argument_list|,
literal|200
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"l1CacheMissCount"
argument_list|,
literal|100
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"l1CacheHitRatio"
argument_list|,
literal|80
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"l1CacheMissRatio"
argument_list|,
literal|20
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"l2CacheHitCount"
argument_list|,
literal|800
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"l2CacheMissCount"
argument_list|,
literal|200
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"l2CacheHitRatio"
argument_list|,
literal|90
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"l2CacheMissRatio"
argument_list|,
literal|10
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"updatesBlockedTime"
argument_list|,
literal|419
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConstuctor
parameter_list|()
block|{
name|assertNotNull
argument_list|(
literal|"There should be a hadoop1/hadoop2 metrics source"
argument_list|,
name|rsm
operator|.
name|getMetricsSource
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"The RegionServerMetricsWrapper should be accessable"
argument_list|,
name|rsm
operator|.
name|getRegionServerWrapper
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSlowCount
parameter_list|()
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|12
condition|;
name|i
operator|++
control|)
block|{
name|rsm
operator|.
name|updateAppend
argument_list|(
literal|null
argument_list|,
literal|12
argument_list|)
expr_stmt|;
name|rsm
operator|.
name|updateAppend
argument_list|(
literal|null
argument_list|,
literal|1002
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|13
condition|;
name|i
operator|++
control|)
block|{
name|rsm
operator|.
name|updateDeleteBatch
argument_list|(
literal|null
argument_list|,
literal|13
argument_list|)
expr_stmt|;
name|rsm
operator|.
name|updateDeleteBatch
argument_list|(
literal|null
argument_list|,
literal|1003
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|14
condition|;
name|i
operator|++
control|)
block|{
name|rsm
operator|.
name|updateGet
argument_list|(
literal|null
argument_list|,
literal|14
argument_list|)
expr_stmt|;
name|rsm
operator|.
name|updateGet
argument_list|(
literal|null
argument_list|,
literal|1004
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|15
condition|;
name|i
operator|++
control|)
block|{
name|rsm
operator|.
name|updateIncrement
argument_list|(
literal|null
argument_list|,
literal|15
argument_list|)
expr_stmt|;
name|rsm
operator|.
name|updateIncrement
argument_list|(
literal|null
argument_list|,
literal|1005
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|16
condition|;
name|i
operator|++
control|)
block|{
name|rsm
operator|.
name|updatePutBatch
argument_list|(
literal|null
argument_list|,
literal|16
argument_list|)
expr_stmt|;
name|rsm
operator|.
name|updatePutBatch
argument_list|(
literal|null
argument_list|,
literal|1006
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|17
condition|;
name|i
operator|++
control|)
block|{
name|rsm
operator|.
name|updatePut
argument_list|(
literal|null
argument_list|,
literal|17
argument_list|)
expr_stmt|;
name|rsm
operator|.
name|updateDelete
argument_list|(
literal|null
argument_list|,
literal|17
argument_list|)
expr_stmt|;
name|rsm
operator|.
name|updateCheckAndDelete
argument_list|(
literal|17
argument_list|)
expr_stmt|;
name|rsm
operator|.
name|updateCheckAndPut
argument_list|(
literal|17
argument_list|)
expr_stmt|;
block|}
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"appendNumOps"
argument_list|,
literal|24
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"deleteBatchNumOps"
argument_list|,
literal|26
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"getNumOps"
argument_list|,
literal|28
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"incrementNumOps"
argument_list|,
literal|30
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"putBatchNumOps"
argument_list|,
literal|32
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"putNumOps"
argument_list|,
literal|17
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"deleteNumOps"
argument_list|,
literal|17
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"checkAndDeleteNumOps"
argument_list|,
literal|17
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"checkAndPutNumOps"
argument_list|,
literal|17
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"slowAppendCount"
argument_list|,
literal|12
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"slowDeleteCount"
argument_list|,
literal|13
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"slowGetCount"
argument_list|,
literal|14
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"slowIncrementCount"
argument_list|,
literal|15
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"slowPutCount"
argument_list|,
literal|16
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
block|}
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
name|FLUSH_FILE_SIZE
init|=
literal|"flushFileSize"
decl_stmt|;
name|String
name|FLUSH_FILE_SIZE_DESC
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
annotation|@
name|Test
specifier|public
name|void
name|testFlush
parameter_list|()
block|{
name|rsm
operator|.
name|updateFlush
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"flushTime_num_ops"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"flushMemstoreSize_num_ops"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"flushOutputSize_num_ops"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"flushedMemstoreBytes"
argument_list|,
literal|2
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"flushedOutputBytes"
argument_list|,
literal|3
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|rsm
operator|.
name|updateFlush
argument_list|(
literal|10
argument_list|,
literal|20
argument_list|,
literal|30
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"flushTimeNumOps"
argument_list|,
literal|2
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"flushMemstoreSize_num_ops"
argument_list|,
literal|2
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"flushOutputSize_num_ops"
argument_list|,
literal|2
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"flushedMemstoreBytes"
argument_list|,
literal|22
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"flushedOutputBytes"
argument_list|,
literal|33
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompaction
parameter_list|()
block|{
name|rsm
operator|.
name|updateCompaction
argument_list|(
literal|false
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|,
literal|4
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"compactionTime_num_ops"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"compactionInputFileCount_num_ops"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"compactionInputSize_num_ops"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"compactionOutputFileCount_num_ops"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"compactedInputBytes"
argument_list|,
literal|4
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"compactedoutputBytes"
argument_list|,
literal|5
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|rsm
operator|.
name|updateCompaction
argument_list|(
literal|false
argument_list|,
literal|10
argument_list|,
literal|20
argument_list|,
literal|30
argument_list|,
literal|40
argument_list|,
literal|50
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"compactionTime_num_ops"
argument_list|,
literal|2
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"compactionInputFileCount_num_ops"
argument_list|,
literal|2
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"compactionInputSize_num_ops"
argument_list|,
literal|2
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"compactionOutputFileCount_num_ops"
argument_list|,
literal|2
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"compactedInputBytes"
argument_list|,
literal|44
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"compactedoutputBytes"
argument_list|,
literal|55
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
comment|// do major compaction
name|rsm
operator|.
name|updateCompaction
argument_list|(
literal|true
argument_list|,
literal|100
argument_list|,
literal|200
argument_list|,
literal|300
argument_list|,
literal|400
argument_list|,
literal|500
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"compactionTime_num_ops"
argument_list|,
literal|3
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"compactionInputFileCount_num_ops"
argument_list|,
literal|3
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"compactionInputSize_num_ops"
argument_list|,
literal|3
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"compactionOutputFileCount_num_ops"
argument_list|,
literal|3
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"compactedInputBytes"
argument_list|,
literal|444
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"compactedoutputBytes"
argument_list|,
literal|555
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"majorCompactionTime_num_ops"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"majorCompactionInputFileCount_num_ops"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"majorCompactionInputSize_num_ops"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"majorCompactionOutputFileCount_num_ops"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"majorCompactedInputBytes"
argument_list|,
literal|400
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"majorCompactedoutputBytes"
argument_list|,
literal|500
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPauseMonitor
parameter_list|()
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|JvmPauseMonitor
operator|.
name|INFO_THRESHOLD_KEY
argument_list|,
literal|1000L
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|JvmPauseMonitor
operator|.
name|WARN_THRESHOLD_KEY
argument_list|,
literal|10000L
argument_list|)
expr_stmt|;
name|JvmPauseMonitor
name|monitor
init|=
operator|new
name|JvmPauseMonitor
argument_list|(
name|conf
argument_list|,
name|serverSource
argument_list|)
decl_stmt|;
name|monitor
operator|.
name|updateMetrics
argument_list|(
literal|1500
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"pauseInfoThresholdExceeded"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"pauseWarnThresholdExceeded"
argument_list|,
literal|0
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"pauseTimeWithoutGc_num_ops"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"pauseTimeWithGc_num_ops"
argument_list|,
literal|0
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|monitor
operator|.
name|updateMetrics
argument_list|(
literal|15000
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"pauseInfoThresholdExceeded"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"pauseWarnThresholdExceeded"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"pauseTimeWithoutGc_num_ops"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"pauseTimeWithGc_num_ops"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

