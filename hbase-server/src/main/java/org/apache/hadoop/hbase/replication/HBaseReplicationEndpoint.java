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
name|replication
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
name|Collections
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
name|UUID
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
name|zookeeper
operator|.
name|ZKClusterId
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
name|KeeperException
operator|.
name|AuthFailedException
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
operator|.
name|ConnectionLossException
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
operator|.
name|SessionExpiredException
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
comment|/**  * A {@link BaseReplicationEndpoint} for replication endpoints whose  * target cluster is an HBase cluster.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|HBaseReplicationEndpoint
extends|extends
name|BaseReplicationEndpoint
implements|implements
name|Abortable
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
name|HBaseReplicationEndpoint
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ZKWatcher
name|zkw
init|=
literal|null
decl_stmt|;
specifier|private
name|List
argument_list|<
name|ServerName
argument_list|>
name|regionServers
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
name|long
name|lastRegionServerUpdate
decl_stmt|;
specifier|protected
specifier|synchronized
name|void
name|disconnect
parameter_list|()
block|{
if|if
condition|(
name|zkw
operator|!=
literal|null
condition|)
block|{
name|zkw
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * A private method used to re-establish a zookeeper session with a peer cluster.    * @param ke    */
specifier|protected
name|void
name|reconnect
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
if|if
condition|(
name|ke
operator|instanceof
name|ConnectionLossException
operator|||
name|ke
operator|instanceof
name|SessionExpiredException
operator|||
name|ke
operator|instanceof
name|AuthFailedException
condition|)
block|{
name|String
name|clusterKey
init|=
name|ctx
operator|.
name|getPeerConfig
argument_list|()
operator|.
name|getClusterKey
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Lost the ZooKeeper connection for peer "
operator|+
name|clusterKey
argument_list|,
name|ke
argument_list|)
expr_stmt|;
try|try
block|{
name|reloadZkWatcher
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|io
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Creation of ZookeeperWatcher failed for peer "
operator|+
name|clusterKey
argument_list|,
name|io
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{
name|startAsync
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|stopAsync
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doStart
parameter_list|()
block|{
try|try
block|{
name|reloadZkWatcher
argument_list|()
expr_stmt|;
name|notifyStarted
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|notifyFailed
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doStop
parameter_list|()
block|{
name|disconnect
argument_list|()
expr_stmt|;
name|notifyStopped
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
comment|// Synchronize peer cluster connection attempts to avoid races and rate
comment|// limit connections when multiple replication sources try to connect to
comment|// the peer cluster. If the peer cluster is down we can get out of control
comment|// over time.
specifier|public
specifier|synchronized
name|UUID
name|getPeerUUID
parameter_list|()
block|{
name|UUID
name|peerUUID
init|=
literal|null
decl_stmt|;
try|try
block|{
name|peerUUID
operator|=
name|ZKClusterId
operator|.
name|getUUIDForCluster
argument_list|(
name|zkw
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
name|reconnect
argument_list|(
name|ke
argument_list|)
expr_stmt|;
block|}
return|return
name|peerUUID
return|;
block|}
comment|/**    * Get the ZK connection to this peer    * @return zk connection    */
specifier|protected
specifier|synchronized
name|ZKWatcher
name|getZkw
parameter_list|()
block|{
return|return
name|zkw
return|;
block|}
comment|/**    * Closes the current ZKW (if not null) and creates a new one    * @throws IOException If anything goes wrong connecting    */
specifier|synchronized
name|void
name|reloadZkWatcher
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|zkw
operator|!=
literal|null
condition|)
name|zkw
operator|.
name|close
argument_list|()
expr_stmt|;
name|zkw
operator|=
operator|new
name|ZKWatcher
argument_list|(
name|ctx
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|"connection to cluster: "
operator|+
name|ctx
operator|.
name|getPeerId
argument_list|()
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|getZkw
argument_list|()
operator|.
name|registerListener
argument_list|(
operator|new
name|PeerRegionServerListener
argument_list|(
name|this
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"The HBaseReplicationEndpoint corresponding to peer "
operator|+
name|ctx
operator|.
name|getPeerId
argument_list|()
operator|+
literal|" was aborted for the following reason(s):"
operator|+
name|why
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
comment|// Currently this is never "Aborted", we just log when the abort method is called.
return|return
literal|false
return|;
block|}
comment|/**    * Get the list of all the region servers from the specified peer    * @param zkw zk connection to use    * @return list of region server addresses or an empty list if the slave is unavailable    */
specifier|protected
specifier|static
name|List
argument_list|<
name|ServerName
argument_list|>
name|fetchSlavesAddresses
parameter_list|(
name|ZKWatcher
name|zkw
parameter_list|)
throws|throws
name|KeeperException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|children
init|=
name|ZKUtil
operator|.
name|listChildrenAndWatchForNewChildren
argument_list|(
name|zkw
argument_list|,
name|zkw
operator|.
name|getZNodePaths
argument_list|()
operator|.
name|rsZNode
argument_list|)
decl_stmt|;
if|if
condition|(
name|children
operator|==
literal|null
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
name|List
argument_list|<
name|ServerName
argument_list|>
name|addresses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|children
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|child
range|:
name|children
control|)
block|{
name|addresses
operator|.
name|add
argument_list|(
name|ServerName
operator|.
name|parseServerName
argument_list|(
name|child
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|addresses
return|;
block|}
comment|/**    * Get a list of all the addresses of all the region servers    * for this peer cluster    * @return list of addresses    */
comment|// Synchronize peer cluster connection attempts to avoid races and rate
comment|// limit connections when multiple replication sources try to connect to
comment|// the peer cluster. If the peer cluster is down we can get out of control
comment|// over time.
specifier|public
specifier|synchronized
name|List
argument_list|<
name|ServerName
argument_list|>
name|getRegionServers
parameter_list|()
block|{
try|try
block|{
name|setRegionServers
argument_list|(
name|fetchSlavesAddresses
argument_list|(
name|this
operator|.
name|getZkw
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
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
literal|"Fetch slaves addresses failed"
argument_list|,
name|ke
argument_list|)
expr_stmt|;
block|}
name|reconnect
argument_list|(
name|ke
argument_list|)
expr_stmt|;
block|}
return|return
name|regionServers
return|;
block|}
comment|/**    * Set the list of region servers for that peer    * @param regionServers list of addresses for the region servers    */
specifier|public
specifier|synchronized
name|void
name|setRegionServers
parameter_list|(
name|List
argument_list|<
name|ServerName
argument_list|>
name|regionServers
parameter_list|)
block|{
name|this
operator|.
name|regionServers
operator|=
name|regionServers
expr_stmt|;
name|lastRegionServerUpdate
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
comment|/**    * Get the timestamp at which the last change occurred to the list of region servers to replicate    * to.    * @return The System.currentTimeMillis at the last time the list of peer region servers changed.    */
specifier|public
name|long
name|getLastRegionServerUpdate
parameter_list|()
block|{
return|return
name|lastRegionServerUpdate
return|;
block|}
comment|/**    * Tracks changes to the list of region servers in a peer's cluster.    */
specifier|public
specifier|static
class|class
name|PeerRegionServerListener
extends|extends
name|ZKListener
block|{
specifier|private
specifier|final
name|HBaseReplicationEndpoint
name|replicationEndpoint
decl_stmt|;
specifier|private
specifier|final
name|String
name|regionServerListNode
decl_stmt|;
specifier|public
name|PeerRegionServerListener
parameter_list|(
name|HBaseReplicationEndpoint
name|replicationPeer
parameter_list|)
block|{
name|super
argument_list|(
name|replicationPeer
operator|.
name|getZkw
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|replicationEndpoint
operator|=
name|replicationPeer
expr_stmt|;
name|this
operator|.
name|regionServerListNode
operator|=
name|replicationEndpoint
operator|.
name|getZkw
argument_list|()
operator|.
name|getZNodePaths
argument_list|()
operator|.
name|rsZNode
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
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
name|regionServerListNode
argument_list|)
condition|)
block|{
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Detected change to peer region servers, fetching updated list"
argument_list|)
expr_stmt|;
name|replicationEndpoint
operator|.
name|setRegionServers
argument_list|(
name|fetchSlavesAddresses
argument_list|(
name|replicationEndpoint
operator|.
name|getZkw
argument_list|()
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
name|LOG
operator|.
name|error
argument_list|(
literal|"Error reading slave addresses"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

