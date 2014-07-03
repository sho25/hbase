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
name|net
operator|.
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|UnknownHostException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|Set
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
name|Abortable
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
name|CoordinatedStateException
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
name|CoordinatedStateManagerFactory
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
name|Server
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
name|ServerLoad
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
name|TableName
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
name|ZooKeeperConnectionException
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
name|MetaMockingUtil
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
name|HConnection
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
name|HConnectionTestingUtility
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
name|Result
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
name|monitoring
operator|.
name|MonitoredTask
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
name|protobuf
operator|.
name|generated
operator|.
name|RegionServerStatusProtos
operator|.
name|RegionServerReportRequest
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
name|FSUtils
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
name|hadoop
operator|.
name|metrics2
operator|.
name|lib
operator|.
name|DefaultMetricsSystem
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

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
import|;
end_import

begin_comment
comment|/**  * Standup the master and fake it to test various aspects of master function.  * Does NOT spin up a mini hbase nor mini dfs cluster testing master (it does  * put up a zk cluster but this is usually pretty fast compared).  Also, should  * be possible to inject faults at points difficult to get at in cluster context.  * TODO: Speed up the zk connection by Master.  It pauses 5 seconds establishing  * session.  */
end_comment

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
name|TestMasterNoCluster
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
name|TestMasterNoCluster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TESTUTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|c
init|=
name|TESTUTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// We use local filesystem.  Set it so it writes into the testdir.
name|FSUtils
operator|.
name|setRootDir
argument_list|(
name|c
argument_list|,
name|TESTUTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|)
expr_stmt|;
name|DefaultMetricsSystem
operator|.
name|setMiniClusterMode
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Startup a mini zk cluster.
name|TESTUTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TESTUTIL
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|KeeperException
throws|,
name|ZooKeeperConnectionException
throws|,
name|IOException
block|{
comment|// Make sure zk is clean before we run the next test.
name|ZooKeeperWatcher
name|zkw
init|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|TESTUTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|"@Before"
argument_list|,
operator|new
name|Abortable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|why
argument_list|,
name|e
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|zkw
argument_list|,
name|zkw
operator|.
name|baseZNode
argument_list|)
expr_stmt|;
name|zkw
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test starting master then stopping it before its fully up.    * @throws IOException    * @throws KeeperException    * @throws InterruptedException    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testStopDuringStart
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
throws|,
name|InterruptedException
block|{
name|CoordinatedStateManager
name|cp
init|=
name|CoordinatedStateManagerFactory
operator|.
name|getCoordinatedStateManager
argument_list|(
name|TESTUTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|HMaster
name|master
init|=
operator|new
name|HMaster
argument_list|(
name|TESTUTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|cp
argument_list|)
decl_stmt|;
name|master
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// Immediately have it stop.  We used hang in assigning meta.
name|master
operator|.
name|stopMaster
argument_list|()
expr_stmt|;
name|master
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test master failover.    * Start up three fake regionservers and a master.    * @throws IOException    * @throws KeeperException    * @throws InterruptedException    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testFailover
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
throws|,
name|InterruptedException
throws|,
name|ServiceException
block|{
specifier|final
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
comment|// Names for our three servers.  Make the port numbers match hostname.
comment|// Will come in use down in the server when we need to figure how to respond.
specifier|final
name|ServerName
name|sn0
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"0.example.org"
argument_list|,
literal|0
argument_list|,
name|now
argument_list|)
decl_stmt|;
specifier|final
name|ServerName
name|sn1
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"1.example.org"
argument_list|,
literal|1
argument_list|,
name|now
argument_list|)
decl_stmt|;
specifier|final
name|ServerName
name|sn2
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"2.example.org"
argument_list|,
literal|2
argument_list|,
name|now
argument_list|)
decl_stmt|;
specifier|final
name|ServerName
index|[]
name|sns
init|=
operator|new
name|ServerName
index|[]
block|{
name|sn0
block|,
name|sn1
block|,
name|sn2
block|}
decl_stmt|;
comment|// Put up the mock servers
specifier|final
name|Configuration
name|conf
init|=
name|TESTUTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|final
name|MockRegionServer
name|rs0
init|=
operator|new
name|MockRegionServer
argument_list|(
name|conf
argument_list|,
name|sn0
argument_list|)
decl_stmt|;
specifier|final
name|MockRegionServer
name|rs1
init|=
operator|new
name|MockRegionServer
argument_list|(
name|conf
argument_list|,
name|sn1
argument_list|)
decl_stmt|;
specifier|final
name|MockRegionServer
name|rs2
init|=
operator|new
name|MockRegionServer
argument_list|(
name|conf
argument_list|,
name|sn2
argument_list|)
decl_stmt|;
comment|// Put some data into the servers.  Make it look like sn0 has the metaH
comment|// Put data into sn2 so it looks like it has a few regions for a table named 't'.
name|MetaTableLocator
operator|.
name|setMetaLocation
argument_list|(
name|rs0
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|rs0
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"t"
argument_list|)
decl_stmt|;
name|Result
index|[]
name|results
init|=
operator|new
name|Result
index|[]
block|{
name|MetaMockingUtil
operator|.
name|getMetaTableRowResult
argument_list|(
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|HBaseTestingUtility
operator|.
name|KEYS
index|[
literal|1
index|]
argument_list|)
argument_list|,
name|rs2
operator|.
name|getServerName
argument_list|()
argument_list|)
block|,
name|MetaMockingUtil
operator|.
name|getMetaTableRowResult
argument_list|(
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|,
name|HBaseTestingUtility
operator|.
name|KEYS
index|[
literal|1
index|]
argument_list|,
name|HBaseTestingUtility
operator|.
name|KEYS
index|[
literal|2
index|]
argument_list|)
argument_list|,
name|rs2
operator|.
name|getServerName
argument_list|()
argument_list|)
block|,
name|MetaMockingUtil
operator|.
name|getMetaTableRowResult
argument_list|(
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|,
name|HBaseTestingUtility
operator|.
name|KEYS
index|[
literal|2
index|]
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
argument_list|,
name|rs2
operator|.
name|getServerName
argument_list|()
argument_list|)
block|}
decl_stmt|;
name|rs1
operator|.
name|setNextResults
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|results
argument_list|)
expr_stmt|;
comment|// Create master.  Subclass to override a few methods so we can insert mocks
comment|// and get notification on transitions.  We need to fake out any rpcs the
comment|// master does opening/closing regions.  Also need to fake out the address
comment|// of the 'remote' mocked up regionservers.
name|CoordinatedStateManager
name|cp
init|=
name|CoordinatedStateManagerFactory
operator|.
name|getCoordinatedStateManager
argument_list|(
name|TESTUTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|HMaster
name|master
init|=
operator|new
name|HMaster
argument_list|(
name|conf
argument_list|,
name|cp
argument_list|)
block|{
name|InetAddress
name|getRemoteInetAddress
parameter_list|(
specifier|final
name|int
name|port
parameter_list|,
specifier|final
name|long
name|serverStartCode
parameter_list|)
throws|throws
name|UnknownHostException
block|{
comment|// Return different address dependent on port passed.
if|if
condition|(
name|port
operator|>
name|sns
operator|.
name|length
condition|)
block|{
return|return
name|super
operator|.
name|getRemoteInetAddress
argument_list|(
name|port
argument_list|,
name|serverStartCode
argument_list|)
return|;
block|}
name|ServerName
name|sn
init|=
name|sns
index|[
name|port
index|]
decl_stmt|;
return|return
name|InetAddress
operator|.
name|getByAddress
argument_list|(
name|sn
operator|.
name|getHostname
argument_list|()
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|10
block|,
literal|0
block|,
literal|0
block|,
operator|(
name|byte
operator|)
name|sn
operator|.
name|getPort
argument_list|()
block|}
argument_list|)
return|;
block|}
annotation|@
name|Override
name|ServerManager
name|createServerManager
parameter_list|(
name|Server
name|master
parameter_list|,
name|MasterServices
name|services
parameter_list|)
throws|throws
name|IOException
block|{
name|ServerManager
name|sm
init|=
name|super
operator|.
name|createServerManager
argument_list|(
name|master
argument_list|,
name|services
argument_list|)
decl_stmt|;
comment|// Spy on the created servermanager
name|ServerManager
name|spy
init|=
name|Mockito
operator|.
name|spy
argument_list|(
name|sm
argument_list|)
decl_stmt|;
comment|// Fake a successful close.
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|true
argument_list|)
operator|.
name|when
argument_list|(
name|spy
argument_list|)
operator|.
name|sendRegionClose
argument_list|(
operator|(
name|ServerName
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|,
operator|(
name|HRegionInfo
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|,
name|Mockito
operator|.
name|anyInt
argument_list|()
argument_list|,
operator|(
name|ServerName
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|,
name|Mockito
operator|.
name|anyBoolean
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|spy
return|;
block|}
annotation|@
name|Override
specifier|public
name|HConnection
name|getShortCircuitConnection
parameter_list|()
block|{
comment|// Insert a mock for the connection, use TESTUTIL.getConfiguration rather than
comment|// the conf from the master; the conf will already have an HConnection
comment|// associate so the below mocking of a connection will fail.
try|try
block|{
return|return
name|HConnectionTestingUtility
operator|.
name|getMockedConnectionAndDecorate
argument_list|(
name|TESTUTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|rs0
argument_list|,
name|rs0
argument_list|,
name|rs0
operator|.
name|getServerName
argument_list|()
argument_list|,
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
name|void
name|initNamespace
parameter_list|()
block|{       }
block|}
decl_stmt|;
name|master
operator|.
name|start
argument_list|()
expr_stmt|;
try|try
block|{
comment|// Wait till master is up ready for RPCs.
while|while
condition|(
operator|!
name|master
operator|.
name|serviceStarted
condition|)
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
comment|// Fake master that there are regionservers out there.  Report in.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|sns
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|RegionServerReportRequest
operator|.
name|Builder
name|request
init|=
name|RegionServerReportRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
empty_stmt|;
name|ServerName
name|sn
init|=
name|ServerName
operator|.
name|parseVersionedServerName
argument_list|(
name|sns
index|[
name|i
index|]
operator|.
name|getVersionedBytes
argument_list|()
argument_list|)
decl_stmt|;
name|request
operator|.
name|setServer
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|sn
argument_list|)
argument_list|)
expr_stmt|;
name|request
operator|.
name|setLoad
argument_list|(
name|ServerLoad
operator|.
name|EMPTY_SERVERLOAD
operator|.
name|obtainServerLoadPB
argument_list|()
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
block|}
name|ZooKeeperWatcher
name|zkw
init|=
name|master
operator|.
name|getZooKeeper
argument_list|()
decl_stmt|;
comment|// Master should now come up.
while|while
condition|(
operator|!
name|master
operator|.
name|isInitialized
argument_list|()
condition|)
block|{
comment|// Fake meta is closed on rs0, try several times in case the event is lost
comment|// due to race with HMaster#assignMeta
name|ZKAssign
operator|.
name|transitionNodeClosed
argument_list|(
name|zkw
argument_list|,
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|sn0
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|master
operator|.
name|isInitialized
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|rs0
operator|.
name|stop
argument_list|(
literal|"Test is done"
argument_list|)
expr_stmt|;
name|rs1
operator|.
name|stop
argument_list|(
literal|"Test is done"
argument_list|)
expr_stmt|;
name|rs2
operator|.
name|stop
argument_list|(
literal|"Test is done"
argument_list|)
expr_stmt|;
name|master
operator|.
name|stopMaster
argument_list|()
expr_stmt|;
name|master
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNotPullingDeadRegionServerFromZK
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
throws|,
name|InterruptedException
block|{
specifier|final
name|Configuration
name|conf
init|=
name|TESTUTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|final
name|ServerName
name|newServer
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"test.sample"
argument_list|,
literal|1
argument_list|,
literal|101
argument_list|)
decl_stmt|;
specifier|final
name|ServerName
name|deadServer
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"test.sample"
argument_list|,
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
specifier|final
name|MockRegionServer
name|rs0
init|=
operator|new
name|MockRegionServer
argument_list|(
name|conf
argument_list|,
name|newServer
argument_list|)
decl_stmt|;
name|CoordinatedStateManager
name|cp
init|=
name|CoordinatedStateManagerFactory
operator|.
name|getCoordinatedStateManager
argument_list|(
name|TESTUTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|HMaster
name|master
init|=
operator|new
name|HMaster
argument_list|(
name|conf
argument_list|,
name|cp
argument_list|)
block|{
annotation|@
name|Override
name|void
name|assignMeta
parameter_list|(
name|MonitoredTask
name|status
parameter_list|,
name|Set
argument_list|<
name|ServerName
argument_list|>
name|previouslyFailedMeatRSs
parameter_list|)
block|{       }
annotation|@
name|Override
name|void
name|initializeZKBasedSystemTrackers
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|KeeperException
throws|,
name|CoordinatedStateException
block|{
name|super
operator|.
name|initializeZKBasedSystemTrackers
argument_list|()
expr_stmt|;
comment|// Record a newer server in server manager at first
name|serverManager
operator|.
name|recordNewServerWithLock
argument_list|(
name|newServer
argument_list|,
name|ServerLoad
operator|.
name|EMPTY_SERVERLOAD
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|onlineServers
init|=
operator|new
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
name|onlineServers
operator|.
name|add
argument_list|(
name|deadServer
argument_list|)
expr_stmt|;
name|onlineServers
operator|.
name|add
argument_list|(
name|newServer
argument_list|)
expr_stmt|;
comment|// Mock the region server tracker to pull the dead server from zk
name|regionServerTracker
operator|=
name|Mockito
operator|.
name|spy
argument_list|(
name|regionServerTracker
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|onlineServers
argument_list|)
operator|.
name|when
argument_list|(
name|regionServerTracker
argument_list|)
operator|.
name|getOnlineServers
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|HConnection
name|getShortCircuitConnection
parameter_list|()
block|{
comment|// Insert a mock for the connection, use TESTUTIL.getConfiguration rather than
comment|// the conf from the master; the conf will already have an HConnection
comment|// associate so the below mocking of a connection will fail.
try|try
block|{
return|return
name|HConnectionTestingUtility
operator|.
name|getMockedConnectionAndDecorate
argument_list|(
name|TESTUTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|rs0
argument_list|,
name|rs0
argument_list|,
name|rs0
operator|.
name|getServerName
argument_list|()
argument_list|,
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
name|void
name|initNamespace
parameter_list|()
block|{       }
block|}
decl_stmt|;
name|master
operator|.
name|start
argument_list|()
expr_stmt|;
try|try
block|{
comment|// Wait till master is initialized.
while|while
condition|(
operator|!
name|master
operator|.
name|initialized
condition|)
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Master is initialized"
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"The dead server should not be pulled in"
argument_list|,
name|master
operator|.
name|serverManager
operator|.
name|isServerOnline
argument_list|(
name|deadServer
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|master
operator|.
name|stopMaster
argument_list|()
expr_stmt|;
name|master
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

