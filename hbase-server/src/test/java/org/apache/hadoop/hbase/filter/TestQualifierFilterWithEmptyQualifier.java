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
name|CellComparator
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
name|RegionInfo
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
name|RegionInfoBuilder
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
name|TableDescriptor
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
name|InternalScanner
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
name|SmallTests
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
name|Before
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
comment|/**  * Test qualifierFilter with empty qualifier column  */
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
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestQualifierFilterWithEmptyQualifier
block|{
specifier|private
specifier|final
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestQualifierFilterWithEmptyQualifier
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TestQualifierFilterWithEmptyQualifier
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
name|HRegion
name|region
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
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|ROWS
init|=
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testRowOne-0"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testRowOne-1"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testRowOne-2"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testRowOne-3"
argument_list|)
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testFamily"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|QUALIFIERS
init|=
block|{
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testQualifier"
argument_list|)
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VALUE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testValueOne"
argument_list|)
decl_stmt|;
specifier|private
name|long
name|numRows
init|=
operator|(
name|long
operator|)
name|ROWS
operator|.
name|length
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|TableDescriptor
name|htd
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestQualifierFilter"
argument_list|)
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|FAMILY
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
name|info
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|this
operator|.
name|region
operator|=
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
name|htd
argument_list|)
expr_stmt|;
comment|// Insert data
for|for
control|(
name|byte
index|[]
name|ROW
range|:
name|ROWS
control|)
block|{
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
name|setDurability
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|QUALIFIER
range|:
name|QUALIFIERS
control|)
block|{
name|p
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
comment|// Flush
name|this
operator|.
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseTestingUtility
operator|.
name|closeRegionAndWAL
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testQualifierFilterWithEmptyColumn
parameter_list|()
throws|throws
name|IOException
block|{
name|long
name|colsPerRow
init|=
literal|2
decl_stmt|;
name|long
name|expectedKeys
init|=
name|colsPerRow
operator|/
literal|2
decl_stmt|;
name|Filter
name|f
init|=
operator|new
name|QualifierFilter
argument_list|(
name|CompareOperator
operator|.
name|EQUAL
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|QUALIFIERS
index|[
literal|0
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|s
operator|.
name|setFilter
argument_list|(
name|f
argument_list|)
expr_stmt|;
name|verifyScanNoEarlyOut
argument_list|(
name|s
argument_list|,
name|this
operator|.
name|numRows
argument_list|,
name|expectedKeys
argument_list|)
expr_stmt|;
name|expectedKeys
operator|=
name|colsPerRow
operator|/
literal|2
expr_stmt|;
name|f
operator|=
operator|new
name|QualifierFilter
argument_list|(
name|CompareOperator
operator|.
name|EQUAL
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|QUALIFIERS
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|s
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
name|s
operator|.
name|setFilter
argument_list|(
name|f
argument_list|)
expr_stmt|;
name|verifyScanNoEarlyOut
argument_list|(
name|s
argument_list|,
name|this
operator|.
name|numRows
argument_list|,
name|expectedKeys
argument_list|)
expr_stmt|;
name|expectedKeys
operator|=
name|colsPerRow
operator|/
literal|2
expr_stmt|;
name|f
operator|=
operator|new
name|QualifierFilter
argument_list|(
name|CompareOperator
operator|.
name|GREATER
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|QUALIFIERS
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|s
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
name|s
operator|.
name|setFilter
argument_list|(
name|f
argument_list|)
expr_stmt|;
name|verifyScanNoEarlyOut
argument_list|(
name|s
argument_list|,
name|this
operator|.
name|numRows
argument_list|,
name|expectedKeys
argument_list|)
expr_stmt|;
name|expectedKeys
operator|=
name|colsPerRow
expr_stmt|;
name|f
operator|=
operator|new
name|QualifierFilter
argument_list|(
name|CompareOperator
operator|.
name|GREATER_OR_EQUAL
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|QUALIFIERS
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|s
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
name|s
operator|.
name|setFilter
argument_list|(
name|f
argument_list|)
expr_stmt|;
name|verifyScanNoEarlyOut
argument_list|(
name|s
argument_list|,
name|this
operator|.
name|numRows
argument_list|,
name|expectedKeys
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyScanNoEarlyOut
parameter_list|(
name|Scan
name|s
parameter_list|,
name|long
name|expectedRows
parameter_list|,
name|long
name|expectedKeys
parameter_list|)
throws|throws
name|IOException
block|{
name|InternalScanner
name|scanner
init|=
name|this
operator|.
name|region
operator|.
name|getScanner
argument_list|(
name|s
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
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|boolean
name|done
init|=
literal|true
init|;
name|done
condition|;
name|i
operator|++
control|)
block|{
name|done
operator|=
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|results
operator|.
name|toArray
argument_list|(
operator|new
name|Cell
index|[
name|results
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|CellComparator
operator|.
name|getInstance
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"counter="
operator|+
name|i
operator|+
literal|", "
operator|+
name|results
argument_list|)
expr_stmt|;
if|if
condition|(
name|results
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
break|break;
block|}
name|assertTrue
argument_list|(
literal|"Scanned too many rows! Only expected "
operator|+
name|expectedRows
operator|+
literal|" total but already scanned "
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
argument_list|,
name|expectedRows
operator|>
name|i
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Expected "
operator|+
name|expectedKeys
operator|+
literal|" keys per row but "
operator|+
literal|"returned "
operator|+
name|results
operator|.
name|size
argument_list|()
argument_list|,
name|expectedKeys
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Expected "
operator|+
name|expectedRows
operator|+
literal|" rows but scanned "
operator|+
name|i
operator|+
literal|" rows"
argument_list|,
name|expectedRows
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

