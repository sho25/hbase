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
name|OPTION_CHECK
import|;
end_import

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
name|OPTION_CHECK_DESC
import|;
end_import

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
name|OPTION_DEBUG
import|;
end_import

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
name|OPTION_DEBUG_DESC
import|;
end_import

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
name|OPTION_OVERWRITE
import|;
end_import

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
name|OPTION_OVERWRITE_DESC
import|;
end_import

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
name|OPTION_SET
import|;
end_import

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
name|OPTION_SET_RESTORE_DESC
import|;
end_import

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
name|OPTION_TABLE
import|;
end_import

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
name|OPTION_TABLE_LIST_DESC
import|;
end_import

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
name|OPTION_TABLE_MAPPING
import|;
end_import

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
name|OPTION_TABLE_MAPPING_DESC
import|;
end_import

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
name|OPTION_YARN_QUEUE_NAME
import|;
end_import

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
name|OPTION_YARN_QUEUE_NAME_RESTORE_DESC
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
name|cli
operator|.
name|CommandLine
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
name|cli
operator|.
name|HelpFormatter
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
name|HBaseConfiguration
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
name|BackupAdminImpl
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
name|util
operator|.
name|AbstractHBaseTool
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
name|util
operator|.
name|ToolRunner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Level
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_comment
comment|/**  *  * Command-line entry point for restore operation  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RestoreDriver
extends|extends
name|AbstractHBaseTool
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
name|RestoreDriver
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|CommandLine
name|cmd
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|USAGE_STRING
init|=
literal|"Usage: hbase restore<backup_path><backup_id> [options]\n"
operator|+
literal|"  backup_path     Path to a backup destination root\n"
operator|+
literal|"  backup_id       Backup image ID to restore\n"
operator|+
literal|"  table(s)        Comma-separated list of tables to restore\n"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|USAGE_FOOTER
init|=
literal|""
decl_stmt|;
specifier|protected
name|RestoreDriver
parameter_list|()
throws|throws
name|IOException
block|{
name|init
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|init
parameter_list|()
throws|throws
name|IOException
block|{
comment|// disable irrelevant loggers to avoid it mess up command output
name|LogUtils
operator|.
name|disableZkAndClientLoggers
argument_list|(
name|LOG
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|parseAndRun
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Check if backup is enabled
if|if
condition|(
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
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|BackupRestoreConstants
operator|.
name|ENABLE_BACKUP
argument_list|)
expr_stmt|;
return|return
operator|-
literal|1
return|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|BackupRestoreConstants
operator|.
name|VERIFY_BACKUP
argument_list|)
expr_stmt|;
comment|// enable debug logging
name|Logger
name|backupClientLogger
init|=
name|Logger
operator|.
name|getLogger
argument_list|(
literal|"org.apache.hadoop.hbase.backup"
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|OPTION_DEBUG
argument_list|)
condition|)
block|{
name|backupClientLogger
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|DEBUG
argument_list|)
expr_stmt|;
block|}
comment|// whether to overwrite to existing table if any, false by default
name|boolean
name|overwrite
init|=
name|cmd
operator|.
name|hasOption
argument_list|(
name|OPTION_OVERWRITE
argument_list|)
decl_stmt|;
if|if
condition|(
name|overwrite
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found -overwrite option in restore command, "
operator|+
literal|"will overwrite to existing table if any in the restore target"
argument_list|)
expr_stmt|;
block|}
comment|// whether to only check the dependencies, false by default
name|boolean
name|check
init|=
name|cmd
operator|.
name|hasOption
argument_list|(
name|OPTION_CHECK
argument_list|)
decl_stmt|;
if|if
condition|(
name|check
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found -check option in restore command, "
operator|+
literal|"will check and verify the dependencies"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|OPTION_SET
argument_list|)
operator|&&
name|cmd
operator|.
name|hasOption
argument_list|(
name|OPTION_TABLE
argument_list|)
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Options -s and -t are mutaully exclusive,"
operator|+
literal|" you can not specify both of them."
argument_list|)
expr_stmt|;
name|printToolUsage
argument_list|()
expr_stmt|;
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
operator|!
name|cmd
operator|.
name|hasOption
argument_list|(
name|OPTION_SET
argument_list|)
operator|&&
operator|!
name|cmd
operator|.
name|hasOption
argument_list|(
name|OPTION_TABLE
argument_list|)
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"You have to specify either set name or table list to restore"
argument_list|)
expr_stmt|;
name|printToolUsage
argument_list|()
expr_stmt|;
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|OPTION_YARN_QUEUE_NAME
argument_list|)
condition|)
block|{
name|String
name|queueName
init|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|OPTION_YARN_QUEUE_NAME
argument_list|)
decl_stmt|;
comment|// Set system property value for MR job
name|System
operator|.
name|setProperty
argument_list|(
literal|"mapreduce.job.queuename"
argument_list|,
name|queueName
argument_list|)
expr_stmt|;
block|}
comment|// parse main restore command options
name|String
index|[]
name|remainArgs
init|=
name|cmd
operator|.
name|getArgs
argument_list|()
decl_stmt|;
if|if
condition|(
name|remainArgs
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
name|printToolUsage
argument_list|()
expr_stmt|;
return|return
operator|-
literal|1
return|;
block|}
name|String
name|backupRootDir
init|=
name|remainArgs
index|[
literal|0
index|]
decl_stmt|;
name|String
name|backupId
init|=
name|remainArgs
index|[
literal|1
index|]
decl_stmt|;
name|String
name|tables
init|=
literal|null
decl_stmt|;
name|String
name|tableMapping
init|=
name|cmd
operator|.
name|hasOption
argument_list|(
name|OPTION_TABLE_MAPPING
argument_list|)
condition|?
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|OPTION_TABLE_MAPPING
argument_list|)
else|:
literal|null
decl_stmt|;
try|try
init|(
specifier|final
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|;
name|BackupAdmin
name|client
operator|=
operator|new
name|BackupAdminImpl
argument_list|(
name|conn
argument_list|)
init|;
init|)
block|{
comment|// Check backup set
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|OPTION_SET
argument_list|)
condition|)
block|{
name|String
name|setName
init|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|OPTION_SET
argument_list|)
decl_stmt|;
try|try
block|{
name|tables
operator|=
name|getTablesForSet
argument_list|(
name|conn
argument_list|,
name|setName
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
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"ERROR: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
operator|+
literal|" for setName="
operator|+
name|setName
argument_list|)
expr_stmt|;
name|printToolUsage
argument_list|()
expr_stmt|;
return|return
operator|-
literal|2
return|;
block|}
if|if
condition|(
name|tables
operator|==
literal|null
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"ERROR: Backup set '"
operator|+
name|setName
operator|+
literal|"' is either empty or does not exist"
argument_list|)
expr_stmt|;
name|printToolUsage
argument_list|()
expr_stmt|;
return|return
operator|-
literal|3
return|;
block|}
block|}
else|else
block|{
name|tables
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|OPTION_TABLE
argument_list|)
expr_stmt|;
block|}
name|TableName
index|[]
name|sTableArray
init|=
name|BackupUtils
operator|.
name|parseTableNames
argument_list|(
name|tables
argument_list|)
decl_stmt|;
name|TableName
index|[]
name|tTableArray
init|=
name|BackupUtils
operator|.
name|parseTableNames
argument_list|(
name|tableMapping
argument_list|)
decl_stmt|;
if|if
condition|(
name|sTableArray
operator|!=
literal|null
operator|&&
name|tTableArray
operator|!=
literal|null
operator|&&
operator|(
name|sTableArray
operator|.
name|length
operator|!=
name|tTableArray
operator|.
name|length
operator|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"ERROR: table mapping mismatch: "
operator|+
name|tables
operator|+
literal|" : "
operator|+
name|tableMapping
argument_list|)
expr_stmt|;
name|printToolUsage
argument_list|()
expr_stmt|;
return|return
operator|-
literal|4
return|;
block|}
name|client
operator|.
name|restore
argument_list|(
name|BackupUtils
operator|.
name|createRestoreRequest
argument_list|(
name|backupRootDir
argument_list|,
name|backupId
argument_list|,
name|check
argument_list|,
name|sTableArray
argument_list|,
name|tTableArray
argument_list|,
name|overwrite
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
return|return
operator|-
literal|5
return|;
block|}
return|return
literal|0
return|;
block|}
specifier|private
name|String
name|getTablesForSet
parameter_list|(
name|Connection
name|conn
parameter_list|,
name|String
name|name
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
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
name|List
argument_list|<
name|TableName
argument_list|>
name|tables
init|=
name|table
operator|.
name|describeBackupSet
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|tables
operator|==
literal|null
condition|)
return|return
literal|null
return|;
return|return
name|StringUtils
operator|.
name|join
argument_list|(
name|tables
argument_list|,
name|BackupRestoreConstants
operator|.
name|TABLENAME_DELIMITER_IN_COMMAND
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|addOptions
parameter_list|()
block|{
comment|// define supported options
name|addOptNoArg
argument_list|(
name|OPTION_OVERWRITE
argument_list|,
name|OPTION_OVERWRITE_DESC
argument_list|)
expr_stmt|;
name|addOptNoArg
argument_list|(
name|OPTION_CHECK
argument_list|,
name|OPTION_CHECK_DESC
argument_list|)
expr_stmt|;
name|addOptNoArg
argument_list|(
name|OPTION_DEBUG
argument_list|,
name|OPTION_DEBUG_DESC
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|OPTION_SET
argument_list|,
name|OPTION_SET_RESTORE_DESC
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|OPTION_TABLE
argument_list|,
name|OPTION_TABLE_LIST_DESC
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|OPTION_TABLE_MAPPING
argument_list|,
name|OPTION_TABLE_MAPPING_DESC
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|OPTION_YARN_QUEUE_NAME
argument_list|,
name|OPTION_YARN_QUEUE_NAME_RESTORE_DESC
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|processOptions
parameter_list|(
name|CommandLine
name|cmd
parameter_list|)
block|{
name|this
operator|.
name|cmd
operator|=
name|cmd
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|doWork
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|parseAndRun
argument_list|(
name|cmd
operator|.
name|getArgs
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|Path
name|hbasedir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|URI
name|defaultFs
init|=
name|hbasedir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|getUri
argument_list|()
decl_stmt|;
name|FSUtils
operator|.
name|setFsDefault
argument_list|(
name|conf
argument_list|,
operator|new
name|Path
argument_list|(
name|defaultFs
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|ret
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|conf
argument_list|,
operator|new
name|RestoreDriver
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|ret
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Tool configuration is not initialized"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"conf"
argument_list|)
throw|;
block|}
name|CommandLine
name|cmd
decl_stmt|;
try|try
block|{
comment|// parse the command line arguments
name|cmd
operator|=
name|parseArgs
argument_list|(
name|args
argument_list|)
expr_stmt|;
name|cmdLineArgs
operator|=
name|args
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Error when parsing command-line arguments: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|printToolUsage
argument_list|()
expr_stmt|;
return|return
name|EXIT_FAILURE
return|;
block|}
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|SHORT_HELP_OPTION
argument_list|)
operator|||
name|cmd
operator|.
name|hasOption
argument_list|(
name|LONG_HELP_OPTION
argument_list|)
condition|)
block|{
name|printToolUsage
argument_list|()
expr_stmt|;
return|return
name|EXIT_FAILURE
return|;
block|}
name|processOptions
argument_list|(
name|cmd
argument_list|)
expr_stmt|;
name|int
name|ret
init|=
name|EXIT_FAILURE
decl_stmt|;
try|try
block|{
name|ret
operator|=
name|doWork
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
literal|"Error running command-line tool"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
name|EXIT_FAILURE
return|;
block|}
return|return
name|ret
return|;
block|}
specifier|protected
name|void
name|printToolUsage
parameter_list|()
throws|throws
name|IOException
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|USAGE_STRING
argument_list|)
expr_stmt|;
name|HelpFormatter
name|helpFormatter
init|=
operator|new
name|HelpFormatter
argument_list|()
decl_stmt|;
name|helpFormatter
operator|.
name|setLeftPadding
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|helpFormatter
operator|.
name|setDescPadding
argument_list|(
literal|8
argument_list|)
expr_stmt|;
name|helpFormatter
operator|.
name|setWidth
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|helpFormatter
operator|.
name|setSyntaxPrefix
argument_list|(
literal|"Options:"
argument_list|)
expr_stmt|;
name|helpFormatter
operator|.
name|printHelp
argument_list|(
literal|" "
argument_list|,
literal|null
argument_list|,
name|options
argument_list|,
name|USAGE_FOOTER
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

