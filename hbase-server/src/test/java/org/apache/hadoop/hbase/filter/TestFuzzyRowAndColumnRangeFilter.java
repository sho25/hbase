begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *   http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|nio
operator|.
name|ByteBuffer
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
name|CellUtil
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
name|testclassification
operator|.
name|FilterTests
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Pair
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
name|AfterClass
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|FilterTests
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
name|TestFuzzyRowAndColumnRangeFilter
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
name|TestFuzzyRowAndColumnRangeFilter
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * @throws java.lang.Exception    */
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
argument_list|()
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
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
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Nothing to do.
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Nothing to do.
block|}
annotation|@
name|Test
specifier|public
name|void
name|Test
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|cf
init|=
literal|"f"
decl_stmt|;
name|String
name|table
init|=
literal|"TestFuzzyAndColumnRangeFilterClient"
decl_stmt|;
name|Table
name|ht
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|cf
argument_list|)
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
comment|// 10 byte row key - (2 bytes 4 bytes 4 bytes)
comment|// 4 byte qualifier
comment|// 4 byte value
for|for
control|(
name|int
name|i1
init|=
literal|0
init|;
name|i1
operator|<
literal|2
condition|;
name|i1
operator|++
control|)
block|{
for|for
control|(
name|int
name|i2
init|=
literal|0
init|;
name|i2
operator|<
literal|5
condition|;
name|i2
operator|++
control|)
block|{
name|byte
index|[]
name|rk
init|=
operator|new
name|byte
index|[
literal|10
index|]
decl_stmt|;
name|ByteBuffer
name|buf
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|rk
argument_list|)
decl_stmt|;
name|buf
operator|.
name|clear
argument_list|()
expr_stmt|;
name|buf
operator|.
name|putShort
argument_list|(
operator|(
name|short
operator|)
literal|2
argument_list|)
expr_stmt|;
name|buf
operator|.
name|putInt
argument_list|(
name|i1
argument_list|)
expr_stmt|;
name|buf
operator|.
name|putInt
argument_list|(
name|i2
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
literal|5
condition|;
name|c
operator|++
control|)
block|{
name|byte
index|[]
name|cq
init|=
operator|new
name|byte
index|[
literal|4
index|]
decl_stmt|;
name|Bytes
operator|.
name|putBytes
argument_list|(
name|cq
argument_list|,
literal|0
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|c
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|rk
argument_list|)
decl_stmt|;
name|p
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|cf
operator|.
name|getBytes
argument_list|()
argument_list|,
name|cq
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|c
argument_list|)
argument_list|)
expr_stmt|;
name|ht
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Inserting: rk: "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|rk
argument_list|)
operator|+
literal|" cq: "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|cq
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|TEST_UTIL
operator|.
name|flush
argument_list|()
expr_stmt|;
comment|// test passes
name|runTest
argument_list|(
name|ht
argument_list|,
literal|0
argument_list|,
literal|10
argument_list|)
expr_stmt|;
comment|// test fails
name|runTest
argument_list|(
name|ht
argument_list|,
literal|1
argument_list|,
literal|8
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runTest
parameter_list|(
name|Table
name|hTable
parameter_list|,
name|int
name|cqStart
parameter_list|,
name|int
name|expectedSize
parameter_list|)
throws|throws
name|IOException
block|{
comment|// [0, 2, ?, ?, ?, ?, 0, 0, 0, 1]
name|byte
index|[]
name|fuzzyKey
init|=
operator|new
name|byte
index|[
literal|10
index|]
decl_stmt|;
name|ByteBuffer
name|buf
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|fuzzyKey
argument_list|)
decl_stmt|;
name|buf
operator|.
name|clear
argument_list|()
expr_stmt|;
name|buf
operator|.
name|putShort
argument_list|(
operator|(
name|short
operator|)
literal|2
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
literal|4
condition|;
name|i
operator|++
control|)
name|buf
operator|.
name|put
argument_list|(
operator|(
name|byte
operator|)
literal|63
argument_list|)
expr_stmt|;
name|buf
operator|.
name|putInt
argument_list|(
operator|(
name|short
operator|)
literal|1
argument_list|)
expr_stmt|;
name|byte
index|[]
name|mask
init|=
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
decl_stmt|;
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|pair
init|=
operator|new
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|(
name|fuzzyKey
argument_list|,
name|mask
argument_list|)
decl_stmt|;
name|FuzzyRowFilter
name|fuzzyRowFilter
init|=
operator|new
name|FuzzyRowFilter
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|pair
argument_list|)
argument_list|)
decl_stmt|;
name|ColumnRangeFilter
name|columnRangeFilter
init|=
operator|new
name|ColumnRangeFilter
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|cqStart
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|4
argument_list|)
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|//regular test
name|runScanner
argument_list|(
name|hTable
argument_list|,
name|expectedSize
argument_list|,
name|fuzzyRowFilter
argument_list|,
name|columnRangeFilter
argument_list|)
expr_stmt|;
comment|//reverse filter order test
name|runScanner
argument_list|(
name|hTable
argument_list|,
name|expectedSize
argument_list|,
name|columnRangeFilter
argument_list|,
name|fuzzyRowFilter
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runScanner
parameter_list|(
name|Table
name|hTable
parameter_list|,
name|int
name|expectedSize
parameter_list|,
name|Filter
modifier|...
name|filters
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|cf
init|=
literal|"f"
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|cf
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|FilterList
name|filterList
init|=
operator|new
name|FilterList
argument_list|(
name|filters
argument_list|)
decl_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|filterList
argument_list|)
expr_stmt|;
name|ResultScanner
name|scanner
init|=
name|hTable
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
name|result
decl_stmt|;
name|long
name|timeBeforeScan
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
name|result
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
name|result
operator|.
name|listCells
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Got rk: "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|kv
argument_list|)
argument_list|)
operator|+
literal|" cq: "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|CellUtil
operator|.
name|cloneQualifier
argument_list|(
name|kv
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|results
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
name|long
name|scanTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|timeBeforeScan
decl_stmt|;
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"scan time = "
operator|+
name|scanTime
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"found "
operator|+
name|results
operator|.
name|size
argument_list|()
operator|+
literal|" results"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedSize
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

