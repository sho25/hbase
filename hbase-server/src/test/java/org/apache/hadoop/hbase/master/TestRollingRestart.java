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
name|Set
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
name|HBaseConfiguration
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
name|HRegionInfo
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
name|LargeTests
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

begin_comment
comment|/**  * Tests the restarting of everything as done during rolling restarts.  */
end_comment

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
name|TestRollingRestart
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
name|TestRollingRestart
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|500000
argument_list|)
specifier|public
name|void
name|testBasicRollingRestart
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Start a cluster with 2 masters and 4 regionservers
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
literal|3
decl_stmt|;
specifier|final
name|int
name|NUM_REGIONS_TO_CREATE
init|=
literal|20
decl_stmt|;
name|int
name|expectedNumRS
init|=
literal|3
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
literal|"testRollingRestart"
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
literal|1
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
name|NavigableSet
argument_list|<
name|String
argument_list|>
name|regions
init|=
name|HBaseTestingUtility
operator|.
name|getAllOnlineRegions
argument_list|(
name|cluster
argument_list|)
decl_stmt|;
name|log
argument_list|(
literal|"Verifying only catalog and namespace regions are assigned\n"
argument_list|)
expr_stmt|;
if|if
condition|(
name|regions
operator|.
name|size
argument_list|()
operator|!=
literal|2
condition|)
block|{
for|for
control|(
name|String
name|oregion
range|:
name|regions
control|)
name|log
argument_list|(
literal|"Region still online: "
operator|+
name|oregion
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|regions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|log
argument_list|(
literal|"Enabling table\n"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|enableTable
argument_list|(
name|table
argument_list|)
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
name|HBaseTestingUtility
operator|.
name|getAllOnlineRegions
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
name|assertRegionsAssigned
argument_list|(
name|cluster
argument_list|,
name|regions
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedNumRS
argument_list|,
name|cluster
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Add a new regionserver
name|log
argument_list|(
literal|"Adding a fourth RS"
argument_list|)
expr_stmt|;
name|RegionServerThread
name|restarted
init|=
name|cluster
operator|.
name|startRegionServer
argument_list|()
decl_stmt|;
name|expectedNumRS
operator|++
expr_stmt|;
name|restarted
operator|.
name|waitForServerOnline
argument_list|()
expr_stmt|;
name|log
argument_list|(
literal|"Additional RS is online"
argument_list|)
expr_stmt|;
name|log
argument_list|(
literal|"Waiting for no more RIT"
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
literal|" assigned on cluster"
argument_list|)
expr_stmt|;
name|assertRegionsAssigned
argument_list|(
name|cluster
argument_list|,
name|regions
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedNumRS
argument_list|,
name|cluster
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Master Restarts
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
name|MasterThread
name|backupMaster
init|=
literal|null
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|masterThreads
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
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
name|backupMaster
operator|=
name|masterThreads
operator|.
name|get
argument_list|(
literal|1
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
name|backupMaster
operator|=
name|masterThreads
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
comment|// Bring down the backup master
name|log
argument_list|(
literal|"Stopping backup master\n\n"
argument_list|)
expr_stmt|;
name|backupMaster
operator|.
name|getMaster
argument_list|()
operator|.
name|stop
argument_list|(
literal|"Stop of backup during rolling restart"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|hbaseCluster
operator|.
name|waitOnMaster
argument_list|(
name|backupMaster
argument_list|)
expr_stmt|;
comment|// Bring down the primary master
name|log
argument_list|(
literal|"Stopping primary master\n\n"
argument_list|)
expr_stmt|;
name|activeMaster
operator|.
name|getMaster
argument_list|()
operator|.
name|stop
argument_list|(
literal|"Stop of active during rolling restart"
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
comment|// Start primary master
name|log
argument_list|(
literal|"Restarting primary master\n\n"
argument_list|)
expr_stmt|;
name|activeMaster
operator|=
name|cluster
operator|.
name|startMaster
argument_list|()
expr_stmt|;
name|cluster
operator|.
name|waitForActiveAndReadyMaster
argument_list|()
expr_stmt|;
name|master
operator|=
name|activeMaster
operator|.
name|getMaster
argument_list|()
expr_stmt|;
comment|// Start backup master
name|log
argument_list|(
literal|"Restarting backup master\n\n"
argument_list|)
expr_stmt|;
name|backupMaster
operator|=
name|cluster
operator|.
name|startMaster
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedNumRS
argument_list|,
name|cluster
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// RegionServer Restarts
comment|// Bring them down, one at a time, waiting between each to complete
name|List
argument_list|<
name|RegionServerThread
argument_list|>
name|regionServers
init|=
name|cluster
operator|.
name|getLiveRegionServerThreads
argument_list|()
decl_stmt|;
name|int
name|num
init|=
literal|1
decl_stmt|;
name|int
name|total
init|=
name|regionServers
operator|.
name|size
argument_list|()
decl_stmt|;
for|for
control|(
name|RegionServerThread
name|rst
range|:
name|regionServers
control|)
block|{
name|ServerName
name|serverName
init|=
name|rst
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|log
argument_list|(
literal|"Stopping region server "
operator|+
name|num
operator|+
literal|" of "
operator|+
name|total
operator|+
literal|" [ "
operator|+
name|serverName
operator|+
literal|"]"
argument_list|)
expr_stmt|;
name|rst
operator|.
name|getRegionServer
argument_list|()
operator|.
name|stop
argument_list|(
literal|"Stopping RS during rolling restart"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|hbaseCluster
operator|.
name|waitOnRegionServer
argument_list|(
name|rst
argument_list|)
expr_stmt|;
name|log
argument_list|(
literal|"Waiting for RS shutdown to be handled by master"
argument_list|)
expr_stmt|;
name|waitForRSShutdownToStartAndFinish
argument_list|(
name|activeMaster
argument_list|,
name|serverName
argument_list|)
expr_stmt|;
name|log
argument_list|(
literal|"RS shutdown done, waiting for no more RIT"
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
literal|" assigned on cluster"
argument_list|)
expr_stmt|;
name|assertRegionsAssigned
argument_list|(
name|cluster
argument_list|,
name|regions
argument_list|)
expr_stmt|;
name|expectedNumRS
operator|--
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedNumRS
argument_list|,
name|cluster
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|log
argument_list|(
literal|"Restarting region server "
operator|+
name|num
operator|+
literal|" of "
operator|+
name|total
argument_list|)
expr_stmt|;
name|restarted
operator|=
name|cluster
operator|.
name|startRegionServer
argument_list|()
expr_stmt|;
name|restarted
operator|.
name|waitForServerOnline
argument_list|()
expr_stmt|;
name|expectedNumRS
operator|++
expr_stmt|;
name|log
argument_list|(
literal|"Region server "
operator|+
name|num
operator|+
literal|" is back online"
argument_list|)
expr_stmt|;
name|log
argument_list|(
literal|"Waiting for no more RIT"
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
literal|" assigned on cluster"
argument_list|)
expr_stmt|;
name|assertRegionsAssigned
argument_list|(
name|cluster
argument_list|,
name|regions
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedNumRS
argument_list|,
name|cluster
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|num
operator|++
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|assertRegionsAssigned
argument_list|(
name|cluster
argument_list|,
name|regions
argument_list|)
expr_stmt|;
comment|// TODO: Bring random 3 of 4 RS down at the same time
name|ht
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Stop the cluster
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
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
name|void
name|waitForRSShutdownToStartAndFinish
parameter_list|(
name|MasterThread
name|activeMaster
parameter_list|,
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|ServerManager
name|sm
init|=
name|activeMaster
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerManager
argument_list|()
decl_stmt|;
comment|// First wait for it to be in dead list
while|while
condition|(
operator|!
name|sm
operator|.
name|getDeadServers
argument_list|()
operator|.
name|isDeadServer
argument_list|(
name|serverName
argument_list|)
condition|)
block|{
name|log
argument_list|(
literal|"Waiting for ["
operator|+
name|serverName
operator|+
literal|"] to be listed as dead in master"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|log
argument_list|(
literal|"Server ["
operator|+
name|serverName
operator|+
literal|"] marked as dead, waiting for it to "
operator|+
literal|"finish dead processing"
argument_list|)
expr_stmt|;
while|while
condition|(
name|sm
operator|.
name|areDeadServersInProgress
argument_list|()
condition|)
block|{
name|log
argument_list|(
literal|"Server ["
operator|+
name|serverName
operator|+
literal|"] still being processed, waiting"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|log
argument_list|(
literal|"Server ["
operator|+
name|serverName
operator|+
literal|"] done with server shutdown processing"
argument_list|)
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
name|int
name|getNumberOfOnlineRegions
parameter_list|(
name|MiniHBaseCluster
name|cluster
parameter_list|)
block|{
name|int
name|numFound
init|=
literal|0
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
name|numFound
operator|+=
name|rst
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getNumberOfOnlineRegions
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|MasterThread
name|mt
range|:
name|cluster
operator|.
name|getMasterThreads
argument_list|()
control|)
block|{
name|numFound
operator|+=
name|mt
operator|.
name|getMaster
argument_list|()
operator|.
name|getNumberOfOnlineRegions
argument_list|()
expr_stmt|;
block|}
return|return
name|numFound
return|;
block|}
specifier|private
name|void
name|assertRegionsAssigned
parameter_list|(
name|MiniHBaseCluster
name|cluster
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|expectedRegions
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|numFound
init|=
name|getNumberOfOnlineRegions
argument_list|(
name|cluster
argument_list|)
decl_stmt|;
if|if
condition|(
name|expectedRegions
operator|.
name|size
argument_list|()
operator|>
name|numFound
condition|)
block|{
name|log
argument_list|(
literal|"Expected to find "
operator|+
name|expectedRegions
operator|.
name|size
argument_list|()
operator|+
literal|" but only found"
operator|+
literal|" "
operator|+
name|numFound
argument_list|)
expr_stmt|;
name|NavigableSet
argument_list|<
name|String
argument_list|>
name|foundRegions
init|=
name|HBaseTestingUtility
operator|.
name|getAllOnlineRegions
argument_list|(
name|cluster
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|region
range|:
name|expectedRegions
control|)
block|{
if|if
condition|(
operator|!
name|foundRegions
operator|.
name|contains
argument_list|(
name|region
argument_list|)
condition|)
block|{
name|log
argument_list|(
literal|"Missing region: "
operator|+
name|region
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
name|expectedRegions
operator|.
name|size
argument_list|()
argument_list|,
name|numFound
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|expectedRegions
operator|.
name|size
argument_list|()
operator|<
name|numFound
condition|)
block|{
name|int
name|doubled
init|=
name|numFound
operator|-
name|expectedRegions
operator|.
name|size
argument_list|()
decl_stmt|;
name|log
argument_list|(
literal|"Expected to find "
operator|+
name|expectedRegions
operator|.
name|size
argument_list|()
operator|+
literal|" but found"
operator|+
literal|" "
operator|+
name|numFound
operator|+
literal|" ("
operator|+
name|doubled
operator|+
literal|" double assignments?)"
argument_list|)
expr_stmt|;
name|NavigableSet
argument_list|<
name|String
argument_list|>
name|doubleRegions
init|=
name|getDoubleAssignedRegions
argument_list|(
name|cluster
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|region
range|:
name|doubleRegions
control|)
block|{
name|log
argument_list|(
literal|"Region is double assigned: "
operator|+
name|region
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|expectedRegions
operator|.
name|size
argument_list|()
argument_list|,
name|numFound
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|log
argument_list|(
literal|"Success!  Found expected number of "
operator|+
name|numFound
operator|+
literal|" regions"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|NavigableSet
argument_list|<
name|String
argument_list|>
name|getDoubleAssignedRegions
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
name|NavigableSet
argument_list|<
name|String
argument_list|>
name|doubled
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
operator|.
name|getRSRpcServices
argument_list|()
argument_list|)
control|)
block|{
if|if
condition|(
operator|!
name|online
operator|.
name|add
argument_list|(
name|region
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
condition|)
block|{
name|doubled
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
block|}
return|return
name|doubled
return|;
block|}
block|}
end_class

end_unit

