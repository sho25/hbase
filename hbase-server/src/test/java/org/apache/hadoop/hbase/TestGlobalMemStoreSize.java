begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional infomation  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
operator|.
name|Region
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
name|MiscTests
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
comment|/**  * Test HBASE-3694 whether the GlobalMemStoreSize is the same as the summary  * of all the online region's MemStoreSize  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
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
name|TestGlobalMemStoreSize
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
name|TestGlobalMemStoreSize
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|int
name|regionServerNum
init|=
literal|4
decl_stmt|;
specifier|private
specifier|static
name|int
name|regionNum
init|=
literal|16
decl_stmt|;
comment|// total region num = region num + root and meta regions
specifier|private
specifier|static
name|int
name|totalRegionNum
init|=
name|regionNum
operator|+
literal|2
decl_stmt|;
specifier|private
name|HBaseTestingUtility
name|TEST_UTIL
decl_stmt|;
specifier|private
name|MiniHBaseCluster
name|cluster
decl_stmt|;
comment|/**    * Test the global mem store size in the region server is equal to sum of each    * region's mem store size    * @throws Exception     */
annotation|@
name|Test
specifier|public
name|void
name|testGlobalMemStore
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Start the cluster
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting cluster"
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|,
name|regionServerNum
argument_list|)
expr_stmt|;
name|cluster
operator|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for active/ready master"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitForActiveAndReadyMaster
argument_list|()
expr_stmt|;
comment|// Create a table with regions
name|TableName
name|table
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestGlobalMemStoreSize"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating table with "
operator|+
name|regionNum
operator|+
literal|" regions"
argument_list|)
expr_stmt|;
name|Table
name|ht
init|=
name|TEST_UTIL
operator|.
name|createMultiRegionTable
argument_list|(
name|table
argument_list|,
name|family
argument_list|,
name|regionNum
argument_list|)
decl_stmt|;
name|int
name|numRegions
init|=
operator|-
literal|1
decl_stmt|;
try|try
init|(
name|RegionLocator
name|r
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getRegionLocator
argument_list|(
name|table
argument_list|)
init|)
block|{
name|numRegions
operator|=
name|r
operator|.
name|getStartKeys
argument_list|()
operator|.
name|length
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|regionNum
argument_list|,
name|numRegions
argument_list|)
expr_stmt|;
name|waitForAllRegionsAssigned
argument_list|()
expr_stmt|;
for|for
control|(
name|HRegionServer
name|server
range|:
name|getOnlineRegionServers
argument_list|()
control|)
block|{
name|long
name|globalMemStoreSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|regionInfo
range|:
name|ProtobufUtil
operator|.
name|getOnlineRegions
argument_list|(
name|server
operator|.
name|getRSRpcServices
argument_list|()
argument_list|)
control|)
block|{
name|globalMemStoreSize
operator|+=
name|server
operator|.
name|getFromOnlineRegions
argument_list|(
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|)
operator|.
name|getMemstoreSize
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|server
operator|.
name|getRegionServerAccounting
argument_list|()
operator|.
name|getGlobalMemstoreSize
argument_list|()
argument_list|,
name|globalMemStoreSize
argument_list|)
expr_stmt|;
block|}
comment|// check the global memstore size after flush
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|HRegionServer
name|server
range|:
name|getOnlineRegionServers
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting flushes on "
operator|+
name|server
operator|.
name|getServerName
argument_list|()
operator|+
literal|", size="
operator|+
name|server
operator|.
name|getRegionServerAccounting
argument_list|()
operator|.
name|getGlobalMemstoreSize
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HRegionInfo
name|regionInfo
range|:
name|ProtobufUtil
operator|.
name|getOnlineRegions
argument_list|(
name|server
operator|.
name|getRSRpcServices
argument_list|()
argument_list|)
control|)
block|{
name|Region
name|r
init|=
name|server
operator|.
name|getFromOnlineRegions
argument_list|(
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|flush
argument_list|(
name|r
argument_list|,
name|server
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Post flush on "
operator|+
name|server
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|timeout
init|=
name|now
operator|+
literal|1000
decl_stmt|;
while|while
condition|(
name|server
operator|.
name|getRegionServerAccounting
argument_list|()
operator|.
name|getGlobalMemstoreSize
argument_list|()
operator|!=
literal|0
operator|&&
name|timeout
operator|<
name|System
operator|.
name|currentTimeMillis
argument_list|()
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
name|long
name|size
init|=
name|server
operator|.
name|getRegionServerAccounting
argument_list|()
operator|.
name|getGlobalMemstoreSize
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|>
literal|0
condition|)
block|{
comment|// If size> 0, see if its because the meta region got edits while
comment|// our test was running....
for|for
control|(
name|HRegionInfo
name|regionInfo
range|:
name|ProtobufUtil
operator|.
name|getOnlineRegions
argument_list|(
name|server
operator|.
name|getRSRpcServices
argument_list|()
argument_list|)
control|)
block|{
name|Region
name|r
init|=
name|server
operator|.
name|getFromOnlineRegions
argument_list|(
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|l
init|=
name|r
operator|.
name|getMemstoreSize
argument_list|()
decl_stmt|;
if|if
condition|(
name|l
operator|>
literal|0
condition|)
block|{
comment|// Only meta could have edits at this stage.  Give it another flush
comment|// clear them.
name|assertTrue
argument_list|(
name|regionInfo
operator|.
name|isMetaRegion
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|r
operator|.
name|toString
argument_list|()
operator|+
literal|" "
operator|+
name|l
operator|+
literal|", reflushing"
argument_list|)
expr_stmt|;
name|r
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|size
operator|=
name|server
operator|.
name|getRegionServerAccounting
argument_list|()
operator|.
name|getGlobalMemstoreSize
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Server="
operator|+
name|server
operator|.
name|getServerName
argument_list|()
operator|+
literal|", i="
operator|+
name|i
operator|++
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
name|ht
operator|.
name|close
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Flush and log stats on flush    * @param r    * @param server    * @throws IOException    */
specifier|private
name|void
name|flush
parameter_list|(
specifier|final
name|Region
name|r
parameter_list|,
specifier|final
name|HRegionServer
name|server
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Flush "
operator|+
name|r
operator|.
name|toString
argument_list|()
operator|+
literal|" on "
operator|+
name|server
operator|.
name|getServerName
argument_list|()
operator|+
literal|", "
operator|+
name|r
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
operator|+
literal|", size="
operator|+
name|server
operator|.
name|getRegionServerAccounting
argument_list|()
operator|.
name|getGlobalMemstoreSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|HRegionServer
argument_list|>
name|getOnlineRegionServers
parameter_list|()
block|{
name|List
argument_list|<
name|HRegionServer
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|HRegionServer
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|rst
range|:
name|cluster
operator|.
name|getRegionServerThreads
argument_list|()
control|)
block|{
if|if
condition|(
name|rst
operator|.
name|getRegionServer
argument_list|()
operator|.
name|isOnline
argument_list|()
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
name|rst
operator|.
name|getRegionServer
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|list
return|;
block|}
comment|/**    * Wait until all the regions are assigned.    */
specifier|private
name|void
name|waitForAllRegionsAssigned
parameter_list|()
throws|throws
name|IOException
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|regionCount
init|=
name|HBaseTestingUtility
operator|.
name|getAllOnlineRegions
argument_list|(
name|cluster
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|regionCount
operator|>=
name|totalRegionNum
condition|)
break|break;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Waiting for there to be "
operator|+
name|totalRegionNum
operator|+
literal|" regions, but there are "
operator|+
name|regionCount
operator|+
literal|" right now."
argument_list|)
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{}
block|}
block|}
block|}
end_class

end_unit

