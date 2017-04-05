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
name|HashSet
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
name|fs
operator|.
name|PathFilter
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
name|BackupSystemTable
operator|.
name|WALItem
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
name|wal
operator|.
name|AbstractFSWALProvider
import|;
end_import

begin_comment
comment|/**  * After a full backup was created, the incremental backup will only store the changes made after  * the last full or incremental backup. Creating the backup copies the logfiles in .logs and  * .oldlogs since the last backup timestamp.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|IncrementalBackupManager
extends|extends
name|BackupManager
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|IncrementalBackupManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|IncrementalBackupManager
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
name|super
argument_list|(
name|conn
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Obtain the list of logs that need to be copied out for this incremental backup. The list is set    * in BackupInfo.    * @return The new HashMap of RS log time stamps after the log roll for this incremental backup.    * @throws IOException exception    */
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|getIncrBackupLogFileMap
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|logList
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|newTimestamps
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|previousTimestampMins
decl_stmt|;
name|String
name|savedStartCode
init|=
name|readBackupStartCode
argument_list|()
decl_stmt|;
comment|// key: tableName
comment|// value:<RegionServer,PreviousTimeStamp>
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
name|readLogTimestampMap
argument_list|()
decl_stmt|;
name|previousTimestampMins
operator|=
name|BackupUtils
operator|.
name|getRSLogTimestampMins
argument_list|(
name|previousTimestampMap
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
literal|"StartCode "
operator|+
name|savedStartCode
operator|+
literal|"for backupID "
operator|+
name|backupInfo
operator|.
name|getBackupId
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// get all new log files from .logs and .oldlogs after last TS and before new timestamp
if|if
condition|(
name|savedStartCode
operator|==
literal|null
operator|||
name|previousTimestampMins
operator|==
literal|null
operator|||
name|previousTimestampMins
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot read any previous back up timestamps from backup system table. "
operator|+
literal|"In order to create an incremental backup, at least one full backup is needed."
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Execute roll log procedure for incremental backup ..."
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|props
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|props
operator|.
name|put
argument_list|(
literal|"backupRoot"
argument_list|,
name|backupInfo
operator|.
name|getBackupRootDir
argument_list|()
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
name|execProcedure
argument_list|(
name|LogRollMasterProcedureManager
operator|.
name|ROLLLOG_PROCEDURE_SIGNATURE
argument_list|,
name|LogRollMasterProcedureManager
operator|.
name|ROLLLOG_PROCEDURE_NAME
argument_list|,
name|props
argument_list|)
expr_stmt|;
block|}
name|newTimestamps
operator|=
name|readRegionServerLastLogRollResult
argument_list|()
expr_stmt|;
name|logList
operator|=
name|getLogFilesForNewBackup
argument_list|(
name|previousTimestampMins
argument_list|,
name|newTimestamps
argument_list|,
name|conf
argument_list|,
name|savedStartCode
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|WALItem
argument_list|>
name|logFromSystemTable
init|=
name|getLogFilesFromBackupSystem
argument_list|(
name|previousTimestampMins
argument_list|,
name|newTimestamps
argument_list|,
name|getBackupInfo
argument_list|()
operator|.
name|getBackupRootDir
argument_list|()
argument_list|)
decl_stmt|;
name|logList
operator|=
name|excludeAlreadyBackedUpWALs
argument_list|(
name|logList
argument_list|,
name|logFromSystemTable
argument_list|)
expr_stmt|;
name|backupInfo
operator|.
name|setIncrBackupFileList
argument_list|(
name|logList
argument_list|)
expr_stmt|;
return|return
name|newTimestamps
return|;
block|}
comment|/**    * Get list of WAL files eligible for incremental backup    * @return list of WAL files    * @throws IOException    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getIncrBackupLogFileList
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|logList
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|newTimestamps
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|previousTimestampMins
decl_stmt|;
name|String
name|savedStartCode
init|=
name|readBackupStartCode
argument_list|()
decl_stmt|;
comment|// key: tableName
comment|// value:<RegionServer,PreviousTimeStamp>
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
name|readLogTimestampMap
argument_list|()
decl_stmt|;
name|previousTimestampMins
operator|=
name|BackupUtils
operator|.
name|getRSLogTimestampMins
argument_list|(
name|previousTimestampMap
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
literal|"StartCode "
operator|+
name|savedStartCode
operator|+
literal|"for backupID "
operator|+
name|backupInfo
operator|.
name|getBackupId
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// get all new log files from .logs and .oldlogs after last TS and before new timestamp
if|if
condition|(
name|savedStartCode
operator|==
literal|null
operator|||
name|previousTimestampMins
operator|==
literal|null
operator|||
name|previousTimestampMins
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot read any previous back up timestamps from backup system table. "
operator|+
literal|"In order to create an incremental backup, at least one full backup is needed."
argument_list|)
throw|;
block|}
name|newTimestamps
operator|=
name|readRegionServerLastLogRollResult
argument_list|()
expr_stmt|;
name|logList
operator|=
name|getLogFilesForNewBackup
argument_list|(
name|previousTimestampMins
argument_list|,
name|newTimestamps
argument_list|,
name|conf
argument_list|,
name|savedStartCode
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|WALItem
argument_list|>
name|logFromSystemTable
init|=
name|getLogFilesFromBackupSystem
argument_list|(
name|previousTimestampMins
argument_list|,
name|newTimestamps
argument_list|,
name|getBackupInfo
argument_list|()
operator|.
name|getBackupRootDir
argument_list|()
argument_list|)
decl_stmt|;
name|logList
operator|=
name|excludeAlreadyBackedUpWALs
argument_list|(
name|logList
argument_list|,
name|logFromSystemTable
argument_list|)
expr_stmt|;
name|backupInfo
operator|.
name|setIncrBackupFileList
argument_list|(
name|logList
argument_list|)
expr_stmt|;
return|return
name|logList
return|;
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|excludeAlreadyBackedUpWALs
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|logList
parameter_list|,
name|List
argument_list|<
name|WALItem
argument_list|>
name|logFromSystemTable
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|walFileNameSet
init|=
name|convertToSet
argument_list|(
name|logFromSystemTable
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
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|logList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|logList
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|name
init|=
name|p
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|walFileNameSet
operator|.
name|contains
argument_list|(
name|name
argument_list|)
condition|)
continue|continue;
name|list
operator|.
name|add
argument_list|(
name|logList
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|list
return|;
block|}
comment|/**    * Create Set of WAL file names (not full path names)    * @param logFromSystemTable    * @return set of WAL file names    */
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|convertToSet
parameter_list|(
name|List
argument_list|<
name|WALItem
argument_list|>
name|logFromSystemTable
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|set
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
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
name|logFromSystemTable
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|WALItem
name|item
init|=
name|logFromSystemTable
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|set
operator|.
name|add
argument_list|(
name|item
operator|.
name|walFile
argument_list|)
expr_stmt|;
block|}
return|return
name|set
return|;
block|}
comment|/**    * For each region server: get all log files newer than the last timestamps, but not newer than    * the newest timestamps.    * @param olderTimestamps timestamp map for each region server of the last backup.    * @param newestTimestamps timestamp map for each region server that the backup should lead to.    * @return list of log files which needs to be added to this backup    * @throws IOException    */
specifier|private
name|List
argument_list|<
name|WALItem
argument_list|>
name|getLogFilesFromBackupSystem
parameter_list|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|olderTimestamps
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|newestTimestamps
parameter_list|,
name|String
name|backupRoot
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|WALItem
argument_list|>
name|logFiles
init|=
operator|new
name|ArrayList
argument_list|<
name|WALItem
argument_list|>
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|WALItem
argument_list|>
name|it
init|=
name|getWALFilesFromBackupSystem
argument_list|()
decl_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|WALItem
name|item
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|String
name|rootDir
init|=
name|item
operator|.
name|getBackupRoot
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|rootDir
operator|.
name|equals
argument_list|(
name|backupRoot
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|String
name|walFileName
init|=
name|item
operator|.
name|getWalFile
argument_list|()
decl_stmt|;
name|String
name|server
init|=
name|BackupUtils
operator|.
name|parseHostNameFromLogFile
argument_list|(
operator|new
name|Path
argument_list|(
name|walFileName
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|server
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|Long
name|tss
init|=
name|getTimestamp
argument_list|(
name|walFileName
argument_list|)
decl_stmt|;
name|Long
name|oldTss
init|=
name|olderTimestamps
operator|.
name|get
argument_list|(
name|server
argument_list|)
decl_stmt|;
name|Long
name|newTss
init|=
name|newestTimestamps
operator|.
name|get
argument_list|(
name|server
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldTss
operator|==
literal|null
condition|)
block|{
name|logFiles
operator|.
name|add
argument_list|(
name|item
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|newTss
operator|==
literal|null
condition|)
block|{
name|newTss
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
if|if
condition|(
name|tss
operator|>
name|oldTss
operator|&&
name|tss
operator|<
name|newTss
condition|)
block|{
name|logFiles
operator|.
name|add
argument_list|(
name|item
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|logFiles
return|;
block|}
specifier|private
name|Long
name|getTimestamp
parameter_list|(
name|String
name|walFileName
parameter_list|)
block|{
name|int
name|index
init|=
name|walFileName
operator|.
name|lastIndexOf
argument_list|(
name|BackupUtils
operator|.
name|LOGNAME_SEPARATOR
argument_list|)
decl_stmt|;
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|walFileName
operator|.
name|substring
argument_list|(
name|index
operator|+
literal|1
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * For each region server: get all log files newer than the last timestamps but not newer than the    * newest timestamps.    * @param olderTimestamps the timestamp for each region server of the last backup.    * @param newestTimestamps the timestamp for each region server that the backup should lead to.    * @param conf the Hadoop and Hbase configuration    * @param savedStartCode the startcode (timestamp) of last successful backup.    * @return a list of log files to be backed up    * @throws IOException exception    */
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|getLogFilesForNewBackup
parameter_list|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|olderTimestamps
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|newestTimestamps
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|String
name|savedStartCode
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"In getLogFilesForNewBackup()\n"
operator|+
literal|"olderTimestamps: "
operator|+
name|olderTimestamps
operator|+
literal|"\n newestTimestamps: "
operator|+
name|newestTimestamps
argument_list|)
expr_stmt|;
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
name|logDir
init|=
operator|new
name|Path
argument_list|(
name|rootdir
argument_list|,
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
decl_stmt|;
name|Path
name|oldLogDir
init|=
operator|new
name|Path
argument_list|(
name|rootdir
argument_list|,
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|rootdir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|NewestLogFilter
name|pathFilter
init|=
operator|new
name|NewestLogFilter
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|resultLogFiles
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
name|newestLogs
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|/*      * The old region servers and timestamps info we kept in backup system table may be out of sync      * if new region server is added or existing one lost. We'll deal with it here when processing      * the logs. If data in backup system table has more hosts, just ignore it. If the .logs      * directory includes more hosts, the additional hosts will not have old timestamps to compare      * with. We'll just use all the logs in that directory. We always write up-to-date region server      * and timestamp info to backup system table at the end of successful backup.      */
name|FileStatus
index|[]
name|rss
decl_stmt|;
name|Path
name|p
decl_stmt|;
name|String
name|host
decl_stmt|;
name|Long
name|oldTimeStamp
decl_stmt|;
name|String
name|currentLogFile
decl_stmt|;
name|long
name|currentLogTS
decl_stmt|;
comment|// Get the files in .logs.
name|rss
operator|=
name|fs
operator|.
name|listStatus
argument_list|(
name|logDir
argument_list|)
expr_stmt|;
for|for
control|(
name|FileStatus
name|rs
range|:
name|rss
control|)
block|{
name|p
operator|=
name|rs
operator|.
name|getPath
argument_list|()
expr_stmt|;
name|host
operator|=
name|BackupUtils
operator|.
name|parseHostNameFromLogFile
argument_list|(
name|p
argument_list|)
expr_stmt|;
if|if
condition|(
name|host
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|FileStatus
index|[]
name|logs
decl_stmt|;
name|oldTimeStamp
operator|=
name|olderTimestamps
operator|.
name|get
argument_list|(
name|host
argument_list|)
expr_stmt|;
comment|// It is possible that there is no old timestamp in backup system table for this host if
comment|// this region server is newly added after our last backup.
if|if
condition|(
name|oldTimeStamp
operator|==
literal|null
condition|)
block|{
name|logs
operator|=
name|fs
operator|.
name|listStatus
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|pathFilter
operator|.
name|setLastBackupTS
argument_list|(
name|oldTimeStamp
argument_list|)
expr_stmt|;
name|logs
operator|=
name|fs
operator|.
name|listStatus
argument_list|(
name|p
argument_list|,
name|pathFilter
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|FileStatus
name|log
range|:
name|logs
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"currentLogFile: "
operator|+
name|log
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|AbstractFSWALProvider
operator|.
name|isMetaFile
argument_list|(
name|log
operator|.
name|getPath
argument_list|()
argument_list|)
condition|)
block|{
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
literal|"Skip hbase:meta log file: "
operator|+
name|log
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
continue|continue;
block|}
name|currentLogFile
operator|=
name|log
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
name|resultLogFiles
operator|.
name|add
argument_list|(
name|currentLogFile
argument_list|)
expr_stmt|;
name|currentLogTS
operator|=
name|BackupUtils
operator|.
name|getCreationTime
argument_list|(
name|log
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
comment|// newestTimestamps is up-to-date with the current list of hosts
comment|// so newestTimestamps.get(host) will not be null.
if|if
condition|(
name|currentLogTS
operator|>
name|newestTimestamps
operator|.
name|get
argument_list|(
name|host
argument_list|)
condition|)
block|{
name|newestLogs
operator|.
name|add
argument_list|(
name|currentLogFile
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Include the .oldlogs files too.
name|FileStatus
index|[]
name|oldlogs
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|oldLogDir
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|oldlog
range|:
name|oldlogs
control|)
block|{
name|p
operator|=
name|oldlog
operator|.
name|getPath
argument_list|()
expr_stmt|;
name|currentLogFile
operator|=
name|p
operator|.
name|toString
argument_list|()
expr_stmt|;
if|if
condition|(
name|AbstractFSWALProvider
operator|.
name|isMetaFile
argument_list|(
name|p
argument_list|)
condition|)
block|{
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
literal|"Skip .meta log file: "
operator|+
name|currentLogFile
argument_list|)
expr_stmt|;
block|}
continue|continue;
block|}
name|host
operator|=
name|BackupUtils
operator|.
name|parseHostFromOldLog
argument_list|(
name|p
argument_list|)
expr_stmt|;
if|if
condition|(
name|host
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|currentLogTS
operator|=
name|BackupUtils
operator|.
name|getCreationTime
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|oldTimeStamp
operator|=
name|olderTimestamps
operator|.
name|get
argument_list|(
name|host
argument_list|)
expr_stmt|;
comment|/*        * It is possible that there is no old timestamp in backup system table for this host. At the        * time of our last backup operation, this rs did not exist. The reason can be one of the two:        * 1. The rs already left/crashed. Its logs were moved to .oldlogs. 2. The rs was added after        * our last backup.        */
if|if
condition|(
name|oldTimeStamp
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|currentLogTS
operator|<
name|Long
operator|.
name|parseLong
argument_list|(
name|savedStartCode
argument_list|)
condition|)
block|{
comment|// This log file is really old, its region server was before our last backup.
continue|continue;
block|}
else|else
block|{
name|resultLogFiles
operator|.
name|add
argument_list|(
name|currentLogFile
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|currentLogTS
operator|>
name|oldTimeStamp
condition|)
block|{
name|resultLogFiles
operator|.
name|add
argument_list|(
name|currentLogFile
argument_list|)
expr_stmt|;
block|}
comment|// It is possible that a host in .oldlogs is an obsolete region server
comment|// so newestTimestamps.get(host) here can be null.
comment|// Even if these logs belong to a obsolete region server, we still need
comment|// to include they to avoid loss of edits for backup.
name|Long
name|newTimestamp
init|=
name|newestTimestamps
operator|.
name|get
argument_list|(
name|host
argument_list|)
decl_stmt|;
if|if
condition|(
name|newTimestamp
operator|!=
literal|null
operator|&&
name|currentLogTS
operator|>
name|newTimestamp
condition|)
block|{
name|newestLogs
operator|.
name|add
argument_list|(
name|currentLogFile
argument_list|)
expr_stmt|;
block|}
block|}
comment|// remove newest log per host because they are still in use
name|resultLogFiles
operator|.
name|removeAll
argument_list|(
name|newestLogs
argument_list|)
expr_stmt|;
return|return
name|resultLogFiles
return|;
block|}
specifier|static
class|class
name|NewestLogFilter
implements|implements
name|PathFilter
block|{
specifier|private
name|Long
name|lastBackupTS
init|=
literal|0L
decl_stmt|;
specifier|public
name|NewestLogFilter
parameter_list|()
block|{     }
specifier|protected
name|void
name|setLastBackupTS
parameter_list|(
name|Long
name|ts
parameter_list|)
block|{
name|this
operator|.
name|lastBackupTS
operator|=
name|ts
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
comment|// skip meta table log -- ts.meta file
if|if
condition|(
name|AbstractFSWALProvider
operator|.
name|isMetaFile
argument_list|(
name|path
argument_list|)
condition|)
block|{
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
literal|"Skip .meta log file: "
operator|+
name|path
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
name|long
name|timestamp
decl_stmt|;
try|try
block|{
name|timestamp
operator|=
name|BackupUtils
operator|.
name|getCreationTime
argument_list|(
name|path
argument_list|)
expr_stmt|;
return|return
name|timestamp
operator|>
name|lastBackupTS
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Cannot read timestamp of log file "
operator|+
name|path
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

