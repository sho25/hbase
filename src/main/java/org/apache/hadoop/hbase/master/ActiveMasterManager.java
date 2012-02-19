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
name|master
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
name|Server
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
name|monitoring
operator|.
name|MonitoredTask
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
name|ZooKeeperListener
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
comment|/**  * Handles everything on master-side related to master election.  *  *<p>Listens and responds to ZooKeeper notifications on the master znode,  * both<code>nodeCreated</code> and<code>nodeDeleted</code>.  *  *<p>Contains blocking methods which will hold up backup masters, waiting  * for the active master to fail.  *  *<p>This class is instantiated in the HMaster constructor and the method  * #blockUntilBecomingActiveMaster() is called to wait until becoming  * the active master of the cluster.  */
end_comment

begin_class
class|class
name|ActiveMasterManager
extends|extends
name|ZooKeeperListener
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
name|ActiveMasterManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|AtomicBoolean
name|clusterHasActiveMaster
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ServerName
name|sn
decl_stmt|;
specifier|private
specifier|final
name|Server
name|master
decl_stmt|;
comment|/**    * @param watcher    * @param sn ServerName    * @param master In an instance of a Master.    */
name|ActiveMasterManager
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|ServerName
name|sn
parameter_list|,
name|Server
name|master
parameter_list|)
block|{
name|super
argument_list|(
name|watcher
argument_list|)
expr_stmt|;
name|this
operator|.
name|sn
operator|=
name|sn
expr_stmt|;
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
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
name|path
operator|.
name|equals
argument_list|(
name|watcher
operator|.
name|masterAddressZNode
argument_list|)
operator|&&
operator|!
name|master
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|handleMasterNodeChange
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
name|path
operator|.
name|equals
argument_list|(
name|watcher
operator|.
name|masterAddressZNode
argument_list|)
operator|&&
operator|!
name|master
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|handleMasterNodeChange
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Handle a change in the master node.  Doesn't matter whether this was called    * from a nodeCreated or nodeDeleted event because there are no guarantees    * that the current state of the master node matches the event at the time of    * our next ZK request.    *    *<p>Uses the watchAndCheckExists method which watches the master address node    * regardless of whether it exists or not.  If it does exist (there is an    * active master), it returns true.  Otherwise it returns false.    *    *<p>A watcher is set which guarantees that this method will get called again if    * there is another change in the master node.    */
specifier|private
name|void
name|handleMasterNodeChange
parameter_list|()
block|{
comment|// Watch the node and check if it exists.
try|try
block|{
synchronized|synchronized
init|(
name|clusterHasActiveMaster
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
name|watcher
operator|.
name|masterAddressZNode
argument_list|)
condition|)
block|{
comment|// A master node exists, there is an active master
name|LOG
operator|.
name|debug
argument_list|(
literal|"A master is now available"
argument_list|)
expr_stmt|;
name|clusterHasActiveMaster
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Node is no longer there, cluster does not have an active master
name|LOG
operator|.
name|debug
argument_list|(
literal|"No master available. Notifying waiting threads"
argument_list|)
expr_stmt|;
name|clusterHasActiveMaster
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// Notify any thread waiting to become the active master
name|clusterHasActiveMaster
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
name|master
operator|.
name|abort
argument_list|(
literal|"Received an unexpected KeeperException, aborting"
argument_list|,
name|ke
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Block until becoming the active master.    *    * Method blocks until there is not another active master and our attempt    * to become the new active master is successful.    *    * This also makes sure that we are watching the master znode so will be    * notified if another master dies.    * @param startupStatus     * @return True if no issue becoming active master else false if another    * master was running or if some other problem (zookeeper, stop flag has been    * set on this Master)    */
name|boolean
name|blockUntilBecomingActiveMaster
parameter_list|(
name|MonitoredTask
name|startupStatus
parameter_list|)
block|{
name|startupStatus
operator|.
name|setStatus
argument_list|(
literal|"Trying to register in ZK as active master"
argument_list|)
expr_stmt|;
name|boolean
name|cleanSetOfActiveMaster
init|=
literal|true
decl_stmt|;
comment|// Try to become the active master, watch if there is another master.
comment|// Write out our ServerName as versioned bytes.
try|try
block|{
name|String
name|backupZNode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|watcher
operator|.
name|backupMasterAddressesZNode
argument_list|,
name|this
operator|.
name|sn
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|ZKUtil
operator|.
name|createEphemeralNodeAndWatch
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|this
operator|.
name|watcher
operator|.
name|masterAddressZNode
argument_list|,
name|this
operator|.
name|sn
operator|.
name|getVersionedBytes
argument_list|()
argument_list|)
condition|)
block|{
comment|// If we were a backup master before, delete our ZNode from the backup
comment|// master directory since we are the active now
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleting ZNode for "
operator|+
name|backupZNode
operator|+
literal|" from backup master directory"
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteNodeFailSilent
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|backupZNode
argument_list|)
expr_stmt|;
comment|// We are the master, return
name|startupStatus
operator|.
name|setStatus
argument_list|(
literal|"Successfully registered as active master."
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterHasActiveMaster
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Master="
operator|+
name|this
operator|.
name|sn
argument_list|)
expr_stmt|;
return|return
name|cleanSetOfActiveMaster
return|;
block|}
name|cleanSetOfActiveMaster
operator|=
literal|false
expr_stmt|;
comment|// There is another active master running elsewhere or this is a restart
comment|// and the master ephemeral node has not expired yet.
name|this
operator|.
name|clusterHasActiveMaster
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|/*        * Add a ZNode for ourselves in the backup master directory since we are        * not the active master.        *        * If we become the active master later, ActiveMasterManager will delete        * this node explicitly.  If we crash before then, ZooKeeper will delete        * this node for us since it is ephemeral.        */
name|LOG
operator|.
name|info
argument_list|(
literal|"Adding ZNode for "
operator|+
name|backupZNode
operator|+
literal|" in backup master directory"
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|createEphemeralNodeAndWatch
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|backupZNode
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
expr_stmt|;
name|String
name|msg
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|ZKUtil
operator|.
name|getDataAndWatch
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|this
operator|.
name|watcher
operator|.
name|masterAddressZNode
argument_list|)
decl_stmt|;
if|if
condition|(
name|bytes
operator|==
literal|null
condition|)
block|{
name|msg
operator|=
operator|(
literal|"A master was detected, but went down before its address "
operator|+
literal|"could be read.  Attempting to become the next active master"
operator|)
expr_stmt|;
block|}
else|else
block|{
name|ServerName
name|currentMaster
init|=
name|ServerName
operator|.
name|parseVersionedServerName
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
if|if
condition|(
name|ServerName
operator|.
name|isSameHostnameAndPort
argument_list|(
name|currentMaster
argument_list|,
name|this
operator|.
name|sn
argument_list|)
condition|)
block|{
name|msg
operator|=
operator|(
literal|"Current master has this master's address, "
operator|+
name|currentMaster
operator|+
literal|"; master was restarted?  Waiting on znode "
operator|+
literal|"to expire..."
operator|)
expr_stmt|;
comment|// Hurry along the expiration of the znode.
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|this
operator|.
name|watcher
operator|.
name|masterAddressZNode
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|msg
operator|=
literal|"Another master is the active master, "
operator|+
name|currentMaster
operator|+
literal|"; waiting to become the next active master"
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|startupStatus
operator|.
name|setStatus
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
name|master
operator|.
name|abort
argument_list|(
literal|"Received an unexpected KeeperException, aborting"
argument_list|,
name|ke
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
synchronized|synchronized
init|(
name|this
operator|.
name|clusterHasActiveMaster
init|)
block|{
while|while
condition|(
name|this
operator|.
name|clusterHasActiveMaster
operator|.
name|get
argument_list|()
operator|&&
operator|!
name|this
operator|.
name|master
operator|.
name|isStopped
argument_list|()
condition|)
block|{
try|try
block|{
name|this
operator|.
name|clusterHasActiveMaster
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// We expect to be interrupted when a master dies, will fall out if so
name|LOG
operator|.
name|debug
argument_list|(
literal|"Interrupted waiting for master to die"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|this
operator|.
name|master
operator|.
name|isStopped
argument_list|()
condition|)
block|{
return|return
name|cleanSetOfActiveMaster
return|;
block|}
comment|// Try to become active master again now that there is no active master
name|blockUntilBecomingActiveMaster
argument_list|(
name|startupStatus
argument_list|)
expr_stmt|;
block|}
return|return
name|cleanSetOfActiveMaster
return|;
block|}
comment|/**    * @return True if cluster has an active master.    */
specifier|public
name|boolean
name|isActiveMaster
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
name|masterAddressZNode
argument_list|)
operator|>=
literal|0
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Received an unexpected KeeperException when checking "
operator|+
literal|"isActiveMaster : "
operator|+
name|ke
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
specifier|public
name|void
name|stop
parameter_list|()
block|{
try|try
block|{
comment|// If our address is in ZK, delete it on our way out
name|byte
index|[]
name|bytes
init|=
name|ZKUtil
operator|.
name|getDataAndWatch
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|masterAddressZNode
argument_list|)
decl_stmt|;
comment|// TODO: redo this to make it atomic (only added for tests)
name|ServerName
name|master
init|=
name|ServerName
operator|.
name|parseVersionedServerName
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
if|if
condition|(
name|master
operator|!=
literal|null
operator|&&
name|master
operator|.
name|equals
argument_list|(
name|this
operator|.
name|sn
argument_list|)
condition|)
block|{
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|masterAddressZNode
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
name|this
operator|.
name|watcher
operator|.
name|prefix
argument_list|(
literal|"Error deleting our own master address node"
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

