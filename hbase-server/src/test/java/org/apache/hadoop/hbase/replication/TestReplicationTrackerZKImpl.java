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
name|replication
package|;
end_package

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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|ClusterId
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
name|ZKClusterId
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
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Ignore
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
name|*
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
name|BeforeClass
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
comment|/**  * This class tests the ReplicationTrackerZKImpl class and ReplicationListener interface. One  * MiniZKCluster is used throughout the entire class. The cluster is initialized with the creation  * of the rsZNode. All other znode creation/initialization is handled by the replication state  * interfaces (i.e. ReplicationPeers, etc.). Each test case in this class should ensure that the  * MiniZKCluster is cleaned and returned to it's initial state (i.e. nothing but the rsZNode).  */
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
name|TestReplicationTrackerZKImpl
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
name|TestReplicationTrackerZKImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|utility
decl_stmt|;
comment|// Each one of the below variables are reinitialized before every test case
specifier|private
name|ZooKeeperWatcher
name|zkw
decl_stmt|;
specifier|private
name|ReplicationPeers
name|rp
decl_stmt|;
specifier|private
name|ReplicationTracker
name|rt
decl_stmt|;
specifier|private
name|AtomicInteger
name|rsRemovedCount
decl_stmt|;
specifier|private
name|String
name|rsRemovedData
decl_stmt|;
specifier|private
name|AtomicInteger
name|plChangedCount
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|plChangedData
decl_stmt|;
specifier|private
name|AtomicInteger
name|peerRemovedCount
decl_stmt|;
specifier|private
name|String
name|peerRemovedData
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
name|utility
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|utility
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|conf
operator|=
name|utility
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|ZooKeeperWatcher
name|zk
init|=
name|HBaseTestingUtility
operator|.
name|getZooKeeperWatcher
argument_list|(
name|utility
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zk
argument_list|,
name|zk
operator|.
name|rsZNode
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|zkw
operator|=
name|HBaseTestingUtility
operator|.
name|getZooKeeperWatcher
argument_list|(
name|utility
argument_list|)
expr_stmt|;
name|String
name|fakeRs1
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw
operator|.
name|rsZNode
argument_list|,
literal|"hostname1.example.org:1234"
argument_list|)
decl_stmt|;
try|try
block|{
name|ZKClusterId
operator|.
name|setClusterId
argument_list|(
name|zkw
argument_list|,
operator|new
name|ClusterId
argument_list|()
argument_list|)
expr_stmt|;
name|rp
operator|=
name|ReplicationFactory
operator|.
name|getReplicationPeers
argument_list|(
name|zkw
argument_list|,
name|conf
argument_list|,
name|zkw
argument_list|)
expr_stmt|;
name|rp
operator|.
name|init
argument_list|()
expr_stmt|;
name|rt
operator|=
name|ReplicationFactory
operator|.
name|getReplicationTracker
argument_list|(
name|zkw
argument_list|,
name|rp
argument_list|,
name|conf
argument_list|,
name|zkw
argument_list|,
operator|new
name|DummyServer
argument_list|(
name|fakeRs1
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Exception during test setup: "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
name|rsRemovedCount
operator|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|rsRemovedData
operator|=
literal|""
expr_stmt|;
name|plChangedCount
operator|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|plChangedData
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|peerRemovedCount
operator|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|peerRemovedData
operator|=
literal|""
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
name|utility
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetListOfRegionServers
parameter_list|()
throws|throws
name|Exception
block|{
comment|// 0 region servers
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rt
operator|.
name|getListOfRegionServers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// 1 region server
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkw
argument_list|,
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw
operator|.
name|rsZNode
argument_list|,
literal|"hostname1.example.org:1234"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rt
operator|.
name|getListOfRegionServers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// 2 region servers
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkw
argument_list|,
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw
operator|.
name|rsZNode
argument_list|,
literal|"hostname2.example.org:1234"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|rt
operator|.
name|getListOfRegionServers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// 1 region server
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|zkw
argument_list|,
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw
operator|.
name|rsZNode
argument_list|,
literal|"hostname2.example.org:1234"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rt
operator|.
name|getListOfRegionServers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// 0 region server
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|zkw
argument_list|,
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw
operator|.
name|rsZNode
argument_list|,
literal|"hostname1.example.org:1234"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rt
operator|.
name|getListOfRegionServers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testRegionServerRemovedEvent
parameter_list|()
throws|throws
name|Exception
block|{
name|ZKUtil
operator|.
name|createAndWatch
argument_list|(
name|zkw
argument_list|,
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw
operator|.
name|rsZNode
argument_list|,
literal|"hostname2.example.org:1234"
argument_list|)
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
expr_stmt|;
name|rt
operator|.
name|registerListener
argument_list|(
operator|new
name|DummyReplicationListener
argument_list|()
argument_list|)
expr_stmt|;
comment|// delete one
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|zkw
argument_list|,
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw
operator|.
name|rsZNode
argument_list|,
literal|"hostname2.example.org:1234"
argument_list|)
argument_list|)
expr_stmt|;
comment|// wait for event
while|while
condition|(
name|rsRemovedCount
operator|.
name|get
argument_list|()
operator|<
literal|1
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|5
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"hostname2.example.org:1234"
argument_list|,
name|rsRemovedData
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
argument_list|(
literal|"Flakey"
argument_list|)
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testPeerRemovedEvent
parameter_list|()
throws|throws
name|Exception
block|{
name|rp
operator|.
name|addPeer
argument_list|(
literal|"5"
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|utility
operator|.
name|getClusterKey
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|rt
operator|.
name|registerListener
argument_list|(
operator|new
name|DummyReplicationListener
argument_list|()
argument_list|)
expr_stmt|;
name|rp
operator|.
name|removePeer
argument_list|(
literal|"5"
argument_list|)
expr_stmt|;
comment|// wait for event
while|while
condition|(
name|peerRemovedCount
operator|.
name|get
argument_list|()
operator|<
literal|1
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|5
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"5"
argument_list|,
name|peerRemovedData
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
argument_list|(
literal|"Flakey"
argument_list|)
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testPeerListChangedEvent
parameter_list|()
throws|throws
name|Exception
block|{
comment|// add a peer
name|rp
operator|.
name|addPeer
argument_list|(
literal|"5"
argument_list|,
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|utility
operator|.
name|getClusterKey
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|zkw
operator|.
name|getRecoverableZooKeeper
argument_list|()
operator|.
name|getZooKeeper
argument_list|()
operator|.
name|getChildren
argument_list|(
literal|"/hbase/replication/peers/5"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|rt
operator|.
name|registerListener
argument_list|(
operator|new
name|DummyReplicationListener
argument_list|()
argument_list|)
expr_stmt|;
name|rp
operator|.
name|disablePeer
argument_list|(
literal|"5"
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|zkw
argument_list|,
literal|"/hbase/replication/peers/5/peer-state"
argument_list|)
expr_stmt|;
comment|// wait for event
name|int
name|tmp
init|=
name|plChangedCount
operator|.
name|get
argument_list|()
decl_stmt|;
while|while
condition|(
name|plChangedCount
operator|.
name|get
argument_list|()
operator|<=
name|tmp
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|5
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|plChangedData
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|plChangedData
operator|.
name|contains
argument_list|(
literal|"5"
argument_list|)
argument_list|)
expr_stmt|;
comment|// clean up
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|zkw
argument_list|,
literal|"/hbase/replication/peers/5"
argument_list|)
expr_stmt|;
block|}
specifier|private
class|class
name|DummyReplicationListener
implements|implements
name|ReplicationListener
block|{
annotation|@
name|Override
specifier|public
name|void
name|regionServerRemoved
parameter_list|(
name|String
name|regionServer
parameter_list|)
block|{
name|rsRemovedData
operator|=
name|regionServer
expr_stmt|;
name|rsRemovedCount
operator|.
name|getAndIncrement
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Received regionServerRemoved event: "
operator|+
name|regionServer
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|peerRemoved
parameter_list|(
name|String
name|peerId
parameter_list|)
block|{
name|peerRemovedData
operator|=
name|peerId
expr_stmt|;
name|peerRemovedCount
operator|.
name|getAndIncrement
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Received peerRemoved event: "
operator|+
name|peerId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|peerListChanged
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|peerIds
parameter_list|)
block|{
name|plChangedData
operator|.
name|clear
argument_list|()
expr_stmt|;
name|plChangedData
operator|.
name|addAll
argument_list|(
name|peerIds
argument_list|)
expr_stmt|;
name|plChangedCount
operator|.
name|getAndIncrement
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Received peerListChanged event"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
class|class
name|DummyServer
implements|implements
name|Server
block|{
specifier|private
name|String
name|serverName
decl_stmt|;
specifier|private
name|boolean
name|isAborted
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|isStopped
init|=
literal|false
decl_stmt|;
specifier|public
name|DummyServer
parameter_list|(
name|String
name|serverName
parameter_list|)
block|{
name|this
operator|.
name|serverName
operator|=
name|serverName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|ZooKeeperWatcher
name|getZooKeeper
parameter_list|()
block|{
return|return
name|zkw
return|;
block|}
annotation|@
name|Override
specifier|public
name|CoordinatedStateManager
name|getCoordinatedStateManager
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|HConnection
name|getShortCircuitConnection
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|MetaTableLocator
name|getMetaTableLocator
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
name|ServerName
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|serverName
argument_list|)
return|;
block|}
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Aborting "
operator|+
name|serverName
argument_list|)
expr_stmt|;
name|this
operator|.
name|isAborted
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
name|this
operator|.
name|isAborted
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{
name|this
operator|.
name|isStopped
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
name|this
operator|.
name|isStopped
return|;
block|}
block|}
block|}
end_class

end_unit

