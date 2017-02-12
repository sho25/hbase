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
name|filter
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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|Cell
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
name|Durability
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
name|filter
operator|.
name|MultiRowRangeFilter
operator|.
name|RowRange
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
name|hfile
operator|.
name|HFile
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

begin_comment
comment|/*  * This test is for the optimization added in HBASE-15243.  * FilterList with two MultiRowRangeFilter's is constructed using Operator.MUST_PASS_ONE.  */
end_comment

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
name|TestFilterListOrOperatorWithBlkCnt
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
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestFilterListOrOperatorWithBlkCnt
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
specifier|private
name|byte
index|[]
name|qf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qf"
argument_list|)
decl_stmt|;
specifier|private
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val"
argument_list|)
decl_stmt|;
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|int
name|numRows
init|=
literal|10000
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
comment|/**    * @throws Exception    */
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
name|long
name|blkSize
init|=
literal|4096
decl_stmt|;
comment|/*      * dfs block size is adjusted so that the specified number of rows would result in      * multiple blocks (8 for this test).      * Later in the test, assertion is made on the number of blocks read.      */
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setLong
argument_list|(
literal|"dfs.blocksize"
argument_list|,
name|blkSize
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setLong
argument_list|(
literal|"dfs.bytes-per-checksum"
argument_list|,
name|blkSize
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * @throws Exception    */
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
specifier|private
specifier|static
name|long
name|getBlkAccessCount
parameter_list|()
block|{
return|return
name|HFile
operator|.
name|DATABLOCK_READ_COUNT
operator|.
name|sum
argument_list|()
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultiRowRangeWithFilterListOrOperatorWithBlkCnt
parameter_list|()
throws|throws
name|IOException
block|{
name|tableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
name|Table
name|ht
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|family
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|generateRows
argument_list|(
name|numRows
argument_list|,
name|ht
argument_list|,
name|family
argument_list|,
name|qf
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|long
name|blocksStart
init|=
name|getBlkAccessCount
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RowRange
argument_list|>
name|ranges1
init|=
operator|new
name|ArrayList
argument_list|<
name|RowRange
argument_list|>
argument_list|()
decl_stmt|;
name|ranges1
operator|.
name|add
argument_list|(
operator|new
name|RowRange
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|10
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|15
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|ranges1
operator|.
name|add
argument_list|(
operator|new
name|RowRange
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|9980
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|9985
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|MultiRowRangeFilter
name|filter1
init|=
operator|new
name|MultiRowRangeFilter
argument_list|(
name|ranges1
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|RowRange
argument_list|>
name|ranges2
init|=
operator|new
name|ArrayList
argument_list|<
name|RowRange
argument_list|>
argument_list|()
decl_stmt|;
name|ranges2
operator|.
name|add
argument_list|(
operator|new
name|RowRange
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|15
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|20
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|ranges2
operator|.
name|add
argument_list|(
operator|new
name|RowRange
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|9985
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|9990
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|MultiRowRangeFilter
name|filter2
init|=
operator|new
name|MultiRowRangeFilter
argument_list|(
name|ranges2
argument_list|)
decl_stmt|;
name|FilterList
name|filterList
init|=
operator|new
name|FilterList
argument_list|(
name|FilterList
operator|.
name|Operator
operator|.
name|MUST_PASS_ONE
argument_list|)
decl_stmt|;
name|filterList
operator|.
name|addFilter
argument_list|(
name|filter1
argument_list|)
expr_stmt|;
name|filterList
operator|.
name|addFilter
argument_list|(
name|filter2
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|filterList
argument_list|)
expr_stmt|;
name|int
name|resultsSize
init|=
name|getResultsSize
argument_list|(
name|ht
argument_list|,
name|scan
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"found "
operator|+
name|resultsSize
operator|+
literal|" results"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|results1
init|=
name|getScanResult
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|10
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|20
argument_list|)
argument_list|,
name|ht
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|results2
init|=
name|getScanResult
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|9980
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|9990
argument_list|)
argument_list|,
name|ht
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|results1
operator|.
name|size
argument_list|()
operator|+
name|results2
operator|.
name|size
argument_list|()
argument_list|,
name|resultsSize
argument_list|)
expr_stmt|;
name|long
name|blocksEnd
init|=
name|getBlkAccessCount
argument_list|()
decl_stmt|;
name|long
name|diff
init|=
name|blocksEnd
operator|-
name|blocksStart
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Diff in number of blocks "
operator|+
name|diff
argument_list|)
expr_stmt|;
comment|/*      * Verify that we don't read all the blocks (8 in total).      */
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|diff
argument_list|)
expr_stmt|;
name|ht
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|generateRows
parameter_list|(
name|int
name|numberOfRows
parameter_list|,
name|Table
name|ht
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qf
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|IOException
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
name|numberOfRows
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
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
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf
argument_list|,
name|value
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
name|ht
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|TEST_UTIL
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|Cell
argument_list|>
name|getScanResult
parameter_list|(
name|byte
index|[]
name|startRow
parameter_list|,
name|byte
index|[]
name|stopRow
parameter_list|,
name|Table
name|ht
parameter_list|)
throws|throws
name|IOException
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|Bytes
operator|.
name|toString
argument_list|(
name|startRow
argument_list|)
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|scan
operator|.
name|setStartRow
argument_list|(
name|startRow
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|Bytes
operator|.
name|toString
argument_list|(
name|stopRow
argument_list|)
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|scan
operator|.
name|setStopRow
argument_list|(
name|stopRow
argument_list|)
expr_stmt|;
block|}
name|ResultScanner
name|scanner
init|=
name|ht
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|kvList
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
decl_stmt|;
name|Result
name|r
decl_stmt|;
while|while
condition|(
operator|(
name|r
operator|=
name|scanner
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Cell
name|kv
range|:
name|r
operator|.
name|listCells
argument_list|()
control|)
block|{
name|kvList
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|kvList
return|;
block|}
specifier|private
name|int
name|getResultsSize
parameter_list|(
name|Table
name|ht
parameter_list|,
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
name|ResultScanner
name|scanner
init|=
name|ht
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
decl_stmt|;
name|Result
name|r
decl_stmt|;
while|while
condition|(
operator|(
name|r
operator|=
name|scanner
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Cell
name|kv
range|:
name|r
operator|.
name|listCells
argument_list|()
control|)
block|{
name|results
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|results
operator|.
name|size
argument_list|()
return|;
block|}
block|}
end_class

end_unit

