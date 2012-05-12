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
package|;
end_package

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
name|ArrayList
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
name|List
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|client
operator|.
name|HBaseAdmin
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
name|FSTableDescriptors
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

begin_comment
comment|/**  * Test the draining servers feature.  * @see<a href="https://issues.apache.org/jira/browse/HBASE-4298">HBASE-4298</a>  */
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
name|TestDrainingServer
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
name|TestDrainingServer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TABLENAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"t"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|COUNT_OF_REGIONS
init|=
name|HBaseTestingUtility
operator|.
name|KEYS
operator|.
name|length
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NB_SLAVES
init|=
literal|5
decl_stmt|;
comment|/**    * Spin up a cluster with a bunch of regions on it.    */
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
name|startMiniCluster
argument_list|(
name|NB_SLAVES
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hbase.master.enabletable.roundrobin"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ZooKeeperWatcher
name|zkw
init|=
name|HBaseTestingUtility
operator|.
name|getZooKeeperWatcher
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
name|TABLENAME
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|createMultiRegionsInMeta
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|htd
argument_list|,
name|HBaseTestingUtility
operator|.
name|KEYS
argument_list|)
expr_stmt|;
comment|// Make a mark for the table in the filesystem.
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|FSTableDescriptors
operator|.
name|createTableDescriptor
argument_list|(
name|fs
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|htd
argument_list|)
expr_stmt|;
comment|// Assign out the regions we just created.
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
decl_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLENAME
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|TABLENAME
argument_list|)
expr_stmt|;
name|boolean
name|ready
init|=
literal|false
decl_stmt|;
while|while
condition|(
operator|!
name|ready
condition|)
block|{
name|ZKAssign
operator|.
name|blockUntilNoRIT
argument_list|(
name|zkw
argument_list|)
expr_stmt|;
comment|// Assert that every regionserver has some regions on it.
name|int
name|i
init|=
literal|0
decl_stmt|;
name|ready
operator|=
literal|true
expr_stmt|;
while|while
condition|(
name|i
operator|<
name|NB_SLAVES
operator|&&
name|ready
condition|)
block|{
name|HRegionServer
name|hrs
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|ProtobufUtil
operator|.
name|getOnlineRegions
argument_list|(
name|hrs
argument_list|)
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ready
operator|=
literal|false
expr_stmt|;
block|}
name|i
operator|++
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|ready
condition|)
block|{
name|admin
operator|.
name|balancer
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
name|HRegionServer
name|setDrainingServer
parameter_list|(
specifier|final
name|HRegionServer
name|hrs
parameter_list|)
throws|throws
name|KeeperException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Making "
operator|+
name|hrs
operator|.
name|getServerName
argument_list|()
operator|+
literal|" the draining server; "
operator|+
literal|"it has "
operator|+
name|hrs
operator|.
name|getNumberOfOnlineRegions
argument_list|()
operator|+
literal|" online regions"
argument_list|)
expr_stmt|;
name|ZooKeeperWatcher
name|zkw
init|=
name|hrs
operator|.
name|getZooKeeper
argument_list|()
decl_stmt|;
name|String
name|hrsDrainingZnode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw
operator|.
name|drainingZNode
argument_list|,
name|hrs
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkw
argument_list|,
name|hrsDrainingZnode
argument_list|)
expr_stmt|;
return|return
name|hrs
return|;
block|}
specifier|private
specifier|static
name|HRegionServer
name|unsetDrainingServer
parameter_list|(
specifier|final
name|HRegionServer
name|hrs
parameter_list|)
throws|throws
name|KeeperException
block|{
name|ZooKeeperWatcher
name|zkw
init|=
name|hrs
operator|.
name|getZooKeeper
argument_list|()
decl_stmt|;
name|String
name|hrsDrainingZnode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw
operator|.
name|drainingZNode
argument_list|,
name|hrs
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|zkw
argument_list|,
name|hrsDrainingZnode
argument_list|)
expr_stmt|;
return|return
name|hrs
return|;
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
comment|/**    * Test adding server to draining servers and then move regions off it.    * Make sure that no regions are moved back to the draining server.    * @throws IOException     * @throws KeeperException     */
annotation|@
name|Test
comment|// (timeout=30000)
specifier|public
name|void
name|testDrainingServerOffloading
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
block|{
comment|// I need master in the below.
name|HMaster
name|master
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|HRegionInfo
name|hriToMoveBack
init|=
literal|null
decl_stmt|;
comment|// Set first server as draining server.
name|HRegionServer
name|drainingServer
init|=
name|setDrainingServer
argument_list|(
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
specifier|final
name|int
name|regionsOnDrainingServer
init|=
name|drainingServer
operator|.
name|getNumberOfOnlineRegions
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|regionsOnDrainingServer
operator|>
literal|0
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|hris
init|=
name|ProtobufUtil
operator|.
name|getOnlineRegions
argument_list|(
name|drainingServer
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|hris
control|)
block|{
comment|// Pass null and AssignmentManager will chose a random server BUT it
comment|// should exclude draining servers.
name|master
operator|.
name|move
argument_list|(
name|hri
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Save off region to move back.
name|hriToMoveBack
operator|=
name|hri
expr_stmt|;
block|}
comment|// Wait for regions to come back on line again.
name|waitForAllRegionsOnline
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|drainingServer
operator|.
name|getNumberOfOnlineRegions
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|unsetDrainingServer
argument_list|(
name|drainingServer
argument_list|)
expr_stmt|;
block|}
comment|// Now we've unset the draining server, we should be able to move a region
comment|// to what was the draining server.
name|master
operator|.
name|move
argument_list|(
name|hriToMoveBack
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|drainingServer
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Wait for regions to come back on line again.
name|waitForAllRegionsOnline
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|drainingServer
operator|.
name|getNumberOfOnlineRegions
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that draining servers are ignored even after killing regionserver(s).    * Verify that the draining server is not given any of the dead servers regions.    * @throws KeeperException    * @throws IOException    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testDrainingServerWithAbort
parameter_list|()
throws|throws
name|KeeperException
throws|,
name|Exception
block|{
comment|// Ensure a stable env
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|balanceSwitch
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|waitForAllRegionsOnline
argument_list|()
expr_stmt|;
specifier|final
name|long
name|regionCount
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|countServedRegions
argument_list|()
decl_stmt|;
comment|// Let's get a copy of the regions today.
name|Collection
argument_list|<
name|HRegion
argument_list|>
name|regions
init|=
operator|new
name|ArrayList
argument_list|<
name|HRegion
argument_list|>
argument_list|()
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
name|NB_SLAVES
condition|;
name|i
operator|++
control|)
block|{
name|HRegionServer
name|hrs
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|regions
operator|.
name|addAll
argument_list|(
name|hrs
operator|.
name|getCopyOfOnlineRegionsSortedBySize
argument_list|()
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Choose the draining server
name|HRegionServer
name|drainingServer
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|int
name|regionsOnDrainingServer
init|=
name|drainingServer
operator|.
name|getNumberOfOnlineRegions
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|regionsOnDrainingServer
operator|>
literal|0
argument_list|)
expr_stmt|;
name|ServerManager
name|sm
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerManager
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|HRegion
argument_list|>
name|regionsBefore
init|=
name|drainingServer
operator|.
name|getCopyOfOnlineRegionsSortedBySize
argument_list|()
operator|.
name|values
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Regions of drained server are: "
operator|+
name|regionsBefore
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Add first server to draining servers up in zk.
name|setDrainingServer
argument_list|(
name|drainingServer
argument_list|)
expr_stmt|;
comment|//wait for the master to receive and manage the event
while|while
condition|(
name|sm
operator|.
name|createDestinationServersList
argument_list|()
operator|.
name|contains
argument_list|(
name|drainingServer
operator|.
name|getServerName
argument_list|()
argument_list|)
condition|)
empty_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"The available servers are: "
operator|+
name|sm
operator|.
name|createDestinationServersList
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Nothing should have happened here."
argument_list|,
name|regionsOnDrainingServer
argument_list|,
name|drainingServer
operator|.
name|getNumberOfOnlineRegions
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"We should not have regions in transition here."
argument_list|,
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionsInTransition
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// Kill a few regionservers.
for|for
control|(
name|int
name|aborted
init|=
literal|0
init|;
name|aborted
operator|<=
literal|2
condition|;
name|aborted
operator|++
control|)
block|{
name|HRegionServer
name|hrs
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
name|aborted
operator|+
literal|1
argument_list|)
decl_stmt|;
name|hrs
operator|.
name|abort
argument_list|(
literal|"Aborting"
argument_list|)
expr_stmt|;
block|}
comment|// Wait for regions to come back online again.
name|waitForAllRegionsOnline
argument_list|()
expr_stmt|;
name|Collection
argument_list|<
name|HRegion
argument_list|>
name|regionsAfter
init|=
name|drainingServer
operator|.
name|getCopyOfOnlineRegionsSortedBySize
argument_list|()
operator|.
name|values
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Regions of drained server are: "
operator|+
name|regionsAfter
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Test conditions are not met: regions were"
operator|+
literal|" created/deleted during the test. "
argument_list|,
name|regionCount
argument_list|,
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|countServedRegions
argument_list|()
argument_list|)
expr_stmt|;
comment|// Assert the draining server still has the same regions.
name|StringBuilder
name|result
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegion
name|r
range|:
name|regionsAfter
control|)
block|{
if|if
condition|(
operator|!
name|regionsBefore
operator|.
name|contains
argument_list|(
name|r
argument_list|)
condition|)
block|{
name|result
operator|.
name|append
argument_list|(
name|r
argument_list|)
operator|.
name|append
argument_list|(
literal|" was added after the drain"
argument_list|)
expr_stmt|;
if|if
condition|(
name|regions
operator|.
name|contains
argument_list|(
name|r
argument_list|)
condition|)
block|{
name|result
operator|.
name|append
argument_list|(
literal|"(existing region"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|result
operator|.
name|append
argument_list|(
literal|"(new region)"
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|append
argument_list|(
literal|"; "
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|HRegion
name|r
range|:
name|regionsBefore
control|)
block|{
if|if
condition|(
operator|!
name|regionsAfter
operator|.
name|contains
argument_list|(
name|r
argument_list|)
condition|)
block|{
name|result
operator|.
name|append
argument_list|(
name|r
argument_list|)
operator|.
name|append
argument_list|(
literal|" was removed after the drain; "
argument_list|)
expr_stmt|;
block|}
block|}
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Errors are: "
operator|+
name|result
operator|.
name|toString
argument_list|()
argument_list|,
name|result
operator|.
name|length
argument_list|()
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|unsetDrainingServer
argument_list|(
name|drainingServer
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|waitForAllRegionsOnline
parameter_list|()
block|{
comment|// Wait for regions to come back on line again.
while|while
condition|(
operator|!
name|isAllRegionsOnline
argument_list|()
condition|)
block|{     }
while|while
condition|(
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|isRegionsInTransition
argument_list|()
condition|)
block|{     }
block|}
specifier|private
name|boolean
name|isAllRegionsOnline
parameter_list|()
block|{
return|return
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|countServedRegions
argument_list|()
operator|==
operator|(
name|COUNT_OF_REGIONS
operator|+
literal|2
comment|/*catalog regions*/
operator|)
return|;
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

