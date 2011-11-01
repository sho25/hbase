begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional infomation  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|*
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
name|List
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
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  * Test HBASE-3694 whether the GlobalMemStoreSize is the same as the summary  * of all the online region's MemStoreSize  */
end_comment

begin_class
specifier|public
class|class
name|TestGlobalMemStoreSize
block|{
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
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
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.assignment.timeoutmonitor.period"
argument_list|,
literal|2000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.assignment.timeoutmonitor.timeout"
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
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
name|byte
index|[]
name|table
init|=
name|Bytes
operator|.
name|toBytes
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
name|HTable
name|ht
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|table
argument_list|,
name|family
argument_list|)
decl_stmt|;
name|int
name|numRegions
init|=
name|TEST_UTIL
operator|.
name|createMultiRegions
argument_list|(
name|conf
argument_list|,
name|ht
argument_list|,
name|family
argument_list|,
name|regionNum
argument_list|)
decl_stmt|;
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
name|server
operator|.
name|getOnlineRegions
argument_list|()
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
operator|.
name|get
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
for|for
control|(
name|HRegionServer
name|server
range|:
name|getOnlineRegionServers
argument_list|()
control|)
block|{
for|for
control|(
name|HRegionInfo
name|regionInfo
range|:
name|server
operator|.
name|getOnlineRegions
argument_list|()
control|)
block|{
name|HRegion
name|region
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
name|region
operator|.
name|flushcache
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
literal|0
argument_list|)
expr_stmt|;
block|}
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/** figure out how many regions are currently being served. */
specifier|private
name|int
name|getRegionCount
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|total
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
name|total
operator|+=
name|server
operator|.
name|getOnlineRegions
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
return|return
name|total
return|;
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
name|getRegionCount
argument_list|()
operator|<
name|totalRegionNum
condition|)
block|{
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
name|getRegionCount
argument_list|()
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

