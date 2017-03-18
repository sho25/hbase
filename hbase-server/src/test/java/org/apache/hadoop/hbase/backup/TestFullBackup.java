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
name|TestFullBackup
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
name|TestFullBackup
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testFullBackupMultipleCommand
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"test full backup on a multiple tables with data: command-line"
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
literal|"full"
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
name|String
name|backupId
init|=
name|data
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
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"backup complete"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

