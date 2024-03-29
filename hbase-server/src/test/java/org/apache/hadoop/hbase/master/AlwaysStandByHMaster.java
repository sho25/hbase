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
name|master
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
name|Threads
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
name|MasterAddressTracker
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
comment|/**  * An implementation of HMaster that always runs as a stand by and never transitions to active.  */
end_comment

begin_class
specifier|public
class|class
name|AlwaysStandByHMaster
extends|extends
name|HMaster
block|{
comment|/**    * An implementation of ActiveMasterManager that never transitions it's master to active state. It    * always remains as a stand by master. With the master registry implementation (HBASE-18095) it    * is expected to have at least one active / standby master always running at any point in time    * since they serve as the gateway for client connections.    *    * With this implementation, tests can simulate the scenario of not having an active master yet    * the client connections to the cluster succeed.    */
specifier|private
specifier|static
class|class
name|AlwaysStandByMasterManager
extends|extends
name|ActiveMasterManager
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
name|AlwaysStandByMasterManager
operator|.
name|class
argument_list|)
decl_stmt|;
name|AlwaysStandByMasterManager
parameter_list|(
name|ZKWatcher
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
argument_list|,
name|sn
argument_list|,
name|master
argument_list|)
expr_stmt|;
block|}
comment|/**      * An implementation that never transitions to an active master.      */
name|boolean
name|blockUntilBecomingActiveMaster
parameter_list|(
name|int
name|checkInterval
parameter_list|,
name|MonitoredTask
name|startupStatus
parameter_list|)
block|{
while|while
condition|(
operator|!
operator|(
name|master
operator|.
name|isAborted
argument_list|()
operator|||
name|master
operator|.
name|isStopped
argument_list|()
operator|)
condition|)
block|{
name|startupStatus
operator|.
name|setStatus
argument_list|(
literal|"Forever looping to stay as a standby master."
argument_list|)
expr_stmt|;
try|try
block|{
name|activeMasterServerName
operator|=
literal|null
expr_stmt|;
try|try
block|{
if|if
condition|(
name|MasterAddressTracker
operator|.
name|getMasterAddress
argument_list|(
name|watcher
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|clusterHasActiveMaster
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|Threads
operator|.
name|sleepWithoutInterrupt
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// pass, we will get notified when some other active master creates the znode.
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|master
operator|.
name|abort
argument_list|(
literal|"Received an unexpected KeeperException, aborting"
argument_list|,
name|e
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
name|clusterHasActiveMaster
operator|.
name|get
argument_list|()
operator|&&
operator|!
name|master
operator|.
name|isStopped
argument_list|()
condition|)
block|{
try|try
block|{
name|clusterHasActiveMaster
operator|.
name|wait
argument_list|(
name|checkInterval
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// We expect to be interrupted when a master dies,
comment|//  will fall out if so
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
name|clusterShutDown
operator|.
name|get
argument_list|()
condition|)
block|{
name|this
operator|.
name|master
operator|.
name|stop
argument_list|(
literal|"Cluster went down before this master became active"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
specifier|public
name|AlwaysStandByHMaster
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|ActiveMasterManager
name|createActiveMasterManager
parameter_list|(
name|ZKWatcher
name|zk
parameter_list|,
name|ServerName
name|sn
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|Server
name|server
parameter_list|)
block|{
return|return
operator|new
name|AlwaysStandByMasterManager
argument_list|(
name|zk
argument_list|,
name|sn
argument_list|,
name|server
argument_list|)
return|;
block|}
block|}
end_class

end_unit

