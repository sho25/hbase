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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|AtomicInteger
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
name|util
operator|.
name|Bytes
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
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Ignore
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
name|Ignore
comment|// SimpleLoadBalancer seems borked whether AMv2 or not. Disabling till gets attention.
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
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
name|TestMasterBalanceThrottling
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
name|TestMasterBalanceThrottling
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
name|byte
index|[]
name|FAMILYNAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
argument_list|)
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setupConfiguration
parameter_list|()
block|{
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_MASTER_LOADBALANCER_CLASS
argument_list|,
literal|"org.apache.hadoop.hbase.master.balancer.SimpleLoadBalancer"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|shutdown
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_BALANCER_MAX_BALANCING
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_BALANCER_PERIOD
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setDouble
argument_list|(
name|HConstants
operator|.
name|HBASE_MASTER_BALANCER_MAX_RIT_PERCENT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_MASTER_BALANCER_MAX_RIT_PERCENT
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testThrottlingByBalanceInterval
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Use default config and start a cluster of two regionservers.
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|TableName
name|tableName
init|=
name|createTable
argument_list|(
literal|"testNoThrottling"
argument_list|)
decl_stmt|;
specifier|final
name|HMaster
name|master
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
comment|// Default max balancing time is 300000 ms and there are 50 regions to balance
comment|// The balance interval is 6000 ms, much longger than the normal region in transition duration
comment|// So the master can balance the region one by one
name|unbalance
argument_list|(
name|master
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|AtomicInteger
name|maxCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|AtomicBoolean
name|stop
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|Thread
name|checker
init|=
name|startBalancerChecker
argument_list|(
name|master
argument_list|,
name|maxCount
argument_list|,
name|stop
argument_list|)
decl_stmt|;
name|master
operator|.
name|balance
argument_list|()
expr_stmt|;
name|stop
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|checker
operator|.
name|interrupt
argument_list|()
expr_stmt|;
name|checker
operator|.
name|join
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
literal|"max regions in transition: "
operator|+
name|maxCount
operator|.
name|get
argument_list|()
argument_list|,
name|maxCount
operator|.
name|get
argument_list|()
operator|==
literal|1
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testThrottlingByMaxRitPercent
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Set max balancing time to 500 ms and max percent of regions in transition to 0.05
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_BALANCER_MAX_BALANCING
argument_list|,
literal|500
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setDouble
argument_list|(
name|HConstants
operator|.
name|HBASE_MASTER_BALANCER_MAX_RIT_PERCENT
argument_list|,
literal|0.05
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|TableName
name|tableName
init|=
name|createTable
argument_list|(
literal|"testThrottlingByMaxRitPercent"
argument_list|)
decl_stmt|;
specifier|final
name|HMaster
name|master
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|unbalance
argument_list|(
name|master
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|AtomicInteger
name|maxCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|AtomicBoolean
name|stop
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|Thread
name|checker
init|=
name|startBalancerChecker
argument_list|(
name|master
argument_list|,
name|maxCount
argument_list|,
name|stop
argument_list|)
decl_stmt|;
name|master
operator|.
name|balance
argument_list|()
expr_stmt|;
name|stop
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|checker
operator|.
name|interrupt
argument_list|()
expr_stmt|;
name|checker
operator|.
name|join
argument_list|()
expr_stmt|;
comment|// The max number of regions in transition is 100 * 0.05 = 5
name|assertTrue
argument_list|(
literal|"max regions in transition: "
operator|+
name|maxCount
operator|.
name|get
argument_list|()
argument_list|,
name|maxCount
operator|.
name|get
argument_list|()
operator|==
literal|5
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|TableName
name|createTable
parameter_list|(
name|String
name|table
parameter_list|)
throws|throws
name|IOException
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|byte
index|[]
name|startKey
init|=
operator|new
name|byte
index|[]
block|{
literal|0x00
block|}
decl_stmt|;
name|byte
index|[]
name|stopKey
init|=
operator|new
name|byte
index|[]
block|{
literal|0x7f
block|}
decl_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|FAMILYNAME
block|}
argument_list|,
literal|1
argument_list|,
name|startKey
argument_list|,
name|stopKey
argument_list|,
literal|100
argument_list|)
expr_stmt|;
return|return
name|tableName
return|;
block|}
specifier|private
name|Thread
name|startBalancerChecker
parameter_list|(
specifier|final
name|HMaster
name|master
parameter_list|,
specifier|final
name|AtomicInteger
name|maxCount
parameter_list|,
specifier|final
name|AtomicBoolean
name|stop
parameter_list|)
block|{
name|Runnable
name|checker
init|=
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
operator|!
name|stop
operator|.
name|get
argument_list|()
condition|)
block|{
name|maxCount
operator|.
name|set
argument_list|(
name|Math
operator|.
name|max
argument_list|(
name|maxCount
operator|.
name|get
argument_list|()
argument_list|,
name|master
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionsInTransitionCount
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
decl_stmt|;
name|Thread
name|thread
init|=
operator|new
name|Thread
argument_list|(
name|checker
argument_list|)
decl_stmt|;
name|thread
operator|.
name|start
argument_list|()
expr_stmt|;
return|return
name|thread
return|;
block|}
specifier|private
name|void
name|unbalance
parameter_list|(
name|HMaster
name|master
parameter_list|,
name|TableName
name|tableName
parameter_list|)
throws|throws
name|Exception
block|{
while|while
condition|(
name|master
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionsInTransitionCount
argument_list|()
operator|>
literal|0
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|HRegionServer
name|biasedServer
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|regionInfo
range|:
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableRegions
argument_list|(
name|tableName
argument_list|)
control|)
block|{
name|master
operator|.
name|move
argument_list|(
name|regionInfo
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|biasedServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
while|while
condition|(
name|master
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionsInTransitionCount
argument_list|()
operator|>
literal|0
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

