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
name|assertNull
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
name|TestMasterAddressTracker
block|{
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
name|TestMasterAddressTracker
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TestMasterAddressTracker
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
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
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
name|testDeleteIfEquals
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|ServerName
name|sn
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"localhost"
argument_list|,
literal|1234
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|MasterAddressTracker
name|addressTracker
init|=
name|setupMasterTracker
argument_list|(
name|sn
argument_list|,
literal|1772
argument_list|)
decl_stmt|;
try|try
block|{
name|assertFalse
argument_list|(
literal|"shouldn't have deleted wrong master server."
argument_list|,
name|MasterAddressTracker
operator|.
name|deleteIfEquals
argument_list|(
name|addressTracker
operator|.
name|getWatcher
argument_list|()
argument_list|,
literal|"some other string."
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|assertTrue
argument_list|(
literal|"Couldn't clean up master"
argument_list|,
name|MasterAddressTracker
operator|.
name|deleteIfEquals
argument_list|(
name|addressTracker
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|sn
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * create an address tracker instance    * @param sn if not-null set the active master    * @param infoPort if there is an active master, set its info port.    */
specifier|private
name|MasterAddressTracker
name|setupMasterTracker
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|,
specifier|final
name|int
name|infoPort
parameter_list|)
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
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|createAndFailSilent
argument_list|(
name|zk
argument_list|,
name|zk
operator|.
name|getZNodePaths
argument_list|()
operator|.
name|baseZNode
argument_list|)
expr_stmt|;
comment|// Should not have a master yet
name|MasterAddressTracker
name|addressTracker
init|=
operator|new
name|MasterAddressTracker
argument_list|(
name|zk
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|addressTracker
operator|.
name|start
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|addressTracker
operator|.
name|hasMaster
argument_list|()
argument_list|)
expr_stmt|;
name|zk
operator|.
name|registerListener
argument_list|(
name|addressTracker
argument_list|)
expr_stmt|;
comment|// Use a listener to capture when the node is actually created
name|NodeCreationListener
name|listener
init|=
operator|new
name|NodeCreationListener
argument_list|(
name|zk
argument_list|,
name|zk
operator|.
name|getZNodePaths
argument_list|()
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
if|if
condition|(
name|sn
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating master node"
argument_list|)
expr_stmt|;
name|MasterAddressTracker
operator|.
name|setMasterAddress
argument_list|(
name|zk
argument_list|,
name|zk
operator|.
name|getZNodePaths
argument_list|()
operator|.
name|masterAddressZNode
argument_list|,
name|sn
argument_list|,
name|infoPort
argument_list|)
expr_stmt|;
comment|// Wait for the node to be created
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for master address manager to be notified"
argument_list|)
expr_stmt|;
name|listener
operator|.
name|waitForCreation
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Master node created"
argument_list|)
expr_stmt|;
block|}
return|return
name|addressTracker
return|;
block|}
comment|/**    * Unit tests that uses ZooKeeper but does not use the master-side methods    * but rather acts directly on ZK.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testMasterAddressTrackerFromZK
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Create the master node with a dummy address
specifier|final
name|int
name|infoPort
init|=
literal|1235
decl_stmt|;
specifier|final
name|ServerName
name|sn
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"localhost"
argument_list|,
literal|1234
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|MasterAddressTracker
name|addressTracker
init|=
name|setupMasterTracker
argument_list|(
name|sn
argument_list|,
name|infoPort
argument_list|)
decl_stmt|;
try|try
block|{
name|assertTrue
argument_list|(
name|addressTracker
operator|.
name|hasMaster
argument_list|()
argument_list|)
expr_stmt|;
name|ServerName
name|pulledAddress
init|=
name|addressTracker
operator|.
name|getMasterAddress
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|pulledAddress
operator|.
name|equals
argument_list|(
name|sn
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|infoPort
argument_list|,
name|addressTracker
operator|.
name|getMasterInfoPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|assertTrue
argument_list|(
literal|"Couldn't clean up master"
argument_list|,
name|MasterAddressTracker
operator|.
name|deleteIfEquals
argument_list|(
name|addressTracker
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|sn
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParsingNull
parameter_list|()
throws|throws
name|Exception
block|{
name|assertNull
argument_list|(
literal|"parse on null data should return null."
argument_list|,
name|MasterAddressTracker
operator|.
name|parse
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoBackups
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|ServerName
name|sn
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"localhost"
argument_list|,
literal|1234
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|MasterAddressTracker
name|addressTracker
init|=
name|setupMasterTracker
argument_list|(
name|sn
argument_list|,
literal|1772
argument_list|)
decl_stmt|;
try|try
block|{
name|assertEquals
argument_list|(
literal|"Should receive 0 for backup not found."
argument_list|,
literal|0
argument_list|,
name|addressTracker
operator|.
name|getBackupMasterInfoPort
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"doesnotexist.example.com"
argument_list|,
literal|1234
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|assertTrue
argument_list|(
literal|"Couldn't clean up master"
argument_list|,
name|MasterAddressTracker
operator|.
name|deleteIfEquals
argument_list|(
name|addressTracker
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|sn
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoMaster
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|MasterAddressTracker
name|addressTracker
init|=
name|setupMasterTracker
argument_list|(
literal|null
argument_list|,
literal|1772
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|addressTracker
operator|.
name|hasMaster
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
literal|"should get null master when none active."
argument_list|,
name|addressTracker
operator|.
name|getMasterAddress
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Should receive 0 for backup not found."
argument_list|,
literal|0
argument_list|,
name|addressTracker
operator|.
name|getMasterInfoPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|NodeCreationListener
extends|extends
name|ZKListener
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
name|NodeCreationListener
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
name|NodeCreationListener
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
name|nodeCreated
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
literal|"nodeCreated("
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
name|waitForCreation
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
block|}
end_class

end_unit

