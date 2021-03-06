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
name|HRegionInfo
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
name|HTestConst
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
name|SmallTests
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
comment|/**  * Test scan/get offset and limit settings within one row through HRegion API.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|SmallTests
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
name|TestIntraRowPagination
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
name|TestIntraRowPagination
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
comment|/**    * Test from client side for scan with maxResultPerCF set    *    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testScanLimitAndOffset
parameter_list|()
throws|throws
name|Exception
block|{
comment|//byte [] TABLE = HTestConst.DEFAULT_TABLE_BYTES;
name|byte
index|[]
index|[]
name|ROWS
init|=
name|HTestConst
operator|.
name|makeNAscii
argument_list|(
name|HTestConst
operator|.
name|DEFAULT_ROW_BYTES
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|FAMILIES
init|=
name|HTestConst
operator|.
name|makeNAscii
argument_list|(
name|HTestConst
operator|.
name|DEFAULT_CF_BYTES
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|QUALIFIERS
init|=
name|HTestConst
operator|.
name|makeNAscii
argument_list|(
name|HTestConst
operator|.
name|DEFAULT_QUALIFIER_BYTES
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
name|tableDescriptor
init|=
operator|new
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|HTestConst
operator|.
name|DEFAULT_TABLE_BYTES
argument_list|)
argument_list|)
decl_stmt|;
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|HTestConst
operator|.
name|DEFAULT_TABLE
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|FAMILIES
control|)
block|{
name|ColumnFamilyDescriptor
name|familyDescriptor
init|=
operator|new
name|ColumnFamilyDescriptorBuilder
operator|.
name|ModifyableColumnFamilyDescriptor
argument_list|(
name|family
argument_list|)
decl_stmt|;
name|tableDescriptor
operator|.
name|setColumnFamily
argument_list|(
name|familyDescriptor
argument_list|)
expr_stmt|;
block|}
name|HRegion
name|region
init|=
name|HBaseTestingUtility
operator|.
name|createRegionAndWAL
argument_list|(
name|info
argument_list|,
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableDescriptor
argument_list|)
decl_stmt|;
try|try
block|{
name|Put
name|put
decl_stmt|;
name|Scan
name|scan
decl_stmt|;
name|Result
name|result
decl_stmt|;
name|boolean
name|toLog
init|=
literal|true
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|kvListExp
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|storeOffset
init|=
literal|1
decl_stmt|;
name|int
name|storeLimit
init|=
literal|3
decl_stmt|;
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
name|ROWS
operator|.
name|length
condition|;
name|r
operator|++
control|)
block|{
name|put
operator|=
operator|new
name|Put
argument_list|(
name|ROWS
index|[
name|r
index|]
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
name|FAMILIES
operator|.
name|length
condition|;
name|c
operator|++
control|)
block|{
for|for
control|(
name|int
name|q
init|=
literal|0
init|;
name|q
operator|<
name|QUALIFIERS
operator|.
name|length
condition|;
name|q
operator|++
control|)
block|{
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|ROWS
index|[
name|r
index|]
argument_list|,
name|FAMILIES
index|[
name|c
index|]
argument_list|,
name|QUALIFIERS
index|[
name|q
index|]
argument_list|,
literal|1
argument_list|,
name|HTestConst
operator|.
name|DEFAULT_VALUE_BYTES
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
if|if
condition|(
name|storeOffset
operator|<=
name|q
operator|&&
name|q
operator|<
name|storeOffset
operator|+
name|storeLimit
condition|)
block|{
name|kvListExp
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
name|scan
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
name|scan
operator|.
name|setRowOffsetPerColumnFamily
argument_list|(
name|storeOffset
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setMaxResultsPerColumnFamily
argument_list|(
name|storeLimit
argument_list|)
expr_stmt|;
name|RegionScanner
name|scanner
init|=
name|region
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
name|kvListScan
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
operator|||
operator|!
name|results
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|kvListScan
operator|.
name|addAll
argument_list|(
name|results
argument_list|)
expr_stmt|;
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|result
operator|=
name|Result
operator|.
name|create
argument_list|(
name|kvListScan
argument_list|)
expr_stmt|;
name|TestScannersFromClientSide
operator|.
name|verifyResult
argument_list|(
name|result
argument_list|,
name|kvListExp
argument_list|,
name|toLog
argument_list|,
literal|"Testing scan with storeOffset and storeLimit"
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|HBaseTestingUtility
operator|.
name|closeRegionAndWAL
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

