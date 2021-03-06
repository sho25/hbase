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
name|ProcedureSuspendedException
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
name|SyncReplicationReplayWALState
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
name|SyncReplicationReplayWALStateData
import|;
end_import

begin_comment
comment|/**  * The procedure for replaying a set of remote wals. It will get an available region server and  * schedule a {@link SyncReplicationReplayWALRemoteProcedure} to actually send the request to region  * server.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SyncReplicationReplayWALProcedure
extends|extends
name|AbstractPeerNoLockProcedure
argument_list|<
name|SyncReplicationReplayWALState
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
name|SyncReplicationReplayWALProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ServerName
name|worker
init|=
literal|null
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|wals
decl_stmt|;
specifier|public
name|SyncReplicationReplayWALProcedure
parameter_list|()
block|{   }
specifier|public
name|SyncReplicationReplayWALProcedure
parameter_list|(
name|String
name|peerId
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|wals
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
name|wals
operator|=
name|wals
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Flow
name|executeFromState
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|SyncReplicationReplayWALState
name|state
parameter_list|)
throws|throws
name|ProcedureSuspendedException
block|{
name|SyncReplicationReplayWALManager
name|syncReplicationReplayWALManager
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getSyncReplicationReplayWALManager
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|ASSIGN_WORKER
case|:
name|worker
operator|=
name|syncReplicationReplayWALManager
operator|.
name|acquirePeerWorker
argument_list|(
name|peerId
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|SyncReplicationReplayWALState
operator|.
name|DISPATCH_WALS_TO_WORKER
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|DISPATCH_WALS_TO_WORKER
case|:
name|addChildProcedure
argument_list|(
operator|new
name|SyncReplicationReplayWALRemoteProcedure
argument_list|(
name|peerId
argument_list|,
name|wals
argument_list|,
name|worker
argument_list|)
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|SyncReplicationReplayWALState
operator|.
name|RELEASE_WORKER
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|RELEASE_WORKER
case|:
name|boolean
name|finished
init|=
literal|false
decl_stmt|;
try|try
block|{
name|finished
operator|=
name|syncReplicationReplayWALManager
operator|.
name|isReplayWALFinished
argument_list|(
name|wals
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|suspend
argument_list|(
name|env
operator|.
name|getMasterConfiguration
argument_list|()
argument_list|,
name|backoff
lambda|->
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to check whether replay wals {} finished for peer id={}"
operator|+
literal|", sleep {} secs and retry"
argument_list|,
name|wals
argument_list|,
name|peerId
argument_list|,
name|backoff
operator|/
literal|1000
argument_list|,
name|e
argument_list|)
argument_list|)
throw|;
block|}
name|syncReplicationReplayWALManager
operator|.
name|releasePeerWorker
argument_list|(
name|peerId
argument_list|,
name|worker
argument_list|,
name|env
operator|.
name|getProcedureScheduler
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|finished
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to replay wals {} for peer id={}, retry"
argument_list|,
name|wals
argument_list|,
name|peerId
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|SyncReplicationReplayWALState
operator|.
name|ASSIGN_WORKER
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
block|}
return|return
name|Flow
operator|.
name|NO_MORE_STATE
return|;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"unhandled state="
operator|+
name|state
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|rollbackState
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|SyncReplicationReplayWALState
name|state
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|state
operator|==
name|getInitialState
argument_list|()
condition|)
block|{
return|return;
block|}
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|protected
name|SyncReplicationReplayWALState
name|getState
parameter_list|(
name|int
name|state
parameter_list|)
block|{
return|return
name|SyncReplicationReplayWALState
operator|.
name|forNumber
argument_list|(
name|state
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|getStateId
parameter_list|(
name|SyncReplicationReplayWALState
name|state
parameter_list|)
block|{
return|return
name|state
operator|.
name|getNumber
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|SyncReplicationReplayWALState
name|getInitialState
parameter_list|()
block|{
return|return
name|SyncReplicationReplayWALState
operator|.
name|ASSIGN_WORKER
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
name|super
operator|.
name|serializeStateData
argument_list|(
name|serializer
argument_list|)
expr_stmt|;
name|SyncReplicationReplayWALStateData
operator|.
name|Builder
name|builder
init|=
name|SyncReplicationReplayWALStateData
operator|.
name|newBuilder
argument_list|()
operator|.
name|setPeerId
argument_list|(
name|peerId
argument_list|)
operator|.
name|addAllWal
argument_list|(
name|wals
argument_list|)
decl_stmt|;
if|if
condition|(
name|worker
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setWorker
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|worker
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|serializer
operator|.
name|serialize
argument_list|(
name|builder
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
name|SyncReplicationReplayWALStateData
name|data
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|SyncReplicationReplayWALStateData
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
name|wals
operator|=
name|data
operator|.
name|getWalList
argument_list|()
expr_stmt|;
if|if
condition|(
name|data
operator|.
name|hasWorker
argument_list|()
condition|)
block|{
name|worker
operator|=
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|data
operator|.
name|getWorker
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|SYNC_REPLICATION_REPLAY_WAL
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|afterReplay
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
comment|// If the procedure is not finished and the worker is not null, we should add it to the used
comment|// worker set, to prevent the worker being used by others.
if|if
condition|(
name|worker
operator|!=
literal|null
operator|&&
operator|!
name|isFinished
argument_list|()
condition|)
block|{
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getSyncReplicationReplayWALManager
argument_list|()
operator|.
name|addUsedPeerWorker
argument_list|(
name|peerId
argument_list|,
name|worker
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

