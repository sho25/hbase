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
name|master
operator|.
name|cleaner
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
name|Map
operator|.
name|Entry
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
name|ReplicationPeers
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
name|ReplicationStateZKBase
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
name|ZKUtil
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
name|ZNodePaths
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
name|ZooKeeperWatcher
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
comment|/**  * Used to clean the replication queues belonging to the peer which does not exist.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReplicationZKNodeCleaner
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
name|ReplicationZKNodeCleaner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ZooKeeperWatcher
name|zkw
decl_stmt|;
specifier|private
specifier|final
name|ReplicationQueuesClient
name|queuesClient
decl_stmt|;
specifier|private
specifier|final
name|ReplicationPeers
name|replicationPeers
decl_stmt|;
specifier|private
specifier|final
name|ReplicationQueueDeletor
name|queueDeletor
decl_stmt|;
specifier|public
name|ReplicationZKNodeCleaner
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ZooKeeperWatcher
name|zkw
parameter_list|,
name|Abortable
name|abortable
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|this
operator|.
name|zkw
operator|=
name|zkw
expr_stmt|;
name|this
operator|.
name|queuesClient
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
name|abortable
argument_list|,
name|zkw
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|queuesClient
operator|.
name|init
argument_list|()
expr_stmt|;
name|this
operator|.
name|replicationPeers
operator|=
name|ReplicationFactory
operator|.
name|getReplicationPeers
argument_list|(
name|zkw
argument_list|,
name|conf
argument_list|,
name|this
operator|.
name|queuesClient
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
name|this
operator|.
name|replicationPeers
operator|.
name|init
argument_list|()
expr_stmt|;
name|this
operator|.
name|queueDeletor
operator|=
operator|new
name|ReplicationQueueDeletor
argument_list|(
name|zkw
argument_list|,
name|conf
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"failed to construct ReplicationZKNodeCleaner"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * @return undeletedQueues replicator with its queueIds for removed peers    * @throws IOException    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|getUnDeletedQueues
parameter_list|()
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
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
name|this
operator|.
name|replicationPeers
operator|.
name|getAllPeerIds
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|List
argument_list|<
name|String
argument_list|>
name|replicators
init|=
name|this
operator|.
name|queuesClient
operator|.
name|getListOfReplicators
argument_list|()
decl_stmt|;
if|if
condition|(
name|replicators
operator|==
literal|null
operator|||
name|replicators
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|undeletedQueues
return|;
block|}
for|for
control|(
name|String
name|replicator
range|:
name|replicators
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|queueIds
init|=
name|this
operator|.
name|queuesClient
operator|.
name|getAllQueues
argument_list|(
name|replicator
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|queueId
range|:
name|queueIds
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
parameter_list|(
name|key
parameter_list|)
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
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to get the replication queues of all replicators"
argument_list|,
name|ke
argument_list|)
throw|;
block|}
return|return
name|undeletedQueues
return|;
block|}
comment|/**    * @return undeletedHFileRefsQueue replicator with its undeleted queueIds for removed peers in    *         hfile-refs queue    * @throws IOException    */
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getUnDeletedHFileRefsQueues
parameter_list|()
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|undeletedHFileRefsQueue
init|=
operator|new
name|HashSet
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
name|this
operator|.
name|replicationPeers
operator|.
name|getAllPeerIds
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|hfileRefsZNode
init|=
name|queueDeletor
operator|.
name|getHfileRefsZNode
argument_list|()
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|-
literal|1
operator|==
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|zkw
argument_list|,
name|hfileRefsZNode
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|listOfPeers
init|=
name|this
operator|.
name|queuesClient
operator|.
name|getAllPeersFromHFileRefsQueue
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|peers
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|listOfPeers
argument_list|)
decl_stmt|;
name|peers
operator|.
name|removeAll
argument_list|(
name|peerIds
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|peers
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|undeletedHFileRefsQueue
operator|.
name|addAll
argument_list|(
name|peers
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to get list of all peers from hfile-refs znode "
operator|+
name|hfileRefsZNode
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|undeletedHFileRefsQueue
return|;
block|}
specifier|private
class|class
name|ReplicationQueueDeletor
extends|extends
name|ReplicationStateZKBase
block|{
specifier|public
name|ReplicationQueueDeletor
parameter_list|(
name|ZooKeeperWatcher
name|zk
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|Abortable
name|abortable
parameter_list|)
block|{
name|super
argument_list|(
name|zk
argument_list|,
name|conf
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
block|}
comment|/**      * @param replicator The regionserver which has undeleted queue      * @param queueId The undeleted queue id      * @throws IOException      */
specifier|public
name|void
name|removeQueue
parameter_list|(
specifier|final
name|String
name|replicator
parameter_list|,
specifier|final
name|String
name|queueId
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|queueZnodePath
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|queuesZNode
argument_list|,
name|replicator
argument_list|)
argument_list|,
name|queueId
argument_list|)
decl_stmt|;
try|try
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
name|replicationPeers
operator|.
name|getAllPeerIds
argument_list|()
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
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|queueZnodePath
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Successfully removed replication queue, replicator: "
operator|+
name|replicator
operator|+
literal|", queueId: "
operator|+
name|queueId
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to delete queue, replicator: "
operator|+
name|replicator
operator|+
literal|", queueId: "
operator|+
name|queueId
argument_list|)
throw|;
block|}
block|}
comment|/**      * @param hfileRefsQueueId The undeleted hfile-refs queue id      * @throws IOException      */
specifier|public
name|void
name|removeHFileRefsQueue
parameter_list|(
specifier|final
name|String
name|hfileRefsQueueId
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|node
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|hfileRefsZNode
argument_list|,
name|hfileRefsQueueId
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|replicationPeers
operator|.
name|getAllPeerIds
argument_list|()
operator|.
name|contains
argument_list|(
name|hfileRefsQueueId
argument_list|)
condition|)
block|{
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|node
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Successfully removed hfile-refs queue "
operator|+
name|hfileRefsQueueId
operator|+
literal|" from path "
operator|+
name|hfileRefsZNode
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to delete hfile-refs queue "
operator|+
name|hfileRefsQueueId
operator|+
literal|" from path "
operator|+
name|hfileRefsZNode
argument_list|)
throw|;
block|}
block|}
name|String
name|getHfileRefsZNode
parameter_list|()
block|{
return|return
name|this
operator|.
name|hfileRefsZNode
return|;
block|}
block|}
comment|/**    * Remove the undeleted replication queue's zk node for removed peers.    * @param undeletedQueues replicator with its queueIds for removed peers    * @throws IOException    */
specifier|public
name|void
name|removeQueues
parameter_list|(
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|undeletedQueues
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|replicatorAndQueueIds
range|:
name|undeletedQueues
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
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
name|queueDeletor
operator|.
name|removeQueue
argument_list|(
name|replicator
argument_list|,
name|queueId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Remove the undeleted hfile-refs queue's zk node for removed peers.    * @param undeletedHFileRefsQueues replicator with its undeleted queueIds for removed peers in    *          hfile-refs queue    * @throws IOException    */
specifier|public
name|void
name|removeHFileRefsQueues
parameter_list|(
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|undeletedHFileRefsQueues
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|String
name|hfileRefsQueueId
range|:
name|undeletedHFileRefsQueues
control|)
block|{
name|queueDeletor
operator|.
name|removeHFileRefsQueue
argument_list|(
name|hfileRefsQueueId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

