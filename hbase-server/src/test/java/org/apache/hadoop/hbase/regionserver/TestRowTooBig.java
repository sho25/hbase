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
name|regionserver
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
name|fs
operator|.
name|Path
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
name|ColumnFamilyDescriptorBuilder
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
name|Get
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
name|RowTooBigException
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
name|TableDescriptorBuilder
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
name|testclassification
operator|.
name|RegionServerTests
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
comment|/**  * Test case to check HRS throws {@link org.apache.hadoop.hbase.client.RowTooBigException}  * when row size exceeds configured limits.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
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
name|TestRowTooBig
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
name|TestRowTooBig
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|HTU
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Path
name|rootRegionDir
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
name|TEST_TD
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
name|TestRowTooBig
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|before
parameter_list|()
throws|throws
name|Exception
block|{
name|HTU
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|HTU
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|TABLE_MAX_ROWSIZE_KEY
argument_list|,
literal|10
operator|*
literal|1024
operator|*
literal|1024L
argument_list|)
expr_stmt|;
name|rootRegionDir
operator|=
name|HTU
operator|.
name|getDataTestDirOnTestFS
argument_list|(
literal|"TestRowTooBig"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|after
parameter_list|()
throws|throws
name|Exception
block|{
name|HTU
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Usecase:    *  - create a row with 5 large  cells (5 Mb each)    *  - flush memstore but don't compact storefiles.    *  - try to Get whole row.    *    * OOME happened before we actually get to reading results, but    * during seeking, as each StoreFile gets it's own scanner,    * and each scanner seeks after the first KV.    * @throws IOException    */
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|RowTooBigException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testScannersSeekOnFewLargeCells
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|row1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|fam1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam1"
argument_list|)
decl_stmt|;
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
name|tableDescriptor
init|=
name|TEST_TD
decl_stmt|;
name|ColumnFamilyDescriptorBuilder
operator|.
name|ModifyableColumnFamilyDescriptor
name|familyDescriptor
init|=
operator|new
name|ColumnFamilyDescriptorBuilder
operator|.
name|ModifyableColumnFamilyDescriptor
argument_list|(
name|fam1
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableDescriptor
operator|.
name|hasColumnFamily
argument_list|(
name|familyDescriptor
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|tableDescriptor
operator|.
name|modifyColumnFamily
argument_list|(
name|familyDescriptor
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tableDescriptor
operator|.
name|setColumnFamily
argument_list|(
name|familyDescriptor
argument_list|)
expr_stmt|;
block|}
specifier|final
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|HBaseTestingUtility
operator|.
name|createRegionAndWAL
argument_list|(
name|hri
argument_list|,
name|rootRegionDir
argument_list|,
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableDescriptor
argument_list|)
decl_stmt|;
try|try
block|{
comment|// Add 5 cells to memstore
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|5
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
name|row1
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
literal|5
operator|*
literal|1024
operator|*
literal|1024
index|]
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|fam1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col_"
operator|+
name|i
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|region
operator|.
name|get
argument_list|(
name|get
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
comment|/**    * Usecase:    *    *  - create a row with 1M cells, 10 bytes in each    *  - flush& run major compaction    *  - try to Get whole row.    *    *  OOME happened in StoreScanner.next(..).    *    * @throws IOException    */
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|RowTooBigException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testScanAcrossManySmallColumns
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|row1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|fam1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam1"
argument_list|)
decl_stmt|;
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
name|tableDescriptor
init|=
name|TEST_TD
decl_stmt|;
name|ColumnFamilyDescriptorBuilder
operator|.
name|ModifyableColumnFamilyDescriptor
name|hcd
init|=
operator|new
name|ColumnFamilyDescriptorBuilder
operator|.
name|ModifyableColumnFamilyDescriptor
argument_list|(
name|fam1
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableDescriptor
operator|.
name|hasColumnFamily
argument_list|(
name|hcd
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|tableDescriptor
operator|.
name|modifyColumnFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tableDescriptor
operator|.
name|setColumnFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
block|}
specifier|final
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|HBaseTestingUtility
operator|.
name|createRegionAndWAL
argument_list|(
name|hri
argument_list|,
name|rootRegionDir
argument_list|,
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableDescriptor
argument_list|)
decl_stmt|;
try|try
block|{
comment|// Add to memstore
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
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
name|row1
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|10
operator|*
literal|10000
condition|;
name|j
operator|++
control|)
block|{
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
literal|10
index|]
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|fam1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col_"
operator|+
name|i
operator|+
literal|"_"
operator|+
name|j
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|region
operator|.
name|compact
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|region
operator|.
name|get
argument_list|(
name|get
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

