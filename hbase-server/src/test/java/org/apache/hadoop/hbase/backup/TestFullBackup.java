begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license  * agreements. See the NOTICE file distributed with this work for additional information regarding  * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License. You may obtain a  * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable  * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"  * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License  * for the specific language governing permissions and limitations under the License.  */
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
comment|/**    * Verify that full backup is created on a single table with data correctly.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testFullBackupSingle
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"test full backup on a single table with data"
argument_list|)
expr_stmt|;
name|String
name|backupId
init|=
name|BackupClient
operator|.
name|create
argument_list|(
literal|"full"
argument_list|,
name|BACKUP_ROOT_DIR
argument_list|,
name|table1
operator|.
name|getNameAsString
argument_list|()
argument_list|,
literal|null
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
block|}
comment|/**    * Verify that full backup is created on multiple tables correctly.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testFullBackupMultiple
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"create full backup image on multiple tables with data"
argument_list|)
expr_stmt|;
name|String
name|tableset
init|=
name|table1
operator|.
name|getNameAsString
argument_list|()
operator|+
name|BackupRestoreConstants
operator|.
name|TABLENAME_DELIMITER_IN_COMMAND
operator|+
name|table2
operator|.
name|getNameAsString
argument_list|()
decl_stmt|;
name|String
name|backupId
init|=
name|BackupClient
operator|.
name|create
argument_list|(
literal|"full"
argument_list|,
name|BACKUP_ROOT_DIR
argument_list|,
name|tableset
argument_list|,
literal|null
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
block|}
comment|/**    * Verify that full backup is created on all tables correctly.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testFullBackupAll
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"create full backup image on all tables"
argument_list|)
expr_stmt|;
name|String
name|backupId
init|=
name|BackupClient
operator|.
name|create
argument_list|(
literal|"full"
argument_list|,
name|BACKUP_ROOT_DIR
argument_list|,
literal|null
argument_list|,
literal|null
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
block|}
comment|/**    * Verify that full backup is created on a table correctly using a snapshot.    * @throws Exception    */
comment|//@Test
comment|//public void testFullBackupUsingSnapshot() throws Exception {
comment|// HBaseAdmin hba = new HBaseAdmin(conf1);
comment|//String snapshot = "snapshot";
comment|//hba.snapshot(snapshot, table1);
comment|//LOG.info("create full backup image on a table using snapshot");
comment|//String backupId =
comment|//    BackupClient.create("full", BACKUP_ROOT_DIR, table1.getNameAsString(),
comment|//      snapshot);
comment|// }
block|}
end_class

end_unit

