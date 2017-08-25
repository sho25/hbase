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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|instanceOf
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
name|assertArrayEquals
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
name|assertFalse
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
name|assertThat
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CompletableFuture
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadLocalRandom
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|IntStream
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
name|MediumTests
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
name|TestAsyncBufferMutator
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
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"async"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|CF
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|CQ
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|int
name|COUNT
init|=
literal|100
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|VALUE
init|=
operator|new
name|byte
index|[
literal|1024
index|]
decl_stmt|;
specifier|private
specifier|static
name|AsyncConnection
name|CONN
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
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|CF
argument_list|)
expr_stmt|;
name|CONN
operator|=
name|ConnectionFactory
operator|.
name|createAsyncConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextBytes
argument_list|(
name|VALUE
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
name|CONN
operator|.
name|close
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|List
argument_list|<
name|CompletableFuture
argument_list|<
name|Void
argument_list|>
argument_list|>
name|futures
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
try|try
init|(
name|AsyncBufferedMutator
name|mutator
init|=
name|CONN
operator|.
name|getBufferedMutatorBuilder
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|setWriteBufferSize
argument_list|(
literal|16
operator|*
literal|1024
argument_list|)
operator|.
name|build
argument_list|()
init|)
block|{
name|List
argument_list|<
name|CompletableFuture
argument_list|<
name|Void
argument_list|>
argument_list|>
name|fs
init|=
name|mutator
operator|.
name|mutate
argument_list|(
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|COUNT
operator|/
literal|2
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|i
lambda|->
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|,
name|VALUE
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
comment|// exceeded the write buffer size, a flush will be called directly
name|fs
operator|.
name|forEach
argument_list|(
name|f
lambda|->
name|f
operator|.
name|join
argument_list|()
argument_list|)
expr_stmt|;
name|IntStream
operator|.
name|range
argument_list|(
name|COUNT
operator|/
literal|2
argument_list|,
name|COUNT
argument_list|)
operator|.
name|forEach
argument_list|(
name|i
lambda|->
block|{
name|futures
operator|.
name|add
argument_list|(
name|mutator
operator|.
name|mutate
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|,
name|VALUE
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
comment|// the first future should have been sent out.
name|futures
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
comment|// the last one should still be in write buffer
name|assertFalse
argument_list|(
name|futures
operator|.
name|get
argument_list|(
name|futures
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|isDone
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// mutator.close will call mutator.flush automatically so all tasks should have been done.
name|futures
operator|.
name|forEach
argument_list|(
name|f
lambda|->
name|f
operator|.
name|join
argument_list|()
argument_list|)
expr_stmt|;
name|RawAsyncTable
name|table
init|=
name|CONN
operator|.
name|getRawTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|COUNT
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|i
lambda|->
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
operator|.
name|map
argument_list|(
name|g
lambda|->
name|table
operator|.
name|get
argument_list|(
name|g
argument_list|)
operator|.
name|join
argument_list|()
argument_list|)
operator|.
name|forEach
argument_list|(
name|r
lambda|->
block|{
name|assertArrayEquals
argument_list|(
name|VALUE
argument_list|,
name|r
operator|.
name|getValue
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|)
argument_list|)
argument_list|;
block|}
block|)
class|;
end_class

begin_function
unit|}    @
name|Test
specifier|public
name|void
name|testClosedMutate
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|AsyncBufferedMutator
name|mutator
init|=
name|CONN
operator|.
name|getBufferedMutator
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|mutator
operator|.
name|close
argument_list|()
expr_stmt|;
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
literal|0
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|,
name|VALUE
argument_list|)
decl_stmt|;
try|try
block|{
name|mutator
operator|.
name|mutate
argument_list|(
name|put
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Close check failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|IOException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"Already closed"
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|CompletableFuture
argument_list|<
name|Void
argument_list|>
name|f
range|:
name|mutator
operator|.
name|mutate
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|put
argument_list|)
argument_list|)
control|)
block|{
try|try
block|{
name|f
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Close check failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|IOException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"Already closed"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_function

unit|}
end_unit
