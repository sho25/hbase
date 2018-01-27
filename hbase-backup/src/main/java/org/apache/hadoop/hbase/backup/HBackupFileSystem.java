begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements. See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License. You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HashMap
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
name|BackupManifest
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
comment|/**  * View to an on-disk Backup Image FileSytem Provides the set of methods necessary to interact with  * the on-disk Backup Image data.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|HBackupFileSystem
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HBackupFileSystem
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * This is utility class.    */
specifier|private
name|HBackupFileSystem
parameter_list|()
block|{   }
comment|/**    * Given the backup root dir, backup id and the table name, return the backup image location,    * which is also where the backup manifest file is. return value look like:    * "hdfs://backup.hbase.org:9000/user/biadmin/backup/backup_1396650096738/default/t1_dn/", where    * "hdfs://backup.hbase.org:9000/user/biadmin/backup" is a backup root directory    * @param backupRootDir backup root directory    * @param backupId backup id    * @param tableName table name    * @return backupPath String for the particular table    */
specifier|public
specifier|static
name|String
name|getTableBackupDir
parameter_list|(
name|String
name|backupRootDir
parameter_list|,
name|String
name|backupId
parameter_list|,
name|TableName
name|tableName
parameter_list|)
block|{
return|return
name|backupRootDir
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|backupId
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|tableName
operator|.
name|getNamespaceAsString
argument_list|()
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|tableName
operator|.
name|getQualifierAsString
argument_list|()
operator|+
name|Path
operator|.
name|SEPARATOR
return|;
block|}
specifier|public
specifier|static
name|String
name|getTableBackupDataDir
parameter_list|(
name|String
name|backupRootDir
parameter_list|,
name|String
name|backupId
parameter_list|,
name|TableName
name|tableName
parameter_list|)
block|{
return|return
name|getTableBackupDir
argument_list|(
name|backupRootDir
argument_list|,
name|backupId
argument_list|,
name|tableName
argument_list|)
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
literal|"data"
return|;
block|}
specifier|public
specifier|static
name|Path
name|getBackupPath
parameter_list|(
name|String
name|backupRootDir
parameter_list|,
name|String
name|backupId
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|backupRootDir
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|backupId
argument_list|)
return|;
block|}
comment|/**    * Given the backup root dir, backup id and the table name, return the backup image location,    * which is also where the backup manifest file is. return value look like:    * "hdfs://backup.hbase.org:9000/user/biadmin/backup/backup_1396650096738/default/t1_dn/", where    * "hdfs://backup.hbase.org:9000/user/biadmin/backup" is a backup root directory    * @param backupRootPath backup root path    * @param tableName table name    * @param backupId backup Id    * @return backupPath for the particular table    */
specifier|public
specifier|static
name|Path
name|getTableBackupPath
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|Path
name|backupRootPath
parameter_list|,
name|String
name|backupId
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|getTableBackupDir
argument_list|(
name|backupRootPath
operator|.
name|toString
argument_list|()
argument_list|,
name|backupId
argument_list|,
name|tableName
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Given the backup root dir and the backup id, return the log file location for an incremental    * backup.    * @param backupRootDir backup root directory    * @param backupId backup id    * @return logBackupDir: ".../user/biadmin/backup/WALs/backup_1396650096738"    */
specifier|public
specifier|static
name|String
name|getLogBackupDir
parameter_list|(
name|String
name|backupRootDir
parameter_list|,
name|String
name|backupId
parameter_list|)
block|{
return|return
name|backupRootDir
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|backupId
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
return|;
block|}
specifier|public
specifier|static
name|Path
name|getLogBackupPath
parameter_list|(
name|String
name|backupRootDir
parameter_list|,
name|String
name|backupId
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|getLogBackupDir
argument_list|(
name|backupRootDir
argument_list|,
name|backupId
argument_list|)
argument_list|)
return|;
block|}
comment|// TODO we do not keep WAL files anymore
comment|// Move manifest file to other place
specifier|private
specifier|static
name|Path
name|getManifestPath
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Path
name|backupRootPath
parameter_list|,
name|String
name|backupId
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|backupRootPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|manifestPath
init|=
operator|new
name|Path
argument_list|(
name|getBackupPath
argument_list|(
name|backupRootPath
operator|.
name|toString
argument_list|()
argument_list|,
name|backupId
argument_list|)
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|BackupManifest
operator|.
name|MANIFEST_FILE_NAME
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|manifestPath
argument_list|)
condition|)
block|{
name|String
name|errorMsg
init|=
literal|"Could not find backup manifest "
operator|+
name|BackupManifest
operator|.
name|MANIFEST_FILE_NAME
operator|+
literal|" for "
operator|+
name|backupId
operator|+
literal|". File "
operator|+
name|manifestPath
operator|+
literal|" does not exists. Did "
operator|+
name|backupId
operator|+
literal|" correspond to previously taken backup ?"
decl_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|errorMsg
argument_list|)
throw|;
block|}
return|return
name|manifestPath
return|;
block|}
specifier|public
specifier|static
name|BackupManifest
name|getManifest
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Path
name|backupRootPath
parameter_list|,
name|String
name|backupId
parameter_list|)
throws|throws
name|IOException
block|{
name|BackupManifest
name|manifest
init|=
operator|new
name|BackupManifest
argument_list|(
name|conf
argument_list|,
name|getManifestPath
argument_list|(
name|conf
argument_list|,
name|backupRootPath
argument_list|,
name|backupId
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|manifest
return|;
block|}
comment|/**    * Check whether the backup image path and there is manifest file in the path.    * @param backupManifestMap If all the manifests are found, then they are put into this map    * @param tableArray the tables involved    * @throws IOException exception    */
specifier|public
specifier|static
name|void
name|checkImageManifestExist
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
name|tableArray
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|Path
name|backupRootPath
parameter_list|,
name|String
name|backupId
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|TableName
name|tableName
range|:
name|tableArray
control|)
block|{
name|BackupManifest
name|manifest
init|=
name|getManifest
argument_list|(
name|conf
argument_list|,
name|backupRootPath
argument_list|,
name|backupId
argument_list|)
decl_stmt|;
name|backupManifestMap
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|manifest
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

