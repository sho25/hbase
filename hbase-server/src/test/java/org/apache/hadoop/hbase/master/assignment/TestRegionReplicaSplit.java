begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|RegionReplicaTestHelper
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
name|RegionServerTests
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
operator|.
name|RegionServerThread
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
name|RegionSplitter
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
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
name|TestRegionReplicaSplit
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
name|TestRegionReplicaSplit
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
name|TestRegionReplicaSplit
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NB_SERVERS
init|=
literal|4
decl_stmt|;
specifier|private
specifier|static
name|Table
name|table
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|HTU
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
name|f
init|=
name|HConstants
operator|.
name|CATALOG_FAMILY
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
name|HTU
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.master.wait.on.regionservers.mintostart"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|startMiniCluster
argument_list|(
name|NB_SERVERS
argument_list|)
expr_stmt|;
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|TestRegionReplicaSplit
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
comment|// Create table then get the single region for our new table.
name|createTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
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
specifier|static
name|void
name|createTable
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|TableDescriptorBuilder
name|builder
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|builder
operator|.
name|setRegionReplication
argument_list|(
literal|3
argument_list|)
expr_stmt|;
comment|// create a table with 3 replication
name|table
operator|=
name|HTU
operator|.
name|createTable
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|f
block|}
argument_list|,
name|getSplits
argument_list|(
literal|2
argument_list|)
argument_list|,
operator|new
name|Configuration
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|byte
index|[]
index|[]
name|getSplits
parameter_list|(
name|int
name|numRegions
parameter_list|)
block|{
name|RegionSplitter
operator|.
name|UniformSplit
name|split
init|=
operator|new
name|RegionSplitter
operator|.
name|UniformSplit
argument_list|()
decl_stmt|;
name|split
operator|.
name|setFirstRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|split
operator|.
name|setLastRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|split
operator|.
name|split
argument_list|(
name|numRegions
argument_list|)
return|;
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
name|HRegionServer
operator|.
name|TEST_SKIP_REPORTING_TRANSITION
operator|=
literal|false
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|HTU
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionReplicaSplitRegionAssignment
parameter_list|()
throws|throws
name|Exception
block|{
name|HTU
operator|.
name|loadNumericRows
argument_list|(
name|table
argument_list|,
name|f
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
comment|// split the table
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regions
init|=
operator|new
name|ArrayList
argument_list|<
name|RegionInfo
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|RegionServerThread
name|rs
range|:
name|HTU
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
control|)
block|{
for|for
control|(
name|Region
name|r
range|:
name|rs
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getRegions
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
control|)
block|{
name|regions
operator|.
name|add
argument_list|(
name|r
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// There are 6 regions before split, 9 regions after split.
name|HTU
operator|.
name|getAdmin
argument_list|()
operator|.
name|split
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
for|for
control|(
name|RegionServerThread
name|rs
range|:
name|HTU
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
control|)
block|{
for|for
control|(
name|Region
name|r
range|:
name|rs
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getRegions
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
control|)
block|{
name|count
operator|++
expr_stmt|;
block|}
block|}
if|if
condition|(
name|count
operator|>=
literal|9
condition|)
block|{
break|break;
block|}
name|count
operator|=
literal|0
expr_stmt|;
block|}
name|RegionReplicaTestHelper
operator|.
name|assertReplicaDistributed
argument_list|(
name|HTU
argument_list|,
name|table
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

