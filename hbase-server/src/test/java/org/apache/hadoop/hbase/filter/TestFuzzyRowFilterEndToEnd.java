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
name|filter
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
name|regionserver
operator|.
name|ConstantSizeRegionSplitPolicy
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
name|RegionScanner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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
name|Arrays
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
name|TestFuzzyRowFilterEndToEnd
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
specifier|final
specifier|static
name|byte
name|fuzzyValue
init|=
operator|(
name|byte
operator|)
literal|63
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
name|TestFuzzyRowFilterEndToEnd
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|int
name|firstPartCardinality
init|=
literal|50
decl_stmt|;
specifier|private
specifier|static
name|int
name|secondPartCardinality
init|=
literal|50
decl_stmt|;
specifier|private
specifier|static
name|int
name|thirdPartCardinality
init|=
literal|50
decl_stmt|;
specifier|private
specifier|static
name|int
name|colQualifiersTotal
init|=
literal|5
decl_stmt|;
specifier|private
specifier|static
name|int
name|totalFuzzyKeys
init|=
name|thirdPartCardinality
operator|/
literal|2
decl_stmt|;
specifier|private
specifier|static
name|String
name|table
init|=
literal|"TestFuzzyRowFilterEndToEnd"
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
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.client.scanner.caching"
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_REGION_SPLIT_POLICY_KEY
argument_list|,
name|ConstantSizeRegionSplitPolicy
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// set no splits
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|HREGION_MAX_FILESIZE
argument_list|,
operator|(
operator|(
name|long
operator|)
literal|1024
operator|)
operator|*
literal|1024
operator|*
literal|1024
operator|*
literal|10
argument_list|)
expr_stmt|;
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
comment|// HBASE-15676 Test that fuzzy info of all fixed bits (0s) finds matching row.
annotation|@
name|Test
specifier|public
name|void
name|testAllFixedBits
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|cf
init|=
literal|"f"
decl_stmt|;
name|String
name|cq
init|=
literal|"q"
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
name|name
operator|.
name|getMethodName
argument_list|()
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
comment|// Load data
name|String
index|[]
name|rows
init|=
operator|new
name|String
index|[]
block|{
literal|"\\x9C\\x00\\x044\\x00\\x00\\x00\\x00"
block|,
literal|"\\x9C\\x00\\x044\\x01\\x00\\x00\\x00"
block|,
literal|"\\x9C\\x00\\x044\\x00\\x01\\x00\\x00"
block|,
literal|"\\x9B\\x00\\x044e\\x9B\\x02\\xBB"
block|,
literal|"\\x9C\\x00\\x044\\x00\\x00\\x01\\x00"
block|,
literal|"\\x9C\\x00\\x044\\x00\\x01\\x00\\x01"
block|,
literal|"\\x9B\\x00\\x044e\\xBB\\xB2\\xBB"
block|, }
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
name|rows
operator|.
name|length
condition|;
name|i
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
name|toBytesBinary
argument_list|(
name|rows
index|[
name|i
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|cf
operator|.
name|getBytes
argument_list|()
argument_list|,
name|cq
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"value"
operator|.
name|getBytes
argument_list|()
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
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|data
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|byte
index|[]
name|fuzzyKey
init|=
name|Bytes
operator|.
name|toBytesBinary
argument_list|(
literal|"\\x9B\\x00\\x044e"
argument_list|)
decl_stmt|;
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
literal|0
block|,
literal|0
block|,
literal|0
block|}
decl_stmt|;
comment|// copy the fuzzy key and mask to test HBASE-18617
name|byte
index|[]
name|copyFuzzyKey
init|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|fuzzyKey
argument_list|,
name|fuzzyKey
operator|.
name|length
argument_list|)
decl_stmt|;
name|byte
index|[]
name|copyMask
init|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|mask
argument_list|,
name|mask
operator|.
name|length
argument_list|)
decl_stmt|;
name|data
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<>
argument_list|(
name|fuzzyKey
argument_list|,
name|mask
argument_list|)
argument_list|)
expr_stmt|;
name|FuzzyRowFilter
name|filter
init|=
operator|new
name|FuzzyRowFilter
argument_list|(
name|data
argument_list|)
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
name|setFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
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
name|int
name|total
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|total
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|total
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|Arrays
operator|.
name|equals
argument_list|(
name|copyFuzzyKey
argument_list|,
name|fuzzyKey
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|Arrays
operator|.
name|equals
argument_list|(
name|copyMask
argument_list|,
name|mask
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHBASE14782
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|cf
init|=
literal|"f"
decl_stmt|;
name|String
name|cq
init|=
literal|"q"
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
name|name
operator|.
name|getMethodName
argument_list|()
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
comment|// Load data
name|String
index|[]
name|rows
init|=
operator|new
name|String
index|[]
block|{
literal|"\\x9C\\x00\\x044\\x00\\x00\\x00\\x00"
block|,
literal|"\\x9C\\x00\\x044\\x01\\x00\\x00\\x00"
block|,
literal|"\\x9C\\x00\\x044\\x00\\x01\\x00\\x00"
block|,
literal|"\\x9C\\x00\\x044\\x00\\x00\\x01\\x00"
block|,
literal|"\\x9C\\x00\\x044\\x00\\x01\\x00\\x01"
block|,
literal|"\\x9B\\x00\\x044e\\xBB\\xB2\\xBB"
block|,      }
decl_stmt|;
name|String
name|badRow
init|=
literal|"\\x9C\\x00\\x03\\xE9e\\xBB{X\\x1Fwts\\x1F\\x15vRX"
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
name|rows
operator|.
name|length
condition|;
name|i
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
name|toBytesBinary
argument_list|(
name|rows
index|[
name|i
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|cf
operator|.
name|getBytes
argument_list|()
argument_list|,
name|cq
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"value"
operator|.
name|getBytes
argument_list|()
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
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytesBinary
argument_list|(
name|badRow
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|cf
operator|.
name|getBytes
argument_list|()
argument_list|,
name|cq
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"value"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|ht
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|flush
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|data
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|byte
index|[]
name|fuzzyKey
init|=
name|Bytes
operator|.
name|toBytesBinary
argument_list|(
literal|"\\x00\\x00\\x044"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|mask
init|=
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
decl_stmt|;
name|data
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<>
argument_list|(
name|fuzzyKey
argument_list|,
name|mask
argument_list|)
argument_list|)
expr_stmt|;
name|FuzzyRowFilter
name|filter
init|=
operator|new
name|FuzzyRowFilter
argument_list|(
name|data
argument_list|)
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
name|setFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
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
name|int
name|total
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|total
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|rows
operator|.
name|length
argument_list|,
name|total
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEndToEnd
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|cf
init|=
literal|"f"
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
name|i0
init|=
literal|0
init|;
name|i0
operator|<
name|firstPartCardinality
condition|;
name|i0
operator|++
control|)
block|{
for|for
control|(
name|int
name|i1
init|=
literal|0
init|;
name|i1
operator|<
name|secondPartCardinality
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
name|thirdPartCardinality
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
name|i0
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
name|colQualifiersTotal
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
name|addColumn
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
block|}
block|}
block|}
block|}
name|TEST_UTIL
operator|.
name|flush
argument_list|()
expr_stmt|;
comment|// test passes
name|runTest1
argument_list|(
name|ht
argument_list|)
expr_stmt|;
name|runTest2
argument_list|(
name|ht
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runTest1
parameter_list|(
name|Table
name|hTable
parameter_list|)
throws|throws
name|IOException
block|{
comment|// [0, 2, ?, ?, ?, ?, 0, 0, 0, 1]
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
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|list
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
name|totalFuzzyKeys
condition|;
name|i
operator|++
control|)
block|{
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
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|4
condition|;
name|j
operator|++
control|)
block|{
name|buf
operator|.
name|put
argument_list|(
name|fuzzyValue
argument_list|)
expr_stmt|;
block|}
name|buf
operator|.
name|putInt
argument_list|(
name|i
argument_list|)
expr_stmt|;
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
argument_list|<>
argument_list|(
name|fuzzyKey
argument_list|,
name|mask
argument_list|)
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|pair
argument_list|)
expr_stmt|;
block|}
name|int
name|expectedSize
init|=
name|secondPartCardinality
operator|*
name|totalFuzzyKeys
operator|*
name|colQualifiersTotal
decl_stmt|;
name|FuzzyRowFilter
name|fuzzyRowFilter0
init|=
operator|new
name|FuzzyRowFilter
argument_list|(
name|list
argument_list|)
decl_stmt|;
comment|// Filters are not stateless - we can't reuse them
name|FuzzyRowFilter
name|fuzzyRowFilter1
init|=
operator|new
name|FuzzyRowFilter
argument_list|(
name|list
argument_list|)
decl_stmt|;
comment|// regular test
name|runScanner
argument_list|(
name|hTable
argument_list|,
name|expectedSize
argument_list|,
name|fuzzyRowFilter0
argument_list|)
expr_stmt|;
comment|// optimized from block cache
name|runScanner
argument_list|(
name|hTable
argument_list|,
name|expectedSize
argument_list|,
name|fuzzyRowFilter1
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runTest2
parameter_list|(
name|Table
name|hTable
parameter_list|)
throws|throws
name|IOException
block|{
comment|// [0, 0, ?, ?, ?, ?, 0, 0, 0, 0] , [0, 1, ?, ?, ?, ?, 0, 0, 0, 1]...
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
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|list
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
name|totalFuzzyKeys
condition|;
name|i
operator|++
control|)
block|{
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
call|(
name|short
call|)
argument_list|(
name|i
operator|*
literal|2
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|4
condition|;
name|j
operator|++
control|)
block|{
name|buf
operator|.
name|put
argument_list|(
name|fuzzyValue
argument_list|)
expr_stmt|;
block|}
name|buf
operator|.
name|putInt
argument_list|(
name|i
operator|*
literal|2
argument_list|)
expr_stmt|;
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
argument_list|<>
argument_list|(
name|fuzzyKey
argument_list|,
name|mask
argument_list|)
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|pair
argument_list|)
expr_stmt|;
block|}
name|int
name|expectedSize
init|=
name|totalFuzzyKeys
operator|*
name|secondPartCardinality
operator|*
name|colQualifiersTotal
decl_stmt|;
name|FuzzyRowFilter
name|fuzzyRowFilter0
init|=
operator|new
name|FuzzyRowFilter
argument_list|(
name|list
argument_list|)
decl_stmt|;
comment|// Filters are not stateless - we can't reuse them
name|FuzzyRowFilter
name|fuzzyRowFilter1
init|=
operator|new
name|FuzzyRowFilter
argument_list|(
name|list
argument_list|)
decl_stmt|;
comment|// regular test
name|runScanner
argument_list|(
name|hTable
argument_list|,
name|expectedSize
argument_list|,
name|fuzzyRowFilter0
argument_list|)
expr_stmt|;
comment|// optimized from block cache
name|runScanner
argument_list|(
name|hTable
argument_list|,
name|expectedSize
argument_list|,
name|fuzzyRowFilter1
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
name|filter
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
name|scan
operator|.
name|setFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|table
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|HRegion
name|first
init|=
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|first
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|RegionScanner
name|scanner
init|=
name|first
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
argument_list|<>
argument_list|()
decl_stmt|;
comment|// Result result;
name|long
name|timeBeforeScan
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|int
name|found
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
condition|)
block|{
name|found
operator|+=
name|results
operator|.
name|size
argument_list|()
expr_stmt|;
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|found
operator|+=
name|results
operator|.
name|size
argument_list|()
expr_stmt|;
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
literal|"\nscan time = "
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
name|found
operator|+
literal|" results\n"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedSize
argument_list|,
name|found
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testFilterList
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|cf
init|=
literal|"f"
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
name|name
operator|.
name|getMethodName
argument_list|()
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
literal|5
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
comment|// Each row contains 5 columns
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
name|addColumn
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
comment|// test passes if we get back 5 KV's (1 row)
name|runTest
argument_list|(
name|ht
argument_list|,
literal|5
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
name|void
name|runTest
parameter_list|(
name|Table
name|hTable
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
name|fuzzyKey1
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
name|fuzzyKey1
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
name|fuzzyValue
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
name|mask1
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
name|byte
index|[]
name|fuzzyKey2
init|=
operator|new
name|byte
index|[
literal|10
index|]
decl_stmt|;
name|buf
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|fuzzyKey2
argument_list|)
expr_stmt|;
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
name|fuzzyValue
argument_list|)
expr_stmt|;
name|byte
index|[]
name|mask2
init|=
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
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
name|pair1
init|=
operator|new
name|Pair
argument_list|<>
argument_list|(
name|fuzzyKey1
argument_list|,
name|mask1
argument_list|)
decl_stmt|;
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|pair2
init|=
operator|new
name|Pair
argument_list|<>
argument_list|(
name|fuzzyKey2
argument_list|,
name|mask2
argument_list|)
decl_stmt|;
name|FuzzyRowFilter
name|fuzzyRowFilter1
init|=
operator|new
name|FuzzyRowFilter
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|pair1
argument_list|)
argument_list|)
decl_stmt|;
name|FuzzyRowFilter
name|fuzzyRowFilter2
init|=
operator|new
name|FuzzyRowFilter
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|pair2
argument_list|)
argument_list|)
decl_stmt|;
comment|// regular test - we expect 1 row back (5 KVs)
name|runScanner
argument_list|(
name|hTable
argument_list|,
name|expectedSize
argument_list|,
name|fuzzyRowFilter1
argument_list|,
name|fuzzyRowFilter2
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
name|filter1
parameter_list|,
name|Filter
name|filter2
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
name|Operator
operator|.
name|MUST_PASS_ALL
argument_list|,
name|filter1
argument_list|,
name|filter2
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
argument_list|<>
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

