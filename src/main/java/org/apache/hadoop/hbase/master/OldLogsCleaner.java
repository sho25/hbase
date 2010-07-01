begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|master
package|;
end_package

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
name|Chore
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
name|RemoteExceptionHandler
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
name|regionserver
operator|.
name|wal
operator|.
name|HLog
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_comment
comment|/**  * This Chore, everytime it runs, will clear the logs in the old logs folder  * that are older than hbase.master.logcleaner.ttl and, in order to limit the  * number of deletes it sends, will only delete maximum 20 in a single run.  */
end_comment

begin_class
specifier|public
class|class
name|OldLogsCleaner
extends|extends
name|Chore
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|OldLogsCleaner
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// Max number we can delete on every chore, this is to make sure we don't
comment|// issue thousands of delete commands around the same time
specifier|private
specifier|final
name|int
name|maxDeletedLogs
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|final
name|Path
name|oldLogDir
decl_stmt|;
specifier|private
specifier|final
name|LogCleanerDelegate
name|logCleaner
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
comment|/**    *    * @param p the period of time to sleep between each run    * @param s the stopper boolean    * @param conf configuration to use    * @param fs handle to the FS    * @param oldLogDir the path to the archived logs    */
specifier|public
name|OldLogsCleaner
parameter_list|(
specifier|final
name|int
name|p
parameter_list|,
specifier|final
name|AtomicBoolean
name|s
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|oldLogDir
parameter_list|)
block|{
name|super
argument_list|(
literal|"OldLogsCleaner"
argument_list|,
name|p
argument_list|,
name|s
argument_list|)
expr_stmt|;
comment|// Use the log cleaner provided by replication if enabled, unless something
comment|// was already provided
if|if
condition|(
name|conf
operator|.
name|getBoolean
argument_list|(
name|HConstants
operator|.
name|REPLICATION_ENABLE_KEY
argument_list|,
literal|false
argument_list|)
operator|&&
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.master.logcleanerplugin.impl"
argument_list|)
operator|==
literal|null
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.master.logcleanerplugin.impl"
argument_list|,
literal|"org.apache.hadoop.hbase.replication.master.ReplicationLogCleaner"
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|maxDeletedLogs
operator|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.master.logcleaner.maxdeletedlogs"
argument_list|,
literal|20
argument_list|)
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|oldLogDir
operator|=
name|oldLogDir
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|logCleaner
operator|=
name|getLogCleaner
argument_list|()
expr_stmt|;
block|}
specifier|private
name|LogCleanerDelegate
name|getLogCleaner
parameter_list|()
block|{
try|try
block|{
name|Class
name|c
init|=
name|Class
operator|.
name|forName
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.master.logcleanerplugin.impl"
argument_list|,
name|TimeToLiveLogCleaner
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|LogCleanerDelegate
name|cleaner
init|=
operator|(
name|LogCleanerDelegate
operator|)
name|c
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|cleaner
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
return|return
name|cleaner
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
literal|"Passed log cleaner implementation throws errors, "
operator|+
literal|"defaulting to TimeToLiveLogCleaner"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
operator|new
name|TimeToLiveLogCleaner
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|chore
parameter_list|()
block|{
try|try
block|{
name|FileStatus
index|[]
name|files
init|=
name|this
operator|.
name|fs
operator|.
name|listStatus
argument_list|(
name|this
operator|.
name|oldLogDir
argument_list|)
decl_stmt|;
name|int
name|nbDeletedLog
init|=
literal|0
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
name|Path
name|filePath
init|=
name|file
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|HLog
operator|.
name|validateHLogFilename
argument_list|(
name|filePath
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|logCleaner
operator|.
name|isLogDeletable
argument_list|(
name|filePath
argument_list|)
condition|)
block|{
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|filePath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|nbDeletedLog
operator|++
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Found a wrongly formated file: "
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
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|filePath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|nbDeletedLog
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|nbDeletedLog
operator|>=
name|maxDeletedLogs
condition|)
block|{
break|break;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|=
name|RemoteExceptionHandler
operator|.
name|checkIOException
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error while cleaning the logs"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

