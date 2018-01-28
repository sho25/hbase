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
name|assertNotNull
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
name|assertNull
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

begin_comment
comment|/**  * Test if Filter is incompatible with scan-limits  */
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
name|TestFilterWithScanLimits
extends|extends
name|FilterTestingCluster
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
name|TestFilterWithScanLimits
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
name|TestFilterWithScanLimits
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"scanWithLimit"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|columnFamily
init|=
literal|"f1"
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testScanWithLimit
parameter_list|()
block|{
name|int
name|kv_number
init|=
literal|0
decl_stmt|;
try|try
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
comment|// set batch number as 2, which means each Result should contain 2 KVs at most
name|scan
operator|.
name|setBatch
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|SingleColumnValueFilter
name|filter
init|=
operator|new
name|SingleColumnValueFilter
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|columnFamily
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c5"
argument_list|)
argument_list|,
name|CompareOperator
operator|.
name|EQUAL
argument_list|,
operator|new
name|SubstringComparator
argument_list|(
literal|"2_c5"
argument_list|)
argument_list|)
decl_stmt|;
comment|// add filter after batch defined
name|scan
operator|.
name|setFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|openTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
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
comment|// Expect to get following row
comment|// row2 =><f1:c1, 2_c1>,<f1:c2, 2_c2>,
comment|// row2 =><f1:c3, 2_c3>,<f1:c4, 2_c4>,
comment|// row2 =><f1:c5, 2_c5>
for|for
control|(
name|Result
name|result
range|:
name|scanner
control|)
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
name|kv_number
operator|++
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|kv_number
operator|+
literal|". kv: "
operator|+
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// no correct result is expected
name|assertNotNull
argument_list|(
literal|"No IncompatibleFilterException catched"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"check the fetched kv number"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"We should not get result(s) returned."
argument_list|,
literal|0
argument_list|,
name|kv_number
argument_list|)
expr_stmt|;
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|prepareData
parameter_list|()
block|{
try|try
block|{
name|createTable
argument_list|(
name|tableName
argument_list|,
name|columnFamily
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|openTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|List
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
comment|// row1 =><f1:c1, 1_c1>,<f1:c2, 1_c2>,<f1:c3, 1_c3>,<f1:c4,1_c4>,
comment|//<f1:c5, 1_c5>
comment|// row2 =><f1:c1, 2_c1>,<f1:c2, 2_c2>,<f1:c3, 2_c3>,<f1:c4,2_c4>,
comment|//<f1:c5, 2_c5>
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
literal|4
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
literal|"row"
operator|+
name|i
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|1
init|;
name|j
operator|<
literal|6
condition|;
name|j
operator|++
control|)
block|{
name|put
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c"
operator|+
name|j
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
operator|+
literal|"_c"
operator|+
name|j
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|assertNull
argument_list|(
literal|"Exception found while putting data into table"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

