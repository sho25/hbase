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
name|balancer
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
name|assertNotNull
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
name|HDFSBlocksDistribution
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
name|ServerName
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
name|TestRegionLocationFinder
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
name|TestRegionLocationFinder
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|MiniHBaseCluster
name|cluster
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Table
name|table
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|int
name|ServerNum
init|=
literal|5
decl_stmt|;
specifier|private
specifier|static
name|RegionLocationFinder
name|finder
init|=
operator|new
name|RegionLocationFinder
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|cluster
operator|=
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|,
name|ServerNum
argument_list|)
expr_stmt|;
name|table
operator|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|FAMILY
argument_list|,
name|HBaseTestingUtility
operator|.
name|KEYS_FOR_HBA_CREATE_TABLE
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|tableName
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|loadTable
argument_list|(
name|table
argument_list|,
name|FAMILY
argument_list|)
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
name|ServerNum
condition|;
name|i
operator|++
control|)
block|{
name|HRegionServer
name|server
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
name|i
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegion
name|region
range|:
name|server
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
control|)
block|{
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
name|finder
operator|.
name|setConf
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|finder
operator|.
name|setServices
argument_list|(
name|cluster
operator|.
name|getMaster
argument_list|()
argument_list|)
expr_stmt|;
name|finder
operator|.
name|setClusterMetrics
argument_list|(
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|getClusterMetrics
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
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
name|testInternalGetTopBlockLocation
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ServerNum
condition|;
name|i
operator|++
control|)
block|{
name|HRegionServer
name|server
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
name|i
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegion
name|region
range|:
name|server
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
control|)
block|{
comment|// get region's hdfs block distribution by region and RegionLocationFinder,
comment|// they should have same result
name|HDFSBlocksDistribution
name|blocksDistribution1
init|=
name|region
operator|.
name|getHDFSBlocksDistribution
argument_list|()
decl_stmt|;
name|HDFSBlocksDistribution
name|blocksDistribution2
init|=
name|finder
operator|.
name|getBlockDistribution
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|blocksDistribution1
operator|.
name|getUniqueBlocksTotalWeight
argument_list|()
argument_list|,
name|blocksDistribution2
operator|.
name|getUniqueBlocksTotalWeight
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|blocksDistribution1
operator|.
name|getUniqueBlocksTotalWeight
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|assertEquals
argument_list|(
name|blocksDistribution1
operator|.
name|getTopHosts
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|blocksDistribution2
operator|.
name|getTopHosts
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMapHostNameToServerName
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|topHosts
init|=
operator|new
name|ArrayList
argument_list|<>
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
name|ServerNum
condition|;
name|i
operator|++
control|)
block|{
name|HRegionServer
name|server
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|String
name|serverHost
init|=
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|getHostname
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|topHosts
operator|.
name|contains
argument_list|(
name|serverHost
argument_list|)
condition|)
block|{
name|topHosts
operator|.
name|add
argument_list|(
name|serverHost
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
init|=
name|finder
operator|.
name|mapHostNameToServerName
argument_list|(
name|topHosts
argument_list|)
decl_stmt|;
comment|// mini cluster, all rs in one host
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|topHosts
operator|.
name|size
argument_list|()
argument_list|)
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
name|ServerNum
condition|;
name|i
operator|++
control|)
block|{
name|ServerName
name|server
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
name|i
argument_list|)
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|servers
operator|.
name|contains
argument_list|(
name|server
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetTopBlockLocations
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ServerNum
condition|;
name|i
operator|++
control|)
block|{
name|HRegionServer
name|server
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
name|i
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegion
name|region
range|:
name|server
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
control|)
block|{
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
init|=
name|finder
operator|.
name|getTopBlockLocations
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
decl_stmt|;
comment|// test table may have empty region
if|if
condition|(
name|region
operator|.
name|getHDFSBlocksDistribution
argument_list|()
operator|.
name|getUniqueBlocksTotalWeight
argument_list|()
operator|==
literal|0
condition|)
block|{
continue|continue;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|topHosts
init|=
name|region
operator|.
name|getHDFSBlocksDistribution
argument_list|()
operator|.
name|getTopHosts
argument_list|()
decl_stmt|;
comment|// rs and datanode may have different host in local machine test
if|if
condition|(
operator|!
name|topHosts
operator|.
name|contains
argument_list|(
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|getHostname
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|ServerNum
condition|;
name|j
operator|++
control|)
block|{
name|ServerName
name|serverName
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
name|j
argument_list|)
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|servers
operator|.
name|contains
argument_list|(
name|serverName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRefreshAndWait
parameter_list|()
throws|throws
name|Exception
block|{
name|finder
operator|.
name|getCache
argument_list|()
operator|.
name|invalidateAll
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
name|ServerNum
condition|;
name|i
operator|++
control|)
block|{
name|HRegionServer
name|server
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
init|=
name|server
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|regions
operator|.
name|size
argument_list|()
operator|<=
literal|0
condition|)
block|{
continue|continue;
block|}
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regionInfos
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|regions
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegion
name|region
range|:
name|regions
control|)
block|{
name|regionInfos
operator|.
name|add
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|finder
operator|.
name|refreshAndWait
argument_list|(
name|regionInfos
argument_list|)
expr_stmt|;
for|for
control|(
name|RegionInfo
name|regionInfo
range|:
name|regionInfos
control|)
block|{
name|assertNotNull
argument_list|(
name|finder
operator|.
name|getCache
argument_list|()
operator|.
name|getIfPresent
argument_list|(
name|regionInfo
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

