begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|assertTrue
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
name|LinkedList
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
name|TreeSet
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
name|filter
operator|.
name|CompareFilter
operator|.
name|CompareOp
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
name|MultiRowRangeFilter
operator|.
name|RowRange
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
name|shaded
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|TestFilterSerialization
block|{
annotation|@
name|Test
specifier|public
name|void
name|testColumnCountGetFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|ColumnCountGetFilter
name|columnCountGetFilter
init|=
operator|new
name|ColumnCountGetFilter
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|columnCountGetFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|columnCountGetFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testColumnPaginationFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|ColumnPaginationFilter
name|columnPaginationFilter
init|=
operator|new
name|ColumnPaginationFilter
argument_list|(
literal|1
argument_list|,
literal|7
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|columnPaginationFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|columnPaginationFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testColumnPrefixFilter
parameter_list|()
throws|throws
name|Exception
block|{
comment|// empty string
name|ColumnPrefixFilter
name|columnPrefixFilter
init|=
operator|new
name|ColumnPrefixFilter
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|""
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|columnPrefixFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|columnPrefixFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// non-empty string
name|columnPrefixFilter
operator|=
operator|new
name|ColumnPrefixFilter
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|columnPrefixFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|columnPrefixFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testColumnRangeFilter
parameter_list|()
throws|throws
name|Exception
block|{
comment|// null columns
name|ColumnRangeFilter
name|columnRangeFilter
init|=
operator|new
name|ColumnRangeFilter
argument_list|(
literal|null
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|columnRangeFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|columnRangeFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// non-null columns
name|columnRangeFilter
operator|=
operator|new
name|ColumnRangeFilter
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|,
literal|false
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|columnRangeFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|columnRangeFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDependentColumnFilter
parameter_list|()
throws|throws
name|Exception
block|{
comment|// null column qualifier/family
name|DependentColumnFilter
name|dependentColumnFilter
init|=
operator|new
name|DependentColumnFilter
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|dependentColumnFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|dependentColumnFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// non-null column qualifier/family
name|dependentColumnFilter
operator|=
operator|new
name|DependentColumnFilter
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
argument_list|,
literal|true
argument_list|,
name|CompareOperator
operator|.
name|GREATER_OR_EQUAL
argument_list|,
operator|new
name|BitComparator
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bitComparator"
argument_list|)
argument_list|,
name|BitComparator
operator|.
name|BitwiseOp
operator|.
name|OR
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dependentColumnFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|dependentColumnFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFamilyFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|FamilyFilter
name|familyFilter
init|=
operator|new
name|FamilyFilter
argument_list|(
name|CompareOperator
operator|.
name|EQUAL
argument_list|,
operator|new
name|BinaryPrefixComparator
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testValueOne"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|familyFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|familyFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFilterList
parameter_list|()
throws|throws
name|Exception
block|{
comment|// empty filter list
name|FilterList
name|filterList
init|=
operator|new
name|FilterList
argument_list|(
operator|new
name|LinkedList
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|filterList
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|filterList
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// non-empty filter list
name|LinkedList
argument_list|<
name|Filter
argument_list|>
name|list
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
operator|new
name|ColumnCountGetFilter
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
operator|new
name|RowFilter
argument_list|(
name|CompareOperator
operator|.
name|EQUAL
argument_list|,
operator|new
name|SubstringComparator
argument_list|(
literal|"testFilterList"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|filterList
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|filterList
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFilterWrapper
parameter_list|()
throws|throws
name|Exception
block|{
name|FilterWrapper
name|filterWrapper
init|=
operator|new
name|FilterWrapper
argument_list|(
operator|new
name|ColumnRangeFilter
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"e"
argument_list|)
argument_list|,
literal|false
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|filterWrapper
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|filterWrapper
argument_list|)
argument_list|)
argument_list|)
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
name|testFirstKeyValueMatchingQualifiersFilter
parameter_list|()
throws|throws
name|Exception
block|{
comment|// empty qualifiers set
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
name|set
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
name|FirstKeyValueMatchingQualifiersFilter
name|firstKeyValueMatchingQualifiersFilter
init|=
operator|new
name|FirstKeyValueMatchingQualifiersFilter
argument_list|(
name|set
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|firstKeyValueMatchingQualifiersFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|firstKeyValueMatchingQualifiersFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// non-empty qualifiers set
name|set
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col0"
argument_list|)
argument_list|)
expr_stmt|;
name|set
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
argument_list|)
expr_stmt|;
name|firstKeyValueMatchingQualifiersFilter
operator|=
operator|new
name|FirstKeyValueMatchingQualifiersFilter
argument_list|(
name|set
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|firstKeyValueMatchingQualifiersFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|firstKeyValueMatchingQualifiersFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFirstKeyOnlyFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|FirstKeyOnlyFilter
name|firstKeyOnlyFilter
init|=
operator|new
name|FirstKeyOnlyFilter
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|firstKeyOnlyFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|firstKeyOnlyFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFuzzyRowFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|LinkedList
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
name|fuzzyList
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
name|fuzzyList
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"999"
argument_list|)
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|1
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|fuzzyList
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"abcd"
argument_list|)
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|0
block|,
literal|1
block|,
literal|1
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|FuzzyRowFilter
name|fuzzyRowFilter
init|=
operator|new
name|FuzzyRowFilter
argument_list|(
name|fuzzyList
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|fuzzyRowFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|fuzzyRowFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInclusiveStopFilter
parameter_list|()
throws|throws
name|Exception
block|{
comment|// InclusveStopFilter with null stopRowKey
name|InclusiveStopFilter
name|inclusiveStopFilter
init|=
operator|new
name|InclusiveStopFilter
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|inclusiveStopFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|inclusiveStopFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// InclusveStopFilter with non-null stopRowKey
name|inclusiveStopFilter
operator|=
operator|new
name|InclusiveStopFilter
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"inclusiveStopFilter"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|inclusiveStopFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|inclusiveStopFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testKeyOnlyFilter
parameter_list|()
throws|throws
name|Exception
block|{
comment|// KeyOnlyFilter with lenAsVal
name|KeyOnlyFilter
name|keyOnlyFilter
init|=
operator|new
name|KeyOnlyFilter
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|keyOnlyFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|keyOnlyFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// KeyOnlyFilter without lenAsVal
name|keyOnlyFilter
operator|=
operator|new
name|KeyOnlyFilter
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|keyOnlyFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|keyOnlyFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultipleColumnPrefixFilter
parameter_list|()
throws|throws
name|Exception
block|{
comment|// empty array
name|byte
index|[]
index|[]
name|prefixes
init|=
literal|null
decl_stmt|;
name|MultipleColumnPrefixFilter
name|multipleColumnPrefixFilter
init|=
operator|new
name|MultipleColumnPrefixFilter
argument_list|(
name|prefixes
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|multipleColumnPrefixFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|multipleColumnPrefixFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// non-empty array
name|prefixes
operator|=
operator|new
name|byte
index|[
literal|2
index|]
index|[]
expr_stmt|;
name|prefixes
index|[
literal|0
index|]
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
expr_stmt|;
name|prefixes
index|[
literal|1
index|]
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|multipleColumnPrefixFilter
operator|=
operator|new
name|MultipleColumnPrefixFilter
argument_list|(
name|prefixes
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|multipleColumnPrefixFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|multipleColumnPrefixFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPageFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|PageFilter
name|pageFilter
init|=
operator|new
name|PageFilter
argument_list|(
literal|6
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|pageFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|pageFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPrefixFilter
parameter_list|()
throws|throws
name|Exception
block|{
comment|// null prefix
name|PrefixFilter
name|prefixFilter
init|=
operator|new
name|PrefixFilter
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|prefixFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|prefixFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// non-null prefix
name|prefixFilter
operator|=
operator|new
name|PrefixFilter
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"abc"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|prefixFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|prefixFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testQualifierFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|QualifierFilter
name|qualifierFilter
init|=
operator|new
name|QualifierFilter
argument_list|(
name|CompareOperator
operator|.
name|EQUAL
argument_list|,
operator|new
name|NullComparator
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|qualifierFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|qualifierFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRandomRowFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|RandomRowFilter
name|randomRowFilter
init|=
operator|new
name|RandomRowFilter
argument_list|(
operator|(
name|float
operator|)
literal|0.1
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|randomRowFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|randomRowFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRowFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|RowFilter
name|rowFilter
init|=
operator|new
name|RowFilter
argument_list|(
name|CompareOperator
operator|.
name|EQUAL
argument_list|,
operator|new
name|SubstringComparator
argument_list|(
literal|"testRowFilter"
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|rowFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|rowFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSingleColumnValueExcludeFilter
parameter_list|()
throws|throws
name|Exception
block|{
comment|// null family/column SingleColumnValueExcludeFilter
name|SingleColumnValueExcludeFilter
name|singleColumnValueExcludeFilter
init|=
operator|new
name|SingleColumnValueExcludeFilter
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|CompareOperator
operator|.
name|GREATER_OR_EQUAL
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|singleColumnValueExcludeFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|singleColumnValueExcludeFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// non-null family/column SingleColumnValueFilter
name|singleColumnValueExcludeFilter
operator|=
operator|new
name|SingleColumnValueExcludeFilter
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
argument_list|,
name|CompareOperator
operator|.
name|LESS_OR_EQUAL
argument_list|,
operator|new
name|NullComparator
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|singleColumnValueExcludeFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|singleColumnValueExcludeFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSingleColumnValueFilter
parameter_list|()
throws|throws
name|Exception
block|{
comment|// null family/column SingleColumnValueFilter
name|SingleColumnValueFilter
name|singleColumnValueFilter
init|=
operator|new
name|SingleColumnValueFilter
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|CompareOperator
operator|.
name|LESS
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|singleColumnValueFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|singleColumnValueFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// non-null family/column SingleColumnValueFilter
name|singleColumnValueFilter
operator|=
operator|new
name|SingleColumnValueFilter
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier"
argument_list|)
argument_list|,
name|CompareOperator
operator|.
name|NOT_EQUAL
argument_list|,
operator|new
name|NullComparator
argument_list|()
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|singleColumnValueFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|singleColumnValueFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSkipFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|SkipFilter
name|skipFilter
init|=
operator|new
name|SkipFilter
argument_list|(
operator|new
name|PageFilter
argument_list|(
literal|6
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|skipFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|skipFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTimestampsFilter
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Empty timestamp list
name|TimestampsFilter
name|timestampsFilter
init|=
operator|new
name|TimestampsFilter
argument_list|(
operator|new
name|LinkedList
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|timestampsFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|timestampsFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Non-empty timestamp list
name|LinkedList
argument_list|<
name|Long
argument_list|>
name|list
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|timestampsFilter
operator|=
operator|new
name|TimestampsFilter
argument_list|(
name|list
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|timestampsFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|timestampsFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testValueFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|ValueFilter
name|valueFilter
init|=
operator|new
name|ValueFilter
argument_list|(
name|CompareOperator
operator|.
name|NO_OP
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testValueOne"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|valueFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|valueFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWhileMatchFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|WhileMatchFilter
name|whileMatchFilter
init|=
operator|new
name|WhileMatchFilter
argument_list|(
operator|new
name|ColumnRangeFilter
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c"
argument_list|)
argument_list|,
literal|false
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"d"
argument_list|)
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|whileMatchFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|whileMatchFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultiRowRangeFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|RowRange
argument_list|>
name|ranges
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|ranges
operator|.
name|add
argument_list|(
operator|new
name|RowRange
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|30
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|40
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|ranges
operator|.
name|add
argument_list|(
operator|new
name|RowRange
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|10
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|20
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|ranges
operator|.
name|add
argument_list|(
operator|new
name|RowRange
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|60
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|70
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|MultiRowRangeFilter
name|multiRowRangeFilter
init|=
operator|new
name|MultiRowRangeFilter
argument_list|(
name|ranges
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|multiRowRangeFilter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|multiRowRangeFilter
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

