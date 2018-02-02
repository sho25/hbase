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
comment|/**  * Based class for testing rpc timeout logic for {@link ConnectionImplementation}.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractTestCIRpcTimeout
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
name|AbstractTestCIRpcTimeout
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
name|SleepCoprocessor
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
annotation|@
name|Test
specifier|public
name|void
name|testRpcTimeout
parameter_list|()
throws|throws
name|IOException
block|{
name|Configuration
name|c
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
try|try
init|(
name|Table
name|table
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
name|SleepCoprocessor
operator|.
name|SLEEP_TIME
operator|/
literal|2
argument_list|)
operator|.
name|setReadRpcTimeout
argument_list|(
name|SleepCoprocessor
operator|.
name|SLEEP_TIME
operator|/
literal|2
argument_list|)
operator|.
name|setWriteRpcTimeout
argument_list|(
name|SleepCoprocessor
operator|.
name|SLEEP_TIME
operator|/
literal|2
argument_list|)
operator|.
name|setOperationTimeout
argument_list|(
name|SleepCoprocessor
operator|.
name|SLEEP_TIME
operator|*
literal|100
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
name|fail
argument_list|(
literal|"Get should not have succeeded"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RetriesExhaustedException
name|e
parameter_list|)
block|{
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
comment|// Again, with configuration based override
name|c
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_RPC_TIMEOUT_KEY
argument_list|,
name|SleepCoprocessor
operator|.
name|SLEEP_TIME
operator|/
literal|2
argument_list|)
expr_stmt|;
name|c
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_RPC_READ_TIMEOUT_KEY
argument_list|,
name|SleepCoprocessor
operator|.
name|SLEEP_TIME
operator|/
literal|2
argument_list|)
expr_stmt|;
name|c
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_RPC_WRITE_TIMEOUT_KEY
argument_list|,
name|SleepCoprocessor
operator|.
name|SLEEP_TIME
operator|/
literal|2
argument_list|)
expr_stmt|;
name|c
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|,
name|SleepCoprocessor
operator|.
name|SLEEP_TIME
operator|*
literal|100
argument_list|)
expr_stmt|;
try|try
init|(
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|c
argument_list|)
init|)
block|{
try|try
init|(
name|Table
name|table
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|execute
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Get should not have succeeded"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RetriesExhaustedException
name|e
parameter_list|)
block|{
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
block|}
end_class

end_unit
