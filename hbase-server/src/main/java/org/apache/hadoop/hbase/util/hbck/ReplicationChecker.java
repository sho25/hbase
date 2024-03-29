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
name|util
operator|.
name|hbck
package|;
end_package

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
name|hbase
operator|.
name|ServerName
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
name|ReplicationPeerStorage
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
name|ReplicationQueueInfo
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
name|replication
operator|.
name|ReplicationStorageFactory
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
name|HbckErrorReporter
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
comment|/**  * Check and fix undeleted replication queues for removed peerId.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReplicationChecker
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
name|ReplicationChecker
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HbckErrorReporter
name|errorReporter
decl_stmt|;
comment|// replicator with its queueIds for removed peers
specifier|private
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|undeletedQueueIds
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// replicator with its undeleted queueIds for removed peers in hfile-refs queue
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|undeletedHFileRefsPeerIds
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ReplicationPeerStorage
name|peerStorage
decl_stmt|;
specifier|private
specifier|final
name|ReplicationQueueStorage
name|queueStorage
decl_stmt|;
specifier|public
name|ReplicationChecker
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ZKWatcher
name|zkw
parameter_list|,
name|HbckErrorReporter
name|errorReporter
parameter_list|)
block|{
name|this
operator|.
name|peerStorage
operator|=
name|ReplicationStorageFactory
operator|.
name|getReplicationPeerStorage
argument_list|(
name|zkw
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|queueStorage
operator|=
name|ReplicationStorageFactory
operator|.
name|getReplicationQueueStorage
argument_list|(
name|zkw
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|errorReporter
operator|=
name|errorReporter
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasUnDeletedQueues
parameter_list|()
block|{
return|return
name|errorReporter
operator|.
name|getErrorList
argument_list|()
operator|.
name|contains
argument_list|(
name|HbckErrorReporter
operator|.
name|ERROR_CODE
operator|.
name|UNDELETED_REPLICATION_QUEUE
argument_list|)
return|;
block|}
specifier|private
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|getUnDeletedQueues
parameter_list|()
throws|throws
name|ReplicationException
block|{
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|undeletedQueues
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|peerIds
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|peerStorage
operator|.
name|listPeerIds
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|ServerName
name|replicator
range|:
name|queueStorage
operator|.
name|getListOfReplicators
argument_list|()
control|)
block|{
for|for
control|(
name|String
name|queueId
range|:
name|queueStorage
operator|.
name|getAllQueues
argument_list|(
name|replicator
argument_list|)
control|)
block|{
name|ReplicationQueueInfo
name|queueInfo
init|=
operator|new
name|ReplicationQueueInfo
argument_list|(
name|queueId
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|peerIds
operator|.
name|contains
argument_list|(
name|queueInfo
operator|.
name|getPeerId
argument_list|()
argument_list|)
condition|)
block|{
name|undeletedQueues
operator|.
name|computeIfAbsent
argument_list|(
name|replicator
argument_list|,
name|key
lambda|->
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
name|queueId
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Undeleted replication queue for removed peer found: "
operator|+
literal|"[removedPeerId={}, replicator={}, queueId={}]"
argument_list|,
name|queueInfo
operator|.
name|getPeerId
argument_list|()
argument_list|,
name|replicator
argument_list|,
name|queueId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|undeletedQueues
return|;
block|}
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|getUndeletedHFileRefsPeers
parameter_list|()
throws|throws
name|ReplicationException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|undeletedHFileRefsPeerIds
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|queueStorage
operator|.
name|getAllPeersFromHFileRefsQueue
argument_list|()
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|peerIds
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|peerStorage
operator|.
name|listPeerIds
argument_list|()
argument_list|)
decl_stmt|;
name|undeletedHFileRefsPeerIds
operator|.
name|removeAll
argument_list|(
name|peerIds
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
for|for
control|(
name|String
name|peerId
range|:
name|undeletedHFileRefsPeerIds
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Undeleted replication hfile-refs queue for removed peer {} found"
argument_list|,
name|peerId
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|undeletedHFileRefsPeerIds
return|;
block|}
specifier|public
name|void
name|checkUnDeletedQueues
parameter_list|()
throws|throws
name|ReplicationException
block|{
name|undeletedQueueIds
operator|=
name|getUnDeletedQueues
argument_list|()
expr_stmt|;
name|undeletedQueueIds
operator|.
name|forEach
argument_list|(
parameter_list|(
name|replicator
parameter_list|,
name|queueIds
parameter_list|)
lambda|->
block|{
name|queueIds
operator|.
name|forEach
argument_list|(
name|queueId
lambda|->
block|{
name|ReplicationQueueInfo
name|queueInfo
init|=
operator|new
name|ReplicationQueueInfo
argument_list|(
name|queueId
argument_list|)
decl_stmt|;
name|String
name|msg
init|=
literal|"Undeleted replication queue for removed peer found: "
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"[removedPeerId=%s, replicator=%s, queueId=%s]"
argument_list|,
name|queueInfo
operator|.
name|getPeerId
argument_list|()
argument_list|,
name|replicator
argument_list|,
name|queueId
argument_list|)
decl_stmt|;
name|errorReporter
operator|.
name|reportError
argument_list|(
name|HbckErrorReporter
operator|.
name|ERROR_CODE
operator|.
name|UNDELETED_REPLICATION_QUEUE
argument_list|,
name|msg
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|undeletedHFileRefsPeerIds
operator|=
name|getUndeletedHFileRefsPeers
argument_list|()
expr_stmt|;
name|undeletedHFileRefsPeerIds
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|peerId
lambda|->
literal|"Undeleted replication hfile-refs queue for removed peer "
operator|+
name|peerId
operator|+
literal|" found"
argument_list|)
operator|.
name|forEach
argument_list|(
name|msg
lambda|->
name|errorReporter
operator|.
name|reportError
argument_list|(
name|HbckErrorReporter
operator|.
name|ERROR_CODE
operator|.
name|UNDELETED_REPLICATION_QUEUE
argument_list|,
name|msg
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|fixUnDeletedQueues
parameter_list|()
throws|throws
name|ReplicationException
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|replicatorAndQueueIds
range|:
name|undeletedQueueIds
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|ServerName
name|replicator
init|=
name|replicatorAndQueueIds
operator|.
name|getKey
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|queueId
range|:
name|replicatorAndQueueIds
operator|.
name|getValue
argument_list|()
control|)
block|{
name|queueStorage
operator|.
name|removeQueue
argument_list|(
name|replicator
argument_list|,
name|queueId
argument_list|)
expr_stmt|;
block|}
name|queueStorage
operator|.
name|removeReplicatorIfQueueIsEmpty
argument_list|(
name|replicator
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|peerId
range|:
name|undeletedHFileRefsPeerIds
control|)
block|{
name|queueStorage
operator|.
name|removePeerFromHFileRefs
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

