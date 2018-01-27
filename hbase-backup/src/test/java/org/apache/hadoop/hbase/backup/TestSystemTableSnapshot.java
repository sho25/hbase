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
name|TestSystemTableSnapshot
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
name|TestSystemTableSnapshot
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Verify backup system table snapshot.    *    * @throws Exception if an operation on the table fails    */
comment|// @Test
specifier|public
name|void
name|_testBackupRestoreSystemTable
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"test snapshot system table"
argument_list|)
expr_stmt|;
name|TableName
name|backupSystem
init|=
name|BackupSystemTable
operator|.
name|getTableName
argument_list|(
name|conf1
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
name|String
name|snapshotName
init|=
literal|"sysTable"
decl_stmt|;
name|hba
operator|.
name|snapshot
argument_list|(
name|snapshotName
argument_list|,
name|backupSystem
argument_list|)
expr_stmt|;
name|hba
operator|.
name|disableTable
argument_list|(
name|backupSystem
argument_list|)
expr_stmt|;
name|hba
operator|.
name|restoreSnapshot
argument_list|(
name|snapshotName
argument_list|)
expr_stmt|;
name|hba
operator|.
name|enableTable
argument_list|(
name|backupSystem
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

