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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReference
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
name|HMaster
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
name|ServerListener
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
name|master
operator|.
name|balancer
operator|.
name|BaseLoadBalancer
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
operator|.
name|RegionServerStartupResponse
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
name|Threads
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
name|TestName
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

begin_comment
comment|/**  * Tests that a regionserver that dies after reporting for duty gets removed  * from list of online regions. See HBASE-9593.  */
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
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestRSKilledWhenInitializing
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
name|TestRSKilledWhenInitializing
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|testName
init|=
operator|new
name|TestName
argument_list|()
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
comment|// This boolean needs to be globally available. It is used below in our
comment|// mocked up regionserver so it knows when to die.
specifier|private
specifier|static
name|AtomicBoolean
name|masterActive
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
comment|// Ditto for this variable. It also is used in the mocked regionserver class.
specifier|private
specifier|static
specifier|final
name|AtomicReference
argument_list|<
name|ServerName
argument_list|>
name|killedRS
init|=
operator|new
name|AtomicReference
argument_list|<
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_MASTERS
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_RS
init|=
literal|2
decl_stmt|;
comment|/**    * Test verifies whether a region server is removed from online servers list in master if it went    * down after registering with master. Test will TIMEOUT if an error!!!!    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testRSTerminationAfterRegisteringToMasterBeforeCreatingEphemeralNode
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Create config to use for this cluster
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
name|ServerManager
operator|.
name|WAIT_ON_REGIONSERVERS_MINTOSTART
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// Start the cluster
specifier|final
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
name|startMiniDFSCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|createRootDir
argument_list|()
expr_stmt|;
specifier|final
name|LocalHBaseCluster
name|cluster
init|=
operator|new
name|LocalHBaseCluster
argument_list|(
name|conf
argument_list|,
name|NUM_MASTERS
argument_list|,
name|NUM_RS
argument_list|,
name|HMaster
operator|.
name|class
argument_list|,
name|RegisterAndDieRegionServer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|MasterThread
name|master
init|=
name|startMaster
argument_list|(
name|cluster
operator|.
name|getMasters
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
comment|// Master is up waiting on RegionServers to check in. Now start RegionServers.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUM_RS
condition|;
name|i
operator|++
control|)
block|{
name|cluster
operator|.
name|getRegionServers
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
comment|// Expected total regionservers depends on whether Master can host regions or not.
name|int
name|expectedTotalRegionServers
init|=
name|NUM_RS
operator|+
operator|(
name|LoadBalancer
operator|.
name|isTablesOnMaster
argument_list|(
name|conf
argument_list|)
condition|?
literal|1
else|:
literal|0
operator|)
decl_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|onlineServersList
init|=
literal|null
decl_stmt|;
do|do
block|{
name|onlineServersList
operator|=
name|master
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServersList
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|onlineServersList
operator|.
name|size
argument_list|()
operator|<
name|expectedTotalRegionServers
condition|)
do|;
comment|// Wait until killedRS is set. Means RegionServer is starting to go down.
while|while
condition|(
name|killedRS
operator|.
name|get
argument_list|()
operator|==
literal|null
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// Wait on the RegionServer to fully die.
while|while
condition|(
name|cluster
operator|.
name|getLiveRegionServers
argument_list|()
operator|.
name|size
argument_list|()
operator|>=
name|expectedTotalRegionServers
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// Make sure Master is fully up before progressing. Could take a while if regions
comment|// being reassigned.
while|while
condition|(
operator|!
name|master
operator|.
name|getMaster
argument_list|()
operator|.
name|isInitialized
argument_list|()
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// Now in steady state. How many regions open? Master should have too many regionservers
comment|// showing still. The downed RegionServer should still be showing as registered.
name|assertTrue
argument_list|(
name|master
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerManager
argument_list|()
operator|.
name|isServerOnline
argument_list|(
name|killedRS
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Find non-meta region (namespace?) and assign to the killed server. That'll trigger cleanup.
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|assignments
init|=
literal|null
decl_stmt|;
do|do
block|{
name|assignments
operator|=
name|master
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionAssignments
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|assignments
operator|==
literal|null
operator|||
name|assignments
operator|.
name|size
argument_list|()
operator|<
literal|2
condition|)
do|;
name|HRegionInfo
name|hri
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|e
range|:
name|assignments
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|isMetaRegion
argument_list|()
condition|)
continue|continue;
name|hri
operator|=
name|e
operator|.
name|getKey
argument_list|()
expr_stmt|;
break|break;
block|}
comment|// Try moving region to the killed server. It will fail. As by-product, we will
comment|// remove the RS from Master online list because no corresponding znode.
name|assertEquals
argument_list|(
name|expectedTotalRegionServers
argument_list|,
name|master
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
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Move "
operator|+
name|hri
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|" to "
operator|+
name|killedRS
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|master
operator|.
name|getMaster
argument_list|()
operator|.
name|move
argument_list|(
name|hri
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|killedRS
operator|.
name|get
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Wait until the RS no longer shows as registered in Master.
while|while
condition|(
name|onlineServersList
operator|.
name|size
argument_list|()
operator|>
operator|(
name|NUM_RS
operator|+
literal|1
operator|)
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|onlineServersList
operator|=
name|master
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServersList
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
comment|// Shutdown is messy with complaints about fs being closed. Why? TODO.
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
name|TEST_UTIL
operator|.
name|shutdownMiniDFSCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|cleanupTestDir
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Start Master. Get as far as the state where Master is waiting on    * RegionServers to check in, then return.    */
specifier|private
name|MasterThread
name|startMaster
parameter_list|(
name|MasterThread
name|master
parameter_list|)
block|{
name|master
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// It takes a while until ServerManager creation to happen inside Master startup.
while|while
condition|(
name|master
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerManager
argument_list|()
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
comment|// Set a listener for the waiting-on-RegionServers state. We want to wait
comment|// until this condition before we leave this method and start regionservers.
specifier|final
name|AtomicBoolean
name|waiting
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|master
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerManager
argument_list|()
operator|==
literal|null
condition|)
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"SM"
argument_list|)
throw|;
name|master
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerManager
argument_list|()
operator|.
name|registerListener
argument_list|(
operator|new
name|ServerListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|waiting
parameter_list|()
block|{
name|waiting
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// Wait until the Master gets to place where it is waiting on RegionServers to check in.
while|while
condition|(
operator|!
name|waiting
operator|.
name|get
argument_list|()
condition|)
block|{
continue|continue;
block|}
comment|// Set the global master-is-active; gets picked up by regionservers later.
name|masterActive
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|master
return|;
block|}
comment|/**    * A RegionServer that reports for duty and then immediately dies if it is the first to receive    * the response to a reportForDuty. When it dies, it clears its ephemeral znode which the master    * notices and so removes the region from its set of online regionservers.    */
specifier|static
class|class
name|RegisterAndDieRegionServer
extends|extends
name|MiniHBaseCluster
operator|.
name|MiniHBaseClusterRegionServer
block|{
specifier|public
name|RegisterAndDieRegionServer
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|CoordinatedStateManager
name|cp
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|cp
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|handleReportForDutyResponse
parameter_list|(
name|RegionServerStartupResponse
name|c
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|killedRS
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|getServerName
argument_list|()
argument_list|)
condition|)
block|{
comment|// Make sure Master is up so it will see the removal of the ephemeral znode for this RS.
while|while
condition|(
operator|!
name|masterActive
operator|.
name|get
argument_list|()
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|kill
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|super
operator|.
name|handleReportForDutyResponse
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

