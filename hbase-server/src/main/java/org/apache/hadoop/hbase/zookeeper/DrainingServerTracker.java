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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableSet
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
name|hbase
operator|.
name|classification
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
name|master
operator|.
name|ServerManager
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
comment|/**  * Tracks the list of draining region servers via ZK.  *  *<p>This class is responsible for watching for changes to the draining  * servers list.  It handles adds/deletes in the draining RS list and  * watches each node.  *  *<p>If an RS gets deleted from draining list, we call  * {@link ServerManager#removeServerFromDrainList(ServerName)}  *  *<p>If an RS gets added to the draining list, we add a watcher to it and call  * {@link ServerManager#addServerToDrainList(ServerName)}  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DrainingServerTracker
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
name|DrainingServerTracker
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ServerManager
name|serverManager
decl_stmt|;
specifier|private
specifier|final
name|NavigableSet
argument_list|<
name|ServerName
argument_list|>
name|drainingServers
init|=
operator|new
name|TreeSet
argument_list|<
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Abortable
name|abortable
decl_stmt|;
specifier|public
name|DrainingServerTracker
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|Abortable
name|abortable
parameter_list|,
name|ServerManager
name|serverManager
parameter_list|)
block|{
name|super
argument_list|(
name|watcher
argument_list|)
expr_stmt|;
name|this
operator|.
name|abortable
operator|=
name|abortable
expr_stmt|;
name|this
operator|.
name|serverManager
operator|=
name|serverManager
expr_stmt|;
block|}
comment|/**    * Starts the tracking of draining RegionServers.    *    *<p>All Draining RSs will be tracked after this method is called.    *    * @throws KeeperException    */
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|KeeperException
throws|,
name|IOException
block|{
name|watcher
operator|.
name|registerListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|servers
init|=
name|ZKUtil
operator|.
name|listChildrenAndWatchThem
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|drainingZNode
argument_list|)
decl_stmt|;
name|add
argument_list|(
name|servers
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|add
parameter_list|(
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|servers
parameter_list|)
throws|throws
name|IOException
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|drainingServers
init|)
block|{
name|this
operator|.
name|drainingServers
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|String
name|n
range|:
name|servers
control|)
block|{
specifier|final
name|ServerName
name|sn
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
name|ZKUtil
operator|.
name|getNodeName
argument_list|(
name|n
argument_list|)
argument_list|)
decl_stmt|;
name|this
operator|.
name|drainingServers
operator|.
name|add
argument_list|(
name|sn
argument_list|)
expr_stmt|;
name|this
operator|.
name|serverManager
operator|.
name|addServerToDrainList
argument_list|(
name|sn
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Draining RS node created, adding to list ["
operator|+
name|sn
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|remove
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|drainingServers
init|)
block|{
name|this
operator|.
name|drainingServers
operator|.
name|remove
argument_list|(
name|sn
argument_list|)
expr_stmt|;
name|this
operator|.
name|serverManager
operator|.
name|removeServerFromDrainList
argument_list|(
name|sn
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeDeleted
parameter_list|(
specifier|final
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|path
operator|.
name|startsWith
argument_list|(
name|watcher
operator|.
name|drainingZNode
argument_list|)
condition|)
block|{
specifier|final
name|ServerName
name|sn
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
name|ZKUtil
operator|.
name|getNodeName
argument_list|(
name|path
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Draining RS node deleted, removing from list ["
operator|+
name|sn
operator|+
literal|"]"
argument_list|)
expr_stmt|;
name|remove
argument_list|(
name|sn
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeChildrenChanged
parameter_list|(
specifier|final
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
name|drainingZNode
argument_list|)
condition|)
block|{
try|try
block|{
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|newNodes
init|=
name|ZKUtil
operator|.
name|listChildrenAndWatchThem
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|drainingZNode
argument_list|)
decl_stmt|;
name|add
argument_list|(
name|newNodes
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
literal|"Unexpected zk exception getting RS nodes"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|abortable
operator|.
name|abort
argument_list|(
literal|"Unexpected zk exception getting RS nodes"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

