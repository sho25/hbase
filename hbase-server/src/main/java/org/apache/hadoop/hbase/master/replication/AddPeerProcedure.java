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
name|replication
operator|.
name|ReplicationPeerConfigUtil
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
name|MasterCoprocessorHost
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
name|procedure
operator|.
name|MasterProcedureEnv
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
name|procedure2
operator|.
name|ProcedureStateSerializer
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
name|ReplicationPeerConfig
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
name|MasterProcedureProtos
operator|.
name|AddPeerStateData
import|;
end_import

begin_comment
comment|/**  * The procedure for adding a new replication peer.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|AddPeerProcedure
extends|extends
name|ModifyPeerProcedure
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
name|AddPeerProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ReplicationPeerConfig
name|peerConfig
decl_stmt|;
specifier|private
name|boolean
name|enabled
decl_stmt|;
specifier|public
name|AddPeerProcedure
parameter_list|()
block|{   }
specifier|public
name|AddPeerProcedure
parameter_list|(
name|String
name|peerId
parameter_list|,
name|ReplicationPeerConfig
name|peerConfig
parameter_list|,
name|boolean
name|enabled
parameter_list|)
block|{
name|super
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|this
operator|.
name|peerConfig
operator|=
name|peerConfig
expr_stmt|;
name|this
operator|.
name|enabled
operator|=
name|enabled
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|PeerOperationType
name|getPeerOperationType
parameter_list|()
block|{
return|return
name|PeerOperationType
operator|.
name|ADD
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|prePeerModification
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|MasterCoprocessorHost
name|cpHost
init|=
name|env
operator|.
name|getMasterCoprocessorHost
argument_list|()
decl_stmt|;
if|if
condition|(
name|cpHost
operator|!=
literal|null
condition|)
block|{
name|cpHost
operator|.
name|preAddReplicationPeer
argument_list|(
name|peerId
argument_list|,
name|peerConfig
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|updatePeerStorage
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|env
operator|.
name|getReplicationManager
argument_list|()
operator|.
name|addReplicationPeer
argument_list|(
name|peerId
argument_list|,
name|peerConfig
argument_list|,
name|enabled
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|postPeerModification
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Successfully added "
operator|+
operator|(
name|enabled
condition|?
literal|"ENABLED"
else|:
literal|"DISABLED"
operator|)
operator|+
literal|" peer "
operator|+
name|peerId
operator|+
literal|", config "
operator|+
name|peerConfig
argument_list|)
expr_stmt|;
name|MasterCoprocessorHost
name|cpHost
init|=
name|env
operator|.
name|getMasterCoprocessorHost
argument_list|()
decl_stmt|;
if|if
condition|(
name|cpHost
operator|!=
literal|null
condition|)
block|{
name|env
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|postAddReplicationPeer
argument_list|(
name|peerId
argument_list|,
name|peerConfig
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|serializeStateData
parameter_list|(
name|ProcedureStateSerializer
name|serializer
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|serializeStateData
argument_list|(
name|serializer
argument_list|)
expr_stmt|;
name|serializer
operator|.
name|serialize
argument_list|(
name|AddPeerStateData
operator|.
name|newBuilder
argument_list|()
operator|.
name|setPeerConfig
argument_list|(
name|ReplicationPeerConfigUtil
operator|.
name|convert
argument_list|(
name|peerConfig
argument_list|)
argument_list|)
operator|.
name|setEnabled
argument_list|(
name|enabled
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|deserializeStateData
parameter_list|(
name|ProcedureStateSerializer
name|serializer
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|deserializeStateData
argument_list|(
name|serializer
argument_list|)
expr_stmt|;
name|AddPeerStateData
name|data
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|AddPeerStateData
operator|.
name|class
argument_list|)
decl_stmt|;
name|peerConfig
operator|=
name|ReplicationPeerConfigUtil
operator|.
name|convert
argument_list|(
name|data
operator|.
name|getPeerConfig
argument_list|()
argument_list|)
expr_stmt|;
name|enabled
operator|=
name|data
operator|.
name|getEnabled
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

