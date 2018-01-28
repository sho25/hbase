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
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestFullBackupSet
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
name|TestFullBackupSet
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
name|TestFullBackupSet
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Verify that full backup is created on a single table with data correctly.    *    * @throws Exception if doing the backup or an operation on the tables fails    */
annotation|@
name|Test
specifier|public
name|void
name|testFullBackupSetExist
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Test full backup, backup set exists"
argument_list|)
expr_stmt|;
comment|// Create set
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
name|String
name|name
init|=
literal|"name"
decl_stmt|;
name|table
operator|.
name|addToBackupSet
argument_list|(
name|name
argument_list|,
operator|new
name|String
index|[]
block|{
name|table1
operator|.
name|getNameAsString
argument_list|()
block|}
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TableName
argument_list|>
name|names
init|=
name|table
operator|.
name|describeBackupSet
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|names
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|names
operator|.
name|size
argument_list|()
operator|==
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|names
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|equals
argument_list|(
name|table1
argument_list|)
argument_list|)
expr_stmt|;
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
literal|"full"
block|,
name|BACKUP_ROOT_DIR
block|,
literal|"-s"
block|,
name|name
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
name|assertTrue
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
name|assertTrue
argument_list|(
name|backups
operator|.
name|size
argument_list|()
operator|==
literal|1
argument_list|)
expr_stmt|;
name|String
name|backupId
init|=
name|backups
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getBackupId
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|checkSucceeded
argument_list|(
name|backupId
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"backup complete"
argument_list|)
expr_stmt|;
comment|// Restore from set into other table
name|args
operator|=
operator|new
name|String
index|[]
block|{
name|BACKUP_ROOT_DIR
block|,
name|backupId
block|,
literal|"-s"
block|,
name|name
block|,
literal|"-m"
block|,
name|table1_restore
operator|.
name|getNameAsString
argument_list|()
block|,
literal|"-o"
block|}
expr_stmt|;
comment|// Run backup
name|ret
operator|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|conf1
argument_list|,
operator|new
name|RestoreDriver
argument_list|()
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ret
operator|==
literal|0
argument_list|)
expr_stmt|;
name|HBaseAdmin
name|hba
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|hba
operator|.
name|tableExists
argument_list|(
name|table1_restore
argument_list|)
argument_list|)
expr_stmt|;
comment|// Verify number of rows in both tables
name|assertEquals
argument_list|(
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|table1
argument_list|)
argument_list|,
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|table1_restore
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|table1_restore
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"restore into other table is complete"
argument_list|)
expr_stmt|;
name|hba
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFullBackupSetDoesNotExist
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"test full backup, backup set does not exist"
argument_list|)
expr_stmt|;
name|String
name|name
init|=
literal|"name1"
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
literal|"full"
block|,
name|BACKUP_ROOT_DIR
block|,
literal|"-s"
block|,
name|name
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
name|assertTrue
argument_list|(
name|ret
operator|!=
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

