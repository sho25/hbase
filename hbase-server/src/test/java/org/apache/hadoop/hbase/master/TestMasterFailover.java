begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|assertNotNull
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
name|util
operator|.
name|List
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
name|hbase
operator|.
name|ClusterStatus
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
name|master
operator|.
name|RegionState
operator|.
name|State
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
name|HRegion
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
name|HRegionServer
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
name|Region
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
name|testclassification
operator|.
name|FlakeyTests
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
name|zookeeper
operator|.
name|MetaTableLocator
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
name|FlakeyTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestMasterFailover
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
name|TestMasterFailover
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// TODO: Next test to add is with testing permutations of the RIT or the RS
comment|//       killed are hosting ROOT and hbase:meta regions.
specifier|private
name|void
name|log
parameter_list|(
name|String
name|string
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"\n\n"
operator|+
name|string
operator|+
literal|" \n\n"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Simple test of master failover.    *<p>    * Starts with three masters.  Kills a backup master.  Then kills the active    * master.  Ensures the final master becomes active and we can still contact    * the cluster.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|240000
argument_list|)
specifier|public
name|void
name|testSimpleMasterFailover
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|NUM_MASTERS
init|=
literal|3
decl_stmt|;
specifier|final
name|int
name|NUM_RS
init|=
literal|3
decl_stmt|;
comment|// Start the cluster
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
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
comment|// get all the master threads
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
comment|// wait for each to come online
for|for
control|(
name|MasterThread
name|mt
range|:
name|masterThreads
control|)
block|{
name|assertTrue
argument_list|(
name|mt
operator|.
name|isAlive
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// verify only one is the active master and we have right number
name|int
name|numActive
init|=
literal|0
decl_stmt|;
name|int
name|activeIndex
init|=
operator|-
literal|1
decl_stmt|;
name|ServerName
name|activeName
init|=
literal|null
decl_stmt|;
name|HMaster
name|active
init|=
literal|null
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
name|masterThreads
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|masterThreads
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getMaster
argument_list|()
operator|.
name|isActiveMaster
argument_list|()
condition|)
block|{
name|numActive
operator|++
expr_stmt|;
name|activeIndex
operator|=
name|i
expr_stmt|;
name|active
operator|=
name|masterThreads
operator|.
name|get
argument_list|(
name|activeIndex
argument_list|)
operator|.
name|getMaster
argument_list|()
expr_stmt|;
name|activeName
operator|=
name|active
operator|.
name|getServerName
argument_list|()
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|numActive
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|NUM_MASTERS
argument_list|,
name|masterThreads
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Active master "
operator|+
name|activeName
argument_list|)
expr_stmt|;
comment|// Check that ClusterStatus reports the correct active and backup masters
name|assertNotNull
argument_list|(
name|active
argument_list|)
expr_stmt|;
name|ClusterStatus
name|status
init|=
name|active
operator|.
name|getClusterStatus
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|status
operator|.
name|getMaster
argument_list|()
operator|.
name|equals
argument_list|(
name|activeName
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|status
operator|.
name|getBackupMastersSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|status
operator|.
name|getBackupMasters
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// attempt to stop one of the inactive masters
name|int
name|backupIndex
init|=
operator|(
name|activeIndex
operator|==
literal|0
condition|?
literal|1
else|:
name|activeIndex
operator|-
literal|1
operator|)
decl_stmt|;
name|HMaster
name|master
init|=
name|cluster
operator|.
name|getMaster
argument_list|(
name|backupIndex
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"\n\nStopping a backup master: "
operator|+
name|master
operator|.
name|getServerName
argument_list|()
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|stopMaster
argument_list|(
name|backupIndex
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitOnMaster
argument_list|(
name|backupIndex
argument_list|)
expr_stmt|;
comment|// Verify still one active master and it's the same
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|masterThreads
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|masterThreads
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getMaster
argument_list|()
operator|.
name|isActiveMaster
argument_list|()
condition|)
block|{
name|assertTrue
argument_list|(
name|activeName
operator|.
name|equals
argument_list|(
name|masterThreads
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|activeIndex
operator|=
name|i
expr_stmt|;
name|active
operator|=
name|masterThreads
operator|.
name|get
argument_list|(
name|activeIndex
argument_list|)
operator|.
name|getMaster
argument_list|()
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|numActive
argument_list|)
expr_stmt|;
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
name|int
name|rsCount
init|=
name|masterThreads
operator|.
name|get
argument_list|(
name|activeIndex
argument_list|)
operator|.
name|getMaster
argument_list|()
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getServersSize
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Active master "
operator|+
name|active
operator|.
name|getServerName
argument_list|()
operator|+
literal|" managing "
operator|+
name|rsCount
operator|+
literal|" regions servers"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|rsCount
argument_list|)
expr_stmt|;
comment|// Check that ClusterStatus reports the correct active and backup masters
name|assertNotNull
argument_list|(
name|active
argument_list|)
expr_stmt|;
name|status
operator|=
name|active
operator|.
name|getClusterStatus
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|status
operator|.
name|getMaster
argument_list|()
operator|.
name|equals
argument_list|(
name|activeName
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|status
operator|.
name|getBackupMastersSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|status
operator|.
name|getBackupMasters
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// kill the active master
name|LOG
operator|.
name|debug
argument_list|(
literal|"\n\nStopping the active master "
operator|+
name|active
operator|.
name|getServerName
argument_list|()
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|stopMaster
argument_list|(
name|activeIndex
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitOnMaster
argument_list|(
name|activeIndex
argument_list|)
expr_stmt|;
comment|// wait for an active master to show up and be ready
name|assertTrue
argument_list|(
name|cluster
operator|.
name|waitForActiveAndReadyMaster
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"\n\nVerifying backup master is now active\n"
argument_list|)
expr_stmt|;
comment|// should only have one master now
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|masterThreads
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// and he should be active
name|active
operator|=
name|masterThreads
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getMaster
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|active
argument_list|)
expr_stmt|;
name|status
operator|=
name|active
operator|.
name|getClusterStatus
argument_list|()
expr_stmt|;
name|ServerName
name|mastername
init|=
name|status
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|mastername
operator|.
name|equals
argument_list|(
name|active
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|active
operator|.
name|isActiveMaster
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|status
operator|.
name|getBackupMastersSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|status
operator|.
name|getBackupMasters
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|rss
init|=
name|status
operator|.
name|getServersSize
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Active master "
operator|+
name|mastername
operator|.
name|getServerName
argument_list|()
operator|+
literal|" managing "
operator|+
name|rss
operator|+
literal|" region servers"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|rss
argument_list|)
expr_stmt|;
comment|// Stop the cluster
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test meta in transition when master failover    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|180000
argument_list|)
specifier|public
name|void
name|testMetaInTransitionWhenMasterFailover
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|NUM_MASTERS
init|=
literal|1
decl_stmt|;
specifier|final
name|int
name|NUM_RS
init|=
literal|1
decl_stmt|;
comment|// Start the cluster
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
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
literal|"Cluster started"
argument_list|)
expr_stmt|;
name|log
argument_list|(
literal|"Moving meta off the master"
argument_list|)
expr_stmt|;
name|HMaster
name|activeMaster
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|HRegionServer
name|rs
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ServerName
name|metaServerName
init|=
name|cluster
operator|.
name|getLiveRegionServerThreads
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|activeMaster
operator|.
name|move
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|metaServerName
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilNoRegionsInTransition
argument_list|(
literal|60000
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Meta should be assigned on expected regionserver"
argument_list|,
name|metaServerName
argument_list|,
name|activeMaster
operator|.
name|getMetaTableLocator
argument_list|()
operator|.
name|getMetaRegionLocation
argument_list|(
name|activeMaster
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Now kill master, meta should remain on rs, where we placed it before.
name|log
argument_list|(
literal|"Aborting master"
argument_list|)
expr_stmt|;
name|activeMaster
operator|.
name|abort
argument_list|(
literal|"test-kill"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitForMasterToStop
argument_list|(
name|activeMaster
operator|.
name|getServerName
argument_list|()
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
name|log
argument_list|(
literal|"Master has aborted"
argument_list|)
expr_stmt|;
comment|// meta should remain where it was
name|RegionState
name|metaState
init|=
name|MetaTableLocator
operator|.
name|getMetaRegionState
argument_list|(
name|rs
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"hbase:meta should be online on RS"
argument_list|,
name|metaState
operator|.
name|getServerName
argument_list|()
argument_list|,
name|rs
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"hbase:meta should be online on RS"
argument_list|,
name|metaState
operator|.
name|getState
argument_list|()
argument_list|,
name|State
operator|.
name|OPEN
argument_list|)
expr_stmt|;
comment|// Start up a new master
name|log
argument_list|(
literal|"Starting up a new master"
argument_list|)
expr_stmt|;
name|activeMaster
operator|=
name|cluster
operator|.
name|startMaster
argument_list|()
operator|.
name|getMaster
argument_list|()
expr_stmt|;
name|log
argument_list|(
literal|"Waiting for master to be ready"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitForActiveAndReadyMaster
argument_list|()
expr_stmt|;
name|log
argument_list|(
literal|"Master is ready"
argument_list|)
expr_stmt|;
comment|// ensure meta is still deployed on RS
name|metaState
operator|=
name|MetaTableLocator
operator|.
name|getMetaRegionState
argument_list|(
name|activeMaster
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"hbase:meta should be online on RS"
argument_list|,
name|metaState
operator|.
name|getServerName
argument_list|()
argument_list|,
name|rs
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"hbase:meta should be online on RS"
argument_list|,
name|metaState
operator|.
name|getState
argument_list|()
argument_list|,
name|State
operator|.
name|OPEN
argument_list|)
expr_stmt|;
comment|// Update meta state as OPENING, then kill master
comment|// that simulates, that RS successfully deployed, but
comment|// RPC was lost right before failure.
comment|// region server should expire (how it can be verified?)
name|MetaTableLocator
operator|.
name|setMetaLocation
argument_list|(
name|activeMaster
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|rs
operator|.
name|getServerName
argument_list|()
argument_list|,
name|State
operator|.
name|OPENING
argument_list|)
expr_stmt|;
name|Region
name|meta
init|=
name|rs
operator|.
name|getRegion
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|rs
operator|.
name|removeRegion
argument_list|(
name|meta
argument_list|,
literal|null
argument_list|)
expr_stmt|;
operator|(
operator|(
name|HRegion
operator|)
name|meta
operator|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|log
argument_list|(
literal|"Aborting master"
argument_list|)
expr_stmt|;
name|activeMaster
operator|.
name|abort
argument_list|(
literal|"test-kill"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitForMasterToStop
argument_list|(
name|activeMaster
operator|.
name|getServerName
argument_list|()
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
name|log
argument_list|(
literal|"Master has aborted"
argument_list|)
expr_stmt|;
comment|// Start up a new master
name|log
argument_list|(
literal|"Starting up a new master"
argument_list|)
expr_stmt|;
name|activeMaster
operator|=
name|cluster
operator|.
name|startMaster
argument_list|()
operator|.
name|getMaster
argument_list|()
expr_stmt|;
name|log
argument_list|(
literal|"Waiting for master to be ready"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitForActiveAndReadyMaster
argument_list|()
expr_stmt|;
name|log
argument_list|(
literal|"Master is ready"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilNoRegionsInTransition
argument_list|(
literal|60000
argument_list|)
expr_stmt|;
name|log
argument_list|(
literal|"Meta was assigned"
argument_list|)
expr_stmt|;
name|metaState
operator|=
name|MetaTableLocator
operator|.
name|getMetaRegionState
argument_list|(
name|activeMaster
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"hbase:meta should be online on RS"
argument_list|,
name|metaState
operator|.
name|getServerName
argument_list|()
argument_list|,
name|rs
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"hbase:meta should be online on RS"
argument_list|,
name|metaState
operator|.
name|getState
argument_list|()
argument_list|,
name|State
operator|.
name|OPEN
argument_list|)
expr_stmt|;
comment|// Update meta state as CLOSING, then kill master
comment|// that simulates, that RS successfully deployed, but
comment|// RPC was lost right before failure.
comment|// region server should expire (how it can be verified?)
name|MetaTableLocator
operator|.
name|setMetaLocation
argument_list|(
name|activeMaster
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|rs
operator|.
name|getServerName
argument_list|()
argument_list|,
name|State
operator|.
name|CLOSING
argument_list|)
expr_stmt|;
name|log
argument_list|(
literal|"Aborting master"
argument_list|)
expr_stmt|;
name|activeMaster
operator|.
name|abort
argument_list|(
literal|"test-kill"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitForMasterToStop
argument_list|(
name|activeMaster
operator|.
name|getServerName
argument_list|()
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
name|log
argument_list|(
literal|"Master has aborted"
argument_list|)
expr_stmt|;
name|rs
operator|.
name|getRSRpcServices
argument_list|()
operator|.
name|closeRegion
argument_list|(
literal|null
argument_list|,
name|ProtobufUtil
operator|.
name|buildCloseRegionRequest
argument_list|(
name|rs
operator|.
name|getServerName
argument_list|()
argument_list|,
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Start up a new master
name|log
argument_list|(
literal|"Starting up a new master"
argument_list|)
expr_stmt|;
name|activeMaster
operator|=
name|cluster
operator|.
name|startMaster
argument_list|()
operator|.
name|getMaster
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|activeMaster
argument_list|)
expr_stmt|;
name|log
argument_list|(
literal|"Waiting for master to be ready"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitForActiveAndReadyMaster
argument_list|()
expr_stmt|;
name|log
argument_list|(
literal|"Master is ready"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilNoRegionsInTransition
argument_list|(
literal|60000
argument_list|)
expr_stmt|;
name|log
argument_list|(
literal|"Meta was assigned"
argument_list|)
expr_stmt|;
comment|// Done, shutdown the cluster
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

