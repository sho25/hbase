begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|wal
operator|.
name|WAL
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
name|Assert
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
comment|/**  * Test the invocation logic of the filters. A filter must be invoked only for  * the columns that are requested for.  */
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
name|TestInvocationRecordFilter
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
name|TestInvocationRecordFilter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TABLE_NAME_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"invocationrecord"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY_NAME_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"mycf"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|QUALIFIER_PREFIX
init|=
literal|"qualifier"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|VALUE_PREFIX
init|=
literal|"value"
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
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
name|htd
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
name|TABLE_NAME_BYTES
argument_list|)
argument_list|)
decl_stmt|;
name|htd
operator|.
name|setColumnFamily
argument_list|(
operator|new
name|ColumnFamilyDescriptorBuilder
operator|.
name|ModifyableColumnFamilyDescriptor
argument_list|(
name|FAMILY_NAME_BYTES
argument_list|)
argument_list|)
expr_stmt|;
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
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
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW_BYTES
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
literal|10
condition|;
name|i
operator|+=
literal|2
control|)
block|{
comment|// puts 0, 2, 4, 6 and 8
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY_NAME_BYTES
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|QUALIFIER_PREFIX
operator|+
name|i
argument_list|)
argument_list|,
operator|(
name|long
operator|)
name|i
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_PREFIX
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
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
name|Test
specifier|public
name|void
name|testFilterInvocation
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Integer
argument_list|>
name|selectQualifiers
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|expectedQualifiers
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|selectQualifiers
operator|.
name|add
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|verifyInvocationResults
argument_list|(
name|selectQualifiers
operator|.
name|toArray
argument_list|(
operator|new
name|Integer
index|[
name|selectQualifiers
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|expectedQualifiers
operator|.
name|toArray
argument_list|(
operator|new
name|Integer
index|[
name|expectedQualifiers
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|selectQualifiers
operator|.
name|clear
argument_list|()
expr_stmt|;
name|selectQualifiers
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|expectedQualifiers
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|verifyInvocationResults
argument_list|(
name|selectQualifiers
operator|.
name|toArray
argument_list|(
operator|new
name|Integer
index|[
name|selectQualifiers
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|expectedQualifiers
operator|.
name|toArray
argument_list|(
operator|new
name|Integer
index|[
name|expectedQualifiers
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|selectQualifiers
operator|.
name|add
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|verifyInvocationResults
argument_list|(
name|selectQualifiers
operator|.
name|toArray
argument_list|(
operator|new
name|Integer
index|[
name|selectQualifiers
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|expectedQualifiers
operator|.
name|toArray
argument_list|(
operator|new
name|Integer
index|[
name|expectedQualifiers
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|selectQualifiers
operator|.
name|add
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|expectedQualifiers
operator|.
name|add
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|verifyInvocationResults
argument_list|(
name|selectQualifiers
operator|.
name|toArray
argument_list|(
operator|new
name|Integer
index|[
name|selectQualifiers
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|expectedQualifiers
operator|.
name|toArray
argument_list|(
operator|new
name|Integer
index|[
name|expectedQualifiers
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|selectQualifiers
operator|.
name|add
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|verifyInvocationResults
argument_list|(
name|selectQualifiers
operator|.
name|toArray
argument_list|(
operator|new
name|Integer
index|[
name|selectQualifiers
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|expectedQualifiers
operator|.
name|toArray
argument_list|(
operator|new
name|Integer
index|[
name|expectedQualifiers
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|selectQualifiers
operator|.
name|add
argument_list|(
literal|8
argument_list|)
expr_stmt|;
name|expectedQualifiers
operator|.
name|add
argument_list|(
literal|8
argument_list|)
expr_stmt|;
name|verifyInvocationResults
argument_list|(
name|selectQualifiers
operator|.
name|toArray
argument_list|(
operator|new
name|Integer
index|[
name|selectQualifiers
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|expectedQualifiers
operator|.
name|toArray
argument_list|(
operator|new
name|Integer
index|[
name|expectedQualifiers
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|verifyInvocationResults
parameter_list|(
name|Integer
index|[]
name|selectQualifiers
parameter_list|,
name|Integer
index|[]
name|expectedQualifiers
parameter_list|)
throws|throws
name|Exception
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|ROW_BYTES
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
name|selectQualifiers
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|get
operator|.
name|addColumn
argument_list|(
name|FAMILY_NAME_BYTES
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|QUALIFIER_PREFIX
operator|+
name|selectQualifiers
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|get
operator|.
name|setFilter
argument_list|(
operator|new
name|InvocationRecordFilter
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|expectedValues
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
name|expectedQualifiers
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|expectedValues
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|ROW_BYTES
argument_list|,
name|FAMILY_NAME_BYTES
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|QUALIFIER_PREFIX
operator|+
name|expectedQualifiers
index|[
name|i
index|]
argument_list|)
argument_list|,
name|expectedQualifiers
index|[
name|i
index|]
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_PREFIX
operator|+
name|expectedQualifiers
index|[
name|i
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|actualValues
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
name|temp
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|InternalScanner
name|scanner
init|=
name|this
operator|.
name|region
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|(
name|temp
argument_list|)
condition|)
block|{
name|actualValues
operator|.
name|addAll
argument_list|(
name|temp
argument_list|)
expr_stmt|;
name|temp
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|actualValues
operator|.
name|addAll
argument_list|(
name|temp
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Actual values "
operator|+
name|actualValues
operator|+
literal|" differ from the expected values:"
operator|+
name|expectedValues
argument_list|,
name|expectedValues
operator|.
name|equals
argument_list|(
name|actualValues
argument_list|)
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
name|WAL
name|wal
init|=
operator|(
operator|(
name|HRegion
operator|)
name|region
operator|)
operator|.
name|getWAL
argument_list|()
decl_stmt|;
operator|(
operator|(
name|HRegion
operator|)
name|region
operator|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|wal
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Filter which gives the list of keyvalues for which the filter is invoked.    */
specifier|private
specifier|static
class|class
name|InvocationRecordFilter
extends|extends
name|FilterBase
block|{
specifier|private
name|List
argument_list|<
name|Cell
argument_list|>
name|visitedKeyValues
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|visitedKeyValues
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterCell
parameter_list|(
specifier|final
name|Cell
name|ignored
parameter_list|)
block|{
name|visitedKeyValues
operator|.
name|add
argument_list|(
name|ignored
argument_list|)
expr_stmt|;
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|filterRowCells
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|kvs
parameter_list|)
block|{
name|kvs
operator|.
name|clear
argument_list|()
expr_stmt|;
name|kvs
operator|.
name|addAll
argument_list|(
name|visitedKeyValues
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasFilterRow
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
end_class

end_unit

