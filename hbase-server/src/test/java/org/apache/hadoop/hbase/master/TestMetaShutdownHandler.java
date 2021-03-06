begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|assertNotEquals
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
name|HBaseClassTestRule
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
name|StartMiniClusterOption
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
name|Waiter
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
name|assignment
operator|.
name|RegionStates
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
name|ZNodePaths
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
name|ClassRule
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

begin_comment
comment|/**  * Tests handling of meta-carrying region server failover.  */
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
name|TestMetaShutdownHandler
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
name|TestMetaShutdownHandler
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestMetaShutdownHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|final
specifier|static
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
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
name|StartMiniClusterOption
name|option
init|=
name|StartMiniClusterOption
operator|.
name|builder
argument_list|()
operator|.
name|numRegionServers
argument_list|(
literal|3
argument_list|)
operator|.
name|rsClass
argument_list|(
name|MyRegionServer
operator|.
name|class
argument_list|)
operator|.
name|numDataNodes
argument_list|(
literal|3
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|option
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
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * This test will test the expire handling of a meta-carrying    * region server.    * After HBaseMiniCluster is up, we will delete the ephemeral    * node of the meta-carrying region server, which will trigger    * the expire of this region server on the master.    * On the other hand, we will slow down the abort process on    * the region server so that it is still up during the master SSH.    * We will check that the master SSH is still successfully done.    */
annotation|@
name|Test
specifier|public
name|void
name|testExpireMetaRegionServer
parameter_list|()
throws|throws
name|Exception
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|HMaster
name|master
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|RegionStates
name|regionStates
init|=
name|master
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
decl_stmt|;
name|ServerName
name|metaServerName
init|=
name|regionStates
operator|.
name|getRegionServerOfRegion
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
decl_stmt|;
if|if
condition|(
name|master
operator|.
name|getServerName
argument_list|()
operator|.
name|equals
argument_list|(
name|metaServerName
argument_list|)
operator|||
name|metaServerName
operator|==
literal|null
operator|||
operator|!
name|metaServerName
operator|.
name|equals
argument_list|(
name|cluster
operator|.
name|getServerHoldingMeta
argument_list|()
argument_list|)
condition|)
block|{
comment|// Move meta off master
name|metaServerName
operator|=
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
expr_stmt|;
name|master
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
name|metaServerName
operator|=
name|regionStates
operator|.
name|getRegionServerOfRegion
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
expr_stmt|;
block|}
name|RegionState
name|metaState
init|=
name|MetaTableLocator
operator|.
name|getMetaRegionState
argument_list|(
name|master
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Wrong state for meta!"
argument_list|,
name|RegionState
operator|.
name|State
operator|.
name|OPEN
argument_list|,
name|metaState
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
literal|"Meta is on master!"
argument_list|,
name|metaServerName
argument_list|,
name|master
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Delete the ephemeral node of the meta-carrying region server.
comment|// This is trigger the expire of this region server on the master.
name|String
name|rsEphemeralNodePath
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|master
operator|.
name|getZooKeeper
argument_list|()
operator|.
name|getZNodePaths
argument_list|()
operator|.
name|rsZNode
argument_list|,
name|metaServerName
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|master
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|rsEphemeralNodePath
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleted the znode for the RegionServer hosting hbase:meta; waiting on SSH"
argument_list|)
expr_stmt|;
comment|// Wait for SSH to finish
specifier|final
name|ServerManager
name|serverManager
init|=
name|master
operator|.
name|getServerManager
argument_list|()
decl_stmt|;
specifier|final
name|ServerName
name|priorMetaServerName
init|=
name|metaServerName
decl_stmt|;
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|120000
argument_list|,
literal|200
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
return|return
operator|!
name|serverManager
operator|.
name|isServerOnline
argument_list|(
name|priorMetaServerName
argument_list|)
operator|&&
operator|!
name|serverManager
operator|.
name|areDeadServersInProgress
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Past wait on RIT"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilNoRegionsInTransition
argument_list|(
literal|60000
argument_list|)
expr_stmt|;
comment|// Now, make sure meta is assigned
name|assertTrue
argument_list|(
literal|"Meta should be assigned"
argument_list|,
name|regionStates
operator|.
name|isRegionOnline
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
argument_list|)
expr_stmt|;
comment|// Now, make sure meta is registered in zk
name|metaState
operator|=
name|MetaTableLocator
operator|.
name|getMetaRegionState
argument_list|(
name|master
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Meta should not be in transition"
argument_list|,
name|RegionState
operator|.
name|State
operator|.
name|OPEN
argument_list|,
name|metaState
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Meta should be assigned"
argument_list|,
name|metaState
operator|.
name|getServerName
argument_list|()
argument_list|,
name|regionStates
operator|.
name|getRegionServerOfRegion
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
literal|"Meta should be assigned on a different server"
argument_list|,
name|metaState
operator|.
name|getServerName
argument_list|()
argument_list|,
name|metaServerName
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|MyRegionServer
extends|extends
name|MiniHBaseClusterRegionServer
block|{
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
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|reason
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
comment|// sleep to slow down the region server abort
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|30
operator|*
literal|1000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
return|return;
block|}
name|super
operator|.
name|abort
argument_list|(
name|reason
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

