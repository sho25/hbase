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
name|TestBackupStatusProgress
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
name|TestBackupStatusProgress
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
name|TestBackupStatusProgress
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Verify that full backup is created on a single table with data correctly.    *    * @throws Exception if doing the backup or an operation on the tables fails    */
annotation|@
name|Test
specifier|public
name|void
name|testBackupStatusProgress
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"test backup status/progress on a single table with data"
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
name|LOG
operator|.
name|info
argument_list|(
literal|"backup complete"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|checkSucceeded
argument_list|(
name|backupId
argument_list|)
argument_list|)
expr_stmt|;
name|BackupInfo
name|info
init|=
name|getBackupAdmin
argument_list|()
operator|.
name|getBackupInfo
argument_list|(
name|backupId
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|info
operator|.
name|getState
argument_list|()
operator|==
name|BackupState
operator|.
name|COMPLETE
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|info
operator|.
name|getShortDescription
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|info
operator|.
name|getProgress
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBackupStatusProgressCommand
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"test backup status/progress on a single table with data: command-line"
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
name|LOG
operator|.
name|info
argument_list|(
literal|"backup complete"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|checkSucceeded
argument_list|(
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
literal|"describe"
block|,
name|backupId
block|}
decl_stmt|;
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
name|String
name|responce
init|=
name|baos
operator|.
name|toString
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|responce
operator|.
name|indexOf
argument_list|(
name|backupId
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|responce
operator|.
name|indexOf
argument_list|(
literal|"COMPLETE"
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|baos
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|()
expr_stmt|;
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
name|args
operator|=
operator|new
name|String
index|[]
block|{
literal|"progress"
block|,
name|backupId
block|}
expr_stmt|;
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
name|responce
operator|=
name|baos
operator|.
name|toString
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|responce
operator|.
name|indexOf
argument_list|(
name|backupId
argument_list|)
operator|>=
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|responce
operator|.
name|indexOf
argument_list|(
literal|"progress"
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|responce
operator|.
name|indexOf
argument_list|(
literal|"100"
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

