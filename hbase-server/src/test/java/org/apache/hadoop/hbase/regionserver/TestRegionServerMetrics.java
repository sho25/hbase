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
name|*
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
name|*
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
name|apache
operator|.
name|log4j
operator|.
name|Level
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
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
name|TestRegionServerMetrics
block|{
specifier|private
specifier|static
name|MetricsAssertHelper
name|metricsHelper
decl_stmt|;
static|static
block|{
name|Logger
operator|.
name|getLogger
argument_list|(
literal|"org.apache.hadoop.hbase"
argument_list|)
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|DEBUG
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|MiniHBaseCluster
name|cluster
decl_stmt|;
specifier|private
specifier|static
name|HRegionServer
name|rs
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
name|MetricsRegionServer
name|metricsRegionServer
decl_stmt|;
specifier|private
specifier|static
name|MetricsRegionServerSource
name|serverSource
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
literal|1
argument_list|,
literal|1
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
literal|1
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
name|rs
operator|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|metricsRegionServer
operator|=
name|rs
operator|.
name|getRegionServerMetrics
argument_list|()
expr_stmt|;
name|serverSource
operator|=
name|metricsRegionServer
operator|.
name|getMetricsSource
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|after
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|TEST_UTIL
operator|!=
literal|null
condition|)
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testRegionCount
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|regionMetricsKey
init|=
literal|"regionCount"
decl_stmt|;
name|long
name|regions
init|=
name|metricsHelper
operator|.
name|getGaugeLong
argument_list|(
name|regionMetricsKey
argument_list|,
name|serverSource
argument_list|)
decl_stmt|;
comment|// Creating a table should add one region
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"table"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
argument_list|)
expr_stmt|;
name|metricsHelper
operator|.
name|assertGaugeGt
argument_list|(
name|regionMetricsKey
argument_list|,
name|regions
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLocalFiles
parameter_list|()
throws|throws
name|Exception
block|{
name|metricsHelper
operator|.
name|assertGauge
argument_list|(
literal|"percentFilesLocal"
argument_list|,
literal|0
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRequestCount
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|tableNameString
init|=
literal|"testRequestCount"
decl_stmt|;
name|byte
index|[]
name|tName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableNameString
argument_list|)
decl_stmt|;
name|byte
index|[]
name|cfName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"d"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rk"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|initValue
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Value"
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tName
argument_list|,
name|cfName
argument_list|)
expr_stmt|;
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tName
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
comment|//wait for the table to come up.
name|metricsRegionServer
operator|.
name|getRegionServerWrapper
argument_list|()
operator|.
name|forceRecompute
argument_list|()
expr_stmt|;
name|long
name|requests
init|=
name|metricsHelper
operator|.
name|getCounter
argument_list|(
literal|"totalRequestCount"
argument_list|,
name|serverSource
argument_list|)
decl_stmt|;
name|long
name|readRequests
init|=
name|metricsHelper
operator|.
name|getCounter
argument_list|(
literal|"readRequestCount"
argument_list|,
name|serverSource
argument_list|)
decl_stmt|;
name|long
name|writeRequests
init|=
name|metricsHelper
operator|.
name|getCounter
argument_list|(
literal|"writeRequestCount"
argument_list|,
name|serverSource
argument_list|)
decl_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tName
argument_list|)
decl_stmt|;
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
name|add
argument_list|(
name|cfName
argument_list|,
name|qualifier
argument_list|,
name|initValue
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
literal|30
condition|;
name|i
operator|++
control|)
block|{
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|row
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|table
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|HRegionInfo
name|i
range|:
name|table
operator|.
name|getRegionLocations
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
name|MetricsRegionAggregateSource
name|agg
init|=
name|rs
operator|.
name|getRegion
argument_list|(
name|i
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
name|i
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
literal|"_getNumOps"
argument_list|,
literal|10
argument_list|,
name|agg
argument_list|)
expr_stmt|;
name|metricsHelper
operator|.
name|assertCounter
argument_list|(
name|prefix
operator|+
literal|"_mutateCount"
argument_list|,
literal|30
argument_list|,
name|agg
argument_list|)
expr_stmt|;
block|}
name|metricsRegionServer
operator|.
name|getRegionServerWrapper
argument_list|()
operator|.
name|forceRecompute
argument_list|()
expr_stmt|;
name|metricsHelper
operator|.
name|assertCounterGt
argument_list|(
literal|"totalRequestCount"
argument_list|,
name|requests
operator|+
literal|39
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|metricsHelper
operator|.
name|assertCounterGt
argument_list|(
literal|"readRequestCount"
argument_list|,
name|readRequests
operator|+
literal|9
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|metricsHelper
operator|.
name|assertCounterGt
argument_list|(
literal|"writeRequestCount"
argument_list|,
name|writeRequests
operator|+
literal|29
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMutationsWithoutWal
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testMutationsWithoutWal"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|cf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"d"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rk"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Value"
argument_list|)
decl_stmt|;
name|metricsRegionServer
operator|.
name|getRegionServerWrapper
argument_list|()
operator|.
name|forceRecompute
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|cf
argument_list|)
expr_stmt|;
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
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
name|add
argument_list|(
name|cf
argument_list|,
name|qualifier
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|p
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|t
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
name|metricsRegionServer
operator|.
name|getRegionServerWrapper
argument_list|()
operator|.
name|forceRecompute
argument_list|()
expr_stmt|;
name|metricsHelper
operator|.
name|assertGauge
argument_list|(
literal|"mutationsWithoutWALCount"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|long
name|minLength
init|=
name|row
operator|.
name|length
operator|+
name|cf
operator|.
name|length
operator|+
name|qualifier
operator|.
name|length
operator|+
name|val
operator|.
name|length
decl_stmt|;
name|metricsHelper
operator|.
name|assertGaugeGt
argument_list|(
literal|"mutationsWithoutWALSize"
argument_list|,
name|minLength
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStoreCount
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testStoreCount"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|cf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"d"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rk"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Value"
argument_list|)
decl_stmt|;
name|metricsRegionServer
operator|.
name|getRegionServerWrapper
argument_list|()
operator|.
name|forceRecompute
argument_list|()
expr_stmt|;
name|long
name|stores
init|=
name|metricsHelper
operator|.
name|getGaugeLong
argument_list|(
literal|"storeCount"
argument_list|,
name|serverSource
argument_list|)
decl_stmt|;
name|long
name|storeFiles
init|=
name|metricsHelper
operator|.
name|getGaugeLong
argument_list|(
literal|"storeFileCount"
argument_list|,
name|serverSource
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|cf
argument_list|)
expr_stmt|;
comment|//Force a hfile.
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
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
name|add
argument_list|(
name|cf
argument_list|,
name|qualifier
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|t
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|metricsRegionServer
operator|.
name|getRegionServerWrapper
argument_list|()
operator|.
name|forceRecompute
argument_list|()
expr_stmt|;
name|metricsHelper
operator|.
name|assertGauge
argument_list|(
literal|"storeCount"
argument_list|,
name|stores
operator|+
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|metricsHelper
operator|.
name|assertGauge
argument_list|(
literal|"storeFileCount"
argument_list|,
name|storeFiles
operator|+
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCheckAndPutCount
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|tableNameString
init|=
literal|"testCheckAndPutCount"
decl_stmt|;
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableNameString
argument_list|)
decl_stmt|;
name|byte
index|[]
name|cf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"d"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rk"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|valOne
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Value"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|valTwo
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ValueTwo"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|valThree
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ValueThree"
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|cf
argument_list|)
expr_stmt|;
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
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
name|add
argument_list|(
name|cf
argument_list|,
name|qualifier
argument_list|,
name|valOne
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|t
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
name|Put
name|pTwo
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|pTwo
operator|.
name|add
argument_list|(
name|cf
argument_list|,
name|qualifier
argument_list|,
name|valTwo
argument_list|)
expr_stmt|;
name|t
operator|.
name|checkAndPut
argument_list|(
name|row
argument_list|,
name|cf
argument_list|,
name|qualifier
argument_list|,
name|valOne
argument_list|,
name|pTwo
argument_list|)
expr_stmt|;
name|t
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
name|Put
name|pThree
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|pThree
operator|.
name|add
argument_list|(
name|cf
argument_list|,
name|qualifier
argument_list|,
name|valThree
argument_list|)
expr_stmt|;
name|t
operator|.
name|checkAndPut
argument_list|(
name|row
argument_list|,
name|cf
argument_list|,
name|qualifier
argument_list|,
name|valOne
argument_list|,
name|pThree
argument_list|)
expr_stmt|;
name|t
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
name|metricsRegionServer
operator|.
name|getRegionServerWrapper
argument_list|()
operator|.
name|forceRecompute
argument_list|()
expr_stmt|;
name|metricsHelper
operator|.
name|assertCounter
argument_list|(
literal|"checkMutateFailedCount"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|metricsHelper
operator|.
name|assertCounter
argument_list|(
literal|"checkMutatePassedCount"
argument_list|,
literal|1
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIncrement
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|tableNameString
init|=
literal|"testIncrement"
decl_stmt|;
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableNameString
argument_list|)
decl_stmt|;
name|byte
index|[]
name|cf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"d"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rk"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0l
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|cf
argument_list|)
expr_stmt|;
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
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
name|add
argument_list|(
name|cf
argument_list|,
name|qualifier
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|t
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|count
init|=
literal|0
init|;
name|count
operator|<
literal|13
condition|;
name|count
operator|++
control|)
block|{
name|Increment
name|inc
init|=
operator|new
name|Increment
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|inc
operator|.
name|addColumn
argument_list|(
name|cf
argument_list|,
name|qualifier
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|t
operator|.
name|increment
argument_list|(
name|inc
argument_list|)
expr_stmt|;
block|}
name|t
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
name|metricsRegionServer
operator|.
name|getRegionServerWrapper
argument_list|()
operator|.
name|forceRecompute
argument_list|()
expr_stmt|;
name|metricsHelper
operator|.
name|assertCounter
argument_list|(
literal|"incrementNumOps"
argument_list|,
literal|13
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAppend
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|tableNameString
init|=
literal|"testAppend"
decl_stmt|;
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableNameString
argument_list|)
decl_stmt|;
name|byte
index|[]
name|cf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"d"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rk"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"One"
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|cf
argument_list|)
expr_stmt|;
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
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
name|add
argument_list|(
name|cf
argument_list|,
name|qualifier
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|t
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|count
init|=
literal|0
init|;
name|count
operator|<
literal|73
condition|;
name|count
operator|++
control|)
block|{
name|Append
name|append
init|=
operator|new
name|Append
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|append
operator|.
name|add
argument_list|(
name|cf
argument_list|,
name|qualifier
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|",Test"
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|append
argument_list|(
name|append
argument_list|)
expr_stmt|;
block|}
name|t
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
name|metricsRegionServer
operator|.
name|getRegionServerWrapper
argument_list|()
operator|.
name|forceRecompute
argument_list|()
expr_stmt|;
name|metricsHelper
operator|.
name|assertCounter
argument_list|(
literal|"appendNumOps"
argument_list|,
literal|73
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScanNext
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|tableNameString
init|=
literal|"testScanNext"
decl_stmt|;
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableNameString
argument_list|)
decl_stmt|;
name|byte
index|[]
name|cf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"d"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"One"
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|cf
argument_list|)
expr_stmt|;
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|t
operator|.
name|setAutoFlush
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|insertCount
init|=
literal|0
init|;
name|insertCount
operator|<
literal|100
condition|;
name|insertCount
operator|++
control|)
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|""
operator|+
name|insertCount
operator|+
literal|"row"
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|cf
argument_list|,
name|qualifier
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|t
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|s
operator|.
name|setBatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|s
operator|.
name|setCaching
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|ResultScanner
name|resultScanners
init|=
name|t
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|nextCount
init|=
literal|0
init|;
name|nextCount
operator|<
literal|30
condition|;
name|nextCount
operator|++
control|)
block|{
name|Result
name|result
init|=
name|resultScanners
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|HRegionInfo
name|i
range|:
name|t
operator|.
name|getRegionLocations
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
name|MetricsRegionAggregateSource
name|agg
init|=
name|rs
operator|.
name|getRegion
argument_list|(
name|i
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
name|i
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
literal|"_scanNextNumOps"
argument_list|,
literal|30
argument_list|,
name|agg
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

