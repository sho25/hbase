begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
import|import static
name|junit
operator|.
name|framework
operator|.
name|Assert
operator|.
name|assertEquals
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
name|Iterator
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
name|HColumnDescriptor
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
name|HTableDescriptor
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
name|TableExistsException
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
name|TableNotFoundException
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
name|Admin
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
name|HTable
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
name|filter
operator|.
name|BinaryComparator
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
name|Filter
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
name|SingleColumnValueFilter
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
name|io
operator|.
name|compress
operator|.
name|Compression
operator|.
name|Algorithm
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
name|RegionServerTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
comment|/*  * This test verifies that the scenarios illustrated by HBASE-10850 work  * w.r.t. essential column family optimization  */
specifier|public
class|class
name|TestSCVFWithMiniCluster
block|{
specifier|private
specifier|static
specifier|final
name|TableName
name|HBASE_TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestSCVFWithMiniCluster"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY_A
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY_B
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QUALIFIER_FOO
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QUALIFIER_BAR
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bar"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Table
name|htable
decl_stmt|;
specifier|private
specifier|static
name|Filter
name|scanFilter
decl_stmt|;
specifier|private
name|int
name|expected
init|=
literal|1
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
name|HBaseTestingUtility
name|util
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
name|util
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Admin
name|admin
init|=
name|util
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|destroy
argument_list|(
name|admin
argument_list|,
name|HBASE_TABLE_NAME
argument_list|)
expr_stmt|;
name|create
argument_list|(
name|admin
argument_list|,
name|HBASE_TABLE_NAME
argument_list|,
name|FAMILY_A
argument_list|,
name|FAMILY_B
argument_list|)
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
name|htable
operator|=
name|util
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|HBASE_TABLE_NAME
argument_list|)
expr_stmt|;
comment|/* Add some values */
name|List
argument_list|<
name|Put
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<
name|Put
argument_list|>
argument_list|()
decl_stmt|;
comment|/* Add a row with 'a:foo' = false */
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
literal|"1"
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY_A
argument_list|,
name|QUALIFIER_FOO
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"false"
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY_A
argument_list|,
name|QUALIFIER_BAR
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"_flag_"
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY_B
argument_list|,
name|QUALIFIER_FOO
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"_flag_"
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY_B
argument_list|,
name|QUALIFIER_BAR
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"_flag_"
argument_list|)
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
comment|/* Add a row with 'a:foo' = true */
name|put
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY_A
argument_list|,
name|QUALIFIER_FOO
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"true"
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY_A
argument_list|,
name|QUALIFIER_BAR
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"_flag_"
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY_B
argument_list|,
name|QUALIFIER_FOO
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"_flag_"
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY_B
argument_list|,
name|QUALIFIER_BAR
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"_flag_"
argument_list|)
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
comment|/* Add a row with 'a:foo' qualifier not set */
name|put
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY_A
argument_list|,
name|QUALIFIER_BAR
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"_flag_"
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY_B
argument_list|,
name|QUALIFIER_FOO
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"_flag_"
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY_B
argument_list|,
name|QUALIFIER_BAR
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"_flag_"
argument_list|)
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|htable
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
comment|/*      * We want to filter out from the scan all rows that do not have the column 'a:foo' with value      * 'false'. Only row with key '1' should be returned in the scan.      */
name|scanFilter
operator|=
operator|new
name|SingleColumnValueFilter
argument_list|(
name|FAMILY_A
argument_list|,
name|QUALIFIER_FOO
argument_list|,
name|CompareOp
operator|.
name|EQUAL
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"false"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
operator|(
operator|(
name|SingleColumnValueFilter
operator|)
name|scanFilter
operator|)
operator|.
name|setFilterIfMissing
argument_list|(
literal|true
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
name|htable
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|verify
parameter_list|(
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
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
name|Iterator
argument_list|<
name|Result
argument_list|>
name|it
init|=
name|scanner
operator|.
name|iterator
argument_list|()
decl_stmt|;
comment|/* Then */
name|int
name|count
init|=
literal|0
decl_stmt|;
try|try
block|{
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|it
operator|.
name|next
argument_list|()
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test the filter by adding all columns of family A in the scan. (OK)    */
annotation|@
name|Test
specifier|public
name|void
name|scanWithAllQualifiersOfFamiliyA
parameter_list|()
throws|throws
name|IOException
block|{
comment|/* Given */
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|FAMILY_A
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|scanFilter
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|scan
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test the filter by adding all columns of family A and B in the scan. (KO: row '3' without    * 'a:foo' qualifier is returned)    */
annotation|@
name|Test
specifier|public
name|void
name|scanWithAllQualifiersOfBothFamilies
parameter_list|()
throws|throws
name|IOException
block|{
comment|/* When */
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|scanFilter
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|scan
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test the filter by adding 2 columns of family A and 1 column of family B in the scan. (KO: row    * '3' without 'a:foo' qualifier is returned)    */
annotation|@
name|Test
specifier|public
name|void
name|scanWithSpecificQualifiers1
parameter_list|()
throws|throws
name|IOException
block|{
comment|/* When */
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|FAMILY_A
argument_list|,
name|QUALIFIER_FOO
argument_list|)
expr_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|FAMILY_A
argument_list|,
name|QUALIFIER_BAR
argument_list|)
expr_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|FAMILY_B
argument_list|,
name|QUALIFIER_BAR
argument_list|)
expr_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|FAMILY_B
argument_list|,
name|QUALIFIER_FOO
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|scanFilter
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|scan
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test the filter by adding 1 column of family A (the one used in the filter) and 1 column of    * family B in the scan. (OK)    */
annotation|@
name|Test
specifier|public
name|void
name|scanWithSpecificQualifiers2
parameter_list|()
throws|throws
name|IOException
block|{
comment|/* When */
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|FAMILY_A
argument_list|,
name|QUALIFIER_FOO
argument_list|)
expr_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|FAMILY_B
argument_list|,
name|QUALIFIER_BAR
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|scanFilter
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|scan
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test the filter by adding 2 columns of family A in the scan. (OK)    */
annotation|@
name|Test
specifier|public
name|void
name|scanWithSpecificQualifiers3
parameter_list|()
throws|throws
name|IOException
block|{
comment|/* When */
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|FAMILY_A
argument_list|,
name|QUALIFIER_FOO
argument_list|)
expr_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|FAMILY_A
argument_list|,
name|QUALIFIER_BAR
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|scanFilter
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|scan
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|create
parameter_list|(
name|Admin
name|admin
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|families
control|)
block|{
name|HColumnDescriptor
name|colDesc
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
decl_stmt|;
name|colDesc
operator|.
name|setMaxVersions
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|colDesc
operator|.
name|setCompressionType
argument_list|(
name|Algorithm
operator|.
name|GZ
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|colDesc
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableExistsException
name|tee
parameter_list|)
block|{
comment|/* Ignore */
block|}
block|}
specifier|private
specifier|static
name|void
name|destroy
parameter_list|(
name|Admin
name|admin
parameter_list|,
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableNotFoundException
name|tnfe
parameter_list|)
block|{
comment|/* Ignore */
block|}
block|}
block|}
end_class

end_unit

