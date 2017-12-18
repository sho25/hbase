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
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
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
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|BackupCopyJob
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
name|BackupRestoreFactory
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
name|mapreduce
operator|.
name|MapReduceBackupCopyJob
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
name|mapreduce
operator|.
name|WALPlayer
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
name|Bytes
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
name|util
operator|.
name|HFileArchiveUtil
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|wal
operator|.
name|AbstractFSWALProvider
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
name|Tool
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
comment|/**  * Incremental backup implementation.  * See the {@link #execute() execute} method.  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|IncrementalTableBackupClient
extends|extends
name|TableBackupClient
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
name|IncrementalTableBackupClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|IncrementalTableBackupClient
parameter_list|()
block|{   }
specifier|public
name|IncrementalTableBackupClient
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
name|super
argument_list|(
name|conn
argument_list|,
name|backupId
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|List
argument_list|<
name|String
argument_list|>
name|filterMissingFiles
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|incrBackupFileList
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|file
range|:
name|incrBackupFileList
control|)
block|{
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|file
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|p
argument_list|)
operator|||
name|isActiveWalPath
argument_list|(
name|p
argument_list|)
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't find file: "
operator|+
name|file
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|list
return|;
block|}
comment|/**    * Check if a given path is belongs to active WAL directory    * @param p path    * @return true, if yes    */
specifier|protected
name|boolean
name|isActiveWalPath
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
return|return
operator|!
name|AbstractFSWALProvider
operator|.
name|isArchivedLogFile
argument_list|(
name|p
argument_list|)
return|;
block|}
specifier|protected
specifier|static
name|int
name|getIndex
parameter_list|(
name|TableName
name|tbl
parameter_list|,
name|List
argument_list|<
name|TableName
argument_list|>
name|sTableList
parameter_list|)
block|{
if|if
condition|(
name|sTableList
operator|==
literal|null
condition|)
return|return
literal|0
return|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|sTableList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|tbl
operator|.
name|equals
argument_list|(
name|sTableList
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
condition|)
block|{
return|return
name|i
return|;
block|}
block|}
return|return
operator|-
literal|1
return|;
block|}
comment|/*    * Reads bulk load records from backup table, iterates through the records and forms the paths    * for bulk loaded hfiles. Copies the bulk loaded hfiles to backup destination    * @param sTableList list of tables to be backed up    * @return map of table to List of files    */
specifier|protected
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
name|handleBulkLoad
parameter_list|(
name|List
argument_list|<
name|TableName
argument_list|>
name|sTableList
parameter_list|)
throws|throws
name|IOException
block|{
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
name|mapForSrc
init|=
operator|new
name|Map
index|[
name|sTableList
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|activeFiles
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|archiveFiles
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
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
name|pair
init|=
name|backupManager
operator|.
name|readBulkloadRows
argument_list|(
name|sTableList
argument_list|)
decl_stmt|;
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
name|map
init|=
name|pair
operator|.
name|getFirst
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileSystem
name|tgtFs
decl_stmt|;
try|try
block|{
name|tgtFs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
operator|new
name|URI
argument_list|(
name|backupInfo
operator|.
name|getBackupRootDir
argument_list|()
argument_list|)
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|use
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to get FileSystem"
argument_list|,
name|use
argument_list|)
throw|;
block|}
name|Path
name|rootdir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|tgtRoot
init|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|backupInfo
operator|.
name|getBackupRootDir
argument_list|()
argument_list|)
argument_list|,
name|backupId
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
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
name|tblEntry
range|:
name|map
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|TableName
name|srcTable
init|=
name|tblEntry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|int
name|srcIdx
init|=
name|getIndex
argument_list|(
name|srcTable
argument_list|,
name|sTableList
argument_list|)
decl_stmt|;
if|if
condition|(
name|srcIdx
operator|<
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Couldn't find "
operator|+
name|srcTable
operator|+
literal|" in source table List"
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|mapForSrc
index|[
name|srcIdx
index|]
operator|==
literal|null
condition|)
block|{
name|mapForSrc
index|[
name|srcIdx
index|]
operator|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Path
argument_list|>
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
expr_stmt|;
block|}
name|Path
name|tblDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|rootdir
argument_list|,
name|srcTable
argument_list|)
decl_stmt|;
name|Path
name|tgtTable
init|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|tgtRoot
argument_list|,
name|srcTable
operator|.
name|getNamespaceAsString
argument_list|()
argument_list|)
argument_list|,
name|srcTable
operator|.
name|getQualifierAsString
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
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
name|regionEntry
range|:
name|tblEntry
operator|.
name|getValue
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|regionName
init|=
name|regionEntry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Path
name|regionDir
init|=
operator|new
name|Path
argument_list|(
name|tblDir
argument_list|,
name|regionName
argument_list|)
decl_stmt|;
comment|// map from family to List of hfiles
for|for
control|(
name|Map
operator|.
name|Entry
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
name|famEntry
range|:
name|regionEntry
operator|.
name|getValue
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|fam
init|=
name|famEntry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Path
name|famDir
init|=
operator|new
name|Path
argument_list|(
name|regionDir
argument_list|,
name|fam
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|files
decl_stmt|;
if|if
condition|(
operator|!
name|mapForSrc
index|[
name|srcIdx
index|]
operator|.
name|containsKey
argument_list|(
name|fam
operator|.
name|getBytes
argument_list|()
argument_list|)
condition|)
block|{
name|files
operator|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
expr_stmt|;
name|mapForSrc
index|[
name|srcIdx
index|]
operator|.
name|put
argument_list|(
name|fam
operator|.
name|getBytes
argument_list|()
argument_list|,
name|files
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|files
operator|=
name|mapForSrc
index|[
name|srcIdx
index|]
operator|.
name|get
argument_list|(
name|fam
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Path
name|archiveDir
init|=
name|HFileArchiveUtil
operator|.
name|getStoreArchivePath
argument_list|(
name|conf
argument_list|,
name|srcTable
argument_list|,
name|regionName
argument_list|,
name|fam
argument_list|)
decl_stmt|;
name|String
name|tblName
init|=
name|srcTable
operator|.
name|getQualifierAsString
argument_list|()
decl_stmt|;
name|Path
name|tgtFam
init|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|tgtTable
argument_list|,
name|regionName
argument_list|)
argument_list|,
name|fam
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|tgtFs
operator|.
name|mkdirs
argument_list|(
name|tgtFam
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"couldn't create "
operator|+
name|tgtFam
argument_list|)
throw|;
block|}
for|for
control|(
name|Pair
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
name|fileWithState
range|:
name|famEntry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|String
name|file
init|=
name|fileWithState
operator|.
name|getFirst
argument_list|()
decl_stmt|;
name|int
name|idx
init|=
name|file
operator|.
name|lastIndexOf
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
name|String
name|filename
init|=
name|file
decl_stmt|;
if|if
condition|(
name|idx
operator|>
literal|0
condition|)
block|{
name|filename
operator|=
name|file
operator|.
name|substring
argument_list|(
name|idx
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|famDir
argument_list|,
name|filename
argument_list|)
decl_stmt|;
name|Path
name|tgt
init|=
operator|new
name|Path
argument_list|(
name|tgtFam
argument_list|,
name|filename
argument_list|)
decl_stmt|;
name|Path
name|archive
init|=
operator|new
name|Path
argument_list|(
name|archiveDir
argument_list|,
name|filename
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|p
argument_list|)
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"found bulk hfile "
operator|+
name|file
operator|+
literal|" in "
operator|+
name|famDir
operator|+
literal|" for "
operator|+
name|tblName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"copying "
operator|+
name|p
operator|+
literal|" to "
operator|+
name|tgt
argument_list|)
expr_stmt|;
block|}
name|activeFiles
operator|.
name|add
argument_list|(
name|p
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|archive
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"copying archive "
operator|+
name|archive
operator|+
literal|" to "
operator|+
name|tgt
argument_list|)
expr_stmt|;
name|archiveFiles
operator|.
name|add
argument_list|(
name|archive
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|files
operator|.
name|add
argument_list|(
name|tgt
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|copyBulkLoadedFiles
argument_list|(
name|activeFiles
argument_list|,
name|archiveFiles
argument_list|)
expr_stmt|;
name|backupManager
operator|.
name|writeBulkLoadedFiles
argument_list|(
name|sTableList
argument_list|,
name|mapForSrc
argument_list|)
expr_stmt|;
name|backupManager
operator|.
name|removeBulkLoadedRows
argument_list|(
name|sTableList
argument_list|,
name|pair
operator|.
name|getSecond
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|mapForSrc
return|;
block|}
specifier|private
name|void
name|copyBulkLoadedFiles
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|activeFiles
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|archiveFiles
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
comment|// Enable special mode of BackupDistCp
name|conf
operator|.
name|setInt
argument_list|(
name|MapReduceBackupCopyJob
operator|.
name|NUMBER_OF_LEVELS_TO_PRESERVE_KEY
argument_list|,
literal|5
argument_list|)
expr_stmt|;
comment|// Copy active files
name|String
name|tgtDest
init|=
name|backupInfo
operator|.
name|getBackupRootDir
argument_list|()
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|backupInfo
operator|.
name|getBackupId
argument_list|()
decl_stmt|;
if|if
condition|(
name|activeFiles
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|String
index|[]
name|toCopy
init|=
operator|new
name|String
index|[
name|activeFiles
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|activeFiles
operator|.
name|toArray
argument_list|(
name|toCopy
argument_list|)
expr_stmt|;
name|incrementalCopyHFiles
argument_list|(
name|toCopy
argument_list|,
name|tgtDest
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|archiveFiles
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|String
index|[]
name|toCopy
init|=
operator|new
name|String
index|[
name|archiveFiles
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|archiveFiles
operator|.
name|toArray
argument_list|(
name|toCopy
argument_list|)
expr_stmt|;
name|incrementalCopyHFiles
argument_list|(
name|toCopy
argument_list|,
name|tgtDest
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
comment|// Disable special mode of BackupDistCp
name|conf
operator|.
name|unset
argument_list|(
name|MapReduceBackupCopyJob
operator|.
name|NUMBER_OF_LEVELS_TO_PRESERVE_KEY
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|execute
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
comment|// case PREPARE_INCREMENTAL:
name|beginBackup
argument_list|(
name|backupManager
argument_list|,
name|backupInfo
argument_list|)
expr_stmt|;
name|backupInfo
operator|.
name|setPhase
argument_list|(
name|BackupPhase
operator|.
name|PREPARE_INCREMENTAL
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"For incremental backup, current table set is "
operator|+
name|backupManager
operator|.
name|getIncrementalBackupTableSet
argument_list|()
argument_list|)
expr_stmt|;
name|newTimestamps
operator|=
operator|(
operator|(
name|IncrementalBackupManager
operator|)
name|backupManager
operator|)
operator|.
name|getIncrBackupLogFileMap
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// fail the overall backup and return
name|failBackup
argument_list|(
name|conn
argument_list|,
name|backupInfo
argument_list|,
name|backupManager
argument_list|,
name|e
argument_list|,
literal|"Unexpected Exception : "
argument_list|,
name|BackupType
operator|.
name|INCREMENTAL
argument_list|,
name|conf
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|// case INCREMENTAL_COPY:
try|try
block|{
comment|// copy out the table and region info files for each table
name|BackupUtils
operator|.
name|copyTableRegionInfo
argument_list|(
name|conn
argument_list|,
name|backupInfo
argument_list|,
name|conf
argument_list|)
expr_stmt|;
comment|// convert WAL to HFiles and copy them to .tmp under BACKUP_ROOT
name|convertWALsToHFiles
argument_list|()
expr_stmt|;
name|incrementalCopyHFiles
argument_list|(
operator|new
name|String
index|[]
block|{
name|getBulkOutputDir
argument_list|()
operator|.
name|toString
argument_list|()
block|}
argument_list|,
name|backupInfo
operator|.
name|getBackupRootDir
argument_list|()
argument_list|)
expr_stmt|;
comment|// Save list of WAL files copied
name|backupManager
operator|.
name|recordWALFiles
argument_list|(
name|backupInfo
operator|.
name|getIncrBackupFileList
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"Unexpected exception in incremental-backup: incremental copy "
operator|+
name|backupId
decl_stmt|;
comment|// fail the overall backup and return
name|failBackup
argument_list|(
name|conn
argument_list|,
name|backupInfo
argument_list|,
name|backupManager
argument_list|,
name|e
argument_list|,
name|msg
argument_list|,
name|BackupType
operator|.
name|INCREMENTAL
argument_list|,
name|conf
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|// case INCR_BACKUP_COMPLETE:
comment|// set overall backup status: complete. Here we make sure to complete the backup.
comment|// After this checkpoint, even if entering cancel process, will let the backup finished
try|try
block|{
comment|// Set the previousTimestampMap which is before this current log roll to the manifest.
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
name|previousTimestampMap
init|=
name|backupManager
operator|.
name|readLogTimestampMap
argument_list|()
decl_stmt|;
name|backupInfo
operator|.
name|setIncrTimestampMap
argument_list|(
name|previousTimestampMap
argument_list|)
expr_stmt|;
comment|// The table list in backupInfo is good for both full backup and incremental backup.
comment|// For incremental backup, it contains the incremental backup table set.
name|backupManager
operator|.
name|writeRegionServerLogTimestamp
argument_list|(
name|backupInfo
operator|.
name|getTables
argument_list|()
argument_list|,
name|newTimestamps
argument_list|)
expr_stmt|;
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
name|newTableSetTimestampMap
init|=
name|backupManager
operator|.
name|readLogTimestampMap
argument_list|()
decl_stmt|;
name|Long
name|newStartCode
init|=
name|BackupUtils
operator|.
name|getMinValue
argument_list|(
name|BackupUtils
operator|.
name|getRSLogTimestampMins
argument_list|(
name|newTableSetTimestampMap
argument_list|)
argument_list|)
decl_stmt|;
name|backupManager
operator|.
name|writeBackupStartCode
argument_list|(
name|newStartCode
argument_list|)
expr_stmt|;
name|handleBulkLoad
argument_list|(
name|backupInfo
operator|.
name|getTableNames
argument_list|()
argument_list|)
expr_stmt|;
comment|// backup complete
name|completeBackup
argument_list|(
name|conn
argument_list|,
name|backupInfo
argument_list|,
name|backupManager
argument_list|,
name|BackupType
operator|.
name|INCREMENTAL
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|failBackup
argument_list|(
name|conn
argument_list|,
name|backupInfo
argument_list|,
name|backupManager
argument_list|,
name|e
argument_list|,
literal|"Unexpected Exception : "
argument_list|,
name|BackupType
operator|.
name|INCREMENTAL
argument_list|,
name|conf
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|void
name|incrementalCopyHFiles
parameter_list|(
name|String
index|[]
name|files
parameter_list|,
name|String
name|backupDest
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Incremental copy HFiles is starting. dest="
operator|+
name|backupDest
argument_list|)
expr_stmt|;
comment|// set overall backup phase: incremental_copy
name|backupInfo
operator|.
name|setPhase
argument_list|(
name|BackupPhase
operator|.
name|INCREMENTAL_COPY
argument_list|)
expr_stmt|;
comment|// get incremental backup file list and prepare parms for DistCp
name|String
index|[]
name|strArr
init|=
operator|new
name|String
index|[
name|files
operator|.
name|length
operator|+
literal|1
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|files
argument_list|,
literal|0
argument_list|,
name|strArr
argument_list|,
literal|0
argument_list|,
name|files
operator|.
name|length
argument_list|)
expr_stmt|;
name|strArr
index|[
name|strArr
operator|.
name|length
operator|-
literal|1
index|]
operator|=
name|backupDest
expr_stmt|;
name|String
name|jobname
init|=
literal|"Incremental_Backup-HFileCopy-"
operator|+
name|backupInfo
operator|.
name|getBackupId
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
literal|"Setting incremental copy HFiles job name to : "
operator|+
name|jobname
argument_list|)
expr_stmt|;
block|}
name|conf
operator|.
name|set
argument_list|(
name|JOB_NAME_CONF_KEY
argument_list|,
name|jobname
argument_list|)
expr_stmt|;
name|BackupCopyJob
name|copyService
init|=
name|BackupRestoreFactory
operator|.
name|getBackupCopyJob
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|int
name|res
init|=
name|copyService
operator|.
name|copy
argument_list|(
name|backupInfo
argument_list|,
name|backupManager
argument_list|,
name|conf
argument_list|,
name|BackupType
operator|.
name|INCREMENTAL
argument_list|,
name|strArr
argument_list|)
decl_stmt|;
if|if
condition|(
name|res
operator|!=
literal|0
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Copy incremental HFile files failed with return code: "
operator|+
name|res
operator|+
literal|"."
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed copy from "
operator|+
name|StringUtils
operator|.
name|join
argument_list|(
name|files
argument_list|,
literal|','
argument_list|)
operator|+
literal|" to "
operator|+
name|backupDest
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Incremental copy HFiles from "
operator|+
name|StringUtils
operator|.
name|join
argument_list|(
name|files
argument_list|,
literal|','
argument_list|)
operator|+
literal|" to "
operator|+
name|backupDest
operator|+
literal|" finished."
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|deleteBulkLoadDirectory
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|deleteBulkLoadDirectory
parameter_list|()
throws|throws
name|IOException
block|{
comment|// delete original bulk load directory on method exit
name|Path
name|path
init|=
name|getBulkOutputDir
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|boolean
name|result
init|=
name|fs
operator|.
name|delete
argument_list|(
name|path
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|result
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Could not delete "
operator|+
name|path
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|convertWALsToHFiles
parameter_list|()
throws|throws
name|IOException
block|{
comment|// get incremental backup file list and prepare parameters for DistCp
name|List
argument_list|<
name|String
argument_list|>
name|incrBackupFileList
init|=
name|backupInfo
operator|.
name|getIncrBackupFileList
argument_list|()
decl_stmt|;
comment|// Get list of tables in incremental backup set
name|Set
argument_list|<
name|TableName
argument_list|>
name|tableSet
init|=
name|backupManager
operator|.
name|getIncrementalBackupTableSet
argument_list|()
decl_stmt|;
comment|// filter missing files out (they have been copied by previous backups)
name|incrBackupFileList
operator|=
name|filterMissingFiles
argument_list|(
name|incrBackupFileList
argument_list|)
expr_stmt|;
for|for
control|(
name|TableName
name|table
range|:
name|tableSet
control|)
block|{
comment|// Check if table exists
if|if
condition|(
name|tableExists
argument_list|(
name|table
argument_list|,
name|conn
argument_list|)
condition|)
block|{
name|walToHFiles
argument_list|(
name|incrBackupFileList
argument_list|,
name|table
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Table "
operator|+
name|table
operator|+
literal|" does not exists. Skipping in WAL converter"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|protected
name|boolean
name|tableExists
parameter_list|(
name|TableName
name|table
parameter_list|,
name|Connection
name|conn
parameter_list|)
throws|throws
name|IOException
block|{
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
return|return
name|admin
operator|.
name|tableExists
argument_list|(
name|table
argument_list|)
return|;
block|}
block|}
specifier|protected
name|void
name|walToHFiles
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|dirPaths
parameter_list|,
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|Tool
name|player
init|=
operator|new
name|WALPlayer
argument_list|()
decl_stmt|;
comment|// Player reads all files in arbitrary directory structure and creates
comment|// a Map task for each file. We use ';' as separator
comment|// because WAL file names contains ','
name|String
name|dirs
init|=
name|StringUtils
operator|.
name|join
argument_list|(
name|dirPaths
argument_list|,
literal|';'
argument_list|)
decl_stmt|;
name|String
name|jobname
init|=
literal|"Incremental_Backup-"
operator|+
name|backupId
operator|+
literal|"-"
operator|+
name|tableName
operator|.
name|getNameAsString
argument_list|()
decl_stmt|;
name|Path
name|bulkOutputPath
init|=
name|getBulkOutputDirForTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|WALPlayer
operator|.
name|BULK_OUTPUT_CONF_KEY
argument_list|,
name|bulkOutputPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|WALPlayer
operator|.
name|INPUT_FILES_SEPARATOR_KEY
argument_list|,
literal|";"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|JOB_NAME_CONF_KEY
argument_list|,
name|jobname
argument_list|)
expr_stmt|;
name|String
index|[]
name|playerArgs
init|=
block|{
name|dirs
block|,
name|tableName
operator|.
name|getNameAsString
argument_list|()
block|}
decl_stmt|;
try|try
block|{
name|player
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|int
name|result
init|=
name|player
operator|.
name|run
argument_list|(
name|playerArgs
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"WAL Player failed"
argument_list|)
throw|;
block|}
name|conf
operator|.
name|unset
argument_list|(
name|WALPlayer
operator|.
name|INPUT_FILES_SEPARATOR_KEY
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
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ee
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Can not convert from directory "
operator|+
name|dirs
operator|+
literal|" (check Hadoop, HBase and WALPlayer M/R job logs) "
argument_list|,
name|ee
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|Path
name|getBulkOutputDirForTable
parameter_list|(
name|TableName
name|table
parameter_list|)
block|{
name|Path
name|tablePath
init|=
name|getBulkOutputDir
argument_list|()
decl_stmt|;
name|tablePath
operator|=
operator|new
name|Path
argument_list|(
name|tablePath
argument_list|,
name|table
operator|.
name|getNamespaceAsString
argument_list|()
argument_list|)
expr_stmt|;
name|tablePath
operator|=
operator|new
name|Path
argument_list|(
name|tablePath
argument_list|,
name|table
operator|.
name|getQualifierAsString
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|Path
argument_list|(
name|tablePath
argument_list|,
literal|"data"
argument_list|)
return|;
block|}
specifier|protected
name|Path
name|getBulkOutputDir
parameter_list|()
block|{
name|String
name|backupId
init|=
name|backupInfo
operator|.
name|getBackupId
argument_list|()
decl_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|backupInfo
operator|.
name|getBackupRootDir
argument_list|()
argument_list|)
decl_stmt|;
name|path
operator|=
operator|new
name|Path
argument_list|(
name|path
argument_list|,
literal|".tmp"
argument_list|)
expr_stmt|;
name|path
operator|=
operator|new
name|Path
argument_list|(
name|path
argument_list|,
name|backupId
argument_list|)
expr_stmt|;
return|return
name|path
return|;
block|}
block|}
end_class

end_unit

