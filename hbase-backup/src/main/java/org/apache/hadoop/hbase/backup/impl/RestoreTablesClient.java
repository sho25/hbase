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
import|import static
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
operator|.
name|JOB_NAME_CONF_KEY
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|lang3
operator|.
name|StringUtils
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
name|LocatedFileStatus
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
name|fs
operator|.
name|RemoteIterator
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
name|RestoreRequest
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
name|util
operator|.
name|RestoreTool
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
name|io
operator|.
name|hfile
operator|.
name|HFile
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

begin_comment
comment|/**  * Restore table implementation  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RestoreTablesClient
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
name|RestoreTablesClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|Connection
name|conn
decl_stmt|;
specifier|private
name|String
name|backupId
decl_stmt|;
specifier|private
name|TableName
index|[]
name|sTableArray
decl_stmt|;
specifier|private
name|TableName
index|[]
name|tTableArray
decl_stmt|;
specifier|private
name|String
name|targetRootDir
decl_stmt|;
specifier|private
name|boolean
name|isOverwrite
decl_stmt|;
specifier|public
name|RestoreTablesClient
parameter_list|(
name|Connection
name|conn
parameter_list|,
name|RestoreRequest
name|request
parameter_list|)
block|{
name|this
operator|.
name|targetRootDir
operator|=
name|request
operator|.
name|getBackupRootDir
argument_list|()
expr_stmt|;
name|this
operator|.
name|backupId
operator|=
name|request
operator|.
name|getBackupId
argument_list|()
expr_stmt|;
name|this
operator|.
name|sTableArray
operator|=
name|request
operator|.
name|getFromTables
argument_list|()
expr_stmt|;
name|this
operator|.
name|tTableArray
operator|=
name|request
operator|.
name|getToTables
argument_list|()
expr_stmt|;
if|if
condition|(
name|tTableArray
operator|==
literal|null
operator|||
name|tTableArray
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|this
operator|.
name|tTableArray
operator|=
name|sTableArray
expr_stmt|;
block|}
name|this
operator|.
name|isOverwrite
operator|=
name|request
operator|.
name|isOverwrite
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
block|}
comment|/**    * Validate target tables.    *    * @param tTableArray: target tables    * @param isOverwrite overwrite existing table    * @throws IOException exception    */
specifier|private
name|void
name|checkTargetTables
parameter_list|(
name|TableName
index|[]
name|tTableArray
parameter_list|,
name|boolean
name|isOverwrite
parameter_list|)
throws|throws
name|IOException
block|{
name|ArrayList
argument_list|<
name|TableName
argument_list|>
name|existTableList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|TableName
argument_list|>
name|disabledTableList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// check if the tables already exist
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
for|for
control|(
name|TableName
name|tableName
range|:
name|tTableArray
control|)
block|{
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|existTableList
operator|.
name|add
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
if|if
condition|(
name|admin
operator|.
name|isTableDisabled
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|disabledTableList
operator|.
name|add
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"HBase table "
operator|+
name|tableName
operator|+
literal|" does not exist. It will be created during restore process"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|existTableList
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
if|if
condition|(
operator|!
name|isOverwrite
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Existing table ("
operator|+
name|existTableList
operator|+
literal|") found in the restore target, please add "
operator|+
literal|"\"-o\" as overwrite option in the command if you mean"
operator|+
literal|" to restore to these existing tables"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Existing table found in target while no \"-o\" "
operator|+
literal|"as overwrite option found"
argument_list|)
throw|;
block|}
else|else
block|{
if|if
condition|(
name|disabledTableList
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Found offline table in the restore target, "
operator|+
literal|"please enable them before restore with \"-overwrite\" option"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Offline table list in restore target: "
operator|+
name|disabledTableList
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Found offline table in the target when restore with \"-overwrite\" option"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
comment|/**    * Restore operation handle each backupImage in array.    *    * @param images: array BackupImage    * @param sTable: table to be restored    * @param tTable: table to be restored to    * @param truncateIfExists: truncate table    * @throws IOException exception    */
specifier|private
name|void
name|restoreImages
parameter_list|(
name|BackupImage
index|[]
name|images
parameter_list|,
name|TableName
name|sTable
parameter_list|,
name|TableName
name|tTable
parameter_list|,
name|boolean
name|truncateIfExists
parameter_list|)
throws|throws
name|IOException
block|{
comment|// First image MUST be image of a FULL backup
name|BackupImage
name|image
init|=
name|images
index|[
literal|0
index|]
decl_stmt|;
name|String
name|rootDir
init|=
name|image
operator|.
name|getRootDir
argument_list|()
decl_stmt|;
name|String
name|backupId
init|=
name|image
operator|.
name|getBackupId
argument_list|()
decl_stmt|;
name|Path
name|backupRoot
init|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|)
decl_stmt|;
name|RestoreTool
name|restoreTool
init|=
operator|new
name|RestoreTool
argument_list|(
name|conf
argument_list|,
name|backupRoot
argument_list|,
name|backupId
argument_list|)
decl_stmt|;
name|Path
name|tableBackupPath
init|=
name|HBackupFileSystem
operator|.
name|getTableBackupPath
argument_list|(
name|sTable
argument_list|,
name|backupRoot
argument_list|,
name|backupId
argument_list|)
decl_stmt|;
name|String
name|lastIncrBackupId
init|=
name|images
operator|.
name|length
operator|==
literal|1
condition|?
literal|null
else|:
name|images
index|[
name|images
operator|.
name|length
operator|-
literal|1
index|]
operator|.
name|getBackupId
argument_list|()
decl_stmt|;
comment|// We need hFS only for full restore (see the code)
name|BackupManifest
name|manifest
init|=
name|HBackupFileSystem
operator|.
name|getManifest
argument_list|(
name|conf
argument_list|,
name|backupRoot
argument_list|,
name|backupId
argument_list|)
decl_stmt|;
if|if
condition|(
name|manifest
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
name|info
argument_list|(
literal|"Restoring '"
operator|+
name|sTable
operator|+
literal|"' to '"
operator|+
name|tTable
operator|+
literal|"' from full"
operator|+
literal|" backup image "
operator|+
name|tableBackupPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|JOB_NAME_CONF_KEY
argument_list|,
literal|"Full_Restore-"
operator|+
name|backupId
operator|+
literal|"-"
operator|+
name|tTable
argument_list|)
expr_stmt|;
name|restoreTool
operator|.
name|fullRestoreTable
argument_list|(
name|conn
argument_list|,
name|tableBackupPath
argument_list|,
name|sTable
argument_list|,
name|tTable
argument_list|,
name|truncateIfExists
argument_list|,
name|lastIncrBackupId
argument_list|)
expr_stmt|;
name|conf
operator|.
name|unset
argument_list|(
name|JOB_NAME_CONF_KEY
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// incremental Backup
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unexpected backup type "
operator|+
name|image
operator|.
name|getType
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|images
operator|.
name|length
operator|==
literal|1
condition|)
block|{
comment|// full backup restore done
return|return;
block|}
name|List
argument_list|<
name|Path
argument_list|>
name|dirList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// add full backup path
comment|// full backup path comes first
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|images
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|BackupImage
name|im
init|=
name|images
index|[
name|i
index|]
decl_stmt|;
name|String
name|fileBackupDir
init|=
name|HBackupFileSystem
operator|.
name|getTableBackupDir
argument_list|(
name|im
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|im
operator|.
name|getBackupId
argument_list|()
argument_list|,
name|sTable
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|list
init|=
name|getFilesRecursively
argument_list|(
name|fileBackupDir
argument_list|)
decl_stmt|;
name|dirList
operator|.
name|addAll
argument_list|(
name|list
argument_list|)
expr_stmt|;
block|}
name|String
name|dirs
init|=
name|StringUtils
operator|.
name|join
argument_list|(
name|dirList
argument_list|,
literal|","
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Restoring '"
operator|+
name|sTable
operator|+
literal|"' to '"
operator|+
name|tTable
operator|+
literal|"' from log dirs: "
operator|+
name|dirs
argument_list|)
expr_stmt|;
name|Path
index|[]
name|paths
init|=
operator|new
name|Path
index|[
name|dirList
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|dirList
operator|.
name|toArray
argument_list|(
name|paths
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|JOB_NAME_CONF_KEY
argument_list|,
literal|"Incremental_Restore-"
operator|+
name|backupId
operator|+
literal|"-"
operator|+
name|tTable
argument_list|)
expr_stmt|;
name|restoreTool
operator|.
name|incrementalRestoreTable
argument_list|(
name|conn
argument_list|,
name|tableBackupPath
argument_list|,
name|paths
argument_list|,
operator|new
name|TableName
index|[]
block|{
name|sTable
block|}
argument_list|,
operator|new
name|TableName
index|[]
block|{
name|tTable
block|}
argument_list|,
name|lastIncrBackupId
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|sTable
operator|+
literal|" has been successfully restored to "
operator|+
name|tTable
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|Path
argument_list|>
name|getFilesRecursively
parameter_list|(
name|String
name|fileBackupDir
parameter_list|)
throws|throws
name|IllegalArgumentException
throws|,
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
operator|(
operator|new
name|Path
argument_list|(
name|fileBackupDir
argument_list|)
operator|)
operator|.
name|toUri
argument_list|()
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|RemoteIterator
argument_list|<
name|LocatedFileStatus
argument_list|>
name|it
init|=
name|fs
operator|.
name|listFiles
argument_list|(
operator|new
name|Path
argument_list|(
name|fileBackupDir
argument_list|)
argument_list|,
literal|true
argument_list|)
decl_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Path
name|p
init|=
name|it
operator|.
name|next
argument_list|()
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|HFile
operator|.
name|isHFileFormat
argument_list|(
name|fs
argument_list|,
name|p
argument_list|)
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|list
return|;
block|}
comment|/**    * Restore operation. Stage 2: resolved Backup Image dependency    * @param backupManifestMap : tableName, Manifest    * @param sTableArray The array of tables to be restored    * @param tTableArray The array of mapping tables to restore to    * @throws IOException exception    */
specifier|private
name|void
name|restore
parameter_list|(
name|HashMap
argument_list|<
name|TableName
argument_list|,
name|BackupManifest
argument_list|>
name|backupManifestMap
parameter_list|,
name|TableName
index|[]
name|sTableArray
parameter_list|,
name|TableName
index|[]
name|tTableArray
parameter_list|,
name|boolean
name|isOverwrite
parameter_list|)
throws|throws
name|IOException
block|{
name|TreeSet
argument_list|<
name|BackupImage
argument_list|>
name|restoreImageSet
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|()
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
name|sTableArray
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|TableName
name|table
init|=
name|sTableArray
index|[
name|i
index|]
decl_stmt|;
name|BackupManifest
name|manifest
init|=
name|backupManifestMap
operator|.
name|get
argument_list|(
name|table
argument_list|)
decl_stmt|;
comment|// Get the image list of this backup for restore in time order from old
comment|// to new.
name|List
argument_list|<
name|BackupImage
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|manifest
operator|.
name|getBackupImage
argument_list|()
argument_list|)
expr_stmt|;
name|TreeSet
argument_list|<
name|BackupImage
argument_list|>
name|set
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|(
name|list
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|BackupImage
argument_list|>
name|depList
init|=
name|manifest
operator|.
name|getDependentListByTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|set
operator|.
name|addAll
argument_list|(
name|depList
argument_list|)
expr_stmt|;
name|BackupImage
index|[]
name|arr
init|=
operator|new
name|BackupImage
index|[
name|set
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|set
operator|.
name|toArray
argument_list|(
name|arr
argument_list|)
expr_stmt|;
name|restoreImages
argument_list|(
name|arr
argument_list|,
name|table
argument_list|,
name|tTableArray
index|[
name|i
index|]
argument_list|,
name|isOverwrite
argument_list|)
expr_stmt|;
name|restoreImageSet
operator|.
name|addAll
argument_list|(
name|list
argument_list|)
expr_stmt|;
if|if
condition|(
name|restoreImageSet
operator|!=
literal|null
operator|&&
operator|!
name|restoreImageSet
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Restore includes the following image(s):"
argument_list|)
expr_stmt|;
for|for
control|(
name|BackupImage
name|image
range|:
name|restoreImageSet
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Backup: "
operator|+
name|image
operator|.
name|getBackupId
argument_list|()
operator|+
literal|" "
operator|+
name|HBackupFileSystem
operator|.
name|getTableBackupDir
argument_list|(
name|image
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|image
operator|.
name|getBackupId
argument_list|()
argument_list|,
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"restoreStage finished"
argument_list|)
expr_stmt|;
block|}
specifier|static
name|long
name|getTsFromBackupId
parameter_list|(
name|String
name|backupId
parameter_list|)
block|{
if|if
condition|(
name|backupId
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|backupId
operator|.
name|substring
argument_list|(
name|backupId
operator|.
name|lastIndexOf
argument_list|(
literal|"_"
argument_list|)
operator|+
literal|1
argument_list|)
argument_list|)
return|;
block|}
specifier|static
name|boolean
name|withinRange
parameter_list|(
name|long
name|a
parameter_list|,
name|long
name|lower
parameter_list|,
name|long
name|upper
parameter_list|)
block|{
return|return
name|a
operator|>=
name|lower
operator|&&
name|a
operator|<=
name|upper
return|;
block|}
specifier|public
name|void
name|execute
parameter_list|()
throws|throws
name|IOException
block|{
comment|// case VALIDATION:
comment|// check the target tables
name|checkTargetTables
argument_list|(
name|tTableArray
argument_list|,
name|isOverwrite
argument_list|)
expr_stmt|;
comment|// case RESTORE_IMAGES:
name|HashMap
argument_list|<
name|TableName
argument_list|,
name|BackupManifest
argument_list|>
name|backupManifestMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// check and load backup image manifest for the tables
name|Path
name|rootPath
init|=
operator|new
name|Path
argument_list|(
name|targetRootDir
argument_list|)
decl_stmt|;
name|HBackupFileSystem
operator|.
name|checkImageManifestExist
argument_list|(
name|backupManifestMap
argument_list|,
name|sTableArray
argument_list|,
name|conf
argument_list|,
name|rootPath
argument_list|,
name|backupId
argument_list|)
expr_stmt|;
name|restore
argument_list|(
name|backupManifestMap
argument_list|,
name|sTableArray
argument_list|,
name|tTableArray
argument_list|,
name|isOverwrite
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

