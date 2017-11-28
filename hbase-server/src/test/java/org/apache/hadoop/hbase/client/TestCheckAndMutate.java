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
name|client
package|;
end_package

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
name|regionserver
operator|.
name|NoSuchColumnFamilyException
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
name|java
operator|.
name|io
operator|.
name|IOException
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

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestCheckAndMutate
block|{
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
name|ROWKEY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"12345"
argument_list|)
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
literal|"cf"
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
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
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
name|Table
name|createTable
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|tableName
operator|.
name|getName
argument_list|()
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
return|return
name|table
return|;
block|}
specifier|private
name|void
name|putOneRow
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|IOException
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROWKEY
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"B"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c"
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|getOneRowAndAssertAllExist
parameter_list|(
specifier|final
name|Table
name|table
parameter_list|)
throws|throws
name|IOException
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|ROWKEY
argument_list|)
decl_stmt|;
name|Result
name|result
init|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Column A value should be a"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|equals
argument_list|(
literal|"a"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Column B value should be b"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"B"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|equals
argument_list|(
literal|"b"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Column C value should be c"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|equals
argument_list|(
literal|"c"
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|getOneRowAndAssertAllButCExist
parameter_list|(
specifier|final
name|Table
name|table
parameter_list|)
throws|throws
name|IOException
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|ROWKEY
argument_list|)
decl_stmt|;
name|Result
name|result
init|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Column A value should be a"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|equals
argument_list|(
literal|"a"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Column B value should be b"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"B"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|equals
argument_list|(
literal|"b"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Column C should not exist"
argument_list|,
name|result
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C"
argument_list|)
argument_list|)
operator|==
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
name|RowMutations
name|makeRowMutationsWithColumnCDeleted
parameter_list|()
throws|throws
name|IOException
block|{
name|RowMutations
name|rm
init|=
operator|new
name|RowMutations
argument_list|(
name|ROWKEY
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROWKEY
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"B"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
argument_list|)
expr_stmt|;
name|rm
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|Delete
name|del
init|=
operator|new
name|Delete
argument_list|(
name|ROWKEY
argument_list|)
decl_stmt|;
name|del
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C"
argument_list|)
argument_list|)
expr_stmt|;
name|rm
operator|.
name|add
argument_list|(
name|del
argument_list|)
expr_stmt|;
return|return
name|rm
return|;
block|}
specifier|private
name|RowMutations
name|getBogusRowMutations
parameter_list|()
throws|throws
name|IOException
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|ROWKEY
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|'b'
block|,
literal|'o'
block|,
literal|'g'
block|,
literal|'u'
block|,
literal|'s'
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|'A'
block|}
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|RowMutations
name|rm
init|=
operator|new
name|RowMutations
argument_list|(
name|ROWKEY
argument_list|)
decl_stmt|;
name|rm
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
return|return
name|rm
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCheckAndMutate
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
init|(
name|Table
name|table
init|=
name|createTable
argument_list|()
init|)
block|{
comment|// put one row
name|putOneRow
argument_list|(
name|table
argument_list|)
expr_stmt|;
comment|// get row back and assert the values
name|getOneRowAndAssertAllExist
argument_list|(
name|table
argument_list|)
expr_stmt|;
comment|// put the same row again with C column deleted
name|RowMutations
name|rm
init|=
name|makeRowMutationsWithColumnCDeleted
argument_list|()
decl_stmt|;
name|boolean
name|res
init|=
name|table
operator|.
name|checkAndMutate
argument_list|(
name|ROWKEY
argument_list|,
name|FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
argument_list|,
name|CompareOperator
operator|.
name|EQUAL
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|rm
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|res
argument_list|)
expr_stmt|;
comment|// get row back and assert the values
name|getOneRowAndAssertAllButCExist
argument_list|(
name|table
argument_list|)
expr_stmt|;
comment|//Test that we get a region level exception
try|try
block|{
name|rm
operator|=
name|getBogusRowMutations
argument_list|()
expr_stmt|;
name|table
operator|.
name|checkAndMutate
argument_list|(
name|ROWKEY
argument_list|,
name|FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
argument_list|,
name|CompareOperator
operator|.
name|EQUAL
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|rm
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected NoSuchColumnFamilyException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RetriesExhaustedWithDetailsException
name|e
parameter_list|)
block|{
try|try
block|{
throw|throw
name|e
operator|.
name|getCause
argument_list|(
literal|0
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|NoSuchColumnFamilyException
name|e1
parameter_list|)
block|{
comment|// expected
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

