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
name|client
operator|.
name|HTable
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
name|Put
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
name|NotServingRegionException
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
name|RegionAlreadyInTransitionException
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
name|protobuf
operator|.
name|RequestConverter
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
name|AdminProtos
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
name|handler
operator|.
name|OpenRegionHandler
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
name|ZKAssign
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
name|Assert
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
comment|/**  * Tests on the region server, without the master.  */
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
name|TestRegionServerNoMaster
block|{
specifier|private
specifier|static
specifier|final
name|int
name|NB_SERVERS
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
name|HTable
name|table
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|row
init|=
literal|"ee"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|HRegionInfo
name|hri
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|regionName
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|HTU
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
name|before
parameter_list|()
throws|throws
name|Exception
block|{
name|HTU
operator|.
name|startMiniCluster
argument_list|(
name|NB_SERVERS
argument_list|)
expr_stmt|;
specifier|final
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|TestRegionServerNoMaster
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// Create table then get the single region for our new table.
name|table
operator|=
name|HTU
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|row
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|hri
operator|=
name|table
operator|.
name|getRegionLocation
argument_list|(
name|row
argument_list|,
literal|false
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
expr_stmt|;
name|regionName
operator|=
name|hri
operator|.
name|getRegionName
argument_list|()
expr_stmt|;
comment|// No master
name|HTU
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|stopMaster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|HTU
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|after
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Clean the state if the test failed before cleaning the znode
comment|// It does not manage all bad failures, so if there are multiple failures, only
comment|//  the first one should be looked at.
name|ZKAssign
operator|.
name|deleteNodeFailSilent
argument_list|(
name|HTU
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|hri
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|HRegionServer
name|getRS
parameter_list|()
block|{
return|return
name|HTU
operator|.
name|getHBaseCluster
argument_list|()
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
return|;
block|}
comment|/**    * Reopen the region. Reused in multiple tests as we always leave the region open after a test.    */
specifier|private
name|void
name|reopenRegion
parameter_list|()
throws|throws
name|Exception
block|{
comment|// We reopen. We need a ZK node here, as a open is always triggered by a master.
name|ZKAssign
operator|.
name|createNodeOffline
argument_list|(
name|HTU
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|hri
argument_list|,
name|getRS
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
comment|// first version is '0'
name|AdminProtos
operator|.
name|OpenRegionRequest
name|orr
init|=
name|RequestConverter
operator|.
name|buildOpenRegionRequest
argument_list|(
name|hri
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|AdminProtos
operator|.
name|OpenRegionResponse
name|responseOpen
init|=
name|getRS
argument_list|()
operator|.
name|openRegion
argument_list|(
literal|null
argument_list|,
name|orr
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|responseOpen
operator|.
name|getOpeningStateCount
argument_list|()
operator|==
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|responseOpen
operator|.
name|getOpeningState
argument_list|(
literal|0
argument_list|)
operator|.
name|equals
argument_list|(
name|AdminProtos
operator|.
name|OpenRegionResponse
operator|.
name|RegionOpeningState
operator|.
name|OPENED
argument_list|)
argument_list|)
expr_stmt|;
name|checkRegionIsOpened
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|checkRegionIsOpened
parameter_list|()
throws|throws
name|Exception
block|{
while|while
condition|(
operator|!
name|getRS
argument_list|()
operator|.
name|getRegionsInTransitionInRS
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertTrue
argument_list|(
name|getRS
argument_list|()
operator|.
name|getRegion
argument_list|(
name|regionName
argument_list|)
operator|.
name|isAvailable
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ZKAssign
operator|.
name|deleteOpenedNode
argument_list|(
name|HTU
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkRegionIsClosed
parameter_list|()
throws|throws
name|Exception
block|{
while|while
condition|(
operator|!
name|getRS
argument_list|()
operator|.
name|getRegionsInTransitionInRS
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|Assert
operator|.
name|assertFalse
argument_list|(
name|getRS
argument_list|()
operator|.
name|getRegion
argument_list|(
name|regionName
argument_list|)
operator|.
name|isAvailable
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NotServingRegionException
name|expected
parameter_list|)
block|{
comment|// That's how it work: if the region is closed we have an exception.
block|}
comment|// We don't delete the znode here, because there is not always a znode.
block|}
comment|/**    * Close the region without using ZK    */
specifier|private
name|void
name|closeNoZK
parameter_list|()
throws|throws
name|Exception
block|{
comment|// no transition in ZK
name|AdminProtos
operator|.
name|CloseRegionRequest
name|crr
init|=
name|RequestConverter
operator|.
name|buildCloseRegionRequest
argument_list|(
name|regionName
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|AdminProtos
operator|.
name|CloseRegionResponse
name|responseClose
init|=
name|getRS
argument_list|()
operator|.
name|closeRegion
argument_list|(
literal|null
argument_list|,
name|crr
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|responseClose
operator|.
name|getClosed
argument_list|()
argument_list|)
expr_stmt|;
comment|// now waiting& checking. After a while, the transition should be done and the region closed
name|checkRegionIsClosed
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testCloseByRegionServer
parameter_list|()
throws|throws
name|Exception
block|{
name|closeNoZK
argument_list|()
expr_stmt|;
name|reopenRegion
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testCloseByMasterWithoutZNode
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Transition in ZK on. This should fail, as there is no znode
name|AdminProtos
operator|.
name|CloseRegionRequest
name|crr
init|=
name|RequestConverter
operator|.
name|buildCloseRegionRequest
argument_list|(
name|regionName
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|AdminProtos
operator|.
name|CloseRegionResponse
name|responseClose
init|=
name|getRS
argument_list|()
operator|.
name|closeRegion
argument_list|(
literal|null
argument_list|,
name|crr
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|responseClose
operator|.
name|getClosed
argument_list|()
argument_list|)
expr_stmt|;
comment|// now waiting. After a while, the transition should be done
while|while
condition|(
operator|!
name|getRS
argument_list|()
operator|.
name|getRegionsInTransitionInRS
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// the region is still available, the close got rejected at the end
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"The close should have failed"
argument_list|,
name|getRS
argument_list|()
operator|.
name|getRegion
argument_list|(
name|regionName
argument_list|)
operator|.
name|isAvailable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testOpenCloseByMasterWithZNode
parameter_list|()
throws|throws
name|Exception
block|{
name|ZKAssign
operator|.
name|createNodeClosing
argument_list|(
name|HTU
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|hri
argument_list|,
name|getRS
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|AdminProtos
operator|.
name|CloseRegionRequest
name|crr
init|=
name|RequestConverter
operator|.
name|buildCloseRegionRequest
argument_list|(
name|regionName
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|AdminProtos
operator|.
name|CloseRegionResponse
name|responseClose
init|=
name|getRS
argument_list|()
operator|.
name|closeRegion
argument_list|(
literal|null
argument_list|,
name|crr
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|responseClose
operator|.
name|getClosed
argument_list|()
argument_list|)
expr_stmt|;
name|checkRegionIsClosed
argument_list|()
expr_stmt|;
name|ZKAssign
operator|.
name|deleteClosedNode
argument_list|(
name|HTU
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
name|reopenRegion
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test that we can send multiple openRegion to the region server.    * This is used when:    * - there is a SocketTimeout: in this case, the master does not know if the region server    * received the request before the timeout.    * - We have a socket error during the operation: same stuff: we don't know    * - a master failover: if we find a znode in thz M_ZK_REGION_OFFLINE, we don't know if    * the region server has received the query or not. Only solution to be efficient: re-ask    * immediately.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testMultipleOpen
parameter_list|()
throws|throws
name|Exception
block|{
comment|// We close
name|closeNoZK
argument_list|()
expr_stmt|;
name|checkRegionIsClosed
argument_list|()
expr_stmt|;
comment|// We reopen. We need a ZK node here, as a open is always triggered by a master.
name|ZKAssign
operator|.
name|createNodeOffline
argument_list|(
name|HTU
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|hri
argument_list|,
name|getRS
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
comment|// We're sending multiple requests in a row. The region server must handle this nicely.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|AdminProtos
operator|.
name|OpenRegionRequest
name|orr
init|=
name|RequestConverter
operator|.
name|buildOpenRegionRequest
argument_list|(
name|hri
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|AdminProtos
operator|.
name|OpenRegionResponse
name|responseOpen
init|=
name|getRS
argument_list|()
operator|.
name|openRegion
argument_list|(
literal|null
argument_list|,
name|orr
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|responseOpen
operator|.
name|getOpeningStateCount
argument_list|()
operator|==
literal|1
argument_list|)
expr_stmt|;
name|AdminProtos
operator|.
name|OpenRegionResponse
operator|.
name|RegionOpeningState
name|ors
init|=
name|responseOpen
operator|.
name|getOpeningState
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"request "
operator|+
name|i
operator|+
literal|" failed"
argument_list|,
name|ors
operator|.
name|equals
argument_list|(
name|AdminProtos
operator|.
name|OpenRegionResponse
operator|.
name|RegionOpeningState
operator|.
name|OPENED
argument_list|)
operator|||
name|ors
operator|.
name|equals
argument_list|(
name|AdminProtos
operator|.
name|OpenRegionResponse
operator|.
name|RegionOpeningState
operator|.
name|ALREADY_OPENED
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|checkRegionIsOpened
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOpenClosingRegion
parameter_list|()
throws|throws
name|Exception
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|getRS
argument_list|()
operator|.
name|getRegion
argument_list|(
name|regionName
argument_list|)
operator|.
name|isAvailable
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
comment|// fake region to be closing now, need to clear state afterwards
name|getRS
argument_list|()
operator|.
name|regionsInTransitionInRS
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
name|FALSE
argument_list|)
expr_stmt|;
name|AdminProtos
operator|.
name|OpenRegionRequest
name|orr
init|=
name|RequestConverter
operator|.
name|buildOpenRegionRequest
argument_list|(
name|hri
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|getRS
argument_list|()
operator|.
name|openRegion
argument_list|(
literal|null
argument_list|,
name|orr
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"The closing region should not be opened"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|se
parameter_list|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"The region should be already in transition"
argument_list|,
name|se
operator|.
name|getCause
argument_list|()
operator|instanceof
name|RegionAlreadyInTransitionException
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|getRS
argument_list|()
operator|.
name|regionsInTransitionInRS
operator|.
name|remove
argument_list|(
name|hri
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testMultipleCloseFromMaster
parameter_list|()
throws|throws
name|Exception
block|{
comment|// As opening, we must support multiple requests on the same region
name|ZKAssign
operator|.
name|createNodeClosing
argument_list|(
name|HTU
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|hri
argument_list|,
name|getRS
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|AdminProtos
operator|.
name|CloseRegionRequest
name|crr
init|=
name|RequestConverter
operator|.
name|buildCloseRegionRequest
argument_list|(
name|regionName
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
decl_stmt|;
try|try
block|{
name|AdminProtos
operator|.
name|CloseRegionResponse
name|responseClose
init|=
name|getRS
argument_list|()
operator|.
name|closeRegion
argument_list|(
literal|null
argument_list|,
name|crr
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"The first request should succeeds"
argument_list|,
literal|0
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"request "
operator|+
name|i
operator|+
literal|" failed"
argument_list|,
name|responseClose
operator|.
name|getClosed
argument_list|()
operator|||
name|responseClose
operator|.
name|hasClosed
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|se
parameter_list|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"The next queries should throw an exception."
argument_list|,
name|i
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
name|checkRegionIsClosed
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ZKAssign
operator|.
name|deleteClosedNode
argument_list|(
name|HTU
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|reopenRegion
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test that if we do a close while opening it stops the opening.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testCancelOpeningWithoutZK
parameter_list|()
throws|throws
name|Exception
block|{
comment|// We close
name|closeNoZK
argument_list|()
expr_stmt|;
name|checkRegionIsClosed
argument_list|()
expr_stmt|;
comment|// Let do the initial steps, without having a handler
name|ZKAssign
operator|.
name|createNodeOffline
argument_list|(
name|HTU
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|hri
argument_list|,
name|getRS
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|getRS
argument_list|()
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
comment|// That's a close without ZK.
name|AdminProtos
operator|.
name|CloseRegionRequest
name|crr
init|=
name|RequestConverter
operator|.
name|buildCloseRegionRequest
argument_list|(
name|regionName
argument_list|,
literal|false
argument_list|)
decl_stmt|;
try|try
block|{
name|getRS
argument_list|()
operator|.
name|closeRegion
argument_list|(
literal|null
argument_list|,
name|crr
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|expected
parameter_list|)
block|{     }
comment|// The state in RIT should have changed to close
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Boolean
operator|.
name|FALSE
argument_list|,
name|getRS
argument_list|()
operator|.
name|getRegionsInTransitionInRS
argument_list|()
operator|.
name|get
argument_list|(
name|hri
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Let's start the open handler
name|HTableDescriptor
name|htd
init|=
name|getRS
argument_list|()
operator|.
name|tableDescriptors
operator|.
name|get
argument_list|(
name|hri
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|getRS
argument_list|()
operator|.
name|service
operator|.
name|submit
argument_list|(
operator|new
name|OpenRegionHandler
argument_list|(
name|getRS
argument_list|()
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hri
argument_list|,
name|htd
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// The open handler should have removed the region from RIT but kept the region closed
name|checkRegionIsClosed
argument_list|()
expr_stmt|;
comment|// The open handler should have updated the value in ZK.
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ZKAssign
operator|.
name|deleteNode
argument_list|(
name|getRS
argument_list|()
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|EventType
operator|.
name|RS_ZK_REGION_FAILED_OPEN
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|reopenRegion
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test an open then a close with ZK. This is going to mess-up the ZK states, so    * the opening will fail as well because it doesn't find what it expects in ZK.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testCancelOpeningWithZK
parameter_list|()
throws|throws
name|Exception
block|{
comment|// We close
name|closeNoZK
argument_list|()
expr_stmt|;
name|checkRegionIsClosed
argument_list|()
expr_stmt|;
comment|// Let do the initial steps, without having a handler
name|getRS
argument_list|()
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
comment|// That's a close without ZK.
name|ZKAssign
operator|.
name|createNodeClosing
argument_list|(
name|HTU
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|hri
argument_list|,
name|getRS
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|AdminProtos
operator|.
name|CloseRegionRequest
name|crr
init|=
name|RequestConverter
operator|.
name|buildCloseRegionRequest
argument_list|(
name|regionName
argument_list|,
literal|false
argument_list|)
decl_stmt|;
try|try
block|{
name|getRS
argument_list|()
operator|.
name|closeRegion
argument_list|(
literal|null
argument_list|,
name|crr
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|expected
parameter_list|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|expected
operator|.
name|getCause
argument_list|()
operator|instanceof
name|NotServingRegionException
argument_list|)
expr_stmt|;
block|}
comment|// The close should have left the ZK state as it is: it's the job the AM to delete it
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ZKAssign
operator|.
name|deleteNode
argument_list|(
name|getRS
argument_list|()
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|EventType
operator|.
name|M_ZK_REGION_CLOSING
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// The state in RIT should have changed to close
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Boolean
operator|.
name|FALSE
argument_list|,
name|getRS
argument_list|()
operator|.
name|getRegionsInTransitionInRS
argument_list|()
operator|.
name|get
argument_list|(
name|hri
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Let's start the open handler
comment|// It should not succeed for two reasons:
comment|//  1) There is no ZK node
comment|//  2) The region in RIT was changed.
comment|// The order is more or less implementation dependant.
name|HTableDescriptor
name|htd
init|=
name|getRS
argument_list|()
operator|.
name|tableDescriptors
operator|.
name|get
argument_list|(
name|hri
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|getRS
argument_list|()
operator|.
name|service
operator|.
name|submit
argument_list|(
operator|new
name|OpenRegionHandler
argument_list|(
name|getRS
argument_list|()
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hri
argument_list|,
name|htd
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// The open handler should have removed the region from RIT but kept the region closed
name|checkRegionIsClosed
argument_list|()
expr_stmt|;
comment|// We should not find any znode here.
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|ZKAssign
operator|.
name|getVersion
argument_list|(
name|HTU
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|hri
argument_list|)
argument_list|)
expr_stmt|;
name|reopenRegion
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

