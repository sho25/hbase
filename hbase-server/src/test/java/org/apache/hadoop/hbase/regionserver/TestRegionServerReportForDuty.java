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
name|CategoryBasedTimeout
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
name|HConstants
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
name|LocalHBaseCluster
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
name|MiniHBaseCluster
operator|.
name|MiniHBaseClusterRegionServer
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
name|LoadBalancer
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
name|ServerManager
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
name|Rule
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
name|junit
operator|.
name|rules
operator|.
name|TestRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|TestRegionServerReportForDuty
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestRegionServerReportForDuty
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
specifier|final
name|TestRule
name|timeout
init|=
name|CategoryBasedTimeout
operator|.
name|builder
argument_list|()
operator|.
name|withTimeout
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
operator|.
name|withLookingForStuckThread
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|SLEEP_INTERVAL
init|=
literal|500
decl_stmt|;
specifier|private
name|HBaseTestingUtility
name|testUtil
decl_stmt|;
specifier|private
name|LocalHBaseCluster
name|cluster
decl_stmt|;
specifier|private
name|RegionServerThread
name|rs
decl_stmt|;
specifier|private
name|RegionServerThread
name|rs2
decl_stmt|;
specifier|private
name|MasterThread
name|master
decl_stmt|;
specifier|private
name|MasterThread
name|backupMaster
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
name|testUtil
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|testUtil
operator|.
name|startMiniDFSCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|testUtil
operator|.
name|startMiniZKCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|testUtil
operator|.
name|createRootDir
argument_list|()
expr_stmt|;
name|cluster
operator|=
operator|new
name|LocalHBaseCluster
argument_list|(
name|testUtil
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|0
argument_list|,
literal|0
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
name|cluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|cluster
operator|.
name|join
argument_list|()
expr_stmt|;
name|testUtil
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
name|testUtil
operator|.
name|shutdownMiniDFSCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Tests region sever reportForDuty with backup master becomes primary master after    * the first master goes away.    */
annotation|@
name|Test
specifier|public
name|void
name|testReportForDutyWithMasterChange
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Start a master and wait for it to become the active/primary master.
comment|// Use a random unique port
name|cluster
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|MASTER_PORT
argument_list|,
name|HBaseTestingUtility
operator|.
name|randomFreePort
argument_list|()
argument_list|)
expr_stmt|;
comment|// master has a rs. defaultMinToStart = 2
name|boolean
name|tablesOnMaster
init|=
name|LoadBalancer
operator|.
name|isTablesOnMaster
argument_list|(
name|testUtil
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|cluster
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|ServerManager
operator|.
name|WAIT_ON_REGIONSERVERS_MINTOSTART
argument_list|,
name|tablesOnMaster
condition|?
literal|2
else|:
literal|1
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|ServerManager
operator|.
name|WAIT_ON_REGIONSERVERS_MAXTOSTART
argument_list|,
name|tablesOnMaster
condition|?
literal|2
else|:
literal|1
argument_list|)
expr_stmt|;
name|master
operator|=
name|cluster
operator|.
name|addMaster
argument_list|()
expr_stmt|;
name|rs
operator|=
name|cluster
operator|.
name|addRegionServer
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Starting master: "
operator|+
name|master
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|master
operator|.
name|start
argument_list|()
expr_stmt|;
name|rs
operator|.
name|start
argument_list|()
expr_stmt|;
name|waitForClusterOnline
argument_list|(
name|master
argument_list|)
expr_stmt|;
comment|// Add a 2nd region server
name|cluster
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|REGION_SERVER_IMPL
argument_list|,
name|MyRegionServer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|rs2
operator|=
name|cluster
operator|.
name|addRegionServer
argument_list|()
expr_stmt|;
comment|// Start the region server. This region server will refresh RPC connection
comment|// from the current active master to the next active master before completing
comment|// reportForDuty
name|LOG
operator|.
name|debug
argument_list|(
literal|"Starting 2nd region server: "
operator|+
name|rs2
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|rs2
operator|.
name|start
argument_list|()
expr_stmt|;
name|waitForSecondRsStarted
argument_list|()
expr_stmt|;
comment|// Stop the current master.
name|master
operator|.
name|getMaster
argument_list|()
operator|.
name|stop
argument_list|(
literal|"Stopping master"
argument_list|)
expr_stmt|;
comment|// Start a new master and use another random unique port
comment|// Also let it wait for exactly 2 region severs to report in.
name|cluster
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|MASTER_PORT
argument_list|,
name|HBaseTestingUtility
operator|.
name|randomFreePort
argument_list|()
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|ServerManager
operator|.
name|WAIT_ON_REGIONSERVERS_MINTOSTART
argument_list|,
name|tablesOnMaster
condition|?
literal|3
else|:
literal|2
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|ServerManager
operator|.
name|WAIT_ON_REGIONSERVERS_MAXTOSTART
argument_list|,
name|tablesOnMaster
condition|?
literal|3
else|:
literal|2
argument_list|)
expr_stmt|;
name|backupMaster
operator|=
name|cluster
operator|.
name|addMaster
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Starting new master: "
operator|+
name|backupMaster
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|backupMaster
operator|.
name|start
argument_list|()
expr_stmt|;
name|waitForClusterOnline
argument_list|(
name|backupMaster
argument_list|)
expr_stmt|;
comment|// Do some checking/asserts here.
name|assertTrue
argument_list|(
name|backupMaster
operator|.
name|getMaster
argument_list|()
operator|.
name|isActiveMaster
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|backupMaster
operator|.
name|getMaster
argument_list|()
operator|.
name|isInitialized
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|backupMaster
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServersList
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|tablesOnMaster
condition|?
literal|3
else|:
literal|2
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|waitForClusterOnline
parameter_list|(
name|MasterThread
name|master
parameter_list|)
throws|throws
name|InterruptedException
block|{
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|master
operator|.
name|getMaster
argument_list|()
operator|.
name|isInitialized
argument_list|()
condition|)
block|{
break|break;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_INTERVAL
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Waiting for master to come online ..."
argument_list|)
expr_stmt|;
block|}
name|rs
operator|.
name|waitForServerOnline
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|waitForSecondRsStarted
parameter_list|()
throws|throws
name|InterruptedException
block|{
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
operator|(
operator|(
name|MyRegionServer
operator|)
name|rs2
operator|.
name|getRegionServer
argument_list|()
operator|)
operator|.
name|getRpcStubCreatedFlag
argument_list|()
operator|==
literal|true
condition|)
block|{
break|break;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_INTERVAL
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Waiting 2nd RS to be started ..."
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Create a Region Server that provide a hook so that we can wait for the master switch over
comment|// before continuing reportForDuty to the mater.
comment|// The idea is that we get a RPC connection to the first active master, then we wait.
comment|// The first master goes down, the second master becomes the active master. The region
comment|// server continues reportForDuty. It should succeed with the new master.
specifier|public
specifier|static
class|class
name|MyRegionServer
extends|extends
name|MiniHBaseClusterRegionServer
block|{
specifier|private
name|ServerName
name|sn
decl_stmt|;
comment|// This flag is to make sure this rs has obtained the rpcStub to the first master.
comment|// The first master will go down after this.
specifier|private
name|boolean
name|rpcStubCreatedFlag
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|masterChanged
init|=
literal|false
decl_stmt|;
specifier|public
name|MyRegionServer
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
annotation|@
name|Override
specifier|protected
specifier|synchronized
name|ServerName
name|createRegionServerStatusStub
parameter_list|(
name|boolean
name|refresh
parameter_list|)
block|{
name|sn
operator|=
name|super
operator|.
name|createRegionServerStatusStub
argument_list|(
name|refresh
argument_list|)
expr_stmt|;
name|rpcStubCreatedFlag
operator|=
literal|true
expr_stmt|;
comment|// Wait for master switch over. Only do this for the second region server.
while|while
condition|(
operator|!
name|masterChanged
condition|)
block|{
name|ServerName
name|newSn
init|=
name|super
operator|.
name|getMasterAddressTracker
argument_list|()
operator|.
name|getMasterAddress
argument_list|(
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|newSn
operator|!=
literal|null
operator|&&
operator|!
name|newSn
operator|.
name|equals
argument_list|(
name|sn
argument_list|)
condition|)
block|{
name|masterChanged
operator|=
literal|true
expr_stmt|;
break|break;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_INTERVAL
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Waiting for master switch over ... "
argument_list|)
expr_stmt|;
block|}
return|return
name|sn
return|;
block|}
specifier|public
name|boolean
name|getRpcStubCreatedFlag
parameter_list|()
block|{
return|return
name|rpcStubCreatedFlag
return|;
block|}
block|}
block|}
end_class

end_unit

