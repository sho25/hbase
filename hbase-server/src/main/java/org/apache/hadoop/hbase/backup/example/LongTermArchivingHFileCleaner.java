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
name|example
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
name|master
operator|.
name|cleaner
operator|.
name|BaseHFileCleanerDelegate
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
name|TimeToLiveHFileCleaner
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
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_comment
comment|/**  * {@link BaseHFileCleanerDelegate} that only cleans HFiles that don't belong to a table that is  * currently being archived.  *<p>  * This only works properly if the {@link TimeToLiveHFileCleaner} is also enabled (it always should  * be), since it may take a little time for the ZK notification to propagate, in which case we may  * accidentally delete some files.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|LongTermArchivingHFileCleaner
extends|extends
name|BaseHFileCleanerDelegate
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
name|LongTermArchivingHFileCleaner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|TableHFileArchiveTracker
name|archiveTracker
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|isFileDeleteable
parameter_list|(
name|Path
name|file
parameter_list|)
block|{
try|try
block|{
name|FileStatus
index|[]
name|deleteStatus
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|file
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// if the file doesn't exist, then it can be deleted (but should never
comment|// happen since deleted files shouldn't get passed in)
if|if
condition|(
name|deleteStatus
operator|==
literal|null
condition|)
return|return
literal|true
return|;
comment|// if its a directory with stuff in it, don't delete
if|if
condition|(
name|deleteStatus
operator|.
name|length
operator|>
literal|1
condition|)
return|return
literal|false
return|;
comment|// if its an empty directory, we can definitely delete
if|if
condition|(
name|deleteStatus
index|[
literal|0
index|]
operator|.
name|isDir
argument_list|()
condition|)
return|return
literal|true
return|;
comment|// otherwise, we need to check the file's table and see its being archived
name|Path
name|family
init|=
name|file
operator|.
name|getParent
argument_list|()
decl_stmt|;
name|Path
name|region
init|=
name|family
operator|.
name|getParent
argument_list|()
decl_stmt|;
name|Path
name|table
init|=
name|region
operator|.
name|getParent
argument_list|()
decl_stmt|;
name|String
name|tableName
init|=
name|table
operator|.
name|getName
argument_list|()
decl_stmt|;
return|return
operator|!
name|archiveTracker
operator|.
name|keepHFiles
argument_list|(
name|tableName
argument_list|)
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
literal|"Failed to lookup status of:"
operator|+
name|file
operator|+
literal|", keeping it just incase."
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
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
comment|// setup our own zookeeper connection
comment|// Make my own Configuration. Then I'll have my own connection to zk that
comment|// I can close myself when comes time.
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|super
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|archiveTracker
operator|=
name|TableHFileArchiveTracker
operator|.
name|create
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|archiveTracker
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error while configuring "
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
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
literal|"Error while configuring "
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
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
name|stop
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|isStopped
argument_list|()
condition|)
return|return;
name|super
operator|.
name|stop
argument_list|(
name|reason
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|archiveTracker
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping "
operator|+
name|this
operator|.
name|archiveTracker
argument_list|)
expr_stmt|;
name|this
operator|.
name|archiveTracker
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

