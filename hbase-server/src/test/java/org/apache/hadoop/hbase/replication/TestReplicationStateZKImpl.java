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
name|ChoreService
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
name|client
operator|.
name|ClusterConnection
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
name|ReplicationTests
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
name|ReplicationTests
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
name|TestReplicationStateZKImpl
extends|extends
name|TestReplicationStateBasic
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
name|TestReplicationStateZKImpl
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
specifier|private
specifier|static
name|ZooKeeperWatcher
name|zkw
decl_stmt|;
specifier|private
specifier|static
name|String
name|replicationZNode
decl_stmt|;
specifier|private
name|ReplicationQueuesZKImpl
name|rqZK
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
name|replicationZNodeName
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.replication"
argument_list|,
literal|"replication"
argument_list|)
decl_stmt|;
name|replicationZNode
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw
operator|.
name|baseZNode
argument_list|,
name|replicationZNodeName
argument_list|)
expr_stmt|;
name|KEY_ONE
operator|=
name|initPeerClusterState
argument_list|(
literal|"/hbase1"
argument_list|)
expr_stmt|;
name|KEY_TWO
operator|=
name|initPeerClusterState
argument_list|(
literal|"/hbase2"
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|String
name|initPeerClusterState
parameter_list|(
name|String
name|baseZKNode
parameter_list|)
throws|throws
name|IOException
throws|,
name|KeeperException
block|{
comment|// Add a dummy region server and set up the cluster id
name|Configuration
name|testConf
init|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|testConf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
name|baseZKNode
argument_list|)
expr_stmt|;
name|ZooKeeperWatcher
name|zkw1
init|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|testConf
argument_list|,
literal|"test1"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|String
name|fakeRs
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw1
operator|.
name|rsZNode
argument_list|,
literal|"hostname1.example.org:1234"
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkw1
argument_list|,
name|fakeRs
argument_list|)
expr_stmt|;
name|ZKClusterId
operator|.
name|setClusterId
argument_list|(
name|zkw1
argument_list|,
operator|new
name|ClusterId
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|ZKUtil
operator|.
name|getZooKeeperClusterKey
argument_list|(
name|testConf
argument_list|)
return|;
block|}
annotation|@
name|Before
annotation|@
name|Override
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|DummyServer
name|ds1
init|=
operator|new
name|DummyServer
argument_list|(
name|server1
argument_list|)
decl_stmt|;
name|DummyServer
name|ds2
init|=
operator|new
name|DummyServer
argument_list|(
name|server2
argument_list|)
decl_stmt|;
name|DummyServer
name|ds3
init|=
operator|new
name|DummyServer
argument_list|(
name|server3
argument_list|)
decl_stmt|;
name|rq1
operator|=
name|ReplicationFactory
operator|.
name|getReplicationQueues
argument_list|(
name|zkw
argument_list|,
name|conf
argument_list|,
name|ds1
argument_list|)
expr_stmt|;
name|rq2
operator|=
name|ReplicationFactory
operator|.
name|getReplicationQueues
argument_list|(
name|zkw
argument_list|,
name|conf
argument_list|,
name|ds2
argument_list|)
expr_stmt|;
name|rq3
operator|=
name|ReplicationFactory
operator|.
name|getReplicationQueues
argument_list|(
name|zkw
argument_list|,
name|conf
argument_list|,
name|ds3
argument_list|)
expr_stmt|;
name|rqc
operator|=
name|ReplicationFactory
operator|.
name|getReplicationQueuesClient
argument_list|(
name|zkw
argument_list|,
name|conf
argument_list|,
name|ds1
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
name|OUR_KEY
operator|=
name|ZKUtil
operator|.
name|getZooKeeperClusterKey
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|rqZK
operator|=
operator|new
name|ReplicationQueuesZKImpl
argument_list|(
name|zkw
argument_list|,
name|conf
argument_list|,
name|ds1
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
name|KeeperException
throws|,
name|IOException
block|{
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|zkw
argument_list|,
name|replicationZNode
argument_list|)
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
name|testIsPeerPath_PathToParentOfPeerNode
parameter_list|()
block|{
name|assertFalse
argument_list|(
name|rqZK
operator|.
name|isPeerPath
argument_list|(
name|rqZK
operator|.
name|peersZNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIsPeerPath_PathToChildOfPeerNode
parameter_list|()
block|{
name|String
name|peerChild
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|rqZK
operator|.
name|peersZNode
argument_list|,
literal|"1"
argument_list|)
argument_list|,
literal|"child"
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|rqZK
operator|.
name|isPeerPath
argument_list|(
name|peerChild
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIsPeerPath_ActualPeerPath
parameter_list|()
block|{
name|String
name|peerPath
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|rqZK
operator|.
name|peersZNode
argument_list|,
literal|"1"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|rqZK
operator|.
name|isPeerPath
argument_list|(
name|peerPath
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|static
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
name|ClusterConnection
name|getConnection
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
annotation|@
name|Override
specifier|public
name|ChoreService
name|getChoreService
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

