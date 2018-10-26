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
name|Arrays
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
name|fs
operator|.
name|Path
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
name|hadoop
operator|.
name|hbase
operator|.
name|procedure2
operator|.
name|ProcedureYieldException
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
name|RecoverStandbyState
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
name|RecoverStandbyStateData
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RecoverStandbyProcedure
extends|extends
name|AbstractPeerNoLockProcedure
argument_list|<
name|RecoverStandbyState
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
name|RecoverStandbyProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|serial
decl_stmt|;
specifier|public
name|RecoverStandbyProcedure
parameter_list|()
block|{   }
specifier|public
name|RecoverStandbyProcedure
parameter_list|(
name|String
name|peerId
parameter_list|,
name|boolean
name|serial
parameter_list|)
block|{
name|super
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|this
operator|.
name|serial
operator|=
name|serial
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
name|RecoverStandbyState
name|state
parameter_list|)
throws|throws
name|ProcedureSuspendedException
throws|,
name|ProcedureYieldException
throws|,
name|InterruptedException
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
name|RENAME_SYNC_REPLICATION_WALS_DIR
case|:
try|try
block|{
name|syncReplicationReplayWALManager
operator|.
name|renameToPeerReplayWALDir
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to rename remote wal dir for peer id={}"
argument_list|,
name|peerId
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|setFailure
argument_list|(
literal|"master-recover-standby"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|NO_MORE_STATE
return|;
block|}
name|setNextState
argument_list|(
name|RecoverStandbyState
operator|.
name|REGISTER_PEER_TO_WORKER_STORAGE
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|REGISTER_PEER_TO_WORKER_STORAGE
case|:
try|try
block|{
name|syncReplicationReplayWALManager
operator|.
name|registerPeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ReplicationException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to register peer to worker storage for peer id={}, retry"
argument_list|,
name|peerId
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ProcedureYieldException
argument_list|()
throw|;
block|}
name|setNextState
argument_list|(
name|RecoverStandbyState
operator|.
name|DISPATCH_WALS
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|DISPATCH_WALS
case|:
name|dispathWals
argument_list|(
name|syncReplicationReplayWALManager
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|RecoverStandbyState
operator|.
name|UNREGISTER_PEER_FROM_WORKER_STORAGE
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|UNREGISTER_PEER_FROM_WORKER_STORAGE
case|:
try|try
block|{
name|syncReplicationReplayWALManager
operator|.
name|unregisterPeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ReplicationException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to unregister peer from worker storage for peer id={}, retry"
argument_list|,
name|peerId
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ProcedureYieldException
argument_list|()
throw|;
block|}
name|setNextState
argument_list|(
name|RecoverStandbyState
operator|.
name|SNAPSHOT_SYNC_REPLICATION_WALS_DIR
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|SNAPSHOT_SYNC_REPLICATION_WALS_DIR
case|:
try|try
block|{
name|syncReplicationReplayWALManager
operator|.
name|renameToPeerSnapshotWALDir
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to cleanup replay wals dir for peer id={}, , retry"
argument_list|,
name|peerId
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ProcedureYieldException
argument_list|()
throw|;
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
comment|// TODO: dispatch wals by region server when serial is true and sort wals
specifier|private
name|void
name|dispathWals
parameter_list|(
name|SyncReplicationReplayWALManager
name|syncReplicationReplayWALManager
parameter_list|)
throws|throws
name|ProcedureYieldException
block|{
try|try
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|wals
init|=
name|syncReplicationReplayWALManager
operator|.
name|getReplayWALsAndCleanUpUnusedFiles
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
name|addChildProcedure
argument_list|(
name|wals
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|wal
lambda|->
operator|new
name|SyncReplicationReplayWALProcedure
argument_list|(
name|peerId
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|syncReplicationReplayWALManager
operator|.
name|removeWALRootPath
argument_list|(
name|wal
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|toArray
argument_list|(
name|SyncReplicationReplayWALProcedure
index|[]
operator|::
operator|new
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
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to get replay wals for peer id={}, , retry"
argument_list|,
name|peerId
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ProcedureYieldException
argument_list|()
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|RecoverStandbyState
name|getState
parameter_list|(
name|int
name|stateId
parameter_list|)
block|{
return|return
name|RecoverStandbyState
operator|.
name|forNumber
argument_list|(
name|stateId
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|getStateId
parameter_list|(
name|RecoverStandbyState
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
name|RecoverStandbyState
name|getInitialState
parameter_list|()
block|{
return|return
name|RecoverStandbyState
operator|.
name|RENAME_SYNC_REPLICATION_WALS_DIR
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
name|RECOVER_STANDBY
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
name|serializer
operator|.
name|serialize
argument_list|(
name|RecoverStandbyStateData
operator|.
name|newBuilder
argument_list|()
operator|.
name|setSerial
argument_list|(
name|serial
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
name|RecoverStandbyStateData
name|data
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|RecoverStandbyStateData
operator|.
name|class
argument_list|)
decl_stmt|;
name|serial
operator|=
name|data
operator|.
name|getSerial
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

