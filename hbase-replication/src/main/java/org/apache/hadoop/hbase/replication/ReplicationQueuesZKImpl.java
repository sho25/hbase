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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|exceptions
operator|.
name|DeserializationException
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
name|Bytes
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
name|Pair
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
name|ZKUtil
operator|.
name|ZKUtilOp
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
name|zookeeper
operator|.
name|KeeperException
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
comment|/**  * This class provides an implementation of the  * interface using ZooKeeper. The  * base znode that this class works at is the myQueuesZnode. The myQueuesZnode contains a list of  * all outstanding WAL files on this region server that need to be replicated. The myQueuesZnode is  * the regionserver name (a concatenation of the region server’s hostname, client port and start  * code). For example:  *  * /hbase/replication/rs/hostname.example.org,6020,1234  *  * Within this znode, the region server maintains a set of WAL replication queues. These queues are  * represented by child znodes named using there give queue id. For example:  *  * /hbase/replication/rs/hostname.example.org,6020,1234/1  * /hbase/replication/rs/hostname.example.org,6020,1234/2  *  * Each queue has one child znode for every WAL that still needs to be replicated. The value of  * these WAL child znodes is the latest position that has been replicated. This position is updated  * every time a WAL entry is replicated. For example:  *  * /hbase/replication/rs/hostname.example.org,6020,1234/1/23522342.23422 [VALUE: 254]  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReplicationQueuesZKImpl
extends|extends
name|ReplicationStateZKBase
implements|implements
name|ReplicationQueues
block|{
comment|/** Znode containing all replication queues for this region server. */
specifier|private
name|String
name|myQueuesZnode
decl_stmt|;
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
name|ReplicationQueuesZKImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|ReplicationQueuesZKImpl
parameter_list|(
name|ReplicationQueuesArguments
name|args
parameter_list|)
block|{
name|this
argument_list|(
name|args
operator|.
name|getZk
argument_list|()
argument_list|,
name|args
operator|.
name|getConf
argument_list|()
argument_list|,
name|args
operator|.
name|getAbortable
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ReplicationQueuesZKImpl
parameter_list|(
specifier|final
name|ZKWatcher
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
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|String
name|serverName
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|this
operator|.
name|myQueuesZnode
operator|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|queuesZNode
argument_list|,
name|serverName
argument_list|)
expr_stmt|;
try|try
block|{
if|if
condition|(
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|this
operator|.
name|myQueuesZnode
argument_list|)
operator|<
literal|0
condition|)
block|{
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|this
operator|.
name|myQueuesZnode
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
name|ReplicationException
argument_list|(
literal|"Could not initialize replication queues."
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|conf
operator|.
name|getBoolean
argument_list|(
name|HConstants
operator|.
name|REPLICATION_BULKLOAD_ENABLE_KEY
argument_list|,
name|HConstants
operator|.
name|REPLICATION_BULKLOAD_ENABLE_DEFAULT
argument_list|)
condition|)
block|{
try|try
block|{
if|if
condition|(
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|this
operator|.
name|hfileRefsZNode
argument_list|)
operator|<
literal|0
condition|)
block|{
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|this
operator|.
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
name|ReplicationException
argument_list|(
literal|"Could not initialize hfile references replication queue."
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|removeQueue
parameter_list|(
name|String
name|queueId
parameter_list|)
block|{
try|try
block|{
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|myQueuesZnode
argument_list|,
name|queueId
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|this
operator|.
name|abortable
operator|.
name|abort
argument_list|(
literal|"Failed to delete queue (queueId="
operator|+
name|queueId
operator|+
literal|")"
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
name|addLog
parameter_list|(
name|String
name|queueId
parameter_list|,
name|String
name|filename
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|String
name|znode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|myQueuesZnode
argument_list|,
name|queueId
argument_list|)
decl_stmt|;
name|znode
operator|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|znode
argument_list|,
name|filename
argument_list|)
expr_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|znode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"Could not add log because znode could not be created. queueId="
operator|+
name|queueId
operator|+
literal|", filename="
operator|+
name|filename
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|removeLog
parameter_list|(
name|String
name|queueId
parameter_list|,
name|String
name|filename
parameter_list|)
block|{
try|try
block|{
name|String
name|znode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|myQueuesZnode
argument_list|,
name|queueId
argument_list|)
decl_stmt|;
name|znode
operator|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|znode
argument_list|,
name|filename
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|znode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|this
operator|.
name|abortable
operator|.
name|abort
argument_list|(
literal|"Failed to remove wal from queue (queueId="
operator|+
name|queueId
operator|+
literal|", filename="
operator|+
name|filename
operator|+
literal|")"
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
name|setLogPosition
parameter_list|(
name|String
name|queueId
parameter_list|,
name|String
name|filename
parameter_list|,
name|long
name|position
parameter_list|)
block|{
try|try
block|{
name|String
name|znode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|myQueuesZnode
argument_list|,
name|queueId
argument_list|)
decl_stmt|;
name|znode
operator|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|znode
argument_list|,
name|filename
argument_list|)
expr_stmt|;
comment|// Why serialize String of Long and not Long as bytes?
name|ZKUtil
operator|.
name|setData
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|znode
argument_list|,
name|ZKUtil
operator|.
name|positionToByteArray
argument_list|(
name|position
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|this
operator|.
name|abortable
operator|.
name|abort
argument_list|(
literal|"Failed to write replication wal position (filename="
operator|+
name|filename
operator|+
literal|", position="
operator|+
name|position
operator|+
literal|")"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLogPosition
parameter_list|(
name|String
name|queueId
parameter_list|,
name|String
name|filename
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|String
name|clusterZnode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|myQueuesZnode
argument_list|,
name|queueId
argument_list|)
decl_stmt|;
name|String
name|znode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|clusterZnode
argument_list|,
name|filename
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
literal|null
decl_stmt|;
try|try
block|{
name|bytes
operator|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|znode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"Internal Error: could not get position in log for queueId="
operator|+
name|queueId
operator|+
literal|", filename="
operator|+
name|filename
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
return|return
literal|0
return|;
block|}
try|try
block|{
return|return
name|ZKUtil
operator|.
name|parseWALPositionFrom
argument_list|(
name|bytes
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|DeserializationException
name|de
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to parse WALPosition for queueId="
operator|+
name|queueId
operator|+
literal|" and wal="
operator|+
name|filename
operator|+
literal|" znode content, continuing."
argument_list|)
expr_stmt|;
block|}
comment|// if we can not parse the position, start at the beginning of the wal file
comment|// again
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isThisOurRegionServer
parameter_list|(
name|String
name|regionserver
parameter_list|)
block|{
return|return
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|queuesZNode
argument_list|,
name|regionserver
argument_list|)
operator|.
name|equals
argument_list|(
name|this
operator|.
name|myQueuesZnode
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getUnClaimedQueueIds
parameter_list|(
name|String
name|regionserver
parameter_list|)
block|{
if|if
condition|(
name|isThisOurRegionServer
argument_list|(
name|regionserver
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|String
name|rsZnodePath
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|queuesZNode
argument_list|,
name|regionserver
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|queues
init|=
literal|null
decl_stmt|;
try|try
block|{
name|queues
operator|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|rsZnodePath
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|this
operator|.
name|abortable
operator|.
name|abort
argument_list|(
literal|"Failed to getUnClaimedQueueIds for RS"
operator|+
name|regionserver
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|queues
return|;
block|}
annotation|@
name|Override
specifier|public
name|Pair
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
name|claimQueue
parameter_list|(
name|String
name|regionserver
parameter_list|,
name|String
name|queueId
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Atomically moving "
operator|+
name|regionserver
operator|+
literal|"/"
operator|+
name|queueId
operator|+
literal|"'s WALs to my queue"
argument_list|)
expr_stmt|;
return|return
name|moveQueueUsingMulti
argument_list|(
name|regionserver
argument_list|,
name|queueId
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|removeReplicatorIfQueueIsEmpty
parameter_list|(
name|String
name|regionserver
parameter_list|)
block|{
name|String
name|rsPath
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|queuesZNode
argument_list|,
name|regionserver
argument_list|)
decl_stmt|;
try|try
block|{
name|List
argument_list|<
name|String
argument_list|>
name|list
init|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|rsPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|list
operator|!=
literal|null
operator|&&
name|list
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|rsPath
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
name|LOG
operator|.
name|warn
argument_list|(
literal|"Got error while removing replicator"
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
name|removeAllQueues
parameter_list|()
block|{
try|try
block|{
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|this
operator|.
name|myQueuesZnode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
comment|// if the znode is already expired, don't bother going further
if|if
condition|(
name|e
operator|instanceof
name|KeeperException
operator|.
name|SessionExpiredException
condition|)
block|{
return|return;
block|}
name|this
operator|.
name|abortable
operator|.
name|abort
argument_list|(
literal|"Failed to delete replication queues for region server: "
operator|+
name|this
operator|.
name|myQueuesZnode
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getLogsInQueue
parameter_list|(
name|String
name|queueId
parameter_list|)
block|{
name|String
name|znode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|myQueuesZnode
argument_list|,
name|queueId
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|result
init|=
literal|null
decl_stmt|;
try|try
block|{
name|result
operator|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|znode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|this
operator|.
name|abortable
operator|.
name|abort
argument_list|(
literal|"Failed to get list of wals for queueId="
operator|+
name|queueId
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getAllQueues
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|listOfQueues
init|=
literal|null
decl_stmt|;
try|try
block|{
name|listOfQueues
operator|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|this
operator|.
name|myQueuesZnode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|this
operator|.
name|abortable
operator|.
name|abort
argument_list|(
literal|"Failed to get a list of queues for region server: "
operator|+
name|this
operator|.
name|myQueuesZnode
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|listOfQueues
operator|==
literal|null
condition|?
operator|new
name|ArrayList
argument_list|<>
argument_list|()
else|:
name|listOfQueues
return|;
block|}
comment|/**    * It "atomically" copies one peer's wals queue from another dead region server and returns them    * all sorted. The new peer id is equal to the old peer id appended with the dead server's znode.    * @param znode pertaining to the region server to copy the queues from    * @peerId peerId pertaining to the queue need to be copied    */
specifier|private
name|Pair
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
name|moveQueueUsingMulti
parameter_list|(
name|String
name|znode
parameter_list|,
name|String
name|peerId
parameter_list|)
block|{
try|try
block|{
comment|// hbase/replication/rs/deadrs
name|String
name|deadRSZnodePath
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|queuesZNode
argument_list|,
name|znode
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ZKUtilOp
argument_list|>
name|listOfOps
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|ReplicationQueueInfo
name|replicationQueueInfo
init|=
operator|new
name|ReplicationQueueInfo
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
name|String
name|newPeerId
init|=
name|peerId
operator|+
literal|"-"
operator|+
name|znode
decl_stmt|;
name|String
name|newPeerZnode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|myQueuesZnode
argument_list|,
name|newPeerId
argument_list|)
decl_stmt|;
comment|// check the logs queue for the old peer cluster
name|String
name|oldClusterZnode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|deadRSZnodePath
argument_list|,
name|peerId
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|wals
init|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|oldClusterZnode
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|peerExists
argument_list|(
name|replicationQueueInfo
operator|.
name|getPeerId
argument_list|()
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Peer "
operator|+
name|replicationQueueInfo
operator|.
name|getPeerId
argument_list|()
operator|+
literal|" didn't exist, will move its queue to avoid the failure of multi op"
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|wal
range|:
name|wals
control|)
block|{
name|String
name|oldWalZnode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|oldClusterZnode
argument_list|,
name|wal
argument_list|)
decl_stmt|;
name|listOfOps
operator|.
name|add
argument_list|(
name|ZKUtilOp
operator|.
name|deleteNodeFailSilent
argument_list|(
name|oldWalZnode
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|listOfOps
operator|.
name|add
argument_list|(
name|ZKUtilOp
operator|.
name|deleteNodeFailSilent
argument_list|(
name|oldClusterZnode
argument_list|)
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|multiOrSequential
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|listOfOps
argument_list|,
literal|false
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|SortedSet
argument_list|<
name|String
argument_list|>
name|logQueue
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|wals
operator|==
literal|null
operator|||
name|wals
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|listOfOps
operator|.
name|add
argument_list|(
name|ZKUtilOp
operator|.
name|deleteNodeFailSilent
argument_list|(
name|oldClusterZnode
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// create the new cluster znode
name|ZKUtilOp
name|op
init|=
name|ZKUtilOp
operator|.
name|createAndFailSilent
argument_list|(
name|newPeerZnode
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
decl_stmt|;
name|listOfOps
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
comment|// get the offset of the logs and set it to new znodes
for|for
control|(
name|String
name|wal
range|:
name|wals
control|)
block|{
name|String
name|oldWalZnode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|oldClusterZnode
argument_list|,
name|wal
argument_list|)
decl_stmt|;
name|byte
index|[]
name|logOffset
init|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|oldWalZnode
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating "
operator|+
name|wal
operator|+
literal|" with data "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|logOffset
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|newLogZnode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|newPeerZnode
argument_list|,
name|wal
argument_list|)
decl_stmt|;
name|listOfOps
operator|.
name|add
argument_list|(
name|ZKUtilOp
operator|.
name|createAndFailSilent
argument_list|(
name|newLogZnode
argument_list|,
name|logOffset
argument_list|)
argument_list|)
expr_stmt|;
name|listOfOps
operator|.
name|add
argument_list|(
name|ZKUtilOp
operator|.
name|deleteNodeFailSilent
argument_list|(
name|oldWalZnode
argument_list|)
argument_list|)
expr_stmt|;
name|logQueue
operator|.
name|add
argument_list|(
name|wal
argument_list|)
expr_stmt|;
block|}
comment|// add delete op for peer
name|listOfOps
operator|.
name|add
argument_list|(
name|ZKUtilOp
operator|.
name|deleteNodeFailSilent
argument_list|(
name|oldClusterZnode
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|trace
argument_list|(
literal|" The multi list size is: "
operator|+
name|listOfOps
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ZKUtil
operator|.
name|multiOrSequential
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|listOfOps
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Atomically moved "
operator|+
name|znode
operator|+
literal|"/"
operator|+
name|peerId
operator|+
literal|"'s WALs to my queue"
argument_list|)
expr_stmt|;
return|return
operator|new
name|Pair
argument_list|<>
argument_list|(
name|newPeerId
argument_list|,
name|logQueue
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
comment|// Multi call failed; it looks like some other regionserver took away the logs.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Got exception in copyQueuesFromRSUsingMulti: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Got exception in copyQueuesFromRSUsingMulti: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addHFileRefs
parameter_list|(
name|String
name|peerId
parameter_list|,
name|List
argument_list|<
name|Pair
argument_list|<
name|Path
argument_list|,
name|Path
argument_list|>
argument_list|>
name|pairs
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|String
name|peerZnode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|hfileRefsZNode
argument_list|,
name|peerId
argument_list|)
decl_stmt|;
name|boolean
name|debugEnabled
init|=
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
decl_stmt|;
if|if
condition|(
name|debugEnabled
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding hfile references "
operator|+
name|pairs
operator|+
literal|" in queue "
operator|+
name|peerZnode
argument_list|)
expr_stmt|;
block|}
name|int
name|size
init|=
name|pairs
operator|.
name|size
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ZKUtilOp
argument_list|>
name|listOfOps
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|size
argument_list|)
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|listOfOps
operator|.
name|add
argument_list|(
name|ZKUtilOp
operator|.
name|createAndFailSilent
argument_list|(
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|peerZnode
argument_list|,
name|pairs
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getSecond
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|debugEnabled
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|" The multi list size for adding hfile references in zk for node "
operator|+
name|peerZnode
operator|+
literal|" is "
operator|+
name|listOfOps
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|ZKUtil
operator|.
name|multiOrSequential
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|listOfOps
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"Failed to create hfile reference znode="
operator|+
name|e
operator|.
name|getPath
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|removeHFileRefs
parameter_list|(
name|String
name|peerId
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|files
parameter_list|)
block|{
name|String
name|peerZnode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|hfileRefsZNode
argument_list|,
name|peerId
argument_list|)
decl_stmt|;
name|boolean
name|debugEnabled
init|=
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
decl_stmt|;
if|if
condition|(
name|debugEnabled
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removing hfile references "
operator|+
name|files
operator|+
literal|" from queue "
operator|+
name|peerZnode
argument_list|)
expr_stmt|;
block|}
name|int
name|size
init|=
name|files
operator|.
name|size
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ZKUtilOp
argument_list|>
name|listOfOps
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|size
argument_list|)
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|listOfOps
operator|.
name|add
argument_list|(
name|ZKUtilOp
operator|.
name|deleteNodeFailSilent
argument_list|(
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|peerZnode
argument_list|,
name|files
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|debugEnabled
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|" The multi list size for removing hfile references in zk for node "
operator|+
name|peerZnode
operator|+
literal|" is "
operator|+
name|listOfOps
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|ZKUtil
operator|.
name|multiOrSequential
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|listOfOps
argument_list|,
literal|true
argument_list|)
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
literal|"Failed to remove hfile reference znode="
operator|+
name|e
operator|.
name|getPath
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
name|addPeerToHFileRefs
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|String
name|peerZnode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|hfileRefsZNode
argument_list|,
name|peerId
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|peerZnode
argument_list|)
operator|==
operator|-
literal|1
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Adding peer "
operator|+
name|peerId
operator|+
literal|" to hfile reference queue."
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|peerZnode
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
name|ReplicationException
argument_list|(
literal|"Failed to add peer "
operator|+
name|peerId
operator|+
literal|" to hfile reference queue."
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|removePeerFromHFileRefs
parameter_list|(
name|String
name|peerId
parameter_list|)
block|{
specifier|final
name|String
name|peerZnode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|hfileRefsZNode
argument_list|,
name|peerId
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|peerZnode
argument_list|)
operator|==
operator|-
literal|1
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
literal|"Peer "
operator|+
name|peerZnode
operator|+
literal|" not found in hfile reference queue."
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Removing peer "
operator|+
name|peerZnode
operator|+
literal|" from hfile reference queue."
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|peerZnode
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
name|LOG
operator|.
name|error
argument_list|(
literal|"Ignoring the exception to remove peer "
operator|+
name|peerId
operator|+
literal|" from hfile reference queue."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

