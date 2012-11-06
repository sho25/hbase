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
name|regionserver
operator|.
name|MetricsRegionServer
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
name|MetricsRegionServerWrapperStub
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
name|MetricsRegionServerSource
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

begin_comment
comment|/**  * Unit test version of rs metrics tests.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestMetricsRegionServer
block|{
specifier|public
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
annotation|@
name|Test
specifier|public
name|void
name|testWrapperSource
parameter_list|()
block|{
name|MetricsRegionServer
name|rsm
init|=
operator|new
name|MetricsRegionServer
argument_list|(
operator|new
name|MetricsRegionServerWrapperStub
argument_list|()
argument_list|)
decl_stmt|;
name|MetricsRegionServerSource
name|serverSource
init|=
name|rsm
operator|.
name|getMetricsSource
argument_list|()
decl_stmt|;
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
literal|"putsWithoutWALCount"
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
literal|"putsWithoutWALSize"
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
literal|"blockCountHitPercent"
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
name|MetricsRegionServer
name|rsm
init|=
operator|new
name|MetricsRegionServer
argument_list|(
operator|new
name|MetricsRegionServerWrapperStub
argument_list|()
argument_list|)
decl_stmt|;
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
block|}
end_class

end_unit

