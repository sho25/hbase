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
name|zookeeper
package|;
end_package

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
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_comment
comment|/**  * Tracks the availability and value of a single ZooKeeper node.  *  *<p>Utilizes the {@link ZKListener} interface to get the necessary  * ZooKeeper events related to the node.  *  *<p>This is the base class used by trackers in both the Master and  * RegionServers.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|ZKNodeTracker
extends|extends
name|ZKListener
block|{
comment|// LOG is being used in subclasses, hence keeping it protected
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ZKNodeTracker
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Path of node being tracked */
specifier|protected
specifier|final
name|String
name|node
decl_stmt|;
comment|/** Data of the node being tracked */
specifier|private
name|byte
index|[]
name|data
decl_stmt|;
comment|/** Used to abort if a fatal error occurs */
specifier|protected
specifier|final
name|Abortable
name|abortable
decl_stmt|;
specifier|private
name|boolean
name|stopped
init|=
literal|false
decl_stmt|;
comment|/**    * Constructs a new ZK node tracker.    *    *<p>After construction, use {@link #start} to kick off tracking.    *    * @param watcher    * @param node    * @param abortable    */
specifier|public
name|ZKNodeTracker
parameter_list|(
name|ZKWatcher
name|watcher
parameter_list|,
name|String
name|node
parameter_list|,
name|Abortable
name|abortable
parameter_list|)
block|{
name|super
argument_list|(
name|watcher
argument_list|)
expr_stmt|;
name|this
operator|.
name|node
operator|=
name|node
expr_stmt|;
name|this
operator|.
name|abortable
operator|=
name|abortable
expr_stmt|;
name|this
operator|.
name|data
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Starts the tracking of the node in ZooKeeper.    *    *<p>Use {@link #blockUntilAvailable()} to block until the node is available    * or {@link #getData(boolean)} to get the data of the node if it is available.    */
specifier|public
specifier|synchronized
name|void
name|start
parameter_list|()
block|{
name|this
operator|.
name|watcher
operator|.
name|registerListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
try|try
block|{
if|if
condition|(
name|ZKUtil
operator|.
name|watchAndCheckExists
argument_list|(
name|watcher
argument_list|,
name|node
argument_list|)
condition|)
block|{
name|byte
index|[]
name|data
init|=
name|ZKUtil
operator|.
name|getDataAndWatch
argument_list|(
name|watcher
argument_list|,
name|node
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|data
operator|=
name|data
expr_stmt|;
block|}
else|else
block|{
comment|// It existed but now does not, try again to ensure a watch is set
name|LOG
operator|.
name|debug
argument_list|(
literal|"Try starting again because there is no data from "
operator|+
name|node
argument_list|)
expr_stmt|;
name|start
argument_list|()
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
name|abortable
operator|.
name|abort
argument_list|(
literal|"Unexpected exception during initialization, aborting"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|synchronized
name|void
name|stop
parameter_list|()
block|{
name|this
operator|.
name|stopped
operator|=
literal|true
expr_stmt|;
name|notifyAll
argument_list|()
expr_stmt|;
block|}
comment|/**    * Gets the data of the node, blocking until the node is available.    *    * @return data of the node    * @throws InterruptedException if the waiting thread is interrupted    */
specifier|public
specifier|synchronized
name|byte
index|[]
name|blockUntilAvailable
parameter_list|()
throws|throws
name|InterruptedException
block|{
return|return
name|blockUntilAvailable
argument_list|(
literal|0
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Gets the data of the node, blocking until the node is available or the    * specified timeout has elapsed.    *    * @param timeout maximum time to wait for the node data to be available,    * n milliseconds.  Pass 0 for no timeout.    * @return data of the node    * @throws InterruptedException if the waiting thread is interrupted    */
specifier|public
specifier|synchronized
name|byte
index|[]
name|blockUntilAvailable
parameter_list|(
name|long
name|timeout
parameter_list|,
name|boolean
name|refresh
parameter_list|)
throws|throws
name|InterruptedException
block|{
if|if
condition|(
name|timeout
operator|<
literal|0
condition|)
throw|throw
operator|new
name|IllegalArgumentException
argument_list|()
throw|;
name|boolean
name|notimeout
init|=
name|timeout
operator|==
literal|0
decl_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|remaining
init|=
name|timeout
decl_stmt|;
if|if
condition|(
name|refresh
condition|)
block|{
try|try
block|{
comment|// This does not create a watch if the node does not exists
name|this
operator|.
name|data
operator|=
name|ZKUtil
operator|.
name|getDataAndWatch
argument_list|(
name|watcher
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
comment|// We use to abort here, but in some cases the abort is ignored (
comment|//  (empty Abortable), so it's better to log...
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unexpected exception handling blockUntilAvailable"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|abortable
operator|.
name|abort
argument_list|(
literal|"Unexpected exception handling blockUntilAvailable"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|boolean
name|nodeExistsChecked
init|=
operator|(
operator|!
name|refresh
operator|||
name|data
operator|!=
literal|null
operator|)
decl_stmt|;
while|while
condition|(
operator|!
name|this
operator|.
name|stopped
operator|&&
operator|(
name|notimeout
operator|||
name|remaining
operator|>
literal|0
operator|)
operator|&&
name|this
operator|.
name|data
operator|==
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|nodeExistsChecked
condition|)
block|{
try|try
block|{
name|nodeExistsChecked
operator|=
operator|(
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|watcher
argument_list|,
name|node
argument_list|)
operator|!=
operator|-
literal|1
operator|)
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
literal|"Got exception while trying to check existence in  ZooKeeper"
operator|+
literal|" of the node: "
operator|+
name|node
operator|+
literal|", retrying if timeout not reached"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
comment|// It did not exists, and now it does.
if|if
condition|(
name|nodeExistsChecked
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Node "
operator|+
name|node
operator|+
literal|" now exists, resetting a watcher"
argument_list|)
expr_stmt|;
try|try
block|{
comment|// This does not create a watch if the node does not exists
name|this
operator|.
name|data
operator|=
name|ZKUtil
operator|.
name|getDataAndWatch
argument_list|(
name|watcher
argument_list|,
name|node
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
name|warn
argument_list|(
literal|"Unexpected exception handling blockUntilAvailable"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|abortable
operator|.
name|abort
argument_list|(
literal|"Unexpected exception handling blockUntilAvailable"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// We expect a notification; but we wait with a
comment|//  a timeout to lower the impact of a race condition if any
name|wait
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|remaining
operator|=
name|timeout
operator|-
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
expr_stmt|;
block|}
return|return
name|this
operator|.
name|data
return|;
block|}
comment|/**    * Gets the data of the node.    *    *<p>If the node is currently available, the most up-to-date known version of    * the data is returned.  If the node is not currently available, null is    * returned.    * @param refresh whether to refresh the data by calling ZK directly.    * @return data of the node, null if unavailable    */
specifier|public
specifier|synchronized
name|byte
index|[]
name|getData
parameter_list|(
name|boolean
name|refresh
parameter_list|)
block|{
if|if
condition|(
name|refresh
condition|)
block|{
try|try
block|{
name|this
operator|.
name|data
operator|=
name|ZKUtil
operator|.
name|getDataAndWatch
argument_list|(
name|watcher
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|abortable
operator|.
name|abort
argument_list|(
literal|"Unexpected exception handling getData"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|this
operator|.
name|data
return|;
block|}
specifier|public
name|String
name|getNode
parameter_list|()
block|{
return|return
name|this
operator|.
name|node
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|nodeCreated
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
operator|!
name|path
operator|.
name|equals
argument_list|(
name|node
argument_list|)
condition|)
return|return;
try|try
block|{
name|byte
index|[]
name|data
init|=
name|ZKUtil
operator|.
name|getDataAndWatch
argument_list|(
name|watcher
argument_list|,
name|node
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|data
operator|=
name|data
expr_stmt|;
name|notifyAll
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|nodeDeleted
argument_list|(
name|path
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
name|abortable
operator|.
name|abort
argument_list|(
literal|"Unexpected exception handling nodeCreated event"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|nodeDeleted
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|path
operator|.
name|equals
argument_list|(
name|node
argument_list|)
condition|)
block|{
try|try
block|{
if|if
condition|(
name|ZKUtil
operator|.
name|watchAndCheckExists
argument_list|(
name|watcher
argument_list|,
name|node
argument_list|)
condition|)
block|{
name|nodeCreated
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|data
operator|=
literal|null
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|abortable
operator|.
name|abort
argument_list|(
literal|"Unexpected exception handling nodeDeleted event"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|nodeDataChanged
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|path
operator|.
name|equals
argument_list|(
name|node
argument_list|)
condition|)
block|{
name|nodeCreated
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Checks if the baseznode set as per the property 'zookeeper.znode.parent'    * exists.    * @return true if baseznode exists.    *         false if doesnot exists.    */
specifier|public
name|boolean
name|checkIfBaseNodeAvailable
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|znodePaths
operator|.
name|baseZNode
argument_list|)
operator|==
operator|-
literal|1
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|abortable
operator|.
name|abort
argument_list|(
literal|"Exception while checking if basenode ("
operator|+
name|watcher
operator|.
name|znodePaths
operator|.
name|baseZNode
operator|+
literal|") exists in ZooKeeper."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
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
literal|"ZKNodeTracker{"
operator|+
literal|"node='"
operator|+
name|node
operator|+
literal|", stopped="
operator|+
name|stopped
operator|+
literal|'}'
return|;
block|}
block|}
end_class

end_unit
