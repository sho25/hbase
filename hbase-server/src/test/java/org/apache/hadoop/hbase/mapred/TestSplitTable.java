begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapred
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
name|assertNotEquals
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
name|MapReduceTests
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
name|Assert
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
name|MapReduceTests
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
name|TestSplitTable
block|{
annotation|@
name|Test
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
name|void
name|testSplitTableCompareTo
parameter_list|()
block|{
name|TableSplit
name|aTableSplit
init|=
operator|new
name|TableSplit
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"tableA"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ddd"
argument_list|)
argument_list|,
literal|"locationA"
argument_list|)
decl_stmt|;
name|TableSplit
name|bTableSplit
init|=
operator|new
name|TableSplit
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"tableA"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"iii"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"kkk"
argument_list|)
argument_list|,
literal|"locationA"
argument_list|)
decl_stmt|;
name|TableSplit
name|cTableSplit
init|=
operator|new
name|TableSplit
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"tableA"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"lll"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzz"
argument_list|)
argument_list|,
literal|"locationA"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|aTableSplit
operator|.
name|compareTo
argument_list|(
name|aTableSplit
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bTableSplit
operator|.
name|compareTo
argument_list|(
name|bTableSplit
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cTableSplit
operator|.
name|compareTo
argument_list|(
name|cTableSplit
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|aTableSplit
operator|.
name|compareTo
argument_list|(
name|bTableSplit
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bTableSplit
operator|.
name|compareTo
argument_list|(
name|aTableSplit
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|aTableSplit
operator|.
name|compareTo
argument_list|(
name|cTableSplit
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cTableSplit
operator|.
name|compareTo
argument_list|(
name|aTableSplit
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bTableSplit
operator|.
name|compareTo
argument_list|(
name|cTableSplit
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cTableSplit
operator|.
name|compareTo
argument_list|(
name|bTableSplit
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cTableSplit
operator|.
name|compareTo
argument_list|(
name|aTableSplit
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
name|void
name|testSplitTableEquals
parameter_list|()
block|{
name|byte
index|[]
name|tableA
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"tableA"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|aaa
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|ddd
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ddd"
argument_list|)
decl_stmt|;
name|String
name|locationA
init|=
literal|"locationA"
decl_stmt|;
name|TableSplit
name|tablesplit
init|=
operator|new
name|TableSplit
argument_list|(
name|tableA
argument_list|,
name|aaa
argument_list|,
name|ddd
argument_list|,
name|locationA
argument_list|)
decl_stmt|;
name|TableSplit
name|tableB
init|=
operator|new
name|TableSplit
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"tableB"
argument_list|)
argument_list|,
name|aaa
argument_list|,
name|ddd
argument_list|,
name|locationA
argument_list|)
decl_stmt|;
name|assertNotEquals
argument_list|(
name|tablesplit
operator|.
name|hashCode
argument_list|()
argument_list|,
name|tableB
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|tablesplit
argument_list|,
name|tableB
argument_list|)
expr_stmt|;
name|TableSplit
name|startBbb
init|=
operator|new
name|TableSplit
argument_list|(
name|tableA
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
argument_list|,
name|ddd
argument_list|,
name|locationA
argument_list|)
decl_stmt|;
name|assertNotEquals
argument_list|(
name|tablesplit
operator|.
name|hashCode
argument_list|()
argument_list|,
name|startBbb
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|tablesplit
argument_list|,
name|startBbb
argument_list|)
expr_stmt|;
name|TableSplit
name|endEee
init|=
operator|new
name|TableSplit
argument_list|(
name|tableA
argument_list|,
name|aaa
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"eee"
argument_list|)
argument_list|,
name|locationA
argument_list|)
decl_stmt|;
name|assertNotEquals
argument_list|(
name|tablesplit
operator|.
name|hashCode
argument_list|()
argument_list|,
name|endEee
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|tablesplit
argument_list|,
name|endEee
argument_list|)
expr_stmt|;
name|TableSplit
name|locationB
init|=
operator|new
name|TableSplit
argument_list|(
name|tableA
argument_list|,
name|aaa
argument_list|,
name|ddd
argument_list|,
literal|"locationB"
argument_list|)
decl_stmt|;
name|assertNotEquals
argument_list|(
name|tablesplit
operator|.
name|hashCode
argument_list|()
argument_list|,
name|locationB
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|tablesplit
argument_list|,
name|locationB
argument_list|)
expr_stmt|;
name|TableSplit
name|same
init|=
operator|new
name|TableSplit
argument_list|(
name|tableA
argument_list|,
name|aaa
argument_list|,
name|ddd
argument_list|,
name|locationA
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|tablesplit
operator|.
name|hashCode
argument_list|()
argument_list|,
name|same
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tablesplit
argument_list|,
name|same
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
name|void
name|testToString
parameter_list|()
block|{
name|TableSplit
name|split
init|=
operator|new
name|TableSplit
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table"
argument_list|)
argument_list|,
literal|"row-start"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"row-end"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"location"
argument_list|)
decl_stmt|;
name|String
name|str
init|=
literal|"HBase table split(table name: table, start row: row-start, "
operator|+
literal|"end row: row-end, region location: location)"
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|str
argument_list|,
name|split
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|split
operator|=
operator|new
name|TableSplit
argument_list|(
operator|(
name|TableName
operator|)
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|str
operator|=
literal|"HBase table split(table name: null, start row: null, "
operator|+
literal|"end row: null, region location: null)"
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|str
argument_list|,
name|split
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

