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
comment|/**  * Tracks the online region servers via ZK.  *  *<p>Handling of new RSs checking in is done via RPC.  This class  * is only responsible for watching for expired nodes.  It handles  * listening for changes in the RS node list and watching each node.  *  *<p>If an RS node gets deleted, this automatically handles calling of  * {@link ServerManager#expireServer(ServerName)}  */
end_comment

begin_class
specifier|public
class|class
name|RegionServerTracker
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
name|RegionServerTracker
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|NavigableSet
argument_list|<
name|ServerName
argument_list|>
name|regionServers
init|=
operator|new
name|TreeSet
argument_list|<
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|ServerManager
name|serverManager
decl_stmt|;
specifier|private
name|Abortable
name|abortable
decl_stmt|;
specifier|public
name|RegionServerTracker
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
comment|/**    * Starts the tracking of online RegionServers.    *    *<p>All RSs will be tracked after this method is called.    *    * @throws KeeperException    * @throws IOException    */
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
name|rsZNode
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
name|regionServers
init|)
block|{
name|this
operator|.
name|regionServers
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
name|ServerName
name|sn
init|=
name|ServerName
operator|.
name|parseServerName
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
name|regionServers
operator|.
name|add
argument_list|(
name|sn
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
name|regionServers
init|)
block|{
name|this
operator|.
name|regionServers
operator|.
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
name|startsWith
argument_list|(
name|watcher
operator|.
name|rsZNode
argument_list|)
condition|)
block|{
name|String
name|serverName
init|=
name|ZKUtil
operator|.
name|getNodeName
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"RegionServer ephemeral node deleted, processing expiration ["
operator|+
name|serverName
operator|+
literal|"]"
argument_list|)
expr_stmt|;
name|ServerName
name|sn
init|=
name|ServerName
operator|.
name|parseServerName
argument_list|(
name|serverName
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|serverManager
operator|.
name|isServerOnline
argument_list|(
name|sn
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|serverName
operator|.
name|toString
argument_list|()
operator|+
literal|" is not online"
argument_list|)
expr_stmt|;
return|return;
block|}
name|remove
argument_list|(
name|sn
argument_list|)
expr_stmt|;
name|this
operator|.
name|serverManager
operator|.
name|expireServer
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
name|rsZNode
argument_list|)
condition|)
block|{
try|try
block|{
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
name|rsZNode
argument_list|)
decl_stmt|;
name|add
argument_list|(
name|servers
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
block|}
block|}
comment|/**    * Gets the online servers.    * @return list of online servers    */
specifier|public
name|List
argument_list|<
name|ServerName
argument_list|>
name|getOnlineServers
parameter_list|()
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|regionServers
init|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
argument_list|(
name|this
operator|.
name|regionServers
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

