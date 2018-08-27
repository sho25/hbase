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
comment|/**  * Testcase for HBASE-21032, where use the wrong readType from a Scan instance which is actually a  * get scan and cause returning only 1 cell per rpc call.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|ClientTests
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
name|TestGetScanPartialResult
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
name|TestGetScanPartialResult
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
name|TABLE
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|CF
init|=
block|{
literal|'c'
block|,
literal|'f'
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW
init|=
block|{
literal|'r'
block|,
literal|'o'
block|,
literal|'w'
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|VALUE_SIZE
init|=
literal|10000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_COLUMNS
init|=
literal|300
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
literal|1
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE
argument_list|,
name|CF
argument_list|)
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
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|byte
index|[]
name|makeLargeValue
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|byte
index|[]
name|v
init|=
operator|new
name|byte
index|[
name|size
index|]
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|v
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
block|}
return|return
name|v
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|IOException
block|{
try|try
init|(
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
name|TABLE
argument_list|)
init|)
block|{
comment|// populate a row with bunch of columns and large values
comment|// to cause scan to return partials
name|byte
index|[]
name|val
init|=
name|makeLargeValue
argument_list|(
name|VALUE_SIZE
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|ROW
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
name|NUM_COLUMNS
condition|;
name|i
operator|++
control|)
block|{
name|p
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
name|t
operator|.
name|put
argument_list|(
name|p
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
name|withStartRow
argument_list|(
name|ROW
argument_list|)
expr_stmt|;
name|scan
operator|.
name|withStopRow
argument_list|(
name|ROW
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAllowPartialResults
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setMaxResultSize
argument_list|(
literal|2L
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
name|scan
operator|.
name|readVersions
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|ResultScanner
name|scanner
init|=
name|t
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|int
name|nResults
init|=
literal|0
decl_stmt|;
name|int
name|nCells
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|result
init|=
name|scanner
operator|.
name|next
argument_list|()
init|;
operator|(
name|result
operator|!=
literal|null
operator|)
condition|;
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
control|)
block|{
name|nResults
operator|++
expr_stmt|;
name|nCells
operator|+=
name|result
operator|.
name|listCells
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|NUM_COLUMNS
argument_list|,
name|nCells
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|nResults
operator|<
literal|5
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
