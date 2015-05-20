begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license  * agreements. See the NOTICE file distributed with this work for additional information regarding  * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License. You may obtain a  * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable  * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"  * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License  * for the specific language governing permissions and limitations under the License.  */
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
name|Result
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
name|ResultScanner
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
name|Scan
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
name|metrics
operator|.
name|ScanMetrics
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
name|metrics
operator|.
name|ServerSideScanMetrics
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
name|filter
operator|.
name|BinaryComparator
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
name|filter
operator|.
name|ColumnPrefixFilter
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
name|filter
operator|.
name|CompareFilter
operator|.
name|CompareOp
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
name|filter
operator|.
name|Filter
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
name|filter
operator|.
name|FilterList
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
name|filter
operator|.
name|FilterList
operator|.
name|Operator
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
name|filter
operator|.
name|FirstKeyOnlyFilter
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
name|filter
operator|.
name|RowFilter
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
name|filter
operator|.
name|SingleColumnValueExcludeFilter
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
name|filter
operator|.
name|SingleColumnValueFilter
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
name|TestServerSideScanMetricsFromClientSide
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
name|Table
name|TABLE
init|=
literal|null
decl_stmt|;
comment|/**    * Table configuration    */
specifier|private
specifier|static
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testTable"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|int
name|NUM_ROWS
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testRow"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
index|[]
name|ROWS
init|=
name|HTestConst
operator|.
name|makeNAscii
argument_list|(
name|ROW
argument_list|,
name|NUM_ROWS
argument_list|)
decl_stmt|;
comment|// Should keep this value below 10 to keep generation of expected kv's simple. If above 10 then
comment|// table/row/cf1/... will be followed by table/row/cf10/... instead of table/row/cf2/... which
comment|// breaks the simple generation of expected kv's
specifier|private
specifier|static
name|int
name|NUM_FAMILIES
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testFamily"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
index|[]
name|FAMILIES
init|=
name|HTestConst
operator|.
name|makeNAscii
argument_list|(
name|FAMILY
argument_list|,
name|NUM_FAMILIES
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|int
name|NUM_QUALIFIERS
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testQualifier"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
index|[]
name|QUALIFIERS
init|=
name|HTestConst
operator|.
name|makeNAscii
argument_list|(
name|QUALIFIER
argument_list|,
name|NUM_QUALIFIERS
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|int
name|VALUE_SIZE
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|VALUE
init|=
name|Bytes
operator|.
name|createMaxByteArray
argument_list|(
name|VALUE_SIZE
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|int
name|NUM_COLS
init|=
name|NUM_FAMILIES
operator|*
name|NUM_QUALIFIERS
decl_stmt|;
comment|// Approximation of how large the heap size of cells in our table. Should be accessed through
comment|// getCellHeapSize().
specifier|private
specifier|static
name|long
name|CELL_HEAP_SIZE
init|=
operator|-
literal|1
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
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|TABLE
operator|=
name|createTestTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|ROWS
argument_list|,
name|FAMILIES
argument_list|,
name|QUALIFIERS
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
block|}
specifier|static
name|Table
name|createTestTable
parameter_list|(
name|TableName
name|name
parameter_list|,
name|byte
index|[]
index|[]
name|rows
parameter_list|,
name|byte
index|[]
index|[]
name|families
parameter_list|,
name|byte
index|[]
index|[]
name|qualifiers
parameter_list|,
name|byte
index|[]
name|cellValue
parameter_list|)
throws|throws
name|IOException
block|{
name|Table
name|ht
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|name
argument_list|,
name|families
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Put
argument_list|>
name|puts
init|=
name|createPuts
argument_list|(
name|rows
argument_list|,
name|families
argument_list|,
name|qualifiers
argument_list|,
name|cellValue
argument_list|)
decl_stmt|;
name|ht
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
return|return
name|ht
return|;
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
comment|/**    * Make puts to put the input value into each combination of row, family, and qualifier    * @param rows    * @param families    * @param qualifiers    * @param value    * @return    * @throws IOException    */
specifier|static
name|ArrayList
argument_list|<
name|Put
argument_list|>
name|createPuts
parameter_list|(
name|byte
index|[]
index|[]
name|rows
parameter_list|,
name|byte
index|[]
index|[]
name|families
parameter_list|,
name|byte
index|[]
index|[]
name|qualifiers
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|Put
name|put
decl_stmt|;
name|ArrayList
argument_list|<
name|Put
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|row
init|=
literal|0
init|;
name|row
operator|<
name|rows
operator|.
name|length
condition|;
name|row
operator|++
control|)
block|{
name|put
operator|=
operator|new
name|Put
argument_list|(
name|rows
index|[
name|row
index|]
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|fam
init|=
literal|0
init|;
name|fam
operator|<
name|families
operator|.
name|length
condition|;
name|fam
operator|++
control|)
block|{
for|for
control|(
name|int
name|qual
init|=
literal|0
init|;
name|qual
operator|<
name|qualifiers
operator|.
name|length
condition|;
name|qual
operator|++
control|)
block|{
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|rows
index|[
name|row
index|]
argument_list|,
name|families
index|[
name|fam
index|]
argument_list|,
name|qualifiers
index|[
name|qual
index|]
argument_list|,
name|qual
argument_list|,
name|value
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
return|return
name|puts
return|;
block|}
comment|/**    * @return The approximate heap size of a cell in the test table. All cells should have    *         approximately the same heap size, so the value is cached to avoid repeating the    *         calculation    * @throws Exception    */
specifier|private
name|long
name|getCellHeapSize
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|CELL_HEAP_SIZE
operator|==
operator|-
literal|1
condition|)
block|{
comment|// Do a partial scan that will return a single result with a single cell
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setMaxResultSize
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAllowPartialResults
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ResultScanner
name|scanner
init|=
name|TABLE
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|Result
name|result
init|=
name|scanner
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|result
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|rawCells
argument_list|()
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|rawCells
argument_list|()
operator|.
name|length
operator|==
literal|1
argument_list|)
expr_stmt|;
name|CELL_HEAP_SIZE
operator|=
name|CellUtil
operator|.
name|estimatedHeapSizeOf
argument_list|(
name|result
operator|.
name|rawCells
argument_list|()
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|CELL_HEAP_SIZE
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRowsSeenMetric
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Base scan configuration
name|Scan
name|baseScan
decl_stmt|;
name|baseScan
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
name|baseScan
operator|.
name|setScanMetricsEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|testRowsSeenMetric
argument_list|(
name|baseScan
argument_list|)
expr_stmt|;
comment|// Test case that only a single result will be returned per RPC to the serer
name|baseScan
operator|.
name|setCaching
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|testRowsSeenMetric
argument_list|(
name|baseScan
argument_list|)
expr_stmt|;
comment|// Test case that partial results are returned from the server. At most one cell will be
comment|// contained in each response
name|baseScan
operator|.
name|setMaxResultSize
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|testRowsSeenMetric
argument_list|(
name|baseScan
argument_list|)
expr_stmt|;
comment|// Test case that size limit is set such that a few cells are returned per partial result from
comment|// the server
name|baseScan
operator|.
name|setCaching
argument_list|(
name|NUM_ROWS
argument_list|)
expr_stmt|;
name|baseScan
operator|.
name|setMaxResultSize
argument_list|(
name|getCellHeapSize
argument_list|()
operator|*
operator|(
name|NUM_COLS
operator|-
literal|1
operator|)
argument_list|)
expr_stmt|;
name|testRowsSeenMetric
argument_list|(
name|baseScan
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testRowsSeenMetric
parameter_list|(
name|Scan
name|baseScan
parameter_list|)
throws|throws
name|Exception
block|{
name|Scan
name|scan
decl_stmt|;
name|scan
operator|=
operator|new
name|Scan
argument_list|(
name|baseScan
argument_list|)
expr_stmt|;
name|testMetric
argument_list|(
name|scan
argument_list|,
name|ServerSideScanMetrics
operator|.
name|COUNT_OF_ROWS_SCANNED_KEY
argument_list|,
name|NUM_ROWS
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
name|ROWS
operator|.
name|length
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|scan
operator|=
operator|new
name|Scan
argument_list|(
name|baseScan
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setStartRow
argument_list|(
name|ROWS
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setStopRow
argument_list|(
name|ROWS
index|[
name|i
operator|+
literal|1
index|]
argument_list|)
expr_stmt|;
name|testMetric
argument_list|(
name|scan
argument_list|,
name|ServerSideScanMetrics
operator|.
name|COUNT_OF_ROWS_SCANNED_KEY
argument_list|,
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
name|ROWS
operator|.
name|length
operator|-
literal|1
init|;
name|i
operator|>
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|scan
operator|=
operator|new
name|Scan
argument_list|(
name|baseScan
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setStartRow
argument_list|(
name|ROWS
index|[
name|i
operator|-
literal|1
index|]
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setStopRow
argument_list|(
name|ROWS
index|[
name|ROWS
operator|.
name|length
operator|-
literal|1
index|]
argument_list|)
expr_stmt|;
name|testMetric
argument_list|(
name|scan
argument_list|,
name|ServerSideScanMetrics
operator|.
name|COUNT_OF_ROWS_SCANNED_KEY
argument_list|,
name|ROWS
operator|.
name|length
operator|-
name|i
argument_list|)
expr_stmt|;
block|}
comment|// The filter should filter out all rows, but we still expect to see every row.
name|Filter
name|filter
init|=
operator|new
name|RowFilter
argument_list|(
name|CompareOp
operator|.
name|EQUAL
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
literal|"xyz"
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|scan
operator|=
operator|new
name|Scan
argument_list|(
name|baseScan
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
name|testMetric
argument_list|(
name|scan
argument_list|,
name|ServerSideScanMetrics
operator|.
name|COUNT_OF_ROWS_SCANNED_KEY
argument_list|,
name|ROWS
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Filter should pass on all rows
name|SingleColumnValueFilter
name|singleColumnValueFilter
init|=
operator|new
name|SingleColumnValueFilter
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|QUALIFIERS
index|[
literal|0
index|]
argument_list|,
name|CompareOp
operator|.
name|EQUAL
argument_list|,
name|VALUE
argument_list|)
decl_stmt|;
name|scan
operator|=
operator|new
name|Scan
argument_list|(
name|baseScan
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|singleColumnValueFilter
argument_list|)
expr_stmt|;
name|testMetric
argument_list|(
name|scan
argument_list|,
name|ServerSideScanMetrics
operator|.
name|COUNT_OF_ROWS_SCANNED_KEY
argument_list|,
name|ROWS
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Filter should filter out all rows
name|singleColumnValueFilter
operator|=
operator|new
name|SingleColumnValueFilter
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|QUALIFIERS
index|[
literal|0
index|]
argument_list|,
name|CompareOp
operator|.
name|NOT_EQUAL
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|scan
operator|=
operator|new
name|Scan
argument_list|(
name|baseScan
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|singleColumnValueFilter
argument_list|)
expr_stmt|;
name|testMetric
argument_list|(
name|scan
argument_list|,
name|ServerSideScanMetrics
operator|.
name|COUNT_OF_ROWS_SCANNED_KEY
argument_list|,
name|ROWS
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRowsFilteredMetric
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Base scan configuration
name|Scan
name|baseScan
decl_stmt|;
name|baseScan
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
name|baseScan
operator|.
name|setScanMetricsEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Test case where scan uses default values
name|testRowsFilteredMetric
argument_list|(
name|baseScan
argument_list|)
expr_stmt|;
comment|// Test case where at most one Result is retrieved per RPC
name|baseScan
operator|.
name|setCaching
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|testRowsFilteredMetric
argument_list|(
name|baseScan
argument_list|)
expr_stmt|;
comment|// Test case where size limit is very restrictive and partial results will be returned from
comment|// server
name|baseScan
operator|.
name|setMaxResultSize
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|testRowsFilteredMetric
argument_list|(
name|baseScan
argument_list|)
expr_stmt|;
comment|// Test a case where max result size limits response from server to only a few cells (not all
comment|// cells from the row)
name|baseScan
operator|.
name|setCaching
argument_list|(
name|NUM_ROWS
argument_list|)
expr_stmt|;
name|baseScan
operator|.
name|setMaxResultSize
argument_list|(
name|getCellHeapSize
argument_list|()
operator|*
operator|(
name|NUM_COLS
operator|-
literal|1
operator|)
argument_list|)
expr_stmt|;
name|testRowsSeenMetric
argument_list|(
name|baseScan
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testRowsFilteredMetric
parameter_list|(
name|Scan
name|baseScan
parameter_list|)
throws|throws
name|Exception
block|{
name|testRowsFilteredMetric
argument_list|(
name|baseScan
argument_list|,
literal|null
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// Row filter doesn't match any row key. All rows should be filtered
name|Filter
name|filter
init|=
operator|new
name|RowFilter
argument_list|(
name|CompareOp
operator|.
name|EQUAL
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
literal|"xyz"
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|testRowsFilteredMetric
argument_list|(
name|baseScan
argument_list|,
name|filter
argument_list|,
name|ROWS
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Filter will return results containing only the first key. Number of entire rows filtered
comment|// should be 0.
name|filter
operator|=
operator|new
name|FirstKeyOnlyFilter
argument_list|()
expr_stmt|;
name|testRowsFilteredMetric
argument_list|(
name|baseScan
argument_list|,
name|filter
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// Column prefix will find some matching qualifier on each row. Number of entire rows filtered
comment|// should be 0
name|filter
operator|=
operator|new
name|ColumnPrefixFilter
argument_list|(
name|QUALIFIERS
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|testRowsFilteredMetric
argument_list|(
name|baseScan
argument_list|,
name|filter
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// Column prefix will NOT find any matching qualifier on any row. All rows should be filtered
name|filter
operator|=
operator|new
name|ColumnPrefixFilter
argument_list|(
literal|"xyz"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|testRowsFilteredMetric
argument_list|(
name|baseScan
argument_list|,
name|filter
argument_list|,
name|ROWS
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Matching column value should exist in each row. No rows should be filtered.
name|filter
operator|=
operator|new
name|SingleColumnValueFilter
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|QUALIFIERS
index|[
literal|0
index|]
argument_list|,
name|CompareOp
operator|.
name|EQUAL
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|testRowsFilteredMetric
argument_list|(
name|baseScan
argument_list|,
name|filter
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// No matching column value should exist in any row. Filter all rows
name|filter
operator|=
operator|new
name|SingleColumnValueFilter
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|QUALIFIERS
index|[
literal|0
index|]
argument_list|,
name|CompareOp
operator|.
name|NOT_EQUAL
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|testRowsFilteredMetric
argument_list|(
name|baseScan
argument_list|,
name|filter
argument_list|,
name|ROWS
operator|.
name|length
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Filter
argument_list|>
name|filters
init|=
operator|new
name|ArrayList
argument_list|<
name|Filter
argument_list|>
argument_list|()
decl_stmt|;
name|filters
operator|.
name|add
argument_list|(
operator|new
name|RowFilter
argument_list|(
name|CompareOp
operator|.
name|EQUAL
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|ROWS
index|[
literal|0
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|filters
operator|.
name|add
argument_list|(
operator|new
name|RowFilter
argument_list|(
name|CompareOp
operator|.
name|EQUAL
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|ROWS
index|[
literal|3
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|numberOfMatchingRowFilters
init|=
name|filters
operator|.
name|size
argument_list|()
decl_stmt|;
name|filter
operator|=
operator|new
name|FilterList
argument_list|(
name|Operator
operator|.
name|MUST_PASS_ONE
argument_list|,
name|filters
argument_list|)
expr_stmt|;
name|testRowsFilteredMetric
argument_list|(
name|baseScan
argument_list|,
name|filter
argument_list|,
name|ROWS
operator|.
name|length
operator|-
name|numberOfMatchingRowFilters
argument_list|)
expr_stmt|;
name|filters
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// Add a single column value exclude filter for each column... The net effect is that all
comment|// columns will be excluded when scanning on the server side. This will result in an empty cell
comment|// array in RegionScanner#nextInternal which should be interpreted as a row being filtered.
for|for
control|(
name|int
name|family
init|=
literal|0
init|;
name|family
operator|<
name|FAMILIES
operator|.
name|length
condition|;
name|family
operator|++
control|)
block|{
for|for
control|(
name|int
name|qualifier
init|=
literal|0
init|;
name|qualifier
operator|<
name|QUALIFIERS
operator|.
name|length
condition|;
name|qualifier
operator|++
control|)
block|{
name|filters
operator|.
name|add
argument_list|(
operator|new
name|SingleColumnValueExcludeFilter
argument_list|(
name|FAMILIES
index|[
name|family
index|]
argument_list|,
name|QUALIFIERS
index|[
name|qualifier
index|]
argument_list|,
name|CompareOp
operator|.
name|EQUAL
argument_list|,
name|VALUE
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|filter
operator|=
operator|new
name|FilterList
argument_list|(
name|Operator
operator|.
name|MUST_PASS_ONE
argument_list|,
name|filters
argument_list|)
expr_stmt|;
name|testRowsFilteredMetric
argument_list|(
name|baseScan
argument_list|,
name|filter
argument_list|,
name|ROWS
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testRowsFilteredMetric
parameter_list|(
name|Scan
name|baseScan
parameter_list|,
name|Filter
name|filter
parameter_list|,
name|int
name|expectedNumFiltered
parameter_list|)
throws|throws
name|Exception
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|(
name|baseScan
argument_list|)
decl_stmt|;
if|if
condition|(
name|filter
operator|!=
literal|null
condition|)
name|scan
operator|.
name|setFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
name|testMetric
argument_list|(
name|scan
argument_list|,
name|ServerSideScanMetrics
operator|.
name|COUNT_OF_ROWS_FILTERED_KEY
argument_list|,
name|expectedNumFiltered
argument_list|)
expr_stmt|;
block|}
comment|/**    * Run the scan to completetion and check the metric against the specified value    * @param scan    * @param metricKey    * @param expectedValue    * @throws Exception    */
specifier|public
name|void
name|testMetric
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|String
name|metricKey
parameter_list|,
name|long
name|expectedValue
parameter_list|)
throws|throws
name|Exception
block|{
name|assertTrue
argument_list|(
literal|"Scan should be configured to record metrics"
argument_list|,
name|scan
operator|.
name|isScanMetricsEnabled
argument_list|()
argument_list|)
expr_stmt|;
name|ResultScanner
name|scanner
init|=
name|TABLE
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
comment|// Iterate through all the results
for|for
control|(
name|Result
name|r
range|:
name|scanner
control|)
block|{     }
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|ScanMetrics
name|metrics
init|=
name|scan
operator|.
name|getScanMetrics
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Metrics are null"
argument_list|,
name|metrics
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Metric : "
operator|+
name|metricKey
operator|+
literal|" does not exist"
argument_list|,
name|metrics
operator|.
name|hasCounter
argument_list|(
name|metricKey
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|long
name|actualMetricValue
init|=
name|metrics
operator|.
name|getCounter
argument_list|(
name|metricKey
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Metric: "
operator|+
name|metricKey
operator|+
literal|" Expected: "
operator|+
name|expectedValue
operator|+
literal|" Actual: "
operator|+
name|actualMetricValue
argument_list|,
name|expectedValue
argument_list|,
name|actualMetricValue
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

