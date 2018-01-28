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
name|ExecutionException
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
name|Collectors
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

begin_comment
comment|/**  * With filter we may stop at a middle of row and think that we still have more cells for the  * current row but actually all the remaining cells will be filtered out by the filter. So it will  * lead to a Result that mayHaveMoreCellsInRow is true but actually there are no cells for the same  * row. Here we want to test if our limited scan still works.  */
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
name|TestRawAsyncTableLimitedScanWithFilter
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
name|TestRawAsyncTableLimitedScanWithFilter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestRegionScanner"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
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
specifier|final
name|byte
index|[]
index|[]
name|CQS
init|=
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq1"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq2"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq3"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq4"
argument_list|)
block|}
decl_stmt|;
specifier|private
specifier|static
name|int
name|ROW_COUNT
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
name|AsyncConnection
name|CONN
decl_stmt|;
specifier|private
specifier|static
name|AsyncTable
argument_list|<
name|?
argument_list|>
name|TABLE
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
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|CONN
operator|=
name|ConnectionFactory
operator|.
name|createAsyncConnection
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|TABLE
operator|=
name|CONN
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|TABLE
operator|.
name|putAll
argument_list|(
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|ROW_COUNT
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|i
lambda|->
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|CQS
operator|.
name|length
argument_list|)
operator|.
name|forEach
argument_list|(
name|j
lambda|->
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|CQS
index|[
name|j
index|]
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
operator|(
name|j
operator|+
literal|1
operator|)
operator|*
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|put
return|;
block|}
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
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
name|CONN
operator|.
name|close
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompleteResult
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|int
name|limit
init|=
literal|5
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
operator|.
name|setFilter
argument_list|(
operator|new
name|ColumnCountOnRowFilter
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|setMaxResultSize
argument_list|(
literal|1
argument_list|)
operator|.
name|setLimit
argument_list|(
name|limit
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|TABLE
operator|.
name|scanAll
argument_list|(
name|scan
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|limit
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
name|limit
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
name|i
argument_list|,
name|Bytes
operator|.
name|toInt
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
literal|2
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|result
operator|.
name|mayHaveMoreCellsInRow
argument_list|()
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
name|CQS
index|[
literal|0
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
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
name|CQS
index|[
literal|1
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAllowPartial
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|int
name|limit
init|=
literal|5
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
operator|.
name|setFilter
argument_list|(
operator|new
name|ColumnCountOnRowFilter
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|setMaxResultSize
argument_list|(
literal|1
argument_list|)
operator|.
name|setAllowPartialResults
argument_list|(
literal|true
argument_list|)
operator|.
name|setLimit
argument_list|(
name|limit
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|TABLE
operator|.
name|scanAll
argument_list|(
name|scan
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
operator|*
name|limit
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
literal|2
operator|*
name|limit
argument_list|)
operator|.
name|forEach
argument_list|(
name|i
lambda|->
block|{
name|int
name|key
init|=
name|i
operator|/
literal|2
decl_stmt|;
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
name|key
argument_list|,
name|Bytes
operator|.
name|toInt
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
literal|1
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|mayHaveMoreCellsInRow
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|cqIndex
init|=
name|i
operator|%
literal|2
decl_stmt|;
name|assertEquals
argument_list|(
name|key
operator|*
operator|(
name|cqIndex
operator|+
literal|1
operator|)
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
name|CQS
index|[
name|cqIndex
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBatchAllowPartial
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|int
name|limit
init|=
literal|5
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
operator|.
name|setFilter
argument_list|(
operator|new
name|ColumnCountOnRowFilter
argument_list|(
literal|3
argument_list|)
argument_list|)
operator|.
name|setBatch
argument_list|(
literal|2
argument_list|)
operator|.
name|setMaxResultSize
argument_list|(
literal|1
argument_list|)
operator|.
name|setAllowPartialResults
argument_list|(
literal|true
argument_list|)
operator|.
name|setLimit
argument_list|(
name|limit
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|TABLE
operator|.
name|scanAll
argument_list|(
name|scan
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
operator|*
name|limit
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
literal|3
operator|*
name|limit
argument_list|)
operator|.
name|forEach
argument_list|(
name|i
lambda|->
block|{
name|int
name|key
init|=
name|i
operator|/
literal|3
decl_stmt|;
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
name|key
argument_list|,
name|Bytes
operator|.
name|toInt
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
literal|1
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|mayHaveMoreCellsInRow
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|cqIndex
init|=
name|i
operator|%
literal|3
decl_stmt|;
name|assertEquals
argument_list|(
name|key
operator|*
operator|(
name|cqIndex
operator|+
literal|1
operator|)
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
name|CQS
index|[
name|cqIndex
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBatch
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|int
name|limit
init|=
literal|5
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
operator|.
name|setFilter
argument_list|(
operator|new
name|ColumnCountOnRowFilter
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|setBatch
argument_list|(
literal|2
argument_list|)
operator|.
name|setMaxResultSize
argument_list|(
literal|1
argument_list|)
operator|.
name|setLimit
argument_list|(
name|limit
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|TABLE
operator|.
name|scanAll
argument_list|(
name|scan
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|limit
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
name|limit
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
name|i
argument_list|,
name|Bytes
operator|.
name|toInt
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
literal|2
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|mayHaveMoreCellsInRow
argument_list|()
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
name|CQS
index|[
literal|0
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
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
name|CQS
index|[
literal|1
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBatchAndFilterDiffer
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|int
name|limit
init|=
literal|5
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
operator|.
name|setFilter
argument_list|(
operator|new
name|ColumnCountOnRowFilter
argument_list|(
literal|3
argument_list|)
argument_list|)
operator|.
name|setBatch
argument_list|(
literal|2
argument_list|)
operator|.
name|setMaxResultSize
argument_list|(
literal|1
argument_list|)
operator|.
name|setLimit
argument_list|(
name|limit
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|TABLE
operator|.
name|scanAll
argument_list|(
name|scan
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
operator|*
name|limit
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
name|limit
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
literal|2
operator|*
name|i
argument_list|)
decl_stmt|;
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
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|mayHaveMoreCellsInRow
argument_list|()
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
name|CQS
index|[
literal|0
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
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
name|CQS
index|[
literal|1
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|result
operator|=
name|results
operator|.
name|get
argument_list|(
literal|2
operator|*
name|i
operator|+
literal|1
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
name|getRow
argument_list|()
argument_list|)
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
name|assertFalse
argument_list|(
name|result
operator|.
name|mayHaveMoreCellsInRow
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
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
name|CQS
index|[
literal|2
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

