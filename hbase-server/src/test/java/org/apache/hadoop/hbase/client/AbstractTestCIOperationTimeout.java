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
name|net
operator|.
name|SocketTimeoutException
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
name|Test
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
comment|/**  * Based class for testing operation timeout logic for {@link ConnectionImplementation}.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractTestCIOperationTimeout
extends|extends
name|AbstractTestCITimeout
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|AbstractTestCIOperationTimeout
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|TableName
name|tableName
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
name|tableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
name|TableDescriptor
name|htd
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|addCoprocessor
argument_list|(
name|SleepAndFailFirstTime
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|addColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|FAM_NAM
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|abstract
name|void
name|execute
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Test that an operation can fail if we read the global operation timeout, even if the individual    * timeout is fine. We do that with:    *<ul>    *<li>client side: an operation timeout of 30 seconds</li>    *<li>server side: we sleep 20 second at each attempt. The first work fails, the second one    * succeeds. But the client won't wait that much, because 20 + 20> 30, so the client timed out    * when the server answers.</li>    *</ul>    */
annotation|@
name|Test
specifier|public
name|void
name|testOperationTimeout
parameter_list|()
throws|throws
name|IOException
block|{
name|TableBuilder
name|builder
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTableBuilder
argument_list|(
name|tableName
argument_list|,
literal|null
argument_list|)
operator|.
name|setRpcTimeout
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|setReadRpcTimeout
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|setWriteRpcTimeout
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
comment|// Check that it works if the timeout is big enough
name|SleepAndFailFirstTime
operator|.
name|ct
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|builder
operator|.
name|setOperationTimeout
argument_list|(
literal|120
operator|*
literal|1000
argument_list|)
operator|.
name|build
argument_list|()
init|)
block|{
name|execute
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
comment|// Resetting and retrying. Will fail this time, not enough time for the second try
name|SleepAndFailFirstTime
operator|.
name|ct
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|builder
operator|.
name|setOperationTimeout
argument_list|(
literal|30
operator|*
literal|1000
argument_list|)
operator|.
name|build
argument_list|()
init|)
block|{
name|SleepAndFailFirstTime
operator|.
name|ct
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|execute
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"We expect an exception here"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SocketTimeoutException
decl||
name|RetriesExhaustedWithDetailsException
name|e
parameter_list|)
block|{
comment|// The client has a CallTimeout class, but it's not shared. We're not very clean today,
comment|// in the general case you can expect the call to stop, but the exception may vary.
comment|// In this test however, we're sure that it will be a socket timeout.
name|LOG
operator|.
name|info
argument_list|(
literal|"We received an exception, as expected "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
