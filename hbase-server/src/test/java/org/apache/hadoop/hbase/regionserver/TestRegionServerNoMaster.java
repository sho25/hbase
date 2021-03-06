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
name|client
operator|.
name|RegionInfo
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
name|RegionLocator
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
name|Table
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
name|TableDescriptor
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
name|RegionServerThread
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
name|shaded
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
name|shaded
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|CloseRegionRequest
import|;
end_import

begin_comment
comment|/**  * Tests on the region server, without the master.  */
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
name|TestRegionServerNoMaster
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
name|TestRegionServerNoMaster
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
name|TestRegionServerNoMaster
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|Table
name|table
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ee"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|RegionInfo
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
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|TestRegionServerNoMaster
operator|.
name|class
operator|.
name|getSimpleName
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
name|addColumn
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
try|try
init|(
name|RegionLocator
name|locator
init|=
name|HTU
operator|.
name|getConnection
argument_list|()
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|hri
operator|=
name|locator
operator|.
name|getRegionLocation
argument_list|(
name|row
argument_list|,
literal|false
argument_list|)
operator|.
name|getRegion
argument_list|()
expr_stmt|;
block|}
name|regionName
operator|=
name|hri
operator|.
name|getRegionName
argument_list|()
expr_stmt|;
name|stopMasterAndAssignMeta
argument_list|(
name|HTU
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|stopMasterAndAssignMeta
parameter_list|(
name|HBaseTestingUtility
name|HTU
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// Stop master
name|HMaster
name|master
init|=
name|HTU
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|Thread
name|masterThread
init|=
name|HTU
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMasterThread
argument_list|()
decl_stmt|;
name|ServerName
name|masterAddr
init|=
name|master
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|master
operator|.
name|stopMaster
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting until master thread exits"
argument_list|)
expr_stmt|;
while|while
condition|(
name|masterThread
operator|!=
literal|null
operator|&&
name|masterThread
operator|.
name|isAlive
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
name|HRegionServer
operator|.
name|TEST_SKIP_REPORTING_TRANSITION
operator|=
literal|true
expr_stmt|;
comment|// Master is down, so is the meta. We need to assign it somewhere
comment|// so that regions can be assigned during the mocking phase.
name|HRegionServer
name|hrs
init|=
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
decl_stmt|;
name|ZKWatcher
name|zkw
init|=
name|hrs
operator|.
name|getZooKeeper
argument_list|()
decl_stmt|;
name|ServerName
name|sn
init|=
name|MetaTableLocator
operator|.
name|getMetaRegionLocation
argument_list|(
name|zkw
argument_list|)
decl_stmt|;
if|if
condition|(
name|sn
operator|!=
literal|null
operator|&&
operator|!
name|masterAddr
operator|.
name|equals
argument_list|(
name|sn
argument_list|)
condition|)
block|{
return|return;
block|}
name|ProtobufUtil
operator|.
name|openRegion
argument_list|(
literal|null
argument_list|,
name|hrs
operator|.
name|getRSRpcServices
argument_list|()
argument_list|,
name|hrs
operator|.
name|getServerName
argument_list|()
argument_list|,
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|sn
operator|=
name|MetaTableLocator
operator|.
name|getMetaRegionLocation
argument_list|(
name|zkw
argument_list|)
expr_stmt|;
if|if
condition|(
name|sn
operator|!=
literal|null
operator|&&
name|sn
operator|.
name|equals
argument_list|(
name|hrs
operator|.
name|getServerName
argument_list|()
argument_list|)
operator|&&
name|hrs
operator|.
name|getOnlineRegions
argument_list|()
operator|.
name|containsKey
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getEncodedName
argument_list|()
argument_list|)
condition|)
block|{
break|break;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Flush the given region in the mini cluster. Since no master, we cannot use HBaseAdmin.flush() */
specifier|public
specifier|static
name|void
name|flushRegion
parameter_list|(
name|HBaseTestingUtility
name|HTU
parameter_list|,
name|RegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|RegionServerThread
name|rst
range|:
name|HTU
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
control|)
block|{
name|HRegion
name|region
init|=
name|rst
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getRegionByEncodedName
argument_list|(
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|region
operator|!=
literal|null
condition|)
block|{
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Region to flush cannot be found"
argument_list|)
throw|;
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
name|HRegionServer
operator|.
name|TEST_SKIP_REPORTING_TRANSITION
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|HTU
operator|.
name|shutdownMiniCluster
argument_list|()
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
specifier|public
specifier|static
name|void
name|openRegion
parameter_list|(
name|HBaseTestingUtility
name|HTU
parameter_list|,
name|HRegionServer
name|rs
parameter_list|,
name|RegionInfo
name|hri
parameter_list|)
throws|throws
name|Exception
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
name|rs
operator|.
name|getServerName
argument_list|()
argument_list|,
name|hri
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|AdminProtos
operator|.
name|OpenRegionResponse
name|responseOpen
init|=
name|rs
operator|.
name|rpcServices
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
argument_list|(
name|HTU
argument_list|,
name|rs
argument_list|,
name|hri
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|checkRegionIsOpened
parameter_list|(
name|HBaseTestingUtility
name|HTU
parameter_list|,
name|HRegionServer
name|rs
parameter_list|,
name|RegionInfo
name|hri
parameter_list|)
throws|throws
name|Exception
block|{
while|while
condition|(
operator|!
name|rs
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
name|rs
operator|.
name|getRegion
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|)
operator|.
name|isAvailable
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|closeRegion
parameter_list|(
name|HBaseTestingUtility
name|HTU
parameter_list|,
name|HRegionServer
name|rs
parameter_list|,
name|HRegionInfo
name|hri
parameter_list|)
throws|throws
name|Exception
block|{
name|AdminProtos
operator|.
name|CloseRegionRequest
name|crr
init|=
name|ProtobufUtil
operator|.
name|buildCloseRegionRequest
argument_list|(
name|rs
operator|.
name|getServerName
argument_list|()
argument_list|,
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|AdminProtos
operator|.
name|CloseRegionResponse
name|responseClose
init|=
name|rs
operator|.
name|rpcServices
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
argument_list|(
name|HTU
argument_list|,
name|rs
argument_list|,
name|hri
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|checkRegionIsClosed
parameter_list|(
name|HBaseTestingUtility
name|HTU
parameter_list|,
name|HRegionServer
name|rs
parameter_list|,
name|RegionInfo
name|hri
parameter_list|)
throws|throws
name|Exception
block|{
while|while
condition|(
operator|!
name|rs
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
name|rs
operator|.
name|getRegion
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
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
block|}
comment|/**    * Close the region without using ZK    */
specifier|private
name|void
name|closeRegionNoZK
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
name|ProtobufUtil
operator|.
name|buildCloseRegionRequest
argument_list|(
name|getRS
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|,
name|regionName
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
name|rpcServices
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
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hri
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCloseByRegionServer
parameter_list|()
throws|throws
name|Exception
block|{
name|closeRegionNoZK
argument_list|()
expr_stmt|;
name|openRegion
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hri
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultipleCloseFromMaster
parameter_list|()
throws|throws
name|Exception
block|{
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
name|ProtobufUtil
operator|.
name|buildCloseRegionRequest
argument_list|(
name|getRS
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|,
name|regionName
argument_list|,
literal|null
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
name|rpcServices
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
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
name|se
parameter_list|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"The next queries may throw an exception."
argument_list|,
name|i
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
name|checkRegionIsClosed
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hri
argument_list|)
expr_stmt|;
name|openRegion
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hri
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that if we do a close while opening it stops the opening.    */
annotation|@
name|Test
specifier|public
name|void
name|testCancelOpeningWithoutZK
parameter_list|()
throws|throws
name|Exception
block|{
comment|// We close
name|closeRegionNoZK
argument_list|()
expr_stmt|;
name|checkRegionIsClosed
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hri
argument_list|)
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
name|AdminProtos
operator|.
name|CloseRegionRequest
name|crr
init|=
name|ProtobufUtil
operator|.
name|buildCloseRegionRequest
argument_list|(
name|getRS
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|,
name|regionName
argument_list|)
decl_stmt|;
try|try
block|{
name|getRS
argument_list|()
operator|.
name|rpcServices
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
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
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
name|TableDescriptor
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
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
name|getRS
argument_list|()
operator|.
name|executorService
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
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|// The open handler should have removed the region from RIT but kept the region closed
name|checkRegionIsClosed
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hri
argument_list|)
expr_stmt|;
name|openRegion
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hri
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests an on-the-fly RPC that was scheduled for the earlier RS on the same port    * for openRegion. The region server should reject this RPC. (HBASE-9721)    */
annotation|@
name|Test
specifier|public
name|void
name|testOpenCloseRegionRPCIntendedForPreviousServer
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
name|ServerName
name|sn
init|=
name|getRS
argument_list|()
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|ServerName
name|earlierServerName
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
name|sn
operator|.
name|getHostname
argument_list|()
argument_list|,
name|sn
operator|.
name|getPort
argument_list|()
argument_list|,
literal|1
argument_list|)
decl_stmt|;
try|try
block|{
name|CloseRegionRequest
name|request
init|=
name|ProtobufUtil
operator|.
name|buildCloseRegionRequest
argument_list|(
name|earlierServerName
argument_list|,
name|regionName
argument_list|)
decl_stmt|;
name|getRS
argument_list|()
operator|.
name|getRSRpcServices
argument_list|()
operator|.
name|closeRegion
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"The closeRegion should have been rejected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
name|se
parameter_list|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|se
operator|.
name|getCause
argument_list|()
operator|instanceof
name|IOException
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|se
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"This RPC was intended for a different server"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//actual close
name|closeRegionNoZK
argument_list|()
expr_stmt|;
try|try
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
name|earlierServerName
argument_list|,
name|hri
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|getRS
argument_list|()
operator|.
name|getRSRpcServices
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
literal|"The openRegion should have been rejected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
name|se
parameter_list|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|se
operator|.
name|getCause
argument_list|()
operator|instanceof
name|IOException
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|se
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"This RPC was intended for a different server"
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|openRegion
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hri
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

