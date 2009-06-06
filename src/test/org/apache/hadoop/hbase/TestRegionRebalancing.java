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
name|hbase
operator|.
name|io
operator|.
name|BatchUpdate
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

begin_comment
comment|/**  * Test whether region rebalancing works. (HBASE-71)  */
end_comment

begin_class
specifier|public
class|class
name|TestRegionRebalancing
extends|extends
name|HBaseClusterTestCase
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
name|HTable
name|table
decl_stmt|;
name|HTableDescriptor
name|desc
decl_stmt|;
specifier|final
name|byte
index|[]
name|FIVE_HUNDRED_KBYTES
decl_stmt|;
specifier|final
name|byte
index|[]
name|COLUMN_NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col:"
argument_list|)
decl_stmt|;
comment|/** constructor */
specifier|public
name|TestRegionRebalancing
parameter_list|()
block|{
name|super
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|FIVE_HUNDRED_KBYTES
operator|=
operator|new
name|byte
index|[
literal|500
operator|*
literal|1024
index|]
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
literal|500
operator|*
literal|1024
condition|;
name|i
operator|++
control|)
block|{
name|FIVE_HUNDRED_KBYTES
index|[
name|i
index|]
operator|=
literal|'x'
expr_stmt|;
block|}
name|desc
operator|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|COLUMN_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Before the hbase cluster starts up, create some dummy regions.    */
annotation|@
name|Override
specifier|public
name|void
name|preHBaseClusterSetup
parameter_list|()
throws|throws
name|IOException
block|{
comment|// create a 20-region table by writing directly to disk
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|startKeys
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|startKeys
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|10
init|;
name|i
operator|<
literal|29
condition|;
name|i
operator|++
control|)
block|{
name|startKeys
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|startKeys
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|startKeys
operator|.
name|size
argument_list|()
operator|+
literal|" start keys generated"
argument_list|)
expr_stmt|;
name|List
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
literal|20
condition|;
name|i
operator|++
control|)
block|{
name|regions
operator|.
name|add
argument_list|(
name|createAregion
argument_list|(
name|startKeys
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|startKeys
operator|.
name|get
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Now create the root and meta regions and insert the data regions
comment|// created above into the meta
name|createRootAndMetaRegions
argument_list|()
expr_stmt|;
for|for
control|(
name|HRegion
name|region
range|:
name|regions
control|)
block|{
name|HRegion
operator|.
name|addRegionToMETA
argument_list|(
name|meta
argument_list|,
name|region
argument_list|)
expr_stmt|;
block|}
name|closeRootAndMeta
argument_list|()
expr_stmt|;
block|}
comment|/**    * For HBASE-71. Try a few different configurations of starting and stopping    * region servers to see if the assignment or regions is pretty balanced.    * @throws IOException     */
specifier|public
name|void
name|testRebalancing
parameter_list|()
throws|throws
name|IOException
block|{
name|table
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Test table should have 20 regions"
argument_list|,
literal|20
argument_list|,
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
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding 2nd region server."
argument_list|)
expr_stmt|;
comment|// add a region server - total of 2
name|cluster
operator|.
name|startRegionServer
argument_list|()
expr_stmt|;
name|assertRegionsAreBalanced
argument_list|()
expr_stmt|;
comment|// add a region server - total of 3
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding 3rd region server."
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|startRegionServer
argument_list|()
expr_stmt|;
name|assertRegionsAreBalanced
argument_list|()
expr_stmt|;
comment|// kill a region server - total of 2
name|LOG
operator|.
name|debug
argument_list|(
literal|"Killing the 3rd region server."
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|stopRegionServer
argument_list|(
literal|2
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitOnRegionServer
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertRegionsAreBalanced
argument_list|()
expr_stmt|;
comment|// start two more region servers - total of 4
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding 3rd region server"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|startRegionServer
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding 4th region server"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|startRegionServer
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
name|debug
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
name|cluster
operator|.
name|startRegionServer
argument_list|()
expr_stmt|;
block|}
name|assertRegionsAreBalanced
argument_list|()
expr_stmt|;
block|}
comment|/** figure out how many regions are currently being served. */
specifier|private
name|int
name|getRegionCount
parameter_list|()
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
block|{
name|boolean
name|success
init|=
literal|false
decl_stmt|;
name|float
name|slop
init|=
name|conf
operator|.
name|getFloat
argument_list|(
literal|"hbase.regions.slop"
argument_list|,
operator|(
name|float
operator|)
literal|0.1
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
name|cluster
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
name|hashCode
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
name|hashCode
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
name|LocalHBaseCluster
operator|.
name|RegionServerThread
name|rst
range|:
name|cluster
operator|.
name|getRegionThreads
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
comment|/**    * Wait until all the regions are assigned.     */
specifier|private
name|void
name|waitForAllRegionsAssigned
parameter_list|()
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
literal|1000
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
comment|/**    * create a region with the specified start and end key and exactly one row    * inside.     */
specifier|private
name|HRegion
name|createAregion
parameter_list|(
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegion
name|region
init|=
name|createNewHRegion
argument_list|(
name|desc
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|)
decl_stmt|;
name|byte
index|[]
name|keyToWrite
init|=
name|startKey
operator|==
literal|null
condition|?
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_000"
argument_list|)
else|:
name|startKey
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|keyToWrite
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|famAndQf
init|=
name|KeyValue
operator|.
name|parseColumn
argument_list|(
name|COLUMN_NAME
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|famAndQf
index|[
literal|0
index|]
argument_list|,
name|famAndQf
index|[
literal|1
index|]
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
name|region
operator|.
name|getLog
argument_list|()
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
return|return
name|region
return|;
block|}
block|}
end_class

end_unit

