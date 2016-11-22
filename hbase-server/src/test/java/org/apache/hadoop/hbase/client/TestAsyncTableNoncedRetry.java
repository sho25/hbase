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
name|concurrent
operator|.
name|ExecutionException
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
name|security
operator|.
name|User
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
name|Before
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
name|TestAsyncTableNoncedRetry
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
name|FAMILY
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
name|QUALIFIER
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
name|byte
index|[]
name|VALUE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|AsyncConnection
name|ASYNC_CONN
decl_stmt|;
specifier|private
specifier|static
name|long
name|NONCE
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|static
name|NonceGenerator
name|NONCE_GENERATOR
init|=
operator|new
name|NonceGenerator
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|long
name|newNonce
parameter_list|()
block|{
return|return
name|NONCE
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNonceGroup
parameter_list|()
block|{
return|return
literal|1L
return|;
block|}
block|}
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|testName
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|private
name|byte
index|[]
name|row
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
name|FAMILY
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|ASYNC_CONN
operator|=
operator|new
name|AsyncConnectionImpl
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|NonceGenerator
name|getNonceGenerator
parameter_list|()
block|{
return|return
name|NONCE_GENERATOR
return|;
block|}
block|}
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
name|ASYNC_CONN
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
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|row
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"[^0-9A-Za-z]"
argument_list|,
literal|"_"
argument_list|)
argument_list|)
expr_stmt|;
name|NONCE
operator|++
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAppend
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|RawAsyncTable
name|table
init|=
name|ASYNC_CONN
operator|.
name|getRawTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|Result
name|result
init|=
name|table
operator|.
name|append
argument_list|(
operator|new
name|Append
argument_list|(
name|row
argument_list|)
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|VALUE
argument_list|,
name|result
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
argument_list|)
expr_stmt|;
name|result
operator|=
name|table
operator|.
name|append
argument_list|(
operator|new
name|Append
argument_list|(
name|row
argument_list|)
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
comment|// the second call should have no effect as we always generate the same nonce.
name|assertArrayEquals
argument_list|(
name|VALUE
argument_list|,
name|result
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
argument_list|)
expr_stmt|;
name|result
operator|=
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|row
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|VALUE
argument_list|,
name|result
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIncrement
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|RawAsyncTable
name|table
init|=
name|ASYNC_CONN
operator|.
name|getRawTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|table
operator|.
name|incrementColumnValue
argument_list|(
name|row
argument_list|,
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
literal|1L
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
comment|// the second call should have no effect as we always generate the same nonce.
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|table
operator|.
name|incrementColumnValue
argument_list|(
name|row
argument_list|,
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
literal|1L
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|row
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

