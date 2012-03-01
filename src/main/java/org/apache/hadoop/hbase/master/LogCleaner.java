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
name|LinkedList
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
name|Stoppable
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
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HConstants
operator|.
name|HBASE_MASTER_LOGCLEANER_PLUGINS
import|;
end_import

begin_comment
comment|/**  * This Chore, everytime it runs, will clear the HLogs in the old logs folder  * that are deletable for each log cleaner in the chain.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|LogCleaner
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
name|LogCleaner
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
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
name|List
argument_list|<
name|LogCleanerDelegate
argument_list|>
name|logCleanersChain
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
comment|/**    *    * @param p the period of time to sleep between each run    * @param s the stopper    * @param conf configuration to use    * @param fs handle to the FS    * @param oldLogDir the path to the archived logs    */
specifier|public
name|LogCleaner
parameter_list|(
specifier|final
name|int
name|p
parameter_list|,
specifier|final
name|Stoppable
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
literal|"LogsCleaner"
argument_list|,
name|p
argument_list|,
name|s
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
name|logCleanersChain
operator|=
operator|new
name|LinkedList
argument_list|<
name|LogCleanerDelegate
argument_list|>
argument_list|()
expr_stmt|;
name|initLogCleanersChain
argument_list|()
expr_stmt|;
block|}
comment|/*    * Initialize the chain of log cleaners from the configuration. The default    * in this chain are: TimeToLiveLogCleaner and ReplicationLogCleaner.    */
specifier|private
name|void
name|initLogCleanersChain
parameter_list|()
block|{
name|String
index|[]
name|logCleaners
init|=
name|conf
operator|.
name|getStrings
argument_list|(
name|HBASE_MASTER_LOGCLEANER_PLUGINS
argument_list|)
decl_stmt|;
if|if
condition|(
name|logCleaners
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|className
range|:
name|logCleaners
control|)
block|{
name|LogCleanerDelegate
name|logCleaner
init|=
name|newLogCleaner
argument_list|(
name|className
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|addLogCleaner
argument_list|(
name|logCleaner
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * A utility method to create new instances of LogCleanerDelegate based    * on the class name of the LogCleanerDelegate.    * @param className fully qualified class name of the LogCleanerDelegate    * @param conf    * @return the new instance    */
specifier|public
specifier|static
name|LogCleanerDelegate
name|newLogCleaner
parameter_list|(
name|String
name|className
parameter_list|,
name|Configuration
name|conf
parameter_list|)
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
name|className
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
literal|"Can NOT create LogCleanerDelegate: "
operator|+
name|className
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|// skipping if can't instantiate
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Add a LogCleanerDelegate to the log cleaner chain. A log file is deletable    * if it is deletable for each LogCleanerDelegate in the chain.    * @param logCleaner    */
specifier|public
name|void
name|addLogCleaner
parameter_list|(
name|LogCleanerDelegate
name|logCleaner
parameter_list|)
block|{
if|if
condition|(
name|logCleaner
operator|!=
literal|null
operator|&&
operator|!
name|logCleanersChain
operator|.
name|contains
argument_list|(
name|logCleaner
argument_list|)
condition|)
block|{
name|logCleanersChain
operator|.
name|add
argument_list|(
name|logCleaner
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Add log cleaner in chain: "
operator|+
name|logCleaner
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
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
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|this
operator|.
name|oldLogDir
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|files
operator|==
literal|null
condition|)
return|return;
name|FILE
label|:
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
for|for
control|(
name|LogCleanerDelegate
name|logCleaner
range|:
name|logCleanersChain
control|)
block|{
if|if
condition|(
name|logCleaner
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"A log cleaner is stopped, won't delete any log."
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
operator|!
name|logCleaner
operator|.
name|isLogDeletable
argument_list|(
name|filePath
argument_list|)
condition|)
block|{
comment|// this log is not deletable, continue to process next log file
continue|continue
name|FILE
continue|;
block|}
block|}
comment|// delete this log file if it passes all the log cleaners
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
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|super
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
for|for
control|(
name|LogCleanerDelegate
name|lc
range|:
name|this
operator|.
name|logCleanersChain
control|)
block|{
try|try
block|{
name|lc
operator|.
name|stop
argument_list|(
literal|"Exiting"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Stopping"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

