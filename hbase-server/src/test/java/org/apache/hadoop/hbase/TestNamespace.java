begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|Admin
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
name|HTable
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
name|master
operator|.
name|HMaster
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
name|testclassification
operator|.
name|MiscTests
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
name|FSUtils
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
name|zookeeper
operator|.
name|ZKUtil
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
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
name|Assert
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
name|Ignore
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
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
name|TestNamespace
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
name|TestNamespace
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HMaster
name|master
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|int
name|NUM_SLAVES_BASE
init|=
literal|4
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
decl_stmt|;
specifier|protected
specifier|static
name|Admin
name|admin
decl_stmt|;
specifier|protected
specifier|static
name|HBaseCluster
name|cluster
decl_stmt|;
specifier|private
specifier|static
name|ZKNamespaceManager
name|zkNamespaceManager
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
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.namespacejanitor.interval"
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|NUM_SLAVES_BASE
argument_list|)
expr_stmt|;
name|admin
operator|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
expr_stmt|;
name|cluster
operator|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
expr_stmt|;
name|master
operator|=
operator|(
operator|(
name|MiniHBaseCluster
operator|)
name|cluster
operator|)
operator|.
name|getMaster
argument_list|()
expr_stmt|;
name|zkNamespaceManager
operator|=
operator|new
name|ZKNamespaceManager
argument_list|(
name|master
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
expr_stmt|;
name|zkNamespaceManager
operator|.
name|start
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
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
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
annotation|@
name|Before
specifier|public
name|void
name|beforeMethod
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|HTableDescriptor
name|desc
range|:
name|admin
operator|.
name|listTables
argument_list|(
name|prefix
operator|+
literal|".*"
argument_list|)
control|)
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|NamespaceDescriptor
name|ns
range|:
name|admin
operator|.
name|listNamespaceDescriptors
argument_list|()
control|)
block|{
if|if
condition|(
name|ns
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
name|prefix
argument_list|)
condition|)
block|{
name|admin
operator|.
name|deleteNamespace
argument_list|(
name|ns
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|verifyReservedNS
parameter_list|()
throws|throws
name|IOException
block|{
comment|//verify existence of reserved namespaces
name|NamespaceDescriptor
name|ns
init|=
name|admin
operator|.
name|getNamespaceDescriptor
argument_list|(
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|ns
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ns
operator|.
name|getName
argument_list|()
argument_list|,
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|zkNamespaceManager
operator|.
name|get
argument_list|(
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE_NAME_STR
argument_list|)
argument_list|)
expr_stmt|;
name|ns
operator|=
name|admin
operator|.
name|getNamespaceDescriptor
argument_list|(
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|ns
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ns
operator|.
name|getName
argument_list|()
argument_list|,
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|zkNamespaceManager
operator|.
name|get
argument_list|(
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE_NAME_STR
argument_list|)
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
name|length
argument_list|)
expr_stmt|;
comment|//verify existence of system tables
name|Set
argument_list|<
name|TableName
argument_list|>
name|systemTables
init|=
name|Sets
operator|.
name|newHashSet
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|,
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
argument_list|)
decl_stmt|;
name|HTableDescriptor
index|[]
name|descs
init|=
name|admin
operator|.
name|listTableDescriptorsByNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|systemTables
operator|.
name|size
argument_list|()
argument_list|,
name|descs
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|HTableDescriptor
name|desc
range|:
name|descs
control|)
block|{
name|assertTrue
argument_list|(
name|systemTables
operator|.
name|contains
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//verify system tables aren't listed
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|admin
operator|.
name|listTables
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
comment|//Try creating default and system namespaces.
name|boolean
name|exceptionCaught
init|=
literal|false
decl_stmt|;
try|try
block|{
name|admin
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|exp
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
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
name|exceptionCaught
operator|=
literal|false
expr_stmt|;
try|try
block|{
name|admin
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|exp
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
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
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|exp
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
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
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|exp
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
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
name|createRemoveTest
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|testName
init|=
literal|"createRemoveTest"
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
name|LOG
operator|.
name|info
argument_list|(
name|testName
argument_list|)
expr_stmt|;
comment|//create namespace and verify
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
name|length
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|60000
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|zkNamespaceManager
operator|.
name|list
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|3
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|zkNamespaceManager
operator|.
name|get
argument_list|(
name|nsName
argument_list|)
argument_list|)
expr_stmt|;
comment|//remove namespace and verify
name|admin
operator|.
name|deleteNamespace
argument_list|(
name|nsName
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
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|zkNamespaceManager
operator|.
name|list
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|zkNamespaceManager
operator|.
name|get
argument_list|(
name|nsName
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|createDoubleTest
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|String
name|testName
init|=
literal|"createDoubleTest"
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
name|LOG
operator|.
name|info
argument_list|(
name|testName
argument_list|)
expr_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"my_table"
argument_list|)
decl_stmt|;
name|TableName
name|tableNameFoo
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|nsName
operator|+
literal|":my_table"
argument_list|)
decl_stmt|;
comment|//create namespace and verify
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
expr_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|nsName
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableNameFoo
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|nsName
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|admin
operator|.
name|listTables
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|tableNameFoo
argument_list|)
argument_list|)
expr_stmt|;
comment|//remove namespace and verify
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|admin
operator|.
name|listTables
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|createTableTest
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|String
name|testName
init|=
literal|"createTableTest"
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
name|LOG
operator|.
name|info
argument_list|(
name|testName
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|nsName
operator|+
literal|":my_table"
argument_list|)
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|colDesc
init|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"my_cf"
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|colDesc
argument_list|)
expr_stmt|;
try|try
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected no namespace exists exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NamespaceNotFoundException
name|ex
parameter_list|)
block|{     }
comment|//create table and in new namespace
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
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
operator|new
name|Path
argument_list|(
name|master
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getRootDir
argument_list|()
argument_list|,
operator|new
name|Path
argument_list|(
name|HConstants
operator|.
name|BASE_NAMESPACE_DIR
argument_list|,
operator|new
name|Path
argument_list|(
name|nsName
argument_list|,
name|desc
operator|.
name|getTableName
argument_list|()
operator|.
name|getQualifierAsString
argument_list|()
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|admin
operator|.
name|listTables
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
comment|//verify non-empty namespace can't be removed
try|try
block|{
name|admin
operator|.
name|deleteNamespace
argument_list|(
name|nsName
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected non-empty namespace constraint exception"
argument_list|)
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
literal|"Caught expected exception: "
operator|+
name|ex
argument_list|)
expr_stmt|;
block|}
comment|//sanity check try to write and read from table
name|Table
name|table
init|=
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
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
literal|"row1"
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"my_cf"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"my_col"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|//flush and read from disk to make sure directory changes are working
name|admin
operator|.
name|flush
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
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
literal|"row1"
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|table
operator|.
name|exists
argument_list|(
name|g
argument_list|)
argument_list|)
expr_stmt|;
comment|//normal case of removing namespace
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteNamespace
argument_list|(
name|nsName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|createTableInDefaultNamespace
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"default_table"
argument_list|)
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|colDesc
init|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"cf1"
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|colDesc
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|admin
operator|.
name|listTables
argument_list|()
operator|.
name|length
operator|==
literal|1
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|createTableInSystemNamespace
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"hbase:createTableInSystemNamespace"
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|colDesc
init|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"cf1"
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|colDesc
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|admin
operator|.
name|listTables
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|admin
operator|.
name|tableExists
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
annotation|@
name|Test
specifier|public
name|void
name|testNamespaceJanitor
parameter_list|()
throws|throws
name|Exception
block|{
name|FileSystem
name|fs
init|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|int
name|fsCount
init|=
name|fs
operator|.
name|listStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|HConstants
operator|.
name|BASE_NAMESPACE_DIR
argument_list|)
argument_list|)
operator|.
name|length
decl_stmt|;
name|Path
name|fakeNSPath
init|=
name|FSUtils
operator|.
name|getNamespaceDir
argument_list|(
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
literal|"foo"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|mkdirs
argument_list|(
name|fakeNSPath
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|fakeZnode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|ZooKeeperWatcher
operator|.
name|namespaceZNode
argument_list|,
literal|"foo"
argument_list|)
decl_stmt|;
name|int
name|zkCount
init|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|TEST_UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|ZooKeeperWatcher
operator|.
name|namespaceZNode
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|TEST_UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|fakeZnode
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
comment|//verify namespace count is the same and orphan is removed
name|assertFalse
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|fakeNSPath
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|fsCount
argument_list|,
name|fs
operator|.
name|listStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|HConstants
operator|.
name|BASE_NAMESPACE_DIR
argument_list|)
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|TEST_UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|fakeZnode
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|zkCount
argument_list|,
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|TEST_UTIL
operator|.
name|getZooKeeperWatcher
argument_list|()
argument_list|,
name|ZooKeeperWatcher
operator|.
name|namespaceZNode
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testNamespaceOperations
parameter_list|()
throws|throws
name|IOException
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
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
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
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
literal|"family1"
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
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
name|admin
operator|.
name|getNamespaceDescriptor
argument_list|(
name|prefix
operator|+
literal|"ns1"
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
decl_stmt|;
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
comment|// get table descriptors for existing namespace
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|prefix
operator|+
literal|"ns1"
argument_list|,
literal|"table1"
argument_list|)
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
literal|"family1"
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|HTableDescriptor
index|[]
name|htds
init|=
name|admin
operator|.
name|listTableDescriptorsByNamespace
argument_list|(
name|prefix
operator|+
literal|"ns1"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"Should have not returned null"
argument_list|,
name|htds
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Should have returned non-empty array"
argument_list|,
literal|1
argument_list|,
name|htds
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// get table descriptors for non-existing namespace
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
name|listTableDescriptorsByNamespace
argument_list|(
literal|"non_existing_namespace"
argument_list|)
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
comment|// get table names for existing namespace
name|TableName
index|[]
name|tableNames
init|=
name|admin
operator|.
name|listTableNamesByNamespace
argument_list|(
name|prefix
operator|+
literal|"ns1"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"Should have not returned null"
argument_list|,
name|tableNames
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Should have returned non-empty array"
argument_list|,
literal|1
argument_list|,
name|tableNames
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// get table names for non-existing namespace
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
name|listTableNamesByNamespace
argument_list|(
literal|"non_existing_namespace"
argument_list|)
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
name|Assert
operator|.
name|assertEquals
argument_list|(
name|exceptionClass
argument_list|,
name|ex
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

