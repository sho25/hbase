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
operator|.
name|assignment
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
name|Waiter
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
name|Get
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
name|zookeeper
operator|.
name|MiniZooKeeperCluster
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
name|Rule
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
name|junit
operator|.
name|rules
operator|.
name|TestName
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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterables
import|;
end_import

begin_comment
comment|/**  * Testcase for HBASE-20792.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|LargeTests
operator|.
name|class
block|,
name|MasterTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestRegionMoveAndAbandon
block|{
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
name|TestRegionMoveAndAbandon
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TestRegionMoveAndAbandon
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|private
name|HBaseTestingUtility
name|UTIL
decl_stmt|;
specifier|private
name|MiniHBaseCluster
name|cluster
decl_stmt|;
specifier|private
name|MiniZooKeeperCluster
name|zkCluster
decl_stmt|;
specifier|private
name|HRegionServer
name|rs1
decl_stmt|;
specifier|private
name|HRegionServer
name|rs2
decl_stmt|;
specifier|private
name|RegionInfo
name|regionInfo
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|zkCluster
operator|=
name|UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|cluster
operator|=
name|UTIL
operator|.
name|startMiniHBaseCluster
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|rs1
operator|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|rs2
operator|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|cluster
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// We'll use hbase:namespace for our testing
name|UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
argument_list|,
literal|30_000
argument_list|)
expr_stmt|;
name|regionInfo
operator|=
name|Iterables
operator|.
name|getOnlyElement
argument_list|(
name|cluster
operator|.
name|getRegions
argument_list|(
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
argument_list|)
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|teardown
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|cluster
operator|!=
literal|null
condition|)
block|{
name|cluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|cluster
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|zkCluster
operator|!=
literal|null
condition|)
block|{
name|zkCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|zkCluster
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Moving {} to {}"
argument_list|,
name|regionInfo
argument_list|,
name|rs2
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Move to RS2
name|UTIL
operator|.
name|moveRegionAndWait
argument_list|(
name|regionInfo
argument_list|,
name|rs2
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Moving {} to {}"
argument_list|,
name|regionInfo
argument_list|,
name|rs1
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Move to RS1
name|UTIL
operator|.
name|moveRegionAndWait
argument_list|(
name|regionInfo
argument_list|,
name|rs1
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Killing RS {}"
argument_list|,
name|rs1
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Stop RS1
name|cluster
operator|.
name|killRegionServer
argument_list|(
name|rs1
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Region should get moved to RS2
name|UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
argument_list|,
literal|30_000
argument_list|)
expr_stmt|;
comment|// Restart the master
name|LOG
operator|.
name|info
argument_list|(
literal|"Killing master {}"
argument_list|,
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|killMaster
argument_list|(
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Stop RS2
name|LOG
operator|.
name|info
argument_list|(
literal|"Killing RS {}"
argument_list|,
name|rs2
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|killRegionServer
argument_list|(
name|rs2
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Start up everything again
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting cluster"
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|startMaster
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|ensureSomeRegionServersAvailable
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|30_000
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|Table
name|nsTable
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
argument_list|)
init|)
block|{
comment|// Doesn't matter what we're getting. We just want to make sure we can access the region
name|nsTable
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

