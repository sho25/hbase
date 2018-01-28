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
name|mapreduce
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintStream
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
name|conf
operator|.
name|Configuration
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
name|CellUtil
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
name|LargeTests
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
name|LauncherSecurityManager
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
name|util
operator|.
name|ToolRunner
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

begin_comment
comment|/**  * Basic test for the CopyTable M/R tool  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MapReduceTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestCopyTable
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
name|TestCopyTable
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
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
name|byte
index|[]
name|ROW1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FAMILY_A_STRING
init|=
literal|"a"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FAMILY_B_STRING
init|=
literal|"b"
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
name|FAMILY_A_STRING
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
name|FAMILY_B_STRING
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
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
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterClass
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
name|void
name|doCopyTableTest
parameter_list|(
name|boolean
name|bulkload
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|tableName1
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"1"
argument_list|)
decl_stmt|;
specifier|final
name|TableName
name|tableName2
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"2"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|COLUMN1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c1"
argument_list|)
decl_stmt|;
try|try
init|(
name|Table
name|t1
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName1
argument_list|,
name|FAMILY
argument_list|)
init|;
name|Table
name|t2
operator|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName2
argument_list|,
name|FAMILY
argument_list|)
init|)
block|{
comment|// put rows into the first table
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
name|p
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
name|p
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|COLUMN1
argument_list|,
name|COLUMN1
argument_list|)
expr_stmt|;
name|t1
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|CopyTable
name|copy
init|=
operator|new
name|CopyTable
argument_list|()
decl_stmt|;
name|int
name|code
decl_stmt|;
if|if
condition|(
name|bulkload
condition|)
block|{
name|code
operator|=
name|ToolRunner
operator|.
name|run
argument_list|(
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|copy
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"--new.name="
operator|+
name|tableName2
operator|.
name|getNameAsString
argument_list|()
block|,
literal|"--bulkload"
block|,
name|tableName1
operator|.
name|getNameAsString
argument_list|()
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|code
operator|=
name|ToolRunner
operator|.
name|run
argument_list|(
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|copy
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"--new.name="
operator|+
name|tableName2
operator|.
name|getNameAsString
argument_list|()
block|,
name|tableName1
operator|.
name|getNameAsString
argument_list|()
block|}
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"copy job failed"
argument_list|,
literal|0
argument_list|,
name|code
argument_list|)
expr_stmt|;
comment|// verify the data was copied into table 2
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
name|Get
name|g
init|=
operator|new
name|Get
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
name|Result
name|r
init|=
name|t2
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|r
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|r
operator|.
name|rawCells
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|COLUMN1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName1
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName2
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Simple end-to-end test    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testCopyTable
parameter_list|()
throws|throws
name|Exception
block|{
name|doCopyTableTest
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Simple end-to-end test with bulkload.    */
annotation|@
name|Test
specifier|public
name|void
name|testCopyTableWithBulkload
parameter_list|()
throws|throws
name|Exception
block|{
name|doCopyTableTest
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStartStopRow
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|tableName1
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"1"
argument_list|)
decl_stmt|;
specifier|final
name|TableName
name|tableName2
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"2"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|COLUMN1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c1"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|ROW0
init|=
name|Bytes
operator|.
name|toBytesBinary
argument_list|(
literal|"\\x01row0"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|ROW1
init|=
name|Bytes
operator|.
name|toBytesBinary
argument_list|(
literal|"\\x01row1"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|ROW2
init|=
name|Bytes
operator|.
name|toBytesBinary
argument_list|(
literal|"\\x01row2"
argument_list|)
decl_stmt|;
name|Table
name|t1
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName1
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
name|Table
name|t2
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName2
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
comment|// put rows into the first table
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|ROW0
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|COLUMN1
argument_list|,
name|COLUMN1
argument_list|)
expr_stmt|;
name|t1
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|ROW1
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|COLUMN1
argument_list|,
name|COLUMN1
argument_list|)
expr_stmt|;
name|t1
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|ROW2
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|COLUMN1
argument_list|,
name|COLUMN1
argument_list|)
expr_stmt|;
name|t1
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|CopyTable
name|copy
init|=
operator|new
name|CopyTable
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ToolRunner
operator|.
name|run
argument_list|(
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|copy
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"--new.name="
operator|+
name|tableName2
block|,
literal|"--startrow=\\x01row1"
block|,
literal|"--stoprow=\\x01row2"
block|,
name|tableName1
operator|.
name|getNameAsString
argument_list|()
block|}
argument_list|)
argument_list|)
expr_stmt|;
comment|// verify the data was copied into table 2
comment|// row1 exist, row0, row2 do not exist
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|ROW1
argument_list|)
decl_stmt|;
name|Result
name|r
init|=
name|t2
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|r
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|r
operator|.
name|rawCells
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|COLUMN1
argument_list|)
argument_list|)
expr_stmt|;
name|g
operator|=
operator|new
name|Get
argument_list|(
name|ROW0
argument_list|)
expr_stmt|;
name|r
operator|=
name|t2
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|r
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|g
operator|=
operator|new
name|Get
argument_list|(
name|ROW2
argument_list|)
expr_stmt|;
name|r
operator|=
name|t2
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|r
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|t1
operator|.
name|close
argument_list|()
expr_stmt|;
name|t2
operator|.
name|close
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName1
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName2
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test copy of table from sourceTable to targetTable all rows from family a    */
annotation|@
name|Test
specifier|public
name|void
name|testRenameFamily
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|sourceTable
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"source"
argument_list|)
decl_stmt|;
specifier|final
name|TableName
name|targetTable
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"-target"
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|families
init|=
block|{
name|FAMILY_A
block|,
name|FAMILY_B
block|}
decl_stmt|;
name|Table
name|t
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|sourceTable
argument_list|,
name|families
argument_list|)
decl_stmt|;
name|Table
name|t2
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|targetTable
argument_list|,
name|families
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|ROW1
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|FAMILY_A
argument_list|,
name|QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Data11"
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|FAMILY_B
argument_list|,
name|QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Data12"
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|FAMILY_A
argument_list|,
name|QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Data13"
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|ROW2
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|FAMILY_B
argument_list|,
name|QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Dat21"
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|FAMILY_A
argument_list|,
name|QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Data22"
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|FAMILY_B
argument_list|,
name|QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Data23"
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|long
name|currentTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"--new.name="
operator|+
name|targetTable
block|,
literal|"--families=a:b"
block|,
literal|"--all.cells"
block|,
literal|"--starttime="
operator|+
operator|(
name|currentTime
operator|-
literal|100000
operator|)
block|,
literal|"--endtime="
operator|+
operator|(
name|currentTime
operator|+
literal|100000
operator|)
block|,
literal|"--versions=1"
block|,
name|sourceTable
operator|.
name|getNameAsString
argument_list|()
block|}
decl_stmt|;
name|assertNull
argument_list|(
name|t2
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW1
argument_list|)
argument_list|)
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|runCopy
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|t2
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW1
argument_list|)
argument_list|)
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|Result
name|res
init|=
name|t2
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW1
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|b1
init|=
name|res
operator|.
name|getValue
argument_list|(
name|FAMILY_B
argument_list|,
name|QUALIFIER
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Data13"
argument_list|,
operator|new
name|String
argument_list|(
name|b1
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|t2
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW2
argument_list|)
argument_list|)
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|res
operator|=
name|t2
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW2
argument_list|)
argument_list|)
expr_stmt|;
name|b1
operator|=
name|res
operator|.
name|getValue
argument_list|(
name|FAMILY_A
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
comment|// Data from the family of B is not copied
name|assertNull
argument_list|(
name|b1
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test main method of CopyTable.    */
annotation|@
name|Test
specifier|public
name|void
name|testMainMethod
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|emptyArgs
init|=
block|{
literal|"-h"
block|}
decl_stmt|;
name|PrintStream
name|oldWriter
init|=
name|System
operator|.
name|err
decl_stmt|;
name|ByteArrayOutputStream
name|data
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|PrintStream
name|writer
init|=
operator|new
name|PrintStream
argument_list|(
name|data
argument_list|)
decl_stmt|;
name|System
operator|.
name|setErr
argument_list|(
name|writer
argument_list|)
expr_stmt|;
name|SecurityManager
name|SECURITY_MANAGER
init|=
name|System
operator|.
name|getSecurityManager
argument_list|()
decl_stmt|;
name|LauncherSecurityManager
name|newSecurityManager
init|=
operator|new
name|LauncherSecurityManager
argument_list|()
decl_stmt|;
name|System
operator|.
name|setSecurityManager
argument_list|(
name|newSecurityManager
argument_list|)
expr_stmt|;
try|try
block|{
name|CopyTable
operator|.
name|main
argument_list|(
name|emptyArgs
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should be exit"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|newSecurityManager
operator|.
name|getExitCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|System
operator|.
name|setErr
argument_list|(
name|oldWriter
argument_list|)
expr_stmt|;
name|System
operator|.
name|setSecurityManager
argument_list|(
name|SECURITY_MANAGER
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|data
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"rs.class"
argument_list|)
argument_list|)
expr_stmt|;
comment|// should print usage information
name|assertTrue
argument_list|(
name|data
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Usage:"
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|runCopy
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|status
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
operator|new
name|CopyTable
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
return|return
name|status
operator|==
literal|0
return|;
block|}
block|}
end_class

end_unit

