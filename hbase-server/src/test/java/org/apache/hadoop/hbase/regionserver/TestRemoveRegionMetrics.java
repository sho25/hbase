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
name|regionserver
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
name|CompatibilityFactory
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
name|NamespaceDescriptor
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
name|Admin
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
name|RegionLocator
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
name|test
operator|.
name|MetricsAssertHelper
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
name|Threads
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
name|TestRemoveRegionMetrics
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
name|TestRemoveRegionMetrics
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|MiniHBaseCluster
name|cluster
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
decl_stmt|;
specifier|private
specifier|static
name|MetricsAssertHelper
name|metricsHelper
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
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|startCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|metricsHelper
operator|=
name|CompatibilityFactory
operator|.
name|getInstance
argument_list|(
name|MetricsAssertHelper
operator|.
name|class
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.splitlog.max.resubmit"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// Make the failure test faster
name|conf
operator|.
name|setInt
argument_list|(
literal|"zookeeper.recovery.retry"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|REGIONSERVER_INFO_PORT
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|cluster
operator|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
expr_stmt|;
name|cluster
operator|.
name|waitForActiveAndReadyMaster
argument_list|()
expr_stmt|;
while|while
condition|(
name|cluster
operator|.
name|getLiveRegionServerThreads
argument_list|()
operator|.
name|size
argument_list|()
operator|<
literal|2
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMoveRegion
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|String
name|tableNameString
init|=
name|name
operator|.
name|getMethodName
argument_list|()
decl_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableNameString
argument_list|)
decl_stmt|;
name|Table
name|t
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"D"
argument_list|)
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|RegionInfo
name|regionInfo
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r1"
argument_list|)
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
literal|30
condition|;
name|i
operator|++
control|)
block|{
name|boolean
name|moved
init|=
literal|false
decl_stmt|;
try|try
init|(
name|RegionLocator
name|locator
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|regionInfo
operator|=
name|locator
operator|.
name|getRegionLocation
argument_list|(
name|row
argument_list|,
literal|true
argument_list|)
operator|.
name|getRegion
argument_list|()
expr_stmt|;
block|}
name|int
name|currentServerIdx
init|=
name|cluster
operator|.
name|getServerWith
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|destServerIdx
init|=
operator|(
name|currentServerIdx
operator|+
literal|1
operator|)
operator|%
name|cluster
operator|.
name|getLiveRegionServerThreads
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|HRegionServer
name|currentServer
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
name|currentServerIdx
argument_list|)
decl_stmt|;
name|HRegionServer
name|destServer
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
name|destServerIdx
argument_list|)
decl_stmt|;
comment|// Do a put. The counters should be non-zero now
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"D"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Zero"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"VALUE"
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|MetricsRegionAggregateSource
name|currentAgg
init|=
name|currentServer
operator|.
name|getRegion
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
operator|.
name|getMetrics
argument_list|()
operator|.
name|getSource
argument_list|()
operator|.
name|getAggregateSource
argument_list|()
decl_stmt|;
name|String
name|prefix
init|=
literal|"namespace_"
operator|+
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE_NAME_STR
operator|+
literal|"_table_"
operator|+
name|tableNameString
operator|+
literal|"_region_"
operator|+
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|"_metric"
decl_stmt|;
name|metricsHelper
operator|.
name|assertCounter
argument_list|(
name|prefix
operator|+
literal|"_putCount"
argument_list|,
literal|1
argument_list|,
name|currentAgg
argument_list|)
expr_stmt|;
try|try
block|{
name|TEST_UTIL
operator|.
name|moveRegionAndWait
argument_list|(
name|regionInfo
argument_list|,
name|destServer
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|moved
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|moved
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|moved
condition|)
block|{
name|MetricsRegionAggregateSource
name|destAgg
init|=
name|destServer
operator|.
name|getRegion
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
operator|.
name|getMetrics
argument_list|()
operator|.
name|getSource
argument_list|()
operator|.
name|getAggregateSource
argument_list|()
decl_stmt|;
name|metricsHelper
operator|.
name|assertCounter
argument_list|(
name|prefix
operator|+
literal|"_putCount"
argument_list|,
literal|0
argument_list|,
name|destAgg
argument_list|)
expr_stmt|;
block|}
block|}
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

