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
name|hbase
operator|.
name|CompareOperator
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
name|Assert
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

begin_comment
comment|/**  * Tests filter Lists in ways that rely on a MiniCluster. Where possible, favor tests in  * TestFilterList and TestFilterFromRegionSide instead.  */
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
name|FilterTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestFilterListOnMini
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
name|TestFilterListOnMini
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
name|TestFilterListOnMini
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
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
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
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
name|testFiltersWithOR
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tn
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"cf1"
block|,
literal|"cf2"
block|}
argument_list|)
decl_stmt|;
name|byte
index|[]
name|CF1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|CF2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf2"
argument_list|)
decl_stmt|;
name|Put
name|put1
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"0"
argument_list|)
argument_list|)
decl_stmt|;
name|put1
operator|.
name|addColumn
argument_list|(
name|CF1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col_a"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put1
argument_list|)
expr_stmt|;
name|Put
name|put2
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"0"
argument_list|)
argument_list|)
decl_stmt|;
name|put2
operator|.
name|addColumn
argument_list|(
name|CF2
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col_b"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put2
argument_list|)
expr_stmt|;
name|FamilyFilter
name|filterCF1
init|=
operator|new
name|FamilyFilter
argument_list|(
name|CompareOperator
operator|.
name|EQUAL
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|CF1
argument_list|)
argument_list|)
decl_stmt|;
name|FamilyFilter
name|filterCF2
init|=
operator|new
name|FamilyFilter
argument_list|(
name|CompareOperator
operator|.
name|EQUAL
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|CF2
argument_list|)
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
name|filterCF1
argument_list|)
expr_stmt|;
name|filterList
operator|.
name|addFilter
argument_list|(
name|filterCF2
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
name|setFilter
argument_list|(
name|filterList
argument_list|)
expr_stmt|;
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Filter list: "
operator|+
name|filterList
argument_list|)
expr_stmt|;
for|for
control|(
name|Result
name|rr
init|=
name|scanner
operator|.
name|next
argument_list|()
init|;
name|rr
operator|!=
literal|null
condition|;
name|rr
operator|=
name|scanner
operator|.
name|next
argument_list|()
control|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|rr
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test case for HBASE-21620    */
annotation|@
name|Test
specifier|public
name|void
name|testColumnPrefixFilterConcatWithOR
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|cf1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
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
literal|"row"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
name|String
index|[]
name|columns
init|=
operator|new
name|String
index|[]
block|{
literal|"1544768273917010001_lt"
block|,
literal|"1544768273917010001_w_1"
block|,
literal|"1544768723910010001_ca_1"
block|,
literal|"1544768723910010001_lt"
block|,
literal|"1544768723910010001_ut_1"
block|,
literal|"1544768723910010001_w_5"
block|,
literal|"1544769779710010001_lt"
block|,
literal|"1544769779710010001_w_5"
block|,
literal|"1544769883529010001_lt"
block|,
literal|"1544769883529010001_w_5"
block|,
literal|"1544769915805010001_lt"
block|,
literal|"1544769915805010001_w_5"
block|,
literal|"1544779883529010001_lt"
block|,
literal|"1544770422942010001_lt"
block|,
literal|"1544770422942010001_w_5"
block|}
decl_stmt|;
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tn
argument_list|,
name|cf1
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
name|columns
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
operator|.
name|addColumn
argument_list|(
name|cf1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|columns
index|[
name|i
index|]
argument_list|)
argument_list|,
name|value
argument_list|)
decl_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|withStartRow
argument_list|(
name|row
argument_list|)
operator|.
name|withStopRow
argument_list|(
name|row
argument_list|,
literal|true
argument_list|)
operator|.
name|setFilter
argument_list|(
operator|new
name|FilterList
argument_list|(
name|Operator
operator|.
name|MUST_PASS_ONE
argument_list|,
operator|new
name|ColumnPrefixFilter
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1544770422942010001_"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|ColumnPrefixFilter
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1544769883529010001_"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|Result
name|result
decl_stmt|;
name|int
name|resultCount
init|=
literal|0
decl_stmt|;
name|int
name|cellCount
init|=
literal|0
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
name|cellCount
operator|+=
name|result
operator|.
name|listCells
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
name|resultCount
operator|++
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|resultCount
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|cellCount
argument_list|,
literal|4
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

