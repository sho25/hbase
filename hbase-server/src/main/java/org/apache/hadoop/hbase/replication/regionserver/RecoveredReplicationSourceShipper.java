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
name|replication
operator|.
name|regionserver
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
name|concurrent
operator|.
name|PriorityBlockingQueue
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
name|replication
operator|.
name|ReplicationException
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
name|ReplicationQueueStorage
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
name|Threads
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
comment|/**  *  Used by a {@link RecoveredReplicationSource}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RecoveredReplicationSourceShipper
extends|extends
name|ReplicationSourceShipper
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
name|RecoveredReplicationSourceShipper
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|RecoveredReplicationSource
name|source
decl_stmt|;
specifier|private
specifier|final
name|ReplicationQueueStorage
name|replicationQueues
decl_stmt|;
specifier|public
name|RecoveredReplicationSourceShipper
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|walGroupId
parameter_list|,
name|PriorityBlockingQueue
argument_list|<
name|Path
argument_list|>
name|queue
parameter_list|,
name|RecoveredReplicationSource
name|source
parameter_list|,
name|ReplicationQueueStorage
name|queueStorage
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|walGroupId
argument_list|,
name|queue
argument_list|,
name|source
argument_list|)
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|replicationQueues
operator|=
name|queueStorage
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|noMoreData
parameter_list|()
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Finished recovering queue for group {} of peer {}"
argument_list|,
name|walGroupId
argument_list|,
name|source
operator|.
name|getQueueId
argument_list|()
argument_list|)
expr_stmt|;
name|source
operator|.
name|getSourceMetrics
argument_list|()
operator|.
name|incrCompletedRecoveryQueue
argument_list|()
expr_stmt|;
name|setWorkerState
argument_list|(
name|WorkerState
operator|.
name|FINISHED
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|postFinish
parameter_list|()
block|{
name|source
operator|.
name|tryFinish
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getStartPosition
parameter_list|()
block|{
name|long
name|startPosition
init|=
name|getRecoveredQueueStartPos
argument_list|()
decl_stmt|;
name|int
name|numRetries
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|numRetries
operator|<=
name|maxRetriesMultiplier
condition|)
block|{
try|try
block|{
name|source
operator|.
name|locateRecoveredPaths
argument_list|(
name|queue
argument_list|)
expr_stmt|;
break|break;
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
literal|"Error while locating recovered queue paths, attempt #"
operator|+
name|numRetries
argument_list|)
expr_stmt|;
name|numRetries
operator|++
expr_stmt|;
block|}
block|}
return|return
name|startPosition
return|;
block|}
comment|// If this is a recovered queue, the queue is already full and the first log
comment|// normally has a position (unless the RS failed between 2 logs)
specifier|private
name|long
name|getRecoveredQueueStartPos
parameter_list|()
block|{
name|long
name|startPosition
init|=
literal|0
decl_stmt|;
name|String
name|peerClusterZNode
init|=
name|source
operator|.
name|getQueueId
argument_list|()
decl_stmt|;
try|try
block|{
name|startPosition
operator|=
name|this
operator|.
name|replicationQueues
operator|.
name|getWALPosition
argument_list|(
name|source
operator|.
name|getServer
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|,
name|peerClusterZNode
argument_list|,
name|this
operator|.
name|queue
operator|.
name|peek
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"Recovered queue started with log {} at position {}"
argument_list|,
name|this
operator|.
name|queue
operator|.
name|peek
argument_list|()
argument_list|,
name|startPosition
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ReplicationException
name|e
parameter_list|)
block|{
name|terminate
argument_list|(
literal|"Couldn't get the position of this recovered queue "
operator|+
name|peerClusterZNode
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|startPosition
return|;
block|}
specifier|private
name|void
name|terminate
parameter_list|(
name|String
name|reason
parameter_list|,
name|Exception
name|cause
parameter_list|)
block|{
if|if
condition|(
name|cause
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Closing worker for wal group {} because: {}"
argument_list|,
name|this
operator|.
name|walGroupId
argument_list|,
name|reason
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Closing worker for wal group "
operator|+
name|this
operator|.
name|walGroupId
operator|+
literal|" because an error occurred: "
operator|+
name|reason
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
name|entryReader
operator|.
name|interrupt
argument_list|()
expr_stmt|;
name|Threads
operator|.
name|shutdown
argument_list|(
name|entryReader
argument_list|,
name|sleepForRetries
argument_list|)
expr_stmt|;
name|this
operator|.
name|interrupt
argument_list|()
expr_stmt|;
name|Threads
operator|.
name|shutdown
argument_list|(
name|this
argument_list|,
name|sleepForRetries
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"ReplicationSourceWorker {} terminated"
argument_list|,
name|this
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

