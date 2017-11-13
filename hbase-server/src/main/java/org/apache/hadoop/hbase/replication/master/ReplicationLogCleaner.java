begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|replication
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
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
name|hbase
operator|.
name|zookeeper
operator|.
name|ZKWatcher
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
name|Abortable
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
name|master
operator|.
name|cleaner
operator|.
name|BaseLogCleanerDelegate
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
name|replication
operator|.
name|ReplicationFactory
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
name|replication
operator|.
name|ReplicationQueuesClient
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
name|replication
operator|.
name|ReplicationQueuesClientArguments
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
name|Collections
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Predicate
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterables
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
name|EnvironmentEdgeManager
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
comment|/**  * Implementation of a log cleaner that checks if a log is still scheduled for  * replication before deleting it when its TTL is over.  */
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
name|ReplicationLogCleaner
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
name|ReplicationLogCleaner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ZKWatcher
name|zkw
decl_stmt|;
specifier|private
name|ReplicationQueuesClient
name|replicationQueues
decl_stmt|;
specifier|private
name|boolean
name|stopped
init|=
literal|false
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|wals
decl_stmt|;
specifier|private
name|long
name|readZKTimestamp
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|preClean
parameter_list|()
block|{
name|readZKTimestamp
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
expr_stmt|;
try|try
block|{
comment|// The concurrently created new WALs may not be included in the return list,
comment|// but they won't be deleted because they're not in the checking set.
name|wals
operator|=
name|replicationQueues
operator|.
name|getAllWALs
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
name|warn
argument_list|(
literal|"Failed to read zookeeper, skipping checking deletable files"
argument_list|)
expr_stmt|;
name|wals
operator|=
literal|null
expr_stmt|;
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
comment|// all members of this class are null if replication is disabled,
comment|// so we cannot filter the files
if|if
condition|(
name|this
operator|.
name|getConf
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
name|files
return|;
block|}
if|if
condition|(
name|wals
operator|==
literal|null
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
return|return
name|Iterables
operator|.
name|filter
argument_list|(
name|files
argument_list|,
operator|new
name|Predicate
argument_list|<
name|FileStatus
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|FileStatus
name|file
parameter_list|)
block|{
name|String
name|wal
init|=
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|boolean
name|logInReplicationQueue
init|=
name|wals
operator|.
name|contains
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
name|logInReplicationQueue
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found log in ZK, keeping: "
operator|+
name|wal
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Didn't find this log in ZK, deleting: "
operator|+
name|wal
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|!
name|logInReplicationQueue
operator|&&
operator|(
name|file
operator|.
name|getModificationTime
argument_list|()
operator|<
name|readZKTimestamp
operator|)
return|;
block|}
block|}
argument_list|)
return|;
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
comment|// Make my own Configuration.  Then I'll have my own connection to zk that
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
try|try
block|{
name|setConf
argument_list|(
name|conf
argument_list|,
operator|new
name|ZKWatcher
argument_list|(
name|conf
argument_list|,
literal|"replicationLogCleaner"
argument_list|,
literal|null
argument_list|)
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
name|VisibleForTesting
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ZKWatcher
name|zk
parameter_list|)
block|{
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
name|zkw
operator|=
name|zk
expr_stmt|;
name|this
operator|.
name|replicationQueues
operator|=
name|ReplicationFactory
operator|.
name|getReplicationQueuesClient
argument_list|(
operator|new
name|ReplicationQueuesClientArguments
argument_list|(
name|conf
argument_list|,
operator|new
name|WarnOnlyAbortable
argument_list|()
argument_list|,
name|zkw
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|replicationQueues
operator|.
name|init
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
name|why
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|stopped
condition|)
return|return;
name|this
operator|.
name|stopped
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|zkw
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
name|zkw
argument_list|)
expr_stmt|;
name|this
operator|.
name|zkw
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
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
specifier|private
specifier|static
class|class
name|WarnOnlyAbortable
implements|implements
name|Abortable
block|{
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"ReplicationLogCleaner received abort, ignoring.  Reason: "
operator|+
name|why
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
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

