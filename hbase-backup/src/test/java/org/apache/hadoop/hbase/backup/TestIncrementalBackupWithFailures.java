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
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|HColumnDescriptor
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
name|BackupInfo
operator|.
name|BackupState
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
name|impl
operator|.
name|BackupSystemTable
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
name|TableBackupClient
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
name|TableBackupClient
operator|.
name|Stage
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
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|ToolRunner
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

begin_class
annotation|@
name|Category
argument_list|(
name|LargeTests
operator|.
name|class
argument_list|)
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestIncrementalBackupWithFailures
extends|extends
name|TestBackupBase
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
name|TestIncrementalBackupWithFailures
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Parameterized
operator|.
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|data
parameter_list|()
block|{
name|provider
operator|=
literal|"multiwal"
expr_stmt|;
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|params
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|params
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
name|Boolean
operator|.
name|TRUE
block|}
argument_list|)
expr_stmt|;
return|return
name|params
return|;
block|}
specifier|public
name|TestIncrementalBackupWithFailures
parameter_list|(
name|Boolean
name|b
parameter_list|)
block|{   }
comment|// implement all test cases in 1 test since incremental backup/restore has dependencies
annotation|@
name|Test
specifier|public
name|void
name|testIncBackupRestore
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
specifier|final
name|byte
index|[]
name|fam3Name
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f3"
argument_list|)
decl_stmt|;
name|table1Desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|fam3Name
argument_list|)
argument_list|)
expr_stmt|;
name|HBaseTestingUtility
operator|.
name|modifyTableSync
argument_list|(
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
argument_list|,
name|table1Desc
argument_list|)
expr_stmt|;
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
name|int
name|NB_ROWS_FAM3
init|=
literal|6
decl_stmt|;
name|insertIntoTable
argument_list|(
name|conn
argument_list|,
name|table1
argument_list|,
name|fam3Name
argument_list|,
literal|3
argument_list|,
name|NB_ROWS_FAM3
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
literal|null
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
comment|// #2 - insert some data to table
name|HTable
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
operator|+
name|NB_ROWS_FAM3
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
name|HTable
name|t2
init|=
operator|(
name|HTable
operator|)
name|conn
operator|.
name|getTable
argument_list|(
name|table2
argument_list|)
decl_stmt|;
name|Put
name|p2
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
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|p2
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-t2"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|p2
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
name|t2
operator|.
name|put
argument_list|(
name|p2
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
name|t2
argument_list|)
argument_list|,
name|NB_ROWS_IN_BATCH
operator|+
literal|5
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
literal|5
operator|+
literal|" rows to "
operator|+
name|table2
argument_list|)
expr_stmt|;
comment|// #3 - incremental backup for multiple tables
name|incrementalBackupWithFailures
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
specifier|private
name|void
name|incrementalBackupWithFailures
parameter_list|()
throws|throws
name|Exception
block|{
name|conf1
operator|.
name|set
argument_list|(
name|TableBackupClient
operator|.
name|BACKUP_CLIENT_IMPL_CLASS
argument_list|,
name|IncrementalTableBackupClientForTest
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|maxStage
init|=
name|Stage
operator|.
name|values
argument_list|()
operator|.
name|length
operator|-
literal|1
decl_stmt|;
comment|// Fail stages between 0 and 4 inclusive
for|for
control|(
name|int
name|stage
init|=
literal|0
init|;
name|stage
operator|<=
name|maxStage
condition|;
name|stage
operator|++
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Running stage "
operator|+
name|stage
argument_list|)
expr_stmt|;
name|runBackupAndFailAtStage
argument_list|(
name|stage
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|runBackupAndFailAtStage
parameter_list|(
name|int
name|stage
parameter_list|)
throws|throws
name|Exception
block|{
name|conf1
operator|.
name|setInt
argument_list|(
name|FullTableBackupClientForTest
operator|.
name|BACKUP_TEST_MODE_STAGE
argument_list|,
name|stage
argument_list|)
expr_stmt|;
try|try
init|(
name|BackupSystemTable
name|table
init|=
operator|new
name|BackupSystemTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|)
init|)
block|{
name|int
name|before
init|=
name|table
operator|.
name|getBackupHistory
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"create"
block|,
literal|"incremental"
block|,
name|BACKUP_ROOT_DIR
block|,
literal|"-t"
block|,
name|table1
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|","
operator|+
name|table2
operator|.
name|getNameAsString
argument_list|()
block|}
decl_stmt|;
comment|// Run backup
name|int
name|ret
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|conf1
argument_list|,
operator|new
name|BackupDriver
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|ret
operator|==
literal|0
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|BackupInfo
argument_list|>
name|backups
init|=
name|table
operator|.
name|getBackupHistory
argument_list|()
decl_stmt|;
name|int
name|after
init|=
name|table
operator|.
name|getBackupHistory
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|after
operator|==
name|before
operator|+
literal|1
argument_list|)
expr_stmt|;
for|for
control|(
name|BackupInfo
name|data
range|:
name|backups
control|)
block|{
if|if
condition|(
name|data
operator|.
name|getType
argument_list|()
operator|==
name|BackupType
operator|.
name|FULL
condition|)
block|{
name|assertTrue
argument_list|(
name|data
operator|.
name|getState
argument_list|()
operator|==
name|BackupState
operator|.
name|COMPLETE
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertTrue
argument_list|(
name|data
operator|.
name|getState
argument_list|()
operator|==
name|BackupState
operator|.
name|FAILED
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

