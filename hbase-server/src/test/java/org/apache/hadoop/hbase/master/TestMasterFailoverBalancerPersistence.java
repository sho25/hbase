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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ClusterMetrics
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
name|MasterNotRunningException
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
name|StartMiniClusterOption
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
name|LargeTests
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
name|MasterTests
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestMasterFailoverBalancerPersistence
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
name|TestMasterFailoverBalancerPersistence
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Test that if the master fails, the load balancer maintains its    * state (running or not) when the next master takes over    *    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testMasterFailoverBalancerPersistence
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Start the cluster
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
name|StartMiniClusterOption
name|option
init|=
name|StartMiniClusterOption
operator|.
name|builder
argument_list|()
operator|.
name|numMasters
argument_list|(
literal|3
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|option
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
name|assertTrue
argument_list|(
name|cluster
operator|.
name|waitForActiveAndReadyMaster
argument_list|()
argument_list|)
expr_stmt|;
name|HMaster
name|active
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
comment|// check that the balancer is on by default for the active master
name|ClusterMetrics
name|clusterStatus
init|=
name|active
operator|.
name|getClusterMetrics
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|clusterStatus
operator|.
name|getBalancerOn
argument_list|()
argument_list|)
expr_stmt|;
name|active
operator|=
name|killActiveAndWaitForNewActive
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
comment|// ensure the load balancer is still running on new master
name|clusterStatus
operator|=
name|active
operator|.
name|getClusterMetrics
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|clusterStatus
operator|.
name|getBalancerOn
argument_list|()
argument_list|)
expr_stmt|;
comment|// turn off the load balancer
name|active
operator|.
name|balanceSwitch
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// once more, kill active master and wait for new active master to show up
name|active
operator|=
name|killActiveAndWaitForNewActive
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
comment|// ensure the load balancer is not running on the new master
name|clusterStatus
operator|=
name|active
operator|.
name|getClusterMetrics
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|clusterStatus
operator|.
name|getBalancerOn
argument_list|()
argument_list|)
expr_stmt|;
comment|// Stop the cluster
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Kill the master and wait for a new active master to show up    *    * @param cluster    * @return the new active master    * @throws InterruptedException    * @throws java.io.IOException    */
specifier|private
name|HMaster
name|killActiveAndWaitForNewActive
parameter_list|(
name|MiniHBaseCluster
name|cluster
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
name|int
name|activeIndex
init|=
name|getActiveMasterIndex
argument_list|(
name|cluster
argument_list|)
decl_stmt|;
name|HMaster
name|active
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|cluster
operator|.
name|stopMaster
argument_list|(
name|activeIndex
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitOnMaster
argument_list|(
name|activeIndex
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cluster
operator|.
name|waitForActiveAndReadyMaster
argument_list|()
argument_list|)
expr_stmt|;
comment|// double check this is actually a new master
name|HMaster
name|newActive
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|active
operator|==
name|newActive
argument_list|)
expr_stmt|;
return|return
name|newActive
return|;
block|}
comment|/**    * return the index of the active master in the cluster    *    * @throws org.apache.hadoop.hbase.MasterNotRunningException    *          if no active master found    */
specifier|private
name|int
name|getActiveMasterIndex
parameter_list|(
name|MiniHBaseCluster
name|cluster
parameter_list|)
throws|throws
name|MasterNotRunningException
block|{
comment|// get all the master threads
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|MasterThread
argument_list|>
name|masterThreads
init|=
name|cluster
operator|.
name|getMasterThreads
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
name|masterThreads
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|masterThreads
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getMaster
argument_list|()
operator|.
name|isActiveMaster
argument_list|()
condition|)
block|{
return|return
name|i
return|;
block|}
block|}
throw|throw
operator|new
name|MasterNotRunningException
argument_list|()
throw|;
block|}
block|}
end_class

end_unit

