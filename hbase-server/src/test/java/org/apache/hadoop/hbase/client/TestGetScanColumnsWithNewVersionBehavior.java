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
name|assertArrayEquals
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
name|KeyValue
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
name|ArrayList
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
name|TestGetScanColumnsWithNewVersionBehavior
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
name|TestGetScanColumnsWithNewVersionBehavior
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
name|byte
index|[]
name|COLA
init|=
block|{
literal|'a'
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLB
init|=
block|{
literal|'b'
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLC
init|=
block|{
literal|'c'
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|TS
init|=
literal|42
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
name|ColumnFamilyDescriptor
name|cd
init|=
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|CF
argument_list|)
operator|.
name|setNewVersionBehavior
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TABLE
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|cd
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|null
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
name|Cell
index|[]
name|expected
init|=
operator|new
name|Cell
index|[
literal|2
index|]
decl_stmt|;
name|expected
index|[
literal|0
index|]
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|CF
argument_list|,
name|COLA
argument_list|,
name|TS
argument_list|,
name|COLA
argument_list|)
expr_stmt|;
name|expected
index|[
literal|1
index|]
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|CF
argument_list|,
name|COLC
argument_list|,
name|TS
argument_list|,
name|COLC
argument_list|)
expr_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|COLA
argument_list|,
name|TS
argument_list|,
name|COLA
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|COLB
argument_list|,
name|TS
argument_list|,
name|COLB
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|COLC
argument_list|,
name|TS
argument_list|,
name|COLC
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// check get request
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|COLA
argument_list|)
expr_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|COLC
argument_list|)
expr_stmt|;
name|Result
name|getResult
init|=
name|t
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|expected
argument_list|,
name|getResult
operator|.
name|rawCells
argument_list|()
argument_list|)
expr_stmt|;
comment|// check scan request
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|COLA
argument_list|)
expr_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|COLC
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
name|List
name|scanResult
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
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
name|scanResult
operator|.
name|addAll
argument_list|(
name|result
operator|.
name|listCells
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertArrayEquals
argument_list|(
name|expected
argument_list|,
name|scanResult
operator|.
name|toArray
argument_list|(
operator|new
name|Cell
index|[
name|scanResult
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

