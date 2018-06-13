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
name|java
operator|.
name|io
operator|.
name|InterruptedIOException
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
name|Iterator
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
name|ExecutorService
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
name|Executors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|ServerMetrics
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
name|ServerMetricsBuilder
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
name|client
operator|.
name|VersionInfoUtil
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
name|Pair
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
name|ZKListener
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
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
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
name|shaded
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|RegionServerInfo
import|;
end_import

begin_comment
comment|/**  *<p>  * Tracks the online region servers via ZK.  *</p>  *<p>  * Handling of new RSs checking in is done via RPC. This class is only responsible for watching for  * expired nodes. It handles listening for changes in the RS node list. The only exception is when  * master restart, we will use the list fetched from zk to construct the initial set of live region  * servers.  *</p>  *<p>  * If an RS node gets deleted, this automatically handles calling of  * {@link ServerManager#expireServer(ServerName)}  *</p>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RegionServerTracker
extends|extends
name|ZKListener
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
name|RegionServerTracker
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|ServerName
argument_list|>
name|regionServers
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ServerManager
name|serverManager
decl_stmt|;
specifier|private
specifier|final
name|MasterServices
name|server
decl_stmt|;
comment|// As we need to send request to zk when processing the nodeChildrenChanged event, we'd better
comment|// move the operation to a single threaded thread pool in order to not block the zk event
comment|// processing since all the zk listener across HMaster will be called in one thread sequentially.
specifier|private
specifier|final
name|ExecutorService
name|executor
decl_stmt|;
specifier|public
name|RegionServerTracker
parameter_list|(
name|ZKWatcher
name|watcher
parameter_list|,
name|MasterServices
name|server
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
name|server
operator|=
name|server
expr_stmt|;
name|this
operator|.
name|serverManager
operator|=
name|serverManager
expr_stmt|;
name|executor
operator|=
name|Executors
operator|.
name|newSingleThreadExecutor
argument_list|(
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|setNameFormat
argument_list|(
literal|"RegionServerTracker-%d"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Pair
argument_list|<
name|ServerName
argument_list|,
name|RegionServerInfo
argument_list|>
name|getServerInfo
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|IOException
block|{
name|ServerName
name|serverName
init|=
name|ServerName
operator|.
name|parseServerName
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|String
name|nodePath
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|watcher
operator|.
name|getZNodePaths
argument_list|()
operator|.
name|rsZNode
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|byte
index|[]
name|data
decl_stmt|;
try|try
block|{
name|data
operator|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|watcher
argument_list|,
name|nodePath
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|InterruptedIOException
operator|)
operator|new
name|InterruptedIOException
argument_list|()
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
comment|// we should receive a children changed event later and then we will expire it, so we still
comment|// need to add it to the region server set.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Server node {} does not exist, already dead?"
argument_list|,
name|name
argument_list|)
expr_stmt|;
return|return
name|Pair
operator|.
name|newPair
argument_list|(
name|serverName
argument_list|,
literal|null
argument_list|)
return|;
block|}
if|if
condition|(
name|data
operator|.
name|length
operator|==
literal|0
operator|||
operator|!
name|ProtobufUtil
operator|.
name|isPBMagicPrefix
argument_list|(
name|data
argument_list|)
condition|)
block|{
comment|// this should not happen actually, unless we have bugs or someone has messed zk up.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Invalid data for region server node {} on zookeeper, data length = {}"
argument_list|,
name|name
argument_list|,
name|data
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|Pair
operator|.
name|newPair
argument_list|(
name|serverName
argument_list|,
literal|null
argument_list|)
return|;
block|}
name|RegionServerInfo
operator|.
name|Builder
name|builder
init|=
name|RegionServerInfo
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|int
name|magicLen
init|=
name|ProtobufUtil
operator|.
name|lengthOfPBMagic
argument_list|()
decl_stmt|;
name|ProtobufUtil
operator|.
name|mergeFrom
argument_list|(
name|builder
argument_list|,
name|data
argument_list|,
name|magicLen
argument_list|,
name|data
operator|.
name|length
operator|-
name|magicLen
argument_list|)
expr_stmt|;
return|return
name|Pair
operator|.
name|newPair
argument_list|(
name|serverName
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
comment|/**    *<p>    * Starts the tracking of online RegionServers.    *</p>    *<p>    * All RSs will be tracked after this method is called.    *</p>    */
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
synchronized|synchronized
init|(
name|this
init|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|servers
init|=
name|ZKUtil
operator|.
name|listChildrenAndWatchForNewChildren
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|getZNodePaths
argument_list|()
operator|.
name|rsZNode
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|n
range|:
name|servers
control|)
block|{
name|Pair
argument_list|<
name|ServerName
argument_list|,
name|RegionServerInfo
argument_list|>
name|pair
init|=
name|getServerInfo
argument_list|(
name|n
argument_list|)
decl_stmt|;
name|ServerName
name|serverName
init|=
name|pair
operator|.
name|getFirst
argument_list|()
decl_stmt|;
name|RegionServerInfo
name|info
init|=
name|pair
operator|.
name|getSecond
argument_list|()
decl_stmt|;
name|regionServers
operator|.
name|add
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
name|ServerMetrics
name|serverMetrics
init|=
name|info
operator|!=
literal|null
condition|?
name|ServerMetricsBuilder
operator|.
name|of
argument_list|(
name|serverName
argument_list|,
name|VersionInfoUtil
operator|.
name|getVersionNumber
argument_list|(
name|info
operator|.
name|getVersionInfo
argument_list|()
argument_list|)
argument_list|)
else|:
name|ServerMetricsBuilder
operator|.
name|of
argument_list|(
name|serverName
argument_list|)
decl_stmt|;
name|serverManager
operator|.
name|checkAndRecordNewServer
argument_list|(
name|serverName
argument_list|,
name|serverMetrics
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|executor
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|synchronized
name|void
name|refresh
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|names
decl_stmt|;
try|try
block|{
name|names
operator|=
name|ZKUtil
operator|.
name|listChildrenAndWatchForNewChildren
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|getZNodePaths
argument_list|()
operator|.
name|rsZNode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
comment|// here we need to abort as we failed to set watcher on the rs node which means that we can
comment|// not track the node deleted evetnt any more.
name|server
operator|.
name|abort
argument_list|(
literal|"Unexpected zk exception getting RS nodes"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
name|Set
argument_list|<
name|ServerName
argument_list|>
name|servers
init|=
name|names
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|ServerName
operator|::
name|parseServerName
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|ServerName
argument_list|>
name|iter
init|=
name|regionServers
operator|.
name|iterator
argument_list|()
init|;
name|iter
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|ServerName
name|sn
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|servers
operator|.
name|contains
argument_list|(
name|sn
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"RegionServer ephemeral node deleted, processing expiration [{}]"
argument_list|,
name|sn
argument_list|)
expr_stmt|;
name|serverManager
operator|.
name|expireServer
argument_list|(
name|sn
argument_list|)
expr_stmt|;
name|iter
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
comment|// here we do not need to parse the region server info as it is useless now, we only need the
comment|// server name.
name|boolean
name|newServerAdded
init|=
literal|false
decl_stmt|;
for|for
control|(
name|ServerName
name|sn
range|:
name|servers
control|)
block|{
if|if
condition|(
name|regionServers
operator|.
name|add
argument_list|(
name|sn
argument_list|)
condition|)
block|{
name|newServerAdded
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"RegionServer ephemeral node created, adding ["
operator|+
name|sn
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|newServerAdded
operator|&&
name|server
operator|.
name|isInitialized
argument_list|()
condition|)
block|{
comment|// Only call the check to move servers if a RegionServer was added to the cluster; in this
comment|// case it could be a server with a new version so it makes sense to run the check.
name|server
operator|.
name|checkIfShouldMoveSystemRegionAsync
argument_list|()
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
name|getZNodePaths
argument_list|()
operator|.
name|rsZNode
argument_list|)
operator|&&
operator|!
name|server
operator|.
name|isAborted
argument_list|()
operator|&&
operator|!
name|server
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|executor
operator|.
name|execute
argument_list|(
name|this
operator|::
name|refresh
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

