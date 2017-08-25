begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements. See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License. You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HBaseAdmin
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
name|Assert
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
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

begin_comment
comment|/**  * 1. Create table t1, t2  * 2. Load data to t1, t2  * 3 Full backup t1, t2  * 4 Delete t2  * 5 Load data to t1  * 6 Incremental backup t1  */
end_comment

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
name|TestIncrementalBackupDeleteTable
extends|extends
name|TestBackupBase
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestIncrementalBackupDeleteTable
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// implement all test cases in 1 test since incremental backup/restore has dependencies
annotation|@
name|Test
specifier|public
name|void
name|testIncBackupDeleteTable
parameter_list|()
throws|throws
name|Exception
block|{
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
name|HBaseAdmin
name|admin
init|=
literal|null
decl_stmt|;
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
name|admin
operator|=
operator|(
name|HBaseAdmin
operator|)
name|conn
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
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
comment|// #2 - insert some data to table table1
name|HTable
name|t1
init|=
operator|(
name|HTable
operator|)
name|conn
operator|.
name|getTable
argument_list|(
name|table1
argument_list|)
decl_stmt|;
name|Put
name|p1
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NB_ROWS_IN_BATCH
condition|;
name|i
operator|++
control|)
block|{
name|p1
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-t1"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|p1
operator|.
name|addColumn
argument_list|(
name|famName
argument_list|,
name|qualName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|t1
operator|.
name|put
argument_list|(
name|p1
argument_list|)
expr_stmt|;
block|}
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
operator|*
literal|2
argument_list|)
expr_stmt|;
name|t1
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Delete table table2
name|admin
operator|.
name|disableTable
argument_list|(
name|table2
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|table2
argument_list|)
expr_stmt|;
comment|// #3 - incremental backup for table1
name|tables
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|table1
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
comment|// #4 - restore full backup for all tables, without overwrite
name|TableName
index|[]
name|tablesRestoreFull
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
name|tablesMapFull
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
name|backupIdFull
argument_list|,
literal|false
argument_list|,
name|tablesRestoreFull
argument_list|,
name|tablesMapFull
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// #5.1 - check tables for full restore
name|HBaseAdmin
name|hAdmin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|hAdmin
operator|.
name|tableExists
argument_list|(
name|table1_restore
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|hAdmin
operator|.
name|tableExists
argument_list|(
name|table2_restore
argument_list|)
argument_list|)
expr_stmt|;
comment|// #5.2 - checking row count of tables for full restore
name|HTable
name|hTable
init|=
operator|(
name|HTable
operator|)
name|conn
operator|.
name|getTable
argument_list|(
name|table1_restore
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
name|hTable
argument_list|)
argument_list|,
name|NB_ROWS_IN_BATCH
argument_list|)
expr_stmt|;
name|hTable
operator|.
name|close
argument_list|()
expr_stmt|;
name|hTable
operator|=
operator|(
name|HTable
operator|)
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
argument_list|)
expr_stmt|;
name|hTable
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// #6 - restore incremental backup for table1
name|TableName
index|[]
name|tablesRestoreIncMultiple
init|=
operator|new
name|TableName
index|[]
block|{
name|table1
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
name|backupIdIncMultiple
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
name|hTable
operator|=
operator|(
name|HTable
operator|)
name|conn
operator|.
name|getTable
argument_list|(
name|table1_restore
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
operator|*
literal|2
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
