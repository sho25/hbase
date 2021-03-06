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
name|coprocessor
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
name|java
operator|.
name|util
operator|.
name|Optional
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
name|CellBuilderFactory
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
name|CellBuilderType
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
name|Append
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
name|Increment
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
name|Row
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
name|CoprocessorTests
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|CoprocessorTests
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
name|TestIncrementAndAppendWithNullResult
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
name|TestIncrementAndAppendWithNullResult
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|util
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|TEST_TABLE
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TEST_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW_A
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|qualifierCol1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Table
name|table
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|util
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|REGION_COPROCESSOR_CONF_KEY
argument_list|,
name|MyObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// reduce the retry count so as to speed up the test
name|util
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|util
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|table
operator|=
name|util
operator|.
name|createTable
argument_list|(
name|TEST_TABLE
argument_list|,
name|TEST_FAMILY
argument_list|)
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
name|util
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|MyObserver
implements|implements
name|RegionCoprocessor
implements|,
name|RegionObserver
block|{
specifier|private
specifier|static
specifier|final
name|Result
name|TMP_RESULT
init|=
name|Result
operator|.
name|create
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|CellBuilderFactory
operator|.
name|create
argument_list|(
name|CellBuilderType
operator|.
name|SHALLOW_COPY
argument_list|)
operator|.
name|setRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
argument_list|)
operator|.
name|setFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|)
operator|.
name|setQualifier
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier"
argument_list|)
argument_list|)
operator|.
name|setType
argument_list|(
name|Cell
operator|.
name|Type
operator|.
name|Put
argument_list|)
operator|.
name|setValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|RegionObserver
argument_list|>
name|getRegionObserver
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|preIncrementAfterRowLock
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Increment
name|increment
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|TMP_RESULT
return|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|postIncrement
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Increment
name|increment
parameter_list|,
name|Result
name|result
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|postAppend
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Append
name|append
parameter_list|,
name|Result
name|result
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|preAppendAfterRowLock
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Append
name|append
parameter_list|)
block|{
return|return
name|TMP_RESULT
return|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIncrement
parameter_list|()
throws|throws
name|Exception
block|{
name|testAppend
argument_list|(
operator|new
name|Increment
argument_list|(
name|ROW_A
argument_list|)
operator|.
name|addColumn
argument_list|(
name|TEST_FAMILY
argument_list|,
name|qualifierCol1
argument_list|,
literal|10L
argument_list|)
argument_list|)
expr_stmt|;
name|testAppend
argument_list|(
operator|new
name|Increment
argument_list|(
name|ROW_A
argument_list|)
operator|.
name|addColumn
argument_list|(
name|TEST_FAMILY
argument_list|,
name|qualifierCol1
argument_list|,
literal|10L
argument_list|)
operator|.
name|setReturnResults
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testAppend
parameter_list|(
name|Increment
name|inc
parameter_list|)
throws|throws
name|Exception
block|{
name|checkResult
argument_list|(
name|table
operator|.
name|increment
argument_list|(
name|inc
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Row
argument_list|>
name|actions
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|inc
argument_list|,
name|inc
argument_list|)
decl_stmt|;
name|Object
index|[]
name|results
init|=
operator|new
name|Object
index|[
name|actions
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|table
operator|.
name|batch
argument_list|(
name|actions
argument_list|,
name|results
argument_list|)
expr_stmt|;
name|checkResult
argument_list|(
name|results
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAppend
parameter_list|()
throws|throws
name|Exception
block|{
name|testAppend
argument_list|(
operator|new
name|Append
argument_list|(
name|ROW_A
argument_list|)
operator|.
name|addColumn
argument_list|(
name|TEST_FAMILY
argument_list|,
name|qualifierCol1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|testAppend
argument_list|(
operator|new
name|Append
argument_list|(
name|ROW_A
argument_list|)
operator|.
name|addColumn
argument_list|(
name|TEST_FAMILY
argument_list|,
name|qualifierCol1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
argument_list|)
operator|.
name|setReturnResults
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testAppend
parameter_list|(
name|Append
name|append
parameter_list|)
throws|throws
name|Exception
block|{
name|checkResult
argument_list|(
name|table
operator|.
name|append
argument_list|(
name|append
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Row
argument_list|>
name|actions
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|append
argument_list|,
name|append
argument_list|)
decl_stmt|;
name|Object
index|[]
name|results
init|=
operator|new
name|Object
index|[
name|actions
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|table
operator|.
name|batch
argument_list|(
name|actions
argument_list|,
name|results
argument_list|)
expr_stmt|;
name|checkResult
argument_list|(
name|results
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|checkResult
parameter_list|(
name|Result
name|r
parameter_list|)
block|{
name|checkResult
argument_list|(
operator|new
name|Object
index|[]
block|{
name|r
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|checkResult
parameter_list|(
name|Object
index|[]
name|results
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|!=
name|results
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|assertNotNull
argument_list|(
literal|"The result["
operator|+
name|i
operator|+
literal|"] should not be null"
argument_list|,
name|results
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"The result["
operator|+
name|i
operator|+
literal|"] should be Result type"
argument_list|,
name|results
index|[
name|i
index|]
operator|instanceof
name|Result
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"The result["
operator|+
name|i
operator|+
literal|"] shuold be empty"
argument_list|,
operator|(
operator|(
name|Result
operator|)
name|results
index|[
name|i
index|]
operator|)
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

