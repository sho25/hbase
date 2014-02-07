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
name|SortedMap
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
name|TreeMap
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|generated
operator|.
name|ZooKeeperProtos
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
name|ZooKeeperWatcher
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
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_comment
comment|/**  * This class provides an implementation of the ReplicationQueues interface using Zookeeper. The  * base znode that this class works at is the myQueuesZnode. The myQueuesZnode contains a list of  * all outstanding HLog files on this region server that need to be replicated. The myQueuesZnode is  * the regionserver name (a concatenation of the region server’s hostname, client port and start  * code). For example:  *  * /hbase/replication/rs/hostname.example.org,6020,1234  *  * Within this znode, the region server maintains a set of HLog replication queues. These queues are  * represented by child znodes named using there give queue id. For example:  *  * /hbase/replication/rs/hostname.example.org,6020,1234/1  * /hbase/replication/rs/hostname.example.org,6020,1234/2  *  * Each queue has one child znode for every HLog that still needs to be replicated. The value of  * these HLog child znodes is the latest position that has been replicated. This position is updated  * every time a HLog entry is replicated. For example:  *  * /hbase/replication/rs/hostname.example.org,6020,1234/1/23522342.23422 [VALUE: 254]  */
end_comment

begin_class
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
comment|/** Name of znode we use to lock during failover */
specifier|private
specifier|final
specifier|static
name|String
name|RS_LOCK_ZNODE
init|=
literal|"lock"
decl_stmt|;
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
name|ReplicationQueuesZKImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|ReplicationQueuesZKImpl
parameter_list|(
specifier|final
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
name|ZKUtil
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
name|ZKUtil
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
name|ZKUtil
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
name|ZKUtil
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
name|ZKUtil
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
name|ZKUtil
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
literal|"Failed to remove hlog from queue (queueId="
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
name|ZKUtil
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
name|ZKUtil
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
literal|"Failed to write replication hlog position (filename="
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
name|ZKUtil
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
name|ZKUtil
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
name|parseHLogPositionFrom
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
literal|"Failed to parse HLogPosition for queueId="
operator|+
name|queueId
operator|+
literal|" and hlog="
operator|+
name|filename
operator|+
literal|"znode content, continuing."
argument_list|)
expr_stmt|;
block|}
comment|// if we can not parse the position, start at the beginning of the hlog file
comment|// again
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isThisOurZnode
parameter_list|(
name|String
name|znode
parameter_list|)
block|{
return|return
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|queuesZNode
argument_list|,
name|znode
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
name|SortedMap
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
name|claimQueues
parameter_list|(
name|String
name|regionserverZnode
parameter_list|)
block|{
name|SortedMap
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
name|newQueues
init|=
operator|new
name|TreeMap
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|// check whether there is multi support. If yes, use it.
if|if
condition|(
name|conf
operator|.
name|getBoolean
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_USEMULTI
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Atomically moving "
operator|+
name|regionserverZnode
operator|+
literal|"'s hlogs to my queue"
argument_list|)
expr_stmt|;
name|newQueues
operator|=
name|copyQueuesFromRSUsingMulti
argument_list|(
name|regionserverZnode
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Moving "
operator|+
name|regionserverZnode
operator|+
literal|"'s hlogs to my queue"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|lockOtherRS
argument_list|(
name|regionserverZnode
argument_list|)
condition|)
block|{
return|return
name|newQueues
return|;
block|}
name|newQueues
operator|=
name|copyQueuesFromRS
argument_list|(
name|regionserverZnode
argument_list|)
expr_stmt|;
name|deleteAnotherRSQueues
argument_list|(
name|regionserverZnode
argument_list|)
expr_stmt|;
block|}
return|return
name|newQueues
return|;
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
name|ZKUtil
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
literal|"Failed to get list of hlogs for queueId="
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
return|;
block|}
comment|/**    * Try to set a lock in another region server's znode.    * @param znode the server names of the other server    * @return true if the lock was acquired, false in every other cases    */
specifier|private
name|boolean
name|lockOtherRS
parameter_list|(
name|String
name|znode
parameter_list|)
block|{
try|try
block|{
name|String
name|parent
init|=
name|ZKUtil
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
if|if
condition|(
name|parent
operator|.
name|equals
argument_list|(
name|this
operator|.
name|myQueuesZnode
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Won't lock because this is us, we're dead!"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|String
name|p
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|parent
argument_list|,
name|RS_LOCK_ZNODE
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|createAndWatch
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|p
argument_list|,
name|lockToByteArray
argument_list|(
name|this
operator|.
name|myQueuesZnode
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
comment|// This exception will pop up if the znode under which we're trying to
comment|// create the lock is already deleted by another region server, meaning
comment|// that the transfer already occurred.
comment|// NoNode => transfer is done and znodes are already deleted
comment|// NodeExists => lock znode already created by another RS
if|if
condition|(
name|e
operator|instanceof
name|KeeperException
operator|.
name|NoNodeException
operator|||
name|e
operator|instanceof
name|KeeperException
operator|.
name|NodeExistsException
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Won't transfer the queue,"
operator|+
literal|" another RS took care of it because of: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed lock other rs"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
comment|/**    * Delete all the replication queues for a given region server.    * @param regionserverZnode The znode of the region server to delete.    */
specifier|private
name|void
name|deleteAnotherRSQueues
parameter_list|(
name|String
name|regionserverZnode
parameter_list|)
block|{
name|String
name|fullpath
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|queuesZNode
argument_list|,
name|regionserverZnode
argument_list|)
decl_stmt|;
try|try
block|{
name|List
argument_list|<
name|String
argument_list|>
name|clusters
init|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|fullpath
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|cluster
range|:
name|clusters
control|)
block|{
comment|// No need to delete, it will be deleted later.
if|if
condition|(
name|cluster
operator|.
name|equals
argument_list|(
name|RS_LOCK_ZNODE
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|String
name|fullClusterPath
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|fullpath
argument_list|,
name|cluster
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|fullClusterPath
argument_list|)
expr_stmt|;
block|}
comment|// Finish cleaning up
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|fullpath
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|KeeperException
operator|.
name|NoNodeException
operator|||
name|e
operator|instanceof
name|KeeperException
operator|.
name|NotEmptyException
condition|)
block|{
comment|// Testing a special case where another region server was able to
comment|// create a lock just after we deleted it, but then was also able to
comment|// delete the RS znode before us or its lock znode is still there.
if|if
condition|(
name|e
operator|.
name|getPath
argument_list|()
operator|.
name|equals
argument_list|(
name|fullpath
argument_list|)
condition|)
block|{
return|return;
block|}
block|}
name|this
operator|.
name|abortable
operator|.
name|abort
argument_list|(
literal|"Failed to delete replication queues for region server: "
operator|+
name|regionserverZnode
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * It "atomically" copies all the hlogs queues from another region server and returns them all    * sorted per peer cluster (appended with the dead server's znode).    * @param znode pertaining to the region server to copy the queues from    * @return HLog queues sorted per peer cluster    */
specifier|private
name|SortedMap
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
name|copyQueuesFromRSUsingMulti
parameter_list|(
name|String
name|znode
parameter_list|)
block|{
name|SortedMap
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
name|queues
init|=
operator|new
name|TreeMap
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|// hbase/replication/rs/deadrs
name|String
name|deadRSZnodePath
init|=
name|ZKUtil
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
name|String
argument_list|>
name|peerIdsToProcess
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|ZKUtilOp
argument_list|>
name|listOfOps
init|=
operator|new
name|ArrayList
argument_list|<
name|ZKUtil
operator|.
name|ZKUtilOp
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
name|peerIdsToProcess
operator|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|deadRSZnodePath
argument_list|)
expr_stmt|;
if|if
condition|(
name|peerIdsToProcess
operator|==
literal|null
condition|)
return|return
name|queues
return|;
comment|// node already processed
for|for
control|(
name|String
name|peerId
range|:
name|peerIdsToProcess
control|)
block|{
name|ReplicationQueueInfo
name|replicationQueueInfo
init|=
operator|new
name|ReplicationQueueInfo
argument_list|(
name|peerId
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
name|peerId
operator|+
literal|" didn't exist, skipping the replay"
argument_list|)
expr_stmt|;
comment|// Protection against moving orphaned queues
continue|continue;
block|}
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
name|ZKUtil
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
name|ZKUtil
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
name|hlogs
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
name|hlogs
operator|==
literal|null
operator|||
name|hlogs
operator|.
name|size
argument_list|()
operator|==
literal|0
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
continue|continue;
comment|// empty log queue.
block|}
comment|// create the new cluster znode
name|SortedSet
argument_list|<
name|String
argument_list|>
name|logQueue
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|queues
operator|.
name|put
argument_list|(
name|newPeerId
argument_list|,
name|logQueue
argument_list|)
expr_stmt|;
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
name|hlog
range|:
name|hlogs
control|)
block|{
name|String
name|oldHlogZnode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|oldClusterZnode
argument_list|,
name|hlog
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
name|oldHlogZnode
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating "
operator|+
name|hlog
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
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|newPeerZnode
argument_list|,
name|hlog
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
comment|// add ops for deleting
name|listOfOps
operator|.
name|add
argument_list|(
name|ZKUtilOp
operator|.
name|deleteNodeFailSilent
argument_list|(
name|oldHlogZnode
argument_list|)
argument_list|)
expr_stmt|;
name|logQueue
operator|.
name|add
argument_list|(
name|hlog
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
block|}
comment|// add delete op for dead rs
name|listOfOps
operator|.
name|add
argument_list|(
name|ZKUtilOp
operator|.
name|deleteNodeFailSilent
argument_list|(
name|deadRSZnodePath
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|" The multi list size is: "
operator|+
name|listOfOps
operator|.
name|size
argument_list|()
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Atomically moved the dead regionserver logs. "
argument_list|)
expr_stmt|;
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
name|queues
operator|.
name|clear
argument_list|()
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
name|queues
operator|.
name|clear
argument_list|()
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
name|queues
return|;
block|}
comment|/**    * This methods copies all the hlogs queues from another region server and returns them all sorted    * per peer cluster (appended with the dead server's znode)    * @param znode server names to copy    * @return all hlogs for all peers of that cluster, null if an error occurred    */
specifier|private
name|SortedMap
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
name|copyQueuesFromRS
parameter_list|(
name|String
name|znode
parameter_list|)
block|{
comment|// TODO this method isn't atomic enough, we could start copying and then
comment|// TODO fail for some reason and we would end up with znodes we don't want.
name|SortedMap
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
name|queues
init|=
operator|new
name|TreeMap
argument_list|<
name|String
argument_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
name|String
name|nodePath
init|=
name|ZKUtil
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
name|String
argument_list|>
name|clusters
init|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|nodePath
argument_list|)
decl_stmt|;
comment|// We have a lock znode in there, it will count as one.
if|if
condition|(
name|clusters
operator|==
literal|null
operator|||
name|clusters
operator|.
name|size
argument_list|()
operator|<=
literal|1
condition|)
block|{
return|return
name|queues
return|;
block|}
comment|// The lock isn't a peer cluster, remove it
name|clusters
operator|.
name|remove
argument_list|(
name|RS_LOCK_ZNODE
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|cluster
range|:
name|clusters
control|)
block|{
name|ReplicationQueueInfo
name|replicationQueueInfo
init|=
operator|new
name|ReplicationQueueInfo
argument_list|(
name|cluster
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
name|cluster
operator|+
literal|" didn't exist, skipping the replay"
argument_list|)
expr_stmt|;
comment|// Protection against moving orphaned queues
continue|continue;
block|}
comment|// We add the name of the recovered RS to the new znode, we can even
comment|// do that for queues that were recovered 10 times giving a znode like
comment|// number-startcode-number-otherstartcode-number-anotherstartcode-etc
name|String
name|newCluster
init|=
name|cluster
operator|+
literal|"-"
operator|+
name|znode
decl_stmt|;
name|String
name|newClusterZnode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|myQueuesZnode
argument_list|,
name|newCluster
argument_list|)
decl_stmt|;
name|String
name|clusterPath
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|nodePath
argument_list|,
name|cluster
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|hlogs
init|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|clusterPath
argument_list|)
decl_stmt|;
comment|// That region server didn't have anything to replicate for this cluster
if|if
condition|(
name|hlogs
operator|==
literal|null
operator|||
name|hlogs
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
continue|continue;
block|}
name|ZKUtil
operator|.
name|createNodeIfNotExistsAndWatch
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|newClusterZnode
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
expr_stmt|;
name|SortedSet
argument_list|<
name|String
argument_list|>
name|logQueue
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|queues
operator|.
name|put
argument_list|(
name|newCluster
argument_list|,
name|logQueue
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|hlog
range|:
name|hlogs
control|)
block|{
name|String
name|z
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|clusterPath
argument_list|,
name|hlog
argument_list|)
decl_stmt|;
name|byte
index|[]
name|positionBytes
init|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|z
argument_list|)
decl_stmt|;
name|long
name|position
init|=
literal|0
decl_stmt|;
try|try
block|{
name|position
operator|=
name|ZKUtil
operator|.
name|parseHLogPositionFrom
argument_list|(
name|positionBytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|DeserializationException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed parse of hlog position from the following znode: "
operator|+
name|z
operator|+
literal|", Exception: "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating "
operator|+
name|hlog
operator|+
literal|" with data "
operator|+
name|position
argument_list|)
expr_stmt|;
name|String
name|child
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|newClusterZnode
argument_list|,
name|hlog
argument_list|)
decl_stmt|;
comment|// Position doesn't actually change, we are just deserializing it for
comment|// logging, so just use the already serialized version
name|ZKUtil
operator|.
name|createAndWatch
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|child
argument_list|,
name|positionBytes
argument_list|)
expr_stmt|;
name|logQueue
operator|.
name|add
argument_list|(
name|hlog
argument_list|)
expr_stmt|;
block|}
block|}
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
literal|"Copy queues from rs"
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
name|queues
return|;
block|}
comment|/**    * @param lockOwner    * @return Serialized protobuf of<code>lockOwner</code> with pb magic prefix prepended suitable    *         for use as content of an replication lock during region server fail over.    */
specifier|static
name|byte
index|[]
name|lockToByteArray
parameter_list|(
specifier|final
name|String
name|lockOwner
parameter_list|)
block|{
name|byte
index|[]
name|bytes
init|=
name|ZooKeeperProtos
operator|.
name|ReplicationLock
operator|.
name|newBuilder
argument_list|()
operator|.
name|setLockOwner
argument_list|(
name|lockOwner
argument_list|)
operator|.
name|build
argument_list|()
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
return|return
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|bytes
argument_list|)
return|;
block|}
block|}
end_class

end_unit

