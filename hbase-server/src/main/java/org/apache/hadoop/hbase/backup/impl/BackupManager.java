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
name|Closeable
import|;
end_import

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
name|Iterator
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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|HConstants
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
name|HTableDescriptor
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
name|hadoop
operator|.
name|hbase
operator|.
name|backup
operator|.
name|master
operator|.
name|BackupLogCleaner
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
name|master
operator|.
name|LogRollMasterProcedureManager
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
name|regionserver
operator|.
name|LogRollRegionServerProcedureManager
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
name|classification
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
name|procedure
operator|.
name|ProcedureManagerHost
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
name|Pair
import|;
end_import

begin_import
import|import
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
comment|/**  * Handles backup requests, creates backup info records in backup system table to  * keep track of backup sessions, dispatches backup request.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|BackupManager
implements|implements
name|Closeable
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
name|BackupManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|Configuration
name|conf
init|=
literal|null
decl_stmt|;
specifier|protected
name|BackupInfo
name|backupInfo
init|=
literal|null
decl_stmt|;
specifier|protected
name|BackupSystemTable
name|systemTable
decl_stmt|;
specifier|protected
specifier|final
name|Connection
name|conn
decl_stmt|;
comment|/**    * Backup manager constructor.    * @param conn connection    * @param conf configuration    * @throws IOException exception    */
specifier|public
name|BackupManager
parameter_list|(
name|Connection
name|conn
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|conf
operator|.
name|getBoolean
argument_list|(
name|BackupRestoreConstants
operator|.
name|BACKUP_ENABLE_KEY
argument_list|,
name|BackupRestoreConstants
operator|.
name|BACKUP_ENABLE_DEFAULT
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|BackupException
argument_list|(
literal|"HBase backup is not enabled. Check your "
operator|+
name|BackupRestoreConstants
operator|.
name|BACKUP_ENABLE_KEY
operator|+
literal|" setting."
argument_list|)
throw|;
block|}
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|conn
operator|=
name|conn
expr_stmt|;
name|this
operator|.
name|systemTable
operator|=
operator|new
name|BackupSystemTable
argument_list|(
name|conn
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns backup info    */
specifier|protected
name|BackupInfo
name|getBackupInfo
parameter_list|()
block|{
return|return
name|backupInfo
return|;
block|}
comment|/**    * This method modifies the master's configuration in order to inject backup-related features    * (TESTs only)    * @param conf configuration    */
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|void
name|decorateMasterConfiguration
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
operator|!
name|isBackupEnabled
argument_list|(
name|conf
argument_list|)
condition|)
block|{
return|return;
block|}
comment|// Add WAL archive cleaner plug-in
name|String
name|plugins
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|HBASE_MASTER_LOGCLEANER_PLUGINS
argument_list|)
decl_stmt|;
name|String
name|cleanerClass
init|=
name|BackupLogCleaner
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|plugins
operator|.
name|contains
argument_list|(
name|cleanerClass
argument_list|)
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_MASTER_LOGCLEANER_PLUGINS
argument_list|,
name|plugins
operator|+
literal|","
operator|+
name|cleanerClass
argument_list|)
expr_stmt|;
block|}
name|String
name|classes
init|=
name|conf
operator|.
name|get
argument_list|(
name|ProcedureManagerHost
operator|.
name|MASTER_PROCEDURE_CONF_KEY
argument_list|)
decl_stmt|;
name|String
name|masterProcedureClass
init|=
name|LogRollMasterProcedureManager
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|classes
operator|==
literal|null
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|ProcedureManagerHost
operator|.
name|MASTER_PROCEDURE_CONF_KEY
argument_list|,
name|masterProcedureClass
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|classes
operator|.
name|contains
argument_list|(
name|masterProcedureClass
argument_list|)
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|ProcedureManagerHost
operator|.
name|MASTER_PROCEDURE_CONF_KEY
argument_list|,
name|classes
operator|+
literal|","
operator|+
name|masterProcedureClass
argument_list|)
expr_stmt|;
block|}
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
literal|"Added log cleaner: "
operator|+
name|cleanerClass
operator|+
literal|"\n"
operator|+
literal|"Added master procedure manager: "
operator|+
name|masterProcedureClass
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * This method modifies the Region Server configuration in order to inject backup-related features    * TESTs only.    * @param conf configuration    */
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|void
name|decorateRegionServerConfiguration
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
operator|!
name|isBackupEnabled
argument_list|(
name|conf
argument_list|)
condition|)
block|{
return|return;
block|}
name|String
name|classes
init|=
name|conf
operator|.
name|get
argument_list|(
name|ProcedureManagerHost
operator|.
name|REGIONSERVER_PROCEDURE_CONF_KEY
argument_list|)
decl_stmt|;
name|String
name|regionProcedureClass
init|=
name|LogRollRegionServerProcedureManager
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|classes
operator|==
literal|null
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|ProcedureManagerHost
operator|.
name|REGIONSERVER_PROCEDURE_CONF_KEY
argument_list|,
name|regionProcedureClass
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|classes
operator|.
name|contains
argument_list|(
name|regionProcedureClass
argument_list|)
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|ProcedureManagerHost
operator|.
name|REGIONSERVER_PROCEDURE_CONF_KEY
argument_list|,
name|classes
operator|+
literal|","
operator|+
name|regionProcedureClass
argument_list|)
expr_stmt|;
block|}
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
literal|"Added region procedure manager: "
operator|+
name|regionProcedureClass
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|boolean
name|isBackupEnabled
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
name|BackupRestoreConstants
operator|.
name|BACKUP_ENABLE_KEY
argument_list|,
name|BackupRestoreConstants
operator|.
name|BACKUP_ENABLE_DEFAULT
argument_list|)
return|;
block|}
comment|/**    * Get configuration    * @return configuration    */
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
comment|/**    * Stop all the work of backup.    */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|systemTable
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|systemTable
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Creates a backup info based on input backup request.    * @param backupId backup id    * @param type type    * @param tableList table list    * @param targetRootDir root dir    * @param workers number of parallel workers    * @param bandwidth bandwidth per worker in MB per sec    * @return BackupInfo    * @throws BackupException exception    */
specifier|public
name|BackupInfo
name|createBackupInfo
parameter_list|(
name|String
name|backupId
parameter_list|,
name|BackupType
name|type
parameter_list|,
name|List
argument_list|<
name|TableName
argument_list|>
name|tableList
parameter_list|,
name|String
name|targetRootDir
parameter_list|,
name|int
name|workers
parameter_list|,
name|long
name|bandwidth
parameter_list|)
throws|throws
name|BackupException
block|{
if|if
condition|(
name|targetRootDir
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|BackupException
argument_list|(
literal|"Wrong backup request parameter: target backup root directory"
argument_list|)
throw|;
block|}
if|if
condition|(
name|type
operator|==
name|BackupType
operator|.
name|FULL
operator|&&
operator|(
name|tableList
operator|==
literal|null
operator|||
name|tableList
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
comment|// If table list is null for full backup, which means backup all tables. Then fill the table
comment|// list with all user tables from meta. It no table available, throw the request exception.
name|HTableDescriptor
index|[]
name|htds
init|=
literal|null
decl_stmt|;
try|try
init|(
name|Admin
name|admin
init|=
name|conn
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|htds
operator|=
name|admin
operator|.
name|listTables
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|BackupException
argument_list|(
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|htds
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|BackupException
argument_list|(
literal|"No table exists for full backup of all tables."
argument_list|)
throw|;
block|}
else|else
block|{
name|tableList
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|HTableDescriptor
name|hTableDescriptor
range|:
name|htds
control|)
block|{
name|TableName
name|tn
init|=
name|hTableDescriptor
operator|.
name|getTableName
argument_list|()
decl_stmt|;
if|if
condition|(
name|tn
operator|.
name|equals
argument_list|(
name|BackupSystemTable
operator|.
name|getTableName
argument_list|(
name|conf
argument_list|)
argument_list|)
condition|)
block|{
comment|// skip backup system table
continue|continue;
block|}
name|tableList
operator|.
name|add
argument_list|(
name|hTableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Full backup all the tables available in the cluster: "
operator|+
name|tableList
argument_list|)
expr_stmt|;
block|}
block|}
comment|// there are one or more tables in the table list
name|backupInfo
operator|=
operator|new
name|BackupInfo
argument_list|(
name|backupId
argument_list|,
name|type
argument_list|,
name|tableList
operator|.
name|toArray
argument_list|(
operator|new
name|TableName
index|[
name|tableList
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|targetRootDir
argument_list|)
expr_stmt|;
name|backupInfo
operator|.
name|setBandwidth
argument_list|(
name|bandwidth
argument_list|)
expr_stmt|;
name|backupInfo
operator|.
name|setWorkers
argument_list|(
name|workers
argument_list|)
expr_stmt|;
return|return
name|backupInfo
return|;
block|}
comment|/**    * Check if any ongoing backup. Currently, we only reply on checking status in backup system    * table. We need to consider to handle the case of orphan records in the future. Otherwise, all    * the coming request will fail.    * @return the ongoing backup id if on going backup exists, otherwise null    * @throws IOException exception    */
specifier|private
name|String
name|getOngoingBackupId
parameter_list|()
throws|throws
name|IOException
block|{
name|ArrayList
argument_list|<
name|BackupInfo
argument_list|>
name|sessions
init|=
name|systemTable
operator|.
name|getBackupInfos
argument_list|(
name|BackupState
operator|.
name|RUNNING
argument_list|)
decl_stmt|;
if|if
condition|(
name|sessions
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|sessions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getBackupId
argument_list|()
return|;
block|}
comment|/**    * Start the backup manager service.    * @throws IOException exception    */
specifier|public
name|void
name|initialize
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|ongoingBackupId
init|=
name|this
operator|.
name|getOngoingBackupId
argument_list|()
decl_stmt|;
if|if
condition|(
name|ongoingBackupId
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"There is a ongoing backup "
operator|+
name|ongoingBackupId
operator|+
literal|". Can not launch new backup until no ongoing backup remains."
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|BackupException
argument_list|(
literal|"There is ongoing backup."
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|setBackupInfo
parameter_list|(
name|BackupInfo
name|backupInfo
parameter_list|)
block|{
name|this
operator|.
name|backupInfo
operator|=
name|backupInfo
expr_stmt|;
block|}
comment|/**    * Get direct ancestors of the current backup.    * @param backupInfo The backup info for the current backup    * @return The ancestors for the current backup    * @throws IOException exception    * @throws BackupException exception    */
specifier|public
name|ArrayList
argument_list|<
name|BackupImage
argument_list|>
name|getAncestors
parameter_list|(
name|BackupInfo
name|backupInfo
parameter_list|)
throws|throws
name|IOException
throws|,
name|BackupException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Getting the direct ancestors of the current backup "
operator|+
name|backupInfo
operator|.
name|getBackupId
argument_list|()
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|BackupImage
argument_list|>
name|ancestors
init|=
operator|new
name|ArrayList
argument_list|<
name|BackupImage
argument_list|>
argument_list|()
decl_stmt|;
comment|// full backup does not have ancestor
if|if
condition|(
name|backupInfo
operator|.
name|getType
argument_list|()
operator|==
name|BackupType
operator|.
name|FULL
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Current backup is a full backup, no direct ancestor for it."
argument_list|)
expr_stmt|;
return|return
name|ancestors
return|;
block|}
comment|// get all backup history list in descending order
name|ArrayList
argument_list|<
name|BackupInfo
argument_list|>
name|allHistoryList
init|=
name|getBackupHistory
argument_list|(
literal|true
argument_list|)
decl_stmt|;
for|for
control|(
name|BackupInfo
name|backup
range|:
name|allHistoryList
control|)
block|{
name|BackupImage
operator|.
name|Builder
name|builder
init|=
name|BackupImage
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|BackupImage
name|image
init|=
name|builder
operator|.
name|withBackupId
argument_list|(
name|backup
operator|.
name|getBackupId
argument_list|()
argument_list|)
operator|.
name|withType
argument_list|(
name|backup
operator|.
name|getType
argument_list|()
argument_list|)
operator|.
name|withRootDir
argument_list|(
name|backup
operator|.
name|getBackupRootDir
argument_list|()
argument_list|)
operator|.
name|withTableList
argument_list|(
name|backup
operator|.
name|getTableNames
argument_list|()
argument_list|)
operator|.
name|withStartTime
argument_list|(
name|backup
operator|.
name|getStartTs
argument_list|()
argument_list|)
operator|.
name|withCompleteTime
argument_list|(
name|backup
operator|.
name|getCompleteTs
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// add the full backup image as an ancestor until the last incremental backup
if|if
condition|(
name|backup
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
name|BackupType
operator|.
name|FULL
argument_list|)
condition|)
block|{
comment|// check the backup image coverage, if previous image could be covered by the newer ones,
comment|// then no need to add
if|if
condition|(
operator|!
name|BackupManifest
operator|.
name|canCoverImage
argument_list|(
name|ancestors
argument_list|,
name|image
argument_list|)
condition|)
block|{
name|ancestors
operator|.
name|add
argument_list|(
name|image
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// found last incremental backup, if previously added full backup ancestor images can cover
comment|// it, then this incremental ancestor is not the dependent of the current incremental
comment|// backup, that is to say, this is the backup scope boundary of current table set.
comment|// Otherwise, this incremental backup ancestor is the dependent ancestor of the ongoing
comment|// incremental backup
if|if
condition|(
name|BackupManifest
operator|.
name|canCoverImage
argument_list|(
name|ancestors
argument_list|,
name|image
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Met the backup boundary of the current table set:"
argument_list|)
expr_stmt|;
for|for
control|(
name|BackupImage
name|image1
range|:
name|ancestors
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"  BackupID="
operator|+
name|image1
operator|.
name|getBackupId
argument_list|()
operator|+
literal|", BackupDir="
operator|+
name|image1
operator|.
name|getRootDir
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|Path
name|logBackupPath
init|=
name|HBackupFileSystem
operator|.
name|getLogBackupPath
argument_list|(
name|backup
operator|.
name|getBackupRootDir
argument_list|()
argument_list|,
name|backup
operator|.
name|getBackupId
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Current backup has an incremental backup ancestor, "
operator|+
literal|"touching its image manifest in "
operator|+
name|logBackupPath
operator|.
name|toString
argument_list|()
operator|+
literal|" to construct the dependency."
argument_list|)
expr_stmt|;
name|BackupManifest
name|lastIncrImgManifest
init|=
operator|new
name|BackupManifest
argument_list|(
name|conf
argument_list|,
name|logBackupPath
argument_list|)
decl_stmt|;
name|BackupImage
name|lastIncrImage
init|=
name|lastIncrImgManifest
operator|.
name|getBackupImage
argument_list|()
decl_stmt|;
name|ancestors
operator|.
name|add
argument_list|(
name|lastIncrImage
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Last dependent incremental backup image: "
operator|+
literal|"{BackupID="
operator|+
name|lastIncrImage
operator|.
name|getBackupId
argument_list|()
operator|+
literal|","
operator|+
literal|"BackupDir="
operator|+
name|lastIncrImage
operator|.
name|getRootDir
argument_list|()
operator|+
literal|"}"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Got "
operator|+
name|ancestors
operator|.
name|size
argument_list|()
operator|+
literal|" ancestors for the current backup."
argument_list|)
expr_stmt|;
return|return
name|ancestors
return|;
block|}
comment|/**    * Get the direct ancestors of this backup for one table involved.    * @param backupInfo backup info    * @param table table    * @return backupImages on the dependency list    * @throws BackupException exception    * @throws IOException exception    */
specifier|public
name|ArrayList
argument_list|<
name|BackupImage
argument_list|>
name|getAncestors
parameter_list|(
name|BackupInfo
name|backupInfo
parameter_list|,
name|TableName
name|table
parameter_list|)
throws|throws
name|BackupException
throws|,
name|IOException
block|{
name|ArrayList
argument_list|<
name|BackupImage
argument_list|>
name|ancestors
init|=
name|getAncestors
argument_list|(
name|backupInfo
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|BackupImage
argument_list|>
name|tableAncestors
init|=
operator|new
name|ArrayList
argument_list|<
name|BackupImage
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|BackupImage
name|image
range|:
name|ancestors
control|)
block|{
if|if
condition|(
name|image
operator|.
name|hasTable
argument_list|(
name|table
argument_list|)
condition|)
block|{
name|tableAncestors
operator|.
name|add
argument_list|(
name|image
argument_list|)
expr_stmt|;
if|if
condition|(
name|image
operator|.
name|getType
argument_list|()
operator|==
name|BackupType
operator|.
name|FULL
condition|)
block|{
break|break;
block|}
block|}
block|}
return|return
name|tableAncestors
return|;
block|}
comment|/*    * backup system table operations    */
comment|/**    * Updates status (state) of a backup session in a persistent store    * @param context context    * @throws IOException exception    */
specifier|public
name|void
name|updateBackupInfo
parameter_list|(
name|BackupInfo
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|systemTable
operator|.
name|updateBackupInfo
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
comment|/**    * Read the last backup start code (timestamp) of last successful backup. Will return null if    * there is no startcode stored in backup system table or the value is of length 0. These two    * cases indicate there is no successful backup completed so far.    * @return the timestamp of a last successful backup    * @throws IOException exception    */
specifier|public
name|String
name|readBackupStartCode
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|systemTable
operator|.
name|readBackupStartCode
argument_list|(
name|backupInfo
operator|.
name|getBackupRootDir
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Write the start code (timestamp) to backup system table. If passed in null, then write 0 byte.    * @param startCode start code    * @throws IOException exception    */
specifier|public
name|void
name|writeBackupStartCode
parameter_list|(
name|Long
name|startCode
parameter_list|)
throws|throws
name|IOException
block|{
name|systemTable
operator|.
name|writeBackupStartCode
argument_list|(
name|startCode
argument_list|,
name|backupInfo
operator|.
name|getBackupRootDir
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the RS log information after the last log roll from backup system table.    * @return RS log info    * @throws IOException exception    */
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|readRegionServerLastLogRollResult
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|systemTable
operator|.
name|readRegionServerLastLogRollResult
argument_list|(
name|backupInfo
operator|.
name|getBackupRootDir
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|Pair
argument_list|<
name|Map
argument_list|<
name|TableName
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Pair
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
argument_list|>
argument_list|>
argument_list|>
argument_list|>
argument_list|,
name|List
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|readBulkloadRows
parameter_list|(
name|List
argument_list|<
name|TableName
argument_list|>
name|tableList
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|systemTable
operator|.
name|readBulkloadRows
argument_list|(
name|tableList
argument_list|)
return|;
block|}
specifier|public
name|void
name|removeBulkLoadedRows
parameter_list|(
name|List
argument_list|<
name|TableName
argument_list|>
name|lst
parameter_list|,
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|rows
parameter_list|)
throws|throws
name|IOException
block|{
name|systemTable
operator|.
name|removeBulkLoadedRows
argument_list|(
name|lst
argument_list|,
name|rows
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeBulkLoadedFiles
parameter_list|(
name|List
argument_list|<
name|TableName
argument_list|>
name|sTableList
parameter_list|,
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Path
argument_list|>
argument_list|>
index|[]
name|maps
parameter_list|)
throws|throws
name|IOException
block|{
name|systemTable
operator|.
name|writeBulkLoadedFiles
argument_list|(
name|sTableList
argument_list|,
name|maps
argument_list|,
name|backupInfo
operator|.
name|getBackupId
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get all completed backup information (in desc order by time)    * @return history info of BackupCompleteData    * @throws IOException exception    */
specifier|public
name|List
argument_list|<
name|BackupInfo
argument_list|>
name|getBackupHistory
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|systemTable
operator|.
name|getBackupHistory
argument_list|()
return|;
block|}
specifier|public
name|ArrayList
argument_list|<
name|BackupInfo
argument_list|>
name|getBackupHistory
parameter_list|(
name|boolean
name|completed
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|systemTable
operator|.
name|getBackupHistory
argument_list|(
name|completed
argument_list|)
return|;
block|}
comment|/**    * Write the current timestamps for each regionserver to backup system table after a successful    * full or incremental backup. Each table may have a different set of log timestamps. The saved    * timestamp is of the last log file that was backed up already.    * @param tables tables    * @throws IOException exception    */
specifier|public
name|void
name|writeRegionServerLogTimestamp
parameter_list|(
name|Set
argument_list|<
name|TableName
argument_list|>
name|tables
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|newTimestamps
parameter_list|)
throws|throws
name|IOException
block|{
name|systemTable
operator|.
name|writeRegionServerLogTimestamp
argument_list|(
name|tables
argument_list|,
name|newTimestamps
argument_list|,
name|backupInfo
operator|.
name|getBackupRootDir
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Read the timestamp for each region server log after the last successful backup. Each table has    * its own set of the timestamps.    * @return the timestamp for each region server. key: tableName value:    *         RegionServer,PreviousTimeStamp    * @throws IOException exception    */
specifier|public
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
name|readLogTimestampMap
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|systemTable
operator|.
name|readLogTimestampMap
argument_list|(
name|backupInfo
operator|.
name|getBackupRootDir
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Return the current tables covered by incremental backup.    * @return set of tableNames    * @throws IOException exception    */
specifier|public
name|Set
argument_list|<
name|TableName
argument_list|>
name|getIncrementalBackupTableSet
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|systemTable
operator|.
name|getIncrementalBackupTableSet
argument_list|(
name|backupInfo
operator|.
name|getBackupRootDir
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Adds set of tables to overall incremental backup table set    * @param tables tables    * @throws IOException exception    */
specifier|public
name|void
name|addIncrementalBackupTableSet
parameter_list|(
name|Set
argument_list|<
name|TableName
argument_list|>
name|tables
parameter_list|)
throws|throws
name|IOException
block|{
name|systemTable
operator|.
name|addIncrementalBackupTableSet
argument_list|(
name|tables
argument_list|,
name|backupInfo
operator|.
name|getBackupRootDir
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Saves list of WAL files after incremental backup operation. These files will be stored until    * TTL expiration and are used by Backup Log Cleaner plugin to determine which WAL files can be    * safely purged.    */
specifier|public
name|void
name|recordWALFiles
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|files
parameter_list|)
throws|throws
name|IOException
block|{
name|systemTable
operator|.
name|addWALFiles
argument_list|(
name|files
argument_list|,
name|backupInfo
operator|.
name|getBackupId
argument_list|()
argument_list|,
name|backupInfo
operator|.
name|getBackupRootDir
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get WAL files iterator    * @return WAL files iterator from backup system table    * @throws IOException    */
specifier|public
name|Iterator
argument_list|<
name|BackupSystemTable
operator|.
name|WALItem
argument_list|>
name|getWALFilesFromBackupSystem
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|systemTable
operator|.
name|getWALFilesIterator
argument_list|(
name|backupInfo
operator|.
name|getBackupRootDir
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|Connection
name|getConnection
parameter_list|()
block|{
return|return
name|conn
return|;
block|}
block|}
end_class

end_unit

