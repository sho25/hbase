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
name|AsyncConnectionConfiguration
operator|.
name|START_LOG_ERRORS_AFTER_COUNT_KEY
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
name|util
operator|.
name|concurrent
operator|.
name|Callable
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
name|NamespaceDescriptor
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
name|NamespaceExistException
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
name|NamespaceNotFoundException
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

begin_comment
comment|/**  * Class to test asynchronous namespace admin operations.  */
end_comment

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
annotation|@
name|Category
argument_list|(
block|{
name|LargeTests
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
name|TestAsyncNamespaceAdminApi
extends|extends
name|TestAsyncAdminBase
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
name|TestAsyncNamespaceAdminApi
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|String
name|prefix
init|=
literal|"TestNamespace"
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
literal|1
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Done initializing cluster"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateAndDelete
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|testName
init|=
literal|"testCreateAndDelete"
decl_stmt|;
name|String
name|nsName
init|=
name|prefix
operator|+
literal|"_"
operator|+
name|testName
decl_stmt|;
comment|// create namespace and verify
name|admin
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|nsName
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|admin
operator|.
name|listNamespaces
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|admin
operator|.
name|listNamespaceDescriptors
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// delete namespace and verify
name|admin
operator|.
name|deleteNamespace
argument_list|(
name|nsName
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|admin
operator|.
name|listNamespaces
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|admin
operator|.
name|listNamespaceDescriptors
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteReservedNS
parameter_list|()
throws|throws
name|Exception
block|{
name|boolean
name|exceptionCaught
init|=
literal|false
decl_stmt|;
try|try
block|{
name|admin
operator|.
name|deleteNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE_NAME_STR
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|exp
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|exp
operator|.
name|toString
argument_list|()
argument_list|,
name|exp
argument_list|)
expr_stmt|;
name|exceptionCaught
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
name|assertTrue
argument_list|(
name|exceptionCaught
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|admin
operator|.
name|deleteNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE_NAME_STR
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|exp
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|exp
operator|.
name|toString
argument_list|()
argument_list|,
name|exp
argument_list|)
expr_stmt|;
name|exceptionCaught
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
name|assertTrue
argument_list|(
name|exceptionCaught
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNamespaceOperations
parameter_list|()
throws|throws
name|Exception
block|{
name|admin
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|prefix
operator|+
literal|"ns1"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
name|admin
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|prefix
operator|+
literal|"ns2"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
comment|// create namespace that already exists
name|runWithExpectedException
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|admin
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|prefix
operator|+
literal|"ns1"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|,
name|NamespaceExistException
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// create a table in non-existing namespace
name|runWithExpectedException
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|TableDescriptorBuilder
name|tableDescriptorBuilder
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"non_existing_namespace"
argument_list|,
literal|"table1"
argument_list|)
argument_list|)
decl_stmt|;
name|ColumnFamilyDescriptor
name|columnFamilyDescriptor
init|=
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family1"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|tableDescriptorBuilder
operator|.
name|setColumnFamily
argument_list|(
name|columnFamilyDescriptor
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|tableDescriptorBuilder
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|,
name|NamespaceNotFoundException
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// get descriptor for existing namespace
name|NamespaceDescriptor
name|ns1
init|=
name|admin
operator|.
name|getNamespaceDescriptor
argument_list|(
name|prefix
operator|+
literal|"ns1"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|prefix
operator|+
literal|"ns1"
argument_list|,
name|ns1
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// get descriptor for non-existing namespace
name|runWithExpectedException
argument_list|(
operator|new
name|Callable
argument_list|<
name|NamespaceDescriptor
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|NamespaceDescriptor
name|call
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|admin
operator|.
name|getNamespaceDescriptor
argument_list|(
literal|"non_existing_namespace"
argument_list|)
operator|.
name|get
argument_list|()
return|;
block|}
block|}
argument_list|,
name|NamespaceNotFoundException
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// delete descriptor for existing namespace
name|admin
operator|.
name|deleteNamespace
argument_list|(
name|prefix
operator|+
literal|"ns2"
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
comment|// delete descriptor for non-existing namespace
name|runWithExpectedException
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|admin
operator|.
name|deleteNamespace
argument_list|(
literal|"non_existing_namespace"
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|,
name|NamespaceNotFoundException
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// modify namespace descriptor for existing namespace
name|ns1
operator|=
name|admin
operator|.
name|getNamespaceDescriptor
argument_list|(
name|prefix
operator|+
literal|"ns1"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ns1
operator|.
name|setConfiguration
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|admin
operator|.
name|modifyNamespace
argument_list|(
name|ns1
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
name|ns1
operator|=
name|admin
operator|.
name|getNamespaceDescriptor
argument_list|(
name|prefix
operator|+
literal|"ns1"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"bar"
argument_list|,
name|ns1
operator|.
name|getConfigurationValue
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
comment|// modify namespace descriptor for non-existing namespace
name|runWithExpectedException
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|admin
operator|.
name|modifyNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
literal|"non_existing_namespace"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|,
name|NamespaceNotFoundException
operator|.
name|class
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteNamespace
argument_list|(
name|prefix
operator|+
literal|"ns1"
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
parameter_list|<
name|V
parameter_list|,
name|E
parameter_list|>
name|void
name|runWithExpectedException
parameter_list|(
name|Callable
argument_list|<
name|V
argument_list|>
name|callable
parameter_list|,
name|Class
argument_list|<
name|E
argument_list|>
name|exceptionClass
parameter_list|)
block|{
try|try
block|{
name|callable
operator|.
name|call
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Get exception is "
operator|+
name|ex
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|exceptionClass
argument_list|,
name|ex
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
name|fail
argument_list|(
literal|"Should have thrown exception "
operator|+
name|exceptionClass
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

