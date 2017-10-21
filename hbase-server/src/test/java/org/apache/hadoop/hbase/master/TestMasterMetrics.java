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
name|master
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
name|CoordinatedStateManager
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
name|HBaseTestingUtility
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
name|MiniHBaseCluster
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
name|shaded
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClusterStatusProtos
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|RegionServerStatusProtos
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
name|MasterTests
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
name|MediumTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
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
name|MasterTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestMasterMetrics
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestMasterMetrics
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|MetricsAssertHelper
name|metricsHelper
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
specifier|static
name|MiniHBaseCluster
name|cluster
decl_stmt|;
specifier|private
specifier|static
name|HMaster
name|master
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
decl_stmt|;
specifier|public
specifier|static
class|class
name|MyMaster
extends|extends
name|HMaster
block|{
specifier|public
name|MyMaster
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|KeeperException
throws|,
name|InterruptedException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/*     @Override     protected void tryRegionServerReport(         long reportStartTime, long reportEndTime) {       // do nothing     } */
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|startCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting cluster"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
literal|null
argument_list|,
name|MyMaster
operator|.
name|class
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|cluster
operator|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for active/ready master"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitForActiveAndReadyMaster
argument_list|()
expr_stmt|;
name|master
operator|=
name|cluster
operator|.
name|getMaster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|after
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|TEST_UTIL
operator|!=
literal|null
condition|)
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testClusterRequests
parameter_list|()
throws|throws
name|Exception
block|{
comment|// sending fake request to master to see how metric value has changed
name|RegionServerStatusProtos
operator|.
name|RegionServerReportRequest
operator|.
name|Builder
name|request
init|=
name|RegionServerStatusProtos
operator|.
name|RegionServerReportRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|ServerName
name|serverName
init|=
name|cluster
operator|.
name|getMaster
argument_list|(
literal|0
argument_list|)
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|request
operator|.
name|setServer
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|serverName
argument_list|)
argument_list|)
expr_stmt|;
name|long
name|expectedRequestNumber
init|=
literal|10000
decl_stmt|;
name|MetricsMasterSource
name|masterSource
init|=
name|master
operator|.
name|getMasterMetrics
argument_list|()
operator|.
name|getMetricsSource
argument_list|()
decl_stmt|;
name|ClusterStatusProtos
operator|.
name|ServerLoad
name|sl
init|=
name|ClusterStatusProtos
operator|.
name|ServerLoad
operator|.
name|newBuilder
argument_list|()
operator|.
name|setTotalNumberOfRequests
argument_list|(
name|expectedRequestNumber
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|request
operator|.
name|setLoad
argument_list|(
name|sl
argument_list|)
expr_stmt|;
name|master
operator|.
name|getMasterRpcServices
argument_list|()
operator|.
name|regionServerReport
argument_list|(
literal|null
argument_list|,
name|request
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|boolean
name|tablesOnMaster
init|=
name|LoadBalancer
operator|.
name|isTablesOnMaster
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|tablesOnMaster
condition|)
block|{
name|metricsHelper
operator|.
name|assertCounter
argument_list|(
literal|"cluster_requests"
argument_list|,
name|expectedRequestNumber
argument_list|,
name|masterSource
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|metricsHelper
operator|.
name|assertCounterGt
argument_list|(
literal|"cluster_requests"
argument_list|,
name|expectedRequestNumber
argument_list|,
name|masterSource
argument_list|)
expr_stmt|;
block|}
name|expectedRequestNumber
operator|=
literal|15000
expr_stmt|;
name|sl
operator|=
name|ClusterStatusProtos
operator|.
name|ServerLoad
operator|.
name|newBuilder
argument_list|()
operator|.
name|setTotalNumberOfRequests
argument_list|(
name|expectedRequestNumber
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|request
operator|.
name|setLoad
argument_list|(
name|sl
argument_list|)
expr_stmt|;
name|master
operator|.
name|getMasterRpcServices
argument_list|()
operator|.
name|regionServerReport
argument_list|(
literal|null
argument_list|,
name|request
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|tablesOnMaster
condition|)
block|{
name|metricsHelper
operator|.
name|assertCounter
argument_list|(
literal|"cluster_requests"
argument_list|,
name|expectedRequestNumber
argument_list|,
name|masterSource
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|metricsHelper
operator|.
name|assertCounterGt
argument_list|(
literal|"cluster_requests"
argument_list|,
name|expectedRequestNumber
argument_list|,
name|masterSource
argument_list|)
expr_stmt|;
block|}
name|master
operator|.
name|stopMaster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDefaultMasterMetrics
parameter_list|()
throws|throws
name|Exception
block|{
name|MetricsMasterSource
name|masterSource
init|=
name|master
operator|.
name|getMasterMetrics
argument_list|()
operator|.
name|getMetricsSource
argument_list|()
decl_stmt|;
name|boolean
name|tablesOnMaster
init|=
name|LoadBalancer
operator|.
name|isTablesOnMaster
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|metricsHelper
operator|.
name|assertGauge
argument_list|(
literal|"numRegionServers"
argument_list|,
literal|1
operator|+
operator|(
name|tablesOnMaster
condition|?
literal|1
else|:
literal|0
operator|)
argument_list|,
name|masterSource
argument_list|)
expr_stmt|;
name|metricsHelper
operator|.
name|assertGauge
argument_list|(
literal|"averageLoad"
argument_list|,
literal|1
operator|+
operator|(
name|tablesOnMaster
condition|?
literal|0
else|:
literal|1
operator|)
argument_list|,
name|masterSource
argument_list|)
expr_stmt|;
name|metricsHelper
operator|.
name|assertGauge
argument_list|(
literal|"numDeadRegionServers"
argument_list|,
literal|0
argument_list|,
name|masterSource
argument_list|)
expr_stmt|;
name|metricsHelper
operator|.
name|assertGauge
argument_list|(
literal|"masterStartTime"
argument_list|,
name|master
operator|.
name|getMasterStartTime
argument_list|()
argument_list|,
name|masterSource
argument_list|)
expr_stmt|;
name|metricsHelper
operator|.
name|assertGauge
argument_list|(
literal|"masterActiveTime"
argument_list|,
name|master
operator|.
name|getMasterActiveTime
argument_list|()
argument_list|,
name|masterSource
argument_list|)
expr_stmt|;
name|metricsHelper
operator|.
name|assertTag
argument_list|(
literal|"isActiveMaster"
argument_list|,
literal|"true"
argument_list|,
name|masterSource
argument_list|)
expr_stmt|;
name|metricsHelper
operator|.
name|assertTag
argument_list|(
literal|"serverName"
argument_list|,
name|master
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|masterSource
argument_list|)
expr_stmt|;
name|metricsHelper
operator|.
name|assertTag
argument_list|(
literal|"clusterId"
argument_list|,
name|master
operator|.
name|getClusterId
argument_list|()
argument_list|,
name|masterSource
argument_list|)
expr_stmt|;
name|metricsHelper
operator|.
name|assertTag
argument_list|(
literal|"zookeeperQuorum"
argument_list|,
name|master
operator|.
name|getZooKeeper
argument_list|()
operator|.
name|getQuorum
argument_list|()
argument_list|,
name|masterSource
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDefaultMasterProcMetrics
parameter_list|()
throws|throws
name|Exception
block|{
name|MetricsMasterProcSource
name|masterSource
init|=
name|master
operator|.
name|getMasterMetrics
argument_list|()
operator|.
name|getMetricsProcSource
argument_list|()
decl_stmt|;
name|metricsHelper
operator|.
name|assertGauge
argument_list|(
literal|"numMasterWALs"
argument_list|,
name|master
operator|.
name|getNumWALFiles
argument_list|()
argument_list|,
name|masterSource
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

