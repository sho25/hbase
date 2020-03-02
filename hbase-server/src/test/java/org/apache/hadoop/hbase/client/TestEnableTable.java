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
name|CountDownLatch
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
name|MetaTableAccessor
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
name|coprocessor
operator|.
name|CoprocessorHost
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
name|coprocessor
operator|.
name|MasterCoprocessor
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
name|coprocessor
operator|.
name|MasterCoprocessorEnvironment
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
name|coprocessor
operator|.
name|MasterObserver
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
name|coprocessor
operator|.
name|ObserverContext
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
name|MasterTests
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
name|After
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
name|MasterTests
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
name|TestEnableTable
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
name|TestEnableTable
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestEnableTable
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILYNAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
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
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|MasterSyncObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
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
comment|/**    * We were only clearing rows that had a hregioninfo column in hbase:meta.  Mangled rows that    * were missing the hregioninfo because of error were being left behind messing up any    * subsequent table made with the same name. HBASE-12980    * @throws IOException    * @throws InterruptedException    */
annotation|@
name|Test
specifier|public
name|void
name|testDeleteForSureClearsAllTableRowsFromMeta
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
specifier|final
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
name|tableDescriptor
init|=
operator|new
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|ColumnFamilyDescriptor
name|familyDescriptor
init|=
operator|new
name|ColumnFamilyDescriptorBuilder
operator|.
name|ModifyableColumnFamilyDescriptor
argument_list|(
name|FAMILYNAME
argument_list|)
decl_stmt|;
name|tableDescriptor
operator|.
name|setColumnFamily
argument_list|(
name|familyDescriptor
argument_list|)
expr_stmt|;
try|try
block|{
name|createTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|tableDescriptor
argument_list|,
name|HBaseTestingUtility
operator|.
name|KEYS_FOR_HBA_CREATE_TABLE
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Got an exception while creating "
operator|+
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|// Now I have a nice table, mangle it by removing the HConstants.REGIONINFO_QUALIFIER_STR
comment|// content from a few of the rows.
try|try
init|(
name|Table
name|metaTable
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
init|)
block|{
try|try
init|(
name|ResultScanner
name|scanner
init|=
name|metaTable
operator|.
name|getScanner
argument_list|(
name|MetaTableAccessor
operator|.
name|getScanForTableName
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tableName
argument_list|)
argument_list|)
init|)
block|{
for|for
control|(
name|Result
name|result
range|:
name|scanner
control|)
block|{
comment|// Just delete one row.
name|Delete
name|d
init|=
operator|new
name|Delete
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|d
operator|.
name|addColumn
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Mangled: "
operator|+
name|d
argument_list|)
expr_stmt|;
name|metaTable
operator|.
name|delete
argument_list|(
name|d
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableDisabled
argument_list|(
name|tableName
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Rely on the coprocessor based latch to make the operation synchronous.
try|try
block|{
name|deleteTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Got an exception while deleting "
operator|+
name|tableName
argument_list|)
expr_stmt|;
block|}
name|int
name|rowCount
init|=
literal|0
decl_stmt|;
try|try
init|(
name|ResultScanner
name|scanner
init|=
name|metaTable
operator|.
name|getScanner
argument_list|(
name|MetaTableAccessor
operator|.
name|getScanForTableName
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tableName
argument_list|)
argument_list|)
init|)
block|{
for|for
control|(
name|Result
name|result
range|:
name|scanner
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Found when none expected: "
operator|+
name|result
argument_list|)
expr_stmt|;
name|rowCount
operator|++
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rowCount
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|MasterSyncObserver
implements|implements
name|MasterCoprocessor
implements|,
name|MasterObserver
block|{
specifier|volatile
name|CountDownLatch
name|tableCreationLatch
init|=
literal|null
decl_stmt|;
specifier|volatile
name|CountDownLatch
name|tableDeletionLatch
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|MasterObserver
argument_list|>
name|getMasterObserver
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
name|void
name|postCompletedCreateTableAction
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|TableDescriptor
name|desc
parameter_list|,
specifier|final
name|RegionInfo
index|[]
name|regions
parameter_list|)
throws|throws
name|IOException
block|{
comment|// the AccessController test, some times calls only and directly the
comment|// postCompletedCreateTableAction()
if|if
condition|(
name|tableCreationLatch
operator|!=
literal|null
condition|)
block|{
name|tableCreationLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|postCompletedDeleteTableAction
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
comment|// the AccessController test, some times calls only and directly the postDeleteTableHandler()
if|if
condition|(
name|tableDeletionLatch
operator|!=
literal|null
condition|)
block|{
name|tableDeletionLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|void
name|createTable
parameter_list|(
name|HBaseTestingUtility
name|testUtil
parameter_list|,
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
name|tableDescriptor
parameter_list|,
name|byte
index|[]
index|[]
name|splitKeys
parameter_list|)
throws|throws
name|Exception
block|{
comment|// NOTE: We need a latch because admin is not sync,
comment|// so the postOp coprocessor method may be called after the admin operation returned.
name|MasterSyncObserver
name|observer
init|=
name|testUtil
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|MasterSyncObserver
operator|.
name|class
argument_list|)
decl_stmt|;
name|observer
operator|.
name|tableCreationLatch
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Admin
name|admin
init|=
name|testUtil
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
if|if
condition|(
name|splitKeys
operator|!=
literal|null
condition|)
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|tableDescriptor
argument_list|,
name|splitKeys
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
block|}
name|observer
operator|.
name|tableCreationLatch
operator|.
name|await
argument_list|()
expr_stmt|;
name|observer
operator|.
name|tableCreationLatch
operator|=
literal|null
expr_stmt|;
name|testUtil
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|deleteTable
parameter_list|(
name|HBaseTestingUtility
name|testUtil
parameter_list|,
name|TableName
name|tableName
parameter_list|)
throws|throws
name|Exception
block|{
comment|// NOTE: We need a latch because admin is not sync,
comment|// so the postOp coprocessor method may be called after the admin operation returned.
name|MasterSyncObserver
name|observer
init|=
name|testUtil
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|MasterSyncObserver
operator|.
name|class
argument_list|)
decl_stmt|;
name|observer
operator|.
name|tableDeletionLatch
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Admin
name|admin
init|=
name|testUtil
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
try|try
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
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
name|tableName
argument_list|)
expr_stmt|;
name|observer
operator|.
name|tableDeletionLatch
operator|.
name|await
argument_list|()
expr_stmt|;
name|observer
operator|.
name|tableDeletionLatch
operator|=
literal|null
expr_stmt|;
block|}
block|}
end_class

end_unit

