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
name|zookeeper
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
name|HashSet
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
name|concurrent
operator|.
name|CopyOnWriteArraySet
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
name|ZooKeeperConnectionException
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
name|client
operator|.
name|HConnectionManager
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
name|WatchedEvent
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
name|Watcher
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
name|ZooKeeper
import|;
end_import

begin_comment
comment|/**  * Acts as the single ZooKeeper Watcher.  One instance of this is instantiated  * for each Master, RegionServer, and client process.  *  *<p>This is the only class that implements {@link Watcher}.  Other internal  * classes which need to be notified of ZooKeeper events must register with  * the local instance of this watcher via {@link #registerListener}.  *  *<p>This class also holds and manages the connection to ZooKeeper.  Code to  * deal with connection related events and exceptions are handled here.  */
end_comment

begin_class
specifier|public
class|class
name|ZooKeeperWatcher
implements|implements
name|Watcher
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
name|ZooKeeperWatcher
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Identifiier for this watcher (for logging only).  Its made of the prefix
comment|// passed on construction and the zookeeper sessionid.
specifier|private
name|String
name|identifier
decl_stmt|;
comment|// zookeeper quorum
specifier|private
name|String
name|quorum
decl_stmt|;
comment|// zookeeper connection
specifier|private
name|ZooKeeper
name|zooKeeper
decl_stmt|;
comment|// abortable in case of zk failure
specifier|private
name|Abortable
name|abortable
decl_stmt|;
comment|// listeners to be notified
specifier|private
specifier|final
name|Set
argument_list|<
name|ZooKeeperListener
argument_list|>
name|listeners
init|=
operator|new
name|CopyOnWriteArraySet
argument_list|<
name|ZooKeeperListener
argument_list|>
argument_list|()
decl_stmt|;
comment|// set of unassigned nodes watched
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|unassignedNodes
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// node names
comment|// base znode for this cluster
specifier|public
name|String
name|baseZNode
decl_stmt|;
comment|// znode containing location of server hosting root region
specifier|public
name|String
name|rootServerZNode
decl_stmt|;
comment|// znode containing ephemeral nodes of the regionservers
specifier|public
name|String
name|rsZNode
decl_stmt|;
comment|// znode of currently active master
specifier|public
name|String
name|masterAddressZNode
decl_stmt|;
comment|// znode containing the current cluster state
specifier|public
name|String
name|clusterStateZNode
decl_stmt|;
comment|// znode used for region transitioning and assignment
specifier|public
name|String
name|assignmentZNode
decl_stmt|;
comment|// znode used for table disabling/enabling
specifier|public
name|String
name|tableZNode
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|Exception
name|constructorCaller
decl_stmt|;
comment|/**    * Instantiate a ZooKeeper connection and watcher.    * @param descriptor Descriptive string that is added to zookeeper sessionid    * and used as identifier for this instance.    * @throws IOException    * @throws ZooKeeperConnectionException    */
specifier|public
name|ZooKeeperWatcher
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|descriptor
parameter_list|,
name|Abortable
name|abortable
parameter_list|)
throws|throws
name|IOException
throws|,
name|ZooKeeperConnectionException
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
comment|// Capture a stack trace now.  Will print it out later if problem so we can
comment|// distingush amongst the myriad ZKWs.
try|try
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"ZKW CONSTRUCTOR STACK TRACE FOR DEBUGGING"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|this
operator|.
name|constructorCaller
operator|=
name|e
expr_stmt|;
block|}
name|this
operator|.
name|quorum
operator|=
name|ZKConfig
operator|.
name|getZKQuorumServersString
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// Identifier will get the sessionid appended later below down when we
comment|// handle the syncconnect event.
name|this
operator|.
name|identifier
operator|=
name|descriptor
expr_stmt|;
name|this
operator|.
name|abortable
operator|=
name|abortable
expr_stmt|;
name|setNodeNames
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|zooKeeper
operator|=
name|ZKUtil
operator|.
name|connect
argument_list|(
name|conf
argument_list|,
name|quorum
argument_list|,
name|this
argument_list|,
name|descriptor
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Create all the necessary "directories" of znodes
comment|// TODO: Move this to an init method somewhere so not everyone calls it?
comment|// The first call against zk can fail with connection loss.  Seems common.
comment|// Apparently this is recoverable.  Retry a while.
comment|// See http://wiki.apache.org/hadoop/ZooKeeper/ErrorHandling
comment|// TODO: Generalize out in ZKUtil.
name|long
name|wait
init|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.zookeeper.recoverable.waittime"
argument_list|,
literal|10000
argument_list|)
decl_stmt|;
name|long
name|finished
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
name|wait
decl_stmt|;
name|KeeperException
name|ke
init|=
literal|null
decl_stmt|;
do|do
block|{
try|try
block|{
name|ZKUtil
operator|.
name|createAndFailSilent
argument_list|(
name|this
argument_list|,
name|baseZNode
argument_list|)
expr_stmt|;
name|ke
operator|=
literal|null
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|ConnectionLossException
name|e
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
operator|&&
operator|(
name|isFinishedRetryingRecoverable
argument_list|(
name|finished
argument_list|)
operator|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Retrying zk create for another "
operator|+
operator|(
name|finished
operator|-
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|)
operator|+
literal|"ms; set 'hbase.zookeeper.recoverable.waittime' to change "
operator|+
literal|"wait time); "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ke
operator|=
name|e
expr_stmt|;
block|}
block|}
do|while
condition|(
name|isFinishedRetryingRecoverable
argument_list|(
name|finished
argument_list|)
condition|)
do|;
comment|// Convert connectionloss exception to ZKCE.
if|if
condition|(
name|ke
operator|!=
literal|null
condition|)
throw|throw
operator|new
name|ZooKeeperConnectionException
argument_list|(
name|ke
argument_list|)
throw|;
name|ZKUtil
operator|.
name|createAndFailSilent
argument_list|(
name|this
argument_list|,
name|assignmentZNode
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|createAndFailSilent
argument_list|(
name|this
argument_list|,
name|rsZNode
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|createAndFailSilent
argument_list|(
name|this
argument_list|,
name|tableZNode
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
name|prefix
argument_list|(
literal|"Unexpected KeeperException creating base node"
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|boolean
name|isFinishedRetryingRecoverable
parameter_list|(
specifier|final
name|long
name|finished
parameter_list|)
block|{
return|return
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|<
name|finished
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|this
operator|.
name|identifier
return|;
block|}
comment|/**    * Adds this instance's identifier as a prefix to the passed<code>str</code>    * @param str String to amend.    * @return A new string with this instance's identifier as prefix: e.g.    * if passed 'hello world', the returned string could be    */
specifier|public
name|String
name|prefix
parameter_list|(
specifier|final
name|String
name|str
parameter_list|)
block|{
return|return
name|this
operator|.
name|toString
argument_list|()
operator|+
literal|" "
operator|+
name|str
return|;
block|}
comment|/**    * Set the local variable node names using the specified configuration.    */
specifier|private
name|void
name|setNodeNames
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|baseZNode
operator|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_ZOOKEEPER_ZNODE_PARENT
argument_list|)
expr_stmt|;
name|rootServerZNode
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|baseZNode
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.rootserver"
argument_list|,
literal|"root-region-server"
argument_list|)
argument_list|)
expr_stmt|;
name|rsZNode
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|baseZNode
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.rs"
argument_list|,
literal|"rs"
argument_list|)
argument_list|)
expr_stmt|;
name|masterAddressZNode
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|baseZNode
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.master"
argument_list|,
literal|"master"
argument_list|)
argument_list|)
expr_stmt|;
name|clusterStateZNode
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|baseZNode
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.state"
argument_list|,
literal|"shutdown"
argument_list|)
argument_list|)
expr_stmt|;
name|assignmentZNode
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|baseZNode
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.unassigned"
argument_list|,
literal|"unassigned"
argument_list|)
argument_list|)
expr_stmt|;
name|tableZNode
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|baseZNode
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.tableEnableDisable"
argument_list|,
literal|"table"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Register the specified listener to receive ZooKeeper events.    * @param listener    */
specifier|public
name|void
name|registerListener
parameter_list|(
name|ZooKeeperListener
name|listener
parameter_list|)
block|{
name|listeners
operator|.
name|add
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the connection to ZooKeeper.    * @return connection reference to zookeeper    */
specifier|public
name|ZooKeeper
name|getZooKeeper
parameter_list|()
block|{
return|return
name|zooKeeper
return|;
block|}
comment|/**    * Get the quorum address of this instance.    * @returns quorum string of this zookeeper connection instance    */
specifier|public
name|String
name|getQuorum
parameter_list|()
block|{
return|return
name|quorum
return|;
block|}
comment|/**    * Method called from ZooKeeper for events and connection status.    *    * Valid events are passed along to listeners.  Connection status changes    * are dealt with locally.    */
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
name|WatchedEvent
name|event
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|prefix
argument_list|(
literal|"Received ZooKeeper Event, "
operator|+
literal|"type="
operator|+
name|event
operator|.
name|getType
argument_list|()
operator|+
literal|", "
operator|+
literal|"state="
operator|+
name|event
operator|.
name|getState
argument_list|()
operator|+
literal|", "
operator|+
literal|"path="
operator|+
name|event
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|event
operator|.
name|getType
argument_list|()
condition|)
block|{
comment|// If event type is NONE, this is a connection status change
case|case
name|None
case|:
block|{
name|connectionEvent
argument_list|(
name|event
argument_list|)
expr_stmt|;
break|break;
block|}
comment|// Otherwise pass along to the listeners
case|case
name|NodeCreated
case|:
block|{
for|for
control|(
name|ZooKeeperListener
name|listener
range|:
name|listeners
control|)
block|{
name|listener
operator|.
name|nodeCreated
argument_list|(
name|event
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
case|case
name|NodeDeleted
case|:
block|{
for|for
control|(
name|ZooKeeperListener
name|listener
range|:
name|listeners
control|)
block|{
name|listener
operator|.
name|nodeDeleted
argument_list|(
name|event
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
case|case
name|NodeDataChanged
case|:
block|{
for|for
control|(
name|ZooKeeperListener
name|listener
range|:
name|listeners
control|)
block|{
name|listener
operator|.
name|nodeDataChanged
argument_list|(
name|event
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
case|case
name|NodeChildrenChanged
case|:
block|{
for|for
control|(
name|ZooKeeperListener
name|listener
range|:
name|listeners
control|)
block|{
name|listener
operator|.
name|nodeChildrenChanged
argument_list|(
name|event
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
block|}
block|}
comment|// Connection management
comment|/**    * Called when there is a connection-related event via the Watcher callback.    *    * If Disconnected or Expired, this should shutdown the cluster.    *    * @param event    */
specifier|private
name|void
name|connectionEvent
parameter_list|(
name|WatchedEvent
name|event
parameter_list|)
block|{
switch|switch
condition|(
name|event
operator|.
name|getState
argument_list|()
condition|)
block|{
case|case
name|SyncConnected
case|:
comment|// Update our identifier.  Otherwise ignore.
name|LOG
operator|.
name|info
argument_list|(
name|this
operator|.
name|identifier
operator|+
literal|" connected"
argument_list|)
expr_stmt|;
comment|// Now, this callback can be invoked before the this.zookeeper is set.
comment|// Wait a little while.
name|long
name|finished
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
name|this
operator|.
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.zookeeper.watcher.sync.connected.wait"
argument_list|,
literal|2000
argument_list|)
decl_stmt|;
while|while
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|<
name|finished
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|zooKeeper
operator|!=
literal|null
condition|)
break|break;
block|}
if|if
condition|(
name|this
operator|.
name|zooKeeper
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"ZK is null on connection event -- see stack trace "
operator|+
literal|"for the stack trace when constructor was called on this zkw"
argument_list|,
name|this
operator|.
name|constructorCaller
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"ZK is null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|identifier
operator|=
name|this
operator|.
name|identifier
operator|+
literal|"-0x"
operator|+
name|Long
operator|.
name|toHexString
argument_list|(
name|this
operator|.
name|zooKeeper
operator|.
name|getSessionId
argument_list|()
argument_list|)
expr_stmt|;
break|break;
comment|// Abort the server if Disconnected or Expired
comment|// TODO: Åny reason to handle these two differently?
case|case
name|Disconnected
case|:
name|LOG
operator|.
name|info
argument_list|(
name|prefix
argument_list|(
literal|"Received Disconnected from ZooKeeper, ignoring"
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|Expired
case|:
name|String
name|msg
init|=
name|prefix
argument_list|(
name|this
operator|.
name|identifier
operator|+
literal|" received expired from "
operator|+
literal|"ZooKeeper, aborting"
argument_list|)
decl_stmt|;
comment|// TODO: One thought is to add call to ZooKeeperListener so say,
comment|// ZooKeperNodeTracker can zero out its data values.
if|if
condition|(
name|this
operator|.
name|abortable
operator|!=
literal|null
condition|)
name|this
operator|.
name|abortable
operator|.
name|abort
argument_list|(
name|msg
argument_list|,
literal|null
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
comment|/**    * Get the set of already watched unassigned nodes.    * @return    */
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getNodes
parameter_list|()
block|{
return|return
name|unassignedNodes
return|;
block|}
comment|/**    * Handles KeeperExceptions in client calls.    *    * This may be temporary but for now this gives one place to deal with these.    *    * TODO: Currently this method rethrows the exception to let the caller handle    *    * @param ke    * @throws KeeperException    */
specifier|public
name|void
name|keeperException
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
throws|throws
name|KeeperException
block|{
name|LOG
operator|.
name|error
argument_list|(
name|prefix
argument_list|(
literal|"Received unexpected KeeperException, re-throwing exception"
argument_list|)
argument_list|,
name|ke
argument_list|)
expr_stmt|;
throw|throw
name|ke
throw|;
block|}
comment|/**    * Handles InterruptedExceptions in client calls.    *    * This may be temporary but for now this gives one place to deal with these.    *    * TODO: Currently, this method does nothing.    *       Is this ever expected to happen?  Do we abort or can we let it run?    *       Maybe this should be logged as WARN?  It shouldn't happen?    *    * @param ie    */
specifier|public
name|void
name|interruptedException
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|prefix
argument_list|(
literal|"Received InterruptedException, doing nothing here"
argument_list|)
argument_list|,
name|ie
argument_list|)
expr_stmt|;
comment|// At least preserver interrupt.
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
comment|// no-op
block|}
comment|/**    * Close the connection to ZooKeeper.    * @throws InterruptedException    */
specifier|public
name|void
name|close
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
name|zooKeeper
operator|!=
literal|null
condition|)
block|{
name|zooKeeper
operator|.
name|close
argument_list|()
expr_stmt|;
comment|//        super.close();
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{     }
block|}
block|}
end_class

end_unit

