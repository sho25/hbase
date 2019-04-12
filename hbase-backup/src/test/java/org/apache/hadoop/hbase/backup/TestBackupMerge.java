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
name|backup
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
name|assertTrue
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
name|backup
operator|.
name|impl
operator|.
name|BackupAdminImpl
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
name|backup
operator|.
name|util
operator|.
name|BackupUtils
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
name|Connection
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
name|ConnectionFactory
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
name|LargeTests
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestBackupMerge
extends|extends
name|TestBackupBase
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
name|TestBackupMerge
operator|.
name|class
argument_list|)
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
name|TestBackupMerge
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|TestIncBackupMergeRestore
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|ADD_ROWS
init|=
literal|99
decl_stmt|;
comment|// #1 - create full backup for all tables
name|LOG
operator|.
name|info
argument_list|(
literal|"create full backup image for all tables"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TableName
argument_list|>
name|tables
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|table1
argument_list|,
name|table2
argument_list|)
decl_stmt|;
comment|// Set custom Merge Job implementation
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf1
argument_list|)
decl_stmt|;
name|Admin
name|admin
init|=
name|conn
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|BackupAdminImpl
name|client
init|=
operator|new
name|BackupAdminImpl
argument_list|(
name|conn
argument_list|)
decl_stmt|;
name|BackupRequest
name|request
init|=
name|createBackupRequest
argument_list|(
name|BackupType
operator|.
name|FULL
argument_list|,
name|tables
argument_list|,
name|BACKUP_ROOT_DIR
argument_list|)
decl_stmt|;
name|String
name|backupIdFull
init|=
name|client
operator|.
name|backupTables
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|checkSucceeded
argument_list|(
name|backupIdFull
argument_list|)
argument_list|)
expr_stmt|;
comment|// #2 - insert some data to table1
name|Table
name|t1
init|=
name|insertIntoTable
argument_list|(
name|conn
argument_list|,
name|table1
argument_list|,
name|famName
argument_list|,
literal|1
argument_list|,
name|ADD_ROWS
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"writing "
operator|+
name|ADD_ROWS
operator|+
literal|" rows to "
operator|+
name|table1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|t1
argument_list|)
argument_list|,
name|NB_ROWS_IN_BATCH
operator|+
name|ADD_ROWS
argument_list|)
expr_stmt|;
name|t1
operator|.
name|close
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"written "
operator|+
name|ADD_ROWS
operator|+
literal|" rows to "
operator|+
name|table1
argument_list|)
expr_stmt|;
name|Table
name|t2
init|=
name|insertIntoTable
argument_list|(
name|conn
argument_list|,
name|table2
argument_list|,
name|famName
argument_list|,
literal|1
argument_list|,
name|ADD_ROWS
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|t2
argument_list|)
argument_list|,
name|NB_ROWS_IN_BATCH
operator|+
name|ADD_ROWS
argument_list|)
expr_stmt|;
name|t2
operator|.
name|close
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"written "
operator|+
name|ADD_ROWS
operator|+
literal|" rows to "
operator|+
name|table2
argument_list|)
expr_stmt|;
comment|// #3 - incremental backup for multiple tables
name|tables
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|table1
argument_list|,
name|table2
argument_list|)
expr_stmt|;
name|request
operator|=
name|createBackupRequest
argument_list|(
name|BackupType
operator|.
name|INCREMENTAL
argument_list|,
name|tables
argument_list|,
name|BACKUP_ROOT_DIR
argument_list|)
expr_stmt|;
name|String
name|backupIdIncMultiple
init|=
name|client
operator|.
name|backupTables
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|checkSucceeded
argument_list|(
name|backupIdIncMultiple
argument_list|)
argument_list|)
expr_stmt|;
name|t1
operator|=
name|insertIntoTable
argument_list|(
name|conn
argument_list|,
name|table1
argument_list|,
name|famName
argument_list|,
literal|2
argument_list|,
name|ADD_ROWS
argument_list|)
expr_stmt|;
name|t1
operator|.
name|close
argument_list|()
expr_stmt|;
name|t2
operator|=
name|insertIntoTable
argument_list|(
name|conn
argument_list|,
name|table2
argument_list|,
name|famName
argument_list|,
literal|2
argument_list|,
name|ADD_ROWS
argument_list|)
expr_stmt|;
name|t2
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// #3 - incremental backup for multiple tables
name|request
operator|=
name|createBackupRequest
argument_list|(
name|BackupType
operator|.
name|INCREMENTAL
argument_list|,
name|tables
argument_list|,
name|BACKUP_ROOT_DIR
argument_list|)
expr_stmt|;
name|String
name|backupIdIncMultiple2
init|=
name|client
operator|.
name|backupTables
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|checkSucceeded
argument_list|(
name|backupIdIncMultiple2
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
name|BackupAdmin
name|bAdmin
init|=
operator|new
name|BackupAdminImpl
argument_list|(
name|conn
argument_list|)
init|)
block|{
name|String
index|[]
name|backups
init|=
operator|new
name|String
index|[]
block|{
name|backupIdIncMultiple
block|,
name|backupIdIncMultiple2
block|}
decl_stmt|;
name|bAdmin
operator|.
name|mergeBackups
argument_list|(
name|backups
argument_list|)
expr_stmt|;
block|}
comment|// #6 - restore incremental backup for multiple tables, with overwrite
name|TableName
index|[]
name|tablesRestoreIncMultiple
init|=
operator|new
name|TableName
index|[]
block|{
name|table1
block|,
name|table2
block|}
decl_stmt|;
name|TableName
index|[]
name|tablesMapIncMultiple
init|=
operator|new
name|TableName
index|[]
block|{
name|table1_restore
block|,
name|table2_restore
block|}
decl_stmt|;
name|client
operator|.
name|restore
argument_list|(
name|BackupUtils
operator|.
name|createRestoreRequest
argument_list|(
name|BACKUP_ROOT_DIR
argument_list|,
name|backupIdIncMultiple2
argument_list|,
literal|false
argument_list|,
name|tablesRestoreIncMultiple
argument_list|,
name|tablesMapIncMultiple
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Table
name|hTable
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|table1_restore
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"After incremental restore: "
operator|+
name|hTable
operator|.
name|getDescriptor
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|countRows
init|=
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|hTable
argument_list|,
name|famName
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"f1 has "
operator|+
name|countRows
operator|+
literal|" rows"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|NB_ROWS_IN_BATCH
operator|+
literal|2
operator|*
name|ADD_ROWS
argument_list|,
name|countRows
argument_list|)
expr_stmt|;
name|hTable
operator|.
name|close
argument_list|()
expr_stmt|;
name|hTable
operator|=
name|conn
operator|.
name|getTable
argument_list|(
name|table2_restore
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|hTable
argument_list|)
argument_list|,
name|NB_ROWS_IN_BATCH
operator|+
literal|2
operator|*
name|ADD_ROWS
argument_list|)
expr_stmt|;
name|hTable
operator|.
name|close
argument_list|()
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

