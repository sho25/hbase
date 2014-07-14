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
name|HashMap
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReplicationPeerZKImpl
implements|implements
name|ReplicationPeer
implements|,
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
name|ReplicationPeerZKImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ReplicationPeerConfig
name|peerConfig
decl_stmt|;
specifier|private
specifier|final
name|String
name|id
decl_stmt|;
specifier|private
specifier|volatile
name|PeerState
name|peerState
decl_stmt|;
specifier|private
specifier|volatile
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableCFs
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
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
specifier|private
name|TableCFsTracker
name|tableCFsTracker
decl_stmt|;
comment|/**    * Constructor that takes all the objects required to communicate with the    * specified peer, except for the region server addresses.    * @param conf configuration object to this peer    * @param id string representation of this peer's identifier    * @param peerConfig configuration for the replication peer    */
specifier|public
name|ReplicationPeerZKImpl
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|id
parameter_list|,
name|ReplicationPeerConfig
name|peerConfig
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|peerConfig
operator|=
name|peerConfig
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
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
name|peerState
operator|=
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
condition|?
name|PeerState
operator|.
name|ENABLED
else|:
name|PeerState
operator|.
name|DISABLED
expr_stmt|;
block|}
comment|/**    * start a table-cfs tracker to listen the (table, cf-list) map change    *    * @param zookeeper zk watcher for the local cluster    * @param tableCFsNode path to zk node which stores table-cfs    * @throws KeeperException    */
specifier|public
name|void
name|startTableCFsTracker
parameter_list|(
name|ZooKeeperWatcher
name|zookeeper
parameter_list|,
name|String
name|tableCFsNode
parameter_list|)
throws|throws
name|KeeperException
block|{
name|this
operator|.
name|tableCFsTracker
operator|=
operator|new
name|TableCFsTracker
argument_list|(
name|tableCFsNode
argument_list|,
name|zookeeper
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableCFsTracker
operator|.
name|start
argument_list|()
expr_stmt|;
name|this
operator|.
name|readTableCFsZnode
argument_list|()
expr_stmt|;
block|}
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|parseTableCFsFromConfig
parameter_list|(
name|String
name|tableCFsConfig
parameter_list|)
block|{
if|if
condition|(
name|tableCFsConfig
operator|==
literal|null
operator|||
name|tableCFsConfig
operator|.
name|trim
argument_list|()
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableCFsMap
init|=
literal|null
decl_stmt|;
comment|// TODO: This should be a PB object rather than a String to be parsed!! See HBASE-11393
comment|// parse out (table, cf-list) pairs from tableCFsConfig
comment|// format: "table1:cf1,cf2;table2:cfA,cfB"
name|String
index|[]
name|tables
init|=
name|tableCFsConfig
operator|.
name|split
argument_list|(
literal|";"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|tab
range|:
name|tables
control|)
block|{
comment|// 1 ignore empty table config
name|tab
operator|=
name|tab
operator|.
name|trim
argument_list|()
expr_stmt|;
if|if
condition|(
name|tab
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
continue|continue;
block|}
comment|// 2 split to "table" and "cf1,cf2"
comment|//   for each table: "table:cf1,cf2" or "table"
name|String
index|[]
name|pair
init|=
name|tab
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
name|String
name|tabName
init|=
name|pair
index|[
literal|0
index|]
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
name|pair
operator|.
name|length
operator|>
literal|2
operator|||
name|tabName
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"ignore invalid tableCFs setting: "
operator|+
name|tab
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// 3 parse "cf1,cf2" part to List<cf>
name|List
argument_list|<
name|String
argument_list|>
name|cfs
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|pair
operator|.
name|length
operator|==
literal|2
condition|)
block|{
name|String
index|[]
name|cfsList
init|=
name|pair
index|[
literal|1
index|]
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|cf
range|:
name|cfsList
control|)
block|{
name|String
name|cfName
init|=
name|cf
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
name|cfName
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|cfs
operator|==
literal|null
condition|)
block|{
name|cfs
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|cfs
operator|.
name|add
argument_list|(
name|cfName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// 4 put<table, List<cf>> to map
if|if
condition|(
name|tableCFsMap
operator|==
literal|null
condition|)
block|{
name|tableCFsMap
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|tableCFsMap
operator|.
name|put
argument_list|(
name|tabName
argument_list|,
name|cfs
argument_list|)
expr_stmt|;
block|}
return|return
name|tableCFsMap
return|;
block|}
specifier|private
name|void
name|readTableCFsZnode
parameter_list|()
block|{
name|String
name|currentTableCFs
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|tableCFsTracker
operator|.
name|getData
argument_list|(
literal|false
argument_list|)
argument_list|)
decl_stmt|;
name|this
operator|.
name|tableCFs
operator|=
name|parseTableCFsFromConfig
argument_list|(
name|currentTableCFs
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|PeerState
name|getPeerState
parameter_list|()
block|{
return|return
name|peerState
return|;
block|}
comment|/**    * Get the identifier of this peer    * @return string representation of the id (short)    */
annotation|@
name|Override
specifier|public
name|String
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
comment|/**    * Get the peer config object    * @return the ReplicationPeerConfig for this peer    */
annotation|@
name|Override
specifier|public
name|ReplicationPeerConfig
name|getPeerConfig
parameter_list|()
block|{
return|return
name|peerConfig
return|;
block|}
comment|/**    * Get the configuration object required to communicate with this peer    * @return configuration object    */
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
comment|/**    * Get replicable (table, cf-list) map of this peer    * @return the replicable (table, cf-list) map    */
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|getTableCFs
parameter_list|()
block|{
return|return
name|this
operator|.
name|tableCFs
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
name|peerConfig
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
comment|// TODO: stop zkw?
block|}
comment|/**    * Parse the raw data from ZK to get a peer's state    * @param bytes raw ZK data    * @return True if the passed in<code>bytes</code> are those of a pb serialized ENABLED state.    * @throws DeserializationException    */
specifier|public
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
comment|/**    * Tracker for (table, cf-list) map of this peer    */
specifier|public
class|class
name|TableCFsTracker
extends|extends
name|ZooKeeperNodeTracker
block|{
specifier|public
name|TableCFsTracker
parameter_list|(
name|String
name|tableCFsZNode
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
name|tableCFsZNode
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
name|readTableCFsZnode
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

