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
name|concurrent
operator|.
name|Semaphore
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
name|fs
operator|.
name|FileSystem
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
name|client
operator|.
name|Connection
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
name|testclassification
operator|.
name|MasterTests
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
name|zookeeper
operator|.
name|ClusterStatusTracker
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
name|MasterAddressTracker
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
name|ZKListener
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
name|ZKWatcher
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

begin_comment
comment|/**  * Test the {@link ActiveMasterManager}.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
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
name|TestActiveMasterManager
block|{
specifier|private
specifier|final
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestActiveMasterManager
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
name|TEST_UTIL
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
name|TEST_UTIL
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRestartMaster
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
block|{
name|ZKWatcher
name|zk
init|=
operator|new
name|ZKWatcher
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|"testActiveMasterManagerFromZK"
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
decl_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|zk
argument_list|,
name|zk
operator|.
name|znodePaths
operator|.
name|masterAddressZNode
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|zk
argument_list|,
name|zk
operator|.
name|znodePaths
operator|.
name|clusterStateZNode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NoNodeException
name|nne
parameter_list|)
block|{}
comment|// Create the master node with a dummy address
name|ServerName
name|master
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"localhost"
argument_list|,
literal|1
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
comment|// Should not have a master yet
name|DummyMaster
name|dummyMaster
init|=
operator|new
name|DummyMaster
argument_list|(
name|zk
argument_list|,
name|master
argument_list|)
decl_stmt|;
name|ClusterStatusTracker
name|clusterStatusTracker
init|=
name|dummyMaster
operator|.
name|getClusterStatusTracker
argument_list|()
decl_stmt|;
name|ActiveMasterManager
name|activeMasterManager
init|=
name|dummyMaster
operator|.
name|getActiveMasterManager
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|activeMasterManager
operator|.
name|clusterHasActiveMaster
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// First test becoming the active master uninterrupted
name|MonitoredTask
name|status
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|MonitoredTask
operator|.
name|class
argument_list|)
decl_stmt|;
name|clusterStatusTracker
operator|.
name|setClusterUp
argument_list|()
expr_stmt|;
name|activeMasterManager
operator|.
name|blockUntilBecomingActiveMaster
argument_list|(
literal|100
argument_list|,
name|status
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|activeMasterManager
operator|.
name|clusterHasActiveMaster
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertMaster
argument_list|(
name|zk
argument_list|,
name|master
argument_list|)
expr_stmt|;
comment|// Now pretend master restart
name|DummyMaster
name|secondDummyMaster
init|=
operator|new
name|DummyMaster
argument_list|(
name|zk
argument_list|,
name|master
argument_list|)
decl_stmt|;
name|ActiveMasterManager
name|secondActiveMasterManager
init|=
name|secondDummyMaster
operator|.
name|getActiveMasterManager
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|secondActiveMasterManager
operator|.
name|clusterHasActiveMaster
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|activeMasterManager
operator|.
name|blockUntilBecomingActiveMaster
argument_list|(
literal|100
argument_list|,
name|status
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|activeMasterManager
operator|.
name|clusterHasActiveMaster
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertMaster
argument_list|(
name|zk
argument_list|,
name|master
argument_list|)
expr_stmt|;
block|}
comment|/**    * Unit tests that uses ZooKeeper but does not use the master-side methods    * but rather acts directly on ZK.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testActiveMasterManagerFromZK
parameter_list|()
throws|throws
name|Exception
block|{
name|ZKWatcher
name|zk
init|=
operator|new
name|ZKWatcher
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|"testActiveMasterManagerFromZK"
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
decl_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|zk
argument_list|,
name|zk
operator|.
name|znodePaths
operator|.
name|masterAddressZNode
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|zk
argument_list|,
name|zk
operator|.
name|znodePaths
operator|.
name|clusterStateZNode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NoNodeException
name|nne
parameter_list|)
block|{}
comment|// Create the master node with a dummy address
name|ServerName
name|firstMasterAddress
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"localhost"
argument_list|,
literal|1
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|ServerName
name|secondMasterAddress
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"localhost"
argument_list|,
literal|2
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
comment|// Should not have a master yet
name|DummyMaster
name|ms1
init|=
operator|new
name|DummyMaster
argument_list|(
name|zk
argument_list|,
name|firstMasterAddress
argument_list|)
decl_stmt|;
name|ActiveMasterManager
name|activeMasterManager
init|=
name|ms1
operator|.
name|getActiveMasterManager
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|activeMasterManager
operator|.
name|clusterHasActiveMaster
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// First test becoming the active master uninterrupted
name|ClusterStatusTracker
name|clusterStatusTracker
init|=
name|ms1
operator|.
name|getClusterStatusTracker
argument_list|()
decl_stmt|;
name|clusterStatusTracker
operator|.
name|setClusterUp
argument_list|()
expr_stmt|;
name|activeMasterManager
operator|.
name|blockUntilBecomingActiveMaster
argument_list|(
literal|100
argument_list|,
name|Mockito
operator|.
name|mock
argument_list|(
name|MonitoredTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|activeMasterManager
operator|.
name|clusterHasActiveMaster
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertMaster
argument_list|(
name|zk
argument_list|,
name|firstMasterAddress
argument_list|)
expr_stmt|;
comment|// New manager will now try to become the active master in another thread
name|WaitToBeMasterThread
name|t
init|=
operator|new
name|WaitToBeMasterThread
argument_list|(
name|zk
argument_list|,
name|secondMasterAddress
argument_list|)
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// Wait for this guy to figure out there is another active master
comment|// Wait for 1 second at most
name|int
name|sleeps
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|!
name|t
operator|.
name|manager
operator|.
name|clusterHasActiveMaster
operator|.
name|get
argument_list|()
operator|&&
name|sleeps
operator|<
literal|100
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|sleeps
operator|++
expr_stmt|;
block|}
comment|// Both should see that there is an active master
name|assertTrue
argument_list|(
name|activeMasterManager
operator|.
name|clusterHasActiveMaster
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|t
operator|.
name|manager
operator|.
name|clusterHasActiveMaster
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// But secondary one should not be the active master
name|assertFalse
argument_list|(
name|t
operator|.
name|isActiveMaster
argument_list|)
expr_stmt|;
comment|// Close the first server and delete it's master node
name|ms1
operator|.
name|stop
argument_list|(
literal|"stopping first server"
argument_list|)
expr_stmt|;
comment|// Use a listener to capture when the node is actually deleted
name|NodeDeletionListener
name|listener
init|=
operator|new
name|NodeDeletionListener
argument_list|(
name|zk
argument_list|,
name|zk
operator|.
name|znodePaths
operator|.
name|masterAddressZNode
argument_list|)
decl_stmt|;
name|zk
operator|.
name|registerListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleting master node"
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|zk
argument_list|,
name|zk
operator|.
name|znodePaths
operator|.
name|masterAddressZNode
argument_list|)
expr_stmt|;
comment|// Wait for the node to be deleted
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for active master manager to be notified"
argument_list|)
expr_stmt|;
name|listener
operator|.
name|waitForDeletion
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Master node deleted"
argument_list|)
expr_stmt|;
comment|// Now we expect the secondary manager to have and be the active master
comment|// Wait for 1 second at most
name|sleeps
operator|=
literal|0
expr_stmt|;
while|while
condition|(
operator|!
name|t
operator|.
name|isActiveMaster
operator|&&
name|sleeps
operator|<
literal|100
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|sleeps
operator|++
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Slept "
operator|+
name|sleeps
operator|+
literal|" times"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|t
operator|.
name|manager
operator|.
name|clusterHasActiveMaster
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|t
operator|.
name|isActiveMaster
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleting master node"
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|zk
argument_list|,
name|zk
operator|.
name|znodePaths
operator|.
name|masterAddressZNode
argument_list|)
expr_stmt|;
block|}
comment|/**    * Assert there is an active master and that it has the specified address.    * @param zk    * @param thisMasterAddress    * @throws KeeperException    * @throws IOException    */
specifier|private
name|void
name|assertMaster
parameter_list|(
name|ZKWatcher
name|zk
parameter_list|,
name|ServerName
name|expectedAddress
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|IOException
block|{
name|ServerName
name|readAddress
init|=
name|MasterAddressTracker
operator|.
name|getMasterAddress
argument_list|(
name|zk
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|readAddress
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|expectedAddress
operator|.
name|equals
argument_list|(
name|readAddress
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|WaitToBeMasterThread
extends|extends
name|Thread
block|{
name|ActiveMasterManager
name|manager
decl_stmt|;
name|DummyMaster
name|dummyMaster
decl_stmt|;
name|boolean
name|isActiveMaster
decl_stmt|;
specifier|public
name|WaitToBeMasterThread
parameter_list|(
name|ZKWatcher
name|zk
parameter_list|,
name|ServerName
name|address
parameter_list|)
block|{
name|this
operator|.
name|dummyMaster
operator|=
operator|new
name|DummyMaster
argument_list|(
name|zk
argument_list|,
name|address
argument_list|)
expr_stmt|;
name|this
operator|.
name|manager
operator|=
name|this
operator|.
name|dummyMaster
operator|.
name|getActiveMasterManager
argument_list|()
expr_stmt|;
name|isActiveMaster
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|manager
operator|.
name|blockUntilBecomingActiveMaster
argument_list|(
literal|100
argument_list|,
name|Mockito
operator|.
name|mock
argument_list|(
name|MonitoredTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Second master has become the active master!"
argument_list|)
expr_stmt|;
name|isActiveMaster
operator|=
literal|true
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|NodeDeletionListener
extends|extends
name|ZKListener
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
name|NodeDeletionListener
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Semaphore
name|lock
decl_stmt|;
specifier|private
name|String
name|node
decl_stmt|;
specifier|public
name|NodeDeletionListener
parameter_list|(
name|ZKWatcher
name|watcher
parameter_list|,
name|String
name|node
parameter_list|)
block|{
name|super
argument_list|(
name|watcher
argument_list|)
expr_stmt|;
name|lock
operator|=
operator|new
name|Semaphore
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|node
operator|=
name|node
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeDeleted
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|path
operator|.
name|equals
argument_list|(
name|node
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"nodeDeleted("
operator|+
name|path
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|lock
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|waitForDeletion
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|lock
operator|.
name|acquire
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Dummy Master Implementation.    */
specifier|public
specifier|static
class|class
name|DummyMaster
implements|implements
name|Server
block|{
specifier|private
specifier|volatile
name|boolean
name|stopped
decl_stmt|;
specifier|private
name|ClusterStatusTracker
name|clusterStatusTracker
decl_stmt|;
specifier|private
name|ActiveMasterManager
name|activeMasterManager
decl_stmt|;
specifier|public
name|DummyMaster
parameter_list|(
name|ZKWatcher
name|zk
parameter_list|,
name|ServerName
name|master
parameter_list|)
block|{
name|this
operator|.
name|clusterStatusTracker
operator|=
operator|new
name|ClusterStatusTracker
argument_list|(
name|zk
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|clusterStatusTracker
operator|.
name|start
argument_list|()
expr_stmt|;
name|this
operator|.
name|activeMasterManager
operator|=
operator|new
name|ActiveMasterManager
argument_list|(
name|zk
argument_list|,
name|master
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|zk
operator|.
name|registerListener
argument_list|(
name|activeMasterManager
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
specifier|final
name|String
name|msg
parameter_list|,
specifier|final
name|Throwable
name|t
parameter_list|)
block|{}
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
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ZKWatcher
name|getZooKeeper
parameter_list|()
block|{
return|return
literal|null
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
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
literal|null
return|;
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
name|stopped
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
name|stopped
operator|=
literal|true
expr_stmt|;
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
specifier|public
name|ClusterStatusTracker
name|getClusterStatusTracker
parameter_list|()
block|{
return|return
name|clusterStatusTracker
return|;
block|}
specifier|public
name|ActiveMasterManager
name|getActiveMasterManager
parameter_list|()
block|{
return|return
name|activeMasterManager
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
annotation|@
name|Override
specifier|public
name|ClusterConnection
name|getClusterConnection
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileSystem
name|getFileSystem
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopping
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|Connection
name|createConnection
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

