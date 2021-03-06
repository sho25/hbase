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
name|Optional
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
name|master
operator|.
name|procedure
operator|.
name|PeerProcedureInterface
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
name|RSProcedureDispatcher
operator|.
name|ServerOperation
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
name|ServerRemoteProcedure
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
name|procedure2
operator|.
name|RemoteProcedureDispatcher
operator|.
name|RemoteOperation
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
name|RemoteProcedureDispatcher
operator|.
name|RemoteProcedure
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
name|regionserver
operator|.
name|RefreshPeerCallable
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
name|MasterProcedureProtos
operator|.
name|PeerModificationType
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
name|RefreshPeerParameter
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
name|RefreshPeerStateData
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RefreshPeerProcedure
extends|extends
name|ServerRemoteProcedure
implements|implements
name|PeerProcedureInterface
implements|,
name|RemoteProcedure
argument_list|<
name|MasterProcedureEnv
argument_list|,
name|ServerName
argument_list|>
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
name|RefreshPeerProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|String
name|peerId
decl_stmt|;
specifier|private
name|PeerOperationType
name|type
decl_stmt|;
specifier|private
name|int
name|stage
decl_stmt|;
specifier|public
name|RefreshPeerProcedure
parameter_list|()
block|{   }
specifier|public
name|RefreshPeerProcedure
parameter_list|(
name|String
name|peerId
parameter_list|,
name|PeerOperationType
name|type
parameter_list|,
name|ServerName
name|targetServer
parameter_list|)
block|{
name|this
argument_list|(
name|peerId
argument_list|,
name|type
argument_list|,
name|targetServer
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RefreshPeerProcedure
parameter_list|(
name|String
name|peerId
parameter_list|,
name|PeerOperationType
name|type
parameter_list|,
name|ServerName
name|targetServer
parameter_list|,
name|int
name|stage
parameter_list|)
block|{
name|this
operator|.
name|peerId
operator|=
name|peerId
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|targetServer
operator|=
name|targetServer
expr_stmt|;
name|this
operator|.
name|stage
operator|=
name|stage
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getPeerId
parameter_list|()
block|{
return|return
name|peerId
return|;
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
name|REFRESH
return|;
block|}
specifier|private
specifier|static
name|PeerModificationType
name|toPeerModificationType
parameter_list|(
name|PeerOperationType
name|type
parameter_list|)
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|ADD
case|:
return|return
name|PeerModificationType
operator|.
name|ADD_PEER
return|;
case|case
name|REMOVE
case|:
return|return
name|PeerModificationType
operator|.
name|REMOVE_PEER
return|;
case|case
name|ENABLE
case|:
return|return
name|PeerModificationType
operator|.
name|ENABLE_PEER
return|;
case|case
name|DISABLE
case|:
return|return
name|PeerModificationType
operator|.
name|DISABLE_PEER
return|;
case|case
name|UPDATE_CONFIG
case|:
return|return
name|PeerModificationType
operator|.
name|UPDATE_PEER_CONFIG
return|;
case|case
name|TRANSIT_SYNC_REPLICATION_STATE
case|:
return|return
name|PeerModificationType
operator|.
name|TRANSIT_SYNC_REPLICATION_STATE
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown type: "
operator|+
name|type
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|PeerOperationType
name|toPeerOperationType
parameter_list|(
name|PeerModificationType
name|type
parameter_list|)
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|ADD_PEER
case|:
return|return
name|PeerOperationType
operator|.
name|ADD
return|;
case|case
name|REMOVE_PEER
case|:
return|return
name|PeerOperationType
operator|.
name|REMOVE
return|;
case|case
name|ENABLE_PEER
case|:
return|return
name|PeerOperationType
operator|.
name|ENABLE
return|;
case|case
name|DISABLE_PEER
case|:
return|return
name|PeerOperationType
operator|.
name|DISABLE
return|;
case|case
name|UPDATE_PEER_CONFIG
case|:
return|return
name|PeerOperationType
operator|.
name|UPDATE_CONFIG
return|;
case|case
name|TRANSIT_SYNC_REPLICATION_STATE
case|:
return|return
name|PeerOperationType
operator|.
name|TRANSIT_SYNC_REPLICATION_STATE
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown type: "
operator|+
name|type
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|RemoteOperation
argument_list|>
name|remoteCallBuild
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|ServerName
name|remote
parameter_list|)
block|{
assert|assert
name|targetServer
operator|.
name|equals
argument_list|(
name|remote
argument_list|)
assert|;
return|return
name|Optional
operator|.
name|of
argument_list|(
operator|new
name|ServerOperation
argument_list|(
name|this
argument_list|,
name|getProcId
argument_list|()
argument_list|,
name|RefreshPeerCallable
operator|.
name|class
argument_list|,
name|RefreshPeerParameter
operator|.
name|newBuilder
argument_list|()
operator|.
name|setPeerId
argument_list|(
name|peerId
argument_list|)
operator|.
name|setType
argument_list|(
name|toPeerModificationType
argument_list|(
name|type
argument_list|)
argument_list|)
operator|.
name|setTargetServer
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|remote
argument_list|)
argument_list|)
operator|.
name|setStage
argument_list|(
name|stage
argument_list|)
operator|.
name|build
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|complete
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|Throwable
name|error
parameter_list|)
block|{
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Refresh peer {} for {} on {} failed"
argument_list|,
name|peerId
argument_list|,
name|type
argument_list|,
name|targetServer
argument_list|,
name|error
argument_list|)
expr_stmt|;
name|this
operator|.
name|succ
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Refresh peer {} for {} on {} suceeded"
argument_list|,
name|peerId
argument_list|,
name|type
argument_list|,
name|targetServer
argument_list|)
expr_stmt|;
name|this
operator|.
name|succ
operator|=
literal|true
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|rollback
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|abort
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
comment|// TODO: no correctness problem if we just ignore this, implement later.
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|waitInitialized
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
return|return
name|env
operator|.
name|waitInitialized
argument_list|(
name|this
argument_list|)
return|;
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
name|serializer
operator|.
name|serialize
argument_list|(
name|RefreshPeerStateData
operator|.
name|newBuilder
argument_list|()
operator|.
name|setPeerId
argument_list|(
name|peerId
argument_list|)
operator|.
name|setType
argument_list|(
name|toPeerModificationType
argument_list|(
name|type
argument_list|)
argument_list|)
operator|.
name|setTargetServer
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|targetServer
argument_list|)
argument_list|)
operator|.
name|setStage
argument_list|(
name|stage
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
name|RefreshPeerStateData
name|data
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|RefreshPeerStateData
operator|.
name|class
argument_list|)
decl_stmt|;
name|peerId
operator|=
name|data
operator|.
name|getPeerId
argument_list|()
expr_stmt|;
name|type
operator|=
name|toPeerOperationType
argument_list|(
name|data
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|targetServer
operator|=
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|data
operator|.
name|getTargetServer
argument_list|()
argument_list|)
expr_stmt|;
name|stage
operator|=
name|data
operator|.
name|getStage
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

