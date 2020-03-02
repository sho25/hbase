begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|fail
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|AbstractMap
operator|.
name|SimpleImmutableEntry
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
name|ColumnFamilyDescriptorBuilder
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
name|TableDescriptorBuilder
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
name|assignment
operator|.
name|RegionStates
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
name|quotas
operator|.
name|SpaceQuotaSnapshot
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
name|quotas
operator|.
name|SpaceQuotaSnapshot
operator|.
name|SpaceQuotaStatus
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
name|quotas
operator|.
name|SpaceViolationPolicy
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|PairOfSameType
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

begin_class
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
name|TestMasterMetricsWrapper
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
name|TestMasterMetricsWrapper
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
name|TestMasterMetricsWrapper
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
name|int
name|NUM_RS
init|=
literal|4
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|NUM_RS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|teardown
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
name|Test
specifier|public
name|void
name|testInfo
parameter_list|()
block|{
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
name|MetricsMasterWrapperImpl
name|info
init|=
operator|new
name|MetricsMasterWrapperImpl
argument_list|(
name|master
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|master
operator|.
name|getSplitPlanCount
argument_list|()
argument_list|,
name|info
operator|.
name|getSplitPlanCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|master
operator|.
name|getMergePlanCount
argument_list|()
argument_list|,
name|info
operator|.
name|getMergePlanCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|master
operator|.
name|getAverageLoad
argument_list|()
argument_list|,
name|info
operator|.
name|getAverageLoad
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|master
operator|.
name|getClusterId
argument_list|()
argument_list|,
name|info
operator|.
name|getClusterId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|master
operator|.
name|getMasterActiveTime
argument_list|()
argument_list|,
name|info
operator|.
name|getActiveTime
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|master
operator|.
name|getMasterStartTime
argument_list|()
argument_list|,
name|info
operator|.
name|getStartTime
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|master
operator|.
name|getMasterCoprocessors
argument_list|()
operator|.
name|length
argument_list|,
name|info
operator|.
name|getCoprocessors
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServersList
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|info
operator|.
name|getNumRegionServers
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|regionServerCount
init|=
name|NUM_RS
operator|+
operator|(
name|LoadBalancer
operator|.
name|isTablesOnMaster
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
condition|?
literal|1
else|:
literal|0
operator|)
decl_stmt|;
name|assertEquals
argument_list|(
name|regionServerCount
argument_list|,
name|info
operator|.
name|getNumRegionServers
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|zkServers
init|=
name|info
operator|.
name|getZookeeperQuorum
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|zkServers
operator|.
name|split
argument_list|(
literal|","
argument_list|)
operator|.
name|length
argument_list|,
name|TEST_UTIL
operator|.
name|getZkCluster
argument_list|()
operator|.
name|getZooKeeperServerNum
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|int
name|index
init|=
literal|3
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping "
operator|+
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
name|index
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|stopRegionServer
argument_list|(
name|index
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|waitOnRegionServer
argument_list|(
name|index
argument_list|)
expr_stmt|;
comment|// We stopped the regionserver but could take a while for the master to notice it so hang here
comment|// until it does... then move forward to see if metrics wrapper notices.
while|while
condition|(
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServers
argument_list|()
operator|.
name|size
argument_list|()
operator|==
name|regionServerCount
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
name|assertEquals
argument_list|(
name|regionServerCount
operator|-
literal|1
argument_list|,
name|info
operator|.
name|getNumRegionServers
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|info
operator|.
name|getNumDeadRegionServers
argument_list|()
argument_list|)
expr_stmt|;
comment|// now we do not expose this information as WALProcedureStore is not the only ProcedureStore
comment|// implementation any more.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|info
operator|.
name|getNumWALFiles
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testQuotaSnapshotConversion
parameter_list|()
block|{
name|MetricsMasterWrapperImpl
name|info
init|=
operator|new
name|MetricsMasterWrapperImpl
argument_list|(
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
operator|new
name|SimpleImmutableEntry
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
argument_list|(
literal|1024L
argument_list|,
literal|2048L
argument_list|)
argument_list|,
name|info
operator|.
name|convertSnapshot
argument_list|(
operator|new
name|SpaceQuotaSnapshot
argument_list|(
name|SpaceQuotaStatus
operator|.
name|notInViolation
argument_list|()
argument_list|,
literal|1024L
argument_list|,
literal|2048L
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|SimpleImmutableEntry
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
argument_list|(
literal|4096L
argument_list|,
literal|2048L
argument_list|)
argument_list|,
name|info
operator|.
name|convertSnapshot
argument_list|(
operator|new
name|SpaceQuotaSnapshot
argument_list|(
operator|new
name|SpaceQuotaStatus
argument_list|(
name|SpaceViolationPolicy
operator|.
name|NO_INSERTS
argument_list|)
argument_list|,
literal|4096L
argument_list|,
literal|2048L
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * tests online and offline region number    */
annotation|@
name|Test
specifier|public
name|void
name|testOfflineRegion
parameter_list|()
throws|throws
name|Exception
block|{
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
name|MetricsMasterWrapperImpl
name|info
init|=
operator|new
name|MetricsMasterWrapperImpl
argument_list|(
name|master
argument_list|)
decl_stmt|;
name|TableName
name|table
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testRegionNumber"
argument_list|)
decl_stmt|;
try|try
block|{
name|RegionInfo
name|hri
decl_stmt|;
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
name|tableDescriptor
init|=
operator|new
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"FAMILY"
argument_list|)
decl_stmt|;
name|tableDescriptor
operator|.
name|setColumnFamily
argument_list|(
operator|new
name|ColumnFamilyDescriptorBuilder
operator|.
name|ModifyableColumnFamilyDescriptor
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|tableDescriptor
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Z"
argument_list|)
argument_list|,
literal|5
argument_list|)
expr_stmt|;
comment|// wait till the table is assigned
name|long
name|timeoutTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
literal|1000
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regions
init|=
name|master
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionsOfTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
if|if
condition|(
name|regions
operator|.
name|size
argument_list|()
operator|>
literal|3
condition|)
block|{
name|hri
operator|=
name|regions
operator|.
name|get
argument_list|(
literal|2
argument_list|)
expr_stmt|;
break|break;
block|}
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
if|if
condition|(
name|now
operator|>
name|timeoutTime
condition|)
block|{
name|fail
argument_list|(
literal|"Could not find an online region"
argument_list|)
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
name|PairOfSameType
argument_list|<
name|Integer
argument_list|>
name|regionNumberPair
init|=
name|info
operator|.
name|getRegionCounts
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|regionNumberPair
operator|.
name|getFirst
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|regionNumberPair
operator|.
name|getSecond
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|offline
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
name|timeoutTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
literal|800
expr_stmt|;
name|RegionStates
name|regionStates
init|=
name|master
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|regionStates
operator|.
name|getRegionByStateOfTable
argument_list|(
name|table
argument_list|)
operator|.
name|get
argument_list|(
name|RegionState
operator|.
name|State
operator|.
name|OFFLINE
argument_list|)
operator|.
name|contains
argument_list|(
name|hri
argument_list|)
condition|)
block|{
break|break;
block|}
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
if|if
condition|(
name|now
operator|>
name|timeoutTime
condition|)
block|{
name|fail
argument_list|(
literal|"Failed to offline the region in time"
argument_list|)
expr_stmt|;
break|break;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
name|regionNumberPair
operator|=
name|info
operator|.
name|getRegionCounts
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|regionNumberPair
operator|.
name|getFirst
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|regionNumberPair
operator|.
name|getSecond
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

