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
name|assertFalse
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
name|mockito
operator|.
name|Mockito
operator|.
name|when
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|executor
operator|.
name|EventHandler
operator|.
name|EventType
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
name|AssignmentManager
operator|.
name|RegionState
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
name|handler
operator|.
name|OpenedRegionHandler
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
name|MockServer
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
name|ZKTable
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
name|ZKUtil
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
name|apache
operator|.
name|zookeeper
operator|.
name|data
operator|.
name|Stat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestOpenedRegionHandler
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
name|TestOpenedRegionHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|HBaseTestingUtility
name|TEST_UTIL
decl_stmt|;
specifier|private
specifier|final
name|int
name|NUM_MASTERS
init|=
literal|1
decl_stmt|;
specifier|private
specifier|final
name|int
name|NUM_RS
init|=
literal|1
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|Configuration
name|resetConf
decl_stmt|;
specifier|private
name|ZooKeeperWatcher
name|zkw
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Stop the cluster
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|resetConf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOpenedRegionHandlerOnMasterRestart
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Start the cluster
name|log
argument_list|(
literal|"Starting cluster"
argument_list|)
expr_stmt|;
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
name|resetConf
operator|=
name|conf
expr_stmt|;
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
name|TEST_UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|NUM_MASTERS
argument_list|,
name|NUM_RS
argument_list|)
expr_stmt|;
name|String
name|tableName
init|=
literal|"testOpenedRegionHandlerOnMasterRestart"
decl_stmt|;
name|MiniHBaseCluster
name|cluster
init|=
name|createRegions
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|abortMaster
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
name|HRegionServer
name|regionServer
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|getRegionBeingServed
argument_list|(
name|cluster
argument_list|,
name|regionServer
argument_list|)
decl_stmt|;
comment|// forcefully move a region to OPENED state in zk
comment|// Create a ZKW to use in the test
name|zkw
operator|=
name|HBaseTestingUtility
operator|.
name|createAndForceNodeToOpenedState
argument_list|(
name|TEST_UTIL
argument_list|,
name|region
argument_list|,
name|regionServer
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Start up a new master
name|log
argument_list|(
literal|"Starting up a new master"
argument_list|)
expr_stmt|;
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
comment|// Failover should be completed, now wait for no RIT
name|log
argument_list|(
literal|"Waiting for no more RIT"
argument_list|)
expr_stmt|;
name|ZKAssign
operator|.
name|blockUntilNoRIT
argument_list|(
name|zkw
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testShouldNotCompeleteOpenedRegionSuccessfullyIfVersionMismatches
parameter_list|()
throws|throws
name|Exception
block|{
name|HRegion
name|region
init|=
literal|null
decl_stmt|;
try|try
block|{
name|int
name|testIndex
init|=
literal|0
decl_stmt|;
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
specifier|final
name|Server
name|server
init|=
operator|new
name|MockServer
argument_list|(
name|TEST_UTIL
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"testShouldNotCompeleteOpenedRegionSuccessfullyIfVersionMismatches"
argument_list|)
decl_stmt|;
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|testIndex
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|testIndex
operator|+
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|region
operator|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|hri
argument_list|,
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|htd
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|region
argument_list|)
expr_stmt|;
name|AssignmentManager
name|am
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|AssignmentManager
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|am
operator|.
name|isRegionInTransition
argument_list|(
name|hri
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|new
name|RegionState
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|RegionState
operator|.
name|State
operator|.
name|OPEN
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|server
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// create a node with OPENED state
name|zkw
operator|=
name|HBaseTestingUtility
operator|.
name|createAndForceNodeToOpenedState
argument_list|(
name|TEST_UTIL
argument_list|,
name|region
argument_list|,
name|server
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|am
operator|.
name|getZKTable
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|new
name|ZKTable
argument_list|(
name|zkw
argument_list|)
argument_list|)
expr_stmt|;
name|Stat
name|stat
init|=
operator|new
name|Stat
argument_list|()
decl_stmt|;
name|String
name|nodeName
init|=
name|ZKAssign
operator|.
name|getNodeName
argument_list|(
name|zkw
argument_list|,
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|getDataAndWatch
argument_list|(
name|zkw
argument_list|,
name|nodeName
argument_list|,
name|stat
argument_list|)
expr_stmt|;
comment|// use the version for the OpenedRegionHandler
name|OpenedRegionHandler
name|handler
init|=
operator|new
name|OpenedRegionHandler
argument_list|(
name|server
argument_list|,
name|am
argument_list|,
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|server
operator|.
name|getServerName
argument_list|()
argument_list|,
name|stat
operator|.
name|getVersion
argument_list|()
argument_list|)
decl_stmt|;
comment|// Once again overwrite the same znode so that the version changes.
name|ZKAssign
operator|.
name|transitionNode
argument_list|(
name|zkw
argument_list|,
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|server
operator|.
name|getServerName
argument_list|()
argument_list|,
name|EventType
operator|.
name|RS_ZK_REGION_OPENED
argument_list|,
name|EventType
operator|.
name|RS_ZK_REGION_OPENED
argument_list|,
name|stat
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
comment|// Should not invoke assignmentmanager.regionOnline. If it is
comment|// invoked as per current mocking it will throw null pointer exception.
name|boolean
name|expectedException
init|=
literal|false
decl_stmt|;
try|try
block|{
name|handler
operator|.
name|process
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|expectedException
operator|=
literal|true
expr_stmt|;
block|}
name|assertFalse
argument_list|(
literal|"The process method should not throw any exception."
argument_list|,
name|expectedException
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|znodes
init|=
name|ZKUtil
operator|.
name|listChildrenAndWatchForNewChildren
argument_list|(
name|zkw
argument_list|,
name|zkw
operator|.
name|assignmentZNode
argument_list|)
decl_stmt|;
name|String
name|regionName
init|=
name|znodes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"The region should not be opened successfully."
argument_list|,
name|regionName
argument_list|,
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
name|region
operator|.
name|getLog
argument_list|()
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|MiniHBaseCluster
name|createRegions
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|ZooKeeperConnectionException
throws|,
name|IOException
throws|,
name|KeeperException
block|{
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
name|zkw
operator|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|conf
argument_list|,
literal|"testOpenedRegionHandler"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Create a table with regions
name|byte
index|[]
name|table
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
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
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|table
argument_list|,
name|family
argument_list|)
expr_stmt|;
comment|//wait till the regions are online
name|log
argument_list|(
literal|"Waiting for no more RIT"
argument_list|)
expr_stmt|;
name|ZKAssign
operator|.
name|blockUntilNoRIT
argument_list|(
name|zkw
argument_list|)
expr_stmt|;
return|return
name|cluster
return|;
block|}
specifier|private
name|void
name|abortMaster
parameter_list|(
name|MiniHBaseCluster
name|cluster
parameter_list|)
block|{
comment|// Stop the master
name|log
argument_list|(
literal|"Aborting master"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|abortMaster
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitOnMaster
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|log
argument_list|(
literal|"Master has aborted"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HRegion
name|getRegionBeingServed
parameter_list|(
name|MiniHBaseCluster
name|cluster
parameter_list|,
name|HRegionServer
name|regionServer
parameter_list|)
block|{
name|Collection
argument_list|<
name|HRegion
argument_list|>
name|onlineRegionsLocalContext
init|=
name|regionServer
operator|.
name|getOnlineRegionsLocalContext
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|HRegion
argument_list|>
name|iterator
init|=
name|onlineRegionsLocalContext
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|HRegion
name|region
init|=
literal|null
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|region
operator|=
name|iterator
operator|.
name|next
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|isMetaTable
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
return|return
name|region
return|;
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
annotation|@
name|org
operator|.
name|junit
operator|.
name|Rule
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
name|cu
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
argument_list|()
decl_stmt|;
block|}
end_class

end_unit

