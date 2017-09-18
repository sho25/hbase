begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|master
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
name|hbase
operator|.
name|HBaseInterfaceAudience
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
name|TableNotFoundException
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
name|impl
operator|.
name|BackupManager
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
name|client
operator|.
name|ConnectionFactory
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
name|master
operator|.
name|HMaster
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
name|master
operator|.
name|MasterServices
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
name|master
operator|.
name|cleaner
operator|.
name|BaseLogCleanerDelegate
import|;
end_import

begin_comment
comment|/**  * Implementation of a log cleaner that checks if a log is still scheduled for incremental backup  * before deleting it when its TTL is over.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|CONFIG
argument_list|)
specifier|public
class|class
name|BackupLogCleaner
extends|extends
name|BaseLogCleanerDelegate
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
name|BackupLogCleaner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|stopped
init|=
literal|false
decl_stmt|;
specifier|private
name|Connection
name|conn
decl_stmt|;
specifier|public
name|BackupLogCleaner
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{
if|if
condition|(
name|params
operator|!=
literal|null
operator|&&
name|params
operator|.
name|containsKey
argument_list|(
name|HMaster
operator|.
name|MASTER
argument_list|)
condition|)
block|{
name|MasterServices
name|master
init|=
operator|(
name|MasterServices
operator|)
name|params
operator|.
name|get
argument_list|(
name|HMaster
operator|.
name|MASTER
argument_list|)
decl_stmt|;
name|conn
operator|=
name|master
operator|.
name|getConnection
argument_list|()
expr_stmt|;
if|if
condition|(
name|getConf
argument_list|()
operator|==
literal|null
condition|)
block|{
name|super
operator|.
name|setConf
argument_list|(
name|conn
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|conn
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|conn
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to create connection"
argument_list|,
name|ioe
argument_list|)
throw|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|getDeletableFiles
parameter_list|(
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|files
parameter_list|)
block|{
comment|// all members of this class are null if backup is disabled,
comment|// so we cannot filter the files
if|if
condition|(
name|this
operator|.
name|getConf
argument_list|()
operator|==
literal|null
operator|||
operator|!
name|BackupManager
operator|.
name|isBackupEnabled
argument_list|(
name|getConf
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
literal|"Backup is not enabled. Check your "
operator|+
name|BackupRestoreConstants
operator|.
name|BACKUP_ENABLE_KEY
operator|+
literal|" setting"
argument_list|)
expr_stmt|;
block|}
return|return
name|files
return|;
block|}
name|List
argument_list|<
name|FileStatus
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|FileStatus
argument_list|>
argument_list|()
decl_stmt|;
try|try
init|(
specifier|final
name|BackupSystemTable
name|table
init|=
operator|new
name|BackupSystemTable
argument_list|(
name|conn
argument_list|)
init|)
block|{
comment|// If we do not have recorded backup sessions
try|try
block|{
if|if
condition|(
operator|!
name|table
operator|.
name|hasBackupSessions
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"BackupLogCleaner has no backup sessions"
argument_list|)
expr_stmt|;
return|return
name|files
return|;
block|}
block|}
catch|catch
parameter_list|(
name|TableNotFoundException
name|tnfe
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"backup system table is not available"
operator|+
name|tnfe
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|files
return|;
block|}
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
name|String
name|wal
init|=
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|boolean
name|logInSystemTable
init|=
name|table
operator|.
name|isWALFileDeletable
argument_list|(
name|wal
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
if|if
condition|(
name|logInSystemTable
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found log file in backup system table, deleting: "
operator|+
name|wal
argument_list|)
expr_stmt|;
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
name|debug
argument_list|(
literal|"Didn't find this log in backup system table, keeping: "
operator|+
name|wal
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|list
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to get backup system table table, therefore will keep all files"
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|// nothing to delete
return|return
operator|new
name|ArrayList
argument_list|<
name|FileStatus
argument_list|>
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|config
parameter_list|)
block|{
comment|// If backup is disabled, keep all members null
name|super
operator|.
name|setConf
argument_list|(
name|config
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|config
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
name|LOG
operator|.
name|warn
argument_list|(
literal|"Backup is disabled - allowing all wals to be deleted"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|stopped
condition|)
block|{
return|return;
block|}
name|this
operator|.
name|stopped
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping BackupLogCleaner"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
name|this
operator|.
name|stopped
return|;
block|}
block|}
end_class

end_unit

