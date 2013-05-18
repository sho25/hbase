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
name|replication
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
name|exceptions
operator|.
name|DeserializationException
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
name|protobuf
operator|.
name|generated
operator|.
name|ZooKeeperProtos
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
name|ZooKeeperNodeTracker
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
name|NodeExistsException
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
import|;
end_import

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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_comment
comment|/**  * This class acts as a wrapper for all the objects used to identify and  * communicate with remote peers and is responsible for answering to expired  * sessions and re-establishing the ZK connections.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReplicationPeer
implements|implements
name|Abortable
implements|,
name|Closeable
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
name|ReplicationPeer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|String
name|clusterKey
decl_stmt|;
specifier|private
specifier|final
name|String
name|id
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
argument_list|<
name|ServerName
argument_list|>
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|peerEnabled
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
comment|// Cannot be final since a new object needs to be recreated when session fails
specifier|private
name|ZooKeeperWatcher
name|zkw
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|PeerStateTracker
name|peerStateTracker
decl_stmt|;
comment|/**    * Constructor that takes all the objects required to communicate with the    * specified peer, except for the region server addresses.    * @param conf configuration object to this peer    * @param key cluster key used to locate the peer    * @param id string representation of this peer's identifier    */
specifier|public
name|ReplicationPeer
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|key
parameter_list|,
name|String
name|id
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
name|clusterKey
operator|=
name|key
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|reloadZkWatcher
argument_list|()
expr_stmt|;
block|}
comment|/**    * start a state tracker to check whether this peer is enabled or not    *    * @param zookeeper zk watcher for the local cluster    * @param peerStateNode path to zk node which stores peer state    * @throws KeeperException    */
specifier|public
name|void
name|startStateTracker
parameter_list|(
name|ZooKeeperWatcher
name|zookeeper
parameter_list|,
name|String
name|peerStateNode
parameter_list|)
throws|throws
name|KeeperException
block|{
name|ensurePeerEnabled
argument_list|(
name|zookeeper
argument_list|,
name|peerStateNode
argument_list|)
expr_stmt|;
name|this
operator|.
name|peerStateTracker
operator|=
operator|new
name|PeerStateTracker
argument_list|(
name|peerStateNode
argument_list|,
name|zookeeper
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|peerStateTracker
operator|.
name|start
argument_list|()
expr_stmt|;
try|try
block|{
name|this
operator|.
name|readPeerStateZnode
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|DeserializationException
name|e
parameter_list|)
block|{
throw|throw
name|ZKUtil
operator|.
name|convert
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|readPeerStateZnode
parameter_list|()
throws|throws
name|DeserializationException
block|{
name|this
operator|.
name|peerEnabled
operator|.
name|set
argument_list|(
name|isStateEnabled
argument_list|(
name|this
operator|.
name|peerStateTracker
operator|.
name|getData
argument_list|(
literal|false
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the cluster key of that peer    * @return string consisting of zk ensemble addresses, client port    * and root znode    */
specifier|public
name|String
name|getClusterKey
parameter_list|()
block|{
return|return
name|clusterKey
return|;
block|}
comment|/**    * Get the state of this peer    * @return atomic boolean that holds the status    */
specifier|public
name|AtomicBoolean
name|getPeerEnabled
parameter_list|()
block|{
return|return
name|peerEnabled
return|;
block|}
comment|/**    * Get a list of all the addresses of all the region servers    * for this peer cluster    * @return list of addresses    */
specifier|public
name|List
argument_list|<
name|ServerName
argument_list|>
name|getRegionServers
parameter_list|()
block|{
return|return
name|regionServers
return|;
block|}
comment|/**    * Set the list of region servers for that peer    * @param regionServers list of addresses for the region servers    */
specifier|public
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
block|}
comment|/**    * Get the ZK connection to this peer    * @return zk connection    */
specifier|public
name|ZooKeeperWatcher
name|getZkw
parameter_list|()
block|{
return|return
name|zkw
return|;
block|}
comment|/**    * Get the identifier of this peer    * @return string representation of the id (short)    */
specifier|public
name|String
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
comment|/**    * Get the configuration object required to communicate with this peer    * @return configuration object    */
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|conf
return|;
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
name|fatal
argument_list|(
literal|"The ReplicationPeer coresponding to peer "
operator|+
name|clusterKey
operator|+
literal|" was aborted for the following reason(s):"
operator|+
name|why
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
comment|/**    * Closes the current ZKW (if not null) and creates a new one    * @throws IOException If anything goes wrong connecting    */
specifier|public
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
name|ZooKeeperWatcher
argument_list|(
name|conf
argument_list|,
literal|"connection to cluster: "
operator|+
name|id
argument_list|,
name|this
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
comment|// Currently the replication peer is never "Aborted", we just log when the
comment|// abort method is called.
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
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
block|{
name|zkw
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * @param bytes    * @return True if the passed in<code>bytes</code> are those of a pb serialized ENABLED state.    * @throws DeserializationException    */
specifier|private
specifier|static
name|boolean
name|isStateEnabled
parameter_list|(
specifier|final
name|byte
index|[]
name|bytes
parameter_list|)
throws|throws
name|DeserializationException
block|{
name|ZooKeeperProtos
operator|.
name|ReplicationState
operator|.
name|State
name|state
init|=
name|parseStateFrom
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
return|return
name|ZooKeeperProtos
operator|.
name|ReplicationState
operator|.
name|State
operator|.
name|ENABLED
operator|==
name|state
return|;
block|}
comment|/**    * @param bytes Content of a state znode.    * @return State parsed from the passed bytes.    * @throws DeserializationException    */
specifier|private
specifier|static
name|ZooKeeperProtos
operator|.
name|ReplicationState
operator|.
name|State
name|parseStateFrom
parameter_list|(
specifier|final
name|byte
index|[]
name|bytes
parameter_list|)
throws|throws
name|DeserializationException
block|{
name|ProtobufUtil
operator|.
name|expectPBMagicPrefix
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
name|int
name|pblen
init|=
name|ProtobufUtil
operator|.
name|lengthOfPBMagic
argument_list|()
decl_stmt|;
name|ZooKeeperProtos
operator|.
name|ReplicationState
operator|.
name|Builder
name|builder
init|=
name|ZooKeeperProtos
operator|.
name|ReplicationState
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|ZooKeeperProtos
operator|.
name|ReplicationState
name|state
decl_stmt|;
try|try
block|{
name|state
operator|=
name|builder
operator|.
name|mergeFrom
argument_list|(
name|bytes
argument_list|,
name|pblen
argument_list|,
name|bytes
operator|.
name|length
operator|-
name|pblen
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
return|return
name|state
operator|.
name|getState
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|DeserializationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Utility method to ensure an ENABLED znode is in place; if not present, we create it.    * @param zookeeper    * @param path Path to znode to check    * @return True if we created the znode.    * @throws NodeExistsException    * @throws KeeperException    */
specifier|private
specifier|static
name|boolean
name|ensurePeerEnabled
parameter_list|(
specifier|final
name|ZooKeeperWatcher
name|zookeeper
parameter_list|,
specifier|final
name|String
name|path
parameter_list|)
throws|throws
name|NodeExistsException
throws|,
name|KeeperException
block|{
if|if
condition|(
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|zookeeper
argument_list|,
name|path
argument_list|)
operator|==
operator|-
literal|1
condition|)
block|{
comment|// There is a race b/w PeerWatcher and ReplicationZookeeper#add method to create the
comment|// peer-state znode. This happens while adding a peer.
comment|// The peer state data is set as "ENABLED" by default.
name|ZKUtil
operator|.
name|createNodeIfNotExistsAndWatch
argument_list|(
name|zookeeper
argument_list|,
name|path
argument_list|,
name|ReplicationStateZKBase
operator|.
name|ENABLED_ZNODE_BYTES
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Tracker for state of this peer    */
specifier|public
class|class
name|PeerStateTracker
extends|extends
name|ZooKeeperNodeTracker
block|{
specifier|public
name|PeerStateTracker
parameter_list|(
name|String
name|peerStateZNode
parameter_list|,
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|Abortable
name|abortable
parameter_list|)
block|{
name|super
argument_list|(
name|watcher
argument_list|,
name|peerStateZNode
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
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
name|super
operator|.
name|nodeDataChanged
argument_list|(
name|path
argument_list|)
expr_stmt|;
try|try
block|{
name|readPeerStateZnode
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|DeserializationException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed deserializing the content of "
operator|+
name|path
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

