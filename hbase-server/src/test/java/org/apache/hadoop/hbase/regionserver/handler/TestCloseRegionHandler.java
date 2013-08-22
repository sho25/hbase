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
operator|.
name|handler
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
name|HTableDescriptor
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
name|RegionTransition
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
name|exceptions
operator|.
name|DeserializationException
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
name|RegionServerServices
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
name|KeeperException
operator|.
name|NodeExistsException
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
comment|/**  * Test of the {@link CloseRegionHandler}.  */
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
name|TestCloseRegionHandler
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestCloseRegionHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|HTU
init|=
name|HBaseTestingUtility
operator|.
name|createLocalHTU
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HTableDescriptor
name|TEST_HTD
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestCloseRegionHandler"
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
name|HRegionInfo
name|TEST_HRI
decl_stmt|;
specifier|private
name|int
name|testIndex
init|=
literal|0
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|before
parameter_list|()
throws|throws
name|Exception
block|{
name|HTU
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
name|after
parameter_list|()
throws|throws
name|IOException
block|{
name|HTU
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Before each test, use a different HRI, so the different tests    * don't interfere with each other. This allows us to use just    * a single ZK cluster for the whole suite.    */
annotation|@
name|Before
specifier|public
name|void
name|setupHRI
parameter_list|()
block|{
name|TEST_HRI
operator|=
operator|new
name|HRegionInfo
argument_list|(
name|TEST_HTD
operator|.
name|getTableName
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
expr_stmt|;
name|testIndex
operator|++
expr_stmt|;
block|}
comment|/**    * Test that if we fail a flush, abort gets set on close.    * @see<a href="https://issues.apache.org/jira/browse/HBASE-4270">HBASE-4270</a>    * @throws IOException    * @throws NodeExistsException    * @throws KeeperException    */
annotation|@
name|Test
specifier|public
name|void
name|testFailedFlushAborts
parameter_list|()
throws|throws
name|IOException
throws|,
name|NodeExistsException
throws|,
name|KeeperException
block|{
specifier|final
name|Server
name|server
init|=
operator|new
name|MockServer
argument_list|(
name|HTU
argument_list|,
literal|false
argument_list|)
decl_stmt|;
specifier|final
name|RegionServerServices
name|rss
init|=
name|HTU
operator|.
name|createMockRegionServerService
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
name|TEST_HTD
decl_stmt|;
specifier|final
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|HTU
operator|.
name|createLocalHRegion
argument_list|(
name|hri
argument_list|,
name|htd
argument_list|)
decl_stmt|;
try|try
block|{
name|assertNotNull
argument_list|(
name|region
argument_list|)
expr_stmt|;
comment|// Spy on the region so can throw exception when close is called.
name|HRegion
name|spy
init|=
name|Mockito
operator|.
name|spy
argument_list|(
name|region
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|abort
init|=
literal|false
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|spy
operator|.
name|close
argument_list|(
name|abort
argument_list|)
argument_list|)
operator|.
name|thenThrow
argument_list|(
operator|new
name|RuntimeException
argument_list|(
literal|"Mocked failed close!"
argument_list|)
argument_list|)
expr_stmt|;
comment|// The CloseRegionHandler will try to get an HRegion that corresponds
comment|// to the passed hri -- so insert the region into the online region Set.
name|rss
operator|.
name|addToOnlineRegions
argument_list|(
name|spy
argument_list|)
expr_stmt|;
comment|// Assert the Server is NOT stopped before we call close region.
name|assertFalse
argument_list|(
name|server
operator|.
name|isStopped
argument_list|()
argument_list|)
expr_stmt|;
name|CloseRegionHandler
name|handler
init|=
operator|new
name|CloseRegionHandler
argument_list|(
name|server
argument_list|,
name|rss
argument_list|,
name|hri
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
name|boolean
name|throwable
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
name|Throwable
name|t
parameter_list|)
block|{
name|throwable
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
name|assertTrue
argument_list|(
name|throwable
argument_list|)
expr_stmt|;
comment|// Abort calls stop so stopped flag should be set.
name|assertTrue
argument_list|(
name|server
operator|.
name|isStopped
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|HRegion
operator|.
name|closeHRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**       * Test if close region can handle ZK closing node version mismatch       * @throws IOException       * @throws NodeExistsException       * @throws KeeperException      * @throws DeserializationException        */
annotation|@
name|Test
specifier|public
name|void
name|testZKClosingNodeVersionMismatch
parameter_list|()
throws|throws
name|IOException
throws|,
name|NodeExistsException
throws|,
name|KeeperException
throws|,
name|DeserializationException
block|{
specifier|final
name|Server
name|server
init|=
operator|new
name|MockServer
argument_list|(
name|HTU
argument_list|)
decl_stmt|;
specifier|final
name|RegionServerServices
name|rss
init|=
name|HTU
operator|.
name|createMockRegionServerService
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
name|TEST_HTD
decl_stmt|;
specifier|final
name|HRegionInfo
name|hri
init|=
name|TEST_HRI
decl_stmt|;
comment|// open a region first so that it can be closed later
name|OpenRegion
argument_list|(
name|server
argument_list|,
name|rss
argument_list|,
name|htd
argument_list|,
name|hri
argument_list|)
expr_stmt|;
comment|// close the region
comment|// Create it CLOSING, which is what Master set before sending CLOSE RPC
name|int
name|versionOfClosingNode
init|=
name|ZKAssign
operator|.
name|createNodeClosing
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|hri
argument_list|,
name|server
operator|.
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
comment|// The CloseRegionHandler will validate the expected version
comment|// Given it is set to invalid versionOfClosingNode+1,
comment|// CloseRegionHandler should be M_ZK_REGION_CLOSING
name|CloseRegionHandler
name|handler
init|=
operator|new
name|CloseRegionHandler
argument_list|(
name|server
argument_list|,
name|rss
argument_list|,
name|hri
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
name|versionOfClosingNode
operator|+
literal|1
argument_list|)
decl_stmt|;
name|handler
operator|.
name|process
argument_list|()
expr_stmt|;
comment|// Handler should remain in M_ZK_REGION_CLOSING
name|RegionTransition
name|rt
init|=
name|RegionTransition
operator|.
name|parseFrom
argument_list|(
name|ZKAssign
operator|.
name|getData
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|rt
operator|.
name|getEventType
argument_list|()
operator|.
name|equals
argument_list|(
name|EventType
operator|.
name|M_ZK_REGION_CLOSING
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**       * Test if the region can be closed properly       * @throws IOException       * @throws NodeExistsException       * @throws KeeperException      * @throws org.apache.hadoop.hbase.exceptions.DeserializationException       */
annotation|@
name|Test
specifier|public
name|void
name|testCloseRegion
parameter_list|()
throws|throws
name|IOException
throws|,
name|NodeExistsException
throws|,
name|KeeperException
throws|,
name|DeserializationException
block|{
specifier|final
name|Server
name|server
init|=
operator|new
name|MockServer
argument_list|(
name|HTU
argument_list|)
decl_stmt|;
specifier|final
name|RegionServerServices
name|rss
init|=
name|HTU
operator|.
name|createMockRegionServerService
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
name|TEST_HTD
decl_stmt|;
name|HRegionInfo
name|hri
init|=
name|TEST_HRI
decl_stmt|;
comment|// open a region first so that it can be closed later
name|OpenRegion
argument_list|(
name|server
argument_list|,
name|rss
argument_list|,
name|htd
argument_list|,
name|hri
argument_list|)
expr_stmt|;
comment|// close the region
comment|// Create it CLOSING, which is what Master set before sending CLOSE RPC
name|int
name|versionOfClosingNode
init|=
name|ZKAssign
operator|.
name|createNodeClosing
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|hri
argument_list|,
name|server
operator|.
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
comment|// The CloseRegionHandler will validate the expected version
comment|// Given it is set to correct versionOfClosingNode,
comment|// CloseRegionHandlerit should be RS_ZK_REGION_CLOSED
name|CloseRegionHandler
name|handler
init|=
operator|new
name|CloseRegionHandler
argument_list|(
name|server
argument_list|,
name|rss
argument_list|,
name|hri
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
name|versionOfClosingNode
argument_list|)
decl_stmt|;
name|handler
operator|.
name|process
argument_list|()
expr_stmt|;
comment|// Handler should have transitioned it to RS_ZK_REGION_CLOSED
name|RegionTransition
name|rt
init|=
name|RegionTransition
operator|.
name|parseFrom
argument_list|(
name|ZKAssign
operator|.
name|getData
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|rt
operator|.
name|getEventType
argument_list|()
operator|.
name|equals
argument_list|(
name|EventType
operator|.
name|RS_ZK_REGION_CLOSED
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|OpenRegion
parameter_list|(
name|Server
name|server
parameter_list|,
name|RegionServerServices
name|rss
parameter_list|,
name|HTableDescriptor
name|htd
parameter_list|,
name|HRegionInfo
name|hri
parameter_list|)
throws|throws
name|IOException
throws|,
name|NodeExistsException
throws|,
name|KeeperException
throws|,
name|DeserializationException
block|{
comment|// Create it OFFLINE node, which is what Master set before sending OPEN RPC
name|ZKAssign
operator|.
name|createNodeOffline
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|hri
argument_list|,
name|server
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|OpenRegionHandler
name|openHandler
init|=
operator|new
name|OpenRegionHandler
argument_list|(
name|server
argument_list|,
name|rss
argument_list|,
name|hri
argument_list|,
name|htd
argument_list|)
decl_stmt|;
name|rss
operator|.
name|getRegionsInTransitionInRS
argument_list|()
operator|.
name|put
argument_list|(
name|hri
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|Boolean
operator|.
name|TRUE
argument_list|)
expr_stmt|;
name|openHandler
operator|.
name|process
argument_list|()
expr_stmt|;
comment|// This parse is not used?
name|RegionTransition
operator|.
name|parseFrom
argument_list|(
name|ZKAssign
operator|.
name|getData
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// delete the node, which is what Master do after the region is opened
name|ZKAssign
operator|.
name|deleteNode
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|EventType
operator|.
name|RS_ZK_REGION_OPENED
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

