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
name|List
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
name|Abortable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableSet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
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
name|apache
operator|.
name|zookeeper
operator|.
name|data
operator|.
name|Stat
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReplicationQueuesClientZKImpl
extends|extends
name|ReplicationStateZKBase
implements|implements
name|ReplicationQueuesClient
block|{
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ReplicationQueuesClientZKImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|ReplicationQueuesClientZKImpl
parameter_list|(
name|ReplicationQueuesClientArguments
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
name|ReplicationQueuesClientZKImpl
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
parameter_list|()
throws|throws
name|ReplicationException
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
name|queuesZNode
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
name|queuesZNode
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
literal|"Internal error while initializing a queues client"
argument_list|,
name|e
argument_list|)
throw|;
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
name|serverName
parameter_list|,
name|String
name|queueId
parameter_list|)
throws|throws
name|KeeperException
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
name|queuesZNode
argument_list|,
name|serverName
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
name|queueId
argument_list|)
expr_stmt|;
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
operator|+
literal|" and serverName="
operator|+
name|serverName
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
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
parameter_list|(
name|String
name|serverName
parameter_list|)
throws|throws
name|KeeperException
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
name|queuesZNode
argument_list|,
name|serverName
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
literal|"Failed to get list of queues for serverName="
operator|+
name|serverName
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getAllWALs
parameter_list|()
throws|throws
name|KeeperException
block|{
comment|/**      * Load all wals in all replication queues from ZK. This method guarantees to return a      * snapshot which contains all WALs in the zookeeper at the start of this call even there      * is concurrent queue failover. However, some newly created WALs during the call may      * not be included.      */
for|for
control|(
name|int
name|retry
init|=
literal|0
init|;
condition|;
name|retry
operator|++
control|)
block|{
name|int
name|v0
init|=
name|getQueuesZNodeCversion
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rss
init|=
name|getListOfReplicators
argument_list|()
decl_stmt|;
if|if
condition|(
name|rss
operator|==
literal|null
operator|||
name|rss
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Didn't find any region server that replicates, won't prevent any deletions."
argument_list|)
expr_stmt|;
return|return
name|ImmutableSet
operator|.
name|of
argument_list|()
return|;
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|wals
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|rs
range|:
name|rss
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|listOfPeers
init|=
name|getAllQueues
argument_list|(
name|rs
argument_list|)
decl_stmt|;
comment|// if rs just died, this will be null
if|if
condition|(
name|listOfPeers
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|String
name|id
range|:
name|listOfPeers
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|peersWals
init|=
name|getLogsInQueue
argument_list|(
name|rs
argument_list|,
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|peersWals
operator|!=
literal|null
condition|)
block|{
name|wals
operator|.
name|addAll
argument_list|(
name|peersWals
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|int
name|v1
init|=
name|getQueuesZNodeCversion
argument_list|()
decl_stmt|;
if|if
condition|(
name|v0
operator|==
name|v1
condition|)
block|{
return|return
name|wals
return|;
block|}
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Replication queue node cversion changed from %d to %d, retry = %d"
argument_list|,
name|v0
argument_list|,
name|v1
argument_list|,
name|retry
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|int
name|getQueuesZNodeCversion
parameter_list|()
throws|throws
name|KeeperException
block|{
try|try
block|{
name|Stat
name|stat
init|=
operator|new
name|Stat
argument_list|()
decl_stmt|;
name|ZKUtil
operator|.
name|getDataNoWatch
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|this
operator|.
name|queuesZNode
argument_list|,
name|stat
argument_list|)
expr_stmt|;
return|return
name|stat
operator|.
name|getCversion
argument_list|()
return|;
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
literal|"Failed to get stat of replication rs node"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getHFileRefsNodeChangeVersion
parameter_list|()
throws|throws
name|KeeperException
block|{
name|Stat
name|stat
init|=
operator|new
name|Stat
argument_list|()
decl_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|getDataNoWatch
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|this
operator|.
name|hfileRefsZNode
argument_list|,
name|stat
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
literal|"Failed to get stat of replication hfile references node."
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
return|return
name|stat
operator|.
name|getCversion
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getAllPeersFromHFileRefsQueue
parameter_list|()
throws|throws
name|KeeperException
block|{
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
name|this
operator|.
name|hfileRefsZNode
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
literal|"Failed to get list of all peers in hfile references node."
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
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
name|getReplicableHFiles
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|KeeperException
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
name|hfileRefsZNode
argument_list|,
name|peerId
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
literal|"Failed to get list of hfile references for peerId="
operator|+
name|peerId
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

