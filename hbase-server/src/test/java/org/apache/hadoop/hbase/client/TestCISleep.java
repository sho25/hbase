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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|ClientTests
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
name|TestCISleep
extends|extends
name|AbstractTestCITimeout
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
name|TestCISleep
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestCISleep
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
block|}
comment|/**    * Test starting from 0 index when RpcRetryingCaller calculate the backoff time.    */
annotation|@
name|Test
specifier|public
name|void
name|testRpcRetryingCallerSleep
parameter_list|()
throws|throws
name|Exception
block|{
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
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|FAM_NAM
argument_list|)
argument_list|)
operator|.
name|setCoprocessor
argument_list|(
name|CoprocessorDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|SleepAndFailFirstTime
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|setProperty
argument_list|(
name|SleepAndFailFirstTime
operator|.
name|SLEEP_TIME_CONF_KEY
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
literal|2000
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
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
name|c
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_PAUSE
argument_list|,
literal|3000
argument_list|)
expr_stmt|;
name|c
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_RPC_TIMEOUT_KEY
argument_list|,
literal|4000
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
name|conn
operator|.
name|getTableBuilder
argument_list|(
name|tableName
argument_list|,
literal|null
argument_list|)
operator|.
name|setOperationTimeout
argument_list|(
literal|8000
argument_list|)
operator|.
name|build
argument_list|()
init|)
block|{
comment|// Check that it works. Because 2s + 3s * RETRY_BACKOFF[0] + 2s< 8s
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|FAM_NAM
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|conn
operator|.
name|getTableBuilder
argument_list|(
name|tableName
argument_list|,
literal|null
argument_list|)
operator|.
name|setOperationTimeout
argument_list|(
literal|6000
argument_list|)
operator|.
name|build
argument_list|()
init|)
block|{
comment|// Will fail this time. After sleep, there are not enough time for second retry
comment|// Beacuse 2s + 3s + 2s> 6s
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|FAM_NAM
argument_list|)
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

