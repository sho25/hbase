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
name|ipc
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
name|*
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
name|NotServingRegionException
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
name|RegionTooBusyException
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
name|exceptions
operator|.
name|OutOfOrderScannerNextException
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
name|exceptions
operator|.
name|RegionMovedException
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
name|RPCTests
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RPCTests
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
name|TestRpcMetrics
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
name|TestRpcMetrics
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|testFactory
parameter_list|()
block|{
name|MetricsHBaseServer
name|masterMetrics
init|=
operator|new
name|MetricsHBaseServer
argument_list|(
literal|"HMaster"
argument_list|,
operator|new
name|MetricsHBaseServerWrapperStub
argument_list|()
argument_list|)
decl_stmt|;
name|MetricsHBaseServerSource
name|masterSource
init|=
name|masterMetrics
operator|.
name|getMetricsSource
argument_list|()
decl_stmt|;
name|MetricsHBaseServer
name|rsMetrics
init|=
operator|new
name|MetricsHBaseServer
argument_list|(
literal|"HRegionServer"
argument_list|,
operator|new
name|MetricsHBaseServerWrapperStub
argument_list|()
argument_list|)
decl_stmt|;
name|MetricsHBaseServerSource
name|rsSource
init|=
name|rsMetrics
operator|.
name|getMetricsSource
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"master"
argument_list|,
name|masterSource
operator|.
name|getMetricsContext
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"regionserver"
argument_list|,
name|rsSource
operator|.
name|getMetricsContext
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Master,sub=IPC"
argument_list|,
name|masterSource
operator|.
name|getMetricsJmxContext
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"RegionServer,sub=IPC"
argument_list|,
name|rsSource
operator|.
name|getMetricsJmxContext
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Master"
argument_list|,
name|masterSource
operator|.
name|getMetricsName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"RegionServer"
argument_list|,
name|rsSource
operator|.
name|getMetricsName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * This test makes sure that the numbers from a MetricsHBaseServerWrapper are correctly exported    * to hadoop metrics 2 system.    */
annotation|@
name|Test
specifier|public
name|void
name|testWrapperSource
parameter_list|()
block|{
name|MetricsHBaseServer
name|mrpc
init|=
operator|new
name|MetricsHBaseServer
argument_list|(
literal|"HMaster"
argument_list|,
operator|new
name|MetricsHBaseServerWrapperStub
argument_list|()
argument_list|)
decl_stmt|;
name|MetricsHBaseServerSource
name|serverSource
init|=
name|mrpc
operator|.
name|getMetricsSource
argument_list|()
decl_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"queueSize"
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
literal|"numCallsInGeneralQueue"
argument_list|,
literal|102
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"numCallsInReplicationQueue"
argument_list|,
literal|103
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"numCallsInPriorityQueue"
argument_list|,
literal|104
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"numOpenConnections"
argument_list|,
literal|105
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"numActiveHandler"
argument_list|,
literal|106
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"numActiveWriteHandler"
argument_list|,
literal|50
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"numActiveReadHandler"
argument_list|,
literal|50
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"numActiveScanHandler"
argument_list|,
literal|6
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"numCallsInWriteQueue"
argument_list|,
literal|50
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"numCallsInReadQueue"
argument_list|,
literal|50
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertGauge
argument_list|(
literal|"numCallsInScanQueue"
argument_list|,
literal|2
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test to make sure that all the actively called method on MetricsHBaseServer work.    */
annotation|@
name|Test
specifier|public
name|void
name|testSourceMethods
parameter_list|()
block|{
name|MetricsHBaseServer
name|mrpc
init|=
operator|new
name|MetricsHBaseServer
argument_list|(
literal|"HMaster"
argument_list|,
operator|new
name|MetricsHBaseServerWrapperStub
argument_list|()
argument_list|)
decl_stmt|;
name|MetricsHBaseServerSource
name|serverSource
init|=
name|mrpc
operator|.
name|getMetricsSource
argument_list|()
decl_stmt|;
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
name|mrpc
operator|.
name|authenticationFailure
argument_list|()
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
name|mrpc
operator|.
name|authenticationSuccess
argument_list|()
expr_stmt|;
block|}
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"authenticationFailures"
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
literal|"authenticationSuccesses"
argument_list|,
literal|13
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
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
name|mrpc
operator|.
name|authorizationSuccess
argument_list|()
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
name|mrpc
operator|.
name|authorizationFailure
argument_list|()
expr_stmt|;
block|}
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"authorizationSuccesses"
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
literal|"authorizationFailures"
argument_list|,
literal|15
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|mrpc
operator|.
name|dequeuedCall
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|mrpc
operator|.
name|processedCall
argument_list|(
literal|101
argument_list|)
expr_stmt|;
name|mrpc
operator|.
name|totalCall
argument_list|(
literal|102
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"queueCallTime_NumOps"
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
literal|"processCallTime_NumOps"
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
literal|"totalCallTime_NumOps"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|mrpc
operator|.
name|sentBytes
argument_list|(
literal|103
argument_list|)
expr_stmt|;
name|mrpc
operator|.
name|sentBytes
argument_list|(
literal|103
argument_list|)
expr_stmt|;
name|mrpc
operator|.
name|sentBytes
argument_list|(
literal|103
argument_list|)
expr_stmt|;
name|mrpc
operator|.
name|receivedBytes
argument_list|(
literal|104
argument_list|)
expr_stmt|;
name|mrpc
operator|.
name|receivedBytes
argument_list|(
literal|104
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"sentBytes"
argument_list|,
literal|309
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"receivedBytes"
argument_list|,
literal|208
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|mrpc
operator|.
name|receivedRequest
argument_list|(
literal|105
argument_list|)
expr_stmt|;
name|mrpc
operator|.
name|sentResponse
argument_list|(
literal|106
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"requestSize_NumOps"
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
literal|"responseSize_NumOps"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|mrpc
operator|.
name|exception
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"exceptions"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|mrpc
operator|.
name|exception
argument_list|(
operator|new
name|RegionMovedException
argument_list|(
name|ServerName
operator|.
name|parseServerName
argument_list|(
literal|"localhost:60020"
argument_list|)
argument_list|,
literal|100
argument_list|)
argument_list|)
expr_stmt|;
name|mrpc
operator|.
name|exception
argument_list|(
operator|new
name|RegionTooBusyException
argument_list|(
literal|"Some region"
argument_list|)
argument_list|)
expr_stmt|;
name|mrpc
operator|.
name|exception
argument_list|(
operator|new
name|OutOfOrderScannerNextException
argument_list|()
argument_list|)
expr_stmt|;
name|mrpc
operator|.
name|exception
argument_list|(
operator|new
name|NotServingRegionException
argument_list|()
argument_list|)
expr_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"exceptions.RegionMovedException"
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
literal|"exceptions.RegionTooBusyException"
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
literal|"exceptions.OutOfOrderScannerNextException"
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
literal|"exceptions.NotServingRegionException"
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
literal|"exceptions"
argument_list|,
literal|5
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testServerContextNameWithHostName
parameter_list|()
block|{
name|String
index|[]
name|masterServerNames
init|=
block|{
literal|"master/node-xyz/10.19.250.253:16020"
block|,
literal|"master/node-regionserver-xyz/10.19.250.253:16020"
block|,
literal|"HMaster/node-xyz/10.19.250.253:16020"
block|,
literal|"HMaster/node-regionserver-xyz/10.19.250.253:16020"
block|}
decl_stmt|;
name|String
index|[]
name|regionServerNames
init|=
block|{
literal|"regionserver/node-xyz/10.19.250.253:16020"
block|,
literal|"regionserver/node-master1-xyz/10.19.250.253:16020"
block|,
literal|"HRegionserver/node-xyz/10.19.250.253:16020"
block|,
literal|"HRegionserver/node-master1-xyz/10.19.250.253:16020"
block|}
decl_stmt|;
name|MetricsHBaseServerSource
name|masterSource
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|serverName
range|:
name|masterServerNames
control|)
block|{
name|masterSource
operator|=
operator|new
name|MetricsHBaseServer
argument_list|(
name|serverName
argument_list|,
operator|new
name|MetricsHBaseServerWrapperStub
argument_list|()
argument_list|)
operator|.
name|getMetricsSource
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"master"
argument_list|,
name|masterSource
operator|.
name|getMetricsContext
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Master,sub=IPC"
argument_list|,
name|masterSource
operator|.
name|getMetricsJmxContext
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Master"
argument_list|,
name|masterSource
operator|.
name|getMetricsName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|MetricsHBaseServerSource
name|rsSource
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|serverName
range|:
name|regionServerNames
control|)
block|{
name|rsSource
operator|=
operator|new
name|MetricsHBaseServer
argument_list|(
name|serverName
argument_list|,
operator|new
name|MetricsHBaseServerWrapperStub
argument_list|()
argument_list|)
operator|.
name|getMetricsSource
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"regionserver"
argument_list|,
name|rsSource
operator|.
name|getMetricsContext
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"RegionServer,sub=IPC"
argument_list|,
name|rsSource
operator|.
name|getMetricsJmxContext
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"RegionServer"
argument_list|,
name|rsSource
operator|.
name|getMetricsName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

