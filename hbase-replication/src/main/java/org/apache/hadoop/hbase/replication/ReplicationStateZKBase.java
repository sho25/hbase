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
name|ByteArrayOutputStream
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
name|List
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
name|zookeeper
operator|.
name|ZKConfig
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
name|annotations
operator|.
name|VisibleForTesting
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
name|protobuf
operator|.
name|CodedOutputStream
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
name|ReplicationProtos
import|;
end_import

begin_comment
comment|/**  * This is a base class for maintaining replication state in zookeeper.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|ReplicationStateZKBase
block|{
comment|/**    * The name of the znode that contains the replication status of a remote slave (i.e. peer)    * cluster.    */
specifier|protected
specifier|final
name|String
name|peerStateNodeName
decl_stmt|;
comment|/** The name of the base znode that contains all replication state. */
specifier|protected
specifier|final
name|String
name|replicationZNode
decl_stmt|;
comment|/** The name of the znode that contains a list of all remote slave (i.e. peer) clusters. */
specifier|protected
specifier|final
name|String
name|peersZNode
decl_stmt|;
comment|/** The name of the znode that contains all replication queues */
specifier|protected
specifier|final
name|String
name|queuesZNode
decl_stmt|;
comment|/** The name of the znode that contains queues of hfile references to be replicated */
specifier|protected
specifier|final
name|String
name|hfileRefsZNode
decl_stmt|;
comment|/** The cluster key of the local cluster */
specifier|protected
specifier|final
name|String
name|ourClusterKey
decl_stmt|;
comment|/** The name of the znode that contains tableCFs */
specifier|protected
specifier|final
name|String
name|tableCFsNodeName
decl_stmt|;
specifier|protected
specifier|final
name|ZKWatcher
name|zookeeper
decl_stmt|;
specifier|protected
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|protected
specifier|final
name|Abortable
name|abortable
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|ENABLED_ZNODE_BYTES
init|=
name|toByteArray
argument_list|(
name|ReplicationProtos
operator|.
name|ReplicationState
operator|.
name|State
operator|.
name|ENABLED
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|DISABLED_ZNODE_BYTES
init|=
name|toByteArray
argument_list|(
name|ReplicationProtos
operator|.
name|ReplicationState
operator|.
name|State
operator|.
name|DISABLED
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ZOOKEEPER_ZNODE_REPLICATION_HFILE_REFS_KEY
init|=
literal|"zookeeper.znode.replication.hfile.refs"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ZOOKEEPER_ZNODE_REPLICATION_HFILE_REFS_DEFAULT
init|=
literal|"hfile-refs"
decl_stmt|;
specifier|public
name|ReplicationStateZKBase
parameter_list|(
name|ZKWatcher
name|zookeeper
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|Abortable
name|abortable
parameter_list|)
block|{
name|this
operator|.
name|zookeeper
operator|=
name|zookeeper
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|abortable
operator|=
name|abortable
expr_stmt|;
name|String
name|replicationZNodeName
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.replication"
argument_list|,
literal|"replication"
argument_list|)
decl_stmt|;
name|String
name|peersZNodeName
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.replication.peers"
argument_list|,
literal|"peers"
argument_list|)
decl_stmt|;
name|String
name|queuesZNodeName
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.replication.rs"
argument_list|,
literal|"rs"
argument_list|)
decl_stmt|;
name|String
name|hfileRefsZNodeName
init|=
name|conf
operator|.
name|get
argument_list|(
name|ZOOKEEPER_ZNODE_REPLICATION_HFILE_REFS_KEY
argument_list|,
name|ZOOKEEPER_ZNODE_REPLICATION_HFILE_REFS_DEFAULT
argument_list|)
decl_stmt|;
name|this
operator|.
name|peerStateNodeName
operator|=
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.replication.peers.state"
argument_list|,
literal|"peer-state"
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableCFsNodeName
operator|=
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.replication.peers.tableCFs"
argument_list|,
literal|"tableCFs"
argument_list|)
expr_stmt|;
name|this
operator|.
name|ourClusterKey
operator|=
name|ZKConfig
operator|.
name|getZooKeeperClusterKey
argument_list|(
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|replicationZNode
operator|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|zookeeper
operator|.
name|znodePaths
operator|.
name|baseZNode
argument_list|,
name|replicationZNodeName
argument_list|)
expr_stmt|;
name|this
operator|.
name|peersZNode
operator|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|replicationZNode
argument_list|,
name|peersZNodeName
argument_list|)
expr_stmt|;
name|this
operator|.
name|queuesZNode
operator|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|replicationZNode
argument_list|,
name|queuesZNodeName
argument_list|)
expr_stmt|;
name|this
operator|.
name|hfileRefsZNode
operator|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|replicationZNode
argument_list|,
name|hfileRefsZNodeName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getListOfReplicators
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|result
init|=
literal|null
decl_stmt|;
try|try
block|{
name|result
operator|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|this
operator|.
name|queuesZNode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|this
operator|.
name|abortable
operator|.
name|abort
argument_list|(
literal|"Failed to get list of replicators"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * @param state the state to convert into a byte array    * @return Serialized protobuf of<code>state</code> with pb magic prefix prepended suitable for    *         use as content of a peer-state znode under a peer cluster id as in    *         /hbase/replication/peers/PEER_ID/peer-state.    */
specifier|protected
specifier|static
name|byte
index|[]
name|toByteArray
parameter_list|(
specifier|final
name|ReplicationProtos
operator|.
name|ReplicationState
operator|.
name|State
name|state
parameter_list|)
block|{
name|ReplicationProtos
operator|.
name|ReplicationState
name|msg
init|=
name|ReplicationProtos
operator|.
name|ReplicationState
operator|.
name|newBuilder
argument_list|()
operator|.
name|setState
argument_list|(
name|state
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// There is no toByteArray on this pb Message?
comment|// 32 bytes is default which seems fair enough here.
try|try
init|(
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
init|)
block|{
name|CodedOutputStream
name|cos
init|=
name|CodedOutputStream
operator|.
name|newInstance
argument_list|(
name|baos
argument_list|,
literal|16
argument_list|)
decl_stmt|;
name|msg
operator|.
name|writeTo
argument_list|(
name|cos
argument_list|)
expr_stmt|;
name|cos
operator|.
name|flush
argument_list|()
expr_stmt|;
name|baos
operator|.
name|flush
argument_list|()
expr_stmt|;
return|return
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|boolean
name|peerExists
parameter_list|(
name|String
name|id
parameter_list|)
throws|throws
name|KeeperException
block|{
return|return
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|peersZNode
argument_list|,
name|id
argument_list|)
argument_list|)
operator|>=
literal|0
return|;
block|}
comment|/**    * Determine if a ZK path points to a peer node.    * @param path path to be checked    * @return true if the path points to a peer node, otherwise false    */
specifier|protected
name|boolean
name|isPeerPath
parameter_list|(
name|String
name|path
parameter_list|)
block|{
return|return
name|path
operator|.
name|split
argument_list|(
literal|"/"
argument_list|)
operator|.
name|length
operator|==
name|peersZNode
operator|.
name|split
argument_list|(
literal|"/"
argument_list|)
operator|.
name|length
operator|+
literal|1
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|protected
name|String
name|getTableCFsNode
parameter_list|(
name|String
name|id
parameter_list|)
block|{
return|return
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|peersZNode
argument_list|,
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|id
argument_list|,
name|this
operator|.
name|tableCFsNodeName
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|protected
name|String
name|getPeerStateNode
parameter_list|(
name|String
name|id
parameter_list|)
block|{
return|return
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|peersZNode
argument_list|,
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|id
argument_list|,
name|this
operator|.
name|peerStateNodeName
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|protected
name|String
name|getPeerNode
parameter_list|(
name|String
name|id
parameter_list|)
block|{
return|return
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|peersZNode
argument_list|,
name|id
argument_list|)
return|;
block|}
block|}
end_class

end_unit

