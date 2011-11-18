begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|zookeeper
package|;
end_package

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
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_comment
comment|/**  * Handles coordination of a single "leader" instance among many possible  * candidates.  The first {@code ZKLeaderManager} to successfully create  * the given znode becomes the leader, allowing the instance to continue  * with whatever processing must be protected.  Other {@ZKLeaderManager}  * instances will wait to be notified of changes to the leader znode.  * If the current master instance fails, the ephemeral leader znode will  * be removed, and all waiting instances will be notified, with the race  * to claim the leader znode beginning all over again.  */
end_comment

begin_class
specifier|public
class|class
name|ZKLeaderManager
extends|extends
name|ZooKeeperListener
block|{
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ZKLeaderManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|leaderExists
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
specifier|private
name|String
name|leaderZNode
decl_stmt|;
specifier|private
name|byte
index|[]
name|nodeId
decl_stmt|;
specifier|private
name|Stoppable
name|candidate
decl_stmt|;
specifier|public
name|ZKLeaderManager
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|String
name|leaderZNode
parameter_list|,
name|byte
index|[]
name|identifier
parameter_list|,
name|Stoppable
name|candidate
parameter_list|)
block|{
name|super
argument_list|(
name|watcher
argument_list|)
expr_stmt|;
name|this
operator|.
name|leaderZNode
operator|=
name|leaderZNode
expr_stmt|;
name|this
operator|.
name|nodeId
operator|=
name|identifier
expr_stmt|;
name|this
operator|.
name|candidate
operator|=
name|candidate
expr_stmt|;
block|}
specifier|public
name|void
name|start
parameter_list|()
block|{
try|try
block|{
name|watcher
operator|.
name|registerListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|String
name|parent
init|=
name|ZKUtil
operator|.
name|getParent
argument_list|(
name|leaderZNode
argument_list|)
decl_stmt|;
if|if
condition|(
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|watcher
argument_list|,
name|parent
argument_list|)
operator|<
literal|0
condition|)
block|{
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|watcher
argument_list|,
name|parent
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
name|watcher
operator|.
name|abort
argument_list|(
literal|"Unhandled zk exception when starting"
argument_list|,
name|ke
argument_list|)
expr_stmt|;
name|candidate
operator|.
name|stop
argument_list|(
literal|"Unhandled zk exception starting up: "
operator|+
name|ke
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeCreated
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|leaderZNode
operator|.
name|equals
argument_list|(
name|path
argument_list|)
operator|&&
operator|!
name|candidate
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|handleLeaderChange
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeDeleted
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|leaderZNode
operator|.
name|equals
argument_list|(
name|path
argument_list|)
operator|&&
operator|!
name|candidate
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|handleLeaderChange
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|handleLeaderChange
parameter_list|()
block|{
try|try
block|{
synchronized|synchronized
init|(
name|leaderExists
init|)
block|{
if|if
condition|(
name|ZKUtil
operator|.
name|watchAndCheckExists
argument_list|(
name|watcher
argument_list|,
name|leaderZNode
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Found new leader for znode: "
operator|+
name|leaderZNode
argument_list|)
expr_stmt|;
name|leaderExists
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Leader change, but no new leader found"
argument_list|)
expr_stmt|;
name|leaderExists
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|leaderExists
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
name|watcher
operator|.
name|abort
argument_list|(
literal|"ZooKeeper error checking for leader znode"
argument_list|,
name|ke
argument_list|)
expr_stmt|;
name|candidate
operator|.
name|stop
argument_list|(
literal|"ZooKeeper error checking for leader: "
operator|+
name|ke
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Blocks until this instance has claimed the leader ZNode in ZooKeeper    */
specifier|public
name|void
name|waitToBecomeLeader
parameter_list|()
block|{
while|while
condition|(
operator|!
name|candidate
operator|.
name|isStopped
argument_list|()
condition|)
block|{
try|try
block|{
if|if
condition|(
name|ZKUtil
operator|.
name|createEphemeralNodeAndWatch
argument_list|(
name|watcher
argument_list|,
name|leaderZNode
argument_list|,
name|nodeId
argument_list|)
condition|)
block|{
comment|// claimed the leader znode
name|leaderExists
operator|.
name|set
argument_list|(
literal|true
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
literal|"Claimed the leader znode as '"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|nodeId
argument_list|)
operator|+
literal|"'"
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
comment|// if claiming the node failed, there should be another existing node
name|byte
index|[]
name|currentId
init|=
name|ZKUtil
operator|.
name|getDataAndWatch
argument_list|(
name|watcher
argument_list|,
name|leaderZNode
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentId
operator|!=
literal|null
operator|&&
name|Bytes
operator|.
name|equals
argument_list|(
name|currentId
argument_list|,
name|nodeId
argument_list|)
condition|)
block|{
comment|// claimed with our ID, but we didn't grab it, possibly restarted?
name|LOG
operator|.
name|info
argument_list|(
literal|"Found existing leader with our ID ("
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|nodeId
argument_list|)
operator|+
literal|"), removing"
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|watcher
argument_list|,
name|leaderZNode
argument_list|)
expr_stmt|;
name|leaderExists
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Found existing leader with ID: "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|nodeId
argument_list|)
argument_list|)
expr_stmt|;
name|leaderExists
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
name|watcher
operator|.
name|abort
argument_list|(
literal|"Unexpected error from ZK, stopping candidate"
argument_list|,
name|ke
argument_list|)
expr_stmt|;
name|candidate
operator|.
name|stop
argument_list|(
literal|"Unexpected error from ZK: "
operator|+
name|ke
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// wait for next chance
synchronized|synchronized
init|(
name|leaderExists
init|)
block|{
while|while
condition|(
name|leaderExists
operator|.
name|get
argument_list|()
operator|&&
operator|!
name|candidate
operator|.
name|isStopped
argument_list|()
condition|)
block|{
try|try
block|{
name|leaderExists
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Interrupted waiting on leader"
argument_list|,
name|ie
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
comment|/**    * Removes the leader znode, if it is currently claimed by this instance.    */
specifier|public
name|void
name|stepDownAsLeader
parameter_list|()
block|{
try|try
block|{
synchronized|synchronized
init|(
name|leaderExists
init|)
block|{
if|if
condition|(
operator|!
name|leaderExists
operator|.
name|get
argument_list|()
condition|)
block|{
return|return;
block|}
name|byte
index|[]
name|leaderId
init|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|watcher
argument_list|,
name|leaderZNode
argument_list|)
decl_stmt|;
if|if
condition|(
name|leaderId
operator|!=
literal|null
operator|&&
name|Bytes
operator|.
name|equals
argument_list|(
name|nodeId
argument_list|,
name|leaderId
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Stepping down as leader"
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteNodeFailSilent
argument_list|(
name|watcher
argument_list|,
name|leaderZNode
argument_list|)
expr_stmt|;
name|leaderExists
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Not current leader, no need to step down"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
name|watcher
operator|.
name|abort
argument_list|(
literal|"Unhandled zookeeper exception removing leader node"
argument_list|,
name|ke
argument_list|)
expr_stmt|;
name|candidate
operator|.
name|stop
argument_list|(
literal|"Unhandled zookeeper exception removing leader node: "
operator|+
name|ke
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|boolean
name|hasLeader
parameter_list|()
block|{
return|return
name|leaderExists
operator|.
name|get
argument_list|()
return|;
block|}
block|}
end_class

end_unit

