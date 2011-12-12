begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hbase
operator|.
name|catalog
operator|.
name|CatalogTracker
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
name|catalog
operator|.
name|MetaReader
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

begin_comment
comment|/**  * Test whether region rebalancing works. (HBASE-71)  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestRegionRebalancing
block|{
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
name|HTable
name|table
decl_stmt|;
name|HTableDescriptor
name|desc
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY_NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
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
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
block|{
name|this
operator|.
name|desc
operator|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|this
operator|.
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * For HBASE-71. Try a few different configurations of starting and stopping    * region servers to see if the assignment or regions is pretty balanced.    * @throws IOException    * @throws InterruptedException    */
annotation|@
name|Test
specifier|public
name|void
name|testRebalanceOnRegionServerNumberChange
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|this
operator|.
name|desc
argument_list|,
name|HBaseTestingUtility
operator|.
name|KEYS
argument_list|)
expr_stmt|;
name|this
operator|.
name|table
operator|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|this
operator|.
name|desc
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|CatalogTracker
name|ct
init|=
operator|new
name|CatalogTracker
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|ct
operator|.
name|start
argument_list|()
expr_stmt|;
try|try
block|{
name|MetaReader
operator|.
name|fullScanMetaAndPrint
argument_list|(
name|ct
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|ct
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Test table should have right number of regions"
argument_list|,
name|HBaseTestingUtility
operator|.
name|KEYS
operator|.
name|length
operator|+
literal|1
comment|/*One extra to account for start/end keys*/
argument_list|,
name|this
operator|.
name|table
operator|.
name|getStartKeys
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// verify that the region assignments are balanced to start out
name|assertRegionsAreBalanced
argument_list|()
expr_stmt|;
comment|// add a region server - total of 2
name|LOG
operator|.
name|info
argument_list|(
literal|"Started second server="
operator|+
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|startRegionServer
argument_list|()
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|balance
argument_list|()
expr_stmt|;
name|assertRegionsAreBalanced
argument_list|()
expr_stmt|;
comment|// add a region server - total of 3
name|LOG
operator|.
name|info
argument_list|(
literal|"Started third server="
operator|+
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|startRegionServer
argument_list|()
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|balance
argument_list|()
expr_stmt|;
name|assertRegionsAreBalanced
argument_list|()
expr_stmt|;
comment|// kill a region server - total of 2
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopped third server="
operator|+
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|stopRegionServer
argument_list|(
literal|2
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|waitOnRegionServer
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|balance
argument_list|()
expr_stmt|;
name|assertRegionsAreBalanced
argument_list|()
expr_stmt|;
comment|// start two more region servers - total of 4
name|LOG
operator|.
name|info
argument_list|(
literal|"Readding third server="
operator|+
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|startRegionServer
argument_list|()
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Added fourth server="
operator|+
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|startRegionServer
argument_list|()
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|balance
argument_list|()
expr_stmt|;
name|assertRegionsAreBalanced
argument_list|()
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
literal|6
condition|;
name|i
operator|++
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Adding "
operator|+
operator|(
name|i
operator|+
literal|5
operator|)
operator|+
literal|"th region server"
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|startRegionServer
argument_list|()
expr_stmt|;
block|}
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|balance
argument_list|()
expr_stmt|;
name|assertRegionsAreBalanced
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
comment|/**    * Determine if regions are balanced. Figure out the total, divide by the    * number of online servers, then test if each server is +/- 1 of average    * rounded up.    */
specifier|private
name|void
name|assertRegionsAreBalanced
parameter_list|()
throws|throws
name|IOException
block|{
comment|// TODO: Fix this test.  Old balancer used to run with 'slop'.  New
comment|// balancer does not.
name|boolean
name|success
init|=
literal|false
decl_stmt|;
name|float
name|slop
init|=
operator|(
name|float
operator|)
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getFloat
argument_list|(
literal|"hbase.regions.slop"
argument_list|,
literal|0.1f
argument_list|)
decl_stmt|;
if|if
condition|(
name|slop
operator|<=
literal|0
condition|)
name|slop
operator|=
literal|1
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
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|success
operator|=
literal|true
expr_stmt|;
comment|// make sure all the regions are reassigned before we test balance
name|waitForAllRegionsAssigned
argument_list|()
expr_stmt|;
name|int
name|regionCount
init|=
name|getRegionCount
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HRegionServer
argument_list|>
name|servers
init|=
name|getOnlineRegionServers
argument_list|()
decl_stmt|;
name|double
name|avg
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAverageLoad
argument_list|()
decl_stmt|;
name|int
name|avgLoadPlusSlop
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|ceil
argument_list|(
name|avg
operator|*
operator|(
literal|1
operator|+
name|slop
operator|)
argument_list|)
decl_stmt|;
name|int
name|avgLoadMinusSlop
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|floor
argument_list|(
name|avg
operator|*
operator|(
literal|1
operator|-
name|slop
operator|)
argument_list|)
operator|-
literal|1
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"There are "
operator|+
name|servers
operator|.
name|size
argument_list|()
operator|+
literal|" servers and "
operator|+
name|regionCount
operator|+
literal|" regions. Load Average: "
operator|+
name|avg
operator|+
literal|" low border: "
operator|+
name|avgLoadMinusSlop
operator|+
literal|", up border: "
operator|+
name|avgLoadPlusSlop
operator|+
literal|"; attempt: "
operator|+
name|i
argument_list|)
expr_stmt|;
for|for
control|(
name|HRegionServer
name|server
range|:
name|servers
control|)
block|{
name|int
name|serverLoad
init|=
name|server
operator|.
name|getOnlineRegions
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|server
operator|.
name|getServerName
argument_list|()
operator|+
literal|" Avg: "
operator|+
name|avg
operator|+
literal|" actual: "
operator|+
name|serverLoad
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
operator|(
name|avg
operator|>
literal|2.0
operator|&&
name|serverLoad
operator|<=
name|avgLoadPlusSlop
operator|&&
name|serverLoad
operator|>=
name|avgLoadMinusSlop
operator|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|server
operator|.
name|getServerName
argument_list|()
operator|+
literal|" Isn't balanced!!! Avg: "
operator|+
name|avg
operator|+
literal|" actual: "
operator|+
name|serverLoad
operator|+
literal|" slop: "
operator|+
name|slop
argument_list|)
expr_stmt|;
name|success
operator|=
literal|false
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|success
condition|)
block|{
comment|// one or more servers are not balanced. sleep a little to give it a
comment|// chance to catch up. then, go back to the retry loop.
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{}
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|balance
argument_list|()
expr_stmt|;
continue|continue;
block|}
comment|// if we get here, all servers were balanced, so we should just return.
return|return;
block|}
comment|// if we get here, we tried 5 times and never got to short circuit out of
comment|// the retry loop, so this is a failure.
name|fail
argument_list|(
literal|"After 5 attempts, region assignments were not balanced."
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
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
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
literal|22
condition|)
block|{
comment|// while (!cluster.getMaster().allRegionsAssigned()) {
name|LOG
operator|.
name|debug
argument_list|(
literal|"Waiting for there to be 22 regions, but there are "
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
literal|200
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

