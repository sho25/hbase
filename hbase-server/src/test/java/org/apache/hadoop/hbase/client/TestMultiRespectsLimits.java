begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
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
name|HColumnDescriptor
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
name|HTableDescriptor
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
name|io
operator|.
name|encoding
operator|.
name|DataBlockEncoding
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
name|ipc
operator|.
name|RpcServerInterface
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
name|metrics
operator|.
name|BaseSource
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
name|ClientTests
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadLocalRandom
import|;
end_import

begin_import
import|import static
name|junit
operator|.
name|framework
operator|.
name|TestCase
operator|.
name|assertEquals
import|;
end_import

begin_comment
comment|/**  * This test sets the multi size WAAAAAY low and then checks to make sure that gets will still make  * progress.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestMultiRespectsLimits
block|{
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
specifier|final
name|MetricsAssertHelper
name|METRICS_ASSERT
init|=
name|CompatibilityFactory
operator|.
name|getInstance
argument_list|(
name|MetricsAssertHelper
operator|.
name|class
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
literal|"D"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|MAX_SIZE
init|=
literal|500
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
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|HBASE_SERVER_SCANNER_MAX_RESULT_SIZE_KEY
argument_list|,
name|MAX_SIZE
argument_list|)
expr_stmt|;
comment|// Only start on regionserver so that all regions are on the same server.
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
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
name|testMultiLimits
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|name
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testMultiLimits"
argument_list|)
decl_stmt|;
name|Table
name|t
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|name
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|loadTable
argument_list|(
name|t
argument_list|,
name|FAMILY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Split the table to make sure that the chunking happens accross regions.
try|try
init|(
specifier|final
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin
operator|.
name|split
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|60000
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
return|return
name|admin
operator|.
name|getTableRegions
argument_list|(
name|name
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|1
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Get
argument_list|>
name|gets
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|MAX_SIZE
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
name|MAX_SIZE
condition|;
name|i
operator|++
control|)
block|{
name|gets
operator|.
name|add
argument_list|(
operator|new
name|Get
argument_list|(
name|HBaseTestingUtility
operator|.
name|ROWS
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|RpcServerInterface
name|rpcServer
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|getRpcServer
argument_list|()
decl_stmt|;
name|BaseSource
name|s
init|=
name|rpcServer
operator|.
name|getMetrics
argument_list|()
operator|.
name|getMetricsSource
argument_list|()
decl_stmt|;
name|long
name|startingExceptions
init|=
name|METRICS_ASSERT
operator|.
name|getCounter
argument_list|(
literal|"exceptions"
argument_list|,
name|s
argument_list|)
decl_stmt|;
name|long
name|startingMultiExceptions
init|=
name|METRICS_ASSERT
operator|.
name|getCounter
argument_list|(
literal|"exceptions.multiResponseTooLarge"
argument_list|,
name|s
argument_list|)
decl_stmt|;
name|Result
index|[]
name|results
init|=
name|t
operator|.
name|get
argument_list|(
name|gets
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|MAX_SIZE
argument_list|,
name|results
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Cells from TEST_UTIL.loadTable have a length of 27.
comment|// Multiplying by less than that gives an easy lower bound on size.
comment|// However in reality each kv is being reported as much higher than that.
name|METRICS_ASSERT
operator|.
name|assertCounterGt
argument_list|(
literal|"exceptions"
argument_list|,
name|startingExceptions
operator|+
operator|(
operator|(
name|MAX_SIZE
operator|*
literal|25
operator|)
operator|/
name|MAX_SIZE
operator|)
argument_list|,
name|s
argument_list|)
expr_stmt|;
name|METRICS_ASSERT
operator|.
name|assertCounterGt
argument_list|(
literal|"exceptions.multiResponseTooLarge"
argument_list|,
name|startingMultiExceptions
operator|+
operator|(
operator|(
name|MAX_SIZE
operator|*
literal|25
operator|)
operator|/
name|MAX_SIZE
operator|)
argument_list|,
name|s
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBlockMultiLimits
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|name
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testBlockMultiLimits"
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
name|hcd
operator|.
name|setDataBlockEncoding
argument_list|(
name|DataBlockEncoding
operator|.
name|FAST_DIFF
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|Table
name|t
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|name
argument_list|)
decl_stmt|;
specifier|final
name|HRegionServer
name|regionServer
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|RpcServerInterface
name|rpcServer
init|=
name|regionServer
operator|.
name|getRpcServer
argument_list|()
decl_stmt|;
name|BaseSource
name|s
init|=
name|rpcServer
operator|.
name|getMetrics
argument_list|()
operator|.
name|getMetricsSource
argument_list|()
decl_stmt|;
name|long
name|startingExceptions
init|=
name|METRICS_ASSERT
operator|.
name|getCounter
argument_list|(
literal|"exceptions"
argument_list|,
name|s
argument_list|)
decl_stmt|;
name|long
name|startingMultiExceptions
init|=
name|METRICS_ASSERT
operator|.
name|getCounter
argument_list|(
literal|"exceptions.multiResponseTooLarge"
argument_list|,
name|s
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
literal|"TEST"
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|cols
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"0"
argument_list|)
block|,
comment|// Get this
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
block|,
comment|// Buffer
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"2"
argument_list|)
block|,
comment|// Buffer
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"3"
argument_list|)
block|,
comment|// Get This
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"4"
argument_list|)
block|,
comment|// Buffer
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"5"
argument_list|)
block|,
comment|// Buffer
block|}
decl_stmt|;
comment|// Set the value size so that one result will be less than the MAX_SIE
comment|// however the block being reference will be larger than MAX_SIZE.
comment|// This should cause the regionserver to try and send a result immediately.
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
name|MAX_SIZE
operator|-
literal|100
index|]
decl_stmt|;
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextBytes
argument_list|(
name|value
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|col
range|:
name|cols
control|)
block|{
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
name|addImmutable
argument_list|(
name|FAMILY
argument_list|,
name|col
argument_list|,
name|value
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
comment|// Make sure that a flush happens
try|try
init|(
specifier|final
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin
operator|.
name|flush
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|60000
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
return|return
name|regionServer
operator|.
name|getOnlineRegions
argument_list|(
name|name
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getMaxFlushedSeqId
argument_list|()
operator|>
literal|3
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Get
argument_list|>
name|gets
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|Get
name|g0
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|g0
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|cols
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|gets
operator|.
name|add
argument_list|(
name|g0
argument_list|)
expr_stmt|;
name|Get
name|g2
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|g2
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|cols
index|[
literal|3
index|]
argument_list|)
expr_stmt|;
name|gets
operator|.
name|add
argument_list|(
name|g2
argument_list|)
expr_stmt|;
name|Result
index|[]
name|results
init|=
name|t
operator|.
name|get
argument_list|(
name|gets
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|results
operator|.
name|length
argument_list|)
expr_stmt|;
name|METRICS_ASSERT
operator|.
name|assertCounterGt
argument_list|(
literal|"exceptions"
argument_list|,
name|startingExceptions
argument_list|,
name|s
argument_list|)
expr_stmt|;
name|METRICS_ASSERT
operator|.
name|assertCounterGt
argument_list|(
literal|"exceptions.multiResponseTooLarge"
argument_list|,
name|startingMultiExceptions
argument_list|,
name|s
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

