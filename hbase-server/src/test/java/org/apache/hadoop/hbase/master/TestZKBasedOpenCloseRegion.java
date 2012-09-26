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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|Collection
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
name|AtomicBoolean
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
name|TableDescriptors
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
name|client
operator|.
name|ResultScanner
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
name|Scan
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
name|EventHandler
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
name|EventHandler
operator|.
name|EventHandlerListener
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
name|EventHandler
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
name|master
operator|.
name|handler
operator|.
name|TotesHRegionInfo
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
name|Threads
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

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|internal
operator|.
name|util
operator|.
name|reflection
operator|.
name|Whitebox
import|;
end_import

begin_comment
comment|/**  * Test open and close of regions using zk.  */
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
name|TestZKBasedOpenCloseRegion
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
name|TestZKBasedOpenCloseRegion
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
name|String
name|TABLENAME
init|=
literal|"TestZKBasedOpenCloseRegion"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|FAMILIES
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c"
argument_list|)
block|}
decl_stmt|;
specifier|private
specifier|static
name|int
name|countOfRegions
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeAllTests
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|c
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|c
operator|.
name|setBoolean
argument_list|(
literal|"dfs.support.append"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|c
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.info.port"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|TABLENAME
argument_list|)
argument_list|,
name|FAMILIES
argument_list|)
expr_stmt|;
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TABLENAME
argument_list|)
decl_stmt|;
name|countOfRegions
operator|=
name|TEST_UTIL
operator|.
name|createMultiRegions
argument_list|(
name|t
argument_list|,
name|getTestFamily
argument_list|()
argument_list|)
expr_stmt|;
name|waitUntilAllRegionsAssigned
argument_list|()
expr_stmt|;
name|addToEachStartKey
argument_list|(
name|countOfRegions
argument_list|)
expr_stmt|;
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterAllTests
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
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getLiveRegionServerThreads
argument_list|()
operator|.
name|size
argument_list|()
operator|<
literal|2
condition|)
block|{
comment|// Need at least two servers.
name|LOG
operator|.
name|info
argument_list|(
literal|"Started new server="
operator|+
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|startRegionServer
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|waitUntilAllRegionsAssigned
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test we reopen a region once closed.    * @throws Exception    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testReOpenRegion
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Number of region servers = "
operator|+
name|cluster
operator|.
name|getLiveRegionServerThreads
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|rsIdx
init|=
literal|0
decl_stmt|;
name|HRegionServer
name|regionServer
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
name|rsIdx
argument_list|)
decl_stmt|;
name|HRegionInfo
name|hri
init|=
name|getNonMetaRegion
argument_list|(
name|ProtobufUtil
operator|.
name|getOnlineRegions
argument_list|(
name|regionServer
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Asking RS to close region "
operator|+
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
name|AtomicBoolean
name|closeEventProcessed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|AtomicBoolean
name|reopenEventProcessed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|EventHandlerListener
name|closeListener
init|=
operator|new
name|ReopenEventListener
argument_list|(
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|closeEventProcessed
argument_list|,
name|EventType
operator|.
name|RS_ZK_REGION_CLOSED
argument_list|)
decl_stmt|;
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|executorService
operator|.
name|registerListener
argument_list|(
name|EventType
operator|.
name|RS_ZK_REGION_CLOSED
argument_list|,
name|closeListener
argument_list|)
expr_stmt|;
name|EventHandlerListener
name|openListener
init|=
operator|new
name|ReopenEventListener
argument_list|(
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|reopenEventProcessed
argument_list|,
name|EventType
operator|.
name|RS_ZK_REGION_OPENED
argument_list|)
decl_stmt|;
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|executorService
operator|.
name|registerListener
argument_list|(
name|EventType
operator|.
name|RS_ZK_REGION_OPENED
argument_list|,
name|openListener
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Unassign "
operator|+
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|assignmentManager
operator|.
name|unassign
argument_list|(
name|hri
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|closeEventProcessed
operator|.
name|get
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
while|while
condition|(
operator|!
name|reopenEventProcessed
operator|.
name|get
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Done with testReOpenRegion"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HRegionInfo
name|getNonMetaRegion
parameter_list|(
specifier|final
name|Collection
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|)
block|{
name|HRegionInfo
name|hri
init|=
literal|null
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|i
range|:
name|regions
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|i
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|i
operator|.
name|isMetaRegion
argument_list|()
condition|)
block|{
name|hri
operator|=
name|i
expr_stmt|;
break|break;
block|}
block|}
return|return
name|hri
return|;
block|}
specifier|public
specifier|static
class|class
name|ReopenEventListener
implements|implements
name|EventHandlerListener
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
name|ReopenEventListener
operator|.
name|class
argument_list|)
decl_stmt|;
name|String
name|regionName
decl_stmt|;
name|AtomicBoolean
name|eventProcessed
decl_stmt|;
name|EventType
name|eventType
decl_stmt|;
specifier|public
name|ReopenEventListener
parameter_list|(
name|String
name|regionName
parameter_list|,
name|AtomicBoolean
name|eventProcessed
parameter_list|,
name|EventType
name|eventType
parameter_list|)
block|{
name|this
operator|.
name|regionName
operator|=
name|regionName
expr_stmt|;
name|this
operator|.
name|eventProcessed
operator|=
name|eventProcessed
expr_stmt|;
name|this
operator|.
name|eventType
operator|=
name|eventType
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|beforeProcess
parameter_list|(
name|EventHandler
name|event
parameter_list|)
block|{
if|if
condition|(
name|event
operator|.
name|getEventType
argument_list|()
operator|==
name|eventType
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Received "
operator|+
name|eventType
operator|+
literal|" and beginning to process it"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|afterProcess
parameter_list|(
name|EventHandler
name|event
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"afterProcess("
operator|+
name|event
operator|+
literal|")"
argument_list|)
expr_stmt|;
if|if
condition|(
name|event
operator|.
name|getEventType
argument_list|()
operator|==
name|eventType
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Finished processing "
operator|+
name|eventType
argument_list|)
expr_stmt|;
name|String
name|regionName
init|=
literal|""
decl_stmt|;
if|if
condition|(
name|eventType
operator|==
name|EventType
operator|.
name|RS_ZK_REGION_OPENED
condition|)
block|{
name|TotesHRegionInfo
name|hriCarrier
init|=
operator|(
name|TotesHRegionInfo
operator|)
name|event
decl_stmt|;
name|regionName
operator|=
name|hriCarrier
operator|.
name|getHRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|eventType
operator|==
name|EventType
operator|.
name|RS_ZK_REGION_CLOSED
condition|)
block|{
name|TotesHRegionInfo
name|hriCarrier
init|=
operator|(
name|TotesHRegionInfo
operator|)
name|event
decl_stmt|;
name|regionName
operator|=
name|hriCarrier
operator|.
name|getHRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|regionName
operator|.
name|equals
argument_list|(
name|regionName
argument_list|)
condition|)
block|{
name|eventProcessed
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|eventProcessed
init|)
block|{
name|eventProcessed
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|CloseRegionEventListener
implements|implements
name|EventHandlerListener
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
name|CloseRegionEventListener
operator|.
name|class
argument_list|)
decl_stmt|;
name|String
name|regionToClose
decl_stmt|;
name|AtomicBoolean
name|closeEventProcessed
decl_stmt|;
specifier|public
name|CloseRegionEventListener
parameter_list|(
name|String
name|regionToClose
parameter_list|,
name|AtomicBoolean
name|closeEventProcessed
parameter_list|)
block|{
name|this
operator|.
name|regionToClose
operator|=
name|regionToClose
expr_stmt|;
name|this
operator|.
name|closeEventProcessed
operator|=
name|closeEventProcessed
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|afterProcess
parameter_list|(
name|EventHandler
name|event
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"afterProcess("
operator|+
name|event
operator|+
literal|")"
argument_list|)
expr_stmt|;
if|if
condition|(
name|event
operator|.
name|getEventType
argument_list|()
operator|==
name|EventType
operator|.
name|RS_ZK_REGION_CLOSED
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Finished processing CLOSE REGION"
argument_list|)
expr_stmt|;
name|TotesHRegionInfo
name|hriCarrier
init|=
operator|(
name|TotesHRegionInfo
operator|)
name|event
decl_stmt|;
if|if
condition|(
name|regionToClose
operator|.
name|equals
argument_list|(
name|hriCarrier
operator|.
name|getHRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting closeEventProcessed flag"
argument_list|)
expr_stmt|;
name|closeEventProcessed
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Region to close didn't match"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|beforeProcess
parameter_list|(
name|EventHandler
name|event
parameter_list|)
block|{
if|if
condition|(
name|event
operator|.
name|getEventType
argument_list|()
operator|==
name|EventType
operator|.
name|M_RS_CLOSE_REGION
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Received CLOSE RPC and beginning to process it"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * This test shows how a region won't be able to be assigned to a RS    * if it's already "processing" it.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testRSAlreadyProcessingRegion
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"starting testRSAlreadyProcessingRegion"
argument_list|)
expr_stmt|;
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|HRegionServer
name|hr0
init|=
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
decl_stmt|;
name|HRegionServer
name|hr1
init|=
name|cluster
operator|.
name|getLiveRegionServerThreads
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getRegionServer
argument_list|()
decl_stmt|;
name|HRegionInfo
name|hri
init|=
name|getNonMetaRegion
argument_list|(
name|ProtobufUtil
operator|.
name|getOnlineRegions
argument_list|(
name|hr0
argument_list|)
argument_list|)
decl_stmt|;
comment|// fake that hr1 is processing the region
name|hr1
operator|.
name|getRegionsInTransitionInRS
argument_list|()
operator|.
name|putIfAbsent
argument_list|(
name|hri
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|AtomicBoolean
name|reopenEventProcessed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|EventHandlerListener
name|openListener
init|=
operator|new
name|ReopenEventListener
argument_list|(
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|reopenEventProcessed
argument_list|,
name|EventType
operator|.
name|RS_ZK_REGION_OPENED
argument_list|)
decl_stmt|;
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|executorService
operator|.
name|registerListener
argument_list|(
name|EventType
operator|.
name|RS_ZK_REGION_OPENED
argument_list|,
name|openListener
argument_list|)
expr_stmt|;
comment|// now ask the master to move the region to hr1, will fail
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|move
argument_list|(
name|hri
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|hr1
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// make sure the region came back
name|assertEquals
argument_list|(
name|hr1
operator|.
name|getOnlineRegion
argument_list|(
name|hri
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// remove the block and reset the boolean
name|hr1
operator|.
name|getRegionsInTransitionInRS
argument_list|()
operator|.
name|remove
argument_list|(
name|hri
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
expr_stmt|;
name|reopenEventProcessed
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// now try moving a region when there is no region in transition.
name|hri
operator|=
name|getNonMetaRegion
argument_list|(
name|ProtobufUtil
operator|.
name|getOnlineRegions
argument_list|(
name|hr1
argument_list|)
argument_list|)
expr_stmt|;
name|openListener
operator|=
operator|new
name|ReopenEventListener
argument_list|(
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|reopenEventProcessed
argument_list|,
name|EventType
operator|.
name|RS_ZK_REGION_OPENED
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|executorService
operator|.
name|registerListener
argument_list|(
name|EventType
operator|.
name|RS_ZK_REGION_OPENED
argument_list|,
name|openListener
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|move
argument_list|(
name|hri
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|hr0
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|reopenEventProcessed
operator|.
name|get
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
comment|// make sure the region has moved from the original RS
name|assertTrue
argument_list|(
name|hr1
operator|.
name|getOnlineRegion
argument_list|(
name|hri
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
operator|==
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testCloseRegion
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Running testCloseRegion"
argument_list|)
expr_stmt|;
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Number of region servers = "
operator|+
name|cluster
operator|.
name|getLiveRegionServerThreads
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|rsIdx
init|=
literal|0
decl_stmt|;
name|HRegionServer
name|regionServer
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
name|rsIdx
argument_list|)
decl_stmt|;
name|HRegionInfo
name|hri
init|=
name|getNonMetaRegion
argument_list|(
name|ProtobufUtil
operator|.
name|getOnlineRegions
argument_list|(
name|regionServer
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Asking RS to close region "
operator|+
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
name|AtomicBoolean
name|closeEventProcessed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|EventHandlerListener
name|listener
init|=
operator|new
name|CloseRegionEventListener
argument_list|(
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|closeEventProcessed
argument_list|)
decl_stmt|;
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|executorService
operator|.
name|registerListener
argument_list|(
name|EventType
operator|.
name|RS_ZK_REGION_CLOSED
argument_list|,
name|listener
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|assignmentManager
operator|.
name|unassign
argument_list|(
name|hri
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|closeEventProcessed
operator|.
name|get
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Done with testCloseRegion"
argument_list|)
expr_stmt|;
block|}
comment|/**    * If region open fails with IOException in openRegion() while doing tableDescriptors.get()    * the region should not add into regionsInTransitionInRS map    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testRegionOpenFailsDueToIOException
parameter_list|()
throws|throws
name|Exception
block|{
name|HRegionInfo
name|REGIONINFO
init|=
operator|new
name|HRegionInfo
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"t"
argument_list|)
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
name|HRegionServer
name|regionServer
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|TableDescriptors
name|htd
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|TableDescriptors
operator|.
name|class
argument_list|)
decl_stmt|;
name|Object
name|orizinalState
init|=
name|Whitebox
operator|.
name|getInternalState
argument_list|(
name|regionServer
argument_list|,
literal|"tableDescriptors"
argument_list|)
decl_stmt|;
name|Whitebox
operator|.
name|setInternalState
argument_list|(
name|regionServer
argument_list|,
literal|"tableDescriptors"
argument_list|,
name|htd
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doThrow
argument_list|(
operator|new
name|IOException
argument_list|()
argument_list|)
operator|.
name|when
argument_list|(
name|htd
argument_list|)
operator|.
name|get
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|ProtobufUtil
operator|.
name|openRegion
argument_list|(
name|regionServer
argument_list|,
name|REGIONINFO
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"It should throw IOException "
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{     }
name|Whitebox
operator|.
name|setInternalState
argument_list|(
name|regionServer
argument_list|,
literal|"tableDescriptors"
argument_list|,
name|orizinalState
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Region should not be in RIT"
argument_list|,
name|regionServer
operator|.
name|getRegionsInTransitionInRS
argument_list|()
operator|.
name|containsKey
argument_list|(
name|REGIONINFO
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|waitUntilAllRegionsAssigned
parameter_list|()
throws|throws
name|IOException
block|{
name|HTable
name|meta
init|=
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|rows
init|=
literal|0
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|SERVER_QUALIFIER
argument_list|)
expr_stmt|;
name|ResultScanner
name|s
init|=
name|meta
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
for|for
control|(
name|Result
name|r
init|=
literal|null
init|;
operator|(
name|r
operator|=
name|s
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|;
control|)
block|{
name|byte
index|[]
name|b
init|=
name|r
operator|.
name|getValue
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|SERVER_QUALIFIER
argument_list|)
decl_stmt|;
if|if
condition|(
name|b
operator|==
literal|null
operator|||
name|b
operator|.
name|length
operator|<=
literal|0
condition|)
block|{
break|break;
block|}
name|rows
operator|++
expr_stmt|;
block|}
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// If I get to here and all rows have a Server, then all have been assigned.
if|if
condition|(
name|rows
operator|>=
name|countOfRegions
condition|)
block|{
break|break;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Found="
operator|+
name|rows
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
name|meta
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/*    * Add to each of the regions in .META. a value.  Key is the startrow of the    * region (except its 'aaa' for first region).  Actual value is the row name.    * @param expected    * @return    * @throws IOException    */
specifier|private
specifier|static
name|int
name|addToEachStartKey
parameter_list|(
specifier|final
name|int
name|expected
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TABLENAME
argument_list|)
decl_stmt|;
name|HTable
name|meta
init|=
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|int
name|rows
init|=
literal|0
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|)
expr_stmt|;
name|ResultScanner
name|s
init|=
name|meta
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
for|for
control|(
name|Result
name|r
init|=
literal|null
init|;
operator|(
name|r
operator|=
name|s
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|;
control|)
block|{
name|HRegionInfo
name|hri
init|=
name|HRegionInfo
operator|.
name|getHRegionInfo
argument_list|(
name|r
argument_list|)
decl_stmt|;
if|if
condition|(
name|hri
operator|==
literal|null
condition|)
break|break;
comment|// If start key, add 'aaa'.
name|byte
index|[]
name|row
init|=
name|getStartKey
argument_list|(
name|hri
argument_list|)
decl_stmt|;
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
name|setWriteToWAL
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|getTestFamily
argument_list|()
argument_list|,
name|getTestQualifier
argument_list|()
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|rows
operator|++
expr_stmt|;
block|}
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|rows
argument_list|)
expr_stmt|;
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
name|meta
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|rows
return|;
block|}
specifier|private
specifier|static
name|byte
index|[]
name|getStartKey
parameter_list|(
specifier|final
name|HRegionInfo
name|hri
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|hri
operator|.
name|getStartKey
argument_list|()
argument_list|)
condition|?
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
else|:
name|hri
operator|.
name|getStartKey
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|byte
index|[]
name|getTestFamily
parameter_list|()
block|{
return|return
name|FAMILIES
index|[
literal|0
index|]
return|;
block|}
specifier|private
specifier|static
name|byte
index|[]
name|getTestQualifier
parameter_list|()
block|{
return|return
name|getTestFamily
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
name|args
index|[]
parameter_list|)
throws|throws
name|Exception
block|{
name|TestZKBasedOpenCloseRegion
operator|.
name|beforeAllTests
argument_list|()
expr_stmt|;
name|TestZKBasedOpenCloseRegion
name|test
init|=
operator|new
name|TestZKBasedOpenCloseRegion
argument_list|()
decl_stmt|;
name|test
operator|.
name|setup
argument_list|()
expr_stmt|;
name|test
operator|.
name|testCloseRegion
argument_list|()
expr_stmt|;
name|TestZKBasedOpenCloseRegion
operator|.
name|afterAllTests
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

