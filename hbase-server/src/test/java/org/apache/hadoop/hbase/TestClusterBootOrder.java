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
name|MasterThread
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
name|Before
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
comment|/**  * Tests the boot order indifference between regionserver and master  */
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
name|TestClusterBootOrder
block|{
specifier|private
specifier|static
specifier|final
name|long
name|SLEEP_INTERVAL
init|=
literal|1000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|SLEEP_TIME
init|=
literal|4000
decl_stmt|;
specifier|private
name|HBaseTestingUtility
name|testUtil
decl_stmt|;
specifier|private
name|LocalHBaseCluster
name|cluster
decl_stmt|;
specifier|private
name|RegionServerThread
name|rs
decl_stmt|;
specifier|private
name|MasterThread
name|master
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|testUtil
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|testUtil
operator|.
name|startMiniDFSCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|testUtil
operator|.
name|startMiniZKCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|testUtil
operator|.
name|createRootDir
argument_list|()
expr_stmt|;
comment|//manually setup hbase dir to point to minidfscluster
name|cluster
operator|=
operator|new
name|LocalHBaseCluster
argument_list|(
name|testUtil
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|cluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|cluster
operator|.
name|join
argument_list|()
expr_stmt|;
name|testUtil
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
name|testUtil
operator|.
name|shutdownMiniDFSCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|startRegionServer
parameter_list|()
throws|throws
name|Exception
block|{
name|rs
operator|=
name|cluster
operator|.
name|addRegionServer
argument_list|()
expr_stmt|;
name|rs
operator|.
name|start
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
operator|*
name|SLEEP_INTERVAL
operator|<
name|SLEEP_TIME
condition|;
name|i
operator|++
control|)
block|{
comment|//we cannot block on wait for rs at this point , since master is not up.
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_INTERVAL
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|rs
operator|.
name|isAlive
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|startMaster
parameter_list|()
throws|throws
name|Exception
block|{
name|master
operator|=
name|cluster
operator|.
name|addMaster
argument_list|()
expr_stmt|;
name|master
operator|.
name|start
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
operator|*
name|SLEEP_INTERVAL
operator|<
name|SLEEP_TIME
condition|;
name|i
operator|++
control|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_INTERVAL
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|master
operator|.
name|isAlive
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|waitForClusterOnline
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|master
operator|.
name|getMaster
argument_list|()
operator|.
name|isInitialized
argument_list|()
condition|)
block|{
break|break;
block|}
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
name|ignored
parameter_list|)
block|{
comment|// Keep waiting
block|}
block|}
name|rs
operator|.
name|waitForServerOnline
argument_list|()
expr_stmt|;
block|}
comment|/**    * Tests launching the cluster by first starting regionserver, and then the master    * to ensure that it does not matter which is started first.    */
annotation|@
name|Test
specifier|public
name|void
name|testBootRegionServerFirst
parameter_list|()
throws|throws
name|Exception
block|{
name|startRegionServer
argument_list|()
expr_stmt|;
name|startMaster
argument_list|()
expr_stmt|;
name|waitForClusterOnline
argument_list|()
expr_stmt|;
block|}
comment|/**    * Tests launching the cluster by first starting master, and then the regionserver    * to ensure that it does not matter which is started first.    */
annotation|@
name|Test
specifier|public
name|void
name|testBootMasterFirst
parameter_list|()
throws|throws
name|Exception
block|{
name|startMaster
argument_list|()
expr_stmt|;
name|startRegionServer
argument_list|()
expr_stmt|;
name|waitForClusterOnline
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

