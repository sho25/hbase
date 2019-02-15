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
name|Delete
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
name|MediumTests
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
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestBackupDeleteRestore
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
name|TestBackupDeleteRestore
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
name|TestBackupDeleteRestore
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Verify that load data- backup - delete some data - restore works as expected - deleted data get    * restored.    *    * @throws Exception if doing the backup or an operation on the tables fails    */
annotation|@
name|Test
specifier|public
name|void
name|testBackupDeleteRestore
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"test full restore on a single table empty table"
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
argument_list|)
decl_stmt|;
name|String
name|backupId
init|=
name|fullTableBackup
argument_list|(
name|tables
argument_list|)
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
name|int
name|numRows
init|=
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|table1
argument_list|)
decl_stmt|;
name|HBaseAdmin
name|hba
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
comment|// delete row
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
name|getTable
argument_list|(
name|table1
argument_list|)
init|)
block|{
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
literal|"row0"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|table
operator|.
name|delete
argument_list|(
name|delete
argument_list|)
expr_stmt|;
name|hba
operator|.
name|flush
argument_list|(
name|table1
argument_list|)
expr_stmt|;
block|}
name|TableName
index|[]
name|tableset
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
name|tablemap
init|=
literal|null
decl_stmt|;
comment|// new TableName[] { table1_restore };
name|BackupAdmin
name|client
init|=
name|getBackupAdmin
argument_list|()
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
name|backupId
argument_list|,
literal|false
argument_list|,
name|tableset
argument_list|,
name|tablemap
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|numRowsAfterRestore
init|=
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|table1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|numRows
argument_list|,
name|numRowsAfterRestore
argument_list|)
expr_stmt|;
name|hba
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

