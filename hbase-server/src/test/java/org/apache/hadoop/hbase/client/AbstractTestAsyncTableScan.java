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
name|client
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
name|CompletableFuture
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|IntStream
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

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractTestAsyncTableScan
block|{
specifier|protected
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"async"
argument_list|)
decl_stmt|;
specifier|protected
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
specifier|protected
specifier|static
name|byte
index|[]
name|CQ1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq1"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|byte
index|[]
name|CQ2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq2"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|int
name|COUNT
init|=
literal|1000
decl_stmt|;
specifier|protected
specifier|static
name|AsyncConnection
name|ASYNC_CONN
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|byte
index|[]
index|[]
name|splitKeys
init|=
operator|new
name|byte
index|[
literal|8
index|]
index|[]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|111
init|;
name|i
operator|<
literal|999
condition|;
name|i
operator|+=
literal|111
control|)
block|{
name|splitKeys
index|[
name|i
operator|/
literal|111
operator|-
literal|1
index|]
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY
argument_list|,
name|splitKeys
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|ASYNC_CONN
operator|=
name|ConnectionFactory
operator|.
name|createAsyncConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|RawAsyncTable
name|table
init|=
name|ASYNC_CONN
operator|.
name|getRawTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|CompletableFuture
argument_list|<
name|?
argument_list|>
argument_list|>
name|futures
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|COUNT
argument_list|)
operator|.
name|forEach
argument_list|(
name|i
lambda|->
name|futures
operator|.
name|add
argument_list|(
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|CQ1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|CQ2
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
operator|*
name|i
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|CompletableFuture
operator|.
name|allOf
argument_list|(
name|futures
operator|.
name|toArray
argument_list|(
operator|new
name|CompletableFuture
argument_list|<
name|?
argument_list|>
index|[
literal|0
index|]
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|ASYNC_CONN
operator|.
name|close
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|protected
specifier|abstract
name|Scan
name|createScan
parameter_list|()
function_decl|;
specifier|protected
specifier|abstract
name|List
argument_list|<
name|Result
argument_list|>
name|doScan
parameter_list|(
name|Scan
name|scan
parameter_list|)
throws|throws
name|Exception
function_decl|;
annotation|@
name|Test
specifier|public
name|void
name|testScanAll
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|doScan
argument_list|(
name|createScan
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|COUNT
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|COUNT
argument_list|)
operator|.
name|forEach
argument_list|(
name|i
lambda|->
block|{
name|Result
name|result
init|=
name|results
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|i
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|Bytes
operator|.
name|toInt
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|CQ1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertResultEquals
parameter_list|(
name|Result
name|result
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|i
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|Bytes
operator|.
name|toInt
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|CQ1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
operator|*
name|i
argument_list|,
name|Bytes
operator|.
name|toInt
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|CQ2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReversedScanAll
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|doScan
argument_list|(
name|createScan
argument_list|()
operator|.
name|setReversed
argument_list|(
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|COUNT
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|COUNT
argument_list|)
operator|.
name|forEach
argument_list|(
name|i
lambda|->
name|assertResultEquals
argument_list|(
name|results
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|COUNT
operator|-
name|i
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScanNoStopKey
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|start
init|=
literal|345
decl_stmt|;
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|doScan
argument_list|(
name|createScan
argument_list|()
operator|.
name|setStartRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|start
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|COUNT
operator|-
name|start
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|COUNT
operator|-
name|start
argument_list|)
operator|.
name|forEach
argument_list|(
name|i
lambda|->
name|assertResultEquals
argument_list|(
name|results
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|start
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReverseScanNoStopKey
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|start
init|=
literal|765
decl_stmt|;
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|doScan
argument_list|(
name|createScan
argument_list|()
operator|.
name|setStartRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|start
argument_list|)
argument_list|)
argument_list|)
operator|.
name|setReversed
argument_list|(
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|start
operator|+
literal|1
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|start
operator|+
literal|1
argument_list|)
operator|.
name|forEach
argument_list|(
name|i
lambda|->
name|assertResultEquals
argument_list|(
name|results
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|start
operator|-
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testScan
parameter_list|(
name|int
name|start
parameter_list|,
name|int
name|stop
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|doScan
argument_list|(
name|createScan
argument_list|()
operator|.
name|setStartRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|start
argument_list|)
argument_list|)
argument_list|)
operator|.
name|setStopRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|stop
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|stop
operator|-
name|start
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|stop
operator|-
name|start
argument_list|)
operator|.
name|forEach
argument_list|(
name|i
lambda|->
name|assertResultEquals
argument_list|(
name|results
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|start
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testReversedScan
parameter_list|(
name|int
name|start
parameter_list|,
name|int
name|stop
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|doScan
argument_list|(
name|createScan
argument_list|()
operator|.
name|setStartRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|start
argument_list|)
argument_list|)
argument_list|)
operator|.
name|setStopRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
name|stop
argument_list|)
argument_list|)
argument_list|)
operator|.
name|setReversed
argument_list|(
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|start
operator|-
name|stop
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|start
operator|-
name|stop
argument_list|)
operator|.
name|forEach
argument_list|(
name|i
lambda|->
name|assertResultEquals
argument_list|(
name|results
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|start
operator|-
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScanWithStartKeyAndStopKey
parameter_list|()
throws|throws
name|Exception
block|{
name|testScan
argument_list|(
literal|345
argument_list|,
literal|567
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReversedScanWithStartKeyAndStopKey
parameter_list|()
throws|throws
name|Exception
block|{
name|testReversedScan
argument_list|(
literal|765
argument_list|,
literal|543
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScanAtRegionBoundary
parameter_list|()
throws|throws
name|Exception
block|{
name|testScan
argument_list|(
literal|222
argument_list|,
literal|333
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReversedScanAtRegionBoundary
parameter_list|()
throws|throws
name|Exception
block|{
name|testScan
argument_list|(
literal|222
argument_list|,
literal|333
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

