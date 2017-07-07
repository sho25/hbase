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
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintStream
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
specifier|public
class|class
name|TestBackupShowHistory
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
name|TestBackupShowHistory
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|findBackup
parameter_list|(
name|List
argument_list|<
name|BackupInfo
argument_list|>
name|history
parameter_list|,
name|String
name|backupId
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|history
operator|.
name|size
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|boolean
name|success
init|=
literal|false
decl_stmt|;
for|for
control|(
name|BackupInfo
name|info
range|:
name|history
control|)
block|{
if|if
condition|(
name|info
operator|.
name|getBackupId
argument_list|()
operator|.
name|equals
argument_list|(
name|backupId
argument_list|)
condition|)
block|{
name|success
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
return|return
name|success
return|;
block|}
comment|/**    * Verify that full backup is created on a single table with data correctly. Verify that history    * works as expected    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testBackupHistory
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"test backup history on a single table with data"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TableName
argument_list|>
name|tableList
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
name|tableList
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
name|List
argument_list|<
name|BackupInfo
argument_list|>
name|history
init|=
name|getBackupAdmin
argument_list|()
operator|.
name|getHistory
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|findBackup
argument_list|(
name|history
argument_list|,
name|backupId
argument_list|)
argument_list|)
expr_stmt|;
name|BackupInfo
operator|.
name|Filter
name|nullFilter
init|=
operator|new
name|BackupInfo
operator|.
name|Filter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|BackupInfo
name|info
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
block|}
decl_stmt|;
name|history
operator|=
name|BackupUtils
operator|.
name|getHistory
argument_list|(
name|conf1
argument_list|,
literal|10
argument_list|,
operator|new
name|Path
argument_list|(
name|BACKUP_ROOT_DIR
argument_list|)
argument_list|,
name|nullFilter
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|findBackup
argument_list|(
name|history
argument_list|,
name|backupId
argument_list|)
argument_list|)
expr_stmt|;
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|System
operator|.
name|setOut
argument_list|(
operator|new
name|PrintStream
argument_list|(
name|baos
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
literal|"history"
block|,
literal|"-n"
block|,
literal|"10"
block|,
literal|"-p"
block|,
name|BACKUP_ROOT_DIR
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
name|LOG
operator|.
name|info
argument_list|(
literal|"show_history"
argument_list|)
expr_stmt|;
name|String
name|output
init|=
name|baos
operator|.
name|toString
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|baos
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|output
operator|.
name|indexOf
argument_list|(
name|backupId
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|tableList
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|table2
argument_list|)
expr_stmt|;
name|String
name|backupId2
init|=
name|fullTableBackup
argument_list|(
name|tableList
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|checkSucceeded
argument_list|(
name|backupId2
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"backup complete: "
operator|+
name|table2
argument_list|)
expr_stmt|;
name|BackupInfo
operator|.
name|Filter
name|tableNameFilter
init|=
operator|new
name|BackupInfo
operator|.
name|Filter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|BackupInfo
name|image
parameter_list|)
block|{
if|if
condition|(
name|table1
operator|==
literal|null
condition|)
return|return
literal|true
return|;
name|List
argument_list|<
name|TableName
argument_list|>
name|names
init|=
name|image
operator|.
name|getTableNames
argument_list|()
decl_stmt|;
return|return
name|names
operator|.
name|contains
argument_list|(
name|table1
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|BackupInfo
operator|.
name|Filter
name|tableSetFilter
init|=
operator|new
name|BackupInfo
operator|.
name|Filter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|BackupInfo
name|info
parameter_list|)
block|{
name|String
name|backupId
init|=
name|info
operator|.
name|getBackupId
argument_list|()
decl_stmt|;
return|return
name|backupId
operator|.
name|startsWith
argument_list|(
literal|"backup"
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|history
operator|=
name|getBackupAdmin
argument_list|()
operator|.
name|getHistory
argument_list|(
literal|10
argument_list|,
name|tableNameFilter
argument_list|,
name|tableSetFilter
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|history
operator|.
name|size
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|boolean
name|success
init|=
literal|true
decl_stmt|;
for|for
control|(
name|BackupInfo
name|info
range|:
name|history
control|)
block|{
if|if
condition|(
operator|!
name|info
operator|.
name|getTableNames
argument_list|()
operator|.
name|contains
argument_list|(
name|table1
argument_list|)
condition|)
block|{
name|success
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
name|assertTrue
argument_list|(
name|success
argument_list|)
expr_stmt|;
name|history
operator|=
name|BackupUtils
operator|.
name|getHistory
argument_list|(
name|conf1
argument_list|,
literal|10
argument_list|,
operator|new
name|Path
argument_list|(
name|BACKUP_ROOT_DIR
argument_list|)
argument_list|,
name|tableNameFilter
argument_list|,
name|tableSetFilter
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|history
operator|.
name|size
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|success
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|BackupInfo
name|info
range|:
name|history
control|)
block|{
if|if
condition|(
operator|!
name|info
operator|.
name|getTableNames
argument_list|()
operator|.
name|contains
argument_list|(
name|table1
argument_list|)
condition|)
block|{
name|success
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
name|assertTrue
argument_list|(
name|success
argument_list|)
expr_stmt|;
name|args
operator|=
operator|new
name|String
index|[]
block|{
literal|"history"
block|,
literal|"-n"
block|,
literal|"10"
block|,
literal|"-p"
block|,
name|BACKUP_ROOT_DIR
block|,
literal|"-t"
block|,
literal|"table1"
block|,
literal|"-s"
block|,
literal|"backup"
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
name|BackupDriver
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
name|LOG
operator|.
name|info
argument_list|(
literal|"show_history"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

