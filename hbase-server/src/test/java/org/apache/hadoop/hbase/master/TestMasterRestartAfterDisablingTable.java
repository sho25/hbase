begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
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
name|assertTrue
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|client
operator|.
name|HBaseAdmin
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
name|client
operator|.
name|HTable
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
name|util
operator|.
name|Bytes
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
name|JVMClusterUtil
operator|.
name|MasterThread
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
name|JVMClusterUtil
operator|.
name|RegionServerThread
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
name|zookeeper
operator|.
name|ZKAssign
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
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
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestMasterRestartAfterDisablingTable
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
name|TestMasterRestartAfterDisablingTable
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testForCheckingIfEnableAndDisableWorksFineAfterSwitch
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|NUM_MASTERS
init|=
literal|2
decl_stmt|;
specifier|final
name|int
name|NUM_RS
init|=
literal|1
decl_stmt|;
specifier|final
name|int
name|NUM_REGIONS_TO_CREATE
init|=
literal|4
decl_stmt|;
comment|// Start the cluster
name|log
argument_list|(
literal|"Starting cluster"
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.assignment.timeoutmonitor.period"
argument_list|,
literal|2000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.assignment.timeoutmonitor.timeout"
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|NUM_MASTERS
argument_list|,
name|NUM_RS
argument_list|)
expr_stmt|;
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|log
argument_list|(
literal|"Waiting for active/ready master"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitForActiveAndReadyMaster
argument_list|()
expr_stmt|;
name|ZooKeeperWatcher
name|zkw
init|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|conf
argument_list|,
literal|"testmasterRestart"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|HMaster
name|master
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
comment|// Create a table with regions
name|byte
index|[]
name|table
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"tableRestart"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
name|log
argument_list|(
literal|"Creating table with "
operator|+
name|NUM_REGIONS_TO_CREATE
operator|+
literal|" regions"
argument_list|)
expr_stmt|;
name|HTable
name|ht
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|table
argument_list|,
name|family
argument_list|)
decl_stmt|;
name|int
name|numRegions
init|=
name|TEST_UTIL
operator|.
name|createMultiRegions
argument_list|(
name|conf
argument_list|,
name|ht
argument_list|,
name|family
argument_list|,
name|NUM_REGIONS_TO_CREATE
argument_list|)
decl_stmt|;
name|numRegions
operator|+=
literal|2
expr_stmt|;
comment|// catalogs
name|log
argument_list|(
literal|"Waiting for no more RIT\n"
argument_list|)
expr_stmt|;
name|blockUntilNoRIT
argument_list|(
name|zkw
argument_list|,
name|master
argument_list|)
expr_stmt|;
name|log
argument_list|(
literal|"Disabling table\n"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|NavigableSet
argument_list|<
name|String
argument_list|>
name|regions
init|=
name|getAllOnlineRegions
argument_list|(
name|cluster
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"The number of regions for the table tableRestart should be 0 and only"
operator|+
literal|"the catalog tables should be present."
argument_list|,
literal|2
argument_list|,
name|regions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|MasterThread
argument_list|>
name|masterThreads
init|=
name|cluster
operator|.
name|getMasterThreads
argument_list|()
decl_stmt|;
name|MasterThread
name|activeMaster
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|masterThreads
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getMaster
argument_list|()
operator|.
name|isActiveMaster
argument_list|()
condition|)
block|{
name|activeMaster
operator|=
name|masterThreads
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|activeMaster
operator|=
name|masterThreads
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|activeMaster
operator|.
name|getMaster
argument_list|()
operator|.
name|stop
argument_list|(
literal|"stopping the active master so that the backup can become active"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|hbaseCluster
operator|.
name|waitOnMaster
argument_list|(
name|activeMaster
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitForActiveAndReadyMaster
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
literal|"The table should not be in enabled state"
argument_list|,
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getZKTable
argument_list|()
operator|.
name|isDisablingOrDisabledTable
argument_list|(
literal|"tableRestart"
argument_list|)
argument_list|)
expr_stmt|;
name|log
argument_list|(
literal|"Enabling table\n"
argument_list|)
expr_stmt|;
comment|// Need a new Admin, the previous one is on the old master
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
name|log
argument_list|(
literal|"Waiting for no more RIT\n"
argument_list|)
expr_stmt|;
name|blockUntilNoRIT
argument_list|(
name|zkw
argument_list|,
name|master
argument_list|)
expr_stmt|;
name|log
argument_list|(
literal|"Verifying there are "
operator|+
name|numRegions
operator|+
literal|" assigned on cluster\n"
argument_list|)
expr_stmt|;
name|regions
operator|=
name|getAllOnlineRegions
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"The assigned regions were not onlined after master switch except for the catalog tables."
argument_list|,
literal|6
argument_list|,
name|regions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"The table should be in enabled state"
argument_list|,
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getZKTable
argument_list|()
operator|.
name|isEnabledTable
argument_list|(
literal|"tableRestart"
argument_list|)
argument_list|)
expr_stmt|;
name|ht
operator|.
name|close
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|log
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"\n\nTRR: "
operator|+
name|msg
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|blockUntilNoRIT
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|,
name|HMaster
name|master
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|InterruptedException
block|{
name|ZKAssign
operator|.
name|blockUntilNoRIT
argument_list|(
name|zkw
argument_list|)
expr_stmt|;
name|master
operator|.
name|assignmentManager
operator|.
name|waitUntilNoRegionsInTransition
argument_list|(
literal|60000
argument_list|)
expr_stmt|;
block|}
specifier|private
name|NavigableSet
argument_list|<
name|String
argument_list|>
name|getAllOnlineRegions
parameter_list|(
name|MiniHBaseCluster
name|cluster
parameter_list|)
throws|throws
name|IOException
block|{
name|NavigableSet
argument_list|<
name|String
argument_list|>
name|online
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|RegionServerThread
name|rst
range|:
name|cluster
operator|.
name|getLiveRegionServerThreads
argument_list|()
control|)
block|{
for|for
control|(
name|HRegionInfo
name|region
range|:
name|ProtobufUtil
operator|.
name|getOnlineRegions
argument_list|(
name|rst
operator|.
name|getRegionServer
argument_list|()
argument_list|)
control|)
block|{
name|online
operator|.
name|add
argument_list|(
name|region
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|online
return|;
block|}
block|}
end_class

end_unit

