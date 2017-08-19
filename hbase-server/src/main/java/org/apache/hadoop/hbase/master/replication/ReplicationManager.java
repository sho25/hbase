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
name|Collection
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
name|Map
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
name|regex
operator|.
name|Pattern
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
name|DoNotRetryIOException
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
name|ReplicationPeerNotFoundException
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
name|TableName
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
name|replication
operator|.
name|BaseReplicationEndpoint
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
name|replication
operator|.
name|ReplicationException
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
name|replication
operator|.
name|ReplicationFactory
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
name|replication
operator|.
name|ReplicationPeerConfig
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
name|replication
operator|.
name|ReplicationPeerDescription
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
name|replication
operator|.
name|ReplicationPeers
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
name|replication
operator|.
name|ReplicationQueuesClient
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
name|replication
operator|.
name|ReplicationQueuesClientArguments
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

begin_comment
comment|/**  * Manages and performs all replication admin operations.  * Used to add/remove a replication peer.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReplicationManager
block|{
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|ZooKeeperWatcher
name|zkw
decl_stmt|;
specifier|private
specifier|final
name|ReplicationQueuesClient
name|replicationQueuesClient
decl_stmt|;
specifier|private
specifier|final
name|ReplicationPeers
name|replicationPeers
decl_stmt|;
specifier|public
name|ReplicationManager
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ZooKeeperWatcher
name|zkw
parameter_list|,
name|Abortable
name|abortable
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|zkw
operator|=
name|zkw
expr_stmt|;
try|try
block|{
name|this
operator|.
name|replicationQueuesClient
operator|=
name|ReplicationFactory
operator|.
name|getReplicationQueuesClient
argument_list|(
operator|new
name|ReplicationQueuesClientArguments
argument_list|(
name|conf
argument_list|,
name|abortable
argument_list|,
name|zkw
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|replicationQueuesClient
operator|.
name|init
argument_list|()
expr_stmt|;
name|this
operator|.
name|replicationPeers
operator|=
name|ReplicationFactory
operator|.
name|getReplicationPeers
argument_list|(
name|zkw
argument_list|,
name|conf
argument_list|,
name|this
operator|.
name|replicationQueuesClient
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
name|this
operator|.
name|replicationPeers
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to construct ReplicationManager"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|addReplicationPeer
parameter_list|(
name|String
name|peerId
parameter_list|,
name|ReplicationPeerConfig
name|peerConfig
parameter_list|)
throws|throws
name|ReplicationException
throws|,
name|IOException
block|{
name|checkNamespacesAndTableCfsConfigConflict
argument_list|(
name|peerConfig
operator|.
name|getNamespaces
argument_list|()
argument_list|,
name|peerConfig
operator|.
name|getTableCFsMap
argument_list|()
argument_list|)
expr_stmt|;
name|checkConfiguredWALEntryFilters
argument_list|(
name|peerConfig
argument_list|)
expr_stmt|;
name|replicationPeers
operator|.
name|registerPeer
argument_list|(
name|peerId
argument_list|,
name|peerConfig
argument_list|)
expr_stmt|;
name|replicationPeers
operator|.
name|peerConnected
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|removeReplicationPeer
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|replicationPeers
operator|.
name|peerDisconnected
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|replicationPeers
operator|.
name|unregisterPeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|enableReplicationPeer
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|this
operator|.
name|replicationPeers
operator|.
name|enablePeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|disableReplicationPeer
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|this
operator|.
name|replicationPeers
operator|.
name|disablePeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ReplicationPeerConfig
name|getPeerConfig
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
throws|,
name|ReplicationPeerNotFoundException
block|{
name|ReplicationPeerConfig
name|peerConfig
init|=
name|replicationPeers
operator|.
name|getReplicationPeerConfig
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
if|if
condition|(
name|peerConfig
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ReplicationPeerNotFoundException
argument_list|(
name|peerId
argument_list|)
throw|;
block|}
return|return
name|peerConfig
return|;
block|}
specifier|public
name|void
name|updatePeerConfig
parameter_list|(
name|String
name|peerId
parameter_list|,
name|ReplicationPeerConfig
name|peerConfig
parameter_list|)
throws|throws
name|ReplicationException
throws|,
name|IOException
block|{
name|checkNamespacesAndTableCfsConfigConflict
argument_list|(
name|peerConfig
operator|.
name|getNamespaces
argument_list|()
argument_list|,
name|peerConfig
operator|.
name|getTableCFsMap
argument_list|()
argument_list|)
expr_stmt|;
name|checkConfiguredWALEntryFilters
argument_list|(
name|peerConfig
argument_list|)
expr_stmt|;
name|this
operator|.
name|replicationPeers
operator|.
name|updatePeerConfig
argument_list|(
name|peerId
argument_list|,
name|peerConfig
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|ReplicationPeerDescription
argument_list|>
name|listReplicationPeers
parameter_list|(
name|Pattern
name|pattern
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|List
argument_list|<
name|ReplicationPeerDescription
argument_list|>
name|peers
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|peerIds
init|=
name|replicationPeers
operator|.
name|getAllPeerIds
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|peerId
range|:
name|peerIds
control|)
block|{
if|if
condition|(
name|pattern
operator|==
literal|null
operator|||
operator|(
name|pattern
operator|!=
literal|null
operator|&&
name|pattern
operator|.
name|matcher
argument_list|(
name|peerId
argument_list|)
operator|.
name|matches
argument_list|()
operator|)
condition|)
block|{
name|peers
operator|.
name|add
argument_list|(
operator|new
name|ReplicationPeerDescription
argument_list|(
name|peerId
argument_list|,
name|replicationPeers
operator|.
name|getStatusOfPeerFromBackingStore
argument_list|(
name|peerId
argument_list|)
argument_list|,
name|replicationPeers
operator|.
name|getReplicationPeerConfig
argument_list|(
name|peerId
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|peers
return|;
block|}
comment|/**    * Set a namespace in the peer config means that all tables in this namespace    * will be replicated to the peer cluster.    *    * 1. If you already have set a namespace in the peer config, then you can't set any table    *    of this namespace to the peer config.    * 2. If you already have set a table in the peer config, then you can't set this table's    *    namespace to the peer config.    *    * @param namespaces    * @param tableCfs    * @throws ReplicationException    */
specifier|private
name|void
name|checkNamespacesAndTableCfsConfigConflict
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|namespaces
parameter_list|,
name|Map
argument_list|<
name|TableName
argument_list|,
name|?
extends|extends
name|Collection
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableCfs
parameter_list|)
throws|throws
name|ReplicationException
block|{
if|if
condition|(
name|namespaces
operator|==
literal|null
operator|||
name|namespaces
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|tableCfs
operator|==
literal|null
operator|||
name|tableCfs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|TableName
argument_list|,
name|?
extends|extends
name|Collection
argument_list|<
name|String
argument_list|>
argument_list|>
name|entry
range|:
name|tableCfs
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|TableName
name|table
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|namespaces
operator|.
name|contains
argument_list|(
name|table
operator|.
name|getNamespaceAsString
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"Table-cfs config conflict with namespaces config in peer"
argument_list|)
throw|;
block|}
block|}
block|}
specifier|private
name|void
name|checkConfiguredWALEntryFilters
parameter_list|(
name|ReplicationPeerConfig
name|peerConfig
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|filterCSV
init|=
name|peerConfig
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
name|BaseReplicationEndpoint
operator|.
name|REPLICATION_WALENTRYFILTER_CONFIG_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|filterCSV
operator|!=
literal|null
operator|&&
operator|!
name|filterCSV
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|String
index|[]
name|filters
init|=
name|filterCSV
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|filter
range|:
name|filters
control|)
block|{
try|try
block|{
name|Class
name|clazz
init|=
name|Class
operator|.
name|forName
argument_list|(
name|filter
argument_list|)
decl_stmt|;
name|Object
name|o
init|=
name|clazz
operator|.
name|newInstance
argument_list|()
decl_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Configured WALEntryFilter "
operator|+
name|filter
operator|+
literal|" could not be created. Failing add/update "
operator|+
literal|"peer operation."
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

