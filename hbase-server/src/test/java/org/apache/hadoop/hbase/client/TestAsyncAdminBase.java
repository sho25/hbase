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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|AsyncProcess
operator|.
name|START_LOG_ERRORS_AFTER_COUNT_KEY
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
name|ForkJoinPool
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
name|TimeUnit
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Supplier
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|io
operator|.
name|IOUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|After
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
name|rules
operator|.
name|TestName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
import|;
end_import

begin_comment
comment|/**  * Class to test AsyncAdmin.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|TestAsyncAdminBase
block|{
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestAsyncAdminBase
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
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
literal|"testFamily"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY_0
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf0"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY_1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf1"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|AsyncConnection
name|ASYNC_CONN
decl_stmt|;
specifier|protected
name|AsyncAdmin
name|admin
decl_stmt|;
annotation|@
name|Parameter
specifier|public
name|Supplier
argument_list|<
name|AsyncAdmin
argument_list|>
name|getAdmin
decl_stmt|;
specifier|private
specifier|static
name|AsyncAdmin
name|getRawAsyncAdmin
parameter_list|()
block|{
return|return
name|ASYNC_CONN
operator|.
name|getAdmin
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|AsyncAdmin
name|getAsyncAdmin
parameter_list|()
block|{
return|return
name|ASYNC_CONN
operator|.
name|getAdmin
argument_list|(
name|ForkJoinPool
operator|.
name|commonPool
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Parameters
specifier|public
specifier|static
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|params
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Supplier
argument_list|<
name|?
argument_list|>
index|[]
block|{
name|TestAsyncAdminBase
operator|::
name|getRawAsyncAdmin
block|}
operator|,
operator|new
name|Supplier
argument_list|<
name|?
argument_list|>
index|[]
block|{
name|TestAsyncAdminBase
operator|::
name|getAsyncAdmin
block|}
block|)
function|;
block|}
end_class

begin_decl_stmt
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
end_decl_stmt

begin_decl_stmt
specifier|protected
name|TableName
name|tableName
decl_stmt|;
end_decl_stmt

begin_function
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
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_RPC_TIMEOUT_KEY
argument_list|,
literal|60000
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|,
literal|120000
argument_list|)
expr_stmt|;
name|TEST_UTIL
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
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|START_LOG_ERRORS_AFTER_COUNT_KEY
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|ASYNC_CONN
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
block|}
end_function

begin_function
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
name|IOUtils
operator|.
name|closeQuietly
argument_list|(
name|ASYNC_CONN
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|admin
operator|=
name|ASYNC_CONN
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|String
name|methodName
init|=
name|testName
operator|.
name|getMethodName
argument_list|()
decl_stmt|;
name|tableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|methodName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|methodName
operator|.
name|length
argument_list|()
operator|-
literal|3
argument_list|)
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
name|admin
operator|.
name|listTableNames
argument_list|(
name|Optional
operator|.
name|of
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
name|tableName
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|".*"
argument_list|)
argument_list|)
argument_list|,
literal|false
argument_list|)
operator|.
name|whenCompleteAsync
argument_list|(
parameter_list|(
name|tables
parameter_list|,
name|err
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|tables
operator|!=
literal|null
condition|)
block|{
name|tables
operator|.
name|forEach
argument_list|(
name|table
lambda|->
block|{
try|try
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|table
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table: "
operator|+
name|tableName
operator|+
literal|" already disabled, so just deleting it."
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|deleteTable
argument_list|(
name|table
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|,
name|ForkJoinPool
operator|.
name|commonPool
argument_list|()
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
end_function

begin_function
specifier|protected
name|void
name|createTableWithDefaultConf
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
name|createTableWithDefaultConf
argument_list|(
name|tableName
argument_list|,
name|Optional
operator|.
name|empty
argument_list|()
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
specifier|protected
name|void
name|createTableWithDefaultConf
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|Optional
argument_list|<
name|byte
index|[]
index|[]
argument_list|>
name|splitKeys
parameter_list|)
block|{
name|createTableWithDefaultConf
argument_list|(
name|tableName
argument_list|,
name|splitKeys
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
specifier|protected
name|void
name|createTableWithDefaultConf
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|Optional
argument_list|<
name|byte
index|[]
index|[]
argument_list|>
name|splitKeys
parameter_list|,
name|byte
index|[]
modifier|...
name|families
parameter_list|)
block|{
name|TableDescriptorBuilder
name|builder
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
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
name|builder
operator|.
name|addColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|family
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|createTable
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|splitKeys
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
end_function

unit|}
end_unit

