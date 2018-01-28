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
name|TestSmallReversedScanner
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
name|TestSmallReversedScanner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestSmallReversedScanner
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testReversedSmall"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"columnFamily"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Table
name|htable
init|=
literal|null
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
comment|// create a table with 4 region: (-oo, b),[b,c),[c,d),[d,+oo)
name|byte
index|[]
name|bytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bcd"
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|splitKeys
init|=
operator|new
name|byte
index|[
name|bytes
operator|.
name|length
index|]
index|[]
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
name|bytes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|splitKeys
index|[
name|i
index|]
operator|=
operator|new
name|byte
index|[]
block|{
name|bytes
index|[
name|i
index|]
block|}
expr_stmt|;
block|}
name|htable
operator|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|splitKeys
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
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
name|TEST_UTIL
operator|.
name|truncateTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
comment|/**    * all rowKeys are fit in the last region.    * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testSmallReversedScan01
parameter_list|()
throws|throws
name|IOException
block|{
name|String
index|[]
index|[]
name|keysCases
init|=
operator|new
name|String
index|[]
index|[]
block|{
block|{
literal|"d0"
block|,
literal|"d1"
block|,
literal|"d2"
block|,
literal|"d3"
block|}
block|,
comment|// all rowKeys fit in the last region.
block|{
literal|"a0"
block|,
literal|"a1"
block|,
literal|"a2"
block|,
literal|"a3"
block|}
block|,
comment|// all rowKeys fit in the first region.
block|{
literal|"a0"
block|,
literal|"b1"
block|,
literal|"c2"
block|,
literal|"d3"
block|}
block|,
comment|// each region with a rowKey
block|}
decl_stmt|;
for|for
control|(
name|int
name|caseIndex
init|=
literal|0
init|;
name|caseIndex
operator|<
name|keysCases
operator|.
name|length
condition|;
name|caseIndex
operator|++
control|)
block|{
name|testSmallReversedScanInternal
argument_list|(
name|keysCases
index|[
name|caseIndex
index|]
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|truncateTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|testSmallReversedScanInternal
parameter_list|(
name|String
index|[]
name|inputRowKeys
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|rowCount
init|=
name|inputRowKeys
operator|.
name|length
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
name|rowCount
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
name|Bytes
operator|.
name|toBytes
argument_list|(
name|inputRowKeys
index|[
name|i
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|COLUMN_FAMILY
argument_list|,
literal|null
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|htable
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
name|setReversed
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setSmall
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ResultScanner
name|scanner
init|=
name|htable
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|Result
name|r
decl_stmt|;
name|int
name|value
init|=
name|rowCount
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
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|r
operator|.
name|getValue
argument_list|(
name|COLUMN_FAMILY
argument_list|,
literal|null
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
operator|--
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|r
operator|.
name|getRow
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|inputRowKeys
index|[
name|value
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
comment|/**    * Corner case:    *  HBase has 4 regions, (-oo,b),[b,c),[c,d),[d,+oo), and only rowKey with byte[]={0x00} locate in region (-oo,b) .    *  test whether reversed small scanner will return infinity results with RowKey={0x00}.    * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testSmallReversedScan02
parameter_list|()
throws|throws
name|IOException
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
operator|new
name|byte
index|[]
block|{
operator|(
name|char
operator|)
literal|0x00
block|}
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|COLUMN_FAMILY
argument_list|,
literal|null
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|htable
operator|.
name|put
argument_list|(
name|put
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
name|setCaching
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setReversed
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setSmall
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ResultScanner
name|scanner
init|=
name|htable
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|Result
name|r
decl_stmt|;
name|int
name|count
init|=
literal|1
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
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|r
operator|.
name|getValue
argument_list|(
name|COLUMN_FAMILY
argument_list|,
literal|null
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
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|r
operator|.
name|getRow
argument_list|()
argument_list|,
operator|new
name|byte
index|[]
block|{
operator|(
name|char
operator|)
literal|0x00
block|}
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
operator|--
name|count
operator|>=
literal|0
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

