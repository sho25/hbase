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
operator|.
name|impl
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|HashMap
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
name|conf
operator|.
name|Configuration
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
name|FileStatus
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
name|FileSystem
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
name|BackupInfo
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
name|BackupPhase
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
name|BackupRequest
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
name|BackupRestoreConstants
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
name|BackupType
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
name|HBackupFileSystem
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
name|BackupManifest
operator|.
name|BackupImage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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
name|util
operator|.
name|EnvironmentEdgeManager
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
name|FSUtils
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
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * Base class for backup operation. Concrete implementation for  * full and incremental backup are delegated to corresponding sub-classes:  * {@link FullTableBackupClient} and {@link IncrementalTableBackupClient}  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|TableBackupClient
block|{
specifier|public
specifier|static
specifier|final
name|String
name|BACKUP_CLIENT_IMPL_CLASS
init|=
literal|"backup.client.impl.class"
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
specifier|final
name|String
name|BACKUP_TEST_MODE_STAGE
init|=
literal|"backup.test.mode.stage"
decl_stmt|;
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
name|TableBackupClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|protected
name|Connection
name|conn
decl_stmt|;
specifier|protected
name|String
name|backupId
decl_stmt|;
specifier|protected
name|List
argument_list|<
name|TableName
argument_list|>
name|tableList
decl_stmt|;
specifier|protected
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|newTimestamps
init|=
literal|null
decl_stmt|;
specifier|protected
name|BackupManager
name|backupManager
decl_stmt|;
specifier|protected
name|BackupInfo
name|backupInfo
decl_stmt|;
specifier|public
name|TableBackupClient
parameter_list|()
block|{   }
specifier|public
name|TableBackupClient
parameter_list|(
specifier|final
name|Connection
name|conn
parameter_list|,
specifier|final
name|String
name|backupId
parameter_list|,
name|BackupRequest
name|request
parameter_list|)
throws|throws
name|IOException
block|{
name|init
argument_list|(
name|conn
argument_list|,
name|backupId
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|(
specifier|final
name|Connection
name|conn
parameter_list|,
specifier|final
name|String
name|backupId
parameter_list|,
name|BackupRequest
name|request
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|request
operator|.
name|getBackupType
argument_list|()
operator|==
name|BackupType
operator|.
name|FULL
condition|)
block|{
name|backupManager
operator|=
operator|new
name|BackupManager
argument_list|(
name|conn
argument_list|,
name|conn
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|backupManager
operator|=
operator|new
name|IncrementalBackupManager
argument_list|(
name|conn
argument_list|,
name|conn
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|backupId
operator|=
name|backupId
expr_stmt|;
name|this
operator|.
name|tableList
operator|=
name|request
operator|.
name|getTableList
argument_list|()
expr_stmt|;
name|this
operator|.
name|conn
operator|=
name|conn
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conn
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|backupInfo
operator|=
name|backupManager
operator|.
name|createBackupInfo
argument_list|(
name|backupId
argument_list|,
name|request
operator|.
name|getBackupType
argument_list|()
argument_list|,
name|tableList
argument_list|,
name|request
operator|.
name|getTargetRootDir
argument_list|()
argument_list|,
name|request
operator|.
name|getTotalTasks
argument_list|()
argument_list|,
name|request
operator|.
name|getBandwidth
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|tableList
operator|==
literal|null
operator|||
name|tableList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|this
operator|.
name|tableList
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|backupInfo
operator|.
name|getTables
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Start new session
name|backupManager
operator|.
name|startBackupSession
argument_list|()
expr_stmt|;
block|}
comment|/**    * Begin the overall backup.    * @param backupInfo backup info    * @throws IOException exception    */
specifier|protected
name|void
name|beginBackup
parameter_list|(
name|BackupManager
name|backupManager
parameter_list|,
name|BackupInfo
name|backupInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|BackupSystemTable
operator|.
name|snapshot
argument_list|(
name|conn
argument_list|)
expr_stmt|;
name|backupManager
operator|.
name|setBackupInfo
argument_list|(
name|backupInfo
argument_list|)
expr_stmt|;
comment|// set the start timestamp of the overall backup
name|long
name|startTs
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|backupInfo
operator|.
name|setStartTs
argument_list|(
name|startTs
argument_list|)
expr_stmt|;
comment|// set overall backup status: ongoing
name|backupInfo
operator|.
name|setState
argument_list|(
name|BackupState
operator|.
name|RUNNING
argument_list|)
expr_stmt|;
name|backupInfo
operator|.
name|setPhase
argument_list|(
name|BackupPhase
operator|.
name|REQUEST
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Backup "
operator|+
name|backupInfo
operator|.
name|getBackupId
argument_list|()
operator|+
literal|" started at "
operator|+
name|startTs
operator|+
literal|"."
argument_list|)
expr_stmt|;
name|backupManager
operator|.
name|updateBackupInfo
argument_list|(
name|backupInfo
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Backup session "
operator|+
name|backupInfo
operator|.
name|getBackupId
argument_list|()
operator|+
literal|" has been started."
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|String
name|getMessage
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
name|e
operator|.
name|getMessage
argument_list|()
decl_stmt|;
if|if
condition|(
name|msg
operator|==
literal|null
operator|||
name|msg
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|msg
operator|=
name|e
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
return|return
name|msg
return|;
block|}
comment|/**    * Delete HBase snapshot for backup.    * @param backupInfo backup info    * @throws Exception exception    */
specifier|protected
specifier|static
name|void
name|deleteSnapshots
parameter_list|(
specifier|final
name|Connection
name|conn
parameter_list|,
name|BackupInfo
name|backupInfo
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Trying to delete snapshot for full backup."
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|snapshotName
range|:
name|backupInfo
operator|.
name|getSnapshotNames
argument_list|()
control|)
block|{
if|if
condition|(
name|snapshotName
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Trying to delete snapshot: "
operator|+
name|snapshotName
argument_list|)
expr_stmt|;
try|try
init|(
name|Admin
name|admin
init|=
name|conn
operator|.
name|getAdmin
argument_list|()
init|;
init|)
block|{
name|admin
operator|.
name|deleteSnapshot
argument_list|(
name|snapshotName
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Deleting the snapshot "
operator|+
name|snapshotName
operator|+
literal|" for backup "
operator|+
name|backupInfo
operator|.
name|getBackupId
argument_list|()
operator|+
literal|" succeeded."
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Clean up directories with prefix "exportSnapshot-", which are generated when exporting    * snapshots.    * @throws IOException exception    */
specifier|protected
specifier|static
name|void
name|cleanupExportSnapshotLog
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|FSUtils
operator|.
name|getCurrentFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|stagingDir
init|=
operator|new
name|Path
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|BackupRestoreConstants
operator|.
name|CONF_STAGING_ROOT
argument_list|,
name|fs
operator|.
name|getWorkingDirectory
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|files
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|stagingDir
argument_list|)
decl_stmt|;
if|if
condition|(
name|files
operator|==
literal|null
condition|)
block|{
return|return;
block|}
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
if|if
condition|(
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"exportSnapshot-"
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Delete log files of exporting snapshot: "
operator|+
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|FSUtils
operator|.
name|delete
argument_list|(
name|fs
argument_list|,
name|file
operator|.
name|getPath
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|==
literal|false
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can not delete "
operator|+
name|file
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Clean up the uncompleted data at target directory if the ongoing backup has already entered    * the copy phase.    */
specifier|protected
specifier|static
name|void
name|cleanupTargetDir
parameter_list|(
name|BackupInfo
name|backupInfo
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
try|try
block|{
comment|// clean up the uncompleted data at target directory if the ongoing backup has already entered
comment|// the copy phase
name|LOG
operator|.
name|debug
argument_list|(
literal|"Trying to cleanup up target dir. Current backup phase: "
operator|+
name|backupInfo
operator|.
name|getPhase
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|backupInfo
operator|.
name|getPhase
argument_list|()
operator|.
name|equals
argument_list|(
name|BackupPhase
operator|.
name|SNAPSHOTCOPY
argument_list|)
operator|||
name|backupInfo
operator|.
name|getPhase
argument_list|()
operator|.
name|equals
argument_list|(
name|BackupPhase
operator|.
name|INCREMENTAL_COPY
argument_list|)
operator|||
name|backupInfo
operator|.
name|getPhase
argument_list|()
operator|.
name|equals
argument_list|(
name|BackupPhase
operator|.
name|STORE_MANIFEST
argument_list|)
condition|)
block|{
name|FileSystem
name|outputFs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
operator|new
name|Path
argument_list|(
name|backupInfo
operator|.
name|getBackupRootDir
argument_list|()
argument_list|)
operator|.
name|toUri
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
comment|// now treat one backup as a transaction, clean up data that has been partially copied at
comment|// table level
for|for
control|(
name|TableName
name|table
range|:
name|backupInfo
operator|.
name|getTables
argument_list|()
control|)
block|{
name|Path
name|targetDirPath
init|=
operator|new
name|Path
argument_list|(
name|HBackupFileSystem
operator|.
name|getTableBackupDir
argument_list|(
name|backupInfo
operator|.
name|getBackupRootDir
argument_list|()
argument_list|,
name|backupInfo
operator|.
name|getBackupId
argument_list|()
argument_list|,
name|table
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|outputFs
operator|.
name|delete
argument_list|(
name|targetDirPath
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Cleaning up uncompleted backup data at "
operator|+
name|targetDirPath
operator|.
name|toString
argument_list|()
operator|+
literal|" done."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"No data has been copied to "
operator|+
name|targetDirPath
operator|.
name|toString
argument_list|()
operator|+
literal|"."
argument_list|)
expr_stmt|;
block|}
name|Path
name|tableDir
init|=
name|targetDirPath
operator|.
name|getParent
argument_list|()
decl_stmt|;
name|FileStatus
index|[]
name|backups
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|outputFs
argument_list|,
name|tableDir
argument_list|)
decl_stmt|;
if|if
condition|(
name|backups
operator|==
literal|null
operator|||
name|backups
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|outputFs
operator|.
name|delete
argument_list|(
name|tableDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|tableDir
operator|.
name|toString
argument_list|()
operator|+
literal|" is empty, remove it."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Cleaning up uncompleted backup data of "
operator|+
name|backupInfo
operator|.
name|getBackupId
argument_list|()
operator|+
literal|" at "
operator|+
name|backupInfo
operator|.
name|getBackupRootDir
argument_list|()
operator|+
literal|" failed due to "
operator|+
name|e1
operator|.
name|getMessage
argument_list|()
operator|+
literal|"."
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Fail the overall backup.    * @param backupInfo backup info    * @param e exception    * @throws Exception exception    */
specifier|protected
name|void
name|failBackup
parameter_list|(
name|Connection
name|conn
parameter_list|,
name|BackupInfo
name|backupInfo
parameter_list|,
name|BackupManager
name|backupManager
parameter_list|,
name|Exception
name|e
parameter_list|,
name|String
name|msg
parameter_list|,
name|BackupType
name|type
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|LOG
operator|.
name|error
argument_list|(
name|msg
operator|+
name|getMessage
argument_list|(
name|e
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|// If this is a cancel exception, then we've already cleaned.
comment|// set the failure timestamp of the overall backup
name|backupInfo
operator|.
name|setCompleteTs
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|)
expr_stmt|;
comment|// set failure message
name|backupInfo
operator|.
name|setFailedMsg
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
comment|// set overall backup status: failed
name|backupInfo
operator|.
name|setState
argument_list|(
name|BackupState
operator|.
name|FAILED
argument_list|)
expr_stmt|;
comment|// compose the backup failed data
name|String
name|backupFailedData
init|=
literal|"BackupId="
operator|+
name|backupInfo
operator|.
name|getBackupId
argument_list|()
operator|+
literal|",startts="
operator|+
name|backupInfo
operator|.
name|getStartTs
argument_list|()
operator|+
literal|",failedts="
operator|+
name|backupInfo
operator|.
name|getCompleteTs
argument_list|()
operator|+
literal|",failedphase="
operator|+
name|backupInfo
operator|.
name|getPhase
argument_list|()
operator|+
literal|",failedmessage="
operator|+
name|backupInfo
operator|.
name|getFailedMsg
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|backupFailedData
argument_list|)
expr_stmt|;
name|cleanupAndRestoreBackupSystem
argument_list|(
name|conn
argument_list|,
name|backupInfo
argument_list|,
name|conf
argument_list|)
expr_stmt|;
comment|// If backup session is updated to FAILED state - means we
comment|// processed recovery already.
name|backupManager
operator|.
name|updateBackupInfo
argument_list|(
name|backupInfo
argument_list|)
expr_stmt|;
name|backupManager
operator|.
name|finishBackupSession
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Backup "
operator|+
name|backupInfo
operator|.
name|getBackupId
argument_list|()
operator|+
literal|" failed."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ee
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Please run backup repair tool manually to restore backup system integrity"
argument_list|)
expr_stmt|;
throw|throw
name|ee
throw|;
block|}
block|}
specifier|public
specifier|static
name|void
name|cleanupAndRestoreBackupSystem
parameter_list|(
name|Connection
name|conn
parameter_list|,
name|BackupInfo
name|backupInfo
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|BackupType
name|type
init|=
name|backupInfo
operator|.
name|getType
argument_list|()
decl_stmt|;
comment|// if full backup, then delete HBase snapshots if there already are snapshots taken
comment|// and also clean up export snapshot log files if exist
if|if
condition|(
name|type
operator|==
name|BackupType
operator|.
name|FULL
condition|)
block|{
name|deleteSnapshots
argument_list|(
name|conn
argument_list|,
name|backupInfo
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|cleanupExportSnapshotLog
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
name|BackupSystemTable
operator|.
name|restoreFromSnapshot
argument_list|(
name|conn
argument_list|)
expr_stmt|;
name|BackupSystemTable
operator|.
name|deleteSnapshot
argument_list|(
name|conn
argument_list|)
expr_stmt|;
comment|// clean up the uncompleted data at target directory if the ongoing backup has already entered
comment|// the copy phase
comment|// For incremental backup, DistCp logs will be cleaned with the targetDir.
name|cleanupTargetDir
argument_list|(
name|backupInfo
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add manifest for the current backup. The manifest is stored within the table backup directory.    * @param backupInfo The current backup info    * @throws IOException exception    * @throws BackupException exception    */
specifier|protected
name|void
name|addManifest
parameter_list|(
name|BackupInfo
name|backupInfo
parameter_list|,
name|BackupManager
name|backupManager
parameter_list|,
name|BackupType
name|type
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|BackupException
block|{
comment|// set the overall backup phase : store manifest
name|backupInfo
operator|.
name|setPhase
argument_list|(
name|BackupPhase
operator|.
name|STORE_MANIFEST
argument_list|)
expr_stmt|;
name|BackupManifest
name|manifest
decl_stmt|;
comment|// Since we have each table's backup in its own directory structure,
comment|// we'll store its manifest with the table directory.
for|for
control|(
name|TableName
name|table
range|:
name|backupInfo
operator|.
name|getTables
argument_list|()
control|)
block|{
name|manifest
operator|=
operator|new
name|BackupManifest
argument_list|(
name|backupInfo
argument_list|,
name|table
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|BackupImage
argument_list|>
name|ancestors
init|=
name|backupManager
operator|.
name|getAncestors
argument_list|(
name|backupInfo
argument_list|,
name|table
argument_list|)
decl_stmt|;
for|for
control|(
name|BackupImage
name|image
range|:
name|ancestors
control|)
block|{
name|manifest
operator|.
name|addDependentImage
argument_list|(
name|image
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|type
operator|==
name|BackupType
operator|.
name|INCREMENTAL
condition|)
block|{
comment|// We'll store the log timestamps for this table only in its manifest.
name|HashMap
argument_list|<
name|TableName
argument_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|>
name|tableTimestampMap
init|=
operator|new
name|HashMap
argument_list|<
name|TableName
argument_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|tableTimestampMap
operator|.
name|put
argument_list|(
name|table
argument_list|,
name|backupInfo
operator|.
name|getIncrTimestampMap
argument_list|()
operator|.
name|get
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
name|manifest
operator|.
name|setIncrTimestampMap
argument_list|(
name|tableTimestampMap
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|BackupImage
argument_list|>
name|ancestorss
init|=
name|backupManager
operator|.
name|getAncestors
argument_list|(
name|backupInfo
argument_list|)
decl_stmt|;
for|for
control|(
name|BackupImage
name|image
range|:
name|ancestorss
control|)
block|{
name|manifest
operator|.
name|addDependentImage
argument_list|(
name|image
argument_list|)
expr_stmt|;
block|}
block|}
name|manifest
operator|.
name|store
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|// For incremental backup, we store a overall manifest in
comment|//<backup-root-dir>/WALs/<backup-id>
comment|// This is used when created the next incremental backup
if|if
condition|(
name|type
operator|==
name|BackupType
operator|.
name|INCREMENTAL
condition|)
block|{
name|manifest
operator|=
operator|new
name|BackupManifest
argument_list|(
name|backupInfo
argument_list|)
expr_stmt|;
comment|// set the table region server start and end timestamps for incremental backup
name|manifest
operator|.
name|setIncrTimestampMap
argument_list|(
name|backupInfo
operator|.
name|getIncrTimestampMap
argument_list|()
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|BackupImage
argument_list|>
name|ancestors
init|=
name|backupManager
operator|.
name|getAncestors
argument_list|(
name|backupInfo
argument_list|)
decl_stmt|;
for|for
control|(
name|BackupImage
name|image
range|:
name|ancestors
control|)
block|{
name|manifest
operator|.
name|addDependentImage
argument_list|(
name|image
argument_list|)
expr_stmt|;
block|}
name|manifest
operator|.
name|store
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Get backup request meta data dir as string.    * @param backupInfo backup info    * @return meta data dir    */
specifier|protected
name|String
name|obtainBackupMetaDataStr
parameter_list|(
name|BackupInfo
name|backupInfo
parameter_list|)
block|{
name|StringBuffer
name|sb
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"type="
operator|+
name|backupInfo
operator|.
name|getType
argument_list|()
operator|+
literal|",tablelist="
argument_list|)
expr_stmt|;
for|for
control|(
name|TableName
name|table
range|:
name|backupInfo
operator|.
name|getTables
argument_list|()
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|table
operator|+
literal|";"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sb
operator|.
name|lastIndexOf
argument_list|(
literal|";"
argument_list|)
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|delete
argument_list|(
name|sb
operator|.
name|lastIndexOf
argument_list|(
literal|";"
argument_list|)
argument_list|,
name|sb
operator|.
name|lastIndexOf
argument_list|(
literal|";"
argument_list|)
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|",targetRootDir="
operator|+
name|backupInfo
operator|.
name|getBackupRootDir
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Clean up directories with prefix "_distcp_logs-", which are generated when DistCp copying    * hlogs.    * @throws IOException exception    */
specifier|protected
name|void
name|cleanupDistCpLog
parameter_list|(
name|BackupInfo
name|backupInfo
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|rootPath
init|=
operator|new
name|Path
argument_list|(
name|backupInfo
operator|.
name|getHLogTargetDir
argument_list|()
argument_list|)
operator|.
name|getParent
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|rootPath
operator|.
name|toUri
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|files
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|rootPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|files
operator|==
literal|null
condition|)
block|{
return|return;
block|}
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
if|if
condition|(
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"_distcp_logs"
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Delete log files of DistCp: "
operator|+
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|delete
argument_list|(
name|fs
argument_list|,
name|file
operator|.
name|getPath
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Complete the overall backup.    * @param backupInfo backup info    * @throws Exception exception    */
specifier|protected
name|void
name|completeBackup
parameter_list|(
specifier|final
name|Connection
name|conn
parameter_list|,
name|BackupInfo
name|backupInfo
parameter_list|,
name|BackupManager
name|backupManager
parameter_list|,
name|BackupType
name|type
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
comment|// set the complete timestamp of the overall backup
name|backupInfo
operator|.
name|setCompleteTs
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|)
expr_stmt|;
comment|// set overall backup status: complete
name|backupInfo
operator|.
name|setState
argument_list|(
name|BackupState
operator|.
name|COMPLETE
argument_list|)
expr_stmt|;
name|backupInfo
operator|.
name|setProgress
argument_list|(
literal|100
argument_list|)
expr_stmt|;
comment|// add and store the manifest for the backup
name|addManifest
argument_list|(
name|backupInfo
argument_list|,
name|backupManager
argument_list|,
name|type
argument_list|,
name|conf
argument_list|)
expr_stmt|;
comment|// compose the backup complete data
name|String
name|backupCompleteData
init|=
name|obtainBackupMetaDataStr
argument_list|(
name|backupInfo
argument_list|)
operator|+
literal|",startts="
operator|+
name|backupInfo
operator|.
name|getStartTs
argument_list|()
operator|+
literal|",completets="
operator|+
name|backupInfo
operator|.
name|getCompleteTs
argument_list|()
operator|+
literal|",bytescopied="
operator|+
name|backupInfo
operator|.
name|getTotalBytesCopied
argument_list|()
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Backup "
operator|+
name|backupInfo
operator|.
name|getBackupId
argument_list|()
operator|+
literal|" finished: "
operator|+
name|backupCompleteData
argument_list|)
expr_stmt|;
block|}
comment|// when full backup is done:
comment|// - delete HBase snapshot
comment|// - clean up directories with prefix "exportSnapshot-", which are generated when exporting
comment|// snapshots
if|if
condition|(
name|type
operator|==
name|BackupType
operator|.
name|FULL
condition|)
block|{
name|deleteSnapshots
argument_list|(
name|conn
argument_list|,
name|backupInfo
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|cleanupExportSnapshotLog
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|BackupType
operator|.
name|INCREMENTAL
condition|)
block|{
name|cleanupDistCpLog
argument_list|(
name|backupInfo
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
name|BackupSystemTable
operator|.
name|deleteSnapshot
argument_list|(
name|conn
argument_list|)
expr_stmt|;
name|backupManager
operator|.
name|updateBackupInfo
argument_list|(
name|backupInfo
argument_list|)
expr_stmt|;
comment|// Finish active session
name|backupManager
operator|.
name|finishBackupSession
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Backup "
operator|+
name|backupInfo
operator|.
name|getBackupId
argument_list|()
operator|+
literal|" completed."
argument_list|)
expr_stmt|;
block|}
comment|/**    * Backup request execution    * @throws IOException    */
specifier|public
specifier|abstract
name|void
name|execute
parameter_list|()
throws|throws
name|IOException
function_decl|;
annotation|@
name|VisibleForTesting
specifier|protected
name|Stage
name|getTestStage
parameter_list|()
block|{
return|return
name|Stage
operator|.
name|valueOf
argument_list|(
literal|"stage_"
operator|+
name|conf
operator|.
name|getInt
argument_list|(
name|BACKUP_TEST_MODE_STAGE
argument_list|,
literal|0
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|protected
name|void
name|failStageIf
parameter_list|(
name|Stage
name|stage
parameter_list|)
throws|throws
name|IOException
block|{
name|Stage
name|current
init|=
name|getTestStage
argument_list|()
decl_stmt|;
if|if
condition|(
name|current
operator|==
name|stage
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed stage "
operator|+
name|stage
operator|+
literal|" in testing"
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
enum|enum
name|Stage
block|{
name|stage_0
block|,
name|stage_1
block|,
name|stage_2
block|,
name|stage_3
block|,
name|stage_4
block|}
block|}
end_class

end_unit

