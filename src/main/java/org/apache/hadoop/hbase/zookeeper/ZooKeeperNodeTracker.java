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
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_comment
comment|/**  * Tracks the availability and value of a single ZooKeeper node.  *  *<p>Utilizes the {@link ZooKeeperListener} interface to get the necessary  * ZooKeeper events related to the node.  *  *<p>This is the base class used by trackers in both the Master and  * RegionServers.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|ZooKeeperNodeTracker
extends|extends
name|ZooKeeperListener
block|{
comment|/**    * Pass this if you do not want a timeout.    */
specifier|public
specifier|final
specifier|static
name|long
name|NO_TIMEOUT
init|=
operator|-
literal|1
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
comment|/**    * Constructs a new ZK node tracker.    *    *<p>After construction, use {@link #start} to kick off tracking.    *    * @param watcher    * @param node    * @param abortable    */
specifier|public
name|ZooKeeperNodeTracker
parameter_list|(
name|ZooKeeperWatcher
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
comment|/**    * Starts the tracking of the node in ZooKeeper.    *    *<p>Use {@link blockUntilAvailable} to block until the node is available    * or {@link getData} to get the data of the node if it is available.    */
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
name|NO_TIMEOUT
argument_list|)
return|;
block|}
comment|/**    * Gets the data of the node, blocking until the node is available or the    * specified timeout has elapsed.    *    * @param timeout maximum time to wait for the node data to be available,    *                in milliseconds.  Pass {@link #NO_TIMEOUT} for no timeout.    * @return data of the node    * @throws InterruptedException if the waiting thread is interrupted    */
specifier|public
specifier|synchronized
name|byte
index|[]
name|blockUntilAvailable
parameter_list|(
name|long
name|timeout
parameter_list|)
throws|throws
name|InterruptedException
block|{
if|if
condition|(
name|timeout
operator|!=
name|NO_TIMEOUT
operator|&&
name|timeout
operator|<
literal|0
condition|)
throw|throw
operator|new
name|IllegalArgumentException
argument_list|()
throw|;
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
while|while
condition|(
operator|(
name|remaining
operator|==
name|NO_TIMEOUT
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
name|remaining
operator|==
name|NO_TIMEOUT
condition|)
name|wait
argument_list|()
expr_stmt|;
else|else
name|wait
argument_list|(
name|remaining
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
name|data
return|;
block|}
comment|/**    * Gets the data of the node.    *    *<p>If the node is currently available, the most up-to-date known version of    * the data is returned.  If the node is not currently available, null is    * returned.    *    * @return data of the node, null if unavailable    */
specifier|public
specifier|synchronized
name|byte
index|[]
name|getData
parameter_list|()
block|{
return|return
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
block|}
end_class

end_unit

